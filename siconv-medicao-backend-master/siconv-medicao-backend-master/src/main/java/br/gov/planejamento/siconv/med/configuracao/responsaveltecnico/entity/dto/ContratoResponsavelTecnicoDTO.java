package br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.dto;

import java.time.LocalDateTime;

import org.jdbi.v3.core.mapper.reflect.ColumnName;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.database.ContratoResponsavelTecnicoBD;
import lombok.Data;

@Data
public class ContratoResponsavelTecnicoDTO {

	@ColumnName("id") // tabela interna - med_contrato_resp_tecnico
	private Long id;

	@ColumnName("dt_inclusao") // tabela interna - med_contrato_resp_tecnico
	private LocalDateTime dataInclusao;

	@ColumnName("in_tipo") // // tabela interna - med_contrato_resp_tecnico
	private TipoResponsavelTecnicoEnum tipo;

    @ColumnName("contrato_fk")
    public Long contratoFk;
    
	@ColumnName("versao") //tabela interna - med_contrato_resp_tecnico
	private Long versao;

	@JsonInclude(Include.NON_NULL)
	private Boolean possuiART;
	
	@JsonInclude(Include.NON_NULL)
	private Boolean possuiARTAtiva;

	@JsonInclude(Include.NON_NULL)
	private Boolean possuiSubmetaAssinada;

	@JsonProperty(access = Access.READ_ONLY)
	public String getDescricaoTipo() {
		return this.getTipo().getDescricao();
	}

	public ContratoResponsavelTecnicoBD converterParaBD() {

		ContratoResponsavelTecnicoBD contratoResponsavelTecnicoBD = new ContratoResponsavelTecnicoBD();

		contratoResponsavelTecnicoBD.setId(this.id);
		contratoResponsavelTecnicoBD.setDataInclusao(this.dataInclusao);
		contratoResponsavelTecnicoBD.setTipo(this.tipo);
		contratoResponsavelTecnicoBD.setVersao(this.versao);

		return contratoResponsavelTecnicoBD;

	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ContratoResponsavelTecnicoDTO other = (ContratoResponsavelTecnicoDTO) obj;
		if (contratoFk == null) {
			if (other.contratoFk != null)
				return false;
		} else if (!contratoFk.equals(other.contratoFk)) {
			return false;
		}
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id)) {
			return false;
		}
		
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((contratoFk == null) ? 0 : contratoFk.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}
	
	
}
