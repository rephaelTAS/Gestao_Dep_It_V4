package com.ossobo.gestaoDepIt.db.repositories;

import com.ossobo.gestaoDepIt.db.config.DatabaseConnection;
import com.ossobo.gestaoDepIt.db.models.InventarioEquipamentos;
import com.ossobo.gestaoDepIt.db.sync.DeviceIdentity;
import com.ossobo.gestaoDepIt.db.sync.SyncTime;
import com.ossobo.gestaoDepIt.utils.DateUtils;
import com.ossobo.winterfx.anotations.Inject;
import com.ossobo.winterfx.anotations.Repository;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * InventarioEquipamentosRepository v3.3
 *
 * Responsabilidade: Acesso a dados de inventario_equipamentos (model v3.1).
 *
 * v3.3 — Parser delegado ao DateUtils:
 *        - getLocalDate / getLocalDateTime chamam DateUtils.parseDateTolerante
 *          e DateUtils.parseDateTimeTolerante — fonte única do formato.
 *        - setLocalDateOrNull: null-safe para java.sql.Date (evita
 *          "Cannot invoke LocalDate.getYear() because date is null").
 *        - Consolida tolerância a ISO, SQLite default e ms Unix.
 *
 * v3.2 — getLocalDateTime reintroduzido, tolerante a dois formatos.
 * v3.1 — getLocalDate tolerante a NULL do xerial.
 * v3.0 — Separação estrutural leitura/mutação.
 *
 * @since v3.3
 */
@Repository
public class InventarioEquipamentosRepository {

    private static final System.Logger logger =
            System.getLogger(InventarioEquipamentosRepository.class.getName());

    @Inject
    private DatabaseConnection dbConnection;

    @Inject
    private DeviceIdentity deviceIdentity;

    private static final String TABLE = "inventario_equipamentos";

    // ============================================================
    // SQL
    // ============================================================

    private static final String SQL_FIND_ALL = """
            SELECT * FROM %s WHERE deleted = 0
            ORDER BY created_at DESC
            """.formatted(TABLE);

    private static final String SQL_FIND_ALL_PAGINATED = """
            SELECT * FROM %s WHERE deleted = 0
            ORDER BY created_at DESC
            LIMIT ? OFFSET ?
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_ID = """
            SELECT * FROM %s WHERE id = ? AND deleted = 0
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_NUM_SERIE = """
            SELECT * FROM %s WHERE num_serie = ? AND deleted = 0
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_MAC = """
            SELECT * FROM %s WHERE endereco_mac = ? AND deleted = 0
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_MAC_LIKE = """
            SELECT * FROM %s WHERE endereco_mac LIKE ? AND deleted = 0
            ORDER BY endereco_mac
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_STATUS = """
            SELECT * FROM %s WHERE status = ? AND deleted = 0
            ORDER BY data_aquisicao DESC
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_CONDICAO = """
            SELECT * FROM %s WHERE condicao = ? AND deleted = 0
            ORDER BY data_ultima_verificacao DESC
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_LOCALIZACAO = """
            SELECT * FROM %s WHERE localizacao = ? AND deleted = 0
            ORDER BY departamento, num_serie
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_DEPARTAMENTO = """
            SELECT * FROM %s WHERE departamento = ? AND deleted = 0
            ORDER BY localizacao, num_serie
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_FUNCIONARIO = """
            SELECT * FROM %s WHERE funcionario_id = ? AND deleted = 0
            ORDER BY data_aquisicao DESC
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_SKU = """
            SELECT * FROM %s WHERE sku_produto = ? AND deleted = 0
            ORDER BY status, num_serie
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_PERIODO_AQUISICAO = """
            SELECT * FROM %s WHERE data_aquisicao BETWEEN ? AND ? AND deleted = 0
            ORDER BY data_aquisicao
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_NUMERO_FATURA = """
            SELECT * FROM %s WHERE numero_fatura = ? AND deleted = 0
            ORDER BY data_aquisicao DESC
            """.formatted(TABLE);

    private static final String SQL_FIND_CRITICOS = """
            SELECT * FROM %s WHERE condicao = 'CRITICO' AND status = 'ATIVO' AND deleted = 0
            ORDER BY data_ultima_verificacao
            """.formatted(TABLE);

    private static final String SQL_FIND_VERIFICACAO_LIMITE = """
            SELECT * FROM %s
            WHERE (data_ultima_verificacao IS NULL OR data_ultima_verificacao < ?)
            AND status = 'ATIVO' AND deleted = 0
            ORDER BY data_ultima_verificacao
            """.formatted(TABLE);

    private static final String SQL_FIND_SEM_MAC = """
            SELECT * FROM %s
            WHERE (endereco_mac IS NULL OR endereco_mac = '')
            AND status = 'ATIVO' AND deleted = 0
            ORDER BY data_aquisicao
            """.formatted(TABLE);

    private static final String SQL_INSERT = """
            INSERT INTO %s (
                id, sku_produto, funcionario_id, num_serie, endereco_mac,
                data_aquisicao, data_instalacao, data_ultima_verificacao,
                numero_fatura, localizacao, departamento, status, condicao,
                documento_entrega, documento_devolucao, observacoes, devolucao,
                created_at, updated_at, device_id, deleted
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)
            """.formatted(TABLE);

    private static final String SQL_UPDATE = """
            UPDATE %s
            SET sku_produto = ?, funcionario_id = ?, num_serie = ?, endereco_mac = ?,
                data_aquisicao = ?, data_instalacao = ?, data_ultima_verificacao = ?,
                numero_fatura = ?, localizacao = ?, departamento = ?, status = ?,
                condicao = ?, documento_entrega = ?, documento_devolucao = ?,
                observacoes = ?, devolucao = ?, updated_at = ?, device_id = ?
            WHERE id = ? AND deleted = 0
            """.formatted(TABLE);

    private static final String SQL_TOMBSTONE = """
            UPDATE %s SET deleted = 1, updated_at = ?, device_id = ?
            WHERE id = ? AND deleted = 0
            """.formatted(TABLE);

    private static final String SQL_UPDATE_STATUS = """
            UPDATE %s SET status = ?, updated_at = ?, device_id = ?
            WHERE id = ? AND deleted = 0
            """.formatted(TABLE);

    private static final String SQL_UPDATE_CONDICAO = """
            UPDATE %s SET condicao = ?, updated_at = ?, device_id = ?
            WHERE id = ? AND deleted = 0
            """.formatted(TABLE);

    private static final String SQL_UPDATE_VERIFICACAO = """
            UPDATE %s
            SET data_ultima_verificacao = ?, condicao = ?, observacoes = ?,
                updated_at = ?, device_id = ?
            WHERE id = ? AND deleted = 0
            """.formatted(TABLE);

    private static final String SQL_UPDATE_LOCALIZACAO = """
            UPDATE %s SET localizacao = ?, departamento = ?, updated_at = ?, device_id = ?
            WHERE id = ? AND deleted = 0
            """.formatted(TABLE);

    private static final String SQL_UPDATE_FUNCIONARIO = """
            UPDATE %s SET funcionario_id = ?, updated_at = ?, device_id = ?
            WHERE id = ? AND deleted = 0
            """.formatted(TABLE);

    private static final String SQL_UPDATE_MAC = """
            UPDATE %s SET endereco_mac = ?, updated_at = ?, device_id = ?
            WHERE id = ? AND deleted = 0
            """.formatted(TABLE);

    private static final String SQL_UPDATE_FATURA = """
            UPDATE %s SET numero_fatura = ?, updated_at = ?, device_id = ?
            WHERE id = ? AND deleted = 0
            """.formatted(TABLE);

    private static final String SQL_UPDATE_DOCUMENTO_ENTREGA = """
            UPDATE %s SET documento_entrega = ?, updated_at = ?, device_id = ?
            WHERE id = ? AND deleted = 0
            """.formatted(TABLE);

    private static final String SQL_UPDATE_DOCUMENTO_DEVOLUCAO = """
            UPDATE %s SET documento_devolucao = ?, updated_at = ?, device_id = ?
            WHERE id = ? AND deleted = 0
            """.formatted(TABLE);

    private static final String SQL_UPDATE_DEVOLUCAO = """
            UPDATE %s SET devolucao = ?, updated_at = ?, device_id = ?
            WHERE id = ? AND deleted = 0
            """.formatted(TABLE);

    private static final String SQL_DELETE_PERMANENT = """
            DELETE FROM %s WHERE id = ? AND deleted = 0
            """.formatted(TABLE);

    // ============================================================
    // LEITURAS PURAS (abrem conexão própria)
    // ============================================================

    public List<InventarioEquipamentos> findAll() throws SQLException {
        try (Connection conn = dbConnection.getConnection()) {
            return executeQuery(conn, SQL_FIND_ALL);
        }
    }

    public List<InventarioEquipamentos> findAll(int limit, int offset) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_ALL_PAGINATED)) {
            stmt.setInt(1, limit);
            stmt.setInt(2, offset);
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    public Optional<InventarioEquipamentos> findById(String id) throws SQLException {
        if (id == null || id.isBlank()) return Optional.empty();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_ID)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapResultSet(rs)) : Optional.empty();
            }
        }
    }

    public Optional<InventarioEquipamentos> findByNumSerie(String numSerie) throws SQLException {
        if (numSerie == null || numSerie.isBlank()) return Optional.empty();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_NUM_SERIE)) {
            stmt.setString(1, numSerie);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapResultSet(rs)) : Optional.empty();
            }
        }
    }

    public Optional<InventarioEquipamentos> findByMacAddress(String mac) throws SQLException {
        if (mac == null || mac.isBlank()) return Optional.empty();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_MAC)) {
            stmt.setString(1, mac);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapResultSet(rs)) : Optional.empty();
            }
        }
    }

    public List<InventarioEquipamentos> findByMacAddressLike(String pattern) throws SQLException {
        if (pattern == null || pattern.isBlank()) return List.of();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_MAC_LIKE)) {
            stmt.setString(1, "%" + pattern + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    public List<InventarioEquipamentos> findByStatus(String status) throws SQLException {
        if (status == null || status.isBlank()) return List.of();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_STATUS)) {
            stmt.setString(1, status);
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    public List<InventarioEquipamentos> findByCondicao(String condicao) throws SQLException {
        if (condicao == null || condicao.isBlank()) return List.of();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_CONDICAO)) {
            stmt.setString(1, condicao);
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    public List<InventarioEquipamentos> findByLocalizacao(String localizacao) throws SQLException {
        if (localizacao == null || localizacao.isBlank()) return List.of();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_LOCALIZACAO)) {
            stmt.setString(1, localizacao);
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    public List<InventarioEquipamentos> findByDepartamento(String departamento) throws SQLException {
        if (departamento == null || departamento.isBlank()) return List.of();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_DEPARTAMENTO)) {
            stmt.setString(1, departamento);
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    public List<InventarioEquipamentos> findByFuncionarioId(String funcionarioId) throws SQLException {
        if (funcionarioId == null || funcionarioId.isBlank()) return List.of();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_FUNCIONARIO)) {
            stmt.setString(1, funcionarioId);
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    public List<InventarioEquipamentos> findBySkuProduto(String sku) throws SQLException {
        if (sku == null || sku.isBlank()) return List.of();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_SKU)) {
            stmt.setString(1, sku);
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    public List<InventarioEquipamentos> findByPeriodoAquisicao(LocalDate inicio, LocalDate fim) throws SQLException {
        if (inicio == null || fim == null) return List.of();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_PERIODO_AQUISICAO)) {
            stmt.setDate(1, Date.valueOf(inicio));
            stmt.setDate(2, Date.valueOf(fim));
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    public List<InventarioEquipamentos> findByNumeroFatura(String numeroFatura) throws SQLException {
        if (numeroFatura == null || numeroFatura.isBlank()) return List.of();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_NUMERO_FATURA)) {
            stmt.setString(1, numeroFatura);
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    public List<InventarioEquipamentos> findCriticos() throws SQLException {
        try (Connection conn = dbConnection.getConnection()) {
            return executeQuery(conn, SQL_FIND_CRITICOS);
        }
    }

    public List<InventarioEquipamentos> findVerificacaoAtrasada() throws SQLException {
        return findParaVerificacao(LocalDate.now().minusYears(1));
    }

    public List<InventarioEquipamentos> findParaVerificacao(LocalDate dataLimite) throws SQLException {
        if (dataLimite == null) return List.of();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_VERIFICACAO_LIMITE)) {
            stmt.setDate(1, Date.valueOf(dataLimite));
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    public List<InventarioEquipamentos> findSemMacAddress() throws SQLException {
        try (Connection conn = dbConnection.getConnection()) {
            return executeQuery(conn, SQL_FIND_SEM_MAC);
        }
    }

    public List<InventarioEquipamentos> findWithFilters(
            String skuProduto, String funcionarioId, String status, String condicao,
            String localizacao, String departamento,
            LocalDate dataInicio, LocalDate dataFim, String macAddress
    ) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM " + TABLE + " WHERE deleted = 0");
        List<Object> params = new ArrayList<>();

        if (skuProduto != null && !skuProduto.isBlank()) { sql.append(" AND sku_produto = ?"); params.add(skuProduto); }
        if (funcionarioId != null && !funcionarioId.isBlank()) { sql.append(" AND funcionario_id = ?"); params.add(funcionarioId); }
        if (status != null && !status.isBlank()) { sql.append(" AND status = ?"); params.add(status); }
        if (condicao != null && !condicao.isBlank()) { sql.append(" AND condicao = ?"); params.add(condicao); }
        if (localizacao != null && !localizacao.isBlank()) { sql.append(" AND localizacao = ?"); params.add(localizacao); }
        if (departamento != null && !departamento.isBlank()) { sql.append(" AND departamento = ?"); params.add(departamento); }
        if (dataInicio != null && dataFim != null) {
            sql.append(" AND data_aquisicao BETWEEN ? AND ?");
            params.add(Date.valueOf(dataInicio));
            params.add(Date.valueOf(dataFim));
        }
        if (macAddress != null && !macAddress.isBlank()) { sql.append(" AND endereco_mac LIKE ?"); params.add("%" + macAddress + "%"); }

        sql.append(" ORDER BY created_at DESC");

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) stmt.setObject(i + 1, params.get(i));
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    /**
     * Reassocia em massa todos os equipamentos que referenciam codDepAntigo
     * para o novo cod_dep. Usado na transferência de departamento.
     *
     * Recebe Connection para composição atômica (Service orquestra).
     *
     * @return número de linhas afetadas
     */
    public int reassociarFuncionario(Connection conn,
                                     String codDepAntigo,
                                     String codDepNovo) throws SQLException {
        if (codDepAntigo == null || codDepAntigo.isBlank()
                || codDepNovo == null || codDepNovo.isBlank()) {
            return 0;
        }
        String agora = agora();
        String sql = """
                UPDATE inventario_equipamentos
                SET funcionario_id = ?, updated_at = ?, device_id = ?
                WHERE funcionario_id = ? AND deleted = 0
                """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, codDepNovo);
            stmt.setString(2, agora);
            stmt.setString(3, deviceId());
            stmt.setString(4, codDepAntigo);
            int afetadas = stmt.executeUpdate();
            logger.log(System.Logger.Level.INFO,
                    "Reassociados {0} equipamento(s) de {1} → {2}",
                    afetadas, codDepAntigo, codDepNovo);
            return afetadas;
        }
    }

    // ============================================================
    // LEITURAS — OVERLOAD COM CONNECTION (usadas dentro de transação)
    // ============================================================

    public Optional<InventarioEquipamentos> findById(Connection conn, String id) throws SQLException {
        if (id == null || id.isBlank()) return Optional.empty();
        try (PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_ID)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapResultSet(rs)) : Optional.empty();
            }
        }
    }

    public boolean existsByNumSerie(Connection conn, String numSerie) throws SQLException {
        if (numSerie == null || numSerie.isBlank()) return false;
        return count(conn, "SELECT COUNT(*) FROM " + TABLE + " WHERE num_serie = ? AND deleted = 0", numSerie) > 0;
    }

    public boolean existsByMacAddress(Connection conn, String mac) throws SQLException {
        if (mac == null || mac.isBlank()) return false;
        return count(conn, "SELECT COUNT(*) FROM " + TABLE + " WHERE endereco_mac = ? AND deleted = 0", mac) > 0;
    }

    // ============================================================
    // MUTAÇÕES — Connection obrigatória (chamador fecha)
    // ============================================================

    public String insert(Connection conn, InventarioEquipamentos equip) throws SQLException {
        if (equip == null) throw new IllegalArgumentException("Equipamento inválido");
        if (conn == null) throw new IllegalArgumentException("Connection é obrigatória");

        String id = (equip.id() == null || equip.id().isBlank())
                ? UUID.randomUUID().toString()
                : equip.id();
        String agora = agora();
        String deviceId = (equip.deviceId() != null && !equip.deviceId().isBlank())
                ? equip.deviceId()
                : deviceId();

        try (PreparedStatement stmt = conn.prepareStatement(SQL_INSERT)) {
            stmt.setString(1, id);
            stmt.setString(2, equip.skuProduto());
            stmt.setString(3, equip.funcionarioId());
            stmt.setString(4, equip.numSerie());
            stmt.setString(5, equip.enderecoMac());
            setLocalDateOrNull(stmt, 6, equip.dataAquisicao());
            setLocalDateOrNull(stmt, 7, equip.dataInstalacao());
            setLocalDateOrNull(stmt, 8, equip.dataUltimaVerificacao());
            stmt.setString(9, equip.numeroFatura());
            stmt.setString(10, equip.localizacao());
            stmt.setString(11, equip.departamento());
            stmt.setString(12, equip.status());
            stmt.setString(13, equip.condicao());
            stmt.setBytes(14, equip.documentoEntrega());
            stmt.setBytes(15, equip.documentoDevolucao());
            stmt.setString(16, equip.observacoes());
            stmt.setBoolean(17, equip.devolucao() != null && equip.devolucao());
            stmt.setString(18, agora);
            stmt.setString(19, agora);
            stmt.setString(20, deviceId);
            stmt.executeUpdate();

            logger.log(System.Logger.Level.INFO, "✅ Equipamento inserido: ID={0}, Série={1}",
                    id, equip.numSerie());
            return id;
        }
    }

    public void update(Connection conn, InventarioEquipamentos equip) throws SQLException {
        if (equip == null) throw new IllegalArgumentException("Equipamento inválido");
        if (equip.id() == null || equip.id().isBlank()) {
            throw new SQLException("ID é obrigatório para atualização");
        }
        if (conn == null) throw new IllegalArgumentException("Connection é obrigatória");

        String agora = agora();
        String deviceId = deviceId();

        try (PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE)) {
            stmt.setString(1, equip.skuProduto());
            stmt.setString(2, equip.funcionarioId());
            stmt.setString(3, equip.numSerie());
            stmt.setString(4, equip.enderecoMac());
            setLocalDateOrNull(stmt, 5, equip.dataAquisicao());
            setLocalDateOrNull(stmt, 6, equip.dataInstalacao());
            setLocalDateOrNull(stmt, 7, equip.dataUltimaVerificacao());
            stmt.setString(8, equip.numeroFatura());
            stmt.setString(9, equip.localizacao());
            stmt.setString(10, equip.departamento());
            stmt.setString(11, equip.status());
            stmt.setString(12, equip.condicao());
            stmt.setBytes(13, equip.documentoEntrega());
            stmt.setBytes(14, equip.documentoDevolucao());
            stmt.setString(15, equip.observacoes());
            stmt.setBoolean(16, equip.devolucao() != null && equip.devolucao());
            stmt.setString(17, agora);
            stmt.setString(18, deviceId);
            stmt.setString(19, equip.id());

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Equipamento com ID " + equip.id() + " não encontrado");
            }
        }
    }

    public void updateStatus(Connection conn, String id, String status) throws SQLException {
        exec(conn, id, "status", stmt -> {
            stmt.setString(1, status);
            stmt.setString(2, agora());
            stmt.setString(3, deviceId());
            stmt.setString(4, id);
        }, SQL_UPDATE_STATUS);
    }

    public void updateCondicao(Connection conn, String id, String condicao) throws SQLException {
        exec(conn, id, "condicao", stmt -> {
            stmt.setString(1, condicao);
            stmt.setString(2, agora());
            stmt.setString(3, deviceId());
            stmt.setString(4, id);
        }, SQL_UPDATE_CONDICAO);
    }

    public void updateVerificacao(Connection conn, String id, LocalDate data, String condicao, String observacoesCompletas) throws SQLException {
        exec(conn, id, "verificacao", stmt -> {
            setLocalDateOrNull(stmt, 1, data);
            stmt.setString(2, condicao);
            stmt.setString(3, observacoesCompletas);
            stmt.setString(4, agora());
            stmt.setString(5, deviceId());
            stmt.setString(6, id);
        }, SQL_UPDATE_VERIFICACAO);
    }

    public void updateLocalizacao(Connection conn, String id, String localizacao, String departamento) throws SQLException {
        exec(conn, id, "localizacao", stmt -> {
            stmt.setString(1, localizacao);
            stmt.setString(2, departamento);
            stmt.setString(3, agora());
            stmt.setString(4, deviceId());
            stmt.setString(5, id);
        }, SQL_UPDATE_LOCALIZACAO);
    }

    public void updateFuncionario(Connection conn, String id, String funcionarioId) throws SQLException {
        exec(conn, id, "funcionário", stmt -> {
            stmt.setString(1, funcionarioId);
            stmt.setString(2, agora());
            stmt.setString(3, deviceId());
            stmt.setString(4, id);
        }, SQL_UPDATE_FUNCIONARIO);
    }

    public void updateMacAddress(Connection conn, String id, String mac) throws SQLException {
        exec(conn, id, "MAC", stmt -> {
            stmt.setString(1, mac);
            stmt.setString(2, agora());
            stmt.setString(3, deviceId());
            stmt.setString(4, id);
        }, SQL_UPDATE_MAC);
    }

    public void updateNumeroFatura(Connection conn, String id, String numeroFatura) throws SQLException {
        exec(conn, id, "fatura", stmt -> {
            stmt.setString(1, numeroFatura);
            stmt.setString(2, agora());
            stmt.setString(3, deviceId());
            stmt.setString(4, id);
        }, SQL_UPDATE_FATURA);
    }

    public void updateDocumentoEntrega(Connection conn, String id, byte[] documento) throws SQLException {
        exec(conn, id, "documento de entrega", stmt -> {
            stmt.setBytes(1, documento);
            stmt.setString(2, agora());
            stmt.setString(3, deviceId());
            stmt.setString(4, id);
        }, SQL_UPDATE_DOCUMENTO_ENTREGA);
    }

    public void updateDocumentoDevolucao(Connection conn, String id, byte[] documento) throws SQLException {
        exec(conn, id, "documento de devolução", stmt -> {
            stmt.setBytes(1, documento);
            stmt.setString(2, agora());
            stmt.setString(3, deviceId());
            stmt.setString(4, id);
        }, SQL_UPDATE_DOCUMENTO_DEVOLUCAO);
    }

    public void updateDevolucao(Connection conn, String id, boolean devolucao) throws SQLException {
        exec(conn, id, "devolução", stmt -> {
            stmt.setBoolean(1, devolucao);
            stmt.setString(2, agora());
            stmt.setString(3, deviceId());
            stmt.setString(4, id);
        }, SQL_UPDATE_DEVOLUCAO);
    }

    public void delete(Connection conn, String id) throws SQLException {
        exec(conn, id, "delete", stmt -> {
            stmt.setString(1, agora());
            stmt.setString(2, deviceId());
            stmt.setString(3, id);
        }, SQL_TOMBSTONE);
    }

    public void deletePermanent(Connection conn, String id) throws SQLException {
        if (conn == null) throw new IllegalArgumentException("Connection é obrigatória");
        try (PreparedStatement stmt = conn.prepareStatement(SQL_DELETE_PERMANENT)) {
            stmt.setString(1, id);
            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Equipamento com ID " + id + " não encontrado");
            }
        }
    }

    // ============================================================
    // VERIFICAÇÕES (leituras puras)
    // ============================================================

    public boolean existsById(String id) throws SQLException {
        if (id == null || id.isBlank()) return false;
        try (Connection conn = dbConnection.getConnection()) {
            return count(conn, "SELECT COUNT(*) FROM " + TABLE + " WHERE id = ? AND deleted = 0", id) > 0;
        }
    }

    public boolean existsByNumSerie(String numSerie) throws SQLException {
        if (numSerie == null || numSerie.isBlank()) return false;
        try (Connection conn = dbConnection.getConnection()) {
            return count(conn, "SELECT COUNT(*) FROM " + TABLE + " WHERE num_serie = ? AND deleted = 0", numSerie) > 0;
        }
    }

    public boolean existsByMacAddress(String mac) throws SQLException {
        if (mac == null || mac.isBlank()) return false;
        try (Connection conn = dbConnection.getConnection()) {
            return count(conn, "SELECT COUNT(*) FROM " + TABLE + " WHERE endereco_mac = ? AND deleted = 0", mac) > 0;
        }
    }

    // ============================================================
    // ESTATÍSTICAS
    // ============================================================

    public int countAll() throws SQLException {
        try (Connection conn = dbConnection.getConnection()) {
            return count(conn, "SELECT COUNT(*) FROM " + TABLE + " WHERE deleted = 0");
        }
    }

    public int countByStatus(String status) throws SQLException {
        try (Connection conn = dbConnection.getConnection()) {
            return count(conn, "SELECT COUNT(*) FROM " + TABLE + " WHERE status = ? AND deleted = 0", status);
        }
    }

    public int countByCondicao(String condicao) throws SQLException {
        try (Connection conn = dbConnection.getConnection()) {
            return count(conn, "SELECT COUNT(*) FROM " + TABLE + " WHERE condicao = ? AND deleted = 0", condicao);
        }
    }

    public Map<String, Integer> countByStatus() throws SQLException {
        try (Connection conn = dbConnection.getConnection()) {
            return countGroupBy(conn, """
                    SELECT status, COUNT(*) as total FROM %s
                    WHERE deleted = 0 GROUP BY status ORDER BY total DESC
                    """.formatted(TABLE));
        }
    }

    public Map<String, Integer> countByCondicao() throws SQLException {
        try (Connection conn = dbConnection.getConnection()) {
            return countGroupBy(conn, """
                    SELECT condicao, COUNT(*) as total FROM %s
                    WHERE deleted = 0 GROUP BY condicao ORDER BY total DESC
                    """.formatted(TABLE));
        }
    }

    public Map<String, Integer> countByDepartamento() throws SQLException {
        try (Connection conn = dbConnection.getConnection()) {
            return countGroupBy(conn, """
                    SELECT departamento, COUNT(*) as total FROM %s
                    WHERE departamento IS NOT NULL AND departamento != '' AND deleted = 0
                    GROUP BY departamento ORDER BY total DESC
                    """.formatted(TABLE));
        }
    }

    public Map<String, Integer> countByLocalizacao() throws SQLException {
        try (Connection conn = dbConnection.getConnection()) {
            return countGroupBy(conn, """
                    SELECT localizacao, COUNT(*) as total FROM %s
                    WHERE deleted = 0 GROUP BY localizacao ORDER BY total DESC
                    """.formatted(TABLE));
        }
    }

    public Map<Integer, Integer> countByAnoAquisicao() throws SQLException {
        String sql = """
                SELECT CAST(strftime('%%Y', data_aquisicao) AS INTEGER) as ano, COUNT(*) as total
                FROM %s
                WHERE data_aquisicao IS NOT NULL AND deleted = 0
                GROUP BY ano ORDER BY ano DESC
                """.formatted(TABLE);

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            Map<Integer, Integer> out = new LinkedHashMap<>();
            while (rs.next()) out.put(rs.getInt("ano"), rs.getInt("total"));
            return Map.copyOf(out);
        }
    }

    public Map<String, Integer> countByMacPrefix() throws SQLException {
        String sql = """
                SELECT substr(endereco_mac, 1, 8) as prefixo_mac, COUNT(*) as total
                FROM %s
                WHERE endereco_mac IS NOT NULL AND endereco_mac != '' AND deleted = 0
                GROUP BY prefixo_mac ORDER BY total DESC
                """.formatted(TABLE);
        try (Connection conn = dbConnection.getConnection()) {
            return countGroupBy(conn, sql);
        }
    }

    // ============================================================
    // DISTINCT
    // ============================================================

    public List<String> findLocalizacoesUnicas() throws SQLException {
        return findDistinct("localizacao");
    }

    public List<String> findDepartamentosUnicos() throws SQLException {
        return findDistinct("departamento");
    }

    // ============================================================
    // AUXILIARES
    // ============================================================

    @FunctionalInterface
    private interface Binder {
        void bind(PreparedStatement stmt) throws SQLException;
    }

    private void exec(Connection conn, String id, String rotulo, Binder binder, String sql) throws SQLException {
        if (conn == null) throw new IllegalArgumentException("Connection é obrigatória");
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            binder.bind(stmt);
            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Equipamento com ID " + id + " não encontrado (" + rotulo + ")");
            }
        }
    }

    /**
     * Null-safe para java.sql.Date: evita Date.valueOf(null), que dispara
     * "Cannot invoke LocalDate.getYear() because date is null".
     */
    private void setLocalDateOrNull(PreparedStatement stmt, int index, LocalDate valor)
            throws SQLException {
        if (valor != null) {
            stmt.setDate(index, Date.valueOf(valor));
        } else {
            stmt.setNull(index, java.sql.Types.DATE);
        }
    }

    private String agora() {
        return SyncTime.agora();
    }

    private String deviceId() {
        return deviceIdentity.getDeviceId();
    }

    private int count(Connection conn, String sql, Object... params) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) stmt.setObject(i + 1, params[i]);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private Map<String, Integer> countGroupBy(Connection conn, String sql) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            Map<String, Integer> out = new LinkedHashMap<>();
            while (rs.next()) out.put(rs.getString(1), rs.getInt(2));
            return Map.copyOf(out);
        }
    }

    private List<String> findDistinct(String column) throws SQLException {
        String sql = """
                SELECT DISTINCT %s FROM %s
                WHERE %s IS NOT NULL AND %s != '' AND deleted = 0
                ORDER BY %s
                """.formatted(column, TABLE, column, column, column);

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            List<String> out = new ArrayList<>();
            while (rs.next()) out.add(rs.getString(1));
            return List.copyOf(out);
        }
    }

    private List<InventarioEquipamentos> executeQuery(Connection conn, String sql) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            return mapResultSetList(rs);
        }
    }

    private List<InventarioEquipamentos> mapResultSetList(ResultSet rs) throws SQLException {
        List<InventarioEquipamentos> list = new ArrayList<>();
        while (rs.next()) list.add(mapResultSet(rs));
        return List.copyOf(list);
    }

    /** Cobre os 21 componentes do record v3.1. */
    private InventarioEquipamentos mapResultSet(ResultSet rs) throws SQLException {
        return new InventarioEquipamentos(
                rs.getString("id"),
                rs.getString("sku_produto"),
                rs.getString("funcionario_id"),
                rs.getString("num_serie"),
                rs.getString("endereco_mac"),
                DateUtils.parseDateTolerante(rs.getString("data_aquisicao")),
                DateUtils.parseDateTolerante(rs.getString("data_instalacao")),
                DateUtils.parseDateTolerante(rs.getString("data_ultima_verificacao")),
                rs.getString("numero_fatura"),
                rs.getString("localizacao"),
                rs.getString("departamento"),
                rs.getString("status"),
                rs.getString("condicao"),
                rs.getBytes("documento_entrega"),
                rs.getBytes("documento_devolucao"),
                rs.getString("observacoes"),
                getBooleanNullable(rs, "devolucao"),
                DateUtils.parseDateTimeTolerante(rs.getString("created_at")),
                DateUtils.parseDateTimeTolerante(rs.getString("updated_at")),
                rs.getString("device_id"),
                rs.getInt("deleted") != 0
        );
    }

    private Boolean getBooleanNullable(ResultSet rs, String column) throws SQLException {
        int v = rs.getInt(column);
        return rs.wasNull() ? null : v != 0;
    }
}