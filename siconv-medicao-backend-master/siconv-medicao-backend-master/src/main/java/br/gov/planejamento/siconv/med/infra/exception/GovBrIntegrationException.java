package br.gov.planejamento.siconv.med.infra.exception;

public class GovBrIntegrationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public GovBrIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
