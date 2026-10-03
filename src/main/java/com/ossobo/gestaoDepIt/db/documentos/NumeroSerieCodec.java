package com.ossobo.gestaoDepIt.db.documentos;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * NumeroSerieCodec v1.0
 *
 * Responsabilidade: conversão pura entre a representação textual do contador
 *                   do número de série ("00-01-A") e um inteiro monotônico
 *                   interno, além do inverso.
 *
 * Formato: DD-DD-S onde
 *   - DD (bloco 1): 00..99  → contador do departamento
 *   - DD (bloco 2): 00..99  → contador da marca dentro do departamento
 *   - S:            A..Z, AA..ZZ, ...  → sufixo alfabético base-26
 *
 * Ordem de incremento (definida pelo cliente):
 *   00-01-A → 00-02-A → ... → 00-99-A
 *           → 01-00-A → 01-01-A → ... → 01-99-A
 *           → ... → 99-99-A
 *           → 00-00-B → ... → 99-99-Z
 *           → 00-00-AA → ...
 *
 * Mapeamento inteiro: idx = (b1 * 100 + b2) + 10_000 * sufixoBase26
 * onde sufixoBase26 é "A=0, B=1, ..., Z=25, AA=26, AB=27, ..."
 *
 * @since v1.0
 */
public final class NumeroSerieCodec {

    private static final int BLOCOS_POR_SUFIXO = 100 * 100; // 10_000

    private static final Pattern FORMATO =
            Pattern.compile("^(\\d{2})-(\\d{2})-([A-Z]+)$");

    private NumeroSerieCodec() {}

    // ============================================================
    // TEXTO → ÍNDICE
    // ============================================================

    public static long paraIndice(String contador) {
        if (contador == null || contador.isBlank()) {
            throw new IllegalArgumentException("Contador vazio");
        }
        Matcher m = FORMATO.matcher(contador.trim());
        if (!m.matches()) {
            throw new IllegalArgumentException(
                    "Contador fora do formato DD-DD-S: '" + contador + "'");
        }
        int b1 = Integer.parseInt(m.group(1));
        int b2 = Integer.parseInt(m.group(2));
        if (b1 > 99 || b2 > 99) {
            throw new IllegalArgumentException("Blocos devem ser 00..99: '" + contador + "'");
        }
        long sufixo = sufixoParaIndice(m.group(3));
        return (long) (b1 * 100 + b2) + sufixo * BLOCOS_POR_SUFIXO;
    }

    // ============================================================
    // ÍNDICE → TEXTO
    // ============================================================

    public static String paraContador(long indice) {
        if (indice < 0) throw new IllegalArgumentException("Índice negativo");
        long sufixoIdx = indice / BLOCOS_POR_SUFIXO;
        int bloco = (int) (indice % BLOCOS_POR_SUFIXO);
        int b1 = bloco / 100;
        int b2 = bloco % 100;
        return String.format("%02d-%02d-%s", b1, b2, indiceParaSufixo(sufixoIdx));
    }

    /** Próximo contador a partir de um texto atual. */
    public static String proximo(String atual) {
        return paraContador(paraIndice(atual) + 1);
    }

    /** Primeiro contador da sequência. */
    public static String primeiro() {
        return "00-01-A";
    }

    // ============================================================
    // SUFIXO BASE-26 (A..Z, AA..ZZ, ...)
    // ============================================================

    static long sufixoParaIndice(String sufixo) {
        long out = 0;
        for (int i = 0; i < sufixo.length(); i++) {
            char c = sufixo.charAt(i);
            if (c < 'A' || c > 'Z') {
                throw new IllegalArgumentException("Sufixo inválido: '" + sufixo + "'");
            }
            out = out * 26 + (c - 'A' + 1);
        }
        return out - 1; // "A" → 0
    }

    static String indiceParaSufixo(long idx) {
        if (idx < 0) throw new IllegalArgumentException("Índice de sufixo negativo");
        long n = idx + 1;
        StringBuilder sb = new StringBuilder();
        while (n > 0) {
            long resto = (n - 1) % 26;
            sb.insert(0, (char) ('A' + resto));
            n = (n - 1) / 26;
        }
        return sb.toString();
    }
}