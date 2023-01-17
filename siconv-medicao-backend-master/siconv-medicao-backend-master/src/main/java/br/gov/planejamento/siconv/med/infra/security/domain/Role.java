package br.gov.planejamento.siconv.med.infra.security.domain;

import static java.util.Arrays.asList;

import java.util.function.Predicate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Role {

    FISCAL_CONVENENTE("fiscal_convenente"),

    GESTOR_CONVENIO_CONVENENTE("gestor_convenio_convenente"),

    GESTOR_FINANCEIRO_CONVENENTE("gestor_financeiro_convenente"),

    OPERADOR_FINANCEIRO_CONVENENTE("operador_financeiro_convenente"),

    AGENTE_ACOMPANHAMENTO_INSTITUICAO_MANDATARIA("agente_acompanhamento_instituicao_mandataria"),

    FISCAL_CONCEDENTE("fiscal_concedente"),

    OPERACIONAL_CONCEDENTE("operacional_concedente"),

    GESTOR_CONVENIO_CONCEDENTE("gestor_convenio_concedente"),

    GESTOR_FINANCEIRO_CONCEDENTE("gestor_financeiro_concedente"),

    FISCAL_ACOMPANHAMENTO("fiscal_acompanhamento"),

    TECNICO_TERCEIRO("tecnico_terceiro"),

    ADMINISTRADOR_SISTEMA("administrador_sistema"),

    ADMINISTRADOR_SISTEMA_ORGAO_EXTERNO("administrador_sistema_orgao_externo");

    private final String key;

    public static Role fromKey(String key) {
        return findValue(role -> role.getKey().equalsIgnoreCase(key));
    }

    private static Role findValue(Predicate<Role> predicate) {
        return asList(values()).stream().filter(predicate).findAny().orElse(null);
    }
}
