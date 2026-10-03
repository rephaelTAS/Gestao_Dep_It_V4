package com.ossobo.gestaoDepIt.controlls.funcionario;

import com.ossobo.gestaoDepIt.config.ViewConstant;
import com.ossobo.gestaoDepIt.controlls.funcionario.funcionarioList.*;
import com.ossobo.gestaoDepIt.controlls.funcionario.funcionarioList.cellfactories.FuncionarioAcoesCallback;
import com.ossobo.gestaoDepIt.db.models.Funcionarios;
import com.ossobo.winterfx.anotations.Controller;
import com.ossobo.winterfx.anotations.ExecMapping;
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
import java.util.concurrent.CompletableFuture;

/**
 * FuncionarioListController v1.1
 *
 * Responsabilidade: coordenação da tela de listagem de funcionários.
 *
 * v1.1 — Logging de diagnóstico + retorno de mensagens de erro da rota:
 *        - initializeComponents envolto em try/catch com System.Logger
 *          (o framework engole exceções durante a troca de cena — sem isso
 *          a app fecha em silêncio com exit 0).
 *        - loadInitialData loga a chamada e o retorno (sucesso/erro) —
 *          permite identificar rota não registrada ou retorno vazio.
 *        - loadInitialData passa a checar `resp.isSuccess()` dentro do
 *          FuncionarioActions; aqui só consumimos o resultado.
 *
 * @since v1.0
 */
@Controller(proxy = false)
@RegisterView(
        id = ViewConstant.Funcionario.LIST,
        fxml = "/META-INF/gestaoDepIt/fxmls/funcionario/FuncionarioList.fxml",
        title = "Gestão de Funcionários",
        primaryCss = "/META-INF/gestaoDepIt/css/funcionario/funcionario-list.css"
)
public class FuncionarioListController
        implements Initializable, WinterFXController, FuncionarioAcoesCallback {

    private static final System.Logger LOGGER =
            System.getLogger(FuncionarioListController.class.getName());

    @FloatingWindow(viewId = ViewConstant.Funcionario.FORM, singleton = false)
    private StageForFloatingWindow funcionarioForm;

    @FloatingWindow(viewId = ViewConstant.Funcionario.DETAIL, singleton = false)
    private StageForFloatingWindow funcionarioDetail;

    // ===== COMPONENTES INTERNOS =====
    private final FuncionarioState state = new FuncionarioState();
    private FuncionarioFilters filters;
    private FuncionarioPagination pagination;
    private FuncionarioActions actions;
    private FuncionarioTableManager tableManager;
    private FuncionarioUIUpdater uiUpdater;

    // ===== FXML — TABELA =====
    @FXML private TableView<Funcionarios> funcionariosTable;
    @FXML private TableColumn<Funcionarios, Void>   colFoto;
    @FXML private TableColumn<Funcionarios, String> colCodDep;
    @FXML private TableColumn<Funcionarios, String> colNome;
    @FXML private TableColumn<Funcionarios, String> colFuncao;
    @FXML private TableColumn<Funcionarios, String> colDepartamento;
    @FXML private TableColumn<Funcionarios, String> colStatus;
    @FXML private TableColumn<Funcionarios, String> colEmail;
    @FXML private TableColumn<Funcionarios, Void>   colAcoes;

    // ===== FXML — FILTROS =====
    @FXML private TextField        filtroNome;
    @FXML private ComboBox<String> filtroDepartamento;
    @FXML private ComboBox<String> filtroStatus;
    @FXML private ComboBox<String> filtroComFoto;

    // ===== FXML — BOTÕES =====
    @FXML private Button btn_novo;
    @FXML private Button btn_limparFiltros;
    @FXML private Button btn_aplicarFiltros;

    // ===== FXML — PAGINAÇÃO =====
    @FXML private Button           btn_primeira;
    @FXML private Button           btn_anterior;
    @FXML private Button           btn_proxima;
    @FXML private Button           btn_ultima;
    @FXML private Label            labelPagina;
    @FXML private ComboBox<Integer> comboItensPorPagina;

    // ===== FXML — STATUS =====
    @FXML private Label             statusLabel;
    @FXML private Label             estatisticasLabel;
    @FXML private ProgressIndicator progressIndicator;

    public FuncionarioListController() { }

    // ===== INITIALIZE =====

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(this::initializeComponents);
    }

    private void initializeComponents() {
        try {
            LOGGER.log(System.Logger.Level.INFO, ">>> initializeComponents iniciou");

            if (!verifyCriticalComponents()) {
                LOGGER.log(System.Logger.Level.ERROR,
                        ">>> verifyCriticalComponents falhou: "
                                + "table={0}, colCodDep={1}, colStatus={2}, colAcoes={3}, btn_novo={4}",
                        funcionariosTable, colCodDep, colStatus, colAcoes, btn_novo);
                return;
            }

            createComponents();
            configureComponents();

            LOGGER.log(System.Logger.Level.INFO,
                    ">>> configureComponents OK; tabela.items={0}, state.size={1}",
                    funcionariosTable.getItems() != null ? funcionariosTable.getItems().size() : -1,
                    state.getFuncionariosData().size());

            loadInitialData();
        } catch (Throwable t) {
            LOGGER.log(System.Logger.Level.ERROR, ">>> initializeComponents falhou", t);
            throw t;
        }
    }

    private boolean verifyCriticalComponents() {
        return funcionariosTable != null && colCodDep != null && colStatus != null
                && colAcoes != null && btn_novo != null;
    }

    private void createComponents() {
        this.filters      = new FuncionarioFilters(state);
        this.pagination   = new FuncionarioPagination(state);
        this.actions      = new FuncionarioActions(state);
        this.tableManager = new FuncionarioTableManager(funcionariosTable, state, this);
        this.uiUpdater    = new FuncionarioUIUpdater(state);
    }

    private void configureComponents() {
        configureFilters();
        configurePagination();
        configureTableManager();
        configureUIUpdater();
    }

    private void configureFilters() {
        filters.initializeUIComponents(
                filtroNome, filtroDepartamento, filtroStatus, filtroComFoto);
        filters.configureBasicFilters();
    }

    private void configurePagination() {
        pagination.initializeUIComponents(
                btn_primeira, btn_anterior, btn_proxima, btn_ultima,
                labelPagina, comboItensPorPagina);
        pagination.configurePagination();
    }

    private void configureTableManager() {
        tableManager.configurarColunas(
                colFoto, colCodDep, colNome, colFuncao,
                colDepartamento, colStatus, colEmail, colAcoes);
        tableManager.configurarTabelaCompleta();

        LOGGER.log(System.Logger.Level.INFO,
                ">>> colunas vinculadas: codDep={0}, nome={1}, funcao={2}, "
                        + "depto={3}, status={4}, email={5}, foto={6}, acoes={7}",
                colCodDep, colNome, colFuncao,
                colDepartamento, colStatus, colEmail, colFoto, colAcoes);
    }

    private void configureUIUpdater() {
        uiUpdater.initializeComponents(
                statusLabel, estatisticasLabel, progressIndicator,
                btn_novo, btn_aplicarFiltros, btn_limparFiltros);
    }

    // ===== CARREGAMENTO =====

    private void loadInitialData() {
        uiUpdater.showProgress(true, "Carregando funcionários...");

        LOGGER.log(System.Logger.Level.INFO,
                ">>> chamando funcionarios/service/paginados pagina={0} tamanho={1}",
                state.getPaginaAtual(), state.getItensPorPagina());

        actions.carregarDados().whenComplete((v, erro) -> {
            LOGGER.log(System.Logger.Level.INFO,
                    ">>> carregarDados retornou. erro={0}, itens no state={1}",
                    erro, state.getFuncionariosData().size());

            Platform.runLater(() -> {
                if (erro != null) {
                    LOGGER.log(System.Logger.Level.ERROR,
                            ">>> carregarDados falhou", erro);
                    uiUpdater.showProgress(false, "Falha ao carregar: " + erro.getMessage());
                    return;
                }
                uiUpdater.showProgress(false, "Funcionários carregados");
                uiUpdater.calcularEstatisticas();
                filters.populateDepartamentos(state.getFuncionariosData());
                pagination.updateUI();

                LOGGER.log(System.Logger.Level.INFO,
                        ">>> tabela.items após carregar = {0}",
                        funcionariosTable.getItems() != null
                                ? funcionariosTable.getItems().size() : -1);
            });
        });
    }

    private void loadPageData() {
        uiUpdater.showProgress(true, "Carregando página...");
        actions.carregarDados().whenComplete((v, erro) -> Platform.runLater(() -> {
            uiUpdater.showProgress(false, "");
            if (erro != null) { uiUpdater.updateStatus("Falha: " + erro.getMessage()); return; }
            uiUpdater.calcularEstatisticas();
        }));
    }

    private void loadFilteredData() {
        uiUpdater.showProgress(true, "Aplicando filtros...");
        actions.carregarDados().whenComplete((v, erro) -> Platform.runLater(() -> {
            if (erro != null) {
                uiUpdater.showProgress(false, "Falha: " + erro.getMessage());
                return;
            }
            var filtrados = filters.applyFilters(state.getFuncionariosData());
            state.setFuncionariosData(filtrados);
            state.setTotalPaginas(filtrados.size());
            state.primeiraPagina();
            pagination.updateUI();
            uiUpdater.showProgress(false, "Filtros aplicados");
            uiUpdater.calcularEstatisticas();
        }));
    }

    // ===== AÇÕES (nome = fx:id) =====

    public void btn_novo(ActionEvent event) {
        if (funcionarioForm != null) funcionarioForm.show();
    }

    public void btn_aplicarFiltros(ActionEvent event) {
        filters.applyFiltersFromUI();
        loadFilteredData();
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

    // ===== FuncionarioAcoesCallback =====

    @Override
    public void abrirDetalhes(Funcionarios funcionario) {
        if (funcionario == null || funcionarioDetail == null) return;
        funcionarioDetail.show();
        Rotas.get("funcionario-detail/carregar/coddep",
                Params.with("coddep", funcionario.codDep()));
    }

    @Override
    public void abrirEdicao(Funcionarios funcionario) {
        if (funcionario == null || funcionarioForm == null) return;
        funcionarioForm.show();
        Rotas.get("funcionario-form/editar/coddep",
                Params.with("coddep", funcionario.codDep()));
    }

    @Override
    public void alternarStatus(Funcionarios funcionario) {
        if (funcionario == null) return;

        boolean ativoAtual = Boolean.TRUE.equals(funcionario.ativo());
        String acao = ativoAtual ? "desativar" : "ativar";

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmação");
        confirm.setHeaderText(null);
        confirm.setContentText("Deseja realmente " + acao + " " + funcionario.nome() + "?");
        confirm.showAndWait().ifPresent(resposta -> {
            if (resposta != ButtonType.OK) return;

            uiUpdater.showProgress(true, "Processando...");
            actions.alternarStatus(funcionario).whenComplete((resp, erro) -> Platform.runLater(() -> {
                uiUpdater.showProgress(false, "");
                if (erro != null) {
                    uiUpdater.updateStatus("Falha: " + erro.getMessage());
                    return;
                }
                if (resp == null || !resp.isSuccess()) {
                    uiUpdater.updateStatus("Falha: " +
                            (resp != null ? resp.getMessage() : "resposta vazia"));
                    return;
                }
                uiUpdater.showSuccess("Status alterado");
                loadInitialData();
            }));
        });
    }

    @Override
    public CompletableFuture<byte[]> carregarImagem(String codDep) {
        return actions.carregarImagem(codDep);
    }

    // ===== ROTA DE RECARGA =====

    @ExecMapping("funcionario-list/refresh")
    public ResponseData refresh() {
        Platform.runLater(this::loadInitialData);
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