package br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.dao;

import java.util.List;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlBatch;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.database.AnotacaoRegistroRtSubmetaBD;

public interface AnotacaoRegistroRtSubmetaDAO {

	
	@SqlBatch("INSERT INTO siconv.med_anotacao_registro_rt_submeta (vrpl_submeta_fk, med_anotacao_registro_rt_fk, adt_login, adt_data_hora, adt_operacao) "
				+ " VALUES (:vrplSubmetaFk, :anotacaoRegistroRtFk, current_setting('med.cpf_usuario'), LOCALTIMESTAMP, 'INSERT')")
	void inserirAnotacaoRegistoSubmeta(@BindBean List<AnotacaoRegistroRtSubmetaBD> listaAnotacoes);

	@SqlBatch("DELETE FROM siconv.med_anotacao_registro_rt_submeta WHERE vrpl_submeta_fk = :vrplSubmetaFk AND med_anotacao_registro_rt_fk = :anotacaoRegistroRtFk")
	void deletar(@BindBean List<AnotacaoRegistroRtSubmetaBD> listaAnotacoes);

    @SqlUpdate("DELETE FROM siconv.med_anotacao_registro_rt_submeta WHERE med_anotacao_registro_rt_fk = :idAnotacao")
    boolean deletarPorIdAnotacao(@Bind("idAnotacao") Long idAnotacao);
}
