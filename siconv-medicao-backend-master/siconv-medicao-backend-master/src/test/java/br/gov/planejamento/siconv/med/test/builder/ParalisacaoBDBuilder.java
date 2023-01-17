package br.gov.planejamento.siconv.med.test.builder;

import java.time.LocalDate;

import br.gov.planejamento.siconv.med.configuracao.paralisacao.entity.database.ParalisacaoBD;

public class ParalisacaoBDBuilder {
	
	private ParalisacaoBD paralisacao;
	
	public ParalisacaoBDBuilder() {}
	
	public static ParalisacaoBDBuilder newParalisacaoBD() {
		ParalisacaoBDBuilder paralisacaoBuilder = new ParalisacaoBDBuilder();
    	paralisacaoBuilder.paralisacao = new ParalisacaoBD();
		return paralisacaoBuilder;
	}
	
	public ParalisacaoBD create() {
		return this.paralisacao;
	}
	
	public ParalisacaoBDBuilder setId(Long id) {
		this.paralisacao.setId(id);
		return this;
	}
	
	public ParalisacaoBDBuilder setDtInicio(LocalDate dtInicio) {
		this.paralisacao.setDtInicio(dtInicio);
		return this;
	}
	
	public ParalisacaoBDBuilder setDtFim(LocalDate dtFim) {
		this.paralisacao.setDtFim(dtFim);
		return this;
	}
	
	public ParalisacaoBDBuilder setMedContratoFk(Long idMedContrato) {
		this.paralisacao.setMedContratoFk(idMedContrato);
		return this;
	}
	
}
