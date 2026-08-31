package com.ossobo.gestaoDepIt.db.config;

import com.ossobo.winterfx.anotations.Inject;
import com.ossobo.winterfx.anotations.PostConstruct;
import com.ossobo.winterfx.anotations.Value;
import com.ossobo.winterfx.di.annotations.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DatabaseConfig v2.1
 *
 * Responsabilidade: Fábrica de conexões SQLite local com configurações otimizadas.
 *                   Não gerencia ciclo de vida das conexões — cada chamada recebe
 *                   conexão nova e quem a obtém fecha via try-with-resources
 *                   ou withConnection().
 *
 * v2.1 - Remoção do pool de conexões artesanal (SQLite local: custo de conexão
 *        desprezível; pool sem retorno correto = overhead sem benefício)
 *      - @Bean removido de createConnection() (risco de registro singleton
 *        compartilhando UMA conexão entre threads — condição de corrida)
 *      - Dependências @Value do pool extintas (pool.tamanho.maximo, pool.timeout)
 *      - Interface pública essencial preservada: getConnection(), withConnection(),
 *        ConnectionAction, isAvailable(), shutdown(), getters de caminho/url
 * v2.0 - Migração para WinterFX (@Configuration, @Bean, @Value, pool)
 */
@Configuration()
public class DatabaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);

    // ============================================================
    // CONFIGURAÇÕES INJETADAS (@Value)
    // ============================================================

    @Value("${database.sqlite.diretorio:dados}")
    private String diretorio;

    @Value("${database.sqlite.arquivo:inventario.db}")
    private String arquivo;

    @Value("${database.sqlite.journal.mode:WAL}")
    private String journalMode;

    @Value("${database.sqlite.synchronous:NORMAL}")
    private String synchronousMode;

    // ============================================================
    // ESTADO INTERNO
    // ============================================================

    private String dbPath;
    private String dbUrl;
    private volatile boolean isShutdown = false;

    // ============================================================
    // INICIALIZAÇÃO
    // ============================================================

    /**
     * Define o caminho absoluto do banco, garante existência do diretório
     * e registra as configurações aplicadas.
     */
    @PostConstruct
    public void init() {
        this.dbPath = Paths.get(diretorio, arquivo).toAbsolutePath().toString();
        this.dbUrl = "jdbc:sqlite:" + dbPath;

        File dataDir = Paths.get(diretorio).toFile();
        if (!dataDir.exists()) {
            if (dataDir.mkdirs()) {
                logger.info("📁 Diretório criado: {}", dataDir.getAbsolutePath());
            } else {
                logger.warn("⚠️ Não foi possível criar diretório: {}", dataDir.getAbsolutePath());
            }
        }

        logger.info("📁 Banco SQLite: {}", dbPath);
        logger.info("   Journal Mode: {}", journalMode);
        logger.info("   Synchronous: {}", synchronousMode);
    }

    // ============================================================
    // CRIAÇÃO DE CONEXÃO
    // ============================================================

    /**
     * Cria uma nova conexão SQLite com pragmas otimizados aplicados.
     * Cada chamada retorna instância independente — sem compartilhamento.
     *
     * Nota: @Bean removido nesta versão (v2.1). Esta classe é invocada
     * diretamente por quem precisa de conexão, não pelo container DI.
     *
     * @return Connection configurada e pronta para uso
     * @throws SQLException em caso de falha de acesso/disco
     */
    public Connection createConnection() throws SQLException {
        if (isShutdown) {
            throw new SQLException("DatabaseConfig está encerrado");
        }

        Connection conn = DriverManager.getConnection(dbUrl);
        conn.setAutoCommit(true);

        // Pragmas são POR CONEXÃO no SQLite — aplicar sempre
        try (var stmt = conn.createStatement()) {
            stmt.execute(String.format("PRAGMA journal_mode=%s", journalMode));
            stmt.execute(String.format("PRAGMA synchronous=%s", synchronousMode));
            stmt.execute("PRAGMA cache_size=10000");
            stmt.execute("PRAGMA foreign_keys=ON");
        }

        logger.debug("🔌 Nova conexão SQLite criada");
        return conn;
    }

    /**
     * Alias de createConnection() mantido por compatibilidade com v2.x.
     * Repositories NÃO devem usar este método — usem
     * DatabaseConnection.withConnection() para respeitar roteamento dual.
     *
     * @return Connection nova
     * @throws SQLException em caso de falha
     */
    public Connection getConnection() throws SQLException {
        return createConnection();
    }

    // ============================================================
    // EXECUÇÃO GERENCIADA
    // ============================================================

    /**
     * Executa uma ação com conexão recém-criada, garantindo fechamento
     * mesmo em caso de exceção.
     *
     * @param action função a executar recebendo a Connection
     * @param <T> tipo do resultado
     * @return resultado da ação
     * @throws SQLException erros originados da ação ou da criação
     */
    public <T> T withConnection(ConnectionAction<T> action) throws SQLException {
        try (Connection conn = createConnection()) {
            return action.execute(conn);
        }
    }

    /**
     * Contrato funcional para ações com conexão gerenciada.
     */
    @FunctionalInterface
    public interface ConnectionAction<T> {
        T execute(Connection conn) throws SQLException;
    }

    /**
     * Testa rapidamente se o banco está acessível.
     *
     * @return true se consegue abrir conexão válida
     */
    public boolean isAvailable() {
        try (Connection conn = createConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            logger.warn("⚠️ SQLite indisponível: {}", e.getMessage());
            return false;
        }
    }

    // ============================================================
    // CONTROLE DE VIDA
    // ============================================================

    /**
     * Marca a configuração como encerrada. Novas tentativas de conexão
     * falham imediatamente. Conexões já entregues não são rastreadas —
     * sua liberação é responsabilidade dos chamadores (try-with-resources).
     */
    public void shutdown() {
        isShutdown = true;
        logger.info("🛑 DatabaseConfig encerrado (factory)");
    }

    // ============================================================
    // GETTERS
    // ============================================================

    public String getDbPath() {
        return dbPath;
    }

    public String getDbUrl() {
        return dbUrl;
    }

    public String getDiretorio() {
        return diretorio;
    }

    public String getArquivo() {
        return arquivo;
    }

    public boolean isShutdown() {
        return isShutdown;
    }
}