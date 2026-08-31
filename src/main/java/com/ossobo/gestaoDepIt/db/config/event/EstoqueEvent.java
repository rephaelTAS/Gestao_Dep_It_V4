package com.ossobo.gestaoDepIt.db.config.event;



/**
 * EstoqueEvent - Evento para operações de estoque
 * v1.0
 *
 * Usado com @EventListener para notificar mudanças no estoque
 */
public record EstoqueEvent<T>(
        T data,
        String acao,          // ENTRADA, SAIDA, AJUSTE, RESERVA
        String mensagem
) {
    public EstoqueEvent(T data, String acao) {
        this(data, acao, acao + " registrada com sucesso");
    }

    public EstoqueEvent(T data, String acao, String mensagem) {
        this.data = data;
        this.acao = acao;
        this.mensagem = mensagem;
    }
}