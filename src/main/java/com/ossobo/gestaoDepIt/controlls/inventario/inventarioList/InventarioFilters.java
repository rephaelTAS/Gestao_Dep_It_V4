package com.ossobo.gestaoDepIt.controlls.inventario.inventarioList;

import com.ossobo.gestaoDepIt.db.enums.CondicaoEquipamento;
import com.ossobo.gestaoDepIt.db.enums.StatusEquipamento;
import com.ossobo.winterfx.router.Rotas;
import com.ossobo.winterfx.router.model.Params;
import com.ossobo.winterfx.router.model.ResponseData;
import javafx.application.Platform;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * InventarioFilters v1.0
 *
 * Responsabilidade única: Traduzir UI de filtros ↔ Params da rota
 * e popular os combos de domínio via rota.
 *
 * Sem service injetado — todos os valores de domínio vêm de rotas de consulta:
 *   /status-validos, /condicoes-validas, /distinct/localizacoes, /distinct/departamentos
 *
 * Filtros de instalação/verificação: UI mantida, mas ainda NÃO enviados para a
 * rota /com-filtros (back-end só aceita início/fim de aquisição + mac).
 * TODO: quando a rota aceitar os 4 parâmetros adicionais, ligar no paramsDeFiltros().
 *
 * @since v1.0
 */
public class InventarioFilters {

    private static final String ROTAS = "inventario-equipamentos/service";

    // ===== FXML — FILTROS =====
    private TextField        filtroMac;
    private ComboBox<String> filtroStatus;
    private ComboBox<String> filtroCondicao;
    private ComboBox<String> filtroLocalizacao;
    private ComboBox<String> filtroDepartamento;
    private TextField        filtroBusca;
    private DatePicker       filtroAquisicaoInicio;
    private DatePicker       filtroAquisicaoFim;
    private DatePicker       filtroInstalacaoInicio;
    private DatePicker       filtroInstalacaoFim;
    private DatePicker       filtroVerificacaoInicio;
    private DatePicker       filtroVerificacaoFim;

    // ===== INICIALIZAÇÃO UI =====
    public void initializeUIComponents(
            TextField mac,
            ComboBox<String> status,
            ComboBox<String> condicao,
            ComboBox<String> localizacao,
            ComboBox<String> departamento,
            TextField busca,
            DatePicker aquisicaoInicio, DatePicker aquisicaoFim,
            DatePicker instalacaoInicio, DatePicker instalacaoFim,
            DatePicker verificacaoInicio, DatePicker verificacaoFim) {
        this.filtroMac = mac;
        this.filtroStatus = status;
        this.filtroCondicao = condicao;
        this.filtroLocalizacao = localizacao;
        this.filtroDepartamento = departamento;
        this.filtroBusca = busca;
        this.filtroAquisicaoInicio = aquisicaoInicio;
        this.filtroAquisicaoFim = aquisicaoFim;
        this.filtroInstalacaoInicio = instalacaoInicio;
        this.filtroInstalacaoFim = instalacaoFim;
        this.filtroVerificacaoInicio = verificacaoInicio;
        this.filtroVerificacaoFim = verificacaoFim;
    }

    // ===== CONFIGURAÇÃO DOS COMBOS =====
    public void configureBasicFilters() {
        if (filtroStatus != null) {
            filtroStatus.getItems().setAll(StatusEquipamento.todos());
        }
        if (filtroCondicao != null) {
            filtroCondicao.getItems().setAll(CondicaoEquipamento.todos());
        }
        carregarLocalizacoes();
        carregarDepartamentos();
    }

    private void carregarLocalizacoes() {
        if (filtroLocalizacao == null) return;
        CompletableFuture.runAsync(() -> {
            var resp = Rotas.get(ROTAS + "/distinct/localizacoes");
            if (!resp.isSuccess()) return;
            List<String> valores = resp.getDataList("valores");
            if (valores == null) return;
            Platform.runLater(() -> filtroLocalizacao.getItems().setAll(valores));
        });
    }

    private void carregarDepartamentos() {
        if (filtroDepartamento == null) return;
        CompletableFuture.runAsync(() -> {
            var resp = Rotas.get(ROTAS + "/distinct/departamentos");
            if (!resp.isSuccess()) return;
            List<String> valores = resp.getDataList("valores");
            if (valores == null) return;
            Platform.runLater(() -> filtroDepartamento.getItems().setAll(valores));
        });
    }

    // ===== LEITURA DA UI → PARAMS =====
    public Params paramsDeFiltros() {
        Params p = Params.with("_", "");   // placeholder — substituído no primeiro filtro presente

        if (filtroBusca != null && notBlank(filtroBusca.getText())) {
            // Rota aceita "sku" como parâmetro de busca; número de série cai em fallback do backend
            p.and("sku", filtroBusca.getText().trim());
        }
        if (filtroStatus != null && filtroStatus.getValue() != null) {
            p.and("status", filtroStatus.getValue());
        }
        if (filtroCondicao != null && filtroCondicao.getValue() != null) {
            p.and("condicao", filtroCondicao.getValue());
        }
        if (filtroLocalizacao != null && filtroLocalizacao.getValue() != null) {
            p.and("localizacao", filtroLocalizacao.getValue());
        }
        if (filtroDepartamento != null && filtroDepartamento.getValue() != null) {
            p.and("departamento", filtroDepartamento.getValue());
        }
        if (filtroMac != null && notBlank(filtroMac.getText())) {
            p.and("mac", filtroMac.getText().trim());
        }
        if (filtroAquisicaoInicio != null && filtroAquisicaoInicio.getValue() != null) {
            p.and("inicio", filtroAquisicaoInicio.getValue().toString());
        }
        if (filtroAquisicaoFim != null && filtroAquisicaoFim.getValue() != null) {
            p.and("fim", filtroAquisicaoFim.getValue().toString());
        }
        // TODO: enviar filtroInstalacao* e filtroVerificacao* quando a rota /com-filtros os aceitar.
        return p;
    }

    // ===== DETECÇÃO DE FILTROS ATIVOS =====
    public boolean temFiltrosAtivos() {
        return (filtroStatus != null && filtroStatus.getValue() != null)
                || (filtroCondicao != null && filtroCondicao.getValue() != null)
                || (filtroLocalizacao != null && filtroLocalizacao.getValue() != null)
                || (filtroDepartamento != null && filtroDepartamento.getValue() != null)
                || (filtroMac != null && notBlank(filtroMac.getText()))
                || (filtroBusca != null && notBlank(filtroBusca.getText()))
                || (filtroAquisicaoInicio != null && filtroAquisicaoInicio.getValue() != null)
                || (filtroAquisicaoFim != null && filtroAquisicaoFim.getValue() != null);
    }

    // ===== LIMPEZA =====
    public void clearFiltersUI() {
        if (filtroStatus != null) filtroStatus.setValue(null);
        if (filtroCondicao != null) filtroCondicao.setValue(null);
        if (filtroLocalizacao != null) filtroLocalizacao.setValue(null);
        if (filtroDepartamento != null) filtroDepartamento.setValue(null);
        if (filtroMac != null) filtroMac.clear();
        if (filtroBusca != null) filtroBusca.clear();
        if (filtroAquisicaoInicio != null) filtroAquisicaoInicio.setValue(null);
        if (filtroAquisicaoFim != null) filtroAquisicaoFim.setValue(null);
        if (filtroInstalacaoInicio != null) filtroInstalacaoInicio.setValue(null);
        if (filtroInstalacaoFim != null) filtroInstalacaoFim.setValue(null);
        if (filtroVerificacaoInicio != null) filtroVerificacaoInicio.setValue(null);
        if (filtroVerificacaoFim != null) filtroVerificacaoFim.setValue(null);
    }

    // ===== UTILITÁRIOS =====
    private boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }

    public void cleanup() {
        filtroMac = null;
        filtroStatus = null;
        filtroCondicao = null;
        filtroLocalizacao = null;
        filtroDepartamento = null;
        filtroBusca = null;
        filtroAquisicaoInicio = null;
        filtroAquisicaoFim = null;
        filtroInstalacaoInicio = null;
        filtroInstalacaoFim = null;
        filtroVerificacaoInicio = null;
        filtroVerificacaoFim = null;
    }
}