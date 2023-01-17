package br.gov.planejamento.siconv.med.configuracao.documentocomplementar.entity.database;

import lombok.Data;

@Data
public class DocumentoComplementarSubmetaBD {

	private Long id;
	
	private Long vrplSubmetaFk;
	
	private Long documentoComplementarFk;
	
	public DocumentoComplementarSubmetaBD(Long vrplSubmetaFk, Long documentoComplementarFk) {
		super();
		this.vrplSubmetaFk = vrplSubmetaFk;
		this.documentoComplementarFk = documentoComplementarFk;
	}
	
	@Override
	public boolean equals(Object obj) {
		
		boolean retorno = false; 
		
		if (obj instanceof DocumentoComplementarSubmetaBD) {
			if (((DocumentoComplementarSubmetaBD)obj).getVrplSubmetaFk() != null && this.getVrplSubmetaFk() != null) {
				retorno = ((DocumentoComplementarSubmetaBD)obj).getVrplSubmetaFk().equals(this.getVrplSubmetaFk());
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
