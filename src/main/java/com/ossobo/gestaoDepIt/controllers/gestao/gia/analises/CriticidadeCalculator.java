package com.ossobo.gestaoDepIt.controllers.gestao.gia.analises;
/*
 * CriticidadeCalculator v1.0
 *
 * Algoritmo de criticidade para equipamentos do inventário.
 * Calcula score baseado em: idade, condição, garantia, falhas e depreciação.
 *
 * Fórmula: Score = (idade * 0.30) + (condicao * 0.25) + (garantia * 0.20) + (falhas * 0.15) + (depreciacao * 0.10)
 * Score 0-30: ÓTIMO | 31-50: BOM | 51-70: REGULAR | 71-85: CRÍTICO | 86-100: SUCATA
 *
 * v1.0: Versão inicial
 */


import com.ossobo.gestaoDepIt.db.models.HistoricoEventos;
import com.ossobo.gestaoDepIt.db.models.InventarioEquipamentos;
import com.ossobo.gestaoDepIt.db.services.HistoricoEventosService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class CriticidadeCalculator {

    private static final Logger logger = LoggerFactory.getLogger(CriticidadeCalculator.class);
    private final HistoricoEventosService historicoService;

    // Pesos do algoritmo
    private static final double PESO_IDADE = 0.30;
    private static final double PESO_CONDICAO = 0.25;
    private static final double PESO_GARANTIA = 0.20;
    private static final double PESO_FALHAS = 0.15;
    private static final double PESO_DEPRECIACAO = 0.10;

    // Vida útil esperada por categoria (em anos)
    private static final int VIDA_UTIL_NOTEBOOK = 5;
    private static final int VIDA_UTIL_DESKTOP = 6;
    private static final int VIDA_UTIL_IMPRESSORA = 7;
    private static final int VIDA_UTIL_SERVIDOR = 8;
    private static final int VIDA_UTIL_MONITOR = 7;
    private static final int VIDA_UTIL_PADRAO = 5;

    public CriticidadeCalculator(HistoricoEventosService historicoService) {
        this.historicoService = historicoService;
    }

    /**
     * Calcula o score de criticidade de um equipamento.
     * @return CriticidadeResult com score, classificação e recomendação
     */
    public CriticidadeResult calcular(InventarioEquipamentos equipamento, String categoria) {
        double scoreIdade = calcularScoreIdade(equipamento, categoria);
        double scoreCondicao = calcularScoreCondicao(equipamento);
        double scoreGarantia = calcularScoreGarantia(equipamento);
        double scoreFalhas = calcularScoreFalhas(equipamento);
        double scoreDepreciacao = calcularScoreDepreciacao(equipamento, categoria);

        double scoreTotal = (scoreIdade * PESO_IDADE) +
                (scoreCondicao * PESO_CONDICAO) +
                (scoreGarantia * PESO_GARANTIA) +
                (scoreFalhas * PESO_FALHAS) +
                (scoreDepreciacao * PESO_DEPRECIACAO);

        String classificacao = classificar(scoreTotal);
        String recomendacao = gerarRecomendacao(scoreTotal, equipamento, categoria);
        int vidaUtilRestante = calcularVidaUtilRestante(equipamento, categoria);

        return new CriticidadeResult(equipamento, scoreTotal, classificacao, recomendacao, vidaUtilRestante);
    }

    /**
     * Calcula criticidade para uma lista de equipamentos e retorna ordenado por criticidade.
     */
    public List<CriticidadeResult> calcularEmLote(List<InventarioEquipamentos> equipamentos, String categoria) {
        return equipamentos.stream()
                .map(e -> calcular(e, categoria))
                .sorted(Comparator.comparingDouble(CriticidadeResult::getScore).reversed())
                .collect(Collectors.toList());
    }

    // ===== CÁLCULOS INDIVIDUAIS =====

    private double calcularScoreIdade(InventarioEquipamentos eq, String categoria) {
        if (eq.getDataAquisicao() == null) return 50; // sem data = médio risco

        int vidaUtil = getVidaUtilPorCategoria(categoria);
        long idade = ChronoUnit.YEARS.between(eq.getDataAquisicao(), LocalDate.now());

        if (idade >= vidaUtil) return 100;  // já passou da vida útil
        if (idade >= vidaUtil * 0.8) return 80; // próximo do fim
        if (idade >= vidaUtil * 0.5) return 50; // meia-vida
        if (idade >= vidaUtil * 0.3) return 25; // ainda bom
        return 5; // novo
    }

    private double calcularScoreCondicao(InventarioEquipamentos eq) {
        if (eq.getCondicao() == null) return 50;
        return switch (eq.getCondicao()) {
            case "OTIMO" -> 5;
            case "BOM" -> 25;
            case "REGULAR" -> 55;
            case "CRITICO" -> 90;
            default -> 50;
        };
    }

    private double calcularScoreGarantia(InventarioEquipamentos eq) {
        if (eq.getDataAquisicao() == null) return 80;

        long anosDesdeCompra = ChronoUnit.YEARS.between(eq.getDataAquisicao(), LocalDate.now());

        // Garantia típica: 1-3 anos
        if (anosDesdeCompra <= 1) return 5;   // dentro da garantia
        if (anosDesdeCompra <= 2) return 15;  // garantia estendida possível
        if (anosDesdeCompra <= 3) return 40;  // recém saiu da garantia
        if (anosDesdeCompra <= 5) return 70;  // fora de garantia há tempo
        return 100; // muito fora de garantia
    }

    private double calcularScoreFalhas(InventarioEquipamentos eq) {
        try {
            List<HistoricoEventos> eventos = historicoService.buscarPorSkuProduto(eq.getSkuProduto());
            long falhas = eventos.stream()
                    .filter(e -> "MANUTENCAO".equals(e.getTipoEvento()) || "BAIXA".equals(e.getTipoEvento()))
                    .count();

            if (falhas == 0) return 5;
            if (falhas == 1) return 30;
            if (falhas == 2) return 55;
            if (falhas <= 5) return 75;
            return 100; // muitas falhas
        } catch (Exception e) {
            logger.warn("Erro ao buscar falhas para SKU {}: {}", eq.getSkuProduto(), e.getMessage());
            return 50; // desconhecido = médio risco
        }
    }

    private double calcularScoreDepreciacao(InventarioEquipamentos eq, String categoria) {
        if (eq.getDataAquisicao() == null) return 50;

        int vidaUtil = getVidaUtilPorCategoria(categoria);
        long idade = ChronoUnit.YEARS.between(eq.getDataAquisicao(), LocalDate.now());

        double taxaAnual = 100.0 / vidaUtil;
        double depreciado = Math.min(100, idade * taxaAnual);
        return depreciado;
    }

    // ===== UTILITÁRIOS =====

    private int getVidaUtilPorCategoria(String categoria) {
        if (categoria == null) return VIDA_UTIL_PADRAO;
        return switch (categoria.toUpperCase()) {
            case "NOTEBOOK", "LAPTOP" -> VIDA_UTIL_NOTEBOOK;
            case "DESKTOP", "COMPUTADOR" -> VIDA_UTIL_DESKTOP;
            case "IMPRESSORA" -> VIDA_UTIL_IMPRESSORA;
            case "SERVIDOR" -> VIDA_UTIL_SERVIDOR;
            case "MONITOR" -> VIDA_UTIL_MONITOR;
            default -> VIDA_UTIL_PADRAO;
        };
    }

    private String classificar(double score) {
        if (score <= 30) return "OTIMO";
        if (score <= 50) return "BOM";
        if (score <= 70) return "REGULAR";
        if (score <= 85) return "CRITICO";
        return "SUCATA";
    }

    private String gerarRecomendacao(double score, InventarioEquipamentos eq, String categoria) {
        if (score <= 30) return "Equipamento em ótimo estado. Manter rotina normal de verificação.";
        if (score <= 50) return "Equipamento bom. Agendar verificação preventiva em 6 meses.";
        if (score <= 70) {
            int vidaRestante = calcularVidaUtilRestante(eq, categoria);
            return String.format("Atenção necessária. Substituir em %d meses. Iniciar orçamento.", Math.max(1, vidaRestante));
        }
        if (score <= 85) return "URGENTE: Substituir em até 3 meses. Risco de falha operacional.";
        return "SUBSTITUIR IMEDIATAMENTE. Equipamento obsoleto ou em estado crítico.";
    }

    private int calcularVidaUtilRestante(InventarioEquipamentos eq, String categoria) {
        if (eq.getDataAquisicao() == null) return 0;
        int vidaUtil = getVidaUtilPorCategoria(categoria);
        long idadeMeses = ChronoUnit.MONTHS.between(eq.getDataAquisicao(), LocalDate.now());
        return Math.max(0, (vidaUtil * 12) - (int) idadeMeses);
    }

    // ===== INNER CLASS =====

    /**
     * Resultado da análise de criticidade.
     */
    public static class CriticidadeResult {
        private final InventarioEquipamentos equipamento;
        private final double score;
        private final String classificacao;
        private final String recomendacao;
        private final int vidaUtilRestanteMeses;

        public CriticidadeResult(InventarioEquipamentos equipamento, double score,
                                 String classificacao, String recomendacao, int vidaUtilRestanteMeses) {
            this.equipamento = equipamento;
            this.score = score;
            this.classificacao = classificacao;
            this.recomendacao = recomendacao;
            this.vidaUtilRestanteMeses = vidaUtilRestanteMeses;
        }

        public InventarioEquipamentos getEquipamento() { return equipamento; }
        public double getScore() { return score; }
        public String getClassificacao() { return classificacao; }
        public String getRecomendacao() { return recomendacao; }
        public int getVidaUtilRestanteMeses() { return vidaUtilRestanteMeses; }

        public String getScoreFormatado() { return String.format("%.1f", score); }
        public String getVidaUtilFormatada() {
            int anos = vidaUtilRestanteMeses / 12;
            int meses = vidaUtilRestanteMeses % 12;
            if (anos > 0) return anos + "a " + meses + "m";
            return meses + " meses";
        }

        @Override
        public String toString() {
            return String.format("Criticidade[%s -> %.1f (%s): %s]",
                    equipamento.getSkuProduto(), score, classificacao, recomendacao);
        }
    }
}