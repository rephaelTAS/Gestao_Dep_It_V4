package com.ossobo.gestaoDepIt.db.services;

import com.ossobo.gestaoDepIt.db.config.DatabaseConnection;
import com.ossobo.gestaoDepIt.db.config.event.HistoricoEvent;
import com.ossobo.gestaoDepIt.db.config.event.InventarioEvent;
import com.ossobo.gestaoDepIt.db.enums.CondicaoEquipamento;
import com.ossobo.gestaoDepIt.db.enums.StatusEquipamento;
import com.ossobo.gestaoDepIt.db.models.HistoricoEventos;
import com.ossobo.gestaoDepIt.db.models.InventarioEquipamentos;
import com.ossobo.gestaoDepIt.db.repositories.HistoricoEventosRepository;
import com.ossobo.gestaoDepIt.db.repositories.InventarioEquipamentosRepository;
import com.ossobo.winterfx.anotations.Inject;
import com.ossobo.winterfx.anotations.Service;
import com.ossobo.winterfx.event.EventBus;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * InventarioCrudService v1.1
 *
 * Responsabilidade: ORQUESTRADOR transacional de TODAS as mutações do
 *                   inventário de equipamentos + histórico de eventos.
 *
 * Regra ratificada: inventário e histórico são inseparáveis — toda mutação
 *                   grava os dois na MESMA transação. Se qualquer escrita
 *                   falhar, rollback desfaz ambas.
 *
 * v1.1 — numSerie passa a ser argumento das fábricas de histórico:
 *        - Cada evento leva o numSerie do equipamento afetado.
 *        - dados_anteriores agora vem do último dados_novos do EQUIPAMENTO
 *          (chave rígida: sku + funcionario_id + num_serie), não do SKU.
 *        - Isso evita misturar históricos de equipamentos diferentes que
 *          compartilham o mesmo SKU.
 *
 * @since v1.1
 */
@Service
public class InventarioCrudService {

    private static final System.Logger logger =
            System.getLogger(InventarioCrudService.class.getName());

    @Inject private DatabaseConnection dbConnection;
    @Inject private InventarioEquipamentosRepository invRepo;
    @Inject private HistoricoEventosRepository histRepo;
    @Inject private HistoricoEventosService histService;
    @Inject private EventBus eventBus;

    // ============================================================
    // CADASTRAR
    // ============================================================

    public InventarioEquipamentos cadastrar(InventarioEquipamentos equip,
                                            String funcionarioExecutor,
                                            String descricaoFuncionario) throws SQLException {
        validarEquipamento(equip);

        Connection conn = dbConnection.beginTransaction();
        try {
            if (invRepo.existsByNumSerie(conn, equip.numSerie())) {
                throw new IllegalArgumentException("Número de série já cadastrado: " + equip.numSerie());
            }
            if (invRepo.existsByMacAddress(conn, equip.enderecoMac())) {
                throw new IllegalArgumentException("Endereço MAC já cadastrado: " + equip.enderecoMac());
            }

            String id = invRepo.insert(conn, equip);
            InventarioEquipamentos salvo = invRepo.findById(conn, id)
                    .orElseThrow(() -> new SQLException("Falha ao buscar equipamento cadastrado"));

            String dadosNovos = jsonEquipamento(salvo);
            String descricao = descricaoFuncionario != null ? descricaoFuncionario : "{}";

            HistoricoEventos evento = HistoricoEventos.criacao(
                    salvo.skuProduto(), salvo.numSerie(),
                    funcionarioExecutor, dadosNovos, descricao);
            histRepo.insert(conn, evento);

            dbConnection.commit(conn);

            publicarEventos(salvo, evento);
            logger.log(System.Logger.Level.INFO,
                    "✅ Cadastro transacional: ID={0}, Série={1}", salvo.id(), salvo.numSerie());
            return salvo;

        } catch (Exception ex) {
            dbConnection.rollback(conn);
            throw ex;
        }
    }

    // ============================================================
    // ATUALIZAR
    // ============================================================

    public InventarioEquipamentos atualizar(InventarioEquipamentos equip,
                                            String funcionarioExecutor,
                                            String descricaoFuncionario) throws SQLException {
        validarEquipamento(equip);
        if (equip.id() == null || equip.id().isBlank()) {
            throw new IllegalArgumentException("ID é obrigatório para atualização");
        }

        Connection conn = dbConnection.beginTransaction();
        try {
            InventarioEquipamentos anterior = invRepo.findById(conn, equip.id())
                    .orElseThrow(() -> new IllegalArgumentException("Equipamento não encontrado: " + equip.id()));

            if (invRepo.existsByNumSerie(conn, equip.numSerie())
                    && !anterior.numSerie().equals(equip.numSerie())) {
                throw new IllegalArgumentException("Número de série já utilizado por outro equipamento");
            }
            if (invRepo.existsByMacAddress(conn, equip.enderecoMac())
                    && !anterior.enderecoMac().equals(equip.enderecoMac())) {
                throw new IllegalArgumentException("Endereço MAC já utilizado por outro equipamento");
            }

            invRepo.update(conn, equip);
            InventarioEquipamentos atualizado = invRepo.findById(conn, equip.id())
                    .orElseThrow(() -> new SQLException("Falha ao buscar equipamento atualizado"));

            // v1.1: dados_anteriores vem do último dados_novos do EQUIPAMENTO
            // (chave: sku + funcionario_id_ATUAL + num_serie).
            String dadosAnteriores = lerUltimoDadosNovos(
                    conn, atualizado.skuProduto(),
                    atualizado.funcionarioId(), atualizado.numSerie())
                    .orElseGet(() -> jsonEquipamento(anterior));

            String dadosNovos = jsonEquipamento(atualizado);
            String descricao = descricaoFuncionario != null ? descricaoFuncionario : "{}";

            HistoricoEventos evento = HistoricoEventos.atualizacao(
                    atualizado.skuProduto(), atualizado.numSerie(),
                    funcionarioExecutor,
                    dadosAnteriores, dadosNovos, descricao);
            histRepo.insert(conn, evento);

            dbConnection.commit(conn);

            publicarEventos(atualizado, evento);
            logger.log(System.Logger.Level.INFO,
                    "✅ Atualização transacional: ID={0}", atualizado.id());
            return atualizado;

        } catch (Exception ex) {
            dbConnection.rollback(conn);
            throw ex;
        }
    }

    // ============================================================
    // EXCLUIR
    // ============================================================

    public void excluir(String id, String funcionarioExecutor, String descricaoFuncionario) throws SQLException {
        Connection conn = dbConnection.beginTransaction();
        try {
            InventarioEquipamentos anterior = invRepo.findById(conn, id)
                    .orElseThrow(() -> new IllegalArgumentException("Equipamento não encontrado: " + id));

            String dadosAnteriores = lerUltimoDadosNovos(
                    conn, anterior.skuProduto(), anterior.funcionarioId(), anterior.numSerie())
                    .orElseGet(() -> jsonEquipamento(anterior));
            String descricao = descricaoFuncionario != null ? descricaoFuncionario : "{}";

            HistoricoEventos evento = HistoricoEventos.exclusao(
                    anterior.skuProduto(), anterior.numSerie(),
                    funcionarioExecutor, dadosAnteriores, descricao);
            histRepo.insert(conn, evento);

            invRepo.delete(conn, id);

            dbConnection.commit(conn);

            eventBus.publish(new HistoricoEvent<>(evento, evento.tipoEvento()));
            eventBus.publish(new InventarioEvent<>(anterior, "EXCLUIDO"));
            logger.log(System.Logger.Level.INFO, "✅ Exclusão transacional: ID={0}", id);

        } catch (Exception ex) {
            dbConnection.rollback(conn);
            throw ex;
        }
    }

    // ============================================================
    // BAIXAR
    // ============================================================

    public void baixar(String id, String funcionarioExecutor, String motivo, String descricaoFuncionario) throws SQLException {
        Connection conn = dbConnection.beginTransaction();
        try {
            InventarioEquipamentos anterior = invRepo.findById(conn, id)
                    .orElseThrow(() -> new IllegalArgumentException("Equipamento não encontrado: " + id));

            if (StatusEquipamento.BAIXADO.name().equals(anterior.status())) {
                throw new IllegalStateException("Equipamento já está baixado");
            }

            String obs = appendObservacao(anterior.observacoes(), "Baixa: " + motivo);
            InventarioEquipamentos alvo = comObservacoes(anterior, obs);

            invRepo.update(conn, alvo);
            invRepo.updateStatus(conn, id, StatusEquipamento.BAIXADO.name());

            InventarioEquipamentos atualizado = invRepo.findById(conn, id)
                    .orElseThrow(() -> new SQLException("Falha ao buscar equipamento baixado"));

            String dadosAnteriores = lerUltimoDadosNovos(
                    conn, atualizado.skuProduto(), atualizado.funcionarioId(), atualizado.numSerie())
                    .orElseGet(() -> jsonEquipamento(anterior));
            String descricao = descricaoFuncionario != null ? descricaoFuncionario : "{}";

            HistoricoEventos evento = HistoricoEventos.baixa(
                    atualizado.skuProduto(), atualizado.numSerie(),
                    funcionarioExecutor, dadosAnteriores, descricao);
            histRepo.insert(conn, evento);

            dbConnection.commit(conn);

            publicarEventos(atualizado, evento);
            logger.log(System.Logger.Level.INFO, "✅ Baixa transacional: ID={0}", id);

        } catch (Exception ex) {
            dbConnection.rollback(conn);
            throw ex;
        }
    }

    // ============================================================
    // MANUTENÇÃO — ENVIAR
    // ============================================================

    public void enviarManutencao(String id, String funcionarioExecutor, String motivo, String descricaoFuncionario) throws SQLException {
        Connection conn = dbConnection.beginTransaction();
        try {
            InventarioEquipamentos anterior = invRepo.findById(conn, id)
                    .orElseThrow(() -> new IllegalArgumentException("Equipamento não encontrado: " + id));

            if (StatusEquipamento.MANUTENCAO.name().equals(anterior.status())) {
                throw new IllegalStateException("Equipamento já está em manutenção");
            }

            String obs = appendObservacao(anterior.observacoes(), "Manutenção: " + motivo);
            InventarioEquipamentos alvo = comObservacoes(anterior, obs);

            invRepo.update(conn, alvo);
            invRepo.updateStatus(conn, id, StatusEquipamento.MANUTENCAO.name());

            InventarioEquipamentos atualizado = invRepo.findById(conn, id)
                    .orElseThrow(() -> new SQLException("Falha ao buscar equipamento em manutenção"));

            String dadosAnteriores = lerUltimoDadosNovos(
                    conn, atualizado.skuProduto(), atualizado.funcionarioId(), atualizado.numSerie())
                    .orElseGet(() -> jsonEquipamento(anterior));
            String dadosNovos = jsonEquipamento(atualizado);
            String descricao = descricaoFuncionario != null ? descricaoFuncionario : "{}";

            HistoricoEventos evento = HistoricoEventos.manutencao(
                    atualizado.skuProduto(), atualizado.numSerie(),
                    funcionarioExecutor, dadosNovos, descricao);
            histRepo.insert(conn, evento);

            dbConnection.commit(conn);

            publicarEventos(atualizado, evento);
            logger.log(System.Logger.Level.INFO, "✅ Envio p/ manutenção transacional: ID={0}", id);

        } catch (Exception ex) {
            dbConnection.rollback(conn);
            throw ex;
        }
    }

    // ============================================================
    // MANUTENÇÃO — RETORNAR
    // ============================================================

    public void retornarManutencao(String id, String funcionarioExecutor,
                                   String condicaoPos, String observacoes,
                                   String descricaoFuncionario) throws SQLException {
        if (!CondicaoEquipamento.isValido(condicaoPos)) {
            throw new IllegalArgumentException("Condição inválida: " + condicaoPos);
        }

        Connection conn = dbConnection.beginTransaction();
        try {
            InventarioEquipamentos anterior = invRepo.findById(conn, id)
                    .orElseThrow(() -> new IllegalArgumentException("Equipamento não encontrado: " + id));

            if (!StatusEquipamento.MANUTENCAO.name().equals(anterior.status())) {
                throw new IllegalStateException("Equipamento não está em manutenção");
            }

            String obsCompleta = appendObservacao(anterior.observacoes(), "Verificação: " + observacoes);

            invRepo.updateStatus(conn, id, StatusEquipamento.ATIVO.name());
            invRepo.updateVerificacao(conn, id, LocalDate.now(), condicaoPos, obsCompleta);

            InventarioEquipamentos atualizado = invRepo.findById(conn, id)
                    .orElseThrow(() -> new SQLException("Falha ao buscar equipamento retornado"));

            String dadosAnteriores = lerUltimoDadosNovos(
                    conn, atualizado.skuProduto(), atualizado.funcionarioId(), atualizado.numSerie())
                    .orElseGet(() -> jsonEquipamento(anterior));
            String dadosNovos = jsonEquipamento(atualizado);
            String descricao = descricaoFuncionario != null ? descricaoFuncionario : "{}";

            HistoricoEventos evento = HistoricoEventos.atualizacao(
                    atualizado.skuProduto(), atualizado.numSerie(),
                    funcionarioExecutor,
                    dadosAnteriores, dadosNovos, descricao);
            histRepo.insert(conn, evento);

            dbConnection.commit(conn);

            publicarEventos(atualizado, evento);
            logger.log(System.Logger.Level.INFO, "✅ Retorno de manutenção transacional: ID={0}", id);

        } catch (Exception ex) {
            dbConnection.rollback(conn);
            throw ex;
        }
    }

    // ============================================================
    // TRANSFERIR LOCALIZAÇÃO (mesmo funcionário, muda de sala)
    // ============================================================

    public void transferirLocalizacao(String id, String funcionarioExecutor,
                                      String localizacao, String departamento,
                                      String descricaoFuncionario) throws SQLException {
        Connection conn = dbConnection.beginTransaction();
        try {
            InventarioEquipamentos anterior = invRepo.findById(conn, id)
                    .orElseThrow(() -> new IllegalArgumentException("Equipamento não encontrado: " + id));

            invRepo.updateLocalizacao(conn, id, localizacao, departamento);

            InventarioEquipamentos atualizado = invRepo.findById(conn, id)
                    .orElseThrow(() -> new SQLException("Falha ao buscar equipamento transferido"));

            String dadosAnteriores = lerUltimoDadosNovos(
                    conn, atualizado.skuProduto(), atualizado.funcionarioId(), atualizado.numSerie())
                    .orElseGet(() -> jsonEquipamento(anterior));
            String dadosNovos = jsonEquipamento(atualizado);
            String descricao = descricaoFuncionario != null ? descricaoFuncionario : "{}";

            HistoricoEventos evento = HistoricoEventos.localizacao(
                    atualizado.skuProduto(), atualizado.numSerie(),
                    funcionarioExecutor, dadosNovos, descricao);
            histRepo.insert(conn, evento);

            dbConnection.commit(conn);

            publicarEventos(atualizado, evento);
            logger.log(System.Logger.Level.INFO, "✅ Localização transacional: ID={0}", id);

        } catch (Exception ex) {
            dbConnection.rollback(conn);
            throw ex;
        }
    }

    // ============================================================
    // REASSOCIAR FUNCIONÁRIO (muda de dono)
    // ============================================================

    public void reassociarFuncionario(String id, String funcionarioExecutor,
                                      String novoFuncionarioId,
                                      String descricaoFuncionario) throws SQLException {
        Connection conn = dbConnection.beginTransaction();
        try {
            InventarioEquipamentos anterior = invRepo.findById(conn, id)
                    .orElseThrow(() -> new IllegalArgumentException("Equipamento não encontrado: " + id));

            invRepo.updateFuncionario(conn, id, novoFuncionarioId);

            InventarioEquipamentos atualizado = invRepo.findById(conn, id)
                    .orElseThrow(() -> new SQLException("Falha ao buscar equipamento reassociado"));

            // Na reassociação, o "anterior" usa o funcionário ANTES da mudança.
            String dadosAnteriores = lerUltimoDadosNovos(
                    conn, anterior.skuProduto(), anterior.funcionarioId(), anterior.numSerie())
                    .orElseGet(() -> jsonEquipamento(anterior));
            String dadosNovos = jsonEquipamento(atualizado);
            String descricao = descricaoFuncionario != null ? descricaoFuncionario : "{}";

            HistoricoEventos evento = HistoricoEventos.transferencia(
                    atualizado.skuProduto(), atualizado.numSerie(),
                    funcionarioExecutor,
                    dadosAnteriores, dadosNovos, descricao);
            histRepo.insert(conn, evento);

            dbConnection.commit(conn);

            publicarEventos(atualizado, evento);
            logger.log(System.Logger.Level.INFO, "✅ Reassociação transacional: ID={0}", id);

        } catch (Exception ex) {
            dbConnection.rollback(conn);
            throw ex;
        }
    }

    // ============================================================
    // REGISTRAR VERIFICAÇÃO
    // ============================================================

    public void registrarVerificacao(String id, String funcionarioExecutor,
                                     LocalDate data, String condicao, String observacoes,
                                     String descricaoFuncionario) throws SQLException {
        if (!CondicaoEquipamento.isValido(condicao)) {
            throw new IllegalArgumentException("Condição inválida: " + condicao);
        }

        Connection conn = dbConnection.beginTransaction();
        try {
            InventarioEquipamentos anterior = invRepo.findById(conn, id)
                    .orElseThrow(() -> new IllegalArgumentException("Equipamento não encontrado: " + id));

            LocalDate dataFinal = (data != null) ? data : LocalDate.now();
            String obsCompleta = appendObservacao(anterior.observacoes(), "Verificação: " + observacoes);

            invRepo.updateVerificacao(conn, id, dataFinal, condicao, obsCompleta);

            InventarioEquipamentos atualizado = invRepo.findById(conn, id)
                    .orElseThrow(() -> new SQLException("Falha ao buscar equipamento verificado"));

            String dadosAnteriores = lerUltimoDadosNovos(
                    conn, atualizado.skuProduto(), atualizado.funcionarioId(), atualizado.numSerie())
                    .orElseGet(() -> jsonEquipamento(anterior));
            String dadosNovos = jsonEquipamento(atualizado);
            String descricao = descricaoFuncionario != null ? descricaoFuncionario : "{}";

            HistoricoEventos evento = HistoricoEventos.atualizacao(
                    atualizado.skuProduto(), atualizado.numSerie(),
                    funcionarioExecutor,
                    dadosAnteriores, dadosNovos, descricao);
            histRepo.insert(conn, evento);

            dbConnection.commit(conn);

            publicarEventos(atualizado, evento);
            logger.log(System.Logger.Level.INFO, "✅ Verificação transacional: ID={0}, Data={1}", id, dataFinal);

        } catch (Exception ex) {
            dbConnection.rollback(conn);
            throw ex;
        }
    }

    // ============================================================
    // ATUALIZAR STATUS (evento acompanha o novo status)
    // ============================================================

    public void atualizarStatus(String id, String funcionarioExecutor,
                                String novoStatus, String descricaoFuncionario) throws SQLException {
        if (!StatusEquipamento.isValido(novoStatus)) {
            throw new IllegalArgumentException("Status inválido: " + novoStatus);
        }

        Connection conn = dbConnection.beginTransaction();
        try {
            InventarioEquipamentos anterior = invRepo.findById(conn, id)
                    .orElseThrow(() -> new IllegalArgumentException("Equipamento não encontrado: " + id));

            invRepo.updateStatus(conn, id, novoStatus);

            InventarioEquipamentos atualizado = invRepo.findById(conn, id)
                    .orElseThrow(() -> new SQLException("Falha ao buscar equipamento atualizado"));

            String dadosAnteriores = lerUltimoDadosNovos(
                    conn, atualizado.skuProduto(), atualizado.funcionarioId(), atualizado.numSerie())
                    .orElseGet(() -> jsonEquipamento(anterior));
            String dadosNovos = jsonEquipamento(atualizado);
            String descricao = descricaoFuncionario != null ? descricaoFuncionario : "{}";

            HistoricoEventos evento = historicoParaStatus(
                    novoStatus, atualizado.skuProduto(), atualizado.numSerie(),
                    funcionarioExecutor, dadosAnteriores, dadosNovos, descricao);
            histRepo.insert(conn, evento);

            dbConnection.commit(conn);

            publicarEventos(atualizado, evento);
            logger.log(System.Logger.Level.INFO,
                    "✅ Status transacional: ID={0} → {1}", id, novoStatus);

        } catch (Exception ex) {
            dbConnection.rollback(conn);
            throw ex;
        }
    }

    // ============================================================
    // ATUALIZAR CONDIÇÃO
    // ============================================================

    public void atualizarCondicao(String id, String funcionarioExecutor,
                                  String novaCondicao, String descricaoFuncionario) throws SQLException {
        if (!CondicaoEquipamento.isValido(novaCondicao)) {
            throw new IllegalArgumentException("Condição inválida: " + novaCondicao);
        }

        Connection conn = dbConnection.beginTransaction();
        try {
            InventarioEquipamentos anterior = invRepo.findById(conn, id)
                    .orElseThrow(() -> new IllegalArgumentException("Equipamento não encontrado: " + id));

            invRepo.updateCondicao(conn, id, novaCondicao);

            InventarioEquipamentos atualizado = invRepo.findById(conn, id)
                    .orElseThrow(() -> new SQLException("Falha ao buscar equipamento atualizado"));

            String dadosAnteriores = lerUltimoDadosNovos(
                    conn, atualizado.skuProduto(), atualizado.funcionarioId(), atualizado.numSerie())
                    .orElseGet(() -> jsonEquipamento(anterior));
            String dadosNovos = jsonEquipamento(atualizado);
            String descricao = descricaoFuncionario != null ? descricaoFuncionario : "{}";

            HistoricoEventos evento = HistoricoEventos.atualizacao(
                    atualizado.skuProduto(), atualizado.numSerie(),
                    funcionarioExecutor,
                    dadosAnteriores, dadosNovos, descricao);
            histRepo.insert(conn, evento);

            dbConnection.commit(conn);

            publicarEventos(atualizado, evento);
            logger.log(System.Logger.Level.INFO,
                    "✅ Condição transacional: ID={0} → {1}", id, novaCondicao);

        } catch (Exception ex) {
            dbConnection.rollback(conn);
            throw ex;
        }
    }

    // ============================================================
    // FATURA / DOCUMENTOS / DEVOLUÇÃO
    // ============================================================

    public void atualizarFatura(String id, String funcionarioExecutor,
                                String numeroFatura, String descricaoFuncionario) throws SQLException {
        Connection conn = dbConnection.beginTransaction();
        try {
            InventarioEquipamentos anterior = invRepo.findById(conn, id)
                    .orElseThrow(() -> new IllegalArgumentException("Equipamento não encontrado: " + id));

            invRepo.updateNumeroFatura(conn, id, numeroFatura);

            InventarioEquipamentos atualizado = invRepo.findById(conn, id)
                    .orElseThrow(() -> new SQLException("Falha ao buscar equipamento atualizado"));

            gravarAtualizacao(conn, anterior, atualizado, funcionarioExecutor, descricaoFuncionario);
            dbConnection.commit(conn);

            eventBus.publish(new InventarioEvent<>(atualizado, "FATURA_ATUALIZADA"));
            logger.log(System.Logger.Level.INFO, "✅ Fatura transacional: ID={0}", id);

        } catch (Exception ex) {
            dbConnection.rollback(conn);
            throw ex;
        }
    }

    public void atualizarDocumentoEntrega(String id, String funcionarioExecutor,
                                          byte[] documento, String descricaoFuncionario) throws SQLException {
        Connection conn = dbConnection.beginTransaction();
        try {
            InventarioEquipamentos anterior = invRepo.findById(conn, id)
                    .orElseThrow(() -> new IllegalArgumentException("Equipamento não encontrado: " + id));

            invRepo.updateDocumentoEntrega(conn, id, documento);

            InventarioEquipamentos atualizado = invRepo.findById(conn, id)
                    .orElseThrow(() -> new SQLException("Falha ao buscar equipamento atualizado"));

            gravarAtualizacao(conn, anterior, atualizado, funcionarioExecutor, descricaoFuncionario);
            dbConnection.commit(conn);

            eventBus.publish(new InventarioEvent<>(atualizado, "DOCUMENTO_ENTREGA_ATUALIZADO"));
            logger.log(System.Logger.Level.INFO, "✅ Doc. entrega transacional: ID={0}", id);

        } catch (Exception ex) {
            dbConnection.rollback(conn);
            throw ex;
        }
    }

    public void atualizarDocumentoDevolucao(String id, String funcionarioExecutor,
                                            byte[] documento, String descricaoFuncionario) throws SQLException {
        Connection conn = dbConnection.beginTransaction();
        try {
            InventarioEquipamentos anterior = invRepo.findById(conn, id)
                    .orElseThrow(() -> new IllegalArgumentException("Equipamento não encontrado: " + id));

            invRepo.updateDocumentoDevolucao(conn, id, documento);

            InventarioEquipamentos atualizado = invRepo.findById(conn, id)
                    .orElseThrow(() -> new SQLException("Falha ao buscar equipamento atualizado"));

            gravarAtualizacao(conn, anterior, atualizado, funcionarioExecutor, descricaoFuncionario);
            dbConnection.commit(conn);

            eventBus.publish(new InventarioEvent<>(atualizado, "DOCUMENTO_DEVOLUCAO_ATUALIZADO"));
            logger.log(System.Logger.Level.INFO, "✅ Doc. devolução transacional: ID={0}", id);

        } catch (Exception ex) {
            dbConnection.rollback(conn);
            throw ex;
        }
    }

    public void atualizarDevolucao(String id, String funcionarioExecutor,
                                   boolean devolucao, String descricaoFuncionario) throws SQLException {
        Connection conn = dbConnection.beginTransaction();
        try {
            InventarioEquipamentos anterior = invRepo.findById(conn, id)
                    .orElseThrow(() -> new IllegalArgumentException("Equipamento não encontrado: " + id));

            invRepo.updateDevolucao(conn, id, devolucao);

            InventarioEquipamentos atualizado = invRepo.findById(conn, id)
                    .orElseThrow(() -> new SQLException("Falha ao buscar equipamento atualizado"));

            String dadosAnteriores = lerUltimoDadosNovos(
                    conn, atualizado.skuProduto(), atualizado.funcionarioId(), atualizado.numSerie())
                    .orElseGet(() -> jsonEquipamento(anterior));
            String descricao = descricaoFuncionario != null ? descricaoFuncionario : "{}";

            HistoricoEventos evento = HistoricoEventos.devolucao(
                    atualizado.skuProduto(), atualizado.numSerie(),
                    funcionarioExecutor, dadosAnteriores, descricao);
            histRepo.insert(conn, evento);

            dbConnection.commit(conn);

            publicarEventos(atualizado, evento);
            logger.log(System.Logger.Level.INFO, "✅ Devolução transacional: ID={0} → {1}", id, devolucao);

        } catch (Exception ex) {
            dbConnection.rollback(conn);
            throw ex;
        }
    }

    // ============================================================
    // HELPERS PRIVADOS
    // ============================================================

    /**
     * Lê o último dados_novos do EQUIPAMENTO (chave rígida:
     * sku_produto + funcionario_id + num_serie). Fallback: Optional.empty()
     * quando é o primeiro evento do equipamento.
     */
    private Optional<String> lerUltimoDadosNovos(Connection conn,
                                                 String skuProduto,
                                                 String funcionarioId,
                                                 String numSerie) throws SQLException {
        return histRepo.ultimoDadosNovos(conn, skuProduto, funcionarioId, numSerie);
    }

    /** Grava o histórico de atualização (dados anteriores + novos) na conn corrente. */
    private void gravarAtualizacao(Connection conn, InventarioEquipamentos anterior,
                                   InventarioEquipamentos atualizado,
                                   String funcionarioExecutor,
                                   String descricaoFuncionario) throws SQLException {
        String dadosAnteriores = lerUltimoDadosNovos(
                conn, atualizado.skuProduto(), atualizado.funcionarioId(), atualizado.numSerie())
                .orElseGet(() -> jsonEquipamento(anterior));
        String dadosNovos = jsonEquipamento(atualizado);
        String descricao = descricaoFuncionario != null ? descricaoFuncionario : "{}";

        HistoricoEventos evento = HistoricoEventos.atualizacao(
                atualizado.skuProduto(), atualizado.numSerie(),
                funcionarioExecutor, dadosAnteriores, dadosNovos, descricao);
        histRepo.insert(conn, evento);
    }

    /** Escolhe a fábrica de histórico correspondente ao novo status. */
    private HistoricoEventos historicoParaStatus(String novoStatus, String sku, String numSerie,
                                                 String func, String anteriores,
                                                 String novos, String descricao) {
        return switch (StatusEquipamento.de(novoStatus)) {
            case MANUTENCAO -> HistoricoEventos.manutencao(sku, numSerie, func, novos, descricao);
            case BAIXADO    -> HistoricoEventos.baixa(sku, numSerie, func, anteriores, descricao);
            default         -> HistoricoEventos.atualizacao(sku, numSerie, func, anteriores, novos, descricao);
        };
    }

    /** Publica os dois eventos no EventBus (fora da transação). */
    private void publicarEventos(InventarioEquipamentos equip, HistoricoEventos evento) {
        eventBus.publish(new InventarioEvent<>(equip, "ATUALIZADO"));
        eventBus.publish(new HistoricoEvent<>(evento, evento.tipoEvento()));
    }

    /** Serializa o equipamento como Map → JSON via utilitário do Histórico. */
    private String jsonEquipamento(InventarioEquipamentos e) {
        if (e == null) return "{}";
        Map<String, Object> dados = new LinkedHashMap<>();
        dados.put("id", e.id());
        dados.put("skuProduto", e.skuProduto());
        dados.put("funcionarioId", e.funcionarioId());
        dados.put("numSerie", e.numSerie());
        dados.put("enderecoMac", e.enderecoMac());
        dados.put("dataAquisicao", e.dataAquisicao() != null ? e.dataAquisicao().toString() : "");
        dados.put("dataInstalacao", e.dataInstalacao() != null ? e.dataInstalacao().toString() : "");
        dados.put("dataUltimaVerificacao", e.dataUltimaVerificacao() != null ? e.dataUltimaVerificacao().toString() : "");
        dados.put("numeroFatura", e.numeroFatura());
        dados.put("localizacao", e.localizacao());
        dados.put("departamento", e.departamento());
        dados.put("status", e.status());
        dados.put("condicao", e.condicao());
        dados.put("observacoes", e.observacoes());
        dados.put("devolucao", e.devolucao() != null ? e.devolucao() : false);
        dados.put("createdAt", e.createdAt() != null ? e.createdAt().toString() : "");
        dados.put("updatedAt", e.updatedAt() != null ? e.updatedAt().toString() : "");
        return histService.criarJsonGenerico(dados);
    }

    /** Regras de validação do equipamento (movidas do service de leitura). */
    private void validarEquipamento(InventarioEquipamentos e) {
        if (e.skuProduto() == null || e.skuProduto().isBlank()) {
            throw new IllegalArgumentException("SKU do produto é obrigatório");
        }
        if (e.funcionarioId() == null || e.funcionarioId().isBlank()) {
            throw new IllegalArgumentException("ID do funcionário é obrigatório");
        }
        if (e.numSerie() == null || e.numSerie().isBlank()) {
            throw new IllegalArgumentException("Número de série é obrigatório");
        }
        // MAC opcional — monitores, periféricos e cabos não têm endereço MAC.
        if (e.localizacao() == null || e.localizacao().isBlank()) {
            throw new IllegalArgumentException("Localização é obrigatória");
        }
        if (e.dataAquisicao() == null) {
            throw new IllegalArgumentException("Data de aquisição é obrigatória");
        }
        if (e.dataAquisicao().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Data de aquisição não pode ser futura");
        }
        if (e.dataInstalacao() != null && e.dataInstalacao().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Data de instalação não pode ser futura");
        }
        if (e.dataUltimaVerificacao() != null && e.dataUltimaVerificacao().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Data da última verificação não pode ser futura");
        }
    }

    private String appendObservacao(String atual, String novo) {
        if (atual == null || atual.isBlank()) return novo;
        return atual + "\n" + novo;
    }

    private InventarioEquipamentos comObservacoes(InventarioEquipamentos e, String novasObservacoes) {
        return new InventarioEquipamentos(
                e.id(), e.skuProduto(), e.funcionarioId(), e.numSerie(), e.enderecoMac(),
                e.dataAquisicao(), e.dataInstalacao(), e.dataUltimaVerificacao(),
                e.numeroFatura(), e.localizacao(), e.departamento(), e.status(), e.condicao(),
                e.documentoEntrega(), e.documentoDevolucao(), novasObservacoes, e.devolucao(),
                e.createdAt(), LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
                e.deviceId(), e.deletado()
        );
    }
}