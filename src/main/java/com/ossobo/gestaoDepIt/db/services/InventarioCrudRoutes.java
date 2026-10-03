package com.ossobo.gestaoDepIt.db.services;

import com.ossobo.gestaoDepIt.db.models.InventarioEquipamentos;
import com.ossobo.winterfx.anotations.*;
import com.ossobo.winterfx.router.model.ResponseData;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * InventarioCrudRoutes v1.0
 *
 * Responsabilidade: Fronteira de Internal Routing das MUTAÇÕES do inventário
 *                   de equipamentos + histórico de eventos.
 *
 * Contrato: cada rota é UMA operação transacional completa — grava as duas
 *           tabelas (inventário + histórico) ou nenhuma. Handlers FINOS:
 *           delegam ao InventarioCrudService e traduzem exceção → ResponseData.
 *
 * Fronteira única de mutação: as rotas de consulta continuam em
 * inventario-equipamentos/service/*; as mutações vivem só aqui.
 *
 * Contratos de payload:
 *   Cadastrar/Atualizar  : "equipamento" (InventarioEquipamentos) +
 *                          "funcionarioid" (executor) +
 *                          "descricao" (opcional, JSON do funcionário)
 *   Demais operações     : "id" (UUID) + "funcionarioid" (executor) +
 *                          "descricao" (opcional) + campos específicos
 *
 * @since v1.0
 */
@Component
@RequestMapping("inventario-crud/service")
public class InventarioCrudRoutes {

    private static final System.Logger logger =
            System.getLogger(InventarioCrudRoutes.class.getName());

    @Inject
    private InventarioCrudService service;

    // ============================================================
    // CRUD — CADASTRAR / ATUALIZAR / EXCLUIR
    // ============================================================

    @PutMapping("cadastrar")
    public ResponseData cadastrar(@Payload("equipamento") InventarioEquipamentos equip,
                                  @RouteVar("funcionarioid") String funcionarioId,
                                  @RouteVar("descricao") String descricao) {
        return escrita(() -> service.cadastrar(equip, funcionarioId, descricao), "equipamento");
    }

    @PutMapping("atualizar")
    public ResponseData atualizar(@Payload("equipamento") InventarioEquipamentos equip,
                                  @RouteVar("funcionarioid") String funcionarioId,
                                  @RouteVar("descricao") String descricao) {
        return escrita(() -> service.atualizar(equip, funcionarioId, descricao), "equipamento");
    }

    @DeleteMapping("excluir")
    public ResponseData excluir(@RouteVar("id") String id,
                                @RouteVar("funcionarioid") String funcionarioId,
                                @RouteVar("descricao") String descricao) {
        return acao("Equipamento excluído", () -> service.excluir(id, funcionarioId, descricao));
    }

    // ============================================================
    // CICLO DE VIDA
    // ============================================================

    @PutMapping("baixar")
    public ResponseData baixar(@RouteVar("id") String id,
                               @RouteVar("funcionarioid") String funcionarioId,
                               @RouteVar("motivo") String motivo,
                               @RouteVar("descricao") String descricao) {
        return acao("Equipamento baixado",
                () -> service.baixar(id, funcionarioId, motivo, descricao));
    }

    @PutMapping("manutencao/enviar")
    public ResponseData enviarManutencao(@RouteVar("id") String id,
                                         @RouteVar("funcionarioid") String funcionarioId,
                                         @RouteVar("motivo") String motivo,
                                         @RouteVar("descricao") String descricao) {
        return acao("Equipamento enviado para manutenção",
                () -> service.enviarManutencao(id, funcionarioId, motivo, descricao));
    }

    @PutMapping("manutencao/retornar")
    public ResponseData retornarManutencao(@RouteVar("id") String id,
                                           @RouteVar("funcionarioid") String funcionarioId,
                                           @RouteVar("condicaopos") String condicaoPos,
                                           @RouteVar("observacoes") String observacoes,
                                           @RouteVar("descricao") String descricao) {
        return acao("Equipamento retornou da manutenção",
                () -> service.retornarManutencao(id, funcionarioId, condicaoPos, observacoes, descricao));
    }

    // ============================================================
    // LOCALIZAÇÃO / FUNCIONÁRIO
    // ============================================================

    @PutMapping("localizacao/transferir")
    public ResponseData transferirLocalizacao(@RouteVar("id") String id,
                                              @RouteVar("funcionarioid") String funcionarioId,
                                              @RouteVar("localizacao") String localizacao,
                                              @RouteVar("departamento") String departamento,
                                              @RouteVar("descricao") String descricao) {
        return acao("Localização alterada",
                () -> service.transferirLocalizacao(id, funcionarioId, localizacao, departamento, descricao));
    }

    @PutMapping("funcionario/reassociar")
    public ResponseData reassociarFuncionario(@RouteVar("id") String id,
                                              @RouteVar("funcionarioid") String funcionarioExecutor,
                                              @RouteVar("novofuncionarioid") String novoFuncionarioId,
                                              @RouteVar("descricao") String descricao) {
        return acao("Responsável atualizado",
                () -> service.reassociarFuncionario(id, funcionarioExecutor, novoFuncionarioId, descricao));
    }

    // ============================================================
    // VERIFICAÇÃO
    // ============================================================

    @PutMapping("verificacao/registrar")
    public ResponseData registrarVerificacao(@RouteVar("id") String id,
                                             @RouteVar("funcionarioid") String funcionarioId,
                                             @RouteVar("data") String data,
                                             @RouteVar("condicao") String condicao,
                                             @RouteVar("observacoes") String observacoes,
                                             @RouteVar("descricao") String descricao) {
        return acao("Verificação registrada", () ->
                service.registrarVerificacao(id, funcionarioId,
                        converterDataOpcional(data, "data"), condicao, observacoes, descricao));
    }

    // ============================================================
    // STATUS / CONDIÇÃO
    // ============================================================

    @PutMapping("status/atualizar")
    public ResponseData atualizarStatus(@RouteVar("id") String id,
                                        @RouteVar("funcionarioid") String funcionarioId,
                                        @RouteVar("valor") String novoStatus,
                                        @RouteVar("descricao") String descricao) {
        return acao("Status alterado",
                () -> service.atualizarStatus(id, funcionarioId, novoStatus, descricao));
    }

    @PutMapping("condicao/atualizar")
    public ResponseData atualizarCondicao(@RouteVar("id") String id,
                                          @RouteVar("funcionarioid") String funcionarioId,
                                          @RouteVar("valor") String novaCondicao,
                                          @RouteVar("descricao") String descricao) {
        return acao("Condição alterada",
                () -> service.atualizarCondicao(id, funcionarioId, novaCondicao, descricao));
    }

    // ============================================================
    // FATURA / DOCUMENTOS / DEVOLUÇÃO
    // ============================================================

    @PutMapping("fatura/atualizar")
    public ResponseData atualizarFatura(@RouteVar("id") String id,
                                        @RouteVar("funcionarioid") String funcionarioId,
                                        @RouteVar("numero") String numeroFatura,
                                        @RouteVar("descricao") String descricao) {
        return acao("Fatura atualizada",
                () -> service.atualizarFatura(id, funcionarioId, numeroFatura, descricao));
    }

    @PutMapping("documento/entrega/atualizar")
    public ResponseData atualizarDocEntrega(@RouteVar("id") String id,
                                            @RouteVar("funcionarioid") String funcionarioId,
                                            @Payload("documento") byte[] documento,
                                            @RouteVar("descricao") String descricao) {
        return acao("Documento de entrega atualizado",
                () -> service.atualizarDocumentoEntrega(id, funcionarioId, documento, descricao));
    }

    @PutMapping("documento/devolucao/atualizar")
    public ResponseData atualizarDocDevolucao(@RouteVar("id") String id,
                                              @RouteVar("funcionarioid") String funcionarioId,
                                              @Payload("documento") byte[] documento,
                                              @RouteVar("descricao") String descricao) {
        return acao("Documento de devolução atualizado",
                () -> service.atualizarDocumentoDevolucao(id, funcionarioId, documento, descricao));
    }

    @PutMapping("devolucao/atualizar")
    public ResponseData atualizarDevolucao(@RouteVar("id") String id,
                                           @RouteVar("funcionarioid") String funcionarioId,
                                           @RouteVar("ativo") Boolean devolucao,
                                           @RouteVar("descricao") String descricao) {
        if (devolucao == null) {
            throw new IllegalArgumentException("Chave 'ativo' é obrigatória (true/false)");
        }
        return acao("Devolução atualizada",
                () -> service.atualizarDevolucao(id, funcionarioId, devolucao, descricao));
    }

    // ============================================================
    // HELPERS DE FRONTEIRA
    // ============================================================

    @FunctionalInterface
    private interface Acao<T> { T executar() throws SQLException; }

    @FunctionalInterface
    private interface VoidAcao { void executar() throws SQLException; }

    private ResponseData acao(String mensagem, VoidAcao acao) {
        try {
            acao.executar();
            return ResponseData.success().withMessage(mensagem);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return erroNegocio(e);
        } catch (SQLException e) {
            return erroBanco(e);
        }
    }

    private <T> ResponseData escrita(Acao<T> acao, String chave) {
        try {
            T resultado = acao.executar();
            return ResponseData.success().withData(chave, resultado);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return erroNegocio(e);
        } catch (SQLException e) {
            return erroBanco(e);
        }
    }

    private ResponseData erroNegocio(RuntimeException e) {
        logger.log(System.Logger.Level.WARNING, "⚠️ Regra de negócio violada: {}", e.getMessage());
        return ResponseData.error(e.getMessage()).withError("negocio", e.getMessage());
    }

    private ResponseData erroBanco(SQLException e) {
        logger.log(System.Logger.Level.ERROR, "❌ Erro de banco de dados: {}", e.getMessage(), e);
        return ResponseData.error("Erro de banco de dados: " + e.getMessage())
                .withError("banco", e.getMessage());
    }

    private LocalDate converterDataOpcional(String bruto, String campo) {
        if (bruto == null || bruto.isBlank()) return null;
        try {
            return LocalDate.parse(bruto.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Data inválida em '" + campo + "': '" + bruto + "' (use yyyy-MM-dd)");
        }
    }
}