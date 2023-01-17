package br.gov.planejamento.siconv.med.empresa.rest;

import static br.gov.planejamento.siconv.med.infra.rest.response.ResponseHelper.success;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.EMPRESA;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import br.gov.planejamento.siconv.med.empresa.business.EmpresaBC;
import br.gov.planejamento.siconv.med.empresa.entity.dto.EmpresaDTO;
import br.gov.planejamento.siconv.med.infra.rest.response.ResponseHelper.DefaultResponse;
import br.gov.planejamento.siconv.med.infra.security.SecurityContext;
import br.gov.planejamento.siconv.med.infra.security.annotation.RequiresAuthentication;

@Path("/")
public class EmpresaRest {

    @Inject
    private EmpresaBC empresasBC;

    @Inject
    private SecurityContext securityContext;

    @GET
    @Path("/empresas/")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresAuthentication(profiles = EMPRESA)
    public DefaultResponse<List<EmpresaDTO>> listarEmpresas() {

        List<EmpresaDTO> empresas = empresasBC.listarEmpresasVinculadasUsuario(securityContext.getUser().getCpf());

        return success(empresas);
    }

    @GET
    @Path("/empresas/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresAuthentication
    public DefaultResponse<EmpresaDTO> consultarEmpresaPorId(@PathParam("id") Long id) {

        EmpresaDTO empresas = empresasBC.consultarEmpresaPorId(id);

        return success(empresas);
    }
}
