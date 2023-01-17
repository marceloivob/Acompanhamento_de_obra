package br.gov.planejamento.siconv.med.integration.util;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;

public abstract class AbstractGRPCConsumer {

    protected AbstractGRPCConsumer() {
        super();
    }

    protected boolean checkStatusException(Exception exception, Status expectedStatus) {
        return exception instanceof StatusRuntimeException
                && ((StatusRuntimeException) exception).getStatus().getCode() == expectedStatus.getCode();
    }
}
