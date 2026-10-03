-- ============================================================
-- Bootstrap INVENTARIO_EQUIPAMENTOS — MySQL remoto → SQLite local
-- Execução: UMA VEZ, APÓS insert_catalogo.sql e insert_funcionarios.sql.
--
-- v2 — Alinhado ao InventarioEquipamentos v3.1:
--      * produto_id REMOVIDO (nunca existiu no remoto)
--      * sku_produto é a PRÓPRIA chave de negócio (referencia catalogo_produtos.sku)
--      * funcionario_id é o PRÓPRIO cod_dep (referencia funcionarios.cod_dep)
--      * Sem subqueries: os valores da chave natural vão diretos
-- ============================================================



-- ---------- 1 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'CMP-HPX-RTL-001', 'Amz_02', 'CND2472S79', 'YU',
    '2023-07-22', NULL, '2024-11-21', NULL,
    'Armazém Geral', 'Departamento de Armazém', 'MANUTENCAO', 'CRITICO',
    NULL, NULL, '', NULL,
    '2026-04-24T14:39:58', '2026-06-09T10:42:05', 'REMOTE-IMPORT', 0
);

-- ---------- 2 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'IMP-HPX-COL-001', 'Amz_02', 'VNC4131775', NULL,
    '2023-07-22', NULL, '2024-11-21', NULL,
    'Armazém Geral', 'Departamento de Armazém', 'ATIVO', 'BOM',
    NULL, NULL, '', NULL,
    '2026-04-24T14:39:58', '2026-04-24T14:39:58', 'REMOTE-IMPORT', 0
);

-- ---------- 3 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'MSE-LOG-MXA-001', 'Amz_02', 'Agri-Dcl-Log-00-01-A', NULL,
    '2026-04-24', NULL, '2026-07-06', NULL,
    'Armazem Geral', 'Departamento Compras e Logística', 'ATIVO', 'OTIMO',
    NULL, NULL, '', 0,
    '2026-04-24T14:39:58', '2026-07-06T14:20:59', 'REMOTE-IMPORT', 0
);

-- ---------- 4 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'CMP-DEL-VES-001', 'Amz_04', 'Agri-Dcl-Dell-00-02-A', NULL,
    '2026-04-24', NULL, '2026-07-06', NULL,
    'Armazém Geral', 'Departamento de Armazém', 'ATIVO', 'CRITICO',
    NULL, NULL, 'A tampa do computador esta partida, e no teclado', 0,
    '2026-04-24T14:39:58', '2026-07-06T14:23:57', 'REMOTE-IMPORT', 0
);

-- ---------- 5 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'CMP-DEL-VOS-001', 'Daf_01', 'G76FFL2', 'yy',
    '2026-04-24', '2026-06-03', '2026-06-03', NULL,
    'Sala da Direção Financeira', 'Departamento Financeiro', 'EM_USO', 'BOM',
    NULL, NULL, '', NULL,
    '2026-04-24T14:39:58', '2026-06-03T12:31:46', 'REMOTE-IMPORT', 0
);

-- ---------- 6 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'MON-DEL-U24-001', 'Daf_07', 'CN-0YPPY0-tv100-8b7-15xv-a10', 'h',
    '2026-04-24', '2026-06-03', '2026-06-03', NULL,
    'Departamento Financeiro e Contabilidade', 'Departamento Financeiro', 'EM_USO', 'BOM',
    NULL, NULL, '', NULL,
    '2026-04-24T14:39:58', '2026-06-03T12:34:33', 'REMOTE-IMPORT', 0
);

-- ---------- 7 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'TCD-LOG-MXK-001', 'Daf_01', '2019D17096daf-01', 'TT',
    '2026-04-24', '2026-06-03', '2025-01-22', NULL,
    'Sala da Direção Financeira', 'Departamento Financeiro', 'EM_USO', 'BOM',
    NULL, NULL, '', NULL,
    '2026-04-24T14:39:58', '2026-06-03T12:46:01', 'REMOTE-IMPORT', 0
);

-- ---------- 8 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'CMP-THI-LEN-001', 'Daf_02', 'CIDF15000051', NULL,
    '2023-08-14', NULL, '2024-11-21', NULL,
    'Departamento Financeiro e Contabilidade', 'Departamento Financeiro', 'ATIVO', 'BOM',
    NULL, NULL, '', NULL,
    '2026-04-24T14:39:58', '2026-04-24T14:39:58', 'REMOTE-IMPORT', 0
);

-- ---------- 9 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'MSE-DEL-MS1-001', 'Daf_02', 'cn-0jd7xg-l0300-85h-0ne4', NULL,
    '2024-06-19', NULL, '2024-11-21', NULL,
    'Departamento Financeiro e Contabilidade', 'Departamento Financeiro', 'ATIVO', 'BOM',
    NULL, NULL, '', NULL,
    '2026-04-24T14:39:58', '2026-04-24T14:39:58', 'REMOTE-IMPORT', 0
);

-- ---------- 10 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'CPU-DEL-OPT-001', 'Daf_02', 'Agri-Daf-Dell-00-05-A', NULL,
    '2024-06-19', NULL, '2026-07-06', NULL,
    'Departamento Financeiro e Contabilidade', 'Departamento Financeiro', 'ATIVO', 'BOM',
    NULL, NULL, '', 0,
    '2026-04-24T14:39:58', '2026-07-06T14:39:22', 'REMOTE-IMPORT', 0
);

-- ---------- 11 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'MON-DEL-E24-004', 'Daf_02', 'Agri-Daf-Dell-00-00-A', NULL,
    '2026-05-29', '2026-06-03', '2026-06-03', NULL,
    'Departamento Financeiro e Contabilidade', 'Departamento Financeiro', 'ATIVO', 'OTIMO',
    NULL, NULL, '', 0,
    '2026-04-24T14:39:58', '2026-07-03T13:01:05', 'REMOTE-IMPORT', 0
);

-- ---------- 12 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'MSE-EQU-NO2-001', 'Daf_02', 'Agri-Daf-No2-00-00-A', NULL,
    '2024-06-19', NULL, NULL, NULL,
    'Departamento Financeiro e Contabilidade', 'Departamento Financeiro', 'ATIVO', 'CRITICO',
    NULL, NULL, 'Rato sencivel', NULL,
    '2026-04-24T14:39:58', '2026-04-24T14:39:58', 'REMOTE-IMPORT', 0
);

-- ---------- 13 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'TCD-LOG-MXK-001', 'Daf_02', 'Agri-Daf-Log-00-04-A', NULL,
    '2026-04-24', NULL, '2026-07-06', NULL,
    'Departamento Financeiro e Contabilidade', 'Departamento Financeiro', 'ATIVO', 'OTIMO',
    NULL, NULL, '', 0,
    '2026-04-24T14:39:58', '2026-07-06T14:22:37', 'REMOTE-IMPORT', 0
);

-- ---------- 14 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'MSE-LOG-MXA-001', 'Daf_02', 'Agri-Daf-Log-00-01-A', NULL,
    '2026-04-24', NULL, '2026-07-03', NULL,
    'Departamento Financeiro e Contabilidade', 'Departamento Financeiro', 'ATIVO', 'OTIMO',
    NULL, NULL, '', 0,
    '2026-04-24T14:39:58', '2026-07-03T16:03:08', 'REMOTE-IMPORT', 0
);

-- ---------- 15 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'MSE-LOG-MXA-001', 'Daf_02', 'Agri-Daf-Log-00-00-A', NULL,
    '2025-01-22', NULL, '2025-01-22', NULL,
    'Departamento Financeiro e Contabilidade', 'Departamento Financeiro', 'ATIVO', 'OTIMO',
    NULL, NULL, '', 0,
    '2026-04-24T14:39:58', '2026-07-03T13:02:00', 'REMOTE-IMPORT', 0
);

-- ---------- 16 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'CMP-HPX-HP2-001', 'Daf_02', 'cnd42027bh', NULL,
    '2025-08-12', NULL, '2025-08-12', NULL,
    'Departamento Financeiro e Contabilidade', 'Departamento Financeiro', 'ATIVO', 'BOM',
    NULL, NULL, '', NULL,
    '2026-04-24T14:39:58', '2026-04-24T14:39:58', 'REMOTE-IMPORT', 0
);

-- ---------- 17 (num_serie duplicado — renomeado -DUP) ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'MSE-EQU-NO2-001', 'Daf_02', 'Agri-Daf-No2-00-00-A-DUP', NULL,
    '2024-06-19', NULL, NULL, NULL,
    'Departamento Financeiro e Contabilidade', 'Departamento Financeiro', 'ATIVO', 'CRITICO',
    NULL, NULL, 'Rato sencivel', NULL,
    '2026-04-24T14:39:58', '2026-04-24T14:39:58', 'REMOTE-IMPORT', 0
);

-- ---------- 18 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'CPU-DEL-D18-001', 'Daf_04', 'J72GGA01WCD', NULL,
    NULL, NULL, '2024-11-21', NULL,
    'Departamento Financeiro e Contabilidade', 'Departamento Financeiro', 'ATIVO', 'BOM',
    NULL, NULL, '', NULL,
    '2026-04-24T14:39:58', '2026-04-24T14:39:58', 'REMOTE-IMPORT', 0
);

-- ---------- 19 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'MSE-COM-WIR-001', 'Daf_04', 'Agri-Daf-Com-00-00-A', NULL,
    '2024-06-19', NULL, '2024-11-21', NULL,
    'Departamento Financeiro e Contabilidade', 'Departamento Financeiro', 'ATIVO', 'BOM',
    NULL, NULL, '', NULL,
    '2026-04-24T14:39:58', '2026-04-24T14:39:58', 'REMOTE-IMPORT', 0
);

-- ---------- 20 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'TCD-EQU-NO2-001', 'Daf_04', 'Agri-Daf-Dell-00-08-A', NULL,
    '2026-04-24', NULL, '2026-07-08', NULL,
    'Departamento Financeiro e Contabilidade', 'Departamento Financeiro', 'ATIVO', 'BOM',
    NULL, NULL, '', 0,
    '2026-04-24T14:39:58', '2026-07-08T15:05:23', 'REMOTE-IMPORT', 0
);

-- ---------- 21 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'TCD-LOG-MXK-001', 'Daf_04', 'Agri-Daf-Log-00-02-A', NULL,
    '2026-04-24', NULL, '2026-07-06', NULL,
    'Departamento Financeiro e Contabilidade', 'Departamento Financeiro', 'ATIVO', 'OTIMO',
    NULL, NULL, '', 0,
    '2026-04-24T14:39:58', '2026-07-06T14:01:17', 'REMOTE-IMPORT', 0
);

-- ---------- 22 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'TEL-SAN-GAL-001', 'Daf_06', 'SM-A057G/DSN', NULL,
    '2024-11-13', NULL, '2024-11-21', NULL,
    'Departamento Financeiro e Contabilidade', 'Departamento Financeiro', 'ATIVO', 'OTIMO',
    NULL, NULL, '', NULL,
    '2026-04-24T14:39:58', '2026-04-24T14:39:58', 'REMOTE-IMPORT', 0
);

-- ---------- 23 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'MSE-LOG-MXA-001', 'Daf_06', 'Agri-Daf-Log-00-03-A', NULL,
    '2026-04-24', NULL, '2026-07-06', NULL,
    'Departamento Financeiro e Contabilidade', 'Departamento Financeiro', 'ATIVO', 'OTIMO',
    NULL, NULL, '', 0,
    '2026-04-24T14:39:58', '2026-07-06T14:17:14', 'REMOTE-IMPORT', 0
);

-- ---------- 24 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'CMP-DEL-ESS-017', 'Amz_02', 'Agri-Dcl-Dell-00-00-A', '44:F7:9F:D0:73:6D',
    '2026-05-09', '2026-05-09', '2026-07-03', NULL,
    'Armazem Geral', 'Departamento Compras e Logística', 'EM_USO', 'OTIMO',
    NULL, NULL, '', 0,
    '2026-04-24T14:39:58', '2026-07-03T13:52:18', 'REMOTE-IMPORT', 0
);

-- ---------- 25 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'CMP-HPX-15D-001', 'Dcl_01', '5CD3322SQ5', '88',
    '2024-12-30', NULL, '2025-03-12', NULL,
    'Dloretor de Compras', 'Departamento Comercial', 'BAIXADO', 'BOM',
    NULL, NULL, 'Foi roubado na empresa.', NULL,
    '2026-04-24T14:39:58', '2026-06-09T10:45:04', 'REMOTE-IMPORT', 0
);

-- ---------- 26 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'CMP-HPX-15D-001', 'Pom_05', '5CD3321WXP', 'uu',
    '2024-12-17', NULL, '2025-01-09', NULL,
    'Escritório de Manutenção (POM)', 'Departamento Industrial (POM)', 'EM_USO', 'BOM',
    NULL, NULL, '', NULL,
    '2026-04-24T14:39:58', '2026-06-09T10:46:37', 'REMOTE-IMPORT', 0
);

-- ---------- 27 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'MSE-LOG-MXA-001', 'Dcl_03', '2417LZ02PRE8', 'uy',
    '2025-01-22', NULL, '2025-01-22', NULL,
    'Sala de contabilidade Escritorio plantação', 'Departamento Compras e Logística', 'EM_USO', 'OTIMO',
    NULL, NULL, '', NULL,
    '2026-04-24T14:39:58', '2026-06-09T10:47:16', 'REMOTE-IMPORT', 0
);

-- ---------- 28 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'MON-HPX-HPP-001', 'Dge_01', '3CM22608MW', NULL,
    NULL, NULL, NULL, NULL,
    'Sala da Direção Geral', 'Direção Geral', 'ATIVO', 'BOM',
    NULL, NULL, '', NULL,
    '2026-04-24T14:39:58', '2026-04-24T14:39:58', 'REMOTE-IMPORT', 0
);

-- ---------- 29 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'CMP-HPX-INT-001', 'Dge_01', 'Agri-Dg-Hp-00-00-A', NULL,
    '2026-04-24', NULL, '2026-07-06', NULL,
    'Sala da Direção Geral', 'Direção Geral', 'ATIVO', 'BOM',
    NULL, NULL, '', 0,
    '2026-04-24T14:39:58', '2026-07-06T14:45:31', 'REMOTE-IMPORT', 0
);

-- ---------- 30 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'MON-SAM-S27-001', 'Dge_01', 'ZZN9H4ZM903417F', NULL,
    NULL, NULL, NULL, NULL,
    'Sala da Direção Geral', 'Direção Geral', 'ATIVO', 'BOM',
    NULL, NULL, '', NULL,
    '2026-04-24T14:39:58', '2026-04-24T14:39:58', 'REMOTE-IMPORT', 0
);

-- ---------- 31 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'CMP-HPX-TPN-001', 'Djr_01', '5CG4120JN6', NULL,
    '2024-08-16', NULL, '2024-08-18', NULL,
    'Sala do Responsável Jurídico', 'Departamento Jurídico', 'ATIVO', 'OTIMO',
    NULL, NULL, '', NULL,
    '2026-04-24T14:39:58', '2026-04-24T14:39:58', 'REMOTE-IMPORT', 0
);

-- ---------- 32 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'MSE-LOG-MXA-001', 'Djr_01', 'Agri-Djr-Log-00-00-A', NULL,
    '2026-04-24', NULL, '2026-07-03', NULL,
    'Sala do Responsável Jurídico', 'Departamento Jurídico', 'EM_USO', 'OTIMO',
    NULL, NULL, '', 0,
    '2026-04-24T14:39:58', '2026-07-03T13:37:13', 'REMOTE-IMPORT', 0
);

-- ---------- 33 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'CMP-THI-LEN-001', 'Dpl_01', 'Agri-Dpl-Leno-00-00-A', NULL,
    '2026-04-24', NULL, '2026-07-06', NULL,
    'Sala da Direção de Plantação', 'Departamento de Plantação', 'ATIVO', 'BOM',
    NULL, NULL, '', 0,
    '2026-04-24T14:39:58', '2026-07-06T14:18:57', 'REMOTE-IMPORT', 0
);

-- ---------- 34 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'MON-DEL-E22-001', 'Dpl_01', 'cn-0j25p8', NULL,
    NULL, NULL, NULL, NULL,
    'Sala da Direção de Plantação', 'Departamento de Plantação', 'ATIVO', 'BOM',
    NULL, NULL, '', NULL,
    '2026-04-24T14:39:58', '2026-04-24T14:39:58', 'REMOTE-IMPORT', 0
);

-- ---------- 35 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'IMP-CAN-PIX-001', 'Dpl_01', 'afss03656', NULL,
    NULL, NULL, NULL, NULL,
    'Sala da Direção de Plantação', 'Departamento de Plantação', 'ATIVO', 'BOM',
    NULL, NULL, '', NULL,
    '2026-04-24T14:39:58', '2026-04-24T14:39:58', 'REMOTE-IMPORT', 0
);

-- ---------- 36 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'CMP-TOS-PYS-001', 'Dpl_02', 'Z108123H', NULL,
    '2024-07-10', NULL, NULL, NULL,
    'Sala de Plantação', 'Departamento de Plantação', 'ATIVO', 'OTIMO',
    NULL, NULL, '', NULL,
    '2026-04-24T14:39:58', '2026-04-24T14:39:58', 'REMOTE-IMPORT', 0
);

-- ---------- 37 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'CMP-HPX-15D-001', 'Dpl_03', '5CD33389FC', NULL,
    '2024-12-17', NULL, '2024-12-19', NULL,
    'Sala de Plantação', 'Departamento de Plantação', 'ATIVO', 'OTIMO',
    NULL, NULL, '', NULL,
    '2026-04-24T14:39:58', '2026-04-24T14:39:58', 'REMOTE-IMPORT', 0
);

-- ---------- 38 PULADO — CMP-DEL-INT-001 não existe no catálogo (órfão) ----------

-- ---------- 39 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'CMP-LEN-PF9-001', 'Drh_01', 'Agri-Dcl-Leno-00-00-A', '00:45:e2:99:de:25',
    '2026-04-24', NULL, '2026-08-04', NULL,
    'Sala da contabilidade escritorio plantação', 'Departamento Compras e Logística', 'EM_USO', 'BOM',
    NULL, NULL, '', 0,
    '2026-04-24T14:39:59', '2026-08-04T11:15:49', 'REMOTE-IMPORT', 0
);

-- ---------- 40 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'CMP-HPX-W4M-001', 'Drh_02', 'PD93165NG', NULL,
    '2024-06-19', NULL, NULL, NULL,
    'Sala Comum Escritorio Geral', 'Departamento de RH', 'ATIVO', 'CRITICO',
    NULL, NULL, 'Problema no acumulo de carga (bateria)', NULL,
    '2026-04-24T14:39:59', '2026-04-24T14:39:59', 'REMOTE-IMPORT', 0
);

-- ---------- 41 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'CMP-HPX-15D-001', 'Drh_04', '5CD3338JT4', NULL,
    '2024-12-17', NULL, '2024-12-18', NULL,
    'Sala Comum Escritorio Geral', 'Departamento de RH', 'ATIVO', 'OTIMO',
    NULL, NULL, '', NULL,
    '2026-04-24T14:39:59', '2026-04-24T14:39:59', 'REMOTE-IMPORT', 0
);

-- ---------- 42 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'MSE-LOG-MXA-001', 'Drh_04', 'Agri-Drh-Log-00-00-A', NULL,
    '2026-04-24', NULL, '2026-07-06', NULL,
    'Sala Comum Escritorio Geral', 'Departamento de RH', 'ATIVO', 'OTIMO',
    NULL, NULL, '', 0,
    '2026-04-24T14:39:59', '2026-07-06T14:16:23', 'REMOTE-IMPORT', 0
);

-- ---------- 43 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'CPU-DEL-D18-001', 'Hsa_03', 'J72GGAO1WCD', NULL,
    NULL, NULL, NULL, NULL,
    'Departamento de Higiene e Segurança', 'Departamento de HSA', 'ATIVO', 'CRITICO',
    NULL, NULL, 'Problemas na fonte de alimentação', NULL,
    '2026-04-24T14:39:59', '2026-04-24T14:39:59', 'REMOTE-IMPORT', 0
);

-- ---------- 44 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'MON-DEL-CN0-001', 'Hsa_03', '10C1KV2', NULL,
    '2026-04-24', NULL, '2026-08-11', NULL,
    'Departamento de Higiene e Segurança', 'Departamento de HSA', 'ATIVO', 'CRITICO',
    NULL, NULL, 'Linhas coloridas no visor', 0,
    '2026-04-24T14:39:59', '2026-08-11T14:06:39', 'REMOTE-IMPORT', 0
);

-- ---------- 45 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'CMP-HPX-RTL-001', 'Inf_01', 'CND2472S7W', NULL,
    '2023-03-11', NULL, NULL, NULL,
    'Sala do Responsável de Infraestruturas', 'Departamento de Infraestruturas', 'ATIVO', 'BOM',
    NULL, NULL, '', NULL,
    '2026-04-24T14:39:59', '2026-04-24T14:39:59', 'REMOTE-IMPORT', 0
);

-- ---------- 46 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'IMP-HPX-COL-002', 'Inf_02', 'CNBRQDDBSJ', NULL,
    NULL, NULL, NULL, NULL,
    'Sala da infraestrutura', 'Departamento de Infraestruturas', 'ATIVO', 'BOM',
    NULL, NULL, '', NULL,
    '2026-04-24T14:39:59', '2026-04-24T14:39:59', 'REMOTE-IMPORT', 0
);

-- ---------- 47 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'IMP-HPX-HPD-001', 'Inf_02', 'Agri-Inf-Hp-00-00-A', NULL,
    '2026-04-24', NULL, '2026-07-06', NULL,
    'Sala da infraestrutura', 'Departamento de Infraestruturas', 'ATIVO', 'BOM',
    NULL, NULL, '', 0,
    '2026-04-24T14:39:59', '2026-07-06T14:39:46', 'REMOTE-IMPORT', 0);

-- ---------- 48 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'IMP-BRO-DCP-001', 'Ofi_02', 'E78284L1N890853', NULL,
    NULL, NULL, NULL, NULL,
    'Sala do Responsável de Oficina', 'Departamento de Oficina', 'ATIVO', 'BOM',
    NULL, NULL, '', NULL,
    '2026-04-24T14:39:59', '2026-04-24T14:39:59', 'REMOTE-IMPORT', 0
);

-- ---------- 49 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'CMP-HPX-15D-001', 'Pom_016', '5CD3304ZCL', NULL,
    '2024-12-17', NULL, '2024-12-17', NULL,
    'Sala de Informática (POM)', 'Departamento de IT', 'ATIVO', 'OTIMO',
    NULL, NULL, '', NULL,
    '2026-04-24T14:39:59', '2026-04-24T14:39:59', 'REMOTE-IMPORT', 0
);

-- ---------- 50 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'IMP-HPX-COL-002', 'Pom_016', 'CNBRQDDB1M', NULL,
    '2024-11-21', NULL, NULL, NULL,
    'Sala de Informática (POM)', 'Departamento de IT', 'ATIVO', 'BOM',
    NULL, NULL, '', NULL,
    '2026-04-24T14:39:59', '2026-04-24T14:39:59', 'REMOTE-IMPORT', 0
);

-- ---------- 51 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'MON-VIE-VS1-001', 'Pom_016', 'VA2215-H', NULL,
    '2023-06-12', NULL, '2024-11-21', NULL,
    'Sala de Informática (POM)', 'Departamento de IT', 'ATIVO', 'BOM',
    NULL, NULL, '', NULL,
    '2026-04-24T14:39:59', '2026-04-24T14:39:59', 'REMOTE-IMPORT', 0
);

-- ---------- 52 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'CMP-DEL-VOS-001', 'Pom_02', 'D76FFL2', NULL,
    '2018-06-04', NULL, '2024-12-30', NULL,
    'Sala da Direção Industrial (POM)', 'Departamento Industrial (POM)', 'ATIVO', 'CRITICO',
    NULL, NULL, 'Teclas não funcionam', NULL,
    '2026-04-24T14:39:59', '2026-04-24T14:39:59', 'REMOTE-IMPORT', 0
);

-- ---------- 53 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'UPS-PHA-P1B-001', 'Pom_02', 'B-85579282', NULL,
    NULL, NULL, NULL, NULL,
    'Sala da Direção Industrial (POM)', 'Departamento Industrial (POM)', 'ATIVO', 'BOM',
    NULL, NULL, '', NULL,
    '2026-04-24T14:39:59', '2026-04-24T14:39:59', 'REMOTE-IMPORT', 0
);

-- ---------- 54 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'MON-LGX-24M-001', 'Pom_02', '603NTZN3X229', NULL,
    '2018-06-04', NULL, '2024-12-30', NULL,
    'Sala da Direção Industrial (POM)', 'Departamento Industrial (POM)', 'ATIVO', 'BOM',
    NULL, NULL, '', NULL,
    '2026-04-24T14:39:59', '2026-04-24T14:39:59', 'REMOTE-IMPORT', 0
);

-- ---------- 55 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'CPU-DEL-OPT-001', 'Pom_03', 'Agri-Pom-Dell-00-02-A', NULL,
    '2018-09-02', NULL, '2026-07-06', NULL,
    'Balança (POM)', 'Departamento Industrial (POM)', 'ATIVO', 'BOM',
    NULL, NULL, '', 0,
    '2026-04-24T14:39:59', '2026-07-06T14:39:02', 'REMOTE-IMPORT', 0
);

-- ---------- 56 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'IMP-HPX-LAS-001', 'Pom_03', 'PHCL115929', NULL,
    '2018-09-02', NULL, '2024-12-30', NULL,
    'Balança (POM)', 'Departamento Industrial (POM)', 'ATIVO', 'BOM',
    NULL, NULL, '', NULL,
    '2026-04-24T14:39:59', '2026-04-24T14:39:59', 'REMOTE-IMPORT', 0
);

-- ---------- 57 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'MON-DEL-GPO-001', 'Pom_03', 'CN-01M2XX', NULL,
    '2018-09-02', NULL, '2024-12-30', NULL,
    'Balança (POM)', 'Departamento Industrial (POM)', 'ATIVO', 'BOM',
    NULL, NULL, '', NULL,
    '2026-04-24T14:39:59', '2026-04-24T14:39:59', 'REMOTE-IMPORT', 0
);

-- ---------- 58 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'TEL-SAN-GAL-001', 'Pom_03', 'R7AY40BYMBZ', NULL,
    '2025-08-18', NULL, '2025-08-18', NULL,
    'Balança (POM)', 'Departamento Industrial (POM)', 'ATIVO', 'OTIMO',
    NULL, NULL, '', NULL,
    '2026-04-24T14:39:59', '2026-04-24T14:39:59', 'REMOTE-IMPORT', 0
);

-- ---------- 59 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'MON-DEL-CN0-001', 'Pom_05', 'Agri-Pom-Dell-00-00-A', NULL,
    '2026-04-24', NULL, '2026-07-06', NULL,
    'Escritório de Manutenção (POM)', 'Departamento Industrial (POM)', 'ATIVO', 'CRITICO',
    NULL, NULL, 'Linhas coloridas no visor', 0,
    '2026-04-24T14:39:59', '2026-07-06T14:02:10', 'REMOTE-IMPORT', 0
);

-- ---------- 60 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'CPU-DEL-D13-001', 'Pom_05', 'D9MNP A03 DC6', NULL,
    NULL, NULL, NULL, NULL,
    'Escritório de Manutenção (POM)', 'Departamento Industrial (POM)', 'ATIVO', 'BOM',
    NULL, NULL, '', NULL,
    '2026-04-24T14:39:59', '2026-04-24T14:39:59', 'REMOTE-IMPORT', 0
);

-- ---------- 61 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'CMP-HPX-RTL-001', 'Pom_08', 'CND2472S6W', NULL,
    '2023-06-04', NULL, '2024-12-30', NULL,
    'Laboratório (POM)', 'Departamento Industrial (POM)', 'ATIVO', 'BOM',
    NULL, NULL, '', NULL,
    '2026-04-24T14:39:59', '2026-04-24T14:39:59', 'REMOTE-IMPORT', 0
);

-- ---------- 62 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'MON-PHI-223-001', 'Pom_08', 'UK0A2245004956', NULL,
    '2023-10-04', NULL, NULL, NULL,
    'Laboratório (POM)', 'Departamento Industrial (POM)', 'ATIVO', 'BOM',
    NULL, NULL, '', NULL,
    '2026-04-24T14:39:59', '2026-04-24T14:39:59', 'REMOTE-IMPORT', 0
);

-- ---------- 63 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'TEL-SAN-GAL-001', 'Pom_08', 'R7AY40BX9FN', NULL,
    '2025-08-18', NULL, '2025-08-18', NULL,
    'Laboratório (POM)', 'Departamento Industrial (POM)', 'ATIVO', 'OTIMO',
    NULL, NULL, '', NULL,
    '2026-04-24T14:39:59', '2026-04-24T14:39:59', 'REMOTE-IMPORT', 0
);

-- ---------- 64 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'MON-SAM-S19-001', 'Pom_10', 'ZYGMH4LDCO5456 W', NULL,
    NULL, NULL, NULL, NULL,
    'Escritório de Produção (POM)', 'Departamento Industrial (POM)', 'ATIVO', 'BOM',
    NULL, NULL, '', NULL,
    '2026-04-24T14:39:59', '2026-04-24T14:39:59', 'REMOTE-IMPORT', 0
);

-- ---------- 65 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'CPU-DEL-VES-001', 'Pom_10', 'CN25N8X2', NULL,
    NULL, NULL, NULL, NULL,
    'Escritório de Produção (POM)', 'Departamento Industrial (POM)', 'ATIVO', 'BOM',
    NULL, NULL, '', NULL,
    '2026-04-24T14:39:59', '2026-04-24T14:39:59', 'REMOTE-IMPORT', 0
);

-- ---------- 66 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'MON-PHI-223-002', 'Pom_11', 'UK0A2246004218', NULL,
    '2023-06-07', NULL, '2024-12-30', NULL,
    'Sala do Departamento Comercial (POM)', 'Departamento Finenceiro', 'ATIVO', 'BOM',
    NULL, NULL, '', NULL,
    '2026-04-24T14:39:59', '2026-04-24T14:39:59', 'REMOTE-IMPORT', 0
);

-- ---------- 67 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'TEL-SAM-GAL-001', 'Pom_11', 'RF8TA0RFN3E', NULL,
    '2023-06-07', NULL, NULL, NULL,
    'Sala do Departamento Comercial (POM)', 'Departamento Finenceiro', 'ATIVO', 'BOM',
    NULL, NULL, '', NULL,
    '2026-04-24T14:39:59', '2026-04-24T14:39:59', 'REMOTE-IMPORT', 0
);

-- ---------- 68 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'CPU-DEL-D18-001', 'Pom_13', 'VOVFTA00WCD', NULL,
    NULL, NULL, NULL, NULL,
    'Sala do responsavel de produção', 'Departamento Industrial (POM)', 'ATIVO', 'BOM',
    NULL, NULL, '', NULL,
    '2026-04-24T14:39:59', '2026-04-24T14:39:59', 'REMOTE-IMPORT', 0
);

-- ---------- 69 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'CMP-HPX-HP2-002', 'Pom_14', 'Agri-Pom-Hp-00-00-A', NULL,
    '2026-04-24', NULL, '2026-08-10', NULL,
    'Recepção do Dep Industrial', 'Departamento Industrial (POM)', 'EM_USO', 'CRITICO',
    NULL, NULL, 'Problemas no teclado Partes do computador esta partido', 0,
    '2026-04-24T14:39:59', '2026-08-10T14:24:30', 'REMOTE-IMPORT', 0
);

-- ---------- 70 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'TCD-LOG-MXK-001', 'Pom_14', 'Agri-Pom-Log-00-00-A', NULL,
    '2026-04-24', NULL, '2026-07-03', NULL,
    'Recepção do Dep Industrial', 'Departamento Industrial (POM)', 'ATIVO', 'OTIMO',
    NULL, NULL, '', 0,
    '2026-04-24T14:39:59', '2026-07-03T13:55:11', 'REMOTE-IMPORT', 0
);

-- ---------- 71 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'TEL-SAN-GAL-001', 'Pom_14', 'R7AY40BXQMY', NULL,
    '2025-08-18', NULL, '2025-08-18', NULL,
    'Recepção do Dep Industrial', 'Departamento Industrial (POM)', 'ATIVO', 'OTIMO',
    NULL, NULL, '', NULL,
    '2026-04-24T14:39:59', '2026-04-24T14:39:59', 'REMOTE-IMPORT', 0
);

-- ---------- 72 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'TEL-SAN-GAL-001', 'Pom_15', 'R7AY31AJVXB', NULL,
    '2025-08-18', NULL, '2025-08-18', NULL,
    'Laboratorio Industrial', 'Departamento Industrial (POM)', 'ATIVO', 'OTIMO',
    NULL, NULL, '', NULL,
    '2026-04-24T14:39:59', '2026-04-24T14:39:59', 'REMOTE-IMPORT', 0
);

-- ---------- 73 (sem funcionário) ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'IMP-HPX-COL-001', NULL, 'CF533A/1816C002', NULL,
    NULL, NULL, NULL, NULL,
    'Armazém Geral', 'Departamento de Armazém', 'ATIVO', 'BOM',
    NULL, NULL, '', NULL,
    '2026-04-24T14:39:59', '2026-04-24T14:39:59', 'REMOTE-IMPORT', 0
);

-- ---------- 74 (sem funcionário) ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'IMP-HPX-COL-001', NULL, 'CF532A/1815C002', NULL,
    NULL, NULL, NULL, NULL,
    'Armazém Geral', 'Departamento de Armazém', 'ATIVO', 'BOM',
    NULL, NULL, '', NULL,
    '2026-04-24T14:39:59', '2026-04-24T14:39:59', 'REMOTE-IMPORT', 0
);

-- ---------- 75 (sem funcionário) ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'IMP-HPX-COL-001', NULL, 'CF530A/1782C002', NULL,
    NULL, NULL, NULL, NULL,
    'Armazém Geral', 'Departamento de Armazém', 'ATIVO', 'BOM',
    NULL, NULL, '', NULL,
    '2026-04-24T14:39:59', '2026-04-24T14:39:59', 'REMOTE-IMPORT', 0
);

-- ---------- 76 (sem funcionário) ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'IMP-HPX-COL-001', NULL, 'CF531A/1817C002', NULL,
    NULL, NULL, NULL, NULL,
    'Armazém Geral', 'Departamento de Armazém', 'ATIVO', 'BOM',
    NULL, NULL, '', NULL,
    '2026-04-24T14:39:59', '2026-04-24T14:39:59', 'REMOTE-IMPORT', 0
);

-- ---------- 77 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'TON-HPX-LAS-001', 'Drh_06', 'CF259A/3183C002', '56',
    '2024-09-29', NULL, NULL, NULL,
    'Balança (POM)', 'Departamento de RH', 'ATIVO', 'BOM',
    NULL, NULL, '', NULL,
    '2026-04-24T14:39:59', '2026-04-30T13:50:21', 'REMOTE-IMPORT', 0
);

-- ---------- 78 (sem funcionário) ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'MON-PHI-223-001', NULL, 'UK0A1644019851', NULL,
    NULL, NULL, NULL, NULL,
    'Sala da Direção Industrial (POM)', 'Departamento Industrial (POM)', 'ATIVO', 'BOM',
    NULL, NULL, '', NULL,
    '2026-04-24T14:39:59', '2026-04-24T14:39:59', 'REMOTE-IMPORT', 0
);

-- ---------- 79 (sem funcionário) ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'OUT-TOP-T23-001', NULL, '21K5D3791220944', NULL,
    NULL, NULL, NULL, NULL,
    'Sala de reunião', 'Departamento Industrial (POM)', 'ATIVO', 'BOM',
    NULL, NULL, '', NULL,
    '2026-04-24T14:39:59', '2026-04-24T14:39:59', 'REMOTE-IMPORT', 0
);

-- ---------- 80 (sem funcionário) ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'UPS-PHA-P8N-001', NULL, '341810504666', NULL,
    NULL, NULL, NULL, NULL,
    'Laboratório (POM)', 'Departamento Industrial (POM)', 'ATIVO', 'BOM',
    NULL, NULL, '', NULL,
    '2026-04-24T14:39:59', '2026-04-24T14:39:59', 'REMOTE-IMPORT', 0
);

-- ---------- 82 (sem funcionário) ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'CMP-ASU-X55-001', NULL, '6522', NULL,
    NULL, NULL, NULL, NULL,
    'Sala do Responsável de Oficina', 'Departamento de Oficina', 'ATIVO', 'BOM',
    NULL, NULL, '', NULL,
    '2026-04-24T14:39:59', '2026-04-24T14:39:59', 'REMOTE-IMPORT', 0
);

-- ---------- 83 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'CMP-DEL-VOS-001', 'Amz_02', 'Agri-Dcl-Dell-00-01-A', NULL,
    '2026-04-24', NULL, '2026-07-06', NULL,
    'Armazém Geral', 'Departamento de Armazém', 'ATIVO', 'BOM',
    NULL, NULL, '', 0,
    '2026-04-24T14:39:59', '2026-07-06T14:18:13', 'REMOTE-IMPORT', 0
);

-- ---------- 84 (sem funcionário) ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'CPU-DEL-D18-001', NULL, 'K37MTA01EMP', NULL,
    NULL, NULL, NULL, NULL,
    'Departamento Financeiro e Contabilidade', 'Departamento Financeiro', 'ATIVO', 'BOM',
    NULL, NULL, '', NULL,
    '2026-04-24T14:39:59', '2026-04-24T14:39:59', 'REMOTE-IMPORT', 0
);

-- ---------- 85 (sem funcionário) ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'CPU-DEL-D18-001', NULL, 'J72GGA01WCD/J72gga01wcd', NULL,
    NULL, NULL, NULL, NULL,
    'Gabinete Técnico de Plantação', 'Departamento de Plantação', 'ATIVO', 'BOM',
    NULL, NULL, '', NULL,
    '2026-04-24T14:39:59', '2026-04-24T14:39:59', 'REMOTE-IMPORT', 0
);

-- ---------- 86 (sem funcionário) ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'IMP-HPX-COL-003', NULL, 'VNBKN683JX', NULL,
    NULL, NULL, NULL, NULL,
    'Escritório Geral', 'Departamento Financeiro', 'ATIVO', 'BOM',
    NULL, NULL, '', NULL,
    '2026-04-24T14:39:59', '2026-04-24T14:39:59', 'REMOTE-IMPORT', 0
);

-- ---------- 87 (sem funcionário) ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'TON-HPX-COL-001', NULL, '207x/w2213x', NULL,
    NULL, NULL, NULL, NULL,
    'Escritório Geral', 'Departamento Financeiro', 'ATIVO', 'BOM',
    NULL, NULL, '', NULL,
    '2026-04-24T14:39:59', '2026-04-24T14:39:59', 'REMOTE-IMPORT', 0
);

-- ---------- 88 (sem funcionário) ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'TON-HPX-COL-001', NULL, '207x/w2210x', NULL,
    NULL, NULL, NULL, NULL,
    'Escritório Geral', 'Departamento Financeiro', 'ATIVO', 'BOM',
    NULL, NULL, '', NULL,
    '2026-04-24T14:39:59', '2026-04-24T14:39:59', 'REMOTE-IMPORT', 0
);

-- ---------- 89 (sem funcionário) ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'TON-HPX-COL-001', NULL, '207x/w2212x', NULL,
    NULL, NULL, NULL, NULL,
    'Escritório Geral', 'Departamento Financeiro', 'ATIVO', 'BOM',
    NULL, NULL, '', NULL,
    '2026-04-24T14:39:59', '2026-04-24T14:39:59', 'REMOTE-IMPORT', 0
);

-- ---------- 90 (sem funcionário) ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'TON-HPX-COL-001', NULL, '207x/w2211x', NULL,
    NULL, NULL, NULL, NULL,
    'Escritório Geral', 'Departamento Financeiro', 'ATIVO', 'BOM',
    NULL, NULL, '', NULL,
    '2026-04-24T14:39:59', '2026-04-24T14:39:59', 'REMOTE-IMPORT', 0
);

-- ---------- 91 (sem funcionário) ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'IMP-HPX-COL-003', NULL, 'VNBKN683GY', NULL,
    NULL, NULL, NULL, NULL,
    'Sala do Director Geral/avariada', 'Direção Geral', 'ATIVO', 'BOM',
    NULL, NULL, '', NULL,
    '2026-04-24T14:39:59', '2026-04-24T14:39:59', 'REMOTE-IMPORT', 0
);

-- ---------- 92 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'IMP-BRO-DCP-001', 'Ofi_02', 'Agri-Dofi-Broth-00-00-A', NULL,
    '2026-04-24', NULL, '2026-08-04', NULL,
    'Sala do Responsável de Oficina', 'Departamento de Oficina', 'ATIVO', 'BOM',
    NULL, NULL, '', 0,
    '2026-04-24T14:40:00', '2026-08-04T10:54:45', 'REMOTE-IMPORT', 0
);

-- ---------- 93 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'MON-LGX-19M-001', 'Inf_01', 'Agri-DInfa-Lg-00-00-A', NULL,
    '2026-04-24', NULL, '2026-08-04', NULL,
    'Sala do Responsável de Infraestruturas', 'Departamento de Infraestruturas', 'EM_USO', 'BOM',
    NULL, NULL, '', 0,
    '2026-04-24T14:40:00', '2026-08-04T10:56:11', 'REMOTE-IMPORT', 0
);

-- ---------- 94 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'MON-DEL-CN0-001', 'Daf_02', 'Agri-Daf-Dell-00-03-A', NULL,
    '2026-04-24', NULL, '2026-07-06', NULL,
    'Escritório de Manutenção (POM)', 'Departamento Industrial (POM)', 'ATIVO', 'BOM',
    NULL, NULL, '', 0,
    '2026-04-24T14:40:00', '2026-07-06T14:25:16', 'REMOTE-IMPORT', 0
);

-- ---------- 95 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'NET-TPL-TLW-001', 'Pom_016', 'Agri-Pom-Tplink-00-00-A', NULL,
    '2026-04-24', NULL, '2026-08-04', NULL,
    'Sala de Informática (POM)', 'Departamento de IT', 'ATIVO', 'BOM',
    NULL, NULL, '', 0,
    '2026-04-24T14:40:00', '2026-08-04T10:57:42', 'REMOTE-IMPORT', 0
);

-- ---------- 96 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'IMP-HPX-COL-002', 'Dge_01', 'Agri-Dge-Hp-00-02-A', NULL,
    '2026-04-24', NULL, '2026-08-04', NULL,
    'Sala da Direção Geral', 'Direção Geral', 'ATIVO', 'BOM',
    NULL, NULL, '', 0,
    '2026-04-24T14:40:00', '2026-08-04T10:58:43', 'REMOTE-IMPORT', 0
);

-- ---------- 97 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'IMP-HPX-COL-002', 'Daf_01', 'Agri-Daf-Hp-00-00-A', NULL,
    '2026-04-24', NULL, '2026-08-04', NULL,
    'Sala da Direção Financeira', 'Departamento Financeiro', 'ATIVO', 'BOM',
    NULL, NULL, '', 0,
    '2026-04-24T14:40:00', '2026-08-04T10:59:52', 'REMOTE-IMPORT', 0
);

-- ---------- 98 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'IMP-HPX-COL-002', 'Daf_01', 'Agri-Daf-Hp-00-01-A', NULL,
    '2026-04-24', NULL, '2026-08-04', NULL,
    'Sala da Direção Financeira', 'Departamento Financeiro', 'EM_USO', 'BOM',
    NULL, NULL, '', 0,
    '2026-04-24T14:40:00', '2026-08-04T11:01:42', 'REMOTE-IMPORT', 0
);

-- ---------- 99 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'IMP-HPX-COL-002', 'Daf_01', 'Agri-Daf-Dell-00-07-A', NULL,
    '2026-04-24', NULL, '2026-07-07', NULL,
    'Escritório Geral', 'Departamento Financeiro', 'ATIVO', 'BOM',
    NULL, NULL, '', 0,
    '2026-04-24T14:40:00', '2026-07-07T12:40:44', 'REMOTE-IMPORT', 0
);

-- ---------- 100 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'CMP-HPX-INT-001', 'Dge_01', 'Agri-Dge-Hp-00-00-A', NULL,
    '2024-07-12', NULL, '2026-07-06', NULL,
    'Sala da Direção Geral', 'Direção Geral', 'ATIVO', 'BOM',
    NULL, NULL, '', 0,
    '2026-04-24T14:40:00', '2026-07-06T14:46:05', 'REMOTE-IMPORT', 0
);

-- ---------- 101 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'CMP-THI-LEN-001', 'Dpl_01', 'Agri-Dpl-Leno-00-01-A', NULL,
    '2026-04-24', NULL, '2026-08-04', NULL,
    'Sala da Direção de Plantação', 'Departamento de Plantação', 'ATIVO', 'BOM',
    NULL, NULL, '', 0,
    '2026-04-24T14:40:00', '2026-08-04T11:02:38', 'REMOTE-IMPORT', 0
);

-- ---------- 102 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'IMP-CAN-PIX-001', 'Dpl_02', 'Agri-Dpl-Canon-00-00-A', NULL,
    '2026-04-24', NULL, '2026-08-04', NULL,
    'Sala de Plantação', 'Departamento de Plantação', 'ATIVO', 'BOM',
    NULL, NULL, '', 0,
    '2026-04-24T14:40:00', '2026-08-04T11:03:33', 'REMOTE-IMPORT', 0
);

-- ---------- 103 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'IMP-HPX-HPL-001', 'Dge_02', 'Agri-Dge-Hp-00-03-A', NULL,
    '2026-04-24', NULL, '2026-08-04', NULL,
    'Secretaria da Direção Geral', 'Direção Geral', 'EM_USO', 'BOM',
    NULL, NULL, '', 0,
    '2026-04-24T14:40:00', '2026-08-04T11:04:41', 'REMOTE-IMPORT', 0
);

-- ---------- 104 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'IMP-HPX-HPL-001', 'Dpl_01', '19A(CF219A)/17A(CF217A)', NULL,
    '2026-04-24', NULL, '2026-08-04', NULL,
    'Sala da Direção de Plantação', 'Departamento de Plantação', 'ATIVO', 'BOM',
    NULL, NULL, '', 0,
    '2026-04-24T14:40:00', '2026-08-04T11:15:18', 'REMOTE-IMPORT', 0
);

-- ---------- 105 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'CMP-HPX-316-001', 'Dge_02', 'Agri-Dge-Hp-00-04-A', NULL,
    '2026-04-24', NULL, '2026-08-04', NULL,
    'Secretaria da Direção Geral', 'Direção Geral', 'ATIVO', 'BOM',
    NULL, NULL, '', 0,
    '2026-04-24T14:40:00', '2026-08-04T11:07:14', 'REMOTE-IMPORT', 0
);

-- ---------- 106 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'CPU-DEL-D18-001', 'Pom_016', 'Agri-Pom-Dell-00-04-A', NULL,
    '2026-04-24', NULL, '2026-08-04', NULL,
    'Sala de Informática (POM)', 'Departamento de IT', 'BAIXADO', 'BOM',
    NULL, NULL, '', 1,
    '2026-04-24T14:40:00', '2026-08-04T11:08:22', 'REMOTE-IMPORT', 0
);

-- ---------- 107 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'NET-TPL-TLW-001', 'Amz_03', 'Agri-Dcl-Tplink-00-00-A', NULL,
    '2026-04-24', NULL, '2026-08-04', NULL,
    'Armazém (POM)', 'Departamento de Armazém', 'ATIVO', 'BOM',
    NULL, NULL, '', 0,
    '2026-04-24T14:40:00', '2026-08-04T11:10:00', 'REMOTE-IMPORT', 0
);

-- ---------- 108 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'IMP-HPX-HPD-001', 'Drh_06', 'Agri-Drh-Hp-00-00-A', NULL,
    '2026-04-24', NULL, '2026-07-06', NULL,
    'Sala do Responsável de Infraestruturas', 'Departamento de Infraestruturas', 'ATIVO', 'BOM',
    NULL, NULL, '', 0,
    '2026-04-24T14:40:00', '2026-07-06T14:40:29', 'REMOTE-IMPORT', 0
);

-- ---------- 109 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'IMP-HPX-HPD-001', 'Djr_01', 'Agri-DInfra-Hp-00-00-A', NULL,
    '2026-04-24', NULL, '2026-08-04', NULL,
    'Sala do Responsável Jurídico', 'Departamento Jurídico', 'ATIVO', 'BOM',
    NULL, NULL, '', 0,
    '2026-04-24T14:40:00', '2026-08-04T11:11:48', 'REMOTE-IMPORT', 0
);

-- ---------- 110 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'IMP-HPX-HPD-001', 'Inf_01', 'Agri-DInfra-Hp-00-01-A', NULL,
    '2026-04-24', NULL, '2026-08-04', NULL,
    'Sala do Responsável de Infraestruturas', 'Departamento de Infraestruturas', 'ATIVO', 'BOM',
    NULL, NULL, '', 0,
    '2026-04-24T14:40:00', '2026-08-04T11:12:47', 'REMOTE-IMPORT', 0
);

-- ---------- 111 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'IMP-HPX-COL-002', 'Dge_01', 'Agri-Dge-Hp-00-05-A', NULL,
    '2026-04-24', NULL, '2026-08-04', NULL,
    'Sala da Direção Geral', 'Direção Geral', 'ATIVO', 'BOM',
    NULL, NULL, '', 0,
    '2026-04-24T14:40:00', '2026-08-04T11:14:53', 'REMOTE-IMPORT', 0
);

-- ---------- 112 (sem funcionário) ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'CMP-HPX-RTL-001', NULL, 'CND2472S75', NULL,
    '2023-07-14', NULL, NULL, NULL,
    'Sala do Responsável de Oficina', 'Departamento de Oficina', 'ATIVO', 'BOM',
    NULL, NULL, '', NULL,
    '2026-04-24T14:40:01', '2026-04-24T14:40:01', 'REMOTE-IMPORT', 0
);

-- ---------- 113 (sem funcionário) ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'CMP-HPX-RTL-001', NULL, 'CND2472S6R', NULL,
    '2023-08-01', NULL, NULL, NULL,
    'Sala do Responsável Jurídico', 'Departamento Jurídico', 'ATIVO', 'BOM',
    NULL, NULL, '', NULL,
    '2026-04-24T14:40:01', '2026-04-24T14:40:01', 'REMOTE-IMPORT', 0
);

-- ---------- 114 (sem funcionário) ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'CMP-HPX-RTL-001', NULL, 'CND2472S8L', NULL,
    '2023-08-04', NULL, NULL, NULL,
    'Escritório 40" (POM)', 'Departamento de HSA', 'ATIVO', 'BOM',
    NULL, NULL, '', NULL,
    '2026-04-24T14:40:01', '2026-04-24T14:40:01', 'REMOTE-IMPORT', 0
);

-- ---------- 115 (sem funcionário) ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'CMP-HPX-RTL-001', NULL, 'Agri-Dpl-Hp-00-00-A', NULL,
    '2026-04-24', NULL, '2026-07-06', NULL,
    '', 'Departamento de HSA', 'ATIVO', 'BOM',
    NULL, NULL, '', 0,
    '2026-04-24T14:40:01', '2026-07-06T14:19:27', 'REMOTE-IMPORT', 0
);

-- ---------- 116 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'CPU-DEL-OPT-001', 'Daf_05', 'Agri-Daf-Dell-00-13-A', '24:2f:d0:d9:82:71',
    '2026-04-24', '2026-06-03', '2026-08-03', NULL,
    'Sala da contabilidade Escritorio Plantação', 'Departamento Financeiro', 'EM_USO', 'BOM',
    NULL, NULL, 'Desliga sozinho.', 0,
    '2026-04-24T14:40:01', '2026-08-03T12:23:34', 'REMOTE-IMPORT', 0
);

-- ---------- 117 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'MON-DEL-CN0-001', 'Daf_02', 'Agri-Daf-Dell-00-14-A', NULL,
    '2026-04-24', NULL, '2026-08-03', NULL,
    'Departamento Financeiro e Contabilidade', 'Departamento Financeiro', 'ATIVO', 'BOM',
    NULL, NULL, '', 0,
    '2026-04-24T14:40:01', '2026-08-03T12:23:47', 'REMOTE-IMPORT', 0
);

-- ---------- 118 (sem dados — só localização e departamento) ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'CMP-HPX-INT-001', NULL, NULL, NULL,
    NULL, NULL, NULL, NULL,
    'Sala da Direção Geral', 'Direção Geral', 'ATIVO', 'BOM',
    NULL, NULL, '', NULL,
    '2026-04-24T14:40:01', '2026-04-24T14:40:01', 'REMOTE-IMPORT', 0
);

-- ---------- 119 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'CMP-THI-LEN-001', 'Daf_09', 'Agri-Daf-Len-00-00-A', '28:c6:3f:be:a3:02',
    '2026-04-24', NULL, '2026-08-03', NULL,
    'Sala de tesouraria do escritorio plantação', 'Departamento Financeiro', 'EM_USO', 'REGULAR',
    NULL, NULL, '', 0,
    '2026-04-24T14:40:01', '2026-08-03T12:31:38', 'REMOTE-IMPORT', 0
);

-- ---------- 120 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'IMP-HPX-HPD-001', 'Drh_06', 'Agri-Drh-Hp-00-01-A', NULL,
    '2019-07-03', NULL, '2026-07-06', NULL,
    'Sala do Responsável de Infraestruturas', 'Departamento de Infraestruturas', 'ATIVO', 'BOM',
    NULL, NULL, '', 0,
    '2026-04-24T14:40:01', '2026-07-06T14:43:52', 'REMOTE-IMPORT', 0
);

-- ---------- 121 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'MON-VIE-VS1-001', 'Dge_02', 'Agri-Dge-View-00-00-A', NULL,
    '2026-04-24', NULL, '2026-08-04', NULL,
    'Secretaria da Direção Geral', 'Direção Geral', 'EM_USO', 'BOM',
    NULL, NULL, '', 0,
    '2026-04-24T14:40:01', '2026-08-04T10:53:01', 'REMOTE-IMPORT', 0
);

-- ---------- 123 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'CMP-HPX-250-001', 'Amz_03', 'Agri-Dcl-Hp-00-00-A', '18:47:3D:D7:23:13',
    '2026-04-24', NULL, '2026-07-06', NULL,
    'Armazém (POM)', 'Departamento de Armazém', 'ATIVO', 'BOM',
    NULL, NULL, '', 0,
    '2026-04-24T14:40:01', '2026-07-06T14:44:57', 'REMOTE-IMPORT', 0
);

-- ---------- 124 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'CMP-HPX-15F-001', 'Daf_01', 'Agri-Daf-Dell-00-06-A', 'E8:B0:C5:5C:91:F8',
    '2026-04-24', NULL, '2026-07-07', NULL,
    'Sala da Direção Financeira', 'Departamento Financeiro', 'EM_USO', 'BOM',
    NULL, NULL, '', 0,
    '2026-04-24T14:40:01', '2026-07-07T11:26:53', 'REMOTE-IMPORT', 0
);

-- ---------- 125 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'MSE-COM-WIR-001', 'Daf_04', 'Agri-Daf-Tec-00-00-A', NULL,
    '2026-04-24', NULL, '2026-08-03', NULL,
    'Departamento Financeiro e Contabilidade', 'Departamento Financeiro', 'ATIVO', 'BOM',
    NULL, NULL, '', 0,
    '2026-04-24T14:40:01', '2026-08-03T12:32:16', 'REMOTE-IMPORT', 0
);

-- ---------- 126 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'TCD-EQU-NO2-001', 'Daf_04', 'Agri-Daf-Tec-00-01-A', NULL,
    '2026-04-24', NULL, '2026-08-03', NULL,
    'Departamento Financeiro e Contabilidade', 'Departamento Financeiro', 'ATIVO', 'BOM',
    NULL, NULL, '', 0,
    '2026-04-24T14:40:01', '2026-08-03T12:32:38', 'REMOTE-IMPORT', 0
);

-- ---------- 127 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'CMP-HPX-INT-001', 'Dge_01', 'Agri-Dge-Hp-00-01-A', NULL,
    '2026-04-24', NULL, '2026-08-03', NULL,
    'Sala da Direção Geral', 'Direção Geral', 'ATIVO', 'BOM',
    NULL, NULL, '', 0,
    '2026-04-24T14:40:01', '2026-08-03T12:33:07', 'REMOTE-IMPORT', 0
);

-- ---------- 128 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'MON-DEL-P24-001', 'Dcl_03', 'Agri-Dcl-Dell-00-05-A', NULL,
    '2026-04-24', '2026-06-05', '2026-08-03', NULL,
    'Sala de contabilidade Escritorio plantação', 'Departamento Compras e Logística', 'EM_USO', 'OTIMO',
    NULL, NULL, '', 1,
    '2026-04-24T14:40:01', '2026-08-03T12:34:01', 'REMOTE-IMPORT', 0
);

-- ---------- 129 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'TCD-LOG-MXK-002', 'Dcl_03', 'Agri-Dcl-Logi-00-02-A', NULL,
    '2026-04-24', NULL, '2026-08-03', NULL,
    'Sala de contabilidade Escritorio plantação', 'Departamento Compras e Logística', 'EM_USO', 'BOM',
    NULL, NULL, '', 0,
    '2026-04-24T14:40:01', '2026-08-03T13:26:30', 'REMOTE-IMPORT', 0
);

-- ---------- 130 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'CMP-THI-LEN-001', 'Dpl_01', 'Agri-Dpl-Len-00-00-A', NULL,
    '2026-04-24', NULL, '2026-08-03', NULL,
    'Sala da Direção de Plantação', 'Departamento de Plantação', 'ATIVO', 'BOM',
    NULL, NULL, '', 0,
    '2026-04-24T14:40:01', '2026-08-03T12:33:33', 'REMOTE-IMPORT', 0
);

-- ---------- 131 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'CMP-ASU-F16-001', 'Dpl_04', 'Agri-Dpl-Asus-00-00-A', NULL,
    '2026-04-24', NULL, '2026-08-03', NULL,
    '', 'Departamento de Plantação', 'ATIVO', 'OTIMO',
    NULL, NULL, '', 0,
    '2026-04-24T14:40:01', '2026-08-03T13:27:57', 'REMOTE-IMPORT', 0
);

-- ---------- 132 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'MSE-LOG-MXA-001', 'Drh_01', 'Agri-Dcl-Logi-00-03-A', NULL,
    '2026-04-24', NULL, '2026-08-03', NULL,
    'Sala da contabilidade escritorio plantação', 'Departamento Compras e Logística', 'EM_USO', 'BOM',
    NULL, NULL, '', 0,
    '2026-04-24T14:40:01', '2026-08-03T13:27:11', 'REMOTE-IMPORT', 0
);

-- ---------- 133 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'CPU-DEL-OPT-001', 'Drh_05', 'Agri-Drh-Dell-00-01-A', NULL,
    '2026-04-24', NULL, '2026-07-06', NULL,
    'Sala Comum Escritorio Geral', 'Departamento de RH', 'ATIVO', 'BOM',
    NULL, NULL, '', 0,
    '2026-04-24T14:40:01', '2026-07-06T14:38:41', 'REMOTE-IMPORT', 0
);

-- ---------- 134 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'MON-DEL-9Q8-001', 'Drh_05', 'Agri-Drh-Dell-00-03-A', NULL,
    '2026-04-24', NULL, '2026-08-03', NULL,
    'Sala Comum Escritorio Geral', 'Departamento de RH', 'ATIVO', 'BOM',
    NULL, NULL, '', 0,
    '2026-04-24T14:40:01', '2026-08-03T13:28:25', 'REMOTE-IMPORT', 0
);

-- ---------- 135 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'TCD-DEL-ONN-001', 'Drh_05', 'Agri-Drh-Dell-00-04-A', NULL,
    '2026-04-24', NULL, '2026-08-03', NULL,
    'Sala Comum Escritorio Geral', 'Departamento de RH', 'ATIVO', 'BOM',
    NULL, NULL, '', 0,
    '2026-04-24T14:40:01', '2026-08-03T13:28:53', 'REMOTE-IMPORT', 0
);

-- ---------- 136 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'MSE-HPX-7CH-001', 'Drh_05', 'Agri-Drh-Dell-00-05-A', NULL,
    '2026-04-24', NULL, '2026-08-03', NULL,
    'Sala Comum Escritorio Geral', 'Departamento de RH', 'ATIVO', 'BOM',
    NULL, NULL, '', 0,
    '2026-04-24T14:40:01', '2026-08-03T13:29:37', 'REMOTE-IMPORT', 0
);

-- ---------- 137 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'CPU-DEL-OPT-001', 'Drh_06', 'Agri-Drh-Dell-00-02-A', NULL,
    '2026-04-24', NULL, '2026-07-06', NULL,
    'Sala Comum Escritorio Geral', 'Departamento de RH', 'ATIVO', 'BOM',
    NULL, NULL, '', 0,
    '2026-04-24T14:40:02', '2026-07-06T14:40:07', 'REMOTE-IMPORT', 0
);

-- ---------- 138 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'MON-DEL-4S8-001', 'Drh_06', 'Agri-Drh-Dell-00-00-A', NULL,
    '2026-04-24', NULL, '2026-07-06', NULL,
    'Sala Comum Escritorio Geral', 'Departamento de RH', 'ATIVO', 'BOM',
    NULL, NULL, '', 0,
    '2026-04-24T14:40:02', '2026-07-06T14:26:38', 'REMOTE-IMPORT', 0
);

-- ---------- 139 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'IMP-HPX-HPD-001', 'Inf_02', 'Agri-Inf-Hp-00-01-A', NULL,
    '2018-07-10', NULL, '2026-07-06', NULL,
    'Sala da infraestrutura', 'Departamento de Infraestruturas', 'ATIVO', 'BOM',
    NULL, NULL, '', 0,
    '2026-04-24T14:40:02', '2026-07-06T14:44:30', 'REMOTE-IMPORT', 0
);

-- ---------- 140 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'MON-DEL-CN0-001', 'Pom_05', 'Agri-Pom-Dell-00-01-A', NULL,
    '2026-04-24', NULL, '2026-08-03', NULL,
    'Escritório de Manutenção (POM)', 'Departamento Industrial (POM)', 'ATIVO', 'BOM',
    NULL, NULL, 'Linhas coloridas no visor', 1,
    '2026-04-24T14:40:02', '2026-08-03T12:22:52', 'REMOTE-IMPORT', 0
);

-- ---------- 141 (num_serie duplicado — renomeado -DUP) ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'TCD-LOG-MXK-002', 'Pom_11', 'Agri-Dcl-Log-00-00-A-DUP', NULL,
    '2026-04-24', NULL, '2026-07-06', NULL,
    'Sala do Departamento Comercial (POM)', 'Departamento Financeiro', 'ATIVO', 'OTIMO',
    NULL, NULL, '', 0,
    '2026-04-24T14:40:02', '2026-07-06T14:16:43', 'REMOTE-IMPORT', 0
);

-- ---------- 144 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'MON-DEL-E24-004', 'Daf_01', 'Agri-Daf-Dell-00-12-A', NULL,
    '2026-05-29', '2026-05-29', '2026-08-03', NULL,
    'Sala da Direção Financeira', 'Departamento Financeiro', 'EM_USO', 'OTIMO',
    NULL, NULL, '', 0,
    '2026-05-29T11:38:30', '2026-08-03T12:21:30', 'REMOTE-IMPORT', 0
);

-- ---------- 145 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'MON-DEL-E24-005', 'Daf_07', 'Agri-Daf-Dell-00-02-A', NULL,
    '2026-06-03', '2026-06-03', '2026-07-06', NULL,
    'Departamento Financeiro e Contabilidade', 'Departamento Financeiro', 'EM_USO', 'BOM',
    NULL, NULL, '', 0,
    '2026-06-03T09:53:52', '2026-07-06T14:24:48', 'REMOTE-IMPORT', 0
);

-- ---------- 146 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'CMP-DEL-VES-001', 'Daf_07', 'Agri-Daf-Dell-00-11-A', '74:40:bb:6d:1d:3b',
    '2026-04-24', '2026-06-03', '2026-08-03', NULL,
    'Departamento Financeiro e Contabilidade', 'Departamento Financeiro', 'EM_USO', 'BOM',
    NULL, NULL, 'Com problemas no caregador, causando pequenos curto circuitos.', 1,
    '2026-06-03T10:07:20', '2026-08-03T12:20:54', 'REMOTE-IMPORT', 0
);

-- ---------- 147 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'MON-DEL-GYB-001', 'Daf_05', 'Agri-Daf-Dell-00-01-A', NULL,
    '2026-04-24', '2026-06-03', '2026-07-06', NULL,
    'Sala da contabilidade Escritorio Plantação', 'Departamento Financeiro', 'EM_USO', 'BOM',
    NULL, NULL, '', 0,
    '2026-06-03T10:36:57', '2026-07-06T14:23:05', 'REMOTE-IMPORT', 0
);

-- ---------- 148 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'TCD-LOG-MXK-001', 'Daf_05', 'Agri-Daf-Logi-00-00-A', NULL,
    '2026-04-24', '2026-06-03', '2026-08-03', NULL,
    'Sala da contabilidade Escritorio Plantação', 'Departamento Financeiro', 'EM_USO', 'BOM',
    NULL, NULL, '', 0,
    '2026-06-03T10:59:55', '2026-08-03T11:29:03', 'REMOTE-IMPORT', 0
);

-- ---------- 149 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'CMP-DEL-ESS-017', 'Daf_07', 'Agri-Daf-Dell-00-10-A', '44:f7:9f:d1:99:f3',
    '2026-05-09', '2026-06-03', '2026-08-03', NULL,
    'Departamento Financeiro e Contabilidade', 'Departamento Financeiro', 'EM_USO', 'OTIMO',
    NULL, NULL, '', 0,
    '2026-06-03T11:12:20', '2026-08-03T11:28:37', 'REMOTE-IMPORT', 0
);

-- ---------- 150 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'CMP-DEL-ESS-017', 'Dcl_03', 'Agri-Dcl-Dell-00-04-A', '44:f7:9f:d1:9a:35',
    '2026-05-09', '2026-06-05', '2026-08-03', NULL,
    'Sala de contabilidade Escritorio plantação', 'Departamento Compras e Logística', 'EM_USO', 'OTIMO',
    NULL, NULL, '', 0,
    '2026-06-05T09:22:48', '2026-08-03T11:28:18', 'REMOTE-IMPORT', 0
);

-- ---------- 151 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'MON-DEL-E24-004', 'Drh_01', 'Agri-Dcl-Dell-00-03-A', NULL,
    '2026-05-29', '2026-06-05', '2026-08-03', NULL,
    'Sala da contabilidade escritorio plantação', 'Departamento Compras e Logística', 'EM_USO', 'OTIMO',
    NULL, NULL, '', 0,
    '2026-06-05T10:05:27', '2026-08-03T11:27:56', 'REMOTE-IMPORT', 0
);

-- ---------- 152 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'TCD-LOG-MXK-002', 'Drh_01', 'Agri-Dcl-Logi-00-01-A', NULL,
    '2026-04-24', '2026-06-05', '2026-08-03', NULL,
    'Sala da contabilidade escritorio plantação', 'Departamento Compras e Logística', 'EM_USO', 'OTIMO',
    NULL, NULL, '', 0,
    '2026-06-05T10:08:57', '2026-08-03T11:27:27', 'REMOTE-IMPORT', 0
);

-- ---------- 153 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'MSE-LOG-MXA-001', 'Dcl_03', 'Agri-Dcl-Logi-00-00-A', NULL,
    '2026-04-24', '2026-06-05', '2026-08-03', NULL,
    'Sala de contabilidade Escritorio plantação', 'Departamento Compras e Logística', 'EM_USO', 'BOM',
    NULL, NULL, '', 0,
    '2026-06-05T10:35:53', '2026-08-03T11:19:47', 'REMOTE-IMPORT', 0
);

-- ---------- 154 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'MON-DEL-E24-004', 'Daf_09', 'Agri-Daf-Dell-00-09-A', NULL,
    '2026-05-29', '2026-06-05', '2026-08-03', NULL,
    'Sala de tesouraria do escritorio plantação', 'Departamento Financeiro', 'EM_USO', 'OTIMO',
    NULL, NULL, '', 0,
    '2026-06-05T10:50:44', '2026-08-03T11:24:19', 'REMOTE-IMPORT', 0
);

-- ---------- 155 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'MON-AOC-M24-006', 'Amz_02', 'Agri-Azm_Aoc_00-01-A', NULL,
    '2026-06-09', '2026-06-09', '2026-08-03', NULL,
    'Armazem Geral', 'Departamento Compras e Logística', 'EM_USO', 'BOM',
    NULL, NULL, '', 0,
    '2026-06-09T10:39:35', '2026-08-03T11:26:03', 'REMOTE-IMPORT', 0
);

-- ---------- 156 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'CMP-DEL-ESS-017', 'Dinfo_001', 'Agri-Dinfo-Dell-00-00-A', NULL,
    '2026-05-09', '2026-07-09', '2026-07-09', NULL,
    'Sala de Informática (POM)', 'Departamento Informático', 'EM_USO', 'OTIMO',
    NULL, NULL, '', 0,
    '2026-07-09T12:39:47', '2026-07-09T12:39:47', 'REMOTE-IMPORT', 0
);

-- ---------- 157 ----------
INSERT OR IGNORE INTO inventario_equipamentos (
    id, sku_produto, funcionario_id, num_serie, endereco_mac,
    data_aquisicao, data_instalacao, data_ultima_verificacao,
    numero_fatura, localizacao, departamento, status, condicao,
    documento_entrega, documento_devolucao, observacoes, devolucao,
    created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'CMP-HPX-15D-001', 'Dinfo_001', 'Agri-Pom-Dell-00-03-A', NULL,
    '2026-04-24', '2026-07-09', '2026-08-03', '14',
    'Sala do dep informatico', 'Departamento Informático', 'EM_USO', 'BOM',
    NULL, NULL, '', 0,
    '2026-07-09T15:07:10', '2026-08-03T15:29:26', 'REMOTE-IMPORT', 0
);



-- ============================================================
-- Verificação
-- ============================================================
-- SELECT COUNT(*) FROM inventario_equipamentos;
-- SELECT COUNT(*) FROM inventario_equipamentos WHERE funcionario_id IS NULL;
-- SELECT sku_produto, COUNT(*) FROM inventario_equipamentos GROUP BY sku_produto;