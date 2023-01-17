package br.gov.planejamento.siconv.med.test.builder;

import br.gov.planejamento.siconv.med.contrato.entity.ModalidadeEnum;
import br.gov.planejamento.siconv.med.contrato.entity.dto.ContratoSiconvDTO;

public class ContratoSiconvBuilder {
	
	private ContratoSiconvDTO contrato;

    public ContratoSiconvBuilder() {
	}
	
    public static ContratoSiconvBuilder newContratoDTOBuilder() {
    	ContratoSiconvBuilder contratoBuilder = new ContratoSiconvBuilder();
    	contratoBuilder.contrato = new ContratoSiconvDTO();
		return contratoBuilder;
	}
	
	public ContratoSiconvDTO create() {
		return this.contrato;
	}
	
	public ContratoSiconvBuilder setId(Long id) {
		this.contrato.setId(id);
		return this;
	}
	
	public ContratoSiconvBuilder setModalidade(ModalidadeEnum modalidade) {
		this.contrato.setModalidade(modalidade);
		return this;
	}
	
	public ContratoSiconvBuilder setCnpj(String cnpj) {
		this.contrato.setCnpj(cnpj);
		return this;
	}
	
	public ContratoSiconvBuilder setNumeroConvenioRepasse(Integer numeroConvenioRepasse) {
		this.contrato.setNumeroConvenioRepasse(numeroConvenioRepasse);
		return this;
	}
	
	public ContratoSiconvBuilder setAnoConvenioRepasse(Integer anoConvenioRepasse) {
		this.contrato.setAnoConvenioRepasse(anoConvenioRepasse);
		return this;
	}
}
