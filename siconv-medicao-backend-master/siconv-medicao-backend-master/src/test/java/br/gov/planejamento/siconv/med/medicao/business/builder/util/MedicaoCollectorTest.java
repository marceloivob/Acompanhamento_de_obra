package br.gov.planejamento.siconv.med.medicao.business.builder.util;

import static br.gov.planejamento.siconv.med.medicao.business.builder.util.MedicaoCollector.ultimaMedicao;
import static br.gov.planejamento.siconv.med.medicao.business.builder.util.MedicaoCollector.ultimaMedicaoPorSituacao;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.AC;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.CE;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.EM;
import static java.util.Optional.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;

import java.util.List;

import org.junit.jupiter.api.Test;

import br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum;
import br.gov.planejamento.siconv.med.medicao.entity.dto.MedicaoDTO;

class MedicaoCollectorTest {

    @Test
    void testUltimaMedicao() {

        MedicaoDTO med1 = buildMedicao(1);
        MedicaoDTO med2 = buildMedicao(2);
        MedicaoDTO med3 = buildMedicao(3);
        MedicaoDTO med4 = buildMedicao(4);

        List<MedicaoDTO> listaMedicoes = List.of(med3, med4, med1, med2); // ordem aleatoria

        assertThat(listaMedicoes.stream().collect(ultimaMedicao()), equalTo(of(med4)));
    }

    @Test
    void testUltimaMedicaoAgrupadaPorSituacao() {

        MedicaoDTO med1 = buildMedicao(1, AC);
        MedicaoDTO med2 = buildMedicao(2, EM);
        MedicaoDTO med3 = buildMedicao(3, CE);
        MedicaoDTO med4 = buildMedicao(4, CE);
        MedicaoDTO med5 = buildMedicao(5, AC);
        MedicaoDTO med6 = buildMedicao(6, AC);

        List<MedicaoDTO> listaMedicoes = List.of(med1, med2, med6, med3, med5, med4); // ordem aleatoria

        assertThat(listaMedicoes.stream().collect(ultimaMedicaoPorSituacao()), allOf(aMapWithSize(3),
                                                                                     hasEntry(AC, of(med6)),
                                                                                     hasEntry(CE, of(med4)),
                                                                                     hasEntry(EM, of(med2))));
    }

    private MedicaoDTO buildMedicao(int sequencial) {
        return buildMedicao(sequencial, null);
    }

    private MedicaoDTO buildMedicao(int sequencial, SituacaoMedicaoEnum situacao) {
        MedicaoDTO medicao = new MedicaoDTO();
        medicao.setSequencial((short) sequencial);
        medicao.setSituacao(situacao);
        return medicao;
    }
}
