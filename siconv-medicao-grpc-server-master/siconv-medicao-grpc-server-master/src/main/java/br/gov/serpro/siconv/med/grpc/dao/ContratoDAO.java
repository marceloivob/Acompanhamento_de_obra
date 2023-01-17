package br.gov.serpro.siconv.med.grpc.dao;

import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

import br.gov.serpro.siconv.med.grpc.bean.ContratoBD;


public interface ContratoDAO {

		@SqlQuery(" select id from med_contrato contr"+
		          " where contr.contrato_fk = Cast(:contratoFk as int8) ")
		@RegisterFieldMapper(ContratoBD.class)
		ContratoBD consultarContratoPorContratoFK(@Bind("contratoFk") Long idContrato);
	
}
