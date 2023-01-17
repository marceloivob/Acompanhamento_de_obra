package br.gov.planejamento.siconv.med.medicao.business.builder.util;

import static br.gov.planejamento.siconv.med.medicao.business.builder.util.MedicaoPredicate.medicaoEnviadaConvenenteNaoAcumulada;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.ACT;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.ATD;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.CE;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.EC;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.ECE;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.EM;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

import java.util.List;

import org.junit.jupiter.api.Test;

import br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum;
import br.gov.planejamento.siconv.med.medicao.entity.dto.MedicaoDTO;

class MedicaoPredicateTest {

    @Test
    void testMedicaoEnviadaConvenenteNaoAcumulada() {

        List<MedicaoDTO> listaMedicoes = List.of(buildMedicao(EC, null),
                                                 buildMedicao(EM, null),
                                                 buildMedicao(ATD, null),
                                                 buildMedicao(EC, 1L),
                                                 buildMedicao(ACT, null),
                                                 buildMedicao(CE, 2L),
                                                 buildMedicao(ECE, 3L),
                                                 buildMedicao(EC, null));

        List<MedicaoDTO> listaFiltrada = listaMedicoes.stream()
                                                      .filter(medicaoEnviadaConvenenteNaoAcumulada())
                                                      .collect(toList());

        assertThat(listaFiltrada, hasSize(2));
    }

    private MedicaoDTO buildMedicao(SituacaoMedicaoEnum situacao, Long idMedAgrupadora) {
        MedicaoDTO medicao = new MedicaoDTO();
        medicao.setSituacao(situacao);
        medicao.setIdMedicaoAgrupadora(idMedAgrupadora);
        return medicao;
    }
}
