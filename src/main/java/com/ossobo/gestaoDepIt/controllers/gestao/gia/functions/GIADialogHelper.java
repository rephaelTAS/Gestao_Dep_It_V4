/*
 * GIADialogHelper v1.0
 *
 * Utilitário para criação de diálogos modais com tabelas de equipamentos.
 * Reaproveitável em todo o módulo GIA — elimina código repetitivo.
 *
 * v1.0: Versão inicial
 *       - criarTabelaEquipamentosComAcoes()
 *       - mostrarDialogoLocalizacao()
 */
package com.ossobo.gestaoDepIt.controllers.gestao.gia.functions;

import com.ossobo.gestaoDepIt.controllers.gestao.equipamentos.inventario.InventarioControllerDetails;
import com.ossobo.gestaoDepIt.controllers.gestao.estoque.catalugoproduto.CatalogoProdutoDetailController;
import com.ossobo.gestaoDepIt.db.models.CatalogoProdutos;
import com.ossobo.gestaoDepIt.db.models.InventarioEquipamentos;
import com.ossobo.nexusfx.NexusFX;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class GIADialogHelper {

    private static final Logger logger = LoggerFactory.getLogger(GIADialogHelper.class);

    private final List<CatalogoProdutos> catalogosCache;
    private final List<InventarioEquipamentos> equipamentosCache;

    public GIADialogHelper(List<CatalogoProdutos> catalogosCache,
                           List<InventarioEquipamentos> equipamentosCache) {
        this.catalogosCache = catalogosCache;
        this.equipamentosCache = equipamentosCache;
    }

    /**
     * Cria uma TableView padrão para equipamentos com coluna de ações.
     * Reaproveitada em: Localização, Recomendações, Críticos, etc.
     *
     * @param equipamentos Lista de equipamentos a exibir
     * @param colunasExtras true para incluir colunas de Nome, Série, Status, Condição, Funcionário
     * @return TableView configurada
     */
    public TableView<InventarioEquipamentos> criarTabelaEquipamentosComAcoes(
            List<InventarioEquipamentos> equipamentos, boolean colunasExtras) {

        TableView<InventarioEquipamentos> tabela = new TableView<>();

        // Coluna SKU (sempre)
        TableColumn<InventarioEquipamentos, String> colSku = new TableColumn<>("SKU");
        colSku.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getSkuProduto()));
        colSku.setPrefWidth(100);
        tabela.getColumns().add(colSku);

        if (colunasExtras) {
            // Coluna Nome (Marca + Modelo)
            TableColumn<InventarioEquipamentos, String> colNome = new TableColumn<>("Equipamento");
            colNome.setCellValueFactory(cellData -> {
                String sku = cellData.getValue().getSkuProduto();
                String nome = catalogosCache.stream()
                        .filter(c -> c.getSku().equals(sku))
                        .findFirst()
                        .map(c -> c.getMarca() + " " + c.getModelo())
                        .orElse(sku);
                return new SimpleStringProperty(nome);
            });
            colNome.setPrefWidth(200);
            tabela.getColumns().add(colNome);

            // Coluna Nº Série
            TableColumn<InventarioEquipamentos, String> colSerie = new TableColumn<>("Nº Série");
            colSerie.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getNumSerie() != null ?
                            cellData.getValue().getNumSerie() : "-"));
            colSerie.setPrefWidth(120);
            tabela.getColumns().add(colSerie);

            // Coluna Status
            TableColumn<InventarioEquipamentos, String> colStatus = new TableColumn<>("Status");
            colStatus.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getStatus() != null ?
                            cellData.getValue().getStatus() : "-"));
            colStatus.setPrefWidth(90);
            tabela.getColumns().add(colStatus);

            // Coluna Condição
            TableColumn<InventarioEquipamentos, String> colCondicao = new TableColumn<>("Condição");
            colCondicao.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getCondicao() != null ?
                            cellData.getValue().getCondicao() : "-"));
            colCondicao.setPrefWidth(90);
            tabela.getColumns().add(colCondicao);

            // Coluna Funcionário
            TableColumn<InventarioEquipamentos, String> colFuncionario = new TableColumn<>("Funcionário");
            colFuncionario.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getFuncionarioId() != null ?
                            cellData.getValue().getFuncionarioId() : "Não atribuído"));
            colFuncionario.setPrefWidth(120);
            tabela.getColumns().add(colFuncionario);
        }

        // Coluna Ações (sempre)
        TableColumn<InventarioEquipamentos, Void> colAcoes = criarColunaAcoes();
        tabela.getColumns().add(colAcoes);

        tabela.getItems().addAll(equipamentos);
        tabela.setPrefHeight(400);

        return tabela;
    }

    /**
     * Cria a coluna de ações padrão (🔍 Equip. + 📦 Prod.)
     */
    private TableColumn<InventarioEquipamentos, Void> criarColunaAcoes() {
        TableColumn<InventarioEquipamentos, Void> colAcoes = new TableColumn<>("Ações");
        colAcoes.setPrefWidth(160);

        colAcoes.setCellFactory(param -> new TableCell<>() {
            private final Button btnEquip = new Button("🔍 Equip.");
            private final Button btnProd = new Button("📦 Prod.");
            private final HBox pane = new HBox(5, btnEquip, btnProd);

            {
                btnEquip.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 11px;");
                btnProd.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 11px;");
                btnEquip.setTooltip(new Tooltip("Ver detalhes do equipamento no inventário"));
                btnProd.setTooltip(new Tooltip("Ver detalhes do produto no catálogo"));

                btnEquip.setOnAction(e -> {
                    InventarioEquipamentos eq = getTableView().getItems().get(getIndex());
                    abrirDetalhesEquipamento(eq.getSkuProduto());
                });

                btnProd.setOnAction(e -> {
                    InventarioEquipamentos eq = getTableView().getItems().get(getIndex());
                    abrirDetalhesProduto(eq.getSkuProduto());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        return colAcoes;
    }

    /**
     * Exibe diálogo modal com tabela de equipamentos filtrados.
     *
     * @param titulo Título da janela
     * @param cabecalho Texto do cabeçalho
     * @param resumo Texto de resumo (ex: "5 equipamentos encontrados")
     * @param equipamentos Lista de equipamentos
     * @param colunasExtras true para colunas detalhadas
     * @param owner Janela pai
     */
    public void mostrarDialogoEquipamentos(String titulo, String cabecalho, String resumo,
                                           List<InventarioEquipamentos> equipamentos,
                                           boolean colunasExtras, Window owner) {
        if (equipamentos.isEmpty()) {
            NexusFX.alerts().info(titulo, "Nenhum equipamento encontrado.", "GIA");
            return;
        }

        TableView<InventarioEquipamentos> tabela = criarTabelaEquipamentosComAcoes(equipamentos, colunasExtras);

        Label lblResumo = new Label(resumo);
        lblResumo.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10 0 10 0;");

        VBox content = new VBox(10, lblResumo, tabela);
        content.setPadding(new Insets(10));

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(titulo);
        dialog.setHeaderText(cabecalho);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setPrefSize(950, 500);

        if (owner != null) dialog.initOwner(owner);
        dialog.showAndWait();
    }

    // ===== MÉTODOS DE NAVEGAÇÃO =====

    private void abrirDetalhesEquipamento(String sku) {
        try {
            InventarioEquipamentos eq = equipamentosCache.stream()
                    .filter(e -> e.getSkuProduto().equals(sku))
                    .findFirst().orElse(null);
            if (eq == null) return;

            Window owner = org.apache.commons.collections4.CollectionUtils.isNotEmpty(equipamentosCache)
                    ? null : null; // Owner será definido no contexto de uso

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
        }
    }

    private void abrirDetalhesProduto(String sku) {
        try {
            NexusFX.dialogs().openModalWithController(
                    "detalhe_produto",
                    "Detalhes do Produto",
                    null,
                    (Object controller) -> {
                        if (controller instanceof CatalogoProdutoDetailController details) {
                            details.setProdutoSku(sku);
                        }
                    }
            );
        } catch (Exception e) {
            logger.error("Erro ao abrir detalhes do produto", e);
        }
    }
}