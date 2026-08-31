package com.ossobo.gestaoDepIt;

import com.ossobo.gestaoDepIt.config.AppConfig;
import com.ossobo.gestaoDepIt.db.sync.SincronizadorDados;
import com.ossobo.gestaoDepIt.ui.splash.SplashManager;
import com.ossobo.winterfx.bootstrap.WinterApplication;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 🎯 Main Application - WinterFX v13.1.5
 *
 * v3.3 - Integração completa com SplashManager e navegação via Controller
 *
 * Fluxo de Inicialização:
 * 1. Launcher → WinterApplication.run(Main.class)
 * 2. Main.start() → SplashManager.show()
 * 3. SplashManager.updateProgress() durante carregamento
 * 4. AppConfig.initialize() → registra configurações
 * 5. WinterApplication.getInstance().autoStart() → carrega View padrão
 * 6. SplashManager.hide() → fecha splash
 * 7. Navegação para Login via Controller
 *
 * @version 3.3
 * @since 2026-08-19
 */
public class Main extends Application {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private SplashManager splashManager;
    private Stage primaryStage;

    // ============================================================
    // INIT - Antes do start()
    // ============================================================

    @Override
    public void init() throws Exception {
        LOGGER.info("""
            
            🏔️ GESTÃO DE TI - INICIANDO
            ============================================
            Framework: WinterFX v13.1.5
            JavaFX: {}
            Java: {} (LTS)
            Modo: {}
            """,
                System.getProperty("javafx.version", "25.0.2"),
                System.getProperty("java.version", "21"),
                isDevMode() ? "🔧 DESENVOLVIMENTO" : "🚀 PRODUÇÃO"
        );
        super.init();
    }

    // ============================================================
    // START - Ponto de entrada principal
    // ============================================================

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        try {
            LOGGER.info("🎬 INICIANDO APLICAÇÃO...");

            // 1. Inicializa SplashManager
            splashManager = SplashManager.getInstance();

            // 2. Exibe Splash Screen com callback
            //    O callback será executado APÓS a splash ser fechada
            splashManager.show(primaryStage, this::onSplashComplete);

            // 3. Atualiza progresso - Banco de Dados
            splashManager.updateProgress(0.2, "📁 Conectando ao banco de dados...");
            Thread.sleep(200); // Simula carregamento

            // 4. Atualiza progresso - Registro de módulos
            splashManager.updateProgress(0.4, "📦 Registrando módulos...");

            // 5. Configura WinterFX (via AppConfig)
            splashManager.updateProgress(0.6, "⚙️ Configurando framework...");
            AppConfig.initialize(getClass());

            // 6. Inicia a aplicação WinterFX com uma View temporária
            //    NOTA: Usamos uma view "splash" que será substituída pelo login
            splashManager.updateProgress(0.8, "🔐 Preparando tela de login...");

            WinterApplication.getInstance()
                    .withMainView("splash")  // View temporária
                    .withDiagnostics(isDevMode())
                    .autoStart(primaryStage);

            // 7. Finaliza Splash
            splashManager.updateProgress(1.0, "✅ Sistema pronto!");
            Thread.sleep(300);

            // 8. Fecha a splash - o callback onSplashComplete será executado
            splashManager.hide();

            LOGGER.info("✅ Aplicação inicializada com sucesso!");

        } catch (Exception e) {
            LOGGER.error("❌ FALHA CRÍTICA NA INICIALIZAÇÃO", e);
            handleCriticalError(primaryStage, e);
        }
    }

    // ============================================================
    // CALLBACK DA SPLASH
    // ============================================================

    /**
     * Executado quando a splash é fechada com sucesso.
     * Aqui navegamos para a tela de login usando o WinterFX.
     */
    private void onSplashComplete() {
        LOGGER.info("🔄 Splash finalizada - navegando para login...");

        // Aguarda um momento para garantir que o WinterFX está pronto
        Platform.runLater(() -> {
            try {
                // Inicia o sincronizador em background
                iniciarSincronizador();

                // Navega para a tela de login usando o NavigationController
                // (que é gerenciado pelo WinterFX)
                NavigationController.navigateToLogin();

                LOGGER.info("✅ Navegação para login concluída");

            } catch (Exception e) {
                LOGGER.error("❌ Erro ao navegar para login", e);
                showEmergencyError(primaryStage, e);
            }
        });
    }

    // ============================================================
    // SINCRONIZADOR (Background)
    // ============================================================

    /**
     * Inicia o sincronizador de dados em uma thread separada.
     * Não bloqueia a UI e não interfere no carregamento da aplicação.
     */
    private void iniciarSincronizador() {
        new Thread(() -> {
            try {
                LOGGER.info("🔄 Iniciando sincronizador em background...");
                SincronizadorDados sinc = SincronizadorDados.getInstance();
                sinc.iniciar();
                LOGGER.info("✅ Sincronizador iniciado e rodando");
            } catch (Exception e) {
                LOGGER.error("❌ Falha ao iniciar sincronizador", e);
            }
        }, "Sincronizador-Thread").start();
    }

    // ============================================================
    // SHUTDOWN - Finalização graciosa
    // ============================================================

    @Override
    public void stop() throws Exception {
        LOGGER.info("🔌 FINALIZANDO APLICAÇÃO...");

        try {
            SincronizadorDados.getInstance().parar();
            LOGGER.info("✅ Sincronizador finalizado");
        } catch (Exception e) {
            LOGGER.warn("⚠️ Erro ao finalizar sincronizador: {}", e.getMessage());
        }

        try {
            AppConfig.shutdown();
            LOGGER.info("✅ WinterFX finalizado");
        } catch (Exception e) {
            LOGGER.warn("⚠️ Erro ao finalizar WinterFX: {}", e.getMessage());
        }

        try {
            if (splashManager != null && splashManager.isShowing()) {
                splashManager.closeImmediately();
            }
        } catch (Exception e) {
            LOGGER.warn("⚠️ Erro ao fechar splash: {}", e.getMessage());
        }

        super.stop();
        LOGGER.info("🎯 APLICAÇÃO FINALIZADA COM SUCESSO");
    }

    // ============================================================
    // TRATAMENTO DE ERROS
    // ============================================================

    private void handleCriticalError(Stage primaryStage, Exception error) {
        if (splashManager != null) {
            splashManager.showError(error.getMessage());
            splashManager.hide();
        }
        showEmergencyErrorScreen(primaryStage, error);
    }

    private void showEmergencyError(Stage stage, Exception error) {
        // Reutiliza o método de emergência
        showEmergencyErrorScreen(stage, error);
    }

    private void showEmergencyErrorScreen(Stage stage, Exception error) {
        try {
            LOGGER.error("🚨 EXIBINDO TELA DE EMERGÊNCIA");

            Label titleLabel = new Label("🚨 SISTEMA INDISPONÍVEL");
            titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #ff4444;");

            String errorMsg = error.getMessage() != null ? error.getMessage() : "Erro desconhecido";
            Label errorLabel = new Label(
                    "Erro: " + errorMsg + "\n\n" +
                            "🔧 Reinicie a aplicação ou contate o suporte técnico.\n" +
                            "📋 Verifique o arquivo de logs para mais detalhes."
            );
            errorLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #666;");
            errorLabel.setWrapText(true);

            Button exitButton = new Button("Sair");
            exitButton.setStyle(
                    "-fx-padding: 12px 30px;" +
                            "-fx-background-color: #ff4444;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 5px;"
            );
            exitButton.setOnAction(e -> {
                Platform.exit();
                System.exit(1);
            });

            VBox layout = new VBox(20, titleLabel, errorLabel, exitButton);
            layout.setStyle(
                    "-fx-padding: 40px;" +
                            "-fx-alignment: center;" +
                            "-fx-background-color: #f8f9fa;"
            );

            Scene scene = new Scene(layout, 550, 320);
            stage.setScene(scene);
            stage.setTitle("🚨 Erro Crítico - Gestão de TI");
            stage.setResizable(false);
            stage.show();

        } catch (Exception e) {
            LOGGER.error("💥 FALHA CATASTRÓFICA", e);
            Platform.exit();
            System.exit(1);
        }
    }

    private boolean isDevMode() {
        return Boolean.parseBoolean(System.getProperty("app.dev.mode", "true"));
    }

    // ============================================================
    // MAIN
    // ============================================================

    public static void main(String[] args) {
        LOGGER.info("🏔️ Iniciando aplicação via Main...");
        launch(args);
    }
}