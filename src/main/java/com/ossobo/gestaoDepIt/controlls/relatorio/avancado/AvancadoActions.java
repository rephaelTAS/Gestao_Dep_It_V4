package com.ossobo.gestaoDepIt.controlls.relatorio.avancado;

import com.ossobo.gestaoDepIt.db.relatorios.RelatorioModelo;
import com.ossobo.winterfx.router.Rotas;
import com.ossobo.winterfx.router.model.Params;
import com.ossobo.winterfx.router.model.ResponseData;

import java.util.List;
import java.util.Map;

/**
 * AvancadoActions v1.2
 *
 * Encapsula chamadas às rotas do relatório avançado.
 *
 * v1.2 — executar(): alinhado ao RelatorioAvancadoRoutes v1.1.
 *        Rota virou @PutMapping e o @Payload("modelo") casa pela chave,
 *        não pelo tipo — enviamos Params.with("modelo", modelo) e
 *        despachamos via Rotas.put.
 */
public final class AvancadoActions {

    private static final System.Logger logger =
            System.getLogger(AvancadoActions.class.getName());

    private AvancadoActions() {}

    // ============================================================
    // GRAFO
    // ============================================================

    public static List<String> aliasesDisponiveis() {
        try {
            Object r = Rotas.get("relatorio-avancado/service/grafo");
            if (r instanceof ResponseData rd && rd.isSuccess()) {
                Object a = rd.getData().get("aliases");
                if (a instanceof java.util.Set<?> s)
                    return s.stream().map(String::valueOf).sorted().toList();
                if (a instanceof List<?> l)
                    return l.stream().map(String::valueOf).toList();
            }
        } catch (Exception e) {
            logger.log(System.Logger.Level.WARNING, "Falha ao buscar aliases: {0}", e.getMessage());
        }
        return List.of();
    }

    public static List<String> vizinhos(String alias) {
        try {
            Object r = Rotas.get("relatorio-avancado/service/vizinhos",
                    Params.with("alias", alias));
            if (r instanceof ResponseData rd && rd.isSuccess()) {
                Object v = rd.getData().get("vizinhos");
                if (v instanceof List<?> l) return l.stream().map(String::valueOf).toList();
            }
        } catch (Exception e) {
            logger.log(System.Logger.Level.WARNING, "Falha ao buscar vizinhos: {0}", e.getMessage());
        }
        return List.of();
    }

    // ============================================================
    // COLUNAS
    // ============================================================

    public static List<String> colunasDe(String alias) {
        try {
            Object r = Rotas.get("relatorio-avancado/service/colunas",
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

    /**
     * Executa a rota avançada.
     *
     * Handler: {@code @PutMapping("executar")} com
     *          {@code @Payload("modelo") RelatorioModelo modelo} e
     *          {@code @RouteVar("limite") Integer limite}.
     * Enviamos:
     *   - chave "modelo" → o RelatorioModelo (casamento por chave, não por tipo)
     *   - chave "limite" → route var (autoboxing para Integer)
     */
    public static Resultado executar(RelatorioModelo modelo, int limite) {
        if (modelo == null) return new Resultado(List.of(), List.of(), 0, "");
        try {
            Params p = Params.with("modelo", modelo)
                    .and("limite", limite);
            Object r = Rotas.put("relatorio-avancado/service/executar", p);

            if (r instanceof ResponseData rd) {
                if (rd.isSuccess()) {
                    Object resultado = rd.getData().get("resultado");
                    if (resultado instanceof com.ossobo.gestaoDepIt.db.relatorios.RelatorioResultado res) {
                        return new Resultado(res.colunas(), res.linhas(), res.total(), res.sql());
                    }
                    logger.log(System.Logger.Level.WARNING,
                            "Rota executar devolveu sucesso sem 'resultado': {0}", rd.getData());
                } else {
                    logger.log(System.Logger.Level.WARNING,
                            "Rota executar devolveu erro: {0}", rd.getMessage());
                }
            }
        } catch (Exception e) {
            logger.log(System.Logger.Level.ERROR,
                    "Falha ao executar avançado", e);
        }
        return new Resultado(List.of(), List.of(), 0, "");
    }

    /** Envelope — evita dependência direta do record do db na UI. */
    public record Resultado(
            List<String> colunas,
            List<Map<String, Object>> linhas,
            int total,
            String sql) {}
}