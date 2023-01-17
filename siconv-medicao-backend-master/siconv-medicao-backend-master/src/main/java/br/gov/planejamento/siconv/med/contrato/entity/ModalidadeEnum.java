package br.gov.planejamento.siconv.med.contrato.entity;

import com.fasterxml.jackson.annotation.JsonFormat;


@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ModalidadeEnum {

    CONVENIO(1, "Convênio"), 
    CONTRATO_REPASSE(2, "Contrato de Repasse"),
    TERMO_DE_COOPERACAO(3, "Termo de Cooperação"),
    TERMO_PARCERIA(5, "Termo de Parceria"),
    CONVENIO_CONTRATO_DE_REPASSE(6, "Convênio e Contrato de Repasse"),
    TERMO_COLABORACAO(8, "Termo de Colaboração"),
    TERMO_FOMENTO(9, "Termo de Fomento"),
    TERMO_DE_COMPROMISSO_CONCEDENTE (11, "Termo de Compromisso", false),
    TERMO_DE_COMPROMISSO_MANDATARIA (11, "Termo de Compromisso", true);


    private final Integer codigo;
    private final String descricao;
    private Boolean possuiInstituicaoMandataria;

    private ModalidadeEnum(final Integer codigo, final String descricao) {
        this.codigo = codigo;
        this.descricao = descricao;
    }
    
    private ModalidadeEnum(final Integer codigo, final String descricao, Boolean possuiInstituicaoMandataria) {
        this.codigo = codigo;
        this.descricao = descricao;
        this.possuiInstituicaoMandataria = possuiInstituicaoMandataria;
    }

    public String getDescricao() {
        return descricao;
    }

    public int getCodigo() {
        return codigo;
    }

    public static ModalidadeEnum fromCodigo(Integer codigo, Boolean possuiInstituicaoMandataria) {
    	
    	if(possuiInstituicaoMandataria == null || codigo != TERMO_DE_COMPROMISSO_CONCEDENTE.getCodigo()) {
    		return fromCodigo(codigo);
    	} 
    	
        if (codigo == TERMO_DE_COMPROMISSO_CONCEDENTE.getCodigo() && !possuiInstituicaoMandataria.booleanValue()) {
            return TERMO_DE_COMPROMISSO_CONCEDENTE;
        } else if (codigo == TERMO_DE_COMPROMISSO_MANDATARIA.getCodigo() && possuiInstituicaoMandataria.booleanValue()) {
        	return TERMO_DE_COMPROMISSO_MANDATARIA;
        }
    	
        throw new IllegalArgumentException("Modalidade desconhecida: " + codigo);

    }
    
    public static ModalidadeEnum fromCodigo(Integer codigo) {
        for (ModalidadeEnum modalidade : ModalidadeEnum.values()) {
            if (modalidade.getCodigo() == codigo) {
                return modalidade;
            }
        }

        throw new IllegalArgumentException("Modalidade desconhecida: " + codigo);
    }

	public Boolean getPossuiInstituicaoMandataria() {
		return possuiInstituicaoMandataria;
	}

}
