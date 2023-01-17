package br.gov.planejamento.siconv.med.medicao.entity.dto;

import java.time.LocalDate;

import org.jdbi.v3.core.mapper.reflect.ColumnName;

import br.gov.planejamento.siconv.med.medicao.entity.SolicitanteVistoriaExtraEnum;
import lombok.Data;

@Data
public class VistoriaExtraDTO {
	
	@ColumnName("in_vistoria_extra")
	private boolean vistoriaExtra;
	
	private LocalDate dataVistoriaExtra;
	
	@ColumnName("in_solicitante_vistoria")
	private SolicitanteVistoriaExtraEnum solicitanteVistoriaExtra;

	private Long versao;
}
