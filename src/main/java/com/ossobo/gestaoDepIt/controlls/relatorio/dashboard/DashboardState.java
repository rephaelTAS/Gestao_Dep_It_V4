package com.ossobo.gestaoDepIt.controlls.relatorio.dashboard;

import java.util.List;
import java.util.Map;

/**
 * DashboardState v1.0
 *
 * Estado imutável do dashboard de relatórios.
 * Guarda os últimos dados recebidos das rotas — UI lê daqui.
 */
public final class DashboardState {

    private long relatoriosHoje;
    private long relatoriosSemana;
    private long totalRelatorios;
    private double mediaPorDia;
    private int combinacoesUnicas;
    private int totalModelos;

    private List<Map<String, Object>> recentes = List.of();
    private Map<String, Integer> contagemPorCombinacao = Map.of();

    // getters / setters simples (não é record — é estado mutável interno)
    public long getRelatoriosHoje() { return relatoriosHoje; }
    public void setRelatoriosHoje(long v) { this.relatoriosHoje = v; }

    public long getRelatoriosSemana() { return relatoriosSemana; }
    public void setRelatoriosSemana(long v) { this.relatoriosSemana = v; }

    public long getTotalRelatorios() { return totalRelatorios; }
    public void setTotalRelatorios(long v) { this.totalRelatorios = v; }

    public double getMediaPorDia() { return mediaPorDia; }
    public void setMediaPorDia(double v) { this.mediaPorDia = v; }

    public int getCombinacoesUnicas() { return combinacoesUnicas; }
    public void setCombinacoesUnicas(int v) { this.combinacoesUnicas = v; }

    public int getTotalModelos() { return totalModelos; }
    public void setTotalModelos(int v) { this.totalModelos = v; }

    public List<Map<String, Object>> getRecentes() { return recentes; }
    public void setRecentes(List<Map<String, Object>> v) { this.recentes = v == null ? List.of() : List.copyOf(v); }

    public Map<String, Integer> getContagemPorCombinacao() { return contagemPorCombinacao; }
    public void setContagemPorCombinacao(Map<String, Integer> v) {
        this.contagemPorCombinacao = v == null ? Map.of() : Map.copyOf(v);
    }
}