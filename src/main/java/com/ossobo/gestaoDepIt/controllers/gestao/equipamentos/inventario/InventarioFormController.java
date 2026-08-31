package com.ossobo.gestaoDepIt.controllers.gestao.equipamentos.inventario;

import com.ossobo.gestaoDepIt.controllers.gestao.equipamentos.inventario.historico.InventarioHistoricoRegistrar;
import com.ossobo.winterfx.anotations.Controller;
import com.ossobo.winterfx.anotations.Inject;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import com.ossobo.gestaoDepIt.db.models.*;
import com.ossobo.gestaoDepIt.db.services.*;

import java.net.URL;
import java.time.LocalDate;
import java.util.*;

/**
 * Controlador do formulário de adição/edição de equipamento no inventário.
 *
 * v3.1 — PROTOTYPE + InventarioHistoricoRegistrar:
 *        - initialize() roda sempre (nova instância a cada abertura)
 *        - Histórico delegado ao InventarioHistoricoRegistrar
 *        - Código limpo: Controller não conhece as regras de histórico
 */
@Controller(proxy = false)
public class InventarioFormController implements Initializable {

    @Inject
    private InventarioEquipamentosService inventarioService;
    @Inject private CatalogoProdutosService catalogoService;
    @Inject private FuncionariosService funcionariosService;
    @Inject private HistoricoEventosService historicoService;

    @FXML private Label labelTitulo;
    @FXML private ComboBox<CatalogoProdutos> comboCatalogo;
    @FXML private ComboBox<Funcionarios> comboFuncionario;
    @FXML private ComboBox<String> comboStatus, comboCondicao;
    @FXML private TextField txtNumSerie, txtEnderecoMac, txtLocalizacao, txtDepartamento;
    @FXML private TextArea txtObservacoes;
    @FXML private DatePicker dateAquisicao, dateInstalacao, dateUltimaVerificacao;
    @FXML private TextField txtSkuProduto, txtFuncionarioId, txtCreatedAt, txtUpdatedAt, txtEquipamentoId;
    @FXML private Button cancelarButton, salvarButton;
    @FXML private Label labelContadorCaracteres;

    private Long equipamentoId;
    private InventarioEquipamentos equipamentoExistente;
    private InventarioHistoricoRegistrar historicoRegistrar;

    // =========================================================================
    // INITIALIZE
    // =========================================================================

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void setEquipamentoId(Long id) {
        this.equipamentoId = id;
    }

    // =========================================================================
    // MODO ADIÇÃO
    // =========================================================================

    private void iniciarModoAdicao() {
        this.equipamentoId = null;
        this.equipamentoExistente = null;
        limparCampos();

        labelTitulo.setText("Novo Equipamento no Inventário");
        comboStatus.setValue("ATIVO");
        comboCondicao.setValue("BOM");
        dateInstalacao.setValue(LocalDate.now());
        dateInstalacao.setDisable(false);
        dateUltimaVerificacao.setValue(LocalDate.now());
    }

    // =========================================================================
    // MODO EDIÇÃO
    // =========================================================================

    private void iniciarModoEdicao() {

    }

    // =========================================================================
    // LIMPAR E PREENCHER
    // =========================================================================

    private void limparCampos() {
        if (txtNumSerie == null) return;
        txtNumSerie.clear();
        txtEnderecoMac.clear();
        txtLocalizacao.clear();
        txtDepartamento.clear();
        txtObservacoes.clear();
        dateAquisicao.setValue(null);
        dateInstalacao.setValue(null);
        dateUltimaVerificacao.setValue(null);
        comboCatalogo.setValue(null);
        comboFuncionario.setValue(null);
        if (txtSkuProduto != null) txtSkuProduto.clear();
        if (txtFuncionarioId != null) txtFuncionarioId.clear();
        if (txtEquipamentoId != null) txtEquipamentoId.clear();
        if (txtCreatedAt != null) txtCreatedAt.clear();
        if (txtUpdatedAt != null) txtUpdatedAt.clear();
    }

    private void preencherFormularioEdicao(InventarioEquipamentos e) {
        for (CatalogoProdutos p : comboCatalogo.getItems()) {
            if (p.sku().equals(e.skuProduto())) { comboCatalogo.setValue(p); break; }
        }
        for (Funcionarios f : comboFuncionario.getItems()) {
            if (f.codDep().equals(e.funcionarioId())) { comboFuncionario.setValue(f); break; }
        }

        txtNumSerie.setText(nvl(e.numSerie()));
        txtEnderecoMac.setText(nvl(e.enderecoMac()));
        txtLocalizacao.setText(nvl(e.localizacao()));
        txtDepartamento.setText(nvl(e.departamento()));
        txtObservacoes.setText(nvl(e.observacoes()));

        dateAquisicao.setValue(e.dataAquisicao());
        dateInstalacao.setValue(e.dataInstalacao());
        dateInstalacao.setDisable(true);
        dateUltimaVerificacao.setValue(e.dataUltimaVerificacao() != null ? e.dataUltimaVerificacao() : LocalDate.now());

        comboStatus.setValue(nvl(e.status(), "ATIVO"));
        comboCondicao.setValue(nvl(e.condicao(), "BOM"));

        if (txtSkuProduto != null) txtSkuProduto.setText(nvl(e.skuProduto()));
        if (txtFuncionarioId != null) txtFuncionarioId.setText(nvl(e.funcionarioId()));
        if (txtEquipamentoId != null) txtEquipamentoId.setText(String.valueOf(e.id()));
        if (txtCreatedAt != null && e.createdAt() != null) txtCreatedAt.setText(e.createdAt().toString());
        if (txtUpdatedAt != null && e.updatedAt() != null) txtUpdatedAt.setText(e.updatedAt().toString());
    }

    // =========================================================================
    // COMBOS E LISTENERS
    // =========================================================================

    private void configurarCombosFixos() {
        comboStatus.getItems().setAll("ATIVO", "MANUTENCAO", "BAIXADO", "RESERVA", "EM_USO");
        comboCondicao.getItems().setAll("OTIMO", "BOM", "REGULAR", "CRITICO");
        if (txtObservacoes != null && labelContadorCaracteres != null) {
            txtObservacoes.textProperty().addListener((obs, old, val) ->
                    labelContadorCaracteres.setText(val.length() + "/1000"));
        }
    }

    private void configurarListeners() {
        comboCatalogo.valueProperty().addListener((obs, antigo, novo) -> {
            if (novo == null) return;
            if (txtSkuProduto != null) txtSkuProduto.setText(novo.sku());
            dateAquisicao.setValue(novo.createdAt() != null ? novo.createdAt().toLocalDate() : LocalDate.now());
            if (equipamentoId == null || (antigo != null && !antigo.equals(novo))) {
                dateInstalacao.setValue(LocalDate.now());
                dateInstalacao.setDisable(false);
            }
        });

        comboFuncionario.valueProperty().addListener((obs, antigo, novo) -> {
            if (novo == null) return;
            if (txtFuncionarioId != null) txtFuncionarioId.setText(novo.codDep());
            txtDepartamento.setText(nvl(novo.departamento()));
            txtLocalizacao.setText(nvl(novo.localTrabalho()));
        });
    }

    private void carregarCombos() {
        try {
            List<CatalogoProdutos> produtos = catalogoService.listarTodos(1, 1000);
            comboCatalogo.getItems().setAll(produtos);
            comboCatalogo.setCellFactory(param -> new ListCell<>() {
                @Override
                protected void updateItem(CatalogoProdutos p, boolean empty) {
                    super.updateItem(p, empty);
                    setText(empty || p == null ? null : p.sku() + " - " + p.marca() + " " + p.modelo());
                }
            });

            List<Funcionarios> funcs = funcionariosService.listarAtivos(1, 1000);
            comboFuncionario.getItems().setAll(funcs);
            comboFuncionario.setCellFactory(param -> new ListCell<>() {
                @Override
                protected void updateItem(Funcionarios f, boolean empty) {
                    super.updateItem(f, empty);
                    setText(empty || f == null ? null : f.codDep() + " - " + f.nome());
                }
            });
        } catch (Exception e) {
        }
    }

    // =========================================================================
    // SALVAR
    // =========================================================================

    @FXML
    private void handleSalvar() {

        try {
            InventarioEquipamentos e = new InventarioEquipamentos(
           comboCatalogo.getValue().sku(),
            comboFuncionario.getValue().departamento(),
           txtNumSerie.getText().trim(),
        txtEnderecoMac.getText().trim(),
       dateAquisicao.getValue(),
            dateInstalacao.getValue(),
    dateUltimaVerificacao.getValue(),
           txtLocalizacao.getText().trim(),
        txtDepartamento.getText().trim(),
     comboStatus.getValue(),
            comboCondicao.getValue(),
    txtObservacoes.getText().trim(),

            if (equipamentoId == null) {
                inventarioService.cadastrarEquipamento(e);
            } else {
                e.setId(equipamentoId);
                inventarioService.atualizarEquipamento(e);
            }
            );



        } catch (Exception ex) {
        }
    }

    // =========================================================================
    // VALIDAÇÃO
    // =========================================================================


    private String nvl(String s) { return s != null && !s.isEmpty() ? s : ""; }
    private String nvl(String s, String def) { return s != null && !s.isEmpty() ? s : def; }

    @FXML private void handleCancelar() { fechar(); }
    private void fechar() { ((Stage) cancelarButton.getScene().getWindow()).close(); }
}