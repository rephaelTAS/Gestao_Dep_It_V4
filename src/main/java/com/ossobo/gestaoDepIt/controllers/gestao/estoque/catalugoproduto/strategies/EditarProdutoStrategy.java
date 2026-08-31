package com.ossobo.gestaoDepIt.controllers.gestao.estoque.catalugoproduto.strategies;

import com.ossobo.gestaoDepIt.db.models.CatalogoProdutos;
import com.ossobo.gestaoDepIt.db.services.CatalogoProdutosService;
import com.ossobo.gestaoDepIt.utils.GerarIdProduto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Estratégia para edição de produto existente usando SKU como identificador.
 * Alinhada com arquitetura SKU-based (SKU é chave primária).
 *
 * Responsabilidade única: Lógica específica de edição por SKU
 */
public class EditarProdutoStrategy implements ProdutoFormStrategy {

    private static final Logger logger = LoggerFactory.getLogger(EditarProdutoStrategy.class);

    private final CatalogoProdutosService produtosService;
    private GerarIdProduto geradorId;
    private String sku; // ✅ Usando SKU em vez de ID
    private CatalogoProdutos produtoOriginal;

    // ===== CONSTRUTORES =====

    /**
     * ✅ CONSTRUTOR PRINCIPAL - Arquitetura SKU-based
     * @param sku SKU do produto a editar (chave primária real)
     * @param produtosService Serviço para operações de produto
     */
    public EditarProdutoStrategy(String sku, CatalogoProdutosService produtosService) {
        if (sku == null || sku.trim().isEmpty()) {
            throw new IllegalArgumentException("SKU não pode ser nulo ou vazio");
        }
        if (produtosService == null) {
            throw new IllegalStateException("CatalogoProdutosService não pode ser null");
        }

        this.sku = sku.trim();
        this.produtosService = produtosService;

        logger.debug("Estratégia de edição criada para SKU: {}", sku);
    }

    /**
     * 🔴 CONSTRUTOR DE COMPATIBILIDADE - DEPRECATED
     * @deprecated Use {@link #EditarProdutoStrategy(String, CatalogoProdutosService)} em vez disso
     * @param produtoId ID do produto (não mais usado como chave primária)
     * @param produtosService Serviço para operações de produto
     */
    @Deprecated
    public EditarProdutoStrategy(Long produtoId, CatalogoProdutosService produtosService) {
        logger.warn("⚠️ CONSTRUTOR DEPRECATED: EditarProdutoStrategy({}, ...)", produtoId);

        if (produtosService == null) {
            throw new IllegalStateException("CatalogoProdutosService não pode ser null");
        }

        this.produtosService = produtosService;

        // Tentar converter ID para SKU (compatibilidade temporária)
        this.sku = converterIdParaSku(produtoId);

        if (this.sku == null) {
            throw new IllegalArgumentException(
                    "Não foi possível encontrar produto para ID: " + produtoId +
                            ". Use SKU em vez de ID."
            );
        }

        logger.warn("⚠️ ID {} convertido para SKU: {} (compatibilidade)", produtoId, sku);
    }

    // ===== CONFIGURAÇÃO =====
    @Override
    public void configurarModo() {
        logger.info("Configurando modo: EDITAR PRODUTO SKU: {}", sku);

        if (produtosService == null) {
            throw new IllegalStateException("Serviço não inicializado");
        }

        carregarProdutoOriginal();
    }

    // ===== VALIDAÇÃO =====
    @Override
    public Map<String, String> validarCamposEspecificos() {
        Map<String, String> erros = new HashMap<>();

        if (produtoOriginal == null) {
            erros.put("geral", "Produto não encontrado para edição: " + sku);
        }

        if (produtoOriginal != null && !produtoOriginal.ativo()) {
            erros.put("status", "Não é possível editar produto inativo");
        }

        return erros;
    }

    // ===== PREPARAÇÃO PARA SALVAR =====
    @Override
    public CatalogoProdutos prepararProdutoParaSalvar(CatalogoProdutos modificado) {
        if (produtoOriginal == null) {
            throw new IllegalStateException("Produto original não carregado");
        }

        // ✅ Preserva dados imutáveis do original
        // NOTA: Não usamos setId() porque o modelo real não tem campo 'id' como chave primária
        modificado.sku(produtoOriginal.sku()); // SKU não muda (chave primária)
        modificado.setCreatedAt(produtoOriginal.getCreatedAt());

        // Atualizar timestamp
        modificado.setUpdatedAt(LocalDateTime.now());

        logger.debug("Produto preparado para atualização SKU: {}", produtoOriginal.getSku());
        return modificado;
    }

    // ===== CONFIGURAÇÃO UI =====
    @Override
    public void configurarUI() {
        // UI específica para edição:
        // - SKU é readonly
        // - Botão "Histórico" visível
        // - Status de "Produto existente"
    }

    // ===== LIMPEZA =====
    @Override
    public void limparModoEspecifico() {
        sku = null;
        produtoOriginal = null;
        geradorId = null;
    }

    // ===== GETTERS =====
    @Override
    public String getTitulo() {
        return produtoOriginal != null ?
                "Editar Produto - " + produtoOriginal.getSku() :
                "Editar Produto - " + sku;
    }

    @Override
    public boolean permiteAlterarSku() {
        return false; // SKU é imutável em edição (chave primária)
    }

    // ===== MÉTODOS DE ID SEMÂNTICO =====
    @Override
    public String gerarIdSemantico() {
        // Em edição, não gera novo ID - mantém o existente
        return produtoOriginal != null ? produtoOriginal.getSku() : sku;
    }

    @Override
    public void setGeradorId(GerarIdProduto gerador) {
        this.geradorId = gerador;
    }

    @Override
    public String getIdAtual() {
        return produtoOriginal != null ? produtoOriginal.getSku() : sku;
    }

    // ===== MÉTODOS ESPECÍFICOS =====
    /**
     * ✅ Carrega produto usando SKU (chave primária real)
     */
    private void carregarProdutoOriginal() {
        try {
            Optional<CatalogoProdutos> produtoOpt = produtosService.buscarPorSku(sku);

            if (produtoOpt.isEmpty()) {
                throw new IllegalArgumentException("Produto não encontrado para SKU: " + sku);
            }

            produtoOriginal = produtoOpt.get();
            logger.debug("Produto carregado para edição SKU: {}", sku);

        } catch (Exception e) {
            logger.error("Erro ao carregar produto SKU: {}", sku, e);
            throw new RuntimeException("Erro ao carregar produto: " + e.getMessage(), e);
        }
    }

    /**
     * 🔴 Método de compatibilidade temporária - REMOVER QUANDO POSSÍVEL
     * Converte ID para SKU buscando no serviço.
     */
    private String converterIdParaSku(Long produtoId) {
        if (produtoId == null) {
            return null;
        }

        try {
            // ❌ MÉTODO DEPRECATED - usar apenas para compatibilidade
            // Em uma refatoração completa, remover este método
            Optional<CatalogoProdutos> produtoOpt = produtosService.buscarPorId(produtoId);

            if (produtoOpt.isPresent()) {
                return produtoOpt.get().getSku();
            }
        } catch (Exception e) {
            logger.error("Erro ao converter ID {} para SKU: {}", produtoId, e.getMessage());
        }

        return null;
    }

    // ===== GETTERS ESPECÍFICOS =====
    public CatalogoProdutos getProdutoOriginal() {
        return produtoOriginal;
    }

    public String getSku() {
        return sku;
    }

}