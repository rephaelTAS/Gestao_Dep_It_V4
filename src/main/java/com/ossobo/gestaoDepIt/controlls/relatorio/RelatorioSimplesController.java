package com.ossobo.gestaoDepIt.controlls.relatorio;

import com.ossobo.gestaoDepIt.config.ViewConstant;
import com.ossobo.gestaoDepIt.controlls.relatorio.simples.SimplesActions;
import com.ossobo.gestaoDepIt.controlls.relatorio.simples.SimplesFilters;
import com.ossobo.gestaoDepIt.controlls.relatorio.simples.SimplesState;
import com.ossobo.gestaoDepIt.controlls.relatorio.simples.SimplesUIUpdater;
import com.ossobo.winterfx.anotations.Controller;
import com.ossobo.winterfx.anotations.PostConstruct;
import com.ossobo.winterfx.view.anotations.RegisterView;
import com.ossobo.winterfx.view.controller.WinterFXController;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;
import java.util.Map;

/**
 * RelatorioSimplesController v1.0
 *
 * Tela de relatório simples: escolhe tabela + filtro + período,
 * executa e exibe resultado em TableView com colunas dinâmicas.
 */
@Controller(proxy = false)
@RegisterView(
        id = ViewConstant.Relatorio.SIMPLIFICADO,
        fxml = "/META-INF/gestaoDepIt/fxmls/relatorio/relatoriosimplificado.fxml",
        title = "Relatório Simples"
)
public class RelatorioSimplesController implements WinterFXController {

    private static final System.Logger logger =
            System.getLogger(RelatorioSimplesController.class.getName());

    private final SimplesState state = new SimplesState();
    private final SimplesFilters filters = new SimplesFilters();

    // ---- Config ----
    @FXML private ComboBox<String> comboTabela;
    @FXML private Label labelFiltro1;
    @FXML private ComboBox<String> comboFiltro1;
    @FXML private DatePicker dpInicio;
    @FXML private DatePicker dpFim;
    @FXML private CheckBox checkApenasAtivos;
    @FXML private CheckBox checkIncluirCabecalho;

    // ---- Ações ----
    @FXML private Button btn_gerarRelatorio;
    @FXML private Button btn_exportarExcel;

    // ---- Tabela ----
    @FXML private TableView<Map<String, Object>> tabelaResultados;
    @FXML private Label lblTotalRegistros;
    @FXML private Label lblStatus;
    @FXML private Label lblFiltroAtivo;

    // ============================================================
    // CICLO DE VIDA
    // ============================================================

    @PostConstruct
    public void init() {
        popularTabelas();
        if (comboTabela != null) {
            comboTabela.getSelectionModel().selectedItemProperty()
                    .addListener((obs, ov, nv) -> onTabelaEscolhida(nv));
        }
    }

    // ============================================================
    // HANDLERS
    // ============================================================

    @FXML
    public void btn_gerarRelatorio() {
        String alias = state.getAliasAtual();
        if (alias.isBlank()) { aviso("Escolha uma tabela."); return; }

        // Filtro simples
        if (comboFiltro1 != null && comboFiltro1.getValue() != null) {
            filters.setFiltro1(comboFiltro1.getValue());
            filters.setColunaFiltro1(colunaFiltroParaAlias(alias));
        } else {
            filters.setFiltro1(null);
            filters.setColunaFiltro1(null);
        }
        filters.setInicio(dpInicio != null ? dpInicio.getValue() : null);
        filters.setFim(dpFim != null ? dpFim.getValue() : null);

        SimplesActions.RelatorioSimplesResultado r = SimplesActions.executar(
                alias,
                state.getColunasSelecionadas().isEmpty()
                        ? state.getColunasDisponiveis()
                        : state.getColunasSelecionadas(),
                filters.paraMapa(),
                0);

        state.setResultadoColunas(r.colunas());
        state.setResultadoLinhas(r.linhas());

        SimplesUIUpdater.aplicarResultado(tabelaResultados, lblStatus, lblTotalRegistros, r);

        if (lblFiltroAtivo != null) {
            lblFiltroAtivo.setText(r.total() + " linha(s) retornada(s)");
        }
    }

    @FXML
    public void btn_exportarExcel() {
        // Delegação futura para rota exportar-xlsx; placeholder por ora
        aviso("Exportação será conectada na próxima fase.");
    }

    // ============================================================
    // INTERNOS
    // ============================================================

    private void popularTabelas() {
        List<String> aliases = SimplesActions.aliasesDisponiveis();
        if (comboTabela != null) {
            comboTabela.getItems().setAll(aliases.stream().map(this::rotulo).toList());
            comboTabela.getItems().add(0, "");
        }
    }

    private void onTabelaEscolhida(String rotulo) {
        if (rotulo == null || rotulo.isBlank()) return;
        String alias = aliasDeRotulo(rotulo);
        state.setAliasAtual(alias);

        List<String> colunas = SimplesActions.colunasDe(alias);
        state.setColunasDisponiveis(colunas);

        if (labelFiltro1 != null) labelFiltro1.setText(rotuloFiltro(alias));
        if (comboFiltro1 != null) {
            comboFiltro1.getItems().clear();
            comboFiltro1.getItems().addAll(valoresFiltro(alias));
        }
    }

    private String colunaFiltroParaAlias(String alias) {
        return switch (alias) {
            case "inventario", "toners" -> "status";
            case "catalogo" -> "categoria";
            case "funcionarios" -> "departamento";
            case "movimentacoes" -> "tipo_movimentacao";
            default -> null;
        };
    }

    private String rotuloFiltro(String alias) {
        return switch (alias) {
            case "inventario" -> "Status:";
            case "catalogo" -> "Categoria:";
            case "funcionarios" -> "Departamento:";
            case "movimentacoes" -> "Tipo:";
            case "toners" -> "Status:";
            default -> "Filtro:";
        };
    }

    private List<String> valoresFiltro(String alias) {
        return switch (alias) {
            case "inventario" -> List.of("ATIVO", "MANUTENCAO", "BAIXADO", "RESERVA", "EM_USO");
            case "movimentacoes" -> List.of("ENTRADA", "SAIDA", "AJUSTE", "RESERVA");
            case "toners" -> List.of("NORMAL", "ATENCAO", "CRITICO", "ESGOTADO");
            default -> List.of();
        };
    }

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

    private String aliasDeRotulo(String rotulo) {
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