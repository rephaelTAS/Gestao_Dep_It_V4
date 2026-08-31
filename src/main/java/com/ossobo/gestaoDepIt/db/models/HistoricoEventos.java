package com.ossobo.gestaoDepIt.db.models;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * HistoricoEventos - Modelo imutável com Record (Java 17+)
 * v3.1 - Degrau 1: UUID universal + deviceId (ledger append-only) + fábrica genérica porTipo
 *
 * Schema: historico_eventos (id TEXT PRIMARY KEY — UUID v4)
 *
 * Responsabilidades:
 * - Representação imutável de evento histórico
 * - Validação de negócio no construtor
 * - Métodos auxiliares para tipo de evento
 * - Transporte de metadados de sync (deviceId — origem do evento)
 *
 * Natureza: LEDGER append-only
 * - Nunca edita, nunca deleta → sem tombstone, sem updated_at
 * - Convergência por união de UUIDs disjuntos (conflito impossível entre devices)
 *
 * Mudanças v3.0 → v3.1:
 * - +porTipo(...): fábrica genérica para despachos dinâmicos onde o tipo só é
 *   conhecido em runtime (ex.: InventarioHistoricoRegistrar.registrarAcao).
 *   Preserva dadosAnteriores para QUALQUER tipo (comportamento do utilitário legado).
 *
 * Mudanças v2.0 → v3.0 (histórico):
 * - id: Long → String (UUID v4, gerado pelas fábricas)
 * - +deviceId: device de origem do evento (rastreio de sync; repository preenche)
 * - SEM deleted: append-only não deleta (tombstone desnecessário)
 * - OBS-H3: TIPOS_VALIDOS alinhados ao ENUM MySQL real — 12 tipos
 *   (adicionados DEVOLUCAO, LOCALIZACAO, TRANSFERENCIA)
 * - toString(): %d → %s (id agora é String)
 */
public record HistoricoEventos(
        String id,                   // UUID v4 — gerado na fábrica, nunca pelo construtor
        String tipoEvento,           // ENUM MySQL real: 12 tipos válidos
        String skuProduto,
        String funcionarioId,
        String descricaoFuncionario, // JSON
        String dadosAnteriores,      // JSON
        String dadosNovos,           // JSON (NOT NULL)
        LocalDateTime createdAt,
        String deviceId              // sync: origem do evento (rastreio)
) {
    // ===== CONSTANTES =====
    // Alinhado ao ENUM do MySQL real (fonte da verdade) — 12 tipos.
    // OBS-H3: registros legados com DEVOLUCAO/LOCALIZACAO/TRANSFERENCIA
    // não podem quebrar o bootstrap.
    public static final List<String> TIPOS_VALIDOS = List.of(
            "CRIACAO", "ATUALIZACAO", "DEVOLUCAO", "BAIXA", "EXCLUSAO",
            "MANUTENCAO", "MOVIMENTACAO", "LOCALIZACAO", "INSTALACAO",
            "LOGIN", "LOGOUT", "TRANSFERENCIA"
    );

    // ===== CONSTRUTOR COMPACTO (VALIDAÇÃO) =====
    public HistoricoEventos {
        // Validação com Pattern Matching (Java 16+)
        if (!(tipoEvento instanceof String t) || t.isBlank()) {
            throw new IllegalArgumentException("Tipo de evento é obrigatório");
        }
        if (!TIPOS_VALIDOS.contains(t)) {
            throw new IllegalArgumentException("Tipo de evento inválido: " + t +
                    ". Tipos permitidos: " + String.join(", ", TIPOS_VALIDOS));
        }

        if (!(skuProduto instanceof String sku) || sku.isBlank()) {
            throw new IllegalArgumentException("SKU do produto é obrigatório");
        }

        if (!(funcionarioId instanceof String func) || func.isBlank()) {
            throw new IllegalArgumentException("ID do funcionário é obrigatório");
        }

        if (!(dadosNovos instanceof String dados) || dados.isBlank()) {
            throw new IllegalArgumentException("Dados novos são obrigatórios");
        }

        // Valores padrão
        if (descricaoFuncionario == null) descricaoFuncionario = "{}";
        if (dadosAnteriores == null) dadosAnteriores = "{}";
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    // ===== CONSTRUTORES DE FÁBRICA (geram UUID + timestamps) =====

    public static HistoricoEventos criacao(
            String skuProduto,
            String funcionarioId,
            String dadosNovos,
            String descricaoFuncionario
    ) {
        return new HistoricoEventos(
                UUID.randomUUID().toString(), "CRIACAO", skuProduto, funcionarioId,
                descricaoFuncionario, null, dadosNovos, LocalDateTime.now(), null
        );
    }

    public static HistoricoEventos atualizacao(
            String skuProduto,
            String funcionarioId,
            String dadosAnteriores,
            String dadosNovos,
            String descricaoFuncionario
    ) {
        return new HistoricoEventos(
                UUID.randomUUID().toString(), "ATUALIZACAO", skuProduto, funcionarioId,
                descricaoFuncionario, dadosAnteriores, dadosNovos, LocalDateTime.now(), null
        );
    }

    public static HistoricoEventos baixa(
            String skuProduto,
            String funcionarioId,
            String dadosAnteriores,
            String descricaoFuncionario
    ) {
        return new HistoricoEventos(
                UUID.randomUUID().toString(), "BAIXA", skuProduto, funcionarioId,
                descricaoFuncionario, dadosAnteriores,
                "{\"status\": \"BAIXADO\"}", LocalDateTime.now(), null
        );
    }

    public static HistoricoEventos exclusao(
            String skuProduto,
            String funcionarioId,
            String dadosAnteriores,
            String descricaoFuncionario
    ) {
        return new HistoricoEventos(
                UUID.randomUUID().toString(), "EXCLUSAO", skuProduto, funcionarioId,
                descricaoFuncionario, dadosAnteriores,
                "{\"status\": \"EXCLUIDO\"}", LocalDateTime.now(), null
        );
    }

    public static HistoricoEventos manutencao(
            String skuProduto,
            String funcionarioId,
            String dadosManutencao,
            String descricaoFuncionario
    ) {
        return new HistoricoEventos(
                UUID.randomUUID().toString(), "MANUTENCAO", skuProduto, funcionarioId,
                descricaoFuncionario, null, dadosManutencao, LocalDateTime.now(), null
        );
    }

    public static HistoricoEventos movimentacao(
            String skuProduto,
            String funcionarioId,
            String dadosMovimentacao,
            String descricaoFuncionario
    ) {
        return new HistoricoEventos(
                UUID.randomUUID().toString(), "MOVIMENTACAO", skuProduto, funcionarioId,
                descricaoFuncionario, null, dadosMovimentacao, LocalDateTime.now(), null
        );
    }

    public static HistoricoEventos instalacao(
            String skuProduto,
            String funcionarioId,
            String dadosInstalacao,
            String descricaoFuncionario
    ) {
        return new HistoricoEventos(
                UUID.randomUUID().toString(), "INSTALACAO", skuProduto, funcionarioId,
                descricaoFuncionario, null, dadosInstalacao, LocalDateTime.now(), null
        );
    }

    /** Fábrica do tipo DEVOLUCAO (presente no ENUM MySQL real — antes não gerável). */
    public static HistoricoEventos devolucao(
            String skuProduto,
            String funcionarioId,
            String dadosDevolucao,
            String descricaoFuncionario
    ) {
        return new HistoricoEventos(
                UUID.randomUUID().toString(), "DEVOLUCAO", skuProduto, funcionarioId,
                descricaoFuncionario, null, dadosDevolucao, LocalDateTime.now(), null
        );
    }

    /** Fábrica do tipo LOCALIZACAO (presente no ENUM MySQL real — antes não gerável). */
    public static HistoricoEventos localizacao(
            String skuProduto,
            String funcionarioId,
            String dadosLocalizacao,
            String descricaoFuncionario
    ) {
        return new HistoricoEventos(
                UUID.randomUUID().toString(), "LOCALIZACAO", skuProduto, funcionarioId,
                descricaoFuncionario, null, dadosLocalizacao, LocalDateTime.now(), null
        );
    }

    /** Fábrica do tipo TRANSFERENCIA (presente no ENUM MySQL real — antes não gerável). */
    public static HistoricoEventos transferencia(
            String skuProduto,
            String funcionarioId,
            String dadosTransferencia,
            String descricaoFuncionario
    ) {
        return new HistoricoEventos(
                UUID.randomUUID().toString(), "TRANSFERENCIA", skuProduto, funcionarioId,
                descricaoFuncionario, null, dadosTransferencia, LocalDateTime.now(), null
        );
    }

    public static HistoricoEventos login(
            String funcionarioId,
            String dadosLogin,
            String descricaoFuncionario
    ) {
        return new HistoricoEventos(
                UUID.randomUUID().toString(), "LOGIN", "SISTEMA", funcionarioId,
                descricaoFuncionario, null, dadosLogin, LocalDateTime.now(), null
        );
    }

    public static HistoricoEventos logout(
            String funcionarioId,
            String dadosLogout,
            String descricaoFuncionario
    ) {
        return new HistoricoEventos(
                UUID.randomUUID().toString(), "LOGOUT", "SISTEMA", funcionarioId,
                descricaoFuncionario, null, dadosLogout, LocalDateTime.now(), null
        );
    }

    /**
     * Fábrica genérica por tipo — para despachos dinâmicos onde o tipo só é
     * conhecido em runtime (ex.: InventarioHistoricoRegistrar.registrarAcao).
     * Preserva dadosAnteriores para QUALQUER tipo (comportamento do utilitário legado).
     * Mantém a regra: UUID + timestamps gerados AQUI, nunca pelo chamador.
     *
     * @param tipo um dos 12 valores de TIPOS_VALIDOS (validado no construtor)
     */
    public static HistoricoEventos porTipo(String tipo, String skuProduto, String funcionarioId,
                                           String dadosAnteriores, String dadosNovos) {
        return new HistoricoEventos(
                UUID.randomUUID().toString(), tipo, skuProduto, funcionarioId,
                null, dadosAnteriores, dadosNovos, LocalDateTime.now(), null
        );
    }

    // ===== MÉTODOS DE NEGÓCIO =====

    public boolean isCriacao() {
        return "CRIACAO".equals(tipoEvento);
    }

    public boolean isAtualizacao() {
        return "ATUALIZACAO".equals(tipoEvento);
    }

    public boolean isDevolucao() {
        return "DEVOLUCAO".equals(tipoEvento);
    }

    public boolean isBaixa() {
        return "BAIXA".equals(tipoEvento);
    }

    public boolean isExclusao() {
        return "EXCLUSAO".equals(tipoEvento);
    }

    public boolean isManutencao() {
        return "MANUTENCAO".equals(tipoEvento);
    }

    public boolean isMovimentacao() {
        return "MOVIMENTACAO".equals(tipoEvento);
    }

    public boolean isLocalizacao() {
        return "LOCALIZACAO".equals(tipoEvento);
    }

    public boolean isInstalacao() {
        return "INSTALACAO".equals(tipoEvento);
    }

    public boolean isLogin() {
        return "LOGIN".equals(tipoEvento);
    }

    public boolean isLogout() {
        return "LOGOUT".equals(tipoEvento);
    }

    public boolean isTransferencia() {
        return "TRANSFERENCIA".equals(tipoEvento);
    }

    public boolean isEventoAutenticacao() {
        return isLogin() || isLogout();
    }

    /**
     * ⚠️ OBS-H3b: lista de "alteração" NÃO foi estendida para os novos tipos
     * (DEVOLUCAO/LOCALIZACAO/TRANSFERENCIA) — chamadores podem usá-la como gatilho
     * de lógica. Revisar semântica no Degrau 2 com os services reais em mãos.
     */
    public boolean isEventoAlteracao() {
        return isCriacao() || isAtualizacao() || isBaixa() || isExclusao();
    }

    /**
     * Extrai o nome do funcionário do JSON de descrição
     */
    public String getFuncionarioNome() {
        if (descricaoFuncionario == null || descricaoFuncionario.isBlank()) {
            return null;
        }
        try {
            // Busca "nome" no JSON
            int idx = descricaoFuncionario.indexOf("\"nome\"");
            if (idx >= 0) {
                int start = descricaoFuncionario.indexOf("\"", idx + 7) + 1;
                int end = descricaoFuncionario.indexOf("\"", start);
                if (start > 0 && end > start) {
                    return descricaoFuncionario.substring(start, end);
                }
            }
        } catch (Exception e) {
            // Fallback silencioso
        }
        return null;
    }

    /**
     * Retorna informação do funcionário (ID + Nome)
     */
    public String getUsuarioInfo() {
        String nome = getFuncionarioNome();
        if (nome != null && !nome.isBlank()) {
            return String.format("ID: %s | Nome: %s", funcionarioId, nome);
        }
        return String.format("ID: %s", funcionarioId);
    }

    /**
     * Retorna descrição legível do evento
     */
    public String getDescricaoEvento() {
        String tipo = switch (tipoEvento) {
            case "CRIACAO" -> "Criação";
            case "ATUALIZACAO" -> "Atualização";
            case "DEVOLUCAO" -> "Devolução";
            case "BAIXA" -> "Baixa";
            case "EXCLUSAO" -> "Exclusão";
            case "MANUTENCAO" -> "Manutenção";
            case "MOVIMENTACAO" -> "Movimentação";
            case "LOCALIZACAO" -> "Localização";
            case "INSTALACAO" -> "Instalação";
            case "LOGIN" -> "Login";
            case "LOGOUT" -> "Logout";
            case "TRANSFERENCIA" -> "Transferência";
            default -> tipoEvento;
        };
        return String.format("%s do produto %s pelo funcionário %s",
                tipo, skuProduto, funcionarioId);
    }

    public String getTipoDescricao() {
        return switch (tipoEvento) {
            case "CRIACAO" -> "Criação";
            case "ATUALIZACAO" -> "Atualização";
            case "DEVOLUCAO" -> "Devolução";
            case "BAIXA" -> "Baixa";
            case "EXCLUSAO" -> "Exclusão";
            case "MANUTENCAO" -> "Manutenção";
            case "MOVIMENTACAO" -> "Movimentação";
            case "LOCALIZACAO" -> "Localização";
            case "INSTALACAO" -> "Instalação";
            case "LOGIN" -> "Login";
            case "LOGOUT" -> "Logout";
            case "TRANSFERENCIA" -> "Transferência";
            default -> tipoEvento;
        };
    }

    // ===== MÉTODOS DE TRANSFORMAÇÃO =====
    // Ledger: transformação pós-criação local, antes do primeiro persist.
    // deviceId nunca é alterado aqui.

    public HistoricoEventos comId(String novoId) {
        return new HistoricoEventos(
                novoId, tipoEvento, skuProduto, funcionarioId,
                descricaoFuncionario, dadosAnteriores, dadosNovos, createdAt, deviceId
        );
    }

    // ===== MÉTODOS ESTÁTICOS =====

    public static List<String> getTiposValidos() {
        return TIPOS_VALIDOS;
    }

    public static boolean isTipoValido(String tipo) {
        return TIPOS_VALIDOS.contains(tipo);
    }

    @Override
    public String toString() {
        return String.format("HistoricoEventos[ID=%s, %s SKU:%s, Func:%s, Data:%s]",
                id, tipoEvento, skuProduto, funcionarioId, createdAt);
    }
}