package com.ossobo.gestaoDepIt.controlls.inventario;

import com.ossobo.gestaoDepIt.config.ViewConstant;
import com.ossobo.gestaoDepIt.controlls.inventario.cellfactories.InventarioAcoesCallback;
import com.ossobo.gestaoDepIt.controlls.inventario.inventarioList.InventarioActions;
import com.ossobo.gestaoDepIt.controlls.inventario.inventarioList.InventarioFilters;
import com.ossobo.gestaoDepIt.controlls.inventario.inventarioList.InventarioPagination;
import com.ossobo.gestaoDepIt.controlls.inventario.inventarioList.InventarioState;
import com.ossobo.gestaoDepIt.controlls.inventario.inventarioList.InventarioTableManager;
import com.ossobo.gestaoDepIt.controlls.inventario.inventarioList.InventarioUIUpdater;
import com.ossobo.gestaoDepIt.db.models.InventarioEquipamentos;
import com.ossobo.winterfx.anotations.Controller;
import com.ossobo.winterfx.anotations.ExecMapping;
import com.ossobo.winterfx.anotations.GetMapping;
import com.ossobo.winterfx.anotations.RequestMapping;
import com.ossobo.winterfx.anotations.RouteVar;
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
 * InventarioEquipamentosListController v2.2
 *
 * Responsabilidade: Coordenação da tela de listagem do inventário.
 *                   Delega estado, filtros, paginação, ações, tabela e
 *                   atualização de UI para os componentes do subpacote
 *                   {@code inventarioList}.
 *
 * Padrões ratificados:
 *  - Zero service injetado — tudo via Rotas (inventario-equipamentos/service/*).
 *  - FXML é fonte da nomenclatura: campo @FXML = fx:id; método = fx:id.
 *  - show() antes do Rotas.get(...) ao abrir janelas flutuantes.
 *  - Ações da tabela emitidas via InventarioAcoesCallback (3 botões).
 *  - Canais corretos: GET para enviar dados à view; PUT para mutação;
 *    EXEC para comando; DELETE para remoção.
 *
 * v2.2 — Rotas de delegação para o Detail:
 *        - @GetMapping("inventario-list/abrir-edicao")  → o Detail chama
 *          para pedir a abertura do form em modo edição.
 *        - @GetMapping("inventario-list/abrir-historico") → o Detail chama
 *          para abrir a janela de histórico pré-filtrada.
 *        - Emissores internos ajustados para Rotas.get (sem mutação).
 *
 * @since v2.1
 */
@Controller
@RegisterView(
        id = ViewConstant.Inventario.LIST,
        fxml = "/META-INF/gestaoDepIt/fxmls/inventario/InventarioEquipamentosView.fxml",
        title = "Inventário de Equipamentos",
        primaryCss = "/META-INF/gestaoDepIt/css/inventario/InventarioEquipamentosView.css"
)
@RequestMapping("inventario-list")
public class InventarioEquipamentosListController implements Initializable, WinterFXController, InventarioAcoesCallback {

    // ===== FLOATING WINDOWS =====
    @FloatingWindow(viewId = ViewConstant.Inventario.FORM, singleton = false)
    private StageForFloatingWindow inventarioForm;

    @FloatingWindow(viewId = ViewConstant.Inventario.DETAIL, singleton = false)
    private StageForFloatingWindow inventarioDetail;

    @FloatingWindow(viewId = ViewConstant.Inventario.HISTORICO_LIST, singleton = false)
    private StageForFloatingWindow historicoList;

    // ===== FXML — TABELA =====
    @FXML private TableView<InventarioEquipamentos> equipamentosTable;
    @FXML private TableColumn<InventarioEquipamentos, String> colId;
    @FXML private TableColumn<InventarioEquipamentos, String> colNumSerie;
    @FXML private TableColumn<InventarioEquipamentos, String> colEnderecoMac;
    @FXML private TableColumn<InventarioEquipamentos, String> colSkuProduto;
    @FXML private TableColumn<InventarioEquipamentos, String> colFuncionarioId;
    @FXML private TableColumn<InventarioEquipamentos, String> colLocalizacao;
    @FXML private TableColumn<InventarioEquipamentos, String> colDepartamento;
    @FXML private TableColumn<InventarioEquipamentos, String> colStatus;
    @FXML private TableColumn<InventarioEquipamentos, String> colCondicao;
    @FXML private TableColumn<InventarioEquipamentos, String> colDataAquisicao;
    @FXML private TableColumn<InventarioEquipamentos, String> colDataInstalacao;
    @FXML private TableColumn<InventarioEquipamentos, String> colDataUltimaVerificacao;
    @FXML private TableColumn<InventarioEquipamentos, Void>   colAcoes;

    // ===== FXML — FILTROS =====
    @FXML private TextField        filtroMac;
    @FXML private ComboBox<String> filtroStatus;
    @FXML private ComboBox<String> filtroCondicao;
    @FXML private ComboBox<String> filtroLocalizacao;
    @FXML private ComboBox<String> filtroDepartamento;
    @FXML private TextField        filtroBusca;
    @FXML private DatePicker       filtroAquisicaoInicio;
    @FXML private DatePicker       filtroAquisicaoFim;
    @FXML private DatePicker       filtroInstalacaoInicio;
    @FXML private DatePicker       filtroInstalacaoFim;
    @FXML private DatePicker       filtroVerificacaoInicio;
    @FXML private DatePicker       filtroVerificacaoFim;

    // ===== FXML — BOTÕES =====
    @FXML private Button btn_novo;
    @FXML private Button btn_historico;
    @FXML private Button btn_aplicarFiltros;
    @FXML private Button btn_limparFiltros;

    // ===== FXML — PAGINAÇÃO =====
    @FXML private Button          btn_primeira;
    @FXML private Button          btn_anterior;
    @FXML private Button          btn_proxima;
    @FXML private Button          btn_ultima;
    @FXML private Label           labelPagina;
    @FXML private ComboBox<Integer> comboItensPorPagina;

    // ===== FXML — STATS / STATUS =====
    @FXML private Label             statTotal;
    @FXML private Label             statAtivos;
    @FXML private Label             statManutencao;
    @FXML private Label             statCriticos;
    @FXML private Label             statSemMac;
    @FXML private Label             statusLabel;
    @FXML private ProgressIndicator progressIndicator;

    // ===== COMPONENTES INTERNOS =====
    private final InventarioState state = new InventarioState();
    private InventarioFilters filters;
    private InventarioPagination pagination;
    private InventarioActions actions;
    private InventarioTableManager tableManager;
    private InventarioUIUpdater uiUpdater;

    public InventarioEquipamentosListController() { }

    // ===== INITIALIZE =====
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(this::initializeComponents);
    }

    private void initializeComponents() {
        if (!verifyCriticalComponents()) {
            System.getLogger("inventario").log(System.Logger.Level.ERROR,
                    "Componentes críticos ausentes no FXML do inventário");
            return;
        }
        createComponents();
        configureComponents();
        loadInitialData();
    }

    private boolean verifyCriticalComponents() {
        return equipamentosTable != null
                && colNumSerie != null
                && colStatus != null
                && colAcoes != null
                && btn_novo != null
                && progressIndicator != null;
    }

    // ===== CREATE COMPONENTS =====
    private void createComponents() {
        this.filters      = new InventarioFilters();
        this.pagination   = new InventarioPagination(state);
        this.actions      = new InventarioActions(state);
        this.tableManager = new InventarioTableManager(equipamentosTable, state, this);
        this.uiUpdater    = new InventarioUIUpdater(state);
    }

    // ===== CONFIGURE COMPONENTS =====
    private void configureComponents() {
        configureFilters();
        configurePagination();
        configureTableManager();
        configureUIUpdater();
    }

    private void configureFilters() {
        filters.initializeUIComponents(
                filtroMac, filtroStatus, filtroCondicao,
                filtroLocalizacao, filtroDepartamento, filtroBusca,
                filtroAquisicaoInicio, filtroAquisicaoFim,
                filtroInstalacaoInicio, filtroInstalacaoFim,
                filtroVerificacaoInicio, filtroVerificacaoFim
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
                colId, colNumSerie, colEnderecoMac, colSkuProduto,
                colFuncionarioId, colLocalizacao, colDepartamento,
                colStatus, colCondicao,
                colDataAquisicao, colDataInstalacao, colDataUltimaVerificacao,
                colAcoes
        );
        tableManager.configurarTabelaCompleta();
    }

    private void configureUIUpdater() {
        uiUpdater.initializeComponents(
                statusLabel, null, progressIndicator,
                btn_novo, btn_aplicarFiltros, btn_limparFiltros,
                statTotal, statAtivos, statManutencao, statCriticos, statSemMac
        );
    }

    // ===== LOAD =====
    private void loadInitialData() {
        uiUpdater.showProgress(true, "Carregando inventário...");
        actions.carregarDados().whenComplete((v, erro) -> Platform.runLater(() -> {
            if (erro != null) {
                uiUpdater.showProgress(false, "Falha ao carregar: " + erro.getMessage());
                System.getLogger("inventario").log(System.Logger.Level.ERROR, "carregarDados falhou", erro);
                return;
            }
            uiUpdater.showProgress(false, "Inventário carregado");
            uiUpdater.calcularEstatisticas();
            pagination.updateUI();
        }));
    }

    private void loadPageData() {
        uiUpdater.showProgress(true, "Carregando página...");
        actions.carregarDados().whenComplete((v, erro) -> Platform.runLater(() -> {
            uiUpdater.showProgress(false, "");
            if (erro != null) {
                uiUpdater.updateStatus("Falha: " + erro.getMessage());
                return;
            }
            uiUpdater.calcularEstatisticas();
            pagination.updateUI();
        }));
    }

    // ===== MÉTODOS DE AÇÃO (nome = fx:id) =====

    public void btn_novo(ActionEvent event) {
        if (inventarioForm == null) return;
        inventarioForm.show();
    }

    /**
     * Botão "Histórico" do cabeçalho — abre a janela SEM filtro aplicado.
     */
    public void btn_historico(ActionEvent event) {
        if (historicoList == null) return;
        historicoList.show();
    }

    public void btn_aplicarFiltros(ActionEvent event) {
        uiUpdater.showProgress(true, "Aplicando filtros...");
        var params = filters.paramsDeFiltros();

        actions.aplicarFiltros(params).whenComplete((v, erro) -> Platform.runLater(() -> {
            if (erro != null) {
                uiUpdater.showProgress(false, "Falha: " + erro.getMessage());
                return;
            }
            uiUpdater.showProgress(false, "Filtros aplicados");
            uiUpdater.calcularEstatisticas();
            pagination.resetToFirstPage();
        }));
    }

    public void btn_limparFiltros(ActionEvent event) {
        filters.clearFiltersUI();
        pagination.resetToFirstPage();
        loadInitialData();
        uiUpdater.updateStatus("Filtros limpos");
    }

    public void btn_primeira(ActionEvent event) { pagination.goToFirstPage();    loadPageData(); }
    public void btn_anterior(ActionEvent event) { pagination.goToPreviousPage(); loadPageData(); }
    public void btn_proxima(ActionEvent event)  { pagination.goToNextPage();     loadPageData(); }
    public void btn_ultima(ActionEvent event)   { pagination.goToLastPage();     loadPageData(); }

    // ===== InventarioAcoesCallback =====

    @Override
    public void abrirDetalhes(InventarioEquipamentos equipamento) {
        if (equipamento == null || inventarioDetail == null) return;
        inventarioDetail.show();
        Rotas.get("inventario-detail/carregar/id",
                Params.with("id", equipamento.id()));
    }

    @Override
    public void abrirEdicao(InventarioEquipamentos equipamento) {
        if (equipamento == null || inventarioForm == null) return;
        inventarioForm.show();
        Rotas.get("inventario-form/editar/id",
                Params.with("id", equipamento.id()));
    }

    @Override
    public void abrirHistorico(InventarioEquipamentos equipamento) {
        if (equipamento == null || historicoList == null) return;
        historicoList.show();
        Rotas.get("historico-list/aplicar-prefiltro",
                Params.with("sku", nvl(equipamento.skuProduto()))
                        .and("funcionarioid", nvl(equipamento.funcionarioId())));
    }

    // ===== ROTAS DE DELEGAÇÃO (chamadas por outras views) =====

    /**
     * Rota acionada pelo Detail ("Editar"). O Detail não tem
     * {@code @FloatingWindow} do form — o list tem. Aqui o list abre o form
     * e injeta o id para modo edição.
     */
    @GetMapping("abrir-edicao")
    public ResponseData abrirEdicaoPorId(@RouteVar("id") String id) {
        if (id == null || id.isBlank() || inventarioForm == null) {
            return ResponseData.error("ID inválido ou janela indisponível");
        }
        Platform.runLater(() -> {
            inventarioForm.show();
            Rotas.get("inventario-form/editar/id", Params.with("id", id));
        });
        return ResponseData.success();
    }

    /**
     * Rota acionada pelo Detail ("Histórico"). Abre a janela de histórico
     * com pré-filtro (sku + funcionarioid).
     */
    @GetMapping("abrir-historico")
    public ResponseData abrirHistoricoPorContexto(@RouteVar("sku") String sku,
                                                  @RouteVar("funcionarioid") String funcionarioId) {
        if (historicoList == null) {
            return ResponseData.error("Janela de histórico indisponível");
        }
        Platform.runLater(() -> {
            historicoList.show();
            Rotas.get("historico-list/aplicar-prefiltro",
                    Params.with("sku", nvl(sku))
                            .and("funcionarioid", nvl(funcionarioId)));
        });
        return ResponseData.success();
    }

    // ===== ROTA DE RECARGA =====

    @ExecMapping("refresh")
    public ResponseData refresh() {
        Platform.runLater(this::loadInitialData);
        return ResponseData.success();
    }

    // ===== CLEANUP =====
    public void cleanup() {
        if (filters != null)      filters.cleanup();
        if (pagination != null)   pagination.cleanup();
        if (actions != null)      actions.cleanup();
        if (tableManager != null) tableManager.cleanup();
        if (uiUpdater != null)    uiUpdater.cleanup();
        if (state != null)        state.cleanup();
    }

    // ===== UTILITÁRIOS =====
    private String nvl(String s) { return s != null ? s : ""; }
}