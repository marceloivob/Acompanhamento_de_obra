package br.gov.planejamento.siconv.med.infra.security.interceptor;

import static java.util.Arrays.asList;

import java.util.List;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import br.gov.planejamento.siconv.med.infra.exception.SecurityAccessException;
import br.gov.planejamento.siconv.med.infra.security.SecurityContext;
import br.gov.planejamento.siconv.med.infra.security.annotation.RequiresAuthentication;
import br.gov.planejamento.siconv.med.infra.security.domain.Profile;

@Priority(2000)
@RequiresAuthentication
@Interceptor
public class RequiresAuthenticationInterceptor {

    @Inject
    private SecurityContext securityContext;

    @AroundInvoke
    public Object intercept(InvocationContext ic) throws Exception {

        if (!securityContext.isLoggedIn()) {
            throw new SecurityAccessException();
        }

        final List<Profile> profiles = getProfilesAnnotation(ic);

        if (!profiles.isEmpty() && !profiles.contains(securityContext.getUser().getProfile())) {
            throw new SecurityAccessException();
        }

        return ic.proceed();
    }

    private List<Profile> getProfilesAnnotation(InvocationContext ic) {

        RequiresAuthentication annotation = ic.getMethod().getAnnotation(RequiresAuthentication.class);

        if (annotation == null) {
            annotation = ic.getTarget().getClass().getAnnotation(RequiresAuthentication.class);
        }

        return asList(annotation.profiles());
    }
}
