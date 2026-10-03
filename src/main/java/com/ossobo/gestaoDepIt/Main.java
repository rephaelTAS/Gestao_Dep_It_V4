package com.ossobo.gestaoDepIt;

import com.ossobo.gestaoDepIt.db.config.DatabaseInitializer;
import com.ossobo.winterfx.bootstrap.WinterApplication;
import com.ossobo.winterfx.router.Rotas;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        // ✅ 1. Inicializa o WinterFX
        WinterApplication.getInstance()
                .withScanPackages("com.ossobo.gestaoDepIt")
                .withMainView("loginform")
                .autoStart(primaryStage);

        // ✅ 2. Inicializa o banco de dados
        try {
            var diContainer = WinterApplication.getInstance().getDiContainer();
            var databaseInitializer = diContainer.getBean(DatabaseInitializer.class);
            if (databaseInitializer != null) {
                databaseInitializer.init();
            } else {
                System.err.println("⚠️ DatabaseInitializer não encontrado no DI");
            }
        } catch (Exception e) {
            System.err.println("❌ Erro ao inicializar banco de dados: " + e.getMessage());
            e.printStackTrace();
        }

        // ✅ 3. Testa rotas
        testarRotas();
    }

    private void testarRotas() {
        // Sem cast — a fachada devolve ResponseData por contrato
        var resposta = Rotas.get("funcionario/service/todos");

        if (resposta.isSuccess()) {
            System.out.println("✅ Rotas funcionando: " + resposta.getMessage());
            System.out.println("   Total no banco: " + resposta.getDataInt("total"));
        } else {
            System.err.println("⚠️ Rota respondeu com erro: " + resposta.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}