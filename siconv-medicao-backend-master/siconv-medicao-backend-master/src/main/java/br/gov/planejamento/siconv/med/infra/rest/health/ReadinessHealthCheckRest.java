package br.gov.planejamento.siconv.med.infra.rest.health;

import java.sql.SQLException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Readiness;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.HandleCallback;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;


@Readiness
@ApplicationScoped
//localhost:8080/health/ready
public class ReadinessHealthCheckRest implements HealthCheck {

	@Inject
	private Logger logger;

	@Inject
	private Jdbi jdbi;

	/**
	 *	
	 * 	Fontes
	 * 	
	 * 	https://stackoverflow.com/questions/8809098/how-do-i-set-the-default-locale-for-my-jvm
	 *	https://stackoverflow.com/questions/55673886/what-is-the-difference-between-c-utf-8-and-en-us-utf-8-locales
	 *	https://github.com/flutter/flutter/issues/13574
	 *
	 */
    @Override
	public HealthCheckResponse call(){

		
		HealthCheckResponseBuilder builder = HealthCheckResponse.named("readiness");
		
		
		if (databaseIsOk()) {
			return builder.up().build();
		} else {
			return builder.down().build();
		}
	}
	
	private boolean databaseIsOk() {
		
		final int TIME_OUT_EM_SEGUNDOS = 15;
		
		try {
		
			return jdbi.withHandle(new HandleCallback<Boolean, SQLException>() {
				@Override
				public Boolean withHandle(Handle handle) throws SQLException {
					return handle.getConnection().isValid(TIME_OUT_EM_SEGUNDOS);
				}
				
			});
		
		} catch (Exception ex) {
			logger.error("Readiness error, na conexão com o banco!", ex);

			return false;
		}
	}

}
