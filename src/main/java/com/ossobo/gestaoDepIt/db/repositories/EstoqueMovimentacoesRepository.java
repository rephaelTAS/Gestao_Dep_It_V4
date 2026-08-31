package com.ossobo.gestaoDepIt.db.repositories;

import com.ossobo.gestaoDepIt.db.config.DatabaseConnection;
import com.ossobo.gestaoDepIt.db.enums.TipoMovimentacao;
import com.ossobo.gestaoDepIt.db.models.EstoqueMovimentacoes;

import java.sql.Date;

import com.ossobo.winterfx.anotations.Inject;
import com.ossobo.winterfx.anotations.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * EstoqueMovimentacoesRepository - Acesso a dados com WinterFX
 * v2.0 - Migrado para Java 17+ com Records e Text Blocks
 *
 * Responsabilidades:
 * - CRUD para EstoqueMovimentacoes
 * - Filtros por SKU, tipo, período, funcionário
 * - Cálculo de saldo e estatísticas
 */
@Repository
public class EstoqueMovimentacoesRepository {

    private static final Logger logger = LoggerFactory.getLogger(EstoqueMovimentacoesRepository.class);

    @Inject
    private DatabaseConnection dbConnection;

    private static final String TABLE = "estoque_movimentacoes";

    // ===== SQL COM TEXT BLOCKS =====

    private static final String SQL_FIND_ALL = """
            SELECT * FROM %s
            ORDER BY data_movimentacao DESC, created_at DESC
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_ID = """
            SELECT * FROM %s
            WHERE id = ?
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_SKU = """
            SELECT * FROM %s
            WHERE sku_produto = ?
            ORDER BY data_movimentacao DESC, created_at DESC
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_TIPO = """
            SELECT * FROM %s
            WHERE tipo_movimentacao = ?
            ORDER BY data_movimentacao DESC
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_LOTE = """
            SELECT * FROM %s
            WHERE lote = ?
            ORDER BY data_movimentacao DESC
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_PERIODO = """
            SELECT * FROM %s
            WHERE data_movimentacao BETWEEN ? AND ?
            ORDER BY data_movimentacao, created_at
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_FUNCIONARIO = """
            SELECT * FROM %s
            WHERE cod_dep_funcionario = ?
            ORDER BY data_movimentacao DESC
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_LOCALIZACAO = """
            SELECT * FROM %s
            WHERE localizacao = ?
            ORDER BY data_movimentacao DESC
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_PROX_VENCIMENTO = """
            SELECT * FROM %s
            WHERE data_validade BETWEEN CURDATE() AND ?
            ORDER BY data_validade ASC
            """.formatted(TABLE);

    private static final String SQL_FIND_VENCIDAS = """
            SELECT * FROM %s
            WHERE data_validade IS NOT NULL AND data_validade < CURDATE()
            ORDER BY data_validade ASC
            """.formatted(TABLE);

    private static final String SQL_FIND_LICENCA_PROXIMA = """
            SELECT * FROM %s
            WHERE data_fim_licenca IS NOT NULL
            AND data_fim_licenca >= CURDATE()
            AND data_fim_licenca <= DATE_ADD(CURDATE(), INTERVAL ? DAY)
            ORDER BY data_fim_licenca ASC
            """.formatted(TABLE);

    private static final String SQL_INSERT = """
            INSERT INTO %s (
                sku_produto, tipo_movimentacao, quantidade, lote,
                data_movimentacao, data_validade, data_fim_licenca,
                localizacao, cod_dep_funcionario, motivo, observacoes
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.formatted(TABLE);

    private static final String SQL_UPDATE = """
            UPDATE %s
            SET sku_produto = ?, tipo_movimentacao = ?, quantidade = ?, lote = ?,
                data_movimentacao = ?, data_validade = ?, data_fim_licenca = ?,
                localizacao = ?, cod_dep_funcionario = ?, motivo = ?, observacoes = ?
            WHERE id = ?
            """.formatted(TABLE);

    private static final String SQL_DELETE = """
            DELETE FROM %s
            WHERE id = ?
            """.formatted(TABLE);

    private static final String SQL_CALCULAR_SALDO = """
            SELECT
                COALESCE(SUM(CASE WHEN tipo_movimentacao = 'ENTRADA' THEN quantidade ELSE 0 END), 0) -
                COALESCE(SUM(CASE WHEN tipo_movimentacao = 'SAIDA' THEN quantidade ELSE 0 END), 0) as saldo
            FROM %s
            WHERE sku_produto = ?
            """.formatted(TABLE);

    // ===== CRUD =====

    public List<EstoqueMovimentacoes> findAll() throws SQLException {
        return executeQuery(SQL_FIND_ALL);
    }

    public Optional<EstoqueMovimentacoes> findById(Long id) throws SQLException {
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

    public List<EstoqueMovimentacoes> findBySkuProduto(String sku) throws SQLException {
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

    public List<EstoqueMovimentacoes> findByTipo(TipoMovimentacao tipo) throws SQLException {
        if (tipo == null) {
            return List.of();
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_TIPO)) {

            stmt.setString(1, tipo.name());
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    public List<EstoqueMovimentacoes> findByLote(String lote) throws SQLException {
        if (!(lote instanceof String s) || s.isBlank()) {
            return List.of();
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_LOTE)) {

            stmt.setString(1, lote);
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    public List<EstoqueMovimentacoes> findByPeriodo(LocalDate inicio, LocalDate fim) throws SQLException {
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

    public List<EstoqueMovimentacoes> findByFuncionario(String codDep) throws SQLException {
        if (!(codDep instanceof String c) || c.isBlank()) {
            return List.of();
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_FUNCIONARIO)) {

            stmt.setString(1, codDep);
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    public List<EstoqueMovimentacoes> findByLocalizacao(String localizacao) throws SQLException {
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

    public List<EstoqueMovimentacoes> findProximosVencimento(LocalDate dataLimite) throws SQLException {
        if (dataLimite == null) {
            return List.of();
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_PROX_VENCIMENTO)) {

            stmt.setDate(1, Date.valueOf(dataLimite));
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    public List<EstoqueMovimentacoes> findVencidos() throws SQLException {
        return executeQuery(SQL_FIND_VENCIDAS);
    }

    public List<EstoqueMovimentacoes> findLicencaProximaVencimento(int dias) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_LICENCA_PROXIMA)) {

            stmt.setInt(1, dias);
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    public Long insert(EstoqueMovimentacoes mov) throws SQLException {
        if (!(mov instanceof EstoqueMovimentacoes m)) {
            throw new IllegalArgumentException("Movimentação inválida");
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_INSERT,
                     PreparedStatement.RETURN_GENERATED_KEYS)) {

            setParameters(stmt, m);
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
                throw new SQLException("Falha ao obter ID gerado");
            }
        }
    }

    public void update(EstoqueMovimentacoes mov) throws SQLException {
        if (!(mov instanceof EstoqueMovimentacoes m)) {
            throw new IllegalArgumentException("Movimentação inválida");
        }
        if (m.id() == null) {
            throw new SQLException("ID não pode ser nulo para atualização");
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE)) {

            setParameters(stmt, m);
            stmt.setLong(12, m.id());

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Movimentação com ID " + m.id() + " não encontrada");
            }
        }
    }

    public void delete(Long id) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_DELETE)) {

            stmt.setLong(1, id);
            int affected = stmt.executeUpdate();

            if (affected == 0) {
                throw new SQLException("Movimentação com ID " + id + " não encontrada");
            }
        }
    }

    /**
     * Salva (insert ou update) baseado na existência do ID
     */
    public Long save(EstoqueMovimentacoes mov) throws SQLException {
        if (mov.id() == null) {
            return insert(mov);
        } else {
            update(mov);
            return mov.id();
        }
    }

    // ===== CONSULTAS ANALÍTICAS =====

    public int calcularSaldo(String skuProduto) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_CALCULAR_SALDO)) {

            stmt.setString(1, skuProduto);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt("saldo") : 0;
            }
        }
    }

    public Map<LocalDate, Integer> getEvolucaoSaldo(String skuProduto, LocalDate inicio, LocalDate fim)
            throws SQLException {
        String sql = """
                SELECT data_movimentacao,
                    SUM(SUM(CASE
                        WHEN tipo_movimentacao = 'ENTRADA' THEN quantidade
                        WHEN tipo_movimentacao = 'SAIDA' THEN -quantidade
                        ELSE 0
                    END)) OVER (ORDER BY data_movimentacao) as saldo_acumulado
                FROM %s
                WHERE sku_produto = ? AND data_movimentacao BETWEEN ? AND ?
                GROUP BY data_movimentacao
                ORDER BY data_movimentacao
                """.formatted(TABLE);

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, skuProduto);
            stmt.setDate(2, Date.valueOf(inicio));
            stmt.setDate(3, Date.valueOf(fim));

            Map<LocalDate, Integer> evolucao = new TreeMap<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    evolucao.put(
                            rs.getDate("data_movimentacao").toLocalDate(),
                            rs.getInt("saldo_acumulado")
                    );
                }
            }
            return Map.copyOf(evolucao);
        }
    }

    public Map<String, Integer> countByTipo(LocalDate inicio, LocalDate fim) throws SQLException {
        String sql = """
                SELECT tipo_movimentacao, COUNT(*) as total
                FROM %s
                WHERE data_movimentacao BETWEEN ? AND ?
                GROUP BY tipo_movimentacao
                """.formatted(TABLE);

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(inicio));
            stmt.setDate(2, Date.valueOf(fim));

            Map<String, Integer> resultado = new HashMap<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultado.put(rs.getString("tipo_movimentacao"), rs.getInt("total"));
                }
            }
            return Map.copyOf(resultado);
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

    public boolean skuExistsInCatalogo(String sku) throws SQLException {
        String sql = "SELECT COUNT(*) FROM catalogo_produtos WHERE sku = ? AND ativo = 1";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, sku);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public boolean funcionarioExists(String codDep) throws SQLException {
        String sql = "SELECT COUNT(*) FROM funcionarios WHERE cod_dep = ? AND ativo = 1";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, codDep);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public int countAll() throws SQLException {
        String sql = "SELECT COUNT(*) FROM %s".formatted(TABLE);
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    // ===== MÉTODOS PRIVADOS =====

    private void setParameters(PreparedStatement stmt, EstoqueMovimentacoes m) throws SQLException {
        stmt.setString(1, m.skuProduto());
        stmt.setString(2, m.tipoMovimentacao().name());
        stmt.setInt(3, m.quantidade());
        stmt.setString(4, m.lote());
        stmt.setDate(5, Date.valueOf(m.dataMovimentacao()));
        stmt.setDate(6, m.dataValidade() != null ? Date.valueOf(m.dataValidade()) : null);
        stmt.setDate(7, m.dataFimLicenca() != null ? Date.valueOf(m.dataFimLicenca()) : null);
        stmt.setString(8, m.localizacao());
        stmt.setString(9, m.codDepFuncionario());
        stmt.setString(10, m.motivo());
        stmt.setString(11, m.observacoes());
    }

    private List<EstoqueMovimentacoes> executeQuery(String sql) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            return mapResultSetList(rs);
        }
    }

    private List<EstoqueMovimentacoes> mapResultSetList(ResultSet rs) throws SQLException {
        List<EstoqueMovimentacoes> list = new ArrayList<>();
        while (rs.next()) {
            list.add(mapResultSet(rs));
        }
        return List.copyOf(list);
    }

    private EstoqueMovimentacoes mapResultSet(ResultSet rs) throws SQLException {
        TipoMovimentacao tipo;
        try {
            tipo = TipoMovimentacao.valueOf(rs.getString("tipo_movimentacao"));
        } catch (IllegalArgumentException e) {
            logger.warn("Valor inválido de tipo_movimentacao: {}", rs.getString("tipo_movimentacao"));
            tipo = TipoMovimentacao.ENTRADA;
        }

        return new EstoqueMovimentacoes(
                rs.getLong("id"),
                rs.getString("sku_produto"),
                tipo,
                rs.getInt("quantidade"),
                rs.getString("lote"),
                rs.getDate("data_movimentacao").toLocalDate(),
                getLocalDate(rs, "data_validade"),
                getLocalDate(rs, "data_fim_licenca"),
                rs.getString("localizacao"),
                rs.getString("cod_dep_funcionario"),
                rs.getString("motivo"),
                rs.getString("observacoes"),
                getLocalDateTime(rs, "created_at")
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