package br.gov.planejamento.siconv.med.infra.security.autoriza;

import static org.eclipse.microprofile.config.ConfigProvider.getConfig;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import lombok.Getter;

@Getter
@ApplicationScoped
public class AutorizaProperties {

    @ConfigProperty(name = "maisbrasil.autoriza.govbr.url-provider")
    private String govBrUrlProvider;

    @ConfigProperty(name = "maisbrasil.autoriza.govbr.url-servicos")
    private String govBrUrlServicos;

    @ConfigProperty(name = "maisbrasil.autoriza.govbr.client-id")
    private String govBrClientId;

    @ConfigProperty(name = "maisbrasil.autoriza.govbr.client-secret")
    private String govBrClientSecret;

    @ConfigProperty(name = "maisbrasil.autoriza.jwt.secret.base64")
    private String jwtSecretBase64;

    @ConfigProperty(name = "maisbrasil.autoriza.jwt.token-validity-in-seconds")
    private Integer jwtTokenValiditySeconds;

    @ConfigProperty(name = "maisbrasil.autoriza.use-proxy")
    private boolean proxyEnabled;

    private String proxyHost;

    private Integer proxyPort;

    private String proxyUser;

    private String proxyPassword;

    @PostConstruct
    protected void initOptionalProperties() {
        if (proxyEnabled) {
            proxyHost = getConfig().getValue("maisbrasil.autoriza.proxy.hostname", String.class);
            proxyPort = getConfig().getValue("maisbrasil.autoriza.proxy.port", Integer.class);
            proxyUser = getConfig().getValue("maisbrasil.autoriza.proxy.username", String.class);
            proxyPassword = getConfig().getValue("maisbrasil.autoriza.proxy.password", String.class);
        }
    }
}
