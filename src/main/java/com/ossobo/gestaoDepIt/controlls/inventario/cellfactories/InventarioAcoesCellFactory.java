package com.ossobo.gestaoDepIt.controlls.inventario.cellfactories;

import com.ossobo.gestaoDepIt.db.models.InventarioEquipamentos;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

/**
 * InventarioAcoesCellFactory v1.0
 *
 * Coluna "Ações" da tabela de inventário: 3 botões — Detalhes, Editar, Histórico.
 *
 * A cell factory NÃO abre janelas nem chama Rotas — apenas invoca
 * {@link InventarioAcoesCallback}, injetado no construtor.
 *
 * Acesso ao item via getTableRow().getItem() (seguro em células recicladas).
 */
public class InventarioAcoesCellFactory
        implements Callback<TableColumn<InventarioEquipamentos, Void>,
        TableCell<InventarioEquipamentos, Void>> {

    private final InventarioAcoesCallback callback;

    public InventarioAcoesCellFactory(InventarioAcoesCallback callback) {
        this.callback = callback;
    }

    @Override
    public TableCell<InventarioEquipamentos, Void> call(TableColumn<InventarioEquipamentos, Void> param) {
        return new TableCell<>() {

            private final Button btnDetalhes  = new Button("Detalhes");
            private final Button btnEditar    = new Button("Editar");
            private final Button btnHistorico = new Button("📋");
            private final HBox   container    = new HBox(5, btnDetalhes, btnEditar, btnHistorico);

            {
                container.setAlignment(Pos.CENTER_LEFT);
                btnDetalhes.getStyleClass().add("action-btn-view");
                btnEditar.getStyleClass().add("action-btn-edit");
                btnHistorico.getStyleClass().add("action-btn-history");
                btnHistorico.setTooltip(new Tooltip("Histórico deste SKU e Funcionário"));

                btnDetalhes.setOnAction(e -> {
                    InventarioEquipamentos eq = itemAtual();
                    if (eq != null && callback != null) callback.abrirDetalhes(eq);
                });
                btnEditar.setOnAction(e -> {
                    InventarioEquipamentos eq = itemAtual();
                    if (eq != null && callback != null) callback.abrirEdicao(eq);
                });
                btnHistorico.setOnAction(e -> {
                    InventarioEquipamentos eq = itemAtual();
                    if (eq != null && callback != null) callback.abrirHistorico(eq);
                });
            }

            private InventarioEquipamentos itemAtual() {
                return getTableRow() != null ? getTableRow().getItem() : null;
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        };
    }
}