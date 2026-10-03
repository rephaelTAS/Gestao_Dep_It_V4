package com.ossobo.gestaoDepIt.db.services;

import com.ossobo.gestaoDepIt.db.relatorios.RelatorioResultado;
import com.ossobo.winterfx.anotations.*;
import com.ossobo.winterfx.router.model.ResponseData;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * RelatorioSimplesRoutes v1.0
 *
 * Canal do relatório SIMPLES: uma tabela, colunas escolhidas, filtros
 * opcionais. Sem JOIN. Devolve RelatorioResultado.
 *
 * Contratos:
 *   GET  simples/executar         → alias + colunas (List<String>) + filtros (Map)
 *   GET  simples/colunas/{alias}  → lista de colunas válidas
 *   PUT  simples/exportar-xlsx    → alias + colunas + filtros → byte[] do xlsx
 *
 * "colunas" vazio = todas as colunas não-auditórias da tabela.
 */
@Component
@RequestMapping("relatorio-simples/service")
public class RelatorioSimplesRoutes {

    private static final System.Logger logger =
            System.getLogger(RelatorioSimplesRoutes.class.getName());

    @Inject
    private RelatorioBuilderService builder;

    @Inject
    private RelatorioExportService export;

    // ============================================================
    // EXECUTAR
    // ============================================================

    @GetMapping("executar")
    public ResponseData executar(@RouteVar("alias") String alias,
                                 @Payload("colunas") List<String> colunas,
                                 @Payload("filtros") Map<String, Object> filtros,
                                 @RouteVar("limite") Integer limite) {
        try {
            RelatorioResultado r = builder.consultarSimples(
                    alias,
                    colunas == null ? List.of() : colunas,
                    filtros == null ? Map.of() : filtros,
                    limite == null ? 0 : limite);

            return ResponseData.success()
                    .withMessage(r.total() + " registro(s)")
                    .withData("resultado", r)
                    .withData("colunas", r.colunas())
                    .withData("total", r.total());
        } catch (IllegalArgumentException | IllegalStateException e) {
            return erroNegocio(e);
        } catch (SQLException e) {
            return erroBanco(e);
        }
    }

    // ============================================================
    // METADADOS
    // ============================================================

    @GetMapping("colunas")
    public ResponseData colunas(@RouteVar("alias") String alias) {
        try {
            return ResponseData.success().withData("colunas", builder.colunasDe(alias));
        } catch (SQLException e) {
            return erroBanco(e);
        }
    }

    // ============================================================
    // EXPORTAR
    // ============================================================

    @PutMapping("exportar-xlsx")
    public ResponseData exportarXlsx(@RouteVar("alias") String alias,
                                     @Payload("colunas") List<String> colunas,
                                     @Payload("filtros") Map<String, Object> filtros) {
        try {
            RelatorioResultado r = builder.consultarSimples(
                    alias,
                    colunas == null ? List.of() : colunas,
                    filtros == null ? Map.of() : filtros,
                    0);

            byte[] xlsx = export.exportar(alias, r);
            return ResponseData.success()
                    .withMessage("XLSX gerado (" + r.total() + " linhas)")
                    .withData("arquivo", xlsx)
                    .withData("nomeSugerido", export.nomeArquivoSugerido("relatorio_" + alias));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return erroNegocio(e);
        } catch (SQLException e) {
            return erroBanco(e);
        } catch (IOException e) {
            logger.log(System.Logger.Level.ERROR, "❌ Falha ao gerar XLSX: {0}", e.getMessage(), e);
            return ResponseData.error("Falha ao gerar XLSX: " + e.getMessage());
        }
    }

    // ============================================================
    // HELPERS
    // ============================================================

    private ResponseData erroNegocio(RuntimeException e) {
        logger.log(System.Logger.Level.WARNING, "⚠️ Regra de negócio violada: {}", e.getMessage());
        return ResponseData.error(e.getMessage()).withError("negocio", e.getMessage());
    }

    private ResponseData erroBanco(SQLException e) {
        logger.log(System.Logger.Level.ERROR, "❌ Erro de banco: {}", e.getMessage(), e);
        return ResponseData.error("Erro de banco de dados: " + e.getMessage())
                .withError("banco", e.getMessage());
    }
}