package br.gov.planejamento.siconv.med.test.builder;

import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.dto.ResponsavelTecnicoDTO;

public class ResponsavelTecnicoDTOBuilder {
	private ResponsavelTecnicoDTO responsavelTecnico;

    public ResponsavelTecnicoDTOBuilder() {
	}
    
	public ResponsavelTecnicoDTO create() {
		return this.responsavelTecnico;
	}
	
    public static ResponsavelTecnicoDTOBuilder newResponsavelTecnicoDTOBuilder() {
    	ResponsavelTecnicoDTOBuilder responsavelTecnicoDTOBuilder = new ResponsavelTecnicoDTOBuilder();
    	responsavelTecnicoDTOBuilder.responsavelTecnico = new ResponsavelTecnicoDTO();
		return responsavelTecnicoDTOBuilder;
	}
	
	public ResponsavelTecnicoDTOBuilder setId(Long id) {
		this.responsavelTecnico.setId(id);
		return this;
	}
	
	public ResponsavelTecnicoDTOBuilder setCpf(String cpf) {
		this.responsavelTecnico.setCpf(cpf);
		return this;
	}

}
