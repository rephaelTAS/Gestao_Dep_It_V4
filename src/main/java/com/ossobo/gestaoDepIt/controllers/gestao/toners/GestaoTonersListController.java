package com.ossobo.gestaoDepIt.controllers.gestao.toners;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Window;
import com.ossobo.nexusfx.di.annotations.Component;
import com.ossobo.nexusfx.NexusFX;

/**
 * 🎯 Controller Elite para Gestão de Toners
 * Gerencia filtros, listagem paginada e ações de estoque.
 */
@Component
public class GestaoTonersListController {

    // ===== FILTROS =====
    @FXML private ComboBox<String> filtroEquipamentoCombo, filtroTonerCombo, filtroStatusCombo;
    @FXML private TextField filtroPercentagemMin, filtroPercentagemMax;

    // ===== TABELA E PAGINAÇÃO =====
    @FXML private TableView<Object> tonersTable; // Substitua Object pelo seu Model (ex: TonerInstallation)
    @FXML private Label estatisticasLabel, alertasLabel, labelPagina, statusLabel;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private ComboBox<Integer> comboItensPorPagina;

    @FXML private Button novaInstalacaoButton;

    @FXML
    public void initialize() {
        setupTable();
        setupFormControls();
        carregarDados();
    }

    private void setupTable() {
        // Aqui você configuraria as colunas de Ações com botões (Editar/Excluir)
        statusLabel.setText("Sistema de Gestão Pronto.");
    }

    private void setupFormControls() {
        // Popular combos simulados
        filtroStatusCombo.setItems(FXCollections.observableArrayList("Normal", "Baixo", "Crítico"));
        comboItensPorPagina.setItems(FXCollections.observableArrayList(10, 20, 50, 100));
        comboItensPorPagina.setValue(20);
    }

    private void carregarDados() {
        progressIndicator.setVisible(true);
        // Simulação de carregamento assíncrono
        estatisticasLabel.setText("Total: 150 registros | Exibindo 20");
        progressIndicator.setVisible(false);
    }

    // =============================================
    // ⚡ INTERFACE ACTIONS (Resolvendo Erros de FXML)
    // =============================================

    @FXML
    private void handleNovaInstalacao(ActionEvent event) {
        Node source = (Node) event.getSource();
        Window parentWindow = source.getScene().getWindow();  // Window, não Stage

        // ✅ CORRETO - openWindowModal com Window
        NexusFX.dialogs().openModalWithController(
                "addeditartoner",
                "Registrar Troca",
                parentWindow,
                (AddEditTonerController controller) -> {
                    // Configuração opcional do controller
                    // Ex: controller.setModoInstalacao();
                }
        );
    }

    @FXML
    private void handleLimparFiltros() {
        filtroEquipamentoCombo.setValue(null);
        filtroTonerCombo.setValue(null);
        filtroStatusCombo.setValue(null);
        filtroPercentagemMin.clear();
        filtroPercentagemMax.clear();
        NexusFX.alerts().info("Filtros", "Critérios de busca resetados.", "SYSTEM");
    }

    @FXML
    private void handleAplicarFiltros() {
        String status = filtroStatusCombo.getValue();
        statusLabel.setText("Filtrando por: " + (status != null ? status : "Todos"));
        carregarDados();
    }

    // ===== NAVEGAÇÃO / PAGINAÇÃO =====

    @FXML
    private void handlePrimeiraPagina() {
        statusLabel.setText("Início da lista.");
    }

    @FXML
    private void handlePaginaAnterior() {
        // Lógica de decremento de página
    }

    @FXML
    private void handleProximaPagina() {
        // Lógica de incremento de página
    }

    @FXML
    private void handleUltimaPagina() {
        statusLabel.setText("Fim da lista.");
    }
}