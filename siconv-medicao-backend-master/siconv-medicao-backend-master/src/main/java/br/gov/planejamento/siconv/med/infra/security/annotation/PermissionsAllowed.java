package br.gov.planejamento.siconv.med.infra.security.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import br.gov.planejamento.siconv.med.infra.security.domain.Permission;
import br.gov.planejamento.siconv.med.infra.security.domain.Profile;

@Target({ METHOD })
@Retention(RUNTIME)
@Repeatable(PermissionsAllowed.List.class)
public @interface PermissionsAllowed {

    Profile profile();

    Permission[] permissions() default {};

    @Target({ METHOD })
    @Retention(RUNTIME)
    @interface List {
        PermissionsAllowed[] value();
    }
}
