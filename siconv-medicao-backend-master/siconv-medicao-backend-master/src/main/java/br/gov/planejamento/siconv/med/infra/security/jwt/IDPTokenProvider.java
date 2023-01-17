package br.gov.planejamento.siconv.med.infra.security.jwt;

import static java.lang.String.format;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;

import br.gov.planejamento.siconv.med.infra.security.domain.TokenIssuer;
import br.gov.planejamento.siconv.med.infra.util.ApplicationProperties;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class IDPTokenProvider {

    @Inject
    private ApplicationProperties config;

    /**
     * Validate IDP token (Siconv raiz)
     * 
     * @param token
     * @return
     */
    public boolean validateToken(String token) {

        try {
            Algorithm algorithm = Algorithm.RSA256(readPublicKeyIDP(), null);
            JWTVerifier verifier = JWT.require(algorithm).withIssuer(TokenIssuer.IDP.getKey()).build();
            verifier.verify(token);

            return true;

        } catch (JWTVerificationException e) {

            String msg = "Token JWT do IDP inválido.";

            if (log.isDebugEnabled()) {
                log.debug(format("%s [Token: %s]", msg, token), e);

            } else {
                log.info(msg);
            }
        }

        return false;
    }

    /**
     * Utility method that reads the public key in the IDP token algorithm
     * 
     * @return
     */
    private RSAPublicKey readPublicKeyIDP() {

        try {
            X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(
                    Base64.getDecoder().decode(config.getPublicKeyJwtIDP()));
            KeyFactory kf = KeyFactory.getInstance("RSA");

            return (RSAPublicKey) kf.generatePublic(pubKeySpec);

        } catch (IllegalArgumentException | NoSuchAlgorithmException | InvalidKeySpecException e) {

            throw new WebApplicationException(
                    "Falha ao ler chave pública para verificação de assinatura do Token JWT do IDP.", e);
        }
    }
}
