package br.gov.planejamento.siconv.med.test.extension;

import static java.util.function.Predicate.isEqual;
import static java.util.function.Predicate.not;
import static org.mockito.Mockito.doReturn;

import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;

import br.gov.planejamento.siconv.med.infra.security.SecurityContext;
import br.gov.planejamento.siconv.med.infra.security.domain.Profile;

@ExtendWith(MockSecurityExtension.class)
public abstract class SecurityBaseTest {

    @Spy
    protected SecurityContext securityContext;

    protected void mockUsuario(Profile profile) {

        doReturn(true).when(securityContext).hasAnyRoleInProfile(profile);
        doReturn(true).when(securityContext).hasAnyPermissionInProfile(profile);
        doReturn(true).when(securityContext).isUserInProfile(profile);

        Stream.of(Profile.values()).filter(not(isEqual(profile))).forEach(profileDiferente -> {
            doReturn(false).when(securityContext).hasAnyRoleInProfile(profileDiferente);
            doReturn(false).when(securityContext).hasAnyPermissionInProfile(profileDiferente);
            doReturn(false).when(securityContext).isUserInProfile(profileDiferente);
        });
    }
}
