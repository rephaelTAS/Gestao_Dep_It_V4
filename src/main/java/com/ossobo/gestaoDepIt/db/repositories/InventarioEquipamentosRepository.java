package com.ossobo.gestaoDepIt.db.repositories;

import com.ossobo.gestaoDepIt.db.config.DatabaseConnection;
import com.ossobo.gestaoDepIt.db.models.InventarioEquipamentos;

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
 * InventarioEquipamentosRepository - Acesso a dados com WinterFX
 * v2.1 - Adicionados campos: numero_fatura, documento_entrega, documento_devolucao, devolucao
 *
 * Responsabilidades:
 * - CRUD para InventarioEquipamentos
 * - Filtros por status, condição, localização, departamento
 * - Gestão de MAC address, documentos e fatura
 * - Estatísticas e relatórios
 */
@Repository
public class InventarioEquipamentosRepository {

    private static final Logger logger = LoggerFactory.getLogger(InventarioEquipamentosRepository.class);

    @Inject
    private DatabaseConnection dbConnection;

    private static final String TABLE = "inventario_equipamentos";

    // ============================================================
    // SQL COM TEXT BLOCKS
    // ============================================================

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

    private static final String SQL_FIND_BY_NUM_SERIE = """
            SELECT * FROM %s
            WHERE num_serie = ?
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_MAC = """
            SELECT * FROM %s
            WHERE endereco_mac = ?
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_MAC_LIKE = """
            SELECT * FROM %s
            WHERE endereco_mac LIKE ?
            ORDER BY endereco_mac
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_STATUS = """
            SELECT * FROM %s
            WHERE status = ?
            ORDER BY data_aquisicao DESC
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_CONDICAO = """
            SELECT * FROM %s
            WHERE condicao = ?
            ORDER BY data_ultima_verificacao DESC
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_LOCALIZACAO = """
            SELECT * FROM %s
            WHERE localizacao = ?
            ORDER BY departamento, num_serie
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_DEPARTAMENTO = """
            SELECT * FROM %s
            WHERE departamento = ?
            ORDER BY localizacao, num_serie
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_FUNCIONARIO = """
            SELECT * FROM %s
            WHERE funcionario_id = ?
            ORDER BY data_aquisicao DESC
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_SKU = """
            SELECT * FROM %s
            WHERE sku_produto = ?
            ORDER BY status, num_serie
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_PERIODO_AQUISICAO = """
            SELECT * FROM %s
            WHERE data_aquisicao BETWEEN ? AND ?
            ORDER BY data_aquisicao
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_NUMERO_FATURA = """
            SELECT * FROM %s
            WHERE numero_fatura = ?
            ORDER BY data_aquisicao DESC
            """.formatted(TABLE);

    private static final String SQL_FIND_CRITICOS = """
            SELECT * FROM %s
            WHERE condicao = 'CRITICO' AND status = 'ATIVO'
            ORDER BY data_ultima_verificacao
            """.formatted(TABLE);

    private static final String SQL_FIND_VERIFICACAO_ATRASADA = """
            SELECT * FROM %s
            WHERE (data_ultima_verificacao IS NULL
                   OR data_ultima_verificacao < DATE_SUB(CURDATE(), INTERVAL 1 YEAR))
            AND status = 'ATIVO'
            ORDER BY data_ultima_verificacao
            """.formatted(TABLE);

    private static final String SQL_FIND_VERIFICACAO_LIMITE = """
            SELECT * FROM %s
            WHERE (data_ultima_verificacao IS NULL OR data_ultima_verificacao < ?)
            AND status = 'ATIVO'
            ORDER BY data_ultima_verificacao
            """.formatted(TABLE);

    private static final String SQL_FIND_SEM_MAC = """
            SELECT * FROM %s
            WHERE (endereco_mac IS NULL OR endereco_mac = '')
            AND status = 'ATIVO'
            ORDER BY data_aquisicao
            """.formatted(TABLE);

    private static final String SQL_INSERT = """
            INSERT INTO %s (
                sku_produto, funcionario_id, num_serie, endereco_mac,
                data_aquisicao, data_instalacao, data_ultima_verificacao,
                numero_fatura, localizacao, departamento, status, condicao,
                documento_entrega, documento_devolucao, observacoes, devolucao
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.formatted(TABLE);

    private static final String SQL_UPDATE = """
            UPDATE %s
            SET sku_produto = ?, funcionario_id = ?, num_serie = ?, endereco_mac = ?,
                data_aquisicao = ?, data_instalacao = ?, data_ultima_verificacao = ?,
                numero_fatura = ?, localizacao = ?, departamento = ?, status = ?,
                condicao = ?, documento_entrega = ?, documento_devolucao = ?,
                observacoes = ?, devolucao = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """.formatted(TABLE);

    private static final String SQL_UPDATE_STATUS = """
            UPDATE %s
            SET status = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """.formatted(TABLE);

    private static final String SQL_UPDATE_CONDICAO = """
            UPDATE %s
            SET condicao = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """.formatted(TABLE);

    private static final String SQL_UPDATE_VERIFICACAO = """
            UPDATE %s
            SET data_ultima_verificacao = ?, condicao = ?,
                observacoes = CONCAT(IFNULL(observacoes, ''), ?),
                updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """.formatted(TABLE);

    private static final String SQL_UPDATE_LOCALIZACAO = """
            UPDATE %s
            SET localizacao = ?, departamento = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """.formatted(TABLE);

    private static final String SQL_UPDATE_FUNCIONARIO = """
            UPDATE %s
            SET funcionario_id = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """.formatted(TABLE);

    private static final String SQL_UPDATE_MAC = """
            UPDATE %s
            SET endereco_mac = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """.formatted(TABLE);

    private static final String SQL_UPDATE_FATURA = """
            UPDATE %s
            SET numero_fatura = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """.formatted(TABLE);

    private static final String SQL_UPDATE_DOCUMENTO_ENTREGA = """
            UPDATE %s
            SET documento_entrega = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """.formatted(TABLE);

    private static final String SQL_UPDATE_DOCUMENTO_DEVOLUCAO = """
            UPDATE %s
            SET documento_devolucao = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """.formatted(TABLE);

    private static final String SQL_UPDATE_DEVOLUCAO = """
            UPDATE %s
            SET devolucao = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """.formatted(TABLE);

    private static final String SQL_DELETE = """
            DELETE FROM %s
            WHERE id = ?
            """.formatted(TABLE);

    // ============================================================
    // CRUD
    // ============================================================

    public List<InventarioEquipamentos> findAll() throws SQLException {
        return executeQuery(SQL_FIND_ALL);
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
        if (id == null || Long.parseLong(id) <= 0) {
            return Optional.empty();
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_ID)) {

            stmt.setLong(1, Long.parseLong(id));
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapResultSet(rs)) : Optional.empty();
            }
        }
    }

    public Optional<InventarioEquipamentos> findByNumSerie(String numSerie) throws SQLException {
        if (!(numSerie instanceof String ns) || ns.isBlank()) {
            return Optional.empty();
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_NUM_SERIE)) {

            stmt.setString(1, numSerie);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapResultSet(rs)) : Optional.empty();
            }
        }
    }

    public Optional<InventarioEquipamentos> findByMacAddress(String mac) throws SQLException {
        if (!(mac instanceof String m) || m.isBlank()) {
            return Optional.empty();
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_MAC)) {

            stmt.setString(1, mac);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapResultSet(rs)) : Optional.empty();
            }
        }
    }

    public List<InventarioEquipamentos> findByMacAddressLike(String pattern) throws SQLException {
        if (!(pattern instanceof String p) || p.isBlank()) {
            return List.of();
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_MAC_LIKE)) {

            stmt.setString(1, "%" + pattern + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    public List<InventarioEquipamentos> findByStatus(String status) throws SQLException {
        if (!(status instanceof String s) || s.isBlank()) {
            return List.of();
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_STATUS)) {

            stmt.setString(1, status);
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    public List<InventarioEquipamentos> findByCondicao(String condicao) throws SQLException {
        if (!(condicao instanceof String c) || c.isBlank()) {
            return List.of();
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_CONDICAO)) {

            stmt.setString(1, condicao);
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    public List<InventarioEquipamentos> findByLocalizacao(String localizacao) throws SQLException {
        if (!(localizacao instanceof String l) || l.isBlank()) {
            return List.of();
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_LOCALIZACAO)) {

            stmt.setString(1, localizacao);
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    public List<InventarioEquipamentos> findByDepartamento(String departamento) throws SQLException {
        if (!(departamento instanceof String d) || d.isBlank()) {
            return List.of();
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_DEPARTAMENTO)) {

            stmt.setString(1, departamento);
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    public List<InventarioEquipamentos> findByFuncionarioId(String funcionarioId) throws SQLException {
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

    public List<InventarioEquipamentos> findBySkuProduto(String sku) throws SQLException {
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

    public List<InventarioEquipamentos> findByPeriodoAquisicao(LocalDate inicio, LocalDate fim) throws SQLException {
        if (inicio == null || fim == null) {
            return List.of();
        }

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
        if (!(numeroFatura instanceof String n) || n.isBlank()) {
            return List.of();
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_NUMERO_FATURA)) {

            stmt.setString(1, numeroFatura);
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    public List<InventarioEquipamentos> findCriticos() throws SQLException {
        return executeQuery(SQL_FIND_CRITICOS);
    }

    public List<InventarioEquipamentos> findVerificacaoAtrasada() throws SQLException {
        return executeQuery(SQL_FIND_VERIFICACAO_ATRASADA);
    }

    public List<InventarioEquipamentos> findParaVerificacao(LocalDate dataLimite) throws SQLException {
        if (dataLimite == null) {
            return List.of();
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_VERIFICACAO_LIMITE)) {

            stmt.setDate(1, Date.valueOf(dataLimite));
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    public List<InventarioEquipamentos> findSemMacAddress() throws SQLException {
        return executeQuery(SQL_FIND_SEM_MAC);
    }

    public String insert(InventarioEquipamentos equip) throws SQLException {
        if (!(equip instanceof InventarioEquipamentos e)) {
            throw new IllegalArgumentException("Equipamento inválido");
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_INSERT,
                     PreparedStatement.RETURN_GENERATED_KEYS)) {

            setInsertParameters(stmt, e);
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
                throw new SQLException("Falha ao obter ID gerado");
            }
        }
    }

    public void update(InventarioEquipamentos equip) throws SQLException {
        if (!(equip instanceof InventarioEquipamentos e)) {
            throw new IllegalArgumentException("Equipamento inválido");
        }
        if (e.id() == null) {
            throw new SQLException("ID não pode ser nulo para atualização");
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE)) {

            setUpdateParameters(stmt, e);
            int affected = stmt.executeUpdate();

            if (affected == 0) {
                throw new SQLException("Equipamento com ID " + e.id() + " não encontrado");
            }
        }
    }

    public void updateStatus(String id, String status) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE_STATUS)) {

            stmt.setString(1, status);
            stmt.setString(2, id);
            int affected = stmt.executeUpdate();

            if (affected == 0) {
                throw new SQLException("Equipamento com ID " + id + " não encontrado");
            }
        }
    }

    public void updateCondicao(String id, String condicao) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE_CONDICAO)) {

            stmt.setString(1, condicao);
            stmt.setString(2, id);
            int affected = stmt.executeUpdate();

            if (affected == 0) {
                throw new SQLException("Equipamento com ID " + id + " não encontrado");
            }
        }
    }

    public void updateVerificacao(String id, LocalDate data, String condicao, String observacoes) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE_VERIFICACAO)) {

            stmt.setDate(1, Date.valueOf(data));
            stmt.setString(2, condicao);
            stmt.setString(3, "\nVerificação: " + observacoes);
            stmt.setString(4, id);
            int affected = stmt.executeUpdate();

            if (affected == 0) {
                throw new SQLException("Equipamento com ID " + id + " não encontrado");
            }
        }
    }

    public void updateLocalizacao(String id, String localizacao, String departamento) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE_LOCALIZACAO)) {

            stmt.setString(1, localizacao);
            stmt.setString(2, departamento);
            stmt.setString(3, id);
            int affected = stmt.executeUpdate();

            if (affected == 0) {
                throw new SQLException("Equipamento com ID " + id + " não encontrado");
            }
        }
    }

    public void updateFuncionario(String id, String funcionarioId) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE_FUNCIONARIO)) {

            stmt.setString(1, funcionarioId);
            stmt.setString(2, id);
            int affected = stmt.executeUpdate();

            if (affected == 0) {
                throw new SQLException("Equipamento com ID " + id + " não encontrado");
            }
        }
    }

    public void updateMacAddress(String id, String mac) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE_MAC)) {

            stmt.setString(1, mac);
            stmt.setString(2, id);
            int affected = stmt.executeUpdate();

            if (affected == 0) {
                throw new SQLException("Equipamento com ID " + id + " não encontrado");
            }
        }
    }

    public void updateNumeroFatura(String id, String numeroFatura) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE_FATURA)) {

            stmt.setString(1, numeroFatura);
            stmt.setString(2, id);
            int affected = stmt.executeUpdate();

            if (affected == 0) {
                throw new SQLException("Equipamento com ID " + id + " não encontrado");
            }
        }
    }

    public void updateDocumentoEntrega(String id, byte[] documento) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE_DOCUMENTO_ENTREGA)) {

            stmt.setBytes(1, documento);
            stmt.setString(2, id);
            int affected = stmt.executeUpdate();

            if (affected == 0) {
                throw new SQLException("Equipamento com ID " + id + " não encontrado");
            }
        }
    }

    public void updateDocumentoDevolucao(String id, byte[] documento) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE_DOCUMENTO_DEVOLUCAO)) {

            stmt.setBytes(1, documento);
            stmt.setString(2, id);
            int affected = stmt.executeUpdate();

            if (affected == 0) {
                throw new SQLException("Equipamento com ID " + id + " não encontrado");
            }
        }
    }

    public void updateDevolucao(String id, boolean devolucao) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE_DEVOLUCAO)) {

            stmt.setBoolean(1, devolucao);
            stmt.setString(2, id);
            int affected = stmt.executeUpdate();

            if (affected == 0) {
                throw new SQLException("Equipamento com ID " + id + " não encontrado");
            }
        }
    }

    public void delete(String id) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_DELETE)) {

            stmt.setString(1, id);
            int affected = stmt.executeUpdate();

            if (affected == 0) {
                throw new SQLException("Equipamento com ID " + id + " não encontrado");
            }
        }
    }

    public String save(InventarioEquipamentos equip) throws SQLException {
        if (equip.id() == null) {
            return insert(equip);
        } else {
            update(equip);
            return equip.id();
        }
    }

    // ============================================================
    // FILTROS COMBINADOS
    // ============================================================

    public List<InventarioEquipamentos> findWithFilters(
            String skuProduto,
            String funcionarioId,
            String status,
            String condicao,
            String localizacao,
            String departamento,
            LocalDate dataInicio,
            LocalDate dataFim,
            String macAddress
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
        if (status != null && !status.isBlank()) {
            sql.append(" AND status = ?");
            params.add(status);
        }
        if (condicao != null && !condicao.isBlank()) {
            sql.append(" AND condicao = ?");
            params.add(condicao);
        }
        if (localizacao != null && !localizacao.isBlank()) {
            sql.append(" AND localizacao = ?");
            params.add(localizacao);
        }
        if (departamento != null && !departamento.isBlank()) {
            sql.append(" AND departamento = ?");
            params.add(departamento);
        }
        if (dataInicio != null && dataFim != null) {
            sql.append(" AND data_aquisicao BETWEEN ? AND ?");
            params.add(dataInicio);
            params.add(dataFim);
        }
        if (macAddress != null && !macAddress.isBlank()) {
            sql.append(" AND endereco_mac LIKE ?");
            params.add("%" + macAddress + "%");
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

    // ============================================================
    // VERIFICAÇÕES
    // ============================================================

    public boolean existsById(String id) throws SQLException {
        if (id == null) return false;
        String sql = "SELECT COUNT(*) FROM %s WHERE id = ?".formatted(TABLE);

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public boolean existsByNumSerie(String numSerie) throws SQLException {
        if (numSerie == null || numSerie.isBlank()) return false;
        String sql = "SELECT COUNT(*) FROM %s WHERE num_serie = ?".formatted(TABLE);

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, numSerie);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public boolean existsByMacAddress(String mac) throws SQLException {
        if (mac == null || mac.isBlank()) return false;
        String sql = "SELECT COUNT(*) FROM %s WHERE endereco_mac = ?".formatted(TABLE);

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, mac);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    // ============================================================
    // ESTATÍSTICAS
    // ============================================================

    public int countAll() throws SQLException {
        return count("SELECT COUNT(*) FROM " + TABLE);
    }

    public int countByStatus(String status) throws SQLException {
        return count("SELECT COUNT(*) FROM " + TABLE + " WHERE status = ?", status);
    }

    public int countByCondicao(String condicao) throws SQLException {
        return count("SELECT COUNT(*) FROM " + TABLE + " WHERE condicao = ?", condicao);
    }

    public Map<String, Integer> countByStatus() throws SQLException {
        String sql = """
                SELECT status, COUNT(*) as total
                FROM %s
                GROUP BY status
                ORDER BY total DESC
                """.formatted(TABLE);

        return countGroupBy(sql);
    }

    public Map<String, Integer> countByCondicao() throws SQLException {
        String sql = """
                SELECT condicao, COUNT(*) as total
                FROM %s
                GROUP BY condicao
                ORDER BY total DESC
                """.formatted(TABLE);

        return countGroupBy(sql);
    }

    public Map<String, Integer> countByDepartamento() throws SQLException {
        String sql = """
                SELECT departamento, COUNT(*) as total
                FROM %s
                WHERE departamento IS NOT NULL AND departamento != ''
                GROUP BY departamento
                ORDER BY total DESC
                """.formatted(TABLE);

        return countGroupBy(sql);
    }

    public Map<String, Integer> countByLocalizacao() throws SQLException {
        String sql = """
                SELECT localizacao, COUNT(*) as total
                FROM %s
                GROUP BY localizacao
                ORDER BY total DESC
                """.formatted(TABLE);

        return countGroupBy(sql);
    }

    public Map<Integer, Integer> countByAnoAquisicao() throws SQLException {
        String sql = """
                SELECT YEAR(data_aquisicao) as ano, COUNT(*) as total
                FROM %s
                GROUP BY YEAR(data_aquisicao)
                ORDER BY ano DESC
                """.formatted(TABLE);

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            Map<Integer, Integer> resultado = new LinkedHashMap<>();
            while (rs.next()) {
                resultado.put(rs.getInt("ano"), rs.getInt("total"));
            }
            return Map.copyOf(resultado);
        }
    }

    public Map<String, Integer> countByMacPrefix() throws SQLException {
        String sql = """
                SELECT SUBSTRING(endereco_mac, 1, 8) as prefixo_mac,
                       COUNT(*) as total
                FROM %s
                WHERE endereco_mac IS NOT NULL AND endereco_mac != ''
                GROUP BY SUBSTRING(endereco_mac, 1, 8)
                ORDER BY total DESC
                """.formatted(TABLE);

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            Map<String, Integer> resultado = new LinkedHashMap<>();
            while (rs.next()) {
                resultado.put(rs.getString("prefixo_mac"), rs.getInt("total"));
            }
            return Map.copyOf(resultado);
        }
    }

    // ============================================================
    // DISTINCT VALUES
    // ============================================================

    public List<String> findLocalizacoesUnicas() throws SQLException {
        return findDistinct("localizacao");
    }

    public List<String> findDepartamentosUnicos() throws SQLException {
        return findDistinct("departamento");
    }

    // ============================================================
    // MÉTODOS PRIVADOS
    // ============================================================

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

    private Map<String, Integer> countGroupBy(String sql) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            Map<String, Integer> resultado = new LinkedHashMap<>();
            while (rs.next()) {
                resultado.put(rs.getString(1), rs.getInt(2));
            }
            return Map.copyOf(resultado);
        }
    }

    private List<String> findDistinct(String column) throws SQLException {
        String sql = """
                SELECT DISTINCT %s FROM %s
                WHERE %s IS NOT NULL AND %s != ''
                ORDER BY %s
                """.formatted(column, TABLE, column, column, column);

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            List<String> resultados = new ArrayList<>();
            while (rs.next()) {
                resultados.add(rs.getString(1));
            }
            return List.copyOf(resultados);
        }
    }

    private List<InventarioEquipamentos> executeQuery(String sql) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            return mapResultSetList(rs);
        }
    }

    private List<InventarioEquipamentos> mapResultSetList(ResultSet rs) throws SQLException {
        List<InventarioEquipamentos> list = new ArrayList<>();
        while (rs.next()) {
            list.add(mapResultSet(rs));
        }
        return List.copyOf(list);
    }

    private InventarioEquipamentos mapResultSet(ResultSet rs) throws SQLException {
        return new InventarioEquipamentos(
                rs.getString("id"),                           // 1
                rs.getString("sku_produto"),                // 2
                rs.getString("funcionario_id"),             // 3
                rs.getString("num_serie"),                  // 4
                rs.getString("endereco_mac"),               // 5
                getLocalDate(rs, "data_aquisicao"),         // 6
                getLocalDate(rs, "data_instalacao"),        // 7
                getLocalDate(rs, "data_ultima_verificacao"),// 8
                rs.getString("numero_fatura"),              // 9 ✅ NOVO
                rs.getString("localizacao"),                // 10
                rs.getString("departamento"),               // 11
                rs.getString("status"),                     // 12
                rs.getString("condicao"),                   // 13
                rs.getBytes("documento_entrega"),           // 14 ✅ NOVO
                rs.getBytes("documento_devolucao"),         // 15 ✅ NOVO
                rs.getString("observacoes"),                // 16
                rs.getBoolean("devolucao"),                 // 17 ✅ NOVO
                getLocalDateTime(rs, "created_at"),         // 18
                getLocalDateTime(rs, "updated_at")          // 19
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

    private void setInsertParameters(PreparedStatement stmt, InventarioEquipamentos e) throws SQLException {
        stmt.setString(1, e.skuProduto());
        stmt.setString(2, e.funcionarioId());
        stmt.setString(3, e.numSerie());
        stmt.setString(4, e.enderecoMac());
        stmt.setDate(5, Date.valueOf(e.dataAquisicao()));
        stmt.setDate(6, e.dataInstalacao() != null ? Date.valueOf(e.dataInstalacao()) : null);
        stmt.setDate(7, e.dataUltimaVerificacao() != null ? Date.valueOf(e.dataUltimaVerificacao()) : null);
        stmt.setString(8, e.numeroFatura());
        stmt.setString(9, e.localizacao());
        stmt.setString(10, e.departamento());
        stmt.setString(11, e.status());
        stmt.setString(12, e.condicao());
        stmt.setBytes(13, e.documentoEntrega());
        stmt.setBytes(14, e.documentoDevolucao());
        stmt.setString(15, e.observacoes());
        stmt.setBoolean(16, e.devolucao() != null && e.devolucao());
    }

    private void setUpdateParameters(PreparedStatement stmt, InventarioEquipamentos e) throws SQLException {
        stmt.setString(1, e.skuProduto());
        stmt.setString(2, e.funcionarioId());
        stmt.setString(3, e.numSerie());
        stmt.setString(4, e.enderecoMac());
        stmt.setDate(5, Date.valueOf(e.dataAquisicao()));
        stmt.setDate(6, e.dataInstalacao() != null ? Date.valueOf(e.dataInstalacao()) : null);
        stmt.setDate(7, e.dataUltimaVerificacao() != null ? Date.valueOf(e.dataUltimaVerificacao()) : null);
        stmt.setString(8, e.numeroFatura());
        stmt.setString(9, e.localizacao());
        stmt.setString(10, e.departamento());
        stmt.setString(11, e.status());
        stmt.setString(12, e.condicao());
        stmt.setBytes(13, e.documentoEntrega());
        stmt.setBytes(14, e.documentoDevolucao());
        stmt.setString(15, e.observacoes());
        stmt.setBoolean(16, e.devolucao() != null && e.devolucao());
        stmt.setString(17, e.id());
    }
}