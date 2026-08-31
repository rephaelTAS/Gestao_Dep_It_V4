package com.ossobo.gestaoDepIt.controllers.gestao.equipamentos.inventario.inventarioList;

import com.ossobo.gestaoDepIt.config.AppImageConfig;
import com.ossobo.gestaoDepIt.db.models.InventarioEquipamentos;
import com.ossobo.gestaoDepIt.db.services.InventarioEquipamentosService;
import com.ossobo.winterfx.router.Rotas;
import com.ossobo.winterfx.router.model.Params;
import com.ossobo.winterfx.router.model.ResponseData;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Gerencia a UI e lógica de filtros do inventário.
 * Responsabilidade única: Popular combos, ler/escrever filtros no state.
 */
public class InventoryFilters {

    private final InventoryState state;
    private final InventarioEquipamentosService service;

    private ComboBox<String> filtroStatusCombo;
    private ComboBox<String> filtroCondicaoCombo;
    private ComboBox<String> filtroLocalizacaoCombo;
    private ComboBox<String> filtroDepartamentoCombo;
    private TextField filtroMacField;
    private TextField filtroBuscaField;
    private DatePicker filtroAquisicaoInicio;
    private DatePicker filtroAquisicaoFim;
    private DatePicker filtroInstalacaoInicio;
    private DatePicker filtroInstalacaoFim;
    private DatePicker filtroVerificacaoInicio;
    private DatePicker filtroVerificacaoFim;

    public InventoryFilters(InventoryState state, InventarioEquipamentosService service) {
        this.state = state;
        this.service = service;
    }

    public void initializeUIComponents(
            ComboBox<String> status, ComboBox<String> condicao,
            ComboBox<String> localizacao, ComboBox<String> departamento,
            TextField mac, TextField busca,
            DatePicker aquisicaoInicio, DatePicker aquisicaoFim,
            DatePicker instalacaoInicio, DatePicker instalacaoFim,
            DatePicker verificacaoInicio, DatePicker verificacaoFim) {
        this.filtroStatusCombo = status;
        this.filtroCondicaoCombo = condicao;
        this.filtroLocalizacaoCombo = localizacao;
        this.filtroDepartamentoCombo = departamento;
        this.filtroMacField = mac;
        this.filtroBuscaField = busca;
        this.filtroAquisicaoInicio = aquisicaoInicio;
        this.filtroAquisicaoFim = aquisicaoFim;
        this.filtroInstalacaoInicio = instalacaoInicio;
        this.filtroInstalacaoFim = instalacaoFim;
        this.filtroVerificacaoInicio = verificacaoInicio;
        this.filtroVerificacaoFim = verificacaoFim;
    }

    public void configureBasicFilters() {
        if (filtroStatusCombo != null) {
            filtroStatusCombo.getItems().setAll("ATIVO", "MANUTENCAO", "BAIXADO", "RESERVA", "EM_USO");
        }
        if (filtroCondicaoCombo != null) {
            filtroCondicaoCombo.getItems().setAll("OTIMO", "BOM", "REGULAR", "CRITICO");
        }
        carregarLocalizacoesDepartamentos();
    }

    private void carregarLocalizacoesDepartamentos() {
        try {
            var re = (ResponseData) Rotas.get("inventario-equipamentos/servicepor/localizacao");
            Map<String,Object> dados = re.getData();
            List<String> localizacoes = extrairValoresDistintos(re, "equipamentos",
                    InventarioEquipamentos::localizacao);
            var dep = (ResponseData) Rotas.get("inventario-equipamentos/servicepor/departamento");
            Map<String,Object> dadosDep = dep.getData();
            List<InventarioEquipamentos> departamentos = (List<InventarioEquipamentos>) dadosDep.get("equipamento");

            if (filtroLocalizacaoCombo != null) {
                filtroLocalizacaoCombo.getItems().setAll(String.valueOf(localizacoes));
            }
            if (filtroDepartamentoCombo != null) {
                filtroDepartamentoCombo.getItems().setAll(String.valueOf(departamentos));
            }
        } catch (Exception e) {
            // Silencioso - filtros funcionam sem localizações/departamentos
        }
    }

    public void applyFiltersFromUI() {
        state.setFiltroStatus(
                filtroStatusCombo != null ? filtroStatusCombo.getValue() : null);
        state.setFiltroCondicao(
                filtroCondicaoCombo != null ? filtroCondicaoCombo.getValue() : null);
        state.setFiltroLocalizacao(
                filtroLocalizacaoCombo != null ? filtroLocalizacaoCombo.getValue() : null);
        state.setFiltroDepartamento(
                filtroDepartamentoCombo != null ? filtroDepartamentoCombo.getValue() : null);
        state.setFiltroMac(
                filtroMacField != null && !filtroMacField.getText().isEmpty()
                        ? filtroMacField.getText() : null);
        state.setFiltroBusca(
                filtroBuscaField != null && !filtroBuscaField.getText().isEmpty()
                        ? filtroBuscaField.getText() : null);
        state.setFiltroAquisicaoInicio(
                filtroAquisicaoInicio != null ? filtroAquisicaoInicio.getValue() : null);
        state.setFiltroAquisicaoFim(
                filtroAquisicaoFim != null ? filtroAquisicaoFim.getValue() : null);
        state.setFiltroInstalacaoInicio(
                filtroInstalacaoInicio != null ? filtroInstalacaoInicio.getValue() : null);
        state.setFiltroInstalacaoFim(
                filtroInstalacaoFim != null ? filtroInstalacaoFim.getValue() : null);
        state.setFiltroVerificacaoInicio(
                filtroVerificacaoInicio != null ? filtroVerificacaoInicio.getValue() : null);
        state.setFiltroVerificacaoFim(
                filtroVerificacaoFim != null ? filtroVerificacaoFim.getValue() : null);
    }

    public void clearFiltersUI() {
        if (filtroStatusCombo != null) filtroStatusCombo.setValue(null);
        if (filtroCondicaoCombo != null) filtroCondicaoCombo.setValue(null);
        if (filtroLocalizacaoCombo != null) filtroLocalizacaoCombo.setValue(null);
        if (filtroDepartamentoCombo != null) filtroDepartamentoCombo.setValue(null);
        if (filtroMacField != null) filtroMacField.clear();
        if (filtroBuscaField != null) filtroBuscaField.clear();
        if (filtroAquisicaoInicio != null) filtroAquisicaoInicio.setValue(null);
        if (filtroAquisicaoFim != null) filtroAquisicaoFim.setValue(null);
        if (filtroInstalacaoInicio != null) filtroInstalacaoInicio.setValue(null);
        if (filtroInstalacaoFim != null) filtroInstalacaoFim.setValue(null);
        if (filtroVerificacaoInicio != null) filtroVerificacaoInicio.setValue(null);
        if (filtroVerificacaoFim != null) filtroVerificacaoFim.setValue(null);
        state.resetFilters();
    }

    /**
     * Extrai a lista do envelope e projeta em strings distintas e ordenadas.
     * Centraliza o cast unchecked num único ponto auditável (padrão da #60).
     */
    private List<String> extrairValoresDistintos(ResponseData resposta, String chave,
                                                 Function<InventarioEquipamentos, String> campo) {
        Map<String, Object> dados = resposta.getData();
        // Fail-fast: rota errada / chave errada / envelope nulo = erro EXPLÍCITO aqui
        if (dados == null || !(dados.get(chave) instanceof List<?> lista)) {
            throw new IllegalStateException("Envelope inesperado — chave '" + chave
                    + "' ausente. Recebido: "
                    + (dados == null ? "getData() nulo" : dados.keySet()));
        }
        @SuppressWarnings("unchecked")   // erasure: seguro por contrato da fronteira
        List<InventarioEquipamentos> equipamentos = (List<InventarioEquipamentos>) lista;

        return equipamentos.stream()
                .map(campo)
                .filter(Objects::nonNull)   // S8/S9/S10: campos nullable nos 158 registros reais
                .distinct()
                .sorted()                   // UX: ordem previsível no dropdown
                .toList();
    }

    public void cleanup() {}
}