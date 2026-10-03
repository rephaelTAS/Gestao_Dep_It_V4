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
 *
 * NOTA: FORA do sync — infra local por máquina.
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
    public ConfigServidorRemoto {
        if (nomeConfig == null || nomeConfig.isBlank()) {
            throw new IllegalArgumentException("Nome da configuração é obrigatório");
        }
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("Host é obrigatório");
        }
        if (porta == null || porta.isBlank()) {
            throw new IllegalArgumentException("Porta é obrigatória");
        }
        if (databaseName == null || databaseName.isBlank()) {
            throw new IllegalArgumentException("Nome do banco de dados é obrigatório");
        }
        if (usuario == null || usuario.isBlank()) {
            throw new IllegalArgumentException("Usuário é obrigatório");
        }
        if (senha == null || senha.isBlank()) {
            throw new IllegalArgumentException("Senha é obrigatória");
        }

        if (tipoBanco == null || tipoBanco.isBlank()) tipoBanco = "MYSQL";
        if (parametrosExtra == null) parametrosExtra = "";
        if (ativo == null) ativo = 1;
        if (statusConexao == null || statusConexao.isBlank()) statusConexao = "NAO_TESTADO";
    }

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

    public boolean isAtivo() {
        return ativo != null && ativo == 1;
    }

    public boolean isConectado() {
        return "CONECTADO".equals(statusConexao);
    }

    public boolean isTestando() {
        return "TESTANDO".equals(statusConexao);
    }

    public boolean isFalha() {
        return "FALHA".equals(statusConexao);
    }

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

    @Override
    public String toString() {
        return String.format("%s (%s@%s:%s/%s)",
                nomeConfig, usuario, host, porta, databaseName);
    }
}