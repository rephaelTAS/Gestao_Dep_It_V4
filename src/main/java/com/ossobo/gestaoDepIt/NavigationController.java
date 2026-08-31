package com.ossobo.gestaoDepIt;

import com.ossobo.gestaoDepIt.config.AppConfig;
import com.ossobo.gestaoDepIt.config.ViewConstant;
import com.ossobo.winterfx.anotations.Controller;
import com.ossobo.winterfx.view.anotations.NewScene;
import com.ossobo.winterfx.view.anotations.RegisterView;
import com.ossobo.winterfx.view.controller.WinterFXController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 🧭 NavigationController - Controlador de Navegação Global
 *
 * Responsabilidades:
 * - Gerenciar a navegação entre telas
 * - Servir como ponto central para @NewScene
 * - Ser gerenciado pelo WinterFX
 *
 * ⚠️ NOTA: Este Controller não tem uma view associada.
 * É apenas um ponto de entrada para navegação.
 *
 * @version 1.0
 * @since 2026-08-19
 */
@Controller(proxy = false)  // ESSENCIAL: Não é uma View, apenas um controller de navegação
public class NavigationController implements WinterFXController {
    private static final Logger LOGGER = LoggerFactory.getLogger(NavigationController.class);

    private static NavigationController instance;

    /**
     * Construtor padrão - WinterFX vai instanciar via DI
     */
    public NavigationController() {
        LOGGER.info("🧭 NavigationController inicializado");
        instance = this;
    }

    // ============================================================
    // MÉTODOS DE NAVEGAÇÃO
    // ============================================================

    /**
     * Navega para a tela de login com @NewScene
     *
     * @NewScene:
     * - view: ID da view registrada com @RegisterView
     * - title: Título da janela
     * - width: Largura da janela
     * - height: Altura da janela
     * - centered: Centralizar na tela
     * - closeCurrent: Fecha a view atual
     */
    @NewScene(
            view = ViewConstant.Main.LOGIN,
            title = "Login - Gestão de TI",
            width = 400,
            height = 350,
            centered = true,
            closeCurrent = true
    )
    public static void navigateToLogin() {
        LOGGER.info("🔐 Navegando para tela de login via @NewScene");
        // O corpo do método pode ficar vazio
        // O WinterFX intercepta a anotação e faz a navegação
    }

    /**
     * Navega para o Dashboard (após login bem-sucedido)
     */
    @NewScene(
            view = ViewConstant.Main.DASHBOARD,
            title = "Dashboard - Gestão de TI",
            width = 1200,
            height = 800,
            centered = true,
            closeCurrent = true
    )
    public void navigateToDashboard() {
        LOGGER.info("📊 Navegando para Dashboard via @NewScene");
    }

}