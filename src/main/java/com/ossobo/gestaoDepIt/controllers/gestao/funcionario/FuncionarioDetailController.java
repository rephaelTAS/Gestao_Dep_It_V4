/*
 * FuncionarioDetailController v1.3
 *
 * Controlador de Detalhes do Funcionário.
 *
 * v1.3: CORRIGIDO — forcarRenderizacao no initialize + retry no setFuncionario
 * v1.2: Singleton reutilizado — verificação null nos @FXML
 * v1.1: Sistema de pendência
 * v1.0: Refatoração completa
 */
package com.ossobo.gestaoDepIt.controllers.gestao.funcionario;

import com.ossobo.gestaoDepIt.config.ViewConstant;
import com.ossobo.gestaoDepIt.db.models.Funcionarios;
import com.ossobo.gestaoDepIt.db.services.FuncionariosService;
import com.ossobo.nexusfx.NexusFX;
import com.ossobo.nexusfx.di.annotations.Controller;
import com.ossobo.nexusfx.di.annotations.Inject;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.time.format.DateTimeFormatter;

@Controller
public class FuncionarioDetailController {

    private static final Logger logger = LoggerFactory.getLogger(FuncionarioDetailController.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Inject
    private FuncionariosService funcionariosService;

    @FXML private Label detailCodDep;
    @FXML private Label detailNome;
    @FXML private Label detailFuncao;
    @FXML private Label detailDepartamento;
    @FXML private Label detailLocalTrabalho;
    @FXML private Label detailEmail;
    @FXML private Label detailTelefone;
    @FXML private Label detailStatus;
    @FXML private Label detailCriadoEm;
    @FXML private Label detailAtualizadoEm;
    @FXML private Label detailInfoImagem;
    @FXML private Label statEquipamentos;
    @FXML private Label statTonerAtivo;
    @FXML private Label statUltimaVerificacao;
    @FXML private Label tituloLabel;
    @FXML private Label mensagemStatusLabel;
    @FXML private ImageView detailImagemPerfil;
    @FXML private Button desativarButton;
    @FXML private Button editarButton;
    @FXML private Button voltarButton;

    private Funcionarios funcionario;

    // =========================================================================
    // CICLO DE VIDA
    // =========================================================================

    @FXML
    public void initialize() {
        logger.info(">>> Detail.initialize() INICIADO");

        if (mensagemStatusLabel != null) {
            mensagemStatusLabel.setManaged(false);
            mensagemStatusLabel.setVisible(false);
        }
        configurarTooltips();

        logger.info(">>> Detail.initialize() CONCLUÍDO — detailNome={}", detailNome != null);

        Platform.runLater(this::forcarRenderizacao);
    }

    private void configurarTooltips() {
        if (detailImagemPerfil != null)
            Tooltip.install(detailImagemPerfil, new Tooltip("Clique para alterar a foto"));
        if (desativarButton != null)
            Tooltip.install(desativarButton, new Tooltip("Ativar/Desativar funcionário"));
        if (editarButton != null)
            Tooltip.install(editarButton, new Tooltip("Editar informações do funcionário"));
    }

    // =========================================================================
    // INJEÇÃO DE DADOS
    // =========================================================================

    public void setFuncionario(Funcionarios funcionario) {
        logger.info(">>> Detail.setFuncionario({}) — detailNome={}",
                funcionario != null ? funcionario.getCodDep() : "null",
                detailNome != null);

        this.funcionario = funcionario;

        if (funcionario != null && detailNome != null) {
            renderizarDados();
        } else if (funcionario != null) {
            logger.warn(">>> Detail: FXML não pronto, agendando retry em 100ms");
            Platform.runLater(() -> {
                try { Thread.sleep(100); } catch (InterruptedException ignored) {}
                if (this.funcionario != null && detailNome != null) {
                    logger.info(">>> Detail: Retry — renderizando agora");
                    renderizarDados();
                }
            });
        }
    }

    public void setFuncionarioId(String codDep) {
        new Thread(() -> {
            try {
                var opt = funcionariosService.buscarPorCodDep(codDep);
                Platform.runLater(() -> {
                    opt.ifPresentOrElse(
                            this::setFuncionario,
                            () -> mostrarMensagemStatus("Funcionário não encontrado: " + codDep, "error")
                    );
                });
            } catch (Exception e) {
                logger.error("Erro ao carregar funcionário: {}", codDep, e);
                Platform.runLater(() -> mostrarMensagemStatus("Erro ao carregar dados", "error"));
            }
        }).start();
    }

    public void forcarRenderizacao() {
        logger.info(">>> Detail.forcarRenderizacao() — funcionario={}, detailNome={}",
                funcionario != null ? funcionario.getCodDep() : "null",
                detailNome != null);

        if (funcionario != null && detailNome != null) {
            renderizarDados();
        }
    }

    // =========================================================================
    // RENDERIZAÇÃO
    // =========================================================================

    private void renderizarDados() {
        if (funcionario == null || detailNome == null) return;

        logger.info(">>> Detail.renderizarDados() — {}", funcionario.getNome());

        try {
            tituloLabel.setText("Detalhes: " + funcionario.getNome());

            detailCodDep.setText(funcionario.getCodDep());
            detailNome.setText(funcionario.getNome());
            detailFuncao.setText(orDash(funcionario.getFuncao()));
            detailDepartamento.setText(orDash(funcionario.getDepartamento()));
            detailLocalTrabalho.setText(orDash(funcionario.getLocalTrabalho()));
            detailEmail.setText(orDash(funcionario.getEmail()));
            detailTelefone.setText(orDash(funcionario.getTelefone()));

            detailCriadoEm.setText(funcionario.getCreatedAt() != null
                    ? funcionario.getCreatedAt().format(DATE_FORMATTER) : "-");
            detailAtualizadoEm.setText(funcionario.getUpdatedAt() != null
                    ? funcionario.getUpdatedAt().format(DATE_FORMATTER) : "-");

            atualizarStatusLabel(funcionario.getAtivo() != null && funcionario.getAtivo());
            renderizarImagemPerfil();

            statEquipamentos.setText("—");
            statTonerAtivo.setText("—");
            statUltimaVerificacao.setText("—");

            logger.info(">>> Detail.renderizarDados() — CONCLUÍDO");

        } catch (Exception e) {
            logger.error("Erro ao renderizar dados do funcionário", e);
            mostrarMensagemStatus("Erro ao exibir dados", "error");
        }
    }

    private String orDash(String value) {
        return value != null && !value.isEmpty() ? value : "-";
    }

    private void renderizarImagemPerfil() {
        if (detailImagemPerfil == null) return;

        if (funcionario.temImagemPerfil()) {
            try {
                Image image = new Image(new ByteArrayInputStream(funcionario.getImagemPerfil()));
                detailImagemPerfil.setImage(image);
                detailInfoImagem.setText(funcionario.getTamanhoImagemFormatado());
                detailInfoImagem.setStyle("-fx-text-fill: #27ae60;");
            } catch (Exception e) {
                logger.warn("Erro ao carregar imagem de perfil para {}", funcionario.getCodDep());
                detailInfoImagem.setText("Erro ao carregar");
                detailInfoImagem.setStyle("-fx-text-fill: #e74c3c;");
            }
        } else {
            detailImagemPerfil.setImage(null);
            detailInfoImagem.setText("Sem imagem");
            detailInfoImagem.setStyle("-fx-text-fill: #7f8c8d;");
        }
    }

    private void atualizarStatusLabel(boolean ativo) {
        if (detailStatus == null || desativarButton == null) return;

        if (ativo) {
            detailStatus.setText("ATIVO");
            detailStatus.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            desativarButton.setText("Desativar");
            desativarButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        } else {
            detailStatus.setText("INATIVO");
            detailStatus.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            desativarButton.setText("Ativar");
            desativarButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
        }
    }

    // =========================================================================
    // HANDLERS
    // =========================================================================

    @FXML
    private void handleVoltar(ActionEvent event) {
        try {
            Node source = (Node) event.getSource();
            Stage stage = (Stage) source.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            logger.error("Erro ao fechar detalhes", e);
        }
    }

    @FXML
    private void handleEditar(ActionEvent event) {
        if (funcionario == null) {
            mostrarMensagemStatus("Nenhum funcionário carregado", "error");
            return;
        }

        try {
            Window owner = ((Node) event.getSource()).getScene().getWindow();
            Stage detailStage = (Stage) owner;
            Window parentWindow = detailStage.getOwner();
            Window modalOwner = parentWindow != null ? parentWindow : NexusFX.stage();

            detailStage.close();

            NexusFX.dialogs().openModalWithController(
                    ViewConstant.Funcionario.ADDEDITARFUNCIONARIO,
                    "Editar Funcionário",
                    modalOwner,
                    (Object controller) -> {
                        if (controller instanceof FuncionarioFormController form) {
                            form.setFuncionario(funcionario);
                        }
                    }
            );

        } catch (Exception e) {
            logger.error("Erro ao abrir edição a partir dos detalhes", e);
            mostrarMensagemStatus("Erro ao abrir editor: " + e.getMessage(), "error");
        }
    }

    @FXML
    private void handleDesativarAtivar(ActionEvent event) {
        if (funcionario == null) return;

        boolean ativoAtual = funcionario.getAtivo() != null && funcionario.getAtivo();
        String acao = ativoAtual ? "desativar" : "ativar";
        String titulo = ativoAtual ? "Desativar Funcionário" : "Ativar Funcionário";
        String mensagem = String.format("Deseja realmente %s %s?", acao, funcionario.getNome());

        NexusFX.alerts().confirmarPerigo(
                mensagem,
                "Esta ação pode afetar o acesso do funcionário ao sistema.",
                titulo,
                confirmado -> {
                    if (confirmado) {
                        executarAlternanciaStatus(!ativoAtual);
                    }
                }
        );
    }

    private void executarAlternanciaStatus(boolean ativar) {
        new Thread(() -> {
            try {
                if (ativar) {
                    funcionariosService.ativarFuncionario(funcionario.getCodDep());
                } else {
                    funcionariosService.desativarFuncionario(funcionario.getCodDep());
                }

                Platform.runLater(() -> {
                    funcionario.setAtivo(ativar);
                    atualizarStatusLabel(ativar);
                    mostrarMensagemStatus(
                            "Funcionário " + (ativar ? "ativado" : "desativado") + " com sucesso!",
                            "success"
                    );
                });

            } catch (Exception e) {
                logger.error("Erro ao alterar status do funcionário", e);
                Platform.runLater(() -> mostrarMensagemStatus("Erro: " + e.getMessage(), "error"));
            }
        }).start();
    }

    @FXML
    private void handleAlterarFoto() {
        mostrarMensagemStatus("Funcionalidade em desenvolvimento", "info");
    }

    @FXML
    private void handleVerEquipamentos() {
        if (funcionario == null) return;
        mostrarMensagemStatus("Funcionalidade em desenvolvimento", "info");
    }

    @FXML
    private void handleTransferirDepartamento() {
        mostrarMensagemStatus("Funcionalidade em desenvolvimento", "info");
    }

    @FXML
    private void handleVerHistorico() {
        if (funcionario == null) return;
        mostrarMensagemStatus("Funcionalidade em desenvolvimento", "info");
    }

    // =========================================================================
    // UTILITÁRIOS
    // =========================================================================

    private void mostrarMensagemStatus(String mensagem, String tipo) {
        Platform.runLater(() -> {
            if (mensagemStatusLabel == null) return;

            mensagemStatusLabel.setText(mensagem);
            mensagemStatusLabel.setManaged(true);
            mensagemStatusLabel.setVisible(true);

            switch (tipo.toLowerCase()) {
                case "success" -> mensagemStatusLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 12px;");
                case "error" -> mensagemStatusLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px;");
                default -> mensagemStatusLabel.setStyle("-fx-text-fill: #3498db; -fx-font-size: 12px;");
            }

            new Thread(() -> {
                try {
                    Thread.sleep(3000);
                    Platform.runLater(() -> {
                        if (mensagemStatusLabel != null) {
                            mensagemStatusLabel.setManaged(false);
                            mensagemStatusLabel.setVisible(false);
                        }
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        });
    }

    public Funcionarios getFuncionario() {
        return funcionario;
    }
}