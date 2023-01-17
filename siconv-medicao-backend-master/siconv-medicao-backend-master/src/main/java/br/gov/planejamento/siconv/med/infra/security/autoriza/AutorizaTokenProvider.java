package br.gov.planejamento.siconv.med.infra.security.autoriza;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.split;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import br.gov.economia.maisbrasil.cadastro.grpc.Entidade;
import br.gov.economia.maisbrasil.cadastro.grpc.TokenResponse;
import br.gov.planejamento.siconv.med.infra.security.domain.Permission;
import br.gov.planejamento.siconv.med.integration.maisbrasil.MaisBrasilGRPCConsumer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class AutorizaTokenProvider {

    @Inject
    AutorizaProperties config;

    @Inject
    MaisBrasilGRPCConsumer cadastroGRPC;

    public String retoken(String currentToken) {
        DecodedJWT jwt = JWT.decode(currentToken);

        Map<String, String> authGovBr = new HashMap<>();
        authGovBr.put("cpf", jwt.getSubject());
        authGovBr.put("nome", jwt.getClaim("usuario").asMap().get("nome").toString());

        return createToken(authGovBr);
    }

    public String createToken(Map<String, String> authGovBr) {

        TokenResponse tokenResponse = cadastroGRPC.getInformacoesToken(authGovBr.get("cpf"));

        HashMap<String, Object> dadosUsuario = new HashMap<>();
        List<Map<String, Object>> listaEmpresas = new ArrayList<>();

        if (!isBlank(tokenResponse.getCpf())) {
            dadosUsuario.put("nome", tokenResponse.getNome());
            listaEmpresas = tokenResponse.getListaEntidadesList().stream()
                    .filter(e -> "empresa".equals(e.getTipoEntidade())).map(this::newEmpresaClaim).collect(toList());

        } else {
            // Para usuário não cadastrado no MaisBrasil o nome é recuperado do GovBr
            dadosUsuario.put("nome", authGovBr.get("nome"));
        }

        return JWT.create().withSubject(authGovBr.get("cpf")).withClaim("usuario", dadosUsuario)
                .withClaim("empresas", listaEmpresas).withIssuer("maisbrasil").withExpiresAt(getTokenExpiration())
                .sign(getAlgorithm());
    }

    private Date getTokenExpiration() {
        return Date.from(Instant.now().plusSeconds(config.getJwtTokenValiditySeconds()));
    }

    private Algorithm getAlgorithm() {
        byte[] keyBytes = Base64.getDecoder().decode(config.getJwtSecretBase64());
        return Algorithm.HMAC512(keyBytes);
    }

    public boolean validateToken(String authToken) {

        try {
            JWTVerifier verifier = JWT.require(getAlgorithm()).withIssuer("maisbrasil").build();
            verifier.verify(authToken);
            return true;

        } catch (JWTVerificationException e) {
            log.info("Invalid JWT.");
            log.trace("Invalid JWT trace: {}", e);
        }

        return false;
    }

    private Map<String, Object> newEmpresaClaim(Entidade entidade) {

        Map<String, Object> claim = new HashMap<>();

        claim.put("id", entidade.getIdEntidade());
        claim.put("cnpj", entidade.getCnpjEntidade());
        claim.put("operacoes", Stream.of(split(entidade.getOperacoes(), ",")).map(Integer::valueOf)
                .filter(op -> Permission.fromId(op) != null).collect(toList()));

        return claim;
    }
}