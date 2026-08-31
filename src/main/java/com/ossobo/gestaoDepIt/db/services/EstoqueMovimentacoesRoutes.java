package com.ossobo.gestaoDepIt.db.services;

import com.ossobo.gestaoDepIt.db.enums.TipoMovimentacao;
import com.ossobo.gestaoDepIt.db.models.EstoqueMovimentacoes;
import com.ossobo.winterfx.anotations.Component;
import com.ossobo.winterfx.anotations.GetMapping;
import com.ossobo.winterfx.anotations.Inject;
import com.ossobo.winterfx.anotations.PutMapping;
import com.ossobo.winterfx.anotations.RequestMapping;
import com.ossobo.winterfx.anotations.RouteVar;
import com.ossobo.winterfx.router.model.ResponseData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * EstoqueMovimentacoesRoutes v1.0
 *
 * Responsabilidade: Fronteira de Internal Routing das movimentações de estoque.
 *                   Handlers FINOS: delegam ao EstoqueMovimentacoesService e
 *                   traduzem exceção → ResponseData.
 *
 * Contratos de payload (documentação viva da API):
 *   Chaves comuns: sku, quantidade (Integer), codDep, motivo, observacoes,
 *                  lote (opcional), dataValidade ("yyyy-MM-dd" ISO, opcional),
 *                  localizacao, inicio/fim (períodos ISO, obrigatórios),
 *                  limiteDias (Integer), tipo (ENTRADA|SAIDA|AJUSTE|RESERVA).
 *
 * Nota de domínio: movimentações são HISTÓRICO IMUTÁVEL — nenhum canal DELETE
 * exposto aqui. Correções futuras entram como AJUSTE, jamais remoção.
 *
 * v1.0 - Criação; 22 rotas (GET leitura / PUT movimentação); conversões isoladas
 *        de datas (String→LocalDate) e enums (String→TipoMovimentacao).
 */
@Component
@RequestMapping("estoque-movimentacoes/service")
public class EstoqueMovimentacoesRoutes {

    private static final Logger logger = LoggerFactory.getLogger(EstoqueMovimentacoesRoutes.class);

    @Inject
    private EstoqueMovimentacoesService service;

    // ============================================================
    // ROTAS — CONSULTAS DE HISTÓRICO (GET)
    // ============================================================

    /** Lista todas as movimentações. */
    @GetMapping("todos")
    public ResponseData todos() {
        return lista(service::listarTodos, "movimentacoes");
    }

    /** Busca movimentação por ID. Erro semântico se inexistente. */
    @GetMapping("por/id")
    public ResponseData buscarPorId(@RouteVar("id") Long id) {
        return optional(() -> service.buscarPorId(id), "movimentacao",
                "Movimentação não encontrada: ID " + id);
    }

    /** Histórico completo do SKU. */
    @GetMapping("por/sku")
    public ResponseData buscarPorSku(@RouteVar("sku") String sku) {
        return lista(() -> service.buscarPorSku(sku), "movimentacoes");
    }

    /**
     * Filtra por tipo. Chave "tipo": ENTRADA | SAIDA | AJUSTE | RESERVA.
     */
    @GetMapping("por/tipo")
    public ResponseData buscarPorTipo(@RouteVar("tipo") String tipo) {
        return lista(() -> service.buscarPorTipo(converterTipo(tipo)), "movimentacoes");
    }

    /** Filtra por período fechado. Chaves ISO obrigatórias: "inicio", "fim". */
    @GetMapping("por/periodo")
    public ResponseData buscarPorPeriodo(@RouteVar("inicio") String inicio,
                                         @RouteVar("fim") String fim) {
        return lista(() -> service.buscarPorPeriodo(
                converterDataObrigatoria(inicio, "inicio"),
                converterDataObrigatoria(fim, "fim")), "movimentacoes");
    }

    /** Movimentações de um funcionário. Chave "codDep". */
    @GetMapping("por/funcionario")
    public ResponseData buscarPorFuncionario(@RouteVar("codDep") String codDep) {
        return lista(() -> service.buscarPorFuncionario(codDep), "movimentacoes");
    }

    /** Itens vencendo até a data-limite. Chave "limite" ISO. */
    @GetMapping("vencimentos/proximos")
    public ResponseData proximosVencimento(@RouteVar("limite") String limite) {
        return lista(() -> service.buscarProximosVencimento(
                converterDataObrigatoria(limite, "limite")), "movimentacoes");
    }

    /** Itens já vencidos. */
    @GetMapping("vencimentos/vencidos")
    public ResponseData vencidos() {
        return lista(service::buscarVencidos, "movimentacoes");
    }

    /** Licenças vencendo nos próximos N dias. Chave "dias" (Integer). */
    @GetMapping("licenca/proxima-vencer")
    public ResponseData licencasAVencer(@RouteVar("dias") Integer dias) {
        return lista(() -> service.buscarLicencaProximaVencimento(
                exigirInt(dias, "dias")), "movimentacoes");
    }

    // ============================================================
    // ROTAS — MOVIMENTAÇÕES (PUT) ⚠️ sempre encaminham evento EstoqueEvent
    // ============================================================

    /**
     * ENTRADA de material.
     * Obrigatórios: sku, quantidade, codDep. Opcionais: lote, dataValidade (ISO),
     * localizacao, motivo, observacoes.
     */
    @PutMapping("entrada")
    public ResponseData registrarEntrada(
            @RouteVar("sku") String sku,
            @RouteVar("quantidade") Integer quantidade,
            @RouteVar("lote") String lote,
            @RouteVar("dataValidade") String dataValidade,
            @RouteVar("localizacao") String localizacao,
            @RouteVar("codDep") String codDep,
            @RouteVar("motivo") String motivo,
            @RouteVar("observacoes") String observacoes) {

        return escrita(() -> service.registrarEntrada(
                sku, exigirInt(quantidade, "quantidade"), lote,
                converterDataOpcional(dataValidade, "dataValidade"),
                localizacao, codDep, motivo, observacoes), "movimentacao");
    }

    /** SAÍDA. Obrigatórios: sku, quantidade, codDep. Valida saldo antes de gravar. */
    @PutMapping("saida")
    public ResponseData registrarSaida(
            @RouteVar("sku") String sku,
            @RouteVar("quantidade") Integer quantidade,
            @RouteVar("codDep") String codDep,
            @RouteVar("motivo") String motivo,
            @RouteVar("observacoes") String observacoes) {

        return escrita(() -> service.registrarSaida(
                sku, exigirInt(quantidade, "quantidade"),
                codDep, motivo, observacoes), "movimentacao");
    }

    /** AJUSTE (positivo ou negativo). */
    @PutMapping("ajuste")
    public ResponseData registrarAjuste(
            @RouteVar("sku") String sku,
            @RouteVar("quantidade") Integer quantidade,
            @RouteVar("codDep") String codDep,
            @RouteVar("motivo") String motivo,
            @RouteVar("observacoes") String observacoes) {

        return escrita(() -> service.registrarAjuste(
                sku, exigirInt(quantidade, "quantidade"),
                codDep, motivo, observacoes), "movimentacao");
    }

    /** RESERVA. Consome saldo disponível. */
    @PutMapping("reserva")
    public ResponseData registrarReserva(
            @RouteVar("sku") String sku,
            @RouteVar("quantidade") Integer quantidade,
            @RouteVar("codDep") String codDep,
            @RouteVar("motivo") String motivo,
            @RouteVar("observacoes") String observacoes) {

        return escrita(() -> service.registrarReserva(
                sku, exigirInt(quantidade, "quantidade"),
                codDep, motivo, observacoes), "movimentacao");
    }

    /**
     * TRANSFERÊNCIA entre localizações (gera SAÍDA + ENTRADA encadeadas).
     * ⚠️ Não-atômico hoje (backlog OBS-E1).
     */
    @PutMapping("transferencia")
    public ResponseData registrarTransferencia(
            @RouteVar("sku") String sku,
            @RouteVar("quantidade") Integer quantidade,
            @RouteVar("origem") String origem,
            @RouteVar("destino") String destino,
            @RouteVar("codDep") String codDep,
            @RouteVar("observacoes") String observacoes) {

        return escrita(() -> {
            service.registrarTransferencia(
                    sku, exigirInt(quantidade, "quantidade"),
                    origem, destino, codDep, observacoes);
            return "Transferência concluída: " + origem + " → " + destino;
        }, "mensagem");
    }

    /** DEVOLUÇÃO (ENTRADA com motivo default "Devolução"). */
    @PutMapping("devolucao")
    public ResponseData registrarDevolucao(
            @RouteVar("sku") String sku,
            @RouteVar("quantidade") Integer quantidade,
            @RouteVar("lote") String lote,
            @RouteVar("codDep") String codDep,
            @RouteVar("motivo") String motivo,
            @RouteVar("observacoes") String observacoes) {

        return escrita(() -> {
            service.registrarDevolucao(
                    sku, exigirInt(quantidade, "quantidade"),
                    lote, codDep, motivo, observacoes);
            return "Devolução registrada";
        }, "mensagem");
    }

    /** PERDA/AVARIA (SAÍDA com motivo default "Perda/avaria"). */
    @PutMapping("perda")
    public ResponseData registrarPerda(
            @RouteVar("sku") String sku,
            @RouteVar("quantidade") Integer quantidade,
            @RouteVar("codDep") String codDep,
            @RouteVar("motivo") String motivo,
            @RouteVar("observacoes") String observacoes) {

        return escrita(() -> {
            service.registrarPerda(
                    sku, exigirInt(quantidade, "quantidade"),
                    codDep, motivo, observacoes);
            return "Perda registrada";
        }, "mensagem");
    }

    // ============================================================
    // ROTAS — ANALÍTICAS (GET)
    // ============================================================

    /** Saldo atual do SKU (entradas − saídas + ajustes). */
    @GetMapping("saldo/por/sku")
    public ResponseData calcularSaldo(@RouteVar("sku") String sku) {
        return valor(() -> service.calcularSaldo(sku), "saldo");
    }

    /** true se saldo >= quantidade pedida. */
    @GetMapping("saldo/suficiente")
    public ResponseData saldoSuficiente(@RouteVar("sku") String sku,
                                        @RouteVar("quantidade") Integer quantidade) {
        return valor(() -> service.validarSaldoSuficiente(
                sku, exigirInt(quantidade, "quantidade")), "suficiente");
    }

    /**
     * Evolução diária do saldo no período. Retorno: Map<LocalDate, Integer>.
     * Chaves ISO obrigatórias: "inicio", "fim".
     */
    @GetMapping("evolucao-saldo")
    public ResponseData evolucaoSaldo(@RouteVar("sku") String sku,
                                      @RouteVar("inicio") String inicio,
                                      @RouteVar("fim") String fim) {
        return valor(() -> service.obterEvolucaoSaldo(
                sku,
                converterDataObrigatoria(inicio, "inicio"),
                converterDataObrigatoria(fim, "fim")), "evolucao");
    }

    /** Contagem agregada por tipo no período. Retorno: Map<String, Integer>. */
    @GetMapping("contagem-por-tipo")
    public ResponseData contagemPorTipo(@RouteVar("inicio") String inicio,
                                        @RouteVar("fim") String fim) {
        return valor(() -> service.obterContagemPorTipo(
                converterDataObrigatoria(inicio, "inicio"),
                converterDataObrigatoria(fim, "fim")), "contagem");
    }

    /** Relatório consolidado do dia. Retorno: Map<String, Object> imutável. */
    @GetMapping("relatorio/diario")
    public ResponseData relatorioDiario(@RouteVar("data") String data) {
        return valor(() -> service.obterRelatorioDiario(
                converterDataObrigatoria(data, "data")), "relatorio");
    }

    @GetMapping("total")
    public ResponseData contarTotal() {
        return valor(service::contarTotal, "total");
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

    private int exigirInt(Integer valor, String campo) {
        if (valor == null) {
            throw new IllegalArgumentException("Parâmetro obrigatório: " + campo);
        }
        return valor;
    }

    // ============================================================
    // CONVERSÕES ISOLADAS (String da rota → tipos de domínio)
    // ============================================================

    /** Converte ISO "yyyy-MM-dd"; campo opcional: null/vazio → null. */
    private LocalDate converterDataOpcional(String bruto, String campo) {
        if (bruto == null || bruto.isBlank()) {
            return null;
        }
        return parseData(bruto, campo);
    }

    /** Converte ISO "yyyy-MM-dd"; falha se ausente. */
    private LocalDate converterDataObrigatoria(String bruto, String campo) {
        if (bruto == null || bruto.isBlank()) {
            throw new IllegalArgumentException("Data obrigatória: " + campo);
        }
        return parseData(bruto, campo);
    }

    private LocalDate parseData(String bruto, String campo) {
        try {
            return LocalDate.parse(bruto.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Data inválida em '" + campo + "': '" + bruto + "' (use yyyy-MM-dd)");
        }
    }

    /** Converte para TipoMovimentacao, listando valores válidos no erro. */
    private TipoMovimentacao converterTipo(String bruto) {
        if (bruto == null || bruto.isBlank()) {
            throw new IllegalArgumentException("Tipo de movimentação é obrigatório");
        }
        try {
            return TipoMovimentacao.valueOf(bruto.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            String validos = String.join(", ",
                    java.util.Arrays.stream(TipoMovimentacao.values()).map(Enum::name).toList());
            throw new IllegalArgumentException(
                    "Tipo inválido: '" + bruto + "'. Válidos: " + validos);
        }
    }
}