package br.gov.planejamento.siconv.med.infra.security.provider;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import br.gov.planejamento.siconv.med.contrato.business.ContratosBC;
import br.gov.planejamento.siconv.med.contrato.entity.ContratoBD;
import br.gov.planejamento.siconv.med.contrato.entity.dto.ContratoSiconvDTO;
import br.gov.planejamento.siconv.med.infra.security.ResourceAuthorityContext;
import io.quarkus.arc.Unremovable;

@Unremovable
@ApplicationScoped
public class ContratoAuthorityContextProvider implements AuthorityContextProvider<Long> {

    @Inject
    private ContratosBC contratoBC;

    @Override
    public ResourceAuthorityContext get(Long idContratoSiconv) {

        ContratoBD contratoMedicao = contratoBC.consultarContratoMedicaoPorContratoFK(idContratoSiconv);

        if (contratoMedicao != null) {

            return new ResourceAuthorityContext(contratoMedicao.getPropostaFk().toString(),
                    contratoMedicao.getCnpjFornecedor());

        } else {

            ContratoSiconvDTO contratoSiconv = contratoBC.consultarContratoPorId(idContratoSiconv);

            return new ResourceAuthorityContext(contratoSiconv.getPropostaFk().toString(), contratoSiconv.getCnpj());
        }
    }
}
