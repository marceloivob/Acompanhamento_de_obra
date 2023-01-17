package br.gov.planejamento.siconv.med.medicao.entity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.dto.TipoResponsavelTecnicoEnum;
import lombok.Data;

@Data
public class ResponsavelTecnicoFiscalizacaoDTO {

	private Long idResponsavelTecnico;
	private TipoResponsavelTecnicoEnum tipo;
	private String nmResponsavelTecnico;
	private String nrCpfResponsavelTecnico;
	private String cdResponsavelTecnico;
	private String uf;

	@JsonProperty(access = Access.READ_ONLY)
	public String getCreaCau() {
		if (this.getUf() != null && this.getUf().length() > 0) {
			return this.getCdResponsavelTecnico() + "/" + this.getUf();
		} else {
			return this.getCdResponsavelTecnico();
		}
	}

	@JsonProperty(access = Access.READ_ONLY)
	public String getPerfil() {
		if (this.getTipo() != null && this.getTipo().equals(TipoResponsavelTecnicoEnum.EXE)) {
			return "Empresa";
		} else {
			return "Convenente";
		}
	}

}
