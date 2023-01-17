package br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.database;

import org.jdbi.v3.core.mapper.reflect.ColumnName;

import lombok.Data;

@Data
public class SubmetaResponsavelTecnicoSocialBD {

	@ColumnName("id") // tabela interna - med_submeta_social
	private Long id;
	
	@ColumnName("vrpl_submeta_fk") 
	private Long vrplSubmetaFk;
	
	@ColumnName("med_contrato_resp_tecnico_social_fk") // tabela interna - med_contrato_resp_tecnico_social
	private Long responsavelTencicoSocialFk;
	
	public SubmetaResponsavelTecnicoSocialBD(Long vrplSubmetaFk, Long responsavelTencicoSocialFk) {
		super();
		this.vrplSubmetaFk = vrplSubmetaFk;
		this.responsavelTencicoSocialFk = responsavelTencicoSocialFk;
	}
	
	@Override
	public boolean equals(Object obj) {
		
		boolean retorno = false; 
		
		if (obj instanceof SubmetaResponsavelTecnicoSocialBD) {
			if (((SubmetaResponsavelTecnicoSocialBD)obj).getVrplSubmetaFk() != null && this.getVrplSubmetaFk() != null) {
				retorno = ((SubmetaResponsavelTecnicoSocialBD)obj).getVrplSubmetaFk().equals(this.getVrplSubmetaFk());
			} else {
				retorno = this == obj;
			}
		} 
	
		return retorno;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}
}