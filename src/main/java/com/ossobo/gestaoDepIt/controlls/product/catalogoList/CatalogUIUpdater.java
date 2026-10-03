package com.ossobo.gestaoDepIt.controlls.product.catalogoList;

import javafx.animation.PauseTransition;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.util.Duration;

/**
 * Gerencia todas as atualizações de interface do usuário.
 * v1.1 - Substitui Thread.sleep por PauseTransition (JavaFX-native).
 * Responsabilidade única: Atualizar labels, progress, estatísticas, notificações.
 */
public class CatalogUIUpdater {

    private Label statusLabel;
    private Label estatisticasLabel;
    private Label filtrosAtivosLabel;
    private ProgressIndicator progressIndicator;
    private Button novoButton;
    private Button aplicarFiltrosButton;
    private Button limparFiltrosButton;

    private final CatalogState state;

    public CatalogUIUpdater(CatalogState state) {
        this.state = state;
    }

    /**
     * Configura componentes UI.
     */
    public void initializeComponents(
            Label status,
            Label estatisticas,
            Label filtrosAtivos,
            ProgressIndicator progress,
            Button novo,
            Button aplicar,
            Button limpar) {
        this.statusLabel = status;
        this.estatisticasLabel = estatisticas;
        this.filtrosAtivosLabel = filtrosAtivos;
        this.progressIndicator = progress;
        this.novoButton = novo;
        this.aplicarFiltrosButton = aplicar;
        this.limparFiltrosButton = limpar;
    }

    // ===== ATUALIZAÇÃO DE STATUS =====

    public void updateStatus(String mensagem) {
        if (statusLabel != null) {
            statusLabel.setText(mensagem);
        }
    }

    public void updateStatistics(String mensagem) {
        if (estatisticasLabel != null) {
            estatisticasLabel.setText(mensagem);
        }
    }

    public void updateActiveFilters(String filtros) {
        if (filtrosAtivosLabel != null) {
            filtrosAtivosLabel.setText(filtros);
        }
    }

    // ===== CONTROLE DE PROGRESSO =====

    /**
     * Mostra/oculta indicador de progresso e desabilita botões.
     */
    public void showProgress(boolean mostrar) {
        if (progressIndicator != null) {
            progressIndicator.setVisible(mostrar);
        }
        if (novoButton != null) novoButton.setDisable(mostrar);
        if (aplicarFiltrosButton != null) aplicarFiltrosButton.setDisable(mostrar);
        if (limparFiltrosButton != null) limparFiltrosButton.setDisable(mostrar);
    }

    /**
     * Atualiza progresso com mensagem.
     */
    public void showProgress(boolean mostrar, String mensagem) {
        showProgress(mostrar);
        updateStatus(mensagem);
    }



    /**
     * Mostra mensagem de sucesso que desaparece após 3 segundos.
     * v1.1: Usa PauseTransition (thread JavaFX) em vez de new Thread().
     */
    public void showSuccess(String mensagem) {
        updateStatus("✅ " + mensagem);

        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(e -> {
            if (statusLabel != null) {
                updateStatus("Pronto");
            }
        });
        delay.play();
    }

    /**
     * Mostra erro crítico e desabilita botões.
     */
    public void showCriticalError(String mensagem) {


        if (novoButton != null) novoButton.setDisable(true);
        if (aplicarFiltrosButton != null) aplicarFiltrosButton.setDisable(true);
        if (limparFiltrosButton != null) limparFiltrosButton.setDisable(true);
    }

    // ===== ESTATÍSTICAS =====

    /**
     * Calcula e atualiza estatísticas.
     */
    public void calcularEstatisticas() {
        if (estatisticasLabel == null) return;

        long totalAtivos = state.getProdutosData().stream()
                .filter(p -> Boolean.TRUE.equals(p.ativo()))
                .count();

        String estatisticas = String.format(
                "Total: %d produtos ativos | Página: %d/%d | Itens: %d",
                totalAtivos,
                state.getPaginaAtual(),
                state.getTotalPaginas(),
                state.getItensPorPagina()
        );

        updateStatistics(estatisticas);
    }

    // ===== FILTROS ATIVOS =====

    /**
     * Calcula e atualiza label de filtros ativos.
     */
    public void calcularFiltrosAtivos() {
        if (filtrosAtivosLabel == null) return;

        StringBuilder filtros = new StringBuilder("Filtros: ");
        boolean temFiltro = false;

        if (state.getFiltroTipo() != null) {
            filtros.append("Tipo:").append(state.getFiltroTipo()).append(" ");
            temFiltro = true;
        }
        if (state.getFiltroCategoria() != null) {
            filtros.append("Cat:").append(state.getFiltroCategoria()).append(" ");
            temFiltro = true;
        }
        if (state.getFiltroMarca() != null) {
            filtros.append("Marca:").append(state.getFiltroMarca()).append(" ");
            temFiltro = true;
        }
        if (state.getFiltroModelo() != null) {
            filtros.append("Mod:").append(state.getFiltroModelo()).append(" ");
            temFiltro = true;
        }
        if (!"Todos".equals(state.getFiltroEstoque())) {
            filtros.append("Est:").append(state.getFiltroEstoque()).append(" ");
            temFiltro = true;
        }
        if (!"Todos".equals(state.getFiltroStatus())) {
            filtros.append("Status:").append(state.getFiltroStatus());
            temFiltro = true;
        }

        updateActiveFilters(temFiltro ? filtros.toString().trim() : "");
    }

    // ===== LIMPEZA =====

    public void cleanup() {
        statusLabel = null;
        estatisticasLabel = null;
        filtrosAtivosLabel = null;
        progressIndicator = null;
        novoButton = null;
        aplicarFiltrosButton = null;
        limparFiltrosButton = null;
    }
}