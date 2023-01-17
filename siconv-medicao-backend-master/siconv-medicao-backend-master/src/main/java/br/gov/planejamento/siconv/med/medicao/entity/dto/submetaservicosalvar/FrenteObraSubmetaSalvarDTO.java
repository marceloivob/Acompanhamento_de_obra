package br.gov.planejamento.siconv.med.medicao.entity.dto.submetaservicosalvar;

import static java.util.Collections.emptyList;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class FrenteObraSubmetaSalvarDTO {

	@NotNull
	private Long id;

	@Valid
	private List<@NotNull EventoSubmetaSalvarDTO> eventos;

	@Valid
	private List<@NotNull ServicoSubmetaSalvarDTO> servicos;

	public List<ServicoSubmetaSalvarDTO> getServicos() {
		return servicos != null ? servicos : emptyList();
	}

	public List<EventoSubmetaSalvarDTO> getEventos() {
		return eventos != null ? eventos : emptyList();
	}
}
