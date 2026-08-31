package com.ossobo.gestaoDepIt.db.config.event;

/**
 * CatalogoEvent - Evento para operações de catálogo
 * v1.0
 *
 * Usado com @EventListener para notificar mudanças no catálogo
 */
public record CatalogoEvent<T>(
        T data,
        String acao,          // CRIADO, ATUALIZADO, EXCLUIDO, ESTOQUE_ATUALIZADO
        String mensagem
) {
    public CatalogoEvent(T data, String acao) {
        this(data, acao, acao + " com sucesso");
    }

    public CatalogoEvent(T data, String acao, String mensagem) {
        this.data = data;
        this.acao = acao;
        this.mensagem = mensagem;
    }
}