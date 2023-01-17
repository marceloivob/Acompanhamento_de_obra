package br.gov.planejamento.siconv.med.infra.security.provider;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response.Status;

import br.gov.planejamento.siconv.med.contrato.business.ContratosBC;
import br.gov.planejamento.siconv.med.contrato.entity.ContratoBD;
import br.gov.planejamento.siconv.med.infra.exception.MedicaoRestException;
import br.gov.planejamento.siconv.med.infra.message.MessageKey;
import br.gov.planejamento.siconv.med.infra.security.ResourceAuthorityContext;
import io.quarkus.arc.Unremovable;

@Unremovable
@ApplicationScoped
public class ObservacaoAuthorityContextProvider implements AuthorityContextProvider<Long> {

    @Inject
    private ContratosBC contratoBC;

    @Override
    public ResourceAuthorityContext get(Long idObservacao) {

        ContratoBD contratoMedicao = contratoBC.consultarContratoAssociadoObservacao(idObservacao);

        if (contratoMedicao != null) {

            return new ResourceAuthorityContext(contratoMedicao.getPropostaFk().toString(),
                    contratoMedicao.getCnpjFornecedor());
        }

        throw new MedicaoRestException(MessageKey.ERRO_OBSERVACAO_NAO_ENCONTRADA, Status.NOT_FOUND.getStatusCode());
    }
}
