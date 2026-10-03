package com.ossobo.gestaoDepIt.controlls.product.catalogoList;


import com.ossobo.gestaoDepIt.controlls.product.catalogoList.cellfactories.AcoesCallback;
import com.ossobo.gestaoDepIt.controlls.product.catalogoList.cellfactories.AcoesCellFactory;
import com.ossobo.gestaoDepIt.controlls.product.catalogoList.cellfactories.StatusCellFactory;
import com.ossobo.gestaoDepIt.db.models.CatalogoProdutos;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.math.BigDecimal;

/**
 * CatalogTableManager v2.4
 *
 * Responsabilidade: Configurar e gerenciar a TableView do catálogo —
 *                   mapeamento coluna↔campo do model, cell factories,
 *                   refresh e limpeza.
 *
 * v2.3 — Incorpora colIva e colPrecoTotal; 12 parâmetros em configurarColunas.
 * v2.4 — Quebra de contrato com AcoesCellFactory:
 *        - {@code CatalogActions} sai do construtor; entra {@link AcoesCallback}.
 *        - A cell factory de ações emite intenção (abrirDetalhes/abrirEdicao);
 *          quem sabe abrir janela é o controller da listagem, via callback.
 *        - {@code actions} mantido como campo opcional para uso futuro da tabela
 *          (ex.: seleção, dirty state) — não é passado à cell factory.
 *
 * @since v2.3
 */
public class CatalogTableManager {

    private final TableView<CatalogoProdutos> tabela;
    private final CatalogState state;
    private final AcoesCallback acoesCallback;

    private TableColumn<CatalogoProdutos, String> colSku;
    private TableColumn<CatalogoProdutos, String> colTipo;
    private TableColumn<CatalogoProdutos, String> colCategoria;
    private TableColumn<CatalogoProdutos, String> colMarca;
    private TableColumn<CatalogoProdutos, String> colModelo;
    private TableColumn<CatalogoProdutos, String> colCor;
    private TableColumn<CatalogoProdutos, String> colPrecoUnitario;
    private TableColumn<CatalogoProdutos, String> colIva;
    private TableColumn<CatalogoProdutos, String> colPrecoTotal;
    private TableColumn<CatalogoProdutos, String> colEstoque;
    private TableColumn<CatalogoProdutos, String> colStatus;
    private TableColumn<CatalogoProdutos, Void>   colAcoes;

    public CatalogTableManager(
            TableView<CatalogoProdutos> tabela,
            CatalogState state,
            AcoesCallback acoesCallback) {
        this.tabela = tabela;
        this.state = state;
        this.acoesCallback = acoesCallback;
    }

    /**
     * Configura referências das colunas.
     * Ordem dos parâmetros espelha a ordem visual do FXML.
     */
    public void configurarColunas(
            TableColumn<CatalogoProdutos, String> sku,
            TableColumn<CatalogoProdutos, String> tipo,
            TableColumn<CatalogoProdutos, String> categoria,
            TableColumn<CatalogoProdutos, String> marca,
            TableColumn<CatalogoProdutos, String> modelo,
            TableColumn<CatalogoProdutos, String> cor,
            TableColumn<CatalogoProdutos, String> precoUnitario,
            TableColumn<CatalogoProdutos, String> iva,
            TableColumn<CatalogoProdutos, String> precoTotal,
            TableColumn<CatalogoProdutos, String> estoque,
            TableColumn<CatalogoProdutos, String> status,
            TableColumn<CatalogoProdutos, Void>   acoes) {
        this.colSku = sku;
        this.colTipo = tipo;
        this.colCategoria = categoria;
        this.colMarca = marca;
        this.colModelo = modelo;
        this.colCor = cor;
        this.colPrecoUnitario = precoUnitario;
        this.colIva = iva;
        this.colPrecoTotal = precoTotal;
        this.colEstoque = estoque;
        this.colStatus = status;
        this.colAcoes = acoes;
    }

    /**
     * Configura a tabela completamente.
     */
    public void configurarTabelaCompleta() {
        tabela.setItems(state.getProdutosData());

        configurarTodasColunas();
        configurarCellFactories();
    }

    /**
     * Configura todas as colunas com cellValueFactory explícito.
     * SimpleStringProperty + fonte única do model para formatação.
     */
    private void configurarTodasColunas() {
        colSku.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().sku()));

        colTipo.setCellValueFactory(cellData -> {
            CatalogoProdutos p = cellData.getValue();
            return new SimpleStringProperty(
                    p.tipoProduto() != null ? p.tipoProduto().toString() : "");
        });

        colCategoria.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().categoria()));

        colMarca.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().marca()));

        colModelo.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().modelo()));

        colCor.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().cor()));

        colPrecoUnitario.setCellValueFactory(cellData -> {
            Integer centavos = cellData.getValue().precoUnitarioCentavos();
            if (centavos == null) {
                return new SimpleStringProperty("€ 0,00");
            }
            BigDecimal preco = BigDecimal.valueOf(centavos, 2);
            return new SimpleStringProperty(String.format("€ %.2f", preco));
        });

        colIva.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getIvaFormatado()));

        colPrecoTotal.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getPrecoTotalFormatado()));

        colEstoque.setCellValueFactory(cellData -> {
            Integer estoque = cellData.getValue().totalRecebido();
            return new SimpleStringProperty(estoque != null ? String.valueOf(estoque) : "0");
        });

        colStatus.setCellValueFactory(cellData -> {
            Boolean ativo = cellData.getValue().ativo();
            return new SimpleStringProperty(Boolean.TRUE.equals(ativo) ? "Ativo" : "Inativo");
        });
    }

    /**
     * Cell factories para renderização visual.
     * v2.4: AcoesCellFactory recebe AcoesCallback, não CatalogActions.
     */
    private void configurarCellFactories() {
        colStatus.setCellFactory(new StatusCellFactory());
        colAcoes.setCellFactory(new AcoesCellFactory(acoesCallback));
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