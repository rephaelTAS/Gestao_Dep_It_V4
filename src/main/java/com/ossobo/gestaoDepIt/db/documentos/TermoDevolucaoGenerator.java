package com.ossobo.gestaoDepIt.db.documentos;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.ossobo.gestaoDepIt.db.models.CatalogoProdutos;
import com.ossobo.gestaoDepIt.db.models.Funcionarios;
import com.ossobo.gestaoDepIt.db.models.InventarioEquipamentos;

/**
 * TermoDevolucaoGenerator v1.0
 *
 * Responsabilidade: gerar o PDF "Termo de Responsabilidade de Devolução de
 * Equipamento Informático" a partir de um equipamento, seu responsável e
 * o produto do catálogo.
 *
 * @since v1.0
 */
public class TermoDevolucaoGenerator extends DocumentoTermoBase {

    private final InventarioEquipamentos equip;
    private final Funcionarios funcionario;
    private final CatalogoProdutos produto;

    public TermoDevolucaoGenerator(InventarioEquipamentos equip,
                                   Funcionarios funcionario,
                                   CatalogoProdutos produto) {
        if (equip == null)       throw new IllegalArgumentException("Equipamento é obrigatório");
        if (funcionario == null) throw new IllegalArgumentException("Funcionário é obrigatório");
        if (produto == null)     throw new IllegalArgumentException("Produto é obrigatório");
        this.equip = equip;
        this.funcionario = funcionario;
        this.produto = produto;
    }

    @Override protected String titulo() {
        return "TERMO DE RESPONSABILIDADE DE DEVOLUÇÃO DE EQUIPAMENTO INFORMÁTICO";
    }

    @Override protected String subtitulo() {
        return "Devolução de Equipamentos da Empresa";
    }

    @Override
    protected void escreverCorpo(Document doc) throws DocumentException {
        doc.add(new Paragraph(
                "Eu, " + nvl(funcionario.nome()) + ", com o número de matrícula "
                        + nvl(funcionario.codDep()) + ", funcionário(a) da empresa "
                        + EMPRESA + ", declaro para os devidos fins que:", FONT_TEXTO));

        doc.add(secao("1. Devolvi o seguinte equipamento à empresa:"));
        doc.add(bullet("Descrição: " + descricaoProduto()));
        doc.add(bullet("Número de série: " + nvl(equip.numSerie())));
        doc.add(bullet("Outros detalhes: " + nvl(equip.observacoes())));

        doc.add(secao("2. Condições de devolução:"));
        doc.add(bullet("O equipamento devolvido encontra-se nas seguintes condições: "
                + descreverCondicao()));

        doc.add(secao("3. Declaro que:"));
        doc.add(bullet("Entrego o equipamento livre de qualquer dano ou perda, exceto por "
                + "desgaste natural decorrente do uso regular;"));
        doc.add(bullet("O equipamento foi devolvido na data de "
                + java.time.LocalDate.now().format(DATA_BR)
                + " ao responsável da empresa " + EMPRESA + ";"));
        doc.add(bullet("O equipamento está completo, sem alterações ou modificações, e com "
                + "todos os acessórios e componentes entregues inicialmente."));

        doc.add(secao("4. Confirmação de devolução:"));
        doc.add(bullet("Declaro que recebi e compreendi as orientações de devolução do "
                + "equipamento, e que a entrega foi realizada conforme as instruções "
                + "da empresa."));
    }

    private String descricaoProduto() {
        return nvl(produto.marca()) + " " + nvl(produto.modelo());
    }

    private String descreverCondicao() {
        String condicao = nvl(equip.condicao());
        String obs = nvl(equip.observacoes());
        return obs.isBlank() ? condicao : condicao + " — " + obs;
    }

    private Paragraph secao(String texto) {
        Paragraph p = new Paragraph(texto, FONT_SECAO);
        p.setSpacingBefore(12f);
        p.setSpacingAfter(6f);
        return p;
    }

    private Paragraph bullet(String texto) {
        Paragraph p = new Paragraph("  •  " + texto, FONT_TEXTO);
        p.setIndentationLeft(20f);
        p.setSpacingAfter(4f);
        return p;
    }

    private String nvl(String s) { return s != null ? s : ""; }
}