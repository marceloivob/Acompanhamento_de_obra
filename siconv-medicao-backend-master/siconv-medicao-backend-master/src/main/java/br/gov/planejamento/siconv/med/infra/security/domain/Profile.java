package br.gov.planejamento.siconv.med.infra.security.domain;

import static java.util.Arrays.asList;

import java.util.function.Predicate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Profile {

    CONCEDENTE("concedente", "Concedente"),

    PROPONENTE_CONVENENTE("proponente", "Convenente"),

    MANDATARIA("mandataria", "Mandatária"),

    EMPRESA("empresa", "Empresa"),

    GUEST("guest", "Acesso Livre"),

    USUARIO_SICONV("usuario_siconv", "Usuário do SICONV sem vínculo com a proposta");

    private final String key;

    private final String description;

    public static Profile fromKey(String key) {
        return findValue(profile -> profile.getKey().equalsIgnoreCase(key));
    }

    private static Profile findValue(Predicate<Profile> predicate) {
        return asList(values()).stream().filter(predicate).findAny().orElse(null);
    }
}
