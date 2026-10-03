package com.ossobo.gestaoDepIt.controlls.product.catalogoList;

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

/**
 * Gerencia toda a lógica de paginação
 * ✅ Single Responsibility: Apenas paginação
 * ✅ Coesão Forte: <120 linhas
 * ✅ Zero Terminal Output
 */
public class CatalogPagination {
    private final CatalogState state;

    // ===== COMPONENTES UI =====
    private Button btnPrimeira;
    private Button btnAnterior;
    private Button btnProxima;
    private Button btnUltima;
    private Label labelPagina;
    private ComboBox<Integer> comboItensPorPagina;

    // ===== CONSTRUTOR =====
    public CatalogPagination(CatalogState state) {
        this.state = state;
    }

    // ===== INICIALIZAÇÃO UI =====
    public void initializeUIComponents(
            Button primeira,
            Button anterior,
            Button proxima,
            Button ultima,
            Label pagina,
            ComboBox<Integer> itensPorPagina
    ) {
        this.btnPrimeira = primeira;
        this.btnAnterior = anterior;
        this.btnProxima = proxima;
        this.btnUltima = ultima;
        this.labelPagina = pagina;
        this.comboItensPorPagina = itensPorPagina;
    }

    // ===== CONFIGURAÇÃO DE PAGINAÇÃO =====
    public void configurePagination() {
        if (comboItensPorPagina != null) {
            comboItensPorPagina.getItems().addAll(10, 20, 50, 100);
            comboItensPorPagina.setValue(state.getItensPorPagina());

            comboItensPorPagina.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && newVal > 0) {
                    state.setItensPorPagina(newVal);
                }
            });
        }
    }

    // ===== CONTROLE DE PAGINAÇÃO =====
    public void goToFirstPage() {
        state.primeiraPagina();
        updateUI();
    }

    public void goToPreviousPage() {
        state.paginaAnterior();
        updateUI();
    }

    public void goToNextPage() {
        state.proximaPagina();
        updateUI();
    }

    public void goToLastPage() {
        state.ultimaPagina();
        updateUI();
    }

    // ===== ATUALIZAÇÃO DE UI =====
    public void updateUI() {
        if (labelPagina != null) {
            labelPagina.setText(String.format("Página %d de %d",
                    state.getPaginaAtual(), state.getTotalPaginas()));
        }

        updateNavigationButtons();
    }

    private void updateNavigationButtons() {
        if (btnPrimeira != null) btnPrimeira.setDisable(state.getPaginaAtual() == 1);
        if (btnAnterior != null) btnAnterior.setDisable(state.getPaginaAtual() == 1);
        if (btnProxima != null) btnProxima.setDisable(state.getPaginaAtual() >= state.getTotalPaginas());
        if (btnUltima != null) btnUltima.setDisable(state.getPaginaAtual() >= state.getTotalPaginas());
    }

    // ===== UTILITÁRIOS =====
    public void calculateTotalPages(int totalItems) {
        state.setTotalPaginas(totalItems);
        updateUI();
    }

    public void resetToFirstPage() {
        state.primeiraPagina();
        updateUI();
    }

    public void cleanup() {
        btnPrimeira = null;
        btnAnterior = null;
        btnProxima = null;
        btnUltima = null;
        labelPagina = null;
        comboItensPorPagina = null;
    }
}