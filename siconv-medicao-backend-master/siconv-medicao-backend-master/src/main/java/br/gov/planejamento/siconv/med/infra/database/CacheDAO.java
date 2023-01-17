package br.gov.planejamento.siconv.med.infra.database;

import org.jdbi.v3.sqlobject.customizer.Define;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.stringtemplate4.UseStringTemplateSqlLocator;

public interface CacheDAO {

	
	@SqlUpdate
	@UseStringTemplateSqlLocator
	void definirUsuarioLogado(@Define("cpf") String cpf);
	
	@SqlQuery
	@UseStringTemplateSqlLocator
	String consultarUsuarioLogado();

}
