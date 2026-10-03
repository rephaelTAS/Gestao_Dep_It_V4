package com.ossobo.gestaoDepIt.main;

import com.ossobo.gestaoDepIt.config.AppImageConfig;
import com.ossobo.gestaoDepIt.config.ViewConstant;
import com.ossobo.gestaoDepIt.db.enums.TipoProduto;
import com.ossobo.gestaoDepIt.db.models.*;
import com.ossobo.gestaoDepIt.db.services.*;
import com.ossobo.winterfx.anotations.Controller;
import com.ossobo.winterfx.anotations.Inject;
import com.ossobo.winterfx.anotations.PostConstruct;
import com.ossobo.winterfx.imagemanager.anotations.SwapImage;
import com.ossobo.winterfx.view.anotations.RegisterView;
import com.ossobo.winterfx.view.anotations.SwapFxml;
import com.ossobo.winterfx.view.controller.WinterFXController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 🎯 DashboardController - Painel de Controle Empresarial com WinterFX
 *
 * ✅ Gerenciado pelo WinterFX (@Controller)
 * ✅ Registro automático via @RegisterView
 * ✅ Injeção de dependências com @Inject
 * ✅ Estatísticas e KPIs em tempo real
 * ✅ Gráficos interativos
 *
 * @version 4.0 (WinterFX)
 * @since 2026-08-19
 */
@Controller(proxy = false)
@RegisterView(
        id = ViewConstant.Main.DASHBOARD,
        fxml = "/META-INF/gestaoDepIt/fxmls/dashboard/dashboard.fxml",
        title = "Dashboard - Gestão de TI",
        width = 1200,
        height = 800,
        centered = true,
        primaryCss = "/META-INF/gestaoDepIt/css/dashboard/dashboard.css"
)
public class DashboardController implements Initializable, WinterFXController {
    private static final Logger LOGGER = System.getLogger(DashboardController.class.getName());

    private static final int PAGINA_INICIAL = 1;
    private static final int TAMANHO_PAGINA_GRANDE = 1000;
    private static final DateTimeFormatter MES_FORMATTER = DateTimeFormatter.ofPattern("MMM yyyy");

    // ============================================================
    // DEPENDÊNCIAS INJETADAS
    // ============================================================

    @Inject private CatalogoProdutosService catalogoService;
    @Inject private EstoqueMovimentacoesService estoqueService;
    @Inject private InventarioEquipamentosService inventarioService;
    @Inject private FuncionariosService funcionariosService;
    @Inject private GestaoTonersService tonersService;
    @Inject private HistoricoEventosService historicoService;

    // ============================================================
    // FXML COMPONENTS - CONTAINER
    // ============================================================

    @FXML private VBox dashboardContainer;

    // ============================================================
    // FXML COMPONENTS - KPIs PRINCIPAIS
    // ============================================================

    @FXML private Label lblTotalProdutos;
    @FXML private Label lblTotalEquipamentos;
    @FXML private Label lblTotalFuncionarios;
    @FXML private Label lblValorTotalEstoque;

    // ============================================================
    // FXML COMPONENTS - ALERTAS CRÍTICOS
    // ============================================================

    @FXML private Label lblProdutoMenorStock;
    @FXML private Label lblEquipamentosManutencao;
    @FXML private Label lblTonersBaixos;
    @FXML private Label lblMovimentacoesMes;
    @FXML private Label lblDepartamentoMaiorEquipamentos;
    @FXML private ProgressBar progressStockSeguranca;

    // ============================================================
    // FXML COMPONENTS - GRÁFICOS
    // ============================================================

    @FXML private BarChart<String, Number> chartMovimentacoes;
    @FXML private PieChart chartDistribuicaoTipos;
    @FXML private LineChart<String, Number> chartTrendEstoque;
    @FXML private BarChart<String, Number> chartEquipamentosDepartamento;

    // ============================================================
    // DADOS CACHE
    // ============================================================

    private List<CatalogoProdutos> catalogoProdutos = new ArrayList<>();
    private List<EstoqueMovimentacoes> movimentacoes = new ArrayList<>();
    private List<InventarioEquipamentos> inventario = new ArrayList<>();
    private List<Funcionarios> funcionarios = new ArrayList<>();
    private List<GestaoToners> toners = new ArrayList<>();

    // ============================================================
    // CICLO DE VIDA - @PostConstruct
    // ============================================================

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            LOGGER.log(Level.INFO, "📊 DashboardController inicializando com WinterFX...");

            carregarDados();
            configurarEstilos();
            atualizarMetricas();
            criarVisualizacoes();

            LOGGER.log(Level.INFO, "✅ DashboardController inicializado com sucesso");

        } catch (Exception e) {
            LOGGER.log(Level.ERROR, "❌ Erro ao inicializar dashboard", e);
        }
    }

    // ============================================================
    // CARREGAMENTO DE DADOS
    // ============================================================

    private void carregarDados() {
        try {
            LOGGER.log(Level.DEBUG, "📊 Carregando dados do dashboard...");

            catalogoProdutos = catalogoService.listarTodos(PAGINA_INICIAL, TAMANHO_PAGINA_GRANDE);
            movimentacoes = carregarMovimentacoesDashboard();
            inventario = inventarioService.listarTodos(PAGINA_INICIAL, TAMANHO_PAGINA_GRANDE);
            funcionarios = funcionariosService.listarAtivos(PAGINA_INICIAL, TAMANHO_PAGINA_GRANDE);
            toners = tonersService.listarTodos(PAGINA_INICIAL, TAMANHO_PAGINA_GRANDE);

            LOGGER.log(Level.INFO, "📊 Dados carregados: {0} produtos, {1} equipamentos, {2} funcionários, {3} toners",
                    catalogoProdutos.size(), inventario.size(), funcionarios.size(), toners.size());

        } catch (Exception e) {
            LOGGER.log(Level.ERROR, "❌ Erro ao carregar dados do dashboard", e);
            // Inicializa listas vazias para evitar NullPointer
            catalogoProdutos = new ArrayList<>();
            movimentacoes = new ArrayList<>();
            inventario = new ArrayList<>();
            funcionarios = new ArrayList<>();
            toners = new ArrayList<>();
        }
    }

    private List<EstoqueMovimentacoes> carregarMovimentacoesDashboard() {
        try {
            LocalDate seisMesesAtras = LocalDate.now().minusMonths(6);
            return estoqueService.buscarPorPeriodo(seisMesesAtras, LocalDate.now());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "⚠️ Não foi possível carregar movimentações, usando lista vazia");
            return new ArrayList<>();
        }
    }

    // ============================================================
    // ATUALIZAÇÃO DE MÉTRICAS
    // ============================================================

    private void atualizarMetricas() {
        atualizarKpisPrincipais();
        atualizarAlertasCriticos();
        atualizarMetricasAvancadas();
    }

    // ----- KPIs PRINCIPAIS -----

    private void atualizarKpisPrincipais() {
        try {
            // Total de Produtos
            lblTotalProdutos.setText(String.valueOf(catalogoProdutos.size()));

            // Equipamentos Ativos (ATIVO ou EM_USO)
            long equipamentosAtivos = inventario.stream()
                    .filter(e -> e.status() != null &&
                            ("ATIVO".equals(e.status()) || "EM_USO".equals(e.status())))
                    .count();
            lblTotalEquipamentos.setText(String.valueOf(equipamentosAtivos));

            // Total de Funcionários Ativos
            lblTotalFuncionarios.setText(String.valueOf(funcionarios.size()));

            // Valor Total do Estoque
            double valorTotal = catalogoProdutos.stream()
                    .mapToDouble(p -> {
                        double preco = p.precoUnitarioCentavos() != null ?
                                p.precoUnitarioCentavos().doubleValue() : 0;
                        int quantidade = calcularStockAtual(p.sku());
                        return preco * quantidade;
                    })
                    .sum();
            lblValorTotalEstoque.setText(String.format("R$ %,.2f", valorTotal));

        } catch (Exception e) {
            LOGGER.log(Level.ERROR, "❌ Erro ao atualizar KPIs principais", e);
        }
    }

    // ----- ALERTAS CRÍTICOS -----

    private void atualizarAlertasCriticos() {
        try {
            // Produto com menor stock
            Optional<CatalogoProdutos> produtoMenorStock = catalogoProdutos.stream()
                    .filter(p -> calcularStockAtual(p.sku()) > 0)
                    .min(Comparator.comparingInt(p -> calcularStockAtual(p.sku())));

            if (produtoMenorStock.isPresent()) {
                CatalogoProdutos p = produtoMenorStock.get();
                int stockAtual = calcularStockAtual(p.sku());
                lblProdutoMenorStock.setText(p.modelo() + " (" + stockAtual + " unidades)");
            } else {
                lblProdutoMenorStock.setText("Nenhum produto com estoque");
            }

            // Equipamentos em manutenção
            long equipamentosManutencao = inventario.stream()
                    .filter(e -> "MANUTENCAO".equals(e.status()))
                    .count();
            lblEquipamentosManutencao.setText(String.valueOf(equipamentosManutencao));

            // Toners com percentagem crítica (< 20%)
            long tonersBaixos = toners.stream()
                    .filter(t -> t.percentagemRestante() != null && t.percentagemRestante() < 20)
                    .count();
            lblTonersBaixos.setText(String.valueOf(tonersBaixos));

            // Movimentações no mês atual
            long movimentacoesMes = movimentacoes.stream()
                    .filter(m -> m.dataMovimentacao() != null &&
                            m.dataMovimentacao().getMonth() == LocalDate.now().getMonth())
                    .count();
            lblMovimentacoesMes.setText(String.valueOf(movimentacoesMes));

        } catch (Exception e) {
            LOGGER.log(Level.ERROR, "❌ Erro ao atualizar alertas críticos", e);
        }
    }

    // ----- MÉTRICAS AVANÇADAS -----

    private void atualizarMetricasAvancadas() {
        try {
            // Departamento com mais equipamentos
            Map<String, Long> equipamentosPorDepartamento = inventario.stream()
                    .filter(e -> e.departamento() != null)
                    .collect(Collectors.groupingBy(
                            InventarioEquipamentos::departamento,
                            Collectors.counting()));

            Optional<Map.Entry<String, Long>> departamentoMaior = equipamentosPorDepartamento
                    .entrySet().stream()
                    .max(Map.Entry.comparingByValue());

            if (departamentoMaior.isPresent()) {
                Map.Entry<String, Long> dep = departamentoMaior.get();
                lblDepartamentoMaiorEquipamentos.setText(
                        dep.getKey() + " (" + dep.getValue() + " equipamentos)");
            } else {
                lblDepartamentoMaiorEquipamentos.setText("Nenhum departamento");
            }

            // Progresso de stock de segurança
            long produtosComStock = catalogoProdutos.stream()
                    .filter(p -> calcularStockAtual(p.sku()) > 0)
                    .count();

            double percentagemStock = catalogoProdutos.isEmpty() ? 0 :
                    (double) produtosComStock / catalogoProdutos.size();
            progressStockSeguranca.setProgress(percentagemStock);

        } catch (Exception e) {
            LOGGER.log(Level.ERROR, "❌ Erro ao atualizar métricas avançadas", e);
        }
    }

    // ============================================================
    // VISUALIZAÇÕES (GRÁFICOS)
    // ============================================================

    private void criarVisualizacoes() {
        try {
            criarGraficoMovimentacoes();
            criarGraficoDistribuicaoTipos();
            criarGraficoTrendEstoque();
            criarGraficoEquipamentosDepartamento();
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, "❌ Erro ao criar visualizações", e);
        }
    }

    /**
     * Gráfico de Movimentações por Tipo
     */
    private void criarGraficoMovimentacoes() {
        try {
            chartMovimentacoes.setTitle("Movimentações de Estoque por Tipo");
            chartMovimentacoes.setLegendVisible(false);
            chartMovimentacoes.getData().clear();

            Map<String, Long> movPorTipo = movimentacoes.stream()
                    .filter(m -> m.tipoMovimentacao() != null)
                    .collect(Collectors.groupingBy(
                            m -> m.tipoMovimentacao().getDescricao(),
                            Collectors.counting()));

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Movimentações");

            movPorTipo.forEach((tipo, qtd) ->
                    series.getData().add(new XYChart.Data<>(tipo, qtd)));

            chartMovimentacoes.getData().add(series);

        } catch (Exception e) {
            LOGGER.log(Level.ERROR, "❌ Erro ao criar gráfico de movimentações", e);
        }
    }

    /**
     * Gráfico de Distribuição por Tipo de Produto
     */
    private void criarGraficoDistribuicaoTipos() {
        try {
            chartDistribuicaoTipos.setTitle("Distribuição por Tipo de Produto");
            chartDistribuicaoTipos.getData().clear();

            Map<TipoProduto, Long> produtosPorTipo = catalogoProdutos.stream()
                    .filter(p -> p.tipoProduto() != null)
                    .collect(Collectors.groupingBy(
                            CatalogoProdutos::tipoProduto,
                            Collectors.counting()));

            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

            produtosPorTipo.forEach((tipo, qtd) ->
                    pieData.add(new PieChart.Data(
                            tipo.getDescricao() + " (" + qtd + ")",
                            qtd)));

            chartDistribuicaoTipos.setData(pieData);

        } catch (Exception e) {
            LOGGER.log(Level.ERROR, "❌ Erro ao criar gráfico de distribuição", e);
        }
    }

    /**
     * Gráfico de Tendência de Movimentações (Últimos 6 Meses)
     */
    private void criarGraficoTrendEstoque() {
        try {
            chartTrendEstoque.setTitle("Tendência de Movimentações - Últimos 6 Meses");
            chartTrendEstoque.getData().clear();

            Map<String, Long> movPorMes = movimentacoes.stream()
                    .filter(m -> m.dataMovimentacao() != null)
                    .collect(Collectors.groupingBy(
                            m -> m.dataMovimentacao().format(MES_FORMATTER),
                            Collectors.counting()));

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Movimentações");

            movPorMes.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(e -> series.getData().add(
                            new XYChart.Data<>(e.getKey(), e.getValue())));

            chartTrendEstoque.getData().add(series);

        } catch (Exception e) {
            LOGGER.log(Level.ERROR, "❌ Erro ao criar gráfico de tendência", e);
        }
    }

    /**
     * Gráfico de Equipamentos por Departamento
     */
    private void criarGraficoEquipamentosDepartamento() {
        try {
            chartEquipamentosDepartamento.setTitle("Equipamentos por Departamento");
            chartEquipamentosDepartamento.getData().clear();

            Map<String, Long> eqPorDep = inventario.stream()
                    .filter(e -> e.departamento() != null && !e.departamento().isBlank())
                    .collect(Collectors.groupingBy(
                            InventarioEquipamentos::departamento,
                            Collectors.counting()));

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Equipamentos");

            eqPorDep.forEach((dep, qtd) ->
                    series.getData().add(new XYChart.Data<>(dep, qtd)));

            chartEquipamentosDepartamento.getData().add(series);

        } catch (Exception e) {
            LOGGER.log(Level.ERROR, "❌ Erro ao criar gráfico de departamentos", e);
        }
    }

    // ============================================================
    // CÁLCULO DE STOCK (USANDO SKU)
    // ============================================================

    /**
     * Calcula o estoque atual para um produto via SKU
     */
    private int calcularStockAtual(String sku) {
        try {
            if (sku == null || sku.isBlank()) return 0;

            int entradas = movimentacoes.stream()
                    .filter(m -> sku.equals(m.skuProduto()))
                    .filter(m -> m.isEntrada())
                    .mapToInt(EstoqueMovimentacoes::quantidade)
                    .sum();

            int saidas = movimentacoes.stream()
                    .filter(m -> sku.equals(m.skuProduto()))
                    .filter(m -> m.isSaida() || m.isReserva())
                    .mapToInt(EstoqueMovimentacoes::quantidade)
                    .sum();

            return Math.max(0, entradas - saidas);

        } catch (Exception e) {
            LOGGER.log(Level.DEBUG, "Erro ao calcular stock para SKU {0}: {1}", sku, e.getMessage());
            return 0;
        }
    }

    // ============================================================
    // ESTILOS
    // ============================================================

    private void configurarEstilos() {
        try {
            String cardStyle = """
                -fx-background-color: rgba(255, 255, 255, 0.95);
                -fx-background-radius: 12px;
                -fx-border-color: rgba(99, 102, 241, 0.15);
                -fx-border-width: 1px;
                -fx-border-radius: 12px;
                -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.08), 10, 0, 0, 2);
                -fx-padding: 20px;
            """;

            dashboardContainer.getChildren().forEach(node -> {
                if (node instanceof Pane) {
                    ((Pane) node).setStyle(cardStyle);
                }
            });

        } catch (Exception e) {
            LOGGER.log(Level.ERROR, "❌ Erro ao configurar estilos", e);
        }
    }

    // ============================================================
    // HANDLERS DE NAVEGAÇÃO
    // ============================================================

    @FXML
    @SwapFxml(
            container = "multiploPanel",
            viewId = ViewConstant.Catalogo.LIST,
            before = false
    )
    @SwapImage(
            imageView = "iconepage",
            imageId = AppImageConfig.System.IN_STOCK
    )
    public void handleVerProdutos(ActionEvent event) {
        LOGGER.log(Level.INFO, "📦 Navegando para Catálogo de Produtos");
    }

    @FXML
    @SwapFxml(
            container = "multiploPanel",
            viewId = ViewConstant.Inventario.LIST,
            before = false
    )
    @SwapImage(
            imageView = "iconepage",
            imageId = AppImageConfig.System.LIST
    )
    public void handleVerEquipamentos(ActionEvent event) {
        LOGGER.log(Level.INFO, "🔧 Navegando para Inventário de Equipamentos");
    }

    @FXML
    @SwapFxml(
            container = "multiploPanel",
            viewId = ViewConstant.Funcionario.LIST,
            before = false
    )
    @SwapImage(
            imageView = "iconepage",
            imageId = AppImageConfig.System.USER
    )
    public void handleVerFuncionarios(ActionEvent event) {
        LOGGER.log(Level.INFO, "👤 Navegando para Gestão de Funcionários");
    }

    @FXML
    @SwapFxml(
            container = "multiploPanel",
            viewId = ViewConstant.Toner.LIST,
            before = false
    )
    @SwapImage(
            imageView = "iconepage",
            imageId = AppImageConfig.System.BALLOT
    )
    public void handleVerToners(ActionEvent event) {
        LOGGER.log(Level.INFO, "🖨️ Navegando para Gestão de Toners");
    }

    @FXML
    @SwapFxml(
            container = "multiploPanel",
            viewId = ViewConstant.Estoque.MOVIMENTACOES_LIST,
            before = false
    )
    @SwapImage(
            imageView = "iconepage",
            imageId = AppImageConfig.System.IN_STOCK
    )
    public void handleVerMovimentacoes(ActionEvent event) {
        LOGGER.log(Level.INFO, "📋 Navegando para Movimentações de Estoque");
    }

    /**
     * Atualiza o dashboard manualmente
     */
    @FXML
    public void handleRefresh(ActionEvent event) {
        LOGGER.log(Level.INFO, "🔄 Atualização manual do dashboard solicitada");
        carregarDados();
        atualizarMetricas();
        criarVisualizacoes();
    }

    // ============================================================
    // API PÚBLICA
    // ============================================================

    /**
     * Atualiza todos os dados do dashboard
     */
    public void atualizarDados() {
        try {
            carregarDados();
            atualizarMetricas();
            criarVisualizacoes();
            LOGGER.log(Level.INFO, "🔄 Dashboard atualizado");
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, "❌ Erro ao atualizar dados", e);
        }
    }

    /**
     * Filtra o dashboard por período
     */
    public void filtrarPorPeriodo(LocalDate inicio, LocalDate fim) {
        try {
            movimentacoes = estoqueService.buscarPorPeriodo(inicio, fim);
            atualizarMetricas();
            criarVisualizacoes();
            LOGGER.log(Level.INFO, "📅 Dashboard filtrado: {0} a {1}", inicio, fim);
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, "❌ Erro ao filtrar dashboard", e);
        }
    }
}