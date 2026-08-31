package com.ossobo.gestaoDepIt.controllers.gestao.visualizar;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import com.ossobo.gestaoDepIt.db.models.InventarioEquipamentos;
import com.ossobo.nexusfx.di.annotations.Inject;

public class AcoesInventarioCellFactoryController {

    @FXML private Button btnVisualizar;
    @FXML private Button btnEditar;
    @FXML private Button btnHistorico;
    @FXML private Button btnBaixar;

    @Inject
    private InventarioEquipamentos equipamento;

    // Factory method para TableColumn
    public static Callback<TableColumn<InventarioEquipamentos, Void>, TableCell<InventarioEquipamentos, Void>> createFactory() {
        return column -> new TableCell<InventarioEquipamentos, Void>() {
            private final HBox container;
            private final Button btnVisualizar;
            private final Button btnEditar;
            private final Button btnHistorico;
            private final Button btnBaixar;

            {
                container = new HBox(5);
                container.setAlignment(javafx.geometry.Pos.CENTER);

                btnVisualizar = new Button("👁️");
                btnEditar = new Button("✏️");
                btnHistorico = new Button("📋");
                btnBaixar = new Button("📤");

                // Estilização dos botões
                String estiloBase = "-fx-font-size: 10; -fx-padding: 3 6; -fx-min-width: 30; -fx-text-fill: white;";

                btnVisualizar.setStyle(estiloBase + "-fx-background-color: #3498db;");
                btnEditar.setStyle(estiloBase + "-fx-background-color: #f39c12;");
                btnHistorico.setStyle(estiloBase + "-fx-background-color: #9b59b6;");
                btnBaixar.setStyle(estiloBase + "-fx-background-color: #e74c3c;");

                // Tooltips
                btnVisualizar.setTooltip(new javafx.scene.control.Tooltip("Visualizar Detalhes"));
                btnEditar.setTooltip(new javafx.scene.control.Tooltip("Editar Equipamento"));
                btnHistorico.setTooltip(new javafx.scene.control.Tooltip("Ver Histórico"));
                btnBaixar.setTooltip(new javafx.scene.control.Tooltip("Dar Baixa no Equipamento"));

                // Ações
                btnVisualizar.setOnAction(e -> visualizarEquipamento());
                btnEditar.setOnAction(e -> editarEquipamento());
                btnHistorico.setOnAction(e -> verHistorico());
                btnBaixar.setOnAction(e -> baixarEquipamento());

                container.getChildren().addAll(btnVisualizar, btnEditar, btnHistorico, btnBaixar);
            }

            private void visualizarEquipamento() {
                InventarioEquipamentos equipamento = getTableView().getItems().get(getIndex());
                System.out.println("Visualizar: " + equipamento.getNumSerie());
                // Abrir dialog de visualização
            }

            private void editarEquipamento() {
                InventarioEquipamentos equipamento = getTableView().getItems().get(getIndex());
                System.out.println("Editar: " + equipamento.getNumSerie());
                // Abrir dialog de edição
            }

            private void verHistorico() {
                InventarioEquipamentos equipamento = getTableView().getItems().get(getIndex());
                System.out.println("Histórico: " + equipamento.getNumSerie());
                // Abrir dialog de histórico
            }

            private void baixarEquipamento() {
                InventarioEquipamentos equipamento = getTableView().getItems().get(getIndex());
                System.out.println("Baixar: " + equipamento.getNumSerie());
                // Confirmar e executar baixa
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    InventarioEquipamentos equipamento = getTableView().getItems().get(getIndex());

                    // Mostrar/ocultar botão baixar baseado no status
                    boolean podeBaixar = !"BAIXADO".equals(equipamento.getStatus());
                    btnBaixar.setVisible(podeBaixar);
                    btnBaixar.setManaged(podeBaixar);

                    setGraphic(container);
                }
            }
        };
    }
}