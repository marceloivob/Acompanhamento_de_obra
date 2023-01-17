package br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.database;

import org.jdbi.v3.core.mapper.reflect.ColumnName;

import lombok.Data;
@Data
public class ResponsavelTecnicoBD {
	
	@ColumnName("id")// tabela interna - med_responsavel_tecnico
	private Long id;
	
	@ColumnName("nr_cpf") // tabela interna - med_responsavel_tecnico
	private  String cpf;
	
	@ColumnName("telefone") // tabela interna - med_responsavel_tecnico
	private  String telefone;
	
	@ColumnName("versao") //tabela interna - med_responsavel_tecnico
	private Long versao;	
	
}