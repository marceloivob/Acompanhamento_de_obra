package br.gov.planejamento.siconv.med.medicao.dao;

import java.util.List;

import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Define;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.statement.UseRowReducer;
import org.jdbi.v3.stringtemplate4.UseStringTemplateEngine;

import br.gov.planejamento.siconv.med.medicao.dao.reducer.ObservacaoReducer;
import br.gov.planejamento.siconv.med.medicao.entity.database.ObservacaoBD;
import br.gov.planejamento.siconv.med.medicao.entity.dto.AnexoDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.ObservacaoDTO;

public interface ObservacaoDAO {

	/**
	 * 
	 * @param observacao
	 * @return
	 */
	@SqlUpdate("INSERT INTO siconv.med_observacao "
			+ "(dt_registro, in_perfil_responsavel, nr_cpf_responsavel, tx_observacao, medicao_fk, versao, adt_login, adt_data_hora, adt_operacao)"
			+ " VALUES (LOCALTIMESTAMP, :inPerfilResponsavel, :nrCpfResponsavel, :txObservacao, :medicaoFk, 1, current_setting('med.cpf_usuario'), LOCALTIMESTAMP, 'INSERT')")
	@RegisterFieldMapper(ObservacaoBD.class)
	@GetGeneratedKeys
	Long inserirObservacao(@Bind("medicaoFk") Long medicaoFk, @BindBean ObservacaoBD observacao);
	
	@SqlUpdate("DELETE FROM siconv.med_observacao WHERE id = :idObservacao AND medicao_fk = :idMedicao")
	void excluirObservacao(@Bind("idMedicao") Long idMedicao, @Bind("idObservacao") Long idObservacao);
	
	@SqlUpdate("UPDATE siconv.med_observacao "
			+ "SET dt_registro = LOCALTIMESTAMP, in_perfil_responsavel = :inPerfilResponsavel, nr_cpf_responsavel = :nrCpfResponsavel, tx_observacao =:txObservacao,"
			+ " versao = :versao + 1, adt_login = current_setting('med.cpf_usuario'), adt_data_hora = LOCALTIMESTAMP, adt_operacao = 'UPDATE' "
			+ " WHERE id = :idObservacao AND medicao_fk =:idMedicao")
	@RegisterFieldMapper(ObservacaoBD.class)
	void alterarObservacao(@Bind("idMedicao") Long idMedicao, @Bind("idObservacao") Long idObservacao,
			@BindBean ObservacaoBD observacao);

	@SqlQuery(" select observ.id as observ_id, observ.versao as observ_versao,   " + 
			"       observ.dt_registro as observ_dt_registro, " + 
			"       observ.in_perfil_responsavel as observ_in_perfil_responsavel, " + 
			"       observ.nr_cpf_responsavel as observ_nr_cpf_responsavel, " + 
			"       observ.tx_observacao as observ_tx_observacao, " + 
			"       observ.medicao_fk as observ_medicao_fk, " + 
			"       observ.in_bloqueio as observ_in_bloqueio, " + 
			"       anexo.id as anexo_id, " + 
			"       anexo.nm_arquivo as anexo_nm_arquivo, " + 
			"       anexo.co_ceph as anexo_co_ceph, " + 
			"       anexo.observacao_fk as anexo_observacao_fk, " +
			"       anexo.in_inativo as anexo_in_inativo, " +
			"       anexo.nr_cpf_inativo as anexo_nr_cpf_inativo " +
			" FROM med_observacao observ " + 
			" left join med_anexo anexo on observ.id = anexo.observacao_fk " + 
			" WHERE observ.medicao_fk = :medicao_fk " +
			"   <if(apenasBloqueada)> AND observ.in_bloqueio = TRUE <endif> " + 
			"   order by observ_dt_registro desc ")
	@RegisterFieldMapper(value = ObservacaoDTO.class, prefix = "observ")
	@RegisterFieldMapper(value = AnexoDTO.class, prefix = "anexo")
	@UseStringTemplateEngine
	@UseRowReducer(ObservacaoReducer.class)
	List<ObservacaoDTO> recuperarObservacaoPorMedicao(@Bind("medicao_fk") Long medicaoFk,
			@Define("apenasBloqueada") @Bind("apenasBloqueada") boolean apenasBloqueada);

	@SqlQuery(" select observ.id as observ_id, observ.versao as observ_versao, " + 
			"       observ.dt_registro as observ_dt_registro, " + 
			"       observ.in_perfil_responsavel as observ_in_perfil_responsavel, " + 
			"       observ.nr_cpf_responsavel as observ_nr_cpf_responsavel, " + 
			"       observ.tx_observacao as observ_tx_observacao, " + 
			"       observ.medicao_fk as observ_medicao_fk, " + 
			"       observ.in_bloqueio as observ_in_bloqueio, " + 
			"       medicao.nr_sequencial as observ_sequencial_medicao_agrupada, " + 
			"       anexo.id as anexo_id, " + 
			"       anexo.nm_arquivo as anexo_nm_arquivo, " + 
			"       anexo.co_ceph as anexo_co_ceph, " + 
			"       anexo.observacao_fk as anexo_observacao_fk, " + 
			"       anexo.in_inativo as anexo_in_inativo, " + 	
			"       anexo.nr_cpf_inativo as anexo_nr_cpf_inativo " +			
			" FROM med_medicao medicao "	+ 
			" JOIN med_observacao observ ON medicao.id = observ.medicao_fk " + 
			" LEFT JOIN med_anexo anexo ON observ.id = anexo.observacao_fk " + 
			" WHERE observ.medicao_fk IN (SELECT id FROM med_medicao mmInterno WHERE mmInterno.medicao_fk_agrupadora = :medicao_fk) " +
			"   <if(apenasBloqueada)> AND observ.in_bloqueio = TRUE <endif>" + 
			"   ORDER BY observ_dt_registro DESC ")
	@RegisterFieldMapper(value = ObservacaoDTO.class, prefix = "observ")
	@RegisterFieldMapper(value = AnexoDTO.class, prefix = "anexo")
	@UseStringTemplateEngine
	@UseRowReducer(ObservacaoReducer.class)
	List<ObservacaoDTO> recuperarObservacaoMedicoesAgrupadas(@Bind("medicao_fk") Long medicaoFk,
			@Define("apenasBloqueada") @Bind("apenasBloqueada") boolean apenasBloqueada);

	@SqlQuery(" SELECT * FROM med_observacao " 
	        + " WHERE med_observacao.id = :observacaoFk "
	        + "<if(apenasBloqueada)> AND med_observacao.in_bloqueio = TRUE <endif>")
	@RegisterFieldMapper(ObservacaoDTO.class)
	@UseStringTemplateEngine
	ObservacaoDTO recuperarObservacaoPorId(@Bind("observacaoFk") Long observacaoFk,
			@Define("apenasBloqueada") @Bind("apenasBloqueada") boolean apenasBloqueada);
	
	@SqlQuery(" SELECT * FROM siconv.med_observacao " 
	        + " WHERE med_observacao.id = :observacaoFk AND med_observacao.medicao_fk = :medicaoFk ")
	@RegisterFieldMapper(ObservacaoDTO.class)
	ObservacaoDTO recuperarObservacaoPorId(@Bind("observacaoFk") Long observacaoFk, @Bind("medicaoFk") Long medicaoFk );
	
	@SqlUpdate("DELETE FROM siconv.med_observacao WHERE medicao_fk = :idMedicao")
	void excluirObservacaoPorIdMedicao(@Bind("idMedicao") Long idMedicao);

	@SqlUpdate("UPDATE siconv.med_observacao "
			+ " SET in_bloqueio = true, "
			+ " versao = :versao + 1, adt_login = current_setting('med.cpf_usuario'), adt_data_hora = LOCALTIMESTAMP, adt_operacao = 'UPDATE' "
			+ " WHERE id =:id")
	void bloquearObservacao(@BindBean ObservacaoBD observacao);

	
	@SqlQuery(" SELECT * FROM siconv.med_observacao " 
	        + " WHERE medicao_fk = :idMedicao AND in_bloqueio = :inBloqueda "
	        + "       <if (perfilEmpresa)> and in_perfil_responsavel = 'EMP' <endif> " 
	        + "       <if (perfilConvenente)>  and in_perfil_responsavel = 'CVE' <endif> " 
	        + "       <if (perfilConcedente)> and in_perfil_responsavel in ('CCE','MAN','FSA','TTE') <endif> " )
	@RegisterFieldMapper(ObservacaoBD.class)
	@UseStringTemplateEngine
	List<ObservacaoBD> consultarObservacoesPorBloqueioPerfil(@Bind("idMedicao") Long idMedicao, @Bind("inBloqueda") Boolean inBloqueda, @Define("perfilEmpresa") @Bind("perfilEmpresa") Boolean perfilEmpresa, @Define("perfilConvenente") @Bind("perfilConvenente") Boolean perfilConvenente, @Define("perfilConcedente") @Bind("perfilConcedente") Boolean perfilConcedente);

	
	@SqlUpdate("UPDATE siconv.med_observacao "
			+ " SET medicao_fk = :idMedicaoAgrupadora, "
			+ " versao = :versao + 1, adt_login = current_setting('med.cpf_usuario'), adt_data_hora = LOCALTIMESTAMP, adt_operacao = 'UPDATE' "
			+ " WHERE id =:id")
	void moverObservacaoMedicaoAgrupadaMedicaoAgrupadora(@BindBean ObservacaoBD observacao, @Bind("idMedicaoAgrupadora") Long idMedicaoAgrupadora);

	@SqlQuery("SELECT COUNT(*) > 0 "
				+ "FROM med_observacao obs " 
				+ "INNER JOIN med_medicao med ON med.id = obs.medicao_fk " 
				+ "WHERE med.id = :idMedicao " 
				+ "AND obs.nr_cpf_responsavel = :cpfResponsavel " 
				+ "AND obs.in_bloqueio = false;")
	boolean existeObservacaoCadastradaPorUsuarioDesbloqueadaNaMedicao(@Bind("idMedicao") Long idMedicao, @Bind("cpfResponsavel") String cpfResponsavel);
}
