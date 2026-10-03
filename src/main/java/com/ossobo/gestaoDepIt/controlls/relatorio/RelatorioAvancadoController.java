package com.ossobo.gestaoDepIt.controlls.relatorio;

import com.ossobo.gestaoDepIt.config.ViewConstant;
import com.ossobo.gestaoDepIt.controlls.relatorio.avancado.*;
import com.ossobo.gestaoDepIt.controlls.relatorio.dados.CombinarDados;
import com.ossobo.gestaoDepIt.db.relatorios.RelatorioModelo;
import com.ossobo.winterfx.anotations.Controller;
import com.ossobo.winterfx.view.anotations.RegisterView;
import com.ossobo.winterfx.view.controller.WinterFXController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.*;

/**
 * RelatorioAvancadoController v1.1
 *
 * Editor Lego: tabela base + colunas + JOINs + colunas por JOIN + filtros.
 * Toda chamada de dados delega a AvancadoActions (rotas internas).
 * v1.1: btn_fechar conectado; imports normalizados.
 */
@Controller(proxy = false)
@RegisterView(
        id = ViewConstant.Relatorio.AVANCADO,
        fxml = "/META-INF/gestaoDepIt/fxmls/relatorio/relatorioavancado.fxml",
        title = "Relatório Avançado",
        primaryCss = "META-INF/gestaoDepIt/css/relatorio/relatorio.css"

)
public class RelatorioAvancadoController implements Initializable,WinterFXController {

    private static final System.Logger logger =
            System.getLogger(RelatorioAvancadoController.class.getName());

    private final AvancadoState state = new AvancadoState();
    private final AvancadoFilters filters = new AvancadoFilters();

    // ---- Painel esquerdo ----
    @FXML private ComboBox<String> comboTabelaBase;
    @FXML private VBox containerColunasBase;
    @FXML private Button btn_selecionarTodasBase;
    @FXML private Button btn_limparTodasBase;
    @FXML private Button btn_fechar;
    @FXML private CheckBox checkJoinFuncionarios;
    @FXML private ScrollPane scrollFuncionarios;
    @FXML private VBox containerColunasFuncionarios;

    @FXML private CheckBox checkJoinCatalogo;
    @FXML private ScrollPane scrollCatalogo;
    @FXML private VBox containerColunasCatalogo;

    @FXML private CheckBox checkJoinMovimentacoes;
    @FXML private ScrollPane scrollMovimentacoes;
    @FXML private VBox containerColunasMovimentacoes;

    @FXML private Button btn_gerarRelatorio;
    @FXML private Button btn_exportarExcel;
    @FXML private Button btn_salvarModelo;

    // ---- Filtros ----
    @FXML private ComboBox<String> comboFiltroStatus;
    @FXML private ComboBox<String> comboFiltroCondicao;
    @FXML private ComboBox<String> comboFiltroLocalizacao;
    @FXML private ComboBox<String> comboFiltroDepartamento;
    @FXML private DatePicker dpInicio;
    @FXML private DatePicker dpFim;
    @FXML private Button btn_aplicarFiltro;
    @FXML private Button btn_limparFiltro;
    @FXML private Label lblFiltroAtivo;

    // ---- Resultado ----
    @FXML private Label lblConfigAtiva;
    @FXML private Label lblTotalRegistros;
    @FXML private TableView<Map<String, Object>> tabelaResultados;
    @FXML private Label lblStatus;
    @FXML private Label lblJoinsAtivos;
    @FXML private Label lblTempoExecucao;

    // ============================================================
    // CICLO DE VIDA
    // ============================================================


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        popularTabelaBase();
        configurarScrollsIniciais();
        configurarFiltros();
        popularFiltrosDinamicos();
    }

    // ============================================================
    // HANDLERS PRINCIPAIS
    // ============================================================

    @FXML
    public void btn_gerarRelatorio(ActionEvent event) {
        RelatorioModelo modelo = AvancadoLegoBuilder.construir(state, filters);
        if (modelo == null) {
            aviso("Selecione a tabela base e ao menos uma coluna.");
            return;
        }
        try {
            long t0 = System.currentTimeMillis();
            AvancadoActions.Resultado r = AvancadoActions.executar(modelo, 0);
            long dt = System.currentTimeMillis() - t0;
            state.setResultadoColunas(r.colunas());
            state.setResultadoLinhas(r.linhas());
            AvancadoUIUpdater.aplicarResultado(
                    tabelaResultados, lblTotalRegistros, lblStatus, lblTempoExecucao, r, dt);
            if (lblConfigAtiva != null)
                lblConfigAtiva.setText(rotulo(state.getBaseAlias()) + " · " + r.total() + " registros");
        } catch (Exception e) {
            logger.log(System.Logger.Level.ERROR,
                    "Falha ao executar avançado: {0}", e.getMessage(), e);
            aviso("Erro ao gerar relatório: " + e.getMessage());
        }
    }

    @FXML
    public void btn_exportarExcel(ActionEvent event) {
        if (state.getResultadoColunas().isEmpty() || state.getResultadoLinhas().isEmpty()) {
            aviso("Gere o relatório antes de exportar.");
            return;
        }
        RelatorioModelo modelo = AvancadoLegoBuilder.construir(state, filters);
        if (modelo == null) {
            aviso("Configure o relatório antes de exportar.");
            return;
        }
        byte[] xlsx = CombinarDados.exportarAvancado(modelo);
        if (xlsx == null || xlsx.length == 0) {
            aviso("Falha ao gerar XLSX.");
            return;
        }
        salvarXlsx(xlsx, modelo.tabelaBase() + ".xlsx");
    }

    private void salvarXlsx(byte[] dados, String nomeSugerido) {
        javafx.stage.FileChooser chooser = new javafx.stage.FileChooser();
        chooser.setTitle("Salvar relatório Excel");
        chooser.setInitialFileName(nomeSugerido);
        chooser.getExtensionFilters().add(
                new javafx.stage.FileChooser.ExtensionFilter("Excel (*.xlsx)", "*.xlsx"));
        java.io.File destino = chooser.showSaveDialog(
                tabelaResultados != null && tabelaResultados.getScene() != null
                        ? tabelaResultados.getScene().getWindow() : null);
        if (destino == null) return;
        try (java.io.FileOutputStream out = new java.io.FileOutputStream(destino)) {
            out.write(dados);
            aviso("Arquivo salvo: " + destino.getName());
        } catch (java.io.IOException e) {
            logger.log(System.Logger.Level.ERROR, "Falha ao salvar XLSX", e);
            aviso("Erro ao salvar: " + e.getMessage());
        }
    }

    @FXML
    public void btn_salvarModelo(ActionEvent event) {
        RelatorioModelo modelo = AvancadoLegoBuilder.construir(state, filters);
        if (modelo == null) { aviso("Configure o relatório antes de salvar."); return; }
        TextInputDialog dlg = new TextInputDialog();
        dlg.setHeaderText("Nome do modelo");
        dlg.setContentText("Nome:");
        dlg.showAndWait().ifPresent(nome -> {
            try {
                com.ossobo.winterfx.router.Rotas.put(
                        "relatorio-modelo/service/salvar",
                        com.ossobo.winterfx.router.model.Params.with("modelo",
                                new RelatorioModelo(
                                        java.util.UUID.randomUUID().toString(),
                                        nome.trim(),
                                        modelo.tabelaBase(),
                                        modelo.colunasBase(),
                                        modelo.joins(),
                                        modelo.filtrosJson(),
                                        java.time.LocalDateTime.now(),
                                        null)));
                aviso("Modelo salvo: " + nome);
            } catch (Exception e) {
                logger.log(System.Logger.Level.WARNING,
                        "Falha ao salvar modelo: {0}", e.getMessage());
                aviso("Erro ao salvar: " + e.getMessage());
            }
        });
    }

    @FXML
    public void btn_aplicarFiltro(ActionEvent event) {
        lerFiltros();
        aviso("Filtros aplicados.");
    }

    @FXML
    public void btn_limparFiltro(ActionEvent event) {
        if (comboFiltroStatus != null) comboFiltroStatus.setValue(null);
        if (comboFiltroCondicao != null) comboFiltroCondicao.setValue(null);
        if (comboFiltroLocalizacao != null) comboFiltroLocalizacao.setValue(null);
        if (comboFiltroDepartamento != null) comboFiltroDepartamento.setValue(null);
        if (dpInicio != null) dpInicio.setValue(null);
        if (dpFim != null) dpFim.setValue(null);
        if (lblFiltroAtivo != null) lblFiltroAtivo.setText("Nenhum filtro aplicado");
    }

    @FXML
    public void btn_selecionarTodasBase(ActionEvent event) {
        AvancadoTableManager.marcarTodos(containerColunasBase, true);
        state.setColunasBaseMarcadas(new LinkedHashSet<>(state.getColunasDisponiveisBase()));
    }

    @FXML
    public void btn_limparTodasBase(ActionEvent event) {
        AvancadoTableManager.marcarTodos(containerColunasBase, false);
        state.setColunasBaseMarcadas(new LinkedHashSet<>());
    }

    @FXML
    public void btn_fechar(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        if (stage != null) stage.close();
    }

    // ============================================================
    // POPULAR
    // ============================================================

    private void popularTabelaBase() {
        if (comboTabelaBase == null) return;
        comboTabelaBase.getItems().setAll(
                AvancadoActions.aliasesDisponiveis().stream().map(this::rotulo).toList());
        comboTabelaBase.getSelectionModel().selectedItemProperty()
                .addListener((obs, ov, nv) -> onBaseEscolhida(nv));
    }

    private void onBaseEscolhida(String rotuloBase) {
        if (rotuloBase == null || rotuloBase.isBlank()) return;
        String alias = aliasDe(rotuloBase);
        state.setBaseAlias(alias);

        List<String> colunas = AvancadoActions.colunasDe(alias);
        state.setColunasDisponiveisBase(colunas);
        Set<String> marcadas = AvancadoTableManager.popularCheckboxesColunas(
                containerColunasBase, colunas, Set.of());
        state.setColunasBaseMarcadas(marcadas);

        configurarJoinsVisiveis(alias);
    }

    private void configurarJoinsVisiveis(String baseAlias) {
        List<String> vizinhos = AvancadoActions.vizinhos(baseAlias);
        visibilidadeJoin(checkJoinCatalogo, scrollCatalogo, vizinhos.contains("catalogo"));
        visibilidadeJoin(checkJoinFuncionarios, scrollFuncionarios, vizinhos.contains("funcionarios"));
        visibilidadeJoin(checkJoinMovimentacoes, scrollMovimentacoes, vizinhos.contains("movimentacoes"));

        if (checkJoinCatalogo != null && !vizinhos.contains("catalogo")) checkJoinCatalogo.setSelected(false);
        if (checkJoinFuncionarios != null && !vizinhos.contains("funcionarios")) checkJoinFuncionarios.setSelected(false);
        if (checkJoinMovimentacoes != null && !vizinhos.contains("movimentacoes")) checkJoinMovimentacoes.setSelected(false);
    }

    private void visibilidadeJoin(CheckBox check, ScrollPane scroll, boolean visivel) {
        if (check != null) { check.setVisible(visivel); check.setManaged(visivel); }
        if (scroll != null) { scroll.setVisible(false); scroll.setManaged(false); }
    }

    private void configurarScrollsIniciais() {
        ligarJoin(checkJoinCatalogo, scrollCatalogo, containerColunasCatalogo, "catalogo");
        ligarJoin(checkJoinFuncionarios, scrollFuncionarios, containerColunasFuncionarios, "funcionarios");
        ligarJoin(checkJoinMovimentacoes, scrollMovimentacoes, containerColunasMovimentacoes, "movimentacoes");
    }

    private void ligarJoin(CheckBox check, ScrollPane scroll, VBox container, String aliasJoin) {
        if (check == null) return;
        check.selectedProperty().addListener((obs, ov, ativo) -> {
            if (scroll != null) { scroll.setVisible(ativo); scroll.setManaged(ativo); }
            if (ativo) {
                List<String> cols = AvancadoActions.colunasDe(aliasJoin);
                Set<String> marcadas = AvancadoTableManager.popularCheckboxesColunas(
                        container, cols, Set.of());
                state.ativarJoin(aliasJoin, cols);
                state.getJoinsAtivos().put(aliasJoin, marcadas);
            } else {
                state.desativarJoin(aliasJoin);
            }
            AvancadoUIUpdater.atualizarJoinsAtivos(lblJoinsAtivos, state);
        });
    }

    private void configurarFiltros() {
        if (comboFiltroStatus != null)
            comboFiltroStatus.getItems().setAll("ATIVO", "MANUTENCAO", "BAIXADO", "RESERVA", "EM_USO");
        if (comboFiltroCondicao != null)
            comboFiltroCondicao.getItems().setAll("OTIMO", "BOM", "REGULAR", "CRITICO");
    }

    /**
     * Popula Localização e Departamento via rotas — tolerante à ausência.
     * Se as rotas não existirem, as combos ficam editáveis e vazias.
     */
    @SuppressWarnings("unchecked")
    private void popularFiltrosDinamicos() {
        try {
            Object r = com.ossobo.winterfx.router.Rotas
                    .get("inventario/service/localizacoes-distintas");
            if (r instanceof com.ossobo.winterfx.router.model.ResponseData rd && rd.isSuccess()) {
                Object dados = rd.getData().get("localizacoes");
                if (dados instanceof List<?> l && comboFiltroLocalizacao != null) {
                    comboFiltroLocalizacao.getItems().setAll((List<String>) l);
                }
            }
        } catch (Exception ignored) {}

        try {
            Object r = com.ossobo.winterfx.router.Rotas
                    .get("funcionario/service/departamentos-distintos");
            if (r instanceof com.ossobo.winterfx.router.model.ResponseData rd && rd.isSuccess()) {
                Object dados = rd.getData().get("departamentos");
                if (dados instanceof List<?> l && comboFiltroDepartamento != null) {
                    comboFiltroDepartamento.getItems().setAll((List<String>) l);
                }
            }
        } catch (Exception ignored) {}
    }

    private void lerFiltros() {
        filters.setStatus(comboFiltroStatus != null ? comboFiltroStatus.getValue() : null);
        filters.setCondicao(comboFiltroCondicao != null ? comboFiltroCondicao.getValue() : null);
        filters.setLocalizacao(comboFiltroLocalizacao != null ? comboFiltroLocalizacao.getValue() : null);
        filters.setDepartamento(comboFiltroDepartamento != null ? comboFiltroDepartamento.getValue() : null);
        filters.setInicio(dpInicio != null ? dpInicio.getValue() : null);
        filters.setFim(dpFim != null ? dpFim.getValue() : null);
        if (lblFiltroAtivo != null) {
            StringBuilder sb = new StringBuilder();
            if (filters.getStatus() != null) sb.append("status=").append(filters.getStatus()).append(" ");
            if (filters.getCondicao() != null) sb.append("condicao=").append(filters.getCondicao()).append(" ");
            lblFiltroAtivo.setText(sb.isEmpty() ? "Nenhum filtro aplicado" : sb.toString().trim());
        }
    }

    // ============================================================
    // UTIL
    // ============================================================

    private String rotulo(String alias) {
        return switch (alias) {
            case "inventario" -> "Inventário";
            case "catalogo" -> "Catálogo";
            case "funcionarios" -> "Funcionários";
            case "usuarios" -> "Usuários";
            case "movimentacoes" -> "Movimentações";
            case "toners" -> "Toners";
            case "historico" -> "Histórico de Eventos";
            default -> alias;
        };
    }

    private String aliasDe(String rotulo) {
        return switch (rotulo) {
            case "Inventário" -> "inventario";
            case "Catálogo" -> "catalogo";
            case "Funcionários" -> "funcionarios";
            case "Usuários" -> "usuarios";
            case "Movimentações" -> "movimentacoes";
            case "Toners" -> "toners";
            case "Histórico de Eventos" -> "historico";
            default -> rotulo;
        };
    }

    private void aviso(String msg) {
        if (lblStatus != null) lblStatus.setText(msg);
    }



}