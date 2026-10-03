package com.ossobo.gestaoDepIt.config;

/**
 * ViewConstant v2.0
 *
 * Fonte única dos IDs de view registrados via @RegisterView e usados
 * pelos @FloatingWindow / @NewScene / Rotas.ui(...).
 *
 * Organização por MÓDULO. Cada módulo agrupa o seu trio:
 *   LIST    → tela de listagem (raiz do módulo)
 *   FORM    → tela de criação/edição (janela flutuante modal)
 *   DETAIL  → tela de detalhes (janela flutuante modal)
 *
 * Módulos transversais (Dashboard, Login, Main, Definições, Sobre)
 * expõem apenas a view raiz.
 *
 * @since v1.0
 */
public final class ViewConstant {

    private ViewConstant() { }

    // ============================================================
    // MAIN / SHELL — views raiz da aplicação
    // ============================================================
    public static final class Main {
        public static final String LOGIN   = "loginform";
        public static final String MAIN    = "main";
        public static final String DASHBOARD = "dashboard";
        public static final String NOTIFICATION = "notificação";
        public static final String DEFINICOES = "definicoes";
        public static final String SOBRE = "sobre";
        public static final String TIME_REPORT = "time";
        public static final String RELATORIO = "relatorio";
        public static final String VISUALIZAR = "visualizar";

        private Main() { }
    }

    // ============================================================
    // CATÁLOGO DE PRODUTOS
    // ============================================================
    public static final class Catalogo {
        public static final String LIST   = "catalogo";
        public static final String FORM   = "adiciona_editar_produto";
        public static final String DETAIL = "detalhe_produto";

        private Catalogo() { }
    }

    // ============================================================
    // INVENTÁRIO DE EQUIPAMENTOS
    // ============================================================
    public static final class Inventario {
        public static final String LIST   = "inventario";
        public static final String FORM   = "addinventario_editarInventario";
        public static final String DETAIL = "inventarioDetails";

        /** View do ledger de eventos (listagem + filtros). */
        public static final String HISTORICO_LIST = "historicoevento";
        /** View de detalhes de um evento do ledger. */
        public static final String HISTORICO_DETAIL = "detalhesHistoricoevento";
        public static final String GERAR_NUM_SERIE = "gerarnumserie";

        private Inventario() { }
    }

    public static final class HistoricoEvento {
        public static final String LIST = "historicoevento";
        public static final String FORM = "historicoeventoform";
    }

        // ============================================================
    // ESTOQUE (Produto Stock + Movimentações)
    // ============================================================
    public static final class Estoque {
        public static final String PRODUTO_STOCK        = "produto_stock";
        public static final String MOVIMENTACOES_LIST   = "movimentacoes";
        public static final String MOVIMENTACOES_FORM   = "addeditamovimenta";
        public static final String MOVIMENTACAO_DETAIL  = "detalhesMovimentacao";
        public static final String SELECAO_PRODUTO      = "produto-selection-dialog";

        private Estoque() { }
    }

    // ============================================================
    // FUNCIONÁRIOS
    // ============================================================
    public static final class Funcionario {
        public static final String LIST   = "funcionario";
        public static final String FORM   = "addeditfuncionario";
        public static final String DETAIL = "detalhesfuncionario";

        private Funcionario() { }
    }

    // ============================================================
    // USUÁRIOS
    // ============================================================
    public static final class Usuario {
        public static final String LIST   = "usuario";
        public static final String DETAIL = "detalheusuario";
        // FORM: ainda não definido — usar quando existir
        // public static final String FORM = "adicionar_editar_usuario";

        private Usuario() { }
    }

    // ============================================================
    // TONERS
    // ============================================================
    public static final class Toner {
        public static final String LIST   = "listartoner";
        public static final String FORM   = "addeditartoner";
        public static final String DETAIL = "detalhestoner";
        public static final String DASHBOARD = "tonerdashboard";

        private Toner() { }
    }

    // ============================================================
    // SERVIDOR REMOTO (config)
    // ============================================================
    public static final class Server {
        public static final String LIST   = "servidor_list";
        public static final String FORM   = "servidor_form";
        public static final String DETAIL = "servidor_detail";

        private Server() { }
    }

    // ============================================================
    // RELATÓRIOS
    // ============================================================
    public static final class Relatorio {
        public static final String DASHBOARD    = "dashboardRelatorio";
        public static final String SIMPLIFICADO = "relasimples";
        public static final String AVANCADO     = "relaavancado";
        private Relatorio() { }
    }

    // ============================================================
    // GENÉRICOS / AUXILIARES
    // ============================================================
    public static final class Aux {
        public static final String ADD_ITEM = "add_item";

        private Aux() { }
    }
}