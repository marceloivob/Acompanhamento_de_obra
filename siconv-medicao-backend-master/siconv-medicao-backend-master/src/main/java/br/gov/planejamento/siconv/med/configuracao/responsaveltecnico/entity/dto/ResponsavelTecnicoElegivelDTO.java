package br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.dto;

import org.jdbi.v3.core.mapper.reflect.ColumnName;

import lombok.Data;

@Data
public class ResponsavelTecnicoElegivelDTO {

	@ColumnName("id") // tabela interna - med_responsavel_tecnico
	private Long id;

	@ColumnName("nr_cpf") // tabela interna - med_responsavel_tecnico
	private String cpf;

	@ColumnName("nm_responsavel") // tabela externa - vrpl_responsavel_tecnico
	private String nome;
	
	private String email;
	
	private String telefone; 
	
	private Long idContratoResponsavelTecnicoSocial;
	
	private Long versao;
	
}