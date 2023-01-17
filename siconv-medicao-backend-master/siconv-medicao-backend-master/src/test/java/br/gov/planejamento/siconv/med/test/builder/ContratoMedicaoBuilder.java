package br.gov.planejamento.siconv.med.test.builder;

import java.time.LocalDate;

import br.gov.planejamento.siconv.med.contrato.entity.ContratoBD;

public class ContratoMedicaoBuilder {
    
	private ContratoBD contrato;

    public ContratoMedicaoBuilder() {
	}
	
    public static ContratoMedicaoBuilder newContratoMedicaoBuilder() {
    	ContratoMedicaoBuilder contratoBuilder = new ContratoMedicaoBuilder();
    	contratoBuilder.contrato = new ContratoBD();
    	contratoBuilder.contrato.setInAcompanhamentoEventos(true);
		return contratoBuilder;
	}
	
	public ContratoBD create() {
		return this.contrato;
	}
	
	public ContratoMedicaoBuilder setId(Long id) {
		this.contrato.id = id;
		return this;
	}
	
	public ContratoMedicaoBuilder setContratoSiconv(Long id) {
		this.contrato.contratoFk = id;
		return this;
	}
	
	public ContratoMedicaoBuilder isSocial() {
		this.contrato.setInSocial(true);
		return this;
	}
	
	public ContratoMedicaoBuilder porEventos(boolean inAcompanhamentoEventos) {
	    this.contrato.setInAcompanhamentoEventos(inAcompanhamentoEventos);
	    return this;
	}

	public ContratoMedicaoBuilder setContratoFk(Long contratoFk) {
		this.contrato.setContratoFk(contratoFk);
		return this;
	}

	public ContratoMedicaoBuilder setAcompanhadoPorEventos(Boolean acompanhadoEventos) {
		this.contrato.setInAcompanhamentoEventos(acompanhadoEventos);
		return this;
	}

	public ContratoMedicaoBuilder setDataInicioObra(LocalDate dataInicioObra) {
		this.contrato.setDataInicioObra(dataInicioObra);
		return this;
	}
}
