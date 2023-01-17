package br.gov.planejamento.siconv.med.infra.security.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.enterprise.util.Nonbinding;
import javax.interceptor.InterceptorBinding;

import br.gov.planejamento.siconv.med.infra.security.provider.AuthorityContextProvider;

@Target({ METHOD })
@Retention(RUNTIME)
@InterceptorBinding
@AuthorityContextInterceptorBinding
public @interface AuthorityContextSuppliedBy {

    @Nonbinding
    Class<? extends AuthorityContextProvider<?>> provider(); //NOSONAR

    @Nonbinding
    String param();
}
