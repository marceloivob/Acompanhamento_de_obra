package br.gov.planejamento.siconv.med.acompanhamento.entity.dto;

import static java.util.Comparator.comparing;

import java.util.Comparator;

import br.gov.planejamento.siconv.med.infra.util.NumeroSubmetaFormatadoComparator;
import br.gov.planejamento.siconv.med.medicao.entity.dto.ValoresSubmetaDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class SubmetaContratoLoteDTO extends ValoresSubmetaDTO {

    private Long id;
    private String numero; // Formato: <número meta>.<número submeta>
    private String descricao;
    private String situacao;
    private String regimeExecucao;

    /**
     * Comparador utilizado para ordenação ascendente padrão, considerando a
     * composição do número da meta com o número próprio da submeta.
     */
    public static final Comparator<SubmetaContratoLoteDTO> ORDENACAO_PADRAO = comparing(
            SubmetaContratoLoteDTO::getNumero, NumeroSubmetaFormatadoComparator.INSTANCE);

}
