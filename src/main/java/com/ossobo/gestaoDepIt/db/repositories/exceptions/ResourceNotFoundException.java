package com.ossobo.gestaoDepIt.db.repositories.exceptions;

/**
 * Exceção personalizada para indicar que um recurso específico não foi encontrado.
 *
 * Estende {@code RuntimeException}, o que significa que é uma exceção não verificada.
 * Isso evita a necessidade de declarar a exceção em assinaturas de métodos
 * e permite um tratamento mais flexível, comum em camadas de serviço
 * e API RESTful, onde a ausência de um recurso é uma condição de erro previsível
 * mas que não exige captura obrigatória.
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Construtor que cria uma nova exceção {@code ResourceNotFoundException}
     * com uma mensagem detalhada.
     *
     * @param message A mensagem de erro que descreve o recurso não encontrado.
     * Ex: "Funcionário com ID 123 não encontrado."
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Construtor que cria uma nova exceção {@code ResourceNotFoundException}
     * com uma mensagem detalhada e a causa raiz.
     *
     * Este construtor é útil quando a não localização do recurso é consequência
     * de outra exceção de baixo nível (por exemplo, um erro na consulta SQL
     * que, indiretamente, levou à conclusão de que o recurso não existia).
     *
     * @param message A mensagem de erro que descreve o recurso não encontrado.
     * @param cause A exceção original ({@code Throwable}) que é a causa raiz
     * deste erro de "recurso não encontrado".
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}