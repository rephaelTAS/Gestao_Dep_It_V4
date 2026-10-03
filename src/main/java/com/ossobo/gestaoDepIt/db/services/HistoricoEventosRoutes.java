package com.ossobo.gestaoDepIt.db.services;

import com.ossobo.winterfx.anotations.*;
import com.ossobo.winterfx.router.model.ResponseData;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * HistoricoEventosRoutes v1.1
 *
 * Responsabilidade: Fronteira de Internal Routing das CONSULTAS e dos
 *                   utilitários JSON do histórico de eventos.
 *
 * v1.1 — Fronteira única de leitura + ledger inviolável:
 *        - PUT registrar e PUT registrar/tipo REMOVIDOS. Gravação de evento
 *          agora só acontece via {@code InventarioCrudService}, que
 *          orquestra transação inventário + histórico atomicamente.
 *        - DELETE antigos/antes-de e DELETE limpar/por/sku REMOVIDOS.
 *          O histórico é LEDGER APPEND-ONLY: nada é apagado, nunca.
 *        - Canal EXEC (json/funcionario, json/produto, json/generico) mantido
 *          — utilitários puros, sem persistência.
 *        - Helpers `escrita` e `Acao<T>` com catch de negócio removidos
 *          (nenhum handler PUT restante os usa).
 *
 * @since v1.0
 */
@Component
@RequestMapping("historico-eventos/service")
public class HistoricoEventosRoutes {

    private static final System.Logger logger = System.getLogger(HistoricoEventosRoutes.class.getName());

    @Inject
    private HistoricoEventosService service;

    // ============================================================
    // ROTAS — CONSULTAS (GET)
    // ============================================================

    @GetMapping("todos")
    public ResponseData todos() {
        return lista(service::listarTodos, "eventos");
    }

    @GetMapping("paginados")
    public ResponseData paginados(@RouteVar("pagina") Integer pagina,
                                  @RouteVar("tamanho") Integer tamanho) {
        return lista(() -> {
            exigirPaginacao(pagina, tamanho);
            return service.listarTodos(pagina, tamanho);
        }, "eventos");
    }

    @GetMapping("por/id")
    public ResponseData buscarPorId(@RouteVar("id") String id) {
        return optional(() -> service.buscarPorId(id), "evento",
                "Evento não encontrado: ID " + id);
    }

    @GetMapping("por/sku")
    public ResponseData porSku(@RouteVar("sku") String sku) {
        return lista(() -> service.buscarPorSku(sku), "eventos");
    }

    @GetMapping("por/funcionario")
    public ResponseData porFuncionario(@RouteVar("funcionarioid") String funcionarioId) {
        return lista(() -> service.buscarPorFuncionario(funcionarioId), "eventos");
    }

    @GetMapping("por/tipo")
    public ResponseData porTipo(@RouteVar("tipo") String tipo) {
        return lista(() -> service.buscarPorTipo(tipo), "eventos");
    }

    @GetMapping("por/periodo")
    public ResponseData porPeriodo(@RouteVar("inicio") String inicio,
                                   @RouteVar("fim") String fim) {
        return lista(() -> service.buscarPorPeriodo(
                converterDataHoraObrigatoria(inicio, "inicio"),
                converterDataHoraObrigatoria(fim, "fim")), "eventos");
    }

    @GetMapping("com-filtros")
    public ResponseData comFiltros(@RouteVar("sku") String sku,
                                   @RouteVar("funcionarioid") String funcionarioId,
                                   @RouteVar("tipo") String tipo,
                                   @RouteVar("inicio") String inicio,
                                   @RouteVar("fim") String fim) {
        return lista(() -> service.buscarComFiltros(
                        sku, funcionarioId, tipo,
                        converterDataHoraOpcional(inicio, "inicio"),
                        converterDataHoraOpcional(fim, "fim")),
                "eventos");
    }

    @GetMapping("alteracoes-produto")
    public ResponseData alteracoesProduto(@RouteVar("sku") String sku) {
        return lista(() -> service.buscarAlteracoesProduto(sku), "eventos");
    }

    @GetMapping("auth-eventos")
    public ResponseData eventosAutenticacao(@RouteVar("inicio") String inicio,
                                            @RouteVar("fim") String fim) {
        return lista(() -> service.buscarEventosAutenticacao(
                converterDataHoraObrigatoria(inicio, "inicio"),
                converterDataHoraObrigatoria(fim, "fim")), "eventos");
    }

    // ============================================================
    // ROTAS — ESTATÍSTICAS (GET)
    // ============================================================

    @GetMapping("stats/tipo")
    public ResponseData statsTipo(@RouteVar("inicio") String inicio,
                                  @RouteVar("fim") String fim) {
        return valor(() -> service.obterEstatisticasPorTipo(
                converterDataHoraObrigatoria(inicio, "inicio"),
                converterDataHoraObrigatoria(fim, "fim")), "estatisticas");
    }

    @GetMapping("stats/sku")
    public ResponseData statsSku(@RouteVar("inicio") String inicio,
                                 @RouteVar("fim") String fim) {
        return valor(() -> service.obterEstatisticasPorSku(
                converterDataHoraObrigatoria(inicio, "inicio"),
                converterDataHoraObrigatoria(fim, "fim")), "estatisticas");
    }

    @GetMapping("stats/funcionario")
    public ResponseData statsFuncionario(@RouteVar("inicio") String inicio,
                                         @RouteVar("fim") String fim) {
        return valor(() -> service.obterEstatisticasPorFuncionario(
                converterDataHoraObrigatoria(inicio, "inicio"),
                converterDataHoraObrigatoria(fim, "fim")), "estatisticas");
    }

    @GetMapping("stats/hora")
    public ResponseData statsHora(@RouteVar("inicio") String inicio,
                                  @RouteVar("fim") String fim) {
        return valor(() -> service.obterAtividadePorHora(
                converterDataHoraObrigatoria(inicio, "inicio"),
                converterDataHoraObrigatoria(fim, "fim")), "atividade");
    }

    @GetMapping("total")
    public ResponseData total() {
        return valor(service::contarTotal, "total");
    }

    @GetMapping("total/periodo")
    public ResponseData totalPeriodo(@RouteVar("inicio") String inicio,
                                     @RouteVar("fim") String fim) {
        return valor(() -> service.contarPorPeriodo(
                converterDataHoraObrigatoria(inicio, "inicio"),
                converterDataHoraObrigatoria(fim, "fim")), "total");
    }

    @GetMapping("tipos-permitidos")
    public ResponseData tiposPermitidos() {
        return valor(service::getTiposPermitidos, "tipos");
    }

    @GetMapping("exists/id")
    public ResponseData existePorId(@RouteVar("id") String id) {
        return valor(() -> service.existePorId(id), "exists");
    }

    // ============================================================
    // ROTAS — UTILITÁRIOS JSON (EXEC: comandos puros, sem persistência)
    // ============================================================

    @ExecMapping("json/funcionario")
    public ResponseData jsonFuncionario(@RouteVar("nome") String nome,
                                        @RouteVar("departamento") String departamento,
                                        @RouteVar("cargo") String cargo) {
        return valor(() -> service.criarJsonFuncionario(nome, departamento, cargo), "json");
    }

    @ExecMapping("json/produto")
    public ResponseData jsonProduto(@RouteVar("sku") String sku,
                                    @RouteVar("nome") String nome,
                                    @RouteVar("descricao") String descricao,
                                    @RouteVar("categoria") String categoria,
                                    @RouteVar("quantidade") Integer quantidade,
                                    @RouteVar("localizacao") String localizacao) {
        int qtd = (quantidade != null) ? quantidade : 0;
        return valor(() -> service.criarJsonProduto(sku, nome, descricao, categoria, qtd, localizacao), "json");
    }

    @ExecMapping("json/generico")
    public ResponseData jsonGenerico(@Payload("dados") Map<String, Object> dados) {
        if (dados == null || dados.isEmpty()) {
            return ResponseData.error("Mapa 'dados' é obrigatório e não pode estar vazio")
                    .withError("dados", "payload ausente");
        }
        return valor(() -> service.criarJsonGenerico(dados), "json");
    }

    // ============================================================
    // HELPERS DE FRONTEIRA
    // ============================================================

    @FunctionalInterface
    private interface Acao<T> { T executar() throws SQLException; }

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
        logger.log(System.Logger.Level.ERROR, "❌ Erro de banco de dados: {}", e.getMessage(), e);
        return ResponseData.error("Erro de banco de dados: " + e.getMessage())
                .withError("banco", e.getMessage());
    }

    private void exigirPaginacao(Integer pagina, Integer tamanho) {
        if (pagina == null || pagina < 1 || tamanho == null || tamanho < 1) {
            throw new IllegalArgumentException("Página e tamanho devem ser >= 1");
        }
    }

    // ============================================================
    // CONVERSÕES ISOLADAS
    // ============================================================

    private LocalDateTime converterDataHoraOpcional(String bruto, String campo) {
        if (bruto == null || bruto.isBlank()) return null;
        return parseDataHora(bruto, campo);
    }

    private LocalDateTime converterDataHoraObrigatoria(String bruto, String campo) {
        if (bruto == null || bruto.isBlank()) {
            throw new IllegalArgumentException("Data/hora obrigatória: " + campo);
        }
        return parseDataHora(bruto, campo);
    }

    private LocalDateTime parseDataHora(String bruto, String campo) {
        try {
            return LocalDateTime.parse(bruto.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Data/hora inválida em '" + campo + "': '" + bruto +
                            "' (use yyyy-MM-ddTHH:mm:ss)");
        }
    }
}