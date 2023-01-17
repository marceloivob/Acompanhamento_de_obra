package br.gov.planejamento.siconv.med.infra.security.serializer;

public class CpfObfuscator extends ObfuscatorSerializer<String> {

    private static final long serialVersionUID = 1L;

    @Override
    protected String obfuscate(String cpf) {
        return String.format("%s", "***" + cpf.substring(3, 9) + "**");
    }
}