package br.gov.planejamento.siconv.med.integration.ceph;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Qualifier;

import io.quarkus.amazon.s3.runtime.S3Config;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@ApplicationScoped
public class S3PresignerProducer {

	private S3Presigner presigner;

	@Inject
	S3Config config;

	@PostConstruct
	public void init() {
		S3Presigner.Builder builder = S3Presigner.builder();
		builder.credentialsProvider(config.aws.credentials.type.create(config.aws.credentials, "quarkus.s3"));
		config.aws.region.ifPresent(builder::region);
		config.sdk.endpointOverride.ifPresent(builder::endpointOverride);
		presigner = builder.build();
	}

	@Produces
	@ApplicationScoped
	@CustomS3Presigner
	public S3Presigner presigner() {
		return presigner;
	}

	@PreDestroy
	public void destroy() {
		if (presigner != null) {
			presigner.close();
		}
	}

	@Target({ FIELD, METHOD })
	@Retention(RUNTIME)
	@Qualifier
	@interface CustomS3Presigner {
	}
}
