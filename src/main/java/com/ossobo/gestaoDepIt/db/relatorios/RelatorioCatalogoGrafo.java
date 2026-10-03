package com.ossobo.gestaoDepIt.db.relatorios;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * RelatorioCatalogoGrafo v1.0
 *
 * FONTE ÚNICA dos JOINs permitidos no relatório Lego.
 * Não é uma verdade do schema (não há FK — Letra J), é uma verdade do DOMÍNIO.
 *
 * Cada alias representa uma tabela sincronizada. Cada aresta é uma condição
 * de igualdade entre chaves de negócio ou identidade.
 *
 * O RelatorioBuilderService consulta este grafo ANTES de montar SQL. Qualquer
 * JOIN fora do grafo é rejeitado com IllegalArgumentException — defesa em
 * profundidade contra SQL injection e contra composições inválidas.
 */
public final class RelatorioCatalogoGrafo {

    private RelatorioCatalogoGrafo() {}

    /** Aresta: "de.alias = para.alias" com condição explícita. */
    public record Aresta(String de, String para, String condicao) {}

    /** Aliases → tabela física no SQLite. */
    private static final Map<String, String> ALIAS_PARA_TABELA = Map.of(
            "inventario",   "inventario_equipamentos",
            "catalogo",     "catalogo_produtos",
            "funcionarios", "funcionarios",
            "usuarios",     "usuarios",
            "movimentacoes","estoque_movimentacoes",
            "toners",       "gestao_toners",
            "historico",    "historico_eventos"
    );

    /** Arestas permitidas (10). Fonte única para validação e para a UI. */
    private static final List<Aresta> ARESTAS = List.of(
            new Aresta("inventario", "catalogo",
                    "inventario.sku_produto = catalogo.sku"),
            new Aresta("inventario", "funcionarios",
                    "inventario.funcionario_id = funcionarios.cod_dep"),
            new Aresta("inventario", "toners",
                    "inventario.id = toners.inventario_id"),
            new Aresta("catalogo", "movimentacoes",
                    "catalogo.sku = movimentacoes.sku_produto"),
            new Aresta("catalogo", "historico",
                    "catalogo.sku = historico.sku_produto"),
            new Aresta("funcionarios", "movimentacoes",
                    "funcionarios.cod_dep = movimentacoes.funcionario_id"),
            new Aresta("funcionarios", "historico",
                    "funcionarios.cod_dep = historico.funcionario_id"),
            new Aresta("funcionarios", "usuarios",
                    "funcionarios.cod_dep = usuarios.funcionario_id"),
            new Aresta("movimentacoes", "usuarios",
                    "movimentacoes.funcionario_id = usuarios.funcionario_id"),
            new Aresta("toners", "catalogo",
                    "toners.sku_produto = catalogo.sku")
    );

    public static Set<String> aliasesValidos() {
        return ALIAS_PARA_TABELA.keySet();
    }

    public static String tabelaFisica(String alias) {
        String t = ALIAS_PARA_TABELA.get(alias);
        if (t == null) throw new IllegalArgumentException("Alias desconhecido: " + alias);
        return t;
    }

    public static List<Aresta> arestas() {
        return ARESTAS;
    }

    /** Aresta direta (de → para) ou inversa (para → de), se existir. */
    public static Optional<Aresta> arestaEntre(String a, String b) {
        return ARESTAS.stream()
                .filter(ar -> (ar.de().equals(a) && ar.para().equals(b))
                        || (ar.de().equals(b) && ar.para().equals(a)))
                .findFirst();
    }

    /** Lista os aliases diretamente conectáveis a "origem". */
    public static List<String> vizinhos(String origem) {
        return ARESTAS.stream()
                .flatMap(ar -> {
                    if (ar.de().equals(origem)) return java.util.stream.Stream.of(ar.para());
                    if (ar.para().equals(origem)) return java.util.stream.Stream.of(ar.de());
                    return java.util.stream.Stream.empty();
                })
                .distinct().toList();
    }
}