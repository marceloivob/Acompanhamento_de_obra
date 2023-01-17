package br.gov.planejamento.siconv.med.medicao.entity.database;

import java.time.LocalDate;

import org.jdbi.v3.core.mapper.reflect.ColumnName;

import br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum;
import br.gov.planejamento.siconv.med.medicao.entity.SolicitanteVistoriaExtraEnum;
import lombok.Data;

@Data
public class MedicaoBD {
	@ColumnName("id")
	public Long id;

	@ColumnName("nr_sequencial")
	public Short nrSequencial;

	@ColumnName("dt_inicio")
	public LocalDate dtInicio;

	@ColumnName("dt_fim")
	public LocalDate dtFim;
	
	@ColumnName("in_situacao")
	public SituacaoMedicaoEnum situacao;

	@ColumnName("med_contrato_fk")
	public Long idContratoMedicao;

	@ColumnName("medicao_fk_agrupadora")
	public Long idMedicaoAgrupadora;

	@ColumnName("in_bloqueio")
	private boolean bloqueada;
	
	@ColumnName("versao")
	private Long versao;
	
	@ColumnName("in_vistoria_extra")
	public boolean vistoriaExtra;

	@ColumnName("dt_vistoria_extra")
	public LocalDate dataVistoriaExtra;

	@ColumnName("in_solicitante_vistoria")
	private SolicitanteVistoriaExtraEnum solicitanteVistoriaExtra;

	@ColumnName("in_complementacao_valor")
	private Boolean permiteComplementacaoValor;
	
	public MedicaoBD(Short nrSequencial, LocalDate dtInicio, LocalDate dtFim, SituacaoMedicaoEnum situacao,
			Long idContratoMedicao) {
		super();
		this.nrSequencial = nrSequencial;
		this.dtInicio = dtInicio;
		this.dtFim = dtFim;
		this.situacao = situacao;
		this.idContratoMedicao = idContratoMedicao;
	}

	public MedicaoBD() {
		super();
	}
}
