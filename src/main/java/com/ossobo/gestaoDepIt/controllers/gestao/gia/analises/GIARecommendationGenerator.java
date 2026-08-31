package com.ossobo.gestaoDepIt.controllers.gestao.gia.analises;/*
 * GIARecommendationGenerator v1.0
 *
 * Gerador de recomendações categorizadas e priorizadas.
 * Absorve o melhor do EquipmentRecommendationGenerator antigo:
 * - Categorias: Manutenção, Qualidade de Dados, Substituição, Distribuição, Gestão Proativa
 * - Prioridades (🔴🟡🟢)
 * - Top 3 recomendações mais urgentes
 *
 * v1.0: Versão inicial — integração com nova arquitetura
 */


import com.ossobo.gestaoDepIt.db.models.CatalogoProdutos;
import com.ossobo.gestaoDepIt.db.models.Funcionarios;
import com.ossobo.gestaoDepIt.db.models.InventarioEquipamentos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class GIARecommendationGenerator {

    private static final Logger logger = LoggerFactory.getLogger(GIARecommendationGenerator.class);

    private final CriticidadeCalculator criticidadeCalculator;
    private final BalanceamentoAnalyzer balanceamentoAnalyzer;
    private final EstoqueInteligenteService estoqueInteligenteService;

    public GIARecommendationGenerator(CriticidadeCalculator criticidadeCalculator,
                                      BalanceamentoAnalyzer balanceamentoAnalyzer,
                                      EstoqueInteligenteService estoqueInteligenteService) {
        this.criticidadeCalculator = criticidadeCalculator;
        this.balanceamentoAnalyzer = balanceamentoAnalyzer;
        this.estoqueInteligenteService = estoqueInteligenteService;
    }

    /**
     * Gera todas as recomendações categorizadas.
     */
    public Map<String, List<Recomendacao>> gerarRecomendacoes(List<InventarioEquipamentos> equipamentos,
                                                              List<CatalogoProdutos> catalogos,
                                                              List<Funcionarios> funcionarios) {
        Map<String, List<Recomendacao>> recomendacoes = new LinkedHashMap<>();

        recomendacoes.put("🔴 Manutenção Urgente", gerarRecomendacoesManutencao(equipamentos));
        recomendacoes.put("📊 Qualidade de Dados", gerarRecomendacoesQualidadeDados(equipamentos));
        recomendacoes.put("🔄 Substituição e Renovação", gerarRecomendacoesSubstituicao(equipamentos));
        recomendacoes.put("⚖️ Distribuição e Balanceamento", gerarRecomendacoesDistribuicao(equipamentos, funcionarios));
        recomendacoes.put("📦 Gestão de Estoque", gerarRecomendacoesEstoque(catalogos));
        recomendacoes.put("💡 Gestão Proativa", gerarRecomendacoesProativas(equipamentos));

        return recomendacoes;
    }

    /**
     * Retorna as 3 recomendações mais prioritárias.
     */
    public List<Recomendacao> getTop3Recomendacoes(List<InventarioEquipamentos> equipamentos,
                                                   List<CatalogoProdutos> catalogos,
                                                   List<Funcionarios> funcionarios) {
        Map<String, List<Recomendacao>> todas = gerarRecomendacoes(equipamentos, catalogos, funcionarios);

        return todas.values().stream()
                .flatMap(List::stream)
                .filter(r -> r.getPrioridade() == Prioridade.URGENTE || r.getPrioridade() == Prioridade.ALTA)
                .sorted(Comparator.comparing(r -> r.getPrioridade().ordinal()))
                .limit(3)
                .collect(Collectors.toList());
    }

    // ===== RECOMENDAÇÕES POR CATEGORIA =====

    private List<Recomendacao> gerarRecomendacoesManutencao(List<InventarioEquipamentos> equipamentos) {
        List<Recomendacao> recs = new ArrayList<>();

        long criticos = equipamentos.stream().filter(e -> "CRITICO".equals(e.getCondicao())).count();
        if (criticos > 0) {
            recs.add(new Recomendacao(
                    Prioridade.URGENTE,
                    String.format("%d equipamentos em condição CRÍTICA necessitam de verificação imediata", criticos),
                    "Substituir ou reparar em até 48 horas"
            ));
        }

        long manutencao = equipamentos.stream().filter(e -> "MANUTENCAO".equals(e.getStatus())).count();
        if (manutencao > 0) {
            recs.add(new Recomendacao(
                    Prioridade.ALTA,
                    String.format("Acompanhar %d equipamentos em manutenção — verificar prazos", manutencao),
                    "Contatar oficina para atualização de status"
            ));
        }

        long semVerificacao = equipamentos.stream()
                .filter(e -> e.getDataUltimaVerificacao() == null ||
                        ChronoUnit.MONTHS.between(e.getDataUltimaVerificacao(), LocalDate.now()) > 6)
                .count();
        if (semVerificacao > 0) {
            recs.add(new Recomendacao(
                    Prioridade.MEDIA,
                    String.format("%d equipamentos sem verificação há mais de 6 meses", semVerificacao),
                    "Agendar rodada de verificação preventiva em 30 dias"
            ));
        }

        return recs;
    }

    private List<Recomendacao> gerarRecomendacoesQualidadeDados(List<InventarioEquipamentos> equipamentos) {
        List<Recomendacao> recs = new ArrayList<>();

        long semDepto = equipamentos.stream().filter(e -> e.getDepartamento() == null || e.getDepartamento().isEmpty()).count();
        long semCondicao = equipamentos.stream().filter(e -> e.getCondicao() == null || e.getCondicao().isEmpty()).count();
        long semData = equipamentos.stream().filter(e -> e.getDataAquisicao() == null).count();
        long totalFaltantes = semDepto + semCondicao + semData;

        if (totalFaltantes > equipamentos.size() * 0.2) {
            recs.add(new Recomendacao(
                    Prioridade.ALTA,
                    String.format("%d campos críticos faltantes em %d equipamentos", totalFaltantes, equipamentos.size()),
                    "Realizar mutirão de atualização cadastral"
            ));
        }

        if (semDepto > 0) {
            recs.add(new Recomendacao(
                    Prioridade.MEDIA,
                    String.format("%d equipamentos sem departamento atribuído", semDepto),
                    "Completar cadastro de departamento"
            ));
        }

        return recs;
    }

    private List<Recomendacao> gerarRecomendacoesSubstituicao(List<InventarioEquipamentos> equipamentos) {
        List<Recomendacao> recs = new ArrayList<>();

        long maisDe5Anos = equipamentos.stream()
                .filter(e -> e.getDataAquisicao() != null &&
                        ChronoUnit.YEARS.between(e.getDataAquisicao(), LocalDate.now()) > 5)
                .count();

        long criticosAntigos = equipamentos.stream()
                .filter(e -> "CRITICO".equals(e.getCondicao()) && e.getDataAquisicao() != null &&
                        ChronoUnit.YEARS.between(e.getDataAquisicao(), LocalDate.now()) > 4)
                .count();

        if (criticosAntigos > 0) {
            recs.add(new Recomendacao(
                    Prioridade.URGENTE,
                    String.format("%d equipamentos críticos com mais de 4 anos — substituir imediatamente", criticosAntigos),
                    "Iniciar processo de compra emergencial"
            ));
        }

        if (maisDe5Anos > 0) {
            recs.add(new Recomendacao(
                    Prioridade.ALTA,
                    String.format("Planejar substituição de %d equipamentos com mais de 5 anos", maisDe5Anos),
                    "Incluir no orçamento do próximo trimestre"
            ));
        }

        return recs;
    }

    private List<Recomendacao> gerarRecomendacoesDistribuicao(List<InventarioEquipamentos> equipamentos,
                                                              List<Funcionarios> funcionarios) {
        List<Recomendacao> recs = new ArrayList<>();

        if (balanceamentoAnalyzer != null && !funcionarios.isEmpty()) {
            BalanceamentoAnalyzer.BalanceamentoResult result =
                    balanceamentoAnalyzer.analisar(equipamentos, funcionarios);

            if (!result.getAcimaDaMedia().isEmpty()) {
                BalanceamentoAnalyzer.FuncionarioBalance maior = result.getAcimaDaMedia().get(0);
                recs.add(new Recomendacao(
                        Prioridade.MEDIA,
                        String.format("%s tem %d equipamentos (%.0f%% acima da média de %.1f)",
                                maior.getNome(), maior.getQuantidade(),
                                (maior.getQuantidade() / result.getMediaEquipamentos() - 1) * 100,
                                result.getMediaEquipamentos()),
                        "Avaliar redistribuição de equipamentos excedentes"
                ));
            }

            if (!result.getAbaixoDaMedia().isEmpty() && !result.getAcimaDaMedia().isEmpty()) {
                recs.add(new Recomendacao(
                        Prioridade.BAIXA,
                        "Há desbalanceamento entre funcionários com muito e pouco equipamento",
                        "Considerar política de redistribuição por departamento"
                ));
            }
        }

        return recs;
    }

    private List<Recomendacao> gerarRecomendacoesEstoque(List<CatalogoProdutos> catalogos) {
        List<Recomendacao> recs = new ArrayList<>();

        long estoqueBaixo = catalogos.stream().filter(c -> c.getTotalRecebido() != null && c.getTotalRecebido() <= 3).count();
        long estoqueZero = catalogos.stream().filter(c -> c.getTotalRecebido() != null && c.getTotalRecebido() == 0).count();

        if (estoqueZero > 0) {
            recs.add(new Recomendacao(
                    Prioridade.URGENTE,
                    String.format("%d itens com estoque ZERADO — risco de ruptura", estoqueZero),
                    "Comprar imediatamente"
            ));
        }

        if (estoqueBaixo > 0) {
            recs.add(new Recomendacao(
                    Prioridade.ALTA,
                    String.format("%d itens com estoque baixo (≤ 3 unidades)", estoqueBaixo),
                    "Incluir na próxima ordem de compra"
            ));
        }

        return recs;
    }

    private List<Recomendacao> gerarRecomendacoesProativas(List<InventarioEquipamentos> equipamentos) {
        List<Recomendacao> recs = new ArrayList<>();

        if (equipamentos.size() > 100) {
            recs.add(new Recomendacao(Prioridade.BAIXA, "Implementar dashboard gerencial com KPIs", "Prazo: 3 meses"));
        }

        recs.add(new Recomendacao(Prioridade.BAIXA, "Realizar inventário físico anual", "Agendar para próximo trimestre"));
        recs.add(new Recomendacao(Prioridade.BAIXA, "Documentar procedimentos de manutenção por tipo de equipamento", "Melhoria contínua"));

        return recs;
    }

    // ===== INNER CLASSES =====

    public enum Prioridade {
        URGENTE, ALTA, MEDIA, BAIXA
    }

    public static class Recomendacao {
        private final Prioridade prioridade;
        private final String descricao;
        private final String acao;

        public Recomendacao(Prioridade prioridade, String descricao, String acao) {
            this.prioridade = prioridade;
            this.descricao = descricao;
            this.acao = acao;
        }

        public Prioridade getPrioridade() { return prioridade; }
        public String getDescricao() { return descricao; }
        public String getAcao() { return acao; }

        public String getIcone() {
            return switch (prioridade) {
                case URGENTE -> "🔴";
                case ALTA -> "🟠";
                case MEDIA -> "🟡";
                case BAIXA -> "🟢";
            };
        }

        @Override
        public String toString() {
            return String.format("%s %s → %s", getIcone(), descricao, acao);
        }
    }
}