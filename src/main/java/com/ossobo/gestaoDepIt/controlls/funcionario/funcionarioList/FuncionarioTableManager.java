package com.ossobo.gestaoDepIt.controlls.funcionario.funcionarioList;

import com.ossobo.gestaoDepIt.controlls.funcionario.funcionarioList.cellfactories.FuncionarioAcoesCallback;
import com.ossobo.gestaoDepIt.controlls.funcionario.funcionarioList.cellfactories.FuncionarioAcoesCellFactory;
import com.ossobo.gestaoDepIt.controlls.funcionario.funcionarioList.cellfactories.FuncionarioFotoCellFactory;
import com.ossobo.gestaoDepIt.controlls.funcionario.funcionarioList.cellfactories.FuncionarioStatusCellFactory;
import com.ossobo.gestaoDepIt.db.models.Funcionarios;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * FuncionarioTableManager v1.0
 *
 * Responsabilidade: configurar e gerenciar a TableView de funcionários —
 *                   mapeamento coluna↔campo, cell factories, refresh.
 *
 * Colunas esperadas:
 *   colFoto, colCodDep, colNome, colFuncao, colDepartamento,
 *   colStatus, colEmail, colAcoes.
 *
 * A coluna Foto é renderizada pela FotoCellFactory, que usa o callback
 * para solicitar bytes ao FuncionarioActions.
 *
 * @since v1.0
 */
public class FuncionarioTableManager {

    private final TableView<Funcionarios> tabela;
    private final FuncionarioState state;
    private final FuncionarioAcoesCallback acoesCallback;

    private TableColumn<Funcionarios, String> colCodDep;
    private TableColumn<Funcionarios, String> colNome;
    private TableColumn<Funcionarios, String> colFuncao;
    private TableColumn<Funcionarios, String> colDepartamento;
    private TableColumn<Funcionarios, String> colStatus;
    private TableColumn<Funcionarios, String> colEmail;
    private TableColumn<Funcionarios, Void>   colFoto;
    private TableColumn<Funcionarios, Void>   colAcoes;

    public FuncionarioTableManager(
            TableView<Funcionarios> tabela,
            FuncionarioState state,
            FuncionarioAcoesCallback acoesCallback) {
        this.tabela = tabela;
        this.state = state;
        this.acoesCallback = acoesCallback;
    }

    public void configurarColunas(
            TableColumn<Funcionarios, Void>   foto,
            TableColumn<Funcionarios, String> codDep,
            TableColumn<Funcionarios, String> nome,
            TableColumn<Funcionarios, String> funcao,
            TableColumn<Funcionarios, String> departamento,
            TableColumn<Funcionarios, String> status,
            TableColumn<Funcionarios, String> email,
            TableColumn<Funcionarios, Void>   acoes) {
        this.colFoto = foto;
        this.colCodDep = codDep;
        this.colNome = nome;
        this.colFuncao = funcao;
        this.colDepartamento = departamento;
        this.colStatus = status;
        this.colEmail = email;
        this.colAcoes = acoes;
    }

    public void configurarTabelaCompleta() {
        tabela.setItems(state.getFuncionariosData());
        configurarTodasColunas();
        configurarCellFactories();
    }

    private void configurarTodasColunas() {
        colCodDep.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue().codDep()));

        colNome.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue().nome()));

        colFuncao.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue().funcao()));

        colDepartamento.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue().departamento()));

        colStatus.setCellValueFactory(cd -> {
            Boolean ativo = cd.getValue().ativo();
            return new SimpleStringProperty(Boolean.TRUE.equals(ativo) ? "Ativo" : "Inativo");
        });

        colEmail.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue().email()));
    }

    private void configurarCellFactories() {
        colStatus.setCellFactory(new FuncionarioStatusCellFactory());
        colFoto.setCellFactory(new FuncionarioFotoCellFactory(acoesCallback));
        colAcoes.setCellFactory(new FuncionarioAcoesCellFactory(acoesCallback));
    }

    public void refresh() {
        if (tabela != null) tabela.refresh();
    }

    public void clear() {
        if (tabela != null && tabela.getItems() != null) {
            tabela.getItems().clear();
        }
    }

    public javafx.stage.Window getWindow() {
        return tabela != null && tabela.getScene() != null
                ? tabela.getScene().getWindow() : null;
    }

    public void cleanup() {
        if (tabela != null) {
            tabela.getItems().clear();
            tabela.setItems(null);
        }
    }
}