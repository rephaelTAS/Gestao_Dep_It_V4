package com.ossobo.gestaoDepIt.controllers.gestao.estoque.movimentacoes.service;

import com.ossobo.gestaoDepIt.controllers.gestao.estoque.movimentacoes.EstoqueMovimentacaoFormController;
import com.ossobo.gestaoDepIt.controllers.gestao.estoque.movimentacoes.dto.MovimentacaoDTO;
import com.ossobo.gestaoDepIt.controllers.gestao.estoque.movimentacoes.detalhes.EstoqueMovimentacaoDetalhesController;
import com.ossobo.nexusfx.NexusFX;
import com.ossobo.nexusfx.di.annotations.Component;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * SERVIÇO COMPLETO DE CONFIGURAÇÃO E NAVEGAÇÃO DE TABELA
 * ✅ Configura todas as colunas conforme FXML
 * ✅ Implementa duplo-clique para detalhes
 * ✅ Formatação profissional dos dados
 * ✅ Atualizado para NexusFX 4.4.0
 */
@Component
public class MovimentacaoTableService {

    // ===== CONSTANTES DE FORMATAÇÃO =====
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ===== CONFIGURE_TABLE (COMPLETO) =====
    public void configureTable(TableView<MovimentacaoDTO> table, Window ownerWindow) {
        if (table == null) return;

        table.getColumns().clear();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // ===== COLUNA: ID =====
        TableColumn<MovimentacaoDTO, Long> colId = new TableColumn<>("#");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(60);
        colId.setStyle("-fx-alignment: CENTER; -fx-font-weight: bold;");

        // ===== COLUNA: DATA =====
        TableColumn<MovimentacaoDTO, LocalDate> colData = new TableColumn<>("Data");
        colData.setCellValueFactory(new PropertyValueFactory<>("dataMovimentacao"));
        colData.setPrefWidth(100);
        colData.setCellFactory(createDateCellFactory());
        colData.setStyle("-fx-alignment: CENTER;");

        // ===== COLUNA: TIPO =====
        TableColumn<MovimentacaoDTO, String> colTipo = new TableColumn<>("Tipo");
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipoMovimentacao"));
        colTipo.setPrefWidth(90);
        colTipo.setCellFactory(createTipoCellFactory());

        // ===== COLUNA: PRODUTO =====
        TableColumn<MovimentacaoDTO, String> colProduto = new TableColumn<>("Produto");
        colProduto.setCellValueFactory(new PropertyValueFactory<>("nomeProduto"));
        colProduto.setPrefWidth(180);
        colProduto.setStyle("-fx-alignment: CENTER_LEFT;");

        // ===== COLUNA: SKU =====
        TableColumn<MovimentacaoDTO, String> colSku = new TableColumn<>("SKU");
        colSku.setCellValueFactory(new PropertyValueFactory<>("skuProduto"));
        colSku.setPrefWidth(120);
        colSku.setStyle("-fx-alignment: CENTER; -fx-font-family: 'Consolas', monospace;");

        // ===== COLUNA: QUANTIDADE =====
        TableColumn<MovimentacaoDTO, Integer> colQuantidade = new TableColumn<>("Qtd");
        colQuantidade.setCellValueFactory(new PropertyValueFactory<>("quantidade"));
        colQuantidade.setPrefWidth(70);
        colQuantidade.setCellFactory(createQuantidadeCellFactory());
        colQuantidade.setStyle("-fx-alignment: CENTER_RIGHT;");

        // ===== COLUNA: LOTE =====
        TableColumn<MovimentacaoDTO, String> colLote = new TableColumn<>("Lote");
        colLote.setCellValueFactory(new PropertyValueFactory<>("lote"));
        colLote.setPrefWidth(100);
        colLote.setStyle("-fx-alignment: CENTER; -fx-font-family: 'Consolas', monospace;");

        // ===== COLUNA: LOCALIZAÇÃO =====
        TableColumn<MovimentacaoDTO, String> colLocalizacao = new TableColumn<>("Local");
        colLocalizacao.setCellValueFactory(new PropertyValueFactory<>("localizacao"));
        colLocalizacao.setPrefWidth(120);
        colLocalizacao.setStyle("-fx-alignment: CENTER;");

        // ===== COLUNA: VALIDADE =====
        TableColumn<MovimentacaoDTO, LocalDate> colValidade = new TableColumn<>("Validade");
        colValidade.setCellValueFactory(new PropertyValueFactory<>("dataValidade"));
        colValidade.setPrefWidth(100);
        colValidade.setCellFactory(createValidadeCellFactory());

        // ===== COLUNA: RESPONSÁVEL =====
        TableColumn<MovimentacaoDTO, String> colResponsavel = new TableColumn<>("Responsável");
        colResponsavel.setCellValueFactory(new PropertyValueFactory<>("nomeUsuario"));
        colResponsavel.setPrefWidth(120);
        colResponsavel.setStyle("-fx-alignment: CENTER;");

        // ===== COLUNA: MOTIVO =====
        TableColumn<MovimentacaoDTO, String> colMotivo = new TableColumn<>("Motivo");
        colMotivo.setCellValueFactory(new PropertyValueFactory<>("motivo"));
        colMotivo.setPrefWidth(200);
        colMotivo.setStyle("-fx-alignment: CENTER_LEFT;");

        // ===== COLUNA: AÇÕES =====
        TableColumn<MovimentacaoDTO, Void> colAcoes = new TableColumn<>("Ações");
        colAcoes.setPrefWidth(80);
        colAcoes.setCellFactory(createActionsCellFactory(table, ownerWindow));
        colAcoes.setStyle("-fx-alignment: CENTER;");

        // ===== ADICIONAR TODAS AS COLUNAS =====
        table.getColumns().addAll(
                colId, colData, colTipo, colProduto, colSku,
                colQuantidade, colLote, colLocalizacao, colValidade,
                colResponsavel, colMotivo, colAcoes
        );

        // ===== CONFIGURAR DUPLO-CLIQUE =====
        configureDoubleClick(table, ownerWindow);

        // ===== CONFIGURAR ESTILO DA TABELA =====
        configureTableStyle(table);
    }

    // ===== FACTORY PARA DATA =====
    private Callback<TableColumn<MovimentacaoDTO, LocalDate>,
            TableCell<MovimentacaoDTO, LocalDate>> createDateCellFactory() {
        return column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(DATE_FORMATTER.format(item));
                    setStyle("-fx-font-weight: bold;");
                }
            }
        };
    }

    // ===== FACTORY PARA TIPO (COM CORES) =====
    private Callback<TableColumn<MovimentacaoDTO, String>,
            TableCell<MovimentacaoDTO, String>> createTipoCellFactory() {
        return column -> new TableCell<>() {
            @Override
            protected void updateItem(String tipo, boolean empty) {
                super.updateItem(tipo, empty);
                if (empty || tipo == null) {
                    setText(null);
                    setStyle("");
                } else {
                    String textoFormatado = formatarTipo(tipo);
                    String cor = obterCorTipo(tipo);
                    setText(textoFormatado);
                    setStyle(String.format(
                            "-fx-font-weight: bold; -fx-text-fill: %s; -fx-alignment: CENTER;",
                            cor
                    ));
                }
            }

            private String formatarTipo(String tipo) {
                if (tipo == null) return "";
                switch (tipo.toUpperCase()) {
                    case "ENTRADA": return "📦 Entrada";
                    case "SAIDA": return "🚚 Saída";
                    case "AJUSTE": return "⚖️ Ajuste";
                    case "TRANSFERENCIA": return "🔄 Transferência";
                    case "DEVOLUCAO": return "↩️ Devolução";
                    case "PERDA": return "💥 Perda";
                    case "RESERVA": return "📋 Reserva";
                    default: return tipo;
                }
            }

            private String obterCorTipo(String tipo) {
                if (tipo == null) return "#95a5a6";
                switch (tipo.toUpperCase()) {
                    case "ENTRADA": return "#27ae60";
                    case "SAIDA": return "#e74c3c";
                    case "AJUSTE": return "#f39c12";
                    case "TRANSFERENCIA": return "#3498db";
                    case "DEVOLUCAO": return "#9b59b6";
                    case "PERDA": return "#e67e22";
                    case "RESERVA": return "#34495e";
                    default: return "#95a5a6";
                }
            }
        };
    }

    // ===== FACTORY PARA QUANTIDADE (COM SINAIS) =====
    private Callback<TableColumn<MovimentacaoDTO, Integer>,
            TableCell<MovimentacaoDTO, Integer>> createQuantidadeCellFactory() {
        return column -> new TableCell<>() {
            @Override
            protected void updateItem(Integer quantidade, boolean empty) {
                super.updateItem(quantidade, empty);
                if (empty || quantidade == null) {
                    setText(null);
                    setStyle("");
                } else {
                    MovimentacaoDTO dto = getTableView().getItems().get(getIndex());
                    String tipo = dto.getTipoMovimentacao();

                    String sinal = "";
                    String cor = "#2c3e50";

                    if ("ENTRADA".equalsIgnoreCase(tipo) || "DEVOLUCAO".equalsIgnoreCase(tipo)) {
                        sinal = "+";
                        cor = "#27ae60";
                    } else if ("SAIDA".equalsIgnoreCase(tipo) || "PERDA".equalsIgnoreCase(tipo)) {
                        sinal = "-";
                        cor = "#e74c3c";
                    }

                    setText(sinal + quantidade);
                    setStyle(String.format("-fx-font-weight: bold; -fx-text-fill: %s;", cor));
                }
            }
        };
    }

    // ===== FACTORY PARA VALIDADE (COM STATUS) =====
    private Callback<TableColumn<MovimentacaoDTO, LocalDate>,
            TableCell<MovimentacaoDTO, LocalDate>> createValidadeCellFactory() {
        return column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate validade, boolean empty) {
                super.updateItem(validade, empty);
                if (empty || validade == null) {
                    setText("N/A");
                    setStyle("-fx-text-fill: #95a5a6; -fx-font-style: italic;");
                } else {
                    setText(DATE_FORMATTER.format(validade));

                    LocalDate hoje = LocalDate.now();
                    if (validade.isBefore(hoje)) {
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    } else if (validade.isBefore(hoje.plusDays(7))) {
                        setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #2c3e50;");
                    }
                }
            }
        };
    }

    // ===== FACTORY PARA AÇÕES (BOTÕES) =====
    private Callback<TableColumn<MovimentacaoDTO, Void>,
            TableCell<MovimentacaoDTO, Void>> createActionsCellFactory(
            TableView<MovimentacaoDTO> table, Window ownerWindow) {
        return column -> new TableCell<>() {
            private final Button btnDetalhes = new Button("👁️");
            private final Button btnEditar = new Button("✏️");

            {
                btnDetalhes.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-min-width: 30; -fx-min-height: 30;");
                btnDetalhes.setOnAction(e -> {
                    MovimentacaoDTO dto = getTableView().getItems().get(getIndex());
                    if (dto != null && dto.getId() != null) {
                        abrirDetalhesMovimentacao(dto.getId(), ownerWindow);
                    }
                });
                btnDetalhes.setTooltip(new Tooltip("Ver detalhes"));

                btnEditar.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-min-width: 30; -fx-min-height: 30;");
                btnEditar.setOnAction(e -> {
                    MovimentacaoDTO dto = getTableView().getItems().get(getIndex());
                    if (dto != null && dto.getId() != null) {
                        abrirEditarMovimentacao(dto.getId(), ownerWindow);
                    }
                });
                btnEditar.setTooltip(new Tooltip("Editar"));

                HBox hbox = new HBox(5, btnDetalhes, btnEditar);
                hbox.setAlignment(Pos.CENTER);
                setGraphic(hbox);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(new HBox(5, btnDetalhes, btnEditar));
                }
            }
        };
    }

    // ===== CONFIGURAR DUPLO-CLIQUE =====
    private void configureDoubleClick(TableView<MovimentacaoDTO> table, Window ownerWindow) {
        table.setRowFactory(tv -> {
            TableRow<MovimentacaoDTO> row = new TableRow<>();

            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    MovimentacaoDTO dto = row.getItem();
                    if (dto != null && dto.getId() != null) {
                        abrirDetalhesMovimentacao(dto.getId(), ownerWindow);
                    }
                }
            });

            return row;
        });
    }

    // ===== ABRIR DETALHES (ATUALIZADO) =====
    private void abrirDetalhesMovimentacao(Long movimentacaoId, Window ownerWindow) {
        try {
            // ✅ CORRETO: usar openWindowModal com controller
            NexusFX.dialogs().openModalWithController(
                    "detalhesMovimentacao",
                    "Detalhes da Movimentação #" + movimentacaoId,
                    ownerWindow,
                    (EstoqueMovimentacaoDetalhesController controller) -> {
                        controller.carregarMovimentacao(movimentacaoId);
                        controller.setOnEditarCallback(id -> {
                            Stage stage = (Stage) controller.getStage();
                            if (stage != null) stage.close();
                            abrirEditarMovimentacao(id, ownerWindow);
                        });
                        controller.setOnFecharCallback(v -> {});
                    }
            );
        } catch (Exception e) {
            NexusFX.alerts().erro(
                    "Erro ao Abrir Detalhes",
                    "Não foi possível abrir os detalhes da movimentação.",
                    "Detalhes: " + e.getMessage(),
                    "MovimentacaoTableService"
            );
        }
    }

    // ===== ABRIR EDIÇÃO (ATUALIZADO) =====
    private void abrirEditarMovimentacao(Long movimentacaoId, Window ownerWindow) {
        try {
            // ✅ CORRETO: usar openWindowModal com controller
            NexusFX.dialogs().openModalWithController(
                    "addeditamovimenta",
                    "Editar Movimentação #" + movimentacaoId,
                    ownerWindow,
                    (EstoqueMovimentacaoFormController controller) -> {
                        controller.inicializarModoEdicao(movimentacaoId);
                    }
            );
        } catch (Exception e) {
            NexusFX.alerts().erro(
                    "Erro ao Abrir Editor",
                    "Não foi possível abrir o editor.",
                    "Detalhes: " + e.getMessage(),
                    "MovimentacaoTableService"
            );
        }
    }

    // ===== CONFIGURAR ESTILO DA TABELA =====
    private void configureTableStyle(TableView<MovimentacaoDTO> table) {
        table.setStyle("-fx-font-size: 12px; -fx-font-family: 'Segoe UI', 'Arial', sans-serif;");

        table.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(MovimentacaoDTO item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setStyle("");
                } else {
                    if (getIndex() % 2 == 0) {
                        setStyle("-fx-background-color: #f8f9fa;");
                    } else {
                        setStyle("-fx-background-color: white;");
                    }

                    if (item.getDataMovimentacao() != null &&
                            item.getDataMovimentacao().isAfter(LocalDate.now().minusDays(7))) {
                        setStyle(getStyle() + " -fx-border-color: #3498db20; -fx-border-width: 0 0 0 3;");
                    }
                }
            }
        });
    }

    // ===== MÉTODOS AUXILIARES =====
    public void refreshTable(TableView<MovimentacaoDTO> table) {
        if (table != null) {
            table.refresh();
        }
    }

    public void sortByColumn(TableView<MovimentacaoDTO> table, String columnName, boolean ascending) {
        table.getColumns().stream()
                .filter(col -> columnName.equals(col.getText()))
                .findFirst()
                .ifPresent(col -> {
                    table.getSortOrder().clear();
                    table.getSortOrder().add(col);
                    col.setSortType(ascending ? TableColumn.SortType.ASCENDING : TableColumn.SortType.DESCENDING);
                });
    }
}