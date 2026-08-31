package com.ossobo.gestaoDepIt.controllers.main;

import com.ossobo.gestaoDepIt.db.models.ConfigServidorRemoto;
import com.ossobo.gestaoDepIt.db.services.ConfigServidorRemotoService;
import com.ossobo.winterfx.anotations.Controller;
import com.ossobo.winterfx.anotations.Inject;
import com.ossobo.winterfx.anotations.PostConstruct;
import com.ossobo.winterfx.view.anotations.RegisterView;
import com.ossobo.winterfx.view.controller.WinterFXController;
import com.ossobo.winterfx.notifications.anotations.OnError;
import com.ossobo.winterfx.notifications.anotations.OnSuccess;
import com.ossobo.winterfx.view.anotations.NewScene;
import com.ossobo.winterfx.view.anotations.SwapFxml;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * ServidorListController - Lista de configurações de servidor
 * v2.0 - Corrigido para usar Records e WinterFX corretamente
 *
 * Responsabilidades:
 * - Exibir todas as configurações em tabela
 * - Gerenciar ações: ver, editar, ativar, desativar, testar
 */
@Controller(proxy = false)
@RegisterView(
        id = "servidor_list",  // ✅ ID direto (sem ViewConstant)
        fxml = "/com/ossobo/gestaoDepIt/fxmls/config/servidor_list.fxml",
        title = "Servidores - Configuração",
        primaryCss = "/com/ossobo/gestaoDepIt/css/config/servidor_list.css"
)
public class ServidorListController implements WinterFXController {

    // ============================================================
    // @FXML INJECTIONS
    // ============================================================

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

    // ============================================================
    // @INJECT SERVICES
    // ============================================================

    @Inject private ConfigServidorRemotoService configService;

    // ============================================================
    // CONSTANTES
    // ============================================================

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // IDs das views para navegação
    private static final String VIEW_SERVIDOR_FORM = "config_servidor";
    private static final String VIEW_SERVIDOR_DETALHES = "servidor_detalhes";

    // ============================================================
    // INICIALIZAÇÃO
    // ============================================================

    @PostConstruct  // ✅ CORRIGIDO: usa @PostConstruct do WinterFX
    public void init() {
        configurarColunas();
        carregarDados();
        atualizarStatusConexao();
    }

    // ============================================================
    // CONFIGURAÇÃO DA TABELA
    // ============================================================

    private void configurarColunas() {
        // ✅ CORRIGIDO: usa lambda para acesso aos campos do Record
        colStatus.setCellValueFactory(cellData -> {
            ConfigServidorRemoto config = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(config.statusConexao());
        });
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    return;
                }

                setText(item);
                String color = switch (item) {
                    case "CONECTADO" -> "#4CAF50";
                    case "FALHA" -> "#F44336";
                    case "TESTANDO" -> "#FF9800";
                    default -> "#9E9E9E";
                };
                setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
            }
        });

        // ✅ CORRIGIDO: usando lambdas para todos os campos do Record
        colNome.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().nomeConfig())
        );
        colTipo.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().tipoBanco())
        );
        colHost.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().host())
        );
        colPorta.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().porta())
        );
        colDatabase.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().databaseName())
        );
        colUsuario.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().usuario())
        );
        colUltimaConexao.setCellValueFactory(cellData -> {
            String ultima = cellData.getValue().ultimaConexao();
            return new javafx.beans.property.SimpleStringProperty(
                    ultima != null ? ultima : "--"
            );
        });

        // Coluna de Ações com botões
        configurarColunaAcoes();
    }

    private void configurarColunaAcoes() {
        colAcoes.setCellFactory(col -> new TableCell<>() {
            private final Button btnVer = new Button("👁️");
            private final Button btnAtivar = new Button("✅");
            private final Button btnDesativar = new Button("❌");
            private final Button btnEditar = new Button("✏️");
            private final Button btnTestar = new Button("🔌");
            private final HBox container = new HBox(5, btnVer, btnAtivar, btnDesativar, btnEditar, btnTestar);

            {
                // Estilização dos botões
                String btnStyle = "-fx-font-size: 11px; -fx-min-width: 28px; -fx-min-height: 28px;";
                btnVer.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;" + btnStyle);
                btnAtivar.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;" + btnStyle);
                btnDesativar.setStyle("-fx-background-color: #F44336; -fx-text-fill: white;" + btnStyle);
                btnEditar.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white;" + btnStyle);
                btnTestar.setStyle("-fx-background-color: #9E9E9E; -fx-text-fill: white;" + btnStyle);

                btnVer.setTooltip(new Tooltip("Ver detalhes"));
                btnAtivar.setTooltip(new Tooltip("Ativar"));
                btnDesativar.setTooltip(new Tooltip("Desativar"));
                btnEditar.setTooltip(new Tooltip("Editar"));
                btnTestar.setTooltip(new Tooltip("Testar conexão"));

                btnVer.setOnAction(e -> {
                    ConfigServidorRemoto config = getTableView().getItems().get(getIndex());
                    abrirDetalhes(config);
                });

                btnAtivar.setOnAction(e -> {
                    ConfigServidorRemoto config = getTableView().getItems().get(getIndex());
                    ativarServidor(config);
                });

                btnDesativar.setOnAction(e -> {
                    ConfigServidorRemoto config = getTableView().getItems().get(getIndex());
                    desativarServidor(config);
                });

                btnEditar.setOnAction(e -> {
                    ConfigServidorRemoto config = getTableView().getItems().get(getIndex());
                    abrirEdicao(config);
                });

                btnTestar.setOnAction(e -> {
                    ConfigServidorRemoto config = getTableView().getItems().get(getIndex());
                    testarConexao(config);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(container);
                }
            }
        });
    }

    // ============================================================
    // CARREGAMENTO DE DADOS
    // ============================================================

    private void carregarDados() {
        try {
            List<ConfigServidorRemoto> servidores = configService.listarTodos();
            tblServidores.getItems().setAll(servidores);
            lblTotal.setText("Total: " + servidores.size());

            if (servidores.isEmpty()) {
                lblTotal.setText("Total: 0 - Nenhum servidor configurado");
            }

        } catch (Exception e) {
            System.err.println("Erro ao carregar servidores: " + e.getMessage());
            tblServidores.getItems().clear();
            lblTotal.setText("Total: 0 - Erro ao carregar");
        }
    }

    private void atualizarStatusConexao() {
        try {
            var ativa = configService.buscarAtiva();
            if (ativa.isPresent()) {
                ConfigServidorRemoto config = ativa.get();
                String status = config.statusConexao();
                lblStatusConexao.setText("🔌 Servidor ativo: " + config.nomeConfig() +
                        " | Status: " + status);
                String color = switch (status) {
                    case "CONECTADO" -> "#4CAF50";
                    case "FALHA" -> "#F44336";
                    case "TESTANDO" -> "#FF9800";
                    default -> "#9E9E9E";
                };
                lblStatusConexao.setStyle("-fx-text-fill: " + color + ";");
            } else {
                lblStatusConexao.setText("🔌 Nenhum servidor ativo configurado");
                lblStatusConexao.setStyle("-fx-text-fill: #9E9E9E;");
            }
        } catch (Exception e) {
            lblStatusConexao.setText("🔌 Erro ao verificar status da conexão");
        }
    }

    // ============================================================
    // AÇÕES DA TABELA
    // ============================================================

    @NewScene(view = VIEW_SERVIDOR_DETALHES, title = "Detalhes do Servidor", centered = true)
    private void abrirDetalhes(ConfigServidorRemoto config) {
        // @NewScene navega automaticamente
        // Os dados são passados via Params na rota
        System.out.println("Abrir detalhes: " + config.nomeConfig());
    }

    @NewScene(view = VIEW_SERVIDOR_FORM, title = "Editar Servidor", centered = true)
    private void abrirEdicao(ConfigServidorRemoto config) {
        // @NewScene navega automaticamente
        // Os dados são passados via Params na rota
        System.out.println("Editar: " + config.nomeConfig());
    }

    @OnSuccess(titulo = "Sucesso", descricao = "Servidor ativado com sucesso!")
    @OnError(titulo = "Erro", descricao = "Falha ao ativar servidor", detalhe = "Verifique se o servidor está configurado corretamente")
    private void ativarServidor(ConfigServidorRemoto config) {
        try {
            // ✅ CORRIGIDO: usa accessor do Record
            configService.definirComoAtiva(config.id());
            carregarDados();
            atualizarStatusConexao();
            mostrarNotificacao("✅ Servidor ativado: " + config.nomeConfig());
        } catch (Exception e) {
            throw new RuntimeException("Erro ao ativar servidor: " + e.getMessage(), e);
        }
    }

    @OnSuccess(titulo = "Sucesso", descricao = "Servidor desativado com sucesso!")
    @OnError(titulo = "Erro", descricao = "Falha ao desativar servidor", detalhe = "Verifique se o servidor existe")
    private void desativarServidor(ConfigServidorRemoto config) {
        try {
            // ✅ CORRIGIDO: cria nova instância com ativo = 0
            ConfigServidorRemoto desativado = new ConfigServidorRemoto(
                    config.id(),
                    config.nomeConfig(),
                    config.tipoBanco(),
                    config.host(),
                    config.porta(),
                    config.databaseName(),
                    config.usuario(),
                    config.senha(),
                    config.parametrosExtra(),
                    0,  // ativo = 0 (desativado)
                    config.ultimaConexao(),
                    config.statusConexao(),
                    config.createdAt(),
                    LocalDateTime.now()
            );
            configService.atualizar(desativado);
            carregarDados();
            atualizarStatusConexao();
            mostrarNotificacao("❌ Servidor desativado: " + config.nomeConfig());
        } catch (Exception e) {
            throw new RuntimeException("Erro ao desativar servidor: " + e.getMessage(), e);
        }
    }

    @OnSuccess(titulo = "Sucesso", descricao = "Conexão testada com sucesso!")
    @OnError(titulo = "Erro", descricao = "Falha ao testar conexão", detalhe = "Verifique as credenciais e a rede")
    private void testarConexao(ConfigServidorRemoto config) {
        try {
            Platform.runLater(() -> {
                mostrarNotificacao("🔄 Testando conexão com " + config.nomeConfig() + "...");
            });

            boolean conectado = configService.testarConexao(config);

            Platform.runLater(() -> {
                if (conectado) {
                    mostrarNotificacao("✅ Conexão com " + config.nomeConfig() + " estabelecida!");
                } else {
                    mostrarNotificacao("❌ Falha na conexão com " + config.nomeConfig());
                }
                carregarDados();
                atualizarStatusConexao();
            });

            if (!conectado) {
                throw new RuntimeException("Falha na conexão com " + config.nomeConfig());
            }

        } catch (Exception e) {
            throw new RuntimeException("Erro ao testar conexão: " + e.getMessage(), e);
        }
    }

    private void mostrarNotificacao(String mensagem) {
        System.out.println(mensagem);
        // TODO: Integrar com NotificationManager do WinterFX
        // NotificationManager.show(mensagem);
    }

    // ============================================================
    // MÉTODOS DE BOTÃO (ActionEvent)
    // ============================================================

    @NewScene(view = VIEW_SERVIDOR_FORM, title = "Novo Servidor", centered = true)
    @OnSuccess(descricao = "Abrindo formulário")
    public void btnNovo(ActionEvent event) {
        // @NewScene faz a navegação automaticamente
    }

    public void btnRefresh(ActionEvent event) {
        carregarDados();
        atualizarStatusConexao();
    }

    @OnSuccess(descricao = "Teste concluído para todos os servidores")
    @OnError(titulo = "Erro", descricao = "Falha ao testar servidores", detalhe = "Verifique a lista de servidores")
    public void btnTestarTodos(ActionEvent event) {
        try {
            List<ConfigServidorRemoto> servidores = configService.listarTodos();
            if (servidores.isEmpty()) {
                mostrarNotificacao("⚠️ Nenhum servidor para testar");
                return;
            }

            // ✅ CORRIGIDO: processa em background para não travar a UI
            new Thread(() -> {
                for (ConfigServidorRemoto config : servidores) {
                    try {
                        configService.testarConexao(config);
                        Platform.runLater(() ->
                                mostrarNotificacao("✅ " + config.nomeConfig() + " testado")
                        );
                    } catch (Exception e) {
                        Platform.runLater(() ->
                                mostrarNotificacao("❌ " + config.nomeConfig() + ": " + e.getMessage())
                        );
                    }
                }

                Platform.runLater(() -> {
                    carregarDados();
                    atualizarStatusConexao();
                    mostrarNotificacao("✅ Teste concluído para " + servidores.size() + " servidores");
                });
            }).start();

        } catch (Exception e) {
            throw new RuntimeException("Erro ao testar todos: " + e.getMessage(), e);
        }
    }
}