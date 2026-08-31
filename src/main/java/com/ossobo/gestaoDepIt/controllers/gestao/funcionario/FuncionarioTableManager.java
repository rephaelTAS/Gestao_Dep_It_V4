/*
 * FuncionarioTableManager v1.0
 *
 * Gerencia a configuração visual e cell factories da tabela de funcionários.
 * Extraído do Controller — SRP: Controller coordena, TableManager configura.
 *
 * v1.0: Versão inicial
 *       - Cell factories para Foto (Circle), Status (Ativo/Inativo), Ações (botões)
 *       - Adapter lambda para evitar PropertyValueFactory
 */

package com.ossobo.gestaoDepIt.controllers.gestao.funcionario;

import com.ossobo.gestaoDepIt.db.models.Funcionarios;
import com.ossobo.nexusfx.NexusFX;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;

public class FuncionarioTableManager {

    private static final Logger logger = LoggerFactory.getLogger(FuncionarioTableManager.class);
    private final FuncionarioListController controller;

    public FuncionarioTableManager(FuncionarioListController controller) {
        this.controller = controller;
    }

    /**
     * Configura todas as colunas da tabela:
     * - cellValueFactory com lambda (evita PropertyValueFactory)
     * - cellFactory para colunas especiais (Foto, Status, Ações)
     */
    public void configurarTabela() {
        configurarColunaFoto();
        configurarColunaCodDep();
        configurarColunaNome();
        configurarColunaFuncao();
        configurarColunaDepartamento();
        configurarColunaStatus();
        configurarColunaEmail();
        configurarColunaAcoes();
    }

    // =========================================================================
    // COLUNA: FOTO (Circle com imagem)
    // =========================================================================

    private void configurarColunaFoto() {
        TableColumn<Funcionarios, String> col = controller.getColFoto();

        col.setCellValueFactory(cellData -> {
            Funcionarios f = cellData.getValue();
            // Retorna string vazia — o valor real é byte[], a renderização é visual
            return new SimpleStringProperty(f.temImagemPerfil() ? "sim" : "");
        });

        col.setCellFactory(param -> new TableCell<>() {
            private final Circle circle = new Circle(18);

            {
                circle.setStyle("-fx-stroke: #ddd; -fx-stroke-width: 1;");
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }

                Funcionarios func = (Funcionarios) getTableRow().getItem();
                if (func.temImagemPerfil()) {
                    try {
                        Image img = new Image(new ByteArrayInputStream(func.getImagemPerfil()));
                        circle.setFill(new ImagePattern(img));
                    } catch (Exception e) {
                        logger.debug("Erro ao carregar imagem de perfil para {}", func.getCodDep());
                        circle.setFill(null);
                    }
                } else {
                    circle.setFill(null);
                }
                setGraphic(circle);
            }
        });

        col.setPrefWidth(50);
        col.setSortable(false);
    }

    // =========================================================================
    // COLUNA: CÓDIGO
    // =========================================================================

    private void configurarColunaCodDep() {
        TableColumn<Funcionarios, String> col = controller.getColCodDep();
        col.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCodDep()));
        col.setPrefWidth(80);
    }

    // =========================================================================
    // COLUNA: NOME
    // =========================================================================

    private void configurarColunaNome() {
        TableColumn<Funcionarios, String> col = controller.getColNome();
        col.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getNome()));
        col.setPrefWidth(150);
    }

    // =========================================================================
    // COLUNA: FUNÇÃO
    // =========================================================================

    private void configurarColunaFuncao() {
        TableColumn<Funcionarios, String> col = controller.getColFuncao();
        col.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getFuncao()));
        col.setPrefWidth(120);
    }

    // =========================================================================
    // COLUNA: DEPARTAMENTO
    // =========================================================================

    private void configurarColunaDepartamento() {
        TableColumn<Funcionarios, String> col = controller.getColDepartamento();
        col.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDepartamento()));
        col.setPrefWidth(120);
    }

    // =========================================================================
    // COLUNA: STATUS (Ativo/Inativo com cor)
    // =========================================================================

    private void configurarColunaStatus() {
        TableColumn<Funcionarios, Boolean> col = controller.getColStatus();

        col.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleBooleanProperty(cellData.getValue().getAtivo()));

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

        col.setPrefWidth(70);
    }

    // =========================================================================
    // COLUNA: EMAIL
    // =========================================================================

    private void configurarColunaEmail() {
        TableColumn<Funcionarios, String> col = controller.getColEmail();
        col.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().getEmail() != null
                                ? cellData.getValue().getEmail()
                                : ""));
        col.setPrefWidth(180);
    }

    // =========================================================================
    // COLUNA: AÇÕES (Visualizar, Editar, Excluir)
    // =========================================================================

    private void configurarColunaAcoes() {
        TableColumn<Funcionarios, Void> col = controller.getColAcoes();

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
                    Funcionarios func = getTableView().getItems().get(getIndex());
                    controller.handleVisualizarFuncionario(func, null);
                });

                btnEdit.setOnAction(e -> {
                    Funcionarios func = getTableView().getItems().get(getIndex());
                    controller.handleEditarFuncionario(func, null);
                });

                btnDel.setOnAction(e -> {
                    Funcionarios func = getTableView().getItems().get(getIndex());
                    controller.handleExcluirFuncionario(func, null);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        col.setPrefWidth(110);
        col.setSortable(false);
    }
}