package com.ossobo.gestaoDepIt.controlls.relatorio.simples;

import java.util.List;
import java.util.Map;

/**
 * SimplesState v1.0
 *
 * Estado da tela de relatório simples: alias escolhido, colunas
 * marcadas, filtros, e o último resultado recebido.
 */
public final class SimplesState {

    private String aliasAtual = "";
    private List<String> colunasDisponiveis = List.of();
    private List<String> colunasSelecionadas = List.of();
    private List<String> resultadoColunas = List.of();
    private List<Map<String, Object>> resultadoLinhas = List.of();

    public String getAliasAtual() { return aliasAtual; }
    public void setAliasAtual(String v) { this.aliasAtual = v == null ? "" : v; }

    public List<String> getColunasDisponiveis() { return colunasDisponiveis; }
    public void setColunasDisponiveis(List<String> v) { this.colunasDisponiveis = v == null ? List.of() : List.copyOf(v); }

    public List<String> getColunasSelecionadas() { return colunasSelecionadas; }
    public void setColunasSelecionadas(List<String> v) { this.colunasSelecionadas = v == null ? List.of() : List.copyOf(v); }

    public List<String> getResultadoColunas() { return resultadoColunas; }
    public void setResultadoColunas(List<String> v) { this.resultadoColunas = v == null ? List.of() : List.copyOf(v); }

    public List<Map<String, Object>> getResultadoLinhas() { return resultadoLinhas; }
    public void setResultadoLinhas(List<Map<String, Object>> v) { this.resultadoLinhas = v == null ? List.of() : List.copyOf(v); }
}