package br.gov.planejamento.siconv.med.medicao.entity.database;

import java.math.BigDecimal;

import org.jdbi.v3.core.mapper.reflect.ColumnName;

import lombok.Data;

@Data
public class ItemMedicaoBD {
	@ColumnName("id")
	public Long idItemMedicao;

	@ColumnName("vrpl_submeta_fk")
	public Long idSubmetaVrpl;

	@ColumnName("vrpl_evento_fk")
	public Long idEventoVrpl;

	@ColumnName("vrpl_frente_obra_fk")
	public Long idFrenteObraVrpl;

	@ColumnName("medicao_fk_empresa")
	public Long idMedicaoEmpresa;

	@ColumnName("medicao_fk_convenente")
	public Long idMedicaoConvenente;

	@ColumnName("medicao_fk_concedente")
	public Long idMedicaoConcedente;

	@ColumnName("med_contrato_fk")
	public Long idContratoMedicao;

	@ColumnName("vl_total_servicos")
	public BigDecimal vlTotalServicos;

	public ItemMedicaoBD() {
		super();
	}

	public ItemMedicaoBD(Long idSubmetaVrpl, Long idEventoVrpl, Long idFrenteObraVrpl, Long idContratoMedicao,
			BigDecimal vlTotalServicos) {
		super();
		this.idSubmetaVrpl = idSubmetaVrpl;
		this.idEventoVrpl = idEventoVrpl;
		this.idFrenteObraVrpl = idFrenteObraVrpl;
		this.idContratoMedicao = idContratoMedicao;
		this.vlTotalServicos = vlTotalServicos;
	}
	
	

}
