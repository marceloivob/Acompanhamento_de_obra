package br.gov.serpro.siconv.med.grpc.dao;

import java.util.List;

import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

import br.gov.serpro.siconv.med.grpc.bean.MedicaoBD;

public interface MedicaoDAO {

	@SqlQuery(" SELECT medicao.id " +
	        " FROM  siconv.med_contrato, " +
	        "       siconv.med_medicao medicao "+
	        " WHERE med_contrato.id = medicao.med_contrato_fk " +
	        "   AND med_contrato.contrato_fk = Cast(:contratoFk as int8) ")
	@RegisterFieldMapper(MedicaoBD.class)
	List<MedicaoBD> listarMedicoes(@Bind("contratoFk") Long contratoFk);
	
}
