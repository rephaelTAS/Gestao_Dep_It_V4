/*
 * GIAGaugesBuilder v1.0
 *
 * Constrói os 5 gauges do dashboard GIA.
 * Extraído do Controller — SRP.
 *
 * v1.0: Versão inicial
 */
package com.ossobo.gestaoDepIt.controllers.gestao.gia.functions;

import com.ossobo.gestaoDepIt.db.models.CatalogoProdutos;
import com.ossobo.gestaoDepIt.db.models.InventarioEquipamentos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class GIAGaugesBuilder {

    private final Label gaugeSaudeGeral, gaugeValorTotal, lblEmManutencao, gaugeCriticos, gaugePrevisaoSubstituicao;
    private final ProgressBar barSaudeGeral;
    private final Label labelSaudeDetalhe, labelValorDetalhe, labelManutencaoDetalhe, labelCriticosDetalhe, labelPrevisaoDetalhe;

    public GIAGaugesBuilder(Label gaugeSaudeGeral, Label gaugeValorTotal, Label lblEmManutencao,
                            Label gaugeCriticos, Label gaugePrevisaoSubstituicao, ProgressBar barSaudeGeral,
                            Label labelSaudeDetalhe, Label labelValorDetalhe, Label labelManutencaoDetalhe,
                            Label labelCriticosDetalhe, Label labelPrevisaoDetalhe) {
        this.gaugeSaudeGeral = gaugeSaudeGeral;
        this.gaugeValorTotal = gaugeValorTotal;
        this.lblEmManutencao = lblEmManutencao;
        this.gaugeCriticos = gaugeCriticos;
        this.gaugePrevisaoSubstituicao = gaugePrevisaoSubstituicao;
        this.barSaudeGeral = barSaudeGeral;
        this.labelSaudeDetalhe = labelSaudeDetalhe;
        this.labelValorDetalhe = labelValorDetalhe;
        this.labelManutencaoDetalhe = labelManutencaoDetalhe;
        this.labelCriticosDetalhe = labelCriticosDetalhe;
        this.labelPrevisaoDetalhe = labelPrevisaoDetalhe;
    }

    public void construir(List<InventarioEquipamentos> equipamentos, List<CatalogoProdutos> catalogos) {
        int total = equipamentos.size();
        long criticos = equipamentos.stream().filter(e -> "CRITICO".equals(e.getCondicao())).count();
        long manutencao = equipamentos.stream().filter(e -> "MANUTENCAO".equals(e.getStatus())).count();
        long bons = equipamentos.stream().filter(e -> "OTIMO".equals(e.getCondicao()) || "BOM".equals(e.getCondicao())).count();
        double saude = total > 0 ? (double) bons / total : 0;

        BigDecimal valorTotal = BigDecimal.ZERO;
        for (CatalogoProdutos cp : catalogos) {
            if (cp.getPrecoUnitario() != null && cp.getTotalRecebido() != null) {
                valorTotal = valorTotal.add(cp.getPrecoUnitario().multiply(BigDecimal.valueOf(cp.getTotalRecebido())));
            }
        }

        long velhos = equipamentos.stream()
                .filter(e -> e.getDataAquisicao() != null && ChronoUnit.YEARS.between(e.getDataAquisicao(), LocalDate.now()) > 4)
                .count();
        BigDecimal previsao = BigDecimal.ZERO;
        for (InventarioEquipamentos ie : equipamentos) {
            if (ie.getDataAquisicao() != null && ChronoUnit.YEARS.between(ie.getDataAquisicao(), LocalDate.now()) > 4) {
                catalogos.stream().filter(c -> c.getSku().equals(ie.getSkuProduto())).findFirst()
                        .ifPresent(c -> { if (c.getPrecoUnitario() != null) previsao.add(c.getPrecoUnitario()); });
            }
        }

        gaugeSaudeGeral.setText(String.format("%.0f%%", saude * 100));
        barSaudeGeral.setProgress(saude);
        labelSaudeDetalhe.setText(String.format("%d críticos ou ruins", criticos));
        gaugeValorTotal.setText(String.format("€ %,.0f", valorTotal));
        labelValorDetalhe.setText(String.format("%d equipamentos ativos", total));
        lblEmManutencao.setText(String.valueOf(manutencao));
        labelManutencaoDetalhe.setText("Precisam de atenção");
        gaugeCriticos.setText(String.valueOf(criticos));
        labelCriticosDetalhe.setText("Ação urgente necessária");
        gaugePrevisaoSubstituicao.setText(String.format("€ %,.0f", previsao));
        labelPrevisaoDetalhe.setText(String.format("%d equipamentos > 4 anos", velhos));
    }
}