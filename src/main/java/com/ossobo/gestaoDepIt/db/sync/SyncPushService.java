package com.ossobo.gestaoDepIt.db.sync;

import com.ossobo.gestaoDepIt.db.models.ConfigServidorRemoto;
import com.ossobo.gestaoDepIt.db.repositories.ConfigServidorRemotoRepository;
import com.ossobo.gestaoDepIt.db.sync.adapters.FuncionariosPushMapper;
import com.ossobo.gestaoDepIt.db.sync.models.PushMapper;
import com.ossobo.gestaoDepIt.db.sync.models.SyncPushResult;
import com.ossobo.winterfx.anotations.Inject;
import com.ossobo.winterfx.anotations.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * SyncPushService v1.0
 *
 * Orquestrador do push local → remoto.
 *
 * Fluxo:
 *   1. Lê config_servidor_remoto (SQLite) — a config ATIVA
 *   2. Abre conexão JDBC com o MySQL remoto
 *   3. Detecta modo: full (sync_state vazio) ou incremental (pending_sync)
 *   4. Para cada entidade NA ORDEM RÍGIDA, chama o mapper correspondente
 *   5. Commit no MySQL se tudo OK; rollback em falha
 *   6. Atualiza sync_state por entidade
 *
 * Arquitetura:
 *   - Serviço NÃO sabe SQL de entidade nenhuma
 *   - Cada entidade tem seu PushMapper (@Service) que faz leitura SQLite + escrita MySQL
 *   - Adicionar nova entidade = criar mapper + 1 linha neste serviço
 */
@Service
public class SyncPushService {

    private static final System.Logger LOGGER = System.getLogger(SyncPushService.class.getName());

    @Inject
    private ConfigServidorRemotoRepository configRepository;

    @Inject
    private SyncStateRepository syncStateRepository;

    // ============================================================
    // MAPPERS REGISTRADOS — NA ORDEM QUE PRECISAM RODAR
    // ============================================================
    //
    // Ordem por dependência lógica (FK não existe no MySQL, mas os
    // dados se relacionam por chave de negócio):
    //   1. catalogo_produtos       (sem dependência)
    //   2. funcionarios            (sem dependência)
    //   3. inventario_equipamentos (usa sku_produto + funcionario_id)
    //   4. usuarios                (usa funcionario_id)
    //   5. gestao_toners           (usa inventario_id + produto_id + usuario_responsavel)
    //   6. estoque_movimentacoes
    //   7. historico_eventos

    @Inject private FuncionariosPushMapper funcionariosMapper;
    // TODO: adicionar os outros 6 mappers quando forem criados:
    // @Inject private CatalogoPushMapper    catalogoMapper;
    // @Inject private InventarioPushMapper  inventarioMapper;
    // @Inject private UsuariosPushMapper    usuariosMapper;
    // @Inject private TonersPushMapper      tonersMapper;
    // @Inject private MovimentacoesPushMapper movimentacoesMapper;
    // @Inject private HistoricoPushMapper   historicoMapper;

    private List<PushMapper> mappersOrdenados() {
        List<PushMapper> l = new ArrayList<>();
        // Ordem: por dependência (pais antes de filhos)
        // l.add(catalogoMapper);       // 1
        l.add(funcionariosMapper);      // 2
        // l.add(inventarioMapper);     // 3
        // l.add(usuariosMapper);       // 4
        // l.add(tonersMapper);         // 5
        // l.add(movimentacoesMapper);  // 6
        // l.add(historicoMapper);      // 7
        return l;
    }

    // ============================================================
    // API PÚBLICA
    // ============================================================

    /**
     * Empurra do local para o remoto.
     * Detecta automaticamente se é full (primeira vez) ou incremental.
     */
    public SyncPushResult empurrar() throws SQLException {
        boolean fullSync = syncStateRepository.obterTodosEstados().isEmpty();

        LOGGER.log(System.Logger.Level.INFO,
                "🚀 Sync iniciado — modo: {0}", fullSync ? "FULL (bootstrap)" : "INCREMENTAL");

        ConfigServidorRemoto config = carregarConfigAtiva();

        try (Connection connMySQL = abrirConexaoMySQL(config)) {
            connMySQL.setAutoCommit(false);

            Map<String, Integer> registrosPorEntidade = new LinkedHashMap<>();
            boolean fullSyncFinal = fullSync;

            try {
                for (PushMapper mapper : mappersOrdenados()) {
                    int total = fullSyncFinal
                            ? mapper.pushFull(connMySQL)
                            : mapper.pushIncremental(connMySQL, idsPendentes(mapper.entidade()));

                    registrosPorEntidade.put(mapper.entidade(), total);
                    LOGGER.log(System.Logger.Level.INFO,
                            "   📤 {0}: {1} registro(s)", mapper.entidade(), total);
                }

                connMySQL.commit();
                LOGGER.log(System.Logger.Level.INFO, "✅ Commit no MySQL");

                // Sucesso: atualiza sync_state por entidade
                for (String entidade : registrosPorEntidade.keySet()) {
                    syncStateRepository.marcarSucesso(entidade,
                            java.time.LocalDateTime.now(), "push OK");
                }

                // TODO: remover pendências já empurradas
                // (implementar quando pending_sync for efetivamente usado)

                return SyncPushResult.ok(fullSyncFinal, registrosPorEntidade);

            } catch (SQLException e) {
                connMySQL.rollback();
                LOGGER.log(System.Logger.Level.ERROR, "❌ Rollback no MySQL — {0}", e.getMessage());
                return SyncPushResult.erro(fullSyncFinal, e.getMessage());
            }

        } catch (SQLException e) {
            LOGGER.log(System.Logger.Level.ERROR, "❌ Falha na conexão MySQL", e);
            return SyncPushResult.erro(fullSync, "Conexão MySQL falhou: " + e.getMessage());
        }
    }

    // ============================================================
    // AUXILIARES
    // ============================================================

    private ConfigServidorRemoto carregarConfigAtiva() throws SQLException {
        return configRepository.findAtivo()
                .orElseThrow(() -> new IllegalStateException(
                        "Nenhuma configuração de servidor remoto ativa. " +
                                "Configure em Definições → Servidor Remoto."));
    }

    private Connection abrirConexaoMySQL(ConfigServidorRemoto config) throws SQLException {
        String url = config.gerarUrlConexao();
        LOGGER.log(System.Logger.Level.INFO, "   🔌 Conectando: {0}", url);
        return DriverManager.getConnection(url, config.usuario(), config.senha());
    }

    /**
     * Lê os IDs pendentes para uma entidade.
     * TODO: implementar quando pending_sync estiver sendo alimentado.
     * Por ora devolve lista vazia (incremental não faz nada).
     */
    private List<String> idsPendentes(String entidade) {
        // Quando implementar: syncStateRepository.listarPendentes(entidade)
        return List.of();
    }
}