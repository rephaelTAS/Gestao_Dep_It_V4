package com.ossobo.gestaoDepIt.db.config;

import com.ossobo.winterfx.anotations.Inject;
import com.ossobo.winterfx.anotations.PostConstruct;
import com.ossobo.winterfx.anotations.Service;
import com.ossobo.winterfx.anotations.Value;
import com.ossobo.winterfx.router.model.ResponseData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * DatabaseInitializer v2.0
 *
 * Responsabilidade: Inicializar e versionar o banco de dados SQLite
 *
 * Padrões: Service + Migration + Versionamento
 *
 * v2.0 - Migração para WinterFX
 * - @Service para injeção e gerenciamento
 * - @PostConstruct para inicialização automática
 * - @Value para configurações externalizadas
 * - Suporte a migrações versionadas
 * - ResponseData para respostas padronizadas
 */
@Service
public class DatabaseInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

    // ============================================================
    // CONFIGURAÇÕES INJETADAS
    // ============================================================

    @Value("${database.initializer.schema.path:/db/schema.sql}")
    private String schemaPath;

    @Value("${database.initializer.migrations.path:/db/migrations}")
    private String migrationsPath;

    @Value("${database.initializer.auto-create:true}")
    private boolean autoCreate;

    @Value("${database.initializer.version:1.0.0}")
    private String versionAtual;

    // ============================================================
    // DEPENDÊNCIAS INJETADAS
    // ============================================================

    @Inject
    private DatabaseConfig databaseConfig;

    // ============================================================
    // ESTADO INTERNO
    // ============================================================

    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private String appliedVersion;
    private List<String> migrationHistory = new ArrayList<>();

    // ============================================================
    // INICIALIZAÇÃO
    // ============================================================

    @PostConstruct
    public void init() {
        logger.info("🗄️ DatabaseInitializer v2.0 inicializando...");
        logger.info("   Schema: {}", schemaPath);
        logger.info("   Migrations: {}", migrationsPath);
        logger.info("   Auto-create: {}", autoCreate);
        logger.info("   Versão atual: {}", versionAtual);

        if (autoCreate) {
            try {
                initialize();
                logger.info("✅ DatabaseInitializer v2.0 pronto");
            } catch (SQLException e) {
                logger.error("❌ Falha ao inicializar banco: {}", e.getMessage());
            }
        } else {
            logger.info("ℹ️ Auto-create desabilitado, pulando inicialização");
        }
    }

    // ============================================================
    // MÉTODO PRINCIPAL DE INICIALIZAÇÃO
    // ============================================================

    /**
     * Inicializa o banco de dados com o schema e migrações.
     * Executa apenas uma vez por execução da aplicação.
     *
     * @throws SQLException em caso de erro na execução do SQL
     */
    public synchronized ResponseData initialize() throws SQLException {
        if (initialized.get()) {
            return ResponseData.success()
                    .withData("jaInicializado", true)
                    .withData("versao", appliedVersion)
                    .withData("mensagem", "Banco já inicializado");
        }

        try (Connection conn = databaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {

            // 1. Executa schema base
            String schema = loadSchema(schemaPath);
            stmt.execute(schema);
            logger.info("✅ Schema base aplicado");

            // 2. Cria tabela de versões (se não existir)
            criarTabelaVersao(stmt);

            // 3. Verifica versão atual
            String versaoAtual = getVersaoAtual(stmt);

            if (versaoAtual == null) {
                // Primeira execução - marca versão inicial
                setVersao(stmt, versionAtual);
                appliedVersion = versionAtual;
                logger.info("✅ Versão inicial registrada: {}", versionAtual);
            } else {
                appliedVersion = versaoAtual;
                logger.info("📌 Versão atual do banco: {}", versaoAtual);

                // 4. Aplica migrações pendentes
                if (!versionAtual.equals(versaoAtual)) {
                    aplicarMigracoes(stmt, versaoAtual, versionAtual);
                }
            }

            // 5. Marca como inicializado
            initialized.set(true);
            migrationHistory.add("Inicialização em " + versionAtual);

            logger.info("✅ Banco inicializado com sucesso!");
            logger.info("📁 Localização: {}", databaseConfig.getDbPath());

            return ResponseData.success()
                    .withData("inicializado", true)
                    .withData("versao", appliedVersion)
                    .withData("versaoEsperada", versionAtual)
                    .withData("migracoes", migrationHistory.size())
                    .withData("mensagem", "Banco inicializado com sucesso");

        } catch (Exception e) {
            logger.error("❌ Falha ao inicializar banco: {}", e.getMessage());
            throw new SQLException("Falha ao inicializar o banco de dados", e);
        }
    }

    // ============================================================
    // MÉTODOS DE MIGRAÇÃO
    // ============================================================

    /**
     * Aplica migrações pendentes entre versões.
     */
    private void aplicarMigracoes(Statement stmt, String de, String para) throws SQLException {
        logger.info("🔄 Aplicando migrações de {} para {}...", de, para);

        List<String> arquivos = listarArquivosMigracao(de, para);

        if (arquivos.isEmpty()) {
            logger.info("ℹ️ Nenhuma migração pendente");
            return;
        }

        for (String arquivo : arquivos) {
            try {
                String sql = loadSchema(migrationsPath + "/" + arquivo);
                stmt.execute(sql);
                migrationHistory.add("Migração: " + arquivo);
                logger.info("✅ Migração aplicada: {}", arquivo);
            } catch (Exception e) {
                logger.error("❌ Falha na migração {}: {}", arquivo, e.getMessage());
                throw new SQLException("Falha na migração: " + arquivo, e);
            }
        }

        // Atualiza versão
        setVersao(stmt, para);
        appliedVersion = para;
        logger.info("✅ Migrações concluídas. Versão atual: {}", para);
    }

    // ============================================================
    // MÉTODOS DE VERSÃO
    // ============================================================

    private void criarTabelaVersao(Statement stmt) throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS versao_banco (
                    id INTEGER PRIMARY KEY CHECK (id = 1),
                    versao TEXT NOT NULL,
                    data_atualizacao DATETIME DEFAULT CURRENT_TIMESTAMP,
                    historico TEXT
                )
                """;
        stmt.execute(sql);
    }

    private String getVersaoAtual(Statement stmt) throws SQLException {
        try (var rs = stmt.executeQuery(
                "SELECT versao FROM versao_banco WHERE id = 1")) {
            if (rs.next()) {
                return rs.getString("versao");
            }
            return null;
        }
    }

    private void setVersao(Statement stmt, String versao) throws SQLException {
        String sql = String.format("""
                INSERT OR REPLACE INTO versao_banco (id, versao, historico) 
                VALUES (1, '%s', '%s')
                """, versao, String.join("; ", migrationHistory));

        stmt.execute(sql);
        logger.info("📌 Versão atualizada para: {}", versao);
    }

    // ============================================================
    // MÉTODOS AUXILIARES
    // ============================================================

    /**
     * Carrega o schema SQL do arquivo de recursos.
     */
    private String loadSchema(String path) {
        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is == null) {
                throw new RuntimeException("Arquivo não encontrado: " + path);
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(is, StandardCharsets.UTF_8))) {

                return reader.lines()
                        .filter(line -> !line.trim().startsWith("--"))
                        .collect(Collectors.joining("\n"));
            }

        } catch (Exception e) {
            throw new RuntimeException("Falha ao carregar arquivo: " + path, e);
        }
    }

    /**
     * Lista arquivos de migração entre versões.
     * (Implementação simplificada - em produção, ler do classpath)
     */
    private List<String> listarArquivosMigracao(String de, String para) {
        List<String> migracoes = new ArrayList<>();

        // Exemplo de migrações
        if (de.compareTo("1.0.0") < 0 && para.compareTo("1.1.0") >= 0) {
            migracoes.add("v1.0.0_to_v1.1.0.sql");
        }

        return migracoes;
    }

    // ============================================================
    // MÉTODOS PÚBLICOS
    // ============================================================

    /**
     * Verifica se o banco já foi inicializado.
     */
    public boolean isInitialized() {
        return initialized.get();
    }

    /**
     * Obtém a versão atual do banco.
     */
    public String getAppliedVersion() {
        return appliedVersion;
    }

    /**
     * Obtém o histórico de migrações.
     */
    public List<String> getMigrationHistory() {
        return new ArrayList<>(migrationHistory);
    }

    /**
     * Recria o banco do zero (perigoso!).
     * Use com cuidado.
     */
    public synchronized ResponseData resetDatabase() {
        if (!initialized.get()) {
            return ResponseData.error("Banco não inicializado");
        }

        try (Connection conn = databaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {

            // Obtém todas as tabelas
            var rs = stmt.executeQuery(
                    "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%'");

            List<String> tables = new ArrayList<>();
            while (rs.next()) {
                tables.add(rs.getString("name"));
            }

            // Remove todas as tabelas
            for (String table : tables) {
                stmt.execute("DROP TABLE IF EXISTS " + table);
                logger.debug("🗑️ Tabela removida: {}", table);
            }

            // Reinicializa
            initialized.set(false);
            migrationHistory.clear();
            initialize();

            return ResponseData.success()
                    .withData("resetado", true)
                    .withData("mensagem", "Banco resetado com sucesso")
                    .withData("tabelasRemovidas", tables.size());

        } catch (SQLException e) {
            logger.error("❌ Falha ao resetar banco: {}", e.getMessage());
            return ResponseData.error("Falha ao resetar: " + e.getMessage());
        }
    }
}