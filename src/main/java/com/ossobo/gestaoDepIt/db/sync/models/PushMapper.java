package com.ossobo.gestaoDepIt.db.sync.models;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * PushMapper v1.0
 *
 * Contrato de um mapper de sincronização local → remoto.
 * Implementado por UMA classe por entidade (funcionarios, usuarios, etc.).
 *
 * Responsabilidades:
 *   - Ler os registros da entidade no SQLite local
 *   - Escrever/atualizar no MySQL remoto (INSERT ... ON DUPLICATE KEY UPDATE)
 *   - Reportar quantos registros foram empurrados
 *
 * NÃO abre conexão — recebe a Connection MySQL já aberta pelo SyncPushService.
 * NÃO controla transação — o SyncPushService faz commit/rollback.
 */
public interface PushMapper {

    /** Nome lógico da entidade (usado em sync_state e logs). */
    String entidade();

    /**
     * Empurra TODOS os registros da entidade (modo full).
     * Chamado quando sync_state está vazio.
     *
     * @param connMySQL conexão com o MySQL remoto, já aberta e em transação
     * @return número de registros empurrados
     */
    int pushFull(Connection connMySQL) throws SQLException;

    /**
     * Empurra registros específicos por ID (modo incremental).
     * Chamado quando há pending_sync para esta entidade.
     *
     * @param connMySQL conexão com o MySQL remoto, já aberta e em transação
     * @param ids       lista de registro_id pendentes (UUIDs)
     * @return número de registros empurrados
     */
    int pushIncremental(Connection connMySQL, java.util.List<String> ids) throws SQLException;
}