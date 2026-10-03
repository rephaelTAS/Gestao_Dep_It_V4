package com.ossobo.gestaoDepIt.controlls.product;

import com.ossobo.gestaoDepIt.config.ViewConstant;
import com.ossobo.gestaoDepIt.controlls.product.catalogoList.*;
import com.ossobo.gestaoDepIt.controlls.product.catalogoList.cellfactories.AcoesCallback;
import com.ossobo.gestaoDepIt.db.models.CatalogoProdutos;
import com.ossobo.gestaoDepIt.db.services.CatalogoProdutosService;
import com.ossobo.winterfx.anotations.Controller;
import com.ossobo.winterfx.anotations.ExecMapping;
import com.ossobo.winterfx.anotations.Inject;
import com.ossobo.winterfx.router.Rotas;
import com.ossobo.winterfx.router.model.Params;
import com.ossobo.winterfx.router.model.ResponseData;
import com.ossobo.winterfx.view.anotations.RegisterView;
import com.ossobo.winterfx.view.controller.WinterFXController;
import com.ossobo.winterfx.view.floatingwindow.StageForFloatingWindow;
import com.ossobo.winterfx.view.floatingwindow.anotations.FloatingWindow;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * CatalogoProdutoListController v1.7
 *
 * Responsabilidade: Coordenação da tela de listagem do catálogo.
 *
 * v1.7 — Correção de canal:
 *        - abrirDetalhes e abrirEdicao passam a usar Rotas.get (enviar dado
 *          para janela popular é LEITURA, não mutação).
 *        - Efeito colateral esperado: os receptores (CatalogoProdutoFormController
 *          e CatalogoProdutoDetailController) passam a expor @GetMapping.
 *
 * @since v1.6
 */
@Controller
@RegisterView(
        id = ViewConstant.Catalogo.LIST,
        fxml = "/META-INF/gestaoDepIt/fxmls/catalogo_produto/CatalogoProdutoList.fxml",
        title = "Catalogo Dos Produtos",
        primaryCss = "/META-INF/gestaoDepIt/css/catalugoproduto/CatalogoProdutoList.css"
)
public class CatalogoProdutoListController implements Initializable, WinterFXController, AcoesCallback {

    @Inject
    private CatalogoProdutosService produtosService;

    @FloatingWindow(viewId = ViewConstant.Catalogo.FORM, singleton = false)
    private StageForFloatingWindow catalogoForm;

    @FloatingWindow(viewId = ViewConstant.Catalogo.DETAIL, singleton = false)
    private StageForFloatingWindow catalogoDetail;

    // ===== COMPONENTES INTERNOS =====
    private final CatalogState state = new CatalogState();
    private CatalogFilters filters;
    private CatalogPagination pagination;
    private CatalogActions actions;
    private CatalogTableManager tableManager;
    private CatalogUIUpdater uiUpdater;

    // ===== FXML — TABELA =====
    @FXML private TableView<CatalogoProdutos> produtosTable;
    @FXML private TableColumn<CatalogoProdutos, String> colSku;
    @FXML private TableColumn<CatalogoProdutos, String> colTipo;
    @FXML private TableColumn<CatalogoProdutos, String> colCategoria;
    @FXML private TableColumn<CatalogoProdutos, String> colMarca;
    @FXML private TableColumn<CatalogoProdutos, String> colModelo;
    @FXML private TableColumn<CatalogoProdutos, String> colCor;
    @FXML private TableColumn<CatalogoProdutos, String> colPrecoUnitario;
    @FXML private TableColumn<CatalogoProdutos, String> colIva;
    @FXML private TableColumn<CatalogoProdutos, String> colPrecoTotal;
    @FXML private TableColumn<CatalogoProdutos, String> colEstoque;
    @FXML private TableColumn<CatalogoProdutos, String> colStatus;
    @FXML private TableColumn<CatalogoProdutos, Void>   colAcoes;

    // ===== FXML — FILTROS =====
    @FXML private ComboBox<String> filtroTipo;
    @FXML private TextField        filtroCategoria;
    @FXML private TextField        filtroMarca;
    @FXML private TextField        filtroModelo;
    @FXML private ComboBox<String> filtroEstoque;
    @FXML private ComboBox<String> filtroStatus;

    // ===== FXML — BOTÕES =====
    @FXML private Button btn_novo;
    @FXML private Button btn_limparFiltros;
    @FXML private Button btn_aplicarFiltros;

    // ===== FXML — PAGINAÇÃO =====
    @FXML private Button          btn_primeira;
    @FXML private Button          btn_anterior;
    @FXML private Button          btn_proxima;
    @FXML private Button          btn_ultima;
    @FXML private Label           labelPagina;
    @FXML private ComboBox<Integer> comboItensPorPagina;

    // ===== FXML — STATUS =====
    @FXML private Label             statusLabel;
    @FXML private Label             estatisticasLabel;
    @FXML private Label             filtrosAtivosLabel;
    @FXML private ProgressIndicator progressIndicator;

    public CatalogoProdutoListController() { }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(this::initializeComponents);
    }

    private void initializeComponents() {
        if (!verifyCriticalComponents()) { showCriticalError(); return; }
        createComponents();
        configureComponents();
        loadInitialData();
    }

    private boolean verifyCriticalComponents() {
        return produtosTable != null && colSku != null && colStatus != null
                && colAcoes != null && btn_novo != null;
    }

    private void createComponents() {
        this.filters      = new CatalogFilters(produtosService, state);
        this.pagination   = new CatalogPagination(state);
        this.actions      = new CatalogActions(state);
        this.tableManager = new CatalogTableManager(produtosTable, state, this);
        this.uiUpdater    = new CatalogUIUpdater(state);
    }

    private void configureComponents() {
        configureFilters();
        configurePagination();
        configureTableManager();
        configureUIUpdater();
    }

    private void configureFilters() {
        filters.initializeUIComponents(
                filtroTipo, filtroCategoria, filtroMarca,
                filtroModelo, filtroEstoque, filtroStatus
        );
        filters.configureBasicFilters();
    }

    private void configurePagination() {
        pagination.initializeUIComponents(
                btn_primeira, btn_anterior, btn_proxima, btn_ultima,
                labelPagina, comboItensPorPagina
        );
        pagination.configurePagination();
    }

    private void configureTableManager() {
        tableManager.configurarColunas(
                colSku, colTipo, colCategoria, colMarca, colModelo, colCor,
                colPrecoUnitario, colIva, colPrecoTotal, colEstoque, colStatus, colAcoes
        );
        tableManager.configurarTabelaCompleta();
    }

    private void configureUIUpdater() {
        uiUpdater.initializeComponents(
                statusLabel, estatisticasLabel, filtrosAtivosLabel,
                progressIndicator, btn_novo, btn_aplicarFiltros, btn_limparFiltros
        );
    }

    private void loadInitialData() {
        uiUpdater.showProgress(true, "Carregando catálogo...");
        actions.carregarDados().whenComplete((v, erro) -> Platform.runLater(() -> {
            if (erro != null) {
                uiUpdater.showProgress(false, "Falha ao carregar: " + erro.getMessage());
                System.getLogger("catalogo").log(System.Logger.Level.ERROR, "carregarDados falhou", erro);
                return;
            }
            uiUpdater.showProgress(false, "Catálogo carregado");
            uiUpdater.calcularEstatisticas();
            pagination.updateUI();
        }));
    }

    private void loadPageData() {
        uiUpdater.showProgress(true, "Carregando página...");
        actions.carregarDados().whenComplete((v, erro) -> Platform.runLater(() -> {
            uiUpdater.showProgress(false, "");
            if (erro != null) { uiUpdater.updateStatus("Falha: " + erro.getMessage()); return; }
            uiUpdater.calcularEstatisticas();
        }));
    }

    // ===== MÉTODOS DE AÇÃO (nome = fx:id do FXML) =====

    public void btn_novo(ActionEvent event) {
        catalogoForm.show();
    }

    public void btn_aplicarFiltros(ActionEvent event) {
        uiUpdater.showProgress(true, "Aplicando filtros...");
        filters.applyFiltersFromUI();

        actions.aplicarFiltros().whenComplete((v, erro) -> Platform.runLater(() -> {
            if (erro != null) { uiUpdater.showProgress(false, "Falha: " + erro.getMessage()); return; }
            uiUpdater.showProgress(false, "Filtros aplicados");
            uiUpdater.calcularEstatisticas();
            uiUpdater.calcularFiltrosAtivos();
            pagination.resetToFirstPage();
        }));
    }

    public void btn_limparFiltros(ActionEvent event) {
        filters.clearFiltersUI();
        pagination.resetToFirstPage();
        loadInitialData();
        uiUpdater.updateStatus("Filtros limpos");
        uiUpdater.updateActiveFilters("");
    }

    public void btn_primeira(ActionEvent event) { pagination.goToFirstPage();    loadPageData(); }
    public void btn_anterior(ActionEvent event) { pagination.goToPreviousPage(); loadPageData(); }
    public void btn_proxima(ActionEvent event)  { pagination.goToNextPage();     loadPageData(); }
    public void btn_ultima(ActionEvent event)   { pagination.goToLastPage();     loadPageData(); }

    // ===== AcoesCallback =====

    @Override
    public void abrirDetalhes(CatalogoProdutos produto) {
        if (produto == null || catalogoDetail == null) return;
        catalogoDetail.show();
        // GET: enviar sku para a janela de detalhes popular — leitura.
        Rotas.get("catalogo/details/detalhes/produto",
                Params.with("sku", produto.sku()));
    }

    @Override
    public void abrirEdicao(CatalogoProdutos produto) {
        if (produto == null || catalogoForm == null) return;
        catalogoForm.show();
        // GET: enviar sku para o form entrar em modo edição — leitura.
        Rotas.get("catalogo/produtoform/form/editar/sku",
                Params.with("sku", produto.sku()));
    }

    // ===== ROTA DE RECARGA =====

    @ExecMapping("catalogo/list/refresh")
    public ResponseData refresh() {
        Platform.runLater(this::loadInitialData);
        return ResponseData.success();
    }

    private void showCriticalError() { }

    public void refreshData() { loadInitialData(); }

    public void cleanup() {
        if (filters != null)      filters.cleanup();
        if (pagination != null)   pagination.cleanup();
        if (actions != null)      actions.cleanup();
        if (tableManager != null) tableManager.cleanup();
        if (uiUpdater != null)    uiUpdater.cleanup();
        if (state != null)        state.cleanup();
    }
}