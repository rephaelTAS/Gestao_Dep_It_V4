package com.ossobo.gestaoDepIt.db.relatorios;

import java.time.LocalDateTime;
import java.util.List;

/**
 * RelatorioModelo v1.0
 *
 * Peça LEGO persistida: receita completa de um relatório salvo.
 * - tabelaBase:  alias da tabela principal ("inventario", "catalogo"...)
 * - colunasBase: colunas da tabela base escolhidas
 * - joins:       lista de RelatorioJoin (0..N)
 * - filtrosJson: filtros serializados (combo status, período, etc.)
 *
 * Schema: relatorio_modelos (id TEXT PRIMARY KEY — UUID v4)
 *         nome UNIQUE (rótulo do usuário)
 */
public record RelatorioModelo(
        String id,
        String nome,
        String tabelaBase,
        List<RelatorioColuna> colunasBase,
        List<RelatorioJoin> joins,
        String filtrosJson,
        LocalDateTime criadoEm,
        String deviceId
) {
    public RelatorioModelo {
        if (id == null || id.isBlank())
            throw new IllegalArgumentException("ID é obrigatório");
        if (nome == null || nome.isBlank())
            throw new IllegalArgumentException("Nome do modelo é obrigatório");
        if (tabelaBase == null || tabelaBase.isBlank())
            throw new IllegalArgumentException("Tabela base é obrigatória");
        colunasBase = colunasBase == null ? List.of() : List.copyOf(colunasBase);
        joins = joins == null ? List.of() : List.copyOf(joins);
        if (filtrosJson == null || filtrosJson.isBlank()) filtrosJson = "{}";
        if (criadoEm == null) criadoEm = LocalDateTime.now();
        if (deviceId == null) deviceId = "";
    }

    public List<String> tabelasEnvolvidas() {
        List<String> todas = new java.util.ArrayList<>();
        todas.add(tabelaBase);
        joins.forEach(j -> todas.add(j.tabela()));
        return List.copyOf(todas);
    }

    public static RelatorioModelo novo(
            String nome,
            String tabelaBase,
            List<RelatorioColuna> colunasBase,
            List<RelatorioJoin> joins,
            String filtrosJson) {
        return new RelatorioModelo(
                java.util.UUID.randomUUID().toString(),
                nome, tabelaBase, colunasBase, joins, filtrosJson,
                LocalDateTime.now(), null);
    }
}