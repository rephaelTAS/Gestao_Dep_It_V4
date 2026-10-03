package com.ossobo.gestaoDepIt.db.enums;

import java.util.List;

/**
 * CondicaoEquipamento v1.0
 *
 * Fonte única de verdade das condições de equipamento do inventário.
 * Alinhado ao CHECK do schema v2.0:
 *   condicao IN ('OTIMO','BOM','REGULAR','CRITICO')
 *
 * Uso:
 *   - Validação:      CondicaoEquipamento.isValido(String)
 *   - Fábrica:        CondicaoEquipamento.de(String) — lança se inválido
 *   - Listagem:       CondicaoEquipamento.todos()  — para UI / rotas de domínio
 *   - Default seguro: CondicaoEquipamento.PADRAO → BOM
 */
public enum CondicaoEquipamento {
    OTIMO,
    BOM,
    REGULAR,
    CRITICO;

    public static final CondicaoEquipamento PADRAO = BOM;

    /** true se o texto (case-insensitive) corresponde a uma condição válida. */
    public static boolean isValido(String condicao) {
        if (condicao == null || condicao.isBlank()) return false;
        String c = condicao.trim().toUpperCase();
        for (CondicaoEquipamento v : values()) {
            if (v.name().equals(c)) return true;
        }
        return false;
    }

    /** Converte texto (case-insensitive) em CondicaoEquipamento ou lança IllegalArgumentException. */
    public static CondicaoEquipamento de(String condicao) {
        if (!isValido(condicao)) {
            throw new IllegalArgumentException("Condição inválida: " + condicao);
        }
        return valueOf(condicao.trim().toUpperCase());
    }

    /** Lista imutável dos nomes, na ordem de declaração. */
    public static List<String> todos() {
        return java.util.Arrays.stream(values())
                .map(Enum::name)
                .toList();
    }

    /** Texto humano da condição — para exibição na UI. */
    public String getDescricao() {
        return switch (this) {
            case OTIMO   -> "Ótimo";
            case BOM     -> "Bom";
            case REGULAR -> "Regular";
            case CRITICO -> "Crítico";
        };
    }
}