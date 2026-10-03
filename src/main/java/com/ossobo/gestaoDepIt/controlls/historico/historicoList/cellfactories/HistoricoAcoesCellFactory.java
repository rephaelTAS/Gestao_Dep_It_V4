package com.ossobo.gestaoDepIt.controlls.historico.historicoList.cellfactories;

import com.ossobo.gestaoDepIt.db.models.HistoricoEventos;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

/**
 * HistoricoAcoesCellFactory v1.0
 *
 * Coluna de ações do histórico: botão 🔍 que abre o Detail.
 * Acesso ao item via getTableRow().getItem() (seguro em células recicladas).
 */
public class HistoricoAcoesCellFactory
        implements Callback<TableColumn<HistoricoEventos, Void>, TableCell<HistoricoEventos, Void>> {

    private final HistoricoAcoesCallback callback;

    public HistoricoAcoesCellFactory(HistoricoAcoesCallback callback) {
        this.callback = callback;
    }

    @Override
    public TableCell<HistoricoEventos, Void> call(TableColumn<HistoricoEventos, Void> param) {
        return new TableCell<>() {

            private final Button btnDetalhes = new Button("🔍");

            {
                btnDetalhes.getStyleClass().add("historico-action-btn");
                btnDetalhes.setOnAction(e -> {
                    HistoricoEventos ev = getTableRow() != null ? getTableRow().getItem() : null;
                    if (ev != null && callback != null) callback.abrirDetalhes(ev);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnDetalhes);
            }
        };
    }
}