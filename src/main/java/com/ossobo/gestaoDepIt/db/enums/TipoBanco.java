package com.ossobo.gestaoDepIt.db.enums;

/**
 * TipoBanco - Enum de tipos de banco de dados suportados
 * v1.0
 */
public enum TipoBanco {
    MYSQL("MySQL", "jdbc:mysql://%s:%s/%s"),
    MARIADB("MariaDB", "jdbc:mariadb://%s:%s/%s"),
    POSTGRESQL("PostgreSQL", "jdbc:postgresql://%s:%s/%s"),
    SQLSERVER("SQL Server", "jdbc:sqlserver://%s:%s;databaseName=%s"),
    ORACLE("Oracle", "jdbc:oracle:thin:@%s:%s:%s");

    private final String displayName;
    private final String urlTemplate;

    TipoBanco(String displayName, String urlTemplate) {
        this.displayName = displayName;
        this.urlTemplate = urlTemplate;
    }

    public String getDisplayName() { return displayName; }
    public String getUrlTemplate() { return urlTemplate; }

    public String formatUrl(String host, String porta, String database) {
        return String.format(urlTemplate, host, porta, database);
    }
}