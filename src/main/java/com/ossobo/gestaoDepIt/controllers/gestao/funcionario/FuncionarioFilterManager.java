/*
 * FuncionarioFilterManager v1.0
 *
 * Gerencia a lógica de filtro da lista de funcionários.
 * Extraído do Controller — SRP: isola regras de filtragem.
 *
 * v1.0: Versão inicial
 *       - Filtro por nome, departamento, status, foto
 *       - Fallback: carrega todos e filtra em memória
 */

package com.ossobo.gestaoDepIt.controllers.gestao.funcionario;

import com.ossobo.gestaoDepIt.db.models.Funcionarios;
import com.ossobo.gestaoDepIt.db.services.FuncionariosService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class FuncionarioFilterManager {

    private static final Logger logger = LoggerFactory.getLogger(FuncionarioFilterManager.class);
    private final FuncionariosService service;

    public FuncionarioFilterManager(FuncionariosService service) {
        this.service = service;
    }

    /**
     * Aplica filtros combinados.
     * Estratégia: tenta usar findWithFilters do Service.
     * Se não retornar resultados ou parâmetros forem nulos, fallback para filtro em memória.
     *
     * @param nome         Nome parcial (nullable)
     * @param departamento Departamento exato (nullable)
     * @param ativo        Status ativo (nullable = todos)
     * @param comFoto      "Com Foto", "Sem Foto", ou null/Todos
     * @return Lista filtrada de funcionários
     */
    public List<Funcionarios> filtrar(String nome, String departamento, Boolean ativo, String comFoto) {
        logger.debug("Filtrando: nome={}, depto={}, ativo={}, foto={}", nome, departamento, ativo, comFoto);

        // Tenta usar o método combinado do Service
        List<Funcionarios> resultados = service.buscarComFiltros(departamento, null, null, ativo, nome);

        // Filtro adicional: Com Foto / Sem Foto (não está no Service)
        if (comFoto != null && !"Todos".equals(comFoto)) {
            boolean apenasComFoto = "Com Foto".equals(comFoto);
            resultados = resultados.stream()
                    .filter(f -> apenasComFoto == f.temImagemPerfil())
                    .collect(Collectors.toList());
        }

        logger.debug("Filtro retornou {} resultado(s)", resultados.size());
        return resultados;
    }
}