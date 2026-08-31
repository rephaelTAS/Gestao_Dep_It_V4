package com.ossobo.gestaoDepIt.controllers.gestao.gia.analises;/*
 * ProjecaoFinanceiraService v1.0
 *
 * Calcula projeções financeiras: depreciação, substituição e orçamento futuro.
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

public class ProjecaoFinanceiraService {

    private static final Logger logger = LoggerFactory.getLogger(ProjecaoFinanceiraService.class);

    // Taxa de depreciação anual padrão
    private static final double TAXA_DEPRECIACAO_ANUAL = 0.20; // 20% ao ano
    // Anos de projeção
    private static final int ANOS_PROJECAO = 5;

    /**
     * Calcula a depreciação de um equipamento.
     * Método linear: valorOriginal * (1 - taxa * idade)
     */
    public BigDecimal calcularDepreciacao(InventarioEquipamentos equipamento, BigDecimal valorOriginal) {
        if (equipamento.getDataAquisicao() == null || valorOriginal == null) return BigDecimal.ZERO;

        long idadeAnos = ChronoUnit.YEARS.between(equipamento.getDataAquisicao(), LocalDate.now());
        double fatorDepreciacao = Math.max(0, 1 - (TAXA_DEPRECIACAO_ANUAL * idadeAnos));

        return valorOriginal.multiply(BigDecimal.valueOf(fatorDepreciacao))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calcula o valor atual total do parque tecnológico (depreciado).
     */
    public BigDecimal calcularValorAtualParque(List<InventarioEquipamentos> equipamentos,
                                               List<CatalogoProdutos> catalogos) {
        BigDecimal total = BigDecimal.ZERO;

        for (InventarioEquipamentos eq : equipamentos) {
            Optional<CatalogoProdutos> cp = catalogos.stream()
                    .filter(c -> c.getSku().equals(eq.getSkuProduto()))
                    .findFirst();

            if (cp.isPresent() && cp.get().getPrecoUnitario() != null) {
                total = total.add(calcularDepreciacao(eq, cp.get().getPrecoUnitario()));
            }
        }

        return total;
    }

    /**
     * Projeta os custos de substituição para os próximos N anos.
     * @return Mapa: Ano (ex: 2026) → Custo projetado
     */
    public Map<Integer, BigDecimal> projetarSubstituicoes(List<InventarioEquipamentos> equipamentos,
                                                          List<CatalogoProdutos> catalogos) {
        Map<Integer, BigDecimal> projecao = new LinkedHashMap<>();
        int anoAtual = LocalDate.now().getYear();

        for (int i = 0; i < ANOS_PROJECAO; i++) {
            final int ano = anoAtual + i;
            final BigDecimal[] custoAno = {BigDecimal.ZERO};  // ✅ Array é effectively final

            for (InventarioEquipamentos eq : equipamentos) {
                if (eq.getDataAquisicao() == null) continue;

                int idadeNoAno = ano - eq.getDataAquisicao().getYear();
                int vidaUtil = estimarVidaUtil(eq, catalogos);

                if (idadeNoAno >= vidaUtil) {
                    catalogos.stream()
                            .filter(c -> c.getSku().equals(eq.getSkuProduto()))
                            .findFirst()
                            .ifPresent(c -> {
                                if (c.getPrecoUnitario() != null) {
                                    custoAno[0] = custoAno[0].add(c.getPrecoUnitario());  // ✅ Modifica conteúdo do array
                                }
                            });
                }
            }

            projecao.put(ano, custoAno[0]);
        }

        return projecao;
    }

    /**
     * Calcula o orçamento anual recomendado para manter o parque saudável.
     */
    public BigDecimal calcularOrcamentoAnualRecomendado(List<InventarioEquipamentos> equipamentos,
                                                        List<CatalogoProdutos> catalogos) {
        Map<Integer, BigDecimal> projecao = projetarSubstituicoes(equipamentos, catalogos);

        BigDecimal totalProjecao = BigDecimal.ZERO;
        for (BigDecimal valor : projecao.values()) {
            totalProjecao = totalProjecao.add(valor);
        }

        return totalProjecao.divide(BigDecimal.valueOf(ANOS_PROJECAO), 2, RoundingMode.HALF_UP);
    }

    /**
     * Gera a curva de depreciação acumulada ano a ano.
     */
    public Map<Integer, BigDecimal> gerarCurvaDepreciacao(List<InventarioEquipamentos> equipamentos,
                                                          List<CatalogoProdutos> catalogos) {
        Map<Integer, BigDecimal> curva = new LinkedHashMap<>();
        int anoAtual = LocalDate.now().getYear();

        for (int i = 0; i < ANOS_PROJECAO; i++) {
            int ano = anoAtual + i;
            BigDecimal valorRestante = BigDecimal.ZERO;

            for (InventarioEquipamentos eq : equipamentos) {
                Optional<CatalogoProdutos> cp = catalogos.stream()
                        .filter(c -> c.getSku().equals(eq.getSkuProduto()))
                        .findFirst();

                if (cp.isPresent() && cp.get().getPrecoUnitario() != null && eq.getDataAquisicao() != null) {
                    int idade = ano - eq.getDataAquisicao().getYear();
                    double fator = Math.max(0, 1 - (TAXA_DEPRECIACAO_ANUAL * idade));
                    valorRestante = valorRestante.add(
                            cp.get().getPrecoUnitario().multiply(BigDecimal.valueOf(fator)));
                }
            }

            curva.put(ano, valorRestante.setScale(2, RoundingMode.HALF_UP));
        }

        return curva;
    }

    /**
     * Estima a vida útil com base na categoria do catálogo.
     */
    private int estimarVidaUtil(InventarioEquipamentos eq, List<CatalogoProdutos> catalogos) {
        return catalogos.stream()
                .filter(c -> c.getSku().equals(eq.getSkuProduto()))
                .findFirst()
                .map(c -> switch (c.getCategoria() != null ? c.getCategoria().toUpperCase() : "") {
                    case "NOTEBOOK", "LAPTOP" -> 5;
                    case "DESKTOP", "COMPUTADOR" -> 6;
                    case "IMPRESSORA" -> 7;
                    case "SERVIDOR" -> 8;
                    case "MONITOR" -> 7;
                    default -> 5;
                })
                .orElse(5);
    }

    // ===== INNER CLASS =====

    public static class ProjecaoFinanceira {
        private final Map<Integer, BigDecimal> projecaoSubstituicao;
        private final Map<Integer, BigDecimal> curvaDepreciacao;
        private final BigDecimal orcamentoAnualRecomendado;
        private final BigDecimal valorAtualParque;
        private final BigDecimal valorOriginalParque;

        public ProjecaoFinanceira(Map<Integer, BigDecimal> projecaoSubstituicao,
                                  Map<Integer, BigDecimal> curvaDepreciacao,
                                  BigDecimal orcamentoAnualRecomendado,
                                  BigDecimal valorAtualParque,
                                  BigDecimal valorOriginalParque) {
            this.projecaoSubstituicao = projecaoSubstituicao;
            this.curvaDepreciacao = curvaDepreciacao;
            this.orcamentoAnualRecomendado = orcamentoAnualRecomendado;
            this.valorAtualParque = valorAtualParque;
            this.valorOriginalParque = valorOriginalParque;
        }

        public Map<Integer, BigDecimal> getProjecaoSubstituicao() { return projecaoSubstituicao; }
        public Map<Integer, BigDecimal> getCurvaDepreciacao() { return curvaDepreciacao; }
        public BigDecimal getOrcamentoAnualRecomendado() { return orcamentoAnualRecomendado; }
        public BigDecimal getValorAtualParque() { return valorAtualParque; }
        public BigDecimal getValorOriginalParque() { return valorOriginalParque; }
    }
}