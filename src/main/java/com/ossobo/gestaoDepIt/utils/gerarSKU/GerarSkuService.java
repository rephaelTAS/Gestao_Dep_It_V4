package com.ossobo.gestaoDepIt.utils.gerarSKU;

import com.ossobo.gestaoDepIt.db.config.DatabaseConnection;
import com.ossobo.winterfx.anotations.Inject;
import com.ossobo.winterfx.anotations.Service;

import java.sql.*;

/**
 * GerarSkuService v2.0
 *
 * Gera SKU no formato TIPO-MARCA-MODELO-NNN com sequência
 * persistida na tabela `sequencias` (transacional, sobrevive a restart).
 *
 * v2.0 — Migrado do ConcurrentHashMap em memória para SQLite:
 *        - Fim do bug de SKU duplicado após reinício.
 *        - Reaproveita a mesma infraestrutura do NumeroSerieGenerator.
 */
@Service
public class GerarSkuService {

    private static final System.Logger logger =
            System.getLogger(GerarSkuService.class.getName());

    @Inject
    private DatabaseConnection dbConnection;

    /** Gera SKU completo: CMP-DEL-LAT-001. */
    public String gerar(String categoria, String marca, String modelo) throws SQLException {
        if (modelo == null || modelo.isBlank())
            throw new IllegalArgumentException("Modelo é obrigatório");
        if (marca == null || marca.isBlank())
            throw new IllegalArgumentException("Marca é obrigatória");
        if (categoria == null || categoria.isBlank())
            throw new IllegalArgumentException("Categoria é obrigatória");

        String idBase = SkuCatalogo.prefixoTipo(categoria)
                + "-" + SkuCatalogo.prefixo3(marca)
                + "-" + SkuCatalogo.prefixo3(modelo);

        int seq = proximaSequencia(idBase);
        return idBase + "-" + String.format("%03d", seq);
    }

    /**
     * Incrementa a sequência para o prefixo — transacional.
     * Cria a linha se não existir (INSERT OR IGNORE).
     */
    private synchronized int proximaSequencia(String prefixo) throws SQLException {
        try (Connection conn = dbConnection.beginTransaction()) {
            try {
                // 1) Garante que a linha existe com valor inicial 0
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT OR IGNORE INTO sequencias (prefixo, ultimo_valor, updated_at) " +
                                "VALUES (?, '0', ?)")) {
                    ps.setString(1, prefixo);
                    ps.setString(2, java.time.LocalDateTime.now().toString());
                    ps.executeUpdate();
                }

                // 2) Lê valor atual
                int atual;
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT ultimo_valor FROM sequencias WHERE prefixo = ?")) {
                    ps.setString(1, prefixo);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new SQLException("Falha ao ler sequência");
                        atual = Integer.parseInt(rs.getString(1));
                    }
                }

                // 3) Atualiza para o próximo
                int proximo = atual + 1;
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE sequencias SET ultimo_valor = ?, updated_at = ? WHERE prefixo = ?")) {
                    ps.setString(1, String.valueOf(proximo));
                    ps.setString(2, java.time.LocalDateTime.now().toString());
                    ps.setString(3, prefixo);
                    ps.executeUpdate();
                }

                dbConnection.commit(conn);
                return proximo;

            } catch (SQLException e) {
                dbConnection.rollback(conn);
                logger.log(System.Logger.Level.ERROR,
                        "❌ Falha ao gerar sequência para {0}: {1}", prefixo, e.getMessage(), e);
                throw e;
            }
        }
    }
}