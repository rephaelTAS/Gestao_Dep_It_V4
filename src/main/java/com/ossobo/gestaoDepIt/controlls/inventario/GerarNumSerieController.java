package com.ossobo.gestaoDepIt.controlls.inventario;

import com.ossobo.gestaoDepIt.config.ViewConstant;
import com.ossobo.winterfx.anotations.Controller;
import com.ossobo.winterfx.anotations.GetMapping;
import com.ossobo.winterfx.anotations.RequestMapping;
import com.ossobo.winterfx.anotations.RouteVar;
import com.ossobo.winterfx.router.Rotas;
import com.ossobo.winterfx.router.model.Params;
import com.ossobo.winterfx.router.model.ResponseData;
import com.ossobo.winterfx.view.anotations.RegisterView;
import com.ossobo.winterfx.view.controller.WinterFXController;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

/**
 * GerarNumSerieController v1.0
 *
 * Janela flutuante: usuário informa o departamento e o controller solicita
 * ao NumeroSerieGenerator (via rota) o próximo número de série da combinação
 * (departamento + marca do produto).
 *
 * Fluxo:
 *   1. Form principal abre esta janela via Rotas.put("inventario-form/numserie/produto",
 *      Params.with("sku", sku).and("marca", marca)) ANTES do showAndWait().
 *   2. Aqui, txtMarca é preenchido a partir do que a rota recebeu.
 *   3. Usuário digita o departamento e clica em Salvar.
 *   4. Chamamos Rotas.put("numserie/service/proximo",
 *         Params.with("departamento", dep).and("marca", marca)).
 *   5. Devolvemos o número gerado ao form principal via
 *      Rotas.put("inventario-form/numserie/resultado",
 *         Params.with("numeroSerie", numero)).
 *   6. Fechamos a janela.
 *
 * Regras WinterFX:
 *   - @Controller(proxy = false)
 *   - FXML sem fx:controller nem onAction
 *   - binding por fx:id (btn_salvar → public void btn_salvar(ActionEvent))
 *
 * @since v1.0
 */
@Controller(proxy = false)
@RequestMapping("gerar-num-serie")
@RegisterView(
        id = ViewConstant.Inventario.GERAR_NUM_SERIE,
        fxml = "/META-INF/gestaoDepIt/fxmls/inventario/GerarNumSeries.fxml",
        title = "Gerar Número de Série",
        width = 480,
        height = 220,
        centered = true,
        resizable = false,
        primaryCss = "/META-INF/gestaoDepIt/css/inventario/InventarioEquipamentoFormView.css"
)
public class GerarNumSerieController implements Initializable, WinterFXController {

    @FXML private Label     labelTitulo;
    @FXML private TextField txtDepartamento;
    @FXML private javafx.scene.control.Button btn_cancelar;
    @FXML private javafx.scene.control.Button btn_salvar;

    /** Marca derivada do produto selecionado no form principal. */
    private String marca;

    // ===== INITIALIZE =====

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> {
            if (labelTitulo != null) {
                labelTitulo.setText("Gerar Número de Série");
            }
        });
    }

    // ===== ROTA DE ENTRADA (chamada pelo form principal antes do show) =====

    /**
     * Recebe a marca do produto selecionado. O form principal chama isso
     * via Rotas.put ANTES de exibir a janela.
     */
    @GetMapping("marca")
    public ResponseData definirMarca(@RouteVar("marca") String marcaRecebida) {
        this.marca = marcaRecebida;
        return ResponseData.success();
    }

    // ===== AÇÕES =====

    public void btn_salvar(ActionEvent event) {
        String departamento = txtDepartamento.getText();
        if (departamento == null || departamento.isBlank()) {
            mensagemErro("Informe o departamento.");
            return;
        }
        if (marca == null || marca.isBlank()) {
            mensagemErro("Marca do produto não foi informada. Reabra esta janela.");
            return;
        }

        btn_salvar.setDisable(true);
        btn_salvar.setText("Gerando...");

        CompletableFuture.runAsync(() -> {
            var resp = Rotas.put("numserie/service/proximo",
                    Params.with("departamento", departamento.trim())
                            .and("marca", marca));
            Platform.runLater(() -> {
                btn_salvar.setDisable(false);
                btn_salvar.setText("Salvar");

                if (resp == null || !resp.isSuccess()) {
                    mensagemErro(resp != null
                            ? (resp.getFirstError() != null ? resp.getFirstError() : resp.getMessage())
                            : "Falha ao gerar número de série.");
                    return;
                }
                String numero = resp.getData("numeroSerie", String.class);
                if (numero == null || numero.isBlank()) {
                    mensagemErro("Número gerado veio vazio.");
                    return;
                }

                // Devolve para o form principal
                Rotas.exec("inventario-form/numserie/aplicar",
                        Params.with("numeroSerie", numero));

                fecharJanela();
            });
        });
    }

    public void btn_cancelar(ActionEvent event) {
        fecharJanela();
    }

    // ===== AUXILIARES =====

    private void mensagemErro(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle("Aviso");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private void fecharJanela() {
        if (btn_cancelar != null && btn_cancelar.getScene() != null) {
            ((Stage) btn_cancelar.getScene().getWindow()).close();
        }
    }
}