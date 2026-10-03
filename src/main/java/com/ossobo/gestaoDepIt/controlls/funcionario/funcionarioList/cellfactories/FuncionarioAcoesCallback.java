package com.ossobo.gestaoDepIt.controlls.funcionario.funcionarioList.cellfactories;

import com.ossobo.gestaoDepIt.db.models.Funcionarios;

/**
 * FuncionarioAcoesCallback v1.0
 *
 * Contrato entre as cell factories e a camada que conhece as janelas
 * flutuantes (FuncionarioListController). As cell factories não abrem
 * janelas, não chamam Rotas — apenas emitem intenção.
 *
 * @since v1.0
 */
public interface FuncionarioAcoesCallback {

    /** Abrir a tela de detalhes do funcionário. */
    void abrirDetalhes(Funcionarios funcionario);

    /** Abrir a tela de edição do funcionário. */
    void abrirEdicao(Funcionarios funcionario);

    /** Alternar status (ativar/desativar). */
    void alternarStatus(Funcionarios funcionario);

    /** Solicitar bytes da imagem de perfil (para a célula Foto). */
    java.util.concurrent.CompletableFuture<byte[]> carregarImagem(String codDep);
}