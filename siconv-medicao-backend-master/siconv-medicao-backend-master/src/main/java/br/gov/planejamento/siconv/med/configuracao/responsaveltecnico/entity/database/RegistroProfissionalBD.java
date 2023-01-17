package br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.database;

import org.jdbi.v3.core.mapper.reflect.ColumnName;

import lombok.Data;

@Data
public class RegistroProfissionalBD {

	@ColumnName("id") // tabela interna - med_registro_profissional
	private Long id;

	@ColumnName("atividade") // tabela interna - med_registro_profissional
	private String atividade;

	@ColumnName("crea_cau") // tabela interna - med_registro_profissional
	private String nrCreaCau;

	@ColumnName("uf") // tabela interna - med_registro_profissional
	private String uf;
	
	@ColumnName("versao") //tabela interna - med_registro_profissional
	private Long versao;

	@ColumnName("med_responsavel_tecnico_fk")
	private Long responsavelTecnicoFk;

}
