package com.ossobo.gestaoDepIt.config;

import com.ossobo.winterfx.imagemanager.anotations.RegisterImage;
import com.ossobo.winterfx.imagemanager.anotations.RegisterImages;
import com.ossobo.winterfx.resources.enums.ViewAnimation;

/**
 * AppImageConfig v2.0
 *
 * Fonte única dos IDs das imagens registradas via @RegisterImage / @InjectImage.
 *
 * Organização:
 *   System  → ícones de UI genéricos (ações, status, módulos)
 *   Modules → imagens específicas por tela (login, dashboard, catálogo, inventário)
 *
 * Padrão da chave: prefixo "." separa hierarquia; a chave é o CONTRATO com o
 * arquivo físico em META-INF/gestaoDepIt/. NUNCA renomear sem migrar o arquivo.
 *
 * @since v1.0
 */

@RegisterImages({
        // ============================================================
        // LOGOS E IMAGENS PRINCIPAIS
        // ============================================================
        @RegisterImage(
                id = "system.app.logo",
                src = "/META-INF/gestaoDepIt/icons/app-logo.jpg",
                imageType = ViewAnimation.ImageType.IMAGE,
                preferredWidth = 200,
                preferredHeight = 80,
                preserveRatio = true
        ),
        @RegisterImage(
                id = "system.user.default",
                src = "/META-INF/gestaoDepIt/icons/system/user.png",
                imageType = ViewAnimation.ImageType.IMAGE,
                preferredWidth = 100,
                preferredHeight = 100,
                preserveRatio = true
        ),

        // ============================================================
        // ÍCONES DE AÇÃO
        // ============================================================
        @RegisterImage(
                id = "system.add",
                src = "/META-INF/gestaoDepIt/icons/system/add.png",
                imageType = ViewAnimation.ImageType.ICON
        ),
        @RegisterImage(
                id = "system.add.item",
                src = "/META-INF/gestaoDepIt/icons/system/add-item.png",
                imageType = ViewAnimation.ImageType.ICON
        ),
        @RegisterImage(
                id = "system.view",
                src = "/META-INF/gestaoDepIt/icons/system/view.png",
                imageType = ViewAnimation.ImageType.ICON
        ),
        @RegisterImage(
                id = "system.edit",
                src = "/META-INF/gestaoDepIt/icons/system/edit.png",
                imageType = ViewAnimation.ImageType.ICON
        ),
        @RegisterImage(
                id = "system.delete",
                src = "/META-INF/gestaoDepIt/icons/system/delete.png",
                imageType = ViewAnimation.ImageType.ICON
        ),
        @RegisterImage(
                id = "system.save",
                src = "/META-INF/gestaoDepIt/icons/system/save.png",
                imageType = ViewAnimation.ImageType.ICON
        ),
        @RegisterImage(
                id = "system.cancel",
                src = "/META-INF/gestaoDepIt/icons/system/cancel.png",
                imageType = ViewAnimation.ImageType.ICON
        ),
        @RegisterImage(
                id = "system.search",
                src = "/META-INF/gestaoDepIt/icons/system/search.png",
                imageType = ViewAnimation.ImageType.ICON
        ),
        @RegisterImage(
                id = "system.refresh",
                src = "/META-INF/gestaoDepIt/icons/system/refresh.png",
                imageType = ViewAnimation.ImageType.ICON
        ),
        @RegisterImage(
                id = "system.export",
                src = "/META-INF/gestaoDepIt/icons/system/export.png",
                imageType = ViewAnimation.ImageType.ICON
        ),
        @RegisterImage(
                id = "system.print",
                src = "/META-INF/gestaoDepIt/icons/system/print.png",
                imageType = ViewAnimation.ImageType.ICON
        ),
        @RegisterImage(
                id = "system.filter",
                src = "/META-INF/gestaoDepIt/icons/system/filter.png",
                imageType = ViewAnimation.ImageType.ICON
        ),

        // ============================================================
        // ÍCONES DE NAVEGAÇÃO
        // ============================================================
        @RegisterImage(
                id = "system.dashboard",
                src = "/META-INF/gestaoDepIt/icons/system/dashboard.png",
                imageType = ViewAnimation.ImageType.ICON
        ),
        @RegisterImage(
                id = "system.menu",
                src = "/META-INF/gestaoDepIt/icons/system/menu.png",
                imageType = ViewAnimation.ImageType.ICON
        ),
        @RegisterImage(
                id = "system.list",
                src = "/META-INF/gestaoDepIt/icons/system/list.png",
                imageType = ViewAnimation.ImageType.ICON
        ),
        @RegisterImage(
                id = "system.report",
                src = "/META-INF/gestaoDepIt/icons/system/report.png",
                imageType = ViewAnimation.ImageType.ICON
        ),
        @RegisterImage(
                id = "system.setting",
                src = "/META-INF/gestaoDepIt/icons/system/setting.png",
                imageType = ViewAnimation.ImageType.ICON
        ),
        @RegisterImage(
                id = "system.home",
                src = "/META-INF/gestaoDepIt/icons/system/home.png",
                imageType = ViewAnimation.ImageType.ICON
        ),
        @RegisterImage(
                id = "system.back",
                src = "/META-INF/gestaoDepIt/icons/system/back.png",
                imageType = ViewAnimation.ImageType.ICON
        ),
        @RegisterImage(
                id = "system.next",
                src = "/META-INF/gestaoDepIt/icons/system/next.png",
                imageType = ViewAnimation.ImageType.ICON
        ),

        // ============================================================
        // ÍCONES DE ENTIDADES
        // ============================================================
        @RegisterImage(
                id = "system.produto",
                src = "/META-INF/gestaoDepIt/icons/system/produto.png",
                imageType = ViewAnimation.ImageType.ICON
        ),
        @RegisterImage(
                id = "system.stock",
                src = "/META-INF/gestaoDepIt/icons/system/stock.png",
                imageType = ViewAnimation.ImageType.ICON
        ),
        @RegisterImage(
                id = "system.user",
                src = "/META-INF/gestaoDepIt/icons/system/user.png",
                imageType = ViewAnimation.ImageType.ICON
        ),
        @RegisterImage(
                id = "system.user.add",
                src = "/META-INF/gestaoDepIt/icons/system/user-add.png",
                imageType = ViewAnimation.ImageType.ICON
        ),
        @RegisterImage(
                id = "system.inventario",
                src = "/META-INF/gestaoDepIt/icons/system/inventario.png",
                imageType = ViewAnimation.ImageType.ICON
        ),
        @RegisterImage(
                id = "system.toner",
                src = "/META-INF/gestaoDepIt/icons/system/toner.png",
                imageType = ViewAnimation.ImageType.ICON
        ),
        @RegisterImage(
                id = "system.funcionario",
                src = "/META-INF/gestaoDepIt/icons/system/funcionario.png",
                imageType = ViewAnimation.ImageType.ICON
        ),
        @RegisterImage(
                id = "system.relatorio",
                src = "/META-INF/gestaoDepIt/icons/system/relatorio.png",
                imageType = ViewAnimation.ImageType.ICON
        ),

        // ============================================================
        // ÍCONES DE STATUS
        // ============================================================
        @RegisterImage(
                id = "system.in.stock",
                src = "/META-INF/gestaoDepIt/icons/system/in_stock.png",
                imageType = ViewAnimation.ImageType.ICON
        ),
        @RegisterImage(
                id = "system.out.of.stock",
                src = "/META-INF/gestaoDepIt/icons/system/out_of_stock.png",
                imageType = ViewAnimation.ImageType.ICON
        ),
        @RegisterImage(
                id = "system.ballot",
                src = "/META-INF/gestaoDepIt/icons/system/ballot.png",
                imageType = ViewAnimation.ImageType.ICON
        ),
        @RegisterImage(
                id = "system.clock",
                src = "/META-INF/gestaoDepIt/icons/system/clock.png",
                imageType = ViewAnimation.ImageType.ICON
        ),
        @RegisterImage(
                id = "system.email",
                src = "/META-INF/gestaoDepIt/icons/system/email.png",
                imageType = ViewAnimation.ImageType.ICON
        ),
        @RegisterImage(
                id = "system.notifications",
                src = "/META-INF/gestaoDepIt/icons/system/notification.png",
                imageType = ViewAnimation.ImageType.ICON
        ),
        @RegisterImage(
                id = "system.success",
                src = "/META-INF/gestaoDepIt/icons/system/success.png",
                imageType = ViewAnimation.ImageType.ICON
        ),
        @RegisterImage(
                id = "system.error",
                src = "/META-INF/gestaoDepIt/icons/system/error.png",
                imageType = ViewAnimation.ImageType.ICON
        ),
        @RegisterImage(
                id = "system.warning",
                src = "/META-INF/gestaoDepIt/icons/system/warning.png",
                imageType = ViewAnimation.ImageType.ICON
        ),
        @RegisterImage(
                id = "system.info",
                src = "/META-INF/gestaoDepIt/icons/system/info.png",
                imageType = ViewAnimation.ImageType.ICON
        ),
        @RegisterImage(
                id = "system.loading",
                src = "/META-INF/gestaoDepIt/icons/system/loading.gif",
                imageType = ViewAnimation.ImageType.ICON
        ),
        @RegisterImage(
                id = "system.empty",
                src = "/META-INF/gestaoDepIt/icons/system/empty.png",
                imageType = ViewAnimation.ImageType.ICON
        ),

        // ============================================================
        // ÍCONES DE MÓDULOS
        // ============================================================
        @RegisterImage(
                id = "system.module.login",
                src = "/META-INF/gestaoDepIt/icons/system/module_login.png",
                imageType = ViewAnimation.ImageType.ICON
        ),
        @RegisterImage(
                id = "system.module.dashboard",
                src = "/META-INF/gestaoDepIt/icons/system/module_dashboard.png",
                imageType = ViewAnimation.ImageType.ICON
        ),
        @RegisterImage(
                id = "system.module.catalogo",
                src = "/META-INF/gestaoDepIt/icons/system/module_catalogo.png",
                imageType = ViewAnimation.ImageType.ICON
        ),
        @RegisterImage(
                id = "system.module.estoque",
                src = "/META-INF/gestaoDepIt/icons/system/module_estoque.png",
                imageType = ViewAnimation.ImageType.ICON
        ),
        @RegisterImage(
                id = "system.module.movimentacoes",
                src = "/META-INF/gestaoDepIt/icons/system/module_movimentacoes.png",
                imageType = ViewAnimation.ImageType.ICON
        ),

        // ============================================================
        // IMAGENS DOS MÓDULOS
        // ============================================================
        @RegisterImage(
                id = "modules.login.background",
                src = "/META-INF/gestaoDepIt/images/login/bg_login.jpg",
                imageType = ViewAnimation.ImageType.BACKGROUND
        ),
        @RegisterImage(
                id = "modules.login.logo",
                src = "/META-INF/gestaoDepIt/images/login/logo_login.png",
                imageType = ViewAnimation.ImageType.IMAGE,
                preferredWidth = 150,
                preferredHeight = 150,
                preserveRatio = true
        ),
        @RegisterImage(
                id = "modules.dashboard.welcome.background",
                src = "/META-INF/gestaoDepIt/images/dashboard/welcome_bg.jpg",
                imageType = ViewAnimation.ImageType.BACKGROUND
        ),
        @RegisterImage(
                id = "modules.catalogo.produto.placeholder",
                src = "/META-INF/gestaoDepIt/images/catalogo/produto_placeholder.png",
                imageType = ViewAnimation.ImageType.IMAGE,
                preferredWidth = 200,
                preferredHeight = 200,
                preserveRatio = true
        ),
        @RegisterImage(
                id = "modules.catalogo.empty.state",
                src = "/META-INF/gestaoDepIt/images/catalogo/empty_state.png",
                imageType = ViewAnimation.ImageType.IMAGE
        ),
        @RegisterImage(
                id = "modules.inventario.equipamento.default",
                src = "/META-INF/gestaoDepIt/images/inventario/equipamento_default.png",
                imageType = ViewAnimation.ImageType.IMAGE,
                preferredWidth = 150,
                preferredHeight = 150,
                preserveRatio = true
        ),
        @RegisterImage(
                id = "modules.inventario.qrcode.placeholder",
                src = "/META-INF/gestaoDepIt/images/inventario/qrcode_placeholder.png",
                imageType = ViewAnimation.ImageType.IMAGE
        )
})
public final class AppImageConfig {

    private AppImageConfig() {
        throw new UnsupportedOperationException("Classe de constantes");
    }

    // ============================================================
    // SYSTEM — ícones e imagens de UI genéricas
    // ============================================================
    public static final class System {

        // ----- Identidade do app -----
        public static final String APP_LOGO     = "system.app.logo";
        public static final String DEFAULT_USER = "system.user.default";

        // ----- Ações (toolbar / botões) -----
        public static final String ADD        = "system.add";
        public static final String ADD_ITEM   = "system.add.item";
        public static final String VIEW       = "system.view";
        public static final String EDIT       = "system.edit";
        public static final String DELETE     = "system.delete";
        public static final String SAVE       = "system.save";
        public static final String CANCEL     = "system.cancel";
        public static final String SEARCH     = "system.search";
        public static final String REFRESH    = "system.refresh";
        public static final String EXPORT     = "system.export";
        public static final String PRINT      = "system.print";
        public static final String FILTER     = "system.filter";

        // ----- Navegação / estrutura -----
        public static final String DASHBOARD = "system.dashboard";
        public static final String MENU      = "system.menu";
        public static final String LIST      = "system.list";
        public static final String REPORT    = "system.report";
        public static final String SETTING   = "system.setting";
        public static final String HOME      = "system.home";
        public static final String BACK      = "system.back";
        public static final String NEXT      = "system.next";

        // ----- Domínio -----
        public static final String PRODUTO      = "system.produto";
        public static final String STOCK        = "system.stock";
        public static final String USER         = "system.user";
        public static final String USER_ADD     = "system.user.add";
        public static final String INVENTARIO   = "system.inventario";
        public static final String TONER        = "system.toner";
        public static final String FUNCIONARIO  = "system.funcionario";
        public static final String RELATORIO    = "system.relatorio";

        // ----- Estados / feedback -----
        public static final String IN_STOCK       = "system.in.stock";
        public static final String OUT_OF_STOCK   = "system.out.of.stock";
        public static final String BALLOT         = "system.ballot";
        public static final String CLOCK          = "system.clock";
        public static final String EMAIL          = "system.email";
        public static final String NOTIFICATIONS  = "system.notifications";
        public static final String SUCCESS        = "system.success";
        public static final String ERROR          = "system.error";
        public static final String WARNING        = "system.warning";
        public static final String INFO           = "system.info";
        public static final String LOADING        = "system.loading";
        public static final String EMPTY          = "system.empty";

        // ----- Ícones de módulo (menu principal) -----
        public static final String MODULE_LOGIN         = "system.module.login";
        public static final String MODULE_DASHBOARD     = "system.module.dashboard";
        public static final String MODULE_CATALOGO      = "system.module.catalogo";
        public static final String MODULE_ESTOQUE       = "system.module.estoque";
        public static final String MODULE_MOVIMENTACOES = "system.module.movimentacoes";

        private System() { }
    }

    // ============================================================
    // MODULES — imagens específicas por tela
    // ============================================================
    public static final class Modules {

        public static final class Login {
            public static final String LOGIN_BG   = "modules.login.background";
            public static final String LOGIN_LOGO = "modules.login.logo";

            private Login() { }
        }

        public static final class Dashboard {
            public static final String WELCOME_BG = "modules.dashboard.welcome.background";

            private Dashboard() { }
        }

        public static final class Catalogo {
            public static final String PRODUTO_PLACEHOLDER = "modules.catalogo.produto.placeholder";
            public static final String EMPTY_STATE         = "modules.catalogo.empty.state";

            private Catalogo() { }
        }

        public static final class Inventario {
            public static final String EQUIPAMENTO_DEFAULT  = "modules.inventario.equipamento.default";
            public static final String QR_CODE_PLACEHOLDER  = "modules.inventario.qrcode.placeholder";

            private Inventario() { }
        }

        private Modules() { }
    }

    // ============================================================
    // UTILITÁRIOS
    // ============================================================

    /** Array com TODOS os IDs de imagem conhecidos — útil para diagnóstico / pré-carregamento. */
    public static String[] getAllImageIds() {
        return new String[]{
                // System — identidade
                System.APP_LOGO, System.DEFAULT_USER,

                // System — ações
                System.ADD, System.ADD_ITEM, System.VIEW, System.EDIT, System.DELETE,
                System.SAVE, System.CANCEL, System.SEARCH, System.REFRESH,
                System.EXPORT, System.PRINT, System.FILTER,

                // System — navegação
                System.DASHBOARD, System.MENU, System.LIST, System.REPORT,
                System.SETTING, System.HOME, System.BACK, System.NEXT,

                // System — domínio
                System.PRODUTO, System.STOCK, System.USER, System.USER_ADD,
                System.INVENTARIO, System.TONER, System.FUNCIONARIO, System.RELATORIO,

                // System — estados
                System.IN_STOCK, System.OUT_OF_STOCK, System.BALLOT, System.CLOCK,
                System.EMAIL, System.NOTIFICATIONS,
                System.SUCCESS, System.ERROR, System.WARNING, System.INFO,
                System.LOADING, System.EMPTY,

                // System — módulos
                System.MODULE_LOGIN, System.MODULE_DASHBOARD,
                System.MODULE_CATALOGO, System.MODULE_ESTOQUE,
                System.MODULE_MOVIMENTACOES,

                // Modules
                Modules.Login.LOGIN_BG, Modules.Login.LOGIN_LOGO,
                Modules.Dashboard.WELCOME_BG,
                Modules.Catalogo.PRODUTO_PLACEHOLDER, Modules.Catalogo.EMPTY_STATE,
                Modules.Inventario.EQUIPAMENTO_DEFAULT, Modules.Inventario.QR_CODE_PLACEHOLDER
        };
    }
}