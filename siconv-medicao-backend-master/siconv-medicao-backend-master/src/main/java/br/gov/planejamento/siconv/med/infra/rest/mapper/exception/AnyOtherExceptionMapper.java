package br.gov.planejamento.siconv.med.infra.rest.mapper.exception;

import static br.gov.planejamento.siconv.med.infra.rest.response.ResponseHelper.error;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;

import br.gov.planejamento.siconv.med.infra.util.ApplicationProperties;
import br.gov.planejamento.siconv.med.infra.util.GeradorTicket;

@Provider
public class AnyOtherExceptionMapper implements ExceptionMapper<Throwable> {

    @Inject
    private Logger logger;

    @Inject
    private GeradorTicket geradorTicket;

    @Inject
    private ApplicationProperties config;

    @Override
    public Response toResponse(Throwable exception) {

        if (exception.getClass().getName().equalsIgnoreCase("org.jboss.resteasy.spi.DefaultOptionsMethodException")) {
            return Response.ok().build();
        }

        String ticket = geradorTicket.gerar();

        logger.error("Ocorreu uma exceção crítica não tratada pela aplicação | Ticket: {}", ticket, exception);

        Map<String, Object> data = new HashMap<>();
        data.put("ticket", ticket);

        if (config.isShowErrorStackTrace()) {
            data.put("stackTrace", ExceptionUtils.getStackTrace(exception));
        }

        return error(Status.INTERNAL_SERVER_ERROR.getStatusCode(), "Unhandled server exception", data);
    }
}
