package com.ossobo.gestaoDepIt.db.sync;

import com.ossobo.gestaoDepIt.db.sync.ConflictResolver.SyncSnapshot;

import java.time.LocalDateTime;
import java.util.List;

/**
 * SyncAdapter - Contrato por entidade para o SyncEngine.
 *
 * Cada entidade (Catálogo, Funcionários, etc.) implementa este adapter,
 * traduzindo entre o model concreto e o motor de sync.
 */
public interface SyncAdapter<T> {

    /** Chave de sync_state: "CATALOGO", "INVENTARIO", etc. */
    String entidade();

    /** Model → Snapshot para o ConflictResolver. */
    SyncSnapshot toSnapshot(T registro);

    /** Busca registros no MySQL alterados após o horizonte. */
    List<T> buscarRemotosDesde(LocalDateTime horizonte) throws Exception;

    /** Estado atual no SQLite; null se inexistente. */
    SyncSnapshot snapshotDoLocal(String registroId) throws Exception;

    /** Aplica o estado vindo do remoto no SQLite. */
    void aplicarNoLocal(T recebido) throws Exception;

    /** Travessia: aplica o vencedor local no MySQL (PUSH). */
    void aplicarNoRemoto(T vencedor) throws Exception;
}