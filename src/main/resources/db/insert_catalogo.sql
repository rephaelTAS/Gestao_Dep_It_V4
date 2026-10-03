-- ============================================================
-- Bootstrap CATALOGO_PRODUTOS — MySQL remoto → SQLite local
-- Execução: UMA VEZ. Rode com: sqlite3 ~/.local/share/GestaoDepIt/inventario.db < insert_catalogo.sql
-- Depois: apague este arquivo.
--
-- 78 produtos exportados do MySQL.
-- UUIDs v4 gerados no SQLite (randomblob) — INSERT OR IGNORE protege por UNIQUE(sku).
-- ============================================================



-- ---------- 1 ----------
INSERT OR IGNORE INTO catalogo_produtos (
    id, sku, tipo_produto, categoria, marca, modelo, cor, descricao, caracteristicas_tecnicas,
    total_recebido, preco_unitario_centavos, iva_basis_points, preco_total_centavos,
    ativo, fornecedor, numero_fatura, fatura_compra,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'CMP-DEL-VOS-001', 'EQUIPAMENTO', 'Departamento Financeiro', 'Dell', 'Vostro 15', 'Preto',
    '', '', 3, 0, 0, 0, 1, NULL, NULL, NULL,
    '2026-04-24T14:39:58', '2026-06-03T09:55:15', 'REMOTE-IMPORT', 0
);

-- ---------- 2 ----------
INSERT OR IGNORE INTO catalogo_produtos (
    id, sku, tipo_produto, categoria, marca, modelo, cor, descricao, caracteristicas_tecnicas,
    total_recebido, preco_unitario_centavos, iva_basis_points, preco_total_centavos,
    ativo, fornecedor, numero_fatura, fatura_compra,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'CMP-HPX-15D-001', 'EQUIPAMENTO', 'Departamento Comercial', 'HP', '15-dy2795wm', '',
    '', '', 13, 0, 0, 0, 1, 'Pcdig', '14', NULL,
    '2026-04-24T14:39:58', '2026-06-29T12:59:24', 'REMOTE-IMPORT', 0
);

-- ---------- 3 ----------
INSERT OR IGNORE INTO catalogo_produtos (
    id, sku, tipo_produto, categoria, marca, modelo, cor, descricao, caracteristicas_tecnicas,
    total_recebido, preco_unitario_centavos, iva_basis_points, preco_total_centavos,
    ativo, fornecedor, numero_fatura, fatura_compra,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'CMP-HPX-15F-001', 'EQUIPAMENTO', 'Departamento Financeiro', 'Hp', '15-fd1000np', '',
    '', '', 1, 0, 0, 0, 1, NULL, NULL, NULL,
    '2026-04-24T14:40:01', '2026-04-24T14:40:01', 'REMOTE-IMPORT', 0
);

-- ---------- 4 ----------
INSERT OR IGNORE INTO catalogo_produtos (
    id, sku, tipo_produto, categoria, marca, modelo, cor, descricao, caracteristicas_tecnicas,
    total_recebido, preco_unitario_centavos, iva_basis_points, preco_total_centavos,
    ativo, fornecedor, numero_fatura, fatura_compra,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'CMP-HPX-250-001', 'EQUIPAMENTO', 'Departamento de Armazém', 'Hp', '250 G7', '',
    '', '', 1, 0, 0, 0, 1, NULL, NULL, NULL,
    '2026-04-24T14:40:01', '2026-04-24T14:40:01', 'REMOTE-IMPORT', 0
);

-- ---------- 5 ----------
INSERT OR IGNORE INTO catalogo_produtos (
    id, sku, tipo_produto, categoria, marca, modelo, cor, descricao, caracteristicas_tecnicas,
    total_recebido, preco_unitario_centavos, iva_basis_points, preco_total_centavos,
    ativo, fornecedor, numero_fatura, fatura_compra,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'CMP-HPX-250-002', 'EQUIPAMENTO', 'Departamento Financeiro', 'Hp', '250 G9', '',
    '', '', 1, 0, 0, 0, 1, NULL, NULL, NULL,
    '2026-04-24T14:40:01', '2026-04-24T14:40:01', 'REMOTE-IMPORT', 0
);

-- ---------- 6 ----------
INSERT OR IGNORE INTO catalogo_produtos (
    id, sku, tipo_produto, categoria, marca, modelo, cor, descricao, caracteristicas_tecnicas,
    total_recebido, preco_unitario_centavos, iva_basis_points, preco_total_centavos,
    ativo, fornecedor, numero_fatura, fatura_compra,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'CMP-HPX-316-001', 'EQUIPAMENTO', 'Departamento Jurídico', 'Hp', '3165NGW', '',
    '', '', 2, 0, 0, 0, 1, NULL, NULL, NULL,
    '2026-04-24T14:40:00', '2026-04-24T14:40:01', 'REMOTE-IMPORT', 0
);

-- ---------- 7 ----------
INSERT OR IGNORE INTO catalogo_produtos (
    id, sku, tipo_produto, categoria, marca, modelo, cor, descricao, caracteristicas_tecnicas,
    total_recebido, preco_unitario_centavos, iva_basis_points, preco_total_centavos,
    ativo, fornecedor, numero_fatura, fatura_compra,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'CMP-HPX-HP2-001', 'EQUIPAMENTO', 'Departamento Financeiro', 'Hp', 'Hp 250 G9 i5', '',
    '', '', 3, 0, 0, 0, 1, NULL, NULL, NULL,
    '2026-04-24T14:39:58', '2026-04-24T14:40:01', 'REMOTE-IMPORT', 0
);

-- ---------- 8 ----------
INSERT OR IGNORE INTO catalogo_produtos (
    id, sku, tipo_produto, categoria, marca, modelo, cor, descricao, caracteristicas_tecnicas,
    total_recebido, preco_unitario_centavos, iva_basis_points, preco_total_centavos,
    ativo, fornecedor, numero_fatura, fatura_compra,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'CMP-HPX-HP2-002', 'EQUIPAMENTO', 'Departamento Industrial (POM)', 'HP', 'HP 250 G7', '',
    '', '', 3, 0, 0, 0, 1, NULL, NULL, NULL,
    '2026-04-24T14:39:59', '2026-04-24T14:40:02', 'REMOTE-IMPORT', 0
);

-- ---------- 9 ----------
INSERT OR IGNORE INTO catalogo_produtos (
    id, sku, tipo_produto, categoria, marca, modelo, cor, descricao, caracteristicas_tecnicas,
    total_recebido, preco_unitario_centavos, iva_basis_points, preco_total_centavos,
    ativo, fornecedor, numero_fatura, fatura_compra,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'CMP-HPX-INT-001', 'EQUIPAMENTO', 'Direção Geral', 'HP', 'Intel core i5', '',
    '', '', 4, 0, 0, 0, 1, NULL, NULL, NULL,
    '2026-04-24T14:39:58', '2026-04-24T14:40:01', 'REMOTE-IMPORT', 0
);

-- ---------- 10 ----------
INSERT OR IGNORE INTO catalogo_produtos (
    id, sku, tipo_produto, categoria, marca, modelo, cor, descricao, caracteristicas_tecnicas,
    total_recebido, preco_unitario_centavos, iva_basis_points, preco_total_centavos,
    ativo, fornecedor, numero_fatura, fatura_compra,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'CMP-HPX-RTL-001', 'EQUIPAMENTO', 'Departamento de Armazém', 'HP', 'RTL8822CE', '',
    '', '', 19, 0, 0, 0, 1, NULL, NULL, NULL,
    '2026-04-24T14:39:58', '2026-04-24T14:40:02', 'REMOTE-IMPORT', 0
);

-- ---------- 11 ----------
INSERT OR IGNORE INTO catalogo_produtos (
    id, sku, tipo_produto, categoria, marca, modelo, cor, descricao, caracteristicas_tecnicas,
    total_recebido, preco_unitario_centavos, iva_basis_points, preco_total_centavos,
    ativo, fornecedor, numero_fatura, fatura_compra,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'CMP-HPX-RTL-002', 'EQUIPAMENTO', 'Departamento de RH', 'HP', 'RTL8821CE', '',
    '', '', 2, 0, 0, 0, 1, NULL, NULL, NULL,
    '2026-04-24T14:39:59', '2026-04-24T14:40:01', 'REMOTE-IMPORT', 0
);

-- ---------- 12 ----------
INSERT OR IGNORE INTO catalogo_produtos (
    id, sku, tipo_produto, categoria, marca, modelo, cor, descricao, caracteristicas_tecnicas,
    total_recebido, preco_unitario_centavos, iva_basis_points, preco_total_centavos,
    ativo, fornecedor, numero_fatura, fatura_compra,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'CMP-HPX-TPN-001', 'EQUIPAMENTO', 'Departamento Jurídico', 'Hp', 'tpn-i130', '',
    '', '', 2, 0, 0, 0, 1, NULL, NULL, NULL,
    '2026-04-24T14:39:58', '2026-04-24T14:40:01', 'REMOTE-IMPORT', 0
);

-- ---------- 13 ----------
INSERT OR IGNORE INTO catalogo_produtos (
    id, sku, tipo_produto, categoria, marca, modelo, cor, descricao, caracteristicas_tecnicas,
    total_recebido, preco_unitario_centavos, iva_basis_points, preco_total_centavos,
    ativo, fornecedor, numero_fatura, fatura_compra,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'CMP-HPX-W4M-001', 'EQUIPAMENTO', 'Departamento de RH', 'Hp', 'w4m72ea#ab9', '',
    '', '', 2, 0, 0, 0, 1, NULL, NULL, NULL,
    '2026-04-24T14:39:59', '2026-04-24T14:39:59', 'REMOTE-IMPORT', 0
);

-- ---------- 14 ----------
INSERT OR IGNORE INTO catalogo_produtos (
    id, sku, tipo_produto, categoria, marca, modelo, cor, descricao, caracteristicas_tecnicas,
    total_recebido, preco_unitario_centavos, iva_basis_points, preco_total_centavos,
    ativo, fornecedor, numero_fatura, fatura_compra,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'CMP-LEN-LEN-001', 'EQUIPAMENTO', 'Laptop', 'Lenovo', 'Lenovo V14 G2 ITL', 'Preto',
    '', '', 2, 0, 0, 0, 1, NULL, NULL, NULL,
    '2026-04-24T14:40:01', '2026-06-05T09:57:32', 'REMOTE-IMPORT', 0
);

-- ---------- 15 ----------
INSERT OR IGNORE INTO catalogo_produtos (
    id, sku, tipo_produto, categoria, marca, modelo, cor, descricao, caracteristicas_tecnicas,
    total_recebido, preco_unitario_centavos, iva_basis_points, preco_total_centavos,
    ativo, fornecedor, numero_fatura, fatura_compra,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'CMP-LEN-PF9-001', 'EQUIPAMENTO', 'Departamento de RH', 'Lenovo', 'PF9XB1C080', '',
    '', '', 1, 0, 0, 0, 1, NULL, NULL, NULL,
    '2026-04-24T14:39:59', '2026-04-24T14:39:59', 'REMOTE-IMPORT', 0
);

-- ---------- 16 ----------
INSERT OR IGNORE INTO catalogo_produtos (
    id, sku, tipo_produto, categoria, marca, modelo, cor, descricao, caracteristicas_tecnicas,
    total_recebido, preco_unitario_centavos, iva_basis_points, preco_total_centavos,
    ativo, fornecedor, numero_fatura, fatura_compra,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'CMP-THI-CAR-001', 'EQUIPAMENTO', 'Departamento Comercial', 'ThinkPad', 'Carbon Gen 11', '',
    '', '', 1, 0, 0, 0, 1, NULL, NULL, NULL,
    '2026-04-24T14:39:58', '2026-04-24T14:39:58', 'REMOTE-IMPORT', 0
);

-- ---------- 17 ----------
INSERT OR IGNORE INTO catalogo_produtos (
    id, sku, tipo_produto, categoria, marca, modelo, cor, descricao, caracteristicas_tecnicas,
    total_recebido, preco_unitario_centavos, iva_basis_points, preco_total_centavos,
    ativo, fornecedor, numero_fatura, fatura_compra,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'CMP-THI-LEN-001', 'EQUIPAMENTO', 'Laptop', 'Lenovo', 'ThinkPad', 'Preto',
    '', '', 2, 0, 0, 0, 1, NULL, NULL, NULL,
    '2026-04-24T14:39:58', '2026-06-05T10:56:20', 'REMOTE-IMPORT', 0
);

-- ---------- 18 ----------
INSERT OR IGNORE INTO catalogo_produtos (
    id, sku, tipo_produto, categoria, marca, modelo, cor, descricao, caracteristicas_tecnicas,
    total_recebido, preco_unitario_centavos, iva_basis_points, preco_total_centavos,
    ativo, fornecedor, numero_fatura, fatura_compra,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'CMP-TOS-PYS-001', 'EQUIPAMENTO', 'Departamento de Plantação', 'Toshiba Dynabook', 'PYS46E-02N01REP', '',
    '', '', 2, 0, 0, 0, 1, NULL, NULL, NULL,
    '2026-04-24T14:39:58', '2026-04-24T14:40:01', 'REMOTE-IMPORT', 0
);

-- ---------- 19 ----------
INSERT OR IGNORE INTO catalogo_produtos (
    id, sku, tipo_produto, categoria, marca, modelo, cor, descricao, caracteristicas_tecnicas,
    total_recebido, preco_unitario_centavos, iva_basis_points, preco_total_centavos,
    ativo, fornecedor, numero_fatura, fatura_compra,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'CPU-DEL-CN0-012', 'EQUIPAMENTO', 'Pc Messa', 'Dell', 'CN-01M2XX', 'Preto',
    '', '', 8, 0, 0, 0, 1, NULL, NULL, NULL,
    '2026-04-25T10:32:52', '2026-04-25T10:32:52', 'REMOTE-IMPORT', 0
);

-- ---------- 20 ----------
INSERT OR IGNORE INTO catalogo_produtos (
    id, sku, tipo_produto, categoria, marca, modelo, cor, descricao, caracteristicas_tecnicas,
    total_recebido, preco_unitario_centavos, iva_basis_points, preco_total_centavos,
    ativo, fornecedor, numero_fatura, fatura_compra,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'CPU-DEL-D13-001', 'EQUIPAMENTO', 'Departamento Industrial (POM)', 'Dell', 'D13S', '',
    '', '', 6, 0, 0, 0, 1, NULL, NULL, NULL,
    '2026-04-24T14:39:59', '2026-04-24T14:40:02', 'REMOTE-IMPORT', 0
);

-- ---------- 21 ----------
INSERT OR IGNORE INTO catalogo_produtos (
    id, sku, tipo_produto, categoria, marca, modelo, cor, descricao, caracteristicas_tecnicas,
    total_recebido, preco_unitario_centavos, iva_basis_points, preco_total_centavos,
    ativo, fornecedor, numero_fatura, fatura_compra,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'CPU-DEL-D18-001', 'EQUIPAMENTO', 'Departamento Financeiro', 'Dell', 'D18M', '',
    '', '', 42, 0, 0, 0, 1, NULL, NULL, NULL,
    '2026-04-24T14:39:58', '2026-04-24T14:40:02', 'REMOTE-IMPORT', 0
);

-- ---------- 22 ----------
INSERT OR IGNORE INTO catalogo_produtos (
    id, sku, tipo_produto, categoria, marca, modelo, cor, descricao, caracteristicas_tecnicas,
    total_recebido, preco_unitario_centavos, iva_basis_points, preco_total_centavos,
    ativo, fornecedor, numero_fatura, fatura_compra,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'CPU-DEL-OPT-001', 'EQUIPAMENTO', 'Departamento Financeiro', 'Dell', 'Optiplex 3050', '',
    '', '', 7, 0, 0, 0, 1, NULL, NULL, NULL,
    '2026-04-24T14:39:58', '2026-04-24T14:40:02', 'REMOTE-IMPORT', 0
);

-- ---------- 23 ----------
INSERT OR IGNORE INTO catalogo_produtos (
    id, sku, tipo_produto, categoria, marca, modelo, cor, descricao, caracteristicas_tecnicas,
    total_recebido, preco_unitario_centavos, iva_basis_points, preco_total_centavos,
    ativo, fornecedor, numero_fatura, fatura_compra,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'CPU-DEL-VES-001', 'EQUIPAMENTO', 'Departamento Industrial (POM)', 'Dell', 'Vestro 3470', '',
    '', '', 2, 0, 0, 0, 1, NULL, NULL, NULL,
    '2026-04-24T14:39:59', '2026-04-24T14:40:02', 'REMOTE-IMPORT', 0
);

-- ---------- 24 ----------
INSERT OR IGNORE INTO catalogo_produtos (
    id, sku, tipo_produto, categoria, marca, modelo, cor, descricao, caracteristicas_tecnicas,
    total_recebido, preco_unitario_centavos, iva_basis_points, preco_total_centavos,
    ativo, fornecedor, numero_fatura, fatura_compra,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'IMP-BRO-DCP-001', 'EQUIPAMENTO', 'Impressora', 'Brother', 'DCP-L2550DN', '',
    'A Brother DCP-L2550DN é uma impressora multifunções laser monocromática 3 em 1 (impressão, cópia e digitalização), projetada para ambientes de escritório ou uso profissional intensivo.',
    '', 1, 25000, 0, 25000, 1, NULL, NULL, NULL,
    '2026-04-24T14:39:59', '2026-05-06T09:21:12', 'REMOTE-IMPORT', 0
);

-- ---------- 25 ----------
INSERT OR IGNORE INTO catalogo_produtos (
    id, sku, tipo_produto, categoria, marca, modelo, cor, descricao, caracteristicas_tecnicas,
    total_recebido, preco_unitario_centavos, iva_basis_points, preco_total_centavos,
    ativo, fornecedor, numero_fatura, fatura_compra,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'IMP-CAN-PIX-001', 'EQUIPAMENTO', 'Impressora', 'canon', 'Pixma', '',
    'A Canon PIXMA G2410 é uma impressora multifunções a jato de tinta com sistema de tanques recarregáveis (MegaTank), indicada para uso doméstico ou pequenos escritórios.',
    '', 6, 19000, 0, 19000, 1, NULL, NULL, NULL,
    '2026-04-24T14:39:58', '2026-05-06T09:23:29', 'REMOTE-IMPORT', 0
);

-- ---------- 26 ----------
INSERT OR IGNORE INTO catalogo_produtos (
    id, sku, tipo_produto, categoria, marca, modelo, cor, descricao, caracteristicas_tecnicas,
    total_recebido, preco_unitario_centavos, iva_basis_points, preco_total_centavos,
    ativo, fornecedor, numero_fatura, fatura_compra,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'IMP-HPX-COL-001', 'EQUIPAMENTO', 'Departamento de Armazém', 'HP', 'ColorLaserJet Pro MFP M180n', '',
    '', '', 11, 0, 0, 0, 1, NULL, NULL, NULL,
    '2026-04-24T14:39:58', '2026-04-24T14:40:01', 'REMOTE-IMPORT', 0
);

-- ---------- 27 ----------
INSERT OR IGNORE INTO catalogo_produtos (
    id, sku, tipo_produto, categoria, marca, modelo, cor, descricao, caracteristicas_tecnicas,
    total_recebido, preco_unitario_centavos, iva_basis_points, preco_total_centavos,
    ativo, fornecedor, numero_fatura, fatura_compra,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'IMP-HPX-COL-002', 'EQUIPAMENTO', 'Departamento de Infraestruturas', 'Hp', 'ColorLaserJet Pro MFP M282nw', '',
    '', '', 26, 0, 0, 0, 1, NULL, NULL, NULL,
    '2026-04-24T14:39:59', '2026-04-24T14:40:02', 'REMOTE-IMPORT', 0
);

-- ---------- 28 ----------
INSERT OR IGNORE INTO catalogo_produtos (
    id, sku, tipo_produto, categoria, marca, modelo, cor, descricao, caracteristicas_tecnicas,
    total_recebido, preco_unitario_centavos, iva_basis_points, preco_total_centavos,
    ativo, fornecedor, numero_fatura, fatura_compra,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'IMP-HPX-COL-003', 'EQUIPAMENTO', 'Departamento Financeiro', 'Hp', 'ColorLaserJet Pro MFP M283fdn', '',
    '', '', 4, 0, 0, 0, 1, NULL, NULL, NULL,
    '2026-04-24T14:39:59', '2026-04-24T14:40:01', 'REMOTE-IMPORT', 0
);

-- ---------- 29 ----------
INSERT OR IGNORE INTO catalogo_produtos (
    id, sku, tipo_produto, categoria, marca, modelo, cor, descricao, caracteristicas_tecnicas,
    total_recebido, preco_unitario_centavos, iva_basis_points, preco_total_centavos,
    ativo, fornecedor, numero_fatura, fatura_compra,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'IMP-HPX-HPD-001', 'EQUIPAMENTO', 'Departamento de Infraestruturas', 'HP', 'HP DESK JET 3762', '',
    '', '', 8, 0, 0, 0, 1, NULL, NULL, NULL,
    '2026-04-24T14:39:59', '2026-04-24T14:40:02', 'REMOTE-IMPORT', 0
);

-- ---------- 30 ----------
INSERT OR IGNORE INTO catalogo_produtos (
    id, sku, tipo_produto, categoria, marca, modelo, cor, descricao, caracteristicas_tecnicas,
    total_recebido, preco_unitario_centavos, iva_basis_points, preco_total_centavos,
    ativo, fornecedor, numero_fatura, fatura_compra,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'IMP-HPX-HPL-001', 'EQUIPAMENTO', 'Departamento de Plantação', 'Hp', 'HP LaserJetPro MFP M130nw', '',
    '', '', 4, 0, 0, 0, 1, NULL, NULL, NULL,
    '2026-04-24T14:40:00', '2026-04-24T14:40:01', 'REMOTE-IMPORT', 0
);

-- ---------- 31 ----------
INSERT OR IGNORE INTO catalogo_produtos (
    id, sku, tipo_produto, categoria, marca, modelo, cor, descricao, caracteristicas_tecnicas,
    total_recebido, preco_unitario_centavos, iva_basis_points, preco_total_centavos,
    ativo, fornecedor, numero_fatura, fatura_compra,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'IMP-HPX-LAS-001', 'EQUIPAMENTO', 'Departamento Industrial (POM)', 'HP', 'LaserJetPro M404dn', '',
    '', '', 4, 0, 0, 0, 1, NULL, NULL, NULL,
    '2026-04-24T14:39:59', '2026-04-24T14:40:02', 'REMOTE-IMPORT', 0
);

-- ---------- 32 ----------
INSERT OR IGNORE INTO catalogo_produtos (
    id, sku, tipo_produto, categoria, marca, modelo, cor, descricao, caracteristicas_tecnicas,
    total_recebido, preco_unitario_centavos, iva_basis_points, preco_total_centavos,
    ativo, fornecedor, numero_fatura, fatura_compra,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'MON-AOC-M24-006', 'EQUIPAMENTO', 'Monitor', 'Aoc', 'M2470SWH', 'Preto',
    '', '', 1, 0, 0, 0, 1, NULL, NULL, NULL,
    '2026-06-09T10:37:09', '2026-06-09T10:37:09', 'REMOTE-IMPORT', 0
);

-- ---------- 33 ----------
INSERT OR IGNORE INTO catalogo_produtos (
    id, sku, tipo_produto, categoria, marca, modelo, cor, descricao, caracteristicas_tecnicas,
    total_recebido, preco_unitario_centavos, iva_basis_points, preco_total_centavos,
    ativo, fornecedor, numero_fatura, fatura_compra,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'MON-DEL-20C-001', 'EQUIPAMENTO', 'Departamento de RH', 'Dell', '20C1KN2', '',
    '', '', 2, 0, 0, 0, 1, NULL, NULL, NULL,
    '2026-04-24T14:39:59', '2026-04-24T14:40:01', 'REMOTE-IMPORT', 0
);

-- ---------- 34 ----------
INSERT OR IGNORE INTO catalogo_produtos (
    id, sku, tipo_produto, categoria, marca, modelo, cor, descricao, caracteristicas_tecnicas,
    total_recebido, preco_unitario_centavos, iva_basis_points, preco_total_centavos,
    ativo, fornecedor, numero_fatura, fatura_compra,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'MON-DEL-4S8-001', 'EQUIPAMENTO', 'Departamento de RH', 'Dell', '4s81KN2', '',
    '', '', 1, 0, 0, 0, 1, NULL, NULL, NULL,
    '2026-04-24T14:40:02', '2026-04-24T15:52:28', 'REMOTE-IMPORT', 0
);

-- ---------- 35 ----------
INSERT OR IGNORE INTO catalogo_produtos (
    id, sku, tipo_produto, categoria, marca, modelo, cor, descricao, caracteristicas_tecnicas,
    total_recebido, preco_unitario_centavos, iva_basis_points, preco_total_centavos,
    ativo, fornecedor, numero_fatura, fatura_compra,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'MON-DEL-8QO-001', 'EQUIPAMENTO', 'Departamento Industrial (POM)', 'Dell', '8QONVQ2', '',
    '', '', 4, 0, 0, 0, 1, NULL, NULL, NULL,
    '2026-04-24T14:39:59', '2026-04-24T14:40:02', 'REMOTE-IMPORT', 0
);

-- ---------- 36 ----------
INSERT OR IGNORE INTO catalogo_produtos (
    id, sku, tipo_produto, categoria, marca, modelo, cor, descricao, caracteristicas_tecnicas,
    total_recebido, preco_unitario_centavos, iva_basis_points, preco_total_centavos,
    ativo, fornecedor, numero_fatura, fatura_compra,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'MON-DEL-9Q8-001', 'EQUIPAMENTO', 'Departamento de RH', 'Dell', '9Q81KN2', '',
    '', '', 1, 0, 0, 0, 1, NULL, NULL, NULL,
    '2026-04-24T14:40:01', '2026-04-24T14:40:01', 'REMOTE-IMPORT', 0
);

-- ---------- 37 ----------
INSERT OR IGNORE INTO catalogo_produtos (
    id, sku, tipo_produto, categoria, marca, modelo, cor, descricao, caracteristicas_tecnicas,
    total_recebido, preco_unitario_centavos, iva_basis_points, preco_total_centavos,
    ativo, fornecedor, numero_fatura, fatura_compra,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'MON-DEL-CN0-001', 'EQUIPAMENTO', 'Departamento de HSA', 'Dell', 'CN-01M2XX', '',
    '', '', 8, 0, 0, 0, 1, NULL, NULL, NULL,
    '2026-04-24T14:39:59', '2026-04-24T14:40:02', 'REMOTE-IMPORT', 0
);

-- ---------- 38 ----------
INSERT OR IGNORE INTO catalogo_produtos (
    id, sku, tipo_produto, categoria, marca, modelo, cor, descricao, caracteristicas_tecnicas,
    total_recebido, preco_unitario_centavos, iva_basis_points, preco_total_centavos,
    ativo, fornecedor, numero_fatura, fatura_compra,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'MON-DEL-E22-001', 'EQUIPAMENTO', 'Departamento de Plantação', 'dell', 'E2222H', '',
    '', '', 4, 0, 0, 0, 1, NULL, NULL, NULL,
    '2026-04-24T14:39:58', '2026-04-24T14:40:01', 'REMOTE-IMPORT', 0
);

-- ---------- 39 ----------
INSERT OR IGNORE INTO catalogo_produtos (
    id, sku, tipo_produto, categoria, marca, modelo, cor, descricao, caracteristicas_tecnicas,
    total_recebido, preco_unitario_centavos, iva_basis_points, preco_total_centavos,
    ativo, fornecedor, numero_fatura, fatura_compra,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'MON-DEL-E24-004', 'EQUIPAMENTO', 'Monitor', 'Dell', 'E2425HSM', 'Preta',
    '', '', 0, 0, 0, 0, 1, NULL, NULL, NULL,
    '2026-05-29T11:34:28', '2026-05-29T11:34:28', 'REMOTE-IMPORT', 0
);

-- ---------- 40 ----------
INSERT OR IGNORE INTO catalogo_produtos (
    id, sku, tipo_produto, categoria, marca, modelo, cor, descricao, caracteristicas_tecnicas,
    total_recebido, preco_unitario_centavos, iva_basis_points, preco_total_centavos,
    ativo, fornecedor, numero_fatura, fatura_compra,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'MON-DEL-E24-005', 'EQUIPAMENTO', 'Monitor', 'Dell', 'E2425hsm', 'Preto',
    '', '', 0, 0, 0, 0, 1, NULL, NULL, NULL,
    '2026-06-03T09:29:45', '2026-06-03T09:29:45', 'REMOTE-IMPORT', 0
);

-- ---------- 41 ----------
INSERT OR IGNORE INTO catalogo_produtos (
    id, sku, tipo_produto, categoria, marca, modelo, cor, descricao, caracteristicas_tecnicas,
    total_recebido, preco_unitario_centavos, iva_basis_points, preco_total_centavos,
    ativo, fornecedor, numero_fatura, fatura_compra,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'MON-DEL-GPO-001', 'EQUIPAMENTO', 'Departamento Industrial (POM)', 'Dell', 'GPONVQ2', '',
    '', '', 4, 0, 0, 0, 1, NULL, NULL, NULL,
    '2026-04-24T14:39:59', '2026-04-24T14:40:02', 'REMOTE-IMPORT', 0
);

-- ---------- 42 ----------
INSERT OR IGNORE INTO catalogo_produtos (
    id, sku, tipo_produto, categoria, marca, modelo, cor, descricao, caracteristicas_tecnicas,
    total_recebido, preco_unitario_centavos, iva_basis_points, preco_total_centavos,
    ativo, fornecedor, numero_fatura, fatura_compra,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'MON-DEL-GYB-001', 'EQUIPAMENTO', 'Departamento Financeiro', 'Dell', 'GYB1KN2', 'Preto',
    '', '', 15, 0, 0, 0, 1, NULL, NULL, NULL,
    '2026-04-24T14:39:58', '2026-06-03T10:24:43', 'REMOTE-IMPORT', 0
);

-- ---------- 43 ----------
INSERT OR IGNORE INTO catalogo_produtos (
    id, sku, tipo_produto, categoria, marca, modelo, cor, descricao, caracteristicas_tecnicas,
    total_recebido, preco_unitario_centavos, iva_basis_points, preco_total_centavos,
    ativo, fornecedor, numero_fatura, fatura_compra,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'MON-DEL-P24-001', 'EQUIPAMENTO', 'Departamento Financeiro', 'Dell', 'P2425h', '',
    '', '', 1, 0, 0, 0, 1, NULL, NULL, NULL,
    '2026-04-24T14:40:01', '2026-04-24T14:40:01', 'REMOTE-IMPORT', 0
);

-- ---------- 44 ----------
INSERT OR IGNORE INTO catalogo_produtos (
    id, sku, tipo_produto, categoria, marca, modelo, cor, descricao, caracteristicas_tecnicas,
    total_recebido, preco_unitario_centavos, iva_basis_points, preco_total_centavos,
    ativo, fornecedor, numero_fatura, fatura_compra,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'MON-DEL-U24-001', 'EQUIPAMENTO', 'Departamento Financeiro', 'Dell', 'U2412Mc', '',
    '', '', 4, 0, 0, 0, 1, NULL, NULL, NULL,
    '2026-04-24T14:39:58', '2026-04-24T14:40:01', 'REMOTE-IMPORT', 0
);

-- ---------- 45 ----------
INSERT OR IGNORE INTO catalogo_produtos (
    id, sku, tipo_produto, categoria, marca, modelo, cor, descricao, caracteristicas_tecnicas,
    total_recebido, preco_unitario_centavos, iva_basis_points, preco_total_centavos,
    ativo, fornecedor, numero_fatura, fatura_compra,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'MON-HPX-HPP-001', 'EQUIPAMENTO', 'Direção Geral', 'HP', 'HPP22G5FHD', '',
    '', '', 4, 0, 0, 0, 1, NULL, NULL, NULL,
    '2026-04-24T14:39:58', '2026-04-24T14:40:01', 'REMOTE-IMPORT', 0
);

-- ---------- 46 ----------
INSERT OR IGNORE INTO catalogo_produtos (
    id, sku, tipo_produto, categoria, marca, modelo, cor, descricao, caracteristicas_tecnicas,
    total_recebido, preco_unitario_centavos, iva_basis_points, preco_total_centavos,
    ativo, fornecedor, numero_fatura, fatura_compra,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'MON-LGX-19M-001', 'EQUIPAMENTO', 'Departamento Industrial (POM)', 'LG', '19M35A', '',
    '', '', 2, 0, 0, 0, 1, NULL, NULL, NULL,
    '2026-04-24T14:40:00', '2026-04-24T14:40:01', 'REMOTE-IMPORT', 0
);

-- ---------- 47 ----------
INSERT OR IGNORE INTO catalogo_produtos (
    id, sku, tipo_produto, categoria, marca, modelo, cor, descricao, caracteristicas_tecnicas,
    total_recebido, preco_unitario_centavos, iva_basis_points, preco_total_centavos,
    ativo, fornecedor, numero_fatura, fatura_compra,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'MON-LGX-24M-001', 'EQUIPAMENTO', 'Departamento Industrial (POM)', 'LG', '24M38H', '',
    '', '', 4, 0, 0, 0, 1, NULL, NULL, NULL,
    '2026-04-24T14:39:59', '2026-04-24T14:40:02', 'REMOTE-IMPORT', 0
);

-- ---------- 48 ----------
INSERT OR IGNORE INTO catalogo_produtos (
    id, sku, tipo_produto, categoria, marca, modelo, cor, descricao, caracteristicas_tecnicas,
    total_recebido, preco_unitario_centavos, iva_basis_points, preco_total_centavos,
    ativo, fornecedor, numero_fatura, fatura_compra,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'MON-PHI-223-001', 'EQUIPAMENTO', 'Departamento Industrial (POM)', 'PHILIPS', '223V5L', '',
    '', '', 9, 0, 0, 0, 1, NULL, NULL, NULL,
    '2026-04-24T14:39:59', '2026-04-24T14:40:02', 'REMOTE-IMPORT', 0
);

-- ---------- 49 ----------
INSERT OR IGNORE INTO catalogo_produtos (
    id, sku, tipo_produto, categoria, marca, modelo, cor, descricao, caracteristicas_tecnicas,
    total_recebido, preco_unitario_centavos, iva_basis_points, preco_total_centavos,
    ativo, fornecedor, numero_fatura, fatura_compra,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'MON-PHI-223-002', 'EQUIPAMENTO', 'Departamento Finenceiro', 'Philips', '223V', '',
    '', '', 4, 0, 0, 0, 1, NULL, NULL, NULL,
    '2026-04-24T14:39:59', '2026-04-24T14:40:02', 'REMOTE-IMPORT', 0
);

-- ---------- 50 ----------
INSERT OR IGNORE INTO catalogo_produtos (
    id, sku, tipo_produto, categoria, marca, modelo, cor, descricao, caracteristicas_tecnicas,
    total_recebido, preco_unitario_centavos, iva_basis_points, preco_total_centavos,
    ativo, fornecedor, numero_fatura, fatura_compra,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'MON-SAM-S19-001', 'EQUIPAMENTO', 'Departamento Industrial (POM)', 'Samsung', 'S19C150F', '',
    '', '', 4, 0, 0, 0, 1, NULL, NULL, NULL,
    '2026-04-24T14:39:59', '2026-04-24T14:40:02', 'REMOTE-IMPORT', 0
);

-- ---------- 51 ----------
INSERT OR IGNORE INTO catalogo_produtos (
    id, sku, tipo_produto, categoria, marca, modelo, cor, descricao, caracteristicas_tecnicas,
    total_recebido, preco_unitario_centavos, iva_basis_points, preco_total_centavos,
    ativo, fornecedor, numero_fatura, fatura_compra,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'MON-SAM-S27-001', 'EQUIPAMENTO', 'Direção Geral', 'Samsung', 'S27F350FHU', '',
    '', '', 4, 0, 0, 0, 1, NULL, NULL, NULL,
    '2026-04-24T14:39:58', '2026-04-24T14:40:01', 'REMOTE-IMPORT', 0
);

-- ---------- 52 ----------
INSERT OR IGNORE INTO catalogo_produtos (
    id, sku, tipo_produto, categoria, marca, modelo, cor, descricao, caracteristicas_tecnicas,
    total_recebido, preco_unitario_centavos, iva_basis_points, preco_total_centavos,
    ativo, fornecedor, numero_fatura, fatura_compra,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'MON-VIE-VS1-001', 'EQUIPAMENTO', 'Departamento de IT', 'ViewSonic', 'Vs18811', '',
    '', '', 3, 0, 0, 0, 1, NULL, NULL, NULL,
    '2026-04-24T14:39:59', '2026-04-24T14:40:02', 'REMOTE-IMPORT', 0
);

-- ---------- 53 ----------
INSERT OR IGNORE INTO catalogo_produtos (
    id, sku, tipo_produto, categoria, marca, modelo, cor, descricao, caracteristicas_tecnicas,
    total_recebido, preco_unitario_centavos, iva_basis_points, preco_total_centavos,
    ativo, fornecedor, numero_fatura, fatura_compra,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'MSE-COM-WIR-001', 'ACESSORIO', 'Departamento Financeiro', 'Combo', 'Wired Keyboard (hw:Rm)', '',
    '', '', 2, 0, 0, 0, 1, NULL, NULL, NULL,
    '2026-04-24T14:39:58', '2026-04-24T14:40:01', 'REMOTE-IMPORT', 0
);

-- ---------- 54 ----------
INSERT OR IGNORE INTO catalogo_produtos (
    id, sku, tipo_produto, categoria, marca, modelo, cor, descricao, caracteristicas_tecnicas,
    total_recebido, preco_unitario_centavos, iva_basis_points, preco_total_centavos,
    ativo, fornecedor, numero_fatura, fatura_compra,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'MSE-DEL-MS1-001', 'ACESSORIO', 'Departamento Financeiro', 'Dell', 'Ms116t', '',
    '', '', 2, 0, 0, 0, 1, NULL, NULL, NULL,
    '2026-04-24T14:39:58', '2026-04-24T14:39:58', 'REMOTE-IMPORT', 0
);

-- ---------- 55 ----------
INSERT OR IGNORE INTO catalogo_produtos (
    id, sku, tipo_produto, categoria, marca, modelo, cor, descricao, caracteristicas_tecnicas,
    total_recebido, preco_unitario_centavos, iva_basis_points, preco_total_centavos,
    ativo, fornecedor, numero_fatura, fatura_compra,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'MSE-EQU-NO2-001', 'ACESSORIO', 'Departamento Financeiro', 'equip', 'No.:245107', '',
    '', '', 2, 0, 0, 0, 1, NULL, NULL, NULL,
    '2026-04-24T14:39:58', '2026-04-24T14:39:58', 'REMOTE-IMPORT', 0
);

-- ---------- 56 ----------
INSERT OR IGNORE INTO catalogo_produtos (
    id, sku, tipo_produto, categoria, marca, modelo, cor, descricao, caracteristicas_tecnicas,
    total_recebido, preco_unitario_centavos, iva_basis_points, preco_total_centavos,
    ativo, fornecedor, numero_fatura, fatura_compra,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'MSE-HPX-7CH-001', 'ACESSORIO', 'Departamento de RH', 'HP', '7CH9444HK6', '',
    '', '', 1, 0, 0, 0, 1, NULL, NULL, NULL,
    '2026-04-24T14:40:01', '2026-04-24T14:40:01', 'REMOTE-IMPORT', 0
);

-- ---------- 57 ----------
INSERT OR IGNORE INTO catalogo_produtos (
    id, sku, tipo_produto, categoria, marca, modelo, cor, descricao, caracteristicas_tecnicas,
    total_recebido, preco_unitario_centavos, iva_basis_points, preco_total_centavos,
    ativo, fornecedor, numero_fatura, fatura_compra,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'MSE-LOG-MXA-001', 'EQUIPAMENTO', 'Departamento de Armazém', 'Logitech', 'MX Anywhere 3S', 'Preto',
    '', '', 20, 0, 0, 0, 1, NULL, NULL, NULL,
    '2026-04-24T14:39:58', '2026-06-03T10:55:41', 'REMOTE-IMPORT', 0
);

-- ---------- 58 ----------
INSERT OR IGNORE INTO catalogo_produtos (
    id, sku, tipo_produto, categoria, marca, modelo, cor, descricao, caracteristicas_tecnicas,
    total_recebido, preco_unitario_centavos, iva_basis_points, preco_total_centavos,
    ativo, fornecedor, numero_fatura, fatura_compra,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'NET-TPL-TLW-001', 'EQUIPAMENTO', 'Departamento Industrial (POM)', 'TP-Link', 'TL-WR940N', '',
    '', '', 4, 0, 0, 0, 1, NULL, NULL, NULL,
    '2026-04-24T14:40:00', '2026-04-24T14:40:01', 'REMOTE-IMPORT', 0
);

-- ---------- 59 ----------
INSERT OR IGNORE INTO catalogo_produtos (
    id, sku, tipo_produto, categoria, marca, modelo, cor, descricao, caracteristicas_tecnicas,
    total_recebido, preco_unitario_centavos, iva_basis_points, preco_total_centavos,
    ativo, fornecedor, numero_fatura, fatura_compra,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'NET-UBQ-DRE-011', 'EQUIPAMENTO', 'Rede', 'Ubquite', 'Dream Machine Especial edition', 'cinsa',
    '', '', 1, 24900, 0, 24900, 1, NULL, NULL, NULL,
    '2026-04-25T10:58:48', '2026-04-25T10:58:48', 'REMOTE-IMPORT', 0
);

-- ---------- 60 ----------
INSERT OR IGNORE INTO catalogo_produtos (
    id, sku, tipo_produto, categoria, marca, modelo, cor, descricao, caracteristicas_tecnicas,
    total_recebido, preco_unitario_centavos, iva_basis_points, preco_total_centavos,
    ativo, fornecedor, numero_fatura, fatura_compra,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'OUT-ASU-ASP-001', 'EQUIPAMENTO', 'Laptop', 'Asus', 'Aspiron', 'proto',
    '', '', 1, 0, 0, 0, 1, 'Rafael', '45', NULL,
    '2026-06-20T11:07:42', '2026-06-22T15:53:45', 'REMOTE-IMPORT', 0
);

-- ---------- 61 ----------
INSERT OR IGNORE INTO catalogo_produtos (
    id, sku, tipo_produto, categoria, marca, modelo, cor, descricao, caracteristicas_tecnicas,
    total_recebido, preco_unitario_centavos, iva_basis_points, preco_total_centavos,
    ativo, fornecedor, numero_fatura, fatura_compra,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'OUT-HPX-15D-001', 'EQUIPAMENTO', 'Departamento Comercial', 'HP', '15-dy2795wm', '',
    '', '', 13, 0, 1500, 0, 1, 'Pcdig', '14', NULL,
    '2026-08-01T10:11:17', '2026-08-01T10:11:17', 'REMOTE-IMPORT', 0
);

-- ---------- 62 ----------
INSERT OR IGNORE INTO catalogo_produtos (
    id, sku, tipo_produto, categoria, marca, modelo, cor, descricao, caracteristicas_tecnicas,
    total_recebido, preco_unitario_centavos, iva_basis_points, preco_total_centavos,
    ativo, fornecedor, numero_fatura, fatura_compra,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'OUT-HPX-4T6-012', 'EQUIPAMENTO', 'Impressora', 'Hp', '4t6p', '',
    '', '', 10, 0, 0, 0, 1, NULL, NULL, NULL,
    '2026-06-29T15:51:05', '2026-06-29T15:52:19', 'REMOTE-IMPORT', 0
);

-- ---------- 63 ----------
INSERT OR IGNORE INTO catalogo_produtos (
    id, sku, tipo_produto, categoria, marca, modelo, cor, descricao, caracteristicas_tecnicas,
    total_recebido, preco_unitario_centavos, iva_basis_points, preco_total_centavos,
    ativo, fornecedor, numero_fatura, fatura_compra,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'OUT-TOP-T23-001', 'EQUIPAMENTO', 'Departamento Industrial (POM)', 'Topvision', 'T23', '',
    '', '', 2, 0, 0, 0, 1, NULL, NULL, NULL,
    '2026-04-24T14:39:59', '2026-04-24T14:40:01', 'REMOTE-IMPORT', 0
);

-- ---------- 64 ----------
INSERT OR IGNORE INTO catalogo_produtos (
    id, sku, tipo_produto, categoria, marca, modelo, cor, descricao, caracteristicas_tecnicas,
    total_recebido, preco_unitario_centavos, iva_basis_points, preco_total_centavos,
    ativo, fornecedor, numero_fatura, fatura_compra,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'TCD-DEL-ONN-001', 'ACESSORIO', 'Departamento de RH', 'Dell', 'ONNC2P', '',
    '', '', 1, 0, 0, 0, 1, NULL, NULL, NULL,
    '2026-04-24T14:40:01', '2026-04-24T14:40:01', 'REMOTE-IMPORT', 0
);

-- ---------- 65 ----------
INSERT OR IGNORE INTO catalogo_produtos (
    id, sku, tipo_produto, categoria, marca, modelo, cor, descricao, caracteristicas_tecnicas,
    total_recebido, preco_unitario_centavos, iva_basis_points, preco_total_centavos,
    ativo, fornecedor, numero_fatura, fatura_compra,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'TCD-EQU-NO2-001', 'ACESSORIO', 'Departamento Financeiro', 'equip', 'No.: 245202', '',
    '', '', 2, 0, 0, 0, 1, NULL, NULL, NULL,
    '2026-04-24T14:39:58', '2026-04-24T14:40:01', 'REMOTE-IMPORT', 0
);

-- ---------- 66 ----------
INSERT OR IGNORE INTO catalogo_produtos (
    id, sku, tipo_produto, categoria, marca, modelo, cor, descricao, caracteristicas_tecnicas,
    total_recebido, preco_unitario_centavos, iva_basis_points, preco_total_centavos,
    ativo, fornecedor, numero_fatura, fatura_compra,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'TCD-LOG-MXK-001', 'ACESSORIO', 'Departamento Financeiro', 'Logitech', 'MX Keys', '',
    '', '', 10, 0, 0, 0, 1, NULL, NULL, NULL,
    '2026-04-24T14:39:58', '2026-04-24T14:40:02', 'REMOTE-IMPORT', 0
);

-- ---------- 67 ----------
INSERT OR IGNORE INTO catalogo_produtos (
    id, sku, tipo_produto, categoria, marca, modelo, cor, descricao, caracteristicas_tecnicas,
    total_recebido, preco_unitario_centavos, iva_basis_points, preco_total_centavos,
    ativo, fornecedor, numero_fatura, fatura_compra,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'TCD-LOG-MXK-002', 'ACESSORIO', 'Departamento Financeiro', 'Logitech', 'MX Keys S', '',
    '', '', 8, 0, 0, 0, 1, NULL, NULL, NULL,
    '2026-04-24T14:39:58', '2026-04-24T14:40:02', 'REMOTE-IMPORT', 0
);

-- ---------- 68 ----------
INSERT OR IGNORE INTO catalogo_produtos (
    id, sku, tipo_produto, categoria, marca, modelo, cor, descricao, caracteristicas_tecnicas,
    total_recebido, preco_unitario_centavos, iva_basis_points, preco_total_centavos,
    ativo, fornecedor, numero_fatura, fatura_compra,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'TEL-SAM-GAL-001', 'EQUIPAMENTO', 'Departamento Finenceiro', 'Samsung', 'Galaxy A13', '',
    '', '', 4, 0, 0, 0, 1, NULL, NULL, NULL,
    '2026-04-24T14:39:59', '2026-04-24T14:40:02', 'REMOTE-IMPORT', 0
);

-- ---------- 69 ----------
INSERT OR IGNORE INTO catalogo_produtos (
    id, sku, tipo_produto, categoria, marca, modelo, cor, descricao, caracteristicas_tecnicas,
    total_recebido, preco_unitario_centavos, iva_basis_points, preco_total_centavos,
    ativo, fornecedor, numero_fatura, fatura_compra,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'TEL-SAN-GAL-001', 'EQUIPAMENTO', 'Telemovel', 'Sansung', 'Galaxy A05s', 'Preto',
    '', '', 10, 8900, 0, 8900, 1, NULL, NULL, NULL,
    '2026-04-24T14:39:58', '2026-04-27T09:14:57', 'REMOTE-IMPORT', 0
);

-- ---------- 70 ----------
INSERT OR IGNORE INTO catalogo_produtos (
    id, sku, tipo_produto, categoria, marca, modelo, cor, descricao, caracteristicas_tecnicas,
    total_recebido, preco_unitario_centavos, iva_basis_points, preco_total_centavos,
    ativo, fornecedor, numero_fatura, fatura_compra,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'TON-HPX-COL-001', 'TONER', 'Departamento Financeiro', 'Hp', 'colorlazerjet pro mfp m 283fdn', '',
    '', '', 16, 0, 0, 0, 1, NULL, NULL, NULL,
    '2026-04-24T14:39:59', '2026-04-24T14:40:01', 'REMOTE-IMPORT', 0
);

-- ---------- 71 ----------
INSERT OR IGNORE INTO catalogo_produtos (
    id, sku, tipo_produto, categoria, marca, modelo, cor, descricao, caracteristicas_tecnicas,
    total_recebido, preco_unitario_centavos, iva_basis_points, preco_total_centavos,
    ativo, fornecedor, numero_fatura, fatura_compra,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'TON-HPX-COL-002', 'TONER', 'Direção Geral', 'Hp', 'colorlazerjet pro mfp m 282nw', '',
    '', '', 8, 0, 0, 0, 1, NULL, NULL, NULL,
    '2026-04-24T14:40:00', '2026-04-24T14:40:01', 'REMOTE-IMPORT', 0
);

-- ---------- 72 ----------
INSERT OR IGNORE INTO catalogo_produtos (
    id, sku, tipo_produto, categoria, marca, modelo, cor, descricao, caracteristicas_tecnicas,
    total_recebido, preco_unitario_centavos, iva_basis_points, preco_total_centavos,
    ativo, fornecedor, numero_fatura, fatura_compra,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'TON-HPX-LAS-001', 'TONER', 'Departamento Industrial (POM)', 'HP', 'Laserjetpro M404 dn', '',
    '', '', 2, 0, 0, 0, 1, NULL, NULL, NULL,
    '2026-04-24T14:39:59', '2026-04-24T14:40:01', 'REMOTE-IMPORT', 0
);

-- ---------- 73 ----------
INSERT OR IGNORE INTO catalogo_produtos (
    id, sku, tipo_produto, categoria, marca, modelo, cor, descricao, caracteristicas_tecnicas,
    total_recebido, preco_unitario_centavos, iva_basis_points, preco_total_centavos,
    ativo, fornecedor, numero_fatura, fatura_compra,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'UPS-PHA-P1B-001', 'EQUIPAMENTO', 'Departamento Industrial (POM)', 'PHASAK', 'P1B', '',
    '', '', 6, 0, 0, 0, 1, NULL, NULL, NULL,
    '2026-04-24T14:39:59', '2026-04-24T14:40:02', 'REMOTE-IMPORT', 0
);

-- ---------- 74 ----------
INSERT OR IGNORE INTO catalogo_produtos (
    id, sku, tipo_produto, categoria, marca, modelo, cor, descricao, caracteristicas_tecnicas,
    total_recebido, preco_unitario_centavos, iva_basis_points, preco_total_centavos,
    ativo, fornecedor, numero_fatura, fatura_compra,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'UPS-PHA-P8N-001', 'EQUIPAMENTO', 'Departamento Industrial (POM)', 'PHASAK', 'P8N', '',
    '', '', 2, 0, 0, 0, 1, NULL, NULL, NULL,
    '2026-04-24T14:39:59', '2026-04-24T14:40:01', 'REMOTE-IMPORT', 0
);



-- ============================================================
-- Verificação pós-INSERT
-- ============================================================
-- SELECT COUNT(*) FROM catalogo_produtos;                                    -- deve ser 74
-- SELECT sku, preco_unitario_centavos, iva_basis_points FROM catalogo_produtos
--   WHERE preco_unitario_centavos > 0 ORDER BY sku;                         -- deve listar os 6 produtos com preço
-- SELECT * FROM catalogo_produtos WHERE tipo_produto = 'ACESSORIO';          -- devem ser os ~7 mapeados