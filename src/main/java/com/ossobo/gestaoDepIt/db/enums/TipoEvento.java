package com.ossobo.gestaoDepIt.db.enums;

/**
 * TipoEvento v1.0
 *
 * Fonte única de verdade dos tipos de evento do histórico.
 * Alinhado a HistoricoEventos.TIPOS_VALIDOS.
 *
 * Uso:
 *   - Validação: TipoEvento.isValido(String)
 *   - Fábrica:   TipoEvento.valueOf(...) — lança se inválido
 *   - Listagem:  TipoEvento.todos()      — para UI / rotas de tipos-permitidos
 */
public enum TipoEvento {
    CRIACAO,
    ATUALIZACAO,
    DEVOLUCAO,
    BAIXA,
    EXCLUSAO,
    MANUTENCAO,
    MOVIMENTACAO,
    LOCALIZACAO,
    INSTALACAO,
    LOGIN,
    LOGOUT,
    TRANSFERENCIA;

    /** true se o texto (case-insensitive) corresponde a um tipo válido. */
    public static boolean isValido(String tipo) {
        if (tipo == null || tipo.isBlank()) return false;
        String t = tipo.trim().toUpperCase();
        for (TipoEvento valor : values()) {
            if (valor.name().equals(t)) return true;
        }
        return false;
    }

    /** Converte texto (case-insensitive) em TipoEvento ou lança IllegalArgumentException. */
    public static TipoEvento de(String tipo) {
        if (!isValido(tipo)) {
            throw new IllegalArgumentException("Tipo de evento inválido: " + tipo);
        }
        return valueOf(tipo.trim().toUpperCase());
    }

    /** Lista imutável dos nomes, na ordem de declaração. */
    public static java.util.List<String> todos() {
        return java.util.Arrays.stream(values())
                .map(Enum::name)
                .toList();
    }
}