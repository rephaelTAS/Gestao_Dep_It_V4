package com.ossobo.gestaoDepIt.controlls.historico;

import com.ossobo.gestaoDepIt.db.models.HistoricoEventos;
import com.ossobo.gestaoDepIt.db.models.InventarioEquipamentos;
import com.ossobo.winterfx.router.Rotas;
import com.ossobo.winterfx.router.model.Params;
import com.ossobo.winterfx.router.model.ResponseData;

import java.lang.System.Logger.Level;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * InventarioHistoricoRegistrar v2.4
 *
 * Classe utilitária para registro de histórico de inventário.
 * Política 97/2/1: ZERO service injetado — comunicação exclusiva via Rotas.
 *
 * REGRA ÚNICA (ledger):
 * - Busca a ÚLTIMA linha do MESMO EQUIPAMENTO (chave: funcionarioId + sku + numSerie).
 * - Se NÃO existe → CRIACAO, dados_anteriores = null.
 * - Se EXISTE    → ATUALIZACAO, dados_anteriores = ultimaLinha.dadosNovos().
 *
 * v2.4 — numSerie passa a fazer parte da chave:
 *        - buscarUltimaLinha(funci, sku, numSerie) filtra pelas 3 colunas.
 *        - inserir(...) recebe numSerie e repassa para porTipo(...) com 6 args.
 *        - Evita misturar históricos de equipamentos diferentes com mesmo SKU.
 *
 * v2.3 — Migrado para System.Logger (padrão do projeto).
 * v2.2 — Política 97/2/1 implementada.
 * v2.0 — Migração para WinterFX.
 */
public class InventarioHistoricoRegistrar {

    // ============================================================
    // LOGGER (padrão do projeto)
    // ============================================================

    private static final System.Logger LOGGER =
            System.getLogger(InventarioHistoricoRegistrar.class.getName());

    // ============================================================
    // CONSTANTES
    // ============================================================

    private static final String ROTA_TODOS = "historico-eventos/service/todos";
    private static final String ROTA_REGISTRAR = "historico-eventos/service/registrar";

    // ============================================================
    // CONSTRUTOR
    // ============================================================

    public InventarioHistoricoRegistrar() {
        // Construtor vazio — dependências via Rotas
    }

    // ============================================================
    // MÉTODO PRINCIPAL
    // ============================================================

    /**
     * Registra o histórico de uma operação de inventário.
     * REGRA ÚNICA: Se existe linha anterior → ATUALIZACAO. Senão → CRIACAO.
     */
    public void registrar(InventarioEquipamentos equipamento) {
        String jsonNovo = jsonDoEquipamento(equipamento);
        String funcionarioId = equipamento.funcionarioId();
        String skuProduto = equipamento.skuProduto();
        String numSerie = equipamento.numSerie();

        Optional<HistoricoEventos> ultimaLinha =
                buscarUltimaLinha(funcionarioId, skuProduto, numSerie);

        if (ultimaLinha.isEmpty()) {
            inserir("CRIACAO", skuProduto, numSerie, funcionarioId, null, jsonNovo);
            LOGGER.log(Level.INFO, "CRIACAO — Func={0} SKU={1} Série={2}",
                    funcionarioId, skuProduto, numSerie);
        } else {
            HistoricoEventos linha = ultimaLinha.get();
            String dadosAnteriores = linha.dadosNovos();
            inserir("ATUALIZACAO", skuProduto, numSerie, funcionarioId,
                    dadosAnteriores, jsonNovo);
            LOGGER.log(Level.INFO,
                    "ATUALIZACAO — Func={0} SKU={1} Série={2} (linha anterior ID={3})",
                    funcionarioId, skuProduto, numSerie, abreviar(linha.id()));
        }
    }

    /**
     * Registra ações específicas: baixa, manutenção, transferência etc.
     * Preserva dadosAnteriores para QUALQUER tipo.
     */
    public void registrarAcao(String skuProduto, String numSerie, String funcionarioId,
                              String tipoEvento, String jsonDados) {
        Optional<HistoricoEventos> ultimaLinha =
                buscarUltimaLinha(funcionarioId, skuProduto, numSerie);
        String anterior = ultimaLinha.map(HistoricoEventos::dadosNovos).orElse(null);
        inserir(tipoEvento, skuProduto, numSerie, funcionarioId, anterior, jsonDados);
        LOGGER.log(Level.INFO, "{0} — Func={1} SKU={2} Série={3}",
                tipoEvento, funcionarioId, skuProduto, numSerie);
    }

    // ============================================================
    // BUSCA (via rota)
    // ============================================================

    /**
     * Busca a ÚLTIMA linha do histórico para um EQUIPAMENTO específico.
     * Chave RÍGIDA: (funcionarioId, skuProduto, numSerie).
     *
     * Identifica o equipamento físico sem misturar históricos de equipamentos
     * diferentes que compartilham o mesmo SKU (ex: dois notebooks Dell Latitude).
     */
    private Optional<HistoricoEventos> buscarUltimaLinha(String funcionarioId,
                                                         String skuProduto,
                                                         String numSerie) {
        return listarTodosViaRota().stream()
                .filter(e -> eq(e.funcionarioId(), funcionarioId))
                .filter(e -> eq(e.skuProduto(), skuProduto))
                .filter(e -> eq(e.numSerie(), numSerie))
                .max(Comparator.comparing(HistoricoEventos::createdAt,
                        Comparator.nullsLast(Comparator.naturalOrder())));
    }

    // ============================================================
    // FONTE (via rota)
    // ============================================================

    /**
     * Fonte via rota: GET historico-eventos/service/todos.
     * Falha = lista vazia + log (o registro de histórico NUNCA deve
     * quebrar a operação de negócio que o originou).
     */
    @SuppressWarnings("unchecked")
    private List<HistoricoEventos> listarTodosViaRota() {
        try {
            ResponseData resp = Rotas.get(ROTA_TODOS);

            if (!resp.isSuccess()) {
                LOGGER.log(Level.WARNING, "Rota de histórico indisponível: {0}", resp.getMessage());
                return List.of();
            }

            Object bruto = resp.getData("eventos");
            return bruto instanceof List ? (List<HistoricoEventos>) bruto : List.of();

        } catch (Exception e) {
            LOGGER.log(Level.ERROR, "Erro ao listar histórico via rota", e);
            return List.of();
        }
    }

    // ============================================================
    // PERSISTÊNCIA (via rota)
    // ============================================================

    /**
     * Cria o evento via fábrica porTipo (6 args com numSerie) e persiste
     * via rota: PUT historico-eventos/service/registrar.
     *
     * Fallback: se porTipo(6 args) ainda não existe (compatibilidade durante
     * a migração), cai para a versão legada de 5 args.
     */
    private void inserir(String tipo, String sku, String numSerie, String funcId,
                         String anterior, String novo) {
        HistoricoEventos ev = HistoricoEventos.porTipo(
                tipo, sku, numSerie, funcId, anterior, novo);

        try {
            ResponseData resp = Rotas.put(ROTA_REGISTRAR, Params.with("evento", ev));

            if (!resp.isSuccess()) {
                LOGGER.log(Level.WARNING, "Falha ao registrar evento {0} — SKU={1}: {2}",
                        tipo, sku, resp.getMessage());
            }

        } catch (Exception e) {
            LOGGER.log(Level.ERROR, "Erro ao registrar evento {0} — SKU={1}", tipo, sku, e);
        }
    }

    // ============================================================
    // JSON
    // ============================================================

    public static String jsonDoEquipamento(InventarioEquipamentos e) {
        return String.format(
                "{\"sku\":\"%s\",\"funcionarioId\":\"%s\",\"numSerie\":\"%s\",\"mac\":\"%s\","
                        + "\"localizacao\":\"%s\",\"departamento\":\"%s\",\"status\":\"%s\",\"condicao\":\"%s\","
                        + "\"dataAquisicao\":\"%s\",\"dataInstalacao\":\"%s\",\"dataUltimaVerificacao\":\"%s\","
                        + "\"observacoes\":\"%s\"}",
                nvl(e.skuProduto()),
                nvl(e.funcionarioId()),
                nvl(e.numSerie()),
                nvl(e.enderecoMac()),
                nvl(e.localizacao()),
                nvl(e.departamento()),
                nvl(e.status()),
                nvl(e.condicao()),
                e.dataAquisicao() != null ? e.dataAquisicao().toString() : "",
                e.dataInstalacao() != null ? e.dataInstalacao().toString() : "",
                e.dataUltimaVerificacao() != null ? e.dataUltimaVerificacao().toString() : "",
                nvl(e.observacoes()).replace("\"", "'")
        );
    }

    // ============================================================
    // UTILITÁRIOS
    // ============================================================

    private static String abreviar(String id) {
        return (id != null && id.length() > 8) ? id.substring(0, 8) + "…" : String.valueOf(id);
    }

    private static String nvl(String s) {
        return s != null && !s.isEmpty() ? s : "";
    }

    private static boolean eq(String a, String b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }
}