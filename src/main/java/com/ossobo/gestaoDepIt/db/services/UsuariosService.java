package com.ossobo.gestaoDepIt.db.services;

import com.ossobo.gestaoDepIt.db.config.event.UsuarioEvent;
import com.ossobo.gestaoDepIt.db.models.Usuario;
import com.ossobo.gestaoDepIt.db.repositories.UsuariosRepository;
import com.ossobo.gestaoDepIt.utils.crypthash.BCryptHash;

import com.ossobo.winterfx.anotations.Inject;
import com.ossobo.winterfx.anotations.Service;
import com.ossobo.winterfx.event.EventBus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * UsuariosService - Regras de negócio com EventBus
 * v2.0 - Migrado para Java 17+ com WinterFX
 *
 * Responsabilidades:
 * - Gerenciar usuários do sistema
 * - Autenticação com BCrypt
 * - Gestão de sessão com UUID
 * - Publicar eventos (@UsuarioEvent)
 */
@Service
public class UsuariosService {

    private static final Logger logger = LoggerFactory.getLogger(UsuariosService.class);

    @Inject
    private UsuariosRepository repository;

    @Inject
    private EventBus eventBus;

    // ===== CONSTANTES =====
    public static final String NIVEL_ADMIN = Usuario.NIVEL_ADMIN;
    public static final String NIVEL_GESTOR = Usuario.NIVEL_GESTOR;
    public static final String NIVEL_SUPERVISOR = Usuario.NIVEL_SUPERVISOR;
    public static final String NIVEL_OPERADOR = Usuario.NIVEL_OPERADOR;
    public static final String NIVEL_READONLY = Usuario.NIVEL_READONLY;

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

    public Optional<Usuario> buscarPorId(Long id) throws SQLException {
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

        // Hash da senha com BCrypt
        String senhaHash = usuario.senhaHash();
        if (senhaHash != null && !senhaHash.startsWith("$2a$")) {
            senhaHash = BCryptHash.hash(senhaHash);
        } else if (senhaHash == null || senhaHash.isBlank()) {
            throw new IllegalArgumentException("Senha é obrigatória");
        }

        Usuario comHash = usuario.comSenhaHash(senhaHash);

        Long id = repository.insert(comHash);
        Usuario criado = repository.findById(id)
                .orElseThrow(() -> new SQLException("Falha ao buscar usuário criado"));

        eventBus.publish(new UsuarioEvent<>(criado, "CRIADO"));
        logger.info("✅ Usuário criado: ID={}, Email={}, Nível={}",
                criado.id(), criado.email(), criado.nivelAcesso());

        return criado;
    }

    public Usuario atualizar(Usuario usuario) throws SQLException {
        validarUsuario(usuario);

        if (usuario.id() == null) {
            throw new IllegalArgumentException("ID não pode ser nulo para atualização");
        }

        Optional<Usuario> existente = repository.findById(usuario.id());
        if (existente.isEmpty()) {
            throw new IllegalArgumentException("Usuário não encontrado: " + usuario.id());
        }

        // Verifica email duplicado
        Optional<Usuario> porEmail = repository.findByEmail(usuario.email());
        if (porEmail.isPresent() && !porEmail.get().id().equals(usuario.id())) {
            throw new IllegalArgumentException("Email já utilizado por outro usuário: " + usuario.email());
        }

        // Verifica funcionário duplicado
        Optional<Usuario> porFunc = repository.findByFuncionarioId(usuario.funcionarioId());
        if (porFunc.isPresent() && !porFunc.get().id().equals(usuario.id())) {
            throw new IllegalArgumentException("Funcionário já possui outro usuário: " + usuario.funcionarioId());
        }

        // Mantém a senha existente se não for fornecida nova
        String senhaHash = usuario.senhaHash();
        if (senhaHash == null || senhaHash.isBlank() || senhaHash.startsWith("$2a$")) {
            senhaHash = existente.get().senhaHash();
        } else {
            senhaHash = BCryptHash.hash(senhaHash);
        }

        Usuario paraAtualizar = new Usuario(
                usuario.id(), usuario.funcionarioId(), usuario.nome(),
                usuario.email(), senhaHash, usuario.nivelAcesso(),
                usuario.isAtivo(), null, null, null, null,
                null, LocalDateTime.now()
        );

        repository.update(paraAtualizar);
        Usuario atualizado = repository.findById(usuario.id())
                .orElseThrow(() -> new SQLException("Falha ao buscar usuário atualizado"));

        eventBus.publish(new UsuarioEvent<>(atualizado, "ATUALIZADO"));
        logger.info("✅ Usuário atualizado: ID={}", atualizado.id());

        return atualizado;
    }

    public void excluir(Long id) throws SQLException {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Usuário não encontrado: " + id);
        }

        Optional<Usuario> usuario = repository.findById(id);
        repository.invalidarSessao(id);
        repository.delete(id);

        usuario.ifPresent(u -> {
            eventBus.publish(new UsuarioEvent<>(u, "EXCLUIDO"));
            logger.info("✅ Usuário excluído: ID={}", id);
        });
    }

    // ===== AUTENTICAÇÃO =====

    public Optional<Usuario> autenticar(String identificador, String senhaTextoPuro) throws SQLException {
        logger.debug("Autenticando: {}", identificador);

        if (identificador == null || identificador.isBlank()) {
            return Optional.empty();
        }

        // Tenta buscar por funcionario_id ou email
        Optional<Usuario> usuarioOpt = repository.findByFuncionarioId(identificador.trim());
        if (usuarioOpt.isEmpty() && identificador.contains("@")) {
            usuarioOpt = repository.findByEmail(identificador.trim());
        }

        if (usuarioOpt.isPresent()) {
            Usuario user = usuarioOpt.get();
            if (!user.isAtivo()) {
                logger.warn("Usuário inativo: {}", identificador);
                return Optional.empty();
            }

            if (BCryptHash.verify(senhaTextoPuro, user.senhaHash())) {
                logger.info("✅ Autenticação bem-sucedida: {}", user.nome());
                return usuarioOpt;
            } else {
                logger.warn("Senha incorreta para: {}", identificador);
            }
        } else {
            logger.warn("Usuário não encontrado: {}", identificador);
        }

        return Optional.empty();
    }

    public void registrarLogin(Long usuarioId, String ip) throws SQLException {
        if (!repository.existsById(usuarioId)) {
            throw new IllegalArgumentException("Usuário não encontrado: " + usuarioId);
        }

        repository.updateLogin(usuarioId, ip);
        logger.info("Login registrado: Usuário={}, IP={}", usuarioId, ip);
    }

    // ===== SESSÃO =====

    public String criarSessao(Long usuarioId) throws SQLException {
        return criarSessao(usuarioId, SESSAO_DURACAO_MINUTOS);
    }

    public String criarSessao(Long usuarioId, int duracaoMinutos) throws SQLException {
        Optional<Usuario> usuario = repository.findById(usuarioId);
        if (usuario.isEmpty()) {
            throw new IllegalArgumentException("Usuário não encontrado: " + usuarioId);
        }
        if (!usuario.get().isAtivo()) {
            throw new IllegalStateException("Usuário inativo: " + usuarioId);
        }

        String token = UUID.randomUUID().toString();
        LocalDateTime expiracao = LocalDateTime.now().plusMinutes(duracaoMinutos);
        repository.updateSessao(usuarioId, token, expiracao);

        logger.info("Sessão criada: Usuário={}, Expiração={}", usuarioId, expiracao);
        return token;
    }

    public Optional<Usuario> validarSessao(String token) throws SQLException {
        return repository.findBySessao(token);
    }

    public void invalidarSessao(Long usuarioId) throws SQLException {
        if (repository.existsById(usuarioId)) {
            repository.invalidarSessao(usuarioId);
            logger.info("Sessão invalidada: Usuário={}", usuarioId);
        }
    }

    public void invalidarTodasSessoes() throws SQLException {
        repository.invalidarTodasSessoes();
        logger.info("Todas as sessões foram invalidadas");
    }

    public void limparSessoesExpiradas() throws SQLException {
        repository.invalidarSessoesExpiradas();
        logger.info("Sessões expiradas foram limpas");
    }

    // ===== SENHA =====

    public void alterarSenha(Long usuarioId, String novaSenhaTextoPuro) throws SQLException {
        if (!repository.existsById(usuarioId)) {
            throw new IllegalArgumentException("Usuário não encontrado: " + usuarioId);
        }

        String novoHash = BCryptHash.hash(novaSenhaTextoPuro);
        repository.updateSenha(usuarioId, novoHash);
        repository.invalidarSessao(usuarioId);

        logger.info("Senha alterada para usuário: {}", usuarioId);
    }

    public void alterarSenhaComValidacao(Long usuarioId, String senhaAtual, String novaSenha) throws SQLException {
        Optional<Usuario> usuario = repository.findById(usuarioId);
        if (usuario.isEmpty()) {
            throw new IllegalArgumentException("Usuário não encontrado: " + usuarioId);
        }

        if (!BCryptHash.verify(senhaAtual, usuario.get().senhaHash())) {
            throw new IllegalArgumentException("Senha atual incorreta");
        }

        String novoHash = BCryptHash.hash(novaSenha);
        repository.updateSenha(usuarioId, novoHash);
        repository.invalidarSessao(usuarioId);

        logger.info("Senha alterada com validação: Usuário={}", usuarioId);
    }

    // ===== ATIVAÇÃO/DESATIVAÇÃO =====

    public void ativar(Long usuarioId) throws SQLException {
        Optional<Usuario> usuario = repository.findById(usuarioId);
        if (usuario.isEmpty()) {
            throw new IllegalArgumentException("Usuário não encontrado: " + usuarioId);
        }
        if (usuario.get().isAtivo()) {
            throw new IllegalStateException("Usuário já está ativo: " + usuarioId);
        }

        repository.ativar(usuarioId);
        Usuario atualizado = repository.findById(usuarioId)
                .orElseThrow(() -> new SQLException("Falha ao buscar usuário"));

        eventBus.publish(new UsuarioEvent<>(atualizado, "ATIVADO"));
        logger.info("✅ Usuário ativado: ID={}", usuarioId);
    }

    public void desativar(Long usuarioId) throws SQLException {
        Optional<Usuario> usuario = repository.findById(usuarioId);
        if (usuario.isEmpty()) {
            throw new IllegalArgumentException("Usuário não encontrado: " + usuarioId);
        }
        if (usuario.get().isInativo()) {
            throw new IllegalStateException("Usuário já está inativo: " + usuarioId);
        }

        repository.desativar(usuarioId);
        Usuario atualizado = repository.findById(usuarioId)
                .orElseThrow(() -> new SQLException("Falha ao buscar usuário"));

        eventBus.publish(new UsuarioEvent<>(atualizado, "DESATIVADO"));
        logger.info("✅ Usuário desativado: ID={}", usuarioId);
    }

    // ===== NÍVEL DE ACESSO =====

    public void atualizarNivelAcesso(Long usuarioId, String nivel) throws SQLException {
        if (!Usuario.NIVEIS_VALIDOS.contains(nivel)) {
            throw new IllegalArgumentException("Nível inválido: " + nivel);
        }

        if (!repository.existsById(usuarioId)) {
            throw new IllegalArgumentException("Usuário não encontrado: " + usuarioId);
        }

        repository.updateNivelAcesso(usuarioId, nivel);

        Usuario atualizado = repository.findById(usuarioId)
                .orElseThrow(() -> new SQLException("Falha ao buscar usuário"));

        eventBus.publish(new UsuarioEvent<>(atualizado, "NIVEL_ALTERADO"));
        logger.info("✅ Nível alterado: Usuário={} → {}", usuarioId, nivel);
    }

    public void promover(Long usuarioId) throws SQLException {
        Optional<Usuario> usuario = repository.findById(usuarioId);
        if (usuario.isEmpty()) {
            throw new IllegalArgumentException("Usuário não encontrado");
        }

        List<String> hierarquia = Usuario.getHierarquia();
        int indiceAtual = hierarquia.indexOf(usuario.get().nivelAcesso());
        if (indiceAtual >= hierarquia.size() - 1) {
            throw new IllegalStateException("Usuário já está no nível máximo");
        }

        atualizarNivelAcesso(usuarioId, hierarquia.get(indiceAtual + 1));
        logger.info("✅ Usuário promovido: {} → {}", usuarioId, hierarquia.get(indiceAtual + 1));
    }

    public void rebaixar(Long usuarioId) throws SQLException {
        Optional<Usuario> usuario = repository.findById(usuarioId);
        if (usuario.isEmpty()) {
            throw new IllegalArgumentException("Usuário não encontrado");
        }

        List<String> hierarquia = Usuario.getHierarquia();
        int indiceAtual = hierarquia.indexOf(usuario.get().nivelAcesso());
        if (indiceAtual <= 0) {
            throw new IllegalStateException("Usuário já está no nível mínimo");
        }

        atualizarNivelAcesso(usuarioId, hierarquia.get(indiceAtual - 1));
        logger.info("✅ Usuário rebaixado: {} → {}", usuarioId, hierarquia.get(indiceAtual - 1));
    }

    // ===== CONSULTAS =====

    public List<Usuario> buscarPorNivel(String nivel) throws SQLException {
        return repository.findByNivelAcesso(nivel);
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
            String nivelAcesso,
            Boolean ativo,
            Boolean comSessaoAtiva,
            LocalDateTime dataLoginInicio,
            LocalDateTime dataLoginFim
    ) throws SQLException {
        return repository.findWithFilters(nome, email, nivelAcesso, ativo,
                comSessaoAtiva, dataLoginInicio, dataLoginFim);
    }

    // ===== ESTATÍSTICAS =====

    public Map<String, Integer> obterEstatisticasPorNivel() throws SQLException {
        return repository.countByNivelAcesso();
    }

    public Map<String, Integer> obterEstatisticasAtividade() throws SQLException {
        return repository.getEstatisticasAtividade();
    }

    public List<Object[]> obterLoginsPorPeriodo(LocalDateTime inicio, LocalDateTime fim) throws SQLException {
        return repository.getLoginsPorPeriodo(inicio, fim);
    }

    public int contarTotal() throws SQLException {
        return repository.countAll();
    }

    public int contarAtivos() throws SQLException {
        return repository.countAtivos();
    }

    // ===== VERIFICAÇÕES =====

    public boolean existePorId(Long id) throws SQLException {
        return repository.existsById(id);
    }

    public boolean emailExiste(String email) throws SQLException {
        return repository.existsByEmail(email);
    }

    public boolean funcionarioTemUsuario(String funcionarioId) throws SQLException {
        return repository.existsByFuncionarioId(funcionarioId);
    }

    public boolean isUsuarioAtivo(Long id) throws SQLException {
        Optional<Usuario> usuario = repository.findById(id);
        return usuario.map(Usuario::isAtivo).orElse(false);
    }

    // ===== PERMISSÕES =====

    public boolean verificarPermissao(Long usuarioId, String nivelRequerido) throws SQLException {
        Optional<Usuario> usuario = repository.findById(usuarioId);
        if (usuario.isEmpty() || !usuario.get().isAtivo()) return false;
        return usuario.get().hasPermissao(nivelRequerido);
    }

    public boolean isAdmin(Long usuarioId) throws SQLException {
        return verificarPermissao(usuarioId, NIVEL_ADMIN);
    }

    public boolean isGestorOuSuperior(Long usuarioId) throws SQLException {
        return verificarPermissao(usuarioId, NIVEL_GESTOR);
    }

    public boolean isSupervisorOuSuperior(Long usuarioId) throws SQLException {
        return verificarPermissao(usuarioId, NIVEL_SUPERVISOR);
    }

    // ===== UTILITÁRIOS =====

    public List<String> getNiveisAcesso() {
        return Usuario.getNiveisValidos();
    }

    public List<String> getHierarquiaPermissoes() {
        return Usuario.getHierarquia();
    }

    // ===== MÉTODO PRIVADO =====

    private void validarUsuario(Usuario u) {
        if (!(u.funcionarioId() instanceof String f) || f.isBlank()) {
            throw new IllegalArgumentException("ID do funcionário é obrigatório");
        }
        if (!(u.nome() instanceof String n) || n.isBlank()) {
            throw new IllegalArgumentException("Nome é obrigatório");
        }
        if (!(u.email() instanceof String e) || e.isBlank()) {
            throw new IllegalArgumentException("Email é obrigatório");
        }
        if (!Usuario.isValidEmail(e)) {
            throw new IllegalArgumentException("Email inválido: " + e);
        }
        if (u.nivelAcesso() != null && !Usuario.NIVEIS_VALIDOS.contains(u.nivelAcesso())) {
            throw new IllegalArgumentException("Nível de acesso inválido: " + u.nivelAcesso());
        }
    }
}