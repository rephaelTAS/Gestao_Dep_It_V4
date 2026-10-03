package com.ossobo.gestaoDepIt.controlls.product.catalogoList.cellfactories;

import com.ossobo.gestaoDepIt.db.models.CatalogoProdutos;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

/**
 * AcoesCellFactory v2.0
 *
 * Responsabilidade: renderizar a coluna "Ações" da tabela de catálogo
 *                   com dois botões — Detalhes e Editar.
 *
 * v2.0 — Reescrita:
 *        - Botões: "Detalhes" e "Editar" (era "Editar" + "Excluir").
 *        - Exclusão removida desta camada: por segurança só é permitida
 *          após abertura dos detalhes.
 *        - Cell factory não abre janelas nem chama Rotas — apenas invoca
 *          {@link AcoesCallback}, injetado no construtor.
 *        - Acesso ao item via getTableRow().getItem() (mais seguro que
 *          getTableView().getItems().get(getIndex()) em células recicladas).
 *
 * @since v1.0
 */
public class AcoesCellFactory implements Callback<TableColumn<CatalogoProdutos, Void>, TableCell<CatalogoProdutos, Void>> {

    private final AcoesCallback callback;

    public AcoesCellFactory(AcoesCallback callback) {
        this.callback = callback;
    }

    @Override
    public TableCell<CatalogoProdutos, Void> call(TableColumn<CatalogoProdutos, Void> param) {
        return new TableCell<>() {

            private final Button btnDetalhes = new Button("Detalhes");
            private final Button btnEditar   = new Button("Editar");
            private final HBox container     = new HBox(6, btnDetalhes, btnEditar);

            {
                container.setAlignment(Pos.CENTER_LEFT);
                btnDetalhes.getStyleClass().add("btn-secondary");
                btnEditar.getStyleClass().add("btn-primary");

                btnDetalhes.setOnAction(e -> {
                    CatalogoProdutos produto = getTableRow() != null ? getTableRow().getItem() : null;
                    if (produto != null && callback != null) {
                        callback.abrirDetalhes(produto);
                    }
                });

                btnEditar.setOnAction(e -> {
                    CatalogoProdutos produto = getTableRow() != null ? getTableRow().getItem() : null;
                    if (produto != null && callback != null) {
                        callback.abrirEdicao(produto);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        };
    }
}