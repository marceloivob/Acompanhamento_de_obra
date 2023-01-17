package br.gov.planejamento.siconv.med.infra.security.interceptor;

import static java.lang.String.format;

import java.lang.reflect.Parameter;

import javax.annotation.Priority;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.ws.rs.PathParam;

import br.gov.planejamento.siconv.med.infra.exception.SecurityAccessException;
import br.gov.planejamento.siconv.med.infra.security.ResourceAuthorityContext;
import br.gov.planejamento.siconv.med.infra.security.SecurityContext;
import br.gov.planejamento.siconv.med.infra.security.annotation.AuthorityContextInterceptorBinding;
import br.gov.planejamento.siconv.med.infra.security.annotation.AuthorityContextSuppliedBy;
import br.gov.planejamento.siconv.med.infra.security.provider.AuthorityContextProvider;

@Priority(1000)
@Interceptor
@AuthorityContextInterceptorBinding
public class AuthorityContextInterceptor {

    @Inject
    SecurityContext securityContext;

    @AroundInvoke
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Object intercept(InvocationContext ic) throws Exception {

        if (!securityContext.isLoggedIn()) {
            throw new SecurityAccessException();
        }

        AuthorityContextSuppliedBy annotation = ic.getMethod().getAnnotation(AuthorityContextSuppliedBy.class);

        AuthorityContextProvider providerInstance = CDI.current().select(annotation.provider()).get();

        Object paramValue = getPathParamValue(ic, annotation.param());

        ResourceAuthorityContext authorityContext = providerInstance.get(paramValue);

        securityContext.setAuthorityContext(authorityContext);

        return ic.proceed();
    }

    private Object getPathParamValue(InvocationContext ic, String paramName) {

        Parameter[] methodParameters = ic.getMethod().getParameters();

        for (int i = 0; i < methodParameters.length; i++) {
            Parameter parameter = methodParameters[i];

            PathParam pathParam = parameter.getAnnotation(PathParam.class);

            if (pathParam != null && pathParam.value().equals(paramName)) {
                return ic.getParameters()[i];
            }
        }

        throw new IllegalArgumentException(format("Não foi declarado um PathParam chamado '%s' para o método '%s'.",
                paramName, ic.getMethod().getName()));
    }
}
