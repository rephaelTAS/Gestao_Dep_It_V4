package com.ossobo.gestaoDepIt.utils;


import com.ossobo.winterfx.anotations.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class GerarIdProduto {

    // ===== CACHE DE SEQUÊNCIAS POR BASE ID =====
    private final Map<String, AtomicInteger> sequenciasPorBase = new ConcurrentHashMap<>();

    // ===== GERAR ID COMPLETO =====
    /**
     * Gera ID único de produto baseado em tipo, marca, modelo + sufixo sequencial
     * Formato: TIPO-MARCA-MODELO-NNN (ex: CMP-DEL-LATI-001)
     *
     * @param categoria Tipo do equipamento (ex: "Notebook")
     * @param marca Marca do produto (ex: "Dell")
     * @param modelo Modelo específico (ex: "Latitude 5400")
     * @return ID único com sufixo de 3 dígitos
     */
    public String gerarIdProduto(String categoria, String marca, String modelo) {
        // Gerar ID base (sem sufixo)
        String idBase = gerarIdBase(categoria, marca, modelo);

        // Obter próximo número sequencial para esta base
        int sequencia = obterProximaSequencia(idBase);

        // Formatar sufixo com 3 dígitos (001, 002, ...)
        String sufixo = String.format("%03d", sequencia);

        // Retornar ID completo
        return idBase + "-" + sufixo;
    }

    // ===== GERAR ID BASE (SEM SUFIXO) =====
    /**
     * Gera a parte base do ID (tipo-marca-modelo)
     * Usado internamente e para busca de produtos similares
     */
    public String gerarIdBase(String categoria, String marca, String modelo) {
        String prefixoTipo = gerarPrefixoTipo(categoria);
        String prefixoMarca = gerarPrefixo(marca);
        String prefixoModelo = gerarPrefixo(modelo);

        return (prefixoTipo + "-" + prefixoMarca + "-" + prefixoModelo).toUpperCase();
    }

    // ===== GERAR PRÓXIMA SEQUÊNCIA =====
    /**
     * Obtém próximo número sequencial para uma base ID
     * Thread-safe usando AtomicInteger
     */
    private int obterProximaSequencia(String idBase) {
        return sequenciasPorBase
                .computeIfAbsent(idBase, k -> new AtomicInteger(0))
                .incrementAndGet();
    }

    // ===== RESETAR SEQUÊNCIA (PARA TESTES/MIGRAÇÃO) =====
    /**
     * Reseta sequência para uma base específica
     * Útil em testes ou migrações
     */
    public void resetarSequencia(String idBase, int valorInicial) {
        sequenciasPorBase.put(idBase, new AtomicInteger(valorInicial));
    }

    // ===== MÉTODOS AUXILIARES (MANTIDOS) =====
    /**
     * Gera prefixo de 3 letras baseado no tipo
     * Mantém compatibilidade com sistema existente
     */
    private String gerarPrefixoTipo(String tipo) {
        tipo = tipo.toLowerCase().trim();

        // Categorias de Computação Principal
        if (tipo.contains("portátil") || tipo.contains("portatil") || tipo.contains("notebook") || tipo.contains("laptop")) return "CMP";
        if (tipo.contains("cpu") || tipo.contains("computador") || tipo.contains("desktop") || tipo.contains("pc")) return "CPU";

        // Periféricos e Acessórios
        if (tipo.contains("monitor") || tipo.contains("ecrã") || tipo.contains("tela")) return "MON";
        if (tipo.contains("impressora") || tipo.contains("multifuncional") || tipo.contains("plotter")) return "IMP";
        if (tipo.contains("mouse") || tipo.contains("rato")) return "MSE";
        if (tipo.contains("teclado")) return "TCD";
        if (tipo.contains("ups") || tipo.contains("nobreak")) return "UPS";
        if (tipo.contains("toner") || tipo.contains("tinteiros") || tipo.contains("cartuchos") || tipo.contains("tinta")) return "TON";
        if (tipo.contains("telemóvel") || tipo.contains("smartphone") || tipo.contains("celular")) return "TEL";
        if (tipo.contains("headset") || tipo.contains("fone") || tipo.contains("microfone")) return "HDS";
        if (tipo.contains("projetor") || tipo.contains("datashow")) return "PRJ";
        if (tipo.contains("scanner") || tipo.contains("digitalizador")) return "SCN";
        if (tipo.contains("webcam") || tipo.contains("câmara") || tipo.contains("camera")) return "CAM";

        // Redes e Conectividade
        if (tipo.contains("rede") || tipo.contains("router") || tipo.contains("roteador") ||
                tipo.contains("switch") || tipo.contains("firewall") || tipo.contains("access point") || tipo.contains("ap")) return "NET";
        if (tipo.contains("cabo") || tipo.contains("adaptador") || tipo.contains("conector") || tipo.contains("extensor")) return "CBL";

        // Armazenamento
        if (tipo.contains("armazenamento") || tipo.contains("hd") || tipo.contains("ssd") ||
                tipo.contains("disco") || tipo.contains("nas") || tipo.contains("san")) return "STO";

        // Servidores
        if (tipo.contains("servidor")) return "SRV";

        // Dispositivos Móveis (outros)
        if (tipo.contains("tablet") || tipo.contains("ipad")) return "TAB";

        // Software e Licenças
        if (tipo.contains("software") || tipo.contains("licença") || tipo.contains("licenca") || tipo.contains("os")) return "SWL";

        // Componentes Internos
        if (tipo.contains("ram") || tipo.contains("memória") || tipo.contains("placa mãe") ||
                tipo.contains("motherboard") || tipo.contains("gpu") || tipo.contains("placa de video") ||
                tipo.contains("fonte") || tipo.contains("psu")) return "CMPN";

        return "OUT";
    }

    // ===== GERAR PREFIXO MARCA/MODELO =====
    /**
     * Gera prefixo de 3 caracteres para marca ou modelo
     */
    private String gerarPrefixo(String input) {
        if (input == null || input.isBlank()) return "XXX";
        String limpo = input.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
        return limpo.length() <= 3 ? String.format("%-3s", limpo).replace(' ', 'X') : limpo.substring(0, 3);
    }

    // ===== MÉTODO PARA OBTER SEQUÊNCIA ATUAL =====
    /**
     * Obtém sequência atual para uma base (útil para relatórios)
     */
    public int getSequenciaAtual(String idBase) {
        AtomicInteger seq = sequenciasPorBase.get(idBase);
        return seq != null ? seq.get() : 0;
    }
}