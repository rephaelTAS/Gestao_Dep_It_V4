-- ============================================================
-- Bootstrap HISTORICO_EVENTOS — dump real (286 registros)
-- Período: 2026-04-24 → 2026-08-11
-- IDs determinísticos HE-0001..HE-0286 (idempotente com INSERT OR IGNORE)
-- device_id = 'REMOTE-IMPORT'
-- Encoding: corrigido a partir do dump original (ïŋ― / \Uffffffff)
-- ============================================================


-- ---------- 1 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0001','CRIACAO','CMP-HPX-RTL-001','CND2472S79','Amz_02',
 '{"id":"Amz_02","acao":"Importação de equipamento via CSV","nome":"Carlos César","data_hora":"2026-04-24T14:39:58.215875600","departamento":"Departamento de Armazém"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"BOM","num_serie":"CND2472S79","localizacao":"Armazém Geral","sku_produto":"CMP-HPX-RTL-001","departamento":"Departamento de Armazém"}',
 '2026-04-24 14:39:58','REMOTE-IMPORT');

-- ---------- 2 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0002','CRIACAO','IMP-HPX-COL-001','VNC4131775','Amz_02',
 '{"id":"Amz_02","acao":"Importação de equipamento via CSV","nome":"Carlos César","data_hora":"2026-04-24T14:39:58.249413700","departamento":"Departamento de Armazém"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"BOM","num_serie":"VNC4131775","localizacao":"Armazém Geral","sku_produto":"IMP-HPX-COL-001","departamento":"Departamento de Armazém"}',
 '2026-04-24 14:39:58','REMOTE-IMPORT');

-- ---------- 3 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0003','CRIACAO','MSE-LOG-MXA-001','2419LZ97BTQ8','Amz_02',
 '{"id":"Amz_02","acao":"Importação de equipamento via CSV","nome":"Carlos César","data_hora":"2026-04-24T14:39:58.284431100","departamento":"Departamento de Armazém"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"OTIMO","num_serie":"2419LZ97BTQ8","localizacao":"Armazém Geral","sku_produto":"MSE-LOG-MXA-001","departamento":"Departamento de Armazém"}',
 '2026-04-24 14:39:58','REMOTE-IMPORT');

-- ---------- 4 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0004','CRIACAO','CMP-DEL-VES-001','C86FFL2','Amz_04',
 '{"id":"Amz_04","acao":"Importação de equipamento via CSV","nome":"Flora Andrande","data_hora":"2026-04-24T14:39:58.321474200","departamento":"Departamento de Armazém"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"CRITICO","num_serie":"C86FFL2","localizacao":"Armazém Geral","observacoes":"A tampa do computador esta partida, e no teclado","sku_produto":"CMP-DEL-VES-001","departamento":"Departamento de Armazém"}',
 '2026-04-24 14:39:58','REMOTE-IMPORT');

-- ---------- 5 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0005','CRIACAO','CMP-DEL-VOS-001','G76FFL2','Daf_01',
 '{"id":"Daf_01","acao":"Importação de equipamento via CSV","nome":"Gonçalo Vieira da Costa","data_hora":"2026-04-24T14:39:58.347447200","departamento":"Departamento Financeiro"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"BOM","num_serie":"G76FFL2","localizacao":"Sala da Direção Financeira","sku_produto":"CMP-DEL-VOS-001","departamento":"Departamento Financeiro"}',
 '2026-04-24 14:39:58','REMOTE-IMPORT');

-- ---------- 6 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0006','CRIACAO','MON-DEL-U24-001','CN-0YPPY0-tv100-8b7-15xv-a10','Daf_01',
 '{"id":"Daf_01","acao":"Importação de equipamento via CSV","nome":"Gonçalo Vieira da Costa","data_hora":"2026-04-24T14:39:58.390981200","departamento":"Departamento Financeiro"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"BOM","num_serie":"CN-0YPPY0-tv100-8b7-15xv-a10","localizacao":"Sala da Direção Financeira","sku_produto":"MON-DEL-U24-001","departamento":"Departamento Financeiro"}',
 '2026-04-24 14:39:58','REMOTE-IMPORT');

-- ---------- 7 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0007','CRIACAO','TCD-LOG-MXK-001','2019D17096','Daf_01',
 '{"id":"Daf_01","acao":"Importação de equipamento via CSV","nome":"Gonçalo Vieira da Costa","data_hora":"2026-04-24T14:39:58.412979400","departamento":"Departamento Financeiro"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"OTIMO","num_serie":"2019D17096","localizacao":"Sala da Direção Financeira","sku_produto":"TCD-LOG-MXK-001","departamento":"Departamento Financeiro"}',
 '2026-04-24 14:39:58','REMOTE-IMPORT');

-- ---------- 8 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0008','CRIACAO','CMP-THI-LEN-001','CIDF15000051','Daf_02',
 '{"id":"Daf_02","acao":"Importação de equipamento via CSV","nome":"Amilcar Bom Jesus","data_hora":"2026-04-24T14:39:58.445988900","departamento":"Departamento Financeiro"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"BOM","num_serie":"CIDF15000051","localizacao":"Departamento Financeiro e Contabilidade","sku_produto":"CMP-THI-LEN-001","departamento":"Departamento Financeiro"}',
 '2026-04-24 14:39:58','REMOTE-IMPORT');

-- ---------- 9 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0009','CRIACAO','MSE-DEL-MS1-001','cn-0jd7xg-l0300-85h-0ne4','Daf_02',
 '{"id":"Daf_02","acao":"Importação de equipamento via CSV","nome":"Amilcar Bom Jesus","data_hora":"2026-04-24T14:39:58.478544400","departamento":"Departamento Financeiro"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"BOM","num_serie":"cn-0jd7xg-l0300-85h-0ne4","localizacao":"Departamento Financeiro e Contabilidade","sku_produto":"MSE-DEL-MS1-001","departamento":"Departamento Financeiro"}',
 '2026-04-24 14:39:58','REMOTE-IMPORT');

-- ---------- 10 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0010','CRIACAO','CPU-DEL-OPT-001','2763312518','Daf_02',
 '{"id":"Daf_02","acao":"Importação de equipamento via CSV","nome":"Amilcar Bom Jesus","data_hora":"2026-04-24T14:39:58.503549600","departamento":"Departamento Financeiro"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"BOM","num_serie":"2763312518","localizacao":"Departamento Financeiro e Contabilidade","sku_produto":"CPU-DEL-OPT-001","departamento":"Departamento Financeiro"}',
 '2026-04-24 14:39:58','REMOTE-IMPORT');

-- ---------- 11 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0011','CRIACAO','MON-DEL-GYB-001','CN-01M2XX-QDC00-874-3841-A05','Daf_02',
 '{"id":"Daf_02","acao":"Importação de equipamento via CSV","nome":"Amilcar Bom Jesus","data_hora":"2026-04-24T14:39:58.528566900","departamento":"Departamento Financeiro"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"BOM","num_serie":"CN-01M2XX-QDC00-874-3841-A05","localizacao":"Departamento Financeiro e Contabilidade","sku_produto":"MON-DEL-GYB-001","departamento":"Departamento Financeiro"}',
 '2026-04-24 14:39:58','REMOTE-IMPORT');

-- ---------- 12 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0012','CRIACAO','MSE-EQU-NO2-001',NULL,'Daf_02',
 '{"id":"Daf_02","acao":"Importação de equipamento via CSV","nome":"Amilcar Bom Jesus","data_hora":"2026-04-24T14:39:58.547580900","departamento":"Departamento Financeiro"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"CRITICO","localizacao":"Departamento Financeiro e Contabilidade","observacoes":"Rato sencivel","sku_produto":"MSE-EQU-NO2-001","departamento":"Departamento Financeiro"}',
 '2026-04-24 14:39:58','REMOTE-IMPORT');

-- ---------- 13 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0013','CRIACAO','TCD-LOG-MXK-001','2019D17095','Daf_02',
 '{"id":"Daf_02","acao":"Importação de equipamento via CSV","nome":"Amilcar Bom Jesus","data_hora":"2026-04-24T14:39:58.573132400","departamento":"Departamento Financeiro"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"OTIMO","num_serie":"2019D17095","localizacao":"Departamento Financeiro e Contabilidade","sku_produto":"TCD-LOG-MXK-001","departamento":"Departamento Financeiro"}',
 '2026-04-24 14:39:58','REMOTE-IMPORT');

-- ---------- 14 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0014','CRIACAO','MSE-LOG-MXA-001','2417lz02q8m8','Daf_02',
 '{"id":"Daf_02","acao":"Importação de equipamento via CSV","nome":"Amilcar Bom Jesus","data_hora":"2026-04-24T14:39:58.592134400","departamento":"Departamento Financeiro"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"OTIMO","num_serie":"2417lz02q8m8","localizacao":"Departamento Financeiro e Contabilidade","sku_produto":"MSE-LOG-MXA-001","departamento":"Departamento Financeiro"}',
 '2026-04-24 14:39:58','REMOTE-IMPORT');

-- ---------- 15 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0015','CRIACAO','MSE-LOG-MXA-001','2405LZ034YV8','Daf_02',
 '{"id":"Daf_02","acao":"Importação de equipamento via CSV","nome":"Amilcar Bom Jesus","data_hora":"2026-04-24T14:39:58.603132300","departamento":"Departamento Financeiro"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"OTIMO","num_serie":"2405LZ034YV8","localizacao":"Departamento Financeiro e Contabilidade","sku_produto":"MSE-LOG-MXA-001","departamento":"Departamento Financeiro"}',
 '2026-04-24 14:39:58','REMOTE-IMPORT');

-- ---------- 16 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0016','CRIACAO','CMP-HPX-HP2-001','cnd42027bh','Daf_02',
 '{"id":"Daf_02","acao":"Importação de equipamento via CSV","nome":"Amilcar Bom Jesus","data_hora":"2026-04-24T14:39:58.620189400","departamento":"Departamento Financeiro"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"BOM","num_serie":"cnd42027bh","localizacao":"Departamento Financeiro e Contabilidade","sku_produto":"CMP-HPX-HP2-001","departamento":"Departamento Financeiro"}',
 '2026-04-24 14:39:58','REMOTE-IMPORT');

-- ---------- 17 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0017','CRIACAO','MSE-EQU-NO2-001',NULL,'Daf_02',
 '{"id":"Daf_02","acao":"Importação de equipamento via CSV","nome":"Amilcar Bom Jesus","data_hora":"2026-04-24T14:39:58.673358900","departamento":"Departamento Financeiro"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"CRITICO","localizacao":"Departamento Financeiro e Contabilidade","observacoes":"Rato sencivel","sku_produto":"MSE-EQU-NO2-001","departamento":"Departamento Financeiro"}',
 '2026-04-24 14:39:58','REMOTE-IMPORT');

-- ---------- 18 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0018','CRIACAO','CPU-DEL-D18-001','J72GGA01WCD','Daf_04',
 '{"id":"Daf_04","acao":"Importação de equipamento via CSV","nome":"Odila Santo Martins","data_hora":"2026-04-24T14:39:58.709349600","departamento":"Departamento Financeiro"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"BOM","num_serie":"J72GGA01WCD","localizacao":"Departamento Financeiro e Contabilidade","sku_produto":"CPU-DEL-D18-001","departamento":"Departamento Financeiro"}',
 '2026-04-24 14:39:58','REMOTE-IMPORT');

-- ---------- 19 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0019','CRIACAO','MSE-COM-WIR-001',NULL,'Daf_04',
 '{"id":"Daf_04","acao":"Importação de equipamento via CSV","nome":"Odila Santo Martins","data_hora":"2026-04-24T14:39:58.724352500","departamento":"Departamento Financeiro"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"BOM","localizacao":"Departamento Financeiro e Contabilidade","sku_produto":"MSE-COM-WIR-001","departamento":"Departamento Financeiro"}',
 '2026-04-24 14:39:58','REMOTE-IMPORT');

-- ---------- 20 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0020','CRIACAO','TCD-EQU-NO2-001',NULL,'Daf_04',
 '{"id":"Daf_04","acao":"Importação de equipamento via CSV","nome":"Odila Santo Martins","data_hora":"2026-04-24T14:39:58.735356900","departamento":"Departamento Financeiro"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"BOM","localizacao":"Departamento Financeiro e Contabilidade","sku_produto":"TCD-EQU-NO2-001","departamento":"Departamento Financeiro"}',
 '2026-04-24 14:39:58','REMOTE-IMPORT');

-- ---------- 21 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0021','CRIACAO','TCD-LOG-MXK-001','2019D17094','Daf_04',
 '{"id":"Daf_04","acao":"Importação de equipamento via CSV","nome":"Odila Santo Martins","data_hora":"2026-04-24T14:39:58.745366000","departamento":"Departamento Financeiro"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"OTIMO","num_serie":"2019D17094","localizacao":"Departamento Financeiro e Contabilidade","sku_produto":"TCD-LOG-MXK-001","departamento":"Departamento Financeiro"}',
 '2026-04-24 14:39:58','REMOTE-IMPORT');

-- ---------- 22 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0022','CRIACAO','TEL-SAN-GAL-001','SM-A057G/DSN','Daf_06',
 '{"id":"Daf_06","acao":"Importação de equipamento via CSV","nome":"Basílio Espírito Santo","data_hora":"2026-04-24T14:39:58.773466700","departamento":"Departamento Financeiro"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"OTIMO","num_serie":"SM-A057G/DSN","localizacao":"Departamento Financeiro e Contabilidade","sku_produto":"TEL-SAN-GAL-001","departamento":"Departamento Financeiro"}',
 '2026-04-24 14:39:58','REMOTE-IMPORT');

-- ---------- 23 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0023','CRIACAO','MSE-LOG-MXA-001','2419LZ07B4T8','Daf_06',
 '{"id":"Daf_06","acao":"Importação de equipamento via CSV","nome":"Basílio Espírito Santo","data_hora":"2026-04-24T14:39:58.785461500","departamento":"Departamento Financeiro"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"OTIMO","num_serie":"2419LZ07B4T8","localizacao":"Departamento Financeiro e Contabilidade","sku_produto":"MSE-LOG-MXA-001","departamento":"Departamento Financeiro"}',
 '2026-04-24 14:39:58','REMOTE-IMPORT');

-- ---------- 24 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0024','CRIACAO','CMP-THI-CAR-001','TP00129C','Dcl_01',
 '{"id":"Dcl_01","acao":"Importação de equipamento via CSV","nome":"Paulo Cesar","data_hora":"2026-04-24T14:39:58.808461700","departamento":"Departamento Comercial"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"BOM","num_serie":"TP00129C","localizacao":"Dloretor de Compras","sku_produto":"CMP-THI-CAR-001","departamento":"Departamento Comercial"}',
 '2026-04-24 14:39:58','REMOTE-IMPORT');

-- ---------- 25 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0025','CRIACAO','CMP-HPX-15D-001','5CD3322SQ5','Dcl_01',
 '{"id":"Dcl_01","acao":"Importação de equipamento via CSV","nome":"Paulo Cesar","data_hora":"2026-04-24T14:39:58.825459600","departamento":"Departamento Comercial"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"BOM","num_serie":"5CD3322SQ5","localizacao":"Dloretor de Compras","sku_produto":"CMP-HPX-15D-001","departamento":"Departamento Comercial"}',
 '2026-04-24 14:39:58','REMOTE-IMPORT');

-- ---------- 26 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0026','CRIACAO','CMP-HPX-15D-001','5CD3321WXP','Dcl_02',
 '{"id":"Dcl_02","acao":"Importação de equipamento via CSV","nome":"Baltzer dos Santos Varela","data_hora":"2026-04-24T14:39:58.836485900","departamento":"Departamento Financeiro"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"BOM","num_serie":"5CD3321WXP","localizacao":"Departamento Financeiro e Contabilidade","sku_produto":"CMP-HPX-15D-001","departamento":"Departamento Financeiro"}',
 '2026-04-24 14:39:58','REMOTE-IMPORT');

-- ---------- 27 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0027','CRIACAO','MSE-LOG-MXA-001','2417LZ02PRE8','Dcl_02',
 '{"id":"Dcl_02","acao":"Importação de equipamento via CSV","nome":"Baltzer dos Santos Varela","data_hora":"2026-04-24T14:39:58.847480300","departamento":"Departamento Financeiro"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"OTIMO","num_serie":"2417LZ02PRE8","localizacao":"Departamento Financeiro e Contabilidade","sku_produto":"MSE-LOG-MXA-001","departamento":"Departamento Financeiro"}',
 '2026-04-24 14:39:58','REMOTE-IMPORT');

-- ---------- 28 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0028','CRIACAO','MON-HPX-HPP-001','3CM22608MW','Dge_01',
 '{"id":"Dge_01","acao":"Importação de equipamento via CSV","nome":"Nicolas Bergerot","data_hora":"2026-04-24T14:39:58.863930500","departamento":"Direção Geral"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"BOM","num_serie":"3CM22608MW","localizacao":"Sala da Direção Geral","sku_produto":"MON-HPX-HPP-001","departamento":"Direção Geral"}',
 '2026-04-24 14:39:58','REMOTE-IMPORT');

-- ---------- 29 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0029','CRIACAO','CMP-HPX-INT-001',NULL,'Dge_01',
 '{"id":"Dge_01","acao":"Importação de equipamento via CSV","nome":"Nicolas Bergerot","data_hora":"2026-04-24T14:39:58.875916400","departamento":"Direção Geral"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"BOM","localizacao":"Sala da Direção Geral","sku_produto":"CMP-HPX-INT-001","departamento":"Direção Geral"}',
 '2026-04-24 14:39:58','REMOTE-IMPORT');

-- ---------- 30 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0030','CRIACAO','MON-SAM-S27-001','ZZN9H4ZM903417F','Dge_01',
 '{"id":"Dge_01","acao":"Importação de equipamento via CSV","nome":"Nicolas Bergerot","data_hora":"2026-04-24T14:39:58.891912800","departamento":"Direção Geral"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"BOM","num_serie":"ZZN9H4ZM903417F","localizacao":"Sala da Direção Geral","sku_produto":"MON-SAM-S27-001","departamento":"Direção Geral"}',
 '2026-04-24 14:39:58','REMOTE-IMPORT');

-- ---------- 31 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0031','CRIACAO','CMP-HPX-TPN-001','5CG4120JN6','Djr_01',
 '{"id":"Djr_01","acao":"Importação de equipamento via CSV","nome":"Herlander Pinto","data_hora":"2026-04-24T14:39:58.911923500","departamento":"Departamento Jurídico"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"OTIMO","num_serie":"5CG4120JN6","localizacao":"Sala do Responsável Jurídico","sku_produto":"CMP-HPX-TPN-001","departamento":"Departamento Jurídico"}',
 '2026-04-24 14:39:58','REMOTE-IMPORT');

-- ---------- 32 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0032','CRIACAO','MSE-LOG-MXA-001','2405LZ0351H8','Djr_01',
 '{"id":"Djr_01","acao":"Importação de equipamento via CSV","nome":"Herlander Pinto","data_hora":"2026-04-24T14:39:58.924935500","departamento":"Departamento Jurídico"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"OTIMO","num_serie":"2405LZ0351H8","localizacao":"Sala do Responsável Jurídico","sku_produto":"MSE-LOG-MXA-001","departamento":"Departamento Jurídico"}',
 '2026-04-24 14:39:58','REMOTE-IMPORT');

-- ---------- 33 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0033','CRIACAO','CMP-THI-LEN-001',NULL,'Dpl_01',
 '{"id":"Dpl_01","acao":"Importação de equipamento via CSV","nome":"Laurent Pillon","data_hora":"2026-04-24T14:39:58.937939200","departamento":"Departamento de Plantação"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"BOM","localizacao":"Sala da Direção de Plantação","sku_produto":"CMP-THI-LEN-001","departamento":"Departamento de Plantação"}',
 '2026-04-24 14:39:58','REMOTE-IMPORT');

-- ---------- 34 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0034','CRIACAO','MON-DEL-E22-001','cn-0j25p8','Dpl_01',
 '{"id":"Dpl_01","acao":"Importação de equipamento via CSV","nome":"Laurent Pillon","data_hora":"2026-04-24T14:39:58.947950500","departamento":"Departamento de Plantação"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"BOM","num_serie":"cn-0j25p8","localizacao":"Sala da Direção de Plantação","sku_produto":"MON-DEL-E22-001","departamento":"Departamento de Plantação"}',
 '2026-04-24 14:39:58','REMOTE-IMPORT');

-- ---------- 35 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0035','CRIACAO','IMP-CAN-PIX-001','afss03656','Dpl_01',
 '{"id":"Dpl_01","acao":"Importação de equipamento via CSV","nome":"Laurent Pillon","data_hora":"2026-04-24T14:39:58.963341900","departamento":"Departamento de Plantação"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"BOM","num_serie":"afss03656","localizacao":"Sala da Direção de Plantação","sku_produto":"IMP-CAN-PIX-001","departamento":"Departamento de Plantação"}',
 '2026-04-24 14:39:58','REMOTE-IMPORT');

-- ---------- 36 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0036','CRIACAO','CMP-TOS-PYS-001','Z108123H','Dpl_02',
 '{"id":"Dpl_02","acao":"Importação de equipamento via CSV","nome":"Ludger Magalhães","data_hora":"2026-04-24T14:39:58.978368100","departamento":"Departamento de Plantação"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"OTIMO","num_serie":"Z108123H","localizacao":"Sala de Plantação","sku_produto":"CMP-TOS-PYS-001","departamento":"Departamento de Plantação"}',
 '2026-04-24 14:39:58','REMOTE-IMPORT');

-- ---------- 37 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0037','CRIACAO','CMP-HPX-15D-001','5CD33389FC','Dpl_03',
 '{"id":"Dpl_03","acao":"Importação de equipamento via CSV","nome":"Tatiana Da Silva","data_hora":"2026-04-24T14:39:58.994343900","departamento":"Departamento de Plantação"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"OTIMO","num_serie":"5CD33389FC","localizacao":"Sala de Plantação","sku_produto":"CMP-HPX-15D-001","departamento":"Departamento de Plantação"}',
 '2026-04-24 14:39:58','REMOTE-IMPORT');

-- ---------- 38 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0038','CRIACAO','CMP-DEL-INT-001','9XXC8 A00','Drc_01',
 '{"id":"Drc_01","acao":"Importação de equipamento via CSV","nome":"Sopheap Kol","data_hora":"2026-04-24T14:39:59.007347300","departamento":"Departamento de Comunicação"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"BOM","num_serie":"9XXC8 A00","localizacao":"0.0","sku_produto":"CMP-DEL-INT-001","departamento":"0.0"}',
 '2026-04-24 14:39:59','REMOTE-IMPORT');

-- ---------- 39 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0039','CRIACAO','CMP-LEN-PF9-001','PF3CYL7H','Drh_01',
 '{"id":"Drh_01","acao":"Importação de equipamento via CSV","nome":"Carlos Matos","data_hora":"2026-04-24T14:39:59.027350300","departamento":"Departamento de RH"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"OTIMO","num_serie":"PF3CYL7H","localizacao":"Sala do Responsável de RH","sku_produto":"CMP-LEN-PF9-001","departamento":"Departamento de RH"}',
 '2026-04-24 14:39:59','REMOTE-IMPORT');

-- ---------- 40 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0040','CRIACAO','CMP-HPX-W4M-001','PD93165NG','Drh_02',
 '{"id":"Drh_02","acao":"Importação de equipamento via CSV","nome":"Sizilene","data_hora":"2026-04-24T14:39:59.041369300","departamento":"Departamento de RH"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"CRITICO","num_serie":"PD93165NG","localizacao":"Sala Comum Escritorio Geral","observacoes":"Problema no acumulo de carga (bateria)","sku_produto":"CMP-HPX-W4M-001","departamento":"Departamento de RH"}',
 '2026-04-24 14:39:59','REMOTE-IMPORT');

-- ---------- 41 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0041','CRIACAO','CMP-HPX-15D-001','5CD3338JT4','Drh_04',
 '{"id":"Drh_04","acao":"Importação de equipamento via CSV","nome":"Niclay Augusto","data_hora":"2026-04-24T14:39:59.062944500","departamento":"Departamento de RH"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"OTIMO","num_serie":"5CD3338JT4","localizacao":"Sala Comum Escritorio Geral","sku_produto":"CMP-HPX-15D-001","departamento":"Departamento de RH"}',
 '2026-04-24 14:39:59','REMOTE-IMPORT');

-- ---------- 42 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0042','CRIACAO','MSE-LOG-MXA-001','2417LZ02Q9Z8','Drh_04',
 '{"id":"Drh_04","acao":"Importação de equipamento via CSV","nome":"Niclay Augusto","data_hora":"2026-04-24T14:39:59.073971700","departamento":"Departamento de RH"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"OTIMO","num_serie":"2417LZ02Q9Z8","localizacao":"Sala Comum Escritorio Geral","sku_produto":"MSE-LOG-MXA-001","departamento":"Departamento de RH"}',
 '2026-04-24 14:39:59','REMOTE-IMPORT');

-- ---------- 43 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0043','CRIACAO','CPU-DEL-D18-001','J72GGAO1WCD','Hsa_03',
 '{"id":"Hsa_03","acao":"Importação de equipamento via CSV","nome":"Braylesson Monteiro","data_hora":"2026-04-24T14:39:59.107946500","departamento":"Departamento de HSA"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"CRITICO","num_serie":"J72GGAO1WCD","localizacao":"Departamento de Higiene e Segurança","observacoes":"Problemas na fonte de alimentação","sku_produto":"CPU-DEL-D18-001","departamento":"Departamento de HSA"}',
 '2026-04-24 14:39:59','REMOTE-IMPORT');

-- ---------- 44 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0044','CRIACAO','MON-DEL-CN0-001','10C1KV2','Hsa_03',
 '{"id":"Hsa_03","acao":"Importação de equipamento via CSV","nome":"Braylesson Monteiro","data_hora":"2026-04-24T14:39:59.115967600","departamento":"Departamento de HSA"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"CRITICO","num_serie":"10C1KV2","localizacao":"Departamento de Higiene e Segurança","observacoes":"Linhas coloridas no visor","sku_produto":"MON-DEL-CN0-001","departamento":"Departamento de HSA"}',
 '2026-04-24 14:39:59','REMOTE-IMPORT');

-- ---------- 45 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0045','CRIACAO','CMP-HPX-RTL-001','CND2472S7W','Inf_01',
 '{"id":"Inf_01","acao":"Importação de equipamento via CSV","nome":"Helder Leitão","data_hora":"2026-04-24T14:39:59.126953700","departamento":"Departamento de Infraestruturas"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"BOM","num_serie":"CND2472S7W","localizacao":"Sala do Responsável de Infraestruturas","sku_produto":"CMP-HPX-RTL-001","departamento":"Departamento de Infraestruturas"}',
 '2026-04-24 14:39:59','REMOTE-IMPORT');

-- ---------- 46 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0046','CRIACAO','IMP-HPX-COL-002','CNBRQDDBSJ','Inf_02',
 '{"id":"Inf_02","acao":"Importação de equipamento via CSV","nome":"Durdiley Guine","data_hora":"2026-04-24T14:39:59.139985200","departamento":"Departamento de Infraestruturas"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"BOM","num_serie":"CNBRQDDBSJ","localizacao":"Sala da infraestrutura","sku_produto":"IMP-HPX-COL-002","departamento":"Departamento de Infraestruturas"}',
 '2026-04-24 14:39:59','REMOTE-IMPORT');

-- ---------- 47 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0047','CRIACAO','IMP-HPX-HPD-001',NULL,'Inf_02',
 '{"id":"Inf_02","acao":"Importação de equipamento via CSV","nome":"Durdiley Guine","data_hora":"2026-04-24T14:39:59.151504600","departamento":"Departamento de Infraestruturas"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"BOM","localizacao":"Sala da infraestrutura","sku_produto":"IMP-HPX-HPD-001","departamento":"Departamento de Infraestruturas"}',
 '2026-04-24 14:39:59','REMOTE-IMPORT');

-- ---------- 48 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0048','CRIACAO','IMP-BRO-DCP-001','E78284L1N890853','Ofi_02',
 '{"id":"Ofi_02","acao":"Importação de equipamento via CSV","nome":"Rail dos Reis","data_hora":"2026-04-24T14:39:59.174505300","departamento":"Departamento de Oficina"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"BOM","num_serie":"E78284L1N890853","localizacao":"Sala do Responsável de Oficina","sku_produto":"IMP-BRO-DCP-001","departamento":"Departamento de Oficina"}',
 '2026-04-24 14:39:59','REMOTE-IMPORT');

-- ---------- 49 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0049','CRIACAO','CMP-HPX-15D-001','5CD3304ZCL','Pom_016',
 '{"id":"Pom_01","acao":"Importação de equipamento via CSV","nome":"Rafael Tavares","data_hora":"2026-04-24T14:39:59.184517800","departamento":"Departamento de IT"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"OTIMO","num_serie":"5CD3304ZCL","localizacao":"Sala de Informática (POM)","sku_produto":"CMP-HPX-15D-001","departamento":"Departamento de IT"}',
 '2026-04-24 14:39:59','REMOTE-IMPORT');

-- ---------- 50 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0050','CRIACAO','IMP-HPX-COL-002','CNBRQDDB1M','Pom_016',
 '{"id":"Pom_01","acao":"Importação de equipamento via CSV","nome":"Rafael Tavares","data_hora":"2026-04-24T14:39:59.196507500","departamento":"Departamento de IT"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"BOM","num_serie":"CNBRQDDB1M","localizacao":"Sala de Informática (POM)","sku_produto":"IMP-HPX-COL-002","departamento":"Departamento de IT"}',
 '2026-04-24 14:39:59','REMOTE-IMPORT');

-- ---------- 51 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0051','CRIACAO','MON-VIE-VS1-001','VA2215-H','Pom_016',
 '{"id":"Pom_01","acao":"Importação de equipamento via CSV","nome":"Rafael Tavares","data_hora":"2026-04-24T14:39:59.209508700","departamento":"Departamento de IT"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"BOM","num_serie":"VA2215-H","localizacao":"Sala de Informática (POM)","sku_produto":"MON-VIE-VS1-001","departamento":"Departamento de IT"}',
 '2026-04-24 14:39:59','REMOTE-IMPORT');

-- ---------- 52 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0052','CRIACAO','CMP-DEL-VOS-001','D76FFL2','Pom_02',
 '{"id":"Pom_02","acao":"Importação de equipamento via CSV","nome":"Pedro dos Santos","data_hora":"2026-04-24T14:39:59.216524400","departamento":"Departamento Industrial (POM)"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"CRITICO","num_serie":"D76FFL2","localizacao":"Sala da Direção Industrial (POM)","observacoes":"Teclas não funcionam","sku_produto":"CMP-DEL-VOS-001","departamento":"Departamento Industrial (POM)"}',
 '2026-04-24 14:39:59','REMOTE-IMPORT');

-- ---------- 53 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0053','CRIACAO','UPS-PHA-P1B-001','B-85579282','Pom_02',
 '{"id":"Pom_02","acao":"Importação de equipamento via CSV","nome":"Pedro dos Santos","data_hora":"2026-04-24T14:39:59.230504700","departamento":"Departamento Industrial (POM)"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"BOM","num_serie":"B-85579282","localizacao":"Sala da Direção Industrial (POM)","sku_produto":"UPS-PHA-P1B-001","departamento":"Departamento Industrial (POM)"}',
 '2026-04-24 14:39:59','REMOTE-IMPORT');

-- ---------- 54 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0054','CRIACAO','MON-LGX-24M-001','603NTZN3X229','Pom_02',
 '{"id":"Pom_02","acao":"Importação de equipamento via CSV","nome":"Pedro dos Santos","data_hora":"2026-04-24T14:39:59.242527600","departamento":"Departamento Industrial (POM)"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"BOM","num_serie":"603NTZN3X229","localizacao":"Sala da Direção Industrial (POM)","sku_produto":"MON-LGX-24M-001","departamento":"Departamento Industrial (POM)"}',
 '2026-04-24 14:39:59','REMOTE-IMPORT');

-- ---------- 55 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0055','CRIACAO','CPU-DEL-OPT-001','2521447814','Pom_03',
 '{"id":"Pom_03","acao":"Importação de equipamento via CSV","nome":"Kleyton","data_hora":"2026-04-24T14:39:59.261079700","departamento":"Departamento Industrial (POM)"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"BOM","num_serie":"2521447814","localizacao":"Balança (POM)","sku_produto":"CPU-DEL-OPT-001","departamento":"Departamento Industrial (POM)"}',
 '2026-04-24 14:39:59','REMOTE-IMPORT');

-- ---------- 56 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0056','CRIACAO','IMP-HPX-LAS-001','PHCL115929','Pom_03',
 '{"id":"Pom_03","acao":"Importação de equipamento via CSV","nome":"Kleyton","data_hora":"2026-04-24T14:39:59.276081200","departamento":"Departamento Industrial (POM)"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"BOM","num_serie":"PHCL115929","localizacao":"Balança (POM)","sku_produto":"IMP-HPX-LAS-001","departamento":"Departamento Industrial (POM)"}',
 '2026-04-24 14:39:59','REMOTE-IMPORT');

-- ---------- 57 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0057','CRIACAO','MON-DEL-GPO-001','CN-01M2XX','Pom_03',
 '{"id":"Pom_03","acao":"Importação de equipamento via CSV","nome":"Kleyton","data_hora":"2026-04-24T14:39:59.295078500","departamento":"Departamento Industrial (POM)"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"BOM","num_serie":"CN-01M2XX","localizacao":"Balança (POM)","sku_produto":"MON-DEL-GPO-001","departamento":"Departamento Industrial (POM)"}',
 '2026-04-24 14:39:59','REMOTE-IMPORT');

-- ---------- 58 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0058','CRIACAO','TEL-SAN-GAL-001','R7AY40BYMBZ','Pom_03',
 '{"id":"Pom_03","acao":"Importação de equipamento via CSV","nome":"Kleyton","data_hora":"2026-04-24T14:39:59.306072000","departamento":"Departamento Industrial (POM)"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"OTIMO","num_serie":"R7AY40BYMBZ","localizacao":"Balança (POM)","sku_produto":"TEL-SAN-GAL-001","departamento":"Departamento Industrial (POM)"}',
 '2026-04-24 14:39:59','REMOTE-IMPORT');

-- ---------- 59 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0059','CRIACAO','MON-DEL-CN0-001',NULL,'Pom_05',
 '{"id":"Pom_05","acao":"Importação de equipamento via CSV","nome":"Martim Viegas","data_hora":"2026-04-24T14:39:59.314086500","departamento":"Departamento Industrial (POM)"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"CRITICO","localizacao":"Escritório de Manutenção (POM)","observacoes":"Linhas coloridas no visor","sku_produto":"MON-DEL-CN0-001","departamento":"Departamento Industrial (POM)"}',
 '2026-04-24 14:39:59','REMOTE-IMPORT');

-- ---------- 60 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0060','CRIACAO','CPU-DEL-D13-001','D9MNP A03 DC6','Pom_05',
 '{"id":"Pom_05","acao":"Importação de equipamento via CSV","nome":"Martim Viegas","data_hora":"2026-04-24T14:39:59.331071600","departamento":"Departamento Industrial (POM)"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"BOM","num_serie":"D9MNP A03 DC6","localizacao":"Escritório de Manutenção (POM)","sku_produto":"CPU-DEL-D13-001","departamento":"Departamento Industrial (POM)"}',
 '2026-04-24 14:39:59','REMOTE-IMPORT');

-- ---------- 61 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0061','CRIACAO','CMP-HPX-RTL-001','CND2472S6W','Pom_08',
 '{"id":"Pom_08","acao":"Importação de equipamento via CSV","nome":"Indira Conceição","data_hora":"2026-04-24T14:39:59.344099800","departamento":"Departamento Industrial (POM)"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"BOM","num_serie":"CND2472S6W","localizacao":"Laboratório (POM)","sku_produto":"CMP-HPX-RTL-001","departamento":"Departamento Industrial (POM)"}',
 '2026-04-24 14:39:59','REMOTE-IMPORT');

-- ---------- 62 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0062','CRIACAO','MON-PHI-223-001','UK0A2245004956','Pom_08',
 '{"id":"Pom_08","acao":"Importação de equipamento via CSV","nome":"Indira Conceição","data_hora":"2026-04-24T14:39:59.367726900","departamento":"Departamento Industrial (POM)"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"BOM","num_serie":"UK0A2245004956","localizacao":"Laboratório (POM)","sku_produto":"MON-PHI-223-001","departamento":"Departamento Industrial (POM)"}',
 '2026-04-24 14:39:59','REMOTE-IMPORT');

-- ---------- 63 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0063','CRIACAO','TEL-SAN-GAL-001','R7AY40BX9FN','Pom_08',
 '{"id":"Pom_08","acao":"Importação de equipamento via CSV","nome":"Indira Conceição","data_hora":"2026-04-24T14:39:59.379731100","departamento":"Departamento Industrial (POM)"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"OTIMO","num_serie":"R7AY40BX9FN","localizacao":"Laboratório (POM)","sku_produto":"TEL-SAN-GAL-001","departamento":"Departamento Industrial (POM)"}',
 '2026-04-24 14:39:59','REMOTE-IMPORT');

-- ---------- 64 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0064','CRIACAO','MON-SAM-S19-001','ZYGMH4LDCO5456 W','Pom_10',
 '{"id":"Pom_10","acao":"Importação de equipamento via CSV","nome":"Geroy Kiti","data_hora":"2026-04-24T14:39:59.394699500","departamento":"Departamento Industrial (POM)"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"BOM","num_serie":"ZYGMH4LDCO5456 W","localizacao":"Escritório de Produção (POM)","sku_produto":"MON-SAM-S19-001","departamento":"Departamento Industrial (POM)"}',
 '2026-04-24 14:39:59','REMOTE-IMPORT');

-- ---------- 65 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0065','CRIACAO','CPU-DEL-VES-001','CN25N8X2','Pom_10',
 '{"id":"Pom_10","acao":"Importação de equipamento via CSV","nome":"Geroy Kiti","data_hora":"2026-04-24T14:39:59.404705300","departamento":"Departamento Industrial (POM)"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"BOM","num_serie":"CN25N8X2","localizacao":"Escritório de Produção (POM)","sku_produto":"CPU-DEL-VES-001","departamento":"Departamento Industrial (POM)"}',
 '2026-04-24 14:39:59','REMOTE-IMPORT');

-- ---------- 66 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0066','CRIACAO','MON-PHI-223-002','UK0A2246004218','Pom_11',
 '{"id":"Pom_11","acao":"Importação de equipamento via CSV","nome":"Paulo Gorge","data_hora":"2026-04-24T14:39:59.424736500","departamento":"Departamento Financeiro"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"BOM","num_serie":"UK0A2246004218","localizacao":"Sala do Departamento Comercial (POM)","sku_produto":"MON-PHI-223-002","departamento":"Departamento Finenceiro"}',
 '2026-04-24 14:39:59','REMOTE-IMPORT');

-- ---------- 67 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0067','CRIACAO','TEL-SAM-GAL-001','RF8TA0RFN3E','Pom_11',
 '{"id":"Pom_11","acao":"Importação de equipamento via CSV","nome":"Paulo Gorge","data_hora":"2026-04-24T14:39:59.433736400","departamento":"Departamento Financeiro"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"BOM","num_serie":"RF8TA0RFN3E","localizacao":"Sala do Departamento Comercial (POM)","sku_produto":"TEL-SAM-GAL-001","departamento":"Departamento Finenceiro"}',
 '2026-04-24 14:39:59','REMOTE-IMPORT');

-- ---------- 68 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0068','CRIACAO','CPU-DEL-D18-001','VOVFTA00WCD','Pom_13',
 '{"id":"Pom_13","acao":"Importação de equipamento via CSV","nome":"Arnoud Veigas","data_hora":"2026-04-24T14:39:59.446713800","departamento":"Departamento Industrial (POM)"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"BOM","num_serie":"VOVFTA00WCD","localizacao":"Sala do responsavel de produção","sku_produto":"CPU-DEL-D18-001","departamento":"Departamento Industrial (POM)"}',
 '2026-04-24 14:39:59','REMOTE-IMPORT');

-- ---------- 69 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0069','CRIACAO','CMP-HPX-HP2-002','CND038244J','Pom_14',
 '{"id":"Pom_14","acao":"Importação de equipamento via CSV","nome":"Savio","data_hora":"2026-04-24T14:39:59.479275600","departamento":"Departamento Industrial (POM)"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"CRITICO","num_serie":"CND038244J","localizacao":"Recepção do Dep Industrial","observacoes":"Problemas no teclado Partes do computador esta partido","sku_produto":"CMP-HPX-HP2-002","departamento":"Departamento Industrial (POM)"}',
 '2026-04-24 14:39:59','REMOTE-IMPORT');

-- ---------- 70 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0070','CRIACAO','TCD-LOG-MXK-001',NULL,'Pom_14',
 '{"id":"Pom_14","acao":"Importação de equipamento via CSV","nome":"Savio","data_hora":"2026-04-24T14:39:59.492273600","departamento":"Departamento Industrial (POM)"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"OTIMO","localizacao":"Recepção do Dep Industrial","sku_produto":"TCD-LOG-MXK-001","departamento":"Departamento Industrial (POM)"}',
 '2026-04-24 14:39:59','REMOTE-IMPORT');

-- ---------- 71 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0071','CRIACAO','TEL-SAN-GAL-001','R7AY40BXQMY','Pom_14',
 '{"id":"Pom_14","acao":"Importação de equipamento via CSV","nome":"Savio","data_hora":"2026-04-24T14:39:59.502268800","departamento":"Departamento Industrial (POM)"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"OTIMO","num_serie":"R7AY40BXQMY","localizacao":"Recepção do Dep Industrial","sku_produto":"TEL-SAN-GAL-001","departamento":"Departamento Industrial (POM)"}',
 '2026-04-24 14:39:59','REMOTE-IMPORT');

-- ---------- 72 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0072','CRIACAO','TEL-SAN-GAL-001','R7AY31AJVXB','Pom_15',
 '{"id":"Pom_15","acao":"Importação de equipamento via CSV","nome":"Cristom Quaresma","data_hora":"2026-04-24T14:39:59.510265700","departamento":"Departamento Industrial (POM)"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"OTIMO","num_serie":"R7AY31AJVXB","localizacao":"Laboratorio Industrial","sku_produto":"TEL-SAN-GAL-001","departamento":"Departamento Industrial (POM)"}',
 '2026-04-24 14:39:59','REMOTE-IMPORT');

-- ============================================================
-- NOTA IMPORTANTE SOBRE OS REGISTROS 73..122
-- ============================================================
-- Os arquivos que você enviou SALTAM de 72 → 123. Faltam os
-- registros 73 a 122 no dump. Se existirem, me envie o trecho.

-- ---------- 123 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0123','CRIACAO','CMP-HPX-250-001','CND1014LJG','Amz_03',
 '{"id":"Amz_03","acao":"Importação de equipamento via CSV","nome":"Domingos Armando","data_hora":"2026-04-24T14:40:01.712255300","departamento":"Departamento de Armazém"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"BOM","num_serie":"CND1014LJG","localizacao":"Armazém (POM)","sku_produto":"CMP-HPX-250-001","departamento":"Departamento de Armazém","endereco_mac":"18:47:3D:D7:23:13"}',
 '2026-04-24 14:40:01','REMOTE-IMPORT');

-- ---------- 124 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0124','CRIACAO','CMP-HPX-15F-001','1h84503rts','Daf_01',
 '{"id":"Daf_01","acao":"Importação de equipamento via CSV","nome":"Gonçalo Vieira da Costa","data_hora":"2026-04-24T14:40:01.724249400","departamento":"Departamento Financeiro"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"CRITICO","num_serie":"1h84503rts","localizacao":"Sala da Direção Financeira","sku_produto":"CMP-HPX-15F-001","departamento":"Departamento Financeiro","endereco_mac":"EB:B0:C5:5C:91:F8"}',
 '2026-04-24 14:40:01','REMOTE-IMPORT');

-- ---------- 125 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0125','CRIACAO','MSE-COM-WIR-001',NULL,'Daf_04',
 '{"id":"Daf_04","acao":"Importação de equipamento via CSV","nome":"Odila Santo Martins","data_hora":"2026-04-24T14:40:01.762832800","departamento":"Departamento Financeiro"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"BOM","localizacao":"Departamento Financeiro e Contabilidade","sku_produto":"MSE-COM-WIR-001","departamento":"Departamento Financeiro"}',
 '2026-04-24 14:40:01','REMOTE-IMPORT');

-- ---------- 126 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0126','CRIACAO','TCD-EQU-NO2-001',NULL,'Daf_04',
 '{"id":"Daf_04","acao":"Importação de equipamento via CSV","nome":"Odila Santo Martins","data_hora":"2026-04-24T14:40:01.772827600","departamento":"Departamento Financeiro"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"BOM","localizacao":"Departamento Financeiro e Contabilidade","sku_produto":"TCD-EQU-NO2-001","departamento":"Departamento Financeiro"}',
 '2026-04-24 14:40:01','REMOTE-IMPORT');

-- ---------- 127 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0127','CRIACAO','CMP-HPX-INT-001',NULL,'Dge_01',
 '{"id":"Dge_01","acao":"Importação de equipamento via CSV","nome":"Nicolas Bergerot","data_hora":"2026-04-24T14:40:01.822849100","departamento":"Direção Geral"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"BOM","localizacao":"Sala da Direção Geral","sku_produto":"CMP-HPX-INT-001","departamento":"Direção Geral"}',
 '2026-04-24 14:40:01','REMOTE-IMPORT');

-- ---------- 128 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0128','CRIACAO','MON-DEL-P24-001','VN-0WYGX3-QDV00-47T-1DOL-A03','Dcl_02',
 '{"id":"Dcl_02","acao":"Importação de equipamento via CSV","nome":"Baltzer dos Santos Varela","data_hora":"2026-04-24T14:40:01.857432100","departamento":"Departamento Financeiro"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"BOM","num_serie":"VN-0WYGX3-QDV00-47T-1DOL-A03","localizacao":"Departamento Financeiro e Contabilidade","sku_produto":"MON-DEL-P24-001","departamento":"Departamento Financeiro"}',
 '2026-04-24 14:40:01','REMOTE-IMPORT');

-- ---------- 129 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0129','CRIACAO','TCD-LOG-MXK-002','2019DJ7094','Dcl_02',
 '{"id":"Dcl_02","acao":"Importação de equipamento via CSV","nome":"Baltzer dos Santos Varela","data_hora":"2026-04-24T14:40:01.864433200","departamento":"Departamento Financeiro"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"BOM","num_serie":"2019DJ7094","localizacao":"Departamento Financeiro e Contabilidade","sku_produto":"TCD-LOG-MXK-002","departamento":"Departamento Financeiro"}',
 '2026-04-24 14:40:01','REMOTE-IMPORT');

-- ---------- 130 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0130','CRIACAO','CMP-THI-LEN-001',NULL,'Dpl_01',
 '{"id":"Dpl_01","acao":"Importação de equipamento via CSV","nome":"Laurent Pillon","data_hora":"2026-04-24T14:40:01.876428700","departamento":"Departamento de Plantação"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"BOM","localizacao":"Sala da Direção de Plantação","sku_produto":"CMP-THI-LEN-001","departamento":"Departamento de Plantação"}',
 '2026-04-24 14:40:01','REMOTE-IMPORT');

-- ---------- 131 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0131','CRIACAO','CMP-ASU-F16-001','SBN0CV12586648D','Dpl_04',
 '{"id":"Dpl_04","acao":"Importação de equipamento via CSV","nome":"Cyril Evrard","data_hora":"2026-04-24T14:40:01.896435500","departamento":"Departamento de Plantação"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"OTIMO","num_serie":"SBN0CV12586648D","localizacao":"0.0","sku_produto":"CMP-ASU-F16-001","departamento":"Departamento de Plantação"}',
 '2026-04-24 14:40:01','REMOTE-IMPORT');

-- ---------- 132 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0132','CRIACAO','MSE-LOG-MXA-001','2405LZ034YP8','Drh_01',
 '{"id":"Drh_01","acao":"Importação de equipamento via CSV","nome":"Carlos Matos","data_hora":"2026-04-24T14:40:01.921431300","departamento":"Departamento de RH"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"BOM","num_serie":"2405LZ034YP8","localizacao":"Sala do Responsável de RH","sku_produto":"MSE-LOG-MXA-001","departamento":"Departamento de RH"}',
 '2026-04-24 14:40:01','REMOTE-IMPORT');

-- ---------- 133 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0133','CRIACAO','CPU-DEL-OPT-001','1M4M7L2','Drh_05',
 '{"id":"Drh_05","acao":"Importação de equipamento via CSV","nome":"Ernane Patrocinio","data_hora":"2026-04-24T14:40:01.945446900","departamento":"Departamento de RH"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"BOM","num_serie":"1M4M7L2","localizacao":"Sala do Responsável de RH","sku_produto":"CPU-DEL-OPT-001","departamento":"Departamento de RH"}',
 '2026-04-24 14:40:01','REMOTE-IMPORT');

-- ---------- 134 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0134','CRIACAO','MON-DEL-9Q8-001','CN-01M2XX - qdc00-874-1wvl-ao5','Drh_05',
 '{"id":"Drh_05","acao":"Importação de equipamento via CSV","nome":"Ernane Patrocinio","data_hora":"2026-04-24T14:40:01.966026100","departamento":"Departamento de RH"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"BOM","num_serie":"CN-01M2XX - qdc00-874-1wvl-ao5","localizacao":"Sala do Responsável de RH","sku_produto":"MON-DEL-9Q8-001","departamento":"Departamento de RH"}',
 '2026-04-24 14:40:01','REMOTE-IMPORT');

-- ---------- 135 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0135','CRIACAO','TCD-DEL-ONN-001','CN-0NNC2P-L0300-7B0-07C8-A03','Drh_05',
 '{"id":"Drh_05","acao":"Importação de equipamento via CSV","nome":"Ernane Patrocinio","data_hora":"2026-04-24T14:40:01.987016800","departamento":"Departamento de RH"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"BOM","num_serie":"CN-0NNC2P-L0300-7B0-07C8-A03","localizacao":"Sala do Responsável de RH","sku_produto":"TCD-DEL-ONN-001","departamento":"Departamento de RH"}',
 '2026-04-24 14:40:01','REMOTE-IMPORT');

-- ---------- 136 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0136','CRIACAO','MSE-HPX-7CH-001','CN-0DV0RH-L0300-B1A-0QR5','Drh_05',
 '{"id":"Drh_05","acao":"Importação de equipamento via CSV","nome":"Ernane Patrocinio","data_hora":"2026-04-24T14:40:01.996015300","departamento":"Departamento de RH"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"BOM","num_serie":"CN-0DV0RH-L0300-B1A-0QR5","localizacao":"Sala do Responsável de RH","sku_produto":"MSE-HPX-7CH-001","departamento":"Departamento de RH"}',
 '2026-04-24 14:40:01','REMOTE-IMPORT');

-- ---------- 137 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0137','CRIACAO','CPU-DEL-OPT-001','95P7DL2','Drh_06',
 '{"id":"Drh_06","acao":"Importação de equipamento via CSV","nome":"Ludmila Bonfim","data_hora":"2026-04-24T14:40:02.003018300","departamento":"Departamento de RH"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"BOM","num_serie":"95P7DL2","localizacao":"Sala Comum Escritorio Geral","sku_produto":"CPU-DEL-OPT-001","departamento":"Departamento de RH"}',
 '2026-04-24 14:40:02','REMOTE-IMPORT');

-- ---------- 138 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0138','CRIACAO','MON-DEL-4S8-001','CN-01M2XX-QDC00-874-1YTI-A05','Drh_06',
 '{"id":"Drh_06","acao":"Importação de equipamento via CSV","nome":"Ludmila Bonfim","data_hora":"2026-04-24T14:40:02.018015900","departamento":"Departamento de RH"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"BOM","num_serie":"CN-01M2XX-QDC00-874-1YTI-A05","localizacao":"Sala Comum Escritorio Geral","sku_produto":"MON-DEL-4S8-001","departamento":"Departamento de RH"}',
 '2026-04-24 14:40:02','REMOTE-IMPORT');

-- ---------- 139 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0139','CRIACAO','IMP-HPX-HPD-001',NULL,'Inf_02',
 '{"id":"Inf_02","acao":"Importação de equipamento via CSV","nome":"Durdiley Guine","data_hora":"2026-04-24T14:40:02.051108500","departamento":"Departamento de Infraestruturas"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"BOM","localizacao":"Sala da infraestrutura","sku_produto":"IMP-HPX-HPD-001","departamento":"Departamento de Infraestruturas"}',
 '2026-04-24 14:40:02','REMOTE-IMPORT');

-- ---------- 140 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0140','CRIACAO','MON-DEL-CN0-001',NULL,'Pom_05',
 '{"id":"Pom_05","acao":"Importação de equipamento via CSV","nome":"Martim Viegas","data_hora":"2026-04-24T14:40:02.104645000","departamento":"Departamento Industrial (POM)"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"CRITICO","localizacao":"Escritório de Manutenção (POM)","observacoes":"Linhas coloridas no visor","sku_produto":"MON-DEL-CN0-001","departamento":"Departamento Industrial (POM)"}',
 '2026-04-24 14:40:02','REMOTE-IMPORT');

-- ---------- 141 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0141','CRIACAO','TCD-LOG-MXK-002',NULL,'Pom_11',
 '{"id":"Pom_11","acao":"Importação de equipamento via CSV","nome":"Paulo Gorge","data_hora":"2026-04-24T14:40:02.151685900","departamento":"Departamento Financeiro"}',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"OTIMO","localizacao":"Sala do Departamento Comercial (POM)","sku_produto":"TCD-LOG-MXK-002","departamento":"Departamento Finenceiro"}',
 '2026-04-24 14:40:02','REMOTE-IMPORT');

-- ---------- 142 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0142','ATUALIZACAO','MON-PHI-223-001','UK0A2245004933','Pom_016',
 NULL,
 '{"mac":"null","sku":"MON-PHI-223-001","status":"ATIVO","condicao":"BOM","numSerie":"UK0A2245004933","localizacao":"null"}',
 '{"mac":"34","sku":"MON-PHI-223-001","status":"ATIVO","condicao":"BOM","numSerie":"UK0A2245004933","localizacao":"Sala De It"}',
 '2026-04-27 13:16:22','REMOTE-IMPORT');

-- ---------- 143 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0143','ATUALIZACAO','CPU-DEL-VES-001','UK0A2245004933','Pom_016',
 NULL,
 '{"mac":"34","sku":"MON-PHI-223-001","status":"ATIVO","condicao":"BOM","numSerie":"UK0A2245004933","localizacao":"Sala De It"}',
 '{"mac":"34:95","sku":"CPU-DEL-VES-001","status":"ATIVO","condicao":"BOM","numSerie":"UK0A2245004933","localizacao":"Sala De It"}',
 '2026-04-27 13:19:07','REMOTE-IMPORT');

-- ---------- 144 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0144','ATUALIZACAO','TON-HPX-LAS-001','CF259A/3183C002','Drh_06',
 NULL,
 '{"mac":"null","sku":"TON-HPX-LAS-001","status":"ATIVO","condicao":"BOM","numSerie":"CF259A/3183C002","localizacao":"Balança (POM)"}',
 '{"mac":"56","sku":"TON-HPX-LAS-001","status":"ATIVO","condicao":"BOM","numSerie":"CF259A/3183C002","localizacao":"Balança (POM)"}',
 '2026-04-30 13:50:21','REMOTE-IMPORT');

-- ---------- 145 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0145','CRIACAO','CMP-HPX-250-001','7676453','Ofi_02',
 NULL,
 NULL,
 '{"mac":"64:774:","sku":"CMP-HPX-250-001","status":"EM_USO","condicao":"BOM","numSerie":"7676453","localizacao":"Sala do Responsável de Oficina","observacoes":"Testando criação no historico","departamento":"Departamento de Oficina"}',
 '2026-05-04 11:14:15','REMOTE-IMPORT');

-- ---------- 146 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0146','ATUALIZACAO','CMP-HPX-250-001','7676453','Ofi_02',
 NULL,
 '{"mac":"64:774:","sku":"CMP-HPX-250-001","status":"EM_USO","condicao":"BOM","numSerie":"7676453","localizacao":"Sala do Responsável de Oficina","observacoes":"Testando criação no historico","departamento":"Departamento de Oficina"}',
 '{"mac":"64:774:","sku":"CMP-HPX-250-001","status":"EM_USO","condicao":"CRITICO","numSerie":"7676453","localizacao":"Sala do Responsável de Oficina","observacoes":"Testando atualização de dados","departamento":"Departamento de Oficina"}',
 '2026-05-04 11:18:35','REMOTE-IMPORT');

-- ---------- 147 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0147','CRIACAO','CPU-DEL-OPT-001','7676453','Ofi_02',
 NULL,
 NULL,
 '{"mac":"64:774:","sku":"CPU-DEL-OPT-001","status":"EM_USO","condicao":"OTIMO","numSerie":"7676453","localizacao":"Sala do Responsável de Oficina","observacoes":"Testando atualização de dados","departamento":"Departamento de Oficina"}',
 '2026-05-04 11:20:11','REMOTE-IMPORT');

-- ---------- 148 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0148','ATUALIZACAO','CPU-DEL-VES-001','UK0A2245004933','Pom_016',
 NULL,
 '{"mac":"34:95","sku":"CPU-DEL-VES-001","status":"ATIVO","condicao":"BOM","numSerie":"UK0A2245004933","localizacao":"Sala De It"}',
 '{"mac":"34:95","sku":"CPU-DEL-VES-001","status":"EM_USO","condicao":"REGULAR","numSerie":"UK0A2245004933","localizacao":"Sala De It","observacoes":"","departamento":"Departamento de IT","dataAquisicao":"2023-10-31","funcionarioId":"Pom_016"}',
 '2026-05-04 11:40:59','REMOTE-IMPORT');

-- ---------- 149 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0149','ATUALIZACAO','CPU-DEL-VES-001','UK0A2245004933','Pom_016',
 NULL,
 '{"mac":"34:95","sku":"CPU-DEL-VES-001","status":"EM_USO","condicao":"REGULAR","numSerie":"UK0A2245004933","localizacao":"Sala De It","observacoes":"","departamento":"Departamento de IT","dataAquisicao":"2023-10-31","funcionarioId":"Pom_016"}',
 '{"mac":"34:95","sku":"CPU-DEL-VES-001","status":"EM_USO","condicao":"REGULAR","numSerie":"UK0A2245004933","localizacao":"Sala De It","observacoes":"","departamento":"Departamento de IT","dataAquisicao":"2023-10-31","funcionarioId":"Pom_016"}',
 '2026-05-04 11:41:23','REMOTE-IMPORT');

-- ---------- 150 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0150','CRIACAO','CMP-ASU-X55-001','33ffs','Pom_016',
 NULL,
 NULL,
 '{"mac":"teste","sku":"CMP-ASU-X55-001","status":"EM_USO","condicao":"CRITICO","numSerie":"33ffs","localizacao":"Sala de Informática (POM)","observacoes":"teste","departamento":"Departamento de IT","dataAquisicao":"2026-04-24","funcionarioId":"Pom_016"}',
 '2026-05-04 11:42:31','REMOTE-IMPORT');

-- ---------- 151 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0151','ATUALIZACAO','CPU-DEL-VES-001','UK0A2245004933','Pom_016',
 NULL,
 '{"mac":"34:95","sku":"CPU-DEL-VES-001","status":"EM_USO","condicao":"REGULAR","numSerie":"UK0A2245004933","localizacao":"Sala De It","observacoes":"","departamento":"Departamento de IT","dataAquisicao":"2023-10-31","funcionarioId":"Pom_016"}',
 '{"mac":"34:95","sku":"CPU-DEL-VES-001","status":"EM_USO","condicao":"CRITICO","numSerie":"UK0A2245004933","localizacao":"Sala De It","observacoes":"","departamento":"Departamento de IT","dataAquisicao":"2023-10-31","funcionarioId":"Pom_016"}',
 '2026-05-04 11:44:32','REMOTE-IMPORT');

-- ---------- 152 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0152','CRIACAO','CMP-DEL-ESS-017','7j91hg4','Dcl_01',
 NULL,
 NULL,
 '{"mac":"44:F7:9F:D0:73:6D","sku":"CMP-DEL-ESS-017","status":"EM_USO","condicao":"OTIMO","numSerie":"7j91hg4","localizacao":"Dloretor de Compras","observacoes":"","departamento":"Departamento Comercial","dataAquisicao":"2026-05-09","funcionarioId":"Dcl_01"}',
 '2026-05-09 10:04:23','REMOTE-IMPORT');

-- ---------- 153 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0153','ATUALIZACAO','CMP-HPX-15D-001','5CD3322SQ5','Dcl_01',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"BOM","num_serie":"5CD3322SQ5","localizacao":"Dloretor de Compras","sku_produto":"CMP-HPX-15D-001","departamento":"Departamento Comercial"}',
 '{"mac":"88","sku":"CMP-HPX-15D-001","status":"ATIVO","condicao":"BOM","numSerie":"5CD3322SQ5","localizacao":"Dloretor de Compras","observacoes":"","departamento":"Departamento Comercial","dataAquisicao":"2024-12-30","funcionarioId":"Dcl_01"}',
 '2026-05-11 08:42:31','REMOTE-IMPORT');

-- ---------- 154 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0154','EXCLUSAO','CMP-ASU-X55-001','33ffs','Pom_016',
 '{"nome":"Rafael Tavares","cargo":"Operador","departamento":"Departamento de IT"}',
 '{"id":143,"status":"EM_USO","condicao":"CRITICO","numSerie":"33ffs","skuProduto":"CMP-ASU-X55-001","enderecoMac":"teste","localizacao":"Sala de Informática (POM)","departamento":"Departamento de IT","dataAquisicao":"2026-04-24"}',
 '{"status":"EXCLUIDO","data_exclusao":"2026-05-18T12:13:37.713574696"}',
 '2026-05-18 12:13:33','REMOTE-IMPORT');

-- ---------- 155 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0155','EXCLUSAO','CPU-DEL-OPT-001','7676453','Ofi_02',
 '{"nome":"Rail dos Reis","cargo":"Operador","departamento":"Departamento de Oficina"}',
 '{"id":142,"status":"EM_USO","condicao":"OTIMO","numSerie":"7676453","skuProduto":"CPU-DEL-OPT-001","enderecoMac":"64:774:","localizacao":"Sala do Responsável de Oficina","departamento":"Departamento de Oficina","dataAquisicao":"2026-04-24"}',
 '{"status":"EXCLUIDO","data_exclusao":"2026-05-18T12:14:01.086278016"}',
 '2026-05-18 12:13:57','REMOTE-IMPORT');

-- ---------- 156 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0156','EXCLUSAO','CPU-DEL-VES-001','UK0A2245004933','Pom_016',
 '{"nome":"Rafael Tavares","cargo":"Operador","departamento":"Departamento de IT"}',
 '{"id":122,"status":"EM_USO","condicao":"CRITICO","numSerie":"UK0A2245004933","skuProduto":"CPU-DEL-VES-001","enderecoMac":"34:95","localizacao":"Sala De It","departamento":"Departamento de IT","dataAquisicao":"2023-10-31"}',
 '{"status":"EXCLUIDO","data_exclusao":"2026-05-18T12:14:31.334706420"}',
 '2026-05-18 12:14:27','REMOTE-IMPORT');

-- ---------- 157 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0157','CRIACAO','MON-DEL-E24-004','CN-06JT9C-FCC00-571-D4PX-A01','Daf_01',
 NULL,
 NULL,
 '{"mac":"KJ:","sku":"MON-DEL-E24-004","status":"EM_USO","condicao":"OTIMO","numSerie":"CN-06JT9C-FCC00-571-D4PX-A01","localizacao":"Sala da Direção Financeira","observacoes":"","departamento":"Departamento Financeiro","dataAquisicao":"2026-05-29","funcionarioId":"Daf_01"}',
 '2026-05-29 11:38:31','REMOTE-IMPORT');

-- ---------- 158 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0158','ATUALIZACAO','CMP-HPX-15F-001','1h84503rts','Daf_01',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"CRITICO","num_serie":"1h84503rts","localizacao":"Sala da Direção Financeira","sku_produto":"CMP-HPX-15F-001","departamento":"Departamento Financeiro","endereco_mac":"EB:B0:C5:5C:91:F8"}',
 '{"mac":"E8:B0:C5:5C:91:F8","sku":"CMP-HPX-15F-001","status":"EM_USO","condicao":"BOM","numSerie":"1h84503rts","localizacao":"Sala da Direção Financeira","observacoes":"","departamento":"Departamento Financeiro","dataAquisicao":"2026-04-24","funcionarioId":"Daf_01"}',
 '2026-05-29 11:53:05','REMOTE-IMPORT');

-- ---------- 159 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0159','CRIACAO','MON-DEL-E24-005','CN-06JT9C-FCC00-571-D4KX-A01','Daf_07',
 NULL,
 NULL,
 '{"mac":"TER","sku":"MON-DEL-E24-005","status":"EM_USO","condicao":"BOM","numSerie":"CN-06JT9C-FCC00-571-D4KX-A01","localizacao":"Departamento Financeiro e Contabilidade","observacoes":"","departamento":"Departamento Financeiro","dataAquisicao":"2026-06-03","funcionarioId":"Daf_07"}',
 '2026-06-03 09:53:52','REMOTE-IMPORT');

-- ---------- 160 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0160','EXCLUSAO','CMP-DEL-VES-001','876FFL2','Daf_07',
 '{"nome":"Dinizia Fortes","cargo":"Operador","departamento":"Departamento Financeiro"}',
 '{"id":81,"status":"ATIVO","condicao":"BOM","numSerie":"876FFL2","skuProduto":"CMP-DEL-VES-001","enderecoMac":"74:40:bb:6d:1d:3b","localizacao":"Departamento Financeiro e Contabilidade","departamento":"Departamento Financeiro","dataAquisicao":"2026-04-24"}',
 '{"status":"EXCLUIDO","data_exclusao":"2026-06-03T10:06:26.549820596"}',
 '2026-06-03 10:06:09','REMOTE-IMPORT');

-- ---------- 161 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0161','ATUALIZACAO','CMP-DEL-VES-001','876FFL2','Daf_07',
 NULL,
 '{"status":"EXCLUIDO","data_exclusao":"2026-06-03T10:06:26.549820596"}',
 '{"mac":"74:40:bb:6d:1d:3b","sku":"CMP-DEL-VES-001","status":"EM_USO","condicao":"BOM","numSerie":"876FFL2","localizacao":"Departamento Financeiro e Contabilidade","observacoes":"Com problemas no caregador, causando pequenos curto circuitos.","departamento":"Departamento Financeiro"}',
 '2026-06-03 10:07:21','REMOTE-IMPORT');

-- ---------- 162 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0162','CRIACAO','MON-DEL-E24-004','VN-0WYGX3QDV00-47T-1DSL-A03','Daf_02',
 NULL,
 NULL,
 '{"mac":"no","sku":"MON-DEL-E24-004","status":"ATIVO","condicao":"OTIMO","numSerie":"VN-0WYGX3QDV00-47T-1DSL-A03","localizacao":"Departamento Financeiro e Contabilidade","observacoes":"","departamento":"Departamento Financeiro","dataAquisicao":"2026-06-03","funcionarioId":"Daf_02"}',
 '2026-06-03 10:30:01','REMOTE-IMPORT');

-- ---------- 163 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0163','CRIACAO','MON-DEL-GYB-001','CN-01M2XX-QDC00-874-3841-A05','Daf_05',
 NULL,
 NULL,
 '{"mac":"NO1","sku":"MON-DEL-GYB-001","status":"EM_USO","condicao":"BOM","numSerie":"CN-01M2XX-QDC00-874-3841-A05","localizacao":"Departamento Financeiro e Contabilidade","observacoes":"","departamento":"Departamento Financeiro","dataAquisicao":"2026-06-03","funcionarioId":"Daf_05"}',
 '2026-06-03 10:36:57','REMOTE-IMPORT');

-- ---------- 164 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0164','CRIACAO','CPU-DEL-OPT-001','J5P7DL2','Daf_05',
 NULL,
 NULL,
 '{"mac":"24:2f:d0:d9:82:71","sku":"CPU-DEL-OPT-001","status":"EM_USO","condicao":"BOM","numSerie":"J5P7DL2","localizacao":"Departamento Financeiro e Contabilidade","observacoes":"Desliga sozinho.","departamento":"Departamento Financeiro","dataAquisicao":"2026-06-03","funcionarioId":"Daf_05"}',
 '2026-06-03 10:48:05','REMOTE-IMPORT');

-- ---------- 165 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0165','CRIACAO','TCD-LOG-MXK-001','2019DJ7094 ITFRCPLOY','Daf_05',
 NULL,
 NULL,
 '{"mac":"NO2","sku":"TCD-LOG-MXK-001","status":"EM_USO","condicao":"BOM","numSerie":"2019DJ7094 ITFRCPLOY","localizacao":"Departamento Financeiro e Contabilidade","observacoes":"","departamento":"Departamento Financeiro","dataAquisicao":"2026-06-03","funcionarioId":"Daf_05"}',
 '2026-06-03 10:59:55','REMOTE-IMPORT');

-- ---------- 166 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0166','CRIACAO','CMP-DEL-ESS-017','P112FD13','Daf_07',
 NULL,
 NULL,
 '{"mac":"44:f7:9f:d1:99:f3","sku":"CMP-DEL-ESS-017","status":"EM_USO","condicao":"OTIMO","numSerie":"P112FD13","localizacao":"Departamento Financeiro e Contabilidade","observacoes":"","departamento":"Departamento Financeiro","dataAquisicao":"2026-06-03","funcionarioId":"Daf_07"}',
 '2026-06-03 11:12:20','REMOTE-IMPORT');

-- ---------- 167 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0167','ATUALIZACAO','CMP-DEL-VOS-001','G76FFL2','Daf_01',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"BOM","num_serie":"G76FFL2","localizacao":"Sala da Direção Financeira","sku_produto":"CMP-DEL-VOS-001","departamento":"Departamento Financeiro"}',
 '{"mac":"yy","sku":"CMP-DEL-VOS-001","status":"EM_USO","condicao":"BOM","numSerie":"G76FFL2","localizacao":"Sala da Direção Financeira","observacoes":"","departamento":"Departamento Financeiro","dataAquisicao":"2026-04-24","funcionarioId":"Daf_01"}',
 '2026-06-03 12:31:46','REMOTE-IMPORT');

-- ---------- 168 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0168','CRIACAO','MON-DEL-U24-001','CN-0YPPY0-tv100-8b7-15xv-a10','Daf_07',
 NULL,
 NULL,
 '{"mac":"h","sku":"MON-DEL-U24-001","status":"EM_USO","condicao":"BOM","numSerie":"CN-0YPPY0-tv100-8b7-15xv-a10","localizacao":"Departamento Financeiro e Contabilidade","observacoes":"","departamento":"Departamento Financeiro","dataAquisicao":"2026-06-03","funcionarioId":"Daf_07"}',
 '2026-06-03 12:34:34','REMOTE-IMPORT');

-- ---------- 169 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0169','ATUALIZACAO','TCD-LOG-MXK-001','2019D17096daf-01','Daf_01',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"OTIMO","num_serie":"2019D17096","localizacao":"Sala da Direção Financeira","sku_produto":"TCD-LOG-MXK-001","departamento":"Departamento Financeiro"}',
 '{"mac":"TT","sku":"TCD-LOG-MXK-001","status":"EM_USO","condicao":"BOM","numSerie":"2019D17096daf-01","localizacao":"Sala da Direção Financeira","observacoes":"","departamento":"Departamento Financeiro","dataAquisicao":"2026-04-24","funcionarioId":"Daf_01"}',
 '2026-06-03 12:46:01','REMOTE-IMPORT');

-- ---------- 170 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0170','CRIACAO','MON-DEL-P24-001','VN-0WYGX3-QDV00-47T-1DOL-A03','Dcl_03',
 NULL,
 NULL,
 '{"mac":"rt","sku":"MON-DEL-P24-001","status":"EM_USO","condicao":"OTIMO","numSerie":"VN-0WYGX3-QDV00-47T-1DOL-A03","localizacao":"Sala de contabilidade Escritorio plantação","observacoes":"","departamento":"Departamento Compras e Logística","dataAquisicao":"2026-06-05","funcionarioId":"Dcl_03"}',
 '2026-06-05 09:14:07','REMOTE-IMPORT');

-- ---------- 171 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0171','CRIACAO','CMP-DEL-ESS-017','GD991HG4','Dcl_03',
 NULL,
 NULL,
 '{"mac":"44:f7:9f:d1:9a:35","sku":"CMP-DEL-ESS-017","status":"EM_USO","condicao":"OTIMO","numSerie":"GD991HG4","localizacao":"Sala de contabilidade Escritorio plantação","observacoes":"","departamento":"Departamento Compras e Logística","dataAquisicao":"2026-06-05","funcionarioId":"Dcl_03"}',
 '2026-06-05 09:22:48','REMOTE-IMPORT');

-- ---------- 172 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0172','CRIACAO','TCD-LOG-MXK-002','2019DJ7094','Dcl_03',
 NULL,
 NULL,
 '{"mac":"FD","sku":"TCD-LOG-MXK-002","status":"EM_USO","condicao":"BOM","numSerie":"2019DJ7094","localizacao":"Sala de contabilidade Escritorio plantação","observacoes":"","departamento":"Departamento Compras e Logística","dataAquisicao":"2026-06-05","funcionarioId":"Dcl_03"}',
 '2026-06-05 09:26:32','REMOTE-IMPORT');

-- ---------- 173 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0173','ATUALIZACAO','MSE-LOG-MXA-001','2405LZ034YP8','Drh_01',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"BOM","num_serie":"2405LZ034YP8","localizacao":"Sala do Responsável de RH","sku_produto":"MSE-LOG-MXA-001","departamento":"Departamento de RH"}',
 '{"mac":"e","sku":"MSE-LOG-MXA-001","status":"EM_USO","condicao":"BOM","numSerie":"2405LZ034YP8","localizacao":"Sala da contabilidade escritorio plantação","observacoes":"","departamento":"Departamento Compras e Logística","dataAquisicao":"2026-04-24","funcionarioId":"Drh_01"}',
 '2026-06-05 09:53:22','REMOTE-IMPORT');

-- ---------- 174 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0174','ATUALIZACAO','CMP-LEN-PF9-001','PF3CYL7H','Drh_01',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"OTIMO","num_serie":"PF3CYL7H","localizacao":"Sala do Responsável de RH","sku_produto":"CMP-LEN-PF9-001","departamento":"Departamento de RH"}',
 '{"mac":"00:45:e2:99:de:25","sku":"CMP-LEN-PF9-001","status":"EM_USO","condicao":"BOM","numSerie":"PF3CYL7H","localizacao":"Sala da contabilidade escritorio plantação","observacoes":"","departamento":"Departamento Compras e Logística","dataAquisicao":"2026-04-24","funcionarioId":"Drh_01"}',
 '2026-06-05 10:00:05','REMOTE-IMPORT');

-- ---------- 175 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0175','CRIACAO','MON-DEL-E24-004','CN-06JT9C-FCC00-571-D4MX-A01','Drh_01',
 NULL,
 NULL,
 '{"mac":"F","sku":"MON-DEL-E24-004","status":"EM_USO","condicao":"OTIMO","numSerie":"CN-06JT9C-FCC00-571-D4MX-A01","localizacao":"Sala da contabilidade escritorio plantação","observacoes":"","departamento":"Departamento Compras e Logística","dataAquisicao":"2026-06-05","funcionarioId":"Drh_01"}',
 '2026-06-05 10:05:28','REMOTE-IMPORT');

-- ---------- 176 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0176','CRIACAO','TCD-LOG-MXK-002','201DJ7094','Drh_01',
 NULL,
 NULL,
 '{"mac":"G","sku":"TCD-LOG-MXK-002","status":"EM_USO","condicao":"OTIMO","numSerie":"201DJ7094","localizacao":"Sala da contabilidade escritorio plantação","observacoes":"","departamento":"Departamento Compras e Logística","dataAquisicao":"2026-06-05","funcionarioId":"Drh_01"}',
 '2026-06-05 10:08:57','REMOTE-IMPORT');

-- ---------- 177 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0177','ATUALIZACAO','MSE-LOG-MXA-001','2405LZ034YP8','Drh_01',
 NULL,
 '{"mac":"e","sku":"MSE-LOG-MXA-001","status":"EM_USO","condicao":"BOM","numSerie":"2405LZ034YP8","localizacao":"Sala da contabilidade escritorio plantação","observacoes":"","departamento":"Departamento Compras e Logística","dataAquisicao":"2026-04-24","funcionarioId":"Drh_01"}',
 '{"mac":"e","sku":"MSE-LOG-MXA-001","status":"EM_USO","condicao":"BOM","numSerie":"2405LZ034YP8","localizacao":"Sala da contabilidade escritorio plantação","observacoes":"","departamento":"Departamento Compras e Logística","dataAquisicao":"2026-04-24","funcionarioId":"Drh_01"}',
 '2026-06-05 10:10:08','REMOTE-IMPORT');

-- ---------- 178 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0178','CRIACAO','MSE-LOG-MXA-001','2417LZ02PRES','Dcl_03',
 NULL,
 NULL,
 '{"mac":"d","sku":"MSE-LOG-MXA-001","status":"EM_USO","condicao":"BOM","numSerie":"2417LZ02PRES","localizacao":"Sala de contabilidade Escritorio plantação","observacoes":"","departamento":"Departamento Compras e Logística","dataAquisicao":"2026-06-05","funcionarioId":"Dcl_03"}',
 '2026-06-05 10:35:53','REMOTE-IMPORT');

-- ---------- 179 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0179','CRIACAO','MON-DEL-E24-004','CN-06JT9C-FCC00-571-D4KX-A01-DAF','Daf_09',
 NULL,
 NULL,
 '{"mac":"T","sku":"MON-DEL-E24-004","status":"EM_USO","condicao":"OTIMO","numSerie":"CN-06JT9C-FCC00-571-D4KX-A01-DAF","localizacao":"Sala de tesouraria do escritorio plantação","observacoes":"","departamento":"Departamento Financeiro","dataAquisicao":"2026-06-05","funcionarioId":"Daf_09"}',
 '2026-06-05 10:50:45','REMOTE-IMPORT');

-- ---------- 180 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0180','CRIACAO','CMP-THI-LEN-001','tt56','Daf_09',
 NULL,
 NULL,
 '{"mac":"28:c6:3f:be:a3:02","sku":"CMP-THI-LEN-001","status":"EM_USO","condicao":"REGULAR","numSerie":"tt56","localizacao":"Sala de tesouraria do escritorio plantação","observacoes":"","departamento":"Departamento Financeiro","dataAquisicao":"2026-06-05","funcionarioId":"Daf_09"}',
 '2026-06-05 10:59:23','REMOTE-IMPORT');

-- ---------- 181 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0181','CRIACAO','CMP-DEL-ESS-017','7j91hg4','Amz_02',
 NULL,
 NULL,
 '{"mac":"44:F7:9F:D0:73:6D","sku":"CMP-DEL-ESS-017","status":"EM_USO","condicao":"OTIMO","numSerie":"7j91hg4","localizacao":"Armazem Geral","observacoes":"","departamento":"Departamento Compras e Logística","dataAquisicao":"2026-05-09","funcionarioId":"Amz_02"}',
 '2026-06-09 10:33:44','REMOTE-IMPORT');

-- ---------- 182 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0182','CRIACAO','MON-AOC-M24-006','236LM00014','Amz_02',
 NULL,
 NULL,
 '{"mac":"UT","sku":"MON-AOC-M24-006","status":"EM_USO","condicao":"BOM","numSerie":"236LM00014","localizacao":"Armazem Geral","observacoes":"","departamento":"Departamento Compras e Logística","dataAquisicao":"2026-06-09","funcionarioId":"Amz_02"}',
 '2026-06-09 10:39:35','REMOTE-IMPORT');

-- ---------- 183 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0183','ATUALIZACAO','CMP-HPX-RTL-001','CND2472S79','Amz_02',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"BOM","num_serie":"CND2472S79","localizacao":"Armazém Geral","sku_produto":"CMP-HPX-RTL-001","departamento":"Departamento de Armazém"}',
 '{"mac":"YU","sku":"CMP-HPX-RTL-001","status":"MANUTENCAO","condicao":"CRITICO","numSerie":"CND2472S79","localizacao":"Armazém Geral","observacoes":"","departamento":"Departamento de Armazém","dataAquisicao":"2023-07-22","funcionarioId":"Amz_02"}',
 '2026-06-09 10:42:05','REMOTE-IMPORT');

-- ---------- 184 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0184','ATUALIZACAO','CMP-HPX-15D-001','5CD3322SQ5','Dcl_01',
 NULL,
 '{"mac":"88","sku":"CMP-HPX-15D-001","status":"ATIVO","condicao":"BOM","numSerie":"5CD3322SQ5","localizacao":"Dloretor de Compras","observacoes":"","departamento":"Departamento Comercial","dataAquisicao":"2024-12-30","funcionarioId":"Dcl_01"}',
 '{"mac":"88","sku":"CMP-HPX-15D-001","status":"BAIXADO","condicao":"BOM","numSerie":"5CD3322SQ5","localizacao":"Dloretor de Compras","observacoes":"Foi roubado na empresa.","departamento":"Departamento Comercial","dataAquisicao":"2024-12-30","funcionarioId":"Dcl_01"}',
 '2026-06-09 10:45:04','REMOTE-IMPORT');

-- ---------- 185 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0185','CRIACAO','CMP-HPX-15D-001','5CD3321WXP','Pom_05',
 NULL,
 NULL,
 '{"mac":"uu","sku":"CMP-HPX-15D-001","status":"EM_USO","condicao":"BOM","numSerie":"5CD3321WXP","localizacao":"Escritório de Manutenção (POM)","observacoes":"","departamento":"Departamento Industrial (POM)","dataAquisicao":"2024-12-17","funcionarioId":"Pom_05"}',
 '2026-06-09 10:46:38','REMOTE-IMPORT');

-- ---------- 186 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0186','ATUALIZACAO','MSE-LOG-MXA-001','2417LZ02PRE8','Dcl_03',
 NULL,
 '{"mac":"d","sku":"MSE-LOG-MXA-001","status":"EM_USO","condicao":"BOM","numSerie":"2417LZ02PRES","localizacao":"Sala de contabilidade Escritorio plantação","observacoes":"","departamento":"Departamento Compras e Logística","dataAquisicao":"2026-06-05","funcionarioId":"Dcl_03"}',
 '{"mac":"uy","sku":"MSE-LOG-MXA-001","status":"EM_USO","condicao":"OTIMO","numSerie":"2417LZ02PRE8","localizacao":"Sala de contabilidade Escritorio plantação","observacoes":"","departamento":"Departamento Compras e Logística","dataAquisicao":"2026-06-05","funcionarioId":"Dcl_03"}',
 '2026-06-09 10:47:16','REMOTE-IMPORT');

-- ---------- 187 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0187','ATUALIZACAO','MON-AOC-M24-006','236LM00014','Amz_02',
 NULL,
 '{"mac":"UT","sku":"MON-AOC-M24-006","status":"EM_USO","condicao":"BOM","numSerie":"236LM00014","localizacao":"Armazem Geral","observacoes":"","departamento":"Departamento Compras e Logística","dataAquisicao":"2026-06-09","funcionarioId":"Amz_02"}',
 '{"mac":"UT","sku":"MON-AOC-M24-006","status":"EM_USO","condicao":"BOM","numSerie":"Agri-Azm_Aoc_00-01-A","localizacao":"Armazem Geral","observacoes":"","departamento":"Departamento Compras e Logística","dataAquisicao":"2026-06-09","funcionarioId":"Amz_02"}',
 '2026-06-18 15:14:40','REMOTE-IMPORT');

-- ---------- 188 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0188','ATUALIZACAO','MON-AOC-M24-006','Agri-Azm_Aoc_00-01-A','Amz_02',
 NULL,
 '{"mac":"UT","sku":"MON-AOC-M24-006","status":"EM_USO","condicao":"BOM","numSerie":"Agri-Azm_Aoc_00-01-A","localizacao":"Armazem Geral","observacoes":"","departamento":"Departamento Compras e Logística","dataAquisicao":"2026-06-09","funcionarioId":"Amz_02"}',
 '{"mac":"","sku":"MON-AOC-M24-006","status":"EM_USO","condicao":"BOM","numSerie":"Agri-Azm_Aoc_00-01-A","localizacao":"Armazem Geral","observacoes":"","departamento":"Departamento Compras e Logística","dataAquisicao":"2026-06-09","funcionarioId":"Amz_02"}',
 '2026-07-03 12:29:36','REMOTE-IMPORT');

-- ---------- 189 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0189','ATUALIZACAO','MON-DEL-E24-004','VN-0WYGX3QDV00-47T-1DSL-A03','Daf_02',
 NULL,
 '{"mac":"no","sku":"MON-DEL-E24-004","status":"ATIVO","condicao":"OTIMO","numSerie":"VN-0WYGX3QDV00-47T-1DSL-A03","localizacao":"Departamento Financeiro e Contabilidade","observacoes":"","departamento":"Departamento Financeiro","dataAquisicao":"2026-06-03","funcionarioId":"Daf_02"}',
 '{"mac":"","sku":"MON-DEL-E24-004","status":"ATIVO","condicao":"OTIMO","numSerie":"Agri-Daf-Dell-00-00-A","localizacao":"Departamento Financeiro e Contabilidade","observacoes":"","departamento":"Departamento Financeiro","dataAquisicao":"2026-06-03","funcionarioId":"Daf_02"}',
 '2026-07-03 12:30:58','REMOTE-IMPORT');

-- ---------- 190 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0190','ATUALIZACAO','MON-AOC-M24-006','Agri-Azm_Aoc_00-01-A','Amz_02',
 NULL,
 '{"mac":"","sku":"MON-AOC-M24-006","status":"EM_USO","condicao":"BOM","numSerie":"Agri-Azm_Aoc_00-01-A","localizacao":"Armazem Geral","observacoes":"","departamento":"Departamento Compras e Logística","dataAquisicao":"2026-06-09","funcionarioId":"Amz_02"}',
 '{"mac":"","sku":"MON-AOC-M24-006","status":"EM_USO","condicao":"BOM","numSerie":"Agri-Azm_Aoc_00-01-A","localizacao":"Armazem Geral","observacoes":"","departamento":"Departamento Compras e Logística","dataAquisicao":"2026-06-09","funcionarioId":"Amz_02"}',
 '2026-07-03 12:59:06','REMOTE-IMPORT');

-- ---------- 191 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0191','ATUALIZACAO','MON-AOC-M24-006','Agri-Azm_Aoc_00-01-A','Amz_02',
 NULL,
 '{"mac":"","sku":"MON-AOC-M24-006","status":"EM_USO","condicao":"BOM","numSerie":"Agri-Azm_Aoc_00-01-A","localizacao":"Armazem Geral","observacoes":"","departamento":"Departamento Compras e Logística","dataAquisicao":"2026-06-09","funcionarioId":"Amz_02"}',
 '{"mac":"","sku":"MON-AOC-M24-006","status":"EM_USO","condicao":"BOM","numSerie":"Agri-Azm_Aoc_00-01-A","localizacao":"Armazem Geral","observacoes":"","departamento":"Departamento Compras e Logística","dataAquisicao":"2026-06-09","funcionarioId":"Amz_02"}',
 '2026-07-03 13:00:40','REMOTE-IMPORT');

-- ---------- 192 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0192','ATUALIZACAO','MON-DEL-E24-004','Agri-Daf-Dell-00-00-A','Daf_02',
 NULL,
 '{"mac":"","sku":"MON-DEL-E24-004","status":"ATIVO","condicao":"OTIMO","numSerie":"Agri-Daf-Dell-00-00-A","localizacao":"Departamento Financeiro e Contabilidade","observacoes":"","departamento":"Departamento Financeiro","dataAquisicao":"2026-06-03","funcionarioId":"Daf_02"}',
 '{"mac":"","sku":"MON-DEL-E24-004","status":"ATIVO","condicao":"OTIMO","numSerie":"Agri-Daf-Dell-00-00-A","localizacao":"Departamento Financeiro e Contabilidade","observacoes":"","departamento":"Departamento Financeiro","dataAquisicao":"2026-06-03","funcionarioId":"Daf_02"}',
 '2026-07-03 13:01:05','REMOTE-IMPORT');

-- ---------- 193 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0193','ATUALIZACAO','MSE-LOG-MXA-001','Agri-Daf-Log-00-00-A','Daf_02',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"OTIMO","num_serie":"2417lz02q8m8","localizacao":"Departamento Financeiro e Contabilidade","sku_produto":"MSE-LOG-MXA-001","departamento":"Departamento Financeiro"}',
 '{"mac":"","sku":"MSE-LOG-MXA-001","status":"ATIVO","condicao":"OTIMO","numSerie":"Agri-Daf-Log-00-00-A","localizacao":"Departamento Financeiro e Contabilidade","observacoes":"","departamento":"Departamento Financeiro","dataAquisicao":"2026-04-24","funcionarioId":"Daf_02"}',
 '2026-07-03 13:02:00','REMOTE-IMPORT');

-- ---------- 194 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0194','ATUALIZACAO','MSE-LOG-MXA-001','Agri-Djr-Log-00-00-A','Djr_01',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"OTIMO","num_serie":"2405LZ0351H8","localizacao":"Sala do Responsável Jurídico","sku_produto":"MSE-LOG-MXA-001","departamento":"Departamento Jurídico"}',
 '{"mac":"","sku":"MSE-LOG-MXA-001","status":"EM_USO","condicao":"OTIMO","numSerie":"Agri-Djr-Log-00-00-A","localizacao":"Sala do Responsável Jurídico","observacoes":"","departamento":"Departamento Jurídico","dataAquisicao":"2026-04-24","funcionarioId":"Djr_01"}',
 '2026-07-03 13:37:13','REMOTE-IMPORT');

-- ---------- 195 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0195','ATUALIZACAO','CMP-DEL-ESS-017','Agri-Dcl-Dell-00-00-A','Amz_02',
 NULL,
 '{"mac":"44:F7:9F:D0:73:6D","sku":"CMP-DEL-ESS-017","status":"EM_USO","condicao":"OTIMO","numSerie":"7j91hg4","localizacao":"Armazem Geral","observacoes":"","departamento":"Departamento Compras e Logística","dataAquisicao":"2026-05-09","funcionarioId":"Amz_02"}',
 '{"mac":"44:F7:9F:D0:73:6D","sku":"CMP-DEL-ESS-017","status":"EM_USO","condicao":"OTIMO","numSerie":"Agri-Dcl-Dell-00-00-A","localizacao":"Armazem Geral","observacoes":"","departamento":"Departamento Compras e Logística","dataAquisicao":"2026-05-09","funcionarioId":"Amz_02"}',
 '2026-07-03 13:52:20','REMOTE-IMPORT');

-- ---------- 196 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0196','ATUALIZACAO','TCD-LOG-MXK-001','Agri-Pom-Log-00-00-A','Pom_14',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"OTIMO","localizacao":"Recepção do Dep Industrial","sku_produto":"TCD-LOG-MXK-001","departamento":"Departamento Industrial (POM)"}',
 '{"mac":"","sku":"TCD-LOG-MXK-001","status":"ATIVO","condicao":"OTIMO","numSerie":"Agri-Pom-Log-00-00-A","localizacao":"Recepção do Dep Industrial","observacoes":"","departamento":"Departamento Industrial (POM)","dataAquisicao":"2026-04-24","funcionarioId":"Pom_14"}',
 '2026-07-03 13:55:13','REMOTE-IMPORT');

-- ---------- 197 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0197','ATUALIZACAO','MSE-LOG-MXA-001','Agri-Daf-Log-00-01-A','Daf_02',
 NULL,
 '{"mac":"","sku":"MSE-LOG-MXA-001","status":"ATIVO","condicao":"OTIMO","numSerie":"Agri-Daf-Log-00-00-A","localizacao":"Departamento Financeiro e Contabilidade","observacoes":"","departamento":"Departamento Financeiro","dataAquisicao":"2026-04-24","funcionarioId":"Daf_02"}',
 '{"mac":"","sku":"MSE-LOG-MXA-001","status":"ATIVO","condicao":"OTIMO","numSerie":"Agri-Daf-Log-00-01-A","localizacao":"Departamento Financeiro e Contabilidade","observacoes":"","departamento":"Departamento Financeiro","dataAquisicao":"2026-04-24","funcionarioId":"Daf_02"}',
 '2026-07-03 16:03:08','REMOTE-IMPORT');

-- ---------- 198 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0198','ATUALIZACAO','TCD-LOG-MXK-001','Agri-Daf-Log-00-02-A','Daf_04',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"OTIMO","num_serie":"2019D17094","localizacao":"Departamento Financeiro e Contabilidade","sku_produto":"TCD-LOG-MXK-001","departamento":"Departamento Financeiro"}',
 '{"mac":"","sku":"TCD-LOG-MXK-001","status":"ATIVO","condicao":"OTIMO","numSerie":"Agri-Daf-Log-00-02-A","localizacao":"Departamento Financeiro e Contabilidade","observacoes":"","departamento":"Departamento Financeiro","dataAquisicao":"2026-04-24","funcionarioId":"Daf_04"}',
 '2026-07-06 14:01:17','REMOTE-IMPORT');

-- ---------- 199 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0199','ATUALIZACAO','MON-DEL-CN0-001','Agri-Pom-Dell-00-00-A','Pom_05',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"CRITICO","localizacao":"Escritório de Manutenção (POM)","observacoes":"Linhas coloridas no visor","sku_produto":"MON-DEL-CN0-001","departamento":"Departamento Industrial (POM)"}',
 '{"mac":"","sku":"MON-DEL-CN0-001","status":"ATIVO","condicao":"CRITICO","numSerie":"Agri-Pom-Dell-00-00-A","localizacao":"Escritório de Manutenção (POM)","observacoes":"Linhas coloridas no visor","departamento":"Departamento Industrial (POM)","dataAquisicao":"2026-04-24","funcionarioId":"Pom_05"}',
 '2026-07-06 14:02:10','REMOTE-IMPORT');

-- ---------- 200 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0200','ATUALIZACAO','MSE-LOG-MXA-001','Agri-Drh-Log-00-00-A','Drh_04',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"OTIMO","num_serie":"2417LZ02Q9Z8","localizacao":"Sala Comum Escritorio Geral","sku_produto":"MSE-LOG-MXA-001","departamento":"Departamento de RH"}',
 '{"mac":"","sku":"MSE-LOG-MXA-001","status":"ATIVO","condicao":"OTIMO","numSerie":"Agri-Drh-Log-00-00-A","localizacao":"Sala Comum Escritorio Geral","observacoes":"","departamento":"Departamento de RH","dataAquisicao":"2026-04-24","funcionarioId":"Drh_04"}',
 '2026-07-06 14:16:23','REMOTE-IMPORT');

-- ---------- 201 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0201','ATUALIZACAO','TCD-LOG-MXK-002','Agri-Dcl-Log-00-00-A','Pom_11',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"OTIMO","localizacao":"Sala do Departamento Comercial (POM)","sku_produto":"TCD-LOG-MXK-002","departamento":"Departamento Finenceiro"}',
 '{"mac":"","sku":"TCD-LOG-MXK-002","status":"ATIVO","condicao":"OTIMO","numSerie":"Agri-Dcl-Log-00-00-A","localizacao":"Sala do Departamento Comercial (POM)","observacoes":"","departamento":"Departamento Financeiro","dataAquisicao":"2026-04-24","funcionarioId":"Pom_11"}',
 '2026-07-06 14:16:43','REMOTE-IMPORT');

-- ---------- 202 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0202','ATUALIZACAO','MSE-LOG-MXA-001','Agri-Daf-Log-00-03-A','Daf_06',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"OTIMO","num_serie":"2419LZ07B4T8","localizacao":"Departamento Financeiro e Contabilidade","sku_produto":"MSE-LOG-MXA-001","departamento":"Departamento Financeiro"}',
 '{"mac":"","sku":"MSE-LOG-MXA-001","status":"ATIVO","condicao":"OTIMO","numSerie":"Agri-Daf-Log-00-03-A","localizacao":"Departamento Financeiro e Contabilidade","observacoes":"","departamento":"Departamento Financeiro","dataAquisicao":"2026-04-24","funcionarioId":"Daf_06"}',
 '2026-07-06 14:17:14','REMOTE-IMPORT');

-- ---------- 203 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0203','CRIACAO','CMP-DEL-VOS-001','Agri-Dcl-Dell-00-01-A','Amz_02',
 NULL,
 NULL,
 '{"mac":"","sku":"CMP-DEL-VOS-001","status":"ATIVO","condicao":"BOM","numSerie":"Agri-Dcl-Dell-00-01-A","localizacao":"Armazém Geral","observacoes":"","departamento":"Departamento de Armazém","dataAquisicao":"2026-04-24","funcionarioId":"Amz_02"}',
 '2026-07-06 14:18:13','REMOTE-IMPORT');

-- ---------- 204 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0204','ATUALIZACAO','CMP-THI-LEN-001','Agri-Dpl-Leno-00-00-A','Dpl_01',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"BOM","localizacao":"Sala da Direção de Plantação","sku_produto":"CMP-THI-LEN-001","departamento":"Departamento de Plantação"}',
 '{"mac":"","sku":"CMP-THI-LEN-001","status":"ATIVO","condicao":"BOM","numSerie":"Agri-Dpl-Leno-00-00-A","localizacao":"Sala da Direção de Plantação","observacoes":"","departamento":"Departamento de Plantação","dataAquisicao":"2026-04-24","funcionarioId":"Dpl_01"}',
 '2026-07-06 14:18:57','REMOTE-IMPORT');

-- ---------- 205 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0205','CRIACAO','CMP-HPX-RTL-001','Agri-Dpl-Hp-00-00-A','Dpl_01',
 NULL,
 NULL,
 '{"mac":"","sku":"CMP-HPX-RTL-001","status":"ATIVO","condicao":"BOM","numSerie":"Agri-Dpl-Hp-00-00-A","localizacao":"","observacoes":"","departamento":"Departamento de HSA","dataAquisicao":"2026-04-24","funcionarioId":"Dpl_01"}',
 '2026-07-06 14:19:27','REMOTE-IMPORT');

-- ---------- 206 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0206','ATUALIZACAO','MSE-LOG-MXA-001','Agri-Dcl-Log-00-01-A','Amz_02',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"OTIMO","num_serie":"2419LZ97BTQ8","localizacao":"Armazém Geral","sku_produto":"MSE-LOG-MXA-001","departamento":"Departamento de Armazém"}',
 '{"mac":"","sku":"MSE-LOG-MXA-001","status":"ATIVO","condicao":"OTIMO","numSerie":"Agri-Dcl-Log-00-01-A","localizacao":"Armazem Geral","observacoes":"","departamento":"Departamento Compras e Logística","dataAquisicao":"2026-04-24","funcionarioId":"Amz_02"}',
 '2026-07-06 14:21:00','REMOTE-IMPORT');

-- ---------- 207 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0207','ATUALIZACAO','TCD-LOG-MXK-001','Agri-Daf-Log-00-04-A','Daf_02',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"OTIMO","num_serie":"2019D17095","localizacao":"Departamento Financeiro e Contabilidade","sku_produto":"TCD-LOG-MXK-001","departamento":"Departamento Financeiro"}',
 '{"mac":"","sku":"TCD-LOG-MXK-001","status":"ATIVO","condicao":"OTIMO","numSerie":"Agri-Daf-Log-00-04-A","localizacao":"Departamento Financeiro e Contabilidade","observacoes":"","departamento":"Departamento Financeiro","dataAquisicao":"2026-04-24","funcionarioId":"Daf_02"}',
 '2026-07-06 14:22:37','REMOTE-IMPORT');

-- ---------- 208 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0208','ATUALIZACAO','MON-DEL-GYB-001','Agri-Daf-Dell-00-01-A','Daf_05',
 NULL,
 '{"mac":"NO1","sku":"MON-DEL-GYB-001","status":"EM_USO","condicao":"BOM","numSerie":"CN-01M2XX-QDC00-874-3841-A05","localizacao":"Departamento Financeiro e Contabilidade","observacoes":"","departamento":"Departamento Financeiro","dataAquisicao":"2026-06-03","funcionarioId":"Daf_05"}',
 '{"mac":"","sku":"MON-DEL-GYB-001","status":"EM_USO","condicao":"BOM","numSerie":"Agri-Daf-Dell-00-01-A","localizacao":"Sala da contabilidade Escritorio Plantação","observacoes":"","departamento":"Departamento Financeiro","dataAquisicao":"2026-06-03","funcionarioId":"Daf_05"}',
 '2026-07-06 14:23:05','REMOTE-IMPORT');

-- ---------- 209 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0209','ATUALIZACAO','CMP-DEL-VES-001','Agri-Dcl-Dell-00-02-A','Amz_04',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"CRITICO","num_serie":"C86FFL2","localizacao":"Armazém Geral","observacoes":"A tampa do computador esta partida, e no teclado","sku_produto":"CMP-DEL-VES-001","departamento":"Departamento de Armazém"}',
 '{"mac":"","sku":"CMP-DEL-VES-001","status":"ATIVO","condicao":"CRITICO","numSerie":"Agri-Dcl-Dell-00-02-A","localizacao":"Armazém Geral","observacoes":"A tampa do computador esta partida, e no teclado","departamento":"Departamento de Armazém","dataAquisicao":"2026-04-24","funcionarioId":"Amz_04"}',
 '2026-07-06 14:23:58','REMOTE-IMPORT');

-- ---------- 210 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0210','ATUALIZACAO','MON-DEL-E24-005','Agri-Daf-Dell-00-02-A','Daf_07',
 NULL,
 '{"mac":"TER","sku":"MON-DEL-E24-005","status":"EM_USO","condicao":"BOM","numSerie":"CN-06JT9C-FCC00-571-D4KX-A01","localizacao":"Departamento Financeiro e Contabilidade","observacoes":"","departamento":"Departamento Financeiro","dataAquisicao":"2026-06-03","funcionarioId":"Daf_07"}',
 '{"mac":"","sku":"MON-DEL-E24-005","status":"EM_USO","condicao":"BOM","numSerie":"Agri-Daf-Dell-00-02-A","localizacao":"Departamento Financeiro e Contabilidade","observacoes":"","departamento":"Departamento Financeiro","dataAquisicao":"2026-06-03","funcionarioId":"Daf_07"}',
 '2026-07-06 14:24:48','REMOTE-IMPORT');

-- ---------- 211 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0211','CRIACAO','MON-DEL-CN0-001','Agri-Daf-Dell-00-03-A','Daf_02',
 NULL,
 NULL,
 '{"mac":"","sku":"MON-DEL-CN0-001","status":"ATIVO","condicao":"BOM","numSerie":"Agri-Daf-Dell-00-03-A","localizacao":"Escritório de Manutenção (POM)","observacoes":"","departamento":"Departamento Industrial (POM)","dataAquisicao":"2026-04-24","funcionarioId":"Daf_02"}',
 '2026-07-06 14:25:16','REMOTE-IMPORT');

-- ---------- 212 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0212','ATUALIZACAO','MON-DEL-CN0-001','Agri-Daf-Dell-00-04-A','Daf_02',
 NULL,
 '{"mac":"","sku":"MON-DEL-CN0-001","status":"ATIVO","condicao":"BOM","numSerie":"Agri-Daf-Dell-00-03-A","localizacao":"Escritório de Manutenção (POM)","observacoes":"","departamento":"Departamento Industrial (POM)","dataAquisicao":"2026-04-24","funcionarioId":"Daf_02"}',
 '{"mac":"","sku":"MON-DEL-CN0-001","status":"ATIVO","condicao":"BOM","numSerie":"Agri-Daf-Dell-00-04-A","localizacao":"Escritório de Manutenção (POM)","observacoes":"","departamento":"Departamento Industrial (POM)","dataAquisicao":"2026-04-24","funcionarioId":"Daf_02"}',
 '2026-07-06 14:25:42','REMOTE-IMPORT');

-- ---------- 213 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0213','ATUALIZACAO','MON-DEL-CN0-001','Agri-Pom-Dell-00-01-A','Pom_05',
 NULL,
 '{"mac":"","sku":"MON-DEL-CN0-001","status":"ATIVO","condicao":"CRITICO","numSerie":"Agri-Pom-Dell-00-00-A","localizacao":"Escritório de Manutenção (POM)","observacoes":"Linhas coloridas no visor","departamento":"Departamento Industrial (POM)","dataAquisicao":"2026-04-24","funcionarioId":"Pom_05"}',
 '{"mac":"","sku":"MON-DEL-CN0-001","status":"ATIVO","condicao":"CRITICO","numSerie":"Agri-Pom-Dell-00-01-A","localizacao":"Escritório de Manutenção (POM)","observacoes":"Linhas coloridas no visor","departamento":"Departamento Industrial (POM)","dataAquisicao":"2026-04-24","funcionarioId":"Pom_05"}',
 '2026-07-06 14:26:11','REMOTE-IMPORT');

-- ---------- 214 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0214','ATUALIZACAO','MON-DEL-4S8-001','Agri-Drh-Dell-00-00-A','Drh_06',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"BOM","num_serie":"CN-01M2XX-QDC00-874-1YTI-A05","localizacao":"Sala Comum Escritorio Geral","sku_produto":"MON-DEL-4S8-001","departamento":"Departamento de RH"}',
 '{"mac":"","sku":"MON-DEL-4S8-001","status":"ATIVO","condicao":"BOM","numSerie":"Agri-Drh-Dell-00-00-A","localizacao":"Sala Comum Escritorio Geral","observacoes":"","departamento":"Departamento de RH","dataAquisicao":"2026-04-24","funcionarioId":"Drh_06"}',
 '2026-07-06 14:26:38','REMOTE-IMPORT');

-- ---------- 215 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0215','ATUALIZACAO','CPU-DEL-OPT-001','Agri-Drh-Dell-00-01-A','Drh_05',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"BOM","num_serie":"1M4M7L2","localizacao":"Sala do Responsável de RH","sku_produto":"CPU-DEL-OPT-001","departamento":"Departamento de RH"}',
 '{"mac":"","sku":"CPU-DEL-OPT-001","status":"ATIVO","condicao":"BOM","numSerie":"Agri-Drh-Dell-00-01-A","localizacao":"Sala Comum Escritorio Geral","observacoes":"","departamento":"Departamento de RH","dataAquisicao":"2026-04-24","funcionarioId":"Drh_05"}',
 '2026-07-06 14:38:41','REMOTE-IMPORT');

-- ---------- 216 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0216','ATUALIZACAO','CPU-DEL-OPT-001','Agri-Pom-Dell-00-02-A','Pom_03',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"BOM","num_serie":"2521447814","localizacao":"Balança (POM)","sku_produto":"CPU-DEL-OPT-001","departamento":"Departamento Industrial (POM)"}',
 '{"mac":"","sku":"CPU-DEL-OPT-001","status":"ATIVO","condicao":"BOM","numSerie":"Agri-Pom-Dell-00-02-A","localizacao":"Balança (POM)","observacoes":"","departamento":"Departamento Industrial (POM)","dataAquisicao":"2018-09-02","funcionarioId":"Pom_03"}',
 '2026-07-06 14:39:02','REMOTE-IMPORT');

-- ---------- 217 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0217','ATUALIZACAO','CPU-DEL-OPT-001','Agri-Daf-Dell-00-05-A','Daf_02',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"BOM","num_serie":"2763312518","localizacao":"Departamento Financeiro e Contabilidade","sku_produto":"CPU-DEL-OPT-001","departamento":"Departamento Financeiro"}',
 '{"mac":"","sku":"CPU-DEL-OPT-001","status":"ATIVO","condicao":"BOM","numSerie":"Agri-Daf-Dell-00-05-A","localizacao":"Departamento Financeiro e Contabilidade","observacoes":"","departamento":"Departamento Financeiro","dataAquisicao":"2026-04-24","funcionarioId":"Daf_02"}',
 '2026-07-06 14:39:22','REMOTE-IMPORT');

-- ---------- 218 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0218','ATUALIZACAO','IMP-HPX-HPD-001','Agri-Inf-Hp-00-00-A','Inf_02',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"BOM","localizacao":"Sala da infraestrutura","sku_produto":"IMP-HPX-HPD-001","departamento":"Departamento de Infraestruturas"}',
 '{"mac":"","sku":"IMP-HPX-HPD-001","status":"ATIVO","condicao":"BOM","numSerie":"Agri-Inf-Hp-00-00-A","localizacao":"Sala da infraestrutura","observacoes":"","departamento":"Departamento de Infraestruturas","dataAquisicao":"2026-04-24","funcionarioId":"Inf_02"}',
 '2026-07-06 14:39:46','REMOTE-IMPORT');

-- ---------- 219 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0219','ATUALIZACAO','CPU-DEL-OPT-001','Agri-Drh-Dell-00-02-A','Drh_06',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"BOM","num_serie":"95P7DL2","localizacao":"Sala Comum Escritorio Geral","sku_produto":"CPU-DEL-OPT-001","departamento":"Departamento de RH"}',
 '{"mac":"","sku":"CPU-DEL-OPT-001","status":"ATIVO","condicao":"BOM","numSerie":"Agri-Drh-Dell-00-02-A","localizacao":"Sala Comum Escritorio Geral","observacoes":"","departamento":"Departamento de RH","dataAquisicao":"2026-04-24","funcionarioId":"Drh_06"}',
 '2026-07-06 14:40:07','REMOTE-IMPORT');

-- ---------- 220 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0220','CRIACAO','IMP-HPX-HPD-001','Agri-Drh-Hp-00-00-A','Drh_06',
 NULL,
 NULL,
 '{"mac":"","sku":"IMP-HPX-HPD-001","status":"ATIVO","condicao":"BOM","numSerie":"Agri-Drh-Hp-00-00-A","localizacao":"Sala do Responsável de Infraestruturas","observacoes":"","departamento":"Departamento de Infraestruturas","dataAquisicao":"2026-04-24","funcionarioId":"Drh_06"}',
 '2026-07-06 14:40:29','REMOTE-IMPORT');

-- ---------- 221 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0221','ATUALIZACAO','IMP-HPX-HPD-001','Agri-Drh-Hp-00-01-A','Drh_06',
 NULL,
 '{"mac":"","sku":"IMP-HPX-HPD-001","status":"ATIVO","condicao":"BOM","numSerie":"Agri-Drh-Hp-00-00-A","localizacao":"Sala do Responsável de Infraestruturas","observacoes":"","departamento":"Departamento de Infraestruturas","dataAquisicao":"2026-04-24","funcionarioId":"Drh_06"}',
 '{"mac":"","sku":"IMP-HPX-HPD-001","status":"ATIVO","condicao":"BOM","numSerie":"Agri-Drh-Hp-00-01-A","localizacao":"Sala do Responsável de Infraestruturas","observacoes":"","departamento":"Departamento de Infraestruturas","dataAquisicao":"2026-04-24","funcionarioId":"Drh_06"}',
 '2026-07-06 14:43:53','REMOTE-IMPORT');

-- ---------- 222 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0222','ATUALIZACAO','IMP-HPX-HPD-001','Agri-Inf-Hp-00-01-A','Inf_02',
 NULL,
 '{"mac":"","sku":"IMP-HPX-HPD-001","status":"ATIVO","condicao":"BOM","numSerie":"Agri-Inf-Hp-00-00-A","localizacao":"Sala da infraestrutura","observacoes":"","departamento":"Departamento de Infraestruturas","dataAquisicao":"2026-04-24","funcionarioId":"Inf_02"}',
 '{"mac":"","sku":"IMP-HPX-HPD-001","status":"ATIVO","condicao":"BOM","numSerie":"Agri-Inf-Hp-00-01-A","localizacao":"Sala da infraestrutura","observacoes":"","departamento":"Departamento de Infraestruturas","dataAquisicao":"2018-07-10","funcionarioId":"Inf_02"}',
 '2026-07-06 14:44:30','REMOTE-IMPORT');

-- ---------- 223 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0223','ATUALIZACAO','CMP-HPX-250-001','Agri-Dcl-Hp-00-00-A','Amz_03',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"BOM","num_serie":"CND1014LJG","localizacao":"Armazém (POM)","sku_produto":"CMP-HPX-250-001","departamento":"Departamento de Armazém","endereco_mac":"18:47:3D:D7:23:13"}',
 '{"mac":"18:47:3D:D7:23:13","sku":"CMP-HPX-250-001","status":"ATIVO","condicao":"BOM","numSerie":"Agri-Dcl-Hp-00-00-A","localizacao":"Armazém (POM)","observacoes":"","departamento":"Departamento de Armazém","dataAquisicao":"2026-04-24","funcionarioId":"Amz_03"}',
 '2026-07-06 14:44:57','REMOTE-IMPORT');

-- ---------- 224 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0224','ATUALIZACAO','CMP-HPX-INT-001','Agri-Dg-Hp-00-00-A','Dge_01',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"BOM","localizacao":"Sala da Direção Geral","sku_produto":"CMP-HPX-INT-001","departamento":"Direção Geral"}',
 '{"mac":"","sku":"CMP-HPX-INT-001","status":"ATIVO","condicao":"BOM","numSerie":"Agri-Dg-Hp-00-00-A","localizacao":"Sala da Direção Geral","observacoes":"","departamento":"Direção Geral","dataAquisicao":"2026-04-24","funcionarioId":"Dge_01"}',
 '2026-07-06 14:45:31','REMOTE-IMPORT');

-- ---------- 225 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0225','ATUALIZACAO','CMP-HPX-INT-001','Agri-Dge-Hp-00-00-A','Dge_01',
 NULL,
 '{"mac":"","sku":"CMP-HPX-INT-001","status":"ATIVO","condicao":"BOM","numSerie":"Agri-Dg-Hp-00-00-A","localizacao":"Sala da Direção Geral","observacoes":"","departamento":"Direção Geral","dataAquisicao":"2026-04-24","funcionarioId":"Dge_01"}',
 '{"mac":"","sku":"CMP-HPX-INT-001","status":"ATIVO","condicao":"BOM","numSerie":"Agri-Dge-Hp-00-00-A","localizacao":"Sala da Direção Geral","observacoes":"","departamento":"Direção Geral","dataAquisicao":"2024-07-12","funcionarioId":"Dge_01"}',
 '2026-07-06 14:46:06','REMOTE-IMPORT');

-- ---------- 226 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0226','ATUALIZACAO','MON-DEL-P24-001','VN-0WYGX3-QDV00-47T-1DOL-A03','Dcl_03',
 NULL,
 '{"mac":"rt","sku":"MON-DEL-P24-001","status":"EM_USO","condicao":"OTIMO","numSerie":"VN-0WYGX3-QDV00-47T-1DOL-A03","localizacao":"Sala de contabilidade Escritorio plantação","observacoes":"","departamento":"Departamento Compras e Logística","dataAquisicao":"2026-06-05","funcionarioId":"Dcl_03"}',
 '{"mac":"","sku":"MON-DEL-P24-001","status":"EM_USO","condicao":"OTIMO","numSerie":"VN-0WYGX3-QDV00-47T-1DOL-A03","localizacao":"Sala de contabilidade Escritorio plantação","observacoes":"","departamento":"Departamento Compras e Logística","dataAquisicao":"2026-06-05","funcionarioId":"Dcl_03"}',
 '2026-07-07 08:55:16','REMOTE-IMPORT');

-- ---------- 227 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0227','ATUALIZACAO','CMP-HPX-15F-001','Agri-Daf-Dell-00-06-A','Daf_01',
 NULL,
 '{"mac":"E8:B0:C5:5C:91:F8","sku":"CMP-HPX-15F-001","status":"EM_USO","condicao":"BOM","numSerie":"1h84503rts","localizacao":"Sala da Direção Financeira","observacoes":"","departamento":"Departamento Financeiro","dataAquisicao":"2026-04-24","funcionarioId":"Daf_01"}',
 '{"mac":"E8:B0:C5:5C:91:F8","sku":"CMP-HPX-15F-001","status":"EM_USO","condicao":"BOM","numSerie":"Agri-Daf-Dell-00-06-A","localizacao":"Sala da Direção Financeira","observacoes":"","departamento":"Departamento Financeiro","dataAquisicao":"2026-04-24","funcionarioId":"Daf_01"}',
 '2026-07-07 11:26:53','REMOTE-IMPORT');

-- ---------- 228 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0228','CRIACAO','IMP-HPX-COL-002','Agri-Daf-Dell-00-07-A','Daf_01',
 NULL,
 NULL,
 '{"mac":"","sku":"IMP-HPX-COL-002","status":"ATIVO","condicao":"BOM","numSerie":"Agri-Daf-Dell-00-07-A","localizacao":"Escritório Geral","observacoes":"","departamento":"Departamento Financeiro","dataAquisicao":"2026-04-24","funcionarioId":"Daf_01"}',
 '2026-07-07 12:40:45','REMOTE-IMPORT');

-- ---------- 229 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0229','ATUALIZACAO','TCD-EQU-NO2-001','Agri-Daf-Dell-00-08-A','Daf_04',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"BOM","localizacao":"Departamento Financeiro e Contabilidade","sku_produto":"TCD-EQU-NO2-001","departamento":"Departamento Financeiro"}',
 '{"mac":"","sku":"TCD-EQU-NO2-001","status":"ATIVO","condicao":"BOM","numSerie":"Agri-Daf-Dell-00-08-A","localizacao":"Departamento Financeiro e Contabilidade","observacoes":"","departamento":"Departamento Financeiro","dataAquisicao":"2026-04-24","funcionarioId":"Daf_04"}',
 '2026-07-08 15:05:23','REMOTE-IMPORT');

-- ---------- 230 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0230','CRIACAO','CMP-DEL-ESS-017','Agri-Dinfo-Dell-00-00-A','Dinfo_001',
 NULL,
 NULL,
 '{"mac":"","sku":"CMP-DEL-ESS-017","status":"EM_USO","condicao":"OTIMO","numSerie":"Agri-Dinfo-Dell-00-00-A","localizacao":"Sala de Informática (POM)","observacoes":"","departamento":"Departamento Informático","dataAquisicao":"2026-05-09","funcionarioId":"Dinfo_001"}',
 '2026-07-09 12:39:49','REMOTE-IMPORT');

-- ---------- 231 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0231','CRIACAO','CMP-HPX-15D-001','Agri-Pom-Dell-00-03-A','Dinfo_001',
 NULL,
 NULL,
 '{"mac":"","sku":"CMP-HPX-15D-001","status":"EM_USO","condicao":"BOM","numSerie":"Agri-Pom-Dell-00-03-A","localizacao":"Sala do dep informatico","observacoes":"","departamento":"Departamento Industrial","dataAquisicao":"2026-04-24","funcionarioId":"Dinfo_001"}',
 '2026-07-09 15:07:10','REMOTE-IMPORT');

-- ---------- 232 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0232','ATUALIZACAO','MON-DEL-P24-001','VN-0WYGX3-QDV00-47T-1DOL-A03','Dcl_03',
 NULL,
 '{"mac":"","sku":"MON-DEL-P24-001","status":"EM_USO","condicao":"OTIMO","numSerie":"VN-0WYGX3-QDV00-47T-1DOL-A03","localizacao":"Sala de contabilidade Escritorio plantação","observacoes":"","departamento":"Departamento Compras e Logística","dataAquisicao":"2026-06-05","funcionarioId":"Dcl_03"}',
 '{"mac":"","sku":"MON-DEL-P24-001","status":"EM_USO","condicao":"OTIMO","numSerie":"VN-0WYGX3-QDV00-47T-1DOL-A03","localizacao":"Sala de contabilidade Escritorio plantação","observacoes":"","departamento":"Departamento Compras e Logística","dataAquisicao":"2026-06-05","funcionarioId":"Dcl_03"}',
 '2026-07-11 09:49:55','REMOTE-IMPORT');

-- ---------- 233 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0233','ATUALIZACAO','TCD-LOG-MXK-002','201DJ7094','Drh_01',
 NULL,
 '{"mac":"G","sku":"TCD-LOG-MXK-002","status":"EM_USO","condicao":"OTIMO","numSerie":"201DJ7094","localizacao":"Sala da contabilidade escritorio plantação","observacoes":"","departamento":"Departamento Compras e Logística","dataAquisicao":"2026-06-05","funcionarioId":"Drh_01"}',
 '{"mac":"","sku":"TCD-LOG-MXK-002","status":"EM_USO","condicao":"OTIMO","numSerie":"201DJ7094","localizacao":"Sala da contabilidade escritorio plantação","observacoes":"","departamento":"Departamento Compras e Logística","dataAquisicao":"2026-06-05","funcionarioId":"Drh_01"}',
 '2026-08-03 10:59:44','REMOTE-IMPORT');

-- ---------- 234 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0234','ATUALIZACAO','TCD-LOG-MXK-002','201DJ7094','Drh_01',
 NULL,
 '{"mac":"","sku":"TCD-LOG-MXK-002","status":"EM_USO","condicao":"OTIMO","numSerie":"201DJ7094","localizacao":"Sala da contabilidade escritorio plantação","observacoes":"","departamento":"Departamento Compras e Logística","dataAquisicao":"2026-06-05","funcionarioId":"Drh_01"}',
 '{"mac":"","sku":"TCD-LOG-MXK-002","status":"EM_USO","condicao":"OTIMO","numSerie":"201DJ7094","localizacao":"Sala da contabilidade escritorio plantação","observacoes":"","departamento":"Departamento Compras e Logística","dataAquisicao":"2026-06-05","funcionarioId":"Drh_01"}',
 '2026-08-03 11:11:21','REMOTE-IMPORT');

-- ---------- 235 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0235','ATUALIZACAO','MSE-LOG-MXA-001','Agri-Dcl-Logi-00-00-A','Dcl_03',
 NULL,
 '{"mac":"uy","sku":"MSE-LOG-MXA-001","status":"EM_USO","condicao":"OTIMO","numSerie":"2417LZ02PRE8","localizacao":"Sala de contabilidade Escritorio plantação","observacoes":"","departamento":"Departamento Compras e Logística","dataAquisicao":"2026-06-05","funcionarioId":"Dcl_03"}',
 '{"mac":"","sku":"MSE-LOG-MXA-001","status":"EM_USO","condicao":"BOM","numSerie":"Agri-Dcl-Logi-00-00-A","localizacao":"Sala de contabilidade Escritorio plantação","observacoes":"","departamento":"Departamento Compras e Logística","dataAquisicao":"2026-06-05","funcionarioId":"Dcl_03"}',
 '2026-08-03 11:19:47','REMOTE-IMPORT');

-- ---------- 236 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0236','ATUALIZACAO','MON-DEL-E24-004','Agri-Daf-Dell-00-09-A','Daf_09',
 NULL,
 '{"mac":"T","sku":"MON-DEL-E24-004","status":"EM_USO","condicao":"OTIMO","numSerie":"CN-06JT9C-FCC00-571-D4KX-A01-DAF","localizacao":"Sala de tesouraria do escritorio plantação","observacoes":"","departamento":"Departamento Financeiro","dataAquisicao":"2026-06-05","funcionarioId":"Daf_09"}',
 '{"mac":"","sku":"MON-DEL-E24-004","status":"EM_USO","condicao":"OTIMO","numSerie":"Agri-Daf-Dell-00-09-A","localizacao":"Sala de tesouraria do escritorio plantação","observacoes":"","departamento":"Departamento Financeiro","dataAquisicao":"2026-06-05","funcionarioId":"Daf_09"}',
 '2026-08-03 11:24:19','REMOTE-IMPORT');

-- ---------- 237 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0237','ATUALIZACAO','MON-AOC-M24-006','Agri-Azm_Aoc_00-01-A','Amz_02',
 NULL,
 '{"mac":"","sku":"MON-AOC-M24-006","status":"EM_USO","condicao":"BOM","numSerie":"Agri-Azm_Aoc_00-01-A","localizacao":"Armazem Geral","observacoes":"","departamento":"Departamento Compras e Logística","dataAquisicao":"2026-06-09","funcionarioId":"Amz_02"}',
 '{"mac":"","sku":"MON-AOC-M24-006","status":"EM_USO","condicao":"BOM","numSerie":"Agri-Azm_Aoc_00-01-A","localizacao":"Armazem Geral","observacoes":"","departamento":"Departamento Compras e Logística","dataAquisicao":"2026-06-09","funcionarioId":"Amz_02"}',
 '2026-08-03 11:26:03','REMOTE-IMPORT');

-- ---------- 238 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0238','ATUALIZACAO','TCD-LOG-MXK-002','Agri-Dcl-Logi-00-01-A','Drh_01',
 NULL,
 '{"mac":"","sku":"TCD-LOG-MXK-002","status":"EM_USO","condicao":"OTIMO","numSerie":"201DJ7094","localizacao":"Sala da contabilidade escritorio plantação","observacoes":"","departamento":"Departamento Compras e Logística","dataAquisicao":"2026-06-05","funcionarioId":"Drh_01"}',
 '{"mac":"","sku":"TCD-LOG-MXK-002","status":"EM_USO","condicao":"OTIMO","numSerie":"Agri-Dcl-Logi-00-01-A","localizacao":"Sala da contabilidade escritorio plantação","observacoes":"","departamento":"Departamento Compras e Logística","dataAquisicao":"2026-06-05","funcionarioId":"Drh_01"}',
 '2026-08-03 11:27:27','REMOTE-IMPORT');

-- ---------- 239 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0239','ATUALIZACAO','MON-DEL-E24-004','Agri-Dcl-Dell-00-03-A','Drh_01',
 NULL,
 '{"mac":"F","sku":"MON-DEL-E24-004","status":"EM_USO","condicao":"OTIMO","numSerie":"CN-06JT9C-FCC00-571-D4MX-A01","localizacao":"Sala da contabilidade escritorio plantação","observacoes":"","departamento":"Departamento Compras e Logística","dataAquisicao":"2026-06-05","funcionarioId":"Drh_01"}',
 '{"mac":"","sku":"MON-DEL-E24-004","status":"EM_USO","condicao":"OTIMO","numSerie":"Agri-Dcl-Dell-00-03-A","localizacao":"Sala da contabilidade escritorio plantação","observacoes":"","departamento":"Departamento Compras e Logística","dataAquisicao":"2026-06-05","funcionarioId":"Drh_01"}',
 '2026-08-03 11:27:56','REMOTE-IMPORT');

-- ---------- 240 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0240','ATUALIZACAO','CMP-DEL-ESS-017','Agri-Dcl-Dell-00-04-A','Dcl_03',
 NULL,
 '{"mac":"44:f7:9f:d1:9a:35","sku":"CMP-DEL-ESS-017","status":"EM_USO","condicao":"OTIMO","numSerie":"GD991HG4","localizacao":"Sala de contabilidade Escritorio plantação","observacoes":"","departamento":"Departamento Compras e Logística","dataAquisicao":"2026-06-05","funcionarioId":"Dcl_03"}',
 '{"mac":"44:f7:9f:d1:9a:35","sku":"CMP-DEL-ESS-017","status":"EM_USO","condicao":"OTIMO","numSerie":"Agri-Dcl-Dell-00-04-A","localizacao":"Sala de contabilidade Escritorio plantação","observacoes":"","departamento":"Departamento Compras e Logística","dataAquisicao":"2026-06-05","funcionarioId":"Dcl_03"}',
 '2026-08-03 11:28:20','REMOTE-IMPORT');

-- ---------- 241 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0241','ATUALIZACAO','CMP-DEL-ESS-017','Agri-Daf-Dell-00-10-A','Daf_07',
 NULL,
 '{"mac":"44:f7:9f:d1:99:f3","sku":"CMP-DEL-ESS-017","status":"EM_USO","condicao":"OTIMO","numSerie":"P112FD13","localizacao":"Departamento Financeiro e Contabilidade","observacoes":"","departamento":"Departamento Financeiro","dataAquisicao":"2026-06-03","funcionarioId":"Daf_07"}',
 '{"mac":"44:f7:9f:d1:99:f3","sku":"CMP-DEL-ESS-017","status":"EM_USO","condicao":"OTIMO","numSerie":"Agri-Daf-Dell-00-10-A","localizacao":"Departamento Financeiro e Contabilidade","observacoes":"","departamento":"Departamento Financeiro","dataAquisicao":"2026-06-03","funcionarioId":"Daf_07"}',
 '2026-08-03 11:28:37','REMOTE-IMPORT');

-- ---------- 242 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0242','ATUALIZACAO','TCD-LOG-MXK-001','Agri-Daf-Logi-00-00-A','Daf_05',
 NULL,
 '{"mac":"NO2","sku":"TCD-LOG-MXK-001","status":"EM_USO","condicao":"BOM","numSerie":"2019DJ7094 ITFRCPLOY","localizacao":"Departamento Financeiro e Contabilidade","observacoes":"","departamento":"Departamento Financeiro","dataAquisicao":"2026-06-03","funcionarioId":"Daf_05"}',
 '{"mac":"","sku":"TCD-LOG-MXK-001","status":"EM_USO","condicao":"BOM","numSerie":"Agri-Daf-Logi-00-00-A","localizacao":"Sala da contabilidade Escritorio Plantação","observacoes":"","departamento":"Departamento Financeiro","dataAquisicao":"2026-06-03","funcionarioId":"Daf_05"}',
 '2026-08-03 11:29:03','REMOTE-IMPORT');

-- ---------- 243 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0243','ATUALIZACAO','CMP-HPX-15D-001','Agri-Pom-Dell-00-03-A','Dinfo_001',
 NULL,
 '{"mac":"","sku":"CMP-HPX-15D-001","status":"EM_USO","condicao":"BOM","numSerie":"Agri-Pom-Dell-00-03-A","localizacao":"Sala do dep informatico","observacoes":"","departamento":"Departamento Industrial","dataAquisicao":"2026-04-24","funcionarioId":"Dinfo_001"}',
 '{"mac":"","sku":"CMP-HPX-15D-001","status":"EM_USO","condicao":"BOM","numSerie":"Agri-Pom-Dell-00-03-A","localizacao":"Sala do dep informatico","observacoes":"","departamento":"Departamento Informático","dataAquisicao":"2026-04-24","funcionarioId":"Dinfo_001"}',
 '2026-08-03 12:20:17','REMOTE-IMPORT');

-- ---------- 244 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0244','ATUALIZACAO','CMP-DEL-VES-001','Agri-Daf-Dell-00-11-A','Daf_07',
 NULL,
 '{"mac":"74:40:bb:6d:1d:3b","sku":"CMP-DEL-VES-001","status":"EM_USO","condicao":"BOM","numSerie":"876FFL2","localizacao":"Departamento Financeiro e Contabilidade","observacoes":"Com problemas no caregador, causando pequenos curto circuitos.","departamento":"Departamento Financeiro"}',
 '{"mac":"74:40:bb:6d:1d:3b","sku":"CMP-DEL-VES-001","status":"EM_USO","condicao":"BOM","numSerie":"Agri-Daf-Dell-00-11-A","localizacao":"Departamento Financeiro e Contabilidade","observacoes":"Com problemas no caregador, causando pequenos curto circuitos.","departamento":"Departamento Financeiro","dataAquisicao":"2026-06-03","funcionarioId":"Daf_07"}',
 '2026-08-03 12:20:54','REMOTE-IMPORT');

-- ---------- 245 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0245','ATUALIZACAO','MON-DEL-E24-004','Agri-Daf-Dell-00-12-A','Daf_01',
 NULL,
 '{"mac":"KJ:","sku":"MON-DEL-E24-004","status":"EM_USO","condicao":"OTIMO","numSerie":"CN-06JT9C-FCC00-571-D4PX-A01","localizacao":"Sala da Direção Financeira","observacoes":"","departamento":"Departamento Financeiro","dataAquisicao":"2026-05-29","funcionarioId":"Daf_01"}',
 '{"mac":"","sku":"MON-DEL-E24-004","status":"EM_USO","condicao":"OTIMO","numSerie":"Agri-Daf-Dell-00-12-A","localizacao":"Sala da Direção Financeira","observacoes":"","departamento":"Departamento Financeiro","dataAquisicao":"2026-05-29","funcionarioId":"Daf_01"}',
 '2026-08-03 12:21:30','REMOTE-IMPORT');

-- ---------- 246 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0246','ATUALIZACAO','MON-DEL-CN0-001','Agri-Pom-Dell-00-01-A','Pom_05',
 NULL,
 '{"mac":"","sku":"MON-DEL-CN0-001","status":"ATIVO","condicao":"CRITICO","numSerie":"Agri-Pom-Dell-00-01-A","localizacao":"Escritório de Manutenção (POM)","observacoes":"Linhas coloridas no visor","departamento":"Departamento Industrial (POM)","dataAquisicao":"2026-04-24","funcionarioId":"Pom_05"}',
 '{"mac":"","sku":"MON-DEL-CN0-001","status":"ATIVO","condicao":"CRITICO","numSerie":"Agri-Pom-Dell-00-01-A","localizacao":"Escritório de Manutenção (POM)","observacoes":"Linhas coloridas no visor","departamento":"Departamento Industrial (POM)","dataAquisicao":"2026-04-24","funcionarioId":"Pom_05"}',
 '2026-08-03 12:22:37','REMOTE-IMPORT');

-- ---------- 247 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0247','ATUALIZACAO','MON-DEL-CN0-001','Agri-Pom-Dell-00-01-A','Pom_05',
 NULL,
 '{"mac":"","sku":"MON-DEL-CN0-001","status":"ATIVO","condicao":"CRITICO","numSerie":"Agri-Pom-Dell-00-01-A","localizacao":"Escritório de Manutenção (POM)","observacoes":"Linhas coloridas no visor","departamento":"Departamento Industrial (POM)","dataAquisicao":"2026-04-24","funcionarioId":"Pom_05"}',
 '{"mac":"","sku":"MON-DEL-CN0-001","status":"ATIVO","condicao":"BOM","numSerie":"Agri-Pom-Dell-00-01-A","localizacao":"Escritório de Manutenção (POM)","observacoes":"Linhas coloridas no visor","departamento":"Departamento Industrial (POM)","dataAquisicao":"2026-04-24","funcionarioId":"Pom_05"}',
 '2026-08-03 12:22:52','REMOTE-IMPORT');

-- ---------- 248 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0248','ATUALIZACAO','CPU-DEL-OPT-001','Agri-Daf-Dell-00-13-A','Daf_05',
 NULL,
 '{"mac":"24:2f:d0:d9:82:71","sku":"CPU-DEL-OPT-001","status":"EM_USO","condicao":"BOM","numSerie":"J5P7DL2","localizacao":"Departamento Financeiro e Contabilidade","observacoes":"Desliga sozinho.","departamento":"Departamento Financeiro","dataAquisicao":"2026-06-03","funcionarioId":"Daf_05"}',
 '{"mac":"24:2f:d0:d9:82:71","sku":"CPU-DEL-OPT-001","status":"EM_USO","condicao":"BOM","numSerie":"Agri-Daf-Dell-00-13-A","localizacao":"Sala da contabilidade Escritorio Plantação","observacoes":"Desliga sozinho.","departamento":"Departamento Financeiro","dataAquisicao":"2026-06-03","funcionarioId":"Daf_05"}',
 '2026-08-03 12:23:34','REMOTE-IMPORT');

-- ---------- 249 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0249','ATUALIZACAO','MON-DEL-CN0-001','Agri-Daf-Dell-00-14-A','Daf_02',
 NULL,
 '{"mac":"","sku":"MON-DEL-CN0-001","status":"ATIVO","condicao":"BOM","numSerie":"Agri-Daf-Dell-00-04-A","localizacao":"Escritório de Manutenção (POM)","observacoes":"","departamento":"Departamento Industrial (POM)","dataAquisicao":"2026-04-24","funcionarioId":"Daf_02"}',
 '{"mac":"","sku":"MON-DEL-CN0-001","status":"ATIVO","condicao":"BOM","numSerie":"Agri-Daf-Dell-00-14-A","localizacao":"Departamento Financeiro e Contabilidade","observacoes":"","departamento":"Departamento Financeiro","dataAquisicao":"2026-04-24","funcionarioId":"Daf_02"}',
 '2026-08-03 12:23:47','REMOTE-IMPORT');

-- ---------- 250 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0250','ATUALIZACAO','CMP-THI-LEN-001','Agri-Daf-Len-00-00-A','Daf_09',
 NULL,
 '{"mac":"28:c6:3f:be:a3:02","sku":"CMP-THI-LEN-001","status":"EM_USO","condicao":"REGULAR","numSerie":"tt56","localizacao":"Sala de tesouraria do escritorio plantação","observacoes":"","departamento":"Departamento Financeiro","dataAquisicao":"2026-06-05","funcionarioId":"Daf_09"}',
 '{"mac":"28:c6:3f:be:a3:02","sku":"CMP-THI-LEN-001","status":"EM_USO","condicao":"REGULAR","numSerie":"Agri-Daf-Len-00-00-A","localizacao":"Sala de tesouraria do escritorio plantação","observacoes":"","departamento":"Departamento Financeiro","dataAquisicao":"2026-06-05","funcionarioId":"Daf_09"}',
 '2026-08-03 12:31:38','REMOTE-IMPORT');

-- ---------- 251 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0251','ATUALIZACAO','MSE-COM-WIR-001','Agri-Daf-Tec-00-00-A','Daf_04',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"BOM","localizacao":"Departamento Financeiro e Contabilidade","sku_produto":"MSE-COM-WIR-001","departamento":"Departamento Financeiro"}',
 '{"mac":"","sku":"MSE-COM-WIR-001","status":"ATIVO","condicao":"BOM","numSerie":"Agri-Daf-Tec-00-00-A","localizacao":"Departamento Financeiro e Contabilidade","observacoes":"","departamento":"Departamento Financeiro","dataAquisicao":"2026-04-24","funcionarioId":"Daf_04"}',
 '2026-08-03 12:32:16','REMOTE-IMPORT');

-- ---------- 252 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0252','ATUALIZACAO','TCD-EQU-NO2-001','Agri-Daf-Tec-00-01-A','Daf_04',
 NULL,
 '{"mac":"","sku":"TCD-EQU-NO2-001","status":"ATIVO","condicao":"BOM","numSerie":"Agri-Daf-Dell-00-08-A","localizacao":"Departamento Financeiro e Contabilidade","observacoes":"","departamento":"Departamento Financeiro","dataAquisicao":"2026-04-24","funcionarioId":"Daf_04"}',
 '{"mac":"","sku":"TCD-EQU-NO2-001","status":"ATIVO","condicao":"BOM","numSerie":"Agri-Daf-Tec-00-01-A","localizacao":"Departamento Financeiro e Contabilidade","observacoes":"","departamento":"Departamento Financeiro","dataAquisicao":"2026-04-24","funcionarioId":"Daf_04"}',
 '2026-08-03 12:32:39','REMOTE-IMPORT');

-- ---------- 253 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0253','ATUALIZACAO','CMP-HPX-INT-001','Agri-Dge-Hp-00-01-A','Dge_01',
 NULL,
 '{"mac":"","sku":"CMP-HPX-INT-001","status":"ATIVO","condicao":"BOM","numSerie":"Agri-Dge-Hp-00-00-A","localizacao":"Sala da Direção Geral","observacoes":"","departamento":"Direção Geral","dataAquisicao":"2024-07-12","funcionarioId":"Dge_01"}',
 '{"mac":"","sku":"CMP-HPX-INT-001","status":"ATIVO","condicao":"BOM","numSerie":"Agri-Dge-Hp-00-01-A","localizacao":"Sala da Direção Geral","observacoes":"","departamento":"Direção Geral","dataAquisicao":"2026-04-24","funcionarioId":"Dge_01"}',
 '2026-08-03 12:33:07','REMOTE-IMPORT');

-- ---------- 254 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0254','ATUALIZACAO','CMP-THI-LEN-001','Agri-Dpl-Len-00-00-A','Dpl_01',
 NULL,
 '{"mac":"","sku":"CMP-THI-LEN-001","status":"ATIVO","condicao":"BOM","numSerie":"Agri-Dpl-Leno-00-00-A","localizacao":"Sala da Direção de Plantação","observacoes":"","departamento":"Departamento de Plantação","dataAquisicao":"2026-04-24","funcionarioId":"Dpl_01"}',
 '{"mac":"","sku":"CMP-THI-LEN-001","status":"ATIVO","condicao":"BOM","numSerie":"Agri-Dpl-Len-00-00-A","localizacao":"Sala da Direção de Plantação","observacoes":"","departamento":"Departamento de Plantação","dataAquisicao":"2026-04-24","funcionarioId":"Dpl_01"}',
 '2026-08-03 12:33:33','REMOTE-IMPORT');

-- ---------- 255 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0255','ATUALIZACAO','MON-DEL-P24-001','Agri-Dcl-Dell-00-05-A','Dcl_03',
 NULL,
 '{"mac":"","sku":"MON-DEL-P24-001","status":"EM_USO","condicao":"OTIMO","numSerie":"VN-0WYGX3-QDV00-47T-1DOL-A03","localizacao":"Sala de contabilidade Escritorio plantação","observacoes":"","departamento":"Departamento Compras e Logística","dataAquisicao":"2026-06-05","funcionarioId":"Dcl_03"}',
 '{"mac":"","sku":"MON-DEL-P24-001","status":"EM_USO","condicao":"OTIMO","numSerie":"Agri-Dcl-Dell-00-05-A","localizacao":"Sala de contabilidade Escritorio plantação","observacoes":"","departamento":"Departamento Compras e Logística","dataAquisicao":"2026-06-05","funcionarioId":"Dcl_03"}',
 '2026-08-03 12:34:01','REMOTE-IMPORT');

-- ---------- 256 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0256','ATUALIZACAO','TCD-LOG-MXK-002','Agri-Dcl-Logi-00-02-A','Dcl_03',
 NULL,
 '{"mac":"FD","sku":"TCD-LOG-MXK-002","status":"EM_USO","condicao":"BOM","numSerie":"2019DJ7094","localizacao":"Sala de contabilidade Escritorio plantação","observacoes":"","departamento":"Departamento Compras e Logística","dataAquisicao":"2026-06-05","funcionarioId":"Dcl_03"}',
 '{"mac":"","sku":"TCD-LOG-MXK-002","status":"EM_USO","condicao":"BOM","numSerie":"Agri-Dcl-Logi-00-02-A","localizacao":"Sala de contabilidade Escritorio plantação","observacoes":"","departamento":"Departamento Compras e Logística","dataAquisicao":"2026-06-05","funcionarioId":"Dcl_03"}',
 '2026-08-03 13:26:30','REMOTE-IMPORT');

-- ---------- 257 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0257','ATUALIZACAO','MSE-LOG-MXA-001','Agri-Dcl-Logi-00-03-A','Drh_01',
 NULL,
 '{"mac":"e","sku":"MSE-LOG-MXA-001","status":"EM_USO","condicao":"BOM","numSerie":"2405LZ034YP8","localizacao":"Sala da contabilidade escritorio plantação","observacoes":"","departamento":"Departamento Compras e Logística","dataAquisicao":"2026-04-24","funcionarioId":"Drh_01"}',
 '{"mac":"","sku":"MSE-LOG-MXA-001","status":"EM_USO","condicao":"BOM","numSerie":"Agri-Dcl-Logi-00-03-A","localizacao":"Sala da contabilidade escritorio plantação","observacoes":"","departamento":"Departamento Compras e Logística","dataAquisicao":"2026-04-24","funcionarioId":"Drh_01"}',
 '2026-08-03 13:27:11','REMOTE-IMPORT');

-- ---------- 258 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0258','ATUALIZACAO','CMP-ASU-F16-001','Agri-Dpl-Asus-00-00-A','Dpl_04',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"OTIMO","num_serie":"SBN0CV12586648D","localizacao":"0.0","sku_produto":"CMP-ASU-F16-001","departamento":"Departamento de Plantação"}',
 '{"mac":"","sku":"CMP-ASU-F16-001","status":"ATIVO","condicao":"OTIMO","numSerie":"Agri-Dpl-Asus-00-00-A","localizacao":"0.0","observacoes":"","departamento":"Departamento de Plantação","dataAquisicao":"2026-04-24","funcionarioId":"Dpl_04"}',
 '2026-08-03 13:27:57','REMOTE-IMPORT');

-- ---------- 259 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0259','ATUALIZACAO','MON-DEL-9Q8-001','Agri-Drh-Dell-00-03-A','Drh_05',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"BOM","num_serie":"CN-01M2XX - qdc00-874-1wvl-ao5","localizacao":"Sala do Responsável de RH","sku_produto":"MON-DEL-9Q8-001","departamento":"Departamento de RH"}',
 '{"mac":"","sku":"MON-DEL-9Q8-001","status":"ATIVO","condicao":"BOM","numSerie":"Agri-Drh-Dell-00-03-A","localizacao":"Sala Comum Escritorio Geral","observacoes":"","departamento":"Departamento de RH","dataAquisicao":"2026-04-24","funcionarioId":"Drh_05"}',
 '2026-08-03 13:28:26','REMOTE-IMPORT');

-- ---------- 260 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0260','ATUALIZACAO','TCD-DEL-ONN-001','Agri-Drh-Dell-00-04-A','Drh_05',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"BOM","num_serie":"CN-0NNC2P-L0300-7B0-07C8-A03","localizacao":"Sala do Responsável de RH","sku_produto":"TCD-DEL-ONN-001","departamento":"Departamento de RH"}',
 '{"mac":"","sku":"TCD-DEL-ONN-001","status":"ATIVO","condicao":"BOM","numSerie":"Agri-Drh-Dell-00-04-A","localizacao":"Sala Comum Escritorio Geral","observacoes":"","departamento":"Departamento de RH","dataAquisicao":"2026-04-24","funcionarioId":"Drh_05"}',
 '2026-08-03 13:28:53','REMOTE-IMPORT');

-- ---------- 261 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0261','ATUALIZACAO','MSE-HPX-7CH-001','Agri-Drh-Dell-00-05-A','Drh_05',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"BOM","num_serie":"CN-0DV0RH-L0300-B1A-0QR5","localizacao":"Sala do Responsável de RH","sku_produto":"MSE-HPX-7CH-001","departamento":"Departamento de RH"}',
 '{"mac":"","sku":"MSE-HPX-7CH-001","status":"ATIVO","condicao":"BOM","numSerie":"Agri-Drh-Dell-00-05-A","localizacao":"Sala Comum Escritorio Geral","observacoes":"","departamento":"Departamento de RH","dataAquisicao":"2026-04-24","funcionarioId":"Drh_05"}',
 '2026-08-03 13:29:37','REMOTE-IMPORT');

-- ---------- 262 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0262','ATUALIZACAO','CMP-DEL-INT-001','Agri-Dcm-Dell-00-00-A','Drc_01',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"BOM","num_serie":"9XXC8 A00","localizacao":"0.0","sku_produto":"CMP-DEL-INT-001","departamento":"0.0"}',
 '{"mac":"","sku":"CMP-DEL-INT-001","status":"ATIVO","condicao":"BOM","numSerie":"Agri-Dcm-Dell-00-00-A","localizacao":"0.0","observacoes":"","departamento":"Departamento de Comunicação","dataAquisicao":"2026-04-24","funcionarioId":"Drc_01"}',
 '2026-08-03 13:33:59','REMOTE-IMPORT');

-- ---------- 263 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0263','ATUALIZACAO','CMP-HPX-15D-001','Agri-Pom-Dell-00-03-A','Dinfo_001',
 NULL,
 '{"mac":"","sku":"CMP-HPX-15D-001","status":"EM_USO","condicao":"BOM","numSerie":"Agri-Pom-Dell-00-03-A","localizacao":"Sala do dep informatico","observacoes":"","departamento":"Departamento Informático","dataAquisicao":"2026-04-24","funcionarioId":"Dinfo_001"}',
 '{"mac":"","sku":"CMP-HPX-15D-001","status":"EM_USO","condicao":"BOM","numSerie":"Agri-Pom-Dell-00-03-A","localizacao":"Sala do dep informatico","observacoes":"","departamento":"Departamento Informático","dataAquisicao":"2026-04-24","funcionarioId":"Dinfo_001"}',
 '2026-08-03 15:29:04','REMOTE-IMPORT');

-- ---------- 264 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0264','ATUALIZACAO','CMP-HPX-15D-001','Agri-Pom-Dell-00-03-A','Dinfo_001',
 NULL,
 '{"mac":"","sku":"CMP-HPX-15D-001","status":"EM_USO","condicao":"BOM","numSerie":"Agri-Pom-Dell-00-03-A","localizacao":"Sala do dep informatico","observacoes":"","departamento":"Departamento Informático","dataAquisicao":"2026-04-24","funcionarioId":"Dinfo_001"}',
 '{"mac":"","sku":"CMP-HPX-15D-001","status":"EM_USO","condicao":"BOM","numSerie":"Agri-Pom-Dell-00-03-A","localizacao":"Sala do dep informatico","observacoes":"","departamento":"Departamento Informático","dataAquisicao":"2026-04-24","funcionarioId":"Dinfo_001"}',
 '2026-08-03 15:29:26','REMOTE-IMPORT');

-- ---------- 265 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0265','CRIACAO','MON-VIE-VS1-001','Agri-Dge-View-00-00-A','Dge_02',
 NULL,
 NULL,
 '{"mac":"","sku":"MON-VIE-VS1-001","status":"EM_USO","condicao":"BOM","numSerie":"Agri-Dge-View-00-00-A","localizacao":"Secretaria da Direção Geral","observacoes":"","departamento":"Direção Geral","dataAquisicao":"2026-04-24","funcionarioId":"Dge_02"}',
 '2026-08-04 10:53:01','REMOTE-IMPORT');

-- ---------- 266 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0266','ATUALIZACAO','IMP-BRO-DCP-001','Agri-Dofi-Broth-00-00-A','Ofi_02',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"BOM","num_serie":"E78284L1N890853","localizacao":"Sala do Responsável de Oficina","sku_produto":"IMP-BRO-DCP-001","departamento":"Departamento de Oficina"}',
 '{"mac":"","sku":"IMP-BRO-DCP-001","status":"ATIVO","condicao":"BOM","numSerie":"Agri-Dofi-Broth-00-00-A","localizacao":"Sala do Responsável de Oficina","observacoes":"","departamento":"Departamento de Oficina","dataAquisicao":"2026-04-24","funcionarioId":"Ofi_02"}',
 '2026-08-04 10:54:45','REMOTE-IMPORT');

-- ---------- 267 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0267','CRIACAO','MON-LGX-19M-001','Agri-DInfa-Lg-00-00-A','Inf_01',
 NULL,
 NULL,
 '{"mac":"","sku":"MON-LGX-19M-001","status":"EM_USO","condicao":"BOM","numSerie":"Agri-DInfa-Lg-00-00-A","localizacao":"Sala do Responsável de Infraestruturas","observacoes":"","departamento":"Departamento de Infraestruturas","dataAquisicao":"2026-04-24","funcionarioId":"Inf_01"}',
 '2026-08-04 10:56:11','REMOTE-IMPORT');

-- ---------- 268 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0268','CRIACAO','NET-TPL-TLW-001','Agri-Pom-Tplink-00-00-A','Pom_016',
 NULL,
 NULL,
 '{"mac":"","sku":"NET-TPL-TLW-001","status":"ATIVO","condicao":"BOM","numSerie":"Agri-Pom-Tplink-00-00-A","localizacao":"Sala de Informática (POM)","observacoes":"","departamento":"Departamento de IT","dataAquisicao":"2026-04-24","funcionarioId":"Pom_016"}',
 '2026-08-04 10:57:42','REMOTE-IMPORT');

-- ---------- 269 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0269','CRIACAO','IMP-HPX-COL-002','Agri-Dge-Hp-00-02-A','Dge_01',
 NULL,
 NULL,
 '{"mac":"","sku":"IMP-HPX-COL-002","status":"ATIVO","condicao":"BOM","numSerie":"Agri-Dge-Hp-00-02-A","localizacao":"Sala da Direção Geral","observacoes":"","departamento":"Direção Geral","dataAquisicao":"2026-04-24","funcionarioId":"Dge_01"}',
 '2026-08-04 10:58:44','REMOTE-IMPORT');

-- ---------- 270 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0270','ATUALIZACAO','IMP-HPX-COL-002','Agri-Daf-Hp-00-00-A','Daf_01',
 NULL,
 '{"mac":"","sku":"IMP-HPX-COL-002","status":"ATIVO","condicao":"BOM","numSerie":"Agri-Daf-Dell-00-07-A","localizacao":"Escritório Geral","observacoes":"","departamento":"Departamento Financeiro","dataAquisicao":"2026-04-24","funcionarioId":"Daf_01"}',
 '{"mac":"","sku":"IMP-HPX-COL-002","status":"ATIVO","condicao":"BOM","numSerie":"Agri-Daf-Hp-00-00-A","localizacao":"Sala da Direção Financeira","observacoes":"","departamento":"Departamento Financeiro","dataAquisicao":"2026-04-24","funcionarioId":"Daf_01"}',
 '2026-08-04 10:59:53','REMOTE-IMPORT');

-- ---------- 271 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0271','ATUALIZACAO','IMP-HPX-COL-002','Agri-Daf-Hp-00-01-A','Daf_01',
 NULL,
 '{"mac":"","sku":"IMP-HPX-COL-002","status":"ATIVO","condicao":"BOM","numSerie":"Agri-Daf-Hp-00-00-A","localizacao":"Sala da Direção Financeira","observacoes":"","departamento":"Departamento Financeiro","dataAquisicao":"2026-04-24","funcionarioId":"Daf_01"}',
 '{"mac":"","sku":"IMP-HPX-COL-002","status":"EM_USO","condicao":"BOM","numSerie":"Agri-Daf-Hp-00-01-A","localizacao":"Sala da Direção Financeira","observacoes":"","departamento":"Departamento Financeiro","dataAquisicao":"2026-04-24","funcionarioId":"Daf_01"}',
 '2026-08-04 11:01:42','REMOTE-IMPORT');

-- ---------- 272 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0272','ATUALIZACAO','CMP-THI-LEN-001','Agri-Dpl-Leno-00-01-A','Dpl_01',
 NULL,
 '{"mac":"","sku":"CMP-THI-LEN-001","status":"ATIVO","condicao":"BOM","numSerie":"Agri-Dpl-Len-00-00-A","localizacao":"Sala da Direção de Plantação","observacoes":"","departamento":"Departamento de Plantação","dataAquisicao":"2026-04-24","funcionarioId":"Dpl_01"}',
 '{"mac":"","sku":"CMP-THI-LEN-001","status":"ATIVO","condicao":"BOM","numSerie":"Agri-Dpl-Leno-00-01-A","localizacao":"Sala da Direção de Plantação","observacoes":"","departamento":"Departamento de Plantação","dataAquisicao":"2026-04-24","funcionarioId":"Dpl_01"}',
 '2026-08-04 11:02:38','REMOTE-IMPORT');

-- ---------- 273 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0273','CRIACAO','IMP-CAN-PIX-001','Agri-Dpl-Canon-00-00-A','Dpl_02',
 NULL,
 NULL,
 '{"mac":"","sku":"IMP-CAN-PIX-001","status":"ATIVO","condicao":"BOM","numSerie":"Agri-Dpl-Canon-00-00-A","localizacao":"Sala de Plantação","observacoes":"","departamento":"Departamento de Plantação","dataAquisicao":"2026-04-24","funcionarioId":"Dpl_02"}',
 '2026-08-04 11:03:33','REMOTE-IMPORT');

-- ---------- 274 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0274','CRIACAO','IMP-HPX-HPL-001','Agri-Dge-Hp-00-03-A','Dge_02',
 NULL,
 NULL,
 '{"mac":"","sku":"IMP-HPX-HPL-001","status":"EM_USO","condicao":"BOM","numSerie":"Agri-Dge-Hp-00-03-A","localizacao":"Secretaria da Direção Geral","observacoes":"","departamento":"Direção Geral","dataAquisicao":"2026-04-24","funcionarioId":"Dge_02"}',
 '2026-08-04 11:04:41','REMOTE-IMPORT');

-- ---------- 275 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0275','CRIACAO','CMP-HPX-316-001','Agri-Dge-Hp-00-04-A','Dge_02',
 NULL,
 NULL,
 '{"mac":"","sku":"CMP-HPX-316-001","status":"ATIVO","condicao":"BOM","numSerie":"Agri-Dge-Hp-00-04-A","localizacao":"Secretaria da Direção Geral","observacoes":"","departamento":"Direção Geral","dataAquisicao":"2026-04-24","funcionarioId":"Dge_02"}',
 '2026-08-04 11:07:16','REMOTE-IMPORT');

-- ---------- 276 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0276','CRIACAO','CPU-DEL-D18-001','Agri-Pom-Dell-00-04-A','Pom_016',
 NULL,
 NULL,
 '{"mac":"","sku":"CPU-DEL-D18-001","status":"BAIXADO","condicao":"BOM","numSerie":"Agri-Pom-Dell-00-04-A","localizacao":"Sala de Informática (POM)","observacoes":"","departamento":"Departamento de IT","dataAquisicao":"2026-04-24","funcionarioId":"Pom_016"}',
 '2026-08-04 11:08:23','REMOTE-IMPORT');

-- ---------- 277 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0277','CRIACAO','NET-TPL-TLW-001','Agri-Dcl-Tplink-00-00-A','Amz_03',
 NULL,
 NULL,
 '{"mac":"","sku":"NET-TPL-TLW-001","status":"ATIVO","condicao":"BOM","numSerie":"Agri-Dcl-Tplink-00-00-A","localizacao":"Armazém (POM)","observacoes":"","departamento":"Departamento de Armazém","dataAquisicao":"2026-04-24","funcionarioId":"Amz_03"}',
 '2026-08-04 11:10:01','REMOTE-IMPORT');

-- ---------- 278 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0278','CRIACAO','IMP-HPX-HPD-001','Agri-DInfra-Hp-00-00-A','Djr_01',
 NULL,
 NULL,
 '{"mac":"","sku":"IMP-HPX-HPD-001","status":"ATIVO","condicao":"BOM","numSerie":"Agri-DInfra-Hp-00-00-A","localizacao":"Sala do Responsável Jurídico","observacoes":"","departamento":"Departamento Jurídico","dataAquisicao":"2026-04-24","funcionarioId":"Djr_01"}',
 '2026-08-04 11:11:50','REMOTE-IMPORT');

-- ---------- 279 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0279','CRIACAO','IMP-HPX-HPD-001','Agri-DInfra-Hp-00-01-A','Inf_01',
 NULL,
 NULL,
 '{"mac":"","sku":"IMP-HPX-HPD-001","status":"ATIVO","condicao":"BOM","numSerie":"Agri-DInfra-Hp-00-01-A","localizacao":"Sala do Responsável de Infraestruturas","observacoes":"","departamento":"Departamento de Infraestruturas","dataAquisicao":"2026-04-24","funcionarioId":"Inf_01"}',
 '2026-08-04 11:12:47','REMOTE-IMPORT');

-- ---------- 280 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0280','ATUALIZACAO','IMP-HPX-COL-002','Agri-Dge-Hp-00-05-A','Dge_01',
 NULL,
 '{"mac":"","sku":"IMP-HPX-COL-002","status":"ATIVO","condicao":"BOM","numSerie":"Agri-Dge-Hp-00-02-A","localizacao":"Sala da Direção Geral","observacoes":"","departamento":"Direção Geral","dataAquisicao":"2026-04-24","funcionarioId":"Dge_01"}',
 '{"mac":"","sku":"IMP-HPX-COL-002","status":"ATIVO","condicao":"BOM","numSerie":"Agri-Dge-Hp-00-05-A","localizacao":"Sala da Direção Geral","observacoes":"","departamento":"Direção Geral","dataAquisicao":"2026-04-24","funcionarioId":"Dge_01"}',
 '2026-08-04 11:14:53','REMOTE-IMPORT');

-- ---------- 281 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0281','CRIACAO','IMP-HPX-HPL-001','19A(CF219A)/17A(CF217A)','Dpl_01',
 NULL,
 NULL,
 '{"mac":"","sku":"IMP-HPX-HPL-001","status":"ATIVO","condicao":"BOM","numSerie":"19A(CF219A)/17A(CF217A)","localizacao":"Sala da Direção de Plantação","observacoes":"","departamento":"Departamento de Plantação","dataAquisicao":"2026-04-24","funcionarioId":"Dpl_01"}',
 '2026-08-04 11:15:18','REMOTE-IMPORT');

-- ---------- 282 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0282','ATUALIZACAO','CMP-LEN-PF9-001','Agri-Dcl-Leno-00-00-A','Drh_01',
 NULL,
 '{"mac":"00:45:e2:99:de:25","sku":"CMP-LEN-PF9-001","status":"EM_USO","condicao":"BOM","numSerie":"PF3CYL7H","localizacao":"Sala da contabilidade escritorio plantação","observacoes":"","departamento":"Departamento Compras e Logística","dataAquisicao":"2026-06-05","funcionarioId":"Drh_01"}',
 '{"mac":"00:45:e2:99:de:25","sku":"CMP-LEN-PF9-001","status":"EM_USO","condicao":"BOM","numSerie":"Agri-Dcl-Leno-00-00-A","localizacao":"Sala da contabilidade escritorio plantação","observacoes":"","departamento":"Departamento Compras e Logística","dataAquisicao":"2026-06-05","funcionarioId":"Drh_01"}',
 '2026-08-04 11:15:49','REMOTE-IMPORT');

-- ---------- 283 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0283','ATUALIZACAO','CMP-HPX-HP2-002','Agri-Pom-Hp-00-00-A','Pom_14',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"CRITICO","num_serie":"CND038244J","localizacao":"Recepção do Dep Industrial","observacoes":"Problemas no teclado Partes do computador esta partido","sku_produto":"CMP-HPX-HP2-002","departamento":"Departamento Industrial (POM)"}',
 '{"mac":"","sku":"CMP-HPX-HP2-002","status":"ATIVO","condicao":"CRITICO","numSerie":"Agri-Pom-Hp-00-00-A","localizacao":"Recepção do Dep Industrial","observacoes":"Problemas no teclado Partes do computador esta partido","departamento":"Departamento Industrial (POM)","dataAquisicao":"2026-04-24","funcionarioId":"Pom_14"}',
 '2026-08-10 14:24:12','REMOTE-IMPORT');

-- ---------- 284 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0284','ATUALIZACAO','CMP-HPX-HP2-002','Agri-Pom-Hp-00-00-A','Pom_14',
 NULL,
 '{"mac":"","sku":"CMP-HPX-HP2-002","status":"ATIVO","condicao":"CRITICO","numSerie":"Agri-Pom-Hp-00-00-A","localizacao":"Recepção do Dep Industrial","observacoes":"Problemas no teclado Partes do computador esta partido","departamento":"Departamento Industrial (POM)","dataAquisicao":"2026-04-24","funcionarioId":"Pom_14"}',
 '{"mac":"","sku":"CMP-HPX-HP2-002","status":"EM_USO","condicao":"CRITICO","numSerie":"Agri-Pom-Hp-00-00-A","localizacao":"Recepção do Dep Industrial","observacoes":"Problemas no teclado Partes do computador esta partido","departamento":"Departamento Industrial (POM)","dataAquisicao":"2026-04-24","funcionarioId":"Pom_14"}',
 '2026-08-10 14:24:30','REMOTE-IMPORT');

-- ---------- 285 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0285','ATUALIZACAO','MON-DEL-CN0-001','10C1KV2','Hsa_03',
 NULL,
 '{"origem":"IMPORTACAO_CSV","status":"ATIVO","condicao":"CRITICO","num_serie":"10C1KV2","localizacao":"Departamento de Higiene e Segurança","observacoes":"Linhas coloridas no visor","sku_produto":"MON-DEL-CN0-001","departamento":"Departamento de HSA"}',
 '{"mac":"","sku":"MON-DEL-CN0-001","status":"ATIVO","condicao":"CRITICO","numSerie":"10C1KV2","localizacao":"Departamento de Higiene e Segurança","observacoes":"Linhas coloridas no visor","departamento":"Departamento de HSA","dataAquisicao":"2026-04-24","funcionarioId":"Hsa_03"}',
 '2026-08-11 14:05:36','REMOTE-IMPORT');

-- ---------- 286 ----------
INSERT OR IGNORE INTO historico_eventos (id, tipo_evento, sku_produto, numSerie, funcionario_id, descricao_funcionario, dados_anteriores, dados_novos, created_at, device_id) VALUES
('HE-0286','ATUALIZACAO','MON-DEL-CN0-001','10C1KV2','Hsa_03',
 NULL,
 '{"mac":"","sku":"MON-DEL-CN0-001","status":"ATIVO","condicao":"CRITICO","numSerie":"10C1KV2","localizacao":"Departamento de Higiene e Segurança","observacoes":"Linhas coloridas no visor","departamento":"Departamento de HSA","dataAquisicao":"2026-04-24","funcionarioId":"Hsa_03"}',
 '{"mac":"","sku":"MON-DEL-CN0-001","status":"ATIVO","condicao":"CRITICO","numSerie":"10C1KV2","localizacao":"Departamento de Higiene e Segurança","observacoes":"Linhas coloridas no visor","departamento":"Departamento de HSA","dataAquisicao":"2026-04-24","funcionarioId":"Hsa_03"}',
 '2026-08-11 14:06:40','REMOTE-IMPORT');



-- ============================================================
-- Verificação final
-- ============================================================
-- SELECT COUNT(*) FROM historico_eventos;                                  -- 286
-- SELECT tipo_evento, COUNT(*) FROM historico_eventos GROUP BY tipo_evento;
-- SELECT COUNT(*) FROM historico_eventos WHERE dados_anteriores IS NULL;  -- CRIACAO