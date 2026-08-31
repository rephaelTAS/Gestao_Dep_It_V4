package com.ossobo.gestaoDepIt.db.repositories;

import com.ossobo.gestaoDepIt.db.config.DatabaseConnection;
import com.ossobo.gestaoDepIt.db.models.Funcionarios;


import com.ossobo.winterfx.anotations.Inject;
import com.ossobo.winterfx.anotations.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * FuncionariosRepository - Acesso a dados com WinterFX
 * v2.0 - Migrado para Java 17+ com Records e Text Blocks
 *
 * Responsabilidades:
 * - CRUD para Funcionarios
 * - Gestão de imagem de perfil
 * - Filtros por departamento, função, status
 * - Estatísticas
 */
@Repository
public class FuncionariosRepository {

    private static final Logger logger = LoggerFactory.getLogger(FuncionariosRepository.class);

    @Inject
    private DatabaseConnection dbConnection;

    private static final String TABLE = "funcionarios";

    // ===== SQL COM TEXT BLOCKS =====

    private static final String SQL_FIND_ALL = """
            SELECT * FROM %s
            ORDER BY nome, departamento
            """.formatted(TABLE);

    private static final String SQL_FIND_ALL_ATIVOS = """
            SELECT * FROM %s
            WHERE ativo = 1
            ORDER BY nome, departamento
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_COD = """
            SELECT * FROM %s
            WHERE cod_dep = ?
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_EMAIL = """
            SELECT * FROM %s
            WHERE email = ?
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_DEPARTAMENTO = """
            SELECT * FROM %s
            WHERE departamento = ?
            ORDER BY nome
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_FUNCAO = """
            SELECT * FROM %s
            WHERE funcao = ?
            ORDER BY nome, departamento
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_LOCAL = """
            SELECT * FROM %s
            WHERE local_trabalho = ?
            ORDER BY nome, departamento
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_NOME = """
            SELECT * FROM %s
            WHERE nome LIKE ?
            ORDER BY nome, departamento
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_STATUS = """
            SELECT * FROM %s
            WHERE ativo = ?
            ORDER BY nome, departamento
            """.formatted(TABLE);

    private static final String SQL_FIND_COM_IMAGEM = """
            SELECT * FROM %s
            WHERE imagem_perfil IS NOT NULL AND ativo = 1
            ORDER BY nome
            """.formatted(TABLE);

    private static final String SQL_FIND_SEM_IMAGEM = """
            SELECT * FROM %s
            WHERE imagem_perfil IS NULL AND ativo = 1
            ORDER BY nome
            """.formatted(TABLE);

    private static final String SQL_INSERT = """
            INSERT INTO %s (
                cod_dep, nome, funcao, departamento, local_trabalho,
                email, telefone, imagem_perfil, tipo_imagem, tamanho_imagem, ativo
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.formatted(TABLE);

    private static final String SQL_UPDATE = """
            UPDATE %s
            SET nome = ?, funcao = ?, departamento = ?, local_trabalho = ?,
                email = ?, telefone = ?, ativo = ?, updated_at = CURRENT_TIMESTAMP
            WHERE cod_dep = ?
            """.formatted(TABLE);

    private static final String SQL_UPDATE_COMPLETO = """
            UPDATE %s
            SET nome = ?, funcao = ?, departamento = ?, local_trabalho = ?,
                email = ?, telefone = ?, ativo = ?, imagem_perfil = ?,
                tipo_imagem = ?, tamanho_imagem = ?, updated_at = CURRENT_TIMESTAMP
            WHERE cod_dep = ?
            """.formatted(TABLE);

    private static final String SQL_UPDATE_IMAGEM = """
            UPDATE %s
            SET imagem_perfil = ?, tipo_imagem = ?, tamanho_imagem = ?,
                updated_at = CURRENT_TIMESTAMP
            WHERE cod_dep = ?
            """.formatted(TABLE);

    private static final String SQL_REMOVER_IMAGEM = """
            UPDATE %s
            SET imagem_perfil = NULL, tipo_imagem = NULL, tamanho_imagem = NULL,
                updated_at = CURRENT_TIMESTAMP
            WHERE cod_dep = ?
            """.formatted(TABLE);

    private static final String SQL_GET_IMAGEM = """
            SELECT imagem_perfil FROM %s
            WHERE cod_dep = ?
            """.formatted(TABLE);

    private static final String SQL_DELETE = """
            DELETE FROM %s
            WHERE cod_dep = ?
            """.formatted(TABLE);

    private static final String SQL_DESATIVAR = """
            UPDATE %s
            SET ativo = 0, updated_at = CURRENT_TIMESTAMP
            WHERE cod_dep = ?
            """.formatted(TABLE);

    private static final String SQL_ATIVAR = """
            UPDATE %s
            SET ativo = 1, updated_at = CURRENT_TIMESTAMP
            WHERE cod_dep = ?
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

    public Optional<Funcionarios> findByCodDep(String codDep) throws SQLException {
        if (!(codDep instanceof String c) || c.isBlank()) {
            return Optional.empty();
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_COD)) {

            stmt.setString(1, codDep);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapResultSet(rs)) : Optional.empty();
            }
        }
    }

    public Optional<Funcionarios> findByEmail(String email) throws SQLException {
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

    public List<Funcionarios> findByDepartamento(String departamento) throws SQLException {
        if (!(departamento instanceof String d) || d.isBlank()) {
            return List.of();
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_DEPARTAMENTO)) {

            stmt.setString(1, departamento);
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    public List<Funcionarios> findByFuncao(String funcao) throws SQLException {
        if (!(funcao instanceof String f) || f.isBlank()) {
            return List.of();
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_FUNCAO)) {

            stmt.setString(1, funcao);
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    public List<Funcionarios> findByLocalTrabalho(String local) throws SQLException {
        if (!(local instanceof String l) || l.isBlank()) {
            return List.of();
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_LOCAL)) {

            stmt.setString(1, local);
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    public List<Funcionarios> findByNomeContaining(String nome) throws SQLException {
        if (!(nome instanceof String n) || n.isBlank()) {
            return List.of();
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_NOME)) {

            stmt.setString(1, "%" + nome + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    public List<Funcionarios> findByStatus(boolean ativo) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_STATUS)) {

            stmt.setBoolean(1, ativo);
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    public List<Funcionarios> findComImagemPerfil() throws SQLException {
        return executeQuery(SQL_FIND_COM_IMAGEM);
    }

    public List<Funcionarios> findSemImagemPerfil() throws SQLException {
        return executeQuery(SQL_FIND_SEM_IMAGEM);
    }

    public String insert(Funcionarios func) throws SQLException {
        if (!(func instanceof Funcionarios f)) {
            throw new IllegalArgumentException("Funcionário inválido");
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_INSERT)) {

            setInsertParameters(stmt, f);
            stmt.executeUpdate();
            return f.codDep();
        }
    }

    public void update(Funcionarios func) throws SQLException {
        if (!(func instanceof Funcionarios f)) {
            throw new IllegalArgumentException("Funcionário inválido");
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE)) {

            setUpdateParameters(stmt, f);
            int affected = stmt.executeUpdate();

            if (affected == 0) {
                throw new SQLException("Funcionário com código " + f.codDep() + " não encontrado");
            }
        }
    }

    public void updateCompleto(Funcionarios func) throws SQLException {
        if (!(func instanceof Funcionarios f)) {
            throw new IllegalArgumentException("Funcionário inválido");
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE_COMPLETO)) {

            setUpdateCompletoParameters(stmt, f);
            int affected = stmt.executeUpdate();

            if (affected == 0) {
                throw new SQLException("Funcionário com código " + f.codDep() + " não encontrado");
            }
        }
    }

    public void updateImagemPerfil(String codDep, byte[] imagem, String tipo, Integer tamanho) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE_IMAGEM)) {

            stmt.setBytes(1, imagem);
            stmt.setString(2, tipo);
            stmt.setInt(3, tamanho != null ? tamanho : (imagem != null ? imagem.length : 0));
            stmt.setString(4, codDep);

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Funcionário com código " + codDep + " não encontrado");
            }
        }
    }

    public void removerImagemPerfil(String codDep) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_REMOVER_IMAGEM)) {

            stmt.setString(1, codDep);
            int affected = stmt.executeUpdate();

            if (affected == 0) {
                throw new SQLException("Funcionário com código " + codDep + " não encontrado");
            }
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

    public void delete(String codDep) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_DELETE)) {

            stmt.setString(1, codDep);
            int affected = stmt.executeUpdate();

            if (affected == 0) {
                throw new SQLException("Funcionário com código " + codDep + " não encontrado");
            }
        }
    }

    public void desativar(String codDep) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_DESATIVAR)) {

            stmt.setString(1, codDep);
            int affected = stmt.executeUpdate();

            if (affected == 0) {
                throw new SQLException("Funcionário com código " + codDep + " não encontrado");
            }
        }
    }

    public void ativar(String codDep) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_ATIVAR)) {

            stmt.setString(1, codDep);
            int affected = stmt.executeUpdate();

            if (affected == 0) {
                throw new SQLException("Funcionário com código " + codDep + " não encontrado");
            }
        }
    }

    // ===== FILTROS COMBINADOS =====

    public List<Funcionarios> findByDepartamentoAndStatus(String departamento, boolean ativo) throws SQLException {
        String sql = """
                SELECT * FROM %s
                WHERE departamento = ? AND ativo = ?
                ORDER BY nome
                """.formatted(TABLE);

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, departamento);
            stmt.setBoolean(2, ativo);
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    public List<Funcionarios> findWithFilters(
            String departamento,
            String funcao,
            String localTrabalho,
            Boolean ativo,
            String nome
    ) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM " + TABLE + " WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (departamento != null && !departamento.isBlank()) {
            sql.append(" AND departamento = ?");
            params.add(departamento);
        }
        if (funcao != null && !funcao.isBlank()) {
            sql.append(" AND funcao = ?");
            params.add(funcao);
        }
        if (localTrabalho != null && !localTrabalho.isBlank()) {
            sql.append(" AND local_trabalho = ?");
            params.add(localTrabalho);
        }
        if (ativo != null) {
            sql.append(" AND ativo = ?");
            params.add(ativo);
        }
        if (nome != null && !nome.isBlank()) {
            sql.append(" AND nome LIKE ?");
            params.add("%" + nome + "%");
        }

        sql.append(" ORDER BY nome, departamento");

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

    public boolean existsByCodDep(String codDep) throws SQLException {
        String sql = "SELECT COUNT(*) FROM %s WHERE cod_dep = ?".formatted(TABLE);

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, codDep);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public boolean existsByEmail(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM %s WHERE email = ?".formatted(TABLE);

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
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

    public int countInativos() throws SQLException {
        return count("SELECT COUNT(*) FROM " + TABLE + " WHERE ativo = 0");
    }

    public int countByDepartamento(String departamento) throws SQLException {
        return count("SELECT COUNT(*) FROM " + TABLE + " WHERE departamento = ?", departamento);
    }

    public int countByFuncao(String funcao) throws SQLException {
        return count("SELECT COUNT(*) FROM " + TABLE + " WHERE funcao = ?", funcao);
    }

    public Map<String, Integer> countByDepartamento() throws SQLException {
        String sql = """
                SELECT departamento, COUNT(*) as total
                FROM %s
                WHERE ativo = 1
                GROUP BY departamento
                ORDER BY departamento
                """.formatted(TABLE);

        return countGroupBy(sql);
    }

    public Map<String, Integer> countByFuncao() throws SQLException {
        String sql = """
                SELECT funcao, COUNT(*) as total
                FROM %s
                WHERE ativo = 1
                GROUP BY funcao
                ORDER BY funcao
                """.formatted(TABLE);

        return countGroupBy(sql);
    }

    public Map<String, Integer> getEstatisticasImagem() throws SQLException {
        Map<String, Integer> stats = new HashMap<>();

        String sqlComImagem = "SELECT COUNT(*) FROM " + TABLE + " WHERE imagem_perfil IS NOT NULL AND ativo = 1";
        String sqlSemImagem = "SELECT COUNT(*) FROM " + TABLE + " WHERE imagem_perfil IS NULL AND ativo = 1";
        String sqlTamanhoTotal = "SELECT COALESCE(SUM(tamanho_imagem), 0) FROM " + TABLE + " WHERE ativo = 1";

        stats.put("com_imagem", count(sqlComImagem));
        stats.put("sem_imagem", count(sqlSemImagem));
        stats.put("tamanho_total_kb", count(sqlTamanhoTotal) / 1024);

        return Map.copyOf(stats);
    }

    public Map<String, Integer> getEstatisticas() throws SQLException {
        Map<String, Integer> stats = new LinkedHashMap<>();
        stats.put("total", countAll());
        stats.put("ativos", countAtivos());
        stats.put("inativos", countInativos());
        stats.put("departamentos", count("SELECT COUNT(DISTINCT departamento) FROM " + TABLE));
        stats.putAll(getEstatisticasImagem());
        return Map.copyOf(stats);
    }

    // ===== DISTINCT VALUES (PARA COMBOBOX) =====

    public List<String> findDepartamentos() throws SQLException {
        return findDistinct("departamento");
    }

    public List<String> findFuncoes() throws SQLException {
        return findDistinct("funcao");
    }

    public List<String> findLocaisTrabalho() throws SQLException {
        return findDistinct("local_trabalho");
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

    private List<String> findDistinct(String column) throws SQLException {
        String sql = """
                SELECT DISTINCT %s FROM %s
                WHERE ativo = 1 AND %s IS NOT NULL AND %s != ''
                ORDER BY %s
                """.formatted(column, TABLE, column, column, column);

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            List<String> resultados = new ArrayList<>();
            while (rs.next()) {
                resultados.add(rs.getString(1));
            }
            return List.copyOf(resultados);
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
            list.add(mapResultSet(rs));
        }
        return List.copyOf(list);
    }

    private Funcionarios mapResultSet(ResultSet rs) throws SQLException {
        return new Funcionarios(
                rs.getString("cod_dep"),
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
                getLocalDateTime(rs, "updated_at")
        );
    }

    private LocalDateTime getLocalDateTime(ResultSet rs, String column) throws SQLException {
        Timestamp ts = rs.getTimestamp(column);
        return ts != null ? ts.toLocalDateTime() : null;
    }

    private void setInsertParameters(PreparedStatement stmt, Funcionarios f) throws SQLException {
        stmt.setString(1, f.codDep());
        stmt.setString(2, f.nome());
        stmt.setString(3, f.funcao());
        stmt.setString(4, f.departamento());
        stmt.setString(5, f.localTrabalho());
        stmt.setString(6, f.email());
        stmt.setString(7, f.telefone());
        stmt.setBytes(8, f.imagemPerfil());
        stmt.setString(9, f.tipoImagem());
        stmt.setObject(10, f.tamanhoImagem());
        stmt.setBoolean(11, f.isAtivo());
    }

    private void setUpdateParameters(PreparedStatement stmt, Funcionarios f) throws SQLException {
        stmt.setString(1, f.nome());
        stmt.setString(2, f.funcao());
        stmt.setString(3, f.departamento());
        stmt.setString(4, f.localTrabalho());
        stmt.setString(5, f.email());
        stmt.setString(6, f.telefone());
        stmt.setBoolean(7, f.isAtivo());
        stmt.setString(8, f.codDep());
    }

    private void setUpdateCompletoParameters(PreparedStatement stmt, Funcionarios f) throws SQLException {
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
        stmt.setString(11, f.codDep());
    }
}