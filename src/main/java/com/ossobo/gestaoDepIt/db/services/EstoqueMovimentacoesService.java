package com.ossobo.gestaoDepIt.db.services;

import com.ossobo.gestaoDepIt.db.config.event.EstoqueEvent;
import com.ossobo.gestaoDepIt.db.enums.TipoMovimentacao;
import com.ossobo.gestaoDepIt.db.models.EstoqueMovimentacoes;
import com.ossobo.gestaoDepIt.db.repositories.EstoqueMovimentacoesRepository;


import com.ossobo.winterfx.anotations.Inject;
import com.ossobo.winterfx.anotations.Service;
import com.ossobo.winterfx.event.EventBus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

/**
 * EstoqueMovimentacoesService - Regras de negócio com EventBus
 * v2.0 - Migrado para Java 17+ com WinterFX
 *
 * Responsabilidades:
 * - Validar movimentações de estoque
 * - Publicar eventos (@EstoqueEvent)
 * - Orquestrar operações de entrada/saída/ajuste/reserva
 * - Calcular saldo e estatísticas
 */
@Service
public class EstoqueMovimentacoesService {

    private static final Logger logger = LoggerFactory.getLogger(EstoqueMovimentacoesService.class);

    @Inject
    private EstoqueMovimentacoesRepository repository;

    @Inject
    private EventBus eventBus;

    // ===== CRUD =====

    public List<EstoqueMovimentacoes> listarTodos() throws SQLException {
        return repository.findAll();
    }

    public Optional<EstoqueMovimentacoes> buscarPorId(Long id) throws SQLException {
        return repository.findById(id);
    }

    public List<EstoqueMovimentacoes> buscarPorSku(String sku) throws SQLException {
        return repository.findBySkuProduto(sku);
    }

    public List<EstoqueMovimentacoes> buscarPorTipo(TipoMovimentacao tipo) throws SQLException {
        return repository.findByTipo(tipo);
    }

    public List<EstoqueMovimentacoes> buscarPorPeriodo(LocalDate inicio, LocalDate fim) throws SQLException {
        return repository.findByPeriodo(inicio, fim);
    }

    public List<EstoqueMovimentacoes> buscarPorFuncionario(String codDep) throws SQLException {
        return repository.findByFuncionario(codDep);
    }

    public List<EstoqueMovimentacoes> buscarProximosVencimento(LocalDate dataLimite) throws SQLException {
        return repository.findProximosVencimento(dataLimite);
    }

    public List<EstoqueMovimentacoes> buscarVencidos() throws SQLException {
        return repository.findVencidos();
    }

    public List<EstoqueMovimentacoes> buscarLicencaProximaVencimento(int dias) throws SQLException {
        return repository.findLicencaProximaVencimento(dias);
    }

    // ===== OPERAÇÕES DE MOVIMENTAÇÃO =====

    public EstoqueMovimentacoes registrarEntrada(
            String sku,
            int quantidade,
            String lote,
            LocalDate dataValidade,
            String localizacao,
            String codDepFuncionario,
            String motivo,
            String observacoes
    ) throws SQLException {
        // Validações
        if (!repository.skuExistsInCatalogo(sku)) {
            throw new IllegalArgumentException("SKU não existe no catálogo: " + sku);
        }
        if (!repository.funcionarioExists(codDepFuncionario)) {
            throw new IllegalArgumentException("Funcionário não encontrado: " + codDepFuncionario);
        }

        EstoqueMovimentacoes mov = EstoqueMovimentacoes.novaEntrada(
                sku, quantidade, lote, dataValidade, localizacao,
                codDepFuncionario, motivo, observacoes
        );

        Long id = repository.insert(mov);
        EstoqueMovimentacoes salva = repository.findById(id)
                .orElseThrow(() -> new SQLException("Falha ao buscar movimentação"));

        eventBus.publish(new EstoqueEvent<>(salva, "ENTRADA"));
        logger.info("✅ Entrada registrada: SKU={}, Qtd={}, Func={}", sku, quantidade, codDepFuncionario);

        return salva;
    }

    public EstoqueMovimentacoes registrarSaida(
            String sku,
            int quantidade,
            String codDepFuncionario,
            String motivo,
            String observacoes
    ) throws SQLException {
        if (!repository.skuExistsInCatalogo(sku)) {
            throw new IllegalArgumentException("SKU não existe no catálogo: " + sku);
        }
        if (!repository.funcionarioExists(codDepFuncionario)) {
            throw new IllegalArgumentException("Funcionário não encontrado: " + codDepFuncionario);
        }

        int saldo = repository.calcularSaldo(sku);
        if (saldo < quantidade) {
            throw new IllegalStateException(
                    "Saldo insuficiente. Disponível: " + saldo + ", Requerido: " + quantidade
            );
        }

        EstoqueMovimentacoes mov = EstoqueMovimentacoes.novaSaida(
                sku, quantidade, codDepFuncionario, motivo, observacoes
        );

        Long id = repository.insert(mov);
        EstoqueMovimentacoes salva = repository.findById(id)
                .orElseThrow(() -> new SQLException("Falha ao buscar movimentação"));

        eventBus.publish(new EstoqueEvent<>(salva, "SAIDA"));
        logger.info("✅ Saída registrada: SKU={}, Qtd={}, Func={}", sku, quantidade, codDepFuncionario);

        return salva;
    }

    public EstoqueMovimentacoes registrarAjuste(
            String sku,
            int quantidade,
            String codDepFuncionario,
            String motivo,
            String observacoes
    ) throws SQLException {
        if (!repository.skuExistsInCatalogo(sku)) {
            throw new IllegalArgumentException("SKU não existe no catálogo: " + sku);
        }
        if (!repository.funcionarioExists(codDepFuncionario)) {
            throw new IllegalArgumentException("Funcionário não encontrado: " + codDepFuncionario);
        }

        EstoqueMovimentacoes mov = EstoqueMovimentacoes.novoAjuste(
                sku, quantidade, codDepFuncionario, motivo, observacoes
        );

        Long id = repository.insert(mov);
        EstoqueMovimentacoes salva = repository.findById(id)
                .orElseThrow(() -> new SQLException("Falha ao buscar movimentação"));

        eventBus.publish(new EstoqueEvent<>(salva, "AJUSTE"));
        logger.info("✅ Ajuste registrado: SKU={}, Qtd={}, Func={}", sku, quantidade, codDepFuncionario);

        return salva;
    }

    public EstoqueMovimentacoes registrarReserva(
            String sku,
            int quantidade,
            String codDepFuncionario,
            String motivo,
            String observacoes
    ) throws SQLException {
        if (!repository.skuExistsInCatalogo(sku)) {
            throw new IllegalArgumentException("SKU não existe no catálogo: " + sku);
        }
        if (!repository.funcionarioExists(codDepFuncionario)) {
            throw new IllegalArgumentException("Funcionário não encontrado: " + codDepFuncionario);
        }

        int saldo = repository.calcularSaldo(sku);
        if (saldo < quantidade) {
            throw new IllegalStateException(
                    "Saldo insuficiente para reserva. Disponível: " + saldo + ", Requerido: " + quantidade
            );
        }

        EstoqueMovimentacoes mov = EstoqueMovimentacoes.novaReserva(
                sku, quantidade, codDepFuncionario, motivo, observacoes
        );

        Long id = repository.insert(mov);
        EstoqueMovimentacoes salva = repository.findById(id)
                .orElseThrow(() -> new SQLException("Falha ao buscar movimentação"));

        eventBus.publish(new EstoqueEvent<>(salva, "RESERVA"));
        logger.info("✅ Reserva registrada: SKU={}, Qtd={}, Func={}", sku, quantidade, codDepFuncionario);

        return salva;
    }

    // ===== OPERAÇÕES COMBINADAS =====

    public void registrarTransferencia(
            String sku,
            int quantidade,
            String localizacaoOrigem,
            String localizacaoDestino,
            String codDepFuncionario,
            String observacoes
    ) throws SQLException {
        // Saída da origem
        registrarSaida(sku, quantidade, codDepFuncionario,
                "Transferência para " + localizacaoDestino, observacoes);

        // Entrada no destino
        registrarEntrada(sku, quantidade, null, null,
                localizacaoDestino, codDepFuncionario,
                "Transferência de " + localizacaoOrigem, observacoes);

        logger.info("✅ Transferência realizada: SKU={}, Qtd={}, {} → {}",
                sku, quantidade, localizacaoOrigem, localizacaoDestino);
    }

    public void registrarDevolucao(
            String sku,
            int quantidade,
            String lote,
            String codDepFuncionario,
            String motivo,
            String observacoes
    ) throws SQLException {
        registrarEntrada(sku, quantidade, lote, null, null,
                codDepFuncionario, motivo != null ? motivo : "Devolução", observacoes);

        logger.info("✅ Devolução registrada: SKU={}, Qtd={}, Func={}", sku, quantidade, codDepFuncionario);
    }

    public void registrarPerda(
            String sku,
            int quantidade,
            String codDepFuncionario,
            String motivo,
            String observacoes
    ) throws SQLException {
        registrarSaida(sku, quantidade, codDepFuncionario,
                motivo != null ? motivo : "Perda/avaria", observacoes);

        logger.info("✅ Perda registrada: SKU={}, Qtd={}, Func={}", sku, quantidade, codDepFuncionario);
    }

    // ===== CONSULTAS ANALÍTICAS =====

    public int calcularSaldo(String sku) throws SQLException {
        return repository.calcularSaldo(sku);
    }

    public Map<LocalDate, Integer> obterEvolucaoSaldo(String sku, LocalDate inicio, LocalDate fim)
            throws SQLException {
        return repository.getEvolucaoSaldo(sku, inicio, fim);
    }

    public Map<String, Integer> obterContagemPorTipo(LocalDate inicio, LocalDate fim) throws SQLException {
        return repository.countByTipo(inicio, fim);
    }

    public Map<String, Object> obterRelatorioDiario(LocalDate data) throws SQLException {
        logger.debug("Gerando relatório diário para: {}", data);

        List<EstoqueMovimentacoes> movs = repository.findByPeriodo(data, data);

        int totalEntradas = 0;
        int totalSaidas = 0;
        int totalAjustes = 0;
        int totalReservas = 0;
        Set<String> skus = new HashSet<>();
        Set<String> funcionarios = new HashSet<>();
        Map<String, Integer> saldoPorSku = new HashMap<>();

        for (EstoqueMovimentacoes m : movs) {
            skus.add(m.skuProduto());
            funcionarios.add(m.codDepFuncionario());

            switch (m.tipoMovimentacao()) {
                case ENTRADA -> {
                    totalEntradas += m.quantidade();
                    saldoPorSku.merge(m.skuProduto(), m.quantidade(), Integer::sum);
                }
                case SAIDA -> {
                    totalSaidas += m.quantidade();
                    saldoPorSku.merge(m.skuProduto(), -m.quantidade(), Integer::sum);
                }
                case AJUSTE -> {
                    totalAjustes += m.quantidade();
                    saldoPorSku.merge(m.skuProduto(), m.quantidade(), Integer::sum);
                }
                case RESERVA -> totalReservas += m.quantidade();
            }
        }

        Map<String, Object> relatorio = new LinkedHashMap<>();
        relatorio.put("data", data);
        relatorio.put("totalMovimentacoes", movs.size());
        relatorio.put("totalEntradas", totalEntradas);
        relatorio.put("totalSaidas", totalSaidas);
        relatorio.put("totalAjustes", totalAjustes);
        relatorio.put("totalReservas", totalReservas);
        relatorio.put("saldoDia", totalEntradas - totalSaidas + totalAjustes);
        relatorio.put("skusMovimentados", skus.size());
        relatorio.put("funcionariosEnvolvidos", funcionarios.size());
        relatorio.put("saldoPorSku", Map.copyOf(saldoPorSku));

        return Map.copyOf(relatorio);
    }

    public int contarTotal() throws SQLException {
        return repository.countAll();
    }

    // ===== VALIDAÇÕES =====

    public boolean validarSaldoSuficiente(String sku, int quantidade) throws SQLException {
        return repository.calcularSaldo(sku) >= quantidade;
    }
}