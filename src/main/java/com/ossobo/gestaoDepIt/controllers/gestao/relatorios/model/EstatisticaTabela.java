package com.ossobo.gestaoDepIt.controllers.gestao.relatorios.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class EstatisticaTabela {
    private final StringProperty categoria;
    private final StringProperty valor;

    public EstatisticaTabela(String categoria, String valor) {
        this.categoria = new SimpleStringProperty(categoria);
        this.valor = new SimpleStringProperty(valor);
    }

    public StringProperty categoriaProperty() { return categoria; }
    public String getCategoria() { return categoria.get(); }

    public StringProperty valorProperty() { return valor; }
    public String getValor() { return valor.get(); }
}