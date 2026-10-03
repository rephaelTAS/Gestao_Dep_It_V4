package com.ossobo.gestaoDepIt.controlls.historico.historicoList;

import com.ossobo.gestaoDepIt.db.models.HistoricoEventos;
import com.ossobo.winterfx.router.Rotas;
import com.ossobo.winterfx.router.model.Params;
import javafx.application.Platform;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * HistoricoActions v1.0
 *
 * Despacho de rotas do histórico e atualização do HistoricoState.
 * Sem UI, sem service injetado.
 *
 * Consulta: canal GET (historico-eventos/service/*).
 *
 * @since v1.0
 */
public class HistoricoActions {

    private static final String ROTAS = "historico-eventos/service";

    private final HistoricoState state;

    public HistoricoActions(HistoricoState state) {
        this.state = state;
    }

    // ============================================================
    // AÇÃO: CARREGAR PÁGINA ATUAL
    // ============================================================

    public CompletableFuture<Void> carregarDados() {
        return CompletableFuture.runAsync(() -> {
            var resposta = Rotas.get(ROTAS + "/paginados",
                    Params.with("pagina", state.getPaginaAtual())
                            .and("tamanho", state.getItensPorPagina()));

            if (!resposta.isSuccess()) {
                throw new IllegalStateException(resposta.getMessage());
            }
            List<HistoricoEventos> eventos = resposta.getDataList("eventos");
            long total = lerTotal(eventos.size());

            Platform.runLater(() -> {
                state.setEventosData(eventos);
                state.setTotalPaginas((int) total);
            });
        });
    }

    // ============================================================
    // AÇÃO: APLICAR FILTROS
    // ============================================================

    public CompletableFuture<Void> aplicarFiltros(Params filtros) {
        return CompletableFuture.runAsync(() -> {
            var resposta = Rotas.get(ROTAS + "/com-filtros", filtros);

            if (!resposta.isSuccess()) {
                throw new IllegalStateException(resposta.getMessage());
            }
            List<HistoricoEventos> eventos = resposta.getDataList("eventos");

            Platform.runLater(() -> {
                state.setEventosData(eventos);
                state.setTotalPaginas(eventos.size());
                state.primeiraPagina();
            });
        });
    }

    // ============================================================
    // HELPERS
    // ============================================================

    /** Leitura tolerante de "total" — aceita Integer ou Long. */
    private long lerTotal(int fallback) {
        try {
            var resp = Rotas.get(ROTAS + "/total");
            if (!resp.isSuccess()) return fallback;
            Object bruto = resp.getData().get("total");
            return (bruto instanceof Number n) ? n.longValue() : fallback;
        } catch (RuntimeException e) {
            return fallback;
        }
    }

    public void cleanup() { }
}