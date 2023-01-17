package br.gov.planejamento.siconv.med.infra.rest.mapper.exception;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.core.JsonParseException;

@Provider
public class JsonParseExceptionMapper implements ExceptionMapper<JsonParseException> {

    @Inject
    private JacksonExceptionHandler handler;

    @Override
    public Response toResponse(JsonParseException exception) {
        return handler.toResponse("Erro durante o parsing do input JSON", exception);
    }
}
