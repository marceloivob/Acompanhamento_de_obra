package br.gov.planejamento.siconv.med.empresa.entity.dto;

import lombok.Data;

@Data
public class EmpresaDTO {

    private Long id;
    private String cnpj;
    private String razaoSocial;
    private Integer qtdContratos;
}
