package com.ossobo.gestaoDepIt.login;

import com.ossobo.gestaoDepIt.config.ViewConstant;
import com.ossobo.gestaoDepIt.db.models.Usuario;
import com.ossobo.gestaoDepIt.events.LoginSuccessEvent;
import com.ossobo.gestaoDepIt.events.LogoutEvent;
import com.ossobo.winterfx.anotations.Controller;
import com.ossobo.winterfx.anotations.EventListener;
import com.ossobo.winterfx.anotations.Inject;
import com.ossobo.winterfx.event.EventBus;
import com.ossobo.winterfx.notifications.anotations.OnError;
import com.ossobo.winterfx.notifications.anotations.OnSuccess;
import com.ossobo.winterfx.router.Rotas;
import com.ossobo.winterfx.router.model.Params;
import com.ossobo.winterfx.router.model.ResponseData;
import com.ossobo.winterfx.view.anotations.NewScene;
import com.ossobo.winterfx.view.anotations.RegisterView;
import com.ossobo.winterfx.view.controller.WinterFXController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

/**
 * LoginController v1.3
 *
 * Login síncrono (BCrypt ~200ms, aceitável em desktop local).
 * Fluxo canônico do WinterFX:
 *   @OnError/@OnSuccess/@NewScene empilham no pipeline AFTER.
 *   Falha → lança exceção → @OnError mostra; @NewScene NÃO dispara.
 *   Sucesso → retorna normal → @NewScene troca a cena.
 *
 * v1.3 — Substitui o padrão async (thread separada + @NewScene em
 *        método próprio) pelo canônico: tudo dentro do handler.
 *        Motivo: o pipeline AFTER só avalia o retorno do MÉTODO anotado.
 */
@Controller(proxy = false)
@RegisterView(
        id = ViewConstant.Main.LOGIN,
        fxml = "/META-INF/gestaoDepIt/fxmls/login/login.fxml",
        title = "Login - Gestão de TI",
        centered = true,
        resizable = false,
        primaryCss = "/META-INF/gestaoDepIt/css/login/login.css"
)
public class LoginController implements WinterFXController {

    private static final Logger LOGGER = System.getLogger(LoginController.class.getName());

    private static final String ROTA_LOGIN = "usuarios/service/auth/login";
    private static final String ROTA_INVALIDAR_SESSAO = "usuarios/service/sessao/invalidar";

    @FXML private TextField usuario;
    @FXML private PasswordField senha;
    @FXML private Label lblStatus;

    @Inject private EventBus eventBus;

    private Usuario usuarioAutenticado;

    @OnSuccess(descricao = "Login realizado com sucesso!")
    @OnError(titulo = "Erro", descricao = "Falha no login", detalhe = "Verifique suas credenciais")
    @NewScene(
            view = ViewConstant.Main.MAIN,
            title = "Main - Gestão de TI",
            centered = true,
            closeCurrent = true
    )
    @FXML
    public void btn_login(ActionEvent event) {
        String username = usuario != null ? usuario.getText().trim() : "";
        String password = senha != null ? senha.getText() : "";

        if (username.isBlank() || password.isBlank()) {
            throw new RuntimeException("Preencha usuário e senha.");
        }

        Object resposta = Rotas.exec(
                ROTA_LOGIN,
                Params.with("identificador", username)
                        .and("senha", password)
                        .and("ip", "local"));

        if (!(resposta instanceof ResponseData rd) || !rd.isSuccess()) {
            throw new RuntimeException("Falha ao contatar o serviço de autenticação.");
        }

        Boolean conectado = rd.getDataBool("conectado");
        if (!Boolean.TRUE.equals(conectado)) {
            throw new RuntimeException("Credenciais inválidas.");
        }

        Usuario user = rd.getData("usuario", Usuario.class);
        if (user == null) {
            throw new RuntimeException("Usuário não retornado pelo serviço.");
        }

        this.usuarioAutenticado = user;
        LOGGER.log(Level.INFO, "✅ Login: {0} ({1})",
                user.nome(), user.nivelAcesso());

        if (eventBus != null) {
            eventBus.publish(new LoginSuccessEvent(user));
        }
    }

    @FXML
    public void btn_cancelar(ActionEvent event) {
        LOGGER.log(Level.INFO, "❌ Cancelando login");
        System.exit(0);
    }

    @EventListener
    public void onLogoutEvent(LogoutEvent event) {
        LOGGER.log(Level.INFO, "📢 Logout: {0}", event.getUsername());
        if (usuarioAutenticado != null) {
            try {
                Rotas.exec(ROTA_INVALIDAR_SESSAO,
                        Params.with("id", usuarioAutenticado.id()));
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "⚠️ Erro ao invalidar sessão: {0}", e.getMessage());
            }
        }
    }

    public Usuario getUsuarioAutenticado() { return usuarioAutenticado; }
    public boolean isAuthenticated() { return usuarioAutenticado != null; }
}