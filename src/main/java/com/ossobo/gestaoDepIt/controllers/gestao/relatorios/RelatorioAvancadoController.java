/*
 * RelatorioAvancadoController v4.2
 *
 * ATUALIZAÇÃO MINUCIOSA — Todas as colunas de todos os geradores
 * alinhadas exatamente com o mapa COLUNAS.
 *
 * REGRA: Toda chave put() no gerador DEVE existir em COLUNAS.get(tabela).
 * Isso garante que o checkbox correspondente existe e que a coluna
 * aparece na tabela quando selecionada.
 *
 * v4.2: Todos os geradores revisados — chaves 100% alinhadas com COLUNAS
 * v4.1: JOINs sem prefixos de emoji
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
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class RelatorioAvancadoController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(RelatorioAvancadoController.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String MODELOS_FILE = "relatorio_avancado_modelos.json";

    @Inject private InventarioEquipamentosService inventarioService;
    @Inject private CatalogoProdutosService catalogoService;
    @Inject private FuncionariosService funcionariosService;
    @Inject private EstoqueMovimentacoesService movimentacoesService;
    @Inject private UsuariosService usuariosService;
    @Inject private HistoricoEventosService historicoService;

    @FXML private ComboBox<String> comboTabelaBase;
    @FXML private VBox containerColunasBase;
    @FXML private CheckBox checkJoinFuncionarios, checkJoinCatalogo, checkJoinUsuarios, checkJoinMovimentacoes;
    @FXML private ScrollPane scrollFuncionarios, scrollCatalogo, scrollUsuarios, scrollMovimentacoes;
    @FXML private VBox containerColunasFuncionarios, containerColunasCatalogo, containerColunasUsuarios, containerColunasMovimentacoes;
    @FXML private ComboBox<String> comboFiltroStatus, comboFiltroCondicao, comboFiltroLocalizacao, comboFiltroDepartamento;
    @FXML private DatePicker dpInicio, dpFim;
    @FXML private Label lblFiltroAtivo, lblTotalRegistros, lblTempoExecucao, lblStatus, lblConfigAtiva, lblJoinsAtivos;
    @FXML private TableView<ObservableList<String>> tabelaResultados;

    private String tabelaBase = "inventario_equipamentos";
    private final Map<String, CheckBox> cbBase = new LinkedHashMap<>();
    private final Map<String, CheckBox> cbFuncionarios = new LinkedHashMap<>();
    private final Map<String, CheckBox> cbCatalogo = new LinkedHashMap<>();
    private final Map<String, CheckBox> cbUsuarios = new LinkedHashMap<>();
    private final Map<String, CheckBox> cbMovimentacoes = new LinkedHashMap<>();
    private List<Map<String, String>> dadosCombinados = new ArrayList<>();
    private List<Map<String, String>> dadosFiltrados = new ArrayList<>();
    private List<String> todasColunasOrdenadas = new ArrayList<>();

    private static final LinkedHashMap<String, String> TABELAS = new LinkedHashMap<>();
    static {
        TABELAS.put("inventario_equipamentos", "Inventário de Equipamentos");
        TABELAS.put("catalogo_produtos", "Catálogo de Produtos");
        TABELAS.put("funcionarios", "Funcionários");
        TABELAS.put("estoque_movimentacoes", "Movimentações de Estoque");
        TABELAS.put("usuarios", "Usuários do Sistema");
        TABELAS.put("historico_eventos", "Histórico de Eventos");
    }

    // =========================================================================
    // COLUNAS: CADA CHAVE AQUI DEVE SER EXATAMENTE A MESMA USADA NO GERADOR
    // =========================================================================
    private static final Map<String, List<String>> COLUNAS = new LinkedHashMap<>();
    static {
        COLUNAS.put("inventario_equipamentos", List.of(
                "ID","SKU","Nº Série","MAC","Localização","Departamento","Status","Condição",
                "Funcionário ID","Data Aquisição","Data Instalação","Última Verificação","Observações",
                // JOINs (adicionados apenas se o JOIN estiver ativo)
                "Nome","Função","Marca","Modelo","Categoria","Preço Unitário"
        ));
        COLUNAS.put("catalogo_produtos", List.of(
                "SKU","Tipo","Categoria","Marca","Modelo","Cor","Preço Unitário","Estoque","Ativo","Descrição"
        ));
        COLUNAS.put("funcionarios", List.of(
                "Código","Nome","Função","Departamento","Local Trabalho","Email","Telefone","Ativo",
                "Nível Acesso","Último Login"
        ));
        COLUNAS.put("estoque_movimentacoes", List.of(
                "ID","SKU","Tipo","Quantidade","Lote","Data Movimentação","Localização","Funcionário","Motivo","Observações",
                "Nome","Marca","Modelo"
        ));
        COLUNAS.put("usuarios", List.of(
                "ID","Nome","Email","Funcionário ID","Nível Acesso","Último Login","IP Login","Ativo",
                "Departamento"
        ));
        COLUNAS.put("historico_eventos", List.of(
                "ID","Tipo Evento","SKU","Funcionário ID","Data/Hora","Descrição Funcionário","Dados Anteriores","Dados Novos"
        ));
    }

    // =========================================================================
    // INITIALIZE
    // =========================================================================
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        comboTabelaBase.getItems().addAll(TABELAS.values());
        comboTabelaBase.setValue(TABELAS.get("inventario_equipamentos"));
        comboTabelaBase.setOnAction(e -> onTabelaBaseChanged());
        checkJoinFuncionarios.setOnAction(e -> toggleJoin(checkJoinFuncionarios, scrollFuncionarios, containerColunasFuncionarios, "funcionarios", cbFuncionarios));
        checkJoinCatalogo.setOnAction(e -> toggleJoin(checkJoinCatalogo, scrollCatalogo, containerColunasCatalogo, "catalogo_produtos", cbCatalogo));
        checkJoinUsuarios.setOnAction(e -> toggleJoin(checkJoinUsuarios, scrollUsuarios, containerColunasUsuarios, "usuarios", cbUsuarios));
        checkJoinMovimentacoes.setOnAction(e -> toggleJoin(checkJoinMovimentacoes, scrollMovimentacoes, containerColunasMovimentacoes, "estoque_movimentacoes", cbMovimentacoes));
        comboFiltroStatus.setValue("TODOS"); comboFiltroCondicao.setValue("TODOS");
        dpFim.setValue(LocalDate.now());
        onTabelaBaseChanged();
        atualizarLabelJoins();
    }

    private void onTabelaBaseChanged() {
        String nome = comboTabelaBase.getValue();
        tabelaBase = TABELAS.entrySet().stream().filter(e -> e.getValue().equals(nome)).map(Map.Entry::getKey).findFirst().orElse("inventario_equipamentos");
        carregarCheckboxes(containerColunasBase, tabelaBase, cbBase, true);
        comboFiltroStatus.getItems().clear(); comboFiltroCondicao.getItems().clear();
        if (tabelaBase.equals("inventario_equipamentos")) {
            comboFiltroStatus.getItems().addAll("TODOS","ATIVO","MANUTENCAO","BAIXADO","RESERVA","EM_USO");
            comboFiltroCondicao.getItems().addAll("TODOS","OTIMO","BOM","REGULAR","CRITICO");
        } else { comboFiltroStatus.getItems().add("TODOS"); comboFiltroCondicao.getItems().add("TODOS"); }
        comboFiltroStatus.setValue("TODOS"); comboFiltroCondicao.setValue("TODOS");
        lblConfigAtiva.setText("Base: " + nome);
    }

    private void toggleJoin(CheckBox check, ScrollPane scroll, VBox container, String tabela, Map<String, CheckBox> map) {
        boolean v = check.isSelected(); scroll.setVisible(v); scroll.setManaged(v);
        if (v && map.isEmpty()) carregarCheckboxes(container, tabela, map, false);
        atualizarLabelJoins();
    }

    private void carregarCheckboxes(VBox container, String tabela, Map<String, CheckBox> map, boolean sel) {
        container.getChildren().clear(); map.clear();
        for (String c : COLUNAS.getOrDefault(tabela, List.of())) {
            CheckBox cb = new CheckBox(c); cb.setSelected(sel); cb.setStyle("-fx-font-size: 11px;");
            map.put(c, cb); container.getChildren().add(cb);
        }
    }

    @FXML private void selecionarTodasColunasBase() { cbBase.values().forEach(cb -> cb.setSelected(true)); }
    @FXML private void limparTodasColunasBase() { cbBase.values().forEach(cb -> cb.setSelected(false)); }
    private void atualizarLabelJoins() {
        List<String> j = new ArrayList<>();
        if (checkJoinFuncionarios.isSelected()) j.add("Func"); if (checkJoinCatalogo.isSelected()) j.add("Cat");
        if (checkJoinUsuarios.isSelected()) j.add("Usr"); if (checkJoinMovimentacoes.isSelected()) j.add("Mov");
        lblJoinsAtivos.setText(j.isEmpty() ? "" : "🔗 " + String.join(" + ", j));
    }

    // =========================================================================
    // GERAR
    // =========================================================================
    @FXML private void handleGerarRelatorio() {
        long inicio = System.currentTimeMillis();
        List<String> colsBase = cbBase.entrySet().stream().filter(e -> e.getValue().isSelected()).map(Map.Entry::getKey).collect(Collectors.toList());
        if (colsBase.isEmpty()) { NexusFX.alerts().warn("Sem Colunas", "Selecione colunas da tabela base.", "Relatórios"); return; }
        boolean jF = checkJoinFuncionarios.isSelected(), jC = checkJoinCatalogo.isSelected(), jU = checkJoinUsuarios.isSelected(), jM = checkJoinMovimentacoes.isSelected();

        new Thread(() -> {
            try {
                List<Funcionarios> funcs = jF ? funcionariosService.listarTodos(1, 10000) : List.of();
                List<CatalogoProdutos> cats = jC ? catalogoService.listarTodos(1, 10000) : List.of();
                List<Usuario> usrs = jU ? usuariosService.listarTodos(1, 10000) : List.of();
                List<EstoqueMovimentacoes> movs = jM ? movimentacoesService.listarTodas(1, 10000) : List.of();

                dadosCombinados.clear();
                switch (tabelaBase) {
                    case "inventario_equipamentos" -> gerarInventario(funcs, cats, jF, jC);
                    case "catalogo_produtos" -> gerarCatalogo();
                    case "funcionarios" -> gerarFuncionarios(usrs, jU);
                    case "estoque_movimentacoes" -> gerarMovimentacoes(funcs, cats, jF, jC);
                    case "usuarios" -> gerarUsuarios(funcs, jF);
                    case "historico_eventos" -> gerarHistorico();
                }

                todasColunasOrdenadas = new ArrayList<>(colsBase);
                if (jF) todasColunasOrdenadas.addAll(cbFuncionarios.entrySet().stream().filter(e -> e.getValue().isSelected()).map(Map.Entry::getKey).toList());
                if (jC) todasColunasOrdenadas.addAll(cbCatalogo.entrySet().stream().filter(e -> e.getValue().isSelected()).map(Map.Entry::getKey).toList());
                if (jU) todasColunasOrdenadas.addAll(cbUsuarios.entrySet().stream().filter(e -> e.getValue().isSelected()).map(Map.Entry::getKey).toList());
                if (jM) todasColunasOrdenadas.addAll(cbMovimentacoes.entrySet().stream().filter(e -> e.getValue().isSelected()).map(Map.Entry::getKey).toList());

                // Normalizar
                for (Map<String, String> linha : dadosCombinados)
                    for (String col : todasColunasOrdenadas) linha.putIfAbsent(col, "-");

                dadosFiltrados = new ArrayList<>(dadosCombinados);
                long tempo = System.currentTimeMillis() - inicio;

                Platform.runLater(() -> {
                    popularCombosFiltro();
                    popularTabela(dadosFiltrados);
                    lblTotalRegistros.setText(String.valueOf(dadosFiltrados.size()));
                    lblTempoExecucao.setText(String.format("(%.1fs)", tempo / 1000.0));
                    lblStatus.setText("Relatório gerado.");
                });
            } catch (Exception e) { logger.error("Erro", e); Platform.runLater(() -> lblStatus.setText("Erro.")); }
        }).start();
    }

    private void popularCombosFiltro() {
        Set<String> locs = dadosCombinados.stream().map(l -> l.get("Localização")).filter(Objects::nonNull).filter(s -> !s.isEmpty() && !"-".equals(s)).collect(Collectors.toCollection(LinkedHashSet::new));
        comboFiltroLocalizacao.getItems().clear(); comboFiltroLocalizacao.getItems().add("TODOS"); comboFiltroLocalizacao.getItems().addAll(locs); comboFiltroLocalizacao.setValue("TODOS");
        Set<String> deptos = dadosCombinados.stream().map(l -> l.get("Departamento")).filter(Objects::nonNull).filter(s -> !s.isEmpty() && !"-".equals(s)).collect(Collectors.toCollection(LinkedHashSet::new));
        comboFiltroDepartamento.getItems().clear(); comboFiltroDepartamento.getItems().add("TODOS"); comboFiltroDepartamento.getItems().addAll(deptos); comboFiltroDepartamento.setValue("TODOS");
    }

    @FXML private void handleAplicarFiltroPosGeracao() {
        String st = comboFiltroStatus.getValue(), co = comboFiltroCondicao.getValue(), lo = comboFiltroLocalizacao.getValue(), de = comboFiltroDepartamento.getValue();
        dadosFiltrados = dadosCombinados.stream()
                .filter(l -> "TODOS".equals(st) || st == null || st.equals(l.get("Status")))
                .filter(l -> "TODOS".equals(co) || co == null || co.equals(l.get("Condição")))
                .filter(l -> "TODOS".equals(lo) || lo == null || lo.equals(l.get("Localização")))
                .filter(l -> "TODOS".equals(de) || de == null || de.equals(l.get("Departamento"))).collect(Collectors.toList());
        popularTabela(dadosFiltrados); lblTotalRegistros.setText(String.valueOf(dadosFiltrados.size()));
    }

    @FXML private void handleLimparFiltro() {
        comboFiltroStatus.setValue("TODOS"); comboFiltroCondicao.setValue("TODOS");
        comboFiltroLocalizacao.setValue("TODOS"); comboFiltroDepartamento.setValue("TODOS");
        dadosFiltrados = new ArrayList<>(dadosCombinados); popularTabela(dadosFiltrados); lblTotalRegistros.setText(String.valueOf(dadosFiltrados.size()));
    }

    // =========================================================================
    // GERADORES — CADA put() DEVE TER CHAVE EXISTENTE EM COLUNAS
    // =========================================================================
    private void gerarInventario(List<Funcionarios> funcs, List<CatalogoProdutos> cats, boolean jF, boolean jC) {
        for (InventarioEquipamentos e : inventarioService.listarTodos(1, 10000)) {
            Map<String, String> l = new LinkedHashMap<>();
            l.put("ID",s(e.getId())); l.put("SKU",nvl(e.getSkuProduto())); l.put("Nº Série",nvl(e.getNumSerie())); l.put("MAC",nvl(e.getEnderecoMac()));
            l.put("Localização",nvl(e.getLocalizacao())); l.put("Departamento",nvl(e.getDepartamento())); l.put("Status",nvl(e.getStatus())); l.put("Condição",nvl(e.getCondicao()));
            l.put("Funcionário ID",nvl(e.getFuncionarioId())); l.put("Data Aquisição",e.getDataAquisicao()!=null?e.getDataAquisicao().format(DATE_FMT):"-");
            l.put("Data Instalação",e.getDataInstalacao()!=null?e.getDataInstalacao().format(DATE_FMT):"-"); l.put("Última Verificação",e.getDataUltimaVerificacao()!=null?e.getDataUltimaVerificacao().format(DATE_FMT):"-");
            l.put("Observações",nvl(e.getObservacoes()));
            if (jF) funcs.stream().filter(f->f.getCodDep().equals(e.getFuncionarioId())).findFirst().ifPresent(f->{
                l.put("Nome",nvl(f.getNome())); l.put("Função",nvl(f.getFuncao())); l.put("Departamento",f.getDepartamento()!=null?f.getDepartamento():"-");
            });
            if (jC) cats.stream().filter(c->c.getSku().equals(e.getSkuProduto())).findFirst().ifPresent(c->{
                l.put("Marca",nvl(c.getMarca())); l.put("Modelo",nvl(c.getModelo())); l.put("Categoria",nvl(c.getCategoria()));
                l.put("Preço Unitário",c.getPrecoUnitario()!=null?"€ "+c.getPrecoUnitario():"-");
            });
            dadosCombinados.add(l);
        }
    }
    private void gerarCatalogo() { for(CatalogoProdutos c:catalogoService.listarTodos(1,10000)){Map<String,String>l=new LinkedHashMap<>();l.put("SKU",nvl(c.getSku()));l.put("Tipo",c.getTipoProduto()!=null?c.getTipoProduto().toString():"-");l.put("Categoria",nvl(c.getCategoria()));l.put("Marca",nvl(c.getMarca()));l.put("Modelo",nvl(c.getModelo()));l.put("Cor",nvl(c.getCor()));l.put("Preço Unitário",c.getPrecoUnitario()!=null?"€ "+c.getPrecoUnitario():"€ 0");l.put("Estoque",s(c.getTotalRecebido()));l.put("Ativo",Boolean.TRUE.equals(c.getAtivo())?"Sim":"Não");l.put("Descrição",nvl(c.getDescricao()));dadosCombinados.add(l);} }
    private void gerarFuncionarios(List<Usuario> usrs, boolean jU) { for(Funcionarios f:funcionariosService.listarTodos(1,10000)){Map<String,String>l=new LinkedHashMap<>();l.put("Código",nvl(f.getCodDep()));l.put("Nome",nvl(f.getNome()));l.put("Função",nvl(f.getFuncao()));l.put("Departamento",nvl(f.getDepartamento()));l.put("Local Trabalho",nvl(f.getLocalTrabalho()));l.put("Email",nvl(f.getEmail()));l.put("Telefone",nvl(f.getTelefone()));l.put("Ativo",Boolean.TRUE.equals(f.getAtivo())?"Sim":"Não");if(jU)usrs.stream().filter(u->u.getFuncionarioId()!=null&&u.getFuncionarioId().equals(f.getCodDep())).findFirst().ifPresent(u->{l.put("Nível Acesso",nvl(u.getNivelAcesso()));l.put("Último Login",u.getUltimoLogin()!=null?u.getUltimoLogin().format(DATE_TIME_FMT):"-");});dadosCombinados.add(l);} }
    private void gerarMovimentacoes(List<Funcionarios> funcs, List<CatalogoProdutos> cats, boolean jF, boolean jC) { for(EstoqueMovimentacoes m:movimentacoesService.listarTodas(1,10000)){Map<String,String>l=new LinkedHashMap<>();l.put("ID",s(m.getId()));l.put("SKU",nvl(m.getSkuProduto()));l.put("Tipo",m.getTipoMovimentacao()!=null?m.getTipoMovimentacao().toString():"-");l.put("Quantidade",s(m.getQuantidade()));l.put("Lote",nvl(m.getLote()));l.put("Data Movimentação",m.getDataMovimentacao()!=null?m.getDataMovimentacao().format(DATE_FMT):"-");l.put("Localização",nvl(m.getLocalizacao()));l.put("Funcionário",nvl(m.getCodDepFuncionario()));l.put("Motivo",nvl(m.getMotivo()));l.put("Observações",nvl(m.getObservacoes()));if(jF)funcs.stream().filter(f->f.getCodDep().equals(m.getCodDepFuncionario())).findFirst().ifPresent(f->l.put("Nome",nvl(f.getNome())));if(jC)cats.stream().filter(c->c.getSku().equals(m.getSkuProduto())).findFirst().ifPresent(c->{l.put("Marca",nvl(c.getMarca()));l.put("Modelo",nvl(c.getModelo()));});dadosCombinados.add(l);} }
    private void gerarUsuarios(List<Funcionarios> funcs, boolean jF) { for(Usuario u:usuariosService.listarTodos(1,10000)){Map<String,String>l=new LinkedHashMap<>();l.put("ID",s(u.getId()));l.put("Nome",nvl(u.getNome()));l.put("Email",nvl(u.getEmail()));l.put("Funcionário ID",nvl(u.getFuncionarioId()));l.put("Nível Acesso",nvl(u.getNivelAcesso()));l.put("Último Login",u.getUltimoLogin()!=null?u.getUltimoLogin().format(DATE_TIME_FMT):"-");l.put("IP Login",nvl(u.getIpUltimoLogin()));l.put("Ativo",Boolean.TRUE.equals(u.getAtivo())?"Sim":"Não");if(jF)funcs.stream().filter(f->f.getCodDep().equals(u.getFuncionarioId())).findFirst().ifPresent(f->l.put("Departamento",nvl(f.getDepartamento())));dadosCombinados.add(l);} }
    private void gerarHistorico() { for(HistoricoEventos h:historicoService.listarTodos(1,10000)){Map<String,String>l=new LinkedHashMap<>();l.put("ID",s(h.getId()));l.put("Tipo Evento",nvl(h.getTipoEvento()));l.put("SKU",nvl(h.getSkuProduto()));l.put("Funcionário ID",nvl(h.getFuncionarioId()));l.put("Data/Hora",h.getCreatedAt()!=null?h.getCreatedAt().format(DATE_TIME_FMT):"-");l.put("Descrição Funcionário",nvl(h.getDescricaoFuncionario()));l.put("Dados Anteriores",nvl(h.getDadosAnteriores()));l.put("Dados Novos",nvl(h.getDadosNovos()));dadosCombinados.add(l);} }

    // =========================================================================
    // TABELA + EXPORTAÇÃO
    // =========================================================================
    private void popularTabela(List<Map<String, String>> dados) {
        tabelaResultados.getColumns().clear(); tabelaResultados.getItems().clear();
        if (dados.isEmpty() || todasColunasOrdenadas.isEmpty()) return;
        for (int i = 0; i < todasColunasOrdenadas.size(); i++) {
            final int idx = i;
            TableColumn<ObservableList<String>, String> col = new TableColumn<>(todasColunasOrdenadas.get(i));
            col.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().size() > idx ? cell.getValue().get(idx) : "-"));
            col.setPrefWidth(Math.max(60, Math.min(180, todasColunasOrdenadas.get(i).length() * 9 + 20)));
            tabelaResultados.getColumns().add(col);
        }
        ObservableList<ObservableList<String>> rows = FXCollections.observableArrayList();
        for (Map<String, String> linha : dados) {
            ObservableList<String> row = FXCollections.observableArrayList();
            for (String col : todasColunasOrdenadas) row.add(linha.getOrDefault(col, "-"));
            rows.add(row);
        }
        tabelaResultados.setItems(rows);
    }

    @FXML private void handleExportarExcel() {
        if (dadosFiltrados.isEmpty()) { NexusFX.alerts().warn("Sem Dados", "Gere o relatório primeiro.", "Relatórios"); return; }
        FileChooser fc = new FileChooser(); fc.setTitle("Salvar"); fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel", "*.xlsx"));
        fc.setInitialFileName("relatorio_avancado_" + LocalDate.now() + ".xlsx");
        File file = fc.showSaveDialog((Stage) tabelaResultados.getScene().getWindow());
        if (file != null) new Thread(() -> {
            try { ExportarExcelService.exportarDadosCombinados(file.getAbsolutePath(), todasColunasOrdenadas, dadosFiltrados);
                Platform.runLater(() -> NexusFX.alerts().info("OK", "Exportado: " + file.getName(), "Relatórios"));
            } catch (Exception e) { Platform.runLater(() -> NexusFX.alerts().erro("Erro", e.getMessage(), "Relatórios")); }
        }).start();
    }

    @FXML private void handleSalvarModelo() {
        StringBuilder sb = new StringBuilder(LocalDateTime.now().format(DATE_TIME_FMT));
        sb.append(" | ").append(comboTabelaBase.getValue()).append(" | Cols:").append(cbBase.values().stream().filter(CheckBox::isSelected).count());
        sb.append(" | J:").append(checkJoinFuncionarios.isSelected()?"F":"").append(checkJoinCatalogo.isSelected()?"C":"").append(checkJoinUsuarios.isSelected()?"U":"").append(checkJoinMovimentacoes.isSelected()?"M":"");
        try { List<String> m = Files.exists(Paths.get(MODELOS_FILE))?new ArrayList<>(Files.readAllLines(Paths.get(MODELOS_FILE))):new ArrayList<>(); m.add(0,sb.toString()); Files.write(Paths.get(MODELOS_FILE),m); NexusFX.alerts().info("Salvo","Configuração salva.","Relatórios"); }
        catch (IOException e) { NexusFX.alerts().erro("Erro","Falha ao salvar.","Relatórios"); }
    }

    private String nvl(String s) { return s != null && !s.isEmpty() ? s : "-"; }
    private String s(Object o) { return o != null ? String.valueOf(o) : "-"; }
}