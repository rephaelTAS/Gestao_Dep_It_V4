package com.ossobo.gestaoDepIt.db.config.event;



/**
 * TonerEvent - Evento para operações de toner
 * v1.0
 *
 * Usado com @EventListener para notificar mudanças em toners
 */
public record TonerEvent<T>(
        T data,
        String acao,          // INSTALADO, ATUALIZADO, EXCLUIDO, SUBSTITUIDO,
        // USO_REGISTRADO, PERCENTAGEM_ATUALIZADA, ESGOTADO
        String mensagem
) {
    public TonerEvent(T data, String acao) {
        this(data, acao, acao + " com sucesso");
    }

    public TonerEvent(T data, String acao, String mensagem) {
        this.data = data;
        this.acao = acao;
        this.mensagem = mensagem;
    }
}
