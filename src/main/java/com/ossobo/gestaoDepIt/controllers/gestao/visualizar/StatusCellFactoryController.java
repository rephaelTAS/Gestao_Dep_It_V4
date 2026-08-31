package com.ossobo.gestaoDepIt.controllers.gestao.visualizar;

import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;
import javafx.util.Callback;
import com.ossobo.gestaoDepIt.db.models.InventarioEquipamentos;

public class StatusCellFactoryController {

    public static Callback<TableColumn<InventarioEquipamentos, String>, TableCell<InventarioEquipamentos, String>> createFactory() {
        return column -> new TableCell<InventarioEquipamentos, String>() {
            private final HBox container = new HBox(6);
            private final Circle circle = new Circle(5);
            private final Label label = new Label();

            {
                container.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                label.setStyle("-fx-font-size: 11; -fx-font-weight: bold;");
                container.getChildren().addAll(circle, label);
            }

            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);

                if (empty || status == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    label.setText(status);

                    switch (status.toUpperCase()) {
                        case "ATIVO":
                            circle.setFill(javafx.scene.paint.Color.web("#27ae60")); break;
                        case "MANUTENCAO":
                            circle.setFill(javafx.scene.paint.Color.web("#e67e22")); break;
                        case "BAIXADO":
                            circle.setFill(javafx.scene.paint.Color.web("#e74c3c")); break;
                        case "RESERVA":
                            circle.setFill(javafx.scene.paint.Color.web("#3498db")); break;
                        case "EM_USO":
                            circle.setFill(javafx.scene.paint.Color.web("#9b59b6")); break;
                        default:
                            circle.setFill(javafx.scene.paint.Color.web("#95a5a6"));
                    }

                    setGraphic(container);
                    setText(null);
                }
            }
        };
    }
}