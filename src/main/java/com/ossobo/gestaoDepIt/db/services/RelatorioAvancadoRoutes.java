package com.ossobo.gestaoDepIt.db.services;

import com.ossobo.gestaoDepIt.db.relatorios.*;
import com.ossobo.winterfx.anotations.*;
import com.ossobo.winterfx.router.model.Params;
import com.ossobo.winterfx.router.model.ResponseData;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * RelatorioAvancadoRoutes v1.1
 *
 * Canal do relatório AVANÇADO (Lego): tabela base + JOINs + colunas + filtros.
 *
 * v1.1 — executar() muda de @GetMapping para @PutMapping:
 *        o modelo viaja como payload (@Payload("modelo")), e o resolver
 *        do WinterFX só processa @Payload em canais de escrita.
 *        Também corrige o placeholder do log ({0} em vez de {}).
 */
@Component
@RequestMapping("relatorio-avancado/service")
public class RelatorioAvancadoRoutes {

    private static final System.Logger logger =
            System.getLogger(RelatorioAvancadoRoutes.class.getName());

    @Inject
    private RelatorioBuilderService builder;

    @Inject
    private RelatorioExportService export;

    // ============================================================
    // EXECUTAR
    // ============================================================

    @PutMapping("executar")
    public ResponseData executar(@Payload("modelo") RelatorioModelo modelo,
                                 @RouteVar("limite") Integer limite) {
        try {
            RelatorioResultado r = builder.consultarModelo(
                    modelo, limite == null ? 0 : limite);

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
    // METADADOS — grafo + colunas
    // ============================================================

    @GetMapping("grafo")
    public ResponseData grafo() {
        return ResponseData.success()
                .withData("aliases", RelatorioCatalogoGrafo.aliasesValidos())
                .withData("arestas", RelatorioCatalogoGrafo.arestas());
    }

    @GetMapping("vizinhos")
    public ResponseData vizinhos(@RouteVar("alias") String alias) {
        return ResponseData.success()
                .withData("vizinhos", RelatorioCatalogoGrafo.vizinhos(alias));
    }

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
    public ResponseData exportarXlsx(@Payload("modelo") RelatorioModelo modelo) {
        try {
            RelatorioResultado r = builder.consultarModelo(modelo, 0);
            byte[] xlsx = export.exportar(
                    modelo.tabelaBase() + "_avancado", r);

            return ResponseData.success()
                    .withMessage("XLSX gerado (" + r.total() + " linhas)")
                    .withData("arquivo", xlsx)
                    .withData("nomeSugerido",
                            export.nomeArquivoSugerido("relatorio_avancado"));
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
        logger.log(System.Logger.Level.WARNING, "⚠️ Regra de negócio violada: {0}", e.getMessage());
        return ResponseData.error(e.getMessage()).withError("negocio", e.getMessage());
    }

    private ResponseData erroBanco(SQLException e) {
        logger.log(System.Logger.Level.ERROR, "❌ Erro de banco: {0}", e.getMessage(), e);
        return ResponseData.error("Erro de banco de dados: " + e.getMessage())
                .withError("banco", e.getMessage());
    }
}