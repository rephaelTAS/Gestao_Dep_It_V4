package com.ossobo.gestaoDepIt.db.repositories;

import com.ossobo.gestaoDepIt.db.config.DatabaseConnection;
import com.ossobo.gestaoDepIt.db.enums.TipoProduto;
import com.ossobo.gestaoDepIt.db.models.CatalogoProdutos;

import com.ossobo.winterfx.anotations.Inject;
import com.ossobo.winterfx.anotations.Repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * CatalogoProdutosRepository - Acesso a dados com suporte Dual Database
 * v2.3 - Repository completo com todos os campos e métodos
 *
 * Responsabilidades:
 * - CRUD completo para CatalogoProdutos
 * - Suporte a SQLite (local) e MySQL (remoto) via DatabaseConnection injetado
 * - Mapeamento ResultSet → CatalogoProdutos (Record com 18 campos)
 * - Métodos de filtro, estatísticas e consultas avançadas
 */
@Repository
public class CatalogoProdutosRepository {

    private static final Logger logger = LoggerFactory.getLogger(CatalogoProdutosRepository.class);

    @Inject
    private DatabaseConnection dbConnection;

    private static final String TABLE = "catalogo_produtos";

    // ============================================================
    // SQL COM TEXT BLOCKS (Java 15+)
    // ============================================================

    private static final String SQL_FIND_ALL = """
            SELECT * FROM %s
            WHERE ativo = 1
            ORDER BY marca, modelo
            """.formatted(TABLE);

    private static final String SQL_FIND_ALL_INACTIVE = """
            SELECT * FROM %s
            ORDER BY marca, modelo
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_SKU = """
            SELECT * FROM %s
            WHERE sku = ?
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_TIPO = """
            SELECT * FROM %s
            WHERE tipo_produto = ? AND ativo = 1
            ORDER BY marca, modelo
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_CATEGORIA = """
            SELECT * FROM %s
            WHERE categoria = ? AND ativo = 1
            ORDER BY marca, modelo
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_MARCA = """
            SELECT * FROM %s
            WHERE marca = ? AND ativo = 1
            ORDER BY modelo
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_MARCA_MODELO = """
            SELECT * FROM %s
            WHERE marca = ? AND modelo = ? AND ativo = 1
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_FORNECEDOR = """
            SELECT * FROM %s
            WHERE fornecedor = ? AND ativo = 1
            ORDER BY marca, modelo
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_FORNECEDOR_LIKE = """
            SELECT * FROM %s
            WHERE fornecedor LIKE ? AND ativo = 1
            ORDER BY fornecedor, marca
            """.formatted(TABLE);

    private static final String SQL_SEARCH = """
            SELECT * FROM %s
            WHERE ativo = 1 AND (
                sku LIKE ? OR categoria LIKE ? OR marca LIKE ? OR modelo LIKE ? OR 
                descricao LIKE ? OR fornecedor LIKE ?
            )
            ORDER BY marca, modelo
            """.formatted(TABLE);

    private static final String SQL_FIND_ESTOQUE_BAIXO = """
            SELECT * FROM %s
            WHERE ativo = 1 AND total_recebido <= ?
            ORDER BY total_recebido ASC
            """.formatted(TABLE);

    private static final String SQL_FIND_SEM_ESTOQUE = """
            SELECT * FROM %s
            WHERE ativo = 1 AND (total_recebido = 0 OR total_recebido IS NULL)
            ORDER BY marca, modelo
            """.formatted(TABLE);

    private static final String SQL_FIND_COM_ESTOQUE = """
            SELECT * FROM %s
            WHERE ativo = 1 AND total_recebido > 0
            ORDER BY total_recebido DESC
            """.formatted(TABLE);

    private static final String SQL_INSERT = """
            INSERT INTO %s (
                sku, tipo_produto, categoria, marca, modelo,
                cor, descricao, caracteristicas_tecnicas,
                preco_unitario, total_recebido, iva, preco_total,
                ativo, fornecedor, numero_fatura, fatura_compra
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.formatted(TABLE);

    private static final String SQL_UPDATE = """
            UPDATE %s
            SET tipo_produto = ?, categoria = ?, marca = ?, modelo = ?, cor = ?,
                descricao = ?, caracteristicas_tecnicas = ?, preco_unitario = ?,
                total_recebido = ?, iva = ?, preco_total = ?,
                ativo = ?, fornecedor = ?, numero_fatura = ?, fatura_compra = ?,
                updated_at = CURRENT_TIMESTAMP
            WHERE sku = ?
            """.formatted(TABLE);

    private static final String SQL_DELETE = """
            UPDATE %s
            SET ativo = 0, updated_at = CURRENT_TIMESTAMP
            WHERE sku = ?
            """.formatted(TABLE);

    private static final String SQL_ACTIVATE = """
            UPDATE %s
            SET ativo = 1, updated_at = CURRENT_TIMESTAMP
            WHERE sku = ?
            """.formatted(TABLE);

    private static final String SQL_UPDATE_STOCK = """
            UPDATE %s
            SET total_recebido = ?, updated_at = CURRENT_TIMESTAMP
            WHERE sku = ?
            """.formatted(TABLE);

    private static final String SQL_ADD_STOCK = """
            UPDATE %s
            SET total_recebido = total_recebido + ?, updated_at = CURRENT_TIMESTAMP
            WHERE sku = ?
            """.formatted(TABLE);

    private static final String SQL_REMOVE_STOCK = """
            UPDATE %s
            SET total_recebido = GREATEST(total_recebido - ?, 0), updated_at = CURRENT_TIMESTAMP
            WHERE sku = ?
            """.formatted(TABLE);

    private static final String SQL_UPDATE_PRECO = """
            UPDATE %s
            SET preco_unitario = ?, preco_total = ?, updated_at = CURRENT_TIMESTAMP
            WHERE sku = ?
            """.formatted(TABLE);

    private static final String SQL_UPDATE_FORNECEDOR = """
            UPDATE %s
            SET fornecedor = ?, updated_at = CURRENT_TIMESTAMP
            WHERE sku = ?
            """.formatted(TABLE);

    private static final String SQL_UPDATE_FATURA = """
            UPDATE %s
            SET numero_fatura = ?, fatura_compra = ?, updated_at = CURRENT_TIMESTAMP
            WHERE sku = ?
            """.formatted(TABLE);

    // ============================================================
    // CONSULTAS PARA DISTINCT VALUES
    // ============================================================

    private static final String SQL_DISTINCT_CATEGORIAS = """
            SELECT DISTINCT categoria FROM %s
            WHERE ativo = 1 AND categoria IS NOT NULL AND categoria != ''
            ORDER BY categoria
            """.formatted(TABLE);

    private static final String SQL_DISTINCT_MARCAS = """
            SELECT DISTINCT marca FROM %s
            WHERE ativo = 1 AND marca IS NOT NULL AND marca != ''
            ORDER BY marca
            """.formatted(TABLE);

    private static final String SQL_DISTINCT_CORES = """
            SELECT DISTINCT cor FROM %s
            WHERE ativo = 1 AND cor IS NOT NULL AND cor != ''
            ORDER BY cor
            """.formatted(TABLE);

    private static final String SQL_DISTINCT_TIPOS = """
            SELECT DISTINCT tipo_produto FROM %s
            WHERE ativo = 1 AND tipo_produto IS NOT NULL
            ORDER BY tipo_produto
            """.formatted(TABLE);

    private static final String SQL_DISTINCT_FORNECEDORES = """
            SELECT DISTINCT fornecedor FROM %s
            WHERE ativo = 1 AND fornecedor IS NOT NULL AND fornecedor != ''
            ORDER BY fornecedor
            """.formatted(TABLE);

    private static final String SQL_DISTINCT_MODELOS_BY_MARCA = """
            SELECT DISTINCT modelo FROM %s
            WHERE ativo = 1 AND marca = ? AND modelo IS NOT NULL
            ORDER BY modelo
            """.formatted(TABLE);

    // ============================================================
    // CONSULTAS ANALÍTICAS E ESTATÍSTICAS
    // ============================================================

    private static final String SQL_VALOR_TOTAL_ESTOQUE = """
            SELECT COALESCE(SUM(preco_unitario * total_recebido), 0) as valor_total
            FROM %s
            WHERE ativo = 1 AND preco_unitario IS NOT NULL AND total_recebido > 0
            """.formatted(TABLE);

    private static final String SQL_VALOR_TOTAL_ESTOQUE_COM_IVA = """
            SELECT COALESCE(SUM(preco_total * total_recebido), 0) as valor_total
            FROM %s
            WHERE ativo = 1 AND preco_total IS NOT NULL AND total_recebido > 0
            """.formatted(TABLE);

    private static final String SQL_COUNT_BY_TIPO = """
            SELECT tipo_produto, COUNT(*) as total
            FROM %s
            WHERE ativo = 1
            GROUP BY tipo_produto
            ORDER BY total DESC
            """.formatted(TABLE);

    private static final String SQL_COUNT_BY_CATEGORIA = """
            SELECT categoria, COUNT(*) as total
            FROM %s
            WHERE ativo = 1 AND categoria IS NOT NULL
            GROUP BY categoria
            ORDER BY total DESC
            LIMIT 20
            """.formatted(TABLE);

    private static final String SQL_COUNT_BY_MARCA = """
            SELECT marca, COUNT(*) as total
            FROM %s
            WHERE ativo = 1 AND marca IS NOT NULL
            GROUP BY marca
            ORDER BY total DESC
            LIMIT 20
            """.formatted(TABLE);

    private static final String SQL_COUNT_BY_FORNECEDOR = """
            SELECT fornecedor, COUNT(*) as total
            FROM %s
            WHERE ativo = 1 AND fornecedor IS NOT NULL AND fornecedor != ''
            GROUP BY fornecedor
            ORDER BY total DESC
            LIMIT 20
            """.formatted(TABLE);

    private static final String SQL_EXISTS_BY_SKU = """
            SELECT COUNT(*) FROM %s WHERE sku = ?
            """.formatted(TABLE);

    private static final String SQL_COUNT_ALL = """
            SELECT COUNT(*) FROM %s
            """.formatted(TABLE);

    private static final String SQL_COUNT_ACTIVE = """
            SELECT COUNT(*) FROM %s WHERE ativo = 1
            """.formatted(TABLE);

    // ============================================================
    // MÉTODOS CRUD
    // ============================================================

    public List<CatalogoProdutos> findAll() throws SQLException {
        return executeQuery(SQL_FIND_ALL);
    }

    public List<CatalogoProdutos> findAll(int limit, int offset) throws SQLException {
        String sql = SQL_FIND_ALL + " LIMIT ? OFFSET ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limit);
            stmt.setInt(2, offset);
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    public List<CatalogoProdutos> findAllIncludingInactive() throws SQLException {
        return executeQuery(SQL_FIND_ALL_INACTIVE);
    }

    public Optional<CatalogoProdutos> findBySku(String sku) throws SQLException {
        if (!(sku instanceof String s) || s.isBlank()) {
            return Optional.empty();
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_SKU)) {

            stmt.setString(1, sku);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapResultSet(rs)) : Optional.empty();
            }
        }
    }

    public String insert(CatalogoProdutos produto) throws SQLException {
        if (!(produto instanceof CatalogoProdutos p)) {
            throw new IllegalArgumentException("Produto inválido");
        }

        String tipoValidado = validateTipoProduto(String.valueOf(p.tipoProduto()));
        BigDecimal precoTotal = calcularPrecoTotal(p.precoUnitario(), p.iva());

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_INSERT)) {

            stmt.setString(1, p.sku());
            stmt.setString(2, tipoValidado);
            stmt.setString(3, p.categoria());
            stmt.setString(4, p.marca());
            stmt.setString(5, p.modelo());
            stmt.setString(6, p.cor());
            stmt.setString(7, p.descricao());
            stmt.setString(8, p.caracteristicasTecnicas());
            stmt.setBigDecimal(9, p.precoUnitario());
            stmt.setInt(10, p.totalRecebido() != null ? p.totalRecebido() : 0);
            stmt.setBigDecimal(11, p.iva());
            stmt.setBigDecimal(12, precoTotal);
            stmt.setBoolean(13, p.ativo() != null ? p.ativo() : true);
            stmt.setString(14, p.fornecedor());
            stmt.setString(15, p.numeroFatura());
            stmt.setBytes(16, p.faturaCompra());

            stmt.executeUpdate();
            logger.info("✅ Produto inserido: SKU={}", p.sku());
            return p.sku();
        }
    }

    public void update(CatalogoProdutos produto) throws SQLException {
        if (!(produto instanceof CatalogoProdutos p)) {
            throw new IllegalArgumentException("Produto inválido");
        }

        String tipoValidado = validateTipoProduto(String.valueOf(p.tipoProduto()));
        BigDecimal precoTotal = calcularPrecoTotal(p.precoUnitario(), p.iva());

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE)) {

            stmt.setString(1, tipoValidado);
            stmt.setString(2, p.categoria());
            stmt.setString(3, p.marca());
            stmt.setString(4, p.modelo());
            stmt.setString(5, p.cor());
            stmt.setString(6, p.descricao());
            stmt.setString(7, p.caracteristicasTecnicas());
            stmt.setBigDecimal(8, p.precoUnitario());
            stmt.setInt(9, p.totalRecebido() != null ? p.totalRecebido() : 0);
            stmt.setBigDecimal(10, p.iva());
            stmt.setBigDecimal(11, precoTotal);
            stmt.setBoolean(12, p.ativo() != null ? p.ativo() : true);
            stmt.setString(13, p.fornecedor());
            stmt.setString(14, p.numeroFatura());
            stmt.setBytes(15, p.faturaCompra());
            stmt.setString(16, p.sku());

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Produto com SKU " + p.sku() + " não encontrado");
            }
            logger.info("✅ Produto atualizado: SKU={}", p.sku());
        }
    }

    public String save(CatalogoProdutos produto) throws SQLException {
        if (existsBySku(produto.sku())) {
            update(produto);
            return produto.sku();
        } else {
            return insert(produto);
        }
    }

    public boolean softDelete(String sku) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_DELETE)) {

            stmt.setString(1, sku);
            boolean result = stmt.executeUpdate() > 0;
            if (result) {
                logger.info("✅ Produto desativado: SKU={}", sku);
            }
            return result;
        }
    }

    public boolean activate(String sku) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_ACTIVATE)) {

            stmt.setString(1, sku);
            boolean result = stmt.executeUpdate() > 0;
            if (result) {
                logger.info("✅ Produto ativado: SKU={}", sku);
            }
            return result;
        }
    }

    public boolean deletePermanent(String sku) throws SQLException {
        String sql = "DELETE FROM %s WHERE sku = ?".formatted(TABLE);
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, sku);
            boolean result = stmt.executeUpdate() > 0;
            if (result) {
                logger.warn("⚠️ Produto excluído permanentemente: SKU={}", sku);
            }
            return result;
        }
    }

    // ============================================================
    // GESTÃO DE ESTOQUE
    // ============================================================

    public boolean atualizarEstoque(String sku, int quantidade) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE_STOCK)) {

            stmt.setInt(1, quantidade);
            stmt.setString(2, sku);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean adicionarEstoque(String sku, int quantidade) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_ADD_STOCK)) {

            stmt.setInt(1, quantidade);
            stmt.setString(2, sku);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean removerEstoque(String sku, int quantidade) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_REMOVE_STOCK)) {

            stmt.setInt(1, quantidade);
            stmt.setString(2, sku);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean atualizarPreco(String sku, BigDecimal precoUnitario) throws SQLException {
        BigDecimal precoTotal = calcularPrecoTotal(precoUnitario, null);

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE_PRECO)) {

            stmt.setBigDecimal(1, precoUnitario);
            stmt.setBigDecimal(2, precoTotal);
            stmt.setString(3, sku);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean atualizarFornecedor(String sku, String fornecedor) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE_FORNECEDOR)) {

            stmt.setString(1, fornecedor);
            stmt.setString(2, sku);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean atualizarFatura(String sku, String numeroFatura, byte[] faturaCompra) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE_FATURA)) {

            stmt.setString(1, numeroFatura);
            stmt.setBytes(2, faturaCompra);
            stmt.setString(3, sku);
            return stmt.executeUpdate() > 0;
        }
    }

    // ============================================================
    // FILTROS
    // ============================================================

    public List<CatalogoProdutos> findByTipo(String tipo) throws SQLException {
        String tipoValidado = validateTipoProduto(tipo);
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_TIPO)) {

            stmt.setString(1, tipoValidado);
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    public List<CatalogoProdutos> findByCategoria(String categoria) throws SQLException {
        if (!(categoria instanceof String c) || c.isBlank()) {
            return List.of();
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_CATEGORIA)) {

            stmt.setString(1, categoria);
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    public List<CatalogoProdutos> findByMarca(String marca) throws SQLException {
        if (!(marca instanceof String m) || m.isBlank()) {
            return List.of();
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_MARCA)) {

            stmt.setString(1, marca);
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    public List<CatalogoProdutos> findByMarcaAndModelo(String marca, String modelo) throws SQLException {
        if (!(marca instanceof String m) || m.isBlank()) {
            return List.of();
        }
        if (!(modelo instanceof String mod) || mod.isBlank()) {
            return List.of();
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_MARCA_MODELO)) {

            stmt.setString(1, marca);
            stmt.setString(2, modelo);
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    public List<CatalogoProdutos> findByFornecedor(String fornecedor) throws SQLException {
        if (!(fornecedor instanceof String f) || f.isBlank()) {
            return List.of();
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_FORNECEDOR)) {

            stmt.setString(1, fornecedor);
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    public List<CatalogoProdutos> findByFornecedorLike(String pattern) throws SQLException {
        if (!(pattern instanceof String p) || p.isBlank()) {
            return List.of();
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_FORNECEDOR_LIKE)) {

            stmt.setString(1, "%" + pattern + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    public List<CatalogoProdutos> search(String termo) throws SQLException {
        if (!(termo instanceof String t) || t.isBlank()) {
            return List.of();
        }

        String termoLike = "%" + termo + "%";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_SEARCH)) {

            for (int i = 1; i <= 6; i++) {
                stmt.setString(i, termoLike);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    // ============================================================
    // CONSULTAS DE ESTOQUE
    // ============================================================

    public List<CatalogoProdutos> findComEstoqueBaixo(int limite) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_ESTOQUE_BAIXO)) {

            stmt.setInt(1, limite);
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    public List<CatalogoProdutos> findSemEstoque() throws SQLException {
        return executeQuery(SQL_FIND_SEM_ESTOQUE);
    }

    public List<CatalogoProdutos> findComEstoque() throws SQLException {
        return executeQuery(SQL_FIND_COM_ESTOQUE);
    }

    // ============================================================
    // DISTINCT VALUES (PARA COMBOBOX)
    // ============================================================

    public List<String> findDistinctCategorias() throws SQLException {
        return executeQueryString(SQL_DISTINCT_CATEGORIAS);
    }

    public List<String> findDistinctMarcas() throws SQLException {
        return executeQueryString(SQL_DISTINCT_MARCAS);
    }

    public List<String> findDistinctCores() throws SQLException {
        return executeQueryString(SQL_DISTINCT_CORES);
    }

    public List<String> findDistinctTipos() throws SQLException {
        return executeQueryString(SQL_DISTINCT_TIPOS);
    }

    public List<String> findDistinctFornecedores() throws SQLException {
        return executeQueryString(SQL_DISTINCT_FORNECEDORES);
    }

    public List<String> findDistinctModelos(String marca) throws SQLException {
        if (!(marca instanceof String m) || m.isBlank()) {
            return List.of();
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_DISTINCT_MODELOS_BY_MARCA)) {

            stmt.setString(1, marca);
            try (ResultSet rs = stmt.executeQuery()) {
                List<String> resultados = new ArrayList<>();
                while (rs.next()) {
                    resultados.add(rs.getString(1));
                }
                return List.copyOf(resultados);
            }
        }
    }

    // ============================================================
    // ESTATÍSTICAS
    // ============================================================

    public boolean existsBySku(String sku) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_EXISTS_BY_SKU)) {

            stmt.setString(1, sku);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public int countAll() throws SQLException {
        return count(SQL_COUNT_ALL);
    }

    public int countActive() throws SQLException {
        return count(SQL_COUNT_ACTIVE);
    }

    public int countByTipo(String tipo) throws SQLException {
        String tipoValidado = validateTipoProduto(tipo);
        String sql = "SELECT COUNT(*) FROM %s WHERE tipo_produto = ? AND ativo = 1".formatted(TABLE);

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, tipoValidado);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public int countByCategoria(String categoria) throws SQLException {
        String sql = "SELECT COUNT(*) FROM %s WHERE categoria = ? AND ativo = 1".formatted(TABLE);

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, categoria);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public int countByMarca(String marca) throws SQLException {
        String sql = "SELECT COUNT(*) FROM %s WHERE marca = ? AND ativo = 1".formatted(TABLE);

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, marca);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public BigDecimal getValorTotalEstoque() throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_VALOR_TOTAL_ESTOQUE);
             ResultSet rs = stmt.executeQuery()) {

            return rs.next() ? rs.getBigDecimal("valor_total") : BigDecimal.ZERO;
        }
    }

    public BigDecimal getValorTotalEstoqueComIva() throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_VALOR_TOTAL_ESTOQUE_COM_IVA);
             ResultSet rs = stmt.executeQuery()) {

            return rs.next() ? rs.getBigDecimal("valor_total") : BigDecimal.ZERO;
        }
    }

    public Map<String, Integer> countByTipo() throws SQLException {
        return countGroupBy(SQL_COUNT_BY_TIPO);
    }

    public Map<String, Integer> countByCategoria() throws SQLException {
        return countGroupBy(SQL_COUNT_BY_CATEGORIA);
    }

    public Map<String, Integer> countByMarca() throws SQLException {
        return countGroupBy(SQL_COUNT_BY_MARCA);
    }

    public Map<String, Integer> countByFornecedor() throws SQLException {
        return countGroupBy(SQL_COUNT_BY_FORNECEDOR);
    }

    // ============================================================
    // MÉTODOS PRIVADOS
    // ============================================================

    private int count(String sql) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private List<String> executeQueryString(String sql) throws SQLException {
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

    private List<CatalogoProdutos> executeQuery(String sql) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            return mapResultSetList(rs);
        }
    }

    private List<CatalogoProdutos> mapResultSetList(ResultSet rs) throws SQLException {
        List<CatalogoProdutos> list = new ArrayList<>();
        while (rs.next()) {
            list.add(mapResultSet(rs));
        }
        return List.copyOf(list);
    }

    private CatalogoProdutos mapResultSet(ResultSet rs) throws SQLException {
        if (!(rs instanceof ResultSet r)) {
            throw new SQLException("ResultSet inválido");
        }

        TipoProduto tipoEnum;
        try {
            tipoEnum = TipoProduto.fromString(r.getString("tipo_produto"));
        } catch (IllegalArgumentException e) {
            logger.warn("Valor inválido de tipo_produto: {}, usando EQUIPAMENTO como fallback",
                    r.getString("tipo_produto"));
            tipoEnum = TipoProduto.EQUIPAMENTO;
        }

        return new CatalogoProdutos(
                r.getString("sku"),                         // 1
                tipoEnum,                                   // 2
                r.getString("categoria"),                   // 3
                r.getString("marca"),                       // 4
                r.getString("modelo"),                      // 5
                r.getString("cor"),                         // 6
                r.getString("descricao"),                   // 7
                r.getString("caracteristicas_tecnicas"),    // 8
                r.getBigDecimal("preco_unitario"),          // 9
                r.getInt("total_recebido"),                 // 10
                r.getBigDecimal("iva"),                     // 11
                r.getBigDecimal("preco_total"),             // 12
                r.getBoolean("ativo"),                      // 13
                r.getString("fornecedor"),                  // 14
                r.getString("numero_fatura"),               // 15
                r.getBytes("fatura_compra"),                // 16
                getLocalDateTime(r, "created_at"),          // 17
                getLocalDateTime(r, "updated_at")           // 18
        );
    }

    private LocalDateTime getLocalDateTime(ResultSet rs, String column) throws SQLException {
        Timestamp ts = rs.getTimestamp(column);
        return ts != null ? ts.toLocalDateTime() : null;
    }

    private BigDecimal calcularPrecoTotal(BigDecimal precoUnitario, BigDecimal iva) {
        if (precoUnitario == null) {
            return BigDecimal.ZERO;
        }
        if (iva == null) {
            return precoUnitario;
        }
        return precoUnitario.add(precoUnitario.multiply(iva).divide(BigDecimal.valueOf(100)));
    }

    private String validateTipoProduto(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("Tipo de produto é obrigatório");
        }

        try {
            return TipoProduto.fromString(input).getDatabaseValue();
        } catch (IllegalArgumentException e) {
            String sugerido = suggestTipoProduto(input);
            if (sugerido != null) {
                logger.warn("Sugestão: use '{}' em vez de '{}'", sugerido, input);
                return sugerido;
            }
            throw new IllegalArgumentException("Tipo de produto inválido: " + input +
                    ". Use um dos: " + TipoProduto.getValidValues());
        }
    }

    private String suggestTipoProduto(String input) {
        String normalized = input.toUpperCase().trim();
        return switch (normalized) {
            case String s when s.contains("EQUIP") || s.contains("HARDWARE") -> "EQUIPAMENTO";
            case String s when s.contains("CONSUM") || s.contains("MATERIAL") -> "CONSUMIVEL";
            case String s when s.contains("TONER") || s.contains("CARTRIDGE") -> "TONER";
            case String s when s.contains("ACESS") || s.contains("PERIF") -> "ACESSORIO";
            case String s when s.contains("SOFT") || s.contains("LICENÇA") -> "SOFTWARE";
            case String s when s.contains("CABO") -> "CABO";
            case String s when s.contains("LICENCA") -> "LICENCA";
            default -> null;
        };
    }
}