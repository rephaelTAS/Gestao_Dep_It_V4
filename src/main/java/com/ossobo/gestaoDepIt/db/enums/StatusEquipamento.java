package com.ossobo.gestaoDepIt.db.enums;

import java.util.List;

/**
 * StatusEquipamento v1.0
 *
 * Fonte única de verdade dos status de equipamento do inventário.
 * Alinhado ao CHECK do schema v2.0:
 *   status IN ('ATIVO','MANUTENCAO','BAIXADO','RESERVA','EM_USO')
 *
 * Uso:
 *   - Validação:      StatusEquipamento.isValido(String)
 *   - Fábrica:        StatusEquipamento.de(String) — lança se inválido
 *   - Listagem:       StatusEquipamento.todos()  — para UI / rotas de domínio
 *   - Default seguro: StatusEquipamento.defaultStatus() → ATIVO
 */
public enum StatusEquipamento {
    ATIVO,
    MANUTENCAO,
    BAIXADO,
    RESERVA,
    EM_USO;

    public static final StatusEquipamento PADRAO = ATIVO;

    /** true se o texto (case-insensitive) corresponde a um status válido. */
    public static boolean isValido(String status) {
        if (status == null || status.isBlank()) return false;
        String s = status.trim().toUpperCase();
        for (StatusEquipamento v : values()) {
            if (v.name().equals(s)) return true;
        }
        return false;
    }

    /** Converte texto (case-insensitive) em StatusEquipamento ou lança IllegalArgumentException. */
    public static StatusEquipamento de(String status) {
        if (!isValido(status)) {
            throw new IllegalArgumentException("Status inválido: " + status);
        }
        return valueOf(status.trim().toUpperCase());
    }

    /** Lista imutável dos nomes, na ordem de declaração. */
    public static List<String> todos() {
        return java.util.Arrays.stream(values())
                .map(Enum::name)
                .toList();
    }

    /** Texto humano do status — para exibição na UI. */
    public String getDescricao() {
        return switch (this) {
            case ATIVO      -> "Ativo";
            case MANUTENCAO -> "Em Manutenção";
            case BAIXADO    -> "Baixado";
            case RESERVA    -> "Reservado";
            case EM_USO     -> "Em Uso";
        };
    }
}