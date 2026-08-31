package com.ossobo.gestaoDepIt.ui.splash;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 🎯 SplashScreenController - Controller do FXML da Splash Screen
 *
 * ⚠️ NOTA IMPORTANTE: Este Controller NÃO é gerenciado pelo WinterFX.
 * A Splash Screen é uma tela transitória que deve carregar ANTES do framework.
 * Por isso usamos fx:controller no FXML e não @Controller.
 *
 * Após a inicialização do WinterFX, a Splash é fechada e o LoginController
 * (gerenciado pelo WinterFX) assume o controle.
 *
 * @version 3.1
 */
public class SplashScreenController {
    private static final Logger LOGGER = LoggerFactory.getLogger(SplashScreenController.class);

    private static final boolean DEV_MODE = true; // Mude para false em produção

    @FXML private ProgressBar progressBar;
    @FXML private Label statusLabel;
    @FXML private Label devModeLabel;

    /**
     * Inicialização automática pelo FXMLLoader
     */
    @FXML
    public void initialize() {
        LOGGER.info("✅ SplashScreen Controller inicializado");

        // Configura modo DEV se ativo
        if (DEV_MODE && devModeLabel != null) {
            devModeLabel.setVisible(true);
            devModeLabel.setManaged(true);
            LOGGER.info("🔧 Modo DEV ativado na Splash Screen");
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

    /**
     * Atualiza a barra de progresso (0.0 a 1.0)
     */
    public void updateProgress(double progress) {
        Platform.runLater(() -> {
            if (progressBar != null) {
                double clamped = Math.max(0.0, Math.min(1.0, progress));
                progressBar.setProgress(clamped);
                LOGGER.debug("📊 Progresso atualizado: {}%", (int)(clamped * 100));
            }
        });
    }

    /**
     * Atualiza o texto de status
     */
    public void updateStatus(String status) {
        Platform.runLater(() -> {
            if (statusLabel != null) {
                statusLabel.setText(status);
                statusLabel.setTextFill(Color.web("#bdc3c7"));
                LOGGER.debug("📌 Status atualizado: {}", status);
            }
        });
    }

    /**
     * Atualiza progresso E status simultaneamente
     */
    public void update(double progress, String status) {
        Platform.runLater(() -> {
            updateProgress(progress);
            updateStatus(status);
        });
    }

    /**
     * Exibe mensagem de erro na splash
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
            LOGGER.error("❌ Erro na Splash: {}", errorMessage);
        });
    }

    /**
     * Define a splash como concluída (progresso 100%)
     */
    public void complete() {
        Platform.runLater(() -> {
            updateProgress(1.0);
            if (statusLabel != null) {
                statusLabel.setText("✅ Sistema pronto!");
                statusLabel.setTextFill(Color.web("#27ae60"));
            }
            LOGGER.info("✅ Splash concluída com sucesso");
        });
    }

    /**
     * Obtém a barra de progresso (para uso externo)
     */
    public ProgressBar getProgressBar() {
        return progressBar;
    }

    /**
     * Obtém o label de status (para uso externo)
     */
    public Label getStatusLabel() {
        return statusLabel;
    }
}