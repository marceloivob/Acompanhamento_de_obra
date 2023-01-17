package br.gov.planejamento.siconv.med.infra.rest;

import static br.gov.planejamento.siconv.med.infra.rest.response.ResponseHelper.success;
import static br.gov.planejamento.siconv.med.infra.util.ApplicationProperties.APPLICATION_JSON_UTF8;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import br.gov.planejamento.siconv.med.infra.rest.response.ResponseHelper.DefaultResponse;
import br.gov.planejamento.siconv.med.infra.util.ApplicationProperties;

@Path("/app")
public class ApplicationInformation {

    @Inject
    private ApplicationProperties config;

    @GET
    @Path("/info")
    @Produces(APPLICATION_JSON_UTF8)
    public DefaultResponse<Map<String, Object>> recoveryInformationAboutBuildFromBackend() throws IOException {

        final Properties properties = new Properties();
        properties.load(this.getClass().getClassLoader().getResourceAsStream("project-build.properties"));

        Map<String, Object> projectInfo = new HashMap<>();

        projectInfo.put("date", LocalDate.now().toString());
        projectInfo.put("time", LocalTime.now().toString());
        projectInfo.put("version", properties.getProperty("version"));
        projectInfo.put("build.date", properties.getProperty("build.date"));

        return success(projectInfo);

    }

    @GET
    @Path("/integrations")
    @Produces(APPLICATION_JSON_UTF8)
    public DefaultResponse<Map<String, Object>> getEndpointIDP() {
        Map<String, Object> mm = new HashMap<>();
        mm.put("IDP", config.getUrlIdp());
        mm.put("SICONV", config.getUrlSiconv());
        mm.put("urlGovBr", config.getUrlGovBr());
        mm.put("idAppGovBr", config.getIdAppGovBr());

        return success(mm);
    }
}
