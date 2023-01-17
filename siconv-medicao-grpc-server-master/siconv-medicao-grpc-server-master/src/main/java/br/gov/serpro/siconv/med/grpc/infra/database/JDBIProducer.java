package br.gov.serpro.siconv.med.grpc.infra.database;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.sql.DataSource;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

@ApplicationScoped
public class JDBIProducer {

	@Inject
	private DataSource dataSource;

	private Jdbi jdbi;

	@PostConstruct
	protected void init() {
		jdbi = Jdbi.create(dataSource);
		jdbi.installPlugin(new SqlObjectPlugin());
	}

	@Produces
	public Jdbi produzir() {
		return jdbi;
	}
}
