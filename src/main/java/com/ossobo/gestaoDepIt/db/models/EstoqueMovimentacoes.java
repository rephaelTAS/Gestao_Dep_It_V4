package com.ossobo.gestaoDepIt.db.models;

import com.ossobo.gestaoDepIt.db.enums.TipoMovimentacao;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * EstoqueMovimentacoes v3.0 - Modelo imutável com Record (Java 17+)
 *
 * Schema: estoque_movimentacoes (id TEXT PRIMARY KEY — UUID v4)
 *
 * LEDGER append-only: não edita, não deleta, sem tombstone.
 * Convergência por união de UUIDs disjuntos (conflito impossível).
 *
 * DECISÃO RATIFICADA (Letra I): SEM FOREIGN KEY — tolerância à ordem de chegada.
 */
public record EstoqueMovimentacoes(
        String id,                   // UUID v4 — gerado na fábrica
        String skuProduto,
        TipoMovimentacao tipoMovimentacao,
        Integer quantidade,
        String lote,
        LocalDate dataMovimentacao,
        LocalDate dataValidade,
        LocalDate dataFimLicenca,
        String localizacao,
        String funcionarioId,        // Referência funcional — SEM FK (Letra I)
        String motivo,
        String observacoes,
        LocalDateTime createdAt,
        String deviceId              // sync: origem do evento (rastreio)
) {
    public EstoqueMovimentacoes {
        // id nasce no GARGALO se ausente (ledger append-only; identidade de sync)
        if (id == null || id.isBlank()) {
            id = java.util.UUID.randomUUID().toString();
        }
        if (skuProduto == null || skuProduto.isBlank()) {
            throw new IllegalArgumentException("SKU do produto é obrigatório");
        }
        if (tipoMovimentacao == null) {
            throw new IllegalArgumentException("Tipo de movimentação é obrigatório");
        }
        if (quantidade == null || quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser positiva");
        }
        if (funcionarioId == null || funcionarioId.isBlank()) {
            throw new IllegalArgumentException("ID do funcionário é obrigatório");
        }

        if (dataMovimentacao == null) dataMovimentacao = LocalDate.now();
        if (lote == null) lote = "";
        if (localizacao == null) localizacao = "";
        if (motivo == null) motivo = "";
        if (observacoes == null) observacoes = "";
        if (deviceId == null) deviceId = "";
    }

    public static EstoqueMovimentacoes novaEntrada(
            String skuProduto,
            Integer quantidade,
            String lote,
            LocalDate dataValidade,
            String localizacao,
            String funcionarioId,
            String motivo,
            String observacoes
    ) {
        return new EstoqueMovimentacoes(
                UUID.randomUUID().toString(), skuProduto, TipoMovimentacao.ENTRADA, quantidade,
                lote, LocalDate.now(), dataValidade, null, localizacao,
                funcionarioId, motivo, observacoes, LocalDateTime.now(), null
        );
    }

    public static EstoqueMovimentacoes novaSaida(
            String skuProduto,
            Integer quantidade,
            String funcionarioId,
            String motivo,
            String observacoes
    ) {
        return new EstoqueMovimentacoes(
                UUID.randomUUID().toString(), skuProduto, TipoMovimentacao.SAIDA, quantidade,
                null, LocalDate.now(), null, null, null,
                funcionarioId, motivo, observacoes, LocalDateTime.now(), null
        );
    }

    public static EstoqueMovimentacoes novoAjuste(
            String skuProduto,
            Integer quantidade,
            String funcionarioId,
            String motivo,
            String observacoes
    ) {
        return new EstoqueMovimentacoes(
                UUID.randomUUID().toString(), skuProduto, TipoMovimentacao.AJUSTE, quantidade,
                null, LocalDate.now(), null, null, null,
                funcionarioId, motivo, observacoes, LocalDateTime.now(), null
        );
    }

    public static EstoqueMovimentacoes novaReserva(
            String skuProduto,
            Integer quantidade,
            String funcionarioId,
            String motivo,
            String observacoes
    ) {
        return new EstoqueMovimentacoes(
                UUID.randomUUID().toString(), skuProduto, TipoMovimentacao.RESERVA, quantidade,
                null, LocalDate.now(), null, null, null,
                funcionarioId, motivo, observacoes, LocalDateTime.now(), null
        );
    }

    public boolean isEntrada() {
        return tipoMovimentacao == TipoMovimentacao.ENTRADA;
    }

    public boolean isSaida() {
        return tipoMovimentacao == TipoMovimentacao.SAIDA;
    }

    public boolean isAjuste() {
        return tipoMovimentacao == TipoMovimentacao.AJUSTE;
    }

    public boolean isReserva() {
        return tipoMovimentacao == TipoMovimentacao.RESERVA;
    }

    public int getSinalQuantidade() {
        return switch (tipoMovimentacao) {
            case ENTRADA, AJUSTE -> quantidade != null ? quantidade : 0;
            case SAIDA, RESERVA -> quantidade != null ? -quantidade : 0;
        };
    }

    public EstoqueMovimentacoes comId(String novoId) {
        return new EstoqueMovimentacoes(
                novoId, skuProduto, tipoMovimentacao, quantidade,
                lote, dataMovimentacao, dataValidade, dataFimLicenca,
                localizacao, funcionarioId, motivo, observacoes, createdAt, deviceId
        );
    }

    @Override
    public String toString() {
        return String.format("Movimentacao[ID=%s, SKU=%s, Tipo=%s, Qtd=%d, Func=%s, Data=%s]",
                id, skuProduto, tipoMovimentacao, quantidade, funcionarioId, dataMovimentacao);
    }
}