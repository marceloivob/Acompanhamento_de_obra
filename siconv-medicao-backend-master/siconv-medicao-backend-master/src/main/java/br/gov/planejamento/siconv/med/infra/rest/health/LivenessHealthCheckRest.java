package br.gov.planejamento.siconv.med.infra.rest.health;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;

@Liveness
@ApplicationScoped
/**
 *  URL de Acesso: localhost:8080/health/live
 *	https://srcco.de/posts/kubernetes-liveness-probes-are-dangerous.html
 *	https://kubernetes.io/docs/tasks/configure-pod-container/configure-liveness-readiness-startup-probes/
 *	https://download.eclipse.org/microprofile/microprofile-health-2.1/microprofile-health-spec.html#health-check-procedures
 *	https://cloud.google.com/blog/products/gcp/kubernetes-best-practices-setting-up-health-checks-with-readiness-and-liveness-probes
 *	https://medium.com/metrosystemsro/kubernetes-readiness-liveliness-probes-best-practices-86c3cd9f0b4a
 *	
 */
public class LivenessHealthCheckRest implements HealthCheck {

	@Override
	public HealthCheckResponse call() {
		
		return HealthCheckResponse
				.named("liveness") 
				.up().build();
		
	}

}
