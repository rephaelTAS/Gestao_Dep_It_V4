package com.ossobo.gestaoDepIt.db.services;

import com.ossobo.gestaoDepIt.db.config.event.CatalogoEvent;
import com.ossobo.gestaoDepIt.db.enums.TipoProduto;
import com.ossobo.gestaoDepIt.db.models.CatalogoProdutos;
import com.ossobo.gestaoDepIt.db.repositories.CatalogoProdutosRepository;

import com.ossobo.winterfx.anotations.Inject;
import com.ossobo.winterfx.anotations.Service;
import com.ossobo.winterfx.event.EventBus;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * CatalogoProdutosService v2.4 - Regras de negócio com Event System
 *
 * v2.4 — Regra "número de fatura único" (fornecedor é informativo):
 *        - salvar() rejeita duplicata de numero_fatura
 *        - atualizar() rejeita duplicata exceto o próprio id
 *        - atualizarFatura() rejeita duplicata exceto o próprio id
 *        - método público existePorFatura(String numero) exposto ao Routes
 *
 * v2.3 — Primeira versão da regra (fornecedor + número) — substituída.
 * v2.2 — Alinhado ao CatalogoProdutos v3.0 + Repository v2.5:
 *        - Sem BigDecimal — tudo Integer centavos/basis points
 *        - Chave de escrita: id (UUID); sku continua UNIQUE para consulta
 */
@Service
public class CatalogoProdutosService {

    private static final System.Logger logger = System.getLogger(CatalogoProdutosService.class.getName());

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

    public Optional<CatalogoProdutos> buscarPorId(String id) throws SQLException {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("ID é obrigatório");
        }
        return repository.findById(id);
    }

    public Optional<CatalogoProdutos> buscarPorSku(String sku) throws SQLException {
        if (sku == null || sku.isBlank()) {
            throw new IllegalArgumentException("SKU é obrigatório");
        }
        return repository.findBySku(sku);
    }

    public CatalogoProdutos salvar(CatalogoProdutos produto) throws SQLException {
        if (produto == null) throw new IllegalArgumentException("Produto inválido");
        validarProduto(produto);

        // REGRA: número de fatura é único (fornecedor é informativo)
        if (repository.existePorFatura(produto.numeroFatura())) {
            throw new IllegalStateException(
                    "Já existe um produto cadastrado com a fatura '"
                            + produto.numeroFatura() + "'.");
        }

        if (repository.existsBySku(produto.sku())) {
            throw new IllegalStateException("Produto com SKU " + produto.sku() + " já existe");
        }

        String id = repository.insert(produto);
        CatalogoProdutos salvo = repository.findById(id)
                .orElseThrow(() -> new SQLException("Falha ao buscar produto salvo"));

        eventBus.publish(new CatalogoEvent<>(salvo, "CRIADO"));
        logger.log(System.Logger.Level.INFO, "✅ Produto criado: ID={0}, SKU={1}",
                id, produto.sku());
        return salvo;
    }

    public CatalogoProdutos atualizar(CatalogoProdutos produto) throws SQLException {
        if (produto == null) throw new IllegalArgumentException("Produto inválido");
        validarProduto(produto);

        if (produto.id() == null || produto.id().isBlank()) {
            throw new IllegalArgumentException("ID é obrigatório para atualização");
        }
        if (!repository.existsById(produto.id())) {
            throw new IllegalArgumentException("Produto com ID " + produto.id() + " não encontrado");
        }

        // REGRA: número de fatura é único — exceto o próprio produto
        if (repository.existePorFaturaExcetoId(produto.numeroFatura(), produto.id())) {
            throw new IllegalStateException(
                    "Outro produto já usa a fatura '" + produto.numeroFatura() + "'.");
        }

        repository.update(produto);
        CatalogoProdutos atualizado = repository.findById(produto.id())
                .orElseThrow(() -> new SQLException("Falha ao buscar produto atualizado"));

        eventBus.publish(new CatalogoEvent<>(atualizado, "ATUALIZADO"));
        logger.log(System.Logger.Level.INFO, "✅ Produto atualizado: ID={0}, SKU={1}",
                produto.id(), produto.sku());
        return atualizado;
    }

    public void excluir(String id) throws SQLException {
        CatalogoProdutos p = repository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("Produto com ID " + id + " não encontrado"));

        if (p.totalRecebido() != null && p.totalRecebido() > 0) {
            throw new IllegalStateException("Não é possível excluir produto com estoque");
        }

        repository.softDelete(id);
        eventBus.publish(new CatalogoEvent<>(p, "EXCLUIDO"));
        logger.log(System.Logger.Level.INFO, "✅ Produto excluído: ID={0}", id);
    }

    public void excluirPermanentemente(String id) throws SQLException {
        CatalogoProdutos p = repository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("Produto com ID " + id + " não encontrado"));

        repository.deletePermanent(id);
        eventBus.publish(new CatalogoEvent<>(p, "EXCLUIDO_PERMANENTEMENTE"));
        logger.log(System.Logger.Level.WARNING, "⚠️ Produto excluído permanentemente: ID={0}", id);
    }

    public void ativar(String id) throws SQLException {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Produto com ID " + id + " não encontrado");
        }

        repository.activate(id);
        repository.findById(id).ifPresent(produto -> {
            eventBus.publish(new CatalogoEvent<>(produto, "ATIVADO"));
            logger.log(System.Logger.Level.INFO, "✅ Produto ativado: ID={0}", id);
        });
    }

    public void desativar(String id) throws SQLException {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Produto com ID " + id + " não encontrado");
        }

        repository.softDelete(id);
        repository.findById(id).ifPresent(produto -> {
            eventBus.publish(new CatalogoEvent<>(produto, "DESATIVADO"));
            logger.log(System.Logger.Level.INFO, "✅ Produto desativado: ID={0}", id);
        });
    }

    // ============================================================
    // ESTOQUE
    // ============================================================

    public CatalogoProdutos adicionarEstoque(String id, int quantidade) throws SQLException {
        if (quantidade <= 0) throw new IllegalArgumentException("Quantidade deve ser positiva");
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Produto com ID " + id + " não encontrado");
        }

        repository.adicionarEstoque(id, quantidade);
        CatalogoProdutos atualizado = repository.findById(id)
                .orElseThrow(() -> new SQLException("Falha ao buscar produto atualizado"));

        eventBus.publish(new CatalogoEvent<>(atualizado, "ESTOQUE_ATUALIZADO"));
        logger.log(System.Logger.Level.INFO, "✅ Estoque adicionado: ID={0}, Qtd={1}, NovoTotal={2}",
                id, quantidade, atualizado.totalRecebido());
        return atualizado;
    }

    public CatalogoProdutos removerEstoque(String id, int quantidade) throws SQLException {
        if (quantidade <= 0) throw new IllegalArgumentException("Quantidade deve ser positiva");

        CatalogoProdutos p = repository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("Produto com ID " + id + " não encontrado"));

        if (p.totalRecebido() < quantidade) {
            throw new IllegalStateException("Estoque insuficiente. Disponível: " + p.totalRecebido());
        }

        repository.removerEstoque(id, quantidade);
        CatalogoProdutos atualizado = repository.findById(id)
                .orElseThrow(() -> new SQLException("Falha ao buscar produto atualizado"));

        eventBus.publish(new CatalogoEvent<>(atualizado, "ESTOQUE_ATUALIZADO"));
        logger.log(System.Logger.Level.INFO, "✅ Estoque removido: ID={0}, Qtd={1}, NovoTotal={2}",
                id, quantidade, atualizado.totalRecebido());
        return atualizado;
    }

    public CatalogoProdutos atualizarEstoque(String id, int quantidade) throws SQLException {
        if (quantidade < 0) throw new IllegalArgumentException("Quantidade não pode ser negativa");
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Produto com ID " + id + " não encontrado");
        }

        repository.atualizarEstoque(id, quantidade);
        CatalogoProdutos atualizado = repository.findById(id)
                .orElseThrow(() -> new SQLException("Falha ao buscar produto atualizado"));

        eventBus.publish(new CatalogoEvent<>(atualizado, "ESTOQUE_ATUALIZADO"));
        logger.log(System.Logger.Level.INFO, "✅ Estoque atualizado: ID={0}, NovoTotal={1}", id, quantidade);
        return atualizado;
    }

    // ============================================================
    // PREÇO
    // ============================================================

    /**
     * Atualiza preço em centavos + IVA em basis points. Recalcula total.
     * @param precoCentavos   ex.: 5990 = 59,90
     * @param ivaBasisPoints  ex.: 2300 = 23,00%
     */
    public CatalogoProdutos atualizarPreco(String id, int precoCentavos, int ivaBasisPoints) throws SQLException {
        if (precoCentavos < 0) throw new IllegalArgumentException("Preço não pode ser negativo");
        if (ivaBasisPoints < 0) throw new IllegalArgumentException("IVA não pode ser negativo");
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Produto com ID " + id + " não encontrado");
        }

        repository.atualizarPreco(id, precoCentavos, ivaBasisPoints);
        CatalogoProdutos atualizado = repository.findById(id)
                .orElseThrow(() -> new SQLException("Falha ao buscar produto atualizado"));

        eventBus.publish(new CatalogoEvent<>(atualizado, "PRECO_ATUALIZADO"));
        logger.log(System.Logger.Level.INFO, "✅ Preço atualizado: ID={0}, Unitário={1}, IVA={2}",
                id, precoCentavos, ivaBasisPoints);
        return atualizado;
    }

    // ============================================================
    // FORNECEDOR / FATURA
    // ============================================================

    public CatalogoProdutos atualizarFornecedor(String id, String fornecedor) throws SQLException {
        if (fornecedor == null || fornecedor.isBlank()) {
            throw new IllegalArgumentException("Fornecedor não pode ser vazio");
        }
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Produto com ID " + id + " não encontrado");
        }

        repository.atualizarFornecedor(id, fornecedor);
        CatalogoProdutos atualizado = repository.findById(id)
                .orElseThrow(() -> new SQLException("Falha ao buscar produto atualizado"));

        eventBus.publish(new CatalogoEvent<>(atualizado, "FORNECEDOR_ATUALIZADO"));
        logger.log(System.Logger.Level.INFO, "✅ Fornecedor atualizado: ID={0}, Fornecedor={1}", id, fornecedor);
        return atualizado;
    }

    public CatalogoProdutos atualizarFatura(String id, String numeroFatura, byte[] faturaCompra) throws SQLException {
        if (numeroFatura == null || numeroFatura.isBlank()) {
            throw new IllegalArgumentException("Número da fatura não pode ser vazio");
        }
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Produto com ID " + id + " não encontrado");
        }

        // Aplica a mesma regra de unicidade — número de fatura é único
        if (repository.existePorFaturaExcetoId(numeroFatura, id)) {
            throw new IllegalStateException(
                    "Outro produto já usa a fatura '" + numeroFatura + "'.");
        }

        repository.atualizarFatura(id, numeroFatura, faturaCompra);
        CatalogoProdutos atualizado = repository.findById(id)
                .orElseThrow(() -> new SQLException("Falha ao buscar produto atualizado"));

        eventBus.publish(new CatalogoEvent<>(atualizado, "FATURA_ATUALIZADA"));
        logger.log(System.Logger.Level.INFO, "✅ Fatura atualizada: ID={0}, Número={1}", id, numeroFatura);
        return atualizado;
    }

    // ============================================================
    // CONSULTAS
    // ============================================================

    public List<CatalogoProdutos> buscarPorTipo(TipoProduto tipo) throws SQLException {
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
        if (termo == null || termo.isBlank()) return listarTodos();
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
    // DISTINCT
    // ============================================================

    public List<String> listarCategorias() throws SQLException     { return repository.findDistinctCategorias(); }
    public List<String> listarMarcas() throws SQLException         { return repository.findDistinctMarcas(); }
    public List<String> listarCores() throws SQLException          { return repository.findDistinctCores(); }
    public List<String> listarTipos() throws SQLException          { return repository.findDistinctTipos(); }
    public List<String> listarFornecedores() throws SQLException   { return repository.findDistinctFornecedores(); }

    public List<String> listarModelosPorMarca(String marca) throws SQLException {
        return repository.findDistinctModelos(marca);
    }

    // ============================================================
    // ESTATÍSTICAS
    // ============================================================

    public int contarTotal() throws SQLException     { return repository.countAll(); }
    public int contarAtivos() throws SQLException    { return repository.countActive(); }

    public int contarPorTipo(TipoProduto tipo) throws SQLException {
        return repository.countByTipo(tipo);
    }

    public int contarPorCategoria(String categoria) throws SQLException {
        return repository.countByCategoria(categoria);
    }

    public int contarPorMarca(String marca) throws SQLException {
        return repository.countByMarca(marca);
    }

    /** Valor total do estoque (sem IVA), em CENTAVOS. */
    public long valorTotalEstoqueCentavos() throws SQLException {
        return repository.getValorTotalEstoqueCentavos();
    }

    /** Valor total do estoque (com IVA), em CENTAVOS. */
    public long valorTotalEstoqueComIvaCentavos() throws SQLException {
        return repository.getValorTotalEstoqueComIvaCentavos();
    }

    public Map<String, Integer> estatisticasPorTipo() throws SQLException        { return repository.countByTipo(); }
    public Map<String, Integer> estatisticasPorCategoria() throws SQLException   { return repository.countByCategoria(); }
    public Map<String, Integer> estatisticasPorMarca() throws SQLException       { return repository.countByMarca(); }
    public Map<String, Integer> estatisticasPorFornecedor() throws SQLException  { return repository.countByFornecedor(); }

    // ============================================================
    // VERIFICAÇÕES
    // ============================================================

    public boolean existePorId(String id) throws SQLException {
        return repository.existsById(id);
    }

    public boolean existePorSku(String sku) throws SQLException {
        return repository.existsBySku(sku);
    }

    /** Verifica se o número de fatura já está registrado. */
    public boolean existePorFatura(String numeroFatura) throws SQLException {
        return repository.existePorFatura(numeroFatura);
    }

    // ============================================================
    // MÉTODOS PRIVADOS
    // ============================================================

    private void validarProduto(CatalogoProdutos p) {
        if (p.sku() == null || p.sku().isBlank()) {
            throw new IllegalArgumentException("SKU é obrigatório");
        }
        if (p.categoria() == null || p.categoria().isBlank()) {
            throw new IllegalArgumentException("Categoria é obrigatória");
        }
        if (p.marca() == null || p.marca().isBlank()) {
            throw new IllegalArgumentException("Marca é obrigatória");
        }
        if (p.modelo() == null || p.modelo().isBlank()) {
            throw new IllegalArgumentException("Modelo é obrigatório");
        }
        if (p.precoUnitarioCentavos() != null && p.precoUnitarioCentavos() < 0) {
            throw new IllegalArgumentException("Preço não pode ser negativo");
        }
        if (p.ivaBasisPoints() != null && p.ivaBasisPoints() < 0) {
            throw new IllegalArgumentException("IVA não pode ser negativo");
        }
        if (p.totalRecebido() != null && p.totalRecebido() < 0) {
            throw new IllegalArgumentException("Estoque não pode ser negativo");
        }
    }
}