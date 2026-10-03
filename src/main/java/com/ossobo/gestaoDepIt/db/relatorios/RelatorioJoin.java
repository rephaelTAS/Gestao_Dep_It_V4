package com.ossobo.gestaoDepIt.db.relatorios;

import java.util.Arrays;
import java.util.List;

/**
 * RelatorioJoin v1.0
 *
 * Peça LEGO: descreve um JOIN do relatório.
 * - tabela:   alias da tabela a juntar ("catalogo", "funcionarios", "toners"...)
 * - colunas:  colunas dessa tabela que entram no SELECT
 * - condicao: expressão "a.x = b.y" validada contra o grafo permitido
 *
 * Serialização: "tabela|col1,col2,col3|condicao" (CSV com pipes internos).
 * O validador do Service rejeita qualquer JOIN fora do RelatorioCatalogoGrafo.
 */
public record RelatorioJoin(
        String tabela,
        List<RelatorioColuna> colunas,
        String condicao
) {
    public RelatorioJoin {
        if (tabela == null || tabela.isBlank())
            throw new IllegalArgumentException("Tabela do JOIN é obrigatória");
        if (condicao == null || condicao.isBlank())
            throw new IllegalArgumentException("Condição do JOIN é obrigatória");
        colunas = colunas == null ? List.of() : List.copyOf(colunas);
    }

    /** "catalogo|catalogo.marca,catalogo.modelo|inventario.sku_produto = catalogo.sku" */
    public String paraCsv() {
        String cols = colunas.stream()
                .map(RelatorioColuna::paraCsv)
                .reduce((a, b) -> a + "," + b)
                .orElse("");
        return tabela + "|" + cols + "|" + condicao;
    }

    public static RelatorioJoin deCsv(String csv) {
        if (csv == null || csv.isBlank())
            throw new IllegalArgumentException("CSV de JOIN vazio");
        String[] partes = csv.split("\\|", -1);
        if (partes.length != 3)
            throw new IllegalArgumentException("CSV de JOIN inválido (esperado 3 partes): " + csv);
        List<RelatorioColuna> cols = partes[1].isBlank()
                ? List.of()
                : Arrays.stream(partes[1].split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(RelatorioColuna::deCsv)
                .toList();
        return new RelatorioJoin(partes[0], cols, partes[2]);
    }
}