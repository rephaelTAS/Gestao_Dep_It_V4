package com.ossobo.gestaoDepIt.db.repositories;

import com.ossobo.gestaoDepIt.db.config.DatabaseConnection;
import com.ossobo.gestaoDepIt.db.enums.Hierarquia;
import com.ossobo.gestaoDepIt.db.models.Usuario;
import com.ossobo.gestaoDepIt.utils.DateUtils;

import com.ossobo.winterfx.anotations.Inject;
import com.ossobo.winterfx.anotations.Repository;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * UsuariosRepository - Acesso a dados com WinterFX
 * v2.2 - Alinhado ao Usuario v3.3
 *
 * Mudanças v2.1 → v2.2:
 * - getLocalDateTime usa DateUtils.parseDateTimeTolerante (leitura via
 *   rs.getString) — o driver xerial não parseia timestamp no formato
 *   SQLite "yyyy-MM-dd HH:mm:ss", causando "Error parsing time stamp".
 *
 * v2.1 - Alinhado ao Usuario v3.1 (PK String/UUID + colunas de sync)
 *   - PK Long → String (UUID v4)
 *   - INSERT inclui id, device_id, deleted e timestamps
 *   - UPDATE grava device_id
 *   - Funções MySQL → timestamps Java (SQL portável)
 *   - getBooleanNullable: NULL vira null
 *   - getLoginsPorPeriodo retorna record LoginPorDia
 */
@Repository
public class UsuariosRepository {

    private static final System.Logger logger = System.getLogger(UsuariosRepository.class.getName());

    @Inject
    private DatabaseConnection dbConnection;

    private static final String TABLE = "usuarios";

    // ============================================================
    // TIPOS AUXILIARES
    // ============================================================

    public record LoginPorDia(LocalDate data, int total) {}

    // ============================================================
    // SQL COM TEXT BLOCKS
    // ============================================================

    private static final String SQL_FIND_ALL = """
            SELECT * FROM %s
            ORDER BY nome, email
            """.formatted(TABLE);

    private static final String SQL_FIND_ALL_PAGINATED = """
            SELECT * FROM %s
            ORDER BY nome, email
            LIMIT ? OFFSET ?
            """.formatted(TABLE);

    private static final String SQL_FIND_ALL_ATIVOS = """
            SELECT * FROM %s
            WHERE ativo = 1
            ORDER BY nome, email
            """.formatted(TABLE);

    private static final String SQL_FIND_ALL_ATIVOS_PAGINATED = """
            SELECT * FROM %s
            WHERE ativo = 1
            ORDER BY nome, email
            LIMIT ? OFFSET ?
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_ID = """
            SELECT * FROM %s
            WHERE id = ?
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_EMAIL = """
            SELECT * FROM %s
            WHERE email = ?
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_FUNCIONARIO = """
            SELECT * FROM %s
            WHERE funcionario_id = ?
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_SESSAO = """
            SELECT * FROM %s
            WHERE sessao_atual = ? AND expiracao_sessao > ?
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_NIVEL = """
            SELECT * FROM %s
            WHERE nivel_acesso = ?
            ORDER BY nome
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_STATUS = """
            SELECT * FROM %s
            WHERE ativo = ?
            ORDER BY nome
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_NOME_LIKE = """
            SELECT * FROM %s
            WHERE nome LIKE ?
            ORDER BY nome
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_EMAIL_LIKE = """
            SELECT * FROM %s
            WHERE email LIKE ?
            ORDER BY email
            """.formatted(TABLE);

    private static final String SQL_FIND_COM_SESSAO_ATIVA = """
            SELECT * FROM %s
            WHERE sessao_atual IS NOT NULL AND expiracao_sessao > ?
            ORDER BY nome
            """.formatted(TABLE);

    private static final String SQL_FIND_COM_SESSAO_EXPIRADA = """
            SELECT * FROM %s
            WHERE sessao_atual IS NOT NULL AND expiracao_sessao <= ?
            ORDER BY nome
            """.formatted(TABLE);

    private static final String SQL_FIND_RECENTES_LOGIN = """
            SELECT * FROM %s
            WHERE ultimo_login >= ?
            ORDER BY ultimo_login DESC
            """.formatted(TABLE);

    private static final String SQL_FIND_INATIVOS = """
            SELECT * FROM %s
            WHERE (ultimo_login IS NULL OR ultimo_login < ?)
            AND ativo = 1
            ORDER BY ultimo_login
            """.formatted(TABLE);

    private static final String SQL_FIND_SESSOES_PRESTES_EXPIRAR = """
            SELECT * FROM %s
            WHERE sessao_atual IS NOT NULL
            AND expiracao_sessao BETWEEN ? AND ?
            ORDER BY expiracao_sessao
            """.formatted(TABLE);

    private static final String SQL_INSERT = """
            INSERT INTO %s (
                id, funcionario_id, nome, email, senha_hash,
                nivel_acesso, ativo, device_id, deleted,
                created_at, updated_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.formatted(TABLE);

    private static final String SQL_UPDATE = """
            UPDATE %s
            SET funcionario_id = ?, nome = ?, email = ?, senha_hash = ?,
                nivel_acesso = ?, ativo = ?, device_id = ?, updated_at = ?
            WHERE id = ?
            """.formatted(TABLE);

    private static final String SQL_UPDATE_SENHA = """
            UPDATE %s
            SET senha_hash = ?, device_id = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """.formatted(TABLE);

    private static final String SQL_UPDATE_LOGIN = """
            UPDATE %s
            SET ultimo_login = ?, ip_ultimo_login = ?, device_id = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """.formatted(TABLE);

    private static final String SQL_UPDATE_SESSAO = """
            UPDATE %s
            SET sessao_atual = ?, expiracao_sessao = ?, device_id = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """.formatted(TABLE);

    private static final String SQL_INVALIDAR_SESSAO = """
            UPDATE %s
            SET sessao_atual = NULL, expiracao_sessao = NULL, device_id = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """.formatted(TABLE);

    private static final String SQL_INVALIDAR_TODAS_SESSOES = """
            UPDATE %s
            SET sessao_atual = NULL, expiracao_sessao = NULL, device_id = ?, updated_at = CURRENT_TIMESTAMP
            """.formatted(TABLE);

    private static final String SQL_INVALIDAR_SESSOES_EXPIRADAS = """
            UPDATE %s
            SET sessao_atual = NULL, expiracao_sessao = NULL, device_id = ?, updated_at = CURRENT_TIMESTAMP
            WHERE expiracao_sessao <= ?
            """.formatted(TABLE);

    private static final String SQL_DESATIVAR = """
            UPDATE %s
            SET ativo = 0, sessao_atual = NULL, expiracao_sessao = NULL,
                device_id = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """.formatted(TABLE);

    private static final String SQL_ATIVAR = """
            UPDATE %s
            SET ativo = 1, device_id = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """.formatted(TABLE);

    private static final String SQL_UPDATE_NIVEL = """
            UPDATE %s
            SET nivel_acesso = ?, device_id = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """.formatted(TABLE);

    private static final String SQL_DELETE = """
            DELETE FROM %s
            WHERE id = ?
            """.formatted(TABLE);

    // ============================================================
    // CRUD — LEITURAS
    // ============================================================

    public List<Usuario> findAll() throws SQLException {
        return executeQuery(SQL_FIND_ALL);
    }

    public List<Usuario> findAll(int limit, int offset) throws SQLException {
        return executeQuery(SQL_FIND_ALL_PAGINATED, limit, offset);
    }

    public List<Usuario> findAllAtivos() throws SQLException {
        return executeQuery(SQL_FIND_ALL_ATIVOS);
    }

    public List<Usuario> findAllAtivos(int limit, int offset) throws SQLException {
        return executeQuery(SQL_FIND_ALL_ATIVOS_PAGINATED, limit, offset);
    }

    public Optional<Usuario> findById(String id) throws SQLException {
        if (id == null || id.isBlank()) return Optional.empty();
        List<Usuario> resultado = executeQuery(SQL_FIND_BY_ID, id);
        return resultado.isEmpty() ? Optional.empty() : Optional.of(resultado.get(0));
    }

    public Optional<Usuario> findByEmail(String email) throws SQLException {
        if (email == null || email.isBlank()) return Optional.empty();
        List<Usuario> resultado = executeQuery(SQL_FIND_BY_EMAIL, email);
        return resultado.isEmpty() ? Optional.empty() : Optional.of(resultado.get(0));
    }

    public Optional<Usuario> findByFuncionarioId(String funcionarioId) throws SQLException {
        if (funcionarioId == null || funcionarioId.isBlank()) return Optional.empty();
        List<Usuario> resultado = executeQuery(SQL_FIND_BY_FUNCIONARIO, funcionarioId);
        return resultado.isEmpty() ? Optional.empty() : Optional.of(resultado.get(0));
    }

    public Optional<Usuario> findBySessao(String token) throws SQLException {
        if (token == null || token.isBlank()) return Optional.empty();
        List<Usuario> resultado = executeQuery(SQL_FIND_BY_SESSAO,
                token, Timestamp.valueOf(LocalDateTime.now()));
        return resultado.isEmpty() ? Optional.empty() : Optional.of(resultado.get(0));
    }

    public List<Usuario> findByNivelAcesso(String nivel) throws SQLException {
        if (nivel == null || nivel.isBlank()) return List.of();
        return executeQuery(SQL_FIND_BY_NIVEL, nivel);
    }

    public List<Usuario> findByStatus(boolean ativo) throws SQLException {
        return executeQuery(SQL_FIND_BY_STATUS, ativo);
    }

    public List<Usuario> findByNomeContaining(String nome) throws SQLException {
        if (nome == null || nome.isBlank()) return List.of();
        return executeQuery(SQL_FIND_BY_NOME_LIKE, "%" + nome + "%");
    }

    public List<Usuario> findByEmailContaining(String email) throws SQLException {
        if (email == null || email.isBlank()) return List.of();
        return executeQuery(SQL_FIND_BY_EMAIL_LIKE, "%" + email + "%");
    }

    public List<Usuario> findComSessaoAtiva() throws SQLException {
        return executeQuery(SQL_FIND_COM_SESSAO_ATIVA,
                Timestamp.valueOf(LocalDateTime.now()));
    }

    public List<Usuario> findComSessaoExpirada() throws SQLException {
        return executeQuery(SQL_FIND_COM_SESSAO_EXPIRADA,
                Timestamp.valueOf(LocalDateTime.now()));
    }

    public List<Usuario> findRecentesLogin(int dias) throws SQLException {
        return executeQuery(SQL_FIND_RECENTES_LOGIN,
                Timestamp.valueOf(LocalDateTime.now().minusDays(dias)));
    }

    public List<Usuario> findInativos(int dias) throws SQLException {
        return executeQuery(SQL_FIND_INATIVOS,
                Timestamp.valueOf(LocalDateTime.now().minusDays(dias)));
    }

    public List<Usuario> findSessoesPrestesExpirar(int minutos) throws SQLException {
        LocalDateTime agora = LocalDateTime.now();
        return executeQuery(SQL_FIND_SESSOES_PRESTES_EXPIRAR,
                Timestamp.valueOf(agora),
                Timestamp.valueOf(agora.plusMinutes(minutos)));
    }

    // ============================================================
    // CRUD — ESCRITAS
    // ============================================================

    public String insert(Usuario usuario) throws SQLException {
        if (usuario == null) {
            throw new IllegalArgumentException("Usuário inválido");
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_INSERT)) {

            LocalDateTime agora = LocalDateTime.now();
            stmt.setString(1, usuario.id());
            stmt.setString(2, usuario.funcionarioId());
            stmt.setString(3, usuario.nome());
            stmt.setString(4, usuario.email());
            stmt.setString(5, usuario.senhaHash());
            stmt.setString(6, usuario.nivelAcesso().name());
            stmt.setBoolean(7, usuario.isAtivo());
            stmt.setString(8, getDeviceId());
            stmt.setBoolean(9, usuario.isDeletado());
            stmt.setTimestamp(10, Timestamp.valueOf(
                    usuario.createdAt() != null ? usuario.createdAt() : agora));
            stmt.setTimestamp(11, Timestamp.valueOf(
                    usuario.updatedAt() != null ? usuario.updatedAt() : agora));

            stmt.executeUpdate();
            return usuario.id();
        }
    }

    public void update(Usuario usuario) throws SQLException {
        if (usuario == null) {
            throw new IllegalArgumentException("Usuário inválido");
        }
        if (usuario.id() == null || usuario.id().isBlank()) {
            throw new SQLException("ID não pode ser vazio para atualização");
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE)) {

            stmt.setString(1, usuario.funcionarioId());
            stmt.setString(2, usuario.nome());
            stmt.setString(3, usuario.email());
            stmt.setString(4, usuario.senhaHash());
            stmt.setString(5, usuario.nivelAcesso().name());
            stmt.setBoolean(6, usuario.isAtivo());
            stmt.setString(7, getDeviceId());
            stmt.setTimestamp(8, Timestamp.valueOf(
                    usuario.updatedAt() != null ? usuario.updatedAt() : LocalDateTime.now()));
            stmt.setString(9, usuario.id());

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Usuário com ID " + usuario.id() + " não encontrado");
            }
        }
    }

    public void updateSenha(String id, String novaSenhaHash) throws SQLException {
        int affected = executeUpdate(SQL_UPDATE_SENHA, novaSenhaHash, getDeviceId(), id);
        if (affected == 0) {
            throw new SQLException("Usuário com ID " + id + " não encontrado");
        }
    }

    public void updateLogin(String id, String ip) throws SQLException {
        int affected = executeUpdate(SQL_UPDATE_LOGIN,
                Timestamp.valueOf(LocalDateTime.now()), ip, getDeviceId(), id);
        if (affected == 0) {
            throw new SQLException("Usuário com ID " + id + " não encontrado");
        }
    }

    public void updateSessao(String id, String token, LocalDateTime expiracao) throws SQLException {
        int affected = executeUpdate(SQL_UPDATE_SESSAO,
                token, Timestamp.valueOf(expiracao), getDeviceId(), id);
        if (affected == 0) {
            throw new SQLException("Usuário com ID " + id + " não encontrado");
        }
    }

    public void invalidarSessao(String id) throws SQLException {
        int affected = executeUpdate(SQL_INVALIDAR_SESSAO, getDeviceId(), id);
        if (affected == 0) {
            throw new SQLException("Usuário com ID " + id + " não encontrado");
        }
    }

    public void invalidarTodasSessoes() throws SQLException {
        executeUpdate(SQL_INVALIDAR_TODAS_SESSOES, getDeviceId());
    }

    public void invalidarSessoesExpiradas() throws SQLException {
        executeUpdate(SQL_INVALIDAR_SESSOES_EXPIRADAS,
                getDeviceId(), Timestamp.valueOf(LocalDateTime.now()));
    }

    public void desativar(String id) throws SQLException {
        int affected = executeUpdate(SQL_DESATIVAR, getDeviceId(), id);
        if (affected == 0) {
            throw new SQLException("Usuário com ID " + id + " não encontrado");
        }
    }

    public void ativar(String id) throws SQLException {
        int affected = executeUpdate(SQL_ATIVAR, getDeviceId(), id);
        if (affected == 0) {
            throw new SQLException("Usuário com ID " + id + " não encontrado");
        }
    }

    public void updateNivelAcesso(String id, String nivel) throws SQLException {
        int affected = executeUpdate(SQL_UPDATE_NIVEL, nivel, getDeviceId(), id);
        if (affected == 0) {
            throw new SQLException("Usuário com ID " + id + " não encontrado");
        }
    }

    public void delete(String id) throws SQLException {
        int affected = executeUpdate(SQL_DELETE, id);
        if (affected == 0) {
            throw new SQLException("Usuário com ID " + id + " não encontrado");
        }
    }

    // ============================================================
    // FILTROS COMBINADOS
    // ============================================================

    public List<Usuario> findWithFilters(
            String nome,
            String email,
            String nivelAcesso,
            Boolean ativo,
            Boolean comSessaoAtiva,
            LocalDateTime dataLoginInicio,
            LocalDateTime dataLoginFim
    ) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM " + TABLE + " WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (nome != null && !nome.isBlank()) {
            sql.append(" AND nome LIKE ?");
            params.add("%" + nome + "%");
        }
        if (email != null && !email.isBlank()) {
            sql.append(" AND email LIKE ?");
            params.add("%" + email + "%");
        }
        if (nivelAcesso != null && !nivelAcesso.isBlank()) {
            sql.append(" AND nivel_acesso = ?");
            params.add(nivelAcesso);
        }
        if (ativo != null) {
            sql.append(" AND ativo = ?");
            params.add(ativo);
        }
        if (comSessaoAtiva != null) {
            if (comSessaoAtiva) {
                sql.append(" AND sessao_atual IS NOT NULL AND expiracao_sessao > ?");
                params.add(Timestamp.valueOf(LocalDateTime.now()));
            } else {
                sql.append(" AND (sessao_atual IS NULL OR expiracao_sessao <= ?)");
                params.add(Timestamp.valueOf(LocalDateTime.now()));
            }
        }
        if (dataLoginInicio != null && dataLoginFim != null) {
            sql.append(" AND ultimo_login BETWEEN ? AND ?");
            params.add(Timestamp.valueOf(dataLoginInicio));
            params.add(Timestamp.valueOf(dataLoginFim));
        }

        sql.append(" ORDER BY nome, email");

        return executeQuery(sql.toString(), params.toArray());
    }

    // ============================================================
    // VERIFICAÇÕES
    // ============================================================

    public boolean existsById(String id) throws SQLException {
        if (id == null || id.isBlank()) return false;
        return count("SELECT COUNT(*) FROM " + TABLE + " WHERE id = ?", id) > 0;
    }

    public boolean existsByEmail(String email) throws SQLException {
        if (email == null || email.isBlank()) return false;
        return count("SELECT COUNT(*) FROM " + TABLE + " WHERE email = ?", email) > 0;
    }

    public boolean existsByFuncionarioId(String funcionarioId) throws SQLException {
        if (funcionarioId == null || funcionarioId.isBlank()) return false;
        return count("SELECT COUNT(*) FROM " + TABLE + " WHERE funcionario_id = ?", funcionarioId) > 0;
    }

    // ============================================================
    // ESTATÍSTICAS
    // ============================================================

    public int countAll() throws SQLException {
        return count("SELECT COUNT(*) FROM " + TABLE);
    }

    public int countAtivos() throws SQLException {
        return count("SELECT COUNT(*) FROM " + TABLE + " WHERE ativo = 1");
    }

    public Map<String, Integer> countByNivelAcesso() throws SQLException {
        String sql = """
                SELECT nivel_acesso, COUNT(*) as total
                FROM %s
                WHERE ativo = 1
                GROUP BY nivel_acesso
                ORDER BY total DESC
                """.formatted(TABLE);
        return countGroupBy(sql);
    }

    public Map<String, Integer> getEstatisticasAtividade() throws SQLException {
        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime inicioDoDia = agora.toLocalDate().atStartOfDay();

        Map<String, Integer> stats = new LinkedHashMap<>();
        stats.put("total", countAll());
        stats.put("ativos", countAtivos());
        stats.put("com_sessao", count(
                "SELECT COUNT(*) FROM " + TABLE
                        + " WHERE sessao_atual IS NOT NULL AND expiracao_sessao > ?",
                Timestamp.valueOf(agora)));
        stats.put("login_hoje", count(
                "SELECT COUNT(*) FROM " + TABLE + " WHERE ultimo_login >= ?",
                Timestamp.valueOf(inicioDoDia)));

        return Map.copyOf(stats);
    }

    public List<LoginPorDia> getLoginsPorPeriodo(LocalDateTime inicio, LocalDateTime fim) throws SQLException {
        if (inicio == null || fim == null) return List.of();

        String sql = """
                SELECT DATE(ultimo_login) as data, COUNT(*) as total
                FROM %s
                WHERE ultimo_login BETWEEN ? AND ?
                GROUP BY DATE(ultimo_login)
                ORDER BY data
                """.formatted(TABLE);

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(inicio));
            stmt.setTimestamp(2, Timestamp.valueOf(fim));

            List<LoginPorDia> resultados = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultados.add(new LoginPorDia(
                            rs.getDate("data").toLocalDate(),
                            rs.getInt("total")));
                }
            }
            return List.copyOf(resultados);
        }
    }

    // ============================================================
    // MÉTODOS PRIVADOS (helpers)
    // ============================================================

    private List<Usuario> executeQuery(String sql, Object... params) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            bindParams(stmt, params);

            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    private int executeUpdate(String sql, Object... params) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            bindParams(stmt, params);
            return stmt.executeUpdate();
        }
    }

    private void bindParams(PreparedStatement stmt, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            stmt.setObject(i + 1, params[i]);
        }
    }

    private int count(String sql, Object... params) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            bindParams(stmt, params);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private Map<String, Integer> countGroupBy(String sql) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            Map<String, Integer> resultado = new LinkedHashMap<>();
            while (rs.next()) {
                resultado.put(rs.getString(1), rs.getInt(2));
            }
            return Map.copyOf(resultado);
        }
    }

    private List<Usuario> mapResultSetList(ResultSet rs) throws SQLException {
        List<Usuario> list = new ArrayList<>();
        while (rs.next()) {
            list.add(mapResultSet(rs));
        }
        return List.copyOf(list);
    }

    private Usuario mapResultSet(ResultSet rs) throws SQLException {
        return new Usuario(
                rs.getString("id"),
                rs.getString("funcionario_id"),
                rs.getString("nome"),
                rs.getString("email"),
                rs.getString("senha_hash"),
                Hierarquia.de(rs.getString("nivel_acesso")),
                getBooleanNullable(rs, "ativo"),
                getLocalDateTime(rs, "ultimo_login"),
                rs.getString("ip_ultimo_login"),
                rs.getString("sessao_atual"),
                getLocalDateTime(rs, "expiracao_sessao"),
                getLocalDateTime(rs, "created_at"),
                getLocalDateTime(rs, "updated_at"),
                rs.getString("device_id"),
                getBooleanNullable(rs, "deleted")
        );
    }

    /** NULL no banco → null no Java (rs.getBoolean devolveria false!). */
    private Boolean getBooleanNullable(ResultSet rs, String column) throws SQLException {
        int valor = rs.getInt(column);
        return rs.wasNull() ? null : valor != 0;
    }

    /**
     * Leitura tolerante de timestamp: aceita ISO com T, formato SQLite
     * "yyyy-MM-dd HH:mm:ss" e ms Unix legado. Substitui rs.getTimestamp —
     * o driver xerial não parseia o formato default do SQLite e estoura
     * "Error parsing time stamp".
     */
    private LocalDateTime getLocalDateTime(ResultSet rs, String column) throws SQLException {
        String raw = rs.getString(column);
        return DateUtils.parseDateTimeTolerante(raw);
    }

    private String getDeviceId() {
        return "UNKNOWN-DEVICE";
    }
}