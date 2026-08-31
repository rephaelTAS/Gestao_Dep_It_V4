package com.ossobo.gestaoDepIt.db.sync;

import com.ossobo.winterfx.anotations.Component;

import java.time.LocalDateTime;

/**
 * ConflictResolver v1.0
 *
 * Responsabilidade: Decidir o destino de um registro quando dois estados
 *                   divergem (local × remoto). POLÍTICA PURA — zero I/O,
 *                   zero banco, zero framework. Estado imutável →
 *                   inerentemente thread-safe.
 *
 * POLÍTICA (tabela de decisão, em ordem de avaliação):
 *   1. Destino inexistente          → APLICAR_RECEBIDO (inserção vinda do outro lado)
 *   2. Recebido é tombstone,
 *      destino vivo                 → APLICAR_RECEBIDO (exclusão NUNCA ressuscita)
 *   3. Destino é tombstone,
 *      recebido vivo                → MANTER_DESTINO   (exclusão NUNCA ressuscita)
 *   4. Ambos tombstones             → JA_CONVERGIDO
 *   5. LWW por updatedAt            → mais recente vence
 *   6. Empate de updatedAt          → desempate DETERMINÍSTICO: deviceId
 *                                      menor (lexicográfico) vence
 *   7. Empate total                 → JA_CONVERGIDO
 *
 * CONFLITO vs vitória limpa: o 'horizonte' (last_sync da entidade) não muda
 * o vencedor — apenas CLASSIFICA. Se ambos os lados foram alterados após o
 * horizonte, a decisão vem rotulada como CONFLITO_* (engine deve logar +
 * publicar evento de auditoria). No bootstrap (horizonte = null) não existe
 * "conflito": é a união inicial, LWW decide limpo.
 *
 * USO NOS DOIS SENTIDOS (o método é simétrico):
 *   PULL  (remoto → local): destino = estado local atual,
 *                           recebido = estado vindo do MySQL,
 *                           horizonte = last_sync da entidade.
 *   PUSH  (upsert no remoto): destino = registro atual no MySQL,
 *                             recebido = estado local enviado,
 *                             horizonte = null (o remoto não mantém relógio;
 *                             conflitos são detectados por quem puxa).
 *
 * ⚠️ TRADE-OFF assumido (documentado na interação de design): exclusão é
 *    absoluta — edição offline posterior à exclusão de outro usuário é
 *    descartada, com rótulo CONFLITO_* para auditoria. Evolução futura
 *    possível: janela de tolerância por version. NÃO implementado.
 *
 * v1.0 - Criação. Snapshot desacoplado dos models (SyncSnapshot aninhado) —
 *        compilável e testável antes da migração UUID.
 */
@Component
public class ConflictResolver {

    // ============================================================
    // TIPOS DO DOMÍNIO DA DECISÃO
    // ============================================================

    /**
     * Estado mínimo de sincronização de um registro — extraído do model
     * pelo SyncEngine. Desacopla esta política dos models concretos.
     *
     * @param id        UUID do registro de negócio
     * @param updatedAt momento da última alteração (motor do LWW)
     * @param deletado  true = tombstone (exclusão lógica)
     * @param deviceId  origem da última alteração (desempate determinístico)
     */
    public record SyncSnapshot(
            String id,
            LocalDateTime updatedAt,
            boolean deletado,
            String deviceId
    ) {
        public SyncSnapshot {
            if (id == null || id.isBlank()) {
                throw new IllegalArgumentException("Snapshot exige id");
            }
            if (updatedAt == null) {
                throw new IllegalArgumentException("Snapshot exige updatedAt (motor do LWW)");
            }
        }
    }

    /** Tipos de decisão que o engine pode executar. */
    public enum TipoDecisao {
        /** Sobrescrever o destino com o estado recebido. */
        APLICAR_RECEBIDO,
        /** Destino permanece como está. */
        MANTER_DESTINO,
        /** Recebido venceu um CONFLITO real (ambos alterados pós-horizonte) —
         *  aplicar + auditoria obrigatória. */
        CONFLITO_RECEBIDO_VENCE,
        /** Destino venceu um CONFLITO real — manter + auditoria obrigatória
         *  (e o estado vencedor deve garantir travessia ao outro lado). */
        CONFLITO_DESTINO_VENCE,
        /** Estados equivalentes — nenhuma ação. */
        JA_CONVERGIDO
    }

    /**
     * Resultado da resolução.
     *
     * @param tipo   o que fazer
     * @param motivo explicação técnica (timestamps/devices) — vai para log/evento
     */
    public record Decisao(TipoDecisao tipo, String motivo) {
        static Decisao de(TipoDecisao tipo, String motivo) {
            return new Decisao(tipo, motivo);
        }
    }

    // ============================================================
    // RESOLUÇÃO
    // ============================================================

    /**
     * Decide o destino de um registro divergente conforme a política da classe.
     *
     * @param noDestino estado atual no lado que recebe a aplicação
     *                  (local no PULL; remoto no PUSH-upsert); null = inexistente
     * @param recebido  estado vindo do outro lado; null = nada a aplicar
     * @param horizonte last_sync da entidade (null no bootstrap — sem conflitos)
     * @return Decisao executável pelo engine
     */
    public Decisao resolver(SyncSnapshot noDestino, SyncSnapshot recebido, LocalDateTime horizonte) {

        // Nada chegou — destino intocado.
        if (recebido == null) {
            return Decisao.de(TipoDecisao.MANTER_DESTINO, "Nada recebido para o registro");
        }

        // Registro novo vindo do outro lado.
        if (noDestino == null) {
            return Decisao.de(TipoDecisao.APLICAR_RECEBIDO,
                    "Destino inexistente — inserindo " + resumo(recebido));
        }

        // ---- Regras absolutas de tombstone (exclusão nunca ressuscita) ----
        if (recebido.deletado() && !noDestino.deletado()) {
            boolean destinoFoiEditado = alteradoApos(noDestino, horizonte);
            return destinoFoiEditado
                    ? Decisao.de(TipoDecisao.CONFLITO_RECEBIDO_VENCE,
                    "Exclusão remota prevalece sobre edição local (edição descartada) — "
                            + detalhe(noDestino, recebido))
                    : Decisao.de(TipoDecisao.APLICAR_RECEBIDO,
                    "Exclusão remota aplicada — " + detalhe(noDestino, recebido));
        }

        if (noDestino.deletado() && !recebido.deletado()) {
            boolean recebidoFoiEditado = alteradoApos(recebido, horizonte);
            return recebidoFoiEditado
                    ? Decisao.de(TipoDecisao.CONFLITO_DESTINO_VENCE,
                    "Exclusão local prevalece sobre edição remota (edição remota descartada) — "
                            + detalhe(noDestino, recebido))
                    : Decisao.de(TipoDecisao.MANTER_DESTINO,
                    "Exclusão local já registrada — " + detalhe(noDestino, recebido));
        }

        if (noDestino.deletado()) { // ambos tombstones
            return Decisao.de(TipoDecisao.JA_CONVERGIDO,
                    "Excluído nos dois lados — " + detalhe(noDestino, recebido));
        }

        // ---- LWW por updatedAt (ambos vivos) ----
        int comparacao = recebido.updatedAt().compareTo(noDestino.updatedAt());

        if (comparacao > 0) {
            boolean conflitoReal = alteradoApos(noDestino, horizonte);
            return conflitoReal
                    ? Decisao.de(TipoDecisao.CONFLITO_RECEBIDO_VENCE,
                    "Conflito (ambos alterados pós-horizonte): remoto mais novo venceu — "
                            + detalhe(noDestino, recebido))
                    : Decisao.de(TipoDecisao.APLICAR_RECEBIDO,
                    "Remoto mais novo — " + detalhe(noDestino, recebido));
        }

        if (comparacao < 0) {
            boolean conflitoReal = alteradoApos(recebido, horizonte);
            return conflitoReal
                    ? Decisao.de(TipoDecisao.CONFLITO_DESTINO_VENCE,
                    "Conflito (ambos alterados pós-horizonte): local mais novo venceu — "
                            + detalhe(noDestino, recebido))
                    : Decisao.de(TipoDecisao.MANTER_DESTINO,
                    "Destino mais novo — " + detalhe(noDestino, recebido));
        }

        // ---- Empate de timestamp: desempate DETERMINÍSTICO por deviceId ----
        String deviceDestino = normalizar(noDestino.deviceId());
        String deviceRecebido = normalizar(recebido.deviceId());

        if (deviceDestino.equals(deviceRecebido)) {
            return Decisao.de(TipoDecisao.JA_CONVERGIDO,
                    "Mesma origem e mesmo momento — estados idênticos: " + resumo(noDestino));
        }

        // Menor lexicográfico vence — QUALQUER máquina que avalie este par
        // chega ao MESMO vencedor (garantia de convergência distribuída).
        boolean recebidoVence = deviceRecebido.compareTo(deviceDestino) < 0;

        return recebidoVence
                ? Decisao.de(TipoDecisao.CONFLITO_RECEBIDO_VENCE,
                "Empate de timestamp — desempate por deviceId: recebido vence — "
                        + detalhe(noDestino, recebido))
                : Decisao.de(TipoDecisao.CONFLITO_DESTINO_VENCE,
                "Empate de timestamp — desempate por deviceId: destino vence — "
                        + detalhe(noDestino, recebido));
    }

    // ============================================================
    // AUXILIARES PUROS
    // ============================================================

    /** true se o snapshot foi alterado APÓS o horizonte (lado "sujo"). */
    private boolean alteradoApos(SyncSnapshot s, LocalDateTime horizonte) {
        return horizonte != null && s.updatedAt().isAfter(horizonte);
    }

    private String normalizar(String deviceId) {
        return deviceId == null ? "" : deviceId.trim();
    }

    private String resumo(SyncSnapshot s) {
        return "id=" + s.id() + ", updatedAt=" + s.updatedAt()
                + (s.deletado() ? " [TOMBSTONE]" : "");
    }

    private String detalhe(SyncSnapshot destino, SyncSnapshot recebido) {
        return "destino{id=" + destino.id() + ", at=" + destino.updatedAt()
                + ", dev=" + destino.deviceId()
                + (destino.deletado() ? ", TOMBSTONE" : "")
                + "} vs recebido{at=" + recebido.updatedAt()
                + ", dev=" + recebido.deviceId()
                + (recebido.deletado() ? ", TOMBSTONE" : "") + "}";
    }
}