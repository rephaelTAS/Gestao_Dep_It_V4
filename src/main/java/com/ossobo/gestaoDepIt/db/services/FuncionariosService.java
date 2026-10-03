package com.ossobo.gestaoDepIt.db.services;

import com.ossobo.gestaoDepIt.db.config.event.FuncionarioEvent;
import com.ossobo.gestaoDepIt.db.config.DatabaseConnection;
import com.ossobo.gestaoDepIt.db.models.Funcionarios;
import com.ossobo.gestaoDepIt.db.models.HistoricoEventos;
import com.ossobo.gestaoDepIt.db.repositories.FuncionariosRepository;
import com.ossobo.gestaoDepIt.db.repositories.InventarioEquipamentosRepository;
import com.ossobo.gestaoDepIt.db.repositories.HistoricoEventosRepository;
import com.ossobo.gestaoDepIt.utils.gerarCodDep.GerarCodDepService;
import com.ossobo.winterfx.anotations.Inject;
import com.ossobo.winterfx.anotations.Service;
import com.ossobo.winterfx.event.EventBus;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * FuncionariosService v2.3 - Regras de negócio com EventBus
 *
 * v2.3 — transferirDepartamento(codDepAtual, novoDepartamento, executor, descricao):
 *        Operação TRANSACIONAL e composta:
 *          1) gera novo cod_dep (GerarCodDepService)
 *          2) atualiza funcionarios.cod_dep + cod_dep_anterior
 *          3) reassocia inventario_equipamentos.funcionario_id
 *          4) registra TRANSFERENCIA no historico_eventos
 *        Tudo ou nada: rollback em qualquer falha.
 *
 * v2.2 — Alinhado ao FuncionariosRepository v2.2.
 */
@Service
public class FuncionariosService {

    private static final System.Logger LOGGER = System.getLogger(FuncionariosService.class.getName());

    @Inject
    private FuncionariosRepository repository;

    @Inject
    private InventarioEquipamentosRepository inventarioRepository;

    @Inject
    private HistoricoEventosRepository historicoRepository;

    @Inject
    private GerarCodDepService gerarCodDepService;

    @Inject
    private TransferenciaDepartamentoService transferenciaService;

    @Inject
    private DatabaseConnection dbConnection;

    @Inject
    private EventBus eventBus;

    // ===== CRUD =====

    public List<Funcionarios> listarTodos() throws SQLException {
        return repository.findAll();
    }

    public List<Funcionarios> listarTodos(int pagina, int tamanho) throws SQLException {
        int offset = (pagina - 1) * tamanho;
        return repository.findAll(tamanho, offset);
    }

    public List<Funcionarios> listarAtivos(int pagina, int tamanho) throws SQLException {
        int offset = (pagina - 1) * tamanho;
        return repository.findAllAtivos(tamanho, offset);
    }

    public Optional<Funcionarios> buscarPorId(String id) throws SQLException {
        return repository.findById(id);
    }

    public Optional<Funcionarios> buscarPorCodDep(String codDep) throws SQLException {
        return repository.findByCodDep(codDep);
    }

    public Optional<Funcionarios> buscarPorEmail(String email) throws SQLException {
        return repository.findByEmail(email);
    }

    public Funcionarios criar(Funcionarios funcionario) throws SQLException {
        validarFuncionario(funcionario);

        if (repository.existsByCodDep(funcionario.codDep())) {
            throw new IllegalArgumentException("Já existe funcionário com código: " + funcionario.codDep());
        }

        if (funcionario.email() != null && !funcionario.email().isBlank()) {
            if (repository.existsByEmail(funcionario.email())) {
                throw new IllegalArgumentException("Email já utilizado: " + funcionario.email());
            }
        }

        String id = repository.insert(funcionario);
        Funcionarios criado = repository.findById(id)
                .orElseThrow(() -> new SQLException("Falha ao buscar funcionário criado"));

        eventBus.publish(new FuncionarioEvent<>(criado, "CRIADO"));
        LOGGER.log(System.Logger.Level.INFO, "Funcionário criado: {0} - {1}",
                criado.codDep(), criado.nome());

        return criado;
    }

    public Funcionarios atualizar(Funcionarios funcionario) throws SQLException {
        validarFuncionario(funcionario);

        Optional<Funcionarios> existente = repository.findByCodDep(funcionario.codDep());
        if (existente.isEmpty()) {
            throw new IllegalArgumentException("Funcionário não encontrado: " + funcionario.codDep());
        }

        if (funcionario.email() != null && !funcionario.email().isBlank()) {
            Optional<Funcionarios> porEmail = repository.findByEmail(funcionario.email());
            if (porEmail.isPresent() && !porEmail.get().codDep().equals(funcionario.codDep())) {
                throw new IllegalArgumentException("Email já utilizado por outro funcionário: " + funcionario.email());
            }
        }

        repository.updateCompleto(funcionario);
        Funcionarios atualizado = repository.findByCodDep(funcionario.codDep())
                .orElseThrow(() -> new SQLException("Falha ao buscar funcionário atualizado"));

        eventBus.publish(new FuncionarioEvent<>(atualizado, "ATUALIZADO"));
        LOGGER.log(System.Logger.Level.INFO, "Funcionário atualizado: {0}", atualizado.codDep());

        return atualizado;
    }

    public void excluir(String codDep) throws SQLException {
        Optional<Funcionarios> func = repository.findByCodDep(codDep);
        if (func.isEmpty()) {
            throw new IllegalArgumentException("Funcionário não encontrado: " + codDep);
        }

        repository.marcarDeletado(func.get().id());

        eventBus.publish(new FuncionarioEvent<>(func.get(), "EXCLUIDO"));
        LOGGER.log(System.Logger.Level.INFO, "Funcionário excluído: {0}", codDep);
    }

    // ===== TRANSFERÊNCIA DE DEPARTAMENTO (v2.3) =====

    /**
     * Transfere um funcionário para outro departamento.
     *
     * Operação atômica composta:
     *  1. Valida que o funcionário existe e que o departamento é DIFERENTE.
     *  2. Gera novo cod_dep via GerarCodDepService (persistido em `sequencias`).
     *  3. Atualiza funcionarios.cod_dep + cod_dep_anterior (guarda o antigo).
     *  4. Reassocia todos os inventario_equipamentos.funcionario_id
     *     que apontavam para o cod_dep antigo.
     *  5. Registra evento TRANSFERENCIA no historico_eventos (ledger).
     *
     * Tudo em UMA transação. Qualquer falha → rollback completo.
     *
     * @param codDepAtual      código atual do funcionário (chave estável até agora)
     * @param novoDepartamento novo departamento ("" ou igual ao atual → erro)
     * @param codDepExecutor   cod_dep do funcionário logado (para auditoria)
     * @param descricao        JSON com dados do evento (opcional)
     * @return novo cod_dep gerado
     */
    public String transferirDepartamento(String codDepAtual,
                                         String novoDepartamento,
                                         String codDepExecutor,
                                         String descricao) throws SQLException {
        return transferenciaService.transferir(
                codDepAtual, novoDepartamento, codDepExecutor, descricao);
    }

    // ===== OPERAÇÕES DE IMAGEM =====

    public void atualizarImagemPerfil(String codDep, byte[] imagem, String tipo) throws SQLException {
        if (!repository.existsByCodDep(codDep)) {
            throw new IllegalArgumentException("Funcionário não encontrado: " + codDep);
        }

        repository.updateImagemPerfil(codDep, imagem, tipo, imagem != null ? imagem.length : null);

        Optional<Funcionarios> func = repository.findByCodDep(codDep);
        func.ifPresent(f -> {
            eventBus.publish(new FuncionarioEvent<>(f, "IMAGEM_ATUALIZADA"));
            LOGGER.log(System.Logger.Level.INFO, "Imagem atualizada: {0}", codDep);
        });
    }

    public void removerImagemPerfil(String codDep) throws SQLException {
        if (!repository.existsByCodDep(codDep)) {
            throw new IllegalArgumentException("Funcionário não encontrado: " + codDep);
        }

        repository.removerImagemPerfil(codDep);

        Optional<Funcionarios> func = repository.findByCodDep(codDep);
        func.ifPresent(f -> {
            eventBus.publish(new FuncionarioEvent<>(f, "IMAGEM_REMOVIDA"));
            LOGGER.log(System.Logger.Level.INFO, "Imagem removida: {0}", codDep);
        });
    }

    public Optional<byte[]> obterImagemPerfil(String codDep) throws SQLException {
        return repository.getImagemPerfil(codDep);
    }

    public boolean temImagemPerfil(String codDep) throws SQLException {
        Optional<Funcionarios> func = repository.findByCodDep(codDep);
        return func.map(Funcionarios::temImagemPerfil).orElse(false);
    }

    // ===== OPERAÇÕES DE STATUS =====

    public void ativar(String codDep) throws SQLException {
        Optional<Funcionarios> func = repository.findByCodDep(codDep);
        if (func.isEmpty()) {
            throw new IllegalArgumentException("Funcionário não encontrado: " + codDep);
        }
        if (func.get().isAtivo()) {
            throw new IllegalStateException("Funcionário já está ativo: " + codDep);
        }

        repository.ativar(codDep);

        Funcionarios atualizado = repository.findByCodDep(codDep)
                .orElseThrow(() -> new SQLException("Falha ao buscar funcionário"));

        eventBus.publish(new FuncionarioEvent<>(atualizado, "ATIVADO"));
        LOGGER.log(System.Logger.Level.INFO, "Funcionário ativado: {0}", codDep);
    }

    public void desativar(String codDep) throws SQLException {
        Optional<Funcionarios> func = repository.findByCodDep(codDep);
        if (func.isEmpty()) {
            throw new IllegalArgumentException("Funcionário não encontrado: " + codDep);
        }
        if (!func.get().isAtivo()) {
            throw new IllegalStateException("Funcionário já está inativo: " + codDep);
        }

        repository.desativar(codDep);

        Funcionarios atualizado = repository.findByCodDep(codDep)
                .orElseThrow(() -> new SQLException("Falha ao buscar funcionário"));

        eventBus.publish(new FuncionarioEvent<>(atualizado, "DESATIVADO"));
        LOGGER.log(System.Logger.Level.INFO, "Funcionário desativado: {0}", codDep);
    }

    // ===== ESTATÍSTICAS =====

    public int contarTotal() throws SQLException {
        return repository.countAll();
    }

    public int contarAtivos() throws SQLException {
        return repository.countAtivos();
    }

    // ===== VALIDAÇÕES =====

    public boolean existePorCodDep(String codDep) throws SQLException {
        return repository.existsByCodDep(codDep);
    }

    public boolean existePorEmail(String email) throws SQLException {
        return repository.existsByEmail(email);
    }

    public boolean isAtivo(String codDep) throws SQLException {
        Optional<Funcionarios> func = repository.findByCodDep(codDep);
        return func.map(Funcionarios::isAtivo).orElse(false);
    }

    // ===== MÉTODOS PRIVADOS =====

    private void validarFuncionario(Funcionarios f) {
        if (f.codDep() == null || f.codDep().isBlank()) {
            throw new IllegalArgumentException("Código do funcionário é obrigatório");
        }
        if (f.nome() == null || f.nome().isBlank()) {
            throw new IllegalArgumentException("Nome é obrigatório");
        }
        if (f.funcao() == null || f.funcao().isBlank()) {
            throw new IllegalArgumentException("Função é obrigatória");
        }
        if (f.departamento() == null || f.departamento().isBlank()) {
            throw new IllegalArgumentException("Departamento é obrigatório");
        }

        if (f.email() != null && !f.email().isBlank()) {
            if (!isValidEmail(f.email())) {
                throw new IllegalArgumentException("Email inválido: " + f.email());
            }
        }
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
}