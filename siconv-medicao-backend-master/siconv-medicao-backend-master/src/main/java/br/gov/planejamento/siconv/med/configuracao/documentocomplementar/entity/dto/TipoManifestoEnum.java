package br.gov.planejamento.siconv.med.configuracao.documentocomplementar.entity.dto;

import static org.apache.commons.lang3.StringUtils.upperCase;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@RequiredArgsConstructor
public enum TipoManifestoEnum {

	DIS("Dispensa","DISPENSA"),
	LPR("Licença Prévia","LICENCA_PREVIA"),
	LIN("Licença de Instalação","LICENCA_DE_INSTALACAO"),
	LOP("Licença de Operação","LICENCA_DE_OPERACAO"),
	PRO("Protocolo","PROTOCOLO"),
	OUT("Outros","OUTROS");

	@Getter
	private final String descricao;

	@Getter
	private final String codigoProjetobasico;
	
	
	@JsonProperty
	public String getCodigo() {
		return name();
	}

	@JsonCreator
	public static TipoManifestoEnum fromCodigo(@JsonProperty("codigo") final String codigo) {
		return valueOf(upperCase(codigo));
	}
	
	public static TipoManifestoEnum fromCodigoProjetoBasico(@JsonProperty("codigoProjetoBasico") final String codigoProjetobasico) {
		
		return List.of(values()).stream().filter( (TipoManifestoEnum tipoManifestoEnum) -> 
			tipoManifestoEnum.getCodigoProjetobasico().equals(codigoProjetobasico)
		).findAny().orElseThrow(() -> new IllegalArgumentException(""));
	}

}