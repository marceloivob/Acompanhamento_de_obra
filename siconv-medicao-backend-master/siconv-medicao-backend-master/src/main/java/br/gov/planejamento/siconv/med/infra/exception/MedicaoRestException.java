package br.gov.planejamento.siconv.med.infra.exception;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Response.Status;

import br.gov.planejamento.siconv.med.infra.message.Message;
import br.gov.planejamento.siconv.med.infra.message.MessageKey;

public class MedicaoRestException extends RuntimeException {

    private static final long serialVersionUID = -4343099397448377632L;

    private final int statusCode;

    private final HashSet<Message> messages = new HashSet<>();

    private final SeverityEnum severity;
    
    public MedicaoRestException() {
        this(Status.PRECONDITION_FAILED.getStatusCode()); // Default status
    }

    public MedicaoRestException(int statusCode) {
        super();
        this.statusCode = statusCode;
        this.severity = SeverityEnum.ERROR;
    }

    public MedicaoRestException(Message message) {
        this();
        addMessage(message);
    }

    public MedicaoRestException(Message message, int statusCode) {
        this(statusCode);
        addMessage(message);
    }

    public MedicaoRestException(MessageKey key) {
        this(new Message(key));
    }

    public MedicaoRestException(MessageKey key, int statusCode) {
        this(new Message(key), statusCode);
    }

    public MedicaoRestException(MessageKey key, String... arguments) {
        this(new Message(key, arguments));
    }

    public MedicaoRestException(Set<Message> messages) {
        this();
        this.messages.addAll(messages);
    }

    public MedicaoRestException(Set<Message> messages, int statusCode) {
        this(statusCode);
        this.messages.addAll(messages);
    }

    public MedicaoRestException(MessageKey key, SeverityEnum severity) {
    	this.statusCode = Status.PRECONDITION_FAILED.getStatusCode();
		this.severity = severity;
		addMessage(new Message(key));
	}

	public MedicaoRestException addMessage(Message message) {
        messages.add(message);
        return this;
    }

    public MedicaoRestException addMessage(MessageKey key) {
        return addMessage(new Message(key));
    }

    public int getStatusCode() {
        return statusCode;
    }

    public Set<Message> getMessages() {
        return messages;
    }

	public SeverityEnum getSeverity() {
		return severity;
	}
    
}
