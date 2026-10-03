package com.ossobo.gestaoDepIt.utils.gerarCodDep;

import com.ossobo.gestaoDepIt.db.config.DatabaseConnection;
import com.ossobo.winterfx.anotations.Inject;
import com.ossobo.winterfx.anotations.Service;

import java.sql.*;
import java.time.LocalDateTime;

/**
 * GerarCodDepService v1.0
 *
 * Gera cod_dep no formato PREFIXO_NN (ex: POM_01, DAF_02).
 * Prefixo derivado do nome do departamento via DepartamentoCatalogo.
 * Sequência persistida na tabela `sequencias` (chave: prefixo) —
 * sobrevive a restarts e é transacional.
 *
 * Reaproveita a MESMA tabela `sequencias` do GerarSkuService/NumeroSerieGenerator.
 * A chave aqui é o prefixo puro ("POM"), não o cod_dep final.
 */
@Service
public class GerarCodDepService {

    private static final System.Logger logger =
            System.getLogger(GerarCodDepService.class.getName());

    @Inject
    private DatabaseConnection dbConnection;

    /** Gera cod_dep (ex: POM_01). Devolve "OUT_01" se o departamento for desconhecido. */
    public String gerar(String departamento) throws SQLException {
        if (departamento == null || departamento.isBlank())
            throw new IllegalArgumentException("Departamento é obrigatório");

        String prefixo = DepartamentoCatalogo.prefixo(departamento);
        int seq = proximaSequencia("CODDEP_" + prefixo);
        return prefixo + "_" + String.format("%02d", seq);
    }

    /**
     * Incrementa a sequência para o prefixo — transacional.
     * Usa chave "CODDEP_PREFIXO" para não colidir com sequências de SKU.
     */
    private synchronized int proximaSequencia(String chave) throws SQLException {
        try (Connection conn = dbConnection.beginTransaction()) {
            try {
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT OR IGNORE INTO sequencias (prefixo, ultimo_valor, updated_at) " +
                                "VALUES (?, '0', ?)")) {
                    ps.setString(1, chave);
                    ps.setString(2, LocalDateTime.now().toString());
                    ps.executeUpdate();
                }

                int atual;
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT ultimo_valor FROM sequencias WHERE prefixo = ?")) {
                    ps.setString(1, chave);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new SQLException("Falha ao ler sequência");
                        atual = Integer.parseInt(rs.getString(1));
                    }
                }

                int proximo = atual + 1;
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE sequencias SET ultimo_valor = ?, updated_at = ? WHERE prefixo = ?")) {
                    ps.setString(1, String.valueOf(proximo));
                    ps.setString(2, LocalDateTime.now().toString());
                    ps.setString(3, chave);
                    ps.executeUpdate();
                }

                dbConnection.commit(conn);
                return proximo;

            } catch (SQLException e) {
                dbConnection.rollback(conn);
                logger.log(System.Logger.Level.ERROR,
                        "❌ Falha ao gerar sequência para {0}: {1}", chave, e.getMessage(), e);
                throw e;
            }
        }
    }
}