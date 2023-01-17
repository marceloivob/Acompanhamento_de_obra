package br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.database;

import lombok.Data;

@Data
public class AnotacaoRegistroRtSubmetaBD {

//	@ColumnName("id") // tabela interna - med_anotacao_registro_rt_submeta
	private Long id;
	
//	@ColumnName("vrpl_submeta_fk") 
	private Long vrplSubmetaFk;
	
//	@ColumnName("med_anotacao_registro_rt_fk") 
	private Long anotacaoRegistroRtFk;
	
	public AnotacaoRegistroRtSubmetaBD(Long vrplSubmetaFk, Long anotacaoRegistroRtFk) {
		super();
		this.vrplSubmetaFk = vrplSubmetaFk;
		this.anotacaoRegistroRtFk = anotacaoRegistroRtFk;
	}
	
	@Override
	public boolean equals(Object obj) {
		
		boolean retorno = false; 
		
		if (obj instanceof AnotacaoRegistroRtSubmetaBD) {
			if (((AnotacaoRegistroRtSubmetaBD)obj).getVrplSubmetaFk() != null && this.getVrplSubmetaFk() != null) {
				retorno = ((AnotacaoRegistroRtSubmetaBD)obj).getVrplSubmetaFk().equals(this.getVrplSubmetaFk());
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
