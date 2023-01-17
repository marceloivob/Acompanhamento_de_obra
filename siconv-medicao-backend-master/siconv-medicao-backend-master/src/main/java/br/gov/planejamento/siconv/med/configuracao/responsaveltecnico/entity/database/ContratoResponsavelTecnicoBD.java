package br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.database;

import java.time.LocalDateTime;

import org.jdbi.v3.core.mapper.reflect.ColumnName;

import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.dto.TipoResponsavelTecnicoEnum;
import lombok.Data;

@Data
public class ContratoResponsavelTecnicoBD {

	@ColumnName("id") // tabela interna - med_contrato_resp_tecnico
	private Long id;

	@ColumnName("med_contrato_fk") // tabela interna - med_contrato
	private Long contrato;

	@ColumnName("med_registro_profissional_fk") // tabela interna - med_registro_profissional
	private Long registro;

	@ColumnName("dt_inclusao") // tabela interna - med_contrato_resp_tecnico
	private LocalDateTime dataInclusao;

	@ColumnName("in_tipo") // // tabela interna - med_contrato_resp_tecnico
	private TipoResponsavelTecnicoEnum tipo;
	
	@ColumnName("versao") //tabela interna - med_contrato_resp_tecnico
	private Long versao;	

}
