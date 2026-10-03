package com.ossobo.gestaoDepIt.controlls.funcionario.funcionarioList.cellfactories;

import com.ossobo.gestaoDepIt.db.models.Funcionarios;
import javafx.application.Platform;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Callback;

import java.io.ByteArrayInputStream;

/**
 * FuncionarioFotoCellFactory v1.0
 *
 * Responsabilidade: renderizar a coluna Foto com um ImageView de 40x40,
 *                   carregando os bytes da imagem de perfil de forma
 *                   assíncrona via callback.
 *
 * Cache em memória por codDep evita recarregar a mesma imagem em
 * células recicladas.
 *
 * @since v1.0
 */
public class FuncionarioFotoCellFactory
        implements Callback<TableColumn<Funcionarios, Void>, TableCell<Funcionarios, Void>> {

    private static final int TAMANHO = 40;

    private final FuncionarioAcoesCallback callback;
    private final java.util.Map<String, Image> cache = new java.util.concurrent.ConcurrentHashMap<>();

    public FuncionarioFotoCellFactory(FuncionarioAcoesCallback callback) {
        this.callback = callback;
    }

    @Override
    public TableCell<Funcionarios, Void> call(TableColumn<Funcionarios, Void> param) {
        return new TableCell<>() {

            private final ImageView imageView = new ImageView();

            {
                imageView.setFitWidth(TAMANHO);
                imageView.setFitHeight(TAMANHO);
                imageView.setPreserveRatio(true);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                Funcionarios f = getTableRow() != null ? getTableRow().getItem() : null;
                if (f == null || !f.temImagemPerfil()) {
                    setGraphic(null);
                    return;
                }
                String cod = f.codDep();

                Image emCache = cache.get(cod);
                if (emCache != null) {
                    imageView.setImage(emCache);
                    setGraphic(imageView);
                    return;
                }

                setGraphic(null);
                callback.carregarImagem(cod).thenAccept(bytes -> {
                    if (bytes == null || bytes.length == 0) return;
                    Image img = new Image(new ByteArrayInputStream(bytes),
                            TAMANHO, TAMANHO, true, true);
                    cache.put(cod, img);
                    Platform.runLater(() -> {
                        // Reaplica apenas se a célula ainda representa o mesmo funcionário.
                        Funcionarios atual = getTableRow() != null ? getTableRow().getItem() : null;
                        if (atual != null && cod.equals(atual.codDep())) {
                            imageView.setImage(img);
                            setGraphic(imageView);
                        }
                    });
                });
            }
        };
    }
}