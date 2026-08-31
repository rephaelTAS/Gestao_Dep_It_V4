package com.ossobo.gestaoDepIt.controllers.gestao.equipamentos.inventario.inventarioList;

import com.ossobo.gestaoDepIt.db.models.InventarioEquipamentos;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.time.LocalDate;

/**
 * Gerencia o estado do inventário.
 * Responsabilidade única: Manter ObservableList + filtros + paginação.
 */
public class InventoryState {

    private final ObservableList<InventarioEquipamentos> equipamentosData = FXCollections.observableArrayList();

    // Paginação
    private int paginaAtual = 1;
    private int totalPaginas = 1;
    private int itensPorPagina = 25;

    // Filtros
    private String filtroStatus = null;
    private String filtroCondicao = null;
    private String filtroLocalizacao = null;
    private String filtroDepartamento = null;
    private String filtroMac = null;
    private String filtroBusca = null;
    private LocalDate filtroAquisicaoInicio = null;
    private LocalDate filtroAquisicaoFim = null;
    private LocalDate filtroInstalacaoInicio = null;
    private LocalDate filtroInstalacaoFim = null;
    private LocalDate filtroVerificacaoInicio = null;
    private LocalDate filtroVerificacaoFim = null;

    // ===== DADOS =====
    public ObservableList<InventarioEquipamentos> getEquipamentosData() { return equipamentosData; }

    public void setEquipamentosData(java.util.List<InventarioEquipamentos> equipamentos) {
        this.equipamentosData.setAll(equipamentos);
    }

    public void clearEquipamentosData() { this.equipamentosData.clear(); }

    // ===== PAGINAÇÃO =====
    public int getPaginaAtual() { return paginaAtual; }
    public void setPaginaAtual(int paginaAtual) {
        this.paginaAtual = Math.max(1, Math.min(paginaAtual, totalPaginas));
    }
    public int getTotalPaginas() { return totalPaginas; }
    public void setTotalPaginas(int totalRegistros) {
        this.totalPaginas = Math.max(1, (int) Math.ceil((double) totalRegistros / itensPorPagina));
    }
    public int getItensPorPagina() { return itensPorPagina; }
    public void setItensPorPagina(int itensPorPagina) {
        this.itensPorPagina = Math.max(1, itensPorPagina);
        this.paginaAtual = 1;
    }
    public void primeiraPagina() { setPaginaAtual(1); }
    public void paginaAnterior() { if (paginaAtual > 1) setPaginaAtual(paginaAtual - 1); }
    public void proximaPagina() { if (paginaAtual < totalPaginas) setPaginaAtual(paginaAtual + 1); }
    public void ultimaPagina() { setPaginaAtual(totalPaginas); }

    // ===== FILTROS =====
    public String getFiltroStatus() { return filtroStatus; }
    public void setFiltroStatus(String filtroStatus) { this.filtroStatus = filtroStatus; this.paginaAtual = 1; }
    public String getFiltroCondicao() { return filtroCondicao; }
    public void setFiltroCondicao(String filtroCondicao) { this.filtroCondicao = filtroCondicao; this.paginaAtual = 1; }
    public String getFiltroLocalizacao() { return filtroLocalizacao; }
    public void setFiltroLocalizacao(String filtroLocalizacao) { this.filtroLocalizacao = filtroLocalizacao; this.paginaAtual = 1; }
    public String getFiltroDepartamento() { return filtroDepartamento; }
    public void setFiltroDepartamento(String filtroDepartamento) { this.filtroDepartamento = filtroDepartamento; this.paginaAtual = 1; }
    public String getFiltroMac() { return filtroMac; }
    public void setFiltroMac(String filtroMac) { this.filtroMac = filtroMac; this.paginaAtual = 1; }
    public String getFiltroBusca() { return filtroBusca; }
    public void setFiltroBusca(String filtroBusca) { this.filtroBusca = filtroBusca; this.paginaAtual = 1; }
    public LocalDate getFiltroAquisicaoInicio() { return filtroAquisicaoInicio; }
    public void setFiltroAquisicaoInicio(LocalDate filtroAquisicaoInicio) { this.filtroAquisicaoInicio = filtroAquisicaoInicio; this.paginaAtual = 1; }
    public LocalDate getFiltroAquisicaoFim() { return filtroAquisicaoFim; }
    public void setFiltroAquisicaoFim(LocalDate filtroAquisicaoFim) { this.filtroAquisicaoFim = filtroAquisicaoFim; this.paginaAtual = 1; }
    public LocalDate getFiltroInstalacaoInicio() { return filtroInstalacaoInicio; }
    public void setFiltroInstalacaoInicio(LocalDate filtroInstalacaoInicio) { this.filtroInstalacaoInicio = filtroInstalacaoInicio; this.paginaAtual = 1; }
    public LocalDate getFiltroInstalacaoFim() { return filtroInstalacaoFim; }
    public void setFiltroInstalacaoFim(LocalDate filtroInstalacaoFim) { this.filtroInstalacaoFim = filtroInstalacaoFim; this.paginaAtual = 1; }
    public LocalDate getFiltroVerificacaoInicio() { return filtroVerificacaoInicio; }
    public void setFiltroVerificacaoInicio(LocalDate filtroVerificacaoInicio) { this.filtroVerificacaoInicio = filtroVerificacaoInicio; this.paginaAtual = 1; }
    public LocalDate getFiltroVerificacaoFim() { return filtroVerificacaoFim; }
    public void setFiltroVerificacaoFim(LocalDate filtroVerificacaoFim) { this.filtroVerificacaoFim = filtroVerificacaoFim; this.paginaAtual = 1; }

    public boolean hasActiveFilters() {
        return filtroStatus != null || filtroCondicao != null || filtroLocalizacao != null ||
                filtroDepartamento != null || filtroMac != null || filtroBusca != null ||
                filtroAquisicaoInicio != null || filtroAquisicaoFim != null;
    }

    public void resetFilters() {
        filtroStatus = null;
        filtroCondicao = null;
        filtroLocalizacao = null;
        filtroDepartamento = null;
        filtroMac = null;
        filtroBusca = null;
        filtroAquisicaoInicio = null;
        filtroAquisicaoFim = null;
        filtroInstalacaoInicio = null;
        filtroInstalacaoFim = null;
        filtroVerificacaoInicio = null;
        filtroVerificacaoFim = null;
        paginaAtual = 1;
    }

    public void cleanup() {
        clearEquipamentosData();
    }
}