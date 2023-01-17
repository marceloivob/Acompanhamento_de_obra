package br.gov.planejamento.siconv.med.infra.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum Environment {

    LOCAL("dev"),

    DESENV("siconv-d"),

    TESTE("siconv-t"),

    VALIDACAO("siconv-v"),

    HOMOLOGACAO("siconv-h"),

    TREINAMENTO("siconv-tre"),

    PRODUCAO("siconv-p");

    @Getter
    private final String profile;

    public static Environment fromProfile(String profile) {
        for (Environment env : Environment.values()) {
            if (env.getProfile().equals(profile)) {
                return env;
            }
        }

        return PRODUCAO;
    }
}
