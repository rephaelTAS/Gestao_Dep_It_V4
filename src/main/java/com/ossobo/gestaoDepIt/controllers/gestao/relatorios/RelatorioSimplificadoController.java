/*
 * RelatorioSimplificadoController v1.0
 *
 * Controller do Relatório Simples.
 * Seleciona uma tabela, aplica filtros, exibe dados e exporta para Excel.
 *
 * v1.0: Versão inicial — 6 tabelas suportadas, filtros dinâmicos, exportação Excel
 */
package com.ossobo.gestaoDepIt.controllers.gestao.relatorios;

import com.ossobo.gestaoDepIt.db.models.*;
import com.ossobo.gestaoDepIt.db.services.*;
import com.ossobo.nexusfx.NexusFX;
import com.ossobo.nexusfx.di.annotations.Controller;
import com.ossobo.nexusfx.di.annotations.Inject;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class RelatorioSimplificadoController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(RelatorioSimplificadoController.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Inject private InventarioEquipamentosService inventarioService;
    @Inject private CatalogoProdutosService catalogoService;
    @Inject private FuncionariosService funcionariosService;
    @Inject private EstoqueMovimentacoesService movimentacoesService;
    @Inject private UsuariosService usuariosService;
    @Inject private HistoricoEventosService historicoService;

    // ===== CONFIGURAÇÃO =====
    @FXML private ComboBox<String> comboTabela;
    @FXML private ComboBox<String> comboFiltro1;
    @FXML private Label labelFiltro1;
    @FXML private DatePicker dpInicio, dpFim;
    @FXML private CheckBox checkApenasAtivos, checkIncluirCabecalho;

    // ===== RESULTADOS =====
    @FXML private TableView<ObservableList<String>> tabelaResultados;
    @FXML private Label lblTotalRegistros, lblStatus, lblFiltroAtivo;

    // ===== ESTADO =====
    private String tabelaSelecionada = "";
    private List<?> dadosAtuais = new ArrayList<>();

    private static final Map<String, String> TABELAS = new LinkedHashMap<>();
    static {
        TABELAS.put("inventario_equipamentos", "Inventário de Equipamentos");
        TABELAS.put("catalogo_produtos", "Catálogo de Produtos");
        TABELAS.put("funcionarios", "Funcionários");
        TABELAS.put("estoque_movimentacoes", "Movimentações de Estoque");
        TABELAS.put("usuarios", "Usuários do Sistema");
        TABELAS.put("historico_eventos", "Histórico de Eventos");
    }

    // =========================================================================
    // INITIALIZE
    // =========================================================================

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        comboTabela.getItems().addAll(TABELAS.values());
        comboTabela.setValue(TABELAS.get("inventario_equipamentos"));
        comboTabela.setOnAction(e -> onTabelaChanged());
        onTabelaChanged();
    }

    // =========================================================================
    // MUDANÇA DE TABELA → ATUALIZA FILTROS
    // =========================================================================

    private void onTabelaChanged() {
        String nomeExibicao = comboTabela.getValue();
        tabelaSelecionada = TABELAS.entrySet().stream()
                .filter(e -> e.getValue().equals(nomeExibicao))
                .map(Map.Entry::getKey)
                .findFirst().orElse("inventario_equipamentos");

        comboFiltro1.getItems().clear();
        comboFiltro1.setValue(null);
        dpInicio.setValue(null);
        dpFim.setValue(null);
        tabelaResultados.getColumns().clear();
        tabelaResultados.getItems().clear();
        lblTotalRegistros.setText("0 registros");
        lblFiltroAtivo.setText("Nenhum filtro aplicado");

        switch (tabelaSelecionada) {
            case "inventario_equipamentos" -> {
                labelFiltro1.setText("Status:");
                comboFiltro1.getItems().addAll("TODOS", "ATIVO", "MANUTENCAO", "BAIXADO", "RESERVA", "EM_USO");
                comboFiltro1.setValue("TODOS");
            }
            case "catalogo_produtos" -> {
                labelFiltro1.setText("Tipo:");
                comboFiltro1.getItems().addAll("TODOS", "EQUIPAMENTO", "CONSUMIVEL", "TONER", "ACESSORIO", "SOFTWARE");
                comboFiltro1.setValue("TODOS");
            }
            case "funcionarios" -> {
                labelFiltro1.setText("Departamento:");
                try {
                    comboFiltro1.getItems().add("TODOS");
                    comboFiltro1.getItems().addAll(funcionariosService.obterDepartamentos());
                    comboFiltro1.setValue("TODOS");
                } catch (Exception ignored) {}
            }
            case "estoque_movimentacoes" -> {
                labelFiltro1.setText("Tipo:");
                comboFiltro1.getItems().addAll("TODOS", "ENTRADA", "SAIDA", "AJUSTE", "RESERVA");
                comboFiltro1.setValue("TODOS");
            }
            case "usuarios" -> {
                labelFiltro1.setText("Nível:");
                comboFiltro1.getItems().addAll("TODOS", "ADMIN", "GESTOR", "SUPERVISOR", "OPERADOR", "READONLY");
                comboFiltro1.setValue("TODOS");
            }
            case "historico_eventos" -> {
                labelFiltro1.setText("Tipo de Evento:");
                comboFiltro1.getItems().addAll("TODOS", "CRIACAO", "ATUALIZACAO", "BAIXA", "MANUTENCAO", "MOVIMENTACAO", "LOGIN", "LOGOUT", "INSTALACAO");
                comboFiltro1.setValue("TODOS");
            }
        }
    }

    // =========================================================================
    // GERAR RELATÓRIO
    // =========================================================================

    @FXML
    private void handleGerarRelatorio() {
        lblStatus.setText("Gerando relatório...");
        String tabela = tabelaSelecionada;
        String filtro = comboFiltro1.getValue();
        LocalDate inicio = dpInicio.getValue();
        LocalDate fim = dpFim.getValue();
        boolean apenasAtivos = checkApenasAtivos.isSelected();

        new Thread(() -> {
            try {
                switch (tabela) {
                    case "inventario_equipamentos" -> gerarInventario(filtro, inicio, fim, apenasAtivos);
                    case "catalogo_produtos" -> gerarCatalogo(filtro, apenasAtivos);
                    case "funcionarios" -> gerarFuncionarios(filtro, apenasAtivos);
                    case "estoque_movimentacoes" -> gerarMovimentacoes(filtro, inicio, fim);
                    case "usuarios" -> gerarUsuarios(filtro, apenasAtivos);
                    case "historico_eventos" -> gerarHistorico(filtro, inicio, fim);
                }
                Platform.runLater(() -> {
                    lblStatus.setText("Relatório gerado com sucesso.");
                    lblFiltroAtivo.setText("Tabela: " + comboTabela.getValue() +
                            (filtro != null && !"TODOS".equals(filtro) ? " | Filtro: " + filtro : ""));
                });
            } catch (Exception e) {
                logger.error("Erro ao gerar relatório", e);
                Platform.runLater(() -> {
                    lblStatus.setText("Erro ao gerar relatório.");
                    NexusFX.alerts().erro("Erro", e.getMessage(), "Relatórios");
                });
            }
        }).start();
    }

    // =========================================================================
    // GERADORES POR TABELA
    // =========================================================================

    private void gerarInventario(String status, LocalDate inicio, LocalDate fim, boolean apenasAtivos) {
        List<InventarioEquipamentos> lista = inventarioService.listarTodos(1, 10000);
        List<InventarioEquipamentos> filtrados = lista.stream()
                .filter(e -> status == null || "TODOS".equals(status) || status.equals(e.getStatus()))
                .filter(e -> !apenasAtivos || "ATIVO".equals(e.getStatus()) || "EM_USO".equals(e.getStatus()))
                .filter(e -> inicio == null || (e.getDataAquisicao() != null && !e.getDataAquisicao().isBefore(inicio)))
                .filter(e -> fim == null || (e.getDataAquisicao() != null && !e.getDataAquisicao().isAfter(fim)))
                .collect(Collectors.toList());
        dadosAtuais = filtrados;

        Platform.runLater(() -> {
            tabelaResultados.getColumns().clear();
            String[] colunas = {"ID", "SKU", "Nº Série", "MAC", "Localização", "Departamento", "Status", "Condição", "Funcionário", "Data Aquisição"};
            for (int i = 0; i < colunas.length; i++) {
                final int idx = i;
                TableColumn<ObservableList<String>, String> col = new TableColumn<>(colunas[i]);
                col.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get(idx)));
                col.setPrefWidth(colunas[i].length() * 10 + 30);
                tabelaResultados.getColumns().add(col);
            }
            ObservableList<ObservableList<String>> dados = FXCollections.observableArrayList();
            for (InventarioEquipamentos e : filtrados) {
                dados.add(FXCollections.observableArrayList(
                        String.valueOf(e.getId()), nvl(e.getSkuProduto()), nvl(e.getNumSerie()),
                        nvl(e.getEnderecoMac()), nvl(e.getLocalizacao()), nvl(e.getDepartamento()),
                        nvl(e.getStatus()), nvl(e.getCondicao()), nvl(e.getFuncionarioId()),
                        e.getDataAquisicao() != null ? e.getDataAquisicao().format(DATE_FMT) : "-"
                ));
            }
            tabelaResultados.setItems(dados);
            lblTotalRegistros.setText(filtrados.size() + " registros");
        });
    }

    private void gerarCatalogo(String tipo, boolean apenasAtivos) {
        List<CatalogoProdutos> lista = catalogoService.listarTodos(1, 10000);
        List<CatalogoProdutos> filtrados = lista.stream()
                .filter(c -> tipo == null || "TODOS".equals(tipo) || (c.getTipoProduto() != null && tipo.equals(c.getTipoProduto().toString())))
                .filter(c -> !apenasAtivos || Boolean.TRUE.equals(c.getAtivo()))
                .collect(Collectors.toList());
        dadosAtuais = filtrados;

        Platform.runLater(() -> {
            tabelaResultados.getColumns().clear();
            String[] colunas = {"SKU", "Tipo", "Categoria", "Marca", "Modelo", "Cor", "Preço Unitário", "Estoque", "Ativo"};
            for (int i = 0; i < colunas.length; i++) {
                final int idx = i;
                TableColumn<ObservableList<String>, String> col = new TableColumn<>(colunas[i]);
                col.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get(idx)));
                col.setPrefWidth(colunas[i].length() * 10 + 30);
                tabelaResultados.getColumns().add(col);
            }
            ObservableList<ObservableList<String>> dados = FXCollections.observableArrayList();
            for (CatalogoProdutos c : filtrados) {
                dados.add(FXCollections.observableArrayList(
                        nvl(c.getSku()), c.getTipoProduto() != null ? c.getTipoProduto().toString() : "-",
                        nvl(c.getCategoria()), nvl(c.getMarca()), nvl(c.getModelo()), nvl(c.getCor()),
                        c.getPrecoUnitario() != null ? "€ " + c.getPrecoUnitario() : "€ 0",
                        String.valueOf(c.getTotalRecebido() != null ? c.getTotalRecebido() : 0),
                        Boolean.TRUE.equals(c.getAtivo()) ? "Sim" : "Não"
                ));
            }
            tabelaResultados.setItems(dados);
            lblTotalRegistros.setText(filtrados.size() + " registros");
        });
    }

    private void gerarFuncionarios(String departamento, boolean apenasAtivos) {
        List<Funcionarios> lista = funcionariosService.listarTodos(1, 10000);
        List<Funcionarios> filtrados = lista.stream()
                .filter(f -> departamento == null || "TODOS".equals(departamento) || departamento.equals(f.getDepartamento()))
                .filter(f -> !apenasAtivos || Boolean.TRUE.equals(f.getAtivo()))
                .collect(Collectors.toList());
        dadosAtuais = filtrados;

        Platform.runLater(() -> {
            tabelaResultados.getColumns().clear();
            String[] colunas = {"Código", "Nome", "Função", "Departamento", "Local Trabalho", "Email", "Telefone", "Ativo"};
            for (int i = 0; i < colunas.length; i++) {
                final int idx = i;
                TableColumn<ObservableList<String>, String> col = new TableColumn<>(colunas[i]);
                col.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get(idx)));
                col.setPrefWidth(colunas[i].length() * 10 + 30);
                tabelaResultados.getColumns().add(col);
            }
            ObservableList<ObservableList<String>> dados = FXCollections.observableArrayList();
            for (Funcionarios f : filtrados) {
                dados.add(FXCollections.observableArrayList(
                        nvl(f.getCodDep()), nvl(f.getNome()), nvl(f.getFuncao()),
                        nvl(f.getDepartamento()), nvl(f.getLocalTrabalho()),
                        nvl(f.getEmail()), nvl(f.getTelefone()),
                        Boolean.TRUE.equals(f.getAtivo()) ? "Sim" : "Não"
                ));
            }
            tabelaResultados.setItems(dados);
            lblTotalRegistros.setText(filtrados.size() + " registros");
        });
    }

    private void gerarMovimentacoes(String tipo, LocalDate inicio, LocalDate fim) {
        List<EstoqueMovimentacoes> lista = movimentacoesService.listarTodas(1, 10000);
        List<EstoqueMovimentacoes> filtrados = lista.stream()
                .filter(m -> tipo == null || "TODOS".equals(tipo) || (m.getTipoMovimentacao() != null && tipo.equals(m.getTipoMovimentacao().toString())))
                .filter(m -> inicio == null || (m.getDataMovimentacao() != null && !m.getDataMovimentacao().isBefore(inicio)))
                .filter(m -> fim == null || (m.getDataMovimentacao() != null && !m.getDataMovimentacao().isAfter(fim)))
                .collect(Collectors.toList());
        dadosAtuais = filtrados;

        Platform.runLater(() -> {
            tabelaResultados.getColumns().clear();
            String[] colunas = {"ID", "SKU", "Tipo", "Quantidade", "Lote", "Data", "Localização", "Funcionário", "Motivo"};
            for (int i = 0; i < colunas.length; i++) {
                final int idx = i;
                TableColumn<ObservableList<String>, String> col = new TableColumn<>(colunas[i]);
                col.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get(idx)));
                col.setPrefWidth(colunas[i].length() * 10 + 30);
                tabelaResultados.getColumns().add(col);
            }
            ObservableList<ObservableList<String>> dados = FXCollections.observableArrayList();
            for (EstoqueMovimentacoes m : filtrados) {
                dados.add(FXCollections.observableArrayList(
                        String.valueOf(m.getId()), nvl(m.getSkuProduto()),
                        m.getTipoMovimentacao() != null ? m.getTipoMovimentacao().toString() : "-",
                        String.valueOf(m.getQuantidade() != null ? m.getQuantidade() : 0),
                        nvl(m.getLote()),
                        m.getDataMovimentacao() != null ? m.getDataMovimentacao().format(DATE_FMT) : "-",
                        nvl(m.getLocalizacao()), nvl(m.getCodDepFuncionario()), nvl(m.getMotivo())
                ));
            }
            tabelaResultados.setItems(dados);
            lblTotalRegistros.setText(filtrados.size() + " registros");
        });
    }

    private void gerarUsuarios(String nivel, boolean apenasAtivos) {
        List<Usuario> lista = usuariosService.listarTodos(1, 10000);
        List<Usuario> filtrados = lista.stream()
                .filter(u -> nivel == null || "TODOS".equals(nivel) || nivel.equals(u.getNivelAcesso()))
                .filter(u -> !apenasAtivos || Boolean.TRUE.equals(u.getAtivo()))
                .collect(Collectors.toList());
        dadosAtuais = filtrados;

        Platform.runLater(() -> {
            tabelaResultados.getColumns().clear();
            String[] colunas = {"ID", "Nome", "Email", "Funcionário ID", "Nível Acesso", "Último Login", "Ativo"};
            for (int i = 0; i < colunas.length; i++) {
                final int idx = i;
                TableColumn<ObservableList<String>, String> col = new TableColumn<>(colunas[i]);
                col.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get(idx)));
                col.setPrefWidth(colunas[i].length() * 10 + 30);
                tabelaResultados.getColumns().add(col);
            }
            ObservableList<ObservableList<String>> dados = FXCollections.observableArrayList();
            for (Usuario u : filtrados) {
                dados.add(FXCollections.observableArrayList(
                        String.valueOf(u.getId()), nvl(u.getNome()), nvl(u.getEmail()),
                        nvl(u.getFuncionarioId()), nvl(u.getNivelAcesso()),
                        u.getUltimoLogin() != null ? u.getUltimoLogin().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "-",
                        Boolean.TRUE.equals(u.getAtivo()) ? "Sim" : "Não"
                ));
            }
            tabelaResultados.setItems(dados);
            lblTotalRegistros.setText(filtrados.size() + " registros");
        });
    }

    private void gerarHistorico(String tipoEvento, LocalDate inicio, LocalDate fim) {
        List<HistoricoEventos> lista = historicoService.listarTodos(1, 10000);
        List<HistoricoEventos> filtrados = lista.stream()
                .filter(h -> tipoEvento == null || "TODOS".equals(tipoEvento) || tipoEvento.equals(h.getTipoEvento()))
                .filter(h -> inicio == null || (h.getCreatedAt() != null && !h.getCreatedAt().toLocalDate().isBefore(inicio)))
                .filter(h -> fim == null || (h.getCreatedAt() != null && !h.getCreatedAt().toLocalDate().isAfter(fim)))
                .collect(Collectors.toList());
        dadosAtuais = filtrados;

        Platform.runLater(() -> {
            tabelaResultados.getColumns().clear();
            String[] colunas = {"ID", "Tipo Evento", "SKU", "Funcionário", "Data/Hora"};
            for (int i = 0; i < colunas.length; i++) {
                final int idx = i;
                TableColumn<ObservableList<String>, String> col = new TableColumn<>(colunas[i]);
                col.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get(idx)));
                col.setPrefWidth(colunas[i].length() * 12 + 30);
                tabelaResultados.getColumns().add(col);
            }
            ObservableList<ObservableList<String>> dados = FXCollections.observableArrayList();
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            for (HistoricoEventos h : filtrados) {
                dados.add(FXCollections.observableArrayList(
                        String.valueOf(h.getId()), nvl(h.getTipoEvento()), nvl(h.getSkuProduto()),
                        nvl(h.getFuncionarioId()),
                        h.getCreatedAt() != null ? h.getCreatedAt().format(dtf) : "-"
                ));
            }
            tabelaResultados.setItems(dados);
            lblTotalRegistros.setText(filtrados.size() + " registros");
        });
    }

    // =========================================================================
    // EXPORTAR EXCEL
    // =========================================================================

    @FXML
    private void handleExportarExcel() {
        if (dadosAtuais.isEmpty()) {
            NexusFX.alerts().warn("Sem Dados", "Gere o relatório primeiro.", "Relatórios");
            return;
        }

        FileChooser fc = new FileChooser();
        fc.setTitle("Salvar Relatório Excel");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel (*.xlsx)", "*.xlsx"));
        fc.setInitialFileName(tabelaSelecionada + "_" + LocalDate.now() + ".xlsx");

        Stage stage = (Stage) tabelaResultados.getScene().getWindow();
        File file = fc.showSaveDialog(stage);

        if (file != null) {
            new Thread(() -> {
                try {
                    boolean comCabecalho = checkIncluirCabecalho.isSelected();
                    switch (tabelaSelecionada) {
                        case "inventario_equipamentos" -> ExportarExcelService.exportarInventarioCompleto(
                                file.getAbsolutePath(), (List<InventarioEquipamentos>) dadosAtuais, List.of(), List.of());
                        case "catalogo_produtos" -> ExportarExcelService.exportarCatalogo(
                                file.getAbsolutePath(), (List<CatalogoProdutos>) dadosAtuais);
                        case "funcionarios" -> ExportarExcelService.exportarFuncionarios(
                                file.getAbsolutePath(), (List<Funcionarios>) dadosAtuais);
                        case "estoque_movimentacoes" -> ExportarExcelService.exportarMovimentacoes(
                                file.getAbsolutePath(), (List<EstoqueMovimentacoes>) dadosAtuais);
                        default -> throw new UnsupportedOperationException("Exportação não suportada para: " + tabelaSelecionada);
                    }
                    Platform.runLater(() -> NexusFX.alerts().info("Exportação Concluída",
                            "Arquivo salvo em:\n" + file.getAbsolutePath(), "Relatórios"));
                } catch (Exception e) {
                    logger.error("Erro ao exportar", e);
                    Platform.runLater(() -> NexusFX.alerts().erro("Erro", e.getMessage(), "Relatórios"));
                }
            }).start();
        }
    }

    private String nvl(String s) { return s != null && !s.isEmpty() ? s : "-"; }
}