package com.ossobo.gestaoDepIt.controllers.gestao.estoque.catalugoproduto.catalogoList.cellfactories;

import com.ossobo.gestaoDepIt.db.models.CatalogoProdutos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

public class StatusCellFactory implements Callback<TableColumn<CatalogoProdutos, String>, TableCell<CatalogoProdutos, String>> {

    @Override
    public TableCell<CatalogoProdutos, String> call(TableColumn<CatalogoProdutos, String> param) {
        return new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle(item.equals("Ativo") ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
                }
            }
        };
    }
}