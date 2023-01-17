package br.gov.planejamento.siconv.med.infra.security.serializer;

public class NullObfuscator extends ObfuscatorSerializer<Object> {

    private static final long serialVersionUID = 1L;

    @Override
    protected Object obfuscate(Object value) {
        return null;
    }
}