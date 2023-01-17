package br.gov.planejamento.siconv.med.contrato.rest;

import static br.gov.planejamento.siconv.med.infra.rest.response.ResponseHelper.success;
import static br.gov.planejamento.siconv.med.infra.security.domain.AuthorizationScope.INVOKE_METHOD;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.EMPRESA;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import br.gov.planejamento.siconv.med.contrato.business.ContratosBC;
import br.gov.planejamento.siconv.med.contrato.entity.dto.ContratoSiconvDTO;
import br.gov.planejamento.siconv.med.infra.rest.response.ResponseHelper.DefaultResponse;
import br.gov.planejamento.siconv.med.infra.security.annotation.AnyPermissionAllowed;
import br.gov.planejamento.siconv.med.infra.security.annotation.AuthorityContextSuppliedBy;
import br.gov.planejamento.siconv.med.infra.security.annotation.RequiresAuthentication;
import br.gov.planejamento.siconv.med.infra.security.annotation.RequiresAuthorization;
import br.gov.planejamento.siconv.med.infra.security.provider.ContratoAuthorityContextProvider;
import br.gov.planejamento.siconv.med.infra.security.provider.EmpresaAuthorityContextProvider;
import br.gov.planejamento.siconv.med.infra.util.UrlConsultaTipoInstrumentoBuilder;

@Path("/")
public class ContratosRest {

    @Inject
    private ContratosBC contratosBC;

    @Inject
    private UrlConsultaTipoInstrumentoBuilder urlBuilder;

    @GET
    @Path("/empresas/{idEmpresa}/contratos")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresAuthorization(to = INVOKE_METHOD)
    @AuthorityContextSuppliedBy(provider = EmpresaAuthorityContextProvider.class, param = "idEmpresa")
    @AnyPermissionAllowed(profile = EMPRESA)
    public DefaultResponse<List<ContratoSiconvDTO>> listarContratosPorEmpresa(@PathParam("idEmpresa") Long idEmpresa) {

        List<ContratoSiconvDTO> contratos = contratosBC.listarContratosPorEmpresa(idEmpresa);

        return success(contratos);

    }

    @GET
    @Path("/contratos/{idContratoSiconv}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresAuthentication
    public DefaultResponse<ContratoSiconvDTO> consultarContratoPorId(
            @PathParam("idContratoSiconv") Long idContratoSiconv) {

        ContratoSiconvDTO contrato = contratosBC.consultarContratoPorId(idContratoSiconv);

        contrato.setUrlSiconvMedicao(
                urlBuilder.getUrl(contrato.getNumeroConvenioRepasse(), contrato.getAnoConvenioRepasse()));

        return success(contrato);

    }

    @GET
    @Path("/contratos/{idContratoSiconv}/temsubmetasaexecutar")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresAuthentication
    @AuthorityContextSuppliedBy(provider = ContratoAuthorityContextProvider.class, param = "idContratoSiconv")
    public DefaultResponse<Boolean> temSubmetasAExecutar(@PathParam("idContratoSiconv") Long idContratoSiconv) {

        return success(contratosBC.temSubmetasAExecutar(idContratoSiconv));
    }

    @GET
    @Path("/contratos/{idContratoSiconv}/paralisado")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresAuthentication
    @AuthorityContextSuppliedBy(provider = ContratoAuthorityContextProvider.class, param = "idContratoSiconv")
    public DefaultResponse<Boolean> isContratoParalisado(@PathParam("idContratoSiconv") Long idContratoSiconv) {

        return success(contratosBC.isContratoParalisado(idContratoSiconv));
    }
}
