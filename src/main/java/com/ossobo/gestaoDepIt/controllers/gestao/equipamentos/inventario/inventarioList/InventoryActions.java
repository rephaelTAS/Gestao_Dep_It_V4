package com.ossobo.gestaoDepIt.controllers.gestao.equipamentos.inventario.inventarioList;

import com.ossobo.gestaoDepIt.db.models.InventarioEquipamentos;
import com.ossobo.gestaoDepIt.db.services.InventarioEquipamentosService;

import com.ossobo.winterfx.anotations.Inject;
import com.ossobo.winterfx.anotations.Service;
import com.ossobo.winterfx.anotations.Async;
import com.ossobo.winterfx.event.EventBus;
import com.ossobo.winterfx.router.Rotas;
import com.ossobo.winterfx.router.model.Params;
import com.ossobo.winterfx.router.model.ResponseData;
import com.ossobo.winterfx.view.anotations.NewScene;
import com.ossobo.winterfx.notifications.anotations.OnError;
import com.ossobo.winterfx.notifications.anotations.OnSuccess;

import com.ossobo.winterfx.view.floatingwindow.StageForFloatingWindow;
import com.ossobo.winterfx.view.floatingwindow.anotations.FloatingWindow;
import javafx.application.Platform;
import javafx.stage.Window;
import javafx.stage.Modality;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Gerencia todas as ações do inventário.
 * v2.0 - Migrado para WinterFX com EventBus e @Async
 *
 * Responsabilidades:
 * - Abrir janelas flutuantes (@FloatingWindow)
 * - Carregar dados com @Async
 * - Aplicar filtros com @Async
 * - Publicar eventos via EventBus
 */
@Service
public class InventoryActions {

}