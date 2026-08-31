package com.ossobo.gestaoDepIt.ui.splash;

import com.ossobo.gestaoDepIt.config.ViewConstant;
import com.ossobo.winterfx.view.anotations.NewScene;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;

/**
 * 🖼️ SplashManager v3.1 - Gerenciador único de Splash Screen
 *
 * Responsabilidades:
 * - Carregar o FXML da Splash Screen
 * - Gerenciar o ciclo de vida da Splash
 * - Fornecer API para atualização de progresso
 * - Transição suave para a aplicação principal
 *
 * ⚠️ IMPORTANTE: A Splash NÃO é gerenciada pelo WinterFX.
 * Ela é carregada antes do framework e fechada após sua inicialização.
 *
 * @version 3.1
 */
public final class SplashManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(SplashManager.class);
    private static final SplashManager INSTANCE = new SplashManager();

    // Caminho do FXML
    private static final String FXML_PATH =
            "/META-INF/gestaoDepIt/fxmls/splash/SplashScreen.fxml";

    // UI Components
    private Stage splashStage;
    private SplashScreenController controller;
    private Runnable onComplete;
    private boolean isShowing = false;

    private SplashManager() {}

    public static SplashManager getInstance() {
        return INSTANCE;
    }

    // ============================================================
    // MÉTODOS PÚBLICOS
    // ============================================================

    /**
     * Exibe a Splash Screen a partir do FXML
     */
    public void show(Stage owner, Runnable onCompleteCallback) {
        if (isShowing) {
            LOGGER.warn("⚠️ Splash já está sendo exibida");
            return;
        }

        this.onComplete = onCompleteCallback;
        isShowing = true;

        try {
            LOGGER.info("🖼️ Carregando Splash Screen do FXML...");

            // Carrega o FXML
            URL fxmlLocation = SplashManager.class.getResource(FXML_PATH);

            if (fxmlLocation == null) {
                LOGGER.error("❌ FXML não encontrado em: {}", FXML_PATH);
                createEmergencyFallback(owner);
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            // Obtém o controller
            controller = loader.getController();
            if (controller == null) {
                LOGGER.warn("⚠️ Controller não encontrado no FXML");
                // Cria um controller dummy
                controller = new SplashScreenController();
            }

            LOGGER.info("✅ Splash Screen carregada com sucesso");

            // Configura o Stage
            setupStage(owner, root);

            // Atualiza status inicial
            controller.updateStatus("Inicializando sistema...");
            controller.updateProgress(0.0);

        } catch (IOException e) {
            LOGGER.error("❌ Erro ao carregar FXML da Splash", e);
            createEmergencyFallback(owner);
        } catch (Exception e) {
            LOGGER.error("❌ Erro inesperado ao carregar Splash", e);
            createEmergencyFallback(owner);
        }
    }

    /**
     * Atualiza o progresso da splash
     */
    public void updateProgress(double progress, String message) {
        if (controller != null) {
            controller.update(progress, message);
        } else {
            LOGGER.warn("⚠️ Tentativa de atualizar splash sem controller");
        }
    }

    /**
     * Atualiza apenas o progresso
     */
    public void updateProgress(double progress) {
        if (controller != null) {
            controller.updateProgress(progress);
        }
    }

    /**
     * Atualiza apenas o status
     */
    public void updateStatus(String message) {
        if (controller != null) {
            controller.updateStatus(message);
        }
    }

    /**
     * Fecha a splash com animação de fade-out
     */
    public void hide() {
        if (!isShowing || splashStage == null) {
            return;
        }

        Platform.runLater(() -> {
            try {
                // Completa a splash antes de fechar
                if (controller != null) {
                    controller.complete();
                }

                Parent root = splashStage.getScene().getRoot();
                FadeTransition fadeOut = new FadeTransition(Duration.millis(500), root);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);
                fadeOut.setOnFinished(event -> {
                    splashStage.close();
                    isShowing = false;
                    if (onComplete != null) {
                        onComplete.run();
                    }
                    LOGGER.info("✅ Splash Screen fechada");
                });
                fadeOut.play();

            } catch (Exception e) {
                // Fallback: fecha imediatamente
                splashStage.close();
                isShowing = false;
                if (onComplete != null) {
                    onComplete.run();
                }
                LOGGER.warn("⚠️ Erro na animação de fade-out: {}", e.getMessage());
            }
        });
    }

    /**
     * Fecha a splash imediatamente (sem animação)
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
     * Exibe mensagem de erro na splash
     */
    public void showError(String message) {
        if (controller != null) {
            controller.showError(message);
        }
    }

    /**
     * Verifica se a splash está sendo exibida
     */
    public boolean isShowing() {
        return isShowing;
    }

    /**
     * Obtém o controller da splash (para uso externo)
     */
    public SplashScreenController getController() {
        return controller;
    }

    // ============================================================
    // MÉTODOS PRIVADOS
    // ============================================================

    /**
     * Configura o Stage da Splash
     */
    private void setupStage(Stage owner, Parent root) {
        splashStage = new Stage();
        splashStage.initStyle(StageStyle.UNDECORATED);
        splashStage.initOwner(owner);
        splashStage.setAlwaysOnTop(true);

        Scene scene = new Scene(root);
        splashStage.setScene(scene);
        splashStage.centerOnScreen();
        splashStage.show();

        // Fade in
        FadeTransition fadeIn = new FadeTransition(Duration.millis(800), root);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();

        LOGGER.info("✅ Splash Stage configurado e exibido");
    }

    /**
     * Fallback de emergência caso o FXML falhe
     */
    private void createEmergencyFallback(Stage owner) {
        LOGGER.warn("⚠️ Criando fallback de emergência para Splash");

        VBox fallbackRoot = new VBox(20);
        fallbackRoot.setStyle(
                "-fx-padding: 40px;" +
                        "-fx-alignment: center;" +
                        "-fx-background-color: #2c3e50;"
        );

        javafx.scene.control.Label title = new javafx.scene.control.Label("🏔️ Gestão de TI");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");

        ProgressBar fallbackProgress = new ProgressBar(0);
        fallbackProgress.setPrefWidth(300);
        fallbackProgress.setStyle("-fx-accent: #27ae60;");

        javafx.scene.control.Label fallbackStatus = new javafx.scene.control.Label("Carregando...");
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

        setupStage(owner, fallbackRoot);
        controller.updateStatus("⚠️ Modo de emergência - FXML não encontrado");
    }

    @NewScene(view = ViewConstant.Main.LOGIN)
    public void showLogin(){

    }
}