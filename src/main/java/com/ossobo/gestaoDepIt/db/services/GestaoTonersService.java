package com.ossobo.gestaoDepIt.db.services;

import com.ossobo.gestaoDepIt.db.config.event.TonerEvent;
import com.ossobo.gestaoDepIt.db.models.GestaoToners;
import com.ossobo.gestaoDepIt.db.repositories.GestaoTonersRepository;
import com.ossobo.gestaoDepIt.db.repositories.GestaoTonersRepository.VidaUtilSku;

import com.ossobo.winterfx.anotations.Inject;
import com.ossobo.winterfx.anotations.Service;
import com.ossobo.winterfx.event.EventBus;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * GestaoTonersService - Regras de negócio com EventBus
 * v2.2 - Alinhado ao GestaoToners v3.0 (PK/FKs String/UUID + colunas de sync)
 *
 * Mudanças v2.1 → v2.2:
 * - substituirToner(): a fábrica GestaoToners.substituicao(...) não existe no
 *   model v3.0 (fonte da verdade). Trocada por novaInstalacaoComObservacoes(...)
 *   — mesma assinatura de 4 argumentos, mesma semântica de "nova instalação".
 *
 * Mudanças v2.0 → v2.1 (mantidas):
 * - TODAS as assinaturas com id/FK: Long → String (UUID v4)
 * - validarToner() simplificado: o record JÁ valida no construtor
 * - registrarInstalacao(): BUG corrigido — o toner anterior agora É marcado
 *   como esgotado (antes só logava "Marcando como substituído")
 * - excluir(): agora usa tombstone (marcarDeletado) para convergir no sync
 * - calcularVidaUtilPorSku(): repassa List<VidaUtilSku>
 * - requireToner(): helper que elimina findById→throw repetido
 */
@Service
public class GestaoTonersService {

    private static final System.Logger logger = System.getLogger(GestaoTonersService.class.getName());

    @Inject
    private GestaoTonersRepository repository;

    @Inject
    private EventBus eventBus;

    // ===== CRUD =====

    public List<GestaoToners> listarTodos() throws SQLException {
        return repository.findAll();
    }

    public List<GestaoToners> listarTodos(int pagina, int tamanho) throws SQLException {
        int offset = (pagina - 1) * tamanho;
        return repository.findAll(tamanho, offset);
    }

    public Optional<GestaoToners> buscarPorId(String id) throws SQLException {
        return repository.findById(id);
    }

    public List<GestaoToners> buscarPorInventario(String inventarioId) throws SQLException {
        return repository.findByInventarioId(inventarioId);
    }

    public List<GestaoToners> buscarPorSku(String sku) throws SQLException {
        return repository.findBySkuProduto(sku);
    }

    // ===== OPERAÇÕES DE INSTALAÇÃO =====

    public GestaoToners registrarInstalacao(GestaoToners toner) throws SQLException {
        validarToner(toner);

        if (!repository.skuExistsInCatalogo(toner.skuProduto())) {
            throw new IllegalArgumentException("SKU não encontrado no catálogo: " + toner.skuProduto());
        }

        if (!repository.inventarioExists(toner.inventarioId())) {
            throw new IllegalArgumentException("Equipamento não encontrado: " + toner.inventarioId());
        }

        // Toner ativo anterior é esgotado para não haver dois ativos no mesmo equipamento
        Optional<GestaoToners> ativo = repository.findTonerAtivoByInventario(toner.inventarioId());
        if (ativo.isPresent()) {
            repository.marcarComoEsgotado(ativo.get().id());
            logger.log(System.Logger.Level.INFO,
                    "Toner anterior {0} (SKU {1}) marcado como esgotado pela nova instalação",
                    ativo.get().id(), ativo.get().skuProduto());
        }

        String id = repository.insert(toner);
        GestaoToners salvo = requireToner(id);

        eventBus.publish(new TonerEvent<>(salvo, "INSTALADO"));
        logger.log(System.Logger.Level.INFO, "✅ Toner instalado: ID={0}, Equipamento={1}, SKU={2}",
                salvo.id(), salvo.inventarioId(), salvo.skuProduto());

        return salvo;
    }

    public GestaoToners atualizar(GestaoToners toner) throws SQLException {
        validarToner(toner);

        if (toner.id() == null || toner.id().isBlank()) {
            throw new IllegalArgumentException("ID não pode ser vazio para atualização");
        }
        requireToner(toner.id());

        repository.update(toner);
        GestaoToners atualizado = requireToner(toner.id());

        eventBus.publish(new TonerEvent<>(atualizado, "ATUALIZADO"));
        logger.log(System.Logger.Level.INFO, "✅ Toner atualizado: ID={0}", atualizado.id());

        return atualizado;
    }

    /**
     * Exclusão LÓGICA (tombstone de sync): o registro permanece com
     * deleted = 1 e updatedAt novo — todos os devices convergem.
     * Para DELETE físico (limpeza administrativa), use o repository diretamente.
     */
    public void excluir(String id) throws SQLException {
        GestaoToners toner = requireToner(id);
        repository.marcarDeletado(id);

        eventBus.publish(new TonerEvent<>(toner, "EXCLUIDO"));
        logger.log(System.Logger.Level.INFO, "✅ Toner excluído (tombstone): ID={0}", id);
    }

    // ===== OPERAÇÕES DE SUBSTITUIÇÃO =====

    /**
     * Substitui o toner ativo do equipamento por um novo SKU.
     *
     * Usa {@link GestaoToners#novaInstalacaoComObservacoes(String, String, String, String)}
     * — fábrica do model (fonte da verdade) com assinatura idêntica à antiga
     * "substituicao(...)" que nunca existiu no record v3.0.
     */
    public GestaoToners substituirToner(String inventarioId, String novoSku,
                                        String usuarioId, String observacoes) throws SQLException {
        logger.log(System.Logger.Level.INFO,
                "Substituindo toner do equipamento {0} pelo SKU {1}", inventarioId, novoSku);

        if (!repository.inventarioExists(inventarioId)) {
            throw new IllegalArgumentException("Equipamento não encontrado: " + inventarioId);
        }

        if (!repository.skuExistsInCatalogo(novoSku)) {
            throw new IllegalArgumentException("SKU não encontrado no catálogo: " + novoSku);
        }

        // Marca toner atual como esgotado antes de instalar o novo
        Optional<GestaoToners> tonerAtual = repository.findTonerAtivoByInventario(inventarioId);
        if (tonerAtual.isPresent()) {
            repository.marcarComoEsgotado(tonerAtual.get().id());
            logger.log(System.Logger.Level.INFO,
                    "Toner anterior {0} marcado como esgotado", tonerAtual.get().id());
        }

        // Fábrica do model — gera UUID + timestamps + percentagem=100 + ciclos=0
        GestaoToners novaInstalacao = GestaoToners.novaInstalacaoComObservacoes(
                inventarioId, novoSku, usuarioId, observacoes
        );

        String id = repository.insert(novaInstalacao);
        GestaoToners salvo = requireToner(id);

        eventBus.publish(new TonerEvent<>(salvo, "SUBSTITUIDO"));
        logger.log(System.Logger.Level.INFO, "✅ Toner substituído: ID={0}, Equipamento={1}, SKU={2}",
                salvo.id(), salvo.inventarioId(), salvo.skuProduto());

        return salvo;
    }

    // ===== OPERAÇÕES DE USO =====

    /**
     * Registra uso do toner em ciclos de impressão.
     *
     * Conversão: 100 ciclos = 1% de percentagem (divisão inteira — usos
     * menores que 100 ciclos não reduzem a percentagem). Para granularidade
     * fina, migre a percentagem para double ou use ciclos como métrica.
     */
    public void registrarUso(String id, int ciclosUtilizados) throws SQLException {
        if (ciclosUtilizados <= 0) {
            throw new IllegalArgumentException("Ciclos utilizados devem ser positivos");
        }

        GestaoToners toner = requireToner(id);

        int reducao = ciclosUtilizados / 100;
        int novaPercentagem = Math.max(0,
                (toner.percentagemRestante() != null ? toner.percentagemRestante() : 100) - reducao);

        repository.updatePercentagem(id, novaPercentagem);
        repository.incrementarCiclos(id, ciclosUtilizados);

        GestaoToners atualizado = requireToner(id);
        if (novaPercentagem == 0) {
            eventBus.publish(new TonerEvent<>(atualizado, "ESGOTADO"));
            logger.log(System.Logger.Level.INFO, "🔴 Toner esgotado: ID={0}, Equipamento={1}",
                    id, atualizado.inventarioId());
        } else {
            eventBus.publish(new TonerEvent<>(atualizado, "USO_REGISTRADO"));
            logger.log(System.Logger.Level.INFO, "✅ Uso registrado: ID={0}, Ciclos=+{1}, Percentagem={2}%",
                    id, ciclosUtilizados, novaPercentagem);
        }
    }

    public void atualizarPercentagem(String id, int percentagem) throws SQLException {
        if (percentagem < 0 || percentagem > 100) {
            throw new IllegalArgumentException("Percentagem deve estar entre 0 e 100");
        }

        requireToner(id);

        repository.updatePercentagem(id, percentagem);
        GestaoToners atualizado = requireToner(id);

        eventBus.publish(new TonerEvent<>(atualizado, "PERCENTAGEM_ATUALIZADA"));
        logger.log(System.Logger.Level.INFO, "✅ Percentagem atualizada: ID={0} → {1}%", id, percentagem);
    }

    public void marcarComoEsgotado(String id) throws SQLException {
        requireToner(id);

        repository.marcarComoEsgotado(id);
        GestaoToners atualizado = requireToner(id);

        eventBus.publish(new TonerEvent<>(atualizado, "ESGOTADO"));
        logger.log(System.Logger.Level.INFO, "🔴 Toner marcado como esgotado: ID={0}", id);
    }

    // ===== CONSULTAS ESPECIALIZADAS =====

    public Optional<GestaoToners> buscarTonerAtivo(String inventarioId) throws SQLException {
        return repository.findTonerAtivoByInventario(inventarioId);
    }

    public Optional<GestaoToners> buscarUltimoToner(String inventarioId) throws SQLException {
        return repository.findUltimoByInventario(inventarioId);
    }

    public List<GestaoToners> buscarBaixaPercentagem(int limite) throws SQLException {
        return repository.findBaixaPercentagem(limite);
    }

    public List<GestaoToners> buscarEsgotados() throws SQLException {
        return repository.findEsgotados();
    }

    public List<GestaoToners> buscarPorUsuario(String usuarioId) throws SQLException {
        return repository.findByUsuarioResponsavel(usuarioId);
    }

    public List<GestaoToners> buscarPorPeriodo(LocalDate inicio, LocalDate fim) throws SQLException {
        return repository.findByPeriodoInstalacao(inicio, fim);
    }

    public List<GestaoToners> buscarComFiltros(
            String inventarioId,
            String sku,
            String usuarioId,
            LocalDate dataInicio,
            LocalDate dataFim,
            Integer percentagemMin,
            Integer percentagemMax
    ) throws SQLException {
        return repository.findWithFilters(inventarioId, sku, usuarioId,
                dataInicio, dataFim, percentagemMin, percentagemMax);
    }

    public List<GestaoToners> buscarParaSubstituicao(int percentagemAlerta) throws SQLException {
        return repository.findParaSubstituicao(percentagemAlerta);
    }

    // ===== ESTATÍSTICAS E RELATÓRIOS =====

    /** Tipo forte — consumidores usam r.sku(), r.mediaCiclos() etc. */
    public List<VidaUtilSku> calcularVidaUtilPorSku() throws SQLException {
        return repository.calcularVidaUtilPorSku();
    }

    public Map<String, Integer> obterEstatisticasUso(LocalDate inicio, LocalDate fim) throws SQLException {
        return repository.getEstatisticasUso(inicio, fim);
    }

    public Map<String, Integer> obterInstalacoesPorMes(int ano) throws SQLException {
        return repository.countInstalacoesPorMes(ano);
    }

    public int contarTotal() throws SQLException {
        return repository.countAll();
    }

    public int contarAtivos() throws SQLException {
        return repository.countAtivos();
    }

    public int contarEsgotados() throws SQLException {
        return repository.countEsgotados();
    }

    public int contarPorInventario(String inventarioId) throws SQLException {
        return repository.countByInventario(inventarioId);
    }

    // ===== VALIDAÇÕES =====

    public boolean existePorId(String id) throws SQLException {
        return repository.existsById(id);
    }

    public boolean equipamentoTemTonerAtivo(String inventarioId) throws SQLException {
        return repository.findTonerAtivoByInventario(inventarioId).isPresent();
    }

    // ===== MÉTODOS PRIVADOS =====

    /**
     * Busca o toner ou lança — elimina o padrão findById→isEmpty→throw
     * que se repetia em ~8 métodos.
     */
    private GestaoToners requireToner(String id) throws SQLException {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("ID do toner é obrigatório");
        }
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Toner não encontrado: " + id));
    }

    /**
     * Defesa em profundidade: o construtor do record JÁ valida tudo isto.
     * Mantido para dar a mensagem de negócio certa antes de validar
     * existência de SKU/inventário (falha rápida, sem bater no banco).
     */
    private void validarToner(GestaoToners t) {
        if (t == null) {
            throw new IllegalArgumentException("Toner não pode ser nulo");
        }
        if (t.inventarioId() == null || t.inventarioId().isBlank()) {
            throw new IllegalArgumentException("ID do inventário é obrigatório");
        }
        if (t.skuProduto() == null || t.skuProduto().isBlank()) {
            throw new IllegalArgumentException("SKU do produto é obrigatório");
        }
        if (t.usuarioResponsavel() == null || t.usuarioResponsavel().isBlank()) {
            throw new IllegalArgumentException("Usuário responsável é obrigatório");
        }
        if (t.percentagemRestante() != null
                && (t.percentagemRestante() < 0 || t.percentagemRestante() > 100)) {
            throw new IllegalArgumentException("Percentagem deve estar entre 0 e 100");
        }
        if (t.ciclosImpressao() != null && t.ciclosImpressao() < 0) {
            throw new IllegalArgumentException("Ciclos de impressão não pode ser negativo");
        }
    }
}