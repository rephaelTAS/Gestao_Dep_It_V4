package com.ossobo.gestaoDepIt.controllers.gestao.estoque.catalugoproduto.validators;

import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Validador independente para formulário de produto
 * Single Responsibility: Apenas validação de dados
 */
public class ProdutoFormValidator {

    private static final Logger logger = LoggerFactory.getLogger(ProdutoFormValidator.class);

    // ===== VALIDAÇÃO DE CAMPOS OBRIGATÓRIOS =====
    public Map<String, String> validarCamposObrigatorios(
            TextField skuField,
            ComboBox<String> tipoProdutoCombo,
            TextField categoriaField,
            TextField marcaField,
            TextField modeloField) {

        Map<String, String> erros = new HashMap<>();

        if (isCampoVazio(skuField)) {
            erros.put("sku", "SKU é obrigatório");
        }

        if (isComboVazio(tipoProdutoCombo)) {
            erros.put("tipoProduto", "Tipo de Produto é obrigatório");
        }

        if (isCampoVazio(categoriaField)) {
            erros.put("categoria", "Categoria é obrigatória");
        }

        if (isCampoVazio(marcaField)) {
            erros.put("marca", "Marca é obrigatória");
        }

        if (isCampoVazio(modeloField)) {
            erros.put("modelo", "Modelo é obrigatório");
        }

        return erros;
    }

    // ===== VALIDAÇÃO DE FORMATO =====
    public Map<String, String> validarFormatos(
            TextArea caracteristicasField,
            TextField corField,
            TextArea descricaoField) {

        Map<String, String> erros = new HashMap<>();

        // Validação JSON nas características
        if (!isCampoVazio(caracteristicasField)) {
            if (!validarJsonBasico(caracteristicasField.getText())) {
                erros.put("caracteristicas", "Formato JSON inválido nas características técnicas");
            }
        }

        // Validação de cor (opcional, mas se preenchida, validar)
        if (!isCampoVazio(corField)) {
            if (corField.getText().length() > 50) {
                erros.put("cor", "Cor muito longa (max 50 caracteres)");
            }
        }

        // Validação de descrição
        if (!isCampoVazio(descricaoField)) {
            if (descricaoField.getText().length() > 1000) {
                erros.put("descricao", "Descrição muito longa (max 1000 caracteres)");
            }
        }

        return erros;
    }

    // ===== MÉTODOS AUXILIARES =====
    private boolean isCampoVazio(TextInputControl campo) {
        return campo.getText() == null || campo.getText().trim().isEmpty();
    }

    private boolean isComboVazio(ComboBox<String> combo) {
        return combo.getValue() == null || combo.getValue().trim().isEmpty();
    }

    private boolean validarJsonBasico(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return true; // Campo vazio é válido
        }

        String trimmed = texto.trim();
        // Validação básica de JSON - em produção usar biblioteca como Jackson/Gson
        return trimmed.startsWith("{") && trimmed.endsWith("}");
    }
}