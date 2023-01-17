package br.gov.planejamento.siconv.med.test.builder;

import java.time.LocalDate;
import java.util.List;

import br.gov.planejamento.siconv.med.configuracao.paralisacao.entity.dto.AnexoParalisacaoDTO;
import br.gov.planejamento.siconv.med.configuracao.paralisacao.entity.dto.ParalisacaoDTO;

public class ParalisacaoDTOBuilder {
	
	private ParalisacaoDTO paralisacao;
	
	public ParalisacaoDTOBuilder() {}
	
	public static ParalisacaoDTOBuilder newParalisacao() {
		ParalisacaoDTOBuilder paralisacaoBuilder = new ParalisacaoDTOBuilder();
    	paralisacaoBuilder.paralisacao = new ParalisacaoDTO();
		return paralisacaoBuilder;
	}
	
	public ParalisacaoDTO create() {
		return this.paralisacao;
	}
	
	public ParalisacaoDTOBuilder setId(Long id) {
		this.paralisacao.setId(id);
		return this;
	}
	
	public ParalisacaoDTOBuilder setIdContratoSiconv(Long idContratoSiconv) {
		this.paralisacao.setIdContratoSiconv(idContratoSiconv);
		return this;
	}
	
	public ParalisacaoDTOBuilder setMedContratoFk(Long medContratoFk) {
		this.paralisacao.setMedContratoFk(medContratoFk);
		return this;
	}
	
	public ParalisacaoDTOBuilder setDataInicio(LocalDate dataInicio) {
		this.paralisacao.setDataInicio(dataInicio);
		return this;
	}
	
	public ParalisacaoDTOBuilder setDataFim(LocalDate dataFim) {
		this.paralisacao.setDataFim(dataFim);
		return this;
	}
	
	public ParalisacaoDTOBuilder setAnexos(List<AnexoParalisacaoDTO> anexos) {
		this.paralisacao.setAnexos(anexos);
		return this;
	}

}
