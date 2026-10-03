package com.ossobo.gestaoDepIt.login;

import com.ossobo.gestaoDepIt.config.ViewConstant;
import com.ossobo.gestaoDepIt.db.models.Usuario;
import com.ossobo.gestaoDepIt.events.LoginSuccessEvent;
import com.ossobo.gestaoDepIt.events.LogoutEvent;
import com.ossobo.winterfx.anotations.Controller;
import com.ossobo.winterfx.anotations.EventListener;
import com.ossobo.winterfx.anotations.Inject;
import com.ossobo.winterfx.anotations.PostConstruct;
import com.ossobo.winterfx.event.EventBus;
import com.ossobo.winterfx.notifications.anotations.OnError;
import com.ossobo.winterfx.router.Rotas;
import com.ossobo.winterfx.router.model.Params;
import com.ossobo.winterfx.router.model.ResponseData;
import com.ossobo.winterfx.view.anotations.NewScene;
import com.ossobo.winterfx.view.anotations.RegisterView;
import com.ossobo.winterfx.view.controller.WinterFXController;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.URL;
import java.util.ResourceBundle;

@Controller(proxy = false)
@RegisterView(
        id = ViewConstant.Main.LOGIN,
        fxml = "/META-INF/gestaoDepIt/fxmls/login/login.fxml",
        title = "Login - Gestão de TI",
        centered = true,
        resizable = false,
        primaryCss = "/META-INF/gestaoDepIt/css/login/login.css"
)
public class LoginController implements Initializable, WinterFXController {

    private static final Logger LOGGER = System.getLogger(LoginController.class.getName());

    // ✅ ROTA CORRETA: auth/login (EXEC)
    private static final String ROTA_LOGIN = "usuarios/service/por/nome";
    private static final String ROTA_INVALIDAR_SESSAO = "usuarios/service/sessao/invalidar";

    @FXML private TextField usuarioField;
    @FXML private PasswordField senhaField;
    @FXML private Button btnLogin;
    @FXML private Button btnCancelar;
    @FXML private Label lblStatus;

    @Inject private EventBus eventBus;

    private Usuario usuarioAutenticado;
    private boolean isLoggingIn = false;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LOGGER.log(Level.INFO, "✅ LoginController pronto");
        applyInitialStyles();
        Platform.runLater(() -> {
            if (usuarioField != null) usuarioField.requestFocus();
        });
    }

    @FXML
    @NewScene(
            view = ViewConstant.Main.MAIN,
            title = "Main - Gestão de TI",
            width = 1500,
            height = 900,
            centered = true,
            closeCurrent = true
    )
    @OnError(descricao = "Campos Vazios", detalhe = "")
    public void btn_login(ActionEvent event) {
        if (isLoggingIn) {
            LOGGER.log(Level.WARNING, "⏳ Login já em andamento");
            return;
        }

        String username = usuarioField != null ? usuarioField.getText().trim() : "";
        String password = senhaField != null ? senhaField.getText() : "";

        if (username.isBlank() || password.isBlank()) {
            showStatusError("Preencha todos os campos!");
        }

        doLogin(username, password);
        System.out.println("Login com Sucesso!");
    }

    @FXML
    public void btn_cancelar(ActionEvent event) {
        LOGGER.log(Level.INFO, "❌ Cancelando login");
        Platform.exit();
        System.exit(0);
    }

    private void doLogin(String username, String password) {
        isLoggingIn = true;
        setUIEnabled(false);
        showStatusInfo("Autenticando...");

        new Thread(() -> {
            try {
                // ✅ Rota EXEC: auth/login
                // Parâmetros: identificador (codDep ou email), senha, ip
                ResponseData resposta = Rotas.get(
                        ROTA_LOGIN,
                        Params.with("nome", username)
                );

                Platform.runLater(() -> tratarRespostaLogin(resposta));

            } catch (Exception e) {
                LOGGER.log(Level.ERROR, "❌ Erro na autenticação", e);
                Platform.runLater(() -> handleAuthenticationError(e));
            } finally {
                Platform.runLater(() -> {
                    isLoggingIn = false;
                    setUIEnabled(true);
                });
            }
        }, "Login-Auth-Thread").start();
    }

    private void tratarRespostaLogin(ResponseData resposta) {
        // Verifica se a resposta é de sucesso
        if (resposta == null || !resposta.isSuccess()) {
            LOGGER.log(Level.WARNING, "❌ Login falhou: resposta de erro");
            handleAuthenticationFailure();
            return;
        }

        // Verifica se conectado é true
        Boolean conectado = resposta.getDataBool("conectado");
        if (!Boolean.TRUE.equals(conectado)) {
            LOGGER.log(Level.WARNING, "❌ Login falhou: credenciais inválidas");
            handleAuthenticationFailure();
            return;
        }

        // Obtém o usuário (já com senhaHash mascarado pela fronteira SEC-1)
        Usuario usuario = resposta.getData("usuario", Usuario.class);
        if (usuario == null) {
            LOGGER.log(Level.ERROR, "❌ Usuário não retornado pela rota");
            handleAuthenticationFailure();
            return;
        }

        this.usuarioAutenticado = usuario;

        String token = resposta.getDataString("token");
        Integer expiraMin = resposta.getDataInt("expiramin");

        LOGGER.log(Level.INFO, "✅ Login bem-sucedido: {0} ({1})",
                usuario.nome(), usuario.nivelAcesso());

        handleAuthenticationSuccess(usuario);
    }

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
        LOGGER.log(Level.ERROR, "❌ Erro de autenticação", e);
    }


    public void navigateToDashboard(Usuario usuario) {
        LOGGER.log(Level.INFO, "📊 Navegando para Dashboard: {0}", usuario.nome());
        if (eventBus != null) {
            eventBus.publish(new LoginSuccessEvent(usuario));
        }
    }

    // ===== UTILITÁRIOS =====

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

    @EventListener
    public void onLogoutEvent(LogoutEvent event) {
        LOGGER.log(Level.INFO, "📢 Logout: {0}", event.getUsername());
        if (usuarioAutenticado != null) {
            try {
                Rotas.exec(ROTA_INVALIDAR_SESSAO, Params.with("id", usuarioAutenticado.id()));
                LOGGER.log(Level.INFO, "✅ Sessão invalidada");
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "⚠️ Erro ao invalidar sessão: {0}", e.getMessage());
            }
        }
    }

    public Usuario getUsuarioAutenticado() { return usuarioAutenticado; }
    public boolean isAuthenticated() { return usuarioAutenticado != null; }




}