package br.gov.planejamento.siconv.med.infra.security.serializer;

public class StringObfuscator extends ObfuscatorSerializer<String> {

    private static final long serialVersionUID = 1L;

    @Override
    protected String obfuscate(String value) {
        return "*".repeat(value.length());
    }
}