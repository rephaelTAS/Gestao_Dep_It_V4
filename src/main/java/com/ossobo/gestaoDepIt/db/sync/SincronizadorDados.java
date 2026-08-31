package com.ossobo.gestaoDepIt.db.sync;

import com.ossobo.gestaoDepIt.db.config.ConexaoVerificador;
import com.ossobo.gestaoDepIt.db.config.DatabaseConnection;
import com.ossobo.gestaoDepIt.db.config.GerenciadorBanco;
import com.ossobo.gestaoDepIt.db.config.event.SyncEvent;
import com.ossobo.gestaoDepIt.db.models.*;
import com.ossobo.gestaoDepIt.db.repositories.*;
import com.ossobo.gestaoDepIt.db.services.*;
import com.ossobo.gestaoDepIt.db.repositories.exceptions.DataAccessException;

import com.ossobo.winterfx.anotations.Inject;
import com.ossobo.winterfx.anotations.PostConstruct;
import com.ossobo.winterfx.anotations.Service;
import com.ossobo.winterfx.event.EventBus;
import com.ossobo.winterfx.router.model.ResponseData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * SincronizadorDados - Sincronização entre SQLite (local) e MySQL (remoto)
 * v2.1 - Corrigido eventos SyncEvent e tipos de dados
 *
 * Responsabilidades:
 * - Detectar falha de conexão com servidor remoto
 * - Salvar dados localmente (SQLite) quando remoto falha
 * - Sincronizar dados para remoto quando conexão retorna
 * - Estratégia: "Cut and Paste" (local sobrescreve remoto)
 * - Funciona em background (thread separada)
 * - Notifica via EventBus (@SyncEvent)
 */
@Service
public final class SincronizadorDados {

    private static final Logger logger = LoggerFactory.getLogger(SincronizadorDados.class);

    // ============================================================
    // CONFIGURAÇÕES
    // ============================================================

    private static final int VERIFICAR_INTERVALO = 30;     // Segundos entre verificações
    private static final int VERIFICAR_INICIAL_DELAY = 5;  // Delay inicial
    private static final int MAX_TENTATIVAS_SYNC = 3;      // Tentativas por entidade

    // ============================================================
    // SINGLETON
    // ============================================================

    private static SincronizadorDados instance;
    private final AtomicBoolean rodando = new AtomicBoolean(false);
    private final AtomicBoolean sincronizando = new AtomicBoolean(false);
    private final AtomicBoolean remotoDisponivel = new AtomicBoolean(false);

    private ScheduledExecutorService scheduler;
    private final Map<String, String> ultimaSincronizacao = new ConcurrentHashMap<>(); // ✅ String → String

    // ============================================================
    // DEPENDÊNCIAS INJETADAS (WINTERFX)
    // ============================================================

    @Inject
    private DatabaseConnection databaseConnection;

    @Inject
    private GerenciadorBanco gerenciadorBanco;

    @Inject
    private ConexaoVerificador conexaoVerificador;

    @Inject
    private EventBus eventBus;

    // ============================================================
    // REPOSITÓRIOS INJETADOS
    // ============================================================

    @Inject
    private CatalogoProdutosRepository catalogoRepository;

    @Inject
    private InventarioEquipamentosRepository inventarioRepository;

    @Inject
    private FuncionariosRepository funcionarioRepository;

    @Inject
    private UsuariosRepository usuarioRepository;

    @Inject
    private EstoqueMovimentacoesRepository movimentacaoRepository;

    @Inject
    private GestaoTonersRepository tonerRepository;

    @Inject
    private HistoricoEventosRepository historicoRepository;

    // ============================================================
    // SERVICES INJETADOS
    // ============================================================

    @Inject
    private CatalogoProdutosService catalogoService;

    @Inject
    private InventarioEquipamentosService inventarioService;

    @Inject
    private FuncionariosService funcionarioService;

    @Inject
    private UsuariosService usuarioService;

    @Inject
    private EstoqueMovimentacoesService movimentacaoService;

    @Inject
    private GestaoTonersService tonerService;

    @Inject
    private HistoricoEventosService historicoService;

    // ============================================================
    // CONSTRUTOR PRIVADO (SINGLETON)
    // ============================================================

    private SincronizadorDados() {
        // Construtor vazio - injeção via WinterFX
    }

    /**
     * Obtém a instância única do sincronizador.
     * O WinterFX gerencia a injeção via @Inject.
     */
    public static synchronized SincronizadorDados getInstance() {
        if (instance == null) {
            instance = new SincronizadorDados();
        }
        return instance;
    }

    // ============================================================
    // INICIALIZAÇÃO PÓS-CONSTRUÇÃO
    // ============================================================

    @PostConstruct
    public void init() {
        logger.info("🔧 SincronizadorDados v2.1 inicializado via WinterFX");
        logger.info("   Verificação a cada {} segundos", VERIFICAR_INTERVALO);
        logger.info("   Delay inicial: {} segundos", VERIFICAR_INICIAL_DELAY);
        logger.info("   Máx tentativas: {}", MAX_TENTATIVAS_SYNC);
    }

    // ============================================================
    // INICIALIZAÇÃO E PARADA
    // ============================================================

    /**
     * Inicia o sincronizador em background.
     */
    public synchronized void iniciar() {
        if (rodando.get()) {
            logger.warn("⚠️ Sincronizador já está rodando");
            return;
        }

        logger.info("🚀 Iniciando SincronizadorDados...");

        // Verifica estado inicial da conexão
        remotoDisponivel.set(gerenciadorBanco.isUsandoRemoto() &&
                conexaoVerificador.hasConexaoAtiva());

        // Cria scheduler com thread daemon
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "SincronizadorDados-Thread");
            t.setDaemon(true);
            return t;
        });

        rodando.set(true);

        // Agenda verificações periódicas
        scheduler.scheduleAtFixedRate(
                this::verificarESincronizar,
                VERIFICAR_INICIAL_DELAY,
                VERIFICAR_INTERVALO,
                TimeUnit.SECONDS
        );

        logger.info("✅ SincronizadorDados iniciado (verificando a cada {}s)",
                VERIFICAR_INTERVALO);

        // ✅ Publica evento de inicialização com 6 argumentos
        eventBus.publish(new SyncEvent(
                SyncEvent.Type.INICIADO,
                "Sistema",
                0,
                0,
                0,
                "Sincronizador iniciado com sucesso"
        ));
    }

    /**
     * Para o sincronizador.
     */
    public synchronized void parar() {
        if (!rodando.get()) {
            return;
        }

        logger.info("🛑 Parando SincronizadorDados...");
        rodando.set(false);

        if (scheduler != null) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        logger.info("✅ SincronizadorDados parado");

        // ✅ Publica evento de parada com 6 argumentos
        eventBus.publish(new SyncEvent(
                SyncEvent.Type.PAUSADO,
                "Sistema",
                0,
                0,
                0,
                "Sincronizador parado"
        ));
    }

    // ============================================================
    // MÉTODO PRINCIPAL DE VERIFICAÇÃO
    // ============================================================

    /**
     * Verifica a conexão e executa sincronização se necessário.
     */
    private void verificarESincronizar() {
        if (!rodando.get() || sincronizando.get()) {
            return;
        }

        try {
            boolean remotoOk = verificarConexaoRemota();

            if (remotoOk && !remotoDisponivel.get()) {
                // Conexão voltou! Sincroniza dados pendentes
                logger.info("🔄 Conexão remota restaurada! Iniciando sincronização...");
                eventBus.publish(new SyncEvent(
                        SyncEvent.Type.INICIADO,
                        "Sistema",
                        0,
                        0,
                        0,
                        "Conexão remota restaurada - Iniciando sincronização"
                ));
                sincronizarPendentes();
                remotoDisponivel.set(true);

            } else if (!remotoOk && remotoDisponivel.get()) {
                // Conexão caiu - modo offline
                logger.warn("⚠️ Conexão remota perdida! Modo offline ativado.");
                remotoDisponivel.set(false);
                eventBus.publish(new SyncEvent(
                        SyncEvent.Type.FALHOU,
                        "Conexão",
                        0,
                        0,
                        1,
                        "Conexão remota perdida - Modo offline ativado"
                ));
                notificarModoOffline();

            } else if (remotoOk) {
                // Conexão estável - verifica se há pendências
                if (temDadosPendentes()) {
                    logger.info("📤 Dados pendentes detectados. Sincronizando...");
                    sincronizarPendentes();
                }
            }

        } catch (Exception e) {
            logger.error("❌ Erro na verificação/sincronização: {}", e.getMessage());
        }
    }

    // ============================================================
    // VERIFICAÇÃO DE CONEXÃO
    // ============================================================

    private boolean verificarConexaoRemota() {
        if (!gerenciadorBanco.isUsandoRemoto()) {
            return false;
        }

        try {
            return conexaoVerificador.hasConexaoAtiva();
        } catch (Exception e) {
            logger.debug("Conexão remota indisponível: {}", e.getMessage());
            return false;
        }
    }

    // ============================================================
    // SINCRONIZAÇÃO DE DADOS PENDENTES
    // ============================================================

    /**
     * Sincroniza todos os dados pendentes para o remoto.
     * Estratégia: "Cut and Paste" - dados locais sobrescrevem remotos.
     */
    public synchronized void sincronizarPendentes() {
        if (sincronizando.get()) {
            logger.info("⏳ Sincronização já em andamento...");
            return;
        }

        if (!gerenciadorBanco.isUsandoRemoto()) {
            logger.warn("⚠️ Não é possível sincronizar: modo remoto não está ativo");
            return;
        }

        if (!conexaoVerificador.hasConexaoAtiva()) {
            logger.warn("⚠️ Não é possível sincronizar: sem conexão remota");
            return;
        }

        sincronizando.set(true);

        try {
            logger.info("🔄 Iniciando sincronização de dados...");

            eventBus.publish(new SyncEvent(
                    SyncEvent.Type.INICIADO,
                    "Sistema",
                    0,
                    0,
                    0,
                    "Iniciando sincronização de dados..."
            ));

            // Sincroniza cada entidade
            int totalSincronizados = 0;
            int totalFalhas = 0;

            // Mapeamento de entidades para progresso
            Map<String, Integer> progressoEntidades = new LinkedHashMap<>();

            totalSincronizados += sincronizarCatalogo(progressoEntidades);
            totalSincronizados += sincronizarInventario(progressoEntidades);
            totalSincronizados += sincronizarFuncionarios(progressoEntidades);
            totalSincronizados += sincronizarUsuarios(progressoEntidades);
            totalSincronizados += sincronizarMovimentacoes(progressoEntidades);
            totalSincronizados += sincronizarToners(progressoEntidades);
            totalSincronizados += sincronizarHistorico(progressoEntidades);

            // Atualiza timestamp
            String agora = LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            ultimaSincronizacao.put("ultima_sync", String.valueOf(System.currentTimeMillis())); // ✅ String
            ultimaSincronizacao.put("ultima_sync_data", agora);

            logger.info("✅ Sincronização concluída! {} registros sincronizados.",
                    totalSincronizados);

            // ✅ Publica evento de conclusão com 6 argumentos
            eventBus.publish(new SyncEvent(
                    SyncEvent.Type.CONCLUIDO,
                    "Sistema",
                    totalSincronizados,
                    totalSincronizados,
                    totalFalhas,
                    totalSincronizados + " registros sincronizados com sucesso"
            ));

            // Notifica UI
            notificarSincronizacaoConcluida(totalSincronizados);

        } catch (Exception e) {
            logger.error("❌ Erro durante sincronização: {}", e.getMessage());
            eventBus.publish(new SyncEvent(
                    SyncEvent.Type.FALHOU,
                    "Sistema",
                    0,
                    0,
                    1,
                    "Erro na sincronização: " + e.getMessage()
            ));
        } finally {
            sincronizando.set(false);
        }
    }

    // ============================================================
    // MÉTODOS DE SINCRONIZAÇÃO POR ENTIDADE
    // ============================================================

    /**
     * Sincroniza Catálogo de Produtos.
     * Lê do SQLite, sobrescreve no MySQL.
     */
    private int sincronizarCatalogo(Map<String, Integer> progresso) {
        String entidade = "Catálogo";
        try {
            List<CatalogoProdutos> locais = catalogoRepository.findAll();
            if (locais.isEmpty()) {
                progresso.put(entidade, 0);
                return 0;
            }

            int count = 0;
            int falhas = 0;
            int total = locais.size();

            for (CatalogoProdutos produto : locais) {
                try {
                    Optional<CatalogoProdutos> remoto = catalogoService.buscarPorSku(produto.sku());

                    if (remoto.isEmpty()) {
                        catalogoService.salvar(produto);
                    } else {
                        catalogoService.atualizar(produto);
                    }
                    count++;

                    // Publica progresso a cada 10 itens
                    if (count % 10 == 0) {
                        eventBus.publish(new SyncEvent(
                                SyncEvent.Type.PROGRESSO,
                                entidade,
                                total,
                                count,
                                falhas,
                                String.format("Sincronizando %s: %d/%d", entidade, count, total)
                        ));
                    }
                } catch (Exception e) {
                    falhas++;
                    logger.warn("⚠️ Falha ao sincronizar produto {}: {}",
                            produto.sku(), e.getMessage());
                }
            }

            progresso.put(entidade, count);
            logger.info("   📦 Catálogo: {} produtos sincronizados", count);
            return count;

        } catch (Exception e) {
            logger.error("❌ Erro ao sincronizar catálogo: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Sincroniza Inventário de Equipamentos.
     */
    private int sincronizarInventario(Map<String, Integer> progresso) {
        String entidade = "Inventário";
        try {
            List<InventarioEquipamentos> locais = inventarioRepository.findAll();
            if (locais.isEmpty()) {
                progresso.put(entidade, 0);
                return 0;
            }

            int count = 0;
            int falhas = 0;
            int total = locais.size();

            for (InventarioEquipamentos item : locais) {
                try {
                    Optional<InventarioEquipamentos> remoto = inventarioService.buscarPorId(item.id());

                    if (remoto.isEmpty()) {
                        inventarioService.cadastrar(item);
                    } else {
                        inventarioService.atualizar(item);
                    }
                    count++;

                    if (count % 10 == 0) {
                        eventBus.publish(new SyncEvent(
                                SyncEvent.Type.PROGRESSO,
                                entidade,
                                total,
                                count,
                                falhas,
                                String.format("Sincronizando %s: %d/%d", entidade, count, total)
                        ));
                    }
                } catch (Exception e) {
                    falhas++;
                    logger.warn("⚠️ Falha ao sincronizar inventário {}: {}",
                            item.numSerie(), e.getMessage());
                }
            }

            progresso.put(entidade, count);
            logger.info("   🔧 Inventário: {} equipamentos sincronizados", count);
            return count;

        } catch (Exception e) {
            logger.error("❌ Erro ao sincronizar inventário: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Sincroniza Funcionários.
     */
    private int sincronizarFuncionarios(Map<String, Integer> progresso) {
        String entidade = "Funcionários";
        try {
            List<Funcionarios> locais = funcionarioRepository.findAll();
            if (locais.isEmpty()) {
                progresso.put(entidade, 0);
                return 0;
            }

            int count = 0;
            int falhas = 0;
            int total = locais.size();

            for (Funcionarios func : locais) {
                try {
                    Optional<Funcionarios> remoto = funcionarioService.buscarPorCodDep(func.codDep());

                    if (remoto.isEmpty()) {
                        funcionarioService.criar(func);
                    } else {
                        funcionarioService.atualizar(func);
                    }
                    count++;

                    if (count % 10 == 0) {
                        eventBus.publish(new SyncEvent(
                                SyncEvent.Type.PROGRESSO,
                                entidade,
                                total,
                                count,
                                falhas,
                                String.format("Sincronizando %s: %d/%d", entidade, count, total)
                        ));
                    }
                } catch (Exception e) {
                    falhas++;
                    logger.warn("⚠️ Falha ao sincronizar funcionário {}: {}",
                            func.codDep(), e.getMessage());
                }
            }

            progresso.put(entidade, count);
            logger.info("   👤 Funcionários: {} sincronizados", count);
            return count;

        } catch (Exception e) {
            logger.error("❌ Erro ao sincronizar funcionários: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Sincroniza Usuários.
     */
    private int sincronizarUsuarios(Map<String, Integer> progresso) {
        String entidade = "Usuários";
        try {
            List<Usuario> locais = usuarioRepository.findAll();
            if (locais.isEmpty()) {
                progresso.put(entidade, 0);
                return 0;
            }

            int count = 0;
            int falhas = 0;
            int total = locais.size();

            for (Usuario usuario : locais) {
                try {
                    Optional<Usuario> remoto = usuarioService.buscarPorId(usuario.id());

                    if (remoto.isEmpty()) {
                        usuarioService.criar(usuario);
                    } else {
                        usuarioService.atualizar(usuario);
                    }
                    count++;

                    if (count % 10 == 0) {
                        eventBus.publish(new SyncEvent(
                                SyncEvent.Type.PROGRESSO,
                                entidade,
                                total,
                                count,
                                falhas,
                                String.format("Sincronizando %s: %d/%d", entidade, count, total)
                        ));
                    }
                } catch (Exception e) {
                    falhas++;
                    logger.warn("⚠️ Falha ao sincronizar usuário {}: {}",
                            usuario.email(), e.getMessage());
                }
            }

            progresso.put(entidade, count);
            logger.info("   🔐 Usuários: {} sincronizados", count);
            return count;

        } catch (Exception e) {
            logger.error("❌ Erro ao sincronizar usuários: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Sincroniza Movimentações de Estoque.
     */
    private int sincronizarMovimentacoes(Map<String, Integer> progresso) {
        String entidade = "Movimentações";
        try {
            List<EstoqueMovimentacoes> locais = movimentacaoRepository.findAll();
            if (locais.isEmpty()) {
                progresso.put(entidade, 0);
                return 0;
            }

            int count = 0;
            int falhas = 0;
            int total = locais.size();

            for (EstoqueMovimentacoes mov : locais) {
                try {
                    Optional<EstoqueMovimentacoes> remoto = movimentacaoService.buscarPorId(mov.id());

                    if (remoto.isEmpty()) {
                        movimentacaoRepository.insert(mov);
                    } else {
                        movimentacaoRepository.update(mov);
                    }
                    count++;

                    if (count % 10 == 0) {
                        eventBus.publish(new SyncEvent(
                                SyncEvent.Type.PROGRESSO,
                                entidade,
                                total,
                                count,
                                falhas,
                                String.format("Sincronizando %s: %d/%d", entidade, count, total)
                        ));
                    }
                } catch (Exception e) {
                    falhas++;
                    logger.warn("⚠️ Falha ao sincronizar movimentação {}: {}",
                            mov.id(), e.getMessage());
                }
            }

            progresso.put(entidade, count);
            logger.info("   📊 Movimentações: {} sincronizadas", count);
            return count;

        } catch (Exception e) {
            logger.error("❌ Erro ao sincronizar movimentações: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Sincroniza Gestão de Toners.
     */
    private int sincronizarToners(Map<String, Integer> progresso) {
        String entidade = "Toners";
        try {
            List<GestaoToners> locais = tonerRepository.findAll();
            if (locais.isEmpty()) {
                progresso.put(entidade, 0);
                return 0;
            }

            int count = 0;
            int falhas = 0;
            int total = locais.size();

            for (GestaoToners toner : locais) {
                try {
                    Optional<GestaoToners> remoto = tonerService.buscarPorId(toner.id());

                    if (remoto.isEmpty()) {
                        tonerService.registrarInstalacao(toner);
                    } else {
                        tonerService.atualizar(toner);
                    }
                    count++;

                    if (count % 10 == 0) {
                        eventBus.publish(new SyncEvent(
                                SyncEvent.Type.PROGRESSO,
                                entidade,
                                total,
                                count,
                                falhas,
                                String.format("Sincronizando %s: %d/%d", entidade, count, total)
                        ));
                    }
                } catch (Exception e) {
                    falhas++;
                    logger.warn("⚠️ Falha ao sincronizar toner {}: {}",
                            toner.id(), e.getMessage());
                }
            }

            progresso.put(entidade, count);
            logger.info("   🖨️ Toners: {} sincronizados", count);
            return count;

        } catch (Exception e) {
            logger.error("❌ Erro ao sincronizar toners: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Sincroniza Histórico de Eventos.
     */
    private int sincronizarHistorico(Map<String, Integer> progresso) {
        String entidade = "Histórico";
        try {
            List<HistoricoEventos> locais = historicoRepository.findAll();
            if (locais.isEmpty()) {
                progresso.put(entidade, 0);
                return 0;
            }

            int count = 0;
            int falhas = 0;
            int total = locais.size();

            for (HistoricoEventos evento : locais) {
                try {
                    Optional<HistoricoEventos> remoto = historicoService.buscarPorId(evento.id());

                    if (remoto.isEmpty()) {
                        historicoService.registrar(evento);
                    } else {
                        historicoService.registrar(evento);
                    }
                    count++;

                    if (count % 10 == 0) {
                        eventBus.publish(new SyncEvent(
                                SyncEvent.Type.PROGRESSO,
                                entidade,
                                total,
                                count,
                                falhas,
                                String.format("Sincronizando %s: %d/%d", entidade, count, total)
                        ));
                    }
                } catch (Exception e) {
                    falhas++;
                    logger.warn("⚠️ Falha ao sincronizar evento {}: {}",
                            evento.id(), e.getMessage());
                }
            }

            progresso.put(entidade, count);
            logger.info("   📜 Histórico: {} eventos sincronizados", count);
            return count;

        } catch (Exception e) {
            logger.error("❌ Erro ao sincronizar histórico: {}", e.getMessage());
            return 0;
        }
    }

    // ============================================================
    // MÉTODOS DE VERIFICAÇÃO
    // ============================================================

    /**
     * Verifica se há dados pendentes para sincronizar.
     */
    public boolean temDadosPendentes() {
        try {
            long localCount = contarDadosLocais();
            long remotoCount = contarDadosRemotos();
            return localCount > remotoCount;
        } catch (Exception e) {
            logger.debug("Erro ao verificar pendências: {}", e.getMessage());
            return false;
        }
    }

    private long contarDadosLocais() throws Exception {
        return catalogoRepository.countAll() +
                inventarioRepository.countAll() +
                funcionarioRepository.countAll() +
                usuarioRepository.countAll() +
                movimentacaoRepository.countAll() +
                tonerRepository.countAll() +
                historicoRepository.countAll();
    }

    private long contarDadosRemotos() {
        try {
            return catalogoService.contarTotal() +
                    inventarioService.contarTotal() +
                    funcionarioService.contarTotal() +
                    usuarioService.contarTotal() +
                    movimentacaoService.contarTotal() +
                    tonerService.contarTotal() +
                    historicoService.contarTotal();
        } catch (Exception e) {
            return 0;
        }
    }

    // ============================================================
    // CALLBACKS E NOTIFICAÇÕES
    // ============================================================

    private final List<SyncListener> listeners = new CopyOnWriteArrayList<>();

    public interface SyncListener {
        void onModoOffline();
        void onModoOnline();
        void onSincronizacaoConcluida(int totalRegistros);
        void onProgresso(String mensagem);
    }

    public void addListener(SyncListener listener) {
        listeners.add(listener);
    }

    public void removeListener(SyncListener listener) {
        listeners.remove(listener);
    }

    private void notificarModoOffline() {
        for (SyncListener l : listeners) {
            try {
                l.onModoOffline();
            } catch (Exception e) {
                logger.warn("Erro no listener: {}", e.getMessage());
            }
        }
    }

    private void notificarSincronizacaoConcluida(int total) {
        for (SyncListener l : listeners) {
            try {
                l.onSincronizacaoConcluida(total);
            } catch (Exception e) {
                logger.warn("Erro no listener: {}", e.getMessage());
            }
        }
    }

    // ============================================================
    // RESPONSEDATA PARA UI
    // ============================================================

    /**
     * Obtém o status do sincronizador para UI via ResponseData.
     */
    public ResponseData getStatus() {
        return ResponseData.success()
                .withData("rodando", rodando.get())
                .withData("sincronizando", sincronizando.get())
                .withData("remotoDisponivel", remotoDisponivel.get())
                .withData("ultimaSincronizacao", getUltimaSincronizacao())
                .withData("temDadosPendentes", temDadosPendentes())
                .withData("intervaloVerificacao", VERIFICAR_INTERVALO);
    }

    // ============================================================
    // GETTERS
    // ============================================================

    public boolean isRemotoDisponivel() {
        return remotoDisponivel.get();
    }

    public boolean isSincronizando() {
        return sincronizando.get();
    }

    public boolean isRodando() {
        return rodando.get();
    }

    public String getUltimaSincronizacao() {
        String data = ultimaSincronizacao.get("ultima_sync_data");
        if (data == null) {
            return "Nunca sincronizado";
        }
        return data;
    }

    public int getTempoProximaVerificacao() {
        return VERIFICAR_INTERVALO;
    }
}