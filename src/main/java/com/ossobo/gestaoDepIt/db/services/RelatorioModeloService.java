package com.ossobo.gestaoDepIt.db.services;

import com.ossobo.gestaoDepIt.db.relatorios.RelatorioCatalogoGrafo;
import com.ossobo.gestaoDepIt.db.relatorios.RelatorioJoin;
import com.ossobo.gestaoDepIt.db.relatorios.RelatorioModelo;
import com.ossobo.gestaoDepIt.db.relatorios.RelatorioModeloRepository;
import com.ossobo.winterfx.anotations.Inject;
import com.ossobo.winterfx.anotations.Service;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * RelatorioModeloService v1.0
 *
 * Regra de negócio dos modelos LEGO salvos.
 * Valida cada JOIN contra o RelatorioCatalogoGrafo ANTES de persistir
 * — defesa em profundidade contra composições inválidas.
 */
@Service
public class RelatorioModeloService {

    private static final System.Logger logger =
            System.getLogger(RelatorioModeloService.class.getName());

    @Inject
    private RelatorioModeloRepository repository;

    // ============================================================
    // ESCRITA
    // ============================================================

    public RelatorioModelo salvar(RelatorioModelo m) throws SQLException {
        validar(m);
        if (repository.existsByNome(m.nome()))
            throw new IllegalStateException("Já existe um modelo com este nome: " + m.nome());
        return repository.insert(m);
    }

    public RelatorioModelo atualizar(RelatorioModelo m) throws SQLException {
        validar(m);
        if (!repository.existsById(m.id()))
            throw new IllegalArgumentException("Modelo não encontrado: " + m.id());
        repository.update(m);
        return m;
    }

    public void excluir(String id) throws SQLException {
        if (id == null || id.isBlank())
            throw new IllegalArgumentException("ID é obrigatório");
        if (!repository.delete(id))
            throw new IllegalArgumentException("Modelo não encontrado: " + id);
        logger.log(System.Logger.Level.INFO, "✅ Modelo excluído: ID={0}", id);
    }

    // ============================================================
    // LEITURA
    // ============================================================

    public Optional<RelatorioModelo> buscarPorId(String id) throws SQLException {
        return repository.findById(id);
    }

    public Optional<RelatorioModelo> buscarPorNome(String nome) throws SQLException {
        return repository.findByNome(nome);
    }

    public List<RelatorioModelo> listarTodos() throws SQLException {
        return repository.findAll();
    }

    // ============================================================
    // VALIDAÇÃO (grafo é a fonte única)
    // ============================================================

    private void validar(RelatorioModelo m) {
        if (m == null) throw new IllegalArgumentException("Modelo inválido");
        if (!RelatorioCatalogoGrafo.aliasesValidos().contains(m.tabelaBase()))
            throw new IllegalArgumentException("Tabela base desconhecida: " + m.tabelaBase());

        for (RelatorioJoin j : m.joins()) {
            if (!RelatorioCatalogoGrafo.aliasesValidos().contains(j.tabela()))
                throw new IllegalArgumentException("Alias de JOIN desconhecido: " + j.tabela());

            RelatorioCatalogoGrafo.Aresta aresta = RelatorioCatalogoGrafo
                    .arestaEntre(m.tabelaBase(), j.tabela())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "JOIN não permitido pelo grafo: " + m.tabelaBase() + " → " + j.tabela()));

            String esperada = aresta.condicao().replace(" ", "");
            String recebida = j.condicao().replace(" ", "");
            if (!esperada.equalsIgnoreCase(recebida))
                throw new IllegalArgumentException(
                        "Condição inválida para " + j.tabela() + ". Esperada: " + aresta.condicao());
        }
    }
}