package br.gov.planejamento.siconv.med.test.extension;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import br.gov.planejamento.siconv.med.infra.security.domain.Permission;
import br.gov.planejamento.siconv.med.infra.security.domain.Profile;
import br.gov.planejamento.siconv.med.infra.security.domain.Role;

@Target({ METHOD, TYPE })
@Retention(RUNTIME)
public @interface MockUsuario {

    String cpf() default "11111111111";

    Profile profile();

    Role[] roles() default {};

    Permission[] permissions() default {};
}
