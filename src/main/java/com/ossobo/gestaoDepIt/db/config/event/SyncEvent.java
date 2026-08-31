package com.ossobo.gestaoDepIt.db.config.event;

import java.time.LocalDateTime;

/**
 * SyncEvent v1.0
 *
 * Evento disparado durante o processo de sincronização.
 */
public class SyncEvent {

    public enum Type {
        INICIADO,
        PROGRESSO,
        CONCLUIDO,
        FALHOU,
        PAUSADO,
        RETOMADO
    }

    private final Type type;
    private final String entidade;
    private final int totalItems;
    private final int processados;
    private final int falhas;
    private final double progresso;
    private final LocalDateTime timestamp;
    private final String mensagem;

    public SyncEvent(Type type, String entidade, int totalItems,
                     int processados, int falhas, String mensagem) {
        this.type = type;
        this.entidade = entidade;
        this.totalItems = totalItems;
        this.processados = processados;
        this.falhas = falhas;
        this.progresso = totalItems > 0 ? (double) processados / totalItems * 100 : 0;
        this.timestamp = LocalDateTime.now();
        this.mensagem = mensagem;
    }

    // Getters
    public Type getType() { return type; }
    public String getEntidade() { return entidade; }
    public int getTotalItems() { return totalItems; }
    public int getProcessados() { return processados; }
    public int getFalhas() { return falhas; }
    public double getProgresso() { return progresso; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getMensagem() { return mensagem; }

    // Conveniência
    public boolean isConcluido() { return type == Type.CONCLUIDO; }
    public boolean isEmAndamento() { return type == Type.INICIADO || type == Type.PROGRESSO; }
    public boolean isFalha() { return type == Type.FALHOU; }

    @Override
    public String toString() {
        return String.format("[%s] %s: %d/%d (%.1f%%) - %s",
                type, entidade, processados, totalItems, progresso, mensagem);
    }
}