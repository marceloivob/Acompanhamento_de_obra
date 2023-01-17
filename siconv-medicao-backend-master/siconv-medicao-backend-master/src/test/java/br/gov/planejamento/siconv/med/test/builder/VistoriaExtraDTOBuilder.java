package br.gov.planejamento.siconv.med.test.builder;

import java.time.LocalDate;

import br.gov.planejamento.siconv.med.medicao.entity.SolicitanteVistoriaExtraEnum;
import br.gov.planejamento.siconv.med.medicao.entity.dto.VistoriaExtraDTO;

public class VistoriaExtraDTOBuilder {
	
	private VistoriaExtraDTO vistoriaExtra;
	
	public VistoriaExtraDTOBuilder() {
	}
	
	public static VistoriaExtraDTOBuilder newVistoriaBuilder() {
		VistoriaExtraDTOBuilder vistoria = new VistoriaExtraDTOBuilder();
    	vistoria.vistoriaExtra = new VistoriaExtraDTO();
		return vistoria;
	}
	
	public VistoriaExtraDTO create() {
		return this.vistoriaExtra;
	}
	
	public VistoriaExtraDTOBuilder setDataVistoria(LocalDate data) {
		this.vistoriaExtra.setDataVistoriaExtra(data);
		return this;
	}
	
	public VistoriaExtraDTOBuilder setSolicitante(SolicitanteVistoriaExtraEnum solicitante) {
		this.vistoriaExtra.setSolicitanteVistoriaExtra(solicitante);
		return this;
	}
	
	public VistoriaExtraDTOBuilder isVistoriaExtra(boolean isVistoriaExtra) {
		this.vistoriaExtra.setVistoriaExtra(isVistoriaExtra);
		return this;
	}
	
	public VistoriaExtraDTOBuilder setVersao(Long versao) {
		this.vistoriaExtra.setVersao(versao);
		return this;
	}
}
