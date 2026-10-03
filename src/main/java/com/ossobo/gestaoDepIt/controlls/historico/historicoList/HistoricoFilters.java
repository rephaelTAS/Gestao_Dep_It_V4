package com.ossobo.gestaoDepIt.controlls.historico.historicoList;

import com.ossobo.gestaoDepIt.db.enums.TipoEvento;
import com.ossobo.winterfx.router.model.Params;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * HistoricoFilters v1.0
 *
 * Traduz UI de filtros ↔ Params da rota.
 * Sem service injetado — valores de domínio vêm de TipoEvento.todos().
 *
 * Filtros contextuais (sku + funcionarioid) são injetados pelo controller
 * via preFiltroSku/preFiltroFuncionario — prioridade sobre os campos de UI.
 *
 * @since v1.0
 */
public class HistoricoFilters {

    private TextField        filtroPesquisa;
    private ComboBox<String> filtroTipoEvento;
    private DatePicker       filtroDataInicio;
    private DatePicker       filtroDataFim;

    /** Filtros contextuais — colocados pelo list controller ao abrir a janela. */
    private String preFiltroSku = null;
    private String preFiltroFuncionarioId = null;

    // ===== INICIALIZAÇÃO UI =====
    public void initializeUIComponents(
            TextField pesquisa,
            ComboBox<String> tipoEvento,
            DatePicker dataInicio,
            DatePicker dataFim) {
        this.filtroPesquisa = pesquisa;
        this.filtroTipoEvento = tipoEvento;
        this.filtroDataInicio = dataInicio;
        this.filtroDataFim = dataFim;
    }

    public void configureBasicFilters() {
        if (filtroTipoEvento != null) {
            filtroTipoEvento.getItems().setAll(TipoEvento.todos());
        }
    }

    // ===== CONTEXTO (filtro aplicado por rota) =====
    public void setPreFiltroSku(String sku) { this.preFiltroSku = sku; }
    public void setPreFiltroFuncionarioId(String id) { this.preFiltroFuncionarioId = id; }

    public boolean temPreFiltro() {
        return (preFiltroSku != null && !preFiltroSku.isBlank())
                || (preFiltroFuncionarioId != null && !preFiltroFuncionarioId.isBlank());
    }

    // ===== LEITURA DA UI → PARAMS =====
    public Params paramsDeFiltros() {
        Params p = Params.with("_", "");

        // Contexto tem precedência sobre campo de busca livre
        if (preFiltroSku != null && !preFiltroSku.isBlank()) {
            p.and("sku", preFiltroSku.trim());
        }
        if (preFiltroFuncionarioId != null && !preFiltroFuncionarioId.isBlank()) {
            p.and("funcionarioid", preFiltroFuncionarioId.trim());
        }

        // Busca livre só entra se não há contexto fechado
        if (!temPreFiltro() && filtroPesquisa != null && notBlank(filtroPesquisa.getText())) {
            // A rota com-filtros aceita sku isolado; texto livre não tem campo dedicado
            // — usamos como fallback sku para o backend filtrar por SKU/descrição.
            p.and("sku", filtroPesquisa.getText().trim());
        }

        if (filtroTipoEvento != null && filtroTipoEvento.getValue() != null) {
            p.and("tipo", filtroTipoEvento.getValue());
        }

        // início/fim: aceita DatePicker só de data → converte para ISO LocalDateTime.
        if (filtroDataInicio != null && filtroDataInicio.getValue() != null) {
            LocalDateTime inicio = LocalDateTime.of(filtroDataInicio.getValue(), LocalTime.MIN);
            p.and("inicio", inicio.toString());
        }
        if (filtroDataFim != null && filtroDataFim.getValue() != null) {
            LocalDateTime fim = LocalDateTime.of(filtroDataFim.getValue(), LocalTime.MAX);
            p.and("fim", fim.toString());
        }

        return p;
    }

    public boolean temFiltrosAtivos() {
        return temPreFiltro()
                || (filtroPesquisa != null && notBlank(filtroPesquisa.getText()))
                || (filtroTipoEvento != null && filtroTipoEvento.getValue() != null)
                || (filtroDataInicio != null && filtroDataInicio.getValue() != null)
                || (filtroDataFim != null && filtroDataFim.getValue() != null);
    }

    // ===== LIMPEZA =====
    public void clearFiltersUI() {
        if (filtroPesquisa != null) filtroPesquisa.clear();
        if (filtroTipoEvento != null) filtroTipoEvento.setValue(null);
        if (filtroDataInicio != null) filtroDataInicio.setValue(null);
        if (filtroDataFim != null) filtroDataFim.setValue(null);
        preFiltroSku = null;
        preFiltroFuncionarioId = null;
    }

    // ===== UTILITÁRIOS =====
    private boolean notBlank(String s) { return s != null && !s.isBlank(); }

    public void cleanup() {
        filtroPesquisa = null;
        filtroTipoEvento = null;
        filtroDataInicio = null;
        filtroDataFim = null;
    }
}