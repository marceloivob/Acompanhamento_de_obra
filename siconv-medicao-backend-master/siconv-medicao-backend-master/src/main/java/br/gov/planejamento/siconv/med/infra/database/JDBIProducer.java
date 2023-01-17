package br.gov.planejamento.siconv.med.infra.database;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.sql.DataSource;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.HandlerDecorators;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

@ApplicationScoped
public class JDBIProducer {

    @Inject
    private DataSource dataSource;

    @Inject
    private CustomSqlLogger sqlLogger;

    private Jdbi jdbi;

    @Inject
    private SiconvTransactionHandler siconvTransactionHandler;

    @Inject
    private SiconvHandlerDecorator siconvHandlerDecorator;

    @PostConstruct
    protected void init() {
        jdbi = Jdbi.create(dataSource);
        jdbi.installPlugin(new SqlObjectPlugin());
        jdbi.setSqlLogger(sqlLogger);
        jdbi.setTransactionHandler(siconvTransactionHandler);
        jdbi.getConfig(HandlerDecorators.class).register(siconvHandlerDecorator);
    }

    @Produces
    public Jdbi produzir() {
        return jdbi;
    }
}
