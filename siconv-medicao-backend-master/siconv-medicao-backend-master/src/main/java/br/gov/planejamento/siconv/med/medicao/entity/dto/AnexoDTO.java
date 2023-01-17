package br.gov.planejamento.siconv.med.medicao.entity.dto;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import br.gov.planejamento.siconv.med.medicao.entity.database.AnexoBD;
import lombok.Data;

@Data
public class AnexoDTO {

	private Long id;
	private String nmArquivo;
	@JsonIgnore
	private String coCeph;
	private Long observacaoFk;
	private String url;
	private boolean inInativo;
	private String nrCpfInativo;
	private String nomeCpfInativo;
	
	@JsonProperty(access = WRITE_ONLY)
	private byte[] arquivo;
	
	public AnexoBD converterParaBD() {
		
		AnexoBD anexoBD = new AnexoBD();
		
		anexoBD.setId(this.id);
		anexoBD.setNmArquivo(this.nmArquivo);
		anexoBD.setCoCeph(this.coCeph);
		anexoBD.setObservacaoFk(this.observacaoFk);
		anexoBD.setInInativo(this.inInativo);
		anexoBD.setNrCpfInativo(this.nrCpfInativo);
		
		return anexoBD;
		
	}

}
