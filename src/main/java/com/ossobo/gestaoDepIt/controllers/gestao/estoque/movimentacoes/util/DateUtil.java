// ===== DATE_UTIL.java =====
package com.ossobo.gestaoDepIt.controllers.gestao.estoque.movimentacoes.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * UTILITÁRIO DE FORMATAÇÃO DE DATAS
 * Propósito: Centralizar formatação de datas
 */
public class DateUtil {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ===== FORMATAR_PERIODO =====
    public static String formatarPeriodo(LocalDate inicio, LocalDate fim) {
        if (inicio == null && fim == null) {
            return "Todos";
        } else if (inicio != null && fim != null) {
            return String.format("%s a %s",
                    inicio.format(FORMATTER),
                    fim.format(FORMATTER)
            );
        } else if (inicio != null) {
            return "Desde " + inicio.format(FORMATTER);
        } else {
            return "Até " + fim.format(FORMATTER);
        }
    }

    // ===== FORMATAR_DATA =====
    public static String formatarData(LocalDate data) {
        return data != null ? data.format(FORMATTER) : "";
    }
}