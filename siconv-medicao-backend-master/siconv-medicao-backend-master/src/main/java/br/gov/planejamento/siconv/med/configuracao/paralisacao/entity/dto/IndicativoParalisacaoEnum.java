package br.gov.planejamento.siconv.med.configuracao.paralisacao.entity.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@RequiredArgsConstructor
@Getter
public enum IndicativoParalisacaoEnum {
	
	DECISAO_JUD_OU_ORGAO_CONTROLE(1, "Decisão judicial ou de órgão de controle interno ou externo"),
	DECLARACAO_EMPRESA_EXECUTORA(2, "Declaração de empresa executora"),
	DECLACARAO_ORGAO_ENTIDADE_ADM_PUBLICA_FEDERAL(3, "Declaração de órgão ou entidade da administração pública federal"),
	SEM_APRESENTAR_BM_PERIODO_MAIOR_90_DIAS(4, "Sem apresentação de boletim de medição por período igual ou superior a 90 dias"),
	OUTROS(5, "Outros");

	@Getter
	private final Integer codigo;
	
	@Getter
	private final String descricao;
	
	@JsonCreator
	public static IndicativoParalisacaoEnum fromCodigo(@JsonProperty("codigo") final Integer cod) {
        for (IndicativoParalisacaoEnum indicativo : IndicativoParalisacaoEnum.values()) {
            if (indicativo.getCodigo().equals(cod)) {
                return indicativo;
            }
        }

        throw new IllegalArgumentException("Não foi encontrado o Enum: " + cod);
    }
	
}
