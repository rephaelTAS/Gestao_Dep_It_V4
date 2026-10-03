package com.ossobo.gestaoDepIt.db.relatorios;

import com.ossobo.gestaoDepIt.db.config.DatabaseConnection;
import com.ossobo.gestaoDepIt.db.relatorios.RelatorioHistorico;
import com.ossobo.winterfx.anotations.Inject;
import com.ossobo.winterfx.anotations.Repository;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * RelatorioHistoricoRepository v1.0
 *
 * Acesso a relatorio_historico (ledger append-only — só INSERT e SELECT).
 * Relatório é LOCAL: NÃO carimba device_id (fora do sync distribuído).
 *
 * Datas: leitura TOLERANTE — aceita ISO com 'T' e formato SQLite
 * ("yyyy-MM-dd HH:mm:ss"), replicando o padrão dos demais repositórios.
 */
@Repository
public class RelatorioHistoricoRepository {

    private static final System.Logger logger =
            System.getLogger(RelatorioHistoricoRepository.class.getName());

    private static final DateTimeFormatter FMT_SQLITE =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final String TABLE = "relatorio_historico";

    private static final String SQL_INSERT = """
            INSERT INTO %s (id, titulo, tabelas, filtros, gerado_em, device_id)
            VALUES (?, ?, ?, ?, ?, NULL)
            """.formatted(TABLE);

    private static final String SQL_FIND_BY_ID =
            "SELECT * FROM " + TABLE + " WHERE id = ?";

    private static final String SQL_FIND_RECENTES =
            "SELECT * FROM " + TABLE + " ORDER BY gerado_em DESC LIMIT ?";

    private static final String SQL_FIND_POR_PERIODO =
            "SELECT * FROM " + TABLE + " WHERE gerado_em >= ? AND gerado_em <= ? ORDER BY gerado_em DESC";

    private static final String SQL_COUNT_TOTAL =
            "SELECT COUNT(*) FROM " + TABLE;

    private static final String SQL_COUNT_POR_COMBINACAO =
            "SELECT tabelas, COUNT(*) AS total FROM " + TABLE +
                    " GROUP BY tabelas ORDER BY total DESC";

    @Inject
    private DatabaseConnection dbConnection;

    // ============================================================
    // ESCRITA (append-only)
    // ============================================================

    public RelatorioHistorico inserir(RelatorioHistorico h) throws SQLException {
        if (h == null) throw new IllegalArgumentException("Histórico inválido");

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_INSERT)) {
            stmt.setString(1, h.id());
            stmt.setString(2, h.titulo());
            stmt.setString(3, h.tabelasCsv());
            stmt.setString(4, h.filtrosJson());
            stmt.setString(5, h.geradoEm().toString());
            stmt.executeUpdate();
            logger.log(System.Logger.Level.INFO,
                    "✅ Histórico registrado: ID={0}, título={1}", h.id(), h.titulo());
            return h;
        }
    }

    // ============================================================
    // LEITURA
    // ============================================================

    public Optional<RelatorioHistorico> findById(String id) throws SQLException {
        if (id == null || id.isBlank()) return Optional.empty();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_ID)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapResultSet(rs)) : Optional.empty();
            }
        }
    }

    public List<RelatorioHistorico> findRecentes(int limite) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_RECENTES)) {
            stmt.setInt(1, Math.max(1, limite));
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    public List<RelatorioHistorico> findByPeriodo(LocalDateTime inicio, LocalDateTime fim) throws SQLException {
        if (inicio == null || fim == null) return List.of();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_POR_PERIODO)) {
            stmt.setString(1, inicio.toString());
            stmt.setString(2, fim.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetList(rs);
            }
        }
    }

    public long countTotal() throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL_COUNT_TOTAL)) {
            return rs.next() ? rs.getLong(1) : 0L;
        }
    }

    /** Agrupamento para o BarChart: contagem por combinação de tabelas. */
    public Map<String, Integer> countPorCombinacao() throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL_COUNT_POR_COMBINACAO)) {
            Map<String, Integer> out = new LinkedHashMap<>();
            while (rs.next()) out.put(rs.getString(1), rs.getInt(2));
            return Map.copyOf(out);
        }
    }

    // ============================================================
    // MAPPER
    // ============================================================

    private List<RelatorioHistorico> mapResultSetList(ResultSet rs) throws SQLException {
        List<RelatorioHistorico> list = new ArrayList<>();
        while (rs.next()) list.add(mapResultSet(rs));
        return List.copyOf(list);
    }

    private RelatorioHistorico mapResultSet(ResultSet rs) throws SQLException {
        return new RelatorioHistorico(
                rs.getString("id"),
                rs.getString("titulo"),
                splitCsv(rs.getString("tabelas")),
                rs.getString("filtros"),
                getLocalDateTime(rs.getString("gerado_em")),
                rs.getString("device_id")
        );
    }

    private List<String> splitCsv(String s) {
        if (s == null || s.isBlank()) return List.of();
        return java.util.Arrays.stream(s.split(","))
                .map(String::trim).filter(x -> !x.isEmpty()).toList();
    }

    /**
     * Parser TOLERANTE — evita o bug DateTimeParseException já diagnosticado:
     * SQLite grava "yyyy-MM-dd HH:mm:ss"; o app grava ISO com 'T'.
     */
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