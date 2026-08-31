package com.ossobo.gestaoDepIt.db.services;

import com.ossobo.gestaoDepIt.db.models.Funcionarios;
import com.ossobo.winterfx.anotations.Component;
import com.ossobo.winterfx.anotations.DeleteMapping;
import com.ossobo.winterfx.anotations.GetMapping;
import com.ossobo.winterfx.anotations.Inject;
import com.ossobo.winterfx.anotations.Payload;
import com.ossobo.winterfx.anotations.PutMapping;
import com.ossobo.winterfx.anotations.RequestMapping;
import com.ossobo.winterfx.anotations.RouteVar;
import com.ossobo.winterfx.router.model.ResponseData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * FuncionariosRoutes v1.0
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
 *   Composição  : "departamento", "funcao" — operações parciais.
 *   Filtros     : com-filtros aceita 5 chaves OPCIONAIS — omitir = sem filtro:
 *                 departamento, funcao, local, nome, ativo (true/false).
 *
 * v1.0 - Criação; 37 rotas (GET 28 / PUT 7 / DELETE 2); status dividido em
 *        rotas explícitas; imagem exposta nos três canais aplicáveis.
 */
@Component
@RequestMapping("funcionarios/service")
public class FuncionariosRoutes {

    private static final Logger logger = LoggerFactory.getLogger(FuncionariosRoutes.class);

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
    // ROTAS — IMAGEM DE PERFIL
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

    /** Funcionários COM imagem. */
    @GetMapping("imagem/com")
    public ResponseData comImagem() {
        return lista(service::listarComImagem, "funcionarios");
    }

    /** Funcionários SEM imagem. */
    @GetMapping("imagem/sem")
    public ResponseData semImagem() {
        return lista(service::listarSemImagem, "funcionarios");
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

    /** Remove a imagem de perfil. */
    @DeleteMapping("imagem/por/coddep")
    public ResponseData removerImagem(@RouteVar("coddep") String codDep) {
        return escrita(() -> {
            service.removerImagemPerfil(codDep);
            return "Imagem removida";
        }, "mensagem");
    }

    // ============================================================
    // ROTAS — FILTROS SIMPLES (GET)
    // ============================================================

    @GetMapping("por/departamento")
    public ResponseData porDepartamento(@RouteVar("departamento") String departamento) {
        return lista(() -> service.buscarPorDepartamento(departamento), "funcionarios");
    }

    @GetMapping("por/funcao")
    public ResponseData porFuncao(@RouteVar("funcao") String funcao) {
        return lista(() -> service.buscarPorFuncao(funcao), "funcionarios");
    }

    @GetMapping("por/local")
    public ResponseData porLocal(@RouteVar("local") String local) {
        return lista(() -> service.buscarPorLocalTrabalho(local), "funcionarios");
    }

    /** Busca textual por nome. */
    @GetMapping("buscar/por/nome")
    public ResponseData porNome(@RouteVar("nome") String nome) {
        return lista(() -> service.buscarPorNome(nome), "funcionarios");
    }

    /** Ativos. */
    @GetMapping("status/ativos")
    public ResponseData ativos() {
        return lista(() -> service.buscarPorStatus(true), "funcionarios");
    }

    /** Inativos. */
    @GetMapping("status/inativos")
    public ResponseData inativos() {
        return lista(() -> service.buscarPorStatus(false), "funcionarios");
    }

    /**
     * Filtro combinado — TODAS as chaves OPCIONAIS:
     * departamento, funcao, local, nome, ativo ("true"/"false").
     * Chave omitida no Params = critério ignorado.
     */
    @GetMapping("com-filtros")
    public ResponseData comFiltros(@RouteVar("departamento") String departamento,
                                   @RouteVar("funcao") String funcao,
                                   @RouteVar("local") String local,
                                   @RouteVar("nome") String nome,
                                   @RouteVar("ativo") Boolean ativo) {
        return lista(() -> service.buscarComFiltros(
                departamento, funcao, local, ativo, nome), "funcionarios");
    }

    // ============================================================
    // ROTAS — ESTATÍSTICAS E DISTINCT (GET)
    // ============================================================

    @GetMapping("estatisticas")
    public ResponseData estatisticas()          { return valor(service::obterEstatisticas, "estatisticas"); }

    @GetMapping("stats/departamento")
    public ResponseData statsDepartamento()     { return valor(service::obterContagemPorDepartamento, "estatisticas"); }

    @GetMapping("stats/funcao")
    public ResponseData statsFuncao()           { return valor(service::obterContagemPorFuncao, "estatisticas"); }

    @GetMapping("stats/imagem")
    public ResponseData statsImagem()           { return valor(service::obterEstatisticasImagem, "estatisticas"); }

    @GetMapping("total")
    public ResponseData total()                 { return valor(service::contarTotal, "total"); }

    @GetMapping("total/ativos")
    public ResponseData totalAtivos()           { return valor(service::contarAtivos, "total"); }

    @GetMapping("distinct/departamentos")
    public ResponseData departamentos()         { return lista(service::listarDepartamentos, "valores"); }

    @GetMapping("distinct/funcoes")
    public ResponseData funcoes()               { return lista(service::listarFuncoes, "valores"); }

    @GetMapping("distinct/locais-trabalho")
    public ResponseData locaisTrabalho()        { return lista(service::listarLocaisTrabalho, "valores"); }

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

    /** Transfere de departamento. */
    @PutMapping("transferir-departamento")
    public ResponseData transferir(@RouteVar("coddep") String codDep,
                                   @RouteVar("departamento") String departamento) {
        return escrita(() -> {
            service.transferirDepartamento(codDep, departamento);
            return "Transferido para: " + departamento;
        }, "mensagem");
    }

    /** Promove/muda função. */
    @PutMapping("promover")
    public ResponseData promover(@RouteVar("coddep") String codDep,
                                 @RouteVar("funcao") String funcao) {
        return escrita(() -> {
            service.promover(codDep, funcao);
            return "Nova função: " + funcao;
        }, "mensagem");
    }

    // ============================================================
    // ROTAS — REMOÇÃO (DELETE)
    // ============================================================

    /**
     * ⚠️ EXCLUSÃO FÍSICA. Política em análise (backlog OBS-F1):
     * histórico de estoque referencia este codDep. Prefira "desativar".
     */
    @DeleteMapping("por/coddep")
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
            logger.warn("⚠️ Regra de negócio violada: {}", e.getMessage());
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
        logger.error("❌ Erro de banco de dados: {}", e.getMessage(), e);
        return ResponseData.error("Erro de banco de dados: " + e.getMessage())
                .withError("banco", e.getMessage());
    }

    private void exigirPaginacao(Integer pagina, Integer tamanho) {
        if (pagina == null || pagina < 1 || tamanho == null || tamanho < 1) {
            throw new IllegalArgumentException("Página e tamanho devem ser >= 1");
        }
    }
}