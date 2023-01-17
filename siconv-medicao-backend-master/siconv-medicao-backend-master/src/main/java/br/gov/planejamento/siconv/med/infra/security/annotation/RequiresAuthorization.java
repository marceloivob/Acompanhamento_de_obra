package br.gov.planejamento.siconv.med.infra.security.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.enterprise.util.Nonbinding;
import javax.interceptor.InterceptorBinding;

import br.gov.planejamento.siconv.med.infra.security.domain.AuthorizationScope;

@Inherited
@InterceptorBinding
@Target({ TYPE, METHOD })
@Retention(RUNTIME)
public @interface RequiresAuthorization {

    @Nonbinding
    AuthorizationScope to() default AuthorizationScope.INVOKE_METHOD;
}
