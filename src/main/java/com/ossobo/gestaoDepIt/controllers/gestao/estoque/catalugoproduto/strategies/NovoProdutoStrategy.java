package com.ossobo.gestaoDepIt.controllers.gestao.estoque.catalugoproduto.strategies;

import com.ossobo.gestaoDepIt.db.models.CatalogoProdutos;
import com.ossobo.gestaoDepIt.db.services.CatalogoProdutosService;
import com.ossobo.gestaoDepIt.utils.GerarIdProduto;
import com.ossobo.winterfx.anotations.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Estratégia para criação de novo produto
 * Responsabilidade única: Lógica específica de criação
 */
public class NovoProdutoStrategy implements ProdutoFormStrategy {

    private static final Logger logger = LoggerFactory.getLogger(NovoProdutoStrategy.class);

    @Inject
    private CatalogoProdutosService produtosService;

    private GerarIdProduto geradorId;
    private String idSemantico;
    private String categoria;
    private String marca;
    private String modelo;

    // ===== CONFIGURAÇÃO =====
    @Override
    public void configurarModo() {
        logger.info("Configurando modo: NOVO PRODUTO");
    }

    // ===== VALIDAÇÃO =====
    @Override
    public Map<String, String> validarCamposEspecificos() {
        Map<String, String> erros = new HashMap<>();

        // Valida se pode gerar ID semântico
        if (categoria != null && marca != null && modelo != null) {
            try {
                gerarIdSemantico();
                if (idSemantico == null || idSemantico.trim().isEmpty()) {
                    erros.put("id", "Não foi possível gerar ID semântico");
                }
            } catch (Exception e) {
                erros.put("id", "Erro ao gerar ID semântico: " + e.getMessage());
            }
        }

        return erros;
    }

    // ===== PREPARAÇÃO PARA SALVAR =====
    @Override
    public CatalogoProdutos prepararProdutoParaSalvar(CatalogoProdutos base) {
        // Configurações específicas para novo produto
        base.createdAt(LocalDateTime.now());
        base.updatedAt(LocalDateTime.now());

        // Configurações padrão para novos produtos
        if (base.ativo() == null) {
            base.ativo(true);
        }

        logger.debug("Produto preparado para criação. ID: {}", idSemantico);
        return base;
    }

    // ===== CONFIGURAÇÃO UI =====
    @Override
    public void configurarUI() {
        // UI específica para novo produto
        // SKU será preenchido automaticamente quando houver dados suficientes
    }

    // ===== LIMPEZA =====
    @Override
    public void limparModoEspecifico() {
        idSemantico = null;
        categoria = null;
        marca = null;
        modelo = null;
        geradorId = null;
    }

    // ===== GETTERS =====
    @Override
    public String getTitulo() {
        return "Novo Produto no Catálogo";
    }

    @Override
    public boolean permiteAlterarSku() {
        return false; // SKU é gerado automaticamente
    }

    // ===== MÉTODOS DE ID SEMÂNTICO =====
    @Override
    public String gerarIdSemantico() {
        if (categoria != null && marca != null && modelo != null &&
                !categoria.trim().isEmpty() && !marca.trim().isEmpty() && !modelo.trim().isEmpty()) {

            idSemantico = geradorId.gerarIdProduto(categoria, marca, modelo);
            logger.debug("ID semântico gerado: {}", idSemantico);
            return idSemantico;
        }
        return null;
    }

    @Override
    public void setGeradorId(GerarIdProduto gerador) {
        this.geradorId = gerador;
    }

    @Override
    public String getIdAtual() {
        return idSemantico;
    }

    // ===== GETTERS PARA CONTROLLER =====
    public String getCategoria() {
        return categoria;
    }

    public String getMarca() {
        return marca;
    }

    public String getModelo() {
        return modelo;
    }

    // ===== SETTERS =====
    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }
}