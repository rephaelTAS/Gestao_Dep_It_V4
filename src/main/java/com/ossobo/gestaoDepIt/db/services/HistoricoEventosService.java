package com.ossobo.gestaoDepIt.db.services;

import com.ossobo.gestaoDepIt.db.config.event.HistoricoEvent;
import com.ossobo.gestaoDepIt.db.models.HistoricoEventos;
import com.ossobo.gestaoDepIt.db.repositories.HistoricoEventosRepository;


import com.ossobo.winterfx.anotations.Inject;
import com.ossobo.winterfx.anotations.Service;
import com.ossobo.winterfx.event.EventBus;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * HistoricoEventosService - Regras de negócio com EventBus
 * v2.0 - Migrado para Java 17+ com WinterFX
 *
 * Responsabilidades:
 * - Gerenciar eventos históricos
 * - Registrar eventos por tipo (CRIACAO, ATUALIZACAO, etc.)
 * - Publicar eventos (@HistoricoEvent)
 * - Consultas especializadas e estatísticas
 */
@Service
public class HistoricoEventosService {

    private static final Logger logger = LoggerFactory.getLogger(HistoricoEventosService.class);

    @Inject
    private HistoricoEventosRepository repository;

    @Inject
    private EventBus eventBus;

    // ===== CRUD =====

    public List<HistoricoEventos> listarTodos() throws SQLException {
        return repository.findAll();
    }

    public List<HistoricoEventos> listarTodos(int pagina, int tamanho) throws SQLException {
        int offset = (pagina - 1) * tamanho;
        return repository.findAll(tamanho, offset);
    }

    public Optional<HistoricoEventos> buscarPorId(Long id) throws SQLException {
        return repository.findById(id);
    }

    public HistoricoEventos registrar(HistoricoEventos evento) throws SQLException {
        validarEvento(evento);

        Long id = repository.insert(evento);
        HistoricoEventos salvo = repository.findById(id)
                .orElseThrow(() -> new SQLException("Falha ao buscar evento registrado"));

        eventBus.publish(new HistoricoEvent<>(salvo, salvo.tipoEvento()));
        logger.info("✅ Evento registrado: ID={}, Tipo={}, SKU={}, Func={}",
                salvo.id(), salvo.tipoEvento(), salvo.skuProduto(), salvo.funcionarioId());

        return salvo;
    }

    // ===== MÉTODOS CONVENIENTES POR TIPO =====

    public HistoricoEventos registrarCriacao(
            String sku,
            String funcionarioId,
            String dadosNovos,
            String descricaoFuncionario
    ) throws SQLException {
        HistoricoEventos evento = HistoricoEventos.criacao(sku, funcionarioId, dadosNovos, descricaoFuncionario);
        return registrar(evento);
    }

    public HistoricoEventos registrarAtualizacao(
            String sku,
            String funcionarioId,
            String dadosAnteriores,
            String dadosNovos,
            String descricaoFuncionario
    ) throws SQLException {
        HistoricoEventos evento = HistoricoEventos.atualizacao(
                sku, funcionarioId, dadosAnteriores, dadosNovos, descricaoFuncionario
        );
        return registrar(evento);
    }

    public HistoricoEventos registrarBaixa(
            String sku,
            String funcionarioId,
            String dadosAnteriores,
            String descricaoFuncionario
    ) throws SQLException {
        HistoricoEventos evento = HistoricoEventos.baixa(sku, funcionarioId, dadosAnteriores, descricaoFuncionario);
        return registrar(evento);
    }

    public HistoricoEventos registrarExclusao(
            String sku,
            String funcionarioId,
            String dadosAnteriores,
            String descricaoFuncionario
    ) throws SQLException {
        HistoricoEventos evento = HistoricoEventos.exclusao(sku, funcionarioId, dadosAnteriores, descricaoFuncionario);
        return registrar(evento);
    }

    public HistoricoEventos registrarManutencao(
            String sku,
            String funcionarioId,
            String dadosManutencao,
            String descricaoFuncionario
    ) throws SQLException {
        HistoricoEventos evento = HistoricoEventos.manutencao(sku, funcionarioId, dadosManutencao, descricaoFuncionario);
        return registrar(evento);
    }

    public HistoricoEventos registrarMovimentacao(
            String sku,
            String funcionarioId,
            String dadosMovimentacao,
            String descricaoFuncionario
    ) throws SQLException {
        HistoricoEventos evento = HistoricoEventos.movimentacao(sku, funcionarioId, dadosMovimentacao, descricaoFuncionario);
        return registrar(evento);
    }

    public HistoricoEventos registrarInstalacao(
            String sku,
            String funcionarioId,
            String dadosInstalacao,
            String descricaoFuncionario
    ) throws SQLException {
        HistoricoEventos evento = HistoricoEventos.instalacao(sku, funcionarioId, dadosInstalacao, descricaoFuncionario);
        return registrar(evento);
    }

    public HistoricoEventos registrarLogin(
            String funcionarioId,
            String dadosLogin,
            String descricaoFuncionario
    ) throws SQLException {
        HistoricoEventos evento = HistoricoEventos.login(funcionarioId, dadosLogin, descricaoFuncionario);
        return registrar(evento);
    }

    public HistoricoEventos registrarLogout(
            String funcionarioId,
            String dadosLogout,
            String descricaoFuncionario
    ) throws SQLException {
        HistoricoEventos evento = HistoricoEventos.logout(funcionarioId, dadosLogout, descricaoFuncionario);
        return registrar(evento);
    }

    // ===== CONSULTAS ESPECIALIZADAS =====

    public List<HistoricoEventos> buscarPorSku(String sku) throws SQLException {
        return repository.findBySkuProduto(sku);
    }

    public List<HistoricoEventos> buscarPorFuncionario(String funcionarioId) throws SQLException {
        return repository.findByFuncionarioId(funcionarioId);
    }

    public List<HistoricoEventos> buscarPorTipo(String tipo) throws SQLException {
        return repository.findByTipoEvento(tipo);
    }

    public List<HistoricoEventos> buscarPorPeriodo(LocalDateTime inicio, LocalDateTime fim) throws SQLException {
        return repository.findByPeriodo(inicio, fim);
    }

    public List<HistoricoEventos> buscarComFiltros(
            String sku,
            String funcionarioId,
            String tipo,
            LocalDateTime inicio,
            LocalDateTime fim
    ) throws SQLException {
        return repository.findWithFilters(sku, funcionarioId, tipo, inicio, fim);
    }

    public List<HistoricoEventos> buscarAlteracoesProduto(String sku) throws SQLException {
        return repository.findAlteracoesProduto(sku);
    }

    public List<HistoricoEventos> buscarEventosAutenticacao(LocalDateTime inicio, LocalDateTime fim) throws SQLException {
        return repository.findEventosAutenticacao(inicio, fim);
    }

    // ===== ESTATÍSTICAS =====

    public Map<String, Integer> obterEstatisticasPorTipo(LocalDateTime inicio, LocalDateTime fim) throws SQLException {
        return repository.countByTipo(inicio, fim);
    }

    public Map<String, Integer> obterEstatisticasPorSku(LocalDateTime inicio, LocalDateTime fim) throws SQLException {
        return repository.countBySku(inicio, fim);
    }

    public Map<String, Integer> obterEstatisticasPorFuncionario(LocalDateTime inicio, LocalDateTime fim) throws SQLException {
        return repository.countByFuncionario(inicio, fim);
    }

    public Map<Integer, Integer> obterAtividadePorHora(LocalDateTime inicio, LocalDateTime fim) throws SQLException {
        return repository.getAtividadePorHora(inicio, fim);
    }

    public int contarTotal() throws SQLException {
        return repository.countAll();
    }

    public int contarPorPeriodo(LocalDateTime inicio, LocalDateTime fim) throws SQLException {
        return repository.countByPeriodo(inicio, fim);
    }

    // ===== VALIDAÇÕES =====

    public boolean existePorId(Long id) throws SQLException {
        return repository.existsById(id);
    }

    public List<String> getTiposPermitidos() {
        return HistoricoEventos.getTiposValidos();
    }

    // ===== UTILITÁRIOS JSON =====

    public String criarJsonFuncionario(String nome, String departamento, String cargo) {
        return String.format("{\"nome\":\"%s\",\"departamento\":\"%s\",\"cargo\":\"%s\"}",
                escapeJson(nome), escapeJson(departamento), escapeJson(cargo));
    }

    public String criarJsonProduto(String sku, String nome, String descricao,
                                   String categoria, int quantidade, String localizacao) {
        return String.format(
                "{\"sku\":\"%s\",\"nome\":\"%s\",\"descricao\":\"%s\"," +
                        "\"categoria\":\"%s\",\"quantidade\":%d,\"localizacao\":\"%s\"}",
                escapeJson(sku), escapeJson(nome), escapeJson(descricao),
                escapeJson(categoria), quantidade, escapeJson(localizacao)
        );
    }

    public String criarJsonGenerico(Map<String, Object> dados) {
        StringBuilder json = new StringBuilder("{");
        boolean primeiro = true;
        for (Map.Entry<String, Object> entry : dados.entrySet()) {
            if (!primeiro) json.append(", ");
            json.append("\"").append(escapeJson(entry.getKey())).append("\": ");
            Object valor = entry.getValue();
            if (valor instanceof Number || valor instanceof Boolean) {
                json.append(valor);
            } else {
                json.append("\"").append(escapeJson(String.valueOf(valor))).append("\"");
            }
            primeiro = false;
        }
        json.append("}");
        return json.toString();
    }

    private String escapeJson(String valor) {
        if (valor == null) return "";
        return valor
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    // ===== MÉTODO PRIVADO =====

    private void validarEvento(HistoricoEventos e) {
        if (!(e.tipoEvento() instanceof String t) || t.isBlank()) {
            throw new IllegalArgumentException("Tipo de evento é obrigatório");
        }
        if (!HistoricoEventos.isTipoValido(t)) {
            throw new IllegalArgumentException("Tipo de evento inválido: " + t +
                    ". Tipos permitidos: " + String.join(", ", HistoricoEventos.getTiposValidos()));
        }
        if (!(e.skuProduto() instanceof String sku) || sku.isBlank()) {
            throw new IllegalArgumentException("SKU do produto é obrigatório");
        }
        if (!(e.funcionarioId() instanceof String func) || func.isBlank()) {
            throw new IllegalArgumentException("ID do funcionário é obrigatório");
        }
        if (!(e.dadosNovos() instanceof String dados) || dados.isBlank()) {
            throw new IllegalArgumentException("Dados novos são obrigatórios");
        }
    }

    // ===== MANUTENÇÃO =====

    public int limparEventosAntigos(LocalDateTime dataLimite) throws SQLException {
        logger.info("Limpando eventos anteriores a: {}", dataLimite);
        int removidos = repository.deleteOldEvents(dataLimite);
        logger.info("✅ {} eventos removidos", removidos);
        return removidos;
    }

    public int limparEventosPorSku(String sku) throws SQLException {
        logger.info("Limpando eventos do SKU: {}", sku);
        int removidos = repository.deleteBySkuProduto(sku);
        logger.info("✅ {} eventos removidos para SKU: {}", removidos, sku);
        return removidos;
    }
}