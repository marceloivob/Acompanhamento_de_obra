package br.gov.planejamento.siconv.med.medicao.business.builder;

import javax.enterprise.context.ApplicationScoped;

import br.gov.planejamento.siconv.med.medicao.business.builder.SubmetaMedicaoBuilder.Context;
import br.gov.planejamento.siconv.med.medicao.business.builder.SubmetaMedicaoBuilder.Step;
import br.gov.planejamento.siconv.med.medicao.entity.dto.SubmetaMedicaoDTO;
import io.quarkus.arc.Unremovable;

@Unremovable
@ApplicationScoped
public class FiltroCamposDesnecessariosListagemSubmetaStep implements Step {

    @Override
    public void process(SubmetaMedicaoDTO submetaMedicao, Context builderContext) {
        submetaMedicao.setFrentesObra(null);
        submetaMedicao.setAssinaturas(null);
    }
}
