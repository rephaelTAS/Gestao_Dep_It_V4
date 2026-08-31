package com.ossobo.gestaoDepIt.db.config;

import com.ossobo.gestaoDepIt.db.config.event.ConexaoEvent;
import com.ossobo.gestaoDepIt.db.config.event.ConexaoStatus;
import com.ossobo.gestaoDepIt.db.models.ConfigServidorRemoto;
import com.ossobo.gestaoDepIt.db.repositories.ConfigServidorRemotoRepository;
import com.ossobo.winterfx.anotations.Inject;
import com.ossobo.winterfx.anotations.PostConstruct;
import com.ossobo.winterfx.anotations.Service;
import com.ossobo.winterfx.anotations.Value;
import com.ossobo.winterfx.event.EventBus;
import com.ossobo.winterfx.router.model.ResponseData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * DatabaseConnection v3.2
 *
 * Responsabilidade: Gerenciar conexão dual SQLite/MySQL com fallback automático
 *
 * Padrões: Service + EventBus + Configuration
 *
 * v3.2 - Corrigido para usar Record (acesso direto aos campos) e Optional
 * v3.1 - Migração para WinterFX Nativo
 */
@Service
public class DatabaseConnection {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnection.class);

    // ============================================================
    // CONFIGURAÇÕES INJETADAS
    // ============================================================

    @Value("${database.sqlite.diretorio:dados}")
    private String sqliteDiretorio;

    @Value("${database.sqlite.arquivo:inventario.db}")
    private String sqliteArquivo;

    // ============================================================
    // DEPENDÊNCIAS INJETADAS
    // ============================================================

    @Inject
    private DatabaseConfig databaseConfig;

    @Inject
    private ConfigServidorRemotoRepository configRepository;

    @Inject
    private EventBus eventBus;  // ✅ WinterFX EventBus

    // ============================================================
    // ESTADO INTERNO
    // ============================================================

    private ConfigServidorRemoto configAtiva;
    private String jdbcUrl;
    private String usuario;
    private String senha;
    private final AtomicBoolean usandoRemoto = new AtomicBoolean(false);
    private volatile LocalDateTime ultimaMudanca;

    // ============================================================
    // INICIALIZAÇÃO
    // ============================================================

    @PostConstruct
    public void init() {
        logger.info("🔌 DatabaseConnection v3.2 inicializando...");
        carregarConfiguracaoAtiva();
        logger.info("✅ DatabaseConnection v3.2 pronta");
        logger.info("   Modo: {}", usandoRemoto.get() ? "REMOTO" : "SQLITE");
        logger.info("   SQLite: {}/{}", sqliteDiretorio, sqliteArquivo);
    }

    // ============================================================
    // CARREGAMENTO DA CONFIGURAÇÃO
    // ============================================================

    /**
     * Carrega a configuração ativa do repositório.
     * Notifica via EventBus em caso de mudança.
     */
    public synchronized void carregarConfiguracaoAtiva() {
        try {
            // ✅ CORRIGIDO: Optional<ConfigServidorRemoto> → ConfigServidorRemoto
            Optional<ConfigServidorRemoto> optConfig = configRepository.findAtivo();

            if (optConfig.isPresent()) {
                ConfigServidorRemoto novaConfig = optConfig.get();
                boolean mudou = configMudou(novaConfig);

                // Configura conexão remota
                this.configAtiva = novaConfig;
                this.jdbcUrl = novaConfig.gerarUrlConexao();
                // ✅ Record: acesso direto aos campos
                this.usuario = novaConfig.usuario();
                this.senha = novaConfig.senha();
                this.usandoRemoto.set(true);

                // Adiciona parâmetros extras
                String parametrosExtra = novaConfig.parametrosExtra();
                if (parametrosExtra != null && !parametrosExtra.isEmpty()) {
                    jdbcUrl += "?" + parseParams(parametrosExtra);
                }

                carregarDriver(novaConfig.tipoBanco());

                logger.info("✅ Configuração remota carregada: {} @ {}:{}",
                        novaConfig.nomeConfig(),
                        novaConfig.host(),
                        novaConfig.porta());

                // Notifica mudança
                if (mudou) {
                    notificarMudanca(ConexaoStatus.REMOTO);
                }

            } else {
                // Fallback para SQLite
                boolean mudou = this.usandoRemoto.get();
                usarFallbackSqlite();

                if (mudou) {
                    notificarMudanca(ConexaoStatus.SQLITE);
                }
            }

        } catch (Exception e) {
            logger.error("❌ Erro ao carregar configuração: {}", e.getMessage());
            usarFallbackSqlite();
            notificarMudanca(ConexaoStatus.ERRO);
        }
    }

    private boolean configMudou(ConfigServidorRemoto novaConfig) {
        if (configAtiva == null) return true;
        // ✅ Record: acesso direto aos campos
        return !configAtiva.host().equals(novaConfig.host())
                || !configAtiva.porta().equals(novaConfig.porta())
                || !configAtiva.databaseName().equals(novaConfig.databaseName());
    }

    private void usarFallbackSqlite() {
        this.configAtiva = null;
        this.jdbcUrl = databaseConfig.getDbUrl();
        this.usuario = null;
        this.senha = null;
        this.usandoRemoto.set(false);
        this.ultimaMudanca = LocalDateTime.now();

        logger.info("📁 Usando SQLite local: {}", databaseConfig.getDbPath());
    }

    private void carregarDriver(String tipoBanco) {
        try {
            String driverClass = switch (tipoBanco) {
                case "MYSQL", "MARIADB" -> "com.mysql.cj.jdbc.Driver";
                case "POSTGRESQL" -> "org.postgresql.Driver";
                case "SQLSERVER" -> "com.microsoft.sqlserver.jdbc.SQLServerDriver";
                case "ORACLE" -> "oracle.jdbc.OracleDriver";
                default -> null;
            };

            if (driverClass != null) {
                Class.forName(driverClass);
                logger.debug("✅ Driver {} carregado", driverClass);
            }

        } catch (ClassNotFoundException e) {
            logger.error("❌ Driver não encontrado para {}: {}", tipoBanco, e.getMessage());
        }
    }

    private String parseParams(String params) {
        if (params == null || params.isEmpty()) return "";
        return params.replace("{", "")
                .replace("}", "")
                .replace("\"", "")
                .replace(" ", "");
    }

    // ============================================================
    // MÉTODOS DE CONEXÃO
    // ============================================================

    /**
     * Obtém conexão com fallback automático.
     * Tenta remoto primeiro, se falhar usa SQLite.
     */
    public Connection getConnection() throws SQLException {
        if (usandoRemoto.get() && usuario != null && senha != null) {
            try {
                return DriverManager.getConnection(jdbcUrl, usuario, senha);
            } catch (SQLException e) {
                logger.warn("❌ Erro no remoto, fallback SQLite: {}", e.getMessage());
                return databaseConfig.getConnection();
            }
        }
        return databaseConfig.getConnection();
    }

    /**
     * Obtém conexão remota (sem fallback).
     */
    public Connection getConnectionRemota() throws SQLException {
        if (!usandoRemoto.get() || usuario == null || senha == null) {
            throw new SQLException("Nenhuma configuração remota ativa");
        }
        return DriverManager.getConnection(jdbcUrl, usuario, senha);
    }

    /**
     * Obtém conexão SQLite forçada.
     */
    public Connection getConnectionSqlite() throws SQLException {
        return databaseConfig.getConnection();
    }

    /**
     * Obtém conexão com gerenciamento automático (withConnection).
     */
    public <T> T withConnection(ConnectionAction<T> action) throws SQLException {
        Connection conn = getConnection();
        try {
            return action.execute(conn);
        } finally {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        }
    }

    @FunctionalInterface
    public interface ConnectionAction<T> {
        T execute(Connection conn) throws SQLException;
    }

    // ============================================================
    // MÉTODOS DE TESTE (COM RESPONSEDATA)
    // ============================================================

    /**
     * Testa a conexão ativa.
     */
    public ResponseData testarConexao() {
        try (Connection conn = getConnection()) {
            boolean isValid = conn.isValid(2);
            return ResponseData.success()
                    .withData("conectado", isValid)
                    .withData("modo", usandoRemoto.get() ? "REMOTO" : "SQLITE")
                    .withData("host", getHost())
                    .withData("database", getDatabase());
        } catch (SQLException e) {
            return ResponseData.error("Falha na conexão: " + e.getMessage())
                    .withData("conectado", false);
        }
    }

    /**
     * Testa especificamente a conexão remota.
     */
    public ResponseData testarConexaoRemota() {
        if (!usandoRemoto.get() || configAtiva == null) {
            return ResponseData.error("Nenhuma configuração remota ativa")
                    .withData("conectado", false);
        }

        try (Connection conn = getConnectionRemota()) {
            boolean isValid = conn.isValid(2);
            // ✅ Record: acesso direto aos campos
            return ResponseData.success()
                    .withData("conectado", isValid)
                    .withData("host", configAtiva.host())
                    .withData("database", configAtiva.databaseName())
                    .withData("usuario", configAtiva.usuario());
        } catch (SQLException e) {
            return ResponseData.error("Falha na conexão remota: " + e.getMessage())
                    .withData("conectado", false)
                    .withData("host", configAtiva != null ? configAtiva.host() : "n/a");
        }
    }

    // ============================================================
    // RECARREGAMENTO E CONTROLE
    // ============================================================

    /**
     * Recarrega configuração ativa do banco.
     */
    public synchronized ResponseData recarregarConfiguracao() {
        logger.info("🔄 Recarregando configuração...");
        carregarConfiguracaoAtiva();

        return ResponseData.success()
                .withData("modo", usandoRemoto.get() ? "REMOTO" : "SQLITE")
                .withData("host", getHost())
                .withData("database", getDatabase())
                .withData("mensagem", "Configuração recarregada");
    }

    /**
     * Força uso de SQLite.
     */
    public synchronized ResponseData forcarSqlite() {
        boolean estavaRemoto = usandoRemoto.get();
        usarFallbackSqlite();

        if (estavaRemoto) {
            notificarMudanca(ConexaoStatus.SQLITE);
        }

        return ResponseData.success()
                .withData("modo", "SQLITE")
                .withData("mensagem", "SQLite forçado com sucesso");
    }

    // ============================================================
    // NOTIFICAÇÕES VIA EVENTBUS (WINTERFX NATIVO)
    // ============================================================

    private void notificarMudanca(ConexaoStatus status) {
        this.ultimaMudanca = LocalDateTime.now();

        ConexaoEvent event = new ConexaoEvent(
                ConexaoEvent.Type.MUDOU,
                status,
                configAtiva,
                String.format("Conexão alterada para: %s", status.getDescricao())
        );

        eventBus.publish(event);
        logger.info("📢 Evento de conexão publicado: {}", status.getDescricao());
    }

    // ============================================================
    // GETTERS
    // ============================================================

    public boolean isUsandoRemoto() {
        return usandoRemoto.get();
    }

    public ConfigServidorRemoto getConfigAtiva() {
        return configAtiva;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    // ✅ CORRIGIDO: Acesso direto aos campos do Record
    public String getHost() {
        return configAtiva != null ? configAtiva.host() : "localhost";
    }

    public String getPorta() {
        return configAtiva != null ? configAtiva.porta() : "3306";
    }

    public String getDatabase() {
        return configAtiva != null ? configAtiva.databaseName() : "inventario.db";
    }

    public String getUsuario() {
        return configAtiva != null ? configAtiva.usuario() : null;
    }

    public LocalDateTime getUltimaMudanca() {
        return ultimaMudanca;
    }

    public String getStatusDescricao() {
        if (usandoRemoto.get() && configAtiva != null) {
            // ✅ Record: acesso direto aos campos
            return String.format("REMOTO: %s@%s:%s",
                    configAtiva.usuario(),
                    configAtiva.host(),
                    configAtiva.porta());
        }
        return "SQLITE: " + databaseConfig.getDbPath();
    }

    /**
     * Obtém o caminho do SQLite
     */
    public String getSqlitePath() {
        return databaseConfig.getDbPath();
    }
}