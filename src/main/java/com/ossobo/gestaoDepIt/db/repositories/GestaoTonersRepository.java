package com.ossobo.gestaoDepIt.db.repositories;

import com.ossobo.gestaoDepIt.db.config.DatabaseConnection;
import com.ossobo.gestaoDepIt.db.models.GestaoToners;


import com.ossobo.winterfx.anotations.Inject;
import com.ossobo.winterfx.anotations.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * GestaoTonersRepository - Acesso a dados com WinterFX
 * v2.0 - Migrado para Java 17+ com Records e Text Blocks
 *
 * Responsabilidades:
 * - CRUD para GestaoToners
 * - Filtros por inventário, SKU, percentagem, período
 * - Cálculo de vida útil e estatísticas
 */
@Repository
public class GestaoTonersRepository {

    private static final Logger logger = LoggerFactory.getLogger(GestaoTonersRepository.class);

    @Inject
    private DatabaseConnection dbConnection;

    private static final String TABLE = "gestao_toners";

    // ===== SQL COM TEXT BLOCKS =====

    private static final String SQL_FIND_ALL = """
            SELECT * FROM %s
            ORDER BY data_instalacao DESC, created_at DESC
            """.formatted(TABLE);

    private static final String SQL_FIND_ALL_PAGINATED = """
            SELECT * FROM %s
            ORDER BY data_instalacao DESC, created_at DESC
            LIMIT ? OFFSET ?
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_ID = """
            SELECT * FROM %s
            WHERE id = ?
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_INVENTARIO = """
            SELECT * FROM %s
            WHERE inventario_id = ?
            ORDER BY data_instalacao DESC
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_SKU = """
            SELECT * FROM %s
            WHERE sku_produto = ?
            ORDER BY data_instalacao DESC
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_DATA = """
            SELECT * FROM %s
            WHERE data_instalacao = ?
            ORDER BY inventario_id
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_PERIODO = """
            SELECT * FROM %s
            WHERE data_instalacao BETWEEN ? AND ?
            ORDER BY data_instalacao
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_PERCENTAGEM_RANGE = """
            SELECT * FROM %s
            WHERE percentagem_restante BETWEEN ? AND ?
            ORDER BY percentagem_restante ASC
            """.formatted(TABLE);

    private static final String SQL_FIND_BAIXA_PERCENTAGEM = """
            SELECT * FROM %s
            WHERE percentagem_restante <= ?
            ORDER BY percentagem_restante ASC
            """.formatted(TABLE);

    private static final String SQL_FIND_ESGOTADOS = """
            SELECT * FROM %s
            WHERE percentagem_restante = 0
            ORDER BY data_instalacao DESC
            """.formatted(TABLE);

    private static final String SQL_FIND_CHEIOS = """
            SELECT * FROM %s
            WHERE percentagem_restante = 100
            ORDER BY data_instalacao DESC
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_USUARIO = """
            SELECT * FROM %s
            WHERE usuario_responsavel = ?
            ORDER BY data_instalacao DESC
            """.formatted(TABLE);

    private static final String SQL_FIND_ATIVO_BY_INVENTARIO = """
            SELECT * FROM %s
            WHERE inventario_id = ? AND percentagem_restante > 0
            ORDER BY data_instalacao DESC
            LIMIT 1
            """.formatted(TABLE);

    private static final String SQL_FIND_ULTIMO_BY_INVENTARIO = """
            SELECT * FROM %s
            WHERE inventario_id = ?
            ORDER BY data_instalacao DESC
            LIMIT 1
            """.formatted(TABLE);

    private static final String SQL_FIND_PARA_SUBSTITUICAO = """
            SELECT * FROM %s
            WHERE percentagem_restante <= ? AND percentagem_restante > 0
            ORDER BY percentagem_restante ASC, data_instalacao ASC
            """.formatted(TABLE);

    private static final String SQL_INSERT = """
            INSERT INTO %s (
                inventario_id, sku_produto, data_instalacao,
                usuario_responsavel, percentagem_restante, ciclos_impressao, observacoes
            ) VALUES (?, ?, ?, ?, ?, ?, ?)
            """.formatted(TABLE);

    private static final String SQL_UPDATE = """
            UPDATE %s
            SET inventario_id = ?, sku_produto = ?, data_instalacao = ?,
                usuario_responsavel = ?, percentagem_restante = ?, ciclos_impressao = ?,
                observacoes = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """.formatted(TABLE);

    private static final String SQL_UPDATE_PERCENTAGEM = """
            UPDATE %s
            SET percentagem_restante = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """.formatted(TABLE);

    private static final String SQL_INCREMENT_CICLOS = """
            UPDATE %s
            SET ciclos_impressao = ciclos_impressao + ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """.formatted(TABLE);

    private static final String SQL_MARCAR_ESGOTADO = """
            UPDATE %s
            SET percentagem_restante = 0, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """.formatted(TABLE);

    private static final String SQL_DELETE = """
            DELETE FROM %s
            WHERE id = ?
            """.formatted(TABLE);

    private static final String SQL_DELETE_BY_INVENTARIO = """
            DELETE FROM %s
            WHERE inventario_id = ?
            """.formatted(TABLE);

    // ===== CRUD =====

    public List<GestaoToners> findAll() throws SQLException {
        return executeQuery(SQL_FIND_ALL);
    }

    public List<GestaoToners> findAll(int limit, int offset) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_ALL_PAGINATED)) {

            stmt.setInt(1, limit);
            stmt.setInt(2, offset);
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    public Optional<GestaoToners> findById(Long id) throws SQLException {
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

    public List<GestaoToners> findByInventarioId(Long inventarioId) throws SQLException {
        if (inventarioId == null || inventarioId <= 0) {
            return List.of();
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_INVENTARIO)) {

            stmt.setLong(1, inventarioId);
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    public List<GestaoToners> findBySkuProduto(String sku) throws SQLException {
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

    public List<GestaoToners> findByDataInstalacao(LocalDate data) throws SQLException {
        if (data == null) {
            return List.of();
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_DATA)) {

            stmt.setDate(1, Date.valueOf(data));
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    public List<GestaoToners> findByPeriodoInstalacao(LocalDate inicio, LocalDate fim) throws SQLException {
        if (inicio == null || fim == null) {
            return List.of();
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_PERIODO)) {

            stmt.setDate(1, Date.valueOf(inicio));
            stmt.setDate(2, Date.valueOf(fim));
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    public List<GestaoToners> findByPercentagemRange(int min, int max) throws SQLException {
        if (min < 0 || max > 100 || min > max) {
            return List.of();
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_PERCENTAGEM_RANGE)) {

            stmt.setInt(1, min);
            stmt.setInt(2, max);
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    public List<GestaoToners> findBaixaPercentagem(int limite) throws SQLException {
        if (limite < 0) {
            return List.of();
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BAIXA_PERCENTAGEM)) {

            stmt.setInt(1, limite);
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    public List<GestaoToners> findEsgotados() throws SQLException {
        return executeQuery(SQL_FIND_ESGOTADOS);
    }

    public List<GestaoToners> findCheios() throws SQLException {
        return executeQuery(SQL_FIND_CHEIOS);
    }

    public List<GestaoToners> findByUsuarioResponsavel(Long usuarioId) throws SQLException {
        if (usuarioId == null || usuarioId <= 0) {
            return List.of();
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_USUARIO)) {

            stmt.setLong(1, usuarioId);
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    public List<GestaoToners> findParaSubstituicao(int percentagemAlerta) throws SQLException {
        if (percentagemAlerta < 0) {
            return List.of();
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_PARA_SUBSTITUICAO)) {

            stmt.setInt(1, percentagemAlerta);
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    public Optional<GestaoToners> findTonerAtivoByInventario(Long inventarioId) throws SQLException {
        if (inventarioId == null || inventarioId <= 0) {
            return Optional.empty();
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_ATIVO_BY_INVENTARIO)) {

            stmt.setLong(1, inventarioId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapResultSet(rs)) : Optional.empty();
            }
        }
    }

    public Optional<GestaoToners> findUltimoByInventario(Long inventarioId) throws SQLException {
        if (inventarioId == null || inventarioId <= 0) {
            return Optional.empty();
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_ULTIMO_BY_INVENTARIO)) {

            stmt.setLong(1, inventarioId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapResultSet(rs)) : Optional.empty();
            }
        }
    }

    public Long insert(GestaoToners toner) throws SQLException {
        if (!(toner instanceof GestaoToners t)) {
            throw new IllegalArgumentException("Toner inválido");
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_INSERT,
                     PreparedStatement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, t.inventarioId());
            stmt.setString(2, t.skuProduto());
            stmt.setDate(3, Date.valueOf(t.dataInstalacao()));
            stmt.setLong(4, t.usuarioResponsavel());
            stmt.setInt(5, t.percentagemRestante());
            stmt.setInt(6, t.ciclosImpressao());
            stmt.setString(7, t.observacoes());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
                throw new SQLException("Falha ao obter ID gerado");
            }
        }
    }

    public void update(GestaoToners toner) throws SQLException {
        if (!(toner instanceof GestaoToners t)) {
            throw new IllegalArgumentException("Toner inválido");
        }
        if (t.id() == null) {
            throw new SQLException("ID não pode ser nulo para atualização");
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE)) {

            stmt.setLong(1, t.inventarioId());
            stmt.setString(2, t.skuProduto());
            stmt.setDate(3, Date.valueOf(t.dataInstalacao()));
            stmt.setLong(4, t.usuarioResponsavel());
            stmt.setInt(5, t.percentagemRestante());
            stmt.setInt(6, t.ciclosImpressao());
            stmt.setString(7, t.observacoes());
            stmt.setLong(8, t.id());

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Toner com ID " + t.id() + " não encontrado");
            }
        }
    }

    public void updatePercentagem(Long id, int percentagem) throws SQLException {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID inválido");
        }
        if (percentagem < 0 || percentagem > 100) {
            throw new IllegalArgumentException("Percentagem deve estar entre 0 e 100");
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE_PERCENTAGEM)) {

            stmt.setInt(1, percentagem);
            stmt.setLong(2, id);
            int affected = stmt.executeUpdate();

            if (affected == 0) {
                throw new SQLException("Toner com ID " + id + " não encontrado");
            }
        }
    }

    public void incrementarCiclos(Long id, int ciclosAdicionais) throws SQLException {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID inválido");
        }
        if (ciclosAdicionais <= 0) {
            throw new IllegalArgumentException("Ciclos adicionais devem ser positivos");
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_INCREMENT_CICLOS)) {

            stmt.setInt(1, ciclosAdicionais);
            stmt.setLong(2, id);
            int affected = stmt.executeUpdate();

            if (affected == 0) {
                throw new SQLException("Toner com ID " + id + " não encontrado");
            }
        }
    }

    public void marcarComoEsgotado(Long id) throws SQLException {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID inválido");
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_MARCAR_ESGOTADO)) {

            stmt.setLong(1, id);
            int affected = stmt.executeUpdate();

            if (affected == 0) {
                throw new SQLException("Toner com ID " + id + " não encontrado");
            }
        }
    }

    public void delete(Long id) throws SQLException {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID inválido");
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_DELETE)) {

            stmt.setLong(1, id);
            int affected = stmt.executeUpdate();

            if (affected == 0) {
                throw new SQLException("Toner com ID " + id + " não encontrado");
            }
        }
    }

    public void deleteByInventario(Long inventarioId) throws SQLException {
        if (inventarioId == null || inventarioId <= 0) {
            throw new IllegalArgumentException("ID do inventário inválido");
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_DELETE_BY_INVENTARIO)) {

            stmt.setLong(1, inventarioId);
            stmt.executeUpdate();
        }
    }

    public Long save(GestaoToners toner) throws SQLException {
        if (toner.id() == null) {
            return insert(toner);
        } else {
            update(toner);
            return toner.id();
        }
    }

    // ===== FILTROS COMBINADOS =====

    public List<GestaoToners> findWithFilters(
            Long inventarioId,
            String skuProduto,
            Long usuarioId,
            LocalDate dataInicio,
            LocalDate dataFim,
            Integer percentagemMin,
            Integer percentagemMax
    ) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM " + TABLE + " WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (inventarioId != null && inventarioId > 0) {
            sql.append(" AND inventario_id = ?");
            params.add(inventarioId);
        }
        if (skuProduto != null && !skuProduto.isBlank()) {
            sql.append(" AND sku_produto = ?");
            params.add(skuProduto);
        }
        if (usuarioId != null && usuarioId > 0) {
            sql.append(" AND usuario_responsavel = ?");
            params.add(usuarioId);
        }
        if (dataInicio != null && dataFim != null) {
            sql.append(" AND data_instalacao BETWEEN ? AND ?");
            params.add(dataInicio);
            params.add(dataFim);
        }
        if (percentagemMin != null && percentagemMax != null) {
            sql.append(" AND percentagem_restante BETWEEN ? AND ?");
            params.add(percentagemMin);
            params.add(percentagemMax);
        } else if (percentagemMin != null) {
            sql.append(" AND percentagem_restante >= ?");
            params.add(percentagemMin);
        } else if (percentagemMax != null) {
            sql.append(" AND percentagem_restante <= ?");
            params.add(percentagemMax);
        }

        sql.append(" ORDER BY data_instalacao DESC, created_at DESC");

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

    // ===== VERIFICAÇÕES =====

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

    public boolean existsByInventarioAndSku(Long inventarioId, String sku) throws SQLException {
        if (inventarioId == null || sku == null || sku.isBlank()) {
            return false;
        }
        String sql = "SELECT COUNT(*) FROM %s WHERE inventario_id = ? AND sku_produto = ?".formatted(TABLE);

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, inventarioId);
            stmt.setString(2, sku);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public boolean skuExistsInCatalogo(String sku) throws SQLException {
        if (sku == null || sku.isBlank()) return false;
        String sql = "SELECT COUNT(*) FROM catalogo_produtos WHERE sku = ? AND ativo = 1";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, sku);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public boolean inventarioExists(Long inventarioId) throws SQLException {
        if (inventarioId == null) return false;
        String sql = "SELECT COUNT(*) FROM inventario_equipamentos WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, inventarioId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    // ===== ESTATÍSTICAS =====

    public int countAll() throws SQLException {
        return count("SELECT COUNT(*) FROM " + TABLE);
    }

    public int countAtivos() throws SQLException {
        return count("SELECT COUNT(*) FROM " + TABLE + " WHERE percentagem_restante > 0");
    }

    public int countEsgotados() throws SQLException {
        return count("SELECT COUNT(*) FROM " + TABLE + " WHERE percentagem_restante = 0");
    }

    public int countByInventario(Long inventarioId) throws SQLException {
        if (inventarioId == null) return 0;
        return count("SELECT COUNT(*) FROM " + TABLE + " WHERE inventario_id = ?", inventarioId);
    }

    public Map<String, Object[]> calcularVidaUtilPorSku() throws SQLException {
        String sql = """
                SELECT sku_produto,
                    AVG(ciclos_impressao) as media_ciclos,
                    AVG(percentagem_restante) as media_percentagem,
                    COUNT(*) as total_instalacoes,
                    MAX(ciclos_impressao) as max_ciclos,
                    MIN(ciclos_impressao) as min_ciclos
                FROM %s
                GROUP BY sku_produto
                ORDER BY media_ciclos DESC
                """.formatted(TABLE);

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            Map<String, Object[]> resultado = new LinkedHashMap<>();
            while (rs.next()) {
                resultado.put(
                        rs.getString("sku_produto"),
                        new Object[]{
                                rs.getDouble("media_ciclos"),
                                rs.getDouble("media_percentagem"),
                                rs.getInt("total_instalacoes"),
                                rs.getInt("max_ciclos"),
                                rs.getInt("min_ciclos")
                        }
                );
            }
            return Map.copyOf(resultado);
        }
    }

    public Map<String, Integer> getEstatisticasUso(LocalDate inicio, LocalDate fim) throws SQLException {
        if (inicio == null || fim == null) {
            return Map.of();
        }

        Map<String, Integer> stats = new LinkedHashMap<>();
        stats.put("total", count("SELECT COUNT(*) FROM " + TABLE + " WHERE data_instalacao BETWEEN ? AND ?", inicio, fim));
        stats.put("ativos", count("SELECT COUNT(*) FROM " + TABLE + " WHERE data_instalacao BETWEEN ? AND ? AND percentagem_restante > 0", inicio, fim));
        stats.put("substituidos", count("SELECT COUNT(*) FROM " + TABLE + " WHERE data_instalacao BETWEEN ? AND ? AND percentagem_restante = 0", inicio, fim));

        return Map.copyOf(stats);
    }

    public Map<String, Integer> countInstalacoesPorMes(int ano) throws SQLException {
        String sql = """
                SELECT DATE_FORMAT(data_instalacao, '%Y-%m') as mes,
                       COUNT(*) as total
                FROM %s
                WHERE YEAR(data_instalacao) = ?
                GROUP BY DATE_FORMAT(data_instalacao, '%Y-%m')
                ORDER BY mes
                """.formatted(TABLE);

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, ano);

            Map<String, Integer> resultado = new LinkedHashMap<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultado.put(rs.getString("mes"), rs.getInt("total"));
                }
            }
            return Map.copyOf(resultado);
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

    private List<GestaoToners> executeQuery(String sql) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            return mapResultSetList(rs);
        }
    }

    private List<GestaoToners> mapResultSetList(ResultSet rs) throws SQLException {
        List<GestaoToners> list = new ArrayList<>();
        while (rs.next()) {
            list.add(mapResultSet(rs));
        }
        return List.copyOf(list);
    }

    private GestaoToners mapResultSet(ResultSet rs) throws SQLException {
        return new GestaoToners(
                rs.getLong("id"),
                rs.getLong("inventario_id"),
                rs.getString("sku_produto"),
                getLocalDate(rs, "data_instalacao"),
                rs.getLong("usuario_responsavel"),
                rs.getInt("percentagem_restante"),
                rs.getInt("ciclos_impressao"),
                rs.getString("observacoes"),
                getLocalDateTime(rs, "created_at"),
                getLocalDateTime(rs, "updated_at")
        );
    }

    private LocalDate getLocalDate(ResultSet rs, String column) throws SQLException {
        Date d = rs.getDate(column);
        return d != null ? d.toLocalDate() : null;
    }

    private LocalDateTime getLocalDateTime(ResultSet rs, String column) throws SQLException {
        Timestamp ts = rs.getTimestamp(column);
        return ts != null ? ts.toLocalDateTime() : null;
    }
}