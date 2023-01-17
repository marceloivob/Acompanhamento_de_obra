package br.gov.planejamento.siconv.med.infra.rest.mapper.exception;

import static br.gov.planejamento.siconv.med.infra.rest.response.ResponseHelper.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.validation.ConstraintViolationException;
import javax.validation.Path.Node;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import br.gov.planejamento.siconv.med.infra.message.ResourceBundleMessages;

@Provider
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Inject
    private ResourceBundleMessages bundle;

    @Override
    public Response toResponse(ConstraintViolationException exception) {

        ArrayList<Object> errors = new ArrayList<>();

        exception.getConstraintViolations().stream().forEach(violation -> {

            Iterator<Node> itPathNode = violation.getPropertyPath().iterator();

            Node lastPathNode;
            do {
                lastPathNode = itPathNode.next();

            } while (itPathNode.hasNext());

            String msg = violation.getMessage();
            if (isResourceBundleKeyFormat(msg)) {
                msg = bundle.getString(removeBraces(msg));
            }

            Map<String, Object> error = new HashMap<>();
            error.put("source", lastPathNode.getName());
            error.put("detail", msg);

            errors.add(error);
        });

        Map<String, Object> data = new HashMap<>();
        data.put("errors", errors);

        return fail(Status.PRECONDITION_FAILED.getStatusCode(), data);
    }

    private String removeBraces(String string) {
        String result = string;

        if (isResourceBundleKeyFormat(string)) {
            result = string.substring(1, string.length() - 1);
        }

        return result;
    }

    private boolean isResourceBundleKeyFormat(final String key) {
        return Pattern.matches("^\\{(.+)\\}$", key == null ? "" : key);
    }
}
