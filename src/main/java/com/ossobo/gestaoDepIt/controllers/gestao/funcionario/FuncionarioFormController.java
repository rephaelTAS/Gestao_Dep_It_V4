/*
 * FuncionarioFormController v1.4
 *
 * Controlador de Formulário de Funcionário (Criação e Edição).
 *
 * v1.4: CORRIGIDO — initialize() NÃO reseta mais os dados
 *       Reset ocorre no fecharJanela() ou via resetFormulario()
 *       setFuncionario() SEMPRE aplica (sem verificação de FXML pronto)
 *       Dados sobrevivem ao ciclo initialize → callback → render
 */
package com.ossobo.gestaoDepIt.controllers.gestao.funcionario;

import com.ossobo.gestaoDepIt.db.models.Funcionarios;
import com.ossobo.gestaoDepIt.db.services.FuncionariosService;
import com.ossobo.nexusfx.NexusFX;
import com.ossobo.nexusfx.di.annotations.Controller;
import com.ossobo.nexusfx.di.annotations.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Controller
public class FuncionarioFormController {

    private static final Logger logger = LoggerFactory.getLogger(FuncionarioFormController.class);

    @Inject
    private FuncionariosService funcionariosService;

    private static final String[] DEPARTAMENTOS = {
            "Departamento Informático",
            "Departamento Industrial",
            "Departamento Financeiro",
            "Departamento Plantação",
            "Departamento Oficina",
            "Departamento RH",
            "Departamento Compras e Logística",
            "Direção Geral"
    };

    @FXML private TextField codDepField;
    @FXML private TextField nomeField;
    @FXML private TextField funcaoField;
    @FXML private TextField localTrabalhoField;
    @FXML private TextField emailField;
    @FXML private TextField telefoneField;
    @FXML private ComboBox<String> departamentoCombo;
    @FXML private CheckBox ativoCheckBox;
    @FXML private Label tituloLabel;
    @FXML private Label labelInfoImagem;
    @FXML private Label mensagemErroLabel;
    @FXML private ImageView imagePreview;

    private Funcionarios funcionario;
    private boolean modoEdicao = false;
    private byte[] imagemBytes;
    private String tipoImagem;
    private boolean inicializado = false;

    // =========================================================================
    // CICLO DE VIDA
    // =========================================================================

    @FXML
    public void initialize() {
        logger.info(">>> Form.initialize() — nomeField={}", nomeField != null);

        carregarDepartamentos();
        ocultarErro();

        if (imagePreview != null) {
            imagePreview.setVisible(false);
            imagePreview.setManaged(false);
        }

        // Só configura como "Novo" se não estiver em modo edição
        if (!modoEdicao && tituloLabel != null) {
            tituloLabel.setText("Novo Funcionário");
            if (codDepField != null) {
                codDepField.setDisable(false);
                codDepField.clear();
            }
        }

        inicializado = true;

        // Se já temos dados, renderiza agora
        if (funcionario != null && modoEdicao) {
            preencherCampos();
        }
    }

    private void carregarDepartamentos() {
        if (departamentoCombo == null) return;
        departamentoCombo.getItems().clear();
        departamentoCombo.getItems().addAll(DEPARTAMENTOS);
        departamentoCombo.getSelectionModel().selectFirst();
    }

    // =========================================================================
    // INJEÇÃO DE DADOS
    // =========================================================================

    public void setFuncionario(Funcionarios funcionario) {
        logger.info(">>> Form.setFuncionario({})", funcionario != null ? funcionario.getCodDep() : "null");

        this.funcionario = funcionario;
        this.modoEdicao = true;

        if (inicializado) {
            preencherCampos();
        }
        // Se não inicializado, o initialize() chamará preencherCampos() no final
    }

    public void setModoEdicao(boolean edicao) {
        this.modoEdicao = edicao;
        if (inicializado && !edicao && tituloLabel != null) {
            tituloLabel.setText("Novo Funcionário");
            if (codDepField != null) codDepField.setDisable(false);
        }
    }

    private void preencherCampos() {
        if (funcionario == null || nomeField == null) return;

        logger.info(">>> Form.preencherCampos() — {}", funcionario.getNome());

        tituloLabel.setText("Editar Funcionário");
        codDepField.setText(funcionario.getCodDep());
        codDepField.setDisable(true);
        nomeField.setText(funcionario.getNome());
        funcaoField.setText(funcionario.getFuncao() != null ? funcionario.getFuncao() : "");
        departamentoCombo.setValue(funcionario.getDepartamento());
        localTrabalhoField.setText(funcionario.getLocalTrabalho() != null ? funcionario.getLocalTrabalho() : "");
        emailField.setText(funcionario.getEmail() != null ? funcionario.getEmail() : "");
        telefoneField.setText(funcionario.getTelefone() != null ? funcionario.getTelefone() : "");
        ativoCheckBox.setSelected(funcionario.getAtivo() != null && funcionario.getAtivo());

        if (funcionario.temImagemPerfil()) {
            this.imagemBytes = funcionario.getImagemPerfil();
            this.tipoImagem = funcionario.getTipoImagem();
            exibirPreviewImagem(imagemBytes);
            labelInfoImagem.setText(funcionario.getTamanhoImagemFormatado());
        } else {
            this.imagemBytes = null;
            this.tipoImagem = null;
            labelInfoImagem.setText("Nenhuma imagem");
        }
    }

    // =========================================================================
    // IMAGEM
    // =========================================================================

    @FXML
    private void handleSelecionarImagem() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Imagens", "*.png", "*.jpg", "*.jpeg"));
        fileChooser.setTitle("Selecionar Foto de Perfil");

        Stage stage = (Stage) nomeField.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            try {
                if (file.length() > 2 * 1024 * 1024) {
                    exibirErro("Imagem muito grande. Tamanho máximo: 2MB");
                    return;
                }
                this.imagemBytes = lerBytes(file);
                this.tipoImagem = extrairExtensao(file.getName());
                exibirPreviewImagem(imagemBytes);
                labelInfoImagem.setText(file.getName() + " (" + formatarTamanho(file.length()) + ")");
                ocultarErro();
            } catch (IOException e) {
                logger.error("Erro ao ler imagem", e);
                exibirErro("Erro ao carregar imagem: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleRemoverImagem() {
        this.imagemBytes = null;
        this.tipoImagem = null;
        imagePreview.setImage(null);
        imagePreview.setVisible(false);
        imagePreview.setManaged(false);
        labelInfoImagem.setText("Nenhuma imagem selecionada");
    }

    private byte[] lerBytes(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            return fis.readAllBytes();
        }
    }

    private String extrairExtensao(String nomeArquivo) {
        int dot = nomeArquivo.lastIndexOf('.');
        return dot > 0 ? nomeArquivo.substring(dot + 1).toLowerCase() : "png";
    }

    private void exibirPreviewImagem(byte[] bytes) {
        try {
            Image image = new Image(new ByteArrayInputStream(bytes));
            imagePreview.setImage(image);
            imagePreview.setVisible(true);
            imagePreview.setManaged(true);
        } catch (Exception e) {
            logger.warn("Erro ao exibir preview da imagem", e);
        }
    }

    private String formatarTamanho(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }

    // =========================================================================
    // SALVAR
    // =========================================================================

    @FXML
    private void handleSalvar() {
        if (!validarCampos()) return;

        try {
            Funcionarios func = construirFuncionario();

            if (modoEdicao) {
                funcionariosService.atualizarFuncionario(func);
                if (imagemBytes != null) {
                    funcionariosService.atualizarImagemPerfil(func.getCodDep(), imagemBytes, tipoImagem);
                }
                NexusFX.alerts().info("Sucesso", "Funcionário atualizado com sucesso!", "Funcionário");
            } else {
                if (funcionariosService.funcionarioExiste(func.getCodDep())) {
                    exibirErro("Já existe um funcionário com o código: " + func.getCodDep());
                    return;
                }
                funcionariosService.criarFuncionario(func);
                if (imagemBytes != null) {
                    funcionariosService.atualizarImagemPerfil(func.getCodDep(), imagemBytes, tipoImagem);
                }
                NexusFX.alerts().info("Sucesso", "Funcionário criado com sucesso!", "Funcionário");
            }

            fecharJanela();

        } catch (IllegalArgumentException e) {
            logger.warn("Validação de negócio: {}", e.getMessage());
            exibirErro(e.getMessage());
        } catch (Exception e) {
            logger.error("Erro ao salvar funcionário", e);
            exibirErro("Erro ao salvar: " + e.getMessage());
        }
    }

    private Funcionarios construirFuncionario() {
        Funcionarios func = new Funcionarios();
        func.setCodDep(codDepField.getText().trim());
        func.setNome(nomeField.getText().trim());
        func.setFuncao(funcaoField.getText().trim());
        func.setDepartamento(departamentoCombo.getValue());
        func.setLocalTrabalho(strOrNull(localTrabalhoField));
        func.setEmail(strOrNull(emailField));
        func.setTelefone(strOrNull(telefoneField));
        func.setAtivo(ativoCheckBox.isSelected());

        if (imagemBytes != null) {
            func.setImagemPerfil(imagemBytes);
            func.setTipoImagem(tipoImagem);
        }
        return func;
    }

    private String strOrNull(TextField field) {
        String val = field.getText();
        return val != null && !val.trim().isEmpty() ? val.trim() : null;
    }

    // =========================================================================
    // VALIDAÇÃO
    // =========================================================================

    private boolean validarCampos() {
        ocultarErro();

        if (isEmpty(codDepField)) {
            exibirErro("Código do departamento é obrigatório");
            codDepField.requestFocus();
            return false;
        }
        if (isEmpty(nomeField)) {
            exibirErro("Nome completo é obrigatório");
            nomeField.requestFocus();
            return false;
        }
        if (isEmpty(funcaoField)) {
            exibirErro("Função é obrigatória");
            funcaoField.requestFocus();
            return false;
        }
        if (departamentoCombo.getValue() == null) {
            exibirErro("Departamento é obrigatório");
            departamentoCombo.requestFocus();
            return false;
        }

        String email = emailField.getText() != null ? emailField.getText().trim() : "";
        if (!email.isEmpty() && !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            exibirErro("Email inválido");
            emailField.requestFocus();
            return false;
        }

        return true;
    }

    private boolean isEmpty(TextField field) {
        return field.getText() == null || field.getText().trim().isEmpty();
    }

    // =========================================================================
    // CANCELAR
    // =========================================================================

    @FXML
    private void handleCancelar() {
        fecharJanela();
    }

    // =========================================================================
    // UTILITÁRIOS
    // =========================================================================

    private void exibirErro(String mensagem) {
        Platform.runLater(() -> {
            mensagemErroLabel.setText(mensagem);
            mensagemErroLabel.setVisible(true);
            mensagemErroLabel.setManaged(true);
        });
    }

    private void ocultarErro() {
        mensagemErroLabel.setVisible(false);
        mensagemErroLabel.setManaged(false);
    }

    private void fecharJanela() {
        // Reset para próxima abertura
        this.modoEdicao = false;
        this.funcionario = null;
        this.imagemBytes = null;
        this.tipoImagem = null;
        this.inicializado = false;

        Stage stage = (Stage) nomeField.getScene().getWindow();
        stage.close();
    }
}