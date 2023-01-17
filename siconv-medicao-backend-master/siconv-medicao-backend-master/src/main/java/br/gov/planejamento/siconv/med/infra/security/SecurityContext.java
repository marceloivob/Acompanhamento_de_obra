package br.gov.planejamento.siconv.med.infra.security;

import static java.util.Arrays.asList;
import static org.apache.commons.collections.CollectionUtils.containsAny;

import java.util.Collection;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import br.gov.planejamento.siconv.med.infra.security.domain.Permission;
import br.gov.planejamento.siconv.med.infra.security.domain.Profile;
import br.gov.planejamento.siconv.med.infra.security.domain.Role;
import lombok.Getter;
import lombok.Setter;

@RequestScoped
public class SecurityContext {

    @Inject
    @Getter
    private UsuarioLogado user;

    @Setter
    private ResourceAuthorityContext authorityContext;

    @Getter
    @Setter
    private boolean sensitiveDataObfuscationEnabled = false;

    public boolean isLoggedIn() {
        return user != null;
    }

    public boolean hasAnyRole() {
        return hasRole(asList(Role.values()));
    }

    public boolean hasRole(Collection<Role> roles) {
        return containsAny(user.getRoles(getAuthorityContextValues()), roles);
    }

    public boolean hasOnlyOneRole(Role role) {
        return asList(role).containsAll(user.getRoles(getAuthorityContextValues()));
    }

    public boolean hasAnyPermission() {
        return hasPermission(asList(Permission.values()));
    }

    public boolean hasPermission(Collection<Permission> permissions) {
        return containsAny(user.getPermissions(getAuthorityContextValues()), permissions);
    }

    public boolean isUserInProfile(Profile profile) {
        return user.getProfile() == profile;
    }

    public boolean isUserInProfiles(Collection<Profile> profiles) {
        return profiles.contains(user.getProfile());
    }

    public boolean hasAnyRoleInProfile(Profile profile) {
        return isUserInProfile(profile) && hasAnyRole();
    }

    public boolean hasRoleInProfile(Profile profile, Collection<Role> roles) {
        return isUserInProfile(profile) && hasRole(roles);
    }

    public boolean hasAnyPermissionInProfile(Profile profile) {
        return isUserInProfile(profile) && hasAnyPermission();
    }

    public boolean hasPermissionInProfile(Profile profile, Collection<Permission> permissions) {
        return isUserInProfile(profile) && hasPermission(permissions);
    }

    private String[] getAuthorityContextValues() {

        if (authorityContext != null) {
            return new String[] { authorityContext.getIdProposta(), authorityContext.getCnpjEmpresa() };
        }

        return new String[] {};
    }
}
