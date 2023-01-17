package br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.dto;

import java.util.ArrayList;
import java.util.List;

import org.jdbi.v3.core.mapper.reflect.ColumnName;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.database.RegistroProfissionalBD;
import lombok.Data;

@Data
@JsonIgnoreProperties ("isRPAssociadoOutroCTEF")
public class RegistroProfissionalDTO {

	@ColumnName("id") // tabela interna - med_registro_profissional
	private Long id;

	@ColumnName("uf") // tabela interna - med_registro_profissional
	private String uf;
	
	@ColumnName("atividade") // tabela interna - med_registro_profissional
	private String atividade;

	@ColumnName("versao") //tabela interna - med_registro_profissional
	private Long versao;
	
	@ColumnName("crea_cau") // tabela interna - med_registro_profissional
	private String nrCreaCau;

	
	@JsonInclude(value = Include.NON_EMPTY)
	private List<ContratoResponsavelTecnicoDTO> contratos = new ArrayList<>();
	

	public RegistroProfissionalBD converterParaBD() {

		RegistroProfissionalBD registroProfissionalBD = new RegistroProfissionalBD();
		registroProfissionalBD.setId(this.id);
		registroProfissionalBD.setAtividade(this.atividade);
		registroProfissionalBD.setNrCreaCau(this.nrCreaCau);
		registroProfissionalBD.setUf(this.uf);
		registroProfissionalBD.setVersao(this.versao);

		return registroProfissionalBD;

	}

	@Override
	public boolean equals(Object obj) {
		
		boolean retorno = false; 
		
		if (obj instanceof RegistroProfissionalDTO) {
			if (((RegistroProfissionalDTO)obj).getId() != null && this.getId() != null) {
				retorno = ((RegistroProfissionalDTO)obj).getId().equals(this.getId());
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
	
	/**
	 * Recupera o Objeto Contrato informado no @contratoFk se não tiver retorna null.
	 * 
	 * @param contratoFk
	 * @return
	 */
	public ContratoResponsavelTecnicoDTO obterContratoVinculado (Long contratoFk) {
		
		ContratoResponsavelTecnicoDTO contrato = null;
		
		for (ContratoResponsavelTecnicoDTO itContrato : getContratos()) {
			if (itContrato.contratoFk.equals(contratoFk)) {
				contrato = itContrato;
			}
		}
		
		return contrato;
	}	
	
	
	
}
