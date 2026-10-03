package com.ossobo.gestaoDepIt.controlls.product.catalogoList;

import com.ossobo.gestaoDepIt.db.models.CatalogoProdutos;
import com.ossobo.winterfx.router.Rotas;
import com.ossobo.winterfx.router.model.Params;
import com.ossobo.winterfx.router.model.ResponseData;
import javafx.application.Platform;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * CatalogActions v4.2
 *
 * Responsabilidade: Despachar rotas de dados do catálogo e atualizar o
 *                   CatalogState. Sem qualquer UI, sem service injetado.
 *
 * v4.1 — Correção da API de Params (WinterFX 20.0.1): Params.with/and.
 * v4.2 — Leitura tolerante de "total" no contar/total: aceita Integer OU Long
 *        via {@code Number.longValue()}, evitando o erro estrito do
 *        {@code ResponseData.getDataLong(...)} ("esperado Long, mas a rota
 *        entregou Integer"). O domínio devolve int — o consumidor se adapta.
 *
 * Threading: rotas de dados rodam fora da FX Thread; mutações de state
 *            voltam via {@code Platform.runLater}.
 *
 * @since v4.0
 */
public class CatalogActions {

    /** Raiz das rotas do catálogo — único lugar com a string. */
    public static final String ROTAS = "catalogo-produtos/service";

    private final CatalogState state;

    public CatalogActions(CatalogState state) {
        this.state = state;
    }

    // ================================================================
    // AÇÃO: ATIVAR / DESATIVAR
    // ================================================================

    public CompletableFuture<ResponseData> ativar(CatalogoProdutos produto) {
        return putAssincrono(ROTAS + "/ativar", Params.with("sku", produto.sku()));
    }

    public CompletableFuture<ResponseData> desativar(CatalogoProdutos produto) {
        return putAssincrono(ROTAS + "/desativar", Params.with("sku", produto.sku()));
    }

    public CompletableFuture<ResponseData> alternarStatus(CatalogoProdutos produto) {
        boolean ativoAtual = Boolean.TRUE.equals(produto.ativo());
        var future = ativoAtual ? desativar(produto) : ativar(produto);

        return future.thenApply(resposta -> {
            if (resposta.isSuccess()) {
                CatalogoProdutos atualizado = produto.comStatus(!ativoAtual);
                return resposta.withData("produto", atualizado);
            }
            return resposta;
        });
    }

    // ================================================================
    // AÇÃO: CARREGAR DADOS
    // ================================================================

    public CompletableFuture<Void> carregarDados() {
        return CompletableFuture.runAsync(() -> {
            var resposta = Rotas.get(ROTAS + "/paginados",
                    Params.with("pagina", state.getPaginaAtual())
                            .and("tamanho", state.getItensPorPagina()));

            if (!resposta.isSuccess()) {
                throw new IllegalStateException(resposta.getMessage());
            }
            List<CatalogoProdutos> produtos = resposta.getDataList("produtos");

            long totalRegistros = lerTotal(produtos.size());

            Platform.runLater(() -> {
                state.setProdutosData(produtos);
                state.setTotalPaginas(Math.toIntExact(totalRegistros));
            });
        });
    }

    /**
     * Lê "total" da rota contar/total aceitando Integer ou Long.
     * Fallback: tamanho da página corrente (UI degradada, mas funcional).
     */
    private long lerTotal(int fallback) {
        try {
            var contagem = Rotas.get(ROTAS + "/contar/total");
            if (!contagem.isSuccess()) return fallback;

            Object bruto = contagem.getData().get("total");
            if (bruto instanceof Number n) {
                return n.longValue();
            }
            return fallback;
        } catch (RuntimeException e) {
            System.getLogger(CatalogActions.class.getName())
                    .log(System.Logger.Level.WARNING,
                            "contar/total indisponível, usando tamanho da página: {0}",
                            e.getMessage());
            return fallback;
        }
    }



    // ================================================================
    // AÇÃO: APLICAR FILTROS
    // ================================================================

    public CompletableFuture<Void> aplicarFiltros() {
        return CompletableFuture.runAsync(() -> {
            var resposta = Rotas.get(ROTAS + "/buscar/com-filtros", paramsDeFiltros());

            if (!resposta.isSuccess()) {
                throw new IllegalStateException(resposta.getMessage());
            }
            List<CatalogoProdutos> produtosFiltrados = resposta.getDataList("produtos");

            List<CatalogoProdutos> resultadoFinal = aplicarFiltroEstoque(produtosFiltrados);

            Platform.runLater(() -> {
                state.setProdutosData(resultadoFinal);
                state.setTotalPaginas(resultadoFinal.size());
                state.primeiraPagina();
            });
        });
    }

    private Params paramsDeFiltros() {
        Params params = null;

        if (state.getFiltroTipo() != null)
            params = Params.with("tipo", state.getFiltroTipo());
        if (state.getFiltroCategoria() != null)
            params = (params == null) ? Params.with("categoria", state.getFiltroCategoria())
                    : params.and("categoria", state.getFiltroCategoria());
        if (state.getFiltroMarca() != null)
            params = (params == null) ? Params.with("marca", state.getFiltroMarca())
                    : params.and("marca", state.getFiltroMarca());
        if (state.getFiltroModelo() != null)
            params = (params == null) ? Params.with("modelo", state.getFiltroModelo())
                    : params.and("modelo", state.getFiltroModelo());
        Boolean status = getStatusFiltrado();
        if (status != null)
            params = (params == null) ? Params.with("ativo", status)
                    : params.and("ativo", status);

        return params == null ? Params.with("_", "") : params;
    }

    private List<CatalogoProdutos> aplicarFiltroEstoque(List<CatalogoProdutos> produtos) {
        String filtro = state.getFiltroEstoque();
        if (filtro == null || "Todos".equals(filtro)) {
            return produtos;
        }
        return produtos.stream()
                .filter(p -> {
                    Integer estoque = p.totalRecebido();
                    if (estoque == null) estoque = 0;
                    return switch (filtro) {
                        case "Com Estoque"   -> estoque > 0;
                        case "Sem Estoque"   -> estoque == 0;
                        case "Estoque Baixo" -> estoque > 0 && estoque <= 5;
                        default              -> true;
                    };
                })
                .collect(Collectors.toList());
    }

    // ================================================================
    // UTILITÁRIOS
    // ================================================================

    public void atualizarItemNaLista(CatalogoProdutos produtoAtualizado) {
        var lista = state.getProdutosData();
        int index = lista.indexOf(produtoAtualizado);
        if (index >= 0) {
            lista.set(index, produtoAtualizado);
        }
    }

    public CompletableFuture<Void> recarregar() {
        return carregarDados();
    }

    public void cleanup() {
        // Rotas não retêm estado — nada a limpar
    }

    // ================================================================
    // INTERNO
    // ================================================================

    private Boolean getStatusFiltrado() {
        if (state.getFiltroStatus() == null) return null;
        return switch (state.getFiltroStatus()) {
            case "Ativos"   -> true;
            case "Inativos" -> false;
            default         -> null;
        };
    }

    private CompletableFuture<ResponseData> putAssincrono(String rota, Params params) {
        return CompletableFuture.supplyAsync(() -> Rotas.put(rota, params));
    }
}