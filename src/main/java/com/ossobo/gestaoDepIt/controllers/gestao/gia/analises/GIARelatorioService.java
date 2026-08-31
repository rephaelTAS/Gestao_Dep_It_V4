package com.ossobo.gestaoDepIt.controllers.gestao.gia.analises;/*
 * GIARelatorioService v1.0
 *
 * Serviço de relatórios do GIA.
 * Implementa a interface RelatorioService da versão antiga.
 * Gera relatórios textuais, exporta TXT/CSV, exibe em UI.
 *
 * v1.0: Versão inicial — integração com nova arquitetura
 */


import com.ossobo.gestaoDepIt.db.models.CatalogoProdutos;
import com.ossobo.gestaoDepIt.db.models.Funcionarios;
import com.ossobo.gestaoDepIt.db.models.InventarioEquipamentos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class GIARelatorioService {

    private static final Logger logger = LoggerFactory.getLogger(GIARelatorioService.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final GIAService giaService;
    private final GIARecommendationGenerator recommendationGenerator;
    private final CriticidadeCalculator criticidadeCalculator;

    private String nivelDetalhe = "NORMAL";
    private final Map<String, String> filtros = new HashMap<>();

    public GIARelatorioService(GIAService giaService,
                               GIARecommendationGenerator recommendationGenerator,
                               CriticidadeCalculator criticidadeCalculator) {
        this.giaService = giaService;
        this.recommendationGenerator = recommendationGenerator;
        this.criticidadeCalculator = criticidadeCalculator;
    }

    // =========================================================================
    // RELATÓRIO COMPLETO
    // =========================================================================

    public String gerarRelatorioCompleto(List<InventarioEquipamentos> equipamentos,
                                         List<CatalogoProdutos> catalogos,
                                         List<Funcionarios> funcionarios) {
        StringBuilder relatorio = new StringBuilder();

        relatorio.append("=".repeat(70)).append("\n");
        relatorio.append("RELATÓRIO COMPLETO DE GESTÃO INTELIGENTE DE ATIVOS\n");
        relatorio.append("=".repeat(70)).append("\n\n");
        relatorio.append("Data: ").append(LocalDate.now().format(DATE_FORMAT)).append("\n");
        relatorio.append("Nível: ").append(nivelDetalhe).append("\n\n");

        // Seção 1: Resumo Executivo
        relatorio.append("1. RESUMO EXECUTIVO\n").append("-".repeat(50)).append("\n");
        relatorio.append(giaService.gerarResumoExecutivo(equipamentos, catalogos)).append("\n\n");

        // Seção 2: Saúde do Inventário
        relatorio.append("2. SAÚDE DO INVENTÁRIO\n").append("-".repeat(50)).append("\n");
        Map<String, String> saude = giaService.avaliarSaudeInventario(equipamentos, catalogos);
        saude.forEach((k, v) -> relatorio.append(String.format("  %s: %s\n", k, v)));
        relatorio.append("\n");

        // Seção 3: Recomendações
        relatorio.append("3. RECOMENDAÇÕES\n").append("-".repeat(50)).append("\n");
        Map<String, List<GIARecommendationGenerator.Recomendacao>> recomendacoes =
                recommendationGenerator.gerarRecomendacoes(equipamentos, catalogos, funcionarios);
        recomendacoes.forEach((categoria, lista) -> {
            relatorio.append("\n").append(categoria).append(":\n");
            lista.forEach(r -> relatorio.append("  ").append(r.toString()).append("\n"));
        });
        relatorio.append("\n");

        // Seção 4: Top Críticos (se detalhado)
        if ("DETALHADO".equals(nivelDetalhe)) {
            relatorio.append("4. TOP 10 EQUIPAMENTOS CRÍTICOS\n").append("-".repeat(50)).append("\n");
            equipamentos.stream()
                    .filter(e -> "CRITICO".equals(e.getCondicao()) || "REGULAR".equals(e.getCondicao()))
                    .sorted((a, b) -> "CRITICO".equals(a.getCondicao()) ? -1 : 1)
                    .limit(10)
                    .forEach(e -> {
                        CriticidadeCalculator.CriticidadeResult cr = criticidadeCalculator.calcular(e, "GERAL");
                        relatorio.append(String.format("  SKU: %s | Score: %s | %s | %s\n",
                                e.getSkuProduto(), cr.getScoreFormatado(), cr.getClassificacao(), cr.getRecomendacao()));
                    });
            relatorio.append("\n");
        }

        relatorio.append("=".repeat(70)).append("\n");
        relatorio.append("FIM DO RELATÓRIO\n");
        relatorio.append("=".repeat(70)).append("\n");

        return relatorio.toString();
    }

    // =========================================================================
    // RELATÓRIO DE CRÍTICOS
    // =========================================================================

    public String gerarRelatorioCriticos(List<InventarioEquipamentos> equipamentos) {
        StringBuilder relatorio = new StringBuilder();
        long criticos = equipamentos.stream().filter(e -> "CRITICO".equals(e.getCondicao())).count();
        long total = equipamentos.size();

        relatorio.append("RELATÓRIO DE EQUIPAMENTOS CRÍTICOS\n");
        relatorio.append("=".repeat(50)).append("\n\n");
        relatorio.append(String.format("Total: %d | Críticos: %d (%.1f%%)\n\n", total, criticos,
                total > 0 ? criticos * 100.0 / total : 0));

        equipamentos.stream()
                .filter(e -> "CRITICO".equals(e.getCondicao()))
                .forEach(e -> {
                    CriticidadeCalculator.CriticidadeResult cr = criticidadeCalculator.calcular(e, "GERAL");
                    relatorio.append(String.format("SKU: %s | Score: %s | %s | %s\n",
                            e.getSkuProduto(), cr.getScoreFormatado(), cr.getVidaUtilFormatada(), cr.getRecomendacao()));
                });

        return relatorio.toString();
    }

    // =========================================================================
    // RELATÓRIO DEPARTAMENTAL
    // =========================================================================

    public String gerarRelatorioDepartamental(String departamento,
                                              List<InventarioEquipamentos> equipamentos) {
        List<InventarioEquipamentos> doDepto = equipamentos.stream()
                .filter(e -> departamento.equals(e.getDepartamento()))
                .collect(Collectors.toList());

        StringBuilder relatorio = new StringBuilder();
        relatorio.append("RELATÓRIO DEPARTAMENTAL: ").append(departamento).append("\n");
        relatorio.append("=".repeat(50)).append("\n\n");
        relatorio.append("Total de Equipamentos: ").append(doDepto.size()).append("\n");

        long criticos = doDepto.stream().filter(e -> "CRITICO".equals(e.getCondicao())).count();
        long manutencao = doDepto.stream().filter(e -> "MANUTENCAO".equals(e.getStatus())).count();
        relatorio.append(String.format("Críticos: %d | Em Manutenção: %d\n\n", criticos, manutencao));

        doDepto.forEach(e -> relatorio.append(String.format("  %s — %s | %s | %s\n",
                e.getSkuProduto(), e.getNumSerie(), e.getStatus(), e.getCondicao())));

        return relatorio.toString();
    }

    // =========================================================================
    // EXPORTAÇÃO
    // =========================================================================

    public boolean exportarParaTexto(String conteudo, String caminho) {
        try {
            if (caminho == null || caminho.trim().isEmpty()) {
                caminho = "relatorio_gia_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".txt";
            }
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(caminho))) {
                writer.write(conteudo);
                writer.flush();
            }
            logger.info("Relatório exportado: {}", caminho);
            return true;
        } catch (IOException e) {
            logger.error("Erro ao exportar relatório: {}", e.getMessage());
            return false;
        }
    }

    // =========================================================================
    // EXIBIÇÃO EM UI
    // =========================================================================

    public void exibirRelatorioUI(String conteudo, Stage stage) {
        TextArea textArea = new TextArea(conteudo);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefSize(750, 550);
        textArea.setStyle("-fx-font-family: 'Consolas', 'Monaco', monospace; -fx-font-size: 12px;");

        ScrollPane scrollPane = new ScrollPane(textArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Relatório GIA");
        dialog.setHeaderText("Relatório gerado em " + LocalDate.now().format(DATE_FORMAT));

        VBox content = new VBox(10);
        content.getChildren().add(scrollPane);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setPrefSize(800, 600);

        if (stage != null) dialog.initOwner(stage);
        dialog.showAndWait();
    }

    // =========================================================================
    // CONFIGURAÇÃO
    // =========================================================================

    public void setNivelDetalhe(String nivel) { this.nivelDetalhe = nivel; }
    public void adicionarFiltro(String chave, String valor) { filtros.put(chave, valor); }
    public void limparFiltros() { filtros.clear(); }
}