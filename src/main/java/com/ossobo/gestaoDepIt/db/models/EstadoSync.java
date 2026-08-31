package com.ossobo.gestaoDepIt.db.models;

import java.time.LocalDateTime;

public record EstadoSync(
        String entidade,
        LocalDateTime lastSync,     // null = nunca sincronizou (bootstrap)
        String status,              // OK | ERRO | EM_ANDAMENTO | NUNCA
        String mensagem,
        LocalDateTime atualizadoEm
) {}