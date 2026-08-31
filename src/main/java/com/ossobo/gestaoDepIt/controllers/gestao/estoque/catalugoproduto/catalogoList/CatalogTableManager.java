package com.ossobo.gestaoDepIt.controllers.gestao.estoque.catalugoproduto.catalogoList;

import com.ossobo.gestaoDepIt.controllers.gestao.estoque.catalugoproduto.catalogoList.cellfactories.AcoesCellFactory;
import com.ossobo.gestaoDepIt.controllers.gestao.estoque.catalugoproduto.catalogoList.cellfactories.StatusCellFactory;
import com.ossobo.gestaoDepIt.db.models.CatalogoProdutos;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.*;
import java.math.BigDecimal;

/**
 * Gerencia toda a configuração e comportamento da tabela.
 * v2.2 - Substitui PropertyValueFactory por SimpleStringProperty (confiável).
 * Responsabilidades: Configurar colunas, cell factories, double-click, refresh.
 */
public class CatalogTableManager {

    private final TableView<CatalogoProdutos> tabela;
    private final CatalogState state;
    private final CatalogActions actions;

    private TableColumn<CatalogoProdutos, String> colSku;
    private TableColumn<CatalogoProdutos, String> colTipo;
    private TableColumn<CatalogoProdutos, String> colCategoria;
    private TableColumn<CatalogoProdutos, String> colMarca;
    private TableColumn<CatalogoProdutos, String> colModelo;
    private TableColumn<CatalogoProdutos, String> colCor;
    private TableColumn<CatalogoProdutos, String> colPrecoUnitario;
    private TableColumn<CatalogoProdutos, String> colEstoque;
    private TableColumn<CatalogoProdutos, String> colStatus;
    private TableColumn<CatalogoProdutos, Void> colAcoes;

    public CatalogTableManager(
            TableView<CatalogoProdutos> tabela,
            CatalogState state,
            CatalogActions actions) {
        this.tabela = tabela;
        this.state = state;
        this.actions = actions;
    }

    /**
     * Configura referências das colunas.
     */
    public void configurarColunas(
            TableColumn<CatalogoProdutos, String> sku,
            TableColumn<CatalogoProdutos, String> tipo,
            TableColumn<CatalogoProdutos, String> categoria,
            TableColumn<CatalogoProdutos, String> marca,
            TableColumn<CatalogoProdutos, String> modelo,
            TableColumn<CatalogoProdutos, String> cor,
            TableColumn<CatalogoProdutos, String> precoUnitario,
            TableColumn<CatalogoProdutos, String> estoque,
            TableColumn<CatalogoProdutos, String> status,
            TableColumn<CatalogoProdutos, Void> acoes) {
        this.colSku = sku;
        this.colTipo = tipo;
        this.colCategoria = categoria;
        this.colMarca = marca;
        this.colModelo = modelo;
        this.colCor = cor;
        this.colPrecoUnitario = precoUnitario;
        this.colEstoque = estoque;
        this.colStatus = status;
        this.colAcoes = acoes;
    }

    /**
     * Configura a tabela completamente.
     */
    public void configurarTabelaCompleta() {
        // Garantir que a ObservableList do state está bindada
        tabela.setItems(state.getProdutosData());

        configurarTodasColunas();
        configurarCellFactories();
        configurarDoubleClick();
    }

    /**
     * Configura todas as colunas com cellValueFactory explícito.
     * v2.2: SimpleStringProperty em vez de PropertyValueFactory.
     */
    private void configurarTodasColunas() {
        // SKU
        colSku.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().sku()));

        // Tipo (Enum → String)
        colTipo.setCellValueFactory(cellData -> {
            CatalogoProdutos p = cellData.getValue();
            return new SimpleStringProperty(
                    p.tipoProduto() != null ? p.tipoProduto().toString() : "");
        });

        // Categoria
        colCategoria.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().categoria()));

        // Marca
        colMarca.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().marca()));

        // Modelo
        colModelo.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().modelo()));

        // Cor
        colCor.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().cor()));

        // Preço formatado
        colPrecoUnitario.setCellValueFactory(cellData -> {
            BigDecimal preco = cellData.getValue().precoUnitario();
            return new SimpleStringProperty(preco != null ?
                    String.format("€ %.2f", preco) : "€ 0,00");
        });

        // Estoque
        colEstoque.setCellValueFactory(cellData -> {
            Integer estoque = cellData.getValue().totalRecebido();
            return new SimpleStringProperty(estoque != null ? String.valueOf(estoque) : "0");
        });

        // Status (Boolean → "Ativo"/"Inativo")
        colStatus.setCellValueFactory(cellData -> {
            Boolean ativo = cellData.getValue().ativo();
            return new SimpleStringProperty(Boolean.TRUE.equals(ativo) ? "Ativo" : "Inativo");
        });
    }

    /**
     * Cell factories para renderização visual.
     */
    private void configurarCellFactories() {
        colStatus.setCellFactory(new StatusCellFactory());
        colAcoes.setCellFactory(new AcoesCellFactory(actions));
    }

    /**
     * Double-click abre detalhes.
     */
    private void configurarDoubleClick() {
        tabela.setRowFactory(tv -> {
            TableRow<CatalogoProdutos> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    CatalogoProdutos produto = row.getItem();
                    if (produto != null && tabela.getScene() != null) {
                        actions.abrirDetalhesProduto(produto, tabela.getScene().getWindow());
                    }
                }
            });
            return row;
        });
    }

    /**
     * Força refresh visual da tabela.
     */
    public void refresh() {
        if (tabela != null) {
            tabela.refresh();
        }
    }

    /**
     * Limpa dados visuais da tabela.
     */
    public void clear() {
        if (tabela != null && tabela.getItems() != null) {
            tabela.getItems().clear();
        }
    }

    public javafx.stage.Window getWindow() {
        return tabela != null && tabela.getScene() != null
                ? tabela.getScene().getWindow() : null;
    }

    /**
     * Limpa recursos.
     */
    public void cleanup() {
        if (tabela != null) {
            tabela.getItems().clear();
            tabela.setItems(null);
        }
    }
}