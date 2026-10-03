package com.ossobo.gestaoDepIt.db.repositories;

import com.ossobo.gestaoDepIt.db.config.DatabaseConnection;
import com.ossobo.gestaoDepIt.db.models.HistoricoEventos;
import com.ossobo.gestaoDepIt.db.sync.DeviceIdentity;
import com.ossobo.winterfx.anotations.Inject;
import com.ossobo.winterfx.anotations.Repository;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * HistoricoEventosRepository v3.1
 *
 * Responsabilidade: Acesso a dados de historico_eventos (model v3.3).
 *
 * v3.1 — num_serie passa a fazer parte da chave de correlação:
 *        - SELECT/INSERT/mapper incluem num_serie.
 *        - ultimoDadosNovos(conn, sku, funcionarioId, numSerie) com chave RÍGIDA
 *          nas 3 colunas — evita misturar históricos de equipamentos
 *          diferentes que compartilham o mesmo SKU.
 *        - índice idx_historico_chave (sku_produto, funcionario_id, num_serie).
 *        - getLocalDateTime tolerante (ISO + SQLite default).
 *
 * v3.0 — Separação estrutural leitura/mutação (Connection obrigatória em mutações).
 */
@Repository
public class HistoricoEventosRepository {

    private static final System.Logger logger =
            System.getLogger(HistoricoEventosRepository.class.getName());

    private static final DateTimeFormatter SQLITE_TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss[.SSS]");

    @Inject
    private DatabaseConnection dbConnection;

    @Inject
    private DeviceIdentity deviceIdentity;

    private static final String TABLE = "historico_eventos";

    // ===== SQL =====

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
            SELECT * FROM %s WHERE id = ?
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_SKU = """
            SELECT * FROM %s WHERE sku_produto = ?
            ORDER BY created_at DESC
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_NUM_SERIE = """
            SELECT * FROM %s WHERE num_serie = ?
            ORDER BY created_at DESC
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_FUNCIONARIO = """
            SELECT * FROM %s WHERE funcionario_id = ?
            ORDER BY created_at DESC
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_TIPO = """
            SELECT * FROM %s WHERE tipo_evento = ?
            ORDER BY created_at DESC
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_PERIODO = """
            SELECT * FROM %s WHERE created_at BETWEEN ? AND ?
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
                id, tipo_evento, sku_produto, num_serie, funcionario_id,
                descricao_funcionario, dados_anteriores, dados_novos,
                created_at, device_id
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.formatted(TABLE);

    private static final String SQL_DELETE_OLD = """
            DELETE FROM %s WHERE created_at < ?
            """.formatted(TABLE);

    private static final String SQL_DELETE_BY_SKU = """
            DELETE FROM %s WHERE sku_produto = ?
            """.formatted(TABLE);

    // Chave RÍGIDA: as 3 colunas juntas identificam o equipamento físico.
    private static final String SQL_ULTIMO_DADOS_NOVOS = """
            SELECT dados_novos FROM %s
            WHERE sku_produto = ? AND funcionario_id = ? AND num_serie = ?
            ORDER BY created_at DESC
            LIMIT 1
            """.formatted(TABLE);

    // ============================================================
    // LEITURAS PURAS (abrem conexão própria)
    // ============================================================

    public List<HistoricoEventos> findAll() throws SQLException {
        try (Connection conn = dbConnection.getConnection()) {
            return executeQuery(conn, SQL_FIND_ALL);
        }
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

    public Optional<HistoricoEventos> findById(String id) throws SQLException {
        if (id == null || id.isBlank()) return Optional.empty();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_ID)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapResultSet(rs)) : Optional.empty();
            }
        }
    }

    public List<HistoricoEventos> findBySkuProduto(String sku) throws SQLException {
        if (sku == null || sku.isBlank()) return List.of();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_SKU)) {
            stmt.setString(1, sku);
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    public List<HistoricoEventos> findByNumSerie(String numSerie) throws SQLException {
        if (numSerie == null || numSerie.isBlank()) return List.of();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_NUM_SERIE)) {
            stmt.setString(1, numSerie);
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    public List<HistoricoEventos> findByFuncionarioId(String funcionarioId) throws SQLException {
        if (funcionarioId == null || funcionarioId.isBlank()) return List.of();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_FUNCIONARIO)) {
            stmt.setString(1, funcionarioId);
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    public List<HistoricoEventos> findByTipoEvento(String tipo) throws SQLException {
        if (tipo == null || tipo.isBlank()) return List.of();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_TIPO)) {
            stmt.setString(1, tipo);
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    public List<HistoricoEventos> findByPeriodo(LocalDateTime inicio, LocalDateTime fim) throws SQLException {
        if (inicio == null || fim == null) return List.of();
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
        if (sku == null || sku.isBlank()) return List.of();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_ALTERACOES_PRODUTO)) {
            stmt.setString(1, sku);
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    public List<HistoricoEventos> findEventosAutenticacao(LocalDateTime inicio, LocalDateTime fim) throws SQLException {
        if (inicio == null || fim == null) return List.of();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_EVENTOS_AUTENTICACAO)) {
            stmt.setTimestamp(1, Timestamp.valueOf(inicio));
            stmt.setTimestamp(2, Timestamp.valueOf(fim));
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    public List<HistoricoEventos> findWithFilters(
            String skuProduto, String funcionarioId, String tipoEvento,
            LocalDateTime dataInicio, LocalDateTime dataFim
    ) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM " + TABLE + " WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (skuProduto != null && !skuProduto.isBlank()) { sql.append(" AND sku_produto = ?"); params.add(skuProduto); }
        if (funcionarioId != null && !funcionarioId.isBlank()) { sql.append(" AND funcionario_id = ?"); params.add(funcionarioId); }
        if (tipoEvento != null && !tipoEvento.isBlank()) { sql.append(" AND tipo_evento = ?"); params.add(tipoEvento); }
        if (dataInicio != null && dataFim != null) {
            sql.append(" AND created_at BETWEEN ? AND ?");
            params.add(Timestamp.valueOf(dataInicio));
            params.add(Timestamp.valueOf(dataFim));
        }
        sql.append(" ORDER BY created_at DESC");

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) stmt.setObject(i + 1, params.get(i));
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    // ============================================================
    // REGRA DO LEDGER — dados_novos mais recente por equipamento
    // ============================================================

    /**
     * Retorna o dados_novos do evento MAIS RECENTE do equipamento.
     * Chave RÍGIDA: (sku_produto, funcionario_id, num_serie).
     *
     * Usado para preencher dados_anteriores do próximo evento — regra do ledger.
     * A chave tripla identifica o equipamento físico sem misturar históricos
     * de equipamentos diferentes que compartilham o mesmo SKU.
     *
     * @return Optional.empty() se é o primeiro evento do equipamento.
     */
    public Optional<String> ultimoDadosNovos(Connection conn,
                                             String skuProduto,
                                             String funcionarioId,
                                             String numSerie) throws SQLException {
        if (skuProduto == null || skuProduto.isBlank()
                || funcionarioId == null || funcionarioId.isBlank()) {
            return Optional.empty();
        }
        String serie = numSerie != null ? numSerie : "";

        try (PreparedStatement stmt = conn.prepareStatement(SQL_ULTIMO_DADOS_NOVOS)) {
            stmt.setString(1, skuProduto);
            stmt.setString(2, funcionarioId);
            stmt.setString(3, serie);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.ofNullable(rs.getString(1)) : Optional.empty();
            }
        }
    }

    /** Variante sem Connection — leitura pura isolada. */
    public Optional<String> ultimoDadosNovos(String skuProduto,
                                             String funcionarioId,
                                             String numSerie) throws SQLException {
        try (Connection conn = dbConnection.getConnection()) {
            return ultimoDadosNovos(conn, skuProduto, funcionarioId, numSerie);
        }
    }

    // ============================================================
    // MUTAÇÕES — Connection obrigatória (chamador fecha)
    // ============================================================

    public String insert(Connection conn, HistoricoEventos evento) throws SQLException {
        if (evento == null) throw new IllegalArgumentException("Evento inválido");
        if (conn == null) throw new IllegalArgumentException("Connection é obrigatória");

        String id = (evento.id() == null || evento.id().isBlank())
                ? UUID.randomUUID().toString()
                : evento.id();
        String agora = (evento.createdAt() != null)
                ? evento.createdAt().truncatedTo(ChronoUnit.MILLIS).toString()
                : LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS).toString();
        String deviceId = (evento.deviceId() != null && !evento.deviceId().isBlank())
                ? evento.deviceId()
                : deviceIdentity.getDeviceId();

        try (PreparedStatement stmt = conn.prepareStatement(SQL_INSERT)) {
            stmt.setString(1, id);
            stmt.setString(2, evento.tipoEvento());
            stmt.setString(3, evento.skuProduto());
            stmt.setString(4, evento.numSerie() != null ? evento.numSerie() : "");
            stmt.setString(5, evento.funcionarioId());
            stmt.setString(6, evento.descricaoFuncionario());
            stmt.setString(7, evento.dadosAnteriores());
            stmt.setString(8, evento.dadosNovos());
            stmt.setString(9, agora);
            stmt.setString(10, deviceId);
            stmt.executeUpdate();
            return id;
        }
    }

    /** Alias semântico — participa de transação externa. */
    public String inserirNaTransacao(Connection conn, HistoricoEventos evento) throws SQLException {
        return insert(conn, evento);
    }

    public int deleteOldEvents(Connection conn, LocalDateTime dataLimite) throws SQLException {
        if (dataLimite == null) return 0;
        if (conn == null) throw new IllegalArgumentException("Connection é obrigatória");
        try (PreparedStatement stmt = conn.prepareStatement(SQL_DELETE_OLD)) {
            stmt.setTimestamp(1, Timestamp.valueOf(dataLimite));
            return stmt.executeUpdate();
        }
    }

    public int deleteBySkuProduto(Connection conn, String sku) throws SQLException {
        if (sku == null || sku.isBlank()) return 0;
        if (conn == null) throw new IllegalArgumentException("Connection é obrigatória");
        try (PreparedStatement stmt = conn.prepareStatement(SQL_DELETE_BY_SKU)) {
            stmt.setString(1, sku);
            return stmt.executeUpdate();
        }
    }

    // ============================================================
    // ESTATÍSTICAS
    // ============================================================

    public int countAll() throws SQLException {
        try (Connection conn = dbConnection.getConnection()) {
            return count(conn, "SELECT COUNT(*) FROM " + TABLE);
        }
    }

    public int countByPeriodo(LocalDateTime inicio, LocalDateTime fim) throws SQLException {
        if (inicio == null || fim == null) return 0;
        try (Connection conn = dbConnection.getConnection()) {
            return count(conn, "SELECT COUNT(*) FROM " + TABLE + " WHERE created_at BETWEEN ? AND ?",
                    Timestamp.valueOf(inicio), Timestamp.valueOf(fim));
        }
    }

    public Map<String, Integer> countByTipo(LocalDateTime inicio, LocalDateTime fim) throws SQLException {
        String sql = """
                SELECT tipo_evento, COUNT(*) as total FROM %s
                WHERE created_at BETWEEN ? AND ?
                GROUP BY tipo_evento ORDER BY total DESC
                """.formatted(TABLE);
        try (Connection conn = dbConnection.getConnection()) {
            return countGroupBy(conn, sql, Timestamp.valueOf(inicio), Timestamp.valueOf(fim));
        }
    }

    public Map<String, Integer> countBySku(LocalDateTime inicio, LocalDateTime fim) throws SQLException {
        String sql = """
                SELECT sku_produto, COUNT(*) as total FROM %s
                WHERE created_at BETWEEN ? AND ?
                GROUP BY sku_produto ORDER BY total DESC LIMIT 20
                """.formatted(TABLE);
        try (Connection conn = dbConnection.getConnection()) {
            return countGroupBy(conn, sql, Timestamp.valueOf(inicio), Timestamp.valueOf(fim));
        }
    }

    public Map<String, Integer> countByFuncionario(LocalDateTime inicio, LocalDateTime fim) throws SQLException {
        String sql = """
                SELECT funcionario_id, COUNT(*) as total FROM %s
                WHERE created_at BETWEEN ? AND ?
                GROUP BY funcionario_id ORDER BY total DESC LIMIT 20
                """.formatted(TABLE);
        try (Connection conn = dbConnection.getConnection()) {
            return countGroupBy(conn, sql, Timestamp.valueOf(inicio), Timestamp.valueOf(fim));
        }
    }

    public Map<Integer, Integer> getAtividadePorHora(LocalDateTime inicio, LocalDateTime fim) throws SQLException {
        String sql = """
                SELECT CAST(strftime('%%H', created_at) AS INTEGER) as hora, COUNT(*) as total
                FROM %s
                WHERE created_at BETWEEN ? AND ?
                GROUP BY hora ORDER BY hora
                """.formatted(TABLE);

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, inicio.toString());
            stmt.setString(2, fim.toString());

            Map<Integer, Integer> atividade = new TreeMap<>();
            for (int hora = 0; hora < 24; hora++) atividade.put(hora, 0);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) atividade.put(rs.getInt("hora"), rs.getInt("total"));
            }
            return Map.copyOf(atividade);
        }
    }

    public boolean existsById(String id) throws SQLException {
        if (id == null || id.isBlank()) return false;
        String sql = "SELECT COUNT(*) FROM %s WHERE id = ?".formatted(TABLE);
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    // ============================================================
    // AUXILIARES
    // ============================================================

    private int count(Connection conn, String sql, Object... params) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) stmt.setObject(i + 1, params[i]);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private Map<String, Integer> countGroupBy(Connection conn, String sql, Object... params) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) stmt.setObject(i + 1, params[i]);
            Map<String, Integer> resultado = new LinkedHashMap<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) resultado.put(rs.getString(1), rs.getInt(2));
            }
            return Map.copyOf(resultado);
        }
    }

    private List<HistoricoEventos> executeQuery(Connection conn, String sql) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            return mapResultSetList(rs);
        }
    }

    private List<HistoricoEventos> mapResultSetList(ResultSet rs) throws SQLException {
        List<HistoricoEventos> list = new ArrayList<>();
        while (rs.next()) list.add(mapResultSet(rs));
        return List.copyOf(list);
    }

    private HistoricoEventos mapResultSet(ResultSet rs) throws SQLException {
        return new HistoricoEventos(
                rs.getString("id"),
                rs.getString("tipo_evento"),
                rs.getString("sku_produto"),
                rs.getString("num_serie"),
                rs.getString("funcionario_id"),
                rs.getString("descricao_funcionario"),
                rs.getString("dados_anteriores"),
                rs.getString("dados_novos"),
                getLocalDateTime(rs, "created_at"),
                rs.getString("device_id")
        );
    }

    /**
     * Lê TIMESTAMP de auditoria tolerando dois formatos coexistentes:
     *   - ISO-8601 com T  →  "2026-09-18T15:30:43.123"  (app)
     *   - SQLite default  →  "2026-09-18 15:30:43"      (seed/SQL antigo)
     */
    private LocalDateTime getLocalDateTime(ResultSet rs, String column) throws SQLException {
        String raw = rs.getString(column);
        if (raw == null || raw.isBlank()) return null;
        try {
            if (raw.indexOf('T') >= 0) return LocalDateTime.parse(raw);
            return LocalDateTime.parse(raw, SQLITE_TIMESTAMP);
        } catch (DateTimeParseException e) {
            logger.log(System.Logger.Level.WARNING,
                    "Timestamp inválido em ''{0}'': ''{1}'' — ignorado.", column, raw);
            return null;
        }
    }
}