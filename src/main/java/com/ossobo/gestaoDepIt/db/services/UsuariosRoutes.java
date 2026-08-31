package com.ossobo.gestaoDepIt.db.services;

import com.ossobo.gestaoDepIt.db.models.Usuario;
import com.ossobo.winterfx.anotations.Component;
import com.ossobo.winterfx.anotations.DeleteMapping;
import com.ossobo.winterfx.anotations.ExecMapping;
import com.ossobo.winterfx.anotations.GetMapping;
import com.ossobo.winterfx.anotations.Inject;
import com.ossobo.winterfx.anotations.Payload;
import com.ossobo.winterfx.anotations.PutMapping;
import com.ossobo.winterfx.anotations.RequestMapping;
import com.ossobo.winterfx.anotations.RouteVar;
import com.ossobo.winterfx.router.model.ResponseData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * UsuariosRoutes v1.0
 *
 * Responsabilidade: Fronteira de Internal Routing dos usuários do sistema.
 *                   Handlers FINOS: delegam ao UsuariosService e traduzem
 *                   exceção → ResponseData.
 *
 * 🔒 SEGURANÇA aplicada nesta fronteira:
 *   - SEC-1: toda resposta contendo Usuario sai com senhaHash mascarado
 *     (comSenhaHash(null)). Domínio preservado intacto.
 *   - SEC-2: rotas AUTH/SENHA executam BCrypt (~100–300ms) — CHAMADORES DEVEM
 *     executá-las FORA da JavaFX Thread (CompletableFuture/task).
 *
 * Contratos de payload (documentação viva da API):
 *   Identidade : "id" (Long), "email", "funcionarioid", "token" (sessão).
 *   Login      : EXEC auth/login → "identificador" (codDep OU email),
 *                "senha" (texto puro, NUNCA gravar/logar), "ip" opcional.
 *                Fluxo consolidado: autenticar → registrarLogin → criarSessao.
 *   Senha      : senha/alterar → id + "senha"; senha/com-validacao → id +
 *                "atual" + "nova".
 *   Filtros    : com-filtros aceita 7 chaves OPCIONAIS (omitir = sem critério).
 *
 * v1.0 - Criação; 45 rotas (GET 30 / PUT 8 / DELETE 1 / EXEC 6);
 *        estreias: mascaramento de hash na saída, fluxo de login consolidado,
 *        rota única de permissão parametrizada.
 */
@Component
@RequestMapping("usuarios/service")
public class UsuariosRoutes {

    private static final Logger logger = LoggerFactory.getLogger(UsuariosRoutes.class);

    @Inject
    private UsuariosService service;

    // ============================================================
    // ROTAS — CONSULTAS (GET)
    // ============================================================

    /** Todos os usuários (hash mascarado). */
    @GetMapping("todos")
    public ResponseData todos() {
        return listaMascarada(service::listarTodos, "usuarios");
    }

    /** Lista paginada. Chaves: pagina (1-based), tamanho. */
    @GetMapping("paginados")
    public ResponseData paginados(@RouteVar("pagina") Integer pagina,
                                  @RouteVar("tamanho") Integer tamanho) {
        return listaMascarada(() -> {
            exigirPaginacao(pagina, tamanho);
            return service.listarTodos(pagina, tamanho);
        }, "usuarios");
    }

    /** Lista paginada apenas ATIVOS. Chaves: pagina, tamanho. */
    @GetMapping("ativos/paginados")
    public ResponseData ativosPaginados(@RouteVar("pagina") Integer pagina,
                                        @RouteVar("tamanho") Integer tamanho) {
        return listaMascarada(() -> {
            exigirPaginacao(pagina, tamanho);
            return service.listarAtivos(pagina, tamanho);
        }, "usuarios");
    }

    /** Busca por ID (hash mascarado). */
    @GetMapping("por/id")
    public ResponseData buscarPorId(@RouteVar("id") Long id) {
        return optional(() -> service.buscarPorId(id), "usuario",
                "Usuário não encontrado: ID " + id);
    }

    /** Busca por email exato (hash mascarado). */
    @GetMapping("por/email")
    public ResponseData buscarPorEmail(@RouteVar("email") String email) {
        return optional(() -> service.buscarPorEmail(email), "usuario",
                "Usuário não encontrado: " + email);
    }

    /** Busca pelo vínculo com funcionário (hash mascarado). */
    @GetMapping("por/funcionario")
    public ResponseData buscarPorFuncionario(@RouteVar("funcionarioid") String funcionarioId) {
        return optional(() -> service.buscarPorFuncionarioId(funcionarioId), "usuario",
                "Usuário não encontrado para funcionário: " + funcionarioId);
    }

    /** Busca textual por nome. */
    @GetMapping("por/nome")
    public ResponseData porNome(@RouteVar("nome") String nome) {
        return listaMascarada(() -> service.buscarPorNome(nome), "usuarios");
    }

    /** Busca parcial por email. */
    @GetMapping("por/email-parcial")
    public ResponseData porEmailParcial(@RouteVar("email") String email) {
        return listaMascarada(() -> service.buscarPorEmailParcial(email), "usuarios");
    }

    /** Usuários de um nível de acesso. */
    @GetMapping("por/nivel")
    public ResponseData porNivel(@RouteVar("nivel") String nivel) {
        return listaMascarada(() -> service.buscarPorNivel(nivel), "usuarios");
    }

    /** Somente ativos. */
    @GetMapping("status/ativos")
    public ResponseData ativos() {
        return listaMascarada(() -> service.buscarPorStatus(true), "usuarios");
    }

    /** Somente inativos. */
    @GetMapping("status/inativos")
    public ResponseData inativos() {
        return listaMascarada(() -> service.buscarPorStatus(false), "usuarios");
    }

    /** Usuários com sessão vigente. */
    @GetMapping("com-sessao-ativa")
    public ResponseData comSessaoAtiva() {
        return listaMascarada(service::buscarComSessaoAtiva, "usuarios");
    }

    /** Logins nos últimos N dias. Chave: dias (Integer). */
    @GetMapping("recentes-login")
    public ResponseData recentesLogin(@RouteVar("dias") Integer dias) {
        return listaMascarada(() -> {
            exigirInt(dias, "dias");
            return service.buscarUsuariosRecentes(dias);
        }, "usuarios");
    }

    /** Sem atividade nos últimos N dias. Chave: dias (Integer). */
    @GetMapping("sem-atividade")
    public ResponseData semAtividade(@RouteVar("dias") Integer dias) {
        return listaMascarada(() -> {
            exigirInt(dias, "dias");
            return service.buscarUsuariosInativos(dias);
        }, "usuarios");
    }

    /** Sessões que vencem nos próximos N minutos. Chave: minutos (Integer). */
    @GetMapping("sessoes/prestes-expirar")
    public ResponseData sessoesPrestesExpirar(@RouteVar("minutos") Integer minutos) {
        return listaMascarada(() -> {
            exigirInt(minutos, "minutos");
            return service.buscarSessoesPrestesExpirar(minutos);
        }, "usuarios");
    }

    /**
     * Filtro combinado — TODAS as chaves OPCIONAIS:
     * nome, email, nivel, ativo ("true"/"false"), comsessao ("true"/"false"),
     * logininicio/loginfim (ISO datetime).
     */
    @GetMapping("com-filtros")
    public ResponseData comFiltros(@RouteVar("nome") String nome,
                                   @RouteVar("email") String email,
                                   @RouteVar("nivel") String nivel,
                                   @RouteVar("ativo") Boolean ativo,
                                   @RouteVar("comsessao") Boolean comSessaoAtiva,
                                   @RouteVar("logininicio") String loginInicio,
                                   @RouteVar("loginfim") String loginFim) {
        return listaMascarada(() -> service.buscarComFiltros(
                        nome, email, nivel, ativo, comSessaoAtiva,
                        converterDataHoraOpcional(loginInicio, "logininicio"),
                        converterDataHoraOpcional(loginFim, "loginfim")),
                "usuarios");
    }

    // ============================================================
    // ROTAS — ESTATÍSTICAS (GET)
    // ============================================================

    @GetMapping("stats/nivel")
    public ResponseData statsNivel()          { return valor(service::obterEstatisticasPorNivel, "estatisticas"); }

    @GetMapping("stats/atividade")
    public ResponseData statsAtividade()      { return valor(service::obterEstatisticasAtividade, "estatisticas"); }

    /** Logins agregados no período. Chaves ISO obrigatórias: inicio, fim. */
    @GetMapping("stats/logins-periodo")
    public ResponseData statsLogins(@RouteVar("inicio") String inicio,
                                    @RouteVar("fim") String fim) {
        return valor(() -> service.obterLoginsPorPeriodo(
                converterDataHoraObrigatoria(inicio, "inicio"),
                converterDataHoraObrigatoria(fim, "fim")), "logins");
    }

    @GetMapping("total")
    public ResponseData total()               { return valor(service::contarTotal, "total"); }

    @GetMapping("total/ativos")
    public ResponseData totalAtivos()         { return valor(service::contarAtivos, "total"); }

    // ============================================================
    // ROTAS — DOMÍNIOS E PERMISSÕES (GET)
    // ============================================================

    /** Níveis de acesso válidos (fonte da verdade: domínio). */
    @GetMapping("niveis-validos")
    public ResponseData niveisValidos()       { return valor(service::getNiveisAcesso, "valores"); }

    /** Hierarquia ordenada de permissões. */
    @GetMapping("hierarquia")
    public ResponseData hierarquia()          { return valor(service::getHierarquiaPermissoes, "valores"); }

    /**
     * Verifica se usuário possui o nível requerido (ou superior).
     * Consolida verificarPermissao/isAdmin/isGestor/isSupervisor em um contrato.
     */
    @GetMapping("permissao/check")
    public ResponseData permissaoCheck(@RouteVar("id") Long id,
                                       @RouteVar("nivel") String nivelRequerido) {
        return valor(() -> service.verificarPermissao(id, nivelRequerido), "permitido");
    }

    @GetMapping("exists/id")
    public ResponseData existeId(@RouteVar("id") Long id) {
        return valor(() -> service.existePorId(id), "exists");
    }

    @GetMapping("exists/email")
    public ResponseData existeEmail(@RouteVar("email") String email) {
        return valor(() -> service.emailExiste(email), "exists");
    }

    @GetMapping("exists/funcionario")
    public ResponseData existeFuncionario(@RouteVar("funcionarioid") String funcionarioId) {
        return valor(() -> service.funcionarioTemUsuario(funcionarioId), "exists");
    }

    @GetMapping("ativo/check")
    public ResponseData isAtivo(@RouteVar("id") Long id) {
        return valor(() -> service.isUsuarioAtivo(id), "ativo");
    }

    // ============================================================
    // ROTAS — AUTENTICAÇÃO E SESSÃO (EXEC)
    // ⚠️ SEC-2: TODO canal abaixo executa BCrypt ou toca sessão.
    //    CHAMAR FORA DA FX THREAD.
    // ============================================================

    /**
     * FLUXO CONSOLIDADO DE LOGIN (decisão D1):
     * autentica (identificador = codDep OU email, senha texto puro) →
     * registra login (IP opcional) → cria sessão (duração default 480min).
     * Sucesso: {"conectado": true, "token": "...", "expiramin": ..., "usuario": {...,hash mascarado}}.
     * Falha de credencial: success=false NÃO lança erro genérico de banco.
     * ⚠️ Backlog OBS-U2 (timing) registrado; desktop local = baixo risco.
     */
    @ExecMapping("auth/login")
    public ResponseData login(@RouteVar("identificador") String identificador,
                              @RouteVar("senha") String senha,
                              @RouteVar("ip") String ip) {
        if (identificador == null || identificador.isBlank()) {
            return ResponseData.error("Identificador é obrigatório").withError("identificador", "ausente");
        }
        if (senha == null || senha.isBlank()) {
            return ResponseData.error("Senha é obrigatória").withError("senha", "ausente");
        }

        try {
            Optional<Usuario> opt = service.autenticar(identificador.trim(), senha);

            if (opt.isEmpty()) {
                logger.warn("🔐 Login recusado para '{}'", identificador);
                return ResponseData.error("Credenciais inválidas")
                        .withData("conectado", false);
            }

            Usuario user = opt.get();
            service.registrarLogin(Long.valueOf(user.id()), ip);
            String token = service.criarSessao(Long.valueOf(user.id()));

            logger.info("✅ Login concluído: {} (sessão emitida)", user.nome());
            return ResponseData.success()
                    .withData("conectado", true)
                    .withData("token", token)
                    .withData("expiramin", UsuariosService.SESSAO_DURACAO_MINUTOS)
                    .withData("usuario", user.comSenhaHash(null));

        } catch (SQLException e) {
            return erroBanco(e);
        }
    }

    /** Registra apenas o login (IP/último acesso). Executar após auth externa. */
    @ExecMapping("auth/registrar-login")
    public ResponseData registrarLogin(@RouteVar("id") Long id,
                                       @RouteVar("ip") String ip) {
        return escrita(() -> { service.registrarLogin(id, ip); return "Login registrado"; }, "mensagem");
    }

    /** Cria sessão. Chave "duracaomin" OPCIONAL (default: 480). Retorna token. */
    @ExecMapping("sessao/criar")
    public ResponseData criarSessao(@RouteVar("id") Long id,
                                    @RouteVar("duracaomin") Integer duracaoMin) {
        return escrita(() ->
                        (duracaoMin == null)
                                ? service.criarSessao(id)
                                : service.criarSessao(id, duracaoMin),
                "token");
    }

    /** Valida token e devolve usuário vinculado (null se inválido/expirado). */
    @ExecMapping("sessao/validar")
    public ResponseData validarSessao(@RouteVar("token") String token) {
        try {
            Optional<Usuario> opt = service.validarSessao(token);
            return ResponseData.success()
                    .withData("valida", opt.isPresent())
                    .withData("usuario", opt.map(u -> u.comSenhaHash(null)).orElse(null));
        } catch (SQLException e) {
            return erroBanco(e);
        }
    }

    /** Invalida a sessão do usuário (logout). */
    @ExecMapping("sessao/invalidar")
    public ResponseData invalidarSessao(@RouteVar("id") Long id) {
        return escrita(() -> {
            service.invalidarSessao(id);
            return "Sessão invalidada";
        }, "mensagem");
    }

    /** Logout global (ex.: shutdown do sistema). */
    @ExecMapping("sessao/invalidar-todas")
    public ResponseData invalidarTodas() {
        return escrita(() -> {
            service.invalidarTodasSessoes();
            return "Todas as sessões invalidadas";
        }, "mensagem");
    }

    // ============================================================
    // ROTAS — MANUTENÇÃO (PUT)
    // ============================================================

    /** Remove sessões já expiradas do banco (rotina de housekeeping). */
    @PutMapping("manutencao/limpar-sessoes")
    public ResponseData limparExpiradas() {
        return escrita(() -> {
            service.limparSessoesExpiradas();
            return "Sessões expiradas removidas";
        }, "mensagem");
    }

    // ============================================================
    // ROTAS — SENHA (PUT) ⚠️ SEC-2: FORA DA FX THREAD (BCrypt ×2)
    // ============================================================

    /** Troca direta (uso administrativo). Chaves: id, "senha". */
    @PutMapping("senha/alterar")
    public ResponseData alterarSenha(@RouteVar("id") Long id,
                                     @Payload("senha") String novaSenha) {
        if (novaSenha == null || novaSenha.isBlank()) {
            return ResponseData.error("Nova senha é obrigatória").withError("senha", "ausente");
        }
        return escrita(() -> {
            service.alterarSenha(id, novaSenha);
            return "Senha alterada";
        }, "mensagem");
    }

    /** Troca segura: valida senha atual. Chaves: id, "atual", "nova". */
    @PutMapping("senha/com-validacao")
    public ResponseData alterarSenhaValidada(@RouteVar("id") Long id,
                                             @Payload("atual") String senhaAtual,
                                             @Payload("nova") String novaSenha) {
        if (senhaAtual == null || senhaAtual.isBlank()) {
            return ResponseData.error("Senha atual é obrigatória").withError("atual", "ausente");
        }
        if (novaSenha == null || novaSenha.isBlank()) {
            return ResponseData.error("Nova senha é obrigatória").withError("nova", "ausente");
        }
        return escrita(() -> {
            service.alterarSenhaComValidacao(id, senhaAtual, novaSenha);
            return "Senha alterada com sucesso";
        }, "mensagem");
    }

    // ============================================================
    // ROTAS — CRUD E ESTADO (PUT / DELETE)
    // ============================================================

    /** Cria usuário. Payload: "usuario". Senha vai em senhaHash (texto→hash no domínio). */
    @PutMapping("criar")
    public ResponseData criar(@Payload("usuario") Usuario usuario) {
        return mascaradaEscrita(() -> service.criar(usuario), "usuario");
    }

    /** Atualiza completo. Payload: "usuario". Hash preservado se vazio/$2a$. */
    @PutMapping("atualizar")
    public ResponseData atualizar(@Payload("usuario") Usuario usuario) {
        return mascaradaEscrita(() -> service.atualizar(usuario), "usuario");
    }

    /** Ativa usuário. Publica UsuarioEvent ATIVADO. */
    @PutMapping("ativar")
    public ResponseData ativar(@RouteVar("id") Long id) {
        return escrita(() -> { service.ativar(id); return "Usuário ativado"; }, "mensagem");
    }

    /** Desativa usuário (mantém histórico/vínculos). */
    @PutMapping("desativar")
    public ResponseData desativar(@RouteVar("id") Long id) {
        return escrita(() -> { service.desativar(id); return "Usuário desativado"; }, "mensagem");
    }

    /** Define nível direto. Chave "valor": veja GET niveis-validos. */
    @PutMapping("nivel/atualizar")
    public ResponseData atualizarNivel(@RouteVar("id") Long id,
                                       @RouteVar("valor") String nivel) {
        return escrita(() -> {
            service.atualizarNivelAcesso(id, nivel);
            return "Nível atualizado";
        }, "mensagem");
    }

    /** Sobe um degrau na hierarquia. */
    @PutMapping("promover")
    public ResponseData promover(@RouteVar("id") Long id) {
        return escrita(() -> { service.promover(id); return "Usuário promovido"; }, "mensagem");
    }

    /** Desce um degrau na hierarquia. */
    @PutMapping("rebaixar")
    public ResponseData rebaixar(@RouteVar("id") Long id) {
        return escrita(() -> { service.rebaixar(id); return "Usuário rebaixado"; }, "mensagem");
    }

    /** Exclui usuário (invalida sessão antes). Publica EXCLUIDO. */
    @DeleteMapping("por/id")
    public ResponseData excluir(@RouteVar("id") Long id) {
        return escrita(() -> {
            service.excluir(id);
            return "Usuário excluído";
        }, "mensagem");
    }

    // ============================================================
    // HELPERS DE FRONTEIRA (padrão unificado + mascaramento SEC-1)
    // ============================================================

    @FunctionalInterface
    private interface Acao<T> {
        T executar() throws SQLException;
    }

    private <T> ResponseData escrita(Acao<T> acao, String chave) {
        try {
            return ResponseData.success().withData(chave, acao.executar());
        } catch (IllegalArgumentException | IllegalStateException e) {
            logger.warn("⚠️ Regra de negócio violada: {}", e.getMessage());
            return ResponseData.error(e.getMessage()).withError("negocio", e.getMessage());
        } catch (SQLException e) {
            return erroBanco(e);
        }
    }

    private <T> ResponseData valor(Acao<T> acao, String chave) {
        try {
            return ResponseData.success().withData(chave, acao.executar());
        } catch (SQLException e) {
            return erroBanco(e);
        }
    }

    private ResponseData erroBanco(SQLException e) {
        logger.error("❌ Erro de banco de dados: {}", e.getMessage(), e);
        return ResponseData.error("Erro de banco de dados: " + e.getMessage())
                .withError("banco", e.getMessage());
    }

    /** Lista de usuários com hash mascarado na saída. */
    private ResponseData listaMascarada(Acao<List<?>> acao, String chave) {
        try {
            List<?> bruto = acao.executar();
            List<?> segura = bruto.stream()
                    .map(o -> (o instanceof Usuario u) ? u.comSenhaHash(null) : o)
                    .toList();
            return ResponseData.success()
                    .withData(chave, segura)
                    .withData("total", segura.size());
        } catch (SQLException e) {
            return erroBanco(e);
        }
    }

    /** Optional contendo Usuario → hash mascarado; vazio → erro semântico. */
    private ResponseData optional(Acao<Optional<?>> acao, String chave, String msgVazio) {
        try {
            Object valor = acao.executar().orElse(null);
            if (valor == null) {
                return ResponseData.error(msgVazio).withError(chave, "não encontrado");
            }
            Object seguro = (valor instanceof Usuario u) ? u.comSenhaHash(null) : valor;
            return ResponseData.success().withData(chave, seguro);
        } catch (SQLException e) {
            return erroBanco(e);
        }
    }

    /** Escrita cujo resultado é Usuario → hash mascarado. */
    private ResponseData mascaradaEscrita(Acao<?> acao, String chave) {
        try {
            Object resultado = acao.executar();
            Object seguro = (resultado instanceof Usuario u) ? u.comSenhaHash(null) : resultado;
            return ResponseData.success().withData(chave, seguro);
        } catch (IllegalArgumentException | IllegalStateException e) {
            logger.warn("⚠️ Regra de negócio violada: {}", e.getMessage());
            return ResponseData.error(e.getMessage()).withError("negocio", e.getMessage());
        } catch (SQLException e) {
            return erroBanco(e);
        }
    }

    private void exigirPaginacao(Integer pagina, Integer tamanho) {
        if (pagina == null || pagina < 1 || tamanho == null || tamanho < 1) {
            throw new IllegalArgumentException("Página e tamanho devem ser >= 1");
        }
    }

    private void exigirInt(Integer valor, String campo) {
        if (valor == null || valor < 1) {
            throw new IllegalArgumentException(campo + " deve ser >= 1");
        }
    }

    // ============================================================
    // CONVERSÕES ISOLADAS (String → LocalDateTime)
    // ============================================================

    private LocalDateTime converterDataHoraOpcional(String bruto, String campo) {
        if (bruto == null || bruto.isBlank()) {
            return null;
        }
        return parseDataHora(bruto, campo);
    }

    private LocalDateTime converterDataHoraObrigatoria(String bruto, String campo) {
        if (bruto == null || bruto.isBlank()) {
            throw new IllegalArgumentException("Data/hora obrigatória: " + campo);
        }
        return parseDataHora(bruto, campo);
    }

    private LocalDateTime parseDataHora(String bruto, String campo) {
        try {
            return LocalDateTime.parse(bruto.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Data/hora inválida em '" + campo + "': '" + bruto +
                            "' (use yyyy-MM-ddTHH:mm:ss)");
        }
    }
}