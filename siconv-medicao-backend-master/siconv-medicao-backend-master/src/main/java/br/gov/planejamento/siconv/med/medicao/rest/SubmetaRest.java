package br.gov.planejamento.siconv.med.medicao.rest;

import static br.gov.planejamento.siconv.med.infra.rest.response.ResponseHelper.success;
import static br.gov.planejamento.siconv.med.infra.security.domain.AuthorizationScope.INVOKE_METHOD;
import static br.gov.planejamento.siconv.med.infra.security.domain.AuthorizationScope.VIEW_RESPONSE_SENSITIVE_DATA;
import static br.gov.planejamento.siconv.med.infra.security.domain.Permission.ASSINAR_SUBMETA;
import static br.gov.planejamento.siconv.med.infra.security.domain.Permission.EDITAR_SUBMETA;
import static br.gov.planejamento.siconv.med.infra.security.domain.Permission.EXCLUIR_SUBMETA;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.CONCEDENTE;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.EMPRESA;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.MANDATARIA;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.PROPONENTE_CONVENENTE;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.USUARIO_SICONV;
import static br.gov.planejamento.siconv.med.infra.security.domain.Role.FISCAL_CONVENENTE;
import static br.gov.planejamento.siconv.med.infra.security.domain.Role.GESTOR_CONVENIO_CONVENENTE;
import static br.gov.planejamento.siconv.med.infra.security.domain.Role.GESTOR_FINANCEIRO_CONVENENTE;
import static br.gov.planejamento.siconv.med.infra.security.domain.Role.OPERADOR_FINANCEIRO_CONVENENTE;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.Operation;

import br.gov.planejamento.siconv.med.infra.rest.response.ResponseHelper.DefaultResponse;
import br.gov.planejamento.siconv.med.infra.security.SecurityContext;
import br.gov.planejamento.siconv.med.infra.security.annotation.AnyPermissionAllowed;
import br.gov.planejamento.siconv.med.infra.security.annotation.AuthorityContextSuppliedBy;
import br.gov.planejamento.siconv.med.infra.security.annotation.PermissionsAllowed;
import br.gov.planejamento.siconv.med.infra.security.annotation.ProfilesAllowed;
import br.gov.planejamento.siconv.med.infra.security.annotation.RequiresAuthentication;
import br.gov.planejamento.siconv.med.infra.security.annotation.RequiresAuthorization;
import br.gov.planejamento.siconv.med.infra.security.annotation.RolesAllowed;
import br.gov.planejamento.siconv.med.infra.security.domain.Role;
import br.gov.planejamento.siconv.med.infra.security.provider.ContratoAuthorityContextProvider;
import br.gov.planejamento.siconv.med.infra.security.provider.MedicaoAuthorityContextProvider;
import br.gov.planejamento.siconv.med.medicao.business.SubmetaBC;
import br.gov.planejamento.siconv.med.medicao.entity.dto.SubmetaMedicaoDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.SubmetaVrplDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.ValoresSubmetaDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.submetaservicosalvar.SubmetaSalvarDTO;
import lombok.Data;

@Path("/")
public class SubmetaRest {

    @Inject
    private SubmetaBC submetaBC;

    @Inject
    private SecurityContext securityContext;

    @GET
    @Path("/medicoes/{idMedicao}/submetas")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retorna lista de Submetas de uma Medição")
    @RequiresAuthorization(to = VIEW_RESPONSE_SENSITIVE_DATA)
    @AuthorityContextSuppliedBy(provider = MedicaoAuthorityContextProvider.class, param = "idMedicao")
    @AnyPermissionAllowed(profile = EMPRESA)
    @ProfilesAllowed({ CONCEDENTE, PROPONENTE_CONVENENTE, MANDATARIA, USUARIO_SICONV })
    public DefaultResponse<ListagemSubmetaOutput> listarSubmetasPorMedicao(@PathParam("idMedicao") Long idMedicao) {

        List<SubmetaMedicaoDTO> submetas = submetaBC.recuperarListaSubmetasPorMedicao(idMedicao);

        ListagemSubmetaOutput output = new ListagemSubmetaOutput(submetas,
                submetaBC.totalizarValoresSubmetas(submetas));

        return success(output);
    }

    @GET
    @Path("/medicoes/{idMedicao}/submetas/{idSubmeta}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retorna a submeta com uma lista de Eventos/Frentes de Obra e respectivos Serviços de uma determinada Submeta")
    @RequiresAuthorization(to = VIEW_RESPONSE_SENSITIVE_DATA)
    @AuthorityContextSuppliedBy(provider = MedicaoAuthorityContextProvider.class, param = "idMedicao")
    @AnyPermissionAllowed(profile = EMPRESA)
    @ProfilesAllowed({ CONCEDENTE, PROPONENTE_CONVENENTE, MANDATARIA, USUARIO_SICONV })
    public DefaultResponse<SubmetaMedicaoDTO> recuperarSubmetaPorMedicao(@PathParam("idMedicao") Long idMedicao,
            @PathParam("idSubmeta") Long idSubmeta) {

        SubmetaMedicaoDTO submeta = submetaBC.recuperarSubmetaPorMedicao(idMedicao, idSubmeta);

        return success(submeta);

    }

    @PUT
    @Path("/medicoes/{idMedicao}/submetas/{idSubmetaVrpl}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Altera dados de uma Submeta. Cria ou atualiza uma submeta medição. Se não existir, sua situação será 'Rascunho'"
            + "Caso a submeta já esteja assinada, remove a assinatura e atualiza a situação para 'Rascunho'."
            + "Atualiza o Item Medição com a informação da medição efetuada pela empresa (indicador Executado Empresa)")
    @RequiresAuthorization(to = INVOKE_METHOD)
    @AuthorityContextSuppliedBy(provider = MedicaoAuthorityContextProvider.class, param = "idMedicao")
    @PermissionsAllowed(profile = EMPRESA, permissions = EDITAR_SUBMETA)
    @RolesAllowed (profile = PROPONENTE_CONVENENTE, roles = { GESTOR_CONVENIO_CONVENENTE, GESTOR_FINANCEIRO_CONVENENTE, OPERADOR_FINANCEIRO_CONVENENTE, FISCAL_CONVENENTE })
    @RolesAllowed (profile = CONCEDENTE, roles = {Role.FISCAL_CONCEDENTE, Role.GESTOR_CONVENIO_CONCEDENTE, Role.GESTOR_FINANCEIRO_CONCEDENTE, Role.OPERACIONAL_CONCEDENTE, Role.FISCAL_ACOMPANHAMENTO, Role.TECNICO_TERCEIRO })
    @RolesAllowed (profile = MANDATARIA, roles = {Role.AGENTE_ACOMPANHAMENTO_INSTITUICAO_MANDATARIA})    
    public DefaultResponse<Integer> salvar(@PathParam("idMedicao") Long idMedicao,
            @PathParam("idSubmetaVrpl") Long idSubmetaVrpl, SubmetaSalvarDTO listaFrentesObra) {

        submetaBC.salvarSubmeta(idMedicao, idSubmetaVrpl, listaFrentesObra, false);

        return success(200);

    }

    @PUT
    @Path("/contratos/{idContrato}/medicoes/{idMedicao}/submetas/{idSubmetaVrpl}/assinatura")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Assina uma submeta validando se o usuário é responsável por aquela Submeta/PO. "
            + "Salva a submeta (medição) antes de assinar, caso a mesma ainda não tenha sido criada.")
    @RequiresAuthorization(to = INVOKE_METHOD)
    @AuthorityContextSuppliedBy(provider = MedicaoAuthorityContextProvider.class, param = "idMedicao")
    @PermissionsAllowed(profile = EMPRESA, permissions = ASSINAR_SUBMETA)
    @RolesAllowed(profile = PROPONENTE_CONVENENTE, roles = { FISCAL_CONVENENTE })
    @RolesAllowed (profile = CONCEDENTE, roles = {Role.FISCAL_ACOMPANHAMENTO, Role.TECNICO_TERCEIRO})
    @RolesAllowed (profile = MANDATARIA, roles = {Role.AGENTE_ACOMPANHAMENTO_INSTITUICAO_MANDATARIA})
    public DefaultResponse<Integer> assinarSubmeta(@PathParam("idContrato") Long idContrato,
            @PathParam("idMedicao") Long idMedicao, @PathParam("idSubmetaVrpl") Long idSubmetaVrpl,
            SubmetaSalvarDTO listaFrentesObra) {

        submetaBC.assinarSubmeta(idContrato, idMedicao, idSubmetaVrpl, listaFrentesObra,
                securityContext.getUser().getCpf());

        return success(200);
    }

    @GET
    @Path("/contratos/{idContrato}/medicoes/{idMedicao}/submetas/{idSubmeta}/assinavel")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "É 'assinável' se o CPF é o do Responsável Técnico ao qual a Submeta está vinculada")
    @RequiresAuthentication
    @AuthorityContextSuppliedBy(provider = ContratoAuthorityContextProvider.class, param = "idContrato")
    public DefaultResponse<Boolean> isSubmetaAssinavelPeloCpf(@PathParam("idContrato") Long idContrato,
    		@PathParam("idMedicao") Long idMedicao,
            @PathParam("idSubmeta") Long idSubmetaVrpl) {

        return success(
                submetaBC.isSubmetaAssinavelPeloCpf(idContrato, idMedicao , idSubmetaVrpl, securityContext.getUser().getCpf()));

    }


    @DELETE
    @Path("/medicoes/{idMedicao}/submetas/{idSubmeta}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Exclusão dos dados da submeta em rascunho (visão empresa)")
    @RequiresAuthorization(to = INVOKE_METHOD)
    @AuthorityContextSuppliedBy(provider = MedicaoAuthorityContextProvider.class, param = "idMedicao")
    @PermissionsAllowed(profile = EMPRESA, permissions = EXCLUIR_SUBMETA)
    @RolesAllowed(profile= PROPONENTE_CONVENENTE, roles = { GESTOR_CONVENIO_CONVENENTE, GESTOR_FINANCEIRO_CONVENENTE,
            OPERADOR_FINANCEIRO_CONVENENTE, FISCAL_CONVENENTE })
    @RolesAllowed (profile = CONCEDENTE, roles = {Role.FISCAL_CONCEDENTE, Role.GESTOR_CONVENIO_CONCEDENTE, Role.GESTOR_FINANCEIRO_CONCEDENTE, Role.OPERACIONAL_CONCEDENTE, Role.FISCAL_ACOMPANHAMENTO, Role.TECNICO_TERCEIRO })
    @RolesAllowed (profile = MANDATARIA, roles = {Role.AGENTE_ACOMPANHAMENTO_INSTITUICAO_MANDATARIA})    
    public DefaultResponse<Integer> excluirRascunhoSubmeta(@PathParam("idMedicao") Long idMedicao,
            @PathParam("idSubmeta") Long idSubmetaVrpl) {

        submetaBC.excluirRascunhoSubmeta(idMedicao, idSubmetaVrpl);

        return success(200);

    }

    @GET
    @Path("/contratos/{idContrato}/submetas")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retorna lista de Submetas de um Contrato")
    @RequiresAuthorization(to = VIEW_RESPONSE_SENSITIVE_DATA)
    @AuthorityContextSuppliedBy(provider = ContratoAuthorityContextProvider.class, param = "idContrato")
    @AnyPermissionAllowed(profile = EMPRESA)
    @ProfilesAllowed({ CONCEDENTE, PROPONENTE_CONVENENTE, MANDATARIA, USUARIO_SICONV })
    public DefaultResponse<List<SubmetaVrplDTO>> listarSubmetasPorContrato(@PathParam("idContrato") Long idContrato) {

        List<SubmetaVrplDTO> submetas = submetaBC.listarSubmetasPorContrato(idContrato);

        return success(submetas);

    }

    @Data
    private class ListagemSubmetaOutput {
        private final List<SubmetaMedicaoDTO> submetas;
        private final ValoresSubmetaDTO total;
    }
}
