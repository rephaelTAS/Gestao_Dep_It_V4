package com.ossobo.gestaoDepIt.db.services;

import com.ossobo.gestaoDepIt.db.models.ConfigServidorRemoto;
import com.ossobo.winterfx.anotations.Component;
import com.ossobo.winterfx.anotations.DeleteMapping;
import com.ossobo.winterfx.anotations.ExecMapping;
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
import java.util.Optional;

/**
 * ConfigServidorRemotoRoutes v1.0
 *
 * Responsabilidade: Fronteira de Internal Routing das configurações de
 *                   servidor remoto. Handlers FINOS: delegam ao
 *                   ConfigServidorRemotoService e traduzem exceção → ResponseData.
 *
 * Nota: regras de negócio e eventos (ConfigEvent) residem no Service (intacto).
 *       Única classe do projeto usando EXEC até agora — testes de conexão são
 *       COMANDOS (tarefa + timeout de rede), não mutações de dados.
 *
 * ⚠️ THREADING: rotas de teste bloqueiam até ~5s (timeout JDBC).
 *    Chamadores DEVEM executá-las FORA da JavaFX Thread
 *    (CompletableFuture.supplyAsync ou ApiDispatcher.onFxThread p/ resultados).
 *
 * v1.0 - Criação; 11 rotas (GET leitura / PUT mutação / DELETE remoção /
 *        EXEC comandos); estrutura de helpers espelhada em CatalogoProdutosRoutes.
 */
@Component
@RequestMapping("config-servidor-remoto/service")
public class ConfigServidorRemotoRoutes {

    private static final Logger logger = LoggerFactory.getLogger(ConfigServidorRemotoRoutes.class);

    @Inject
    private ConfigServidorRemotoService service;

    // ============================================================
    // ROTAS — CONSULTAS (GET)
    // ============================================================

    /** Lista todas as configurações cadastradas. */
    @GetMapping("todos")
    public ResponseData todos() {
        return lista(service::listarTodos, "configs");
    }

    /** Busca configuração por ID. Erro semântico se inexistente. */
    @GetMapping("por/id")
    public ResponseData buscarPorId(@RouteVar("id") Long id) {
        return optional(() -> service.buscarPorId(id), "config",
                "Configuração não encontrada: ID " + id);
    }

    /** Busca configuração por nome exato. */
    @GetMapping("por/nome")
    public ResponseData buscarPorNome(@RouteVar("nome") String nome) {
        return optional(() -> service.buscarPorNome(nome), "config",
                "Configuração não encontrada: '" + nome + "'");
    }

    /** Retorna a configuração ATIVA atual (pode não existir nenhuma). */
    @GetMapping("ativa")
    public ResponseData buscarAtiva() {
        return optionalSemErro(service::buscarAtiva, "config");
    }

    /** true se há servidor remoto configurado E conectado. */
    @GetMapping("exists/configurada")
    public ResponseData isConfigurada() {
        return valor(service::isServidorRemotoConfigurado, "configurada");
    }

    // ============================================================
    // ROTAS — MUTAÇÕES (PUT)
    // ============================================================

    /** Cria nova configuração. Payload: "config". */
    @PutMapping("salvar")
    public ResponseData salvar(@Payload("config") ConfigServidorRemoto config) {
        return escrita(() -> service.salvar(config), "config");
    }

    /** Atualiza configuração existente. Payload: "config". */
    @PutMapping("atualizar")
    public ResponseData atualizar(@Payload("config") ConfigServidorRemoto config) {
        return escrita(() -> service.atualizar(config), "config");
    }

    /** Define como ativa (desativa as demais). */
    @PutMapping("definir-como-ativa")
    public ResponseData definirComoAtiva(@RouteVar("id") Long id) {
        return escrita(() -> service.definirComoAtiva(id), "config");
    }

    // ============================================================
    // ROTAS — REMOÇÃO (DELETE)
    // ============================================================

    /** Exclui configuração por ID. */
    @DeleteMapping("por/id")
    public ResponseData deletar(@RouteVar("id") Long id) {
        return escrita(() -> { service.deletar(id); return "Configuração " + id + " excluída"; }, "mensagem");
    }

    // ============================================================
    // ROTAS — COMANDOS (EXEC) ⚠️ BLOQUEANTES (~5s)
    // ============================================================

    /**
     * Testa conexão da config recebida e persiste o status resultante.
     * Payload: "config". ⚠️ Chamar FORA da FX Thread.
     */
    @ExecMapping("testar")
    public ResponseData testar(@Payload("config") ConfigServidorRemoto config) {
        return valor(() -> service.testarConexao(config), "conectado");
    }

    /**
     * Testa conexão da config ativa. Sem config ativa → conectado=false.
     * ⚠️ Chamar FORA da FX Thread.
     */
    @ExecMapping("testar/ativa")
    public ResponseData testarAtiva() {
        return valor(service::testarConexaoAtiva, "conectado");
    }

    /**
     * Testa conexão retornando mensagem detalhada para exibição.
     * Payload: "config". ⚠️ Chamar FORA da FX Thread.
     */
    @ExecMapping("testar/mensagem")
    public ResponseData testarComMensagem(@Payload("config") ConfigServidorRemoto config) {
        return valor(() -> service.testarConexaoComMensagem(config), "mensagem");
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

    private ResponseData lista(Acao<java.util.List<?>> acao, String chave) {
        try {
            var dados = acao.executar();
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

    /** Optional presente → sucesso; vazio → erro semântico com mensagem. */
    private <T> ResponseData optional(Acao<Optional<T>> acao, String chave, String msgVazio) {
        try {
            return acao.executar()
                    .<ResponseData>map(v -> ResponseData.success().withData(chave, v))
                    .orElseGet(() -> ResponseData.error(msgVazio).withError(chave, "não encontrado"));
        } catch (SQLException e) {
            return erroBanco(e);
        }
    }

    /** Optional vazio ≠ erro (ex.: nenhuma config ativa é estado válido). */
    private <T> ResponseData optionalSemErro(Acao<Optional<T>> acao, String chave) {
        try {
            return ResponseData.success()
                    .withData(chave, acao.executar().orElse(null));
        } catch (SQLException e) {
            return erroBanco(e);
        }
    }

    private ResponseData erroBanco(SQLException e) {
        logger.error("❌ Erro de banco de dados: {}", e.getMessage(), e);
        return ResponseData.error("Erro de banco de dados: " + e.getMessage())
                .withError("banco", e.getMessage());
    }
}