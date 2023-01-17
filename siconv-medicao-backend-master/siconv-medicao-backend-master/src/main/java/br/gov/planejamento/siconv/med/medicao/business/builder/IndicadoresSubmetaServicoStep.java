package br.gov.planejamento.siconv.med.medicao.business.builder;

import static br.gov.planejamento.siconv.med.medicao.business.builder.util.ServicoPredicate.servicoPendentePreenchimentoConcedente;
import static br.gov.planejamento.siconv.med.medicao.business.builder.util.ServicoPredicate.servicoPendentePreenchimentoConvenente;
import static br.gov.planejamento.siconv.med.medicao.business.builder.util.ServicoPredicate.servicoPendentePreenchimentoEmpresa;
import static br.gov.planejamento.siconv.med.medicao.business.builder.util.ServicoPredicate.servicoPreenchidoConcedenteMedicaoAtual;
import static br.gov.planejamento.siconv.med.medicao.business.builder.util.ServicoPredicate.servicoPreenchidoConvenenteMedicaoAtual;
import static br.gov.planejamento.siconv.med.medicao.business.builder.util.ServicoPredicate.servicoPreenchidoEmpresaMedicaoAtual;
import static org.apache.commons.lang3.BooleanUtils.isNotFalse;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import br.gov.planejamento.siconv.med.infra.security.SecurityContext;
import br.gov.planejamento.siconv.med.medicao.business.builder.SubmetaMedicaoBuilder.Context;
import br.gov.planejamento.siconv.med.medicao.entity.dto.MedicaoDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.SubmetaMedicaoDTO;
import io.quarkus.arc.Unremovable;

@Unremovable
@ApplicationScoped
public class IndicadoresSubmetaServicoStep extends AbstractSubmetaMedicaoStep {

    @Inject
    public IndicadoresSubmetaServicoStep(SecurityContext securityContext) {
        super(securityContext);
    }

    @Override
    public void process(SubmetaMedicaoDTO submetaMedicao, Context builderContext) {

        MedicaoDTO medicao = builderContext.getMedicao();

        submetaMedicao.setPermiteMarcacaoEmpresa(permiteMarcacaoEmpresa(submetaMedicao, medicao));

        submetaMedicao.setPermiteMarcacaoConvenente(permiteMarcacaoConvenente(submetaMedicao, medicao, builderContext));

        submetaMedicao.setPermiteMarcacaoConcedente(permiteMarcacaoConcedente(submetaMedicao, medicao, builderContext));
    }

    private boolean permiteMarcacaoEmpresa(SubmetaMedicaoDTO submetaMedicao, MedicaoDTO medicao) {

        return medicao.getSituacao().permiteManutencaoEmpresa()
                && !medicao.isBloqueada()
                && isNotFalse(medicao.getPermiteComplementacaoValor())
                && ((!medicao.isAcumulada() && existeServicoPendenteOuPreenchidoEmpresa(submetaMedicao)) 
                    || (medicao.isAcumulada() && existeServicoPreenchidoEmpresa(submetaMedicao)));
    }

    private boolean permiteMarcacaoConvenente(SubmetaMedicaoDTO submetaMedicao, MedicaoDTO medicao,
            Context builderContext) {

        return medicao.getSituacao().permiteManutencaoConvenente()
                && !medicao.isBloqueada()
                && isNotFalse(medicao.getPermiteComplementacaoValor())
                && ((!medicao.isAcumulada() && (requerAssinaturaConvenente(submetaMedicao, builderContext)
                                                || existeServicoPendenteOuPreenchidoConvenente(submetaMedicao)))
                    || (medicao.isAcumulada() && existeServicoPreenchidoConvenente(submetaMedicao)));
    }

    private boolean permiteMarcacaoConcedente(SubmetaMedicaoDTO submetaMedicao, MedicaoDTO medicao,
            Context builderContext) {

        return medicao.getSituacao().permiteManutencaoConcedente()
                && !medicao.isBloqueada()
                && !medicao.isAcumulada()
                && (requerAssinaturaConcedente(submetaMedicao, builderContext)
                    || existeServicoPendenteOuPreenchidoConcedente(submetaMedicao));
    }

    private boolean existeServicoPreenchidoEmpresa(SubmetaMedicaoDTO submetaMedicao) {
        return getServicos(submetaMedicao).anyMatch(servicoPreenchidoEmpresaMedicaoAtual());
    }

    private boolean existeServicoPendenteOuPreenchidoEmpresa(SubmetaMedicaoDTO submetaMedicao) {
        return getServicos(submetaMedicao)
                .anyMatch(servicoPendentePreenchimentoEmpresa().or(servicoPreenchidoEmpresaMedicaoAtual()));
    }

    private boolean existeServicoPreenchidoConvenente(SubmetaMedicaoDTO submetaMedicao) {
        return getServicos(submetaMedicao).anyMatch(servicoPreenchidoConvenenteMedicaoAtual());
    }

    private boolean existeServicoPendenteOuPreenchidoConvenente(SubmetaMedicaoDTO submetaMedicao) {
        return getServicos(submetaMedicao)
                .anyMatch(servicoPendentePreenchimentoConvenente().or(servicoPreenchidoConvenenteMedicaoAtual()));
    }

    private boolean existeServicoPendenteOuPreenchidoConcedente(SubmetaMedicaoDTO submetaMedicao) {
        return getServicos(submetaMedicao)
                .anyMatch(servicoPendentePreenchimentoConcedente().or(servicoPreenchidoConcedenteMedicaoAtual()));
    }
}
