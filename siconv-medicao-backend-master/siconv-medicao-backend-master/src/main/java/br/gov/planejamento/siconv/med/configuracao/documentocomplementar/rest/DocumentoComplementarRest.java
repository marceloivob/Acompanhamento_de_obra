package br.gov.planejamento.siconv.med.configuracao.documentocomplementar.rest;

import static br.gov.planejamento.siconv.med.infra.rest.response.ResponseHelper.success;
import static br.gov.planejamento.siconv.med.infra.security.domain.AuthorizationScope.INVOKE_METHOD;
import static br.gov.planejamento.siconv.med.infra.security.domain.AuthorizationScope.VIEW_RESPONSE_SENSITIVE_DATA;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.CONCEDENTE;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.EMPRESA;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.MANDATARIA;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.PROPONENTE_CONVENENTE;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.USUARIO_SICONV;
import static br.gov.planejamento.siconv.med.infra.security.domain.Role.ADMINISTRADOR_SISTEMA;
import static br.gov.planejamento.siconv.med.infra.security.domain.Role.ADMINISTRADOR_SISTEMA_ORGAO_EXTERNO;
import static br.gov.planejamento.siconv.med.infra.security.domain.Role.FISCAL_CONVENENTE;
import static br.gov.planejamento.siconv.med.infra.security.domain.Role.GESTOR_CONVENIO_CONVENENTE;
import static br.gov.planejamento.siconv.med.infra.security.domain.Role.GESTOR_FINANCEIRO_CONVENENTE;
import static br.gov.planejamento.siconv.med.infra.security.domain.Role.OPERADOR_FINANCEIRO_CONVENENTE;
import static br.gov.planejamento.siconv.med.infra.util.ApplicationProperties.APPLICATION_JSON_UTF8;

import java.util.List;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import br.gov.planejamento.siconv.med.configuracao.documentocomplementar.business.DocumentoComplementarBC;
import br.gov.planejamento.siconv.med.configuracao.documentocomplementar.entity.dto.DocumentoComplementarDTO;
import br.gov.planejamento.siconv.med.infra.rest.response.ResponseHelper.DefaultResponse;
import br.gov.planejamento.siconv.med.infra.security.annotation.AnyPermissionAllowed;
import br.gov.planejamento.siconv.med.infra.security.annotation.AuthorityContextSuppliedBy;
import br.gov.planejamento.siconv.med.infra.security.annotation.ProfilesAllowed;
import br.gov.planejamento.siconv.med.infra.security.annotation.RequiresAuthorization;
import br.gov.planejamento.siconv.med.infra.security.annotation.RolesAllowed;
import br.gov.planejamento.siconv.med.infra.security.provider.ContratoAuthorityContextProvider;
import br.gov.planejamento.siconv.med.infra.security.provider.DocumentoComplementarAuthorityContextProvider;
import br.gov.planejamento.siconv.med.infra.util.FileUploadUtil;
import br.gov.planejamento.siconv.med.infra.util.FileUploadUtil.FileUpload;
import br.gov.planejamento.siconv.med.infra.util.MultipartFormDataInputHelper;

@Path("/")
public class DocumentoComplementarRest {

    @Inject
    private DocumentoComplementarBC documentocomplementarBC;

    @Inject
    private FileUploadUtil fileUploadUtil;

    @GET
    @Path("/contratos/{idContratoSiconv}/documentoscomplementares/")
    @Produces(APPLICATION_JSON_UTF8)
    @RequiresAuthorization(to = VIEW_RESPONSE_SENSITIVE_DATA)
    @AuthorityContextSuppliedBy(provider = ContratoAuthorityContextProvider.class, param = "idContratoSiconv")
    @AnyPermissionAllowed(profile = EMPRESA)
    @ProfilesAllowed({ CONCEDENTE, PROPONENTE_CONVENENTE, MANDATARIA, USUARIO_SICONV })
    public DefaultResponse<Object> listarDocumentosComplementares(
            @PathParam("idContratoSiconv") Long idContratoSiconv) {

        return success(documentocomplementarBC.listarDocumentosComplementares(idContratoSiconv));
    }

    @GET
    @Path("/documentoscomplementares/{idDocumentoComplementar}")
    @Produces(APPLICATION_JSON_UTF8)
    @RequiresAuthorization(to = VIEW_RESPONSE_SENSITIVE_DATA)
    @AuthorityContextSuppliedBy(provider = DocumentoComplementarAuthorityContextProvider.class, param = "idDocumentoComplementar")
    @AnyPermissionAllowed(profile = EMPRESA)
    @ProfilesAllowed({ CONCEDENTE, PROPONENTE_CONVENENTE, MANDATARIA, USUARIO_SICONV })
    public DefaultResponse<DocumentoComplementarDTO> consultarDocumentoComplementar(
            @PathParam("idDocumentoComplementar") Long idDocumentoComplementar) {

        return success(documentocomplementarBC.consultarDocumentoComplementar(idDocumentoComplementar));
    }

    @POST
    @Path("/contratos/{idContratoSiconv}/documentoscomplementares")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(APPLICATION_JSON_UTF8)
    @RequiresAuthorization(to = INVOKE_METHOD)
    @AuthorityContextSuppliedBy(provider = ContratoAuthorityContextProvider.class, param = "idContratoSiconv")
    @RolesAllowed(profile = PROPONENTE_CONVENENTE, roles = { GESTOR_CONVENIO_CONVENENTE, GESTOR_FINANCEIRO_CONVENENTE,
            OPERADOR_FINANCEIRO_CONVENENTE, FISCAL_CONVENENTE })
    public DefaultResponse<Long> incluirDocumentoComplementar(@PathParam("idContratoSiconv") Long idContratoSiconv,
            MultipartFormDataInput multipart) {

        return success(documentocomplementarBC.incluirDocumentoComplementar(idContratoSiconv,
                parseDocumentoComplementarDTO(multipart)));
    }

    private DocumentoComplementarDTO parseDocumentoComplementarDTO(MultipartFormDataInput multipart) {

        DocumentoComplementarDTO dto = new DocumentoComplementarDTO();

        try {

            MultipartFormDataInputHelper multipartHelper = new MultipartFormDataInputHelper(multipart);

            dto = multipartHelper.getJsonType("documentoComplementarDTO",
                    typeFactory -> typeFactory.constructType(DocumentoComplementarDTO.class));

            List<FileUpload> arquivos = fileUploadUtil.process(multipart);

            if (!arquivos.isEmpty()) {
                FileUpload arquivo = arquivos.get(0);
                dto.setArquivo(arquivo.getContent());
                dto.setNmArquivo(arquivo.getFileName());
            }

        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Falha ao realizar o parsing do Documento Complementar a partir do multipartFormDataInput.", e);
        }

        return dto;
    }

    /**
     * Excluir um Documento Complementar do Contrato
     */
    @DELETE
    @Path("/documentoscomplementares/{idDocumentoComplementar}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Excluir um Documento Complementar do Contrato")
    @RequiresAuthorization(to = INVOKE_METHOD)
    @AuthorityContextSuppliedBy(provider = DocumentoComplementarAuthorityContextProvider.class, param = "idDocumentoComplementar")
    @RolesAllowed(profile = PROPONENTE_CONVENENTE, roles = { GESTOR_CONVENIO_CONVENENTE, GESTOR_FINANCEIRO_CONVENENTE,
            OPERADOR_FINANCEIRO_CONVENENTE, FISCAL_CONVENENTE })
    public DefaultResponse<String> excluirDocumentoComplementarContrato(
            @PathParam("idDocumentoComplementar") Long idDocumentoComplementar) {

        documentocomplementarBC.excluirDocumentoComplementarContrato(idDocumentoComplementar);

        return success("ok");

    }

    @PUT
    @Path("/documentoscomplementares/{idDocumentoComplementar}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(APPLICATION_JSON_UTF8)
    @RequiresAuthorization(to = INVOKE_METHOD)
    @AuthorityContextSuppliedBy(provider = DocumentoComplementarAuthorityContextProvider.class, param = "idDocumentoComplementar")
    @RolesAllowed(profile = PROPONENTE_CONVENENTE, roles = { GESTOR_CONVENIO_CONVENENTE, GESTOR_FINANCEIRO_CONVENENTE,
            OPERADOR_FINANCEIRO_CONVENENTE, FISCAL_CONVENENTE })
    public DefaultResponse<String> alterarDocumentoComplementar(
            @PathParam("idDocumentoComplementar") Long idDocumentoComplementar, MultipartFormDataInput multipart) {

        DocumentoComplementarDTO documentoInput = parseDocumentoComplementarDTO(multipart);
        documentoInput.setId(idDocumentoComplementar);

        documentocomplementarBC.alterarDocumentoComplementar(documentoInput);

        return success("ok");
    }
    
    @PUT
    @Path("/documentoscomplementares/{idDocumentoComplementar}/bloqueado")
	@Produces(APPLICATION_JSON_UTF8)
    @RequiresAuthorization(to = INVOKE_METHOD)
    @AuthorityContextSuppliedBy(provider = DocumentoComplementarAuthorityContextProvider.class, param = "idDocumentoComplementar")
    @RolesAllowed(profile = CONCEDENTE, roles = {ADMINISTRADOR_SISTEMA, ADMINISTRADOR_SISTEMA_ORGAO_EXTERNO})
	public DefaultResponse<String> setarBloqueioDocumentoComplementar(@PathParam("idDocumentoComplementar") Long idDocumentoComplementar, @NotNull Boolean bloqueio) {

		documentocomplementarBC.setarBloqueioDocumentoComplementar(idDocumentoComplementar, bloqueio);

		return success("ok");

	}
    
}
