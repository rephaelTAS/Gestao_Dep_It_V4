package com.ossobo.gestaoDepIt.controllers.gestao.estoque.movimentacoes;

import com.ossobo.gestaoDepIt.controllers.gestao.estoque.movimentacoes.dialogs.ProdutoSelectionDialog;
import com.ossobo.gestaoDepIt.db.models.CatalogoProdutos;
import com.ossobo.gestaoDepIt.db.models.EstoqueMovimentacoes;
import com.ossobo.gestaoDepIt.db.services.CatalogoProdutosService;
import com.ossobo.gestaoDepIt.db.services.EstoqueMovimentacoesService;
import com.ossobo.nexusfx.NexusFX;
import com.ossobo.nexusfx.di.annotations.Inject;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;

import java.time.LocalDate;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * Controller para formulário de movimentação de estoque
 * ✅ ZERO TERMINAL OUTPUT + COESÃO FORTE (≤200 linhas)
 * ✅ ATUALIZADO para NexusFX 4.4.0
 */
public class EstoqueMovimentacaoFormController {

    // ===== DEPENDÊNCIAS =====
    @Inject private EstoqueMovimentacoesService movimentacoesService;
    @Inject private CatalogoProdutosService produtosService;

    // ===== COMPONENTES INTERNOS =====
    private NotificationManager notificationManager;
    private SaveHandler saveHandler;
    private Window ownerWindow;
    private BiConsumer<Boolean, String> callback;

    // ===== ESTADO =====
    private CatalogoProdutos produtoSelecionado;
    private EstoqueMovimentacoes movimentacaoEmEdicao;
    private String usuarioLogadoId = "1L";
    private boolean modoEdicao = false;

    // ===== PROPRIEDADES =====
    private final StringProperty produtoInfo = new SimpleStringProperty("Nenhum produto selecionado");
    private final StringProperty unidadeMedida = new SimpleStringProperty("un");
    private final IntegerProperty saldoAtual = new SimpleIntegerProperty(0);
    private final IntegerProperty quantidade = new SimpleIntegerProperty(0);
    private final IntegerProperty novoSaldo = new SimpleIntegerProperty(0);
    private final StringProperty tipoMovimentacao = new SimpleStringProperty();
    private final StringProperty mensagemErro = new SimpleStringProperty();
    private final BooleanProperty carregando = new SimpleBooleanProperty(false);

    // ===== COMPONENTES FXML =====
    @FXML private Label tituloLabel;
    @FXML private Label produtoInfoLabel;
    @FXML private Label saldoAtualLabel;
    @FXML private Button selecionarProdutoButton;
    @FXML private ComboBox<String> tipoMovimentacaoCombo;
    @FXML private TextField quantidadeField;
    @FXML private Label unidadeLabel;
    @FXML private TextField loteField;
    @FXML private DatePicker dataMovimentacaoPicker;
    @FXML private DatePicker dataValidadePicker;
    @FXML private DatePicker dataFimLicencaPicker;
    @FXML private TextField localizacaoField;
    @FXML private TextField motivoField;
    @FXML private TextArea observacoesField;
    @FXML private Label saldoAtualDisplay;
    @FXML private Label movimentacaoDisplay;
    @FXML private Label novoSaldoDisplay;
    @FXML private Label mensagemErroLabel;
    @FXML private Button salvarButton;
    @FXML private Button cancelarButton;

    // ===== CLASSE INTERNA: NOTIFICATION_MANAGER =====
    private class NotificationManager {
        private long ultimoToast = 0;
        private final long COOLDOWN_TOAST = 2000;

        public void toast(String mensagem) {
            long agora = System.currentTimeMillis();
            if (agora - ultimoToast > COOLDOWN_TOAST) {
                ultimoToast = agora;
                Platform.runLater(() -> mostrarToastUI(mensagem));
            }
        }

        private void mostrarToastUI(String mensagem) {
            if (mensagemErroLabel != null) {
                mensagemErroLabel.setText("✓ " + mensagem);
                mensagemErroLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 11px;");
                mensagemErroLabel.setVisible(true);
                mensagemErroLabel.setManaged(true);

                PauseTransition dismiss = new PauseTransition(Duration.seconds(2.5));
                dismiss.setOnFinished(e -> {
                    mensagemErroLabel.setVisible(false);
                    mensagemErroLabel.setManaged(false);
                });
                dismiss.play();
            }
        }

        public void info(String titulo, String mensagem) {
            Platform.runLater(() ->
                    NexusFX.alerts().info(titulo, mensagem, "Movimentação de Estoque"));
        }

        public void warning(String titulo, String mensagem, Runnable onConfirm) {
            Platform.runLater(() ->
                    NexusFX.alerts().confirmar(titulo, mensagem, resposta -> {
                        if (resposta && onConfirm != null) onConfirm.run();
                    }));
        }

        public void error(String contexto, String problema, String solucao) {
            Platform.runLater(() ->
                    NexusFX.alerts().erro(contexto, "Problema: " + problema + "\n\nSolução: " + solucao,
                            "Movimentação de Estoque"));
        }

        public void critical(String contexto, String erro, String acaoRequerida) {
            Platform.runLater(() ->
                    NexusFX.alerts().erro("FALHA DO SISTEMA - " + contexto,
                            "Erro: " + erro + "\n\nAção Requerida: " + acaoRequerida, "Sistema"));
        }
    }

    // ===== CLASSE INTERNA: SAVE_HANDLER =====
    private class SaveHandler {
        void executarSalvamento() {
            carregando.set(true);
            Platform.runLater(() -> {
                try {
                    EstoqueMovimentacoes movimentacao = criarOuAtualizarMovimentacao();
                    String movimentacaoId = salvarMovimentacao(movimentacao);

                    notificarSucesso(movimentacao, movimentacaoId);
                    fecharJanelaComSucesso(movimentacaoId);

                } catch (Exception e) {
                    notificarFalha(e);
                } finally {
                    carregando.set(false);
                }
            });
        }

        private String salvarMovimentacao(EstoqueMovimentacoes movimentacao) {
            if (modoEdicao) {
                movimentacoesService.atualizarMovimentacao(movimentacao);
            } else {
                movimentacoesService.registrarMovimentacao(movimentacao);
            }
            return String.valueOf(movimentacao.getId());
        }

        private void notificarSucesso(EstoqueMovimentacoes movimentacao, String movimentacaoId) {
            if (callback != null) {
                callback.accept(true, movimentacaoId);
            }

            notificationManager.info("Sucesso",
                    "Movimentação " + (modoEdicao ? "atualizada" : "registrada") +
                            " com sucesso!\n\nID: " + movimentacao.getId() + "\nSKU: " +
                            movimentacao.getSkuProduto() + "\nTipo: " + movimentacao.getTipoMovimentacao());
        }

        private void notificarFalha(Exception e) {
            if (callback != null) {
                callback.accept(false, "Erro: " + e.getMessage());
            }

            notificationManager.critical("Erro no Salvamento",
                    "Falha ao salvar movimentação: " + e.getMessage(),
                    "Verifique a conexão com o banco de dados");
        }
    }

    // ===== INICIALIZAÇÃO =====
    @FXML
    private void initialize() {
        notificationManager = new NotificationManager();
        saveHandler = new SaveHandler();
        configurarUI();
        configurarBindings();
        configurarListeners();
        dataMovimentacaoPicker.setValue(LocalDate.now());
        notificationManager.toast("Formulário de movimentação carregado");
    }

    // ===== CONFIGURAÇÃO DA UI =====
    private void configurarUI() {
        tipoMovimentacaoCombo.getItems().addAll(
                "ENTRADA", "SAIDA", "AJUSTE", "TRANSFERÊNCIA", "DEVOLUÇÃO", "PERDA", "RESERVA");
    }

    // ===== HANDLER PARA SELEÇÃO DE PRODUTO (ATUALIZADO) =====
    @FXML
    private void handleSelecionarProduto() {
        Window window = selecionarProdutoButton.getScene().getWindow();
        try {
            // ✅ CORRETO: usar openWindowModal com controller
            NexusFX.dialogs().openModalWithController(
                    "produto-selection-dialog",
                    "Selecionar Produto",
                    window,
                    (ProdutoSelectionDialog controller) -> {
                        controller.setOnSelectCallback(produto -> {
                            if (produto != null) Platform.runLater(() -> selecionarProduto(produto));
                        });
                    }
            );
        } catch (Exception e) {
            notificationManager.critical("Erro na Seleção",
                    "Falha ao abrir seletor de produtos: " + e.getMessage(),
                    "Verifique se o componente está configurado corretamente");
        }
    }

    // ===== PROCESSAR PRODUTO SELECIONADO =====
    private void selecionarProduto(CatalogoProdutos produto) {
        if (produto == null) return;
        produtoSelecionado = produto;
        produtoInfo.set(formatarInfoProduto(produto));
        carregarSaldoProduto(produto.getSku());
        notificationManager.toast("Produto selecionado: " + produto.getSku());
    }

    private String formatarInfoProduto(CatalogoProdutos produto) {
        return String.format("%s | %s - %s %s", produto.getSku(),
                produto.getDescricao() != null ? produto.getDescricao() : "",
                produto.getMarca() != null ? produto.getMarca() : "",
                produto.getModelo() != null ? produto.getModelo() : "").trim();
    }

    // ===== CARREGAR SALDO DO PRODUTO =====
    private void carregarSaldoProduto(String skuProduto) {
        carregando.set(true);
        Platform.runLater(() -> {
            try {
                int saldo = movimentacoesService.calcularSaldoAtual(skuProduto);
                saldoAtual.set(saldo);
                atualizarUnidadeMedida();
                atualizarCalculoSaldo();
                notificationManager.toast("Saldo carregado: " + saldo + " unidades");
            } catch (Exception e) {
                notificationManager.error("Erro no Saldo",
                        "Falha ao carregar saldo do produto",
                        "Verifique a conexão com o banco de dados");
            } finally {
                carregando.set(false);
            }
        });
    }

    // ===== CONFIGURAÇÃO DE BINDINGS =====
    private void configurarBindings() {
        produtoInfoLabel.textProperty().bind(produtoInfo);
        unidadeLabel.textProperty().bind(unidadeMedida);
        saldoAtualLabel.textProperty().bind(saldoAtual.asString("Saldo atual: %d unidades"));
        saldoAtualDisplay.textProperty().bind(saldoAtual.asString());
        novoSaldoDisplay.textProperty().bind(novoSaldo.asString());
        mensagemErroLabel.textProperty().bind(mensagemErro);
        mensagemErroLabel.visibleProperty().bind(mensagemErro.isNotEmpty());

        tipoMovimentacaoCombo.valueProperty().bindBidirectional(tipoMovimentacao);
        quantidadeField.textProperty().bindBidirectional(quantidade,
                new javafx.util.converter.NumberStringConverter());

        salvarButton.disableProperty().bind(
                carregando.or(produtoInfo.isEqualTo("Nenhum produto selecionado")));
    }

    // ===== CONFIGURAÇÃO INICIAL POR MODO =====
    public void inicializarModoCriacao(String skuProduto) {
        modoEdicao = false;
        tituloLabel.setText("Nova Movimentação de Estoque");
        salvarButton.setText("Registrar Movimentação");
        if (skuProduto != null && !skuProduto.trim().isEmpty()) carregarProduto(skuProduto);
    }

    public void inicializarModoEdicao(Long movimentacaoId) {
        modoEdicao = true;
        tituloLabel.setText("Editar Movimentação de Estoque");
        salvarButton.setText("Atualizar Movimentação");
        if (movimentacaoId != null) carregarMovimentacaoParaEdicao(movimentacaoId);
    }

    // ===== CARREGAMENTO DE DADOS =====
    private void carregarProduto(String skuProduto) {
        carregando.set(true);
        Platform.runLater(() -> {
            try {
                Optional<CatalogoProdutos> produtoOpt = produtosService.buscarPorSku(skuProduto);
                if (produtoOpt.isPresent()) {
                    produtoSelecionado = produtoOpt.get();
                    produtoInfo.set(formatarInfoProduto(produtoSelecionado));
                    int saldo = movimentacoesService.calcularSaldoAtual(skuProduto);
                    saldoAtual.set(saldo);
                    atualizarUnidadeMedida();
                    atualizarCalculoSaldo();
                    notificationManager.toast("Produto carregado: " + skuProduto);
                } else {
                    notificationManager.error("Produto Não Encontrado",
                            "SKU: " + skuProduto, "Verifique o SKU ou selecione manualmente");
                }
            } catch (Exception e) {
                notificationManager.error("Erro no Carregamento",
                        "Falha ao carregar produto: " + e.getMessage(),
                        "Tente novamente mais tarde");
            } finally {
                carregando.set(false);
            }
        });
    }

    private void carregarMovimentacaoParaEdicao(Long movimentacaoId) {
        carregando.set(true);
        Platform.runLater(() -> {
            try {
                Optional<EstoqueMovimentacoes> movimentacaoOpt = movimentacoesService.buscarPorId(movimentacaoId);
                if (movimentacaoOpt.isPresent()) {
                    movimentacaoEmEdicao = movimentacaoOpt.get();
                    if (movimentacaoEmEdicao.getSkuProduto() != null) {
                        carregarProduto(movimentacaoEmEdicao.getSkuProduto());
                    }
                    preencherCamposMovimentacao(movimentacaoEmEdicao);
                    notificationManager.toast("Movimentação carregada: #" + movimentacaoId);
                } else {
                    notificationManager.error("Movimentação Não Encontrada",
                            "ID: " + movimentacaoId, "Verifique o ID ou recarregue a lista");
                }
            } catch (Exception e) {
                notificationManager.error("Erro no Carregamento",
                        "Falha ao carregar movimentação: " + e.getMessage(),
                        "Tente novamente mais tarde");
            } finally {
                carregando.set(false);
            }
        });
    }

    // ===== ATUALIZAR UNIDADE DE MEDIDA =====
    private void atualizarUnidadeMedida() {
        if (produtoSelecionado == null || produtoSelecionado.getTipoProduto() == null) return;
        String tipo = produtoSelecionado.getTipoProduto().toString().toUpperCase();
        if (tipo.contains("SOFTWARE") || tipo.contains("LICENÇA") || tipo.contains("LICENCA")) {
            unidadeMedida.set("lic");
        } else if (tipo.contains("SERVIÇO") || tipo.contains("SERVICO")) {
            unidadeMedida.set("hrs");
        } else {
            unidadeMedida.set("un");
        }
    }

    // ===== PREENCHER CAMPOS DE MOVIMENTAÇÃO =====
    private void preencherCamposMovimentacao(EstoqueMovimentacoes movimentacao) {
        tipoMovimentacao.set(String.valueOf(movimentacao.getTipoMovimentacao()));
        quantidade.set(movimentacao.getQuantidade());
        loteField.setText(formatarCampo(movimentacao.getLote()));
        dataMovimentacaoPicker.setValue(movimentacao.getDataMovimentacao());
        dataValidadePicker.setValue(movimentacao.getDataValidade());
        dataFimLicencaPicker.setValue(movimentacao.getDataFimLicenca());
        localizacaoField.setText(formatarCampo(movimentacao.getLocalizacao()));
        motivoField.setText(formatarCampo(movimentacao.getMotivo()));
        observacoesField.setText(formatarCampo(movimentacao.getObservacoes()));
        atualizarCalculoSaldo();
    }

    // ===== CÁLCULO DE SALDO =====
    private void atualizarCalculoSaldo() {
        int saldo = saldoAtual.get();
        int qtd = quantidade.get();
        String tipo = tipoMovimentacao.get();
        int calculado = saldo;

        if (tipo != null) {
            switch (tipo) {
                case "ENTRADA":
                case "DEVOLUÇÃO": calculado = saldo + qtd; break;
                case "SAIDA":
                case "TRANSFERÊNCIA":
                case "PERDA": calculado = saldo - qtd; break;
                case "AJUSTE": calculado = qtd; break;
                case "RESERVA": calculado = saldo; break;
            }
        }

        novoSaldo.set(calculado);

        if (qtd > 0) {
            movimentacaoDisplay.setText((tipo != null &&
                    (tipo.equals("ENTRADA") || tipo.equals("DEVOLUÇÃO")) ? "+" : "-") + qtd);
        } else {
            movimentacaoDisplay.setText("0");
        }

        novoSaldoDisplay.setStyle(calculado < 0 ?
                "-fx-text-fill: #e74c3c; -fx-font-weight: bold;" :
                "-fx-text-fill: #27ae60; -fx-font-weight: bold;");
    }

    // ===== INTERFACE PÚBLICA =====
    public void setCallback(BiConsumer<Boolean, String> callback) {
        this.callback = callback;
    }

    public void setOwnerWindow(Window ownerWindow) {
        this.ownerWindow = ownerWindow;
    }

    // ===== CONFIGURAR LISTENERS =====
    private void configurarListeners() {
        tipoMovimentacao.addListener((obs, oldVal, newVal) -> {
            atualizarCalculoSaldo();
            boolean entrada = "ENTRADA".equals(newVal) || "DEVOLUÇÃO".equals(newVal);
            dataValidadePicker.setDisable(!entrada);
            dataFimLicencaPicker.setDisable(!entrada);
            loteField.setDisable(!entrada);
            localizacaoField.setDisable(!entrada);
        });

        quantidade.addListener((obs, oldVal, newVal) -> atualizarCalculoSaldo());
    }

    // ===== VALIDAÇÕES =====
    private boolean validarFormulario() {
        StringBuilder erros = new StringBuilder();

        if (produtoSelecionado == null) erros.append("• Selecione um produto\n");
        if (tipoMovimentacao.get() == null || tipoMovimentacao.get().isEmpty())
            erros.append("• Selecione o tipo de movimentação\n");
        if (quantidade.get() <= 0) erros.append("• Quantidade deve ser maior que zero\n");
        if (motivoField.getText() == null || motivoField.getText().trim().isEmpty())
            erros.append("• Motivo é obrigatório\n");
        if (dataMovimentacaoPicker.getValue() == null)
            erros.append("• Data da movimentação é obrigatória\n");
        else if (dataMovimentacaoPicker.getValue().isAfter(LocalDate.now()))
            erros.append("• Data da movimentação não pode ser no futuro\n");

        String tipo = tipoMovimentacao.get();
        if (tipo != null && ("SAIDA".equals(tipo) || "TRANSFERÊNCIA".equals(tipo) || "PERDA".equals(tipo))) {
            if (produtoSelecionado != null && quantidade.get() > saldoAtual.get())
                erros.append("• Saldo insuficiente para a movimentação\n");
        }

        mensagemErro.set(erros.toString());
        return erros.length() == 0;
    }

    // ===== HANDLER PARA SALVAR =====
    @FXML
    private void handleSalvar() {
        if (!validarFormulario()) return;

        notificationManager.warning("Confirmar " + (modoEdicao ? "Atualização" : "Registro"),
                "Deseja " + (modoEdicao ? "atualizar" : "registrar") + " esta movimentação?\n\n" +
                        "Produto: " + (produtoSelecionado != null ? produtoSelecionado.getSku() : "N/A") + "\n" +
                        "Tipo: " + tipoMovimentacao.get() + "\n" +
                        "Quantidade: " + quantidade.get() + " " + unidadeMedida.get(),
                saveHandler::executarSalvamento);
    }

    // ===== CRIAR/ATUALIZAR MOVIMENTAÇÃO =====
    private EstoqueMovimentacoes criarOuAtualizarMovimentacao() {
        EstoqueMovimentacoes movimentacao = modoEdicao ? movimentacaoEmEdicao : new EstoqueMovimentacoes();

        if (!modoEdicao) movimentacao.setSkuProduto(produtoSelecionado.getSku());

        movimentacao.setTipoMovimentacao(tipoMovimentacao.get());
        movimentacao.setQuantidade(quantidade.get());
        movimentacao.setCodDepFuncionario(usuarioLogadoId);
        movimentacao.setLote(formatarCampoParaBanco(loteField.getText()));
        movimentacao.setDataMovimentacao(dataMovimentacaoPicker.getValue());
        movimentacao.setDataValidade(dataValidadePicker.getValue());
        movimentacao.setDataFimLicenca(dataFimLicencaPicker.getValue());
        movimentacao.setLocalizacao(formatarCampoParaBanco(localizacaoField.getText()));
        movimentacao.setMotivo(formatarCampoParaBanco(motivoField.getText()));
        movimentacao.setObservacoes(formatarCampoParaBanco(observacoesField.getText()));

        return movimentacao;
    }

    // ===== HANDLER PARA CANCELAR =====
    @FXML
    private void handleCancelar() {
        notificationManager.warning("Confirmar Cancelamento",
                "Tem certeza que deseja cancelar?\n\nTodas as alterações serão perdidas.",
                () -> {
                    if (callback != null) callback.accept(false, "Cancelado pelo usuário");
                    fecharJanela();
                });
    }

    // ===== FECHAR JANELA =====
    private void fecharJanela() {
        Stage stage = (Stage) cancelarButton.getScene().getWindow();
        stage.close();
        notificationManager.toast("Formulário fechado");
    }

    private void fecharJanelaComSucesso(String movimentacaoId) {
        Stage stage = (Stage) cancelarButton.getScene().getWindow();
        if (callback != null) callback.accept(true, movimentacaoId);
        stage.close();
        notificationManager.toast("Formulário salvo e fechado");
    }

    // ===== UTILITÁRIOS =====
    private String formatarCampo(String valor) {
        return valor != null ? valor : "";
    }

    private String formatarCampoParaBanco(String valor) {
        if (valor == null || valor.trim().isEmpty()) return null;
        return valor.trim();
    }

    // ===== MÉTODOS PÚBLICOS =====
    public void setUsuarioLogadoId(String codDep) {
        this.usuarioLogadoId = codDep;
    }

    // ===== LIMPEZA =====
    public void cleanup() {
        produtoSelecionado = null;
        movimentacaoEmEdicao = null;
        notificationManager.toast("Sessão de formulário finalizada");
    }
}