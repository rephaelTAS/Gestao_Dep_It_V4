package com.ossobo.gestaoDepIt.controllers.gestao.equipamentos.inventario.historico;

import com.ossobo.gestaoDepIt.db.models.HistoricoEventos;
import com.ossobo.gestaoDepIt.db.services.HistoricoEventosService;
import com.ossobo.winterfx.anotations.Controller;
import com.ossobo.winterfx.anotations.Inject;
import com.ossobo.winterfx.anotations.PostConstruct;
import com.ossobo.winterfx.router.Rotas;
import com.ossobo.winterfx.router.model.Params;
import com.ossobo.winterfx.view.anotations.RegisterView;
import com.ossobo.winterfx.view.controller.WinterFXController;
import com.ossobo.winterfx.view.floatingwindow.StageForFloatingWindow;
import com.ossobo.winterfx.view.floatingwindow.anotations.FloatingWindow;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador do Histórico de Eventos com auditoria completa.
 * v2.0 - Migração NexusFX → WinterFX + Degrau 1 (record v3.0, id String UUID).
 *
 * Mudanças v1.2 → v2.0:
 * - @Controller(proxy=false) + WinterFXController + @RegisterView + @PostConstruct
 * - Modal NexusFX.dialogs().openModalWithController → @FloatingWindow + rota set-id
 *   (regra de ouro: NUNCA injetar o controller de janela flutuante)
 * - Getters JavaBean → accessors de record · System.out → SLF4J
 * - Combo de tipos: 9 → 12 tipos (OBS-H3 — alinhado ao model v3.0)
 */
@Controller(proxy = false)
@RegisterView(
        id = "historicoEventosList",                                    // ⚠️ CONFIRMAR id usado pela navegação
        fxml = "/META-INF/gestaoDepIt/fxmls/historico/lista.fxml",      // ⚠️ CONFIRMAR caminho real do FXML
        title = "Histórico de Eventos",
        width = 1200,
        height = 800,
        centered = true
)
public class HistoricoEventosListController implements WinterFXController {

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoricoEventosListController.class);
    private static final DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @Inject
    private HistoricoEventosService historicoService;

    /** Janela flutuante de detalhes — dados via rota (padrão WinterFX). */
    @FloatingWindow(
            viewId = "detalhesHistoricoevento",
            singleton = false
    )
    private StageForFloatingWindow janelaDetalhes;

    // Filtros
    @FXML private TextField filtroPesquisaField, filtroIpField;
    @FXML private ComboBox<String> filtroTipoEventoCombo, filtroEntidadeCombo, filtroUsuarioCombo;
    @FXML private DatePicker filtroDataInicio, filtroDataFim;

    // Botões filtro
    @FXML private Button aplicarFiltrosButton, limparFiltrosButton;

    // Tabela
    @FXML private TableView<HistoricoEventos> eventosTable;
    @FXML private TableColumn<HistoricoEventos, String> colId, colDataHora, colTipoEvento, colEntidade;
    @FXML private TableColumn<HistoricoEventos, String> colSkuProduto, colFuncionario, colDescricao;
    @FXML private TableColumn<HistoricoEventos, String> colUsuario, colIpOrigem;
    @FXML private TableColumn<HistoricoEventos, Void> colDetalhes;

    // Paginação
    @FXML private Button btnPrimeira, btnAnterior, btnProxima, btnUltima;
    @FXML private Label labelPagina;
    @FXML private ComboBox<Integer> comboItensPorPagina;

    // Status e estatísticas
    @FXML private Label statusLabel, estatisticasLabel, filtrosAtivosLabel;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private Button relatorioButton, exportarButton;

    private final ObservableList<HistoricoEventos> dados = FXCollections.observableArrayList();
    private int paginaAtual = 1;
    private int itensPorPagina = 30;
    private int totalRegistros = 0;
    private boolean filtroManual = false;

    // Pendências de pré-filtro (antes do FXML injetar)
    private String filtrosPendentesSku = null;
    private String filtrosPendentesFuncId = null;

    @PostConstruct
    public void init() {
        LOGGER.info("🔄 HistoricoEventosListController pronto");
        configurarTabela();
        configurarFiltros();
        configurarPaginacao();

        if (filtrosPendentesSku != null || filtrosPendentesFuncId != null) {
            aplicarFiltrosPendentes();
        } else {
            carregarDados();
        }
    }

    // ===== CONFIGURAÇÕES INICIAIS =====

    private void configurarTabela() {
        colId.setCellValueFactory(c -> new SimpleStringProperty(abreviarId(c.getValue().id())));
        colDataHora.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().createdAt() != null ? c.getValue().createdAt().format(DATE_TIME_FMT) : "-"));
        colTipoEvento.setCellValueFactory(c -> new SimpleStringProperty(nvl(c.getValue().tipoEvento())));
        colEntidade.setCellValueFactory(c -> new SimpleStringProperty("-"));
        colSkuProduto.setCellValueFactory(c -> new SimpleStringProperty(nvl(c.getValue().skuProduto())));
        colFuncionario.setCellValueFactory(c -> new SimpleStringProperty(nvl(c.getValue().funcionarioId())));
        colDescricao.setCellValueFactory(c -> new SimpleStringProperty(
                truncar(nvl(c.getValue().getDescricaoEvento()), 80)));
        colUsuario.setCellValueFactory(c -> new SimpleStringProperty(nvl(c.getValue().getUsuarioInfo())));
        colIpOrigem.setCellValueFactory(c -> new SimpleStringProperty("-"));

        colDetalhes.setCellFactory(col -> new TableCell<>() {
            private final Button btnVer = new Button("🔍");
            {
                btnVer.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
                btnVer.setOnAction(e -> {
                    HistoricoEventos ev = getTableView().getItems().get(getIndex());
                    abrirDetalhes(ev);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnVer);
            }
        });

        eventosTable.setItems(dados);
    }

    /** 12 tipos — fonte da verdade: HistoricoEventos.TIPOS_VALIDOS (OBS-H3). */
    private void configurarFiltros() {
        filtroTipoEventoCombo.getItems().setAll(HistoricoEventos.TIPOS_VALIDOS);
    }

    private void configurarPaginacao() {
        comboItensPorPagina.getItems().setAll(15, 30, 50, 100);
        comboItensPorPagina.setValue(30);
        comboItensPorPagina.setOnAction(e -> {
            itensPorPagina = comboItensPorPagina.getValue();
            paginaAtual = 1;
            carregarDados();
        });
    }

    // ===== PRÉ-FILTROS (com retry para FXML não injetado) =====

    /**
     * Define filtros iniciais e força carregamento.
     * Suporta chamada antes do FXML estar pronto.
     */
    public void setFiltrosIniciais(String sku, String funcionarioId) {
        LOGGER.info("🎯 setFiltrosIniciais - SKU={} | Func={}", sku, funcionarioId);
        filtroManual = true;
        this.filtrosPendentesSku = sku;
        this.filtrosPendentesFuncId = funcionarioId;

        if (filtroPesquisaField == null) {
            LOGGER.info("⏳ FXML ainda não injetado — agendando para depois do @PostConstruct");
            return;
        }
        aplicarFiltrosPendentes();
    }

    private void aplicarFiltrosPendentes() {
        if (filtroPesquisaField == null) {
            LOGGER.error("❌ filtroPesquisaField ainda é null — abortando");
            return;
        }
        StringBuilder sb = new StringBuilder();
        if (filtrosPendentesSku != null && !filtrosPendentesSku.isEmpty()) {
            sb.append(filtrosPendentesSku.trim());
        }
        if (filtrosPendentesFuncId != null && !filtrosPendentesFuncId.isEmpty()) {
            if (sb.length() > 0) sb.append(" | ");
            sb.append(filtrosPendentesFuncId.trim());
        }
        filtroPesquisaField.setText(sb.toString());
        LOGGER.info("✅ Filtros pendentes aplicados: {}", sb);

        filtrosPendentesSku = null;
        filtrosPendentesFuncId = null;
        paginaAtual = 1;
        carregarDados();
    }

    // ===== CARREGAMENTO DE DADOS =====

    private void carregarDados() {
        showProgress(true);
        statusLabel.setText("Carregando...");

        final String pesquisa = valorOuNull(filtroPesquisaField.getText());
        final String tipo = filtroTipoEventoCombo.getValue();
        final LocalDate inicio = filtroDataInicio.getValue();
        final LocalDate fim = filtroDataFim.getValue();
        final int pagina = paginaAtual;
        final int tamanho = itensPorPagina;

        // Extrai múltiplos termos (separados por "|")
        final String[] termos = pesquisa != null ? pesquisa.split("\\|") : new String[0];
        final String sku = termos.length > 0 ? termos[0].trim() : null;
        final String funcId = termos.length > 1 ? termos[1].trim() : null;

        LOGGER.debug("🔍 carregarDados - SKU={} | FuncId={} | Tipo={}", sku, funcId, tipo);

        new Thread(() -> {
            try {
                List<HistoricoEventos> lista = new ArrayList<>();

                // Estratégia mantida: buscar tudo e filtrar em memória
                // (backlog: substituir por com-filtros do service quando auditado)
                List<HistoricoEventos> todos = historicoService.listarTodos(1, 10000);
                LOGGER.debug("📦 Total de eventos no banco: {}", todos.size());

                for (HistoricoEventos ev : todos) {
                    boolean incluir = true;

                    if (sku != null && !sku.isEmpty()) {
                        if (ev.skuProduto() == null || !ev.skuProduto().equalsIgnoreCase(sku)) {
                            incluir = false;
                        }
                    }
                    if (funcId != null && !funcId.isEmpty()) {
                        if (ev.funcionarioId() == null || !ev.funcionarioId().equalsIgnoreCase(funcId)) {
                            if (sku == null || sku.isEmpty() ||
                                    ev.skuProduto() == null || !ev.skuProduto().equalsIgnoreCase(sku)) {
                                incluir = false;
                            }
                        } else {
                            incluir = true;
                        }
                    }
                    if (tipo != null && !tipo.isEmpty()) {
                        if (ev.tipoEvento() == null || !ev.tipoEvento().equalsIgnoreCase(tipo)) {
                            incluir = false;
                        }
                    }
                    if (inicio != null && ev.createdAt() != null && ev.createdAt().toLocalDate().isBefore(inicio)) {
                        incluir = false;
                    }
                    if (fim != null && ev.createdAt() != null && ev.createdAt().toLocalDate().isAfter(fim)) {
                        incluir = false;
                    }

                    if (incluir) {
                        lista.add(ev);
                    }
                }

                // Ordena por data decrescente
                lista.sort((a, b) -> {
                    if (a.createdAt() == null && b.createdAt() == null) return 0;
                    if (a.createdAt() == null) return 1;
                    if (b.createdAt() == null) return -1;
                    return b.createdAt().compareTo(a.createdAt());
                });

                int total = lista.size();
                LOGGER.debug("✅ Após filtros: {} eventos", total);

                int fromIndex = Math.min((pagina - 1) * tamanho, lista.size());
                int toIndex = Math.min(fromIndex + tamanho, lista.size());
                List<HistoricoEventos> paginaLista = new ArrayList<>(lista.subList(fromIndex, toIndex));

                final List<HistoricoEventos> finalLista = paginaLista;
                final int finalTotal = total;

                Platform.runLater(() -> {
                    dados.setAll(finalLista);
                    totalRegistros = finalTotal;
                    int totalPaginas = (int) Math.ceil((double) finalTotal / Math.max(1, tamanho));
                    labelPagina.setText("Página " + pagina + " de " + Math.max(1, totalPaginas));
                    estatisticasLabel.setText("Total: " + finalTotal + " eventos encontrados");
                    atualizarLabelFiltrosAtivos();
                    statusLabel.setText("Pronto");
                    showProgress(false);
                });
            } catch (Exception e) {
                LOGGER.error("❌ Erro ao carregar dados", e);
                Platform.runLater(() -> {
                    statusLabel.setText("Erro");
                    showProgress(false);
                });
            }
        }, "Historico-List-Load").start();
    }

    // ===== NAVEGAÇÃO =====

    /** Janela flutuante via @FloatingWindow + rota (padrão WinterFX). */
    private void abrirDetalhes(HistoricoEventos ev) {
        Rotas.put("historico-evento-detalhes/set-id", Params.with("id", ev.id()));
        janelaDetalhes.show();
    }

    // ===== HANDLERS FXML =====

    @FXML private void handleAplicarFiltros() {
        paginaAtual = 1;
        carregarDados();
    }

    @FXML private void handleLimparFiltros() {
        filtroPesquisaField.clear();
        filtroTipoEventoCombo.setValue(null);
        if (filtroEntidadeCombo != null) filtroEntidadeCombo.setValue(null);
        if (filtroUsuarioCombo != null) filtroUsuarioCombo.setValue(null);
        filtroDataInicio.setValue(null);
        filtroDataFim.setValue(null);
        filtroIpField.clear();
        filtroManual = false;
        paginaAtual = 1;
        carregarDados();
    }

    @FXML private void handleAtualizar() { carregarDados(); }

    @FXML private void handleGerarRelatorio() {
        statusLabel.setText("ℹ️ Relatório em desenvolvimento.");
    }

    @FXML private void handleExportarCSV() {
        statusLabel.setText("ℹ️ Exportação CSV em desenvolvimento.");
    }

    @FXML private void handleFechar() {
        ((Stage) eventosTable.getScene().getWindow()).close();
    }

    @FXML private void handlePrimeiraPagina() { paginaAtual = 1; carregarDados(); }
    @FXML private void handlePaginaAnterior() { if (paginaAtual > 1) { paginaAtual--; carregarDados(); } }
    @FXML private void handleProximaPagina() {
        int totalPaginas = (int) Math.ceil((double) totalRegistros / Math.max(1, itensPorPagina));
        if (paginaAtual < totalPaginas) { paginaAtual++; carregarDados(); }
    }
    @FXML private void handleUltimaPagina() {
        int totalPaginas = (int) Math.ceil((double) totalRegistros / Math.max(1, itensPorPagina));
        paginaAtual = Math.max(1, totalPaginas);
        carregarDados();
    }

    // ===== UTILITÁRIOS =====

    private void showProgress(boolean show) {
        if (progressIndicator != null) progressIndicator.setVisible(show);
        if (aplicarFiltrosButton != null) aplicarFiltrosButton.setDisable(show);
        if (limparFiltrosButton != null) limparFiltrosButton.setDisable(show);
        if (exportarButton != null) exportarButton.setDisable(show);
        if (relatorioButton != null) relatorioButton.setDisable(show);
    }

    private void atualizarLabelFiltrosAtivos() {
        int ativos = 0;
        if (filtroTipoEventoCombo.getValue() != null) ativos++;
        if (filtroDataInicio.getValue() != null || filtroDataFim.getValue() != null) ativos++;
        if (filtroPesquisaField.getText() != null && !filtroPesquisaField.getText().isEmpty()) ativos++;
        if (filtroManual) ativos++;

        if (filtrosAtivosLabel != null) {
            filtrosAtivosLabel.setText(ativos > 0 ? "(" + ativos + " filtro(s) ativo(s))" : "");
            filtrosAtivosLabel.setStyle(ativos > 0
                    ? "-fx-text-fill: #8b5cf6; -fx-font-size: 12px; -fx-font-style: italic;"
                    : "-fx-text-fill: #6b7280;");
        }
    }

    /** UUID é longo — exibe os 8 primeiros caracteres na tabela. */
    private String abreviarId(String id) {
        return (id != null && id.length() > 8) ? id.substring(0, 8) + "…" : nvl(id);
    }

    private String valorOuNull(String s) {
        return (s != null && !s.trim().isEmpty()) ? s.trim() : null;
    }

    private String nvl(String s) { return (s != null && !s.isEmpty()) ? s : "-"; }

    private String truncar(String s, int max) {
        return (s != null && s.length() > max) ? s.substring(0, max) + "..." : s;
    }
}