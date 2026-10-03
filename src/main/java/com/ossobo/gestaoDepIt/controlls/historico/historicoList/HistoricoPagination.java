package com.ossobo.gestaoDepIt.controlls.historico.historicoList;

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

/**
 * HistoricoPagination v1.0
 *
 * Controle de página + atualização da UI de paginação.
 *
 * @since v1.0
 */
public class HistoricoPagination {

    private final HistoricoState state;

    private Button btnPrimeira, btnAnterior, btnProxima, btnUltima;
    private Label labelPagina;
    private ComboBox<Integer> comboItensPorPagina;

    public HistoricoPagination(HistoricoState state) {
        this.state = state;
    }

    public void initializeUIComponents(Button primeira, Button anterior,
                                       Button proxima, Button ultima,
                                       Label pagina, ComboBox<Integer> itensPorPagina) {
        this.btnPrimeira = primeira;
        this.btnAnterior = anterior;
        this.btnProxima = proxima;
        this.btnUltima = ultima;
        this.labelPagina = pagina;
        this.comboItensPorPagina = itensPorPagina;
    }

    public void configurePagination() {
        if (comboItensPorPagina != null) {
            comboItensPorPagina.getItems().setAll(15, 30, 50, 100);
            comboItensPorPagina.setValue(state.getItensPorPagina());
            comboItensPorPagina.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && newVal > 0) state.setItensPorPagina(newVal);
            });
        }
    }

    public void goToFirstPage()    { state.primeiraPagina();  updateUI(); }
    public void goToPreviousPage() { state.paginaAnterior();  updateUI(); }
    public void goToNextPage()     { state.proximaPagina();   updateUI(); }
    public void goToLastPage()     { state.ultimaPagina();    updateUI(); }

    public void updateUI() {
        if (labelPagina != null) {
            labelPagina.setText(String.format("Página %d de %d",
                    state.getPaginaAtual(), state.getTotalPaginas()));
        }
        updateButtons();
    }

    private void updateButtons() {
        int atual = state.getPaginaAtual();
        int total = state.getTotalPaginas();
        if (btnPrimeira != null) btnPrimeira.setDisable(atual == 1);
        if (btnAnterior != null) btnAnterior.setDisable(atual == 1);
        if (btnProxima != null)  btnProxima.setDisable(atual >= total);
        if (btnUltima != null)   btnUltima.setDisable(atual >= total);
    }

    public void resetToFirstPage() { state.primeiraPagina(); updateUI(); }

    public void cleanup() {
        btnPrimeira = btnAnterior = btnProxima = btnUltima = null;
        labelPagina = null;
        comboItensPorPagina = null;
    }
}