// ===== ESTATISTICAS_DTO.java =====
package com.ossobo.gestaoDepIt.controllers.gestao.estoque.movimentacoes.dto;

import java.util.Map;

/**
 * DTO PARA ESTATÍSTICAS DE MOVIMENTAÇÕES
 * Propósito: Transferir dados estatísticos entre camadas
 * Princípio: Imutabilidade quando possível
 */
public class EstatisticasDTO {
    private final int totalRegistros;
    private final int totalEntradas;
    private final int totalSaidas;
    private final int totalAjustes;
    private final int totalReservas;
    private final int saldoAtual;
    private final int produtosDiferentes;
    private final int funcionariosEnvolvidos;
    private final String periodo;

    // ===== CONSTRUTOR PRIVADO (USAR BUILDER) =====
    private EstatisticasDTO(Builder builder) {
        this.totalRegistros = builder.totalRegistros;
        this.totalEntradas = builder.totalEntradas;
        this.totalSaidas = builder.totalSaidas;
        this.totalAjustes = builder.totalAjustes;
        this.totalReservas = builder.totalReservas;
        this.saldoAtual = builder.saldoAtual;
        this.produtosDiferentes = builder.produtosDiferentes;
        this.funcionariosEnvolvidos = builder.funcionariosEnvolvidos;
        this.periodo = builder.periodo;
    }

    // ===== GETTERS =====
    public int getTotalRegistros() { return totalRegistros; }
    public int getTotalEntradas() { return totalEntradas; }
    public int getTotalSaidas() { return totalSaidas; }
    public int getTotalAjustes() { return totalAjustes; }
    public int getTotalReservas() { return totalReservas; }
    public int getSaldoAtual() { return saldoAtual; }
    public int getProdutosDiferentes() { return produtosDiferentes; }
    public int getFuncionariosEnvolvidos() { return funcionariosEnvolvidos; }
    public String getPeriodo() { return periodo; }

    // ===== BUILDER PATTERN =====
    public static class Builder {
        private int totalRegistros;
        private int totalEntradas;
        private int totalSaidas;
        private int totalAjustes;
        private int totalReservas;
        private int saldoAtual;
        private int produtosDiferentes;
        private int funcionariosEnvolvidos;
        private String periodo = "Todos";

        public Builder totalRegistros(int totalRegistros) {
            this.totalRegistros = totalRegistros;
            return this;
        }

        public Builder totalEntradas(int totalEntradas) {
            this.totalEntradas = totalEntradas;
            return this;
        }

        public Builder totalSaidas(int totalSaidas) {
            this.totalSaidas = totalSaidas;
            return this;
        }

        public Builder totalAjustes(int totalAjustes) {
            this.totalAjustes = totalAjustes;
            return this;
        }

        public Builder totalReservas(int totalReservas) {
            this.totalReservas = totalReservas;
            return this;
        }

        public Builder saldoAtual(int saldoAtual) {
            this.saldoAtual = saldoAtual;
            return this;
        }

        public Builder produtosDiferentes(int produtosDiferentes) {
            this.produtosDiferentes = produtosDiferentes;
            return this;
        }

        public Builder funcionariosEnvolvidos(int funcionariosEnvolvidos) {
            this.funcionariosEnvolvidos = funcionariosEnvolvidos;
            return this;
        }

        public Builder periodo(String periodo) {
            this.periodo = periodo;
            return this;
        }

        public EstatisticasDTO build() {
            validate();
            return new EstatisticasDTO(this);
        }

        private void validate() {
            if (totalRegistros < 0) {
                throw new IllegalArgumentException("Total de registros não pode ser negativo");
            }
            if (saldoAtual != (totalEntradas - totalSaidas + totalAjustes)) {
                // Apenas log warning, não throw - pode ser por filtros
                System.err.println("Warning: Saldo inconsistente com entradas/saídas/ajustes");
            }
        }
    }

    // ===== MÉTODO FROM_MAP (PARA CONVERSÃO DO SERVICE) =====
    public static EstatisticasDTO fromMap(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return empty();
        }

        return new Builder()
                .totalRegistros(((Number) map.getOrDefault("totalRegistros", 0)).intValue())
                .totalEntradas(((Number) map.getOrDefault("totalEntradas", 0)).intValue())
                .totalSaidas(((Number) map.getOrDefault("totalSaidas", 0)).intValue())
                .totalAjustes(((Number) map.getOrDefault("totalAjustes", 0)).intValue())
                .totalReservas(((Number) map.getOrDefault("totalReservas", 0)).intValue())
                .saldoAtual(((Number) map.getOrDefault("saldoAtual", 0)).intValue())
                .produtosDiferentes(((Number) map.getOrDefault("produtosDiferentes", 0)).intValue())
                .funcionariosEnvolvidos(((Number) map.getOrDefault("funcionariosEnvolvidos", 0)).intValue())
                .periodo((String) map.getOrDefault("periodo", "Todos"))
                .build();
    }

    // ===== MÉTODO EMPTY (PATTERN) =====
    public static EstatisticasDTO empty() {
        return new Builder().build();
    }

    // ===== MÉTODOS DE UTILIDADE =====
    public boolean isEmpty() {
        return totalRegistros == 0 && totalEntradas == 0 && totalSaidas == 0;
    }

    public double getPercentualEntradas() {
        if (totalEntradas == 0) return 0.0;
        return (double) totalEntradas / (totalEntradas + totalSaidas) * 100;
    }

    public double getPercentualSaidas() {
        if (totalSaidas == 0) return 0.0;
        return (double) totalSaidas / (totalEntradas + totalSaidas) * 100;
    }

    public String getSaldoFormatado() {
        if (saldoAtual > 0) {
            return String.format("+%d", saldoAtual);
        } else if (saldoAtual < 0) {
            return String.format("%d", saldoAtual);
        }
        return "0";
    }

    // ===== TOSTRING PARA DEBUG =====
    @Override
    public String toString() {
        return String.format(
                "Estatisticas[Registros=%d, Entradas=%d, Saídas=%d, Saldo=%d, Período=%s]",
                totalRegistros, totalEntradas, totalSaidas, saldoAtual, periodo
        );
    }
}