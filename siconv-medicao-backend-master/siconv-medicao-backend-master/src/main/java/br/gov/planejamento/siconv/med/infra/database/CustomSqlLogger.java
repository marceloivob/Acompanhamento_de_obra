package br.gov.planejamento.siconv.med.infra.database;

import java.time.temporal.ChronoUnit;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jdbi.v3.core.statement.SqlLogger;
import org.jdbi.v3.core.statement.StatementContext;
import org.slf4j.Logger;

@Dependent
public class CustomSqlLogger implements SqlLogger {

    @Inject
    private Logger logger;

    @Inject
    private SqlFormatter formatter;

    @Override
    public void logAfterExecution(StatementContext context) {

        if (logger.isDebugEnabled()) {

            String sql = formatter.format(context.getRenderedSql());

            logger.debug(sql);

            logger.debug("Parâmetros utilizados: {}", context.getBinding());
            logger.debug("Tempo de execução da consulta: {} ms", context.getElapsedTime(ChronoUnit.MILLIS));
        }
    }

}
