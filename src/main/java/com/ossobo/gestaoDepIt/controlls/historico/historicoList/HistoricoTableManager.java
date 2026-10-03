package com.ossobo.gestaoDepIt.controlls.historico.historicoList;

import com.ossobo.gestaoDepIt.controlls.historico.historicoList.cellfactories.HistoricoAcoesCallback;
import com.ossobo.gestaoDepIt.controlls.historico.historicoList.cellfactories.HistoricoAcoesCellFactory;
import com.ossobo.gestaoDepIt.controlls.historico.historicoList.cellfactories.HistoricoTipoCellFactory;
import com.ossobo.gestaoDepIt.db.models.HistoricoEventos;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;

import java.time.format.DateTimeFormatter;

/**
 * HistoricoTableManager v1.0
 *
 * Configuração da TableView do histórico — mapeamento coluna↔campo,
 * cell factories, double-click.
 *
 * A coluna de ações emite intenção via HistoricoAcoesCallback.
 *
 * @since v1.0
 */
public class HistoricoTableManager {

    private static final DateTimeFormatter DATE_TIME_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final TableView<HistoricoEventos> tabela;
    private final HistoricoState state;
    private final HistoricoAcoesCallback callback;

    private TableColumn<HistoricoEventos, String> colDataHora;
    private TableColumn<HistoricoEventos, String> colTipoEvento;
    private TableColumn<HistoricoEventos, String> colSkuProduto;
    private TableColumn<HistoricoEventos, String> colFuncionario;
    private TableColumn<HistoricoEventos, String> colDescricao;
    private TableColumn<HistoricoEventos, Void>   colAcoes;

    public HistoricoTableManager(TableView<HistoricoEventos> tabela,
                                 HistoricoState state,
                                 HistoricoAcoesCallback callback) {
        this.tabela = tabela;
        this.state = state;
        this.callback = callback;
    }

    public void configurarColunas(
            TableColumn<HistoricoEventos, String> dataHora,
            TableColumn<HistoricoEventos, String> tipoEvento,
            TableColumn<HistoricoEventos, String> skuProduto,
            TableColumn<HistoricoEventos, String> funcionario,
            TableColumn<HistoricoEventos, String> descricao,
            TableColumn<HistoricoEventos, Void>   acoes) {
        this.colDataHora = dataHora;
        this.colTipoEvento = tipoEvento;
        this.colSkuProduto = skuProduto;
        this.colFuncionario = funcionario;
        this.colDescricao = descricao;
        this.colAcoes = acoes;
    }

    public void configurarTabelaCompleta() {
        tabela.setItems(state.getEventosData());
        configurarTodasColunas();
        configurarCellFactories();
        configurarDoubleClick();
    }

    private void configurarTodasColunas() {
        colDataHora.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().createdAt() != null
                        ? c.getValue().createdAt().format(DATE_TIME_FMT)
                        : "-"));
        colTipoEvento.setCellValueFactory(c -> new SimpleStringProperty(
                nvl(c.getValue().tipoEvento())));
        colSkuProduto.setCellValueFactory(c -> new SimpleStringProperty(
                nvl(c.getValue().skuProduto())));
        colFuncionario.setCellValueFactory(c -> new SimpleStringProperty(
                nvl(c.getValue().funcionarioId())));
        colDescricao.setCellValueFactory(c -> new SimpleStringProperty(
                truncar(nvl(c.getValue().getDescricaoEvento()), 120)));
    }

    private void configurarCellFactories() {
        colTipoEvento.setCellFactory(new HistoricoTipoCellFactory());
        colAcoes.setCellFactory(new HistoricoAcoesCellFactory(callback));
    }

    private void configurarDoubleClick() {
        tabela.setRowFactory(tv -> {
            TableRow<HistoricoEventos> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty() && callback != null) {
                    callback.abrirDetalhes(row.getItem());
                }
            });
            return row;
        });
    }

    public void refresh() { if (tabela != null) tabela.refresh(); }

    public void clear() {
        if (tabela != null && tabela.getItems() != null) tabela.getItems().clear();
    }

    public void cleanup() {
        if (tabela != null) {
            tabela.getItems().clear();
            tabela.setItems(null);
        }
    }

    private String nvl(String s) { return s != null && !s.isEmpty() ? s : "-"; }

    private String truncar(String s, int max) {
        return (s != null && s.length() > max) ? s.substring(0, max) + "..." : s;
    }
}