package br.gov.planejamento.siconv.med.medicao.dao;

import java.util.List;

import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.statement.SqlBatch;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import br.gov.planejamento.siconv.med.medicao.entity.database.AnexoBD;
import br.gov.planejamento.siconv.med.medicao.entity.dto.AnexoDTO;

public interface AnexoDAO {

	@SqlBatch("INSERT INTO siconv.med_anexo (nm_arquivo, co_ceph, observacao_fk, adt_login, adt_data_hora, adt_operacao) "
			+ " VALUES (:nmArquivo, :coCeph, :observacaoFk, current_setting('med.cpf_usuario'), LOCALTIMESTAMP, 'INSERT')")
	void inserirAnexo(@BindBean List<AnexoBD> listaAnexos);
	
	/**
	 * Exclui todos os anexos da observação
	 * @param medObsFk
	 */
	@SqlUpdate("DELETE FROM siconv.med_anexo WHERE observacao_fk = :observacaoFk")
	void excluirAnexoPorObservacaoFK(@Bind("observacaoFk") Long observacaoFk);
	
	@SqlQuery("SELECT id FROM siconv.med_anexo WHERE observacao_fk = :observacaoFk")
	List<Long> buscarIdAnexoPorIdObservacao(@Bind("observacaoFk") Long observacaoFk);
	
	@SqlUpdate("DELETE FROM siconv.med_anexo WHERE id IN (<listaIdAnexo>)")
	void excluirAnexoPorListaIdAnexo(@BindList("listaIdAnexo") List<Long> listaIdAnexo);

	@SqlQuery("SELECT * FROM siconv.med_anexo WHERE id = :anexoFk")
	@RegisterFieldMapper(AnexoDTO.class)
	AnexoDTO buscarAnexoPorId(@Bind("anexoFk") Long anexoFk);	
	
	@SqlUpdate("UPDATE siconv.med_anexo Set in_inativo = true, nr_cpf_inativo = :nrCpfInativo WHERE id = :anexoFk")
	void inativarAnexoPorId(@Bind("anexoFk") Long anexoFk, @Bind("nrCpfInativo") String nrCpfInativo);	
	
	@SqlUpdate("UPDATE siconv.med_anexo Set in_inativo = false, nr_cpf_inativo = null WHERE id = :anexoFk")
	void ativarAnexoPorId(@Bind("anexoFk") Long anexoFk);	
	
	@SqlUpdate("DELETE FROM siconv.med_anexo anexo WHERE anexo.observacao_fk "
			+ " IN (SELECT med_observacao.id from med_observacao WHERE med_observacao.medicao_fk = :idMedicao)")
	void excluirAnexoPorIdMedicao(@Bind("idMedicao") Long idMedicao);
}
