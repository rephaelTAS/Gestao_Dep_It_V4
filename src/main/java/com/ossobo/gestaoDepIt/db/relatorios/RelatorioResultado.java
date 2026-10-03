package com.ossobo.gestaoDepIt.db.relatorios;

import java.util.List;
import java.util.Map;

/**
 * RelatorioResultado v1.0
 *
 * Envelope do resultado de uma consulta dinâmica.
 * - colunas:  ordem canônica das colunas (para POI e TableView)
 * - linhas:   cada linha é um Map<colunaFisica, valor>, imutável
 * - total:    número de linhas
 * - sql:      SQL efetivamente executado (auditoria + debug)
 *
 * Auto-descritivo: POI monta header a partir de "colunas", TableView
 * monta TableColumns dinâmicos a partir de "colunas".
 */
public record RelatorioResultado(
        List<String> colunas,
        List<Map<String, Object>> linhas,
        int total,
        String sql
) {
    public RelatorioResultado {
        colunas = colunas == null ? List.of() : List.copyOf(colunas);
        linhas = linhas == null ? List.of() : List.copyOf(linhas);
        if (sql == null) sql = "";
        total = linhas.size();
    }

    public static RelatorioResultado vazio(String sql) {
        return new RelatorioResultado(List.of(), List.of(), 0, sql);
    }
}