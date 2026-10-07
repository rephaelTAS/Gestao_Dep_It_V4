package com.ossobo.gestaoDepIt.controlls.usuarios;

import com.ossobo.gestaoDepIt.config.ViewConstant;
import com.ossobo.gestaoDepIt.db.enums.Hierarquia;
import com.ossobo.gestaoDepIt.db.models.Usuario;
import com.ossobo.winterfx.anotations.Controller;
import com.ossobo.winterfx.anotations.PostConstruct;
import com.ossobo.winterfx.router.Rotas;
import com.ossobo.winterfx.router.model.Params;
import com.ossobo.winterfx.router.model.ResponseData;
import com.ossobo.winterfx.view.anotations.RegisterView;
import com.ossobo.winterfx.view.controller.WinterFXController;
import com.ossobo.winterfx.view.floatingwindow.StageForFloatingWindow;
import com.ossobo.winterfx.view.floatingwindow.anotations.FloatingWindow;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * UsuarioListController v1.0
 *
 * Lista + filtros + paginação de usuários.
 * Zero chamadas diretas a Service; tudo via Rotas.
 */
@Controller(proxy = false)
@RegisterView(
        id = ViewConstant.Usuario.LIST,
        fxml = "/META-INF/gestaoDepIt/fxmls/usuario/UsuarioList.fxml",
        primaryCss = "/META-INF/gestaoDepIt/css/usuario/usuario-list.css",
        title = "Gestão de Usuários"
)
public class UsuarioListController implements Initializable, WinterFXController {

    private static final System.Logger logger =
            System.getLogger(UsuarioListController.class.getName());

    private static final DateTimeFormatter FMT_DATA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ---- Filtros ----
    @FXML private TextField filtroNome;
    @FXML private TextField filtroEmail;
    @FXML private ComboBox<String> filtroNivel;
    @FXML private ComboBox<String> filtroStatus;
    @FXML private ComboBox<String> filtroSessao;
    @FXML private Button btn_limparFiltros;
    @FXML private Button btn_aplicarFiltros;

    // ---- Cabeçalho ----
    @FXML private Button btn_novoUsuario;

    // ---- Estatísticas ----
    @FXML private Label estatisticasLabel;
    @FXML private Label sessoesAtivasLabel;

    @FloatingWindow(
            viewId = ViewConstant.Usuario.FORM,
            singleton = false
    )
    private StageForFloatingWindow formUsuario;

    // ---- Tabela ----
    @FXML private TableView<Usuario> usuariosTable;
    @FXML private TableColumn<Usuario, String> colNome;
    @FXML private TableColumn<Usuario, String> colEmail;
    @FXML private TableColumn<Usuario, String> colFuncionario;
    @FXML private TableColumn<Usuario, String> colNivelAcesso;
    @FXML private TableColumn<Usuario, String> colStatus;
    @FXML private TableColumn<Usuario, String> colUltimoLogin;
    @FXML private TableColumn<Usuario, String> colIpLogin;
    @FXML private TableColumn<Usuario, String> colSessaoAtiva;
    @FXML private TableColumn<Usuario, Object> colAcoes;

    // ---- Paginação ----
    @FXML private Button btn_primeira;
    @FXML private Button btn_anterior;
    @FXML private Label labelPagina;
    @FXML private Button btn_proxima;
    @FXML private Button btn_ultima;
    @FXML private ComboBox<Integer> comboItensPorPagina;

    // ---- Status ----
    @FXML private Label statusLabel;
    @FXML private ProgressIndicator progressIndicator;

    // ---- Estado interno ----
    private int pagina = 1;
    private int tamanhoPagina = 20;
    private int totalRegistros = 0;
    private boolean usandoFiltros = false;

    // ============================================================
    // CICLO DE VIDA
    // ============================================================

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        inicializarColunas();
        inicializarFiltrosDominio();
        inicializarPaginacao();
        carregarEstatisticas();
        carregarUsuarios();
    }

    // ============================================================
    // BINDING DE COLUNAS
    // ============================================================

    private void inicializarColunas() {
        if (colNome != null)
            colNome.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().nome()));
        if (colEmail != null)
            colEmail.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().email()));
        if (colFuncionario != null)
            colFuncionario.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().funcionarioId()));
        if (colNivelAcesso != null)
            colNivelAcesso.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNivelDescricao()));
        if (colStatus != null)
            colStatus.setCellValueFactory(d -> new SimpleStringProperty(
                    d.getValue().isAtivo() ? "Ativo" : "Inativo"));
        if (colUltimoLogin != null)
            colUltimoLogin.setCellValueFactory(d -> new SimpleStringProperty(
                    formatar(d.getValue().ultimoLogin())));
        if (colIpLogin != null)
            colIpLogin.setCellValueFactory(d -> new SimpleStringProperty(
                    valorOuTraco(d.getValue().ipUltimoLogin())));
        if (colSessaoAtiva != null)
            colSessaoAtiva.setCellValueFactory(d -> new SimpleStringProperty(
                    d.getValue().hasSessaoAtiva() ? "Sim" : "Não"));
        if (colAcoes != null)
            colAcoes.setCellValueFactory(d -> new SimpleObjectProperty<>(""));
    }

    private String formatar(LocalDateTime dt) {
        return dt == null ? "—" : dt.format(FMT_DATA_HORA);
    }

    private String valorOuTraco(String s) {
        return (s == null || s.isBlank()) ? "—" : s;
    }

    // ============================================================
    // FILTROS DE DOMÍNIO
    // ============================================================

    private void inicializarFiltrosDominio() {
        if (filtroNivel != null) {
            filtroNivel.getItems().setAll(Hierarquia.todos());
        }
        if (filtroStatus != null) {
            filtroStatus.getItems().setAll("Ativo", "Inativo");
        }
        if (filtroSessao != null) {
            filtroSessao.getItems().setAll("Com sessão", "Sem sessão");
        }
    }

    private void inicializarPaginacao() {
        if (comboItensPorPagina != null) {
            comboItensPorPagina.getItems().setAll(10, 20, 50, 100);
            comboItensPorPagina.setValue(tamanhoPagina);
            comboItensPorPagina.setOnAction(e -> {
                Integer v = comboItensPorPagina.getValue();
                if (v != null) {
                    tamanhoPagina = v;
                    pagina = 1;
                    carregarUsuarios();
                }
            });
        }
        atualizarBotoesPaginacao();
    }

    // ============================================================
    // AÇÕES
    // ============================================================

    @FXML
    public void btn_aplicarFiltros(ActionEvent event) {
        pagina = 1;
        usandoFiltros = true;
        carregarUsuarios();
    }

    @FXML
    public void btn_limparFiltros(ActionEvent event) {
        if (filtroNome != null) filtroNome.clear();
        if (filtroEmail != null) filtroEmail.clear();
        if (filtroNivel != null) filtroNivel.setValue(null);
        if (filtroStatus != null) filtroStatus.setValue(null);
        if (filtroSessao != null) filtroSessao.setValue(null);
        usandoFiltros = false;
        pagina = 1;
        carregarUsuarios();
    }

    @FXML
    public void btn_novoUsuario(ActionEvent event) {
        formUsuario.show();
        Rotas.exec("usuario-list/abrir-form");
    }

    @FXML public void btn_primeira(ActionEvent e) { irPara(1); }
    @FXML public void btn_anterior(ActionEvent e) { irPara(Math.max(1, pagina - 1)); }
    @FXML public void btn_proxima(ActionEvent e)  { irPara(Math.min(totalPaginas(), pagina + 1)); }
    @FXML public void btn_ultima(ActionEvent e)   { irPara(totalPaginas()); }

    // ============================================================
    // CARREGAMENTO
    // ============================================================

    @SuppressWarnings("unchecked")
    private void carregarUsuarios() {
        mostrarProgresso(true);
        try {
            Params p = construirParams();
            String rota = usandoFiltros ? "usuarios/service/com-filtros" : "usuarios/service/todos";
            Object r = Rotas.get(rota, p);
            aplicarResposta(r, rota);
        } catch (Exception e) {
            logger.log(System.Logger.Level.ERROR, "Falha ao carregar usuários", e);
            aviso("Erro ao carregar usuários: " + e.getMessage());
            if (usuariosTable != null) usuariosTable.getItems().clear();
        } finally {
            mostrarProgresso(false);
            atualizarBotoesPaginacao();
        }
    }

    private Params construirParams() {
        Params p = Params.empty();
        if (usandoFiltros) {
            if (filtroNome != null && notBlank(filtroNome.getText()))
                p.and("nome", filtroNome.getText().trim());
            if (filtroEmail != null && notBlank(filtroEmail.getText()))
                p.and("email", filtroEmail.getText().trim());
            if (filtroNivel != null && filtroNivel.getValue() != null)
                p.and("nivel", filtroNivel.getValue());
            if (filtroStatus != null && filtroStatus.getValue() != null)
                p.and("ativo", "Ativo".equals(filtroStatus.getValue()));
            if (filtroSessao != null && filtroSessao.getValue() != null)
                p.and("comsessao", "Com sessão".equals(filtroSessao.getValue()));
        }
        return p;
    }

    @SuppressWarnings("unchecked")
    private void aplicarResposta(Object resposta, String rota) {
        if (!(resposta instanceof ResponseData rd) || !rd.isSuccess()) {
            if (resposta instanceof ResponseData rdErr)
                logger.log(System.Logger.Level.WARNING,
                        "Rota {0} devolveu erro: {1}", rota, rdErr.getMessage());
            if (usuariosTable != null) usuariosTable.getItems().clear();
            totalRegistros = 0;
            return;
        }
        Object dados = rd.getData().get("usuarios");
        if (dados instanceof List<?> l) {
            List<Usuario> usuarios = l.stream()
                    .filter(Usuario.class::isInstance)
                    .map(Usuario.class::cast)
                    .toList();
            if (usuariosTable != null)
                usuariosTable.setItems(FXCollections.observableArrayList(usuarios));
            Object t = rd.getData().get("total");
            totalRegistros = (t instanceof Number n) ? n.intValue() : usuarios.size();
            aviso("Carregados " + usuarios.size() + " usuário(s).");
        }
    }

    private void carregarEstatisticas() {
        try {
            long total = valorLong("usuarios/service/total", "total");
            long ativos = valorLong("usuarios/service/total/ativos", "total");
            long comSessao = valorLong("usuarios/service/com-sessao-ativa", "total");

            if (estatisticasLabel != null)
                estatisticasLabel.setText(
                        "Total: " + total + "   •   Ativos: " + ativos);
            if (sessoesAtivasLabel != null)
                sessoesAtivasLabel.setText("Sessões ativas: " + comSessao);
        } catch (Exception e) {
            logger.log(System.Logger.Level.WARNING,
                    "Falha ao carregar estatísticas: {0}", e.getMessage());
        }
    }

    // ============================================================
    // PAGINAÇÃO
    // ============================================================

    private void irPara(int nova) {
        if (nova < 1 || nova > totalPaginas()) return;
        pagina = nova;
        carregarUsuarios();
    }

    private int totalPaginas() {
        return Math.max(1, (int) Math.ceil((double) totalRegistros / tamanhoPagina));
    }

    private void atualizarBotoesPaginacao() {
        if (labelPagina != null)
            labelPagina.setText("Página " + pagina + " de " + totalPaginas());
        if (btn_primeira != null) btn_primeira.setDisable(pagina <= 1);
        if (btn_anterior != null) btn_anterior.setDisable(pagina <= 1);
        if (btn_proxima != null)  btn_proxima.setDisable(pagina >= totalPaginas());
        if (btn_ultima != null)   btn_ultima.setDisable(pagina >= totalPaginas());
    }

    // ============================================================
    // UTIL
    // ============================================================

    private long valorLong(String rota, String chave) {
        try {
            Object r = Rotas.get(rota);
            if (r instanceof ResponseData rd && rd.isSuccess()) {
                Object v = rd.getData().get(chave);
                if (v instanceof Number n) return n.longValue();
                Object lista = rd.getData().get("usuarios");
                if (lista instanceof List<?> l) return l.size();
            }
        } catch (Exception ignored) {}
        return 0L;
    }

    private boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }

    private void mostrarProgresso(boolean visivel) {
        if (progressIndicator != null) {
            progressIndicator.setVisible(visivel);
            progressIndicator.setManaged(visivel);
        }
    }

    private void aviso(String msg) {
        if (statusLabel != null) statusLabel.setText(msg);
    }
}