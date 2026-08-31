/*
 * UsuarioFilterManager v1.0
 *
 * Gerencia a lógica de filtro da lista de usuários.
 * Extraído do Controller — SRP.
 *
 * v1.0: Versão inicial
 */
package com.ossobo.gestaoDepIt.controllers.gestao.pessoal;

import com.ossobo.gestaoDepIt.db.models.Usuario;
import com.ossobo.gestaoDepIt.db.services.UsuariosService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class UsuarioFilterManager {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioFilterManager.class);
    private final UsuariosService service;

    public UsuarioFilterManager(UsuariosService service) {
        this.service = service;
    }

    /**
     * Aplica filtros combinados.
     * Usa busca por nome se fornecido, depois filtra em memória pelos demais campos.
     */
    public List<Usuario> filtrar(String nome, String email, String nivelAcesso, Boolean ativo, Boolean sessaoAtiva) {
        logger.debug("Filtrando: nome={}, email={}, nivel={}, ativo={}, sessao={}",
                nome, email, nivelAcesso, ativo, sessaoAtiva);

        // Carrega todos os ativos como base
        List<Usuario> resultados = service.buscarComFiltros(nome, email, nivelAcesso, ativo, sessaoAtiva, null, null);

        logger.debug("Filtro retornou {} resultado(s)", resultados.size());
        return resultados;
    }
}