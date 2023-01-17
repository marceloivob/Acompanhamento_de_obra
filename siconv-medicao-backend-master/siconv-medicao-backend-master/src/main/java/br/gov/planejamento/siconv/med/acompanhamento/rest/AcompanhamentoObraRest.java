package br.gov.planejamento.siconv.med.acompanhamento.rest;

import static br.gov.planejamento.siconv.med.infra.rest.response.ResponseHelper.ok;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import br.gov.planejamento.siconv.med.acompanhamento.business.AcompanhamentoObraBC;
import br.gov.planejamento.siconv.med.infra.security.annotation.AuthorityContextSuppliedBy;
import br.gov.planejamento.siconv.med.infra.security.annotation.RequiresAuthentication;
import br.gov.planejamento.siconv.med.infra.security.provider.PropostaAuthorityContextProvider;

@Path("/")
public class AcompanhamentoObraRest {

	@Inject
	private AcompanhamentoObraBC bc;

	@GET
	@Path("/propostas/{idProposta}/contratoslotes")
	@Produces(MediaType.APPLICATION_JSON)
	@RequiresAuthentication
    @AuthorityContextSuppliedBy(provider = PropostaAuthorityContextProvider.class, param = "idProposta")
	public Response listarContratosLotes(@PathParam("idProposta") Long idProposta) {

		return ok(bc.listarContratosLotes(idProposta));
	}
}
