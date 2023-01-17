package br.gov.planejamento.siconv.med.medicao.entity.dto;

import static br.gov.planejamento.siconv.med.infra.util.MathUtil.calcularPercentual;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ValoresSubmetaDTO {

    private BigDecimal valorSubmeta;

    private BigDecimal valorRealizadoEmpresa;
    private BigDecimal valorRealizadoAcumuladoEmpresa;

    private BigDecimal valorRealizadoConvenente;
    private BigDecimal valorRealizadoAcumuladoConvenente;

    private BigDecimal valorRealizadoConcedente;
    private BigDecimal valorRealizadoAcumuladoConcedente;

    public BigDecimal getPercentualRealizadoEmpresa() {
        return calcularPercentual(valorRealizadoEmpresa, valorSubmeta);
    }

    public BigDecimal getPercentualRealizadoAcumuladoEmpresa() {
        return calcularPercentual(valorRealizadoAcumuladoEmpresa, valorSubmeta);
    }

    public BigDecimal getPercentualRealizadoConvenente() {
        return calcularPercentual(valorRealizadoConvenente, valorSubmeta);
    }

    public BigDecimal getPercentualRealizadoAcumuladoConvenente() {
        return calcularPercentual(valorRealizadoAcumuladoConvenente, valorSubmeta);
    }

    public BigDecimal getPercentualRealizadoConcedente() {
        return calcularPercentual(valorRealizadoConcedente, valorSubmeta);
    }

    public BigDecimal getPercentualRealizadoAcumuladoConcedente() {
        return calcularPercentual(valorRealizadoAcumuladoConcedente, valorSubmeta);
    }

}