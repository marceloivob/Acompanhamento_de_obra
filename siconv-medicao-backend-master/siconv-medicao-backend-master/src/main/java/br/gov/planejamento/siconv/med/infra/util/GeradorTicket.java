package br.gov.planejamento.siconv.med.infra.util;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class GeradorTicket {

    /**
     * Pattern para formatação de data.
     */
    private static final String DATE_FORMAT_PATTERN = "yyyyMMddHHmmss";

    public String gerar() {
        final StringBuilder ticket = new StringBuilder();
        final String currentTime = getCurrentTimeFormatted();
        ticket.append(currentTime);

        String ticketSessionId = gerarSessionIdAleatorio();

        ticket.append(ticketSessionId);

        return ticket.toString();
    }

    private String getCurrentTimeFormatted() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN);
        return formatter.format(LocalDateTime.now());
    }

    private String gerarSessionIdAleatorio() {
    
	    SecureRandom sr = new SecureRandom();
	    long codigoAleatorio = sr.nextLong();
	    codigoAleatorio = Math.abs(codigoAleatorio) % 10000000000L;
	    return Long.toString(codigoAleatorio);
    }
    
}
