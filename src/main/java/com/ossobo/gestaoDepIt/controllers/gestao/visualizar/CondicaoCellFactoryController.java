package com.ossobo.gestaoDepIt.controllers.gestao.visualizar;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;
import javafx.util.Callback;
import com.ossobo.gestaoDepIt.db.models.InventarioEquipamentos;

public class CondicaoCellFactoryController {

    @FXML private Circle indicadorCondicao;
    @FXML private Label labelCondicao;

    // Factory method para TableColumn
    public static Callback<TableColumn<InventarioEquipamentos, String>, TableCell<InventarioEquipamentos, String>> createFactory() {
        return column -> new TableCell<InventarioEquipamentos, String>() {
            private final HBox container;
            private final Circle circle;
            private final Label label;

            {
                container = new HBox(6);
                container.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                circle = new Circle(5);
                label = new Label();
                label.setStyle("-fx-font-size: 11; -fx-font-weight: bold;");

                container.getChildren().addAll(circle, label);
            }

            @Override
            protected void updateItem(String condicao, boolean empty) {
                super.updateItem(condicao, empty);

                if (empty || condicao == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    label.setText(condicao);

                    // Cores baseadas na condição
                    switch (condicao.toUpperCase()) {
                        case "OTIMO":
                            circle.setFill(javafx.scene.paint.Color.web("#27ae60")); // Verde
                            label.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 11; -fx-font-weight: bold;");
                            break;
                        case "BOM":
                            circle.setFill(javafx.scene.paint.Color.web("#3498db")); // Azul
                            label.setStyle("-fx-text-fill: #3498db; -fx-font-size: 11; -fx-font-weight: bold;");
                            break;
                        case "REGULAR":
                            circle.setFill(javafx.scene.paint.Color.web("#f39c12")); // Laranja
                            label.setStyle("-fx-text-fill: #f39c12; -fx-font-size: 11; -fx-font-weight: bold;");
                            break;
                        case "CRITICO":
                            circle.setFill(javafx.scene.paint.Color.web("#e74c3c")); // Vermelho
                            label.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 11; -fx-font-weight: bold;");
                            break;
                        default:
                            circle.setFill(javafx.scene.paint.Color.web("#95a5a6")); // Cinza
                    }

                    setGraphic(container);
                    setText(null);
                }
            }
        };
    }
}