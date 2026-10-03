package com.ossobo.gestaoDepIt.controlls.funcionario.funcionarioList;

import com.ossobo.gestaoDepIt.db.models.Funcionarios;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * FuncionarioFilters v1.0
 *
 * Responsabilidade: gerenciar UI e estado dos filtros de funcionários.
 *
 * Diferenças face ao CatalogFilters:
 *  - Filtros (nome, departamento, status, comFoto) são aplicados em memória
 *    sobre a lista carregada. Não há rota de "buscar/com-filtros" em
 *    FuncionariosRoutes.
 *  - O combo de departamento é populado a partir da própria lista carregada.
 *
 * @since v1.0
 */
public class FuncionarioFilters {

    private final FuncionarioState state;

    private TextField        filtroNomeField;
    private ComboBox<String> filtroDepartamentoCombo;
    private ComboBox<String> filtroStatusCombo;
    private ComboBox<String> filtroComFotoCombo;

    public FuncionarioFilters(FuncionarioState state) {
        this.state = state;
    }

    public void initializeUIComponents(
            TextField nomeField,
            ComboBox<String> departamentoCombo,
            ComboBox<String> statusCombo,
            ComboBox<String> comFotoCombo) {
        this.filtroNomeField = nomeField;
        this.filtroDepartamentoCombo = departamentoCombo;
        this.filtroStatusCombo = statusCombo;
        this.filtroComFotoCombo = comFotoCombo;
    }

    public void configureBasicFilters() {
        if (filtroStatusCombo != null) {
            filtroStatusCombo.getItems().setAll("Todos", "Ativos", "Inativos");
            filtroStatusCombo.setValue("Todos");
        }
        if (filtroComFotoCombo != null) {
            filtroComFotoCombo.getItems().setAll("Todos", "Com Foto", "Sem Foto");
            filtroComFotoCombo.setValue("Todos");
        }
        if (filtroDepartamentoCombo != null) {
            filtroDepartamentoCombo.setValue(null);
        }
    }

    /**
     * Popula o combo de departamentos com os distintos da lista carregada.
     * Chamado após cada carga de dados.
     */
    public void populateDepartamentos(List<Funcionarios> funcionarios) {
        if (filtroDepartamentoCombo == null || funcionarios == null) return;

        String selecionado = filtroDepartamentoCombo.getValue();

        Set<String> distintos = funcionarios.stream()
                .map(Funcionarios::departamento)
                .filter(d -> d != null && !d.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        filtroDepartamentoCombo.getItems().setAll(distintos);
        if (selecionado != null && distintos.contains(selecionado)) {
            filtroDepartamentoCombo.setValue(selecionado);
        } else {
            filtroDepartamentoCombo.setValue(null);
        }
    }

    public void applyFiltersFromUI() {
        state.setFiltroNome(
                filtroNomeField != null ? filtroNomeField.getText() : null);
        state.setFiltroDepartamento(
                filtroDepartamentoCombo != null ? filtroDepartamentoCombo.getValue() : null);
        state.setFiltroStatus(
                filtroStatusCombo != null ? filtroStatusCombo.getValue() : "Todos");
        state.setFiltroComFoto(
                filtroComFotoCombo != null ? filtroComFotoCombo.getValue() : "Todos");
    }

    public void clearFiltersUI() {
        if (filtroNomeField != null) filtroNomeField.clear();
        if (filtroDepartamentoCombo != null) filtroDepartamentoCombo.setValue(null);
        if (filtroStatusCombo != null) filtroStatusCombo.setValue("Todos");
        if (filtroComFotoCombo != null) filtroComFotoCombo.setValue("Todos");
        state.resetFilters();
    }

    /**
     * Aplica filtros do state sobre uma lista de funcionários (em memória).
     */
    public List<Funcionarios> applyFilters(List<Funcionarios> funcionarios) {
        if (funcionarios == null || funcionarios.isEmpty()) return funcionarios;

        return funcionarios.stream()
                .filter(this::passaNome)
                .filter(this::passaDepartamento)
                .filter(this::passaStatus)
                .filter(this::passaComFoto)
                .collect(Collectors.toList());
    }

    private boolean passaNome(Funcionarios f) {
        String filtro = state.getFiltroNome();
        if (filtro == null) return true;
        String nome = f.nome() != null ? f.nome().toLowerCase() : "";
        return nome.contains(filtro.toLowerCase());
    }

    private boolean passaDepartamento(Funcionarios f) {
        String filtro = state.getFiltroDepartamento();
        if (filtro == null) return true;
        return filtro.equals(f.departamento());
    }

    private boolean passaStatus(Funcionarios f) {
        String filtro = state.getFiltroStatus();
        if (filtro == null || "Todos".equals(filtro)) return true;
        boolean ativo = Boolean.TRUE.equals(f.ativo());
        return switch (filtro) {
            case "Ativos"   -> ativo;
            case "Inativos" -> !ativo;
            default         -> true;
        };
    }

    private boolean passaComFoto(Funcionarios f) {
        String filtro = state.getFiltroComFoto();
        if (filtro == null || "Todos".equals(filtro)) return true;
        boolean tem = f.temImagemPerfil();
        return switch (filtro) {
            case "Com Foto" -> tem;
            case "Sem Foto" -> !tem;
            default         -> true;
        };
    }

    public boolean hasActiveFilters() {
        return state.hasActiveFilters();
    }

    public void cleanup() {
        filtroNomeField = null;
        filtroDepartamentoCombo = null;
        filtroStatusCombo = null;
        filtroComFotoCombo = null;
    }
}