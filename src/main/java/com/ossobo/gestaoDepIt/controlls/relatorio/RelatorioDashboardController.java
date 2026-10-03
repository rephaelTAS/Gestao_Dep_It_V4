package com.ossobo.gestaoDepIt.controlls.relatorio;

import com.ossobo.gestaoDepIt.config.ViewConstant;
import com.ossobo.gestaoDepIt.controlls.relatorio.dados.CombinarDados;
import com.ossobo.gestaoDepIt.controlls.relatorio.dashboard.DashboardState;
import com.ossobo.gestaoDepIt.controlls.relatorio.dashboard.DashboardUIUpdater;
import com.ossobo.gestaoDepIt.db.relatorios.RelatorioModelo;
import com.ossobo.winterfx.anotations.Controller;
import com.ossobo.winterfx.anotations.PostConstruct;
import com.ossobo.winterfx.view.anotations.RegisterView;
import com.ossobo.winterfx.view.controller.WinterFXController;
import com.ossobo.winterfx.view.floatingwindow.StageForFloatingWindow;
import com.ossobo.winterfx.view.floatingwindow.anotations.FloatingWindow;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * RelatorioDashboardController v1.1
 *
 * Tela inicial do módulo de relatórios: KPIs, gráfico de combinações,
 * tabelas de estatísticas e recentes, lista de modelos salvos.
 *
 * v1.1 — Zero chamadas diretas a Rotas: tudo via CombinarDados.
 *        Reflection de "nome" eliminada (usa RelatorioModelo.nome()).
 *        Remoção de modelo resolve nome → id via fachada.
 */
@Controller(proxy = false)
@RegisterView(
        id = ViewConstant.Relatorio.DASHBOARD,
        fxml = "/META-INF/gestaoDepIt/fxmls/relatorio/dashboardRelatorio.fxml",
        title = "Dashboard de Relatórios"
)
public class RelatorioDashboardController implements WinterFXController {

    private static final System.Logger logger =
            System.getLogger(RelatorioDashboardController.class.getName());

    private static final DateTimeFormatter FMT_DATA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final DashboardState state = new DashboardState();

    // ---- KPIs topo ----
    @FXML private Label labelHoje;
    @FXML private Label labelSemana;
    @FXML private Label labelModelosSalvos;

    // ---- KPIs rodapé ----
    @FXML private Label labelTotalRelatorios;
    @FXML private Label labelMediaDia;
    @FXML private Label labelCombinacoesUnicas;
    @FXML private Label labelTotalModelos;

    // ---- Gráfico ----
    @FXML private BarChart<String, Number> graficoCombinacoes;

    // ---- Tabelas ----
    @FXML private TableView<Map<String, Object>> tabelaEstatisticas;
    @FXML private TableView<Map<String, Object>> tabelaRelatoriosRecentes;

    // ---- Colunas: tabelaEstatisticas ----
    @FXML private TableColumn<Map<String, Object>, Object> colTabela;
    @FXML private TableColumn<Map<String, Object>, Object> colConsultas;
    @FXML private TableColumn<Map<String, Object>, Object> colUso;

    // ---- Colunas: tabelaRelatoriosRecentes ----
    @FXML private TableColumn<Map<String, Object>, Object> colRecData;
    @FXML private TableColumn<Map<String, Object>, Object> colRecTitulo;
    @FXML private TableColumn<Map<String, Object>, Object> colRecTabelas;
    @FXML private TableColumn<Map<String, Object>, Object> colRecAcoes;

    // ---- Lista de modelos ----
    @FXML private ListView<String> listaModelosSalvos;

    @FloatingWindow(
            viewId = ViewConstant.Relatorio.AVANCADO,
            singleton = false
    )
    private StageForFloatingWindow relaAvancado;

    @FloatingWindow(
            viewId = ViewConstant.Relatorio.SIMPLIFICADO,
            singleton = false
    )
    private StageForFloatingWindow relaSimplificado;

    // ============================================================
    // CICLO DE VIDA
    // ============================================================

    @PostConstruct
    public void init() {
        inicializarColunasTabelas();
        try {
            carregarKpis();
            carregarGrafico();
            carregarRecentes();
            carregarModelos();
        } catch (Exception e) {
            logger.log(System.Logger.Level.ERROR,
                    "❌ Falha ao carregar dashboard: {0}", e.getMessage(), e);
        }
    }

    // ============================================================
    // BINDING DAS COLUNAS
    // ============================================================

    private void inicializarColunasTabelas() {
        // ----- tabelaEstatisticas -----
        if (colTabela != null)
            colTabela.setCellValueFactory(d -> new SimpleObjectProperty<>(
                    String.valueOf(d.getValue().getOrDefault("tabela", ""))));
        if (colConsultas != null)
            colConsultas.setCellValueFactory(d -> new SimpleObjectProperty<>(
                    String.valueOf(d.getValue().getOrDefault("consultas", 0))));
        if (colUso != null)
            colUso.setCellValueFactory(d -> new SimpleObjectProperty<>(
                    String.valueOf(d.getValue().getOrDefault("ultimaVez", "—"))));

        // ----- tabelaRelatoriosRecentes -----
        if (colRecData != null)
            colRecData.setCellValueFactory(d -> new SimpleObjectProperty<>(
                    formatarData(d.getValue().get("geradoEm"))));
        if (colRecTitulo != null)
            colRecTitulo.setCellValueFactory(d -> new SimpleObjectProperty<>(
                    String.valueOf(d.getValue().getOrDefault("titulo", ""))));
        if (colRecTabelas != null)
            colRecTabelas.setCellValueFactory(d -> new SimpleObjectProperty<>(
                    String.valueOf(d.getValue().getOrDefault("tabelas", ""))));
        if (colRecAcoes != null)
            colRecAcoes.setCellValueFactory(d -> new SimpleObjectProperty<>(""));
    }

    /** Aceita LocalDateTime, String ISO ou null. */
    private String formatarData(Object valor) {
        if (valor == null) return "—";
        if (valor instanceof LocalDateTime dt) return dt.format(FMT_DATA_HORA);
        return valor.toString();
    }

    // ============================================================
    // AÇÕES
    // ============================================================

    @FXML
    public void btn_atualizar(ActionEvent event) {
        init();
    }

    @FXML
    public void btn_relatorioSimples(ActionEvent event) {
        relaSimplificado.show();
    }

    @FXML
    public void btn_relatorioAvancado(ActionEvent event) {
        relaAvancado.show();
    }

    @FXML
    public void btn_removerModelo(ActionEvent event) {
        String selecionado = listaModelosSalvos != null
                ? listaModelosSalvos.getSelectionModel().getSelectedItem()
                : null;
        if (selecionado == null) return;

        // A lista mostra nomes; a remoção exige id. Resolve pelo nome.
        RelatorioModelo alvo = CombinarDados.listarModelos().stream()
                .filter(m -> selecionado.equals(m.nome()))
                .findFirst()
                .orElse(null);
        if (alvo == null) return;

        if (CombinarDados.removerModelo(alvo.id())) {
            carregarModelos();
            carregarKpis();
        } else {
            logger.log(System.Logger.Level.WARNING,
                    "Falha ao remover modelo: {0}", selecionado);
        }
    }

    @FXML
    public void btn_limparHistorico(ActionEvent event) {
        // Ledger é append-only — sem rota de delete.
        logger.log(System.Logger.Level.INFO,
                "ℹ️ Histórico é ledger append-only — não pode ser apagado.");
    }

    // ============================================================
    // CARREGAMENTO VIA FACHADA
    // ============================================================

    private void carregarKpis() {
        state.setRelatoriosHoje(CombinarDados.totalHoje());
        state.setRelatoriosSemana(CombinarDados.totalSemana());
        state.setTotalRelatorios(CombinarDados.totalGeral());
        state.setMediaPorDia(CombinarDados.mediaPorDia());

        state.setTotalModelos(CombinarDados.listarModelos().size());

        Map<String, Integer> combinacoes = CombinarDados.contagemPorCombinacao();
        state.setCombinacoesUnicas(combinacoes.size());
        state.setContagemPorCombinacao(combinacoes);

        DashboardUIUpdater.atualizarKpis(state,
                labelHoje, labelSemana, labelModelosSalvos,
                labelTotalRelatorios, labelMediaDia,
                labelCombinacoesUnicas, labelTotalModelos);
    }

    private void carregarGrafico() {
        DashboardUIUpdater.atualizarGrafico(
                graficoCombinacoes, state.getContagemPorCombinacao());
        DashboardUIUpdater.atualizarTabelaEstatisticas(
                tabelaEstatisticas, state.getContagemPorCombinacao());
    }

    private void carregarRecentes() {
        List<Map<String, Object>> lista = CombinarDados.recentes(20);
        state.setRecentes(lista);
        DashboardUIUpdater.atualizarTabelaRecentes(tabelaRelatoriosRecentes, lista);
    }

    private void carregarModelos() {
        List<String> nomes = CombinarDados.listarModelos().stream()
                .map(m -> m.nome() == null ? "?" : m.nome())
                .toList();
        DashboardUIUpdater.atualizarListaModelos(listaModelosSalvos, nomes);
    }
}