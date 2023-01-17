package br.gov.planejamento.siconv.med.infra.rest.mapper.exception;

import static br.gov.planejamento.siconv.med.infra.rest.response.ResponseHelper.error;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;

import com.fasterxml.jackson.core.JacksonException;

import br.gov.planejamento.siconv.med.infra.util.ApplicationProperties;

@ApplicationScoped
public class JacksonExceptionHandler {

    @Inject
    private Logger logger;

    @Inject
    private ApplicationProperties config;

    public Response toResponse(String msgError, JacksonException exception) {

        Map<String, Object> data = new HashMap<>();

        if (config.isShowErrorStackTrace()) {
            data.put("stackTrace", ExceptionUtils.getStackTrace(exception));
        }

        logger.debug(msgError, exception);

        return error(Status.BAD_REQUEST.getStatusCode(), msgError, data);
    }
}
