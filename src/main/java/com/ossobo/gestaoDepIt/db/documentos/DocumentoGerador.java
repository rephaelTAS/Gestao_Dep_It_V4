package com.ossobo.gestaoDepIt.db.documentos;

import com.ossobo.gestaoDepIt.db.models.CatalogoProdutos;
import com.ossobo.gestaoDepIt.db.models.Funcionarios;
import com.ossobo.gestaoDepIt.db.models.InventarioEquipamentos;
import com.ossobo.winterfx.anotations.Service;

/**
 * DocumentoGerador v1.0
 *
 * Responsabilidade: fachada única que produz os PDFs de termo de entrega e
 * termo de devolução a partir do equipamento + responsável + produto.
 *
 * Não resolve dependências: recebe tudo pronto (mais testável, sem ciclo).
 *
 * @since v1.0
 */
@Service
public class DocumentoGerador {

    public byte[] gerarEntrega(InventarioEquipamentos equip,
                               Funcionarios funcionario,
                               CatalogoProdutos produto) {
        return new TermoEntregaGenerator(equip, funcionario, produto).gerar();
    }

    public byte[] gerarDevolucao(InventarioEquipamentos equip,
                                 Funcionarios funcionario,
                                 CatalogoProdutos produto) {
        return new TermoDevolucaoGenerator(equip, funcionario, produto).gerar();
    }
}