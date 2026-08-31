package com.ossobo.gestaoDepIt.db.services;

import com.ossobo.gestaoDepIt.db.config.event.InventarioEvent;
import com.ossobo.gestaoDepIt.db.models.InventarioEquipamentos;
import com.ossobo.gestaoDepIt.db.repositories.InventarioEquipamentosRepository;

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
 * InventarioEquipamentosService - Regras de negócio com EventBus
 * v2.1 - Adicionados métodos para fatura, documentos e devolução
 *
 * Responsabilidades:
 * - Gerenciar equipamentos no inventário
 * - Validar regras de negócio
 * - Publicar eventos (@InventarioEvent)
 * - Operações de manutenção, baixa, documentos e fatura
 */
@Service
public class InventarioEquipamentosService {

    private static final Logger logger = LoggerFactory.getLogger(InventarioEquipamentosService.class);

    @Inject
    private InventarioEquipamentosRepository repository;

    @Inject
    private EventBus eventBus;

    // ============================================================
    // CRUD
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

    public InventarioEquipamentos cadastrar(InventarioEquipamentos equip) throws SQLException {
        validarEquipamento(equip);

        if (repository.existsByNumSerie(equip.numSerie())) {
            throw new IllegalArgumentException("Número de série já cadastrado: " + equip.numSerie());
        }

        if (repository.existsByMacAddress(equip.enderecoMac())) {
            throw new IllegalArgumentException("Endereço MAC já cadastrado: " + equip.enderecoMac());
        }

        String id = repository.insert(equip);
        InventarioEquipamentos salvo = repository.findById(id)
                .orElseThrow(() -> new SQLException("Falha ao buscar equipamento cadastrado"));

        eventBus.publish(new InventarioEvent<>(salvo, "CADASTRADO"));
        logger.info("✅ Equipamento cadastrado: ID={}, Série={}, MAC={}",
                salvo.id(), salvo.numSerie(), salvo.enderecoMac());

        return salvo;
    }

    public InventarioEquipamentos atualizar(InventarioEquipamentos equip) throws SQLException {
        validarEquipamento(equip);

        if (equip.id() == null) {
            throw new IllegalArgumentException("ID não pode ser nulo para atualização");
        }

        Optional<InventarioEquipamentos> existente = repository.findById(equip.id());
        if (existente.isEmpty()) {
            throw new IllegalArgumentException("Equipamento não encontrado: " + equip.id());
        }

        Optional<InventarioEquipamentos> porSerie = repository.findByNumSerie(equip.numSerie());
        if (porSerie.isPresent() && !porSerie.get().id().equals(equip.id())) {
            throw new IllegalArgumentException("Número de série já utilizado por outro equipamento");
        }

        Optional<InventarioEquipamentos> porMac = repository.findByMacAddress(equip.enderecoMac());
        if (porMac.isPresent() && !porMac.get().id().equals(equip.id())) {
            throw new IllegalArgumentException("Endereço MAC já utilizado por outro equipamento");
        }

        repository.update(equip);
        InventarioEquipamentos atualizado = repository.findById(equip.id())
                .orElseThrow(() -> new SQLException("Falha ao buscar equipamento atualizado"));

        eventBus.publish(new InventarioEvent<>(atualizado, "ATUALIZADO"));
        logger.info("✅ Equipamento atualizado: ID={}", atualizado.id());

        return atualizado;
    }

    public void excluir(String id) throws SQLException {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Equipamento não encontrado: " + id);
        }

        Optional<InventarioEquipamentos> equip = repository.findById(id);
        repository.delete(id);

        equip.ifPresent(e -> {
            eventBus.publish(new InventarioEvent<>(e, "EXCLUIDO"));
            logger.info("✅ Equipamento excluído: ID={}", id);
        });
    }

    // ============================================================
    // OPERAÇÕES DE STATUS
    // ============================================================

    public void atualizarStatus(String id, String novoStatus) throws SQLException {
        if (!InventarioEquipamentos.isStatusValido(novoStatus)) {
            throw new IllegalArgumentException("Status inválido: " + novoStatus);
        }

        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Equipamento não encontrado: " + id);
        }

        repository.updateStatus(id, novoStatus);

        Optional<InventarioEquipamentos> equip = repository.findById(id);
        equip.ifPresent(e -> {
            eventBus.publish(new InventarioEvent<>(e, "STATUS_ALTERADO"));
            logger.info("✅ Status alterado: ID={} → {}", id, novoStatus);
        });
    }

    public void atualizarCondicao(String id, String novaCondicao) throws SQLException {
        if (!InventarioEquipamentos.isCondicaoValida(novaCondicao)) {
            throw new IllegalArgumentException("Condição inválida: " + novaCondicao);
        }

        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Equipamento não encontrado: " + id);
        }

        repository.updateCondicao(id, novaCondicao);

        Optional<InventarioEquipamentos> equip = repository.findById(id);
        equip.ifPresent(e -> {
            eventBus.publish(new InventarioEvent<>(e, "CONDICAO_ALTERADA"));
            logger.info("✅ Condição alterada: ID={} → {}", id, novaCondicao);
        });
    }

    public void baixarEquipamento(String id, String motivo) throws SQLException {
        Optional<InventarioEquipamentos> equip = repository.findById(id);
        if (equip.isEmpty()) {
            throw new IllegalArgumentException("Equipamento não encontrado: " + id);
        }

        if (equip.get().isBaixado()) {
            throw new IllegalStateException("Equipamento já está baixado");
        }

        repository.updateStatus(id, "BAIXADO");

        String obs = equip.get().observacoes() != null && !equip.get().observacoes().isBlank()
                ? equip.get().observacoes() + "\nBaixa: " + motivo
                : "Baixa: " + motivo;

        InventarioEquipamentos atualizado = equip.get().comObservacoes(obs);
        repository.update(atualizado);

        eventBus.publish(new InventarioEvent<>(atualizado, "BAIXADO"));
        logger.info("✅ Equipamento baixado: ID={}, Motivo={}", id, motivo);
    }

    public void enviarParaManutencao(String id, String motivo) throws SQLException {
        Optional<InventarioEquipamentos> equip = repository.findById(id);
        if (equip.isEmpty()) {
            throw new IllegalArgumentException("Equipamento não encontrado: " + id);
        }

        if (equip.get().isEmManutencao()) {
            throw new IllegalStateException("Equipamento já está em manutenção");
        }

        repository.updateStatus(id, "MANUTENCAO");

        String obs = equip.get().observacoes() != null && !equip.get().observacoes().isBlank()
                ? equip.get().observacoes() + "\nManutenção: " + motivo
                : "Manutenção: " + motivo;

        InventarioEquipamentos atualizado = equip.get().comObservacoes(obs);
        repository.update(atualizado);

        eventBus.publish(new InventarioEvent<>(atualizado, "MANUTENCAO_INICIADA"));
        logger.info("✅ Equipamento em manutenção: ID={}, Motivo={}", id, motivo);
    }

    public void retornarDeManutencao(String id, String condicaoPos, String observacoes) throws SQLException {
        Optional<InventarioEquipamentos> equip = repository.findById(id);
        if (equip.isEmpty()) {
            throw new IllegalArgumentException("Equipamento não encontrado: " + id);
        }

        if (!equip.get().isEmManutencao()) {
            throw new IllegalStateException("Equipamento não está em manutenção");
        }

        if (!InventarioEquipamentos.isCondicaoValida(condicaoPos)) {
            throw new IllegalArgumentException("Condição inválida: " + condicaoPos);
        }

        repository.updateStatus(id, "ATIVO");
        repository.updateCondicao(id, condicaoPos);
        repository.updateVerificacao(id, LocalDate.now(), condicaoPos, observacoes);

        Optional<InventarioEquipamentos> atualizado = repository.findById(id);
        atualizado.ifPresent(e -> {
            eventBus.publish(new InventarioEvent<>(e, "MANUTENCAO_FINALIZADA"));
            logger.info("✅ Equipamento retornou da manutenção: ID={}, Condição={}", id, condicaoPos);
        });
    }

    // ============================================================
    // OPERAÇÕES DE LOCALIZAÇÃO
    // ============================================================

    public void transferirLocalizacao(String id, String localizacao, String departamento) throws SQLException {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Equipamento não encontrado: " + id);
        }

        repository.updateLocalizacao(id, localizacao, departamento);

        Optional<InventarioEquipamentos> equip = repository.findById(id);
        equip.ifPresent(e -> {
            eventBus.publish(new InventarioEvent<>(e, "LOCALIZACAO_ALTERADA"));
            logger.info("✅ Localização alterada: ID={} → {}/{}", id, localizacao, departamento);
        });
    }

    public void reassociarFuncionario(String id, String funcionarioId) throws SQLException {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Equipamento não encontrado: " + id);
        }

        repository.updateFuncionario(id, funcionarioId);

        Optional<InventarioEquipamentos> equip = repository.findById(id);
        equip.ifPresent(e -> {
            eventBus.publish(new InventarioEvent<>(e, "FUNCIONARIO_REASSOCIADO"));
            logger.info("✅ Funcionário reassociado: ID={} → {}", id, funcionarioId);
        });
    }

    // ============================================================
    // OPERAÇÕES DE VERIFICAÇÃO
    // ============================================================

    public void registrarVerificacao(String id, LocalDate data, String condicao, String observacoes) throws SQLException {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Equipamento não encontrado: " + id);
        }

        if (!InventarioEquipamentos.isCondicaoValida(condicao)) {
            throw new IllegalArgumentException("Condição inválida: " + condicao);
        }

        if (data == null) {
            data = LocalDate.now();
        }

        repository.updateVerificacao(id, data, condicao, observacoes);

        Optional<InventarioEquipamentos> equip = repository.findById(id);
        LocalDate finalData = data;
        equip.ifPresent(e -> {
            eventBus.publish(new InventarioEvent<>(e, "VERIFICACAO_REGISTRADA"));
            logger.info("✅ Verificação registrada: ID={}, Data={}, Condição={}",
                    id, finalData, condicao);
        });
    }

    // ============================================================
    // OPERAÇÕES DE FATURA E DOCUMENTOS
    // ============================================================

    public void atualizarNumeroFatura(String id, String numeroFatura) throws SQLException {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Equipamento não encontrado: " + id);
        }

        repository.updateNumeroFatura(id, numeroFatura);

        Optional<InventarioEquipamentos> equip = repository.findById(id);
        equip.ifPresent(e -> {
            eventBus.publish(new InventarioEvent<>(e, "FATURA_ATUALIZADA"));
            logger.info("✅ Número de fatura atualizado: ID={} → {}", id, numeroFatura);
        });
    }

    public void atualizarDocumentoEntrega(String id, byte[] documento) throws SQLException {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Equipamento não encontrado: " + id);
        }

        repository.updateDocumentoEntrega(id, documento);

        Optional<InventarioEquipamentos> equip = repository.findById(id);
        equip.ifPresent(e -> {
            eventBus.publish(new InventarioEvent<>(e, "DOCUMENTO_ENTREGA_ATUALIZADO"));
            logger.info("✅ Documento de entrega atualizado: ID={}", id);
        });
    }

    public void atualizarDocumentoDevolucao(String id, byte[] documento) throws SQLException {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Equipamento não encontrado: " + id);
        }

        repository.updateDocumentoDevolucao(id, documento);

        Optional<InventarioEquipamentos> equip = repository.findById(id);
        equip.ifPresent(e -> {
            eventBus.publish(new InventarioEvent<>(e, "DOCUMENTO_DEVOLUCAO_ATUALIZADO"));
            logger.info("✅ Documento de devolução atualizado: ID={}", id);
        });
    }

    public void atualizarDevolucao(String id, boolean devolucao) throws SQLException {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Equipamento não encontrado: " + id);
        }

        repository.updateDevolucao(id, devolucao);

        Optional<InventarioEquipamentos> equip = repository.findById(id);
        equip.ifPresent(e -> {
            eventBus.publish(new InventarioEvent<>(e, "DEVOLUCAO_ATUALIZADA"));
            logger.info("✅ Status de devolução atualizado: ID={} → {}", id, devolucao);
        });
    }

    // ============================================================
    // FILTROS
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
            String sku,
            String funcionarioId,
            String status,
            String condicao,
            String localizacao,
            String departamento,
            LocalDate dataInicio,
            LocalDate dataFim,
            String macAddress
    ) throws SQLException {
        return repository.findWithFilters(sku, funcionarioId, status, condicao,
                localizacao, departamento, dataInicio, dataFim, macAddress);
    }

    // ============================================================
    // RELATÓRIOS E ESTATÍSTICAS
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

    public Map<String, Integer> obterEstatisticasPorStatus() throws SQLException {
        return repository.countByStatus();
    }

    public Map<String, Integer> obterEstatisticasPorCondicao() throws SQLException {
        return repository.countByCondicao();
    }

    public Map<String, Integer> obterEstatisticasPorDepartamento() throws SQLException {
        return repository.countByDepartamento();
    }

    public Map<String, Integer> obterEstatisticasPorLocalizacao() throws SQLException {
        return repository.countByLocalizacao();
    }

    public Map<Integer, Integer> obterEstatisticasPorAnoAquisicao() throws SQLException {
        return repository.countByAnoAquisicao();
    }

    public Map<String, Integer> obterEstatisticasPorPrefixoMac() throws SQLException {
        return repository.countByMacPrefix();
    }

    public int contarTotal() throws SQLException {
        return repository.countAll();
    }

    public int contarPorStatus(String status) throws SQLException {
        return repository.countByStatus(status);
    }

    // ============================================================
    // DISTINCT VALUES (PARA COMBOBOX)
    // ============================================================

    public List<String> listarLocalizacoes() throws SQLException {
        return repository.findLocalizacoesUnicas();
    }

    public List<String> listarDepartamentos() throws SQLException {
        return repository.findDepartamentosUnicos();
    }

    public List<String> listarStatus() {
        return InventarioEquipamentos.getStatusValidos();
    }

    public List<String> listarCondicoes() {
        return InventarioEquipamentos.getCondicoesValidas();
    }

    // ============================================================
    // VALIDAÇÕES
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

    // ============================================================
    // MÉTODO PRIVADO
    // ============================================================

    private void validarEquipamento(InventarioEquipamentos e) {
        if (!(e.skuProduto() instanceof String sku) || sku.isBlank()) {
            throw new IllegalArgumentException("SKU do produto é obrigatório");
        }
        if (!(e.funcionarioId() instanceof String func) || func.isBlank()) {
            throw new IllegalArgumentException("ID do funcionário é obrigatório");
        }
        if (!(e.numSerie() instanceof String ns) || ns.isBlank()) {
            throw new IllegalArgumentException("Número de série é obrigatório");
        }
        if (!(e.enderecoMac() instanceof String mac) || mac.isBlank()) {
            throw new IllegalArgumentException("Endereço MAC é obrigatório");
        }
        if (!(e.localizacao() instanceof String loc) || loc.isBlank()) {
            throw new IllegalArgumentException("Localização é obrigatória");
        }
        if (e.dataAquisicao() == null) {
            throw new IllegalArgumentException("Data de aquisição é obrigatória");
        }
        if (e.dataAquisicao().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Data de aquisição não pode ser futura");
        }
        if (e.dataInstalacao() != null && e.dataInstalacao().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Data de instalação não pode ser futura");
        }
        if (e.dataUltimaVerificacao() != null && e.dataUltimaVerificacao().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Data da última verificação não pode ser futura");
        }
    }
}