package com.ossobo.gestaoDepIt.utils.gerarCodDep;


import com.ossobo.winterfx.anotations.*;
import com.ossobo.winterfx.router.model.ResponseData;

import java.sql.SQLException;

/**
 * GerarCodDepRoutes v1.0
 *
 * Fronteira de Internal Routing para geração de cod_dep.
 */
@Component
@RequestMapping("gerar-coddep")
public class GerarCodDepRoutes {

    private static final System.Logger logger =
            System.getLogger(GerarCodDepRoutes.class.getName());

    @Inject
    private GerarCodDepService service;

    @GetMapping("proximo")
    public ResponseData proximo(@RouteVar("departamento") String departamento) {
        try {
            String cod = service.gerar(departamento);
            return ResponseData.success().withData("coddep", cod);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseData.error(e.getMessage()).withError("negocio", e.getMessage());
        } catch (SQLException e) {
            logger.log(System.Logger.Level.ERROR,
                    "❌ Erro ao gerar cod_dep: {0}", e.getMessage(), e);
            return ResponseData.error("Erro ao gerar cod_dep: " + e.getMessage())
                    .withError("banco", e.getMessage());
        }
    }
}