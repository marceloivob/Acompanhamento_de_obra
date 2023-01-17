package br.gov.planejamento.siconv.med.configuracao.paralisacao.entity.database;

import lombok.Data;

@Data
public class AnexoParalisacaoBD {

	private Long id;
	private String nmArquivo;
	private String coCeph;
	private Long paralisacaoFk;
}
