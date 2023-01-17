package br.gov.planejamento.siconv.med.medicao.business;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import br.gov.planejamento.siconv.med.infra.security.SecurityContext;
import br.gov.planejamento.siconv.med.infra.security.domain.Profile;
import br.gov.planejamento.siconv.med.infra.security.domain.Role;
import br.gov.planejamento.siconv.med.medicao.entity.dto.PerfilEnum;

@ApplicationScoped
public class PerfilHelper {

    @Inject
    private SecurityContext securityContext;

    public PerfilEnum getPerfilUsuarioLogado() {

        Profile profile = securityContext.getUser().getProfile();

        switch (profile) {

        case EMPRESA:
            return PerfilEnum.EMP;

        case PROPONENTE_CONVENENTE:
            return PerfilEnum.CVE;

        case MANDATARIA:
            return PerfilEnum.MAN;

        case CONCEDENTE:
            return perfilConcedente();

        default:
            return null;
        }

    }

    private PerfilEnum perfilConcedente() {

        if (securityContext.hasOnlyOneRole(Role.FISCAL_ACOMPANHAMENTO)) {
            return PerfilEnum.FSA;

        } else if (securityContext.hasOnlyOneRole(Role.TECNICO_TERCEIRO)) {
            return PerfilEnum.TTE;

        } else if (securityContext.hasOnlyOneRole(Role.ADMINISTRADOR_SISTEMA)
                || securityContext.hasOnlyOneRole(Role.ADMINISTRADOR_SISTEMA_ORGAO_EXTERNO)) {
            return PerfilEnum.ADM;

        } else {
            return PerfilEnum.CCE;
        }
    }
}
