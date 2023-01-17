package br.gov.planejamento.siconv.med.infra.logging;

import static org.apache.commons.collections.CollectionUtils.isEmpty;

import javax.enterprise.inject.spi.CDI;

import org.jboss.resteasy.core.ResteasyContext;
import org.jboss.resteasy.spi.HttpRequest;

import br.gov.planejamento.siconv.med.infra.security.SecurityContext;
import io.sentry.event.EventBuilder;
import io.sentry.event.helper.EventBuilderHelper;
import io.sentry.event.interfaces.UserInterface;

/**
 * {@link EventBuilderHelper} que coleta informações do {@link HttpRequest}
 * (Resteasy) e anexa ao evento que será enviado ao Sentry. A identificação do
 * usuário logado também será anexada.
 */
public class CustomEventBuilderHelper implements EventBuilderHelper {

    // Constantes
    private static final String URI = "uri";
    private static final String METHOD = "method";
    private static final String REMOTE_ADDRESS = "Remote-Address";
    private static final String HEADER_PREFIX = "H_";

    @Override
    public void helpBuildingEvent(EventBuilder eventBuilder) {
        try {
            addRequestInfo(eventBuilder);
            addUserInfo(eventBuilder);
        } catch (Exception e) {
            // Abafando exceção para não interferir no registro do evento no Sentry,
            // caso ocorra alguma falha na coleta das informações.
        }
    }

    private void addRequestInfo(EventBuilder eventBuilder) {

        HttpRequest request = ResteasyContext.getContextData(HttpRequest.class);

        if (request == null) {
            return;
        }

        eventBuilder.withTag(URI, request.getUri().getRequestUri().toString());
        eventBuilder.withTag(METHOD, request.getHttpMethod());
        eventBuilder.withExtra(REMOTE_ADDRESS, request.getRemoteAddress());

        addHeaders(eventBuilder, request);
    }

    private void addHeaders(EventBuilder eventBuilder, HttpRequest request) {
        request.getHttpHeaders().getRequestHeaders().forEach((name, values) -> {
            if (name != null && !isEmpty(values) && values.get(0) != null) {
                eventBuilder.withExtra(HEADER_PREFIX + name, values.get(0));
            }
        });
    }

    private void addUserInfo(EventBuilder eventBuilder) {

        SecurityContext securityContext = CDI.current().select(SecurityContext.class).get();

        if (securityContext == null) {
            return;
        }

        if (securityContext.isLoggedIn()) {
            eventBuilder.withSentryInterface(new UserInterface(securityContext.getUser().getCpf(), null, null, null));
        }
    }
}
