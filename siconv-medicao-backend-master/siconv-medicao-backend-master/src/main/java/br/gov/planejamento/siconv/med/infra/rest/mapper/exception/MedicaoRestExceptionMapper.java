package br.gov.planejamento.siconv.med.infra.rest.mapper.exception;

import static br.gov.planejamento.siconv.med.infra.rest.response.ResponseHelper.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import br.gov.planejamento.siconv.med.infra.exception.MedicaoRestException;
import br.gov.planejamento.siconv.med.infra.message.ResourceBundleMessages;

@Provider
public class MedicaoRestExceptionMapper implements ExceptionMapper<MedicaoRestException> {

    @Inject
    private ResourceBundleMessages bundle;

    @Override
    public Response toResponse(MedicaoRestException exception) {

        ArrayList<Object> errors = new ArrayList<>();

        exception.getMessages().stream().forEach(message -> {

            Map<String, Object> error = new HashMap<>();
            error.put("code", message.getKey().value());
            error.put("detail", bundle.getString(message));
            error.put("severity", exception.getSeverity());

            errors.add(error);
        });

        Map<String, Object> data = new HashMap<>();
        data.put("errors", errors);

        return fail(exception.getStatusCode(), data);
    }
}
