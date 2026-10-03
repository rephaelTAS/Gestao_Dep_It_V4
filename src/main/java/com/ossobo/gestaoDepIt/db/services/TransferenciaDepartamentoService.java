package com.ossobo.gestaoDepIt.db.services;

import com.ossobo.gestaoDepIt.db.config.DatabaseConnection;
import com.ossobo.gestaoDepIt.db.models.Funcionarios;
import com.ossobo.gestaoDepIt.db.models.HistoricoEventos;
import com.ossobo.gestaoDepIt.db.models.InventarioEquipamentos;
import com.ossobo.gestaoDepIt.db.repositories.FuncionariosRepository;
import com.ossobo.gestaoDepIt.db.repositories.HistoricoEventosRepository;
import com.ossobo.gestaoDepIt.db.repositories.InventarioEquipamentosRepository;
import com.ossobo.gestaoDepIt.utils.gerarCodDep.GerarCodDepService;
import com.ossobo.winterfx.anotations.Inject;
import com.ossobo.winterfx.anotations.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * TransferenciaDepartamentoService v1.0
 *
 * Transferência atômica de um funcionário para outro departamento.
 *
 * Fluxo (tudo em UMA transação):
 *   1. Valida funcionário + departamento diferente.
 *   2. Gera novo cod_dep (GerarCodDepService — transação própria).
 *   3. Busca no inventário TODOS os equipamentos do funcionário atual.
 *   4. Para cada equipamento:
 *      a. Atualiza funcionario_id para o novo cod_dep.
 *      b. Lê o ÚLTIMO dados_novos pelo (sku, coddep_antigo, numSerie) → anteriores.
 *      c. Insere evento TRANSFERENCIA (anteriores, novos=snapshot atual).
 *   5. Atualiza funcionarios.cod_dep + cod_dep_anterior.
 *   6. Commit.
 *
 * A chave do histórico é RÍGIDA: (sku_produto, funcionario_id, num_serie) —
 * identifica o equipamento físico sem misturar históricos de SKUs iguais.
 */
@Service
public class TransferenciaDepartamentoService {

    private static final System.Logger logger =
            System.getLogger(TransferenciaDepartamentoService.class.getName());

    @Inject private FuncionariosRepository funcionariosRepository;
    @Inject private InventarioEquipamentosRepository inventarioRepository;
    @Inject private HistoricoEventosRepository historicoRepository;
    @Inject private GerarCodDepService gerarCodDepService;
    @Inject private DatabaseConnection dbConnection;

    /**
     * Transfere um funcionário para outro departamento.
     *
     * Regra do ledger (v1.1):
     *   - funcionario_id (coluna) = NOVO coddep (dono atual do equipamento).
     *   - dados_anteriores (JSON) = snapshot ANTES (tem o coddep antigo).
     *   - dados_novos (JSON)      = snapshot DEPOIS (tem o coddep novo).
     *
     * O coddep antigo vive APENAS em dados_anteriores.
     * O executor vai em descricao_funcionario, para auditoria.
     *
     * @return novo cod_dep gerado
     */
    public String transferir(String codDepAtual,
                             String novoDepartamento,
                             String codDepExecutor,
                             String descricao) throws SQLException {

        // 1. Valida
        if (codDepAtual == null || codDepAtual.isBlank())
            throw new IllegalArgumentException("cod_dep atual é obrigatório");
        if (novoDepartamento == null || novoDepartamento.isBlank())
            throw new IllegalArgumentException("Novo departamento é obrigatório");
        if (codDepExecutor == null || codDepExecutor.isBlank())
            throw new IllegalArgumentException("Executor é obrigatório para auditoria");

        Funcionarios func = funcionariosRepository.findByCodDep(codDepAtual)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Funcionário não encontrado: " + codDepAtual));

        if (novoDepartamento.equals(func.departamento()))
            throw new IllegalStateException(
                    "Funcionário já está no departamento: " + novoDepartamento);

        // 2. Gera novo cod_dep FORA da transação (tem própria)
        String novoCodDep = gerarCodDepService.gerar(novoDepartamento);

        // 3. Lista equipamentos ANTES da transação (leitura pura)
        List<InventarioEquipamentos> equipamentos =
                inventarioRepository.findByFuncionarioId(codDepAtual);

        // 4-5-6. Transação composta
        try (Connection conn = dbConnection.beginTransaction()) {
            try {
                for (InventarioEquipamentos equip : equipamentos) {
                    // 4a. Snapshot ANTES — o record ainda tem o coddep antigo.
                    String dadosAnteriores = snapshotJson(equip);

                    // 4b. Atualiza inventário para o novo coddep.
                    InventarioEquipamentos atualizado = equip.comFuncionario(novoCodDep);
                    inventarioRepository.update(conn, atualizado);

                    // 4c. Snapshot DEPOIS — já reflete o novo coddep.
                    String dadosNovos = snapshotJson(atualizado);

                    // 4d. Evento:
                    //     - funcionario_id = NOVO coddep (dono atual).
                    //     - dados_anteriores = estado antes (coddep antigo).
                    //     - dados_novos = estado atual (coddep novo).
                    //     - descricao = executor + transição (auditoria).
                    String descricaoEvento = String.format(
                            "Transferência executada por %s: %s → %s",
                            codDepExecutor, codDepAtual, novoCodDep);

                    HistoricoEventos evento = HistoricoEventos.transferencia(
                            atualizado.skuProduto(),
                            atualizado.numSerie(),
                            novoCodDep,                 // ← dono ATUAL (não executor)
                            dadosAnteriores,
                            dadosNovos,
                            descricaoEvento);

                    historicoRepository.insert(conn, evento);
                }

                // 5. Atualiza o funcionário (cod_dep + cod_dep_anterior)
                funcionariosRepository.transferirDepartamento(
                        conn, func.id(), novoCodDep, codDepAtual);

                dbConnection.commit(conn);
                logger.log(System.Logger.Level.INFO,
                        "✅ Transferência concluída: {0} → {1} ({2} equipamento(s))",
                        codDepAtual, novoCodDep, equipamentos.size());
                return novoCodDep;

            } catch (SQLException e) {
                dbConnection.rollback(conn);
                logger.log(System.Logger.Level.ERROR,
                        "❌ Falha na transferência de {0}: {1}",
                        codDepAtual, e.getMessage(), e);
                throw e;
            }
        }
    }

    /** Snapshot JSON do equipamento (dados_novos do histórico). */
    private String snapshotJson(InventarioEquipamentos e) {
        return String.format(
                "{\"id\":\"%s\",\"sku\":\"%s\",\"funcionario\":\"%s\"," +
                        "\"numSerie\":\"%s\",\"status\":\"%s\",\"condicao\":\"%s\"," +
                        "\"localizacao\":\"%s\",\"departamento\":\"%s\"}",
                esc(e.id()), esc(e.skuProduto()), esc(e.funcionarioId()),
                esc(e.numSerie()), esc(e.status()), esc(e.condicao()),
                esc(e.localizacao()), esc(e.departamento()));
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}