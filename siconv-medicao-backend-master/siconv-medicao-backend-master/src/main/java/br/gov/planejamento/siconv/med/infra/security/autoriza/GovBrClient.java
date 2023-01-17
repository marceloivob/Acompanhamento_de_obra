package br.gov.planejamento.siconv.med.infra.security.autoriza;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.http.HttpHeaders.ACCEPT;
import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.apache.http.HttpStatus.SC_OK;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jose4j.json.JsonUtil;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.keys.resolvers.JwksVerificationKeyResolver;
import org.jose4j.lang.JoseException;

import br.gov.planejamento.siconv.med.infra.exception.GovBrIntegrationException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class GovBrClient {

    @Inject
    AutorizaProperties config;

    public Map<String, String> authenticate(String code, String redirectUri) {

        log.debug("===============================================");
        log.debug("Iniciando a Integração com Gov.Br");
        log.debug("===============================================");

        try {
            String json = extractTokens(code, redirectUri);

            Map<String, Object> token = JsonUtil.parseJson(json);
            String accessToken = token.get("access_token").toString();
            String idToken = token.get("id_token").toString();

            JwtConsumer jwtValidator = getJwtValidator();

            JwtClaims accessTokenJwtClaims = jwtValidator.processToClaims(accessToken);
            JwtClaims idTokenJwtClaims = jwtValidator.processToClaims(idToken);

            String cpf = accessTokenJwtClaims.getSubject();
            String nome = idTokenJwtClaims.getStringClaimValue("name");

            var authGovBr = new HashMap<String, String>();
            authGovBr.put("cpf", cpf);
            authGovBr.put("nome", nome);

            return authGovBr;

        } catch (Exception e) {
            throw new GovBrIntegrationException("Ocorreu uma falha durante a integração com o GovBr.", e);
        }
    }

    private String extractTokens(String code, String redirectUri)
            throws IOException, HttpException, URISyntaxException {

        URIBuilder uriBuilder = new URIBuilder(config.getGovBrUrlProvider());
        uriBuilder.setPath("/token");
        uriBuilder.setParameter("grant_type", "authorization_code");
        uriBuilder.setParameter("code", code);
        uriBuilder.setParameter("redirect_uri", redirectUri);

        HttpPost request = new HttpPost(uriBuilder.build());
        request.addHeader(ACCEPT, APPLICATION_JSON);
        request.addHeader(AUTHORIZATION, String.format("Basic %s", Base64.getEncoder().encodeToString(
                String.format("%s:%s", config.getGovBrClientId(), config.getGovBrClientSecret()).getBytes())));

        HttpResponse response = getHttpClient().execute(request);

        validarResponseStatus(response);

        return extrairResponseContent(response);
    }

    private JwtConsumer getJwtValidator() throws IOException, JoseException, HttpException, URISyntaxException {

        URIBuilder uriBuilder = new URIBuilder(config.getGovBrUrlProvider());
        uriBuilder.setPath("/jwk");

        HttpGet request = new HttpGet(uriBuilder.build());
        request.addHeader(ACCEPT, APPLICATION_JSON);

        HttpResponse response = getHttpClient().execute(request);

        validarResponseStatus(response);

        JsonWebKeySet jwks = new JsonWebKeySet(extrairResponseContent(response));
        JwksVerificationKeyResolver verificationKeyResolver = new JwksVerificationKeyResolver(jwks.getJsonWebKeys());

        return new JwtConsumerBuilder()

                // Exige que o token tenha um tempo de validade
                .setRequireExpirationTime()

                // Verifica audience da plataforma
                .setExpectedAudience(config.getGovBrClientId())

                // Testa se o tempo de validade do access token é inferior ou igual ao tempo
                // máximo estipulado (Tempo padrão de 60 minutos)
                .setMaxFutureValidityInMinutes(60)

                // Esta é uma boa prática
                .setAllowedClockSkewInSeconds(30)

                // Exige que o token tenha um Subject
                .setRequireSubject()

                // Verifica a procedência do token
                .setExpectedIssuer(config.getGovBrUrlProvider() + "/")

                // Verifica a assinatura com o jwks fornecido
                .setVerificationKeyResolver(verificationKeyResolver)

                // Cria a instância JwtConsumer
                .build();
    }

    public String getSelos(String accessToken) {

        try {
            URIBuilder uriBuilder = new URIBuilder(config.getGovBrUrlServicos());
            uriBuilder.setPath("/api/info/usuario/selo");

            HttpGet request = new HttpGet(uriBuilder.build());
            request.addHeader(ACCEPT, APPLICATION_JSON);
            request.addHeader(AUTHORIZATION, "Bearer " + accessToken);

            HttpResponse response = getHttpClient().execute(request);

            validarResponseStatus(response);

            return extrairResponseContent(response);

        } catch (Exception e) {
            throw new GovBrIntegrationException("Erro ao tentar recuperar os Selos do usuário no GovBr", e);
        }

    }

    private HttpClient getHttpClient() {

        HttpClient client = null;

        log.debug(String.format("Proxy Enabled: '%s'", config.isProxyEnabled()));

        if (config.isProxyEnabled()) {

            log.debug("ACESSANDO GOV.BR USANDO PROXY AUTENTICADO...");

            HttpHost proxy = new HttpHost(config.getProxyHost(), config.getProxyPort());
            Credentials credentials = new UsernamePasswordCredentials(config.getProxyUser(), config.getProxyPassword());
            AuthScope authScope = new AuthScope(config.getProxyHost(), config.getProxyPort());
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(authScope, credentials);

            client = HttpClientBuilder.create().setProxy(proxy).setDefaultCredentialsProvider(credentialsProvider)
                    .build();
        } else {

            log.debug("ACESSANDO GOV.BR SEM O USO DE PROXY... ");

            client = HttpClientBuilder.create().build();
        }

        return client;
    }

    private void validarResponseStatus(HttpResponse response) throws HttpException {

        if (response.getStatusLine().getStatusCode() != SC_OK) {

            String responseContent = null;

            try {
                responseContent = extrairResponseContent(response);

            } catch (Exception e) {
                log.warn("Não foi possível extrair o conteúdo do Response.", e);
            }

            throw new HttpException("Resposta HTTP inesperada [Status: " + response.getStatusLine().getStatusCode()
                    + " | Content: \"" + responseContent + "\"]");
        }
    }

    private String extrairResponseContent(HttpResponse response) throws IOException {

        if (response == null || response.getEntity() == null) {
            return null;
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

        StringBuilder content = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            content.append(line);
        }

        return content.toString();
    }

}
