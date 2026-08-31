/*
 * GIADTOs v1.0
 *
 * Data Transfer Objects para as tabelas do dashboard GIA.
 * Extraídos do Controller — SRP.
 *
 * v1.0: Versão inicial
 */
package com.ossobo.gestaoDepIt.controllers.gestao.gia.functions;

import javafx.beans.property.SimpleStringProperty;
import java.math.BigDecimal;

public final class GIADTOs {

    private GIADTOs() {}

    public static class LocalizacaoStats {
        private final SimpleStringProperty localizacao;
        private final SimpleStringProperty quantidade;
        private final SimpleStringProperty valorFormatado;

        public LocalizacaoStats(String localizacao, int quantidade, BigDecimal valor) {
            this.localizacao = new SimpleStringProperty(localizacao);
            this.quantidade = new SimpleStringProperty(String.valueOf(quantidade));
            this.valorFormatado = new SimpleStringProperty(String.format("€ %,.0f", valor));
        }
        public String getLocalizacao() { return localizacao.get(); }
        public String getQuantidade() { return quantidade.get(); }
        public String getValorFormatado() { return valorFormatado.get(); }
    }

    public static class CriticoItem {
        private final SimpleStringProperty sku, nome, idade, status, recomendacao;
        public CriticoItem(String sku, String nome, String idade, String status, String recomendacao) {
            this.sku = new SimpleStringProperty(sku);
            this.nome = new SimpleStringProperty(nome);
            this.idade = new SimpleStringProperty(idade);
            this.status = new SimpleStringProperty(status);
            this.recomendacao = new SimpleStringProperty(recomendacao);
        }
        public String getSku() { return sku.get(); }
        public String getNome() { return nome.get(); }
        public String getIdade() { return idade.get(); }
        public String getStatus() { return status.get(); }
        public String getRecomendacao() { return recomendacao.get(); }
    }

    public static class RecomendacaoItem {
        private final SimpleStringProperty prioridade, acao, item, impacto, prazo;
        public RecomendacaoItem(String prioridade, String acao, String item, String impacto, String prazo) {
            this.prioridade = new SimpleStringProperty(prioridade);
            this.acao = new SimpleStringProperty(acao);
            this.item = new SimpleStringProperty(item);
            this.impacto = new SimpleStringProperty(impacto);
            this.prazo = new SimpleStringProperty(prazo);
        }
        public String getPrioridade() { return prioridade.get(); }
        public String getAcao() { return acao.get(); }
        public String getItem() { return item.get(); }
        public String getImpacto() { return impacto.get(); }
        public String getPrazo() { return prazo.get(); }
    }

    public static class CustoBeneficioItem {
        private final SimpleStringProperty sku, nome, processador, ram, preco, custoAno, pontuacao;
        public CustoBeneficioItem(String sku, String nome, String processador, String ram,
                                  String preco, String custoAno, String pontuacao) {
            this.sku = new SimpleStringProperty(sku);
            this.nome = new SimpleStringProperty(nome);
            this.processador = new SimpleStringProperty(processador);
            this.ram = new SimpleStringProperty(ram);
            this.preco = new SimpleStringProperty(preco);
            this.custoAno = new SimpleStringProperty(custoAno);
            this.pontuacao = new SimpleStringProperty(pontuacao);
        }
        public String getSku() { return sku.get(); }
        public String getNome() { return nome.get(); }
        public String getProcessador() { return processador.get(); }
        public String getRam() { return ram.get(); }
        public String getPreco() { return preco.get(); }
        public String getCustoAno() { return custoAno.get(); }
        public String getPontuacao() { return pontuacao.get(); }
    }

    public static class EstoqueAlertaItem {
        private final SimpleStringProperty sku, nome, estoqueAtual, estoqueMinimo, excesso, sugestao;
        public EstoqueAlertaItem(String sku, String nome, String estoqueAtual, String estoqueMinimo,
                                 String excesso, String sugestao) {
            this.sku = new SimpleStringProperty(sku);
            this.nome = new SimpleStringProperty(nome);
            this.estoqueAtual = new SimpleStringProperty(estoqueAtual);
            this.estoqueMinimo = new SimpleStringProperty(estoqueMinimo);
            this.excesso = new SimpleStringProperty(excesso);
            this.sugestao = new SimpleStringProperty(sugestao);
        }
        public String getSku() { return sku.get(); }
        public String getNome() { return nome.get(); }
        public String getEstoqueAtual() { return estoqueAtual.get(); }
        public String getEstoqueMinimo() { return estoqueMinimo.get(); }
        public String getExcesso() { return excesso.get(); }
        public String getSugestao() { return sugestao.get(); }
    }

    public static class ItemParado {
        private final SimpleStringProperty sku, nome, diasParado, valorImobilizado, acaoSugerida;
        public ItemParado(String sku, String nome, String diasParado, String valorImobilizado, String acaoSugerida) {
            this.sku = new SimpleStringProperty(sku);
            this.nome = new SimpleStringProperty(nome);
            this.diasParado = new SimpleStringProperty(diasParado);
            this.valorImobilizado = new SimpleStringProperty(valorImobilizado);
            this.acaoSugerida = new SimpleStringProperty(acaoSugerida);
        }
        public String getSku() { return sku.get(); }
        public String getNome() { return nome.get(); }
        public String getDiasParado() { return diasParado.get(); }
        public String getValorImobilizado() { return valorImobilizado.get(); }
        public String getAcaoSugerida() { return acaoSugerida.get(); }
    }
}