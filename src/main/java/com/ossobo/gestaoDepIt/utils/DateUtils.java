package com.ossobo.gestaoDepIt.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 🎯 UTILITÁRIOS DE DATA - Funções comuns para formatação de datas
 */
public final class DateUtils {

    private DateUtils() {
        // Classe utilitária - não instanciável
    }

    // 🎯 FORMATADORES
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm:ss");

    /**
     * ✅ FORMATA DATA (dd/MM/yyyy)
     */
    public static String formatDate(LocalDate date) {
        if (date == null) return "N/A";
        return date.format(DATE_FORMATTER);
    }

    /**
     * ✅ FORMATA DATA/HORA (dd/MM/yyyy HH:mm:ss)
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "N/A";
        return dateTime.format(DATE_TIME_FORMATTER);
    }

    /**
     * ✅ FORMATA HORA (HH:mm:ss)
     */
    public static String formatTime(LocalDateTime dateTime) {
        if (dateTime == null) return "N/A";
        return dateTime.format(TIME_FORMATTER);
    }

    /**
     * ✅ PARSE DATA (dd/MM/yyyy)
     */
    public static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        try {
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * ✅ PARSE DATA/HORA (dd/MM/yyyy HH:mm:ss)
     */
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) return null;
        try {
            return LocalDateTime.parse(dateTimeStr, DATE_TIME_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * ✅ VERIFICA SE DATA É VÁLIDA
     */
    public static boolean isValidDate(String dateStr) {
        try {
            LocalDate.parse(dateStr, DATE_FORMATTER);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * ✅ OBTÉM DATA ATUAL FORMATADA
     */
    public static String getCurrentDateFormatted() {
        return LocalDate.now().format(DATE_FORMATTER);
    }

    /**
     * ✅ OBTÉM DATA/HORA ATUAL FORMATADA
     */
    public static String getCurrentDateTimeFormatted() {
        return LocalDateTime.now().format(DATE_TIME_FORMATTER);
    }

    /**
     * ✅ CALCULA DIFERENÇA EM DIAS
     */
    public static long daysBetween(LocalDate start, LocalDate end) {
        if (start == null || end == null) return 0;
        return java.time.temporal.ChronoUnit.DAYS.between(start, end);
    }

    /**
     * ✅ VERIFICA SE DATA ESTÁ NO PASSADO
     */
    public static boolean isPastDate(LocalDate date) {
        return date != null && date.isBefore(LocalDate.now());
    }

    /**
     * ✅ VERIFICA SE DATA ESTÁ NO FUTURO
     */
    public static boolean isFutureDate(LocalDate date) {
        return date != null && date.isAfter(LocalDate.now());
    }

    /**
     * ✅ FORMATA DURAÇÃO (em segundos para formato legível)
     */
    public static String formatDuration(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, secs);
        } else {
            return String.format("%d:%02d", minutes, secs);
        }
    }

    // ===== PARSE TOLERANTE (persistência) =====

    /** Aceita "yyyy-MM-dd" (ISO) e timestamp Unix em ms. */
    public static LocalDate parseDateTolerante(String raw) {
        if (raw == null || raw.isBlank()) return null;

        // ISO: yyyy-MM-dd
        if (raw.contains("-")) {
            try {
                return LocalDate.parse(raw);
            } catch (Exception e) {
                return null;
            }
        }

        // Legado: timestamp Unix em ms
        try {
            long ms = Long.parseLong(raw);
            return java.time.Instant.ofEpochMilli(ms)
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate();
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Aceita ISO com T, SQLite default (yyyy-MM-dd HH:mm:ss) e ms. */
    public static LocalDateTime parseDateTimeTolerante(String raw) {
        if (raw == null || raw.isBlank()) return null;

        // ISO: yyyy-MM-ddTHH:mm:ss[.SSS]
        if (raw.contains("T")) {
            try {
                return LocalDateTime.parse(raw);
            } catch (Exception e) { /* tenta próximo */ }
        }

        // SQLite default: yyyy-MM-dd HH:mm:ss[.SSS]
        if (raw.contains("-") && raw.contains(" ")) {
            try {
                return LocalDateTime.parse(raw,
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss[.SSS]"));
            } catch (Exception e) { /* tenta próximo */ }
        }

        // Legado: ms
        try {
            long ms = Long.parseLong(raw);
            return java.time.Instant.ofEpochMilli(ms)
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDateTime();
        } catch (NumberFormatException e) {
            return null;
        }
    }
}