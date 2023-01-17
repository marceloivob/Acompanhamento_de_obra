package br.gov.planejamento.siconv.med.acompanhamento.entity.dto;

import static br.gov.planejamento.siconv.med.infra.util.MathUtil.calcularPercentual;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class ListagemContratoLoteDTO {

	private TipoInstrumentoDTO tipoInstrumento;

	private List<ContratoLoteDTO> contratosLotes = new ArrayList<>();

	private BigDecimal valorTotalSubmetas;
	private BigDecimal valorTotalEmpresa;
	private BigDecimal valorTotalConvenente;
	private BigDecimal valorTotalConcedente;

	public BigDecimal getPercentualTotalEmpresa() {
		return calcularPercentual(valorTotalEmpresa, valorTotalSubmetas);
	}

	public BigDecimal getPercentualTotalConvenente() {
		return calcularPercentual(valorTotalConvenente, valorTotalSubmetas);
	}

	public BigDecimal getPercentualTotalConcedente() {
		return calcularPercentual(valorTotalConcedente, valorTotalSubmetas);
	}
}
