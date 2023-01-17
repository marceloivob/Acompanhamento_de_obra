package br.gov.planejamento.siconv.med.medicao.entity;

import static org.apache.commons.lang3.StringUtils.upperCase;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@RequiredArgsConstructor
public enum SituacaoMedicaoEnum {

    /**
     * EM - Em Elaboração
     */
    EM("Em Elaboração"),

    /**
     * EC - Enviada para o Convenente
     */
    EC("Enviada para o Convenente"),

    /**
     * EXC - Excluída
     */
    EXC("Excluída"),

    /**
     * AT - Em Ateste pelo Convenente
     */
    AT("Em Ateste pelo Convenente"),

    /**
     * ATD - Atestada
     */
    ATD("Atestada"),

    /**
     * ECE - Enviada para Complementação da Empresa
     */
    ECE("Enviada para Complementação da Empresa"),

    /**
     * CE - Em Complementação pela Empresa
     */
    CE("Em Complementação pela Empresa"),

    /**
     * AC - Em Análise pelo Concedente/Mandatária
     */
    AC("Em Análise pelo Concedente/Mandatária"),

    /**
     * ACT - Aceita
     */
    ACT("Aceita"),

    /**
     * ECC - Enviada para Complementação do Convenente
     */
    ECC("Enviada para Complementação do Convenente"),

    /**
     * CC - Em Complementação pelo Convenente
     */
    CC("Em Complementação pelo Convenente");

    @Getter
    private final String descricao;

    @JsonProperty
    public String getCodigo() {
        return name();
    }

    @JsonCreator
    public static SituacaoMedicaoEnum fromCodigo(@JsonProperty("codigo") final String codigo) {
        return valueOf(upperCase(codigo));
    }

    public boolean permiteManutencaoEmpresa() {
        return this == SituacaoMedicaoEnum.EM || this == SituacaoMedicaoEnum.CE;
    }

    public boolean permiteManutencaoConvenente() {
        return this == SituacaoMedicaoEnum.AT || this == SituacaoMedicaoEnum.CC;
    }

    public boolean permitePublicacaoEmpresa() {
        return this != SituacaoMedicaoEnum.EM && this != SituacaoMedicaoEnum.CE;
    }

    public boolean permitePublicacaoConvenente() {
        return this != SituacaoMedicaoEnum.AT && this != SituacaoMedicaoEnum.CC;
    }

    public boolean permiteManutencaoConcedente() {
        return this == SituacaoMedicaoEnum.AC;
    }

    public boolean permitePublicacaoConcedente() {
        return this == SituacaoMedicaoEnum.ACT;
    }

}
