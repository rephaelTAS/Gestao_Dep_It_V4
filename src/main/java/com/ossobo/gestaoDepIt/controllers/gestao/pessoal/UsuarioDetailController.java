/*
 * UsuarioDetailController v2.1
 *
 * Controlador de Detalhes do Usuário.
 * Exibe dados completos, segurança e ações administrativas.
 *
 * v2.1: Atualizado para BCrypt
 *       - Removido hashSenha() — senha texto puro para o Service
 *       - alterarSenhaComValidacao e alterarSenha recebem texto puro
 * v2.0: Refatoração completa — código limpo e organizado
 */
package com.ossobo.gestaoDepIt.controllers.gestao.pessoal;

import com.ossobo.gestaoDepIt.db.models.Usuario;
import com.ossobo.gestaoDepIt.db.services.UsuariosService;
import com.ossobo.nexusfx.NexusFX;
import com.ossobo.nexusfx.di.annotations.Controller;
import com.ossobo.nexusfx.di.annotations.Inject;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;

@Controller
public class UsuarioDetailController {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioDetailController.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Inject
    private UsuariosService usuariosService;

    // =========================================================================
    // COMPONENTES FXML — DADOS DO USUÁRIO
    // =========================================================================
    @FXML private Label detailId, detailNome, detailEmail, detailFuncionario;
    @FXML private Label detailNivelAcesso, detailStatus;
    @FXML private Label detailCriadoEm, detailAtualizadoEm;
    @FXML private Label detailUltimoLogin, detailIpLogin, detailSessaoAtiva, detailExpiraSessao;
    @FXML private Label tituloLabel, subtituloLabel, mensagemStatusLabel;
    @FXML private Label statEventos, statMovimentacoes, statLogin30dias;
    @FXML private Button voltarButton, desativarButton;

    // =========================================================================
    // COMPONENTES FXML — PAINEL ALTERAR SENHA
    // =========================================================================
    @FXML private Button alterarSenhaButton;
    @FXML private VBox alterarSenhaPanel;
    @FXML private PasswordField alterarSenhaAtualField, alterarNovaSenhaField, alterarConfirmarSenhaField;
    @FXML private Label alterarSenhaErroLabel;

    // =========================================================================
    // COMPONENTES FXML — PAINEL REDEFINIR SENHA
    // =========================================================================
    @FXML private Button redefinirSenhaButton;
    @FXML private VBox redefinirSenhaPanel;
    @FXML private PasswordField redefinirNovaSenhaField, redefinirConfirmarSenhaField;
    @FXML private Label redefinirSenhaErroLabel;

    // =========================================================================
    // ESTADO
    // =========================================================================
    private Usuario usuario;
    private boolean inicializado = false;

    // =========================================================================
    // CICLO DE VIDA
    // =========================================================================

    @FXML
    public void initialize() {
        logger.info(">>> UsuarioDetail.initialize()");
        ocultarMensagemStatus();
        esconderPainelAlterarSenha();
        esconderPainelRedefinirSenha();
        inicializado = true;
        if (usuario != null) renderizarDados();
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
        if (inicializado && usuario != null) renderizarDados();
    }

    // =========================================================================
    // RENDERIZAÇÃO DE DADOS
    // =========================================================================

    private void renderizarDados() {
        if (usuario == null || detailNome == null) return;

        tituloLabel.setText("Detalhes: " + usuario.getNome());
        subtituloLabel.setText("Email: " + nvl(usuario.getEmail()));

        detailId.setText(String.valueOf(usuario.getId()));
        detailNome.setText(usuario.getNome());
        detailEmail.setText(nvl(usuario.getEmail()));
        detailFuncionario.setText(nvl(usuario.getFuncionarioId()));
        detailNivelAcesso.setText(nvl(usuario.getNivelAcesso()));

        boolean ativo = usuario.getAtivo() != null && usuario.getAtivo();
        detailStatus.setText(ativo ? "ATIVO" : "INATIVO");
        detailStatus.setStyle(ativo ? "-fx-text-fill: #27ae60; -fx-font-weight: bold;"
                : "-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        desativarButton.setText(ativo ? "Desativar" : "Ativar");

        detailCriadoEm.setText(formatDate(usuario.getCreatedAt()));
        detailAtualizadoEm.setText(formatDate(usuario.getUpdatedAt()));
        detailUltimoLogin.setText(usuario.getUltimoLogin() != null ? usuario.getUltimoLogin().format(DATE_FORMATTER) : "Nunca");
        detailIpLogin.setText(nvl(usuario.getIpUltimoLogin()));

        boolean temSessao = usuario.getSessaoAtual() != null && !usuario.getSessaoAtual().isEmpty();
        detailSessaoAtiva.setText(temSessao ? "Ativa" : "Inativa");
        detailSessaoAtiva.setStyle(temSessao ? "-fx-text-fill: #27ae60; -fx-font-weight: bold;"
                : "-fx-text-fill: #95a5a6;");
        detailExpiraSessao.setText(formatDate(usuario.getExpiracaoSessao()));

        statEventos.setText("Eventos Registrados: —");
        statMovimentacoes.setText("Movimentações: —");
        statLogin30dias.setText("Logins (30 dias): —");
    }

    private String nvl(String value) { return value != null && !value.isEmpty() ? value : "-"; }

    private String formatDate(java.time.LocalDateTime date) { return date != null ? date.format(DATE_FORMATTER) : "-"; }

    // =========================================================================
    // HANDLERS — AÇÕES PRINCIPAIS
    // =========================================================================

    @FXML
    private void handleVoltar(ActionEvent event) {
        try { ((Stage) ((Node) event.getSource()).getScene().getWindow()).close(); }
        catch (Exception e) { logger.error("Erro ao fechar detalhes", e); }
    }

    @FXML
    private void handleDesativarAtivar(ActionEvent event) {
        if (usuario == null) return;
        boolean ativoAtual = usuario.getAtivo() != null && usuario.getAtivo();
        String acao = ativoAtual ? "desativar" : "ativar";

        NexusFX.alerts().confirmarPerigo(
                String.format("Deseja %s o usuário %s?", acao, usuario.getNome()),
                ativoAtual ? "A sessão será invalidada." : "",
                (ativoAtual ? "Desativar" : "Ativar") + " Usuário",
                confirmado -> { if (confirmado) executarAlternanciaStatus(!ativoAtual); }
        );
    }

    private void executarAlternanciaStatus(boolean ativar) {
        new Thread(() -> {
            try {
                if (ativar) usuariosService.ativarUsuario(usuario.getId());
                else usuariosService.desativarUsuario(usuario.getId());
                Platform.runLater(() -> {
                    usuario.setAtivo(ativar);
                    renderizarDados();
                    mostrarMensagemStatus("Usuário " + (ativar ? "ativado" : "desativado") + "!", "success");
                });
            } catch (Exception e) {
                logger.error("Erro ao alterar status", e);
                Platform.runLater(() -> mostrarMensagemStatus("Erro: " + e.getMessage(), "error"));
            }
        }).start();
    }

    @FXML
    private void handleForcarLogout() {
        if (usuario == null) return;
        NexusFX.alerts().confirmarPerigo(
                "Forçar logout de " + usuario.getNome() + "?",
                "A sessão atual será invalidada.",
                "Forçar Logout",
                confirmado -> { if (confirmado) executarForcarLogout(); }
        );
    }

    private void executarForcarLogout() {
        new Thread(() -> {
            try {
                usuariosService.forcarLogoutUsuario(usuario.getId());
                Platform.runLater(() -> {
                    usuario.setSessaoAtual(null);
                    usuario.setExpiracaoSessao(null);
                    renderizarDados();
                    mostrarMensagemStatus("Logout forçado com sucesso!", "success");
                });
            } catch (Exception e) {
                logger.error("Erro ao forçar logout", e);
                Platform.runLater(() -> mostrarMensagemStatus("Erro: " + e.getMessage(), "error"));
            }
        }).start();
    }

    @FXML private void handleAuditarUsuario() { mostrarMensagemStatus("Em desenvolvimento", "info"); }
    @FXML private void handleAlterarNivelAcesso() { mostrarMensagemStatus("Em desenvolvimento", "info"); }

    // =========================================================================
    // PAINEL: ALTERAR SENHA (usuário sabe a senha atual) ✅ BCrypt
    // =========================================================================

    @FXML
    private void handleMostrarAlterarSenha() {
        if (usuario == null) return;
        esconderPainelRedefinirSenha();
        togglePainel(alterarSenhaPanel, () -> {
            alterarSenhaAtualField.clear();
            alterarNovaSenhaField.clear();
            alterarConfirmarSenhaField.clear();
            ocultarErro(alterarSenhaErroLabel);
        });
    }

    @FXML private void handleCancelarAlterarSenha() { esconderPainelAlterarSenha(); }

    @FXML
    private void handleSalvarAlterarSenha() {
        if (usuario == null) return;

        String senhaAtual = alterarSenhaAtualField.getText();
        String novaSenha = alterarNovaSenhaField.getText();
        String confirmarSenha = alterarConfirmarSenhaField.getText();

        if (senhaAtual.isEmpty()) { mostrarErro(alterarSenhaErroLabel, "Senha atual é obrigatória"); return; }
        if (!validarNovaSenha(novaSenha, confirmarSenha, alterarSenhaErroLabel)) return;

        new Thread(() -> {
            try {
                // ✅ Texto puro — Service usa BCrypt
                usuariosService.alterarSenhaComValidacao(usuario.getId(), senhaAtual, novaSenha);
                Platform.runLater(() -> {
                    esconderPainelAlterarSenha();
                    mostrarMensagemStatus("Senha alterada com sucesso!", "success");
                });
            } catch (IllegalArgumentException e) {
                Platform.runLater(() -> mostrarErro(alterarSenhaErroLabel, e.getMessage()));
            } catch (Exception e) {
                logger.error("Erro ao alterar senha", e);
                Platform.runLater(() -> mostrarErro(alterarSenhaErroLabel, "Erro: " + e.getMessage()));
            }
        }).start();
    }

    private void esconderPainelAlterarSenha() {
        alterarSenhaPanel.setVisible(false);
        alterarSenhaPanel.setManaged(false);
        if (alterarSenhaAtualField != null) alterarSenhaAtualField.clear();
        if (alterarNovaSenhaField != null) alterarNovaSenhaField.clear();
        if (alterarConfirmarSenhaField != null) alterarConfirmarSenhaField.clear();
        if (alterarSenhaErroLabel != null) ocultarErro(alterarSenhaErroLabel);
    }

    // =========================================================================
    // PAINEL: REDEFINIR SENHA (admin, sem senha atual) ✅ BCrypt
    // =========================================================================

    @FXML
    private void handleMostrarRedefinirSenha() {
        if (usuario == null) return;
        esconderPainelAlterarSenha();
        togglePainel(redefinirSenhaPanel, () -> {
            redefinirNovaSenhaField.clear();
            redefinirConfirmarSenhaField.clear();
            ocultarErro(redefinirSenhaErroLabel);
        });
    }

    @FXML private void handleCancelarRedefinirSenha() { esconderPainelRedefinirSenha(); }

    @FXML
    private void handleSalvarRedefinirSenha() {
        if (usuario == null) return;

        String novaSenha = redefinirNovaSenhaField.getText();
        String confirmarSenha = redefinirConfirmarSenhaField.getText();

        if (!validarNovaSenha(novaSenha, confirmarSenha, redefinirSenhaErroLabel)) return;

        new Thread(() -> {
            try {
                // ✅ Texto puro — Service usa BCrypt
                usuariosService.alterarSenha(usuario.getId(), novaSenha);
                usuariosService.forcarLogoutUsuario(usuario.getId());
                Platform.runLater(() -> {
                    usuario.setSessaoAtual(null);
                    usuario.setExpiracaoSessao(null);
                    esconderPainelRedefinirSenha();
                    renderizarDados();
                    mostrarMensagemStatus("Senha redefinida! Sessão invalidada.", "success");
                });
            } catch (Exception e) {
                logger.error("Erro ao redefinir senha", e);
                Platform.runLater(() -> mostrarErro(redefinirSenhaErroLabel, "Erro: " + e.getMessage()));
            }
        }).start();
    }

    private void esconderPainelRedefinirSenha() {
        redefinirSenhaPanel.setVisible(false);
        redefinirSenhaPanel.setManaged(false);
        if (redefinirNovaSenhaField != null) redefinirNovaSenhaField.clear();
        if (redefinirConfirmarSenhaField != null) redefinirConfirmarSenhaField.clear();
        if (redefinirSenhaErroLabel != null) ocultarErro(redefinirSenhaErroLabel);
    }

    // =========================================================================
    // UTILITÁRIOS DE PAINEL
    // =========================================================================

    private void togglePainel(VBox painel, Runnable onOpen) {
        boolean visivel = painel.isVisible();
        painel.setVisible(!visivel);
        painel.setManaged(!visivel);
        if (!visivel && onOpen != null) onOpen.run();
    }

    private boolean validarNovaSenha(String novaSenha, String confirmarSenha, Label erroLabel) {
        if (novaSenha.isEmpty()) { mostrarErro(erroLabel, "Nova senha é obrigatória"); return false; }
        if (novaSenha.length() < 6) { mostrarErro(erroLabel, "Mínimo 6 caracteres"); return false; }
        if (!novaSenha.equals(confirmarSenha)) { mostrarErro(erroLabel, "Senhas não conferem"); return false; }
        return true;
    }

    // =========================================================================
    // UTILITÁRIOS DE UI
    // =========================================================================

    private void mostrarErro(Label label, String mensagem) {
        label.setText(mensagem);
        label.setVisible(true);
        label.setManaged(true);
    }

    private void ocultarErro(Label label) {
        label.setVisible(false);
        label.setManaged(false);
    }

    private void mostrarMensagemStatus(String mensagem, String tipo) {
        Platform.runLater(() -> {
            if (mensagemStatusLabel == null) return;
            mensagemStatusLabel.setText(mensagem);
            mensagemStatusLabel.setManaged(true);
            mensagemStatusLabel.setVisible(true);
            mensagemStatusLabel.setStyle(switch (tipo) {
                case "success" -> "-fx-text-fill: #27ae60; -fx-font-size: 12px;";
                case "error" -> "-fx-text-fill: #e74c3c; -fx-font-size: 12px;";
                default -> "-fx-text-fill: #3498db; -fx-font-size: 12px;";
            });
            new Thread(() -> {
                try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
                Platform.runLater(this::ocultarMensagemStatus);
            }).start();
        });
    }

    private void ocultarMensagemStatus() {
        if (mensagemStatusLabel != null) {
            mensagemStatusLabel.setManaged(false);
            mensagemStatusLabel.setVisible(false);
        }
    }
}