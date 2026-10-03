package com.ossobo.gestaoDepIt.db.models;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * InventarioEquipamentos v3.1 - Modelo imutável com Record (Java 17+)
 *
 * Schema: inventario_equipamentos (id TEXT PRIMARY KEY — UUID v4)
 *
 * v3.1 — Alinhado ao schema REAL do MySQL remoto:
 *        - REMOVIDO campo produtoId (nunca existiu no remoto)
 *        - skuProduto referencia catalogo_produtos.sku (chave de negócio)
 *        - funcionarioId referencia funcionarios.cod_dep (chave de negócio),
 *          NÃO funcionarios.id (UUID)
 */
public record InventarioEquipamentos(
        String id,                   // UUID v4 — gerado na fábrica (identidade de sync)
        String skuProduto,           // FK → catalogo_produtos.sku (código de negócio)
        String funcionarioId,        // FK → funcionarios.cod_dep (código de negócio)
        String numSerie,
        String enderecoMac,
        LocalDate dataAquisicao,
        LocalDate dataInstalacao,
        LocalDate dataUltimaVerificacao,
        String numeroFatura,
        String localizacao,
        String departamento,
        String status,
        String condicao,
        byte[] documentoEntrega,
        byte[] documentoDevolucao,
        String observacoes,
        Boolean devolucao,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String deviceId,
        boolean deletado
) {
    public static final List<String> STATUS_VALIDOS = List.of("ATIVO", "MANUTENCAO", "BAIXADO", "RESERVA", "EM_USO");
    public static final List<String> CONDICOES_VALIDAS = List.of("OTIMO", "BOM", "REGULAR", "CRITICO");

    public InventarioEquipamentos {
        // id nasce no GARGALO se ausente (identidade de sync).
        // num_serie é a chave de negócio estável para o equipamento.
        if (id == null || id.isBlank()) {
            id = java.util.UUID.randomUUID().toString();
        }
        if (skuProduto == null || skuProduto.isBlank()) {
            throw new IllegalArgumentException("SKU do produto é obrigatório");
        }

        if (status == null || status.isBlank()) status = "ATIVO";
        if (!STATUS_VALIDOS.contains(status)) {
            throw new IllegalArgumentException("Status inválido: " + status);
        }

        if (condicao == null || condicao.isBlank()) condicao = "BOM";
        if (!CONDICOES_VALIDAS.contains(condicao)) {
            throw new IllegalArgumentException("Condição inválida: " + condicao);
        }

        if (funcionarioId == null) funcionarioId = "";
        if (departamento == null) departamento = "";
        if (observacoes == null) observacoes = "";
        if (numeroFatura == null) numeroFatura = "";
        if (documentoEntrega == null) documentoEntrega = new byte[0];
        if (documentoDevolucao == null) documentoDevolucao = new byte[0];
        if (devolucao == null) devolucao = false;
        if (deviceId == null) deviceId = "";
    }

    public static InventarioEquipamentos novo(
            String skuProduto,
            String funcionarioId,
            String numSerie,
            String enderecoMac,
            LocalDate dataAquisicao,
            String localizacao
    ) {
        if (funcionarioId == null || funcionarioId.isBlank()) {
            throw new IllegalArgumentException("ID do funcionário é obrigatório");
        }
        if (numSerie == null || numSerie.isBlank()) {
            throw new IllegalArgumentException("Número de série é obrigatório");
        }

        if (dataAquisicao == null) {
            throw new IllegalArgumentException("Data de aquisição é obrigatória");
        }
        if (localizacao == null || localizacao.isBlank()) {
            throw new IllegalArgumentException("Localização é obrigatória");
        }

        LocalDateTime agora = LocalDateTime.now();
        return new InventarioEquipamentos(
                UUID.randomUUID().toString(), skuProduto, funcionarioId, numSerie, enderecoMac,
                dataAquisicao, null, null, "", localizacao, "",
                "ATIVO", "BOM", new byte[0], new byte[0], "", false,
                agora, agora, null, false
        );
    }

    public boolean isAtivo() {
        return "ATIVO".equals(status);
    }

    public boolean isDeletado() {
        return deletado;
    }

    public String getStatusDescricao() {
        return switch (status) {
            case "ATIVO" -> "Ativo";
            case "MANUTENCAO" -> "Em Manutenção";
            case "BAIXADO" -> "Baixado";
            case "RESERVA" -> "Reservado";
            case "EM_USO" -> "Em Uso";
            default -> status;
        };
    }

    public InventarioEquipamentos comStatus(String novoStatus) {
        if (!STATUS_VALIDOS.contains(novoStatus)) {
            throw new IllegalArgumentException("Status inválido: " + novoStatus);
        }
        return new InventarioEquipamentos(
                id, skuProduto, funcionarioId, numSerie, enderecoMac,
                dataAquisicao, dataInstalacao, dataUltimaVerificacao,
                numeroFatura, localizacao, departamento, novoStatus, condicao,
                documentoEntrega, documentoDevolucao, observacoes, devolucao,
                createdAt, LocalDateTime.now(), deviceId, deletado
        );
    }

    public InventarioEquipamentos comDeletado(boolean novoDeletado) {
        return new InventarioEquipamentos(
                id, skuProduto, funcionarioId, numSerie, enderecoMac,
                dataAquisicao, dataInstalacao, dataUltimaVerificacao,
                numeroFatura, localizacao, departamento, status, condicao,
                documentoEntrega, documentoDevolucao, observacoes, devolucao,
                createdAt, LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
                deviceId, novoDeletado
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InventarioEquipamentos that)) return false;
        return deletado == that.deletado &&
                Objects.equals(id, that.id) &&
                Objects.equals(skuProduto, that.skuProduto) &&
                Objects.equals(funcionarioId, that.funcionarioId) &&
                Objects.equals(numSerie, that.numSerie) &&
                Objects.equals(enderecoMac, that.enderecoMac) &&
                Objects.equals(dataAquisicao, that.dataAquisicao) &&
                Objects.equals(dataInstalacao, that.dataInstalacao) &&
                Objects.equals(dataUltimaVerificacao, that.dataUltimaVerificacao) &&
                Objects.equals(numeroFatura, that.numeroFatura) &&
                Objects.equals(localizacao, that.localizacao) &&
                Objects.equals(departamento, that.departamento) &&
                Objects.equals(status, that.status) &&
                Objects.equals(condicao, that.condicao) &&
                Arrays.equals(documentoEntrega, that.documentoEntrega) &&
                Arrays.equals(documentoDevolucao, that.documentoDevolucao) &&
                Objects.equals(observacoes, that.observacoes) &&
                Objects.equals(devolucao, that.devolucao) &&
                Objects.equals(createdAt, that.createdAt) &&
                Objects.equals(updatedAt, that.updatedAt) &&
                Objects.equals(deviceId, that.deviceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, skuProduto, funcionarioId, numSerie,
                enderecoMac, dataAquisicao, dataInstalacao, dataUltimaVerificacao,
                numeroFatura, localizacao, departamento, status, condicao,
                Arrays.hashCode(documentoEntrega), Arrays.hashCode(documentoDevolucao),
                observacoes, devolucao, createdAt, updatedAt, deviceId, deletado);
    }

    @Override
    public String toString() {
        return String.format("InventarioEquipamentos[ID=%s, Série=%s, MAC=%s, Status=%s]",
                id, numSerie, enderecoMac, status);
    }

    public InventarioEquipamentos comFuncionario(String novoFuncionarioId) {
        return new InventarioEquipamentos(
                id, skuProduto, novoFuncionarioId, numSerie, enderecoMac,
                dataAquisicao, dataInstalacao, dataUltimaVerificacao,
                numeroFatura, localizacao, departamento, status, condicao,
                documentoEntrega, documentoDevolucao, observacoes, devolucao,
                createdAt, java.time.LocalDateTime.now(), deviceId, deletado
        );
    }
}