package br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.rest;

import static br.gov.planejamento.siconv.med.infra.rest.response.ResponseHelper.success;
import static br.gov.planejamento.siconv.med.infra.security.domain.AuthorizationScope.INVOKE_METHOD;
import static br.gov.planejamento.siconv.med.infra.security.domain.AuthorizationScope.VIEW_RESPONSE_SENSITIVE_DATA;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.CONCEDENTE;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.EMPRESA;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.MANDATARIA;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.PROPONENTE_CONVENENTE;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.USUARIO_SICONV;
import static br.gov.planejamento.siconv.med.infra.security.domain.Role.FISCAL_CONVENENTE;
import static br.gov.planejamento.siconv.med.infra.security.domain.Role.GESTOR_CONVENIO_CONVENENTE;
import static br.gov.planejamento.siconv.med.infra.security.domain.Role.GESTOR_FINANCEIRO_CONVENENTE;
import static br.gov.planejamento.siconv.med.infra.security.domain.Role.OPERADOR_FINANCEIRO_CONVENENTE;
import static br.gov.planejamento.siconv.med.infra.util.ApplicationProperties.APPLICATION_JSON_UTF8;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.Operation;

import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.business.ResponsavelTecnicoBC;
import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.dto.ResponsavelTecnicoDTO;
import br.gov.planejamento.siconv.med.infra.rest.response.ResponseHelper.DefaultResponse;
import br.gov.planejamento.siconv.med.infra.security.annotation.AnyPermissionAllowed;
import br.gov.planejamento.siconv.med.infra.security.annotation.AuthorityContextSuppliedBy;
import br.gov.planejamento.siconv.med.infra.security.annotation.ProfilesAllowed;
import br.gov.planejamento.siconv.med.infra.security.annotation.RequiresAuthorization;
import br.gov.planejamento.siconv.med.infra.security.annotation.RolesAllowed;
import br.gov.planejamento.siconv.med.infra.security.provider.ContratoAuthorityContextProvider;
import br.gov.planejamento.siconv.med.infra.security.provider.ContratoRespTecnicoAuthorityContextProvider;

@Path("/")
public class ResponsavelTecnicoRest {
	@Inject
	private ResponsavelTecnicoBC responsaveltecnicoBC;

	/**
	 * Consulta Todos os Responsáveis Técnicos vinculados ao contrato
	 * 
	 * @param idMedContrato
	 */
	@GET
	@Path("/responsavel/listar/{numeroContrato}")
	@Produces(MediaType.APPLICATION_JSON)
	@RequiresAuthorization(to = VIEW_RESPONSE_SENSITIVE_DATA)
	@AuthorityContextSuppliedBy(provider = ContratoAuthorityContextProvider.class, param = "numeroContrato")
	@AnyPermissionAllowed(profile = EMPRESA)
	@ProfilesAllowed({ CONCEDENTE, PROPONENTE_CONVENENTE, MANDATARIA, USUARIO_SICONV })
	public DefaultResponse<List<ResponsavelTecnicoDTO>> listaResponsavelTecnicoPorContrato(
			@PathParam("numeroContrato") Long idMedContrato) {

		List<ResponsavelTecnicoDTO> listaponsaveltecnico = responsaveltecnicoBC
				.listarResponsavelTecnicoPorContrato(idMedContrato);

		return success(listaponsaveltecnico);

	}

	/**
	 * Consulta Todos os Responsáveis Técnicos disponíveis para combo do ART para um
	 * tipo
	 * 
	 * @param idMedContrato
	 * @param tipo
	 */
	@GET
	@Path("/responsavel/listar/{numeroContrato}/tipo/{tipo}")
	@Produces(MediaType.APPLICATION_JSON)
    @RequiresAuthorization(to = VIEW_RESPONSE_SENSITIVE_DATA)
    @AuthorityContextSuppliedBy(provider = ContratoAuthorityContextProvider.class, param = "numeroContrato")
    @AnyPermissionAllowed(profile = EMPRESA)
	@ProfilesAllowed({ CONCEDENTE, PROPONENTE_CONVENENTE, MANDATARIA, USUARIO_SICONV })
	public DefaultResponse<List<ResponsavelTecnicoDTO>> listaResponsavelTecnicoPorContratoTipo(
			@PathParam("numeroContrato") Long idMedContrato, @PathParam("tipo") String tipo) {

		List<ResponsavelTecnicoDTO> listaponsaveltecnico = responsaveltecnicoBC
				.listarResponsavelTecnicoPorContratoTipo(idMedContrato, tipo);

		return success(listaponsaveltecnico);

	}

	/**
	 * Inserir Responsável Técnico e seus registros e vinculo
	 * 
	 * @throws IOException
	 */
	@POST
	@Path("/contratos/{idContrato}/responsavel")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(APPLICATION_JSON_UTF8)
	@RequiresAuthorization(to = INVOKE_METHOD)
	@AuthorityContextSuppliedBy(provider = ContratoAuthorityContextProvider.class, param = "idContrato")
    @RolesAllowed(profile = PROPONENTE_CONVENENTE, roles = { GESTOR_CONVENIO_CONVENENTE, GESTOR_FINANCEIRO_CONVENENTE,
            OPERADOR_FINANCEIRO_CONVENENTE, FISCAL_CONVENENTE })
	public DefaultResponse<ResponsavelTecnicoDTO> incluir(ResponsavelTecnicoDTO responsavelTecnicoDTO,
			@PathParam("idContrato") Long idContrato) {

		responsaveltecnicoBC.salvar(responsavelTecnicoDTO, idContrato);

		return success(responsavelTecnicoDTO);
	}

	/**
	 * Alterar Responsável Técnico e seus registros e vinculo
	 * 
	 * @throws IOException
	 */
	@PUT
	@Path("/contratos/{idContrato}/responsavel/{idResponsavelTecnico}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(APPLICATION_JSON_UTF8)
    @RequiresAuthorization(to = INVOKE_METHOD)
    @AuthorityContextSuppliedBy(provider = ContratoAuthorityContextProvider.class, param = "idContrato")
    @RolesAllowed(profile = PROPONENTE_CONVENENTE, roles = { GESTOR_CONVENIO_CONVENENTE, GESTOR_FINANCEIRO_CONVENENTE,
            OPERADOR_FINANCEIRO_CONVENENTE, FISCAL_CONVENENTE })
	public DefaultResponse<ResponsavelTecnicoDTO> alterar(ResponsavelTecnicoDTO responsavelTecnicoDTO,
			@PathParam("idContrato") Long idContrato, @PathParam("idResponsavelTecnico") Long idResponsavelTecnico) {

		responsaveltecnicoBC.salvar(responsavelTecnicoDTO, idContrato);

		return success(responsavelTecnicoDTO);
	}

	/**
	 * Excluir Vinculo do Responsavel Tecnico com o Contrato
	 */
	@DELETE
	@Path("/responsavel/{idMedContratoRespTec}")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Excluir Vinculo do Responsavel Tecnico com o Contrato")
	@RequiresAuthorization(to = INVOKE_METHOD)
	@AuthorityContextSuppliedBy(provider = ContratoRespTecnicoAuthorityContextProvider.class, param = "idMedContratoRespTec")
    @RolesAllowed(profile = PROPONENTE_CONVENENTE, roles = { GESTOR_CONVENIO_CONVENENTE, GESTOR_FINANCEIRO_CONVENENTE,
            OPERADOR_FINANCEIRO_CONVENENTE, FISCAL_CONVENENTE })
	public DefaultResponse<String> excluirVinculoRespTecContrato(
			@PathParam("idMedContratoRespTec") Long idMedContratoRespTec) {

		responsaveltecnicoBC.excluirVinculoRespTecContrato(idMedContratoRespTec);

		return success("ok");

	}
	
    @GET
    @Path("/responsavel/{numeroCPF}/tipo/{tipoRespTec}/contrato/{idContratoSiconv}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiresAuthorization(to = INVOKE_METHOD)
    @AuthorityContextSuppliedBy(provider = ContratoAuthorityContextProvider.class, param = "idContratoSiconv")
    @RolesAllowed(profile = PROPONENTE_CONVENENTE, roles = { GESTOR_CONVENIO_CONVENENTE, GESTOR_FINANCEIRO_CONVENENTE,
            OPERADOR_FINANCEIRO_CONVENENTE, FISCAL_CONVENENTE })
    public DefaultResponse<ResponsavelTecnicoDTO> consultarResponsavelTecnicoPorTipo(@PathParam("numeroCPF") String numeroCPF, 
    		                                                                  @PathParam("tipoRespTec") String tipoRespTec,
    		                                                                  @PathParam("idContratoSiconv") Long idContratoSiconv,
    		                                                                  @QueryParam("validate") Boolean validate ) {
    	
    	ResponsavelTecnicoDTO respTecnico = responsaveltecnicoBC.recuperarResponsavelTecnicoPorCPFTipo(numeroCPF, tipoRespTec, idContratoSiconv, validate);

    	return success(respTecnico);
        
    }

	@GET
	@Path("/responsavelTecnico/{idContratoResponsavelTecnico}")
	@Produces(MediaType.APPLICATION_JSON)
	@RequiresAuthorization(to = VIEW_RESPONSE_SENSITIVE_DATA)
    @AuthorityContextSuppliedBy(provider = ContratoRespTecnicoAuthorityContextProvider.class, param = "idContratoResponsavelTecnico")
    @AnyPermissionAllowed(profile = EMPRESA)
	@ProfilesAllowed({ CONCEDENTE, PROPONENTE_CONVENENTE, MANDATARIA, USUARIO_SICONV })
	public DefaultResponse<ResponsavelTecnicoDTO> consultarResponsavelTecnicoPorId(
			@PathParam("idContratoResponsavelTecnico") Long idContratoResponsavelTecnico) {

		ResponsavelTecnicoDTO rt = responsaveltecnicoBC.consultarContratoResponsavelTecnicoPorId(idContratoResponsavelTecnico);

		return success(rt);

	}
}
