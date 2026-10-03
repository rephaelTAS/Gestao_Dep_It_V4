package com.ossobo.gestaoDepIt.controlls.relatorio.avancado;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * AvancadoState v1.0
 *
 * Estado completo do editor Lego:
 *   - baseAlias + colunasBase marcadas
 *   - joins ativos (Map<aliasJoin, Set<colunas>>)
 *   - resultado atual
 */
public final class AvancadoState {

    private String baseAlias = "";
    private List<String> colunasDisponiveisBase = List.of();
    private Set<String> colunasBaseMarcadas = new LinkedHashSet<>();

    /** aliasJoin → colunas marcadas dessa tabela. */
    private final Map<String, Set<String>> joinsAtivos = new LinkedHashMap<>();
    private final Map<String, List<String>> colunasDisponiveisPorJoin = new LinkedHashMap<>();

    private List<String> resultadoColunas = List.of();
    private List<Map<String, Object>> resultadoLinhas = List.of();

    // ---- base ----
    public String getBaseAlias() { return baseAlias; }
    public void setBaseAlias(String v) { this.baseAlias = v == null ? "" : v; }

    public List<String> getColunasDisponiveisBase() { return colunasDisponiveisBase; }
    public void setColunasDisponiveisBase(List<String> v) {
        this.colunasDisponiveisBase = v == null ? List.of() : List.copyOf(v);
    }

    public Set<String> getColunasBaseMarcadas() { return colunasBaseMarcadas; }
    /**
     * Guarda a referência direta do Set (NÃO copia).
     * O Set vem de AvancadoTableManager.popularCheckboxesColunas, que o
     * mantém sincronizado via listeners dos CheckBox — precisa ser o MESMO
     * objeto para o state enxergar as marcações em tempo real.
     */
    public void setColunasBaseMarcadas(Set<String> v) {
        this.colunasBaseMarcadas = v == null ? new LinkedHashSet<>() : v;
    }
    // ---- joins ----
    public Map<String, Set<String>> getJoinsAtivos() { return joinsAtivos; }

    public void ativarJoin(String alias, List<String> colunasDisponiveis) {
        joinsAtivos.computeIfAbsent(alias, k -> new LinkedHashSet<>());
        colunasDisponiveisPorJoin.put(alias, colunasDisponiveis == null ? List.of() : List.copyOf(colunasDisponiveis));
    }

    public void desativarJoin(String alias) {
        joinsAtivos.remove(alias);
        colunasDisponiveisPorJoin.remove(alias);
    }

    public List<String> colunasDisponiveisDoJoin(String alias) {
        return colunasDisponiveisPorJoin.getOrDefault(alias, List.of());
    }

    public Set<String> colunasMarcadasDoJoin(String alias) {
        return joinsAtivos.getOrDefault(alias, new LinkedHashSet<>());
    }

    // ---- resultado ----
    public List<String> getResultadoColunas() { return resultadoColunas; }
    public void setResultadoColunas(List<String> v) {
        this.resultadoColunas = v == null ? List.of() : List.copyOf(v);
    }

    public List<Map<String, Object>> getResultadoLinhas() { return resultadoLinhas; }
    public void setResultadoLinhas(List<Map<String, Object>> v) {
        this.resultadoLinhas = v == null ? List.of() : List.copyOf(v);
    }
}