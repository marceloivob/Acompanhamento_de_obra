package br.gov.planejamento.siconv.med.infra.rest.mapper.exception;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.Priorities;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;

@Provider
@Priority(Priorities.ENTITY_CODER)
public class UnrecognizedPropertyExceptionHandler implements ExceptionMapper<UnrecognizedPropertyException> {

    @Inject
    private JacksonExceptionHandler handler;

    @Override
    public Response toResponse(UnrecognizedPropertyException exception) {
        return handler.toResponse("Propriedade desconhecida para a entrada JSON informada", exception);
    }
}