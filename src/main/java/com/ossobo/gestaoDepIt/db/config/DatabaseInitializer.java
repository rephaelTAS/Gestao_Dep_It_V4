package com.ossobo.gestaoDepIt.db.config;

import com.ossobo.winterfx.anotations.Component;
import com.ossobo.winterfx.anotations.Inject;
import com.ossobo.winterfx.anotations.PostConstruct;
import org.mindrot.jbcrypt.BCrypt;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
/**
 * DatabaseInitializer v2.2
 *
 * Inicialização do banco SQLite local: PRAGMAs, schema (idempotente)
 * e seed do par guest. Roda uma única vez via @PostConstruct.
 *
 * v2.1 — Correções estruturais no executor de DDL:
 *   1. Splitter de statements que respeita comentários (-- e /* *​/) e
 *      strings ('...' e "...") — antes split(";") quebrava em qualquer ';'
 *      dentro de comentário ou literal.
 *   2. Marcação incremental de user_version.
 *   3. Log individual por statement.
 *   4. Leitura de recurso com remoção de BOM UTF-8.
 *
 * v2.2 — Transação dos scripts de bootstrap controlada via JDBC:
 *   - O executarScriptSePendente agora abre/fecha a transação ele mesmo,
 *     ignorando os BEGIN TRANSACTION/COMMIT internos dos scripts SQL.
 *   - Rollback explícito em caso de falha — nunca deixa transação aberta,
 *     evitando o bug de "script reporta sucesso mas não commita".
 *   - Autocommit resetado antes de cada script (imune a estado residual).
 */
@Component
public final class DatabaseInitializer {

    private static final Logger LOGGER = System.getLogger(DatabaseInitializer.class.getName());

    private static final String SCHEMA_RESOURCE = "/db/schema_v2.sql";
    private static final int SCHEMA_VERSION_TARGET = 2;

    // =========================================================================
// BOOTSTRAP DE SCRIPTS SQL — controle de idempotência
// =========================================================================

    /** Tabela de controle: 1 linha por script já aplicado. */
    private static final String BOOTSTRAP_TABLE = "_bootstrap_scripts";

    /**
     * Scripts de bootstrap, NA ORDEM de execução.
     * Cada um é aplicado UMA ÚNICA VEZ (registrado em _bootstrap_scripts).
     * Para re-importar: DELETE FROM _bootstrap_scripts WHERE nome = '...';
     * ou apague a tabela inteira (ver resetBootstrap()).
     */
    private static final List<String> SCRIPTS_BOOTSTRAP = List.of(
            "/db/insert_catalogo.sql",
            "/db/insert_funcionarios.sql",
            "/db/insert_inventario.sql",
            "/db/HISTORICO_EVENTOS.sql"
    );

    @Inject
    private DatabaseConnection databaseConnection;

    @PostConstruct
    public void init() {
        LOGGER.log(Level.INFO, "📦 Inicializando banco de dados...");

        try (Connection conn = databaseConnection.getConnection()) {
            configurePragmas(conn);
            createTables(conn);
            seedInitialData(conn);
            executarBootstrapScripts(conn);

            LOGGER.log(Level.INFO, "✅ Banco de dados inicializado com sucesso!");
        } catch (SQLException e) {
            LOGGER.log(Level.ERROR, "❌ Erro ao inicializar banco de dados", e);
            throw new RuntimeException("Falha na inicialização do banco", e);
        }
    }

    // =========================================================================
    // PRAGMAS
    // =========================================================================

    private void configurePragmas(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("PRAGMA journal_mode=WAL")) {
            if (rs.next()) {
                LOGGER.log(Level.INFO, "✅ journal_mode = {0}", rs.getString(1));
            }
        }
        try (Statement st = conn.createStatement()) {
            st.execute("PRAGMA synchronous = NORMAL");
        }
    }

    // =========================================================================
    // SCHEMA — execução idempotente e incremental
    // =========================================================================

    private void createTables(Connection conn) throws SQLException {
        int versionAtual = lerUserVersion(conn);

        if (versionAtual >= SCHEMA_VERSION_TARGET) {
            LOGGER.log(Level.INFO, "✅ Schema v{0} já aplicado (user_version={1})",
                    SCHEMA_VERSION_TARGET, versionAtual);
            return;
        }

        String schema = readClasspathResource(SCHEMA_RESOURCE);
        List<String> statements = splitSqlStatements(schema);

        LOGGER.log(Level.INFO, "📜 Executando {0} statement(s) de {1} (user_version atual={2})",
                statements.size(), SCHEMA_RESOURCE, versionAtual);

        int executados = 0;
        for (int i = 0; i < statements.size(); i++) {
            String sql = statements.get(i);
            String rotulo = rotuloDoStatement(sql);

            try (Statement st = conn.createStatement()) {
                st.execute(sql);
                executados++;

                if (ehCreateTable(sql)) {
                    LOGGER.log(Level.INFO, "  ✅ [{0}/{1}] {2}",
                            i + 1, statements.size(), rotulo);
                }
            } catch (SQLException e) {
                LOGGER.log(Level.ERROR,
                        "❌ Falha no statement [{0}/{1}] — {2}\nSQL:\n{3}",
                        i + 1, statements.size(), rotulo, sql);
                throw new SQLException(
                        "Falha ao executar statement " + (i + 1) + "/" + statements.size()
                                + " (" + rotulo + "): " + e.getMessage()
                                + "\nSQL:\n" + sql,
                        e);
            }
        }

        try (Statement st = conn.createStatement()) {
            st.execute("PRAGMA user_version = " + SCHEMA_VERSION_TARGET);
        }

        LOGGER.log(Level.INFO, "✅ Schema v{0} aplicado ({1} statement(s) executado(s))",
                SCHEMA_VERSION_TARGET, executados);
    }

    private int lerUserVersion(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("PRAGMA user_version")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    // =========================================================================
    // SPLITTER DE SQL — respeita comentários e strings
    // =========================================================================

    /**
     * Divide o script SQL em statements, respeitando:
     *   - Comentários de linha:      -- até fim de linha
     *   - Comentários de bloco:      /* ... *​/
     *   - Strings com aspas simples: '...'  (com escape '' dentro)
     *   - Strings com aspas duplas:  "..."  (com escape "" dentro)
     */
    static List<String> splitSqlStatements(String script) {
        List<String> out = new ArrayList<>();
        if (script == null) return out;

        StringBuilder atual = new StringBuilder();
        int i = 0;
        int n = script.length();

        while (i < n) {
            char c = script.charAt(i);
            char next = (i + 1 < n) ? script.charAt(i + 1) : '\0';

            // Comentário de linha
            if (c == '-' && next == '-') {
                int fim = script.indexOf('\n', i);
                i = (fim < 0) ? n : fim + 1;
                continue;
            }

            // Comentário de bloco
            if (c == '/' && next == '*') {
                int fim = script.indexOf("*/", i + 2);
                i = (fim < 0) ? n : fim + 2;
                continue;
            }

            // String com aspas simples
            if (c == '\'') {
                atual.append(c);
                i++;
                while (i < n) {
                    char cur = script.charAt(i);
                    atual.append(cur);
                    if (cur == '\'') {
                        if (i + 1 < n && script.charAt(i + 1) == '\'') {
                            atual.append('\'');
                            i += 2;
                            continue;
                        }
                        break;
                    }
                    i++;
                }
                i++;
                continue;
            }

            // String com aspas duplas
            if (c == '"') {
                atual.append(c);
                i++;
                while (i < n) {
                    char cur = script.charAt(i);
                    atual.append(cur);
                    if (cur == '"') {
                        if (i + 1 < n && script.charAt(i + 1) == '"') {
                            atual.append('"');
                            i += 2;
                            continue;
                        }
                        break;
                    }
                    i++;
                }
                i++;
                continue;
            }

            // Fim de statement
            if (c == ';') {
                String stmt = atual.toString().trim();
                if (!stmt.isEmpty()) {
                    out.add(stmt);
                }
                atual.setLength(0);
                i++;
                continue;
            }

            atual.append(c);
            i++;
        }

        String ultimo = atual.toString().trim();
        if (!ultimo.isEmpty()) {
            out.add(ultimo);
        }

        return out;
    }

    /** Rótulo humano-legível do statement (nome da tabela/índice) para log. */
    private String rotuloDoStatement(String sql) {
        String head = sql.trim().toUpperCase();
        if (head.startsWith("CREATE TABLE")) {
            return "CREATE TABLE " + extrairNomeIdentificador(sql, "CREATE TABLE");
        }
        if (head.startsWith("CREATE INDEX") || head.startsWith("CREATE UNIQUE INDEX")) {
            String[] partes = sql.trim().split("\\s+");
            return "CREATE INDEX " + (partes.length >= 3 ? partes[partes.length - 1] : "?");
        }
        if (head.startsWith("ALTER TABLE")) {
            return "ALTER TABLE " + extrairNomeIdentificador(sql, "ALTER TABLE");
        }
        if (head.startsWith("INSERT"))  return "INSERT";
        if (head.startsWith("PRAGMA"))  return "PRAGMA";
        String primeiraLinha = sql.trim().split("\\R", 2)[0];
        return primeiraLinha.length() > 60 ? primeiraLinha.substring(0, 57) + "..." : primeiraLinha;
    }

    private boolean ehCreateTable(String sql) {
        return sql.trim().toUpperCase().startsWith("CREATE TABLE");
    }

    private String extrairNomeIdentificador(String sql, String prefixo) {
        String resto = sql.trim().substring(prefixo.length()).trim();
        if (resto.toUpperCase().startsWith("IF NOT EXISTS")) {
            resto = resto.substring("IF NOT EXISTS".length()).trim();
        }
        int fim = 0;
        while (fim < resto.length()) {
            char c = resto.charAt(fim);
            if (Character.isLetterOrDigit(c) || c == '_' || c == '.') fim++;
            else break;
        }
        return resto.substring(0, fim);
    }

    // =========================================================================
    // SEED GUEST
    // =========================================================================

    private void seedInitialData(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT 1 FROM usuarios WHERE id = ? LIMIT 1")) {
            ps.setString(1, GuestConstants.GUEST_ID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    LOGGER.log(Level.INFO, "✅ Usuário guest já existe");
                    return;
                }
            }
        }

        String guestPassword = System.getProperty("guest.password", "guest123");
        String hash = BCrypt.hashpw(guestPassword, BCrypt.gensalt());

        conn.setAutoCommit(false);
        try {
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO funcionarios (cod_dep, nome, departamento, funcao, ativo) " +
                            "VALUES (?, ?, ?, ?, ?)")) {
                ps.setString(1, GuestConstants.GUEST_FUNC_ID);
                ps.setString(2, GuestConstants.GUEST_FUNC_NAME);
                ps.setString(3, GuestConstants.GUEST_DEPARTMENT);
                ps.setString(4, GuestConstants.GUEST_ROLE);
                ps.setInt(5, 1);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO usuarios (id, funcionario_id, nome, email, senha_hash, nivel_acesso, ativo) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                ps.setString(1, GuestConstants.GUEST_ID);
                ps.setString(2, GuestConstants.GUEST_FUNC_ID);
                ps.setString(3, GuestConstants.GUEST_NAME);
                ps.setString(4, GuestConstants.GUEST_EMAIL);
                ps.setString(5, hash);
                ps.setString(6, GuestConstants.GUEST_LEVEL);
                ps.setInt(7, 1);
                ps.executeUpdate();
            }

            conn.commit();
            LOGGER.log(Level.INFO, "✅ Usuário guest criado com sucesso!");

        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    // =========================================================================
    // LEITURA DE RECURSO
    // =========================================================================

    /**
     * Lê o recurso como UTF-8 e remove BOM se presente.
     */
    private String readClasspathResource(String path) {
        try (var is = getClass().getResourceAsStream(path)) {
            if (is == null) {
                throw new IllegalStateException(
                        "Recurso não encontrado no classpath: " + path
                                + " — verifique se schema_v2.sql está em src/main/resources/db/");
            }
            byte[] bytes = is.readAllBytes();

            // Remove BOM UTF-8 se presente
            if (bytes.length >= 3
                    && (bytes[0] & 0xFF) == 0xEF
                    && (bytes[1] & 0xFF) == 0xBB
                    && (bytes[2] & 0xFF) == 0xBF) {
                byte[] semBom = new byte[bytes.length - 3];
                System.arraycopy(bytes, 3, semBom, 0, semBom.length);
                bytes = semBom;
                LOGGER.log(Level.WARNING, "⚠️ BOM UTF-8 detectado em {0} — removido", path);
            }

            return new String(bytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao ler recurso: " + path, e);
        }
    }


    // =========================================================================
// BOOTSTRAP DE SCRIPTS SQL
// =========================================================================

    /**
     * Executa os scripts de bootstrap pendentes, na ordem de SCRIPTS_BOOTSTRAP.
     *
     * - Idempotente: cada script é aplicado 1x (controle em _bootstrap_scripts).
     * - Transação controlada via JDBC (ignora BEGIN/COMMIT internos do script).
     * - Rollback automático em caso de falha — nunca deixa transação aberta.
     * - Autocommit resetado antes de cada script.
     *
     * Para re-importar um script específico:
     *   DELETE FROM _bootstrap_scripts WHERE nome = '/db/insert_catalogo.sql';
     *
     * Para re-importar TODOS (apaga tudo):
     *   ver método estático resetBootstrap(Connection).
     */
    private void executarBootstrapScripts(Connection conn) throws SQLException {
        garantirTabelaBootstrap(conn);

        Set<String> aplicados = lerScriptsAplicados(conn);

        List<String> pendentes = SCRIPTS_BOOTSTRAP.stream()
                .filter(s -> !aplicados.contains(s))
                .toList();

        if (pendentes.isEmpty()) {
            LOGGER.log(Level.INFO, "✅ Todos os {0} scripts de bootstrap já aplicados",
                    SCRIPTS_BOOTSTRAP.size());
            return;
        }

        LOGGER.log(Level.INFO, "📜 Executando {0} script(s) de bootstrap pendente(s)",
                pendentes.size());

        for (String recurso : pendentes) {
            executarScriptSePendente(conn, recurso);
        }
    }

    /**
     * Executa um único script, registrando-o em _bootstrap_scripts ao final.
     * Tudo dentro de UMA transação JDBC — se falhar, rollback completo.
     */
    private void executarScriptSePendente(Connection conn, String recurso) throws SQLException {
        String sql = readClasspathResource(recurso);
        List<String> statements = splitSqlStatements(sql);

        LOGGER.log(Level.INFO, "📜 [{0}] {1} statement(s)", recurso, statements.size());

        // Estado limpo: nunca herda autocommit de outro trecho
        conn.setAutoCommit(true);

        try {
            conn.setAutoCommit(false);

            int executados = 0;
            for (int i = 0; i < statements.size(); i++) {
                String stmt = statements.get(i);
                try (Statement st = conn.createStatement()) {
                    st.execute(stmt);
                    executados++;
                } catch (SQLException e) {
                    LOGGER.log(Level.ERROR,
                            "❌ Falha [{0}] no statement {1}/{2}\nSQL:\n{3}",
                            recurso, i + 1, statements.size(), stmt);
                    throw e;
                }
            }

            // Registra como aplicado (mesma transação → atômico)
            registrarScriptAplicado(conn, recurso);

            conn.commit();
            LOGGER.log(Level.INFO, "✅ [{0}] {1} statement(s) executado(s) e registrado(s)",
                    recurso, executados);

        } catch (SQLException e) {
            try {
                conn.rollback();
                LOGGER.log(Level.WARNING, "↩️ Rollback aplicado em {0}", recurso);
            } catch (SQLException rb) {
                LOGGER.log(Level.ERROR, "❌ Falha no rollback de " + recurso, rb);
            }
            throw new SQLException(
                    "Falha ao executar bootstrap " + recurso + ": " + e.getMessage(), e);
        } finally {
            conn.setAutoCommit(true);
        }
    }

// =========================================================================
// TABELA DE CONTROLE
// =========================================================================

    private void garantirTabelaBootstrap(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute("""
            CREATE TABLE IF NOT EXISTS %s (
                nome        TEXT PRIMARY KEY,
                aplicado_em TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
            )
            """.formatted(BOOTSTRAP_TABLE));
        }
    }

    private Set<String> lerScriptsAplicados(Connection conn) throws SQLException {
        Set<String> out = new HashSet<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT nome FROM " + BOOTSTRAP_TABLE)) {
            while (rs.next()) {
                out.add(rs.getString(1));
            }
        }
        return out;
    }

    private void registrarScriptAplicado(Connection conn, String nome) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT OR IGNORE INTO " + BOOTSTRAP_TABLE + " (nome) VALUES (?)")) {
            ps.setString(1, nome);
            ps.executeUpdate();
        }
    }

// =========================================================================
// RESET — apaga o controle de bootstrap
// =========================================================================

    /**
     * Remove o controle de bootstrap (opcionalmente de UM script específico).
     *
     * Uso típico:
     *   - resetBootstrap(conn, null)            → todos os scripts rodarão de novo
     *   - resetBootstrap(conn, "/db/insert_inventario.sql") → só esse rodará
     *
     * IMPORTANTE: os dados em si NÃO são apagados — apenas o registro de "já apliquei".
     * Como os scripts usam INSERT OR IGNORE, rodar de novo NÃO duplica.
     * Se quiser apagar os dados também, faça DELETE das tabelas antes.
     */
    public static void resetBootstrap(Connection conn, String nomeScript) throws SQLException {
        try (Statement st = conn.createStatement()) {
            if (nomeScript == null) {
                st.execute("DROP TABLE IF EXISTS " + BOOTSTRAP_TABLE);
            } else {
                try (PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM " + BOOTSTRAP_TABLE + " WHERE nome = ?")) {
                    ps.setString(1, nomeScript);
                    ps.executeUpdate();
                }
            }
        }
    }
}