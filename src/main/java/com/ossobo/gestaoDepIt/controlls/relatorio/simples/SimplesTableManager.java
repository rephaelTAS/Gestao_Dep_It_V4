package com.ossobo.gestaoDepIt.controlls.relatorio.simples;

import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.List;
import java.util.Map;

/**
 * SimplesTableManager v1.0
 *
 * Reconstrói as TableColumns dinamicamente a partir da lista de colunas
 * devolvida pelo Builder. Uma coluna = uma chave do Map<coluna, valor>.
 */
public final class SimplesTableManager {

    private SimplesTableManager() {}

    public static void aplicar(TableView<Map<String, Object>> tabela,
                               List<String> colunas) {
        if (tabela == null) return;
        tabela.getColumns().clear();

        for (String col : colunas) {
            TableColumn<Map<String, Object>, Object> tc = new TableColumn<>(formatar(col));
            tc.setCellValueFactory(data ->
                    new SimpleObjectProperty<>(data.getValue().get(col)));
            tc.setPrefWidth(160);
            tabela.getColumns().add(tc);
        }
    }

    /** "sku_produto" → "Sku Produto" (só para o cabeçalho). */
    private static String formatar(String coluna) {
        if (coluna == null || coluna.isBlank()) return "";
        String[] partes = coluna.split("_");
        StringBuilder sb = new StringBuilder();
        for (String p : partes) {
            if (p.isEmpty()) continue;
            if (!sb.isEmpty()) sb.append(' ');
            sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1));
        }
        return sb.toString();
    }
}