package com.ossobo.gestaoDepIt.controllers.gestao.relatorios.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class RelatorioRecente {
    private final StringProperty nome;
    private final StringProperty data;
    private final StringProperty status;

    public RelatorioRecente(String nome, String data, String status) {
        this.nome = new SimpleStringProperty(nome);
        this.data = new SimpleStringProperty(data);
        this.status = new SimpleStringProperty(status);
    }

    // Getters para Properties (Necessário para a TableView do JavaFX)
    public StringProperty nomeProperty() { return nome; }
    public StringProperty dataProperty() { return data; }
    public StringProperty statusProperty() { return status; }

    // Getters simples
    public String getNome() { return nome.get(); }
    public String getData() { return data.get(); }
    public String getStatus() { return status.get(); }
}