/*
 * GIAChartBuilder v1.0
 *
 * Constrói os 5 gráficos do dashboard GIA.
 * Extraído do Controller — SRP.
 *
 * v1.0: Versão inicial
 */
package com.ossobo.gestaoDepIt.controllers.gestao.gia.functions;

import com.ossobo.gestaoDepIt.db.models.Funcionarios;
import com.ossobo.gestaoDepIt.db.models.InventarioEquipamentos;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GIAChartBuilder {

    private final PieChart pieChartStatus;
    private final BarChart<String, Number> barChartDepartamento, barChartBalanceamento, graficoCondicao;
    private final LineChart<String, Number> lineChartProjecao;

    public GIAChartBuilder(PieChart pieChartStatus, BarChart<String, Number> barChartDepartamento,
                           BarChart<String, Number> barChartBalanceamento, BarChart<String, Number> graficoCondicao,
                           LineChart<String, Number> lineChartProjecao) {
        this.pieChartStatus = pieChartStatus;
        this.barChartDepartamento = barChartDepartamento;
        this.barChartBalanceamento = barChartBalanceamento;
        this.graficoCondicao = graficoCondicao;
        this.lineChartProjecao = lineChartProjecao;
    }

    public void construirTodos(List<InventarioEquipamentos> equipamentos, List<Funcionarios> funcionarios) {
        construirPizzaStatus(equipamentos);
        construirBarrasDepartamento(equipamentos);
        construirBarrasCondicao(equipamentos);
        construirLinhaProjecao(equipamentos);
        construirBarrasBalanceamento(equipamentos, funcionarios);
    }

    private void construirPizzaStatus(List<InventarioEquipamentos> equipamentos) {
        Map<String, Long> porStatus = equipamentos.stream()
                .collect(Collectors.groupingBy(e -> e.getStatus() != null ? e.getStatus() : "?", Collectors.counting()));
        ObservableList<PieChart.Data> dados = FXCollections.observableArrayList();
        porStatus.forEach((k, v) -> dados.add(new PieChart.Data(k + " (" + v + ")", v)));
        pieChartStatus.setData(dados);
    }

    private void construirBarrasDepartamento(List<InventarioEquipamentos> equipamentos) {
        Map<String, Long> porDepto = equipamentos.stream()
                .filter(e -> e.getDepartamento() != null)
                .collect(Collectors.groupingBy(InventarioEquipamentos::getDepartamento, Collectors.counting()));
        barChartDepartamento.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Equipamentos");
        porDepto.entrySet().stream().sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .forEach(e -> series.getData().add(new XYChart.Data<>(e.getKey(), e.getValue())));
        barChartDepartamento.getData().add(series);
    }

    private void construirBarrasCondicao(List<InventarioEquipamentos> equipamentos) {
        Map<String, Long> porCond = equipamentos.stream()
                .filter(e -> e.getCondicao() != null)
                .collect(Collectors.groupingBy(InventarioEquipamentos::getCondicao, Collectors.counting()));
        graficoCondicao.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (String cond : List.of("OTIMO", "BOM", "REGULAR", "CRITICO")) {
            series.getData().add(new XYChart.Data<>(cond, porCond.getOrDefault(cond, 0L)));
        }
        graficoCondicao.getData().add(series);
    }

    private void construirLinhaProjecao(List<InventarioEquipamentos> equipamentos) {
        lineChartProjecao.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Custo Projetado (€)");
        int anoAtual = LocalDate.now().getYear();
        for (int i = 0; i < 5; i++) {
            int ano = anoAtual + i;
            long count = equipamentos.stream()
                    .filter(e -> e.getDataAquisicao() != null && ano - e.getDataAquisicao().getYear() >= 4)
                    .count();
            series.getData().add(new XYChart.Data<>(String.valueOf(ano), count * 500));
        }
        lineChartProjecao.getData().add(series);
    }

    private void construirBarrasBalanceamento(List<InventarioEquipamentos> equipamentos, List<Funcionarios> funcionarios) {
        Map<String, Long> porFunc = equipamentos.stream()
                .filter(e -> e.getFuncionarioId() != null)
                .collect(Collectors.groupingBy(InventarioEquipamentos::getFuncionarioId, Collectors.counting()));
        barChartBalanceamento.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Equipamentos");
        porFunc.entrySet().stream().sorted(Map.Entry.<String, Long>comparingByValue().reversed()).limit(10)
                .forEach(e -> {
                    String nome = funcionarios.stream().filter(f -> f.getCodDep().equals(e.getKey()))
                            .findFirst().map(Funcionarios::getNome).orElse(e.getKey());
                    series.getData().add(new XYChart.Data<>(nome, e.getValue()));
                });
        barChartBalanceamento.getData().add(series);
    }
}