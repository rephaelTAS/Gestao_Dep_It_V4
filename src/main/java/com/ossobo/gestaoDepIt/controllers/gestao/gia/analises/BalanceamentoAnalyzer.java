package com.ossobo.gestaoDepIt.controllers.gestao.gia.analises;/*
 * BalanceamentoAnalyzer v1.0
 *
 * Analisa o balanceamento de equipamentos entre funcionários e departamentos.
 * Identifica outliers e sugere redistribuição.
 *
 * v1.0: Versão inicial
 */

import com.ossobo.gestaoDepIt.db.models.Funcionarios;
import com.ossobo.gestaoDepIt.db.models.InventarioEquipamentos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class BalanceamentoAnalyzer {

    private static final Logger logger = LoggerFactory.getLogger(BalanceamentoAnalyzer.class);

    // Limiar de desbalanceamento: funcionário com mais de 1.5x a média é outlier
    private static final double FATOR_OUTLIER_SUPERIOR = 1.5;
    // Funcionário com menos de 0.3x a média está desfalcado
    private static final double FATOR_OUTLIER_INFERIOR = 0.3;

    /**
     * Analisa o balanceamento de equipamentos por funcionário.
     * @return BalanceamentoResult com médias, outliers e sugestões
     */
    public BalanceamentoResult analisar(List<InventarioEquipamentos> equipamentos,
                                        List<Funcionarios> funcionarios) {

        // Agrupar equipamentos por funcionário
        Map<String, Long> qtdPorFuncionario = equipamentos.stream()
                .filter(e -> e.getFuncionarioId() != null)
                .collect(Collectors.groupingBy(InventarioEquipamentos::getFuncionarioId, Collectors.counting()));

        if (qtdPorFuncionario.isEmpty()) {
            return new BalanceamentoResult(0, 0, List.of(), List.of(), List.of());
        }

        // Calcular média
        double media = qtdPorFuncionario.values().stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0);

        double limiteSuperior = media * FATOR_OUTLIER_SUPERIOR;
        double limiteInferior = media * FATOR_OUTLIER_INFERIOR;

        // Identificar outliers
        List<FuncionarioBalance> acimaDaMedia = new ArrayList<>();
        List<FuncionarioBalance> abaixoDaMedia = new ArrayList<>();
        List<FuncionarioBalance> todos = new ArrayList<>();

        for (Map.Entry<String, Long> entry : qtdPorFuncionario.entrySet()) {
            String codDep = entry.getKey();
            long quantidade = entry.getValue();

            Funcionarios func = funcionarios.stream()
                    .filter(f -> f.getCodDep().equals(codDep))
                    .findFirst()
                    .orElse(null);

            String nome = func != null ? func.getNome() : codDep;
            String departamento = func != null ? func.getDepartamento() : "Desconhecido";

            FuncionarioBalance fb = new FuncionarioBalance(codDep, nome, departamento, quantidade, media);

            todos.add(fb);

            if (quantidade > limiteSuperior) {
                acimaDaMedia.add(fb);
                fb.setAlerta(String.format("⚠ %s tem %.0f equipamentos (média: %.1f). Sugestão: redistribuir.",
                        nome, (double) quantidade, media));
            } else if (quantidade < limiteInferior) {
                abaixoDaMedia.add(fb);
                fb.setAlerta(String.format("📌 %s tem apenas %d equipamento(s). Pode precisar de mais recursos.",
                        nome, quantidade));
            }
        }

        // Ordenar
        todos.sort(Comparator.comparingLong(FuncionarioBalance::getQuantidade).reversed());
        acimaDaMedia.sort(Comparator.comparingLong(FuncionarioBalance::getQuantidade).reversed());
        abaixoDaMedia.sort(Comparator.comparingLong(FuncionarioBalance::getQuantidade));

        // Gerar sugestões de redistribuição
        List<String> sugestoes = gerarSugestoes(acimaDaMedia, abaixoDaMedia);

        logger.info("Análise de balanceamento: média={:.1f}, {} acima, {} abaixo, {} sugestões",
                media, acimaDaMedia.size(), abaixoDaMedia.size(), sugestoes.size());

        return new BalanceamentoResult(media, qtdPorFuncionario.size(), todos, acimaDaMedia, abaixoDaMedia, sugestoes);
    }

    /**
     * Analisa balanceamento por departamento.
     */
    public Map<String, DepartamentoBalance> analisarPorDepartamento(List<InventarioEquipamentos> equipamentos,
                                                                    List<Funcionarios> funcionarios) {
        Map<String, List<Funcionarios>> funcsPorDepto = funcionarios.stream()
                .filter(f -> f.getDepartamento() != null)
                .collect(Collectors.groupingBy(Funcionarios::getDepartamento));

        Map<String, DepartamentoBalance> resultado = new LinkedHashMap<>();

        for (Map.Entry<String, List<Funcionarios>> entry : funcsPorDepto.entrySet()) {
            String depto = entry.getKey();
            int totalFuncionarios = entry.getValue().size();

            long totalEquipamentos = equipamentos.stream()
                    .filter(e -> depto.equals(e.getDepartamento()))
                    .count();

            double mediaPorFunc = totalFuncionarios > 0 ? (double) totalEquipamentos / totalFuncionarios : 0;

            resultado.put(depto, new DepartamentoBalance(depto, totalFuncionarios, totalEquipamentos, mediaPorFunc));
        }

        return resultado;
    }

    private List<String> gerarSugestoes(List<FuncionarioBalance> acima, List<FuncionarioBalance> abaixo) {
        List<String> sugestoes = new ArrayList<>();

        if (!acima.isEmpty() && !abaixo.isEmpty()) {
            sugestoes.add(String.format("🔴 %d funcionário(s) com excesso de equipamentos", acima.size()));
            sugestoes.add(String.format("🟡 %d funcionário(s) com poucos equipamentos", abaixo.size()));

            if (acima.size() >= 2 && abaixo.size() >= 2) {
                sugestoes.add("💡 Considere uma política de redistribuição por departamento.");
            }

            // Sugestão específica do maior outlier
            FuncionarioBalance maior = acima.get(0);
            FuncionarioBalance menor = abaixo.get(0);
            if (maior.getQuantidade() >= menor.getQuantidade() + 2) {
                sugestoes.add(String.format("🔄 Sugestão: Transferir equipamento de %s para %s.",
                        maior.getNome(), menor.getNome()));
            }
        }

        if (sugestoes.isEmpty()) {
            sugestoes.add("✅ Balanceamento adequado. Nenhuma ação necessária.");
        }

        return sugestoes;
    }

    // ===== INNER CLASSES =====

    public static class BalanceamentoResult {
        private final double mediaEquipamentos;
        private final int totalFuncionariosComEquipamento;
        private final List<FuncionarioBalance> todos;
        private final List<FuncionarioBalance> acimaDaMedia;
        private final List<FuncionarioBalance> abaixoDaMedia;
        private final List<String> sugestoes;

        public BalanceamentoResult(double mediaEquipamentos, int totalFuncionariosComEquipamento,
                                   List<FuncionarioBalance> todos, List<FuncionarioBalance> acimaDaMedia,
                                   List<FuncionarioBalance> abaixoDaMedia) {
            this(mediaEquipamentos, totalFuncionariosComEquipamento, todos, acimaDaMedia, abaixoDaMedia, List.of());
        }

        public BalanceamentoResult(double mediaEquipamentos, int totalFuncionariosComEquipamento,
                                   List<FuncionarioBalance> todos, List<FuncionarioBalance> acimaDaMedia,
                                   List<FuncionarioBalance> abaixoDaMedia, List<String> sugestoes) {
            this.mediaEquipamentos = mediaEquipamentos;
            this.totalFuncionariosComEquipamento = totalFuncionariosComEquipamento;
            this.todos = todos;
            this.acimaDaMedia = acimaDaMedia;
            this.abaixoDaMedia = abaixoDaMedia;
            this.sugestoes = sugestoes;
        }

        public double getMediaEquipamentos() { return mediaEquipamentos; }
        public int getTotalFuncionariosComEquipamento() { return totalFuncionariosComEquipamento; }
        public List<FuncionarioBalance> getTodos() { return todos; }
        public List<FuncionarioBalance> getAcimaDaMedia() { return acimaDaMedia; }
        public List<FuncionarioBalance> getAbaixoDaMedia() { return abaixoDaMedia; }
        public List<String> getSugestoes() { return sugestoes; }
    }

    public static class FuncionarioBalance {
        private final String codDep;
        private final String nome;
        private final String departamento;
        private final long quantidade;
        private final double media;
        private String alerta;

        public FuncionarioBalance(String codDep, String nome, String departamento, long quantidade, double media) {
            this.codDep = codDep;
            this.nome = nome;
            this.departamento = departamento;
            this.quantidade = quantidade;
            this.media = media;
        }

        public String getCodDep() { return codDep; }
        public String getNome() { return nome; }
        public String getDepartamento() { return departamento; }
        public long getQuantidade() { return quantidade; }
        public double getMedia() { return media; }
        public String getAlerta() { return alerta; }
        public void setAlerta(String alerta) { this.alerta = alerta; }
        public boolean isAcimaDaMedia() { return quantidade > media * FATOR_OUTLIER_SUPERIOR; }
        public boolean isAbaixoDaMedia() { return quantidade < media * FATOR_OUTLIER_INFERIOR; }
    }

    public static class DepartamentoBalance {
        private final String departamento;
        private final int totalFuncionarios;
        private final long totalEquipamentos;
        private final double mediaPorFuncionario;

        public DepartamentoBalance(String departamento, int totalFuncionarios, long totalEquipamentos, double mediaPorFuncionario) {
            this.departamento = departamento;
            this.totalFuncionarios = totalFuncionarios;
            this.totalEquipamentos = totalEquipamentos;
            this.mediaPorFuncionario = mediaPorFuncionario;
        }

        public String getDepartamento() { return departamento; }
        public int getTotalFuncionarios() { return totalFuncionarios; }
        public long getTotalEquipamentos() { return totalEquipamentos; }
        public double getMediaPorFuncionario() { return mediaPorFuncionario; }
    }
}