package br.gov.planejamento.siconv.med.medicao.business.builder;

import static br.gov.planejamento.siconv.med.test.builder.ContextBuilder.newContextBuilder;
import static java.math.BigDecimal.valueOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

import br.gov.planejamento.siconv.med.medicao.business.builder.CalculoValoresServicoStep;
import br.gov.planejamento.siconv.med.medicao.business.builder.SubmetaMedicaoBuilder.Context;
import br.gov.planejamento.siconv.med.medicao.entity.dto.MedicaoDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.ServicoVrplDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.SubmetaMedicaoDTO;
import br.gov.planejamento.siconv.med.test.builder.ServicoVrplDTOBuilder;
import br.gov.planejamento.siconv.med.test.builder.SubmetaMedicaoDTOBuilder;
import br.gov.planejamento.siconv.med.test.extension.BusinessControllerBaseTest;

class CalculoValoresServicoStepTest extends BusinessControllerBaseTest {

    @InjectMocks
    private CalculoValoresServicoStep calculoValoresStep;

    @Test
    void testServicoSemPreenchimento() {

        Long idMedicao = 1L;

        ServicoVrplDTO servico = getServicoBuilder()
                                     .comQtdPlanejada(valueOf(10))
                                     .comPreco(valueOf(2.25))
                                     .build();

        calculoValoresStep.process(buildSubmetaComServico(servico), buildContextComMedicao(idMedicao));

        assertThat(servico.getVlTotalServico(), comparesEqualTo(valueOf(22.50)));

        assertNull(servico.getQtdRealizadoEmpresa());
        assertNull(servico.getValorRealizadoEmpresa());
        assertNull(servico.getQtdAcumuladoEmpresa());
        assertNull(servico.getValorAcumuladoEmpresa());

        assertNull(servico.getQtdRealizadoConvenente());
        assertNull(servico.getValorRealizadoConvenente());
        assertNull(servico.getQtdAcumuladoConvenente());
        assertNull(servico.getValorAcumuladoConvenente());

        assertNull(servico.getQtdRealizadoConcedente());
        assertNull(servico.getValorRealizadoConcedente());
        assertNull(servico.getQtdAcumuladoConcedente());
        assertNull(servico.getValorAcumuladoConcedente());
    }

    @Test
    void testServicoApenasComValoresEmpresa() {

        Long idMedicao = 2L;

        ServicoVrplDTO servico = getServicoBuilder()
                                     .comQtdPlanejada(valueOf(86.88))
                                     .comPreco(valueOf(2))
                                     .comPreenchimentoMedicao(1L, valueOf(10), null, null)
                                     .comPreenchimentoMedicao(2L, valueOf(33.44), null, null)
                                     .comPreenchimentoMedicao(3L, valueOf(77.66), null, null)
                                     .build();

        calculoValoresStep.process(buildSubmetaComServico(servico), buildContextComMedicao(idMedicao));

        assertThat(servico.getVlTotalServico(), comparesEqualTo(valueOf(173.76)));

        assertThat(servico.getQtdRealizadoEmpresa(), comparesEqualTo(valueOf(33.44)));
        assertThat(servico.getValorRealizadoEmpresa(), comparesEqualTo(valueOf(66.88)));
        assertThat(servico.getQtdAcumuladoEmpresa(), comparesEqualTo(valueOf(43.44)));
        assertThat(servico.getValorAcumuladoEmpresa(), comparesEqualTo(valueOf(86.88)));

        assertNull(servico.getQtdRealizadoConvenente());
        assertNull(servico.getValorRealizadoConvenente());
        assertNull(servico.getQtdAcumuladoConvenente());
        assertNull(servico.getValorAcumuladoConvenente());

        assertNull(servico.getQtdRealizadoConcedente());
        assertNull(servico.getValorRealizadoConcedente());
        assertNull(servico.getQtdAcumuladoConcedente());
        assertNull(servico.getValorAcumuladoConcedente());
    }

    @Test
    void testServicoComValoresEmpresaConvenente() {

        Long idMedicao = 3L;

        ServicoVrplDTO servico = getServicoBuilder()
                                     .comQtdPlanejada(valueOf(111))
                                     .comPreco(valueOf(10))
                                     .comPreenchimentoMedicao(1L, valueOf(50), valueOf(5), null)
                                     .comPreenchimentoMedicao(2L, valueOf(61), valueOf(33.44), null)
                                     .comPreenchimentoMedicao(3L, null, valueOf(10), null)
                                     .build();

        calculoValoresStep.process(buildSubmetaComServico(servico), buildContextComMedicao(idMedicao));

        assertThat(servico.getVlTotalServico(), comparesEqualTo(valueOf(1110)));

        assertNull(servico.getQtdRealizadoEmpresa());
        assertNull(servico.getValorRealizadoEmpresa());
        assertThat(servico.getQtdAcumuladoEmpresa(), comparesEqualTo(valueOf(111)));
        assertThat(servico.getValorAcumuladoEmpresa(), comparesEqualTo(valueOf(1110)));

        assertThat(servico.getQtdRealizadoConvenente(), comparesEqualTo(valueOf(10)));
        assertThat(servico.getValorRealizadoConvenente(), comparesEqualTo(valueOf(100)));
        assertThat(servico.getQtdAcumuladoConvenente(), comparesEqualTo(valueOf(48.44)));
        assertThat(servico.getValorAcumuladoConvenente(), comparesEqualTo(valueOf(484.4)));

        assertNull(servico.getQtdRealizadoConcedente());
        assertNull(servico.getValorRealizadoConcedente());
        assertNull(servico.getQtdAcumuladoConcedente());
        assertNull(servico.getValorAcumuladoConcedente());
    }

    @Test
    void testServicoComValoresTodosPerfis() {

        Long idMedicao = 4L;

        ServicoVrplDTO servico = getServicoBuilder()
                                     .comQtdPlanejada(valueOf(80.45))
                                     .comPreco(valueOf(33.12))
                                     .comPreenchimentoMedicao(1L, valueOf(23.44), null, null)
                                     .comPreenchimentoMedicao(2L, valueOf(12.89), valueOf(30), null)
                                     .comPreenchimentoMedicao(3L, null, valueOf(6.33), valueOf(36.33))
                                     .comPreenchimentoMedicao(4L, valueOf(44.12), valueOf(40), valueOf(30))
                                     .build();

        calculoValoresStep.process(buildSubmetaComServico(servico), buildContextComMedicao(idMedicao));

        assertThat(servico.getVlTotalServico(), comparesEqualTo(valueOf(2664.5)));

        assertThat(servico.getQtdRealizadoEmpresa(), comparesEqualTo(valueOf(44.12)));
        assertThat(servico.getValorRealizadoEmpresa(), comparesEqualTo(valueOf(1461.25)));
        assertThat(servico.getQtdAcumuladoEmpresa(), comparesEqualTo(valueOf(80.45)));
        assertThat(servico.getValorAcumuladoEmpresa(), comparesEqualTo(valueOf(2664.5)));

        assertThat(servico.getQtdRealizadoConvenente(), comparesEqualTo(valueOf(40)));
        assertThat(servico.getValorRealizadoConvenente(), comparesEqualTo(valueOf(1324.8)));
        assertThat(servico.getQtdAcumuladoConvenente(), comparesEqualTo(valueOf(76.33)));
        assertThat(servico.getValorAcumuladoConvenente(), comparesEqualTo(valueOf(2528.05)));

        assertThat(servico.getQtdRealizadoConcedente(), comparesEqualTo(valueOf(30)));
        assertThat(servico.getValorRealizadoConcedente(), comparesEqualTo(valueOf(993.6)));
        assertThat(servico.getQtdAcumuladoConcedente(), comparesEqualTo(valueOf(66.33)));
        assertThat(servico.getValorAcumuladoConcedente(), comparesEqualTo(valueOf(2196.85)));
    }

    private SubmetaMedicaoDTO buildSubmetaComServico(ServicoVrplDTO servico) {
        return new SubmetaMedicaoDTOBuilder(1L, 1L, 1L).servicos(List.of(servico)).create();
    }

    private Context buildContextComMedicao(Long idMedicao) {
        MedicaoDTO medicao = new MedicaoDTO();
        medicao.setId(idMedicao);

        return newContextBuilder().setContext(null, medicao, null, null).create();
    }

    private ServicoVrplDTOBuilder getServicoBuilder() {
        return new ServicoVrplDTOBuilder();
    }
}
