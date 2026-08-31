package com.ossobo.gestaoDepIt.db.repositories;

import com.ossobo.gestaoDepIt.db.config.DatabaseConnection;
import com.ossobo.gestaoDepIt.db.models.ConfigServidorRemoto;


import com.ossobo.winterfx.anotations.Inject;
import com.ossobo.winterfx.anotations.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * ConfigServidorRemotoRepository - Acesso a dados com WinterFX
 * v2.0 - Migrado para Java 17+ com Records e Text Blocks
 *
 * Responsabilidades:
 * - CRUD para ConfigServidorRemoto
 * - Buscar configuração ativa
 * - Atualizar status de conexão
 */
@Repository
public class ConfigServidorRemotoRepository {

    private static final Logger logger = LoggerFactory.getLogger(ConfigServidorRemotoRepository.class);

    @Inject
    private DatabaseConnection dbConnection;

    private static final String TABLE = "config_servidor_remoto";

    // ===== SQL COM TEXT BLOCKS =====

    private static final String SQL_FIND_ALL = """
            SELECT * FROM %s
            ORDER BY nome_config
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_ID = """
            SELECT * FROM %s
            WHERE id = ?
            """.formatted(TABLE);

    private static final String SQL_FIND_ATIVO = """
            SELECT * FROM %s
            WHERE ativo = 1
            LIMIT 1
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_NOME = """
            SELECT * FROM %s
            WHERE nome_config = ?
            """.formatted(TABLE);

    private static final String SQL_INSERT = """
            INSERT INTO %s (
                nome_config, tipo_banco, host, porta, database_name,
                usuario, senha, parametros_extra, ativo,
                status_conexao, created_at, updated_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            """.formatted(TABLE);

    private static final String SQL_UPDATE = """
            UPDATE %s
            SET nome_config = ?, tipo_banco = ?, host = ?, porta = ?,
                database_name = ?, usuario = ?, senha = ?, parametros_extra = ?,
                ativo = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """.formatted(TABLE);

    private static final String SQL_UPDATE_STATUS = """
            UPDATE %s
            SET status_conexao = ?, ultima_conexao = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """.formatted(TABLE);

    private static final String SQL_DESATIVAR_TODAS = """
            UPDATE %s
            SET ativo = 0, updated_at = CURRENT_TIMESTAMP
            WHERE ativo = 1
            """.formatted(TABLE);

    private static final String SQL_DELETE = """
            DELETE FROM %s
            WHERE id = ?
            """.formatted(TABLE);

    // ===== MÉTODOS CRUD =====

    public List<ConfigServidorRemoto> findAll() throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_ALL);
             ResultSet rs = stmt.executeQuery()) {
            return mapResultSetList(rs);
        }
    }

    public Optional<ConfigServidorRemoto> findById(Long id) throws SQLException {
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

    public Optional<ConfigServidorRemoto> findAtivo() throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_ATIVO);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? Optional.of(mapResultSet(rs)) : Optional.empty();
        }
    }

    public Optional<ConfigServidorRemoto> findByNome(String nomeConfig) throws SQLException {
        if (!(nomeConfig instanceof String n) || n.isBlank()) {
            return Optional.empty();
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_NOME)) {

            stmt.setString(1, nomeConfig);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapResultSet(rs)) : Optional.empty();
            }
        }
    }

    public Long insert(ConfigServidorRemoto config) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_INSERT,
                     PreparedStatement.RETURN_GENERATED_KEYS)) {

            setInsertParameters(stmt, config);
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
                throw new SQLException("Falha ao obter ID gerado");
            }
        }
    }

    public void update(ConfigServidorRemoto config) throws SQLException {
        if (!(config instanceof ConfigServidorRemoto c)) {
            throw new IllegalArgumentException("Configuração inválida");
        }
        if (c.id() == null) {
            throw new SQLException("ID não pode ser nulo para atualização");
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE)) {

            setUpdateParameters(stmt, config);
            int affected = stmt.executeUpdate();

            if (affected == 0) {
                throw new SQLException("Configuração com ID " + c.id() + " não encontrada");
            }
        }
    }

    public void delete(Long id) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_DELETE)) {

            stmt.setLong(1, id);
            int affected = stmt.executeUpdate();

            if (affected == 0) {
                throw new SQLException("Configuração com ID " + id + " não encontrada");
            }
        }
    }

    /**
     * Salva (insert ou update) baseado na existência do ID
     */
    public Long save(ConfigServidorRemoto config) throws SQLException {
        if (config.id() == null) {
            return insert(config);
        } else {
            update(config);
            return config.id();
        }
    }

    /**
     * Desativa todas as configurações
     * @return número de registros atualizados
     */
    public int desativarTodas() throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_DESATIVAR_TODAS)) {

            return stmt.executeUpdate();
        }
    }

    /**
     * Atualiza status de conexão
     */
    public void atualizarStatus(Long id, String status, String ultimaConexao) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE_STATUS)) {

            stmt.setString(1, status);
            stmt.setString(2, ultimaConexao);
            stmt.setLong(3, id);
            stmt.executeUpdate();
        }
    }

    /**
     * Verifica se existe configuração com determinado nome
     */
    public boolean existsByNome(String nomeConfig) throws SQLException {
        String sql = "SELECT COUNT(*) FROM %s WHERE nome_config = ?".formatted(TABLE);

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nomeConfig);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    // ===== MÉTODOS PRIVADOS =====

    private void setInsertParameters(PreparedStatement stmt, ConfigServidorRemoto c) throws SQLException {
        stmt.setString(1, c.nomeConfig());
        stmt.setString(2, c.tipoBanco());
        stmt.setString(3, c.host());
        stmt.setString(4, c.porta());
        stmt.setString(5, c.databaseName());
        stmt.setString(6, c.usuario());
        stmt.setString(7, c.senha());
        stmt.setString(8, c.parametrosExtra());
        stmt.setInt(9, c.ativo() != null ? c.ativo() : 1);
        stmt.setString(10, c.statusConexao() != null ? c.statusConexao() : "NAO_TESTADO");
    }

    private void setUpdateParameters(PreparedStatement stmt, ConfigServidorRemoto c) throws SQLException {
        stmt.setString(1, c.nomeConfig());
        stmt.setString(2, c.tipoBanco());
        stmt.setString(3, c.host());
        stmt.setString(4, c.porta());
        stmt.setString(5, c.databaseName());
        stmt.setString(6, c.usuario());
        stmt.setString(7, c.senha());
        stmt.setString(8, c.parametrosExtra());
        stmt.setInt(9, c.ativo() != null ? c.ativo() : 1);
        stmt.setLong(10, c.id());
    }

    private List<ConfigServidorRemoto> mapResultSetList(ResultSet rs) throws SQLException {
        List<ConfigServidorRemoto> list = new ArrayList<>();
        while (rs.next()) {
            list.add(mapResultSet(rs));
        }
        return List.copyOf(list);
    }

    private ConfigServidorRemoto mapResultSet(ResultSet rs) throws SQLException {
        return new ConfigServidorRemoto(
                rs.getLong("id"),
                rs.getString("nome_config"),
                rs.getString("tipo_banco"),
                rs.getString("host"),
                rs.getString("porta"),
                rs.getString("database_name"),
                rs.getString("usuario"),
                rs.getString("senha"),
                rs.getString("parametros_extra"),
                rs.getInt("ativo"),
                rs.getString("ultima_conexao"),
                rs.getString("status_conexao"),
                getLocalDateTime(rs, "created_at"),
                getLocalDateTime(rs, "updated_at")
        );
    }

    private LocalDateTime getLocalDateTime(ResultSet rs, String column) throws SQLException {
        Timestamp ts = rs.getTimestamp(column);
        return ts != null ? ts.toLocalDateTime() : null;
    }
}