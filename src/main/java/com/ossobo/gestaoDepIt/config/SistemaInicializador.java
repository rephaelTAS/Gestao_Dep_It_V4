package com.ossobo.gestaoDepIt.config;

import com.ossobo.gestaoDepIt.db.config.ConexaoVerificador;
import com.ossobo.gestaoDepIt.db.config.DatabaseConnection;
import com.ossobo.gestaoDepIt.db.config.DatabaseInitializer;
import com.ossobo.gestaoDepIt.db.config.GerenciadorBanco;
import com.ossobo.gestaoDepIt.db.models.ConfigServidorRemoto;
import com.ossobo.gestaoDepIt.db.services.ConfigServidorRemotoService;
import com.ossobo.gestaoDepIt.db.sync.SincronizadorDados;

import com.ossobo.winterfx.anotations.Inject;
import com.ossobo.winterfx.anotations.PostConstruct;
import com.ossobo.winterfx.anotations.Service;
import com.ossobo.winterfx.router.model.ResponseData;

import javafx.application.Platform;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Optional;

/**
 * SistemaInicializador - Orquestra a inicialização completa do sistema
 * v2.0 - Migrado para WinterFX com injeção de dependências
 *
 * Responsabilidades:
 * - Ordenar a inicialização dos componentes
 * - Garantir que banco de dados esteja pronto antes da UI
 * - Configurar conexão remota se disponível
 * - Iniciar sincronizador em background
 * - Reportar progresso para Splash Screen
 */
@Service
public final class SistemaInicializador {

    private static final Logger logger = LoggerFactory.getLogger(SistemaInicializador.class);

    private static SistemaInicializador instance;

    // ============================================================
    // DEPENDÊNCIAS INJETADAS
    // ============================================================

    @Inject
    private DatabaseConnection databaseConnection;

    @Inject
    private GerenciadorBanco gerenciadorBanco;

    @Inject
    private ConexaoVerificador conexaoVerificador;

    @Inject
    private ConfigServidorRemotoService configService;

    @Inject
    private SincronizadorDados sincronizador;

    // ============================================================
    // ESTADO
    // ============================================================

    private boolean inicializado = false;
    private boolean usandoRemoto = false;
    private String statusConexao = "INICIALIZANDO...";
    private ProgressListener progressListener;

    // ============================================================
    // INTERFACE DE PROGRESSO
    // ============================================================

    public interface ProgressListener {
        void onProgress(int percent, String mensagem);
        void onConcluido(boolean usandoRemoto, String status);
        void onErro(String erro);
    }

    // ============================================================
    // SINGLETON
    // ============================================================

    private SistemaInicializador() {}

    @PostConstruct
    public void init() {
        logger.info("🔧 SistemaInicializador v2.0 inicializado via WinterFX");
        instance = this;
    }

    public static SistemaInicializador getInstance() {
        if (instance == null) {
            // Fallback - será instanciado via WinterFX
            instance = new SistemaInicializador();
        }
        return instance;
    }

    // ============================================================
    // INICIALIZAÇÃO PRINCIPAL
    // ============================================================

    /**
     * Executa toda a sequência de inicialização.
     * Este método é bloqueante e deve ser chamado antes da UI.
     *
     * @param listener Listener para reportar progresso (opcional)
     * @return true se inicialização foi bem-sucedida
     */
    public synchronized boolean inicializar(ProgressListener listener) {
        if (inicializado) {
            logger.warn("⚠️ Sistema já inicializado");
            return true;
        }

        this.progressListener = listener;

        try {
            // ============================================================
            // PASSO 1: INICIAR BANCO SQLITE (SEMPRE DISPONÍVEL)
            // ============================================================
            reportarProgresso(10, "📁 Inicializando banco de dados local...");
            logger.info("📁 PASSO 1: Inicializando SQLite...");

            try {
                DatabaseInitializer.initialize();
                logger.info("✅ SQLite inicializado com sucesso");
            } catch (SQLException e) {
                logger.error("❌ Falha ao inicializar SQLite: {}", e.getMessage());
                reportarErro("Falha ao inicializar banco local: " + e.getMessage());
                return false;
            }

            // ============================================================
            // PASSO 2: CARREGAR CONFIGURAÇÃO REMOTA
            // ============================================================
            reportarProgresso(30, "🔍 Verificando configuração remota...");
            logger.info("🔍 PASSO 2: Carregando configuração remota...");

            Optional<ConfigServidorRemoto> optConfig = Optional.empty();
            try {
                optConfig = configService.buscarAtiva();
            } catch (Exception e) {
                logger.warn("⚠️ Erro ao buscar configuração remota: {}", e.getMessage());
            }

            // ============================================================
            // PASSO 3: VERIFICAR CONEXÃO REMOTA
            // ============================================================
            if (optConfig.isPresent()) {
                ConfigServidorRemoto configAtiva = optConfig.get();

                if (configAtiva.isAtivo()) {
                    reportarProgresso(50, "🌐 Testando conexão com servidor remoto...");
                    // ✅ Record: acesso direto aos campos
                    logger.info("🌐 PASSO 3: Testando conexão com {}:{}",
                            configAtiva.host(), configAtiva.porta());

                    ResponseData status = conexaoVerificador.verificarConexaoCompleta(configAtiva);

                    if (status.isSuccess()) {
                        // ============================================================
                        // PASSO 4a: CONEXÃO REMOTA OK
                        // ============================================================
                        usandoRemoto = true;
                        statusConexao = "CONECTADO";

                        // Atualiza DatabaseConnection para usar remoto
                        databaseConnection.recarregarConfiguracao();

                        // ✅ Record: acesso direto aos campos
                        logger.info("✅ Conexão remota estabelecida: {}@{}:{}/{}",
                                configAtiva.usuario(),
                                configAtiva.host(),
                                configAtiva.porta(),
                                configAtiva.databaseName());

                        reportarProgresso(70, "✅ Conectado ao servidor remoto!");

                    } else {
                        // ============================================================
                        // PASSO 4b: CONEXÃO REMOTA FALHOU
                        // ============================================================
                        usandoRemoto = false;
                        statusConexao = "OFFLINE";

                        // Força SQLite
                        databaseConnection.forcarSqlite();

                        logger.warn("⚠️ Conexão remota falhou. Usando SQLite local.");

                        reportarProgresso(60, "⚠️ Servidor indisponível. Usando modo offline.");
                    }
                } else {
                    // Configuração inativa
                    usandoRemoto = false;
                    statusConexao = "SQLITE_PADRAO";
                    databaseConnection.forcarSqlite();
                    logger.info("📁 Configuração remota inativa. Usando SQLite local.");
                    reportarProgresso(60, "📁 Usando banco de dados local (SQLite)");
                }

            } else {
                // ============================================================
                // PASSO 3b: SEM CONFIGURAÇÃO REMOTA
                // ============================================================
                usandoRemoto = false;
                statusConexao = "SQLITE_PADRAO";

                // Garante SQLite
                databaseConnection.forcarSqlite();

                logger.info("📁 Nenhuma configuração remota. Usando SQLite local.");

                reportarProgresso(60, "📁 Usando banco de dados local (SQLite)");
            }

            // ============================================================
            // PASSO 5: INICIAR SINCRONIZADOR
            // ============================================================
            reportarProgresso(85, "🔄 Iniciando sincronizador de dados...");
            logger.info("🔄 PASSO 5: Iniciando SincronizadorDados...");

            sincronizador.iniciar();

            // ============================================================
            // PASSO 6: VERIFICAR SE HÁ DADOS PENDENTES
            // ============================================================
            if (usandoRemoto) {
                boolean temPendentes = sincronizador.temDadosPendentes();
                if (temPendentes) {
                    logger.info("📤 Dados pendentes detectados. Sincronizando...");
                    reportarProgresso(92, "📤 Sincronizando dados pendentes...");

                    // Executa sincronização inicial em background
                    new Thread(() -> {
                        try {
                            sincronizador.sincronizarPendentes();
                        } catch (Exception e) {
                            logger.error("❌ Erro na sincronização inicial: {}", e.getMessage());
                        }
                    }, "Sync-Inicial").start();
                } else {
                    logger.info("✅ Nenhum dado pendente para sincronizar");
                }
            }

            // ============================================================
            // PASSO 7: INICIALIZAÇÃO CONCLUÍDA
            // ============================================================
            inicializado = true;

            String mensagemFinal = usandoRemoto ?
                    "🌐 Sistema online - Conectado ao servidor" :
                    "📁 Sistema offline - Usando SQLite local";

            logger.info("✅ {}", mensagemFinal);
            reportarProgresso(100, mensagemFinal);

            if (progressListener != null) {
                progressListener.onConcluido(usandoRemoto, statusConexao);
            }

            return true;

        } catch (Exception e) {
            logger.error("❌ Erro crítico na inicialização: {}", e.getMessage(), e);
            reportarErro("Erro na inicialização: " + e.getMessage());
            return false;
        }
    }

    // ============================================================
    // MÉTODOS DE REPORTE
    // ============================================================

    private void reportarProgresso(int percent, String mensagem) {
        logger.info("  [{}%] {}", percent, mensagem);
        if (progressListener != null) {
            Platform.runLater(() -> {
                progressListener.onProgress(percent, mensagem);
            });
        }
    }

    private void reportarErro(String erro) {
        logger.error("❌ {}", erro);
        if (progressListener != null) {
            Platform.runLater(() -> {
                progressListener.onErro(erro);
            });
        }
    }

    // ============================================================
    // GETTERS
    // ============================================================

    public boolean isInicializado() {
        return inicializado;
    }

    public boolean isUsandoRemoto() {
        return usandoRemoto;
    }

    public String getStatusConexao() {
        return statusConexao;
    }

    public String getDescricaoConexao() {
        if (usandoRemoto) {
            ConfigServidorRemoto config = databaseConnection.getConfigAtiva();
            if (config != null) {
                // ✅ Record: acesso direto aos campos
                return String.format("🌐 %s@%s:%s/%s",
                        config.usuario(),
                        config.host(),
                        config.porta(),
                        config.databaseName());
            }
            return "🌐 Servidor remoto";
        }
        return "📁 SQLite Local";
    }
}