package br.gov.planejamento.siconv.med.infra.security.producer;

import static br.gov.planejamento.siconv.med.infra.security.domain.TokenIssuer.IDP;
import static br.gov.planejamento.siconv.med.infra.security.domain.TokenIssuer.PLATAFORMA_MAIS_BRASIL;
import static com.google.common.base.Predicates.notNull;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Produces;

import com.auth0.jwt.interfaces.DecodedJWT;

import br.gov.planejamento.siconv.med.infra.security.UsuarioLogado;
import br.gov.planejamento.siconv.med.infra.security.annotation.UserAuthenticatedEvent;
import br.gov.planejamento.siconv.med.infra.security.domain.Permission;
import br.gov.planejamento.siconv.med.infra.security.domain.Profile;
import br.gov.planejamento.siconv.med.infra.security.domain.Role;
import br.gov.planejamento.siconv.med.infra.security.domain.TokenIssuer;
import lombok.Data;

@RequestScoped
public class UsuarioLogadoProducer {

    @Produces
    @Dependent
    private UsuarioLogado authenticatedUser;

    public void handleAuthenticationEvent(@Observes @UserAuthenticatedEvent DecodedJWT jwt) {

        TokenIssuer issuer = TokenIssuer.fromKey(jwt.getIssuer());

        if (issuer == IDP) {
            authenticatedUser = createUsuarioIDP(jwt);

        } else if (issuer == PLATAFORMA_MAIS_BRASIL) {
            authenticatedUser = createUsuarioPlataforma(jwt);
        }
    }

    private UsuarioLogado createUsuarioIDP(DecodedJWT jwt) {

        UsuarioLogadoImpl usuario = new UsuarioLogadoImpl();
        usuario.setCpf(jwt.getClaim("cpf").asString());

        String tipoEnte = jwt.getClaim("tipoEnte").asString();

        if (tipoEnte != null) {
            usuario.setProfile(Profile.fromKey(tipoEnte));
        } else {
            if (usuario.getCpf().equalsIgnoreCase("guest")) {
                usuario.setProfile(Profile.GUEST);
            } else {
                usuario.setProfile(Profile.USUARIO_SICONV);
            }
        }

        Long idProposta = jwt.getClaim("idProposta").asLong();
        List<String> rolesKeys = jwt.getClaim("roles").asList(String.class);

        String vinculoFiscalizacao = jwt.getClaim("vinculoFiscalizacao").asString();
        if (vinculoFiscalizacao != null) {
            if (rolesKeys == null) {
                rolesKeys = new ArrayList<>();
            }
            rolesKeys.add(vinculoFiscalizacao);
        }

        Set<Role> roles = convertRoles(rolesKeys);

        // Usuário com vínculo de fiscalização/acompanhamento no Siconv será tratado como
        // CONCEDENTE que possui ROLE de fiscal de acompanhamento ou técnico de terceiros.
        if (roles.contains(Role.FISCAL_ACOMPANHAMENTO) || roles.contains(Role.TECNICO_TERCEIRO)) {
            usuario.setProfile(Profile.CONCEDENTE);
        }

        usuario.getRolesMap().put(idProposta.toString(), roles);

        return usuario;
    }

    private Set<Role> convertRoles(List<String> rolesKeys) {

        if (rolesKeys == null) {
            return emptySet();
        }

        return rolesKeys.stream().map(Role::fromKey).filter(notNull()).collect(toSet());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private UsuarioLogado createUsuarioPlataforma(DecodedJWT jwt) {

        UsuarioLogadoImpl usuario = new UsuarioLogadoImpl();
        usuario.setCpf(jwt.getSubject());
        usuario.setProfile(Profile.EMPRESA); // O Profile é sempre EMPRESA para login da plataforma

        List<Map> empresas = jwt.getClaim("empresas").asList(Map.class);
        if (empresas != null) {
            for (Map empresa : empresas) {
                String cnpj = (String) empresa.get("cnpj");
                List<Object> permissionsId = (List<Object>) empresa.get("operacoes");

                usuario.getPermissionsMap().put(cnpj, convertPermissions(permissionsId));
            }
        }

        return usuario;
    }

    private Set<Permission> convertPermissions(List<Object> permissionsId) {

        if (permissionsId == null) {
            return emptySet();
        }

        return permissionsId.stream().map(id -> Permission.fromId(Integer.valueOf(id.toString()))).filter(notNull())
                .collect(toSet());
    }

    @Alternative
    @Data
    private class UsuarioLogadoImpl implements UsuarioLogado {

        private String cpf;
        private Profile profile;
        private final Map<String, Set<Role>> rolesMap = new HashMap<>();
        private final Map<String, Set<Permission>> permissionsMap = new HashMap<>();

        @Override
        public Set<Role> getRoles(String... context) {
            return rolesMap.entrySet().stream().filter(entry -> List.of(context).contains(entry.getKey()))
                    .flatMap(entry -> entry.getValue().stream()).collect(toSet());
        }

        @Override
        public Set<Permission> getPermissions(String... context) {
            return permissionsMap.entrySet().stream().filter(entry -> List.of(context).contains(entry.getKey()))
                    .flatMap(entry -> entry.getValue().stream()).collect(toSet());
        }
    }
}
