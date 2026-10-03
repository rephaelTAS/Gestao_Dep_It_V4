package com.ossobo.gestaoDepIt.db.sync;

import com.ossobo.gestaoDepIt.db.models.ConfigServidorRemoto;
import com.ossobo.gestaoDepIt.db.repositories.ConfigServidorRemotoRepository;
import com.ossobo.winterfx.anotations.Component;
import com.ossobo.winterfx.anotations.Inject;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * MysqlConnection v1.1
 *
 * STATELESS — não mantém conexão persistente, não cacheia estado.
 * Cada chamada consulta o banco local e testa rede.
 *
 * Estados do canal remoto:
 * - SEM_CONFIG: nenhuma config ativa → modo local PERMANENTE
 * - OFFLINE: config existe, host inacessível → modo local TEMPORÁRIO
 * - ONLINE: pipeline liberado
 *
 * v1.1: Decisão via findAtivo() (fonte única, sem contagens).
 * v1.0: Criação.
 */
@Component
public class MysqlConnection {

    private static final System.Logger LOGGER = System.getLogger(MysqlConnection.class.getName());
    private static final int TCP_TIMEOUT_MS = 3000;

    @Inject
    private ConfigServidorRemotoRepository configRepository;

    /**
     * Verifica disponibilidade do servidor remoto.
     * Fonte única: findAtivo() — MESMO método que a UI usa.
     */
    public DisponibilidadeRemota verificarDisponibilidade() throws SQLException {
        return configRepository.findAtivo()
                .map(cfg -> isOnline(cfg) ? DisponibilidadeRemota.ONLINE : DisponibilidadeRemota.OFFLINE)
                .orElse(DisponibilidadeRemota.SEM_CONFIG);
    }

    /**
     * Config ativa no SQLite — fonte única da decisão de sync.
     */
    public ConfigServidorRemoto configAtiva() throws SQLException {
        return configRepository.findAtivo()
                .orElseThrow(() -> new IllegalStateException("Nenhuma configuração de servidor ativa"));
    }

    /**
     * TCP connect com timeout — mais confiável que ICMP.
     */
    public boolean isOnline(ConfigServidorRemoto cfg) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(cfg.host(), Integer.parseInt(cfg.porta())), TCP_TIMEOUT_MS);
            return true;
        } catch (IOException | NumberFormatException e) {
            LOGGER.log(System.Logger.Level.WARNING, "Servidor inacessível: {0}:{1} — {2}",
                    cfg.host(), cfg.porta(), e.getMessage());
            return false;
        }
    }

    /**
     * Conexão JDBC com o remoto. Reusa gerarUrlConexao() do record.
     */
    public Connection getConnection() throws SQLException {
        ConfigServidorRemoto cfg = configAtiva();
        return DriverManager.getConnection(cfg.gerarUrlConexao(), cfg.usuario(), cfg.senha());
    }
}