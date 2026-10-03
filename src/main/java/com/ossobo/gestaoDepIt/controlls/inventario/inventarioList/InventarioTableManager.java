package com.ossobo.gestaoDepIt.controlls.inventario.inventarioList;

import com.ossobo.gestaoDepIt.controlls.inventario.cellfactories.InventarioAcoesCallback;
import com.ossobo.gestaoDepIt.controlls.inventario.cellfactories.InventarioAcoesCellFactory;
import com.ossobo.gestaoDepIt.controlls.inventario.cellfactories.InventarioStatusCellFactory;
import com.ossobo.gestaoDepIt.db.models.InventarioEquipamentos;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * InventarioTableManager v1.0
 *
 * Responsabilidade única: Configurar a TableView do inventário —
 * mapeamento coluna↔campo, cell factories, double-click, refresh, limpeza.
 *
 * A coluna de ações é ligada via {@link InventarioAcoesCallback} (não Actions) —
 * a cell factory emite intenção; quem abre as janelas é o controller.
 *
 * @since v1.0
 */
public class InventarioTableManager {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final TableView<InventarioEquipamentos> tabela;
    private final InventarioState state;
    private final InventarioAcoesCallback callback;

    private TableColumn<InventarioEquipamentos, String> colId;
    private TableColumn<InventarioEquipamentos, String> colNumSerie;
    private TableColumn<InventarioEquipamentos, String> colEnderecoMac;
    private TableColumn<InventarioEquipamentos, String> colSkuProduto;
    private TableColumn<InventarioEquipamentos, String> colFuncionarioId;
    private TableColumn<InventarioEquipamentos, String> colLocalizacao;
    private TableColumn<InventarioEquipamentos, String> colDepartamento;
    private TableColumn<InventarioEquipamentos, String> colStatus;
    private TableColumn<InventarioEquipamentos, String> colCondicao;
    private TableColumn<InventarioEquipamentos, String> colDataAquisicao;
    private TableColumn<InventarioEquipamentos, String> colDataInstalacao;
    private TableColumn<InventarioEquipamentos, String> colDataUltimaVerificacao;
    private TableColumn<InventarioEquipamentos, Void>   colAcoes;

    public InventarioTableManager(TableView<InventarioEquipamentos> tabela,
                                  InventarioState state,
                                  InventarioAcoesCallback callback) {
        this.tabela = tabela;
        this.state = state;
        this.callback = callback;
    }

    // ===== CONFIGURAÇÃO DE COLUNAS (ordem espelha o FXML) =====
    public void configurarColunas(
            TableColumn<InventarioEquipamentos, String> id,
            TableColumn<InventarioEquipamentos, String> numSerie,
            TableColumn<InventarioEquipamentos, String> enderecoMac,
            TableColumn<InventarioEquipamentos, String> skuProduto,
            TableColumn<InventarioEquipamentos, String> funcionarioId,
            TableColumn<InventarioEquipamentos, String> localizacao,
            TableColumn<InventarioEquipamentos, String> departamento,
            TableColumn<InventarioEquipamentos, String> status,
            TableColumn<InventarioEquipamentos, String> condicao,
            TableColumn<InventarioEquipamentos, String> dataAquisicao,
            TableColumn<InventarioEquipamentos, String> dataInstalacao,
            TableColumn<InventarioEquipamentos, String> dataUltimaVerificacao,
            TableColumn<InventarioEquipamentos, Void>   acoes) {
        this.colId = id;
        this.colNumSerie = numSerie;
        this.colEnderecoMac = enderecoMac;
        this.colSkuProduto = skuProduto;
        this.colFuncionarioId = funcionarioId;
        this.colLocalizacao = localizacao;
        this.colDepartamento = departamento;
        this.colStatus = status;
        this.colCondicao = condicao;
        this.colDataAquisicao = dataAquisicao;
        this.colDataInstalacao = dataInstalacao;
        this.colDataUltimaVerificacao = dataUltimaVerificacao;
        this.colAcoes = acoes;
    }

    // ===== CONFIGURAÇÃO COMPLETA =====
    public void configurarTabelaCompleta() {
        tabela.setItems(state.getEquipamentosData());
        configurarTodasColunas();
        configurarCellFactories();
        configurarDoubleClick();
    }

    private void configurarTodasColunas() {
        colId.setCellValueFactory(c -> new SimpleStringProperty(nvl(c.getValue().id())));
        colNumSerie.setCellValueFactory(c -> new SimpleStringProperty(nvl(c.getValue().numSerie())));
        colEnderecoMac.setCellValueFactory(c -> new SimpleStringProperty(formatMac(c.getValue().enderecoMac())));
        colSkuProduto.setCellValueFactory(c -> new SimpleStringProperty(nvl(c.getValue().skuProduto())));
        colFuncionarioId.setCellValueFactory(c -> new SimpleStringProperty(nvl(c.getValue().funcionarioId())));
        colLocalizacao.setCellValueFactory(c -> new SimpleStringProperty(nvl(c.getValue().localizacao())));
        colDepartamento.setCellValueFactory(c -> new SimpleStringProperty(nvl(c.getValue().departamento())));
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(nvl(c.getValue().status())));
        colCondicao.setCellValueFactory(c -> new SimpleStringProperty(nvl(c.getValue().condicao())));
        colDataAquisicao.setCellValueFactory(c -> new SimpleStringProperty(fmtDate(c.getValue().dataAquisicao())));
        colDataInstalacao.setCellValueFactory(c -> new SimpleStringProperty(fmtDate(c.getValue().dataInstalacao())));
        colDataUltimaVerificacao.setCellValueFactory(c -> new SimpleStringProperty(fmtDate(c.getValue().dataUltimaVerificacao())));
    }

    private void configurarCellFactories() {
        colStatus.setCellFactory(new InventarioStatusCellFactory());
        colAcoes.setCellFactory(new InventarioAcoesCellFactory(callback));
    }

    private void configurarDoubleClick() {
        tabela.setRowFactory(tv -> {
            TableRow<InventarioEquipamentos> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty() && callback != null) {
                    callback.abrirDetalhes(row.getItem());
                }
            });
            return row;
        });
    }

    // ===== REFRESH / CLEAR =====
    public void refresh() {
        if (tabela != null) tabela.refresh();
    }

    public void clear() {
        if (tabela != null && tabela.getItems() != null) {
            tabela.getItems().clear();
        }
    }

    public void cleanup() {
        if (tabela != null) {
            tabela.getItems().clear();
            tabela.setItems(null);
        }
    }

    // ===== UTILITÁRIOS =====
    private String nvl(String s) { return s != null && !s.isEmpty() ? s : "-"; }

    private String fmtDate(LocalDate d) { return d != null ? d.format(DATE_FMT) : "-"; }

    private String formatMac(String mac) {
        if (mac == null || mac.isBlank()) return "-";
        String limpo = mac.replaceAll("[:\\-]", "").toUpperCase();
        if (limpo.length() == 12) {
            return limpo.replaceAll("(.{2})", "$1:").substring(0, 17);
        }
        return mac;
    }
}