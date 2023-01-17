package br.gov.planejamento.siconv.med.infra.security.serializer;

import java.io.IOException;

import javax.enterprise.inject.spi.CDI;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import br.gov.planejamento.siconv.med.infra.security.SecurityContext;

public abstract class ObfuscatorSerializer<T> extends StdSerializer<T> {

    private static final long serialVersionUID = 1L;

    protected ObfuscatorSerializer() {
        this(null);
    }

    protected ObfuscatorSerializer(Class<T> clazz) {
        super(clazz);
    }

    @Override
    public void serialize(T value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (getSecurityContext().isSensitiveDataObfuscationEnabled()) {
            gen.writeObject(obfuscate(value));
        } else {
            gen.writeObject(value);
        }
    }

    protected SecurityContext getSecurityContext() {
        return CDI.current().select(SecurityContext.class).get();
    }

    protected abstract T obfuscate(T value);
}