package com.ossobo.gestaoDepIt.controlls.relatorio.avancado;

import javafx.scene.control.Label;
import javafx.scene.control.TableView;

import java.util.Map;

/**
 * AvancadoUIUpdater v1.0
 *
 * Renderiza o resultado do relatório avançado no TableView + labels de status.
 */
public final class AvancadoUIUpdater {

    private AvancadoUIUpdater() {}

    public static void aplicarResultado(TableView<Map<String, Object>> tabela,
                                        Label totalRegistros,
                                        Label status,
                                        Label tempoExecucao,
                                        AvancadoActions.Resultado r,
                                        long millis) {
        if (tabela != null) {
            AvancadoTableManager.aplicarColunasResultado(tabela, r.colunas());
            tabela.getItems().setAll(r.linhas());
        }
        if (totalRegistros != null) totalRegistros.setText(String.valueOf(r.total()));
        if (status != null) status.setText("Pronto. " + r.total() + " registro(s).");
        if (tempoExecucao != null) tempoExecucao.setText(millis + " ms");
    }

    public static void atualizarJoinsAtivos(Label label, AvancadoState state) {
        if (label == null) return;
        int n = state.getJoinsAtivos().size();
        label.setText(n == 0 ? "" : n + " JOIN(s) ativo(s)");
    }
}