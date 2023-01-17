package br.gov.planejamento.siconv.med.medicao.business.builder;

import static br.gov.planejamento.siconv.med.infra.util.MathUtil.calcularPercentual;
import static br.gov.planejamento.siconv.med.medicao.business.builder.util.EventoPredicate.eventoConcluidoConcedente;
import static br.gov.planejamento.siconv.med.medicao.business.builder.util.EventoPredicate.eventoConcluidoConcedenteMedicaoAtual;
import static br.gov.planejamento.siconv.med.medicao.business.builder.util.EventoPredicate.eventoConcluidoConvenente;
import static br.gov.planejamento.siconv.med.medicao.business.builder.util.EventoPredicate.eventoConcluidoConvenenteMedicaoAtual;
import static br.gov.planejamento.siconv.med.medicao.business.builder.util.EventoPredicate.eventoConcluidoEmpresa;
import static br.gov.planejamento.siconv.med.medicao.business.builder.util.EventoPredicate.eventoConcluidoEmpresaMedicaoAtual;

import java.math.BigDecimal;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import br.gov.planejamento.siconv.med.infra.security.SecurityContext;
import br.gov.planejamento.siconv.med.medicao.business.builder.SubmetaMedicaoBuilder.Context;
import br.gov.planejamento.siconv.med.medicao.entity.dto.MedicaoDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.SubmetaMedicaoDTO;
import io.quarkus.arc.Unremovable;

@Unremovable
@ApplicationScoped
public class CalculoValoresSubmetaPorEventoStep extends AbstractSubmetaMedicaoStep {

    @Inject
    public CalculoValoresSubmetaPorEventoStep(SecurityContext securityContext) {
        super(securityContext);
    }

    @Override
    public void process(SubmetaMedicaoDTO submetaMedicao, Context builderContext) {

        MedicaoDTO medicao = builderContext.getMedicao();

        preencherValoresEmpresaSubmetaPorEvento(submetaMedicao, medicao, builderContext);
        preencherValoresConvenenteSubmetaPorEvento(submetaMedicao, medicao, builderContext);
        preencherValoresConcedenteSubmetaPorEvento(submetaMedicao, medicao, builderContext);
    }

    private void preencherValoresEmpresaSubmetaPorEvento(SubmetaMedicaoDTO submetaMedicao, MedicaoDTO medicao,
            Context builderContext) {

        submetaMedicao.setValorRealizadoEmpresa(
                somarValoresEventos(submetaMedicao, eventoConcluidoEmpresaMedicaoAtual(medicao)));

        if (submetaMedicao.getValorRealizadoEmpresa() == null && submetaMedicao.getSituacaoEmpresa() != null) {
            submetaMedicao.setValorRealizadoEmpresa(BigDecimal.ZERO);
        }

        submetaMedicao.setValorRealizadoAcumuladoEmpresa(
                somarValoresEventos(submetaMedicao, eventoConcluidoEmpresa(medicao)));

        if (submetaMedicao.getValorRealizadoAcumuladoEmpresa() == null
                && existeRegistroSubmetaEmpresa(submetaMedicao.getId(), medicao.getId(), builderContext)) {
            submetaMedicao.setValorRealizadoAcumuladoEmpresa(BigDecimal.ZERO);
        }

        submetaMedicao.setPercentualRealizadoEmpresa(
                calcularPercentual(submetaMedicao.getValorRealizadoEmpresa(), submetaMedicao.getValor()));

        submetaMedicao.setPercentualRealizadoAcumuladoEmpresa(
                calcularPercentual(submetaMedicao.getValorRealizadoAcumuladoEmpresa(), submetaMedicao.getValor()));
    }

    private void preencherValoresConvenenteSubmetaPorEvento(SubmetaMedicaoDTO submetaMedicao, MedicaoDTO medicao,
            Context builderContext) {

        submetaMedicao.setValorRealizadoConvenente(
                somarValoresEventos(submetaMedicao, eventoConcluidoConvenenteMedicaoAtual(medicao)));

        if (submetaMedicao.getValorRealizadoConvenente() == null && submetaMedicao.getSituacaoConvenente() != null) {
            submetaMedicao.setValorRealizadoConvenente(BigDecimal.ZERO);
        }

        submetaMedicao.setValorRealizadoAcumuladoConvenente(
                somarValoresEventos(submetaMedicao, eventoConcluidoConvenente(medicao)));

        if (submetaMedicao.getValorRealizadoAcumuladoConvenente() == null
                && existeRegistroSubmetaConvenente(submetaMedicao.getId(), medicao.getId(), builderContext)) {
            submetaMedicao.setValorRealizadoAcumuladoConvenente(BigDecimal.ZERO);
        }

        submetaMedicao.setPercentualRealizadoConvenente(
                calcularPercentual(submetaMedicao.getValorRealizadoConvenente(), submetaMedicao.getValor()));

        submetaMedicao.setPercentualRealizadoAcumuladoConvenente(
                calcularPercentual(submetaMedicao.getValorRealizadoAcumuladoConvenente(), submetaMedicao.getValor()));
    }

    private void preencherValoresConcedenteSubmetaPorEvento(SubmetaMedicaoDTO submetaMedicao, MedicaoDTO medicao,
            Context builderContext) {

        submetaMedicao.setValorRealizadoConcedente(
                somarValoresEventos(submetaMedicao, eventoConcluidoConcedenteMedicaoAtual(medicao)));

        if (submetaMedicao.getValorRealizadoConcedente() == null && submetaMedicao.getSituacaoConcedente() != null) {
            submetaMedicao.setValorRealizadoConcedente(BigDecimal.ZERO);
        }

        submetaMedicao.setValorRealizadoAcumuladoConcedente(
                somarValoresEventos(submetaMedicao, eventoConcluidoConcedente(medicao)));

        if (submetaMedicao.getValorRealizadoAcumuladoConcedente() == null
                && existeRegistroSubmetaConcedente(submetaMedicao.getId(), medicao.getId(), builderContext)) {
            submetaMedicao.setValorRealizadoAcumuladoConcedente(BigDecimal.ZERO);
        }

        submetaMedicao.setPercentualRealizadoConcedente(
                calcularPercentual(submetaMedicao.getValorRealizadoConcedente(), submetaMedicao.getValor()));

        submetaMedicao.setPercentualRealizadoAcumuladoConcedente(
                calcularPercentual(submetaMedicao.getValorRealizadoAcumuladoConcedente(), submetaMedicao.getValor()));
    }

}
