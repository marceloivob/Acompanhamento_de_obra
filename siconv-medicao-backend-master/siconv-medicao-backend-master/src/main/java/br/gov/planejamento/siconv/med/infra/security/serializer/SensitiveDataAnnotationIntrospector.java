package br.gov.planejamento.siconv.med.infra.security.serializer;

import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.NopAnnotationIntrospector;

import br.gov.planejamento.siconv.med.infra.security.annotation.SensitiveData;
import br.gov.planejamento.siconv.med.infra.security.domain.SensitiveDataType;

public class SensitiveDataAnnotationIntrospector extends NopAnnotationIntrospector {

    private static final long serialVersionUID = 1L;

    @Override
    public Object findSerializer(Annotated am) {
        SensitiveData annotation = am.getAnnotation(SensitiveData.class);
        if (annotation != null) {
            return getSerializerFromType(annotation.type());
        }
        return null;
    }

    private Class<? extends JsonSerializer<?>> getSerializerFromType(SensitiveDataType type) {

        switch (type) {
        case CPF:
            return CpfObfuscator.class;

        case TELEFONE:
            return TelefoneObfuscator.class;

        case EMAIL:
            return StringObfuscator.class;

        case URL:
            return NullObfuscator.class;

        default:
            return null;
        }
    }
}
