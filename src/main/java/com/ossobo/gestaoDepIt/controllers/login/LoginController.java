package com.ossobo.gestaoDepIt.controllers.login;

import com.ossobo.gestaoDepIt.config.ViewConstant;
import com.ossobo.gestaoDepIt.db.models.Usuario;
import com.ossobo.gestaoDepIt.events.LoginSuccessEvent;
import com.ossobo.gestaoDepIt.events.LogoutEvent;
import com.ossobo.winterfx.anotations.Controller;
import com.ossobo.winterfx.anotations.EventListener;
import com.ossobo.winterfx.anotations.Inject;
import com.ossobo.winterfx.anotations.PostConstruct;
import com.ossobo.winterfx.event.EventBus;
import com.ossobo.winterfx.router.Rotas;
import com.ossobo.winterfx.router.model.Params;
import com.ossobo.winterfx.router.model.ResponseData;
import com.ossobo.winterfx.view.anotations.NewScene;
import com.ossobo.winterfx.view.anotations.RegisterView;
import com.ossobo.winterfx.view.controller.WinterFXController;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 🎯 LoginController - Tela de Login (WinterFX)
 * v5.0 - Política v1.0: ZERO Service injetado — consome a rota consolidada
 *        "usuarios/service/auth/login" (D1: autentica + registra login + cria sessão).
 *
 * Mudanças v4.1 → v5.0:
 * - @Inject UsuariosService REMOVIDO — comunicação exclusivamente via Rotas
 * - Fluxo manual (autenticar → registrarLogin → criarSessao) colapsa em 1 rota EXEC
 * - senhaHash JÁ vem mascarado pela fronteira (SEC-1) — controller não trata hash
 * - @Inject EventBus mantido (publicação de evento de aplicação — caso 🟡 a confirmar)
 */
@Controller(proxy = false)
@RegisterView(
        id = ViewConstant.Main.LOGIN,
        fxml = "/META-INF/gestaoDepIt/fxmls/login/login.fxml",
        title = "Login - Gestão de TI",
        width = 450,
        height = 350,
        centered = true,
        resizable = false,
        primaryCss = "/META-INF/gestaoDepIt/css/login/login.css"
)
public class LoginController implements WinterFXController {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginController.class);

    /** Rota consolidada de login (D1) — contrato: UsuariosRoutes. */
    private static final String ROTA_LOGIN = "usuarios/service/auth/login";
    private static final String ROTA_INVALIDAR_SESSAO = "usuarios/service/sessao/invalidar";

    // ===== FXML =====
    @FXML private TextField usuarioField;
    @FXML private PasswordField senhaField;
    @FXML private Button btnLogin;
    @FXML private Button btnCancelar;
    @FXML private Label lblStatus;
    @FXML private ImageView logoImageView;

    // ===== INJEÇÃO (apenas infra de comunicação) =====
    @Inject
    private EventBus eventBus;

    // ===== ESTADO =====
    private Usuario usuarioAutenticado;
    private boolean isLoggingIn = false;

    @PostConstruct
    public void init() {
        LOGGER.info("✅ LoginController pronto (via Rotas — sem service injetado)");
        setupEventHandlers();
        applyInitialStyles();
        Platform.runLater(() -> { if (usuarioField != null) usuarioField.requestFocus(); });
    }

    // ===== HANDLERS FXML =====

    @FXML
    public void handleLogin(ActionEvent event) {
        LOGGER.info("🔐 Tentativa de login iniciada");

        if (isLoggingIn) {
            LOGGER.warn("⏳ Login já em andamento");
            return;
        }

        String username = usuarioField != null ? usuarioField.getText().trim() : "";
        String password = senhaField != null ? senhaField.getText() : "";

        if (username.isEmpty() || password.isEmpty()) {
            showStatusError("Preencha todos os campos!");
            return;
        }

        doLogin(username, password);
    }

    @FXML
    public void handleCancel(ActionEvent event) {
        LOGGER.info("❌ Cancelando login");
        Platform.exit();
        System.exit(0);
    }

    // ===== AUTENTICAÇÃO VIA ROTA (SEC-2: BCrypt fora da FX Thread) =====

    private void doLogin(String username, String password) {
        isLoggingIn = true;
        setUIEnabled(false);
        showStatusInfo("Autenticando...");

        new Thread(() -> {
            try {
                Object resposta = Rotas.exec(ROTA_LOGIN,
                        Params.with("identificador", username)
                                .and("senha", password)
                                .and("ip", "127.0.0.1"));
                Platform.runLater(() -> tratarRespostaLogin(resposta, username));
            } catch (Exception e) {
                LOGGER.error("❌ Erro na autenticação", e);
                Platform.runLater(() -> handleAuthenticationError(e));
            } finally {
                Platform.runLater(() -> {
                    isLoggingIn = false;
                    setUIEnabled(true);
                });
            }
        }, "Login-Auth-Thread").start();
    }

    /** Contrato da rota auth/login: conectado/token/expiramin/usuario (hash mascarado). */
    private void tratarRespostaLogin(Object resposta, String username) {
        if (!(resposta instanceof ResponseData r) || !r.isSuccess()
                || !Boolean.TRUE.equals(r.getData().get("conectado"))) {
            LOGGER.warn("❌ Login falhou: {}", username);
            handleAuthenticationFailure();
            return;
        }

        Usuario usuario = (Usuario) r.getData().get("usuario");
        this.usuarioAutenticado = usuario;

        String token = String.valueOf(r.getData().get("token"));
        LOGGER.info("✅ Login bem-sucedido: {} ({}) | sessão {}…",
                usuario.nome(), usuario.nivelAcesso(),
                token.substring(0, Math.min(8, token.length())));

        handleAuthenticationSuccess(usuario);
    }

    // ===== RESULTADOS =====

    private void handleAuthenticationSuccess(Usuario usuario) {
        showStatusSuccess("✅ Bem-vindo, " + usuario.nome() + "!");
        navigateToDashboard(usuario);
    }

    private void handleAuthenticationFailure() {
        showStatusError("❌ Credenciais inválidas!");
        if (senhaField != null) senhaField.clear();
        if (usuarioField != null) usuarioField.requestFocus();
    }

    private void handleAuthenticationError(Exception e) {
        showStatusError("❌ Erro ao autenticar: " + e.getMessage());
        LOGGER.error("❌ Erro de autenticação", e);
    }

    // ===== NAVEGAÇÃO =====

    @NewScene(
            view = ViewConstant.Main.MAIN,
            title = "Main - Gestão de TI",
            width = 1200,
            height = 800,
            centered = true,
            closeCurrent = true
    )
    public void navigateToDashboard(Usuario usuario) {
        LOGGER.info("📊 Navegando para Dashboard: {}", usuario.nome());
        if (eventBus != null) {
            eventBus.publish(new LoginSuccessEvent(usuario)); // hash já mascarado pela fronteira
        }
    }

    // ===== TECLADO / UI (intocados da v4.x) =====

    private void setupEventHandlers() {
        if (usuarioField != null) {
            usuarioField.setOnAction(e -> { if (senhaField != null) senhaField.requestFocus(); });
        }
        if (senhaField != null) {
            senhaField.setOnAction(this::handleLoginTeclado);
        }
        if (btnLogin != null) {
            btnLogin.setOnKeyPressed(this::handleKeyPress);
        }
    }

    private void handleLoginTeclado(ActionEvent e) {
        handleLogin(e);
    }

    private void handleKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            handleLogin(new ActionEvent());
        }
    }

    private void applyInitialStyles() {
        if (lblStatus != null) {
            lblStatus.setText("Digite suas credenciais");
            lblStatus.getStyleClass().add("status-info");
        }
    }

    private void setUIEnabled(boolean enabled) {
        if (usuarioField != null) usuarioField.setDisable(!enabled);
        if (senhaField != null) senhaField.setDisable(!enabled);
        if (btnLogin != null) {
            btnLogin.setDisable(!enabled);
            btnLogin.setText(enabled ? "Entrar" : "Autenticando...");
        }
        if (btnCancelar != null) btnCancelar.setDisable(!enabled);
    }

    private void showStatusInfo(String m)    { setStatus(m, "status-info"); }
    private void showStatusSuccess(String m) { setStatus(m, "status-success"); }
    private void showStatusError(String m)   { setStatus(m, "status-error"); }

    private void setStatus(String m, String classe) {
        if (lblStatus != null) {
            lblStatus.setText(m);
            lblStatus.getStyleClass().removeAll("status-error", "status-info", "status-success");
            lblStatus.getStyleClass().add(classe);
        }
    }

    // ===== EVENTOS =====

    @EventListener
    public void onLogoutEvent(LogoutEvent event) {
        LOGGER.info("📢 Logout: {}", event.getUsername());
        if (usuarioAutenticado != null) {
            try {
                Rotas.exec(ROTA_INVALIDAR_SESSAO, Params.with("id", usuarioAutenticado.id()));
                LOGGER.info("✅ Sessão invalidada");
            } catch (Exception e) {
                LOGGER.warn("⚠️ Erro ao invalidar sessão: {}", e.getMessage());
            }
        }
    }

    // ===== GETTERS =====

    public Usuario getUsuarioAutenticado() { return usuarioAutenticado; }
    public boolean isAuthenticated()       { return usuarioAutenticado != null; }
}