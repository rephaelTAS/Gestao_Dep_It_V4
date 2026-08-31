package com.ossobo.gestaoDepIt.db.services;

import com.ossobo.gestaoDepIt.db.models.HistoricoEventos;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * HistoricoEventosRoutes v1.0
 *
 * Responsabilidade: Fronteira de Internal Routing do histórico de eventos.
 *                   Handlers FINOS: delegam ao HistoricoEventosService e traduzem
 *                   exceção → ResponseData.
 *
 * Contratos de payload (documentação viva da API):
 *   Registro por objeto : PUT registrar → payload "evento" (HistoricoEventos).
 *   Registro por tipo   : PUT registrar/tipo → chaves "tipo" (ver GET
 *                         tipos-permitidos), "funcionarioid", "dadosnovos",
 *                         "descricao"; OPCIONAIS: "sku" (ausente em LOGIN/
 *                         LOGOUT), "dadosanteriores" (usado em ATUALIZACAO,
 *                         BAIXA, EXCLUSAO).
 *   Datas/hora          : "yyyy-MM-ddTHH:mm:ss" (ISO LocalDateTime); períodos
 *                         início/fim OBRIGATÓRIOS onde aplicável.
 *   Limpeza (DESTAQUE)  : DELETE é IRREVERSÍVEL — canais delimitados.
 *
 * Estreias deste arquivo:
 *   - Despacho unificado registrar/tipo (switch por tipo, evita 9 rotas gêmeas);
 *   - Canal EXEC para utilitários JSON (comandos puros, sem persistência).
 *
 * v1.0 - Criação; 25 rotas (GET 18 / PUT 2 / DELETE 2 / EXEC 3);
 *        datas String ISO convertidas isoladamente (padrão da família Routes).
 */
@Component
@RequestMapping("historico-eventos/service")
public class HistoricoEventosRoutes {

    private static final Logger logger = LoggerFactory.getLogger(HistoricoEventosRoutes.class);

    @Inject
    private HistoricoEventosService service;

    // ============================================================
    // ROTAS — CONSULTAS (GET)
    // ============================================================

    /** Todos os eventos históricos. */
    @GetMapping("todos")
    public ResponseData todos() {
        return lista(service::listarTodos, "eventos");
    }

    /** Lista paginada. Chaves: pagina (1-based), tamanho. */
    @GetMapping("paginados")
    public ResponseData paginados(@RouteVar("pagina") Integer pagina,
                                  @RouteVar("tamanho") Integer tamanho) {
        return lista(() -> {
            exigirPaginacao(pagina, tamanho);
            return service.listarTodos(pagina, tamanho);
        }, "eventos");
    }

    /** Busca evento por ID. Erro semântico se inexistente. */
    @GetMapping("por/id")
    public ResponseData buscarPorId(@RouteVar("id") Long id) {
        return optional(() -> service.buscarPorId(id), "evento",
                "Evento não encontrado: ID " + id);
    }

    /** Histórico completo de um produto. */
    @GetMapping("por/sku")
    public ResponseData porSku(@RouteVar("sku") String sku) {
        return lista(() -> service.buscarPorSku(sku), "eventos");
    }

    /** Histórico de ações de um funcionário. */
    @GetMapping("por/funcionario")
    public ResponseData porFuncionario(@RouteVar("funcionarioid") String funcionarioId) {
        return lista(() -> service.buscarPorFuncionario(funcionarioId), "eventos");
    }

    /** Eventos de um tipo. Chave "tipo": consulte GET tipos-permitidos. */
    @GetMapping("por/tipo")
    public ResponseData porTipo(@RouteVar("tipo") String tipo) {
        return lista(() -> service.buscarPorTipo(tipo), "eventos");
    }

    /** Eventos por período fechado. Chaves obrigatórias: inicio, fim (ISO). */
    @GetMapping("por/periodo")
    public ResponseData porPeriodo(@RouteVar("inicio") String inicio,
                                   @RouteVar("fim") String fim) {
        return lista(() -> service.buscarPorPeriodo(
                converterDataHoraObrigatoria(inicio, "inicio"),
                converterDataHoraObrigatoria(fim, "fim")), "eventos");
    }

    /**
     * Filtro combinado — TODAS as chaves OPCIONAIS:
     * sku, funcionarioid, tipo, inicio/fim (ISO). Omitir = sem critério.
     */
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

    /** Trilha de alterações de um produto (redução especializada). */
    @GetMapping("alteracoes-produto")
    public ResponseData alteracoesProduto(@RouteVar("sku") String sku) {
        return lista(() -> service.buscarAlteracoesProduto(sku), "eventos");
    }

    /** Eventos de autenticação (login/logout) no período. */
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

    /** Contagem por tipo no período. */
    @GetMapping("stats/tipo")
    public ResponseData statsTipo(@RouteVar("inicio") String inicio,
                                  @RouteVar("fim") String fim) {
        return valor(() -> service.obterEstatisticasPorTipo(
                converterDataHoraObrigatoria(inicio, "inicio"),
                converterDataHoraObrigatoria(fim, "fim")), "estatisticas");
    }

    /** Contagem por SKU no período. */
    @GetMapping("stats/sku")
    public ResponseData statsSku(@RouteVar("inicio") String inicio,
                                 @RouteVar("fim") String fim) {
        return valor(() -> service.obterEstatisticasPorSku(
                converterDataHoraObrigatoria(inicio, "inicio"),
                converterDataHoraObrigatoria(fim, "fim")), "estatisticas");
    }

    /** Contagem por funcionário no período. */
    @GetMapping("stats/funcionario")
    public ResponseData statsFuncionario(@RouteVar("inicio") String inicio,
                                         @RouteVar("fim") String fim) {
        return valor(() -> service.obterEstatisticasPorFuncionario(
                converterDataHoraObrigatoria(inicio, "inicio"),
                converterDataHoraObrigatoria(fim, "fim")), "estatisticas");
    }

    /** Distribuição da atividade por hora do dia. Retorno: Map<Integer, Integer>. */
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

    /** Lista de tipos válidos (fonte da verdade p/ UI e p/ registrar/tipo). */
    @GetMapping("tipos-permitidos")
    public ResponseData tiposPermitidos() {
        return valor(service::getTiposPermitidos, "tipos");
    }

    @GetMapping("exists/id")
    public ResponseData existePorId(@RouteVar("id") Long id) {
        return valor(() -> service.existePorId(id), "exists");
    }

    // ============================================================
    // ROTAS — REGISTRO (PUT)
    // ============================================================

    /**
     * Registra evento a partir do objeto completo. Payload: "evento".
     * Útil para chamadas internas via rota (política domínio⇄domínio),
     * quando quem emite já possui o HistoricoEventos montado.
     */
    @PutMapping("registrar")
    public ResponseData registrar(@Payload("evento") HistoricoEventos evento) {
        return escrita(() -> service.registrar(evento), "evento");
    }

    /**
     * DESPACHO UNIFICADO por tipo (evita 9 rotas gêmeas).
     * Chave "tipo" aceita os valores listados em GET tipos-permitidos.
     * Campos por tipo:
     *   CRIACAO/MANUTENCAO/MOVIMENTACAO/INSTALACAO → sku + dadosnovos;
     *   ATUALIZACAO                                → sku + dadosanteriores + dadosnovos;
     *   BAIXA/EXCLUSAO                             → sku + dadosanteriores;
     *   LOGIN/LOGOUT                               → somente funcionarioid + dadosnovos.
     * Campos ausentes chegam null — o domínio valida o que exige.
     * ⚠️ Backlog OBS-H1: LOGIN/LOGOUT podem conflitar com validação de SKU no domínio.
     */
    @PutMapping("registrar/tipo")
    public ResponseData registrarPorTipo(
            @RouteVar("tipo") String tipo,
            @RouteVar("sku") String sku,
            @RouteVar("funcionarioid") String funcionarioId,
            @RouteVar("dadosanteriores") String dadosAnteriores,
            @RouteVar("dadosnovos") String dadosNovos,
            @RouteVar("descricao") String descricaoFuncionario) {

        if (tipo == null || tipo.isBlank()) {
            return ResponseData.error("Tipo de evento é obrigatório")
                    .withError("tipo", "informe um tipo válido (veja tipos-permitidos)");
        }

        return escrita(() -> {
            String t = tipo.trim().toUpperCase();
            return switch (t) {
                case "CRIACAO"      -> service.registrarCriacao(sku, funcionarioId, dadosNovos, descricaoFuncionario);
                case "ATUALIZACAO"  -> service.registrarAtualizacao(sku, funcionarioId, dadosAnteriores, dadosNovos, descricaoFuncionario);
                case "BAIXA"        -> service.registrarBaixa(sku, funcionarioId, dadosAnteriores, descricaoFuncionario);
                case "EXCLUSAO"     -> service.registrarExclusao(sku, funcionarioId, dadosAnteriores, descricaoFuncionario);
                case "MANUTENCAO"   -> service.registrarManutencao(sku, funcionarioId, dadosNovos, descricaoFuncionario);
                case "MOVIMENTACAO" -> service.registrarMovimentacao(sku, funcionarioId, dadosNovos, descricaoFuncionario);
                case "INSTALACAO"   -> service.registrarInstalacao(sku, funcionarioId, dadosNovos, descricaoFuncionario);
                case "LOGIN"        -> service.registrarLogin(funcionarioId, dadosNovos, descricaoFuncionario);
                case "LOGOUT"       -> service.registrarLogout(funcionarioId, dadosNovos, descricaoFuncionario);
                default             -> throw new IllegalArgumentException(
                        "Tipo não suportado: '" + tipo + "'. Veja rota tipos-permitidos.");
            };
        }, "evento");
    }

    // ============================================================
    // ROTAS — LIMPEZA (DELETE) ⚠️ IRREVERSÍVEL
    // ============================================================

    /**
     * Remove eventos ANTERIORES à data-hora limite (retenção/rotatividade).
     * Chave "limite" ISO obrigatória. Retorna quantidade removida.
     */
    @DeleteMapping("antigos/antes-de")
    public ResponseData limparAntigos(@RouteVar("limite") String limite) {
        return valor(() -> service.limparEventosAntigos(
                converterDataHoraObrigatoria(limite, "limite")), "removidos");
    }

    /** Remove TODO o histórico de um SKU. Ação administrativa extrema. */
    @DeleteMapping("por/sku")
    public ResponseData limparPorSku(@RouteVar("sku") String sku) {
        return valor(() -> service.limparEventosPorSku(sku), "removidos");
    }

    // ============================================================
    // ROTAS — UTILITÁRIOS JSON (EXEC: comandos puros, sem persistência)
    // ============================================================

    /** Monta JSON de funcionário para campos de dados. EXEC não toca banco. */
    @ExecMapping("json/funcionario")
    public ResponseData jsonFuncionario(@RouteVar("nome") String nome,
                                        @RouteVar("departamento") String departamento,
                                        @RouteVar("cargo") String cargo) {
        return valor(() -> service.criarJsonFuncionario(nome, departamento, cargo), "json");
    }

    /** Monta JSON de produto para campos de dados. */
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

    /** Serializa mapa arbitrário. Payload: "dados" (Map<String, Object>). */
    @ExecMapping("json/generico")
    public ResponseData jsonGenerico(@Payload("dados") Map<String, Object> dados) {
        if (dados == null || dados.isEmpty()) {
            return ResponseData.error("Mapa 'dados' é obrigatório e não pode estar vazio")
                    .withError("dados", "payload ausente");
        }
        return valor(() -> service.criarJsonGenerico(dados), "json");
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

    // ============================================================
    // CONVERSÕES ISOLADAS (String → LocalDateTime, padrão da família)
    // ============================================================

    /** Converte ISO datetime; campo opcional: null/vazio → null. */
    private LocalDateTime converterDataHoraOpcional(String bruto, String campo) {
        if (bruto == null || bruto.isBlank()) {
            return null;
        }
        return parseDataHora(bruto, campo);
    }

    /** Converte ISO datetime; falha se ausente. */
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