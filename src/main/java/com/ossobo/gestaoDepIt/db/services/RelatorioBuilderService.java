package com.ossobo.gestaoDepIt.db.services;

import com.ossobo.gestaoDepIt.db.config.DatabaseConnection;
import com.ossobo.gestaoDepIt.db.relatorios.*;
import com.ossobo.winterfx.anotations.Inject;
import com.ossobo.winterfx.anotations.Service;

import java.sql.*;
import java.util.*;

/**
 * RelatorioBuilderService v1.1
 *
 * Motor do relatório LEGO: monta SQL dinâmico a partir de um RelatorioModelo
 * ou de parâmetros simples, executa e devolve RelatorioResultado.
 *
 * v1.1 — executar(): substitui Map.copyOf(linha) por Collections.unmodifiableMap
 *        (o Map.copyOf rejeita null em valores; colunas NULL do SQLite
 *        derrubavam a leitura com NullPointerException).
 *
 * Garantias de segurança:
 *   - Identificadores (tabela/coluna) são validados contra PRAGMA table_info
 *     e contra o RelatorioCatalogoGrafo. NUNCA interpolados crus.
 *   - Valores de filtro viajam SEMPRE como PreparedStatement (?), nunca
 *     concatenados no SQL.
 *   - JOINs só são aceitos se a aresta existir no grafo — defesa em profundidade.
 *
 * Não publica eventos (relatório é read-only, fora do sync).
 */
@Service
public class RelatorioBuilderService {

    private static final System.Logger logger =
            System.getLogger(RelatorioBuilderService.class.getName());

    @Inject
    private DatabaseConnection dbConnection;

    // ============================================================
    // METADADOS — colunas de uma tabela (PRAGMA)
    // ============================================================

    /**
     * Retorna as colunas físicas de um alias do grafo.
     * Fonte única: o schema real via PRAGMA table_info.
     * Retorna List vazia se o alias for inválido (chamador decide).
     */
    public List<String> colunasDe(String alias) throws SQLException {
        if (!RelatorioCatalogoGrafo.aliasesValidos().contains(alias)) return List.of();
        String tabelaFisica = RelatorioCatalogoGrafo.tabelaFisica(alias);

        // PRAGMA não aceita placeholder para o nome da tabela — valida antes.
        if (!tabelaFisica.matches("[a-z_]+")) {
            throw new IllegalArgumentException("Tabela suspeita: " + tabelaFisica);
        }

        List<String> colunas = new ArrayList<>();
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("PRAGMA table_info(" + tabelaFisica + ")")) {
            while (rs.next()) {
                String nome = rs.getString("name");
                // Ignora colunas internas de auditoria/sync — não fazem sentido no relatório
                if (nome.equals("device_id") || nome.equals("deleted")
                        || nome.equals("fatura_compra") || nome.equals("documento_entrega")
                        || nome.equals("documento_devolucao") || nome.equals("imagem_perfil")) {
                    continue;
                }
                colunas.add(nome);
            }
        }
        return List.copyOf(colunas);
    }

    // ============================================================
    // CONSULTA SIMPLES — uma tabela, colunas escolhidas, filtros opcionais
    // ============================================================

    /**
     * @param alias          alias do grafo ("inventario", "catalogo"...)
     * @param colunasDesejadas  subconjunto das colunas de alias (vazio = todas)
     * @param filtros        Map<coluna, valor>; só aplica colunas válidas
     * @param limite         máximo de linhas (<=0 = sem limite)
     */
    public RelatorioResultado consultarSimples(
            String alias,
            List<String> colunasDesejadas,
            Map<String, Object> filtros,
            int limite) throws SQLException {

        if (!RelatorioCatalogoGrafo.aliasesValidos().contains(alias))
            throw new IllegalArgumentException("Tabela desconhecida: " + alias);

        String tabelaFisica = RelatorioCatalogoGrafo.tabelaFisica(alias);
        Set<String> colunasValidas = new HashSet<>(colunasDe(alias));

        List<String> colunas = (colunasDesejadas == null || colunasDesejadas.isEmpty())
                ? List.copyOf(colunasValidas)
                : colunasDesejadas.stream().filter(colunasValidas::contains).toList();

        if (colunas.isEmpty())
            throw new IllegalArgumentException("Nenhuma coluna válida selecionada para " + alias);

        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(String.join(", ", colunas));
        sql.append(" FROM ").append(tabelaFisica);

        String where = construirWhere(filtros, colunasValidas, params);
        if (!where.isEmpty()) sql.append(" WHERE ").append(where);

        if (limite > 0) sql.append(" LIMIT ").append(limite);

        return executar(sql.toString(), colunas, params);
    }

    // ============================================================
    // CONSULTA LEGO — modelo salvo (base + JOINs + filtros)
    // ============================================================

    public RelatorioResultado consultarModelo(RelatorioModelo modelo, int limite) throws SQLException {
        if (modelo == null) throw new IllegalArgumentException("Modelo inválido");

        // Revalidação defensiva (Service já validou ao salvar — cinto e suspensório)
        validarModelo(modelo);

        String baseAlias = modelo.tabelaBase();
        String baseFisica = RelatorioCatalogoGrafo.tabelaFisica(baseAlias);

        // 1) Colunas selecionadas: base primeiro, depois cada JOIN na ordem
        LinkedHashMap<String, String> projecao = new LinkedHashMap<>(); // "alias.col" -> "alias_col"

        for (RelatorioColuna c : modelo.colunasBase()) {
            projecao.put(c.tabela() + "." + c.coluna(), c.tabela() + "_" + c.coluna());
        }
        for (RelatorioJoin j : modelo.joins()) {
            for (RelatorioColuna c : j.colunas()) {
                projecao.put(c.tabela() + "." + c.coluna(), c.tabela() + "_" + c.coluna());
            }
        }
        if (projecao.isEmpty())
            throw new IllegalArgumentException("Nenhuma coluna selecionada no modelo");

        // 2) FROM + JOINs (na ordem declarada)
        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(String.join(", ",
                projecao.keySet().stream()
                        .map(k -> k + " AS " + projecao.get(k).replace('.', '_'))
                        .toList()));
        sql.append(" FROM ").append(baseFisica).append(" AS ").append(baseAlias);

        for (RelatorioJoin j : modelo.joins()) {
            String aliasJoin = j.tabela();
            String tabelaJoin = RelatorioCatalogoGrafo.tabelaFisica(aliasJoin);

            // Condição reescrita com aliases explícitos (garante semântica)
            String condicao = j.condicao()
                    .replace(baseAlias + ".", baseAlias + ".")
                    .replace(aliasJoin + ".", aliasJoin + ".");

            sql.append(" LEFT JOIN ").append(tabelaJoin)
                    .append(" AS ").append(aliasJoin)
                    .append(" ON ").append(condicao);
        }

        // 3) Filtros (opcional — só se o modelo trouxe)
        List<Object> params = new ArrayList<>();
        Map<String, Object> filtros = extrairFiltros(modelo.filtrosJson());
        if (!filtros.isEmpty()) {
            Set<String> colunasBase = new HashSet<>(colunasDe(baseAlias));
            String where = construirWhere(filtros, colunasBase, params);
            if (!where.isEmpty()) sql.append(" WHERE ").append(where);
        }

        if (limite > 0) sql.append(" LIMIT ").append(limite);

        return executar(sql.toString(), List.copyOf(projecao.values()), params);
    }

    // ============================================================
    // EXECUÇÃO
    // ============================================================

    private RelatorioResultado executar(String sql, List<String> colunas, List<Object> params)
            throws SQLException {

        logger.log(System.Logger.Level.DEBUG, "🔎 SQL do relatório: {0}", sql);

        List<Map<String, Object>> linhas = new ArrayList<>();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData();
                int n = meta.getColumnCount();

                while (rs.next()) {
                    LinkedHashMap<String, Object> linha = new LinkedHashMap<>(n);
                    for (int i = 1; i <= n; i++) {
                        Object v = rs.getObject(i);
                        linha.put(meta.getColumnLabel(i), v);
                    }
                    // unmodifiableMap aceita null (Map.copyOf não aceita)
                    linhas.add(Collections.unmodifiableMap(linha));
                }
            }
        }
        return new RelatorioResultado(colunas, linhas, linhas.size(), sql);
    }

    // ============================================================
    // HELPERS PRIVADOS
    // ============================================================

    /**
     * Constrói cláusula WHERE com placeholders. Só aceita colunas
     * que existam no Set recebido. Valores viajam como params — nunca
     * interpolação.
     */
    private String construirWhere(Map<String, Object> filtros,
                                  Set<String> colunasValidas,
                                  List<Object> params) {
        if (filtros == null || filtros.isEmpty()) return "";

        List<String> condicoes = new ArrayList<>();
        for (Map.Entry<String, Object> e : filtros.entrySet()) {
            String col = e.getKey();
            Object val = e.getValue();
            if (val == null) continue;
            if (!colunasValidas.contains(col)) continue;
            if (val instanceof String s && s.isBlank()) continue;

            condicoes.add(col + " = ?");
            params.add(val);
        }
        return String.join(" AND ", condicoes);
    }

    /**
     * Filtros chegam serializados em JSON simples. Parser defensivo:
     * falha silenciosa devolve Map vazio (relatório sem filtro é válido).
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> extrairFiltros(String json) {
        if (json == null || json.isBlank() || json.equals("{}")) return Map.of();
        try {
            com.google.gson.Gson gson = new com.google.gson.Gson();
            Map<String, Object> bruto = gson.fromJson(json, Map.class);
            return bruto == null ? Map.of() : bruto;
        } catch (Exception e) {
            logger.log(System.Logger.Level.WARNING,
                    "Falha ao parsear filtros JSON: {0}", json);
            return Map.of();
        }
    }

    /** Revalida modelo contra o grafo — chamado antes de executar. */
    private void validarModelo(RelatorioModelo m) {
        if (!RelatorioCatalogoGrafo.aliasesValidos().contains(m.tabelaBase()))
            throw new IllegalArgumentException("Tabela base desconhecida: " + m.tabelaBase());

        for (RelatorioJoin j : m.joins()) {
            if (!RelatorioCatalogoGrafo.aliasesValidos().contains(j.tabela()))
                throw new IllegalArgumentException("Alias de JOIN desconhecido: " + j.tabela());

            RelatorioCatalogoGrafo.Aresta aresta = RelatorioCatalogoGrafo
                    .arestaEntre(m.tabelaBase(), j.tabela())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "JOIN não permitido pelo grafo: " + m.tabelaBase() + " → " + j.tabela()));

            String esperada = aresta.condicao().replace(" ", "");
            String recebida = j.condicao().replace(" ", "");
            if (!esperada.equalsIgnoreCase(recebida))
                throw new IllegalArgumentException(
                        "Condição inválida para " + j.tabela() + ". Esperada: " + aresta.condicao());
        }
    }
}