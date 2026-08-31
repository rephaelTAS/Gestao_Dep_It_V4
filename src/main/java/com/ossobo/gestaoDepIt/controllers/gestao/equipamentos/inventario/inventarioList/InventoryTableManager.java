package com.ossobo.gestaoDepIt.controllers.gestao.equipamentos.inventario.inventarioList;

import com.ossobo.gestaoDepIt.db.models.InventarioEquipamentos;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.*;
import java.time.format.DateTimeFormatter;

/**
 * Gerencia a configuração da tabela de inventário.
 * Responsabilidade única: Configurar colunas, cell factories, double-click.
 */
public class InventoryTableManager {

    private final TableView<InventarioEquipamentos> tabela;
    private final InventoryState state;
    private final InventoryActions actions;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

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
    private TableColumn<InventarioEquipamentos, Void> colAcoes;

    public InventoryTableManager(TableView<InventarioEquipamentos> tabela,
                                 InventoryState state, InventoryActions actions) {
        this.tabela = tabela;
        this.state = state;
        this.actions = actions;
    }

    public void configurarColunas(
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
            TableColumn<InventarioEquipamentos, Void> acoes) {
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

    public void configurarTabelaCompleta() {
        tabela.setItems(state.getEquipamentosData());
        configurarTodasColunas();
        configurarDoubleClick();
    }

    private void configurarTodasColunas() {
        colNumSerie.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().numSerie()));
        colEnderecoMac.setCellValueFactory(cell -> new SimpleStringProperty(
                formatarMac(cell.getValue().enderecoMac())));
        colSkuProduto.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getSkuProduto()));
        colFuncionarioId.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getFuncionarioId()));
        colLocalizacao.setCellValueFactory(cell -> new SimpleStringProperty(
                nvl(cell.getValue().getLocalizacao())));
        colDepartamento.setCellValueFactory(cell -> new SimpleStringProperty(
                nvl(cell.getValue().getDepartamento())));
        colStatus.setCellValueFactory(cell -> new SimpleStringProperty(
                nvl(cell.getValue().getStatus())));
        colCondicao.setCellValueFactory(cell -> new SimpleStringProperty(
                nvl(cell.getValue().getCondicao())));
        colDataAquisicao.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getDataAquisicao() != null
                        ? cell.getValue().getDataAquisicao().format(DATE_FMT) : "-"));
        colDataInstalacao.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getDataInstalacao() != null
                        ? cell.getValue().getDataInstalacao().format(DATE_FMT) : "-"));
        colDataUltimaVerificacao.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getDataUltimaVerificacao() != null
                        ? cell.getValue().getDataUltimaVerificacao().format(DATE_FMT) : "-"));
    }

    private void configurarDoubleClick() {
        equipamentosTable.setRowFactory(tv -> {
            TableRow<InventarioEquipamentos> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    abrirDetalhes(row.getItem());
                }
            });
            return row;
        });
    }

    private void configurarDoubleClick() {
        tabela.setRowFactory(tv -> {
            TableRow<InventarioEquipamentos> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    actions.abrirDetalhes(row.getItem(), tabela.getScene().getWindow());
                }
            });
            return row;
        });
    }

    private String formatarMac(String mac) {
        if (mac == null || mac.trim().isEmpty()) return "-";
        String limpo = mac.replaceAll("[:\\-]", "").toUpperCase();
        if (limpo.length() == 12) {
            return limpo.replaceAll("(.{2})", "$1:").substring(0, 17);
        }
        return mac;
    }

    private String nvl(String s) { return s != null ? s : "-"; }

    public void refresh() { if (tabela != null) tabela.refresh(); }

    public void cleanup() {
        if (tabela != null) {
            tabela.getItems().clear();
            tabela.setItems(null);
        }
    }
}