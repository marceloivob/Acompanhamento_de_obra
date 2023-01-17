//package br.gov.serpro.contrato.grpc;
package br.gov.serpro.siconv.med.grpc.services;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.gov.serpro.siconv.med.grpc.ContratoGrpc;
import br.gov.serpro.siconv.med.grpc.ContratoGrpc.ContratoBlockingStub;
import br.gov.serpro.siconv.med.grpc.ContratoRequest;
import br.gov.serpro.siconv.med.grpc.ContratoResponse;
import br.gov.serpro.siconv.med.grpc.HealthCheckRequest;
import br.gov.serpro.siconv.med.grpc.HealthCheckResponse;
import br.gov.serpro.siconv.med.grpc.HealthGrpc;
import br.gov.serpro.siconv.med.grpc.HealthGrpc.HealthBlockingStub;
import br.gov.serpro.siconv.med.grpc.dto.ContratoDTO;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MedicaoGRPCClient {

	
	static Logger logger = LoggerFactory.getLogger(MedicaoGRPCClient.class);
	
	// ESTALEIRO
	public static final String HOST = System.getProperty("host", "localhost");	
//	public static final String HOST = System.getProperty("host", "nodes.estaleiro.serpro");

	public static final int PORT = Integer.parseInt(System.getProperty("MEDICAO_GRPC_PORT", "50051"));
//	public static final int PORT = Integer.parseInt(System.getProperty("MEDICAO_GRPC_PORT", "32744"));

	// public static final String HOST = System.getProperty("host",
	// "nodes.estaleiro.serpro");
	// public static final int PORT = Integer.parseInt(System.getProperty("port",
	// "30287"));

	// public static final String HOST = System.getProperty("host",
	// "des-siconv-grpc.estaleiro.serpro.gov.br");
	// public static final int PORT = Integer.parseInt(System.getProperty("port",
	// "443"));

	public static final Boolean useSSLGRPC = Boolean.valueOf(System.getProperty("useSSLGRPC", "true"));

	private final ManagedChannel channel;

	private final ContratoBlockingStub blockingContratoStub;
	
	private final HealthBlockingStub blockingHealthCheckStub;

	/**
	 * Construindo um cliente para se conectar ao servidor em {@code host:port}. SSL
	 * ativo por padrao, sendo possivel configurar via variavel de execucao
	 * useSSLGRPC
	 * 
	 * @param host
	 * @param port
	 * @throws SSLException
	 */
	public MedicaoGRPCClient(String host, int port) throws SSLException {

		this(useSSLGRPC ? ManagedChannelBuilder.forAddress(host, port).useTransportSecurity().build()
				: ManagedChannelBuilder.forAddress(host, port).usePlaintext(true).build());

		log.info("[CONTRATOS GRPC Client]: **Channel criado com sucesso na porta {} e no host {}", port, host);
	}

	/**
	 * Construindo um cliente para se conectar ao servidor em {@code host:port}. SSL
	 * configurado via parametro useSSL;
	 * 
	 * @param host
	 * @param port
	 * @param useSSL (true = Canal de conexao com SSL)
	 * @throws SSLException
	 */
	public MedicaoGRPCClient(String host, int port, Boolean useSSL) throws SSLException {

		this(useSSL ? ManagedChannelBuilder.forAddress(host, port).useTransportSecurity().build()
				: ManagedChannelBuilder.forAddress(host, port).usePlaintext(true).build());

		log.info("[CONTRATOS GRPC Client Construtor SSL]: **Channel criado com sucesso na porta {} e no host {}", port,
				host);
	}

	MedicaoGRPCClient(ManagedChannel channel) {
		this.channel = channel;
		blockingContratoStub = ContratoGrpc.newBlockingStub(channel);
		blockingHealthCheckStub = HealthGrpc.newBlockingStub(channel);
	}

	/**
	 * Finaliza execução
	 * 
	 * @throws Exception
	 */
	public void shutdown() throws Exception {
		channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
	}
	
	public Optional<ContratoDTO> getContratoPorContratoFk(long idProposta) {
		
		ContratoRequest request = ContratoRequest.newBuilder().setIdContrato(idProposta).build();

		ContratoResponse contrato = blockingContratoStub.consultarContrato(request);

		ContratoDTO dto = new ContratoDTO ();
		
		dto.setConfiguracaoIniciada(contrato.getConfiguracaoIniciada());
		dto.setMedicaoIniciada(contrato.getMedicaoIniciada());		
		
		return Optional.of(dto);		
		
	}
	
	/**
	 * Como chamar o gRPC a partir do Estaleiro (kubernets)
	 * 
	 * https://cloud.google.com/blog/topics/developers-practitioners/health-checking-your-grpc-servers-gke
	 * 
	 * Liveness Health Check Service
	 * 
	 * @return
	 */
	public HealthCheckResponse liveness() {
		
		HealthCheckRequest request = HealthCheckRequest.newBuilder().build();
		
		HealthCheckResponse healthCheck = blockingHealthCheckStub.check(request);

		return healthCheck;		
		
	}	

	/**
	 * Readiness Health Check Service
	 * 
	 * @return
	 */
	public HealthCheckResponse readiness() {
		
		HealthCheckRequest request = HealthCheckRequest.newBuilder().build();
		
		HealthCheckResponse healthCheck = blockingHealthCheckStub.ready(request);

		return healthCheck;
		
	}	
	

	public static void main(String[] args) throws Exception {
		String host = HOST;
		Integer porta = PORT;

		if (args.length > 0) {
			host = args[0];
			porta = Integer.parseInt(args[1]);
		}

		MedicaoGRPCClient client = null;
		
		if (host != null && porta != null) {

			client = new MedicaoGRPCClient(host, porta, false);
			
//			System.out.println(client.liveness());

			System.out.println(client.readiness());
			
//			Optional<ContratoDTO> contrato = client.getContratoPorContratoFk(16);
//			
//			if (contrato.isPresent()) {
//				logger.info("Configuracao iniciada: {} e Medicao iniciada: {} ", contrato.get().isConfiguracaoIniciada(),contrato.get().isMedicaoIniciada());
//			} else {
//				logger.info("Contrato não encontrado.");
//			}
		}
	}

}
