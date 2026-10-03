package com.ossobo.gestaoDepIt.controlls.historico.historicoList;

import com.ossobo.gestaoDepIt.db.models.HistoricoEventos;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

/**
 * HistoricoState v1.0
 *
 * Estado da tela de listagem do histórico:
 *  - ObservableList de eventos (fonte da TableView)
 *  - Paginação (página atual / total / itens por página)
 *
 * Filtros NÃO vivem aqui — ficam no HistoricoFilters (padrão do catálogo).
 *
 * @since v1.0
 */
public class HistoricoState {

    private final ObservableList<HistoricoEventos> eventosData = FXCollections.observableArrayList();

    private int paginaAtual = 1;
    private int totalPaginas = 1;
    private int itensPorPagina = 30;

    // ===== DADOS =====
    public ObservableList<HistoricoEventos> getEventosData() { return eventosData; }

    public void setEventosData(List<HistoricoEventos> eventos) {
        this.eventosData.setAll(eventos);
    }

    public void clearEventosData() { this.eventosData.clear(); }

    // ===== PAGINAÇÃO =====
    public int getPaginaAtual() { return paginaAtual; }

    public void setPaginaAtual(int paginaAtual) {
        if (paginaAtual < 1) paginaAtual = 1;
        if (paginaAtual > totalPaginas) paginaAtual = totalPaginas;
        this.paginaAtual = paginaAtual;
    }

    public void primeiraPagina()   { setPaginaAtual(1); }
    public void paginaAnterior()   { if (paginaAtual > 1) setPaginaAtual(paginaAtual - 1); }
    public void proximaPagina()    { if (paginaAtual < totalPaginas) setPaginaAtual(paginaAtual + 1); }
    public void ultimaPagina()     { setPaginaAtual(totalPaginas); }

    public int getTotalPaginas() { return totalPaginas; }

    public void setTotalPaginas(int totalRegistros) {
        this.totalPaginas = Math.max(1, (int) Math.ceil((double) totalRegistros / itensPorPagina));
    }

    public int getItensPorPagina() { return itensPorPagina; }

    public void setItensPorPagina(int itensPorPagina) {
        this.itensPorPagina = Math.max(1, itensPorPagina);
        this.paginaAtual = 1;
    }

    public void cleanup() { clearEventosData(); }
}