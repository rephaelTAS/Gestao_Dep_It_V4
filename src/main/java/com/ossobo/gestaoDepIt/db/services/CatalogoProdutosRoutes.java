package com.ossobo.gestaoDepIt.db.services;

import com.ossobo.gestaoDepIt.db.models.CatalogoProdutos;
import com.ossobo.winterfx.anotations.Component;
import com.ossobo.winterfx.anotations.DeleteMapping;
import com.ossobo.winterfx.anotations.GetMapping;
import com.ossobo.winterfx.anotations.Inject;
import com.ossobo.winterfx.anotations.Payload;
import com.ossobo.winterfx.anotations.PutMapping;
import com.ossobo.winterfx.anotations.RequestMapping;
import com.ossobo.winterfx.anotations.RouteVar;
import com.ossobo.winterfx.router.model.ResponseData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * CatalogoProdutosRoutes v1.1
 *
 * Responsabilidade: Fronteira de Internal Routing do catálogo de produtos.
 *                   Handlers FINOS: delegam ao CatalogoProdutosService e traduzem
 *                   exceção → ResponseData. Único ponto de captura da camada.
 *
 * <h2>Contrato de chamada (emissor — view)</h2>
 * Com a API tipada do {@code Params}:
 * <pre>{@code
 * // Mutação com objeto:
 * Rotas.put("catalogo-produtos/service/salvar",
 *           Params.with("produto", novoProduto));
 *
 * // Mutação com params tipados:
 * var resposta = Rotas.put("catalogo-produtos/service/estoque/adicionar",
 *           Params.withString("sku", "SKU-001")
 *                 .withInt("quantidade", 5));
 *
 * // Preço como String (vírgula pt-BR suportada):
 * Rotas.put("catalogo-produtos/service/preco/atualizar",
 *           Params.withString("sku", "SKU-001")
 *                 .withString("preco", "1290,50"));
 *
 * // Leitura tipada do retorno:
 * if (resposta.isSuccess()) {
 *     var produto = resposta.getData("produto", CatalogoProdutos.class);
 * }
 *
 * // Listas:
 * var lista = Rotas.get("catalogo-produtos/service/todos");
 * List<CatalogoProdutos> produtos = lista.getDataList("produtos");
 * var total = lista.getDataInt("total");
 * }</pre>
 *
 * <h2>Chaves de retorno acordadas</h2>
 * <table>
 *   <tr><th>Tipo de rota</th><th>Chave</th><th>Conteúdo</th></tr>
 *   <tr><td>CRUD / estoque / preço</td><td>{@code "produto"}</td><td>{@code CatalogoProdutos}</td></tr>
 *   <tr><td>Listagens</td><td>{@code "produtos"} + {@code "total"}</td><td>{@code List} + Integer</td></tr>
 *   <tr><td>Distinct values (combos)</td><td>{@code "valores"} + {@code "total"}</td><td>{@code List<String>} + Integer</td></tr>
 *   <tr><td>Contagens / estatísticas</td><td>{@code "total"} / {@code "valor"} / {@code "estatisticas"}</td><td>numérico / mapa</td></tr>
 *   <tr><td>Exclusões / ativar / desativar</td><td>— (só {@code getMessage()})</td><td>mensagem global</td></tr>
 * </table>
 *
 * <p>Notas: regras de negócio e eventos residem em CatalogoProdutosService (intacto).
 * Esta classe NÃO conhece repository nem EventBus.</p>
 *
 * <p>v1.0 - Criação; 38 rotas (GET leitura / PUT mutação / DELETE remoção).</p>
 * <p>v1.1 - Mensagens no canal certo ({@code withMessage} — o antigo padrão de
 *          devolver String como dado sob a chave "mensagem" foi eliminado);
 *          helpers {@code acao}/{@code escrita}/{@code lista}/{@code valor} com
 *          captura uniforme de {@code IllegalArgumentException}/{@code IllegalStateException}
 *          (antes vazavam por {@code lista} e {@code valor}); {@code erroNegocio()}
 *          extraído (DRY); mensagem com contagem automática nas listas;
 *          Javadoc com o contrato de chamada usando {@code Params} tipado.</p>
 */
@Component
@RequestMapping("catalogo-produtos/service")
public class CatalogoProdutosRoutes {

    private static final Logger logger = LoggerFactory.getLogger(CatalogoProdutosRoutes.class);

    @Inject
    private CatalogoProdutosService service;

    // ============================================================
    // ROTAS — CRUD
    // ============================================================

    /** Cria produto. Emissor: {@code Params.with("produto", objeto)}. */
    @PutMapping("salvar")
    public ResponseData salvar(@Payload("produto") CatalogoProdutos produto) {
        return escrita("Produto salvo com sucesso", "produto",
                () -> service.salvar(produto));
    }

    /** Atualiza produto. Emissor: {@code Params.with("produto", objeto)}. */
    @PutMapping("atualizar")
    public ResponseData atualizar(@Payload("produto") CatalogoProdutos produto) {
        return escrita("Produto atualizado com sucesso", "produto",
                () -> service.atualizar(produto));
    }

    /** Soft delete por SKU. Emissor: {@code Params.withString("sku", "...")}. */
    @DeleteMapping("por/sku")
    public ResponseData excluir(@RouteVar("sku") String sku) {
        return acao("SKU " + sku + " excluído", () -> service.excluir(sku));
    }

    /** Exclusão física por SKU (irreversível). */
    @DeleteMapping("permanente/por/sku")
    public ResponseData excluirPermanentemente(@RouteVar("sku") String sku) {
        return acao("SKU " + sku + " excluído permanentemente",
                () -> service.excluirPermanentemente(sku));
    }

    /** Ativa produto desativado. */
    @PutMapping("ativar")
    public ResponseData ativar(@RouteVar("sku") String sku) {
        return acao("Produto ativado", () -> service.ativar(sku));
    }

    /** Desativa produto (soft delete). */
    @PutMapping("desativar")
    public ResponseData desativar(@RouteVar("sku") String sku) {
        return acao("Produto desativado", () -> service.desativar(sku));
    }

    // ============================================================
    // ROTAS — ESTOQUE
    // ============================================================

    /** Emissor: {@code Params.withString("sku", s).withInt("quantidade", n)}. */
    @PutMapping("estoque/adicionar")
    public ResponseData adicionarEstoque(@RouteVar("sku") String sku,
                                         @RouteVar("quantidade") Integer quantidade) {
        return escrita("Estoque adicionado", "produto",
                () -> service.adicionarEstoque(sku, exigirInt(quantidade, "quantidade")));
    }

    @PutMapping("estoque/remover")
    public ResponseData removerEstoque(@RouteVar("sku") String sku,
                                       @RouteVar("quantidade") Integer quantidade) {
        return escrita("Estoque removido", "produto",
                () -> service.removerEstoque(sku, exigirInt(quantidade, "quantidade")));
    }

    @PutMapping("estoque/atualizar")
    public ResponseData atualizarEstoque(@RouteVar("sku") String sku,
                                         @RouteVar("quantidade") Integer quantidade) {
        return escrita("Estoque atualizado", "produto",
                () -> service.atualizarEstoque(sku, exigirInt(quantidade, "quantidade")));
    }

    // ============================================================
    // ROTAS — PREÇO / FORNECEDOR / FATURA
    // ============================================================

    /**
     * Preço chega como String (suporta vírgula pt-BR); conversão isolada em
     * {@link #converterPreco}. Emissor: {@code Params.withString("preco", "1290,50")}.
     */
    @PutMapping("preco/atualizar")
    public ResponseData atualizarPreco(@RouteVar("sku") String sku,
                                       @RouteVar("preco") String preco) {
        return escrita("Preço atualizado", "produto",
                () -> service.atualizarPreco(sku, converterPreco(preco)));
    }

    @PutMapping("fornecedor/atualizar")
    public ResponseData atualizarFornecedor(@RouteVar("sku") String sku,
                                            @RouteVar("fornecedor") String fornecedor) {
        return escrita("Fornecedor atualizado", "produto",
                () -> service.atualizarFornecedor(sku, fornecedor));
    }

    /**
     * Atualiza fatura do SKU. O emissor DEVE sempre enviar a chave "fatura"
     * (valor pode ser {@code null} quando não houver arquivo).
     * Nota: {@code byte[]} não é Collection — passa por referência, sem cópia defensiva.
     */
    @PutMapping("fatura/atualizar")
    public ResponseData atualizarFatura(@RouteVar("sku") String sku,
                                        @RouteVar("numero") String numero,
                                        @Payload("fatura") byte[] faturaCompra) {
        return escrita("Fatura atualizada", "produto",
                () -> service.atualizarFatura(sku, numero, faturaCompra));
    }

    // ============================================================
    // ROTAS — CONSULTAS (PRODUTOS)
    // ============================================================

    /** Emissor lê: {@code resposta.getDataList("produtos")} + {@code getDataInt("total")}. */
    @GetMapping("todos")
    public ResponseData todos() {
        return lista(() -> service.listarTodos(), "produtos");
    }

    /** Lista paginada. Emissor: {@code Params.withInt("pagina", 1).withInt("tamanho", 50)}. */
    @GetMapping("paginados")
    public ResponseData paginados(@RouteVar("pagina") Integer pagina,
                                  @RouteVar("tamanho") Integer tamanho) {
        return lista(() -> {
            if (pagina == null || pagina < 1 || tamanho == null || tamanho < 1) {
                throw new IllegalArgumentException("Página e tamanho devem ser >= 1");
            }
            return service.listarTodos(pagina, tamanho);
        }, "produtos");
    }

    /** Busca por SKU; erro semântico (com erros por campo) se inexistente. */
    @GetMapping("por/sku")
    public ResponseData buscarPorSku(@RouteVar("sku") String sku) {
        try {
            Optional<CatalogoProdutos> opt = service.buscarPorSku(sku);
            return opt.<ResponseData>map(p -> ResponseData.success()
                            .withMessage("Produto encontrado")
                            .withData("produto", p))
                    .orElseGet(() -> ResponseData.error("Produto não encontrado: " + sku)
                            .withError("sku", "SKU inexistente"));
        } catch (SQLException e) {
            return erroBanco(e);
        }
    }

    @GetMapping("por/tipo")
    public ResponseData porTipo(@RouteVar("tipo") String tipo) {
        return lista(() -> service.buscarPorTipo(tipo), "produtos");
    }

    @GetMapping("por/categoria")
    public ResponseData porCategoria(@RouteVar("categoria") String categoria) {
        return lista(() -> service.buscarPorCategoria(categoria), "produtos");
    }

    @GetMapping("por/marca")
    public ResponseData porMarca(@RouteVar("marca") String marca) {
        return lista(() -> service.buscarPorMarca(marca), "produtos");
    }

    @GetMapping("por/marca-e-modelo")
    public ResponseData porMarcaEModelo(@RouteVar("marca") String marca,
                                        @RouteVar("modelo") String modelo) {
        return lista(() -> service.buscarPorMarcaEModelo(marca, modelo), "produtos");
    }

    @GetMapping("por/fornecedor")
    public ResponseData porFornecedor(@RouteVar("fornecedor") String fornecedor) {
        return lista(() -> service.buscarPorFornecedor(fornecedor), "produtos");
    }

    /** Termo vazio → lista completa (contrato do service). */
    @GetMapping("buscar/por/termo")
    public ResponseData buscarPorTermo(@RouteVar("termo") String termo) {
        return lista(() -> service.buscarPorTermo(termo), "produtos");
    }

    // ============================================================
    // ROTAS — CONSULTAS (ESTOQUE)
    // ============================================================

    /** Emissor: {@code Params.withInt("limite", 10)}. */
    @GetMapping("estoque/baixo")
    public ResponseData estoqueBaixo(@RouteVar("limite") Integer limite) {
        return lista(() -> {
            if (limite == null || limite < 0) {
                throw new IllegalArgumentException("Limite deve ser >= 0");
            }
            return service.buscarEstoqueBaixo(limite);
        }, "produtos");
    }

    @GetMapping("estoque/sem")
    public ResponseData semEstoque() {
        return lista(service::buscarSemEstoque, "produtos");
    }

    @GetMapping("estoque/com")
    public ResponseData comEstoque() {
        return lista(service::buscarComEstoque, "produtos");
    }

    // ============================================================
    // ROTAS — DISTINCT VALUES (COMBOBOXES DA UI)
    // ============================================================

    @GetMapping("distinct/categorias")
    public ResponseData categorias()          { return lista(service::listarCategorias, "valores"); }

    @GetMapping("distinct/marcas")
    public ResponseData marcas()              { return lista(service::listarMarcas, "valores"); }

    @GetMapping("distinct/cores")
    public ResponseData cores()               { return lista(service::listarCores, "valores"); }

    @GetMapping("distinct/tipos")
    public ResponseData tipos()               { return lista(service::listarTipos, "valores"); }

    @GetMapping("distinct/fornecedores")
    public ResponseData fornecedores()        { return lista(service::listarFornecedores, "valores"); }

    @GetMapping("distinct/modelos")
    public ResponseData modelosPorMarca(@RouteVar("marca") String marca) {
        return lista(() -> service.listarModelosPorMarca(marca), "valores");
    }

    // ============================================================
    // ROTAS — ESTATÍSTICAS
    // ============================================================

    @GetMapping("contar/total")
    public ResponseData contarTotal()          { return valor(service::contarTotal, "total"); }

    @GetMapping("contar/ativos")
    public ResponseData contarAtivos()         { return valor(service::contarAtivos, "total"); }

    @GetMapping("contar/por/tipo")
    public ResponseData contarPorTipo(@RouteVar("tipo") String tipo) {
        return valor(() -> service.contarPorTipo(tipo), "total");
    }

    @GetMapping("contar/por/categoria")
    public ResponseData contarPorCategoria(@RouteVar("categoria") String categoria) {
        return valor(() -> service.contarPorCategoria(categoria), "total");
    }

    @GetMapping("contar/por/marca")
    public ResponseData contarPorMarca(@RouteVar("marca") String marca) {
        return valor(() -> service.contarPorMarca(marca), "total");
    }

    @GetMapping("valor-total-estoque")
    public ResponseData valorTotalEstoque()    { return valor(service::valorTotalEstoque, "valor"); }

    @GetMapping("valor-total-estoque-com-iva")
    public ResponseData valorTotalEstoqueComIva() { return valor(service::valorTotalEstoqueComIva, "valor"); }

    @GetMapping("stats/por/tipo")
    public ResponseData statsPorTipo()         { return valor(service::estatisticasPorTipo, "estatisticas"); }

    @GetMapping("stats/por/categoria")
    public ResponseData statsPorCategoria()    { return valor(service::estatisticasPorCategoria, "estatisticas"); }

    @GetMapping("stats/por/marca")
    public ResponseData statsPorMarca()        { return valor(service::estatisticasPorMarca, "estatisticas"); }

    @GetMapping("stats/por/fornecedor")
    public ResponseData statsPorFornecedor()   { return valor(service::estatisticasPorFornecedor, "estatisticas"); }

    @GetMapping("existe/por/sku")
    public ResponseData existePorSku(@RouteVar("sku") String sku) {
        return valor(() -> service.existePorSku(sku), "exists");
    }

    // ============================================================
    // HELPERS DE FRONTEIRA (ÚNICO PONTO DE CAPTURA)
    // ============================================================

    /** Ação que devolve um resultado (produto, lista, valor...). */
    @FunctionalInterface
    private interface Acao<T> {
        T executar() throws SQLException;
    }

    /** Ação de mutação SEM resultado útil — a mensagem é o retorno. */
    @FunctionalInterface
    private interface VoidAcao {
        void executar() throws SQLException;
    }

    /**
     * Mutação sem retorno util (excluir, ativar, desativar).
     * A mensagem vai para o canal CERTO: {@code withMessage} — o emissor lê
     * com {@code resposta.getMessage()}, nunca via mapa de dados.
     */
    private ResponseData acao(String mensagem, VoidAcao acao) {
        try {
            acao.executar();
            return ResponseData.success().withMessage(mensagem);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return erroNegocio(e);
        } catch (SQLException e) {
            return erroBanco(e);
        }
    }

    /**
     * Mutação com retorno (salvar, atualizar, estoque, preço...).
     * Entrega mensagem global + resultado sob a chave acordada no contrato.
     */
    private <T> ResponseData escrita(String mensagem, String chave, Acao<T> acao) {
        try {
            T resultado = acao.executar();
            return ResponseData.success()
                    .withMessage(mensagem)
                    .withData(chave, resultado);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return erroNegocio(e);
        } catch (SQLException e) {
            return erroBanco(e);
        }
    }

    /**
     * Consulta em lista. Entrega a lista sob a chave + "total" + mensagem
     * com a contagem — o emissor não precisa recomputar {@code size()}.
     */
    private ResponseData lista(Acao<List<?>> acao, String chave) {
        try {
            List<?> dados = acao.executar();
            return ResponseData.success()
                    .withMessage(dados.size() + " registro(s) encontrado(s)")
                    .withData(chave, dados)
                    .withData("total", dados.size());
        } catch (IllegalArgumentException | IllegalStateException e) {
            return erroNegocio(e);
        } catch (SQLException e) {
            return erroBanco(e);
        }
    }

    /** Consulta escalar (contagens, valores agregados, estatísticas). */
    private <T> ResponseData valor(Acao<T> acao, String chave) {
        try {
            return ResponseData.success().withData(chave, acao.executar());
        } catch (IllegalArgumentException | IllegalStateException e) {
            return erroNegocio(e);
        } catch (SQLException e) {
            return erroBanco(e);
        }
    }

    /** Regra de negócio violada — warn no log, erro semântico com campo. */
    private ResponseData erroNegocio(RuntimeException e) {
        logger.warn("⚠️ Regra de negócio violada: {}", e.getMessage());
        return ResponseData.error(e.getMessage())
                .withError("negocio", e.getMessage());
    }

    /** Erro de banco — error no log com stacktrace completo. */
    private ResponseData erroBanco(SQLException e) {
        logger.error("❌ Erro de banco de dados: {}", e.getMessage(), e);
        return ResponseData.error("Erro de banco de dados: " + e.getMessage())
                .withError("banco", e.getMessage());
    }

    /** Garante unboxing seguro de @RouteVar Integer → int (autoboxing cobre o resto). */
    private int exigirInt(Integer valor, String campo) {
        if (valor == null) {
            throw new IllegalArgumentException("Parâmetro obrigatório: " + campo);
        }
        return valor;
    }

    /**
     * Busca combinando múltiplos filtros OPCIONAIS. Parâmetros ausentes ou
     * null são ignorados pelo service (comportamento delegado).
     *
     * <p>Emissor — envie apenas os filtros selecionados:
     * {@code Params.withString("marca", m).with("ativo", Boolean.TRUE)}</p>
     */
    @GetMapping("buscar/com-filtros")
    public ResponseData buscarComFiltros(@RouteVar("tipo") String tipo,
                                         @RouteVar("categoria") String categoria,
                                         @RouteVar("marca") String marca,
                                         @RouteVar("modelo") String modelo,
                                         @RouteVar("cor") String cor,
                                         @RouteVar("ativo") Boolean ativo) {
        return lista(() -> service.buscarComFiltros(tipo, categoria, marca, modelo, cor, ativo),
                "produtos");
    }

    /** Aceita vírgula decimal (pt-BR): "1290,50" → BigDecimal. */
    private BigDecimal converterPreco(String bruto) {
        if (bruto == null || bruto.isBlank()) {
            throw new IllegalArgumentException("Preço não informado");
        }
        try {
            return new BigDecimal(bruto.trim().replace(",", "."));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Preço inválido: '" + bruto + "'");
        }
    }
}