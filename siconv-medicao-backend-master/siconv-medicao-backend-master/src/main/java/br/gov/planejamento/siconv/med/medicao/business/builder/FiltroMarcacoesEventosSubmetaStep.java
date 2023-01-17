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
public class FiltroMarcacoesEventosSubmetaStep extends AbstractSubmetaMedicaoStep {

    @Inject
    public FiltroMarcacoesEventosSubmetaStep(SecurityContext securityContext) {
        super(securityContext);
    }

    @Override
    public void process(SubmetaMedicaoDTO submetaMedicao, Context builderContext) {

        MedicaoDTO medicao = builderContext.getMedicao();

        getEventos(submetaMedicao).forEach(evento -> {
            if (evento.getIdMedicaoEmpresa() != null && (evento.getIdMedicaoEmpresa() > medicao.getId()
                    || !permiteVisualizarDadosEmpresa(evento.getIdMedicaoEmpresa(), builderContext))) {
                evento.setIdMedicaoEmpresa(null);
            }

            if (evento.getIdMedicaoConvenente() != null && (evento.getIdMedicaoConvenente() > medicao.getId()
                    || !permiteVisualizarDadosConvenente(evento.getIdMedicaoConvenente(), builderContext))) {
                evento.setIdMedicaoConvenente(null);
            }

            if (evento.getIdMedicaoConcedente() != null && (evento.getIdMedicaoConcedente() > medicao.getId()
                    || !permiteVisualizarDadosConcedente(evento.getIdMedicaoConcedente(), builderContext))) {
                evento.setIdMedicaoConcedente(null);
            }
        });
    }
}
