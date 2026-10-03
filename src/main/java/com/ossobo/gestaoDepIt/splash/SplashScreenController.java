/*
 * SplashScreenController v3.2
 *
 * Controller do FXML da Splash Screen.
 *
 * NOTA IMPORTANTE: Este Controller NÃO é gerenciado pelo WinterFX.
 * A Splash Screen é uma tela transitória que deve carregar ANTES do framework.
 * Por isso usamos fx:controller no FXML e não @Controller.
 *
 * v3.2: Migrado para System.Logger (padrão do projeto)
 *       Organização com separadores e Javadoc
 *       Removido SLF4J
 * v3.1: Correção de modo DEV
 * v3.0: Versão inicial
 */
package com.ossobo.gestaoDepIt.splash;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.paint.Color;

import java.lang.System.Logger.Level;

/**
 * SplashScreenController - Controller da Splash Screen.
 *
 * Responsabilidades:
 * - Gerenciar a barra de progresso
 * - Gerenciar o label de status
 * - Exibir mensagens de erro
 * - Indicar conclusão do carregamento
 *
 * NOTA: Este controller NÃO é gerenciado pelo WinterFX.
 * A Splash Screen deve carregar ANTES do framework.
 */
public class SplashScreenController {

    // ============================================================
    // LOGGER (padrão do projeto)
    // ============================================================

    private static final System.Logger LOGGER = System.getLogger(SplashScreenController.class.getName());

    // ============================================================
    // CONSTANTES
    // ============================================================

    private static final boolean DEV_MODE = true;

    // ============================================================
    // @FXML INJECTIONS
    // ============================================================

    @FXML private ProgressBar progressBar;
    @FXML private Label statusLabel;
    @FXML private Label devModeLabel;

    // ============================================================
    // CICLO DE VIDA
    // ============================================================

    /**
     * Inicialização automática pelo FXMLLoader.
     */
    @FXML
    public void initialize() {
        LOGGER.log(Level.INFO, "SplashScreen Controller inicializado");

        // Configura modo DEV se ativo
        if (DEV_MODE && devModeLabel != null) {
            devModeLabel.setVisible(true);
            devModeLabel.setManaged(true);
            LOGGER.log(Level.INFO, "Modo DEV ativado na Splash Screen");
        }

        // Estado inicial
        if (statusLabel != null) {
            statusLabel.setText("Carregando módulos...");
            statusLabel.setTextFill(Color.web("#bdc3c7"));
        }

        if (progressBar != null) {
            progressBar.setProgress(0.0);
            progressBar.setStyle("-fx-accent: #27ae60; -fx-background-color: rgba(255,255,255,0.15);");
        }
    }

    // ============================================================
    // MÉTODOS PÚBLICOS
    // ============================================================

    /**
     * Atualiza a barra de progresso (0.0 a 1.0).
     */
    public void updateProgress(double progress) {
        Platform.runLater(() -> {
            if (progressBar != null) {
                double clamped = Math.max(0.0, Math.min(1.0, progress));
                progressBar.setProgress(clamped);
                LOGGER.log(Level.DEBUG, "Progresso atualizado: {0}%", (int) (clamped * 100));
            }
        });
    }

    /**
     * Atualiza o texto de status.
     */
    public void updateStatus(String status) {
        Platform.runLater(() -> {
            if (statusLabel != null) {
                statusLabel.setText(status);
                statusLabel.setTextFill(Color.web("#bdc3c7"));
                LOGGER.log(Level.DEBUG, "Status atualizado: {0}", status);
            }
        });
    }

    /**
     * Atualiza progresso E status simultaneamente.
     */
    public void update(double progress, String status) {
        Platform.runLater(() -> {
            updateProgress(progress);
            updateStatus(status);
        });
    }

    /**
     * Exibe mensagem de erro na splash.
     */
    public void showError(String errorMessage) {
        Platform.runLater(() -> {
            if (statusLabel != null) {
                statusLabel.setText("❌ " + errorMessage);
                statusLabel.setTextFill(Color.web("#ff6b6b"));
                statusLabel.setStyle("-fx-font-weight: bold;");
            }
            if (progressBar != null) {
                progressBar.setProgress(0.0);
                progressBar.setStyle("-fx-accent: #ff6b6b; -fx-background-color: rgba(255,255,255,0.15);");
            }
            LOGGER.log(Level.ERROR, "Erro na Splash: {0}", errorMessage);
        });
    }

    /**
     * Define a splash como concluída (progresso 100%).
     */
    public void complete() {
        Platform.runLater(() -> {
            updateProgress(1.0);
            if (statusLabel != null) {
                statusLabel.setText("✅ Sistema pronto!");
                statusLabel.setTextFill(Color.web("#27ae60"));
            }
            LOGGER.log(Level.INFO, "Splash concluída com sucesso");
        });
    }

    // ============================================================
    // GETTERS
    // ============================================================

    public ProgressBar getProgressBar() {
        return progressBar;
    }

    public Label getStatusLabel() {
        return statusLabel;
    }
}