package br.gov.planejamento.siconv.med.infra.security.provider;

import br.gov.planejamento.siconv.med.infra.security.ResourceAuthorityContext;

public interface AuthorityContextProvider<T> {

    public ResourceAuthorityContext get(T param);

}