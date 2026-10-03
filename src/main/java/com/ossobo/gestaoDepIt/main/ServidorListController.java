package com.ossobo.gestaoDepIt.main;

import com.ossobo.gestaoDepIt.config.ViewConstant;
import com.ossobo.gestaoDepIt.db.models.ConfigServidorRemoto;
import com.ossobo.gestaoDepIt.db.services.ConfigServidorRemotoService;
import com.ossobo.winterfx.anotations.*;
import com.ossobo.winterfx.notifications.anotations.OnError;
import com.ossobo.winterfx.notifications.anotations.OnSuccess;
import com.ossobo.winterfx.router.Rotas;
import com.ossobo.winterfx.router.model.ResponseData;
import com.ossobo.winterfx.view.anotations.NewScene;
import com.ossobo.winterfx.view.anotations.RegisterView;
import com.ossobo.winterfx.view.controller.WinterFXController;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
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
@RequestMapping("servidor/list/controll")
public class ServidorListController implements WinterFXController {

    private static final Logger LOGGER = System.getLogger(ServidorListController.class.getName());

     // ============================================================
    // CONSTANTES
    // ============================================================

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");




    // ============================================================
    // CONFIGURAÇÃO DA TABELA
    // ============================================================

    @ExecMapping("configurar/colunas")
    private ResponseData configurarColunas(@UI("tblServidores") TableView<ConfigServidorRemoto> tblServidores,
                                           @UI("colStatus") TableColumn<ConfigServidorRemoto, String> colStatus,
                                           @UI("colNome") TableColumn<ConfigServidorRemoto, String> colNome,
                                           @UI("colTipo") TableColumn<ConfigServidorRemoto, String> colTipo,
                                           @UI("colHost") TableColumn<ConfigServidorRemoto, String> colHost,
                                           @UI("colPorta") TableColumn<ConfigServidorRemoto, String> colPorta,
                                           @UI("colDatabase") TableColumn<ConfigServidorRemoto, String> colDatabase,
                                           @UI("colUsuario") TableColumn<ConfigServidorRemoto, String> colUsuario,
                                           @UI("colUltimaConexao")  TableColumn<ConfigServidorRemoto, String> colUltimaConexao,
                                           @UI("colAcoes") TableColumn<ConfigServidorRemoto, Void> colAcoes,
                                           @UI("lblTotal") Label lblTotal,
                                           @UI("lblStatusConexao") Label lblStatusConexao
                                           ) {
        // Verifica cada coluna antes de configurar
        if (colStatus != null) {
            colStatus.setCellValueFactory(cellData -> {
                ConfigServidorRemoto config = cellData.getValue();
                return new SimpleStringProperty(config != null ? config.statusConexao() : "");
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
                        case "CONECTADO" -> "#22C55E";
                        case "FALHA" -> "#EF4444";
                        case "TESTANDO" -> "#F59E0B";
                        default -> "#94A3B8";
                    };
                    setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
                }
            });
        }

        if (colNome != null) {
            colNome.setCellValueFactory(cellData -> {
                ConfigServidorRemoto config = cellData.getValue();
                return new SimpleStringProperty(config != null ? config.nomeConfig() : "");
            });
        }

        if (colTipo != null) {
            colTipo.setCellValueFactory(cellData -> {
                ConfigServidorRemoto config = cellData.getValue();
                return new SimpleStringProperty(config != null ? config.tipoBanco() : "");
            });
        }

        if (colHost != null) {
            colHost.setCellValueFactory(cellData -> {
                ConfigServidorRemoto config = cellData.getValue();
                return new SimpleStringProperty(config != null ? config.host() : "");
            });
        }

        if (colPorta != null) {
            colPorta.setCellValueFactory(cellData -> {
                ConfigServidorRemoto config = cellData.getValue();
                return new SimpleStringProperty(config != null ? config.porta() : "");
            });
        }

        if (colDatabase != null) {
            colDatabase.setCellValueFactory(cellData -> {
                ConfigServidorRemoto config = cellData.getValue();
                return new SimpleStringProperty(config != null ? config.databaseName() : "");
            });
        }

        if (colUsuario != null) {
            colUsuario.setCellValueFactory(cellData -> {
                ConfigServidorRemoto config = cellData.getValue();
                return new SimpleStringProperty(config != null ? config.usuario() : "");
            });
        }

        if (colUltimaConexao != null) {
            colUltimaConexao.setCellValueFactory(cellData -> {
                ConfigServidorRemoto config = cellData.getValue();
                String ultima = config != null ? config.ultimaConexao() : null;
                return new SimpleStringProperty(ultima != null ? ultima : "--");
            });
        }

        if (colAcoes != null) {
           // configurarColunaAcoes(colAcoes);
        }


        return ResponseData.success();
    }
/*
    private void configurarColunaAcoes(TableColumn<ConfigServidorRemoto, Void> colAcoes) {
        if (colAcoes == null) return;

        colAcoes.setCellFactory(col -> new TableCell<>() {
            private final Button btnVer = new Button("👁️");
            private final Button btnAtivar = new Button("✅");
            private final Button btnDesativar = new Button("❌");
            private final Button btnEditar = new Button("✏️");
            private final Button btnTestar = new Button("🔌");
            private final HBox container = new HBox(5, btnVer, btnAtivar, btnDesativar, btnEditar, btnTestar);

            {
                String btnStyle = "-fx-font-size: 11px; -fx-min-width: 28px; -fx-min-height: 28px; -fx-cursor: hand;";
                btnVer.setStyle("-fx-background-color: #4F46E5; -fx-text-fill: white;" + btnStyle);
                btnAtivar.setStyle("-fx-background-color: #22C55E; -fx-text-fill: white;" + btnStyle);
                btnDesativar.setStyle("-fx-background-color: #EF4444; -fx-text-fill: white;" + btnStyle);
                btnEditar.setStyle("-fx-background-color: #F59E0B; -fx-text-fill: white;" + btnStyle);
                btnTestar.setStyle("-fx-background-color: #8B5CF6; -fx-text-fill: white;" + btnStyle);

                btnVer.setTooltip(new Tooltip("Ver detalhes"));
                btnAtivar.setTooltip(new Tooltip("Ativar"));
                btnDesativar.setTooltip(new Tooltip("Desativar"));
                btnEditar.setTooltip(new Tooltip("Editar"));
                btnTestar.setTooltip(new Tooltip("Testar conexão"));

                btnVer.setOnAction(e -> {
                    ConfigServidorRemoto config = getTableView().getItems().get(getIndex());
                    if (config != null) abrirDetalhes(config);
                });

                btnAtivar.setOnAction(e -> {
                    ConfigServidorRemoto config = getTableView().getItems().get(getIndex());
                    if (config != null) ativarServidor(config);
                });

                btnDesativar.setOnAction(e -> {
                    ConfigServidorRemoto config = getTableView().getItems().get(getIndex());
                    if (config != null) desativarServidor(config);
                });

                btnEditar.setOnAction(e -> {
                    ConfigServidorRemoto config = getTableView().getItems().get(getIndex());
                    if (config != null) abrirEdicao(config);
                });

                btnTestar.setOnAction(e -> {
                    ConfigServidorRemoto config = getTableView().getItems().get(getIndex());
                    if (config != null) testarConexao(config);
                });
            }


            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });
    }

    // ============================================================
    // CARREGAMENTO DE DADOS
    // ============================================================

    private void carregarDados(TableView<ConfigServidorRemoto> tblServidores, Label lblTotal) {
        try {
            if (tblServidores == null) {
                LOGGER.log(Level.WARNING, "⚠️ tblServidores é null, não é possível carregar dados");
                return;
            }
            var resp = Rotas.get("config-servidor-remoto/service/todos");


            List<ConfigServidorRemoto> servidores = resp.getDataList("configs");
            tblServidores.getItems().setAll(servidores);

            if (lblTotal != null) {
                lblTotal.setText(servidores.isEmpty() ? "Total: 0 - Nenhum servidor configurado" : "Total: " + servidores.size());
            }

        } catch (Exception e) {
            LOGGER.log(Level.ERROR, "❌ Erro ao carregar servidores", e);
            if (tblServidores != null) tblServidores.getItems().clear();
            if (lblTotal != null) lblTotal.setText("Total: 0 - Erro ao carregar");
        }
    }*/
/*
    private void atualizarStatusConexao() {
        try {
            if (lblStatusConexao == null) return;

            var ativa = configService.buscarAtiva();
            if (ativa.isPresent()) {
                ConfigServidorRemoto config = ativa.get();
                String status = config.statusConexao();
                lblStatusConexao.setText("🔌 Servidor ativo: " + config.nomeConfig() + " | Status: " + status);
                String color = switch (status) {
                    case "CONECTADO" -> "#22C55E";
                    case "FALHA" -> "#EF4444";
                    case "TESTANDO" -> "#F59E0B";
                    default -> "#94A3B8";
                };
                lblStatusConexao.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
            } else {
                lblStatusConexao.setText("🔌 Nenhum servidor ativo configurado");
                lblStatusConexao.setStyle("-fx-text-fill: #94A3B8;");
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, "❌ Erro ao verificar status da conexão", e);
            if (lblStatusConexao != null) {
                lblStatusConexao.setText("🔌 Erro ao verificar status da conexão");
            }
        }
    }

    // ============================================================
    // AÇÕES DA TABELA
    // ============================================================

    @NewScene(view = ViewConstant.Server.SERVIDOR_DETAIL, title = "Detalhes do Servidor", centered = true)
    private void abrirDetalhes(ConfigServidorRemoto config) {
        LOGGER.log(Level.INFO, "📋 Abrindo detalhes: {0}", config.nomeConfig());
        // @NewScene navega automaticamente
    }

    @NewScene(view = ViewConstant.Server.SERVIDOR_FORM, title = "Editar Servidor", centered = true)
    private void abrirEdicao(ConfigServidorRemoto config) {
        LOGGER.log(Level.INFO, "✏️ Editando: {0}", config.nomeConfig());
        // @NewScene navega automaticamente
    }

    @OnSuccess(titulo = "Sucesso", descricao = "Servidor ativado com sucesso!")
    @OnError(titulo = "Erro", descricao = "Falha ao ativar servidor", detalhe = "Verifique se o servidor está configurado corretamente")
    private void ativarServidor(ConfigServidorRemoto config) {
        try {
            configService.definirComoAtiva(config.id());
            carregarDados();
            atualizarStatusConexao();
            LOGGER.log(Level.INFO, "✅ Servidor ativado: {0}", config.nomeConfig());
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, "❌ Erro ao ativar servidor", e);
            throw new RuntimeException("Erro ao ativar servidor: " + e.getMessage(), e);
        }
    }

    @OnSuccess(titulo = "Sucesso", descricao = "Servidor desativado com sucesso!")
    @OnError(titulo = "Erro", descricao = "Falha ao desativar servidor", detalhe = "Verifique se o servidor existe")
    private void desativarServidor(ConfigServidorRemoto config) {
        try {
            // Cria nova instância com ativo = 0
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
                    0,
                    config.ultimaConexao(),
                    config.statusConexao(),
                    config.createdAt(),
                    LocalDateTime.now()
            );
            configService.atualizar(desativado);
            carregarDados();
            atualizarStatusConexao();
            LOGGER.log(Level.INFO, "❌ Servidor desativado: {0}", config.nomeConfig());
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, "❌ Erro ao desativar servidor", e);
            throw new RuntimeException("Erro ao desativar servidor: " + e.getMessage(), e);
        }
    }

    @OnSuccess(titulo = "Sucesso", descricao = "Conexão testada com sucesso!")
    @OnError(titulo = "Erro", descricao = "Falha ao testar conexão", detalhe = "Verifique as credenciais e a rede")
    private void testarConexao(ConfigServidorRemoto config) {
        try {
            LOGGER.log(Level.INFO, "🔄 Testando conexão com: {0}", config.nomeConfig());

            boolean conectado = configService.testarConexao(config);

            if (conectado) {
                LOGGER.log(Level.INFO, "✅ Conexão estabelecida com: {0}", config.nomeConfig());
            } else {
                LOGGER.log(Level.WARNING, "❌ Falha na conexão com: {0}", config.nomeConfig());
                throw new RuntimeException("Falha na conexão com " + config.nomeConfig());
            }

            carregarDados();
            atualizarStatusConexao();

        } catch (Exception e) {
            LOGGER.log(Level.ERROR, "❌ Erro ao testar conexão", e);
            throw new RuntimeException("Erro ao testar conexão: " + e.getMessage(), e);
        }
    }

    // ============================================================
    // MÉTODOS DE BOTÃO (ActionEvent)
    // ============================================================

    @NewScene(view = VIEW_SERVIDOR_FORM, title = "Novo Servidor", centered = true)
    @OnSuccess(descricao = "Abrindo formulário")
    public void btnNovo(ActionEvent event) {
        LOGGER.log(Level.INFO, "➕ Criando novo servidor");
        // @NewScene faz a navegação automaticamente
    }

    public void btnRefresh(ActionEvent event) {
        LOGGER.log(Level.INFO, "🔄 Atualizando lista");
        carregarDados();
        atualizarStatusConexao();
    }

    @OnSuccess(descricao = "Teste concluído para todos os servidores")
    @OnError(titulo = "Erro", descricao = "Falha ao testar servidores", detalhe = "Verifique a lista de servidores")
    public void btnTestarTodos(ActionEvent event) {
        try {
            List<ConfigServidorRemoto> servidores = configService.listarTodos();
            if (servidores.isEmpty()) {
                LOGGER.log(Level.WARNING, "⚠️ Nenhum servidor para testar");
                return;
            }

            LOGGER.log(Level.INFO, "🔄 Testando {0} servidores...", servidores.size());

            // Processa em background
            new Thread(() -> {
                for (ConfigServidorRemoto config : servidores) {
                    try {
                        configService.testarConexao(config);
                        LOGGER.log(Level.INFO, "✅ {0} testado com sucesso", config.nomeConfig());
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING, "❌ {0}: {1}", config.nomeConfig(), e.getMessage());
                    }
                }

                Platform.runLater(() -> {
                    carregarDados();
                    atualizarStatusConexao();
                    LOGGER.log(Level.INFO, "✅ Teste concluído para {0} servidores", servidores.size());
                });
            }).start();

        } catch (Exception e) {
            LOGGER.log(Level.ERROR, "❌ Erro ao testar todos os servidores", e);
            throw new RuntimeException("Erro ao testar todos: " + e.getMessage(), e);
        }
    }*/
}