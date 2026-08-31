package com.ossobo.gestaoDepIt.controllers.gestao.equipamentos.inventario.inventarioList;

import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;

/**
 * Atualiza labels e indicadores da UI do inventário.
 * Responsabilidade única: Stats, status, progresso.
 */
public class InventoryUIUpdater {

    private final InventoryState state;
    private Label statusLabel;
    private ProgressIndicator progressIndicator;
    private Label statTotal;
    private Label statAtivos;
    private Label statManutencao;
    private Label statCriticos;
    private Label statSemMac;

    public InventoryUIUpdater(InventoryState state) {
        this.state = state;
    }

    public void initializeComponents(
            Label status, ProgressIndicator progress,
            Label total, Label ativos, Label manutencao,
            Label criticos, Label semMac) {
        this.statusLabel = status;
        this.progressIndicator = progress;
        this.statTotal = total;
        this.statAtivos = ativos;
        this.statManutencao = manutencao;
        this.statCriticos = criticos;
        this.statSemMac = semMac;
    }

    public void updateStatus(String mensagem) {
        if (statusLabel != null) statusLabel.setText(mensagem);
    }

    public void showProgress(boolean mostrar, String mensagem) {
        if (progressIndicator != null) progressIndicator.setVisible(mostrar);
        updateStatus(mensagem);
    }

    public void calcularEstatisticas() {
        var data = state.getEquipamentosData();
        long total = data.size();
        long ativos = data.stream().filter(e -> "ATIVO".equals(e.status()) || "EM_USO".equals(e.status())).count();
        long manutencao = data.stream().filter(e -> "MANUTENCAO".equals(e.status())).count();
        long criticos = data.stream().filter(e -> "CRITICO".equals(e.condicao())).count();
        long semMac = data.stream().filter(e -> e.enderecoMac() == null || e.enderecoMac().isEmpty()).count();

        if (statTotal != null) statTotal.setText(String.valueOf(total));
        if (statAtivos != null) statAtivos.setText(String.valueOf(ativos));
        if (statManutencao != null) statManutencao.setText(String.valueOf(manutencao));
        if (statCriticos != null) statCriticos.setText(String.valueOf(criticos));
        if (statSemMac != null) statSemMac.setText(String.valueOf(semMac));
    }

    public void cleanup() {}
}