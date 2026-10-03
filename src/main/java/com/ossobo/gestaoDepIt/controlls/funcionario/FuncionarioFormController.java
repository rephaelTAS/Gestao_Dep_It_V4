package com.ossobo.gestaoDepIt.controlls.funcionario;

import com.ossobo.gestaoDepIt.config.ViewConstant;
import com.ossobo.gestaoDepIt.db.models.Funcionarios;
import com.ossobo.winterfx.anotations.Controller;
import com.ossobo.winterfx.anotations.GetMapping;
import com.ossobo.winterfx.anotations.RequestMapping;
import com.ossobo.winterfx.anotations.RouteVar;
import com.ossobo.winterfx.router.Rotas;
import com.ossobo.winterfx.router.model.Params;
import com.ossobo.winterfx.router.model.ResponseData;
import com.ossobo.winterfx.view.anotations.RegisterView;
import com.ossobo.winterfx.view.controller.WinterFXController;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

/**
 * FuncionarioFormController v2.4
 *
 * Controlador do formulário de criação/edição de funcionário.
 *
 * v2.4 — Correção de ordem de execução que apagava o preenchimento:
 *        - initialize() deixou de rodar dentro de Platform.runLater (o
 *          show() do FloatingWindow é síncrono — o runLater atrasava a
 *          inicialização e o setModoNovo() limpava o que já fora preenchido).
 *        - inicializarComponentes() só entra em modo novo se NÃO houver
 *          edição pendente (codDepPendenteEdicao).
 *        - setModoEdicao() chama aplicarEdicao() direto (os @FXML já estão
 *          injetados quando a rota chega).
 *
 * v2.3 — Transferência de departamento no mesmo botão:
 *        - Edição com MESMO depto: `atualizar` (cod_dep imutável).
 *        - Edição com depto DIFERENTE: `transferir-departamento` (gera novo
 *          cod_dep + propaga inventário + registra histórico).
 *
 * v2.2 — codDepPendenteEdicao.
 * v2.1 — Geração automática de cod_dep.
 * v2.0 — Migração para o padrão WinterFX + Rotas.
 *
 * @since v2.4
 */
@Controller(proxy = false)
@RequestMapping("funcionario-form")
@RegisterView(
        id = ViewConstant.Funcionario.FORM,
        fxml = "/META-INF/gestaoDepIt/fxmls/funcionario/FuncionarioForm.fxml",
        primaryCss = "/META-INF/gestaoDepIt/css/funcionario/funcionario-form.css"
)
public class FuncionarioFormController implements Initializable, WinterFXController {

    private static final System.Logger logger =
            System.getLogger(FuncionarioFormController.class.getName());

    private static final String[] DEPARTAMENTOS = {
            "Departamento Informático",
            "Departamento Industrial",
            "Departamento Financeiro",
            "Departamento Plantação",
            "Departamento Oficina",
            "Departamento RH",
            "Departamento Compras e Logística",
            "Direção Geral"
    };

    // ===== FXML =====
    @FXML private Label  tituloLabel;
    @FXML private TextField codDepField;
    @FXML private TextField nomeField;
    @FXML private TextField funcaoField;
    @FXML private ComboBox<String> departamentoCombo;
    @FXML private TextField localTrabalhoField;
    @FXML private TextField emailField;
    @FXML private TextField telefoneField;
    @FXML private CheckBox ativoCheckBox;
    @FXML private Label labelInfoImagem;
    @FXML private Label mensagemErroLabel;
    @FXML private ImageView imagePreview;
    @FXML private javafx.scene.control.Button btn_selecionarImagem;
    @FXML private javafx.scene.control.Button btn_removerImagem;
    @FXML private javafx.scene.control.Button btn_cancelar;
    @FXML private javafx.scene.control.Button btn_salvar;

    // ===== ESTADO =====
    private Funcionarios funcionario;
    private boolean modoEdicao = false;
    private byte[] imagemBytes;
    private String tipoImagem;
    private boolean carregandoEdicao = false;

    /**
     * codDep pendente de edição — fallback defensivo caso a rota chegue
     * ANTES do FXML carregar. Na prática, com FloatingWindow não-singleton,
     * o show() já injeta os @FXML e initialize() roda antes de setModoEdicao.
     */
    private String codDepPendenteEdicao = null;

    /**
     * Último cod_dep gerado como PREVIEW de transferência. Reservado para
     * uso futuro (banner de aviso, etc). O btn_salvar compara
     * departamentoCombo.getValue() com funcionario.departamento() — não
     * depende deste campo.
     */
    private String codDepPreviewTransferencia = null;

    // ===== INITIALIZE =====

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // initialize() já roda na FX thread. Platform.runLater aqui atrasava
        // a inicialização e o setModoNovo() limpava o preenchimento de edição
        // feito por setModoEdicao.
        inicializarComponentes();

        // Fallback: se a rota setModoEdicao chegou ANTES do FXML carregar,
        // aplica a edição pendente agora.
        if (codDepPendenteEdicao != null && !codDepPendenteEdicao.isBlank()) {
            String cod = codDepPendenteEdicao;
            codDepPendenteEdicao = null;
            aplicarEdicao(cod);
        }
    }

    private void inicializarComponentes() {
        carregarDepartamentos();
        configurarListenerDepartamento();
        ocultarErro();
        if (imagePreview != null) {
            imagePreview.setVisible(false);
            imagePreview.setManaged(false);
        }
        if (codDepField != null) {
            codDepField.setEditable(false);
            codDepField.setStyle("-fx-background-color: #f5f5f5; -fx-font-weight: bold;");
        }
        // Só entra em modo novo se NÃO houver edição pendente — caso contrário
        // limparia o formulário que setModoEdicao já preencheu.
        if (codDepPendenteEdicao == null || codDepPendenteEdicao.isBlank()) {
            setModoNovo();
        }
    }

    private void carregarDepartamentos() {
        if (departamentoCombo == null) return;
        departamentoCombo.getItems().setAll(DEPARTAMENTOS);
        departamentoCombo.getSelectionModel().selectFirst();
    }

    /**
     * Listener do departamento:
     *  - Modo novo: gera cod_dep do zero.
     *  - Modo edição, mesmo departamento: restaura o cod_dep atual.
     *  - Modo edição, departamento diferente: gera cod_dep novo (preview
     *    da transferência).
     */
    private void configurarListenerDepartamento() {
        if (departamentoCombo == null || codDepField == null) return;

        departamentoCombo.valueProperty().addListener((obs, antigo, novo) -> {
            if (carregandoEdicao) return;

            if (novo == null || novo.isBlank()) {
                codDepField.clear();
                codDepPreviewTransferencia = null;
                return;
            }

            if (modoEdicao && funcionario != null && novo.equals(funcionario.departamento())) {
                codDepPreviewTransferencia = null;
                codDepField.setText(funcionario.codDep());
                return;
            }

            gerarCodDep(novo);
        });
    }

    private void gerarCodDep(String departamento) {
        CompletableFuture.runAsync(() -> {
            try {
                var resp = Rotas.get("gerar-coddep/proximo",
                        Params.with("departamento", departamento));
                Platform.runLater(() -> {
                    if (resp == null || !resp.isSuccess()) {
                        logger.log(System.Logger.Level.WARNING,
                                "Falha ao gerar cod_dep: {0}",
                                resp != null ? resp.getFirstError() : "resposta nula");
                        return;
                    }
                    String cod = resp.getData("coddep", String.class);
                    if (cod != null && !cod.isBlank()) {
                        codDepField.setText(cod.trim());
                        if (modoEdicao) {
                            codDepPreviewTransferencia = cod.trim();
                        }
                    }
                });
            } catch (Exception e) {
                logger.log(System.Logger.Level.ERROR,
                        "❌ Erro ao gerar cod_dep: {0}", e.getMessage(), e);
            }
        });
    }

    // ===== MODOS =====

    public void setModoNovo() {
        this.modoEdicao = false;
        this.funcionario = null;
        this.imagemBytes = null;
        this.tipoImagem = null;
        this.codDepPreviewTransferencia = null;

        tituloLabel.setText("Novo Funcionário");
        limparFormulario();

        if (codDepField != null) {
            codDepField.setDisable(false);
        }
        if (btn_salvar != null) {
            btn_salvar.setText("Salvar Funcionário");
        }
        ocultarErro();

        // NÃO gera cod_dep automaticamente — a geração acontece:
        //   1) quando o usuário muda o departamento (listener)
        //   2) no btn_salvar, se ainda estiver vazio (fallback)
    }

    /**
     * GET: o list envia o codDep; o form entra em modo edição.
     */
    @GetMapping("editar/coddep")
    public ResponseData setModoEdicao(@RouteVar("coddep") String codDep) {
        if (codDep == null || codDep.isBlank()) {
            return ResponseData.error("codDep é obrigatório para edição");
        }
        // initialize() já rodou (show() é síncrono em FloatingWindow),
        // portanto os @FXML já estão injetados. Aplica direto.
        if (nomeField != null) {
            return aplicarEdicao(codDep);
        }
        // Fallback: FXML ainda não injetado — guarda para initialize() aplicar.
        codDepPendenteEdicao = codDep;
        return ResponseData.success();
    }

    private ResponseData aplicarEdicao(String codDep) {
        carregandoEdicao = true;
        try {
            var resp = Rotas.get("funcionarios/service/por/coddep",
                    Params.with("coddep", codDep));
            if (resp == null || !resp.isSuccess()) {
                return ResponseData.error("Funcionário não encontrado: " + codDep);
            }
            Funcionarios f = resp.getData("funcionario", Funcionarios.class);
            if (f == null) {
                return ResponseData.error("Funcionário não encontrado: " + codDep);
            }
            this.funcionario = f;
            this.modoEdicao = true;
            this.codDepPreviewTransferencia = null;
            preencherCampos();
            return ResponseData.success();
        } finally {
            carregandoEdicao = false;
        }
    }

    private void preencherCampos() {
        if (funcionario == null) return;

        carregandoEdicao = true;
        try {
            tituloLabel.setText("Editar Funcionário");
            codDepField.setText(funcionario.codDep());
            // readonly, não disabled — usuário vê o valor e o preview de transferência
            codDepField.setDisable(false);
            codDepField.setEditable(false);

            nomeField.setText(nvl(funcionario.nome()));
            funcaoField.setText(nvl(funcionario.funcao()));

            if (funcionario.departamento() != null
                    && !departamentoCombo.getItems().contains(funcionario.departamento())) {
                departamentoCombo.getItems().add(funcionario.departamento());
            }
            departamentoCombo.setValue(funcionario.departamento());

            localTrabalhoField.setText(nvl(funcionario.localTrabalho()));
            emailField.setText(nvl(funcionario.email()));
            telefoneField.setText(nvl(funcionario.telefone()));
            ativoCheckBox.setSelected(Boolean.TRUE.equals(funcionario.ativo()));

            if (funcionario.temImagemPerfil()) {
                this.imagemBytes = funcionario.imagemPerfil();
                this.tipoImagem = funcionario.tipoImagem();
                exibirPreviewImagem(imagemBytes);
                labelInfoImagem.setText(funcionario.getTamanhoImagemFormatado());
            } else {
                this.imagemBytes = null;
                this.tipoImagem = null;
                labelInfoImagem.setText("Nenhuma imagem selecionada");
                imagePreview.setImage(null);
                imagePreview.setVisible(false);
                imagePreview.setManaged(false);
            }

            if (btn_salvar != null) {
                btn_salvar.setText("Atualizar Funcionário");
            }
        } finally {
            carregandoEdicao = false;
        }
    }

    // ===== IMAGEM =====

    public void btn_selecionarImagem(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Imagens", "*.png", "*.jpg", "*.jpeg"));
        chooser.setTitle("Selecionar Foto de Perfil");

        File file = chooser.showOpenDialog(janela());
        if (file == null) return;

        try {
            if (file.length() > 2 * 1024 * 1024) {
                exibirErro("Imagem muito grande. Tamanho máximo: 2MB");
                return;
            }
            this.imagemBytes = lerBytes(file);
            this.tipoImagem = extrairExtensao(file.getName());
            exibirPreviewImagem(imagemBytes);
            labelInfoImagem.setText(file.getName() + " (" + formatarTamanho(file.length()) + ")");
            ocultarErro();
        } catch (IOException e) {
            logger.log(System.Logger.Level.ERROR, "Erro ao ler imagem", e);
            exibirErro("Erro ao carregar imagem: " + e.getMessage());
        }
    }

    public void btn_removerImagem(ActionEvent event) {
        this.imagemBytes = null;
        this.tipoImagem = null;
        imagePreview.setImage(null);
        imagePreview.setVisible(false);
        imagePreview.setManaged(false);
        labelInfoImagem.setText("Nenhuma imagem selecionada");
    }

    // ===== SALVAR / CANCELAR =====

    public void btn_salvar(ActionEvent event) {
        // Fallback: em modo novo, se ninguém mexeu no combo, gera agora.
        if (!modoEdicao && (codDepField.getText() == null || codDepField.getText().isBlank())) {
            String dep = departamentoCombo != null ? departamentoCombo.getValue() : null;
            if (dep != null && !dep.isBlank()) {
                try {
                    var resp = Rotas.get("gerar-coddep/proximo",
                            Params.with("departamento", dep));
                    if (resp != null && resp.isSuccess()) {
                        String cod = resp.getData("coddep", String.class);
                        if (cod != null && !cod.isBlank()) {
                            codDepField.setText(cod.trim());
                        }
                    }
                } catch (Exception e) {
                    logger.log(System.Logger.Level.ERROR,
                            "❌ Erro ao gerar cod_dep no salvar: {0}", e.getMessage(), e);
                }
            }
        }

        if (!validarCampos()) return;

        boolean edicao = modoEdicao;
        boolean transferencia = edicao
                && funcionario != null
                && departamentoCombo.getValue() != null
                && !departamentoCombo.getValue().equals(funcionario.departamento());

        if (transferencia) {
            salvarTransferencia();
        } else {
            salvarNormal(edicao);
        }
    }

    private void salvarNormal(boolean edicao) {
        Funcionarios func;
        try {
            func = construirFuncionario();
        } catch (IllegalArgumentException | IllegalStateException e) {
            exibirErro(e.getMessage());
            return;
        }

        String rota = edicao
                ? "funcionarios/service/atualizar"
                : "funcionarios/service/criar";

        btn_salvar.setDisable(true);
        btn_salvar.setText("Processando...");

        CompletableFuture.runAsync(() -> {
            try {
                var resp = Rotas.put(rota, Params.with("funcionario", func));
                Platform.runLater(() -> {
                    btn_salvar.setDisable(false);
                    btn_salvar.setText(edicao ? "Atualizar Funcionário" : "Salvar Funcionário");

                    if (resp == null || !resp.isSuccess()) {
                        exibirErro(resp != null
                                ? (resp.getFirstError() != null ? resp.getFirstError() : resp.getMessage())
                                : "Falha ao salvar.");
                        return;
                    }
                    if (imagemBytes != null) {
                        salvarImagemEmCadeia(func.codDep());
                    } else {
                        finalizarSalvamento();
                    }
                });
            } catch (Exception ex) {
                logger.log(System.Logger.Level.ERROR,
                        "❌ Erro ao salvar funcionário: {0}", ex.getMessage(), ex);
                Platform.runLater(() -> {
                    btn_salvar.setDisable(false);
                    btn_salvar.setText(edicao ? "Atualizar Funcionário" : "Salvar Funcionário");
                    exibirErro("Erro ao salvar: " + ex.getMessage());
                });
            }
        });
    }

    private void salvarTransferencia() {
        String codDepAtual = funcionario.codDep();
        String novoDepartamento = departamentoCombo.getValue();

        btn_salvar.setDisable(true);
        btn_salvar.setText("Transferindo...");

        CompletableFuture.runAsync(() -> {
            try {
                var resp = Rotas.put("funcionarios/service/transferir-departamento",
                        Params.with("coddep", codDepAtual)
                                .and("departamento", novoDepartamento)
                                .and("funcionarioid", codDepAtual)
                                .and("descricao", "{}"));
                Platform.runLater(() -> {
                    btn_salvar.setDisable(false);
                    btn_salvar.setText("Atualizar Funcionário");
                    if (resp == null || !resp.isSuccess()) {
                        exibirErro(resp != null
                                ? (resp.getFirstError() != null ? resp.getFirstError() : resp.getMessage())
                                : "Falha na transferência.");
                        return;
                    }
                    finalizarSalvamento();
                });
            } catch (Exception ex) {
                logger.log(System.Logger.Level.ERROR,
                        "❌ Erro na transferência: {0}", ex.getMessage(), ex);
                Platform.runLater(() -> {
                    btn_salvar.setDisable(false);
                    btn_salvar.setText("Atualizar Funcionário");
                    exibirErro("Erro na transferência: " + ex.getMessage());
                });
            }
        });
    }

    private void salvarImagemEmCadeia(String codDep) {
        CompletableFuture.runAsync(() -> {
            try {
                var rImg = Rotas.put("funcionarios/service/imagem/atualizar",
                        Params.with("coddep", codDep)
                                .and("imagem", imagemBytes)
                                .and("tipo", tipoImagem));
                Platform.runLater(() -> {
                    if (rImg == null || !rImg.isSuccess()) {
                        exibirErro("Funcionário salvo, mas falha ao gravar imagem: "
                                + (rImg != null ? rImg.getMessage() : "resposta nula"));
                        return;
                    }
                    finalizarSalvamento();
                });
            } catch (Exception ex) {
                logger.log(System.Logger.Level.ERROR,
                        "❌ Erro ao gravar imagem: {0}", ex.getMessage(), ex);
                Platform.runLater(() -> exibirErro("Erro ao gravar imagem: " + ex.getMessage()));
            }
        });
    }

    private void finalizarSalvamento() {
        Rotas.exec("funcionario-list/refresh");
        fecharJanela();
    }

    public void btn_cancelar(ActionEvent event) {
        fecharJanela();
    }

    // ===== MONTAGEM =====

    private Funcionarios construirFuncionario() {
        String codDep = codDepField.getText().trim();
        String nome = nomeField.getText().trim();
        String funcao = funcaoField.getText().trim();
        String departamento = departamentoCombo.getValue();
        String localTrabalho = strOrNull(localTrabalhoField);
        String email = strOrNull(emailField);
        String telefone = strOrNull(telefoneField);
        boolean ativo = ativoCheckBox.isSelected();

        if (!modoEdicao) {
            Funcionarios base = (imagemBytes != null)
                    ? Funcionarios.novoComImagem(codDep, nome, funcao, departamento,
                    localTrabalho, email, telefone, imagemBytes, tipoImagem)
                    : Funcionarios.novoCompleto(codDep, nome, funcao, departamento,
                    localTrabalho, email, telefone);
            return base.comAtivo(ativo);
        }

        if (funcionario == null || funcionario.id() == null) {
            throw new IllegalStateException("Modo edição sem funcionário carregado");
        }

        return new Funcionarios(
                funcionario.id(),
                funcionario.codDep(),
                funcionario.codDepAnterior(),
                nome,
                funcao,
                funcionario.departamento(),
                localTrabalho,
                email,
                telefone,
                imagemBytes,
                imagemBytes != null ? tipoImagem : null,
                imagemBytes != null ? imagemBytes.length : null,
                ativo,
                funcionario.createdAt(),
                LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
                funcionario.deviceId(),
                funcionario.isDeletado()
        );
    }

    // ===== VALIDAÇÃO =====

    private boolean validarCampos() {
        ocultarErro();

        if (isEmpty(codDepField)) {
            exibirErro("Aguarde a geração do código de departamento");
            codDepField.requestFocus();
            return false;
        }
        if (isEmpty(nomeField)) {
            exibirErro("Nome completo é obrigatório");
            nomeField.requestFocus();
            return false;
        }
        if (isEmpty(funcaoField)) {
            exibirErro("Função é obrigatória");
            funcaoField.requestFocus();
            return false;
        }
        if (departamentoCombo.getValue() == null) {
            exibirErro("Departamento é obrigatório");
            departamentoCombo.requestFocus();
            return false;
        }

        String email = nvl(emailField.getText()).trim();
        if (!email.isEmpty()
                && !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            exibirErro("Email inválido");
            emailField.requestFocus();
            return false;
        }
        return true;
    }

    // ===== AUXILIARES =====

    private byte[] lerBytes(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            return fis.readAllBytes();
        }
    }

    private String extrairExtensao(String nomeArquivo) {
        int dot = nomeArquivo.lastIndexOf('.');
        return dot > 0 ? nomeArquivo.substring(dot + 1).toLowerCase() : "png";
    }

    private void exibirPreviewImagem(byte[] bytes) {
        try {
            Image image = new Image(new ByteArrayInputStream(bytes));
            imagePreview.setImage(image);
            imagePreview.setVisible(true);
            imagePreview.setManaged(true);
        } catch (Exception e) {
            logger.log(System.Logger.Level.WARNING, "Erro ao exibir preview da imagem", e);
        }
    }

    private String formatarTamanho(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }

    private void limparFormulario() {
        codDepField.clear();
        nomeField.clear();
        funcaoField.clear();
        departamentoCombo.getSelectionModel().selectFirst();
        localTrabalhoField.clear();
        emailField.clear();
        telefoneField.clear();
        ativoCheckBox.setSelected(true);
        labelInfoImagem.setText("Nenhuma imagem selecionada");
        imagePreview.setImage(null);
        imagePreview.setVisible(false);
        imagePreview.setManaged(false);
    }

    private boolean isEmpty(TextField f) {
        return f.getText() == null || f.getText().trim().isEmpty();
    }

    private String strOrNull(TextField f) {
        String v = f.getText();
        return v != null && !v.trim().isEmpty() ? v.trim() : null;
    }

    private String nvl(String s) { return s != null ? s : ""; }

    private void exibirErro(String msg) {
        if (mensagemErroLabel == null) return;
        mensagemErroLabel.setText(msg);
        mensagemErroLabel.setVisible(true);
        mensagemErroLabel.setManaged(true);
    }

    private void ocultarErro() {
        if (mensagemErroLabel == null) return;
        mensagemErroLabel.setVisible(false);
        mensagemErroLabel.setManaged(false);
    }

    private Stage janela() {
        return (btn_cancelar != null && btn_cancelar.getScene() != null)
                ? (Stage) btn_cancelar.getScene().getWindow()
                : null;
    }

    private void fecharJanela() {
        Stage s = janela();
        if (s != null) s.close();
    }

    public void cleanup() {
        funcionario = null;
        imagemBytes = null;
        tipoImagem = null;
        codDepPendenteEdicao = null;
        codDepPreviewTransferencia = null;
    }
}