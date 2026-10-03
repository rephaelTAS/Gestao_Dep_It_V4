package com.ossobo.gestaoDepIt.controlls.relatorio.avancado;

import com.ossobo.gestaoDepIt.db.relatorios.RelatorioCatalogoGrafo;
import com.ossobo.gestaoDepIt.db.relatorios.RelatorioColuna;
import com.ossobo.gestaoDepIt.db.relatorios.RelatorioJoin;
import com.ossobo.gestaoDepIt.db.relatorios.RelatorioModelo;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * AvancadoLegoBuilder v1.0
 *
 * Monta um RelatorioModelo a partir do estado da UI.
 * Responsabilidade única: transformar (base + colunas + joins + filtros)
 * em um record válido que o Builder do backend aceita.
 *
 * Se a UI não forneceu colunas para base nem para nenhum JOIN → null.
 */
public final class AvancadoLegoBuilder {

    private static final Gson GSON = new Gson();

    private AvancadoLegoBuilder() {}

    public static RelatorioModelo construir(AvancadoState state, AvancadoFilters filters) {
        if (state == null) return null;
        String baseAlias = state.getBaseAlias();
        if (baseAlias == null || baseAlias.isBlank()) return null;

        // 1) Colunas da base
        List<RelatorioColuna> colunasBase = new ArrayList<>();
        for (String col : state.getColunasBaseMarcadas()) {
            colunasBase.add(new RelatorioColuna(baseAlias, col, col));
        }

        // 2) JOINs ativos
        List<RelatorioJoin> joins = new ArrayList<>();
        for (var entry : state.getJoinsAtivos().entrySet()) {
            String aliasJoin = entry.getKey();
            Set<String> marcadas = entry.getValue();
            if (marcadas.isEmpty()) continue;

            // Condição vem do grafo — fonte única
            String condicao = RelatorioCatalogoGrafo.arestaEntre(baseAlias, aliasJoin)
                    .map(RelatorioCatalogoGrafo.Aresta::condicao)
                    .orElse(null);
            if (condicao == null) continue; // JOIN inválido — ignorar

            List<RelatorioColuna> cols = new ArrayList<>();
            for (String col : marcadas) {
                cols.add(new RelatorioColuna(aliasJoin, col, col));
            }
            joins.add(new RelatorioJoin(aliasJoin, cols, condicao));
        }

        // 3) Nada selecionado → aborta
        if (colunasBase.isEmpty() && joins.isEmpty()) return null;

        // 4) Filtros → JSON
        String filtrosJson = GSON.toJson(filters == null ? java.util.Map.of()
                : filters.paraMapa(baseAlias));

        // 5) Monta record com ID/nome temporários (não é persistido)
        return new RelatorioModelo(
                "temp-" + java.util.UUID.randomUUID(),
                "Relatório temporário",
                baseAlias,
                colunasBase,
                joins,
                filtrosJson,
                java.time.LocalDateTime.now(),
                null
        );
    }
}