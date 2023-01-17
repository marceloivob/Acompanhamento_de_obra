package br.gov.planejamento.siconv.med.configuracao.paralisacao.entity.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@RequiredArgsConstructor
@Getter
public enum MotivoParalisacaoEnum {
	
	ACAO_JUDICIAL(1, "Ação judicial"),
	ALTO_REAJUSTE(2, "Alto reajuste dos valores de material"),
	AUSENCIA_RECURSO(3, "Ausência de recursos orçamentário/financeiro"),
	BAIXA_GOVERNANCA(4, "Baixa governança sobre o objeto/localidade de recurso de emenda parlamentar"),
	CARENCIA_MATERIAL(5, "Carência no mercado local de materiais"),
	CONSTANTE_ALTERACAO_PRECO(6, "Constantes necessidade de realinhamentos de preços"),
	DEMORA_LIBERACAO(7, "Demora na liberação de recursos pela união, acarretando aumento no valor do bem e desistência do fornecedor"),
	DESISTENCIA_COM_JUST(8, "Desistência ou abandono pela empresa com justificativa"),
	DESISTENCIA_SEM_JUST(9, "Desistência ou abandono pela empresa sem justificativa"),
	DESVIO_FINALIDADE(10, "Desvio de finalidade (do objeto)"),
	DIFICULDADE_TECNICA(11, "Dificuldades técnicas da organização executora"),
	EXCESSO_BUROCRACIA(12, "Excesso de burocracia (mandatária)"),
	EXECUCAO_DESACORDO_PROJETO(13, "Execução em desacordo com o projeto"),
	FALTA_RECURSO_CONTRAPARTIDA(14, "Falta de recursos de contrapartida"),
	FALTA_TIT_OU_DESAPROPRIACAO(15, "Falta de titularidade e/ou desapropriação"),
	FALTA_EQUIPE_TECNICA(16, "Falta equipe técnica nos municípios para operacionalizar os instrumentos"),
	INADIMPLENCIA_EMP_EXE(17, "Inadimplência da empresa executora"),
	LOCAL_GEO_DIFICIL(18, "Localização geográfica de difícil acesso para entrega de materiais e serviços"),
	NAO_OBTENCAO_LIC_AUT(19, "Não obtenção de licenças, autorizações ou outros instrumentos equivalentes"),
	INDICE_PLUV_ELEVADO(20, "Índices pluviométricos elevados em decorrência de chuvas"),
	PERDA_PRAZO_PRORROGACAO(21, "Perda de prazo / prorrogação de vigência"),
	PROBLEMA_GARANTIA_CONTRATO(22, "Problemas na garantia contratual"),
	PROBLEMA_TECNICO_EXE(23, "Problemas técnicos de execução"),
	PROJ_MAL_ELABORADO(24, "Projeto mal elaborado com impacto no licitatório"),
	RESCISAO_CONTRATUAL(25, "Rescisão contratual"),
	REVISAO_PROJ_BAS(26, "Revisão de projeto básico"),
	REVISAO_PROJ_EXE(27, "Revisão de projeto executivo");

	@Getter
	private final Integer codigo;
	
	@Getter
	private final String descricao;
	
	@JsonCreator
	public static MotivoParalisacaoEnum fromCodigo(@JsonProperty("codigo") final Integer cod) {
        for (MotivoParalisacaoEnum motivo : MotivoParalisacaoEnum.values()) {
            if (motivo.getCodigo().equals(cod)) {
                return motivo;
            }
        }

        throw new IllegalArgumentException("Não foi encontrado o Enum: " + cod);
    }
	
}
