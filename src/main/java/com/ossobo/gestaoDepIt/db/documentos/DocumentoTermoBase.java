package com.ossobo.gestaoDepIt.db.documentos;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * DocumentoTermoBase v1.0
 *
 * Responsabilidade: prover o layout comum dos termos (cabeçalho, título,
 * subtítulo, empresa, bloco de assinaturas e rodapé) para as subclasses
 * TermoEntregaGenerator e TermoDevolucaoGenerator.
 *
 * Decisão: sem iText 7 (AGPL). Usa OpenPDF (LGPL/MPL), API próxima do iText 2.
 *
 * TODO: externalizar EMPRESA / RESP_TI / CIDADE em application.properties.
 *
 * @since v1.0
 */
public abstract class DocumentoTermoBase {

    protected static final String EMPRESA = "AGRIPALMA";
    protected static final String RESP_TI = "Rafael Tavares";
    protected static final String CIDADE = "São Tomé, Ribeira Peixe";

    protected static final DateTimeFormatter DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    protected static final Font FONT_TITULO   = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
    protected static final Font FONT_SUBTITULO= FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 11);
    protected static final Font FONT_SECAO    = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
    protected static final Font FONT_TEXTO    = FontFactory.getFont(FontFactory.HELVETICA, 11);
    protected static final Font FONT_RODAPE   = FontFactory.getFont(FontFactory.HELVETICA, 10);

    /** Gera o PDF completo. Subclasses implementam apenas o corpo específico. */
    public byte[] gerar() {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document();
            PdfWriter.getInstance(doc, out);
            doc.open();

            escreverCabecalho(doc);
            escreverCorpo(doc);
            escreverAssinaturas(doc);
            escreverRodape(doc);

            doc.close();
            return out.toByteArray();
        } catch (DocumentException | java.io.IOException e) {
            throw new IllegalStateException("Falha ao gerar PDF: " + e.getMessage(), e);
        }
    }

    /** Corpo específico de cada termo. */
    protected abstract void escreverCorpo(Document doc) throws DocumentException;

    /** Título exibido no topo (ex.: "TERMO DE RESPONSABILIDADE ..."). */
    protected abstract String titulo();

    /** Subtítulo exibido abaixo do título. */
    protected abstract String subtitulo();

    // ============================================================
    // BLOCOS COMUNS
    // ============================================================

    private void escreverCabecalho(Document doc) throws DocumentException {
        Paragraph t = new Paragraph(titulo(), FONT_TITULO);
        t.setAlignment(Element.ALIGN_CENTER);
        doc.add(t);

        Paragraph s = new Paragraph(subtitulo(), FONT_SUBTITULO);
        s.setAlignment(Element.ALIGN_CENTER);
        s.setSpacingAfter(14f);
        doc.add(s);

        Paragraph empresa = new Paragraph("EMPRESA: " + EMPRESA, FONT_SECAO);
        empresa.setSpacingAfter(14f);
        doc.add(empresa);
    }

    private void escreverAssinaturas(Document doc) throws DocumentException {
        String data = LocalDate.now().format(DATA_BR);

        doc.add(new Paragraph(" "));
        Paragraph local = new Paragraph(CIDADE + ", " + data, FONT_TEXTO);
        local.setSpacingBefore(20f);
        doc.add(local);

        doc.add(new Paragraph(" "));
        doc.add(new Paragraph("_" .repeat(50), FONT_TEXTO));
        doc.add(new Paragraph("Utilizador", FONT_RODAPE));
        doc.add(new Paragraph(" "));
        doc.add(new Paragraph("_" .repeat(50), FONT_TEXTO));
        doc.add(new Paragraph("Responsável de T.I — " + RESP_TI, FONT_RODAPE));
        doc.add(new Paragraph(" "));
        doc.add(new Paragraph("_" .repeat(50), FONT_TEXTO));
        doc.add(new Paragraph("Visto responsável do Departamento", FONT_RODAPE));
        doc.add(new Paragraph(" "));
        doc.add(new Paragraph("_" .repeat(50), FONT_TEXTO));
        doc.add(new Paragraph("Visto Diretor Geral", FONT_RODAPE));
    }

    private void escreverRodape(Document doc) throws DocumentException {
        String data = LocalDate.now().format(DATA_BR);
        Paragraph r = new Paragraph(
                "Empresa " + EMPRESA + ", " + CIDADE + " aos " + data, FONT_RODAPE);
        r.setSpacingBefore(24f);
        r.setAlignment(Element.ALIGN_CENTER);
        doc.add(r);
    }
}