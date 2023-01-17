package br.gov.planejamento.siconv.med.configuracao.paralisacao.entity.dto;

import static org.apache.commons.lang3.StringUtils.upperCase;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@RequiredArgsConstructor
@Getter
public enum ResponsavelParalisacaoEnum {

	EMP("Empresa"),
	CVE("Convenente"),
	CCE("Concedente"),
	MAN("Mandatária"),
	ORG("Órgão de controle"),
	JUD("Judiciário"),
	OUT("Outros");

	@Getter
	private final String descricao;

	@JsonProperty
	public String getCodigo() {
		return name();
	}
	
    @JsonCreator
    public static ResponsavelParalisacaoEnum fromCodigo(@JsonProperty("codigo") final String codigo) {
        return valueOf(upperCase(codigo));
    }


}
