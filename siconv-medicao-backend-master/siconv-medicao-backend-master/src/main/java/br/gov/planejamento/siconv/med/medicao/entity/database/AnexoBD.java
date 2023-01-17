package br.gov.planejamento.siconv.med.medicao.entity.database;

import lombok.Data;

@Data
public class AnexoBD {

	private Long id;
	private String nmArquivo;
	private String coCeph;
	private Long observacaoFk;
	private boolean inInativo;
	private String nrCpfInativo;
	

}