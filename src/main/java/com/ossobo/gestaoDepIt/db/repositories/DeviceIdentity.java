package com.ossobo.gestaoDepIt.sync;

import com.ossobo.gestaoDepIt.db.config.DatabaseConfig;
import com.ossobo.winterfx.anotations.Component;
import com.ossobo.winterfx.anotations.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.UUID;

/**
 * DeviceIdentity v1.0
 *
 * Objetivo: Identidade única desta instalação do app para o sistema de sync.
 *           O UUID nasce na primeira consulta e vive no SQLite local (app_meta).
 *
 * Nota: init LAZY (não @PostConstruct) — elimina corrida de ordem de beans
 *       contra o DatabaseInitializer; cache em memória após a 1ª carga.
 *
 * v1.0 - Criação.
 */
@Component
public class DeviceIdentity {

    private static final Logger logger = LoggerFactory.getLogger(DeviceIdentity.class);
    private static final String META_KEY = "device_id";

    @Inject
    private DatabaseConfig databaseConfig;

    private volatile String cachedId;

    /**
     * Retorna o device_id desta máquina, criando-o na primeira chamada.
     * Falha de I/O é fatal: sync não opera sem identidade.
     */
    public String getDeviceId() {
        if (cachedId != null) {
            return cachedId;
        }
        synchronized (this) {
            if (cachedId != null) {
                return cachedId;
            }
            try (var conn = databaseConfig.getConnection()) {
                cachedId = buscar(conn);
                if (cachedId == null) {
                    cachedId = UUID.randomUUID().toString();
                    gravar(conn, cachedId);
                    logger.info("🆔 DeviceIdentity criada: {}", cachedId);
                } else {
                    logger.debug("🆔 DeviceIdentity carregada: {}", cachedId);
                }
                return cachedId;
            } catch (SQLException e) {
                throw new RuntimeException("Falha ao obter identidade do dispositivo", e);
            }
        }
    }

    private String buscar(java.sql.Connection conn) throws SQLException {
        try (var ps = conn.prepareStatement(
                "SELECT valor FROM app_meta WHERE chave = ?")) {
            ps.setString(1, META_KEY);
            try (var rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("valor") : null;
            }
        }
    }

    private void gravar(java.sql.Connection conn, String deviceId) throws SQLException {
        try (var ps = conn.prepareStatement(
                "INSERT OR REPLACE INTO app_meta (chave, valor) VALUES (?, ?)")) {
            ps.setString(1, META_KEY);
            ps.setString(2, deviceId);
            ps.executeUpdate();
        }
    }
}