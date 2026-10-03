package com.ossobo.gestaoDepIt.controlls.inventario.inventarioList;

import javafx.animation.PauseTransition;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.util.Duration;

/**
 * InventarioUIUpdater v1.0
 *
 * Responsabilidade única: Atualizar labels, progresso e estatísticas da
 * tela de inventário. Sem Thread.sleep — PauseTransition para delays.
 *
 * @since v1.0
 */
public class InventarioUIUpdater {

    private final InventarioState state;

    private Label statusLabel;
    private Label estatisticasLabel;
    private ProgressIndicator progressIndicator;
    private Button novoButton;
    private Button aplicarFiltrosButton;
    private Button limparFiltrosButton;
    private Label statTotal;
    private Label statAtivos;
    private Label statManutencao;
    private Label statCriticos;
    private Label statSemMac;

    public InventarioUIUpdater(InventarioState state) {
        this.state = state;
    }

    // ===== INICIALIZAÇÃO UI =====
    public void initializeComponents(
            Label status,
            Label estatisticas,
            ProgressIndicator progress,
            Button novo,
            Button aplicar,
            Button limpar,
            Label total, Label ativos, Label manutencao,
            Label criticos, Label semMac) {
        this.statusLabel = status;
        this.estatisticasLabel = estatisticas;
        this.progressIndicator = progress;
        this.novoButton = novo;
        this.aplicarFiltrosButton = aplicar;
        this.limparFiltrosButton = limpar;
        this.statTotal = total;
        this.statAtivos = ativos;
        this.statManutencao = manutencao;
        this.statCriticos = criticos;
        this.statSemMac = semMac;
    }

    // ===== STATUS =====
    public void updateStatus(String mensagem) {
        if (statusLabel != null) statusLabel.setText(mensagem);
    }

    // ===== PROGRESSO =====
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

    // ===== SUCESSO (com auto-clear) =====
    public void showSuccess(String mensagem) {
        updateStatus("✅ " + mensagem);

        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(e -> updateStatus("Pronto"));
        delay.play();
    }

    // ===== ESTATÍSTICAS =====
    public void calcularEstatisticas() {
        var dados = state.getEquipamentosData();

        long total      = dados.size();
        long ativos     = dados.stream()
                .filter(e -> "ATIVO".equals(e.status()) || "EM_USO".equals(e.status())).count();
        long manutencao = dados.stream()
                .filter(e -> "MANUTENCAO".equals(e.status())).count();
        long criticos   = dados.stream()
                .filter(e -> "CRITICO".equals(e.condicao())).count();
        long semMac     = dados.stream()
                .filter(e -> e.enderecoMac() == null || e.enderecoMac().isEmpty()).count();

        if (statTotal != null)      statTotal.setText(String.valueOf(total));
        if (statAtivos != null)     statAtivos.setText(String.valueOf(ativos));
        if (statManutencao != null) statManutencao.setText(String.valueOf(manutencao));
        if (statCriticos != null)   statCriticos.setText(String.valueOf(criticos));
        if (statSemMac != null)     statSemMac.setText(String.valueOf(semMac));

        if (estatisticasLabel != null) {
            estatisticasLabel.setText(String.format(
                    "Total: %d | Ativos: %d | Manutenção: %d | Críticos: %d | Sem MAC: %d",
                    total, ativos, manutencao, criticos, semMac));
        }
    }

    // ===== LIMPEZA =====
    public void cleanup() {
        statusLabel = null;
        estatisticasLabel = null;
        progressIndicator = null;
        novoButton = null;
        aplicarFiltrosButton = null;
        limparFiltrosButton = null;
        statTotal = null;
        statAtivos = null;
        statManutencao = null;
        statCriticos = null;
        statSemMac = null;
    }
}