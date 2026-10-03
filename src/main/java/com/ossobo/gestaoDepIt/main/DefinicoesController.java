package com.ossobo.gestaoDepIt.main;

import com.ossobo.gestaoDepIt.config.ViewConstant;
import com.ossobo.gestaoDepIt.db.models.ConfigServidorRemoto;
import com.ossobo.winterfx.anotations.Controller;
import com.ossobo.winterfx.anotations.Inject;
import com.ossobo.winterfx.anotations.PostConstruct;
import com.ossobo.winterfx.event.EventBus;
import com.ossobo.winterfx.resources.enums.ViewType;
import com.ossobo.winterfx.router.Rotas;
import com.ossobo.winterfx.router.model.Params;
import com.ossobo.winterfx.router.model.ResponseData;
import com.ossobo.winterfx.view.anotations.InjectView;
import com.ossobo.winterfx.view.anotations.RegisterView;
import com.ossobo.winterfx.view.controller.WinterFXController;
import com.ossobo.winterfx.view.floatingwindow.StageForFloatingWindow;
import com.ossobo.winterfx.view.floatingwindow.anotations.FloatingWindow;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

@Controller(proxy = false)
@RegisterView(
        id = "definicoes",
        fxml = "/META-INF/gestaoDepIt/fxmls/main/definicoes.fxml",
        title = "Definições - Gestão Dep IT",
        width = 900,
        height = 700,
        centered = true,
        primaryCss = "/css/main/definicoes.css",
        viewType = ViewType.STATIC
)
public class DefinicoesController implements Initializable, WinterFXController {

    private static final Logger LOGGER = System.getLogger(DefinicoesController.class.getName());

    // ========================


    @Inject
    private EventBus eventBus;

    @FloatingWindow(
            viewId = ViewConstant.Server.FORM,
            singleton = false
    )
    public StageForFloatingWindow formServer;


    // ========================
    // COMPONENTES FXML - ABA GERAL
    // ========================
    @FXML private ComboBox<String> cmbTema;
    @FXML private ComboBox<String> cmbIdioma;
    @FXML private TextField txtDiretorioBackup;
    @FXML private Button btnSelecionarBackup;
    @FXML private ComboBox<String> cmbFrequenciaBackup;
    @FXML private Spinner<Integer> spinnerQuantidadeBackups;
    @FXML private CheckBox chkIniciarComWindows;
    @FXML private CheckBox chkVerificarAtualizacoes;
    @FXML private CheckBox chkModoEconomia;
    @FXML private StackPane spane_contentBanc;

    // ========================
    // COMPONENTES FXML - ABA BANCO
    // ========================
    @FXML private TableView<ConfigServidorRemoto> tblServidores;
    @FXML private TableColumn<ConfigServidorRemoto, String> colStatus;
    @FXML private TableColumn<ConfigServidorRemoto, String> colNome;
    @FXML private TableColumn<ConfigServidorRemoto, String> colTipo;
    @FXML private TableColumn<ConfigServidorRemoto, String> colHost;
    @FXML private TableColumn<ConfigServidorRemoto, String> colPorta;
    @FXML private TableColumn<ConfigServidorRemoto, String> colDatabase;
    @FXML private TableColumn<ConfigServidorRemoto, String> colUsuario;
    @FXML private TableColumn<ConfigServidorRemoto, String> colUltimaConexao;
    @FXML private TableColumn<ConfigServidorRemoto, Void> colAcoes;

    @FXML private Label lblTotal;
    @FXML private Label lblStatusConexao;

    @FXML private Button btnNovo;
    @FXML private Button btnRefresh;
    @FXML private Button btnTestarTodos;

    // ========================
    // COMPONENTES FXML - ABA SINCRONIZAÇÃO
    // ========================
    @FXML private CheckBox chkSincronizarAutomatico;
    @FXML private Spinner<Integer> spinnerIntervalo;
    @FXML private CheckBox chkSincronizarAoIniciar;
    @FXML private CheckBox chkModoOffline;
    @FXML private TextField txtArquivoOffline;
    @FXML private Button btnAlterarOffline;
    @FXML private Label lblTamanhoOffline;
    @FXML private Label lblUltimaExportacao;
    @FXML private Button btnExportarOffline;
    @FXML private Button btnImportarOffline;
    @FXML private Button btnLimparOffline;
    @FXML private Label lblUltimaSincronizacao;
    @FXML private Label lblStatusSync;
    @FXML private Label lblRegistrosSync;

    // ========================
    // COMPONENTES FXML - ABA NOTIFICAÇÕES
    // ========================
    @FXML private CheckBox chkNotificacoesAtivas;
    @FXML private CheckBox chkSomNotificacoes;
    @FXML private Spinner<Integer> spinnerTempoNotificacao;
    @FXML private ComboBox<String> cmbTipoNotificacao;

    // ========================
    // COMPONENTES FXML - ABA LOGS
    // ========================
    @FXML private ComboBox<String> cmbNivelLog;
    @FXML private Spinner<Integer> spinnerDiasLogs;
    @FXML private TextField txtDiretorioLogs;
    @FXML private Button btnSelecionarLogs;
    @FXML private Button btnExportarLogs;
    @FXML private Button btnLimparLogs;
    @FXML private Button btnAbrirPastaLogs;

    // ========================
    // COMPONENTES FXML - RODAPÉ
    @FXML private Button btnRestaurarPadroes;
    @FXML private Button btnCancelarDefinicoes;
    @FXML private Button btnSalvarDefinicoes;

    // ========================
    // CICLO DE VIDA
    // ========================

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LOGGER.log(Level.INFO, "🔧 Inicializando controlador de Definições");
        configurarTabelaServidores();
        configurarEventHandlers();
        carregarConfiguracoes();
    }

    // ========================
    // CONFIGURAÇÃO DE EVENTOS
    // ========================

    private void configurarEventHandlers() {
        // Aba Geral
        btnSelecionarBackup.setOnAction(e -> selecionarDiretorio(txtDiretorioBackup));


        // Aba Sincronização
        btnAlterarOffline.setOnAction(e -> selecionarArquivoOffline());
        btnExportarOffline.setOnAction(e -> exportarOffline());
        btnImportarOffline.setOnAction(e -> importarOffline());
        btnLimparOffline.setOnAction(e -> limparOffline());

        // Aba Logs
        btnSelecionarLogs.setOnAction(e -> selecionarDiretorio(txtDiretorioLogs));
        btnExportarLogs.setOnAction(e -> exportarLogs());
        btnLimparLogs.setOnAction(e -> limparLogs());
        btnAbrirPastaLogs.setOnAction(e -> abrirPastaLogs());

        // Rodapé
        btnRestaurarPadroes.setOnAction(e -> restaurarPadroes());
        btnCancelarDefinicoes.setOnAction(e -> fecharJanela());
        btnSalvarDefinicoes.setOnAction(e -> salvarTodasConfiguracoes());
    }

    // ========================
    // MÉTODOS AUXILIARES - DIÁLOGO DE DIRETÓRIO
    // ========================

    /**
     * Abre um seletor de diretório e preenche o campo de texto.
     * Protegido contra NullPointer se o botão ou a cena não estiverem prontos.
     */
    private void selecionarDiretorio(TextField campo) {
        if (campo == null) {
            LOGGER.log(Level.WARNING, "⚠️ Campo de texto é nulo, não é possível selecionar diretório");
            return;
        }

        try {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Selecionar Diretório");

            Stage stage = obterStage();
            if (stage == null) {
                LOGGER.log(Level.WARNING, "⚠️ Stage não disponível");
                return;
            }

            File diretorio = chooser.showDialog(stage);
            if (diretorio != null) {
                campo.setText(diretorio.getAbsolutePath());
                LOGGER.log(Level.INFO, "📁 Diretório selecionado: {0}", diretorio.getAbsolutePath());
            }

        } catch (Exception e) {
            LOGGER.log(Level.ERROR, "❌ Erro ao selecionar diretório", e);
        }
    }

    /**
     * Obtém o Stage de forma segura, a partir de qualquer botão disponível.
     */
    private Stage obterStage() {
        // Tenta obter o Stage do botão de backup
        if (btnSelecionarBackup != null && btnSelecionarBackup.getScene() != null) {
            return (Stage) btnSelecionarBackup.getScene().getWindow();
        }

        // Fallback: tenta usar o botão de cancelar
        if (btnCancelarDefinicoes != null && btnCancelarDefinicoes.getScene() != null) {
            return (Stage) btnCancelarDefinicoes.getScene().getWindow();
        }

        // Fallback: tenta usar qualquer outro botão
        if (btnSalvarDefinicoes != null && btnSalvarDefinicoes.getScene() != null) {
            return (Stage) btnSalvarDefinicoes.getScene().getWindow();
        }

        LOGGER.log(Level.WARNING, "⚠️ Nenhum botão disponível para obter o Stage");
        return null;
    }

    private void fecharJanela() {
        try {
            Stage stage = obterStage();
            if (stage != null) {
                stage.close();
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, "❌ Erro ao fechar janela", e);
        }
    }

    // ========================
    // MÉTODOS DE NEGÓCIO (Implementar)
    // ========================

    private void carregarConfiguracoes() {
        // TODO: Carregar do ConfiguracaoService
        LOGGER.log(Level.DEBUG, "📋 Carregando configurações...");
    }

    private void testarConexao() {
        // TODO: Testar conexão MySQL
        LOGGER.log(Level.DEBUG, "🔌 Testando conexão com banco...");
    }

    private void salvarConfiguracoesBanco() {
        // TODO: Salvar no ConfiguracaoService
        LOGGER.log(Level.DEBUG, "💾 Salvando configurações de banco...");
    }

    private void otimizarBancoLocal() {
        // TODO: VACUUM no SQLite
        LOGGER.log(Level.DEBUG, "🔄 Otimizando banco local...");
    }

    private void exportarOffline() {
        // TODO: Exportar dados para offline
        LOGGER.log(Level.DEBUG, "📤 Exportando dados offline...");
    }

    private void importarOffline() {
        // TODO: Importar dados do offline
        LOGGER.log(Level.DEBUG, "📥 Importando dados offline...");
    }

    private void limparOffline() {
        // TODO: Limpar dados offline
        LOGGER.log(Level.DEBUG, "🗑️ Limpando dados offline...");
    }

    private void exportarLogs() {
        // TODO: Exportar logs
        LOGGER.log(Level.DEBUG, "📤 Exportando logs...");
    }

    private void limparLogs() {
        // TODO: Limpar logs antigos
        LOGGER.log(Level.DEBUG, "🗑️ Limpando logs antigos...");
    }

    private void abrirPastaLogs() {
        // TODO: Abrir pasta no explorer
        LOGGER.log(Level.DEBUG, "📂 Abrindo pasta de logs...");
    }

    private void selecionarArquivoOffline() {
        // TODO: Selecionar arquivo .db offline
        LOGGER.log(Level.DEBUG, "📂 Selecionando arquivo offline...");
    }

    private void restaurarPadroes() {
        // TODO: Restaurar configurações padrão
        LOGGER.log(Level.DEBUG, "🔄 Restaurando configurações padrão...");
    }

    private void salvarTodasConfiguracoes() {
        // TODO: Salvar todas as configurações
        LOGGER.log(Level.INFO, "💾 Salvando todas as configurações...");
        fecharJanela();
    }

    private void configurarTabelaServidores() {
        // No DefinicoesController (dono do definicoes.fxml), DENTRO do @PostConstruct
// (que já roda na FX Thread — requisito do canal UI):

// REGRA DE PAREAMENTO: @ExecMapping → Rotas.exec | @UiMapping → Rotas.ui
// Se o facade não casar com a anotação, lookup() não acha e você recebe
// ResponseData.error("Rota inexistente") SEM exceção — erro silencioso.
        ResponseData r = Rotas.exec("/servidor/list/controll/configurar/colunas", Params.empty()
                .and("tblServidores",     tblServidores)     // chaves = nomes EXATOS dos @UI(...)
                .and("colStatus",         colStatus)
                .and("colNome",           colNome)
                .and("colTipo",           colTipo)
                .and("colHost",           colHost)
                .and("colPorta",          colPorta)
                .and("colDatabase",       colDatabase)
                .and("colUsuario",        colUsuario)
                .and("colUltimaConexao",  colUltimaConexao)
                .and("colAcoes",          colAcoes)
                .and("lblTotal",          lblTotal)
                .and("lblStatusConexao",  lblStatusConexao));


        if (!r.isSuccess()) {
            LOGGER.log(Level.ERROR, "❌ Falha ao montar aba Banco: {0}", r.getMessage());
        }
    }

    public void btnNovo(ActionEvent event) {
        System.out.println("btnNovo");
        formServer.show();
    }
}