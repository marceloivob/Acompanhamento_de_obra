package br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.dto;

public enum AtividadeRegistroProfissionalEnum {

	ARQ("Arquitetura"),
    ENG("Engenharia"),
    SOC("Trabalho Social");

	private String descricao;

    private AtividadeRegistroProfissionalEnum(String descricao) {
		this.descricao = descricao;
	}

    public String getDescricao() {
		return descricao;
	}

    public static AtividadeRegistroProfissionalEnum fromName(final String name) {
        for (AtividadeRegistroProfissionalEnum tipo : AtividadeRegistroProfissionalEnum.values()) {
            if (tipo.name().equalsIgnoreCase(name)) {
                return tipo;
            }
        }

        throw new IllegalArgumentException("Não foi encontrado o Enum: " + name);
    }
    
    public static AtividadeRegistroProfissionalEnum fromDescricao(final String descricao) {
        for (AtividadeRegistroProfissionalEnum tipo : AtividadeRegistroProfissionalEnum.values()) {
            if (tipo.descricao.equalsIgnoreCase(descricao)) {
                return tipo;
            }
        }

        throw new IllegalArgumentException("Não foi encontrado o Enum: " + descricao);
    }
    
}
