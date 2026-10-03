package com.ossobo.gestaoDepIt.db.models;

import com.ossobo.gestaoDepIt.db.enums.TipoProduto;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Objects;

/**
 * CatalogoProdutos v3.0 - Modelo imutável com Record (Java 17+)
 *
 * Schema: catalogo_produtos (id TEXT PRIMARY KEY — UUID v4)
 *
 * DECISÕES RATIFICADAS:
 * - id: String (UUID) — identidade de sync (nasce no gargalo)
 * - sku: String (UNIQUE) — código de negócio (corrigível)
 * - precoUnitarioCentavos: Integer — 5990 = 59,90
 * - ivaBasisPoints: Integer — 2300 = 23,00%
 * - precoTotalCentavos: Integer — DERIVADO no construtor (fonte única)
 * - deviceId: String — origem da última mutação (LWW)
 * - deletado: boolean — tombstone absoluto
 */
public record CatalogoProdutos(
        // ---- Identidade ----
        String id,                      // UUID — nasce no GARGALO do insert se ausente
        String sku,                     // código de negócio — UNIQUE, corrigível

        // ---- Negócio ----
        TipoProduto tipoProduto,
        String categoria,
        String marca,
        String modelo,
        String cor,
        String descricao,
        String caracteristicasTecnicas,
        Integer totalRecebido,
        Integer precoUnitarioCentavos,  // 5990 = 59,90
        Integer ivaBasisPoints,         // 2300 = 23,00%
        Integer precoTotalCentavos,     // DERIVADO — recalcula no construtor
        Boolean ativo,
        String fornecedor,
        String numeroFatura,
        byte[] faturaCompra,

        // ---- Auditoria ----
        LocalDateTime createdAt,
        LocalDateTime updatedAt,

        // ---- Sync ----
        String deviceId,                // null na fábrica → carimbado no gargalo
        boolean deletado
) {

    // ============================================================
    // CONSTANTES
    // ============================================================

    private static final int MAX_IMAGE_SIZE = 2 * 1024 * 1024; // 2MB

    // ============================================================
    // CÁLCULO DETERMINÍSTICO (HALF_UP em inteiros)
    // ============================================================

    /**
     * Preço total em centavos — aritmética INTEIRA, HALF_UP.
     * Mesma entrada → MESMO centavo em qualquer máquina.
     */
    public static int calcularPrecoTotalCentavos(int precoUnitarioCentavos, int ivaBasisPoints) {
        long produto = (long) precoUnitarioCentavos * (10_000L + ivaBasisPoints);
        return (int) ((produto + 5_000L) / 10_000L);
    }

    // ============================================================
    // CONSTRUTOR COMPACTO
    // ============================================================

    public CatalogoProdutos {
        // Validação com null-check direto

        if (id == null || id.isBlank()) {
            id = java.util.UUID.randomUUID().toString();
        }
        if (sku == null || sku.isBlank()) {
            throw new IllegalArgumentException("SKU é obrigatório");
        }
        if (tipoProduto == null) {
            throw new IllegalArgumentException("Tipo de produto é obrigatório");
        }
        if (categoria == null || categoria.isBlank()) {
            throw new IllegalArgumentException("Categoria é obrigatória");
        }
        if (marca == null || marca.isBlank()) {
            throw new IllegalArgumentException("Marca é obrigatória");
        }
        if (modelo == null || modelo.isBlank()) {
            throw new IllegalArgumentException("Modelo é obrigatório");
        }

        // Normalização
        if (cor == null) cor = "";
        if (descricao == null) descricao = "";
        if (caracteristicasTecnicas == null) caracteristicasTecnicas = "";
        if (precoUnitarioCentavos == null || precoUnitarioCentavos < 0) precoUnitarioCentavos = 0;
        if (ivaBasisPoints == null || ivaBasisPoints < 0) ivaBasisPoints = 0;
        if (totalRecebido == null) totalRecebido = 0;
        if (ativo == null) ativo = true;
        if (fornecedor == null) fornecedor = "";
        if (numeroFatura == null) numeroFatura = "";
        if (faturaCompra == null) faturaCompra = new byte[0];
        if (deviceId == null) deviceId = "";

        // FONTE ÚNICA do total: recalculado AQUI
        precoTotalCentavos = calcularPrecoTotalCentavos(precoUnitarioCentavos, ivaBasisPoints);
    }

    // ============================================================
    // FÁBRICAS
    // ============================================================

    public static CatalogoProdutos novo(
            String sku,
            TipoProduto tipoProduto,
            String categoria,
            String marca,
            String modelo
    ) {
        return new CatalogoProdutos(
                null, sku, tipoProduto, categoria, marca, modelo,
                null, null, null, null, null, null, null,
                true, null, null, null,
                null, null, null, false
        );
    }

    public static CatalogoProdutos novoCompleto(

            String sku,
            TipoProduto tipoProduto,
            String categoria,
            String marca,
            String modelo,
            String cor,
            String descricao,
            String caracteristicasTecnicas,
            Integer precoUnitarioCentavos,
            Integer ivaBasisPoints,
            Integer totalRecebido,
            String fornecedor,
            String numeroFatura,
            byte[] faturaCompra
    ) {
        if (faturaCompra != null && faturaCompra.length > MAX_IMAGE_SIZE) {
            throw new IllegalArgumentException("Fatura muito grande. Tamanho máximo: 2MB");
        }

        return new CatalogoProdutos(
                null, sku, tipoProduto, categoria, marca, modelo,
                cor, descricao, caracteristicasTecnicas, totalRecebido,
                precoUnitarioCentavos, ivaBasisPoints, null,
                true, fornecedor, numeroFatura, faturaCompra,
                null, null, null, false
        );
    }

    // ============================================================
    // MÉTODOS DE NEGÓCIO
    // ============================================================

    public String getPrecoUnitarioFormatado() {
        return formatarCentavos(precoUnitarioCentavos);
    }

    public String getPrecoTotalFormatado() {
        return formatarCentavos(precoTotalCentavos);
    }

    public String getIvaFormatado() {
        return ivaBasisPoints != null
                ? String.format("%.2f%%", ivaBasisPoints / 100.0)
                : "0,00%";
    }

    public String getNomeCompleto() {
        return String.format("%s %s", marca, modelo);
    }

    public boolean isDisponivel() {
        return Boolean.TRUE.equals(ativo) && totalRecebido != null && totalRecebido > 0;
    }

    public boolean isEstoqueBaixo() {
        return totalRecebido != null && totalRecebido > 0 && totalRecebido <= 5;
    }

    public boolean temFatura() {
        return faturaCompra != null && faturaCompra.length > 0;
    }

    public boolean isDeletado() {
        return deletado;
    }

    // ============================================================
    // FORMATADOR DE CENTAVOS (fonte única)
    // ============================================================

    public static String formatarCentavos(Integer centavos) {
        if (centavos == null) return "0,00";
        int abs = Math.abs(centavos);
        int reais = abs / 100;
        int centavosRestantes = abs % 100;
        return String.format("%s%d,%02d",
                centavos < 0 ? "-" : "",
                reais,
                centavosRestantes);
    }

    // ============================================================
    // MÉTODOS DE TRANSFORMAÇÃO
    // ============================================================

    public CatalogoProdutos comId(String novoId) {
        return new CatalogoProdutos(
                novoId, sku, tipoProduto, categoria, marca, modelo,
                cor, descricao, caracteristicasTecnicas, totalRecebido,
                precoUnitarioCentavos, ivaBasisPoints, precoTotalCentavos,
                ativo, fornecedor, numeroFatura, faturaCompra,
                createdAt, updatedAt, deviceId, deletado
        );
    }

    public CatalogoProdutos comSku(String novoSku) {
        return new CatalogoProdutos(
                id, novoSku, tipoProduto, categoria, marca, modelo,
                cor, descricao, caracteristicasTecnicas, totalRecebido,
                precoUnitarioCentavos, ivaBasisPoints, precoTotalCentavos,
                ativo, fornecedor, numeroFatura, faturaCompra,
                createdAt, updatedAt, deviceId, deletado
        );
    }

    public CatalogoProdutos comStatus(boolean novoStatus) {
        return new CatalogoProdutos(
                id, sku, tipoProduto, categoria, marca, modelo,
                cor, descricao, caracteristicasTecnicas, totalRecebido,
                precoUnitarioCentavos, ivaBasisPoints, precoTotalCentavos,
                novoStatus, fornecedor, numeroFatura, faturaCompra,
                createdAt, updatedAt, deviceId, deletado
        );
    }

    public CatalogoProdutos comEstoque(Integer novoEstoque) {
        return new CatalogoProdutos(
                id, sku, tipoProduto, categoria, marca, modelo,
                cor, descricao, caracteristicasTecnicas, novoEstoque,
                precoUnitarioCentavos, ivaBasisPoints, precoTotalCentavos,
                ativo, fornecedor, numeroFatura, faturaCompra,
                createdAt, updatedAt, deviceId, deletado
        );
    }

    public CatalogoProdutos comPreco(Integer novoPrecoCentavos) {
        return new CatalogoProdutos(
                id, sku, tipoProduto, categoria, marca, modelo,
                cor, descricao, caracteristicasTecnicas, totalRecebido,
                novoPrecoCentavos, ivaBasisPoints, null,
                ativo, fornecedor, numeroFatura, faturaCompra,
                createdAt, updatedAt, deviceId, deletado
        );
    }

    public CatalogoProdutos comIva(Integer novoIvaBp) {
        return new CatalogoProdutos(
                id, sku, tipoProduto, categoria, marca, modelo,
                cor, descricao, caracteristicasTecnicas, totalRecebido,
                precoUnitarioCentavos, novoIvaBp, null,
                ativo, fornecedor, numeroFatura, faturaCompra,
                createdAt, updatedAt, deviceId, deletado
        );
    }

    public CatalogoProdutos comPrecos(Integer precoCentavos, Integer ivaBp) {
        return new CatalogoProdutos(
                id, sku, tipoProduto, categoria, marca, modelo,
                cor, descricao, caracteristicasTecnicas, totalRecebido,
                precoCentavos, ivaBp, null,
                ativo, fornecedor, numeroFatura, faturaCompra,
                createdAt, updatedAt, deviceId, deletado
        );
    }

    public CatalogoProdutos comDeletado(boolean novoDeletado) {
        return new CatalogoProdutos(
                id, sku, tipoProduto, categoria, marca, modelo,
                cor, descricao, caracteristicasTecnicas, totalRecebido,
                precoUnitarioCentavos, ivaBasisPoints, precoTotalCentavos,
                ativo, fornecedor, numeroFatura, faturaCompra,
                createdAt, LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
                deviceId, novoDeletado
        );
    }

    // ============================================================
    // EQUALS/HASHCODE MANUAIS (byte[])
    // ============================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CatalogoProdutos that)) return false;
        return deletado == that.deletado &&
                Objects.equals(id, that.id) &&
                Objects.equals(sku, that.sku) &&
                tipoProduto == that.tipoProduto &&
                Objects.equals(categoria, that.categoria) &&
                Objects.equals(marca, that.marca) &&
                Objects.equals(modelo, that.modelo) &&
                Objects.equals(cor, that.cor) &&
                Objects.equals(descricao, that.descricao) &&
                Objects.equals(caracteristicasTecnicas, that.caracteristicasTecnicas) &&
                Objects.equals(totalRecebido, that.totalRecebido) &&
                Objects.equals(precoUnitarioCentavos, that.precoUnitarioCentavos) &&
                Objects.equals(ivaBasisPoints, that.ivaBasisPoints) &&
                Objects.equals(precoTotalCentavos, that.precoTotalCentavos) &&
                Objects.equals(ativo, that.ativo) &&
                Objects.equals(fornecedor, that.fornecedor) &&
                Objects.equals(numeroFatura, that.numeroFatura) &&
                Arrays.equals(faturaCompra, that.faturaCompra) &&
                Objects.equals(createdAt, that.createdAt) &&
                Objects.equals(updatedAt, that.updatedAt) &&
                Objects.equals(deviceId, that.deviceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, sku, tipoProduto, categoria, marca, modelo,
                cor, descricao, caracteristicasTecnicas, totalRecebido,
                precoUnitarioCentavos, ivaBasisPoints, precoTotalCentavos,
                ativo, fornecedor, numeroFatura, Arrays.hashCode(faturaCompra),
                createdAt, updatedAt, deviceId, deletado);
    }

    @Override
    public String toString() {
        return String.format("CatalogoProdutos[SKU=%s, %s %s, Estoque=%d, Preço=%s, Ativo=%s]",
                sku, marca, modelo,
                totalRecebido != null ? totalRecebido : 0,
                formatarCentavos(precoUnitarioCentavos),
                Boolean.TRUE.equals(ativo) ? "Sim" : "Não");
    }
}