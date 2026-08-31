package com.ossobo.gestaoDepIt.controllers.gestao.estoque.catalugoproduto.catalogoList;

import com.ossobo.gestaoDepIt.db.models.CatalogoProdutos;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Gerencia o estado da aplicação de catálogo
 * ✅ Single Responsibility: Apenas estado
 * ✅ Coesão Forte: <100 linhas
 * ✅ Zero Terminal Output
 */
public class CatalogState {
    // ===== DADOS =====
    private final ObservableList<CatalogoProdutos> produtosData = FXCollections.observableArrayList();

    // ===== PAGINAÇÃO =====
    private int paginaAtual = 1;
    private int totalPaginas = 1;
    private int itensPorPagina = 20;

    // ===== FILTROS =====
    private String filtroTipo = null;
    private String filtroCategoria = null;
    private String filtroMarca = null;
    private String filtroModelo = null;
    private String filtroEstoque = "Todos";
    private String filtroStatus = "Todos";

    // ===== MÉTODOS DE ACESSO =====
    public ObservableList<CatalogoProdutos> getProdutosData() { return produtosData; }

    public void setProdutosData(java.util.List<CatalogoProdutos> produtos) {
        this.produtosData.setAll(produtos);
    }

    public void clearProdutosData() { this.produtosData.clear(); }

    // ===== PAGINAÇÃO =====
    public int getPaginaAtual() { return paginaAtual; }

    public void setPaginaAtual(int paginaAtual) {
        if (paginaAtual < 1) paginaAtual = 1;
        if (paginaAtual > totalPaginas) paginaAtual = totalPaginas;
        this.paginaAtual = paginaAtual;
    }

    public void primeiraPagina() { setPaginaAtual(1); }

    public void paginaAnterior() {
        if (paginaAtual > 1) setPaginaAtual(paginaAtual - 1);
    }

    public void proximaPagina() {
        if (paginaAtual < totalPaginas) setPaginaAtual(paginaAtual + 1);
    }

    public void ultimaPagina() { setPaginaAtual(totalPaginas); }

    public int getTotalPaginas() { return totalPaginas; }

    public void setTotalPaginas(int totalProdutos) {
        this.totalPaginas = Math.max(1, (int) Math.ceil((double) totalProdutos / itensPorPagina));
    }

    public int getItensPorPagina() { return itensPorPagina; }

    public void setItensPorPagina(int itensPorPagina) {
        this.itensPorPagina = Math.max(1, itensPorPagina);
        this.paginaAtual = 1;
    }

    // ===== FILTROS =====
    public String getFiltroTipo() { return filtroTipo; }

    public void setFiltroTipo(String filtroTipo) {
        this.filtroTipo = filtroTipo;
        this.paginaAtual = 1;
    }

    public String getFiltroCategoria() { return filtroCategoria; }

    public void setFiltroCategoria(String filtroCategoria) {
        this.filtroCategoria = (filtroCategoria != null && !filtroCategoria.trim().isEmpty())
                ? filtroCategoria.trim() : null;
        this.paginaAtual = 1;
    }

    public String getFiltroMarca() { return filtroMarca; }

    public void setFiltroMarca(String filtroMarca) {
        this.filtroMarca = (filtroMarca != null && !filtroMarca.trim().isEmpty())
                ? filtroMarca.trim() : null;
        this.paginaAtual = 1;
    }

    public String getFiltroModelo() { return filtroModelo; }

    public void setFiltroModelo(String filtroModelo) {
        this.filtroModelo = (filtroModelo != null && !filtroModelo.trim().isEmpty())
                ? filtroModelo.trim() : null;
        this.paginaAtual = 1;
    }

    public String getFiltroEstoque() { return filtroEstoque; }

    public void setFiltroEstoque(String filtroEstoque) {
        this.filtroEstoque = filtroEstoque != null ? filtroEstoque : "Todos";
        this.paginaAtual = 1;
    }

    public String getFiltroStatus() { return filtroStatus; }

    public void setFiltroStatus(String filtroStatus) {
        this.filtroStatus = filtroStatus != null ? filtroStatus : "Todos";
        this.paginaAtual = 1;
    }

    // ===== UTILITÁRIOS =====
    public boolean hasActiveFilters() {
        return filtroTipo != null ||
                filtroCategoria != null ||
                filtroMarca != null ||
                filtroModelo != null ||
                !"Todos".equals(filtroEstoque) ||
                !"Todos".equals(filtroStatus);
    }

    public void resetFilters() {
        filtroTipo = null;
        filtroCategoria = null;
        filtroMarca = null;
        filtroModelo = null;
        filtroEstoque = "Todos";
        filtroStatus = "Todos";
        paginaAtual = 1;
    }

    public void cleanup() {
        clearProdutosData();
    }
}