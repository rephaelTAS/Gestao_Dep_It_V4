package com.ossobo.gestaoDepIt.db.services;

import com.ossobo.gestaoDepIt.db.models.InventarioEquipamentos;
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
 * InventarioEquipamentosRoutes v1.0
 *
 * Responsabilidade: Fronteira de Internal Routing do inventário de equipamentos.
 *                   Handlers FINOS: delegam ao InventarioEquipamentosService e
 *                   traduzem exceção → ResponseData.
 *
 * Contratos de payload (documentação viva da API):
 *   Identidade : "id" (Long); alternativas "numserie", "mac", "fatura".
 *   CRUD       : "equipamento" (payload InventarioEquipamentos).
 *   Estado     : baixar → "motivo"; manutencao/* → motivo/condicaopos/observacoes;
 *                verificacao → data (ISO), condicao, observacoes.
 *   Documentos : payloads "documento" (byte[]); devolucao/atualizar → "ativo".
 *   Filtros    : com-filtros aceita 9 chaves OPCIONAIS (omitir = sem critério):
 *                sku, funcionarioid, status, condicao, localizacao,
 *                departamento, inicio/fim (ISO), mac.
 *
 * ⚠️ Backlog registrado (ver conversa): OBS-I1 (reversão de status em
 *    baixa/manutenção — confirmação aguardada no repository), OBS-I2
 *    (retorno de manutenção sem transação), OBS-I3 (obrigatoriedade do MAC ×
 *    rota sem-mac).
 *
 * v1.0 - Criação; 49 rotas (GET 34 / PUT 14 / DELETE 1); maior fronteira
 *        do sistema — corresponde ao domínio central.
 */
@Component
@RequestMapping("inventario-equipamentos/service")
public class InventarioEquipamentosRoutes {

    private static final Logger logger = LoggerFactory.getLogger(InventarioEquipamentosRoutes.class);

    @Inject
    private InventarioEquipamentosService service;

    // ============================================================
    // ROTAS — CONSULTAS BÁSICAS (GET)
    // ============================================================

    /** Todos os equipamentos. */
    @GetMapping("todos")
    public ResponseData todos() {
        return lista(service::listarTodos, "equipamentos");
    }

    /** Lista paginada. Chaves: pagina (1-based), tamanho. */
    @GetMapping("paginados")
    public ResponseData paginados(@RouteVar("pagina") Integer pagina,
                                  @RouteVar("tamanho") Integer tamanho) {
        return lista(() -> {
            exigirPaginacao(pagina, tamanho);
            return service.listarTodos(pagina, tamanho);
        }, "equipamentos");
    }

    /** Busca por ID. Erro semântico se inexistente. */
    @GetMapping("por/id")
    public ResponseData buscarPorId(@RouteVar("id") String id) {
        return optional(() -> service.buscarPorId(id), "equipamento",
                "Equipamento não encontrado: ID " + id);
    }

    /** Busca por número de série (único). */
    @GetMapping("por/num-serie")
    public ResponseData buscarPorNumSerie(@RouteVar("numserie") String numSerie) {
        return optional(() -> service.buscarPorNumSerie(numSerie), "equipamento",
                "Equipamento não encontrado: série '" + numSerie + "'");
    }

    /** Busca por MAC exato (único). */
    @GetMapping("por/mac")
    public ResponseData buscarPorMac(@RouteVar("mac") String mac) {
        return optional(() -> service.buscarPorMacAddress(mac), "equipamento",
                "Equipamento não encontrado: MAC '" + mac + "'");
    }

    /** Busca por padrão LIKE de MAC. */
    @GetMapping("por/mac-like")
    public ResponseData buscarPorMacLike(@RouteVar("padrao") String padrao) {
        return lista(() -> service.buscarPorMacAddressLike(padrao), "equipamentos");
    }

    /** Equipamentos vinculados a uma fatura. */
    @GetMapping("por/fatura")
    public ResponseData buscarPorFatura(@RouteVar("fatura") String numeroFatura) {
        return lista(() -> service.buscarPorNumeroFatura(numeroFatura), "equipamentos");
    }

    // ============================================================
    // ROTAS — FILTROS SIMPLES (GET)
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

    /** Aquisições no período fechado. Chaves ISO obrigatórias: inicio, fim. */
    @GetMapping("por/periodo-aquisicao")
    public ResponseData porPeriodoAquisicao(@RouteVar("inicio") String inicio,
                                            @RouteVar("fim") String fim) {
        return lista(() -> service.buscarPorPeriodoAquisicao(
                converterDataObrigatoria(inicio, "inicio"),
                converterDataObrigatoria(fim, "fim")), "equipamentos");
    }

    /**
     * Filtro combinado — TODAS as chaves OPCIONAIS:
     * sku, funcionarioid, status, condicao, localizacao, departamento,
     * inicio/fim (ISO de aquisição), mac.
     */
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
                        mac),
                "equipamentos");
    }

    // ============================================================
    // ROTAS — RELATÓRIOS E ALERTAS (GET)
    // ============================================================

    /** Equipamentos em estado crítico (critério do domínio). */
    @GetMapping("criticos")
    public ResponseData criticos() {
        return lista(service::buscarEquipamentosCriticos, "equipamentos");
    }

    /** Verificação periódica atrasada. */
    @GetMapping("verificacao-atrasada")
    public ResponseData verificacaoAtrasada() {
        return lista(service::buscarEquipamentosVerificacaoAtrasada, "equipamentos");
    }

    /** Devem passar por verificação até a data-limite. Chave ISO: limite. */
    @GetMapping("para-verificacao")
    public ResponseData paraVerificacao(@RouteVar("limite") String limite) {
        return lista(() -> service.buscarEquipamentosParaVerificacao(
                converterDataObrigatoria(limite, "limite")), "equipamentos");
    }

    /** Sem MAC cadastrado. ⚠️ Backlog OBS-I3: conflito com obrigatoriedade. */
    @GetMapping("sem-mac")
    public ResponseData semMac() {
        return lista(service::buscarEquipamentosSemMacAddress, "equipamentos");
    }

    // ============================================================
    // ROTAS — ESTATÍSTICAS (GET)
    // ============================================================

    @GetMapping("stats/status")
    public ResponseData statsStatus()           { return valor(service::obterEstatisticasPorStatus, "estatisticas"); }

    @GetMapping("stats/condicao")
    public ResponseData statsCondicao()         { return valor(service::obterEstatisticasPorCondicao, "estatisticas"); }

    @GetMapping("stats/departamento")
    public ResponseData statsDepartamento()     { return valor(service::obterEstatisticasPorDepartamento, "estatisticas"); }

    @GetMapping("stats/localizacao")
    public ResponseData statsLocalizacao()      { return valor(service::obterEstatisticasPorLocalizacao, "estatisticas"); }

    /** Distribuição por ano de aquisição. Retorno: Map<Integer, Integer>. */
    @GetMapping("stats/ano-aquisicao")
    public ResponseData statsAnoAquisicao()     { return valor(service::obterEstatisticasPorAnoAquisicao, "estatisticas"); }

    /** Agrupamento por fabricante implícito (prefixo MAC). */
    @GetMapping("stats/prefixo-mac")
    public ResponseData statsPrefixoMac()       { return valor(service::obterEstatisticasPorPrefixoMac, "estatisticas"); }

    @GetMapping("total")
    public ResponseData total()                 { return valor(service::contarTotal, "total"); }

    @GetMapping("total/por/status")
    public ResponseData totalPorStatus(@RouteVar("status") String status) {
        return valor(() -> service.contarPorStatus(status), "total");
    }

    // ============================================================
    // ROTAS — DISTINCT / DOMÍNIOS VÁLIDOS (COMBOBOXES)
    // ============================================================

    @GetMapping("distinct/localizacoes")
    public ResponseData localizacoes()          { return lista(service::listarLocalizacoes, "valores"); }

    @GetMapping("distinct/departamentos")
    public ResponseData departamentos()         { return lista(service::listarDepartamentos, "valores"); }

    /** Status válidos (fonte da verdade: domínio). */
    @GetMapping("status-validos")
    public ResponseData statusValidos()         { return valor(service::listarStatus, "valores"); }

    /** Condições válidas (fonte da verdade: domínio). */
    @GetMapping("condicoes-validas")
    public ResponseData condicoesValidas()      { return valor(service::listarCondicoes, "valores"); }

    // ============================================================
    // ROTAS — VERIFICAÇÕES (GET)
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
    // ROTAS — CRUD (PUT)
    // ============================================================

    /**
     * Cadastra equipamento. Payload: "equipamento".
     * Valida duplicidade de série e MAC no domínio.
     */
    @PutMapping("cadastrar")
    public ResponseData cadastrar(@Payload("equipamento") InventarioEquipamentos equip) {
        return escrita(() -> service.cadastrar(equip), "equipamento");
    }

    /** Atualiza completo. Payload: "equipamento". */
    @PutMapping("atualizar")
    public ResponseData atualizar(@Payload("equipamento") InventarioEquipamentos equip) {
        return escrita(() -> service.atualizar(equip), "equipamento");
    }

    // ============================================================
    // ROTAS — CICLO DE VIDA (PUT)
    // ============================================================

    /** Altera status direto. Chave "valor": veja GET status-validos. */
    @PutMapping("status/atualizar")
    public ResponseData atualizarStatus(@RouteVar("id") String id,
                                        @RouteVar("valor") String novoStatus) {
        return escrita(() -> {
            service.atualizarStatus(id, novoStatus);
            return "Status alterado";
        }, "mensagem");
    }

    /** Altera condição direta. Chave "valor": veja GET condicoes-validas. */
    @PutMapping("condicao/atualizar")
    public ResponseData atualizarCondicao(@RouteVar("id") String id,
                                          @RouteVar("valor") String novaCondicao) {
        return escrita(() -> {
            service.atualizarCondicao(id, novaCondicao);
            return "Condição alterada";
        }, "mensagem");
    }

    /** BAIXA definitiva do equipamento. Chave: motivo. Publica BAIXADO. */
    @PutMapping("baixar")
    public ResponseData baixar(@RouteVar("id") String id,
                               @RouteVar("motivo") String motivo) {
        return escrita(() -> {
            service.baixarEquipamento(id, motivo);
            return "Equipamento baixado";
        }, "mensagem");
    }

    /** Envia para manutenção. Chave: motivo. */
    @PutMapping("manutencao/enviar")
    public ResponseData enviarManutencao(@RouteVar("id") String id,
                                         @RouteVar("motivo") String motivo) {
        return escrita(() -> {
            service.enviarParaManutencao(id, motivo);
            return "Equipamento enviado para manutenção";
        }, "mensagem");
    }

    /**
     * Retorna da manutenção. Chaves: condicaopos (válida), observacoes.
     * ⚠️ Backlog OBS-I2: operação composta sem transação ainda.
     */
    @PutMapping("manutencao/retornar")
    public ResponseData retornarManutencao(@RouteVar("id") String id,
                                           @RouteVar("condicaopos") String condicaoPos,
                                           @RouteVar("observacoes") String observacoes) {
        return escrita(() -> {
            service.retornarDeManutencao(id, condicaoPos, observacoes);
            return "Equipamento retornou da manutenção";
        }, "mensagem");
    }

    /** Transfere de localização/departamento. */
    @PutMapping("localizacao/transferir")
    public ResponseData transferir(@RouteVar("id") String id,
                                   @RouteVar("localizacao") String localizacao,
                                   @RouteVar("departamento") String departamento) {
        return escrita(() -> {
            service.transferirLocalizacao(id, localizacao, departamento);
            return "Transferido para " + localizacao + "/" + departamento;
        }, "mensagem");
    }

    /** Reassocia a outro funcionário responsável. */
    @PutMapping("funcionario/reassociar")
    public ResponseData reassociarFuncionario(@RouteVar("id") String id,
                                              @RouteVar("funcionarioid") String funcionarioId) {
        return escrita(() -> {
            service.reassociarFuncionario(id, funcionarioId);
            return "Responsável atualizado";
        }, "mensagem");
    }

    /** Registra verificação periódica. Chaves: data (ISO), condicao, observacoes. */
    @PutMapping("verificacao/registrar")
    public ResponseData registrarVerificacao(@RouteVar("id") String id,
                                             @RouteVar("data") String data,
                                             @RouteVar("condicao") String condicao,
                                             @RouteVar("observacoes") String observacoes) {
        return escrita(() -> {
            service.registrarVerificacao(id, converterDataOpcional(data, "data"), condicao, observacoes);
            return "Verificação registrada";
        }, "mensagem");
    }

    // ============================================================
    // ROTAS — FATURA, DOCUMENTOS E DEVOLUÇÃO (PUT)
    // ============================================================

    /** Vincula número de fatura. */
    @PutMapping("fatura/atualizar")
    public ResponseData atualizarFatura(@RouteVar("id") String id,
                                        @RouteVar("numero") String numeroFatura) {
        return escrita(() -> {
            service.atualizarNumeroFatura(id, numeroFatura);
            return "Fatura atualizada";
        }, "mensagem");
    }

    /** Anexa/substitui documento de entrega (byte[]). Payload: "documento". */
    @PutMapping("documento/entrega/atualizar")
    public ResponseData atualizarDocEntrega(@RouteVar("id") String id,
                                            @Payload("documento") byte[] documento) {
        return escrita(() -> {
            service.atualizarDocumentoEntrega(id, documento);
            return "Documento de entrega atualizado";
        }, "mensagem");
    }

    /** Anexa/substitui documento de devolução (byte[]). Payload: "documento". */
    @PutMapping("documento/devolucao/atualizar")
    public ResponseData atualizarDocDevolucao(@RouteVar("id") String id,
                                              @Payload("documento") byte[] documento) {
        return escrita(() -> {
            service.atualizarDocumentoDevolucao(id, documento);
            return "Documento de devolução atualizado";
        }, "mensagem");
    }

    /** Marca/desmarca devolução. Chave: ativo ("true"/"false"). */
    @PutMapping("devolucao/atualizar")
    public ResponseData atualizarDevolucao(@RouteVar("id") String id,
                                           @RouteVar("ativo") Boolean devolucao) {
        return escrita(() -> {
            if (devolucao == null) {
                throw new IllegalArgumentException("Chave 'ativo' é obrigatória (true/false)");
            }
            service.atualizarDevolucao(id, devolucao);
            return "Devolução atualizada";
        }, "mensagem");
    }

    // ============================================================
    // ROTAS — REMOÇÃO (DELETE)
    // ============================================================

    /** Exclusão física do equipamento. Publica EXCLUIDO. */
    @DeleteMapping("por/id")
    public ResponseData excluir(@RouteVar("id") String id) {
        return escrita(() -> {
            service.excluir(id);
            return "Equipamento excluído";
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