package br.gov.planejamento.siconv.med.test.builder;

import br.gov.planejamento.siconv.med.integration.dto.UsuarioDTO;

public class UsuarioDTOBuilder {

    private UsuarioDTO usuario = new UsuarioDTO();

    public UsuarioDTOBuilder() {
    }

    public static UsuarioDTOBuilder newUsuarioDTOBuilder() {
        return new UsuarioDTOBuilder();
    }

    public UsuarioDTO create() {
        return this.usuario;
    }

    public UsuarioDTOBuilder setCPF(String cpf) {
        this.usuario.setCpf(cpf);
        return this;
    }

    public UsuarioDTOBuilder setNome(String nome) {
        this.usuario.setNome(nome);
        return this;
    }

    public UsuarioDTOBuilder vinculadoConvenioAtual() {
        this.usuario.setVinculadoConvenioAtual(true);
        return this;
    }

    public UsuarioDTOBuilder vinculadoEmpresa() {
        this.usuario.setVinculadoEmpresa(true);
        return this;
    }

    public UsuarioDTOBuilder assinanteSubmetaEmpresa() {
        this.usuario.setAssinanteSubmetaEmpresa(true);
        return this;
    }

    public UsuarioDTOBuilder fiscalConvenente() {
        this.usuario.setFiscalConvenente(true);
        return this;
    }

    public UsuarioDTOBuilder inativo() {
        this.usuario.setAtivo(false);
        return this;
    }
}
