-- ============================================================
-- SCHEMA SQLITE - SISTEMA DE INVENTÁRIO (gestaoDepIt)
-- Versão: v2.1 — UUID universal + colunas de sincronização + fatura única
--
-- SIMETRIA COM MYSQL v2: após o script de migração do Degrau 3,
-- ambos os bancos terão ESTA mesma estrutura.
--
-- DECISÕES RATIFICADAS (sessão #1-#20):
--   A) PK UUID em TODAS as tabelas mutáveis (exceto config_servidor_remoto)
--   B) Códigos de negócio (sku, cod_dep) como UNIQUE, não PK
--   C) preco_unitario/iva/preco_total em CENTAVOS (INTEGER)
--   D) updated_at SEMPRE escrito pelo app em ISO-8601 (millis)
--   E) Tabelas append-only (estoque, historico) sem deleted/updated_at
--   F) Partial index uk_config_uma_ativa garante apenas 1 config ativa
--   G) (revogada) FK entre tabelas sincronizadas → ver Letra J
--   H) Seed SQL removido (boot Java semeará guest)
--   I) Ledgers (estoque_movimentacoes, historico_eventos) SEM FK
--      — tolerância a ordem de chegada no sync distribuído
--   J) SEM FOREIGN KEY em NENHUMA tabela sincronizada. A ordem de
--      chegada no sync distribuído não é garantida, e o MySQL remoto
--      (fonte da verdade) também não impõe FK em runtime. Integridade
--      referencial é responsabilidade dos Services (validação antes
--      de gravar), não do schema físico.
--
-- v2.1 — Regra "número de fatura único" ratificada:
--        uk_catalogo_fatura é UNIQUE parcial sobre (numero_fatura).
--        O fornecedor é apenas informativo — NÃO participa da chave.
--        Parcial (WHERE numero_fatura != '') para permitir rascunhos
--        de produto sem fatura ainda atribuída.
-- ============================================================

-- ============================================================
-- TABELA: CATALOGO_PRODUTOS
-- PK: id (UUID) · Código de negócio: sku (UNIQUE)
-- Regra: numero_fatura é UNIQUE quando preenchido (fatura é documento único)
-- ============================================================
CREATE TABLE IF NOT EXISTS catalogo_produtos (
                                                 id TEXT PRIMARY KEY,                          -- UUID v4 (identidade de sync)
                                                 sku TEXT NOT NULL UNIQUE,                     -- Código de negócio (corrigível sem cascata)
                                                 tipo_produto TEXT NOT NULL CHECK (tipo_produto IN ('EQUIPAMENTO','PERIFERICO','TONER','CABO','ACESSORIO','SOFTWARE','LICENCA','OUTRO')),
    categoria TEXT NOT NULL,
    marca TEXT NOT NULL,
    modelo TEXT NOT NULL,
    cor TEXT,
    descricao TEXT,
    caracteristicas_tecnicas TEXT,
    total_recebido INTEGER DEFAULT 0,
    preco_unitario_centavos INTEGER,                       -- CENTAVOS (5990 = 59.90)
    iva_basis_points INTEGER,                                  -- BASIS POINTS (2300 = 23.00%)
    preco_total_centavos INTEGER,                          -- CENTAVOS (calculado)
    ativo INTEGER DEFAULT 1,
    fornecedor TEXT,                                       -- informativo, não é chave
    numero_fatura TEXT,                                    -- UNIQUE parcial (ver índice abaixo)
    fatura_compra BLOB,
    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT DEFAULT CURRENT_TIMESTAMP,
    device_id TEXT,
    deleted INTEGER NOT NULL DEFAULT 0
    );

CREATE INDEX IF NOT EXISTS idx_catalogo_categoria ON catalogo_produtos(categoria);
CREATE INDEX IF NOT EXISTS idx_catalogo_marca ON catalogo_produtos(marca);
CREATE INDEX IF NOT EXISTS idx_catalogo_ativo ON catalogo_produtos(ativo);
CREATE INDEX IF NOT EXISTS idx_catalogo_updated ON catalogo_produtos(updated_at);
-- NOTA: UNIQUE em sku já cria índice automaticamente no SQLite

-- Regra de negócio: número de fatura é único quando preenchido.
-- Um mesmo fornecedor pode ter N produtos (cada um com sua fatura),
-- mas a MESMA fatura não pode ser registrada duas vezes.
-- Parcial: permite produtos-rascunho sem fatura (NULL ou '').
CREATE UNIQUE INDEX IF NOT EXISTS uk_catalogo_fatura
    ON catalogo_produtos (numero_fatura)
    WHERE numero_fatura IS NOT NULL AND numero_fatura != '';

-- ============================================================
-- TABELA: FUNCIONARIOS
-- PK: id (UUID) · Código de negócio: cod_dep (UNIQUE)
-- ============================================================
CREATE TABLE IF NOT EXISTS funcionarios (
                                            id TEXT PRIMARY KEY,                          -- UUID v4 (identidade de sync)
                                            cod_dep TEXT NOT NULL UNIQUE,                 -- Código de negócio (corrigível sem cascata)
                                            cod_dep_anterior TEXT,
                                            nome TEXT NOT NULL,
                                            funcao TEXT,
                                            departamento TEXT,
                                            local_trabalho TEXT,
                                            email TEXT,
                                            telefone TEXT,
                                            imagem_perfil BLOB,
                                            tipo_imagem TEXT,
                                            tamanho_imagem INTEGER,
                                            ativo INTEGER DEFAULT 1,
                                            created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                                            updated_at TEXT DEFAULT CURRENT_TIMESTAMP,
                                            device_id TEXT,
                                            deleted INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_funcionarios_departamento ON funcionarios(departamento);
CREATE INDEX IF NOT EXISTS idx_funcionarios_ativo ON funcionarios(ativo);
CREATE INDEX IF NOT EXISTS idx_funcionarios_updated ON funcionarios(updated_at);
-- NOTA: UNIQUE em cod_dep já cria índice automaticamente no SQLite

-- ============================================================
-- TABELA: USUARIOS  (PK → UUID TEXT)
-- funcionario_id: referência funcional a funcionarios.cod_dep (SEM FK — Letra J)
-- ============================================================
CREATE TABLE IF NOT EXISTS usuarios (
                                        id TEXT PRIMARY KEY,                          -- UUID v4 (identidade de sync)
                                        funcionario_id TEXT NOT NULL,                 -- Referência funcional a funcionarios.cod_dep
                                        nome TEXT NOT NULL,
                                        email TEXT NOT NULL UNIQUE,
                                        senha_hash TEXT NOT NULL,
                                        nivel_acesso TEXT NOT NULL CHECK (nivel_acesso IN ('ADMIN','GESTOR','SUPERVISOR','OPERADOR','READONLY')) DEFAULT 'OPERADOR',
    ativo INTEGER DEFAULT 1,
    ultimo_login TEXT,
    ip_ultimo_login TEXT,
    sessao_atual TEXT,
    expiracao_sessao TEXT,
    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT DEFAULT CURRENT_TIMESTAMP,
    device_id TEXT,
    deleted INTEGER NOT NULL DEFAULT 0
    );

CREATE UNIQUE INDEX IF NOT EXISTS uk_usuarios_funcionario ON usuarios(funcionario_id);
CREATE INDEX IF NOT EXISTS idx_usuarios_nivel ON usuarios(nivel_acesso);
CREATE INDEX IF NOT EXISTS idx_usuarios_ativo ON usuarios(ativo);
CREATE INDEX IF NOT EXISTS idx_usuarios_sessao ON usuarios(sessao_atual);
CREATE INDEX IF NOT EXISTS idx_usuarios_updated ON usuarios(updated_at);

-- ============================================================
-- TABELA: INVENTARIO_EQUIPAMENTOS  (PK → UUID TEXT)
-- sku_produto: referência funcional a catalogo_produtos.sku  (SEM FK — Letra J)
-- funcionario_id: referência funcional a funcionarios.cod_dep (SEM FK — Letra J)
-- ============================================================
CREATE TABLE IF NOT EXISTS inventario_equipamentos (
                                                       id TEXT PRIMARY KEY,                          -- UUID v4 (identidade de sync)
                                                       sku_produto TEXT NOT NULL,                    -- Referência funcional a catalogo_produtos.sku
                                                       funcionario_id TEXT,                          -- Referência funcional a funcionarios.cod_dep
                                                       num_serie TEXT UNIQUE,
                                                       endereco_mac TEXT,
                                                       data_aquisicao TEXT,
                                                       data_instalacao TEXT,
                                                       data_ultima_verificacao TEXT,
                                                       numero_fatura TEXT,
                                                       localizacao TEXT,
                                                       departamento TEXT,
                                                       status TEXT NOT NULL CHECK (status IN ('ATIVO','MANUTENCAO','BAIXADO','RESERVA','EM_USO')) DEFAULT 'ATIVO',
    condicao TEXT NOT NULL CHECK (condicao IN ('OTIMO','BOM','REGULAR','CRITICO')) DEFAULT 'BOM',
    documento_entrega BLOB,
    documento_devolucao BLOB,
    observacoes TEXT,
    devolucao INTEGER,
    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT DEFAULT CURRENT_TIMESTAMP,
    device_id TEXT,
    deleted INTEGER NOT NULL DEFAULT 0
    );

CREATE INDEX IF NOT EXISTS idx_inventario_sku ON inventario_equipamentos(sku_produto);
CREATE INDEX IF NOT EXISTS idx_inventario_funcionario ON inventario_equipamentos(funcionario_id);
CREATE INDEX IF NOT EXISTS idx_inventario_status ON inventario_equipamentos(status);
CREATE INDEX IF NOT EXISTS idx_inventario_departamento ON inventario_equipamentos(departamento);
CREATE INDEX IF NOT EXISTS idx_inventario_localizacao ON inventario_equipamentos(localizacao);
CREATE INDEX IF NOT EXISTS idx_inventario_mac ON inventario_equipamentos(endereco_mac);
CREATE INDEX IF NOT EXISTS idx_inventario_updated ON inventario_equipamentos(updated_at);

-- ============================================================
-- TABELA: ESTOQUE_MOVIMENTACOES  (PK → UUID · LEDGER append-only)
-- DECISÃO RATIFICADA (Letra I): SEM FOREIGN KEY
-- ============================================================
CREATE TABLE IF NOT EXISTS estoque_movimentacoes (
                                                     id TEXT PRIMARY KEY,                          -- UUID v4 (identidade de sync)
                                                     sku_produto TEXT NOT NULL,                    -- Cópia histórica do SKU (ledger)
                                                     tipo_movimentacao TEXT NOT NULL CHECK (tipo_movimentacao IN ('ENTRADA','SAIDA','AJUSTE','RESERVA')),
    quantidade INTEGER NOT NULL CHECK (quantidade > 0),
    lote TEXT,
    data_movimentacao TEXT NOT NULL DEFAULT CURRENT_DATE,
    data_validade TEXT,
    data_fim_licenca TEXT,
    localizacao TEXT,
    funcionario_id TEXT NOT NULL,                 -- Referência FUNCIONAL a funcionarios.cod_dep
-- SEM constraint — decisão ratificada (Letra I):
-- ledger append-only não quebra por pai ausente
-- (ordem de chegada no sync não é garantida;
--  histórico órfão é registro do passado, não corrupção)
    motivo TEXT,
    observacoes TEXT,
    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
    device_id TEXT                                -- origem do evento (rastreio de sync)
    );

CREATE INDEX IF NOT EXISTS idx_movimentacoes_sku ON estoque_movimentacoes(sku_produto);
CREATE INDEX IF NOT EXISTS idx_movimentacoes_tipo ON estoque_movimentacoes(tipo_movimentacao);
CREATE INDEX IF NOT EXISTS idx_movimentacoes_data ON estoque_movimentacoes(data_movimentacao);
CREATE INDEX IF NOT EXISTS idx_movimentacoes_funcionario ON estoque_movimentacoes(funcionario_id);
CREATE INDEX IF NOT EXISTS idx_movimentacoes_lote ON estoque_movimentacoes(lote);

-- ============================================================
-- TABELA: GESTAO_TONERS  (PK → UUID)
-- inventario_id / produto_id / usuario_responsavel: referências funcionais (SEM FK — Letra J)
-- ============================================================
CREATE TABLE IF NOT EXISTS gestao_toners (
                                             id TEXT PRIMARY KEY,                          -- UUID v4 (identidade de sync)
                                             inventario_id TEXT NOT NULL,                  -- Referência funcional a inventario_equipamentos.id
                                             produto_id TEXT NOT NULL,                     -- Referência funcional a catalogo_produtos.id
                                             sku_produto TEXT,                             -- Cópia histórica do SKU (para consultas/relatórios)
                                             data_instalacao TEXT NOT NULL,
                                             usuario_responsavel TEXT NOT NULL,            -- Referência funcional a usuarios.id
                                             percentagem_restante INTEGER DEFAULT 100 CHECK (percentagem_restante BETWEEN 0 AND 100),
    ciclos_impressao INTEGER DEFAULT 0,
    observacoes TEXT,
    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT DEFAULT CURRENT_TIMESTAMP,
    device_id TEXT,
    deleted INTEGER NOT NULL DEFAULT 0
    );

CREATE INDEX IF NOT EXISTS idx_toners_inventario ON gestao_toners(inventario_id);
CREATE INDEX IF NOT EXISTS idx_toners_produto ON gestao_toners(produto_id);
CREATE INDEX IF NOT EXISTS idx_toners_sku ON gestao_toners(sku_produto);
CREATE INDEX IF NOT EXISTS idx_toners_data ON gestao_toners(data_instalacao);
CREATE INDEX IF NOT EXISTS idx_toners_usuario ON gestao_toners(usuario_responsavel);
CREATE INDEX IF NOT EXISTS idx_toners_updated ON gestao_toners(updated_at);

-- ============================================================
-- TABELA: HISTORICO_EVENTOS  (PK → UUID · LEDGER append-only)
-- DECISÃO RATIFICADA (Letra I): SEM FOREIGN KEY
-- ============================================================
CREATE TABLE IF NOT EXISTS historico_eventos (
                                                 id TEXT PRIMARY KEY,                          -- UUID v4 (identidade de sync)
                                                 tipo_evento TEXT CHECK (tipo_evento IN ('CRIACAO','ATUALIZACAO','DEVOLUCAO','BAIXA','EXCLUSAO','MANUTENCAO','MOVIMENTACAO','LOCALIZACAO','INSTALACAO','LOGIN','LOGOUT','TRANSFERENCIA')),
    sku_produto TEXT NOT NULL,                    -- Cópia histórica do SKU (ledger)
    numSerie TEXT,
    funcionario_id TEXT NOT NULL,                 -- Referência FUNCIONAL a funcionarios.cod_dep
-- SEM constraint — decisão ratificada (Letra I):
-- ledger append-only não quebra por pai ausente
-- (ordem de chegada no sync não é garantida;
--  histórico órfão é registro do passado, não corrupção)
    descricao_funcionario TEXT,
    dados_anteriores TEXT,
    dados_novos TEXT NOT NULL,
    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
    device_id TEXT                                -- origem do evento (rastreio de sync)
    );
CREATE INDEX IF NOT EXISTS idx_historico_chave ON historico_eventos (sku_produto, funcionario_id, numSerie);
CREATE INDEX IF NOT EXISTS idx_historico_tipo ON historico_eventos(tipo_evento);
CREATE INDEX IF NOT EXISTS idx_historico_sku ON historico_eventos(sku_produto);
CREATE INDEX IF NOT EXISTS idx_historico_funcionario ON historico_eventos(funcionario_id);
CREATE INDEX IF NOT EXISTS idx_historico_data ON historico_eventos(created_at);

-- ============================================================
-- TABELA: config_servidor_remoto — INFRA LOCAL (FORA do sync)
-- Único AUTOINCREMENT restante do sistema inteiro.
-- ============================================================
CREATE TABLE IF NOT EXISTS config_servidor_remoto (
                                                      id INTEGER PRIMARY KEY AUTOINCREMENT,
                                                      nome_config TEXT NOT NULL UNIQUE,
                                                      tipo_banco TEXT NOT NULL CHECK (tipo_banco IN ('MYSQL','POSTGRESQL','ORACLE','SQLSERVER','MARIADB')) DEFAULT 'MYSQL',
    host TEXT NOT NULL,
    porta TEXT NOT NULL,
    database_name TEXT NOT NULL,
    usuario TEXT NOT NULL,
    senha TEXT NOT NULL,
    parametros_extra TEXT,
    ativo INTEGER DEFAULT 1,
    ultima_conexao TEXT,
    status_conexao TEXT CHECK (status_conexao IN ('CONECTADO','FALHA','TESTANDO','NAO_TESTADO')) DEFAULT 'NAO_TESTADO',
    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT DEFAULT CURRENT_TIMESTAMP
    );

-- ✨ Garantia de banco: apenas 1 config ativa por vez
CREATE UNIQUE INDEX IF NOT EXISTS uk_config_uma_ativa
    ON config_servidor_remoto(ativo) WHERE ativo = 1;

CREATE INDEX IF NOT EXISTS idx_config_servidor_ativo ON config_servidor_remoto(ativo);
CREATE INDEX IF NOT EXISTS idx_config_servidor_tipo ON config_servidor_remoto(tipo_banco);

-- ============================================================
-- SINCRONIZAÇÃO — metadados LOCAIS (nunca sincronizar, nunca no MySQL)
-- ============================================================
CREATE TABLE IF NOT EXISTS app_meta (
                                        chave TEXT PRIMARY KEY,
                                        valor TEXT
);

CREATE TABLE IF NOT EXISTS pending_sync (
                                            id          TEXT PRIMARY KEY,
                                            tabela      TEXT NOT NULL,
                                            registro_id TEXT NOT NULL,
                                            operacao    TEXT NOT NULL CHECK (operacao IN ('UPSERT','DELETE')),
    criado_em   TEXT NOT NULL,
    tentativas  INTEGER DEFAULT 0
    );

CREATE INDEX IF NOT EXISTS idx_pending_criado ON pending_sync(criado_em);

CREATE TABLE IF NOT EXISTS sync_state (
                                          entidade        TEXT PRIMARY KEY,
                                          last_sync       TEXT,
                                          ultimo_status   TEXT,
                                          ultima_mensagem TEXT,
                                          atualizado_em   TEXT
);

-- Migration: tabela sequencias (usada por NumeroSerieGenerator e GerarSkuService)
CREATE TABLE IF NOT EXISTS sequencias (
                                          prefixo      TEXT PRIMARY KEY,
                                          ultimo_valor TEXT NOT NULL,
                                          updated_at   TEXT NOT NULL
);

-- ============================================================
-- RELATÓRIOS — histórico (ledger append-only) e modelos LEGO salvos
-- ============================================================
CREATE TABLE IF NOT EXISTS relatorio_historico (
                                                   id TEXT PRIMARY KEY,
                                                   titulo TEXT NOT NULL,
                                                   tabelas TEXT NOT NULL,        -- CSV: "inventario,catalogo"
                                                   filtros TEXT NOT NULL,        -- JSON serializado
                                                   gerado_em TEXT NOT NULL,      -- ISO
                                                   device_id TEXT
);

CREATE INDEX IF NOT EXISTS idx_relatorio_hist_gerado ON relatorio_historico(gerado_em);

CREATE TABLE IF NOT EXISTS relatorio_modelos (
                                                 id TEXT PRIMARY KEY,
                                                 nome TEXT NOT NULL UNIQUE,
                                                 tabela_base TEXT NOT NULL,
                                                 colunas_base TEXT NOT NULL,
                                                 tabelas_combinadas TEXT NOT NULL,
                                                 filtros TEXT NOT NULL,
                                                 criado_em TEXT NOT NULL
);

-- ============================================================
-- DADOS INICIAIS
-- ============================================================
-- SEED SQL REMOVIDO (S2 ratificado #42): nenhum usuário nasce por script.
--
-- O Initializer (Java) semeará em runtime o PAR guest:
--   1) Funcionarios.novo(GUEST_FUNC_ID, GUEST_COD_DEP, "Conta Guest", "Sistema", "TI")
--   2) Usuario.guestPadrao(BCrypt.hashpw(senhaDoBinario, ...)) → GUEST_ID fixo
-- Hash nunca é hardcoded em SQL.
--
-- Constantes Guest (fonte única):
--   GUEST_FUNC_ID = "guest-func-0000-0000-000000000000"
--   GUEST_COD_DEP = "GUEST"
--   GUEST_ID      = "guest-0000-0000-000000000000"
-- ============================================================