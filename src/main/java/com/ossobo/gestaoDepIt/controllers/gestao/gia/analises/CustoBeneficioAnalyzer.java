package com.ossobo.gestaoDepIt.controllers.gestao.gia.analises;/*
 * CustoBeneficioAnalyzer v1.0
 *
 * Analisa relação custo-benefício dos equipamentos do catálogo.
 * Ranking baseado em: preço, vida útil, especificações técnicas (CPU, RAM).
 *
 * v1.0: Versão inicial
 */


import com.ossobo.gestaoDepIt.db.models.CatalogoProdutos;
import com.ossobo.gestaoDepIt.db.models.InventarioEquipamentos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class CustoBeneficioAnalyzer {

    private static final Logger logger = LoggerFactory.getLogger(CustoBeneficioAnalyzer.class);

    /**
     * Gera ranking de melhor custo-benefício.
     * Score = (vida_util * fator_especificacoes) / preco
     * Quanto maior o score, melhor o custo-benefício.
     */
    public List<CustoBeneficioRanking> gerarRanking(List<CatalogoProdutos> catalogos,
                                                    List<InventarioEquipamentos> equipamentos) {
        List<CustoBeneficioRanking> ranking = new ArrayList<>();

        for (CatalogoProdutos cp : catalogos) {
            if (cp.getPrecoUnitario() == null || cp.getPrecoUnitario().compareTo(BigDecimal.ZERO) <= 0) continue;

            // Calcular idade média dos equipamentos deste SKU
            double idadeMedia = equipamentos.stream()
                    .filter(e -> e.getSkuProduto().equals(cp.getSku()))
                    .filter(e -> e.getDataAquisicao() != null)
                    .mapToLong(e -> ChronoUnit.YEARS.between(e.getDataAquisicao(), LocalDate.now()))
                    .average()
                    .orElse(0);

            int vidaUtil = estimarVidaUtil(cp);
            double fatorEspecificacoes = extrairFatorEspecificacoes(cp);
            double custoAno = idadeMedia > 0 ? cp.getPrecoUnitario().doubleValue() / Math.max(1, idadeMedia) : cp.getPrecoUnitario().doubleValue();

            // Score: quanto maior, melhor
            double score = (vidaUtil * fatorEspecificacoes * 100) / cp.getPrecoUnitario().doubleValue();
            score = Math.min(100, Math.max(0, score));

            CustoBeneficioRanking item = new CustoBeneficioRanking();
            item.setSku(cp.getSku());
            item.setNome(cp.getNomeCompleto());
            item.setProcessador(extrairProcessador(cp));
            item.setRam(extrairRAM(cp));
            item.setArmazenamento(extrairArmazenamento(cp));
            item.setPreco(cp.getPrecoUnitario());
            item.setVidaUtilEstimada(vidaUtil);
            item.setCustoPorAno(BigDecimal.valueOf(custoAno).setScale(2, RoundingMode.HALF_UP));
            item.setScore(score);
            item.setIdadeMedia(idadeMedia > 0 ? String.format("%.1f anos", idadeMedia) : "Novo");

            ranking.add(item);
        }

        ranking.sort(Comparator.comparingDouble(CustoBeneficioRanking::getScore).reversed());
        return ranking;
    }

    /**
     * Compara dois equipamentos lado a lado.
     */
    public String compararEquipamentos(CatalogoProdutos a, CatalogoProdutos b) {
        double scoreA = calcularScoreIndividual(a);
        double scoreB = calcularScoreIndividual(b);

        StringBuilder sb = new StringBuilder();
        sb.append("Comparação:\n");
        sb.append(String.format("  %s: Score %.1f\n", a.getNomeCompleto(), scoreA));
        sb.append(String.format("  %s: Score %.1f\n", b.getNomeCompleto(), scoreB));

        if (scoreA > scoreB) {
            sb.append(String.format("✅ %s tem melhor custo-benefício (%.1f%% superior)", a.getNomeCompleto(), (scoreA / scoreB - 1) * 100));
        } else if (scoreB > scoreA) {
            sb.append(String.format("✅ %s tem melhor custo-benefício (%.1f%% superior)", b.getNomeCompleto(), (scoreB / scoreA - 1) * 100));
        } else {
            sb.append("⚖️ Ambos têm custo-benefício equivalente.");
        }

        return sb.toString();
    }

    private double calcularScoreIndividual(CatalogoProdutos cp) {
        if (cp.getPrecoUnitario() == null || cp.getPrecoUnitario().compareTo(BigDecimal.ZERO) <= 0) return 0;
        int vidaUtil = estimarVidaUtil(cp);
        double fator = extrairFatorEspecificacoes(cp);
        return Math.min(100, (vidaUtil * fator * 100) / cp.getPrecoUnitario().doubleValue());
    }

    private int estimarVidaUtil(CatalogoProdutos cp) {
        String cat = cp.getCategoria() != null ? cp.getCategoria().toUpperCase() : "";
        return switch (cat) {
            case "NOTEBOOK", "LAPTOP" -> 5;
            case "DESKTOP", "COMPUTADOR" -> 6;
            case "IMPRESSORA" -> 7;
            case "SERVIDOR" -> 8;
            case "MONITOR" -> 7;
            default -> 5;
        };
    }

    /**
     * Extrai fator de especificações técnicas (CPU + RAM + Armazenamento).
     * Valor normalizado entre 0.5 e 2.0.
     */
    private double extrairFatorEspecificacoes(CatalogoProdutos cp) {
        double fator = 1.0;

        String json = cp.getCaracteristicasTecnicas();
        if (json == null) return fator;

        // Processador: i7 > i5 > i3
        if (json.contains("i7") || json.contains("Ryzen 7")) fator += 0.4;
        else if (json.contains("i5") || json.contains("Ryzen 5")) fator += 0.2;
        else if (json.contains("i3") || json.contains("Ryzen 3")) fator += 0.0;

        // RAM
        try {
            if (json.contains("\"ram\"")) {
                String[] partes = json.split("\"ram\":\"");
                if (partes.length > 1) {
                    String ramStr = partes[1].split("\"")[0].replaceAll("[^0-9]", "");
                    int ram = Integer.parseInt(ramStr);
                    if (ram >= 32) fator += 0.3;
                    else if (ram >= 16) fator += 0.2;
                    else if (ram >= 8) fator += 0.1;
                }
            }
        } catch (Exception ignored) {}

        return Math.max(0.5, Math.min(2.0, fator));
    }

    private String extrairProcessador(CatalogoProdutos cp) {
        String json = cp.getCaracteristicasTecnicas();
        if (json != null && json.contains("\"processador\"")) {
            try {
                String[] partes = json.split("\"processador\":\"");
                if (partes.length > 1) return partes[1].split("\"")[0];
            } catch (Exception ignored) {}
        }
        return "-";
    }

    private String extrairRAM(CatalogoProdutos cp) {
        String json = cp.getCaracteristicasTecnicas();
        if (json != null && json.contains("\"ram\"")) {
            try {
                String[] partes = json.split("\"ram\":\"");
                if (partes.length > 1) return partes[1].split("\"")[0];
            } catch (Exception ignored) {}
        }
        return "-";
    }

    private String extrairArmazenamento(CatalogoProdutos cp) {
        String json = cp.getCaracteristicasTecnicas();
        if (json != null && json.contains("\"armazenamento\"")) {
            try {
                String[] partes = json.split("\"armazenamento\":\"");
                if (partes.length > 1) return partes[1].split("\"")[0];
            } catch (Exception ignored) {}
        }
        return "-";
    }

    // ===== INNER CLASS =====

    public static class CustoBeneficioRanking {
        private String sku;
        private String nome;
        private String processador;
        private String ram;
        private String armazenamento;
        private BigDecimal preco;
        private int vidaUtilEstimada;
        private BigDecimal custoPorAno;
        private double score;
        private String idadeMedia;

        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }
        public String getNome() { return nome; }
        public void setNome(String nome) { this.nome = nome; }
        public String getProcessador() { return processador; }
        public void setProcessador(String processador) { this.processador = processador; }
        public String getRam() { return ram; }
        public void setRam(String ram) { this.ram = ram; }
        public String getArmazenamento() { return armazenamento; }
        public void setArmazenamento(String armazenamento) { this.armazenamento = armazenamento; }
        public BigDecimal getPreco() { return preco; }
        public void setPreco(BigDecimal preco) { this.preco = preco; }
        public int getVidaUtilEstimada() { return vidaUtilEstimada; }
        public void setVidaUtilEstimada(int vidaUtilEstimada) { this.vidaUtilEstimada = vidaUtilEstimada; }
        public BigDecimal getCustoPorAno() { return custoPorAno; }
        public void setCustoPorAno(BigDecimal custoPorAno) { this.custoPorAno = custoPorAno; }
        public double getScore() { return score; }
        public void setScore(double score) { this.score = score; }
        public String getIdadeMedia() { return idadeMedia; }
        public void setIdadeMedia(String idadeMedia) { this.idadeMedia = idadeMedia; }
    }
}