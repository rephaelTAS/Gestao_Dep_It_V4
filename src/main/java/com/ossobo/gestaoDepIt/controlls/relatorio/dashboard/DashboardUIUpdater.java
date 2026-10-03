package com.ossobo.gestaoDepIt.controlls.relatorio.dashboard;

import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ListView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * DashboardUIUpdater v1.0
 *
 * Renderiza o DashboardState nos componentes da view.
 * Sem lógica de negócio — apenas apresentação.
 */
public final class DashboardUIUpdater {

    private static final DateTimeFormatter FMT_HORA =
            DateTimeFormatter.ofPattern("dd/MM HH:mm");

    private DashboardUIUpdater() {}

    // ============================================================
    // KPIs
    // ============================================================

    public static void atualizarKpis(DashboardState state,
                                     Label kpiHoje,
                                     Label kpiSemana,
                                     Label kpiModelos,
                                     Label kpiTotal,
                                     Label kpiMedia,
                                     Label kpiCombinacoes,
                                     Label kpiTotalModelos) {
        if (kpiHoje != null) kpiHoje.setText(String.valueOf(state.getRelatoriosHoje()));
        if (kpiSemana != null) kpiSemana.setText(String.valueOf(state.getRelatoriosSemana()));
        if (kpiModelos != null) kpiModelos.setText(String.valueOf(state.getTotalModelos()));
        if (kpiTotal != null) kpiTotal.setText(String.valueOf(state.getTotalRelatorios()));
        if (kpiMedia != null) kpiMedia.setText(String.format("%.1f", state.getMediaPorDia()));
        if (kpiCombinacoes != null) kpiCombinacoes.setText(String.valueOf(state.getCombinacoesUnicas()));
        if (kpiTotalModelos != null) kpiTotalModelos.setText(String.valueOf(state.getTotalModelos()));
    }

    // ============================================================
    // GRÁFICO
    // ============================================================

    public static void atualizarGrafico(BarChart<String, Number> grafico,
                                        Map<String, Integer> contagem) {
        if (grafico == null) return;
        grafico.getData().clear();
        if (contagem.isEmpty()) return;

        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        contagem.forEach((chave, valor) ->
                serie.getData().add(new XYChart.Data<>(chave, valor)));
        grafico.getData().add(serie);
    }

    // ============================================================
    // TABELAS
    // ============================================================

    /** Tabela de estatísticas: Tabela / Consultas / ÚltimaVez. */
    public static void atualizarTabelaEstatisticas(TableView<Map<String, Object>> tabela,
                                                   Map<String, Integer> contagem) {
        if (tabela == null) return;
        tabela.getItems().clear();
        contagem.forEach((k, v) -> {
            java.util.LinkedHashMap<String, Object> linha = new java.util.LinkedHashMap<>();
            linha.put("tabela", k);
            linha.put("consultas", v);
            linha.put("ultimaVez", "—");
            tabela.getItems().add(linha);
        });
    }

    /** Tabela de recentes — já chega como List<Map> da rota. */
    public static void atualizarTabelaRecentes(TableView<Map<String, Object>> tabela,
                                               List<Map<String, Object>> recentes) {
        if (tabela == null) return;
        tabela.getItems().setAll(recentes);
    }

    // ============================================================
    // LISTA DE MODELOS
    // ============================================================

    public static void atualizarListaModelos(ListView<String> lista, List<String> nomes) {
        if (lista == null) return;
        lista.getItems().setAll(nomes);
    }

    // ============================================================
    // UTIL
    // ============================================================

    public static String formatarHora(LocalDateTime dt) {
        return dt == null ? "—" : dt.format(FMT_HORA);
    }
}