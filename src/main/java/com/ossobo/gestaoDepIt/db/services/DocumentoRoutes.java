package com.ossobo.gestaoDepIt.db.services;

import com.ossobo.gestaoDepIt.db.documentos.DocumentoGerador;
import com.ossobo.gestaoDepIt.db.models.CatalogoProdutos;
import com.ossobo.gestaoDepIt.db.models.Funcionarios;
import com.ossobo.gestaoDepIt.db.models.InventarioEquipamentos;
import com.ossobo.winterfx.anotations.*;
import com.ossobo.winterfx.router.model.ResponseData;

import java.sql.SQLException;
import java.util.Optional;

/**
 * DocumentoRoutes v1.0
 *
 * Responsabilidade: fronteira de Internal Routing para geração dos PDFs de
 * termo de entrega e termo de devolução.
 *
 * Contrato:
 *   PUT documento/service/gerar-entrega    — payload "equipamento" +
 *                                            routevar "funcionarioid"
 *   PUT documento/service/gerar-devolucao  — idem
 *
 * Retorno: ResponseData com "pdf" = byte[] e "nomeArquivo" = String.
 * O controller decide onde salvar via FileChooser.
 *
 * @since v1.0
 */
@Component
@RequestMapping("documento/service")
public class DocumentoRoutes {

    private static final System.Logger logger = System.getLogger(DocumentoRoutes.class.getName());

    @Inject private DocumentoGerador gerador;
    @Inject private FuncionariosService funcionariosService;
    @Inject private CatalogoProdutosService catalogoService;

    @PutMapping("gerar-entrega")
    public ResponseData gerarEntrega(@Payload("equipamento") InventarioEquipamentos equip,
                                     @RouteVar("funcionarioid") String funcionarioId) {
        return gerar(equip, funcionarioId, true);
    }

    @PutMapping("gerar-devolucao")
    public ResponseData gerarDevolucao(@Payload("equipamento") InventarioEquipamentos equip,
                                       @RouteVar("funcionarioid") String funcionarioId) {
        return gerar(equip, funcionarioId, false);
    }

    private ResponseData gerar(InventarioEquipamentos equip, String funcionarioId, boolean entrega) {
        try {
            if (equip == null) {
                throw new IllegalArgumentException("Equipamento é obrigatório");
            }
            if (funcionarioId == null || funcionarioId.isBlank()) {
                throw new IllegalArgumentException("Funcionário responsável é obrigatório");
            }

            Funcionarios func = funcionariosService.buscarPorCodDep(funcionarioId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Funcionário não encontrado: " + funcionarioId));

            CatalogoProdutos produto = catalogoService.buscarPorSku(equip.skuProduto())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Produto não encontrado: " + equip.skuProduto()));

            byte[] pdf = entrega
                    ? gerador.gerarEntrega(equip, func, produto)
                    : gerador.gerarDevolucao(equip, func, produto);

            String nome = (entrega ? "termo-entrega-" : "termo-devolucao-")
                    + equip.numSerie() + ".pdf";

            return ResponseData.success()
                    .withMessage("PDF gerado")
                    .withData("pdf", pdf)
                    .withData("nomeArquivo", nome);

        } catch (IllegalArgumentException | IllegalStateException e) {
            logger.log(System.Logger.Level.WARNING, "⚠️ {0}", e.getMessage());
            return ResponseData.error(e.getMessage()).withError("negocio", e.getMessage());
        } catch (SQLException e) {
            logger.log(System.Logger.Level.ERROR, "❌ {0}", e.getMessage(), e);
            return ResponseData.error("Erro de banco: " + e.getMessage())
                    .withError("banco", e.getMessage());
        }
    }

    // Silencia import não usado quando Optional não aparece — mantém contrato.
    @SuppressWarnings("unused")
    private Optional<Funcionarios> _unused() { return Optional.empty(); }
}