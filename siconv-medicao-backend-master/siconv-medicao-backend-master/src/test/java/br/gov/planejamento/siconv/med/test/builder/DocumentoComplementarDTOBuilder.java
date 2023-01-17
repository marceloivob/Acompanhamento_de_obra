package br.gov.planejamento.siconv.med.test.builder;

import java.time.LocalDate;
import java.util.List;

import br.gov.planejamento.siconv.med.configuracao.documentocomplementar.entity.dto.DocumentoComplementarDTO;
import br.gov.planejamento.siconv.med.configuracao.documentocomplementar.entity.dto.TipoDocumentoEnum;
import br.gov.planejamento.siconv.med.configuracao.documentocomplementar.entity.dto.TipoManifestoEnum;
import br.gov.planejamento.siconv.med.medicao.entity.dto.SubmetaVrplDTO;

public class DocumentoComplementarDTOBuilder {

	private DocumentoComplementarDTO documento;
	
	public DocumentoComplementarDTOBuilder() {}
	
	public static DocumentoComplementarDTOBuilder newDocumentoComplementar() {
		DocumentoComplementarDTOBuilder docBuilder = new DocumentoComplementarDTOBuilder();
    	docBuilder.documento = new DocumentoComplementarDTO();
		return docBuilder;
	}
	
	public DocumentoComplementarDTO create() {
		return this.documento;
	}
	
	public DocumentoComplementarDTOBuilder setId(Long id) {
		this.documento.setId(id);
		return this;
	}

	public DocumentoComplementarDTOBuilder setBloqueado(boolean bloqueado) {
		this.documento.setBloqueado(bloqueado);
		return this;
	}
	
	public DocumentoComplementarDTOBuilder setMedContratoFk(Long medContratoFk) {
		this.documento.setMedContratoFk(medContratoFk);
		return this;
	}	

	public DocumentoComplementarDTOBuilder setIdContratoSiconv(Long idContratoSiconv) {
		this.documento.setIdContratoSiconv(idContratoSiconv);
		return this;
	}
	
	public DocumentoComplementarDTOBuilder setTipoDocumento(TipoDocumentoEnum tipo) {
		this.documento.setTipoDocumento(tipo);
		return this;
	}
	
	public DocumentoComplementarDTOBuilder setTipoManifestoAmbiental(TipoManifestoEnum tipo) {
		this.documento.setTipoManifestoAmbiental(tipo);
		return this;
	}
	
	public DocumentoComplementarDTOBuilder setSubmetas(List<SubmetaVrplDTO> submetas) {
		this.documento.setSubmetas(submetas);
		return this;
	}
	
	public DocumentoComplementarDTOBuilder setDtEmissao(LocalDate dtEmissao) {
		this.documento.setDtEmissao(dtEmissao);
		return this;
	}
	
	public DocumentoComplementarDTOBuilder setDtValidade(LocalDate dtValidade) {
		this.documento.setDtValidade(dtValidade);
		return this;
	}
	
	public DocumentoComplementarDTOBuilder setEquivaleALicencaInstalacao(boolean eqLicencaInstalacao) {
		this.documento.setEqLicencaInstalacao(eqLicencaInstalacao);
		return this;
	}

	public DocumentoComplementarDTOBuilder bloquearDocumento() {
		this.documento.setBloqueado(Boolean.TRUE);
		return this;
	}
	
	public DocumentoComplementarDTOBuilder desBloquearDocumento() {
		this.documento.setBloqueado(Boolean.FALSE);
		return this;
	}

	public DocumentoComplementarDTOBuilder setNrDocumento(String nrDocumento) {
		this.documento.setNrDocumento(nrDocumento);
		return this;
	}
	
}
