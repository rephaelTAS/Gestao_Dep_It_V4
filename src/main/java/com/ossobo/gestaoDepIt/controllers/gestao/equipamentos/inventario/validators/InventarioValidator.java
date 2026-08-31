package com.ossobo.gestaoDepIt.controllers.gestao.equipamentos.inventario.validators;

import com.ossobo.winterfx.anotations.Inject;
import com.ossobo.winterfx.anotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ossobo.gestaoDepIt.db.services.CatalogoProdutosService;
import com.ossobo.gestaoDepIt.db.services.FuncionariosService;
import com.ossobo.gestaoDepIt.db.services.InventarioEquipamentosService;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Validador especializado para regras de negócio do inventário
 * Princípio: "Validação é uma responsabilidade separada"
 */
@Service
public class InventarioValidator {

    private static final Logger logger = LoggerFactory.getLogger(InventarioValidator.class);

    @Inject
    private InventarioEquipamentosService inventarioService;
    @Inject
    private CatalogoProdutosService catalogoService;
    @Inject
    private FuncionariosService funcionariosService;

    // =====VALIDAÇÃO DE CRIAÇÃO=====
    public InventarioValidator.ValidationResult validarCriacao(String numeroSerie, String enderecoMac,
                                                               String skuProduto, String funcionarioId) throws SQLException {

        InventarioValidator.ValidationResult result = new InventarioValidator.ValidationResult();

        // 1. Validação de unicidade
        if (inventarioService.numeroSerieExiste(numeroSerie)) {
            result.addError("numeroSerie",
                    "Número de série já existe: " + numeroSerie);
        }

        if (inventarioService.macAddressExiste(enderecoMac)) {
            result.addError("enderecoMac",
                    "Endereço MAC já existe: " + enderecoMac);
        }

        // 2. Validação de referências
        if (!catalogoService.produtoExiste(skuProduto)) {
            result.addError("skuProduto",
                    "SKU não encontrado no catálogo: " + skuProduto);
        }

        if (!funcionariosService.funcionarioExiste(funcionarioId)) {
            result.addError("funcionarioId",
                    "Funcionário não encontrado: " + funcionarioId);
        }

        // 3. Validação de formato MAC
        if (!isValidMacAddress(enderecoMac)) {
            result.addError("enderecoMac",
                    "Formato de endereço MAC inválido: " + enderecoMac);
        }

        logger.debug("Validação de criação: {} erros encontrados", result.getErrorCount());
        return result;
    }

    // =====VALIDAÇÃO DE ATUALIZAÇÃO=====
    public InventarioValidator.ValidationResult validarAtualizacao(Long equipamentoId, String novoMac,
                                                                   String novoFuncionarioId) throws SQLException {

        InventarioValidator.ValidationResult result = new InventarioValidator.ValidationResult();

        // 1. Validação de existência
        if (!inventarioService.equipamentoExiste(equipamentoId)) {
            result.addError("equipamento", "Equipamento não encontrado: " + equipamentoId);
            return result; // Falha rápida
        }

        // 2. Validação de unicidade do novo MAC
        if (novoMac != null && inventarioService.macAddressExiste(novoMac)) {
            // Verificar se o MAC pertence ao mesmo equipamento
            inventarioService.buscarPorMacAddress(novoMac).ifPresent(existente -> {
                if (!existente.getId().equals(equipamentoId)) {
                    result.addError("enderecoMac",
                            "Endereço MAC já usado por outro equipamento: " + novoMac);
                }
            });
        }

        // 3. Validação de funcionário
        if (novoFuncionarioId != null && !funcionariosService.funcionarioExiste(novoFuncionarioId)) {
            result.addError("funcionarioId",
                    "Funcionário não encontrado: " + novoFuncionarioId);
        }

        return result;
    }

    // =====VALIDAÇÃO DE MAC ADDRESS=====
    private boolean isValidMacAddress(String mac) {
        if (mac == null) return false;

        // Formato: XX:XX:XX:XX:XX:XX ou XX-XX-XX-XX-XX-XX
        String macRegex = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$";
        return mac.matches(macRegex);
    }

    // =====CLASSE DE RESULTADO DE VALIDAÇÃO=====
    public static class ValidationResult {
        private final Map<String, String> errors = new HashMap<>();
        private boolean isValid = true;

        public void addError(String field, String message) {
            errors.put(field, message);
            isValid = false;
        }

        public boolean isValid() {
            return isValid;
        }

        public int getErrorCount() {
            return errors.size();
        }

        public Map<String, String> getErrors() {
            return Collections.unmodifiableMap(errors);
        }

        public String getFormattedErrors() {
            if (isValid) return "Validação OK";

            StringBuilder sb = new StringBuilder("Erros de validação:\n");
            errors.forEach((field, message) ->
                    sb.append(String.format("  • %s: %s\n", field, message)));

            return sb.toString();
        }

        public void throwIfInvalid() {
            if (!isValid) {
                throw new IllegalArgumentException(getFormattedErrors());
            }
        }
    }
}
