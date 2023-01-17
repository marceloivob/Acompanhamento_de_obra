package br.gov.planejamento.siconv.med.medicao.dao;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jdbi.v3.sqlobject.config.KeyColumn;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.config.ValueColumn;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.BindFields;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum;
import br.gov.planejamento.siconv.med.medicao.entity.database.MedicaoBD;
import br.gov.planejamento.siconv.med.medicao.entity.dto.MedicaoDTO;

public interface MedicaoDAO {

	/**
	 * Obtém a última Medição de um ID de contrato VRPL passado com argumento
	 * 
	 * @param idContratoVRPL
	 * @return
	 */
	@SqlQuery(" SELECT med.* "
			+ " FROM med_medicao med "
			+ " WHERE med.id = ( "
			+ "     SELECT MAX(med_sub.id) "
			+ "     FROM med_medicao med_sub "
			+ "      JOIN med_contrato con_sub ON con_sub.id = med_sub.med_contrato_fk "
			+ "     WHERE con_sub.contrato_fk = :idContratoVRPL"
			+ "   )")
	@RegisterFieldMapper(MedicaoBD.class)	
	MedicaoBD consultarUltimaMedicao (@Bind("idContratoVRPL") Long idContratoVRPL);
	
	/**
	 * Obtém a Medição mais recente com situação diferente de 'Em Elaboração' e 'Em
	 * complementação pela Empresa', a partir de um ID de contrato VRPL
	 * 
	 * @param idContratoVRPL
	 * @return
	 */
	@SqlQuery ("  select medicao.* from med_medicao medicao, med_contrato contrato " +
				" where contrato.id = medicao.med_contrato_fk and " +
				"       contrato.contrato_fk = Cast(:idContratoVRPL as int8) and " +
				"       (medicao.id = (select max(medicao_sub.id) from med_medicao medicao_sub ,med_contrato contrato_sub " + 
				"                      where contrato_sub.id = medicao_sub.med_contrato_fk  and " +
				"                            contrato.id = contrato_sub.id and " +
				"                            medicao_sub.in_situacao not in ('EM', 'CE')))")
	@RegisterFieldMapper(MedicaoBD.class)	
	Optional<MedicaoBD> consultarUltimaMedicaoPublicaEmpresa (@Bind("idContratoVRPL") Long idContratoVRPL);
	
		
	@SqlQuery(" SELECT medicao.id as id, " +
			"       medicao.nr_sequencial as sequencial, " +
			"       medicao.dt_inicio as dataInicio, " +
	        "       medicao.dt_fim as dataFim, " +
	        "       medicao.in_bloqueio, " +
	        "       medicao.in_situacao, medicao.versao as versao, " +
	        "       medicao.in_complementacao_valor, " +
	        "       med_contrato.dt_inicio_obra as dataInicioObra, " +
	        "       med_contrato.id as idContrato, " +
	        "       medicao.medicao_fk_agrupadora as idMedicaoAgrupadora, " +	        	        
	        "       ( SELECT COUNT(submeta) > 0 " +
	        "            FROM  siconv.med_submeta_medicao submeta " +
	        "              WHERE submeta.medicao_fk = medicao.id AND submeta.dt_assinatura_empresa IS NOT NULL" +
	        "       ) as possuiSubmetaAssinada " +
	        " FROM  siconv.med_contrato, " +
	        "       siconv.med_medicao medicao "+
	        " WHERE med_contrato.id = medicao.med_contrato_fk " +
	        "   AND med_contrato.contrato_fk = Cast(:idContrato as int8) " + 
	        "   ORDER BY medicao.nr_sequencial")
	@RegisterFieldMapper(MedicaoDTO.class)
	List<MedicaoDTO> listarMedicoes(@Bind("idContrato") Long idContrato);

	
	@SqlQuery(" SELECT COUNT(medicao) as qtdeMedicoes " +
	        "  FROM siconv.med_contrato, " +
	        "       siconv.med_medicao medicao " +
	        "  WHERE med_contrato.id = medicao.med_contrato_fk and " +
	        "        med_contrato.contrato_fk = Cast(:idContrato as int8) ")
	int consultarQtdeMedicoesPorContrato(@Bind("idContrato") Long idContrato);
	
	@SqlQuery(" Select medicao.id as id, " +
			"       medicao.nr_sequencial as sequencial, " +
			"       medicao.dt_inicio as dataInicio, " +
	        "       medicao.dt_fim as dataFim, " +			
	        "       medicao.in_bloqueio, " +
	        "       medicao.dt_vistoria_extra as dataVistoriaExtra, " +			
	        "       medicao.in_solicitante_vistoria, " +
	        "       medicao.in_vistoria_extra, " +
	        "       medicao.in_situacao, " +
	        "       medicao.in_complementacao_valor, " +
	        "       medicao.versao as versao, " +
	        "       medicao.medicao_fk_agrupadora as idMedicaoAgrupadora, " +
	        "       med_agrupadora.nr_sequencial as sequencialMedicaoAgrupadora, " +
	        "       med_contrato.dt_inicio_obra as dataInicioObra, " +
	        "       med_contrato.id as idContrato, " +
	        "       med_contrato.contrato_fk as idContratoSiconv, " +
	        "       exists (select 1 from med_medicao mm " + 
	        "               where mm.medicao_fk_agrupadora = Cast(:idMedicao as int8) " + 
	        "               ) as isMedicaoAgrupadora " +
	        " From med_medicao medicao " +
	        " Inner Join med_contrato on med_contrato.id = medicao.med_contrato_fk " +
	        " Left Join med_medicao med_agrupadora on medicao.medicao_fk_agrupadora = med_agrupadora.id " +
	        " Where  medicao.id = Cast(:idMedicao as int8) " +
	        " Order by medicao.nr_sequencial")
	@RegisterFieldMapper(MedicaoDTO.class)
	MedicaoDTO obterMedicao(@Bind("idMedicao") Long idMedicao);


	@SqlQuery(" Select * from med_medicao med "+
	          " where med.id = Cast(:idMedicao as int8) ")
	@RegisterFieldMapper(MedicaoBD.class)
	MedicaoBD consultarMedicao(@Bind("idMedicao") Long idMedicao);	
	
	@SqlQuery(" Select med.* from med_medicao med, med_contrato contr "+
	          " where med.med_contrato_fk = contr.id and " +
	          "       contr.contrato_fk = Cast(:idContrato as int8) and " +
	          "       med.nr_sequencial = :seq ")
	@RegisterFieldMapper(MedicaoBD.class)
	MedicaoBD consultarMedicaoPorSequencial(@Bind("idContrato") Long idContrato, @Bind("seq") Short seq);		
	
	
	@SqlUpdate("INSERT INTO med_medicao (nr_sequencial, dt_inicio, dt_fim, in_situacao, med_contrato_fk, versao, adt_login, adt_data_hora, adt_operacao) "
			+ " VALUES ( :nrSequencial, :dtInicio, :dtFim, :situacao, :idContratoMedicao, 1, current_setting('med.cpf_usuario'), LOCALTIMESTAMP, 'INSERT' )")
    @RegisterFieldMapper(MedicaoBD.class)
	@GetGeneratedKeys
	Long inserir(@BindFields MedicaoBD medicao);

	@SqlUpdate("UPDATE med_medicao"
	        + " SET dt_inicio = :dtInicio,"
			+ "     dt_fim = :dtFim,"
	        + "     in_situacao = :situacao,"
			+ "     in_vistoria_extra = :vistoriaExtra,"
			+ "     dt_vistoria_extra = :dataVistoriaExtra,"
			+ "     in_solicitante_vistoria = :solicitanteVistoriaExtra,"
			+ "     in_complementacao_valor = :permiteComplementacaoValor,"
			+ "     medicao_fk_agrupadora = :idMedicaoAgrupadora,"
	        + "     in_bloqueio = :bloqueada,"
			+ "     versao = :versao + 1,"
	        + "     adt_login = current_setting('med.cpf_usuario'),"
	        + "     adt_data_hora = LOCALTIMESTAMP,"
	        + "     adt_operacao = 'UPDATE'"
			+ " WHERE id = :id")
	void alterar(@BindBean MedicaoBD medicao);
	
	@SqlQuery("SELECT COUNT(medicao) > 0 "
			+ " FROM siconv.med_contrato contrato, siconv.med_medicao medicao  "
			+ " WHERE contrato.id = medicao.med_contrato_fk "
			+ "    AND medicao.in_situacao = 'EC' "
			+ "    AND medicao.id = :idMedicao "
			+ "    AND medicao.nr_sequencial = (SELECT MAX(NR_SEQUENCIAL) FROM med_medicao WHERE med_medicao.med_contrato_fk = medicao.med_contrato_FK )")
	Boolean permiteCancelarEnvioConvenente(@Bind("idMedicao") Long idMedicao);
	
	@SqlUpdate("DELETE FROM siconv.med_medicao WHERE id = :idMedicao")
	void excluirMedicaoPorId(@Bind("idMedicao") Long idMedicao);

	@SqlQuery("SELECT id, in_situacao FROM siconv.med_medicao WHERE med_contrato_fk = :idContratoMedicao")
	@KeyColumn("id")
	@ValueColumn("in_situacao")
	Map<Long, SituacaoMedicaoEnum> listarSituacoesMedicoes(@Bind("idContratoMedicao") Long idContratoMedicao);
	

	@SqlQuery ("  select medicao.* from med_medicao medicao, med_contrato contrato " +
			" where contrato.id = medicao.med_contrato_fk and " +
			"           contrato.id = Cast(:idContratoMedicao as int8) and " +
			"           medicao.nr_sequencial < Cast(:sequencial as int8) and " +
			"           medicao.in_situacao in (<listaSituacao>) ")
	@RegisterFieldMapper(MedicaoBD.class)
	List<MedicaoBD> listarMedicoesAnterioresPorSituacao (@Bind("idContratoMedicao") Long idContratoMedicao,@Bind("sequencial") Short sequencial, @BindList("listaSituacao") List<SituacaoMedicaoEnum> listaSituacao);

	@SqlQuery(" SELECT medicao.* FROM med_medicao medicao" +
			" JOIN med_observacao observ ON medicao.id = observ.medicao_fk" +
			"  WHERE observ.id = :idObservacao")
	@RegisterFieldMapper(MedicaoBD.class)
	Optional<MedicaoBD> consultarMedicaoPorIdObs(@Bind("idObservacao") Long idObservacao);

	@SqlQuery(" SELECT COUNT(med.id) > 0"
			+ " FROM siconv.med_medicao med"
			+ " WHERE med.med_contrato_fk = :idContratoMedicao"
			+ "   AND med.in_situacao IN (<situacoes>)")
	Boolean existeMedicao(@Bind("idContratoMedicao") Long idContratoMedicao,
			@BindList("situacoes") List<SituacaoMedicaoEnum> situacoes);
	
	@SqlQuery(" SELECT med.id"
			+ " FROM siconv.med_medicao med"
			+ " WHERE med.medicao_fk_agrupadora = :idMedicaoAgrupadora"
			+ " ORDER BY med.id")
	List<Long> listarIdMedicoesAcumuladas(@Bind("idMedicaoAgrupadora") Long idMedicaoAgrupadora);
	
	@SqlQuery(" SELECT med.*"
			+ " FROM siconv.med_medicao med"
			+ " WHERE med.medicao_fk_agrupadora = :idMedicaoAgrupadora"
			+ " ORDER BY med.id")
	@RegisterFieldMapper(MedicaoBD.class)
	List<MedicaoBD> listarMedicoesAcumuladas(@Bind("idMedicaoAgrupadora") Long idMedicaoAgrupadora);
	
	@SqlQuery(" SELECT med.*"
			+ " FROM siconv.med_medicao med"
			+ " WHERE med.med_contrato_fk = :idContratoMedicao"
			+ "   AND med.in_situacao = :situacao")
	@RegisterFieldMapper(MedicaoBD.class)
	List<MedicaoBD> consultarMedicaoporSituacao(@Bind("idContratoMedicao") Long idContratoMedicao, @Bind("situacao") SituacaoMedicaoEnum situacao);
	
	@SqlUpdate("UPDATE siconv.med_medicao "
			+ " SET in_bloqueio = true, versao = :versao + 1, adt_login = current_setting('med.cpf_usuario'), adt_data_hora = LOCALTIMESTAMP, adt_operacao = 'UPDATE' "
			+ " WHERE id = :id")
	void bloquearMedicao(@BindBean MedicaoBD medicao);

	@SqlQuery("SELECT COUNT(medicao) > 0 "
			+ " FROM siconv.med_contrato contrato, siconv.med_medicao medicao  "
			+ " WHERE contrato.id = medicao.med_contrato_fk "
			+ "    AND medicao.in_situacao = 'ATD' "
			+ "    AND medicao.id = :idMedicao "
			+ "    AND medicao.nr_sequencial = (SELECT MAX(NR_SEQUENCIAL) FROM med_medicao WHERE med_medicao.med_contrato_fk = medicao.med_contrato_FK  AND med_medicao.in_situacao IN ('ATD', 'AT') )")
	Boolean permiteCancelarEnvioConcedente(@Bind("idMedicao") Long idMedicao);
	
	@SqlUpdate("UPDATE siconv.med_medicao "
			+ " SET in_bloqueio = false, versao = :versao + 1, adt_login = current_setting('med.cpf_usuario'), adt_data_hora = LOCALTIMESTAMP, adt_operacao = 'UPDATE' "
			+ " WHERE id = :id")
	void desbloquearMedicao(@BindBean MedicaoBD medicao);

	@SqlQuery("SELECT COUNT(medicao) > 0 "
			+ " FROM siconv.med_contrato contrato, siconv.med_medicao medicao  "
			+ " WHERE contrato.id = medicao.med_contrato_fk "
			+ "    AND medicao.in_situacao = 'ACT' "
			+ "    AND medicao.id = :idMedicao "
			+ "    AND medicao.nr_sequencial = (SELECT MAX(NR_SEQUENCIAL) "
			+ "        FROM med_medicao WHERE med_medicao.med_contrato_fk = medicao.med_contrato_FK AND med_medicao.in_situacao = 'ACT') "
			+ "    AND NOT EXISTS "
			+ "        (SELECT 1 FROM med_medicao m "
			+ "         WHERE m.med_contrato_fk = medicao.med_contrato_FK "
			+ "         AND m.nr_sequencial > medicao.nr_sequencial "
			+ "         AND (m.in_situacao = 'AC' OR m.in_complementacao_valor IS NOT NULL) )")
	Boolean permiteCancelarAceite(@Bind("idMedicao") Long idMedicao);
	
	@SqlQuery("SELECT id, in_complementacao_valor FROM siconv.med_medicao WHERE med_contrato_fk = :idContratoMedicao")
	@KeyColumn("id")
	@ValueColumn("in_complementacao_valor")
	Map<Long, Boolean> listarIndicadorComplementacaoValorMedicoes(@Bind("idContratoMedicao") Long idContratoMedicao);

}
