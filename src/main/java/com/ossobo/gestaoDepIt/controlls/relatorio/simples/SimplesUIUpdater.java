package com.ossobo.gestaoDepIt.controlls.relatorio.simples;

import javafx.scene.control.Label;
import javafx.scene.control.TableView;

import java.util.List;
import java.util.Map;

/**
 * SimplesUIUpdater v1.0
 *
 * Renderiza o resultado no TableView + status label.
 */
public final class SimplesUIUpdater {

    private SimplesUIUpdater() {}

    public static void aplicarResultado(TableView<Map<String, Object>> tabela,
                                        Label statusLabel,
                                        Label totalLabel,
                                        SimplesActions.RelatorioSimplesResultado r) {
        if (tabela != null) {
            SimplesTableManager.aplicar(tabela, r.colunas());
            tabela.getItems().setAll(r.linhas());
        }
        if (statusLabel != null) {
            statusLabel.setText(r.total() + " registro(s)");
        }
        if (totalLabel != null) {
            totalLabel.setText(r.total() + " registros");
        }
    }
}