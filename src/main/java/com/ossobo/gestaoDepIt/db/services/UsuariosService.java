package com.ossobo.gestaoDepIt.db.services;

import com.ossobo.gestaoDepIt.db.config.event.UsuarioEvent;
import com.ossobo.gestaoDepIt.db.enums.Hierarquia;
import com.ossobo.gestaoDepIt.db.models.Usuario;
import com.ossobo.gestaoDepIt.db.repositories.UsuariosRepository;

import com.ossobo.gestaoDepIt.utils.crypthash.BCryptHash;
import com.ossobo.winterfx.anotations.Inject;
import com.ossobo.winterfx.anotations.Service;
import com.ossobo.winterfx.event.EventBus;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * UsuariosService - Regras de negócio com EventBus
 * v2.3 - Alinhado ao Usuario v3.2 e ao enum Hierarquia v1.0
 *
 * Mudanças v2.2 → v2.3:
 * - Assinaturas de nível: String → Hierarquia (fonte única)
 * - promover/rebaixar usam Hierarquia.proximo()/anterior() (aritmética de ordinal)
 * - verificarPermissao delega a Hierarquia.temPermissao()
 * - Removida constante NIVEL_* do Service — quem define nível é o enum
 * - getNiveisAcesso()/getHierarquiaPermissoes() passam a delegar ao enum
 */
@Service
public class UsuariosService {

    private static final System.Logger logger = System.getLogger(UsuariosService.class.getName());

    @Inject
    private UsuariosRepository repository;

    @Inject
    private EventBus eventBus;

    public static final int SESSAO_DURACAO_MINUTOS = 480; // 8 horas

    // ===== CRUD =====

    public List<Usuario> listarTodos() throws SQLException {
        return repository.findAll();
    }

    public List<Usuario> listarTodos(int pagina, int tamanho) throws SQLException {
        int offset = (pagina - 1) * tamanho;
        return repository.findAll(tamanho, offset);
    }

    public List<Usuario> listarAtivos(int pagina, int tamanho) throws SQLException {
        int offset = (pagina - 1) * tamanho;
        return repository.findAllAtivos(tamanho, offset);
    }

    public Optional<Usuario> buscarPorId(String id) throws SQLException {
        return repository.findById(id);
    }

    public Optional<Usuario> buscarPorEmail(String email) throws SQLException {
        return repository.findByEmail(email);
    }

    public Optional<Usuario> buscarPorFuncionarioId(String funcionarioId) throws SQLException {
        return repository.findByFuncionarioId(funcionarioId);
    }

    public Optional<Usuario> buscarPorSessao(String token) throws SQLException {
        return repository.findBySessao(token);
    }

    public Usuario criar(Usuario usuario) throws SQLException {
        validarUsuario(usuario);

        if (repository.existsByEmail(usuario.email())) {
            throw new IllegalArgumentException("Email já utilizado: " + usuario.email());
        }
        if (repository.existsByFuncionarioId(usuario.funcionarioId())) {
            throw new IllegalArgumentException("Funcionário já possui usuário: " + usuario.funcionarioId());
        }

        String senhaHash = usuario.senhaHash();
        if (senhaHash == null || senhaHash.isBlank()) {
            throw new IllegalArgumentException("Senha é obrigatória");
        }
        if (!senhaHash.startsWith("$2a$")) {
            senhaHash = BCryptHash.hash(senhaHash);
        }

        Usuario comHash = usuario.comSenhaHash(senhaHash);

        String id = repository.insert(comHash);
        Usuario criado = repository.findById(id)
                .orElseThrow(() -> new SQLException("Falha ao buscar usuário criado"));

        eventBus.publish(new UsuarioEvent<>(criado, "CRIADO"));
        logger.log(System.Logger.Level.INFO,"✅ Usuário criado: ID={}, Email={}, Nível={}",
                criado.id(), criado.email(), criado.nivelAcesso());

        return criado;
    }

    public Usuario atualizar(Usuario usuario) throws SQLException {
        validarUsuario(usuario);

        if (usuario.id() == null || usuario.id().isBlank()) {
            throw new IllegalArgumentException("ID não pode ser vazio para atualização");
        }

        Usuario existente = requireUsuario(usuario.id());

        Optional<Usuario> porEmail = repository.findByEmail(usuario.email());
        if (porEmail.isPresent() && !porEmail.get().id().equals(usuario.id())) {
            throw new IllegalArgumentException("Email já utilizado por outro usuário: " + usuario.email());
        }

        Optional<Usuario> porFunc = repository.findByFuncionarioId(usuario.funcionarioId());
        if (porFunc.isPresent() && !porFunc.get().id().equals(usuario.id())) {
            throw new IllegalArgumentException("Funcionário já possui outro usuário: " + usuario.funcionarioId());
        }

        String senhaHash = usuario.senhaHash();
        if (senhaHash == null || senhaHash.isBlank() || senhaHash.startsWith("$2a$")) {
            senhaHash = existente.senhaHash();
        } else {
            senhaHash = BCryptHash.hash(senhaHash);
        }

        Usuario paraAtualizar = new Usuario(
                usuario.id(),
                usuario.funcionarioId(),
                usuario.nome(),
                usuario.email(),
                senhaHash,
                usuario.nivelAcesso(),
                usuario.isAtivo(),
                existente.ultimoLogin(),
                existente.ipUltimoLogin(),
                null,
                null,
                existente.createdAt(),
                LocalDateTime.now(),
                null,
                existente.deletado()
        );

        repository.update(paraAtualizar);
        Usuario atualizado = repository.findById(usuario.id())
                .orElseThrow(() -> new SQLException("Falha ao buscar usuário atualizado"));

        eventBus.publish(new UsuarioEvent<>(atualizado, "ATUALIZADO"));
        logger.log(System.Logger.Level.INFO,"✅ Usuário atualizado: ID={}", atualizado.id());

        return atualizado;
    }

    public void excluir(String id) throws SQLException {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Usuário não encontrado: " + id);
        }

        Optional<Usuario> usuario = repository.findById(id);
        repository.invalidarSessao(id);
        repository.delete(id);

        usuario.ifPresent(u -> {
            eventBus.publish(new UsuarioEvent<>(u, "EXCLUIDO"));
            logger.log(System.Logger.Level.INFO,"✅ Usuário excluído: ID={}", id);
        });
    }

    // ===== AUTENTICAÇÃO =====

    public Optional<Usuario> autenticar(String identificador, String senhaTextoPuro) throws SQLException {
        logger.log(System.Logger.Level.DEBUG,"Autenticando: {}", identificador);

        if (identificador == null || identificador.isBlank()
                || senhaTextoPuro == null || senhaTextoPuro.isBlank()) {
            return Optional.empty();
        }

        Optional<Usuario> usuarioOpt = repository.findByFuncionarioId(identificador.trim());
        if (usuarioOpt.isEmpty() && identificador.contains("@")) {
            usuarioOpt = repository.findByEmail(identificador.trim());
        }

        if (usuarioOpt.isPresent()) {
            Usuario user = usuarioOpt.get();

            if (user.isDeletado()) {
                logger.log(System.Logger.Level.WARNING,"Usuário deletado (tombstone): {}", identificador);
                return Optional.empty();
            }
            if (!user.isAtivo()) {
                logger.log(System.Logger.Level.WARNING,"Usuário inativo: {}", identificador);
                return Optional.empty();
            }

            if (BCryptHash.verify(senhaTextoPuro, user.senhaHash())) {
                logger.log(System.Logger.Level.WARNING,"✅ Autenticação bem-sucedida: {}", user.nome());
                return usuarioOpt;
            }
            logger.log(System.Logger.Level.WARNING,"Senha incorreta para: {}", identificador);
        } else {
            logger.log(System.Logger.Level.WARNING,"Usuário não encontrado: {}", identificador);
        }

        return Optional.empty();
    }

    public void registrarLogin(String usuarioId, String ip) throws SQLException {
        if (!repository.existsById(usuarioId)) {
            throw new IllegalArgumentException("Usuário não encontrado: " + usuarioId);
        }

        repository.updateLogin(usuarioId, ip);
        logger.log(System.Logger.Level.INFO,"Login registrado: Usuário={}, IP={}", usuarioId, ip);
    }

    // ===== SESSÃO =====

    public String criarSessao(String usuarioId) throws SQLException {
        return criarSessao(usuarioId, SESSAO_DURACAO_MINUTOS);
    }

    public String criarSessao(String usuarioId, int duracaoMinutos) throws SQLException {
        Usuario usuario = requireUsuario(usuarioId);
        if (!usuario.isAtivo()) {
            throw new IllegalStateException("Usuário inativo: " + usuarioId);
        }
        if (usuario.isDeletado()) {
            throw new IllegalStateException("Usuário deletado: " + usuarioId);
        }

        String token = UUID.randomUUID().toString();
        LocalDateTime expiracao = LocalDateTime.now().plusMinutes(duracaoMinutos);
        repository.updateSessao(usuarioId, token, expiracao);

        logger.log(System.Logger.Level.INFO,"Sessão criada: Usuário={}, Expiração={}", usuarioId, expiracao);
        return token;
    }

    public Optional<Usuario> validarSessao(String token) throws SQLException {
        return repository.findBySessao(token);
    }

    public void invalidarSessao(String usuarioId) throws SQLException {
        if (repository.existsById(usuarioId)) {
            repository.invalidarSessao(usuarioId);
            logger.log(System.Logger.Level.INFO,"Sessão invalidada: Usuário={}", usuarioId);
        }
    }

    public void invalidarTodasSessoes() throws SQLException {
        repository.invalidarTodasSessoes();
        logger.log(System.Logger.Level.INFO,"Todas as sessões foram invalidadas");
    }

    public void limparSessoesExpiradas() throws SQLException {
        repository.invalidarSessoesExpiradas();
        logger.log(System.Logger.Level.INFO,"Sessões expiradas foram limpas");
    }

    // ===== SENHA =====

    public void alterarSenha(String usuarioId, String novaSenhaTextoPuro) throws SQLException {
        requireUsuario(usuarioId);

        String novoHash = BCryptHash.hash(novaSenhaTextoPuro);
        repository.updateSenha(usuarioId, novoHash);
        repository.invalidarSessao(usuarioId);

        logger.log(System.Logger.Level.INFO,"Senha alterada para usuário: {}", usuarioId);
    }

    public void alterarSenhaComValidacao(String usuarioId, String senhaAtual, String novaSenha) throws SQLException {
        Usuario usuario = requireUsuario(usuarioId);

        if (!BCryptHash.verify(senhaAtual, usuario.senhaHash())) {
            throw new IllegalArgumentException("Senha atual incorreta");
        }

        String novoHash = BCryptHash.hash(novaSenha);
        repository.updateSenha(usuarioId, novoHash);
        repository.invalidarSessao(usuarioId);

        logger.log(System.Logger.Level.INFO,"Senha alterada com validação: Usuário={}", usuarioId);
    }

    // ===== ATIVAÇÃO/DESATIVAÇÃO =====

    public void ativar(String usuarioId) throws SQLException {
        Usuario usuario = requireUsuario(usuarioId);
        if (usuario.isAtivo()) {
            throw new IllegalStateException("Usuário já está ativo: " + usuarioId);
        }

        repository.ativar(usuarioId);
        Usuario atualizado = requireUsuario(usuarioId);

        eventBus.publish(new UsuarioEvent<>(atualizado, "ATIVADO"));
        logger.log(System.Logger.Level.INFO,"✅ Usuário ativado: ID={}", usuarioId);
    }

    public void desativar(String usuarioId) throws SQLException {
        Usuario usuario = requireUsuario(usuarioId);
        if (!usuario.isAtivo()) {
            throw new IllegalStateException("Usuário já está inativo: " + usuarioId);
        }

        repository.desativar(usuarioId);
        Usuario atualizado = requireUsuario(usuarioId);

        eventBus.publish(new UsuarioEvent<>(atualizado, "DESATIVADO"));
        logger.log(System.Logger.Level.INFO,"✅ Usuário desativado: ID={}", usuarioId);
    }

    // ===== NÍVEL DE ACESSO =====

    public void atualizarNivelAcesso(String usuarioId, Hierarquia nivel) throws SQLException {
        if (nivel == null) {
            throw new IllegalArgumentException("Nível é obrigatório");
        }

        requireUsuario(usuarioId);

        repository.updateNivelAcesso(usuarioId, nivel.name());

        Usuario atualizado = requireUsuario(usuarioId);

        eventBus.publish(new UsuarioEvent<>(atualizado, "NIVEL_ALTERADO"));
        logger.log(System.Logger.Level.INFO,"✅ Nível alterado: Usuário={} → {}", usuarioId, nivel);
    }

    public void promover(String usuarioId) throws SQLException {
        Usuario usuario = requireUsuario(usuarioId);

        if (usuario.nivelAcesso().isTopo()) {
            throw new IllegalStateException("Usuário já está no nível máximo");
        }

        Hierarquia novoNivel = usuario.nivelAcesso().proximo();
        atualizarNivelAcesso(usuarioId, novoNivel);
        logger.log(System.Logger.Level.INFO,"✅ Usuário promovido: {} → {}", usuarioId, novoNivel);
    }

    public void rebaixar(String usuarioId) throws SQLException {
        Usuario usuario = requireUsuario(usuarioId);

        if (usuario.nivelAcesso().isMinimo()) {
            throw new IllegalStateException("Usuário já está no nível mínimo");
        }

        Hierarquia novoNivel = usuario.nivelAcesso().anterior();
        atualizarNivelAcesso(usuarioId, novoNivel);
        logger.log(System.Logger.Level.INFO,"✅ Usuário rebaixado: {} → {}", usuarioId, novoNivel);
    }

    // ===== CONSULTAS =====

    public List<Usuario> buscarPorNivel(Hierarquia nivel) throws SQLException {
        return repository.findByNivelAcesso(nivel.name());
    }

    public List<Usuario> buscarPorStatus(boolean ativo) throws SQLException {
        return repository.findByStatus(ativo);
    }

    public List<Usuario> buscarPorNome(String nome) throws SQLException {
        return repository.findByNomeContaining(nome);
    }

    public List<Usuario> buscarPorEmailParcial(String email) throws SQLException {
        return repository.findByEmailContaining(email);
    }

    public List<Usuario> buscarComSessaoAtiva() throws SQLException {
        return repository.findComSessaoAtiva();
    }

    public List<Usuario> buscarUsuariosRecentes(int dias) throws SQLException {
        return repository.findRecentesLogin(dias);
    }

    public List<Usuario> buscarUsuariosInativos(int dias) throws SQLException {
        return repository.findInativos(dias);
    }

    public List<Usuario> buscarSessoesPrestesExpirar(int minutos) throws SQLException {
        return repository.findSessoesPrestesExpirar(minutos);
    }

    public List<Usuario> buscarComFiltros(
            String nome,
            String email,
            Hierarquia nivelAcesso,
            Boolean ativo,
            Boolean comSessaoAtiva,
            LocalDateTime dataLoginInicio,
            LocalDateTime dataLoginFim
    ) throws SQLException {
        return repository.findWithFilters(nome, email, nivelAcesso.name(), ativo,
                comSessaoAtiva, dataLoginInicio, dataLoginFim);
    }

    // ===== ESTATÍSTICAS =====

    public Map<String, Integer> obterEstatisticasPorNivel() throws SQLException {
        return repository.countByNivelAcesso();
    }

    public Map<String, Integer> obterEstatisticasAtividade() throws SQLException {
        return repository.getEstatisticasAtividade();
    }

    public List<UsuariosRepository.LoginPorDia> obterLoginsPorPeriodo(
            LocalDateTime inicio, LocalDateTime fim) throws SQLException {
        return repository.getLoginsPorPeriodo(inicio, fim);
    }

    public int contarTotal() throws SQLException {
        return repository.countAll();
    }

    public int contarAtivos() throws SQLException {
        return repository.countAtivos();
    }

    // ===== VERIFICAÇÕES =====

    public boolean existePorId(String id) throws SQLException {
        return repository.existsById(id);
    }

    public boolean emailExiste(String email) throws SQLException {
        return repository.existsByEmail(email);
    }

    public boolean funcionarioTemUsuario(String funcionarioId) throws SQLException {
        return repository.existsByFuncionarioId(funcionarioId);
    }

    public boolean isUsuarioAtivo(String id) throws SQLException {
        return repository.findById(id).map(Usuario::isAtivo).orElse(false);
    }

    // ===== PERMISSÕES =====

    public boolean verificarPermissao(String usuarioId, Hierarquia nivelRequerido) throws SQLException {
        Optional<Usuario> usuario = repository.findById(usuarioId);
        if (usuario.isEmpty() || usuario.get().isDeletado() || !usuario.get().isAtivo()) {
            return false;
        }
        return usuario.get().temPermissao(nivelRequerido);
    }

    public boolean isAdmin(String usuarioId) throws SQLException {
        return verificarPermissao(usuarioId, Hierarquia.NIVEL_ADMIN);
    }

    public boolean isGestorOuSuperior(String usuarioId) throws SQLException {
        return verificarPermissao(usuarioId, Hierarquia.NIVEL_GESTOR);
    }

    public boolean isSupervisorOuSuperior(String usuarioId) throws SQLException {
        return verificarPermissao(usuarioId, Hierarquia.NIVEL_SUPERVISOR);
    }

    // ===== UTILITÁRIOS =====

    public List<String> getNiveisAcesso() {
        return Hierarquia.todos();
    }

    public List<String> getHierarquiaPermissoes() {
        return Hierarquia.todos();
    }

    // ===== MÉTODOS PRIVADOS =====

    private Usuario requireUsuario(String id) throws SQLException {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("ID do usuário é obrigatório");
        }
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + id));
    }

    private void validarUsuario(Usuario u) {
        if (u == null) {
            throw new IllegalArgumentException("Usuário não pode ser nulo");
        }
        if (u.funcionarioId() == null || u.funcionarioId().isBlank()) {
            throw new IllegalArgumentException("ID do funcionário é obrigatório");
        }
        if (u.nome() == null || u.nome().isBlank()) {
            throw new IllegalArgumentException("Nome é obrigatório");
        }
        if (u.email() == null || u.email().isBlank()) {
            throw new IllegalArgumentException("Email é obrigatório");
        }
        if (!Usuario.isValidEmail(u.email())) {
            throw new IllegalArgumentException("Email inválido: " + u.email());
        }
        // nivelAcesso não precisa ser validado — é Hierarquia, tipo forte
    }
}