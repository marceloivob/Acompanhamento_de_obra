package br.gov.planejamento.siconv.med.medicao.entity.dto.submetaservicosalvar;

import java.math.BigDecimal;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class ServicoSubmetaSalvarDTO {

	@NotNull
	private Long id;

	@DecimalMin("0")
	private BigDecimal qtdInformada;
}
