package com.ossobo.gestaoDepIt.controllers.main;

import com.ossobo.gestaoDepIt.db.enums.TipoBanco;
import com.ossobo.gestaoDepIt.db.models.ConfigServidorRemoto;
import com.ossobo.gestaoDepIt.db.services.ConfigServidorRemotoService;
import com.ossobo.winterfx.anotations.Controller;
import com.ossobo.winterfx.anotations.Inject;
import com.ossobo.winterfx.anotations.PostConstruct;
import com.ossobo.winterfx.view.anotations.RegisterView;
import com.ossobo.winterfx.view.controller.WinterFXController;
import com.ossobo.winterfx.notifications.anotations.OnError;
import com.ossobo.winterfx.notifications.anotations.OnSuccess;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ConfigServidorController - Controlador para configuração de servidor remoto
 * v2.0 - Corrigido para usar Records corretamente
 *
 * Responsabilidades:
 * - Gerenciar formulário de configuração
 * - Testar conexão com servidor remoto
 * - Salvar/Ativar configuração
 */
@Controller(proxy = false)
@RegisterView(
        id = "config_servidor",
        fxml = "/com/ossobo/gestaoDepIt/fxmls/config/config_servidor.fxml",
        title = "Configuração de Servidor Remoto",
        primaryCss = "/com/ossobo/gestaoDepIt/css/config/config_servidor.css"
)
public class ConfigServidorController implements WinterFXController {

    @FXML private TextField txtNomeConfig;
    @FXML private ComboBox<String> cmbTipoBanco;
    @FXML private TextField txtHost;
    @FXML private TextField txtPorta;
    @FXML private TextField txtDatabase;
    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtSenha;
    @FXML private TextField txtParametros;
    @FXML private Label lblStatus;
    @FXML private Label lblConexao;

    @Inject private ConfigServidorRemotoService configService;

    private ConfigServidorRemoto configEditando;
    private boolean modoEdicao = false;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ============================================================
    // INICIALIZAÇÃO
    // ============================================================

    @PostConstruct
    public void init() {
        configurarComboBox();
        carregarConfiguracao();
        atualizarStatusUI();
    }

    // ============================================================
    // MÉTODOS PRIVADOS
    // ============================================================

    private void configurarComboBox() {
        for (TipoBanco tipo : TipoBanco.values()) {
            cmbTipoBanco.getItems().add(tipo.getDisplayName());
        }
        cmbTipoBanco.getSelectionModel().select("MySQL");
    }

    private void carregarConfiguracao() {
        try {
            // ✅ CORRIGIDO: usa orElse(null) para Optional
            ConfigServidorRemoto ativa = configService.buscarAtiva().orElse(null);
            if (ativa != null) {
                preencherFormulario(ativa);
                configEditando = ativa;
                modoEdicao = true;
                lblStatus.setText("Status: " + ativa.statusConexao()); // ✅ Record accessor
                lblConexao.setText("Última conexão: " +
                        (ativa.ultimaConexao() != null ? ativa.ultimaConexao() : "--")); // ✅ Record accessor
            }
        } catch (Exception e) {
            System.err.println("Erro ao carregar configuração: " + e.getMessage());
        }
    }

    private void preencherFormulario(ConfigServidorRemoto config) {
        // ✅ CORRIGIDO: usa accessors do Record
        txtNomeConfig.setText(config.nomeConfig());
        cmbTipoBanco.getSelectionModel().select(
                TipoBanco.valueOf(config.tipoBanco()).getDisplayName()
        );
        txtHost.setText(config.host());
        txtPorta.setText(config.porta());
        txtDatabase.setText(config.databaseName());
        txtUsuario.setText(config.usuario());
        txtSenha.setText(config.senha());
        txtParametros.setText(config.parametrosExtra() != null ? config.parametrosExtra() : "");
    }

    private ConfigServidorRemoto criarConfigDoFormulario() {
        String tipoBanco = cmbTipoBanco.getSelectionModel().getSelectedItem();
        if (tipoBanco == null || tipoBanco.isEmpty()) {
            tipoBanco = "MySQL";
        }

        // Mapeia display name para enum
        String tipoEnum = "";
        for (TipoBanco tb : TipoBanco.values()) {
            if (tb.getDisplayName().equals(tipoBanco)) {
                tipoEnum = tb.name();
                break;
            }
        }

        // ✅ CORRIGIDO: usa o construtor completo do Record (14 argumentos)
        if (modoEdicao && configEditando != null) {
            // Mantém o ID e timestamps existentes
            return new ConfigServidorRemoto(
                    configEditando.id(),                      // mantém ID
                    txtNomeConfig.getText().trim(),
                    tipoEnum,
                    txtHost.getText().trim(),
                    txtPorta.getText().trim(),
                    txtDatabase.getText().trim(),
                    txtUsuario.getText().trim(),
                    txtSenha.getText().trim(),
                    txtParametros.getText() != null ? txtParametros.getText().trim() : "",
                    configEditando.ativo(),                   // mantém status ativo
                    configEditando.ultimaConexao(),           // mantém última conexão
                    configEditando.statusConexao(),           // mantém status
                    configEditando.createdAt(),               // mantém data criação
                    LocalDateTime.now()                       // atualiza updatedAt
            );
        } else {
            // Cria nova configuração com valores padrão
            return new ConfigServidorRemoto(
                    null,                                     // ID null (novo)
                    txtNomeConfig.getText().trim(),
                    tipoEnum,
                    txtHost.getText().trim(),
                    txtPorta.getText().trim(),
                    txtDatabase.getText().trim(),
                    txtUsuario.getText().trim(),
                    txtSenha.getText().trim(),
                    txtParametros.getText() != null ? txtParametros.getText().trim() : "",
                    1,                                        // ativo = 1 (padrão)
                    null,                                     // ultimaConexao = null
                    "NAO_TESTADO",                            // status inicial
                    LocalDateTime.now(),                      // createdAt
                    LocalDateTime.now()                       // updatedAt
            );
        }
    }

    private void atualizarStatusUI() {
        try {
            // ✅ CORRIGIDO: usa orElse(null) para Optional
            ConfigServidorRemoto ativa = configService.buscarAtiva().orElse(null);
            if (ativa != null) {
                String status = ativa.statusConexao(); // ✅ Record accessor
                lblStatus.setText("Status: " + status);
                String cor = switch (status) {
                    case "CONECTADO" -> "#4CAF50";
                    case "FALHA" -> "#F44336";
                    case "TESTANDO" -> "#FF9800";
                    default -> "#9E9E9E";
                };
                lblStatus.setStyle("-fx-text-fill: " + cor + ";");
            } else {
                lblStatus.setText("Status: Nenhuma configuração ativa");
            }
        } catch (Exception e) {
            lblStatus.setText("Status: Erro ao carregar");
        }
    }

    private void fechar() {
        txtNomeConfig.getScene().getWindow().hide();
    }

    // ============================================================
    // MÉTODOS DE BOTÃO (ActionEvent)
    // ============================================================

    @OnSuccess(descricao = "Conexão testada com sucesso!")
    @OnError(titulo = "Erro de Conexão", descricao = "Falha ao testar conexão", detalhe = "Verifique as credenciais e a rede")
    public void btnTestar(ActionEvent event) {
        try {
            ConfigServidorRemoto config = criarConfigDoFormulario();

            // ✅ CORRIGIDO: usa accessors do Record
            if (config.nomeConfig().isEmpty()) {
                throw new IllegalArgumentException("Nome da configuração é obrigatório");
            }
            if (config.host().isEmpty()) {
                throw new IllegalArgumentException("Host é obrigatório");
            }
            if (config.porta().isEmpty()) {
                throw new IllegalArgumentException("Porta é obrigatória");
            }

            Platform.runLater(() -> {
                lblStatus.setText("Status: TESTANDO...");
                lblStatus.setStyle("-fx-text-fill: #FF9800;");
            });

            String mensagem = configService.testarConexaoComMensagem(config);

            Platform.runLater(() -> {
                if (mensagem.contains("✅")) {
                    lblStatus.setText("Status: CONECTADO");
                    lblStatus.setStyle("-fx-text-fill: #4CAF50;");
                } else {
                    lblStatus.setText("Status: FALHA");
                    lblStatus.setStyle("-fx-text-fill: #F44336;");
                }
                lblConexao.setText("Última conexão: " +
                        LocalDateTime.now().format(FORMATTER));
            });

            if (mensagem.contains("❌") || mensagem.contains("Falha")) {
                throw new RuntimeException(mensagem);
            }

        } catch (Exception e) {
            throw new RuntimeException("Erro ao testar conexão: " + e.getMessage());
        }
    }

    @OnSuccess(titulo = "Sucesso", descricao = "Configuração salva com sucesso!")
    @OnError(titulo = "Erro", descricao = "Falha ao salvar configuração", detalhe = "Verifique os campos obrigatórios")
    public void btnSalvar(ActionEvent event) {
        try {
            ConfigServidorRemoto config = criarConfigDoFormulario();

            // ✅ CORRIGIDO: usa accessors do Record
            if (config.nomeConfig().isEmpty()) {
                throw new IllegalArgumentException("Nome da configuração é obrigatório");
            }

            if (modoEdicao && configEditando != null) {
                configService.atualizar(config);
            } else {
                configService.salvar(config);
            }

            // Recarrega a configuração salva
            configEditando = configService.buscarPorId(config.id()).orElse(null);
            modoEdicao = true;
            atualizarStatusUI();

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @OnSuccess(titulo = "Ativado", descricao = "Configuração ativada com sucesso!")
    @OnError(titulo = "Erro", descricao = "Falha ao ativar configuração", detalhe = "Salve a configuração antes de ativá-la")
    public void btnAtivar(ActionEvent event) {
        try {
            if (configEditando == null || configEditando.id() == null) { // ✅ Record accessor
                throw new IllegalStateException("Salve a configuração antes de ativá-la");
            }

            configService.definirComoAtiva(configEditando.id()); // ✅ Record accessor

            // Recarrega a configuração ativa
            ConfigServidorRemoto ativa = configService.buscarAtiva().orElse(null);
            if (ativa != null) {
                preencherFormulario(ativa);
                configEditando = ativa;
                atualizarStatusUI();
            }

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void btnFechar(ActionEvent event) {
        fechar();
    }
}