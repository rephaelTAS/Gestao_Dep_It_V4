package com.ossobo.gestaoDepIt.controlls.historico.historicoList.cellfactories;

import com.ossobo.gestaoDepIt.db.models.HistoricoEventos;

/**
 * HistoricoAcoesCallback v1.0
 *
 * Contrato entre a coluna de ações e a camada que sabe abrir janelas.
 * A cell factory emite intenção; o controller decide o destino.
 */
public interface HistoricoAcoesCallback {

    /** Abrir a tela de detalhes do evento. */
    void abrirDetalhes(HistoricoEventos evento);
}