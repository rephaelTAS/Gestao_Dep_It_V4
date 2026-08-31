package com.ossobo.gestaoDepIt.controllers.gestao.estoque.catalugoproduto;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Window;
import com.ossobo.gestaoDepIt.controllers.gestao.estoque.catalugoproduto.catalogoList.*;
import com.ossobo.gestaoDepIt.db.models.CatalogoProdutos;
import com.ossobo.gestaoDepIt.db.services.CatalogoProdutosService;
import com.ossobo.nexusfx.di.annotations.Controller;
import com.ossobo.nexusfx.di.annotations.Inject;
import com.ossobo.nexusfx.NexusFX;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller principal para catálogo de produtos
 * ✅ Coordenação apenas (Single Responsibility)
 * ✅ Coesão Forte: <180 linhas
 * ✅ Zero Terminal Output
 * ✅ Usa NexusFX.alerts() exclusivamente
 */
@Controller
public class CatalogoProdutoListController implements Initializable {

    // ===== SERVIÇO (INJEÇÃO) =====
    @Inject
    private CatalogoProdutosService produtosService;

    // ===== COMPONENTES =====
    private final CatalogState state = new CatalogState();
    private CatalogFilters filters;
    private CatalogPagination pagination;
    private CatalogActions actions;
    private CatalogTableManager tableManager;
    private CatalogUIUpdater uiUpdater;

    // ===== COMPONENTES FXML =====
    @FXML private TableView<CatalogoProdutos> produtosTable;

    @FXML private TableColumn<CatalogoProdutos, String> colSku;
    @FXML private TableColumn<CatalogoProdutos, String> colTipo;
    @FXML private TableColumn<CatalogoProdutos, String> colCategoria;
    @FXML private TableColumn<CatalogoProdutos, String> colMarca;
    @FXML private TableColumn<CatalogoProdutos, String> colModelo;
    @FXML private TableColumn<CatalogoProdutos, String> colCor;
    @FXML private TableColumn<CatalogoProdutos, String> colPrecoUnitario;
    @FXML private TableColumn<CatalogoProdutos, String> colEstoque;
    @FXML private TableColumn<CatalogoProdutos, String> colStatus;
    @FXML private TableColumn<CatalogoProdutos, Void> colAcoes;

    @FXML private ComboBox<String> filtroTipoCombo;
    @FXML private TextField filtroCategoriaField;
    @FXML private TextField filtroMarcaField;
    @FXML private TextField filtroModeloField;
    @FXML private ComboBox<String> filtroEstoqueCombo;
    @FXML private ComboBox<String> filtroStatusCombo;

    @FXML private Button novoButton;
    @FXML private Button limparFiltrosButton;
    @FXML private Button aplicarFiltrosButton;

    @FXML private Button btnPrimeira;
    @FXML private Button btnAnterior;
    @FXML private Button btnProxima;
    @FXML private Button btnUltima;
    @FXML private Label labelPagina;
    @FXML private ComboBox<Integer> comboItensPorPagina;

    @FXML private Label statusLabel;
    @FXML private Label estatisticasLabel;
    @FXML private Label filtrosAtivosLabel;
    @FXML private ProgressIndicator progressIndicator;

    // ===== CONSTRUTOR =====
    public CatalogoProdutoListController() {
        // Construtor vazio - Zero Terminal Output
    }

    // ===== INITIALIZE =====
    /**
     * Inicialização do controller
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(this::initializeComponents);
    }

    // ===== INITIALIZE COMPONENTS =====
    /**
     * Inicializa todos os componentes
     */
    private void initializeComponents() {
        if (!verifyCriticalComponents()) {
            showCriticalError();
            return;
        }

        createComponents();
        configureComponents();
        loadInitialData();
    }

    // ===== VERIFY CRITICAL COMPONENTS =====
    /**
     * Verifica componentes críticos do FXML
     */
    private boolean verifyCriticalComponents() {
        return produtosTable != null &&
                colSku != null &&
                colStatus != null &&
                colAcoes != null &&
                novoButton != null;
    }

    // ===== CREATE COMPONENTS =====
    /**
     * Cria instâncias dos componentes
     */
    private void createComponents() {
        this.filters = new CatalogFilters(produtosService, state);
        this.pagination = new CatalogPagination(state);
        this.actions = new CatalogActions(produtosService, state);
        this.tableManager = new CatalogTableManager(produtosTable, state, actions);
        this.uiUpdater = new CatalogUIUpdater(state);
    }

    // ===== CONFIGURE COMPONENTS =====
    /**
     * Configura todos os componentes
     */
    private void configureComponents() {
        configureFilters();
        configurePagination();
        configureTableManager();
        configureUIUpdater();
    }

    // ===== CONFIGURE FILTERS =====
    /**
     * Configura componente de filtros
     */
    private void configureFilters() {
        filters.initializeUIComponents(
                filtroTipoCombo,
                filtroCategoriaField,
                filtroMarcaField,
                filtroModeloField,
                filtroEstoqueCombo,
                filtroStatusCombo
        );

        filters.configureBasicFilters();
    }

    // ===== CONFIGURE PAGINATION =====
    /**
     * Configura componente de paginação
     */
    private void configurePagination() {
        pagination.initializeUIComponents(
                btnPrimeira,
                btnAnterior,
                btnProxima,
                btnUltima,
                labelPagina,
                comboItensPorPagina
        );

        pagination.configurePagination();
    }

    // ===== CONFIGURE TABLE MANAGER =====
    /**
     * Configura gerenciador de tabela
     */
    private void configureTableManager() {
        tableManager.configurarColunas(
                colSku, colTipo, colCategoria, colMarca, colModelo,
                colCor, colPrecoUnitario, colEstoque, colStatus, colAcoes
        );

        tableManager.configurarTabelaCompleta();
    }

    // ===== CONFIGURE UI UPDATER =====
    /**
     * Configura atualizador de UI
     */
    private void configureUIUpdater() {
        uiUpdater.initializeComponents(
                statusLabel,
                estatisticasLabel,
                filtrosAtivosLabel,
                progressIndicator,
                novoButton,
                aplicarFiltrosButton,
                limparFiltrosButton
        );
    }

    // ===== LOAD INITIAL DATA =====
    /**
     * Carrega dados iniciais
     */
    private void loadInitialData() {
        uiUpdater.showProgress(true, "Carregando catálogo...");

        actions.carregarDados().thenRun(() -> {
            Platform.runLater(() -> {
                uiUpdater.showProgress(false, "Catálogo carregado");
                uiUpdater.calcularEstatisticas();
                pagination.updateUI();
            });
        });
    }

    // ===== HANDLERS DE AÇÃO =====
    @FXML
    private void handleNovoProduto() {
        Window owner = produtosTable.getScene().getWindow();
        actions.abrirNovoProduto(owner);
    }

    @FXML
    private void handleAplicarFiltros() {
        uiUpdater.showProgress(true, "Aplicando filtros...");
        filters.applyFiltersFromUI();

        actions.aplicarFiltros().thenRun(() -> {
            Platform.runLater(() -> {
                uiUpdater.showProgress(false, "Filtros aplicados");
                uiUpdater.calcularEstatisticas();
                uiUpdater.calcularFiltrosAtivos();
                pagination.resetToFirstPage();
            });
        });
    }

    @FXML
    private void handleLimparFiltros() {
        filters.clearFiltersUI();
        pagination.resetToFirstPage();
        loadInitialData();

        uiUpdater.updateStatus("Filtros limpos");
        uiUpdater.updateActiveFilters("");
    }

    @FXML
    private void handlePrimeiraPagina() {
        pagination.goToFirstPage();
        loadPageData();
    }

    @FXML
    private void handlePaginaAnterior() {
        pagination.goToPreviousPage();
        loadPageData();
    }

    @FXML
    private void handleProximaPagina() {
        pagination.goToNextPage();
        loadPageData();
    }

    @FXML
    private void handleUltimaPagina() {
        pagination.goToLastPage();
        loadPageData();
    }

    // ===== LOAD PAGE DATA =====
    /**
     * Carrega dados da página atual
     */
    private void loadPageData() {
        uiUpdater.showProgress(true, "Carregando página...");

        actions.carregarDados().thenRun(() -> {
            Platform.runLater(() -> {
                uiUpdater.showProgress(false, "");
                uiUpdater.calcularEstatisticas();
            });
        });
    }

    // ===== SHOW CRITICAL ERROR =====
    /**
     * Mostra erro crítico usando NexusFX
     */
    private void showCriticalError() {
        NexusFX.alerts().erro(
                "ERRO DE INICIALIZAÇÃO",
                "Componentes críticos não carregados.",
                "Detalhes Técnicos:\nFXML não carregou componentes necessários\n\nAção:\n1. Verificar arquivo FXML\n2. Contatar suporte",
                "Sistema"
        );
    }

    // ===== API PÚBLICA =====
    /**
     * Atualiza dados do controller
     */
    public void refreshData() {
        loadInitialData();
    }

    /**
     * Limpa recursos do controller
     */
    public void cleanup() {
        if (filters != null) filters.cleanup();
        if (pagination != null) pagination.cleanup();
        if (actions != null) actions.cleanup();
        if (tableManager != null) tableManager.cleanup();
        if (uiUpdater != null) uiUpdater.cleanup();
        if (state != null) state.cleanup();
    }
}