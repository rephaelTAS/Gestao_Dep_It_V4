package com.ossobo.gestaoDepIt.controllers.gestao.estoque.catalugoproduto;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import com.ossobo.gestaoDepIt.db.enums.TipoProduto;
import com.ossobo.gestaoDepIt.db.models.CatalogoProdutos;
import com.ossobo.gestaoDepIt.db.services.CatalogoProdutosService;
import com.ossobo.gestaoDepIt.controllers.gestao.estoque.catalugoproduto.strategies.*;
import com.ossobo.gestaoDepIt.controllers.gestao.estoque.catalugoproduto.validators.ProdutoFormValidator;
import com.ossobo.gestaoDepIt.utils.GerarIdProduto;
import com.ossobo.nexusfx.di.annotations.Inject;
import com.ossobo.nexusfx.NexusFX;

import java.math.BigDecimal;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Controlador para formulário de adição/edição de produto.
 * v2.0 - Correção: setModoEdicao usa EditarProdutoStrategy, formato Euro (€).
 * Responsabilidade: Gerenciar formulário, validação, salvamento via strategy.
 */
public class CatalogoProdutoFormController implements Initializable {

    @Inject private CatalogoProdutosService produtosService;
    @Inject private GerarIdProduto geradorId;

    // Componentes FXML
    @FXML private Label tituloLabel;
    @FXML private TextField skuField;
    @FXML private ComboBox<String> tipoProdutoCombo;
    @FXML private TextField categoriaField;
    @FXML private TextField marcaField;
    @FXML private TextField modeloField;
    @FXML private TextField corField;
    @FXML private TextField precoUnitarioField;
    @FXML private TextField totalRecebidoField;
    @FXML private TextArea descricaoField;
    @FXML private TextArea caracteristicasField;
    @FXML private CheckBox ativoCheckBox;
    @FXML private Label previewLabel;
    @FXML private Label mensagemErroLabel;
    @FXML private Label idGeradoLabel;
    @FXML private VBox idGeradoContainer;
    @FXML private Button cancelarButton;
    @FXML private Button salvarButton;

    private final StringProperty previewProperty = new SimpleStringProperty();
    private final ProdutoFormValidator validator = new ProdutoFormValidator();
    private ProdutoFormStrategy strategyAtual;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        verificarComponentesFXML();
        inicializarComponentes();
        configurarBindings();
        configurarListenersGeracaoAutomatica();
        setModoNovo();
    }

    // ===== VERIFICAÇÃO DE COMPONENTES =====
    private void verificarComponentesFXML() {
        List<Object> obrigatorios = Arrays.asList(
                skuField, tipoProdutoCombo, categoriaField, marcaField, modeloField,
                previewLabel, cancelarButton, salvarButton);

        String[] nomes = {"skuField", "tipoProdutoCombo", "categoriaField", "marcaField",
                "modeloField", "previewLabel", "cancelarButton", "salvarButton"};

        StringBuilder erros = new StringBuilder();
        for (int i = 0; i < obrigatorios.size(); i++) {
            if (obrigatorios.get(i) == null) {
                erros.append("• ").append(nomes[i]).append("\n");
            }
        }

        if (erros.length() > 0) {
            NexusFX.alerts().erro(
                    "Configuração do Formulário",
                    "Componentes FXML obrigatórios ausentes:\n" + erros,
                    "Sistema");
            throw new IllegalStateException("Componentes FXML obrigatórios ausentes");
        }
    }

    // ===== INICIALIZAÇÃO =====
    private void inicializarComponentes() {
        tipoProdutoCombo.getItems().addAll(TipoProduto.getDisplayNames());
        previewLabel.textProperty().bind(previewProperty);

        skuField.setEditable(false);
        skuField.setStyle("-fx-background-color: #f5f5f5; -fx-font-weight: bold;");

        if (idGeradoContainer != null) {
            idGeradoContainer.setVisible(false);
            idGeradoContainer.setManaged(false);
        }

        configurarCamposNumericos();
    }

    private void configurarCamposNumericos() {
        if (precoUnitarioField != null) {
            precoUnitarioField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal.matches("\\d*(\\.\\d{0,2})?")) {
                    precoUnitarioField.setText(oldVal);
                }
            });
        }

        if (totalRecebidoField != null) {
            totalRecebidoField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal.matches("\\d*")) {
                    totalRecebidoField.setText(oldVal);
                }
            });
        }
    }

    // ===== BINDINGS E LISTENERS =====
    private void configurarBindings() {
        previewLabel.textProperty().bind(previewProperty);
    }

    private void configurarListenersGeracaoAutomatica() {
        PauseTransition debounce = new PauseTransition(Duration.millis(300));
        debounce.setOnFinished(e -> tentarGerarSkuAutomaticamente());

        categoriaField.textProperty().addListener((obs, oldVal, newVal) -> {
            atualizarPreview();
            if (strategyAtual instanceof NovoProdutoStrategy && newVal != null && newVal.length() > 1) {
                ((NovoProdutoStrategy) strategyAtual).setCategoria(newVal);
                debounce.playFromStart();
            }
        });

        marcaField.textProperty().addListener((obs, oldVal, newVal) -> {
            atualizarPreview();
            if (strategyAtual instanceof NovoProdutoStrategy && newVal != null && newVal.length() > 1) {
                ((NovoProdutoStrategy) strategyAtual).setMarca(newVal);
                debounce.playFromStart();
            }
        });

        modeloField.textProperty().addListener((obs, oldVal, newVal) -> {
            atualizarPreview();
            if (strategyAtual instanceof NovoProdutoStrategy && newVal != null && newVal.length() > 1) {
                ((NovoProdutoStrategy) strategyAtual).setModelo(newVal);
                debounce.playFromStart();
            }
        });

        corField.textProperty().addListener((obs, oldVal, newVal) -> atualizarPreview());
        if (precoUnitarioField != null)
            precoUnitarioField.textProperty().addListener((obs, oldVal, newVal) -> atualizarPreview());
        if (totalRecebidoField != null)
            totalRecebidoField.textProperty().addListener((obs, oldVal, newVal) -> atualizarPreview());
    }

    // ===== MODOS DE OPERAÇÃO =====
    public void setModoNovo() {
        NovoProdutoStrategy strategy = new NovoProdutoStrategy();
        strategy.setGeradorId(geradorId);
        trocarEstrategia(strategy);

        if (tituloLabel != null) {
            tituloLabel.setText("Novo Produto");
        }

        salvarButton.setText("Salvar Produto");
        limparFormulario();
    }

    /**
     * ✅ v2.0 CORRIGIDO: Usa EditarProdutoStrategy em vez de chamar service diretamente.
     */
    public void setModoEdicao(String sku) {
        if (sku == null || sku.trim().isEmpty()) {
            NexusFX.alerts().erro("Erro", "SKU inválido para edição.", "Catálogo");
            return;
        }

        mostrarCarregamento(true);

        CompletableFuture.runAsync(() -> {
            try {
                // ✅ CORREÇÃO: Cria a strategy de edição
                EditarProdutoStrategy strategy = new EditarProdutoStrategy(sku.trim(), produtosService);

                Platform.runLater(() -> {
                    trocarEstrategia(strategy);

                    // Preenche formulário com dados do produto original
                    CatalogoProdutos original = strategy.getProdutoOriginal();
                    if (original != null) {
                        preencherCamposDoProduto(original);
                        if (tituloLabel != null) {
                            tituloLabel.setText("Editar Produto - " + original.getSku());
                        }
                        salvarButton.setText("Atualizar Produto");
                    }

                    mostrarCarregamento(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    NexusFX.alerts().erro(
                            "Produto Não Encontrado",
                            "SKU " + sku + " não existe no sistema.",
                            "Catálogo");
                    setModoNovo();
                    mostrarCarregamento(false);
                });
            }
        });
    }

    private void preencherCamposDoProduto(CatalogoProdutos p) {
        skuField.setText(p.getSku());
        categoriaField.setText(p.getCategoria());
        marcaField.setText(p.getMarca());
        modeloField.setText(p.getModelo());
        corField.setText(p.getCor() != null ? p.getCor() : "");
        precoUnitarioField.setText(p.getPrecoUnitario() != null ? p.getPrecoUnitario().toString() : "");
        totalRecebidoField.setText(p.getTotalRecebido() != null ? p.getTotalRecebido().toString() : "");
        descricaoField.setText(p.getDescricao() != null ? p.getDescricao() : "");
        caracteristicasField.setText(p.getCaracteristicasTecnicas() != null ? p.getCaracteristicasTecnicas() : "");
        ativoCheckBox.setSelected(Boolean.TRUE.equals(p.getAtivo()));

        if (p.getTipoProduto() != null) {
            tipoProdutoCombo.getSelectionModel().select(p.getTipoProduto().toString());
        }

        atualizarPreview();
    }

    // ===== ESTRATÉGIA =====
    private void trocarEstrategia(ProdutoFormStrategy novaStrategy) {
        if (strategyAtual != null) {
            strategyAtual.limparModoEspecifico();
        }
        strategyAtual = novaStrategy;
        strategyAtual.configurarModo();
    }

    // ===== GERAÇÃO DE SKU =====
    private void tentarGerarSkuAutomaticamente() {
        if (!(strategyAtual instanceof NovoProdutoStrategy)) return;

        NovoProdutoStrategy strategy = (NovoProdutoStrategy) strategyAtual;
        String tipo = strategy.getCategoria();
        String marca = strategy.getMarca();
        String modelo = strategy.getModelo();

        if (tipo != null && !tipo.isEmpty() &&
                marca != null && !marca.isEmpty() &&
                modelo != null && !modelo.isEmpty()) {

            String skuGerado = strategy.gerarIdSemantico();
            if (skuGerado != null && !skuGerado.isEmpty()) {
                skuField.setText(skuGerado);
                mostrarFeedbackSkuGerado(skuGerado);
            }
        }
    }

    private void mostrarFeedbackSkuGerado(String sku) {
        if (idGeradoLabel != null) {
            idGeradoLabel.setText("SKU gerado: " + sku);
        }
        if (idGeradoContainer != null) {
            idGeradoContainer.setVisible(true);
            idGeradoContainer.setManaged(true);
        }
    }

    // ===== PREVIEW =====
    private void atualizarPreview() {
        String sku = skuField.getText() != null ? skuField.getText() : "-";
        String tipo = tipoProdutoCombo.getValue() != null ? tipoProdutoCombo.getValue() : "-";
        String marca = marcaField.getText() != null ? marcaField.getText() : "-";
        String modelo = modeloField.getText() != null ? modeloField.getText() : "-";
        String preco = (precoUnitarioField != null && !precoUnitarioField.getText().isEmpty())
                ? precoUnitarioField.getText() : "-";
        String estoque = (totalRecebidoField != null && !totalRecebidoField.getText().isEmpty())
                ? totalRecebidoField.getText() : "-";

        // ✅ v2.0: Formato Euro
        String preview = String.format(
                "SKU: %s | Tipo: %s | Marca: %s | Modelo: %s | Preço: € %s | Estoque: %s",
                sku, tipo, marca, modelo, preco, estoque);

        previewProperty.set(preview);
    }

    // ===== VALIDAÇÃO =====
    private boolean validarFormulario() {
        Map<String, String> erros = validator.validarCamposObrigatorios(
                skuField, tipoProdutoCombo, categoriaField, marcaField, modeloField);

        if (precoUnitarioField != null && !precoUnitarioField.getText().trim().isEmpty()) {
            try {
                new BigDecimal(precoUnitarioField.getText().trim());
            } catch (NumberFormatException e) {
                erros.put("preco", "Preço inválido (use 0.00)");
            }
        }

        if (totalRecebidoField != null && !totalRecebidoField.getText().trim().isEmpty()) {
            try {
                Integer.parseInt(totalRecebidoField.getText().trim());
            } catch (NumberFormatException e) {
                erros.put("estoque", "Estoque inválido (use número inteiro)");
            }
        }

        Map<String, String> errosEspecificos = strategyAtual.validarCamposEspecificos();
        erros.putAll(errosEspecificos);

        if (!erros.isEmpty()) {
            mostrarErros(erros);
            return false;
        }
        return true;
    }

    private void mostrarErros(Map<String, String> erros) {
        StringBuilder mensagem = new StringBuilder();
        erros.forEach((campo, erro) -> mensagem.append("• ").append(erro).append("\n"));

        NexusFX.alerts().erro(
                "Validação do Formulário",
                mensagem.toString(),
                "Catálogo");
    }

    // ===== HANDLERS =====
    @FXML
    private void handleSalvar() {
        if (validarFormulario()) {
            salvarProduto();
        }
    }

    @FXML
    private void handleCancelar() {
        fecharJanela();
    }

    // ===== SALVAMENTO =====
    private void salvarProduto() {
        mostrarCarregamento(true);

        CompletableFuture.runAsync(() -> {
            try {
                CatalogoProdutos produto = criarProdutoBase();
                produto = strategyAtual.prepararProdutoParaSalvar(produto);

                if (strategyAtual instanceof NovoProdutoStrategy) {
                    produtosService.criarProduto(produto);
                } else {
                    produtosService.atualizarProduto(produto);
                }

                CatalogoProdutos finalProduto = produto;
                Platform.runLater(() -> {
                    NexusFX.alerts().info(
                            "Sucesso",
                            "Produto " + finalProduto.getSku() + " salvo com sucesso.",
                            "Catálogo");
                    fecharJanela();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    NexusFX.alerts().erro(
                            "Erro ao Salvar",
                            e.getMessage(),
                            "Sistema");
                    mostrarCarregamento(false);
                });
            }
        });
    }

    private CatalogoProdutos criarProdutoBase() {
        CatalogoProdutos produto = new CatalogoProdutos();

        produto.setSku(skuField.getText().trim());
        produto.setTipoProduto(TipoProduto.fromString(tipoProdutoCombo.getValue()));
        produto.setCategoria(categoriaField.getText().trim());
        produto.setMarca(marcaField.getText().trim());
        produto.setModelo(modeloField.getText().trim());
        produto.setCor(corField.getText() != null ? corField.getText().trim() : null);
        produto.setDescricao(descricaoField.getText() != null ? descricaoField.getText().trim() : null);

        if (precoUnitarioField != null && !precoUnitarioField.getText().trim().isEmpty()) {
            try {
                produto.setPrecoUnitario(new BigDecimal(precoUnitarioField.getText().trim()));
            } catch (NumberFormatException e) {
                produto.setPrecoUnitario(BigDecimal.ZERO);
            }
        }

        if (totalRecebidoField != null && !totalRecebidoField.getText().trim().isEmpty()) {
            try {
                produto.setTotalRecebido(Integer.parseInt(totalRecebidoField.getText().trim()));
            } catch (NumberFormatException e) {
                produto.setTotalRecebido(0);
            }
        }

        produto.setCaracteristicasTecnicas(
                caracteristicasField.getText() != null && !caracteristicasField.getText().trim().isEmpty()
                        ? caracteristicasField.getText().trim() : null);
        produto.setAtivo(ativoCheckBox.isSelected());

        return produto;
    }

    // ===== UI UTILITÁRIOS =====
    private void mostrarCarregamento(boolean carregando) {
        salvarButton.setDisable(carregando);
        cancelarButton.setDisable(carregando);
        salvarButton.setText(carregando ? "Processando..." :
                (strategyAtual instanceof NovoProdutoStrategy ? "Salvar Produto" : "Atualizar Produto"));
    }

    private void limparFormulario() {
        skuField.clear();
        tipoProdutoCombo.setValue(null);
        categoriaField.clear();
        marcaField.clear();
        modeloField.clear();
        corField.clear();
        if (precoUnitarioField != null) precoUnitarioField.clear();
        if (totalRecebidoField != null) totalRecebidoField.clear();
        descricaoField.clear();
        caracteristicasField.clear();
        ativoCheckBox.setSelected(true);
        if (idGeradoContainer != null) {
            idGeradoContainer.setVisible(false);
            idGeradoContainer.setManaged(false);
        }
    }

    private void fecharJanela() {
        Stage stage = (Stage) cancelarButton.getScene().getWindow();
        stage.close();
    }

    public void cleanup() {
        if (strategyAtual != null) {
            strategyAtual.limparModoEspecifico();
        }
        previewProperty.unbind();
    }
}