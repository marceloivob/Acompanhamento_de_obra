package br.gov.planejamento.siconv.med.infra.exception;

public class GrpcIntegrationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public GrpcIntegrationException(Throwable cause) {
        super("Ocorreu uma falha durante a integração com um serviço GRPC.", cause);
    }
}
