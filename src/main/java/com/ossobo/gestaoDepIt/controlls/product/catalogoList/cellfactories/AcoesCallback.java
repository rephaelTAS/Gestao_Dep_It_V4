package com.ossobo.gestaoDepIt.controlls.product.catalogoList.cellfactories;

import com.ossobo.gestaoDepIt.db.models.CatalogoProdutos;

/**
 * AcoesCallback v1.0
 *
 * Contrato entre a AcoesCellFactory e a camada que conhece as janelas
 * flutuantes (CatalogoProdutoListController). A cell factory não abre
 * janelas, não chama Rotas — apenas emite a intenção.
 */
public interface AcoesCallback {

    /** Abrir a tela de detalhes do produto. */
    void abrirDetalhes(CatalogoProdutos produto);

    /** Abrir a tela de edição do produto. */
    void abrirEdicao(CatalogoProdutos produto);
}