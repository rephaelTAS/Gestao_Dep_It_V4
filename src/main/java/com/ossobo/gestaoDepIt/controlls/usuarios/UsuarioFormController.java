package com.ossobo.gestaoDepIt.controlls.usuarios;

import com.ossobo.gestaoDepIt.config.ViewConstant;
import com.ossobo.gestaoDepIt.db.enums.Hierarquia;
import com.ossobo.gestaoDepIt.db.models.Funcionarios;
import com.ossobo.gestaoDepIt.db.models.Usuario;
import com.ossobo.winterfx.anotations.Controller;
import com.ossobo.winterfx.anotations.PostConstruct;
import com.ossobo.winterfx.router.Rotas;
import com.ossobo.winterfx.router.model.Params;
import com.ossobo.winterfx.router.model.ResponseData;
import com.ossobo.winterfx.view.anotations.RegisterView;
import com.ossobo.winterfx.view.controller.WinterFXController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * UsuarioFormController v1.0
 *
 * Formulário de criação/edição de usuário. Zero Service injetado.
 * Modo novo/edição via rota usuario-form/service/setFuncionario.
 */
@Controller(proxy = false)
@RegisterView(
        id = ViewConstant.Usuario.FORM,
        fxml = "/META-INF/gestaoDepIt/fxmls/usuario/UsuarioForm.fxml",
        primaryCss = "/META-INF/gestaoDepIt/css/usuario/usuario-form.css",
        title = "Usuário"
)
public class UsuarioFormController implements Initializable, WinterFXController {

    private static final System.Logger logger =
            System.getLogger(UsuarioFormController.class.getName());

    @FXML private Label lab_criarEditar;
    @FXML private ComboBox<String> combo_funcionario;
    @FXML private Label lab_codDep;
    @FXML private Label lab_nome;
    @FXML private Label lab_departamento;
    @FXML private Label lab_funcao;
    @FXML private PasswordField fiel_novasenha;
    @FXML private PasswordField fiel_confirmarSenha;
    @FXML private ComboBox<Hierarquia> combo_nivelPermicao;
    @FXML private Button btn_cancelar;
    @FXML private Button btn_salvar;

    private String funcionarioSelecionado;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        popularNiveis();
        popularFuncionarios();
        if (combo_funcionario != null) {
            combo_funcionario.getSelectionModel().selectedItemProperty()
                    .addListener((obs, ov, nv) -> onFuncionarioEscolhido(nv));
        }
    }

    // ============================================================
    // AÇÕES
    // ============================================================

    @FXML
    public void btn_cancelar(ActionEvent event) {
        fechar();
    }

    @FXML
    public void btn_salvar(ActionEvent event) {
        if (funcionarioSelecionado == null) { aviso("Selecione um funcionário."); return; }
        String senha = fiel_novasenha != null ? fiel_novasenha.getText() : null;
        String confirma = fiel_confirmarSenha != null ? fiel_confirmarSenha.getText() : null;
        if (senha == null || senha.isBlank()) { aviso("Informe a senha."); return; }
        if (!senha.equals(confirma)) { aviso("As senhas não coincidem."); return; }
        Hierarquia nivel = combo_nivelPermicao != null ? combo_nivelPermicao.getValue() : null;
        if (nivel == null) { aviso("Selecione o nível de permissão."); return; }

        // 1) Buscar o funcionário para copiar nome/email
        Funcionarios funcionario = buscarFuncionario(funcionarioSelecionado);
        if (funcionario == null) { aviso("Funcionário não encontrado."); return; }
        if (funcionario.email() == null || funcionario.email().isBlank()) {
            aviso("Funcionário sem email cadastrado — não é possível criar usuário.");
            return;
        }

        // 2) Montar Usuario com hash — depende do service (ver pergunta 2)
        Usuario novo = Usuario.novoComNivel(
                funcionario.codDep(),
                funcionario.nome(),
                funcionario.email(),
                senha,              // ← texto puro ou hash, conforme o service
                nivel);

        // 3) Enviar pela rota
        Object r = Rotas.put("usuarios/service/criar", Params.with("usuario", novo));
        if (r instanceof ResponseData rd) {
            if (rd.isSuccess()) {
                aviso("Usuário criado: " + novo.nome());
                fechar();
            } else {
                aviso("Erro: " + rd.getFirstError());
            }
        }
    }

    private Funcionarios buscarFuncionario(String codDep) {
        try {
            Object r = Rotas.get("funcionarios/service/por/coddep", Params.with("coddep", codDep));
            if (r instanceof ResponseData rd && rd.isSuccess()) {
                Object f = rd.getData().get("funcionario");
                if (f instanceof Funcionarios func) return func;
            }
        } catch (Exception e) {
            logger.log(System.Logger.Level.WARNING, "Falha ao buscar funcionário", e);
        }
        return null;
    }

    // ============================================================
    // CARREGAMENTO
    // ============================================================

    private void popularNiveis() {
        if (combo_nivelPermicao == null) return;
        combo_nivelPermicao.getItems().setAll(Hierarquia.values());
        combo_nivelPermicao.setValue(Hierarquia.OPERADOR);
    }

    @SuppressWarnings("unchecked")
    private void popularFuncionarios() {
        if (combo_funcionario == null) return;
        try {
            Object r = Rotas.get("funcionarios/service/todos");
            if (r instanceof ResponseData rd && rd.isSuccess()) {
                Object dados = rd.getData().get("funcionarios");
                if (dados instanceof List<?> l) {
                    List<String> codDeps = l.stream()
                            .filter(com.ossobo.gestaoDepIt.db.models.Funcionarios.class::isInstance)
                            .map(com.ossobo.gestaoDepIt.db.models.Funcionarios.class::cast)
                            .map(com.ossobo.gestaoDepIt.db.models.Funcionarios::codDep)
                            .filter(s -> s != null && !s.isBlank())
                            .sorted()
                            .toList();
                    combo_funcionario.getItems().setAll(codDeps);
                }
            }
        } catch (Exception e) {
            logger.log(System.Logger.Level.WARNING,
                    "Falha ao carregar funcionários: {0}", e.getMessage(), e);
        }
    }

    private void onFuncionarioEscolhido(String codDep) {
        if (codDep == null || codDep.isBlank()) return;
        funcionarioSelecionado = codDep;
        if (lab_codDep != null) lab_codDep.setText(codDep);
        try {
            Object r = Rotas.get("funcionarios/service/por/coddep", Params.with("coddep", codDep));
            if (r instanceof ResponseData rd && rd.isSuccess()) {
                Object f = rd.getData().get("funcionario");
                if (lab_nome != null && f != null)
                    lab_nome.setText(extrair(f, "nome"));
                if (lab_departamento != null && f != null)
                    lab_departamento.setText(extrair(f, "departamento"));
                if (lab_funcao != null && f != null)
                    lab_funcao.setText(extrair(f, "funcao"));
            }
        } catch (Exception e) {
            logger.log(System.Logger.Level.WARNING,
                    "Falha ao buscar funcionário: {0}", e.getMessage());
        }
    }

    private String extrair(Object obj, String metodo) {
        try {
            Object v = obj.getClass().getMethod(metodo).invoke(obj);
            return v == null ? "—" : v.toString();
        } catch (Exception e) {
            return "—";
        }
    }

    // ============================================================
    // UTIL
    // ============================================================

    private void fechar() {
        if (btn_cancelar != null && btn_cancelar.getScene() != null
                && btn_cancelar.getScene().getWindow() != null) {
            btn_cancelar.getScene().getWindow().hide();
        }
    }

    private void aviso(String msg) {
        logger.log(System.Logger.Level.INFO, msg);
    }



}