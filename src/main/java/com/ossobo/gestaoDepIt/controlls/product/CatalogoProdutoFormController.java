package com.ossobo.gestaoDepIt.controlls.product;

import com.ossobo.gestaoDepIt.config.ViewConstant;
import com.ossobo.gestaoDepIt.controlls.product.validators.ProdutoFormValidator;
import com.ossobo.gestaoDepIt.db.enums.TipoProduto;
import com.ossobo.gestaoDepIt.db.models.CatalogoProdutos;
import com.ossobo.winterfx.anotations.Controller;
import com.ossobo.winterfx.anotations.GetMapping;
import com.ossobo.winterfx.anotations.RequestMapping;
import com.ossobo.winterfx.anotations.RouteVar;
import com.ossobo.winterfx.router.Rotas;
import com.ossobo.winterfx.router.model.Params;
import com.ossobo.winterfx.router.model.ResponseData;
import com.ossobo.winterfx.view.anotations.RegisterView;
import com.ossobo.winterfx.view.controller.WinterFXController;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.file.Files;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

/**
 * CatalogoProdutoFormController v2.8
 *
 * v2.8 — Atualização resolvida por SKU na fronteira:
 *        - Salvar em modo edição passa a chamar `atualizar/por/sku`.
 *        - O id (UUID de sync) NÃO trafega no payload — é resolvido pelo
 *          backend a partir do SKU (chave de negócio imutável).
 *        - Fim do bug "Produto com ID ... não encontrado".
 *
 * v2.7 — Correção do NullPointerException em edição: rota guarda sku
 *        pendente e aplica no initialize() (FXML pode não estar injetado
 *        quando a rota chega). Listeners de SKU cobrem categoria+marca+modelo.
 *
 * v2.6 — Suporte a anexo da fatura de compra (byte[]).
 * v2.5 — setModoEdicao é @GetMapping (o form RECEBE, não muta banco).
 *
 * @since v2.8
 */
@Controller(proxy = false)
@RequestMapping("catalogo/produtoform")
@RegisterView(
        id = ViewConstant.Catalogo.FORM,
        fxml = "/META-INF/gestaoDepIt/fxmls/catalogo_produto/CatalogoProdutosForm.fxml",
        primaryCss = "/META-INF/gestaoDepIt/css/catalugoproduto/CatalogoProdutoForm.css"
)
public class CatalogoProdutoFormController implements Initializable, WinterFXController {

    private static final System.Logger logger =
            System.getLogger(CatalogoProdutoFormController.class.getName());

    private static final long MAX_FATURA_BYTES = 2L * 1024 * 1024; // 2MB
    private static final String[] EXTENSOES_FATURA = {"*.pdf", "*.png", "*.jpg", "*.jpeg"};

    // ===== FXML =====
    @FXML private Label tituloLabel;
    @FXML private TextField skuField;
    @FXML private ComboBox<String> tipoProduto;
    @FXML private TextField categoriaField;
    @FXML private TextField marcaField;
    @FXML private TextField modeloField;
    @FXML private TextField corField;
    @FXML private TextField fornecedorField;
    @FXML private TextField numeroFaturaField;
    @FXML private Button btn_anexarFatura;
    @FXML private Button btn_removerFatura;
    @FXML private Label lblFaturaSelecionada;
    @FXML private TextField precoUnitarioField;
    @FXML private TextField ivaField;
    @FXML private TextField totalRecebidoField;
    @FXML private TextArea descricaoField;
    @FXML private TextArea caracteristicasField;
    @FXML private CheckBox ativoCheckBox;
    @FXML private Label previewLabel;
    @FXML private Label mensagemErroLabel;
    @FXML private Label idGeradoLabel;
    @FXML private VBox idGeradoContainer;
    @FXML private Button btn_cancelar;
    @FXML private Button btn_salvar;

    // ===== ESTADO =====
    private boolean emModoEdicao = false;
    private boolean carregandoEdicao = false;

    /**
     * SKU pendente de edição — aplicado no initialize() quando a rota de
     * edição chega ANTES do FXML carregar (bug clássico do WinterFX:
     * o detail chama a rota, a rota instancia o controller, só depois o
     * FXML é injetado nos @FXML).
     */
    private String skuPendenteEdicao = null;

    /** Bytes do anexo da fatura — null/empty = sem anexo. */
    private byte[] faturaBytes = null;
    /** Nome do arquivo anexado — só para feedback na UI. */
    private String faturaNome = null;

    private final ProdutoFormValidator validator = new ProdutoFormValidator();
    private final PauseTransition skuDebounce = new PauseTransition(Duration.millis(400));

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        inicializarComponentes();
        configurarListenersGeracaoAutomatica();
        setModoNovo();

        // Aplica edição pendente (se a rota veio antes do FXML carregar)
        if (skuPendenteEdicao != null && !skuPendenteEdicao.isBlank()) {
            String sku = skuPendenteEdicao;
            skuPendenteEdicao = null;
            aplicarEdicao(sku);
        }
    }

    private void inicializarComponentes() {
        tipoProduto.getItems().addAll(TipoProduto.getDisplayNames());

        skuField.setEditable(false);
        skuField.setStyle("-fx-background-color: #f5f5f5; -fx-font-weight: bold;");

        if (idGeradoContainer != null) {
            idGeradoContainer.setVisible(false);
            idGeradoContainer.setManaged(false);
        }

        atualizarFeedbackFatura();
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
        if (ivaField != null) {
            ivaField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal.matches("\\d*(\\.\\d{0,2})?")) {
                    ivaField.setText(oldVal);
                }
            });
        }
    }

    private void configurarListenersGeracaoAutomatica() {
        skuDebounce.setOnFinished(e -> gerarSku());

        // Geração de SKU escuta os 3 campos que o compõem.
        // Bloqueado em edição — SKU é imutável depois de criado.
        javafx.beans.value.ChangeListener<String> listener = (obs, oldVal, newVal) -> {
            if (!carregandoEdicao && !emModoEdicao) {
                skuDebounce.playFromStart();
            }
        };

        categoriaField.textProperty().addListener(listener);
        marcaField.textProperty().addListener(listener);
        modeloField.textProperty().addListener(listener);
    }

    private void gerarSku() {
        String categoria = categoriaField.getText();
        String marca = marcaField.getText();
        String modelo = modeloField.getText();

        if (categoria == null || categoria.isBlank()
                || marca == null || marca.isBlank()
                || modelo == null || modelo.isBlank()) {
            skuField.clear();
            return;
        }

        try {
            var resp = Rotas.get("gerarIdProduto/gerarIdProdutoForm",
                    Params.with("categoria", categoria)
                            .and("marca", marca)
                            .and("modelo", modelo));

            String id = resp.getDataString("idproduto");
            if (id != null && !id.isBlank()) {
                skuField.setText(id.trim());
            }
        } catch (Exception e) {
            // Deliberado: falha na geração não pode travar a digitação.
            logger.log(System.Logger.Level.DEBUG,
                    "Geração de SKU falhou (silenciosa): {0}", e.getMessage());
        }
    }

    // ===== MODOS =====
    public void setModoNovo() {
        emModoEdicao = false;
        tituloLabel.setText("Adicionar Produto");
        mostrarCarregamento(false);
        limparFormulario();
    }

    /**
     * GET: o Detail/List envia o sku; o form entra em modo edição.
     *
     * ATENÇÃO: NÃO preencher campos aqui. Se a rota é chamada ANTES do
     * FXMLLoader injetar os @FXML, os campos são null → NPE.
     * Solução: guardar o sku e aplicar no initialize().
     */
    @GetMapping("form/editar/sku")
    public ResponseData setModoEdicao(@RouteVar("sku") String sku) {
        if (sku == null || sku.isBlank()) {
            return ResponseData.error("SKU é obrigatório para edição");
        }
        if (skuField != null) {
            return aplicarEdicao(sku);
        }
        skuPendenteEdicao = sku;
        return ResponseData.success();
    }

    private ResponseData aplicarEdicao(String sku) {
        var resp = Rotas.get("catalogo-produtos/service/por/sku",
                Params.with("sku", sku));

        CatalogoProdutos original = resp.getData("produto", CatalogoProdutos.class);
        if (original == null) {
            return ResponseData.error("Produto não encontrado: " + sku);
        }

        emModoEdicao = true;   // ANTES de preencher — trava geração de SKU
        preencherCamposDoProduto(original);
        tituloLabel.setText("Editar Produto - " + original.sku());
        mostrarCarregamento(false);
        return ResponseData.success();
    }

    private void preencherCamposDoProduto(CatalogoProdutos p) {
        carregandoEdicao = true;
        try {
            skuField.setText(p.sku());
            categoriaField.setText(p.categoria());
            marcaField.setText(p.marca());
            modeloField.setText(p.modelo());
            corField.setText(p.cor() != null ? p.cor() : "");

            if (fornecedorField != null) {
                fornecedorField.setText(p.fornecedor() != null ? p.fornecedor() : "");
            }
            if (numeroFaturaField != null) {
                numeroFaturaField.setText(p.numeroFatura() != null ? p.numeroFatura() : "");
            }

            faturaBytes = p.faturaCompra() != null && p.faturaCompra().length > 0
                    ? p.faturaCompra() : null;
            faturaNome = faturaBytes != null ? "fatura_" + p.sku() : null;
            atualizarFeedbackFatura();

            precoUnitarioField.setText(formatarCentavosParaEuros(p.precoUnitarioCentavos()));
            if (ivaField != null) {
                ivaField.setText(formatarIvaBpParaPercent(p.ivaBasisPoints()));
            }

            totalRecebidoField.setText(p.totalRecebido() != null ? p.totalRecebido().toString() : "");
            descricaoField.setText(p.descricao() != null ? p.descricao() : "");
            caracteristicasField.setText(p.caracteristicasTecnicas() != null ? p.caracteristicasTecnicas() : "");
            ativoCheckBox.setSelected(Boolean.TRUE.equals(p.ativo()));

            if (p.tipoProduto() != null) {
                tipoProduto.getSelectionModel().select(p.tipoProduto().toString());
            }

            atualizarPreview();
        } finally {
            carregandoEdicao = false;
        }
    }

    private void atualizarPreview() {
        String sku = skuField.getText() != null ? skuField.getText() : "-";
        String tipo = tipoProduto.getValue() != null ? tipoProduto.getValue() : "-";
        String marca = marcaField.getText() != null ? marcaField.getText() : "-";
        String modelo = modeloField.getText() != null ? modeloField.getText() : "-";
        String preco = (precoUnitarioField != null && !precoUnitarioField.getText().isEmpty())
                ? precoUnitarioField.getText() : "-";
        String estoque = (totalRecebidoField != null && !totalRecebidoField.getText().isEmpty())
                ? totalRecebidoField.getText() : "-";

        previewLabel.setText(String.format(
                "SKU: %s | Tipo: %s | Marca: %s | Modelo: %s | Preço: € %s | Estoque: %s",
                sku, tipo, marca, modelo, preco, estoque));
    }

    // ===== VALIDAÇÃO =====
    private boolean validarFormulario() {
        Map<String, String> erros = validator.validarCamposObrigatorios(
                skuField, tipoProduto, categoriaField, marcaField, modeloField);

        if (precoUnitarioField != null && !precoUnitarioField.getText().trim().isEmpty()) {
            try {
                new BigDecimal(precoUnitarioField.getText().trim());
            } catch (NumberFormatException e) {
                erros.put("preco", "Preço inválido (use 0.00)");
            }
        }
        if (ivaField != null && !ivaField.getText().trim().isEmpty()) {
            try {
                new BigDecimal(ivaField.getText().trim());
            } catch (NumberFormatException e) {
                erros.put("iva", "IVA inválido (use 23 ou 23.00)");
            }
        }
        if (totalRecebidoField != null && !totalRecebidoField.getText().trim().isEmpty()) {
            try {
                Integer.parseInt(totalRecebidoField.getText().trim());
            } catch (NumberFormatException e) {
                erros.put("estoque", "Estoque inválido (use número inteiro)");
            }
        }

        if (erros.isEmpty()) {
            mensagemErroLabel.setText("");
            return true;
        }
        mensagemErroLabel.setText(String.join(" • ", erros.values()));
        return false;
    }

    // ===== HANDLERS (nome = fx:id) =====
    public void btn_salvar(ActionEvent event) {
        if (validarFormulario()) salvarProduto();
    }

    public void btn_cancelar(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        if (stage != null) stage.close();
    }

    @FXML
    public void btn_anexarFatura(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Selecionar Fatura de Compra");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fatura (PDF/Imagem)", EXTENSOES_FATURA));
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Todos os arquivos", "*.*"));

        javafx.stage.Window owner = (btn_anexarFatura != null && btn_anexarFatura.getScene() != null)
                ? btn_anexarFatura.getScene().getWindow()
                : null;
        File arquivo = chooser.showOpenDialog(owner);
        if (arquivo == null) return;

        try {
            long tamanho = arquivo.length();
            if (tamanho > MAX_FATURA_BYTES) {
                mensagemErroLabel.setText("Fatura maior que 2MB (" +
                        String.format("%.1f MB", tamanho / (1024.0 * 1024.0)) + "). Escolha outro arquivo.");
                return;
            }
            faturaBytes = Files.readAllBytes(arquivo.toPath());
            faturaNome = arquivo.getName();
            atualizarFeedbackFatura();
            mensagemErroLabel.setText("");
        } catch (Exception e) {
            logger.log(System.Logger.Level.ERROR,
                    "❌ Falha ao ler fatura: {0}", e.getMessage(), e);
            mensagemErroLabel.setText("Falha ao ler arquivo: " + e.getMessage());
        }
    }

    @FXML
    public void btn_removerFatura(ActionEvent event) {
        faturaBytes = null;
        faturaNome = null;
        atualizarFeedbackFatura();
    }

    private void atualizarFeedbackFatura() {
        if (lblFaturaSelecionada == null) return;
        if (faturaBytes == null || faturaBytes.length == 0) {
            lblFaturaSelecionada.setText("Nenhum arquivo anexado");
            if (btn_removerFatura != null) btn_removerFatura.setDisable(true);
        } else {
            double kb = faturaBytes.length / 1024.0;
            lblFaturaSelecionada.setText(
                    (faturaNome != null ? faturaNome : "fatura") +
                            String.format(" (%.1f KB)", kb));
            if (btn_removerFatura != null) btn_removerFatura.setDisable(false);
        }
    }

    // ===== SALVAMENTO =====
    /**
     * Modo novo: salvar direto (SKU gerado + id criado no banco).
     * Modo edição: atualizar por SKU — a fronteira resolve SKU → id.
     */
    private void salvarProduto() {
        CatalogoProdutos produto = criarProdutoBase();
        boolean edicao = emModoEdicao;
        mostrarCarregamento(true);

        CompletableFuture
                .supplyAsync(() -> Rotas.put(
                        edicao ? "catalogo-produtos/service/atualizar/por/sku"
                                : "catalogo-produtos/service/salvar",
                        Params.with("produto", produto)))
                .whenComplete((resp, erro) -> Platform.runLater(() -> {
                    mostrarCarregamento(false);
                    if (erro != null) {
                        mensagemErroLabel.setText("Erro ao salvar: " + erro.getMessage());
                    } else if (!resp.isSuccess()) {
                        mensagemErroLabel.setText(resp.getFirstError());
                    } else {
                        Rotas.exec("catalogo/list/refresh");
                        setModoNovo();
                    }
                }));
    }

    private CatalogoProdutos criarProdutoBase() {
        return CatalogoProdutos.novoCompleto(
                skuField.getText().trim(),
                TipoProduto.fromString(tipoProduto.getValue()),
                categoriaField.getText().trim(),
                marcaField.getText().trim(),
                modeloField.getText().trim(),
                corField.getText(),
                descricaoField.getText(),
                caracteristicasField.getText(),
                parsePrecoUnitarioCentavos(),
                parseIvaBasisPoints(),
                parseTotalRecebido(),
                fornecedorField != null ? fornecedorField.getText() : null,
                numeroFaturaField != null ? numeroFaturaField.getText() : null,
                faturaBytes
        );
    }

    private Integer parsePrecoUnitarioCentavos() {
        String texto = precoUnitarioField.getText();
        if (texto == null || texto.isBlank()) return null;
        try {
            return new BigDecimal(texto.trim())
                    .multiply(BigDecimal.valueOf(100)).intValueExact();
        } catch (NumberFormatException | ArithmeticException e) {
            return 0;
        }
    }

    private Integer parseIvaBasisPoints() {
        if (ivaField == null) return null;
        String texto = ivaField.getText();
        if (texto == null || texto.isBlank()) return null;
        try {
            return new BigDecimal(texto.trim())
                    .multiply(BigDecimal.valueOf(100)).intValueExact();
        } catch (NumberFormatException | ArithmeticException e) {
            return 0;
        }
    }

    private Integer parseTotalRecebido() {
        String texto = totalRecebidoField.getText();
        if (texto == null || texto.isBlank()) return null;
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String formatarCentavosParaEuros(Integer centavos) {
        if (centavos == null) return "";
        return String.format("%d.%02d", centavos / 100, Math.abs(centavos % 100));
    }

    private String formatarIvaBpParaPercent(Integer bp) {
        if (bp == null) return "";
        return String.format("%d.%02d", bp / 100, Math.abs(bp % 100));
    }

    private void mostrarCarregamento(boolean carregando) {
        btn_salvar.setDisable(carregando);
        btn_cancelar.setDisable(carregando);
        btn_salvar.setText(carregando
                ? "Processando..."
                : (emModoEdicao ? "Atualizar Produto" : "Salvar Produto"));
    }

    private void limparFormulario() {
        skuField.clear();
        tipoProduto.setValue(null);
        categoriaField.clear();
        marcaField.clear();
        modeloField.clear();
        corField.clear();
        if (fornecedorField != null) fornecedorField.clear();
        if (numeroFaturaField != null) numeroFaturaField.clear();
        faturaBytes = null;
        faturaNome = null;
        atualizarFeedbackFatura();
        if (precoUnitarioField != null) precoUnitarioField.clear();
        if (ivaField != null) ivaField.clear();
        if (totalRecebidoField != null) totalRecebidoField.clear();
        descricaoField.clear();
        caracteristicasField.clear();
        ativoCheckBox.setSelected(true);
        if (idGeradoContainer != null) {
            idGeradoContainer.setVisible(false);
            idGeradoContainer.setManaged(false);
        }
    }
}