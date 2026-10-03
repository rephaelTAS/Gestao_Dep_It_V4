package com.ossobo.gestaoDepIt.db.services;

import com.ossobo.gestaoDepIt.db.models.Funcionarios;
import com.ossobo.winterfx.anotations.*;
import com.ossobo.winterfx.router.model.ResponseData;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * FuncionariosRoutes v1.1
 *
 * Responsabilidade: Fronteira de Internal Routing dos funcionários.
 *                   Handlers FINOS: delegam ao FuncionariosService e traduzem
 *                   exceção → ResponseData.
 *
 * Contratos de payload (documentação viva da API):
 *   Identidade sempre pela chave "coddep" (PK String).
 *   CRUD        : "funcionario" (payload Funcionarios).
 *   Imagem      : PUT exige "imagem" (byte[]) + "tipo"; GET devolve "imagem"
 *                 (byte[] ou null); DELETE remove. Chave "temimagem" (boolean).
 *
 * v1.0 - Criação; 37 rotas (GET 28 / PUT 7 / DELETE 2); status dividido em
 *        rotas explícitas; imagem exposta nos três canais aplicáveis.
 * v1.1 - Alinhado ao FuncionariosService v2.2 (fonte da verdade = Repository
 *        v2.2). Removidas 18 rotas cujos métodos do Service foram suprimidos
 *        por dependerem de consultas/agregações não oferecidas pelo repositório.
 *        Total atual: 19 rotas.
 */
@Component
@RequestMapping("funcionarios/service")
public class FuncionariosRoutes {

    private static final System.Logger logger = System.getLogger(FuncionariosRoutes.class.getName());

    @Inject
    private FuncionariosService service;

    // ============================================================
    // ROTAS — CONSULTAS BÁSICAS (GET)
    // ============================================================

    /** Todos os funcionários. */
    @GetMapping("todos")
    public ResponseData todos() {
        return lista(service::listarTodos, "funcionarios");
    }

    /** Lista paginada. Chaves: pagina (1-based), tamanho. */
    @GetMapping("paginados")
    public ResponseData paginados(@RouteVar("pagina") Integer pagina,
                                  @RouteVar("tamanho") Integer tamanho) {
        return lista(() -> {
            exigirPaginacao(pagina, tamanho);
            return service.listarTodos(pagina, tamanho);
        }, "funcionarios");
    }

    /** Lista paginada apenas ativos. Chaves: pagina, tamanho. */
    @GetMapping("ativos/paginados")
    public ResponseData ativosPaginados(@RouteVar("pagina") Integer pagina,
                                        @RouteVar("tamanho") Integer tamanho) {
        return lista(() -> {
            exigirPaginacao(pagina, tamanho);
            return service.listarAtivos(pagina, tamanho);
        }, "funcionarios");
    }

    /** Busca por código. Erro semântico se inexistente. */
    @GetMapping("por/coddep")
    public ResponseData buscarPorCodDep(@RouteVar("coddep") String codDep) {
        return optional(() -> service.buscarPorCodDep(codDep), "funcionario",
                "Funcionário não encontrado: " + codDep);
    }

    /** Busca por email. Erro semântico se inexistente. */
    @GetMapping("por/email")
    public ResponseData buscarPorEmail(@RouteVar("email") String email) {
        return optional(() -> service.buscarPorEmail(email), "funcionario",
                "Funcionário não encontrado: " + email);
    }

    // ============================================================
    // ROTAS — IMAGEM DE PERFIL (GET / PUT / DELETE com paths distintos)
    // ============================================================

    /** Devolve os bytes da imagem (null se não houver). */
    @GetMapping("imagem/por/coddep")
    public ResponseData imagem(@RouteVar("coddep") String codDep) {
        try {
            byte[] dados = service.obterImagemPerfil(codDep).orElse(null);
            return ResponseData.success()
                    .withData("imagem", dados)
                    .withData("presente", dados != null);
        } catch (SQLException e) {
            return erroBanco(e);
        }
    }

    /** true se o funcionário possui imagem gravada. */
    @GetMapping("imagem/existe")
    public ResponseData temImagem(@RouteVar("coddep") String codDep) {
        return valor(() -> service.temImagemPerfil(codDep), "temimagem");
    }

    /**
     * Transfere o funcionário para outro departamento.
     * Operação composta (funcionário + inventário + histórico) atômica.
     *
     * Chaves:
     *   "coddep"        — cod_dep atual do funcionário
     *   "departamento"  — novo departamento
     *   "funcionarioid" — cod_dep do executor (auditoria)
     *   "descricao"     — JSON opcional
     */
    @PutMapping("transferir-departamento")
    public ResponseData transferirDepartamento(@RouteVar("coddep") String codDep,
                                               @RouteVar("departamento") String novoDepartamento,
                                               @RouteVar("funcionarioid") String codDepExecutor,
                                               @RouteVar("descricao") String descricao) {
        return escrita(() -> {
            String novoCod = service.transferirDepartamento(
                    codDep, novoDepartamento, codDepExecutor, descricao);
            return novoCod;
        }, "novoCodDep");
    }

    /**
     * Grava/substitui a imagem. Obrigatórios: "coddep", "imagem" (byte[]),
     * "tipo" (png/jpeg/jpg... validados pelo domínio).
     */
    @PutMapping("imagem/atualizar")
    public ResponseData atualizarImagem(@RouteVar("coddep") String codDep,
                                        @Payload("imagem") byte[] imagem,
                                        @RouteVar("tipo") String tipo) {
        return escrita(() -> {
            service.atualizarImagemPerfil(codDep, imagem, tipo);
            return "Imagem atualizada";
        }, "mensagem");
    }

    /**
     * Remove a imagem de perfil.
     * Path renomeado para evitar colisão com GET /imagem/por/coddep
     */
    @DeleteMapping("imagem/remover/por/coddep")
    public ResponseData removerImagem(@RouteVar("coddep") String codDep) {
        return escrita(() -> {
            service.removerImagemPerfil(codDep);
            return "Imagem removida";
        }, "mensagem");
    }

    // ============================================================
    // ROTAS — ESTATÍSTICAS (GET)
    // ============================================================

    @GetMapping("total")
    public ResponseData total()                 { return valor(service::contarTotal, "total"); }

    @GetMapping("total/ativos")
    public ResponseData totalAtivos()           { return valor(service::contarAtivos, "total"); }

    // ============================================================
    // ROTAS — VERIFICAÇÕES (GET)
    // ============================================================

    @GetMapping("exists/coddep")
    public ResponseData existeCodDep(@RouteVar("coddep") String codDep) {
        return valor(() -> service.existePorCodDep(codDep), "exists");
    }

    @GetMapping("exists/email")
    public ResponseData existeEmail(@RouteVar("email") String email) {
        return valor(() -> service.existePorEmail(email), "exists");
    }

    /** Checagem rápida de estado ativo. */
    @GetMapping("ativo/check")
    public ResponseData isAtivo(@RouteVar("coddep") String codDep) {
        return valor(() -> service.isAtivo(codDep), "ativo");
    }

    // ============================================================
    // ROTAS — MUTAÇÕES (PUT)
    // ============================================================

    /** Cria funcionário. Payload: "funcionario". Publica FuncionarioEvent CRIADO. */
    @PutMapping("criar")
    public ResponseData criar(@Payload("funcionario") Funcionarios funcionario) {
        return escrita(() -> service.criar(funcionario), "funcionario");
    }

    /** Atualiza completo. Payload: "funcionario". */
    @PutMapping("atualizar")
    public ResponseData atualizar(@Payload("funcionario") Funcionarios funcionario) {
        return escrita(() -> service.atualizar(funcionario), "funcionario");
    }

    /** Ativa funcionário. */
    @PutMapping("ativar")
    public ResponseData ativar(@RouteVar("coddep") String codDep) {
        return escrita(() -> { service.ativar(codDep); return "Funcionário ativado"; }, "mensagem");
    }

    /** Desativa funcionário (soft state). */
    @PutMapping("desativar")
    public ResponseData desativar(@RouteVar("coddep") String codDep) {
        return escrita(() -> { service.desativar(codDep); return "Funcionário desativado"; }, "mensagem");
    }

    // ============================================================
    // ROTAS — REMOÇÃO (DELETE)
    // ============================================================

    /**
     * ⚠️ EXCLUSÃO FÍSICA. Política em análise (backlog OBS-F1):
     * histórico de estoque referencia este codDep. Prefira "desativar".
     */
    @DeleteMapping("deletar/por/coddep")
    public ResponseData excluir(@RouteVar("coddep") String codDep) {
        return escrita(() -> {
            service.excluir(codDep);
            return "Funcionário excluído";
        }, "mensagem");
    }

    // ============================================================
    // HELPERS DE FRONTEIRA (padrão unificado das classes Routes)
    // ============================================================

    @FunctionalInterface
    private interface Acao<T> {
        T executar() throws SQLException;
    }

    private <T> ResponseData escrita(Acao<T> acao, String chave) {
        try {
            return ResponseData.success().withData(chave, acao.executar());
        } catch (IllegalArgumentException | IllegalStateException e) {
            logger.log(System.Logger.Level.WARNING,"⚠️ Regra de negócio violada: {}", e.getMessage());
            return ResponseData.error(e.getMessage()).withError("negocio", e.getMessage());
        } catch (SQLException e) {
            return erroBanco(e);
        }
    }

    private ResponseData lista(Acao<List<?>> acao, String chave) {
        try {
            List<?> dados = acao.executar();
            return ResponseData.success().withData(chave, dados).withData("total", dados.size());
        } catch (SQLException e) {
            return erroBanco(e);
        }
    }

    private <T> ResponseData valor(Acao<T> acao, String chave) {
        try {
            return ResponseData.success().withData(chave, acao.executar());
        } catch (SQLException e) {
            return erroBanco(e);
        }
    }

    private <T> ResponseData optional(Acao<Optional<T>> acao, String chave, String msgVazio) {
        try {
            return acao.executar()
                    .<ResponseData>map(v -> ResponseData.success().withData(chave, v))
                    .orElseGet(() -> ResponseData.error(msgVazio).withError(chave, "não encontrado"));
        } catch (SQLException e) {
            return erroBanco(e);
        }
    }

    private ResponseData erroBanco(SQLException e) {
        logger.log(System.Logger.Level.ERROR,"❌ Erro de banco de dados: {}", e.getMessage(), e);
        return ResponseData.error("Erro de banco de dados: " + e.getMessage())
                .withError("banco", e.getMessage());
    }

    private void exigirPaginacao(Integer pagina, Integer tamanho) {
        if (pagina == null || pagina < 1 || tamanho == null || tamanho < 1) {
            throw new IllegalArgumentException("Página e tamanho devem ser >= 1");
        }
    }
}