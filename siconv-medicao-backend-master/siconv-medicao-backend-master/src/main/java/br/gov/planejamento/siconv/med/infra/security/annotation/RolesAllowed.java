package br.gov.planejamento.siconv.med.infra.security.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import br.gov.planejamento.siconv.med.infra.security.domain.Profile;
import br.gov.planejamento.siconv.med.infra.security.domain.Role;

@Target({ METHOD })
@Retention(RUNTIME)
@Repeatable(RolesAllowed.List.class)
public @interface RolesAllowed {

    Profile profile();

    Role[] roles() default {};

    @Target({ METHOD })
    @Retention(RUNTIME)
    @interface List {
        RolesAllowed[] value();
    }
}
