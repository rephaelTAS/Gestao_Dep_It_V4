package com.ossobo.gestaoDepIt.db.config;

import com.ossobo.gestaoDepIt.db.config.event.ConexaoEvent;
import com.ossobo.gestaoDepIt.db.config.event.ConexaoStatus;
import com.ossobo.gestaoDepIt.db.models.ConfigServidorRemoto;
import com.ossobo.gestaoDepIt.db.repositories.ConfigServidorRemotoRepository;
import com.ossobo.gestaoDepIt.db.repositories.exceptions.DataAccessException;
import com.ossobo.gestaoDepIt.db.services.ConfigServidorRemotoService;
import com.ossobo.winterfx.anotations.Inject;
import com.ossobo.winterfx.anotations.PostConstruct;
import com.ossobo.winterfx.anotations.Service;
import com.ossobo.winterfx.event.EventBus;
import com.ossobo.winterfx.router.model.ResponseData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * GerenciadorBanco v2.2
 *
 * Responsabilidade: Gerenciar troca entre SQLite e banco remoto
 *
 * Padrões: Service + EventBus + Async
 *
 * v2.2 - Corrigido para usar Record (acesso direto aos campos)
 * v2.1 - Migração para WinterFX Nativo
 */
@Service
public class GerenciadorBanco {

    private static final Logger logger = LoggerFactory.getLogger(GerenciadorBanco.class);

    // ============================================================
    // DEPENDÊNCIAS INJETADAS
    // ============================================================

    @Inject
    private DatabaseConnection databaseConnection;

    @Inject
    private ConfigServidorRemotoService configService;

    @Inject
    private ConfigServidorRemotoRepository configRepository;

    @Inject
    private ConexaoVerificador conexaoVerificador;

    @Inject
    private EventBus eventBus;

    // ============================================================
    // ESTADO INTERNO
    // ============================================================

    private final AtomicBoolean trocaEmAndamento = new AtomicBoolean(false);
    private LocalDateTime ultimaTroca;
    private String ultimoStatus;

    // ============================================================
    // INICIALIZAÇÃO
    // ============================================================

    @PostConstruct
    public void init() {
        logger.info("🔄 GerenciadorBanco v2.2 inicializando...");
        trocarAutomatico();
        logger.info("✅ GerenciadorBanco v2.2 pronto");
        logger.info("   Banco atual: {}", getDescricaoBancoAtual());
    }

    // ============================================================
    // MÉTODOS DE TROCA (COM RESPONSEDATA)
    // ============================================================

    /**
     * Troca para o banco de dados remoto configurado.
     *
     * @param configId ID da configuração a ser ativada
     * @return ResponseData com resultado da operação
     */
    public synchronized ResponseData trocarParaRemoto(Long configId) {
        if (trocaEmAndamento.get()) {
            return ResponseData.error("Troca já em andamento")
                    .withData("aguarde", true);
        }

        trocaEmAndamento.set(true);
        ultimaTroca = LocalDateTime.now();

        try {
            // 1. Busca a configuração
            Optional<ConfigServidorRemoto> optConfig = configService.buscarPorId(configId);
            if (optConfig.isEmpty()) {
                return ResponseData.error("Configuração não encontrada")
                        .withError("configId", "ID inválido: " + configId);
            }

            ConfigServidorRemoto config = optConfig.get();

            // 2. Verifica se a conexão é possível
            ResponseData status = conexaoVerificador.verificarConexaoCompleta(config);

            if (!status.isSuccess()) {
                // ✅ Record: acesso direto aos campos
                return ResponseData.error("Não é possível conectar: " + status.getMessage())
                        .withData("host", config.host())
                        .withData("porta", config.porta())
                        .withData("status", status.getData());
            }

            // 3. Ativa a configuração
            configService.definirComoAtiva(configId);

            // 4. Recarrega a configuração no DatabaseConnection
            databaseConnection.recarregarConfiguracao();

            // 5. Notifica via EventBus
            notificarMudanca(ConexaoStatus.REMOTO, config);

            // 6. Atualiza status
            ultimoStatus = "REMOTO: " + config.nomeConfig();

            // ✅ Record: acesso direto aos campos
            logger.info("✅ Troca para banco remoto: {}", config.nomeConfig());

            return ResponseData.success()
                    .withData("modo", "REMOTO")
                    .withData("config", config.nomeConfig())
                    .withData("host", config.host())
                    .withData("database", config.databaseName())
                    .withData("usuario", config.usuario())
                    .withData("mensagem", "Troca para remoto realizada com sucesso");

        } catch (DataAccessException | SQLException e) {
            logger.error("❌ Erro ao trocar para remoto: {}", e.getMessage());
            return ResponseData.error("Erro no banco: " + e.getMessage())
                    .withData("modo", "ERRO");
        } finally {
            trocaEmAndamento.set(false);
        }
    }

    /**
     * Troca para o banco SQLite (padrão).
     */
    public synchronized ResponseData trocarParaSqlite() {
        if (trocaEmAndamento.get()) {
            return ResponseData.error("Troca já em andamento")
                    .withData("aguarde", true);
        }

        trocaEmAndamento.set(true);
        ultimaTroca = LocalDateTime.now();

        try {
            // 1. Desativa todas as configurações remotas
            desativarTodasConfiguracoes();

            // 2. Força SQLite no DatabaseConnection
            databaseConnection.forcarSqlite();

            // 3. Notifica via EventBus
            notificarMudanca(ConexaoStatus.SQLITE, null);

            // 4. Atualiza status
            ultimoStatus = "SQLITE";

            logger.info("✅ Troca para SQLite realizada!");

            return ResponseData.success()
                    .withData("modo", "SQLITE")
                    .withData("database", databaseConnection.getDatabase())
                    .withData("mensagem", "Troca para SQLite realizada com sucesso");

        } catch (Exception e) {
            logger.error("❌ Erro ao trocar para SQLite: {}", e.getMessage());
            return ResponseData.error("Erro na troca: " + e.getMessage())
                    .withData("modo", "ERRO");
        } finally {
            trocaEmAndamento.set(false);
        }
    }

    /**
     * Troca automática com base na configuração ativa.
     */
    public synchronized ResponseData trocarAutomatico() {
        try {
            Optional<ConfigServidorRemoto> optAtiva = configService.buscarAtiva();

            if (optAtiva.isEmpty()) {
                logger.info("📁 Nenhuma configuração remota ativa. Usando SQLite.");
                return trocarParaSqlite();
            }

            ConfigServidorRemoto ativa = optAtiva.get();

            // Verifica conexão
            ResponseData status = conexaoVerificador.verificarConexaoCompleta(ativa);

            if (status.isSuccess()) {
                // ✅ Record: acesso direto aos campos
                logger.info("✅ Conexão remota OK, usando: {}", ativa.nomeConfig());
                databaseConnection.recarregarConfiguracao();
                notificarMudanca(ConexaoStatus.REMOTO, ativa);
                ultimoStatus = "REMOTO (automático): " + ativa.nomeConfig();

                return ResponseData.success()
                        .withData("modo", "REMOTO")
                        .withData("config", ativa.nomeConfig())
                        .withData("automatico", true)
                        .withData("mensagem", "Conexão remota estabelecida automaticamente");
            } else {
                logger.warn("⚠️ Conexão remota falhou. Usando SQLite.");
                return trocarParaSqlite();
            }

        } catch (DataAccessException | SQLException e) {
            logger.error("❌ Erro na troca automática: {}", e.getMessage());
            return trocarParaSqlite();
        }
    }

    /**
     * Troca assíncrona (não bloqueia UI).
     */
    public CompletableFuture<ResponseData> trocarAutomaticoAsync() {
        return CompletableFuture.supplyAsync(this::trocarAutomatico);
    }

    // ============================================================
    // MÉTODOS DE ESTADO
    // ============================================================

    public boolean isUsandoRemoto() {
        return databaseConnection.isUsandoRemoto();
    }

    public boolean isUsandoSqlite() {
        return !databaseConnection.isUsandoRemoto();
    }

    public String getBancoAtual() {
        if (isUsandoRemoto()) {
            ConfigServidorRemoto config = databaseConnection.getConfigAtiva();
            if (config != null) {
                // ✅ Record: acesso direto aos campos
                return String.format("MySQL: %s:%s/%s",
                        config.host(),
                        config.porta(),
                        config.databaseName());
            }
            return "Remoto (desconhecido)";
        }
        return "SQLite: " + databaseConnection.getDatabase();
    }

    public String getDescricaoBancoAtual() {
        if (isUsandoRemoto()) {
            ConfigServidorRemoto config = databaseConnection.getConfigAtiva();
            if (config != null) {
                boolean conectado = conexaoVerificador.hasConexaoAtiva();
                // ✅ Record: acesso direto aos campos
                return String.format("🌐 %s (%s@%s:%s/%s) %s",
                        config.nomeConfig(),
                        config.usuario(),
                        config.host(),
                        config.porta(),
                        config.databaseName(),
                        conectado ? "🟢 CONECTADO" : "🔴 DESCONECTADO");
            }
            return "🌐 Remoto (desconhecido)";
        }
        return "📁 SQLite Local (" + databaseConnection.getDatabase() + ")";
    }

    public ResponseData getStatusConexao() {
        return conexaoVerificador.verificarConexaoAtiva();
    }

    public ConexaoVerificador.StatusConexao getStatusConexaoEnum() {
        try {
            ResponseData response = conexaoVerificador.verificarConexaoAtiva();
            Object status = response.getData().get("status");
            if (status != null) {
                return ConexaoVerificador.StatusConexao.valueOf(status.toString());
            }
        } catch (Exception e) {
            logger.warn("Erro ao obter status enum: {}", e.getMessage());
        }
        return ConexaoVerificador.StatusConexao.SQLITE_PADRAO;
    }

    // ============================================================
    // MÉTODOS DE UTILIDADE
    // ============================================================

    public boolean isSqliteDisponivel() {
        try {
            databaseConnection.getConnectionSqlite();
            return true;
        } catch (Exception e) {
            logger.error("❌ SQLite indisponível: {}", e.getMessage());
            return false;
        }
    }

    public boolean isTrocaEmAndamento() {
        return trocaEmAndamento.get();
    }

    public LocalDateTime getUltimaTroca() {
        return ultimaTroca;
    }

    public String getUltimoStatus() {
        return ultimoStatus;
    }

    // ============================================================
    // MÉTODOS PRIVADOS
    // ============================================================

    private void desativarTodasConfiguracoes() throws DataAccessException, SQLException {
        List<ConfigServidorRemoto> todas = configService.listarTodos();
        for (ConfigServidorRemoto config : todas) {
            if (config.isAtivo()) {
                // ✅ Record: criar nova instância com ativo = 0
                ConfigServidorRemoto desativada = config.comAtivo(0);
                configService.atualizar(desativada);
            }
        }
        logger.debug("✅ Todas as configurações remotas desativadas");
    }

    private void notificarMudanca(ConexaoStatus status, ConfigServidorRemoto config) {
        String mensagem = String.format("Banco alterado para: %s", status.getDescricao());

        ConexaoEvent event = new ConexaoEvent(
                ConexaoEvent.Type.MUDOU,
                status,
                config,
                mensagem
        );

        eventBus.publish(event);
        logger.info("📢 Evento de mudança de banco publicado: {}", status.getDescricao());
    }

    // ============================================================
    // MÉTODO PARA UI (RESPONSEDATA COMPLETO)
    // ============================================================

    public ResponseData getInfoBancoAtual() {
        return ResponseData.success()
                .withData("modo", isUsandoRemoto() ? "REMOTO" : "SQLITE")
                .withData("descricao", getDescricaoBancoAtual())
                .withData("banco", getBancoAtual())
                .withData("conectado", conexaoVerificador.hasConexaoAtiva())
                .withData("ultimaTroca", ultimaTroca != null ? ultimaTroca.toString() : "N/A")
                .withData("ultimoStatus", ultimoStatus != null ? ultimoStatus : "N/A")
                .withData("sqliteDisponivel", isSqliteDisponivel())
                .withData("trocaEmAndamento", trocaEmAndamento.get());
    }
}