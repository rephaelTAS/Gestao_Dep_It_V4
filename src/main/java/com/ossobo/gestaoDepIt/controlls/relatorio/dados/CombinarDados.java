package com.ossobo.gestaoDepIt.controlls.relatorio.dados;

import com.ossobo.gestaoDepIt.db.relatorios.RelatorioModelo;
import com.ossobo.gestaoDepIt.db.relatorios.RelatorioResultado;
import com.ossobo.winterfx.router.Rotas;
import com.ossobo.winterfx.router.model.Params;
import com.ossobo.winterfx.router.model.ResponseData;

import java.util.List;
import java.util.Map;

/**
 * CombinarDados v1.0
 *
 * Centro de dados do módulo de relatórios. Fachada estática única para
 * Simples, Avançado e Dashboard — encapsula TODAS as rotas de consulta
 * (e as de ledger/modelos quando o Dashboard precisar escrever).
 *
 * Regra: nenhum controller de tela chama Rotas diretamente; tudo passa
 * por aqui. Um erro de contrato de rota é corrigido num só lugar.
 *
 * Convenções:
 *   - Resultado é o envelope neutro (não vaza RelatorioResultado para a UI).
 *   - Métodos retornam valor "vazio" (0, List.of(), Map.of()) quando a rota
 *     falha — a UI não quebra. Erros são logados.
 */
public final class CombinarDados {

    private static final System.Logger logger =
            System.getLogger(CombinarDados.class.getName());

    private CombinarDados() {}

    // ============================================================
    // METADADOS — grafo + colunas
    // ============================================================

    @SuppressWarnings("unchecked")
    public static List<String> aliases() {
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
    // EXECUÇÃO — Avançado
    // ============================================================

    /**
     * Executa o relatório avançado (modelo + joins + filtros).
     * Rota: PUT relatorio-avancado/service/executar (payload "modelo").
     */
    public static Resultado executarAvancado(RelatorioModelo modelo, int limite) {
        if (modelo == null) return Resultado.vazio();
        try {
            Params p = Params.with("modelo", modelo).and("limite", limite);
            Object r = Rotas.put("relatorio-avancado/service/executar", p);
            return extrairResultado(r, "avançado");
        } catch (Exception e) {
            logger.log(System.Logger.Level.ERROR, "Falha ao executar avançado", e);
            return Resultado.vazio();
        }
    }

    // ============================================================
    // EXECUÇÃO — Simples
    // ============================================================

    /**
     * Executa o relatório simples (uma tabela).
     * Rota: GET relatorio-simples/service/executar
     *       params → alias, limite; payload → colunas, filtros.
     */
    public static Resultado executarSimples(String alias,
                                            List<String> colunas,
                                            Map<String, Object> filtros,
                                            int limite) {
        if (alias == null || alias.isBlank()) return Resultado.vazio();
        try {
            Params p = Params.with("alias", alias)
                    .and("limite", limite)
                    .and("colunas", colunas == null ? List.of() : colunas)
                    .and("filtros", filtros == null ? Map.of() : filtros);
            Object r = Rotas.get("relatorio-simples/service/executar", p);
            return extrairResultado(r, "simples");
        } catch (Exception e) {
            logger.log(System.Logger.Level.ERROR, "Falha ao executar simples", e);
            return Resultado.vazio();
        }
    }

    // ============================================================
    // EXPORT — Avançado
    // ============================================================

    /** Exporta o relatório avançado para XLSX. Retorna null em falha. */
    public static byte[] exportarAvancado(RelatorioModelo modelo) {
        if (modelo == null) return null;
        try {
            Object r = Rotas.put("relatorio-avancado/service/exportar-xlsx",
                    Params.with("modelo", modelo));
            return extrairArquivo(r, "avançado");
        } catch (Exception e) {
            logger.log(System.Logger.Level.ERROR, "Falha ao exportar avançado", e);
            return null;
        }
    }

    // ============================================================
    // EXPORT — Simples
    // ============================================================

    /** Exporta o relatório simples para XLSX. Retorna null em falha. */
    public static byte[] exportarSimples(String alias,
                                         List<String> colunas,
                                         Map<String, Object> filtros) {
        if (alias == null || alias.isBlank()) return null;
        try {
            Params p = Params.with("alias", alias)
                    .and("colunas", colunas == null ? List.of() : colunas)
                    .and("filtros", filtros == null ? Map.of() : filtros);
            Object r = Rotas.put("relatorio-simples/service/exportar-xlsx", p);
            return extrairArquivo(r, "simples");
        } catch (Exception e) {
            logger.log(System.Logger.Level.ERROR, "Falha ao exportar simples", e);
            return null;
        }
    }

    // ============================================================
    // HISTÓRICO (Dashboard)
    // ============================================================

    public static long totalHoje()      { return longDe("relatorio-historico/service/total/hoje", "total"); }
    public static long totalSemana()    { return longDe("relatorio-historico/service/total/semana", "total"); }
    public static long totalGeral()     { return longDe("relatorio-historico/service/total", "total"); }
    public static double mediaPorDia()  { return doubleDe("relatorio-historico/service/media-por-dia", "media"); }

    public static Map<String, Integer> contagemPorCombinacao() {
        try {
            Object r = Rotas.get("relatorio-historico/service/contagem-por-combinacao");
            if (r instanceof ResponseData rd && rd.isSuccess()) {
                Object c = rd.getData().get("contagem");
                if (c instanceof Map<?, ?> m) {
                    java.util.LinkedHashMap<String, Integer> out = new java.util.LinkedHashMap<>();
                    m.forEach((k, v) -> out.put(String.valueOf(k),
                            v instanceof Number n ? n.intValue() : 0));
                    return out;
                }
            }
        } catch (Exception e) {
            logger.log(System.Logger.Level.WARNING, "Falha ao buscar contagem: {0}", e.getMessage());
        }
        return Map.of();
    }

    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> recentes(int limite) {
        try {
            Object r = Rotas.get("relatorio-historico/service/recentes",
                    Params.with("limite", limite));
            if (r instanceof ResponseData rd && rd.isSuccess()) {
                Object dados = rd.getData().get("relatorios");
                if (dados instanceof List<?> l)
                    return (List<Map<String, Object>>) l;
            }
        } catch (Exception e) {
            logger.log(System.Logger.Level.WARNING, "Falha ao buscar recentes: {0}", e.getMessage());
        }
        return List.of();
    }

    // ============================================================
    // MODELOS (Dashboard)
    // ============================================================

    public static List<RelatorioModelo> listarModelos() {
        try {
            Object r = Rotas.get("relatorio-modelo/service/todos");
            if (r instanceof ResponseData rd && rd.isSuccess()) {
                Object dados = rd.getData().get("modelos");
                if (dados instanceof List<?> l) {
                    return l.stream()
                            .filter(RelatorioModelo.class::isInstance)
                            .map(RelatorioModelo.class::cast)
                            .toList();
                }
            }
        } catch (Exception e) {
            logger.log(System.Logger.Level.WARNING, "Falha ao listar modelos: {0}", e.getMessage());
        }
        return List.of();
    }

    public static boolean salvarModelo(RelatorioModelo modelo) {
        if (modelo == null) return false;
        try {
            Object r = Rotas.put("relatorio-modelo/service/salvar",
                    Params.with("modelo", modelo));
            return r instanceof ResponseData rd && rd.isSuccess();
        } catch (Exception e) {
            logger.log(System.Logger.Level.ERROR, "Falha ao salvar modelo", e);
            return false;
        }
    }

    public static boolean removerModelo(String id) {
        if (id == null || id.isBlank()) return false;
        try {
            Object r = Rotas.delete("relatorio-modelo/service/deletar/por/id",
                    Params.with("id", id));
            return r instanceof ResponseData rd && rd.isSuccess();
        } catch (Exception e) {
            logger.log(System.Logger.Level.ERROR, "Falha ao remover modelo", e);
            return false;
        }
    }

    // ============================================================
    // INTERNOS
    // ============================================================

    private static Resultado extrairResultado(Object resposta, String origem) {
        if (!(resposta instanceof ResponseData rd)) {
            logger.log(System.Logger.Level.WARNING,
                    "Rota {0} devolveu tipo inesperado: {1}", origem, resposta);
            return Resultado.vazio();
        }
        if (!rd.isSuccess()) {
            logger.log(System.Logger.Level.WARNING,
                    "Rota {0} devolveu erro: {1}", origem, rd.getMessage());
            return Resultado.vazio();
        }
        Object resultado = rd.getData().get("resultado");
        if (resultado instanceof RelatorioResultado res) {
            return new Resultado(res.colunas(), res.linhas(), res.total(), res.sql());
        }
        logger.log(System.Logger.Level.WARNING,
                "Rota {0} devolveu sucesso sem 'resultado'", origem);
        return Resultado.vazio();
    }

    private static byte[] extrairArquivo(Object resposta, String origem) {
        if (!(resposta instanceof ResponseData rd) || !rd.isSuccess()) {
            if (resposta instanceof ResponseData rdErr)
                logger.log(System.Logger.Level.WARNING,
                        "Export {0} devolveu erro: {1}", origem, rdErr.getMessage());
            return null;
        }
        Object arquivo = rd.getData().get("arquivo");
        return (arquivo instanceof byte[] b) ? b : null;
    }

    private static long longDe(String rota, String chave) {
        try {
            Object r = Rotas.get(rota);
            if (r instanceof ResponseData rd && rd.isSuccess()) {
                Object v = rd.getData().get(chave);
                if (v instanceof Number n) return n.longValue();
            }
        } catch (Exception ignored) {}
        return 0L;
    }

    private static double doubleDe(String rota, String chave) {
        try {
            Object r = Rotas.get(rota);
            if (r instanceof ResponseData rd && rd.isSuccess()) {
                Object v = rd.getData().get(chave);
                if (v instanceof Number n) return n.doubleValue();
            }
        } catch (Exception ignored) {}
        return 0.0;
    }

    // ============================================================
    // ENVELOPE
    // ============================================================

    public record Resultado(
            List<String> colunas,
            List<Map<String, Object>> linhas,
            int total,
            String sql) {

        public static Resultado vazio() {
            return new Resultado(List.of(), List.of(), 0, "");
        }

        public boolean vazioResultado() {
            return linhas.isEmpty();
        }
    }
}