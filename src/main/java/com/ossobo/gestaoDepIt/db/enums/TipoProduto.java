package com.ossobo.gestaoDepIt.db.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ✅ ENUM ÚNICO para Tipo de Produto
 * Combina funcionalidades de ambos os ENUMs anteriores
 * Compatível com MySQL ENUM('EQUIPAMENTO','CONSUMIVEL','TONER','ACESSORIO','SOFTWARE')
 */
public enum TipoProduto {
    EQUIPAMENTO("Equipamento"),
    CONSUMIVEL("Consumível"),
    TONER("Toner"),
    ACESSORIO("Acessório"),
    SOFTWARE("Software");

    private final String descricao;

    private TipoProduto(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getDatabaseValue() {
        return this.name(); // "EQUIPAMENTO", "CONSUMIVEL", etc.
    }

    @Override
    public String toString() {
        return descricao;
    }

    // ===== MÉTODOS UTILITÁRIOS (do TipoProdutoEnum) =====

    /**
     * Converter de String para Enum com fallback flexível
     */
    public static TipoProduto fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        // Tentar match exato
        try {
            return TipoProduto.valueOf(value.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            // Fallback: mapeamento flexível
            return mapFlexible(value);
        }
    }

    private static TipoProduto mapFlexible(String value) {
        String normalized = value.toLowerCase().trim();

        return switch (normalized) {
            case "equipamento", "equip", "hardware" -> EQUIPAMENTO;
            case "consumivel", "consumível", "material" -> CONSUMIVEL;
            case "toner", "cartucho" -> TONER;
            case "acessorio", "acessório", "periférico" -> ACESSORIO;
            case "software", "programa", "licença" -> SOFTWARE;
            default -> throw new IllegalArgumentException(
                    "Tipo de produto inválido: " + value +
                            ". Valores válidos: " + Arrays.toString(values())
            );
        };
    }

    /**
     * Validar se um valor é aceitável
     */
    public static boolean isValid(String value) {
        try {
            fromString(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Lista de valores válidos para o banco
     */
    public static List<String> getValidValues() {
        return Arrays.stream(values())
                .map(TipoProduto::getDatabaseValue)
                .collect(Collectors.toList());
    }

    /**
     * Lista de descrições para exibição
     */
    public static List<String> getDisplayNames() {
        return Arrays.stream(values())
                .map(TipoProduto::getDescricao)
                .collect(Collectors.toList());
    }
}