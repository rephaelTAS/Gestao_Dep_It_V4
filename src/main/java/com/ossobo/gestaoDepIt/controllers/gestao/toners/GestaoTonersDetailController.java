package com.ossobo.gestaoDepIt.controllers.gestao.toners;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Window;
import com.ossobo.nexusfx.NexusFX;

public class GestaoTonersDetailController {

    // --- Elementos do Cabeçalho ---
    @FXML private Button voltarButton;
    @FXML private Label tituloLabel;
    @FXML private Label subtituloLabel;
    @FXML private Button editarButton;
    @FXML private Button atualizarUsoButton;
    @FXML private Button substituirButton;

    // --- Dados da Instalação ---
    @FXML private Label detailId;
    @FXML private Label detailEquipamento;
    @FXML private Label detailToner;
    @FXML private Label detailDataInstalacao;
    @FXML private Label detailDiasInstalado;
    @FXML private Label detailUsuario;

    // --- Status do Toner ---
    @FXML private Label detailPercentagem;
    @FXML private Label detailCiclos;
    @FXML private Label detailStatus;
    @FXML private Label detailVidaUtil;
    @FXML private TextArea detailObservacoes;

    // --- Gráficos e Estatísticas ---
    @FXML private ProgressBar progressBar;
    @FXML private Label progressLabel;
    @FXML private Label alertaLabel;
    @FXML private Label statCiclosDia;
    @FXML private Label statProjecao;
    @FXML private Label statHistorico;

    // --- Ações Rápidas ---
    @FXML private Button btnRegistrarUso;
    @FXML private Button btnAjustarPercentagem;
    @FXML private Button btnVerHistorico;
    @FXML private Button btnMarcarEsgotado;
    @FXML private Label mensagemStatusLabel;

    // Objeto de dados (Substitua 'TonerModel' pelo nome da sua classe de modelo)
    private Object currentToner;

    @FXML
    public void initialize() {
        // Inicializações que não dependem dos dados injetados
        mensagemStatusLabel.setManaged(false);
    }

    /**
     * Método para injetar os dados quando o modal ou tela abre.
     * No NexusFX, você chamaria isso no data passing do ViewManager.
     */
    public void setTonerData(Object toner) {
        this.currentToner = toner;
        preencherCampos();
    }

    private void preencherCampos() {
        // Exemplo de preenchimento (ajuste os getters conforme seu modelo)
        detailId.setText("TNR-2026-001");
        tituloLabel.setText("Impressora Financeiro - Piso 1");
        subtituloLabel.setText("ID: TNR-2026-001");

        detailEquipamento.setText("HP LaserJet M404n");
        detailToner.setText("CF258A (Preto)");
        detailDataInstalacao.setText("01/01/2026");
        detailDiasInstalado.setText("7 dias");
        detailUsuario.setText("Admin_Sistemas");

        double progresso = 0.75; // 75%
        updateProgressBar(progresso);

        detailPercentagem.setText("75%");
        detailCiclos.setText("1.250");
        detailStatus.setText("EM USO");
        detailVidaUtil.setText("Estimada em 4.500 ciclos");

        detailObservacoes.setText("Instalado durante a manutenção preventiva do setor.");
    }

    private void updateProgressBar(double progress) {
        progressBar.setProgress(progress);
        progressLabel.setText((int)(progress * 100) + "%");

        // Lógica de cor baseada no nível
        if (progress < 0.2) {
            progressBar.setStyle("-fx-accent: #e74c3c;"); // Vermelho
            alertaLabel.setText("⚠️ Nível Crítico! Providencie a substituição.");
        } else if (progress < 0.5) {
            progressBar.setStyle("-fx-accent: #f39c12;"); // Laranja
            alertaLabel.setText("");
        } else {
            progressBar.setStyle("-fx-accent: #27ae60;"); // Verde
            alertaLabel.setText("");
        }
    }

    // --- Handlers de Ação ---

    @FXML
    private void handleVoltar(ActionEvent event) {
        // Se for um modal, fecha a janela
        Window window = ((Node) event.getSource()).getScene().getWindow();
        window.hide();
    }

    @FXML
    private void handleEditar(ActionEvent event) {
        Window owner = ((Node) event.getSource()).getScene().getWindow();

        // ✅ CORRETO - substituir modals() por dialogs().openWindowModal()
        NexusFX.dialogs().openModalWithController(
                "addeditartoner",
                "Editar Instalação",
                owner,
                (AddEditTonerController controller) -> {
                    // Passa os dados atuais para o controller de edição
                    if (currentToner != null) {
                    }
                }
        );
    }

    @FXML
    private void handleAtualizarUso(ActionEvent event) {
        mostrarMensagem("Sincronizando contadores do equipamento...", "#3498db");
    }

    @FXML
    private void handleSubstituir(ActionEvent event) {
        Window owner = ((Node) event.getSource()).getScene().getWindow();
        // Lógica para abrir workflow de substituição
    }

    @FXML
    private void handleRegistrarUso() {
        mostrarMensagem("Uso registrado com sucesso.", "#27ae60");
    }

    @FXML
    private void handleAjustarPercentagem() {
        // Abrir dialog de input numérico
    }

    @FXML
    private void handleVerHistorico() {
        // Trocar para view de logs
    }

    @FXML
    private void handleMarcarEsgotado() {
        updateProgressBar(0.0);
        detailStatus.setText("ESGOTADO");
        mostrarMensagem("Status alterado para ESGOTADO.", "#e74c3c");
    }

    private void mostrarMensagem(String texto, String corHex) {
        mensagemStatusLabel.setText(texto);
        mensagemStatusLabel.setStyle("-fx-text-fill: " + corHex + "; -fx-font-size: 12px;");
        mensagemStatusLabel.setVisible(true);
        mensagemStatusLabel.setManaged(true);
    }
}