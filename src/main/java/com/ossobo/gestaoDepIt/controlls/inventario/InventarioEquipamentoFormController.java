package com.ossobo.gestaoDepIt.controlls.inventario;

import com.ossobo.gestaoDepIt.config.ViewConstant;
import com.ossobo.gestaoDepIt.db.enums.CondicaoEquipamento;
import com.ossobo.gestaoDepIt.db.enums.StatusEquipamento;
import com.ossobo.gestaoDepIt.db.models.CatalogoProdutos;
import com.ossobo.gestaoDepIt.db.models.Funcionarios;
import com.ossobo.gestaoDepIt.db.models.InventarioEquipamentos;
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
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * InventarioEquipamentoFormController v5.2
 *
 * Formulário de criação/edição de equipamento do inventário.
 *
 * v5.2 — Correções de robustez:
 *        - Logs estruturados nos carregamentos de combo (silent-return virava
 *          combo vazio sem explicação).
 *        - try/catch em btn_salvar, carregarParaEdicao e nos carregadores de
 *          combo — evita travar em "Processando..." quando a rota lança.
 *        - btnGerarNumSerie envia a marca ao gerador ANTES de abrir a janela
 *          (o GerarNumSerieController espera via @GetMapping("marca")).
 *        - Null-safe em getDataList (evita NPE quando a rota devolve vazio).
 *
 * v5.1 — Cumpre Regra de Ouro #3 e #10 do WinterFX (binding por fx:id).
 * v5.0 — Geração e anexação dos termos (entrega + devolução).
 *
 * @since v5.2
 */
@Controller(proxy = false)
@RequestMapping("inventario-form")
@RegisterView(
        id = ViewConstant.Inventario.FORM,
        fxml = "/META-INF/gestaoDepIt/fxmls/inventario/InventarioEquipamentoFormView.fxml",
        title = "Equipamento",
        primaryCss = "/META-INF/gestaoDepIt/css/inventario/InventarioEquipamentoFormView.css"
)
public class InventarioEquipamentoFormController implements Initializable, WinterFXController {

    private static final System.Logger logger =
            System.getLogger(InventarioEquipamentoFormController.class.getName());

    // ===== FXML — Identificação / Localização / Status =====
    @FXML private VBox   headerContainer;
    @FXML private Label  labelTitulo;

    @FXML private ComboBox<CatalogoProdutos> comboCatalogo;
    @FXML private ComboBox<Funcionarios>     comboFuncionario;
    @FXML private ComboBox<String>           comboStatus;
    @FXML private ComboBox<String>           comboCondicao;

    @FXML private TextField txtNumSerie;
    @FXML private TextField txtEnderecoMac;
    @FXML private TextField txtLocalizacao;
    @FXML private TextField txtDepartamento;
    @FXML private TextField txtNumeroFatura;
    @FXML private TextField txtObservacoes;

    @FXML private DatePicker dateAquisicao;
    @FXML private DatePicker dateInstalacao;
    @FXML private DatePicker dateUltimaVerificacao;

    @FXML private CheckBox   chkDevolucao;

    // ===== FXML — Documentos =====
    @FXML private VBox    secaoDocEntrega;
    @FXML private Button  btnGerarEntrega;
    @FXML private Button  btnSelecionarEntrega;
    @FXML private Button  btnLimparEntrega;
    @FXML private Label   lblStatusEntrega;

    @FXML private VBox    secaoDocDevolucao;
    @FXML private Button  btnGerarDevolucao;
    @FXML private Button  btnSelecionarDevolucao;
    @FXML private Button  btnLimparDevolucao;
    @FXML private Label   lblStatusDevolucao;

    // ===== FXML — Sistema =====
    @FXML private VBox       sistemaInfoSection;
    @FXML private TextField  txtId;
    @FXML private TextField  txtCreatedAt;
    @FXML private TextField  txtUpdatedAt;

    @FXML private Button     btn_cancelar;
    @FXML private Button     btn_salvar;

    // ===== ESTADO =====
    private String equipamentoId;
    private InventarioEquipamentos equipamentoExistente;

    private byte[] documentoEntregaBytes;
    private byte[] documentoDevolucaoBytes;

    private CompletableFuture<Void> combosProntos = CompletableFuture.completedFuture(null);

    @FloatingWindow(
            viewId = ViewConstant.Inventario.GERAR_NUM_SERIE,
            singleton = false
    )
    private StageForFloatingWindow gerarNumSerieForm;

    // ===== INITIALIZE =====
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(this::inicializarComponentes);
    }

    private void inicializarComponentes() {
        configurarCombosFixos();
        configurarDateAquisicaoReadonly();
        combosProntos = CompletableFuture.allOf(
                carregarComboCatalogo(),
                carregarComboFuncionario()
        );
        configurarListeners();
        configurarListenersDocumentos();
        setModoNovo();
    }

    private void configurarCombosFixos() {
        comboStatus.getItems().setAll(StatusEquipamento.todos());
        comboCondicao.getItems().setAll(CondicaoEquipamento.todos());
    }

    private void configurarDateAquisicaoReadonly() {
        dateAquisicao.setEditable(false);
        dateAquisicao.setDisable(false);
        dateAquisicao.getEditor().setEditable(false);
        dateAquisicao.getEditor().setFocusTraversable(false);
        dateAquisicao.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED,
                javafx.scene.input.MouseEvent::consume);
    }

    // ============================================================
    // CARREGAMENTO DE COMBOS (v5.2: try/catch + log + null-safe)
    // ============================================================

    private CompletableFuture<Void> carregarComboCatalogo() {
        return CompletableFuture.runAsync(() -> {
            try {
                var resp = Rotas.get("catalogo-produtos/service/todos");
                if (resp == null || !resp.isSuccess()) {
                    logger.log(System.Logger.Level.WARNING,
                            "Falha ao carregar catálogo: {0}",
                            resp != null ? resp.getFirstError() : "resposta nula");
                    return;
                }
                List<CatalogoProdutos> produtos = resp.getDataList("produtos");
                final List<CatalogoProdutos> lista = produtos != null ? produtos : List.of();

                Platform.runLater(() -> {
                    comboCatalogo.getItems().setAll(lista);
                    comboCatalogo.setCellFactory(p -> new ListCell<>() {
                        @Override protected void updateItem(CatalogoProdutos c, boolean vazio) {
                            super.updateItem(c, vazio);
                            setText(vazio || c == null ? null
                                    : c.sku() + " — " + c.marca() + " " + c.modelo());
                        }
                    });
                    comboCatalogo.setButtonCell(new ListCell<>() {
                        @Override protected void updateItem(CatalogoProdutos c, boolean vazio) {
                            super.updateItem(c, vazio);
                            setText(vazio || c == null ? null
                                    : c.sku() + " — " + c.marca() + " " + c.modelo());
                        }
                    });
                    reaplicarSelecaoSeEdicao();
                });
            } catch (Exception e) {
                logger.log(System.Logger.Level.ERROR,
                        "❌ Erro ao carregar combo de catálogo: {0}", e.getMessage(), e);
            }
        });
    }

    private CompletableFuture<Void> carregarComboFuncionario() {
        return CompletableFuture.runAsync(() -> {
            try {
                var resp = Rotas.get("funcionarios/service/todos");
                if (resp == null || !resp.isSuccess()) {
                    logger.log(System.Logger.Level.WARNING,
                            "Falha ao carregar funcionários: {0}",
                            resp != null ? resp.getFirstError() : "resposta nula");
                    return;
                }
                List<Funcionarios> funcs = resp.getDataList("funcionarios");
                final List<Funcionarios> lista = funcs != null ? funcs : List.of();

                Platform.runLater(() -> {
                    comboFuncionario.getItems().setAll(lista);
                    comboFuncionario.setCellFactory(p -> new ListCell<>() {
                        @Override protected void updateItem(Funcionarios f, boolean vazio) {
                            super.updateItem(f, vazio);
                            setText(vazio || f == null ? null
                                    : f.codDep() + " — " + f.nome());
                        }
                    });
                    comboFuncionario.setButtonCell(new ListCell<>() {
                        @Override protected void updateItem(Funcionarios f, boolean vazio) {
                            super.updateItem(f, vazio);
                            setText(vazio || f == null ? null
                                    : f.codDep() + " — " + f.nome());
                        }
                    });
                    reaplicarSelecaoSeEdicao();
                });
            } catch (Exception e) {
                logger.log(System.Logger.Level.ERROR,
                        "❌ Erro ao carregar combo de funcionários: {0}", e.getMessage(), e);
            }
        });
    }

    private void configurarListeners() {
        comboCatalogo.valueProperty().addListener((obs, antigo, novo) -> {
            if (novo == null) { dateAquisicao.setValue(null); return; }
            dateAquisicao.setValue(dataAquisicaoDe(novo));
        });

        comboFuncionario.valueProperty().addListener((obs, antigo, novo) -> {
            if (novo == null) return;
            if (isBlank(txtDepartamento)) txtDepartamento.setText(nvl(novo.departamento()));
            if (isBlank(txtLocalizacao))   txtLocalizacao.setText(nvl(novo.localTrabalho()));
        });
    }

    private void configurarListenersDocumentos() {
        chkDevolucao.selectedProperty().addListener((obs, antigo, novo) -> {
            secaoDocDevolucao.setVisible(Boolean.TRUE.equals(novo));
            secaoDocDevolucao.setManaged(Boolean.TRUE.equals(novo));
        });
        secaoDocDevolucao.setVisible(false);
        secaoDocDevolucao.setManaged(false);
    }

    private LocalDate dataAquisicaoDe(CatalogoProdutos produto) {
        return produto.createdAt() != null ? produto.createdAt().toLocalDate() : null;
    }

    // ===== MODOS =====
    public void setModoNovo() {
        this.equipamentoId = null;
        this.equipamentoExistente = null;
        this.documentoEntregaBytes = null;
        this.documentoDevolucaoBytes = null;

        labelTitulo.setText("Novo Equipamento no Inventário");
        limparFormulario();
        comboStatus.setValue(StatusEquipamento.PADRAO.name());
        comboCondicao.setValue(CondicaoEquipamento.PADRAO.name());
        dateInstalacao.setValue(LocalDate.now());
        dateUltimaVerificacao.setValue(LocalDate.now());
        sistemaInfoSection.setVisible(false);
        sistemaInfoSection.setManaged(false);
        btn_salvar.setText("Salvar Equipamento");

        aplicarModoDocumentos(null, null);
    }

    // ===== ROTA DE ENTRADA (v5.2: try/catch) =====
    @GetMapping("editar/id")
    public ResponseData carregarParaEdicao(@RouteVar("id") String id) {
        CompletableFuture.runAsync(() -> {
            try {
                combosProntos.join();
                var resp = Rotas.get("inventario-equipamentos/service/por/id",
                        Params.with("id", id));
                if (resp == null || !resp.isSuccess()) {
                    Platform.runLater(() -> mensagemErro(
                            resp != null ? resp.getMessage() : "Falha ao consultar equipamento."));
                    return;
                }
                InventarioEquipamentos e = resp.getData("equipamento", InventarioEquipamentos.class);
                if (e == null) {
                    Platform.runLater(() -> mensagemErro("Equipamento não encontrado."));
                    return;
                }
                Platform.runLater(() -> preencherFormulario(e));
            } catch (Exception ex) {
                logger.log(System.Logger.Level.ERROR,
                        "❌ Erro ao carregar equipamento para edição: {0}", ex.getMessage(), ex);
                Platform.runLater(() -> mensagemErro("Erro ao carregar: " + ex.getMessage()));
            }
        });
        return ResponseData.success();
    }

    @ExecMapping("numserie/aplicar")
    public ResponseData aplicarNumSerie(@RouteVar("numeroSerie") String numero) {
        Platform.runLater(() -> txtNumSerie.setText(numero));
        return ResponseData.success();
    }

    private void preencherFormulario(InventarioEquipamentos e) {
        this.equipamentoId = e.id();
        this.equipamentoExistente = e;
        this.documentoEntregaBytes = e.documentoEntrega();
        this.documentoDevolucaoBytes = e.documentoDevolucao();

        labelTitulo.setText("Editar Equipamento");
        btn_salvar.setText("Atualizar Equipamento");

        aplicarSelecaoCombos(e);

        txtNumSerie.setText(nvl(e.numSerie()));
        txtEnderecoMac.setText(nvl(e.enderecoMac()));
        txtLocalizacao.setText(nvl(e.localizacao()));
        txtDepartamento.setText(nvl(e.departamento()));
        txtNumeroFatura.setText(nvl(e.numeroFatura()));
        txtObservacoes.setText(nvl(e.observacoes()));

        if (comboCatalogo.getValue() != null) {
            dateAquisicao.setValue(dataAquisicaoDe(comboCatalogo.getValue()));
        } else {
            dateAquisicao.setValue(e.dataAquisicao());
        }

        dateInstalacao.setValue(e.dataInstalacao());
        dateUltimaVerificacao.setValue(e.dataUltimaVerificacao());

        comboStatus.setValue(e.status());
        comboCondicao.setValue(e.condicao());
        chkDevolucao.setSelected(Boolean.TRUE.equals(e.devolucao()));

        sistemaInfoSection.setVisible(true);
        sistemaInfoSection.setManaged(true);
        txtId.setText(nvl(e.id()));
        txtCreatedAt.setText(e.createdAt() != null ? e.createdAt().toString() : "");
        txtUpdatedAt.setText(e.updatedAt() != null ? e.updatedAt().toString() : "");

        aplicarModoDocumentos(e.documentoEntrega(), e.documentoDevolucao());
    }

    private void aplicarSelecaoCombos(InventarioEquipamentos e) {
        if (e == null) return;
        comboCatalogo.getItems().stream()
                .filter(p -> p.sku().equals(e.skuProduto()))
                .findFirst().ifPresent(comboCatalogo::setValue);
        comboFuncionario.getItems().stream()
                .filter(f -> f.codDep().equals(e.funcionarioId()))
                .findFirst().ifPresent(comboFuncionario::setValue);
    }

    private void reaplicarSelecaoSeEdicao() {
        if (equipamentoExistente != null) aplicarSelecaoCombos(equipamentoExistente);
    }

    // ===== DOCUMENTOS =====
    private void aplicarModoDocumentos(byte[] entregaGravada, byte[] devolucaoGravada) {
        boolean entregaBloqueada = entregaGravada != null && entregaGravada.length > 0;
        boolean devolucaoBloqueada = devolucaoGravada != null && devolucaoGravada.length > 0;

        btnGerarEntrega.setDisable(entregaBloqueada);
        btnSelecionarEntrega.setDisable(entregaBloqueada);
        btnLimparEntrega.setDisable(entregaBloqueada);

        btnGerarDevolucao.setDisable(devolucaoBloqueada);
        btnSelecionarDevolucao.setDisable(devolucaoBloqueada);
        btnLimparDevolucao.setDisable(devolucaoBloqueada);

        if (entregaBloqueada) {
            String quando = equipamentoExistente != null && equipamentoExistente.createdAt() != null
                    ? equipamentoExistente.createdAt().toString() : "—";
            lblStatusEntrega.setText("Documento já emitido em " + quando
                    + " (" + formatarTamanho(documentoEntregaBytes) + ")");
        } else {
            atualizarLabelEntrega();
        }

        if (devolucaoBloqueada) {
            String quando = equipamentoExistente != null && equipamentoExistente.createdAt() != null
                    ? equipamentoExistente.createdAt().toString() : "—";
            lblStatusDevolucao.setText("Documento já emitido em " + quando
                    + " (" + formatarTamanho(documentoDevolucaoBytes) + ")");
        } else {
            atualizarLabelDevolucao();
        }
    }

    private void atualizarLabelEntrega() {
        if (documentoEntregaBytes == null || documentoEntregaBytes.length == 0) {
            lblStatusEntrega.setText("Nenhum documento anexado.");
        } else {
            lblStatusEntrega.setText("Documento pronto: " + formatarTamanho(documentoEntregaBytes));
        }
    }

    private void atualizarLabelDevolucao() {
        if (documentoDevolucaoBytes == null || documentoDevolucaoBytes.length == 0) {
            lblStatusDevolucao.setText("Nenhum documento anexado.");
        } else {
            lblStatusDevolucao.setText("Documento pronto: " + formatarTamanho(documentoDevolucaoBytes));
        }
    }

    // ===== HANDLERS =====
    public void btnGerarEntrega(ActionEvent event) {
        gerarTermo(false);
    }

    public void btnGerarDevolucao(ActionEvent event) {
        gerarTermo(true);
    }

    public void btnSelecionarEntrega(ActionEvent event) {
        selecionarAssinado(false);
    }

    public void btnSelecionarDevolucao(ActionEvent event) {
        selecionarAssinado(true);
    }

    public void btnLimparEntrega(ActionEvent event) {
        documentoEntregaBytes = null;
        atualizarLabelEntrega();
    }

    public void btnLimparDevolucao(ActionEvent event) {
        documentoDevolucaoBytes = null;
        atualizarLabelDevolucao();
    }

    /**
     * v5.2 — Envia a marca do produto selecionado ao GerarNumSerieController
     * ANTES de abrir a janela. O gerador espera receber via @GetMapping("marca").
     */
    public void btnGerarNumSerie(ActionEvent event) {
        CatalogoProdutos produto = comboCatalogo.getValue();
        if (produto == null) {
            mensagemErro("Selecione um produto do catálogo antes de gerar o número de série.");
            return;
        }
        try {
            Rotas.get("gerar-num-serie/marca",
                    Params.with("marca", produto.marca()));
        } catch (Exception e) {
            logger.log(System.Logger.Level.WARNING,
                    "Falha ao enviar marca ao gerador de número de série: {0}", e.getMessage());
        }
        gerarNumSerieForm.show();
    }

    // ===== GERAÇÃO / SELEÇÃO DE TERMOS =====
    private void gerarTermo(boolean devolucao) {
        if (!validarParaGerar()) return;

        InventarioEquipamentos equip = montarEquipamento();
        String rota = devolucao
                ? "documento/service/gerar-devolucao"
                : "documento/service/gerar-entrega";
        String codDep = comboFuncionario.getValue().codDep();

        setBotoesDocumentoDesabilitados(true, devolucao);

        CompletableFuture.runAsync(() -> {
            try {
                var resp = Rotas.put(rota,
                        Params.with("equipamento", equip)
                                .and("funcionarioid", codDep));
                Platform.runLater(() -> {
                    setBotoesDocumentoDesabilitados(false, devolucao);
                    if (resp == null || !resp.isSuccess()) {
                        mensagemErro(resp != null
                                ? (resp.getFirstError() != null ? resp.getFirstError() : resp.getMessage())
                                : "Falha ao gerar documento.");
                        return;
                    }
                    byte[] pdf = resp.getData("pdf", byte[].class);
                    String nomeArquivo = resp.getData("nomeArquivo", String.class);
                    if (pdf == null || pdf.length == 0) {
                        mensagemErro("PDF vazio retornado pelo gerador.");
                        return;
                    }
                    salvarPdfComDialogo(pdf, nomeArquivo, devolucao);
                });
            } catch (Exception ex) {
                logger.log(System.Logger.Level.ERROR,
                        "❌ Erro ao gerar termo: {0}", ex.getMessage(), ex);
                Platform.runLater(() -> {
                    setBotoesDocumentoDesabilitados(false, devolucao);
                    mensagemErro("Erro ao gerar termo: " + ex.getMessage());
                });
            }
        });
    }

    private void salvarPdfComDialogo(byte[] pdf, String nomeArquivo, boolean devolucao) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Salvar termo de " + (devolucao ? "devolução" : "entrega"));
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        chooser.setInitialFileName(nomeArquivo != null ? nomeArquivo : "termo.pdf");

        File destino = chooser.showSaveDialog(janela());
        if (destino == null) return;

        try {
            Files.write(destino.toPath(), pdf);
        } catch (IOException ex) {
            mensagemErro("Falha ao gravar PDF: " + ex.getMessage());
        }
    }

    private void selecionarAssinado(boolean devolucao) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Selecionar termo de " + (devolucao ? "devolução" : "entrega") + " assinado");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF", "*.pdf"));

        File origem = chooser.showOpenDialog(janela());
        if (origem == null) return;

        try {
            byte[] bytes = Files.readAllBytes(origem.toPath());
            if (bytes.length == 0) {
                mensagemErro("Arquivo selecionado está vazio.");
                return;
            }
            if (devolucao) {
                documentoDevolucaoBytes = bytes;
                atualizarLabelDevolucao();
            } else {
                documentoEntregaBytes = bytes;
                atualizarLabelEntrega();
            }
        } catch (IOException ex) {
            mensagemErro("Falha ao ler o PDF: " + ex.getMessage());
        }
    }

    private void setBotoesDocumentoDesabilitados(boolean desabilitado, boolean devolucao) {
        if (devolucao) {
            btnGerarDevolucao.setDisable(desabilitado);
            btnSelecionarDevolucao.setDisable(desabilitado);
            btnLimparDevolucao.setDisable(desabilitado);
        } else {
            btnGerarEntrega.setDisable(desabilitado);
            btnSelecionarEntrega.setDisable(desabilitado);
            btnLimparEntrega.setDisable(desabilitado);
        }
    }

    private boolean validarParaGerar() {
        if (comboCatalogo.getValue() == null) { mensagemErro("Selecione um produto do catálogo."); return false; }
        if (comboFuncionario.getValue() == null) { mensagemErro("Selecione um funcionário responsável."); return false; }
        if (isBlank(txtNumSerie)) { mensagemErro("Número de série é obrigatório."); return false; }
        return true;
    }

    private String formatarTamanho(byte[] bytes) {
        if (bytes == null) return "0 B";
        int len = bytes.length;
        if (len < 1024) return len + " B";
        if (len < 1024 * 1024) return String.format("%.1f KB", len / 1024.0);
        return String.format("%.1f MB", len / (1024.0 * 1024.0));
    }

    // ===== SALVAR / CANCELAR (v5.2: try/catch) =====
    public void btn_salvar(ActionEvent event) {
        if (!validar()) return;

        InventarioEquipamentos equipamento = montarEquipamento();
        boolean edicao = equipamentoId != null;
        String rota = edicao
                ? "inventario-crud/service/atualizar"
                : "inventario-crud/service/cadastrar";

        btn_salvar.setDisable(true);
        btn_salvar.setText("Processando...");

        CompletableFuture.runAsync(() -> {
            try {
                var resp = Rotas.put(rota,
                        Params.with("equipamento", equipamento)
                                .and("funcionarioid", comboFuncionario.getValue().codDep())
                                .and("descricao", "{}"));

                Platform.runLater(() -> {
                    btn_salvar.setDisable(false);
                    btn_salvar.setText(edicao ? "Atualizar Equipamento" : "Salvar Equipamento");

                    if (resp == null || !resp.isSuccess()) {
                        mensagemErro(resp != null
                                ? (resp.getFirstError() != null ? resp.getFirstError() : resp.getMessage())
                                : "Falha ao salvar.");
                        return;
                    }
                    Rotas.exec("inventario-list/refresh");
                    fecharJanela();
                });
            } catch (Exception ex) {
                logger.log(System.Logger.Level.ERROR,
                        "❌ Erro ao salvar equipamento: {0}", ex.getMessage(), ex);
                Platform.runLater(() -> {
                    btn_salvar.setDisable(false);
                    btn_salvar.setText(edicao ? "Atualizar Equipamento" : "Salvar Equipamento");
                    mensagemErro("Erro ao salvar: " + ex.getMessage());
                });
            }
        });
    }

    public void btn_cancelar(ActionEvent event) {
        fecharJanela();
    }

    // ===== MONTAGEM =====
    private InventarioEquipamentos montarEquipamento() {
        CatalogoProdutos catalogo = comboCatalogo.getValue();
        Funcionarios funcionario = comboFuncionario.getValue();

        String id = edicao() ? equipamentoExistente.id() : UUID.randomUUID().toString();
        LocalDate aquisicao = catalogo != null
                ? dataAquisicaoDe(catalogo)
                : dateAquisicao.getValue();

        byte[] entrega = documentoEntregaBytes;
        byte[] devolucao = chkDevolucao.isSelected() ? documentoDevolucaoBytes : null;

        return new InventarioEquipamentos(
                id,
                catalogo != null ? catalogo.sku() : null,
                funcionario != null ? funcionario.codDep() : null,
                txtNumSerie.getText(),
                txtEnderecoMac.getText(),
                aquisicao,
                dateInstalacao.getValue(),
                dateUltimaVerificacao.getValue(),
                txtNumeroFatura.getText(),
                txtLocalizacao.getText(),
                txtDepartamento.getText(),
                comboStatus.getValue(),
                comboCondicao.getValue(),
                entrega,
                devolucao,
                txtObservacoes.getText(),
                chkDevolucao.isSelected(),
                edicao() ? equipamentoExistente.createdAt() : null,
                null,
                edicao() ? equipamentoExistente.deviceId() : null,
                false
        );
    }

    private boolean edicao() { return equipamentoId != null; }

    // ===== VALIDAÇÃO =====
    private boolean validar() {
        if (comboCatalogo.getValue() == null) { mensagemErro("Selecione um produto do catálogo."); return false; }
        if (comboFuncionario.getValue() == null) { mensagemErro("Selecione um funcionário responsável."); return false; }
        if (isBlank(txtNumSerie)) { mensagemErro("Número de série é obrigatório."); return false; }
        if (dateAquisicao.getValue() == null) { mensagemErro("Data de aquisição é obrigatória."); return false; }
        if (isBlank(txtLocalizacao)) { mensagemErro("Localização é obrigatória."); return false; }
        if (comboStatus.getValue() == null) { mensagemErro("Status é obrigatório."); return false; }
        if (comboCondicao.getValue() == null) { mensagemErro("Condição é obrigatória."); return false; }

        if (documentoEntregaBytes == null || documentoEntregaBytes.length == 0) {
            mensagemErro("O termo de entrega assinado é obrigatório.");
            return false;
        }

        if (chkDevolucao.isSelected()
                && (documentoDevolucaoBytes == null || documentoDevolucaoBytes.length == 0)) {
            mensagemErro("O termo de devolução assinado é obrigatório.");
            return false;
        }
        return true;
    }

    private void mensagemErro(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Validação");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private boolean isBlank(TextField f) {
        return f.getText() == null || f.getText().isBlank();
    }

    private void limparFormulario() {
        comboCatalogo.setValue(null);
        comboFuncionario.setValue(null);
        comboStatus.setValue(null);
        comboCondicao.setValue(null);
        txtNumSerie.clear();
        txtEnderecoMac.clear();
        txtLocalizacao.clear();
        txtDepartamento.clear();
        txtNumeroFatura.clear();
        txtObservacoes.clear();
        dateAquisicao.setValue(null);
        dateInstalacao.setValue(null);
        dateUltimaVerificacao.setValue(null);
        chkDevolucao.setSelected(false);
        txtId.clear();
        txtCreatedAt.clear();
        txtUpdatedAt.clear();
        lblStatusEntrega.setText("Nenhum documento anexado.");
        lblStatusDevolucao.setText("Nenhum documento anexado.");
    }

    private Stage janela() {
        return btn_cancelar != null && btn_cancelar.getScene() != null
                ? (Stage) btn_cancelar.getScene().getWindow()
                : null;
    }

    private void fecharJanela() {
        Stage s = janela();
        if (s != null) s.close();
    }

    private String nvl(String s) { return s != null ? s : ""; }

    public void cleanup() {
        equipamentoExistente = null;
        equipamentoId = null;
        documentoEntregaBytes = null;
        documentoDevolucaoBytes = null;
    }
}