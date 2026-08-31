package com.ossobo.gestaoDepIt.db.config.event;


/**
 * FuncionarioEvent - Evento para operações de funcionários
 * v1.0
 *
 * Usado com @EventListener para notificar mudanças em funcionários
 */
public record FuncionarioEvent<T>(
        T data,
        String acao,          // CRIADO, ATUALIZADO, EXCLUIDO, ATIVADO, DESATIVADO,
        // IMAGEM_ATUALIZADA, IMAGEM_REMOVIDA, TRANSFERIDO, PROMOVIDO
        String mensagem
) {
    public FuncionarioEvent(T data, String acao) {
        this(data, acao, acao + " com sucesso");
    }

    public FuncionarioEvent(T data, String acao, String mensagem) {
        this.data = data;
        this.acao = acao;
        this.mensagem = mensagem;
    }
}