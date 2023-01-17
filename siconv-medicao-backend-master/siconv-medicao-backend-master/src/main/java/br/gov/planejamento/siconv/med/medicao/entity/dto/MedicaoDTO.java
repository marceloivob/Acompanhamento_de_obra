package br.gov.planejamento.siconv.med.medicao.entity.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.jdbi.v3.core.mapper.reflect.ColumnName;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum;
import br.gov.planejamento.siconv.med.medicao.entity.SolicitanteVistoriaExtraEnum;
import lombok.Data;

@Data
public class MedicaoDTO {

    private Long id;
    private Short sequencial;
    private LocalDate dataInicio;
    private LocalDate dataFim;

    // Empresa
    private BigDecimal valorRealizadoEmpresa;
    private BigDecimal percentualRealizadoEmpresa;
    private BigDecimal valorRealizadoAcumuladoEmpresa;
    private BigDecimal percentualRealizadoAcumuladoEmpresa;

    // Convenente
    private BigDecimal valorRealizadoConvenente;
    private BigDecimal percentualRealizadoConvenente;
    private BigDecimal valorRealizadoAcumuladoConvenente;
    private BigDecimal percentualRealizadoAcumuladoConvenente;

    private BigDecimal valorRealizadoConcedente;
    private BigDecimal percentualRealizadoConcedente;
    private BigDecimal valorRealizadoAcumuladoConcedente;
    private BigDecimal percentualRealizadoAcumuladoConcedente;

    @ColumnName("in_situacao")
    private SituacaoMedicaoEnum situacao;

    @ColumnName("in_bloqueio")
    private boolean bloqueada;

    private LocalDate dataInicioObra;
    private Long idContrato;

    private Long versao;

    private boolean possuiSubmetaAssinada;
    private boolean permiteCancelarEnvio;
    private boolean permiteCancelarEnvioParaComplementacao;
    private boolean permiteIniciarAteste;
    private boolean permiteCancelarAceite;
    private boolean permiteExcluir;

    @JsonInclude(value = Include.NON_NULL)
    private Long idContratoSiconv;

    private Long idMedicaoAgrupadora;

    @JsonInclude(value = Include.NON_NULL)
    private Short sequencialMedicaoAgrupadora;

    @JsonInclude(value = Include.NON_NULL)
    private Boolean isMedicaoAgrupadora;

    @JsonIgnore
    @JsonProperty(access = Access.READ_ONLY)
    public boolean isAcumulada() {
        return idMedicaoAgrupadora != null;
    }

    @ColumnName("in_vistoria_extra")
    private boolean vistoriaExtra;

    private LocalDate dataVistoriaExtra;

    @ColumnName("in_solicitante_vistoria")
    private SolicitanteVistoriaExtraEnum solicitanteVistoriaExtra;

    @ColumnName("in_complementacao_valor")
    private Boolean permiteComplementacaoValor;

    public void setValoresTotalSubmetas(ValoresSubmetaDTO totalSubmetas) {

        // Empresa
        setValorRealizadoEmpresa(totalSubmetas.getValorRealizadoEmpresa());
        setValorRealizadoAcumuladoEmpresa(totalSubmetas.getValorRealizadoAcumuladoEmpresa());
        setPercentualRealizadoEmpresa(totalSubmetas.getPercentualRealizadoEmpresa());
        setPercentualRealizadoAcumuladoEmpresa(totalSubmetas.getPercentualRealizadoAcumuladoEmpresa());

        // Convenente
        setValorRealizadoConvenente(totalSubmetas.getValorRealizadoConvenente());
        setValorRealizadoAcumuladoConvenente(totalSubmetas.getValorRealizadoAcumuladoConvenente());
        setPercentualRealizadoConvenente(totalSubmetas.getPercentualRealizadoConvenente());
        setPercentualRealizadoAcumuladoConvenente(totalSubmetas.getPercentualRealizadoAcumuladoConvenente());

        // Concedente
        setValorRealizadoConcedente(totalSubmetas.getValorRealizadoConcedente());
        setValorRealizadoAcumuladoConcedente(totalSubmetas.getValorRealizadoAcumuladoConcedente());
        setPercentualRealizadoConcedente(totalSubmetas.getPercentualRealizadoConcedente());
        setPercentualRealizadoAcumuladoConcedente(totalSubmetas.getPercentualRealizadoAcumuladoConcedente());
    }
}
