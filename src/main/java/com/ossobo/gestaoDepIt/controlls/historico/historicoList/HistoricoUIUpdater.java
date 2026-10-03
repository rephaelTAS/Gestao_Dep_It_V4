package com.ossobo.gestaoDepIt.controlls.historico.historicoList;

import javafx.animation.PauseTransition;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.util.Duration;

/**
 * HistoricoUIUpdater v1.0
 *
 * Atualiza labels, progresso e estatísticas da tela de histórico.
 * Sem Thread.sleep — PauseTransition para delays.
 *
 * @since v1.0
 */
public class HistoricoUIUpdater {

    private final HistoricoState state;

    private Label statusLabel;
    private Label estatisticasLabel;
    private Label filtrosAtivosLabel;
    private ProgressIndicator progressIndicator;
    private Button btnRelatorio;
    private Button btnExportar;
    private Button btnLimparFiltros;
    private Button btnAplicarFiltros;

    public HistoricoUIUpdater(HistoricoState state) {
        this.state = state;
    }

    public void initializeComponents(
            Label status,
            Label estatisticas,
            Label filtrosAtivos,
            ProgressIndicator progress,
            Button relatorio,
            Button exportar,
            Button limpar,
            Button aplicar) {
        this.statusLabel = status;
        this.estatisticasLabel = estatisticas;
        this.filtrosAtivosLabel = filtrosAtivos;
        this.progressIndicator = progress;
        this.btnRelatorio = relatorio;
        this.btnExportar = exportar;
        this.btnLimparFiltros = limpar;
        this.btnAplicarFiltros = aplicar;
    }

    // ===== STATUS =====
    public void updateStatus(String mensagem) {
        if (statusLabel != null) statusLabel.setText(mensagem);
    }

    // ===== PROGRESSO =====
    public void showProgress(boolean mostrar) {
        if (progressIndicator != null) progressIndicator.setVisible(mostrar);
        if (btnRelatorio != null) btnRelatorio.setDisable(mostrar);
        if (btnExportar != null) btnExportar.setDisable(mostrar);
        if (btnLimparFiltros != null) btnLimparFiltros.setDisable(mostrar);
        if (btnAplicarFiltros != null) btnAplicarFiltros.setDisable(mostrar);
    }

    public void showProgress(boolean mostrar, String mensagem) {
        showProgress(mostrar);
        updateStatus(mensagem);
    }

    // ===== SUCESSO =====
    public void showSuccess(String mensagem) {
        updateStatus("✅ " + mensagem);
        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(e -> updateStatus("Pronto"));
        delay.play();
    }

    // ===== ESTATÍSTICAS =====
    public void calcularEstatisticas() {
        if (estatisticasLabel != null) {
            estatisticasLabel.setText(String.format(
                    "Total: %d evento(s) · Página %d de %d",
                    state.getEventosData().size(),
                    state.getPaginaAtual(),
                    state.getTotalPaginas()));
        }
    }

    public void atualizarFiltrosAtivos(String texto) {
        if (filtrosAtivosLabel != null) {
            filtrosAtivosLabel.setText(texto != null ? texto : "");
        }
    }

    public void cleanup() {
        statusLabel = null;
        estatisticasLabel = null;
        filtrosAtivosLabel = null;
        progressIndicator = null;
        btnRelatorio = null;
        btnExportar = null;
        btnLimparFiltros = null;
        btnAplicarFiltros = null;
    }
}