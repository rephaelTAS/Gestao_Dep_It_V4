package com.ossobo.gestaoDepIt.db.documentos;

import java.text.Normalizer;
import java.util.Map;

/**
 * CodigoCurto v1.0
 *
 * Responsabilidade: derivar códigos curtos (3 letras) a partir de nomes de
 * departamentos e marcas, conforme convenção ratificada.
 *
 * Estratégia:
 *   - Remove acentos e normaliza para ASCII.
 *   - Remove conectivos (de, da, do, e, ...).
 *   - Se há 3+ palavras significativas: pega 1ª letra de cada (até 3).
 *   - Se há 1 palavra: pega as 3 primeiras letras.
 *   - Se há 2 palavras: 1ª letra da 1ª + 2 primeiras da 2ª (total 3).
 *
 * Exceções mapeadas em CODIGOS_FIXOS — vazio hoje; adicionar quando
 * algum nome não gerar código satisfatório.
 *
 * @since v1.0
 */
public final class CodigoCurto {

    private static final Map<String, String> CODIGOS_FIXOS = Map.of(
            // Exemplo futuro: "Departamento de Compra e Logística" → forçar "DCL"
            // "compra e logistica" -> "DCL"
    );

    private static final Map<String, String> MARCAS_FIXAS = Map.of(
            // Exemplo futuro: "Logitech" -> "LOG"
    );

    private CodigoCurto() {}

    public static String deDepartamento(String nome) {
        return gerar(nome, CODIGOS_FIXOS);
    }

    public static String deMarca(String nome) {
        return gerar(nome, MARCAS_FIXAS);
    }

    private static String gerar(String bruto, Map<String, String> fixos) {
        if (bruto == null || bruto.isBlank()) {
            throw new IllegalArgumentException("Nome vazio para código curto");
        }
        String normalizado = normalizar(bruto);
        String fixo = fixos.get(normalizado);
        if (fixo != null) return fixo;

        String[] palavras = significativas(normalizado);
        if (palavras.length == 0) {
            throw new IllegalArgumentException("Nome sem palavras úteis: '" + bruto + "'");
        }

        String codigo;
        if (palavras.length >= 3) {
            codigo = "" + p(palavras[0]) + p(palavras[1]) + p(palavras[2]);
        } else if (palavras.length == 2) {
            codigo = p(palavras[0]) + primeiras(palavras[1], 2);
        } else {
            codigo = primeiras(palavras[0], 3);
        }
        if (codigo.length() < 3) {
            codigo = (codigo + "XXX").substring(0, 3);
        }
        return codigo.toUpperCase();
    }

    private static String normalizar(String s) {
        String semAcento = Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return semAcento.toLowerCase().trim();
    }

    private static String[] significativas(String normalizado) {
        String limpo = normalizado
                .replaceAll("[^a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
        if (limpo.isEmpty()) return new String[0];
        return java.util.Arrays.stream(limpo.split(" "))
                .filter(p -> !p.isEmpty())
                .filter(p -> !isConectivo(p))
                .toArray(String[]::new);
    }

    private static boolean isConectivo(String p) {
        return switch (p) {
            case "de", "da", "do", "das", "dos", "e", "o", "a", "os", "as", "em" -> true;
            default -> false;
        };
    }

    private static char p(String palavra) {
        return Character.toUpperCase(palavra.charAt(0));
    }

    private static String primeiras(String palavra, int n) {
        String s = palavra.toUpperCase();
        return s.length() >= n ? s.substring(0, n) : (s + "XXX").substring(0, n);
    }
}