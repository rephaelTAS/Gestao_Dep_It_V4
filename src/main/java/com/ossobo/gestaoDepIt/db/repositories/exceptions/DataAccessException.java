// File: packt/database/exceptions/DataAccessException.java
package com.ossobo.gestaoDepIt.db.repositories.exceptions;

/**
 * Exceção personalizada para encapsular erros de acesso a dados.
 * Estende {@code RuntimeException} para evitar a necessidade de declaração 'throws'
 * em cada método da camada de serviço, simplificando as assinaturas dos métodos.
 * Permite encapsular exceções de infraestrutura (como SQLException)
 * em uma exceção de domínio mais específica.
 */
public class DataAccessException extends RuntimeException {

    /**
     * Construtor que aceita uma mensagem de erro detalhada.
     * @param message A mensagem de erro que descreve a causa do problema de acesso a dados.
     */
    public DataAccessException(String message) {
        super(message);
    }

    /**
     * Construtor que aceita uma mensagem de erro detalhada e a causa original
     * (a exceção de baixo nível que provocou este erro, por exemplo, uma SQLException).
     *
     * @param message A mensagem de erro que descreve a causa do problema de acesso a dados.
     * @param cause A exceção original (Throwable) que é a causa raiz deste erro.
     */
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}