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
import com.ossobo.winterfx.view.floatingwindow.StageForFloatingWindow;
import com.ossobo.winterfx.view.floatingwindow.anotations.FloatingWindow;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

/**
 * FuncionarioDetailController v1.0
 *
 * Responsabilidade: exibir detalhes de um funcionário e ações rápidas
 *                   (editar, ativar/desativar, alterar foto, voltar).
 *
 * Espelha CatalogoProdutoDetailController v2.4:
 *   - @Controller(proxy = false) + @RequestMapping + @RegisterView.
 *   - Recebe o codDep do list via Rotas.get → @GetMapping("carregar/coddep").
 *   - Handlers nomeados pelo fx:id (binding WinterFX, sem onAction no FXML).
 *
 * Escopo v1.0:
 *   - btn_voltar       → fecha a janela atual.
 *   - btn_editar       → encaminha codDep ao form e fecha esta janela.
 *   - btn_desativar    → alterna status (ativar/desativar). Texto muda conforme
 *                        o estado atual do funcionário.
 *   - btn_alterarFoto  → mesmo fluxo do btn_editar (abre o form).
 *
 * Fora do escopo (stub visual):
 *   - Estatísticas (statEquipamentos / statTonerAtivo / statUltimaVerificacao):
 *     preenchidas com "—" até existirem rotas no backend.
 *   - Ações rápidas (btn_verEquipamentos / btn_transferirDepto / btn_historico):
 *     handlers vazios por enquanto.
 *
 * @since v1.0
 */
@Controller(proxy = false)
@RequestMapping("funcionario-detail")
@RegisterView(
        id = ViewConstant.Funcionario.DETAIL,
        fxml = "/META-INF/gestaoDepIt/fxmls/funcionario/FuncionarioDetail.fxml",
        primaryCss = "/META-INF/gestaoDepIt/css/funcionario/funcionario-detail.css"
)
public class FuncionarioDetailController implements Initializable, WinterFXController {

    private final DateTimeFormatter dateFormatter =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ===== ESTADO =====
    private Funcionarios funcionario;
    private String codDep;
    private volatile boolean fxmlCarregado = false;
    private final CompletableFuture<Void> fxmlCarregadoFuture = new CompletableFuture<>();

    // ===== FXML — HEADER =====
    @FXML private Button btn_voltar;
    @FXML private Button btn_editar;
    @FXML private Button btn_desativar;
    @FXML private Label  tituloLabel;

    // ===== FXML — FOTO =====
    @FXML private ImageView detailImagemPerfil;
    @FXML private Label     detailInfoImagem;
    @FXML private Button    btn_alterarFoto;

    // ===== FXML — DADOS PESSOAIS =====
    @FXML private Label detailCodDep;
    @FXML private Label detailNome;
    @FXML private Label detailFuncao;
    @FXML private Label detailDepartamento;
    @FXML private Label detailLocalTrabalho;
    @FXML private Label detailEmail;
    @FXML private Label detailTelefone;
    @FXML private Label detailStatus;

    // ===== FXML — SISTEMA =====
    @FXML private Label detailCriadoEm;
    @FXML private Label detailAtualizadoEm;

    // ===== FXML — ESTATÍSTICAS (stub v1.0) =====
    @FXML private Label statEquipamentos;
    @FXML private Label statTonerAtivo;
    @FXML private Label statUltimaVerificacao;

    // ===== FXML — AÇÕES RÁPIDAS (stub v1.0) =====
    @FXML private Button btn_verEquipamentos;
    @FXML private Button btn_transferirDepto;
    @FXML private Button btn_historico;

    // ===== FXML — MENSAGEM =====
    @FXML private Label mensagemStatusLabel;

    @FloatingWindow(
            viewId = ViewConstant.Funcionario.FORM,
            singleton = false
    )
    private StageForFloatingWindow formFuncionario;
    @FXML
    private void voltar(ActionEvent event) {}

    // ===== INITIALIZE =====

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        fxmlCarregado = true;
        fxmlCarregadoFuture.complete(null);
    }

    // ===== ROTA DE ENTRADA (chamada pelo list) =====

    @GetMapping("carregar/coddep")
    public ResponseData carregar(@RouteVar("coddep") String codDep) {
        this.codDep = codDep;
        fxmlCarregadoFuture.thenRun(() ->
                Platform.runLater(() -> carregarFuncionario(codDep)));
        return ResponseData.success();
    }

    private void carregarFuncionario(String codDep) {
        if (codDep == null || codDep.isBlank()) return;
        mostrarCarregamento(true);

        CompletableFuture
                .supplyAsync(() -> Rotas.get("funcionarios/service/por/coddep",
                        Params.with("coddep", codDep)))
                .whenComplete((resp, erro) -> Platform.runLater(() -> {
                    mostrarCarregamento(false);
                    if (erro != null) { mostrarErro("Erro ao carregar", erro.getMessage()); return; }
                    if (resp == null || !resp.isSuccess()) {
                        mostrarErro("Erro", resp != null ? resp.getFirstError() : "resposta nula");
                        return;
                    }
                    Funcionarios f = resp.getData("funcionario", Funcionarios.class);
                    if (f == null) {
                        mostrarErro("Não encontrado", "Funcionário: " + codDep);
                        return;
                    }
                    this.funcionario = f;
                    preencherDados(f);
                    carregarImagem(f);
                    atualizarInterface();
                }));
    }

    private void preencherDados(Funcionarios f) {
        tituloLabel.setText("Detalhes do Funcionário");

        detailCodDep.setText(nullToDash(f.codDep()));
        detailNome.setText(nullToDash(f.nome()));
        detailFuncao.setText(nullToDash(f.funcao()));
        detailDepartamento.setText(nullToDash(f.departamento()));
        detailLocalTrabalho.setText(nullToDash(f.localTrabalho()));
        detailEmail.setText(nullToDash(f.email()));
        detailTelefone.setText(nullToDash(f.telefone()));

        boolean ativo = Boolean.TRUE.equals(f.ativo());
        detailStatus.setText(ativo ? "Ativo" : "Inativo");
        detailStatus.getStyleClass().setAll(ativo ? "status-ativo" : "status-inativo");

        detailCriadoEm.setText(f.createdAt() != null ? f.createdAt().format(dateFormatter) : "-");
        detailAtualizadoEm.setText(f.updatedAt() != null ? f.updatedAt().format(dateFormatter) : "-");

        // Estatísticas — stub v1.0 (rotas ainda não existem).
        if (statEquipamentos != null) statEquipamentos.setText("Equipamentos: —");
        if (statTonerAtivo != null) statTonerAtivo.setText("Toner Ativo: —");
        if (statUltimaVerificacao != null) statUltimaVerificacao.setText("Última Verificação: —");
    }

    private void carregarImagem(Funcionarios f) {
        if (detailImagemPerfil == null) return;

        if (!f.temImagemPerfil()) {
            detailImagemPerfil.setImage(null);
            if (detailInfoImagem != null) detailInfoImagem.setText("Sem imagem");
            return;
        }

        CompletableFuture
                .supplyAsync(() -> Rotas.get("funcionarios/service/imagem/por/coddep",
                        Params.with("coddep", f.codDep())))
                .whenComplete((resp, erro) -> Platform.runLater(() -> {
                    if (erro != null || resp == null || !resp.isSuccess()) {
                        if (detailInfoImagem != null) detailInfoImagem.setText("Imagem indisponível");
                        return;
                    }
                    byte[] bytes = resp.getData("imagem", byte[].class);
                    if (bytes == null || bytes.length == 0) {
                        detailImagemPerfil.setImage(null);
                        if (detailInfoImagem != null) detailInfoImagem.setText("Sem imagem");
                        return;
                    }
                    Image img = new Image(new ByteArrayInputStream(bytes),
                            150, 150, true, true);
                    detailImagemPerfil.setImage(img);
                    if (detailInfoImagem != null) {
                        detailInfoImagem.setText(f.getTamanhoImagemFormatado());
                    }
                }));
    }

    private void atualizarInterface() {
        if (funcionario == null) return;
        boolean ativo = Boolean.TRUE.equals(funcionario.ativo());
        btn_desativar.setText(ativo ? "Desativar" : "Ativar");
        btn_desativar.getStyleClass().setAll(ativo ? "btn-danger" : "btn-success");
    }

    // ===== HANDLERS (nome = fx:id) =====

    public void btn_voltar(ActionEvent event) {
        fecharJanela();
    }

    public void btn_editar(ActionEvent event) {
        if (funcionario == null) return;
        formFuncionario.show();
        Rotas.get("funcionario-form/editar/coddep",
                Params.with("coddep", funcionario.codDep()));

    }

    public void btn_alterarFoto(ActionEvent event) {
        // Mesmo fluxo do btn_editar: abre o form para o usuário trocar a foto.
        btn_editar(event);
    }

    public void btn_desativar(ActionEvent event) {
        if (funcionario == null) return;
        boolean ativoAtual = Boolean.TRUE.equals(funcionario.ativo());
        executarToggleStatus(ativoAtual);
    }

    public void btn_verEquipamentos(ActionEvent event) {
        // Stub v1.0 — aguardando rota no backend.
    }

    public void btn_transferirDepto(ActionEvent event) {
        // Stub v1.0 — aguardando rota no backend.
    }

    public void btn_historico(ActionEvent event) {
        // Stub v1.0 — aguardando rota no backend.
    }

    // ===== AÇÕES =====

    private void executarToggleStatus(boolean ativoAtual) {
        mostrarCarregamento(true);
        String rota = ativoAtual
                ? "funcionarios/service/desativar"
                : "funcionarios/service/ativar";
        String cod = funcionario.codDep();

        CompletableFuture
                .supplyAsync(() -> Rotas.put(rota, Params.with("coddep", cod)))
                .whenComplete((resp, erro) -> Platform.runLater(() -> {
                    mostrarCarregamento(false);
                    if (erro != null) { mostrarErro("Erro", erro.getMessage()); return; }
                    if (resp == null || !resp.isSuccess()) {
                        mostrarErro("Erro", resp != null ? resp.getFirstError() : "resposta nula");
                        return;
                    }
                    // Recarrega do banco para refletir o estado atual.
                    carregarFuncionario(cod);
                }));
    }

    // ===== UI =====

    private void mostrarCarregamento(boolean carregando) {
        if (!fxmlCarregado) return;
        if (btn_voltar != null) btn_voltar.setDisable(carregando);
        if (btn_editar != null) btn_editar.setDisable(carregando);
        if (btn_desativar != null) btn_desativar.setDisable(carregando);
        if (btn_alterarFoto != null) btn_alterarFoto.setDisable(carregando);
    }

    private void mostrarErro(String titulo, String mensagem) {
        if (mensagemStatusLabel != null) {
            mensagemStatusLabel.setText(titulo + ": " + (mensagem != null ? mensagem : ""));
            mensagemStatusLabel.setVisible(true);
            mensagemStatusLabel.setManaged(true);
        }
    }

    private void fecharJanela() {
        if (btn_voltar != null && btn_voltar.getScene() != null) {
            ((Stage) btn_voltar.getScene().getWindow()).close();
        }
    }

    private String nullToDash(String s) { return s != null && !s.isBlank() ? s : "—"; }

    public void cleanup() {
        funcionario = null;
        codDep = null;
    }
}