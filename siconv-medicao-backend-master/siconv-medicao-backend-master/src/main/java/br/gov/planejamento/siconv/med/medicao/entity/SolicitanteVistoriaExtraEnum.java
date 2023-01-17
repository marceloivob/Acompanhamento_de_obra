package br.gov.planejamento.siconv.med.medicao.entity;

import static org.apache.commons.lang3.StringUtils.upperCase;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@RequiredArgsConstructor
public enum SolicitanteVistoriaExtraEnum {

    /**
     * CVN - Convenente
     */
    CVN("Convenente"),

    /**
     * CCD - Concedente
     */
    CCD("Concedente"),

    /**
     * MDT - Mandatária
     */
    MDT("Mandatária"),

    /**
     * OCT - Órgão de Controle
     */
    CTR("Órgão de Controle"),

    /**
     * OUT - Outros
     */
    OUT("Outros");

    @Getter
    private final String descricao;

    @JsonProperty
    public String getCodigo() {
        return name();
    }

    @JsonCreator
    public static SolicitanteVistoriaExtraEnum fromCodigo(@JsonProperty("codigo") final String codigo) {
        return valueOf(upperCase(codigo));
    }

}
