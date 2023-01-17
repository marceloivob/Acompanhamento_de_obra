package br.gov.planejamento.siconv.med.acompanhamento.entity.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
public class ContratoLoteDTO {

	private Long id;
	private Tipo tipo;
	private String numero;
	private boolean aptoIniciar;
	private boolean acompEventos;
	private Boolean configuradoMedicao;
	private Short numeroUltimaMedicao;
	private Long qtdeDiasSemMedicao;
	private Boolean atrasado;
	private Boolean paralisado;

	private List<SubmetaContratoLoteDTO> submetas = new ArrayList<>();

	@RequiredArgsConstructor
	public enum Tipo {

		CONTRATO("C"), LOTE("L");

		@JsonValue
		private final String sigla;
	}
}
