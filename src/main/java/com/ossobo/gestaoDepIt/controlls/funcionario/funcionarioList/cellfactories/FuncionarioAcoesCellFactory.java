package com.ossobo.gestaoDepIt.controlls.funcionario.funcionarioList.cellfactories;

import com.ossobo.gestaoDepIt.db.models.Funcionarios;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

/**
 * FuncionarioAcoesCellFactory v1.0
 *
 * Responsabilidade: renderizar a coluna "Ações" da tabela de funcionários
 *                   com três botões — Detalhes, Editar e Ativar/Desativar.
 *
 * O rótulo do terceiro botão varia com o estado atual do funcionário.
 * A factory apenas emite intenção via {@link FuncionarioAcoesCallback};
 * não abre janelas, não chama Rotas.
 *
 * @since v1.0
 */
public class FuncionarioAcoesCellFactory
        implements Callback<TableColumn<Funcionarios, Void>, TableCell<Funcionarios, Void>> {

    private final FuncionarioAcoesCallback callback;

    public FuncionarioAcoesCellFactory(FuncionarioAcoesCallback callback) {
        this.callback = callback;
    }

    @Override
    public TableCell<Funcionarios, Void> call(TableColumn<Funcionarios, Void> param) {
        return new TableCell<>() {

            private final Button btnDetalhes   = new Button("Detalhes");
            private final Button btnEditar     = new Button("Editar");
            private final Button btnAlternar   = new Button();
            private final HBox   container     = new HBox(6, btnDetalhes, btnEditar, btnAlternar);

            {
                container.setAlignment(Pos.CENTER_LEFT);
                btnDetalhes.getStyleClass().add("btn-secondary");
                btnEditar.getStyleClass().add("btn-primary");
                btnAlternar.getStyleClass().add("btn-secondary");

                btnDetalhes.setOnAction(e -> {
                    Funcionarios f = getTableRow() != null ? getTableRow().getItem() : null;
                    if (f != null && callback != null) callback.abrirDetalhes(f);
                });

                btnEditar.setOnAction(e -> {
                    Funcionarios f = getTableRow() != null ? getTableRow().getItem() : null;
                    if (f != null && callback != null) callback.abrirEdicao(f);
                });

                btnAlternar.setOnAction(e -> {
                    Funcionarios f = getTableRow() != null ? getTableRow().getItem() : null;
                    if (f != null && callback != null) callback.alternarStatus(f);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                Funcionarios f = getTableRow() != null ? getTableRow().getItem() : null;
                if (f != null) {
                    boolean ativo = Boolean.TRUE.equals(f.ativo());
                    btnAlternar.setText(ativo ? "Desativar" : "Ativar");
                }
                setGraphic(container);
            }
        };
    }
}