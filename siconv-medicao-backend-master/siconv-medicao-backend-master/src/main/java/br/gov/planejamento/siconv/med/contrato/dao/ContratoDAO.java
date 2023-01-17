package br.gov.planejamento.siconv.med.contrato.dao;

import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import br.gov.planejamento.siconv.med.contrato.entity.ContratoBD;

public interface ContratoDAO {

		@SqlQuery(" select * from med_contrato contr"+
		          " where contr.contrato_fk = Cast(:idContratoSiconv as int8) ")
		@RegisterFieldMapper(ContratoBD.class)
		ContratoBD consultarContratoPorContratoFK(@Bind("idContratoSiconv") Long idContratoSiconv);

		@SqlQuery(" select * from med_contrato contr"+
		          " where contr.proposta_fk = Cast(:propostaFk as int8) ")
		@RegisterFieldMapper(ContratoBD.class)
		ContratoBD consultarContratoPorPropostaFK(@Bind("propostaFk") Long propostaFk);
		
		@SqlQuery(" Select * From med_contrato contrato "+
		          " where contrato.id = Cast(:idContratoMedicao as int8) ")
		@RegisterFieldMapper(ContratoBD.class)
		ContratoBD consultarContrato(@Bind("idContratoMedicao") Long idContratoMedicao);
		
		@SqlUpdate(" INSERT INTO med_contrato (dt_inicio_obra, contrato_fk, in_Social, " + 
				   " cnpj_fornecedor, proposta_fk, in_acompanhamento_eventos, adt_login, adt_data_hora, adt_operacao) " + 
				   " VALUES ( :dataInicioObra, :contratoFk, :inSocial, :cnpjFornecedor, " + 
				   " :propostaFk, :inAcompanhamentoEventos, current_setting('med.cpf_usuario'), LOCALTIMESTAMP, 'INSERT' )")
	    @RegisterFieldMapper(ContratoBD.class)		
		@GetGeneratedKeys
		ContratoBD inserir(@BindBean ContratoBD contratoBD);	
		
		@SqlQuery(" Select contrato.* from med_contrato contrato, med_medicao med "+
					"where contrato.id = med.med_contrato_fk  and"+
					"      med.id = Cast(:idMedicao as int8) ")
		@RegisterFieldMapper(ContratoBD.class)
		ContratoBD consultarContratoAssociadoMedicao(@Bind("idMedicao") Long idMedicao);

        @SqlQuery(" SELECT"
                + "    contrato.*"
                + " FROM med_contrato contrato"
                + "   JOIN med_medicao med ON med.med_contrato_fk = contrato.id"
                + "   JOIN med_observacao observacao ON observacao.medicao_fk = med.id"
                + " WHERE observacao.id = :idObservacao")
        @RegisterFieldMapper(ContratoBD.class)
        ContratoBD consultarContratoAssociadoObservacao(@Bind("idObservacao") Long idObservacao);

        @SqlQuery(" SELECT"
                + "    contrato.*"
                + " FROM med_contrato contrato"
                + "   JOIN med_contrato_resp_tecnico crt ON crt.med_contrato_fk = contrato.id"
                + "   JOIN med_anotacao_registro_rt anotacao ON anotacao.med_contrato_resp_tecnico_fk = crt.id"
                + " WHERE anotacao.id = :idAnotacao")
        @RegisterFieldMapper(ContratoBD.class)
        ContratoBD consultarContratoAssociadoAnotacao(@Bind("idAnotacao") Long idAnotacao);

        @SqlQuery(" SELECT"
                + "    contrato.*"
                + " FROM med_contrato contrato"
                + "   JOIN med_contrato_resp_tecnico crt ON crt.med_contrato_fk = contrato.id"
                + " WHERE crt.id = :idContratoRespTec")
        @RegisterFieldMapper(ContratoBD.class)
        ContratoBD consultarContratoAssociadoContratoRespTecnico(@Bind("idContratoRespTec") Long idContratoRespTec);

        @SqlQuery(" SELECT"
                + "    contrato.*"
                + " FROM med_contrato contrato"
                + "   JOIN med_contrato_resp_tecnico_social crt_social ON crt_social.med_contrato_fk = contrato.id"
                + " WHERE crt_social.id = :idContratoRespTecSocial")
        @RegisterFieldMapper(ContratoBD.class)
        ContratoBD consultarContratoAssociadoContratoRespTecnicoSocial(@Bind("idContratoRespTecSocial") Long idContratoRespTecSocial);

        @SqlQuery(" SELECT"
                + "    contrato.*"
                + " FROM med_contrato contrato"
                + "   JOIN med_doc_complementar doc ON doc.med_contrato_fk = contrato.id"
                + " WHERE doc.id = :idDocumentoComplementar")
        @RegisterFieldMapper(ContratoBD.class)
        ContratoBD consultarContratoAssociadoDocumentoComplementar(@Bind("idDocumentoComplementar") Long idDocumentoComplementar);
        
        @SqlQuery(" SELECT"
                + "    contrato.*"
                + " FROM med_contrato contrato"
                + "   JOIN med_paralisacao paralisacao ON paralisacao.med_contrato_fk = contrato.id"
                + " WHERE paralisacao.id = :idParalisacao")
        @RegisterFieldMapper(ContratoBD.class)
        ContratoBD consultarContratoAssociadoParalisacao(@Bind("idParalisacao") Long idParalisacao);

		@SqlUpdate("UPDATE med_contrato set dt_inicio_obra = :dataInicioObra, adt_login = current_setting('med.cpf_usuario'), adt_data_hora = LOCALTIMESTAMP, adt_operacao = 'UPDATE' "
				+ " where id = :id")
	    @RegisterFieldMapper(ContratoBD.class)		
		void alterar(@BindBean ContratoBD contratoBD);
		
		@SqlUpdate("delete from siconv.med_contrato where id = :idContrato")
		void excluir(@Bind("idContrato") Long idContrato);
}
