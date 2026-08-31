package com.ossobo.gestaoDepIt.controllers.gestao.estoque.catalugoproduto.catalogoList.cellfactories;

import com.ossobo.gestaoDepIt.controllers.gestao.estoque.catalugoproduto.catalogoList.CatalogActions;
import com.ossobo.gestaoDepIt.db.models.CatalogoProdutos;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

public class AcoesCellFactory implements Callback<TableColumn<CatalogoProdutos, Void>, TableCell<CatalogoProdutos, Void>> {

    private final CatalogActions actions;

    public AcoesCellFactory(CatalogActions actions) {
        this.actions = actions;
    }

    @Override
    public TableCell<CatalogoProdutos, Void> call(TableColumn<CatalogoProdutos, Void> param) {
        return new TableCell<>() {
            private final Button btnEditar = new Button("Editar");
            private final Button btnExcluir = new Button("Excluir");

            {
                // Dentro de AcoesCellFactory.java
                btnEditar.setOnAction(e -> {
                    CatalogoProdutos produto = getTableView().getItems().get(getIndex());
                    actions.editarProduto(produto, getScene().getWindow());
                });

                btnExcluir.setOnAction(e -> {
                    CatalogoProdutos produto = getTableView().getItems().get(getIndex());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(new javafx.scene.layout.HBox(5, btnEditar, btnExcluir));
                }
            }
        };
    }
}