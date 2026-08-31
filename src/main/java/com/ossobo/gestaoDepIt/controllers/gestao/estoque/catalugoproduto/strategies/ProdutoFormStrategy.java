package com.ossobo.gestaoDepIt.controllers.gestao.estoque.catalugoproduto.strategies;

import com.ossobo.gestaoDepIt.db.models.CatalogoProdutos;
import com.ossobo.gestaoDepIt.utils.GerarIdProduto;
import java.util.Map;

/**
 * Interface Strategy para diferentes modos do formulário de produto
 * Princípio Open/Closed - Novos modos sem modificar código existente
 */
public interface ProdutoFormStrategy {

    // ===== CONFIGURAÇÃO INICIAL =====
    void configurarModo();

    // ===== VALIDAÇÃO ESPECÍFICA =====
    Map<String, String> validarCamposEspecificos();

    // ===== PREPARAÇÃO DE DADOS =====
    CatalogoProdutos prepararProdutoParaSalvar(CatalogoProdutos base);

    // ===== CONFIGURAÇÃO DE UI =====
    void configurarUI();

    // ===== LIMPEZA =====
    void limparModoEspecifico();

    // ===== GETTERS =====
    String getTitulo();
    boolean permiteAlterarSku();

    // ===== GERAÇÃO DE ID SEMÂNTICO =====
    String gerarIdSemantico();
    void setGeradorId(GerarIdProduto gerador);
    String getIdAtual();
}