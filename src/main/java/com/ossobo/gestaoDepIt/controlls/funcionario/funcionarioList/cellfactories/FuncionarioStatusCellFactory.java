package com.ossobo.gestaoDepIt.controlls.funcionario.funcionarioList.cellfactories;

import com.ossobo.gestaoDepIt.db.models.Funcionarios;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

/**
 * FuncionarioStatusCellFactory v1.0
 *
 * Responsabilidade: colorir o texto da coluna Status (verde=Ativo,
 *                   vermelho=Inativo). Espelha StatusCellFactory.
 *
 * @since v1.0
 */
public class FuncionarioStatusCellFactory
        implements Callback<TableColumn<Funcionarios, String>, TableCell<Funcionarios, String>> {

    @Override
    public TableCell<Funcionarios, String> call(TableColumn<Funcionarios, String> param) {
        return new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle(item.equals("Ativo")
                            ? "-fx-text-fill: green;"
                            : "-fx-text-fill: red;");
                }
            }
        };
    }
}