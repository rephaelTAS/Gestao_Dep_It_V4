package com.ossobo.gestaoDepIt.controllers.gestao.gia.analises;/*
 * ManutencaoPreventivaService v1.0
 *
 * Serviço de manutenção preventiva e verificação periódica.
 * Absorve o melhor do SistemaManutencaoAvancado antigo:
 * - Regras de verificação por tipo de equipamento
 * - Alertas de defeito, verificação, substituição
 * - Intervalos configuráveis por categoria
 *
 * v1.0: Versão inicial — integração com nova arquitetura
 */


import com.ossobo.gestaoDepIt.db.models.CatalogoProdutos;
import com.ossobo.gestaoDepIt.db.models.HistoricoEventos;
import com.ossobo.gestaoDepIt.db.models.InventarioEquipamentos;
import com.ossobo.gestaoDepIt.db.services.HistoricoEventosService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class ManutencaoPreventivaService {

    private static final Logger logger = LoggerFactory.getLogger(ManutencaoPreventivaService.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Intervalos de verificação (dias)
    private static final int VERIFICACAO_DEFEITOS = 8;
    private static final int VERIFICACAO_COMPUTADORES = 30;
    private static final int VERIFICACAO_IMPRESSORAS = 45;
    private static final int VERIFICACAO_GERAL = 60;
    private static final int ANOS_SUBSTITUICAO = 5;

    private final HistoricoEventosService historicoService;
    private final CriticidadeCalculator criticidadeCalculator;

    public ManutencaoPreventivaService(HistoricoEventosService historicoService,
                                       CriticidadeCalculator criticidadeCalculator) {
        this.historicoService = historicoService;
        this.criticidadeCalculator = criticidadeCalculator;
    }

    /**
     * Executa análise completa de manutenção preventiva.
     * @return Lista de alertas gerados
     */
    public List<AlertaManutencao> executarAnalise(List<InventarioEquipamentos> equipamentos,
                                                  List<CatalogoProdutos> catalogos) {
        List<AlertaManutencao> alertas = new ArrayList<>();
        LocalDate hoje = LocalDate.now();

        for (InventarioEquipamentos eq : equipamentos) {
            Optional<CatalogoProdutos> catalogo = catalogos.stream()
                    .filter(c -> c.getSku().equals(eq.getSkuProduto())).findFirst();

            // 1. Verificar condição crítica
            if ("CRITICO".equals(eq.getCondicao()) && deveAlertaDefeito(eq, hoje)) {
                alertas.add(new AlertaManutencao(
                        eq, TipoAlerta.DEFEITO,
                        "Equipamento em condição CRÍTICA — Última verificação: " + formatarData(eq.getDataUltimaVerificacao()),
                        "Verificar e reparar em até " + VERIFICACAO_DEFEITOS + " dias"
                ));
            }

            // 2. Verificação periódica
            if (deveVerificacaoPeriodica(eq, catalogo.orElse(null), hoje)) {
                int intervalo = getIntervaloVerificacao(catalogo.orElse(null));
                alertas.add(new AlertaManutencao(
                        eq, TipoAlerta.VERIFICACAO,
                        "Verificação periódica necessária — Última: " + formatarData(eq.getDataUltimaVerificacao()),
                        "Agendar inspeção em até " + intervalo + " dias"
                ));
            }

            // 3. Substituição por idade
            if (eq.getDataAquisicao() != null) {
                long anos = ChronoUnit.YEARS.between(eq.getDataAquisicao(), hoje);
                if (anos >= ANOS_SUBSTITUICAO) {
                    alertas.add(new AlertaManutencao(
                            eq, TipoAlerta.SUBSTITUICAO,
                            "Equipamento com " + anos + " anos de uso",
                            "Planejar substituição no próximo orçamento"
                    ));
                }
            }
        }

        logger.info("Análise de manutenção: {} alertas gerados para {} equipamentos",
                alertas.size(), equipamentos.size());
        return alertas;
    }

    /**
     * Retorna apenas alertas urgentes (DEFEITO + CRÍTICO).
     */
    public List<AlertaManutencao> getAlertasUrgentes(List<InventarioEquipamentos> equipamentos,
                                                     List<CatalogoProdutos> catalogos) {
        return executarAnalise(equipamentos, catalogos).stream()
                .filter(a -> a.getTipo() == TipoAlerta.DEFEITO)
                .collect(Collectors.toList());
    }

    /**
     * Retorna equipamentos que precisam de verificação nos próximos N dias.
     */
    public List<AlertaManutencao> getVerificacoesProximas(List<InventarioEquipamentos> equipamentos,
                                                          List<CatalogoProdutos> catalogos,
                                                          int diasLimite) {
        LocalDate hoje = LocalDate.now();
        LocalDate limite = hoje.plusDays(diasLimite);

        return equipamentos.stream()
                .filter(e -> e.getDataUltimaVerificacao() != null)
                .filter(e -> {
                    Optional<CatalogoProdutos> cp = catalogos.stream()
                            .filter(c -> c.getSku().equals(e.getSkuProduto())).findFirst();
                    int intervalo = getIntervaloVerificacao(cp.orElse(null));
                    LocalDate proximaVerificacao = e.getDataUltimaVerificacao().plusDays(intervalo);
                    return !proximaVerificacao.isAfter(limite) && !proximaVerificacao.isBefore(hoje);
                })
                .map(e -> new AlertaManutencao(e, TipoAlerta.VERIFICACAO,
                        "Verificação programada para: " + formatarData(e.getDataUltimaVerificacao()),
                        "Dentro do prazo"))
                .collect(Collectors.toList());
    }

    // ===== REGRAS =====

    private boolean deveAlertaDefeito(InventarioEquipamentos eq, LocalDate hoje) {
        if (eq.getDataUltimaVerificacao() == null) return true;
        return ChronoUnit.DAYS.between(eq.getDataUltimaVerificacao(), hoje) >= VERIFICACAO_DEFEITOS;
    }

    private boolean deveVerificacaoPeriodica(InventarioEquipamentos eq, CatalogoProdutos catalogo, LocalDate hoje) {
        if (eq.getDataUltimaVerificacao() == null) return true;
        int intervalo = getIntervaloVerificacao(catalogo);
        return ChronoUnit.DAYS.between(eq.getDataUltimaVerificacao(), hoje) >= intervalo;
    }

    private int getIntervaloVerificacao(CatalogoProdutos catalogo) {
        if (catalogo == null) return VERIFICACAO_GERAL;

        String desc = catalogo.getDescricao() != null ? catalogo.getDescricao().toLowerCase() : "";
        String cat = catalogo.getCategoria() != null ? catalogo.getCategoria().toUpperCase() : "";

        if (cat.contains("COMPUTADOR") || cat.contains("NOTEBOOK") || cat.contains("DESKTOP") ||
                desc.contains("computador") || desc.contains("notebook")) {
            return VERIFICACAO_COMPUTADORES;
        }
        if (cat.contains("IMPRESSORA") || desc.contains("impressora")) {
            return VERIFICACAO_IMPRESSORAS;
        }
        return VERIFICACAO_GERAL;
    }

    private String formatarData(LocalDate data) {
        return data != null ? data.format(DATE_FORMAT) : "Nunca";
    }

    // ===== INNER CLASSES =====

    public enum TipoAlerta {
        DEFEITO, VERIFICACAO, SUBSTITUICAO, GARANTIA, LICENCA
    }

    public static class AlertaManutencao {
        private final InventarioEquipamentos equipamento;
        private final TipoAlerta tipo;
        private final String descricao;
        private final String acao;

        public AlertaManutencao(InventarioEquipamentos equipamento, TipoAlerta tipo, String descricao, String acao) {
            this.equipamento = equipamento;
            this.tipo = tipo;
            this.descricao = descricao;
            this.acao = acao;
        }

        public InventarioEquipamentos getEquipamento() { return equipamento; }
        public TipoAlerta getTipo() { return tipo; }
        public String getDescricao() { return descricao; }
        public String getAcao() { return acao; }

        public String getIcone() {
            return switch (tipo) {
                case DEFEITO -> "🔴";
                case SUBSTITUICAO -> "🟠";
                case VERIFICACAO -> "🟡";
                case GARANTIA -> "🔵";
                case LICENCA -> "🟣";
            };
        }

        @Override
        public String toString() {
            return String.format("%s [%s] %s → %s", getIcone(), tipo, descricao, acao);
        }
    }
}