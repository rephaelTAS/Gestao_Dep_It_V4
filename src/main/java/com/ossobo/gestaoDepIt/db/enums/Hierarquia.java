package com.ossobo.gestaoDepIt.db.enums;

import java.util.Arrays;
import java.util.List;

/**
 * Hierarquia v1.0
 *
 * Fonte ÚNICA da ordem de privilégios do sistema.
 * A ordem dos valores define o poder crescente:
 *   READONLY < OPERADOR < SUPERVISOR < GESTOR < ADMIN
 *
 * Uso:
 *   - Comparação: a.temPermissao(b) → a possui nível >= b
 *   - Navegação:  .proximo() / .anterior()
 *   - Conversão:  Hierarquia.de("ADMIN") / isValido("ADMIN")
 *   - Listagem:   Hierarquia.todos() → para rotas de UI / niveis-validos
 *
 * Persistência: o nome do enum (name()) é gravado como TEXT na coluna
 * nivel_acesso. Nunca reordenar valores existentes — quebra dados gravados.
 * Adicione sempre no fim, respeitando a ordem crescente de privilégio.
 */
public enum Hierarquia {
    NIVEL_READONLY,
    NIVEL_OPERADOR,
    NIVEL_SUPERVISOR,
    NIVEL_GESTOR,
    NIVEL_ADMIN;

    /** true se este nível é igual ou superior ao requerido. */
    public boolean temPermissao(Hierarquia requerido) {
        return requerido != null && this.ordinal() >= requerido.ordinal();
    }

    /** Próximo nível acima (ou este mesmo, se já for o topo). */
    public Hierarquia proximo() {
        int idx = ordinal() + 1;
        return (idx < values().length) ? values()[idx] : this;
    }

    /** Nível anterior (ou este mesmo, se já for o mínimo). */
    public Hierarquia anterior() {
        int idx = ordinal() - 1;
        return (idx >= 0) ? values()[idx] : this;
    }

    public boolean isTopo() {
        return ordinal() == values().length - 1;
    }

    public boolean isMinimo() {
        return ordinal() == 0;
    }

    /** Converte texto (case-insensitive) em Hierarquia ou lança IllegalArgumentException. */
    public static Hierarquia de(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nível de acesso é obrigatório");
        }
        String alvo = nome.trim().toUpperCase();
        for (Hierarquia h : values()) {
            if (h.name().equals(alvo)) return h;
        }
        throw new IllegalArgumentException("Nível de acesso inválido: " + nome);
    }

    /** true se o texto (case-insensitive) corresponde a um nível válido. */
    public static boolean isValido(String nome) {
        if (nome == null || nome.isBlank()) return false;
        String alvo = nome.trim().toUpperCase();
        for (Hierarquia h : values()) {
            if (h.name().equals(alvo)) return true;
        }
        return false;
    }

    /** Lista imutável dos nomes (ordem crescente de privilégio). */
    public static List<String> todos() {
        return Arrays.stream(values()).map(Enum::name).toList();
    }
}