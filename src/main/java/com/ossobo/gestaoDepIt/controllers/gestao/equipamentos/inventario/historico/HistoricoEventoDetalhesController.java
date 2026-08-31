package com.ossobo.gestaoDepIt.controllers.gestao.equipamentos.inventario.historico;

import com.ossobo.gestaoDepIt.db.models.HistoricoEventos;
import com.ossobo.winterfx.anotations.Controller;
import com.ossobo.winterfx.anotations.PostConstruct;
import com.ossobo.winterfx.anotations.PutMapping;
import com.ossobo.winterfx.anotations.RequestMapping;
import com.ossobo.winterfx.anotations.RouteVar;
import com.ossobo.winterfx.router.Rotas;
import com.ossobo.winterfx.router.model.Params;
import com.ossobo.winterfx.router.model.ResponseData;
import com.ossobo.winterfx.view.anotations.RegisterView;
import com.ossobo.winterfx.view.controller.WinterFXController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;

/**
 * Controlador de detalhes de um evento do histórico (janela flutuante).
 * v2.2 - Política 97/2/1: ZERO service injetado — comunicação exclusiva via Rotas.
 *
 * Responsabilidades:
 * - Exibir detalhes, comparação e exportação de UM evento
 * - Entrada de dados por rota (set-id) e fonte por rota (por/id) —
 *   padrão @FloatingWindow: NUNCA injetar controller de janela flutuante
 *
 * Mudanças v2.0 → v2.2:
 * - @Inject HistoricoEventosService REMOVIDO (Política 97/2/1)
 * - Fonte dos dados: Rotas.get("historico-eventos/service/por/id") —
 *   contrato da HistoricoEventosRoutes (chave de retorno: "evento")
 * - Import Params confirmado contra exemplo canônico: router.model.Params
 *
 * Mudanças v1.1 → v2.0 (histórico):
 * - @Controller(proxy=false) + WinterFXController + @RegisterView
 * - Entrada por rota @PutMapping("set-id") substitui setEventoId(Long)
 * - Initializable → @PostConstruct · Getters JavaBean → accessors de record
 * - eventoId: Long → String (UUID) · "{}" tratado como "sem dados anteriores"
 */
@Controller(proxy = false)
@RegisterView(
        id = "detalhesHistoricoevento",                                 // referenciado pelo @FloatingWindow do ListController
        fxml = "/META-INF/gestaoDepIt/fxmls/historico/detalhes.fxml",   // ⚠️ CONFIRMAR caminho real do FXML
        title = "Detalhes do Evento",
        width = 780,
        height = 640,
        centered = true
)
@RequestMapping("historico-evento-detalhes")
public class HistoricoEventoDetalhesController implements WinterFXController {

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoricoEventoDetalhesController.class);
    private static final DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    /** Contrato: HistoricoEventosRoutes @GetMapping("por/id") — chave de retorno "evento". */
    private static final String ROTA_POR_ID = "historico-eventos/service/por/id";

    // Header
    @FXML private Button voltarButton, imprimirButton, exportarButton;
    @FXML private Label tituloLabel, subtituloLabel, mensagemStatusLabel;

    // Info Geral
    @FXML private Label detailId, detailDataHora, detailTipo, detailEntidade, detailEntidadeId;
    @FXML private Label detailIpOrigem, detailSkuProduto, detailFuncionario;
    @FXML private TextArea detailDescricao;

    // Usuário
    @FXML private Label detailUsuarioId, detailUsuarioNome, detailUsuarioEmail;
    @FXML private Button btnVerEntidade, btnAuditarUsuario;

    // Abas - Status
    @FXML private Label lblStatusAnteriores, lblStatusNovos;

    // Abas - Dados
    @FXML private TextArea detailDadosAnteriores, detailDadosNovos, txtComparacao, txtDetalhesFuncionario;

    // Info Adicional
    @FXML private Label lblInformacoesAdicionais;

    private HistoricoEventos evento;
    private String eventoId;

    @PostConstruct
    public void init() {
        LOGGER.info("✅ HistoricoEventoDetalhesController pronto (aguardando rota set-id)");
    }

    // ============================================================
    // PORTA DE ENTRADA (ROTA)
    // ============================================================

    /**
     * Recebe o ID do evento do ListController após a janela abrir:
     * janelaDetalhes.show() → Rotas.put("historico-evento-detalhes/set-id", Params.with("id", ev.id()));
     * Dispara carga assíncrona e retorna imediatamente.
     */
    @PutMapping("set-id")
    public ResponseData receberEventoId(@RouteVar("id") String id) {
        carregarEvento(id);
        return ResponseData.success().withData("agendado", true);
    }

    /**
     * Fonte via rota (Política 97/2/1): GET historico-eventos/service/por/id.
     * Envelope de sucesso: {"evento": HistoricoEventos} ·
     * inexistente/erro: success=false com mensagem pronta da fronteira.
     */
    private void carregarEvento(String id) {
        this.eventoId = id;
        new Thread(() -> {
            try {
                Object resposta = Rotas.get(ROTA_POR_ID, Params.with("id", id));
                if (!(resposta instanceof ResponseData r)) {
                    throw new IllegalStateException("Resposta inesperada da fronteira de histórico");
                }
                if (r.isSuccess() && r.getData().get("evento") instanceof HistoricoEventos ev) {
                    evento = ev;
                    Platform.runLater(this::preencherInterface);
                } else {
                    LOGGER.warn("Evento não carregado ({}): {}", abreviarId(id), r.getMessage());
                    Platform.runLater(() ->
                            mensagemStatusLabel.setText("❌ " + r.getMessage()));
                }
            } catch (Exception e) {
                LOGGER.error("❌ Erro ao carregar evento {}", abreviarId(id), e);
                Platform.runLater(() ->
                        mensagemStatusLabel.setText("❌ Erro ao carregar: " + e.getMessage()));
            }
        }, "Historico-Detalhes-Load").start();
    }

    // ============================================================
    // PREENCHIMENTO DA INTERFACE
    // ============================================================

    private void preencherInterface() {
        if (evento == null) return;

        // Header
        tituloLabel.setText("Detalhes do Evento " + abreviarId(evento.id()));
        subtituloLabel.setText("Tipo: " + nvl(evento.tipoEvento())
                + " | SKU: " + nvl(evento.skuProduto())
                + " | Func: " + nvl(evento.funcionarioId()));

        // Info Geral
        detailId.setText(evento.id());
        detailDataHora.setText(evento.createdAt() != null
                ? evento.createdAt().format(DATE_TIME_FMT) : "-");
        detailTipo.setText(nvl(evento.tipoEvento()));

        // Entidade: inferida do tipo de evento (schema não tem campo "entidade")
        detailEntidade.setText(inferirEntidade(evento.tipoEvento()));
        detailEntidadeId.setText("-");

        // IP Origem: schema não tem esse campo
        detailIpOrigem.setText("Não disponível");

        detailSkuProduto.setText(nvl(evento.skuProduto()));
        detailFuncionario.setText(nvl(evento.funcionarioId()));

        // Descrição: método de negócio do record (nome preservado)
        detailDescricao.setText(nvl(evento.getDescricaoEvento()));

        // Usuário/Funcionário — funcionario_id + descricao_funcionario (JSON)
        detailUsuarioId.setText(nvl(evento.funcionarioId()));
        detailUsuarioNome.setText(nvl(extrairNomeFuncionario(evento.descricaoFuncionario())));
        detailUsuarioEmail.setText(nvl(extrairEmailFuncionario(evento.descricaoFuncionario())));

        // Dados Anteriores ("{}" = default do record = sem dados)
        if (temConteudo(evento.dadosAnteriores())) {
            detailDadosAnteriores.setText(formatarJson(evento.dadosAnteriores()));
            lblStatusAnteriores.setText("✅ Dados disponíveis");
            lblStatusAnteriores.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
        } else {
            detailDadosAnteriores.setText("Nenhum dado anterior registrado (criação ou primeira ação).");
            lblStatusAnteriores.setText("⚠️ Dados não disponíveis");
            lblStatusAnteriores.setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
        }

        // Dados Novos
        if (evento.dadosNovos() != null && !evento.dadosNovos().isEmpty()) {
            detailDadosNovos.setText(formatarJson(evento.dadosNovos()));
            lblStatusNovos.setText("✅ Dados disponíveis");
            lblStatusNovos.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
        } else {
            detailDadosNovos.setText("Nenhum dado novo registrado.");
            lblStatusNovos.setText("⚠️ Dados não disponíveis");
            lblStatusNovos.setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
        }

        gerarComparacao();

        txtDetalhesFuncionario.setText(formatarJson(
                evento.descricaoFuncionario() != null ? evento.descricaoFuncionario() : "{}"));

        lblInformacoesAdicionais.setText(
                "Evento registrado em " + (evento.createdAt() != null
                        ? evento.createdAt().format(DATE_TIME_FMT) : "N/A")
                        + " | SKU: " + nvl(evento.skuProduto())
                        + " | Funcionário: " + nvl(evento.funcionarioId()));

        mensagemStatusLabel.setText("✅ Dados carregados com sucesso.");
    }

    private void gerarComparacao() {
        String anterior = evento.dadosAnteriores();
        String novo = evento.dadosNovos();

        if (!temConteudo(anterior) && !temConteudo(novo)) {
            txtComparacao.setText("Sem dados para comparar.");
            return;
        }
        if (!temConteudo(anterior)) {
            txtComparacao.setText("🆕 EVENTO DE CRIAÇÃO — Não há dados anteriores.\n\n"
                    + "Dados novos registrados:\n" + formatarJson(novo));
            return;
        }
        if (!temConteudo(novo)) {
            txtComparacao.setText("🗑️ EVENTO DE EXCLUSÃO/BAIXA — Não há dados novos.\n\n"
                    + "Dados anteriores:\n" + formatarJson(anterior));
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("══════════════ COMPARAÇÃO ══════════════\n\n");
        String[] linhasA = anterior.split("\n");
        String[] linhasN = novo.split("\n");

        int max = Math.max(linhasA.length, linhasN.length);
        for (int i = 0; i < max; i++) {
            String la = i < linhasA.length ? linhasA[i].trim() : "";
            String ln = i < linhasN.length ? linhasN[i].trim() : "";
            if (la.equals(ln)) {
                sb.append("  = ").append(la).append("\n");
            } else {
                if (!la.isEmpty()) sb.append("❌ ANT: ").append(la).append("\n");
                if (!ln.isEmpty()) sb.append("✅ NOV: ").append(ln).append("\n");
            }
        }
        txtComparacao.setText(sb.toString());
    }

    // ===== HANDLERS =====

    @FXML private void handleVoltar() {
        ((Stage) voltarButton.getScene().getWindow()).close();
    }

    @FXML private void handleFechar() {
        ((Stage) voltarButton.getScene().getWindow()).close();
    }

    @FXML private void handleImprimir() {
        mensagemStatusLabel.setText("ℹ️ Impressão em desenvolvimento.");
    }

    @FXML private void handleExportarJson() {
        if (evento == null) return;
        String json = String.format(
                "{\"id\":\"%s\",\"tipo_evento\":\"%s\",\"sku_produto\":\"%s\",\"funcionario_id\":\"%s\","
                        + "\"created_at\":\"%s\",\"descricao_funcionario\":%s,"
                        + "\"dados_anteriores\":%s,\"dados_novos\":%s}",
                nvl(evento.id()),
                nvl(evento.tipoEvento()),
                nvl(evento.skuProduto()),
                nvl(evento.funcionarioId()),
                evento.createdAt() != null ? evento.createdAt().format(DATE_TIME_FMT) : "",
                evento.descricaoFuncionario() != null ? evento.descricaoFuncionario() : "{}",
                evento.dadosAnteriores() != null ? evento.dadosAnteriores() : "{}",
                evento.dadosNovos() != null ? evento.dadosNovos() : "{}");
        copiarParaClipboard(json);
        mensagemStatusLabel.setText("✅ JSON copiado para a área de transferência.");
    }

    @FXML private void handleExportarPDF() {
        mensagemStatusLabel.setText("ℹ️ Exportação PDF em desenvolvimento.");
    }

    @FXML private void handleCopiarDadosAnteriores() {
        copiarParaClipboard(detailDadosAnteriores.getText());
        mensagemStatusLabel.setText("✅ Dados anteriores copiados.");
    }

    @FXML private void handleCopiarDadosNovos() {
        copiarParaClipboard(detailDadosNovos.getText());
        mensagemStatusLabel.setText("✅ Dados novos copiados.");
    }

    @FXML private void handleVisualizarFormatado() {
        detailDadosNovos.setText(formatarJson(detailDadosNovos.getText()));
        mensagemStatusLabel.setText("✅ Dados formatados.");
    }

    @FXML private void handleGerarRelatorioDiferencas() {
        mensagemStatusLabel.setText("ℹ️ Relatório de diferenças em desenvolvimento.");
    }

    @FXML private void handleVerEntidade() {
        mensagemStatusLabel.setText("📦 SKU: " + nvl(evento.skuProduto())
                + " | Funcionário: " + nvl(evento.funcionarioId()));
    }

    @FXML private void handleAuditarUsuario() {
        mensagemStatusLabel.setText("👤 Funcionário: " + nvl(evento.funcionarioId())
                + " | Nome: " + nvl(extrairNomeFuncionario(evento.descricaoFuncionario())));
    }

    @FXML private void handleAuditoriaRelacionada() {
        mensagemStatusLabel.setText("ℹ️ Eventos relacionados ao SKU "
                + nvl(evento.skuProduto()) + " — em desenvolvimento.");
    }

    // ===== UTILITÁRIOS =====

    private void copiarParaClipboard(String texto) {
        ClipboardContent content = new ClipboardContent();
        content.putString(texto);
        Clipboard.getSystemClipboard().setContent(content);
    }

    private String formatarJson(String json) {
        if (json == null || json.isEmpty()) return "";
        try {
            return json.replace(",", ",\n  ")
                    .replace("{", "{\n  ")
                    .replace("}", "\n}")
                    .replace("\"[", "[\n    ")
                    .replace("]\"", "\n  ]");
        } catch (Exception e) {
            return json;
        }
    }

    /** UUID é longo — exibe os 8 primeiros caracteres na UI. */
    private String abreviarId(String id) {
        return (id != null && id.length() > 8) ? id.substring(0, 8) + "…" : nvl(id);
    }

    /**
     * "{}" (default do record v3.0) e "null" textual = sem conteúdo real.
     * Correção de migração: o legado só checava null/vazio/"null".
     */
    private boolean temConteudo(String json) {
        return json != null && !json.isEmpty()
                && !"null".equals(json) && !"{}".equals(json);
    }

    private String extrairNomeFuncionario(String json) {
        if (json == null || json.isEmpty()) return null;
        try {
            if (json.contains("\"nome\":")) {
                String[] partes = json.split("\"nome\":\"");
                if (partes.length > 1) {
                    return partes[1].split("\"")[0];
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    private String extrairEmailFuncionario(String json) {
        if (json == null || json.isEmpty()) return null;
        try {
            if (json.contains("\"email\":")) {
                String[] partes = json.split("\"email\":\"");
                if (partes.length > 1) {
                    return partes[1].split("\"")[0];
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    private String inferirEntidade(String tipoEvento) {
        if (tipoEvento == null) return "DESCONHECIDO";
        return switch (tipoEvento.toUpperCase()) {
            case "CRIACAO", "ATUALIZACAO", "BAIXA" -> "INVENTARIO";
            case "MANUTENCAO" -> "MANUTENCAO";
            case "MOVIMENTACAO" -> "MOVIMENTACAO";
            case "INSTALACAO" -> "INSTALACAO";
            case "LOGIN", "LOGOUT" -> "SISTEMA";
            default -> tipoEvento;
        };
    }

    private String nvl(String s) { return (s != null && !s.isEmpty()) ? s : "-"; }
}