package com.ossobo.gestaoDepIt.controlls.inventario.inventarioList;

import com.ossobo.gestaoDepIt.db.models.InventarioEquipamentos;
import com.ossobo.winterfx.router.Rotas;
import com.ossobo.winterfx.router.model.Params;
import com.ossobo.winterfx.router.model.ResponseData;
import javafx.application.Platform;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * InventarioActions v1.0
 *
 * Responsabilidade: Despachar rotas de dados do inventário e atualizar o
 *                   InventarioState. Sem UI, sem service injetado.
 *
 * Consultas: canal GET (inventario-equipamentos/service/*).
 * Refresh:   canal EXEC (inventario-list/refresh).
 *
 * Falhas propagam no CompletableFuture — o controller decide o feedback.
 *
 * @since v1.0
 */
public class InventarioActions {

    /** Raiz das rotas de consulta. */
    public static final String ROTAS = "inventario-equipamentos/service";

    private final InventarioState state;

    public InventarioActions(InventarioState state) {
        this.state = state;
    }

    // ================================================================
    // AÇÃO: CARREGAR DADOS (página atual + count)
    // ================================================================

    public CompletableFuture<Void> carregarDados() {
        return CompletableFuture.runAsync(() -> {
            var resposta = Rotas.get(ROTAS + "/paginados",
                    Params.with("pagina", state.getPaginaAtual())
                            .and("tamanho", state.getItensPorPagina()));

            if (!resposta.isSuccess()) {
                throw new IllegalStateException(resposta.getMessage());
            }
            List<InventarioEquipamentos> equipamentos = resposta.getDataList("equipamentos");

            long total = lerTotal(equipamentos.size());

            Platform.runLater(() -> {
                state.setEquipamentosData(equipamentos);
                state.setTotalPaginas((int) total);
            });
        });
    }

    // ================================================================
    // AÇÃO: APLICAR FILTROS
    // ================================================================

    public CompletableFuture<Void> aplicarFiltros(Params filtros) {
        return CompletableFuture.runAsync(() -> {
            var resposta = Rotas.get(ROTAS + "/com-filtros", filtros);

            if (!resposta.isSuccess()) {
                throw new IllegalStateException(resposta.getMessage());
            }
            List<InventarioEquipamentos> equipamentos = resposta.getDataList("equipamentos");

            Platform.runLater(() -> {
                state.setEquipamentosData(equipamentos);
                state.setTotalPaginas(equipamentos.size());
                state.primeiraPagina();
            });
        });
    }

    // ================================================================
    // HELPERS
    // ================================================================

    /**
     * Lê "total" aceitando Integer ou Long (ResponseData estrito).
     * Fallback: tamanho da página corrente.
     */
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

    public void cleanup() {
        // Rotas não retêm estado.
    }
}