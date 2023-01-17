package br.gov.planejamento.siconv.med.infra.security.interceptor;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import java.util.List;
import java.util.stream.Stream;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import br.gov.planejamento.siconv.med.infra.exception.SecurityAccessException;
import br.gov.planejamento.siconv.med.infra.security.SecurityContext;
import br.gov.planejamento.siconv.med.infra.security.annotation.AnyPermissionAllowed;
import br.gov.planejamento.siconv.med.infra.security.annotation.AnyRoleAllowed;
import br.gov.planejamento.siconv.med.infra.security.annotation.AuthorityContextSuppliedBy;
import br.gov.planejamento.siconv.med.infra.security.annotation.PermissionsAllowed;
import br.gov.planejamento.siconv.med.infra.security.annotation.ProfilesAllowed;
import br.gov.planejamento.siconv.med.infra.security.annotation.RequiresAuthorization;
import br.gov.planejamento.siconv.med.infra.security.annotation.RolesAllowed;
import br.gov.planejamento.siconv.med.infra.security.domain.AuthorizationScope;
import br.gov.planejamento.siconv.med.infra.security.domain.Profile;

@Priority(3000)
@RequiresAuthorization
@Interceptor
public class RequiresAuthorizationInterceptor {

    @Inject
    private SecurityContext securityContext;

    @AroundInvoke
    public Object intercept(InvocationContext ic) throws Exception {

        if (!securityContext.isLoggedIn()) {
            throw new SecurityAccessException();
        }

        checkAuthorityContextAnnotation(ic);

        boolean accessAllowed = securityContext.isUserInProfiles(getProfilesAllowed(ic))
                || getRolesAnnotations(ic).anyMatch(tag -> securityContext.hasRoleInProfile(tag.profile(), asList(tag.roles())))
                || getAnyPermissionAnnotations(ic).anyMatch(tag -> securityContext.hasAnyPermissionInProfile(tag.profile()))
                || getPermissionsAnnotations(ic).anyMatch(tag -> securityContext.hasPermissionInProfile(tag.profile(), asList(tag.permissions())))
                || getAnyRoleAnnotations(ic).anyMatch(tag -> securityContext.hasAnyRoleInProfile(tag.profile()));

        if (!accessAllowed) {
            if (getAuthorizationScope(ic) == AuthorizationScope.VIEW_RESPONSE_SENSITIVE_DATA) {
                securityContext.setSensitiveDataObfuscationEnabled(true);
            } else {
                throw new SecurityAccessException();
            }
        }

        return ic.proceed();
    }

    private void checkAuthorityContextAnnotation(InvocationContext ic) {
        if (!ic.getMethod().isAnnotationPresent(AuthorityContextSuppliedBy.class)) {
            throw new IllegalArgumentException(
                    format("A anotação @AuthorityContextSuppliedBy não foi declarada para o método '%s'.",
                            ic.getMethod().getName()));
        }
    }

    private List<Profile> getProfilesAllowed(InvocationContext ic) {
        ProfilesAllowed annotation = ic.getMethod().getAnnotation(ProfilesAllowed.class);
        return annotation != null ? asList(annotation.value()) : emptyList();
    }

    private Stream<RolesAllowed> getRolesAnnotations(InvocationContext ic) {
        return asList(ic.getMethod().getAnnotationsByType(RolesAllowed.class)).stream();
    }

    private Stream<AnyPermissionAllowed> getAnyPermissionAnnotations(InvocationContext ic) {
        return asList(ic.getMethod().getAnnotationsByType(AnyPermissionAllowed.class)).stream();
    }

    private Stream<PermissionsAllowed> getPermissionsAnnotations(InvocationContext ic) {
        return asList(ic.getMethod().getAnnotationsByType(PermissionsAllowed.class)).stream();
    }

    private Stream<AnyRoleAllowed> getAnyRoleAnnotations(InvocationContext ic) {
        return asList(ic.getMethod().getAnnotationsByType(AnyRoleAllowed.class)).stream();
    }

    private AuthorizationScope getAuthorizationScope(InvocationContext ic) {
        return ic.getMethod().getAnnotation(RequiresAuthorization.class).to();
    }

}
