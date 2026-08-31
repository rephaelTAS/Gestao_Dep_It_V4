package com.ossobo.gestaoDepIt.db.models;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * GestaoToners - Modelo imutável com Record (Java 17+)
 * v3.0 - Degrau 1: UUID universal + FKs migradas para UUID (S3) + colunas de sync
 *
 * Schema: gestao_toners (id TEXT PRIMARY KEY — UUID v4)
 *
 * Responsabilidades:
 * - Representação imutável de gestão de toners
 * - Validação de negócio no construtor
 * - Métodos auxiliares para estado do toner
 * - Transporte de metadados de sync (LWW + tombstone)
 *
 * Mudanças v2.1 → v3.0:
 * - id: Long → String (UUID v4, gerado pelas fábricas)
 * - inventarioId: Long → String (FK aponta para UUID de inventario_equipamentos — S3)
 * - usuarioResponsavel: Long → String (FK aponta para UUID de usuarios — S3)
 * - +deviceId: device da última mutação (desempate LWW; repository preenche na escrita)
 * - +deleted: tombstone absoluto de sincronização
 * - Fábricas geram UUID + timestamps + deleted=false
 * - toString(): %d → %s (id e FKs agora são String)
 */
public record GestaoToners(
        String id,                   // UUID v4 — gerado na fábrica, nunca pelo construtor
        String inventarioId,         // FK → inventario_equipamentos.id (UUID — S3)
        String skuProduto,           // FK → catalogo_produtos.sku
        LocalDate dataInstalacao,
        String usuarioResponsavel,   // FK → usuarios.id (UUID — S3)
        Integer percentagemRestante, // 0-100
        Integer ciclosImpressao,
        String observacoes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String deviceId,             // sync: origem da última mutação (desempate LWW)
        Boolean deleted              // sync: tombstone absoluto
) {
    // ===== CONSTRUTOR COMPACTO (VALIDAÇÃO) =====
    public GestaoToners {
        if (!(inventarioId instanceof String inv) || inv.isBlank()) {
            throw new IllegalArgumentException("ID do inventário é obrigatório");
        }

        if (!(skuProduto instanceof String sku) || sku.isBlank()) {
            throw new IllegalArgumentException("SKU do produto é obrigatório");
        }
        if (sku.length() > 50) {
            throw new IllegalArgumentException("SKU deve ter no máximo 50 caracteres");
        }

        if (!(usuarioResponsavel instanceof String user) || user.isBlank()) {
            throw new IllegalArgumentException("Usuário responsável é obrigatório");
        }

        // Valores padrão
        if (dataInstalacao == null) {
            dataInstalacao = LocalDate.now();
        }

        if (percentagemRestante == null) {
            percentagemRestante = 100;
        }
        if (percentagemRestante < 0 || percentagemRestante > 100) {
            throw new IllegalArgumentException("Percentagem deve estar entre 0 e 100");
        }

        if (ciclosImpressao == null) {
            ciclosImpressao = 0;
        }
        if (ciclosImpressao < 0) {
            throw new IllegalArgumentException("Ciclos de impressão não pode ser negativo");
        }

        if (observacoes == null) observacoes = "";
        if (observacoes.length() > 65535) {
            throw new IllegalArgumentException("Observações muito longas");
        }

        // sync: registro lido sem a coluna populada = não deletado
        if (deleted == null) deleted = false;
    }

    // ===== CONSTRUTORES DE FÁBRICA (geram UUID + timestamps) =====

    public static GestaoToners novaInstalacao(
            String inventarioId,
            String skuProduto,
            String usuarioResponsavel
    ) {
        LocalDateTime agora = LocalDateTime.now();
        return new GestaoToners(
                UUID.randomUUID().toString(), inventarioId, skuProduto, LocalDate.now(),
                usuarioResponsavel, 100, 0, null, agora, agora, null, false
        );
    }

    public static GestaoToners novaInstalacaoComObservacoes(
            String inventarioId,
            String skuProduto,
            String usuarioResponsavel,
            String observacoes
    ) {
        LocalDateTime agora = LocalDateTime.now();
        return new GestaoToners(
                UUID.randomUUID().toString(), inventarioId, skuProduto, LocalDate.now(),
                usuarioResponsavel, 100, 0, observacoes, agora, agora, null, false
        );
    }

    public static GestaoToners substituicao(
            String inventarioId,
            String skuProduto,
            String usuarioResponsavel,
            String observacoes
    ) {
        String obs = observacoes != null && !observacoes.isBlank()
                ? observacoes + " (Substituição)"
                : "Substituição de toner";
        LocalDateTime agora = LocalDateTime.now();
        return new GestaoToners(
                UUID.randomUUID().toString(), inventarioId, skuProduto, LocalDate.now(),
                usuarioResponsavel, 100, 0, obs, agora, agora, null, false
        );
    }

    // ===== MÉTODOS DE NEGÓCIO =====

    public boolean isAtivo() {
        return percentagemRestante != null && percentagemRestante > 0;
    }

    public boolean isEsgotado() {
        return percentagemRestante != null && percentagemRestante == 0;
    }

    public boolean isDeletado() {
        return Boolean.TRUE.equals(deleted);
    }

    public boolean isBaixaPercentagem(int limite) {
        return percentagemRestante != null && percentagemRestante <= limite && percentagemRestante > 0;
    }

    public boolean isCheio() {
        return percentagemRestante != null && percentagemRestante == 100;
    }

    public boolean isAltoVolume(int limite) {
        return ciclosImpressao != null && ciclosImpressao >= limite;
    }

    /**
     * Retorna o status do toner baseado na percentagem restante
     */
    public String statusDescricao() {
        if (isEsgotado()) {
            return "ESGOTADO";
        }
        if (isBaixaPercentagem(20)) {
            return "CRITICO";
        }
        if (isBaixaPercentagem(50)) {
            return "ATENCAO";
        }
        return "NORMAL";
    }

    public String getStatusDescricao() {
        return statusDescricao();
    }

    public String getNivelDescricao() {
        return switch (statusDescricao()) {
            case "ESGOTADO" -> "🔴 Esgotado";
            case "CRITICO" -> "🟠 Crítico (≤20%)";
            case "ATENCAO" -> "🟡 Atenção (≤50%)";
            default -> "🟢 Normal (>50%)";
        };
    }

    // ===== MÉTODOS DE TRANSFORMAÇÃO =====
    // deviceId nunca é alterado aqui — repository preenche na escrita

    public GestaoToners comId(String novoId) {
        return new GestaoToners(
                novoId, inventarioId, skuProduto, dataInstalacao,
                usuarioResponsavel, percentagemRestante, ciclosImpressao,
                observacoes, createdAt, updatedAt, deviceId, deleted
        );
    }

    public GestaoToners comPercentagem(Integer novaPercentagem) {
        return new GestaoToners(
                id, inventarioId, skuProduto, dataInstalacao,
                usuarioResponsavel, novaPercentagem, ciclosImpressao,
                observacoes, createdAt, LocalDateTime.now(), deviceId, deleted
        );
    }

    public GestaoToners comCiclosAdicionais(Integer ciclos) {
        int novosCiclos = (this.ciclosImpressao != null ? this.ciclosImpressao : 0) + ciclos;
        return new GestaoToners(
                id, inventarioId, skuProduto, dataInstalacao,
                usuarioResponsavel, percentagemRestante, novosCiclos,
                observacoes, createdAt, LocalDateTime.now(), deviceId, deleted
        );
    }

    public GestaoToners comObservacoes(String novasObservacoes) {
        return new GestaoToners(
                id, inventarioId, skuProduto, dataInstalacao,
                usuarioResponsavel, percentagemRestante, ciclosImpressao,
                novasObservacoes, createdAt, LocalDateTime.now(), deviceId, deleted
        );
    }

    public GestaoToners comDataInstalacao(LocalDate novaData) {
        return new GestaoToners(
                id, inventarioId, skuProduto, novaData,
                usuarioResponsavel, percentagemRestante, ciclosImpressao,
                observacoes, createdAt, LocalDateTime.now(), deviceId, deleted
        );
    }

    public GestaoToners comUsuarioResponsavel(String novoUsuario) {
        return new GestaoToners(
                id, inventarioId, skuProduto, dataInstalacao,
                novoUsuario, percentagemRestante, ciclosImpressao,
                observacoes, createdAt, LocalDateTime.now(), deviceId, deleted
        );
    }

    public GestaoToners comoEsgotado() {
        return new GestaoToners(
                id, inventarioId, skuProduto, dataInstalacao,
                usuarioResponsavel, 0, ciclosImpressao,
                observacoes, createdAt, LocalDateTime.now(), deviceId, deleted
        );
    }

    /** Tombstone de sincronização: marca exclusão lógica com updatedAt novo (LWW). */
    public GestaoToners comDeletado(boolean deletado) {
        return new GestaoToners(
                id, inventarioId, skuProduto, dataInstalacao,
                usuarioResponsavel, percentagemRestante, ciclosImpressao,
                observacoes, createdAt, LocalDateTime.now(), deviceId, deletado
        );
    }

    public GestaoToners comCreatedAt(LocalDateTime data) {
        return new GestaoToners(
                id, inventarioId, skuProduto, dataInstalacao,
                usuarioResponsavel, percentagemRestante, ciclosImpressao,
                observacoes, data, updatedAt, deviceId, deleted
        );
    }

    public GestaoToners comUpdatedAt(LocalDateTime data) {
        return new GestaoToners(
                id, inventarioId, skuProduto, dataInstalacao,
                usuarioResponsavel, percentagemRestante, ciclosImpressao,
                observacoes, createdAt, data, deviceId, deleted
        );
    }

    /**
     * Retorna uma representação resumida para exibição em UI
     */
    public String getResumo() {
        return String.format("Toner %s | %d%% | %d ciclos | %s",
                skuProduto, percentagemRestante, ciclosImpressao, getStatusDescricao());
    }

    @Override
    public String toString() {
        return String.format("GestaoToners[ID=%s, Inventario=%s, SKU=%s, %d%%]",
                id, inventarioId, skuProduto, percentagemRestante);
    }
}