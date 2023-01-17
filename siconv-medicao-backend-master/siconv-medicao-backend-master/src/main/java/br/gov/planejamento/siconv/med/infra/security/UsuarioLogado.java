package br.gov.planejamento.siconv.med.infra.security;

import java.util.Set;

import br.gov.planejamento.siconv.med.infra.security.domain.Permission;
import br.gov.planejamento.siconv.med.infra.security.domain.Profile;
import br.gov.planejamento.siconv.med.infra.security.domain.Role;

public interface UsuarioLogado {

    String getCpf();

    public Profile getProfile();

    public Set<Role> getRoles(String... context);

    public Set<Permission> getPermissions(String... context);
}
