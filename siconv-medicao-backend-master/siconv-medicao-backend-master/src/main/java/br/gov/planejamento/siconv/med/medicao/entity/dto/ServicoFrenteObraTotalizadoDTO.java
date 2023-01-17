package br.gov.planejamento.siconv.med.medicao.entity.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ServicoFrenteObraTotalizadoDTO {

	private Long idSubmetaVrpl;
	private Long idServico;
	private Long idFrenteObra;
	private BigDecimal qtdeServico;
	private BigDecimal vlPrecoUnitarioLicitado;
	
}
