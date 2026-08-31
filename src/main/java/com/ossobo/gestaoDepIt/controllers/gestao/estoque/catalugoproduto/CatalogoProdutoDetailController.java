package com.ossobo.gestaoDepIt.controllers.gestao.estoque.catalugoproduto;

import com.ossobo.winterfx.anotations.Inject;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;
import com.ossobo.gestaoDepIt.db.models.CatalogoProdutos;
import com.ossobo.gestaoDepIt.db.services.CatalogoProdutosService;


import java.math.BigDecimal;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

/**
 * Controller para visualização de detalhes de produtos.
 * v2.0 - Correção: formato Euro (€), NexusFX.critical() → erro(), openModal assinatura correta.
 * Responsabilidade: Exibir detalhes, ações rápidas (estoque, preço, status).
 */
public class CatalogoProdutoDetailController implements Initializable {

    @Inject
    private CatalogoProdutosService produtosService;

    private CatalogoProdutos produto;
    private String produtoSku;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private volatile boolean fxmlCarregado = false;
    private final CompletableFuture<Void> fxmlCarregadoFuture = new CompletableFuture<>();

    // Componentes FXML
    @FXML private Button voltarButton;
    @FXML private Label tituloLabel;
    @FXML private Label subtituloLabel;
    @FXML private Button editarButton;
    @FXML private Button desativarButton;
    @FXML private Label detailSku;
    @FXML private Label detailTipo;
    @FXML private Label detailCategoria;
    @FXML private Label detailMarca;
    @FXML private Label detailModelo;
    @FXML private Label detailCor;
    @FXML private Label detailPrecoUnitario;
    @FXML private Label detailEstoque;
    @FXML private Label detailStatus;
    @FXML private TextArea detailDescricao;
    @FXML private TextArea detailCaracteristicas;
    @FXML private Label detailCriadoEm;
    @FXML private Label detailAtualizadoEm;
    @FXML private Label statValorTotal;
    @FXML private Label statPrecoUnitario;
    @FXML private Label statQuantidadeEstoque;
    @FXML private Label statEstoque;
    @FXML private Label statEquipamentos;
    @FXML private Label statMovimentacoes;
    @FXML private Label statToners;
    @FXML private Button btnAjustarEstoque;
    @FXML private Button btnAtualizarPreco;
    @FXML private Button btnVerEquipamentos;
    @FXML private Button btnMovimentar;
    @FXML private Button btnHistorico;
    @FXML private Label mensagemStatusLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        detailDescricao.setEditable(false);
        detailCaracteristicas.setEditable(false);
        fxmlCarregado = true;
        fxmlCarregadoFuture.complete(null);
    }

    // ===== MÉTODO PRINCIPAL =====
    public void setProdutoSku(String produtoSku) {
        this.produtoSku = produtoSku;
        fxmlCarregadoFuture.thenRun(() -> Platform.runLater(() -> carregarProduto(produtoSku)));
    }

    // ===== CARREGAMENTO =====
    private void carregarProduto(String sku) {
        mostrarCarregamento(true);

        CompletableFuture.runAsync(() -> {
            try {
                Optional<CatalogoProdutos> produtoOpt = produtosService.buscarPorSku(sku);

                Platform.runLater(() -> {
                    if (produtoOpt.isPresent()) {
                        this.produto = produtoOpt.get();
                        preencherDados(produto);
                        atualizarInterface();
                    } else {
                        mostrarErro("Produto Não Encontrado", "SKU: " + sku);
                    }
                    mostrarCarregamento(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    mostrarErro("Erro", "Falha ao carregar: " + e.getMessage());
                    mostrarCarregamento(false);
                });
            }
        });
    }

    // ===== PREENCHIMENTO =====
    private void preencherDados(CatalogoProdutos p) {
        detailSku.setText(p.sku());
        detailTipo.setText(p.tipoProduto() != null ? p.tipoProduto().toString() : "-");
        detailCategoria.setText(nullToDash(p.categoria()));
        detailMarca.setText(nullToDash(p.marca()));
        detailModelo.setText(nullToDash(p.modelo()));
        detailCor.setText(nullToDash(p.cor()));

        // Preço (€)
        BigDecimal preco = p.getPrecoUnitario();
        String precoStr = preco != null ? String.format("€ %.2f", preco) : "€ 0,00";
        detailPrecoUnitario.setText(precoStr);
        if (statPrecoUnitario != null) statPrecoUnitario.setText(precoStr);

        // Estoque
        Integer estoque = p.getTotalRecebido();
        String estoqueStr = estoque != null ? estoque + " unidades" : "0 unidades";
        detailEstoque.setText(estoqueStr);
        if (statQuantidadeEstoque != null) statQuantidadeEstoque.setText(estoqueStr);

        // Valor total (€)
        if (statValorTotal != null) {
            if (preco != null && estoque != null && estoque > 0) {
                statValorTotal.setText(String.format("€ %.2f", preco.multiply(new BigDecimal(estoque))));
            } else {
                statValorTotal.setText("€ 0,00");
            }
        }

        // Status
        boolean ativo = Boolean.TRUE.equals(p.getAtivo());
        detailStatus.setText(ativo ? "Ativo" : "Inativo");
        detailStatus.getStyleClass().setAll(ativo ? "status-ativo" : "status-inativo");

        // Textos
        detailDescricao.setText(p.getDescricao() != null ? p.getDescricao() : "Sem descrição.");
        detailCaracteristicas.setText(p.getCaracteristicasTecnicas() != null ? p.getCaracteristicasTecnicas() : "Nenhuma.");

        // Datas
        detailCriadoEm.setText(p.getCreatedAt() != null ? p.getCreatedAt().format(dateFormatter) : "-");
        detailAtualizadoEm.setText(p.getUpdatedAt() != null ? p.getUpdatedAt().format(dateFormatter) : "-");

        // Título
        tituloLabel.setText("Detalhes do Produto");
        subtituloLabel.setText(String.format("SKU: %s | %s %s",
                p.getSku(), nullToDash(p.getMarca()), nullToDash(p.getModelo())));

        // Estatísticas mock
        if (statEstoque != null) statEstoque.setText("Em Estoque: " + (estoque != null ? estoque : 0) + " unidades");
        if (statEquipamentos != null) statEquipamentos.setText("Equipamentos: -");
        if (statMovimentacoes != null) statMovimentacoes.setText("Movimentações: -");
        if (statToners != null) statToners.setText("Toners: -");
    }

    private String nullToDash(String s) { return s != null ? s : "-"; }

    private void atualizarInterface() {
        if (produto == null) return;
        boolean ativo = Boolean.TRUE.equals(produto.getAtivo());

        desativarButton.setText(ativo ? "Desativar" : "Ativar");
        desativarButton.getStyleClass().setAll(ativo ? "button-danger" : "button-success");

        btnAjustarEstoque.setDisable(!ativo);
        btnAtualizarPreco.setDisable(!ativo);
        btnMovimentar.setDisable(!ativo);
    }

    // ===== HANDLERS =====
    @FXML
    private void handleVoltar() {
        fecharJanela();
    }

    @FXML
    private void handleEditar(ActionEvent event) {
        if (produto == null) return;

        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();

        NexusFX.dialogs().openModalWithController(
                "adiciona_editar_produto",
                "Editar Produto - " + produto.getSku(),
                stage,
                CatalogoProdutoFormController.class,
                controller -> controller.setModoEdicao(produto.getSku())
        );
    }

    @FXML
    private void handleDesativarAtivar() {
        if (produto == null) return;

        boolean ativo = Boolean.TRUE.equals(produto.getAtivo());
        String acao = ativo ? "desativar" : "ativar";

        NexusFX.alerts().confirmar(
                acao.substring(0, 1).toUpperCase() + acao.substring(1) + " Produto",
                String.format("Deseja %s o produto %s - %s %s?",
                        acao, produto.getSku(), produto.getMarca(), produto.getModelo()),
                confirmado -> {
                    if (confirmado) executarToggleStatus(ativo);
                }
        );
    }

    @FXML
    private void handleAjustarEstoque() {
        if (produto == null) return;

        String atual = produto.getTotalRecebido() != null ? String.valueOf(produto.getTotalRecebido()) : "0";
        TextInputDialog dialog = new TextInputDialog(atual);
        dialog.setTitle("Ajustar Estoque");
        dialog.setHeaderText("Produto: " + produto.getSku() + " - " + produto.getModelo());
        dialog.setContentText("Nova quantidade:");

        dialog.showAndWait().ifPresent(valor -> {
            try {
                int qtd = Integer.parseInt(valor);
                if (qtd < 0) throw new NumberFormatException();
                ajustarEstoque(qtd);
            } catch (NumberFormatException e) {
                NexusFX.alerts().erro("Valor Inválido", "Use um número inteiro positivo.", "Catálogo");
            }
        });
    }

    @FXML
    private void handleAtualizarPreco() {
        if (produto == null) return;

        String atual = produto.getPrecoUnitario() != null ? String.format("%.2f", produto.getPrecoUnitario()) : "0.00";
        TextInputDialog dialog = new TextInputDialog(atual);
        dialog.setTitle("Atualizar Preço");
        dialog.setHeaderText("Produto: " + produto.getSku() + " - " + produto.getModelo());
        dialog.setContentText("Novo preço (€):");

        dialog.showAndWait().ifPresent(valor -> {
            try {
                BigDecimal preco = new BigDecimal(valor.replace(",", "."));
                if (preco.compareTo(BigDecimal.ZERO) < 0) throw new NumberFormatException();
                atualizarPreco(preco);
            } catch (NumberFormatException e) {
                NexusFX.alerts().erro("Valor Inválido", "Use formato decimal (ex: 150.50).", "Catálogo");
            }
        });
    }

    @FXML
    private void handleVerEquipamentos() {
        mostrarInfo("Em desenvolvimento", "Funcionalidade disponível em breve.");
    }

    @FXML
    private void handleRegistrarMovimentacao() {
        mostrarInfo("Em desenvolvimento", "Funcionalidade disponível em breve.");
    }

    @FXML
    private void handleVerHistorico() {
        mostrarInfo("Em desenvolvimento", "Funcionalidade disponível em breve.");
    }

    // ===== AÇÕES =====
    private void ajustarEstoque(int novaQuantidade) {
        mostrarCarregamento(true);
        CompletableFuture.runAsync(() -> {
            try {
                produtosService.atualizarEstoque(produto.getSku(), novaQuantidade);
                produto.setTotalRecebido(novaQuantidade);
                Platform.runLater(() -> {
                    preencherDados(produto);
                    mostrarCarregamento(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    mostrarErro("Erro", e.getMessage());
                    mostrarCarregamento(false);
                });
            }
        });
    }

    private void atualizarPreco(BigDecimal novoPreco) {
        mostrarCarregamento(true);
        CompletableFuture.runAsync(() -> {
            try {
                produto.setPrecoUnitario(novoPreco);
                produtosService.atualizarProduto(produto);
                Platform.runLater(() -> {
                    preencherDados(produto);
                    mostrarCarregamento(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    mostrarErro("Erro", e.getMessage());
                    mostrarCarregamento(false);
                });
            }
        });
    }

    private void executarToggleStatus(boolean ativoAtual) {
        mostrarCarregamento(true);
        CompletableFuture.runAsync(() -> {
            try {
                if (ativoAtual) {
                    produtosService.desativarProduto(produto.getSku());
                    produto.setAtivo(false);
                } else {
                    produtosService.ativarProduto(produto.getSku());
                    produto.setAtivo(true);
                }
                Platform.runLater(() -> {
                    atualizarInterface();
                    preencherDados(produto);
                    mostrarCarregamento(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    mostrarErro("Erro", e.getMessage());
                    mostrarCarregamento(false);
                });
            }
        });
    }

    // ===== UI =====
    private void mostrarCarregamento(boolean carregando) {
        if (!fxmlCarregado) return;
        if (voltarButton != null) voltarButton.setDisable(carregando);
        if (editarButton != null) editarButton.setDisable(carregando);
        if (desativarButton != null) desativarButton.setDisable(carregando);
    }

    private void mostrarErro(String titulo, String mensagem) {
        NexusFX.alerts().erro(titulo, mensagem, "Catálogo");
    }

    private void mostrarInfo(String titulo, String mensagem) {
        NexusFX.alerts().info(titulo, mensagem, "Catálogo");
    }

    private void fecharJanela() {
        if (voltarButton != null && voltarButton.getScene() != null) {
            ((Stage) voltarButton.getScene().getWindow()).close();
        }
    }

    public void refreshData() {
        if (produtoSku != null) carregarProduto(produtoSku);
    }

    public void cleanup() {
        produto = null;
        produtoSku = null;
    }
}