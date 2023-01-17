package br.gov.planejamento.siconv.med.medicao.entity.database;

import java.math.BigDecimal;

import org.jdbi.v3.core.mapper.reflect.ColumnName;

import lombok.Data;

@Data
public class ItemMedicaoBMBD {
	
	@ColumnName("id")
	public Long idItemMedicaoBM;

	@ColumnName("med_contrato_fk")
	public Long idContratoMedicao;
	
	@ColumnName("vrpl_submeta_fk")
	public Long idSubmetaVrpl;
	
	@ColumnName("vrpl_frente_obra_fk")
	public Long idFrenteObraVrpl;
	
	@ColumnName("vrpl_servico_fk")
	public Long idServicoVrpl;

	@ColumnName("qt_total_servico")
	public BigDecimal qtTotalServico;
	
	@ColumnName("vl_preco_unitario_licitado")
	public BigDecimal vlPrecoUnitarioLicitado;

	public ItemMedicaoBMBD() {
		super();
	}

	public ItemMedicaoBMBD(Long idContratoMedicao, Long idSubmetaVrpl, 
			Long idFrenteObraVrpl, Long idServicoVrpl,  
			BigDecimal qtTotalServico, BigDecimal vlPrecoUnitarioLicitado) {
		super();
		this.idContratoMedicao = idContratoMedicao;
		this.idSubmetaVrpl = idSubmetaVrpl;
		this.idFrenteObraVrpl = idFrenteObraVrpl;
		this.idServicoVrpl = idServicoVrpl;
		this.qtTotalServico = qtTotalServico;
		this.vlPrecoUnitarioLicitado = vlPrecoUnitarioLicitado;
	}

}
