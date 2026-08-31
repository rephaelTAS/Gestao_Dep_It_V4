// packt/app/controllers/gestao/estoque/movimentacoes/dialogs/ProdutoSelectionDialog.java
package com.ossobo.gestaoDepIt.controllers.gestao.estoque.movimentacoes.dialogs;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ossobo.gestaoDepIt.db.models.CatalogoProdutos;
import com.ossobo.gestaoDepIt.db.services.CatalogoProdutosService;
import com.ossobo.nexusfx.di.annotations.Inject;
import com.ossobo.nexusfx.NexusFX;

import java.util.function.Consumer;

/**
 * Diálogo SIMPLIFICADO para seleção de produtos usando NexusFX
 * API limpa: setOnSelectCallback(Consumer<CatalogoProdutos>)
 */
public class ProdutoSelectionDialog {

    private static final Logger logger = LoggerFactory.getLogger(ProdutoSelectionDialog.class);

    @Inject
    private CatalogoProdutosService produtosService;

    // ===== COMPONENTES FXML =====
    @FXML private TextField filtroTextField;
    @FXML private TableView<CatalogoProdutos> produtosTable;
    @FXML private TableColumn<CatalogoProdutos, String> codigoColumn;
    @FXML private TableColumn<CatalogoProdutos, String> descricaoColumn;
    @FXML private Label contadorLabel;

    // ===== DADOS =====
    private ObservableList<CatalogoProdutos> produtosList = FXCollections.observableArrayList();
    private FilteredList<CatalogoProdutos> produtosFiltrados;
    private Consumer<CatalogoProdutos> onSelectCallback;

    // ===== MÉTODO DE INICIALIZAÇÃO =====
    @FXML
    private void initialize() {
        logger.info("Inicializando diálogo simplificado de seleção de produtos");
        configurarTabela();
        carregarProdutos();
        configurarListeners();
    }

    // ===== CONFIGURAÇÃO DA TABELA =====
    private void configurarTabela() {
        // Coluna de Código
        codigoColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getSku())
        );
        codigoColumn.setPrefWidth(150);

        // Coluna de Descrição
        descricaoColumn.setCellValueFactory(cellData -> {
            CatalogoProdutos produto = cellData.getValue();
            return new SimpleStringProperty(
                    String.format("%s %s %s",
                            produto.getTipoProduto() != null ? produto.getTipoProduto() : "",
                            produto.getMarca() != null ? produto.getMarca() : "",
                            produto.getModelo() != null ? produto.getModelo() : ""
                    ).trim()
            );
        });
        descricaoColumn.setPrefWidth(400);

        // Double-click para selecionar
        produtosTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                handleSelecionar();
            }
        });
    }

    // ===== CARREGAMENTO DE DADOS =====
    private void carregarProdutos() {
        try {
            produtosList.setAll(produtosService.listarTodos(1, 50)); // Carrega 50 produtos
            produtosFiltrados = new FilteredList<>(produtosList, p -> true);
            produtosTable.setItems(produtosFiltrados);
            atualizarContador();

        } catch (Exception e) {
            logger.error("Erro ao carregar produtos", e);
            NexusFX.alerts().erro(
                    "Erro",
                    "Não foi possível carregar produtos: " + e.getMessage(),
                    "Seleção de Produtos"
            );
        }
    }

    // ===== FILTRO EM TEMPO REAL =====
    private void aplicarFiltro() {
        String filtro = filtroTextField.getText().trim().toLowerCase();

        if (filtro.isEmpty()) {
            produtosFiltrados.setPredicate(p -> true);
        } else {
            produtosFiltrados.setPredicate(produto ->
                    (produto.getSku() != null && produto.getSku().toLowerCase().contains(filtro)) ||
                            (produto.getMarca() != null && produto.getMarca().toLowerCase().contains(filtro)) ||
                            (produto.getModelo() != null && produto.getModelo().toLowerCase().contains(filtro)) ||
                            (produto.getTipoProduto() != null && produto.getCategoria().toLowerCase().contains(filtro)) ||
                            (produto.getCategoria() != null && produto.getCategoria().toLowerCase().contains(filtro))
            );
        }
        atualizarContador();
    }

    // ===== HANDLERS =====
    @FXML
    private void handleBuscar() {
        aplicarFiltro();
    }

    @FXML
    private void handleSelecionar() {
        CatalogoProdutos selecionado = produtosTable.getSelectionModel().getSelectedItem();

        if (selecionado != null && onSelectCallback != null) {
            onSelectCallback.accept(selecionado);
            fecharJanela();
        } else if (selecionado == null) {
            NexusFX.alerts().warn(
                    "Atenção",
                    "Selecione um produto da lista",
                    "Seleção de Produtos"
            );
        }
    }

    @FXML
    private void handleCancelar() {
        fecharJanela();
    }

    // ===== UTILITÁRIOS =====
    private void atualizarContador() {
        int total = produtosList.size();
        int filtrados = produtosFiltrados.size();
        contadorLabel.setText(filtroTextField.getText().trim().isEmpty()
                ? String.format("Total: %d produtos", total)
                : String.format("Mostrando %d de %d produtos", filtrados, total)
        );
    }

    private void configurarListeners() {
        filtroTextField.textProperty().addListener((obs, oldVal, newVal) -> aplicarFiltro());
        filtroTextField.setOnAction(e -> aplicarFiltro());
    }

    private void fecharJanela() {
        produtosTable.getScene().getWindow().hide();
    }

    // ===== MÉTODOS PÚBLICOS (API LIMPA) =====

    /**
     * Define o callback para quando um produto for selecionado
     * @param callback Consumer que recebe o produto selecionado
     */
    public void setOnSelectCallback(Consumer<CatalogoProdutos> callback) {
        this.onSelectCallback = callback;
    }

    /**
     * Retorna o produto atualmente selecionado na tabela
     * @return CatalogoProdutos selecionado ou null
     */
    public CatalogoProdutos getSelecionado() {
        return produtosTable.getSelectionModel().getSelectedItem();
    }
}