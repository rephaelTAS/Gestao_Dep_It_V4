package com.ossobo.gestaoDepIt.main;

import com.ossobo.gestaoDepIt.config.ViewConstant;
import com.ossobo.gestaoDepIt.db.models.ConfigServidorRemoto;
import com.ossobo.gestaoDepIt.db.services.ConfigServidorRemotoService;
import com.ossobo.winterfx.anotations.*;
import com.ossobo.winterfx.notifications.anotations.OnError;
import com.ossobo.winterfx.notifications.anotations.OnSuccess;
import com.ossobo.winterfx.router.Rotas;
import com.ossobo.winterfx.router.model.Params;
import com.ossobo.winterfx.view.anotations.NewScene;
import com.ossobo.winterfx.view.anotations.RegisterView;
import com.ossobo.winterfx.view.anotations.SwapFxml;
import com.ossobo.winterfx.view.controller.WinterFXController;
import com.ossobo.winterfx.view.floatingwindow.anotations.FloatingWindow;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * ServidorDetailController - Detalhes da configuração do servidor
 * v2.1 - Corrigido API Params
 *
 * Responsabilidades:
 * - Exibir detalhes completos da configuração
 * - Permitir ativar/desativar/testar
 * - Navegar para edição
 */
@Controller(proxy = false)
@RegisterView(
        id = ViewConstant.Server.DETAIL,
        fxml = "/META-INF/gestaoDepIt/fxmls/main/servidor_detail.fxml",
        title = "Detalhes do Servidor",
        primaryCss = "/com/ossobo/gestaoDepIt/css/config/servidor_detail.css"
)
public class ServidorDetailController implements Initializable, WinterFXController {

    // ============================================================
    // @FXML INJECTIONS
    // ============================================================

    @FloatingWindow(viewId = ViewConstant.Server.FORM, title = "Editar Servidor", singleton = false)

    @FXML private Label txtTitulo;
    @FXML private Label lblStatus;
    @FXML private Label lblNome;
    @FXML private Label lblTipo;
    @FXML private Label lblHost;
    @FXML private Label lblPorta;
    @FXML private Label lblDatabase;
    @FXML private Label lblUsuario;
    @FXML private Label lblSenha;
    @FXML private Label lblParametros;
    @FXML private Label lblUltimaConexao;
    @FXML private Label lblCreatedAt;
    @FXML private Label lblUpdatedAt;
    @FXML private Label lblUrl;

    // ============================================================
    // @INJECT SERVICES
    // ============================================================

    @Inject private ConfigServidorRemotoService configService;

    // ============================================================
    // CONSTANTES
    // ============================================================

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final String VIEW_SERVIDOR_FORM = ViewConstant.Server.FORM;

    // ============================================================
    // ESTADO
    // ============================================================

    private ConfigServidorRemoto configAtual;
    private Long configIdCarregado;

    // ============================================================
    // INICIALIZAÇÃO
    // ============================================================

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        carregarConfiguracaoAtiva();
    }

    // ============================================================
    // CARREGAMENTO DE DADOS
    // ============================================================

    private void carregarConfiguracaoAtiva() {
        try {
            Optional<ConfigServidorRemoto> ativa = configService.buscarAtiva();
            if (ativa.isPresent()) {
                configAtual = ativa.get();
                configIdCarregado = configAtual.id();
                preencherDetalhes(configAtual);
            } else {
                lblStatus.setText("Nenhuma configuração ativa");
                lblStatus.setStyle("-fx-text-fill: #9E9E9E;");
                txtTitulo.setText("🔍 Detalhes do Servidor");
            }
        } catch (Exception e) {
            System.err.println("Erro ao carregar configuração ativa: " + e.getMessage());
            lblStatus.setText("Erro ao carregar");
            lblStatus.setStyle("-fx-text-fill: #F44336;");
        }
    }

    /**
     * Carrega uma configuração específica pelo ID.
     */
    public void carregarConfiguracaoPorId(Long id) {
        try {
            Optional<ConfigServidorRemoto> config = configService.buscarPorId(id);
            if (config.isPresent()) {
                configAtual = config.get();
                configIdCarregado = id;
                preencherDetalhes(configAtual);
            } else {
                lblStatus.setText("Configuração não encontrada (ID: " + id + ")");
                lblStatus.setStyle("-fx-text-fill: #F44336;");
            }
        } catch (Exception e) {
            System.err.println("Erro ao carregar configuração: " + e.getMessage());
            lblStatus.setText("Erro ao carregar: " + e.getMessage());
            lblStatus.setStyle("-fx-text-fill: #F44336;");
        }
    }

    /**
     * ✅ MÉTODO DE ROTA CORRETO:
     * Recebe o ID via @RouteVar do Params enviado pelo Rotas.post()
     *
     * Exemplo de chamada:
     * Rotas.post("config_servidor/carregar", Params.with("id", 10L));
     */
    @PutMapping("carregar")
    public void carregarConfiguracaoPorRota(@RouteVar("id") Long id) {
        carregarConfiguracaoPorId(id);
    }

    private void preencherDetalhes(ConfigServidorRemoto config) {
        txtTitulo.setText("🔍 Detalhes do Servidor - " + config.nomeConfig());

        lblNome.setText(config.nomeConfig());
        lblTipo.setText(config.tipoBanco());
        lblHost.setText(config.host());
        lblPorta.setText(config.porta());
        lblDatabase.setText(config.databaseName());
        lblUsuario.setText(config.usuario());
        lblSenha.setText("••••••••");
        lblParametros.setText(
                config.parametrosExtra() != null && !config.parametrosExtra().isEmpty()
                        ? config.parametrosExtra()
                        : "--"
        );

        lblUltimaConexao.setText(
                config.ultimaConexao() != null && !config.ultimaConexao().isEmpty()
                        ? config.ultimaConexao()
                        : "--"
        );

        lblCreatedAt.setText(
                config.createdAt() != null
                        ? config.createdAt().format(FORMATTER)
                        : "--"
        );

        lblUpdatedAt.setText(
                config.updatedAt() != null
                        ? config.updatedAt().format(FORMATTER)
                        : "--"
        );

        lblUrl.setText(config.gerarUrlConexao());

        String status = config.statusConexao() != null ? config.statusConexao() : "NAO_TESTADO";
        lblStatus.setText("Status: " + status);

        String color = switch (status) {
            case "CONECTADO" -> "#4CAF50";
            case "FALHA" -> "#F44336";
            case "TESTANDO" -> "#FF9800";
            default -> "#9E9E9E";
        };
        lblStatus.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
        lblStatus.getStyleClass().setAll("status-badge", status.toLowerCase().replace("_", "-"));
    }

    // ============================================================
    // MÉTODOS DE AÇÃO
    // ============================================================

    @OnSuccess(titulo = "Sucesso", descricao = "Conexão testada com sucesso!")
    @OnError(titulo = "Erro de Conexão", descricao = "Falha ao testar conexão",
            detalhe = "Verifique as credenciais e a conectividade da rede")
    public void btnTestar(ActionEvent event) throws SQLException {
        if (configAtual == null) {
            throw new IllegalStateException("Nenhuma configuração para testar");
        }

        Platform.runLater(() -> {
            lblStatus.setText("Status: TESTANDO...");
            lblStatus.setStyle("-fx-text-fill: #FF9800; -fx-font-weight: bold;");
        });

        boolean conectado = configService.testarConexao(configAtual);

        Platform.runLater(() -> {
            if (conectado) {
                String agora = LocalDateTime.now().format(FORMATTER);
                lblStatus.setText("Status: CONECTADO");
                lblStatus.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
                lblUltimaConexao.setText(agora);

                try {
                    Optional<ConfigServidorRemoto> recarregado = configService.buscarPorId(configAtual.id());
                    if (recarregado.isPresent()) {
                        configAtual = recarregado.get();
                        configIdCarregado = configAtual.id();
                        lblUltimaConexao.setText(
                                configAtual.ultimaConexao() != null ? configAtual.ultimaConexao() : agora
                        );
                    }
                } catch (Exception e) {
                    System.err.println("Erro ao recarregar configuração: " + e.getMessage());
                }
            } else {
                lblStatus.setText("Status: FALHA");
                lblStatus.setStyle("-fx-text-fill: #F44336; -fx-font-weight: bold;");
                throw new RuntimeException("Falha ao testar conexão com " + configAtual.nomeConfig());
            }
        });

        if (!conectado) {
            throw new RuntimeException("Falha ao testar conexão com " + configAtual.nomeConfig());
        }
    }

    @OnSuccess(titulo = "Sucesso", descricao = "Servidor ativado com sucesso!")
    @OnError(titulo = "Erro", descricao = "Falha ao ativar servidor",
            detalhe = "Verifique se o servidor está configurado corretamente")
    public void btnAtivar(ActionEvent event) {
        if (configAtual == null || configAtual.id() == null) {
            throw new IllegalStateException("Configuração inválida para ativação");
        }

        try {
            configService.definirComoAtiva(configAtual.id());

            Optional<ConfigServidorRemoto> recarregado = configService.buscarPorId(configAtual.id());
            if (recarregado.isPresent()) {
                configAtual = recarregado.get();
                configIdCarregado = configAtual.id();
                preencherDetalhes(configAtual);
            } else {
                throw new RuntimeException("Configuração não encontrada após ativação");
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao ativar servidor: " + e.getMessage(), e);
        }
    }

    @OnSuccess(titulo = "Sucesso", descricao = "Servidor desativado com sucesso!")
    @OnError(titulo = "Erro", descricao = "Falha ao desativar servidor",
            detalhe = "Verifique se o servidor existe e tente novamente")
    public void btnDesativar(ActionEvent event) {
        if (configAtual == null || configAtual.id() == null) {
            throw new IllegalStateException("Configuração inválida para desativação");
        }

        try {
            ConfigServidorRemoto desativado = new ConfigServidorRemoto(
                    configAtual.id(),
                    configAtual.nomeConfig(),
                    configAtual.tipoBanco(),
                    configAtual.host(),
                    configAtual.porta(),
                    configAtual.databaseName(),
                    configAtual.usuario(),
                    configAtual.senha(),
                    configAtual.parametrosExtra(),
                    0,
                    configAtual.ultimaConexao(),
                    configAtual.statusConexao(),
                    configAtual.createdAt(),
                    LocalDateTime.now()
            );

            configService.atualizar(desativado);

            Optional<ConfigServidorRemoto> recarregado = configService.buscarPorId(configAtual.id());
            if (recarregado.isPresent()) {
                configAtual = recarregado.get();
                configIdCarregado = configAtual.id();
                preencherDetalhes(configAtual);
            } else {
                throw new RuntimeException("Configuração não encontrada após desativação");
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao desativar servidor: " + e.getMessage(), e);
        }
    }


    @OnSuccess(descricao = "Abrindo edição")
    public void btnEditar(ActionEvent event) {
        // ✅ CORRETO: Envia o ID via Params para a rota
        if (configAtual != null && configAtual.id() != null) {
            Rotas.put("config_servidor/carregar",
                    Params.with("id", configAtual.id())
            );
        }
        // @NewScene fará a navegação
    }

    public void btnVoltar(ActionEvent event) {
        try {
            txtTitulo.getScene().getWindow().hide();
        } catch (Exception e) {
            System.err.println("Erro ao voltar: " + e.getMessage());
            txtTitulo.getScene().getWindow().hide();
        }
    }
}