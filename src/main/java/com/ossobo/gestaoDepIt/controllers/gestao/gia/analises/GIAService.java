package com.ossobo.gestaoDepIt.controllers.gestao.gia.analises;/*
 * GIAService v1.0
 *
 * Centralizador de análises do GIA.
 * Absorve o melhor do InventarioMetricsService antigo:
 * - Saúde do inventário (🟢🟡🔴)
 * - Resumo executivo
 * - Tendência de renovação
 * - Métricas consolidadas
 *
 * v1.0: Versão inicial — integração com nova arquitetura
 */


import com.ossobo.gestaoDepIt.db.models.CatalogoProdutos;
import com.ossobo.gestaoDepIt.db.models.InventarioEquipamentos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class GIAService {

    private static final Logger logger = LoggerFactory.getLogger(GIAService.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final CriticidadeCalculator criticidadeCalculator;
    private final BalanceamentoAnalyzer balanceamentoAnalyzer;
    private final ProjecaoFinanceiraService projecaoFinanceiraService;
    private final EstoqueInteligenteService estoqueInteligenteService;
    private final CustoBeneficioAnalyzer custoBeneficioAnalyzer;

    public GIAService(CriticidadeCalculator criticidadeCalculator,
                      BalanceamentoAnalyzer balanceamentoAnalyzer,
                      ProjecaoFinanceiraService projecaoFinanceiraService,
                      EstoqueInteligenteService estoqueInteligenteService,
                      CustoBeneficioAnalyzer custoBeneficioAnalyzer) {
        this.criticidadeCalculator = criticidadeCalculator;
        this.balanceamentoAnalyzer = balanceamentoAnalyzer;
        this.projecaoFinanceiraService = projecaoFinanceiraService;
        this.estoqueInteligenteService = estoqueInteligenteService;
        this.custoBeneficioAnalyzer = custoBeneficioAnalyzer;
    }

    // =========================================================================
    // SAÚDE DO INVENTÁRIO (🟢🟡🔴)
    // =========================================================================

    /**
     * Avalia a saúde geral do inventário com indicadores visuais.
     * Herdado do antigo InventarioMetricsService.getSaudeInventario()
     */
    public Map<String, String> avaliarSaudeInventario(List<InventarioEquipamentos> equipamentos,
                                                      List<CatalogoProdutos> catalogos) {
        Map<String, String> saude = new LinkedHashMap<>();
        long total = equipamentos.size();

        if (total == 0) {
            saude.put("Status", "🔴 Inventário vazio");
            return saude;
        }

        // 1. Criticidade
        long criticos = equipamentos.stream().filter(e -> "CRITICO".equals(e.getCondicao())).count();
        if (criticos == 0) {
            saude.put("Criticidade", "🟢 Excelente — Nenhum equipamento crítico");
        } else if (criticos <= total * 0.05) {
            saude.put("Criticidade", "🟡 Moderada — " + criticos + " equipamentos críticos");
        } else {
            saude.put("Criticidade", "🔴 Crítica — " + criticos + " equipamentos críticos (" +
                    String.format("%.1f%%", criticos * 100.0 / total) + ")");
        }

        // 2. Completude de dados
        long semDepto = equipamentos.stream().filter(e -> e.getDepartamento() == null || e.getDepartamento().isEmpty()).count();
        long semStatus = equipamentos.stream().filter(e -> e.getStatus() == null || e.getStatus().isEmpty()).count();
        long semCondicao = equipamentos.stream().filter(e -> e.getCondicao() == null || e.getCondicao().isEmpty()).count();
        long semData = equipamentos.stream().filter(e -> e.getDataAquisicao() == null).count();
        long totalFaltantes = semDepto + semStatus + semCondicao + semData;
        double completude = 100.0 - (totalFaltantes * 100.0 / (total * 4.0));

        if (completude >= 95) {
            saude.put("Completude", "🟢 Excelente — " + String.format("%.1f%%", completude));
        } else if (completude >= 80) {
            saude.put("Completude", "🟡 Moderada — " + String.format("%.1f%%", completude));
        } else {
            saude.put("Completude", "🔴 Baixa — " + String.format("%.1f%%", completude) +
                    " (" + totalFaltantes + " campos faltantes)");
        }

        // 3. Modernidade (idade)
        long antigos = equipamentos.stream()
                .filter(e -> e.getDataAquisicao() != null &&
                        ChronoUnit.YEARS.between(e.getDataAquisicao(), LocalDate.now()) > 5)
                .count();
        double pctAntigos = total > 0 ? (antigos * 100.0 / total) : 0;

        if (pctAntigos < 20) {
            saude.put("Modernidade", "🟢 Excelente — " + String.format("%.1f%%", pctAntigos) + " antigos");
        } else if (pctAntigos < 40) {
            saude.put("Modernidade", "🟡 Moderada — " + String.format("%.1f%%", pctAntigos) + " antigos");
        } else {
            saude.put("Modernidade", "🔴 Precária — " + String.format("%.1f%%", pctAntigos) + " antigos");
        }

        // 4. Manutenção
        long emManutencao = equipamentos.stream().filter(e -> "MANUTENCAO".equals(e.getStatus())).count();
        double pctManutencao = total > 0 ? (emManutencao * 100.0 / total) : 0;

        if (pctManutencao < 5) {
            saude.put("Manutenção", "🟢 Normal — " + emManutencao + " em manutenção");
        } else if (pctManutencao < 15) {
            saude.put("Manutenção", "🟡 Atenção — " + emManutencao + " em manutenção");
        } else {
            saude.put("Manutenção", "🔴 Crítica — " + emManutencao + " em manutenção (" +
                    String.format("%.1f%%", pctManutencao) + ")");
        }

        // 5. Avaliação geral
        long vermelhos = saude.values().stream().filter(v -> v.contains("🔴")).count();
        long verdes = saude.values().stream().filter(v -> v.contains("🟢")).count();

        if (vermelhos >= 2) {
            saude.put("Avaliação Geral", "🔴 Atenção Urgente Necessária");
        } else if (vermelhos == 1) {
            saude.put("Avaliação Geral", "🟡 Monitorar — Há pontos de atenção");
        } else if (verdes >= 3) {
            saude.put("Avaliação Geral", "🟢 Saudável — Inventário bem gerenciado");
        } else {
            saude.put("Avaliação Geral", "🟡 Regular — Manter monitoramento");
        }

        return saude;
    }

    // =========================================================================
    // RESUMO EXECUTIVO
    // =========================================================================

    /**
     * Gera resumo executivo das métricas principais.
     * Herdado do antigo InventarioMetricsService.getResumoExecutivo()
     */
    public String gerarResumoExecutivo(List<InventarioEquipamentos> equipamentos,
                                       List<CatalogoProdutos> catalogos) {
        StringBuilder resumo = new StringBuilder();
        long total = equipamentos.size();
        long criticos = equipamentos.stream().filter(e -> "CRITICO".equals(e.getCondicao())).count();
        long manutencao = equipamentos.stream().filter(e -> "MANUTENCAO".equals(e.getStatus())).count();
        long baixados = equipamentos.stream().filter(e -> "BAIXADO".equals(e.getStatus())).count();

        // Valor total
        BigDecimal valorTotal = BigDecimal.ZERO;
        for (InventarioEquipamentos eq : equipamentos) {
            catalogos.stream().filter(c -> c.getSku().equals(eq.getSkuProduto())).findFirst()
                    .ifPresent(c -> {
                        if (c.getPrecoUnitario() != null) valorTotal.add(c.getPrecoUnitario());
                    });
        }

        // Idade média
        double idadeMedia = equipamentos.stream()
                .filter(e -> e.getDataAquisicao() != null)
                .mapToLong(e -> ChronoUnit.YEARS.between(e.getDataAquisicao(), LocalDate.now()))
                .average().orElse(0);

        resumo.append("RESUMO EXECUTIVO — INVENTÁRIO DE EQUIPAMENTOS\n");
        resumo.append("=".repeat(55)).append("\n\n");
        resumo.append(String.format("Total de Equipamentos:     %d\n", total));
        resumo.append(String.format("Valor Total Estimado:      € %,.0f\n", valorTotal));
        resumo.append(String.format("Em Condição Crítica:       %d (%.1f%%)\n", criticos, total > 0 ? criticos * 100.0 / total : 0));
        resumo.append(String.format("Em Manutenção:             %d\n", manutencao));
        resumo.append(String.format("Baixados:                  %d\n", baixados));
        resumo.append(String.format("Idade Média:               %.1f anos\n", idadeMedia));
        resumo.append(String.format("Ativos:                    %d\n", total - baixados));
        resumo.append("\n");
        resumo.append("Saúde do Inventário:\n");

        Map<String, String> saude = avaliarSaudeInventario(equipamentos, catalogos);
        saude.forEach((k, v) -> resumo.append(String.format("  %s: %s\n", k, v)));

        resumo.append("\n").append("=".repeat(55)).append("\n");
        resumo.append("Gerado em: ").append(LocalDate.now().format(DATE_FORMAT)).append("\n");

        return resumo.toString();
    }

    // =========================================================================
    // TENDÊNCIA DE RENOVAÇÃO
    // =========================================================================

    /**
     * Analisa tendência de necessidade de renovação.
     * Herdado do antigo InventarioMetricsService.getTendenciaRenovacao()
     */
    public String analisarTendenciaRenovacao(List<InventarioEquipamentos> equipamentos) {
        long total = equipamentos.size();
        if (total == 0) return "Dados insuficientes para análise.";

        long maisDe5Anos = equipamentos.stream()
                .filter(e -> e.getDataAquisicao() != null &&
                        ChronoUnit.YEARS.between(e.getDataAquisicao(), LocalDate.now()) > 5)
                .count();

        long entre3e5 = equipamentos.stream()
                .filter(e -> e.getDataAquisicao() != null)
                .filter(e -> {
                    long idade = ChronoUnit.YEARS.between(e.getDataAquisicao(), LocalDate.now());
                    return idade >= 3 && idade <= 5;
                })
                .count();

        double pctAntigos = (maisDe5Anos * 100.0) / total;
        double pctProximos = (entre3e5 * 100.0) / total;

        StringBuilder tendencia = new StringBuilder();
        tendencia.append("ANÁLISE DE TENDÊNCIA DE RENOVAÇÃO\n");
        tendencia.append("-".repeat(40)).append("\n\n");

        if (pctAntigos > 50) {
            tendencia.append("🔴 ALTA necessidade de renovação\n");
            tendencia.append(String.format("   %.1f%% dos equipamentos têm mais de 5 anos.\n", pctAntigos));
            tendencia.append("   Recomendação: Iniciar plano de substituição imediato.\n");
        } else if (pctAntigos > 30) {
            tendencia.append("🟡 MÉDIA necessidade de renovação\n");
            tendencia.append(String.format("   %.1f%% dos equipamentos têm mais de 5 anos.\n", pctAntigos));
            tendencia.append("   Recomendação: Planejar orçamento para próximos 12 meses.\n");
        } else if (pctAntigos > 15) {
            tendencia.append("🟢 BAIXA necessidade de renovação\n");
            tendencia.append(String.format("   %.1f%% dos equipamentos têm mais de 5 anos.\n", pctAntigos));
            tendencia.append("   Recomendação: Monitorar e planejar com antecedência.\n");
        } else {
            tendencia.append("🟢 Inventário atualizado\n");
            tendencia.append(String.format("   Apenas %.1f%% dos equipamentos têm mais de 5 anos.\n", pctAntigos));
            tendencia.append("   Nenhuma ação urgente necessária.\n");
        }

        tendencia.append(String.format("\nPróximos 2 anos: %.1f%% dos equipamentos atingirão 5+ anos.\n", pctProximos));
        tendencia.append(String.format("   (%d equipamentos atualmente com 3-5 anos)\n", entre3e5));


        return tendencia.toString();
    }

    // =========================================================================
    // MÉTRICAS CONSOLIDADAS
    // =========================================================================

    /**
     * Retorna todas as métricas em um mapa consolidado.
     */
    public Map<String, Object> obterMetricasConsolidadas(List<InventarioEquipamentos> equipamentos,
                                                         List<CatalogoProdutos> catalogos) {
        Map<String, Object> metricas = new LinkedHashMap<>();
        long total = equipamentos.size();

        metricas.put("totalEquipamentos", total);
        metricas.put("equipamentosCriticos", equipamentos.stream().filter(e -> "CRITICO".equals(e.getCondicao())).count());
        metricas.put("emManutencao", equipamentos.stream().filter(e -> "MANUTENCAO".equals(e.getStatus())).count());
        metricas.put("baixados", equipamentos.stream().filter(e -> "BAIXADO".equals(e.getStatus())).count());
        metricas.put("ativos", equipamentos.stream().filter(e -> "ATIVO".equals(e.getStatus())).count());

        // Distribuições
        metricas.put("distribuicaoDepartamento", equipamentos.stream()
                .collect(Collectors.groupingBy(e -> e.getDepartamento() != null ? e.getDepartamento() : "Não Informado",
                        Collectors.counting())));
        metricas.put("distribuicaoStatus", equipamentos.stream()
                .collect(Collectors.groupingBy(e -> e.getStatus() != null ? e.getStatus() : "Não Informado",
                        Collectors.counting())));
        metricas.put("distribuicaoCondicao", equipamentos.stream()
                .collect(Collectors.groupingBy(e -> e.getCondicao() != null ? e.getCondicao() : "Não Informado",
                        Collectors.counting())));

        // Saúde
        metricas.put("saudeInventario", avaliarSaudeInventario(equipamentos, catalogos));

        // Valor total
        BigDecimal valorTotal = BigDecimal.ZERO;
        for (InventarioEquipamentos eq : equipamentos) {
            catalogos.stream().filter(c -> c.getSku().equals(eq.getSkuProduto())).findFirst()
                    .ifPresent(c -> { if (c.getPrecoUnitario() != null) valorTotal.add(c.getPrecoUnitario()); });
        }
        metricas.put("valorTotalEstimado", valorTotal);

        return metricas;
    }

    // =========================================================================
    // GETTERS
    // =========================================================================

    public CriticidadeCalculator getCriticidadeCalculator() { return criticidadeCalculator; }
    public BalanceamentoAnalyzer getBalanceamentoAnalyzer() { return balanceamentoAnalyzer; }
    public ProjecaoFinanceiraService getProjecaoFinanceiraService() { return projecaoFinanceiraService; }
    public EstoqueInteligenteService getEstoqueInteligenteService() { return estoqueInteligenteService; }
    public CustoBeneficioAnalyzer getCustoBeneficioAnalyzer() { return custoBeneficioAnalyzer; }
}