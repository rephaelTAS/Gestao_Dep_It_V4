package com.ossobo.gestaoDepIt;

import com.ossobo.winterfx.bootstrap.WinterApplication;

/**
 * 🎯 Launcher - Ponto de entrada com inicialização correta do WinterFX
 *
 * v3.0 - Migrado de NexusFX para WinterFX
 *
 * Responsabilidades:
 * - Inicializar o framework WinterFX
 * - Iniciar a aplicação JavaFX
 * - Configurar scan de pacotes automaticamente
 */
public class Launcher {

    public static void main(String[] args) {
        System.out.println("🏔️ Iniciando Gestão de TI via Launcher...");
        System.out.println("❄️ WinterFX v13.1.5");
        System.out.println("📁 Modo: Dual Database (SQLite + MySQL)");

        // ✅ WinterFX: Inicializa e lança a aplicação
        WinterApplication.run(Main.class);
    }
}