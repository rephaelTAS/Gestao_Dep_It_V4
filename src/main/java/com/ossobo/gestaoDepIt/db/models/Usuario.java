package com.ossobo.gestaoDepIt.db.models;

import com.ossobo.gestaoDepIt.db.enums.Hierarquia;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Usuario v3.3 - Modelo imutável com Record (Java 17+)
 *
 * Schema: usuarios (id TEXT PRIMARY KEY — UUID v4)
 *
 * CONSTANTES DO GUEST:
 * - GUEST_ID: UUID fixo universal (mesma entidade em todos os devices)
 * - GUEST_FUNC_ID: codDep fixo do funcionário-parceiro
 *
 * v3.3 — senhaHash opcional no record: normaliza null/branco para "".
 *        O record serve leitura (mascaramento SEC-1) E escrita. Exigir hash
 *        aqui bloqueava comSenhaHash(null) e semSenha(). A obrigatoriedade
 *        do hash na CRIAÇÃO é regra de serviço (UsuariosService.criar).
 *
 * v3.2 — nivelAcesso passa de String para Hierarquia (enum fonte única).
 *        Removidas as listas NIVEIS_VALIDOS e HIERARQUIA — vivem no enum.
 *        Persistência continua TEXT (name() do enum) — contrato do banco intacto.
 */
public record Usuario(
        String id,
        String funcionarioId,
        String nome,
        String email,
        String senhaHash,
        Hierarquia nivelAcesso,
        Boolean ativo,
        LocalDateTime ultimoLogin,
        String ipUltimoLogin,
        String sessaoAtual,
        LocalDateTime expiracaoSessao,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String deviceId,
        boolean deletado
) {
    // Guest constants
    public static final String GUEST_ID = "00000000-0000-0000-0000-000000000001";
    public static final String GUEST_FUNC_ID = "GUEST";

    public Usuario {
        // id nasce no GARGALO se ausente (identidade de sync).
        if (id == null || id.isBlank()) {
            id = java.util.UUID.randomUUID().toString();
        }
        if (funcionarioId == null || funcionarioId.isBlank()) {
            throw new IllegalArgumentException("ID do funcionário é obrigatório");
        }
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome é obrigatório");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email é obrigatório");
        }
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Email inválido: " + email);
        }
        // senhaHash vazio = objeto mascarado (SEC-1) ou pré-criação.
        // A obrigatoriedade é validada no UsuariosService.criar.
        if (senhaHash == null) senhaHash = "";

        if (nivelAcesso == null) nivelAcesso = Hierarquia.OPERADOR;
        if (ativo == null) ativo = true;
        if (ipUltimoLogin == null) ipUltimoLogin = "";
        if (sessaoAtual == null) sessaoAtual = "";
        if (deviceId == null) deviceId = "";
    }

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
                Hierarquia.OPERADOR, true, null, null, null, null,
                agora, agora, null, false
        );
    }

    /**
     * Cópia para transporte (hash removido).
     * O construtor compacto aceita senhaHash vazio desde a v3.3.
     */
    public Usuario semSenha() {
        return new Usuario(
                id, funcionarioId, nome, email, "",
                nivelAcesso, ativo, ultimoLogin, ipUltimoLogin,
                sessaoAtual, expiracaoSessao, createdAt, updatedAt,
                deviceId, deletado
        );
    }

    public static Usuario novoComNivel(
            String funcionarioId,
            String nome,
            String email,
            String senhaHash,
            Hierarquia nivelAcesso
    ) {
        LocalDateTime agora = LocalDateTime.now();
        return new Usuario(
                UUID.randomUUID().toString(),
                funcionarioId, nome, email, senhaHash,
                nivelAcesso, true, null, null, null, null,
                agora, agora, null, false
        );
    }

    public static Usuario guestPadrao(String senhaHash) {
        LocalDateTime agora = LocalDateTime.now();
        return new Usuario(
                GUEST_ID, GUEST_FUNC_ID, "Guest", "guest@local", senhaHash,
                Hierarquia.ADMIN, true, null, null, null, null,
                agora, agora, null, false
        );
    }

    public boolean isAtivo() {
        return Boolean.TRUE.equals(ativo);
    }

    public boolean isAdmin() {
        return nivelAcesso == Hierarquia.ADMIN;
    }

    public boolean isDeletado() {
        return deletado;
    }

    public boolean hasSessaoAtiva() {
        return sessaoAtual != null && !sessaoAtual.isBlank() &&
                expiracaoSessao != null && expiracaoSessao.isAfter(LocalDateTime.now());
    }

    /** true se este usuário possui o nível requerido ou superior. */
    public boolean temPermissao(Hierarquia requerido) {
        return nivelAcesso != null && nivelAcesso.temPermissao(requerido);
    }

    public String getNivelDescricao() {
        return switch (nivelAcesso) {
            case ADMIN -> "Administrador";
            case GESTOR -> "Gestor";
            case SUPERVISOR -> "Supervisor";
            case OPERADOR -> "Operador";
            case READONLY -> "Leitura Apenas";
        };
    }

    public static boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    public Usuario comSenhaHash(String novaSenhaHash) {
        return new Usuario(
                id, funcionarioId, nome, email, novaSenhaHash,
                nivelAcesso, ativo, ultimoLogin, ipUltimoLogin,
                sessaoAtual, expiracaoSessao, createdAt, LocalDateTime.now(),
                deviceId, deletado
        );
    }

    public Usuario comAtivo(boolean novoAtivo) {
        return new Usuario(
                id, funcionarioId, nome, email, senhaHash,
                nivelAcesso, novoAtivo, ultimoLogin, ipUltimoLogin,
                null, null, createdAt, LocalDateTime.now(),
                deviceId, deletado
        );
    }

    public Usuario comNivel(Hierarquia novoNivel) {
        return new Usuario(
                id, funcionarioId, nome, email, senhaHash,
                novoNivel, ativo, ultimoLogin, ipUltimoLogin,
                sessaoAtual, expiracaoSessao, createdAt, LocalDateTime.now(),
                deviceId, deletado
        );
    }

    public Usuario comDeletado(boolean novoDeletado) {
        return new Usuario(
                id, funcionarioId, nome, email, senhaHash,
                nivelAcesso, ativo, ultimoLogin, ipUltimoLogin,
                sessaoAtual, expiracaoSessao, createdAt, LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
                deviceId, novoDeletado
        );
    }

    @Override
    public String toString() {
        return String.format("Usuario[ID=%s, Nome=%s, Email=%s, Nivel=%s]",
                id, nome, email, nivelAcesso);
    }
}