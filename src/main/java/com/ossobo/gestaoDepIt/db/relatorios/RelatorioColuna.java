package com.ossobo.gestaoDepIt.db.relatorios;

/**
 * RelatorioColuna v1.0
 *
 * Peça LEGO: descreve uma coluna selecionada no relatório.
 * - tabela:  alias lógico da tabela ("inventario", "catalogo", "funcionarios"...)
 * - coluna:  nome físico da coluna no schema SQLite
 * - rotulo:  título exibido no cabeçalho (opcional — default = coluna)
 *
 * Imutável. Serializado para CSV simples "tabela.coluna" (rotulo omitido).
 */
public record RelatorioColuna(
        String tabela,
        String coluna,
        String rotulo
) {

    public RelatorioColuna {
        if (tabela == null || tabela.isBlank())
            throw new IllegalArgumentException("Tabela da coluna é obrigatória");
        if (coluna == null || coluna.isBlank())
            throw new IllegalArgumentException("Coluna é obrigatória");
        if (rotulo == null || rotulo.isBlank()) rotulo = coluna;
    }

    /** "inventario.num_serie" — formato canônico CSV. */
    public String paraCsv() {
        return tabela + "." + coluna;
    }

    /** Reverte "inventario.num_serie" → RelatorioColuna com rótulo default. */
    public static RelatorioColuna deCsv(String csv) {
        if (csv == null || !csv.contains("."))
            throw new IllegalArgumentException("CSV de coluna inválido: " + csv);
        String[] partes = csv.split("\\.", 2);
        return new RelatorioColuna(partes[0], partes[1], partes[1]);
    }
}