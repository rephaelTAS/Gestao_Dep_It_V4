package com.ossobo.gestaoDepIt.db.repositories;

import com.ossobo.gestaoDepIt.db.config.DatabaseConfig;
import com.ossobo.gestaoDepIt.db.models.EstadoSync;
import com.ossobo.gestaoDepIt.db.models.PendingSync;
import com.ossobo.winterfx.anotations.Inject;
import com.ossobo.winterfx.anotations.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * SyncStateRepository v1.1
 *
 * Objetivo: Persistência dos metadados de sincronização LOCAIS:
 *           relógio por entidade (sync_state) + fila de pendências (pending_sync).
 *
 * ⚠️ REGRA CRÍTICA: usa DatabaseConfig (SQLite FORÇADO). Estes dados são
 *    por-dispositivo e JAMAIS migram para o remoto. Nunca trocar por
 *    DatabaseConnection (que rotearia ao MySQL).
 *
 * v1.1 - Métodos administrativos: reset de relógio(s) (força bootstrap full
 *        na próxima execução) e descarte de pendências (housekeeping).
 * v1.0 - Criação. PreparedStatement em 100% das operações (lição C1).
 */
@Repository
public class SyncStateRepository {

    private static final Logger logger = LoggerFactory.getLogger(SyncStateRepository.class);

    @Inject
    private DatabaseConfig databaseConfig;

    // ===== REGISTRO DE ESTADO (record aninhado — consumido pelo Engine e pela UI) =====



    // ===== RELÓGIO (sync_state) =====

    /** Estado de uma entidade; vazio = nunca sincronizou. */
    public EstadoSync obterEstado(String entidade) {
        String sql = """
                SELECT entidade, last_sync, ultimo_status, ultima_mensagem, atualizado_em
                FROM sync_state WHERE entidade = ?
                """;
        try (var conn = databaseConfig.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setString(1, entidade);
            try (var rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao obter estado de sync: " + entidade, e);
        }
    }

    /** Todos os estados (rota sync/status itera isto). */
    public List<EstadoSync> obterTodosEstados() {
        String sql = "SELECT entidade, last_sync, ultimo_status, ultima_mensagem, atualizado_em FROM sync_state";
        try (var conn = databaseConfig.getConnection();
             var ps = conn.prepareStatement(sql);
             var rs = ps.executeQuery()) {
            List<EstadoSync> estados = new ArrayList<>();
            while (rs.next()) {
                estados.add(mapear(rs));
            }
            return estados;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar estados de sync", e);
        }
    }

    /**
     * Apaga o estado de UMA entidade — ela volta a "nunca sincronizou",
     * forçando BOOTSTRAP FULL (união por UUID, seguro) na próxima execução.
     * Uso: troca de servidor, recuperação de desastre, testes.
     */
    public void resetarEstado(String entidade) {
        if (entidade == null || entidade.isBlank()) {
            throw new IllegalArgumentException("Entidade é obrigatória para reset");
        }
        String sql = "DELETE FROM sync_state WHERE entidade = ?";
        try (var conn = databaseConfig.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setString(1, entidade);
            int removidos = ps.executeUpdate();
            logger.warn("♻️ Relógio de sync resetado: {} ({} linha). Próximo ciclo fará bootstrap full.",
                    entidade, removidos);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao resetar estado de sync: " + entidade, e);
        }
    }

    /**
     * Apaga TODOS os estados — todas as entidades farão bootstrap full
     * na próxima execução. Ação administrativa extrema.
     */
    public void resetarTodosEstados() {
        try (var conn = databaseConfig.getConnection();
             var ps = conn.prepareStatement("DELETE FROM sync_state")) {
            int removidos = ps.executeUpdate();
            logger.warn("♻️ TODOS os relógios de sync resetados ({}). Bootstrap full no próximo ciclo.", removidos);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao resetar todos os estados de sync", e);
        }
    }

    /**
     * Descarta UMA pendência específica. Uso legítimo: pendência órfã cujo
     * registro foi apagado localmente por manutenção manual.
     * ⚠️ Descartar pendência = essa alteração NUNCA chegará ao remoto.
     */
    public void removerPendencia(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("ID da pendência é obrigatório");
        }
        String sql = "DELETE FROM pending_sync WHERE id = ?";
        try (var conn = databaseConfig.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            int removidos = ps.executeUpdate();
            if (removidos > 0) {
                logger.warn("🗑️ Pendência descartada: {}", id);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao descartar pendência " + id, e);
        }
    }

    /**
     * Esvazia a fila inteira. Ação administrativa EXTREMA: todas as
     * alterações locais pendentes serão perdidas para o remoto.
     * Uso típico: acompanhado de resetarTodosEstados() para re-bootstrap limpo.
     */
    public void limparPendencias() {
        try (var conn = databaseConfig.getConnection();
             var ps = conn.prepareStatement("DELETE FROM pending_sync")) {
            int removidos = ps.executeUpdate();
            logger.warn("🗑️ Fila de pendências esvaziada ({} registros). Alterações locais NÃO serão enviadas.", removidos);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao limpar pendências", e);
        }
    }

    /** Carimba sucesso: relógio avança para o momento do ciclo. */
    public void marcarSucesso(String entidade, LocalDateTime momento, String mensagem) {
        upsert(entidade, momento, "OK", mensagem);
    }

    /** Registra falha — relógio NÃO avança (próximo ciclo reenvia). */
    public void marcarErro(String entidade, String mensagem) {
        EstadoSync atual = obterEstado(entidade);
        upsert(entidade, atual != null ? atual.lastSync() : null, "ERRO", mensagem);
    }

    /** Marca início de ciclo (UI pode mostrar spinner). */
    public void marcarEmAndamento(String entidade) {
        EstadoSync atual = obterEstado(entidade);
        upsert(entidade, atual != null ? atual.lastSync() : null, "EM_ANDAMENTO", null);
    }

    private void upsert(String entidade, LocalDateTime lastSync, String status, String mensagem) {
        String sql = """
                INSERT OR REPLACE INTO sync_state
                    (entidade, last_sync, ultimo_status, ultima_mensagem, atualizado_em)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (var conn = databaseConfig.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setString(1, entidade);
            ps.setString(2, lastSync != null ? lastSync.toString() : null);
            ps.setString(3, status);
            ps.setString(4, mensagem);
            ps.setString(5, LocalDateTime.now().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao gravar estado de sync: " + entidade, e);
        }
    }

    private EstadoSync mapear(java.sql.ResultSet rs) throws SQLException {
        String lastSync = rs.getString("last_sync");
        String atualizado = rs.getString("atualizado_em");
        return new EstadoSync(
                rs.getString("entidade"),
                lastSync != null ? LocalDateTime.parse(lastSync) : null,
                rs.getString("ultimo_status"),
                rs.getString("ultima_mensagem"),
                atualizado != null ? LocalDateTime.parse(atualizado) : null
        );
    }

    // ===== FILA DE PENDÊNCIAS (pending_sync) =====

    /** Insere 1 pendência (uso: repositories de negócio no ato da escrita offline). */
    public void registrarPendencia(PendingSync p) {
        String sql = """
                INSERT INTO pending_sync (id, tabela, registro_id, operacao, criado_em, tentativas)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (var conn = databaseConfig.getConnection();
             var ps = conn.prepareStatement(sql)) {
            preencher(ps, p);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao registrar pendência de sync", e);
        }
    }

    /** Insere lote em UMA transação (mesma atomicidade da escrita de negócio offline). */
    public void registrarPendencias(List<PendingSync> pendencias) {
        if (pendencias == null || pendencias.isEmpty()) {
            return;
        }
        String sql = """
                INSERT INTO pending_sync (id, tabela, registro_id, operacao, criado_em, tentativas)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (var conn = databaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try (var ps = conn.prepareStatement(sql)) {
                for (PendingSync p : pendencias) {
                    preencher(ps, p);
                    ps.addBatch();
                }
                ps.executeBatch();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao registrar lote de pendências", e);
        }
    }

    /** Lote para o PUSH — FIFO por criação. */
    public List<PendingSync> listarPendencias(int limite) {
        String sql = """
                SELECT id, tabela, registro_id, operacao, criado_em, tentativas
                FROM pending_sync ORDER BY criado_em ASC LIMIT ?
                """;
        try (var conn = databaseConfig.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limite);
            try (var rs = ps.executeQuery()) {
                List<PendingSync> lista = new ArrayList<>();
                while (rs.next()) {
                    lista.add(new PendingSync(
                            rs.getString("id"),
                            rs.getString("tabela"),
                            rs.getString("registro_id"),
                            rs.getString("operacao"),
                            LocalDateTime.parse(rs.getString("criado_em")),
                            rs.getInt("tentativas")));
                }
                return lista;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar pendências", e);
        }
    }

    /** Remove pendências já confirmadas pelo remoto (fim do PUSH bem-sucedido). */
    public void removerPendencias(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        String sql = "DELETE FROM pending_sync WHERE id = ?";
        try (var conn = databaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try (var ps = conn.prepareStatement(sql)) {
                for (String id : ids) {
                    ps.setString(1, id);
                    ps.addBatch();
                }
                ps.executeBatch();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao remover pendências confirmadas", e);
        }
    }

    /** Incrementa contador de tentativas (push falhou; retrys com backoff). */
    public void incrementarTentativa(String id) {
        String sql = "UPDATE pending_sync SET tentativas = tentativas + 1 WHERE id = ?";
        try (var conn = databaseConfig.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao incrementar tentativa da pendência " + id, e);
        }
    }

    /** Tamanho da fila — alimenta rota sync/pending e ícone de status da UI. */
    public int contarPendencias() {
        try (var conn = databaseConfig.getConnection();
             var ps = conn.prepareStatement("SELECT COUNT(*) AS total FROM pending_sync");
             var rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt("total") : 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao contar pendências", e);
        }
    }

    private void preencher(java.sql.PreparedStatement ps, PendingSync p) throws SQLException {
        ps.setString(1, p.id());
        ps.setString(2, p.tabela());
        ps.setString(3, p.registroId());
        ps.setString(4, p.operacao());
        ps.setString(5, p.criadoEm().toString());
        ps.setInt(6, p.tentativas());
    }
}