package br.gov.planejamento.siconv.med.contrato.entity.dto;

import lombok.Data;

@Data
public class AndamentoContratoDTO {

	private Long qtdeDiasSemMedicao;
	private boolean atrasado;
	private Long idUltimaMedicao;
	private Short sequencialUltimaMedicao;

	public boolean possuiMedicao() {
		return idUltimaMedicao != null && sequencialUltimaMedicao != null;
	}
}