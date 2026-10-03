package com.ossobo.gestaoDepIt.db.sync;

import com.ossobo.gestaoDepIt.db.config.DatabaseConnection;
import com.ossobo.gestaoDepIt.db.sync.models.EstadoSync;
import com.ossobo.gestaoDepIt.db.sync.models.PendingSync;
import com.ossobo.winterfx.anotations.Inject;
import com.ossobo.winterfx.anotations.Repository;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * SyncStateRepository v1.3
 *
 * Persistência dos metadados de sincronização LOCAIS:
 * relógio por entidade (sync_state) + fila de pendências (pending_sync).
 *
 * REGRA CRÍTICA: usa DatabaseConnection (SQLite FORÇADO).
 *
 * v1.3 — Adicionado overload registrarPendencia(Connection, PendingSync) para
 *        uso dentro de transação de negócio compartilhada (atomicidade entre
 *        o registro de negócio e sua pendência de sync).
 */
@Repository
public class SyncStateRepository {

    private static final System.Logger LOGGER = System.getLogger(SyncStateRepository.class.getName());

    @Inject
    private DatabaseConnection databaseConnection;

    // ===== RELÓGIO (sync_state) =====

    public EstadoSync obterEstado(String entidade) {
        String sql = """
                SELECT entidade, last_sync, ultimo_status, ultima_mensagem, atualizado_em
                FROM sync_state WHERE entidade = ?
                """;
        try (var conn = databaseConnection.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setString(1, entidade);
            try (var rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao obter estado de sync: " + entidade, e);
        }
    }

    public List<EstadoSync> obterTodosEstados() {
        String sql = "SELECT entidade, last_sync, ultimo_status, ultima_mensagem, atualizado_em FROM sync_state";
        try (var conn = databaseConnection.getConnection();
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

    public void resetarEstado(String entidade) {
        if (entidade == null || entidade.isBlank()) {
            throw new IllegalArgumentException("Entidade é obrigatória para reset");
        }
        String sql = "DELETE FROM sync_state WHERE entidade = ?";
        try (var conn = databaseConnection.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setString(1, entidade);
            int removidos = ps.executeUpdate();
            LOGGER.log(System.Logger.Level.WARNING, "Relógio de sync resetado: {0} ({1} linha)",
                    entidade, removidos);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao resetar estado de sync: " + entidade, e);
        }
    }

    public void resetarTodosEstados() {
        try (var conn = databaseConnection.getConnection();
             var ps = conn.prepareStatement("DELETE FROM sync_state")) {
            int removidos = ps.executeUpdate();
            LOGGER.log(System.Logger.Level.WARNING, "TODOS os relógios de sync resetados ({0})", removidos);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao resetar todos os estados de sync", e);
        }
    }

    public void removerPendencia(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("ID da pendência é obrigatório");
        }
        String sql = "DELETE FROM pending_sync WHERE id = ?";
        try (var conn = databaseConnection.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            int removidos = ps.executeUpdate();
            if (removidos > 0) {
                LOGGER.log(System.Logger.Level.WARNING, "Pendência descartada: {0}", id);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao descartar pendência " + id, e);
        }
    }

    public void limparPendencias() {
        try (var conn = databaseConnection.getConnection();
             var ps = conn.prepareStatement("DELETE FROM pending_sync")) {
            int removidos = ps.executeUpdate();
            LOGGER.log(System.Logger.Level.WARNING, "Fila de pendências esvaziada ({0} registros)", removidos);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao limpar pendências", e);
        }
    }

    public void marcarSucesso(String entidade, LocalDateTime momento, String mensagem) {
        upsert(entidade, momento, "OK", mensagem);
    }

    public void marcarErro(String entidade, String mensagem) {
        EstadoSync atual = obterEstado(entidade);
        upsert(entidade, atual != null ? atual.lastSync() : null, "ERRO", mensagem);
    }

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
        try (var conn = databaseConnection.getConnection();
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

    // ===== FILA DE PENDÊNCIAS =====

    /**
     * Registra pendência em conexão própria (autônoma).
     * Encapsula SQLException em RuntimeException — uso fora de transação de negócio.
     */
    public void registrarPendencia(PendingSync p) {
        String sql = """
                INSERT INTO pending_sync (id, tabela, registro_id, operacao, criado_em, tentativas)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (var conn = databaseConnection.getConnection();
             var ps = conn.prepareStatement(sql)) {
            preencher(ps, p);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao registrar pendência de sync", e);
        }
    }

    /**
     * Registra pendência em Connection EXTERNA, sem abrir transação própria.
     * O chamador controla commit/rollback — garante atomicidade entre o
     * registro de negócio e sua pendência de sync.
     *
     * Propaga SQLException (checked) para que o chamador possa executar rollback
     * consciente. Não confundir com registrarPendencia(PendingSync), que é
     * autônomo e encapsula a exceção.
     *
     * @param conn conexão já aberta e em modo transacional pelo chamador
     * @param p    pendência a registrar
     */
    public void registrarPendencia(Connection conn, PendingSync p) throws SQLException {
        String sql = """
                INSERT INTO pending_sync (id, tabela, registro_id, operacao, criado_em, tentativas)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (var ps = conn.prepareStatement(sql)) {
            preencher(ps, p);
            ps.executeUpdate();
        }
    }

    public void registrarPendencias(List<PendingSync> pendencias) {
        if (pendencias == null || pendencias.isEmpty()) {
            return;
        }
        String sql = """
                INSERT INTO pending_sync (id, tabela, registro_id, operacao, criado_em, tentativas)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (var conn = databaseConnection.getConnection()) {
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

    public List<PendingSync> listarPendencias(int limite) {
        String sql = """
                SELECT id, tabela, registro_id, operacao, criado_em, tentativas
                FROM pending_sync ORDER BY criado_em ASC LIMIT ?
                """;
        try (var conn = databaseConnection.getConnection();
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

    public void removerPendencias(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        String sql = "DELETE FROM pending_sync WHERE id = ?";
        try (var conn = databaseConnection.getConnection()) {
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

    public void incrementarTentativa(String id) {
        String sql = "UPDATE pending_sync SET tentativas = tentativas + 1 WHERE id = ?";
        try (var conn = databaseConnection.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao incrementar tentativa da pendência " + id, e);
        }
    }

    public int contarPendencias() {
        try (var conn = databaseConnection.getConnection();
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