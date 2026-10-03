package com.ossobo.gestaoDepIt.db.sync;

import com.ossobo.gestaoDepIt.db.config.DatabaseConnection;
import com.ossobo.winterfx.anotations.Component;
import com.ossobo.winterfx.anotations.Inject;

import java.sql.SQLException;
import java.util.UUID;

/**
 * DeviceIdentity v1.1
 *
 * Objetivo: Identidade única desta instalação do app para o sistema de sync.
 *           O UUID nasce na primeira consulta e vive no SQLite local (app_meta).
 *
 * v1.1 - System.Logger nativo.
 * v1.0 - Criação.
 */
@Component
public class DeviceIdentity {

    private static final System.Logger LOGGER = System.getLogger(DeviceIdentity.class.getName());
    private static final String META_KEY = "device_id";

    @Inject
    private DatabaseConnection databaseConnection;

    private volatile String cachedId;

    public String getDeviceId() {
        if (cachedId != null) {
            return cachedId;
        }
        synchronized (this) {
            if (cachedId != null) {
                return cachedId;
            }
            try (var conn = databaseConnection.getConnection()) {
                cachedId = buscar(conn);
                if (cachedId == null) {
                    cachedId = UUID.randomUUID().toString();
                    gravar(conn, cachedId);
                    LOGGER.log(System.Logger.Level.INFO, "DeviceIdentity criada: {0}", cachedId);
                } else {
                    LOGGER.log(System.Logger.Level.DEBUG, "DeviceIdentity carregada: {0}", cachedId);
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