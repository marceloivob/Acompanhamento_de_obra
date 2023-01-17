package br.gov.planejamento.siconv.med.medicao.business.builder;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import br.gov.planejamento.siconv.med.infra.security.SecurityContext;
import br.gov.planejamento.siconv.med.medicao.business.builder.SubmetaMedicaoBuilder.Context;
import br.gov.planejamento.siconv.med.medicao.entity.dto.MedicaoDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.SubmetaMedicaoDTO;
import io.quarkus.arc.Unremovable;

@Unremovable
@ApplicationScoped
public class FiltroSituacoesSubmetaStep extends AbstractSubmetaMedicaoStep {

    @Inject
    public FiltroSituacoesSubmetaStep(SecurityContext securityContext) {
        super(securityContext);
    }

    @Override
    public void process(SubmetaMedicaoDTO submetaMedicao, Context builderContext) {

        MedicaoDTO medicao = builderContext.getMedicao();

        if (submetaMedicao.getSituacaoEmpresa() != null
                && !permiteVisualizarDadosEmpresa(medicao.getId(), builderContext)) {
            submetaMedicao.setSituacaoEmpresa(null);
        }

        if (submetaMedicao.getSituacaoConvenente() != null
                && !permiteVisualizarDadosConvenente(medicao.getId(), builderContext)) {
            submetaMedicao.setSituacaoConvenente(null);
        }

        if (submetaMedicao.getSituacaoConcedente() != null
                && !permiteVisualizarDadosConcedente(medicao.getId(), builderContext)) {
            submetaMedicao.setSituacaoConcedente(null);
        }
    }
}
