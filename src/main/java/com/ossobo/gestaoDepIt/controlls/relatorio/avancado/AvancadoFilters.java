package com.ossobo.gestaoDepIt.controlls.relatorio.avancado;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * AvancadoFilters v1.0
 *
 * Filtros do relatório avançado — aplicados SOMENTE na tabela base
 * (decisão ratificada em F2). Serializa para Map<String,Object>.
 */
public final class AvancadoFilters {

    private String status;
    private String condicao;
    private String localizacao;
    private String departamento;
    private String tipoMovimentacao;
    private LocalDate inicio;
    private LocalDate fim;

    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }

    public String getCondicao() { return condicao; }
    public void setCondicao(String v) { this.condicao = v; }

    public String getLocalizacao() { return localizacao; }
    public void setLocalizacao(String v) { this.localizacao = v; }

    public String getDepartamento() { return departamento; }
    public void setDepartamento(String v) { this.departamento = v; }

    public String getTipoMovimentacao() { return tipoMovimentacao; }
    public void setTipoMovimentacao(String v) { this.tipoMovimentacao = v; }

    public LocalDate getInicio() { return inicio; }
    public void setInicio(LocalDate v) { this.inicio = v; }

    public LocalDate getFim() { return fim; }
    public void setFim(LocalDate v) { this.fim = v; }

    /** Converte para Map aceito pelo Builder. Só inclui colunas da base. */
    public Map<String, Object> paraMapa(String aliasBase) {
        Map<String, Object> m = new LinkedHashMap<>();
        switch (aliasBase) {
            case "inventario" -> {
                if (status != null && !status.isBlank()) m.put("status", status);
                if (condicao != null && !condicao.isBlank()) m.put("condicao", condicao);
                if (localizacao != null && !localizacao.isBlank()) m.put("localizacao", localizacao);
                if (departamento != null && !departamento.isBlank()) m.put("departamento", departamento);
            }
            case "catalogo" -> {
                if (status != null && !status.isBlank()) m.put("categoria", status);
            }
            case "funcionarios" -> {
                if (departamento != null && !departamento.isBlank()) m.put("departamento", departamento);
            }
            case "movimentacoes" -> {
                if (tipoMovimentacao != null && !tipoMovimentacao.isBlank())
                    m.put("tipo_movimentacao", tipoMovimentacao);
            }
            case "toners" -> {
                if (status != null && !status.isBlank()) m.put("percentagem_restante", status);
            }
            default -> { /* outros: sem filtro */ }
        }
        return m;
    }
}