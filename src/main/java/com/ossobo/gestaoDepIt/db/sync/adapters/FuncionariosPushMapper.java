package com.ossobo.gestaoDepIt.db.sync.adapters;

import com.ossobo.gestaoDepIt.db.config.DatabaseConnection;
import com.ossobo.gestaoDepIt.db.models.Funcionarios;
import com.ossobo.gestaoDepIt.db.repositories.FuncionariosRepository;
import com.ossobo.gestaoDepIt.db.sync.models.PushMapper;
import com.ossobo.winterfx.anotations.Inject;
import com.ossobo.winterfx.anotations.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * FuncionariosPushMapper v1.0
 *
 * Push da entidade `funcionarios` — SQLite local → MySQL remoto.
 *
 * Estratégia:
 *   - Lê os registros do SQLite via FuncionariosRepository (fonte única)
 *   - Escreve no MySQL via UPSERT (INSERT ... ON DUPLICATE KEY UPDATE)
 *   - Idempotente: rodar 2x não duplica
 *
 * Chave de negócio no MySQL: id (UUID). O `cod_dep` é UNIQUE secundário.
 */
@Service
public class FuncionariosPushMapper implements PushMapper {

    private static final System.Logger LOGGER =
            System.getLogger(FuncionariosPushMapper.class.getName());

    @Inject
    private FuncionariosRepository repository;   // lê do SQLite

    @Inject
    private DatabaseConnection dbConnection;     // abre conexão SQLite para os SELECTs

    @Override
    public String entidade() {
        return "funcionarios";
    }

    @Override
    public int pushFull(Connection connMySQL) throws SQLException {
        List<Funcionarios> todos = repository.findAll();
        return pushRegistros(connMySQL, todos);
    }

    @Override
    public int pushIncremental(Connection connMySQL, List<String> ids) throws SQLException {
        int total = 0;
        for (String id : ids) {
            var opt = repository.findById(id);
            if (opt.isPresent()) {
                total += pushRegistros(connMySQL, List.of(opt.get()));
            }
        }
        return total;
    }

    // ============================================================
    // ESCRITA NO MYSQL
    // ============================================================

    private static final String SQL_UPSERT = """
            INSERT INTO funcionarios (
                id, cod_dep, nome, funcao, departamento, local_trabalho,
                email, telefone, imagem_perfil, tipo_imagem, tamanho_imagem,
                ativo, created_at, updated_at, device_id, deleted
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                cod_dep = VALUES(cod_dep),
                nome = VALUES(nome),
                funcao = VALUES(funcao),
                departamento = VALUES(departamento),
                local_trabalho = VALUES(local_trabalho),
                email = VALUES(email),
                telefone = VALUES(telefone),
                imagem_perfil = VALUES(imagem_perfil),
                tipo_imagem = VALUES(tipo_imagem),
                tamanho_imagem = VALUES(tamanho_imagem),
                ativo = VALUES(ativo),
                updated_at = VALUES(updated_at),
                device_id = VALUES(device_id),
                deleted = VALUES(deleted)
            """;

    private int pushRegistros(Connection connMySQL, List<Funcionarios> registros) throws SQLException {
        if (registros.isEmpty()) return 0;

        try (PreparedStatement ps = connMySQL.prepareStatement(SQL_UPSERT)) {
            for (Funcionarios f : registros) {
                preencherUpsert(ps, f);
                ps.addBatch();
            }
            int[] resultados = ps.executeBatch();
            int total = 0;
            for (int r : resultados) {
                if (r >= 0 || r == PreparedStatement.SUCCESS_NO_INFO) total++;
            }
            return total;
        }
    }

    private void preencherUpsert(PreparedStatement ps, Funcionarios f) throws SQLException {
        ps.setString(1, f.id());
        ps.setString(2, f.codDep());
        ps.setString(3, f.nome());
        ps.setString(4, f.funcao());
        ps.setString(5, f.departamento());
        ps.setString(6, f.localTrabalho());
        ps.setString(7, f.email());
        ps.setString(8, f.telefone());
        ps.setBytes(9, f.imagemPerfil());
        ps.setString(10, f.tipoImagem());
        if (f.tamanhoImagem() != null) ps.setInt(11, f.tamanhoImagem());
        else ps.setNull(11, java.sql.Types.INTEGER);
        ps.setBoolean(12, f.isAtivo());
        ps.setString(13, f.createdAt() != null ? f.createdAt().toString() : null);
        ps.setString(14, f.updatedAt() != null ? f.updatedAt().toString() : null);
        ps.setString(15, f.deviceId());
        ps.setBoolean(16, f.isDeletado());
    }
}