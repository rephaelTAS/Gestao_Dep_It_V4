/*
 * RelatorioDashboardController v2.0
 *
 * Controller do Dashboard de Relatórios — versão profissional.
 *
 * Funcionalidades:
 * - KPIs de relatórios gerados (hoje, semana, modelos salvos)
 * - Gráfico de visão geral (quantos relatórios de cada tipo)
 * - Tabela de tabelas mais usadas em relatórios
 * - Modelos salvos (ListView reutilizável)
 * - Atividade recente (apenas eventos de relatório)
 * - Exportações rápidas (4 tipos)
 * - Histórico de relatórios gerados (JSON em memória)
 *
 * v2.0: Alinhado com o novo FXML profissional
 * v1.1: Correções de compilação
 */
package com.ossobo.gestaoDepIt.controllers.gestao.relatorios;

import com.ossobo.gestaoDepIt.db.models.*;
import com.ossobo.gestaoDepIt.db.services.*;
import com.ossobo.nexusfx.NexusFX;
import com.ossobo.nexusfx.di.annotations.Controller;
import com.ossobo.nexusfx.di.annotations.Inject;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.stage.Window;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class RelatorioDashboardController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(RelatorioDashboardController.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String MODELOS_FILE = "relatorio_modelos.json";

    @Inject private InventarioEquipamentosService inventarioService;
    @Inject private CatalogoProdutosService catalogoService;
    @Inject private FuncionariosService funcionariosService;
    @Inject private EstoqueMovimentacoesService movimentacoesService;
    @Inject private UsuariosService usuariosService;

    // ===== KPIs DO TOPO =====
    @FXML private Label labelHoje, labelSemana, labelModelosSalvos, labelUltimaAtualizacao;
    @FXML private ImageView imageicone;

    // ===== GRÁFICO =====
    @FXML private BarChart<String, Number> graficoCombinacoes;

    // ===== TABELA ESTATÍSTICAS =====
    @FXML private TableView<TabelaEstats> tabelaEstatisticas;
    @FXML private TableColumn<TabelaEstats, String> colTabela, colConsultas, colUso;

    // ===== TABELA RELATÓRIOS RECENTES =====
    @FXML private TableView<RelatorioRecente> tabelaRelatoriosRecentes;
    @FXML private TableColumn<RelatorioRecente, String> colRecData, colRecTitulo, colRecTabelas;
    @FXML private TableColumn<RelatorioRecente, Void> colRecAcoes;

    // ===== KPIs SECUNDÁRIOS =====
    @FXML private Label labelTotalRelatorios, labelMediaDia;
    @FXML private Label labelCombinacoesUnicas, labelTotalModelos;

    // ===== MODELOS SALVOS =====
    @FXML private ListView<String> listaModelosSalvos;

    // ===== DADOS =====
    private List<InventarioEquipamentos> equipamentos = new ArrayList<>();
    private List<CatalogoProdutos> catalogos = new ArrayList<>();
    private List<Funcionarios> funcionarios = new ArrayList<>();

    // Histórico de relatórios gerados (em memória)
    private final List<ModeloRelatorio> historicoRelatorios = new ArrayList<>();
    private final Map<String, Integer> contadorPorTabela = new LinkedHashMap<>();

    // =========================================================================
    // INITIALIZE
    // =========================================================================

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarTabelas();
        carregarModelosSalvos();
        carregarDados();
    }

    private void configurarTabelas() {
        colTabela.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTabela()));
        colConsultas.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf(cell.getValue().getConsultas())));
        colUso.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getUso()));

        colRecData.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getData()));
        colRecTitulo.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTitulo()));
        colRecTabelas.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTabelas()));

        colRecAcoes.setCellFactory(col -> new TableCell<>() {
            private final Button btnReusar = new Button("🔄");
            {
                btnReusar.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 11px;");
                btnReusar.setTooltip(new Tooltip("Reutilizar este relatório como modelo"));
                btnReusar.setOnAction(e -> {
                    RelatorioRecente item = getTableView().getItems().get(getIndex());
                    salvarModelo(item.getTitulo(), item.getTabelas());
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnReusar);
            }
        });
    }

    // =========================================================================
    // CARREGAMENTO DE DADOS
    // =========================================================================

    private void carregarDados() {
        new Thread(() -> {
            try {
                equipamentos = inventarioService.listarTodos(1, 10000);
                catalogos = catalogoService.listarTodos(1, 10000);
                funcionarios = funcionariosService.listarTodos(1, 10000);

                Platform.runLater(() -> {
                    popularIndicadores();
                    popularGraficoVisaoGeral();
                    popularTabelaEstatisticas();
                    popularRelatoriosRecentes();
                    popularKPIs();
                    labelUltimaAtualizacao.setText("Atualizado: " + LocalDateTime.now().format(DATE_FMT));
                });
            } catch (Exception e) {
                logger.error("Erro ao carregar dados", e);
                Platform.runLater(() -> NexusFX.alerts().erro("Erro", "Falha ao carregar dados.", "Relatórios"));
            }
        }).start();
    }

    // =========================================================================
    // INDICADORES DO TOPO
    // =========================================================================

    private void popularIndicadores() {
        LocalDate hoje = LocalDate.now();
        LocalDate inicioSemana = hoje.minusDays(7);

        long hojeCount = historicoRelatorios.stream()
                .filter(m -> m.getDataCriacao() != null && m.getDataCriacao().toLocalDate().equals(hoje))
                .count();
        long semanaCount = historicoRelatorios.stream()
                .filter(m -> m.getDataCriacao() != null && !m.getDataCriacao().toLocalDate().isBefore(inicioSemana))
                .count();

        if (labelHoje != null) labelHoje.setText(String.valueOf(hojeCount));
        if (labelSemana != null) labelSemana.setText(String.valueOf(semanaCount));
        if (labelModelosSalvos != null) labelModelosSalvos.setText(String.valueOf(listaModelosSalvos.getItems().size()));
    }

    // =========================================================================
    // GRÁFICO: VISÃO GERAL DE RELATÓRIOS GERADOS
    // =========================================================================

    private void popularGraficoVisaoGeral() {
        graficoCombinacoes.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Relatórios Gerados");

        contadorPorTabela.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(8)
                .forEach(e -> series.getData().add(new XYChart.Data<>(e.getKey(), e.getValue())));

        if (series.getData().isEmpty()) {
            series.getData().add(new XYChart.Data<>("Nenhum relatório gerado", 0));
        }

        graficoCombinacoes.getData().add(series);
    }

    // =========================================================================
    // TABELA: TABELAS MAIS USADAS EM RELATÓRIOS
    // =========================================================================

    private void popularTabelaEstatisticas() {
        ObservableList<TabelaEstats> dados = FXCollections.observableArrayList();

        contadorPorTabela.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(e -> {
                    String ultimaVez = historicoRelatorios.stream()
                            .filter(m -> m.getTabelas().contains(e.getKey()))
                            .max(Comparator.comparing(ModeloRelatorio::getDataCriacao))
                            .map(m -> m.getDataCriacao().format(DATE_FMT))
                            .orElse("-");
                    dados.add(new TabelaEstats(e.getKey(), e.getValue(), ultimaVez));
                });

        if (dados.isEmpty()) {
            dados.add(new TabelaEstats("Nenhum relatório gerado ainda", 0, "-"));
        }

        tabelaEstatisticas.setItems(dados);
    }

    // =========================================================================
    // TABELA: RELATÓRIOS RECENTES
    // =========================================================================

    private void popularRelatoriosRecentes() {
        ObservableList<RelatorioRecente> dados = FXCollections.observableArrayList();

        historicoRelatorios.stream()
                .sorted((a, b) -> b.getDataCriacao().compareTo(a.getDataCriacao()))
                .limit(15)
                .forEach(m -> dados.add(new RelatorioRecente(
                        m.getDataCriacao().format(DATE_FMT),
                        m.getTitulo(),
                        m.getTabelas()
                )));

        tabelaRelatoriosRecentes.setItems(dados);
    }

    // =========================================================================
    // KPIs SECUNDÁRIOS
    // =========================================================================

    private void popularKPIs() {
        int total = historicoRelatorios.size();
        long dias = Math.max(1, java.time.temporal.ChronoUnit.DAYS.between(
                LocalDate.now().minusMonths(1), LocalDate.now()));
        int mediaDia = (int) (total / Math.max(1, dias));
        int combinacoesUnicas = contadorPorTabela.size();
        int modelos = listaModelosSalvos.getItems().size();

        if (labelTotalRelatorios != null) labelTotalRelatorios.setText(String.valueOf(total));
        if (labelMediaDia != null) labelMediaDia.setText(String.valueOf(mediaDia));
        if (labelCombinacoesUnicas != null) labelCombinacoesUnicas.setText(String.valueOf(combinacoesUnicas));
        if (labelTotalModelos != null) labelTotalModelos.setText(String.valueOf(modelos));
    }

    // =========================================================================
    // MODELOS SALVOS
    // =========================================================================

    private void carregarModelosSalvos() {
        listaModelosSalvos.getItems().clear();
        File file = new File(MODELOS_FILE);
        if (file.exists()) {
            try {
                List<String> linhas = Files.readAllLines(file.toPath());
                listaModelosSalvos.getItems().addAll(linhas);
            } catch (IOException e) {
                logger.warn("Erro ao carregar modelos salvos: {}", e.getMessage());
            }
        }
    }

    private void salvarModelo(String nome, String tabelas) {
        String linha = LocalDateTime.now().format(DATE_FMT) + " | " + nome + " | " + tabelas;
        listaModelosSalvos.getItems().add(0, linha);

        try {
            Files.write(Paths.get(MODELOS_FILE), listaModelosSalvos.getItems());
        } catch (IOException e) {
            logger.warn("Erro ao salvar modelo: {}", e.getMessage());
        }

        popularKPIs();
    }

    @FXML
    private void removerModeloSelecionado() {
        int idx = listaModelosSalvos.getSelectionModel().getSelectedIndex();
        if (idx >= 0) {
            listaModelosSalvos.getItems().remove(idx);
            try {
                Files.write(Paths.get(MODELOS_FILE), listaModelosSalvos.getItems());
            } catch (IOException ignored) {}
            popularKPIs();
        }
    }

    @FXML
    private void exportarModeloSelecionado() {
        String selecionado = listaModelosSalvos.getSelectionModel().getSelectedItem();
        if (selecionado != null) {
            // Extrai tabelas do modelo e exporta
            String[] partes = selecionado.split(" \\| ");
            if (partes.length >= 3) {
                String tabelas = partes[2];
                if (tabelas.contains("Inventário")) exportarInventarioCompleto();
                else if (tabelas.contains("Catálogo")) exportarCatalogo();
                else if (tabelas.contains("Funcionário")) exportarFuncionarios();
                else if (tabelas.contains("Movimentação")) exportarMovimentacoes();
            }
        }
    }

    // =========================================================================
    // REGISTRO DE RELATÓRIO GERADO
    // =========================================================================

    private void registrarRelatorioGerado(String titulo, String tabelas) {
        ModeloRelatorio modelo = new ModeloRelatorio(titulo, tabelas, LocalDateTime.now());
        historicoRelatorios.add(modelo);

        // Incrementa contador para cada tabela usada
        for (String tabela : tabelas.split(" \\+ ")) {
            contadorPorTabela.merge(tabela.trim(), 1, Integer::sum);
        }

        // Atualiza UI
        Platform.runLater(() -> {
            popularIndicadores();
            popularGraficoVisaoGeral();
            popularTabelaEstatisticas();
            popularRelatoriosRecentes();
            popularKPIs();
        });
    }

    // =========================================================================
    // EXPORTAÇÕES
    // =========================================================================

    @FXML
    private void exportarParaExcel() {
        exportarInventarioCompleto();
    }

    @FXML
    private void exportarInventarioCompleto() {
        FileChooser fc = criarFileChooser("inventario_completo");
        File file = fc.showSaveDialog(getStage());
        if (file != null) {
            new Thread(() -> {
                try {
                    ExportarExcelService.exportarInventarioCompleto(
                            file.getAbsolutePath(), equipamentos, catalogos, funcionarios);
                    Platform.runLater(() -> {
                        NexusFX.alerts().info("Exportação Concluída", "Arquivo: " + file.getName(), "Relatórios");
                        registrarRelatorioGerado("Inventário Completo", "Inventário + Catálogo + Funcionários");
                    });
                } catch (Exception e) {
                    logger.error("Erro ao exportar", e);
                    Platform.runLater(() -> NexusFX.alerts().erro("Erro", e.getMessage(), "Relatórios"));
                }
            }).start();
        }
    }

    @FXML
    private void exportarCatalogo() {
        FileChooser fc = criarFileChooser("catalogo_produtos");
        File file = fc.showSaveDialog(getStage());
        if (file != null) {
            new Thread(() -> {
                try {
                    ExportarExcelService.exportarCatalogo(file.getAbsolutePath(), catalogos);
                    Platform.runLater(() -> {
                        NexusFX.alerts().info("Exportação Concluída", "Arquivo: " + file.getName(), "Relatórios");
                        registrarRelatorioGerado("Catálogo de Produtos", "Catálogo");
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> NexusFX.alerts().erro("Erro", e.getMessage(), "Relatórios"));
                }
            }).start();
        }
    }

    @FXML
    private void exportarFuncionarios() {
        FileChooser fc = criarFileChooser("funcionarios");
        File file = fc.showSaveDialog(getStage());
        if (file != null) {
            new Thread(() -> {
                try {
                    ExportarExcelService.exportarFuncionarios(file.getAbsolutePath(), funcionarios);
                    Platform.runLater(() -> {
                        NexusFX.alerts().info("Exportação Concluída", "Arquivo: " + file.getName(), "Relatórios");
                        registrarRelatorioGerado("Funcionários", "Funcionários");
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> NexusFX.alerts().erro("Erro", e.getMessage(), "Relatórios"));
                }
            }).start();
        }
    }

    @FXML
    private void exportarMovimentacoes() {
        FileChooser fc = criarFileChooser("movimentacoes");
        File file = fc.showSaveDialog(getStage());
        if (file != null) {
            new Thread(() -> {
                try {
                    List<EstoqueMovimentacoes> movs = movimentacoesService.listarTodas(1, 10000);
                    ExportarExcelService.exportarMovimentacoes(file.getAbsolutePath(), movs);
                    Platform.runLater(() -> {
                        NexusFX.alerts().info("Exportação Concluída", "Arquivo: " + file.getName(), "Relatórios");
                        registrarRelatorioGerado("Movimentações de Estoque", "Movimentações");
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> NexusFX.alerts().erro("Erro", e.getMessage(), "Relatórios"));
                }
            }).start();
        }
    }

    @FXML
    private void limparHistoricoRelatorios() {
        historicoRelatorios.clear();
        contadorPorTabela.clear();
        popularIndicadores();
        popularGraficoVisaoGeral();
        popularTabelaEstatisticas();
        popularRelatoriosRecentes();
        popularKPIs();
        NexusFX.alerts().info("Histórico Limpo", "O histórico de relatórios foi limpo.", "Relatórios");
    }

    // =========================================================================
    // HANDLERS
    // =========================================================================

    @FXML private void atualizarDashboard() { carregarDados(); }

    // Substituir os handlers de abertura no RelatorioDashboardController:

    @FXML
    private void abrirRelatorioSimples() {
        try {
            Window owner = graficoCombinacoes.getScene().getWindow();
            NexusFX.dialogs().openModalWithController(
                    "relasimples",
                    "Relatório Simples",
                    owner,
                    (Object controller) -> {
                        // Controller já está configurado via initialize()
                    }
            );
            registrarRelatorioGerado("Relatório Simples", "Sistema");
        } catch (Exception e) {
            logger.error("Erro ao abrir Relatório Simples", e);
            NexusFX.alerts().erro("Erro", "Não foi possível abrir o Relatório Simples.", "Relatórios");
        }
    }

    @FXML
    private void abrirRelatorioAvancado() {
        try {
            Window owner = graficoCombinacoes.getScene().getWindow();
            NexusFX.dialogs().openModalWithController(
                    "relaavancado",
                    "Relatório Avançado",
                    owner,
                    (Object controller) -> {
                        // Controller já está configurado via initialize()
                    }
            );
            registrarRelatorioGerado("Relatório Avançado", "Sistema");
        } catch (Exception e) {
            logger.error("Erro ao abrir Relatório Avançado", e);
            NexusFX.alerts().erro("Erro", "Não foi possível abrir o Relatório Avançado.", "Relatórios");
        }
    }

    // =========================================================================
    // UTILITÁRIOS
    // =========================================================================

    private FileChooser criarFileChooser(String nomeBase) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Salvar Relatório Excel");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel (*.xlsx)", "*.xlsx"));
        fc.setInitialFileName(nomeBase + "_" + LocalDate.now() + ".xlsx");
        return fc;
    }

    private Stage getStage() {
        return (Stage) graficoCombinacoes.getScene().getWindow();
    }

    // =========================================================================
    // DTOs
    // =========================================================================

    public static class TabelaEstats {
        private final String tabela;
        private final int consultas;
        private final String uso;
        public TabelaEstats(String tabela, int consultas, String uso) {
            this.tabela = tabela; this.consultas = consultas; this.uso = uso;
        }
        public String getTabela() { return tabela; }
        public int getConsultas() { return consultas; }
        public String getUso() { return uso; }
    }

    public static class RelatorioRecente {
        private final String data, titulo, tabelas;
        public RelatorioRecente(String data, String titulo, String tabelas) {
            this.data = data; this.titulo = titulo; this.tabelas = tabelas;
        }
        public String getData() { return data; }
        public String getTitulo() { return titulo; }
        public String getTabelas() { return tabelas; }
    }

    private static class ModeloRelatorio {
        private final String titulo;
        private final String tabelas;
        private final LocalDateTime dataCriacao;
        public ModeloRelatorio(String titulo, String tabelas, LocalDateTime dataCriacao) {
            this.titulo = titulo; this.tabelas = tabelas; this.dataCriacao = dataCriacao;
        }
        public String getTitulo() { return titulo; }
        public String getTabelas() { return tabelas; }
        public LocalDateTime getDataCriacao() { return dataCriacao; }
    }
}