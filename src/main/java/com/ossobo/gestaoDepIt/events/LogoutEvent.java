package com.ossobo.gestaoDepIt.events;

/**
 * Evento disparado quando o usuário faz logout
 */
public class LogoutEvent {
    private final String username;
    private final long timestamp;

    public LogoutEvent(String username) {
        this.username = username;
        this.timestamp = System.currentTimeMillis();
    }

    public String getUsername() {
        return username;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
