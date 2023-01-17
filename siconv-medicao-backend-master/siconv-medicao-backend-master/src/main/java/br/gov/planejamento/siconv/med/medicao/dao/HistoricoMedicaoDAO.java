package br.gov.planejamento.siconv.med.medicao.dao;

import java.util.List;
import java.util.Optional;

import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import br.gov.planejamento.siconv.med.medicao.entity.database.HistoricoMedicaoBD;
import br.gov.planejamento.siconv.med.medicao.entity.dto.HistoricoMedicaoDTO;

public interface HistoricoMedicaoDAO {

	@SqlUpdate(
		"INSERT INTO siconv.med_historico_medicao "
			+ "(med_contrato_fk, nr_cpf_responsavel, in_perfil_responsavel, nr_sequencial, in_situacao, dt_registro, adt_login, adt_data_hora, adt_operacao)"
			+ " VALUES (:idContratoMedicao, :nrCpfResponsavel, :inPerfilResponsavel, :nrSequencial, :situacao, LOCALTIMESTAMP, current_setting('med.cpf_usuario'), LOCALTIMESTAMP, 'INSERT')")
	@RegisterFieldMapper(HistoricoMedicaoBD.class)
	@GetGeneratedKeys
	Long inserir(@BindBean HistoricoMedicaoBD submeta);
	
	/**
	 * 
	 * @param historico de medicao por contrato de execução
	 * @return
	 */	
	@SqlQuery(" select histmed.id                    as historico_id," +
	          "        histmed.med_contrato_fk       as historico_id_contrato_medicao, " + 
			  "        histmed.nr_cpf_responsavel    as historico_nr_cpf_responsavel, " + 
			  "        histmed.in_perfil_responsavel as historico_perfil, " +
			  "        histmed.nr_sequencial         as historico_nr_sequencial, " +
			  "        histmed.in_situacao           as historico_situacao, " +
			  "        histmed.dt_registro           as historico_data_hora " +
			  " FROM   med_historico_medicao histmed," + 
			  "        med_contrato"  +
			   " WHERE med_contrato.contrato_fk = :idContratoSiconv" +
			   " AND   histmed.med_contrato_fk = med_contrato.id" 
			+ " order by  historico_data_hora DESC, historico_nr_sequencial DESC")
	@RegisterFieldMapper(value = HistoricoMedicaoDTO.class, prefix = "historico")
	List<HistoricoMedicaoDTO> recuperarHistoricoMedicaoPorContrato(@Bind("idContratoSiconv") Long idContratoSiconv);

	/**
	 * 
	 * @param Identificador do Contrato
	 * @param Número da medição
	 * @return Último registro no histórico de uma determinada medição do contrato especificado  
	 */	
	@SqlQuery(" SELECT histmed.* " +
			  " FROM  med_historico_medicao histmed " + 
			  " WHERE histmed.id = (SELECT max(historico.id) " + 
			  "                     FROM med_historico_medicao historico " +
			  "                     WHERE historico.med_contrato_fk = :idContrato " +
			  "                     AND   historico.nr_sequencial = :sequencialMedicao)")
	@RegisterFieldMapper(value = HistoricoMedicaoBD.class)
	Optional<HistoricoMedicaoBD> recuperarUltimoHistoricoPorMedicaoContrato(@Bind("idContrato") Long idContrato, 
			@Bind("sequencialMedicao") Short sequencialMedicao);

	/**
	 * 
	 * @param Identificador do Contrato
	 * @param Número        da medição
	 * @return Histórico <b>da</b> medição
	 */
	@SqlQuery(" SELECT histmed.*" +
			  " FROM  med_historico_medicao histmed " + 
			  " WHERE histmed.id IN (SELECT historico.id " + 
			  "                     FROM med_historico_medicao historico " +
			  "                     WHERE historico.med_contrato_fk = :idContrato " +
			  "                     AND   historico.nr_sequencial = :sequencialMedicao) " + 
			  " ORDER BY histmed.id")
	@RegisterFieldMapper(value = HistoricoMedicaoBD.class)
	List<HistoricoMedicaoBD> recuperarHistoricoMedicao(@Bind("idContrato") Long idContrato,
			@Bind("sequencialMedicao") Short sequencialMedicao);

	@SqlUpdate("DELETE from siconv.med_historico_medicao "
			 + "WHERE med_contrato_fk = :idContrato")
	void excluir(@Bind("idContrato") Long idContrato);
	
}
