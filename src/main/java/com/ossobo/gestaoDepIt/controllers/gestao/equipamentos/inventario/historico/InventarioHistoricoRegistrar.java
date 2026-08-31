/*
 * InventarioHistoricoRegistrar v2.2
 *
 * Classe utilitária para registro de histórico de inventário.
 * Política 97/2/1: ZERO service injetado — comunicação exclusiva via Rotas
 * (contratos da HistoricoEventosRoutes). Construtor vazio.
 *
 * REGRA ÚNICA:
 * - Se NÃO existe linha anterior para funcionario_id + sku_produto → CRIACAO
 * - Se EXISTE linha anterior → ATUALIZACAO (dados_anteriores = linha.dados_novos)
 *
 * ORDENAÇÃO GARANTIDA POR DATA:
 * - A busca da última linha usa created_at (data de criação do evento)
 * - Fonte agora é a rota "todos" (sem teto de paginação — a última linha
 *   nunca fica fora; com o volume legado de ~287 eventos, custo irrelevante.
 *   Backlog: migrar para com-filtros/ordenação server-side se o volume crescer)
 *
 * CICLO RESULTANTE:
 * Linha 1: CRIACAO      [null]              → [Dados Iniciais]      ← 2024-01-10 09:00
 * Linha 2: ATUALIZACAO  [Dados Iniciais]    → [Dados Atualizados]   ← 2024-03-15 14:30
 * Linha 3: ATUALIZACAO  [Dados Atualizados] → [Dados Novos]         ← 2024-06-20 11:00
 *
 * v2.2: Política 97/2/1 — HistoricoEventosService eliminado; fonte via
 *       Rotas.get(".../todos") [chave "eventos"] e persistência via
 *       Rotas.put(".../registrar", Payload "evento"). Import Params
 *       confirmado contra exemplo canônico: router.model.Params.
 * v2.0: Migração Degrau 1 — records v3.0 (accessors, id String UUID) +
 *       criação via HistoricoEventos.porTipo() (v3.1: UUID na fábrica).
 * v1.2: Garantia de ordenação por data na busca da última linha
 * v1.1: Simplificado — regra única, sem alternância
 * v1.0: Versão inicial
 */
package com.ossobo.gestaoDepIt.controllers.gestao.equipamentos.inventario.historico;

import com.ossobo.gestaoDepIt.db.models.HistoricoEventos;
import com.ossobo.gestaoDepIt.db.models.InventarioEquipamentos;
import com.ossobo.winterfx.router.Rotas;
import com.ossobo.winterfx.router.model.Params;
import com.ossobo.winterfx.router.model.ResponseData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class InventarioHistoricoRegistrar {

    private static final Logger logger = LoggerFactory.getLogger(InventarioHistoricoRegistrar.class);

    /** Contrato: HistoricoEventosRoutes @GetMapping("todos") — chaves de retorno "eventos" + "total". */
    private static final String ROTA_TODOS = "historico-eventos/service/todos";

    /** Contrato: HistoricoEventosRoutes @PutMapping("registrar") — payload "evento". */
    private static final String ROTA_REGISTRAR = "historico-eventos/service/registrar";

    /** Construtor vazio — dependências chegam pela infra de Rotas, não por injeção. */
    public InventarioHistoricoRegistrar() { }

    // =========================================================================
    // MÉTODO PRINCIPAL
    // =========================================================================

    /**
     * Registra o histórico de uma operação de inventário.
     * REGRA ÚNICA: Se existe linha anterior → ATUALIZACAO. Senão → CRIACAO.
     * A linha mais recente é determinada por created_at (data de criação do evento).
     */
    public void registrar(InventarioEquipamentos equipamento) {
        String jsonNovo = jsonDoEquipamento(equipamento);
        String funcionarioId = equipamento.funcionarioId();
        String skuProduto = equipamento.skuProduto();

        Optional<HistoricoEventos> ultimaLinha = buscarUltimaLinha(funcionarioId, skuProduto);

        if (ultimaLinha.isEmpty()) {
            inserir("CRIACAO", skuProduto, funcionarioId, null, jsonNovo);
            logger.info("📝 CRIACAO — Func={} SKU={} (primeira linha)", funcionarioId, skuProduto);
        } else {
            HistoricoEventos linha = ultimaLinha.get();
            String dadosAnteriores = linha.dadosNovos();
            inserir("ATUALIZACAO", skuProduto, funcionarioId, dadosAnteriores, jsonNovo);
            logger.info("✏️ ATUALIZACAO — Func={} SKU={} (linha anterior ID={} de {})",
                    funcionarioId, skuProduto, abreviar(linha.id()), linha.createdAt());
        }
    }

    /**
     * Registra ações específicas: baixa, manutenção, transferência etc.
     * Preserva dadosAnteriores para QUALQUER tipo (via porTipo — comportamento legado).
     */
    public void registrarAcao(String skuProduto, String funcionarioId, String tipoEvento, String jsonDados) {
        Optional<HistoricoEventos> ultimaLinha = buscarUltimaLinha(funcionarioId, skuProduto);
        String anterior = ultimaLinha.map(HistoricoEventos::dadosNovos).orElse(null);
        inserir(tipoEvento, skuProduto, funcionarioId, anterior, jsonDados);
        logger.info("📋 {} — Func={} SKU={}", tipoEvento, funcionarioId, skuProduto);
    }

    // =========================================================================
    // PRIVADOS — fonte e persistência via rotas (Política 97/2/1)
    // =========================================================================

    /**
     * Busca a ÚLTIMA linha do histórico para um funcionário + SKU
     * (created_at decrescente — mais recente primeiro).
     */
    private Optional<HistoricoEventos> buscarUltimaLinha(String funcionarioId, String skuProduto) {
        return listarTodosViaRota().stream()
                .filter(e -> e.funcionarioId() != null && e.funcionarioId().equals(funcionarioId))
                .filter(e -> e.skuProduto() != null && e.skuProduto().equals(skuProduto))
                .max(Comparator.comparing(HistoricoEventos::createdAt,
                        Comparator.nullsLast(Comparator.naturalOrder())));
    }

    /**
     * Fonte via rota: GET historico-eventos/service/todos.
     * Sucesso = chave "eventos" (contrato da fronteira) ·
     * falha = lista vazia + log (o registro de histórico NUNCA deve
     * quebrar a operação de negócio que o originou).
     */
    @SuppressWarnings("unchecked")
    private List<HistoricoEventos> listarTodosViaRota() {
        try {
            Object resposta = Rotas.get(ROTA_TODOS);
            if (resposta instanceof ResponseData r && r.isSuccess()) {
                Object bruto = r.getData().get("eventos");
                return bruto instanceof List ? (List<HistoricoEventos>) bruto : List.of();
            }
            logger.warn("⚠️ Rota de histórico indisponível: {}",
                    resposta instanceof ResponseData r ? r.getMessage() : "resposta inesperada");
        } catch (Exception e) {
            logger.error("❌ Erro ao listar histórico via rota", e);
        }
        return List.of();
    }

    /**
     * Cria o evento via fábrica porTipo (UUID + timestamps gerados no model)
     * e persiste via rota: PUT historico-eventos/service/registrar (payload "evento").
     * created_at nunca é setado manualmente.
     */
    private void inserir(String tipo, String sku, String funcId, String anterior, String novo) {
        HistoricoEventos ev = HistoricoEventos.porTipo(tipo, sku, funcId, anterior, novo);
        try {
            Object resposta = Rotas.put(ROTA_REGISTRAR, Params.with("evento", ev));
            if (resposta instanceof ResponseData r && !r.isSuccess()) {
                logger.warn("⚠️ Falha ao registrar evento {} — SKU={}: {}",
                        tipo, sku, r.getMessage());
            }
        } catch (Exception e) {
            logger.error("❌ Erro ao registrar evento {} — SKU={}", tipo, sku, e);
        }
    }

    // =========================================================================
    // JSON
    // =========================================================================

    public static String jsonDoEquipamento(InventarioEquipamentos e) {
        return String.format(
                "{\"sku\":\"%s\",\"funcionarioId\":\"%s\",\"numSerie\":\"%s\",\"mac\":\"%s\"," +
                        "\"localizacao\":\"%s\",\"departamento\":\"%s\",\"status\":\"%s\",\"condicao\":\"%s\"," +
                        "\"dataAquisicao\":\"%s\",\"dataInstalacao\":\"%s\",\"dataUltimaVerificacao\":\"%s\"," +
                        "\"observacoes\":\"%s\"}",
                nvl(e.skuProduto()), nvl(e.funcionarioId()), nvl(e.numSerie()), nvl(e.enderecoMac()),
                nvl(e.localizacao()), nvl(e.departamento()), nvl(e.status()), nvl(e.condicao()),
                e.dataAquisicao() != null ? e.dataAquisicao().toString() : "",
                e.dataInstalacao() != null ? e.dataInstalacao().toString() : "",
                e.dataUltimaVerificacao() != null ? e.dataUltimaVerificacao().toString() : "",
                nvl(e.observacoes()).replace("\"", "'")
        );
    }

    private static String abreviar(String id) {
        return (id != null && id.length() > 8) ? id.substring(0, 8) + "…" : String.valueOf(id);
    }

    private static String nvl(String s) { return s != null && !s.isEmpty() ? s : ""; }
}