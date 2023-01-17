package br.gov.planejamento.siconv.med.infra.util;

import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.EMPRESA;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import br.gov.planejamento.siconv.med.infra.security.SecurityContext;
import br.gov.planejamento.siconv.med.integration.siconv.SiconvGRPCConsumer;

@ApplicationScoped
public class UrlConsultaTipoInstrumentoBuilder {

    private static final String URI_CONSULTA = "/ConsultarProposta/ResultadoDaConsultaDeConvenioSelecionarConvenio.do?idConvenio=";

    private static final String QUERY_PARAM_GUEST_LOGIN = "&destino=&Usr=guest&Pwd=guest";

    @Inject
    private ApplicationProperties config;

    @Inject
    private SiconvGRPCConsumer siconvConsumer;

    @Inject
    private SecurityContext securityContext;

    public String getUrl(Integer sequencialTipoInstrumento, Integer anoTipoInstrumento) {

        Long idConvenio = siconvConsumer.getIdConvenio(sequencialTipoInstrumento, anoTipoInstrumento);

        String url = config.getUrlSiconv() + URI_CONSULTA + idConvenio;

        if (!isLoggedSiconv()) {
            url += QUERY_PARAM_GUEST_LOGIN;
        }

        return url;
    }

    private boolean isLoggedSiconv() {
        return securityContext.isLoggedIn() && securityContext.getUser().getProfile() != EMPRESA;
    }
}
