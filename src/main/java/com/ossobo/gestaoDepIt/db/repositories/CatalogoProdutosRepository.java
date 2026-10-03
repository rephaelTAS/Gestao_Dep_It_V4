package com.ossobo.gestaoDepIt.db.repositories;

import com.ossobo.gestaoDepIt.db.config.DatabaseConnection;
import com.ossobo.gestaoDepIt.db.enums.TipoProduto;
import com.ossobo.gestaoDepIt.db.models.CatalogoProdutos;
import com.ossobo.gestaoDepIt.db.sync.DeviceIdentity;
import com.ossobo.winterfx.anotations.Inject;
import com.ossobo.winterfx.anotations.Repository;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * CatalogoProdutosRepository v2.6 - Acesso a dados (model v3.0)
 *
 * v2.6 — Regra de negócio "número de fatura único" (fornecedor é informativo):
 *        - existePorFatura(numeroFatura)
 *        - existePorFaturaExcetoId(numeroFatura, idExceto)
 *        - SQL_EXISTS_FATURA usa só numero_fatura
 *
 * v2.5 — Tentativa inicial com (fornecedor, numero) — substituída em v2.6.
 * v2.4 — Alinhado ao CatalogoProdutos v3.0 (model = fonte da verdade).
 *        Parser de data TOLERANTE (SQLite "yyyy-MM-dd HH:mm:ss" e ISO com 'T').
 */
@Repository
public class CatalogoProdutosRepository {

    private static final System.Logger logger = System.getLogger(CatalogoProdutosRepository.class.getName());

    private static final DateTimeFormatter FMT_SQLITE =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Inject
    private DatabaseConnection dbConnection;

    @Inject
    private DeviceIdentity deviceIdentity;

    private static final String TABLE = "catalogo_produtos";

    // ============================================================
    // SQL
    // ============================================================

    private static final String SQL_FIND_ALL = """
            SELECT * FROM %s
            WHERE ativo = 1 AND deleted = 0
            ORDER BY marca, modelo
            """.formatted(TABLE);

    private static final String SQL_FIND_ALL_INACTIVE = """
            SELECT * FROM %s
            WHERE deleted = 0
            ORDER BY marca, modelo
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_ID = """
            SELECT * FROM %s WHERE id = ? AND deleted = 0
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_SKU = """
            SELECT * FROM %s WHERE sku = ? AND deleted = 0
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_TIPO = """
            SELECT * FROM %s
            WHERE tipo_produto = ? AND ativo = 1 AND deleted = 0
            ORDER BY marca, modelo
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_CATEGORIA = """
            SELECT * FROM %s
            WHERE categoria = ? AND ativo = 1 AND deleted = 0
            ORDER BY marca, modelo
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_MARCA = """
            SELECT * FROM %s
            WHERE marca = ? AND ativo = 1 AND deleted = 0
            ORDER BY modelo
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_MARCA_MODELO = """
            SELECT * FROM %s
            WHERE marca = ? AND modelo = ? AND ativo = 1 AND deleted = 0
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_FORNECEDOR = """
            SELECT * FROM %s
            WHERE fornecedor = ? AND ativo = 1 AND deleted = 0
            ORDER BY marca, modelo
            """.formatted(TABLE);

    private static final String SQL_SEARCH = """
            SELECT * FROM %s
            WHERE ativo = 1 AND deleted = 0 AND (
                sku LIKE ? OR categoria LIKE ? OR marca LIKE ? OR modelo LIKE ? OR
                descricao LIKE ? OR fornecedor LIKE ?
            )
            ORDER BY marca, modelo
            """.formatted(TABLE);

    private static final String SQL_FIND_ESTOQUE_BAIXO = """
            SELECT * FROM %s
            WHERE ativo = 1 AND deleted = 0 AND total_recebido <= ?
            ORDER BY total_recebido ASC
            """.formatted(TABLE);

    private static final String SQL_FIND_SEM_ESTOQUE = """
            SELECT * FROM %s
            WHERE ativo = 1 AND deleted = 0 AND (total_recebido = 0 OR total_recebido IS NULL)
            ORDER BY marca, modelo
            """.formatted(TABLE);

    private static final String SQL_FIND_COM_ESTOQUE = """
            SELECT * FROM %s
            WHERE ativo = 1 AND deleted = 0 AND total_recebido > 0
            ORDER BY total_recebido DESC
            """.formatted(TABLE);

    private static final String SQL_INSERT = """
            INSERT INTO %s (
                id, sku, tipo_produto, categoria, marca, modelo,
                cor, descricao, caracteristicas_tecnicas,
                total_recebido, preco_unitario_centavos, iva_basis_points, preco_total_centavos,
                ativo, fornecedor, numero_fatura, fatura_compra,
                created_at, updated_at, device_id, deleted
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)
            """.formatted(TABLE);

    private static final String SQL_UPDATE = """
            UPDATE %s
            SET sku = ?, tipo_produto = ?, categoria = ?, marca = ?, modelo = ?, cor = ?,
                descricao = ?, caracteristicas_tecnicas = ?,
                total_recebido = ?, preco_unitario_centavos = ?, iva_basis_points = ?, preco_total_centavos = ?,
                ativo = ?, fornecedor = ?, numero_fatura = ?, fatura_compra = ?,
                updated_at = ?, device_id = ?
            WHERE id = ? AND deleted = 0
            """.formatted(TABLE);

    private static final String SQL_TOMBSTONE = """
            UPDATE %s
            SET deleted = 1, ativo = 0, updated_at = ?, device_id = ?
            WHERE id = ? AND deleted = 0
            """.formatted(TABLE);

    private static final String SQL_ACTIVATE = """
            UPDATE %s
            SET ativo = 1, updated_at = ?, device_id = ?
            WHERE id = ? AND deleted = 0
            """.formatted(TABLE);

    private static final String SQL_UPDATE_STOCK = """
            UPDATE %s
            SET total_recebido = ?, updated_at = ?, device_id = ?
            WHERE id = ? AND deleted = 0
            """.formatted(TABLE);

    private static final String SQL_ADD_STOCK = """
            UPDATE %s
            SET total_recebido = COALESCE(total_recebido, 0) + ?, updated_at = ?, device_id = ?
            WHERE id = ? AND deleted = 0
            """.formatted(TABLE);

    private static final String SQL_REMOVE_STOCK = """
            UPDATE %s
            SET total_recebido = MAX(COALESCE(total_recebido, 0) - ?, 0), updated_at = ?, device_id = ?
            WHERE id = ? AND deleted = 0
            """.formatted(TABLE);

    private static final String SQL_UPDATE_PRECO = """
            UPDATE %s
            SET preco_unitario_centavos = ?, iva_basis_points = ?, preco_total_centavos = ?,
                updated_at = ?, device_id = ?
            WHERE id = ? AND deleted = 0
            """.formatted(TABLE);

    private static final String SQL_UPDATE_FORNECEDOR = """
            UPDATE %s
            SET fornecedor = ?, updated_at = ?, device_id = ?
            WHERE id = ? AND deleted = 0
            """.formatted(TABLE);

    private static final String SQL_UPDATE_FATURA = """
            UPDATE %s
            SET numero_fatura = ?, fatura_compra = ?, updated_at = ?, device_id = ?
            WHERE id = ? AND deleted = 0
            """.formatted(TABLE);

    private static final String SQL_DELETE_PERMANENT = """
            DELETE FROM %s WHERE id = ? AND deleted = 0
            """.formatted(TABLE);

    /**
     * Verifica existência de fatura (só número — fornecedor é informativo).
     * Fatura é documento único: pode ter N produtos do mesmo fornecedor,
     * mas o MESMO número não pode ser cadastrado duas vezes.
     */
    private static final String SQL_EXISTS_FATURA = """
            SELECT COUNT(*) FROM %s
            WHERE numero_fatura = ? AND deleted = 0
            """.formatted(TABLE);

    private static final String SQL_EXISTS_FATURA_EXCETO_ID = """
            SELECT COUNT(*) FROM %s
            WHERE numero_fatura = ? AND id != ? AND deleted = 0
            """.formatted(TABLE);

    private static final String SQL_DISTINCT_CATEGORIAS = """
            SELECT DISTINCT categoria FROM %s
            WHERE ativo = 1 AND deleted = 0 AND categoria IS NOT NULL AND categoria != ''
            ORDER BY categoria
            """.formatted(TABLE);

    private static final String SQL_DISTINCT_MARCAS = """
            SELECT DISTINCT marca FROM %s
            WHERE ativo = 1 AND deleted = 0 AND marca IS NOT NULL AND marca != ''
            ORDER BY marca
            """.formatted(TABLE);

    private static final String SQL_DISTINCT_CORES = """
            SELECT DISTINCT cor FROM %s
            WHERE ativo = 1 AND deleted = 0 AND cor IS NOT NULL AND cor != ''
            ORDER BY cor
            """.formatted(TABLE);

    private static final String SQL_DISTINCT_TIPOS = """
            SELECT DISTINCT tipo_produto FROM %s
            WHERE ativo = 1 AND deleted = 0 AND tipo_produto IS NOT NULL
            ORDER BY tipo_produto
            """.formatted(TABLE);

    private static final String SQL_DISTINCT_FORNECEDORES = """
            SELECT DISTINCT fornecedor FROM %s
            WHERE ativo = 1 AND deleted = 0 AND fornecedor IS NOT NULL AND fornecedor != ''
            ORDER BY fornecedor
            """.formatted(TABLE);

    private static final String SQL_DISTINCT_MODELOS_BY_MARCA = """
            SELECT DISTINCT modelo FROM %s
            WHERE ativo = 1 AND deleted = 0 AND marca = ? AND modelo IS NOT NULL
            ORDER BY modelo
            """.formatted(TABLE);

    private static final String SQL_VALOR_TOTAL_ESTOQUE = """
            SELECT COALESCE(SUM(preco_unitario_centavos * total_recebido), 0) as valor_total
            FROM %s
            WHERE ativo = 1 AND deleted = 0 AND preco_unitario_centavos > 0 AND total_recebido > 0
            """.formatted(TABLE);

    private static final String SQL_VALOR_TOTAL_ESTOQUE_COM_IVA = """
            SELECT COALESCE(SUM(preco_total_centavos * total_recebido), 0) as valor_total
            FROM %s
            WHERE ativo = 1 AND deleted = 0 AND preco_total_centavos > 0 AND total_recebido > 0
            """.formatted(TABLE);

    private static final String SQL_COUNT_BY_TIPO = """
            SELECT tipo_produto, COUNT(*) as total FROM %s
            WHERE ativo = 1 AND deleted = 0
            GROUP BY tipo_produto ORDER BY total DESC
            """.formatted(TABLE);

    private static final String SQL_COUNT_BY_CATEGORIA = """
            SELECT categoria, COUNT(*) as total FROM %s
            WHERE ativo = 1 AND deleted = 0 AND categoria IS NOT NULL
            GROUP BY categoria ORDER BY total DESC LIMIT 20
            """.formatted(TABLE);

    private static final String SQL_COUNT_BY_MARCA = """
            SELECT marca, COUNT(*) as total FROM %s
            WHERE ativo = 1 AND deleted = 0 AND marca IS NOT NULL
            GROUP BY marca ORDER BY total DESC LIMIT 20
            """.formatted(TABLE);

    private static final String SQL_COUNT_BY_FORNECEDOR = """
            SELECT fornecedor, COUNT(*) as total FROM %s
            WHERE ativo = 1 AND deleted = 0 AND fornecedor IS NOT NULL AND fornecedor != ''
            GROUP BY fornecedor ORDER BY total DESC LIMIT 20
            """.formatted(TABLE);

    // ============================================================
    // CRUD
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

    public Optional<CatalogoProdutos> findById(String id) throws SQLException {
        if (id == null || id.isBlank()) return Optional.empty();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_ID)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapResultSet(rs)) : Optional.empty();
            }
        }
    }

    public Optional<CatalogoProdutos> findBySku(String sku) throws SQLException {
        if (sku == null || sku.isBlank()) return Optional.empty();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_SKU)) {
            stmt.setString(1, sku);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapResultSet(rs)) : Optional.empty();
            }
        }
    }

    /**
     * Insere. Gera UUID se id ausente; carimba created_at/updated_at/device_id.
     * @return id (UUID) do produto gravado.
     */
    public String insert(CatalogoProdutos produto) throws SQLException {
        if (produto == null) throw new IllegalArgumentException("Produto inválido");

        String id = (produto.id() == null || produto.id().isBlank())
                ? UUID.randomUUID().toString()
                : produto.id();
        String agora = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS).toString();
        String deviceId = (produto.deviceId() != null && !produto.deviceId().isBlank())
                ? produto.deviceId()
                : deviceIdentity.getDeviceId();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_INSERT)) {

            stmt.setString(1, id);
            stmt.setString(2, produto.sku());
            stmt.setString(3, produto.tipoProduto().name());
            stmt.setString(4, produto.categoria());
            stmt.setString(5, produto.marca());
            stmt.setString(6, produto.modelo());
            stmt.setString(7, produto.cor());
            stmt.setString(8, produto.descricao());
            stmt.setString(9, produto.caracteristicasTecnicas());
            stmt.setInt(10, produto.totalRecebido() != null ? produto.totalRecebido() : 0);
            stmt.setInt(11, produto.precoUnitarioCentavos() != null ? produto.precoUnitarioCentavos() : 0);
            stmt.setInt(12, produto.ivaBasisPoints() != null ? produto.ivaBasisPoints() : 0);
            stmt.setInt(13, produto.precoTotalCentavos() != null ? produto.precoTotalCentavos() : 0);
            stmt.setBoolean(14, produto.ativo() != null ? produto.ativo() : true);
            stmt.setString(15, produto.fornecedor());
            stmt.setString(16, produto.numeroFatura());
            stmt.setBytes(17, produto.faturaCompra());
            stmt.setString(18, agora);
            stmt.setString(19, agora);
            stmt.setString(20, deviceId);

            stmt.executeUpdate();
            logger.log(System.Logger.Level.INFO, "✅ Produto inserido: ID={0}, SKU={1}", id, produto.sku());
            return id;
        }
    }

    public void update(CatalogoProdutos produto) throws SQLException {
        if (produto == null) throw new IllegalArgumentException("Produto inválido");
        if (produto.id() == null || produto.id().isBlank()) {
            throw new SQLException("ID do produto é obrigatório para atualização");
        }

        String agora = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS).toString();
        String deviceId = deviceIdentity.getDeviceId();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE)) {

            stmt.setString(1, produto.sku());
            stmt.setString(2, produto.tipoProduto().name());
            stmt.setString(3, produto.categoria());
            stmt.setString(4, produto.marca());
            stmt.setString(5, produto.modelo());
            stmt.setString(6, produto.cor());
            stmt.setString(7, produto.descricao());
            stmt.setString(8, produto.caracteristicasTecnicas());
            stmt.setInt(9, produto.totalRecebido() != null ? produto.totalRecebido() : 0);
            stmt.setInt(10, produto.precoUnitarioCentavos() != null ? produto.precoUnitarioCentavos() : 0);
            stmt.setInt(11, produto.ivaBasisPoints() != null ? produto.ivaBasisPoints() : 0);
            stmt.setInt(12, produto.precoTotalCentavos() != null ? produto.precoTotalCentavos() : 0);
            stmt.setBoolean(13, produto.ativo() != null ? produto.ativo() : true);
            stmt.setString(14, produto.fornecedor());
            stmt.setString(15, produto.numeroFatura());
            stmt.setBytes(16, produto.faturaCompra());
            stmt.setString(17, agora);
            stmt.setString(18, deviceId);
            stmt.setString(19, produto.id());

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Produto com ID " + produto.id() + " não encontrado");
            }
            logger.log(System.Logger.Level.INFO, "✅ Produto atualizado: ID={0}, SKU={1}",
                    produto.id(), produto.sku());
        }
    }

    public String save(CatalogoProdutos produto) throws SQLException {
        if (produto.id() != null && !produto.id().isBlank() && existsById(produto.id())) {
            update(produto);
            return produto.id();
        }
        return insert(produto);
    }

    /** Soft delete (tombstone). Chave: id. */
    public boolean softDelete(String id) throws SQLException {
        String agora = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS).toString();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_TOMBSTONE)) {
            stmt.setString(1, agora);
            stmt.setString(2, deviceIdentity.getDeviceId());
            stmt.setString(3, id);
            boolean ok = stmt.executeUpdate() > 0;
            if (ok) logger.log(System.Logger.Level.INFO, "✅ Produto desativado: ID={0}", id);
            return ok;
        }
    }

    public boolean activate(String id) throws SQLException {
        String agora = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS).toString();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_ACTIVATE)) {
            stmt.setString(1, agora);
            stmt.setString(2, deviceIdentity.getDeviceId());
            stmt.setString(3, id);
            boolean ok = stmt.executeUpdate() > 0;
            if (ok) logger.log(System.Logger.Level.INFO, "✅ Produto ativado: ID={0}", id);
            return ok;
        }
    }

    public boolean deletePermanent(String id) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_DELETE_PERMANENT)) {
            stmt.setString(1, id);
            boolean ok = stmt.executeUpdate() > 0;
            if (ok) logger.log(System.Logger.Level.WARNING, "⚠️ Produto excluído permanentemente: ID={0}", id);
            return ok;
        }
    }

    // ============================================================
    // ESTOQUE
    // ============================================================

    public boolean atualizarEstoque(String id, int quantidade) throws SQLException {
        String agora = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS).toString();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE_STOCK)) {
            stmt.setInt(1, quantidade);
            stmt.setString(2, agora);
            stmt.setString(3, deviceIdentity.getDeviceId());
            stmt.setString(4, id);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean adicionarEstoque(String id, int quantidade) throws SQLException {
        String agora = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS).toString();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_ADD_STOCK)) {
            stmt.setInt(1, quantidade);
            stmt.setString(2, agora);
            stmt.setString(3, deviceIdentity.getDeviceId());
            stmt.setString(4, id);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean removerEstoque(String id, int quantidade) throws SQLException {
        String agora = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS).toString();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_REMOVE_STOCK)) {
            stmt.setInt(1, quantidade);
            stmt.setString(2, agora);
            stmt.setString(3, deviceIdentity.getDeviceId());
            stmt.setString(4, id);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Atualiza preço em centavos + IVA em basis points. Recalcula preço total
     * via {@link CatalogoProdutos#calcularPrecoTotalCentavos(int, int)} (HALF_UP).
     */
    public boolean atualizarPreco(String id, int precoCentavos, int ivaBasisPoints) throws SQLException {
        int precoTotal = CatalogoProdutos.calcularPrecoTotalCentavos(precoCentavos, ivaBasisPoints);
        String agora = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS).toString();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE_PRECO)) {
            stmt.setInt(1, precoCentavos);
            stmt.setInt(2, ivaBasisPoints);
            stmt.setInt(3, precoTotal);
            stmt.setString(4, agora);
            stmt.setString(5, deviceIdentity.getDeviceId());
            stmt.setString(6, id);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean atualizarFornecedor(String id, String fornecedor) throws SQLException {
        String agora = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS).toString();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE_FORNECEDOR)) {
            stmt.setString(1, fornecedor);
            stmt.setString(2, agora);
            stmt.setString(3, deviceIdentity.getDeviceId());
            stmt.setString(4, id);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean atualizarFatura(String id, String numeroFatura, byte[] faturaCompra) throws SQLException {
        String agora = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS).toString();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE_FATURA)) {
            stmt.setString(1, numeroFatura);
            stmt.setBytes(2, faturaCompra);
            stmt.setString(3, agora);
            stmt.setString(4, deviceIdentity.getDeviceId());
            stmt.setString(5, id);
            return stmt.executeUpdate() > 0;
        }
    }

    // ============================================================
    // FILTROS
    // ============================================================

    public List<CatalogoProdutos> findByTipo(TipoProduto tipo) throws SQLException {
        if (tipo == null) return List.of();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_TIPO)) {
            stmt.setString(1, tipo.name());
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    public List<CatalogoProdutos> findByCategoria(String categoria) throws SQLException {
        if (categoria == null || categoria.isBlank()) return List.of();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_CATEGORIA)) {
            stmt.setString(1, categoria);
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    public List<CatalogoProdutos> findByMarca(String marca) throws SQLException {
        if (marca == null || marca.isBlank()) return List.of();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_MARCA)) {
            stmt.setString(1, marca);
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    public List<CatalogoProdutos> findByMarcaAndModelo(String marca, String modelo) throws SQLException {
        if (marca == null || marca.isBlank() || modelo == null || modelo.isBlank()) return List.of();
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
        if (fornecedor == null || fornecedor.isBlank()) return List.of();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_FORNECEDOR)) {
            stmt.setString(1, fornecedor);
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    public List<CatalogoProdutos> search(String termo) throws SQLException {
        if (termo == null || termo.isBlank()) return List.of();
        String like = "%" + termo + "%";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_SEARCH)) {
            for (int i = 1; i <= 6; i++) stmt.setString(i, like);
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    // ============================================================
    // ESTOQUE — CONSULTAS
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
    // DISTINCT
    // ============================================================

    public List<String> findDistinctCategorias() throws SQLException { return executeQueryString(SQL_DISTINCT_CATEGORIAS); }
    public List<String> findDistinctMarcas() throws SQLException     { return executeQueryString(SQL_DISTINCT_MARCAS); }
    public List<String> findDistinctCores() throws SQLException      { return executeQueryString(SQL_DISTINCT_CORES); }
    public List<String> findDistinctTipos() throws SQLException      { return executeQueryString(SQL_DISTINCT_TIPOS); }
    public List<String> findDistinctFornecedores() throws SQLException { return executeQueryString(SQL_DISTINCT_FORNECEDORES); }

    public List<String> findDistinctModelos(String marca) throws SQLException {
        if (marca == null || marca.isBlank()) return List.of();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_DISTINCT_MODELOS_BY_MARCA)) {
            stmt.setString(1, marca);
            try (ResultSet rs = stmt.executeQuery()) {
                List<String> out = new ArrayList<>();
                while (rs.next()) out.add(rs.getString(1));
                return List.copyOf(out);
            }
        }
    }

    // ============================================================
    // FATURA ÚNICA — só número (fornecedor é informativo)
    // ============================================================

    /**
     * Verifica se já existe produto com o número de fatura.
     * Ignora deletados. Retorna false se numeroFatura for vazio.
     */
    public boolean existePorFatura(String numeroFatura) throws SQLException {
        if (numeroFatura == null || numeroFatura.isBlank()) return false;
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_EXISTS_FATURA)) {
            stmt.setString(1, numeroFatura);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * Variante para atualização: exclui o próprio id da checagem.
     * Útil quando o usuário está editando um produto existente sem trocar a fatura.
     */
    public boolean existePorFaturaExcetoId(String numeroFatura, String idExceto) throws SQLException {
        if (numeroFatura == null || numeroFatura.isBlank()
                || idExceto == null || idExceto.isBlank()) {
            return false;
        }
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_EXISTS_FATURA_EXCETO_ID)) {
            stmt.setString(1, numeroFatura);
            stmt.setString(2, idExceto);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    // ============================================================
    // ESTATÍSTICAS
    // ============================================================

    public boolean existsById(String id) throws SQLException {
        if (id == null || id.isBlank()) return false;
        return count("SELECT COUNT(*) FROM " + TABLE + " WHERE id = ? AND deleted = 0", id) > 0;
    }

    public boolean existsBySku(String sku) throws SQLException {
        if (sku == null || sku.isBlank()) return false;
        return count("SELECT COUNT(*) FROM " + TABLE + " WHERE sku = ? AND deleted = 0", sku) > 0;
    }

    public int countAll() throws SQLException {
        return count("SELECT COUNT(*) FROM " + TABLE + " WHERE deleted = 0");
    }

    public int countActive() throws SQLException {
        return count("SELECT COUNT(*) FROM " + TABLE + " WHERE ativo = 1 AND deleted = 0");
    }

    public int countByTipo(TipoProduto tipo) throws SQLException {
        if (tipo == null) return 0;
        return count("SELECT COUNT(*) FROM " + TABLE + " WHERE tipo_produto = ? AND ativo = 1 AND deleted = 0",
                tipo.name());
    }

    public int countByCategoria(String categoria) throws SQLException {
        return count("SELECT COUNT(*) FROM " + TABLE + " WHERE categoria = ? AND ativo = 1 AND deleted = 0",
                categoria);
    }

    public int countByMarca(String marca) throws SQLException {
        return count("SELECT COUNT(*) FROM " + TABLE + " WHERE marca = ? AND ativo = 1 AND deleted = 0", marca);
    }

    /** Valor total do estoque em centavos (preço unitário × quantidade). */
    public long getValorTotalEstoqueCentavos() throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_VALOR_TOTAL_ESTOQUE);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getLong("valor_total") : 0L;
        }
    }

    /** Valor total do estoque em centavos (preço total com IVA × quantidade). */
    public long getValorTotalEstoqueComIvaCentavos() throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_VALOR_TOTAL_ESTOQUE_COM_IVA);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getLong("valor_total") : 0L;
        }
    }

    public Map<String, Integer> countByTipo() throws SQLException        { return countGroupBy(SQL_COUNT_BY_TIPO); }
    public Map<String, Integer> countByCategoria() throws SQLException   { return countGroupBy(SQL_COUNT_BY_CATEGORIA); }
    public Map<String, Integer> countByMarca() throws SQLException       { return countGroupBy(SQL_COUNT_BY_MARCA); }
    public Map<String, Integer> countByFornecedor() throws SQLException  { return countGroupBy(SQL_COUNT_BY_FORNECEDOR); }

    // ============================================================
    // AUXILIARES
    // ============================================================

    private int count(String sql, Object... params) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) stmt.setObject(i + 1, params[i]);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private List<String> executeQueryString(String sql) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            List<String> out = new ArrayList<>();
            while (rs.next()) out.add(rs.getString(1));
            return List.copyOf(out);
        }
    }

    private Map<String, Integer> countGroupBy(String sql) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            Map<String, Integer> out = new LinkedHashMap<>();
            while (rs.next()) out.put(rs.getString(1), rs.getInt(2));
            return Map.copyOf(out);
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
        while (rs.next()) list.add(mapResultSet(rs));
        return List.copyOf(list);
    }

    /** Mapeia os 20 componentes do record CatalogoProdutos v3.0. */
    private CatalogoProdutos mapResultSet(ResultSet rs) throws SQLException {
        TipoProduto tipo;
        try {
            tipo = TipoProduto.fromString(rs.getString("tipo_produto"));
        } catch (IllegalArgumentException e) {
            logger.log(System.Logger.Level.WARNING,
                    "tipo_produto inválido no banco: {0} — usando EQUIPAMENTO",
                    rs.getString("tipo_produto"));
            tipo = TipoProduto.EQUIPAMENTO;
        }

        return new CatalogoProdutos(
                rs.getString("id"),
                rs.getString("sku"),
                tipo,
                rs.getString("categoria"),
                rs.getString("marca"),
                rs.getString("modelo"),
                rs.getString("cor"),
                rs.getString("descricao"),
                rs.getString("caracteristicas_tecnicas"),
                rs.getInt("total_recebido"),
                rs.getInt("preco_unitario_centavos"),
                rs.getInt("iva_basis_points"),
                rs.getInt("preco_total_centavos"),
                rs.getBoolean("ativo"),
                rs.getString("fornecedor"),
                rs.getString("numero_fatura"),
                rs.getBytes("fatura_compra"),
                getLocalDateTime(rs, "created_at"),
                getLocalDateTime(rs, "updated_at"),
                rs.getString("device_id"),
                rs.getInt("deleted") != 0
        );
    }

    /**
     * Parser TOLERANTE — evita DateTimeParseException:
     * SQLite grava "yyyy-MM-dd HH:mm:ss"; o app grava ISO com 'T'.
     */
    private LocalDateTime getLocalDateTime(ResultSet rs, String column) throws SQLException {
        String value = rs.getString(column);
        if (value == null || value.isBlank()) return null;
        try {
            return value.contains("T")
                    ? LocalDateTime.parse(value)
                    : LocalDateTime.parse(value, FMT_SQLITE);
        } catch (Exception e) {
            logger.log(System.Logger.Level.WARNING,
                    "Data inválida em catalogo_produtos.{0}: {1}", column, value);
            return null;
        }
    }
}