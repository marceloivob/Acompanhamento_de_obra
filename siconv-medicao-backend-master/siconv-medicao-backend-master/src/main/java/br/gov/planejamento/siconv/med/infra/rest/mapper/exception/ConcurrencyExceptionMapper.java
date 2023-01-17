package br.gov.planejamento.siconv.med.infra.rest.mapper.exception;

import java.sql.SQLException;

import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;

import org.jdbi.v3.core.statement.UnableToExecuteStatementException;
import org.slf4j.Logger;

import br.gov.planejamento.siconv.med.infra.exception.ConcurrencyException;
import br.gov.planejamento.siconv.med.infra.exception.MedicaoRestException;

@Provider
public class ConcurrencyExceptionMapper implements ExceptionMapper<UnableToExecuteStatementException> {

	/**
	 * Código do erro de concorrência gerado pela trigger de banco.
	 */
	private static final String SQLSTATE_CONCURRENCY_ERROR_CODE = "23501";

	@Inject
	private Logger logger;

	@Context
	private Providers providers;

	@Override
	public Response toResponse(UnableToExecuteStatementException e) {

		if (e.getCause() instanceof SQLException
				&& ((SQLException) e.getCause()).getSQLState().equals(SQLSTATE_CONCURRENCY_ERROR_CODE)) {

			logger.error("Erro de concorrência na atualização do registro", e);

			return providers.getExceptionMapper(MedicaoRestException.class).toResponse(new ConcurrencyException());

		} else {

			return providers.getExceptionMapper(Throwable.class).toResponse(e);
		}
	}
}
