package br.gov.planejamento.siconv.med.infra.message;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@AllArgsConstructor
@RequiredArgsConstructor
public class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    @Getter
    @NonNull
    private MessageKey key;

    @Getter
    private String[] arguments;

}
