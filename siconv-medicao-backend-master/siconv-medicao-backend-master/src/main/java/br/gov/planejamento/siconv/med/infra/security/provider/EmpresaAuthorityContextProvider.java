package br.gov.planejamento.siconv.med.infra.security.provider;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response.Status;

import br.gov.planejamento.siconv.med.empresa.business.EmpresaBC;
import br.gov.planejamento.siconv.med.empresa.entity.dto.EmpresaDTO;
import br.gov.planejamento.siconv.med.infra.exception.MedicaoRestException;
import br.gov.planejamento.siconv.med.infra.message.MessageKey;
import br.gov.planejamento.siconv.med.infra.security.ResourceAuthorityContext;
import io.quarkus.arc.Unremovable;

@Unremovable
@ApplicationScoped
public class EmpresaAuthorityContextProvider implements AuthorityContextProvider<Long> {

    @Inject
    private EmpresaBC empresasBC;

    @Override
    public ResourceAuthorityContext get(Long idEmpresa) {

        EmpresaDTO empresa = empresasBC.consultarEmpresaPorId(idEmpresa);

        if (empresa != null) {
            return new ResourceAuthorityContext("", empresa.getCnpj());

        }

        throw new MedicaoRestException(MessageKey.ERRO_EMPRESA_INEXISTENTE, Status.NOT_FOUND.getStatusCode());
    }
}
