package com.ossobo.gestaoDepIt.db.models;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * InventarioEquipamentos - Modelo imutável com Record (Java 17+)
 * v3.0 - Degrau 1: UUID universal + colunas de sincronização + alinhamento ao schema MySQL real
 *
 * Schema: inventario_equipamentos (id TEXT PRIMARY KEY — UUID v4)
 *
 * Responsabilidades:
 * - Representação imutável de equipamento em inventário
 * - Validação de invariantes no construtor (apenas o que o banco impõe como NOT NULL)
 * - Métodos auxiliares para status e condição
 * - Transporte de metadados de sync (LWW + tombstone)
 *
 * Mudanças v2.1 → v3.0:
 * - id: Long → String (UUID v4, gerado pelas fábricas)
 * - +deviceId: device da última mutação (desempate LWW; repository preenche na escrita)
 * - +deleted: tombstone absoluto de sincronização
 * - S8/S9/S10: construtor alinhado ao MySQL real (fonte da verdade) — campos
 *   NULLABLE no banco (data_aquisicao, funcionario_id, num_serie, endereco_mac,
 *   localizacao) deixam de ser exigidos aqui; as exigências de CRIAÇÃO vivem
 *   nas fábricas. Dados legados incompletos não quebram o bootstrap.
 * - S1b: validações de data futura removidas do construtor (relógios dessincronizados
 *   entre devices podem criar datas "futuras" legítimas) — fábricas mantêm a regra
 * - getNomeCompleto(): sobrevive a numSerie null (possível agora na leitura)
 * - toString(): %d → %s (id agora é String)
 */
public record InventarioEquipamentos(
        String id,                  // UUID v4 — gerado na fábrica, nunca pelo construtor
        String skuProduto,          // FK → catalogo_produtos.sku (NOT NULL no banco)
        String funcionarioId,       // FK → funcionarios.cod_dep (NULLABLE no banco)
        String numSerie,
        String enderecoMac,
        LocalDate dataAquisicao,
        LocalDate dataInstalacao,
        LocalDate dataUltimaVerificacao,
        String numeroFatura,
        String localizacao,
        String departamento,
        String status,              // ATIVO, MANUTENCAO, BAIXADO, RESERVA, EM_USO
        String condicao,            // OTIMO, BOM, REGULAR, CRITICO
        byte[] documentoEntrega,    // BLOB
        byte[] documentoDevolucao,  // BLOB
        String observacoes,
        Boolean devolucao,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String deviceId,            // sync: origem da última mutação (desempate LWW)
        Boolean deleted             // sync: tombstone absoluto
) {
    // ===== CONSTANTES =====
    public static final List<String> STATUS_VALIDOS = List.of("ATIVO", "MANUTENCAO", "BAIXADO", "RESERVA", "EM_USO");
    public static final List<String> CONDICOES_VALIDAS = List.of("OTIMO", "BOM", "REGULAR", "CRITICO");

    // ===== CONSTRUTOR DE COMPATIBILIDADE (ponte v2.1 → v3.0) =====

    /**
     * Ponte de compatibilidade: mantém chamadores legados (repository v1) compilando.
     * Delega ao canônico com deviceId=null e deleted=false.
     * Código novo deve usar o construtor canônico (20 argumentos).
     */
    public InventarioEquipamentos(
            String id, String skuProduto, String funcionarioId, String numSerie, String enderecoMac,
            LocalDate dataAquisicao, LocalDate dataInstalacao, LocalDate dataUltimaVerificacao,
            String numeroFatura, String localizacao, String departamento, String status, String condicao,
            byte[] documentoEntrega, byte[] documentoDevolucao, String observacoes, Boolean devolucao,
            LocalDateTime createdAt, LocalDateTime updatedAt
    ) {
        this(id, skuProduto, funcionarioId, numSerie, enderecoMac,
                dataAquisicao, dataInstalacao, dataUltimaVerificacao,
                numeroFatura, localizacao, departamento, status, condicao,
                documentoEntrega, documentoDevolucao, observacoes, devolucao,
                createdAt, updatedAt, null, false);
    }

    // ===== CONSTRUTOR COMPACTO (VALIDAÇÃO — apenas invariantes do banco) =====
    public InventarioEquipamentos {
        // NOT NULL no MySQL real — invariantes legítimas do construtor:
        if (!(skuProduto instanceof String sku) || sku.isBlank()) {
            throw new IllegalArgumentException("SKU do produto é obrigatório");
        }
        if (sku.length() > 50) {
            throw new IllegalArgumentException("SKU deve ter no máximo 50 caracteres");
        }

        if (status == null || status.isBlank()) {
            status = "ATIVO";
        }
        if (!STATUS_VALIDOS.contains(status)) {
            throw new IllegalArgumentException("Status inválido: " + status);
        }

        if (condicao == null || condicao.isBlank()) {
            condicao = "BOM";
        }
        if (!CONDICOES_VALIDAS.contains(condicao)) {
            throw new IllegalArgumentException("Condição inválida: " + condicao);
        }

        // Campos NULLABLE no MySQL real (S8/S9/S10) — tolerantes aqui.
        // Exigências de negócio vivem nas fábricas (criação).
        if (departamento == null) departamento = "";

        if (observacoes == null) observacoes = "";
        if (observacoes.length() > 65535) {
            throw new IllegalArgumentException("Observações muito longas");
        }

        if (numeroFatura == null) numeroFatura = "";
        if (documentoEntrega == null) documentoEntrega = new byte[0];
        if (documentoDevolucao == null) documentoDevolucao = new byte[0];
        if (devolucao == null) devolucao = false;

        // S1b: validações de data futura REMOVIDAS daqui — relógios de devices
        // dessincronizados podem gerar datas "futuras" legítimas; rejeitá-las
        // na leitura quebraria o bootstrap. A regra de criação vive nas fábricas.

        // sync: registro lido sem a coluna populada = não deletado
        if (deleted == null) deleted = false;
    }

    // ===== CONSTRUTORES DE FÁBRICA (geram UUID + timestamps + regras de criação) =====

    /** Regra de criação S1b: nenhum campo temporal de novo equipamento aponta para o futuro. */
    private static void validarDatasDeCriacao(LocalDate dataAquisicao, LocalDate dataInstalacao, LocalDate dataVerificacao) {
        if (dataAquisicao != null && dataAquisicao.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Data de aquisição não pode ser futura");
        }
        if (dataInstalacao != null && dataInstalacao.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Data de instalação não pode ser futura");
        }
        if (dataVerificacao != null && dataVerificacao.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Data da última verificação não pode ser futura");
        }
    }

    public static InventarioEquipamentos novo(
            String skuProduto,
            String funcionarioId,
            String numSerie,
            String enderecoMac,
            LocalDate dataAquisicao,
            String localizacao
    ) {
        // Regras de criação (eram do construtor v2.1): equipamento novo nasce completo
        if (funcionarioId == null || funcionarioId.isBlank()) {
            throw new IllegalArgumentException("ID do funcionário é obrigatório");
        }
        if (numSerie == null || numSerie.isBlank()) {
            throw new IllegalArgumentException("Número de série é obrigatório");
        }
        if (enderecoMac == null || enderecoMac.isBlank()) {
            throw new IllegalArgumentException("Endereço MAC é obrigatório");
        }
        if (dataAquisicao == null) {
            throw new IllegalArgumentException("Data de aquisição é obrigatória");
        }
        if (localizacao == null || localizacao.isBlank()) {
            throw new IllegalArgumentException("Localização é obrigatória");
        }
        validarDatasDeCriacao(dataAquisicao, null, null);

        LocalDateTime agora = LocalDateTime.now();
        return new InventarioEquipamentos(
                UUID.randomUUID().toString(), skuProduto, funcionarioId, numSerie, enderecoMac,
                dataAquisicao, null, null, "", localizacao, "",
                "ATIVO", "BOM", new byte[0], new byte[0], "", false,
                agora, agora, null, false
        );
    }

    public static InventarioEquipamentos novoCompleto(
            String skuProduto,
            String funcionarioId,
            String numSerie,
            String enderecoMac,
            LocalDate dataAquisicao,
            LocalDate dataInstalacao,
            String localizacao,
            String departamento,
            String status,
            String condicao,
            String observacoes
    ) {
        if (funcionarioId == null || funcionarioId.isBlank()) {
            throw new IllegalArgumentException("ID do funcionário é obrigatório");
        }
        if (numSerie == null || numSerie.isBlank()) {
            throw new IllegalArgumentException("Número de série é obrigatório");
        }
        if (enderecoMac == null || enderecoMac.isBlank()) {
            throw new IllegalArgumentException("Endereço MAC é obrigatório");
        }
        if (dataAquisicao == null) {
            throw new IllegalArgumentException("Data de aquisição é obrigatória");
        }
        if (localizacao == null || localizacao.isBlank()) {
            throw new IllegalArgumentException("Localização é obrigatória");
        }
        validarDatasDeCriacao(dataAquisicao, dataInstalacao, null);

        LocalDateTime agora = LocalDateTime.now();
        return new InventarioEquipamentos(
                UUID.randomUUID().toString(), skuProduto, funcionarioId, numSerie, enderecoMac,
                dataAquisicao, dataInstalacao, null, "", localizacao, departamento,
                status, condicao, new byte[0], new byte[0], observacoes, false,
                agora, agora, null, false
        );
    }

    public static InventarioEquipamentos novoComFatura(
            String skuProduto,
            String funcionarioId,
            String numSerie,
            String enderecoMac,
            LocalDate dataAquisicao,
            String localizacao,
            String numeroFatura,
            byte[] documentoEntrega
    ) {
        if (funcionarioId == null || funcionarioId.isBlank()) {
            throw new IllegalArgumentException("ID do funcionário é obrigatório");
        }
        if (numSerie == null || numSerie.isBlank()) {
            throw new IllegalArgumentException("Número de série é obrigatório");
        }
        if (enderecoMac == null || enderecoMac.isBlank()) {
            throw new IllegalArgumentException("Endereço MAC é obrigatório");
        }
        if (dataAquisicao == null) {
            throw new IllegalArgumentException("Data de aquisição é obrigatória");
        }
        if (localizacao == null || localizacao.isBlank()) {
            throw new IllegalArgumentException("Localização é obrigatória");
        }
        validarDatasDeCriacao(dataAquisicao, null, null);

        LocalDateTime agora = LocalDateTime.now();
        return new InventarioEquipamentos(
                UUID.randomUUID().toString(), skuProduto, funcionarioId, numSerie, enderecoMac,
                dataAquisicao, null, null, numeroFatura, localizacao, "",
                "ATIVO", "BOM", documentoEntrega, new byte[0], "", false,
                agora, agora, null, false
        );
    }

    // ===== MÉTODOS DE NEGÓCIO =====

    public boolean isAtivo() {
        return "ATIVO".equals(status);
    }

    public boolean isEmManutencao() {
        return "MANUTENCAO".equals(status);
    }

    public boolean isBaixado() {
        return "BAIXADO".equals(status);
    }

    public boolean isReserva() {
        return "RESERVA".equals(status);
    }

    public boolean isEmUso() {
        return "EM_USO".equals(status);
    }

    public boolean isDeletado() {
        return Boolean.TRUE.equals(deleted);
    }

    public boolean isCondicaoOtima() {
        return "OTIMO".equals(condicao);
    }

    public boolean isCondicaoCritica() {
        return "CRITICO".equals(condicao);
    }

    public boolean isVerificacaoAtrasada(int meses) {
        if (dataUltimaVerificacao == null) return true;
        return dataUltimaVerificacao.plusMonths(meses).isBefore(LocalDate.now());
    }

    public boolean isVerificacaoAtrasada() {
        return isVerificacaoAtrasada(12);
    }

    public boolean isMacAddressValido() {
        if (enderecoMac == null || enderecoMac.isBlank()) return false;
        return enderecoMac.matches("^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$");
    }

    public boolean temDocumentoEntrega() {
        return documentoEntrega != null && documentoEntrega.length > 0;
    }

    public boolean temDocumentoDevolucao() {
        return documentoDevolucao != null && documentoDevolucao.length > 0;
    }

    public boolean temFatura() {
        return numeroFatura != null && !numeroFatura.isBlank();
    }

    public String getNomeCompleto() {
        // numSerie pode ser null em registros legados (S10) — exibição segura
        return numSerie != null && !numSerie.isBlank()
                ? String.format("%s [%s]", numSerie, skuProduto)
                : String.format("[sem série] %s", skuProduto);
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

    public String getCondicaoDescricao() {
        return switch (condicao) {
            case "OTIMO" -> "Ótimo";
            case "BOM" -> "Bom";
            case "REGULAR" -> "Regular";
            case "CRITICO" -> "Crítico";
            default -> condicao;
        };
    }

    // ===== MÉTODOS DE TRANSFORMAÇÃO =====
    // deviceId nunca é alterado aqui — repository preenche na escrita

    public InventarioEquipamentos comId(String novoId) {
        return new InventarioEquipamentos(
                novoId, skuProduto, funcionarioId, numSerie, enderecoMac,
                dataAquisicao, dataInstalacao, dataUltimaVerificacao,
                numeroFatura, localizacao, departamento, status, condicao,
                documentoEntrega, documentoDevolucao, observacoes, devolucao,
                createdAt, updatedAt, deviceId, deleted
        );
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
                createdAt, LocalDateTime.now(), deviceId, deleted
        );
    }

    public InventarioEquipamentos comCondicao(String novaCondicao) {
        if (!CONDICOES_VALIDAS.contains(novaCondicao)) {
            throw new IllegalArgumentException("Condição inválida: " + novaCondicao);
        }
        return new InventarioEquipamentos(
                id, skuProduto, funcionarioId, numSerie, enderecoMac,
                dataAquisicao, dataInstalacao, dataUltimaVerificacao,
                numeroFatura, localizacao, departamento, status, novaCondicao,
                documentoEntrega, documentoDevolucao, observacoes, devolucao,
                createdAt, LocalDateTime.now(), deviceId, deleted
        );
    }

    public InventarioEquipamentos comLocalizacao(String novaLocalizacao, String novoDepartamento) {
        return new InventarioEquipamentos(
                id, skuProduto, funcionarioId, numSerie, enderecoMac,
                dataAquisicao, dataInstalacao, dataUltimaVerificacao,
                numeroFatura, novaLocalizacao, novoDepartamento, status, condicao,
                documentoEntrega, documentoDevolucao, observacoes, devolucao,
                createdAt, LocalDateTime.now(), deviceId, deleted
        );
    }

    public InventarioEquipamentos comFuncionario(String novoFuncionarioId) {
        return new InventarioEquipamentos(
                id, skuProduto, novoFuncionarioId, numSerie, enderecoMac,
                dataAquisicao, dataInstalacao, dataUltimaVerificacao,
                numeroFatura, localizacao, departamento, status, condicao,
                documentoEntrega, documentoDevolucao, observacoes, devolucao,
                createdAt, LocalDateTime.now(), deviceId, deleted
        );
    }

    public InventarioEquipamentos comMacAddress(String novoMac) {
        return new InventarioEquipamentos(
                id, skuProduto, funcionarioId, numSerie, novoMac,
                dataAquisicao, dataInstalacao, dataUltimaVerificacao,
                numeroFatura, localizacao, departamento, status, condicao,
                documentoEntrega, documentoDevolucao, observacoes, devolucao,
                createdAt, LocalDateTime.now(), deviceId, deleted
        );
    }

    public InventarioEquipamentos comNumeroFatura(String novoNumero) {
        return new InventarioEquipamentos(
                id, skuProduto, funcionarioId, numSerie, enderecoMac,
                dataAquisicao, dataInstalacao, dataUltimaVerificacao,
                novoNumero, localizacao, departamento, status, condicao,
                documentoEntrega, documentoDevolucao, observacoes, devolucao,
                createdAt, LocalDateTime.now(), deviceId, deleted
        );
    }

    public InventarioEquipamentos comDocumentoEntrega(byte[] doc) {
        return new InventarioEquipamentos(
                id, skuProduto, funcionarioId, numSerie, enderecoMac,
                dataAquisicao, dataInstalacao, dataUltimaVerificacao,
                numeroFatura, localizacao, departamento, status, condicao,
                doc, documentoDevolucao, observacoes, devolucao,
                createdAt, LocalDateTime.now(), deviceId, deleted
        );
    }

    public InventarioEquipamentos comDocumentoDevolucao(byte[] doc) {
        return new InventarioEquipamentos(
                id, skuProduto, funcionarioId, numSerie, enderecoMac,
                dataAquisicao, dataInstalacao, dataUltimaVerificacao,
                numeroFatura, localizacao, departamento, status, condicao,
                documentoEntrega, doc, observacoes, devolucao,
                createdAt, LocalDateTime.now(), deviceId, deleted
        );
    }

    public InventarioEquipamentos comDevolucao(boolean novaDevolucao) {
        return new InventarioEquipamentos(
                id, skuProduto, funcionarioId, numSerie, enderecoMac,
                dataAquisicao, dataInstalacao, dataUltimaVerificacao,
                numeroFatura, localizacao, departamento, status, condicao,
                documentoEntrega, documentoDevolucao, observacoes, novaDevolucao,
                createdAt, LocalDateTime.now(), deviceId, deleted
        );
    }

    public InventarioEquipamentos comVerificacao(LocalDate data, String novaCondicao, String obs) {
        if (!CONDICOES_VALIDAS.contains(novaCondicao)) {
            throw new IllegalArgumentException("Condição inválida: " + novaCondicao);
        }
        String novaObservacao = observacoes != null && !observacoes.isBlank()
                ? observacoes + "\nVerificação (" + data + "): " + obs
                : "Verificação (" + data + "): " + obs;
        return new InventarioEquipamentos(
                id, skuProduto, funcionarioId, numSerie, enderecoMac,
                dataAquisicao, dataInstalacao, data, numeroFatura,
                localizacao, departamento, status, novaCondicao,
                documentoEntrega, documentoDevolucao, novaObservacao, devolucao,
                createdAt, LocalDateTime.now(), deviceId, deleted
        );
    }

    public InventarioEquipamentos comObservacoes(String novasObservacoes) {
        return new InventarioEquipamentos(
                id, skuProduto, funcionarioId, numSerie, enderecoMac,
                dataAquisicao, dataInstalacao, dataUltimaVerificacao,
                numeroFatura, localizacao, departamento, status, condicao,
                documentoEntrega, documentoDevolucao, novasObservacoes, devolucao,
                createdAt, LocalDateTime.now(), deviceId, deleted
        );
    }

    /** Tombstone de sincronização: marca exclusão lógica com updatedAt novo (LWW). */
    public InventarioEquipamentos comDeletado(boolean deletado) {
        return new InventarioEquipamentos(
                id, skuProduto, funcionarioId, numSerie, enderecoMac,
                dataAquisicao, dataInstalacao, dataUltimaVerificacao,
                numeroFatura, localizacao, departamento, status, condicao,
                documentoEntrega, documentoDevolucao, observacoes, devolucao,
                createdAt, LocalDateTime.now(), deviceId, deletado
        );
    }

    // ===== MÉTODOS ESTÁTICOS DE VALIDAÇÃO =====

    public static boolean isStatusValido(String status) {
        return STATUS_VALIDOS.contains(status);
    }

    public static boolean isCondicaoValida(String condicao) {
        return CONDICOES_VALIDAS.contains(condicao);
    }

    public static List<String> getStatusValidos() {
        return STATUS_VALIDOS;
    }

    public static List<String> getCondicoesValidas() {
        return CONDICOES_VALIDAS;
    }

    // ===== EQUALS/HASHCODE =====
    // Campos de sync (deviceId, deleted) e timestamps ficam FORA — semântica v2.1 preservada.

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InventarioEquipamentos that)) return false;
        return Objects.equals(id, that.id) &&
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
                Objects.equals(devolucao, that.devolucao);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, skuProduto, funcionarioId, numSerie, enderecoMac,
                dataAquisicao, dataInstalacao, dataUltimaVerificacao, numeroFatura,
                localizacao, departamento, status, condicao, Arrays.hashCode(documentoEntrega),
                Arrays.hashCode(documentoDevolucao), observacoes, devolucao);
    }

    @Override
    public String toString() {
        return String.format("InventarioEquipamentos[ID=%s, Série=%s, MAC=%s, Status=%s]",
                id, numSerie, enderecoMac, status);
    }
}