package com.ossobo.gestaoDepIt.db.repositories;

import com.ossobo.gestaoDepIt.db.config.DatabaseConnection;
import com.ossobo.gestaoDepIt.db.models.Funcionarios;
import com.ossobo.gestaoDepIt.db.sync.DeviceIdentity;
import com.ossobo.gestaoDepIt.db.sync.SyncStateRepository;
import com.ossobo.gestaoDepIt.db.sync.models.PendingSync;
import com.ossobo.winterfx.anotations.Inject;
import com.ossobo.winterfx.anotations.Repository;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * FuncionariosRepository v2.4 - Migrado para UUID + colunas de sync
 *
 * v2.4 — Transferência de departamento:
 *        - coluna cod_dep_anterior incluída em INSERT/UPDATE/mapper.
 *        - método transferirDepartamento(id, novoCodDep, codDepAnterior)
 *          atômico: UPDATE cod_dep + cod_dep_anterior + updated_at + device_id.
 *
 * v2.3 — getLocalDateTime tolerante (ISO + SQLite).
 * v2.2 — registrarPendencia com Connection compartilhada.
 */
@Repository
public class FuncionariosRepository {

    private static final System.Logger LOGGER = System.getLogger(FuncionariosRepository.class.getName());

    private static final DateTimeFormatter SQLITE_TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss[.SSS]");

    @Inject
    private DatabaseConnection dbConnection;

    @Inject
    private DeviceIdentity deviceIdentity;

    @Inject
    private SyncStateRepository syncStateRepository;

    private static final String TABLE = "funcionarios";

    // ===== SQL =====

    private static final String SQL_FIND_ALL = """
            SELECT * FROM %s
            ORDER BY nome, departamento
            """.formatted(TABLE);

    private static final String SQL_FIND_ALL_ATIVOS = """
            SELECT * FROM %s
            WHERE ativo = 1 AND deleted = 0
            ORDER BY nome, departamento
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_ID = """
            SELECT * FROM %s
            WHERE id = ?
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_COD = """
            SELECT * FROM %s
            WHERE cod_dep = ? AND deleted = 0
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_EMAIL = """
            SELECT * FROM %s
            WHERE email = ? AND deleted = 0
            """.formatted(TABLE);

    private static final String SQL_INSERT = """
            INSERT INTO %s (
                id, cod_dep, cod_dep_anterior, nome, funcao, departamento, local_trabalho,
                email, telefone, imagem_perfil, tipo_imagem, tamanho_imagem,
                ativo, created_at, updated_at, device_id, deleted
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)
            """.formatted(TABLE);

    private static final String SQL_UPDATE = """
            UPDATE %s
            SET nome = ?, funcao = ?, departamento = ?, local_trabalho = ?,
                email = ?, telefone = ?, ativo = ?, updated_at = ?
            WHERE cod_dep = ? AND deleted = 0
            """.formatted(TABLE);

    private static final String SQL_UPDATE_COMPLETO = """
            UPDATE %s
            SET nome = ?, funcao = ?, departamento = ?, local_trabalho = ?,
                email = ?, telefone = ?, ativo = ?, imagem_perfil = ?,
                tipo_imagem = ?, tamanho_imagem = ?, updated_at = ?
            WHERE cod_dep = ? AND deleted = 0
            """.formatted(TABLE);

    private static final String SQL_TRANSFERIR_DEPARTAMENTO = """
            UPDATE %s
            SET cod_dep = ?, cod_dep_anterior = ?, updated_at = ?, device_id = ?
            WHERE id = ? AND deleted = 0
            """.formatted(TABLE);

    private static final String SQL_UPDATE_IMAGEM = """
            UPDATE %s
            SET imagem_perfil = ?, tipo_imagem = ?, tamanho_imagem = ?,
                updated_at = ?
            WHERE cod_dep = ? AND deleted = 0
            """.formatted(TABLE);

    private static final String SQL_REMOVER_IMAGEM = """
            UPDATE %s
            SET imagem_perfil = NULL, tipo_imagem = NULL, tamanho_imagem = NULL,
                updated_at = ?
            WHERE cod_dep = ? AND deleted = 0
            """.formatted(TABLE);

    private static final String SQL_GET_IMAGEM = """
            SELECT imagem_perfil FROM %s
            WHERE cod_dep = ? AND deleted = 0
            """.formatted(TABLE);

    private static final String SQL_TOMBSTONE = """
            UPDATE %s
            SET deleted = 1, updated_at = ?, device_id = ?
            WHERE id = ?
            """.formatted(TABLE);

    private static final String SQL_DESATIVAR = """
            UPDATE %s
            SET ativo = 0, updated_at = ?
            WHERE cod_dep = ? AND deleted = 0
            """.formatted(TABLE);

    private static final String SQL_ATIVAR = """
            UPDATE %s
            SET ativo = 1, updated_at = ?
            WHERE cod_dep = ? AND deleted = 0
            """.formatted(TABLE);

    private static final String SQL_APLICAR_REMOTO = """
            INSERT OR REPLACE INTO %s (
                id, cod_dep, cod_dep_anterior, nome, funcao, departamento, local_trabalho,
                email, telefone, imagem_perfil, tipo_imagem, tamanho_imagem,
                ativo, created_at, updated_at, device_id, deleted
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.formatted(TABLE);

    // ===== CRUD =====

    public List<Funcionarios> findAll() throws SQLException {
        return executeQuery(SQL_FIND_ALL);
    }

    public List<Funcionarios> findAll(int limit, int offset) throws SQLException {
        String sql = SQL_FIND_ALL + " LIMIT ? OFFSET ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            stmt.setInt(2, offset);
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    public List<Funcionarios> findAllAtivos(int limit, int offset) throws SQLException {
        String sql = SQL_FIND_ALL_ATIVOS + " LIMIT ? OFFSET ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            stmt.setInt(2, offset);
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    public Optional<Funcionarios> findById(String id) throws SQLException {
        if (id == null || id.isBlank()) return Optional.empty();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_ID)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapResultSet(rs)) : Optional.empty();
            }
        }
    }

    public Optional<Funcionarios> findByCodDep(String codDep) throws SQLException {
        if (codDep == null || codDep.isBlank()) return Optional.empty();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_COD)) {
            stmt.setString(1, codDep);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapResultSet(rs)) : Optional.empty();
            }
        }
    }

    public Optional<Funcionarios> findByEmail(String email) throws SQLException {
        if (email == null || email.isBlank()) return Optional.empty();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_EMAIL)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapResultSet(rs)) : Optional.empty();
            }
        }
    }

    // ===== ESCRITA LOCAL (com carimbo + pendência) =====

    public String insert(Funcionarios f) throws SQLException {
        if (f == null) throw new IllegalArgumentException("Funcionário inválido");

        String id = (f.id() == null || f.id().isBlank())
                ? UUID.randomUUID().toString()
                : f.id();

        String agora = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS).toString();
        String deviceId = deviceIdentity.getDeviceId();

        try (Connection conn = dbConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmt = conn.prepareStatement(SQL_INSERT)) {
                stmt.setString(1, id);
                stmt.setString(2, f.codDep());
                stmt.setString(3, f.codDepAnterior());
                stmt.setString(4, f.nome());
                stmt.setString(5, f.funcao());
                stmt.setString(6, f.departamento());
                stmt.setString(7, f.localTrabalho());
                stmt.setString(8, f.email());
                stmt.setString(9, f.telefone());
                stmt.setBytes(10, f.imagemPerfil());
                stmt.setString(11, f.tipoImagem());
                stmt.setObject(12, f.tamanhoImagem());
                stmt.setBoolean(13, f.isAtivo());
                stmt.setString(14, agora);
                stmt.setString(15, agora);
                stmt.setString(16, deviceId);
                stmt.executeUpdate();

                PendingSync pendencia = PendingSync.nova("funcionarios", id, PendingSync.OP_UPSERT);
                syncStateRepository.registrarPendencia(conn, pendencia);

                conn.commit();
                LOGGER.log(System.Logger.Level.INFO, "Funcionário inserido: {0} ({1})", id, f.codDep());
                return id;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public void update(Funcionarios f) throws SQLException {
        if (f == null) throw new IllegalArgumentException("Funcionário inválido");

        String agora = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS).toString();

        try (Connection conn = dbConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE)) {
                stmt.setString(1, f.nome());
                stmt.setString(2, f.funcao());
                stmt.setString(3, f.departamento());
                stmt.setString(4, f.localTrabalho());
                stmt.setString(5, f.email());
                stmt.setString(6, f.telefone());
                stmt.setBoolean(7, f.isAtivo());
                stmt.setString(8, agora);
                stmt.setString(9, f.codDep());

                int affected = stmt.executeUpdate();
                if (affected == 0) {
                    throw new SQLException("Funcionário com código " + f.codDep() + " não encontrado");
                }

                PendingSync pendencia = PendingSync.nova("funcionarios", f.id(), PendingSync.OP_UPSERT);
                syncStateRepository.registrarPendencia(conn, pendencia);

                conn.commit();
                LOGGER.log(System.Logger.Level.INFO, "Funcionário atualizado: {0}", f.codDep());
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public void updateCompleto(Funcionarios f) throws SQLException {
        if (f == null) throw new IllegalArgumentException("Funcionário inválido");

        String agora = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS).toString();

        try (Connection conn = dbConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE_COMPLETO)) {
                stmt.setString(1, f.nome());
                stmt.setString(2, f.funcao());
                stmt.setString(3, f.departamento());
                stmt.setString(4, f.localTrabalho());
                stmt.setString(5, f.email());
                stmt.setString(6, f.telefone());
                stmt.setBoolean(7, f.isAtivo());
                stmt.setBytes(8, f.imagemPerfil());
                stmt.setString(9, f.tipoImagem());
                stmt.setObject(10, f.tamanhoImagem());
                stmt.setString(11, agora);
                stmt.setString(12, f.codDep());

                int affected = stmt.executeUpdate();
                if (affected == 0) {
                    throw new SQLException("Funcionário com código " + f.codDep() + " não encontrado");
                }

                PendingSync pendencia = PendingSync.nova("funcionarios", f.id(), PendingSync.OP_UPSERT);
                syncStateRepository.registrarPendencia(conn, pendencia);

                conn.commit();
                LOGGER.log(System.Logger.Level.INFO, "Funcionário atualizado completo: {0}", f.codDep());
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * Transferência de departamento: atualiza cod_dep + cod_dep_anterior
     * atomicamente. Devolve void — o caller (Service) faz a orquestração
     * completa (inventário + histórico) na MESMA transação.
     *
     * NÃO usa transação própria — recebe a Connection para permitir
     * composição atômica entre repositórios (inventário + histórico).
     */
    public void transferirDepartamento(Connection conn,
                                       String id,
                                       String novoCodDep,
                                       String codDepAnterior) throws SQLException {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("ID é obrigatório");
        }
        if (novoCodDep == null || novoCodDep.isBlank()) {
            throw new IllegalArgumentException("Novo cod_dep é obrigatório");
        }

        String agora = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS).toString();
        String deviceId = deviceIdentity.getDeviceId();

        try (PreparedStatement stmt = conn.prepareStatement(SQL_TRANSFERIR_DEPARTAMENTO)) {
            stmt.setString(1, novoCodDep);
            stmt.setString(2, codDepAnterior != null ? codDepAnterior : "");
            stmt.setString(3, agora);
            stmt.setString(4, deviceId);
            stmt.setString(5, id);

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Funcionário " + id + " não encontrado para transferência");
            }

            PendingSync pendencia = PendingSync.nova("funcionarios", id, PendingSync.OP_UPSERT);
            syncStateRepository.registrarPendencia(conn, pendencia);

            LOGGER.log(System.Logger.Level.INFO,
                    "Transferência: {0} → {1} (funcionario id={2})",
                    codDepAnterior, novoCodDep, id);
        }
    }

    // ===== TOMBSTONE (DELETE lógico) =====

    public void marcarDeletado(String id) throws SQLException {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("ID é obrigatório");
        }

        String agora = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS).toString();

        try (Connection conn = dbConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmt = conn.prepareStatement(SQL_TOMBSTONE)) {
                stmt.setString(1, agora);
                stmt.setString(2, deviceIdentity.getDeviceId());
                stmt.setString(3, id);

                int affected = stmt.executeUpdate();
                if (affected == 0) {
                    throw new SQLException("Funcionário " + id + " não encontrado");
                }

                PendingSync pendencia = PendingSync.nova("funcionarios", id, PendingSync.OP_UPSERT);
                syncStateRepository.registrarPendencia(conn, pendencia);

                conn.commit();
                LOGGER.log(System.Logger.Level.INFO, "Funcionário marcado como deletado: {0}", id);
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    // ===== APLICAR DO REMOTO (PULL - sem carimbo, sem pendência) =====

    public void aplicarDoRemoto(Funcionarios f) throws SQLException {
        if (f == null) throw new IllegalArgumentException("Funcionário inválido");

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_APLICAR_REMOTO)) {

            stmt.setString(1, f.id());
            stmt.setString(2, f.codDep());
            stmt.setString(3, f.codDepAnterior());
            stmt.setString(4, f.nome());
            stmt.setString(5, f.funcao());
            stmt.setString(6, f.departamento());
            stmt.setString(7, f.localTrabalho());
            stmt.setString(8, f.email());
            stmt.setString(9, f.telefone());
            stmt.setBytes(10, f.imagemPerfil());
            stmt.setString(11, f.tipoImagem());
            stmt.setObject(12, f.tamanhoImagem());
            stmt.setBoolean(13, f.isAtivo());
            stmt.setString(14, f.createdAt() != null ? f.createdAt().toString() : null);
            stmt.setString(15, f.updatedAt() != null ? f.updatedAt().toString() : null);
            stmt.setString(16, f.deviceId());
            stmt.setBoolean(17, f.isDeletado());

            stmt.executeUpdate();
            LOGGER.log(System.Logger.Level.DEBUG, "Aplicado do remoto: {0}", f.id());
        }
    }

    // ===== OPERAÇÕES DE IMAGEM =====

    public void updateImagemPerfil(String codDep, byte[] imagem, String tipo, Integer tamanho) throws SQLException {
        String agora = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS).toString();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE_IMAGEM)) {

            stmt.setBytes(1, imagem);
            stmt.setString(2, tipo);
            stmt.setInt(3, tamanho != null ? tamanho : (imagem != null ? imagem.length : 0));
            stmt.setString(4, agora);
            stmt.setString(5, codDep);

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Funcionário com código " + codDep + " não encontrado");
            }
            LOGGER.log(System.Logger.Level.INFO, "Imagem atualizada: {0}", codDep);
        }
    }

    public void removerImagemPerfil(String codDep) throws SQLException {
        String agora = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS).toString();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_REMOVER_IMAGEM)) {

            stmt.setString(1, agora);
            stmt.setString(2, codDep);

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Funcionário com código " + codDep + " não encontrado");
            }
            LOGGER.log(System.Logger.Level.INFO, "Imagem removida: {0}", codDep);
        }
    }

    public Optional<byte[]> getImagemPerfil(String codDep) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_GET_IMAGEM)) {

            stmt.setString(1, codDep);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.ofNullable(rs.getBytes("imagem_perfil")) : Optional.empty();
            }
        }
    }

    // ===== STATUS =====

    public void desativar(String codDep) throws SQLException {
        String agora = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS).toString();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_DESATIVAR)) {

            stmt.setString(1, agora);
            stmt.setString(2, codDep);

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Funcionário com código " + codDep + " não encontrado");
            }
            LOGGER.log(System.Logger.Level.INFO, "Funcionário desativado: {0}", codDep);
        }
    }

    public void ativar(String codDep) throws SQLException {
        String agora = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS).toString();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_ATIVAR)) {

            stmt.setString(1, agora);
            stmt.setString(2, codDep);

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Funcionário com código " + codDep + " não encontrado");
            }
            LOGGER.log(System.Logger.Level.INFO, "Funcionário ativado: {0}", codDep);
        }
    }

    // ===== VERIFICAÇÕES =====

    public boolean existsByCodDep(String codDep) throws SQLException {
        String sql = "SELECT COUNT(*) FROM %s WHERE cod_dep = ? AND deleted = 0".formatted(TABLE);
        return count(sql, codDep) > 0;
    }

    public boolean existsByEmail(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM %s WHERE email = ? AND deleted = 0".formatted(TABLE);
        return count(sql, email) > 0;
    }

    public int countAll() throws SQLException {
        return count("SELECT COUNT(*) FROM " + TABLE + " WHERE deleted = 0");
    }

    public int countAtivos() throws SQLException {
        return count("SELECT COUNT(*) FROM " + TABLE + " WHERE ativo = 1 AND deleted = 0");
    }

    // ===== AUXILIARES =====

    private int count(String sql, Object... params) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private List<Funcionarios> executeQuery(String sql) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            return mapResultSetList(rs);
        }
    }

    private List<Funcionarios> mapResultSetList(ResultSet rs) throws SQLException {
        List<Funcionarios> list = new ArrayList<>();
        while (rs.next()) {
            Funcionarios f = mapResultSet(rs);
            if (f != null) list.add(f);
        }
        return List.copyOf(list);
    }

    private Funcionarios mapResultSet(ResultSet rs) throws SQLException {
        try {
            return new Funcionarios(
                    rs.getString("id"),
                    rs.getString("cod_dep"),
                    rs.getString("cod_dep_anterior"),
                    rs.getString("nome"),
                    rs.getString("funcao"),
                    rs.getString("departamento"),
                    rs.getString("local_trabalho"),
                    rs.getString("email"),
                    rs.getString("telefone"),
                    rs.getBytes("imagem_perfil"),
                    rs.getString("tipo_imagem"),
                    rs.getInt("tamanho_imagem"),
                    rs.getBoolean("ativo"),
                    getLocalDateTime(rs, "created_at"),
                    getLocalDateTime(rs, "updated_at"),
                    rs.getString("device_id"),
                    rs.getInt("deleted") != 0
            );
        } catch (IllegalArgumentException e) {
            String id = rs.getString("id");
            String cod = rs.getString("cod_dep");
            LOGGER.log(System.Logger.Level.WARNING,
                    "⚠️ Funcionário ignorado — dados inválidos: {0} | id=''{1}'' cod_dep=''{2}''",
                    e.getMessage(), id, cod);
            return null;
        }
    }

    private LocalDateTime getLocalDateTime(ResultSet rs, String column) throws SQLException {
        String raw = rs.getString(column);
        if (raw == null || raw.isBlank()) return null;
        try {
            if (raw.indexOf('T') >= 0) {
                return LocalDateTime.parse(raw);
            }
            return LocalDateTime.parse(raw, SQLITE_TIMESTAMP);
        } catch (DateTimeParseException e) {
            LOGGER.log(System.Logger.Level.WARNING,
                    "Timestamp inválido em ''{0}'': ''{1}'' — ignorado.", column, raw);
            return null;
        }
    }
}