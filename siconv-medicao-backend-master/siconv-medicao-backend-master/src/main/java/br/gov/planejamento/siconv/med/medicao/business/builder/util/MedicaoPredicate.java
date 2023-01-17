package br.gov.planejamento.siconv.med.medicao.business.builder.util;

import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.EC;

import java.util.function.Predicate;

import br.gov.planejamento.siconv.med.medicao.entity.dto.MedicaoDTO;

/**
 * Implementações de funcões {@link Predicate} para objetos {@link MedicaoDTO}
 * que são úteis para aplicar filtros em Collections ou Streams.
 */
public final class MedicaoPredicate {

    private MedicaoPredicate() {
    }

    public static Predicate<MedicaoDTO> medicaoEnviadaConvenenteNaoAcumulada() {
        return medicao -> medicao.getSituacao() == EC && !medicao.isAcumulada();
    }
}
