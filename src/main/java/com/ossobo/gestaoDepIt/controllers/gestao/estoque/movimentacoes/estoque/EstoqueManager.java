// ===== ESTOQUE_MANAGER.java =====
package com.ossobo.gestaoDepIt.controllers.gestao.estoque.movimentacoes.estoque;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart;
import com.ossobo.gestaoDepIt.controllers.gestao.estoque.movimentacoes.dto.MovimentacaoDTO;
import com.ossobo.gestaoDepIt.controllers.gestao.estoque.movimentacoes.dto.FiltroMovimentacaoDTO;
import com.ossobo.gestaoDepIt.controllers.gestao.estoque.movimentacoes.dto.EstatisticasDTO;
import com.ossobo.gestaoDepIt.db.models.EstoqueMovimentacoes;
import com.ossobo.gestaoDepIt.db.services.EstoqueMovimentacoesService;
import com.ossobo.nexusfx.di.annotations.Component;
import com.ossobo.nexusfx.di.annotations.Inject;

import java.util.List;
import java.util.stream.Collectors;

/**
 * GERENCIADOR DE LÓGICA DE ESTOQUE
 * Propósito: Centralizar regras de negócio relacionadas a estoque
 * Princípio: "Single Responsibility"
 */
@Component
public class EstoqueManager {

    @Inject
    private EstoqueMovimentacoesService movimentacoesService;

    // ===== BUSCAR_MOVIMENTACOES =====
    public ObservableList<MovimentacaoDTO> buscarMovimentacoes(FiltroMovimentacaoDTO filtro) {
        try {
            // Converte filtro DTO para parâmetros do service
            var movimentacoes = movimentacoesService.buscarParaConversaoDTO(
                    filtro, 1, 1000 // Ajustar paginação conforme necessário
            );

            // Converte para DTOs de apresentação
            return FXCollections.observableArrayList(
                    movimentacoes.stream()
                            .map(this::converterParaDTO)
                            .collect(Collectors.toList())
            );

        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar movimentações", e);
        }
    }

    // ===== CALCULAR_ESTATISTICAS =====
    public EstatisticasDTO calcularEstatisticas(FiltroMovimentacaoDTO filtro) {
        try {
            var estatisticas = movimentacoesService.buscarEstatisticasDashboard(filtro);
            return EstatisticasDTO.fromMap(estatisticas);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao calcular estatísticas", e);
        }
    }

    // ===== GERAR_DADOS_GRAFICO =====
    public ObservableList<PieChart.Data> gerarDadosGrafico(FiltroMovimentacaoDTO filtro) {
        try {
            var contagemPorTipo = movimentacoesService.obterContagemPorTipo(
                    filtro.getDataInicio(), filtro.getDataFim()
            );

            return FXCollections.observableArrayList(
                    contagemPorTipo.entrySet().stream()
                            .map(entry -> new PieChart.Data(entry.getKey(), entry.getValue()))
                            .collect(Collectors.toList())
            );
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar dados gráfico", e);
        }
    }

    // ===== EXPORTAR_PARA_EXCEL =====
    public void exportarParaExcel(List<MovimentacaoDTO> dados, FiltroMovimentacaoDTO filtro) {
        // Implementação de exportação Excel
        // Usaria Apache POI ou similar
    }

    // ===== CONVERTER_PARA_DTO (PRIVADO) =====
// ===== CONVERTER_PARA_DTO (ATUALIZADO) =====
    /**
     * Converte entidade para DTO imutável usando Builder Pattern.
     */
    private MovimentacaoDTO converterParaDTO(EstoqueMovimentacoes entity) {
        return new MovimentacaoDTO.Builder()
                .id(entity.getId())
                .dataMovimentacao(entity.getDataMovimentacao())
                .skuProduto(entity.getSkuProduto())
                .tipoMovimentacao(entity.getTipoMovimentacao().toString())
                .quantidade(entity.getQuantidade())
                .lote(entity.getLote())
                .dataValidade(entity.getDataValidade())
                .localizacao(entity.getLocalizacao())
                .nomeUsuario(buscarNomeUsuario(entity.getCodDepFuncionario()))
                .motivo(entity.getMotivo())
                .nomeProduto(buscarNomeProduto(entity.getSkuProduto())) // Se necessário
                .build();
    }

    // ===== MÉTODO AUXILIAR BUSCAR NOME USUÁRIO =====
    private String buscarNomeUsuario(String codDepFuncionario) {
        // Implementação real buscaria de serviço/repositório
        // Por enquanto, retorna o código ou nome mock
        try {
            // Simulação - na implementação real, usar serviço
            return "Usuário #" + codDepFuncionario;
        } catch (Exception e) {
            return "Desconhecido";
        }
    }

    // ===== MÉTODO AUXILIAR BUSCAR NOME PRODUTO =====
    private String buscarNomeProduto(String sku) {
        // Implementação real buscaria de catálogo de produtos
        // Por enquanto, retorna o SKU
        return "Produto SKU: " + sku;
    }
}