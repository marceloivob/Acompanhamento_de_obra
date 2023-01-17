package br.gov.planejamento.siconv.med.test.builder;

import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.database.ContratoResponsavelTecnicoBD;
import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.dto.TipoResponsavelTecnicoEnum;

public class ContratoResponsavelTecnicoBDBuilder {

	private ContratoResponsavelTecnicoBD contratoResponsavelTecnicoBD;

    public ContratoResponsavelTecnicoBDBuilder() {
	}
    
	public ContratoResponsavelTecnicoBD create() {
		return this.contratoResponsavelTecnicoBD;
	}
	
    public static ContratoResponsavelTecnicoBDBuilder newContratoResponsavelTecnicoBDBuilder() {
    	ContratoResponsavelTecnicoBDBuilder contratoResponsavelTecnicoBDBuilder = new ContratoResponsavelTecnicoBDBuilder();
    	contratoResponsavelTecnicoBDBuilder.contratoResponsavelTecnicoBD = new ContratoResponsavelTecnicoBD();
		return contratoResponsavelTecnicoBDBuilder;
	}
	
	public ContratoResponsavelTecnicoBDBuilder setId(Long id) {
		this.contratoResponsavelTecnicoBD.setId(id);
		return this;
	}
	
	public ContratoResponsavelTecnicoBDBuilder setTipo(TipoResponsavelTecnicoEnum tipo) {
		this.contratoResponsavelTecnicoBD.setTipo(tipo);
		return this;
	}
	
	public ContratoResponsavelTecnicoBDBuilder setIdMedContrato(Long idContrato) {
		this.contratoResponsavelTecnicoBD.setContrato(idContrato);
		return this;
	}
	
}
