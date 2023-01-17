package br.gov.planejamento.siconv.med.infra.security.serializer;

public class TelefoneObfuscator extends ObfuscatorSerializer<String> {

    private static final long serialVersionUID = 1L;

    @Override
    protected String obfuscate(String telefone) {
        return telefone.replaceAll("[0-9]", "*");
    }
}