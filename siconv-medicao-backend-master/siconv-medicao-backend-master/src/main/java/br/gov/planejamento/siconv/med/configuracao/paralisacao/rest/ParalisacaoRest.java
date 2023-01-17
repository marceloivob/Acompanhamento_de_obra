package br.gov.planejamento.siconv.med.configuracao.paralisacao.rest;

import static br.gov.planejamento.siconv.med.infra.rest.response.ResponseHelper.success;
import static br.gov.planejamento.siconv.med.infra.security.domain.AuthorizationScope.INVOKE_METHOD;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.CONCEDENTE;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.MANDATARIA;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.PROPONENTE_CONVENENTE;
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
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import br.gov.planejamento.siconv.med.configuracao.paralisacao.business.ParalisacaoBC;
import br.gov.planejamento.siconv.med.configuracao.paralisacao.entity.dto.AnexoParalisacaoDTO;
import br.gov.planejamento.siconv.med.configuracao.paralisacao.entity.dto.ParalisacaoDTO;
import br.gov.planejamento.siconv.med.infra.exception.MedicaoRestException;
import br.gov.planejamento.siconv.med.infra.message.MessageKey;
import br.gov.planejamento.siconv.med.infra.rest.response.ResponseHelper.DefaultResponse;
import br.gov.planejamento.siconv.med.infra.security.annotation.AuthorityContextSuppliedBy;
import br.gov.planejamento.siconv.med.infra.security.annotation.RequiresAuthentication;
import br.gov.planejamento.siconv.med.infra.security.annotation.RequiresAuthorization;
import br.gov.planejamento.siconv.med.infra.security.annotation.RolesAllowed;
import br.gov.planejamento.siconv.med.infra.security.provider.ContratoAuthorityContextProvider;
import br.gov.planejamento.siconv.med.infra.security.provider.ParalisacaoAuthorityContextProvider;
import br.gov.planejamento.siconv.med.infra.util.FileUploadUtil;
import br.gov.planejamento.siconv.med.infra.util.MultipartFormDataInputHelper;

@Path("/")
public class ParalisacaoRest {

    @Inject
    private ParalisacaoBC paralisacaoBC;

    @Inject
    private FileUploadUtil fileUploadUtil;

    @GET
    @Path("/contratos/{idContratoSiconv}/paralisacoes/")
    @Produces(APPLICATION_JSON_UTF8)
    @Operation(summary = "Consulta lista de Paralisações de um contrato")
    @RequiresAuthentication
    public DefaultResponse<Object> listarParalisacoes(@PathParam("idContratoSiconv") Long idContratoSiconv) {

        return success(paralisacaoBC.listarParalisacoes(idContratoSiconv));
    }

    @GET
    @Path("/paralisacoes/{idParalisacao}")
    @Produces(APPLICATION_JSON_UTF8)
    @Operation(summary = "Consulta Paralisação de Obra e seus anexos")
    @RequiresAuthentication
    public DefaultResponse<ParalisacaoDTO> consultarParalisacao(@PathParam("idParalisacao") Long idParalisacao) {

        return success(paralisacaoBC.consultarParalisacao(idParalisacao));
    }

    @DELETE
    @Path("/paralisacoes/{idParalisacao}")
    @Produces(APPLICATION_JSON_UTF8)
    @Operation(summary = "Exclusão de Paralisação de Obra e seus anexos")
    @RequiresAuthorization(to = INVOKE_METHOD)
    @AuthorityContextSuppliedBy(provider = ParalisacaoAuthorityContextProvider.class, param = "idParalisacao")
    @RolesAllowed(profile = PROPONENTE_CONVENENTE, roles = { GESTOR_CONVENIO_CONVENENTE,
                                                             GESTOR_FINANCEIRO_CONVENENTE,
                                                             OPERADOR_FINANCEIRO_CONVENENTE,
                                                             FISCAL_CONVENENTE })
    @RolesAllowed(profile = CONCEDENTE, roles = { FISCAL_CONCEDENTE,
                                                  GESTOR_CONVENIO_CONCEDENTE,
                                                  GESTOR_FINANCEIRO_CONCEDENTE,
                                                  OPERACIONAL_CONCEDENTE,
                                                  FISCAL_ACOMPANHAMENTO,
                                                  TECNICO_TERCEIRO })
    @RolesAllowed(profile = MANDATARIA, roles = { AGENTE_ACOMPANHAMENTO_INSTITUICAO_MANDATARIA })
    public DefaultResponse<String> excluirParalisacao(@PathParam("idParalisacao") Long idParalisacao) {

        paralisacaoBC.excluirParalisacao(idParalisacao);

        return success("ok");
    }

    @POST
    @Path("/contratos/{idContratoSiconv}/paralisacoes")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(APPLICATION_JSON_UTF8)
    @Operation(summary = "Inclusão de Paralisação e seus anexos")
    @RequiresAuthorization(to = INVOKE_METHOD)
    @AuthorityContextSuppliedBy(provider = ContratoAuthorityContextProvider.class, param = "idContratoSiconv")
    @RolesAllowed(profile = PROPONENTE_CONVENENTE, roles = { GESTOR_CONVENIO_CONVENENTE,
                                                             GESTOR_FINANCEIRO_CONVENENTE,
                                                             OPERADOR_FINANCEIRO_CONVENENTE,
                                                             FISCAL_CONVENENTE })
    @RolesAllowed(profile = CONCEDENTE, roles = { FISCAL_CONCEDENTE,
                                                  GESTOR_CONVENIO_CONCEDENTE,
                                                  GESTOR_FINANCEIRO_CONCEDENTE,
                                                  OPERACIONAL_CONCEDENTE,
                                                  FISCAL_ACOMPANHAMENTO,
                                                  TECNICO_TERCEIRO })
    @RolesAllowed(profile = MANDATARIA, roles = { AGENTE_ACOMPANHAMENTO_INSTITUICAO_MANDATARIA })
    public DefaultResponse<Long> incluir(@PathParam("idContratoSiconv") Long idContratoSiconv,
            MultipartFormDataInput multipart) {

        ParalisacaoDTO paralisacaoDTO = parseParalisacaoDTO(multipart);

        validarParalisacaoDtoNaoNulo(paralisacaoDTO);

        paralisacaoDTO.setIdContratoSiconv(idContratoSiconv);

        return success(paralisacaoBC.incluirParalisacao(idContratoSiconv, paralisacaoDTO));
    }

    @PUT
    @Path("/paralisacoes/{idParalisacao}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(APPLICATION_JSON_UTF8)
    @Operation(summary = "Alteração de paralisação de obras e seus anexos")
    @RequiresAuthorization(to = INVOKE_METHOD)
    @AuthorityContextSuppliedBy(provider = ParalisacaoAuthorityContextProvider.class, param = "idParalisacao")
    @RolesAllowed(profile = PROPONENTE_CONVENENTE, roles = { GESTOR_CONVENIO_CONVENENTE,
                                                             GESTOR_FINANCEIRO_CONVENENTE,
                                                             OPERADOR_FINANCEIRO_CONVENENTE,
                                                             FISCAL_CONVENENTE })
    @RolesAllowed(profile = CONCEDENTE, roles = { FISCAL_CONCEDENTE,
                                                  GESTOR_CONVENIO_CONCEDENTE,
                                                  GESTOR_FINANCEIRO_CONCEDENTE,
                                                  OPERACIONAL_CONCEDENTE,
                                                  FISCAL_ACOMPANHAMENTO,
                                                  TECNICO_TERCEIRO })
    @RolesAllowed(profile = MANDATARIA, roles = { AGENTE_ACOMPANHAMENTO_INSTITUICAO_MANDATARIA })
    public DefaultResponse<String> alterarParalisacao(@PathParam("idParalisacao") Long idParalisacao,
            MultipartFormDataInput multipart) {

        ParalisacaoDTO paralisacaoDTO = parseParalisacaoDTO(multipart);

        validarParalisacaoDtoNaoNulo(paralisacaoDTO);

        paralisacaoDTO.setId(idParalisacao);

        paralisacaoBC.alterarParalisacao(paralisacaoDTO);

        return success("ok");
    }

    /**
     * Monta o objeto ParalisacaoDTO a partir do multipartFormDataInput
     * 
     * @param multipart
     * @return
     */
    private ParalisacaoDTO parseParalisacaoDTO(MultipartFormDataInput multipart) {

        ParalisacaoDTO paralisacaoDTO;

        try {
            MultipartFormDataInputHelper multipartHelper = new MultipartFormDataInputHelper(multipart);

            paralisacaoDTO = multipartHelper.getJsonType("paralisacaoDTO",
                    typeFactory -> typeFactory.constructType(ParalisacaoDTO.class));

            List<FileUploadUtil.FileUpload> listaArquivos = fileUploadUtil.process(multipart);

            this.carregarAnexosParalisacao(listaArquivos, paralisacaoDTO);

        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Falha ao realizar o parsing do ParalisacaoDTO a partir do multipartFormDataInput.", e);
        }

        return paralisacaoDTO;
    }

    /**
     * Monta a lista de Anexos de uma Paralisacao.
     * 
     * @param multipart
     * @param paralisacaoDTO
     */
    private void carregarAnexosParalisacao(List<FileUploadUtil.FileUpload> listaArquivos,
            ParalisacaoDTO paralisacaoDTO) {

        for (FileUploadUtil.FileUpload arquivo : listaArquivos) {

            AnexoParalisacaoDTO anexo = new AnexoParalisacaoDTO();

            // Define o ID do Anexo
            anexo.setId(arquivo.getId());

            // Define o Arquivo
            anexo.setArquivo(arquivo.getContent());
            anexo.setNmArquivo(arquivo.getFileName());
            paralisacaoDTO.getAnexos().add(anexo);
        }
    }

    private void validarParalisacaoDtoNaoNulo(ParalisacaoDTO paralisacaoDTO) {
        if (paralisacaoDTO == null) {
            throw new MedicaoRestException(MessageKey.ERRO_PARAMETRO_OBRIGATORIO_NAO_INFORMADO, "paralisacaoDTO");
        }
    }
}
