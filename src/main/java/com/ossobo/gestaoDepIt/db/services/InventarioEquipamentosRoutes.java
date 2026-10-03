package com.ossobo.gestaoDepIt.db.services;

import com.ossobo.winterfx.anotations.*;
import com.ossobo.winterfx.router.model.ResponseData;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

/**
 * InventarioEquipamentosRoutes v1.3
 *
 * Responsabilidade: Fronteira de Internal Routing das CONSULTAS do inventário
 *                   de equipamentos.
 *
 * v1.3 — Fronteira única de leitura:
 *        - TODAS as mutações foram removidas desta classe; vivem agora em
 *          {@code InventarioCrudRoutes} (@RequestMapping("inventario-crud/service")),
 *          que orquestra transação inventário + histórico atomicamente.
 *        - Este arquivo expõe APENAS GET (listagens, buscas, filtros,
 *          estatísticas, distinct e verificações de existência).
 *        - Helpers `acao`, `escrita` e `converterDataOpcional` removidos —
 *          não há mais handler PUT/DELETE que os use.
 *
 * @since v1.1
 */
@Component
@RequestMapping("inventario-equipamentos/service")
public class InventarioEquipamentosRoutes {

    private static final System.Logger logger = System.getLogger(InventarioEquipamentosRoutes.class.getName());

    @Inject
    private InventarioEquipamentosService service;

    // ============================================================
    // CONSULTAS BÁSICAS
    // ============================================================

    @GetMapping("todos")
    public ResponseData todos() {
        return lista(service::listarTodos, "equipamentos");
    }

    @GetMapping("paginados")
    public ResponseData paginados(@RouteVar("pagina") Integer pagina,
                                  @RouteVar("tamanho") Integer tamanho) {
        return lista(() -> {
            exigirPaginacao(pagina, tamanho);
            return service.listarTodos(pagina, tamanho);
        }, "equipamentos");
    }

    @GetMapping("por/id")
    public ResponseData buscarPorId(@RouteVar("id") String id) {
        return optional(() -> service.buscarPorId(id), "equipamento",
                "Equipamento não encontrado: ID " + id);
    }

    @GetMapping("por/num-serie")
    public ResponseData buscarPorNumSerie(@RouteVar("numserie") String numSerie) {
        return optional(() -> service.buscarPorNumSerie(numSerie), "equipamento",
                "Equipamento não encontrado: série '" + numSerie + "'");
    }

    @GetMapping("por/mac")
    public ResponseData buscarPorMac(@RouteVar("mac") String mac) {
        return optional(() -> service.buscarPorMacAddress(mac), "equipamento",
                "Equipamento não encontrado: MAC '" + mac + "'");
    }

    @GetMapping("por/mac-like")
    public ResponseData buscarPorMacLike(@RouteVar("padrao") String padrao) {
        return lista(() -> service.buscarPorMacAddressLike(padrao), "equipamentos");
    }

    @GetMapping("por/fatura")
    public ResponseData buscarPorFatura(@RouteVar("fatura") String numeroFatura) {
        return lista(() -> service.buscarPorNumeroFatura(numeroFatura), "equipamentos");
    }

    // ============================================================
    // FILTROS SIMPLES
    // ============================================================

    @GetMapping("por/status")
    public ResponseData porStatus(@RouteVar("status") String status) {
        return lista(() -> service.buscarPorStatus(status), "equipamentos");
    }

    @GetMapping("por/condicao")
    public ResponseData porCondicao(@RouteVar("condicao") String condicao) {
        return lista(() -> service.buscarPorCondicao(condicao), "equipamentos");
    }

    @GetMapping("por/localizacao")
    public ResponseData porLocalizacao(@RouteVar("localizacao") String localizacao) {
        return lista(() -> service.buscarPorLocalizacao(localizacao), "equipamentos");
    }

    @GetMapping("por/departamento")
    public ResponseData porDepartamento(@RouteVar("departamento") String departamento) {
        return lista(() -> service.buscarPorDepartamento(departamento), "equipamentos");
    }

    @GetMapping("por/funcionario")
    public ResponseData porFuncionario(@RouteVar("funcionarioid") String funcionarioId) {
        return lista(() -> service.buscarPorFuncionario(funcionarioId), "equipamentos");
    }

    @GetMapping("por/sku")
    public ResponseData porSku(@RouteVar("sku") String sku) {
        return lista(() -> service.buscarPorSku(sku), "equipamentos");
    }

    @GetMapping("por/periodo-aquisicao")
    public ResponseData porPeriodoAquisicao(@RouteVar("inicio") String inicio,
                                            @RouteVar("fim") String fim) {
        return lista(() -> service.buscarPorPeriodoAquisicao(
                converterDataObrigatoria(inicio, "inicio"),
                converterDataObrigatoria(fim, "fim")), "equipamentos");
    }

    @GetMapping("com-filtros")
    public ResponseData comFiltros(@RouteVar("sku") String sku,
                                   @RouteVar("funcionarioid") String funcionarioId,
                                   @RouteVar("status") String status,
                                   @RouteVar("condicao") String condicao,
                                   @RouteVar("localizacao") String localizacao,
                                   @RouteVar("departamento") String departamento,
                                   @RouteVar("inicio") String inicio,
                                   @RouteVar("fim") String fim,
                                   @RouteVar("mac") String mac) {
        return lista(() -> service.buscarComFiltros(
                sku, funcionarioId, status, condicao,
                localizacao, departamento,
                converterDataOpcional(inicio, "inicio"),
                converterDataOpcional(fim, "fim"),
                mac), "equipamentos");
    }

    // ============================================================
    // RELATÓRIOS / ALERTAS
    // ============================================================

    @GetMapping("criticos")
    public ResponseData criticos() {
        return lista(service::buscarEquipamentosCriticos, "equipamentos");
    }

    @GetMapping("verificacao-atrasada")
    public ResponseData verificacaoAtrasada() {
        return lista(service::buscarEquipamentosVerificacaoAtrasada, "equipamentos");
    }

    @GetMapping("para-verificacao")
    public ResponseData paraVerificacao(@RouteVar("limite") String limite) {
        return lista(() -> service.buscarEquipamentosParaVerificacao(
                converterDataObrigatoria(limite, "limite")), "equipamentos");
    }

    @GetMapping("sem-mac")
    public ResponseData semMac() {
        return lista(service::buscarEquipamentosSemMacAddress, "equipamentos");
    }

    // ============================================================
    // ESTATÍSTICAS
    // ============================================================

    @GetMapping("stats/status")         public ResponseData statsStatus()       { return valor(service::obterEstatisticasPorStatus, "estatisticas"); }
    @GetMapping("stats/condicao")       public ResponseData statsCondicao()     { return valor(service::obterEstatisticasPorCondicao, "estatisticas"); }
    @GetMapping("stats/departamento")   public ResponseData statsDepartamento() { return valor(service::obterEstatisticasPorDepartamento, "estatisticas"); }
    @GetMapping("stats/localizacao")    public ResponseData statsLocalizacao()  { return valor(service::obterEstatisticasPorLocalizacao, "estatisticas"); }
    @GetMapping("stats/ano-aquisicao")  public ResponseData statsAno()          { return valor(service::obterEstatisticasPorAnoAquisicao, "estatisticas"); }
    @GetMapping("stats/prefixo-mac")    public ResponseData statsPrefixoMac()   { return valor(service::obterEstatisticasPorPrefixoMac, "estatisticas"); }

    @GetMapping("total")
    public ResponseData total() {
        return valor(service::contarTotal, "total");
    }

    @GetMapping("total/por/status")
    public ResponseData totalPorStatus(@RouteVar("status") String status) {
        return valor(() -> service.contarPorStatus(status), "total");
    }

    // ============================================================
    // DISTINCT / DOMÍNIOS
    // ============================================================

    @GetMapping("distinct/localizacoes")    public ResponseData localizacoes()   { return lista(service::listarLocalizacoes, "valores"); }
    @GetMapping("distinct/departamentos")   public ResponseData departamentos()  { return lista(service::listarDepartamentos, "valores"); }
    @GetMapping("status-validos")           public ResponseData statusValidos()  { return valor(service::listarStatus, "valores"); }
    @GetMapping("condicoes-validas")        public ResponseData condicoesValidas(){ return valor(service::listarCondicoes, "valores"); }

    // ============================================================
    // VERIFICAÇÕES
    // ============================================================

    @GetMapping("exists/id")
    public ResponseData existePorId(@RouteVar("id") String id) {
        return valor(() -> service.existePorId(id), "exists");
    }

    @GetMapping("exists/serie")
    public ResponseData existeNumSerie(@RouteVar("numserie") String numSerie) {
        return valor(() -> service.numeroSerieExiste(numSerie), "exists");
    }

    @GetMapping("exists/mac")
    public ResponseData existeMac(@RouteVar("mac") String mac) {
        return valor(() -> service.macAddressExiste(mac), "exists");
    }

    // ============================================================
    // HELPERS DE FRONTEIRA
    // ============================================================

    @FunctionalInterface
    private interface Acao<T> { T executar() throws SQLException; }

    private ResponseData lista(Acao<List<?>> acao, String chave) {
        try {
            List<?> dados = acao.executar();
            return ResponseData.success()
                    .withData(chave, dados)
                    .withData("total", dados.size());
        } catch (IllegalArgumentException | IllegalStateException e) {
            return erroNegocio(e);
        } catch (SQLException e) {
            return erroBanco(e);
        }
    }

    private <T> ResponseData valor(Acao<T> acao, String chave) {
        try {
            return ResponseData.success().withData(chave, acao.executar());
        } catch (IllegalArgumentException | IllegalStateException e) {
            return erroNegocio(e);
        } catch (SQLException e) {
            return erroBanco(e);
        }
    }

    private <T> ResponseData optional(Acao<Optional<T>> acao, String chave, String msgVazio) {
        try {
            return acao.executar()
                    .<ResponseData>map(v -> ResponseData.success().withData(chave, v))
                    .orElseGet(() -> ResponseData.error(msgVazio).withError(chave, "não encontrado"));
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

    private void exigirPaginacao(Integer pagina, Integer tamanho) {
        if (pagina == null || pagina < 1 || tamanho == null || tamanho < 1) {
            throw new IllegalArgumentException("Página e tamanho devem ser >= 1");
        }
    }

    // ============================================================
    // CONVERSÕES ISOLADAS
    // ============================================================

    private LocalDate converterDataOpcional(String bruto, String campo) {
        if (bruto == null || bruto.isBlank()) return null;
        return parseData(bruto, campo);
    }

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
}