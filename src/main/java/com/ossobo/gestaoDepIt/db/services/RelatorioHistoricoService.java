package com.ossobo.gestaoDepIt.db.services;

import com.ossobo.gestaoDepIt.db.relatorios.RelatorioHistorico;
import com.ossobo.gestaoDepIt.db.relatorios.RelatorioHistoricoRepository;
import com.ossobo.winterfx.anotations.Inject;
import com.ossobo.winterfx.anotations.Service;

import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * RelatorioHistoricoService v1.0
 *
 * Regra de negócio do ledger de relatórios gerados.
 * Append-only: registra, nunca altera/apaga.
 * Repositório injetado — segue o padrão dos demais services do projeto.
 */
@Service
public class RelatorioHistoricoService {

    private static final System.Logger logger =
            System.getLogger(RelatorioHistoricoService.class.getName());

    @Inject
    private RelatorioHistoricoRepository repository;

    // ============================================================
    // ESCRITA
    // ============================================================

    public RelatorioHistorico registrar(String titulo, List<String> tabelas, String filtrosJson)
            throws SQLException {
        if (titulo == null || titulo.isBlank())
            throw new IllegalArgumentException("Título é obrigatório");
        if (tabelas == null || tabelas.isEmpty())
            throw new IllegalArgumentException("Ao menos uma tabela é obrigatória");

        RelatorioHistorico h = RelatorioHistorico.novo(titulo, tabelas, filtrosJson);
        return repository.inserir(h);
    }

    // ============================================================
    // LEITURA / MÉTRICAS
    // ============================================================

    public List<RelatorioHistorico> listarRecentes(int limite) throws SQLException {
        return repository.findRecentes(limite);
    }

    public long contarTotal() throws SQLException {
        return repository.countTotal();
    }

    public long contarHoje() throws SQLException {
        LocalDateTime inicio = LocalDate.now().atStartOfDay();
        LocalDateTime fim = LocalDate.now().atTime(LocalTime.MAX);
        return repository.findByPeriodo(inicio, fim).size();
    }

    public long contarSemana() throws SQLException {
        LocalDateTime inicio = LocalDate.now().minusDays(6).atStartOfDay();
        LocalDateTime fim = LocalDate.now().atTime(LocalTime.MAX);
        return repository.findByPeriodo(inicio, fim).size();
    }

    public double mediaPorDia() throws SQLException {
        List<RelatorioHistorico> todos = repository.findRecentes(10_000);
        if (todos.isEmpty()) return 0.0;
        LocalDateTime maisAntigo = todos.stream()
                .map(RelatorioHistorico::geradoEm)
                .min(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now());
        long dias = Math.max(1, Duration.between(maisAntigo, LocalDateTime.now()).toDays() + 1);
        return (double) todos.size() / dias;
    }

    public Map<String, Integer> contarPorCombinacao() throws SQLException {
        return repository.countPorCombinacao();
    }
}