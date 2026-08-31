package com.ossobo.gestaoDepIt.controllers.gestao.equipamentos.inventario;

import com.ossobo.winterfx.anotations.Controller;
import com.ossobo.winterfx.anotations.Inject;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Window;
import com.ossobo.gestaoDepIt.db.models.InventarioEquipamentos;
import com.ossobo.gestaoDepIt.db.services.InventarioEquipamentosService;
import com.ossobo.gestaoDepIt.controllers.gestao.equipamentos.inventario.historico.HistoricoEventosListController;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

/**
 * Controller principal do inventário de equipamentos.
 * v1.2 — Ação de histórico por SKU + Funcionário integrada.
 */
@Controller
public class InventarioListController implements Initializable {

    @Inject
    private InventarioEquipamentosService service;

    // Tabela
    @FXML private TableView<InventarioEquipamentos> equipamentosTable;
    @FXML private TableColumn<InventarioEquipamentos, String> colNumSerie, colEnderecoMac, colSkuProduto;
    @FXML private TableColumn<InventarioEquipamentos, String> colFuncionarioId, colLocalizacao, colDepartamento;
    @FXML private TableColumn<InventarioEquipamentos, String> colStatus, colCondicao;
    @FXML private TableColumn<InventarioEquipamentos, String> colDataAquisicao, colDataInstalacao, colDataUltimaVerificacao;
    @FXML private TableColumn<InventarioEquipamentos, Void> colAcoes;

    // Filtros
    @FXML private ComboBox<String> filtroStatusCombo, filtroCondicaoCombo, filtroLocalizacaoCombo, filtroDepartamentoCombo;
    @FXML private TextField filtroMacField, filtroBuscaField;
    @FXML private DatePicker filtroAquisicaoInicio, filtroAquisicaoFim;
    @FXML private DatePicker filtroInstalacaoInicio, filtroInstalacaoFim;
    @FXML private DatePicker filtroVerificacaoInicio, filtroVerificacaoFim;

    // Botões
    @FXML private Button novoButton, aplicarFiltrosButton, limparFiltrosButton, historicoButton;
    @FXML private Button btnPrimeira, btnAnterior, btnProxima, btnUltima;

    // Stats e status
    @FXML private Label statTotal, statAtivos, statManutencao, statCriticos, statSemMac;
    @FXML private Label statusLabel, labelPagina;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private ComboBox<Integer> comboItensPorPagina;

    private final ObservableList<InventarioEquipamentos> dados = FXCollections.observableArrayList();
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private int paginaAtual = 1;
    private int itensPorPagina = 25;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarTabela();
        configurarFiltros();
        configurarPaginacao();
        carregarDados();
    }

    // ===== TABELA =====
    private void configurarTabela() {
    }

    // ===== DOUBLE CLICK → ABRIR DETALHES =====


    // ===== FILTROS =====
    private void configurarFiltros() {
        filtroStatusCombo.getItems().setAll("ATIVO", "MANUTENCAO", "BAIXADO", "RESERVA", "EM_USO");
        filtroCondicaoCombo.getItems().setAll("OTIMO", "BOM", "REGULAR", "CRITICO");
    }

    // ===== PAGINAÇÃO =====
    private void configurarPaginacao() {
        comboItensPorPagina.getItems().setAll(10, 25, 50, 100);
        comboItensPorPagina.setValue(25);
        comboItensPorPagina.setOnAction(e -> {
            itensPorPagina = comboItensPorPagina.getValue();
            paginaAtual = 1;
            carregarDados();
        });
    }

    // ===== CARREGAR DADOS =====
    private void carregarDados() {

    }



    // ===== STATS =====
    private void atualizarStats() {

    }

    // ===== NAVEGAÇÃO =====
    private void editarEquipamento(InventarioEquipamentos eq) {

    }

    private void abrirDetalhes(InventarioEquipamentos eq) {

    }

    /**
     * Abre o histórico pré-filtrado pelo SKU do equipamento E pelo funcionário associado.
     * Mostra todos os eventos daquele SKU (mesmo com outros funcionários) +
     * todos os eventos daquele funcionário (mesmo com outros SKUs).
     */
    private void abrirHistoricoSkuFuncionario(InventarioEquipamentos eq) {

    }

    // ===== HANDLERS FXML =====
    @FXML
    private void handleNovoEquipamento() {

    }

    @FXML private void handleAplicarFiltros() {

    }

    @FXML private void handleLimparFiltros() {

    }

    @FXML private void handleVerHistorico() {

    }

    @FXML private void handlePrimeiraPagina() { paginaAtual = 1; carregarDados(); }
    @FXML private void handlePaginaAnterior() { if (paginaAtual > 1) { paginaAtual--; carregarDados(); } }
    @FXML private void handleProximaPagina() { paginaAtual++; carregarDados(); }
    @FXML private void handleUltimaPagina() { paginaAtual = 999; carregarDados(); }

    // ===== UTILITÁRIOS =====
    private void showProgress(boolean show) {
        progressIndicator.setVisible(show);
        statusLabel.setText(show ? "Carregando..." : "Pronto");
        novoButton.setDisable(show);
        aplicarFiltrosButton.setDisable(show);
    }

    private String nvl(String s) { return s != null && !s.isEmpty() ? s : "-"; }
    private String fmtDate(LocalDate d) { return d != null ? d.format(DATE_FMT) : "-"; }
    private String formatMac(String mac) {
        if (mac == null || mac.isEmpty()) return "-";
        String limpo = mac.replaceAll("[:\\-]", "").toUpperCase();
        return limpo.length() == 12 ? limpo.replaceAll("(.{2})", "$1:").substring(0, 17) : mac;
    }
}