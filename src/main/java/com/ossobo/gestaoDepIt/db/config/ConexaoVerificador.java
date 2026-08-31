package com.ossobo.gestaoDepIt.db.config;

import com.ossobo.gestaoDepIt.db.models.ConfigServidorRemoto;
import com.ossobo.winterfx.anotations.Inject;
import com.ossobo.winterfx.anotations.PostConstruct;
import com.ossobo.winterfx.anotations.Service;
import com.ossobo.winterfx.anotations.Value;
import com.ossobo.winterfx.router.model.ResponseData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * ConexaoVerificador v2.1
 *
 * Responsabilidade: Gerenciar e verificar conectividade com servidor remoto
 *
 * Padrões: Service + Configuration + Async
 *
 * v2.1 - Corrigido para usar Record (acesso direto aos campos)
 * v2.0 - Migração para WinterFX
 */
@Service
public class ConexaoVerificador {

    private static final Logger logger = LoggerFactory.getLogger(ConexaoVerificador.class);

    // ============================================================
    // CONFIGURAÇÕES INJETADAS (@Value)
    // ============================================================

    @Value("${conexao.verificador.timeout.socket:3000}")
    private int timeoutSocket;

    @Value("${conexao.verificador.timeout.db:5000}")
    private int timeoutDb;

    @Value("${conexao.verificador.porta.padrao:3306}")
    private int portaPadrao;

    @Value("${conexao.verificador.threads.pool:2}")
    private int threadPoolSize;

    // ============================================================
    // DEPENDÊNCIAS INJETADAS
    // ============================================================

    @Inject
    private DatabaseConnection databaseConnection;

    // ============================================================
    // INICIALIZAÇÃO
    // ============================================================

    @PostConstruct
    public void init() {
        logger.info("✅ ConexaoVerificador v2.1 inicializado");
        logger.info("   Timeout Socket: {}ms", timeoutSocket);
        logger.info("   Timeout DB: {}ms", timeoutDb);
        logger.info("   Porta padrão: {}", portaPadrao);
        logger.info("   Pool threads: {}", threadPoolSize);
    }

    // ============================================================
    // MÉTODOS PÚBLICOS (COM RESPONSEDATA)
    // ============================================================

    /**
     * Verifica se o host está acessível via socket.
     *
     * @param host Host a verificar
     * @param porta Porta a verificar
     * @return ResponseData com status e dados
     */
    public ResponseData verificarHost(String host, String porta) {
        if (host == null || host.trim().isEmpty()) {
            return ResponseData.error("Host não informado")
                    .withError("host", "Host é obrigatório");
        }

        int port = parsePort(porta);
        long startTime = System.currentTimeMillis();

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeoutSocket);
            long elapsed = System.currentTimeMillis() - startTime;

            logger.info("✅ Host {}:{} acessível ({}ms)", host, port, elapsed);

            return ResponseData.success()
                    .withData("host", host)
                    .withData("porta", port)
                    .withData("tempoResposta", elapsed)
                    .withData("acessivel", true);

        } catch (SocketTimeoutException e) {
            logger.warn("⏱️ Timeout ao conectar a {}:{}", host, port);
            return ResponseData.error(String.format("Timeout (%dms)", timeoutSocket))
                    .withData("host", host)
                    .withData("porta", port)
                    .withData("acessivel", false);

        } catch (IOException e) {
            logger.warn("❌ Host {}:{} inacessível: {}", host, port, e.getMessage());
            return ResponseData.error("Host inacessível: " + e.getMessage())
                    .withData("host", host)
                    .withData("porta", port)
                    .withData("acessivel", false);
        }
    }

    /**
     * Verifica conexão completa com o banco remoto.
     * ✅ CORRIGIDO: Usa acesso direto aos campos do Record
     *
     * @param config Configuração do servidor
     * @return ResponseData com status detalhado
     */
    public ResponseData verificarConexaoCompleta(ConfigServidorRemoto config) {
        if (config == null) {
            return ResponseData.error("Configuração inválida")
                    .withError("config", "Configuração do servidor é obrigatória");
        }

        // ✅ Record: acesso direto aos campos
        String host = config.host();
        String porta = config.porta();
        String databaseName = config.databaseName();
        String usuario = config.usuario();
        String senha = config.senha();
        String parametrosExtra = config.parametrosExtra();

        // 1. Verifica host
        ResponseData hostCheck = verificarHost(host, porta);
        if (!hostCheck.isSuccess()) {
            return ResponseData.error("Host inacessível")
                    .withData("host", host)
                    .withData("porta", porta)
                    .withData("etapa", "HOST_CHECK");
        }

        // 2. Verifica banco
        String url = config.gerarUrlConexao();

        if (parametrosExtra != null && !parametrosExtra.isEmpty()) {
            url += "?" + parseParams(parametrosExtra);
        }

        long startTime = System.currentTimeMillis();

        try (Connection conn = DriverManager.getConnection(url, usuario, senha)) {
            boolean valid = conn.isValid(timeoutDb / 1000);
            long elapsed = System.currentTimeMillis() - startTime;

            if (valid) {
                logger.info("✅ Conexão com banco {} estabelecida! ({}ms)",
                        databaseName, elapsed);

                return ResponseData.success()
                        .withData("database", databaseName)
                        .withData("tempoResposta", elapsed)
                        .withData("host", host)
                        .withData("usuario", usuario)
                        .withData("conectado", true);

            } else {
                return ResponseData.error("Falha na autenticação")
                        .withData("database", databaseName)
                        .withData("usuario", usuario)
                        .withData("conectado", false)
                        .withError("senha", "Verifique usuário e senha");
            }

        } catch (SQLException e) {
            logger.error("❌ Erro ao conectar ao banco: {}", e.getMessage());
            return ResponseData.error("Erro no banco: " + e.getMessage())
                    .withData("database", databaseName)
                    .withData("conectado", false)
                    .withData("erro", e.getMessage())
                    .withError("database", "Erro de conexão com o banco");
        }
    }

    /**
     * Verifica conexão atual do sistema (assíncrono).
     *
     * @return ResponseData com status atual
     */
    public ResponseData verificarConexaoAtiva() {
        try {
            ConfigServidorRemoto config = databaseConnection.getConfigAtiva();

            if (config == null) {
                return ResponseData.success()
                        .withData("status", StatusConexao.SQLITE_PADRAO.name())
                        .withData("statusMsg", StatusConexao.SQLITE_PADRAO.getMensagem())
                        .withData("modo", "SQLITE");
            }

            if (!databaseConnection.isUsandoRemoto()) {
                return ResponseData.success()
                        .withData("status", StatusConexao.SQLITE_PADRAO.name())
                        .withData("statusMsg", StatusConexao.SQLITE_PADRAO.getMensagem())
                        .withData("modo", "SQLITE");
            }

            return verificarConexaoCompleta(config);

        } catch (Exception e) {
            logger.error("❌ Erro ao verificar conexão ativa: {}", e.getMessage());
            return ResponseData.error("Erro na verificação: " + e.getMessage())
                    .withData("status", StatusConexao.ERRO_VERIFICACAO.name());
        }
    }

    /**
     * Verificação assíncrona em background.
     * Não bloqueia UI.
     *
     * @return CompletableFuture com resultado
     */
    public CompletableFuture<ResponseData> verificarConexaoAsync() {
        return CompletableFuture.supplyAsync(this::verificarConexaoAtiva);
    }

    /**
     * Verifica conexão com timeout.
     *
     * @param timeoutSeconds Tempo máximo de espera
     * @return ResponseData com resultado
     */
    public ResponseData verificarConexaoComTimeout(int timeoutSeconds) {
        try {
            return verificarConexaoAsync()
                    .get(timeoutSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.error("⏱️ Timeout na verificação de conexão");
            return ResponseData.error("Timeout na verificação")
                    .withData("timeout", timeoutSeconds);
        }
    }

    /**
     * Verifica se há conexão ativa (síncrono rápido).
     *
     * @return true se conectado, false caso contrário
     */
    public boolean hasConexaoAtiva() {
        try (Connection conn = databaseConnection.getConnection()) {
            return conn != null && !conn.isClosed() && conn.isValid(1);
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Retorna informações do host para exibição na UI
     */
    public String getHostInfo() {
        try {
            ConfigServidorRemoto config = databaseConnection.getConfigAtiva();
            if (config != null) {
                // ✅ Record: acesso direto aos campos
                return String.format("%s@%s:%s/%s",
                        config.usuario(),
                        config.host(),
                        config.porta(),
                        config.databaseName());
            }
        } catch (Exception e) {
            logger.error("Erro ao obter host info", e);
        }
        return "SQLite local";
    }

    /**
     * Retorna o status da conexão como texto
     */
    public String getStatusText() {
        try {
            ConfigServidorRemoto config = databaseConnection.getConfigAtiva();
            if (config != null) {
                String status = config.statusConexao();
                if (status != null) {
                    return switch (status) {
                        case "CONECTADO" -> "✅ Conectado";
                        case "FALHA" -> "❌ Falha";
                        case "TESTANDO" -> "⏳ Testando...";
                        case "NAO_TESTADO" -> "⚪ Não testado";
                        default -> status;
                    };
                }
            }
        } catch (Exception e) {
            logger.error("Erro ao obter status", e);
        }
        return "📁 SQLite local";
    }

    // ============================================================
    // MÉTODOS PRIVADOS
    // ============================================================

    private int parsePort(String porta) {
        if (porta == null || porta.trim().isEmpty()) {
            return portaPadrao;
        }
        try {
            return Integer.parseInt(porta.trim());
        } catch (NumberFormatException e) {
            logger.warn("⚠️ Porta inválida: {}, usando {}", porta, portaPadrao);
            return portaPadrao;
        }
    }

    private String parseParams(String params) {
        if (params == null || params.isEmpty()) {
            return "";
        }
        return params.replace("{", "")
                .replace("}", "")
                .replace("\"", "")
                .replace(" ", "");
    }

    // ============================================================
    // ENUM STATUS
    // ============================================================

    /**
     * Status de conexão (mantido para compatibilidade).
     */
    public enum StatusConexao {
        CONECTADO("✅ Conectado ao servidor remoto"),
        HOST_INACESSIVEL("❌ Host/porta inacessível"),
        FALHA_AUTENTICACAO("❌ Falha na autenticação"),
        ERRO_BANCO("❌ Erro ao acessar o banco"),
        CONFIG_INVALIDA("⚠️ Configuração inválida"),
        SQLITE_PADRAO("📁 Usando SQLite local"),
        ERRO_VERIFICACAO("❌ Erro ao verificar conexão");

        private final String mensagem;

        StatusConexao(String mensagem) {
            this.mensagem = mensagem;
        }

        public String getMensagem() {
            return mensagem;
        }

        public boolean isConectado() {
            return this == CONECTADO;
        }

        public boolean isFallback() {
            return this == SQLITE_PADRAO;
        }
    }
}