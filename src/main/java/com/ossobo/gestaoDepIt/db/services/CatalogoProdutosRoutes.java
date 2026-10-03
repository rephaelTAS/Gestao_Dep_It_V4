package com.ossobo.gestaoDepIt.db.services;

import com.ossobo.gestaoDepIt.db.enums.TipoProduto;
import com.ossobo.gestaoDepIt.db.models.CatalogoProdutos;
import com.ossobo.winterfx.anotations.*;
import com.ossobo.winterfx.router.model.ResponseData;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * CatalogoProdutosRoutes v1.5
 *
 * v1.5 — Rota nova `atualizar/por/sku`:
 *        - Resolve SKU → id na fronteira. Cliente envia o record SEM se
 *          preocupar com o UUID (chave de sync). SKU é imutável em edição.
 *
 * v1.4 — Fatura única passa a ser SÓ o número (fornecedor é informativo).
 * v1.3 — Rota `existe/por/fatura` (versão anterior com fornecedor).
 * v1.2 — Alinhado ao Service v2.2 / Repository v2.4 / Model v3.0.
 */
@Component
@RequestMapping("catalogo-produtos/service")
public class CatalogoProdutosRoutes {

    private static final System.Logger logger = System.getLogger(CatalogoProdutosRoutes.class.getName());

    @Inject
    private CatalogoProdutosService service;

    // ============================================================
    // ROTAS — CRUD
    // ============================================================

    @PutMapping("salvar")
    public ResponseData salvar(@Payload("produto") CatalogoProdutos produto) {
        return escrita("Produto salvo com sucesso", "produto",
                () -> service.salvar(produto));
    }

    /**
     * Atualiza por ID (payload deve trazer o id preenchido).
     * Mantida para compatibilidade reversa. Prefira `atualizar/por/sku`.
     */
    @PutMapping("atualizar")
    public ResponseData atualizar(@Payload("produto") CatalogoProdutos produto) {
        return escrita("Produto atualizado com sucesso", "produto",
                () -> service.atualizar(produto));
    }

    /**
     * Atualiza resolvendo SKU → id na fronteira.
     *
     * O cliente envia o record SEM se preocupar com o UUID (chave de sync).
     * O SKU é a chave de negócio estável — nasce uma vez, nunca muda.
     * O id interno é resolvido aqui e injetado no record antes do service.
     */
    @PutMapping("atualizar/por/sku")
    public ResponseData atualizarPorSku(@Payload("produto") CatalogoProdutos produto) {
        return escrita("Produto atualizado com sucesso", "produto", () -> {
            if (produto == null) throw new IllegalArgumentException("Produto inválido");
            if (produto.sku() == null || produto.sku().isBlank()) {
                throw new IllegalArgumentException("SKU é obrigatório para atualização");
            }
            String idAtual = idDeSku(produto.sku());
            return service.atualizar(produto.comId(idAtual));
        });
    }

    @DeleteMapping("deletar/por/sku")
    public ResponseData excluir(@RouteVar("sku") String sku) {
        return acao("SKU " + sku + " excluído", () -> {
            String id = idDeSku(sku);
            service.excluir(id);
        });
    }

    @DeleteMapping("permanente/por/sku")
    public ResponseData excluirPermanentemente(@RouteVar("sku") String sku) {
        return acao("SKU " + sku + " excluído permanentemente", () -> {
            String id = idDeSku(sku);
            service.excluirPermanentemente(id);
        });
    }

    @PutMapping("ativar")
    public ResponseData ativar(@RouteVar("sku") String sku) {
        return acao("Produto ativado", () -> service.ativar(idDeSku(sku)));
    }

    @PutMapping("desativar")
    public ResponseData desativar(@RouteVar("sku") String sku) {
        return acao("Produto desativado", () -> service.desativar(idDeSku(sku)));
    }

    // ============================================================
    // ROTAS — ESTOQUE
    // ============================================================

    @PutMapping("estoque/adicionar")
    public ResponseData adicionarEstoque(@RouteVar("sku") String sku,
                                         @RouteVar("quantidade") Integer quantidade) {
        return escrita("Estoque adicionado", "produto",
                () -> service.adicionarEstoque(idDeSku(sku), exigirInt(quantidade, "quantidade")));
    }

    @PutMapping("estoque/remover")
    public ResponseData removerEstoque(@RouteVar("sku") String sku,
                                       @RouteVar("quantidade") Integer quantidade) {
        return escrita("Estoque removido", "produto",
                () -> service.removerEstoque(idDeSku(sku), exigirInt(quantidade, "quantidade")));
    }

    @PutMapping("estoque/atualizar")
    public ResponseData atualizarEstoque(@RouteVar("sku") String sku,
                                         @RouteVar("quantidade") Integer quantidade) {
        return escrita("Estoque atualizado", "produto",
                () -> service.atualizarEstoque(idDeSku(sku), exigirInt(quantidade, "quantidade")));
    }

    // ============================================================
    // ROTAS — PREÇO / FORNECEDOR / FATURA
    // ============================================================

    @PutMapping("preco/atualizar")
    public ResponseData atualizarPreco(@RouteVar("sku") String sku,
                                       @RouteVar("preco") String preco,
                                       @RouteVar("iva") String iva) {
        return escrita("Preço atualizado", "produto", () -> {
            int centavos = converterPrecoCentavos(preco);
            int bp = converterIvaBp(iva);
            return service.atualizarPreco(idDeSku(sku), centavos, bp);
        });
    }

    @PutMapping("fornecedor/atualizar")
    public ResponseData atualizarFornecedor(@RouteVar("sku") String sku,
                                            @RouteVar("fornecedor") String fornecedor) {
        return escrita("Fornecedor atualizado", "produto",
                () -> service.atualizarFornecedor(idDeSku(sku), fornecedor));
    }

    @PutMapping("fatura/atualizar")
    public ResponseData atualizarFatura(@RouteVar("sku") String sku,
                                        @RouteVar("numero") String numero,
                                        @Payload("fatura") byte[] faturaCompra) {
        return escrita("Fatura atualizada", "produto",
                () -> service.atualizarFatura(idDeSku(sku), numero, faturaCompra));
    }

    // ============================================================
    // ROTAS — CONSULTAS
    // ============================================================

    @GetMapping("todos")
    public ResponseData todos() {
        return lista(() -> service.listarTodos(), "produtos");
    }

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

    @GetMapping("por/id")
    public ResponseData buscarPorId(@RouteVar("id") String id) {
        try {
            Optional<CatalogoProdutos> opt = service.buscarPorId(id);
            return opt.<ResponseData>map(p -> ResponseData.success()
                            .withMessage("Produto encontrado")
                            .withData("produto", p))
                    .orElseGet(() -> ResponseData.error("Produto não encontrado: " + id)
                            .withError("id", "ID inexistente"));
        } catch (SQLException e) {
            return erroBanco(e);
        }
    }

    @GetMapping("por/tipo")
    public ResponseData porTipo(@RouteVar("tipo") String tipo) {
        return lista(() -> service.buscarPorTipo(TipoProduto.fromString(tipo)), "produtos");
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

    @GetMapping("buscar/por/termo")
    public ResponseData buscarPorTermo(@RouteVar("termo") String termo) {
        return lista(() -> service.buscarPorTermo(termo), "produtos");
    }

    // ============================================================
    // ROTAS — ESTOQUE (consultas)
    // ============================================================

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
    public ResponseData semEstoque() { return lista(service::buscarSemEstoque, "produtos"); }

    @GetMapping("estoque/com")
    public ResponseData comEstoque() { return lista(service::buscarComEstoque, "produtos"); }

    // ============================================================
    // ROTAS — DISTINCT
    // ============================================================

    @GetMapping("distinct/categorias")   public ResponseData categorias()    { return lista(service::listarCategorias, "valores"); }
    @GetMapping("distinct/marcas")       public ResponseData marcas()        { return lista(service::listarMarcas, "valores"); }
    @GetMapping("distinct/cores")        public ResponseData cores()         { return lista(service::listarCores, "valores"); }
    @GetMapping("distinct/tipos")        public ResponseData tipos()         { return lista(service::listarTipos, "valores"); }
    @GetMapping("distinct/fornecedores") public ResponseData fornecedores()  { return lista(service::listarFornecedores, "valores"); }

    @GetMapping("distinct/modelos")
    public ResponseData modelosPorMarca(@RouteVar("marca") String marca) {
        return lista(() -> service.listarModelosPorMarca(marca), "valores");
    }

    // ============================================================
    // ROTAS — VERIFICAÇÃO DE FATURA (só o número)
    // ============================================================

    /**
     * Verifica se o número de fatura já está registrado.
     * Feedback imediato na UI antes de tentar salvar.
     * Chave: APENAS "numero" — fornecedor é informativo, não entra na regra.
     */
    @GetMapping("existe/por/fatura")
    public ResponseData existePorFatura(@RouteVar("numero") String numero) {
        return valor(() -> service.existePorFatura(numero), "exists");
    }

    // ============================================================
    // ROTAS — ESTATÍSTICAS
    // ============================================================

    @GetMapping("contar/total")   public ResponseData contarTotal()  { return valor(service::contarTotal, "total"); }
    @GetMapping("contar/ativos")  public ResponseData contarAtivos() { return valor(service::contarAtivos, "total"); }

    @GetMapping("contar/por/tipo")
    public ResponseData contarPorTipo(@RouteVar("tipo") String tipo) {
        return valor(() -> service.contarPorTipo(TipoProduto.fromString(tipo)), "total");
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
    public ResponseData valorTotalEstoque() {
        return valor(service::valorTotalEstoqueCentavos, "valor_centavos");
    }

    @GetMapping("valor-total-estoque-com-iva")
    public ResponseData valorTotalEstoqueComIva() {
        return valor(service::valorTotalEstoqueComIvaCentavos, "valor_centavos");
    }

    @GetMapping("stats/por/tipo")        public ResponseData statsPorTipo()       { return valor(service::estatisticasPorTipo, "estatisticas"); }
    @GetMapping("stats/por/categoria")   public ResponseData statsPorCategoria()  { return valor(service::estatisticasPorCategoria, "estatisticas"); }
    @GetMapping("stats/por/marca")       public ResponseData statsPorMarca()      { return valor(service::estatisticasPorMarca, "estatisticas"); }
    @GetMapping("stats/por/fornecedor")  public ResponseData statsPorFornecedor() { return valor(service::estatisticasPorFornecedor, "estatisticas"); }

    @GetMapping("existe/por/sku")
    public ResponseData existePorSku(@RouteVar("sku") String sku) {
        return valor(() -> service.existePorSku(sku), "exists");
    }

    // ============================================================
    // HELPERS DE FRONTEIRA
    // ============================================================

    @FunctionalInterface
    private interface Acao<T> { T executar() throws SQLException; }

    @FunctionalInterface
    private interface VoidAcao { void executar() throws SQLException; }

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

    private <T> ResponseData escrita(String mensagem, String chave, Acao<T> acao) {
        try {
            T resultado = acao.executar();
            return ResponseData.success().withMessage(mensagem).withData(chave, resultado);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return erroNegocio(e);
        } catch (SQLException e) {
            return erroBanco(e);
        }
    }

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

    private <T> ResponseData valor(Acao<T> acao, String chave) {
        try {
            return ResponseData.success().withData(chave, acao.executar());
        } catch (IllegalArgumentException | IllegalStateException e) {
            return erroNegocio(e);
        } catch (SQLException e) {
            return erroBanco(e);
        }
    }

    private ResponseData erroNegocio(RuntimeException e) {
        // System.Logger usa MessageFormat — {0}, não {} (SLF4J).
        logger.log(System.Logger.Level.WARNING, "⚠️ Regra de negócio violada: {0}", e.getMessage());
        String msg = (e.getMessage() == null || e.getMessage().isBlank())
                ? e.getClass().getSimpleName()
                : e.getMessage();
        return ResponseData.error(msg).withError("negocio", msg);
    }

    private ResponseData erroBanco(SQLException e) {
        logger.log(System.Logger.Level.ERROR, "❌ Erro de banco de dados: {0}", e.getMessage(), e);
        return ResponseData.error("Erro de banco de dados: " + e.getMessage())
                .withError("banco", e.getMessage());
    }

    private int exigirInt(Integer valor, String campo) {
        if (valor == null) throw new IllegalArgumentException("Parâmetro obrigatório: " + campo);
        return valor;
    }

    private String idDeSku(String sku) throws SQLException {
        return service.buscarPorSku(sku)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado: " + sku))
                .id();
    }

    private int converterPrecoCentavos(String bruto) {
        if (bruto == null || bruto.isBlank()) {
            throw new IllegalArgumentException("Preço não informado");
        }
        try {
            String limpo = bruto.trim().replace(",", ".");
            java.math.BigDecimal valor = new java.math.BigDecimal(limpo);
            return valor.multiply(java.math.BigDecimal.valueOf(100)).intValueExact();
        } catch (NumberFormatException | ArithmeticException e) {
            throw new IllegalArgumentException("Preço inválido: '" + bruto + "'");
        }
    }

    private int converterIvaBp(String bruto) {
        if (bruto == null || bruto.isBlank()) return 0;
        try {
            String limpo = bruto.trim().replace(",", ".");
            java.math.BigDecimal valor = new java.math.BigDecimal(limpo);
            return valor.multiply(java.math.BigDecimal.valueOf(100)).intValueExact();
        } catch (NumberFormatException | ArithmeticException e) {
            throw new IllegalArgumentException("IVA inválido: '" + bruto + "'");
        }
    }
}