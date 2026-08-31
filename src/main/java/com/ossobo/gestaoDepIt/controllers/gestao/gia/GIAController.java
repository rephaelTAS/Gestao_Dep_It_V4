/*
 * GIAController v1.4
 *
 * Controlador do Dashboard de Gestão Inteligente de Ativos.
 *
 * v1.4: Adicionado — cellValueFactory para colunas da tabela de inventário
 *       Adicionado — coluna de Ações com 2 botões (Detalhes Equip. + Detalhes Prod.)
 *       Adicionado — métodos abrirDetalhesEquipamento() e abrirDetalhesProduto()
 * v1.3: Integrado com GIAService, GIARelatorioService, ManutencaoPreventivaService, GIARecommendationGenerator
 */
package com.ossobo.gestaoDepIt.controllers.gestao.gia;

import com.ossobo.gestaoDepIt.controllers.gestao.equipamentos.inventario.InventarioControllerDetails;
import com.ossobo.gestaoDepIt.controllers.gestao.estoque.catalugoproduto.CatalogoProdutoDetailController;
import com.ossobo.gestaoDepIt.controllers.gestao.gia.analises.*;
import com.ossobo.gestaoDepIt.controllers.gestao.gia.functions.*;
import com.ossobo.gestaoDepIt.db.models.*;
import com.ossobo.gestaoDepIt.db.services.*;
import com.ossobo.nexusfx.NexusFX;
import com.ossobo.nexusfx.di.annotations.Controller;
import com.ossobo.nexusfx.di.annotations.Inject;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class GIAController {

    private static final Logger logger = LoggerFactory.getLogger(GIAController.class);

    // ===== SERVIÇOS INJETADOS =====
    @Inject private InventarioEquipamentosService inventarioService;
    @Inject private CatalogoProdutosService catalogoService;
    @Inject private FuncionariosService funcionariosService;
    @Inject private EstoqueMovimentacoesService movimentacoesService;
    @Inject private GestaoTonersService tonersService;
    @Inject private HistoricoEventosService historicoService;

    // ===== GAUGES =====
    @FXML private Label gaugeSaudeGeral, gaugeValorTotal, lblEmManutencao, gaugeCriticos, gaugePrevisaoSubstituicao;
    @FXML private ProgressBar barSaudeGeral;
    @FXML private Label labelSaudeDetalhe, labelValorDetalhe, labelManutencaoDetalhe, labelCriticosDetalhe, labelPrevisaoDetalhe;

    // ===== GRÁFICOS =====
    @FXML private PieChart pieChartStatus;
    @FXML private BarChart<String, Number> barChartDepartamento, barChartBalanceamento, graficoCondicao;
    @FXML private LineChart<String, Number> lineChartProjecao;

    // ===== TABELAS =====
    @FXML private TableView<InventarioEquipamentos> tblInventario;
    @FXML private TableView<GIADTOs.LocalizacaoStats> tblLocalizacao;
    @FXML private TableView<GIADTOs.CriticoItem> tabelaCriticos;
    @FXML private TableView<GIADTOs.RecomendacaoItem> tabelaRecomendacoes;
    @FXML private TableView<GIADTOs.CustoBeneficioItem> tabelaCustoBeneficio;
    @FXML private TableView<GIADTOs.EstoqueAlertaItem> tabelaEstoqueAlertas;
    @FXML private TableView<GIADTOs.ItemParado> tabelaItensParados;

    // ===== FILTROS =====
    @FXML private ComboBox<String> cbTipoProduto, cbStatus, cbCondicao, cbDepartamento, cbLocalizacao;
    @FXML private CheckBox ckApenasDefeito;

    // ===== RODAPÉ =====
    @FXML private Label lblTotalItens, labelUltimaAtualizacao, statusLabel;
    @FXML private ProgressIndicator progressIndicator;

    // ===== SUBCOMPONENTES (SRP) =====
    private GIADataLoader dataLoader;
    private GIAGaugesBuilder gaugesBuilder;
    private GIAChartBuilder chartBuilder;
    private GIATableBuilder tableBuilder;

    // ===== SERVICES DE INTELIGÊNCIA =====
    private CriticidadeCalculator criticidadeCalculator;
    private BalanceamentoAnalyzer balanceamentoAnalyzer;
    private ProjecaoFinanceiraService projecaoFinanceiraService;
    private EstoqueInteligenteService estoqueInteligenteService;
    private CustoBeneficioAnalyzer custoBeneficioAnalyzer;

    // ===== SERVICES INTEGRADOS =====
    private GIAService giaService;
    private GIARelatorioService relatorioService;
    private ManutencaoPreventivaService manutencaoService;
    private GIARecommendationGenerator recommendationGenerator;

    // =========================================================================
    // CICLO DE VIDA
    // =========================================================================

    @FXML
    public void initialize() {
        logger.info("Inicializando GIA Controller v1.4 — Tabelas com Ações");

        // Inicializar Services de inteligência
        criticidadeCalculator = new CriticidadeCalculator(historicoService);
        balanceamentoAnalyzer = new BalanceamentoAnalyzer();
        projecaoFinanceiraService = new ProjecaoFinanceiraService();
        estoqueInteligenteService = new EstoqueInteligenteService();
        custoBeneficioAnalyzer = new CustoBeneficioAnalyzer();

        // Inicializar Services integrados
        giaService = new GIAService(criticidadeCalculator, balanceamentoAnalyzer,
                projecaoFinanceiraService, estoqueInteligenteService, custoBeneficioAnalyzer);
        recommendationGenerator = new GIARecommendationGenerator(criticidadeCalculator,
                balanceamentoAnalyzer, estoqueInteligenteService);
        relatorioService = new GIARelatorioService(giaService, recommendationGenerator, criticidadeCalculator);
        manutencaoService = new ManutencaoPreventivaService(historicoService, criticidadeCalculator);

        // Inicializar subcomponentes
        dataLoader = new GIADataLoader(inventarioService, catalogoService, funcionariosService,
                movimentacoesService, tonersService, historicoService);
        gaugesBuilder = new GIAGaugesBuilder(gaugeSaudeGeral, gaugeValorTotal, lblEmManutencao,
                gaugeCriticos, gaugePrevisaoSubstituicao, barSaudeGeral, labelSaudeDetalhe, labelValorDetalhe,
                labelManutencaoDetalhe, labelCriticosDetalhe, labelPrevisaoDetalhe);
        chartBuilder = new GIAChartBuilder(pieChartStatus, barChartDepartamento, barChartBalanceamento,
                graficoCondicao, lineChartProjecao);
        tableBuilder = new GIATableBuilder(tblInventario, tblLocalizacao, tabelaCriticos, tabelaRecomendacoes,
                tabelaCustoBeneficio, tabelaEstoqueAlertas, tabelaItensParados, lblTotalItens);

        // Injetar Services no TableBuilder
        tableBuilder.setServices(criticidadeCalculator, balanceamentoAnalyzer,
                projecaoFinanceiraService, estoqueInteligenteService, custoBeneficioAnalyzer);

        // ✅ Configurar colunas da tabela de inventário
        configurarColunasInventario();
        configurarColunaAcoesInventario();

        carregarCombosFiltros();
        carregarAnalises();
    }

    // =========================================================================
    // CONFIGURAÇÃO DAS COLUNAS DA TABELA DE INVENTÁRIO
    // =========================================================================

    @SuppressWarnings("unchecked")
    private void configurarColunasInventario() {
        TableColumn<InventarioEquipamentos, String> colSKU =
                (TableColumn<InventarioEquipamentos, String>) tblInventario.getColumns().get(0);
        colSKU.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getSkuProduto()));

        TableColumn<InventarioEquipamentos, String> colTipo =
                (TableColumn<InventarioEquipamentos, String>) tblInventario.getColumns().get(1);
        colTipo.setCellValueFactory(cellData -> {
            String sku = cellData.getValue().getSkuProduto();
            String tipo = dataLoader.getCatalogos().stream()
                    .filter(c -> c.getSku().equals(sku))
                    .findFirst()
                    .map(c -> c.getTipoProduto() != null ? c.getTipoProduto().toString() : "-")
                    .orElse("-");
            return new SimpleStringProperty(tipo);
        });

        TableColumn<InventarioEquipamentos, String> colMarca =
                (TableColumn<InventarioEquipamentos, String>) tblInventario.getColumns().get(2);
        colMarca.setCellValueFactory(cellData -> {
            String sku = cellData.getValue().getSkuProduto();
            String marca = dataLoader.getCatalogos().stream()
                    .filter(c -> c.getSku().equals(sku))
                    .findFirst()
                    .map(c -> c.getMarca() + " " + c.getModelo())
                    .orElse("-");
            return new SimpleStringProperty(marca);
        });

        TableColumn<InventarioEquipamentos, String> colSerie =
                (TableColumn<InventarioEquipamentos, String>) tblInventario.getColumns().get(3);
        colSerie.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getNumSerie() != null ? cellData.getValue().getNumSerie() : "-"));

        TableColumn<InventarioEquipamentos, String> colLoc =
                (TableColumn<InventarioEquipamentos, String>) tblInventario.getColumns().get(4);
        colLoc.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getLocalizacao() != null ? cellData.getValue().getLocalizacao() : "-"));

        TableColumn<InventarioEquipamentos, String> colStatus =
                (TableColumn<InventarioEquipamentos, String>) tblInventario.getColumns().get(5);
        colStatus.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStatus() != null ? cellData.getValue().getStatus() : "-"));

        TableColumn<InventarioEquipamentos, String> colCond =
                (TableColumn<InventarioEquipamentos, String>) tblInventario.getColumns().get(6);
        colCond.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCondicao() != null ? cellData.getValue().getCondicao() : "-"));

        TableColumn<InventarioEquipamentos, String> colData =
                (TableColumn<InventarioEquipamentos, String>) tblInventario.getColumns().get(7);
        colData.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDataAquisicao() != null ?
                        cellData.getValue().getDataAquisicao().toString() : "-"));
    }

    @SuppressWarnings("unchecked")
    private void configurarColunaAcoesInventario() {
        TableColumn<InventarioEquipamentos, Void> colAcoes =
                (TableColumn<InventarioEquipamentos, Void>) tblInventario.getColumns().get(8);

        colAcoes.setCellFactory(param -> new TableCell<>() {
            private final Button btnDetalhesEquip = new Button("🔍 Equip.");
            private final Button btnDetalhesProd = new Button("📦 Prod.");
            private final HBox pane = new HBox(5, btnDetalhesEquip, btnDetalhesProd);

            {
                btnDetalhesEquip.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 11px;");
                btnDetalhesProd.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 11px;");

                btnDetalhesEquip.setTooltip(new Tooltip("Ver detalhes do equipamento no inventário"));
                btnDetalhesProd.setTooltip(new Tooltip("Ver detalhes do produto no catálogo"));

                btnDetalhesEquip.setOnAction(e -> {
                    InventarioEquipamentos eq = getTableView().getItems().get(getIndex());
                    abrirDetalhesEquipamento(eq);
                });

                btnDetalhesProd.setOnAction(e -> {
                    InventarioEquipamentos eq = getTableView().getItems().get(getIndex());
                    abrirDetalhesProduto(eq);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void abrirDetalhesEquipamento(InventarioEquipamentos eq) {
        try {
            Window owner = tblInventario.getScene().getWindow();
            NexusFX.dialogs().openModalWithController(
                    "inventarioDetails",
                    "Detalhes do Equipamento",
                    owner,
                    (Object controller) -> {
                        if (controller instanceof InventarioControllerDetails details) {
                            details.carregarEquipamento(eq.getId());
                        }
                    }
            );
        } catch (Exception e) {
            logger.error("Erro ao abrir detalhes do equipamento", e);
            NexusFX.alerts().erro("Erro", "Não foi possível abrir os detalhes do equipamento.", e.getMessage());
        }
    }

    private void abrirDetalhesProduto(InventarioEquipamentos eq) {
        try {
            Window owner = tblInventario.getScene().getWindow();
            NexusFX.dialogs().openModalWithController(
                    "detalhe_produto",
                    "Detalhes do Produto",
                    owner,
                    (Object controller) -> {
                        if (controller instanceof CatalogoProdutoDetailController details) {
                            details.setProdutoSku(eq.getSkuProduto());
                        }
                    }
            );
        } catch (Exception e) {
            logger.error("Erro ao abrir detalhes do produto", e);
            NexusFX.alerts().erro("Erro", "Não foi possível abrir os detalhes do produto.", e.getMessage());
        }
    }

    // =========================================================================
    // COMBOS DE FILTROS
    // =========================================================================

    private void carregarCombosFiltros() {
        cbTipoProduto.getItems().addAll("TODOS", "EQUIPAMENTO", "CONSUMIVEL", "TONER", "ACESSORIO", "SOFTWARE");
        cbTipoProduto.getSelectionModel().selectFirst();
        cbStatus.getItems().addAll("TODOS", "ATIVO", "MANUTENCAO", "BAIXADO", "RESERVA", "EM_USO");
        cbStatus.getSelectionModel().selectFirst();
        cbCondicao.getItems().addAll("TODOS", "OTIMO", "BOM", "REGULAR", "CRITICO");
        cbCondicao.getSelectionModel().selectFirst();
    }

    // =========================================================================
    // CARREGAMENTO DE ANÁLISES
    // =========================================================================

    private void carregarAnalises() {
        mostrarLoading(true);
        statusLabel.setText("Analisando dados...");

        new Thread(() -> {
            try {
                dataLoader.carregarTudo();

                List<InventarioEquipamentos> equipamentos = dataLoader.getEquipamentos();
                List<CatalogoProdutos> catalogos = dataLoader.getCatalogos();
                List<Funcionarios> funcionarios = dataLoader.getFuncionarios();

                Platform.runLater(() -> {
                    cbDepartamento.getItems().clear();
                    cbDepartamento.getItems().add("TODOS");
                    cbDepartamento.getItems().addAll(dataLoader.getInventarioService().listarDepartamentosUnicos());
                    cbDepartamento.getSelectionModel().selectFirst();

                    cbLocalizacao.getItems().clear();
                    cbLocalizacao.getItems().add("TODOS");
                    cbLocalizacao.getItems().addAll(dataLoader.getInventarioService().listarLocalizacoesUnicas());
                    cbLocalizacao.getSelectionModel().selectFirst();

                    gaugesBuilder.construir(equipamentos, catalogos);
                    chartBuilder.construirTodos(equipamentos, funcionarios);
                    tableBuilder.construirTodas(equipamentos, catalogos, funcionarios);

                    int criticos = (int) equipamentos.stream()
                            .filter(e -> "CRITICO".equals(e.getCondicao())).count();
                    Map<String, String> saude = giaService.avaliarSaudeInventario(equipamentos, catalogos);
                    String avaliacaoGeral = saude.getOrDefault("Avaliação Geral", "N/A");

                    labelUltimaAtualizacao.setText("Última análise: " +
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                    statusLabel.setText(avaliacaoGeral + " | " + criticos + " críticos | " +
                            equipamentos.size() + " equipamentos");
                    mostrarLoading(false);
                });
            } catch (Exception e) {
                logger.error("Erro ao carregar análises", e);
                Platform.runLater(() -> {
                    mostrarLoading(false);
                    statusLabel.setText("Erro na análise: " + e.getMessage());
                });
            }
        }).start();
    }

    // =========================================================================
    // HANDLERS
    // =========================================================================

    @FXML private void handleAtualizar() { carregarAnalises(); }

    @FXML
    private void handleAplicarFiltros() {
        String status = cbStatus.getValue();
        String condicao = cbCondicao.getValue();
        String departamento = cbDepartamento.getValue();
        String localizacao = cbLocalizacao.getValue();

        List<InventarioEquipamentos> filtrados = dataLoader.getEquipamentos().stream()
                .filter(e -> "TODOS".equals(status) || status.equals(e.getStatus()))
                .filter(e -> "TODOS".equals(condicao) || condicao.equals(e.getCondicao()))
                .filter(e -> "TODOS".equals(departamento) || departamento.equals(e.getDepartamento()))
                .filter(e -> "TODOS".equals(localizacao) || localizacao.equals(e.getLocalizacao()))
                .collect(Collectors.toList());

        tblInventario.getItems().clear();
        tblInventario.getItems().addAll(filtrados);
        lblTotalItens.setText(String.valueOf(filtrados.size()));
        statusLabel.setText("Filtros aplicados — " + filtrados.size() + " resultados");
    }

    @FXML
    private void handleLimparFiltros() {
        cbTipoProduto.getSelectionModel().selectFirst();
        cbStatus.getSelectionModel().selectFirst();
        cbCondicao.getSelectionModel().selectFirst();
        cbDepartamento.getSelectionModel().selectFirst();
        cbLocalizacao.getSelectionModel().selectFirst();
        ckApenasDefeito.setSelected(false);

        tblInventario.getItems().clear();
        tblInventario.getItems().addAll(dataLoader.getEquipamentos());
        lblTotalItens.setText(String.valueOf(dataLoader.getEquipamentos().size()));
        statusLabel.setText("Filtros limpos");
    }

    @FXML
    private void handleExportarRelatorio() {
        try {
            String relatorio = relatorioService.gerarRelatorioCompleto(
                    dataLoader.getEquipamentos(), dataLoader.getCatalogos(), dataLoader.getFuncionarios());
            Stage stage = (Stage) tblInventario.getScene().getWindow();
            relatorioService.exibirRelatorioUI(relatorio, stage);
        } catch (Exception e) {
            logger.error("Erro ao gerar relatório", e);
            NexusFX.alerts().erro("Erro no Relatório", "Não foi possível gerar o relatório completo.", e.getMessage());
        }
    }

    @FXML
    private void handleDetalhesItem() {
        InventarioEquipamentos selecionado = tblInventario.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            NexusFX.alerts().warn("Seleção Necessária", "Selecione um item na tabela de inventário.", "GIA");
            return;
        }

        CriticidadeCalculator.CriticidadeResult criticidade = criticidadeCalculator.calcular(selecionado, "GERAL");

        String infoCatalogo = dataLoader.getCatalogos().stream()
                .filter(c -> c.getSku().equals(selecionado.getSkuProduto()))
                .findFirst()
                .map(c -> String.format("\n\n📦 Catálogo:\nMarca: %s\nModelo: %s\nPreço: € %s\nEstoque: %d",
                        c.getMarca(), c.getModelo(),
                        c.getPrecoUnitario() != null ? c.getPrecoUnitario() : "N/D",
                        c.getTotalRecebido() != null ? c.getTotalRecebido() : 0))
                .orElse("");

        String infoFuncionario = dataLoader.getFuncionarios().stream()
                .filter(f -> f.getCodDep().equals(selecionado.getFuncionarioId()))
                .findFirst()
                .map(f -> String.format("\n\n👤 Responsável:\nNome: %s\nDepartamento: %s\nEmail: %s",
                        f.getNome(), f.getDepartamento(), f.getEmail() != null ? f.getEmail() : "N/D"))
                .orElse("");

        NexusFX.alerts().info("🔍 Detalhes do Equipamento",
                String.format("SKU: %s\nNº Série: %s\nMAC: %s\nStatus: %s\nCondição: %s\nLocalização: %s\nData Aquisição: %s\n\n" +
                                "📊 Análise de Criticidade:\nScore: %s/100\nClassificação: %s\nVida Útil Restante: %s\n\n💡 Recomendação:\n%s%s%s",
                        selecionado.getSkuProduto(),
                        selecionado.getNumSerie() != null ? selecionado.getNumSerie() : "N/A",
                        selecionado.getEnderecoMac() != null ? selecionado.getEnderecoMac() : "N/A",
                        selecionado.getStatus() != null ? selecionado.getStatus() : "N/A",
                        selecionado.getCondicao() != null ? selecionado.getCondicao() : "N/A",
                        selecionado.getLocalizacao() != null ? selecionado.getLocalizacao() : "N/A",
                        selecionado.getDataAquisicao() != null ? selecionado.getDataAquisicao().toString() : "N/A",
                        criticidade.getScoreFormatado(), criticidade.getClassificacao(),
                        criticidade.getVidaUtilFormatada(), criticidade.getRecomendacao(),
                        infoCatalogo, infoFuncionario), "GIA — Detalhes do Equipamento");
    }

    @FXML
    private void handleRelatorioDefeitos() {
        try {
            List<InventarioEquipamentos> equipamentos = dataLoader.getEquipamentos();
            List<ManutencaoPreventivaService.AlertaManutencao> alertas =
                    manutencaoService.getAlertasUrgentes(equipamentos, dataLoader.getCatalogos());

            long criticos = equipamentos.stream().filter(e -> "CRITICO".equals(e.getCondicao())).count();
            long regulares = equipamentos.stream().filter(e -> "REGULAR".equals(e.getCondicao())).count();
            long manutencao = equipamentos.stream().filter(e -> "MANUTENCAO".equals(e.getStatus())).count();

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("📊 Resumo de Equipamentos com Problemas:\n\n"));
            sb.append(String.format("🔴 Críticos: %d\n🟡 Regulares: %d\n🔧 Em Manutenção: %d\n", criticos, regulares, manutencao));

            if (!alertas.isEmpty()) {
                sb.append(String.format("\n🚨 Alertas Urgentes (%d):\n", alertas.size()));
                alertas.forEach(a -> sb.append("\n").append(a.toString()));
            }

            sb.append("\n\nConsulte a tabela 'Top 10 Críticos' e 'Recomendações' para mais detalhes.");
            NexusFX.alerts().info("Relatório de Defeitos", sb.toString(), "GIA");
        } catch (Exception e) {
            logger.error("Erro ao gerar relatório de defeitos", e);
            NexusFX.alerts().erro("Erro", "Não foi possível gerar o relatório de defeitos.", e.getMessage());
        }
    }

    @FXML
    private void handleGestaoToners() {
        NexusFX.alerts().info("Gestão de Toners", "Acesse o módulo de Toners para gestão completa.", "GIA");
    }

    // =========================================================================
    // UTILITÁRIOS
    // =========================================================================

    private void mostrarLoading(boolean mostrar) {
        if (progressIndicator != null) progressIndicator.setVisible(mostrar);
    }

    // =========================================================================
    // GETTERS
    // =========================================================================

    public GIADataLoader getDataLoader() { return dataLoader; }
    public GIAService getGiaService() { return giaService; }
    public GIARelatorioService getRelatorioService() { return relatorioService; }
    public ManutencaoPreventivaService getManutencaoService() { return manutencaoService; }
    public GIARecommendationGenerator getRecommendationGenerator() { return recommendationGenerator; }
    public CriticidadeCalculator getCriticidadeCalculator() { return criticidadeCalculator; }
    public BalanceamentoAnalyzer getBalanceamentoAnalyzer() { return balanceamentoAnalyzer; }
    public ProjecaoFinanceiraService getProjecaoFinanceiraService() { return projecaoFinanceiraService; }
    public EstoqueInteligenteService getEstoqueInteligenteService() { return estoqueInteligenteService; }
    public CustoBeneficioAnalyzer getCustoBeneficioAnalyzer() { return custoBeneficioAnalyzer; }
}