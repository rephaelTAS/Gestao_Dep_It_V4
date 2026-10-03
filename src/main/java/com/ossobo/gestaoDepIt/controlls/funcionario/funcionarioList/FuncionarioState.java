package com.ossobo.gestaoDepIt.controlls.funcionario.funcionarioList;

import com.ossobo.gestaoDepIt.db.models.Funcionarios;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

/**
 * FuncionarioState v1.0
 *
 * Responsabilidade: manter o estado da tela de listagem de funcionários —
 *                   dados, paginação, filtros.
 *
 * Espelha CatalogState (catálogo) — mesma estrutura, zero service.
 *
 * @since v1.0
 */
public class FuncionarioState {

    // ===== DADOS =====
    private final ObservableList<Funcionarios> funcionariosData = FXCollections.observableArrayList();

    // ===== PAGINAÇÃO =====
    private int paginaAtual = 1;
    private int totalPaginas = 1;
    private int itensPorPagina = 20;

    // ===== FILTROS =====
    private String filtroNome = null;
    private String filtroDepartamento = null;
    private String filtroStatus = "Todos";
    private String filtroComFoto = "Todos";

    // ===== DADOS =====
    public ObservableList<Funcionarios> getFuncionariosData() { return funcionariosData; }

    public void setFuncionariosData(List<Funcionarios> funcionarios) {
        this.funcionariosData.setAll(funcionarios);
    }

    public void clearFuncionariosData() { this.funcionariosData.clear(); }

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

    public void setTotalPaginas(int totalFuncionarios) {
        this.totalPaginas = Math.max(1, (int) Math.ceil((double) totalFuncionarios / itensPorPagina));
    }

    public int getItensPorPagina() { return itensPorPagina; }

    public void setItensPorPagina(int itensPorPagina) {
        this.itensPorPagina = Math.max(1, itensPorPagina);
        this.paginaAtual = 1;
    }

    // ===== FILTROS =====
    public String getFiltroNome() { return filtroNome; }

    public void setFiltroNome(String filtroNome) {
        this.filtroNome = (filtroNome != null && !filtroNome.trim().isEmpty())
                ? filtroNome.trim() : null;
        this.paginaAtual = 1;
    }

    public String getFiltroDepartamento() { return filtroDepartamento; }

    public void setFiltroDepartamento(String filtroDepartamento) {
        this.filtroDepartamento = (filtroDepartamento != null && !filtroDepartamento.trim().isEmpty())
                ? filtroDepartamento.trim() : null;
        this.paginaAtual = 1;
    }

    public String getFiltroStatus() { return filtroStatus; }

    public void setFiltroStatus(String filtroStatus) {
        this.filtroStatus = filtroStatus != null ? filtroStatus : "Todos";
        this.paginaAtual = 1;
    }

    public String getFiltroComFoto() { return filtroComFoto; }

    public void setFiltroComFoto(String filtroComFoto) {
        this.filtroComFoto = filtroComFoto != null ? filtroComFoto : "Todos";
        this.paginaAtual = 1;
    }

    // ===== UTILITÁRIOS =====
    public boolean hasActiveFilters() {
        return filtroNome != null ||
                filtroDepartamento != null ||
                !"Todos".equals(filtroStatus) ||
                !"Todos".equals(filtroComFoto);
    }

    public void resetFilters() {
        filtroNome = null;
        filtroDepartamento = null;
        filtroStatus = "Todos";
        filtroComFoto = "Todos";
        paginaAtual = 1;
    }

    public void cleanup() {
        clearFuncionariosData();
    }
}