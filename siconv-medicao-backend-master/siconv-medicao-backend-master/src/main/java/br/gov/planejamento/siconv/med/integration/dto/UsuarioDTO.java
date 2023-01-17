package br.gov.planejamento.siconv.med.integration.dto;

import lombok.Data;

@Data
public class UsuarioDTO {

    private Long id;
    private String cpf;
    private String nome;
    private String email;
    private boolean vinculadoConvenioAtual;
    private boolean vinculadoOutroConvenio;
    private boolean fiscalConvenente;
    private boolean vinculadoEmpresa;
    private boolean assinanteSubmetaEmpresa;
    private boolean ativo = true;
}
