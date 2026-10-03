package com.ossobo.gestaoDepIt.db.services;

import com.ossobo.gestaoDepIt.db.relatorios.RelatorioResultado;
import com.ossobo.winterfx.anotations.Service;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * RelatorioExportService v1.0
 *
 * Gera arquivos .xlsx a partir de RelatorioResultado usando Apache POI.
 * Suporta:
 *   - 1 aba  (relatório simples / avançado)
 *   - N abas (exportação rápida multi-tabela do Dashboard)
 *
 * Sem estado — thread-safe (cada chamada cria o próprio Workbook).
 */
@Service
public class RelatorioExportService {

    private static final System.Logger logger =
            System.getLogger(RelatorioExportService.class.getName());

    private static final DateTimeFormatter FMT_TITULO =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    /** Envelope interno para exportação multi-aba. */
    public record AbaXlsx(String nome, RelatorioResultado resultado) {}

    // ============================================================
    // API
    // ============================================================

    /** Uma aba — o caso mais comum (Simples e Avançado). */
    public byte[] exportar(String tituloAba, RelatorioResultado r) throws IOException {
        return exportarMulti(List.of(new AbaXlsx(tituloAba, r)));
    }

    /** N abas — exportação rápida do Dashboard. */
    public byte[] exportarMulti(List<AbaXlsx> abas) throws IOException {
        if (abas == null || abas.isEmpty())
            throw new IllegalArgumentException("Nenhuma aba para exportar");

        try (XSSFWorkbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            CellStyle headerStyle = criarEstiloCabecalho(wb);

            for (AbaXlsx aba : abas) {
                String nome = sanitizarNomeAba(aba.nome());
                Sheet sheet = wb.createSheet(nome);
                preencherSheet(sheet, aba.resultado(), headerStyle);
            }

            wb.write(out);
            logger.log(System.Logger.Level.INFO,
                    "✅ Exportado XLSX com {0} aba(s)", abas.size());
            return out.toByteArray();
        }
    }

    /** Nome sugerido para o arquivo (usado pela UI ao salvar). */
    public String nomeArquivoSugerido(String prefixo) {
        return (prefixo == null || prefixo.isBlank() ? "relatorio" : prefixo)
                + "_" + LocalDateTime.now().format(FMT_TITULO) + ".xlsx";
    }

    // ============================================================
    // INTERNOS
    // ============================================================

    private void preencherSheet(Sheet sheet, RelatorioResultado r, CellStyle headerStyle) {
        List<String> colunas = r.colunas();
        if (colunas.isEmpty()) return;

        // Header
        Row header = sheet.createRow(0);
        for (int c = 0; c < colunas.size(); c++) {
            Cell cell = header.createCell(c);
            cell.setCellValue(formatarRotulo(colunas.get(c)));
            cell.setCellStyle(headerStyle);
        }

        // Linhas
        int rowIdx = 1;
        for (Map<String, Object> linha : r.linhas()) {
            Row row = sheet.createRow(rowIdx++);
            for (int c = 0; c < colunas.size(); c++) {
                Object valor = linha.get(colunas.get(c));
                Cell cell = row.createCell(c);
                escreverValor(cell, valor);
            }
        }

        // Largura automática (limite razoável)
        for (int c = 0; c < colunas.size(); c++) {
            sheet.autoSizeColumn(c);
            int largura = sheet.getColumnWidth(c);
            sheet.setColumnWidth(c, Math.min(largura + 512, 60 * 256));
        }
    }

    private CellStyle criarEstiloCabecalho(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.LEFT);
        return style;
    }

    /** "sku_produto" → "Sku Produto" (apenas para o cabeçalho). */
    private String formatarRotulo(String colunaFisica) {
        if (colunaFisica == null || colunaFisica.isBlank()) return "";
        String[] partes = colunaFisica.split("_");
        StringBuilder sb = new StringBuilder();
        for (String p : partes) {
            if (p.isEmpty()) continue;
            if (!sb.isEmpty()) sb.append(' ');
            sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1));
        }
        return sb.toString();
    }

    /** Suporta tipos comuns; resto vira toString(). */
    private void escreverValor(Cell cell, Object valor) {
        switch (valor) {
            case null -> cell.setBlank();
            case Number n -> cell.setCellValue(n.doubleValue());
            case Boolean b -> cell.setCellValue(b);
            case java.time.LocalDate d -> cell.setCellValue(d.toString());
            case java.time.LocalDateTime dt -> cell.setCellValue(dt.toString());
            case byte[] ignored -> cell.setCellValue("[binário]");
            default -> cell.setCellValue(valor.toString());
        }
    }

    /** POI proíbe "[]:*?/\\" e nomes > 31 chars em nomes de aba. */
    private String sanitizarNomeAba(String bruto) {
        if (bruto == null || bruto.isBlank()) return "Dados";
        String limpo = bruto.replaceAll("[\\[\\]:*?/\\\\]", "_").trim();
        return limpo.length() > 31 ? limpo.substring(0, 31) : limpo;
    }
}