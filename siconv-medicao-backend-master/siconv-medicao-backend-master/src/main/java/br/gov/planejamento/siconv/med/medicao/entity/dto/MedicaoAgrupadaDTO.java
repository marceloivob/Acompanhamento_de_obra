package br.gov.planejamento.siconv.med.medicao.entity.dto;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import br.gov.planejamento.siconv.med.medicao.entity.database.MedicaoBD;
import lombok.Data;

@Data
public class MedicaoAgrupadaDTO {

	private Short sequencial;
	private Long id;
	private LocalDate dataInicio;
	private LocalDate dataFim;

	@JsonInclude(value = Include.NON_NULL)
	private List<SubmetaMedicaoDTO> listaSubmetasPreenchidas;

	public MedicaoAgrupadaDTO(MedicaoBD medicao) {
		super();
		this.id = medicao.getId();
		this.sequencial = medicao.getNrSequencial();
		this.dataInicio = medicao.getDtInicio();
		this.dataFim = medicao.getDtFim();
	}
}