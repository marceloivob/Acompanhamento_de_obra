package br.gov.planejamento.siconv.med.configuracao.paralisacao.entity.database;

import java.time.LocalDate;

import br.gov.planejamento.siconv.med.configuracao.paralisacao.entity.dto.IndicativoParalisacaoEnum;
import br.gov.planejamento.siconv.med.configuracao.paralisacao.entity.dto.MotivoParalisacaoEnum;
import br.gov.planejamento.siconv.med.configuracao.paralisacao.entity.dto.ResponsavelParalisacaoEnum;
import lombok.Data;

@Data
public class ParalisacaoBD {

	private Long id;

	private Long medContratoFk;

	private LocalDate dtInicio;

	private LocalDate dtFim;

	private String txObservacao;

	private ResponsavelParalisacaoEnum inResponsavel;

	private IndicativoParalisacaoEnum inIndicativo;

	private MotivoParalisacaoEnum inMotivo;
	
	private Long versao;
}
