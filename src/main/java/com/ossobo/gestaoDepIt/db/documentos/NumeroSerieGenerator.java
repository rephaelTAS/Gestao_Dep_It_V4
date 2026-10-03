package com.ossobo.gestaoDepIt.db.documentos;

import com.ossobo.gestaoDepIt.db.config.DatabaseConnection;
import com.ossobo.gestaoDepIt.db.sync.SyncTime;
import com.ossobo.winterfx.anotations.Inject;
import com.ossobo.winterfx.anotations.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * NumeroSerieGenerator v1.0
 *
 * Responsabilidade: gerar e persistir o próximo número de série no formato
 *                   Agri-XXX-YYY-DD-DD-S, usando a tabela sequencias como
 *                   fonte transacional do último valor emitido por prefixo.
 *
 * Prefixo = "Agri-" + codDep + "-" + codMarca + "-"
 * Contador = "DD-DD-S"  (ver NumeroSerieCodec)
 *
 * Concorrência: leitura + escrita numa única transação com
 *               SELECT ... FOR UPDATE quando disponível; SQLite trata a
 *               escrita serial, então o pior caso é retry.
 *
 * @since v1.0
 */
@Service
public class NumeroSerieGenerator {

    private static final String EMPRESA = "Agri";

    @Inject
    private DatabaseConnection dbConnection;

    /**
     * Emite o próximo número de série para (departamento, marca).
     * Retorna o valor completo, ex.: "Agri-Dcl-Log-00-01-A".
     */
    public String proximo(String nomeDepartamento, String nomeMarca) throws SQLException {
        String codDep = CodigoCurto.deDepartamento(nomeDepartamento);
        String codMarca = CodigoCurto.deMarca(nomeMarca);
        String prefixo = EMPRESA + "-" + codDep + "-" + codMarca + "-";

        try (Connection conn = dbConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String ultimo = lerUltimo(conn, prefixo)
                        .orElse(NumeroSerieCodec.primeiro());
                String proximo = NumeroSerieCodec.proximo(ultimo);
                gravar(conn, prefixo, proximo);
                conn.commit();
                return prefixo + proximo;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private Optional<String> lerUltimo(Connection conn, String prefixo) throws SQLException {
        String sql = "SELECT ultimo_valor FROM sequencias WHERE prefixo = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, prefixo);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(rs.getString(1)) : Optional.empty();
            }
        }
    }

    private void gravar(Connection conn, String prefixo, String valor) throws SQLException {
        String agora = SyncTime.agora();
        String sql = """
                INSERT INTO sequencias (prefixo, ultimo_valor, updated_at)
                VALUES (?, ?, ?)
                ON CONFLICT(prefixo) DO UPDATE SET
                    ultimo_valor = excluded.ultimo_valor,
                    updated_at   = excluded.updated_at
                """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, prefixo);
            stmt.setString(2, valor);
            stmt.setString(3, agora);
            stmt.executeUpdate();
        }
    }
}