package br.gov.planejamento.siconv.med.infra.security.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import br.gov.planejamento.siconv.med.infra.security.domain.SensitiveDataType;

@Target({ FIELD })
@Retention(RUNTIME)
public @interface SensitiveData {

    SensitiveDataType type();
}
