-- ============================================================
-- Bootstrap FUNCIONARIOS — MySQL remoto → SQLite local
-- Execução: UMA VEZ. Depois apague.
-- ============================================================



-- ---------- 1 ----------
INSERT OR IGNORE INTO funcionarios (
    id, cod_dep, nome, funcao, departamento, local_trabalho,
    email, telefone, imagem_perfil, tipo_imagem, tamanho_imagem,
    ativo, created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'Amz_02', 'Carlos Cesar', 'Auxiliar Administrativo de Armazém',
    'Departamento Compras e Logística', 'Armazem Geral',
    'ccesar@agripalmastp.com', '', NULL, NULL, NULL,
    1, '2026-04-24T13:26:28', '2026-05-26T12:06:05', 'REMOTE-IMPORT', 0
);

-- ---------- 2 ----------
INSERT OR IGNORE INTO funcionarios (
    id, cod_dep, nome, funcao, departamento, local_trabalho,
    email, telefone, imagem_perfil, tipo_imagem, tamanho_imagem,
    ativo, created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'Amz_03', 'Domingos Armando', 'Auxiliar Administrativo de Armazém',
    'Departamento de Armazém', 'Armazém (POM)',
    'domingos.armando@agripalmastp.com', '', NULL, NULL, NULL,
    1, '2026-04-24T13:26:28', '2026-04-24T13:26:28', 'REMOTE-IMPORT', 0
);

-- ---------- 3 ----------
INSERT OR IGNORE INTO funcionarios (
    id, cod_dep, nome, funcao, departamento, local_trabalho,
    email, telefone, imagem_perfil, tipo_imagem, tamanho_imagem,
    ativo, created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'Amz_04', 'Flora Andrande', 'Auxiliar de Armazém',
    'Departamento de Armazém', 'Armazém Geral',
    'flora.andrande@agripalmastp.com', '', NULL, NULL, NULL,
    1, '2026-04-24T13:26:28', '2026-04-24T13:26:28', 'REMOTE-IMPORT', 0
);

-- ---------- 4 ----------
INSERT OR IGNORE INTO funcionarios (
    id, cod_dep, nome, funcao, departamento, local_trabalho,
    email, telefone, imagem_perfil, tipo_imagem, tamanho_imagem,
    ativo, created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'Daf_01', 'Gonçalo Vieira da Costa', 'Director Financeiro',
    'Departamento Financeiro', 'Sala da Direção Financeira',
    'goncalo.costa@agripalmastp.com', '', NULL, NULL, NULL,
    1, '2026-04-24T13:26:28', '2026-04-24T13:26:28', 'REMOTE-IMPORT', 0
);

-- ---------- 5 ----------
INSERT OR IGNORE INTO funcionarios (
    id, cod_dep, nome, funcao, departamento, local_trabalho,
    email, telefone, imagem_perfil, tipo_imagem, tamanho_imagem,
    ativo, created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'Daf_02', 'Amilcar Bom Jesus', 'Contabilista',
    'Departamento Financeiro', 'Departamento Financeiro e Contabilidade',
    'amilcar.jesus@agripalmastp.com', '', NULL, NULL, NULL,
    1, '2026-04-24T13:26:28', '2026-04-24T13:26:28', 'REMOTE-IMPORT', 0
);

-- ---------- 6 ----------
INSERT OR IGNORE INTO funcionarios (
    id, cod_dep, nome, funcao, departamento, local_trabalho,
    email, telefone, imagem_perfil, tipo_imagem, tamanho_imagem,
    ativo, created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'Daf_04', 'Odila Santo Martins', 'Assistente de Contabilidade',
    'Departamento Financeiro', 'Departamento Financeiro e Contabilidade',
    'odila.martins@agripalmastp.com', '', NULL, NULL, NULL,
    1, '2026-04-24T13:26:28', '2026-04-24T13:26:28', 'REMOTE-IMPORT', 0
);

-- ---------- 7 ----------
INSERT OR IGNORE INTO funcionarios (
    id, cod_dep, nome, funcao, departamento, local_trabalho,
    email, telefone, imagem_perfil, tipo_imagem, tamanho_imagem,
    ativo, created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'Daf_05', 'Josimar Neto', 'Tecnico Contabilistico',
    'Departamento Financeiro', 'Sala da contabilidade Escritorio Plantação',
    'josimar.neto@agripalmastp.com', '', NULL, NULL, NULL,
    1, '2026-04-24T13:26:28', '2026-06-05T10:53:12', 'REMOTE-IMPORT', 0
);

-- ---------- 8 ----------
INSERT OR IGNORE INTO funcionarios (
    id, cod_dep, nome, funcao, departamento, local_trabalho,
    email, telefone, imagem_perfil, tipo_imagem, tamanho_imagem,
    ativo, created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'Daf_06', 'Bazilio Espirito Santo', 'Tecnico de Tesouraria',
    'Departamento Financeiro', 'Departamento Financeiro e Contabilidade',
    'banto@agripalmastp.com', '', NULL, NULL, NULL,
    1, '2026-04-24T13:26:28', '2026-05-26T12:03:58', 'REMOTE-IMPORT', 0
);

-- ---------- 9 ----------
INSERT OR IGNORE INTO funcionarios (
    id, cod_dep, nome, funcao, departamento, local_trabalho,
    email, telefone, imagem_perfil, tipo_imagem, tamanho_imagem,
    ativo, created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'Daf_07', 'Dinizia Fortes', 'Tecnico Contabilistico',
    'Departamento Financeiro', 'Departamento Financeiro e Contabilidade',
    'dinizia.fortes@agripalmastp.com', '', NULL, NULL, NULL,
    1, '2026-04-24T13:26:28', '2026-04-24T13:26:28', 'REMOTE-IMPORT', 0
);

-- ---------- 10 ----------
INSERT OR IGNORE INTO funcionarios (
    id, cod_dep, nome, funcao, departamento, local_trabalho,
    email, telefone, imagem_perfil, tipo_imagem, tamanho_imagem,
    ativo, created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'Daf_09', 'Edmilson Pinto', 'Tesoureiro',
    'Departamento Financeiro', 'Sala de tesouraria do escritorio plantação',
    'epinto@agripalmastp.com', '', NULL, NULL, NULL,
    1, '2026-06-05T10:45:29', '2026-06-05T10:45:29', 'REMOTE-IMPORT', 0
);

-- ---------- 11 ----------
INSERT OR IGNORE INTO funcionarios (
    id, cod_dep, nome, funcao, departamento, local_trabalho,
    email, telefone, imagem_perfil, tipo_imagem, tamanho_imagem,
    ativo, created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'Dcl_01', 'Paulo Cesar', 'Diretor de Compras e Logisticas',
    'Departamento Comercial', 'Diretor de Compras',
    'paulo.cesar@agripalmastp.com', '', NULL, NULL, NULL,
    1, '2026-04-24T13:26:28', '2026-04-24T13:26:28', 'REMOTE-IMPORT', 0
);

-- ---------- 12 ----------
INSERT OR IGNORE INTO funcionarios (
    id, cod_dep, nome, funcao, departamento, local_trabalho,
    email, telefone, imagem_perfil, tipo_imagem, tamanho_imagem,
    ativo, created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'Dcl_02', 'Baltzer dos Santos Varela', 'Gestor de Compra',
    'Departamento Financeiro', 'Departamento Financeiro e Contabilidade',
    'baltzer.varela@agripalmastp.com', '', NULL, NULL, NULL,
    1, '2026-04-24T13:26:28', '2026-04-24T13:26:28', 'REMOTE-IMPORT', 0
);

-- ---------- 13 ----------
INSERT OR IGNORE INTO funcionarios (
    id, cod_dep, nome, funcao, departamento, local_trabalho,
    email, telefone, imagem_perfil, tipo_imagem, tamanho_imagem,
    ativo, created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'Dcl_03', 'Alex Spencer Rangel', 'Gestor de compras internacionais',
    'Departamento Compras e Logística', 'Sala de contabilidade Escritorio plantação',
    'aspencer@agripalmastp.com', '', NULL, NULL, NULL,
    1, '2026-06-05T09:02:40', '2026-06-05T09:02:40', 'REMOTE-IMPORT', 0
);

-- ---------- 14 ----------
INSERT OR IGNORE INTO funcionarios (
    id, cod_dep, nome, funcao, departamento, local_trabalho,
    email, telefone, imagem_perfil, tipo_imagem, tamanho_imagem,
    ativo, created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'Dge_01', 'Nicolas Bergerot', 'Director Geral',
    'Direção Geral', 'Sala da Direção Geral',
    'nicolas.bergerot@agripalmastp.com', '', NULL, NULL, NULL,
    1, '2026-04-24T13:26:28', '2026-04-24T13:26:28', 'REMOTE-IMPORT', 0
);

-- ---------- 15 ----------
INSERT OR IGNORE INTO funcionarios (
    id, cod_dep, nome, funcao, departamento, local_trabalho,
    email, telefone, imagem_perfil, tipo_imagem, tamanho_imagem,
    ativo, created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'Dge_02', 'Elvina Pedrosa', 'Secretaria Diretor Geral',
    'Direção Geral', 'Secretaria da Direção Geral',
    'elvina.pedrosa@agripalmastp.com', '', NULL, NULL, NULL,
    1, '2026-04-24T13:26:28', '2026-04-24T13:26:28', 'REMOTE-IMPORT', 0
);

-- ---------- 16 (com metadados de imagem — bytes NULL) ----------
INSERT OR IGNORE INTO funcionarios (
    id, cod_dep, nome, funcao, departamento, local_trabalho,
    email, telefone, imagem_perfil, tipo_imagem, tamanho_imagem,
    ativo, created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'Dinfo_001', 'Rafael', 'Tecnico informatico',
    'Departamento Informático', 'Sala do dep informatico',
    'rtav@agripalmastp.com', '+239 991 30 93',
    NULL, 'jpg', 48041,
    1, '2026-07-09T12:34:19', '2026-07-09T15:10:19', 'REMOTE-IMPORT', 0
);

-- ---------- 17 ----------
INSERT OR IGNORE INTO funcionarios (
    id, cod_dep, nome, funcao, departamento, local_trabalho,
    email, telefone, imagem_perfil, tipo_imagem, tamanho_imagem,
    ativo, created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'Djr_01', 'Herlander Pinto', 'Responsavel de departamento Legal',
    'Departamento Jurídico', 'Sala do Responsável Jurídico',
    'herlander.pinto@agripalmastp.com', '', NULL, NULL, NULL,
    1, '2026-04-24T13:26:28', '2026-04-24T13:26:28', 'REMOTE-IMPORT', 0
);

-- ---------- 18 ----------
INSERT OR IGNORE INTO funcionarios (
    id, cod_dep, nome, funcao, departamento, local_trabalho,
    email, telefone, imagem_perfil, tipo_imagem, tamanho_imagem,
    ativo, created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'Dpl_01', 'Laurent Pillon', 'Director de Plantação',
    'Departamento de Plantação', 'Sala da Direção de Plantação',
    'laurent.pillon@agripalmastp.com', '', NULL, NULL, NULL,
    1, '2026-04-24T13:26:28', '2026-04-24T13:26:28', 'REMOTE-IMPORT', 0
);

-- ---------- 19 ----------
INSERT OR IGNORE INTO funcionarios (
    id, cod_dep, nome, funcao, departamento, local_trabalho,
    email, telefone, imagem_perfil, tipo_imagem, tamanho_imagem,
    ativo, created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'Dpl_02', 'Ludger Magalhães', 'Assistente de Plantação',
    'Departamento de Plantação', 'Sala de Plantação',
    'ludger.magalhaes@agripalmastp.com', '', NULL, NULL, NULL,
    1, '2026-04-24T13:26:29', '2026-04-24T13:26:29', 'REMOTE-IMPORT', 0
);

-- ---------- 20 ----------
INSERT OR IGNORE INTO funcionarios (
    id, cod_dep, nome, funcao, departamento, local_trabalho,
    email, telefone, imagem_perfil, tipo_imagem, tamanho_imagem,
    ativo, created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'Dpl_03', 'Tatiana Da Silva', 'Assistente do Diretor de Plantação',
    'Departamento de Plantação', 'Sala de Plantação',
    'tatiana.silva@agripalmastp.com', '', NULL, NULL, NULL,
    1, '2026-04-24T13:26:29', '2026-04-24T13:26:29', 'REMOTE-IMPORT', 0
);

-- ---------- 21 (departamento vazio → N/A) ----------
INSERT OR IGNORE INTO funcionarios (
    id, cod_dep, nome, funcao, departamento, local_trabalho,
    email, telefone, imagem_perfil, tipo_imagem, tamanho_imagem,
    ativo, created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'Dpl_04', 'Cyril Evrard', 'Eliminação de Pragas',
    'N/A', '',
    'cyril.evrard@agripalmastp.com', '', NULL, NULL, NULL,
    1, '2026-04-24T13:26:29', '2026-04-24T13:26:29', 'REMOTE-IMPORT', 0
);

-- ---------- 22 (departamento vazio → N/A) ----------
INSERT OR IGNORE INTO funcionarios (
    id, cod_dep, nome, funcao, departamento, local_trabalho,
    email, telefone, imagem_perfil, tipo_imagem, tamanho_imagem,
    ativo, created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'Drc_01', 'Sopheap Kol', 'Responsável de Comunicação',
    'Departamento de Comunicação', '',
    'sopheap.kol@agripalmastp.com', '', NULL, NULL, NULL,
    1, '2026-04-24T13:26:29', '2026-04-24T13:26:29', 'REMOTE-IMPORT', 0
);

-- ---------- 23 ----------
INSERT OR IGNORE INTO funcionarios (
    id, cod_dep, nome, funcao, departamento, local_trabalho,
    email, telefone, imagem_perfil, tipo_imagem, tamanho_imagem,
    ativo, created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'Drh_01', 'Carlos Matos', 'Gestor de Compras Internacionais',
    'Departamento Compras e Logística', 'Sala da contabilidade escritorio plantação',
    'cmatos@agripalmastp.com', '', NULL, NULL, NULL,
    1, '2026-04-24T13:26:29', '2026-06-05T09:51:17', 'REMOTE-IMPORT', 0
);

-- ---------- 24 ----------
INSERT OR IGNORE INTO funcionarios (
    id, cod_dep, nome, funcao, departamento, local_trabalho,
    email, telefone, imagem_perfil, tipo_imagem, tamanho_imagem,
    ativo, created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'Drh_02', 'Sizilene', 'Assistente administrativo de Recursos Humanos',
    'Departamento de RH', 'Sala Comum Escritorio Geral',
    'sizilene@agripalmastp.com', '', NULL, NULL, NULL,
    1, '2026-04-24T13:26:29', '2026-04-24T13:26:29', 'REMOTE-IMPORT', 0
);

-- ---------- 25 ----------
INSERT OR IGNORE INTO funcionarios (
    id, cod_dep, nome, funcao, departamento, local_trabalho,
    email, telefone, imagem_perfil, tipo_imagem, tamanho_imagem,
    ativo, created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'Drh_04', 'Niclay Augusto', 'Assistente administrativo de Recursos Humanos',
    'Departamento de RH', 'Sala Comum Escritorio Geral',
    'niclay.augusto@agripalmastp.com', '', NULL, NULL, NULL,
    1, '2026-04-24T13:26:29', '2026-04-24T13:26:29', 'REMOTE-IMPORT', 0
);

-- ---------- 26 ----------
INSERT OR IGNORE INTO funcionarios (
    id, cod_dep, nome, funcao, departamento, local_trabalho,
    email, telefone, imagem_perfil, tipo_imagem, tamanho_imagem,
    ativo, created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'Drh_05', 'Ernane Patrocinio', 'Agente administrativo',
    'Departamento de RH', 'Sala Comum Escritorio Geral',
    'ernane.patrocinio@agripalmastp.com', '', NULL, NULL, NULL,
    1, '2026-04-24T13:26:29', '2026-04-24T13:26:29', 'REMOTE-IMPORT', 0
);

-- ---------- 27 ----------
INSERT OR IGNORE INTO funcionarios (
    id, cod_dep, nome, funcao, departamento, local_trabalho,
    email, telefone, imagem_perfil, tipo_imagem, tamanho_imagem,
    ativo, created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'Drh_06', 'Ludmila Bonfim', 'Agente administrativo',
    'Departamento de RH', 'Sala Comum Escritorio Geral',
    'ludmila.bonfim@agripalmastp.com', '', NULL, NULL, NULL,
    1, '2026-04-24T13:26:29', '2026-04-24T13:26:29', 'REMOTE-IMPORT', 0
);

-- ---------- 28 ----------
INSERT OR IGNORE INTO funcionarios (
    id, cod_dep, nome, funcao, departamento, local_trabalho,
    email, telefone, imagem_perfil, tipo_imagem, tamanho_imagem,
    ativo, created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'Drh_07', 'Celsio Junqueira', 'Responsável de Recursos Humanos',
    'Departamento de RH', 'Sala do Responsável de RH',
    'celsio.junqueira@agripalmastp.com', '', NULL, NULL, NULL,
    1, '2026-04-24T13:26:29', '2026-04-24T13:26:29', 'REMOTE-IMPORT', 0
);

-- ---------- 29 ----------
INSERT OR IGNORE INTO funcionarios (
    id, cod_dep, nome, funcao, departamento, local_trabalho,
    email, telefone, imagem_perfil, tipo_imagem, tamanho_imagem,
    ativo, created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'Drh_09', 'Celsio Junqueira', 'Diretor dos recursos humano',
    'Departamento RH', 'Sala do diretor dos recursos humanos',
    'cjunqueira@agripalmastp.com', '', NULL, NULL, NULL,
    1, '2026-04-28T14:56:19', '2026-04-28T14:56:19', 'REMOTE-IMPORT', 0
);

-- ---------- 30 ----------
INSERT OR IGNORE INTO funcionarios (
    id, cod_dep, nome, funcao, departamento, local_trabalho,
    email, telefone, imagem_perfil, tipo_imagem, tamanho_imagem,
    ativo, created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'Hsa_01', 'Cynthia Van-Dunem', 'Responsável de Sustentabilidade',
    'Departamento de HSA', 'Sala de Sustentabilidade e HSA (POM)',
    'cynthia.vandunem@agripalmastp.com', '', NULL, NULL, NULL,
    1, '2026-04-24T13:26:29', '2026-04-24T13:26:29', 'REMOTE-IMPORT', 0
);

-- ---------- 31 ----------
INSERT OR IGNORE INTO funcionarios (
    id, cod_dep, nome, funcao, departamento, local_trabalho,
    email, telefone, imagem_perfil, tipo_imagem, tamanho_imagem,
    ativo, created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'Hsa_02', 'Adozindo', 'Responsavel social de Ligação Comunitária',
    'Departamento de HSA', 'Higiene e Segurança no Trabalho',
    'adozindo@agripalmastp.com', '', NULL, NULL, NULL,
    1, '2026-04-24T13:26:29', '2026-04-24T13:26:29', 'REMOTE-IMPORT', 0
);

-- ---------- 32 ----------
INSERT OR IGNORE INTO funcionarios (
    id, cod_dep, nome, funcao, departamento, local_trabalho,
    email, telefone, imagem_perfil, tipo_imagem, tamanho_imagem,
    ativo, created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'Hsa_03', 'Braylesson Monteiro', 'Tecnico de HSA',
    'Departamento de HSA', 'Departamento de Higiene e Segurança',
    'bmonteiro@agripalmastp.com', '', NULL, NULL, NULL,
    1, '2026-04-24T13:26:29', '2026-05-26T12:06:35', 'REMOTE-IMPORT', 0
);

-- ---------- 33 ----------
INSERT OR IGNORE INTO funcionarios (
    id, cod_dep, nome, funcao, departamento, local_trabalho,
    email, telefone, imagem_perfil, tipo_imagem, tamanho_imagem,
    ativo, created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'Inf_01', 'Helder Leitão', 'Responsável do Departamento de Infraestruturas',
    'Departamento de Infraestruturas', 'Sala do Responsável de Infraestruturas',
    'helder.leitao@agripalmastp.com', '', NULL, NULL, NULL,
    1, '2026-04-24T13:26:29', '2026-04-24T13:26:29', 'REMOTE-IMPORT', 0
);

-- ---------- 34 ----------
INSERT OR IGNORE INTO funcionarios (
    id, cod_dep, nome, funcao, departamento, local_trabalho,
    email, telefone, imagem_perfil, tipo_imagem, tamanho_imagem,
    ativo, created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'Inf_02', 'Durdiley Guine', 'Secretario De Infraestrutura',
    'Departamento de Infraestruturas', 'Sala da infraestrutura',
    'durdiley.guine@agripalmastp.com', '', NULL, NULL, NULL,
    1, '2026-04-24T13:26:29', '2026-04-24T13:26:29', 'REMOTE-IMPORT', 0
);

-- ---------- 35 ----------
INSERT OR IGNORE INTO funcionarios (
    id, cod_dep, nome, funcao, departamento, local_trabalho,
    email, telefone, imagem_perfil, tipo_imagem, tamanho_imagem,
    ativo, created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'Ofi_01', 'George Karakaside', 'Responsável de Oficina',
    'Departamento de Oficina', 'Sala do Responsável de Oficina',
    'george.karakaside@agripalmastp.com', '', NULL, NULL, NULL,
    1, '2026-04-24T13:26:29', '2026-04-24T13:26:29', 'REMOTE-IMPORT', 0
);

-- ---------- 36 ----------
INSERT OR IGNORE INTO funcionarios (
    id, cod_dep, nome, funcao, departamento, local_trabalho,
    email, telefone, imagem_perfil, tipo_imagem, tamanho_imagem,
    ativo, created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'Ofi_02', 'Rail dos Reis', 'Assistente Administrativo da Oficina',
    'Departamento de Oficina', 'Sala do Responsável de Oficina',
    'rail.reis@agripalmastp.com', '', NULL, NULL, NULL,
    1, '2026-04-24T13:26:29', '2026-04-24T13:26:29', 'REMOTE-IMPORT', 0
);

-- ---------- 37 ----------
INSERT OR IGNORE INTO funcionarios (
    id, cod_dep, nome, funcao, departamento, local_trabalho,
    email, telefone, imagem_perfil, tipo_imagem, tamanho_imagem,
    ativo, created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'Ofi_03', 'Johann Miller', 'Técnico de Oficina',
    'Departamento de Oficina', 'Oficina',
    'johann.miller@agripalmastp.com', '', NULL, NULL, NULL,
    1, '2026-04-24T13:26:29', '2026-04-24T13:26:29', 'REMOTE-IMPORT', 0
);

-- ---------- 38 (com metadados de imagem — bytes NULL) ----------
INSERT OR IGNORE INTO funcionarios (
    id, cod_dep, nome, funcao, departamento, local_trabalho,
    email, telefone, imagem_perfil, tipo_imagem, tamanho_imagem,
    ativo, created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'Pom_016', 'Rafael Tavares', 'Tecnico Informatico',
    'Departamento Industrial', '',
    'rtavares@agripalmastp.com', '239 991 30 93',
    NULL, 'jpg', 48041,
    1, '2026-04-24T13:26:29', '2026-08-04T11:48:25', 'REMOTE-IMPORT', 0
);

-- ---------- 39 ----------
INSERT OR IGNORE INTO funcionarios (
    id, cod_dep, nome, funcao, departamento, local_trabalho,
    email, telefone, imagem_perfil, tipo_imagem, tamanho_imagem,
    ativo, created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'Pom_02', 'Pedro dos Santos', 'Director Industrial',
    'Departamento Industrial (POM)', 'Sala da Direção Industrial (POM)',
    'pedro.santos@agripalmastp.com', '', NULL, NULL, NULL,
    1, '2026-04-24T13:26:29', '2026-04-24T13:26:29', 'REMOTE-IMPORT', 0
);

-- ---------- 40 ----------
INSERT OR IGNORE INTO funcionarios (
    id, cod_dep, nome, funcao, departamento, local_trabalho,
    email, telefone, imagem_perfil, tipo_imagem, tamanho_imagem,
    ativo, created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'Pom_03', 'Kleyton', 'Auxiliar administrativo',
    'Departamento Industrial (POM)', 'Balança (POM)',
    'kleyton@agripalmastp.com', '', NULL, NULL, NULL,
    1, '2026-04-24T13:26:29', '2026-04-24T13:26:29', 'REMOTE-IMPORT', 0
);

-- ---------- 41 ----------
INSERT OR IGNORE INTO funcionarios (
    id, cod_dep, nome, funcao, departamento, local_trabalho,
    email, telefone, imagem_perfil, tipo_imagem, tamanho_imagem,
    ativo, created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'Pom_05', 'Martim Viegas', 'Responsável de Manutenção',
    'Departamento Industrial (POM)', 'Escritório de Manutenção (POM)',
    'martim.viegas@agripalmastp.com', '', NULL, NULL, NULL,
    1, '2026-04-24T13:26:29', '2026-04-24T13:26:29', 'REMOTE-IMPORT', 0
);

-- ---------- 42 ----------
INSERT OR IGNORE INTO funcionarios (
    id, cod_dep, nome, funcao, departamento, local_trabalho,
    email, telefone, imagem_perfil, tipo_imagem, tamanho_imagem,
    ativo, created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'Pom_08', 'Indira Conceição', 'Responsável de Laboratório',
    'Departamento Industrial (POM)', 'Laboratório (POM)',
    'indira.conceicao@agripalmastp.com', '', NULL, NULL, NULL,
    1, '2026-04-24T13:26:29', '2026-04-24T13:26:29', 'REMOTE-IMPORT', 0
);

-- ---------- 43 ----------
INSERT OR IGNORE INTO funcionarios (
    id, cod_dep, nome, funcao, departamento, local_trabalho,
    email, telefone, imagem_perfil, tipo_imagem, tamanho_imagem,
    ativo, created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'Pom_10', 'Geroy Kiti', 'Supervisor de Produção',
    'Departamento Industrial (POM)', 'Escritório de Produção (POM)',
    'geroy.kiti@agripalmastp.com', '', NULL, NULL, NULL,
    1, '2026-04-24T13:26:29', '2026-04-24T13:26:29', 'REMOTE-IMPORT', 0
);

-- ---------- 44 ----------
INSERT OR IGNORE INTO funcionarios (
    id, cod_dep, nome, funcao, departamento, local_trabalho,
    email, telefone, imagem_perfil, tipo_imagem, tamanho_imagem,
    ativo, created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'Pom_11', 'Paulo Gorge', 'Responsável do Departamento Comercial',
    'Departamento Financeiro', 'Sala do Departamento Comercial (POM)',
    'paulo.gorge@agripalmastp.com', '', NULL, NULL, NULL,
    1, '2026-04-24T13:26:29', '2026-04-24T13:26:29', 'REMOTE-IMPORT', 0
);

-- ---------- 45 (departamento vazio → N/A) ----------
INSERT OR IGNORE INTO funcionarios (
    id, cod_dep, nome, funcao, departamento, local_trabalho,
    email, telefone, imagem_perfil, tipo_imagem, tamanho_imagem,
    ativo, created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'Pom_12', 'Vera Susana Gomes da Silva', 'Coordenador de Qualidade ISO 9001',
    'N/A', '',
    'vera.silva@agripalmastp.com', '', NULL, NULL, NULL,
    1, '2026-04-24T13:26:29', '2026-04-24T13:26:29', 'REMOTE-IMPORT', 0
);

-- ---------- 46 ----------
INSERT OR IGNORE INTO funcionarios (
    id, cod_dep, nome, funcao, departamento, local_trabalho,
    email, telefone, imagem_perfil, tipo_imagem, tamanho_imagem,
    ativo, created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'Pom_13', 'Arnoud Veigas', 'Responsavel de Produção',
    'Departamento Industrial (POM)', 'Sala do responsavel de produção',
    'arnoud.veigas@agripalmastp.com', '', NULL, NULL, NULL,
    1, '2026-04-24T13:26:29', '2026-04-24T13:26:29', 'REMOTE-IMPORT', 0
);

-- ---------- 47 ----------
INSERT OR IGNORE INTO funcionarios (
    id, cod_dep, nome, funcao, departamento, local_trabalho,
    email, telefone, imagem_perfil, tipo_imagem, tamanho_imagem,
    ativo, created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'Pom_14', 'Savio', 'Secretario do Dep. Industrial',
    'Departamento Industrial (POM)', 'Recepção do Dep Industrial',
    'savio@agripalmastp.com', '', NULL, NULL, NULL,
    1, '2026-04-24T13:26:29', '2026-04-24T13:26:29', 'REMOTE-IMPORT', 0
);

-- ---------- 48 ----------
INSERT OR IGNORE INTO funcionarios (
    id, cod_dep, nome, funcao, departamento, local_trabalho,
    email, telefone, imagem_perfil, tipo_imagem, tamanho_imagem,
    ativo, created_at, updated_at, device_id, deleted
) VALUES (
    lower(hex(randomblob(4)))||'-'||lower(hex(randomblob(2)))||'-4'||substr(lower(hex(randomblob(2))),2)||'-'||substr('89ab',abs(random())%4+1,1)||substr(lower(hex(randomblob(2))),2)||'-'||lower(hex(randomblob(6))),
    'Pom_15', 'Cristom Quaresma', 'Analista de Fruto',
    'Departamento Industrial (POM)', 'Laboratorio Industrial',
    'cristom.quaresma@agripalmastp.com', '', NULL, NULL, NULL,
    1, '2026-04-24T13:26:29', '2026-04-24T13:26:29', 'REMOTE-IMPORT', 0
);



-- ============================================================
-- Verificação
-- ============================================================
-- SELECT COUNT(*) FROM funcionarios;                                  -- deve ser 48
-- SELECT cod_dep, nome, departamento FROM funcionarios
--   WHERE departamento = 'N/A';                                       -- deve listar 3
-- SELECT cod_dep, nome, tipo_imagem, tamanho_imagem FROM funcionarios
--   WHERE tipo_imagem IS NOT NULL;                                    -- deve listar 2 (Dinfo_001, Pom_016)