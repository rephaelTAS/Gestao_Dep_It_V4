package com.ossobo.gestaoDepIt.db.sync.models;

import java.time.LocalDateTime;

/**
 * EstadoSync v1.0
 *
 * Estado de sincronização de uma entidade.
 */
public record EstadoSync(
        String entidade,
        LocalDateTime lastSync,     // null = nunca sincronizou (bootstrap)
        String status,              // OK | ERRO | EM_ANDAMENTO | NUNCA
        String mensagem,
        LocalDateTime atualizadoEm
) {}