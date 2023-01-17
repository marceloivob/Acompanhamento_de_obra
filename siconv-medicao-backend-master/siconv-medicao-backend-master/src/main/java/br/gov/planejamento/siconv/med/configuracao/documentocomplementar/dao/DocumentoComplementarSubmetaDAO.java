package br.gov.planejamento.siconv.med.configuracao.documentocomplementar.dao;

import java.util.List;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlBatch;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import br.gov.planejamento.siconv.med.configuracao.documentocomplementar.entity.database.DocumentoComplementarSubmetaBD;

public interface DocumentoComplementarSubmetaDAO {

	
	@SqlBatch("INSERT INTO siconv.med_doc_complementar_submeta (vrpl_submeta_fk, med_doc_complementar_fk, adt_login, adt_data_hora, adt_operacao) "
			+ " VALUES (:vrplSubmetaFk, :documentoComplementarFk, current_setting('med.cpf_usuario'), LOCALTIMESTAMP, 'INSERT')")
	void inserirDocumentoSubmeta(@BindBean List<DocumentoComplementarSubmetaBD> listaDocumentoSubmeta);

	@SqlUpdate("DELETE FROM siconv.med_doc_complementar_submeta WHERE med_doc_complementar_fk = :idDocumentoComplementar")
    boolean deletarPorIdDocumentoComplementar(@Bind("idDocumentoComplementar") Long idDocumentoComplementar);
	
	@SqlBatch("DELETE FROM siconv.med_doc_complementar_submeta WHERE vrpl_submeta_fk = :vrplSubmetaFk AND med_doc_complementar_fk = :documentoComplementarFk")
	void deletar(@BindBean List<DocumentoComplementarSubmetaBD> listaDocumento);
	
}
