package com.ossobo.gestaoDepIt.db.services;

import com.ossobo.gestaoDepIt.db.config.event.FuncionarioEvent;
import com.ossobo.gestaoDepIt.db.models.Funcionarios;
import com.ossobo.gestaoDepIt.db.repositories.FuncionariosRepository;


import com.ossobo.winterfx.anotations.Inject;
import com.ossobo.winterfx.anotations.Service;
import com.ossobo.winterfx.event.EventBus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * FuncionariosService - Regras de negócio com EventBus
 * v2.0 - Migrado para Java 17+ com WinterFX
 *
 * Responsabilidades:
 * - Gerenciar funcionários
 * - Gestão de imagem de perfil
 * - Publicar eventos (@FuncionarioEvent)
 * - Validações de negócio
 */
@Service
public class FuncionariosService {

    private static final Logger logger = LoggerFactory.getLogger(FuncionariosService.class);

    @Inject
    private FuncionariosRepository repository;

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

        repository.insert(funcionario);
        Funcionarios criado = repository.findByCodDep(funcionario.codDep())
                .orElseThrow(() -> new SQLException("Falha ao buscar funcionário criado"));

        eventBus.publish(new FuncionarioEvent<>(criado, "CRIADO"));
        logger.info("✅ Funcionário criado: {} - {}", criado.codDep(), criado.nome());

        return criado;
    }

    public Funcionarios atualizar(Funcionarios funcionario) throws SQLException {
        validarFuncionario(funcionario);

        Optional<Funcionarios> existente = repository.findByCodDep(funcionario.codDep());
        if (existente.isEmpty()) {
            throw new IllegalArgumentException("Funcionário não encontrado: " + funcionario.codDep());
        }

        // Verifica email duplicado (excluindo o próprio)
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
        logger.info("✅ Funcionário atualizado: {}", atualizado.codDep());

        return atualizado;
    }

    public void excluir(String codDep) throws SQLException {
        if (!repository.existsByCodDep(codDep)) {
            throw new IllegalArgumentException("Funcionário não encontrado: " + codDep);
        }

        Optional<Funcionarios> func = repository.findByCodDep(codDep);
        repository.delete(codDep);

        func.ifPresent(f -> {
            eventBus.publish(new FuncionarioEvent<>(f, "EXCLUIDO"));
            logger.info("✅ Funcionário excluído: {}", codDep);
        });
    }

    // ===== OPERAÇÕES DE IMAGEM =====

    public void atualizarImagemPerfil(String codDep, byte[] imagem, String tipo) throws SQLException {
        if (!repository.existsByCodDep(codDep)) {
            throw new IllegalArgumentException("Funcionário não encontrado: " + codDep);
        }

        validarImagem(imagem, tipo);

        repository.updateImagemPerfil(codDep, imagem, tipo, imagem != null ? imagem.length : null);

        Optional<Funcionarios> func = repository.findByCodDep(codDep);
        func.ifPresent(f -> {
            eventBus.publish(new FuncionarioEvent<>(f, "IMAGEM_ATUALIZADA"));
            logger.info("✅ Imagem atualizada: {}", codDep);
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
            logger.info("✅ Imagem removida: {}", codDep);
        });
    }

    public Optional<byte[]> obterImagemPerfil(String codDep) throws SQLException {
        return repository.getImagemPerfil(codDep);
    }

    public List<Funcionarios> listarComImagem() throws SQLException {
        return repository.findComImagemPerfil();
    }

    public List<Funcionarios> listarSemImagem() throws SQLException {
        return repository.findSemImagemPerfil();
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
        logger.info("✅ Funcionário ativado: {}", codDep);
    }

    public void desativar(String codDep) throws SQLException {
        Optional<Funcionarios> func = repository.findByCodDep(codDep);
        if (func.isEmpty()) {
            throw new IllegalArgumentException("Funcionário não encontrado: " + codDep);
        }
        if (func.get().isInativo()) {
            throw new IllegalStateException("Funcionário já está inativo: " + codDep);
        }

        repository.desativar(codDep);

        Funcionarios atualizado = repository.findByCodDep(codDep)
                .orElseThrow(() -> new SQLException("Falha ao buscar funcionário"));

        eventBus.publish(new FuncionarioEvent<>(atualizado, "DESATIVADO"));
        logger.info("✅ Funcionário desativado: {}", codDep);
    }

    // ===== FILTROS =====

    public List<Funcionarios> buscarPorDepartamento(String departamento) throws SQLException {
        return repository.findByDepartamento(departamento);
    }

    public List<Funcionarios> buscarPorFuncao(String funcao) throws SQLException {
        return repository.findByFuncao(funcao);
    }

    public List<Funcionarios> buscarPorLocalTrabalho(String local) throws SQLException {
        return repository.findByLocalTrabalho(local);
    }

    public List<Funcionarios> buscarPorNome(String nome) throws SQLException {
        return repository.findByNomeContaining(nome);
    }

    public List<Funcionarios> buscarPorStatus(boolean ativo) throws SQLException {
        return repository.findByStatus(ativo);
    }

    public List<Funcionarios> buscarComFiltros(
            String departamento,
            String funcao,
            String localTrabalho,
            Boolean ativo,
            String nome
    ) throws SQLException {
        return repository.findWithFilters(departamento, funcao, localTrabalho, ativo, nome);
    }

    // ===== OPERAÇÕES DE NEGÓCIO =====

    public void transferirDepartamento(String codDep, String novoDepartamento) throws SQLException {
        Optional<Funcionarios> func = repository.findByCodDep(codDep);
        if (func.isEmpty()) {
            throw new IllegalArgumentException("Funcionário não encontrado: " + codDep);
        }

        Funcionarios atualizado = func.get().comDepartamento(novoDepartamento);
        repository.update(atualizado);

        Funcionarios salvo = repository.findByCodDep(codDep)
                .orElseThrow(() -> new SQLException("Falha ao buscar funcionário"));

        eventBus.publish(new FuncionarioEvent<>(salvo, "TRANSFERIDO"));
        logger.info("✅ Funcionário transferido: {} → {}", codDep, novoDepartamento);
    }

    public void promover(String codDep, String novaFuncao) throws SQLException {
        Optional<Funcionarios> func = repository.findByCodDep(codDep);
        if (func.isEmpty()) {
            throw new IllegalArgumentException("Funcionário não encontrado: " + codDep);
        }

        Funcionarios atualizado = func.get().comFuncao(novaFuncao);
        repository.update(atualizado);

        Funcionarios salvo = repository.findByCodDep(codDep)
                .orElseThrow(() -> new SQLException("Falha ao buscar funcionário"));

        eventBus.publish(new FuncionarioEvent<>(salvo, "PROMOVIDO"));
        logger.info("✅ Funcionário promovido: {} → {}", codDep, novaFuncao);
    }

    // ===== ESTATÍSTICAS =====

    public Map<String, Integer> obterEstatisticas() throws SQLException {
        return repository.getEstatisticas();
    }

    public Map<String, Integer> obterContagemPorDepartamento() throws SQLException {
        return repository.countByDepartamento();
    }

    public Map<String, Integer> obterContagemPorFuncao() throws SQLException {
        return repository.countByFuncao();
    }

    public Map<String, Integer> obterEstatisticasImagem() throws SQLException {
        return repository.getEstatisticasImagem();
    }

    public int contarTotal() throws SQLException {
        return repository.countAll();
    }

    public int contarAtivos() throws SQLException {
        return repository.countAtivos();
    }

    public List<String> listarDepartamentos() throws SQLException {
        return repository.findDepartamentos();
    }

    public List<String> listarFuncoes() throws SQLException {
        return repository.findFuncoes();
    }

    public List<String> listarLocaisTrabalho() throws SQLException {
        return repository.findLocaisTrabalho();
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
        if (!(f.codDep() instanceof String c) || c.isBlank()) {
            throw new IllegalArgumentException("Código do funcionário é obrigatório");
        }
        if (!(f.nome() instanceof String n) || n.isBlank()) {
            throw new IllegalArgumentException("Nome é obrigatório");
        }
        if (!(f.funcao() instanceof String func) || func.isBlank()) {
            throw new IllegalArgumentException("Função é obrigatória");
        }
        if (!(f.departamento() instanceof String dept) || dept.isBlank()) {
            throw new IllegalArgumentException("Departamento é obrigatório");
        }

        // Validação de email
        if (f.email() != null && !f.email().isBlank()) {
            if (!isValidEmail(f.email())) {
                throw new IllegalArgumentException("Email inválido: " + f.email());
            }
        }

        // Validação de imagem
        if (f.temImagemPerfil()) {
            validarImagem(f.imagemPerfil(), f.tipoImagem());
        }
    }

    private void validarImagem(byte[] imagem, String tipo) {
        if (imagem == null || imagem.length == 0) {
            return;
        }

        if (!Funcionarios.isTamanhoImagemValido(imagem.length)) {
            throw new IllegalArgumentException("Imagem muito grande. Tamanho máximo: " +
                    Funcionarios.getMaxImageSize() / (1024 * 1024) + "MB");
        }

        if (!Funcionarios.isTipoImagemSuportado(tipo)) {
            throw new IllegalArgumentException("Tipo de imagem não suportado: " + tipo +
                    ". Tipos suportados: " + String.join(", ", Funcionarios.getTiposSuportados()));
        }
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
}