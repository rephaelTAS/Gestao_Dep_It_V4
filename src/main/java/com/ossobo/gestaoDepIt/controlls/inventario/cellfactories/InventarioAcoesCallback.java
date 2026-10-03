package com.ossobo.gestaoDepIt.controlls.inventario.cellfactories;

import com.ossobo.gestaoDepIt.db.models.InventarioEquipamentos;

/**
 * InventarioAcoesCallback v1.0
 *
 * Contrato entre a InventarioAcoesCellFactory e a camada que conhece as
 * janelas flutuantes (InventarioEquipamentosListController).
 *
 * A cell factory não abre janelas, não chama Rotas — apenas emite intenção.
 */
public interface InventarioAcoesCallback {

    /** Abrir a tela de detalhes do equipamento. */
    void abrirDetalhes(InventarioEquipamentos equipamento);

    /** Abrir a tela de edição do equipamento. */
    void abrirEdicao(InventarioEquipamentos equipamento);

    /** Abrir a tela de histórico pré-filtrado por SKU + Funcionário. */
    void abrirHistorico(InventarioEquipamentos equipamento);
}