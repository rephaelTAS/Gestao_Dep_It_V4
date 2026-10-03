package com.ossobo.gestaoDepIt.db.sync;

import com.ossobo.winterfx.anotations.Component;

import java.time.LocalDateTime;

/**
 * ConflictResolver v1.1
 *
 * Responsabilidade: Decidir o destino de um registro quando dois estados
 *                   divergem (local × remoto). POLÍTICA PURA — zero I/O,
 *                   zero banco, zero framework. Estado imutável →
 *                   inerentemente thread-safe.
 *
 * POLÍTICA (tabela de decisão, em ordem de avaliação):
 *   1. Destino inexistente          → APLICAR_RECEBIDO
 *   2. Recebido é tombstone,
 *      destino vivo                 → APLICAR_RECEBIDO (exclusão NUNCA ressuscita)
 *   3. Destino é tombstone,
 *      recebido vivo                → MANTER_DESTINO
 *   4. Ambos tombstones             → JA_CONVERGIDO
 *   5. LWW por updatedAt            → mais recente vence
 *   6. Empate de updatedAt          → desempate por deviceId
 *   7. Empate total                 → JA_CONVERGIDO
 *
 * v1.1 - Patch: desempate por deviceId no bootstrap retorna decisão LIMPA.
 * v1.0 - Criação.
 */
@Component
public class ConflictResolver {

    private static final System.Logger LOGGER = System.getLogger(ConflictResolver.class.getName());

    // ============================================================
    // TIPOS DO DOMÍNIO DA DECISÃO
    // ============================================================

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
            if (deviceId == null) deviceId = "";
        }
    }

    public enum TipoDecisao {
        APLICAR_RECEBIDO,
        MANTER_DESTINO,
        CONFLITO_RECEBIDO_VENCE,
        CONFLITO_DESTINO_VENCE,
        JA_CONVERGIDO
    }

    public record Decisao(TipoDecisao tipo, String motivo) {
        static Decisao de(TipoDecisao tipo, String motivo) {
            return new Decisao(tipo, motivo);
        }
    }

    // ============================================================
    // RESOLUÇÃO
    // ============================================================

    public Decisao resolver(SyncSnapshot noDestino, SyncSnapshot recebido, LocalDateTime horizonte) {

        if (recebido == null) {
            return Decisao.de(TipoDecisao.MANTER_DESTINO, "Nada recebido para o registro");
        }

        if (noDestino == null) {
            return Decisao.de(TipoDecisao.APLICAR_RECEBIDO,
                    "Destino inexistente — inserindo " + resumo(recebido));
        }

        // ---- Regras absolutas de tombstone ----
        if (recebido.deletado() && !noDestino.deletado()) {
            boolean destinoFoiEditado = alteradoApos(noDestino, horizonte);
            return destinoFoiEditado
                    ? Decisao.de(TipoDecisao.CONFLITO_RECEBIDO_VENCE,
                    "Exclusão remota prevalece sobre edição local — " + detalhe(noDestino, recebido))
                    : Decisao.de(TipoDecisao.APLICAR_RECEBIDO,
                    "Exclusão remota aplicada — " + detalhe(noDestino, recebido));
        }

        if (noDestino.deletado() && !recebido.deletado()) {
            boolean recebidoFoiEditado = alteradoApos(recebido, horizonte);
            return recebidoFoiEditado
                    ? Decisao.de(TipoDecisao.CONFLITO_DESTINO_VENCE,
                    "Exclusão local prevalece sobre edição remota — " + detalhe(noDestino, recebido))
                    : Decisao.de(TipoDecisao.MANTER_DESTINO,
                    "Exclusão local já registrada — " + detalhe(noDestino, recebido));
        }

        if (noDestino.deletado()) {
            return Decisao.de(TipoDecisao.JA_CONVERGIDO,
                    "Excluído nos dois lados — " + detalhe(noDestino, recebido));
        }

        // ---- LWW por updatedAt ----
        int comparacao = recebido.updatedAt().compareTo(noDestino.updatedAt());

        if (comparacao > 0) {
            boolean conflitoReal = alteradoApos(noDestino, horizonte);
            return conflitoReal
                    ? Decisao.de(TipoDecisao.CONFLITO_RECEBIDO_VENCE,
                    "Conflito: remoto mais novo venceu — " + detalhe(noDestino, recebido))
                    : Decisao.de(TipoDecisao.APLICAR_RECEBIDO,
                    "Remoto mais novo — " + detalhe(noDestino, recebido));
        }

        if (comparacao < 0) {
            boolean conflitoReal = alteradoApos(recebido, horizonte);
            return conflitoReal
                    ? Decisao.de(TipoDecisao.CONFLITO_DESTINO_VENCE,
                    "Conflito: local mais novo venceu — " + detalhe(noDestino, recebido))
                    : Decisao.de(TipoDecisao.MANTER_DESTINO,
                    "Destino mais novo — " + detalhe(noDestino, recebido));
        }

        // ---- Empate de timestamp: desempate por deviceId ----
        String deviceDestino = normalizar(noDestino.deviceId());
        String deviceRecebido = normalizar(recebido.deviceId());

        if (deviceDestino.equals(deviceRecebido)) {
            return Decisao.de(TipoDecisao.JA_CONVERGIDO,
                    "Mesma origem e mesmo momento — " + resumo(noDestino));
        }

        boolean recebidoVence = deviceRecebido.compareTo(deviceDestino) < 0;
        boolean conflitoReal = alteradoApos(noDestino, horizonte);

        if (conflitoReal) {
            return Decisao.de(
                    recebidoVence ? TipoDecisao.CONFLITO_RECEBIDO_VENCE : TipoDecisao.CONFLITO_DESTINO_VENCE,
                    "Empate de timestamp — deviceId desempatou (conflito pós-horizonte): "
                            + (recebidoVence ? "recebido" : "destino") + " vence — " + detalhe(noDestino, recebido));
        }

        // Bootstrap/fora do horizonte: decisão LIMPA
        return Decisao.de(
                recebidoVence ? TipoDecisao.APLICAR_RECEBIDO : TipoDecisao.MANTER_DESTINO,
                "Empate de timestamp — deviceId desempatou (sem conflito) — "
                        + detalhe(noDestino, recebido));
    }

    // ============================================================
    // AUXILIARES
    // ============================================================

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