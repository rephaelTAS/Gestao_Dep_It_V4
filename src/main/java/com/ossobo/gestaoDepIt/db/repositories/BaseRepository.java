package com.ossobo.gestaoDepIt.db.repositories;

import com.ossobo.gestaoDepIt.db.config.DatabaseConnection;


import com.ossobo.winterfx.anotations.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * BaseRepository - Classe base para repositórios com WinterFX
 * v2.1 - Corrigido conflito de variável no switch
 *
 * Responsabilidades:
 * - Fornecer métodos comuns para execução de queries
 * - Mapeamento de ResultSet para objetos
 * - Tratamento de parâmetros com Pattern Matching
 */
public abstract class BaseRepository {

    private static final Logger logger = LoggerFactory.getLogger(BaseRepository.class);

    @Inject
    private DatabaseConnection dbConnection;

    // ===== CONSTRUTOR =====

    public BaseRepository() {
        // Injeção de dependência via WinterFX
    }

    public BaseRepository(DatabaseConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    // ===== INTERFACES FUNCIONAIS =====

    /**
     * Interface funcional para mapear ResultSet para objeto.
     */
    @FunctionalInterface
    protected interface RowMapper<T> {
        T mapRow(ResultSet rs) throws SQLException;
    }

    // ===== MÉTODOS DE EXECUÇÃO =====

    /**
     * Executa uma query SELECT e retorna lista de objetos mapeados.
     * @param sql Query SQL com placeholders (?)
     * @param mapper Função para mapear ResultSet → Objeto
     * @param params Parâmetros para a query
     * @return Lista de objetos mapeados (imutável)
     */
    protected <T> List<T> executeQuery(String sql, RowMapper<T> mapper, Object... params) {
        // Validação com Pattern Matching (Java 16+)
        if (!(sql instanceof String s) || s.isBlank()) {
            throw new IllegalArgumentException("SQL não pode ser vazio");
        }

        List<T> results = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            setParameters(pstmt, params);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapper.mapRow(rs));
                }
            }

        } catch (SQLException e) {
            logger.error("❌ Erro ao executar query: {}", sql, e);
            throw new RuntimeException("Erro ao executar query", e);
        }

        return List.copyOf(results);
    }

    /**
     * Executa uma query SELECT e retorna um Optional com o resultado.
     */
    protected <T> Optional<T> executeQuerySingle(String sql, RowMapper<T> mapper, Object... params) {
        List<T> results = executeQuery(sql, mapper, params);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    /**
     * Executa uma query de UPDATE/INSERT/DELETE.
     * @return Número de linhas afetadas
     */
    protected int executeUpdate(String sql, Object... params) {
        if (!(sql instanceof String s) || s.isBlank()) {
            throw new IllegalArgumentException("SQL não pode ser vazio");
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            setParameters(pstmt, params);
            return pstmt.executeUpdate();

        } catch (SQLException e) {
            logger.error("❌ Erro ao executar update: {}", sql, e);
            throw new RuntimeException("Erro ao executar update", e);
        }
    }

    /**
     * Executa uma query de INSERT e retorna o ID gerado.
     * @return ID gerado ou -1 se não houver
     */
    protected long executeInsert(String sql, Object... params) {
        if (!(sql instanceof String s) || s.isBlank()) {
            throw new IllegalArgumentException("SQL não pode ser vazio");
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            setParameters(pstmt, params);
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }

            return -1;

        } catch (SQLException e) {
            logger.error("❌ Erro ao executar insert: {}", sql, e);
            throw new RuntimeException("Erro ao executar insert", e);
        }
    }

    /**
     * Executa uma query de INSERT e retorna o ID gerado como Optional.
     */
    protected Optional<Long> executeInsertOptional(String sql, Object... params) {
        long id = executeInsert(sql, params);
        return id > 0 ? Optional.of(id) : Optional.empty();
    }

    // ===== MÉTODOS EM LOTE =====

    /**
     * Executa uma query em lote (batch).
     * @return Array com resultados de cada execução
     */
    protected int[] executeBatch(String sql, List<Object[]> batchParams) {
        if (!(sql instanceof String s) || s.isBlank()) {
            throw new IllegalArgumentException("SQL não pode ser vazio");
        }
        if (batchParams == null || batchParams.isEmpty()) {
            return new int[0];
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);

            for (Object[] params : batchParams) {
                setParameters(pstmt, params);
                pstmt.addBatch();
            }

            int[] results = pstmt.executeBatch();
            conn.commit();

            return results;

        } catch (SQLException e) {
            logger.error("❌ Erro ao executar batch: {}", sql, e);
            throw new RuntimeException("Erro ao executar batch", e);
        }
    }

    // ===== MÉTODOS DE CONTAGEM =====

    /**
     * Conta registros de uma tabela.
     * @return Número de registros
     */
    protected int count(String tableName) {
        if (!(tableName instanceof String t) || t.isBlank()) {
            throw new IllegalArgumentException("Nome da tabela não pode ser vazio");
        }

        String sql = "SELECT COUNT(*) FROM " + tableName;
        List<Integer> results = executeQuery(sql, rs -> rs.getInt(1));
        return results.isEmpty() ? 0 : results.get(0);
    }

    /**
     * Conta registros com condição.
     */
    protected int count(String tableName, String whereClause, Object... params) {
        if (!(tableName instanceof String t) || t.isBlank()) {
            throw new IllegalArgumentException("Nome da tabela não pode ser vazio");
        }
        if (!(whereClause instanceof String w) || w.isBlank()) {
            throw new IllegalArgumentException("Cláusula WHERE não pode ser vazia");
        }

        String sql = "SELECT COUNT(*) FROM " + tableName + " WHERE " + whereClause;
        List<Integer> results = executeQuery(sql, rs -> rs.getInt(1), params);
        return results.isEmpty() ? 0 : results.get(0);
    }

    /**
     * Verifica se existe registro com a condição.
     */
    protected boolean exists(String tableName, String whereClause, Object... params) {
        return count(tableName, whereClause, params) > 0;
    }

    /**
     * Verifica se existe registro com ID.
     */
    protected boolean existsById(String tableName, Object id) {
        return count(tableName, "id = ?", id) > 0;
    }

    // ===== MÉTODOS AUXILIARES DE PARÂMETROS =====

    /**
     * Configura os parâmetros do PreparedStatement com Pattern Matching.
     * Suporta: String, Integer, Long, Double, Boolean, LocalDate, LocalDateTime, byte[], Date, Enum
     */
    private void setParameters(PreparedStatement pstmt, Object... params) throws SQLException {
        if (params == null || params.length == 0) {
            return;
        }

        for (int i = 0; i < params.length; i++) {
            Object param = params[i];
            int index = i + 1;

            // Pattern Matching para cada tipo (Java 16+)
            // ✅ CORRIGIDO: Usar "intVal" em vez de "i" para evitar conflito
            switch (param) {
                case null -> pstmt.setNull(index, Types.NULL);
                case String s -> pstmt.setString(index, s);
                case Integer intVal -> pstmt.setInt(index, intVal);
                case Long longVal -> pstmt.setLong(index, longVal);
                case Double doubleVal -> pstmt.setDouble(index, doubleVal);
                case Boolean boolVal -> pstmt.setBoolean(index, boolVal);
                case LocalDate localDate -> pstmt.setDate(index, Date.valueOf(localDate));
                case LocalDateTime localDateTime -> pstmt.setTimestamp(index, Timestamp.valueOf(localDateTime));
                case byte[] bytes -> pstmt.setBytes(index, bytes);
                case java.util.Date utilDate -> pstmt.setDate(index, new Date(utilDate.getTime()));
                case Enum<?> enumVal -> pstmt.setString(index, enumVal.name());
                default -> pstmt.setObject(index, param);
            }
        }
    }

    // ===== MÉTODOS DE MAPEAMENTO =====

    /**
     * Mapeia um ResultSet para LocalDate com validação.
     */
    protected LocalDate getLocalDate(ResultSet rs, String column) throws SQLException {
        Date d = rs.getDate(column);
        return d != null ? d.toLocalDate() : null;
    }

    /**
     * Mapeia um ResultSet para LocalDateTime com validação.
     */
    protected LocalDateTime getLocalDateTime(ResultSet rs, String column) throws SQLException {
        Timestamp ts = rs.getTimestamp(column);
        return ts != null ? ts.toLocalDateTime() : null;
    }

    /**
     * Mapeia um ResultSet para um Enum com fallback.
     */
    protected <E extends Enum<E>> E getEnum(ResultSet rs, String column, Class<E> enumClass, E fallback)
            throws SQLException {
        String value = rs.getString(column);
        if (value == null) {
            return fallback;
        }
        try {
            return Enum.valueOf(enumClass, value);
        } catch (IllegalArgumentException e) {
            logger.warn("Valor inválido para enum {}: {}, usando fallback", enumClass.getSimpleName(), value);
            return fallback;
        }
    }

    /**
     * Mapeia um ResultSet para byte[] com validação.
     */
    protected byte[] getBytes(ResultSet rs, String column) throws SQLException {
        byte[] data = rs.getBytes(column);
        return rs.wasNull() ? null : data;
    }

    // ===== MÉTODOS DE UTILIDADE =====

    /**
     * Escapa caracteres especiais para LIKE (evita SQL Injection parcial).
     */
    protected String escapeLike(String value) {
        if (value == null) return null;
        return value
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }

    /**
     * Converte camelCase para snake_case.
     * Exemplo: "skuProduto" → "sku_produto"
     */
    protected String camelToSnake(String camelCase) {
        if (camelCase == null || camelCase.isBlank()) {
            return camelCase;
        }

        // Mapeamento explícito para campos conhecidos
        return switch (camelCase) {
            case "skuProduto" -> "sku_produto";
            case "inventarioId" -> "inventario_id";
            case "usuarioResponsavel" -> "usuario_responsavel";
            case "percentagemRestante" -> "percentagem_restante";
            case "ciclosImpressao" -> "ciclos_impressao";
            case "dataInstalacao" -> "data_instalacao";
            case "dataAquisicao" -> "data_aquisicao";
            case "dataValidade" -> "data_validade";
            case "dataFimLicenca" -> "data_fim_licenca";
            case "dataUltimaVerificacao" -> "data_ultima_verificacao";
            case "codDepFuncionario" -> "cod_dep_funcionario";
            case "funcionarioId" -> "funcionario_id";
            case "numeroSerie" -> "numero_serie";
            case "precoUnitario" -> "preco_unitario";
            case "tipoProduto" -> "tipo_produto";
            case "caracteristicasTecnicas" -> "caracteristicas_tecnicas";
            default -> camelCase.replaceAll("([A-Z])", "_$1").toLowerCase();
        };
    }

    /**
     * Gera placeholders para IN clause.
     * Exemplo: placeholders(3) → "?, ?, ?"
     */
    protected String placeholders(int count) {
        if (count <= 0) return "";
        return "?" + ", ?".repeat(count - 1);
    }

    /**
     * Gera placeholders para IN clause com prefixo.
     * Exemplo: placeholders("p", 3) → "p1, p2, p3"
     */
    protected String placeholders(String prefix, int count) {
        if (count <= 0 || prefix == null || prefix.isBlank()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= count; i++) {
            if (i > 1) sb.append(", ");
            sb.append(prefix).append(i);
        }
        return sb.toString();
    }

    // ===== MÉTODOS DE VALIDAÇÃO =====

    /**
     * Valida que um parâmetro não seja nulo ou vazio.
     * @throws IllegalArgumentException se inválido
     */
    protected void requireNonBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Valida que um ID seja positivo.
     * @throws IllegalArgumentException se inválido
     */
    protected void requirePositive(Long id, String message) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Valida que um ID seja positivo.
     * @throws IllegalArgumentException se inválido
     */
    protected void requirePositive(Integer id, String message) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Valida que um objeto não seja nulo.
     * @throws IllegalArgumentException se nulo
     */
    protected void requireNonNull(Object obj, String message) {
        if (obj == null) {
            throw new IllegalArgumentException(message);
        }
    }
}