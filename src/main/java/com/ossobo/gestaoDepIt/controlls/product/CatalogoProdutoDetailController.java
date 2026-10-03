package com.ossobo.gestaoDepIt.controlls.product;

import com.ossobo.gestaoDepIt.config.ViewConstant;
import com.ossobo.gestaoDepIt.db.models.CatalogoProdutos;
import com.ossobo.winterfx.anotations.*;
import com.ossobo.winterfx.router.Rotas;
import com.ossobo.winterfx.router.model.Params;
import com.ossobo.winterfx.router.model.ResponseData;
import com.ossobo.winterfx.view.anotations.RegisterView;
import com.ossobo.winterfx.view.controller.WinterFXController;
import com.ossobo.winterfx.view.floatingwindow.StageForFloatingWindow;
import com.ossobo.winterfx.view.floatingwindow.anotations.FloatingWindow;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

/**
 * CatalogoProdutoDetailController v2.5
 *
 * Responsabilidade: Exibir detalhes do produto e ações rápidas
 *                   (estoque, preço, status, editar, fatura).
 *
 * v2.5 — Download/visualização da fatura de compra (byte[]):
 *        - btn_baixarFatura: FileChooser + Files.write, extensão por magic bytes
 *        - btn_abrirFatura: extrai para tmp e abre no visualizador do SO
 *        - mostrarErro / mostrarInfo implementados com Alert
 *
 * v2.4 — Correções de integração com o WinterFX 20.0.1:
 *        - @PutMapping em setProdutoSku (list controller dispara via Rotas.put).
 *        - Handlers renomeados para o nome do fx:id.
 *        - btn_voltar dispara Rotas.put("catalogo/list/refresh") antes de fechar.
 *
 * @since v2.1
 */
@Controller(proxy = false)
@RequestMapping("catalogo/details")
@RegisterView(
        id = ViewConstant.Catalogo.DETAIL,
        fxml = "/META-INF/gestaoDepIt/fxmls/catalogo_produto/CatalogoProdutoDetail.fxml",
        primaryCss = "/META-INF/gestaoDepIt/css/catalugoproduto/CatalogoProdutoDetail.css"
)
public class CatalogoProdutoDetailController implements Initializable, WinterFXController {

    private static final System.Logger logger =
            System.getLogger(CatalogoProdutoDetailController.class.getName());

    private CatalogoProdutos produto;
    private String produtoSku;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private volatile boolean fxmlCarregado = false;
    private final CompletableFuture<Void> fxmlCarregadoFuture = new CompletableFuture<>();

    // ===== FXML — HEADER =====
    @FXML private Button btn_voltar;
    @FXML private Button btn_editar;
    @FXML private Button btn_desativar;
    @FXML private Label  tituloLabel;
    @FXML private Label  subtituloLabel;

    // ===== FXML — CARD: Informações do Produto =====
    @FXML private Label detailSku;
    @FXML private Label detailTipo;
    @FXML private Label detailCategoria;
    @FXML private Label detailMarca;
    @FXML private Label detailModelo;
    @FXML private Label detailCor;
    @FXML private Label detailPrecoUnitario;
    @FXML private Label detailIva;
    @FXML private Label detailPrecoTotal;
    @FXML private Label detailEstoque;
    @FXML private Label detailStatus;
    @FXML private Label detailFornecedor;
    @FXML private Label detailNumeroFatura;

    // ===== FXML — CARD: Descrição / Características =====
    @FXML private TextArea detailDescricao;
    @FXML private TextArea detailCaracteristicas;

    // ===== FXML — CARD: Fatura de Compra =====
    @FXML private Label  detailFaturaStatus;
    @FXML private Button btn_baixarFatura;
    @FXML private Button btn_abrirFatura;

    // ===== FXML — CARD: Financeiro =====
    @FXML private Label statValorTotal;
    @FXML private Label statPrecoUnitario;
    @FXML private Label statIva;
    @FXML private Label statQuantidadeEstoque;

    // ===== FXML — CARD: Estatísticas =====
    @FXML private Label statEstoque;
    @FXML private Label statEquipamentos;
    @FXML private Label statMovimentacoes;
    @FXML private Label statToners;

    // ===== FXML — CARD: Datas =====
    @FXML private Label detailCriadoEm;
    @FXML private Label detailAtualizadoEm;

    // ===== FXML — CARD: Ações Rápidas =====
    @FXML private Button btn_ajustarEstoque;
    @FXML private Button btn_atualizarPreco;
    @FXML private Button btn_movimentar;
    @FXML private Button btn_historico;

    // ===== FXML — Mensagem =====
    @FXML private Label mensagemStatusLabel;

    @FloatingWindow(
            viewId = ViewConstant.Catalogo.FORM,
            singleton = false
    )
    private StageForFloatingWindow formCatalogoProduto;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (detailDescricao != null) detailDescricao.setEditable(false);
        if (detailCaracteristicas != null) detailCaracteristicas.setEditable(false);
        fxmlCarregado = true;
        fxmlCarregadoFuture.complete(null);
    }

    // ===== ROTA DE ENTRADA (list controller dispara via Rotas.put) =====
    @GetMapping("detalhes/produto")
    public ResponseData setProdutoSku(@RouteVar("sku") String produtoSku) {
        this.produtoSku = produtoSku;
        fxmlCarregadoFuture.thenRun(() -> Platform.runLater(() -> carregarProduto(produtoSku)));
        return ResponseData.success();
    }

    private void carregarProduto(String sku) {
        mostrarCarregamento(true);

        CompletableFuture
                .supplyAsync(() -> Rotas.get("catalogo-produtos/service/por/sku",
                        Params.with("sku", sku)))
                .whenComplete((resp, erro) -> Platform.runLater(() -> {
                    mostrarCarregamento(false);

                    if (erro != null) { mostrarErro("Erro ao Carregar", erro.getMessage()); return; }
                    if (!resp.isSuccess()) { mostrarErro("Erro", resp.getFirstError()); return; }

                    CatalogoProdutos p = resp.getData("produto", CatalogoProdutos.class);
                    if (p != null) {
                        this.produto = p;
                        preencherDados(p);
                        atualizarInterface();
                    } else {
                        mostrarErro("Produto Não Encontrado", "SKU: " + sku);
                    }
                }));
    }

    private void preencherDados(CatalogoProdutos p) {
        detailSku.setText(p.sku());
        detailTipo.setText(p.tipoProduto() != null ? p.tipoProduto().toString() : "-");
        detailCategoria.setText(nullToDash(p.categoria()));
        detailMarca.setText(nullToDash(p.marca()));
        detailModelo.setText(nullToDash(p.modelo()));
        detailCor.setText(nullToDash(p.cor()));
        detailFornecedor.setText(nullToDash(p.fornecedor()));
        detailNumeroFatura.setText(nullToDash(p.numeroFatura()));

        Integer centavos = p.precoUnitarioCentavos();
        BigDecimal preco = centavos != null ? BigDecimal.valueOf(centavos, 2) : BigDecimal.ZERO;
        String precoStr = String.format("€ %.2f", preco);
        detailPrecoUnitario.setText(precoStr);
        if (statPrecoUnitario != null) statPrecoUnitario.setText(precoStr);

        String ivaStr = p.getIvaFormatado();
        detailIva.setText(ivaStr);
        if (statIva != null) statIva.setText(ivaStr);

        detailPrecoTotal.setText(p.getPrecoTotalFormatado());

        Integer estoque = p.totalRecebido();
        String estoqueStr = (estoque != null ? estoque : 0) + " unidades";
        detailEstoque.setText(estoqueStr);
        if (statQuantidadeEstoque != null) statQuantidadeEstoque.setText(estoqueStr);

        if (statValorTotal != null) {
            if (estoque != null && estoque > 0) {
                BigDecimal total = preco.multiply(BigDecimal.valueOf(estoque));
                statValorTotal.setText(String.format("€ %.2f", total));
            } else {
                statValorTotal.setText("€ 0,00");
            }
        }

        boolean ativo = Boolean.TRUE.equals(p.ativo());
        detailStatus.setText(ativo ? "Ativo" : "Inativo");
        detailStatus.getStyleClass().setAll(ativo ? "status-ativo" : "status-inativo");

        detailDescricao.setText(p.descricao() != null ? p.descricao() : "Sem descrição.");
        detailCaracteristicas.setText(p.caracteristicasTecnicas() != null ? p.caracteristicasTecnicas() : "Nenhuma.");

        detailCriadoEm.setText(p.createdAt() != null ? p.createdAt().format(dateFormatter) : "-");
        detailAtualizadoEm.setText(p.updatedAt() != null ? p.updatedAt().format(dateFormatter) : "-");

        tituloLabel.setText("Detalhes do Produto");
        subtituloLabel.setText(String.format("SKU: %s | %s %s",
                p.sku(), nullToDash(p.marca()), nullToDash(p.modelo())));

        // ===== Fatura =====
        boolean temFatura = p.faturaCompra() != null && p.faturaCompra().length > 0;
        if (detailFaturaStatus != null) {
            detailFaturaStatus.setText(temFatura
                    ? String.format("Anexo (%s)", formatarTamanho(p.faturaCompra().length))
                    : "Sem anexo");
        }
        if (btn_baixarFatura != null) btn_baixarFatura.setDisable(!temFatura);
        if (btn_abrirFatura != null) btn_abrirFatura.setDisable(!temFatura);

        if (statEstoque != null) statEstoque.setText("Em Estoque: " + (estoque != null ? estoque : 0) + " unidades");
        if (statEquipamentos != null) statEquipamentos.setText("Equipamentos: -");
        if (statMovimentacoes != null) statMovimentacoes.setText("Movimentações: -");
        if (statToners != null) statToners.setText("Toners: -");
    }

    private String nullToDash(String s) { return s != null ? s : "-"; }

    private void atualizarInterface() {
        if (produto == null) return;
        boolean ativo = Boolean.TRUE.equals(produto.ativo());

        btn_desativar.setText(ativo ? "Desativar" : "Ativar");
        btn_desativar.getStyleClass().setAll(ativo ? "btn-danger" : "btn-success");

        btn_ajustarEstoque.setDisable(!ativo);
        btn_atualizarPreco.setDisable(!ativo);
        btn_movimentar.setDisable(!ativo);
    }

    // ===== HANDLERS (nome = fx:id) =====

    public void btn_voltar(ActionEvent event) {
        fecharJanela();
    }

    public void btn_editar(ActionEvent event) {
        formCatalogoProduto.show();
        if (produto == null) return;
        Rotas.get("catalogo/produtoform/form/editar/sku",
                Params.with("sku", produto.sku()));
    }

    public void btn_desativar(ActionEvent event) {
        if (produto == null) return;
        executarToggleStatus(Boolean.TRUE.equals(produto.ativo()));
    }

    public void btn_ajustarEstoque(ActionEvent event) {
        if (produto == null) return;

        String atual = produto.totalRecebido() != null ? String.valueOf(produto.totalRecebido()) : "0";
        TextInputDialog dialog = new TextInputDialog(atual);
        dialog.setTitle("Ajustar Estoque");
        dialog.setHeaderText("Produto: " + produto.sku() + " - " + produto.modelo());
        dialog.setContentText("Nova quantidade:");

        dialog.showAndWait().ifPresent(valor -> {
            try {
                int qtd = Integer.parseInt(valor);
                if (qtd < 0) throw new NumberFormatException();
                ajustarEstoque(qtd);
            } catch (NumberFormatException e) {
                mostrarErro("Valor inválido", "Informe um número inteiro >= 0.");
            }
        });
    }

    public void btn_atualizarPreco(ActionEvent event) {
        if (produto == null) return;

        Integer centavos = produto.precoUnitarioCentavos();
        String atual = centavos != null
                ? String.format("%.2f", centavos / 100.0)
                : "0.00";
        TextInputDialog dialog = new TextInputDialog(atual);
        dialog.setTitle("Atualizar Preço");
        dialog.setHeaderText("Produto: " + produto.sku() + " - " + produto.modelo());
        dialog.setContentText("Novo preço (€):");

        dialog.showAndWait().ifPresent(valor -> {
            try {
                BigDecimal preco = new BigDecimal(valor.replace(",", "."));
                if (preco.compareTo(BigDecimal.ZERO) < 0) throw new NumberFormatException();
                atualizarPreco(preco);
            } catch (NumberFormatException e) {
                mostrarErro("Valor inválido", "Informe um preço válido (ex: 59.90).");
            }
        });
    }

    public void btn_movimentar(ActionEvent event) {
        mostrarInfo("Em desenvolvimento", "Funcionalidade disponível em breve.");
    }

    public void btn_historico(ActionEvent event) {
        mostrarInfo("Em desenvolvimento", "Funcionalidade disponível em breve.");
    }

    // ===== HANDLERS — FATURA =====

    public void btn_baixarFatura(ActionEvent event) {
        if (produto == null) return;
        byte[] bytes = produto.faturaCompra();
        if (bytes == null || bytes.length == 0) {
            mostrarInfo("Sem fatura", "Este produto não tem fatura anexada.");
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Salvar Fatura");
        chooser.setInitialFileName(sugerirNomeFatura(produto));

        javafx.stage.Window owner = (btn_baixarFatura != null && btn_baixarFatura.getScene() != null)
                ? btn_baixarFatura.getScene().getWindow()
                : null;
        File destino = chooser.showSaveDialog(owner);
        if (destino == null) return;

        try {
            Files.write(destino.toPath(), bytes);
            mostrarInfo("Fatura salva", "Arquivo gravado em:\n" + destino.getAbsolutePath());
        } catch (Exception e) {
            logger.log(System.Logger.Level.ERROR,
                    "❌ Falha ao salvar fatura: {0}", e.getMessage(), e);
            mostrarErro("Erro ao salvar", e.getMessage());
        }
    }

    public void btn_abrirFatura(ActionEvent event) {
        if (produto == null) return;
        byte[] bytes = produto.faturaCompra();
        if (bytes == null || bytes.length == 0) {
            mostrarInfo("Sem fatura", "Este produto não tem fatura anexada.");
            return;
        }

        try {
            String nome = sugerirNomeFatura(produto);
            Path tmp = Files.createTempFile("fatura_", "_" + nome);
            Files.write(tmp, bytes);
            tmp.toFile().deleteOnExit();

            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                Desktop.getDesktop().open(tmp.toFile());
            } else {
                mostrarInfo("Fatura extraída",
                        "Abrir automaticamente não é suportado. Arquivo em:\n" + tmp);
            }
        } catch (Exception e) {
            logger.log(System.Logger.Level.ERROR,
                    "❌ Falha ao abrir fatura: {0}", e.getMessage(), e);
            mostrarErro("Erro ao abrir", e.getMessage());
        }
    }

    // ===== AÇÕES =====
    private void ajustarEstoque(int novaQuantidade) {
        mostrarCarregamento(true);
        CompletableFuture
                .supplyAsync(() -> Rotas.put("catalogo-produtos/service/estoque/atualizar",
                        Params.with("sku", produto.sku())
                                .and("quantidade", novaQuantidade)))
                .whenComplete((resp, erro) -> Platform.runLater(() -> {
                    mostrarCarregamento(false);
                    if (erro != null) { mostrarErro("Erro", erro.getMessage()); return; }
                    if (!resp.isSuccess()) { mostrarErro("Erro", resp.getFirstError()); return; }
                    carregarProduto(produto.sku());
                }));
    }

    private void atualizarPreco(BigDecimal novoPreco) {
        mostrarCarregamento(true);
        int centavos = novoPreco.multiply(BigDecimal.valueOf(100)).intValueExact();
        CompletableFuture
                .supplyAsync(() -> Rotas.put("catalogo-produtos/service/preco/atualizar",
                        Params.with("sku", produto.sku())
                                .and("preco", String.valueOf(centavos / 100.0))
                                .and("iva", "")))
                .whenComplete((resp, erro) -> Platform.runLater(() -> {
                    mostrarCarregamento(false);
                    if (erro != null) { mostrarErro("Erro", erro.getMessage()); return; }
                    if (!resp.isSuccess()) { mostrarErro("Erro", resp.getFirstError()); return; }
                    carregarProduto(produto.sku());
                }));
    }

    private void executarToggleStatus(boolean ativoAtual) {
        mostrarCarregamento(true);
        String rota = ativoAtual
                ? "catalogo-produtos/service/desativar"
                : "catalogo-produtos/service/ativar";
        String sku = produto.sku();
        boolean novoStatus = !ativoAtual;

        CompletableFuture
                .supplyAsync(() -> Rotas.put(rota, Params.with("sku", sku)))
                .whenComplete((resp, erro) -> Platform.runLater(() -> {
                    mostrarCarregamento(false);
                    if (erro != null) { mostrarErro("Erro", erro.getMessage()); return; }
                    if (!resp.isSuccess()) { mostrarErro("Erro", resp.getFirstError()); return; }
                    produto = produto.comStatus(novoStatus);
                    preencherDados(produto);
                    atualizarInterface();
                }));
    }

    // ===== HELPERS — FATURA =====

    /** Sugere nome baseado no SKU + número da fatura. Extensão deduzida por magic bytes. */
    private String sugerirNomeFatura(CatalogoProdutos p) {
        String base = "fatura_" + (p.sku() != null ? p.sku() : "produto");
        if (p.numeroFatura() != null && !p.numeroFatura().isBlank()) {
            base += "_" + p.numeroFatura().replaceAll("[^a-zA-Z0-9_-]", "");
        }
        return base + extensaoPorMagicBytes(p.faturaCompra());
    }

    /** Deteta extensão pelos primeiros bytes (PDF, PNG, JPG). Fallback: .bin. */
    private String extensaoPorMagicBytes(byte[] bytes) {
        if (bytes == null || bytes.length < 4) return ".bin";
        // PDF: %PDF
        if (bytes[0] == 0x25 && bytes[1] == 0x50 && bytes[2] == 0x44 && bytes[3] == 0x46) return ".pdf";
        // PNG: \x89PNG
        if ((bytes[0] & 0xFF) == 0x89 && bytes[1] == 0x50 && bytes[2] == 0x4E && bytes[3] == 0x47) return ".png";
        // JPG: \xFF\xD8\xFF
        if ((bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xD8 && (bytes[2] & 0xFF) == 0xFF) return ".jpg";
        return ".bin";
    }

    /** "153 KB" / "1.2 MB" / "900 B". */
    private String formatarTamanho(int bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.0f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }

    // ===== UI =====
    private void mostrarCarregamento(boolean carregando) {
        if (!fxmlCarregado) return;
        if (btn_voltar != null) btn_voltar.setDisable(carregando);
        if (btn_editar != null) btn_editar.setDisable(carregando);
        if (btn_desativar != null) btn_desativar.setDisable(carregando);
    }

    private void mostrarErro(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    private void mostrarInfo(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    private void fecharJanela() {
        if (btn_voltar != null && btn_voltar.getScene() != null) {
            ((Stage) btn_voltar.getScene().getWindow()).close();
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