package com.ossobo.gestaoDepIt.controlls.funcionario.funcionarioList;

import javafx.animation.PauseTransition;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.util.Duration;

/**
 * FuncionarioUIUpdater v1.0
 *
 * Responsabilidade: atualizar labels, progress indicator e notificações
 *                   da tela de listagem de funcionários.
 *
 * Espelha CatalogUIUpdater.
 *
 * @since v1.0
 */
public class FuncionarioUIUpdater {

    private Label statusLabel;
    private Label estatisticasLabel;
    private ProgressIndicator progressIndicator;
    private Button novoButton;
    private Button aplicarFiltrosButton;
    private Button limparFiltrosButton;

    private final FuncionarioState state;

    public FuncionarioUIUpdater(FuncionarioState state) {
        this.state = state;
    }

    public void initializeComponents(
            Label status,
            Label estatisticas,
            ProgressIndicator progress,
            Button novo,
            Button aplicar,
            Button limpar) {
        this.statusLabel = status;
        this.estatisticasLabel = estatisticas;
        this.progressIndicator = progress;
        this.novoButton = novo;
        this.aplicarFiltrosButton = aplicar;
        this.limparFiltrosButton = limpar;
    }

    public void updateStatus(String mensagem) {
        if (statusLabel != null) statusLabel.setText(mensagem);
    }

    public void updateStatistics(String mensagem) {
        if (estatisticasLabel != null) estatisticasLabel.setText(mensagem);
    }

    public void showProgress(boolean mostrar) {
        if (progressIndicator != null) progressIndicator.setVisible(mostrar);
        if (novoButton != null) novoButton.setDisable(mostrar);
        if (aplicarFiltrosButton != null) aplicarFiltrosButton.setDisable(mostrar);
        if (limparFiltrosButton != null) limparFiltrosButton.setDisable(mostrar);
    }

    public void showProgress(boolean mostrar, String mensagem) {
        showProgress(mostrar);
        updateStatus(mensagem);
    }

    public void showSuccess(String mensagem) {
        updateStatus("✅ " + mensagem);
        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(e -> updateStatus("Pronto"));
        delay.play();
    }

    public void calcularEstatisticas() {
        if (estatisticasLabel == null) return;

        long totalAtivos = state.getFuncionariosData().stream()
                .filter(f -> Boolean.TRUE.equals(f.ativo()))
                .count();

        String estatisticas = String.format(
                "Total: %d funcionários ativos | Página: %d/%d | Itens: %d",
                totalAtivos,
                state.getPaginaAtual(),
                state.getTotalPaginas(),
                state.getItensPorPagina());

        updateStatistics(estatisticas);
    }

    public void cleanup() {
        statusLabel = null;
        estatisticasLabel = null;
        progressIndicator = null;
        novoButton = null;
        aplicarFiltrosButton = null;
        limparFiltrosButton = null;
    }
}