package com.ossobo.gestaoDepIt.controllers.gestao.relatorios;

import com.ossobo.gestaoDepIt.db.models.CatalogoProdutos;
import com.ossobo.gestaoDepIt.db.models.EstoqueMovimentacoes;
import com.ossobo.gestaoDepIt.db.models.Funcionarios;
import com.ossobo.gestaoDepIt.db.models.InventarioEquipamentos;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class ExportarExcelService {

    private static final Logger logger = LoggerFactory.getLogger(ExportarExcelService.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static void exportarInventarioCompleto(String caminho,
                                                  List<InventarioEquipamentos> equipamentos,
                                                  List<CatalogoProdutos> catalogos,
                                                  List<Funcionarios> funcionarios) throws IOException {
        logger.info("Exportando inventário para Excel: {}", caminho);

        try (Workbook workbook = new XSSFWorkbook()) {
            // ===== ABA 1: INVENTÁRIO =====
            Sheet sheetInv = workbook.createSheet("Inventário");
            criarCabecalho(workbook, sheetInv, new String[]{
                    "ID", "SKU", "Nº Série", "MAC", "Funcionário ID", "Nome Funcionário",
                    "Departamento", "Localização", "Status", "Condição",
                    "Data Aquisição", "Data Instalação", "Última Verificação", "Observações"
            });

            CellStyle styleData = workbook.createCellStyle();
            styleData.setDataFormat(workbook.createDataFormat().getFormat("dd/mm/yyyy"));

            int rowIdx = 1;
            for (InventarioEquipamentos eq : equipamentos) {
                Row row = sheetInv.createRow(rowIdx++);
                row.createCell(0).setCellValue(eq.getId() != null ? eq.getId() : 0);
                row.createCell(1).setCellValue(nvl(eq.getSkuProduto()));
                row.createCell(2).setCellValue(nvl(eq.getNumSerie()));
                row.createCell(3).setCellValue(nvl(eq.getEnderecoMac()));
                row.createCell(4).setCellValue(nvl(eq.getFuncionarioId()));

                String nomeFunc = funcionarios.stream()
                        .filter(f -> f.getCodDep().equals(eq.getFuncionarioId()))
                        .findFirst().map(Funcionarios::getNome).orElse("-");
                row.createCell(5).setCellValue(nomeFunc);

                row.createCell(6).setCellValue(nvl(eq.getDepartamento()));
                row.createCell(7).setCellValue(nvl(eq.getLocalizacao()));
                row.createCell(8).setCellValue(nvl(eq.getStatus()));
                row.createCell(9).setCellValue(nvl(eq.getCondicao()));

                if (eq.getDataAquisicao() != null) {
                    Cell cell = row.createCell(10);
                    cell.setCellValue(eq.getDataAquisicao().format(DATE_FMT));
                    cell.setCellStyle(styleData);
                }
                if (eq.getDataInstalacao() != null) {
                    Cell cell = row.createCell(11);
                    cell.setCellValue(eq.getDataInstalacao().format(DATE_FMT));
                    cell.setCellStyle(styleData);
                }
                if (eq.getDataUltimaVerificacao() != null) {
                    Cell cell = row.createCell(12);
                    cell.setCellValue(eq.getDataUltimaVerificacao().format(DATE_FMT));
                    cell.setCellStyle(styleData);
                }
                row.createCell(13).setCellValue(nvl(eq.getObservacoes()));
            }
            autoSizeColumns(sheetInv, 14);

            // ===== ABA 2: CATÁLOGO =====
            Sheet sheetCat = workbook.createSheet("Catálogo");
            criarCabecalho(workbook, sheetCat, new String[]{
                    "SKU", "Tipo", "Categoria", "Marca", "Modelo", "Cor",
                    "Preço Unitário", "Estoque", "Ativo"
            });
            int rowCat = 1;
            for (CatalogoProdutos cp : catalogos) {
                Row row = sheetCat.createRow(rowCat++);
                row.createCell(0).setCellValue(nvl(cp.getSku()));
                row.createCell(1).setCellValue(cp.getTipoProduto() != null ? cp.getTipoProduto().toString() : "-");
                row.createCell(2).setCellValue(nvl(cp.getCategoria()));
                row.createCell(3).setCellValue(nvl(cp.getMarca()));
                row.createCell(4).setCellValue(nvl(cp.getModelo()));
                row.createCell(5).setCellValue(nvl(cp.getCor()));
                if (cp.getPrecoUnitario() != null) row.createCell(6).setCellValue(cp.getPrecoUnitario().doubleValue());
                row.createCell(7).setCellValue(cp.getTotalRecebido() != null ? cp.getTotalRecebido() : 0);
                row.createCell(8).setCellValue(Boolean.TRUE.equals(cp.getAtivo()) ? "Sim" : "Não");
            }
            autoSizeColumns(sheetCat, 9);

            // ===== ABA 3: FUNCIONÁRIOS =====
            Sheet sheetFunc = workbook.createSheet("Funcionários");
            criarCabecalho(workbook, sheetFunc, new String[]{
                    "Código", "Nome", "Função", "Departamento", "Local Trabalho",
                    "Email", "Telefone", "Ativo"
            });
            int rowFunc = 1;
            for (Funcionarios f : funcionarios) {
                Row row = sheetFunc.createRow(rowFunc++);
                row.createCell(0).setCellValue(nvl(f.getCodDep()));
                row.createCell(1).setCellValue(nvl(f.getNome()));
                row.createCell(2).setCellValue(nvl(f.getFuncao()));
                row.createCell(3).setCellValue(nvl(f.getDepartamento()));
                row.createCell(4).setCellValue(nvl(f.getLocalTrabalho()));
                row.createCell(5).setCellValue(nvl(f.getEmail()));
                row.createCell(6).setCellValue(nvl(f.getTelefone()));
                row.createCell(7).setCellValue(Boolean.TRUE.equals(f.getAtivo()) ? "Sim" : "Não");
            }
            autoSizeColumns(sheetFunc, 8);

            // ===== SALVAR =====
            try (FileOutputStream fos = new FileOutputStream(caminho)) {
                workbook.write(fos);
            }
            logger.info("Exportação concluída: {} inv, {} cat, {} func",
                    equipamentos.size(), catalogos.size(), funcionarios.size());
        }
    }

    private static void criarCabecalho(Workbook workbook, Sheet sheet, String[] colunas) {
        CellStyle styleHeader = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        styleHeader.setFont(font);
        styleHeader.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        styleHeader.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styleHeader.setAlignment(HorizontalAlignment.CENTER);

        Row header = sheet.createRow(0);
        for (int i = 0; i < colunas.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(colunas[i]);
            cell.setCellStyle(styleHeader);
        }
    }

    public static void exportarDadosCombinados(String caminho, List<String> colunas, List<Map<String, String>> dados) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Relatório Avançado");
            criarCabecalho(workbook, sheet, colunas.toArray(new String[0]));
            int rowIdx = 1;
            for (Map<String, String> linha : dados) {
                Row row = sheet.createRow(rowIdx++);
                for (int i = 0; i < colunas.size(); i++) {
                    row.createCell(i).setCellValue(linha.getOrDefault(colunas.get(i), "-"));
                }
            }
            autoSizeColumns(sheet, colunas.size());
            try (FileOutputStream fos = new FileOutputStream(caminho)) { workbook.write(fos); }
        }
    }

    // No ExportarExcelService.java, adicionar:

    public static void exportarCatalogo(String caminho, List<CatalogoProdutos> catalogos) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Catálogo");
            criarCabecalho(workbook, sheet, new String[]{"SKU","Tipo","Categoria","Marca","Modelo","Cor","Preço Unitário","Estoque","Ativo"});
            int rowIdx = 1;
            for (CatalogoProdutos cp : catalogos) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(nvl(cp.getSku()));
                row.createCell(1).setCellValue(cp.getTipoProduto() != null ? cp.getTipoProduto().toString() : "-");
                row.createCell(2).setCellValue(nvl(cp.getCategoria()));
                row.createCell(3).setCellValue(nvl(cp.getMarca()));
                row.createCell(4).setCellValue(nvl(cp.getModelo()));
                row.createCell(5).setCellValue(nvl(cp.getCor()));
                if (cp.getPrecoUnitario() != null) row.createCell(6).setCellValue(cp.getPrecoUnitario().doubleValue());
                row.createCell(7).setCellValue(cp.getTotalRecebido() != null ? cp.getTotalRecebido() : 0);
                row.createCell(8).setCellValue(Boolean.TRUE.equals(cp.getAtivo()) ? "Sim" : "Não");
            }
            autoSizeColumns(sheet, 9);
            try (FileOutputStream fos = new FileOutputStream(caminho)) { workbook.write(fos); }
        }
    }

    public static void exportarFuncionarios(String caminho, List<Funcionarios> funcionarios) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Funcionários");
            criarCabecalho(workbook, sheet, new String[]{"Código","Nome","Função","Departamento","Local Trabalho","Email","Telefone","Ativo"});
            int rowIdx = 1;
            for (Funcionarios f : funcionarios) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(nvl(f.getCodDep()));
                row.createCell(1).setCellValue(nvl(f.getNome()));
                row.createCell(2).setCellValue(nvl(f.getFuncao()));
                row.createCell(3).setCellValue(nvl(f.getDepartamento()));
                row.createCell(4).setCellValue(nvl(f.getLocalTrabalho()));
                row.createCell(5).setCellValue(nvl(f.getEmail()));
                row.createCell(6).setCellValue(nvl(f.getTelefone()));
                row.createCell(7).setCellValue(Boolean.TRUE.equals(f.getAtivo()) ? "Sim" : "Não");
            }
            autoSizeColumns(sheet, 8);
            try (FileOutputStream fos = new FileOutputStream(caminho)) { workbook.write(fos); }
        }
    }

    public static void exportarMovimentacoes(String caminho, List<EstoqueMovimentacoes> movimentacoes) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Movimentações");
            criarCabecalho(workbook, sheet, new String[]{"ID","SKU","Tipo","Quantidade","Lote","Data","Localização","Funcionário","Motivo"});
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            int rowIdx = 1;
            for (EstoqueMovimentacoes m : movimentacoes) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(m.getId() != null ? m.getId() : 0);
                row.createCell(1).setCellValue(nvl(m.getSkuProduto()));
                row.createCell(2).setCellValue(m.getTipoMovimentacao() != null ? m.getTipoMovimentacao().toString() : "-");
                row.createCell(3).setCellValue(m.getQuantidade() != null ? m.getQuantidade() : 0);
                row.createCell(4).setCellValue(nvl(m.getLote()));
                row.createCell(5).setCellValue(m.getDataMovimentacao() != null ? m.getDataMovimentacao().format(dtf) : "-");
                row.createCell(6).setCellValue(nvl(m.getLocalizacao()));
                row.createCell(7).setCellValue(nvl(m.getCodDepFuncionario()));
                row.createCell(8).setCellValue(nvl(m.getMotivo()));
            }
            autoSizeColumns(sheet, 9);
            try (FileOutputStream fos = new FileOutputStream(caminho)) { workbook.write(fos); }
        }
    }

    private static void autoSizeColumns(Sheet sheet, int numColunas) {
        for (int i = 0; i < numColunas; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private static String nvl(String s) { return s != null && !s.isEmpty() ? s : "-"; }
}