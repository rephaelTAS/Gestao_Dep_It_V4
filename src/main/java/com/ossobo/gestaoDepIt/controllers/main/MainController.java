package com.ossobo.gestaoDepIt.controllers.main;

import com.ossobo.gestaoDepIt.config.AppImageConfig;
import com.ossobo.gestaoDepIt.config.ViewConstant;
import com.ossobo.gestaoDepIt.db.models.Funcionarios;
import com.ossobo.gestaoDepIt.db.models.Usuario;
import com.ossobo.gestaoDepIt.db.services.FuncionariosService;
import com.ossobo.gestaoDepIt.events.LogoutEvent;
import com.ossobo.winterfx.anotations.Controller;
import com.ossobo.winterfx.anotations.EventListener;
import com.ossobo.winterfx.anotations.Inject;
import com.ossobo.winterfx.anotations.PostConstruct;
import com.ossobo.winterfx.event.EventBus;
import com.ossobo.winterfx.imagemanager.anotations.InjectImage;
import com.ossobo.winterfx.imagemanager.anotations.SwapImage;
import com.ossobo.winterfx.view.anotations.RegisterView;
import com.ossobo.winterfx.view.anotations.SwapFxml;
import com.ossobo.winterfx.view.controller.WinterFXController;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * 🎯 MainController - Dashboard Principal com WinterFX
 *
 * ✅ Gerenciado pelo WinterFX (@Controller)
 * ✅ Registro automático via @RegisterView
 * ✅ Injeção de imagens com @InjectImage
 * ✅ Navegação com @SwapFxml
 * ✅ Troca de imagens com @SwapImage
 * ✅ Eventos com @EventListener
 * ✅ Binding automático via FXML (fx:id)
 *
 * ⚠️ NOTA: Não é necessário configurar setOnAction() manualmente.
 * O WinterFX faz o binding automático através do FXML.
 *
 * @version 4.0 (WinterFX)
 * @since 2026-08-19
 */
@Controller(proxy = false)
@RegisterView(
        id = ViewConstant.Main.DASHBOARD,
        fxml = "/META-INF/gestaoDepIt/fxmls/main/main.fxml",
        title = "Dashboard - Gestão de TI",
        width = 1200,
        height = 800,
        centered = true,
        primaryCss = "/META-INF/gestaoDepIt/css/main/main.css"
)
public class MainController implements WinterFXController {
    private static final Logger LOGGER = LoggerFactory.getLogger(MainController.class);

    // ============================================================
    // CONSTANTES
    // ============================================================

    private static final double EXPANDED_WIDTH = 280;
    private static final double COLLAPSED_WIDTH = 70;
    private static final String USER_DEFAULT = "Convidado";

    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ============================================================
    // FXML INJECTION - O WinterFX injeta automaticamente
    // ============================================================

    // Container principal
    @FXML private StackPane stack_sistema;
    @FXML private StackPane multiploPanel;
    @FXML private VBox sidebar;

    // Header
    @FXML private Button toggleMenuBtn;
    @FXML @InjectImage(AppImageConfig.System.SETTING)
    private ImageView menuIcon;

    @FXML @InjectImage(AppImageConfig.System.NOTIFICATIONS)
    private ImageView imageNotificatio;

    @FXML private ImageView iconepage;
    @FXML private Label nomepage;
    @FXML private Button btn_notificacao;

    // User info
    @FXML @InjectImage(AppImageConfig.System.DEFAULT_USER)
    private ImageView imageUser;

    @FXML private Label nameUser;
    @FXML private Label timeLabel;
    @FXML private Label dateLabel;

    // Footer
    @FXML private Label lblFooter;

    // ============================================================
    // BOTÕES DO MENU SIDEBAR
    // ============================================================

    // Seção PRINCIPAL
    @FXML private Button btn_dashboard;
    @FXML @InjectImage(AppImageConfig.System.DASHBOARD)
    private ImageView dashboardIcon;

    // Seção ESTOQUE
    @FXML private Button btn_catalogo;
    @FXML @InjectImage(AppImageConfig.System.MENU)
    private ImageView catalogoIcon;

    @FXML private Button btn_estoque;
    @FXML @InjectImage(AppImageConfig.System.STOCK)
    private ImageView estoqueIcon;

    @FXML private Button btn_produtoStock;
    @FXML @InjectImage(AppImageConfig.System.IN_STOCK)
    private ImageView produtoStockIcone;

    // Seção EQUIPAMENTOS
    @FXML private Button btn_inventario;
    @FXML @InjectImage(AppImageConfig.System.MENU)
    private ImageView inventarioIcon;

    @FXML private Button btn_toners;
    @FXML @InjectImage(AppImageConfig.System.BALLOT)
    private ImageView tonersIcon;

    // Seção PESSOAL
    @FXML private Button btn_funcionarios;
    @FXML @InjectImage(AppImageConfig.System.USER)
    private ImageView funcionariosIcon;

    @FXML private Button btn_usuarios;
    @FXML @InjectImage(AppImageConfig.System.USER)
    private ImageView addItemIcon;

    // Seção RELATÓRIOS
    @FXML private Button btn_relatorio;
    @FXML @InjectImage(AppImageConfig.System.REPORT)
    private ImageView reportIcon;

    @FXML private Button btn_timeReport;
    @FXML @InjectImage(AppImageConfig.System.CLOCK)
    private ImageView timeReportIcon;

    @FXML private Button btn_historico;
    @FXML @InjectImage(AppImageConfig.System.USER)
    private ImageView historicoIcon;

    // Seção VISUALIZAÇÃO
    @FXML private Button btn_visualizar;
    @FXML @InjectImage(AppImageConfig.System.VIEW)
    private ImageView viewIcon;

    // ============================================================
    // DEPENDÊNCIAS INJETADAS
    // ============================================================

    @Inject
    private FuncionariosService funcionariosService;

    @Inject
    private EventBus eventBus;

    // ============================================================
    // ESTADO
    // ============================================================

    private boolean isMenuCollapsed = false;
    private String currentSystemView = "";
    private boolean isSystemCollapsed = false;
    private Usuario usuarioLogado;
    private String loggedInUserName;

    // ============================================================
    // CICLO DE VIDA - @PostConstruct
    // ============================================================

    @PostConstruct
    public void init() {
        LOGGER.info("🏔️ MainController inicializando com WinterFX...");

        try {
            // Configurar menu
            setupMenu();

            // Configurar data/hora
            setupDateTime();

            // Usuário padrão
            updateUserDisplay(USER_DEFAULT);

            // Carregar view padrão - Dashboard
            loadDashboard();

            // Mostrar painel de notificações inicialmente
            showNotificationsInitially();

            LOGGER.info("✅ MainController inicializado com sucesso");

        } catch (Exception e) {
            LOGGER.error("❌ Erro durante a inicialização do MainController", e);
        }
    }

    // ============================================================
    // CONFIGURAÇÃO DO MENU
    // ============================================================

    private void setupMenu() {
        sidebar.getStyleClass().remove("collapsed");
        sidebar.getStyleClass().add("menu-expanded");
        sidebar.setPrefWidth(EXPANDED_WIDTH);
        isMenuCollapsed = false;
        LOGGER.debug("Menu configurado no modo expandido");
    }

    // ============================================================
    // CONFIGURAÇÃO DATA/HORA
    // ============================================================

    private void setupDateTime() {
        Timeline clock = new Timeline(
                new KeyFrame(Duration.ZERO, e -> updateDateTime()),
                new KeyFrame(Duration.seconds(1))
        );
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
        LOGGER.debug("Relógio configurado");
    }

    private void updateDateTime() {
        try {
            LocalDateTime now = LocalDateTime.now();
            if (timeLabel != null) {
                timeLabel.setText(now.format(timeFormatter));
            }
            if (dateLabel != null) {
                dateLabel.setText(now.format(dateFormatter));
            }
            if (lblFooter != null) {
                lblFooter.setText(String.format(
                        "© %d Gestão de TI - Todos os direitos reservados",
                        now.getYear()
                ));
            }
        } catch (Exception e) {
            LOGGER.error("❌ Erro ao atualizar data/hora", e);
        }
    }

    // ============================================================
    // TOGGLE DO MENU
    // ============================================================

    /**
     * Alterna o menu lateral entre expandido e recolhido.
     * O binding é feito automaticamente via FXML (fx:id="toggleMenuBtn").
     */
    @FXML
    private void toggleMenu() {
        isMenuCollapsed = !isMenuCollapsed;

        if (isMenuCollapsed) {
            sidebar.setPrefWidth(COLLAPSED_WIDTH);
            if (menuIcon != null) menuIcon.setRotate(180);
            sidebar.getStyleClass().remove("menu-expanded");
            sidebar.getStyleClass().add("menu-collapsed");
        } else {
            sidebar.setPrefWidth(EXPANDED_WIDTH);
            if (menuIcon != null) menuIcon.setRotate(0);
            sidebar.getStyleClass().remove("menu-collapsed");
            sidebar.getStyleClass().add("menu-expanded");
        }
    }

    // ============================================================
    // HANDLERS DOS BOTÕES - COM @SwapFxml E @SwapImage
    // O WinterFX faz o binding automático via FXML (fx:id)
    // ============================================================

    // -------------------------------------------------------------
    // SEÇÃO PRINCIPAL
    // -------------------------------------------------------------

    /**
     * Dashboard - View principal
     * Binding via FXML: btn_dashboard → handleDashboard
     */
    @FXML
    @SwapFxml(
            container = "multiploPanel",
            viewId = ViewConstant.Main.DASHBOARD,
            before = false
    )
    @SwapImage(
            imageView = "iconepage",
            imageId = AppImageConfig.System.DASHBOARD
    )
    public void handleDashboard(ActionEvent event) {
        LOGGER.info("📊 Navegando para Dashboard");
        updatePageTitle("Dashboard");
    }

    // -------------------------------------------------------------
    // SEÇÃO ESTOQUE
    // -------------------------------------------------------------

    /**
     * Catálogo de Produtos
     * Binding via FXML: btn_catalogo → handleCatalogo
     */
    @FXML
    @SwapFxml(
            container = "multiploPanel",
            viewId = ViewConstant.Main.CATALOGO,
            before = false
    )
    @SwapImage(
            imageView = "iconepage",
            imageId = AppImageConfig.System.IN_STOCK
    )
    public void handleCatalogo(ActionEvent event) {
        LOGGER.info("📦 Navegando para Catálogo de Produtos");
        updatePageTitle("Catálogo de Produtos");
    }

    /**
     * Movimentações de Estoque
     * Binding via FXML: btn_estoque → handleMovimentacoes
     */
    @FXML
    @SwapFxml(
            container = "multiploPanel",
            viewId = ViewConstant.Main.MOVIMENTACOES,
            before = false
    )
    @SwapImage(
            imageView = "iconepage",
            imageId = AppImageConfig.System.IN_STOCK
    )
    public void handleMovimentacoes(ActionEvent event) {
        LOGGER.info("📋 Navegando para Movimentações de Estoque");
        updatePageTitle("Movimentações de Estoque");
    }

    /**
     * Gestão de Stock
     * Binding via FXML: btn_produtoStock → handleEstoque
     */
    @FXML
    @SwapFxml(
            container = "multiploPanel",
            viewId = ViewConstant.Main.ESTOQUE,
            before = false
    )
    @SwapImage(
            imageView = "iconepage",
            imageId = AppImageConfig.System.PRODUTO
    )
    public void handleEstoque(ActionEvent event) {
        LOGGER.info("📊 Navegando para Gestão de Stock");
        updatePageTitle("Gestão de Stock");
    }

    // -------------------------------------------------------------
    // SEÇÃO EQUIPAMENTOS
    // -------------------------------------------------------------

    /**
     * Inventário de Equipamentos
     * Binding via FXML: btn_inventario → handleInventario
     */
    @FXML
    @SwapFxml(
            container = "multiploPanel",
            viewId = ViewConstant.Main.INVENTARIO,
            before = false
    )
    @SwapImage(
            imageView = "iconepage",
            imageId = AppImageConfig.System.LIST
    )
    public void handleInventario(ActionEvent event) {
        LOGGER.info("🔧 Navegando para Inventário de Equipamentos");
        updatePageTitle("Inventário de Equipamentos");
    }

    /**
     * Gestão de Toners
     * Binding via FXML: btn_toners → handleToners
     */
    @FXML
    @SwapFxml(
            container = "multiploPanel",
            viewId = ViewConstant.Main.TONERSDashboard,
            before = false
    )
    @SwapImage(
            imageView = "iconepage",
            imageId = AppImageConfig.System.BALLOT
    )
    public void handleToners(ActionEvent event) {
        LOGGER.info("🖨️ Navegando para Gestão de Toners");
        updatePageTitle("Gestão de Toners");
    }

    // -------------------------------------------------------------
    // SEÇÃO PESSOAL
    // -------------------------------------------------------------

    /**
     * Gestão de Funcionários
     * Binding via FXML: btn_funcionarios → handleFuncionarios
     */
    @FXML
    @SwapFxml(
            container = "multiploPanel",
            viewId = ViewConstant.Main.FUNCIONARIOS,
            before = false
    )
    @SwapImage(
            imageView = "iconepage",
            imageId = AppImageConfig.System.USER
    )
    public void handleFuncionarios(ActionEvent event) {
        LOGGER.info("👤 Navegando para Gestão de Funcionários");
        updatePageTitle("Gestão de Funcionários");
    }

    /**
     * Gestão de Usuários
     * Binding via FXML: btn_usuarios → handleUsuarios
     */
    @FXML
    @SwapFxml(
            container = "multiploPanel",
            viewId = ViewConstant.Main.USUARIO,
            before = false
    )
    @SwapImage(
            imageView = "iconepage",
            imageId = AppImageConfig.System.USER
    )
    public void handleUsuarios(ActionEvent event) {
        LOGGER.info("🔐 Navegando para Gestão de Usuários");
        updatePageTitle("Gestão de Usuários");
    }

    // -------------------------------------------------------------
    // SEÇÃO RELATÓRIOS
    // -------------------------------------------------------------

    /**
     * Relatórios
     * Binding via FXML: btn_relatorio → handleRelatorios
     */
    @FXML
    @SwapFxml(
            container = "multiploPanel",
            viewId = ViewConstant.Main.RELATORIO,
            before = false
    )
    @SwapImage(
            imageView = "iconepage",
            imageId = AppImageConfig.System.REPORT
    )
    public void handleRelatorios(ActionEvent event) {
        LOGGER.info("📈 Navegando para Relatórios");
        updatePageTitle("Relatórios");
    }

    /**
     * Relatório de Tempo
     * Binding via FXML: btn_timeReport → handleTimeReport
     */
    @FXML
    @SwapFxml(
            container = "multiploPanel",
            viewId = ViewConstant.Main.TIME_REPORT,
            before = false
    )
    @SwapImage(
            imageView = "iconepage",
            imageId = AppImageConfig.System.CLOCK
    )
    public void handleTimeReport(ActionEvent event) {
        LOGGER.info("⏰ Navegando para Relatório de Tempo");
        updatePageTitle("Relatório de Tempo");
    }

    /**
     * Histórico de Eventos
     * Binding via FXML: btn_historico → handleHistorico
     */
    @FXML
    @SwapFxml(
            container = "multiploPanel",
            viewId = ViewConstant.Inventario.HistoricoEventos,
            before = false
    )
    @SwapImage(
            imageView = "iconepage",
            imageId = AppImageConfig.System.VIEW
    )
    public void handleHistorico(ActionEvent event) {
        LOGGER.info("📜 Navegando para Histórico de Eventos");
        updatePageTitle("Histórico de Eventos");
    }

    // -------------------------------------------------------------
    // SEÇÃO VISUALIZAÇÃO
    // -------------------------------------------------------------

    /**
     * Visualizar
     * Binding via FXML: btn_visualizar → handleVisualizar
     */
    @FXML
    @SwapFxml(
            container = "multiploPanel",
            viewId = ViewConstant.Main.VISUALIZAR,
            before = false
    )
    @SwapImage(
            imageView = "iconepage",
            imageId = AppImageConfig.System.VIEW
    )
    public void handleVisualizar(ActionEvent event) {
        LOGGER.info("👁️ Navegando para Visualizar");
        updatePageTitle("Visualizar");
    }

    // -------------------------------------------------------------
    // CARREGAMENTO INICIAL (Sem evento)
    // -------------------------------------------------------------

    /**
     * Carrega o Dashboard na inicialização
     * Chamado diretamente pelo @PostConstruct
     */
    @SwapFxml(
            container = "multiploPanel",
            viewId = ViewConstant.Main.DASHBOARD,
            before = false
    )
    @SwapImage(
            imageView = "iconepage",
            imageId = AppImageConfig.System.DASHBOARD
    )
    private void loadDashboard() {
        LOGGER.info("📊 Carregando Dashboard inicial");
        updatePageTitle("Dashboard");
    }

    // ============================================================
    // SISTEMA DE NOTIFICAÇÕES
    // ============================================================

    private void showNotificationsInitially() {
        currentSystemView = ViewConstant.Main.NOTIFICATION;
        loadSystemContentView(ViewConstant.Main.NOTIFICATION);
        expandPanel();
        LOGGER.info("Painel de notificações aberto na inicialização");
    }

    /**
     * Notificações
     * Binding via FXML: btn_notificacao → handleNotificacoes
     */
    @FXML
    @SwapFxml(
            container = "stack_sistema",
            viewId = ViewConstant.Main.NOTIFICATION,
            before = false
    )
    public void handleNotificacoes(ActionEvent event) {
        toggleSystemView(ViewConstant.Main.NOTIFICATION);
    }

    /**
     * Definições
     * Binding via clique na imagem do usuário
     */
    @FXML
    @SwapFxml(
            container = "stack_sistema",
            viewId = ViewConstant.Main.DEFINICOES,
            before = false
    )
    public void handleDefinicoes() {
        toggleSystemView(ViewConstant.Main.DEFINICOES);
    }

    private void toggleSystemView(String targetViewId) {
        if (!currentSystemView.equals(targetViewId)) {
            loadSystemContentView(targetViewId);
            currentSystemView = targetViewId;
            expandPanel();
        } else {
            if (isSystemCollapsed) {
                expandPanel();
            } else {
                collapsePanel();
            }
        }
    }

    private void expandPanel() {
        isSystemCollapsed = false;
        stack_sistema.setPrefWidth(400);
        stack_sistema.setVisible(true);
        stack_sistema.getStyleClass().remove("collapsed");
        stack_sistema.getStyleClass().add("expanded");
    }

    private void collapsePanel() {
        isSystemCollapsed = true;
        stack_sistema.setPrefWidth(0);
        stack_sistema.setVisible(false);
        stack_sistema.getStyleClass().remove("expanded");
        stack_sistema.getStyleClass().add("collapsed");
    }

    @SwapFxml(
            container = "stack_sistema",
            viewId = "#{viewId}",
            before = false
    )
    private void loadSystemContentView(String viewId) {
        LOGGER.debug("🔄 Carregando view do sistema: {}", viewId);
    }

    // ============================================================
    // UTILITÁRIOS
    // ============================================================

    /**
     * Atualiza o título da página
     */
    private void updatePageTitle(String title) {
        if (nomepage != null) {
            nomepage.setText(title);
        }
    }

    // ============================================================
    // GERENCIAMENTO DE USUÁRIO
    // ============================================================

    /**
     * Define o usuário logado após autenticação
     */
    public void setLoggedInUser(Usuario usuario) {
        try {
            LOGGER.info("👤 Recebendo usuário logado: {}", usuario.nome());
            this.usuarioLogado = usuario;
            this.loggedInUserName = usuario.nome();
            updateUserDisplay(usuario.nome());

            if (funcionariosService != null) {
                buscarDadosFuncionario(usuario.funcionarioId());
            }
        } catch (Exception e) {
            LOGGER.error("❌ Erro ao definir usuário logado", e);
            updateUserDisplay(usuario.nome());
        }
    }

    /**
     * Define o usuário logado (fallback com nome)
     */
    public void setLoggedInUser(String userName) {
        try {
            LOGGER.info("👤 Recebendo usuário logado: {}", userName);
            this.loggedInUserName = userName;
            updateUserDisplay(userName);

            if (funcionariosService != null) {
                buscarDadosFuncionarioPorNome(userName);
            }
        } catch (Exception e) {
            LOGGER.error("❌ Erro ao definir usuário logado", e);
            updateUserDisplay(userName);
        }
    }

    private void updateUserDisplay(String userName) {
        try {
            if (nameUser != null) {
                nameUser.setText(userName);
            }
            updateWindowTitle(userName);
        } catch (Exception e) {
            LOGGER.error("❌ Erro ao atualizar display do usuário", e);
        }
    }

    private void buscarDadosFuncionario(String funcionarioId) {
        try {
            if (funcionarioId == null || funcionarioId.isBlank()) {
                return;
            }
            // Converte String para Long (o ID é Long no banco)
            // Mas como o Funcionario usa codDep (String), buscamos pelo codDep
            Optional<Funcionarios> funcionarioOpt = funcionariosService.buscarPorCodDep(funcionarioId);
            funcionarioOpt.ifPresent(this::updateUserDisplayWithFullData);
        } catch (Exception e) {
            LOGGER.error("❌ Erro ao buscar dados do funcionário: {}", e.getMessage());
        }
    }

    private void buscarDadosFuncionarioPorNome(String nome) {
        try {
            List<Funcionarios> funcionarios = funcionariosService.buscarPorNome(nome);
            if (funcionarios != null && !funcionarios.isEmpty()) {
                updateUserDisplayWithFullData(funcionarios.get(0));
            }
        } catch (Exception e) {
            LOGGER.error("❌ Erro ao buscar dados do funcionário: {}", e.getMessage());
        }
    }

    private void updateUserDisplayWithFullData(Funcionarios funcionario) {
        try {
            if (nameUser != null) {
                nameUser.setText(funcionario.nome());
            }
            loadUserImage(funcionario);
        } catch (Exception e) {
            LOGGER.warn("⚠️ Erro ao atualizar com dados completos", e);
            updateUserDisplay(funcionario.nome());
        }
    }

    private void updateWindowTitle(String userName) {
        try {
            if (nameUser != null && nameUser.getScene() != null) {
                var stage = (javafx.stage.Stage) nameUser.getScene().getWindow();
                if (stage != null) {
                    stage.setTitle(String.format("Gestão de TI - Usuário: %s", userName));
                }
            }
        } catch (Exception e) {
            LOGGER.debug("ℹ️ Não foi possível atualizar título da janela");
        }
    }

    /**
     * Carrega a imagem do funcionário (sobrescreve a imagem injetada via @InjectImage)
     */
    private void loadUserImage(Funcionarios funcionario) {
        try {
            if (funcionario != null && funcionario.temImagemPerfil() && imageUser != null) {
                Image image = new Image(new ByteArrayInputStream(funcionario.imagemPerfil()));
                imageUser.setImage(image);
                LOGGER.debug("✅ Imagem do funcionário carregada: {}", funcionario.nome());
            }
        } catch (Exception e) {
            LOGGER.warn("⚠️ Erro ao carregar imagem do funcionário", e);
        }
    }

    // ============================================================
    // EVENTOS
    // ============================================================

    /**
     * Escuta evento de logout para limpar estado
     */
    @EventListener
    public void onLogoutEvent(LogoutEvent event) {
        LOGGER.info("📢 Logout recebido no MainController: {}", event.getUsername());

        this.usuarioLogado = null;
        this.loggedInUserName = null;
        updateUserDisplay(USER_DEFAULT);

        Platform.runLater(() -> {
            if (nameUser != null && nameUser.getScene() != null) {
                var stage = (javafx.stage.Stage) nameUser.getScene().getWindow();
                if (stage != null) {
                    stage.close();
                }
            }
        });
    }

    // ============================================================
    // SHUTDOWN
    // ============================================================

    /**
     * Método chamado quando a janela é fechada
     */
    @FXML
    private void handleWindowClose() {
        LOGGER.info("🔌 Fechando MainController...");
        if (eventBus != null && usuarioLogado != null) {
            eventBus.publish(new LogoutEvent(usuarioLogado.nome()));
        }
    }

    // ============================================================
    // GETTERS
    // ============================================================

    public Usuario getUsuarioLogado() {
        return usuarioLogado;
    }

    public String getLoggedInUser() {
        return loggedInUserName;
    }

    public boolean isAuthenticated() {
        return usuarioLogado != null;
    }
}