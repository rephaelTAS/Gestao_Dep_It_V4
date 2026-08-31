package com.ossobo.gestaoDepIt.db.models;

import java.time.LocalDateTime;

/**
 * ConfigServidorRemoto - Configuração de servidor remoto (Record imutável)
 * v2.0 - Migrado para Record com validação compacta
 *
 * Schema: config_servidor_remoto
 *
 * Responsabilidades:
 * - Armazenar configurações de conexão com BD remoto
 * - Suporte para MySQL, PostgreSQL, Oracle, SQLServer, MariaDB
 */
public record ConfigServidorRemoto(
        Long id,
        String nomeConfig,
        String tipoBanco,          // MYSQL, POSTGRESQL, ORACLE, SQLSERVER, MARIADB
        String host,
        String porta,
        String databaseName,
        String usuario,
        String senha,
        String parametrosExtra,
        Integer ativo,
        String ultimaConexao,
        String statusConexao,      // CONECTADO, FALHA, TESTANDO, NAO_TESTADO
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    // ===== CONSTRUTOR COMPACTO (VALIDAÇÃO) =====
    public ConfigServidorRemoto {
        if (!(nomeConfig instanceof String n) || n.isBlank()) {
            throw new IllegalArgumentException("Nome da configuração é obrigatório");
        }
        if (!(host instanceof String h) || h.isBlank()) {
            throw new IllegalArgumentException("Host é obrigatório");
        }
        if (!(porta instanceof String p) || p.isBlank()) {
            throw new IllegalArgumentException("Porta é obrigatória");
        }
        if (!(databaseName instanceof String db) || db.isBlank()) {
            throw new IllegalArgumentException("Nome do banco de dados é obrigatório");
        }
        if (!(usuario instanceof String u) || u.isBlank()) {
            throw new IllegalArgumentException("Usuário é obrigatório");
        }
        if (!(senha instanceof String s) || s.isBlank()) {
            throw new IllegalArgumentException("Senha é obrigatória");
        }

        // Valores padrão
        if (tipoBanco == null || tipoBanco.isBlank()) {
            tipoBanco = "MYSQL";
        }
        if (parametrosExtra == null) {
            parametrosExtra = "";
        }
        if (ativo == null) {
            ativo = 1;
        }
        if (statusConexao == null || statusConexao.isBlank()) {
            statusConexao = "NAO_TESTADO";
        }
    }

    // ===== CONSTRUTOR DE FÁBRICA =====
    public static ConfigServidorRemoto novo(
            String nomeConfig,
            String tipoBanco,
            String host,
            String porta,
            String databaseName,
            String usuario,
            String senha,
            String parametrosExtra
    ) {
        return new ConfigServidorRemoto(
                null, nomeConfig, tipoBanco, host, porta,
                databaseName, usuario, senha, parametrosExtra,
                1, null, "NAO_TESTADO", null, null
        );
    }

    // ===== MÉTODOS DE NEGÓCIO =====

    /**
     * Gera a URL de conexão baseada no tipo de banco.
     */
    public String gerarUrlConexao() {
        return switch (tipoBanco) {
            case "MYSQL", "MARIADB" ->
                    String.format("jdbc:%s://%s:%s/%s",
                            tipoBanco.toLowerCase(), host, porta, databaseName);
            case "POSTGRESQL" ->
                    String.format("jdbc:postgresql://%s:%s/%s", host, porta, databaseName);
            case "SQLSERVER" ->
                    String.format("jdbc:sqlserver://%s:%s;databaseName=%s", host, porta, databaseName);
            case "ORACLE" ->
                    String.format("jdbc:oracle:thin:@%s:%s:%s", host, porta, databaseName);
            default ->
                    String.format("jdbc:%s://%s:%s/%s", tipoBanco, host, porta, databaseName);
        };
    }

    /**
     * Verifica se a configuração está ativa.
     */
    public boolean isAtivo() {
        return ativo != null && ativo == 1;
    }

    /**
     * Verifica se a configuração está conectada.
     */
    public boolean isConectado() {
        return "CONECTADO".equals(statusConexao);
    }

    /**
     * Verifica se está em teste.
     */
    public boolean isTestando() {
        return "TESTANDO".equals(statusConexao);
    }

    /**
     * Verifica se falhou.
     */
    public boolean isFalha() {
        return "FALHA".equals(statusConexao);
    }

    // ===== MÉTODOS DE TRANSFORMAÇÃO =====

    public ConfigServidorRemoto comStatus(String novoStatus) {
        return new ConfigServidorRemoto(
                id, nomeConfig, tipoBanco, host, porta, databaseName,
                usuario, senha, parametrosExtra, ativo,
                null, novoStatus, createdAt, updatedAt
        );
    }

    public ConfigServidorRemoto comStatus(String novoStatus, String ultimaConexao) {
        return new ConfigServidorRemoto(
                id, nomeConfig, tipoBanco, host, porta, databaseName,
                usuario, senha, parametrosExtra, ativo,
                ultimaConexao, novoStatus, createdAt, updatedAt
        );
    }

    public ConfigServidorRemoto comAtivo(int novoAtivo) {
        return new ConfigServidorRemoto(
                id, nomeConfig, tipoBanco, host, porta, databaseName,
                usuario, senha, parametrosExtra, novoAtivo,
                ultimaConexao, statusConexao, createdAt, updatedAt
        );
    }

    public ConfigServidorRemoto comAtualizado(LocalDateTime data) {
        return new ConfigServidorRemoto(
                id, nomeConfig, tipoBanco, host, porta, databaseName,
                usuario, senha, parametrosExtra, ativo,
                ultimaConexao, statusConexao, createdAt, data
        );
    }

    @Override
    public String toString() {
        return String.format("%s (%s@%s:%s/%s)",
                nomeConfig, usuario, host, porta, databaseName);
    }
}