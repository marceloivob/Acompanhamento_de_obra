package br.gov.planejamento.siconv.med.medicao.rest;

import static br.gov.planejamento.siconv.med.infra.rest.response.ResponseHelper.success;
import static br.gov.planejamento.siconv.med.infra.security.domain.AuthorizationScope.INVOKE_METHOD;
import static br.gov.planejamento.siconv.med.infra.security.domain.AuthorizationScope.VIEW_RESPONSE_SENSITIVE_DATA;
import static br.gov.planejamento.siconv.med.infra.security.domain.Permission.EDITAR_OBSERVACAO_MEDICAO;
import static br.gov.planejamento.siconv.med.infra.security.domain.Permission.EXCLUIR_OBSERVACAO_MEDICAO;
import static br.gov.planejamento.siconv.med.infra.security.domain.Permission.INCLUIR_OBSERVACAO_MEDICAO;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.CONCEDENTE;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.EMPRESA;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.MANDATARIA;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.PROPONENTE_CONVENENTE;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.USUARIO_SICONV;
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
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import br.gov.planejamento.siconv.med.infra.rest.response.ResponseHelper.DefaultResponse;
import br.gov.planejamento.siconv.med.infra.security.SecurityContext;
import br.gov.planejamento.siconv.med.infra.security.annotation.AnyPermissionAllowed;
import br.gov.planejamento.siconv.med.infra.security.annotation.AuthorityContextSuppliedBy;
import br.gov.planejamento.siconv.med.infra.security.annotation.PermissionsAllowed;
import br.gov.planejamento.siconv.med.infra.security.annotation.ProfilesAllowed;
import br.gov.planejamento.siconv.med.infra.security.annotation.RequiresAuthorization;
import br.gov.planejamento.siconv.med.infra.security.annotation.RolesAllowed;
import br.gov.planejamento.siconv.med.infra.security.provider.MedicaoAuthorityContextProvider;
import br.gov.planejamento.siconv.med.infra.security.provider.ObservacaoAuthorityContextProvider;
import br.gov.planejamento.siconv.med.infra.util.FileUploadUtil;
import br.gov.planejamento.siconv.med.infra.util.MultipartFormDataInputHelper;
import br.gov.planejamento.siconv.med.medicao.business.ObservacaoBC;
import br.gov.planejamento.siconv.med.medicao.business.PerfilHelper;
import br.gov.planejamento.siconv.med.medicao.entity.dto.AnexoDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.ObservacaoDTO;

@Path("/medicoes/{idMedicao}/observacoes/")
public class ObservacaoRest {

    @Inject
    private ObservacaoBC observacaoBC;

    @Inject
    private FileUploadUtil fileUploadUtil;

    @Inject
    private SecurityContext securityContext;

    @Inject
    private PerfilHelper perfilHelper;

    /**
     * Inserir Observação e seus anexos
     * 
     * @throws IOException
     */
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(APPLICATION_JSON_UTF8)
    @Operation(summary = "Inclusão de Observação e seus anexos")
    @RequiresAuthorization(to = INVOKE_METHOD)
    @AuthorityContextSuppliedBy(provider = MedicaoAuthorityContextProvider.class, param = "idMedicao")
    @PermissionsAllowed(profile = EMPRESA, permissions = INCLUIR_OBSERVACAO_MEDICAO)
    @RolesAllowed(profile= PROPONENTE_CONVENENTE, roles = { GESTOR_CONVENIO_CONVENENTE, GESTOR_FINANCEIRO_CONVENENTE,
            OPERADOR_FINANCEIRO_CONVENENTE, FISCAL_CONVENENTE })
    @RolesAllowed(profile= CONCEDENTE, roles = { FISCAL_CONCEDENTE, GESTOR_CONVENIO_CONCEDENTE, GESTOR_FINANCEIRO_CONCEDENTE, OPERACIONAL_CONCEDENTE, FISCAL_ACOMPANHAMENTO, TECNICO_TERCEIRO })
    @RolesAllowed(profile= MANDATARIA, roles = { AGENTE_ACOMPANHAMENTO_INSTITUICAO_MANDATARIA })
    public DefaultResponse<Long> inserirObservacao(@PathParam("idMedicao") Long medMedicaoFk,
            MultipartFormDataInput multipart) throws IOException {

        MultipartFormDataInputHelper multipartHelper = new MultipartFormDataInputHelper(multipart);

        ObservacaoDTO observacaoDTO = multipartHelper.getJsonType("observacaoDTO",
                factory -> factory.constructType(ObservacaoDTO.class));

        observacaoDTO.setMedicaoFk(medMedicaoFk);
        observacaoDTO.setNrCpfResponsavel(securityContext.getUser().getCpf());
        observacaoDTO.setInPerfilResponsavel(perfilHelper.getPerfilUsuarioLogado());

        List<FileUploadUtil.FileUpload> listaArquivos = fileUploadUtil.process(multipart);

        this.carregarAnexosObservacao(listaArquivos, observacaoDTO);

        return success(observacaoBC.inserirObservacao(medMedicaoFk, observacaoDTO));

    }

    /**
     * Monta a lista de Anexos de uma Observação.
     * 
     * @param multipart
     * @param observacaoDTO
     */
    private void carregarAnexosObservacao(List<FileUploadUtil.FileUpload> listaArquivos, ObservacaoDTO observacaoDTO) {

        for (FileUploadUtil.FileUpload arquivo : listaArquivos) {

            AnexoDTO anexo = new AnexoDTO();

            // Define o ID do Anexo
            anexo.setId(arquivo.getId());

            // Define o Arquivo
            anexo.setArquivo(arquivo.getContent());

            anexo.setNmArquivo(arquivo.getFileName());

            observacaoDTO.getAnexos().add(anexo);
        }
    }

    /**
     * Excluir Observação e seus anexos
     */
    @DELETE
    @Path("/{idObservacao}")
    @Produces(APPLICATION_JSON_UTF8)
    @Operation(summary = "Exclusão de Observação e seus anexos")
    @RequiresAuthorization(to = INVOKE_METHOD)
    @AuthorityContextSuppliedBy(provider = MedicaoAuthorityContextProvider.class, param = "idMedicao")
    @PermissionsAllowed(profile = EMPRESA, permissions = EXCLUIR_OBSERVACAO_MEDICAO)
    @RolesAllowed(profile= PROPONENTE_CONVENENTE, roles = { GESTOR_CONVENIO_CONVENENTE, GESTOR_FINANCEIRO_CONVENENTE,
            OPERADOR_FINANCEIRO_CONVENENTE, FISCAL_CONVENENTE })
    @RolesAllowed(profile= CONCEDENTE, roles = { FISCAL_CONCEDENTE, GESTOR_CONVENIO_CONCEDENTE, GESTOR_FINANCEIRO_CONCEDENTE, OPERACIONAL_CONCEDENTE, FISCAL_ACOMPANHAMENTO, TECNICO_TERCEIRO })
    @RolesAllowed(profile= MANDATARIA, roles = { AGENTE_ACOMPANHAMENTO_INSTITUICAO_MANDATARIA })    
    public DefaultResponse<String> excluirObservacao(@PathParam("idMedicao") Long idMedicao,
            @PathParam("idObservacao") Long idObservacao) {

        observacaoBC.excluirObservacao(idMedicao, idObservacao);

        return success("ok");

    }

    @PUT
    @Path("/{idObservacao}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(APPLICATION_JSON_UTF8)
    @Operation(summary = "Alteração da Observação e seus anexos")
    @RequiresAuthorization(to = INVOKE_METHOD)
    @AuthorityContextSuppliedBy(provider = MedicaoAuthorityContextProvider.class, param = "idMedicao")
    @PermissionsAllowed(profile = EMPRESA, permissions = EDITAR_OBSERVACAO_MEDICAO)
    @RolesAllowed(profile= PROPONENTE_CONVENENTE, roles = { GESTOR_CONVENIO_CONVENENTE, GESTOR_FINANCEIRO_CONVENENTE,
            OPERADOR_FINANCEIRO_CONVENENTE, FISCAL_CONVENENTE })
    @RolesAllowed(profile= CONCEDENTE, roles = { FISCAL_CONCEDENTE, GESTOR_CONVENIO_CONCEDENTE, GESTOR_FINANCEIRO_CONCEDENTE, OPERACIONAL_CONCEDENTE, FISCAL_ACOMPANHAMENTO, TECNICO_TERCEIRO })
    @RolesAllowed(profile= MANDATARIA, roles = { AGENTE_ACOMPANHAMENTO_INSTITUICAO_MANDATARIA })    
    public DefaultResponse<String> alterarObservacao(@PathParam("idMedicao") Long idMedicao,
            @PathParam("idObservacao") Long idObservacao, MultipartFormDataInput multipart) throws IOException {

        MultipartFormDataInputHelper multipartHelper = new MultipartFormDataInputHelper(multipart);

        ObservacaoDTO observacaoDTO = multipartHelper.getJsonType("observacaoDTO",
                factory -> factory.constructType(ObservacaoDTO.class));

        observacaoDTO.setId(idObservacao);
        observacaoDTO.setMedicaoFk(idMedicao);
        observacaoDTO.setNrCpfResponsavel(securityContext.getUser().getCpf());
        observacaoDTO.setInPerfilResponsavel(perfilHelper.getPerfilUsuarioLogado());

        List<FileUploadUtil.FileUpload> listaArquivos = fileUploadUtil.process(multipart);

        this.carregarAnexosObservacao(listaArquivos, observacaoDTO);

        observacaoBC.alterarObservacao(idMedicao, idObservacao, observacaoDTO);

        return success("ok");
    }

    @GET
    @Path("/")
    @Produces(APPLICATION_JSON_UTF8)
    @Operation(summary = "Retorna lista de Observações de uma Medição")
    @RequiresAuthorization(to = VIEW_RESPONSE_SENSITIVE_DATA)
    @AuthorityContextSuppliedBy(provider = MedicaoAuthorityContextProvider.class, param = "idMedicao")
    @AnyPermissionAllowed(profile = EMPRESA)
    @ProfilesAllowed({ CONCEDENTE, PROPONENTE_CONVENENTE, MANDATARIA, USUARIO_SICONV })
	public DefaultResponse<List<ObservacaoDTO>> consultarObservacaoMedicao(@PathParam("idMedicao") Long idMedicao,
			@QueryParam("medicoesAgrupadas") boolean medicoesAgrupadas) {

		List<ObservacaoDTO> observacoes = observacaoBC.buscarObservacoesMedicao(idMedicao, medicoesAgrupadas);

		return success(observacoes);

	}

    @GET
    @Path("/{idObservacao}")
    @Produces(APPLICATION_JSON_UTF8)
    @RequiresAuthorization(to = VIEW_RESPONSE_SENSITIVE_DATA)
    @AuthorityContextSuppliedBy(provider = ObservacaoAuthorityContextProvider.class, param = "idObservacao")
    @AnyPermissionAllowed(profile = EMPRESA)
    @ProfilesAllowed({ CONCEDENTE, PROPONENTE_CONVENENTE, MANDATARIA, USUARIO_SICONV })
    public DefaultResponse<ObservacaoDTO> consultarObservacaoId(@PathParam("idObservacao") Long idObservacao) {

        ObservacaoDTO observacao = observacaoBC.buscarObservacaoId(idObservacao);

        return success(observacao);

    }
    
	@PUT
	@Path("/{idObservacao}/anexos/{idAnexo}/ativa")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(APPLICATION_JSON_UTF8)
	@RequiresAuthorization(to = INVOKE_METHOD)
	@AuthorityContextSuppliedBy(provider = MedicaoAuthorityContextProvider.class, param = "idMedicao")
	@RolesAllowed(profile = PROPONENTE_CONVENENTE, roles = { GESTOR_CONVENIO_CONVENENTE, GESTOR_FINANCEIRO_CONVENENTE,OPERADOR_FINANCEIRO_CONVENENTE, FISCAL_CONVENENTE })
    @RolesAllowed(profile= CONCEDENTE, roles = { FISCAL_CONCEDENTE, GESTOR_CONVENIO_CONCEDENTE, GESTOR_FINANCEIRO_CONCEDENTE, OPERACIONAL_CONCEDENTE, FISCAL_ACOMPANHAMENTO, TECNICO_TERCEIRO })
    @RolesAllowed(profile= MANDATARIA, roles = { AGENTE_ACOMPANHAMENTO_INSTITUICAO_MANDATARIA })	
	public DefaultResponse<String> ativarAnexo(@PathParam("idObservacao") Long idObservacao, 
			                				   @PathParam("idAnexo") Long idAnexo, 
			                                   @PathParam("idMedicao") Long idMedicao) {
		observacaoBC.ativarInativarAnexo("ativar", idAnexo, idObservacao, idMedicao);
		
		return success("Ok");
	} 
	
	@PUT
	@Path("/{idObservacao}/anexos/{idAnexo}/inativa")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(APPLICATION_JSON_UTF8)
	@RequiresAuthorization(to = INVOKE_METHOD)
	@AuthorityContextSuppliedBy(provider = MedicaoAuthorityContextProvider.class, param = "idMedicao")
	@RolesAllowed(profile = PROPONENTE_CONVENENTE, roles = { GESTOR_CONVENIO_CONVENENTE, GESTOR_FINANCEIRO_CONVENENTE,OPERADOR_FINANCEIRO_CONVENENTE, FISCAL_CONVENENTE })
    @RolesAllowed(profile= CONCEDENTE, roles = { FISCAL_CONCEDENTE, GESTOR_CONVENIO_CONCEDENTE, GESTOR_FINANCEIRO_CONCEDENTE, OPERACIONAL_CONCEDENTE, FISCAL_ACOMPANHAMENTO, TECNICO_TERCEIRO })
    @RolesAllowed(profile= MANDATARIA, roles = { AGENTE_ACOMPANHAMENTO_INSTITUICAO_MANDATARIA })	
	public DefaultResponse<String> inativarAnexo(@PathParam("idObservacao") Long idObservacao, 
			                                     @PathParam("idAnexo") Long idAnexo, 
			                                     @PathParam("idMedicao") Long idMedicao) {

		observacaoBC.ativarInativarAnexo("inativar", idAnexo, idObservacao, idMedicao);
		
		return success("Ok");
	}    

    
}
