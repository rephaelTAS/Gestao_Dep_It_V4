package com.ossobo.gestaoDepIt.db.sync.models;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * SyncPushResult v1.0
 *
 * Resultado de uma operação de push do local para o remoto.
 * Imutável — devolvido ao chamador no fim de SyncPushService.empurrar().
 */
public record SyncPushResult(
        boolean sucesso,
        boolean fullSync,             // true se foi bootstrap (full), false se incremental
        LocalDateTime momento,
        Map<String, Integer> registrosPorEntidade,  // entidade → nº registros empurrados
        int totalRegistros,
        String mensagemErro           // null se sucesso
) {

    public static SyncPushResult ok(boolean fullSync, Map<String, Integer> porEntidade) {
        int total = porEntidade.values().stream().mapToInt(Integer::intValue).sum();
        return new SyncPushResult(
                true, fullSync, LocalDateTime.now(),
                Map.copyOf(porEntidade), total, null);
    }

    public static SyncPushResult erro(boolean fullSync, String mensagem) {
        return new SyncPushResult(
                false, fullSync, LocalDateTime.now(),
                Map.of(), 0, mensagem);
    }
}