package br.gov.planejamento.siconv.med.medicao.entity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@RequiredArgsConstructor
@Getter
public enum PerfilEnum {

	/**
	 * CCE - Concedente
	 */
	CCE("Concedente"),

	/**
	 * MAN - Mandatária
	 */
	MAN("Mandatária"),

	/**
	 * CVE - Convenente
	 */
	CVE("Convenente"),

	/**
	 * EMP - Empresa
	 */
	EMP("Empresa"),

	/**
	 * FSA - Fiscal Acompanhamento e Fiscalização
	 */
	FSA("Fiscal Acompanhamento e Fiscalização"),

	/**
	 * TTE - Técnico de Terceiro Acompanhamento e Fiscalização
	 */
	TTE("Técnico de Terceiro Acompanhamento e Fiscalização"),

	/**
	 * ADM - Administrador do Sistema
	 */
	ADM("Sistema");

	private final String descricao;

	@JsonProperty
	public String getCodigo() {
		return name();
	}

}
