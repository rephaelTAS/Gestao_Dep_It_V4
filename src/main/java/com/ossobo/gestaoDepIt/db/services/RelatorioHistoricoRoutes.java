package com.ossobo.gestaoDepIt.db.services;

import com.ossobo.gestaoDepIt.db.relatorios.RelatorioHistorico;
import com.ossobo.winterfx.anotations.*;
import com.ossobo.winterfx.router.model.ResponseData;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * RelatorioHistoricoRoutes v1.0
 *
 * Fronteira de Internal Routing do ledger de relatórios gerados.
 * Contrato de payload:
 *   PUT registrar → "titulo" + "tabelas" (List<String>) + "filtros" (JSON String)
 */
@Component
@RequestMapping("relatorio-historico/service")
public class RelatorioHistoricoRoutes {

    private static final System.Logger logger = System.getLogger(RelatorioHistoricoRoutes.class.getName());

    @Inject
    private RelatorioHistoricoService service;

    @PutMapping("registrar")
    public ResponseData registrar(@RouteVar("titulo") String titulo,
                                  @Payload("tabelas") List<String> tabelas,
                                  @RouteVar("filtros") String filtrosJson) {
        return escrita(() -> service.registrar(titulo, tabelas, filtrosJson), "relatorio");
    }

    @GetMapping("recentes")
    public ResponseData recentes(@RouteVar("limite") Integer limite) {
        return lista(() -> service.listarRecentes(limite == null ? 20 : limite), "relatorios");
    }

    @GetMapping("total")
    public ResponseData total() { return valor(service::contarTotal, "total"); }

    @GetMapping("total/hoje")
    public ResponseData totalHoje() { return valor(service::contarHoje, "total"); }

    @GetMapping("total/semana")
    public ResponseData totalSemana() { return valor(service::contarSemana, "total"); }

    @GetMapping("media-por-dia")
    public ResponseData mediaPorDia() { return valor(service::mediaPorDia, "media"); }

    @GetMapping("contagem-por-combinacao")
    public ResponseData contagemPorCombinacao() { return valor(service::contarPorCombinacao, "contagem"); }

    // ============================================================
    // HELPERS DE FRONTEIRA
    // ============================================================

    @FunctionalInterface
    private interface Acao<T> { T executar() throws SQLException; }

    private <T> ResponseData escrita(Acao<T> acao, String chave) {
        try {
            return ResponseData.success().withData(chave, acao.executar());
        } catch (IllegalArgumentException | IllegalStateException e) {
            logger.log(System.Logger.Level.WARNING, "⚠️ Regra de negócio violada: {}", e.getMessage());
            return ResponseData.error(e.getMessage()).withError("negocio", e.getMessage());
        } catch (SQLException e) {
            return erroBanco(e);
        }
    }

    private ResponseData lista(Acao<List<?>> acao, String chave) {
        try {
            List<?> dados = acao.executar();
            return ResponseData.success().withData(chave, dados).withData("total", dados.size());
        } catch (SQLException e) { return erroBanco(e); }
    }

    private <T> ResponseData valor(Acao<T> acao, String chave) {
        try {
            return ResponseData.success().withData(chave, acao.executar());
        } catch (SQLException e) { return erroBanco(e); }
    }

    private ResponseData erroBanco(SQLException e) {
        logger.log(System.Logger.Level.ERROR, "❌ Erro de banco: {}", e.getMessage(), e);
        return ResponseData.error("Erro de banco de dados: " + e.getMessage())
                .withError("banco", e.getMessage());
    }
}