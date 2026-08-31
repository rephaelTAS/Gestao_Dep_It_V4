package com.ossobo.gestaoDepIt.db.models;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * PendingSync v1.0
 *
 * Objetivo: Uma escrita local aguardando envio ao servidor remoto (outbox).
 *           Cada instância = 1 registro alterado offline, na fila local.
 *
 * v1.0 - Criação. Fábrica nova() gera UUID e carimba o momento.
 */
public record PendingSync(
        String id,            // UUID da própria pendência
        String tabela,        // tabela de negócio de origem
        String registroId,    // UUID do registro alterado
        String operacao,      // UPSERT | DELETE
        LocalDateTime criadoEm,
        int tentativas
) {

    public static final String OP_UPSERT = "UPSERT";
    public static final String OP_DELETE = "DELETE";

    /** Cria pendência nova (tentativas=0, momento=agora). */
    public static PendingSync nova(String tabela, String registroId, String operacao) {
        if (tabela == null || tabela.isBlank()) {
            throw new IllegalArgumentException("Tabela é obrigatória na pendência");
        }
        if (registroId == null || registroId.isBlank()) {
            throw new IllegalArgumentException("registroId é obrigatório na pendência");
        }
        if (!OP_UPSERT.equals(operacao) && !OP_DELETE.equals(operacao)) {
            throw new IllegalArgumentException("Operação inválida: " + operacao);
        }
        return new PendingSync(UUID.randomUUID().toString(), tabela, registroId,
                operacao, LocalDateTime.now(), 0);
    }

    /** Cópia com tentativas incrementada (uso do engine em falhas de push). */
    public PendingSync comTentativaExtra() {
        return new PendingSync(id, tabela, registroId, operacao, criadoEm, tentativas + 1);
    }
}