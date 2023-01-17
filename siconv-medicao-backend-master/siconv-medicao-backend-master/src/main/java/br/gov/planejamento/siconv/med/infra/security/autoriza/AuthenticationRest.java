package br.gov.planejamento.siconv.med.infra.security.autoriza;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.OK;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static org.apache.commons.lang3.StringUtils.substring;
import static org.apache.commons.lang3.StringUtils.trim;

import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Path("/api")
public class AuthenticationRest {

    private static final String AUTHENTICATION_SCHEME = "Bearer";

    @Inject
    private GovBrClient govBrClient;

    @Inject
    private AutorizaTokenProvider autorizaTokenProvider;

    @POST
    @Path("/authenticate")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Response authenticate(Map<String, String> body) {

        try {
            Map<String, String> auth = govBrClient.authenticate(body.get("Code"), body.get("RedirectURI"));

            String jwt = autorizaTokenProvider.createToken(auth);

            return tokenResponseBody(jwt);

        } catch (Exception e) {

            String msg = "Falha no serviço de autenticação.";
            log.error(msg, e);

            return error(UNAUTHORIZED, msg);
        }
    }

    @GET
    @Path("/retoken")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Response retoken(final @Context HttpHeaders httpHeaders) {

        try {
            String authorizationHeader = httpHeaders.getHeaderString(HttpHeaders.AUTHORIZATION);

            if (authorizationHeader == null || authorizationHeader.isEmpty()) {

                return fail(UNAUTHORIZED, "As credenciais do usuário para revalidação não foram informadas.");
            }

            String token = trim(substring(authorizationHeader, AUTHENTICATION_SCHEME.length()));

            if (!autorizaTokenProvider.validateToken(token)) {

                return fail(UNAUTHORIZED, "As credenciais do usuário para revalidação estão inválidas.");
            }

            String newJwt = autorizaTokenProvider.retoken(token);

            return tokenResponseBody(newJwt);

        } catch (Exception e) {

            String msg = "Falha no serviço de revalidação de Token.";
            log.error(msg, e);

            return error(INTERNAL_SERVER_ERROR, msg);
        }
    }

    private static Response error(Status status, String message) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("status", "ERROR").putObject("data").putArray("errors").addObject().put("detail", message);
        return Response.status(status).entity(node).build();
    }

    private static Response fail(Status status, String message) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("status", "FAIL").putObject("data").putArray("errors").addObject().put("detail", message);
        return Response.status(status).entity(node).build();
    }

    private static Response tokenResponseBody(String jwt) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.putObject("body").put("token", jwt);
        return Response.status(OK).entity(node).build();
    }
}
