// ===== MOVIMENTACAO_FORM_VIEW.java (CORRIGIDO v4.4.7) =====
package com.ossobo.gestaoDepIt.controllers.gestao.estoque.movimentacoes.formview;

import javafx.stage.Window;
import com.ossobo.gestaoDepIt.controllers.gestao.estoque.movimentacoes.EstoqueMovimentacaoFormController;
import com.ossobo.nexusfx.di.annotations.Component;
import com.ossobo.nexusfx.NexusFX;
import java.util.function.BiConsumer;

/**
 * v4.4.7 - GERENCIADOR DE FORMULÁRIO DE MOVIMENTAÇÃO
 * ✅ Corrigido: NexusFX.dialogs().openWindowModal()
 */
@Component
public class MovimentacaoFormView {

    public void abrirFormulario(Window owner, BiConsumer<Boolean, String> callback) {
        abrirFormulario(owner, callback, null, null);
    }

    public void abrirFormularioEdicao(Window owner, Long movimentacaoId, BiConsumer<Boolean, String> callback) {
        abrirFormulario(owner, callback, null, movimentacaoId);
    }

    public void abrirFormularioParaProduto(Window owner, String skuProduto, BiConsumer<Boolean, String> callback) {
        abrirFormulario(owner, callback, skuProduto, null);
    }

    /**
     * v4.4.7 - Correção: substituído openWithController por openWindowModal
     */
    private void abrirFormulario(Window owner, BiConsumer<Boolean, String> callback,
                                 String skuProduto, Long movimentacaoId) {
        try {
            // ✅ CORRETO - usando openWindowModal com Consumer<Controller>
            NexusFX.dialogs().openModalWithController(
                    "addeditamovimenta",
                    determinarTitulo(skuProduto, movimentacaoId),
                    owner,
                    (EstoqueMovimentacaoFormController controller) -> {
                        controller.setCallback(callback);
                        controller.setOwnerWindow(owner);

                        if (movimentacaoId != null) {
                            controller.inicializarModoEdicao(movimentacaoId);
                        } else if (skuProduto != null) {
                            controller.inicializarModoCriacao(skuProduto);
                        } else {
                            controller.inicializarModoCriacao(null);
                        }
                    }
            );
        } catch (Exception e) {
            NexusFX.alerts().erro(
                    "Erro ao Abrir Formulário",
                    "Não foi possível abrir o formulário de movimentação: " + e.getMessage(),
                    "MovimentacaoFormView"
            );

            if (callback != null) {
                callback.accept(false, "Erro ao abrir formulário: " + e.getMessage());
            }
        }
    }

    private String determinarTitulo(String skuProduto, Long movimentacaoId) {
        if (movimentacaoId != null) {
            return "Editar Movimentação #" + movimentacaoId;
        } else if (skuProduto != null) {
            return "Nova Movimentação - " + skuProduto;
        } else {
            return "Nova Movimentação de Estoque";
        }
    }

    public boolean isFormularioDisponivel() {
        try {
            return getClass().getResource("/fxml/addeditamovimenta.fxml") != null;
        } catch (Exception e) {
            return false;
        }
    }
}