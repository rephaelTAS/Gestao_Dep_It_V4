package com.ossobo.gestaoDepIt.db.services;

import com.ossobo.gestaoDepIt.db.models.EstadoSync;
import com.ossobo.gestaoDepIt.db.models.PendingSync;
import com.ossobo.gestaoDepIt.db.repositories.SyncStateRepository;
import com.ossobo.winterfx.anotations.Component;
import com.ossobo.winterfx.anotations.DeleteMapping;
import com.ossobo.winterfx.anotations.GetMapping;
import com.ossobo.winterfx.anotations.Inject;
import com.ossobo.winterfx.anotations.PutMapping;
import com.ossobo.winterfx.anotations.RequestMapping;
import com.ossobo.winterfx.anotations.RouteVar;
import com.ossobo.winterfx.router.model.ResponseData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * SyncRoutes v1.0
 *
 * Responsabilidade: Fronteira de Internal Routing dos metadados de
 *                   sincronização (sync_state + pending_sync + device_id).
 *                   Handlers FINOS sobre infraestrutura local.
 *
 * Decisão arquitetural: injeta SyncStateRepository e DeviceIdentity DIRETO
 * (infraestrutura local, sem service intermediário). O SyncEngine (Degrau 4)
 * será o service natural deste módulo — a rota "executar" entra lá.
 *
 * Contratos (documentação viva):
 *   Entidade : nome da tabela de negócio ('catalogo_produtos', 'inventario'...).
 *   Reset    : apagar relógio(s) força BOOTSTRAP FULL no próximo ciclo
 *              (união por UUID — seguro, sem perda de dados).
 *   Descarte : remover pendência = alteração local NUNCA chegará ao remoto.
 *
 * v1.0 - Criação; 9 rotas (GET 5 / PUT 2 / DELETE 2). Rotas de comando
 *        (sync/executar) entram no Degrau 4 com o SyncEngine.
 */
@Component
@RequestMapping("sync")
public class SyncRoutes {

    private static final Logger logger = LoggerFactory.getLogger(SyncRoutes.class);

    @Inject
    private SyncStateRepository syncStateRepository;

    @Inject
    private com.ossobo.gestaoDepIt.sync.DeviceIdentity deviceIdentity;

    // ============================================================
    // ROTAS — LEITURA (GET)
    // ============================================================

    /** Estado de todas as entidades já sincronizadas (tela de diagnóstico). */
    @GetMapping("status")
    public ResponseData status() {
        try {
            List<EstadoSync> estados = syncStateRepository.obterTodosEstados();
            return ResponseData.success()
                    .withData("estados", estados)
                    .withData("total", estados.size())
                    .withData("dispositivo", deviceIdentity.getDeviceId());
        } catch (RuntimeException e) {
            return erroInterno(e);
        }
    }

    /** Estado de UMA entidade. Null = nunca sincronizou (estado válido). */
    @GetMapping("status/por/entidade")
    public ResponseData statusPorEntidade(@RouteVar("entidade") String entidade) {
        if (entidade == null || entidade.isBlank()) {
            return ResponseData.error("Entidade é obrigatória")
                    .withError("entidade", "ausente");
        }
        try {
            EstadoSync estado = syncStateRepository.obterEstado(entidade);
            return ResponseData.success()
                    .withData("estado", estado)
                    .withData("nuncaSincronizado", estado == null);
        } catch (RuntimeException e) {
            return erroInterno(e);
        }
    }

    /** Pendências aguardando PUSH. Chave "limite" OPCIONAL (default 100, FIFO). */
    @GetMapping("pending")
    public ResponseData pendencias(@RouteVar("limite") Integer limite) {
        try {
            int efetivo = (limite == null || limite < 1) ? 100 : Math.min(limite, 1000);
            List<PendingSync> fila = syncStateRepository.listarPendencias(efetivo);
            return ResponseData.success()
                    .withData("pendencias", fila)
                    .withData("total", fila.size())
                    .withData("totalFila", syncStateRepository.contarPendencias());
        } catch (RuntimeException e) {
            return erroInterno(e);
        }
    }

    /** Contagem da fila — alimenta ícone de status da UI (⏳ pendências). */
    @GetMapping("pending/contagem")
    public ResponseData contagemPendencias() {
        try {
            return ResponseData.success()
                    .withData("total", syncStateRepository.contarPendencias());
        } catch (RuntimeException e) {
            return erroInterno(e);
        }
    }

    /** Identidade desta instalação (diagnóstico/suporte). */
    @GetMapping("dispositivo")
    public ResponseData dispositivo() {
        try {
            return ResponseData.success()
                    .withData("deviceId", deviceIdentity.getDeviceId());
        } catch (RuntimeException e) {
            return erroInterno(e);
        }
    }

    // ============================================================
    // ROTAS — RESET DE RELÓGIO (PUT) — força bootstrap full
    // ============================================================

    /** Reseta o relógio de UMA entidade → bootstrap full dela no próximo ciclo. */
    @PutMapping("state/reset/por/entidade")
    public ResponseData resetarEstado(@RouteVar("entidade") String entidade) {
        return escrita(() -> {
            syncStateRepository.resetarEstado(entidade);
            return "Relógio de '" + entidade + "' resetado — bootstrap full no próximo ciclo";
        }, "mensagem");
    }

    /** Reseta TODOS os relógios → bootstrap full completo. Extremo. */
    @PutMapping("state/reset/todos")
    public ResponseData resetarTodos() {
        return escrita(() -> {
            syncStateRepository.resetarTodosEstados();
            return "Todos os relógios resetados — bootstrap full no próximo ciclo";
        }, "mensagem");
    }

    // ============================================================
    // ROTAS — MANUTENÇÃO DA FILA (DELETE)
    // ============================================================

    /** Descarta 1 pendência (ex.: registro órfão). ⚠️ Alteração nunca irá ao remoto. */
    @DeleteMapping("pending/por/id")
    public ResponseData descartarPendencia(@RouteVar("id") String id) {
        return escrita(() -> {
            syncStateRepository.removerPendencia(id);
            return "Pendência descartada";
        }, "mensagem");
    }

    /** Esvazia a fila INTEIRA. ⚠️ TODAS as alterações locais pendentes serão perdidas. */
    @DeleteMapping("pending/todas")
    public ResponseData limparPendencias() {
        return escrita(() -> {
            syncStateRepository.limparPendencias();
            return "Fila de pendências esvaziada";
        }, "mensagem");
    }

    // ============================================================
    // HELPERS DE FRONTEIRA (padrão unificado da família Routes)
    // ============================================================

    @FunctionalInterface
    private interface Acao {
        String executar();
    }

    private ResponseData escrita(Acao acao) {
        try {
            return ResponseData.success().withData("mensagem", acao.executar());
        } catch (IllegalArgumentException | IllegalStateException e) {
            logger.warn("⚠️ Validação de sync: {}", e.getMessage());
            return ResponseData.error(e.getMessage()).withError("negocio", e.getMessage());
        }
    }

    private ResponseData erroInterno(RuntimeException e) {
        logger.error("❌ Erro nos metadados de sync: {}", e.getMessage(), e);
        return ResponseData.error("Erro de sincronização: " + e.getMessage())
                .withError("sync", e.getMessage());
    }
}