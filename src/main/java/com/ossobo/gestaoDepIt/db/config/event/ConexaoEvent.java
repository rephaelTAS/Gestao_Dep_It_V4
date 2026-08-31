package com.ossobo.gestaoDepIt.db.config.event;

import com.ossobo.gestaoDepIt.db.models.ConfigServidorRemoto;

import java.time.LocalDateTime;

/**
 * ConexaoEvent v2.0
 *
 * Evento disparado quando o status da conexão muda.
 * Usado com EventBus do WinterFX.
 *
 * v2.0 - Migrado para Java 17+ com suporte a Record
 * v1.1 - Adicionado campo mensagem para descrições ricas
 */
public class ConexaoEvent {

    public enum Type {
        MUDOU,      // Mudança de configuração
        FALHOU,     // Falha na conexão
        RESTAUROU   // Conexão restaurada
    }

    private final Type type;
    private final ConexaoStatus status;
    private final ConfigServidorRemoto config;
    private final String mensagem;
    private final LocalDateTime timestamp;

    // ============================================================
    // CONSTRUTORES
    // ============================================================

    /**
     * Construtor completo com mensagem.
     */
    public ConexaoEvent(Type type, ConexaoStatus status,
                        ConfigServidorRemoto config, String mensagem) {
        this.type = type;
        this.status = status;
        this.config = config;
        this.mensagem = mensagem;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Construtor sem mensagem (para compatibilidade).
     */
    public ConexaoEvent(Type type, ConexaoStatus status,
                        ConfigServidorRemoto config) {
        this(type, status, config, status.getDescricao());
    }

    // ============================================================
    // GETTERS
    // ============================================================

    public Type getType() { return type; }
    public ConexaoStatus getStatus() { return status; }
    public ConfigServidorRemoto getConfig() { return config; }
    public String getMensagem() { return mensagem; }
    public LocalDateTime getTimestamp() { return timestamp; }

    // ============================================================
    // MÉTODOS DE CONVENIÊNCIA
    // ============================================================

    public boolean isRemoto() {
        return status == ConexaoStatus.REMOTO;
    }

    public boolean isSqlite() {
        return status == ConexaoStatus.SQLITE;
    }

    public boolean isErro() {
        return status == ConexaoStatus.ERRO;
    }

    public String getDescricao() {
        return String.format("[%s] %s - %s", type, status, mensagem);
    }

    /**
     * Retorna informações do host para exibição na UI
     * ✅ CORRIGIDO: Usa acesso direto aos campos do Record
     */
    public String getHostInfo() {
        if (config != null) {
            // ✅ Record: acesso direto aos campos (sem get)
            return String.format("%s@%s:%s/%s",
                    config.usuario(),
                    config.host(),
                    config.porta(),
                    config.databaseName());
        }
        return "SQLite local";
    }

    /**
     * Retorna o nome da configuração
     * ✅ CORRIGIDO: Acesso direto ao campo do Record
     */
    public String getConfigName() {
        if (config != null) {
            return config.nomeConfig();
        }
        return "Local";
    }

    /**
     * Retorna o status da conexão como texto
     */
    public String getStatusText() {
        if (config != null) {
            String statusConexao = config.statusConexao();
            if (statusConexao != null) {
                return switch (statusConexao) {
                    case "CONECTADO" -> "Conectado";
                    case "FALHA" -> "Falha";
                    case "TESTANDO" -> "Testando...";
                    case "NAO_TESTADO" -> "Não testado";
                    default -> statusConexao;
                };
            }
        }
        return "Desconhecido";
    }

    /**
     * Verifica se a configuração está ativa
     */
    public boolean isConfigAtivo() {
        return config != null && config.isAtivo();
    }

    /**
     * Verifica se a configuração está conectada
     */
    public boolean isConfigConectado() {
        return config != null && config.isConectado();
    }

    @Override
    public String toString() {
        return getDescricao();
    }
}