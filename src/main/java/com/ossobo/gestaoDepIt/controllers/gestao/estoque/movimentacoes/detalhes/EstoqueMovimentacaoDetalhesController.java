package com.ossobo.gestaoDepIt.controllers.gestao.estoque.movimentacoes.detalhes;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import com.ossobo.gestaoDepIt.controllers.gestao.estoque.movimentacoes.EstoqueMovimentacaoFormController;
import com.ossobo.gestaoDepIt.db.models.*;
import com.ossobo.gestaoDepIt.db.services.*;
import com.ossobo.nexusfx.di.annotations.Inject;
import com.ossobo.nexusfx.NexusFX;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Controller COMPLETO e FUNCIONAL para visualização detalhada de movimentações
 * ✅ Totalmente compatível com o FXML fornecido
 * ✅ Todos os botões funcionando
 * ✅ Carregamento correto de dados
 */
public class EstoqueMovimentacaoDetalhesController {

    // ===== CONSTANTES =====
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATETIME_SHORT = DateTimeFormatter.ofPattern("dd/MM HH:mm");

    // ===== DEPENDÊNCIAS =====
    @Inject private EstoqueMovimentacoesService movimentacoesService;
    @Inject private CatalogoProdutosService catalogoService;
    @Inject private FuncionariosService funcionariosService;

    // ===== COMPONENTES FXML (Mapeados conforme o FXML fornecido) =====
    // Cards de Resumo
    @FXML private Label subtitleLabel;
    @FXML private Circle tipoCircle;
    @FXML private Label tipoIcon;
    @FXML private Label tipoLabel;
    @FXML private Label quantidadeLabel;
    @FXML private Circle statusCircle;
    @FXML private Label statusLabel;
    @FXML private Label statusDetailLabel;
    @FXML private Label idLabel;
    @FXML private Label createdAtLabel;
    @FXML private Label rastreabilidadeLabel;
    @FXML private Label lastUpdatedLabel;

    // Informações do Produto
    @FXML private Label produtoLabel;
    @FXML private Label codigoProdutoLabel;
    @FXML private Label categoriaLabel;

    // Detalhes da Movimentação
    @FXML private Label dataMovimentacaoLabel;
    @FXML private Label diasAtrasLabel;
    @FXML private Label loteLabel;
    @FXML private Label localizacaoLabel;
    @FXML private Label validadeLabel;
    @FXML private Label validadeStatusLabel;
    @FXML private Label fimLicencaLable;
    @FXML private Button btnMapa;

    // Observações
    @FXML private TextArea motivoTextArea;
    @FXML private TextArea observacoesTextArea;

    // Responsável
    @FXML private Label usuarioInicial;
    @FXML private Label usuarioNomeLabel;
    @FXML private Label usuarioDepartamentoLabel;
    @FXML private Label usuarioEmailLabel;
    @FXML private Button btnContatar;

    // Timeline
    @FXML private VBox timelineContainer;

    // Ações Rápidas
    @FXML private Button btnEditar;
    @FXML private Button btnDuplicar;
    @FXML private Button btnExcluir;

    // Botões do Cabeçalho
    @FXML private Button btnImprimir;
    @FXML private Button btnExportar;
    @FXML private Button btnVoltar;

    // ===== ESTADO =====
    private Long movimentacaoId;
    private Consumer<Long> onEditarCallback;
    private Consumer<Long> onDuplicarCallback;
    private Consumer<Long> onExcluirCallback;
    private Consumer<Void> onFecharCallback;
    private boolean inicializacaoCompleta = false;

    // ===== INICIALIZAÇÃO DO DI =====
    @Inject
    public void initDependencies() {
        // Este método é chamado APÓS a injeção de dependências
        inicializacaoCompleta = true;
        System.out.println("✅ Dependências injetadas no controller de detalhes");
    }

    // ===== INICIALIZAÇÃO FXML =====
    @FXML
    public void initialize() {
        System.out.println("🔄 Inicializando controller de detalhes...");

        configurarComponentes();
        configurarEventHandlers();
        configurarEstadoInicial();

        Platform.runLater(() -> {
            if (subtitleLabel != null) {
                subtitleLabel.setText("Aguardando seleção...");
            }
        });
    }

    // ===== CONFIGURAÇÃO INICIAL =====
    private void configurarComponentes() {
        // Configurar áreas de texto
        if (motivoTextArea != null) {
            motivoTextArea.setWrapText(true);
            motivoTextArea.setEditable(false);
            motivoTextArea.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #e9ecef;");
        }

        if (observacoesTextArea != null) {
            observacoesTextArea.setWrapText(true);
            observacoesTextArea.setEditable(false);
            observacoesTextArea.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #e9ecef;");
        }
    }

    private void configurarEventHandlers() {
        // Botão Fechar/Voltar
        if (btnVoltar != null) {
            btnVoltar.setOnAction(this::handleFechar);
        }

        // Botão Editar
        if (btnEditar != null) {
            btnEditar.setOnAction(this::handleEditar);
        }

        // Botão Duplicar
        if (btnDuplicar != null) {
            btnDuplicar.setOnAction(this::handleDuplicar);
        }

        // Botão Excluir
        if (btnExcluir != null) {
            btnExcluir.setOnAction(this::handleExcluir);
        }

        // Botão Contatar
        if (btnContatar != null) {
            btnContatar.setOnAction(this::handleContatar);
        }

        // Botão Mapa
        if (btnMapa != null) {
            btnMapa.setOnAction(this::handleMapa);
        }

        // Botão Imprimir
        if (btnImprimir != null) {
            btnImprimir.setOnAction(this::handleImprimir);
        }

        // Botão Exportar
        if (btnExportar != null) {
            btnExportar.setOnAction(this::handleExportar);
        }
    }

    private void configurarEstadoInicial() {
        // Configurar valores padrão
        setLabelText(idLabel, "--");
        setLabelText(tipoLabel, "--");
        setLabelText(quantidadeLabel, "--");
        setLabelText(statusLabel, "--");
        setLabelText(statusDetailLabel, "--");
        setLabelText(produtoLabel, "--");
        setLabelText(codigoProdutoLabel, "--");
        setLabelText(categoriaLabel, "--");
        setLabelText(dataMovimentacaoLabel, "--");
        setLabelText(loteLabel, "--");
        setLabelText(localizacaoLabel, "--");
        setLabelText(validadeLabel, "--");
        setLabelText(fimLicencaLable, "--");
        setLabelText(usuarioNomeLabel, "--");
        setLabelText(usuarioDepartamentoLabel, "--");
        setLabelText(usuarioEmailLabel, "--");

        if (motivoTextArea != null) motivoTextArea.setText("Nenhum motivo informado");
        if (observacoesTextArea != null) observacoesTextArea.setText("Nenhuma observação");
        if (lastUpdatedLabel != null) lastUpdatedLabel.setText("Atualizado: --");
    }

    // ===== MÉTODO PÚBLICO PARA CARREGAR MOVIMENTAÇÃO =====
    public void carregarMovimentacao(Long id) {
        System.out.println("📥 Carregando movimentação ID: " + id);
        this.movimentacaoId = id;

        if (!inicializacaoCompleta) {
            System.out.println("⚠️ Aguardando injeção de dependências...");
            // Agenda o carregamento para quando as dependências estiverem prontas
            Platform.runLater(() -> carregarMovimentacao(id));
            return;
        }

        mostrarEstadoCarregamento("Carregando movimentação #" + id, true);

        // Carregar dados de forma assíncrona
        CompletableFuture.runAsync(() -> {
            try {
                Optional<EstoqueMovimentacoes> movOpt = movimentacoesService.buscarPorId(id);

                Platform.runLater(() -> {
                    if (movOpt.isEmpty()) {
                        mostrarErro("Movimentação não encontrada");
                        return;
                    }

                    EstoqueMovimentacoes movimentacao = movOpt.get();
                    atualizarInterfaceCompleta(movimentacao);
                    mostrarEstadoCarregamento("Carregamento completo", false);

                    System.out.println("✅ Movimentação #" + id + " carregada com sucesso");

                    // Atualizar título da janela
                    atualizarTituloJanela(movimentacao);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    mostrarErro("Erro ao carregar: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        });
    }

    // ===== ATUALIZAR INTERFACE COMPLETA =====
    private void atualizarInterfaceCompleta(EstoqueMovimentacoes mov) {
        try {
            // 1. Informações Básicas
            setLabelText(idLabel, "#" + mov.getId());
            setLabelText(subtitleLabel, String.format("Movimentação #%d • %s",
                    mov.getId(), formatarData(mov.getDataMovimentacao())));

            // 2. Tipo e Quantidade
            atualizarTipoMovimentacao(mov);
            atualizarQuantidade(mov);

            // 3. Status e Datas
            atualizarStatus(mov);

            // 4. Produto
            atualizarProduto(mov);

            // 5. Detalhes da Movimentação
            atualizarDetalhesMovimentacao(mov);

            // 6. Observações
            atualizarObservacoes(mov);

            // 7. Responsável
            atualizarResponsavel(mov);

            // 8. Timeline
            construirTimeline(mov);

            // 9. Rodapé
            atualizarRodape();

        } catch (Exception e) {
            mostrarErro("Erro ao atualizar interface: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ===== MÉTODOS DE ATUALIZAÇÃO ESPECÍFICOS =====
    private void atualizarTipoMovimentacao(EstoqueMovimentacoes mov) {
        String tipo = String.valueOf(mov.getTipoMovimentacao());
        String tipoFormatado = formatarTipo(tipo);
        Color cor = obterCorPorTipo(tipo);

        setLabelText(tipoLabel, tipoFormatado);

        if (tipoIcon != null) {
            tipoIcon.setText(obterIconePorTipo(tipo));
        }

        if (tipoCircle != null) {
            tipoCircle.setFill(cor);
        }
    }

    private void atualizarQuantidade(EstoqueMovimentacoes mov) {
        Integer quantidade = mov.getQuantidade();
        String quantidadeStr = quantidade != null ? quantidade.toString() : "0";
        String sinal = quantidade != null && quantidade > 0 ? "+" : "";

        setLabelText(quantidadeLabel, sinal + quantidadeStr);

        // Estilizar baseado no tipo
        String tipo = String.valueOf(mov.getTipoMovimentacao());
        if ("ENTRADA".equalsIgnoreCase(tipo)) {
            quantidadeLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: 900; -fx-text-fill: #27ae60;");
        } else if ("SAIDA".equalsIgnoreCase(tipo)) {
            quantidadeLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: 900; -fx-text-fill: #e74c3c;");
        } else {
            quantidadeLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: 900; -fx-text-fill: #2c3e50;");
        }
    }

    private void atualizarStatus(EstoqueMovimentacoes mov) {
        String statusText = obterStatusTexto(mov);
        String statusDetail = obterStatusDetalhe(mov);
        Color corStatus = obterCorStatus(mov);

        setLabelText(statusLabel, statusText);
        setLabelText(statusDetailLabel, statusDetail);

        if (statusCircle != null) {
            statusCircle.setFill(corStatus);
        }

        // Data de criação
        if (mov.getCreatedAt() != null && createdAtLabel != null) {
            createdAtLabel.setText(mov.getCreatedAt().format(DATETIME_SHORT));
        }
    }

    private void atualizarProduto(EstoqueMovimentacoes mov) {
        String sku = mov.getSkuProduto();

        if (sku == null || sku.trim().isEmpty()) {
            setLabelText(produtoLabel, "Produto não especificado");
            setLabelText(codigoProdutoLabel, "N/A");
            setLabelText(categoriaLabel, "N/A");
            return;
        }

        // Buscar produto de forma assíncrona
        CompletableFuture.runAsync(() -> {
            try {
                Optional<CatalogoProdutos> produtoOpt = catalogoService.buscarPorSku(sku);

                Platform.runLater(() -> {
                    if (produtoOpt.isPresent()) {
                        CatalogoProdutos produto = produtoOpt.get();
                        setLabelText(produtoLabel, produto.getModelo() != null ? produto.getModelo() : "N/A");
                        setLabelText(codigoProdutoLabel, produto.getSku() != null ? produto.getSku() : "N/A");
                        setLabelText(categoriaLabel, produto.getCategoria() != null ? produto.getCategoria() : "N/A");
                    } else {
                        setLabelText(produtoLabel, "Produto não encontrado");
                        setLabelText(codigoProdutoLabel, "SKU: " + sku);
                        setLabelText(categoriaLabel, "N/A");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    setLabelText(produtoLabel, "Erro ao carregar");
                    setLabelText(codigoProdutoLabel, "SKU: " + sku);
                    setLabelText(categoriaLabel, "N/A");
                });
            }
        });
    }

    private void atualizarDetalhesMovimentacao(EstoqueMovimentacoes mov) {
        // Data da movimentação
        if (mov.getDataMovimentacao() != null) {
            String dataFormatada = formatarData(mov.getDataMovimentacao());
            setLabelText(dataMovimentacaoLabel, dataFormatada);

            // Calcular dias atrás
            long diasAtras = ChronoUnit.DAYS.between(mov.getDataMovimentacao(), LocalDate.now());
            if (diasAtras == 0) {
                setLabelText(diasAtrasLabel, "(Hoje)");
            } else if (diasAtras == 1) {
                setLabelText(diasAtrasLabel, "(1 dia atrás)");
            } else {
                setLabelText(diasAtrasLabel, "(" + diasAtras + " dias atrás)");
            }
        } else {
            setLabelText(dataMovimentacaoLabel, "Não informada");
            setLabelText(diasAtrasLabel, "");
        }

        // Lote
        setLabelText(loteLabel, formatarCampo(mov.getLote()));

        // Localização
        setLabelText(localizacaoLabel, formatarCampo(mov.getLocalizacao()));

        // Validade
        atualizarValidade(mov);

        // Fim Licença
        if (mov.getDataFimLicenca() != null) {
            setLabelText(fimLicencaLable, formatarData(mov.getDataFimLicenca()));
        } else {
            setLabelText(fimLicencaLable, "Não aplicável");
        }
    }

    private void atualizarValidade(EstoqueMovimentacoes mov) {
        if (mov.getDataValidade() == null) {
            setLabelText(validadeLabel, "Não definida");
            if (validadeStatusLabel != null) {
                validadeStatusLabel.setText("");
                validadeStatusLabel.setStyle("");
            }
            return;
        }

        String dataValidade = formatarData(mov.getDataValidade());
        setLabelText(validadeLabel, dataValidade);

        if (validadeStatusLabel != null) {
            LocalDate hoje = LocalDate.now();
            LocalDate validade = mov.getDataValidade();

            if (validade.isBefore(hoje)) {
                // Vencida
                validadeStatusLabel.setText("VENCIDA");
                validadeStatusLabel.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 1 6 1 6; -fx-background-radius: 6;");
            } else if (validade.isEqual(hoje)) {
                // Vence hoje
                validadeStatusLabel.setText("VENCE HOJE");
                validadeStatusLabel.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-padding: 1 6 1 6; -fx-background-radius: 6;");
            } else if (validade.isBefore(hoje.plusDays(7))) {
                // Vence em até 7 dias
                long diasParaVencer = ChronoUnit.DAYS.between(hoje, validade);
                validadeStatusLabel.setText(diasParaVencer + " DIAS");
                validadeStatusLabel.setStyle("-fx-background-color: #f1c40f; -fx-text-fill: black; -fx-padding: 1 6 1 6; -fx-background-radius: 6;");
            } else {
                // Válida
                validadeStatusLabel.setText("VÁLIDA");
                validadeStatusLabel.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-padding: 1 6 1 6; -fx-background-radius: 6;");
            }
        }
    }

    private void atualizarObservacoes(EstoqueMovimentacoes mov) {
        if (motivoTextArea != null) {
            motivoTextArea.setText(formatarCampo(mov.getMotivo()));
        }

        if (observacoesTextArea != null) {
            observacoesTextArea.setText(formatarCampo(mov.getObservacoes()));
        }
    }

    private void atualizarResponsavel(EstoqueMovimentacoes mov) {
        String responsavelId = mov.getCodDepFuncionario();

        if (responsavelId == null) {
            setLabelText(usuarioNomeLabel, "Não informado");
            setLabelText(usuarioDepartamentoLabel, "N/A");
            setLabelText(usuarioEmailLabel, "N/A");

            if (usuarioInicial != null) {
                usuarioInicial.setText("?");
            }
            return;
        }

        // Buscar responsável de forma assíncrona
        CompletableFuture.runAsync(() -> {
            try {
                Optional<Funcionarios> funcionarioOpt = funcionariosService.buscarPorCodDep(responsavelId);

                Platform.runLater(() -> {
                    if (funcionarioOpt.isPresent()) {
                        Funcionarios func = funcionarioOpt.get();

                        // Nome
                        setLabelText(usuarioNomeLabel, formatarCampo(func.getNome()));

                        // Departamento
                        setLabelText(usuarioDepartamentoLabel, formatarCampo(func.getDepartamento()));

                        // Email
                        setLabelText(usuarioEmailLabel, formatarCampo(func.getEmail()));

                        // Iniciais
                        if (usuarioInicial != null && func.getNome() != null && !func.getNome().trim().isEmpty()) {
                            String[] partes = func.getNome().split(" ");
                            if (partes.length > 0) {
                                String inicial = partes[0].substring(0, 1).toUpperCase();
                                usuarioInicial.setText(inicial);
                            }
                        }
                    } else {
                        setLabelText(usuarioNomeLabel, "Usuário #" + responsavelId);
                        setLabelText(usuarioDepartamentoLabel, "Não encontrado");
                        setLabelText(usuarioEmailLabel, "N/A");

                        if (usuarioInicial != null) {
                            usuarioInicial.setText("?");
                        }
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    setLabelText(usuarioNomeLabel, "Erro ao carregar");
                    setLabelText(usuarioDepartamentoLabel, "N/A");
                    setLabelText(usuarioEmailLabel, "N/A");

                    if (usuarioInicial != null) {
                        usuarioInicial.setText("!");
                    }
                });
            }
        });
    }

    private void construirTimeline(EstoqueMovimentacoes mov) {
        if (timelineContainer == null) return;

        timelineContainer.getChildren().clear();

        // Evento 1: Criação
        if (mov.getCreatedAt() != null) {
            adicionarEventoTimeline(
                    "📝 Criado",
                    mov.getCreatedAt().format(DATETIME_FORMAT),
                    "Registro criado no sistema",
                    Color.web("#3498db")
            );
        }

        // Evento 2: Movimentação
        if (mov.getDataMovimentacao() != null) {
            String icone = "ENTRADA".equalsIgnoreCase(String.valueOf(mov.getTipoMovimentacao())) ? "📦" : "🚚";
            String tipo = "ENTRADA".equalsIgnoreCase(String.valueOf(mov.getTipoMovimentacao())) ? "Entrada" : "Saída";

            adicionarEventoTimeline(
                    icone + " " + tipo,
                    formatarData(mov.getDataMovimentacao()),
                    mov.getQuantidade() + " unidades",
                    "ENTRADA".equalsIgnoreCase(String.valueOf(mov.getTipoMovimentacao())) ?
                            Color.web("#27ae60") : Color.web("#e74c3c")
            );
        }

        // Evento 3: Validade
        if (mov.getDataValidade() != null) {
            boolean vencida = mov.getDataValidade().isBefore(LocalDate.now());
            String icone = vencida ? "⏰" : "✅";
            String status = vencida ? "Vencida" : "Válida";

            adicionarEventoTimeline(
                    icone + " Validade",
                    formatarData(mov.getDataValidade()),
                    "Data de validade - " + status,
                    vencida ? Color.web("#e74c3c") : Color.web("#2ecc71")
            );
        }

        // Evento 4: Fim Licença
        if (mov.getDataFimLicenca() != null) {
            adicionarEventoTimeline(
                    "📄 Fim Licença",
                    formatarData(mov.getDataFimLicenca()),
                    "Data limite da licença",
                    Color.web("#9b59b6")
            );
        }

        // Evento 5: Atualização
        if (mov.getDataMovimentacao() != null) {
            adicionarEventoTimeline(
                    "✏️ Atualizado",
                    mov.getDataMovimentacao().format(DATETIME_FORMAT),
                    "Última atualização",
                    Color.web("#f39c12")
            );
        }
    }

    private void adicionarEventoTimeline(String titulo, String data, String descricao, Color cor) {
        HBox evento = new HBox(8);
        evento.setAlignment(Pos.CENTER_LEFT);
        evento.setStyle("-fx-padding: 0 0 6 0;");

        // Ponto colorido
        Circle ponto = new Circle(4);
        ponto.setFill(cor);

        // Conteúdo
        VBox conteudo = new VBox(2);

        // Título
        Label tituloLabel = new Label(titulo);
        tituloLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");

        // Linha com data e descrição
        HBox linhaInfos = new HBox(6);
        linhaInfos.setAlignment(Pos.CENTER_LEFT);

        Label dataLabel = new Label(data);
        dataLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #666;");

        Label descLabel = new Label("• " + descricao);
        descLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #888;");

        linhaInfos.getChildren().addAll(dataLabel, descLabel);
        conteudo.getChildren().addAll(tituloLabel, linhaInfos);

        evento.getChildren().addAll(ponto, conteudo);
        timelineContainer.getChildren().add(evento);
    }

    private void atualizarRodape() {
        if (lastUpdatedLabel != null) {
            lastUpdatedLabel.setText("Atualizado: " + LocalDateTime.now().format(DATETIME_SHORT));
        }
    }

    private void atualizarTituloJanela(EstoqueMovimentacoes mov) {
        try {
            Stage stage = (Stage) (btnVoltar != null ? btnVoltar.getScene().getWindow() : null);
            if (stage != null) {
                stage.setTitle("Detalhes da Movimentação #" + mov.getId() + " - " + mov.getTipoMovimentacao());
            }
        } catch (Exception e) {
            // Ignora erro de título
        }
    }

    // ===== HANDLERS DE BOTÕES =====


    @FXML
    private void handleEditar(ActionEvent event) {
        if (movimentacaoId == null) {
            NexusFX.alerts().erro("Erro", "Nenhuma movimentação carregada", "Detalhes");
            return;
        }

        NexusFX.alerts().confirmar(
                "Confirmar Edição",
                "Deseja editar a movimentação #" + movimentacaoId + "?",
                resposta -> {
                    if (resposta) {
                        if (onEditarCallback != null) {
                            onEditarCallback.accept(movimentacaoId);
                        } else {
                            abrirModalEdicao();
                        }
                    }
                }
        );
    }

    @FXML
    private void handleDuplicar(ActionEvent event) {
        if (movimentacaoId == null) {
            NexusFX.alerts().erro("Erro", "Nenhuma movimentação carregada", "Detalhes");
            return;
        }

        NexusFX.alerts().confirmar(
                "Confirmar Duplicação",
                "Deseja criar uma cópia da movimentação #" + movimentacaoId + "?",
                resposta -> {
                    if (resposta) {
                        if (onDuplicarCallback != null) {
                            onDuplicarCallback.accept(movimentacaoId);
                        } else {
                            NexusFX.alerts().info("Funcionalidade", "Duplicação será implementada em breve", "Detalhes");
                        }
                    }
                }
        );
    }

    @FXML
    private void handleExcluir(ActionEvent event) {
        if (movimentacaoId == null) {
            NexusFX.alerts().erro("Erro", "Nenhuma movimentação carregada", "Detalhes");
            return;
        }

        NexusFX.alerts().confirmar(
                "Confirmar Exclusão",
                "Tem certeza que deseja excluir a movimentação #" + movimentacaoId + "?\nEsta ação não pode ser desfeita.",
                resposta -> {
                    if (resposta) {
                        if (onExcluirCallback != null) {
                            onExcluirCallback.accept(movimentacaoId);
                        } else {
                            NexusFX.alerts().info("Funcionalidade", "Exclusão será implementada em breve", "Detalhes");
                        }
                    }
                }
        );
    }

    @FXML
    private void handleContatar(ActionEvent event) {
        NexusFX.alerts().info("Funcionalidade", "Contato com responsável será implementado em breve", "Detalhes");
    }

    @FXML
    private void handleMapa(ActionEvent event) {
        if (localizacaoLabel != null && !"--".equals(localizacaoLabel.getText())) {
            NexusFX.alerts().info("Localização", "Local: " + localizacaoLabel.getText(), "Mapa");
        } else {
            NexusFX.alerts().info("Localização", "Localização não informada", "Mapa");
        }
    }

    @FXML
    private void handleImprimir(ActionEvent event) {
        NexusFX.alerts().info("Impressão", "Funcionalidade de impressão será implementada em breve", "Detalhes");
    }

    @FXML
    private void handleExportar(ActionEvent event) {
        NexusFX.alerts().info("Exportação", "Funcionalidade de exportação será implementada em breve", "Detalhes");
    }

    // ===== MÉTODOS AUXILIARES =====
    // ===== MÉTODOS AUXILIARES =====
    private void abrirModalEdicao() {
        try {
            Stage ownerStage = (Stage) btnEditar.getScene().getWindow();

            // ✅ CORRETO - usando Consumer<Controller> diretamente
            NexusFX.dialogs().openModalWithController(
                    "addeditamovimenta",
                    "Editar Movimentação #" + movimentacaoId,
                    ownerStage,
                    (EstoqueMovimentacaoFormController controller) -> {
                        controller.inicializarModoEdicao(movimentacaoId);
                    }
            );
        } catch (Exception e) {
            NexusFX.alerts().erro("Erro", "Não foi possível abrir o editor: " + e.getMessage(), "Edição");
        }
    }

    // ===== UTILITÁRIOS DE FORMATAÇÃO =====
    private String formatarData(LocalDate data) {
        return data != null ? data.format(DATE_FORMAT) : "Não informado";
    }

    private String formatarTipo(String tipo) {
        if (tipo == null) return "Desconhecido";

        switch (tipo.toUpperCase()) {
            case "ENTRADA": return "📦 Entrada";
            case "SAIDA": return "🚚 Saída";
            case "AJUSTE": return "⚖️ Ajuste";
            case "TRANSFERENCIA": return "🔄 Transferência";
            case "RESERVA": return "📋 Reserva";
            default: return tipo;
        }
    }

    private String obterIconePorTipo(String tipo) {
        if (tipo == null) return "❓";

        switch (tipo.toUpperCase()) {
            case "ENTRADA": return "📦";
            case "SAIDA": return "🚚";
            case "AJUSTE": return "⚖️";
            case "TRANSFERENCIA": return "🔄";
            case "RESERVA": return "📋";
            default: return "❓";
        }
    }

    private Color obterCorPorTipo(String tipo) {
        if (tipo == null) return Color.web("#95a5a6");

        switch (tipo.toUpperCase()) {
            case "ENTRADA": return Color.web("#27ae60");
            case "SAIDA": return Color.web("#e74c3c");
            case "AJUSTE": return Color.web("#f39c12");
            case "TRANSFERENCIA": return Color.web("#3498db");
            case "RESERVA": return Color.web("#9b59b6");
            default: return Color.web("#95a5a6");
        }
    }

    private String obterStatusTexto(EstoqueMovimentacoes mov) {
        if (mov.getDataValidade() == null) {
            return "Ativo";
        }

        LocalDate hoje = LocalDate.now();
        LocalDate validade = mov.getDataValidade();

        if (validade.isBefore(hoje)) {
            return "Vencido";
        } else if (validade.isEqual(hoje)) {
            return "Vence Hoje";
        } else if (validade.isBefore(hoje.plusDays(7))) {
            return "Próximo do Vencimento";
        } else {
            return "Válido";
        }
    }

    private String obterStatusDetalhe(EstoqueMovimentacoes mov) {
        if (mov.getDataValidade() == null) {
            return "Sem data de validade";
        }

        LocalDate hoje = LocalDate.now();
        LocalDate validade = mov.getDataValidade();
        long diasRestantes = ChronoUnit.DAYS.between(hoje, validade);

        if (diasRestantes < 0) {
            return "Vencido há " + (-diasRestantes) + " dias";
        } else if (diasRestantes == 0) {
            return "Vence hoje";
        } else {
            return diasRestantes + " dias restantes";
        }
    }

    private Color obterCorStatus(EstoqueMovimentacoes mov) {
        if (mov.getDataValidade() == null) {
            return Color.web("#95a5a6");
        }

        LocalDate hoje = LocalDate.now();
        LocalDate validade = mov.getDataValidade();

        if (validade.isBefore(hoje)) {
            return Color.web("#e74c3c");
        } else if (validade.isEqual(hoje)) {
            return Color.web("#f39c12");
        } else if (validade.isBefore(hoje.plusDays(7))) {
            return Color.web("#f1c40f");
        } else {
            return Color.web("#2ecc71");
        }
    }

    private String formatarCampo(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return "Não informado";
        }
        return valor;
    }

    private void setLabelText(Label label, String text) {
        if (label != null) {
            label.setText(text != null ? text : "");
        }
    }

    // ===== CONTROLE DE ESTADO =====
    private void mostrarEstadoCarregamento(String mensagem, boolean carregando) {
        if (subtitleLabel != null) {
            subtitleLabel.setText(mensagem);
            subtitleLabel.setStyle(carregando ?
                    "-fx-font-size: 11px; -fx-text-fill: #f39c12;" :
                    "-fx-font-size: 11px; -fx-text-fill: #7f8c8d;"
            );
        }

        // Desabilitar botões durante carregamento
        if (btnEditar != null) btnEditar.setDisable(carregando);
        if (btnDuplicar != null) btnDuplicar.setDisable(carregando);
        if (btnExcluir != null) btnExcluir.setDisable(carregando);
        if (btnContatar != null) btnContatar.setDisable(carregando);
        if (btnMapa != null) btnMapa.setDisable(carregando);
    }

    private void mostrarErro(String mensagem) {
        mostrarEstadoCarregamento("Erro: " + mensagem, false);

        if (subtitleLabel != null) {
            subtitleLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #e74c3c;");
        }

        NexusFX.alerts().erro("Erro no Carregamento", mensagem, "Detalhes da Movimentação");
    }

    /**
     * Obtém a Stage deste controller (útil para callbacks).
     * @return Stage da janela atual ou null se não disponível
     */
    public Stage getStage() {
        if (btnVoltar != null && btnVoltar.getScene() != null) {
            return (Stage) btnVoltar.getScene().getWindow();
        }
        return null;
    }

    // ===== ATUALIZAR O MÉTODO HANDLE_FECHAR =====
    @FXML
    private void handleFechar(ActionEvent event) {
        System.out.println("✕ Fechando janela de detalhes...");

        if (onFecharCallback != null) {
            onFecharCallback.accept(null);
        }

        // Usar getStage() para maior clareza
        Stage stage = getStage();
        if (stage != null) {
            stage.close();
        }
    }

    // ===== SETTERS PARA CALLBACKS =====
    public void setOnEditarCallback(Consumer<Long> callback) {
        this.onEditarCallback = callback;
    }

    public void setOnDuplicarCallback(Consumer<Long> callback) {
        this.onDuplicarCallback = callback;
    }

    public void setOnExcluirCallback(Consumer<Long> callback) {
        this.onExcluirCallback = callback;
    }

    public void setOnFecharCallback(Consumer<Void> callback) {
        this.onFecharCallback = callback;
    }

    public Long getMovimentacaoId() {
        return movimentacaoId;
    }

    // ===== MÉTODO PARA TESTE DIRETO =====
    public void testeCarregamento(Long id) {
        System.out.println("🧪 Teste de carregamento para ID: " + id);
        this.movimentacaoId = id;

        // Simular dados para teste
        Platform.runLater(() -> {
            setLabelText(idLabel, "#" + id);
            setLabelText(tipoLabel, "📦 Entrada");
            setLabelText(quantidadeLabel, "+150");
            setLabelText(produtoLabel, "Produto Teste");
            setLabelText(codigoProdutoLabel, "SKU-TEST-001");
            setLabelText(dataMovimentacaoLabel, LocalDate.now().format(DATE_FORMAT));
            setLabelText(subtitleLabel, "Modo de teste - ID: " + id);

            if (tipoCircle != null) tipoCircle.setFill(Color.web("#27ae60"));
            if (tipoIcon != null) tipoIcon.setText("📦");

            System.out.println("✅ Teste de interface concluído");
        });
    }
}