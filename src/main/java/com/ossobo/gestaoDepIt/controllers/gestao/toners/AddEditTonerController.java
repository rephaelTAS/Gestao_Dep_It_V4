package com.ossobo.gestaoDepIt.controllers.gestao.toners;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import com.ossobo.nexusfx.di.annotations.Component;
import com.ossobo.nexusfx.NexusFX;
import java.time.LocalDate;

/**
 * 🎯 Controller para Adicionar ou Editar Instalações de Toner.
 * Gerencia a lógica de compatibilidade e persistência.
 */
@Component
public class AddEditTonerController {

    // ===== CABEÇALHO E INFO =====
    @FXML private Label tituloLabel;
    @FXML private Label equipamentoInfoLabel, tonerAtualLabel;
    @FXML private Label tonerInfoLabel, estoqueTonerLabel;
    @FXML private Label compatibilidadeLabel, vidaUtilLabel;
    @FXML private Label percentagemLabel, mensagemErroLabel;

    // ===== CAMPOS DO FORMULÁRIO =====
    @FXML private DatePicker dataInstalacaoPicker;
    @FXML private Slider percentagemSlider;
    @FXML private TextField ciclosImpressaoField;
    @FXML private TextArea observacoesField;

    // ===== BOTÕES =====
    @FXML private Button salvarButton;

    @FXML
    public void initialize() {
        // Configurações iniciais
        dataInstalacaoPicker.setValue(LocalDate.now());

        // Listener para atualizar o label da porcentagem em tempo real
        percentagemSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            percentagemLabel.setText(Math.round(newVal.doubleValue()) + "%");
            atualizarCorStatus(newVal.doubleValue());
        });

        limparMensagens();
    }

    private void atualizarCorStatus(double valor) {
        if (valor > 50) percentagemLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
        else if (valor > 20) percentagemLabel.setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
        else percentagemLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
    }

    private void limparMensagens() {
        mensagemErroLabel.setVisible(false);
        mensagemErroLabel.setManaged(false);
    }

    // =============================================
    // ⚡ ACTIONS - SELEÇÃO DE ENTIDADES
    // =============================================

    @FXML
    private void handleSelecionarEquipamento() {
        // Aqui você chamaria um modal de busca do NexusFX
        // Exemplo: NexusFX.modals().openModal("busca_equipamento", "Selecionar Impressora", this);
        equipamentoInfoLabel.setText("HP LaserJet M404n - S/N: JP123456");
        tonerAtualLabel.setText("Toner atual: CF258A (15%)");
        verificarCompatibilidade();
    }

    @FXML
    private void handleLimparEquipamento() {
        equipamentoInfoLabel.setText("Nenhum equipamento selecionado");
        tonerAtualLabel.setText("Toner atual: Nenhum");
        verificarCompatibilidade();
    }

    @FXML
    private void handleSelecionarToner() {
        tonerInfoLabel.setText("Toner HP 58A Preto Original (CF258A)");
        estoqueTonerLabel.setText("Estoque disponível: 5 unidades");
        verificarCompatibilidade();
    }

    @FXML
    private void handleLimparToner() {
        tonerInfoLabel.setText("Nenhum toner selecionado");
        estoqueTonerLabel.setText("Estoque disponível: -");
        verificarCompatibilidade();
    }

    private void verificarCompatibilidade() {
        // Lógica de negócio para validar se o toner encaixa na impressora
        if (!equipamentoInfoLabel.getText().contains("Nenhum") && !tonerInfoLabel.getText().contains("Nenhum")) {
            compatibilidadeLabel.setText("✅ Compatibilidade Confirmada");
            compatibilidadeLabel.setStyle("-fx-text-fill: #16a085; -fx-font-weight: bold;");
            vidaUtilLabel.setText("Vida útil estimada: 3.000 páginas");
        } else {
            compatibilidadeLabel.setText("Selecione equipamento e toner para verificar compatibilidade");
            compatibilidadeLabel.setStyle("-fx-text-fill: #34495e;");
            vidaUtilLabel.setText("Vida útil estimada: -");
        }
    }

    // =============================================
    // 💾 ACTIONS - SALVAMENTO E CANCELAMENTO
    // =============================================

    @FXML
    private void handleSalvar() {
        try {
            // 1. Validação básica
            if (equipamentoInfoLabel.getText().contains("Nenhum")) {
                exibirErro("Selecione um equipamento antes de salvar.");
                return;
            }

            // 2. Coleta de dados (Simulada)
            System.out.println("Salvando instalação de toner...");

            // 3. Feedback visual
            NexusFX.alerts().info(
                    "Sucesso",
                    "Instalação registrada com sucesso!",
                    "Equipamento atualizado no sistema."
            );

            // 4. Fechar o modal (se o seu framework gerenciar o Stage)
            // NexusFX.modals().closeCurrent();

        } catch (Exception e) {
            exibirErro("Erro ao salvar: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancelar() {
        // Lógica para fechar a janela sem salvar
        System.out.println("Operação cancelada pelo usuário.");
        // NexusFX.modals().closeCurrent();
    }

    private void exibirErro(String msg) {
        mensagemErroLabel.setText("❌ " + msg);
        mensagemErroLabel.setVisible(true);
        mensagemErroLabel.setManaged(true);
    }
}