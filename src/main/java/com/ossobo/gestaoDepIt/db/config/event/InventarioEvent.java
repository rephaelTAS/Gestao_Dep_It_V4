package com.ossobo.gestaoDepIt.db.config.event;


/**
 * InventarioEvent - Evento para operações de inventário
 * v1.0
 *
 * Usado com @EventListener para notificar mudanças no inventário
 */
public record InventarioEvent<T>(
        T data,
        String acao,          // CADASTRADO, ATUALIZADO, EXCLUIDO, STATUS_ALTERADO,
        // CONDICAO_ALTERADA, BAIXADO, MANUTENCAO_INICIADA,
        // MANUTENCAO_FINALIZADA, LOCALIZACAO_ALTERADA,
        // FUNCIONARIO_REASSOCIADO, VERIFICACAO_REGISTRADA
        String mensagem
) {
    public InventarioEvent(T data, String acao) {
        this(data, acao, acao + " com sucesso");
    }

    public InventarioEvent(T data, String acao, String mensagem) {
        this.data = data;
        this.acao = acao;
        this.mensagem = mensagem;
    }
}
