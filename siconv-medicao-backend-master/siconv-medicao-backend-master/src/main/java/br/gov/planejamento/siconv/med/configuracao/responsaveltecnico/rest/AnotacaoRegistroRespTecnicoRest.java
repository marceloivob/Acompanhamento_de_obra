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
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.business.AnotacaoRegistroRespTecnicoBC;
import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.dto.AnotacaoRegistroRespTecnicoDTO;
import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.dto.TipoResponsavelTecnicoEnum;
import br.gov.planejamento.siconv.med.infra.rest.response.ResponseHelper.DefaultResponse;
import br.gov.planejamento.siconv.med.infra.security.annotation.AnyPermissionAllowed;
import br.gov.planejamento.siconv.med.infra.security.annotation.AuthorityContextSuppliedBy;
import br.gov.planejamento.siconv.med.infra.security.annotation.ProfilesAllowed;
import br.gov.planejamento.siconv.med.infra.security.annotation.RequiresAuthorization;
import br.gov.planejamento.siconv.med.infra.security.annotation.RolesAllowed;
import br.gov.planejamento.siconv.med.infra.security.provider.AnotacaoAuthorityContextProvider;
import br.gov.planejamento.siconv.med.infra.security.provider.ContratoAuthorityContextProvider;
import br.gov.planejamento.siconv.med.infra.util.FileUploadUtil;
import br.gov.planejamento.siconv.med.infra.util.FileUploadUtil.FileUpload;
import br.gov.planejamento.siconv.med.infra.util.MultipartFormDataInputHelper;
import br.gov.planejamento.siconv.med.medicao.entity.dto.SubmetaVrplDTO;

@Path("/")
public class AnotacaoRegistroRespTecnicoRest {

    @Inject
    private AnotacaoRegistroRespTecnicoBC bc;

    @Inject
    private FileUploadUtil fileUploadUtil;

    @GET
    @Path("/contratos/{idContratoSiconv}/arts/")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresAuthorization(to = VIEW_RESPONSE_SENSITIVE_DATA)
    @AuthorityContextSuppliedBy(provider = ContratoAuthorityContextProvider.class, param = "idContratoSiconv")
    @AnyPermissionAllowed(profile = EMPRESA)
    @ProfilesAllowed({ CONCEDENTE, PROPONENTE_CONVENENTE, MANDATARIA, USUARIO_SICONV })
    public DefaultResponse<List<AnotacaoRegistroRespTecnicoDTO>> listarAnotacoes(
            @PathParam("idContratoSiconv") Long idContratoSiconv) {

        return success(bc.listarAnotacoes(idContratoSiconv));
    }

    @POST
    @Path("/contratos/{idContratoSiconv}/arts")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(APPLICATION_JSON_UTF8)
    @RequiresAuthorization(to = INVOKE_METHOD)
    @AuthorityContextSuppliedBy(provider = ContratoAuthorityContextProvider.class, param = "idContratoSiconv")
    @RolesAllowed(profile = PROPONENTE_CONVENENTE, roles = { GESTOR_CONVENIO_CONVENENTE, GESTOR_FINANCEIRO_CONVENENTE,
            OPERADOR_FINANCEIRO_CONVENENTE, FISCAL_CONVENENTE })
    public DefaultResponse<Long> incluirAnotacao(@PathParam("idContratoSiconv") Long idContratoSiconv,
            MultipartFormDataInput multipart) {

        return success(bc.incluirAnotacao(idContratoSiconv, parseAnotacaoDTO(multipart)));
    }

    @DELETE
    @Path("/arts/{idAnotacao}")
    @Produces(APPLICATION_JSON_UTF8)
    @RequiresAuthorization(to = INVOKE_METHOD)
    @AuthorityContextSuppliedBy(provider = AnotacaoAuthorityContextProvider.class, param = "idAnotacao")
    @RolesAllowed(profile = PROPONENTE_CONVENENTE, roles = { GESTOR_CONVENIO_CONVENENTE, GESTOR_FINANCEIRO_CONVENENTE,
            OPERADOR_FINANCEIRO_CONVENENTE, FISCAL_CONVENENTE })
    public DefaultResponse<String> excluirAnotacao(@PathParam("idAnotacao") Long idAnotacao) {

        bc.excluirAnotacao(idAnotacao);

        return success("ok");
    }

    @PUT
    @Path("/arts/{idAnotacao}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(APPLICATION_JSON_UTF8)
    @RequiresAuthorization(to = INVOKE_METHOD)
    @AuthorityContextSuppliedBy(provider = AnotacaoAuthorityContextProvider.class, param = "idAnotacao")
    @RolesAllowed(profile = PROPONENTE_CONVENENTE, roles = { GESTOR_CONVENIO_CONVENENTE, GESTOR_FINANCEIRO_CONVENENTE,
            OPERADOR_FINANCEIRO_CONVENENTE, FISCAL_CONVENENTE })
    public DefaultResponse<String> alterarAnotacao(@PathParam("idAnotacao") Long idAnotacao,
            MultipartFormDataInput multipart) {

        AnotacaoRegistroRespTecnicoDTO anotacaoInput = parseAnotacaoDTO(multipart);
        anotacaoInput.setId(idAnotacao);

        bc.alterarAnotacao(anotacaoInput);

        return success("ok");
    }

    @PUT
    @Path("/arts/{idAnotacao}/inativacao")
    @Produces(APPLICATION_JSON_UTF8)
    @RequiresAuthorization(to = INVOKE_METHOD)
    @AuthorityContextSuppliedBy(provider = AnotacaoAuthorityContextProvider.class, param = "idAnotacao")
    @RolesAllowed(profile = PROPONENTE_CONVENENTE, roles = { GESTOR_CONVENIO_CONVENENTE, GESTOR_FINANCEIRO_CONVENENTE,
            OPERADOR_FINANCEIRO_CONVENENTE, FISCAL_CONVENENTE })
    public DefaultResponse<String> inativarAnotacao(@PathParam("idAnotacao") Long idAnotacao) {

        bc.inativarAnotacao(idAnotacao);

        return success("ok");
    }

    @GET
    @Path("/arts/{idAnotacao}")
    @Produces(APPLICATION_JSON_UTF8)
    @RequiresAuthorization(to = VIEW_RESPONSE_SENSITIVE_DATA)
    @AuthorityContextSuppliedBy(provider = AnotacaoAuthorityContextProvider.class, param = "idAnotacao")
    @AnyPermissionAllowed(profile = EMPRESA)
    @ProfilesAllowed({ CONCEDENTE, PROPONENTE_CONVENENTE, MANDATARIA, USUARIO_SICONV })
    public DefaultResponse<AnotacaoRegistroRespTecnicoDTO> consultarAnotacao(@PathParam("idAnotacao") Long idAnotacao) {

        return success(bc.consultarAnotacao(idAnotacao));
    }

    private AnotacaoRegistroRespTecnicoDTO parseAnotacaoDTO(MultipartFormDataInput multipart) {

        AnotacaoRegistroRespTecnicoDTO dto = new AnotacaoRegistroRespTecnicoDTO();

        try {

            MultipartFormDataInputHelper multipartHelper = new MultipartFormDataInputHelper(multipart);

            dto.setIdMedContratoRespTec(multipartHelper.getLong("idMedContratoRespTec"));
            dto.setNumero(multipartHelper.getString("numero"));
            dto.setDataEmissao(multipartHelper.getLocalDate("dataEmissao"));
            dto.setTipo(TipoResponsavelTecnicoEnum.valueOf(multipartHelper.getString("tipo")));
            dto.setVersao(multipartHelper.getLong("versao"));

            List<FileUpload> arquivos = fileUploadUtil.process(multipart);

            if (!arquivos.isEmpty()) {
                FileUpload arquivo = arquivos.get(0);
                dto.setArquivo(arquivo.getContent());
                dto.setNmArquivo(arquivo.getFileName());
            }

            dto.setSubmetas(multipartHelper.getJsonType("submetas",
                    typeFactory -> typeFactory.constructCollectionType(List.class, SubmetaVrplDTO.class)));

        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Falha ao realizar o parsing da Anotação a partir do multipartFormDataInput.", e);
        }

        return dto;
    }
}
