package br.gov.planejamento.siconv.med.infra.exception;

import javax.ws.rs.core.Response.Status;

import br.gov.planejamento.siconv.med.infra.message.MessageKey;

public class SecurityAccessException extends MedicaoRestException {

    private static final long serialVersionUID = 1L;

    public SecurityAccessException() {
        super(MessageKey.ERRO_ACESSO_PERFIL_NAO_AUTORIZADO, Status.FORBIDDEN.getStatusCode());
    }

    public SecurityAccessException(MessageKey key, int statusCode) {
        super(key, statusCode);
    }

    public SecurityAccessException(MessageKey msg) {
        super(msg);
    }
}
