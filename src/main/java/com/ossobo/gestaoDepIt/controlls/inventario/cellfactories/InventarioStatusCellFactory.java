package com.ossobo.gestaoDepIt.controlls.inventario.cellfactories;

import com.ossobo.gestaoDepIt.db.models.InventarioEquipamentos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

/**
 * InventarioStatusCellFactory v1.0
 *
 * Renderiza a coluna "Status" com cor por valor:
 *   ATIVO      → verde
 *   EM_USO     → azul
 *   RESERVA    → roxo
 *   MANUTENCAO → laranja
 *   BAIXADO    → cinza
 */
public class InventarioStatusCellFactory
        implements Callback<TableColumn<InventarioEquipamentos, String>,
        TableCell<InventarioEquipamentos, String>> {

    @Override
    public TableCell<InventarioEquipamentos, String> call(TableColumn<InventarioEquipamentos, String> param) {
        return new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(item);
                setStyle("-fx-text-fill: " + corDe(item) + "; -fx-font-weight: bold;");
            }
        };
    }

    private String corDe(String status) {
        return switch (status) {
            case "ATIVO"      -> "#10b981";
            case "EM_USO"     -> "#3b82f6";
            case "RESERVA"    -> "#8b5cf6";
            case "MANUTENCAO" -> "#f59e0b";
            case "BAIXADO"    -> "#6b7280";
            default           -> "#374151";
        };
    }
}