package com.ossobo.gestaoDepIt.db.services;

import com.ossobo.gestaoDepIt.db.relatorios.RelatorioModelo;
import com.ossobo.winterfx.anotations.*;
import com.ossobo.winterfx.router.model.ResponseData;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * RelatorioModeloRoutes v1.0
 *
 * Fronteira de Internal Routing dos modelos LEGO.
 * Contrato de payload:
 *   "modelo" (RelatorioModelo) — com id, nome, tabelaBase, colunasBase, joins, filtrosJson.
 */
@Component
@RequestMapping("relatorio-modelo/service")
public class RelatorioModeloRoutes {

    private static final System.Logger logger = System.getLogger(RelatorioModeloRoutes.class.getName());

    @Inject
    private RelatorioModeloService service;

    @PutMapping("salvar")
    public ResponseData salvar(@Payload("modelo") RelatorioModelo modelo) {
        return escrita(() -> service.salvar(modelo), "modelo");
    }

    @PutMapping("atualizar")
    public ResponseData atualizar(@Payload("modelo") RelatorioModelo modelo) {
        return escrita(() -> service.atualizar(modelo), "modelo");
    }

    @DeleteMapping("deletar/por/id")
    public ResponseData excluir(@RouteVar("id") String id) {
        return escrita(() -> { service.excluir(id); return "Modelo excluído"; }, "mensagem");
    }

    @GetMapping("todos")
    public ResponseData todos() { return lista(service::listarTodos, "modelos"); }

    @GetMapping("por/id")
    public ResponseData porId(@RouteVar("id") String id) {
        return optional(() -> service.buscarPorId(id), "modelo", "Modelo não encontrado: " + id);
    }

    @GetMapping("por/nome")
    public ResponseData porNome(@RouteVar("nome") String nome) {
        return optional(() -> service.buscarPorNome(nome), "modelo", "Modelo não encontrado: " + nome);
    }

    @GetMapping("grafo/aliases")
    public ResponseData aliases() {
        return ResponseData.success()
                .withData("aliases", com.ossobo.gestaoDepIt.db.relatorios.RelatorioCatalogoGrafo.aliasesValidos());
    }

    @GetMapping("grafo/vizinhos")
    public ResponseData vizinhos(@RouteVar("alias") String alias) {
        return ResponseData.success()
                .withData("vizinhos", com.ossobo.gestaoDepIt.db.relatorios.RelatorioCatalogoGrafo.vizinhos(alias));
    }

    @GetMapping("grafo/arestas")
    public ResponseData arestas() {
        return ResponseData.success()
                .withData("arestas", com.ossobo.gestaoDepIt.db.relatorios.RelatorioCatalogoGrafo.arestas());
    }

    // ============================================================
    // HELPERS
    // ============================================================

    @FunctionalInterface
    private interface Acao<T> { T executar() throws SQLException; }

    private <T> ResponseData escrita(Acao<T> acao, String chave) {
        try {
            return ResponseData.success().withData(chave, acao.executar());
        } catch (IllegalArgumentException | IllegalStateException e) {
            logger.log(System.Logger.Level.WARNING, "⚠️ Regra de negócio violada: {}", e.getMessage());
            return ResponseData.error(e.getMessage()).withError("negocio", e.getMessage());
        } catch (SQLException e) { return erroBanco(e); }
    }

    private ResponseData lista(Acao<List<?>> acao, String chave) {
        try {
            List<?> dados = acao.executar();
            return ResponseData.success().withData(chave, dados).withData("total", dados.size());
        } catch (SQLException e) { return erroBanco(e); }
    }

    private <T> ResponseData optional(Acao<Optional<T>> acao, String chave, String msgVazio) {
        try {
            return acao.executar()
                    .<ResponseData>map(v -> ResponseData.success().withData(chave, v))
                    .orElseGet(() -> ResponseData.error(msgVazio).withError(chave, "não encontrado"));
        } catch (SQLException e) { return erroBanco(e); }
    }

    private ResponseData erroBanco(SQLException e) {
        logger.log(System.Logger.Level.ERROR, "❌ Erro de banco: {0}", e.getMessage(), e);
        return ResponseData.error("Erro de banco de dados: " + e.getMessage())
                .withError("banco", e.getMessage());
    }
}