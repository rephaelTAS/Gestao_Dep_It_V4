package com.ossobo.gestaoDepIt.controlls.relatorio.simples;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * SimplesFilters v1.0
 *
 * Filtros do relatório simples: filtro1 (dinâmico — status/categoria/etc.),
 * período (início/fim). Serializa para Map<String,Object> que o Builder aceita.
 */
public final class SimplesFilters {

    private String filtro1 = null;      // valor do comboFiltro1
    private String colunaFiltro1 = null; // coluna correspondente (dinâmica)
    private LocalDate inicio;
    private LocalDate fim;

    public String getFiltro1() { return filtro1; }
    public void setFiltro1(String v) { this.filtro1 = v; }

    public String getColunaFiltro1() { return colunaFiltro1; }
    public void setColunaFiltro1(String v) { this.colunaFiltro1 = v; }

    public LocalDate getInicio() { return inicio; }
    public void setInicio(LocalDate v) { this.inicio = v; }

    public LocalDate getFim() { return fim; }
    public void setFim(LocalDate v) { this.fim = v; }

    /** Converte para Map aceito por RelatorioBuilderService.consultarSimples. */
    public Map<String, Object> paraMapa() {
        Map<String, Object> m = new LinkedHashMap<>();
        if (colunaFiltro1 != null && filtro1 != null && !filtro1.isBlank()) {
            m.put(colunaFiltro1, filtro1);
        }
        return m;
    }
}