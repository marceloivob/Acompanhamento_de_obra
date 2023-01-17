package br.gov.planejamento.siconv.med.medicao.rest;

import static br.gov.planejamento.siconv.med.infra.rest.response.ResponseHelper.success;
import static br.gov.planejamento.siconv.med.infra.security.domain.AuthorizationScope.VIEW_RESPONSE_SENSITIVE_DATA;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.CONCEDENTE;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.EMPRESA;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.MANDATARIA;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.PROPONENTE_CONVENENTE;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.USUARIO_SICONV;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.Operation;

import br.gov.planejamento.siconv.med.infra.rest.response.ResponseHelper.DefaultResponse;
import br.gov.planejamento.siconv.med.medicao.business.HistoricoMedicaoBC;
import br.gov.planejamento.siconv.med.medicao.entity.dto.HistoricoMedicaoDTO;
import br.gov.planejamento.siconv.med.infra.security.annotation.AnyPermissionAllowed;
import br.gov.planejamento.siconv.med.infra.security.annotation.AuthorityContextSuppliedBy;
import br.gov.planejamento.siconv.med.infra.security.annotation.ProfilesAllowed;
import br.gov.planejamento.siconv.med.infra.security.annotation.RequiresAuthorization;
import br.gov.planejamento.siconv.med.infra.security.provider.ContratoAuthorityContextProvider;

@Path("/")
public class HistoricoMedicaoRest {

    @Inject
    private HistoricoMedicaoBC medicaoBC;

    /**
     * @GET - Listar Historico de Medicoes
     * 
     */
    @GET
    @Path("/contratos/{idContratoSiconv}/historico")
    @Produces(MediaType.APPLICATION_JSON + "; charset=utf-8")
    @Operation(summary = "Retorna o historico de medições de um contrato")
    @RequiresAuthorization(to = VIEW_RESPONSE_SENSITIVE_DATA)
    @AuthorityContextSuppliedBy(provider = ContratoAuthorityContextProvider.class, param = "idContratoSiconv")
    @AnyPermissionAllowed(profile = EMPRESA)
    @ProfilesAllowed({ CONCEDENTE, PROPONENTE_CONVENENTE, MANDATARIA, USUARIO_SICONV })
    public DefaultResponse<List<HistoricoMedicaoDTO>> recuperarHistoricoMedicaoPorContrato(
            @PathParam("idContratoSiconv") Long idContratoSiconv) {

        List<HistoricoMedicaoDTO> historicos = medicaoBC.buscarHistoricosMedicao(idContratoSiconv);

        return success(historicos);

    }

}