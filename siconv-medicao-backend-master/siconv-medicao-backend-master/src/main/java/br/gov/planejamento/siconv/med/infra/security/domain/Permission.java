package br.gov.planejamento.siconv.med.infra.security.domain;

import static java.util.Arrays.asList;

import java.util.function.Predicate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Permission {

    INCLUIR_MEDICAO( 1000, "incluir_medicao"),
    EDITAR_MEDICAO( 1001, "editar_medicao"),
    EXCLUIR_MEDICAO( 1002, "excluir_medicao"),
    ENVIAR_MEDICAO_CONVENENTE( 1003, "enviar_medicao_convenente"),
    CANCELAR_ENVIO_MEDICAO_CONVENENTE( 1004, "cancelar_envio_medicao_convenente"),

    EDITAR_SUBMETA( 1005, "editar_submeta"),
    EXCLUIR_SUBMETA( 1006, "excluir_submeta"),
    ASSINAR_SUBMETA( 1007, "assinar_submeta"),

    INCLUIR_OBSERVACAO_MEDICAO( 1008, "incluir_observacao_medicao"),
    EDITAR_OBSERVACAO_MEDICAO( 1009, "editar_observacao_medicao"),
    EXCLUIR_OBSERVACAO_MEDICAO( 1010, "excluir_observacao_medicao"),

    VISUALIZAR_MEDICAO (1011, "visualizar_medicao");

    private final Integer id;

    private final String key;

    public static Permission fromId(Integer id) {
        return findValue(permission -> permission.getId().equals(id));
    }

    public static Permission fromKey(String key) {
        return findValue(permission -> permission.getKey().equalsIgnoreCase(key));
    }

    private static Permission findValue(Predicate<Permission> predicate) {
        return asList(values()).stream().filter(predicate).findAny().orElse(null);
    }
}
