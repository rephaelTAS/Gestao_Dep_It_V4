-- ============================================================
-- SCHEMA SQLITE - SISTEMA DE INVENTÁRIO (gestaoDepIt)
-- Versão: v2.0 — Degrau 1: UUID universal + colunas de sincronização
--
-- SIMETRIA COM MYSQL v2 (ratificado #41): após o script de migração
-- do Degrau 3, ambos os bancos terão ESTA mesma estrutura.
--
-- Convenções de sync:
--   device_id TEXT                     → origem da última mutação (desempate LWW)
--   deleted INTEGER NOT NULL DEFAULT 0 → tombstone absoluto (apenas tabelas mutáveis)
--   Tabelas append-only (estoque, historico) NÃO têm deleted/updated_at:
--   imutáveis → convergência por união de UUIDs, conflito impossível.
--
-- Substitui v1.0 por completo. Ambiente novo: apague o .db antigo antes
-- de rodar (CREATE TABLE IF NOT EXISTS não recria tabelas existentes).
-- ============================================================

-- ============================================================
-- TABELA: CATALOGO_PRODUTOS  (PK de negócio: sku — sem UUID)
-- ============================================================
CREATE TABLE IF NOT EXISTS catalogo_produtos (
                                                 sku TEXT PRIMARY KEY,
                                                 tipo_produto TEXT NOT NULL CHECK (tipo_produto IN ('EQUIPAMENTO','PERIFERICO','TONER','CABO','ACESSORIO','SOFTWARE','LICENCA','OUTRO')),
    categoria TEXT NOT NULL,
    marca TEXT NOT NULL,
    modelo TEXT NOT NULL,
    cor TEXT,
    descricao TEXT,
    caracteristicas_tecnicas TEXT,
    total_recebido INTEGER DEFAULT 0,
    preco_unitario REAL,
    iva REAL,
    preco_total REAL,
    ativo INTEGER DEFAULT 1,
    fornecedor TEXT,
    numero_fatura TEXT,
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

-- ============================================================
-- TABELA: FUNCIONARIOS  (PK de negócio: cod_dep — sem UUID)
-- ============================================================
CREATE TABLE IF NOT EXISTS funcionarios (
                                            cod_dep TEXT PRIMARY KEY,
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

-- ============================================================
-- TABELA: USUARIOS  (PK → UUID TEXT)
-- ============================================================
CREATE TABLE IF NOT EXISTS usuarios (
                                        id TEXT PRIMARY KEY,                          -- UUID v4 (fábrica do model)
                                        funcionario_id TEXT NOT NULL,
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
    deleted INTEGER NOT NULL DEFAULT 0,
    FOREIGN KEY (funcionario_id) REFERENCES funcionarios(cod_dep) ON DELETE RESTRICT ON UPDATE CASCADE
    );

CREATE UNIQUE INDEX IF NOT EXISTS uk_usuarios_funcionario ON usuarios(funcionario_id);
CREATE INDEX IF NOT EXISTS idx_usuarios_nivel ON usuarios(nivel_acesso);
CREATE INDEX IF NOT EXISTS idx_usuarios_ativo ON usuarios(ativo);
CREATE INDEX IF NOT EXISTS idx_usuarios_sessao ON usuarios(sessao_atual);
CREATE INDEX IF NOT EXISTS idx_usuarios_updated ON usuarios(updated_at);

-- ============================================================
-- TABELA: INVENTARIO_EQUIPAMENTOS  (PK → UUID TEXT)
-- ============================================================
CREATE TABLE IF NOT EXISTS inventario_equipamentos (
                                                       id TEXT PRIMARY KEY,                          -- UUID v4 (fábrica do model)
                                                       sku_produto TEXT NOT NULL,
                                                       funcionario_id TEXT,
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
    deleted INTEGER NOT NULL DEFAULT 0,
    FOREIGN KEY (sku_produto) REFERENCES catalogo_produtos(sku) ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (funcionario_id) REFERENCES funcionarios(cod_dep) ON DELETE SET NULL ON UPDATE CASCADE
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
-- Sem deleted, sem updated_at: imutável por design do dono (#21–#26).
-- ============================================================
CREATE TABLE IF NOT EXISTS estoque_movimentacoes (
                                                     id TEXT PRIMARY KEY,                          -- UUID v4 (fábrica do model)
                                                     sku_produto TEXT NOT NULL,
                                                     tipo_movimentacao TEXT NOT NULL CHECK (tipo_movimentacao IN ('ENTRADA','SAIDA','AJUSTE','RESERVA')),
    quantidade INTEGER NOT NULL CHECK (quantidade > 0),
    lote TEXT,
    data_movimentacao TEXT NOT NULL DEFAULT CURRENT_DATE,
    data_validade TEXT,
    data_fim_licenca TEXT,
    localizacao TEXT,
    cod_dep_funcionario TEXT NOT NULL,
    motivo TEXT,
    observacoes TEXT,
    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
    device_id TEXT                                -- origem do evento (rastreio de sync)
    );

CREATE INDEX IF NOT EXISTS idx_movimentacoes_sku ON estoque_movimentacoes(sku_produto);
CREATE INDEX IF NOT EXISTS idx_movimentacoes_tipo ON estoque_movimentacoes(tipo_movimentacao);
CREATE INDEX IF NOT EXISTS idx_movimentacoes_data ON estoque_movimentacoes(data_movimentacao);
CREATE INDEX IF NOT EXISTS idx_movimentacoes_funcionario ON estoque_movimentacoes(cod_dep_funcionario);
CREATE INDEX IF NOT EXISTS idx_movimentacoes_lote ON estoque_movimentacoes(lote);

-- ============================================================
-- TABELA: GESTAO_TONERS  (PK → UUID · FKs → TEXT conforme S3)
-- ============================================================
CREATE TABLE IF NOT EXISTS gestao_toners (
                                             id TEXT PRIMARY KEY,                          -- UUID v4 (fábrica do model)
                                             inventario_id TEXT NOT NULL,                  -- UUID de inventario_equipamentos (S3)
                                             sku_produto TEXT NOT NULL,
                                             data_instalacao TEXT NOT NULL,
                                             usuario_responsavel TEXT NOT NULL,            -- UUID de usuarios (S3)
                                             percentagem_restante INTEGER DEFAULT 100 CHECK (percentagem_restante BETWEEN 0 AND 100),
    ciclos_impressao INTEGER DEFAULT 0,
    observacoes TEXT,
    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT DEFAULT CURRENT_TIMESTAMP,
    device_id TEXT,
    deleted INTEGER NOT NULL DEFAULT 0,
    FOREIGN KEY (inventario_id) REFERENCES inventario_equipamentos(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (sku_produto) REFERENCES catalogo_produtos(sku) ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (usuario_responsavel) REFERENCES usuarios(id) ON DELETE RESTRICT ON UPDATE CASCADE
    );

CREATE INDEX IF NOT EXISTS idx_toners_inventario ON gestao_toners(inventario_id);
CREATE INDEX IF NOT EXISTS idx_toners_sku ON gestao_toners(sku_produto);
CREATE INDEX IF NOT EXISTS idx_toners_data ON gestao_toners(data_instalacao);
CREATE INDEX IF NOT EXISTS idx_toners_usuario ON gestao_toners(usuario_responsavel);
CREATE INDEX IF NOT EXISTS idx_toners_updated ON gestao_toners(updated_at);

-- ============================================================
-- TABELA: HISTORICO_EVENTOS  (PK → UUID · LEDGER append-only)
-- 12 tipos = ENUM do MySQL real (OBS-H3 fechada #43).
-- ============================================================
CREATE TABLE IF NOT EXISTS historico_eventos (
                                                 id TEXT PRIMARY KEY,                          -- UUID v4 (fábrica do model)
                                                 tipo_evento TEXT CHECK (tipo_evento IN ('CRIACAO','ATUALIZACAO','DEVOLUCAO','BAIXA','EXCLUSAO','MANUTENCAO','MOVIMENTACAO','LOCALIZACAO','INSTALACAO','LOGIN','LOGOUT','TRANSFERENCIA')),
    sku_produto TEXT NOT NULL,
    funcionario_id TEXT NOT NULL,
    descricao_funcionario TEXT,
    dados_anteriores TEXT,
    dados_novos TEXT NOT NULL,
    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
    device_id TEXT                                -- origem do evento (rastreio de sync)
    );

CREATE INDEX IF NOT EXISTS idx_historico_tipo ON historico_eventos(tipo_evento);
CREATE INDEX IF NOT EXISTS idx_historico_sku ON historico_eventos(sku_produto);
CREATE INDEX IF NOT EXISTS idx_historico_funcionario ON historico_eventos(funcionario_id);
CREATE INDEX IF NOT EXISTS idx_historico_data ON historico_eventos(created_at);

-- ============================================================
-- TABELA: config_servidor_remoto — v1.0 INTOCADA (S7 ratificado #41)
-- Dado local de infraestrutura: FORA do sync, setup por máquina.
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
    parametros_extra TEXT, -- JSON com parâmetros adicionais
    ativo INTEGER DEFAULT 1,
    ultima_conexao TEXT,
    status_conexao TEXT CHECK (status_conexao IN ('CONECTADO','FALHA','TESTANDO','NAO_TESTADO')) DEFAULT 'NAO_TESTADO',
    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT DEFAULT CURRENT_TIMESTAMP
    );

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

-- ============================================================
-- DADOS INICIAIS
-- ============================================================
-- SEED SQL REMOVIDO (S2 ratificado #42): nenhum usuário nasce por script.
--
-- Motivo: um ADMIN pré-existente mantém a guarda do guest FECHADA
-- para sempre — o fluxo First-Run morreria.
--
-- O Initializer (Java) semeará em runtime o PAR guest:
--   1) Funcionarios.novo(Usuario.GUEST_FUNC_ID, "Conta Guest", "Sistema", "TI")
--   2) Usuario.guestPadrao(BCrypt.hashpw(senhaDoBinario, ...))  → GUEST_ID fixo
-- Hash nunca é hardcoded em SQL.
-- ============================================================