package br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.database;

import java.time.LocalDate;

import org.jdbi.v3.core.mapper.reflect.ColumnName;

import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.dto.TipoResponsavelTecnicoEnum;
import lombok.Data;

@Data
public class AnotacaoRegistroRespTecnicoBD {

	@ColumnName("id") // tabela interna - med_anotacao_registro_rt
	private Long id;

	@ColumnName("nr_art_rrt") // tabela interna - med_anotacao_registro_rt
	private String numero;
	
	@ColumnName("dt_emissao") // tabela interna - med_anotacao_registro_rt
	private LocalDate dataEmissao;

	@ColumnName("in_tipo") // tabela interna - med_anotacao_registro_rt
	private TipoResponsavelTecnicoEnum tipo;
	
	@ColumnName("dt_inativacao") // tabela interna - med_anotacao_registro_rt
	private LocalDate dataInativacao;
	
	@ColumnName("nm_arquivo") // tabela interna - med_anotacao_registro_rt
	private String nmArquivo;
	
	@ColumnName("co_ceph") // tabela interna - med_anotacao_registro_rt
	private String coCeph;
	
	@ColumnName("med_contrato_resp_tecnico_fk") // tabela interna - med_contrato
	private Long idMedContratoRespTec;
	
	@ColumnName("versao") // tabela interna - med_anotacao_registro_rt
	private Long versao;
	
}
