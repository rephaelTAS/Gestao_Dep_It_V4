package com.ossobo.gestaoDepIt.main;

import com.ossobo.gestaoDepIt.config.AppImageConfig;
import com.ossobo.gestaoDepIt.config.ViewConstant;
import com.ossobo.gestaoDepIt.db.models.Funcionarios;
import com.ossobo.gestaoDepIt.db.models.Usuario;
import com.ossobo.gestaoDepIt.db.services.FuncionariosService;
import com.ossobo.gestaoDepIt.events.LogoutEvent;
import com.ossobo.winterfx.anotations.Controller;
import com.ossobo.winterfx.anotations.EventListener;
import com.ossobo.winterfx.anotations.Inject;
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
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.ByteArrayInputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * MainController v5.0
 *
 * Alinhado ao main.fxml v2.0 (nomes finais):
 *   appShell           — BorderPane raiz
 *   toggleSidebarBtn   — alterna o menu lateral
 *   menuIcon           — ícone do toggle (ImageView, @InjectImage)
 *   pageIcon/pageName  — contexto da página (breadcrumb)
 *   btnSettings        — definições (ImageView settingsIcon dentro)
 *   btnNotifications   — notificações
 *   userName/userImage — dados do usuário logado
 *   sidebar            — VBox do menu
 *   btnDashboard … btnHistorico — itens de navegação
 *   contentHost        — StackPane do conteúdo central (alvo de @SwapFxml)
 *   asideHost          — StackPane do painel direito
 *   lblFooter          — rodapé
 *
 * v5.0 — Renomeações do FXML v2.0. Ícones individuais do menu removidos do
 *        FXML: o controller passa a operar apenas sobre pageIcon via @SwapImage.
 *        Handlers órfãos (btn_timeReport, btn_visualizar, btn_notificacao)
 *        removidos. stack_sistema → asideHost. multiploPanel → contentHost.
 */
@Controller(proxy = false)
@RegisterView(
        id = ViewConstant.Main.MAIN,
        fxml = "/META-INF/gestaoDepIt/fxmls/main/main.fxml",
        title = "Dashboard - Gestão de TI",
        width = 1200,
        height = 800,
        centered = true,
        primaryCss = "/META-INF/gestaoDepIt/css/main/main.css"
)
public class MainController implements Initializable, WinterFXController {

    private static final Logger LOGGER = System.getLogger(MainController.class.getName());

    private static final double EXPANDED_WIDTH = 240;
    private static final double COLLAPSED_WIDTH = 70;
    private static final String USER_DEFAULT = "Convidado";

    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ============================================================
    // FXML — containers principais
    // ============================================================

    @FXML private StackPane contentHost;
    @FXML private StackPane asideHost;
    @FXML private VBox sidebar;

    // ============================================================
    // Header
    // ============================================================

    @FXML private Button toggleSidebarBtn;
    @FXML @InjectImage(AppImageConfig.System.SETTING)
    private ImageView menuIcon;

    @FXML @InjectImage(AppImageConfig.System.NOTIFICATIONS)
    private ImageView notificationIcon;

    @FXML private ImageView settingsIcon;
    @FXML private ImageView pageIcon;
    @FXML private Label pageName;
    @FXML private Button btnNotifications;

    // ============================================================
    // User info
    // ============================================================

    @FXML @InjectImage(AppImageConfig.System.DEFAULT_USER)
    private ImageView userImage;

    @FXML private Label userName;
    @FXML private Label userInitials;
    @FXML private Label timeLabel;
    @FXML private Label dateLabel;

    // ============================================================
    // Footer
    // ============================================================

    @FXML private Label lblFooter;

    // ============================================================
    // Itens de navegação do menu
    // ============================================================

    @FXML private Button btnDashboard;
    @FXML private Button btnCatalogo;
    @FXML private Button btnEstoque;
    @FXML private Button btnProdutoStock;
    @FXML private Button btnInventario;
    @FXML private Button btnToners;
    @FXML private Button btnFuncionarios;
    @FXML private Button btnUsuarios;
    @FXML private Button btnRelatorio;
    @FXML private Button btnHistorico;

    // ============================================================
    // Dependências
    // ============================================================

    @Inject
    private FuncionariosService funcionariosService;

    @Inject
    private EventBus eventBus;

    // ============================================================
    // Estado
    // ============================================================

    private boolean isMenuCollapsed = false;
    private Usuario usuarioLogado;
    private String loggedInUserName;

    // ============================================================
    // Ciclo de vida
    // ============================================================

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LOGGER.log(Level.INFO, "🏔️ MainController inicializando com WinterFX...");
        try {
            setupMenu();
            setupDateTime();
            updateUserDisplay(USER_DEFAULT);
            loadDashboard();
            LOGGER.log(Level.INFO, "✅ MainController inicializado com sucesso");
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, "❌ Erro durante a inicialização do MainController", e);
        }
    }

    // ============================================================
    // Menu lateral
    // ============================================================

    private void setupMenu() {
        if (sidebar == null) {
            LOGGER.log(Level.WARNING, "⚠️ sidebar não injetado");
            return;
        }
        sidebar.getStyleClass().remove("menu-collapsed");
        sidebar.getStyleClass().add("menu-expanded");
        sidebar.setPrefWidth(EXPANDED_WIDTH);
        isMenuCollapsed = false;
    }

    @FXML
    public void toggleSidebarBtn(ActionEvent event) {
        if (sidebar == null) return;
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
    // Data/hora
    // ============================================================

    private void setupDateTime() {
        Timeline clock = new Timeline(
                new KeyFrame(Duration.ZERO, e -> updateDateTime()),
                new KeyFrame(Duration.seconds(1))
        );
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
    }

    private void updateDateTime() {
        try {
            LocalDateTime now = LocalDateTime.now();
            if (timeLabel != null) timeLabel.setText(now.format(timeFormatter));
            if (dateLabel != null) dateLabel.setText(now.format(dateFormatter));
            if (lblFooter != null) {
                lblFooter.setText(String.format(
                        "© %d Gestão de Equipamentos de TI — Todos os direitos reservados",
                        now.getYear()));
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, "❌ Erro ao atualizar data/hora", e);
        }
    }

    // ============================================================
    // Navegação — handlers dos itens do menu
    // ============================================================

    @FXML
    @SwapFxml(container = "contentHost", viewId = ViewConstant.Main.DASHBOARD, before = false)
    @SwapImage(imageView = "pageIcon", imageId = AppImageConfig.System.DASHBOARD)
    public void btnDashboard(ActionEvent event) {
        updatePageTitle("Dashboard");
    }

    @FXML
    @SwapFxml(container = "contentHost", viewId = ViewConstant.Catalogo.LIST, before = false)
    @SwapImage(imageView = "pageIcon", imageId = AppImageConfig.System.IN_STOCK)
    public void btnCatalogo(ActionEvent event) {
        updatePageTitle("Catálogo");
    }

    @FXML
    @SwapFxml(container = "contentHost", viewId = ViewConstant.Estoque.MOVIMENTACOES_LIST, before = false)
    @SwapImage(imageView = "pageIcon", imageId = AppImageConfig.System.IN_STOCK)
    public void btnEstoque(ActionEvent event) {
        updatePageTitle("Movimentações");
    }

    @FXML
    @SwapFxml(container = "contentHost", viewId = ViewConstant.Estoque.PRODUTO_STOCK, before = false)
    @SwapImage(imageView = "pageIcon", imageId = AppImageConfig.System.PRODUTO)
    public void btnProdutoStock(ActionEvent event) {
        updatePageTitle("Produtos E Inventário");
    }

    @FXML
    @SwapFxml(container = "contentHost", viewId = ViewConstant.Inventario.LIST, before = false)
    @SwapImage(imageView = "pageIcon", imageId = AppImageConfig.System.LIST)
    public void btnInventario(ActionEvent event) {
        updatePageTitle("Inventário");
    }

    @FXML
    @SwapFxml(container = "contentHost", viewId = ViewConstant.Toner.LIST, before = false)
    @SwapImage(imageView = "pageIcon", imageId = AppImageConfig.System.BALLOT)
    public void btnToners(ActionEvent event) {
        updatePageTitle("Toners");
    }

    @FXML
    @SwapFxml(container = "contentHost", viewId = ViewConstant.Funcionario.LIST, before = false)
    @SwapImage(imageView = "pageIcon", imageId = AppImageConfig.System.USER)
    public void btnFuncionarios(ActionEvent event) {
        updatePageTitle("Funcionários");
    }

    @FXML
    @SwapFxml(container = "contentHost", viewId = ViewConstant.Usuario.LIST, before = false)
    @SwapImage(imageView = "pageIcon", imageId = AppImageConfig.System.USER)
    public void btnUsuarios(ActionEvent event) {
        updatePageTitle("Usuários");
    }

    @FXML
    @SwapFxml(container = "contentHost", viewId = ViewConstant.Relatorio.DASHBOARD, before = false)
    @SwapImage(imageView = "pageIcon", imageId = AppImageConfig.System.REPORT)
    public void btnRelatorio(ActionEvent event) {
        updatePageTitle("Relatórios");
    }

    @FXML
    @SwapFxml(container = "contentHost", viewId = ViewConstant.HistoricoEvento.LIST, before = false)
    @SwapImage(imageView = "pageIcon", imageId = AppImageConfig.System.VIEW)
    public void btnHistorico(ActionEvent event) {
        updatePageTitle("Histórico");
    }

    // ============================================================
    // Carga inicial
    // ============================================================

    @SwapFxml(container = "contentHost", viewId = ViewConstant.Main.DASHBOARD, before = false)
    @SwapImage(imageView = "pageIcon", imageId = AppImageConfig.System.DASHBOARD)
    private void loadDashboard() {
        updatePageTitle("Dashboard");
    }

    // ============================================================
    // Painel de sistema (notificações / definições)
    // ============================================================

    @FXML
    @SwapFxml(container = "asideHost", viewId = ViewConstant.Main.NOTIFICATION, before = false)
    public void btnNotifications(ActionEvent event) {
        expandAside();
    }

    @FXML
    @SwapFxml(container = "contentHost", viewId = ViewConstant.Main.DEFINICOES, before = false)
    public void btnSettings(ActionEvent event) {
        updatePageTitle("Definições");
    }

    private void expandAside() {
        if (asideHost == null) return;
        asideHost.setPrefWidth(400);
        asideHost.setVisible(true);
        asideHost.getStyleClass().remove("collapsed");
        asideHost.getStyleClass().add("expanded");
    }

    // ============================================================
    // Utilitários
    // ============================================================

    private void updatePageTitle(String title) {
        if (pageName != null) pageName.setText(title);
    }

    // ============================================================
    // Usuário
    // ============================================================

    public void setLoggedInUser(Usuario usuario) {
        try {
            this.usuarioLogado = usuario;
            this.loggedInUserName = usuario.nome();
            updateUserDisplay(usuario.nome());
            if (funcionariosService != null) {
                buscarDadosFuncionario(usuario.funcionarioId());
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, "❌ Erro ao definir usuário logado", e);
            updateUserDisplay(usuario.nome());
        }
    }

    public void setLoggedInUser(String nomeUsuario) {
        try {
            this.loggedInUserName = nomeUsuario;
            updateUserDisplay(nomeUsuario);
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, "❌ Erro ao definir usuário logado", e);
            updateUserDisplay(nomeUsuario);
        }
    }

    private void updateUserDisplay(String nomeUsuario) {
        try {
            if (userName != null) userName.setText(nomeUsuario);
            if (userInitials != null && nomeUsuario != null && !nomeUsuario.isBlank()) {
                userInitials.setText(String.valueOf(nomeUsuario.charAt(0)).toUpperCase());
            }
            updateWindowTitle(nomeUsuario);
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, "❌ Erro ao atualizar display do usuário", e);
        }
    }

    private void buscarDadosFuncionario(String funcionarioId) {
        try {
            if (funcionarioId == null || funcionarioId.isBlank()) return;
            Optional<Funcionarios> opt = funcionariosService.buscarPorCodDep(funcionarioId);
            opt.ifPresent(this::updateUserDisplayWithFullData);
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, "❌ Erro ao buscar dados do funcionário: {0}", e.getMessage());
        }
    }

    private void updateUserDisplayWithFullData(Funcionarios funcionario) {
        try {
            if (userName != null) userName.setText(funcionario.nome());
            if (userInitials != null && funcionario.nome() != null && !funcionario.nome().isBlank()) {
                userInitials.setText(
                        String.valueOf(funcionario.nome().charAt(0)).toUpperCase());
            }
            loadUserImage(funcionario);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "⚠️ Erro ao atualizar com dados completos", e);
            updateUserDisplay(funcionario.nome());
        }
    }

    private void updateWindowTitle(String nomeUsuario) {
        try {
            if (userName != null && userName.getScene() != null) {
                var stage = (javafx.stage.Stage) userName.getScene().getWindow();
                if (stage != null) {
                    stage.setTitle(String.format("Gestão de TI - Usuário: %s", nomeUsuario));
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.DEBUG, "ℹ️ Não foi possível atualizar título da janela");
        }
    }

    private void loadUserImage(Funcionarios funcionario) {
        try {
            if (funcionario != null && funcionario.temImagemPerfil() && userImage != null) {
                Image image = new Image(new ByteArrayInputStream(funcionario.imagemPerfil()));
                userImage.setImage(image);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "⚠️ Erro ao carregar imagem do funcionário", e);
        }
    }

    // ============================================================
    // Eventos
    // ============================================================

    @EventListener
    public void onLogoutEvent(LogoutEvent event) {
        this.usuarioLogado = null;
        this.loggedInUserName = null;
        updateUserDisplay(USER_DEFAULT);

        Platform.runLater(() -> {
            if (userName != null && userName.getScene() != null) {
                var stage = (javafx.stage.Stage) userName.getScene().getWindow();
                if (stage != null) stage.close();
            }
        });
    }

    @FXML
    private void handleWindowClose() {
        if (eventBus != null && usuarioLogado != null) {
            eventBus.publish(new LogoutEvent(usuarioLogado.nome()));
        }
    }

    // ============================================================
    // Getters
    // ============================================================

    public Usuario getUsuarioLogado() { return usuarioLogado; }
    public String getLoggedInUser() { return loggedInUserName; }
    public boolean isAuthenticated() { return usuarioLogado != null; }
}