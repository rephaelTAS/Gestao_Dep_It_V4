package com.ossobo.gestaoDepIt.config;

import com.ossobo.winterfx.bootstrap.WinterApplication;

import com.ossobo.winterfx.scanner.registry.BeanRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AppConfig v6.1 - Configuração centralizada da aplicação
 *
 * Responsabilidades:
 * - Holder de configurações globais
 * - Gerenciamento do ciclo de vida do WinterFX
 * - Ponto único de acesso ao framework
 *
 * ⚠️ NOTA: Esta classe NÃO inicializa o WinterFX.
 * A inicialização é feita pelo Launcher via WinterApplication.run()
 *
 * @version 6.1
 * @since 2026-08-19
 */
public final class AppConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppConfig.class);

    // Singleton do WinterFX
    private static WinterApplication winterApp;
    private static BeanRegistry beanRegistry;
    private static boolean initialized = false;

    // Configurações da aplicação
    private static final String APP_NAME = "Gestão de TI";
    private static final String APP_VERSION = "3.1";
    private static final String FRAMEWORK = "WinterFX 13.1.5";

    private AppConfig() {
        throw new UnsupportedOperationException("Classe utilitária - não instanciar");
    }

    // ============================================================
    // INICIALIZAÇÃO
    // ============================================================

    /**
     * Inicializa as configurações da aplicação.
     * Deve ser chamada APÓS o WinterFX ser iniciado pelo Launcher.
     *
     * @param mainClass Classe principal da aplicação (para referência)
     */
    public static void initialize(Class<?> mainClass) {
        if (initialized) {
            LOGGER.warn("⚠️ AppConfig já inicializado");
            return;
        }

        try {
            LOGGER.info("🏔️ AppConfig v6.1: Registrando configurações...");

            // Obtém referência ao WinterFX (já iniciado pelo Launcher)
            winterApp = WinterApplication.getInstance();


            if (winterApp == null) {
                throw new IllegalStateException("WinterFX não foi inicializado. Verifique o Launcher.");
            }

            LOGGER.info("✅ AppConfig vinculado ao WinterFX");
            LOGGER.info("   Aplicação: {} v{}", APP_NAME, APP_VERSION);
            LOGGER.info("   Framework: {}", FRAMEWORK);
            LOGGER.info("   Pacote base: {}", mainClass.getPackageName());

            initialized = true;

        } catch (Exception e) {
            LOGGER.error("❌ Erro ao configurar AppConfig", e);
            throw new RuntimeException("Falha na configuração da aplicação", e);
        }
    }

    // ============================================================
    // GETTERS - Acesso ao Framework
    // ============================================================

    /**
     * Obtém a instância do WinterApplication
     */
    public static WinterApplication getWinterApp() {
        if (!initialized) {
            LOGGER.warn("⚠️ AppConfig não inicializado. Chamando initialize() automaticamente...");
            initialize(AppConfig.class);
        }
        return winterApp;
    }

    /**
     * Obtém o BeanRegistry para acesso a beans gerenciados
     */
    public static BeanRegistry getBeanRegistry() {
        if (!initialized) {
            initialize(AppConfig.class);
        }
        return beanRegistry;
    }


    // ============================================================
    // SHUTDOWN
    // ============================================================

    /**
     * Finaliza a aplicação de forma graciosa
     */
    public static void shutdown() {
        if (!initialized) {
            LOGGER.warn("⚠️ AppConfig não estava inicializado");
            return;
        }

        LOGGER.info("🛑 AppConfig: Iniciando shutdown...");

        try {
            // Finaliza WinterFX
            if (winterApp != null) {
                winterApp.shutdown();
                LOGGER.info("✅ WinterFX finalizado");
            }
        } catch (Exception e) {
            LOGGER.warn("⚠️ Erro ao finalizar WinterFX: {}", e.getMessage());
        }

        // Limpa referências
        winterApp = null;
        beanRegistry = null;
        initialized = false;

        LOGGER.info("✅ AppConfig finalizado");
    }

    // ============================================================
    // UTILITÁRIOS
    // ============================================================

    /**
     * Verifica se o AppConfig já foi inicializado
     */
    public static boolean isInitialized() {
        return initialized;
    }

    /**
     * Obtém o nome da aplicação
     */
    public static String getAppName() {
        return APP_NAME;
    }

    /**
     * Obtém a versão da aplicação
     */
    public static String getAppVersion() {
        return APP_VERSION;
    }

    /**
     * Obtém informações completas da aplicação
     */
    public static String getAppInfo() {
        return String.format("%s v%s (%s)", APP_NAME, APP_VERSION, FRAMEWORK);
    }

    /**
     * Verifica se está em modo de desenvolvimento
     */
    public static boolean isDevMode() {
        return Boolean.parseBoolean(System.getProperty("app.dev.mode", "true"));
    }
}