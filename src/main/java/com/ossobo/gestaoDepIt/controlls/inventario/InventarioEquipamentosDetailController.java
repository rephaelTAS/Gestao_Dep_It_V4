package com.ossobo.gestaoDepIt.controlls.inventario;

import com.ossobo.gestaoDepIt.config.ViewConstant;
import com.ossobo.gestaoDepIt.db.models.CatalogoProdutos;
import com.ossobo.gestaoDepIt.db.models.InventarioEquipamentos;
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
import javafx.scene.image.ImageView;
import javafx.scene.shape.Arc;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

/**
 * InventarioEquipamentosDetailController v2.0
 *
 * Detalhes de UM equipamento do inventário. Janela flutuante filha.
 *
 *  - Zero service injetado — fonte via Rotas.
 *  - FXML é fonte da nomenclatura: campo @FXML = fx:id; método = fx:id.
 *  - Recebe id via rota GET "inventario-detail/carregar/id", após show().
 *  - Ações de mutação delegam para inventario-crud/service/* (transacional).
 *  - Histórico abre a janela própria (ViewConstant.Inventario.HISTORICO_LIST)
 *    com pré-filtro via GET historico-list/aplicar-prefiltro.
 *
 * @since v2.0
 */
@Controller(proxy = false)
@RequestMapping("inventario-detail")
@RegisterView(
        id = ViewConstant.Inventario.DETAIL,
        fxml = "/META-INF/gestaoDepIt/fxmls/inventario/InventarioEquipamentosDetails.fxml",
        title = "Detalhes do Equipamento",
        primaryCss = "/META-INF/gestaoDepIt/css/inventario/InventarioEquipamentosDetails.css"
)
public class InventarioEquipamentosDetailController implements Initializable, WinterFXController {

    private static final DateTimeFormatter DATE_FMT      = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ===== FXML — HEADER =====
    @FXML private ImageView imgEquipamentoIcon;
    @FXML private Label     lblTituloEquipamento;
    @FXML private Label     lblSubtitulo;
    @FXML private Label     lblStatusBadge;
    @FXML private Label     lblCondicaoBadge;
    @FXML private Button    btn_editar;
    @FXML private Button    btn_historico;
    @FXML private Button    btn_relatorio;
    @FXML private Button    btn_excluir;
    @FXML private Button    btn_fechar;
    @FXML private Label     lblTempoUso;
    @FXML private Label     lblProximaVerificacao;
    @FXML private ProgressBar pbPerformance;
    @FXML private Label     lblPerformancePercent;

    // ===== FXML — CARDS =====
    @FXML private Label lblNumSerie, lblEnderecoMac, lblSku, lblTipoEquipamento;
    @FXML private Label lblDepartamento, lblLocalizacao, lblFuncionarioId;
    @FXML private Label lblFuncionarioNome, lblFuncao;
    @FXML private Arc   arcStatusProgress;
    @FXML private Label lblStatusScore, lblStatus, lblCondicao, lblUltimaVerificacao, lblDevolucao;

    // ===== FXML — AÇÕES RÁPIDAS =====
    @FXML private Button btn_verificacao, btn_manutencao, btn_qrCode, btn_anexar, btn_baixar;

    // ===== FXML — DADOS TÉCNICOS =====
    @FXML private Label    lblMarca, lblModelo, lblCor, lblCategoria, lblPrecoUnitario;
    @FXML private Label    lblDataAquisicao, lblDataInstalacao, lblNumeroFatura;
    @FXML private Label    lblCreatedAt, lblUpdatedAt;
    @FXML private TextArea txtCaracteristicasTecnicas, txtDescricao, txtObservacoes;

    // ===== FXML — HISTÓRICO =====
    @FXML private ListView<String> listVerificacoes;
    @FXML private ListView<String> listManutencoes;

    // ===== FXML — BOTTOM =====
    @FXML private Label lblUltimaAtualizacao;
    @FXML private Label lblIdRegistro;
    @FXML private Label lblIdBanco;

    // ===== ESTADO =====
    private InventarioEquipamentos equipamento;
    private CatalogoProdutos catalogo;

    // ===== INITIALIZE =====
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Nada — os dados chegam via rota após show().
    }

    // ===== ROTA DE ENTRADA (chamada pelo list após show()) =====
    @GetMapping("carregar/id")
    public ResponseData carregarPorId(@RouteVar("id") String id) {
        CompletableFuture.runAsync(() -> {
            var resp = Rotas.get("inventario-equipamentos/service/por/id",
                    Params.with("id", id));
            if (!resp.isSuccess()) {
                Platform.runLater(() -> lblSubtitulo.setText("Erro: " + resp.getMessage()));
                return;
            }
            InventarioEquipamentos e = resp.getData("equipamento", InventarioEquipamentos.class);
            if (e == null) {
                Platform.runLater(() -> lblSubtitulo.setText("Equipamento não encontrado."));
                return;
            }
            // Carrega o produto de catálogo (dados técnicos) em paralelo
            CatalogoProdutos c = null;
            if (e.skuProduto() != null) {
                var rc = Rotas.get("catalogo-produtos/service/por/sku",
                        Params.with("sku", e.skuProduto()));
                if (rc.isSuccess()) {
                    c = rc.getData("produto", CatalogoProdutos.class);
                }
            }
            final CatalogoProdutos cat = c;
            Platform.runLater(() -> {
                this.equipamento = e;
                this.catalogo = cat;
                preencherInterface();
            });
        });
        return ResponseData.success();
    }

    private void preencherInterface() {
        if (equipamento == null) return;

        // Header
        String titulo = catalogo != null
                ? nvl(catalogo.marca()) + " " + nvl(catalogo.modelo())
                : "Equipamento";
        lblTituloEquipamento.setText(titulo);
        lblSubtitulo.setText("Série: " + nvl(equipamento.numSerie()));

        aplicarBadge(lblStatusBadge, equipamento.status());
        aplicarBadge(lblCondicaoBadge, equipamento.condicao());

        lblTempoUso.setText(calcularTempoUso());
        lblProximaVerificacao.setText(calcularProximaVerificacao());

        // Cards
        lblNumSerie.setText(nvl(equipamento.numSerie()));
        lblEnderecoMac.setText(nvl(equipamento.enderecoMac()));
        lblSku.setText(nvl(equipamento.skuProduto()));
        lblTipoEquipamento.setText(catalogo != null
                ? nvl(catalogo.tipoProduto() != null ? catalogo.tipoProduto().toString() : "-")
                + " — " + nvl(catalogo.categoria())
                : "Não catalogado");

        lblDepartamento.setText(nvl(equipamento.departamento()));
        lblLocalizacao.setText(nvl(equipamento.localizacao()));
        lblFuncionarioId.setText(nvl(equipamento.funcionarioId()));
        lblFuncionarioNome.setText("—");
        lblFuncao.setText("—");

        double score = calcularScoreSaude();
        lblStatusScore.setText(String.format("%.0f%%", score));
        if (arcStatusProgress != null) {
            arcStatusProgress.setLength(-score * 3.6);   // 100% = 360°
        }
        if (pbPerformance != null) pbPerformance.setProgress(score / 100.0);
        if (lblPerformancePercent != null) lblPerformancePercent.setText(String.format("%.0f%%", score));

        lblStatus.setText(nvl(equipamento.status()));
        lblCondicao.setText(nvl(equipamento.condicao()));
        lblUltimaVerificacao.setText(equipamento.dataUltimaVerificacao() != null
                ? equipamento.dataUltimaVerificacao().format(DATE_FMT) : "Nunca");
        lblDevolucao.setText(Boolean.TRUE.equals(equipamento.devolucao()) ? "Sim" : "Não");

        // Técnicos
        if (catalogo != null) {
            lblMarca.setText(nvl(catalogo.marca()));
            lblModelo.setText(nvl(catalogo.modelo()));
            lblCor.setText(nvl(catalogo.cor()));
            lblCategoria.setText(nvl(catalogo.categoria()));
            lblPrecoUnitario.setText(formatarCentavos(catalogo.precoUnitarioCentavos()));
            txtCaracteristicasTecnicas.setText(nvl(catalogo.caracteristicasTecnicas()));
            txtDescricao.setText(nvl(catalogo.descricao()));
        }
        lblDataAquisicao.setText(equipamento.dataAquisicao() != null
                ? equipamento.dataAquisicao().format(DATE_FMT) : "-");
        lblDataInstalacao.setText(equipamento.dataInstalacao() != null
                ? equipamento.dataInstalacao().format(DATE_FMT) : "-");
        lblNumeroFatura.setText(nvl(equipamento.numeroFatura()));
        lblCreatedAt.setText(equipamento.createdAt() != null
                ? equipamento.createdAt().format(DATE_TIME_FMT) : "-");
        lblUpdatedAt.setText(equipamento.updatedAt() != null
                ? equipamento.updatedAt().format(DATE_TIME_FMT) : "-");
        txtObservacoes.setText(nvl(equipamento.observacoes()));

        // Bottom
        lblUltimaAtualizacao.setText(equipamento.updatedAt() != null
                ? equipamento.updatedAt().format(DATE_TIME_FMT) : "-");
        lblIdRegistro.setText("#INV-" + abreviar(equipamento.id()));
        lblIdBanco.setText(abreviar(equipamento.id()));

        // Listas de histórico (em memória — dívida: virão por rota)
        listVerificacoes.getItems().clear();
        listVerificacoes.getItems().add("Última verificação: " +
                (equipamento.dataUltimaVerificacao() != null
                        ? equipamento.dataUltimaVerificacao().format(DATE_FMT) : "Nunca"));
        listManutencoes.getItems().clear();
    }

    // ===== MÉTODOS DE AÇÃO (nome = fx:id) =====

    public void btn_fechar(ActionEvent event) {
        fecharJanela();
    }

    public void btn_editar(ActionEvent event) {
        if (equipamento == null) return;
        // Fecha o detail e delega ao list para abrir o form em edição.
        // O list tem @FloatingWindow do form e conhece o id.
        Rotas.get("inventario-list/abrir-edicao", Params.with("id", equipamento.id()));
        fecharJanela();
    }

    public void btn_historico(ActionEvent event) {
        if (equipamento == null) return;
        // Abre a janela de histórico com pré-filtro (sku + funcionário).
        Rotas.get("inventario-list/abrir-historico",
                Params.with("sku", nvl(equipamento.skuProduto()))
                        .and("funcionarioid", nvl(equipamento.funcionarioId())));
        fecharJanela();
    }

    public void btn_relatorio(ActionEvent event) {
        alertaInfo("Relatório", "Em desenvolvimento.");
    }

    public void btn_excluir(ActionEvent event) {
        if (equipamento == null) return;
        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Confirmar Exclusão");
        confirmacao.setHeaderText(null);
        confirmacao.setContentText("Excluir equipamento " + nvl(equipamento.numSerie()) + "?\n"
                + "O evento será registrado no histórico.");
        confirmacao.showAndWait().ifPresent(botao -> {
            if (botao == ButtonType.OK) executarExclusao();
        });
    }

    public void btn_verificacao(ActionEvent event) {
        if (equipamento == null) return;
        CompletableFuture.runAsync(() -> {
            var resp = Rotas.put("inventario-crud/service/verificacao/registrar",
                    Params.with("id", equipamento.id())
                            .and("funcionarioid", "SISTEMA")
                            .and("data", LocalDate.now().toString())
                            .and("condicao", equipamento.condicao())
                            .and("observacoes", "Verificação manual via detalhes")
                            .and("descricao", "{}"));
            Platform.runLater(() -> {
                if (resp.isSuccess()) {
                    Rotas.exec("inventario-list/refresh");
                    recarregar();
                } else {
                    alertaInfo("Erro", resp.getFirstError());
                }
            });
        });
    }

    public void btn_manutencao(ActionEvent event) {
        if (equipamento == null) return;
        CompletableFuture.runAsync(() -> {
            var resp = Rotas.put("inventario-crud/service/manutencao/enviar",
                    Params.with("id", equipamento.id())
                            .and("funcionarioid", "SISTEMA")
                            .and("motivo", "Envio via detalhes")
                            .and("descricao", "{}"));
            Platform.runLater(() -> {
                if (resp.isSuccess()) {
                    Rotas.exec("inventario-list/refresh");
                    recarregar();
                } else {
                    alertaInfo("Erro", resp.getFirstError());
                }
            });
        });
    }

    public void btn_qrCode(ActionEvent event) {
        alertaInfo("QR Code", "Em desenvolvimento.");
    }

    public void btn_anexar(ActionEvent event) {
        alertaInfo("Documento", "Em desenvolvimento.");
    }

    public void btn_baixar(ActionEvent event) {
        if (equipamento == null) return;
        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Confirmar Baixa");
        confirmacao.setHeaderText(null);
        confirmacao.setContentText("Baixar equipamento " + nvl(equipamento.numSerie()) + "?");
        confirmacao.showAndWait().ifPresent(botao -> {
            if (botao == ButtonType.OK) {
                CompletableFuture.runAsync(() -> {
                    var resp = Rotas.put("inventario-crud/service/baixar",
                            Params.with("id", equipamento.id())
                                    .and("funcionarioid", "SISTEMA")
                                    .and("motivo", "Baixa via detalhes")
                                    .and("descricao", "{}"));
                    Platform.runLater(() -> {
                        if (resp.isSuccess()) {
                            Rotas.exec("inventario-list/refresh");
                            recarregar();
                        } else {
                            alertaInfo("Erro", resp.getFirstError());
                        }
                    });
                });
            }
        });
    }

    // ===== HELPERS =====

    private void executarExclusao() {
        CompletableFuture.runAsync(() -> {
            var resp = Rotas.delete("inventario-crud/service/excluir",
                    Params.with("id", equipamento.id())
                            .and("funcionarioid", "SISTEMA")
                            .and("descricao", "{}"));
            Platform.runLater(() -> {
                if (resp.isSuccess()) {
                    Rotas.exec("inventario-list/refresh");
                    fecharJanela();
                } else {
                    alertaInfo("Erro", resp.getFirstError());
                }
            });
        });
    }

    private void recarregar() {
        if (equipamento != null) carregarPorId(equipamento.id());
    }

    private void fecharJanela() {
        if (btn_fechar != null && btn_fechar.getScene() != null) {
            ((Stage) btn_fechar.getScene().getWindow()).close();
        }
    }

    private void alertaInfo(String titulo, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.setContentText(msg != null ? msg : "Sem detalhes.");
        a.showAndWait();
    }

    private void aplicarBadge(Label badge, String valor) {
        if (badge == null || valor == null) return;
        badge.setText(valor);
        String cor = switch (valor.toUpperCase()) {
            case "ATIVO", "EM_USO"   -> "#10b981";
            case "MANUTENCAO"        -> "#f59e0b";
            case "BAIXADO"           -> "#6b7280";
            case "RESERVA"           -> "#8b5cf6";
            case "OTIMO"             -> "#10b981";
            case "BOM"               -> "#3b82f6";
            case "REGULAR"           -> "#f59e0b";
            case "CRITICO"           -> "#ef4444";
            default                  -> "#6b7280";
        };
        badge.setStyle("-fx-background-color: " + cor + "; -fx-text-fill: white;"
                + " -fx-padding: 4 12; -fx-background-radius: 12; -fx-font-weight: bold;");
    }

    private double calcularScoreSaude() {
        double base = switch (nvl(equipamento.status()).toUpperCase()) {
            case "ATIVO" -> 90; case "EM_USO" -> 80; case "RESERVA" -> 70;
            case "MANUTENCAO" -> 40; default -> 10;
        };
        base += switch (nvl(equipamento.condicao()).toUpperCase()) {
            case "OTIMO" -> 10; case "BOM" -> 5; case "REGULAR" -> -10; case "CRITICO" -> -30;
            default -> 0;
        };
        return Math.max(0, Math.min(100, base));
    }

    private String calcularTempoUso() {
        if (equipamento.dataAquisicao() == null) return "-";
        Period p = Period.between(equipamento.dataAquisicao(), LocalDate.now());
        return p.getYears() + " anos, " + p.getMonths() + " meses";
    }

    private String calcularProximaVerificacao() {
        if (equipamento.dataUltimaVerificacao() == null) return "-";
        return equipamento.dataUltimaVerificacao().plusYears(1).format(DATE_FMT);
    }

    private String formatarCentavos(Integer centavos) {
        if (centavos == null) return "€ 0,00";
        return String.format("€ %.2f", centavos / 100.0);
    }

    private String abreviar(String id) {
        return (id != null && id.length() > 8) ? id.substring(0, 8) + "…" : (id != null ? id : "-");
    }

    private String nvl(String s) { return s != null ? s : ""; }

    public void cleanup() {
        equipamento = null;
        catalogo = null;
    }
}