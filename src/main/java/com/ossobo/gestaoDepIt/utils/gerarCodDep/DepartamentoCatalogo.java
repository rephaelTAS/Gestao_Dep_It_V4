package com.ossobo.gestaoDepIt.utils.gerarCodDep;

import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * DepartamentoCatalogo v1.0
 *
 * Fonte única dos departamentos e dos seus prefixos de cod_dep.
 * Prefixo: 3 letras maiúsculas (POM, DAF, DIF...).
 * cod_dep final: <PREFIXO>_<NN> (ex: POM_01, DAF_02).
 *
 * Casamento por substring normalizada — tolera acentos e variações.
 */
public final class DepartamentoCatalogo {

    private DepartamentoCatalogo() {}

    /** chave = substring da label; valor = prefixo de 3 letras. */
    private static final Map<String, String> PREFIXOS = new LinkedHashMap<>();

    static {
        coloca("informatico", "DIF");
        coloca("industrial", "POM");
        coloca("financeiro", "DAF");
        coloca("plantacao", "DPL");
        coloca("oficina", "DOF");
        coloca("rh", "DRH");
        coloca("compras", "DCL");
        coloca("logistica", "DCL");
        coloca("direcao geral", "DGE");
    }

    private static void coloca(String chave, String prefixo) {
        PREFIXOS.put(chave.toLowerCase(), prefixo);
    }

    /**
     * Deriva o prefixo de 3 letras a partir do nome do departamento.
     * Devolve "OUT" quando nada casa (fallback seguro).
     */
    public static String prefixo(String departamento) {
        if (departamento == null || departamento.isBlank()) return "OUT";
        String normalizado = normalizar(departamento);
        return PREFIXOS.entrySet().stream()
                .filter(e -> normalizado.contains(e.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse("OUT");
    }

    /** Normaliza acentos e caixa. */
    private static String normalizar(String s) {
        return Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase()
                .trim();
    }

    public static Set<String> prefixosConhecidos() {
        return Set.copyOf(PREFIXOS.values());
    }
}