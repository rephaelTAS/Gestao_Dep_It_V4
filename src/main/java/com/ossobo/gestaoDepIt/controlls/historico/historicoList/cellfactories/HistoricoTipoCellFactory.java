package com.ossobo.gestaoDepIt.controlls.historico.historicoList.cellfactories;

import com.ossobo.gestaoDepIt.db.enums.TipoEvento;
import com.ossobo.gestaoDepIt.db.models.HistoricoEventos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

/**
 * HistoricoTipoCellFactory v1.0
 *
 * Coluna "Tipo" com badge colorido por semântica:
 *   VERDE  → criação / instalação / login (entrada de estado)
 *   AZUL   → atualização / movimentação / transferência / localização / devolução
 *   VERMELHO → baixa / exclusão / logout / manutenção (saída ou risco)
 *   CINZA  → desconhecido
 */
public class HistoricoTipoCellFactory
        implements Callback<TableColumn<HistoricoEventos, String>, TableCell<HistoricoEventos, String>> {

    @Override
    public TableCell<HistoricoEventos, String> call(TableColumn<HistoricoEventos, String> param) {
        return new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll(
                        "tipo-cell",
                        "tipo-cell-criacao",
                        "tipo-cell-atualizacao",
                        "tipo-cell-exclusao",
                        "tipo-cell-baixa",
                        "tipo-cell-manutencao",
                        "tipo-cell-outro");

                if (empty || item == null) {
                    setText(null);
                    return;
                }
                setText(item);
                getStyleClass().add("tipo-cell");
                getStyleClass().add(classeDe(item));
            }
        };
    }

    private String classeDe(String tipo) {
        try {
            TipoEvento t = TipoEvento.de(tipo);
            return switch (t) {
                case CRIACAO, INSTALACAO, LOGIN -> "tipo-cell-criacao";
                case ATUALIZACAO, MOVIMENTACAO, TRANSFERENCIA,
                     LOCALIZACAO, DEVOLUCAO -> "tipo-cell-atualizacao";
                case EXCLUSAO, BAIXA -> "tipo-cell-exclusao";
                case MANUTENCAO, LOGOUT -> "tipo-cell-manutencao";
            };
        } catch (IllegalArgumentException e) {
            return "tipo-cell-outro";
        }
    }
}