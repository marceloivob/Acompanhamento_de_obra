package br.gov.planejamento.siconv.med.infra.security.domain;

import static java.util.Arrays.asList;

import java.util.function.Predicate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum TokenIssuer {

    IDP("siconvidp"),

    PLATAFORMA_MAIS_BRASIL("maisbrasil");

    @Getter
    private final String key;

    public static TokenIssuer fromKey(String key) {
        return findValue(issuer -> issuer.getKey().equals(key));
    }

    private static TokenIssuer findValue(Predicate<TokenIssuer> predicate) {
        return asList(values()).stream().filter(predicate).findAny().orElse(null);
    }
}
