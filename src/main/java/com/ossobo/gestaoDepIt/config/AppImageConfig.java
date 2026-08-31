package com.ossobo.gestaoDepIt.config;

import com.ossobo.winterfx.imagemanager.anotations.RegisterImage;
import com.ossobo.winterfx.imagemanager.anotations.RegisterImages;
import com.ossobo.winterfx.resources.enums.ResourceOrigin;
import com.ossobo.winterfx.resources.enums.ViewAnimation;

/**
 * 🎯 Registro de Imagens do Sistema - WinterFX
 *
 * ✅ Registro automático via @RegisterImage
 * ✅ As imagens são carregadas pelo WinterFX durante a inicialização
 * ✅ IDs podem ser usados com @InjectImage
 *
 * @author Rafael Tavares
 */
@RegisterImages({
        // ============================================================
        // LOGOS E IMAGENS PRINCIPAIS
        // ============================================================
        @RegisterImage(
                id = "system.app.logo",
                src = "/com/ossobo/gestaoDepIt/assets/images/logo.png",
                imageType = ViewAnimation.ImageType.IMAGE,
                preferredWidth = 200,
                preferredHeight = 80,
                preserveRatio = true
        ),
        @RegisterImage(
                id = "system.user.default",
                src = "/com/ossobo/gestaoDepIt/assets/images/default_user.png",
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
                src = "/com/ossobo/gestaoDepIt/assets/icons/add.png",
                imageType = ViewAnimation.ImageType.ICON
        ),
        @RegisterImage(
                id = "system.add.item",
                src = "/com/ossobo/gestaoDepIt/assets/icons/add_item.png",
                imageType = ViewAnimation.ImageType.ICON
        ),
        @RegisterImage(
                id = "system.view",
                src = "/com/ossobo/gestaoDepIt/assets/icons/view.png",
                imageType = ViewAnimation.ImageType.ICON
        ),
        @RegisterImage(
                id = "system.edit",
                src = "/com/ossobo/gestaoDepIt/assets/icons/edit.png",
                imageType = ViewAnimation.ImageType.ICON
        ),
        @RegisterImage(
                id = "system.delete",
                src = "/com/ossobo/gestaoDepIt/assets/icons/delete.png",
                imageType = ViewAnimation.ImageType.ICON
        ),
        @RegisterImage(
                id = "system.save",
                src = "/com/ossobo/gestaoDepIt/assets/icons/save.png",
                imageType = ViewAnimation.ImageType.ICON
        ),
        @RegisterImage(
                id = "system.cancel",
                src = "/com/ossobo/gestaoDepIt/assets/icons/cancel.png",
                imageType = ViewAnimation.ImageType.ICON
        ),
        @RegisterImage(
                id = "system.search",
                src = "/com/ossobo/gestaoDepIt/assets/icons/search.png",
                imageType = ViewAnimation.ImageType.ICON
        ),
        @RegisterImage(
                id = "system.refresh",
                src = "/com/ossobo/gestaoDepIt/assets/icons/refresh.png",
                imageType = ViewAnimation.ImageType.ICON
        ),
        @RegisterImage(
                id = "system.export",
                src = "/com/ossobo/gestaoDepIt/assets/icons/export.png",
                imageType = ViewAnimation.ImageType.ICON
        ),
        @RegisterImage(
                id = "system.print",
                src = "/com/ossobo/gestaoDepIt/assets/icons/print.png",
                imageType = ViewAnimation.ImageType.ICON
        ),
        @RegisterImage(
                id = "system.filter",
                src = "/com/ossobo/gestaoDepIt/assets/icons/filter.png",
                imageType = ViewAnimation.ImageType.ICON
        ),

        // ============================================================
        // ÍCONES DE NAVEGAÇÃO
        // ============================================================
        @RegisterImage(
                id = "system.dashboard",
                src = "/com/ossobo/gestaoDepIt/assets/icons/dashboard.png",
                imageType = ViewAnimation.ImageType.ICON
        ),
        @RegisterImage(
                id = "system.menu",
                src = "/com/ossobo/gestaoDepIt/assets/icons/menu.png",
                imageType = ViewAnimation.ImageType.ICON
        ),
        @RegisterImage(
                id = "system.list",
                src = "/com/ossobo/gestaoDepIt/assets/icons/list.png",
                imageType = ViewAnimation.ImageType.ICON
        ),
        @RegisterImage(
                id = "system.report",
                src = "/com/ossobo/gestaoDepIt/assets/icons/report.png",
                imageType = ViewAnimation.ImageType.ICON
        ),
        @RegisterImage(
                id = "system.setting",
                src = "/com/ossobo/gestaoDepIt/assets/icons/setting.png",
                imageType = ViewAnimation.ImageType.ICON
        ),
        @RegisterImage(
                id = "system.home",
                src = "/com/ossobo/gestaoDepIt/assets/icons/home.png",
                imageType = ViewAnimation.ImageType.ICON
        ),
        @RegisterImage(
                id = "system.back",
                src = "/com/ossobo/gestaoDepIt/assets/icons/back.png",
                imageType = ViewAnimation.ImageType.ICON
        ),
        @RegisterImage(
                id = "system.next",
                src = "/com/ossobo/gestaoDepIt/assets/icons/next.png",
                imageType = ViewAnimation.ImageType.ICON
        ),

        // ============================================================
        // ÍCONES DE ENTIDADES
        // ============================================================
        @RegisterImage(
                id = "system.produto",
                src = "/com/ossobo/gestaoDepIt/assets/icons/produto.png",
                imageType = ViewAnimation.ImageType.ICON
        ),
        @RegisterImage(
                id = "system.stock",
                src = "/com/ossobo/gestaoDepIt/assets/icons/stock.png",
                imageType = ViewAnimation.ImageType.ICON
        ),
        @RegisterImage(
                id = "system.user",
                src = "/com/ossobo/gestaoDepIt/assets/icons/user.png",
                imageType = ViewAnimation.ImageType.ICON
        ),
        @RegisterImage(
                id = "system.user.add",
                src = "/com/ossobo/gestaoDepIt/assets/icons/user_add.png",
                imageType = ViewAnimation.ImageType.ICON
        ),
        @RegisterImage(
                id = "system.inventario",
                src = "/com/ossobo/gestaoDepIt/assets/icons/inventario.png",
                imageType = ViewAnimation.ImageType.ICON
        ),
        @RegisterImage(
                id = "system.toner",
                src = "/com/ossobo/gestaoDepIt/assets/icons/toner.png",
                imageType = ViewAnimation.ImageType.ICON
        ),
        @RegisterImage(
                id = "system.funcionario",
                src = "/com/ossobo/gestaoDepIt/assets/icons/funcionario.png",
                imageType = ViewAnimation.ImageType.ICON
        ),
        @RegisterImage(
                id = "system.relatorio",
                src = "/com/ossobo/gestaoDepIt/assets/icons/relatorio.png",
                imageType = ViewAnimation.ImageType.ICON
        ),

        // ============================================================
        // ÍCONES DE STATUS
        // ============================================================
        @RegisterImage(
                id = "system.in.stock",
                src = "/com/ossobo/gestaoDepIt/assets/icons/in_stock.png",
                imageType = ViewAnimation.ImageType.ICON
        ),
        @RegisterImage(
                id = "system.out.of.stock",
                src = "/com/ossobo/gestaoDepIt/assets/icons/out_of_stock.png",
                imageType = ViewAnimation.ImageType.ICON
        ),
        @RegisterImage(
                id = "system.ballot",
                src = "/com/ossobo/gestaoDepIt/assets/icons/ballot.png",
                imageType = ViewAnimation.ImageType.ICON
        ),
        @RegisterImage(
                id = "system.clock",
                src = "/com/ossobo/gestaoDepIt/assets/icons/clock.png",
                imageType = ViewAnimation.ImageType.ICON
        ),
        @RegisterImage(
                id = "system.email",
                src = "/com/ossobo/gestaoDepIt/assets/icons/email.png",
                imageType = ViewAnimation.ImageType.ICON
        ),
        @RegisterImage(
                id = "system.notifications",
                src = "/com/ossobo/gestaoDepIt/assets/icons/notifications.png",
                imageType = ViewAnimation.ImageType.ICON
        ),
        @RegisterImage(
                id = "system.success",
                src = "/com/ossobo/gestaoDepIt/assets/icons/success.png",
                imageType = ViewAnimation.ImageType.ICON
        ),
        @RegisterImage(
                id = "system.error",
                src = "/com/ossobo/gestaoDepIt/assets/icons/error.png",
                imageType = ViewAnimation.ImageType.ICON
        ),
        @RegisterImage(
                id = "system.warning",
                src = "/com/ossobo/gestaoDepIt/assets/icons/warning.png",
                imageType = ViewAnimation.ImageType.ICON
        ),
        @RegisterImage(
                id = "system.info",
                src = "/com/ossobo/gestaoDepIt/assets/icons/info.png",
                imageType = ViewAnimation.ImageType.ICON
        ),
        @RegisterImage(
                id = "system.loading",
                src = "/com/ossobo/gestaoDepIt/assets/icons/loading.gif",
                imageType = ViewAnimation.ImageType.ICON
        ),
        @RegisterImage(
                id = "system.empty",
                src = "/com/ossobo/gestaoDepIt/assets/icons/empty.png",
                imageType = ViewAnimation.ImageType.ICON
        ),

        // ============================================================
        // ÍCONES DE MÓDULOS
        // ============================================================
        @RegisterImage(
                id = "system.module.login",
                src = "/com/ossobo/gestaoDepIt/assets/icons/module_login.png",
                imageType = ViewAnimation.ImageType.ICON
        ),
        @RegisterImage(
                id = "system.module.dashboard",
                src = "/com/ossobo/gestaoDepIt/assets/icons/module_dashboard.png",
                imageType = ViewAnimation.ImageType.ICON
        ),
        @RegisterImage(
                id = "system.module.catalogo",
                src = "/com/ossobo/gestaoDepIt/assets/icons/module_catalogo.png",
                imageType = ViewAnimation.ImageType.ICON
        ),
        @RegisterImage(
                id = "system.module.estoque",
                src = "/com/ossobo/gestaoDepIt/assets/icons/module_estoque.png",
                imageType = ViewAnimation.ImageType.ICON
        ),
        @RegisterImage(
                id = "system.module.movimentacoes",
                src = "/com/ossobo/gestaoDepIt/assets/icons/module_movimentacoes.png",
                imageType = ViewAnimation.ImageType.ICON
        ),

        // ============================================================
        // IMAGENS DOS MÓDULOS
        // ============================================================
        @RegisterImage(
                id = "modules.login.background",
                src = "/com/ossobo/gestaoDepIt/assets/images/login/bg_login.jpg",
                imageType = ViewAnimation.ImageType.BACKGROUND
        ),
        @RegisterImage(
                id = "modules.login.logo",
                src = "/com/ossobo/gestaoDepIt/assets/images/login/logo_login.png",
                imageType = ViewAnimation.ImageType.IMAGE,
                preferredWidth = 150,
                preferredHeight = 150,
                preserveRatio = true
        ),
        @RegisterImage(
                id = "modules.dashboard.welcome.background",
                src = "/com/ossobo/gestaoDepIt/assets/images/dashboard/welcome_bg.jpg",
                imageType = ViewAnimation.ImageType.BACKGROUND
        ),
        @RegisterImage(
                id = "modules.catalogo.produto.placeholder",
                src = "/com/ossobo/gestaoDepIt/assets/images/catalogo/produto_placeholder.png",
                imageType = ViewAnimation.ImageType.IMAGE,
                preferredWidth = 200,
                preferredHeight = 200,
                preserveRatio = true
        ),
        @RegisterImage(
                id = "modules.catalogo.empty.state",
                src = "/com/ossobo/gestaoDepIt/assets/images/catalogo/empty_state.png",
                imageType = ViewAnimation.ImageType.IMAGE
        ),
        @RegisterImage(
                id = "modules.inventario.equipamento.default",
                src = "/com/ossobo/gestaoDepIt/assets/images/inventario/equipamento_default.png",
                imageType = ViewAnimation.ImageType.IMAGE,
                preferredWidth = 150,
                preferredHeight = 150,
                preserveRatio = true
        ),
        @RegisterImage(
                id = "modules.inventario.qrcode.placeholder",
                src = "/com/ossobo/gestaoDepIt/assets/images/inventario/qrcode_placeholder.png",
                imageType = ViewAnimation.ImageType.IMAGE
        )
})
public final class AppImageConfig {

    private AppImageConfig() {
        throw new UnsupportedOperationException("Classe de constantes");
    }

    // ============================================================
    // CONSTANTES - IDs DAS IMAGENS
    // ============================================================

    public static final class System {
        public static final String APP_LOGO = "system.app.logo";
        public static final String DEFAULT_USER = "system.user.default";

        public static final String ADD = "system.add";
        public static final String ADD_ITEM = "system.add.item";
        public static final String VIEW = "system.view";
        public static final String EDIT = "system.edit";
        public static final String DELETE = "system.delete";
        public static final String SAVE = "system.save";
        public static final String CANCEL = "system.cancel";
        public static final String SEARCH = "system.search";
        public static final String REFRESH = "system.refresh";
        public static final String EXPORT = "system.export";
        public static final String PRINT = "system.print";
        public static final String FILTER = "system.filter";

        public static final String DASHBOARD = "system.dashboard";
        public static final String MENU = "system.menu";
        public static final String LIST = "system.list";
        public static final String REPORT = "system.report";
        public static final String SETTING = "system.setting";
        public static final String HOME = "system.home";
        public static final String BACK = "system.back";
        public static final String NEXT = "system.next";

        public static final String PRODUTO = "system.produto";
        public static final String STOCK = "system.stock";
        public static final String USER = "system.user";
        public static final String USER_ADD = "system.user.add";
        public static final String INVENTARIO = "system.inventario";
        public static final String TONER = "system.toner";
        public static final String FUNCIONARIO = "system.funcionario";
        public static final String RELATORIO = "system.relatorio";

        public static final String IN_STOCK = "system.in.stock";
        public static final String OUT_OF_STOCK = "system.out.of.stock";
        public static final String BALLOT = "system.ballot";
        public static final String CLOCK = "system.clock";
        public static final String EMAIL = "system.email";
        public static final String NOTIFICATIONS = "system.notifications";
        public static final String SUCCESS = "system.success";
        public static final String ERROR = "system.error";
        public static final String WARNING = "system.warning";
        public static final String INFO = "system.info";
        public static final String LOADING = "system.loading";
        public static final String EMPTY = "system.empty";

        public static final String MODULE_LOGIN = "system.module.login";
        public static final String MODULE_DASHBOARD = "system.module.dashboard";
        public static final String MODULE_CATALOGO = "system.module.catalogo";
        public static final String MODULE_ESTOQUE = "system.module.estoque";
        public static final String MODULE_MOVIMENTACOES = "system.module.movimentacoes";

        private System() {}
    }

    public static final class Modules {
        public static final class Login {
            public static final String LOGIN_BG = "modules.login.background";
            public static final String LOGIN_LOGO = "modules.login.logo";
            private Login() {}
        }

        public static final class Dashboard {
            public static final String WELCOME_BG = "modules.dashboard.welcome.background";
            private Dashboard() {}
        }

        public static final class Catalogo {
            public static final String PRODUTO_PLACEHOLDER = "modules.catalogo.produto.placeholder";
            public static final String EMPTY_STATE = "modules.catalogo.empty.state";
            private Catalogo() {}
        }

        public static final class Inventario {
            public static final String EQUIPAMENTO_DEFAULT = "modules.inventario.equipamento.default";
            public static final String QR_CODE_PLACEHOLDER = "modules.inventario.qrcode.placeholder";
            private Inventario() {}
        }

        private Modules() {}
    }

    // ============================================================
    // UTILITÁRIOS
    // ============================================================

    public static String[] getAllImageIds() {
        return new String[]{
                System.APP_LOGO, System.DEFAULT_USER,
                System.ADD, System.ADD_ITEM, System.VIEW, System.EDIT, System.DELETE,
                System.SAVE, System.CANCEL, System.SEARCH, System.REFRESH,
                System.EXPORT, System.PRINT, System.FILTER,
                System.DASHBOARD, System.MENU, System.LIST, System.REPORT,
                System.SETTING, System.HOME, System.BACK, System.NEXT,
                System.PRODUTO, System.STOCK, System.USER, System.USER_ADD,
                System.INVENTARIO, System.TONER, System.FUNCIONARIO, System.RELATORIO,
                System.IN_STOCK, System.OUT_OF_STOCK, System.BALLOT, System.CLOCK,
                System.EMAIL, System.NOTIFICATIONS,
                System.SUCCESS, System.ERROR, System.WARNING, System.INFO,
                System.LOADING, System.EMPTY,
                System.MODULE_LOGIN, System.MODULE_DASHBOARD,
                System.MODULE_CATALOGO, System.MODULE_ESTOQUE,
                System.MODULE_MOVIMENTACOES,
                Modules.Login.LOGIN_BG, Modules.Login.LOGIN_LOGO,
                Modules.Dashboard.WELCOME_BG,
                Modules.Catalogo.PRODUTO_PLACEHOLDER, Modules.Catalogo.EMPTY_STATE,
                Modules.Inventario.EQUIPAMENTO_DEFAULT, Modules.Inventario.QR_CODE_PLACEHOLDER
        };
    }
}