package br.gov.planejamento.siconv.med.medicao.rest;

import static br.gov.planejamento.siconv.med.infra.rest.response.ResponseHelper.success;
import static br.gov.planejamento.siconv.med.infra.security.domain.AuthorizationScope.INVOKE_METHOD;
import static br.gov.planejamento.siconv.med.infra.security.domain.AuthorizationScope.VIEW_RESPONSE_SENSITIVE_DATA;
import static br.gov.planejamento.siconv.med.infra.security.domain.Permission.CANCELAR_ENVIO_MEDICAO_CONVENENTE;
import static br.gov.planejamento.siconv.med.infra.security.domain.Permission.EDITAR_MEDICAO;
import static br.gov.planejamento.siconv.med.infra.security.domain.Permission.ENVIAR_MEDICAO_CONVENENTE;
import static br.gov.planejamento.siconv.med.infra.security.domain.Permission.EXCLUIR_MEDICAO;
import static br.gov.planejamento.siconv.med.infra.security.domain.Permission.INCLUIR_MEDICAO;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.CONCEDENTE;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.EMPRESA;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.MANDATARIA;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.PROPONENTE_CONVENENTE;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.USUARIO_SICONV;
import static br.gov.planejamento.siconv.med.infra.security.domain.Role.ADMINISTRADOR_SISTEMA;
import static br.gov.planejamento.siconv.med.infra.security.domain.Role.ADMINISTRADOR_SISTEMA_ORGAO_EXTERNO;
import static br.gov.planejamento.siconv.med.infra.security.domain.Role.AGENTE_ACOMPANHAMENTO_INSTITUICAO_MANDATARIA;
import static br.gov.planejamento.siconv.med.infra.security.domain.Role.FISCAL_ACOMPANHAMENTO;
import static br.gov.planejamento.siconv.med.infra.security.domain.Role.FISCAL_CONCEDENTE;
import static br.gov.planejamento.siconv.med.infra.security.domain.Role.FISCAL_CONVENENTE;
import static br.gov.planejamento.siconv.med.infra.security.domain.Role.GESTOR_CONVENIO_CONCEDENTE;
import static br.gov.planejamento.siconv.med.infra.security.domain.Role.GESTOR_CONVENIO_CONVENENTE;
import static br.gov.planejamento.siconv.med.infra.security.domain.Role.GESTOR_FINANCEIRO_CONCEDENTE;
import static br.gov.planejamento.siconv.med.infra.security.domain.Role.GESTOR_FINANCEIRO_CONVENENTE;
import static br.gov.planejamento.siconv.med.infra.security.domain.Role.OPERACIONAL_CONCEDENTE;
import static br.gov.planejamento.siconv.med.infra.security.domain.Role.OPERADOR_FINANCEIRO_CONVENENTE;
import static br.gov.planejamento.siconv.med.infra.security.domain.Role.TECNICO_TERCEIRO;
import static br.gov.planejamento.siconv.med.infra.util.ApplicationProperties.APPLICATION_JSON_UTF8;

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

import br.gov.planejamento.siconv.med.infra.rest.response.ResponseHelper.DefaultResponse;
import br.gov.planejamento.siconv.med.infra.security.annotation.AnyPermissionAllowed;
import br.gov.planejamento.siconv.med.infra.security.annotation.AuthorityContextSuppliedBy;
import br.gov.planejamento.siconv.med.infra.security.annotation.PermissionsAllowed;
import br.gov.planejamento.siconv.med.infra.security.annotation.ProfilesAllowed;
import br.gov.planejamento.siconv.med.infra.security.annotation.RequiresAuthorization;
import br.gov.planejamento.siconv.med.infra.security.annotation.RolesAllowed;
import br.gov.planejamento.siconv.med.infra.security.domain.Role;
import br.gov.planejamento.siconv.med.infra.security.provider.ContratoAuthorityContextProvider;
import br.gov.planejamento.siconv.med.infra.security.provider.MedicaoAuthorityContextProvider;
import br.gov.planejamento.siconv.med.medicao.business.MedicaoBC;
import br.gov.planejamento.siconv.med.medicao.entity.dto.MedicaoAgrupadaDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.MedicaoDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.VistoriaExtraDTO;

@Path("/")
public class MedicaoRest {

    @Inject
    private MedicaoBC medicaoBC;

    @GET
    @Path("/contratos/{idContrato}/medicoes/")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresAuthorization(to = VIEW_RESPONSE_SENSITIVE_DATA)
    @AuthorityContextSuppliedBy(provider = ContratoAuthorityContextProvider.class, param = "idContrato")
    @AnyPermissionAllowed(profile = EMPRESA)
    @ProfilesAllowed({ CONCEDENTE, PROPONENTE_CONVENENTE, MANDATARIA, USUARIO_SICONV })
    public DefaultResponse<List<MedicaoDTO>> listarMedicoes(@PathParam("idContrato") Long idContrato) {

        List<MedicaoDTO> medicoes = medicaoBC.listarMedicoes(idContrato);

        
        return success(medicoes);

    }

    @GET
    @Path("/medicoes/{idMedicao}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresAuthorization(to = VIEW_RESPONSE_SENSITIVE_DATA)
    @AuthorityContextSuppliedBy(provider = MedicaoAuthorityContextProvider.class, param = "idMedicao")
    @AnyPermissionAllowed(profile = EMPRESA)
    @ProfilesAllowed({ CONCEDENTE, PROPONENTE_CONVENENTE, MANDATARIA, USUARIO_SICONV })
    public DefaultResponse<MedicaoDTO> obterMedicao(@PathParam("idMedicao") Long idMedicao) {

        MedicaoDTO medicao = medicaoBC.obterMedicao(idMedicao);

        return success(medicao);

    }

	@POST
	@Path("/contratos/{idContrato}/medicoes")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(APPLICATION_JSON_UTF8)
	@RequiresAuthorization(to = INVOKE_METHOD)
	@AuthorityContextSuppliedBy(provider = ContratoAuthorityContextProvider.class, param = "idContrato")
	@PermissionsAllowed(profile = EMPRESA, permissions = INCLUIR_MEDICAO)
	public DefaultResponse<MedicaoDTO> incluir(MedicaoDTO medicao, @PathParam("idContrato") Long idContrato) {

		return success(medicaoBC.incluir(medicao, idContrato));
	}

    @PUT
    @Path("/medicoes/{idMedicao}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(APPLICATION_JSON_UTF8)
    @RequiresAuthorization(to = INVOKE_METHOD)
    @AuthorityContextSuppliedBy(provider = MedicaoAuthorityContextProvider.class, param = "idMedicao")
    @PermissionsAllowed(profile = EMPRESA, permissions = EDITAR_MEDICAO)
    public DefaultResponse<MedicaoDTO> alterar(MedicaoDTO medicao, @PathParam("idMedicao") Long idMedicao) {

    	medicao.setId(idMedicao);
        return success(medicaoBC.alterar(medicao));
    }
    
    @PUT
    @Path("/medicoes/{idMedicao}/concedentemandataria")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(APPLICATION_JSON_UTF8)
    @RequiresAuthorization(to = INVOKE_METHOD)
    @AuthorityContextSuppliedBy(provider = MedicaoAuthorityContextProvider.class, param = "idMedicao")
    @RolesAllowed(profile = CONCEDENTE, roles = {FISCAL_CONCEDENTE, GESTOR_CONVENIO_CONCEDENTE, GESTOR_FINANCEIRO_CONCEDENTE, FISCAL_ACOMPANHAMENTO, TECNICO_TERCEIRO, OPERACIONAL_CONCEDENTE})
    @RolesAllowed(profile = MANDATARIA, roles = {AGENTE_ACOMPANHAMENTO_INSTITUICAO_MANDATARIA})
    public DefaultResponse<MedicaoDTO> alterarConcedenteMandataria(VistoriaExtraDTO vistoriaExtra, @PathParam("idMedicao") Long idMedicao) {

        return success(medicaoBC.alterarConcedenteMandataria(vistoriaExtra, idMedicao));
    }

    @PUT
    @Path("/medicoes/{idMedicao}/envio")
    @Operation(summary = "Envia a Medição para o Convenente")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(APPLICATION_JSON_UTF8)
    @RequiresAuthorization(to = INVOKE_METHOD)
    @AuthorityContextSuppliedBy(provider = MedicaoAuthorityContextProvider.class, param = "idMedicao")
    @PermissionsAllowed(profile = EMPRESA, permissions = ENVIAR_MEDICAO_CONVENENTE)
    public DefaultResponse<MedicaoDTO> enviar(MedicaoDTO medicao, @PathParam("idMedicao") Long idMedicao) {

    	medicao.setId(idMedicao);
        return success(medicaoBC.enviarConvenente(medicao));
    }
    
    @PUT
    @Path("/medicoes/{idMedicao}/inicioateste")
    @Operation(summary = "Inicia o Ateste de uma Medição.")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(APPLICATION_JSON_UTF8)
    @RequiresAuthorization(to = INVOKE_METHOD)
    @AuthorityContextSuppliedBy(provider = MedicaoAuthorityContextProvider.class, param = "idMedicao")
    @RolesAllowed(profile = PROPONENTE_CONVENENTE, roles = {FISCAL_CONVENENTE, GESTOR_CONVENIO_CONVENENTE, GESTOR_FINANCEIRO_CONVENENTE, OPERADOR_FINANCEIRO_CONVENENTE})
    public DefaultResponse<MedicaoDTO> iniciarAteste (@PathParam("idMedicao") Long idMedicao) {

        MedicaoDTO medicao = medicaoBC.iniciarAteste(idMedicao);

        return success(medicao);
    }
    
    @PUT
    @Path("/medicoes/{idMedicao}/inicioanalise")
    @Operation(summary = "Inicia a Análise de uma Medição pelo Concedente/Mandatária.")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(APPLICATION_JSON_UTF8)
    @RequiresAuthorization(to = INVOKE_METHOD)
    @AuthorityContextSuppliedBy(provider = MedicaoAuthorityContextProvider.class, param = "idMedicao")
    @RolesAllowed(profile = CONCEDENTE, roles = {FISCAL_CONCEDENTE, GESTOR_CONVENIO_CONCEDENTE, GESTOR_FINANCEIRO_CONCEDENTE, FISCAL_ACOMPANHAMENTO, TECNICO_TERCEIRO, OPERACIONAL_CONCEDENTE})
    @RolesAllowed(profile = MANDATARIA, roles = {Role.AGENTE_ACOMPANHAMENTO_INSTITUICAO_MANDATARIA})
    public DefaultResponse<MedicaoDTO> iniciarAnalise (@PathParam("idMedicao") Long idMedicao) {

        MedicaoDTO medicao = medicaoBC.iniciarAnalise(idMedicao);

        return success(medicao);
    }

    @PUT
    @Path("/medicoes/{idMedicao}/cancelamentoenvioconvenente")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(APPLICATION_JSON_UTF8)
    @RequiresAuthorization(to = INVOKE_METHOD)
    @AuthorityContextSuppliedBy(provider = MedicaoAuthorityContextProvider.class, param = "idMedicao")
    @PermissionsAllowed(profile = EMPRESA, permissions = CANCELAR_ENVIO_MEDICAO_CONVENENTE)
    public DefaultResponse<String> cancelarEnvioConvenente(@PathParam("idMedicao") Long idMedicao) {

        medicaoBC.cancelarEnvioConvenente(idMedicao);

        return success("ok");
    }

    /**
     * Excluir Medicao
     */
    @DELETE
    @Path("/medicoes/{idMedicao}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Exclusão da medição")
    @RequiresAuthorization(to = INVOKE_METHOD)
    @AuthorityContextSuppliedBy(provider = MedicaoAuthorityContextProvider.class, param = "idMedicao")
    @PermissionsAllowed(profile = EMPRESA, permissions = EXCLUIR_MEDICAO)
    @RolesAllowed(profile = PROPONENTE_CONVENENTE, roles = { GESTOR_CONVENIO_CONVENENTE, GESTOR_FINANCEIRO_CONVENENTE,
            OPERADOR_FINANCEIRO_CONVENENTE, FISCAL_CONVENENTE })
    @RolesAllowed(profile = CONCEDENTE, roles = { ADMINISTRADOR_SISTEMA, ADMINISTRADOR_SISTEMA_ORGAO_EXTERNO })
    public DefaultResponse<String> excluirMedicao(@PathParam("idMedicao") Long idMedicao) {

        medicaoBC.excluirMedicao(idMedicao);

        return success("ok");

    }

	@PUT
	@Path("/medicoes/{idMedicao}/ateste")
	@Produces(APPLICATION_JSON_UTF8)
	@RequiresAuthorization(to = INVOKE_METHOD)
	@AuthorityContextSuppliedBy(provider = MedicaoAuthorityContextProvider.class, param = "idMedicao")
	@RolesAllowed(profile = PROPONENTE_CONVENENTE, roles = { FISCAL_CONVENENTE, GESTOR_CONVENIO_CONVENENTE,
			GESTOR_FINANCEIRO_CONVENENTE, OPERADOR_FINANCEIRO_CONVENENTE })
	public DefaultResponse<MedicaoDTO> atestar(@PathParam("idMedicao") Long idMedicao) {

		return success(medicaoBC.atestar(idMedicao));
	}

	@PUT
	@Path("/medicoes/{idMedicao}/aceite")
	@Produces(APPLICATION_JSON_UTF8)
	@RequiresAuthorization(to = INVOKE_METHOD)
	@AuthorityContextSuppliedBy(provider = MedicaoAuthorityContextProvider.class, param = "idMedicao")
    @RolesAllowed(profile = CONCEDENTE, roles = {FISCAL_CONCEDENTE, GESTOR_CONVENIO_CONCEDENTE, GESTOR_FINANCEIRO_CONCEDENTE, FISCAL_ACOMPANHAMENTO, TECNICO_TERCEIRO})
    @RolesAllowed(profile = MANDATARIA, roles = {AGENTE_ACOMPANHAMENTO_INSTITUICAO_MANDATARIA})
	public DefaultResponse<MedicaoDTO> aceitar(VistoriaExtraDTO vistoriaExtraDTO, @PathParam("idMedicao") Long idMedicao) {

		return success(medicaoBC.aceitar(vistoriaExtraDTO, idMedicao));
	}
	
	@PUT
	@Path("/medicoes/{idMedicao}/cancelaaceite")
	@Produces(APPLICATION_JSON_UTF8)
	@RequiresAuthorization(to = INVOKE_METHOD)
	@AuthorityContextSuppliedBy(provider = MedicaoAuthorityContextProvider.class, param = "idMedicao")
    @RolesAllowed(profile = CONCEDENTE, roles = {FISCAL_CONCEDENTE, GESTOR_CONVENIO_CONCEDENTE, GESTOR_FINANCEIRO_CONCEDENTE, FISCAL_ACOMPANHAMENTO, TECNICO_TERCEIRO})
    @RolesAllowed(profile = MANDATARIA, roles = {AGENTE_ACOMPANHAMENTO_INSTITUICAO_MANDATARIA})
	public DefaultResponse<String> cancelarAceite(@PathParam("idMedicao") Long idMedicao) {
		
		medicaoBC.cancelarAceite(idMedicao);
		return success("ok");
	}	
	
    @GET
    @Path("/medicoes/{idMedicao}/permitecomplementacao")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresAuthorization(to = INVOKE_METHOD)
    @AuthorityContextSuppliedBy(provider = MedicaoAuthorityContextProvider.class, param = "idMedicao")
    @RolesAllowed(profile = PROPONENTE_CONVENENTE, roles = { FISCAL_CONVENENTE, GESTOR_CONVENIO_CONVENENTE,
            GESTOR_FINANCEIRO_CONVENENTE, OPERADOR_FINANCEIRO_CONVENENTE })
    @RolesAllowed(profile = CONCEDENTE, roles = { FISCAL_CONCEDENTE, GESTOR_CONVENIO_CONCEDENTE,
            GESTOR_FINANCEIRO_CONCEDENTE, OPERACIONAL_CONCEDENTE, FISCAL_ACOMPANHAMENTO, TECNICO_TERCEIRO,
            ADMINISTRADOR_SISTEMA, ADMINISTRADOR_SISTEMA_ORGAO_EXTERNO })
    @RolesAllowed(profile = MANDATARIA, roles = { AGENTE_ACOMPANHAMENTO_INSTITUICAO_MANDATARIA })
    public DefaultResponse<Boolean> verificarMedicaoPermiteComplementacao(@PathParam("idMedicao") Long idMedicao) {

        return success(medicaoBC.verificarMedicaoPermiteComplementacao(idMedicao));
    }

	@PUT
	@Path("/medicoes/{idMedicao}/complementacaoempresa")
	@Produces(APPLICATION_JSON_UTF8)
	@RequiresAuthorization(to = INVOKE_METHOD)
	@AuthorityContextSuppliedBy(provider = MedicaoAuthorityContextProvider.class, param = "idMedicao")
	@RolesAllowed(profile = PROPONENTE_CONVENENTE, roles = { FISCAL_CONVENENTE, GESTOR_CONVENIO_CONVENENTE,
			GESTOR_FINANCEIRO_CONVENENTE, OPERADOR_FINANCEIRO_CONVENENTE })
	public DefaultResponse<MedicaoDTO> solicitarComplementacaoEmpresa(@PathParam("idMedicao") Long idMedicao) {

		return success(medicaoBC.solicitarComplementacaoEmpresa(idMedicao));
	}
	
	@PUT
	@Path("/medicoes/{idMedicao}/complementacaoconvenente")
	@Produces(APPLICATION_JSON_UTF8)
	@RequiresAuthorization(to = INVOKE_METHOD)
	@AuthorityContextSuppliedBy(provider = MedicaoAuthorityContextProvider.class, param = "idMedicao")
    @RolesAllowed(profile = CONCEDENTE, roles = {OPERACIONAL_CONCEDENTE, FISCAL_CONCEDENTE, GESTOR_CONVENIO_CONCEDENTE, GESTOR_FINANCEIRO_CONCEDENTE, FISCAL_ACOMPANHAMENTO, TECNICO_TERCEIRO, ADMINISTRADOR_SISTEMA, ADMINISTRADOR_SISTEMA_ORGAO_EXTERNO})
    @RolesAllowed(profile = MANDATARIA, roles = {AGENTE_ACOMPANHAMENTO_INSTITUICAO_MANDATARIA})
	public DefaultResponse<MedicaoDTO> solicitarComplementacaoConvenente(VistoriaExtraDTO vistoriaExtraDTO, @PathParam("idMedicao") Long idMedicao) {

		return success(medicaoBC.solicitarComplementacaoConvenente(vistoriaExtraDTO, idMedicao));
	}
	
	@PUT
    @Path("/medicoes/{idMedicao}/iniciocomplementacao")
	@Operation(summary = "Inicia a Complementação de uma Medição pela Empresa ou pelo Convenente.")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(APPLICATION_JSON_UTF8)
    @RequiresAuthorization(to = INVOKE_METHOD)
    @AuthorityContextSuppliedBy(provider = MedicaoAuthorityContextProvider.class, param = "idMedicao")
	@PermissionsAllowed(profile = EMPRESA, permissions = INCLUIR_MEDICAO)
	@RolesAllowed(profile = PROPONENTE_CONVENENTE, roles = { FISCAL_CONVENENTE, GESTOR_CONVENIO_CONVENENTE,
			GESTOR_FINANCEIRO_CONVENENTE, OPERADOR_FINANCEIRO_CONVENENTE })
    public DefaultResponse<MedicaoDTO> iniciarComplementacao(@PathParam("idMedicao") Long idMedicao) {

        MedicaoDTO medicao = medicaoBC.iniciarComplementacao(idMedicao);

        return success(medicao);
    }

	@PUT
	@Path("/medicoes/{idMedicao}/cancelamentoenvioconcedente")
	@Produces(APPLICATION_JSON_UTF8)
	@RequiresAuthorization(to = INVOKE_METHOD)
	@AuthorityContextSuppliedBy(provider = MedicaoAuthorityContextProvider.class, param = "idMedicao")
	@RolesAllowed(profile = CONCEDENTE, roles = { ADMINISTRADOR_SISTEMA, ADMINISTRADOR_SISTEMA_ORGAO_EXTERNO })
	public DefaultResponse<String> cancelarEnvioConcedente(@PathParam("idMedicao") Long idMedicao) {

		medicaoBC.cancelarEnvioConcedente(idMedicao);

		return success("ok");

	}
	
	/**
	 * Cancelar Envio para Complementação da Empresa ou do Convenente
	 */
	@PUT
	@Path("/medicoes/{idMedicao}/cancelamentoenviocomplementacao")
	@Produces(APPLICATION_JSON_UTF8)
	@RequiresAuthorization(to = INVOKE_METHOD)
	@AuthorityContextSuppliedBy(provider = MedicaoAuthorityContextProvider.class, param = "idMedicao")
	@RolesAllowed(profile = PROPONENTE_CONVENENTE, roles = { FISCAL_CONVENENTE, GESTOR_CONVENIO_CONVENENTE,
			GESTOR_FINANCEIRO_CONVENENTE, OPERADOR_FINANCEIRO_CONVENENTE })
    @RolesAllowed(profile = CONCEDENTE, roles = {OPERACIONAL_CONCEDENTE, FISCAL_CONCEDENTE, GESTOR_CONVENIO_CONCEDENTE, GESTOR_FINANCEIRO_CONCEDENTE, FISCAL_ACOMPANHAMENTO, TECNICO_TERCEIRO, ADMINISTRADOR_SISTEMA, ADMINISTRADOR_SISTEMA_ORGAO_EXTERNO})
    @RolesAllowed(profile = MANDATARIA, roles = {AGENTE_ACOMPANHAMENTO_INSTITUICAO_MANDATARIA})
	public DefaultResponse<String> cancelarEnvioParaComplementacao(@PathParam("idMedicao") Long idMedicao) {

		medicaoBC.cancelarEnvioParaComplementacao(idMedicao);

		return success("ok");

	}
	
    @GET
    @Path("/medicoes/{idMedicaoAgrupadora}/listaagrupadas")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresAuthorization(to = VIEW_RESPONSE_SENSITIVE_DATA)
    @AuthorityContextSuppliedBy(provider = MedicaoAuthorityContextProvider.class, param = "idMedicaoAgrupadora")
    @AnyPermissionAllowed(profile = EMPRESA)
    @ProfilesAllowed({ CONCEDENTE, PROPONENTE_CONVENENTE, MANDATARIA, USUARIO_SICONV })    
    public DefaultResponse<List<MedicaoAgrupadaDTO>> listarMedicoesAgrupadas(@PathParam("idMedicaoAgrupadora") Long idMedicaoAgrupadora, 
    		@QueryParam ("submetasPreenchidas") Boolean submetasPreenchidas) {

        List<MedicaoAgrupadaDTO> medicoes = medicaoBC.listarMedicoesAgrupadas(idMedicaoAgrupadora, submetasPreenchidas);
        
        return success(medicoes);

    }
	
}
