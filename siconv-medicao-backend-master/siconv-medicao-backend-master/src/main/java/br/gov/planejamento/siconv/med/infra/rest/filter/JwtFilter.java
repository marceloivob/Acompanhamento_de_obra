package br.gov.planejamento.siconv.med.infra.rest.filter;

import static br.gov.planejamento.siconv.med.infra.security.domain.TokenIssuer.IDP;
import static br.gov.planejamento.siconv.med.infra.security.domain.TokenIssuer.PLATAFORMA_MAIS_BRASIL;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.substring;
import static org.apache.commons.lang3.StringUtils.trim;

import javax.annotation.Priority;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;

import br.gov.planejamento.siconv.med.infra.exception.SecurityAccessException;
import br.gov.planejamento.siconv.med.infra.message.MessageKey;
import br.gov.planejamento.siconv.med.infra.security.annotation.UserAuthenticatedEvent;
import br.gov.planejamento.siconv.med.infra.security.autoriza.AutorizaTokenProvider;
import br.gov.planejamento.siconv.med.infra.security.domain.TokenIssuer;
import br.gov.planejamento.siconv.med.infra.security.jwt.IDPTokenProvider;
import lombok.extern.slf4j.Slf4j;

@Provider
@Priority(Priorities.AUTHENTICATION)
@Slf4j
public class JwtFilter implements ContainerRequestFilter {

    private static final String AUTHENTICATION_SCHEME = "Bearer";

    @Inject
    private IDPTokenProvider idpTokenProvider;

    @Inject
    private AutorizaTokenProvider maisbrasilTokenProvider;

    @Inject
    @UserAuthenticatedEvent
    private Event<DecodedJWT> userAuthenticatedEvent;

    @Override
    public void filter(ContainerRequestContext requestContext) {

        String authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        if (isBlank(authorizationHeader)) {
            return;
        }

        String token = trim(substring(authorizationHeader, AUTHENTICATION_SCHEME.length()));

        try {
            DecodedJWT jwt = JWT.decode(token);
            TokenIssuer issuer = TokenIssuer.fromKey(jwt.getIssuer());

            boolean valid = false;

            if (issuer == IDP) {
                valid = idpTokenProvider.validateToken(token);

            } else if (issuer == PLATAFORMA_MAIS_BRASIL) {
                valid = maisbrasilTokenProvider.validateToken(token);
            }

            if (valid) {
                userAuthenticatedEvent.fire(jwt);

            } else {
                throw new SecurityAccessException(MessageKey.ERRO_CREDENCIAL_AUTENTICACAO_INVALIDA,
                        Status.UNAUTHORIZED.getStatusCode());
            }

        } catch (JWTDecodeException e) {

            String msg = "Token informado possui formato de JWT inválido.";

            if (log.isDebugEnabled()) {
                log.debug(format("%s [Authorization Header: %s]", msg, authorizationHeader), e);

            } else {
                log.info(msg);
            }

            requestContext.abortWith(Response.status(Status.BAD_REQUEST).build());
        }
    }
}
