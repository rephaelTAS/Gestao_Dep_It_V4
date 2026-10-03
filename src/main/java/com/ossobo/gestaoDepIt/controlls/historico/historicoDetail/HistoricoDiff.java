package com.ossobo.gestaoDepIt.controlls.historico.historicoDetail;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * HistoricoDiff v1.0
 *
 * Utilitário puro: compara dois snapshots JSON-planos e devolve a lista de
 * diferenças campo a campo. NÃO persiste, NÃO conhece banco, NÃO conhece UI.
 *
 * Contrato de dadosAnteriores/dadosNovos: objeto JSON plano
 *   {"chave":"valor","outra":"valor2"}
 *
 * Chaves de um lado só aparecem com "(vazio)" no outro lado.
 *
 * @since v1.0
 */
public final class HistoricoDiff {

    public record Diferenca(String campo, String antes, String depois) {
        public String formatado() {
            return String.format("%-20s  %-30s  →  %s", campo, abreviar(antes), abreviar(depois));
        }

        private static String abreviar(String v) {
            return (v == null) ? "(vazio)" : v;
        }
    }

    private HistoricoDiff() { }

    /** Compara dois JSONs planos. Devolve a lista de diferenças na ordem das chaves. */
    public static List<Diferenca> comparar(String jsonAntes, String jsonDepois) {
        var mapaAntes = parsePlano(jsonAntes);
        var mapaDepois = parsePlano(jsonDepois);

        Set<String> todasChaves = new HashSet<>();
        todasChaves.addAll(mapaAntes.keySet());
        todasChaves.addAll(mapaDepois.keySet());

        List<Diferenca> diferencas = new ArrayList<>();
        for (String chave : todasChaves) {
            String vAntes = mapaAntes.get(chave);
            String vDepois = mapaDepois.get(chave);
            if (!Objects.equals(vAntes, vDepois)) {
                diferencas.add(new Diferenca(chave,
                        vAntes != null ? vAntes : "",
                        vDepois != null ? vDepois : ""));
            }
        }
        return diferencas;
    }

    /** Conta quantas chaves são iguais (idênticas) nos dois lados. */
    public static int contarIguais(String jsonAntes, String jsonDepois) {
        var mapaAntes = parsePlano(jsonAntes);
        var mapaDepois = parsePlano(jsonDepois);

        int iguais = 0;
        for (var e : mapaAntes.entrySet()) {
            if (Objects.equals(e.getValue(), mapaDepois.get(e.getKey()))) iguais++;
        }
        return iguais;
    }

    // ============================================================
    // Parser JSON plano — SEM dependência externa. Suporta aninhamento simples.
    // ============================================================

    /**
     * Parse de objeto JSON plano. Chaves com valor string/número/boolean.
     * Objetos aninhados viram string "{...}" (comparação textual do campo).
     * Tolerante a espaços; trata aspas escapadas.
     */
    public static java.util.Map<String, String> parsePlano(String json) {
        java.util.LinkedHashMap<String, String> mapa = new java.util.LinkedHashMap<>();
        if (json == null || json.isBlank()) return mapa;

        String s = json.trim();
        if (s.startsWith("{")) s = s.substring(1);
        if (s.endsWith("}")) s = s.substring(0, s.length() - 1);

        int i = 0;
        while (i < s.length()) {
            // chave
            int abrirChave = s.indexOf('"', i);
            if (abrirChave < 0) break;
            int fecharChave = encontrarAspa(s, abrirChave + 1);
            if (fecharChave < 0) break;
            String chave = s.substring(abrirChave + 1, fecharChave);

            // dois pontos
            int dp = s.indexOf(':', fecharChave);
            if (dp < 0) break;

            // valor: string, número, boolean, objeto, array ou nulo
            int inicio = dp + 1;
            while (inicio < s.length() && Character.isWhitespace(s.charAt(inicio))) inicio++;
            if (inicio >= s.length()) break;

            String valor;
            int proximo;
            char c = s.charAt(inicio);
            if (c == '"') {
                int fim = encontrarAspa(s, inicio + 1);
                if (fim < 0) break;
                valor = s.substring(inicio + 1, fim);
                proximo = fim + 1;
            } else if (c == '{') {
                int fim = encontrarFechamento(s, inicio, '{', '}');
                if (fim < 0) break;
                valor = s.substring(inicio, fim + 1);
                proximo = fim + 1;
            } else if (c == '[') {
                int fim = encontrarFechamento(s, inicio, '[', ']');
                if (fim < 0) break;
                valor = s.substring(inicio, fim + 1);
                proximo = fim + 1;
            } else {
                int fim = inicio;
                while (fim < s.length() && s.charAt(fim) != ',' && s.charAt(fim) != '}') fim++;
                valor = s.substring(inicio, fim).trim();
                proximo = fim;
            }

            mapa.put(chave, valor);

            // próxima vírgula
            int proxima = s.indexOf(',', proximo);
            if (proxima < 0) break;
            i = proxima + 1;
        }
        return mapa;
    }

    private static int encontrarAspa(String s, int inicio) {
        for (int i = inicio; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\') { i++; continue; }
            if (c == '"') return i;
        }
        return -1;
    }

    private static int encontrarFechamento(String s, int inicio, char abre, char fecha) {
        int nivel = 0;
        boolean emString = false;
        for (int i = inicio; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\') { i++; continue; }
            if (c == '"') emString = !emString;
            if (emString) continue;
            if (c == abre) nivel++;
            else if (c == fecha) {
                nivel--;
                if (nivel == 0) return i;
            }
        }
        return -1;
    }
}