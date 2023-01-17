package br.gov.planejamento.siconv.med.configuracao.paralisacao.entity.dto;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;

import br.gov.planejamento.siconv.med.configuracao.paralisacao.entity.database.AnexoParalisacaoBD;
import br.gov.planejamento.siconv.med.infra.validation.InsertGroup;
import lombok.Data;

@Data
public class AnexoParalisacaoDTO {

	private Long id;

	@NotEmpty
	@Size(min = 1, max = 100)
	private String nmArquivo;

	@JsonIgnore
	private String coCeph;

	private String url;

	@JsonIgnore
	@NotNull(groups = { InsertGroup.class })
	private byte[] arquivo;

	private Long paralisacaoFk;

	public AnexoParalisacaoBD converterParaBD() {

		AnexoParalisacaoBD anexoParalisacaoBD = new AnexoParalisacaoBD();

		anexoParalisacaoBD.setId(this.id);
		anexoParalisacaoBD.setNmArquivo(this.nmArquivo);
		anexoParalisacaoBD.setCoCeph(this.coCeph);
		anexoParalisacaoBD.setParalisacaoFk(this.paralisacaoFk);

		return anexoParalisacaoBD;
	}
}
