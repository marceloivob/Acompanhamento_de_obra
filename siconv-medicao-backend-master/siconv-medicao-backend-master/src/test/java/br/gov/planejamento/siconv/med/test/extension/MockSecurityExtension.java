package br.gov.planejamento.siconv.med.test.extension;

import static org.apache.commons.lang3.reflect.FieldUtils.readField;
import static org.apache.commons.lang3.reflect.FieldUtils.writeField;
import static org.junit.platform.commons.support.AnnotationSupport.findAnnotation;

import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import br.gov.planejamento.siconv.med.infra.security.SecurityContext;
import br.gov.planejamento.siconv.med.infra.security.UsuarioLogado;
import br.gov.planejamento.siconv.med.infra.security.domain.Permission;
import br.gov.planejamento.siconv.med.infra.security.domain.Profile;
import br.gov.planejamento.siconv.med.infra.security.domain.Role;
import lombok.Data;

public class MockSecurityExtension implements BeforeEachCallback {

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {

        Consumer<MockUsuario> processAnnotation = (annotation) -> processAnnotation(context, annotation);

        findMethodAnnotation(context).ifPresentOrElse(processAnnotation,
                () -> findClassAnnotation(context).ifPresent(processAnnotation));
    }

    private Optional<MockUsuario> findMethodAnnotation(ExtensionContext context) {
        return findAnnotation(context.getTestMethod(), MockUsuario.class);
    }

    private Optional<MockUsuario> findClassAnnotation(ExtensionContext context) {
        return findAnnotation(context.getTestClass(), MockUsuario.class);
    }

    private void processAnnotation(ExtensionContext context, MockUsuario annotation) {
        SecurityContext securityContext = getSecurityContext(context.getRequiredTestInstance());
        createMockUsuario(securityContext, annotation);
    }

    private SecurityContext getSecurityContext(Object testInstance) {
        try {
            SecurityContext securityContext = (SecurityContext) readField(testInstance, "securityContext", true);

            if (securityContext == null) {
                securityContext = new SecurityContext();
                writeField(testInstance, "securityContext", securityContext, true);
            }

            return securityContext;

        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "A anotação @MockUsuario exige a declaração de um atributo 'securityContext' na classe de teste",
                    e);
        }
    }

    private void createMockUsuario(SecurityContext securityContext, MockUsuario annotation) {
        try {
            writeField(securityContext, "user", UsuarioBuilder.build(annotation), true);

        } catch (Exception e) {
            throw new IllegalArgumentException("Erro ao criar mock do usuário logado", e);
        }
    }

    @Data
    private static class UsuarioBuilder implements UsuarioLogado {

        final String cpf;
        final Profile profile;
        final Set<Role> roles;
        final Set<Permission> permissions;

        @Override
        public Set<Role> getRoles(String... context) {
            return roles;
        }

        @Override
        public Set<Permission> getPermissions(String... context) {
            return permissions;
        }

        static UsuarioBuilder build(MockUsuario annotation) {
            return new UsuarioBuilder(annotation.cpf(), annotation.profile(), Set.of(annotation.roles()),
                    Set.of(annotation.permissions()));
        }
    }
}
