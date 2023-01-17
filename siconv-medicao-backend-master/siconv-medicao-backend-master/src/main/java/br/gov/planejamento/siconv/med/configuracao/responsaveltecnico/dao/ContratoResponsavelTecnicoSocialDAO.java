package br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.dao;

import java.util.List;
import java.util.Optional;

import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlBatch;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.statement.UseRowReducer;
import org.jdbi.v3.stringtemplate4.UseStringTemplateEngine;

import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.database.ContratoResponsavelTecnicoSocialBD;
import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.database.SubmetaResponsavelTecnicoSocialBD;
import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.dto.ContratoResponsavelTecnicoSocialDTO;
import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.dto.ResponsavelTecnicoDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.SubmetaVrplDTO;

public interface ContratoResponsavelTecnicoSocialDAO {

	@SqlUpdate(" INSERT INTO siconv.med_contrato_resp_tecnico_social " +
			   " (med_contrato_fk, med_responsavel_tecnico_fk, in_tipo, in_atividade, nm_registro_profissional, " +
			   "  nm_arquivo_curriculo, co_ceph_curriculo, nm_formacao, nm_orgao_responsavel, nr_telefone_orgao, tx_email_orgao, " + 
			   "  dt_inclusao, versao, adt_login, adt_data_hora, adt_operacao)" +
 			   " VALUES (:contrato, :responsavelTecnico, :tipo, :atividade, :registroProfissional, :nmArquivoCurriculo, :coCephCurriculo, :formacao, " +
			   " :nmOrgaoResponsavel, :nrTelefoneOrgao, :txEmailOrgao, LOCALTIMESTAMP, 1, current_setting('med.cpf_usuario'), LOCALTIMESTAMP, 'INSERT')")
	@RegisterFieldMapper(ContratoResponsavelTecnicoSocialBD.class)
	@GetGeneratedKeys
	ContratoResponsavelTecnicoSocialBD inserir(@BindBean ContratoResponsavelTecnicoSocialBD contratoResponsavelTecnicoSocialBD);
	
	@SqlUpdate(" UPDATE siconv.med_contrato_resp_tecnico_social "
            + " SET nm_arquivo_curriculo = :nmArquivoCurriculo, "
            + "     co_ceph_curriculo = :coCephCurriculo, "
            + "     nm_formacao = :formacao, "
            + "     nm_registro_profissional = :registroProfissional, "
            + "     nm_orgao_responsavel = :nmOrgaoResponsavel, "
            + "     nr_telefone_orgao = :nrTelefoneOrgao, "
            + "     tx_email_orgao = :txEmailOrgao, "
            + "     versao = :versao + 1, "
            + "     adt_login = current_setting('med.cpf_usuario'), "
            + "     adt_data_hora = LOCALTIMESTAMP, "
            + "     adt_operacao = 'UPDATE' "
            + " WHERE id = :id ")
	boolean alterar(@BindBean ContratoResponsavelTecnicoSocialBD contratoResponsavelTecnicoSocialBD);
	
	@SqlBatch("INSERT INTO siconv.med_contrato_rt_social_submeta (vrpl_submeta_fk, med_contrato_resp_tecnico_social_fk, adt_login, adt_data_hora, adt_operacao) "
			+ " VALUES (:vrplSubmetaFk, :responsavelTencicoSocialFk, current_setting('med.cpf_usuario'), LOCALTIMESTAMP, 'INSERT')")
	void inserirResponsavelTecnicoSocialSubmeta(@BindBean List<SubmetaResponsavelTecnicoSocialBD> submetas);
	
	@SqlBatch("DELETE FROM siconv.med_contrato_rt_social_submeta WHERE vrpl_submeta_fk = :vrplSubmetaFk AND med_contrato_resp_tecnico_social_fk = :responsavelTencicoSocialFk")
	void deletarResponsavelTecnicoSocialSubmeta(@BindBean List<SubmetaResponsavelTecnicoSocialBD> submetas);
	
	@SqlQuery(" SELECT  " + 
			"          rtsocial.id as rtsocial_id, " +
			"          contrato.id as rtsocial_med_contrato_fk,  " + 
			"          rtsocial.dt_inclusao as rtsocial_dt_inclusao,  " + 
			"          rtsocial.dt_inativacao as rtsocial_dt_inativacao,  " + 
			"          rtsocial.nm_formacao as rtsocial_nm_formacao,  " + 
			"          rtsocial.nm_registro_profissional as rtsocial_nm_registro_profissional,  " + 
			"          rtsocial.in_tipo as rtsocial_in_tipo,  " + 
			"          rtsocial.in_atividade as rtsocial_in_atividade,  " + 
			"          rtsocial.nm_arquivo_curriculo as rtsocial_nm_arquivo_curriculo, " + 
			"          rtsocial.co_ceph_curriculo as rtsocial_co_ceph_curriculo,  " + 
			"          rtsocial.nm_orgao_responsavel as orgao_nm_orgao_responsavel,  " + 
			"          rtsocial.nr_telefone_orgao as orgao_nr_telefone_orgao,  " + 
			"          rtsocial.tx_email_orgao as orgao_tx_email_orgao, " + 
			"          rtsocial.versao as rtsocial_versao, " + 
			"          rt.id as rt_id," + 
			"          rt.nr_cpf as rt_nr_cpf, " + 
			"          rt.versao as rt_versao, " + 
			"          sub.vrpl_submeta_fk as sub_id," + 
			"          (SELECT COUNT(submeta) > 0  " + 
			"                    FROM siconv.med_submeta_medicao submeta, " + 
			"                              siconv.med_medicao med  " + 
			"          WHERE  " + 
			"          submeta.medicao_fk = med.id and  " + 
			"          med.med_contrato_fk = contrato.id and  " + 
			"          (submeta.nr_cpf_resp_empresa = rt.nr_cpf " +
            "                    OR submeta.nr_cpf_resp_convenente = rt.nr_cpf) " +
			"          ) as rtsocial_possui_submeta_assinada  " + 
			"  FROM siconv.med_contrato as contrato,  " + 
			"          siconv.med_contrato_resp_tecnico_social as rtsocial,  " + 
			"          siconv.med_responsavel_tecnico as rt, " + 
			"          siconv.med_contrato_rt_social_submeta sub " + 
			"  WHERE contrato.id = rtsocial.med_contrato_fk and  " + 
			"          rtsocial.med_responsavel_tecnico_fk = rt.id and  " + 
			"          sub.med_contrato_resp_tecnico_social_fk = rtsocial.id and " + 
			"          contrato.contrato_fk = Cast(:idContratoSiconv as int8)  " + 
			"  ORDER BY rtsocial.dt_inclusao DESC ")
	@RegisterFieldMapper(value = ContratoResponsavelTecnicoSocialDTO.class, prefix = "rtsocial")
	@RegisterFieldMapper(value = ContratoResponsavelTecnicoSocialDTO.Orgao.class, prefix = "orgao")
	@RegisterFieldMapper(value = ResponsavelTecnicoDTO.class, prefix = "rt")
	@RegisterFieldMapper(value = SubmetaVrplDTO.class, prefix = "sub")
	@UseRowReducer(ContratoResponsavelTecnicoSocialReducer.class)
	List<ContratoResponsavelTecnicoSocialDTO> listarResponsavelTecnicoSocialPorContrato(
			@Bind("idContratoSiconv") Long idContratoSiconv);
	
	@SqlQuery(" SELECT count(rt.id) > 0  " + 
			  " FROM siconv.med_responsavel_tecnico as rt,  " + 
			  "   siconv.med_contrato_resp_tecnico_social as contrato_rt_social, " + 
			  "   siconv.med_contrato as contrato,  " + 
			  "   siconv.med_medicao as medicao, " + 
			  "   siconv.med_submeta_medicao as submeta_medicao " + 
			  " WHERE  " + 
			  "   contrato_rt_social.med_contrato_fk = contrato.id and  " + 
			  "   contrato.id = medicao.med_contrato_fk and " + 
			  "   medicao.id = submeta_medicao.medicao_fk and " + 
			  "   contrato_rt_social.id = :idMedContratoRTSocial and " + 
			  "   (rt.nr_cpf = submeta_medicao.nr_cpf_resp_empresa " +
			  "    OR rt.nr_cpf = submeta_medicao.nr_cpf_resp_convenente) and  " +
			  "   rt.id = contrato_rt_social.med_responsavel_tecnico_fk ")
	Boolean isContratoSocialAssinadoPeloResponsavelTecnico(@Bind("idMedContratoRTSocial") Long idMedContratoRTSocial);
	
	@SqlUpdate("UPDATE siconv.med_contrato_resp_tecnico_social SET dt_inativacao = LOCALTIMESTAMP, versao = :versao + 1, adt_login = current_setting('med.cpf_usuario'),"
            + " adt_data_hora = LOCALTIMESTAMP, adt_operacao = 'UPDATE' WHERE id = :id")
    boolean inativar(@BindBean ContratoResponsavelTecnicoSocialBD contratoResponsavelTecnicoSocialBD);
	
	@SqlQuery(" SELECT  " + 
			"          rtsocial.id as rtsocial_id, " +
			"          rtsocial.med_contrato_fk  as rtsocial_med_contrato_fk,  " + 
			"          rtsocial.dt_inclusao as rtsocial_dt_inclusao,  " + 
			"          rtsocial.dt_inativacao as rtsocial_dt_inativacao,  " + 
			"          rtsocial.nm_formacao as rtsocial_nm_formacao,  " + 
			"          rtsocial.nm_registro_profissional as rtsocial_nm_registro_profissional,  " + 
			"          rtsocial.in_tipo as rtsocial_in_tipo,  " + 
			"          rtsocial.in_atividade as rtsocial_in_atividade,  " + 
			"          rtsocial.nm_arquivo_curriculo as rtsocial_nm_arquivo_curriculo, " + 
			"          rtsocial.co_ceph_curriculo as rtsocial_co_ceph_curriculo,  " + 
			"          rtsocial.nm_orgao_responsavel as orgao_nm_orgao_responsavel,  " + 
			"          rtsocial.nr_telefone_orgao as orgao_nr_telefone_orgao,  " + 
			"          rtsocial.tx_email_orgao as orgao_tx_email_orgao, " + 
			"          rtsocial.versao as rtsocial_versao, " +
			"          rt.id as rt_id," + 
			"          rt.nr_cpf as rt_nr_cpf, " +
			"          rt.telefone as rt_telefone, " + 
			"          rt.versao as rt_versao, " + 
			"          submetaRtSocial.vrpl_submeta_fk as sub_id, " + 
			"          (SELECT COUNT(submeta) > 0  " + 
			"            FROM siconv.med_submeta_medicao submeta, " + 
			"          siconv.med_medicao med  " + 
			"  WHERE  " + 
			"          submeta.medicao_fk = med.id and  " + 
			"          med.med_contrato_fk = contrato.id and  " + 
			"          (submeta.nr_cpf_resp_empresa = rt.nr_cpf " +
            "                    OR submeta.nr_cpf_resp_convenente = rt.nr_cpf) " +
			"          ) as rtsocial_possui_submeta_assinada  " + 
			" FROM siconv.med_contrato_resp_tecnico_social as rtsocial  " + 
			" JOIN med_responsavel_tecnico rt ON rtsocial.med_responsavel_tecnico_fk = rt.id  " + 
			" JOIN med_contrato_rt_social_submeta submetaRtSocial ON rtsocial.id = submetaRtSocial.med_contrato_resp_tecnico_social_fk " +
			" JOIN med_contrato as contrato ON rtsocial.med_contrato_fk = contrato.id " + 
			" WHERE rtsocial.id = :id")
	@RegisterFieldMapper(value = ContratoResponsavelTecnicoSocialDTO.class, prefix = "rtsocial")
	@RegisterFieldMapper(value = ContratoResponsavelTecnicoSocialDTO.Orgao.class, prefix = "orgao")
	@RegisterFieldMapper(value = ResponsavelTecnicoDTO.class, prefix = "rt")
	@RegisterFieldMapper(value = SubmetaVrplDTO.class, prefix = "sub")
	@UseRowReducer(ContratoResponsavelTecnicoSocialReducer.class)
	Optional<ContratoResponsavelTecnicoSocialDTO> consultarContratoResponsavelTecnicoSocialPorId(@Bind("id") Long id);
	
	@SqlQuery(" SELECT  " + 
			"          rtsocial.id as rtsocial_id, " +
			"          rtsocial.med_contrato_fk  as rtsocial_med_contrato_fk,  " + 
			"          rtsocial.dt_inclusao as rtsocial_dt_inclusao,  " + 
			"          rtsocial.dt_inativacao as rtsocial_dt_inativacao,  " + 
			"          rtsocial.nm_formacao as rtsocial_nm_formacao,  " + 
			"          rtsocial.nm_registro_profissional as rtsocial_nm_registro_profissional,  " + 
			"          rtsocial.in_tipo as rtsocial_in_tipo,  " + 
			"          rtsocial.in_atividade as rtsocial_in_atividade,  " + 
			"          rtsocial.nm_arquivo_curriculo as rtsocial_nm_arquivo_curriculo, " + 
			"          rtsocial.co_ceph_curriculo as rtsocial_co_ceph_curriculo,  " + 
			"          rtsocial.nm_orgao_responsavel as orgao_nm_orgao_responsavel,  " + 
			"          rtsocial.nr_telefone_orgao as orgao_nr_telefone_orgao,  " + 
			"          rtsocial.tx_email_orgao as orgao_tx_email_orgao, " + 
			"          rtsocial.versao as rtsocial_versao, " +
			"          rt.id as rt_id," + 
			"          rt.nr_cpf as rt_nr_cpf, " +
			"          rt.telefone as rt_telefone, " + 
			"          rt.versao as rt_versao, " + 
			"          submetaRtSocial.vrpl_submeta_fk as sub_id" + 
			" FROM siconv.med_contrato_resp_tecnico_social as rtsocial  " + 
			" JOIN med_responsavel_tecnico rt ON rtsocial.med_responsavel_tecnico_fk = rt.id  " + 
			" JOIN med_contrato_rt_social_submeta submetaRtSocial ON rtsocial.id = submetaRtSocial.med_contrato_resp_tecnico_social_fk " +
			" WHERE rt.id = :idRt and " + 
			"          rtsocial.med_contrato_fk = :idContratoMedicao ")
	@RegisterFieldMapper(value = ContratoResponsavelTecnicoSocialDTO.class, prefix = "rtsocial")
	@RegisterFieldMapper(value = ContratoResponsavelTecnicoSocialDTO.Orgao.class, prefix = "orgao")
	@RegisterFieldMapper(value = ResponsavelTecnicoDTO.class, prefix = "rt")
	@RegisterFieldMapper(value = SubmetaVrplDTO.class, prefix = "sub")
	@UseRowReducer(ContratoResponsavelTecnicoSocialReducer.class)
	@UseStringTemplateEngine
	Optional<ContratoResponsavelTecnicoSocialDTO> consultarContratoResponsavelTecnicoSocialPorIdRtAtivoNoContrato(@Bind("idRt") Long idRt, @Bind("idContratoMedicao") Long idContratoMedicao);
	
	
	@SqlUpdate("delete from siconv.med_contrato_resp_tecnico_social where id = :idResponsavelContrato")
	void excluir(@Bind("idResponsavelContrato") Long idResponsavelContrato);
	
	@SqlUpdate("delete from siconv.med_contrato_rt_social_submeta where med_contrato_resp_tecnico_social_fk = :idResponsavelContrato")
	void excluirSubmetaPorIdRTContratoSocial(@Bind("idResponsavelContrato") Long idResponsavelContrato);

	@SqlQuery(" SELECT COUNT(rtsocial.id) > 0" +
	        " FROM siconv.med_contrato_resp_tecnico_social as rtsocial  " + 
			" JOIN med_responsavel_tecnico rt ON rtsocial.med_responsavel_tecnico_fk = rt.id  " + 
			" JOIN med_contrato as contrato ON rtsocial.med_contrato_fk = contrato.id " + 
			" WHERE contrato.contrato_fk = :idContrato AND " +
            " rtsocial.dt_inativacao IS NULL AND " +
			" rtsocial.in_tipo = :tipo  ")
	boolean existeRTSocialAtivo(@Bind("idContrato") Long idContrato, @Bind("tipo") String tipo);
	
    
    @SqlQuery("SELECT count(med_responsavel_tecnico_fk) > 0 " 
    		+ "FROM siconv.med_contrato_resp_tecnico_social "  
    		+ "WHERE med_contrato_fk = :idContrato ")    
    boolean existeRespTecnicoSocialContrato(@Bind("idContrato") Long idContrato);
	
}
