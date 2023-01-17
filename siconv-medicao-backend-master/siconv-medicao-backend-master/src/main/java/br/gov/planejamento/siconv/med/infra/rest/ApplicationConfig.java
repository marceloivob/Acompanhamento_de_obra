package br.gov.planejamento.siconv.med.infra.rest;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeIn;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;

@OpenAPIDefinition(info = @Info(title = "SICONV Medição - API", version = "1.0.0"), security = @SecurityRequirement(name = "Token JWT"))
@SecurityScheme(scheme = "Bearer", in = SecuritySchemeIn.HEADER, securitySchemeName = "Token JWT", type = SecuritySchemeType.HTTP)
@ApplicationPath("/")
public class ApplicationConfig extends Application {

}
