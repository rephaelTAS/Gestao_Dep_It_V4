package com.ossobo.gestaoDepIt.db.models;

import com.ossobo.gestaoDepIt.db.enums.TipoEvento;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * HistoricoEventos v3.3 - Modelo imutável com Record (Java 17+)
 *
 * Schema: historico_eventos (id TEXT PRIMARY KEY — UUID v4)
 *
 * LEDGER append-only: não edita, não deleta, sem tombstone.
 * DECISÃO RATIFICADA (Letra I): SEM FOREIGN KEY.
 *
 * v3.3 — numSerie passa a ser campo próprio do evento (chave do equipamento).
 *        Regra do ledger: a chave é (skuProduto, funcionarioId, numSerie) —
 *        identificar o equipamento físico sem misturar históricos.
 *        Todos os eventos têm num_serie ('' para auth/produto).
 *        Fábricas atualizadas para aceitar numSerie explicitamente.
 *
 * v3.2 — Tipos válidos migram para enum TipoEvento (fonte única).
 */
public record HistoricoEventos(
        String id,                   // UUID v4 — gerado no gargalo
        String tipoEvento,
        String skuProduto,
        String numSerie,             // chave do equipamento físico — '' quando N/A
        String funcionarioId,        // referência funcional — SEM FK (Letra I)
        String descricaoFuncionario,
        String dadosAnteriores,      // JSON do estado anterior (regra do ledger)
        String dadosNovos,           // JSON do estado atual
        LocalDateTime createdAt,
        String deviceId
) {
    public HistoricoEventos {
        if (id == null || id.isBlank()) {
            id = UUID.randomUUID().toString();
        }
        if (tipoEvento == null || tipoEvento.isBlank()) {
            throw new IllegalArgumentException("Tipo de evento é obrigatório");
        }
        if (!TipoEvento.isValido(tipoEvento)) {
            throw new IllegalArgumentException("Tipo de evento inválido: " + tipoEvento);
        }
        if (skuProduto == null || skuProduto.isBlank()) {
            throw new IllegalArgumentException("SKU do produto é obrigatório");
        }
        if (funcionarioId == null || funcionarioId.isBlank()) {
            throw new IllegalArgumentException("ID do funcionário é obrigatório");
        }
        if (dadosNovos == null || dadosNovos.isBlank()) {
            throw new IllegalArgumentException("Dados novos são obrigatórios");
        }

        tipoEvento = tipoEvento.trim().toUpperCase();
        if (numSerie == null) numSerie = "";              // obrigatório na coluna, '' quando N/A
        if (descricaoFuncionario == null) descricaoFuncionario = "{}";
        if (dadosAnteriores == null) dadosAnteriores = "{}";
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (deviceId == null) deviceId = "";
    }

    // ===== FÁBRICAS POR TIPO =====

    public static HistoricoEventos criacao(String sku, String numSerie, String func,
                                           String dadosNovos, String descricao) {
        return fabricar(TipoEvento.CRIACAO, sku, numSerie, func, null, dadosNovos, descricao);
    }

    public static HistoricoEventos atualizacao(String sku, String numSerie, String func,
                                               String anteriores, String novos, String descricao) {
        return fabricar(TipoEvento.ATUALIZACAO, sku, numSerie, func, anteriores, novos, descricao);
    }

    public static HistoricoEventos baixa(String sku, String numSerie, String func,
                                         String anteriores, String descricao) {
        return fabricar(TipoEvento.BAIXA, sku, numSerie, func, anteriores, "{}", descricao);
    }

    public static HistoricoEventos exclusao(String sku, String numSerie, String func,
                                            String anteriores, String descricao) {
        return fabricar(TipoEvento.EXCLUSAO, sku, numSerie, func, anteriores, "{}", descricao);
    }

    public static HistoricoEventos manutencao(String sku, String numSerie, String func,
                                              String dados, String descricao) {
        return fabricar(TipoEvento.MANUTENCAO, sku, numSerie, func, null, dados, descricao);
    }

    public static HistoricoEventos movimentacao(String sku, String func,
                                                String dados, String descricao) {
        // Movimentação é de PRODUTO (estoque), não de equipamento físico — sem série.
        return fabricar(TipoEvento.MOVIMENTACAO, sku, "", func, null, dados, descricao);
    }

    public static HistoricoEventos instalacao(String sku, String numSerie, String func,
                                              String dados, String descricao) {
        return fabricar(TipoEvento.INSTALACAO, sku, numSerie, func, null, dados, descricao);
    }

    public static HistoricoEventos devolucao(String sku, String numSerie, String func,
                                             String anteriores, String descricao) {
        return fabricar(TipoEvento.DEVOLUCAO, sku, numSerie, func, anteriores, "{}", descricao);
    }

    public static HistoricoEventos transferencia(String sku, String numSerie, String func,
                                                 String anteriores, String novos,
                                                 String descricao) {
        return fabricar(TipoEvento.TRANSFERENCIA, sku, numSerie, func, anteriores, novos, descricao);
    }

    public static HistoricoEventos localizacao(String sku, String numSerie, String func,
                                               String dados, String descricao) {
        return fabricar(TipoEvento.LOCALIZACAO, sku, numSerie, func, null, dados, descricao);
    }

    public static HistoricoEventos login(String func, String dadosLogin, String descricao) {
        return fabricar(TipoEvento.LOGIN, "AUTH", "", func, null, dadosLogin, descricao);
    }

    public static HistoricoEventos logout(String func, String dadosLogout, String descricao) {
        return fabricar(TipoEvento.LOGOUT, "AUTH", "", func, null, dadosLogout, descricao);
    }

    /** Fábrica genérica — preserva compatibilidade com porTipo(tipo, ...). */
    public static HistoricoEventos porTipo(String tipo, String sku, String numSerie, String func,
                                           String anteriores, String novos) {
        return fabricar(TipoEvento.de(tipo), sku, numSerie, func, anteriores,
                (novos == null || novos.isBlank()) ? "{}" : novos, null);
    }

    private static HistoricoEventos fabricar(TipoEvento tipo, String sku, String numSerie,
                                             String func, String anteriores,
                                             String novos, String descricao) {
        return new HistoricoEventos(
                UUID.randomUUID().toString(),
                tipo.name(),
                sku,
                numSerie != null ? numSerie : "",
                func,
                descricao,
                anteriores != null ? anteriores : "{}",
                novos != null ? novos : "{}",
                LocalDateTime.now(),
                null
        );
    }

    // ===== HELPERS =====

    public boolean isCriacao() {
        return TipoEvento.CRIACAO.name().equals(tipoEvento);
    }

    public boolean isAtualizacao() {
        return TipoEvento.ATUALIZACAO.name().equals(tipoEvento);
    }

    public String getTipoDescricao() {
        return switch (tipoEvento) {
            case "CRIACAO"       -> "Criação";
            case "ATUALIZACAO"   -> "Atualização";
            case "DEVOLUCAO"     -> "Devolução";
            case "BAIXA"         -> "Baixa";
            case "EXCLUSAO"      -> "Exclusão";
            case "MANUTENCAO"    -> "Manutenção";
            case "MOVIMENTACAO"  -> "Movimentação";
            case "LOCALIZACAO"   -> "Localização";
            case "INSTALACAO"    -> "Instalação";
            case "LOGIN"         -> "Login";
            case "LOGOUT"        -> "Logout";
            case "TRANSFERENCIA" -> "Transferência";
            default              -> tipoEvento;
        };
    }

    public String getDescricaoEvento() {
        return String.format("%s do produto %s (série %s) pelo funcionário %s",
                getTipoDescricao(), skuProduto,
                numSerie != null && !numSerie.isBlank() ? numSerie : "N/A",
                funcionarioId);
    }

    @Override
    public String toString() {
        return String.format("HistoricoEventos[ID=%s, %s SKU:%s, Série:%s, Func:%s, Data:%s]",
                id, tipoEvento, skuProduto, numSerie, funcionarioId, createdAt);
    }
}