package br.gov.planejamento.siconv.med.acompanhamento.entity.dto;

import br.gov.planejamento.siconv.med.contrato.entity.ModalidadeEnum;
import lombok.Data;

@Data
public class TipoInstrumentoDTO {

	private Integer numeroConvenioRepasse;

	private Integer anoConvenioRepasse;

	private String localidade;

	private String nomeObjetoContratoRepasse;

	private String urlSiconvMedicao;

	private ModalidadeEnum modalidade;
	
	private String nomeConvenente;
}
