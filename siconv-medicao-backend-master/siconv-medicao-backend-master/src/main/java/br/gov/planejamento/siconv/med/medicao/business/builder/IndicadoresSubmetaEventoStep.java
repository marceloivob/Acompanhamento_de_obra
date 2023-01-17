package br.gov.planejamento.siconv.med.medicao.business.builder;

import static br.gov.planejamento.siconv.med.medicao.business.builder.util.EventoPredicate.eventoConcluidoConcedente;
import static br.gov.planejamento.siconv.med.medicao.business.builder.util.EventoPredicate.eventoConcluidoConcedenteMedicaoAtual;
import static br.gov.planejamento.siconv.med.medicao.business.builder.util.EventoPredicate.eventoConcluidoConvenente;
import static br.gov.planejamento.siconv.med.medicao.business.builder.util.EventoPredicate.eventoConcluidoConvenenteMedicaoAcumulada;
import static br.gov.planejamento.siconv.med.medicao.business.builder.util.EventoPredicate.eventoConcluidoConvenenteMedicaoAtual;
import static br.gov.planejamento.siconv.med.medicao.business.builder.util.EventoPredicate.eventoConcluidoEmpresa;
import static br.gov.planejamento.siconv.med.medicao.business.builder.util.EventoPredicate.eventoConcluidoEmpresaMedicaoAcumulada;
import static br.gov.planejamento.siconv.med.medicao.business.builder.util.EventoPredicate.eventoConcluidoEmpresaMedicaoAtual;
import static java.util.function.Predicate.not;
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
public class IndicadoresSubmetaEventoStep extends AbstractSubmetaMedicaoStep {

    @Inject
    public IndicadoresSubmetaEventoStep(SecurityContext securityContext) {
        super(securityContext);
    }

    @Override
    public void process(SubmetaMedicaoDTO submetaMedicao, Context builderContext) {

        MedicaoDTO medicao = builderContext.getMedicao();

        submetaMedicao.setPermiteMarcacaoEmpresa(permiteMarcacaoEmpresa(submetaMedicao, medicao, builderContext));

        submetaMedicao.setPermiteMarcacaoConvenente(permiteMarcacaoConvenente(submetaMedicao, medicao, builderContext));

        submetaMedicao.setPermiteMarcacaoConcedente(permiteMarcacaoConcedente(submetaMedicao, medicao, builderContext));
    }

    private boolean permiteMarcacaoEmpresa(SubmetaMedicaoDTO submetaMedicao, MedicaoDTO medicao,
            Context builderContext) {

        return medicao.getSituacao().permiteManutencaoEmpresa()
                && !medicao.isAcumulada() 
                && !medicao.isBloqueada()
                && isNotFalse(medicao.getPermiteComplementacaoValor())
                && getEventos(submetaMedicao).anyMatch(
                        not(eventoConcluidoEmpresa(medicao))
                        .or(eventoConcluidoEmpresaMedicaoAtual(medicao)
                        .or(eventoConcluidoEmpresaMedicaoAcumulada(builderContext.getCacheIdMedicoesAcumuladas()))));
    }

    private boolean permiteMarcacaoConvenente(SubmetaMedicaoDTO submetaMedicao, MedicaoDTO medicao,
            Context builderContext) {

        return medicao.getSituacao().permiteManutencaoConvenente()
                && !medicao.isAcumulada()
                && !medicao.isBloqueada()
                && isNotFalse(medicao.getPermiteComplementacaoValor())
                && (requerAssinaturaConvenente(submetaMedicao, builderContext)
                        || getEventos(submetaMedicao).anyMatch(
                                eventoConcluidoEmpresa(medicao).and(
                                        not(eventoConcluidoConvenente(medicao))
                                        .or(eventoConcluidoConvenenteMedicaoAtual(medicao))
                                        .or(eventoConcluidoConvenenteMedicaoAcumulada(builderContext.getCacheIdMedicoesAcumuladas())))));
    }

    private boolean permiteMarcacaoConcedente(SubmetaMedicaoDTO submetaMedicao, MedicaoDTO medicao,
            Context builderContext) {

        return medicao.getSituacao().permiteManutencaoConcedente()
                && !medicao.isAcumulada()
                && !medicao.isBloqueada()
                && (requerAssinaturaConcedente(submetaMedicao, builderContext)
                        || getEventos(submetaMedicao).anyMatch(
                                eventoConcluidoConvenente(medicao).and(
                                        not(eventoConcluidoConcedente(medicao))
                                        .or(eventoConcluidoConcedenteMedicaoAtual(medicao)))));
    }
}
