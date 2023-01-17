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

import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.business.ContratoResponsavelTecnicoSocialBC;
import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.dto.ContratoResponsavelTecnicoSocialDTO;
import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.dto.ResponsavelTecnicoElegivelDTO;
import br.gov.planejamento.siconv.med.infra.rest.response.ResponseHelper.DefaultResponse;
import br.gov.planejamento.siconv.med.infra.security.annotation.AnyPermissionAllowed;
import br.gov.planejamento.siconv.med.infra.security.annotation.AuthorityContextSuppliedBy;
import br.gov.planejamento.siconv.med.infra.security.annotation.ProfilesAllowed;
import br.gov.planejamento.siconv.med.infra.security.annotation.RequiresAuthorization;
import br.gov.planejamento.siconv.med.infra.security.annotation.RolesAllowed;
import br.gov.planejamento.siconv.med.infra.security.provider.ContratoAuthorityContextProvider;
import br.gov.planejamento.siconv.med.infra.security.provider.ContratoRespTecnicoSocialAuthorityContextProvider;
import br.gov.planejamento.siconv.med.infra.util.FileUploadUtil;
import br.gov.planejamento.siconv.med.infra.util.FileUploadUtil.FileUpload;
import br.gov.planejamento.siconv.med.infra.util.MultipartFormDataInputHelper;

@Path("/")
public class ContratoResponsavelTecnicoSocialRest {

    @Inject
    private ContratoResponsavelTecnicoSocialBC contratoRtSocialBC;

    @Inject
    private FileUploadUtil fileUploadUtil;

    @GET
    @Path("/contratos/{idContratoSiconv}/responsaveissocial")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresAuthorization(to = VIEW_RESPONSE_SENSITIVE_DATA)
    @AuthorityContextSuppliedBy(provider = ContratoAuthorityContextProvider.class, param = "idContratoSiconv")
    @AnyPermissionAllowed(profile = EMPRESA)
    @ProfilesAllowed({ CONCEDENTE, PROPONENTE_CONVENENTE, MANDATARIA, USUARIO_SICONV })
    public DefaultResponse<List<ContratoResponsavelTecnicoSocialDTO>> listarResponsavelTecnicoSocialPorContrato(
            @PathParam("idContratoSiconv") Long idContratoSiconv) {

        List<ContratoResponsavelTecnicoSocialDTO> listaContratoResposavelTecnicoSocial = this.contratoRtSocialBC
                .listarResponsavelTecnicoSocialPorContrato(idContratoSiconv);

        return success(listaContratoResposavelTecnicoSocial);
    }

    @POST
    @Path("/contratos/{idContratoSiconv}/responsaveissocial")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(APPLICATION_JSON_UTF8)
    @RequiresAuthorization(to = INVOKE_METHOD)
    @AuthorityContextSuppliedBy(provider = ContratoAuthorityContextProvider.class, param = "idContratoSiconv")
    @RolesAllowed(profile = PROPONENTE_CONVENENTE, roles = { GESTOR_CONVENIO_CONVENENTE, GESTOR_FINANCEIRO_CONVENENTE,
            OPERADOR_FINANCEIRO_CONVENENTE, FISCAL_CONVENENTE })
    public DefaultResponse<ContratoResponsavelTecnicoSocialDTO> incluir(
            @PathParam("idContratoSiconv") Long idContratoSiconv, MultipartFormDataInput multipart) {

        ContratoResponsavelTecnicoSocialDTO rtSocialDTO = parseDTO(multipart);
        contratoRtSocialBC.salvar(rtSocialDTO, idContratoSiconv);

        return success(rtSocialDTO);

    }

    @GET
    @Path("/contratos/{idContratoSiconv}/responsaveissocial/elegivel")
    @Produces(APPLICATION_JSON_UTF8)
    @RequiresAuthorization(to = INVOKE_METHOD)
    @AuthorityContextSuppliedBy(provider = ContratoAuthorityContextProvider.class, param = "idContratoSiconv")
    @RolesAllowed(profile = PROPONENTE_CONVENENTE, roles = { GESTOR_CONVENIO_CONVENENTE, GESTOR_FINANCEIRO_CONVENENTE,
            OPERADOR_FINANCEIRO_CONVENENTE, FISCAL_CONVENENTE })
    public DefaultResponse<ResponsavelTecnicoElegivelDTO> consultarResponsavelTecnicoElegivel(
            @PathParam("idContratoSiconv") Long idContratoSiconv, @QueryParam("cpf") String cpf,
            @QueryParam("tipo") String tipo) {

        ResponsavelTecnicoElegivelDTO rtElegivel = this.contratoRtSocialBC
                .consultarResponsavelTecnicoElegivel(idContratoSiconv, cpf, tipo);

        return success(rtElegivel);
    }

    @GET
    @Path("/responsaveissocial/{idResponsavelContrato}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresAuthorization(to = VIEW_RESPONSE_SENSITIVE_DATA)
    @AuthorityContextSuppliedBy(provider = ContratoRespTecnicoSocialAuthorityContextProvider.class, param = "idResponsavelContrato")
    @AnyPermissionAllowed(profile = EMPRESA)
    @ProfilesAllowed({ CONCEDENTE, PROPONENTE_CONVENENTE, MANDATARIA, USUARIO_SICONV })
    public DefaultResponse<ContratoResponsavelTecnicoSocialDTO> consultarRTSocialPorId(
            @PathParam("idResponsavelContrato") Long idResponsavelContrato) {

        return success(this.contratoRtSocialBC.consultarContratoResponsavelTecnicoSocialPorId(idResponsavelContrato));
    }

    @PUT
    @Path("/responsaveissocial/{idResponsavelContrato}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(APPLICATION_JSON_UTF8)
    @RequiresAuthorization(to = INVOKE_METHOD)
    @AuthorityContextSuppliedBy(provider = ContratoRespTecnicoSocialAuthorityContextProvider.class, param = "idResponsavelContrato")
    @RolesAllowed(profile = PROPONENTE_CONVENENTE, roles = { GESTOR_CONVENIO_CONVENENTE, GESTOR_FINANCEIRO_CONVENENTE,
            OPERADOR_FINANCEIRO_CONVENENTE, FISCAL_CONVENENTE })
    public DefaultResponse<String> alterar(MultipartFormDataInput multipart,
            @PathParam("idResponsavelContrato") Long idResponsavelContrato) {

        ContratoResponsavelTecnicoSocialDTO rtSocialDTO = parseDTO(multipart);
        rtSocialDTO.setId(idResponsavelContrato);

        contratoRtSocialBC.alterar(rtSocialDTO);

        return success("ok");
    }

    @DELETE
    @Path("/responsaveissocial/{idResponsavelContrato}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresAuthorization(to = INVOKE_METHOD)
    @AuthorityContextSuppliedBy(provider = ContratoRespTecnicoSocialAuthorityContextProvider.class, param = "idResponsavelContrato")
    @RolesAllowed(profile = PROPONENTE_CONVENENTE, roles = { GESTOR_CONVENIO_CONVENENTE, GESTOR_FINANCEIRO_CONVENENTE,
            OPERADOR_FINANCEIRO_CONVENENTE, FISCAL_CONVENENTE })
    public DefaultResponse<String> excluir(@PathParam("idResponsavelContrato") Long idResponsavelContrato) {

        this.contratoRtSocialBC.excluir(idResponsavelContrato);

        return success("ok");

    }

    @PUT
    @Path("/responsaveissocial/{idResponsavelContrato}/inativacao")
    @Produces(APPLICATION_JSON_UTF8)
    @RequiresAuthorization(to = INVOKE_METHOD)
    @AuthorityContextSuppliedBy(provider = ContratoRespTecnicoSocialAuthorityContextProvider.class, param = "idResponsavelContrato")
    @RolesAllowed(profile = PROPONENTE_CONVENENTE, roles = { GESTOR_CONVENIO_CONVENENTE, GESTOR_FINANCEIRO_CONVENENTE,
            OPERADOR_FINANCEIRO_CONVENENTE, FISCAL_CONVENENTE })
    public DefaultResponse<String> inativar(@PathParam("idResponsavelContrato") Long idResponsavelContrato) {

        contratoRtSocialBC.inativar(idResponsavelContrato);

        return success("ok");
    }

    private ContratoResponsavelTecnicoSocialDTO parseDTO(MultipartFormDataInput multipart) {

        ContratoResponsavelTecnicoSocialDTO dto = new ContratoResponsavelTecnicoSocialDTO();

        try {

            MultipartFormDataInputHelper multipartHelper = new MultipartFormDataInputHelper(multipart);

            dto = multipartHelper.getJsonType("contratoResponsavelTecnicoSocialDTO",
                    typeFactory -> typeFactory.constructType(ContratoResponsavelTecnicoSocialDTO.class));

            List<FileUpload> curriculo = fileUploadUtil.process(multipart);

            if (!curriculo.isEmpty()) {
                FileUpload arquivo = curriculo.get(0);
                dto.setArquivo(arquivo.getContent());
                dto.setNomeArquivo(arquivo.getFileName());
            }

        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Falha ao realizar o parsing do Responsável Técnico Social a partir do multipartFormDataInput.", e);
        }

        return dto;
    }

}
