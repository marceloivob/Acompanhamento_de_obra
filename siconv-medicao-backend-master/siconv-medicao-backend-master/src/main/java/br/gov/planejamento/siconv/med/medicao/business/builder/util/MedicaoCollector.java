package br.gov.planejamento.siconv.med.medicao.business.builder.util;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.maxBy;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collector;

import br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum;
import br.gov.planejamento.siconv.med.medicao.entity.dto.MedicaoDTO;

/**
 * Implementações de funcões {@link Collector} para objetos {@link MedicaoDTO}
 * que são úteis para aplicar filtros em Collections ou Streams.
 */
public final class MedicaoCollector {

    private MedicaoCollector() {
    }

    public static Collector<MedicaoDTO, ?, Optional<MedicaoDTO>> ultimaMedicao() {
        return maxBy(comparing(MedicaoDTO::getSequencial));
    }

    public static Collector<MedicaoDTO, ?, Map<SituacaoMedicaoEnum, Optional<MedicaoDTO>>> ultimaMedicaoPorSituacao() {
        return groupingBy(MedicaoDTO::getSituacao, ultimaMedicao());
    }
}
