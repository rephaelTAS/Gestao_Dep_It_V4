package com.ossobo.gestaoDepIt.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.Optional;

/**
 * 🎯 UTILITÁRIOS JAVA FX - Funções comuns para UI
 */
public final class FXUtils {

    private FXUtils() {
        // Classe utilitária - não instanciável
    }

    /**
     * ✅ EXIBE ALERTA DE ERRO
     */
    public static void showErrorAlert(String title, String message) {
        showAlert(Alert.AlertType.ERROR, title, message);
    }

    /**
     * ✅ EXIBE ALERTA DE INFORMAÇÃO
     */
    public static void showInfoAlert(String title, String message) {
        showAlert(Alert.AlertType.INFORMATION, title, message);
    }

    /**
     * ✅ EXIBE ALERTA DE AVISO
     */
    public static void showWarningAlert(String title, String message) {
        showAlert(Alert.AlertType.WARNING, title, message);
    }

    /**
     * ✅ EXIBE ALERTA GENÉRICO
     */
    public static void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * ✅ EXIBE ALERTA DE CONFIRMAÇÃO
     */
    public static Optional<ButtonType> showConfirmationAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait();
    }

    /**
     * ✅ ABRE JANELA MODAL
     */
    public static void openModalWindow(Stage stage, Window owner) {
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(owner);
        stage.show();
    }

    /**
     * ✅ ABRE JANELA MODAL DE APLICAÇÃO
     */
    public static void openApplicationModal(Stage stage) {
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();
    }

    /**
     * ✅ CENTRALIZA JANELA NA TELA
     */
    public static void centerWindow(Stage stage) {
        stage.centerOnScreen();
    }

    /**
     * ✅ CENTRALIZA JANELA RELATIVA AO OWNER
     */
    public static void centerWindowRelativeTo(Stage stage, Window owner) {
        if (owner != null) {
            stage.setX(owner.getX() + owner.getWidth() / 2 - stage.getWidth() / 2);
            stage.setY(owner.getY() + owner.getHeight() / 2 - stage.getHeight() / 2);
        } else {
            centerWindow(stage);
        }
    }

    /**
     * ✅ EXIBE DIALOGO COM TAMANHO MÍNIMO
     */
    public static void showDialogWithMinHeight(Dialog<?> dialog, double minHeight) {
        dialog.getDialogPane().setMinHeight(minHeight);
        dialog.showAndWait();
    }

    /**
     * ✅ EXECUTA NA THREAD DO JAVA FX
     */
    public static void runOnFXThread(Runnable task) {
        if (javafx.application.Platform.isFxApplicationThread()) {
            task.run();
        } else {
            javafx.application.Platform.runLater(task);
        }
    }

    /**
     * ✅ VERIFICA SE É THREAD DO JAVA FX
     */
    public static boolean isFXThread() {
        return javafx.application.Platform.isFxApplicationThread();
    }

    /**
     * ✅ FORMATA BYTES PARA TAMANHO LEGÍVEL
     */
    public static String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }

    /**
     * ✅ LIMITA TEXTO COM ELIPSES
     */
    public static String truncateText(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }

    /**
     * ✅ FORMATA NÚMERO COM SEPARADORES
     */
    public static String formatNumber(long number) {
        return String.format("%,d", number);
    }

    /**
     * ✅ FORMATA PORCENTAGEM
     */
    public static String formatPercentage(double value) {
        return String.format("%.1f%%", value * 100);
    }

    /**
     * ✅ VALIDA EMAIL SIMPLES
     */
    public static boolean isValidEmail(String email) {
        if (email == null) return false;
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
}