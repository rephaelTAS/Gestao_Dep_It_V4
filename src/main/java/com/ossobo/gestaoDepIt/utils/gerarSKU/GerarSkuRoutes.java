package com.ossobo.gestaoDepIt.utils.gerarSKU;

import com.ossobo.winterfx.anotations.*;
import com.ossobo.winterfx.router.model.ResponseData;

import java.sql.SQLException;

/**
 * GerarSkuRoutes v1.0
 *
 * Fronteira de Internal Routing para geração de SKU.
 * Substitui o antigo `GerarIdProduto` (mantido em paralelo para
 * compatibilidade reversa; depreciar após migração completa).
 */
@Component
@RequestMapping("gerarIdProduto")
public class GerarSkuRoutes {

    private static final System.Logger logger =
            System.getLogger(GerarSkuRoutes.class.getName());

    @Inject
    private GerarSkuService service;

    @GetMapping("gerarIdProdutoForm")
    public ResponseData gerarIdProduto(@RouteVar("categoria") String categoria,
                                       @RouteVar("marca") String marca,
                                       @RouteVar("modelo") String modelo) {
        try {
            String sku = service.gerar(categoria, marca, modelo);
            return ResponseData.success().withData("idproduto", sku);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseData.error(e.getMessage()).withError("negocio", e.getMessage());
        } catch (SQLException e) {
            logger.log(System.Logger.Level.ERROR,
                    "❌ Erro de banco ao gerar SKU: {0}", e.getMessage(), e);
            return ResponseData.error("Erro ao gerar SKU: " + e.getMessage())
                    .withError("banco", e.getMessage());
        }
    }

    /** Métodos utilitários expostos como leitura (opcional). */
    @GetMapping("categorias-conhecidas")
    public ResponseData categorias() {
        return ResponseData.success().withData("categorias", SkuCatalogo.categoriasConhecidas());
    }
}