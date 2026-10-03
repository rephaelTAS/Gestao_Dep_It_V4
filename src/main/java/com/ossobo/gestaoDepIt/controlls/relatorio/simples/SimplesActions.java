package com.ossobo.gestaoDepIt.controlls.relatorio.simples;

import com.ossobo.winterfx.router.Rotas;
import com.ossobo.winterfx.router.model.Params;
import com.ossobo.winterfx.router.model.ResponseData;

import java.util.List;
import java.util.Map;

/**
 * SimplesActions v1.0
 *
 * Encapsula chamadas às rotas do relatório simples.
 * Retorna dados tipados ou vazio — sem lançar exceção para a UI.
 */
public final class SimplesActions {

    private static final System.Logger logger =
            System.getLogger(SimplesActions.class.getName());

    private SimplesActions() {}

    // ============================================================
    // GRAFO
    // ============================================================

    @SuppressWarnings("unchecked")
    public static List<String> aliasesDisponiveis() {
        try {
            Object r = Rotas.get("relatorio-avancado/service/grafo");
            if (r instanceof ResponseData rd && rd.isSuccess()) {
                Object a = rd.getData().get("aliases");
                if (a instanceof java.util.Set<?> s) {
                    return s.stream().map(String::valueOf).sorted().toList();
                }
                if (a instanceof List<?> l) {
                    return l.stream().map(String::valueOf).toList();
                }
            }
        } catch (Exception e) {
            logger.log(System.Logger.Level.WARNING, "Falha ao buscar aliases: {0}", e.getMessage());
        }
        return List.of();
    }

    // ============================================================
    // COLUNAS
    // ============================================================

    @SuppressWarnings("unchecked")
    public static List<String> colunasDe(String alias) {
        try {
            Object r = Rotas.get("relatorio-simples/service/colunas",
                    Params.with("alias", alias));
            if (r instanceof ResponseData rd && rd.isSuccess()) {
                Object c = rd.getData().get("colunas");
                if (c instanceof List<?> l) return l.stream().map(String::valueOf).toList();
            }
        } catch (Exception e) {
            logger.log(System.Logger.Level.WARNING, "Falha ao buscar colunas: {0}", e.getMessage());
        }
        return List.of();
    }

    // ============================================================
    // EXECUTAR
    // ============================================================

    @SuppressWarnings("unchecked")
    public static RelatorioSimplesResultado executar(String alias,
                                                     List<String> colunas,
                                                     Map<String, Object> filtros,
                                                     int limite) {
        try {
            Params p = Params.with("alias", alias)
                    .and("colunas", colunas)
                    .and("filtros", filtros)
                    .and("limite", limite);

            Object r = Rotas.get("relatorio-simples/service/executar", p);
            if (r instanceof ResponseData rd && rd.isSuccess()) {
                Object cols = rd.getData().get("colunas");
                Object linhas = rd.getData().get("resultado");
                if (linhas instanceof com.ossobo.gestaoDepIt.db.relatorios.RelatorioResultado res) {
                    return new RelatorioSimplesResultado(res.colunas(), res.linhas(), res.total());
                }
            }
        } catch (Exception e) {
            logger.log(System.Logger.Level.ERROR, "Falha ao executar: {0}", e.getMessage(), e);
        }
        return new RelatorioSimplesResultado(List.of(), List.of(), 0);
    }

    /** Envelope simples para a UI — evita dependência direta do record do db. */
    public record RelatorioSimplesResultado(
            List<String> colunas,
            List<Map<String, Object>> linhas,
            int total) {}
}