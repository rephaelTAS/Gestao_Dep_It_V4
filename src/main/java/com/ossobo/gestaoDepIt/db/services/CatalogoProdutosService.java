package com.ossobo.gestaoDepIt.db.services;

import com.ossobo.gestaoDepIt.db.config.event.CatalogoEvent;
import com.ossobo.gestaoDepIt.db.models.CatalogoProdutos;
import com.ossobo.gestaoDepIt.db.repositories.CatalogoProdutosRepository;

import com.ossobo.winterfx.anotations.Inject;
import com.ossobo.winterfx.anotations.Service;
import com.ossobo.winterfx.event.EventBus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * CatalogoProdutosService - Regras de negócio com Event System
 * v2.1 - Service completo com todos os métodos
 *
 * Responsabilidades:
 * - Validação de negócio
 * - Publicação de eventos (CatalogoEvent)
 * - Orquestração de operações
 * - Estatísticas e relatórios
 */
@Service
public class CatalogoProdutosService {

    private static final Logger logger = LoggerFactory.getLogger(CatalogoProdutosService.class);

    @Inject
    private CatalogoProdutosRepository repository;

    @Inject
    private EventBus eventBus;

    // ============================================================
    // CRUD
    // ============================================================

    public List<CatalogoProdutos> listarTodos() throws SQLException {
        return repository.findAll();
    }

    public List<CatalogoProdutos> listarTodos(int pagina, int tamanho) throws SQLException {
        int offset = (pagina - 1) * tamanho;
        return repository.findAll(tamanho, offset);
    }

    public Optional<CatalogoProdutos> buscarPorSku(String sku) throws SQLException {
        if (!(sku instanceof String s) || s.isBlank()) {
            throw new IllegalArgumentException("SKU é obrigatório");
        }
        return repository.findBySku(sku);
    }

    public CatalogoProdutos salvar(CatalogoProdutos produto) throws SQLException {
        if (!(produto instanceof CatalogoProdutos p)) {
            throw new IllegalArgumentException("Produto inválido");
        }

        validarProduto(p);

        if (repository.existsBySku(p.sku())) {
            throw new IllegalStateException("Produto com SKU " + p.sku() + " já existe");
        }

        String sku = repository.insert(p);
        CatalogoProdutos salvo = repository.findBySku(sku)
                .orElseThrow(() -> new SQLException("Falha ao buscar produto salvo"));

        eventBus.publish(new CatalogoEvent<>(salvo, "CRIADO"));
        logger.info("✅ Produto criado: SKU={}, Nome={}", sku, p.getNomeCompleto());

        return salvo;
    }

    public CatalogoProdutos atualizar(CatalogoProdutos produto) throws SQLException {
        if (!(produto instanceof CatalogoProdutos p)) {
            throw new IllegalArgumentException("Produto inválido");
        }

        validarProduto(p);

        if (!repository.existsBySku(p.sku())) {
            throw new IllegalArgumentException("Produto com SKU " + p.sku() + " não encontrado");
        }

        repository.update(p);
        CatalogoProdutos atualizado = repository.findBySku(p.sku())
                .orElseThrow(() -> new SQLException("Falha ao buscar produto atualizado"));

        eventBus.publish(new CatalogoEvent<>(atualizado, "ATUALIZADO"));
        logger.info("✅ Produto atualizado: SKU={}", p.sku());

        return atualizado;
    }

    public void excluir(String sku) throws SQLException {
        Optional<CatalogoProdutos> opt = repository.findBySku(sku);
        CatalogoProdutos p = opt.orElseThrow(() ->
                new IllegalArgumentException("Produto com SKU " + sku + " não encontrado")
        );

        if (p.totalRecebido() != null && p.totalRecebido() > 0) {
            throw new IllegalStateException("Não é possível excluir produto com estoque");
        }

        repository.softDelete(sku);
        eventBus.publish(new CatalogoEvent<>(p, "EXCLUIDO"));
        logger.info("✅ Produto excluído: SKU={}", sku);
    }

    public void excluirPermanentemente(String sku) throws SQLException {
        Optional<CatalogoProdutos> opt = repository.findBySku(sku);
        CatalogoProdutos p = opt.orElseThrow(() ->
                new IllegalArgumentException("Produto com SKU " + sku + " não encontrado")
        );

        repository.deletePermanent(sku);
        eventBus.publish(new CatalogoEvent<>(p, "EXCLUIDO_PERMANENTEMENTE"));
        logger.warn("⚠️ Produto excluído permanentemente: SKU={}", sku);
    }

    public void ativar(String sku) throws SQLException {
        if (!repository.existsBySku(sku)) {
            throw new IllegalArgumentException("Produto com SKU " + sku + " não encontrado");
        }

        repository.activate(sku);
        Optional<CatalogoProdutos> p = repository.findBySku(sku);
        p.ifPresent(produto -> {
            eventBus.publish(new CatalogoEvent<>(produto, "ATIVADO"));
            logger.info("✅ Produto ativado: SKU={}", sku);
        });
    }

    public void desativar(String sku) throws SQLException {
        if (!repository.existsBySku(sku)) {
            throw new IllegalArgumentException("Produto com SKU " + sku + " não encontrado");
        }

        repository.softDelete(sku);
        Optional<CatalogoProdutos> p = repository.findBySku(sku);
        p.ifPresent(produto -> {
            eventBus.publish(new CatalogoEvent<>(produto, "DESATIVADO"));
            logger.info("✅ Produto desativado: SKU={}", sku);
        });
    }

    // ============================================================
    // OPERAÇÕES DE ESTOQUE
    // ============================================================

    public CatalogoProdutos adicionarEstoque(String sku, int quantidade) throws SQLException {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser positiva");
        }

        Optional<CatalogoProdutos> opt = repository.findBySku(sku);
        CatalogoProdutos p = opt.orElseThrow(() ->
                new IllegalArgumentException("Produto com SKU " + sku + " não encontrado")
        );

        repository.adicionarEstoque(sku, quantidade);

        CatalogoProdutos atualizado = repository.findBySku(sku)
                .orElseThrow(() -> new SQLException("Falha ao buscar produto atualizado"));

        eventBus.publish(new CatalogoEvent<>(atualizado, "ESTOQUE_ATUALIZADO"));
        logger.info("✅ Estoque adicionado: SKU={}, Qtd={}, NovoTotal={}",
                sku, quantidade, atualizado.totalRecebido());

        return atualizado;
    }

    public CatalogoProdutos removerEstoque(String sku, int quantidade) throws SQLException {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser positiva");
        }

        Optional<CatalogoProdutos> opt = repository.findBySku(sku);
        CatalogoProdutos p = opt.orElseThrow(() ->
                new IllegalArgumentException("Produto com SKU " + sku + " não encontrado")
        );

        if (p.totalRecebido() < quantidade) {
            throw new IllegalStateException("Estoque insuficiente. Disponível: " + p.totalRecebido());
        }

        repository.removerEstoque(sku, quantidade);

        CatalogoProdutos atualizado = repository.findBySku(sku)
                .orElseThrow(() -> new SQLException("Falha ao buscar produto atualizado"));

        eventBus.publish(new CatalogoEvent<>(atualizado, "ESTOQUE_ATUALIZADO"));
        logger.info("✅ Estoque removido: SKU={}, Qtd={}, NovoTotal={}",
                sku, quantidade, atualizado.totalRecebido());

        return atualizado;
    }

    public CatalogoProdutos atualizarEstoque(String sku, int quantidade) throws SQLException {
        if (quantidade < 0) {
            throw new IllegalArgumentException("Quantidade não pode ser negativa");
        }

        Optional<CatalogoProdutos> opt = repository.findBySku(sku);
        CatalogoProdutos p = opt.orElseThrow(() ->
                new IllegalArgumentException("Produto com SKU " + sku + " não encontrado")
        );

        repository.atualizarEstoque(sku, quantidade);

        CatalogoProdutos atualizado = repository.findBySku(sku)
                .orElseThrow(() -> new SQLException("Falha ao buscar produto atualizado"));

        eventBus.publish(new CatalogoEvent<>(atualizado, "ESTOQUE_ATUALIZADO"));
        logger.info("✅ Estoque atualizado: SKU={}, NovoTotal={}", sku, quantidade);

        return atualizado;
    }

    // ============================================================
    // OPERAÇÕES DE PREÇO
    // ============================================================

    public CatalogoProdutos atualizarPreco(String sku, BigDecimal precoUnitario) throws SQLException {
        if (precoUnitario == null || precoUnitario.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Preço deve ser maior ou igual a zero");
        }

        if (!repository.existsBySku(sku)) {
            throw new IllegalArgumentException("Produto com SKU " + sku + " não encontrado");
        }

        repository.atualizarPreco(sku, precoUnitario);

        CatalogoProdutos atualizado = repository.findBySku(sku)
                .orElseThrow(() -> new SQLException("Falha ao buscar produto atualizado"));

        eventBus.publish(new CatalogoEvent<>(atualizado, "PRECO_ATUALIZADO"));
        logger.info("✅ Preço atualizado: SKU={}, Preço={}", sku, precoUnitario);

        return atualizado;
    }

    // ============================================================
    // OPERAÇÕES DE FORNECEDOR E FATURA
    // ============================================================

    public CatalogoProdutos atualizarFornecedor(String sku, String fornecedor) throws SQLException {
        if (!(fornecedor instanceof String f) || f.isBlank()) {
            throw new IllegalArgumentException("Fornecedor não pode ser vazio");
        }

        if (!repository.existsBySku(sku)) {
            throw new IllegalArgumentException("Produto com SKU " + sku + " não encontrado");
        }

        repository.atualizarFornecedor(sku, fornecedor);

        CatalogoProdutos atualizado = repository.findBySku(sku)
                .orElseThrow(() -> new SQLException("Falha ao buscar produto atualizado"));

        eventBus.publish(new CatalogoEvent<>(atualizado, "FORNECEDOR_ATUALIZADO"));
        logger.info("✅ Fornecedor atualizado: SKU={}, Fornecedor={}", sku, fornecedor);

        return atualizado;
    }

    public CatalogoProdutos atualizarFatura(String sku, String numeroFatura, byte[] faturaCompra) throws SQLException {
        if (!(numeroFatura instanceof String n) || n.isBlank()) {
            throw new IllegalArgumentException("Número da fatura não pode ser vazio");
        }

        if (!repository.existsBySku(sku)) {
            throw new IllegalArgumentException("Produto com SKU " + sku + " não encontrado");
        }

        repository.atualizarFatura(sku, numeroFatura, faturaCompra);

        CatalogoProdutos atualizado = repository.findBySku(sku)
                .orElseThrow(() -> new SQLException("Falha ao buscar produto atualizado"));

        eventBus.publish(new CatalogoEvent<>(atualizado, "FATURA_ATUALIZADA"));
        logger.info("✅ Fatura atualizada: SKU={}, Número={}", sku, numeroFatura);

        return atualizado;
    }

    // ============================================================
    // CONSULTAS
    // ============================================================

    public List<CatalogoProdutos> buscarPorTipo(String tipo) throws SQLException {
        return repository.findByTipo(tipo);
    }

    public List<CatalogoProdutos> buscarPorCategoria(String categoria) throws SQLException {
        return repository.findByCategoria(categoria);
    }

    public List<CatalogoProdutos> buscarPorMarca(String marca) throws SQLException {
        return repository.findByMarca(marca);
    }

    public List<CatalogoProdutos> buscarPorMarcaEModelo(String marca, String modelo) throws SQLException {
        return repository.findByMarcaAndModelo(marca, modelo);
    }

    public List<CatalogoProdutos> buscarPorFornecedor(String fornecedor) throws SQLException {
        return repository.findByFornecedor(fornecedor);
    }

    public List<CatalogoProdutos> buscarPorTermo(String termo) throws SQLException {
        if (termo == null || termo.isBlank()) {
            return listarTodos();
        }
        return repository.search(termo);
    }

    public List<CatalogoProdutos> buscarEstoqueBaixo(int limite) throws SQLException {
        return repository.findComEstoqueBaixo(limite);
    }

    public List<CatalogoProdutos> buscarSemEstoque() throws SQLException {
        return repository.findSemEstoque();
    }

    public List<CatalogoProdutos> buscarComEstoque() throws SQLException {
        return repository.findComEstoque();
    }

    // ============================================================
    // DISTINCT VALUES
    // ============================================================

    public List<String> listarCategorias() throws SQLException {
        return repository.findDistinctCategorias();
    }

    public List<String> listarMarcas() throws SQLException {
        return repository.findDistinctMarcas();
    }

    public List<String> listarCores() throws SQLException {
        return repository.findDistinctCores();
    }

    public List<String> listarTipos() throws SQLException {
        return repository.findDistinctTipos();
    }

    public List<String> listarFornecedores() throws SQLException {
        return repository.findDistinctFornecedores();
    }

    public List<String> listarModelosPorMarca(String marca) throws SQLException {
        return repository.findDistinctModelos(marca);
    }

    // ============================================================
    // ESTATÍSTICAS
    // ============================================================

    public int contarTotal() throws SQLException {
        return repository.countAll();
    }

    public int contarAtivos() throws SQLException {
        return repository.countActive();
    }

    public int contarPorTipo(String tipo) throws SQLException {
        return repository.countByTipo(tipo);
    }

    public int contarPorCategoria(String categoria) throws SQLException {
        return repository.countByCategoria(categoria);
    }

    public int contarPorMarca(String marca) throws SQLException {
        return repository.countByMarca(marca);
    }

    public BigDecimal valorTotalEstoque() throws SQLException {
        return repository.getValorTotalEstoque();
    }

    public BigDecimal valorTotalEstoqueComIva() throws SQLException {
        return repository.getValorTotalEstoqueComIva();
    }

    public Map<String, Integer> estatisticasPorTipo() throws SQLException {
        return repository.countByTipo();
    }

    public Map<String, Integer> estatisticasPorCategoria() throws SQLException {
        return repository.countByCategoria();
    }

    public Map<String, Integer> estatisticasPorMarca() throws SQLException {
        return repository.countByMarca();
    }

    public Map<String, Integer> estatisticasPorFornecedor() throws SQLException {
        return repository.countByFornecedor();
    }

    // ============================================================
    // VALIDAÇÕES
    // ============================================================

    public boolean existePorSku(String sku) throws SQLException {
        return repository.existsBySku(sku);
    }

    // ============================================================
    // MÉTODOS PRIVADOS
    // ============================================================

    private void validarProduto(CatalogoProdutos p) {
        if (!(p.sku() instanceof String s) || s.isBlank()) {
            throw new IllegalArgumentException("SKU é obrigatório");
        }
        if (!(p.categoria() instanceof String cat) || cat.isBlank()) {
            throw new IllegalArgumentException("Categoria é obrigatória");
        }
        if (!(p.marca() instanceof String m) || m.isBlank()) {
            throw new IllegalArgumentException("Marca é obrigatória");
        }
        if (!(p.modelo() instanceof String mod) || mod.isBlank()) {
            throw new IllegalArgumentException("Modelo é obrigatório");
        }
        if (p.precoUnitario() == null || p.precoUnitario().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Preço deve ser maior ou igual a zero");
        }
        if (p.totalRecebido() != null && p.totalRecebido() < 0) {
            throw new IllegalArgumentException("Estoque não pode ser negativo");
        }
        if (p.iva() != null && (p.iva().compareTo(BigDecimal.ZERO) < 0 || p.iva().compareTo(BigDecimal.valueOf(100)) > 0)) {
            throw new IllegalArgumentException("IVA deve estar entre 0% e 100%");
        }
    }

    public List<CatalogoProdutos> buscarComFiltros(String tipo, String categoria, String marca, String modelo, String cor, Boolean ativo) {
        return null;
    }
}