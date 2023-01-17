package br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.dao;

import java.util.List;

import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.statement.UseRowReducer;

import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.database.ResponsavelTecnicoBD;
import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.dto.ContratoResponsavelTecnicoDTO;
import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.dto.RegistroProfissionalDTO;
import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.dto.ResponsavelTecnicoDTO;
import br.gov.planejamento.siconv.med.contrato.entity.ContratoBD;

public interface ResponsavelTecnicoDAO {

	@SqlQuery("SELECT respTec.id AS rt_id,"
			+ " respTec.nr_cpf AS rt_nr_cpf,"
			+ " respTec.telefone AS rt_telefone,"
			+ " respTec.versao AS rt_versao, "
			+ " regProf.id AS rp_id,"
			+ " regProf.atividade AS rp_atividade,"
			+ " regProf.crea_cau AS rp_crea_cau,"
			+ " regProf.uf AS rp_uf,"
			+ " regProf.versao AS rp_versao, "
			+ " contRespTec.id AS crt_id,"
			+ " medContrato.contrato_fk AS crt_contrato_fk,"
			+ " contRespTec.dt_inclusao AS crt_dt_inclusao,"
			+ " contRespTec.in_tipo AS crt_in_tipo, "
			+ " contRespTec.versao AS crt_versao, "
            + " ("
			+ "  SELECT"
		    + "     COUNT(art) > 0" 
		    + "  FROM"
		    + "     siconv.med_anotacao_registro_rt art"
		    + "  WHERE"
		    + "     art.med_contrato_resp_tecnico_fk = contRespTec.id"
		    + " ) AS crt_possui_ART,"
            + " ("
			+ "  SELECT"
		    + "     COUNT(art) > 0" 
		    + "  FROM"
		    + "     siconv.med_anotacao_registro_rt art"
		    + "  WHERE"
		    + "     art.med_contrato_resp_tecnico_fk = contRespTec.id and "
		    + "     art.dt_inativacao is null "
		    + " ) AS crt_possui_ART_ativa,"
            + " ("
		    + "  SELECT"
            + "     COUNT(sub) > 0"
            + "  FROM"
            + "     siconv.med_submeta_medicao sub,"
            + "     siconv.med_medicao med"
            + "  WHERE"
            + "     sub.medicao_fk = med.id"
            + "     and med.med_contrato_fk = medContrato.id"
            + "     and (sub.nr_cpf_resp_empresa = respTec.nr_cpf"
            + "          OR sub.nr_cpf_resp_convenente = respTec.nr_cpf)"
            + " ) AS crt_possui_submeta_assinada"
			+ " FROM  med_contrato medContrato, med_contrato_resp_tecnico contRespTec,"
			+ " med_registro_profissional regProf, med_responsavel_tecnico respTec"
			+ " WHERE medContrato.id = contRespTec.med_contrato_fk"
			+ " AND contRespTec.med_registro_profissional_fk = regProf.id"
			+ " AND regProf.med_responsavel_tecnico_fk = respTec.id"
			+ " AND medContrato.contrato_fk = :idMedContrato"
			+ " ORDER BY contRespTec.dt_inclusao DESC ")
	@RegisterFieldMapper(value=ResponsavelTecnicoDTO.class, prefix="rt")
	@RegisterFieldMapper(value=RegistroProfissionalDTO.class, prefix="rp")
	@RegisterFieldMapper(value=ContratoResponsavelTecnicoDTO.class, prefix="crt")
	@UseRowReducer(ResponsavelTecnicoReducer.class)
	List<ResponsavelTecnicoDTO> listarResponsavelTecnicoPorContrato(@Bind("idMedContrato") Long idMedContrato);

	@SqlUpdate("INSERT INTO siconv.med_responsavel_tecnico (nr_cpf, telefone, versao, adt_login, adt_data_hora, adt_operacao) VALUES ( :cpf, :telefone, 1, current_setting('med.cpf_usuario'), LOCALTIMESTAMP, 'INSERT')")
    @RegisterFieldMapper(ResponsavelTecnicoBD.class)
	@GetGeneratedKeys
	ResponsavelTecnicoBD inserir(@BindBean ResponsavelTecnicoBD responsavelTecnicoBD);

	@SqlUpdate("UPDATE med_responsavel_tecnico "
			+ "SET telefone =:telefone, versao = :versao + 1, adt_login = current_setting('med.cpf_usuario'), " 
			+ "    adt_data_hora = LOCALTIMESTAMP, adt_operacao = 'UPDATE' "
			+ "where id = :id ")
    @RegisterFieldMapper(ResponsavelTecnicoBD.class)
	@GetGeneratedKeys
	ResponsavelTecnicoBD alterar(@BindBean ResponsavelTecnicoBD responsavelTecnicoBD);

	@SqlQuery("SELECT respTec.id AS rt_id,   " + 
			"         respTec.nr_cpf AS rt_nr_cpf, " +
			"         respTec.telefone AS rt_telefone, " + 
			"         respTec.versao AS rt_versao, " +
			"         regProf.id AS rp_id, " + 
			"         regProf.atividade AS rp_atividade, " + 
			"         regProf.crea_cau AS rp_crea_cau, " + 
			"         regProf.uf AS rp_uf, " + 
			"         regProf.versao AS rp_versao, " +	
			"         medContrato.id AS con_id, " +
			"         contRespTec.id AS crt_id, " + 
			"         contRespTec.med_contrato_fk AS crt_contrato, " + 
			"         contRespTec.dt_inclusao AS crt_dt_inclusao, " + 
			"         contRespTec.in_tipo AS crt_in_tipo, " +
			"         contRespTec.versao AS crt_versao " +
			" FROM  med_responsavel_tecnico respTec " + 
			"      join med_registro_profissional regProf on (regProf.med_responsavel_tecnico_fk = respTec.id) " + 
			"      left join med_contrato_resp_tecnico contRespTec on (contRespTec.med_registro_profissional_fk = regProf.id) " + 
			"      left join med_contrato medContrato on (medContrato.id = contRespTec.med_contrato_fk) " + 
			" WHERE respTec.id = :id ")
	@RegisterFieldMapper(value=ResponsavelTecnicoDTO.class, prefix="rt")
	@RegisterFieldMapper(value=RegistroProfissionalDTO.class, prefix="rp")
	@RegisterFieldMapper(value=ContratoResponsavelTecnicoDTO.class, prefix="crt")
	@RegisterBeanMapper(value=ContratoBD.class, prefix="con")
	@UseRowReducer(ResponsavelTecnicoReducer.class)
	ResponsavelTecnicoDTO consultarResponsavelTecnicoPorId(@Bind("id")  Long id);

	@SqlQuery("SELECT respTec.id AS rt_id,"
			+ " respTec.nr_cpf AS rt_nr_cpf,"
			+ " respTec.telefone AS rt_telefone,"
			+ " respTec.versao AS rt_versao,"
			+ " regProf.id AS rp_id,"
			+ " regProf.atividade AS rp_atividade,"
			+ " regProf.crea_cau AS rp_crea_cau,"
			+ " regProf.uf AS rp_uf,"
			+ " regProf.versao AS rp_versao, " 
			+ " contRespTec.id AS crt_id,"
			+ " contRespTec.med_contrato_fk AS crt_contrato,"
			+ " contRespTec.dt_inclusao AS crt_dt_inclusao,"
			+ " contRespTec.in_tipo AS crt_in_tipo,"
			+ " contRespTec.versao AS crt_versao, "
			+ " medContrato.contrato_fk AS crt_contrato_fk"
			+ " FROM  med_contrato medContrato, med_contrato_resp_tecnico contRespTec,"
			+ " med_registro_profissional regProf, med_responsavel_tecnico respTec"
			+ " WHERE medContrato.id = contRespTec.med_contrato_fk"
			+ " AND contRespTec.med_registro_profissional_fk = regProf.id"
			+ " AND regProf.med_responsavel_tecnico_fk = respTec.id"
			+ " AND medContrato.contrato_fk = :idMedContrato"
			+ " AND contRespTec.in_tipo = :tipo")
	@RegisterFieldMapper(value=ResponsavelTecnicoDTO.class, prefix="rt")
	@RegisterFieldMapper(value=RegistroProfissionalDTO.class, prefix="rp")
	@RegisterFieldMapper(value=ContratoResponsavelTecnicoDTO.class, prefix="crt")
	@UseRowReducer(ResponsavelTecnicoReducer.class)
	List<ResponsavelTecnicoDTO> listarResponsavelTecnicoPorContratoTipo(@Bind("idMedContrato") Long idMedContrato, @Bind("tipo") String tipo);
	
	@SqlQuery("SELECT respTec.id AS rt_id, " + 
			"         respTec.nr_cpf AS rt_nr_cpf, " +
			"         respTec.telefone AS rt_telefone, " + 
			"         respTec.versao AS rt_versao, " + 
			"         regProf.id AS rp_id, " + 
			"         regProf.atividade AS rp_atividade, " + 
			"         regProf.crea_cau AS rp_crea_cau, " + 
			"         regProf.uf AS rp_uf, " + 
			"         regProf.versao AS rp_versao, " +
			"         medContrato.id AS con_id, " +
			"         medContrato.contrato_fk AS crt_contrato_fk, " +
			"         contRespTec.id AS crt_id, " +
			"         contRespTec.med_contrato_fk AS crt_contrato, " +
			"         contRespTec.dt_inclusao AS crt_dt_inclusao, " +
			"         contRespTec.in_tipo AS crt_in_tipo, " +
			"         contRespTec.versao AS crt_versao, " +
			"         ( " +
			"             SELECT " +
			"                   COUNT(sub) > 0 " +
			"                   FROM siconv.med_submeta_medicao sub, " +
			"                        siconv.med_medicao med " +
			"                   WHERE " +
			"                       sub.medicao_fk = med.id " +
			"                       and med.med_contrato_fk = medContrato.id " + 
			"                       and (sub.nr_cpf_resp_empresa = respTec.nr_cpf " +
            "                            OR sub.nr_cpf_resp_convenente = respTec.nr_cpf) " +
            "         ) AS crt_possui_submeta_assinada " +
            " FROM  med_responsavel_tecnico respTec " + 
            " join med_registro_profissional regProf on (regProf.med_responsavel_tecnico_fk = respTec.id) " + 
            "         left join med_contrato_resp_tecnico contRespTec on (contRespTec.med_registro_profissional_fk = regProf.id) " +
            "         left join med_contrato medContrato on (medContrato.id = contRespTec.med_contrato_fk) " +
			" Where respTec.nr_cpf = :numeroCPF " +
			" order by rp_uf,rp_crea_cau ")
	@RegisterFieldMapper(value=ResponsavelTecnicoDTO.class, prefix="rt")
	@RegisterFieldMapper(value=RegistroProfissionalDTO.class, prefix="rp")
	@RegisterFieldMapper(value=ContratoResponsavelTecnicoDTO.class, prefix="crt")
	@UseRowReducer(ResponsavelTecnicoReducer.class)
	ResponsavelTecnicoDTO consultarRegistrosRespTecnico(@Bind("numeroCPF") String numeroCPF);
	
	@SqlQuery(" SELECT respTec.id AS rt_id, " + 
			"   respTec.nr_cpf AS rt_nr_cpf, " +
			"   respTec.telefone AS rt_telefone, " + 
			"   respTec.versao AS rt_versao " +
			" FROM  med_responsavel_tecnico respTec " + 
			" WHERE respTec.nr_cpf = :numeroCPF ")
	@RegisterFieldMapper(value=ResponsavelTecnicoDTO.class, prefix="rt")
	ResponsavelTecnicoDTO consultarResponsavelTecnicoPorCpf(@Bind("numeroCPF") String numeroCPF);
	
	
	@SqlQuery (" select respTec.id as rt_id, "
			+ "         respTec.nr_cpf as rt_nr_cpf, "
			+ "         respTec.telefone as rt_telefone, "
			+ "         crt.med_contrato_fk as rt_med_contrato_fk "
			+ " from med_responsavel_tecnico as respTec,"
			+ "     med_registro_profissional as rp, "
			+ "     med_contrato_resp_tecnico as crt "
			+ " where respTec.id = rp.med_responsavel_tecnico_fk and "
			+ "       rp.id = crt.med_registro_profissional_fk and "
			+ "       crt.id = :contratoRespTecnicoId" )
	@RegisterFieldMapper(value=ResponsavelTecnicoDTO.class, prefix="rt")
	ResponsavelTecnicoDTO consultarResponsavelTecnicoPorContratoRespTecnicoId(@Bind("contratoRespTecnicoId") Long contratoRespTecnicoId);	
}