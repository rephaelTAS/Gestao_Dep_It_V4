package com.ossobo.gestaoDepIt.db.models;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Usuario - Modelo imutável com Record (Java 17+)
 * v3.1 - Degrau 1: UUID universal + colunas de sync + fábrica do guest (First-Run)
 *
 * Schema: usuarios (id TEXT PRIMARY KEY — UUID v4)
 *
 * Responsabilidades:
 * - Representação imutável de usuário do sistema
 * - Validação de negócio no construtor
 * - Gestão de autenticação e sessão
 * - Transporte de metadados de sync (LWW + tombstone)
 *
 * Mudanças v3.0 → v3.1:
 * - +GUEST_ID: UUID fixo universal (mesma entidade em todos os devices — converge natural)
 * - +GUEST_FUNC_ID: codDep fixo do funcionário-parceiro (FK obrigatória — Initializer semeia o par)
 * - +guestPadrao(senhaHash): fábrica da conta provisória do First-Run.
 *   Guarda de acesso (só loga com zero ADMINs ativos) vive no AuthService futuro.
 *
 * Mudanças v2.0 → v3.0 (histórico):
 * - id: Long → String (UUID v4, gerado pelas fábricas)
 * - +deviceId: device da última mutação (desempate LWW; repository preenche na escrita)
 * - +deleted: tombstone absoluto de sincronização
 * - toString(): %d → %s
 * - Fábricas geram UUID + createdAt/updatedAt + deleted=false
 */
public record Usuario(
        String id,                   // UUID v4 — gerado na fábrica, nunca pelo construtor
        String funcionarioId,        // FK → funcionarios.cod_dep
        String nome,
        String email,
        String senhaHash,
        String nivelAcesso,          // ADMIN, GESTOR, SUPERVISOR, OPERADOR, READONLY
        Boolean ativo,
        LocalDateTime ultimoLogin,
        String ipUltimoLogin,
        String sessaoAtual,
        LocalDateTime expiracaoSessao,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String deviceId,             // sync: origem da última mutação (desempate LWW)
        Boolean deleted              // sync: tombstone absoluto
) {
    // ===== CONSTANTES =====
    public static final String NIVEL_ADMIN = "ADMIN";
    public static final String NIVEL_GESTOR = "GESTOR";
    public static final String NIVEL_SUPERVISOR = "SUPERVISOR";
    public static final String NIVEL_OPERADOR = "OPERADOR";
    public static final String NIVEL_READONLY = "READONLY";

    public static final List<String> NIVEIS_VALIDOS = List.of(
            NIVEL_ADMIN, NIVEL_GESTOR, NIVEL_SUPERVISOR, NIVEL_OPERADOR, NIVEL_READONLY
    );

    public static final List<String> HIERARQUIA = List.of(
            NIVEL_READONLY, NIVEL_OPERADOR, NIVEL_SUPERVISOR, NIVEL_GESTOR, NIVEL_ADMIN
    );

    // ===== CONSTANTES DO GUEST (First-Run) =====

    /** UUID fixo universal do guest — idêntico em todos os dispositivos (uma única entidade global). */
    public static final String GUEST_ID = "00000000-0000-0000-0000-000000000001";

    /** codDep fixo do funcionário-parceiro do guest (FK obrigatória — Initializer semeia o par). */
    public static final String GUEST_FUNC_ID = "GUEST";

    // ===== CONSTRUTOR COMPACTO (VALIDAÇÃO) =====
    public Usuario {
        if (!(funcionarioId instanceof String func) || func.isBlank()) {
            throw new IllegalArgumentException("ID do funcionário é obrigatório");
        }

        if (!(nome instanceof String n) || n.isBlank()) {
            throw new IllegalArgumentException("Nome é obrigatório");
        }

        if (!(email instanceof String e) || e.isBlank()) {
            throw new IllegalArgumentException("Email é obrigatório");
        }
        if (!isValidEmail(e)) {
            throw new IllegalArgumentException("Email inválido: " + e);
        }

        if (!(senhaHash instanceof String hash) || hash.isBlank()) {
            throw new IllegalArgumentException("Hash da senha é obrigatório");
        }

        if (nivelAcesso == null || nivelAcesso.isBlank()) {
            nivelAcesso = NIVEL_OPERADOR;
        }
        if (!NIVEIS_VALIDOS.contains(nivelAcesso)) {
            throw new IllegalArgumentException("Nível de acesso inválido: " + nivelAcesso +
                    ". Valores permitidos: " + String.join(", ", NIVEIS_VALIDOS));
        }

        if (ativo == null) ativo = true;

        if (ipUltimoLogin == null) ipUltimoLogin = "";
        if (sessaoAtual == null) sessaoAtual = "";

        // sync: registro lido sem a coluna populada = não deletado
        if (deleted == null) deleted = false;
    }

    // ===== CONSTRUTORES DE FÁBRICA (geram UUID + timestamps) =====

    public static Usuario novo(
            String funcionarioId,
            String nome,
            String email,
            String senhaHash
    ) {
        LocalDateTime agora = LocalDateTime.now();
        return new Usuario(
                UUID.randomUUID().toString(),
                funcionarioId, nome, email, senhaHash,
                NIVEL_OPERADOR, true, null, null, null, null,
                agora, agora, null, false
        );
    }

    public static Usuario novoComNivel(
            String funcionarioId,
            String nome,
            String email,
            String senhaHash,
            String nivelAcesso
    ) {
        LocalDateTime agora = LocalDateTime.now();
        return new Usuario(
                UUID.randomUUID().toString(),
                funcionarioId, nome, email, senhaHash,
                nivelAcesso, true, null, null, null, null,
                agora, agora, null, false
        );
    }

    /**
     * Fábrica do guest (First-Run): conta provisória de configuração.
     * Nível ADMIN provisório; guarda de acesso (só loga com zero ADMINs ativos)
     * vive no AuthService futuro — o model permanece puro de infra.
     *
     * @param senhaHash hash BCrypt gerado pelo seed (Initializer) — model não conhece infra
     */
    public static Usuario guestPadrao(String senhaHash) {
        LocalDateTime agora = LocalDateTime.now();
        return new Usuario(
                GUEST_ID, GUEST_FUNC_ID, "Guest", "guest@local", senhaHash,
                NIVEL_ADMIN, true, null, null, null, null,
                agora, agora, null, false
        );
    }

    // ===== MÉTODOS DE NEGÓCIO =====

    public boolean isAtivo() {
        return Boolean.TRUE.equals(ativo);
    }

    public boolean isInativo() {
        return Boolean.FALSE.equals(ativo);
    }

    public boolean isAdmin() {
        return NIVEL_ADMIN.equals(nivelAcesso);
    }

    public boolean isGestor() {
        return NIVEL_GESTOR.equals(nivelAcesso) || isAdmin();
    }

    public boolean isSupervisor() {
        return NIVEL_SUPERVISOR.equals(nivelAcesso) || isGestor();
    }

    public boolean isOperador() {
        return NIVEL_OPERADOR.equals(nivelAcesso) || isSupervisor();
    }

    public boolean isReadOnly() {
        return NIVEL_READONLY.equals(nivelAcesso);
    }

    public boolean isDeletado() {
        return Boolean.TRUE.equals(deleted);
    }

    public boolean hasSessaoAtiva() {
        return sessaoAtual != null && !sessaoAtual.isBlank() &&
                expiracaoSessao != null && expiracaoSessao.isAfter(LocalDateTime.now());
    }

    public boolean isSessaoExpirada() {
        return sessaoAtual != null && !sessaoAtual.isBlank() &&
                expiracaoSessao != null && expiracaoSessao.isBefore(LocalDateTime.now());
    }

    public int getNivelHierarquia() {
        int idx = HIERARQUIA.indexOf(nivelAcesso);
        return idx >= 0 ? idx : 0;
    }

    public boolean hasPermissao(String nivelRequerido) {
        if (nivelRequerido == null) return true;
        int nivelUsuario = getNivelHierarquia();
        int nivelRequeridoIdx = HIERARQUIA.indexOf(nivelRequerido);
        if (nivelRequeridoIdx == -1) return false;
        return nivelUsuario >= nivelRequeridoIdx;
    }

    public String getNivelDescricao() {
        return switch (nivelAcesso) {
            case NIVEL_ADMIN -> "Administrador";
            case NIVEL_GESTOR -> "Gestor";
            case NIVEL_SUPERVISOR -> "Supervisor";
            case NIVEL_OPERADOR -> "Operador";
            case NIVEL_READONLY -> "Leitura Apenas";
            default -> nivelAcesso;
        };
    }

    public String getStatusDescricao() {
        return isAtivo() ? "Ativo" : "Inativo";
    }

    public String getSessaoStatus() {
        if (hasSessaoAtiva()) return "Ativa";
        if (isSessaoExpirada()) return "Expirada";
        return "Nenhuma";
    }

    // ===== MÉTODOS DE TRANSFORMAÇÃO =====
    // deviceId nunca é alterado aqui — repository preenche na escrita

    public Usuario comId(String novoId) {
        return new Usuario(
                novoId, funcionarioId, nome, email, senhaHash,
                nivelAcesso, ativo, ultimoLogin, ipUltimoLogin,
                sessaoAtual, expiracaoSessao, createdAt, updatedAt,
                deviceId, deleted
        );
    }

    public Usuario comSenhaHash(String novaSenhaHash) {
        return new Usuario(
                id, funcionarioId, nome, email, novaSenhaHash,
                nivelAcesso, ativo, ultimoLogin, ipUltimoLogin,
                sessaoAtual, expiracaoSessao, createdAt, LocalDateTime.now(),
                deviceId, deleted
        );
    }

    public Usuario comNivelAcesso(String novoNivel) {
        if (!NIVEIS_VALIDOS.contains(novoNivel)) {
            throw new IllegalArgumentException("Nível inválido: " + novoNivel);
        }
        return new Usuario(
                id, funcionarioId, nome, email, senhaHash,
                novoNivel, ativo, ultimoLogin, ipUltimoLogin,
                sessaoAtual, expiracaoSessao, createdAt, LocalDateTime.now(),
                deviceId, deleted
        );
    }

    public Usuario comAtivo(boolean novoAtivo) {
        return new Usuario(
                id, funcionarioId, nome, email, senhaHash,
                nivelAcesso, novoAtivo, ultimoLogin, ipUltimoLogin,
                null, null, createdAt, LocalDateTime.now(),
                deviceId, deleted
        );
    }

    public Usuario comLogin(String ip) {
        return new Usuario(
                id, funcionarioId, nome, email, senhaHash,
                nivelAcesso, ativo, LocalDateTime.now(), ip,
                sessaoAtual, expiracaoSessao, createdAt, LocalDateTime.now(),
                deviceId, deleted
        );
    }

    public Usuario comSessao(String token, int duracaoMinutos) {
        LocalDateTime expiracao = LocalDateTime.now().plusMinutes(duracaoMinutos);
        return new Usuario(
                id, funcionarioId, nome, email, senhaHash,
                nivelAcesso, ativo, ultimoLogin, ipUltimoLogin,
                token, expiracao, createdAt, LocalDateTime.now(),
                deviceId, deleted
        );
    }

    public Usuario semSessao() {
        return new Usuario(
                id, funcionarioId, nome, email, senhaHash,
                nivelAcesso, ativo, ultimoLogin, ipUltimoLogin,
                null, null, createdAt, LocalDateTime.now(),
                deviceId, deleted
        );
    }

    public Usuario comNome(String novoNome) {
        return new Usuario(
                id, funcionarioId, novoNome, email, senhaHash,
                nivelAcesso, ativo, ultimoLogin, ipUltimoLogin,
                sessaoAtual, expiracaoSessao, createdAt, LocalDateTime.now(),
                deviceId, deleted
        );
    }

    public Usuario comEmail(String novoEmail) {
        if (!isValidEmail(novoEmail)) {
            throw new IllegalArgumentException("Email inválido: " + novoEmail);
        }
        return new Usuario(
                id, funcionarioId, nome, novoEmail, senhaHash,
                nivelAcesso, ativo, ultimoLogin, ipUltimoLogin,
                sessaoAtual, expiracaoSessao, createdAt, LocalDateTime.now(),
                deviceId, deleted
        );
    }

    /** Tombstone de sincronização: marca exclusão lógica com updatedAt novo (LWW). */
    public Usuario comDeletado(boolean deletado) {
        return new Usuario(
                id, funcionarioId, nome, email, senhaHash,
                nivelAcesso, ativo, ultimoLogin, ipUltimoLogin,
                sessaoAtual, expiracaoSessao, createdAt, LocalDateTime.now(),
                deviceId, deletado
        );
    }

    public Usuario comCreatedAt(LocalDateTime data) {
        return new Usuario(
                id, funcionarioId, nome, email, senhaHash,
                nivelAcesso, ativo, ultimoLogin, ipUltimoLogin,
                sessaoAtual, expiracaoSessao, data, updatedAt,
                deviceId, deleted
        );
    }

    public Usuario comUpdatedAt(LocalDateTime data) {
        return new Usuario(
                id, funcionarioId, nome, email, senhaHash,
                nivelAcesso, ativo, ultimoLogin, ipUltimoLogin,
                sessaoAtual, expiracaoSessao, createdAt, data,
                deviceId, deleted
        );
    }

    // ===== MÉTODOS ESTÁTICOS =====

    public static boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    public static List<String> getNiveisValidos() {
        return NIVEIS_VALIDOS;
    }

    public static List<String> getHierarquia() {
        return HIERARQUIA;
    }

    public static int getNivelHierarquia(String nivel) {
        return HIERARQUIA.indexOf(nivel);
    }

    @Override
    public String toString() {
        return String.format("Usuario[ID=%s, Nome=%s, Email=%s, Nivel=%s]",
                id, nome, email, nivelAcesso);
    }
}