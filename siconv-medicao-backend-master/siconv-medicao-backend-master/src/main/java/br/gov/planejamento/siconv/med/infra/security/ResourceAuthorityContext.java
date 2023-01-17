package br.gov.planejamento.siconv.med.infra.security;

import lombok.Data;

@Data
public class ResourceAuthorityContext {

    private final String idProposta;

    private final String cnpjEmpresa;
}