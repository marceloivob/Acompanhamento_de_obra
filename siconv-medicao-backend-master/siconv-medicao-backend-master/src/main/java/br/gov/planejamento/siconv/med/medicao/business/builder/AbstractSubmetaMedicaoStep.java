package br.gov.planejamento.siconv.med.medicao.business.builder;

import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.CONCEDENTE;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.EMPRESA;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.PROPONENTE_CONVENENTE;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoSubmetaEnum.ASS;
import static org.apache.commons.lang3.BooleanUtils.isFalse;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import br.gov.planejamento.siconv.med.infra.exception.MedicaoRestException;
import br.gov.planejamento.siconv.med.infra.message.MessageKey;
import br.gov.planejamento.siconv.med.infra.security.SecurityContext;
import br.gov.planejamento.siconv.med.infra.security.domain.Profile;
import br.gov.planejamento.siconv.med.medicao.business.builder.SubmetaMedicaoBuilder.Context;
import br.gov.planejamento.siconv.med.medicao.business.builder.SubmetaMedicaoBuilder.Step;
import br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum;
import br.gov.planejamento.siconv.med.medicao.entity.database.SubmetaMedicaoBD;
import br.gov.planejamento.siconv.med.medicao.entity.dto.EventoVrplDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.MedicaoDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.ServicoVrplDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.SubmetaMedicaoDTO;

public abstract class AbstractSubmetaMedicaoStep implements Step {

    protected SecurityContext securityContext;

    protected AbstractSubmetaMedicaoStep() {
        super();
    }

    protected AbstractSubmetaMedicaoStep(SecurityContext securityContext) {
        this.securityContext = securityContext;
    }

    protected boolean permiteVisualizarDadosConvenente(Long idMedicao, Context builderContext) {

        return securityContext.hasAnyRoleInProfile(PROPONENTE_CONVENENTE)
                || builderContext.getCacheSituacaoMedicao().get(idMedicao).permitePublicacaoConvenente()
                || isFalse(builderContext.getCacheIndComplementacaoValor().get(idMedicao));
    }

    protected boolean permiteVisualizarDadosEmpresa(Long idMedicao, Context builderContext) {

        return securityContext.hasAnyPermissionInProfile(EMPRESA)
                || builderContext.getCacheSituacaoMedicao().get(idMedicao).permitePublicacaoEmpresa()
                || isFalse(builderContext.getCacheIndComplementacaoValor().get(idMedicao));
    }

    protected boolean permiteVisualizarDadosConcedente(Long idMedicao, Context builderContext) {

        return securityContext.hasAnyRoleInProfile(CONCEDENTE)
                || securityContext.hasAnyRoleInProfile(Profile.MANDATARIA)
                || builderContext.getCacheSituacaoMedicao().get(idMedicao).permitePublicacaoConcedente();
    }

    protected Stream<EventoVrplDTO> getEventos(SubmetaMedicaoDTO submetaMedicao) {

        return submetaMedicao.getFrentesObra().stream().flatMap(fo -> fo.getEventos().stream());
    }

    protected BigDecimal somarValoresEventos(SubmetaMedicaoDTO submetaMedicao, Predicate<EventoVrplDTO> filtroEventos) {

        Stream<EventoVrplDTO> eventos = getEventos(submetaMedicao);

        return eventos.filter(filtroEventos).map(EventoVrplDTO::getValor).reduce(BigDecimal::add).orElse(null);
    }

    protected Stream<ServicoVrplDTO> getServicos(SubmetaMedicaoDTO submetaMedicao) {

        return submetaMedicao.getFrentesObra().stream().flatMap(fo -> fo.getServicos().stream());
    }

    protected Stream<ServicoVrplDTO> getServicosfromMacroServico(SubmetaMedicaoDTO submetaMedicao) {

        validaCarregamentoMacroServico(submetaMedicao);

        return submetaMedicao.getFrentesObra().stream().flatMap(fo -> fo.getMacroServicosView().stream())
                .flatMap(macroServico -> macroServico.getServicos().stream());
    }

    private void validaCarregamentoMacroServico(SubmetaMedicaoDTO submetaMedicao) {

        if (!submetaMedicao.getFrentesObra().stream().flatMap(fo -> fo.getServicos().stream())
                .collect(Collectors.toList()).isEmpty()
                && submetaMedicao.getFrentesObra().stream().flatMap(fo -> fo.getMacroServicosView().stream())
                        .collect(Collectors.toList()).isEmpty()) {

            throw new MedicaoRestException(MessageKey.ERRO_MACRO_SERVICO_NAO_CARREGADO);
        }

    }

    protected boolean existeRegistroSubmetaEmpresa(Long idSubmeta, Long idMedicao, Context builderContext) {

        List<SubmetaMedicaoBD> cacheSubmetaMedicao = builderContext.getCacheSubmetaMedicaoBD();

        return cacheSubmetaMedicao.stream()
                .anyMatch(submetaMedicao -> submetaMedicao.getIdSubmetaVrpl().equals(idSubmeta)
                        && submetaMedicao.getSituacaoEmpresa() != null
                        && submetaMedicao.getIdMedicao().longValue() <= idMedicao.longValue()
                        && permiteVisualizarDadosEmpresa(submetaMedicao.getIdMedicao(), builderContext));
    }

    protected boolean existeRegistroSubmetaConvenente(Long idSubmeta, Long idMedicao, Context builderContext) {

        List<SubmetaMedicaoBD> cacheSubmetaMedicao = builderContext.getCacheSubmetaMedicaoBD();

        return cacheSubmetaMedicao.stream()
                .anyMatch(submetaMedicao -> submetaMedicao.getIdSubmetaVrpl().equals(idSubmeta)
                        && submetaMedicao.getSituacaoConvenente() != null
                        && submetaMedicao.getIdMedicao().longValue() <= idMedicao.longValue()
                        && permiteVisualizarDadosConvenente(submetaMedicao.getIdMedicao(), builderContext));
    }

    protected boolean existeRegistroSubmetaConcedente(Long idSubmeta, Long idMedicao, Context builderContext) {

        List<SubmetaMedicaoBD> cacheSubmetaMedicao = builderContext.getCacheSubmetaMedicaoBD();

        return cacheSubmetaMedicao.stream()
                .anyMatch(submetaMedicao -> submetaMedicao.getIdSubmetaVrpl().equals(idSubmeta)
                        && submetaMedicao.getSituacaoConcedente() != null
                        && submetaMedicao.getIdMedicao().longValue() <= idMedicao.longValue()
                        && permiteVisualizarDadosConcedente(submetaMedicao.getIdMedicao(), builderContext));
    }

    protected boolean requerAssinaturaConvenente(SubmetaMedicaoDTO submetaMedicao, Context builderContext) {

        MedicaoDTO medicao = builderContext.getMedicao();

        return submetaMedicao.getSituacaoEmpresa() == ASS || (medicao.getSituacao() == SituacaoMedicaoEnum.AT
                && isSubmetaAssinadaPelaEmpresaMedicaoAcumulada(submetaMedicao, builderContext));
    }

    private boolean isSubmetaAssinadaPelaEmpresaMedicaoAcumulada(SubmetaMedicaoDTO submetaMedicao,
            Context builderContext) {

        List<Long> cacheIdMedicoesAcumuladas = builderContext.getCacheIdMedicoesAcumuladas();
        List<SubmetaMedicaoBD> cacheSubmetaMedicaoBD = builderContext.getCacheSubmetaMedicaoBD();

        return cacheSubmetaMedicaoBD.stream()
                .anyMatch(submetaMedicaoBD -> submetaMedicaoBD.getIdSubmetaVrpl().equals(submetaMedicao.getId())
                        && submetaMedicaoBD.getSituacaoEmpresa() == ASS
                        && cacheIdMedicoesAcumuladas.contains(submetaMedicaoBD.getIdMedicao()));
    }

    protected boolean requerAssinaturaConcedente(SubmetaMedicaoDTO submetaMedicao, Context builderContext) {

        return submetaMedicao.getSituacaoConvenente() == ASS
                || isSubmetaAssinadaPeloConvenenteMedicaoAcumulada(submetaMedicao, builderContext);
    }

    private boolean isSubmetaAssinadaPeloConvenenteMedicaoAcumulada(SubmetaMedicaoDTO submetaMedicao,
            Context builderContext) {

        List<Long> cacheIdMedicoesAcumuladas = builderContext.getCacheIdMedicoesAcumuladas();
        List<SubmetaMedicaoBD> cacheSubmetaMedicaoBD = builderContext.getCacheSubmetaMedicaoBD();

        return cacheSubmetaMedicaoBD.stream()
                .anyMatch(submetaMedicaoBD -> submetaMedicaoBD.getIdSubmetaVrpl().equals(submetaMedicao.getId())
                        && submetaMedicaoBD.getSituacaoConvenente() == ASS
                        && cacheIdMedicoesAcumuladas.contains(submetaMedicaoBD.getIdMedicao()));
    }
}
