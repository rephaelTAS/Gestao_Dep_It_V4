/*
 * FuncionarioListController v1.2
 *
 * Controlador de Listagem de Funcionários.
 * Responsável pela coordenação entre FXML, TableManager e Service.
 *
 * v1.2: Corrigido import ViewConstant (pacote real: config.view)
 *       Corrigido NexusFX.alerts().erro() — 3 parâmetros: (titulo, mensagem, detalhes)
 *       Substituído NexusFX.guard()/toast() por NexusFX.alerts() (API real disponível)
 * v1.1: Corrigido — NexusFX.errors() → NexusFX.alerts().erro()
 * v1.0: Refatoração completa — base sólida
 */
package com.ossobo.gestaoDepIt.controllers.gestao.funcionario;

import com.ossobo.gestaoDepIt.config.ViewConstant;
import com.ossobo.gestaoDepIt.db.models.Funcionarios;
import com.ossobo.gestaoDepIt.db.services.FuncionariosService;
import com.ossobo.nexusfx.NexusFX;
import com.ossobo.nexusfx.di.annotations.Controller;
import com.ossobo.nexusfx.di.annotations.Inject;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Controller
public class FuncionarioListController {

    private static final Logger logger = LoggerFactory.getLogger(FuncionarioListController.class);

    @Inject
    private FuncionariosService funcionariosService;

    @FXML private Button novoButton;
    @FXML private TextField filtroNomeField;
    @FXML private ComboBox<String> filtroDepartamentoCombo;
    @FXML private ComboBox<String> filtroStatusCombo;
    @FXML private ComboBox<String> filtroComFotoCombo;
    @FXML private Label estatisticasLabel;
    @FXML private Label statusLabel;
    @FXML private ProgressIndicator progressIndicator;

    @FXML private TableView<Funcionarios> funcionariosTable;
    @FXML private TableColumn<Funcionarios, String> colFoto;
    @FXML private TableColumn<Funcionarios, String> colCodDep;
    @FXML private TableColumn<Funcionarios, String> colNome;
    @FXML private TableColumn<Funcionarios, String> colFuncao;
    @FXML private TableColumn<Funcionarios, String> colDepartamento;
    @FXML private TableColumn<Funcionarios, Boolean> colStatus;
    @FXML private TableColumn<Funcionarios, String> colEmail;
    @FXML private TableColumn<Funcionarios, Void> colAcoes;

    @FXML private Label labelPagina;
    @FXML private ComboBox<Integer> comboItensPorPagina;

    private FuncionarioTableManager tableManager;
    private FuncionarioFilterManager filterManager;

    private ObservableList<Funcionarios> masterData = FXCollections.observableArrayList();
    private int paginaAtual = 1;
    private int totalPaginas = 1;
    private int itensPorPagina = 20;

    // =========================================================================
    // CICLO DE VIDA
    // =========================================================================

    @FXML
    public void initialize() {
        logger.info("Inicializando FuncionarioListController");

        this.tableManager = new FuncionarioTableManager(this);
        this.filterManager = new FuncionarioFilterManager(funcionariosService);

        tableManager.configurarTabela();
        carregarCombos();
        configurarPaginacao();
        carregarDados();
    }

    // =========================================================================
    // CARREGAMENTO DE DADOS
    // =========================================================================

    private void carregarDados() {
        mostrarLoading(true);
        statusLabel.setText("Carregando funcionários...");

        new Thread(() -> {
            try {
                List<Funcionarios> funcionarios = funcionariosService.listarTodos(paginaAtual, itensPorPagina);
                int total = funcionariosService.obterEstatisticas().getOrDefault("total", 0);
                totalPaginas = (int) Math.ceil((double) total / itensPorPagina);
                if (totalPaginas < 1) totalPaginas = 1;

                Platform.runLater(() -> {
                    masterData.setAll(funcionarios);
                    funcionariosTable.setItems(masterData);
                    atualizarLabelPagina();
                    atualizarEstatisticas();
                    mostrarLoading(false);
                    statusLabel.setText("");
                });

            } catch (Exception e) {
                logger.error("Erro ao carregar funcionários", e);
                Platform.runLater(() -> {
                    mostrarLoading(false);
                    statusLabel.setText("Erro ao carregar dados");
                    NexusFX.alerts().erro("Erro", "Falha ao carregar funcionários", e.getMessage());
                });
            }
        }).start();
    }

    // =========================================================================
    // CONFIGURAÇÃO DOS COMBOS
    // =========================================================================

    private void carregarCombos() {
        filtroDepartamentoCombo.setItems(FXCollections.observableArrayList(
                "Todos", "TI", "RH", "Financeiro", "Operações", "Administrativo"
        ));
        filtroDepartamentoCombo.getSelectionModel().selectFirst();

        filtroStatusCombo.setItems(FXCollections.observableArrayList(
                "Todos", "Ativos", "Inativos"
        ));
        filtroStatusCombo.getSelectionModel().selectFirst();

        filtroComFotoCombo.setItems(FXCollections.observableArrayList(
                "Todos", "Com Foto", "Sem Foto"
        ));
        filtroComFotoCombo.getSelectionModel().selectFirst();
    }

    // =========================================================================
    // PAGINAÇÃO
    // =========================================================================

    private void configurarPaginacao() {
        comboItensPorPagina.setItems(FXCollections.observableArrayList(10, 20, 50, 100));
        comboItensPorPagina.setValue(20);
        comboItensPorPagina.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                itensPorPagina = newVal;
                paginaAtual = 1;
                carregarDados();
            }
        });
    }

    private void atualizarLabelPagina() {
        labelPagina.setText(String.format("Página %d de %d", paginaAtual, totalPaginas));
    }

    private void irParaPagina(int novaPagina) {
        if (novaPagina >= 1 && novaPagina <= totalPaginas) {
            paginaAtual = novaPagina;
            carregarDados();
        }
    }

    @FXML private void handlePrimeiraPagina() { irParaPagina(1); }
    @FXML private void handlePaginaAnterior() { irParaPagina(paginaAtual - 1); }
    @FXML private void handleProximaPagina() { irParaPagina(paginaAtual + 1); }
    @FXML private void handleUltimaPagina() { irParaPagina(totalPaginas); }

    // =========================================================================
    // HANDLERS DE AÇÕES
    // =========================================================================

    @FXML
    private void handleNovoFuncionario(ActionEvent event) {
        try {
            Window owner = ((Node) event.getSource()).getScene().getWindow();
            NexusFX.dialogs().openModalWithController(
                    ViewConstant.Funcionario.ADDEDITARFUNCIONARIO,
                    "Novo Funcionário",
                    owner,
                    (Object controller) -> {
                        if (controller instanceof FuncionarioFormController form) {
                            form.setModoEdicao(false);
                        }
                    }
            );
            carregarDados();

        } catch (Exception e) {
            logger.error("Erro ao abrir formulário de novo funcionário", e);
            NexusFX.alerts().erro("Erro", "Não foi possível abrir o formulário", e.getMessage());
        }
    }

    void handleVisualizarFuncionario(Funcionarios funcionario, ActionEvent event) {
        logger.info(">>> ABRINDO DETALHES para: {} (codDep={})", funcionario.getNome(), funcionario.getCodDep());

        try {
            Window owner = event != null
                    ? ((Node) event.getSource()).getScene().getWindow()
                    : funcionariosTable.getScene().getWindow();

            NexusFX.dialogs().openModalWithController(
                    ViewConstant.Funcionario.DETALHESFUNCIONARIO,
                    "Detalhes do Funcionário",
                    owner,
                    (Object controller) -> {
                        logger.info(">>> CALLBACK DETALHES: controller={}, isDetail={}",
                                controller.getClass().getSimpleName(),
                                controller instanceof FuncionarioDetailController);

                        if (controller instanceof FuncionarioDetailController detailController) {
                            logger.info(">>> Chamando setFuncionario com: {}", funcionario.getNome());
                            detailController.setFuncionario(funcionario);
                            logger.info(">>> setFuncionario concluído");
                        }
                    }
            );
        } catch (Exception e) {
            logger.error("Erro ao visualizar funcionário", e);
            NexusFX.alerts().erro("Erro", "Não foi possível abrir os detalhes", e.getMessage());
        }
    }

    void handleEditarFuncionario(Funcionarios funcionario, ActionEvent event) {
        logger.info(">>> ABRINDO EDIÇÃO para: {} (codDep={})", funcionario.getNome(), funcionario.getCodDep());

        try {
            Window owner = event != null
                    ? ((Node) event.getSource()).getScene().getWindow()
                    : funcionariosTable.getScene().getWindow();

            NexusFX.dialogs().openModalWithController(
                    ViewConstant.Funcionario.ADDEDITARFUNCIONARIO,
                    "Editar Funcionário",
                    owner,
                    (Object controller) -> {
                        logger.info(">>> CALLBACK EDIÇÃO: controller={}, isForm={}",
                                controller.getClass().getSimpleName(),
                                controller instanceof FuncionarioFormController);

                        if (controller instanceof FuncionarioFormController form) {
                            logger.info(">>> Chamando setFuncionario com: {}", funcionario.getNome());
                            form.setFuncionario(funcionario);
                            logger.info(">>> setFuncionario concluído");
                        }
                    }
            );
            carregarDados();

        } catch (Exception e) {
            logger.error("Erro ao editar funcionário", e);
            NexusFX.alerts().erro("Erro", "Não foi possível abrir o formulário de edição", e.getMessage());
        }
    }

    void handleExcluirFuncionario(Funcionarios funcionario, ActionEvent event) {
        String nome = funcionario.getNome();

        NexusFX.alerts().confirmarPerigo(
                String.format("Deseja realmente excluir %s?", nome),
                "Esta ação é irreversível e removerá todos os dados associados.",
                "Confirmar Exclusão",
                confirmado -> {
                    if (confirmado) {
                        executarExclusao(funcionario);
                    }
                }
        );
    }

    private void executarExclusao(Funcionarios funcionario) {
        mostrarLoading(true);

        new Thread(() -> {
            try {
                String nome = funcionario.getNome();
                funcionariosService.excluirFuncionario(funcionario.getCodDep());

                Platform.runLater(() -> {
                    masterData.remove(funcionario);
                    mostrarLoading(false);
                    atualizarEstatisticas();

                    NexusFX.alerts().info(
                            "Funcionário Excluído",
                            nome + " foi removido com sucesso.",
                            "Sistema"
                    );
                });

            } catch (Exception e) {
                logger.error("Erro ao excluir funcionário", e);
                Platform.runLater(() -> {
                    mostrarLoading(false);
                    NexusFX.alerts().erro("Erro ao excluir", "Falha na exclusão", e.getMessage());
                });
            }
        }).start();
    }

    // =========================================================================
    // FILTROS
    // =========================================================================

    @FXML
    private void handleAplicarFiltros() {
        String nome = filtroNomeField.getText().trim();
        String departamento = filtroDepartamentoCombo.getValue();
        String status = filtroStatusCombo.getValue();
        String comFoto = filtroComFotoCombo.getValue();

        if ("Todos".equals(departamento)) departamento = null;
        Boolean ativo = null;
        if ("Ativos".equals(status)) ativo = true;
        else if ("Inativos".equals(status)) ativo = false;

        mostrarLoading(true);
        statusLabel.setText("Aplicando filtros...");

        final String finalDepartamento = departamento;
        final Boolean finalAtivo = ativo;
        final String finalComFoto = comFoto;
        final String finalNome = (nome != null && !nome.isEmpty()) ? nome : null;

        new Thread(() -> {
            try {
                List<Funcionarios> resultados = filterManager.filtrar(
                        finalNome, finalDepartamento, finalAtivo, finalComFoto
                );

                Platform.runLater(() -> {
                    masterData.setAll(resultados);
                    funcionariosTable.setItems(masterData);
                    paginaAtual = 1;
                    totalPaginas = 1;
                    atualizarLabelPagina();
                    atualizarEstatisticas();
                    mostrarLoading(false);
                    statusLabel.setText(resultados.size() + " funcionário(s) encontrado(s)");
                });

            } catch (Exception e) {
                logger.error("Erro ao aplicar filtros", e);
                Platform.runLater(() -> {
                    mostrarLoading(false);
                    statusLabel.setText("Erro ao filtrar");
                });
            }
        }).start();
    }

    @FXML
    private void handleLimparFiltros() {
        filtroNomeField.clear();
        filtroDepartamentoCombo.getSelectionModel().selectFirst();
        filtroStatusCombo.getSelectionModel().selectFirst();
        filtroComFotoCombo.getSelectionModel().selectFirst();

        paginaAtual = 1;
        carregarDados();
    }

    // =========================================================================
    // UTILITÁRIOS
    // =========================================================================

    private void mostrarLoading(boolean mostrar) {
        Platform.runLater(() -> {
            if (progressIndicator != null) progressIndicator.setVisible(mostrar);
            if (funcionariosTable != null) funcionariosTable.setDisable(mostrar);
        });
    }

    private void atualizarEstatisticas() {
        int total = masterData.size();
        estatisticasLabel.setText(String.format("Total: %d funcionário%s", total, total != 1 ? "s" : ""));
    }

    // =========================================================================
    // GETTERS PARA TABLE MANAGER
    // =========================================================================

    TableView<Funcionarios> getFuncionariosTable() { return funcionariosTable; }
    TableColumn<Funcionarios, String> getColFoto() { return colFoto; }
    TableColumn<Funcionarios, String> getColCodDep() { return colCodDep; }
    TableColumn<Funcionarios, String> getColNome() { return colNome; }
    TableColumn<Funcionarios, String> getColFuncao() { return colFuncao; }
    TableColumn<Funcionarios, String> getColDepartamento() { return colDepartamento; }
    TableColumn<Funcionarios, Boolean> getColStatus() { return colStatus; }
    TableColumn<Funcionarios, String> getColEmail() { return colEmail; }
    TableColumn<Funcionarios, Void> getColAcoes() { return colAcoes; }
}