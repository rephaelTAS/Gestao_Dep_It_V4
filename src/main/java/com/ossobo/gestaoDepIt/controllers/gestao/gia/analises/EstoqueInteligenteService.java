package com.ossobo.gestaoDepIt.controllers.gestao.gia.analises;/*
 * EstoqueInteligenteService v1.0
 *
 * Analisa estoques: mínimo, máximo, ruptura, excesso, itens parados.
 * Baseado em consumo histórico e movimentações.
 *
 * v1.0: Versão inicial
 */


import com.ossobo.gestaoDepIt.db.models.CatalogoProdutos;
import com.ossobo.gestaoDepIt.db.models.EstoqueMovimentacoes;
import com.ossobo.gestaoDepIt.db.enums.TipoMovimentacao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class EstoqueInteligenteService {

    private static final Logger logger = LoggerFactory.getLogger(EstoqueInteligenteService.class);

    // Estoque mínimo padrão
    private static final int ESTOQUE_MINIMO_PADRAO = 5;
    // Dias para considerar item parado
    private static final int DIAS_ITEM_PARADO = 90;
    // Meses para cálculo de consumo médio
    private static final int MESES_CONSUMO_MEDIO = 6;

    /**
     * Calcula o estoque mínimo recomendado baseado no consumo histórico.
     */
    public int calcularEstoqueMinimo(String sku, List<EstoqueMovimentacoes> movimentacoes) {
        List<EstoqueMovimentacoes> saidas = movimentacoes.stream()
                .filter(m -> m.getSkuProduto().equals(sku))
                .filter(m -> m.getTipoMovimentacao() == TipoMovimentacao.SAIDA)
                .filter(m -> m.getDataMovimentacao() != null)
                .filter(m -> ChronoUnit.MONTHS.between(m.getDataMovimentacao(), LocalDate.now()) <= MESES_CONSUMO_MEDIO)
                .collect(Collectors.toList());

        if (saidas.isEmpty()) return ESTOQUE_MINIMO_PADRAO;

        int totalSaida = saidas.stream().mapToInt(EstoqueMovimentacoes::getQuantidade).sum();
        double mediaMensal = (double) totalSaida / MESES_CONSUMO_MEDIO;

        // Estoque mínimo = 1.5x a média mensal (margem de segurança)
        return Math.max(1, (int) Math.ceil(mediaMensal * 1.5));
    }

    /**
     * Analisa todos os produtos do catálogo e gera alertas.
     */
    public List<EstoqueAlerta> analisarEstoque(List<CatalogoProdutos> catalogos,
                                               List<EstoqueMovimentacoes> movimentacoes) {
        List<EstoqueAlerta> alertas = new ArrayList<>();

        for (CatalogoProdutos cp : catalogos) {
            if (cp.getTotalRecebido() == null) continue;

            int estoqueAtual = cp.getTotalRecebido();
            int minimo = calcularEstoqueMinimo(cp.getSku(), movimentacoes);
            int maximo = minimo * 3; // máximo = 3x o mínimo

            EstoqueAlerta alerta = new EstoqueAlerta();
            alerta.setSku(cp.getSku());
            alerta.setNome(cp.getNomeCompleto());
            alerta.setEstoqueAtual(estoqueAtual);
            alerta.setEstoqueMinimo(minimo);
            alerta.setEstoqueMaximo(maximo);

            if (estoqueAtual <= 0) {
                alerta.setStatus(EstoqueStatus.ROMPIDO);
                alerta.setSugestao("Comprar " + minimo + " unidades imediatamente");
            } else if (estoqueAtual < minimo) {
                alerta.setStatus(EstoqueStatus.BAIXO);
                alerta.setSugestao("Comprar " + (minimo - estoqueAtual) + " unidades");
            } else if (estoqueAtual > maximo) {
                alerta.setStatus(EstoqueStatus.EXCESSO);
                alerta.setSugestao("Excesso de " + (estoqueAtual - maximo) + " un. Reduzir compras.");
            } else {
                alerta.setStatus(EstoqueStatus.ADEQUADO);
                alerta.setSugestao("Estoque adequado");
            }

            alertas.add(alerta);
        }

        return alertas;
    }

    /**
     * Identifica itens parados (sem movimentação há mais de N dias).
     */
    public List<ItemParado> identificarItensParados(List<CatalogoProdutos> catalogos,
                                                    List<EstoqueMovimentacoes> movimentacoes) {
        List<ItemParado> parados = new ArrayList<>();

        for (CatalogoProdutos cp : catalogos) {
            Optional<EstoqueMovimentacoes> ultimaMov = movimentacoes.stream()
                    .filter(m -> m.getSkuProduto().equals(cp.getSku()))
                    .filter(m -> m.getDataMovimentacao() != null)
                    .max(Comparator.comparing(EstoqueMovimentacoes::getDataMovimentacao));

            if (ultimaMov.isPresent()) {
                long diasParado = ChronoUnit.DAYS.between(ultimaMov.get().getDataMovimentacao(), LocalDate.now());

                if (diasParado > DIAS_ITEM_PARADO) {
                    ItemParado ip = new ItemParado();
                    ip.setSku(cp.getSku());
                    ip.setNome(cp.getNomeCompleto());
                    ip.setDiasParado(diasParado);
                    ip.setValorImobilizado(cp.getValorTotalEstoque());
                    ip.setAcaoSugerida(gerarAcaoItemParado(diasParado, cp));
                    parados.add(ip);
                }
            }
        }

        parados.sort(Comparator.comparingLong(ItemParado::getDiasParado).reversed());
        return parados;
    }

    private String gerarAcaoItemParado(long diasParado, CatalogoProdutos cp) {
        if (diasParado > 365) return "Descartar ou doar — parado há mais de 1 ano";
        if (diasParado > 180) return "Avaliar venda ou redistribuição — parado há 6+ meses";
        return "Monitorar — se não houver consumo em 30 dias, reavaliar";
    }

    // ===== INNER CLASSES =====

    public enum EstoqueStatus {
        ROMPIDO, BAIXO, ADEQUADO, EXCESSO
    }

    public static class EstoqueAlerta {
        private String sku;
        private String nome;
        private int estoqueAtual;
        private int estoqueMinimo;
        private int estoqueMaximo;
        private EstoqueStatus status;
        private String sugestao;

        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }
        public String getNome() { return nome; }
        public void setNome(String nome) { this.nome = nome; }
        public int getEstoqueAtual() { return estoqueAtual; }
        public void setEstoqueAtual(int estoqueAtual) { this.estoqueAtual = estoqueAtual; }
        public int getEstoqueMinimo() { return estoqueMinimo; }
        public void setEstoqueMinimo(int estoqueMinimo) { this.estoqueMinimo = estoqueMinimo; }
        public int getEstoqueMaximo() { return estoqueMaximo; }
        public void setEstoqueMaximo(int estoqueMaximo) { this.estoqueMaximo = estoqueMaximo; }
        public EstoqueStatus getStatus() { return status; }
        public void setStatus(EstoqueStatus status) { this.status = status; }
        public String getSugestao() { return sugestao; }
        public void setSugestao(String sugestao) { this.sugestao = sugestao; }
    }

    public static class ItemParado {
        private String sku;
        private String nome;
        private long diasParado;
        private BigDecimal valorImobilizado;
        private String acaoSugerida;

        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }
        public String getNome() { return nome; }
        public void setNome(String nome) { this.nome = nome; }
        public long getDiasParado() { return diasParado; }
        public void setDiasParado(long diasParado) { this.diasParado = diasParado; }
        public BigDecimal getValorImobilizado() { return valorImobilizado; }
        public void setValorImobilizado(BigDecimal valorImobilizado) { this.valorImobilizado = valorImobilizado; }
        public String getAcaoSugerida() { return acaoSugerida; }
        public void setAcaoSugerida(String acaoSugerida) { this.acaoSugerida = acaoSugerida; }
    }
}