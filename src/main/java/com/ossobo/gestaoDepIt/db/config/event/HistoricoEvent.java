package com.ossobo.gestaoDepIt.db.config.event;



/**
 * HistoricoEvent - Evento para operações de histórico
 * v1.0
 *
 * Usado com @EventListener para notificar mudanças no histórico
 */
public record HistoricoEvent<T>(
        T data,
        String tipoEvento,    // CRIACAO, ATUALIZACAO, BAIXA, EXCLUSAO, etc.
        String mensagem
) {
    public HistoricoEvent(T data, String tipoEvento) {
        this(data, tipoEvento, "Evento " + tipoEvento + " registrado com sucesso");
    }

    public HistoricoEvent(T data, String tipoEvento, String mensagem) {
        this.data = data;
        this.tipoEvento = tipoEvento;
        this.mensagem = mensagem;
    }
}