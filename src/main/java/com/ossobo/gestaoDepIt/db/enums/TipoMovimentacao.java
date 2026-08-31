package com.ossobo.gestaoDepIt.db.enums;



public enum TipoMovimentacao {
    ENTRADA("Entrada"),
    SAIDA("Saída"),
    AJUSTE("Ajuste"),
    RESERVA("Reserva");

    private final String descricao;

    private TipoMovimentacao(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    @Override
    public String toString() {
        return descricao;
    }
}