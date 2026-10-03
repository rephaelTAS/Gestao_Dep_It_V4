package com.ossobo.gestaoDepIt.db.services;

import com.ossobo.gestaoDepIt.db.enums.CondicaoEquipamento;
import com.ossobo.gestaoDepIt.db.enums.StatusEquipamento;
import com.ossobo.gestaoDepIt.db.models.InventarioEquipamentos;
import com.ossobo.gestaoDepIt.db.repositories.InventarioEquipamentosRepository;
import com.ossobo.winterfx.anotations.Inject;
import com.ossobo.winterfx.anotations.Service;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * InventarioEquipamentosService v3.0
 *
 * Responsabilidade: Regras de negócio de LEITURA do inventário de equipamentos.
 *
 * v3.0 — Separação estrutural leitura/mutação (ratificada):
 *        - TODAS as mutações saíram deste service; agora vivem em
 *          {@code InventarioCrudService}, que orquestra a transação
 *          inventário + histórico atomicamente.
 *        - Este service expõe APENAS consultas: listagens, buscas,
 *          filtros, estatísticas, distinct e verificações.
 *        - Mutações via rota: {@code inventario-crud/service/*}.
 *        - Consultas via rota:  {@code inventario-equipamentos/service/*}.
 *        - listarStatus() e listarCondicoes() passam a delegar aos enums
 *          StatusEquipamento / CondicaoEquipamento (fonte única).
 *
 * @since v2.3
 */
@Service
public class InventarioEquipamentosService {

    @Inject
    private InventarioEquipamentosRepository repository;

    // ============================================================
    // LISTAGENS / BUSCAS
    // ============================================================

    public List<InventarioEquipamentos> listarTodos() throws SQLException {
        return repository.findAll();
    }

    public List<InventarioEquipamentos> listarTodos(int pagina, int tamanho) throws SQLException {
        int offset = (pagina - 1) * tamanho;
        return repository.findAll(tamanho, offset);
    }

    public Optional<InventarioEquipamentos> buscarPorId(String id) throws SQLException {
        return repository.findById(id);
    }

    public Optional<InventarioEquipamentos> buscarPorNumSerie(String numSerie) throws SQLException {
        return repository.findByNumSerie(numSerie);
    }

    public Optional<InventarioEquipamentos> buscarPorMacAddress(String mac) throws SQLException {
        return repository.findByMacAddress(mac);
    }

    public List<InventarioEquipamentos> buscarPorMacAddressLike(String pattern) throws SQLException {
        return repository.findByMacAddressLike(pattern);
    }

    public List<InventarioEquipamentos> buscarPorNumeroFatura(String numeroFatura) throws SQLException {
        return repository.findByNumeroFatura(numeroFatura);
    }

    // ============================================================
    // FILTROS SIMPLES
    // ============================================================

    public List<InventarioEquipamentos> buscarPorStatus(String status) throws SQLException {
        return repository.findByStatus(status);
    }

    public List<InventarioEquipamentos> buscarPorCondicao(String condicao) throws SQLException {
        return repository.findByCondicao(condicao);
    }

    public List<InventarioEquipamentos> buscarPorLocalizacao(String localizacao) throws SQLException {
        return repository.findByLocalizacao(localizacao);
    }

    public List<InventarioEquipamentos> buscarPorDepartamento(String departamento) throws SQLException {
        return repository.findByDepartamento(departamento);
    }

    public List<InventarioEquipamentos> buscarPorFuncionario(String funcionarioId) throws SQLException {
        return repository.findByFuncionarioId(funcionarioId);
    }

    public List<InventarioEquipamentos> buscarPorSku(String sku) throws SQLException {
        return repository.findBySkuProduto(sku);
    }

    public List<InventarioEquipamentos> buscarPorPeriodoAquisicao(LocalDate inicio, LocalDate fim) throws SQLException {
        return repository.findByPeriodoAquisicao(inicio, fim);
    }

    public List<InventarioEquipamentos> buscarComFiltros(
            String sku, String funcionarioId, String status, String condicao,
            String localizacao, String departamento,
            LocalDate dataInicio, LocalDate dataFim, String macAddress
    ) throws SQLException {
        return repository.findWithFilters(sku, funcionarioId, status, condicao,
                localizacao, departamento, dataInicio, dataFim, macAddress);
    }

    // ============================================================
    // RELATÓRIOS / ALERTAS
    // ============================================================

    public List<InventarioEquipamentos> buscarEquipamentosCriticos() throws SQLException {
        return repository.findCriticos();
    }

    public List<InventarioEquipamentos> buscarEquipamentosVerificacaoAtrasada() throws SQLException {
        return repository.findVerificacaoAtrasada();
    }

    public List<InventarioEquipamentos> buscarEquipamentosParaVerificacao(LocalDate dataLimite) throws SQLException {
        return repository.findParaVerificacao(dataLimite);
    }

    public List<InventarioEquipamentos> buscarEquipamentosSemMacAddress() throws SQLException {
        return repository.findSemMacAddress();
    }

    // ============================================================
    // ESTATÍSTICAS
    // ============================================================

    public Map<String, Integer> obterEstatisticasPorStatus() throws SQLException        { return repository.countByStatus(); }
    public Map<String, Integer> obterEstatisticasPorCondicao() throws SQLException      { return repository.countByCondicao(); }
    public Map<String, Integer> obterEstatisticasPorDepartamento() throws SQLException  { return repository.countByDepartamento(); }
    public Map<String, Integer> obterEstatisticasPorLocalizacao() throws SQLException   { return repository.countByLocalizacao(); }
    public Map<Integer, Integer> obterEstatisticasPorAnoAquisicao() throws SQLException { return repository.countByAnoAquisicao(); }
    public Map<String, Integer> obterEstatisticasPorPrefixoMac() throws SQLException    { return repository.countByMacPrefix(); }

    public int contarTotal() throws SQLException {
        return repository.countAll();
    }

    public int contarPorStatus(String status) throws SQLException {
        return repository.countByStatus(status);
    }

    // ============================================================
    // DISTINCT / DOMÍNIOS (fonte única: enums)
    // ============================================================

    public List<String> listarLocalizacoes() throws SQLException {
        return repository.findLocalizacoesUnicas();
    }

    public List<String> listarDepartamentos() throws SQLException {
        return repository.findDepartamentosUnicos();
    }

    public List<String> listarStatus() {
        return StatusEquipamento.todos();
    }

    public List<String> listarCondicoes() {
        return CondicaoEquipamento.todos();
    }

    // ============================================================
    // VERIFICAÇÕES (unicidade / existência)
    // ============================================================

    public boolean existePorId(String id) throws SQLException {
        return repository.existsById(id);
    }

    public boolean numeroSerieExiste(String numSerie) throws SQLException {
        return repository.existsByNumSerie(numSerie);
    }

    public boolean macAddressExiste(String mac) throws SQLException {
        return repository.existsByMacAddress(mac);
    }
}