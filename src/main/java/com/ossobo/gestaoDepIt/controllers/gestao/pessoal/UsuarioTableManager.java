/*
 * UsuarioTableManager v1.0
 *
 * Gerencia a configuração visual e cell factories da tabela de usuários.
 * Extraído do Controller — SRP.
 *
 * v1.0: Versão inicial
 *       - Cell factories para Nível Acesso, Status, Sessão, Ações
 *       - cellValueFactory com lambda (sem PropertyValueFactory)
 */
package com.ossobo.gestaoDepIt.controllers.gestao.pessoal;

import com.ossobo.gestaoDepIt.db.models.Usuario;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;

public class UsuarioTableManager {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioTableManager.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final UsuarioListController controller;

    public UsuarioTableManager(UsuarioListController controller) {
        this.controller = controller;
    }

    public void configurarTabela() {
        configurarColunaNome();
        configurarColunaEmail();
        configurarColunaFuncionario();
        configurarColunaNivelAcesso();
        configurarColunaStatus();
        configurarColunaUltimoLogin();
        configurarColunaIpLogin();
        configurarColunaSessaoAtiva();
        configurarColunaAcoes();
    }

    private void configurarColunaNome() {
        TableColumn<Usuario, String> col = controller.getColNome();
        col.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNome()));
        col.setPrefWidth(180);
    }

    private void configurarColunaEmail() {
        TableColumn<Usuario, String> col = controller.getColEmail();
        col.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getEmail() != null ? cellData.getValue().getEmail() : ""));
        col.setPrefWidth(200);
    }

    private void configurarColunaFuncionario() {
        TableColumn<Usuario, String> col = controller.getColFuncionario();
        col.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getFuncionarioId() != null ? cellData.getValue().getFuncionarioId() : "-"));
        col.setPrefWidth(120);
    }

    private void configurarColunaNivelAcesso() {
        TableColumn<Usuario, String> col = controller.getColNivelAcesso();
        col.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getNivelAcesso() != null ? cellData.getValue().getNivelAcesso() : "-"));

        col.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(String nivel, boolean empty) {
                super.updateItem(nivel, empty);
                if (empty || nivel == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(nivel);
                    switch (nivel) {
                        case "ADMIN" -> setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                        case "GESTOR" -> setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold;");
                        case "SUPERVISOR" -> setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;");
                        case "OPERADOR" -> setStyle("-fx-text-fill: #2ecc71;");
                        case "READONLY" -> setStyle("-fx-text-fill: #95a5a6;");
                        default -> setStyle("");
                    }
                }
            }
        });

        col.setPrefWidth(100);
    }

    private void configurarColunaStatus() {
        TableColumn<Usuario, Boolean> col = controller.getColStatus();
        col.setCellValueFactory(cellData -> new SimpleBooleanProperty(
                cellData.getValue().getAtivo() != null && cellData.getValue().getAtivo()));

        col.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean ativo, boolean empty) {
                super.updateItem(ativo, empty);
                if (empty || ativo == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(ativo ? "Ativo" : "Inativo");
                    setStyle(ativo
                            ? "-fx-text-fill: #27ae60; -fx-font-weight: bold;"
                            : "-fx-text-fill: #e74c3c;");
                }
            }
        });

        col.setPrefWidth(80);
    }

    private void configurarColunaUltimoLogin() {
        TableColumn<Usuario, String> col = controller.getColUltimoLogin();
        col.setCellValueFactory(cellData -> {
            Usuario u = cellData.getValue();
            if (u.getUltimoLogin() != null) {
                return new SimpleStringProperty(u.getUltimoLogin().format(DATE_FORMATTER));
            }
            return new SimpleStringProperty("Nunca");
        });
        col.setPrefWidth(140);
    }

    private void configurarColunaIpLogin() {
        TableColumn<Usuario, String> col = controller.getColIpLogin();
        col.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getIpUltimoLogin() != null ? cellData.getValue().getIpUltimoLogin() : "-"));
        col.setPrefWidth(120);
    }

    private void configurarColunaSessaoAtiva() {
        TableColumn<Usuario, String> col = controller.getColSessaoAtiva();
        col.setCellValueFactory(cellData -> {
            Usuario u = cellData.getValue();
            boolean temSessao = u.getSessaoAtual() != null && u.getSessaoAtual().length() > 0;
            return new SimpleStringProperty(temSessao ? "Online" : "Offline");
        });

        col.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    setStyle("Online".equals(status)
                            ? "-fx-text-fill: #27ae60; -fx-font-weight: bold;"
                            : "-fx-text-fill: #95a5a6;");
                }
            }
        });

        col.setPrefWidth(80);
    }

    private void configurarColunaAcoes() {
        TableColumn<Usuario, Void> col = controller.getColAcoes();

        col.setCellFactory(param -> new TableCell<>() {
            private final Button btnView = new Button("👁");
            private final Button btnEdit = new Button("✎");
            private final Button btnDel = new Button("🗑");
            private final HBox pane = new HBox(6, btnView, btnEdit, btnDel);

            {
                btnView.getStyleClass().add("btn-action-view");
                btnEdit.getStyleClass().add("btn-action-edit");
                btnDel.getStyleClass().add("btn-action-delete");

                btnView.setTooltip(new Tooltip("Ver detalhes"));
                btnEdit.setTooltip(new Tooltip("Editar"));
                btnDel.setTooltip(new Tooltip("Excluir"));

                btnView.setOnAction(e -> {
                    Usuario usuario = getTableView().getItems().get(getIndex());
                    controller.handleVisualizarUsuario(usuario, null);
                });

                btnEdit.setOnAction(e -> {
                    Usuario usuario = getTableView().getItems().get(getIndex());
                    controller.handleEditarUsuario(usuario, null);
                });

                btnDel.setOnAction(e -> {
                    Usuario usuario = getTableView().getItems().get(getIndex());
                    controller.handleExcluirUsuario(usuario, null);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        col.setPrefWidth(150);
        col.setSortable(false);
    }
}