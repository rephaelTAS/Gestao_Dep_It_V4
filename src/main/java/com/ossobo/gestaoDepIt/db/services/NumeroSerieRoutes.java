package com.ossobo.gestaoDepIt.db.services;

import com.ossobo.gestaoDepIt.db.documentos.NumeroSerieGenerator;
import com.ossobo.winterfx.anotations.*;
import com.ossobo.winterfx.router.model.ResponseData;

import java.sql.SQLException;

/**
 * NumeroSerieRoutes v1.0
 *
 * Responsabilidade: fronteira de Internal Routing para geração do próximo
 * número de série a partir de (departamento, marca).
 *
 * Contrato:
 *   PUT numserie/service/proximo
 *     routevar "departamento" — nome legível do departamento
 *     routevar "marca"        — nome legível da marca
 *   Retorno: "numeroSerie" (String) + "prefixo" (String)
 *
 * @since v1.0
 */
@Component
@RequestMapping("numserie/service")
public class NumeroSerieRoutes {

    private static final System.Logger logger = System.getLogger(NumeroSerieRoutes.class.getName());

    @Inject
    private NumeroSerieGenerator generator;

    @PutMapping("proximo")
    public ResponseData proximo(@RouteVar("departamento") String departamento,
                                @RouteVar("marca") String marca) {
        try {
            if (departamento == null || departamento.isBlank()) {
                throw new IllegalArgumentException("Departamento é obrigatório");
            }
            if (marca == null || marca.isBlank()) {
                throw new IllegalArgumentException("Marca é obrigatória");
            }
            String numero = generator.proximo(departamento, marca);
            String prefixo = numero.substring(0, numero.lastIndexOf('-') + 1);
            return ResponseData.success()
                    .withMessage("Número de série gerado")
                    .withData("numeroSerie", numero)
                    .withData("prefixo", prefixo);
        } catch (IllegalArgumentException | IllegalStateException e) {
            logger.log(System.Logger.Level.WARNING, "⚠️ {0}", e.getMessage());
            return ResponseData.error(e.getMessage()).withError("negocio", e.getMessage());
        } catch (SQLException e) {
            logger.log(System.Logger.Level.ERROR, "❌ {0}", e.getMessage(), e);
            return ResponseData.error("Erro de banco: " + e.getMessage())
                    .withError("banco", e.getMessage());
        }
    }
}