package com.ossobo.gestaoDepIt.db.sync;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

/**
 * SyncTime v1.0 — FONTE ÚNICA do contrato de timestamps de sync (decisão D).
 *
 * <p>Por que existe: escrita e leitura são o MESMO contrato visto de dois
 * lados. A decisão D ratificou o formato da escrita (ISO-8601 truncado a
 * MILLIS) e a leitura ficou órfã do lado do xerial ({@code getTimestamp}
 * só aceita {@code yyyy-MM-dd HH:mm:ss}). O resultado foi
 * {@code Error parsing time stamp} em qualquer registro carimbado pelo
 * app migrado. Este utilitário torna a divergência estruturalmente
 * impossível: ninguém formata ou parseia timestamp fora daqui.</p>
 *
 * <h2>Contrato</h2>
 * <ul>
 *   <li><b>Escrita:</b> ISO-8601 truncado a MILLIS —
 *       {@code 2026-09-18T15:57:26.123}. O truncamento é obrigatório:
 *       {@code LocalDateTime.toString()} varia de precisão (omite millis
 *       quando zero), e formas heterogêneas quebram a ordenação
 *       lexicográfica do horizonte ({@code WHERE updated_at > ?}).</li>
 *   <li><b>Leitura:</b> aceita ISO-8601 canônico; formato legado
 *       (espaço) é lido com {@code WARNING} — cada hit é um caminho de
 *       escrita ainda NÃO migrado. Formato ilegível → fail-fast com
 *       mensagem acionável.</li>
 *   <li><b>Proibido:</b> {@code rs.getTimestamp()} em coluna de sync.
 *       Além do formato, o xerial aplica timezone — o sync exige
 *       timestamps timezone-neutros e determinísticos.</li>
 * </ul>
 *
 * <h2>Escopo</h2>
 * <p>Aplicável a TODA tabela sincronizada (todas exceto
 * {@code config_servidor_remoto}, que está fora do sync por decisão
 * ratificada — ainda assim recomendado usar o utilitário por
 * uniformidade de leitura).</p>
 *
 * <h2>Evolução</h2>
 * <p>Quando o {@code WARNING} de formato legado parar de aparecer no
 * boot, os dados antigos foram re-carimbados e o {@code LEGADO} pode
 * ser removido deste utilitário.</p>
 *
 * @since v1.0
 */
public final class SyncTime {

    private static final System.Logger LOGGER = System.getLogger(SyncTime.class.getName());

    /** Formato legado: DEFAULT CURRENT_TIMESTAMP do SQLite / DATETIME do MySQL v1. */
    private static final DateTimeFormatter LEGADO =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private SyncTime() { }

    // ============================================================
    // ESCRITA
    // ============================================================

    /**
     * Carimbo canônico (decisão D): ISO-8601 truncado a MILLIS.
     *
     * <p>Truncar fixa a forma — {@code LocalDateTime.toString()} tem
     * precisão VARIÁVEL (omite millis quando zero), e formas diferentes
     * quebram a ordenação lexicográfica do horizonte
     * ({@code WHERE updated_at > ?}).</p>
     */
    public static String agora() {
        return LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS).toString();
    }

    // ============================================================
    // LEITURA
    // ============================================================

    /**
     * Leitura tolerante com auditoria:
     * <ul>
     *   <li>ISO-8601 canônico → parse direto, sem log.</li>
     *   <li>Formato legado (espaço) → parse com {@code WARNING};
     *       cada hit marca um caminho de escrita NÃO migrado.</li>
     *   <li>Formato ilegível → fail-fast com mensagem acionável
     *       (nunca NPE três quadros adiante).</li>
     * </ul>
     *
     * <p><b>Fail-fast assumido:</b> um registro com timestamp ruim
     * derruba a lista inteira. O trade-off é consciente —
     * visibilidade acima de disponibilidade de lista. Em sistema de
     * inventário, lista silenciosamente incompleta é pior que lista
     * ausente com erro acionável.</p>
     *
     * @param raw       valor bruto do {@code ResultSet.getString(...)}.
     * @param contexto  identificação legível para o log e para o erro
     *                  (ex.: {@code "inventario_equipamentos.created_at"}).
     * @return {@code LocalDateTime} parseado, ou {@code null} se {@code raw} for nulo/vazio.
     * @throws IllegalStateException se o formato não for reconhecido.
     */
    public static LocalDateTime parse(String raw, String contexto) {
        if (raw == null || raw.isBlank()) return null;

        try {
            return LocalDateTime.parse(raw);                        // decisão D
        } catch (DateTimeParseException primeiro) {
            try {
                LocalDateTime legado = LocalDateTime.parse(raw, LEGADO);
                LOGGER.log(System.Logger.Level.WARNING,
                        "Timestamp em formato legado: ''{0}'' em {1} — caminho de escrita não migrado?",
                        raw, contexto);
                return legado;
            } catch (DateTimeParseException segundo) {
                throw new IllegalStateException(
                        "Timestamp ilegível: '" + raw + "' em " + contexto, segundo);
            }
        }
    }
}