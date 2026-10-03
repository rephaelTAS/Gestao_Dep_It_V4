module com.ossobo.Gestao_Dep_It_V4 {

    // ============================================================
    // REQUIRES
    // ============================================================

    // JavaFX
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    // Scanner e proxy
    requires io.github.classgraph;
    requires net.bytebuddy;

    // UI Extras
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;

    // Banco
    requires java.sql;

    // Framework
    requires com.ossobo.winterfx;
    requires jbcrypt;
    requires com.github.librepdf.openpdf;
    requires com.google.gson;
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;
    requires java.desktop;

    // ============================================================
    // OPENS — reflexão do WinterFX (instanciação) + FXMLLoader (@FXML)
    // ============================================================
    //
    // Regra: um pacote com classe @Controller/@RegisterView precisa abrir
    // para AMBOS os atores:
    //   - com.ossobo.winterfx → instancia o controller por reflexão
    //   - javafx.fxml         → injeta os campos @FXML
    //
    // Pacotes sem @FXML (Component/Service/Repository) abrem só para winterfx.
    //
    // ATENÇÃO JPMS: opens X to A e opens X to B são declarações DIFERENTES
    // e o compilador RECUSA duplicata com destinos distintos. Sempre unifique
    // numa linha: opens X to A, B;

    // Raiz / shell
    opens com.ossobo.gestaoDepIt       to javafx.fxml, com.ossobo.winterfx;
    opens com.ossobo.gestaoDepIt.login to javafx.fxml, com.ossobo.winterfx;
    opens com.ossobo.gestaoDepIt.main  to javafx.fxml, com.ossobo.winterfx;

    // Camadas de dados (DI — sem @FXML)
    opens com.ossobo.gestaoDepIt.db.config                  to com.ossobo.winterfx;
    opens com.ossobo.gestaoDepIt.db.config.event            to com.ossobo.winterfx;
    opens com.ossobo.gestaoDepIt.db.enums                   to com.ossobo.winterfx;
    opens com.ossobo.gestaoDepIt.db.models                  to com.ossobo.winterfx;
    opens com.ossobo.gestaoDepIt.db.repositories            to com.ossobo.winterfx;
    opens com.ossobo.gestaoDepIt.db.repositories.exceptions to com.ossobo.winterfx;
    opens com.ossobo.gestaoDepIt.db.services                to com.ossobo.winterfx;
    opens com.ossobo.gestaoDepIt.db.sync                    to com.ossobo.winterfx;
    opens com.ossobo.gestaoDepIt.db.documentos              to com.ossobo.winterfx;
    opens com.ossobo.gestaoDepIt.db.relatorios              to com.ossobo.winterfx;

    // Controllers — Inventário
    opens com.ossobo.gestaoDepIt.controlls.inventario                to javafx.fxml, com.ossobo.winterfx;
    opens com.ossobo.gestaoDepIt.controlls.inventario.inventarioList to javafx.fxml, com.ossobo.winterfx;
    opens com.ossobo.gestaoDepIt.controlls.inventario.cellfactories  to com.ossobo.winterfx;

    // Controllers — Histórico
    opens com.ossobo.gestaoDepIt.controlls.historico                              to javafx.fxml, com.ossobo.winterfx;
    opens com.ossobo.gestaoDepIt.controlls.historico.historicoList                to javafx.fxml, com.ossobo.winterfx;
    opens com.ossobo.gestaoDepIt.controlls.historico.historicoDetail              to javafx.fxml, com.ossobo.winterfx;
    opens com.ossobo.gestaoDepIt.controlls.historico.historicoList.cellfactories  to com.ossobo.winterfx;

    // Controllers — Catálogo
    opens com.ossobo.gestaoDepIt.controlls.product                             to javafx.fxml, com.ossobo.winterfx;
    opens com.ossobo.gestaoDepIt.controlls.product.catalogoList                to javafx.fxml, com.ossobo.winterfx;
    opens com.ossobo.gestaoDepIt.controlls.product.catalogoList.cellfactories  to com.ossobo.winterfx;

    // Controllers — Funcionários
    opens com.ossobo.gestaoDepIt.controlls.funcionario                                to javafx.fxml, com.ossobo.winterfx;
    opens com.ossobo.gestaoDepIt.controlls.funcionario.funcionarioList                to com.ossobo.winterfx;
    opens com.ossobo.gestaoDepIt.controlls.funcionario.funcionarioList.cellfactories  to com.ossobo.winterfx;

    // Controllers — Relatórios (FXML + WinterFX na MESMA linha)
    opens com.ossobo.gestaoDepIt.controlls.relatorio           to javafx.fxml, com.ossobo.winterfx;
    opens com.ossobo.gestaoDepIt.controlls.relatorio.dashboard to javafx.fxml, com.ossobo.winterfx;
    opens com.ossobo.gestaoDepIt.controlls.relatorio.simples   to javafx.fxml, com.ossobo.winterfx;
    opens com.ossobo.gestaoDepIt.controlls.relatorio.avancado  to javafx.fxml, com.ossobo.winterfx;

    // Utils
    opens com.ossobo.gestaoDepIt.utils            to com.ossobo.winterfx;
    opens com.ossobo.gestaoDepIt.utils.crypthash  to com.ossobo.winterfx;


    opens com.ossobo.gestaoDepIt.utils.gerarCodDep to com.ossobo.winterfx;
    // ============================================================
    // EXPORTS
    // ============================================================

    exports com.ossobo.gestaoDepIt;
    exports com.ossobo.gestaoDepIt.login;
    exports com.ossobo.gestaoDepIt.main;
    exports com.ossobo.gestaoDepIt.db.models;
    exports com.ossobo.gestaoDepIt.db.enums;
    exports com.ossobo.gestaoDepIt.db.sync;
    exports com.ossobo.gestaoDepIt.controlls.historico;
    exports com.ossobo.gestaoDepIt.controlls.product;
    exports com.ossobo.gestaoDepIt.db.documentos to com.ossobo.winterfx;
    opens com.ossobo.gestaoDepIt.utils.gerarSKU to com.ossobo.winterfx;
    opens com.ossobo.gestaoDepIt.controlls.relatorio.dados to com.ossobo.winterfx, javafx.fxml;
}