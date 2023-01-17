package br.gov.planejamento.siconv.med.medicao.entity.database;

import java.time.Instant;

import org.jdbi.v3.core.mapper.reflect.ColumnName;

import br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum;
import br.gov.planejamento.siconv.med.medicao.entity.dto.PerfilEnum;
import lombok.Data;

@Data
public class HistoricoMedicaoBD {
	
	@ColumnName("id")
	private Long id;
		
	@ColumnName("med_contrato_fk")
	public Long idContratoMedicao;
	
	@ColumnName("nr_cpf_responsavel")
	private String nrCpfResponsavel;
	
	@ColumnName("in_perfil_responsavel")
	private PerfilEnum inPerfilResponsavel;
	
	@ColumnName("nr_sequencial")
	private Short nrSequencial;
	
	@ColumnName("in_situacao")
	private SituacaoMedicaoEnum situacao;
	
	@ColumnName("dt_registro")
	private Instant dataHora;

	
	public HistoricoMedicaoBD() {
		super();
	}

	
	public HistoricoMedicaoBD(Long idContratoMedicao, Short nrSequencial, SituacaoMedicaoEnum situacao) {
		super();
		this.idContratoMedicao = idContratoMedicao;
		this.nrSequencial = nrSequencial;
		this.situacao = situacao;
	}	
	
}
