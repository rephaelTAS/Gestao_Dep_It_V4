/*
 * GIATableBuilder v1.2
 *
 * Constrói as 7 tabelas do dashboard GIA com cellValueFactory configurado.
 * Duplo clique em todas as tabelas com ações contextuais.
 *
 * v1.2: Adicionado — duplo clique em Estoque Alerta (abre detalhes do produto)
 *       Adicionado — duplo clique em Itens Parados (abre detalhes do equipamento)
 *       Adicionado — duplo clique em Custo-Benefício (abre detalhes do produto)
 *       Adicionado — duplo clique em Críticos (abre detalhes do equipamento)
 * v1.1: Adicionado — cellValueFactory para todas as tabelas + ações nos Críticos
 */
package com.ossobo.gestaoDepIt.controllers.gestao.gia.functions;

import com.ossobo.gestaoDepIt.controllers.gestao.equipamentos.inventario.InventarioControllerDetails;
import com.ossobo.gestaoDepIt.controllers.gestao.estoque.catalugoproduto.CatalogoProdutoDetailController;
import com.ossobo.gestaoDepIt.controllers.gestao.gia.analises.*;
import com.ossobo.gestaoDepIt.db.models.CatalogoProdutos;
import com.ossobo.gestaoDepIt.db.models.Funcionarios;
import com.ossobo.gestaoDepIt.db.models.InventarioEquipamentos;
import com.ossobo.nexusfx.NexusFX;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class GIATableBuilder {

    private static final Logger logger = LoggerFactory.getLogger(GIATableBuilder.class);

    private final TableView<InventarioEquipamentos> tblInventario;
    private final TableView<GIADTOs.LocalizacaoStats> tblLocalizacao;
    private final TableView<GIADTOs.CriticoItem> tabelaCriticos;
    private final TableView<GIADTOs.RecomendacaoItem> tabelaRecomendacoes;
    private final TableView<GIADTOs.CustoBeneficioItem> tabelaCustoBeneficio;
    private final TableView<GIADTOs.EstoqueAlertaItem> tabelaEstoqueAlertas;
    private final TableView<GIADTOs.ItemParado> tabelaItensParados;
    private final Label lblTotalItens;

    private CriticidadeCalculator criticidadeCalculator;
    private BalanceamentoAnalyzer balanceamentoAnalyzer;
    private ProjecaoFinanceiraService projecaoFinanceiraService;
    private EstoqueInteligenteService estoqueInteligenteService;
    private CustoBeneficioAnalyzer custoBeneficioAnalyzer;

    private GIADialogHelper dialogHelper;
    private List<CatalogoProdutos> catalogosCache;
    private List<InventarioEquipamentos> equipamentosCache;

    public GIATableBuilder(TableView<InventarioEquipamentos> tblInventario,
                           TableView<GIADTOs.LocalizacaoStats> tblLocalizacao,
                           TableView<GIADTOs.CriticoItem> tabelaCriticos,
                           TableView<GIADTOs.RecomendacaoItem> tabelaRecomendacoes,
                           TableView<GIADTOs.CustoBeneficioItem> tabelaCustoBeneficio,
                           TableView<GIADTOs.EstoqueAlertaItem> tabelaEstoqueAlertas,
                           TableView<GIADTOs.ItemParado> tabelaItensParados,
                           Label lblTotalItens) {
        this.tblInventario = tblInventario;
        this.tblLocalizacao = tblLocalizacao;
        this.tabelaCriticos = tabelaCriticos;
        this.tabelaRecomendacoes = tabelaRecomendacoes;
        this.tabelaCustoBeneficio = tabelaCustoBeneficio;
        this.tabelaEstoqueAlertas = tabelaEstoqueAlertas;
        this.tabelaItensParados = tabelaItensParados;
        this.lblTotalItens = lblTotalItens;
    }

    public void setServices(CriticidadeCalculator criticidadeCalculator,
                            BalanceamentoAnalyzer balanceamentoAnalyzer,
                            ProjecaoFinanceiraService projecaoFinanceiraService,
                            EstoqueInteligenteService estoqueInteligenteService,
                            CustoBeneficioAnalyzer custoBeneficioAnalyzer) {
        this.criticidadeCalculator = criticidadeCalculator;
        this.balanceamentoAnalyzer = balanceamentoAnalyzer;
        this.projecaoFinanceiraService = projecaoFinanceiraService;
        this.estoqueInteligenteService = estoqueInteligenteService;
        this.custoBeneficioAnalyzer = custoBeneficioAnalyzer;
    }

    public void construirTodas(List<InventarioEquipamentos> equipamentos,
                               List<CatalogoProdutos> catalogos,
                               List<Funcionarios> funcionarios) {
        this.catalogosCache = catalogos;
        this.equipamentosCache = equipamentos;
        this.dialogHelper = new GIADialogHelper(catalogosCache, equipamentosCache);

        configurarTodasAsColunas();
        configurarDuploCliqueLocalizacao();
        configurarDuploCliqueRecomendacoes();
        configurarDuploCliqueEstoque();
        configurarDuploCliqueItensParados();
        configurarDuploCliqueCustoBeneficio();
        configurarDuploCliqueCriticos();

        construirTabelaInventario(equipamentos);
        construirTabelaLocalizacao(equipamentos, catalogos);
        construirTabelaCriticos(equipamentos, catalogos);
        construirTabelaRecomendacoes(equipamentos, catalogos);
        construirTabelaCustoBeneficio(catalogos, equipamentos);
        construirTabelaEstoque(catalogos);
        construirTabelaItensParados(equipamentos, catalogos);
    }

    // =========================================================================
    // CONFIGURAÇÃO DE COLUNAS
    // =========================================================================

    @SuppressWarnings("unchecked")
    private void configurarTodasAsColunas() {
        configurarColunasLocalizacao();
        configurarColunasCriticos();
        configurarColunasRecomendacoes();
        configurarColunasCustoBeneficio();
        configurarColunasEstoque();
        configurarColunasItensParados();
    }

    @SuppressWarnings("unchecked")
    private void configurarColunasLocalizacao() {
        TableColumn<GIADTOs.LocalizacaoStats, String> col0 = (TableColumn<GIADTOs.LocalizacaoStats, String>) tblLocalizacao.getColumns().get(0);
        col0.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getLocalizacao()));
        TableColumn<GIADTOs.LocalizacaoStats, String> col1 = (TableColumn<GIADTOs.LocalizacaoStats, String>) tblLocalizacao.getColumns().get(1);
        col1.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getQuantidade()));
        if (tblLocalizacao.getColumns().size() > 2) {
            TableColumn<GIADTOs.LocalizacaoStats, String> col2 = (TableColumn<GIADTOs.LocalizacaoStats, String>) tblLocalizacao.getColumns().get(2);
            col2.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getValorFormatado()));
        }
    }

    @SuppressWarnings("unchecked")
    private void configurarColunasCriticos() {
        TableColumn<GIADTOs.CriticoItem, String> col0 = (TableColumn<GIADTOs.CriticoItem, String>) tabelaCriticos.getColumns().get(0);
        col0.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSku()));
        TableColumn<GIADTOs.CriticoItem, String> col1 = (TableColumn<GIADTOs.CriticoItem, String>) tabelaCriticos.getColumns().get(1);
        col1.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNome()));
        TableColumn<GIADTOs.CriticoItem, String> col2 = (TableColumn<GIADTOs.CriticoItem, String>) tabelaCriticos.getColumns().get(2);
        col2.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getIdade()));
        TableColumn<GIADTOs.CriticoItem, String> col3 = (TableColumn<GIADTOs.CriticoItem, String>) tabelaCriticos.getColumns().get(3);
        col3.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus()));
        TableColumn<GIADTOs.CriticoItem, String> col4 = (TableColumn<GIADTOs.CriticoItem, String>) tabelaCriticos.getColumns().get(4);
        col4.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRecomendacao()));
        if (tabelaCriticos.getColumns().size() > 5) {
            configurarColunaAcoesCriticos();
        }
    }

    @SuppressWarnings("unchecked")
    private void configurarColunasRecomendacoes() {
        ((TableColumn<GIADTOs.RecomendacaoItem, String>) tabelaRecomendacoes.getColumns().get(0)).setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPrioridade()));
        ((TableColumn<GIADTOs.RecomendacaoItem, String>) tabelaRecomendacoes.getColumns().get(1)).setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAcao()));
        ((TableColumn<GIADTOs.RecomendacaoItem, String>) tabelaRecomendacoes.getColumns().get(2)).setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getItem()));
        ((TableColumn<GIADTOs.RecomendacaoItem, String>) tabelaRecomendacoes.getColumns().get(3)).setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getImpacto()));
        ((TableColumn<GIADTOs.RecomendacaoItem, String>) tabelaRecomendacoes.getColumns().get(4)).setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPrazo()));
    }

    @SuppressWarnings("unchecked")
    private void configurarColunasCustoBeneficio() {
        ((TableColumn<GIADTOs.CustoBeneficioItem, String>) tabelaCustoBeneficio.getColumns().get(0)).setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSku()));
        ((TableColumn<GIADTOs.CustoBeneficioItem, String>) tabelaCustoBeneficio.getColumns().get(1)).setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNome()));
        ((TableColumn<GIADTOs.CustoBeneficioItem, String>) tabelaCustoBeneficio.getColumns().get(2)).setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProcessador()));
        ((TableColumn<GIADTOs.CustoBeneficioItem, String>) tabelaCustoBeneficio.getColumns().get(3)).setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRam()));
        ((TableColumn<GIADTOs.CustoBeneficioItem, String>) tabelaCustoBeneficio.getColumns().get(4)).setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPreco()));
        ((TableColumn<GIADTOs.CustoBeneficioItem, String>) tabelaCustoBeneficio.getColumns().get(5)).setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCustoAno()));
        ((TableColumn<GIADTOs.CustoBeneficioItem, String>) tabelaCustoBeneficio.getColumns().get(6)).setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPontuacao()));
    }

    @SuppressWarnings("unchecked")
    private void configurarColunasEstoque() {
        ((TableColumn<GIADTOs.EstoqueAlertaItem, String>) tabelaEstoqueAlertas.getColumns().get(0)).setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSku()));
        ((TableColumn<GIADTOs.EstoqueAlertaItem, String>) tabelaEstoqueAlertas.getColumns().get(1)).setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNome()));
        ((TableColumn<GIADTOs.EstoqueAlertaItem, String>) tabelaEstoqueAlertas.getColumns().get(2)).setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEstoqueAtual()));
        ((TableColumn<GIADTOs.EstoqueAlertaItem, String>) tabelaEstoqueAlertas.getColumns().get(3)).setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEstoqueMinimo()));
        ((TableColumn<GIADTOs.EstoqueAlertaItem, String>) tabelaEstoqueAlertas.getColumns().get(4)).setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getExcesso()));
        ((TableColumn<GIADTOs.EstoqueAlertaItem, String>) tabelaEstoqueAlertas.getColumns().get(5)).setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSugestao()));
    }

    @SuppressWarnings("unchecked")
    private void configurarColunasItensParados() {
        ((TableColumn<GIADTOs.ItemParado, String>) tabelaItensParados.getColumns().get(0)).setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSku()));
        ((TableColumn<GIADTOs.ItemParado, String>) tabelaItensParados.getColumns().get(1)).setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNome()));
        ((TableColumn<GIADTOs.ItemParado, String>) tabelaItensParados.getColumns().get(2)).setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDiasParado()));
        ((TableColumn<GIADTOs.ItemParado, String>) tabelaItensParados.getColumns().get(3)).setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getValorImobilizado()));
        ((TableColumn<GIADTOs.ItemParado, String>) tabelaItensParados.getColumns().get(4)).setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAcaoSugerida()));
    }

    @SuppressWarnings("unchecked")
    private void configurarColunaAcoesCriticos() {
        TableColumn<GIADTOs.CriticoItem, Void> colAcoes = (TableColumn<GIADTOs.CriticoItem, Void>) tabelaCriticos.getColumns().get(5);
        colAcoes.setCellFactory(param -> new TableCell<>() {
            private final Button btnEquip = new Button("🔍");
            private final Button btnProd = new Button("📦");
            private final HBox pane = new HBox(5, btnEquip, btnProd);
            {
                btnEquip.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 11px;");
                btnProd.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 11px;");
                btnEquip.setTooltip(new Tooltip("Ver equipamento"));
                btnProd.setTooltip(new Tooltip("Ver produto"));
                btnEquip.setOnAction(e -> {
                    GIADTOs.CriticoItem item = getTableView().getItems().get(getIndex());
                    abrirDetalhesEquipamento(item.getSku());
                });
                btnProd.setOnAction(e -> {
                    GIADTOs.CriticoItem item = getTableView().getItems().get(getIndex());
                    abrirDetalhesProduto(item.getSku());
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) { super.updateItem(item, empty); setGraphic(empty ? null : pane); }
        });
    }

    // =========================================================================
    // DUPLO CLIQUE: LOCALIZAÇÃO
    // =========================================================================

    private void configurarDuploCliqueLocalizacao() {
        tblLocalizacao.setRowFactory(tv -> {
            TableRow<GIADTOs.LocalizacaoStats> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    abrirDetalhesLocalizacao(row.getItem().getLocalizacao());
                }
            });
            return row;
        });
    }

    private void abrirDetalhesLocalizacao(String localizacao) {
        if (dialogHelper == null) return;
        List<InventarioEquipamentos> filtrados = equipamentosCache.stream()
                .filter(e -> localizacao.equals(e.getLocalizacao())).collect(Collectors.toList());
        dialogHelper.mostrarDialogoEquipamentos("Equipamentos em: " + localizacao,
                "Distribuição por Localização — Detalhes",
                String.format("📍 %s — %d equipamento(s)", localizacao, filtrados.size()),
                filtrados, true, tblLocalizacao.getScene().getWindow());
    }

    // =========================================================================
    // DUPLO CLIQUE: RECOMENDAÇÕES
    // =========================================================================

    private void configurarDuploCliqueRecomendacoes() {
        tabelaRecomendacoes.setRowFactory(tv -> {
            TableRow<GIADTOs.RecomendacaoItem> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    abrirDetalhesRecomendacao(row.getItem());
                }
            });
            return row;
        });
    }

    private void abrirDetalhesRecomendacao(GIADTOs.RecomendacaoItem item) {
        if (dialogHelper == null) return;
        List<InventarioEquipamentos> filtrados;
        String titulo, resumo;

        switch (item.getAcao()) {
            case "Substituir":
                filtrados = equipamentosCache.stream().filter(e -> "CRITICO".equals(e.getCondicao())).collect(Collectors.toList());
                titulo = "Equipamentos Críticos para Substituição";
                resumo = String.format("⚠ %d equipamento(s) crítico(s)", filtrados.size());
                break;
            case "Verificar":
                filtrados = equipamentosCache.stream().filter(e -> e.getDataUltimaVerificacao() == null ||
                        ChronoUnit.MONTHS.between(e.getDataUltimaVerificacao(), LocalDate.now()) > 6).collect(Collectors.toList());
                titulo = "Equipamentos Pendentes de Verificação";
                resumo = String.format("🟡 %d equipamento(s) sem verificação", filtrados.size());
                break;
            case "Comprar":
                mostrarDialogoEstoqueBaixo(item);
                return;
            case "Planejar":
                filtrados = equipamentosCache.stream().filter(e -> e.getDataAquisicao() != null &&
                        ChronoUnit.YEARS.between(e.getDataAquisicao(), LocalDate.now()) > 4).collect(Collectors.toList());
                titulo = "Equipamentos para Planejamento de Substituição";
                resumo = String.format("🟢 %d equipamento(s) com +4 anos", filtrados.size());
                break;
            default: return;
        }

        dialogHelper.mostrarDialogoEquipamentos(titulo, "Recomendação: " + item.getAcao(), resumo, filtrados, true,
                tabelaRecomendacoes.getScene().getWindow());
    }

    private void mostrarDialogoEstoqueBaixo(GIADTOs.RecomendacaoItem item) {
        List<CatalogoProdutos> estoqueBaixo = catalogosCache.stream()
                .filter(CatalogoProdutos::isEstoqueBaixo).collect(Collectors.toList());

        TableView<CatalogoProdutos> tabela = new TableView<>();
        TableColumn<CatalogoProdutos, String> colSku = new TableColumn<>("SKU");
        colSku.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSku()));
        colSku.setPrefWidth(100);
        TableColumn<CatalogoProdutos, String> colNome = new TableColumn<>("Produto");
        colNome.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNomeCompleto()));
        colNome.setPrefWidth(250);
        TableColumn<CatalogoProdutos, String> colEstoque = new TableColumn<>("Estoque");
        colEstoque.setCellValueFactory(cellData -> new SimpleStringProperty(
                String.valueOf(cellData.getValue().getTotalRecebido() != null ? cellData.getValue().getTotalRecebido() : 0)));
        colEstoque.setPrefWidth(80);
        TableColumn<CatalogoProdutos, String> colPreco = new TableColumn<>("Preço");
        colPreco.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getPrecoUnitario() != null ? "€ " + cellData.getValue().getPrecoUnitario() : "€ 0"));
        colPreco.setPrefWidth(100);
        tabela.getColumns().addAll(colSku, colNome, colEstoque, colPreco);
        tabela.getItems().addAll(estoqueBaixo);
        tabela.setPrefHeight(300);

        Label lblResumo = new Label(String.format("📦 %d produto(s) com estoque baixo", estoqueBaixo.size()));
        lblResumo.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10 0 10 0;");
        VBox content = new VBox(10, lblResumo, tabela);
        content.setPadding(new Insets(10));

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Estoque Baixo");
        dialog.setHeaderText("Produtos que precisam de reposição");
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setPrefSize(600, 400);
        dialog.initOwner(tabelaRecomendacoes.getScene().getWindow());
        dialog.showAndWait();
    }

    // =========================================================================
    // DUPLO CLIQUE: ESTOQUE INTELIGENTE (abre detalhes do produto)
    // =========================================================================

    private void configurarDuploCliqueEstoque() {
        tabelaEstoqueAlertas.setRowFactory(tv -> {
            TableRow<GIADTOs.EstoqueAlertaItem> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    abrirDetalhesProduto(row.getItem().getSku());
                }
            });
            return row;
        });
    }

    // =========================================================================
    // DUPLO CLIQUE: ITENS PARADOS (abre detalhes do equipamento)
    // =========================================================================

    private void configurarDuploCliqueItensParados() {
        tabelaItensParados.setRowFactory(tv -> {
            TableRow<GIADTOs.ItemParado> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    abrirDetalhesEquipamento(row.getItem().getSku());
                }
            });
            return row;
        });
    }

    // =========================================================================
    // DUPLO CLIQUE: CUSTO-BENEFÍCIO (abre detalhes do produto)
    // =========================================================================

    private void configurarDuploCliqueCustoBeneficio() {
        tabelaCustoBeneficio.setRowFactory(tv -> {
            TableRow<GIADTOs.CustoBeneficioItem> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    abrirDetalhesProduto(row.getItem().getSku());
                }
            });
            return row;
        });
    }

    // =========================================================================
    // DUPLO CLIQUE: CRÍTICOS (abre detalhes do equipamento)
    // =========================================================================

    private void configurarDuploCliqueCriticos() {
        tabelaCriticos.setRowFactory(tv -> {
            TableRow<GIADTOs.CriticoItem> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    abrirDetalhesEquipamento(row.getItem().getSku());
                }
            });
            return row;
        });
    }

    // =========================================================================
    // MÉTODOS DE ABERTURA DE DETALHES
    // =========================================================================

    private void abrirDetalhesEquipamento(String sku) {
        try {
            InventarioEquipamentos eq = equipamentosCache.stream()
                    .filter(e -> e.getSkuProduto().equals(sku)).findFirst().orElse(null);
            if (eq == null) return;
            Window owner = tblInventario.getScene().getWindow();
            NexusFX.dialogs().openModalWithController("inventarioDetails", "Detalhes do Equipamento", owner,
                    (Object controller) -> {
                        if (controller instanceof InventarioControllerDetails details) details.carregarEquipamento(eq.getId());
                    });
        } catch (Exception e) { logger.error("Erro ao abrir detalhes do equipamento", e); }
    }

    private void abrirDetalhesProduto(String sku) {
        try {
            Window owner = tblInventario.getScene().getWindow();
            NexusFX.dialogs().openModalWithController("detalhe_produto", "Detalhes do Produto", owner,
                    (Object controller) -> {
                        if (controller instanceof CatalogoProdutoDetailController details) details.setProdutoSku(sku);
                    });
        } catch (Exception e) { logger.error("Erro ao abrir detalhes do produto", e); }
    }

    // =========================================================================
    // TABELAS
    // =========================================================================

    private void construirTabelaInventario(List<InventarioEquipamentos> equipamentos) {
        tblInventario.setItems(FXCollections.observableArrayList(equipamentos));
        lblTotalItens.setText(String.valueOf(equipamentos.size()));
    }

    private void construirTabelaLocalizacao(List<InventarioEquipamentos> equipamentos, List<CatalogoProdutos> catalogos) {
        Map<String, Long> porLoc = equipamentos.stream().filter(e -> e.getLocalizacao() != null)
                .collect(Collectors.groupingBy(InventarioEquipamentos::getLocalizacao, Collectors.counting()));
        ObservableList<GIADTOs.LocalizacaoStats> dados = FXCollections.observableArrayList();
        porLoc.forEach((loc, qtd) -> {
            final BigDecimal[] valor = {BigDecimal.ZERO};
            for (InventarioEquipamentos ie : equipamentos) {
                if (loc.equals(ie.getLocalizacao())) {
                    catalogos.stream().filter(c -> c.getSku().equals(ie.getSkuProduto())).findFirst()
                            .ifPresent(c -> { if (c.getPrecoUnitario() != null) valor[0] = valor[0].add(c.getPrecoUnitario()); });
                }
            }
            dados.add(new GIADTOs.LocalizacaoStats(loc, qtd.intValue(), valor[0]));
        });
        tblLocalizacao.setItems(dados);
    }

    private void construirTabelaCriticos(List<InventarioEquipamentos> equipamentos, List<CatalogoProdutos> catalogos) {
        ObservableList<GIADTOs.CriticoItem> dados = FXCollections.observableArrayList();
        equipamentos.stream()
                .filter(e -> "CRITICO".equals(e.getCondicao()) || "REGULAR".equals(e.getCondicao()))
                .sorted((a, b) -> "CRITICO".equals(a.getCondicao()) ? -1 : 1).limit(10)
                .forEach(eq -> {
                    String nome = catalogos.stream().filter(c -> c.getSku().equals(eq.getSkuProduto())).findFirst()
                            .map(c -> c.getMarca() + " " + c.getModelo()).orElse(eq.getSkuProduto());
                    long idade = eq.getDataAquisicao() != null ? ChronoUnit.YEARS.between(eq.getDataAquisicao(), LocalDate.now()) : 0;
                    String rec;
                    if (criticidadeCalculator != null) {
                        rec = criticidadeCalculator.calcular(eq, "GERAL").getRecomendacao();
                    } else {
                        rec = "CRITICO".equals(eq.getCondicao()) ? "Substituir imediatamente" : "Avaliar manutenção preventiva";
                    }
                    dados.add(new GIADTOs.CriticoItem(eq.getSkuProduto(), nome, idade + " anos", eq.getCondicao(), rec));
                });
        tabelaCriticos.setItems(dados);
    }

    private void construirTabelaRecomendacoes(List<InventarioEquipamentos> equipamentos, List<CatalogoProdutos> catalogos) {
        ObservableList<GIADTOs.RecomendacaoItem> dados = FXCollections.observableArrayList();
        long criticos = equipamentos.stream().filter(e -> "CRITICO".equals(e.getCondicao())).count();
        if (criticos > 0) dados.add(new GIADTOs.RecomendacaoItem("🔴", "Substituir", criticos + " equipamentos críticos", "Risco de falha operacional", "Imediato"));
        long semVerificacao = equipamentos.stream().filter(e -> e.getDataUltimaVerificacao() == null ||
                ChronoUnit.MONTHS.between(e.getDataUltimaVerificacao(), LocalDate.now()) > 6).count();
        if (semVerificacao > 0) dados.add(new GIADTOs.RecomendacaoItem("🟡", "Verificar", semVerificacao + " sem verificação há 6+ meses", "Prevenção de falhas", "30 dias"));
        long estoqueBaixo = catalogos.stream().filter(CatalogoProdutos::isEstoqueBaixo).count();
        if (estoqueBaixo > 0) dados.add(new GIADTOs.RecomendacaoItem("🟠", "Comprar", estoqueBaixo + " itens com estoque baixo", "Evitar ruptura", "15 dias"));
        long velhos = equipamentos.stream().filter(e -> e.getDataAquisicao() != null &&
                ChronoUnit.YEARS.between(e.getDataAquisicao(), LocalDate.now()) > 4).count();
        if (velhos > 0) dados.add(new GIADTOs.RecomendacaoItem("🟢", "Planejar", velhos + " equipamentos com +4 anos", "Iniciar orçamento", "6 meses"));
        if (dados.isEmpty()) dados.add(new GIADTOs.RecomendacaoItem("✅", "Nenhuma", "Tudo em ordem", "Nenhum alerta ativo", "—"));
        tabelaRecomendacoes.setItems(dados);
    }

    private void construirTabelaCustoBeneficio(List<CatalogoProdutos> catalogos, List<InventarioEquipamentos> equipamentos) {
        ObservableList<GIADTOs.CustoBeneficioItem> dados = FXCollections.observableArrayList();
        if (custoBeneficioAnalyzer != null) {
            custoBeneficioAnalyzer.gerarRanking(catalogos, equipamentos).forEach(item ->
                    dados.add(new GIADTOs.CustoBeneficioItem(item.getSku(), item.getNome(), item.getProcessador(), item.getRam(),
                            "€ " + item.getPreco(), "€ " + item.getCustoPorAno(), String.format("%.0f/100", item.getScore()))));
        } else {
            catalogos.stream().filter(c -> c.getPrecoUnitario() != null && c.getPrecoUnitario().compareTo(BigDecimal.ZERO) > 0)
                    .sorted((a, b) -> b.getPrecoUnitario().compareTo(a.getPrecoUnitario())).limit(10)
                    .forEach(c -> dados.add(new GIADTOs.CustoBeneficioItem(c.getSku(), c.getNomeCompleto(),
                            extrairProcessador(c), extrairRAM(c), "€ " + c.getPrecoUnitario(), "N/D", "N/D")));
        }
        tabelaCustoBeneficio.setItems(dados);
    }

    private void construirTabelaEstoque(List<CatalogoProdutos> catalogos) {
        ObservableList<GIADTOs.EstoqueAlertaItem> dados = FXCollections.observableArrayList();
        catalogos.stream().filter(c -> c.getTotalRecebido() != null && c.getTotalRecebido() <= 10)
                .sorted((a, b) -> Integer.compare(a.getTotalRecebido(), b.getTotalRecebido())).limit(15)
                .forEach(c -> {
                    int minimo = c.isEstoqueBaixo() ? 5 : 3;
                    boolean abaixo = c.getTotalRecebido() < minimo;
                    dados.add(new GIADTOs.EstoqueAlertaItem(c.getSku(), c.getNomeCompleto(),
                            String.valueOf(c.getTotalRecebido()), String.valueOf(minimo),
                            abaixo ? "SIM" : "OK", abaixo ? "Comprar " + (minimo - c.getTotalRecebido()) + " un." : "Estoque adequado"));
                });
        tabelaEstoqueAlertas.setItems(dados);
    }

    private void construirTabelaItensParados(List<InventarioEquipamentos> equipamentos, List<CatalogoProdutos> catalogos) {
        ObservableList<GIADTOs.ItemParado> dados = FXCollections.observableArrayList();
        equipamentos.stream()
                .filter(e -> e.getDataUltimaVerificacao() == null || ChronoUnit.MONTHS.between(e.getDataUltimaVerificacao(), LocalDate.now()) > 3)
                .sorted((a, b) -> {
                    long da = a.getDataUltimaVerificacao() != null ? ChronoUnit.DAYS.between(a.getDataUltimaVerificacao(), LocalDate.now()) : 365;
                    long db = b.getDataUltimaVerificacao() != null ? ChronoUnit.DAYS.between(b.getDataUltimaVerificacao(), LocalDate.now()) : 365;
                    return Long.compare(db, da);
                }).limit(10)
                .forEach(e -> {
                    String nome = catalogos.stream().filter(c -> c.getSku().equals(e.getSkuProduto())).findFirst()
                            .map(c -> c.getMarca() + " " + c.getModelo()).orElse(e.getSkuProduto());
                    long dias = e.getDataUltimaVerificacao() != null ? ChronoUnit.DAYS.between(e.getDataUltimaVerificacao(), LocalDate.now()) : 365;
                    BigDecimal valor = catalogos.stream().filter(c -> c.getSku().equals(e.getSkuProduto())).findFirst()
                            .map(CatalogoProdutos::getPrecoUnitario).orElse(BigDecimal.ZERO);
                    String acao = dias > 365 ? "Descartar ou doar" : dias > 180 ? "Avaliar redistribuição" : "Monitorar por mais 30 dias";
                    dados.add(new GIADTOs.ItemParado(e.getSkuProduto(), nome, dias + " dias", "€ " + valor, acao));
                });
        tabelaItensParados.setItems(dados);
    }

    private String extrairProcessador(CatalogoProdutos cp) {
        String json = cp.getCaracteristicasTecnicas();
        if (json != null && json.contains("\"processador\"")) {
            try { String[] p = json.split("\"processador\":\""); if (p.length > 1) return p[1].split("\"")[0]; } catch (Exception ignored) {}
        }
        return "-";
    }

    private String extrairRAM(CatalogoProdutos cp) {
        String json = cp.getCaracteristicasTecnicas();
        if (json != null && json.contains("\"ram\"")) {
            try { String[] p = json.split("\"ram\":\""); if (p.length > 1) return p[1].split("\"")[0]; } catch (Exception ignored) {}
        }
        return "-";
    }
}