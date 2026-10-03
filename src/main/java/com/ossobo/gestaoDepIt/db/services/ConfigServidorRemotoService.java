package com.ossobo.gestaoDepIt.db.services;

import com.ossobo.gestaoDepIt.db.config.event.ConfigEvent;
import com.ossobo.gestaoDepIt.db.models.ConfigServidorRemoto;
import com.ossobo.gestaoDepIt.db.repositories.ConfigServidorRemotoRepository;

import com.ossobo.winterfx.anotations.Inject;
import com.ossobo.winterfx.anotations.Service;
import com.ossobo.winterfx.event.EventBus;



import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * ConfigServidorRemotoService - Serviço com EventBus e validações
 * v2.0 - Migrado para Java 17+ com WinterFX
 *
 * Responsabilidades:
 * - Gerenciar configurações de servidores remotos
 * - Testar conexões com banco de dados
 * - Publicar eventos de mudança
 */
@Service
public class ConfigServidorRemotoService {

    private static final System.Logger logger = System.getLogger(ConfigServidorRemotoService.class.getName());
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Inject
    private ConfigServidorRemotoRepository repository;

    @Inject
    private EventBus eventBus;

    // ===== CRUD COM EVENTOS =====

    public List<ConfigServidorRemoto> listarTodos() throws SQLException {
        return repository.findAll();
    }

    public Optional<ConfigServidorRemoto> buscarPorId(Long id) throws SQLException {
        return repository.findById(id);
    }

    public Optional<ConfigServidorRemoto> buscarAtiva() throws SQLException {
        return repository.findAtivo();
    }

    public Optional<ConfigServidorRemoto> buscarPorNome(String nome) throws SQLException {
        return repository.findByNome(nome);
    }

    public ConfigServidorRemoto salvar(ConfigServidorRemoto config) throws SQLException {
        validarConfig(config);

        // Verifica duplicidade de nome
        if (repository.existsByNome(config.nomeConfig())) {
            throw new IllegalArgumentException("Configuração com nome '" +
                    config.nomeConfig() + "' já existe");
        }

        // Se for ativa, desativa as outras
        if (config.isAtivo()) {
            repository.desativarTodas();
        }

        Long id = repository.insert(config);
        ConfigServidorRemoto salvo = repository.findById(id)
                .orElseThrow(() -> new SQLException("Falha ao buscar configuração salva"));

        // Publica evento
        eventBus.publish(new ConfigEvent(salvo, "CRIADO"));
        logger.log(System.Logger.Level.INFO,"Configuração criada: {}", salvo.nomeConfig());

        return salvo;
    }

    public ConfigServidorRemoto atualizar(ConfigServidorRemoto config) throws SQLException {
        validarConfig(config);

        if (config.id() == null) {
            throw new IllegalArgumentException("ID não pode ser nulo para atualização");
        }

        // Se for ativa, desativa as outras
        if (config.isAtivo()) {
            repository.desativarTodas();
        }

        repository.update(config);
        ConfigServidorRemoto atualizado = repository.findById(config.id())
                .orElseThrow(() -> new SQLException("Falha ao buscar configuração atualizada"));

        eventBus.publish(new ConfigEvent(atualizado, "ATUALIZADO"));
        logger.log(System.Logger.Level.INFO,"Configuração atualizada: {}", atualizado.nomeConfig());

        return atualizado;
    }

    public void deletar(Long id) throws SQLException {
        Optional<ConfigServidorRemoto> opt = repository.findById(id);
        ConfigServidorRemoto config = opt.orElseThrow(() ->
                new IllegalArgumentException("Configuração com ID " + id + " não encontrada")
        );

        repository.delete(id);
        eventBus.publish(new ConfigEvent(config, "EXCLUIDO"));
        logger.log(System.Logger.Level.INFO,"Configuração excluída: {}", config.nomeConfig());
    }

    // ===== OPERAÇÕES DE CONEXÃO =====

    /**
     * Define uma configuração como ativa, desativando as demais.
     */
    public ConfigServidorRemoto definirComoAtiva(Long id) throws SQLException {
        Optional<ConfigServidorRemoto> opt = repository.findById(id);
        ConfigServidorRemoto config = opt.orElseThrow(() ->
                new IllegalArgumentException("Configuração com ID " + id + " não encontrada")
        );

        if (config.isAtivo()) {
            return config; // Já está ativa
        }

        repository.desativarTodas();
        ConfigServidorRemoto atualizada = config.comAtivo(1);
        repository.update(atualizada);

        ConfigServidorRemoto salva = repository.findById(id)
                .orElseThrow(() -> new SQLException("Falha ao buscar configuração atualizada"));

        eventBus.publish(new ConfigEvent(salva, "ATIVADA"));
        logger.log(System.Logger.Level.INFO,"Configuração ativada: {}", salva.nomeConfig());

        return salva;
    }

    /**
     * Testa a conexão com o servidor remoto.
     */
    public boolean testarConexao(ConfigServidorRemoto config) throws SQLException {
        String url = config.gerarUrlConexao();

        // Atualiza status para TESTANDO
        ConfigServidorRemoto testando = config.comStatus("TESTANDO");
        repository.update(testando);

        try (var conn = DriverManager.getConnection(url, config.usuario(), config.senha())) {
            boolean conectado = conn.isValid(5);

            if (conectado) {
                String agora = LocalDateTime.now().format(FORMATTER);
                ConfigServidorRemoto conectada = config
                        .comStatus("CONECTADO")
                        .comStatus("CONECTADO", agora);
                repository.update(conectada);

                eventBus.publish(new ConfigEvent(conectada, "CONECTADO"));
                logger.log(System.Logger.Level.INFO,"✅ Conexão estabelecida: {}", config.nomeConfig());
                return true;
            } else {
                ConfigServidorRemoto falha = config.comStatus("FALHA");
                repository.update(falha);
                logger.log(System.Logger.Level.WARNING,"❌ Conexão falhou: timeout de 5 segundos - {}", config.nomeConfig());
                return false;
            }

        } catch (SQLException e) {
            ConfigServidorRemoto falha = config.comStatus("FALHA");
            repository.update(falha);
            logger.log(System.Logger.Level.ERROR,"❌ Erro ao conectar: {} - {}", config.nomeConfig(), e.getMessage());
            return false;
        }
    }

    /**
     * Testa a conexão com o servidor remoto e retorna mensagem detalhada.
     */
    public String testarConexaoComMensagem(ConfigServidorRemoto config) throws SQLException {
        String url = config.gerarUrlConexao();

        ConfigServidorRemoto testando = config.comStatus("TESTANDO");
        repository.update(testando);

        try (var conn = DriverManager.getConnection(url, config.usuario(), config.senha())) {
            boolean conectado = conn.isValid(5);

            if (conectado) {
                String agora = LocalDateTime.now().format(FORMATTER);
                ConfigServidorRemoto conectada = config
                        .comStatus("CONECTADO")
                        .comStatus("CONECTADO", agora);
                repository.update(conectada);

                eventBus.publish(new ConfigEvent(conectada, "CONECTADO"));
                return "✅ Conexão estabelecida com sucesso!";
            } else {
                ConfigServidorRemoto falha = config.comStatus("FALHA");
                repository.update(falha);
                return "❌ Falha na conexão: timeout de 5 segundos";
            }

        } catch (SQLException e) {
            ConfigServidorRemoto falha = config.comStatus("FALHA");
            repository.update(falha);
            return "❌ Erro ao conectar: " + e.getMessage();
        }
    }

    /**
     * Testa a conexão ativa e retorna o resultado.
     */
    public boolean testarConexaoAtiva() throws SQLException {
        Optional<ConfigServidorRemoto> opt = repository.findAtivo();
        if (opt.isEmpty()) {
            logger.log(System.Logger.Level.WARNING,"Nenhuma configuração ativa encontrada");
            return false;
        }
        return testarConexao(opt.get());
    }

    /**
     * Verifica se o servidor remoto está configurado e conectado.
     */
    public boolean isServidorRemotoConfigurado() throws SQLException {
        Optional<ConfigServidorRemoto> opt = repository.findAtivo();
        return opt.map(ConfigServidorRemoto::isConectado).orElse(false);
    }

    // ===== MÉTODOS PRIVADOS =====

    private void validarConfig(ConfigServidorRemoto config) {
        if (config == null) {
            throw new IllegalArgumentException("Configuração não pode ser nula");
        }
        if (!(config.nomeConfig() instanceof String n) || n.isBlank()) {
            throw new IllegalArgumentException("Nome da configuração é obrigatório");
        }
        if (!(config.host() instanceof String h) || h.isBlank()) {
            throw new IllegalArgumentException("Host é obrigatório");
        }
        if (!(config.porta() instanceof String p) || p.isBlank()) {
            throw new IllegalArgumentException("Porta é obrigatória");
        }
        if (!(config.databaseName() instanceof String db) || db.isBlank()) {
            throw new IllegalArgumentException("Nome do banco de dados é obrigatório");
        }
        if (!(config.usuario() instanceof String u) || u.isBlank()) {
            throw new IllegalArgumentException("Usuário é obrigatório");
        }
        if (!(config.senha() instanceof String s) || s.isBlank()) {
            throw new IllegalArgumentException("Senha é obrigatória");
        }
    }
}