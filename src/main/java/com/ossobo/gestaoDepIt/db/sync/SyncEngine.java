package com.ossobo.gestaoDepIt.db.sync;

import com.ossobo.gestaoDepIt.db.config.DatabaseConnection;
import com.ossobo.gestaoDepIt.db.sync.event.SyncEvent;
import com.ossobo.gestaoDepIt.db.sync.models.EstadoSync;
import com.ossobo.gestaoDepIt.db.sync.models.PendingSync;
import com.ossobo.winterfx.anotations.Inject;
import com.ossobo.winterfx.anotations.PostConstruct;
import com.ossobo.winterfx.anotations.Service;
import com.ossobo.winterfx.event.EventBus;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * SyncEngine v1.1
 *
 * Motor de sincronização offline-first.
 * Substitui o SincronizadorDados legacy.
 *
 * v1.1: Estados SEM_CONFIG/OFFLINE/ONLINE (fonte única).
 * v1.0: Criação.
 */
@Service
public class SyncEngine {

    private static final System.Logger LOGGER = System.getLogger(SyncEngine.class.getName());

    private static final int VERIFICAR_INTERVALO = 30;
    private static final int VERIFICAR_INICIAL_DELAY = 5;
    private static final int PUSH_LIMITE = 100;

    private final AtomicBoolean rodando = new AtomicBoolean(false);
    private final AtomicBoolean sincronizando = new AtomicBoolean(false);
    private final AtomicBoolean remotoDisponivel = new AtomicBoolean(false);
    private boolean modoSemConfigLogado = false;

    private ScheduledExecutorService scheduler;

    @Inject
    private DatabaseConnection databaseConnection;

    @Inject
    private EventBus eventBus;

    @Inject
    private MysqlConnection mysqlConnection;

    @Inject
    private SyncStateRepository syncStateRepository;

    @Inject
    private ConflictResolver conflictResolver;

    @Inject
    private DeviceIdentity deviceIdentity;

    private final List<SyncAdapter<?>> adapters = new ArrayList<>();
    private final ConcurrentHashMap<String, LocalDateTime> ultimaSincronizacao = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        LOGGER.log(System.Logger.Level.INFO, "SyncEngine v1.1 inicializado");
        LOGGER.log(System.Logger.Level.INFO, "Verificação a cada {0}s", VERIFICAR_INTERVALO);
    }

    public void registrarAdapter(SyncAdapter<?> adapter) {
        adapters.add(adapter);
        LOGGER.log(System.Logger.Level.INFO, "Adapter registrado: {0}", adapter.entidade());
    }

    public synchronized void iniciar() {
        if (rodando.get()) {
            LOGGER.log(System.Logger.Level.WARNING, "SyncEngine já está rodando");
            return;
        }

        LOGGER.log(System.Logger.Level.INFO, "Iniciando SyncEngine...");

        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "SyncEngine-Thread");
            t.setDaemon(true);
            return t;
        });

        rodando.set(true);

        scheduler.scheduleAtFixedRate(
                this::ciclo,
                VERIFICAR_INICIAL_DELAY,
                VERIFICAR_INTERVALO,
                TimeUnit.SECONDS
        );

        LOGGER.log(System.Logger.Level.INFO, "SyncEngine iniciado");

        eventBus.publish(new SyncEvent(
                SyncEvent.Type.INICIADO,
                "Sistema",
                0, 0, 0,
                "SyncEngine iniciado"
        ));
    }

    public synchronized void parar() {
        if (!rodando.get()) return;

        LOGGER.log(System.Logger.Level.INFO, "Parando SyncEngine...");
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

        LOGGER.log(System.Logger.Level.INFO, "SyncEngine parado");
        eventBus.publish(new SyncEvent(
                SyncEvent.Type.PAUSADO,
                "Sistema",
                0, 0, 0,
                "SyncEngine parado"
        ));
    }

    // ============================================================
    // CICLO PRINCIPAL
    // ============================================================

    private void ciclo() {
        if (!rodando.get() || sincronizando.get()) {
            return;
        }

        try {
            DisponibilidadeRemota estado = mysqlConnection.verificarDisponibilidade();

            switch (estado) {
                case SEM_CONFIG -> {
                    if (!modoSemConfigLogado) {
                        LOGGER.log(System.Logger.Level.INFO, "Nenhuma configuração remota ativa — operando apenas local");
                        eventBus.publish(new SyncEvent(
                                SyncEvent.Type.PAUSADO,
                                "Sistema",
                                0, 0, 0,
                                "Nenhuma configuração remota ativa — operando apenas local"
                        ));
                        modoSemConfigLogado = true;
                    }
                    remotoDisponivel.set(false);
                }

                case OFFLINE -> {
                    if (remotoDisponivel.get()) {
                        LOGGER.log(System.Logger.Level.WARNING, "Servidor remoto inacessível — dados seguros no SQLite");
                        eventBus.publish(new SyncEvent(
                                SyncEvent.Type.FALHOU,
                                "Conexão",
                                0, 0, 1,
                                "Servidor remoto inacessível — dados seguros no SQLite"
                        ));
                        remotoDisponivel.set(false);
                    }
                }

                case ONLINE -> {
                    if (!remotoDisponivel.get()) {
                        LOGGER.log(System.Logger.Level.INFO, "Conexão remota restaurada!");
                        eventBus.publish(new SyncEvent(
                                SyncEvent.Type.INICIADO,
                                "Sistema",
                                0, 0, 0,
                                "Conexão restaurada — sincronizando"
                        ));
                        remotoDisponivel.set(true);
                    }
                    executarCiclo();
                }
            }

        } catch (Exception e) {
            LOGGER.log(System.Logger.Level.ERROR, "Erro no ciclo de sync", e);
        }
    }

    // ============================================================
    // EXECUÇÃO DO CICLO
    // ============================================================

    private void executarCiclo() throws Exception {
        if (sincronizando.get()) return;
        sincronizando.set(true);

        int totalPuxados = 0;
        int totalEmpurrados = 0;
        int totalFalhas = 0;

        try {
            LOGGER.log(System.Logger.Level.INFO, "Iniciando ciclo de sync...");

            for (SyncAdapter<?> adapter : adapters) {
                try {
                    syncStateRepository.marcarEmAndamento(adapter.entidade());

                    int puxados = puxar(adapter);
                    int empurrados = empurrar(adapter);

                    totalPuxados += puxados;
                    totalEmpurrados += empurrados;

                    ultimaSincronizacao.put(adapter.entidade(), LocalDateTime.now());
                    syncStateRepository.marcarSucesso(adapter.entidade(), LocalDateTime.now(),
                            String.format("PULL=%d, PUSH=%d", puxados, empurrados));

                    LOGGER.log(System.Logger.Level.INFO, "Sync {0}: PULL={1}, PUSH={2}",
                            adapter.entidade(), puxados, empurrados);

                } catch (Exception e) {
                    totalFalhas++;
                    syncStateRepository.marcarErro(adapter.entidade(), e.getMessage());
                    LOGGER.log(System.Logger.Level.ERROR, "Falha no sync de {0}: {1}",
                            adapter.entidade(), e.getMessage());
                }
            }

            LOGGER.log(System.Logger.Level.INFO, "Ciclo concluído: PULL={0}, PUSH={1}, FALHAS={2}",
                    totalPuxados, totalEmpurrados, totalFalhas);

            if (totalPuxados > 0 || totalEmpurrados > 0) {
                eventBus.publish(new SyncEvent(
                        SyncEvent.Type.CONCLUIDO,
                        "Sistema",
                        totalPuxados + totalEmpurrados,
                        totalPuxados + totalEmpurrados,
                        totalFalhas,
                        String.format("%d registros sincronizados", totalPuxados + totalEmpurrados)
                ));
            }

        } finally {
            sincronizando.set(false);
        }
    }

    // ============================================================
    // PULL: remoto → local
    // ============================================================

    private <T> int puxar(SyncAdapter<T> adapter) throws Exception {
        EstadoSync st = syncStateRepository.obterEstado(adapter.entidade());
        LocalDateTime horizonte = st != null ? st.lastSync() : null;

        List<T> remotos = adapter.buscarRemotosDesde(horizonte);
        if (remotos.isEmpty()) {
            return 0;
        }

        int aplicados = 0;
        for (T recebido : remotos) {
            ConflictResolver.SyncSnapshot snapRemoto = adapter.toSnapshot(recebido);
            if (snapRemoto == null) continue;

            ConflictResolver.SyncSnapshot snapLocal = adapter.snapshotDoLocal(snapRemoto.id());
            ConflictResolver.Decisao d = conflictResolver.resolver(snapLocal, snapRemoto, horizonte);

            switch (d.tipo()) {
                case APLICAR_RECEBIDO:
                case CONFLITO_RECEBIDO_VENCE:
                    adapter.aplicarNoLocal(recebido);
                    aplicados++;
                    break;

                case CONFLITO_DESTINO_VENCE:
                    T local = buscarLocalPorId(adapter, snapLocal.id());
                    if (local != null) {
                        adapter.aplicarNoRemoto(local);
                        aplicados++;
                    }
                    break;

                case MANTER_DESTINO:
                case JA_CONVERGIDO:
                    break;
            }

            if (d.tipo().name().startsWith("CONFLITO")) {
                LOGGER.log(System.Logger.Level.WARNING, "Conflito em {0}: {1}",
                        adapter.entidade(), d.motivo());
            }
        }

        return aplicados;
    }

    // ============================================================
    // PUSH: local → remoto
    // ============================================================

    private <T> int empurrar(SyncAdapter<T> adapter) throws Exception {
        List<PendingSync> pendencias = syncStateRepository.listarPendencias(PUSH_LIMITE);
        if (pendencias.isEmpty()) {
            return 0;
        }

        int aplicados = 0;
        List<String> idsRemover = new ArrayList<>();

        for (PendingSync pendencia : pendencias) {
            try {
                ConflictResolver.SyncSnapshot snapLocal = adapter.snapshotDoLocal(pendencia.registroId());
                if (snapLocal == null) {
                    idsRemover.add(pendencia.id());
                    continue;
                }

                ConflictResolver.SyncSnapshot snapRemoto = buscarRemotoPorId(adapter, pendencia.registroId());
                ConflictResolver.Decisao d = conflictResolver.resolver(snapRemoto, snapLocal, null);

                switch (d.tipo()) {
                    case APLICAR_RECEBIDO:
                    case CONFLITO_RECEBIDO_VENCE:
                        T registro = buscarLocalCompleto(adapter, pendencia.registroId());
                        if (registro != null) {
                            adapter.aplicarNoRemoto(registro);
                            idsRemover.add(pendencia.id());
                            aplicados++;
                        }
                        break;

                    case MANTER_DESTINO:
                    case CONFLITO_DESTINO_VENCE:
                    case JA_CONVERGIDO:
                        idsRemover.add(pendencia.id());
                        break;
                }

                if (d.tipo().name().startsWith("CONFLITO")) {
                    LOGGER.log(System.Logger.Level.WARNING, "Conflito no push de {0}: {1}",
                            adapter.entidade(), d.motivo());
                }

            } catch (Exception e) {
                syncStateRepository.incrementarTentativa(pendencia.id());
                LOGGER.log(System.Logger.Level.WARNING, "Falha no push de {0}: {1}",
                        pendencia.registroId(), e.getMessage());
            }
        }

        syncStateRepository.removerPendencias(idsRemover);
        return aplicados;
    }

    // ============================================================
    // MÉTODOS AUXILIARES (a serem implementados pelos adapters)
    // ============================================================

    @SuppressWarnings("unchecked")
    private <T> T buscarLocalPorId(SyncAdapter<T> adapter, String id) throws Exception {
        // Implementação específica por adapter
        return null;
    }

    private <T> ConflictResolver.SyncSnapshot buscarRemotoPorId(SyncAdapter<T> adapter, String id) {
        // Implementação específica por adapter
        return null;
    }

    private <T> T buscarLocalCompleto(SyncAdapter<T> adapter, String id) {
        // Implementação específica por adapter
        return null;
    }

    // ============================================================
    // MÉTODOS PÚBLICOS
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

    public String getUltimaSincronizacao(String entidade) {
        LocalDateTime dt = ultimaSincronizacao.get(entidade);
        return dt != null ? dt.toString() : "Nunca";
    }

    public int getPendencias() {
        return syncStateRepository.contarPendencias();
    }
}