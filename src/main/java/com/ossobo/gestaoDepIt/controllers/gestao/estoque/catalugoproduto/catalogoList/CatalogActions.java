package com.ossobo.gestaoDepIt.controllers.gestao.estoque.catalugoproduto.catalogoList;

import com.ossobo.gestaoDepIt.db.models.CatalogoProdutos;
import com.ossobo.gestaoDepIt.db.models.InventarioEquipamentos;
import com.ossobo.winterfx.router.Rotas;
import com.ossobo.winterfx.router.model.Params;
import com.ossobo.winterfx.router.model.ResponseData;
import javafx.application.Platform;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Lógica de AÇÕES do catálogo — sem qualquer UI.
 *
 * <h2>v4.0 — Era NexusFX encerrada</h2>
 * Os diálogos e alerts do NexusFX (predecessor) foram absorvidos pelo WinterFX:
 * <ul>
 *   <li>Modais → {@code @FloatingWindow} no controller (ver CatalogListController)</li>
 *   <li>Confirmação → {@code @OnConfirmation} no Pipeline</li>
 *   <li>Feedback sucesso/erro → status da tela + {@code @OnError}</li>
 * </ul>
 * Esta classe ficou com o que lhe é próprio: DESPACHAR rotas e atualizar o state.
 * NUNCA lança para erro de negócio — as rotas devolvem envelope; falhas de
 * framework propagam no CompletableFuture para o controller decidir.
 *
 * <h2>Threading</h2>
 * Rotas de dados rodam na thread de origem (sem FX lock) — seguras no
 * {@code runAsync}. Mutações de state/ObservableList voltam à FX Thread
 * via {@code Platform.runLater}.
 *
 * @author Rafael Tavares
 * @since 2.0
 */
public class CatalogActions {

    /** Raiz das rotas do catálogo — único lugar com a string. */
    public static final String ROTAS = "catalogo-produtos/service";

    private final CatalogState state;

    /** v4.0: sem service injetado — rotas substituem o acesso direto. */
    public CatalogActions(CatalogState state) {
        this.state = state;
    }

    // ================================================================
    // AÇÃO: ATIVAR / DESATIVAR (toggle — a confirmação é do controller)
    // ================================================================

    /** Ativa o produto. Emissor alternativo direto: {@code Rotas.put(ROTAS+"/ativar", Params.withString("sku", sku))}. */
    public CompletableFuture<ResponseData> ativar(CatalogoProdutos produto) {
        return putAssincrono(ROTAS + "/ativar", Params.withString("sku", produto.getSku()));
    }

    /** Desativa o produto (soft delete). */
    public CompletableFuture<ResponseData> desativar(CatalogoProdutos produto) {
        return putAssincrono(ROTAS + "/desativar", Params.withString("sku", produto.getSku()));
    }

    /**
     * Executa o toggle (o controller pergunta antes via {@code @OnConfirmation}).
     * Atualiza o modelo local com o estado do envelope.
     */
    public CompletableFuture<ResponseData> alternarStatus(CatalogoProdutos produto) {
        boolean ativoAtual = Boolean.TRUE.equals(produto.getAtivo());
        var future = ativoAtual ? desativar(produto) : ativar(produto);

        future.thenAccept(resposta -> Platform.runLater(() -> {
            if (resposta.isSuccess()) {
                produto.setAtivo(!ativoAtual);   // reflete o estado confirmado pelo servidor
            }
        }));
        return future;
    }

    // ================================================================
    // AÇÃO: CARREGAR DADOS (v4.0 — ROTAS + PAGINAÇÃO CORRETA)
    // ================================================================

    /**
     * Carrega a página atual + total de páginas (COUNT no banco, O(1)).
     *
     * <p>Falhas PROPAGAM no future — o controller decide o feedback com
     * {@code .exceptionally(...)}. Nada é engolido aqui.</p>
     */
    public CompletableFuture<Void> carregarDados() {
        return CompletableFuture.runAsync(() -> {
            // 1. Produtos da página atual
            var resposta = Rotas.get(ROTAS + "/paginados",
                    Params.withInt("pagina", state.getPaginaAtual())
                            .withInt("tamanho", state.getItensPorPagina()));

            if (!resposta.isSuccess()) {
                throw new IllegalStateException(resposta.getMessage());
            }
            var produtos = resposta.getDataList("produtos");

            // 2. Total de PÁGINAS via COUNT no banco (correção do v2.x que
            //    atribuía contagem de registros como total de páginas)
            var contagem = Rotas.get(ROTAS + "/contar/total");
            long totalRegistros = contagem.isSuccess()
                    ? contagem.getDataLong("total")
                    : produtos.size();   // fallback defensivo

            int totalPaginas = Math.max(1,
                    (int) Math.ceil((double) totalRegistros / state.getItensPorPagina()));

            // 3. UI mutável → FX Thread (ObservableList não é thread-safe)
            Platform.runLater(() -> {
                state.setProdutosData(produtos);
                state.setTotalPaginas(totalPaginas);
            });
        });
    }

    // ================================================================
    // AÇÃO: APLICAR FILTROS (v4.0 — FILTROS COMBINADOS NO BANCO)
    // ================================================================

    /**
     * Filtros de tipo/categoria/marca/modelo/status resolvem-se NO BANCO
     * (rota buscar/com-filtros — só os selecionados são enviados).
     * Filtro de estoque permanece em memória (regra de apresentação).
     */
    public CompletableFuture<Void> aplicarFiltros() {
        return CompletableFuture.runAsync(() -> {
            var resposta = Rotas.get(ROTAS + "/buscar/com-filtros", paramsDeFiltros());

            if (!resposta.isSuccess()) {
                throw new IllegalStateException(resposta.getMessage());
            }
            var produtosFiltrados = resposta.getDataList("produtos");

            List<CatalogoProdutos> resultadoFinal = aplicarFiltroEstoque(produtosFiltrados);

            Platform.runLater(() -> {
                state.setProdutosData(resultadoFinal);
                state.setTotalPaginas(Math.max(1, resultadoFinal.size()));
                state.primeiraPagina();
            });
        });
    }

    /** Monta o Params APENAS com os filtros selecionados (ausentes = null no handler). */
    private Params paramsDeFiltros() {
        var params = Params.empty();
        if (state.getFiltroTipo() != null)      params.and("tipo", state.getFiltroTipo());
        if (state.getFiltroCategoria() != null) params.and("categoria", state.getFiltroCategoria());
        if (state.getFiltroMarca() != null)     params.and("marca", state.getFiltroMarca());
        if (state.getFiltroModelo() != null)    params.and("modelo", state.getFiltroModelo());
        var status = getStatusFiltrado();
        if (status != null)                     params.and("ativo", status);
        return params;
    }

    /**
     * "Com Estoque" → totalRecebido > 0 · "Sem Estoque" → 0/null ·
     * "Estoque Baixo" → >0 && <=5 · "Todos" → sem filtro.
     */
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

    /** Atualiza um item na lista local (requer ObservableList mutável no state). */
    public void atualizarItemNaLista(CatalogoProdutos produtoAtualizado) {
        var lista = state.getProdutosData();
        int index = lista.indexOf(produtoAtualizado);
        if (index >= 0) {
            lista.set(index, produtoAtualizado);
        }
    }

    /** Recarrega o state a partir das rotas (após salvar/editar num modal). */
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

    /** PUT assíncrono — roda em background; o envelope sobe intacto. */
    private CompletableFuture<ResponseData> putAssincrono(String rota, Params params) {
        return CompletableFuture.supplyAsync(() -> Rotas.put(rota, params));
    }
}