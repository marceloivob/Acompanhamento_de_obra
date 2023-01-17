package br.gov.serpro.siconv.med.grpc.services;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;

import br.gov.serpro.siconv.med.grpc.ContratoGrpc.ContratoImplBase;
import br.gov.serpro.siconv.med.grpc.ContratoRequest;
import br.gov.serpro.siconv.med.grpc.ContratoResponse;
import br.gov.serpro.siconv.med.grpc.bean.ContratoBD;
import br.gov.serpro.siconv.med.grpc.bean.MedicaoBD;
import br.gov.serpro.siconv.med.grpc.dao.ContratoDAO;
import br.gov.serpro.siconv.med.grpc.dao.MedicaoDAO;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.protobuf.ProtoUtils;
import io.grpc.stub.StreamObserver;
import io.quarkus.grpc.GrpcService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@GrpcService
public class ContratoService extends ContratoImplBase {

	@Inject
	private Jdbi jdbi;

    @ConfigProperty(name = "sentry.environment")
    public Optional<String> environment;
	
	@Override
	public void consultarContrato(ContratoRequest contrato,
			StreamObserver<ContratoResponse> responseObserver) {

		try (Handle handle = jdbi.open()) {
			ContratoBD contratoBD = handle.attach(ContratoDAO.class)
					.consultarContratoPorContratoFK(contrato.getIdContrato());

			br.gov.serpro.siconv.med.grpc.ContratoResponse.Builder contratoResponse = ContratoResponse.newBuilder();
			
			if (contratoBD != null) {
				contratoResponse.setConfiguracaoIniciada (true);	
				
				List<MedicaoBD> listaMedicoes = handle.attach(MedicaoDAO.class)
						.listarMedicoes(contrato.getIdContrato());
				
				if (!listaMedicoes.isEmpty()) {
					contratoResponse.setMedicaoIniciada(true);
				}

				responseObserver.onNext(contratoResponse.build());
				
				responseObserver.onCompleted();

			} else {
				Status status = Status.NOT_FOUND.withDescription("Contrato não encontrado");
				decorateResponseWithError(status, responseObserver, contratoResponse);
				log.warn("NOT FOUND ID: "+contrato.getIdContrato());
			}
		} catch (Exception e) {
			log.error("error",e);
			
			Status status = Status.INTERNAL.withDescription("Erro interno");
			br.gov.serpro.siconv.med.grpc.ContratoResponse.Builder contratoResponse = ContratoResponse.newBuilder();
			decorateResponseWithError(status, responseObserver, contratoResponse);
		}
		
	}

	private void decorateResponseWithError(Status status,
								StreamObserver<ContratoResponse> responseObserver,
								br.gov.serpro.siconv.med.grpc.ContratoResponse.Builder contratoResponse) {
		Metadata.Key<ContratoResponse> contratoMetadata =
			    ProtoUtils.keyForProto(ContratoResponse.getDefaultInstance());
		Metadata metadata = new Metadata();
		metadata.put(contratoMetadata, contratoResponse.build());
		responseObserver.onError(status
			      .asRuntimeException(metadata));
	}

}
