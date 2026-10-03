package com.ossobo.gestaoDepIt.db.relatorios;

import java.time.LocalDateTime;
import java.util.List;

/**
 * RelatorioHistorico v1.0
 *
 * Registro append-only de relatório gerado.
 * Fonte dos KPIs do Dashboard ("Relatórios Hoje", "Esta Semana",
 * "Tabelas Mais Usadas", "Visão Geral" no BarChart).
 *
 * Schema: relatorio_historico (id TEXT PRIMARY KEY — UUID v4)
 */
public record RelatorioHistorico(
        String id,
        String titulo,
        List<String> tabelas,
        String filtrosJson,
        LocalDateTime geradoEm,
        String deviceId
) {
    public RelatorioHistorico {
        if (id == null || id.isBlank())
            throw new IllegalArgumentException("ID é obrigatório");
        if (titulo == null || titulo.isBlank())
            throw new IllegalArgumentException("Título é obrigatório");
        tabelas = tabelas == null ? List.of() : List.copyOf(tabelas);
        if (filtrosJson == null || filtrosJson.isBlank()) filtrosJson = "{}";
        if (geradoEm == null) geradoEm = LocalDateTime.now();
        if (deviceId == null) deviceId = "";
    }

    public String tabelasCsv() { return String.join(",", tabelas); }

    public static RelatorioHistorico novo(String titulo, List<String> tabelas, String filtrosJson) {
        return new RelatorioHistorico(
                java.util.UUID.randomUUID().toString(),
                titulo, tabelas, filtrosJson, LocalDateTime.now(), null);
    }
}