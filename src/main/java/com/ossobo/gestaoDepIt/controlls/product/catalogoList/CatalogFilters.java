package com.ossobo.gestaoDepIt.controlls.product.catalogoList;

import com.ossobo.gestaoDepIt.db.models.CatalogoProdutos;
import com.ossobo.gestaoDepIt.db.services.CatalogoProdutosService;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Gerencia a UI e o estado dos filtros do catálogo.
 * v1.1 - Correção: applyAdditionalFilters não modifica lista original.
 * Responsabilidade única: Gerenciar componentes de filtro e aplicar regras de filtragem.
 */
public class CatalogFilters {

    private final CatalogoProdutosService produtosService;
    private final CatalogState state;

    // Componentes UI
    private ComboBox<String> filtroTipoCombo;
    private TextField filtroCategoriaField;
    private TextField filtroMarcaField;
    private TextField filtroModeloField;
    private ComboBox<String> filtroEstoqueCombo;
    private ComboBox<String> filtroStatusCombo;

    public CatalogFilters(CatalogoProdutosService produtosService, CatalogState state) {
        this.produtosService = produtosService;
        this.state = state;
    }

    /**
     * Injeta referências dos componentes FXML.
     */
    public void initializeUIComponents(
            ComboBox<String> tipoCombo,
            TextField categoriaField,
            TextField marcaField,
            TextField modeloField,
            ComboBox<String> estoqueCombo,
            ComboBox<String> statusCombo) {
        this.filtroTipoCombo = tipoCombo;
        this.filtroCategoriaField = categoriaField;
        this.filtroMarcaField = marcaField;
        this.filtroModeloField = modeloField;
        this.filtroEstoqueCombo = estoqueCombo;
        this.filtroStatusCombo = statusCombo;
    }

    /**
     * Popula os ComboBox com opções padrão.
     */
    public void configureBasicFilters() {
        if (filtroEstoqueCombo != null) {
            filtroEstoqueCombo.getItems().setAll("Todos", "Com Estoque", "Sem Estoque", "Estoque Baixo");
            filtroEstoqueCombo.setValue("Todos");
        }

        if (filtroStatusCombo != null) {
            filtroStatusCombo.getItems().setAll("Todos", "Ativos", "Inativos");
            filtroStatusCombo.setValue("Todos");
        }
    }

    /**
     * Lê os valores da UI e atualiza o state.
     */
    public void applyFiltersFromUI() {
        state.setFiltroTipo(
                filtroTipoCombo != null ? filtroTipoCombo.getValue() : null);
        state.setFiltroCategoria(
                filtroCategoriaField != null ? filtroCategoriaField.getText() : null);
        state.setFiltroMarca(
                filtroMarcaField != null ? filtroMarcaField.getText() : null);
        state.setFiltroModelo(
                filtroModeloField != null ? filtroModeloField.getText() : null);
        state.setFiltroEstoque(
                filtroEstoqueCombo != null ? filtroEstoqueCombo.getValue() : "Todos");
        state.setFiltroStatus(
                filtroStatusCombo != null ? filtroStatusCombo.getValue() : "Todos");
    }

    /**
     * Limpa todos os campos de filtro na UI e no state.
     */
    public void clearFiltersUI() {
        if (filtroTipoCombo != null) filtroTipoCombo.setValue(null);
        if (filtroCategoriaField != null) filtroCategoriaField.clear();
        if (filtroMarcaField != null) filtroMarcaField.clear();
        if (filtroModeloField != null) filtroModeloField.clear();
        if (filtroEstoqueCombo != null) filtroEstoqueCombo.setValue("Todos");
        if (filtroStatusCombo != null) filtroStatusCombo.setValue("Todos");
        state.resetFilters();
    }

    /**
     * Aplica filtros do state sobre uma lista de produtos.
     * v1.1: Não modifica a lista original - retorna nova lista filtrada.
     */
    public List<CatalogoProdutos> applyFilters(List<CatalogoProdutos> produtos) {
        if (produtos == null || produtos.isEmpty()) {
            return produtos;
        }

        // 1. Aplica filtro de estoque (em memória)
        List<CatalogoProdutos> resultado = applyFiltroEstoque(produtos);

        return resultado;
    }

    /**
     * Filtra por estoque sem modificar a lista original.
     * ✅ v1.1: Usa stream().filter() em vez de removeIf().
     */
    private List<CatalogoProdutos> applyFiltroEstoque(List<CatalogoProdutos> produtos) {
        String filtro = state.getFiltroEstoque();
        if (filtro == null || "Todos".equals(filtro)) {
            return List.copyOf(produtos); // Cópia imutável
        }

        return produtos.stream()
                .filter(p -> {
                    Integer estoque = p.totalRecebido();
                    if (estoque == null) estoque = 0;

                    return switch (filtro) {
                        case "Com Estoque" -> estoque > 0;
                        case "Sem Estoque" -> estoque == 0;
                        case "Estoque Baixo" -> estoque > 0 && estoque <= 5;
                        default -> true;
                    };
                })
                .collect(Collectors.toList());
    }

    /**
     * Converte filtro de status da UI para Boolean.
     */
    public Boolean getStatusFiltrado() {
        String status = state.getFiltroStatus();
        if (status == null) return null;
        return switch (status) {
            case "Ativos" -> true;
            case "Inativos" -> false;
            default -> null;
        };
    }

    /**
     * Verifica se há filtros ativos.
     */
    public boolean hasActiveFilters() {
        return state.hasActiveFilters();
    }

    /**
     * Limpa recursos.
     */
    public void cleanup() {
        filtroTipoCombo = null;
        filtroCategoriaField = null;
        filtroMarcaField = null;
        filtroModeloField = null;
        filtroEstoqueCombo = null;
        filtroStatusCombo = null;
    }
}