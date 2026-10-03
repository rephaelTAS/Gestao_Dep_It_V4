package com.ossobo.gestaoDepIt.db.config;

/**
 * IDs fixos do sistema — fonte única para evitar divergências entre seed e login.
 * NÃO contém dependências de DI/Rotas para evitar ciclos no boot.
 */
public final class GuestConstants {

    private GuestConstants() {}  // Não instanciável

    // IDs
    public static final String GUEST_ID = "GUEST";
    public static final String GUEST_FUNC_ID = "GUEST_FUNC";

    // Dados do funcionário
    public static final String GUEST_FUNC_NAME = "Conta Guest";
    public static final String GUEST_DEPARTMENT = "Sistema";
    public static final String GUEST_ROLE = "Convidado";

    // Dados do usuário
    public static final String GUEST_NAME = "Guest";
    public static final String GUEST_EMAIL = "guest@localhost";
    public static final String GUEST_LEVEL = "READONLY";
}