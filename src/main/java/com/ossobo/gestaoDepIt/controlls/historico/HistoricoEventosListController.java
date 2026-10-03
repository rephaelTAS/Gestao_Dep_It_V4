package com.ossobo.gestaoDepIt.controlls.historico;

import com.ossobo.gestaoDepIt.config.ViewConstant;
import com.ossobo.gestaoDepIt.controlls.historico.historicoList.HistoricoActions;
import com.ossobo.gestaoDepIt.controlls.historico.historicoList.HistoricoFilters;
import com.ossobo.gestaoDepIt.controlls.historico.historicoList.HistoricoPagination;
import com.ossobo.gestaoDepIt.controlls.historico.historicoList.HistoricoState;
import com.ossobo.gestaoDepIt.controlls.historico.historicoList.HistoricoTableManager;
import com.ossobo.gestaoDepIt.controlls.historico.historicoList.HistoricoUIUpdater;
import com.ossobo.gestaoDepIt.controlls.historico.historicoList.cellfactories.HistoricoAcoesCallback;
import com.ossobo.gestaoDepIt.db.models.HistoricoEventos;
import com.ossobo.winterfx.anotations.Controller;
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
 * HistoricoEventosListController v3.1
 *
 * Listagem do histórico de eventos. Janela flutuante aberta pelo inventário.
 *
 * Padrões ratificados:
 *  - Zero service injetado — tudo via Rotas (historico-eventos/service/*).
 *  - FXML é fonte da nomenclatura: campo @FXML = fx:id; método = fx:id.
 *  - Delegação: State / Filters / Pagination / Actions / TableManager / UIUpdater.
 *  - Pre-filtro recebido por rota após show() — sku + funcionarioid.
 *  - CANAIS: GET para enviar dados à view; nenhum PUT/EXEC neste controller.
 *
 * v3.1 — Correção de canal:
 *        - abrirDetalhes agora usa Rotas.get(...) — enviar id para a janela
 *          de detalhes é leitura, não mutação.
 *        - aplicar-prefiltro passa a @GetMapping — recebe contexto da UI,
 *          não muta nada.
 *
 * @since v3.0
 */
@Controller(proxy = false)
@RegisterView(
        id = ViewConstant.Inventario.HISTORICO_LIST,
        fxml = "/META-INF/gestaoDepIt/fxmls/historico/HistoricoEventosList.fxml",
        title = "Histórico de Eventos",
        primaryCss = "/META-INF/gestaoDepIt/css/historico/HistoricoEventosList.css"
)
@RequestMapping("historico-list")
public class HistoricoEventosListController implements Initializable, WinterFXController, HistoricoAcoesCallback {

    // ===== FLOATING WINDOW =====
    @FloatingWindow(viewId = ViewConstant.Inventario.HISTORICO_DETAIL, singleton = false)
    private StageForFloatingWindow historicoDetail;

    // ===== FXML — HEADER =====
    @FXML private Button btn_relatorio;
    @FXML private Button btn_exportar;

    // ===== FXML — SUBTÍTULO / STATS =====
    @FXML private Label estatisticasLabel;
    @FXML private Label filtrosAtivosLabel;

    // ===== FXML — FILTROS =====
    @FXML private TextField        filtroPesquisa;
    @FXML private ComboBox<String> filtroTipoEvento;
    @FXML private DatePicker       filtroDataInicio;
    @FXML private DatePicker       filtroDataFim;
    @FXML private Button           btn_limparFiltros;
    @FXML private Button           btn_aplicarFiltros;

    // ===== FXML — TABELA =====
    @FXML private TableView<HistoricoEventos> eventosTable;
    @FXML private TableColumn<HistoricoEventos, String> colDataHora;
    @FXML private TableColumn<HistoricoEventos, String> colTipoEvento;
    @FXML private TableColumn<HistoricoEventos, String> colSkuProduto;
    @FXML private TableColumn<HistoricoEventos, String> colFuncionario;
    @FXML private TableColumn<HistoricoEventos, String> colDescricao;
    @FXML private TableColumn<HistoricoEventos, Void>   colAcoes;

    // ===== FXML — PAGINAÇÃO =====
    @FXML private Button btn_primeira;
    @FXML private Button btn_anterior;
    @FXML private Button btn_proxima;
    @FXML private Button btn_ultima;
    @FXML private Label  labelPagina;
    @FXML private ComboBox<Integer> comboItensPorPagina;

    // ===== FXML — STATUS =====
    @FXML private Label             statusLabel;
    @FXML private Button            btn_atualizar;
    @FXML private Button            btn_fechar;
    @FXML private ProgressIndicator progressIndicator;

    // ===== COMPONENTES INTERNOS =====
    private final HistoricoState state = new HistoricoState();
    private HistoricoFilters filters;
    private HistoricoPagination pagination;
    private HistoricoActions actions;
    private HistoricoTableManager tableManager;
    private HistoricoUIUpdater uiUpdater;

    public HistoricoEventosListController() { }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(this::initializeComponents);
    }

    private void initializeComponents() {
        if (!verifyCriticalComponents()) {
            System.getLogger("historico").log(System.Logger.Level.ERROR,
                    "Componentes críticos ausentes no FXML do histórico");
            return;
        }
        createComponents();
        configureComponents();
        loadInitialData();
    }

    private boolean verifyCriticalComponents() {
        return eventosTable != null
                && colDataHora != null
                && colTipoEvento != null
                && colAcoes != null
                && progressIndicator != null;
    }

    private void createComponents() {
        this.filters      = new HistoricoFilters();
        this.pagination   = new HistoricoPagination(state);
        this.actions      = new HistoricoActions(state);
        this.tableManager = new HistoricoTableManager(eventosTable, state, this);
        this.uiUpdater    = new HistoricoUIUpdater(state);
    }

    private void configureComponents() {
        filters.initializeUIComponents(
                filtroPesquisa, filtroTipoEvento, filtroDataInicio, filtroDataFim);
        filters.configureBasicFilters();

        pagination.initializeUIComponents(
                btn_primeira, btn_anterior, btn_proxima, btn_ultima,
                labelPagina, comboItensPorPagina);
        pagination.configurePagination();

        tableManager.configurarColunas(
                colDataHora, colTipoEvento, colSkuProduto,
                colFuncionario, colDescricao, colAcoes);
        tableManager.configurarTabelaCompleta();

        uiUpdater.initializeComponents(
                statusLabel, estatisticasLabel, filtrosAtivosLabel, progressIndicator,
                btn_relatorio, btn_exportar, btn_limparFiltros, btn_aplicarFiltros);
    }

    // ===== LOAD =====
    private void loadInitialData() {
        uiUpdater.showProgress(true, "Carregando histórico...");
        actions.carregarDados().whenComplete((v, erro) -> Platform.runLater(() -> {
            if (erro != null) {
                uiUpdater.showProgress(false, "Falha ao carregar: " + erro.getMessage());
                System.getLogger("historico").log(System.Logger.Level.ERROR, "carregarDados falhou", erro);
                return;
            }
            uiUpdater.showProgress(false, "Histórico carregado");
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

    public void btn_relatorio(ActionEvent event) {
        uiUpdater.updateStatus("ℹ️ Relatório em desenvolvimento.");
    }

    public void btn_exportar(ActionEvent event) {
        uiUpdater.updateStatus("ℹ️ Exportação CSV em desenvolvimento.");
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
            uiUpdater.atualizarFiltrosAtivos(filters.temFiltrosAtivos() ? "(filtros ativos)" : "");
            pagination.resetToFirstPage();
        }));
    }

    public void btn_limparFiltros(ActionEvent event) {
        filters.clearFiltersUI();
        pagination.resetToFirstPage();
        uiUpdater.atualizarFiltrosAtivos("");
        loadInitialData();
        uiUpdater.updateStatus("Filtros limpos");
    }

    public void btn_primeira(ActionEvent event) { pagination.goToFirstPage();    loadPageData(); }
    public void btn_anterior(ActionEvent event) { pagination.goToPreviousPage(); loadPageData(); }
    public void btn_proxima(ActionEvent event)  { pagination.goToNextPage();     loadPageData(); }
    public void btn_ultima(ActionEvent event)   { pagination.goToLastPage();     loadPageData(); }

    public void btn_atualizar(ActionEvent event) { loadInitialData(); }

    public void btn_fechar(ActionEvent event) {
        Platform.runLater(() -> {
            if (eventosTable != null && eventosTable.getScene() != null) {
                ((javafx.stage.Stage) eventosTable.getScene().getWindow()).close();
            }
        });
    }

    // ===== HistoricoAcoesCallback =====

    @Override
    public void abrirDetalhes(HistoricoEventos evento) {
        if (evento == null || historicoDetail == null) return;
        historicoDetail.show();
        // GET: enviar id para a janela de detalhes popular. NÃO é mutação.
        Rotas.get("historico-detail/carregar/id",
                Params.with("id", evento.id()));
    }

    // ===== ROTA DE PRE-FILTRO (chamada pelo inventário após show()) =====

    /**
     * O inventário abre esta janela e, logo depois, injeta o contexto
     * (sku + funcionarioid) por esta rota. Enviar contexto é LEITURA —
     * @GetMapping, não @PutMapping.
     */
    @GetMapping("aplicar-prefiltro")
    public ResponseData aplicarPreFiltro(@RouteVar("sku") String sku,
                                         @RouteVar("funcionarioid") String funcionarioId) {
        Platform.runLater(() -> {
            filters.setPreFiltroSku(sku);
            filters.setPreFiltroFuncionarioId(funcionarioId);
            uiUpdater.atualizarFiltrosAtivos("(filtro: SKU e funcionário)");

            var params = filters.paramsDeFiltros();
            uiUpdater.showProgress(true, "Aplicando filtro...");
            actions.aplicarFiltros(params).whenComplete((v, erro) -> Platform.runLater(() -> {
                if (erro != null) {
                    uiUpdater.showProgress(false, "Falha: " + erro.getMessage());
                    return;
                }
                uiUpdater.showProgress(false, "Filtro aplicado");
                uiUpdater.calcularEstatisticas();
                pagination.resetToFirstPage();
            }));
        });
        return ResponseData.success();
    }

    public void cleanup() {
        if (filters != null)      filters.cleanup();
        if (pagination != null)   pagination.cleanup();
        if (actions != null)      actions.cleanup();
        if (tableManager != null) tableManager.cleanup();
        if (uiUpdater != null)    uiUpdater.cleanup();
        if (state != null)        state.cleanup();
    }
}