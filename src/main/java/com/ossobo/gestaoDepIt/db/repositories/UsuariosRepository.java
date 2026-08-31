package com.ossobo.gestaoDepIt.db.repositories;

import com.ossobo.gestaoDepIt.db.config.DatabaseConnection;
import com.ossobo.gestaoDepIt.db.models.Usuario;


import com.ossobo.winterfx.anotations.Inject;
import com.ossobo.winterfx.anotations.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * UsuariosRepository - Acesso a dados com WinterFX
 * v2.0 - Migrado para Java 17+ com Records e Text Blocks
 *
 * Responsabilidades:
 * - CRUD para Usuario
 * - Autenticação e gestão de sessão
 * - Filtros por nível, status, atividade
 * - Estatísticas
 */
@Repository
public class UsuariosRepository {

    private static final Logger logger = LoggerFactory.getLogger(UsuariosRepository.class);

    @Inject
    private DatabaseConnection dbConnection;

    private static final String TABLE = "usuarios";

    // ===== SQL COM TEXT BLOCKS =====

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
            WHERE sessao_atual = ? AND expiracao_sessao > NOW()
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
            WHERE sessao_atual IS NOT NULL AND expiracao_sessao > NOW()
            ORDER BY nome
            """.formatted(TABLE);

    private static final String SQL_FIND_COM_SESSAO_EXPIRADA = """
            SELECT * FROM %s
            WHERE sessao_atual IS NOT NULL AND expiracao_sessao <= NOW()
            ORDER BY nome
            """.formatted(TABLE);

    private static final String SQL_FIND_RECENTES_LOGIN = """
            SELECT * FROM %s
            WHERE ultimo_login >= DATE_SUB(NOW(), INTERVAL ? DAY)
            ORDER BY ultimo_login DESC
            """.formatted(TABLE);

    private static final String SQL_FIND_INATIVOS = """
            SELECT * FROM %s
            WHERE (ultimo_login IS NULL OR ultimo_login < DATE_SUB(NOW(), INTERVAL ? DAY))
            AND ativo = 1
            ORDER BY ultimo_login
            """.formatted(TABLE);

    private static final String SQL_FIND_SESSOES_PRESTES_EXPIRAR = """
            SELECT * FROM %s
            WHERE sessao_atual IS NOT NULL
            AND expiracao_sessao BETWEEN NOW() AND DATE_ADD(NOW(), INTERVAL ? MINUTE)
            ORDER BY expiracao_sessao
            """.formatted(TABLE);

    private static final String SQL_INSERT = """
            INSERT INTO %s (
                funcionario_id, nome, email, senha_hash,
                nivel_acesso, ativo
            ) VALUES (?, ?, ?, ?, ?, ?)
            """.formatted(TABLE);

    private static final String SQL_UPDATE = """
            UPDATE %s
            SET funcionario_id = ?, nome = ?, email = ?, senha_hash = ?,
                nivel_acesso = ?, ativo = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """.formatted(TABLE);

    private static final String SQL_UPDATE_SENHA = """
            UPDATE %s
            SET senha_hash = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """.formatted(TABLE);

    private static final String SQL_UPDATE_LOGIN = """
            UPDATE %s
            SET ultimo_login = NOW(), ip_ultimo_login = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """.formatted(TABLE);

    private static final String SQL_UPDATE_SESSAO = """
            UPDATE %s
            SET sessao_atual = ?, expiracao_sessao = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """.formatted(TABLE);

    private static final String SQL_INVALIDAR_SESSAO = """
            UPDATE %s
            SET sessao_atual = NULL, expiracao_sessao = NULL, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """.formatted(TABLE);

    private static final String SQL_INVALIDAR_TODAS_SESSOES = """
            UPDATE %s
            SET sessao_atual = NULL, expiracao_sessao = NULL, updated_at = CURRENT_TIMESTAMP
            """.formatted(TABLE);

    private static final String SQL_INVALIDAR_SESSOES_EXPIRADAS = """
            UPDATE %s
            SET sessao_atual = NULL, expiracao_sessao = NULL
            WHERE expiracao_sessao <= NOW()
            """.formatted(TABLE);

    private static final String SQL_DESATIVAR = """
            UPDATE %s
            SET ativo = 0, sessao_atual = NULL, expiracao_sessao = NULL,
                updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """.formatted(TABLE);

    private static final String SQL_ATIVAR = """
            UPDATE %s
            SET ativo = 1, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """.formatted(TABLE);

    private static final String SQL_UPDATE_NIVEL = """
            UPDATE %s
            SET nivel_acesso = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """.formatted(TABLE);

    private static final String SQL_DELETE = """
            DELETE FROM %s
            WHERE id = ?
            """.formatted(TABLE);

    // ===== CRUD =====

    public List<Usuario> findAll() throws SQLException {
        return executeQuery(SQL_FIND_ALL);
    }

    public List<Usuario> findAll(int limit, int offset) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_ALL_PAGINATED)) {

            stmt.setInt(1, limit);
            stmt.setInt(2, offset);
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    public List<Usuario> findAllAtivos(int limit, int offset) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_ALL_ATIVOS_PAGINATED)) {

            stmt.setInt(1, limit);
            stmt.setInt(2, offset);
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    public Optional<Usuario> findById(Long id) throws SQLException {
        if (id == null || id <= 0) {
            return Optional.empty();
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_ID)) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapResultSet(rs)) : Optional.empty();
            }
        }
    }

    public Optional<Usuario> findByEmail(String email) throws SQLException {
        if (!(email instanceof String e) || e.isBlank()) {
            return Optional.empty();
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_EMAIL)) {

            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapResultSet(rs)) : Optional.empty();
            }
        }
    }

    public Optional<Usuario> findByFuncionarioId(String funcionarioId) throws SQLException {
        if (!(funcionarioId instanceof String f) || f.isBlank()) {
            return Optional.empty();
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_FUNCIONARIO)) {

            stmt.setString(1, funcionarioId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapResultSet(rs)) : Optional.empty();
            }
        }
    }

    public Optional<Usuario> findBySessao(String token) throws SQLException {
        if (!(token instanceof String t) || t.isBlank()) {
            return Optional.empty();
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_SESSAO)) {

            stmt.setString(1, token);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapResultSet(rs)) : Optional.empty();
            }
        }
    }

    public List<Usuario> findByNivelAcesso(String nivel) throws SQLException {
        if (!(nivel instanceof String n) || n.isBlank()) {
            return List.of();
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_NIVEL)) {

            stmt.setString(1, nivel);
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    public List<Usuario> findByStatus(boolean ativo) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_STATUS)) {

            stmt.setBoolean(1, ativo);
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    public List<Usuario> findByNomeContaining(String nome) throws SQLException {
        if (!(nome instanceof String n) || n.isBlank()) {
            return List.of();
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_NOME_LIKE)) {

            stmt.setString(1, "%" + nome + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    public List<Usuario> findByEmailContaining(String email) throws SQLException {
        if (!(email instanceof String e) || e.isBlank()) {
            return List.of();
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_EMAIL_LIKE)) {

            stmt.setString(1, "%" + email + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    public List<Usuario> findComSessaoAtiva() throws SQLException {
        return executeQuery(SQL_FIND_COM_SESSAO_ATIVA);
    }

    public List<Usuario> findComSessaoExpirada() throws SQLException {
        return executeQuery(SQL_FIND_COM_SESSAO_EXPIRADA);
    }

    public List<Usuario> findRecentesLogin(int dias) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_RECENTES_LOGIN)) {

            stmt.setInt(1, dias);
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    public List<Usuario> findInativos(int dias) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_INATIVOS)) {

            stmt.setInt(1, dias);
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    public List<Usuario> findSessoesPrestesExpirar(int minutos) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_SESSOES_PRESTES_EXPIRAR)) {

            stmt.setInt(1, minutos);
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    public Long insert(Usuario usuario) throws SQLException {
        if (!(usuario instanceof Usuario u)) {
            throw new IllegalArgumentException("Usuário inválido");
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_INSERT,
                     PreparedStatement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, u.funcionarioId());
            stmt.setString(2, u.nome());
            stmt.setString(3, u.email());
            stmt.setString(4, u.senhaHash());
            stmt.setString(5, u.nivelAcesso());
            stmt.setBoolean(6, u.isAtivo());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
                throw new SQLException("Falha ao obter ID gerado");
            }
        }
    }

    public void update(Usuario usuario) throws SQLException {
        if (!(usuario instanceof Usuario u)) {
            throw new IllegalArgumentException("Usuário inválido");
        }
        if (u.id() == null) {
            throw new SQLException("ID não pode ser nulo para atualização");
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE)) {

            stmt.setString(1, u.funcionarioId());
            stmt.setString(2, u.nome());
            stmt.setString(3, u.email());
            stmt.setString(4, u.senhaHash());
            stmt.setString(5, u.nivelAcesso());
            stmt.setBoolean(6, u.isAtivo());
            stmt.setLong(7, u.id());

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Usuário com ID " + u.id() + " não encontrado");
            }
        }
    }

    public void updateSenha(Long id, String novaSenhaHash) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE_SENHA)) {

            stmt.setString(1, novaSenhaHash);
            stmt.setLong(2, id);
            int affected = stmt.executeUpdate();

            if (affected == 0) {
                throw new SQLException("Usuário com ID " + id + " não encontrado");
            }
        }
    }

    public void updateLogin(Long id, String ip) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE_LOGIN)) {

            stmt.setString(1, ip);
            stmt.setLong(2, id);
            int affected = stmt.executeUpdate();

            if (affected == 0) {
                throw new SQLException("Usuário com ID " + id + " não encontrado");
            }
        }
    }

    public void updateSessao(Long id, String token, LocalDateTime expiracao) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE_SESSAO)) {

            stmt.setString(1, token);
            stmt.setTimestamp(2, Timestamp.valueOf(expiracao));
            stmt.setLong(3, id);
            int affected = stmt.executeUpdate();

            if (affected == 0) {
                throw new SQLException("Usuário com ID " + id + " não encontrado");
            }
        }
    }

    public void invalidarSessao(Long id) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_INVALIDAR_SESSAO)) {

            stmt.setLong(1, id);
            int affected = stmt.executeUpdate();

            if (affected == 0) {
                throw new SQLException("Usuário com ID " + id + " não encontrado");
            }
        }
    }

    public void invalidarTodasSessoes() throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_INVALIDAR_TODAS_SESSOES)) {

            stmt.executeUpdate();
        }
    }

    public void invalidarSessoesExpiradas() throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_INVALIDAR_SESSOES_EXPIRADAS)) {

            stmt.executeUpdate();
        }
    }

    public void desativar(Long id) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_DESATIVAR)) {

            stmt.setLong(1, id);
            int affected = stmt.executeUpdate();

            if (affected == 0) {
                throw new SQLException("Usuário com ID " + id + " não encontrado");
            }
        }
    }

    public void ativar(Long id) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_ATIVAR)) {

            stmt.setLong(1, id);
            int affected = stmt.executeUpdate();

            if (affected == 0) {
                throw new SQLException("Usuário com ID " + id + " não encontrado");
            }
        }
    }

    public void updateNivelAcesso(Long id, String nivel) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE_NIVEL)) {

            stmt.setString(1, nivel);
            stmt.setLong(2, id);
            int affected = stmt.executeUpdate();

            if (affected == 0) {
                throw new SQLException("Usuário com ID " + id + " não encontrado");
            }
        }
    }

    public void delete(Long id) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_DELETE)) {

            stmt.setLong(1, id);
            int affected = stmt.executeUpdate();

            if (affected == 0) {
                throw new SQLException("Usuário com ID " + id + " não encontrado");
            }
        }
    }

    // ===== FILTROS COMBINADOS =====

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
                sql.append(" AND sessao_atual IS NOT NULL AND expiracao_sessao > NOW()");
            } else {
                sql.append(" AND (sessao_atual IS NULL OR expiracao_sessao <= NOW())");
            }
        }
        if (dataLoginInicio != null && dataLoginFim != null) {
            sql.append(" AND ultimo_login BETWEEN ? AND ?");
            params.add(dataLoginInicio);
            params.add(dataLoginFim);
        }

        sql.append(" ORDER BY nome, email");

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    // ===== VERIFICAÇÕES =====

    public boolean existsById(Long id) throws SQLException {
        if (id == null) return false;
        String sql = "SELECT COUNT(*) FROM %s WHERE id = ?".formatted(TABLE);

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public boolean existsByEmail(String email) throws SQLException {
        if (email == null || email.isBlank()) return false;
        String sql = "SELECT COUNT(*) FROM %s WHERE email = ?".formatted(TABLE);

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public boolean existsByFuncionarioId(String funcionarioId) throws SQLException {
        if (funcionarioId == null || funcionarioId.isBlank()) return false;
        String sql = "SELECT COUNT(*) FROM %s WHERE funcionario_id = ?".formatted(TABLE);

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, funcionarioId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    // ===== ESTATÍSTICAS =====

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
        Map<String, Integer> stats = new LinkedHashMap<>();

        stats.put("total", countAll());
        stats.put("ativos", countAtivos());
        stats.put("com_sessao", count("SELECT COUNT(*) FROM " + TABLE + " WHERE sessao_atual IS NOT NULL AND expiracao_sessao > NOW()"));
        stats.put("login_hoje", count("SELECT COUNT(*) FROM " + TABLE + " WHERE DATE(ultimo_login) = CURDATE()"));

        return Map.copyOf(stats);
    }

    public List<Object[]> getLoginsPorPeriodo(LocalDateTime inicio, LocalDateTime fim) throws SQLException {
        if (inicio == null || fim == null) {
            return List.of();
        }

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

            List<Object[]> resultados = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultados.add(new Object[]{
                            rs.getDate("data").toLocalDate(),
                            rs.getInt("total")
                    });
                }
            }
            return List.copyOf(resultados);
        }
    }

    // ===== MÉTODOS PRIVADOS =====

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

    private List<Usuario> executeQuery(String sql) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            return mapResultSetList(rs);
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
                rs.getLong("id"),
                rs.getString("funcionario_id"),
                rs.getString("nome"),
                rs.getString("email"),
                rs.getString("senha_hash"),
                rs.getString("nivel_acesso"),
                rs.getBoolean("ativo"),
                getLocalDateTime(rs, "ultimo_login"),
                rs.getString("ip_ultimo_login"),
                rs.getString("sessao_atual"),
                getLocalDateTime(rs, "expiracao_sessao"),
                getLocalDateTime(rs, "created_at"),
                getLocalDateTime(rs, "updated_at")
        );
    }

    private LocalDateTime getLocalDateTime(ResultSet rs, String column) throws SQLException {
        Timestamp ts = rs.getTimestamp(column);
        return ts != null ? ts.toLocalDateTime() : null;
    }
}