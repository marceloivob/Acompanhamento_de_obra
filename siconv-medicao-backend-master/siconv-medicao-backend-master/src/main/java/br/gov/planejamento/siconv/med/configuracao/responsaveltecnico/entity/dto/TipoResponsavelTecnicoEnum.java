package br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.dto;

import static org.apache.commons.lang3.StringUtils.upperCase;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@RequiredArgsConstructor
public enum TipoResponsavelTecnicoEnum {

	EXE("Execução"),
    FIS("Fiscalização"),
    ANS("Análise");

    @Getter
    private final String descricao;

    @JsonProperty
    public String getCodigo() {
        return name();
    }

    @JsonCreator
    public static TipoResponsavelTecnicoEnum fromCodigo(@JsonProperty("codigo") final String codigo) {
        return valueOf(upperCase(codigo));
    }
}
