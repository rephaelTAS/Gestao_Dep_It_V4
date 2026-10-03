package com.ossobo.gestaoDepIt.db.repositories;

import com.ossobo.gestaoDepIt.db.config.DatabaseConnection;
import com.ossobo.gestaoDepIt.db.models.GestaoToners;

import com.ossobo.winterfx.anotations.Inject;
import com.ossobo.winterfx.anotations.Repository;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;

/**
 * GestaoTonersRepository - Acesso a dados com WinterFX
 * v2.1 - Alinhado ao GestaoToners v3.0 (PK/FKs String/UUID + colunas de sync)
 *
 * Mudanças v2.0 → v2.1:
 * - id, inventarioId e usuarioResponsavel: Long → String (UUID) em TODAS as
 *   assinaturas e setters
 * - INSERT inclui id (da fábrica), device_id, deleted, created_at, updated_at
 * - UPDATE grava device_id (origem da última mutação — sync LWW)
 * - +marcarDeletado(): tombstone de sync (DELETE físico continua disponível,
 *   mas para convergência entre devices o tombstone é o caminho)
 * - DATE_FORMAT/YEAR() (MySQL) → agrupamento por mês calculado no Java
 *   (SQLite não tem essas funções)
 * - getIntegerNullable: NULL no banco não vira mais 0 silenciosamente
 *   (0% = "esgotado" — um NULL virar 0 corrompia o dado!)
 * - calcularVidaUtilPorSku retorna record VidaUtilSku (antes: Object[])
 * - Queries de alerta/substituição filtram deleted = 0 (tombstones não
 *   devem disparar alertas de troca de toner)
 * - save() decide insert/update por existsById (a fábrica SEMPRE gera id,
 *   então id != null não significa mais "já persistido")
 */
@Repository
public class GestaoTonersRepository {

    private static final System.Logger logger = System.getLogger(GestaoTonersRepository.class.getName());

    @Inject
    private DatabaseConnection dbConnection;

    private static final String TABLE = "gestao_toners";

    // ============================================================
    // TIPOS AUXILIARES
    // ============================================================

    /** Estatísticas de vida útil agregadas por SKU (substitui o antigo Object[]). */
    public record VidaUtilSku(
            String sku,
            double mediaCiclos,
            double mediaPercentagem,
            int totalInstalacoes,
            int maxCiclos,
            int minCiclos
    ) {}

    // ============================================================
    // SQL COM TEXT BLOCKS
    // ============================================================

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

    // ✅ deleted = 0: toner tombado não pode disparar alerta de reposição
    private static final String SQL_FIND_BAIXA_PERCENTAGEM = """
            SELECT * FROM %s
            WHERE percentagem_restante <= ? AND deleted = 0
            ORDER BY percentagem_restante ASC
            """.formatted(TABLE);

    private static final String SQL_FIND_ESGOTADOS = """
            SELECT * FROM %s
            WHERE percentagem_restante = 0 AND deleted = 0
            ORDER BY data_instalacao DESC
            """.formatted(TABLE);

    private static final String SQL_FIND_CHEIOS = """
            SELECT * FROM %s
            WHERE percentagem_restante = 100 AND deleted = 0
            ORDER BY data_instalacao DESC
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_USUARIO = """
            SELECT * FROM %s
            WHERE usuario_responsavel = ?
            ORDER BY data_instalacao DESC
            """.formatted(TABLE);

    // ✅ "toner ATIVO do equipamento" — exclui tombstones por definição
    private static final String SQL_FIND_ATIVO_BY_INVENTARIO = """
            SELECT * FROM %s
            WHERE inventario_id = ? AND percentagem_restante > 0 AND deleted = 0
            ORDER BY data_instalacao DESC
            LIMIT 1
            """.formatted(TABLE);

    private static final String SQL_FIND_ULTIMO_BY_INVENTARIO = """
            SELECT * FROM %s
            WHERE inventario_id = ?
            ORDER BY data_instalacao DESC
            LIMIT 1
            """.formatted(TABLE);

    // ✅ lista de "quem trocar" — tombstones fora
    private static final String SQL_FIND_PARA_SUBSTITUICAO = """
            SELECT * FROM %s
            WHERE percentagem_restante <= ? AND percentagem_restante > 0 AND deleted = 0
            ORDER BY percentagem_restante ASC, data_instalacao ASC
            """.formatted(TABLE);

    // ✅ id/device_id/deleted/timestamps incluídos — a fábrica já os gerou
    private static final String SQL_INSERT = """
            INSERT INTO %s (
                id, inventario_id, sku_produto, data_instalacao,
                usuario_responsavel, percentagem_restante, ciclos_impressao,
                observacoes, device_id, deleted, created_at, updated_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.formatted(TABLE);

    private static final String SQL_UPDATE = """
            UPDATE %s
            SET inventario_id = ?, sku_produto = ?, data_instalacao = ?,
                usuario_responsavel = ?, percentagem_restante = ?, ciclos_impressao = ?,
                observacoes = ?, device_id = ?, updated_at = ?
            WHERE id = ?
            """.formatted(TABLE);

    private static final String SQL_UPDATE_PERCENTAGEM = """
            UPDATE %s
            SET percentagem_restante = ?, device_id = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """.formatted(TABLE);

    private static final String SQL_INCREMENT_CICLOS = """
            UPDATE %s
            SET ciclos_impressao = ciclos_impressao + ?, device_id = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """.formatted(TABLE);

    private static final String SQL_MARCAR_ESGOTADO = """
            UPDATE %s
            SET percentagem_restante = 0, device_id = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """.formatted(TABLE);

    /** ✅ Tombstone de sync — a exclusão que CONVERGE entre devices. */
    private static final String SQL_MARCAR_DELETADO = """
            UPDATE %s
            SET deleted = 1, device_id = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """.formatted(TABLE);

    private static final String SQL_DELETE = """
            DELETE FROM %s
            WHERE id = ?
            """.formatted(TABLE);

    // ⚠️ DELETE físico em massa — com sync, prefira tombar cada registro
    private static final String SQL_DELETE_BY_INVENTARIO = """
            DELETE FROM %s
            WHERE inventario_id = ?
            """.formatted(TABLE);

    // ============================================================
    // CRUD — LEITURAS
    // ============================================================

    public List<GestaoToners> findAll() throws SQLException {
        return executeQuery(SQL_FIND_ALL);
    }

    public List<GestaoToners> findAll(int limit, int offset) throws SQLException {
        return executeQuery(SQL_FIND_ALL_PAGINATED, limit, offset);
    }

    public Optional<GestaoToners> findById(String id) throws SQLException {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        List<GestaoToners> resultado = executeQuery(SQL_FIND_BY_ID, id);
        return resultado.isEmpty() ? Optional.empty() : Optional.of(resultado.get(0));
    }

    public List<GestaoToners> findByInventarioId(String inventarioId) throws SQLException {
        if (inventarioId == null || inventarioId.isBlank()) {
            return List.of();
        }
        return executeQuery(SQL_FIND_BY_INVENTARIO, inventarioId);
    }

    public List<GestaoToners> findBySkuProduto(String sku) throws SQLException {
        if (sku == null || sku.isBlank()) {
            return List.of();
        }
        return executeQuery(SQL_FIND_BY_SKU, sku);
    }

    public List<GestaoToners> findByDataInstalacao(LocalDate data) throws SQLException {
        if (data == null) {
            return List.of();
        }
        return executeQuery(SQL_FIND_BY_DATA, Date.valueOf(data));
    }

    public List<GestaoToners> findByPeriodoInstalacao(LocalDate inicio, LocalDate fim) throws SQLException {
        if (inicio == null || fim == null) {
            return List.of();
        }
        return executeQuery(SQL_FIND_BY_PERIODO, Date.valueOf(inicio), Date.valueOf(fim));
    }

    public List<GestaoToners> findByPercentagemRange(int min, int max) throws SQLException {
        if (min < 0 || max > 100 || min > max) {
            return List.of();
        }
        return executeQuery(SQL_FIND_BY_PERCENTAGEM_RANGE, min, max);
    }

    public List<GestaoToners> findBaixaPercentagem(int limite) throws SQLException {
        if (limite < 0) {
            return List.of();
        }
        return executeQuery(SQL_FIND_BAIXA_PERCENTAGEM, limite);
    }

    public List<GestaoToners> findEsgotados() throws SQLException {
        return executeQuery(SQL_FIND_ESGOTADOS);
    }

    public List<GestaoToners> findCheios() throws SQLException {
        return executeQuery(SQL_FIND_CHEIOS);
    }

    public List<GestaoToners> findByUsuarioResponsavel(String usuarioId) throws SQLException {
        if (usuarioId == null || usuarioId.isBlank()) {
            return List.of();
        }
        return executeQuery(SQL_FIND_BY_USUARIO, usuarioId);
    }

    public List<GestaoToners> findParaSubstituicao(int percentagemAlerta) throws SQLException {
        if (percentagemAlerta < 0) {
            return List.of();
        }
        return executeQuery(SQL_FIND_PARA_SUBSTITUICAO, percentagemAlerta);
    }

    public Optional<GestaoToners> findTonerAtivoByInventario(String inventarioId) throws SQLException {
        if (inventarioId == null || inventarioId.isBlank()) {
            return Optional.empty();
        }
        List<GestaoToners> resultado = executeQuery(SQL_FIND_ATIVO_BY_INVENTARIO, inventarioId);
        return resultado.isEmpty() ? Optional.empty() : Optional.of(resultado.get(0));
    }

    public Optional<GestaoToners> findUltimoByInventario(String inventarioId) throws SQLException {
        if (inventarioId == null || inventarioId.isBlank()) {
            return Optional.empty();
        }
        List<GestaoToners> resultado = executeQuery(SQL_FIND_ULTIMO_BY_INVENTARIO, inventarioId);
        return resultado.isEmpty() ? Optional.empty() : Optional.of(resultado.get(0));
    }

    // ============================================================
    // CRUD — ESCRITAS
    // ============================================================

    /**
     * Insere o toner. O id (UUID v4) JÁ veio da fábrica — não há
     * "generated keys" a recuperar.
     *
     * @return o id do toner inserido (o mesmo que veio no objeto)
     */
    public String insert(GestaoToners toner) throws SQLException {
        if (toner == null) {
            throw new IllegalArgumentException("Toner inválido");
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_INSERT)) {

            LocalDateTime agora = LocalDateTime.now();
            stmt.setString(1, toner.id());
            stmt.setString(2, toner.inventarioId());
            stmt.setString(3, toner.skuProduto());
            stmt.setDate(4, Date.valueOf(
                    toner.dataInstalacao() != null ? toner.dataInstalacao() : LocalDate.now()));
            stmt.setString(5, toner.usuarioResponsavel());
            stmt.setInt(6, toner.percentagemRestante() != null ? toner.percentagemRestante() : 100);
            stmt.setInt(7, toner.ciclosImpressao() != null ? toner.ciclosImpressao() : 0);
            stmt.setString(8, toner.observacoes());
            stmt.setString(9, getDeviceId());
            stmt.setBoolean(10, toner.isDeletado());
            stmt.setTimestamp(11, Timestamp.valueOf(
                    toner.createdAt() != null ? toner.createdAt() : agora));
            stmt.setTimestamp(12, Timestamp.valueOf(
                    toner.updatedAt() != null ? toner.updatedAt() : agora));

            stmt.executeUpdate();
            return toner.id();
        }
    }

    public void update(GestaoToners toner) throws SQLException {
        if (toner == null) {
            throw new IllegalArgumentException("Toner inválido");
        }
        if (toner.id() == null || toner.id().isBlank()) {
            throw new SQLException("ID não pode ser vazio para atualização");
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE)) {

            stmt.setString(1, toner.inventarioId());
            stmt.setString(2, toner.skuProduto());
            stmt.setDate(3, Date.valueOf(
                    toner.dataInstalacao() != null ? toner.dataInstalacao() : LocalDate.now()));
            stmt.setString(4, toner.usuarioResponsavel());
            stmt.setInt(5, toner.percentagemRestante() != null ? toner.percentagemRestante() : 100);
            stmt.setInt(6, toner.ciclosImpressao() != null ? toner.ciclosImpressao() : 0);
            stmt.setString(7, toner.observacoes());
            stmt.setString(8, getDeviceId());
            stmt.setTimestamp(9, Timestamp.valueOf(
                    toner.updatedAt() != null ? toner.updatedAt() : LocalDateTime.now()));
            stmt.setString(10, toner.id());

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Toner com ID " + toner.id() + " não encontrado");
            }
        }
    }

    /**
     * Insere ou atualiza conforme o id já existir no banco.
     * ✅ A fábrica SEMPRE gera id — id != null não significa mais "já persistido",
     * então a decisão é por consulta (custo: 1 COUNT, segurança: total).
     */
    public String save(GestaoToners toner) throws SQLException {
        if (toner == null || toner.id() == null || toner.id().isBlank()) {
            throw new IllegalArgumentException("Toner com ID inválido");
        }
        if (existsById(toner.id())) {
            update(toner);
        } else {
            insert(toner);
        }
        return toner.id();
    }

    public void updatePercentagem(String id, int percentagem) throws SQLException {
        if (percentagem < 0 || percentagem > 100) {
            throw new IllegalArgumentException("Percentagem deve estar entre 0 e 100");
        }
        int affected = executeUpdate(SQL_UPDATE_PERCENTAGEM, percentagem, getDeviceId(), id);
        if (affected == 0) {
            throw new SQLException("Toner com ID " + id + " não encontrado");
        }
    }

    public void incrementarCiclos(String id, int ciclosAdicionais) throws SQLException {
        if (ciclosAdicionais <= 0) {
            throw new IllegalArgumentException("Ciclos adicionais devem ser positivos");
        }
        int affected = executeUpdate(SQL_INCREMENT_CICLOS, ciclosAdicionais, getDeviceId(), id);
        if (affected == 0) {
            throw new SQLException("Toner com ID " + id + " não encontrado");
        }
    }

    public void marcarComoEsgotado(String id) throws SQLException {
        int affected = executeUpdate(SQL_MARCAR_ESGOTADO, getDeviceId(), id);
        if (affected == 0) {
            throw new SQLException("Toner com ID " + id + " não encontrado");
        }
    }

    /**
     * ✅ Exclusão lógica para sync (tombstone + updatedAt novo = LWW converge).
     * Preferir este método ao DELETE físico enquanto o sync estiver ativo.
     */
    public void marcarDeletado(String id) throws SQLException {
        int affected = executeUpdate(SQL_MARCAR_DELETADO, getDeviceId(), id);
        if (affected == 0) {
            throw new SQLException("Toner com ID " + id + " não encontrado");
        }
    }

    /** DELETE físico individual. Com sync ativo, prefira {@link #marcarDeletado(String)}. */
    public void delete(String id) throws SQLException {
        int affected = executeUpdate(SQL_DELETE, id);
        if (affected == 0) {
            throw new SQLException("Toner com ID " + id + " não encontrado");
        }
    }

    /** ⚠️ DELETE físico em massa. Com sync ativo, tombe cada registro em vez disto. */
    public void deleteByInventario(String inventarioId) throws SQLException {
        if (inventarioId == null || inventarioId.isBlank()) {
            throw new IllegalArgumentException("ID do inventário inválido");
        }
        executeUpdate(SQL_DELETE_BY_INVENTARIO, inventarioId);
    }

    // ============================================================
    // FILTROS COMBINADOS
    // ============================================================

    public List<GestaoToners> findWithFilters(
            String inventarioId,
            String skuProduto,
            String usuarioId,
            LocalDate dataInicio,
            LocalDate dataFim,
            Integer percentagemMin,
            Integer percentagemMax
    ) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM " + TABLE + " WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (inventarioId != null && !inventarioId.isBlank()) {
            sql.append(" AND inventario_id = ?");
            params.add(inventarioId);
        }
        if (skuProduto != null && !skuProduto.isBlank()) {
            sql.append(" AND sku_produto = ?");
            params.add(skuProduto);
        }
        if (usuarioId != null && !usuarioId.isBlank()) {
            sql.append(" AND usuario_responsavel = ?");
            params.add(usuarioId);
        }
        if (dataInicio != null && dataFim != null) {
            sql.append(" AND data_instalacao BETWEEN ? AND ?");
            params.add(Date.valueOf(dataInicio));   // ✅ explícito, não setObject(LocalDate)
            params.add(Date.valueOf(dataFim));
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

        return executeQuery(sql.toString(), params.toArray());
    }

    // ============================================================
    // VERIFICAÇÕES
    // ============================================================

    public boolean existsById(String id) throws SQLException {
        if (id == null || id.isBlank()) return false;
        return count("SELECT COUNT(*) FROM " + TABLE + " WHERE id = ?", id) > 0;
    }

    public boolean existsByInventarioAndSku(String inventarioId, String sku) throws SQLException {
        if (inventarioId == null || inventarioId.isBlank() || sku == null || sku.isBlank()) {
            return false;
        }
        return count("SELECT COUNT(*) FROM " + TABLE
                + " WHERE inventario_id = ? AND sku_produto = ?", inventarioId, sku) > 0;
    }

    public boolean skuExistsInCatalogo(String sku) throws SQLException {
        if (sku == null || sku.isBlank()) return false;
        return count("SELECT COUNT(*) FROM catalogo_produtos WHERE sku = ? AND ativo = 1", sku) > 0;
    }

    /** ✅ FK agora é UUID (String). */
    public boolean inventarioExists(String inventarioId) throws SQLException {
        if (inventarioId == null || inventarioId.isBlank()) return false;
        return count("SELECT COUNT(*) FROM inventario_equipamentos WHERE id = ?", inventarioId) > 0;
    }

    // ============================================================
    // ESTATÍSTICAS
    // ============================================================

    public int countAll() throws SQLException {
        return count("SELECT COUNT(*) FROM " + TABLE);
    }

    public int countAtivos() throws SQLException {
        return count("SELECT COUNT(*) FROM " + TABLE + " WHERE percentagem_restante > 0 AND deleted = 0");
    }

    public int countEsgotados() throws SQLException {
        return count("SELECT COUNT(*) FROM " + TABLE + " WHERE percentagem_restante = 0 AND deleted = 0");
    }

    public int countByInventario(String inventarioId) throws SQLException {
        if (inventarioId == null || inventarioId.isBlank()) return 0;
        return count("SELECT COUNT(*) FROM " + TABLE + " WHERE inventario_id = ?", inventarioId);
    }

    public List<VidaUtilSku> calcularVidaUtilPorSku() throws SQLException {
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

            List<VidaUtilSku> resultado = new ArrayList<>();
            while (rs.next()) {
                resultado.add(new VidaUtilSku(
                        rs.getString("sku_produto"),
                        rs.getDouble("media_ciclos"),
                        rs.getDouble("media_percentagem"),
                        rs.getInt("total_instalacoes"),
                        rs.getInt("max_ciclos"),
                        rs.getInt("min_ciclos")));
            }
            return List.copyOf(resultado);
        }
    }

    public Map<String, Integer> getEstatisticasUso(LocalDate inicio, LocalDate fim) throws SQLException {
        if (inicio == null || fim == null) {
            return Map.of();
        }

        String base = "SELECT COUNT(*) FROM " + TABLE + " WHERE data_instalacao BETWEEN ? AND ?";
        Map<String, Integer> stats = new LinkedHashMap<>();
        stats.put("total", count(base, Date.valueOf(inicio), Date.valueOf(fim)));
        stats.put("ativos", count(base + " AND percentagem_restante > 0", Date.valueOf(inicio), Date.valueOf(fim)));
        stats.put("substituidos", count(base + " AND percentagem_restante = 0", Date.valueOf(inicio), Date.valueOf(fim)));

        return stats;   // LinkedHashMap preserva a ordem de inserção (Map.copyOf NÃO preserva!)
    }

    /**
     * ✅ Era DATE_FORMAT/YEAR() (MySQL — não existem em SQLite).
     * Agora: busca o período no SQL e agrupa por mês no Java. Portável.
     */
    public Map<String, Integer> countInstalacoesPorMes(int ano) throws SQLException {
        LocalDate inicio = LocalDate.of(ano, Month.JANUARY, 1);
        LocalDate fim = LocalDate.of(ano, Month.DECEMBER, 31);

        String sql = "SELECT data_instalacao FROM " + TABLE
                + " WHERE data_instalacao BETWEEN ? AND ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(inicio));
            stmt.setDate(2, Date.valueOf(fim));

            // TreeMap = meses já em ordem; unmodifiableSortedMap preserva a ordem
            SortedMap<String, Integer> resultado = new TreeMap<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    LocalDate data = getLocalDate(rs, "data_instalacao");
                    if (data != null) {
                        String mes = String.format("%04d-%02d", data.getYear(), data.getMonthValue());
                        resultado.merge(mes, 1, Integer::sum);
                    }
                }
            }
            return Collections.unmodifiableSortedMap(resultado);
        }
    }

    // ============================================================
    // MÉTODOS PRIVADOS (helpers)
    // ============================================================

    private List<GestaoToners> executeQuery(String sql, Object... params) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            bindParams(stmt, params);

            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    private int executeUpdate(String sql, Object... params) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            bindParams(stmt, params);
            return stmt.executeUpdate();
        }
    }

    private void bindParams(PreparedStatement stmt, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            stmt.setObject(i + 1, params[i]);
        }
    }

    private int count(String sql, Object... params) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            bindParams(stmt, params);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private List<GestaoToners> mapResultSetList(ResultSet rs) throws SQLException {
        List<GestaoToners> list = new ArrayList<>();
        while (rs.next()) {
            list.add(mapResultSet(rs));
        }
        return List.copyOf(list);
    }

    // ✅ 12 componentes — alinhado ao GestaoToners v3.0
    private GestaoToners mapResultSet(ResultSet rs) throws SQLException {
        return new GestaoToners(
                rs.getString("id"),
                rs.getString("inventario_id"),
                rs.getString("sku_produto"),
                getLocalDate(rs, "data_instalacao"),
                rs.getString("usuario_responsavel"),
                getIntegerNullable(rs, "percentagem_restante"),
                getIntegerNullable(rs, "ciclos_impressao"),
                rs.getString("observacoes"),
                getLocalDateTime(rs, "created_at"),
                getLocalDateTime(rs, "updated_at"),
                rs.getString("device_id"),
                getBooleanNullable(rs, "deleted")
        );
    }

    /** NULL no banco → null no Java. rs.getInt devolveria 0 (= "esgotado"!) silenciosamente. */
    private Integer getIntegerNullable(ResultSet rs, String column) throws SQLException {
        int valor = rs.getInt(column);
        return rs.wasNull() ? null : valor;
    }

    private Boolean getBooleanNullable(ResultSet rs, String column) throws SQLException {
        int valor = rs.getInt(column);
        return rs.wasNull() ? null : valor != 0;
    }

    private LocalDate getLocalDate(ResultSet rs, String column) throws SQLException {
        Date d = rs.getDate(column);
        return d != null ? d.toLocalDate() : null;
    }

    private LocalDateTime getLocalDateTime(ResultSet rs, String column) throws SQLException {
        Timestamp ts = rs.getTimestamp(column);
        return ts != null ? ts.toLocalDateTime() : null;
    }

    /**
     * Identidade do device de origem (sync LWW).
     * TODO (Degrau 2 do sync): substituir por DeviceIdentityService (@Service).
     */
    private String getDeviceId() {
        return "UNKNOWN-DEVICE";
    }
}