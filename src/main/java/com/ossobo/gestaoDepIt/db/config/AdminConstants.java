package com.ossobo.gestaoDepIt.db.config;

/**
 * AdminConstants v1.0
 *
 * IDs fixos do usuário administrador padrão — fonte única entre o seed
 * do DatabaseInitializer e o login (mesmo padrão de GuestConstants).
 *
 * ⚠️ A conta ADMIN nasce com senha fixa (DEFAULT_PASSWORD) e existe apenas
 *    para o primeiro boot após formatação. Deve ser desativada/alterada
 *    assim que um administrador real for criado.
 */
public final class AdminConstants {

    private AdminConstants() {}  // Não instanciável

    // ===== Funcionário ADMIN =====
    public static final String ADMIN_FUNC_ID   = "ADMIN";
    public static final String ADMIN_FUNC_NAME = "ADM";
    public static final String ADMIN_DEPARTMENT = "TI";
    public static final String ADMIN_ROLE      = "Administrador";

    // ===== Usuário ADMIN =====
    public static final String ADMIN_ID    = "ADMIN";
    public static final String ADMIN_NAME  = "ADM";
    public static final String ADMIN_EMAIL = "admin@local";
    public static final String ADMIN_NIVEL = "ADMIN";

    /**
     * Senha padrão — TROCAR EM PRODUÇÃO.
     * A primeira conta real criada deve desativar/alterar este usuário.
     */
    public static final String DEFAULT_PASSWORD = "admin";
}