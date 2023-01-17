package br.gov.planejamento.siconv.med.infra.exception;

import javax.ws.rs.core.Response.Status;

import br.gov.planejamento.siconv.med.infra.message.MessageKey;

public class ConcurrencyException extends MedicaoRestException {

	private static final long serialVersionUID = 1L;

	public ConcurrencyException() {
		super(MessageKey.ERRO_GERAL_CONCORRENCIA, Status.CONFLICT.getStatusCode());
	}

	public ConcurrencyException(int statusCode) {
		super(MessageKey.ERRO_GERAL_CONCORRENCIA, statusCode);
	}

	public ConcurrencyException(MessageKey msg) {
		super(msg, Status.CONFLICT.getStatusCode());
	}
}
