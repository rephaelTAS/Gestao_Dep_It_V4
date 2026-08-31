package com.ossobo.gestaoDepIt.db.config.event;

/**
 * ConexaoStatus v1.0
 *
 * Status possíveis da conexão.
 */
public enum ConexaoStatus {
    REMOTO("🟢 Conectado ao servidor remoto"),
    SQLITE("🟡 Usando SQLite local (offline)"),
    ERRO("🔴 Erro na conexão"),
    TESTANDO("🔄 Testando conexão...");

    private final String descricao;

    ConexaoStatus(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean isOnline() { return this == REMOTO; }
    public boolean isOffline() { return this == SQLITE; }
}