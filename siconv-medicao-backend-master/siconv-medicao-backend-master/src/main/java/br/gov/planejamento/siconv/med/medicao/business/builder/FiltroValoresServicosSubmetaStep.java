package br.gov.planejamento.siconv.med.medicao.business.builder;

import java.util.Map;
import java.util.Map.Entry;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import br.gov.planejamento.siconv.med.infra.security.SecurityContext;
import br.gov.planejamento.siconv.med.medicao.business.builder.SubmetaMedicaoBuilder.Context;
import br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum;
import br.gov.planejamento.siconv.med.medicao.entity.dto.MedicaoDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.ServicoVrplDTO.ValorServicoBM;
import br.gov.planejamento.siconv.med.medicao.entity.dto.SubmetaMedicaoDTO;
import io.quarkus.arc.Unremovable;

@Unremovable
@ApplicationScoped
public class FiltroValoresServicosSubmetaStep extends AbstractSubmetaMedicaoStep {

    @Inject
    public FiltroValoresServicosSubmetaStep(SecurityContext securityContext) {
        super(securityContext);
    }

    @Override
    public void process(SubmetaMedicaoDTO submetaMedicao, Context builderContext) {

        MedicaoDTO medicao = builderContext.getMedicao();

        Map<Long, SituacaoMedicaoEnum> cacheSituacaoMedicao = builderContext.getCacheSituacaoMedicao();

        super.getServicos(submetaMedicao).forEach(servico -> {
            // Remove a medição posterior em elaboração ou em ateste, se existir.
            servico.getValoresPorIdMedicao().keySet().removeIf(idMedicao -> idMedicao > medicao.getId()
                    && (cacheSituacaoMedicao.get(idMedicao) == SituacaoMedicaoEnum.EM 
                     || cacheSituacaoMedicao.get(idMedicao) == SituacaoMedicaoEnum.AT) );

            // Verifica para cada serviço se os dados do Serviço podem ser visíveis pelos
            // atores.
            for (Entry<Long, ValorServicoBM> mbValores : servico.getValoresPorIdMedicao().entrySet()) {
                filtrarValoresEmpresa(builderContext, mbValores);
                filtrarValoresConvenente(builderContext, mbValores);
                filtrarValoresConcedente(builderContext, mbValores);
            }
        });
    }

    private void filtrarValoresEmpresa(Context builderContext, Entry<Long, ValorServicoBM> mbValores) {
        if (mbValores.getValue().getQtdEmpresa() != null
                && !permiteVisualizarDadosEmpresa(mbValores.getKey(), builderContext)) {
            mbValores.getValue().setQtdEmpresa(null);
        }
    }

    private void filtrarValoresConvenente(Context builderContext, Entry<Long, ValorServicoBM> mbValores) {
        if (mbValores.getValue().getQtdConvenente() != null
                && !permiteVisualizarDadosConvenente(mbValores.getKey(), builderContext)) {
            mbValores.getValue().setQtdConvenente(null);
        }
    }

    private void filtrarValoresConcedente(Context builderContext, Entry<Long, ValorServicoBM> mbValores) {
        if (mbValores.getValue().getQtdConcedente() != null
                && !permiteVisualizarDadosConcedente(mbValores.getKey(), builderContext)) {
            mbValores.getValue().setQtdConcedente(null);
        }
    }
}
