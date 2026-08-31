// ===== FILTRO_MOVIMENTACAO_DTO.java =====
package com.ossobo.gestaoDepIt.controllers.gestao.estoque.movimentacoes.dto;

import java.time.LocalDate;

/**
 * DTO para filtros de movimentações
 * Propósito: Transferir dados de filtro entre camadas
 */
public class FiltroMovimentacaoDTO {
    private String skuProduto;
    private String tipoMovimentacao;
    private String lote;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private String codDepFuncionario;

    // Construtor
    public FiltroMovimentacaoDTO() {}

    // Getters e Setters
    public String getSkuProduto() { return skuProduto; }
    public void setSkuProduto(String skuProduto) { this.skuProduto = skuProduto; }

    public String getTipoMovimentacao() { return tipoMovimentacao; }
    public void setTipoMovimentacao(String tipoMovimentacao) { this.tipoMovimentacao = tipoMovimentacao; }

    public String getLote() { return lote; }
    public void setLote(String lote) { this.lote = lote; }

    public LocalDate getDataInicio() { return dataInicio; }
    public void setDataInicio(LocalDate dataInicio) { this.dataInicio = dataInicio; }

    public LocalDate getDataFim() { return dataFim; }
    public void setDataFim(LocalDate dataFim) { this.dataFim = dataFim; }

    public String getCodDepFuncionario() { return codDepFuncionario; }
    public void setCodDepFuncionario(String codDepFuncionario) { this.codDepFuncionario = codDepFuncionario; }

    public boolean isEmpty() {
        return (skuProduto == null || skuProduto.isEmpty()) &&
                (tipoMovimentacao == null || tipoMovimentacao.isEmpty()) &&
                (lote == null || lote.isEmpty()) &&
                dataInicio == null &&
                dataFim == null &&
                (codDepFuncionario == null || codDepFuncionario.isEmpty());
    }
}