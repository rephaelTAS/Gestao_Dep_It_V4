package com.ossobo.gestaoDepIt.db.config.event;


/**
 * UsuarioEvent - Evento para operações de usuário
 * v1.0
 *
 * Usado com @EventListener para notificar mudanças em usuários
 */
public record UsuarioEvent<T>(
        T data,
        String acao,          // CRIADO, ATUALIZADO, EXCLUIDO, ATIVADO, DESATIVADO,
        // NIVEL_ALTERADO, LOGIN, LOGOUT
        String mensagem
) {
    public UsuarioEvent(T data, String acao) {
        this(data, acao, acao + " com sucesso");
    }

    public UsuarioEvent(T data, String acao, String mensagem) {
        this.data = data;
        this.acao = acao;
        this.mensagem = mensagem;
    }
}
