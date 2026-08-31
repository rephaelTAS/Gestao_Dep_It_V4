package com.ossobo.gestaoDepIt.db.services;

import com.ossobo.gestaoDepIt.db.models.GestaoToners;
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
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * GestaoTonersRoutes v1.0
 *
 * Responsabilidade: Fronteira de Internal Routing da gestão de toners.
 *                   Handlers FINOS: delegam ao GestaoTonersService e traduzem
 *                   exceção → ResponseData.
 *
 * Contratos de payload (documentação viva da API):
 *   CRUD       : "toner" (payload GestaoToners).
 *   Identidade : "id" (Long); por equipamento "inventarioid"; produto "sku".
 *   Uso        : PUT usar → id + ciclos (Integer > 0);
 *                PUT percentagem/atualizar → id + valor (0–100).
 *   Filtros    : com-filtros aceita 7 chaves OPCIONAIS — omitir = sem filtro:
 *                inventarioid, sku, usuarioid, inicio/fim (ISO),
 *                percmin/percmax (Integer 0–100).
 *   Períodos   : "inicio"/"fim" ISO obrigatórios onde aplicável; "ano" Integer.
 *
 * v1.0 - Criação; 28 rotas (GET 21 / PUT 5 / DELETE 1); datas String ISO
 *        convertidas isoladamente (padrão EstoqueMovimentacoesRoutes).
 */
@Component
@RequestMapping("gestao-toners/service")
public class GestaoTonersRoutes {

    private static final Logger logger = LoggerFactory.getLogger(GestaoTonersRoutes.class);

    @Inject
    private GestaoTonersService service;

    // ============================================================
    // ROTAS — CONSULTAS BÁSICAS (GET)
    // ============================================================

    /** Todos os registros de toner. */
    @GetMapping("todos")
    public ResponseData todos() {
        return lista(service::listarTodos, "toners");
    }

    /** Lista paginada. Chaves: pagina (1-based), tamanho. */
    @GetMapping("paginados")
    public ResponseData paginados(@RouteVar("pagina") Integer pagina,
                                  @RouteVar("tamanho") Integer tamanho) {
        return lista(() -> {
            exigirPaginacao(pagina, tamanho);
            return service.listarTodos(pagina, tamanho);
        }, "toners");
    }

    /** Busca por ID. Erro semântico se inexistente. */
    @GetMapping("por/id")
    public ResponseData buscarPorId(@RouteVar("id") Long id) {
        return optional(() -> service.buscarPorId(id), "toner",
                "Toner não encontrado: ID " + id);
    }

    /** Histórico de toners de um equipamento. */
    @GetMapping("por/inventario")
    public ResponseData porInventario(@RouteVar("inventarioid") Long inventarioId) {
        return lista(() -> service.buscarPorInventario(inventarioId), "toners");
    }

    /** Histórico de toners por SKU de produto. */
    @GetMapping("por/sku")
    public ResponseData porSku(@RouteVar("sku") String sku) {
        return lista(() -> service.buscarPorSku(sku), "toners");
    }

    // ============================================================
    // ROTAS — ESTADO ATUAL DO EQUIPAMENTO (GET)
    // ============================================================

    /** Toner atualmente ativo no equipamento (null se nenhum). */
    @GetMapping("ativo/por/inventario")
    public ResponseData tonerAtivo(@RouteVar("inventarioid") Long inventarioId) {
        return optionalSemErro(() -> service.buscarTonerAtivo(inventarioId), "toner");
    }

    /** Último registro de toner do equipamento (qualquer status). */
    @GetMapping("ultimo/por/inventario")
    public ResponseData ultimoToner(@RouteVar("inventarioid") Long inventarioId) {
        return optionalSemErro(() -> service.buscarUltimoToner(inventarioId), "toner");
    }

    /** true se o equipamento possui toner ativo instalado. */
    @GetMapping("tem-ativo")
    public ResponseData temAtivo(@RouteVar("inventarioid") Long inventarioId) {
        return valor(() -> service.equipamentoTemTonerAtivo(inventarioId), "temativo");
    }

    // ============================================================
    // ROTAS — ALERTAS E ESPECIALIZADAS (GET)
    // ============================================================

    /** Toners com percentagem abaixo do limite. Chave: limite (0–100). */
    @GetMapping("baixa-percentagem")
    public ResponseData baixaPercentagem(@RouteVar("limite") Integer limite) {
        return lista(() -> {
            exigirIntervalo(limite, 0, 100, "limite");
            return service.buscarBaixaPercentagem(limite);
        }, "toners");
    }

    /** Toners esgotados. */
    @GetMapping("esgotados")
    public ResponseData esgotados() {
        return lista(service::buscarEsgotados, "toners");
    }

    /** Toners por usuário responsável. */
    @GetMapping("por/usuario")
    public ResponseData porUsuario(@RouteVar("usuarioid") Long usuarioId) {
        return lista(() -> service.buscarPorUsuario(usuarioId), "toners");
    }

    /** Instalações por período fechado. Chaves ISO: inicio, fim. */
    @GetMapping("por/periodo")
    public ResponseData porPeriodo(@RouteVar("inicio") String inicio,
                                   @RouteVar("fim") String fim) {
        return lista(() -> service.buscarPorPeriodo(
                converterDataObrigatoria(inicio, "inicio"),
                converterDataObrigatoria(fim, "fim")), "toners");
    }

    /**
     * Filtro combinado — TODAS as chaves OPCIONAIS:
     * inventarioid, sku, usuarioid, inicio/fim (ISO), percmin/percmax (0–100).
     */
    @GetMapping("com-filtros")
    public ResponseData comFiltros(@RouteVar("inventarioid") Long inventarioId,
                                   @RouteVar("sku") String sku,
                                   @RouteVar("usuarioid") Long usuarioId,
                                   @RouteVar("inicio") String inicio,
                                   @RouteVar("fim") String fim,
                                   @RouteVar("percmin") Integer percMin,
                                   @RouteVar("percmax") Integer percMax) {
        if (percMin != null) exigirIntervalo(percMin, 0, 100, "percmin");
        if (percMax != null) exigirIntervalo(percMax, 0, 100, "percmax");

        return lista(() -> service.buscarComFiltros(
                        inventarioId, sku, usuarioId,
                        converterDataOpcional(inicio, "inicio"),
                        converterDataOpcional(fim, "fim"),
                        percMin, percMax),
                "toners");
    }

    /** Toners candidatos à substituição dado o nível de alerta. */
    @GetMapping("para-substituicao")
    public ResponseData paraSubstituicao(@RouteVar("alerta") Integer alerta) {
        return lista(() -> {
            exigirIntervalo(alerta, 0, 100, "alerta");
            return service.buscarParaSubstituicao(alerta);
        }, "toners");
    }

    // ============================================================
    // ROTAS — ESTATÍSTICAS (GET)
    // ============================================================

    /** Vida útil média por SKU. Retorno: Map<String, Object[]>. */
    @GetMapping("stats/vida-util")
    public ResponseData vidaUtilPorSku() {
        return valor(service::calcularVidaUtilPorSku, "vidautil");
    }

    /** Estatísticas de uso no período. Chaves ISO: inicio, fim. */
    @GetMapping("stats/uso")
    public ResponseData statsUso(@RouteVar("inicio") String inicio,
                                 @RouteVar("fim") String fim) {
        return valor(() -> service.obterEstatisticasUso(
                converterDataObrigatoria(inicio, "inicio"),
                converterDataObrigatoria(fim, "fim")), "estatisticas");
    }

    /** Instalações agrupadas por mês do ano informado. */
    @GetMapping("stats/instalacoes-por-mes")
    public ResponseData instalacoesPorMes(@RouteVar("ano") Integer ano) {
        return valor(() -> {
            if (ano == null || ano < 1900 || ano > 2200) {
                throw new IllegalArgumentException("Ano inválido: " + ano);
            }
            return service.obterInstalacoesPorMes(ano);
        }, "estatisticas");
    }

    @GetMapping("total")
    public ResponseData total()                 { return valor(service::contarTotal, "total"); }

    @GetMapping("total/ativos")
    public ResponseData totalAtivos()           { return valor(service::contarAtivos, "total"); }

    @GetMapping("total/esgotados")
    public ResponseData totalEsgotados()        { return valor(service::contarEsgotados, "total"); }

    @GetMapping("total/por/inventario")
    public ResponseData totalPorInventario(@RouteVar("inventarioid") Long inventarioId) {
        return valor(() -> service.contarPorInventario(inventarioId), "total");
    }

    @GetMapping("exists/id")
    public ResponseData existePorId(@RouteVar("id") Long id) {
        return valor(() -> service.existePorId(id), "exists");
    }

    // ============================================================
    // ROTAS — MUTAÇÕES (PUT)
    // ============================================================

    /**
     * Instala toner. Payload: "toner".
     * ⚠️ Backlog OBS-T1: presença de toner ativo prévio apenas gera warning hoje.
     */
    @PutMapping("instalar")
    public ResponseData instalar(@Payload("toner") GestaoToners toner) {
        return escrita(() -> service.registrarInstalacao(toner), "toner");
    }

    /** Atualiza registro. Payload: "toner". Publica TonerEvent ATUALIZADO. */
    @PutMapping("atualizar")
    public ResponseData atualizar(@Payload("toner") GestaoToners toner) {
        return escrita(() -> service.atualizar(toner), "toner");
    }

    /**
     * Registra consumo de ciclos e reduz percentagem automaticamente
     * (redução = ciclos/100). Publica USO_REGISTRADO ou ESGOTADO ao zerar.
     */
    @PutMapping("usar")
    public ResponseData usar(@RouteVar("id") Long id,
                             @RouteVar("ciclos") Integer ciclos) {
        return escrita(() -> {
            service.registrarUso(id, exigirInt(ciclos, "ciclos"));
            return "Uso registrado";
        }, "mensagem");
    }

    /** Define percentagem absoluta (0–100). */
    @PutMapping("percentagem/atualizar")
    public ResponseData atualizarPercentagem(@RouteVar("id") Long id,
                                             @RouteVar("valor") Integer valor) {
        return escrita(() -> {
            service.atualizarPercentagem(id, exigirInt(valor, "valor"));
            return "Percentagem atualizada";
        }, "mensagem");
    }

    /** Marca toner como esgotado (percentagem 0). */
    @PutMapping("esgotar")
    public ResponseData esgotar(@RouteVar("id") Long id) {
        return escrita(() -> {
            service.marcarComoEsgotado(id);
            return "Toner marcado como esgotado";
        }, "mensagem");
    }

    // ============================================================
    // ROTAS — REMOÇÃO (DELETE)
    // ============================================================

    /** Exclui registro de toner. Publica TonerEvent EXCLUIDO. */
    @DeleteMapping("por/id")
    public ResponseData excluir(@RouteVar("id") Long id) {
        return escrita(() -> {
            service.excluir(id);
            return "Toner excluído";
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

    private int exigirInt(Integer valor, String campo) {
        if (valor == null) {
            throw new IllegalArgumentException("Parâmetro obrigatório: " + campo);
        }
        return valor;
    }

    private void exigirPaginacao(Integer pagina, Integer tamanho) {
        if (pagina == null || pagina < 1 || tamanho == null || tamanho < 1) {
            throw new IllegalArgumentException("Página e tamanho devem ser >= 1");
        }
    }

    private void exigirIntervalo(Integer valor, int min, int max, String campo) {
        if (valor == null || valor < min || valor > max) {
            throw new IllegalArgumentException(
                    campo + " deve estar entre " + min + " e " + max);
        }
    }

    // ============================================================
    // CONVERSÕES ISOLADAS
    // ============================================================

    private LocalDate converterDataOpcional(String bruto, String campo) {
        if (bruto == null || bruto.isBlank()) {
            return null;
        }
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