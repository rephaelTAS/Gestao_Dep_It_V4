package com.ossobo.gestaoDepIt.db.models;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;

/**
 * Funcionarios - Modelo imutável com Record (Java 17+)
 * v3.0 - Degrau 1: colunas de sincronização (PK codDep preservada — sem UUID)
 *
 * Schema: funcionarios (cod_dep VARCHAR(45) PRIMARY KEY)
 *
 * Responsabilidades:
 * - Representação imutável de funcionário
 * - Validação de negócio no construtor
 * - Gestão de imagem de perfil (byte[])
 * - Transporte de metadados de sync (LWW + tombstone)
 *
 * Mudanças v2.0 → v3.0:
 * - +deviceId: device da última mutação (desempate LWW; repository preenche na escrita)
 * - +deleted: tombstone absoluto de sincronização
 * - Fábricas geram createdAt/updatedAt + deleted=false
 * - Construtor de 13 argumentos mantido como ponte de compatibilidade
 * - equals/hashCode: campos de sync permanecem fora (semântica v2.0 preservada)
 */
public record Funcionarios(
        String codDep,              // PRIMARY KEY (código de negócio — não é UUID)
        String nome,
        String funcao,
        String departamento,
        String localTrabalho,
        String email,
        String telefone,
        byte[] imagemPerfil,        // BLOB (até 2MB)
        String tipoImagem,          // png, jpg, jpeg, gif
        Integer tamanhoImagem,      // em bytes
        Boolean ativo,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String deviceId,            // sync: origem da última mutação (desempate LWW)
        Boolean deleted             // sync: tombstone absoluto
) {
    // ===== CONSTANTES =====
    private static final int MAX_IMAGE_SIZE = 2 * 1024 * 1024; // 2MB
    private static final String[] TIPOS_SUPORTADOS = {"png", "jpg", "jpeg", "gif"};

    // ===== CONSTRUTOR DE COMPATIBILIDADE (ponte v2.0 → v3.0) =====

    /**
     * Ponte de compatibilidade: mantém chamadores legados (repository v1) compilando.
     * Delega ao canônico com deviceId=null e deleted=false.
     * Código novo deve usar o construtor canônico (15 argumentos).
     */
    public Funcionarios(
            String codDep, String nome, String funcao, String departamento,
            String localTrabalho, String email, String telefone,
            byte[] imagemPerfil, String tipoImagem, Integer tamanhoImagem,
            Boolean ativo, LocalDateTime createdAt, LocalDateTime updatedAt
    ) {
        this(codDep, nome, funcao, departamento,
                localTrabalho, email, telefone,
                imagemPerfil, tipoImagem, tamanhoImagem,
                ativo, createdAt, updatedAt, null, false);
    }

    // ===== CONSTRUTOR COMPACTO (VALIDAÇÃO) =====
    public Funcionarios {
        // Validação com Pattern Matching (Java 16+)
        if (!(codDep instanceof String c) || c.isBlank()) {
            throw new IllegalArgumentException("Código do funcionário é obrigatório");
        }
        if (!(nome instanceof String n) || n.isBlank()) {
            throw new IllegalArgumentException("Nome é obrigatório");
        }
        if (!(funcao instanceof String f) || f.isBlank()) {
            throw new IllegalArgumentException("Função é obrigatória");
        }
        if (!(departamento instanceof String d) || d.isBlank()) {
            throw new IllegalArgumentException("Departamento é obrigatório");
        }

        // Valores padrão
        if (localTrabalho == null) localTrabalho = "";
        if (email == null) email = "";
        if (telefone == null) telefone = "";
        if (ativo == null) ativo = true;

        // Validação de imagem
        if (imagemPerfil != null && imagemPerfil.length > 0) {
            if (!isTamanhoImagemValido(imagemPerfil.length)) {
                throw new IllegalArgumentException("Imagem muito grande. Tamanho máximo: 2MB");
            }
            if (!isTipoImagemSuportado(tipoImagem)) {
                throw new IllegalArgumentException("Tipo de imagem não suportado: " + tipoImagem +
                        ". Tipos suportados: " + Arrays.toString(TIPOS_SUPORTADOS));
            }
        }

        // Se tem imagem, tamanho é calculado automaticamente
        if (imagemPerfil != null && imagemPerfil.length > 0 && tamanhoImagem == null) {
            tamanhoImagem = imagemPerfil.length;
        }

        // sync: registro lido sem a coluna populada = não deletado
        if (deleted == null) deleted = false;
    }

    // ===== CONSTRUTORES DE FÁBRICA (geram timestamps) =====
    // A PK (codDep) vem do chamador — código de matrícula é de negócio, o model não inventa.

    public static Funcionarios novo(
            String codDep,
            String nome,
            String funcao,
            String departamento
    ) {
        LocalDateTime agora = LocalDateTime.now();
        return new Funcionarios(
                codDep, nome, funcao, departamento,
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
                codDep, nome, funcao, departamento,
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
        Integer tamanhoBox = tamanho > 0 ? tamanho : null;
        return new Funcionarios(
                codDep, nome, funcao, departamento,
                localTrabalho, email, telefone, imagemPerfil, tipoImagem, tamanhoBox,
                true, agora, agora, null, false
        );
    }

    // ===== MÉTODOS DE NEGÓCIO =====

    public boolean temImagemPerfil() {
        return imagemPerfil != null && imagemPerfil.length > 0;
    }

    public boolean isAtivo() {
        return Boolean.TRUE.equals(ativo);
    }

    public boolean isInativo() {
        return Boolean.FALSE.equals(ativo);
    }

    public boolean isDeletado() {
        return Boolean.TRUE.equals(deleted);
    }

    public boolean isImagemValida() {
        return temImagemPerfil() && isTamanhoImagemValido(tamanhoImagem);
    }

    /**
     * Formata o tamanho da imagem para leitura humana
     */
    public String getTamanhoImagemFormatado() {
        if (tamanhoImagem == null || tamanhoImagem == 0) return "N/A";

        return switch (tamanhoImagem) {
            case int t when t < 1024 -> t + " B";
            case int t when t < 1024 * 1024 -> String.format("%.1f KB", t / 1024.0);
            default -> String.format("%.1f MB", tamanhoImagem / (1024.0 * 1024.0));
        };
    }

    /**
     * Nome completo formatado (inclui código)
     */
    public String getNomeCompleto() {
        return String.format("%s [%s]", nome, codDep);
    }

    // ===== MÉTODOS DE TRANSFORMAÇÃO =====
    // deviceId nunca é alterado aqui — repository preenche na escrita

    public Funcionarios comAtivo(boolean novoAtivo) {
        return new Funcionarios(
                codDep, nome, funcao, departamento,
                localTrabalho, email, telefone, imagemPerfil, tipoImagem, tamanhoImagem,
                novoAtivo, createdAt, LocalDateTime.now(), deviceId, deleted
        );
    }

    public Funcionarios comNome(String novoNome) {
        return new Funcionarios(
                codDep, novoNome, funcao, departamento,
                localTrabalho, email, telefone, imagemPerfil, tipoImagem, tamanhoImagem,
                ativo, createdAt, LocalDateTime.now(), deviceId, deleted
        );
    }

    public Funcionarios comFuncao(String novaFuncao) {
        return new Funcionarios(
                codDep, nome, novaFuncao, departamento,
                localTrabalho, email, telefone, imagemPerfil, tipoImagem, tamanhoImagem,
                ativo, createdAt, LocalDateTime.now(), deviceId, deleted
        );
    }

    public Funcionarios comDepartamento(String novoDepartamento) {
        return new Funcionarios(
                codDep, nome, funcao, novoDepartamento,
                localTrabalho, email, telefone, imagemPerfil, tipoImagem, tamanhoImagem,
                ativo, createdAt, LocalDateTime.now(), deviceId, deleted
        );
    }

    public Funcionarios comEmail(String novoEmail) {
        return new Funcionarios(
                codDep, nome, funcao, departamento,
                localTrabalho, novoEmail, telefone, imagemPerfil, tipoImagem, tamanhoImagem,
                ativo, createdAt, LocalDateTime.now(), deviceId, deleted
        );
    }

    public Funcionarios comTelefone(String novoTelefone) {
        return new Funcionarios(
                codDep, nome, funcao, departamento,
                localTrabalho, email, novoTelefone, imagemPerfil, tipoImagem, tamanhoImagem,
                ativo, createdAt, LocalDateTime.now(), deviceId, deleted
        );
    }

    public Funcionarios comImagem(byte[] novaImagem, String novoTipo) {
        LocalDateTime agora = LocalDateTime.now();
        int tamanho = novaImagem != null ? novaImagem.length : 0;
        Integer tamanhoBox = tamanho > 0 ? tamanho : null;
        return new Funcionarios(
                codDep, nome, funcao, departamento,
                localTrabalho, email, telefone, novaImagem, novoTipo, tamanhoBox,
                ativo, createdAt, agora, deviceId, deleted
        );
    }

    public Funcionarios semImagem() {
        return new Funcionarios(
                codDep, nome, funcao, departamento,
                localTrabalho, email, telefone, null, null, null,
                ativo, createdAt, LocalDateTime.now(), deviceId, deleted
        );
    }

    /** Tombstone de sincronização: marca exclusão lógica com updatedAt novo (LWW). */
    public Funcionarios comDeletado(boolean deletado) {
        return new Funcionarios(
                codDep, nome, funcao, departamento,
                localTrabalho, email, telefone, imagemPerfil, tipoImagem, tamanhoImagem,
                ativo, createdAt, LocalDateTime.now(), deviceId, deletado
        );
    }

    public Funcionarios comCreatedAt(LocalDateTime data) {
        return new Funcionarios(
                codDep, nome, funcao, departamento,
                localTrabalho, email, telefone, imagemPerfil, tipoImagem, tamanhoImagem,
                ativo, data, updatedAt, deviceId, deleted
        );
    }

    public Funcionarios comUpdatedAt(LocalDateTime data) {
        return new Funcionarios(
                codDep, nome, funcao, departamento,
                localTrabalho, email, telefone, imagemPerfil, tipoImagem, tamanhoImagem,
                ativo, createdAt, data, deviceId, deleted
        );
    }

    // ===== MÉTODOS ESTÁTICOS DE VALIDAÇÃO =====

    public static boolean isTamanhoImagemValido(Integer tamanho) {
        return tamanho != null && tamanho <= MAX_IMAGE_SIZE && tamanho > 0;
    }

    public static boolean isTamanhoImagemValido(int tamanho) {
        return tamanho <= MAX_IMAGE_SIZE && tamanho > 0;
    }

    public static boolean isTipoImagemSuportado(String tipo) {
        if (tipo == null) return false;
        return Arrays.stream(TIPOS_SUPORTADOS)
                .anyMatch(t -> t.equalsIgnoreCase(tipo));
    }

    public static String[] getTiposSuportados() {
        return Arrays.copyOf(TIPOS_SUPORTADOS, TIPOS_SUPORTADOS.length);
    }

    public static int getMaxImageSize() {
        return MAX_IMAGE_SIZE;
    }

    // ===== EQUALS/HASHCODE =====
    // Campos de sync (deviceId, deleted) e timestamps ficam FORA — semântica v2.0:
    // igualdade = conteúdo de negócio. LWW/tombstone comparam updatedAt diretamente.

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Funcionarios that)) return false;
        return Objects.equals(codDep, that.codDep) &&
                Objects.equals(nome, that.nome) &&
                Objects.equals(funcao, that.funcao) &&
                Objects.equals(departamento, that.departamento) &&
                Objects.equals(localTrabalho, that.localTrabalho) &&
                Objects.equals(email, that.email) &&
                Objects.equals(telefone, that.telefone) &&
                Arrays.equals(imagemPerfil, that.imagemPerfil) &&
                Objects.equals(tipoImagem, that.tipoImagem) &&
                Objects.equals(tamanhoImagem, that.tamanhoImagem) &&
                Objects.equals(ativo, that.ativo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codDep, nome, funcao, departamento, localTrabalho,
                email, telefone, Arrays.hashCode(imagemPerfil), tipoImagem,
                tamanhoImagem, ativo);
    }

    @Override
    public String toString() {
        return String.format(
                "Funcionario[COD=%s, Nome=%s, Depto=%s, Imagem=%s]",
                codDep, nome, departamento,
                temImagemPerfil() ? "Sim (" + getTamanhoImagemFormatado() + ")" : "Não"
        );
    }
}