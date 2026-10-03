package com.ossobo.gestaoDepIt.utils.gerarSKU;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * SkuCatalogo v1.0
 *
 * Fonte única das regras de composição de SKU.
 * Ordem de prioridade: específicas ANTES das genéricas.
 *
 * Formato: TIPO-MARCA-MODELO-NNN
 *   TIPO   = 2-4 letras (categoria)
 *   MARCA  = 3 letras (marca)
 *   MODELO = 3 letras (modelo)
 *   NNN    = sequencial 3 dígitos, persistido em tabela `sequencias`
 */
public final class SkuCatalogo {

    private SkuCatalogo() {}

    /**
     * Categoria → prefixo de tipo.
     * ORDEM IMPORTA: matches específicos antes dos genéricos.
     * Usa LinkedHashMap para preservar a ordem de inserção.
     */
    private static final Map<String, String> TIPO_PREFIXO = new LinkedHashMap<>();

    static {
        // ---- Específicos primeiro (evitam falso-positivo) ----
        coloca("access point", "NET-AP");
        coloca("nobreak", "UPS");
        coloca("multifuncional", "IMP");
        coloca("impressora", "IMP");
        coloca("plotter", "IMP");
        coloca("projetor", "PRJ");
        coloca("datashow", "PRJ");
        coloca("webcam", "CAM");

        // ---- Categorias principais ----
        coloca("notebook", "CMP");
        coloca("portatil", "CMP");
        coloca("laptop", "CMP");
        coloca("computador", "CPU");
        coloca("desktop", "CPU");

        // ---- Periféricos ----
        coloca("monitor", "MON");
        coloca("ecra", "MON");
        coloca("tela", "MON");
        coloca("teclado", "TCD");
        coloca("mouse", "MSE");
        coloca("rato", "MSE");
        coloca("headset", "HDS");
        coloca("fone", "HDS");
        coloca("microfone", "HDS");
        coloca("scanner", "SCN");
        coloca("digitalizador", "SCN");

        // ---- Consumíveis ----
        coloca("toner", "TON");
        coloca("tinteiro", "TON");
        coloca("cartucho", "TON");
        coloca("tinta", "TON");

        // ---- Móveis ----
        coloca("telemovel", "TEL");
        coloca("smartphone", "TEL");
        coloca("celular", "TEL");
        coloca("tablet", "TAB");
        coloca("ipad", "TAB");

        // ---- Rede ----
        coloca("router", "NET");
        coloca("roteador", "NET");
        coloca("switch", "NET");
        coloca("firewall", "NET");
        coloca("rede", "NET");
        coloca("cabo", "CBL");
        coloca("adaptador", "CBL");
        coloca("conector", "CBL");
        coloca("extensor", "CBL");

        // ---- Armazenamento ----
        coloca("ssd", "STO");
        coloca("disco", "STO");
        coloca("armazenamento", "STO");
        coloca("nas", "STO");
        coloca("san", "STO");

        // ---- Servidores ----
        coloca("servidor", "SRV");

        // ---- Software ----
        coloca("software", "SWL");
        coloca("licenca", "SWL");
        coloca("licença", "SWL");
        coloca("sistema operativo", "SWL");

        // ---- Componentes internos ----
        coloca("ram", "CMPN");
        coloca("memoria", "CMPN");
        coloca("memória", "CMPN");
        coloca("placa mae", "CMPN");
        coloca("placa mãe", "CMPN");
        coloca("motherboard", "CMPN");
        coloca("gpu", "CMPN");
        coloca("placa de video", "CMPN");
        coloca("fonte", "CMPN");
        coloca("psu", "CMPN");
    }

    private static void coloca(String chave, String prefixo) {
        TIPO_PREFIXO.put(chave.toLowerCase(), prefixo);
    }

    /** Prefixo para categoria; fallback OUT. */
    public static String prefixoTipo(String categoria) {
        if (categoria == null || categoria.isBlank()) return "OUT";
        String normalizado = normalizar(categoria);
        return TIPO_PREFIXO.entrySet().stream()
                .filter(e -> normalizado.contains(e.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse("OUT");
    }

    /** Prefixo de 3 letras para marca/modelo. */
    public static String prefixo3(String bruto) {
        if (bruto == null || bruto.isBlank()) return "XXX";
        String limpo = bruto.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
        if (limpo.isEmpty()) return "XXX";
        if (limpo.length() <= 3) {
            return (limpo + "XXX").substring(0, 3);
        }
        return limpo.substring(0, 3);
    }

    /** Normaliza acentos e caixa para casar com o catálogo. */
    private static String normalizar(String s) {
        return java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase()
                .trim();
    }

    public static Set<String> categoriasConhecidas() {
        return TIPO_PREFIXO.keySet();
    }
}