package com.ossobo.gestaoDepIt.db.repositories.exceptions;

/**
 * Exceção lançada para indicar que uma operação de persistência (inserção ou atualização)
 * falhou devido a uma violação de restrição de unicidade no banco de dados.
 * Por exemplo, tentar inserir um registro com um nome de usuário que já existe.
 * Esta exceção estende DataAccessException para manter a hierarquia de exceções de acesso a dados.
 */
public class DuplicateEntryException extends DataAccessException {

    /**
     * Construtor para DuplicateEntryException com uma mensagem detalhada.
     *
     * @param message A mensagem detalhada (e.g., "Registro com ID duplicado.").
     */
    public DuplicateEntryException(String message) {
        super(message);
    }

    /**
     * Construtor para DuplicateEntryException com uma mensagem detalhada e a causa raiz.
     * Útil quando a exceção original do JDBC (SQLException) é a causa da duplicação.
     *
     * @param message A mensagem detalhada.
     * @param cause   A causa raiz da exceção.
     */
    public DuplicateEntryException(String message, Throwable cause) {
        super(message, cause);
    }
}