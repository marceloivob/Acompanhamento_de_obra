package br.gov.planejamento.siconv.med.test.builder;

import java.time.LocalDateTime;

import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.dto.ContratoResponsavelTecnicoSocialDTO;
import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.dto.ResponsavelTecnicoDTO;
import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.dto.TipoResponsavelTecnicoEnum;

public class ContratoResponsavelTecnicoSocialDTOBuilder {

	private ContratoResponsavelTecnicoSocialDTO contratoResponsavelTecnicoSocial;

    public ContratoResponsavelTecnicoSocialDTOBuilder() {
	}
    
	public ContratoResponsavelTecnicoSocialDTO create() {
		return this.contratoResponsavelTecnicoSocial;
	}
	
    public static ContratoResponsavelTecnicoSocialDTOBuilder newContratoResponsavelTecnicoSocialDTOBuilder() {
    	ContratoResponsavelTecnicoSocialDTOBuilder contratoResponsavelTecnicoSocialBuilder = new ContratoResponsavelTecnicoSocialDTOBuilder();
    	contratoResponsavelTecnicoSocialBuilder.contratoResponsavelTecnicoSocial = new ContratoResponsavelTecnicoSocialDTO();
		return contratoResponsavelTecnicoSocialBuilder;
	}
	
	public ContratoResponsavelTecnicoSocialDTOBuilder setId(Long id) {
		this.contratoResponsavelTecnicoSocial.setId(id);
		return this;
	}
	
	public ContratoResponsavelTecnicoSocialDTOBuilder setTipo(TipoResponsavelTecnicoEnum tipo) {
		this.contratoResponsavelTecnicoSocial.setTipo(tipo);
		return this;
	}
	
	public ContratoResponsavelTecnicoSocialDTOBuilder setCodigoCephArquivo(String ceph) {
		this.contratoResponsavelTecnicoSocial.getCodigoCephArquivo();
		return this;
	}

	public ContratoResponsavelTecnicoSocialDTOBuilder setResponsavelTecnico(ResponsavelTecnicoDTO rt) {
		this.contratoResponsavelTecnicoSocial.setResponsavelTecnico(rt);
		return this;
	}
	
	public ContratoResponsavelTecnicoSocialDTOBuilder setDtInativacao(LocalDateTime dtInativacao) {
		this.contratoResponsavelTecnicoSocial.setDtInativacao(dtInativacao);
		return this;
	}

	public ContratoResponsavelTecnicoSocialDTOBuilder setMedContratoFk(Long medContratoFk) {
		this.contratoResponsavelTecnicoSocial.setMedContratoFk(medContratoFk);
		return this;
	}
	
}
