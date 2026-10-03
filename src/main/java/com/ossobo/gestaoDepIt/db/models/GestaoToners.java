package com.ossobo.gestaoDepIt.db.models;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * GestaoToners v3.0 - Modelo imutável com Record (Java 17+)
 *
 * Schema: gestao_toners (id TEXT PRIMARY KEY — UUID v4)
 */
public record GestaoToners(
        String id,                   // UUID v4 — gerado na fábrica
        String inventarioId,         // FK → inventario_equipamentos.id (UUID)
        String skuProduto,
        LocalDate dataInstalacao,
        String usuarioResponsavel,   // FK → usuarios.id (UUID)
        Integer percentagemRestante,
        Integer ciclosImpressao,
        String observacoes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String deviceId,
        boolean deletado
) {
    public GestaoToners {
        // id nasce no GARGALO se ausente (identidade de sync)
        if (id == null || id.isBlank()) {
            id = java.util.UUID.randomUUID().toString();
        }
        if (inventarioId == null || inventarioId.isBlank()) {
            throw new IllegalArgumentException("ID do inventário é obrigatório");
        }
        if (skuProduto == null || skuProduto.isBlank()) {
            throw new IllegalArgumentException("SKU do produto é obrigatório");
        }
        if (usuarioResponsavel == null || usuarioResponsavel.isBlank()) {
            throw new IllegalArgumentException("Usuário responsável é obrigatório");
        }

        if (dataInstalacao == null) dataInstalacao = LocalDate.now();
        if (percentagemRestante == null) percentagemRestante = 100;
        if (percentagemRestante < 0 || percentagemRestante > 100) {
            throw new IllegalArgumentException("Percentagem deve estar entre 0 e 100");
        }
        if (ciclosImpressao == null) ciclosImpressao = 0;
        if (ciclosImpressao < 0) {
            throw new IllegalArgumentException("Ciclos de impressão não pode ser negativo");
        }
        if (observacoes == null) observacoes = "";
        if (deviceId == null) deviceId = "";
    }

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

    public boolean isAtivo() {
        return percentagemRestante != null && percentagemRestante > 0;
    }

    public boolean isEsgotado() {
        return percentagemRestante != null && percentagemRestante == 0;
    }

    public boolean isDeletado() {
        return deletado;
    }

    public String statusDescricao() {
        if (isEsgotado()) return "ESGOTADO";
        if (percentagemRestante != null && percentagemRestante <= 20) return "CRITICO";
        if (percentagemRestante != null && percentagemRestante <= 50) return "ATENCAO";
        return "NORMAL";
    }

    public GestaoToners comPercentagem(Integer novaPercentagem) {
        return new GestaoToners(
                id, inventarioId, skuProduto, dataInstalacao,
                usuarioResponsavel, novaPercentagem, ciclosImpressao,
                observacoes, createdAt, LocalDateTime.now(), deviceId, deletado
        );
    }

    public GestaoToners comDeletado(boolean novoDeletado) {
        return new GestaoToners(
                id, inventarioId, skuProduto, dataInstalacao,
                usuarioResponsavel, percentagemRestante, ciclosImpressao,
                observacoes, createdAt, LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
                deviceId, novoDeletado
        );
    }

    @Override
    public String toString() {
        return String.format("GestaoToners[ID=%s, Inventario=%s, SKU=%s, %d%%]",
                id, inventarioId, skuProduto, percentagemRestante);
    }
}