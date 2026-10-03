package com.ossobo.gestaoDepIt.controlls.historico;

import com.ossobo.gestaoDepIt.config.ViewConstant;
import com.ossobo.gestaoDepIt.controlls.historico.historicoDetail.HistoricoDiff;
import com.ossobo.gestaoDepIt.controlls.historico.historicoDetail.HistoricoFormatter;
import com.ossobo.gestaoDepIt.db.models.HistoricoEventos;
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
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.Stage;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

/**
 * HistoricoEventoDetailController v3.1
 *
 * Detalhes de UM evento do histórico. Janela flutuante filha.
 *
 * v3.1 — Correção de canal:
 *        - carregar/id passa a @GetMapping — receber id para popular é leitura.
 *        - btn_verOutrosSku e btn_verOutrosFuncionario usam Rotas.get para
 *          enviar contexto à listagem; a listagem decide o que fazer com ele.
 *
 * @since v3.0
 */
@Controller(proxy = false)
@RegisterView(
        id = ViewConstant.Inventario.HISTORICO_DETAIL,
        fxml = "/META-INF/gestaoDepIt/fxmls/historico/HistoricoEventoDetail.fxml",
        title = "Detalhes do Evento",
        primaryCss = "/META-INF/gestaoDepIt/css/historico/HistoricoEventoDetail.css"
)
@RequestMapping("historico-detail")
public class HistoricoEventoDetailController implements Initializable, WinterFXController {

    private static final DateTimeFormatter DATE_TIME_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private static final String ROTAS = "historico-eventos/service";

    // ===== FXML — HEADER =====
    @FXML private Button btn_voltar;
    @FXML private Label  tituloLabel;
    @FXML private Label  subtituloLabel;
    @FXML private Label  subtituloDeviceLabel;
    @FXML private Button btn_imprimir;
    @FXML private Button btn_exportar;
    @FXML private Label  mensagemStatusLabel;

    // ===== FXML — CARDS CONTEXTO =====
    @FXML private Label cardSku;
    @FXML private Label cardFuncionarioId;
    @FXML private Label cardFuncionarioNome;
    @FXML private Label cardFuncionarioEmail;
    @FXML private Label cardResumo;

    // ===== FXML — DESCRIÇÃO =====
    @FXML private Label descricaoEvento;

    // ===== FXML — SNAPSHOTS =====
    @FXML private TextArea areaAnteriores;
    @FXML private TextArea areaNovos;

    // ===== FXML — DIFERENÇAS =====
    @FXML private Label             lblDiferencasTitulo;
    @FXML private ListView<String>  listaDiferencas;

    // ===== FXML — BOTTOM =====
    @FXML private Button btn_verOutrosSku;
    @FXML private Button btn_verOutrosFuncionario;
    @FXML private Button btn_copiarJson;
    @FXML private Button btn_fechar;

    // ===== ESTADO =====
    private HistoricoEventos evento;

    public HistoricoEventoDetailController() { }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Nada — os dados chegam via rota após show().
    }

    // ===== ROTA DE ENTRADA (GET: enviar id para popular a tela) =====

    @GetMapping("carregar/id")
    public ResponseData carregarPorId(@RouteVar("id") String id) {
        CompletableFuture.runAsync(() -> {
            var resp = Rotas.get(ROTAS + "/por/id", Params.with("id", id));
            if (!resp.isSuccess()) {
                Platform.runLater(() ->
                        mensagemStatusLabel.setText("❌ " + resp.getMessage()));
                return;
            }
            HistoricoEventos ev = resp.getData("evento", HistoricoEventos.class);
            if (ev == null) {
                Platform.runLater(() ->
                        mensagemStatusLabel.setText("❌ Evento não encontrado."));
                return;
            }
            this.evento = ev;
            Platform.runLater(this::preencherInterface);
        });
        return ResponseData.success();
    }

    // ===== PREENCHIMENTO =====

    private void preencherInterface() {
        if (evento == null) return;

        tituloLabel.setText("Detalhes do Evento");
        subtituloLabel.setText(String.format("Tipo: %s  |  %s",
                nvl(evento.tipoEvento()),
                evento.createdAt() != null ? evento.createdAt().format(DATE_TIME_FMT) : "-"));
        subtituloDeviceLabel.setText("Dispositivo: " + nvl(evento.deviceId()));

        cardSku.setText(nvl(evento.skuProduto()));
        cardFuncionarioId.setText(nvl(evento.funcionarioId()));

        String nomeFunc  = HistoricoFormatter.extrairValor(evento.descricaoFuncionario(), "nome");
        String emailFunc = HistoricoFormatter.extrairValor(evento.descricaoFuncionario(), "email");
        cardFuncionarioNome.setText(nomeFunc != null ? nomeFunc : "—");
        cardFuncionarioEmail.setText(emailFunc != null ? emailFunc : "—");

        descricaoEvento.setText(nvl(evento.getDescricaoEvento()));

        areaAnteriores.setText(HistoricoFormatter.formatar(evento.dadosAnteriores()));
        areaNovos.setText(HistoricoFormatter.formatar(evento.dadosNovos()));

        List<HistoricoDiff.Diferenca> diferencas =
                HistoricoDiff.comparar(evento.dadosAnteriores(), evento.dadosNovos());
        int iguais = HistoricoDiff.contarIguais(evento.dadosAnteriores(), evento.dadosNovos());

        lblDiferencasTitulo.setText(String.format("Diferenças (%d) · %d campo(s) idêntico(s)",
                diferencas.size(), iguais));
        cardResumo.setText(String.format("%d alterado(s) · %d idêntico(s)",
                diferencas.size(), iguais));

        listaDiferencas.getItems().clear();
        if (diferencas.isEmpty()) {
            listaDiferencas.getItems().add("(nenhuma diferença — snapshot único ou igual)");
        } else {
            for (var d : diferencas) listaDiferencas.getItems().add(d.formatado());
        }

        mensagemStatusLabel.setText("✅ Evento carregado.");
    }

    // ===== MÉTODOS DE AÇÃO (nome = fx:id) =====

    public void btn_voltar(ActionEvent event) { fecharJanela(); }

    public void btn_imprimir(ActionEvent event) {
        mensagemStatusLabel.setText("ℹ️ Impressão em desenvolvimento.");
    }

    public void btn_exportar(ActionEvent event) {
        if (evento == null) return;
        copiarParaClipboard(jsonEvento());
        mensagemStatusLabel.setText("✅ JSON copiado para a área de transferência.");
    }

    public void btn_verOutrosSku(ActionEvent event) {
        if (evento == null) return;
        // GET: enviar contexto à listagem de histórico. NÃO muta nada.
        Rotas.get("historico-list/aplicar-prefiltro",
                Params.with("sku", evento.skuProduto())
                        .and("funcionarioid", ""));
        mensagemStatusLabel.setText("🔗 Filtrando histórico pelo SKU " + nvl(evento.skuProduto()));
    }

    public void btn_verOutrosFuncionario(ActionEvent event) {
        if (evento == null) return;
        Rotas.get("historico-list/aplicar-prefiltro",
                Params.with("sku", "")
                        .and("funcionarioid", evento.funcionarioId()));
        mensagemStatusLabel.setText("👤 Filtrando histórico pelo funcionário " + nvl(evento.funcionarioId()));
    }

    public void btn_copiarJson(ActionEvent event) {
        if (evento == null) return;
        copiarParaClipboard(jsonEvento());
        mensagemStatusLabel.setText("✅ JSON copiado.");
    }

    public void btn_fechar(ActionEvent event) { fecharJanela(); }

    // ===== HELPERS =====

    private void fecharJanela() {
        if (btn_voltar != null && btn_voltar.getScene() != null) {
            ((Stage) btn_voltar.getScene().getWindow()).close();
        }
    }

    private void copiarParaClipboard(String texto) {
        ClipboardContent c = new ClipboardContent();
        c.putString(texto);
        Clipboard.getSystemClipboard().setContent(c);
    }

    private String jsonEvento() {
        return String.format(
                "{\"id\":\"%s\",\"tipoEvento\":\"%s\",\"skuProduto\":\"%s\","
                        + "\"funcionarioId\":\"%s\",\"createdAt\":\"%s\","
                        + "\"deviceId\":\"%s\","
                        + "\"descricaoFuncionario\":%s,"
                        + "\"dadosAnteriores\":%s,\"dadosNovos\":%s}",
                nvl(evento.id()), nvl(evento.tipoEvento()), nvl(evento.skuProduto()),
                nvl(evento.funcionarioId()),
                evento.createdAt() != null ? evento.createdAt().toString() : "",
                nvl(evento.deviceId()),
                evento.descricaoFuncionario() != null ? evento.descricaoFuncionario() : "{}",
                evento.dadosAnteriores() != null ? evento.dadosAnteriores() : "{}",
                evento.dadosNovos() != null ? evento.dadosNovos() : "{}");
    }

    private String nvl(String s) { return s != null && !s.isEmpty() ? s : "-"; }

    public void cleanup() { evento = null; }
}