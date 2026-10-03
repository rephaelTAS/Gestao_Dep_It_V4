package com.ossobo.gestaoDepIt.controlls.historico.historicoDetail;

import java.util.Map;

/**
 * HistoricoFormatter v1.0
 *
 * Utilitário puro: formata snapshots JSON para exibição legível
 * (uma chave por linha, indentada) — para as TextAreas do Detail.
 *
 * @since v1.0
 */
public final class HistoricoFormatter {

    private HistoricoFormatter() { }

    /**
     * Formata JSON plano ou aninhado como uma chave por linha.
     * Tolerante: se não conseguir parsear, devolve o texto original.
     */
    public static String formatar(String json) {
        if (json == null || json.isBlank() || "{}".equals(json.trim())) {
            return "(sem dados)";
        }
        try {
            Map<String, String> mapa = HistoricoDiff.parsePlano(json);
            if (mapa.isEmpty()) return json;
            StringBuilder sb = new StringBuilder();
            for (var e : mapa.entrySet()) {
                sb.append(String.format("%-22s : %s%n", e.getKey(), e.getValue()));
            }
            return sb.toString().trim();
        } catch (Exception e) {
            return json;
        }
    }

    /** Extrai um valor de um JSON plano (útil para ler campos de descricaoFuncionario). */
    public static String extrairValor(String json, String chave) {
        if (json == null || chave == null) return null;
        return HistoricoDiff.parsePlano(json).get(chave);
    }
}