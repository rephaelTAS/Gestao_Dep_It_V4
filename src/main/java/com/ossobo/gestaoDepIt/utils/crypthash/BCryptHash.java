package com.ossobo.gestaoDepIt.utils.crypthash;

/*
 * BCryptHash v1.0
 *
 * Utilitário de hash de senha com BCrypt.
 * Centraliza a lógica de hash e verificação para todo o sistema.
 *
 * v1.0: Versão inicial
 */

import org.mindrot.jbcrypt.BCrypt;

public final class BCryptHash {

    // Fator de trabalho: 12 = 2^12 iterações (~200ms em hardware moderno)
    private static final int WORKLOAD = 12;

    private BCryptHash() {}

    /**
     * Gera hash BCrypt de uma senha em texto puro.
     * Ex: BCryptHash.hash("teste35") → "$2a$12$..."
     */
    public static String hash(String plainPassword) {
        String salt = BCrypt.gensalt(WORKLOAD);
        return BCrypt.hashpw(plainPassword, salt);
    }

    /**
     * Verifica se uma senha em texto puro corresponde ao hash armazenado.
     * Ex: BCryptHash.verify("teste35", hashDoBanco) → true/false
     */
    public static boolean verify(String plainPassword, String hashedPassword) {
        if (hashedPassword == null || !hashedPassword.startsWith("$2a$")) {
            return false;
        }
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}