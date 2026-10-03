package com.ossobo.gestaoDepIt.db.sync;

/**
 * Estado do canal remoto no primeiro ciclo — alimenta o SyncEvent e a UI.
 */
public enum DisponibilidadeRemota {
    /** Nenhuma config ativa: modo local PERMANENTE até o usuário configurar. */
    SEM_CONFIG,
    /** Config existe, host inacessível: modo local TEMPORÁRIO (fila cresce). */
    OFFLINE,
    /** Pipeline liberado. */
    ONLINE
}