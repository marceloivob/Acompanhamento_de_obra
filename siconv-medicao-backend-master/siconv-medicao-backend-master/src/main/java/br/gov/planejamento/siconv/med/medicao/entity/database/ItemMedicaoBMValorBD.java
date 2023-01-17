package br.gov.planejamento.siconv.med.medicao.entity.database;

import java.math.BigDecimal;

import org.jdbi.v3.core.mapper.reflect.ColumnName;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ItemMedicaoBMValorBD {

	@ColumnName("id")
	public Long idItemMedicaoBMValor;

	@ColumnName("med_item_medicao_bm_fk")
	public Long idItemMedicaoBM;

	@ColumnName("med_medicao_fk")
	public Long idMedicao;

	@ColumnName("qt_empresa")
	public BigDecimal qtEmpresa;

	@ColumnName("qt_convenente")
	public BigDecimal qtConvenente;

	@ColumnName("qt_concedente")
	public BigDecimal qtConcedente;

	public ItemMedicaoBMValorBD(Long idItemMedicaoBM, Long idMedicao) {
		super();
		this.idItemMedicaoBM = idItemMedicaoBM;
		this.idMedicao = idMedicao;
	}

	public boolean isPersistido() {
		return idItemMedicaoBMValor != null;
	}

	public boolean possuiQuantidadePreenchida() {
		return qtEmpresa != null || qtConvenente != null || qtConcedente != null;
	}
}
