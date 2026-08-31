package com.ossobo.gestaoDepIt.db.repositories;

import com.ossobo.gestaoDepIt.db.config.DatabaseConnection;
import com.ossobo.gestaoDepIt.db.models.HistoricoEventos;


import com.ossobo.winterfx.anotations.Inject;
import com.ossobo.winterfx.anotations.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * HistoricoEventosRepository - Acesso a dados com WinterFX
 * v2.0 - Migrado para Java 17+ com Records e Text Blocks
 *
 * Responsabilidades:
 * - CRUD para HistoricoEventos
 * - Filtros por SKU, funcionário, tipo, período
 * - Estatísticas e relatórios
 */
@Repository
public class HistoricoEventosRepository {

    private static final Logger logger = LoggerFactory.getLogger(HistoricoEventosRepository.class);

    @Inject
    private DatabaseConnection dbConnection;

    private static final String TABLE = "historico_eventos";

    // ===== SQL COM TEXT BLOCKS =====

    private static final String SQL_FIND_ALL = """
            SELECT * FROM %s
            ORDER BY created_at DESC
            """.formatted(TABLE);

    private static final String SQL_FIND_ALL_PAGINATED = """
            SELECT * FROM %s
            ORDER BY created_at DESC
            LIMIT ? OFFSET ?
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_ID = """
            SELECT * FROM %s
            WHERE id = ?
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_SKU = """
            SELECT * FROM %s
            WHERE sku_produto = ?
            ORDER BY created_at DESC
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_FUNCIONARIO = """
            SELECT * FROM %s
            WHERE funcionario_id = ?
            ORDER BY created_at DESC
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_TIPO = """
            SELECT * FROM %s
            WHERE tipo_evento = ?
            ORDER BY created_at DESC
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_PERIODO = """
            SELECT * FROM %s
            WHERE created_at BETWEEN ? AND ?
            ORDER BY created_at DESC
            """.formatted(TABLE);

    private static final String SQL_FIND_ALTERACOES_PRODUTO = """
            SELECT * FROM %s
            WHERE sku_produto = ?
            AND tipo_evento IN ('CRIACAO', 'ATUALIZACAO', 'BAIXA', 'EXCLUSAO')
            ORDER BY created_at DESC
            """.formatted(TABLE);

    private static final String SQL_FIND_EVENTOS_AUTENTICACAO = """
            SELECT * FROM %s
            WHERE tipo_evento IN ('LOGIN', 'LOGOUT')
            AND created_at BETWEEN ? AND ?
            ORDER BY created_at DESC
            """.formatted(TABLE);

    private static final String SQL_INSERT = """
            INSERT INTO %s (
                tipo_evento, sku_produto, funcionario_id,
                descricao_funcionario, dados_anteriores, dados_novos
            ) VALUES (?, ?, ?, ?, ?, ?)
            """.formatted(TABLE);

    private static final String SQL_DELETE_OLD = """
            DELETE FROM %s
            WHERE created_at < ?
            """.formatted(TABLE);

    private static final String SQL_DELETE_BY_SKU = """
            DELETE FROM %s
            WHERE sku_produto = ?
            """.formatted(TABLE);

    // ===== CRUD =====

    public List<HistoricoEventos> findAll() throws SQLException {
        return executeQuery(SQL_FIND_ALL);
    }

    public List<HistoricoEventos> findAll(int limit, int offset) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_ALL_PAGINATED)) {

            stmt.setInt(1, limit);
            stmt.setInt(2, offset);
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    public Optional<HistoricoEventos> findById(Long id) throws SQLException {
        if (id == null || id <= 0) {
            return Optional.empty();
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_ID)) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapResultSet(rs)) : Optional.empty();
            }
        }
    }

    public List<HistoricoEventos> findBySkuProduto(String sku) throws SQLException {
        if (!(sku instanceof String s) || s.isBlank()) {
            return List.of();
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_SKU)) {

            stmt.setString(1, sku);
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    public List<HistoricoEventos> findByFuncionarioId(String funcionarioId) throws SQLException {
        if (!(funcionarioId instanceof String f) || f.isBlank()) {
            return List.of();
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_FUNCIONARIO)) {

            stmt.setString(1, funcionarioId);
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    public List<HistoricoEventos> findByTipoEvento(String tipo) throws SQLException {
        if (!(tipo instanceof String t) || t.isBlank()) {
            return List.of();
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_TIPO)) {

            stmt.setString(1, tipo);
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    public List<HistoricoEventos> findByPeriodo(LocalDateTime inicio, LocalDateTime fim) throws SQLException {
        if (inicio == null || fim == null) {
            return List.of();
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_PERIODO)) {

            stmt.setTimestamp(1, Timestamp.valueOf(inicio));
            stmt.setTimestamp(2, Timestamp.valueOf(fim));
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    public List<HistoricoEventos> findAlteracoesProduto(String sku) throws SQLException {
        if (!(sku instanceof String s) || s.isBlank()) {
            return List.of();
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_ALTERACOES_PRODUTO)) {

            stmt.setString(1, sku);
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    public List<HistoricoEventos> findEventosAutenticacao(LocalDateTime inicio, LocalDateTime fim) throws SQLException {
        if (inicio == null || fim == null) {
            return List.of();
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_EVENTOS_AUTENTICACAO)) {

            stmt.setTimestamp(1, Timestamp.valueOf(inicio));
            stmt.setTimestamp(2, Timestamp.valueOf(fim));
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    public Long insert(HistoricoEventos evento) throws SQLException {
        if (!(evento instanceof HistoricoEventos e)) {
            throw new IllegalArgumentException("Evento inválido");
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_INSERT,
                     PreparedStatement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, e.tipoEvento());
            stmt.setString(2, e.skuProduto());
            stmt.setString(3, e.funcionarioId());
            stmt.setString(4, e.descricaoFuncionario());
            stmt.setString(5, e.dadosAnteriores());
            stmt.setString(6, e.dadosNovos());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
                throw new SQLException("Falha ao obter ID gerado");
            }
        }
    }

    public int deleteOldEvents(LocalDateTime dataLimite) throws SQLException {
        if (dataLimite == null) {
            return 0;
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_DELETE_OLD)) {

            stmt.setTimestamp(1, Timestamp.valueOf(dataLimite));
            return stmt.executeUpdate();
        }
    }

    public int deleteBySkuProduto(String sku) throws SQLException {
        if (!(sku instanceof String s) || s.isBlank()) {
            return 0;
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_DELETE_BY_SKU)) {

            stmt.setString(1, sku);
            return stmt.executeUpdate();
        }
    }

    // ===== FILTROS COMBINADOS =====

    public List<HistoricoEventos> findWithFilters(
            String skuProduto,
            String funcionarioId,
            String tipoEvento,
            LocalDateTime dataInicio,
            LocalDateTime dataFim
    ) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM " + TABLE + " WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (skuProduto != null && !skuProduto.isBlank()) {
            sql.append(" AND sku_produto = ?");
            params.add(skuProduto);
        }
        if (funcionarioId != null && !funcionarioId.isBlank()) {
            sql.append(" AND funcionario_id = ?");
            params.add(funcionarioId);
        }
        if (tipoEvento != null && !tipoEvento.isBlank()) {
            sql.append(" AND tipo_evento = ?");
            params.add(tipoEvento);
        }
        if (dataInicio != null && dataFim != null) {
            sql.append(" AND created_at BETWEEN ? AND ?");
            params.add(dataInicio);
            params.add(dataFim);
        }

        sql.append(" ORDER BY created_at DESC");

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    // ===== ESTATÍSTICAS =====

    public int countAll() throws SQLException {
        return count("SELECT COUNT(*) FROM " + TABLE);
    }

    public int countByPeriodo(LocalDateTime inicio, LocalDateTime fim) throws SQLException {
        return count("SELECT COUNT(*) FROM " + TABLE + " WHERE created_at BETWEEN ? AND ?", inicio, fim);
    }

    public Map<String, Integer> countByTipo(LocalDateTime inicio, LocalDateTime fim) throws SQLException {
        String sql = """
                SELECT tipo_evento, COUNT(*) as total
                FROM %s
                WHERE created_at BETWEEN ? AND ?
                GROUP BY tipo_evento
                ORDER BY total DESC
                """.formatted(TABLE);

        return countGroupBy(sql, inicio, fim);
    }

    public Map<String, Integer> countBySku(LocalDateTime inicio, LocalDateTime fim) throws SQLException {
        String sql = """
                SELECT sku_produto, COUNT(*) as total
                FROM %s
                WHERE created_at BETWEEN ? AND ?
                GROUP BY sku_produto
                ORDER BY total DESC
                LIMIT 20
                """.formatted(TABLE);

        return countGroupBy(sql, inicio, fim);
    }

    public Map<String, Integer> countByFuncionario(LocalDateTime inicio, LocalDateTime fim) throws SQLException {
        String sql = """
                SELECT funcionario_id, COUNT(*) as total
                FROM %s
                WHERE created_at BETWEEN ? AND ?
                GROUP BY funcionario_id
                ORDER BY total DESC
                LIMIT 20
                """.formatted(TABLE);

        return countGroupBy(sql, inicio, fim);
    }

    public Map<Integer, Integer> getAtividadePorHora(LocalDateTime inicio, LocalDateTime fim) throws SQLException {
        String sql = """
                SELECT HOUR(created_at) as hora, COUNT(*) as total
                FROM %s
                WHERE created_at BETWEEN ? AND ?
                GROUP BY HOUR(created_at)
                ORDER BY hora
                """.formatted(TABLE);

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(inicio));
            stmt.setTimestamp(2, Timestamp.valueOf(fim));

            Map<Integer, Integer> atividade = new TreeMap<>();
            for (int hora = 0; hora < 24; hora++) {
                atividade.put(hora, 0);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    atividade.put(rs.getInt("hora"), rs.getInt("total"));
                }
            }
            return Map.copyOf(atividade);
        }
    }

    public boolean existsById(Long id) throws SQLException {
        if (id == null) return false;
        String sql = "SELECT COUNT(*) FROM %s WHERE id = ?".formatted(TABLE);

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    // ===== MÉTODOS PRIVADOS =====

    private int count(String sql, Object... params) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private Map<String, Integer> countGroupBy(String sql, Object... params) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }

            Map<String, Integer> resultado = new LinkedHashMap<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultado.put(rs.getString(1), rs.getInt(2));
                }
            }
            return Map.copyOf(resultado);
        }
    }

    private List<HistoricoEventos> executeQuery(String sql) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            return mapResultSetList(rs);
        }
    }

    private List<HistoricoEventos> mapResultSetList(ResultSet rs) throws SQLException {
        List<HistoricoEventos> list = new ArrayList<>();
        while (rs.next()) {
            list.add(mapResultSet(rs));
        }
        return List.copyOf(list);
    }

    private HistoricoEventos mapResultSet(ResultSet rs) throws SQLException {
        return new HistoricoEventos(
                rs.getLong("id"),
                rs.getString("tipo_evento"),
                rs.getString("sku_produto"),
                rs.getString("funcionario_id"),
                rs.getString("descricao_funcionario"),
                rs.getString("dados_anteriores"),
                rs.getString("dados_novos"),
                getLocalDateTime(rs, "created_at")
        );
    }

    private LocalDateTime getLocalDateTime(ResultSet rs, String column) throws SQLException {
        Timestamp ts = rs.getTimestamp(column);
        return ts != null ? ts.toLocalDateTime() : null;
    }
}