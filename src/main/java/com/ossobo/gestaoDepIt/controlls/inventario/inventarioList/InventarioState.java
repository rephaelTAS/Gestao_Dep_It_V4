package com.ossobo.gestaoDepIt.controlls.inventario.inventarioList;

import com.ossobo.gestaoDepIt.db.models.InventarioEquipamentos;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

/**
 * InventarioState v1.0
 *
 * Responsabilidade única: Estado da tela de listagem do inventário.
 *  - ObservableList de equipamentos (fonte da TableView)
 *  - Paginação (página atual / total / itens por página)
 *
 * Filtros NÃO vivem aqui — ficam no InventarioFilters (padrão do catálogo).
 *
 * @since v1.0
 */
public class InventarioState {

    // ===== DADOS =====
    private final ObservableList<InventarioEquipamentos> equipamentosData = FXCollections.observableArrayList();

    // ===== PAGINAÇÃO =====
    private int paginaAtual = 1;
    private int totalPaginas = 1;
    private int itensPorPagina = 25;

    // ===== DADOS =====
    public ObservableList<InventarioEquipamentos> getEquipamentosData() {
        return equipamentosData;
    }

    public void setEquipamentosData(List<InventarioEquipamentos> equipamentos) {
        this.equipamentosData.setAll(equipamentos);
    }

    public void clearEquipamentosData() {
        this.equipamentosData.clear();
    }

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

    public void setTotalPaginas(int totalRegistros) {
        this.totalPaginas = Math.max(1, (int) Math.ceil((double) totalRegistros / itensPorPagina));
    }

    public int getItensPorPagina() { return itensPorPagina; }

    public void setItensPorPagina(int itensPorPagina) {
        this.itensPorPagina = Math.max(1, itensPorPagina);
        this.paginaAtual = 1;
    }

    // ===== CICLO DE VIDA =====
    public void cleanup() {
        clearEquipamentosData();
    }
}