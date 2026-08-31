package com.ossobo.gestaoDepIt.db.config.event;

import com.ossobo.gestaoDepIt.db.models.ConfigServidorRemoto;

/**
 * ConfigEvent - Evento para operações de configuração de servidor
 * v1.0
 *
 * Usado com @EventListener para notificar mudanças na configuração
 */
public record ConfigEvent(
        ConfigServidorRemoto config,
        String acao,          // CRIADO, ATUALIZADO, EXCLUIDO, ATIVADA, CONECTADO, DESCONECTADO
        String mensagem
) {
    public ConfigEvent(ConfigServidorRemoto config, String acao) {
        this(config, acao, acao + " com sucesso");
    }

    public ConfigEvent(ConfigServidorRemoto config, String acao, String mensagem) {
        this.config = config;
        this.acao = acao;
        this.mensagem = mensagem;
    }
}
