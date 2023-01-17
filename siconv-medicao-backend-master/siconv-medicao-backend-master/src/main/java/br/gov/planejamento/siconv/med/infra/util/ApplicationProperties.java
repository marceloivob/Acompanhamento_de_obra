package br.gov.planejamento.siconv.med.infra.util;

import static br.gov.planejamento.siconv.med.infra.util.Environment.fromProfile;
import static io.quarkus.runtime.configuration.ProfileManager.getActiveProfile;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import lombok.Getter;

@ApplicationScoped
@Getter
public class ApplicationProperties {

    public static final String APPLICATION_JSON_UTF8 = "application/json; charset=utf-8";

    @ConfigProperty(name = "medicao.rest.showErrorStackTrace")
    private boolean showErrorStackTrace;

    @ConfigProperty(name = "publickey.jwt.idp")
    private String publicKeyJwtIDP;

    @ConfigProperty(name = "integrations.PUBLIC.IDP.endpoint")
    private String urlIdp;

    @ConfigProperty(name = "integrations.PUBLIC.SICONV.endpoint")
    private String urlSiconv;

    @ConfigProperty(name = "maisbrasil.autoriza.govbr.url-provider")
    private String urlGovBr;

    @ConfigProperty(name = "maisbrasil.autoriza.govbr.client-id")
    private String idAppGovBr;

    private Environment environment = fromProfile(getActiveProfile());
}
