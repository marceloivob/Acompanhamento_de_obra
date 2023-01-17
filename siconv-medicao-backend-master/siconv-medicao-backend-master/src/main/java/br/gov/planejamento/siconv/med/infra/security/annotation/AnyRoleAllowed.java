package br.gov.planejamento.siconv.med.infra.security.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import br.gov.planejamento.siconv.med.infra.security.domain.Profile;

@Target({ METHOD })
@Retention(RUNTIME)
@Repeatable(AnyRoleAllowed.List.class)
public @interface AnyRoleAllowed {

    Profile profile();

    @Target({ METHOD })
    @Retention(RUNTIME)
    @interface List {
        AnyRoleAllowed[] value();
    }
}
