package br.gov.planejamento.siconv.med.integration.ceph;

import static java.util.Arrays.asList;

import java.io.IOException;
import java.net.URLEncoder;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.metrics.annotation.SimplyTimed;

import br.gov.planejamento.siconv.med.infra.exception.MedicaoRestException;
import br.gov.planejamento.siconv.med.infra.message.MessageKey;
import br.gov.planejamento.siconv.med.integration.ceph.S3PresignerProducer.CustomS3Presigner;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@ApplicationScoped
public class CephActions {

	private static final Integer LIMITE_NOME_ANEXO = 100;

	private static final List<String> EXTENSOES_VALIDAS = asList(".PDF", ".XLS", ".JPG", ".JPEG", ".PNG", ".ODT",
			".ODS", ".XLSX", ".DWG", ".DOC", ".DOCX");

	private static final Integer TAMANHO_MAXIMO_ANEXO = 10 * 1024 * 1024; // 10485760 bytes = 10 MB

	private static final String NOME_BUCKET = "medicao";

	@Inject
	S3Client cephClient;

	@Inject
	@CustomS3Presigner
	S3Presigner presigner;

	@SimplyTimed
	public String uploadFile(byte[] content, final String filename) {

		try {

			if (filename.length() > LIMITE_NOME_ANEXO) {
				throw new MedicaoRestException(MessageKey.ERRO_LIMITE_NOME_ANEXO_EXCEDIDO);
			}

			if (!EXTENSOES_VALIDAS
					.contains(filename.substring(filename.lastIndexOf("."), filename.length()).toUpperCase())) {
				throw new MedicaoRestException(MessageKey.ERRO_EXTENSAO_ANEXO_INVALIDA);
			}

			if (content.length > TAMANHO_MAXIMO_ANEXO) {
				throw new MedicaoRestException(MessageKey.ERRO_TAMANHO_MAXIMO_ANEXO_EXCEDIDO);
			}

			String encodedFilename = URLEncoder.encode(filename, "UTF-8").replace("+", "%20");
			String key = URLEncoder.encode(filename + "_" + UUID.randomUUID(), "UTF-8");

			Map<String, String> userMetadata = new HashMap<>();
			userMetadata.put("filename", encodedFilename);

			cephClient.putObject(PutObjectRequest.builder().bucket(NOME_BUCKET).key(key).metadata(userMetadata)
					.contentDisposition("attachment; filename=" + encodedFilename).acl(ObjectCannedACL.PRIVATE).build(),
					RequestBody.fromBytes(content));

			return key;

		} catch (IOException ioe) {
			throw new IllegalArgumentException(ioe);
		}
	}

	@SimplyTimed
	public String getPresignedUrl(String key) {

		GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(NOME_BUCKET).key(key).build();

		GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
				.signatureDuration(Duration.ofMinutes(20)).getObjectRequest(getObjectRequest).build();

		PresignedGetObjectRequest presignedGetObjectRequest = presigner.presignGetObject(getObjectPresignRequest);

		return presignedGetObjectRequest.url().toString();
	}
}
