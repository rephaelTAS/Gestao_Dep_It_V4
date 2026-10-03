package com.ossobo.gestaoDepIt.db.documentos;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.ossobo.gestaoDepIt.db.models.CatalogoProdutos;
import com.ossobo.gestaoDepIt.db.models.Funcionarios;
import com.ossobo.gestaoDepIt.db.models.InventarioEquipamentos;

/**
 * TermoEntregaGenerator v1.0
 *
 * Responsabilidade: gerar o PDF "Termo de Responsabilidade para Uso de
 * Equipamento Informático" a partir de um equipamento, seu responsável e
 * o produto do catálogo.
 *
 * @since v1.0
 */
public class TermoEntregaGenerator extends DocumentoTermoBase {

    private final InventarioEquipamentos equip;
    private final Funcionarios funcionario;
    private final CatalogoProdutos produto;

    public TermoEntregaGenerator(InventarioEquipamentos equip,
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
        return "TERMO DE RESPONSABILIDADE PARA USO DE EQUIPAMENTO INFORMÁTICO";
    }

    @Override protected String subtitulo() {
        return "Entrega e Uso de Equipamentos da Empresa";
    }

    @Override
    protected void escreverCorpo(Document doc) throws DocumentException {
        doc.add(new Paragraph(
                "Eu, " + nvl(funcionario.nome()) + ", com o número de matrícula "
                        + nvl(funcionario.codDep()) + ", funcionário(a) da empresa "
                        + EMPRESA + ", declaro para os devidos fins que:", FONT_TEXTO));

        doc.add(secao("1. Recebi em perfeitas condições o seguinte equipamento:"));
        doc.add(bullet("Descrição: " + descricaoProduto()));
        doc.add(bullet("Número de série: " + nvl(equip.numSerie())));
        doc.add(bullet("Outros detalhes: " + nvl(equip.observacoes())));

        doc.add(secao("2. Comprometo-me a:"));
        doc.add(bullet("1. Utilizar o equipamento exclusivamente para fins profissionais "
                + "relacionados às minhas funções na empresa " + EMPRESA + ";"));
        doc.add(bullet("2. Zelar pelo bom uso e manutenção do equipamento, evitando danos "
                + "por uso inadequado ou negligência;"));
        doc.add(bullet("3. Comunicar imediatamente ao setor responsável qualquer problema "
                + "técnico, dano ou perda do equipamento;"));
        doc.add(bullet("4. Restituir o equipamento nas mesmas condições em que foi recebido, "
                + "salvo desgaste natural pelo uso regular, em caso de substituição, "
                + "encerramento do vínculo empregatício ou quando solicitado pela empresa."));

        doc.add(secao("3. Declaro que recebi e compreendi as orientações de uso do equipamento, "
                + "(incluindo manual de instruções) e/ou treinamento fornecido pela empresa."));
    }

    private String descricaoProduto() {
        return nvl(produto.marca()) + " " + nvl(produto.modelo());
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