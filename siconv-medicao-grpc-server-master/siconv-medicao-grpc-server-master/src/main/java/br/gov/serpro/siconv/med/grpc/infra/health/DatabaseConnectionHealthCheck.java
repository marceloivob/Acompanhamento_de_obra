package br.gov.serpro.siconv.med.grpc.infra.health;

import java.sql.SQLException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.HandleCallback;
import org.jdbi.v3.core.Jdbi;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@Readiness
@ApplicationScoped
public class DatabaseConnectionHealthCheck implements HealthCheck {

	@Inject
	private Jdbi jdbi;
	
    @Override
    public HealthCheckResponse call() {
    	
    	if (hasValidConnection()) {
    		return HealthCheckResponse.up("Database connection ready!!");
    	}
    	
		return HealthCheckResponse.down("Database connection is not ready!!!");
    }
    
    
    
    public boolean hasValidConnection() {
		
		final int TIME_OUT_EM_SEGUNDOS = 15;
		
		try {
			return jdbi.withHandle(new HandleCallback<Boolean, SQLException>() {

				@Override
				public Boolean withHandle(Handle handle) throws SQLException {
					
					return handle.getConnection().isValid(TIME_OUT_EM_SEGUNDOS);
				}
				
			});
		} catch (Exception e) {
			log.error("############## READINESS ERROR ##############", e);
			return Boolean.FALSE;
		}
		
	}
}