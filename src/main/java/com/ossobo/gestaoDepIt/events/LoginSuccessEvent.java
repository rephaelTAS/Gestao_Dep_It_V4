package com.ossobo.gestaoDepIt.events;

import com.ossobo.gestaoDepIt.db.models.Usuario;

public class LoginSuccessEvent {
    private final Usuario usuario;
    private final long timestamp;

    public LoginSuccessEvent(Usuario usuario) {
        this.usuario = usuario;
        this.timestamp = System.currentTimeMillis();
    }

    public Usuario getUsuario() { return usuario; }
    public long getTimestamp() { return timestamp; }
    public String getUsername() { return usuario != null ? usuario.nome() : null; }
}