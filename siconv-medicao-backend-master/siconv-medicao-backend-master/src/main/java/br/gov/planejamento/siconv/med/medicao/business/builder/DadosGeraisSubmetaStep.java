package br.gov.planejamento.siconv.med.medicao.business.builder;

import static java.lang.Integer.parseInt;
import static org.apache.commons.lang3.StringUtils.split;

import java.math.RoundingMode;

import javax.enterprise.context.ApplicationScoped;

import br.gov.planejamento.siconv.med.medicao.business.builder.SubmetaMedicaoBuilder.Context;
import br.gov.planejamento.siconv.med.medicao.business.builder.SubmetaMedicaoBuilder.Step;
import br.gov.planejamento.siconv.med.medicao.entity.dto.SubmetaMedicaoDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.SubmetaVrplDTO;
import io.quarkus.arc.Unremovable;

@Unremovable
@ApplicationScoped
public class DadosGeraisSubmetaStep implements Step {

    @Override
    public void process(SubmetaMedicaoDTO submetaMedicao, Context builderContext) {

        SubmetaVrplDTO submetaContrato = builderContext.getCacheSubmetasContrato().get(submetaMedicao.getId());

        String nrSubmetaAnalise = submetaContrato.getNrSubmetaAnalise();
        submetaMedicao.setNrMeta(parseInt(split(nrSubmetaAnalise, '.')[0]));
        submetaMedicao.setNrSubmeta(parseInt(split(nrSubmetaAnalise, '.')[1]));
        
        submetaMedicao.setDescricao(nrSubmetaAnalise + " - " + submetaContrato.getDescricao());
        submetaMedicao.setValor(submetaContrato.getValor().setScale(2, RoundingMode.HALF_UP));

        // Atribui o Id do Contrato a Submeta associada.
        submetaMedicao.setIdContratoSiconv(builderContext.getContrato().getContratoFk());

    }
    
}
