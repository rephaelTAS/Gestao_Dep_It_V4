package com.ossobo.gestaoDepIt.db.services;

import com.ossobo.gestaoDepIt.db.enums.TipoEvento;
import com.ossobo.gestaoDepIt.db.models.HistoricoEventos;
import com.ossobo.gestaoDepIt.db.repositories.HistoricoEventosRepository;
import com.ossobo.winterfx.anotations.Inject;
import com.ossobo.winterfx.anotations.Service;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * HistoricoEventosService v3.0
 *
 * Responsabilidade: Regras de negócio de LEITURA do histórico de eventos,
 *                   utilitários JSON (composição de payload) e domínios.
 *
 * v3.0 — Separação estrutural leitura/mutação (ratificada):
 *        - TODAS as mutações saíram deste service; agora vivem em
 *          {@code InventarioCrudService}, que orquestra a transação
 *          inventário + histórico atomicamente.
 *        - Este service expõe APENAS consultas, utilitários JSON e
 *          {@code getTiposPermitidos()}.
 *        - Utilitários JSON continuam aqui — são a fonte única do formato
 *          (o InventarioCrudService os usa para montar dadosAnteriores/Novos).
 *        - getTiposPermitidos() delega a TipoEvento.todos() — fonte única.
 *
 * @since v2.1
 */
@Service
public class HistoricoEventosService {

    @Inject
    private HistoricoEventosRepository repository;

    // ============================================================
    // LISTAGENS / BUSCAS
    // ============================================================

    public List<HistoricoEventos> listarTodos() throws SQLException {
        return repository.findAll();
    }

    public List<HistoricoEventos> listarTodos(int pagina, int tamanho) throws SQLException {
        int offset = (pagina - 1) * tamanho;
        return repository.findAll(tamanho, offset);
    }

    public Optional<HistoricoEventos> buscarPorId(String id) throws SQLException {
        return repository.findById(id);
    }

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

    public List<HistoricoEventos> buscarComFiltros(String sku, String funcionarioId, String tipo,
                                                   LocalDateTime inicio, LocalDateTime fim) throws SQLException {
        return repository.findWithFilters(sku, funcionarioId, tipo, inicio, fim);
    }

    public List<HistoricoEventos> buscarAlteracoesProduto(String sku) throws SQLException {
        return repository.findAlteracoesProduto(sku);
    }

    public List<HistoricoEventos> buscarEventosAutenticacao(LocalDateTime inicio, LocalDateTime fim) throws SQLException {
        return repository.findEventosAutenticacao(inicio, fim);
    }


    // ============================================================
    // ESTATÍSTICAS
    // ============================================================

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

    // ============================================================
    // VERIFICAÇÕES
    // ============================================================

    public boolean existePorId(String id) throws SQLException {
        return repository.existsById(id);
    }

    /** Lista de tipos permitidos — fonte única: enum TipoEvento. */
    public List<String> getTiposPermitidos() {
        return TipoEvento.todos();
    }

    // ============================================================
    // UTILITÁRIOS JSON (fonte única do formato)
    // ============================================================

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
                escapeJson(categoria), quantidade, escapeJson(localizacao));
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
}