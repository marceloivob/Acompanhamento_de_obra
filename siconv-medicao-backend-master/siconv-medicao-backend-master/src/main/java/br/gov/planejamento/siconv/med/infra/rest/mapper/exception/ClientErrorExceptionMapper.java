package br.gov.planejamento.siconv.med.infra.rest.mapper.exception;

import static br.gov.planejamento.siconv.med.infra.rest.response.ResponseHelper.error;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ClientErrorExceptionMapper implements ExceptionMapper<ClientErrorException> {

    @Override
    public Response toResponse(ClientErrorException exception) {
        return error(exception.getResponse().getStatus(), "Http exception", null);
    }
}
