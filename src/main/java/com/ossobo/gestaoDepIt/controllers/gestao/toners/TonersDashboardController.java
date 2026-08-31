package com.ossobo.gestaoDepIt.controllers.gestao.toners;


import com.ossobo.nexusfx.view.loader.LoadedView;
import com.ossobo.nexusfx.view.refresh.RefreshableController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.Window;
import com.ossobo.gestaoDepIt.config.ViewConstant;
import com.ossobo.nexusfx.NexusFX;

/**
 * v4.4.7 - Controller para o Dashboard de Toners
 * Implementa RefreshableController para integração com o RefreshManager do NexusFX
 *
 * ✅ CORRIGIDO: Substituído NexusFX.modals() por NexusFX.dialogs().openWindowModal()
 */
public class TonersDashboardController implements RefreshableController {

    // ===== UI COMPONENTS =====
    @FXML private Label cardAtivos, cardAlertas, cardMediaUso, cardCiclosMedios;
    @FXML private PieChart distribuicaoChart;
    @FXML private ListView<String> alertasListView;
    @FXML private TableView<Object> tonersCriticosTable;
    @FXML private TableColumn<Object, String> colCriticoEquipamento, colCriticoToner, colCriticoPercentagem, colCriticoDias, colCriticoAcao;
    @FXML private Button btn_listaToner;

    // ===== CONSTRUTOR =====
    public TonersDashboardController() {
        // NexusFX cuidará da injeção
    }

    @FXML
    public void initialize() {
        System.out.println("🚀 Inicializando Dashboard de Toners via NexusFX");
        setupTable();
        refreshData();
    }

    // ===== INTERFACE REFRESHABLE =====
    @Override
    public void refreshData() {
        try {
            updateCards();
            updateCharts();
            updateAlertList();
            updateCriticalTable();
        } catch (Exception e) {
            NexusFX.alerts().erro("Erro de Atualização", "Falha ao carregar dados do Dashboard.", e.getMessage(), "TONERS_DASH");
        }
    }

    // ===== LÓGICA DE NEGÓCIO =====
    private void updateCards() {
        cardAtivos.setText("124");
        cardAlertas.setText("8");
        cardMediaUso.setText("62%");
        cardCiclosMedios.setText("4.200");
    }

    private void updateCharts() {
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                new PieChart.Data("Crítico (<10%)", 8),
                new PieChart.Data("Baixo (10-30%)", 25),
                new PieChart.Data("Normal (>30%)", 91)
        );
        distribuicaoChart.setData(pieData);
    }

    private void updateAlertList() {
        alertasListView.getItems().setAll(
                "⚠️ HP LaserJet 500: Toner Preto em 5%",
                "⚠️ Samsung M4070: Toner expirando em 2 dias",
                "🚨 Estoque de Toner CF226X está zerado!"
        );
    }

    private void updateCriticalTable() {
        // Carregar do banco de dados
    }

    private void setupTable() {
        colCriticoEquipamento.setCellValueFactory(f -> new javafx.beans.property.SimpleStringProperty("Equipamento X"));
    }

    // ===== HANDLERS DE EVENTOS (CORRIGIDOS) =====

    @FXML
    private void handleAtualizar() {
        refreshData();
        NexusFX.alerts().info("Sincronização", "Dados atualizados com sucesso.", "SYSTEM");
    }

    /**
     * v4.4.7 - Correção: NexusFX.dialogs().openWindowModal()
     */
    @FXML
    private void handleGerarRelatorio() {
        Stage currentStage = NexusFX.stage();
        if (currentStage != null) {
            NexusFX.dialogs().openModalWithController(
                    "view_relatorio_toner",
                    "Exportar Relatório",
                    currentStage,
                    null  // Sem callback, apenas abre o modal
            );
        } else {
            NexusFX.alerts().erro("Erro", "Não foi possível obter a janela principal", "Tente novamente", "TONER");
        }
    }

    @FXML
    private void handleVerEstoque() {
        NexusFX.views().loadFresh("view_estoque_toners");
    }

    /**
     * v4.4.7 - Correção: Substituído NexusFX.modals().openModal() por dialogs()
     */
    @FXML
    private void handeleListarToner() {
        Window window = btn_listaToner.getScene().getWindow();

        if (window instanceof Stage) {
            NexusFX.dialogs().openModalWithController(
                    "listartoner",
                    "Listar Tonners",
                    (Stage) window,
                    controller -> {
                        // Callback opcional após carregar o modal
                        System.out.println("✅ Modal de listagem de toners aberto");
                    }
            );
        }

        // Carregar view principal (se necessário)
        LoadedView<?> mainViewResult = NexusFX.views().load(ViewConstant.Toner.LISTARTONER);
        Parent mainView = mainViewResult.getRoot();
        Object controller = mainViewResult.getController();
    }

    @FXML
    private void handlePlanejarSubstituicoes() {
        NexusFX.alerts().info(
                "Planejamento",
                "Função de planejamento automático em desenvolvimento.",
                "Aguarde a próxima atualização do NexusFX.",
                "MODULO_PLAN"
        );
    }
}