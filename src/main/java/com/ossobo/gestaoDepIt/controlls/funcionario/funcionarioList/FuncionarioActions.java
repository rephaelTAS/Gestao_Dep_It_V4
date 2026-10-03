package com.ossobo.gestaoDepIt.controlls.funcionario.funcionarioList;

import com.ossobo.gestaoDepIt.db.models.Funcionarios;
import com.ossobo.winterfx.router.Rotas;
import com.ossobo.winterfx.router.model.Params;
import com.ossobo.winterfx.router.model.ResponseData;
import javafx.application.Platform;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * FuncionarioActions v1.1
 *
 * Responsabilidade: despachar rotas de dados de funcionários e atualizar
 *                   o FuncionarioState. Sem UI, sem service injetado.
 *
 * Espelha CatalogActions. Raiz das rotas = "funcionarios/service".
 *
 * v1.1 — Diagnóstico de falha na carga:
 *        - Log da chamada e do retorno de /paginados.
 *        - Checagem explícita de resposta nula e de erro com mensagem legível.
 *        - Exceção propagada com contexto — o whenComplete do controller
 *          imprime a causa raiz em vez de "resposta vazia".
 *
 * @since v1.0
 */
public class FuncionarioActions {

    private static final System.Logger LOGGER =
            System.getLogger(FuncionarioActions.class.getName());

    public static final String ROTAS = "funcionarios/service";

    private final FuncionarioState state;

    public FuncionarioActions(FuncionarioState state) {
        this.state = state;
    }

    // ===== CARREGAR DADOS =====

    public CompletableFuture<Void> carregarDados() {
        return CompletableFuture.runAsync(() -> {
            LOGGER.log(System.Logger.Level.INFO,
                    ">>> GET {0}/paginados (pagina={1}, tamanho={2})",
                    ROTAS, state.getPaginaAtual(), state.getItensPorPagina());

            ResponseData resposta;
            try {
                resposta = Rotas.get(ROTAS + "/paginados",
                        Params.with("pagina", state.getPaginaAtual())
                                .and("tamanho", state.getItensPorPagina()));
            } catch (RuntimeException e) {
                LOGGER.log(System.Logger.Level.ERROR,
                        ">>> Rotas.get lançou exceção", e);
                throw e;
            }

            if (resposta == null) {
                throw new IllegalStateException(
                        "Rota " + ROTAS + "/paginados devolveu null");
            }
            if (!resposta.isSuccess()) {
                String motivo = resposta.getFirstError() != null
                        ? resposta.getFirstError()
                        : resposta.getMessage();
                throw new IllegalStateException(
                        "Rota " + ROTAS + "/paginados falhou: " + motivo);
            }

            List<Funcionarios> funcionarios = resposta.getDataList("funcionarios");

            LOGGER.log(System.Logger.Level.INFO,
                    ">>> recebidos {0} funcionários",
                    funcionarios != null ? funcionarios.size() : -1);

            if (funcionarios == null) funcionarios = List.of();

            long totalRegistros = lerTotal(funcionarios.size());

            final List<Funcionarios> lista = funcionarios;
            Platform.runLater(() -> {
                state.setFuncionariosData(lista);
                state.setTotalPaginas(Math.toIntExact(totalRegistros));
            });
        });
    }

    private long lerTotal(int fallback) {
        try {
            var contagem = Rotas.get(ROTAS + "/total");
            if (contagem == null || !contagem.isSuccess()) return fallback;
            Object bruto = contagem.getData().get("total");
            if (bruto instanceof Number n) return n.longValue();
            return fallback;
        } catch (RuntimeException e) {
            LOGGER.log(System.Logger.Level.WARNING,
                    "total indisponível, usando tamanho da página: {0}", e.getMessage());
            return fallback;
        }
    }

    // ===== AÇÕES DE ESTADO =====

    public CompletableFuture<ResponseData> ativar(Funcionarios f) {
        return putAssincrono(ROTAS + "/ativar", Params.with("coddep", f.codDep()));
    }

    public CompletableFuture<ResponseData> desativar(Funcionarios f) {
        return putAssincrono(ROTAS + "/desativar", Params.with("coddep", f.codDep()));
    }

    public CompletableFuture<ResponseData> alternarStatus(Funcionarios f) {
        boolean ativoAtual = Boolean.TRUE.equals(f.ativo());
        return ativoAtual ? desativar(f) : ativar(f);
    }

    // ===== CARREGAR IMAGEM (para célula Foto) =====

    public CompletableFuture<byte[]> carregarImagem(String codDep) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var resposta = Rotas.get(ROTAS + "/imagem/por/coddep",
                        Params.with("coddep", codDep));
                if (resposta == null || !resposta.isSuccess()) return null;
                return resposta.getData("imagem", byte[].class);
            } catch (RuntimeException e) {
                LOGGER.log(System.Logger.Level.WARNING,
                        "imagem indisponível para {0}: {1}", codDep, e.getMessage());
                return null;
            }
        });
    }

    // ===== UTILITÁRIOS =====

    public void atualizarItemNaLista(Funcionarios atualizado) {
        var lista = state.getFuncionariosData();
        int index = lista.indexOf(atualizado);
        if (index >= 0) lista.set(index, atualizado);
    }

    public CompletableFuture<Void> recarregar() {
        return carregarDados();
    }

    public void cleanup() { /* rotas não retêm estado */ }

    private CompletableFuture<ResponseData> putAssincrono(String rota, Params params) {
        return CompletableFuture.supplyAsync(() -> Rotas.put(rota, params));
    }
}