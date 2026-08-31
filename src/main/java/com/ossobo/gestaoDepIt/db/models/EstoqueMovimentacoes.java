package com.ossobo.gestaoDepIt.db.models;

import com.ossobo.gestaoDepIt.db.enums.TipoMovimentacao;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * EstoqueMovimentacoes - Modelo imutável com Record (Java 17+)
 * v3.0 - Degrau 1: UUID universal + deviceId (ledger append-only)
 *
 * Schema: estoque_movimentacoes (id TEXT PRIMARY KEY — UUID v4)
 *
 * Responsabilidades:
 * - Representação imutável de movimentação de estoque
 * - Validação de invariantes eternas no construtor
 * - Métodos auxiliares para tipo de movimentação
 * - Transporte de metadados de sync (deviceId — origem do evento)
 *
 * Natureza: LEDGER append-only
 * - Nunca edita, nunca deleta → sem tombstone, sem updated_at
 * - Convergência por união de UUIDs disjuntos (conflito impossível entre devices)
 *
 * Mudanças v2.0 → v3.0:
 * - id: Long → String (UUID v4, gerado pelas fábricas)
 * - +deviceId: device de origem do evento (rastreio de sync; repository preenche)
 * - SEM deleted: append-only não deleta (tombstone desnecessário)
 * - S1: validação de dataValidade no passado movida do construtor para as
 *   fábricas — dados históricos com validade expirada não podem quebrar o bootstrap
 * - toString(): %d → %s (id agora é String)
 */
public record EstoqueMovimentacoes(
        String id,                   // UUID v4 — gerado na fábrica, nunca pelo construtor
        String skuProduto,
        TipoMovimentacao tipoMovimentacao,
        Integer quantidade,
        String lote,
        LocalDate dataMovimentacao,
        LocalDate dataValidade,
        LocalDate dataFimLicenca,
        String localizacao,
        String codDepFuncionario,
        String motivo,
        String observacoes,
        LocalDateTime createdAt,
        String deviceId              // sync: origem do evento (rastreio)
) {
    // ===== CONSTRUTOR COMPACTO (VALIDAÇÃO — invariantes eternas apenas) =====
    public EstoqueMovimentacoes {
        // Validação com Pattern Matching (Java 16+)
        if (!(skuProduto instanceof String sku) || sku.isBlank()) {
            throw new IllegalArgumentException("SKU do produto é obrigatório");
        }
        if (!(tipoMovimentacao instanceof TipoMovimentacao)) {
            throw new IllegalArgumentException("Tipo de movimentação é obrigatório");
        }
        if (quantidade == null || quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser positiva");
        }
        if (!(codDepFuncionario instanceof String cod) || cod.isBlank()) {
            throw new IllegalArgumentException("Código do funcionário é obrigatório");
        }

        // Valores padrão
        if (dataMovimentacao == null) {
            dataMovimentacao = LocalDate.now();
        }
        if (lote == null) lote = "";
        if (localizacao == null) localizacao = "";
        if (motivo == null) motivo = "";
        if (observacoes == null) observacoes = "";

        // S1: validação temporal de dataValidade REMOVIDA daqui —
        // vive nas fábricas (criação). O construtor canônico aceita qualquer
        // data legítima do banco: histórico com validade expirada é dado válido.
    }

    // ===== CONSTRUTORES DE FÁBRICA (geram UUID + timestamps) =====

    /** S1: regra temporal de criação — nova movimentação não aceita validade vencida. */
    private static void validarDataValidade(LocalDate dataValidade) {
        if (dataValidade != null && dataValidade.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Data de validade não pode ser no passado");
        }
    }

    public static EstoqueMovimentacoes novaEntrada(
            String skuProduto,
            Integer quantidade,
            String lote,
            LocalDate dataValidade,
            String localizacao,
            String codDepFuncionario,
            String motivo,
            String observacoes
    ) {
        validarDataValidade(dataValidade);
        return new EstoqueMovimentacoes(
                UUID.randomUUID().toString(), skuProduto, TipoMovimentacao.ENTRADA, quantidade,
                lote, LocalDate.now(), dataValidade, null, localizacao,
                codDepFuncionario, motivo, observacoes, LocalDateTime.now(), null
        );
    }

    public static EstoqueMovimentacoes novaSaida(
            String skuProduto,
            Integer quantidade,
            String codDepFuncionario,
            String motivo,
            String observacoes
    ) {
        return new EstoqueMovimentacoes(
                UUID.randomUUID().toString(), skuProduto, TipoMovimentacao.SAIDA, quantidade,
                null, LocalDate.now(), null, null, null,
                codDepFuncionario, motivo, observacoes, LocalDateTime.now(), null
        );
    }

    public static EstoqueMovimentacoes novoAjuste(
            String skuProduto,
            Integer quantidade,
            String codDepFuncionario,
            String motivo,
            String observacoes
    ) {
        return new EstoqueMovimentacoes(
                UUID.randomUUID().toString(), skuProduto, TipoMovimentacao.AJUSTE, quantidade,
                null, LocalDate.now(), null, null, null,
                codDepFuncionario, motivo, observacoes, LocalDateTime.now(), null
        );
    }

    public static EstoqueMovimentacoes novaReserva(
            String skuProduto,
            Integer quantidade,
            String codDepFuncionario,
            String motivo,
            String observacoes
    ) {
        return new EstoqueMovimentacoes(
                UUID.randomUUID().toString(), skuProduto, TipoMovimentacao.RESERVA, quantidade,
                null, LocalDate.now(), null, null, null,
                codDepFuncionario, motivo, observacoes, LocalDateTime.now(), null
        );
    }

    // ===== MÉTODOS DE NEGÓCIO =====

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

    /**
     * Retorna o sinal da quantidade (positivo para entrada, negativo para saída)
     */
    public int getSinalQuantidade() {
        return switch (tipoMovimentacao) {
            case ENTRADA, AJUSTE -> quantidade != null ? quantidade : 0;
            case SAIDA, RESERVA -> quantidade != null ? -quantidade : 0;
        };
    }

    /**
     * Verifica se a movimentação está dentro do período
     */
    public boolean isNoPeriodo(LocalDate inicio, LocalDate fim) {
        return dataMovimentacao != null &&
                !dataMovimentacao.isBefore(inicio) &&
                !dataMovimentacao.isAfter(fim);
    }

    // ===== MÉTODOS DE TRANSFORMAÇÃO =====
    // Ledger: transformações existem para correção pontual pós-criação local,
    // antes do primeiro persist. deviceId nunca é alterado aqui.

    public EstoqueMovimentacoes comId(String novoId) {
        return new EstoqueMovimentacoes(
                novoId, skuProduto, tipoMovimentacao, quantidade,
                lote, dataMovimentacao, dataValidade, dataFimLicenca,
                localizacao, codDepFuncionario, motivo, observacoes, createdAt, deviceId
        );
    }

    public EstoqueMovimentacoes comDataMovimentacao(LocalDate novaData) {
        return new EstoqueMovimentacoes(
                id, skuProduto, tipoMovimentacao, quantidade,
                lote, novaData, dataValidade, dataFimLicenca,
                localizacao, codDepFuncionario, motivo, observacoes, createdAt, deviceId
        );
    }

    public EstoqueMovimentacoes comCreatedAt(LocalDateTime dataCriacao) {
        return new EstoqueMovimentacoes(
                id, skuProduto, tipoMovimentacao, quantidade,
                lote, dataMovimentacao, dataValidade, dataFimLicenca,
                localizacao, codDepFuncionario, motivo, observacoes, dataCriacao, deviceId
        );
    }

    @Override
    public String toString() {
        return String.format("Movimentacao[ID=%s, SKU=%s, Tipo=%s, Qtd=%d, Func=%s, Data=%s]",
                id, skuProduto, tipoMovimentacao, quantidade, codDepFuncionario, dataMovimentacao);
    }
}