package br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.dao;

import java.util.Optional;

import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.database.ContratoResponsavelTecnicoBD;

public interface ContratoResponsavelTecnicoDAO {

	@SqlUpdate("INSERT INTO siconv.med_contrato_resp_tecnico (med_contrato_fk, med_registro_profissional_fk, dt_inclusao, in_tipo, versao, adt_login, adt_data_hora, adt_operacao) "
  			 + "VALUES (:contrato, :registro, LOCALTIMESTAMP, :tipo, 1, current_setting('med.cpf_usuario'), LOCALTIMESTAMP, 'INSERT')")
	@RegisterFieldMapper(ContratoResponsavelTecnicoBD.class)
	@GetGeneratedKeys
	ContratoResponsavelTecnicoBD inserir(@BindBean ContratoResponsavelTecnicoBD contratoResponsavelTecnicoBD);

	@SqlUpdate("update siconv.med_contrato_resp_tecnico "
			+ " set med_registro_profissional_fk = :registro, "
			+ " dt_inclusao = :dataInclusao, versao = :versao + 1, adt_login = current_setting('med.cpf_usuario'), adt_data_hora = LOCALTIMESTAMP, adt_operacao = 'UPDATE' "
			+ " where id = :id")
	@RegisterFieldMapper(ContratoResponsavelTecnicoBD.class)
	@GetGeneratedKeys
	ContratoResponsavelTecnicoBD alterar(@BindBean ContratoResponsavelTecnicoBD contratoResponsavelTecnicoBD);

	@SqlUpdate("delete from siconv.med_contrato_resp_tecnico where id = :idMedContratoRespTec")
	void excluir(@Bind("idMedContratoRespTec") Long idMedContratoRespTec);

	@SqlQuery(" Select cont.contrato_fk " +
			  " From med_contrato_resp_tecnico md, " +
			  "      med_contrato cont " +
	          " Where md.id = :idMedContratoRespTec " +
			  "   and md.med_contrato_fk = cont.id ")
	Long  validarMedContratoPorContratoRespTecnico(@Bind("idMedContratoRespTec")  Long idMedContratoRespTec);
	
	@SqlQuery(" SELECT count(respTec.id) > 0 FROM siconv.med_responsavel_tecnico respTec,"
			+ " siconv.med_registro_profissional regProf,"
			+ " siconv.med_contrato_resp_tecnico contRespTec,"
			+ " siconv.med_contrato cont, siconv.med_medicao medicao,"
			+ " siconv.med_submeta_medicao subMedicao"
			+ " WHERE respTec.id = regProf.med_responsavel_tecnico_fk"
			+ " AND regProf.id = contRespTec.med_registro_profissional_fk"
			+ " AND contRespTec.med_contrato_fk = cont.id AND cont.id = medicao.med_contrato_fk"
			+ " AND medicao.id = subMedicao.medicao_fk"
			+ " AND contRespTec.id = :idMedContratoRespTec"
			+ " AND (respTec.nr_cpf = subMedicao.nr_cpf_resp_empresa"
			+ "      OR respTec.nr_cpf = subMedicao.nr_cpf_resp_convenente)")
	boolean isContRespTecnicoAssinado(@Bind("idMedContratoRespTec") Long idMedContratoRespTec);

	@SqlQuery("SELECT count(anotacao.id) > 0 FROM siconv.med_anotacao_registro_rt anotacao"
			+ " WHERE anotacao.med_contrato_resp_tecnico_fk = :idMedContratoRespTec")
	boolean isContRespTecnicoAnotado(@Bind("idMedContratoRespTec") Long idMedContratoRespTec);
	
    @SqlQuery("SELECT * FROM siconv.med_contrato_resp_tecnico WHERE id = :id")
    @RegisterFieldMapper(ContratoResponsavelTecnicoBD.class)
    Optional<ContratoResponsavelTecnicoBD> consultar(@Bind("id") Long id);
    
    @SqlQuery("SELECT count(med_registro_profissional_fk) > 0 " + 
    		  "FROM siconv.med_contrato_resp_tecnico " + 
    		  "WHERE med_contrato_fk = :idContrato ")    
    boolean existeRespTecnicoContrato(@Bind("idContrato") Long idContrato);


    
}
