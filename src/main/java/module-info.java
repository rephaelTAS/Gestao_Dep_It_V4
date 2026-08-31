module com.ossobo.gestaoDepIt {

    // ==========================================
    // DEPENDENCIAS EXTERNAS (REQUIRES)
    // ==========================================

    // === MODULOS JAVAFX ===
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires transitive javafx.graphics;
    requires transitive javafx.base;
    requires javafx.media;
    requires javafx.swing;
    requires javafx.web;

    // === MODULOS JAVA STANDARD ===
    requires java.prefs;
    requires java.sql;
    requires java.desktop;
    requires java.net.http;
    requires java.management;
    requires jdk.httpserver;
    requires jdk.xml.dom;

    // === MODULOS DE LOGGING ===
    requires org.slf4j;
    requires ch.qos.logback.classic;
    requires ch.qos.logback.core;

    // === MODULOS DE JSON E SERIALIZACAO ===
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.annotation;
    requires com.google.gson;

    // === MODULOS DE CRIPTOGRAFIA ===
    requires jbcrypt;

    // === MODULOS DE E-MAIL ===
    requires jakarta.mail;

    // === MODULOS APACHE POI (EXCEL) ===
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;
    requires com.google.common;
    requires java.naming;
    requires org.checkerframework.checker.qual;
    requires java.logging;

    // === NEXUSFX FRAMEWORK ===
    requires org.reflections;
    requires javassist;
    requires org.apache.commons.collections4;
    requires io.github.classgraph;
    requires com.ossobo.winterfx;


    // ==========================================
    // ABERTURAS PARA REFLEXAO (OPENS)
    // ==========================================

    // === PACOTE RAIZ ===
    opens com.ossobo.gestaoDepIt to com.ossobo.nexusfx, javafx.fxml, javafx.graphics;

    // === CONFIG ===
    opens com.ossobo.gestaoDepIt.config to com.ossobo.nexusfx, javafx.fxml;
    opens com.ossobo.gestaoDepIt.config.view to com.ossobo.nexusfx, javafx.fxml;

    // === UTILS ===
    opens com.ossobo.gestaoDepIt.utils to com.ossobo.nexusfx;



    // === DATABASE ===
    opens com.ossobo.gestaoDepIt.db.config to com.ossobo.nexusfx;
    opens com.ossobo.gestaoDepIt.db.models to com.ossobo.nexusfx, com.fasterxml.jackson.databind;
    opens com.ossobo.gestaoDepIt.db.enums to com.ossobo.nexusfx, com.fasterxml.jackson.databind;
    opens com.ossobo.gestaoDepIt.db.repositories to com.ossobo.nexusfx;
    opens com.ossobo.gestaoDepIt.db.repositories.exceptions to com.ossobo.nexusfx;
    opens com.ossobo.gestaoDepIt.db.services to com.ossobo.nexusfx;

    // === CONTROLLERS - LOGIN ===
    opens com.ossobo.gestaoDepIt.controllers.login to com.ossobo.nexusfx, javafx.fxml;

    // === CONTROLLERS - MAIN ===
    opens com.ossobo.gestaoDepIt.controllers.main to com.ossobo.nexusfx, javafx.fxml;
    opens com.ossobo.gestaoDepIt.controllers.main.sidebar to com.ossobo.nexusfx, javafx.fxml;

    // === CONTROLLERS - PROGRESS BAR ===
    opens com.ossobo.gestaoDepIt.controllers.progresBar to com.ossobo.nexusfx, javafx.fxml;

    // === CONTROLLERS - REPORTS ===
    opens com.ossobo.gestaoDepIt.controllers.reports to com.ossobo.nexusfx, javafx.fxml;

    // === CONTROLLERS - FUNCIONARIO ===
    opens com.ossobo.gestaoDepIt.controllers.gestao.funcionario to com.ossobo.nexusfx, javafx.fxml;

    // === CONTROLLERS - PESSOAL ===
    opens com.ossobo.gestaoDepIt.controllers.gestao.pessoal to com.ossobo.nexusfx, javafx.fxml;

    // === CONTROLLERS - RELATORIOS ===
    opens com.ossobo.gestaoDepIt.controllers.gestao.relatorios to com.ossobo.nexusfx, javafx.fxml;
    opens com.ossobo.gestaoDepIt.controllers.gestao.relatorios.model to javafx.base;

    // === CONTROLLERS - TONERS ===
    opens com.ossobo.gestaoDepIt.controllers.gestao.toners to com.ossobo.nexusfx, javafx.fxml;

    // === CONTROLLERS - VISUALIZAR ===
    opens com.ossobo.gestaoDepIt.controllers.gestao.visualizar to com.ossobo.nexusfx, javafx.fxml;

    opens com.ossobo.gestaoDepIt.controllers.gestao.gia to com.ossobo.nexusfx, javafx.fxml;

    // === CONTROLLERS - INVENTARIO ===
    opens com.ossobo.gestaoDepIt.controllers.gestao.equipamentos.inventario to com.ossobo.nexusfx, javafx.fxml;
    opens com.ossobo.gestaoDepIt.controllers.gestao.equipamentos.inventario.audit to com.ossobo.nexusfx;
    opens com.ossobo.gestaoDepIt.controllers.gestao.equipamentos.inventario.builders to javafx.base;
    opens com.ossobo.gestaoDepIt.controllers.gestao.equipamentos.inventario.dto to com.ossobo.nexusfx, javafx.base;
    opens com.ossobo.gestaoDepIt.controllers.gestao.equipamentos.inventario.historico to com.ossobo.nexusfx, javafx.fxml;
    opens com.ossobo.gestaoDepIt.controllers.gestao.equipamentos.inventario.service to com.ossobo.nexusfx;
    opens com.ossobo.gestaoDepIt.controllers.gestao.equipamentos.inventario.validators to com.ossobo.nexusfx;

    // === CONTROLLERS - ESTOQUE - CATALOGO PRODUTO ===
    opens com.ossobo.gestaoDepIt.controllers.gestao.estoque.catalugoproduto to com.ossobo.nexusfx, javafx.fxml;
    opens com.ossobo.gestaoDepIt.controllers.gestao.estoque.catalugoproduto.catalogoList to com.ossobo.nexusfx, javafx.fxml;
    opens com.ossobo.gestaoDepIt.controllers.gestao.estoque.catalugoproduto.strategies to com.ossobo.nexusfx, javafx.fxml;
    opens com.ossobo.gestaoDepIt.controllers.gestao.estoque.catalugoproduto.validators to javafx.fxml;


    // === CONTROLLERS - ESTOQUE - MOVIMENTACOES ===
    opens com.ossobo.gestaoDepIt.controllers.gestao.estoque.movimentacoes to com.ossobo.nexusfx, javafx.fxml;
    opens com.ossobo.gestaoDepIt.controllers.gestao.estoque.movimentacoes.detalhes to com.ossobo.nexusfx, javafx.fxml;
    opens com.ossobo.gestaoDepIt.controllers.gestao.estoque.movimentacoes.dialogs to com.ossobo.nexusfx, javafx.fxml;
    opens com.ossobo.gestaoDepIt.controllers.gestao.estoque.movimentacoes.dto to com.ossobo.nexusfx, javafx.base;
    opens com.ossobo.gestaoDepIt.controllers.gestao.estoque.movimentacoes.estoque to com.ossobo.nexusfx;
    opens com.ossobo.gestaoDepIt.controllers.gestao.estoque.movimentacoes.formview to com.ossobo.nexusfx;
    opens com.ossobo.gestaoDepIt.controllers.gestao.estoque.movimentacoes.service to com.ossobo.nexusfx;
    opens com.ossobo.gestaoDepIt.controllers.gestao.estoque.movimentacoes.util to javafx.fxml;
    opens com.ossobo.gestaoDepIt.controllers.gestao.equipamentos.inventario.mappers to com.ossobo.nexusfx, javafx.base;


    // ==========================================
    // EXPORTACOES PUBLICAS (EXPORTS)
    // ==========================================

    exports com.ossobo.gestaoDepIt;
    exports com.ossobo.gestaoDepIt.config;
    exports com.ossobo.gestaoDepIt.config.view;
    exports com.ossobo.gestaoDepIt.controllers.login;
    exports com.ossobo.gestaoDepIt.controllers.main;
    exports com.ossobo.gestaoDepIt.controllers.main.sidebar;
    exports com.ossobo.gestaoDepIt.controllers.progresBar;
    exports com.ossobo.gestaoDepIt.controllers.gestao.estoque.movimentacoes;
    exports com.ossobo.gestaoDepIt.controllers.gestao.estoque.movimentacoes.service;
    exports com.ossobo.gestaoDepIt.controllers.gestao.estoque.catalugoproduto;
    exports com.ossobo.gestaoDepIt.controllers.gestao.pessoal;
    exports com.ossobo.gestaoDepIt.controllers.gestao.visualizar;

    // Exports qualificados para NexusFX (DiContainer)
    exports com.ossobo.gestaoDepIt.db.config to com.ossobo.nexusfx;
    exports com.ossobo.gestaoDepIt.db.repositories to com.ossobo.nexusfx;
    exports com.ossobo.gestaoDepIt.db.repositories.exceptions to com.ossobo.nexusfx;
    exports com.ossobo.gestaoDepIt.db.services to com.ossobo.nexusfx;
    opens com.ossobo.gestaoDepIt.controllers.gestao.estoque.catalugoproduto.catalogoList.cellfactories to com.ossobo.nexusfx, javafx.fxml;
    exports com.ossobo.gestaoDepIt.db.config.event to com.ossobo.nexusfx;
    opens com.ossobo.gestaoDepIt.db.config.event to com.ossobo.nexusfx;
    exports com.ossobo.gestaoDepIt.ui.splash;
    opens com.ossobo.gestaoDepIt.ui.splash to com.ossobo.nexusfx, javafx.fxml;
    exports com.ossobo.gestaoDepIt.events;
    opens com.ossobo.gestaoDepIt.events to com.ossobo.nexusfx, javafx.fxml, javafx.graphics;
}