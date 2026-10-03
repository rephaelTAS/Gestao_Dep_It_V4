package com.ossobo.gestaoDepIt.db.models;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Objects;

/**
 * Funcionarios v3.1 - Modelo imutável com Record (Java 17+)
 *
 * Schema: funcionarios (id TEXT PRIMARY KEY — UUID v4)
 *         cod_dep TEXT UNIQUE — código de negócio (corrigível)
 *         cod_dep_anterior TEXT — último código antes da transferência
 *
 * DECISÕES RATIFICADAS:
 * - id: String (UUID) — identidade de sync
 * - cod_dep: String (UNIQUE) — código de negócio (corrigível sem cascata)
 * - cod_dep_anterior: String — 1 nível (histórico completo fica no ledger)
 *
 * v3.1 — codDepAnterior para auditoria de transferência de departamento.
 */
public record Funcionarios(
        String id,                  // UUID — nasce no GARGALO do insert se ausente
        String codDep,              // código de negócio — UNIQUE, corrigível
        String codDepAnterior,      // último código antes da transferência ("" se nunca)
        String nome,
        String funcao,
        String departamento,
        String localTrabalho,
        String email,
        String telefone,
        byte[] imagemPerfil,
        String tipoImagem,
        Integer tamanhoImagem,
        Boolean ativo,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String deviceId,
        boolean deletado
) {
    private static final int MAX_IMAGE_SIZE = 2 * 1024 * 1024;
    private static final String[] TIPOS_SUPORTADOS = {"png", "jpg", "jpeg", "gif"};

    public Funcionarios {
        if (id == null || id.isBlank()) {
            id = java.util.UUID.randomUUID().toString();
        }
        if (codDep == null || codDep.isBlank()) {
            throw new IllegalArgumentException("Código do funcionário é obrigatório");
        }
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome é obrigatório");
        }
        if (funcao == null || funcao.isBlank()) {
            throw new IllegalArgumentException("Função é obrigatória");
        }
        if (departamento == null || departamento.isBlank()) {
            throw new IllegalArgumentException("Departamento é obrigatório");
        }

        if (codDepAnterior == null) codDepAnterior = "";
        if (localTrabalho == null) localTrabalho = "";
        if (email == null) email = "";
        if (telefone == null) telefone = "";
        if (ativo == null) ativo = true;
        if (deviceId == null) deviceId = "";

        if (imagemPerfil != null && imagemPerfil.length > 0) {
            if (imagemPerfil.length > MAX_IMAGE_SIZE) {
                throw new IllegalArgumentException("Imagem muito grande. Tamanho máximo: 2MB");
            }
            if (!isTipoImagemSuportado(tipoImagem)) {
                throw new IllegalArgumentException("Tipo de imagem não suportado: " + tipoImagem);
            }
        }
        if (imagemPerfil != null && imagemPerfil.length > 0 && tamanhoImagem == null) {
            tamanhoImagem = imagemPerfil.length;
        }
    }

    public static Funcionarios novo(
            String codDep,
            String nome,
            String funcao,
            String departamento
    ) {
        LocalDateTime agora = LocalDateTime.now();
        return new Funcionarios(
                null, codDep, null, nome, funcao, departamento,
                null, null, null, null, null, null,
                true, agora, agora, null, false
        );
    }

    public static Funcionarios novoCompleto(
            String codDep,
            String nome,
            String funcao,
            String departamento,
            String localTrabalho,
            String email,
            String telefone
    ) {
        LocalDateTime agora = LocalDateTime.now();
        return new Funcionarios(
                null, codDep, null, nome, funcao, departamento,
                localTrabalho, email, telefone, null, null, null,
                true, agora, agora, null, false
        );
    }

    public static Funcionarios novoComImagem(
            String codDep,
            String nome,
            String funcao,
            String departamento,
            String localTrabalho,
            String email,
            String telefone,
            byte[] imagemPerfil,
            String tipoImagem
    ) {
        LocalDateTime agora = LocalDateTime.now();
        int tamanho = imagemPerfil != null ? imagemPerfil.length : 0;
        return new Funcionarios(
                null, codDep, null, nome, funcao, departamento,
                localTrabalho, email, telefone, imagemPerfil, tipoImagem,
                tamanho > 0 ? tamanho : null,
                true, agora, agora, null, false
        );
    }

    public boolean temImagemPerfil() {
        return imagemPerfil != null && imagemPerfil.length > 0;
    }

    public boolean isAtivo() {
        return Boolean.TRUE.equals(ativo);
    }

    public boolean isDeletado() {
        return deletado;
    }

    public boolean foiTransferido() {
        return codDepAnterior != null && !codDepAnterior.isBlank();
    }

    public String getTamanhoImagemFormatado() {
        if (tamanhoImagem == null || tamanhoImagem == 0) return "N/A";
        if (tamanhoImagem < 1024) return tamanhoImagem + " B";
        if (tamanhoImagem < 1024 * 1024) return String.format("%.1f KB", tamanhoImagem / 1024.0);
        return String.format("%.1f MB", tamanhoImagem / (1024.0 * 1024.0));
    }

    public Funcionarios comAtivo(boolean novoAtivo) {
        return new Funcionarios(
                id, codDep, codDepAnterior, nome, funcao, departamento,
                localTrabalho, email, telefone, imagemPerfil, tipoImagem, tamanhoImagem,
                novoAtivo, createdAt, LocalDateTime.now(), deviceId, deletado
        );
    }

    public Funcionarios comDeletado(boolean novoDeletado) {
        return new Funcionarios(
                id, codDep, codDepAnterior, nome, funcao, departamento,
                localTrabalho, email, telefone, imagemPerfil, tipoImagem, tamanhoImagem,
                ativo, createdAt, LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
                deviceId, novoDeletado
        );
    }

    public static boolean isTipoImagemSuportado(String tipo) {
        if (tipo == null) return false;
        return Arrays.stream(TIPOS_SUPORTADOS).anyMatch(t -> t.equalsIgnoreCase(tipo));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Funcionarios that)) return false;
        return deletado == that.deletado &&
                Objects.equals(id, that.id) &&
                Objects.equals(codDep, that.codDep) &&
                Objects.equals(codDepAnterior, that.codDepAnterior) &&
                Objects.equals(nome, that.nome) &&
                Objects.equals(funcao, that.funcao) &&
                Objects.equals(departamento, that.departamento) &&
                Objects.equals(localTrabalho, that.localTrabalho) &&
                Objects.equals(email, that.email) &&
                Objects.equals(telefone, that.telefone) &&
                Arrays.equals(imagemPerfil, that.imagemPerfil) &&
                Objects.equals(tipoImagem, that.tipoImagem) &&
                Objects.equals(tamanhoImagem, that.tamanhoImagem) &&
                Objects.equals(ativo, that.ativo) &&
                Objects.equals(createdAt, that.createdAt) &&
                Objects.equals(updatedAt, that.updatedAt) &&
                Objects.equals(deviceId, that.deviceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, codDep, codDepAnterior, nome, funcao, departamento,
                localTrabalho, email, telefone, Arrays.hashCode(imagemPerfil),
                tipoImagem, tamanhoImagem, ativo, createdAt, updatedAt, deviceId, deletado);
    }

    @Override
    public String toString() {
        return String.format("Funcionario[ID=%s, COD=%s, Nome=%s, Depto=%s, Imagem=%s]",
                id, codDep, nome, departamento,
                temImagemPerfil() ? "Sim (" + getTamanhoImagemFormatado() + ")" : "Não");
    }
}