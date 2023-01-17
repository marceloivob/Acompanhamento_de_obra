package br.gov.planejamento.siconv.med.configuracao.paralisacao.dao;

import java.util.List;
import java.util.Set;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.statement.SqlBatch;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import br.gov.planejamento.siconv.med.configuracao.paralisacao.entity.database.AnexoParalisacaoBD;

public interface AnexoParalisacaoDAO {

	@SqlUpdate("DELETE FROM siconv.med_anexo_paralisacao WHERE paralisacao_fk = :idParalisacao")
	void excluirAnexoPorParalisacaoId(@Bind("idParalisacao") Long idParalisacao);

	@SqlBatch("INSERT INTO siconv.med_anexo_paralisacao (nm_arquivo, co_ceph, paralisacao_fk, adt_login, adt_data_hora, adt_operacao) "
			+ " VALUES (:nmArquivo, :coCeph, :paralisacaoFk, current_setting('med.cpf_usuario'), LOCALTIMESTAMP, 'INSERT')")
	void inserirAnexosParalisacao(@BindBean List<AnexoParalisacaoBD> listaAnexosBD);

	@SqlQuery("SELECT id FROM siconv.med_anexo_paralisacao WHERE paralisacao_fk = :paralisacaoFk")
	Set<Long> buscarIdAnexoPorIdParalisacao(@Bind("paralisacaoFk") Long idParalisacao);

	@SqlUpdate("DELETE FROM siconv.med_anexo_paralisacao WHERE id IN (<listaIdAnexo>)")
	void excluirAnexoPorListaIdAnexo(@BindList("listaIdAnexo") Set<Long> listaIdAnexo);
}
