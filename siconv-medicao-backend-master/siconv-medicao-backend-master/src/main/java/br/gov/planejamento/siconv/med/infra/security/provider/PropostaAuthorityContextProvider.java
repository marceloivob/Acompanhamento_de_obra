package br.gov.planejamento.siconv.med.infra.security.provider;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import br.gov.planejamento.siconv.med.contrato.business.ContratosBC;
import br.gov.planejamento.siconv.med.contrato.entity.ContratoBD;
import br.gov.planejamento.siconv.med.infra.security.ResourceAuthorityContext;
import io.quarkus.arc.Unremovable;

@Unremovable
@ApplicationScoped
public class PropostaAuthorityContextProvider implements AuthorityContextProvider<Long> {

	@Inject
    private ContratosBC contratoBC;
	
	@Override
    public ResourceAuthorityContext get(Long idProposta) {

		ContratoBD contratoMedicao = contratoBC.consultarContratoMedicaoPorPropostaFK(idProposta);

		if (contratoMedicao != null) {
	        return new ResourceAuthorityContext(contratoMedicao.getPropostaFk().toString(),
	                contratoMedicao.getCnpjFornecedor());
		} else {
			return new ResourceAuthorityContext(String.valueOf(idProposta),
	                "");
		}
    }
}
