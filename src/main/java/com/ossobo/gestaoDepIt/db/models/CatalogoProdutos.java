package com.ossobo.gestaoDepIt.db.models;

import com.ossobo.gestaoDepIt.db.enums.TipoProduto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;

/**
 * CatalogoProdutos - Modelo imutável com Record (Java 17+)
 * v2.1 - Adicionados campos: iva, precoTotal, fornecedor, numeroFatura, faturaCompra
 *
 * Schema: catalogo_produtos
 *
 * Responsabilidades:
 * - Representação imutável do produto
 * - Validação de negócio no construtor
 * - Métodos auxiliares para estado do produto
 */
public record CatalogoProdutos(
        String sku,                    // PRIMARY KEY
        TipoProduto tipoProduto,       // ENUM
        String categoria,
        String marca,
        String modelo,
        String cor,
        String descricao,
        String caracteristicasTecnicas,
        BigDecimal precoUnitario,
        Integer totalRecebido,
        BigDecimal iva,                // ✅ NOVO
        BigDecimal precoTotal,         // ✅ NOVO
        Boolean ativo,
        String fornecedor,             // ✅ NOVO
        String numeroFatura,           // ✅ NOVO
        byte[] faturaCompra,           // ✅ NOVO (BLOB)
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    // ===== CONSTRUTOR COMPACTO (VALIDAÇÃO) =====
    public CatalogoProdutos {
        // Validação com Pattern Matching (Java 16+)
        if (!(sku instanceof String s) || s.isBlank()) {
            throw new IllegalArgumentException("SKU é obrigatório");
        }
        if (!(tipoProduto instanceof TipoProduto)) {
            throw new IllegalArgumentException("Tipo de produto é obrigatório");
        }
        if (!(categoria instanceof String cat) || cat.isBlank()) {
            throw new IllegalArgumentException("Categoria é obrigatória");
        }
        if (!(marca instanceof String m) || m.isBlank()) {
            throw new IllegalArgumentException("Marca é obrigatória");
        }
        if (!(modelo instanceof String mod) || mod.isBlank()) {
            throw new IllegalArgumentException("Modelo é obrigatório");
        }

        // Valores padrão para campos opcionais
        if (cor == null) cor = "";
        if (descricao == null) descricao = "";
        if (caracteristicasTecnicas == null) caracteristicasTecnicas = "";
        if (precoUnitario == null) precoUnitario = BigDecimal.ZERO;
        if (iva == null) iva = BigDecimal.ZERO;
        if (precoTotal == null) precoTotal = BigDecimal.ZERO;
        if (totalRecebido == null) totalRecebido = 0;
        if (ativo == null) ativo = true;
        if (fornecedor == null) fornecedor = "";
        if (numeroFatura == null) numeroFatura = "";
        if (faturaCompra == null) faturaCompra = new byte[0];
    }

    // ===== CONSTRUTOR DE FÁBRICA =====
    public static CatalogoProdutos novo(
            String sku,
            TipoProduto tipoProduto,
            String categoria,
            String marca,
            String modelo,
            String cor,
            String descricao,
            String caracteristicasTecnicas,
            BigDecimal precoUnitario,
            Integer totalRecebido
    ) {
        return new CatalogoProdutos(
                sku, tipoProduto, categoria, marca, modelo,
                cor, descricao, caracteristicasTecnicas,
                precoUnitario, totalRecebido, BigDecimal.ZERO, BigDecimal.ZERO,
                true, "", "", new byte[0], null, null
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
            BigDecimal precoUnitario,
            BigDecimal iva,
            Integer totalRecebido,
            String fornecedor,
            String numeroFatura,
            byte[] faturaCompra
    ) {
        BigDecimal precoTotal = precoUnitario != null && iva != null
                ? precoUnitario.add(precoUnitario.multiply(iva).divide(BigDecimal.valueOf(100)))
                : BigDecimal.ZERO;

        return new CatalogoProdutos(
                sku, tipoProduto, categoria, marca, modelo,
                cor, descricao, caracteristicasTecnicas,
                precoUnitario, totalRecebido, iva, precoTotal,
                true, fornecedor, numeroFatura, faturaCompra,
                null, null
        );
    }

    // ===== MÉTODOS DE NEGÓCIO =====

    /**
     * Nome completo do produto (Marca + Modelo)
     */
    public String getNomeCompleto() {
        return String.format("%s %s", marca, modelo);
    }

    /**
     * Verifica se está disponível para venda
     */
    public boolean isDisponivel() {
        return Boolean.TRUE.equals(ativo) &&
                totalRecebido != null &&
                totalRecebido > 0;
    }

    /**
     * Valor total em estoque (preço × quantidade)
     */
    public BigDecimal getValorTotalEstoque() {
        if (precoUnitario == null || totalRecebido == null || totalRecebido <= 0) {
            return BigDecimal.ZERO;
        }
        return precoUnitario.multiply(BigDecimal.valueOf(totalRecebido));
    }

    /**
     * Calcula o preço total com base no preço unitário e IVA
     */
    public BigDecimal calcularPrecoTotal() {
        if (precoUnitario == null || iva == null) {
            return BigDecimal.ZERO;
        }
        return precoUnitario.add(precoUnitario.multiply(iva).divide(BigDecimal.valueOf(100)));
    }

    /**
     * Verifica se o estoque está baixo (< 5 unidades)
     */
    public boolean isEstoqueBaixo() {
        return totalRecebido != null && totalRecebido > 0 && totalRecebido <= 5;
    }

    /**
     * Verifica se o produto está ativo e com estoque
     */
    public boolean isAtivoComEstoque() {
        return Boolean.TRUE.equals(ativo) && totalRecebido != null && totalRecebido > 0;
    }

    /**
     * Verifica se tem fatura anexada
     */
    public boolean temFatura() {
        return faturaCompra != null && faturaCompra.length > 0;
    }

    /**
     * Verifica se tem fornecedor cadastrado
     */
    public boolean temFornecedor() {
        return fornecedor != null && !fornecedor.isBlank();
    }

    // ===== MÉTODOS DE TRANSFORMAÇÃO =====

    public CatalogoProdutos comStatus(boolean novoStatus) {
        return new CatalogoProdutos(
                sku, tipoProduto, categoria, marca, modelo,
                cor, descricao, caracteristicasTecnicas,
                precoUnitario, totalRecebido, iva, precoTotal,
                novoStatus, fornecedor, numeroFatura, faturaCompra,
                createdAt, updatedAt
        );
    }

    public CatalogoProdutos comEstoque(Integer novoEstoque) {
        return new CatalogoProdutos(
                sku, tipoProduto, categoria, marca, modelo,
                cor, descricao, caracteristicasTecnicas,
                precoUnitario, novoEstoque, iva, precoTotal,
                ativo, fornecedor, numeroFatura, faturaCompra,
                createdAt, updatedAt
        );
    }

    public CatalogoProdutos comPreco(BigDecimal novoPreco) {
        BigDecimal novoTotal = novoPreco != null && iva != null
                ? novoPreco.add(novoPreco.multiply(iva).divide(BigDecimal.valueOf(100)))
                : BigDecimal.ZERO;

        return new CatalogoProdutos(
                sku, tipoProduto, categoria, marca, modelo,
                cor, descricao, caracteristicasTecnicas,
                novoPreco, totalRecebido, iva, novoTotal,
                ativo, fornecedor, numeroFatura, faturaCompra,
                createdAt, updatedAt
        );
    }

    public CatalogoProdutos comIva(BigDecimal novoIva) {
        BigDecimal novoTotal = precoUnitario != null && novoIva != null
                ? precoUnitario.add(precoUnitario.multiply(novoIva).divide(BigDecimal.valueOf(100)))
                : BigDecimal.ZERO;

        return new CatalogoProdutos(
                sku, tipoProduto, categoria, marca, modelo,
                cor, descricao, caracteristicasTecnicas,
                precoUnitario, totalRecebido, novoIva, novoTotal,
                ativo, fornecedor, numeroFatura, faturaCompra,
                createdAt, updatedAt
        );
    }

    public CatalogoProdutos comPrecos(BigDecimal preco, BigDecimal iva) {
        BigDecimal total = preco != null && iva != null
                ? preco.add(preco.multiply(iva).divide(BigDecimal.valueOf(100)))
                : BigDecimal.ZERO;

        return new CatalogoProdutos(
                sku, tipoProduto, categoria, marca, modelo,
                cor, descricao, caracteristicasTecnicas,
                preco, totalRecebido, iva, total,
                ativo, fornecedor, numeroFatura, faturaCompra,
                createdAt, updatedAt
        );
    }

    public CatalogoProdutos comFornecedor(String novoFornecedor) {
        return new CatalogoProdutos(
                sku, tipoProduto, categoria, marca, modelo,
                cor, descricao, caracteristicasTecnicas,
                precoUnitario, totalRecebido, iva, precoTotal,
                ativo, novoFornecedor, numeroFatura, faturaCompra,
                createdAt, updatedAt
        );
    }

    public CatalogoProdutos comNumeroFatura(String novoNumero) {
        return new CatalogoProdutos(
                sku, tipoProduto, categoria, marca, modelo,
                cor, descricao, caracteristicasTecnicas,
                precoUnitario, totalRecebido, iva, precoTotal,
                ativo, fornecedor, novoNumero, faturaCompra,
                createdAt, updatedAt
        );
    }

    public CatalogoProdutos comFaturaCompra(byte[] novaFatura) {
        return new CatalogoProdutos(
                sku, tipoProduto, categoria, marca, modelo,
                cor, descricao, caracteristicasTecnicas,
                precoUnitario, totalRecebido, iva, precoTotal,
                ativo, fornecedor, numeroFatura, novaFatura,
                createdAt, updatedAt
        );
    }

    public CatalogoProdutos comCreatedAt(LocalDateTime data) {
        return new CatalogoProdutos(
                sku, tipoProduto, categoria, marca, modelo,
                cor, descricao, caracteristicasTecnicas,
                precoUnitario, totalRecebido, iva, precoTotal,
                ativo, fornecedor, numeroFatura, faturaCompra,
                data, updatedAt
        );
    }

    public CatalogoProdutos comUpdatedAt(LocalDateTime data) {
        return new CatalogoProdutos(
                sku, tipoProduto, categoria, marca, modelo,
                cor, descricao, caracteristicasTecnicas,
                precoUnitario, totalRecebido, iva, precoTotal,
                ativo, fornecedor, numeroFatura, faturaCompra,
                createdAt, data
        );
    }

    // ===== EQUALS/HASHCODE =====

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CatalogoProdutos that)) return false;
        return Objects.equals(sku, that.sku) &&
                Objects.equals(tipoProduto, that.tipoProduto) &&
                Objects.equals(categoria, that.categoria) &&
                Objects.equals(marca, that.marca) &&
                Objects.equals(modelo, that.modelo) &&
                Objects.equals(cor, that.cor) &&
                Objects.equals(descricao, that.descricao) &&
                Objects.equals(caracteristicasTecnicas, that.caracteristicasTecnicas) &&
                Objects.equals(precoUnitario, that.precoUnitario) &&
                Objects.equals(totalRecebido, that.totalRecebido) &&
                Objects.equals(iva, that.iva) &&
                Objects.equals(precoTotal, that.precoTotal) &&
                Objects.equals(ativo, that.ativo) &&
                Objects.equals(fornecedor, that.fornecedor) &&
                Objects.equals(numeroFatura, that.numeroFatura) &&
                Arrays.equals(faturaCompra, that.faturaCompra);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sku, tipoProduto, categoria, marca, modelo,
                cor, descricao, caracteristicasTecnicas, precoUnitario,
                totalRecebido, iva, precoTotal, ativo, fornecedor,
                numeroFatura, Arrays.hashCode(faturaCompra));
    }

    // ===== MÉTODO TO_STRING =====

    @Override
    public String toString() {
        return String.format("Produto[SKU=%s, %s %s, Estoque=%d, Preço=%s, Ativo=%s]",
                sku, marca, modelo,
                totalRecebido != null ? totalRecebido : 0,
                precoUnitario != null ? precoUnitario : BigDecimal.ZERO,
                ativo ? "Sim" : "Não");
    }
}