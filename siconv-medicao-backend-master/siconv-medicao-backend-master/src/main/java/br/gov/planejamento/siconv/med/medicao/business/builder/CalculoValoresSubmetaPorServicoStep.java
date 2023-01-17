package br.gov.planejamento.siconv.med.medicao.business.builder;

import static br.gov.planejamento.siconv.med.infra.util.MathUtil.calcularPercentual;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import br.gov.planejamento.siconv.med.infra.security.SecurityContext;
import br.gov.planejamento.siconv.med.medicao.business.builder.SubmetaMedicaoBuilder.Context;
import br.gov.planejamento.siconv.med.medicao.entity.dto.MedicaoDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.ServicoVrplDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.SubmetaMedicaoDTO;
import io.quarkus.arc.Unremovable;

@Unremovable
@ApplicationScoped
public class CalculoValoresSubmetaPorServicoStep extends AbstractSubmetaMedicaoStep {

    @Inject
    public CalculoValoresSubmetaPorServicoStep(SecurityContext securityContext) {
        super(securityContext);
    }

    @Override
    public void process(SubmetaMedicaoDTO submetaMedicao, Context builderContext) {

        MedicaoDTO medicao = builderContext.getMedicao();

        preencherValoresEmpresaSubmetaPorServico(submetaMedicao, medicao, builderContext);
        preencherValoresConvenenteSubmetaPorServico(submetaMedicao, medicao, builderContext);
        preencherValoresConcedenteSubmetaPorServico(submetaMedicao, medicao, builderContext);
    }

    private void preencherValoresEmpresaSubmetaPorServico(SubmetaMedicaoDTO submetaMedicao, MedicaoDTO medicao,
            Context builderContext) {

        submetaMedicao.setValorRealizadoEmpresa(
                somarValoresServicosRealizado(submetaMedicao, ServicoVrplDTO::getValorRealizadoEmpresa));

        if (submetaMedicao.getValorRealizadoEmpresa() == null && submetaMedicao.getSituacaoEmpresa() != null) {
            submetaMedicao.setValorRealizadoEmpresa(BigDecimal.ZERO);
        }

        submetaMedicao.setValorRealizadoAcumuladoEmpresa(
                somarValoresAcumuladosServicos(submetaMedicao, ServicoVrplDTO::getValorAcumuladoEmpresa));

        if (submetaMedicao.getValorRealizadoAcumuladoEmpresa() == null
                && existeRegistroSubmetaEmpresa(submetaMedicao.getId(), medicao.getId(), builderContext)) {
            submetaMedicao.setValorRealizadoAcumuladoEmpresa(BigDecimal.ZERO);
        }

        submetaMedicao.setPercentualRealizadoEmpresa(
                calcularPercentual(submetaMedicao.getValorRealizadoEmpresa(), submetaMedicao.getValor()));

        submetaMedicao.setPercentualRealizadoAcumuladoEmpresa(
                calcularPercentual(submetaMedicao.getValorRealizadoAcumuladoEmpresa(), submetaMedicao.getValor()));
    }

    private void preencherValoresConvenenteSubmetaPorServico(SubmetaMedicaoDTO submetaMedicao, MedicaoDTO medicao,
            Context builderContext) {

        submetaMedicao.setValorRealizadoConvenente(
                somarValoresServicosRealizado(submetaMedicao, ServicoVrplDTO::getValorRealizadoConvenente));

        if (submetaMedicao.getValorRealizadoConvenente() == null && submetaMedicao.getSituacaoConvenente() != null) {
            submetaMedicao.setValorRealizadoConvenente(BigDecimal.ZERO);
        }

        submetaMedicao.setValorRealizadoAcumuladoConvenente(
                somarValoresAcumuladosServicos(submetaMedicao, ServicoVrplDTO::getValorAcumuladoConvenente));

        if (submetaMedicao.getValorRealizadoAcumuladoConvenente() == null
                && existeRegistroSubmetaConvenente(submetaMedicao.getId(), medicao.getId(), builderContext)) {
            submetaMedicao.setValorRealizadoAcumuladoConvenente(BigDecimal.ZERO);
        }

        submetaMedicao.setPercentualRealizadoConvenente(
                calcularPercentual(submetaMedicao.getValorRealizadoConvenente(), submetaMedicao.getValor()));

        submetaMedicao.setPercentualRealizadoAcumuladoConvenente(
                calcularPercentual(submetaMedicao.getValorRealizadoAcumuladoConvenente(), submetaMedicao.getValor()));

    }

    private void preencherValoresConcedenteSubmetaPorServico(SubmetaMedicaoDTO submetaMedicao, MedicaoDTO medicao,
            Context builderContext) {

        submetaMedicao.setValorRealizadoConcedente(
                somarValoresServicosRealizado(submetaMedicao, ServicoVrplDTO::getValorRealizadoConcedente));

        if (submetaMedicao.getValorRealizadoConcedente() == null && submetaMedicao.getSituacaoConcedente() != null) {
            submetaMedicao.setValorRealizadoConcedente(BigDecimal.ZERO);
        }

        submetaMedicao.setValorRealizadoAcumuladoConcedente(
                somarValoresAcumuladosServicos(submetaMedicao, ServicoVrplDTO::getValorAcumuladoConcedente));

        if (submetaMedicao.getValorRealizadoAcumuladoConcedente() == null
                && existeRegistroSubmetaConcedente(submetaMedicao.getId(), medicao.getId(), builderContext)) {
            submetaMedicao.setValorRealizadoAcumuladoConcedente(BigDecimal.ZERO);
        }

        submetaMedicao.setPercentualRealizadoConcedente(
                calcularPercentual(submetaMedicao.getValorRealizadoConcedente(), submetaMedicao.getValor()));

        submetaMedicao.setPercentualRealizadoAcumuladoConcedente(
                calcularPercentual(submetaMedicao.getValorRealizadoAcumuladoConcedente(), submetaMedicao.getValor()));
    }

    private BigDecimal somarValoresAcumuladosServicos(SubmetaMedicaoDTO submetaMedicao,
            Function<ServicoVrplDTO, BigDecimal> funcaoRetornoValorAcumulado) {

        return getServicos(submetaMedicao).map(funcaoRetornoValorAcumulado).filter(Objects::nonNull)
                .reduce(BigDecimal::add).orElse(null);

    }

    private BigDecimal somarValoresServicosRealizado(SubmetaMedicaoDTO submetaMedicao,
            Function<ServicoVrplDTO, BigDecimal> funcaoRetornoValorRealizado) {

        return getServicos(submetaMedicao).map(funcaoRetornoValorRealizado).filter(Objects::nonNull)
                .reduce(BigDecimal::add).orElse(null);
    }

}
