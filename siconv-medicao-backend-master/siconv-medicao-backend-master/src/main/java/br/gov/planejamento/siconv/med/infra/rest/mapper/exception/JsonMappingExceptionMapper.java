package br.gov.planejamento.siconv.med.infra.rest.mapper.exception;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.JsonMappingException;

@Provider
public class JsonMappingExceptionMapper implements ExceptionMapper<JsonMappingException> {

    @Inject
    private JacksonExceptionHandler handler;

    @Override
    public Response toResponse(JsonMappingException exception) {
        return handler.toResponse("Erro durante o mapeamento da entrada JSON", exception);
    }
}
