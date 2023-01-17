package br.gov.planejamento.siconv.med.medicao.entity.dto;

import static java.util.Comparator.comparing;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import br.gov.planejamento.siconv.med.infra.util.NumeroSubmetaFormatadoComparator;
import lombok.Data;

@Data
public class SubmetaVrplDTO {

    private Long id;

    private String descricao;

    private BigDecimal valor;

    private String nrSubmetaAnalise; // Formato: <número meta>.<número submeta>

    @JsonInclude(value = Include.NON_EMPTY)
    private List<FrenteObraVrplDTO> frentesObras = new ArrayList<>();

    public FrenteObraVrplDTO addFrentesObras(FrenteObraVrplDTO frenteObraDTO) {
        int pos = this.frentesObras.indexOf(frenteObraDTO);
        if (pos == -1) {
            this.frentesObras.add(frenteObraDTO);
            return frenteObraDTO;
        }

        return this.frentesObras.get(pos);
    }

    /**
     * Comparador utilizado para ordenação ascendente padrão, considerando a
     * composição do número da meta com o número próprio da submeta.
     */
    public static final Comparator<SubmetaVrplDTO> ORDENACAO_PADRAO = comparing(SubmetaVrplDTO::getNrSubmetaAnalise,
            NumeroSubmetaFormatadoComparator.INSTANCE);
}
