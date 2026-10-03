package com.ossobo.gestaoDepIt.db.config;

import com.ossobo.winterfx.anotations.Component;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * DatabaseConnection v1.1
 *
 * Responsabilidade: Resolver o caminho do banco por SO, garantir o diretório
 *                   de dados e abrir conexões SQLite com os pragmas corretos
 *                   (foreign_keys=ON, busy_timeout=5000).
 *
 * v1.1 — Transações explícitas para operações compostas entre domínios:
 *        - beginTransaction() → getConnection + setAutoCommit(false)
 *        - commit(conn)        → commit + close
 *        - rollback(conn)      → rollback + close (tolerante a conn já fechada)
 *
 *        Contrato: quem chama beginTransaction() é dono do ciclo e DEVE fechar
 *        via commit() ou rollback(). Métodos que recebem Connection externa
 *        (overloads nos repositories) NÃO fecham a conexão.
 */
@Component
public final class DatabaseConnection {

    private static final Logger LOGGER = System.getLogger(DatabaseConnection.class.getName());
    private static final String APP_NAME = "GestaoDepIt";
    private static final String DB_FILE = "inventario.db";

    private final Path dbFile;
    private final String databaseUrl;

    public DatabaseConnection() {
        Path baseDir = resolveBaseDirectory();
        this.dbFile = baseDir.resolve(DB_FILE);
        this.databaseUrl = "jdbc:sqlite:" + dbFile;

        LOGGER.log(Level.INFO, "📁 Banco de dados: {0}", dbFile);
        LOGGER.log(Level.INFO, "🖥️  SO: {0}", System.getProperty("os.name"));
    }

    /**
     * Resolve o diretório de dados por SO, seguindo convenções modernas:
     *   Linux  → $XDG_DATA_HOME/GestaoDepIt (fallback: ~/.local/share/GestaoDepIt)
     *   macOS  → ~/Library/Application Support/GestaoDepIt
     *   Windows→ %APPDATA%\GestaoDepIt (fallback: ~\AppData\Roaming\GestaoDepIt)
     */
    private static Path resolveBaseDirectory() {
        String os = System.getProperty("os.name").toLowerCase();
        String userHome = System.getProperty("user.home");

        Path base;
        if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            base = (appData != null && !appData.isBlank())
                    ? Paths.get(appData, APP_NAME)
                    : Paths.get(userHome, "AppData", "Roaming", APP_NAME);
        } else if (os.contains("mac")) {
            base = Paths.get(userHome, "Library", "Application Support", APP_NAME);
        } else {
            // Linux/Unix: prioriza XDG_DATA_HOME, fallback para ~/.local/share
            String xdg = System.getenv("XDG_DATA_HOME");
            base = (xdg != null && !xdg.isBlank())
                    ? Paths.get(xdg, APP_NAME)
                    : Paths.get(userHome, ".local", "share", APP_NAME);
        }
        return base;
    }

    /**
     * Garante que o diretório de dados existe e é gravável.
     * Log apenas na criação real (evita spam por conexão).
     */
    public void ensureDataDirectory() {
        try {
            Path parent = dbFile.getParent();
            boolean criou = !Files.exists(parent);
            Files.createDirectories(parent);
            if (!Files.isWritable(parent)) {
                throw new IllegalStateException("Sem permissão de escrita em: " + parent);
            }
            if (criou) {
                LOGGER.log(Level.INFO, "📁 Diretório de dados criado: {0}", parent);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao preparar diretório de dados: " + dbFile, e);
        }
    }

    /**
     * Abre conexão garantindo:
     *   1. Diretório existente (SQLite cria o ARQUIVO, jamais os diretórios pais)
     *   2. foreign_keys = ON → default OFF em CADA conexão do SQLite; sem isto,
     *      RESTRICT/CASCADE/SET NULL do DDL v2 são decorativos
     *   3. busy_timeout → espera 5s por lock em vez de falhar com SQLITE_BUSY
     */
    public Connection getConnection() throws SQLException {
        ensureDataDirectory();
        Connection conn = DriverManager.getConnection(databaseUrl);
        try (Statement st = conn.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");
            st.execute("PRAGMA busy_timeout = 5000");
        }
        return conn;
    }

    // ============================================================
    // TRANSAÇÕES EXPLÍCITAS (v1.1)
    // ============================================================

    /**
     * Abre conexão com auto-commit desligado — o chamador é dono do ciclo
     * transacional e DEVE fechar via {@link #commit(Connection)} ou
     * {@link #rollback(Connection)}.
     *
     * Usado por operações compostas entre domínios (ex.: inventário + histórico),
     * garantindo atomicidade real: ou os dois gravam, ou nenhum grava.
     */
    public Connection beginTransaction() throws SQLException {
        Connection conn = getConnection();
        conn.setAutoCommit(false);
        return conn;
    }

    /**
     * Comita a transação e fecha a conexão.
     * Se o commit falhar, tenta rollback defensivo antes de propagar a exceção.
     */
    public void commit(Connection conn) throws SQLException {
        if (conn == null) return;
        try {
            conn.commit();
        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ignored) { /* best-effort */ }
            throw e;
        } finally {
            try { conn.close(); } catch (SQLException ignored) { /* best-effort */ }
        }
    }

    /**
     * Desfaz a transação e fecha a conexão.
     * Tolerante: conexão já fechada ou nula não propaga erro — rollback é
     * caminho de exceção, nunca deve mascarar a exceção original.
     */
    public void rollback(Connection conn) {
        if (conn == null) return;
        try {
            conn.rollback();
        } catch (SQLException ignored) {
            // best-effort: rollback é caminho de falha, não deve propagar
        } finally {
            try { conn.close(); } catch (SQLException ignored) { /* best-effort */ }
        }
    }
}