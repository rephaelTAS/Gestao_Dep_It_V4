package com.ossobo.gestaoDepIt.controlls.relatorio.avancado;

import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * AvancadoTableManager v1.0
 *
 * Duas responsabilidades coesas:
 *   1) Popular VBox com CheckBox por coluna (editor Lego).
 *   2) Reconstruir TableColumns dinâmicas do TableView de resultado.
 */
public final class AvancadoTableManager {

    private AvancadoTableManager() {}

    // ============================================================
    // EDITOR LEGO — CheckBox por coluna
    // ============================================================

    /**
     * Popula um VBox com CheckBox para cada coluna.
     * @param container       VBox destino
     * @param colunas         colunas disponíveis (nomes físicos)
     * @param marcadasIniciais  colunas que já vêm marcadas
     * @return                Set interno que reflete as marcações atuais
     */
    public static Set<String> popularCheckboxesColunas(VBox container,
                                                       List<String> colunas,
                                                       Set<String> marcadasIniciais) {
        Set<String> marcadas = new LinkedHashSet<>();
        if (container == null) return marcadas;
        container.getChildren().clear();
        if (colunas == null) return marcadas;

        Set<String> iniciais = marcadasIniciais == null ? Set.of() : marcadasIniciais;
        for (String col : colunas) {
            CheckBox cb = new CheckBox(col);
            cb.setSelected(iniciais.contains(col));
            if (cb.isSelected()) marcadas.add(col);
            cb.selectedProperty().addListener((obs, ov, nv) -> {
                if (nv) marcadas.add(col); else marcadas.remove(col);
            });
            container.getChildren().add(cb);
        }
        return marcadas;
    }

    /** Marca todas as CheckBox de um VBox. */
    public static void marcarTodos(VBox container, boolean marcar) {
        if (container == null) return;
        container.getChildren().forEach(n -> {
            if (n instanceof CheckBox cb) cb.setSelected(marcar);
        });
    }

    // ============================================================
    // TABELA DE RESULTADO
    // ============================================================

    public static void aplicarColunasResultado(TableView<Map<String, Object>> tabela,
                                               List<String> colunas) {
        if (tabela == null) return;
        tabela.getColumns().clear();
        if (colunas == null) return;
        for (String col : colunas) {
            TableColumn<Map<String, Object>, Object> tc = new TableColumn<>(formatar(col));
            tc.setCellValueFactory(data ->
                    new SimpleObjectProperty<>(data.getValue().get(col)));
            tc.setPrefWidth(160);
            tabela.getColumns().add(tc);
        }
    }

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