package com.ossobo.gestaoDepIt.db.relatorios;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ossobo.gestaoDepIt.db.config.DatabaseConnection;
import com.ossobo.winterfx.anotations.Inject;
import com.ossobo.winterfx.anotations.Repository;

import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * RelatorioModeloRepository v2.0
 *
 * Schema v2:
 *   id, nome, tabela_base, colunas_base (CSV),
 *   tabelas_combinadas (JSON), filtros (JSON), criado_em.
 *
 * v2.0 — Remove device_id (modelo é local). Substitui CSV de joins por
 *        JSON estruturado [{alias, colunas, condicao}]. Renomeia "colunas"
 *        para "colunas_base". Renomeia "joins" para "tabelas_combinadas".
 *        parseColunasBase recebe a tabelaBase para reconstruir o alias real.
 */
@Repository
public class RelatorioModeloRepository {

    private static final System.Logger logger =
            System.getLogger(RelatorioModeloRepository.class.getName());

    private static final DateTimeFormatter FMT_SQLITE =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final Gson GSON = new Gson();

    private static final Type TYPE_COMBINADAS = new TypeToken<List<Combinada>>(){}.getType();

    private static final String TABLE = "relatorio_modelos";

    private static final String SQL_INSERT = """
            INSERT INTO %s (id, nome, tabela_base, colunas_base, tabelas_combinadas, filtros, criado_em)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """.formatted(TABLE);

    private static final String SQL_UPDATE = """
            UPDATE %s
               SET nome = ?, tabela_base = ?, colunas_base = ?,
                   tabelas_combinadas = ?, filtros = ?
             WHERE id = ?
            """.formatted(TABLE);

    private static final String SQL_DELETE =
            "DELETE FROM " + TABLE + " WHERE id = ?";

    private static final String SQL_FIND_BY_ID =
            "SELECT * FROM " + TABLE + " WHERE id = ?";

    private static final String SQL_FIND_BY_NOME =
            "SELECT * FROM " + TABLE + " WHERE nome = ?";

    private static final String SQL_FIND_ALL =
            "SELECT * FROM " + TABLE + " ORDER BY criado_em DESC";

    private static final String SQL_EXISTS_NOME =
            "SELECT 1 FROM " + TABLE + " WHERE nome = ?";

    @Inject
    private DatabaseConnection dbConnection;

    // ============================================================
    // ESCRITA
    // ============================================================

    public RelatorioModelo insert(RelatorioModelo m) throws SQLException {
        if (m == null) throw new IllegalArgumentException("Modelo inválido");
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_INSERT)) {
            stmt.setString(1, m.id());
            stmt.setString(2, m.nome());
            stmt.setString(3, m.tabelaBase());
            stmt.setString(4, colunasBaseCsv(m.colunasBase()));
            stmt.setString(5, combinadasJson(m.joins()));
            stmt.setString(6, m.filtrosJson());
            stmt.setString(7, m.criadoEm().toString());
            stmt.executeUpdate();
            logger.log(System.Logger.Level.INFO,
                    "✅ Modelo inserido: ID={0}, nome={1}", m.id(), m.nome());
            return m;
        }
    }

    public void update(RelatorioModelo m) throws SQLException {
        if (m == null) throw new IllegalArgumentException("Modelo inválido");
        if (m.id() == null || m.id().isBlank())
            throw new SQLException("ID do modelo é obrigatório para atualização");

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE)) {
            stmt.setString(1, m.nome());
            stmt.setString(2, m.tabelaBase());
            stmt.setString(3, colunasBaseCsv(m.colunasBase()));
            stmt.setString(4, combinadasJson(m.joins()));
            stmt.setString(5, m.filtrosJson());
            stmt.setString(6, m.id());
            int afetadas = stmt.executeUpdate();
            if (afetadas == 0)
                throw new SQLException("Modelo não encontrado: " + m.id());
            logger.log(System.Logger.Level.INFO,
                    "✅ Modelo atualizado: ID={0}, nome={1}", m.id(), m.nome());
        }
    }

    public boolean delete(String id) throws SQLException {
        if (id == null || id.isBlank()) return false;
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_DELETE)) {
            stmt.setString(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    // ============================================================
    // LEITURA
    // ============================================================

    public Optional<RelatorioModelo> findById(String id) throws SQLException {
        if (id == null || id.isBlank()) return Optional.empty();
        return queryUm(SQL_FIND_BY_ID, id);
    }

    public Optional<RelatorioModelo> findByNome(String nome) throws SQLException {
        if (nome == null || nome.isBlank()) return Optional.empty();
        return queryUm(SQL_FIND_BY_NOME, nome);
    }

    public List<RelatorioModelo> findAll() throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_ALL);
             ResultSet rs = stmt.executeQuery()) {
            return mapResultSetList(rs);
        }
    }

    public boolean existsByNome(String nome) throws SQLException {
        if (nome == null || nome.isBlank()) return false;
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_EXISTS_NOME)) {
            stmt.setString(1, nome);
            try (ResultSet rs = stmt.executeQuery()) { return rs.next(); }
        }
    }

    public boolean existsById(String id) throws SQLException {
        return findById(id).isPresent();
    }

    // ============================================================
    // AUXILIARES — mapeamento
    // ============================================================

    private Optional<RelatorioModelo> queryUm(String sql, String param) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, param);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapResultSet(rs)) : Optional.empty();
            }
        }
    }

    private List<RelatorioModelo> mapResultSetList(ResultSet rs) throws SQLException {
        List<RelatorioModelo> list = new ArrayList<>();
        while (rs.next()) list.add(mapResultSet(rs));
        return List.copyOf(list);
    }

    private RelatorioModelo mapResultSet(ResultSet rs) throws SQLException {
        String tabelaBase = rs.getString("tabela_base");
        return new RelatorioModelo(
                rs.getString("id"),
                rs.getString("nome"),
                tabelaBase,
                parseColunasBase(rs.getString("colunas_base"), tabelaBase),
                parseCombinadas(rs.getString("tabelas_combinadas")),
                rs.getString("filtros"),
                getLocalDateTime(rs.getString("criado_em")),
                null
        );
    }

    // ============================================================
    // SERIALIZAÇÃO — colunas_base (CSV) e tabelas_combinadas (JSON)
    // ============================================================

    /** Formato CSV: "coluna1,coluna2,..." — sem prefixo de tabela (a base é implícita). */
    private String colunasBaseCsv(List<RelatorioColuna> cols) {
        if (cols == null || cols.isEmpty()) return "";
        return cols.stream()
                .map(RelatorioColuna::coluna)
                .reduce((a, b) -> a + "," + b)
                .orElse("");
    }

    /** Reconstrói RelatorioColuna com o alias real da tabela base. */
    private List<RelatorioColuna> parseColunasBase(String csv, String tabelaBase) {
        if (csv == null || csv.isBlank()) return List.of();
        if (tabelaBase == null || tabelaBase.isBlank()) return List.of();
        return Arrays.stream(csv.split(","))
                .map(String::trim).filter(s -> !s.isEmpty())
                .map(c -> new RelatorioColuna(tabelaBase, c, c))
                .toList();
    }

    /** Estrutura serializada de cada JOIN: alias + colunas + condição. */
    private record Combinada(String alias, List<String> colunas, String condicao) {}

    private String combinadasJson(List<RelatorioJoin> joins) {
        if (joins == null || joins.isEmpty()) return "[]";
        List<Combinada> lista = joins.stream()
                .map(j -> new Combinada(
                        j.tabela(),
                        j.colunas().stream().map(RelatorioColuna::coluna).toList(),
                        j.condicao()))
                .toList();
        return GSON.toJson(lista);
    }

    private List<RelatorioJoin> parseCombinadas(String json) {
        if (json == null || json.isBlank() || json.equals("[]")) return List.of();
        try {
            List<Combinada> lista = GSON.fromJson(json, TYPE_COMBINADAS);
            if (lista == null) return List.of();
            return lista.stream()
                    .map(c -> new RelatorioJoin(
                            c.alias(),
                            c.colunas().stream()
                                    .map(col -> new RelatorioColuna(c.alias(), col, col))
                                    .toList(),
                            c.condicao()))
                    .toList();
        } catch (Exception e) {
            logger.log(System.Logger.Level.WARNING,
                    "Falha ao parsear tabelas_combinadas: {0}", json);
            return List.of();
        }
    }

    private LocalDateTime getLocalDateTime(String raw) {
        if (raw == null || raw.isBlank()) return LocalDateTime.now();
        try {
            return raw.contains("T")
                    ? LocalDateTime.parse(raw)
                    : LocalDateTime.parse(raw, FMT_SQLITE);
        } catch (Exception e) {
            logger.log(System.Logger.Level.WARNING,
                    "Data inválida em {0}: {1}", TABLE, raw);
            return LocalDateTime.now();
        }
    }
}