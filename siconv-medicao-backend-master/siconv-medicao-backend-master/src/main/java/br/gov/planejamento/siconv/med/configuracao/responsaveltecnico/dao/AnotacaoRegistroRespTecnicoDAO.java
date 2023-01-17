package br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.dao;

import java.util.List;
import java.util.Optional;

import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.statement.UseRowReducer;

import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.database.AnotacaoRegistroRespTecnicoBD;
import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.dto.AnotacaoRegistroRespTecnicoDTO;
import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.dto.ContratoResponsavelTecnicoDTO;
import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.dto.ResponsavelTecnicoDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.SubmetaVrplDTO;

public interface AnotacaoRegistroRespTecnicoDAO {

	
	@SqlQuery(" select arrt.id as arrt_id,   " + 
			"          arrt.nr_art_rrt as arrt_numero,  " + 
			"          arrt.dt_emissao as arrt_data_emissao,  " + 
			"          arrt.in_tipo as arrt_in_tipo,  " + 
			"          arrt.dt_inativacao as arrt_data_inativacao,  " + 
			"          arrt.nm_arquivo as arrt_nm_arquivo,  " + 
			"          arrt.co_ceph as arrt_co_ceph,  " + 
			"          arrt.med_contrato_resp_tecnico_fk as arrt_contratoResponsavelTecnicoFk,  " + 
	        "          arrt.versao as arrt_versao, " +
			"          rt.id as rt_id," +
			"          rt.nr_cpf as rt_nr_cpf," + 
			"          arrt.med_contrato_resp_tecnico_fk as crt_id, " +
			"          crt.in_tipo as crt_in_tipo, " +
			"          submetaRt.vrpl_submeta_fk as subm_id " + 
			"  from med_anotacao_registro_rt arrt  " + 
			"          join med_contrato_resp_tecnico crt on (arrt.med_contrato_resp_tecnico_fk = crt.id)   " + 
			"          join med_contrato con on (crt.med_contrato_fk = con.id)" + 
			"          join med_registro_profissional rp on (crt.med_registro_profissional_fk = rp.id)   " + 
			"          join med_responsavel_tecnico rt on (rp.med_responsavel_tecnico_fk = rt.id)  " + 
			"          join med_anotacao_registro_rt_submeta submetaRt on (arrt.id = submetaRt.med_anotacao_registro_rt_fk) " +
			"  where crt.med_contrato_fk = Cast(:idContratoResponsavel as int8) "
			+ "ORDER BY arrt.dt_emissao DESC")
	@RegisterFieldMapper(value = AnotacaoRegistroRespTecnicoDTO.class, prefix = "arrt")
	@RegisterFieldMapper(value = ContratoResponsavelTecnicoDTO.class, prefix = "crt")
	@RegisterFieldMapper(value = ResponsavelTecnicoDTO.class, prefix = "rt")
	@RegisterFieldMapper(value = SubmetaVrplDTO.class, prefix = "subm")
	@UseRowReducer(AnotacaoRegistroRespTecnicoReducer.class)
	List<AnotacaoRegistroRespTecnicoDTO> listarAnotacaoRegistroRT(@Bind("idContratoResponsavel") Long idContradoResponsavel);

	@SqlUpdate("INSERT INTO siconv.med_anotacao_registro_rt "
			+ "(nr_art_rrt, dt_emissao, in_tipo, nm_arquivo, co_ceph, med_contrato_resp_tecnico_fk, versao, adt_login, adt_data_hora, adt_operacao)"
			+ " VALUES (:numero, :dataEmissao, :tipo, :nmArquivo, :coCeph, :idMedContratoRespTec, 1, current_setting('med.cpf_usuario'), LOCALTIMESTAMP, 'INSERT')")
	@RegisterFieldMapper(AnotacaoRegistroRespTecnicoBD.class)
	@GetGeneratedKeys
	Long inserirAnotacao(@BindBean AnotacaoRegistroRespTecnicoBD anotacao);

	@SqlUpdate(" UPDATE siconv.med_anotacao_registro_rt"
             + " SET nr_art_rrt = :numero,"
             + "     dt_emissao = :dataEmissao,"
             + "     in_tipo = :tipo,"
             + "     nm_arquivo = :nmArquivo,"
             + "     co_ceph = :coCeph,"
             + "     med_contrato_resp_tecnico_fk = :idMedContratoRespTec,"
             + "     versao = :versao + 1,"
             + "     adt_login = current_setting('med.cpf_usuario'),"
             + "     adt_data_hora = LOCALTIMESTAMP,"
             + "     adt_operacao = 'UPDATE'"
             + " WHERE id = :id")
	boolean alterar(@BindBean AnotacaoRegistroRespTecnicoBD anotacao);

    @SqlUpdate("DELETE FROM siconv.med_anotacao_registro_rt WHERE id = :id")
    boolean deletar(@Bind("id") Long id);

    @SqlUpdate("UPDATE siconv.med_anotacao_registro_rt SET dt_inativacao = LOCALTIMESTAMP, versao = :versao + 1, adt_login = current_setting('med.cpf_usuario'),"
            + " adt_data_hora = LOCALTIMESTAMP, adt_operacao = 'UPDATE' WHERE id = :id")
    boolean inativar(@BindBean AnotacaoRegistroRespTecnicoBD anotacao);

    @SqlQuery(" SELECT arrt.id as arrt_id,"
            + "        arrt.nr_art_rrt as arrt_numero,"
            + "        arrt.dt_emissao as arrt_data_emissao,"
            + "        arrt.in_tipo as arrt_in_tipo,"
            + "        arrt.dt_inativacao as arrt_data_inativacao,"
            + "        arrt.nm_arquivo as arrt_nm_arquivo,"
            + "        arrt.co_ceph as arrt_co_ceph,"
            + "        arrt.versao as arrt_versao,"
            + "        crt.id as crt_id,"
            + "        rt.id as rt_id,"
            + "        rt.nr_cpf as rt_nr_cpf,"
            + "        submetaRt.vrpl_submeta_fk as subm_id"
            + " FROM med_anotacao_registro_rt arrt"
            + "   JOIN med_contrato_resp_tecnico crt ON arrt.med_contrato_resp_tecnico_fk = crt.id"
            + "   JOIN med_registro_profissional rp ON crt.med_registro_profissional_fk = rp.id"
            + "   JOIN med_responsavel_tecnico rt ON rp.med_responsavel_tecnico_fk = rt.id"
            + "   JOIN med_anotacao_registro_rt_submeta submetaRt ON arrt.id = submetaRt.med_anotacao_registro_rt_fk"
            + " WHERE arrt.id = :id")
    @RegisterFieldMapper(value = AnotacaoRegistroRespTecnicoDTO.class, prefix = "arrt")
    @RegisterFieldMapper(value = ResponsavelTecnicoDTO.class, prefix = "rt")
    @RegisterFieldMapper(value = SubmetaVrplDTO.class, prefix = "subm")
    @UseRowReducer(AnotacaoRegistroRespTecnicoReducer.class)
    Optional<AnotacaoRegistroRespTecnicoDTO> consultarAnotacaoDTO(@Bind("id") Long id);
    

    @SqlQuery(" SELECT"
            + "     COUNT(sub.id) > 0"
            + " FROM"
            + "     siconv.med_submeta_medicao sub"
            + "     JOIN siconv.med_medicao med ON med.id = sub.medicao_fk"
            + "     JOIN siconv.med_contrato con ON con.id = med.med_contrato_fk"
            + "     JOIN siconv.med_contrato_resp_tecnico crt ON crt.med_contrato_fk = con.id"
            + "     JOIN siconv.med_registro_profissional rp ON rp.id = crt.med_registro_profissional_fk"
            + "     JOIN siconv.med_responsavel_tecnico rt ON rt.id = rp.med_responsavel_tecnico_fk"
            + "     JOIN siconv.med_anotacao_registro_rt art ON art.med_contrato_resp_tecnico_fk = crt.id"
            + "     JOIN siconv.med_anotacao_registro_rt_submeta artsub ON artsub.med_anotacao_registro_rt_fk = art.id"
            + " WHERE"
            + "     sub.vrpl_submeta_fk = artsub.vrpl_submeta_fk AND"
            + "     (sub.nr_cpf_resp_empresa = rt.nr_cpf OR sub.nr_cpf_resp_convenente = rt.nr_cpf) AND"
            + "     art.id = :idAnotacao")
    boolean existeSubmetaAssinadaPeloResponsavelAnotacao(@Bind("idAnotacao") Long idAnotacao);
    
    @SqlQuery(" SELECT COUNT(art.id) > 0 "
            + " FROM siconv.med_contrato con "
            + "     JOIN siconv.med_contrato_resp_tecnico crt ON crt.med_contrato_fk = con.id "
            + "     JOIN siconv.med_registro_profissional rp ON rp.id = crt.med_registro_profissional_fk "
            + "     JOIN siconv.med_responsavel_tecnico rt ON rt.id = rp.med_responsavel_tecnico_fk "
            + "     JOIN siconv.med_anotacao_registro_rt art ON art.med_contrato_resp_tecnico_fk = crt.id "
            + " WHERE con.contrato_fk = :idContrato AND "
            + "     art.dt_inativacao IS NULL AND "
            + "     art.in_tipo = :tipo ")
    boolean existeAnotacaoComRTAtiva(@Bind("idContrato") Long idContrato, @Bind("tipo") String tipo);
}
