package com.ossobo.gestaoDepIt.db.services;

import com.ossobo.gestaoDepIt.db.config.event.TonerEvent;
import com.ossobo.gestaoDepIt.db.models.GestaoToners;
import com.ossobo.gestaoDepIt.db.repositories.GestaoTonersRepository;


import com.ossobo.winterfx.anotations.Inject;
import com.ossobo.winterfx.anotations.Service;
import com.ossobo.winterfx.event.EventBus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * GestaoTonersService - Regras de negócio com EventBus
 * v2.0 - Migrado para Java 17+ com WinterFX
 *
 * Responsabilidades:
 * - Gerenciar instalações e substituições de toners
 * - Validar regras de negócio
 * - Publicar eventos (@TonerEvent)
 * - Cálculo de vida útil e estatísticas
 */
@Service
public class GestaoTonersService {

    private static final Logger logger = LoggerFactory.getLogger(GestaoTonersService.class);

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

    public Optional<GestaoToners> buscarPorId(Long id) throws SQLException {
        return repository.findById(id);
    }

    public List<GestaoToners> buscarPorInventario(Long inventarioId) throws SQLException {
        return repository.findByInventarioId(inventarioId);
    }

    public List<GestaoToners> buscarPorSku(String sku) throws SQLException {
        return repository.findBySkuProduto(sku);
    }

    // ===== OPERAÇÕES DE INSTALAÇÃO =====

    public GestaoToners registrarInstalacao(GestaoToners toner) throws SQLException {
        validarToner(toner);

        // Verifica se SKU existe no catálogo
        if (!repository.skuExistsInCatalogo(toner.skuProduto())) {
            throw new IllegalArgumentException("SKU não encontrado no catálogo: " + toner.skuProduto());
        }

        // Verifica se inventário existe
        if (!repository.inventarioExists(toner.inventarioId())) {
            throw new IllegalArgumentException("Equipamento não encontrado: " + toner.inventarioId());
        }

        // Verifica se já existe toner ativo
        Optional<GestaoToners> ativo = repository.findTonerAtivoByInventario(toner.inventarioId());
        if (ativo.isPresent()) {
            logger.warn("Equipamento {} já possui toner ativo (ID: {}). Marcando como substituído.",
                    toner.inventarioId(), ativo.get().id());
        }

        Long id = repository.insert(toner);
        GestaoToners salvo = repository.findById(id)
                .orElseThrow(() -> new SQLException("Falha ao buscar toner instalado"));

        eventBus.publish(new TonerEvent<>(salvo, "INSTALADO"));
        logger.info("✅ Toner instalado: ID={}, Equipamento={}, SKU={}",
                salvo.id(), salvo.inventarioId(), salvo.skuProduto());

        return salvo;
    }

    public GestaoToners atualizar(GestaoToners toner) throws SQLException {
        validarToner(toner);

        if (toner.id() == null) {
            throw new IllegalArgumentException("ID não pode ser nulo para atualização");
        }

        if (!repository.existsById(toner.id())) {
            throw new IllegalArgumentException("Toner não encontrado: " + toner.id());
        }

        repository.update(toner);
        GestaoToners atualizado = repository.findById(toner.id())
                .orElseThrow(() -> new SQLException("Falha ao buscar toner atualizado"));

        eventBus.publish(new TonerEvent<>(atualizado, "ATUALIZADO"));
        logger.info("✅ Toner atualizado: ID={}", atualizado.id());

        return atualizado;
    }

    public void excluir(Long id) throws SQLException {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Toner não encontrado: " + id);
        }

        Optional<GestaoToners> toner = repository.findById(id);
        repository.delete(id);

        toner.ifPresent(t -> {
            eventBus.publish(new TonerEvent<>(t, "EXCLUIDO"));
            logger.info("✅ Toner excluído: ID={}", id);
        });
    }

    // ===== OPERAÇÕES DE SUBSTITUIÇÃO =====

    public GestaoToners substituirToner(Long inventarioId, String novoSku, Long usuarioId, String observacoes)
            throws SQLException {
        logger.info("Substituindo toner do equipamento {} pelo SKU {}", inventarioId, novoSku);

        // Verifica equipamento
        if (!repository.inventarioExists(inventarioId)) {
            throw new IllegalArgumentException("Equipamento não encontrado: " + inventarioId);
        }

        // Verifica SKU
        if (!repository.skuExistsInCatalogo(novoSku)) {
            throw new IllegalArgumentException("SKU não encontrado no catálogo: " + novoSku);
        }

        // Marca toner atual como esgotado
        Optional<GestaoToners> tonerAtual = repository.findTonerAtivoByInventario(inventarioId);
        if (tonerAtual.isPresent()) {
            repository.marcarComoEsgotado(tonerAtual.get().id());
            logger.info("Toner anterior {} marcado como esgotado", tonerAtual.get().id());
        }

        // Cria nova instalação
        GestaoToners novaInstalacao = GestaoToners.substituicao(
                inventarioId, novoSku, usuarioId, observacoes
        );

        Long id = repository.insert(novaInstalacao);
        GestaoToners salvo = repository.findById(id)
                .orElseThrow(() -> new SQLException("Falha ao buscar toner instalado"));

        eventBus.publish(new TonerEvent<>(salvo, "SUBSTITUIDO"));
        logger.info("✅ Toner substituído: ID={}, Equipamento={}, SKU={}",
                salvo.id(), salvo.inventarioId(), salvo.skuProduto());

        return salvo;
    }

    // ===== OPERAÇÕES DE USO =====

    public void registrarUso(Long id, int ciclosUtilizados) throws SQLException {
        if (ciclosUtilizados <= 0) {
            throw new IllegalArgumentException("Ciclos utilizados devem ser positivos");
        }

        Optional<GestaoToners> toner = repository.findById(id);
        if (toner.isEmpty()) {
            throw new IllegalArgumentException("Toner não encontrado: " + id);
        }

        // Calcula redução da percentagem
        int reducao = ciclosUtilizados / 100;
        int novaPercentagem = Math.max(0, toner.get().percentagemRestante() - reducao);

        repository.updatePercentagem(id, novaPercentagem);
        repository.incrementarCiclos(id, ciclosUtilizados);

        // Se esgotou, publica evento
        if (novaPercentagem == 0) {
            Optional<GestaoToners> atualizado = repository.findById(id);
            atualizado.ifPresent(t -> {
                eventBus.publish(new TonerEvent<>(t, "ESGOTADO"));
                logger.info("🔴 Toner esgotado: ID={}, Equipamento={}", id, t.inventarioId());
            });
        } else {
            Optional<GestaoToners> atualizado = repository.findById(id);
            atualizado.ifPresent(t -> {
                eventBus.publish(new TonerEvent<>(t, "USO_REGISTRADO"));
                logger.info("✅ Uso registrado: ID={}, Ciclos=+{}, Percentagem={}%",
                        id, ciclosUtilizados, novaPercentagem);
            });
        }
    }

    public void atualizarPercentagem(Long id, int percentagem) throws SQLException {
        if (percentagem < 0 || percentagem > 100) {
            throw new IllegalArgumentException("Percentagem deve estar entre 0 e 100");
        }

        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Toner não encontrado: " + id);
        }

        repository.updatePercentagem(id, percentagem);

        Optional<GestaoToners> atualizado = repository.findById(id);
        atualizado.ifPresent(t -> {
            eventBus.publish(new TonerEvent<>(t, "PERCENTAGEM_ATUALIZADA"));
            logger.info("✅ Percentagem atualizada: ID={} → {}%", id, percentagem);
        });
    }

    public void marcarComoEsgotado(Long id) throws SQLException {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Toner não encontrado: " + id);
        }

        repository.marcarComoEsgotado(id);

        Optional<GestaoToners> atualizado = repository.findById(id);
        atualizado.ifPresent(t -> {
            eventBus.publish(new TonerEvent<>(t, "ESGOTADO"));
            logger.info("🔴 Toner marcado como esgotado: ID={}", id);
        });
    }

    // ===== CONSULTAS ESPECIALIZADAS =====

    public Optional<GestaoToners> buscarTonerAtivo(Long inventarioId) throws SQLException {
        return repository.findTonerAtivoByInventario(inventarioId);
    }

    public Optional<GestaoToners> buscarUltimoToner(Long inventarioId) throws SQLException {
        return repository.findUltimoByInventario(inventarioId);
    }

    public List<GestaoToners> buscarBaixaPercentagem(int limite) throws SQLException {
        return repository.findBaixaPercentagem(limite);
    }

    public List<GestaoToners> buscarEsgotados() throws SQLException {
        return repository.findEsgotados();
    }

    public List<GestaoToners> buscarPorUsuario(Long usuarioId) throws SQLException {
        return repository.findByUsuarioResponsavel(usuarioId);
    }

    public List<GestaoToners> buscarPorPeriodo(LocalDate inicio, LocalDate fim) throws SQLException {
        return repository.findByPeriodoInstalacao(inicio, fim);
    }

    public List<GestaoToners> buscarComFiltros(
            Long inventarioId,
            String sku,
            Long usuarioId,
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

    public Map<String, Object[]> calcularVidaUtilPorSku() throws SQLException {
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

    public int contarPorInventario(Long inventarioId) throws SQLException {
        return repository.countByInventario(inventarioId);
    }

    // ===== VALIDAÇÕES =====

    public boolean existePorId(Long id) throws SQLException {
        return repository.existsById(id);
    }

    public boolean equipamentoTemTonerAtivo(Long inventarioId) throws SQLException {
        return repository.findTonerAtivoByInventario(inventarioId).isPresent();
    }

    // ===== MÉTODOS PRIVADOS =====

    private void validarToner(GestaoToners t) {
        if (t.inventarioId() == null || t.inventarioId() <= 0) {
            throw new IllegalArgumentException("ID do inventário é obrigatório");
        }
        if (!(t.skuProduto() instanceof String sku) || sku.isBlank()) {
            throw new IllegalArgumentException("SKU do produto é obrigatório");
        }
        if (t.usuarioResponsavel() == null || t.usuarioResponsavel() <= 0) {
            throw new IllegalArgumentException("Usuário responsável é obrigatório");
        }
        if (t.percentagemRestante() < 0 || t.percentagemRestante() > 100) {
            throw new IllegalArgumentException("Percentagem deve estar entre 0 e 100");
        }
        if (t.ciclosImpressao() < 0) {
            throw new IllegalArgumentException("Ciclos de impressão não pode ser negativo");
        }
    }
}