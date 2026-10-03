/*
 * SplashManager v3.2
 *
 * Gerenciador único de Splash Screen.
 * Responsável por carregar, exibir e gerenciar o ciclo de vida da splash.
 *
 * v3.2: Migrado para System.Logger (padrão do projeto)
 *       Organização com separadores e Javadoc
 *       Removido SLF4J
 *       Removido @NewScene (não usado)
 * v3.1: Correção de fallback
 * v3.0: Versão inicial
 */
package com.ossobo.gestaoDepIt.splash;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.IOException;
import java.lang.System.Logger.Level;
import java.net.URL;

/**
 * SplashManager - Gerenciador único da Splash Screen.
 *
 * Responsabilidades:
 * - Carregar o FXML da Splash Screen
 * - Gerenciar o ciclo de vida da Splash
 * - Fornecer API para atualização de progresso
 * - Transição suave para a aplicação principal
 *
 * IMPORTANTE: A Splash NÃO é gerenciada pelo WinterFX.
 * Ela é carregada antes do framework e fechada após sua inicialização.
 */
public final class SplashManager {

    // ============================================================
    // LOGGER (padrão do projeto)
    // ============================================================

    private static final System.Logger LOGGER = System.getLogger(SplashManager.class.getName());

    // ============================================================
    // CONSTANTES
    // ============================================================

    private static final String FXML_PATH = "/META-INF/gestaoDepIt/fxmls/splash/SplashScreen.fxml";
    private static final double FADE_IN_DURATION_MS = 800;
    private static final double FADE_OUT_DURATION_MS = 500;

    // ============================================================
    // SINGLETON
    // ============================================================

    private static final SplashManager INSTANCE = new SplashManager();

    private SplashManager() {
        // Construtor privado (singleton)
    }

    public static SplashManager getInstance() {
        return INSTANCE;
    }

    // ============================================================
    // CAMPOS DE ESTADO
    // ============================================================

    private Stage splashStage;
    private SplashScreenController controller;
    private Runnable onComplete;
    private boolean isShowing = false;

    // ============================================================
    // MÉTODOS PÚBLICOS
    // ============================================================

    /**
     * Exibe a Splash Screen a partir do FXML.
     *
     * @param owner Stage pai (pode ser null)
     * @param onCompleteCallback Callback executado após o fade-out
     */
    public void show(Stage owner, Runnable onCompleteCallback) {
        if (isShowing) {
            LOGGER.log(Level.WARNING, "Splash já está sendo exibida");
            return;
        }

        this.onComplete = onCompleteCallback;
        isShowing = true;

        try {
            LOGGER.log(Level.INFO, "Carregando Splash Screen do FXML...");

            URL fxmlLocation = SplashManager.class.getResource(FXML_PATH);

            if (fxmlLocation == null) {
                LOGGER.log(Level.ERROR, "FXML não encontrado em: {0}", FXML_PATH);
                criarFallbackEmergencial(owner);
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            controller = loader.getController();
            if (controller == null) {
                LOGGER.log(Level.WARNING, "Controller não encontrado no FXML");
                controller = new SplashScreenController();
            }

            LOGGER.log(Level.INFO, "Splash Screen carregada com sucesso");

            configurarStage(owner, root);
            controller.updateStatus("Inicializando sistema...");
            controller.updateProgress(0.0);

        } catch (IOException e) {
            LOGGER.log(Level.ERROR, "Erro ao carregar FXML da Splash", e);
            criarFallbackEmergencial(owner);
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, "Erro inesperado ao carregar Splash", e);
            criarFallbackEmergencial(owner);
        }
    }

    /**
     * Atualiza o progresso da splash.
     *
     * @param progress Valor entre 0.0 e 1.0
     * @param message Mensagem de status
     */
    public void updateProgress(double progress, String message) {
        if (controller != null) {
            controller.update(progress, message);
        } else {
            LOGGER.log(Level.WARNING, "Tentativa de atualizar splash sem controller");
        }
    }

    /**
     * Atualiza apenas o progresso.
     */
    public void updateProgress(double progress) {
        if (controller != null) {
            controller.updateProgress(progress);
        }
    }

    /**
     * Atualiza apenas o status.
     */
    public void updateStatus(String message) {
        if (controller != null) {
            controller.updateStatus(message);
        }
    }

    /**
     * Fecha a splash com animação de fade-out.
     */
    public void hide() {
        if (!isShowing || splashStage == null) {
            return;
        }

        Platform.runLater(() -> {
            try {
                if (controller != null) {
                    controller.complete();
                }

                Parent root = splashStage.getScene().getRoot();
                FadeTransition fadeOut = new FadeTransition(Duration.millis(FADE_OUT_DURATION_MS), root);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);
                fadeOut.setOnFinished(event -> {
                    splashStage.close();
                    isShowing = false;
                    if (onComplete != null) {
                        onComplete.run();
                    }
                    LOGGER.log(Level.INFO, "Splash Screen fechada");
                });
                fadeOut.play();

            } catch (Exception e) {
                splashStage.close();
                isShowing = false;
                if (onComplete != null) {
                    onComplete.run();
                }
                LOGGER.log(Level.WARNING, "Erro na animação de fade-out: {0}", e.getMessage());
            }
        });
    }

    /**
     * Fecha a splash imediatamente (sem animação).
     */
    public void closeImmediately() {
        if (splashStage != null) {
            Platform.runLater(() -> {
                splashStage.close();
                isShowing = false;
            });
        }
    }

    /**
     * Exibe mensagem de erro na splash.
     */
    public void showError(String message) {
        if (controller != null) {
            controller.showError(message);
        }
    }

    /**
     * Verifica se a splash está sendo exibida.
     */
    public boolean isShowing() {
        return isShowing;
    }

    /**
     * Obtém o controller da splash.
     */
    public SplashScreenController getController() {
        return controller;
    }

    // ============================================================
    // MÉTODOS PRIVADOS
    // ============================================================

    /**
     * Configura o Stage da Splash.
     */
    private void configurarStage(Stage owner, Parent root) {
        splashStage = new Stage();
        splashStage.initStyle(StageStyle.UNDECORATED);
        splashStage.initOwner(owner);
        splashStage.setAlwaysOnTop(true);

        Scene scene = new Scene(root);
        splashStage.setScene(scene);
        splashStage.centerOnScreen();
        splashStage.show();

        // Fade in
        FadeTransition fadeIn = new FadeTransition(Duration.millis(FADE_IN_DURATION_MS), root);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();

        LOGGER.log(Level.INFO, "Splash Stage configurado e exibido");
    }

    /**
     * Fallback de emergência caso o FXML falhe.
     */
    private void criarFallbackEmergencial(Stage owner) {
        LOGGER.log(Level.WARNING, "Criando fallback de emergência para Splash");

        VBox fallbackRoot = new VBox(20);
        fallbackRoot.setStyle(
                "-fx-padding: 40px;" +
                        "-fx-alignment: center;" +
                        "-fx-background-color: #2c3e50;"
        );

        Label title = new Label("🏔️ Gestão de TI");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");

        ProgressBar fallbackProgress = new ProgressBar(0);
        fallbackProgress.setPrefWidth(300);
        fallbackProgress.setStyle("-fx-accent: #27ae60;");

        Label fallbackStatus = new Label("Carregando...");
        fallbackStatus.setStyle("-fx-text-fill: #bdc3c7;");

        fallbackRoot.getChildren().addAll(title, fallbackProgress, fallbackStatus);

        // Cria um controller dummy para o fallback
        controller = new SplashScreenController() {
            @Override
            public void updateProgress(double progress) {
                Platform.runLater(() -> fallbackProgress.setProgress(progress));
            }

            @Override
            public void updateStatus(String status) {
                Platform.runLater(() -> fallbackStatus.setText(status));
            }
        };

        configurarStage(owner, fallbackRoot);
        controller.updateStatus("⚠️ Modo de emergência - FXML não encontrado");
    }
}