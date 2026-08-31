/*
 * UsuarioListController v1.0
 *
 * Controlador de Listagem de Usuários.
 *
 * v1.0: Versão inicial — base sólida
 *       - Tipagem forte (Usuario)
 *       - Injeção de Service via @Inject
 *       - TableManager e FilterManager extraídos (SRP)
 *       - ViewConstant para viewIds
 */
package com.ossobo.gestaoDepIt.controllers.gestao.pessoal;

import com.ossobo.gestaoDepIt.config.ViewConstant;
import com.ossobo.gestaoDepIt.db.models.Usuario;
import com.ossobo.gestaoDepIt.db.services.UsuariosService;
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
public class UsuarioListController {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioListController.class);

    @Inject
    private UsuariosService usuariosService;

    @FXML private Button novoUsuarioButton;
    @FXML private TextField filtroNomeField;
    @FXML private TextField filtroEmailField;
    @FXML private ComboBox<String> filtroNivelCombo;
    @FXML private ComboBox<String> filtroStatusCombo;
    @FXML private ComboBox<String> filtroSessaoCombo;
    @FXML private Label estatisticasLabel;
    @FXML private Label sessoesAtivasLabel;
    @FXML private Label statusLabel;
    @FXML private ProgressIndicator progressIndicator;

    @FXML private TableView<Usuario> usuariosTable;
    @FXML private TableColumn<Usuario, String> colNome;
    @FXML private TableColumn<Usuario, String> colEmail;
    @FXML private TableColumn<Usuario, String> colFuncionario;
    @FXML private TableColumn<Usuario, String> colNivelAcesso;
    @FXML private TableColumn<Usuario, Boolean> colStatus;
    @FXML private TableColumn<Usuario, String> colUltimoLogin;
    @FXML private TableColumn<Usuario, String> colIpLogin;
    @FXML private TableColumn<Usuario, String> colSessaoAtiva;
    @FXML private TableColumn<Usuario, Void> colAcoes;

    @FXML private Label labelPagina;
    @FXML private ComboBox<Integer> comboItensPorPagina;

    private UsuarioTableManager tableManager;
    private UsuarioFilterManager filterManager;

    private ObservableList<Usuario> masterData = FXCollections.observableArrayList();
    private int paginaAtual = 1;
    private int totalPaginas = 1;
    private int itensPorPagina = 20;

    @FXML
    public void initialize() {
        logger.info("Inicializando UsuarioListController");

        this.tableManager = new UsuarioTableManager(this);
        this.filterManager = new UsuarioFilterManager(usuariosService);

        tableManager.configurarTabela();
        carregarCombos();
        configurarPaginacao();
        carregarDados();
    }

    private void carregarDados() {
        mostrarLoading(true);
        statusLabel.setText("Carregando usuários...");

        new Thread(() -> {
            try {
                List<Usuario> usuarios = usuariosService.listarTodos(paginaAtual, itensPorPagina);
                int total = usuariosService.obterTotalUsuarios();
                totalPaginas = (int) Math.ceil((double) total / itensPorPagina);
                if (totalPaginas < 1) totalPaginas = 1;

                var stats = usuariosService.obterEstatisticasAtividade();
                int comSessao = stats.getOrDefault("com_sessao", 0);

                Platform.runLater(() -> {
                    masterData.setAll(usuarios);
                    usuariosTable.setItems(masterData);
                    atualizarLabelPagina();
                    atualizarEstatisticas(comSessao);
                    mostrarLoading(false);
                    statusLabel.setText("");
                });

            } catch (Exception e) {
                logger.error("Erro ao carregar usuários", e);
                Platform.runLater(() -> {
                    mostrarLoading(false);
                    statusLabel.setText("Erro ao carregar dados");
                    NexusFX.alerts().erro("Erro", "Falha ao carregar usuários", e.getMessage());
                });
            }
        }).start();
    }

    private void carregarCombos() {
        filtroNivelCombo.setItems(FXCollections.observableArrayList(
                "Todos", "ADMIN", "GESTOR", "SUPERVISOR", "OPERADOR", "READONLY"
        ));
        filtroNivelCombo.getSelectionModel().selectFirst();

        filtroStatusCombo.setItems(FXCollections.observableArrayList(
                "Todos", "Ativos", "Inativos"
        ));
        filtroStatusCombo.getSelectionModel().selectFirst();

        filtroSessaoCombo.setItems(FXCollections.observableArrayList(
                "Todas", "Online", "Offline"
        ));
        filtroSessaoCombo.getSelectionModel().selectFirst();
    }

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
    private void handleNovoUsuario(ActionEvent event) {
        try {
            Window owner = ((Node) event.getSource()).getScene().getWindow();
            NexusFX.dialogs().openModalWithController(
                    ViewConstant.Main.USUARIO,
                    "Novo Usuário",
                    owner,
                    (Object controller) -> {}
            );
            carregarDados();
        } catch (Exception e) {
            logger.error("Erro ao abrir formulário de novo usuário", e);
            NexusFX.alerts().erro("Erro", "Não foi possível abrir o formulário", e.getMessage());
        }
    }

    void handleVisualizarUsuario(Usuario usuario, ActionEvent event) {
        try {
            Window owner = event != null
                    ? ((Node) event.getSource()).getScene().getWindow()
                    : usuariosTable.getScene().getWindow();

            NexusFX.dialogs().openModalWithController(
                    ViewConstant.Main.DETALHEUSUARIO,
                    "Detalhes do Usuário",
                    owner,
                    (Object controller) -> {
                        if (controller instanceof UsuarioDetailController detail) {
                            detail.setUsuario(usuario);
                        }
                    }
            );
        } catch (Exception e) {
            logger.error("Erro ao visualizar usuário", e);
            NexusFX.alerts().erro("Erro", "Não foi possível abrir os detalhes", e.getMessage());
        }
    }

    void handleEditarUsuario(Usuario usuario, ActionEvent event) {
        try {
            Window owner = event != null
                    ? ((Node) event.getSource()).getScene().getWindow()
                    : usuariosTable.getScene().getWindow();

            NexusFX.dialogs().openModalWithController(
                    ViewConstant.Main.DETALHEUSUARIO,
                    "Editar Usuário",
                    owner,
                    (Object controller) -> {
                        if (controller instanceof UsuarioDetailController detail) {
                            detail.setUsuario(usuario);
                        }
                    }
            );
            carregarDados();
        } catch (Exception e) {
            logger.error("Erro ao editar usuário", e);
            NexusFX.alerts().erro("Erro", "Não foi possível abrir a edição", e.getMessage());
        }
    }

    void handleExcluirUsuario(Usuario usuario, ActionEvent event) {
        String nome = usuario.getNome();

        NexusFX.alerts().confirmarPerigo(
                String.format("Deseja realmente excluir o usuário %s?", nome),
                "Esta ação é irreversível e invalidará qualquer sessão ativa.",
                "Confirmar Exclusão",
                confirmado -> {
                    if (confirmado) {
                        executarExclusao(usuario);
                    }
                }
        );
    }

    private void executarExclusao(Usuario usuario) {
        mostrarLoading(true);
        new Thread(() -> {
            try {
                String nome = usuario.getNome();
                usuariosService.excluirUsuario(usuario.getId());

                Platform.runLater(() -> {
                    masterData.remove(usuario);
                    mostrarLoading(false);
                    atualizarEstatisticas(0);
                    NexusFX.alerts().info("Usuário Excluído", nome + " foi removido com sucesso.", "Sistema");
                });
            } catch (Exception e) {
                logger.error("Erro ao excluir usuário", e);
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
        String email = filtroEmailField.getText().trim();
        String nivel = filtroNivelCombo.getValue();
        String status = filtroStatusCombo.getValue();
        String sessao = filtroSessaoCombo.getValue();

        if ("Todos".equals(nivel)) nivel = null;
        Boolean ativo = null;
        if ("Ativos".equals(status)) ativo = true;
        else if ("Inativos".equals(status)) ativo = false;
        Boolean sessaoAtiva = null;
        if ("Online".equals(sessao)) sessaoAtiva = true;
        else if ("Offline".equals(sessao)) sessaoAtiva = false;

        mostrarLoading(true);
        final String fNivel = nivel;
        final Boolean fAtivo = ativo;
        final Boolean fSessao = sessaoAtiva;
        final String fNome = nome.isEmpty() ? null : nome;
        final String fEmail = email.isEmpty() ? null : email;

        new Thread(() -> {
            try {
                List<Usuario> resultados = filterManager.filtrar(fNome, fEmail, fNivel, fAtivo, fSessao);
                Platform.runLater(() -> {
                    masterData.setAll(resultados);
                    usuariosTable.setItems(masterData);
                    paginaAtual = 1;
                    totalPaginas = 1;
                    atualizarLabelPagina();
                    atualizarEstatisticas(0);
                    mostrarLoading(false);
                    statusLabel.setText(resultados.size() + " usuário(s) encontrado(s)");
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
        filtroEmailField.clear();
        filtroNivelCombo.getSelectionModel().selectFirst();
        filtroStatusCombo.getSelectionModel().selectFirst();
        filtroSessaoCombo.getSelectionModel().selectFirst();
        paginaAtual = 1;
        carregarDados();
    }

    // =========================================================================
    // UTILITÁRIOS
    // =========================================================================

    private void mostrarLoading(boolean mostrar) {
        Platform.runLater(() -> {
            if (progressIndicator != null) progressIndicator.setVisible(mostrar);
            if (usuariosTable != null) usuariosTable.setDisable(mostrar);
        });
    }

    private void atualizarEstatisticas(int sessoesAtivas) {
        int total = masterData.size();
        estatisticasLabel.setText(String.format("Total: %d usuário%s", total, total != 1 ? "s" : ""));
        sessoesAtivasLabel.setText(sessoesAtivas > 0 ? String.format("● %d online", sessoesAtivas) : "");
    }

    // =========================================================================
    // GETTERS PARA TABLE MANAGER
    // =========================================================================

    TableView<Usuario> getUsuariosTable() { return usuariosTable; }
    TableColumn<Usuario, String> getColNome() { return colNome; }
    TableColumn<Usuario, String> getColEmail() { return colEmail; }
    TableColumn<Usuario, String> getColFuncionario() { return colFuncionario; }
    TableColumn<Usuario, String> getColNivelAcesso() { return colNivelAcesso; }
    TableColumn<Usuario, Boolean> getColStatus() { return colStatus; }
    TableColumn<Usuario, String> getColUltimoLogin() { return colUltimoLogin; }
    TableColumn<Usuario, String> getColIpLogin() { return colIpLogin; }
    TableColumn<Usuario, String> getColSessaoAtiva() { return colSessaoAtiva; }
    TableColumn<Usuario, Void> getColAcoes() { return colAcoes; }
}