package br.gov.planejamento.siconv.med.configuracao.documentocomplementar.dao;

import java.util.List;
import java.util.Optional;

import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.statement.UseRowReducer;

import br.gov.planejamento.siconv.med.configuracao.documentocomplementar.entity.database.DocumentoComplementarBD;
import br.gov.planejamento.siconv.med.configuracao.documentocomplementar.entity.dto.DocumentoComplementarDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.SubmetaVrplDTO;

public interface DocumentoComplementarDAO {

	@SqlQuery("select   dc.id                       as dc_id," + 
			"           dc.in_tipo_documento        as dc_in_tipo_documento," + 
			"           dc.in_tipo_manifesto        as dc_in_tipo_manifesto," + 
			"           dc.nm_orgao_emissor         as dc_nm_orgao_emissor," + 
			"           dc.dt_emissao               as dc_dt_emissao," + 
			"           dc.dt_validade              as dc_dt_validade," +
			"           dc.nm_arquivo               as dc_nm_arquivo," +	
			"           dc.co_ceph                  as dc_co_ceph," +
			"           dc.in_bloqueio              as dc_in_bloqueio," +
			"           dc.versao                   as dc_versao," +
			"           submetaDc.vrpl_submeta_fk   as subm_id," +
			"           crt.contrato_fk             as dc_contrato_fk," +
			"           dc.tx_descricao_outros      as dc_tx_descricao_outros," +
			"           dc.in_eq_lic_inst           as dc_in_eq_lic_inst" +	
			"  from     med_doc_complementar       dc" + 
			"           join med_contrato crt on (dc.med_contrato_fk = crt.id)" + 
			"           left join med_doc_complementar_submeta submetaDc on (dc.id = submetaDc.med_doc_complementar_fk)" +
			"  where crt.contrato_fk = Cast(:idContratoSiconv as int8) "
			+ " ORDER BY dc.in_tipo_documento, dc.dt_emissao DESC")
	@RegisterFieldMapper(value = DocumentoComplementarDTO.class,       prefix = "dc")
	@RegisterFieldMapper(value = SubmetaVrplDTO.class,                 prefix = "subm")
	@UseRowReducer(DocumentoComplementarReducer.class)
	List<DocumentoComplementarDTO> listarDocumentosComplementares(@Bind("idContratoSiconv") Long idContratoSiconv);
	
	@SqlUpdate("INSERT INTO siconv.med_doc_complementar "
			+ "(in_tipo_documento, in_tipo_manifesto, dt_emissao, dt_validade, nr_documento, tx_descricao, nm_orgao_emissor, nm_arquivo,  co_ceph, med_contrato_fk, versao, adt_login, adt_data_hora, adt_operacao, tx_descricao_outros, in_eq_lic_inst)"
			+ " VALUES (:tipoDocumento, :tipoManifestoAmbiental, :dtEmissao, :dtValidade, :nrDocumento, :txDescricao, :nmOrgaoEmissor , :nmArquivo, :coCeph, :medContratoFk, 1, current_setting('med.cpf_usuario'), LOCALTIMESTAMP, 'INSERT', :txDescricaoOutros, :eqLicencaInstalacao )")
	@RegisterFieldMapper(DocumentoComplementarBD.class)
	@GetGeneratedKeys
	Long inserirDocumento(@BindBean DocumentoComplementarBD documento);

	@SqlUpdate("delete from siconv.med_doc_complementar where id = :idDocumentoComplementar")
	void excluir(@Bind("idDocumentoComplementar") Long idDocumentoComplementar);
	
	@SqlQuery(" select   dc.id                     as dc_id,   " + 
			"            dc.in_tipo_documento      as dc_in_tipo_documento,  " + 
			"            dc.in_tipo_manifesto      as dc_in_tipo_manifesto,  " + 
			"            dc.nm_orgao_emissor       as dc_nm_orgao_emissor,  " + 
			"            dc.dt_emissao             as dc_dt_emissao,  " + 
			"            dc.dt_validade            as dc_dt_validade, " +
			"            dc.nr_documento           as dc_nr_documento, " +
			"            dc.tx_descricao           as dc_tx_descricao, " +
			"            dc.nm_arquivo             as dc_nm_arquivo, " +	
			"            dc.co_ceph                as dc_co_ceph, " +
			"            dc.in_bloqueio            as dc_in_bloqueio, " +
			"            dc.med_contrato_fk        as dc_med_contrato_fk, " +
			"            dc.versao                 as dc_versao, " +
			"            submetaDc.vrpl_submeta_fk as subm_id, " +
			"            crt.contrato_fk           as dc_contrato_fk," +
			"            dc.tx_descricao_outros    as dc_tx_descricao_outros," + 
			"            dc.in_eq_lic_inst         as dc_in_eq_lic_inst" +			
			"  from      med_doc_complementar      dc  " +
			"            join med_contrato crt on (dc.med_contrato_fk = crt.id)   " + 			
			"            left join med_doc_complementar_submeta submetaDc on (dc.id    = submetaDc.med_doc_complementar_fk) " +
			"  where dc.id = Cast(:idDocumentoComplementar as int8) ")
	@RegisterFieldMapper(value = DocumentoComplementarDTO.class, prefix = "dc")
	@RegisterFieldMapper(value = SubmetaVrplDTO.class, prefix = "subm")
	@UseRowReducer(DocumentoComplementarReducer.class)
	Optional<DocumentoComplementarDTO> consultarDocumentoComplementar(@Bind("idDocumentoComplementar") Long idDocumentoComplementar);

	@SqlUpdate(" UPDATE siconv.med_doc_complementar"
            + " SET in_tipo_documento = :tipoDocumento,"
            + "     in_tipo_manifesto = :tipoManifestoAmbiental,"
            + "     dt_emissao = :dtEmissao,"
            + "     dt_validade = :dtValidade,"
            + "     nr_documento = :nrDocumento,"
            + "     tx_descricao = :txDescricao,"
            + "     nm_orgao_emissor = :nmOrgaoEmissor,"
            + "     nm_arquivo = :nmArquivo,"
            + "     co_ceph = :coCeph,"
            + "     med_contrato_fk = :medContratoFk,"
            + "     versao = :versao + 1,"
            + "     adt_login = current_setting('med.cpf_usuario'),"
            + "     adt_data_hora = LOCALTIMESTAMP,"
            + "     adt_operacao = 'UPDATE',"
            + "     tx_descricao_outros = :txDescricaoOutros,"
            + "     in_eq_lic_inst = :eqLicencaInstalacao"
            + " WHERE id = :id")
	boolean alterar(@BindBean DocumentoComplementarBD documento);

	@SqlQuery (" select dc.* " +
				" from med_doc_complementar dc"+ 
				" where dc.id = (select max(dc_inner.id) from med_contrato crt_inner, med_doc_complementar dc_inner " +
				"                  where    dc_inner.med_contrato_fk = crt_inner.id and " +
				"                           crt_inner.contrato_fk = :idContrato and " +
				"                           dc_inner.in_tipo_documento = 'OSE') ")
	@RegisterFieldMapper(DocumentoComplementarBD.class)
	Optional<DocumentoComplementarBD> consultarDocumentoComplementarOSParaContrato(@Bind("idContrato") Long idContrato);

	@SqlQuery(" SELECT doc.id"
			+ " FROM siconv.med_doc_complementar doc"
			+ " WHERE doc.med_contrato_fk = :idContratoMedicao"
			+ "   AND doc.in_bloqueio = :bloqueado")
	List<Long> listarIdDocumentoComplementar(@Bind("idContratoMedicao") Long idContratoMedicao,
			@Bind("bloqueado") boolean bloqueado);

    @SqlQuery("SELECT  Count(id) > 0 " + 
    		  "FROM med_doc_complementar " + 
    		  "WHERE med_contrato_fk = :idContrato ")    
    boolean existeDocumentoComplementarContrato(@Bind("idContrato") Long idContrato);
    
    @SqlUpdate(" UPDATE siconv.med_doc_complementar"
			 + " SET in_bloqueio = :bloqueio,"
			 + "     versao = versao + 1,"
			 + "     adt_login = current_setting('med.cpf_usuario'),"
			 + "     adt_data_hora = LOCALTIMESTAMP,"
			 + "     adt_operacao = 'UPDATE'"
			 + " WHERE id = :idDocumentoComplementar")
	void setarBloqueioDocumentoComplementar(@Bind("idDocumentoComplementar") Long idDocumentoComplementar, @Bind("bloqueio") boolean bloqueio);
}
