package br.gov.planejamento.siconv.med.medicao.entity.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class EventoFrenteObraTotalizadoDTO {

	private Long idSubmetaVrpl;
	private Long idEvento;
	private Long idFrenteObra;
	private BigDecimal totalEvento;
	
	
}
