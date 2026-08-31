/*
 * GIADataLoader v1.0
 *
 * Carrega e mantém os dados base para o dashboard GIA.
 * Centraliza o acesso aos Services — SRP.
 *
 * v1.0: Versão inicial
 */
package com.ossobo.gestaoDepIt.controllers.gestao.gia.functions;

import com.ossobo.gestaoDepIt.db.models.*;
import com.ossobo.gestaoDepIt.db.services.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class GIADataLoader {

    private static final Logger logger = LoggerFactory.getLogger(GIADataLoader.class);

    private final InventarioEquipamentosService inventarioService;
    private final CatalogoProdutosService catalogoService;
    private final FuncionariosService funcionariosService;
    private final EstoqueMovimentacoesService movimentacoesService;
    private final GestaoTonersService tonersService;
    private final HistoricoEventosService historicoService;

    private List<InventarioEquipamentos> equipamentos = new ArrayList<>();
    private List<CatalogoProdutos> catalogos = new ArrayList<>();
    private List<Funcionarios> funcionarios = new ArrayList<>();
    private List<EstoqueMovimentacoes> movimentacoes = new ArrayList<>();
    private List<GestaoToners> toners = new ArrayList<>();

    public GIADataLoader(InventarioEquipamentosService inventarioService,
                         CatalogoProdutosService catalogoService,
                         FuncionariosService funcionariosService,
                         EstoqueMovimentacoesService movimentacoesService,
                         GestaoTonersService tonersService,
                         HistoricoEventosService historicoService) {
        this.inventarioService = inventarioService;
        this.catalogoService = catalogoService;
        this.funcionariosService = funcionariosService;
        this.movimentacoesService = movimentacoesService;
        this.tonersService = tonersService;
        this.historicoService = historicoService;
    }

    /** Carrega todos os dados dos Services. */
    public void carregarTudo() {
        logger.info("Carregando dados para análise GIA...");
        equipamentos = inventarioService.listarTodos(1, 10000);
        catalogos = catalogoService.listarTodos(1, 10000);
        funcionarios = funcionariosService.listarTodos(1, 10000);
        logger.info("Dados carregados: {} equipamentos, {} catálogos, {} funcionários",
                equipamentos.size(), catalogos.size(), funcionarios.size());
    }

    // Getters
    public List<InventarioEquipamentos> getEquipamentos() { return equipamentos; }
    public List<CatalogoProdutos> getCatalogos() { return catalogos; }
    public List<Funcionarios> getFuncionarios() { return funcionarios; }
    public List<EstoqueMovimentacoes> getMovimentacoes() { return movimentacoes; }
    public List<GestaoToners> getToners() { return toners; }
    public InventarioEquipamentosService getInventarioService() { return inventarioService; }
    public CatalogoProdutosService getCatalogoService() { return catalogoService; }
    public FuncionariosService getFuncionariosService() { return funcionariosService; }
    public EstoqueMovimentacoesService getMovimentacoesService() { return movimentacoesService; }
    public GestaoTonersService getTonersService() { return tonersService; }
    public HistoricoEventosService getHistoricoService() { return historicoService; }
}