package br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.dao;

import java.util.List;

import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.database.RegistroProfissionalBD;

public interface RegistroProfissionalDAO {

	@SqlUpdate("INSERT INTO siconv.med_registro_profissional (atividade, crea_cau, uf, med_responsavel_tecnico_fk, versao, adt_login, adt_data_hora, adt_operacao) VALUES (:atividade, :nrCreaCau, :uf, :responsavelTecnicoFk, 1, current_setting('med.cpf_usuario'), LOCALTIMESTAMP, 'INSERT')")
	@RegisterFieldMapper(RegistroProfissionalBD.class)
	@GetGeneratedKeys
	RegistroProfissionalBD inserir(@BindBean RegistroProfissionalBD registroProfissionalBD);

	@SqlUpdate("update siconv.med_registro_profissional set atividade=:atividade, crea_cau=:nrCreaCau,  uf=:uf, versao = :versao + 1, adt_login = current_setting('med.cpf_usuario'), adt_data_hora = LOCALTIMESTAMP, adt_operacao = 'UPDATE' where id =:id ")
	@RegisterFieldMapper(RegistroProfissionalBD.class)
	void alterar(@BindBean RegistroProfissionalBD registroProfissionalBD);
	
	@SqlUpdate("DELETE FROM siconv.med_registro_profissional WHERE id IN (<listaIdRegistro>)")
	void excluirRegistrosPorListaId(@BindList("listaIdRegistro") List<Long> listaIdRegistro, @BindList("listaVersaoRegistro") List<Long> listaVersaoRegistro);
	
}
