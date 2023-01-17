package br.gov.planejamento.siconv.med.medicao.business.builder;

import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.AC;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.ACT;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.AT;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.ATD;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.EC;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.ECC;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.ECE;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.EM;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum;
import br.gov.planejamento.siconv.med.medicao.entity.dto.MedicaoDTO;

class IndicadoresAcaoListaMedicoesBuilderTest {

    @Test
    void testCancelarEnvioConvenente() {

        MedicaoDTO med1 = buildMedicao(1, EC);
        MedicaoDTO med2 = buildMedicao(2, EC);

        IndicadoresAcaoListaMedicoesBuilder.of(List.of(med1, med2)).build();

        assertFalse(med1.isPermiteCancelarEnvio());
        assertTrue(med2.isPermiteCancelarEnvio());
    }

    @Test
    void testCancelarEnvioConvenenteComMedicaoEmElaboracao() {

        MedicaoDTO med1 = buildMedicao(1, EC);
        MedicaoDTO med2 = buildMedicao(2, EC);
        MedicaoDTO med3 = buildMedicao(3, EM);

        IndicadoresAcaoListaMedicoesBuilder.of(List.of(med1, med2, med3)).build();

        assertFalse(med1.isPermiteCancelarEnvio());
        assertFalse(med2.isPermiteCancelarEnvio());
    }

    @Test
    void testCancelarAteste() {

        MedicaoDTO med1 = buildMedicao(1, ATD);
        MedicaoDTO med2 = buildMedicao(2, ATD);
        MedicaoDTO med3 = buildMedicao(3, EM);

        IndicadoresAcaoListaMedicoesBuilder.of(List.of(med1, med2, med3)).build();

        assertFalse(med1.isPermiteCancelarEnvio());
        assertTrue(med2.isPermiteCancelarEnvio());
    }

    @Test
    void testCancelarAtesteComMedicaoEmAteste() {

        MedicaoDTO med1 = buildMedicao(1, ATD);
        MedicaoDTO med2 = buildMedicao(2, ATD);
        MedicaoDTO med3 = buildMedicao(3, AT);

        IndicadoresAcaoListaMedicoesBuilder.of(List.of(med1, med2, med3)).build();

        assertFalse(med1.isPermiteCancelarEnvio());
        assertFalse(med2.isPermiteCancelarEnvio());
    }

    @Test
    void testCancelarAceite() {

        MedicaoDTO med1 = buildMedicao(1, ACT);
        MedicaoDTO med2 = buildMedicao(2, ACT);
        MedicaoDTO med3 = buildMedicao(3, ATD);
        MedicaoDTO med4 = buildMedicao(4, EC);

        IndicadoresAcaoListaMedicoesBuilder.of(List.of(med1, med2, med3, med4)).build();

        assertFalse(med1.isPermiteCancelarAceite());
        assertTrue(med2.isPermiteCancelarAceite());
    }

    @Test
    void testCancelarAceiteComMedicaoEmAnalise() {

        MedicaoDTO med1 = buildMedicao(1, ACT);
        MedicaoDTO med2 = buildMedicao(2, ACT);
        MedicaoDTO med3 = buildMedicao(3, AC);
        MedicaoDTO med4 = buildMedicao(4, EC);

        IndicadoresAcaoListaMedicoesBuilder.of(List.of(med1, med2, med3, med4)).build();

        assertFalse(med1.isPermiteCancelarAceite());
        assertFalse(med2.isPermiteCancelarAceite());
    }

    @Test
    void testCancelarAceiteComMedicaoEmComplementacaoAnalise() {

        MedicaoDTO med1 = buildMedicao(1, ACT);
        MedicaoDTO med2 = buildMedicao(2, ACT);
        MedicaoDTO med3 = buildMedicaoComComplementacaoAnalise(3, EC);

        IndicadoresAcaoListaMedicoesBuilder.of(List.of(med1, med2, med3)).build();

        assertFalse(med1.isPermiteCancelarAceite());
        assertFalse(med2.isPermiteCancelarAceite());
    }

    @Test
    void testCancelarEnvioComplementacaoEmpresa() {

        MedicaoDTO med1 = buildMedicao(1, ECE);
        MedicaoDTO med2 = buildMedicao(2, ECE);
        MedicaoDTO med3 = buildMedicao(3, EM);

        IndicadoresAcaoListaMedicoesBuilder.of(List.of(med1, med2, med3)).build();

        assertFalse(med1.isPermiteCancelarEnvioParaComplementacao());
        assertTrue(med2.isPermiteCancelarEnvioParaComplementacao());
    }

    @ParameterizedTest
    @ValueSource(strings = { "AT", "CC", "ECC" })
    void testCancelarEnvioComplementacaoEmpresaComMedicaoPosterior(String codigoSituacaoPosterior) {

        MedicaoDTO med1 = buildMedicao(1, ECE);
        MedicaoDTO med2 = buildMedicao(2, ECE);
        MedicaoDTO med3 = buildMedicao(3, SituacaoMedicaoEnum.fromCodigo(codigoSituacaoPosterior));

        IndicadoresAcaoListaMedicoesBuilder.of(List.of(med1, med2, med3)).build();

        assertFalse(med1.isPermiteCancelarEnvioParaComplementacao());
        assertFalse(med2.isPermiteCancelarEnvioParaComplementacao());
    }

    @Test
    void testCancelarEnvioComplementacaoConvenente() {

        MedicaoDTO med1 = buildMedicao(1, ECC);
        MedicaoDTO med2 = buildMedicao(2, ECC);
        MedicaoDTO med3 = buildMedicao(3, AT);

        IndicadoresAcaoListaMedicoesBuilder.of(List.of(med1, med2, med3)).build();

        assertFalse(med1.isPermiteCancelarEnvioParaComplementacao());
        assertTrue(med2.isPermiteCancelarEnvioParaComplementacao());
    }

    @Test
    void testCancelarEnvioComplementacaoConvenenteComMedicaoEmAnalise() {

        MedicaoDTO med1 = buildMedicao(1, ECC);
        MedicaoDTO med2 = buildMedicao(2, ECC);
        MedicaoDTO med3 = buildMedicao(3, AC);

        IndicadoresAcaoListaMedicoesBuilder.of(List.of(med1, med2, med3)).build();

        assertFalse(med1.isPermiteCancelarEnvioParaComplementacao());
        assertFalse(med2.isPermiteCancelarEnvioParaComplementacao());
    }

    @Test
    void testIniciarAteste() {

        MedicaoDTO med1 = buildMedicao(1, EC);
        MedicaoDTO med2 = buildMedicao(2, EC);
        MedicaoDTO med3 = buildMedicao(2, EM);

        IndicadoresAcaoListaMedicoesBuilder.of(List.of(med1, med2, med3)).build();

        assertTrue(med1.isPermiteIniciarAteste());
        assertTrue(med2.isPermiteIniciarAteste());
    }

    @Test
    void testIniciarAtesteComplementacaoAnalise() {

        MedicaoDTO med1 = buildMedicaoComComplementacaoAnaliseAcumulada(1, EC);
        MedicaoDTO med2 = buildMedicaoComComplementacaoAnalise(2, EC);

        IndicadoresAcaoListaMedicoesBuilder.of(List.of(med1, med2)).build();

        assertFalse(med1.isPermiteIniciarAteste());
        assertTrue(med2.isPermiteIniciarAteste());
    }

    @Test
    void testIniciarAtesteComComplementacaoAnaliseAnteriorNaoAtestada() {

        MedicaoDTO med1 = buildMedicaoComComplementacaoAnalise(1, EC);
        MedicaoDTO med2 = buildMedicao(2, EC);

        IndicadoresAcaoListaMedicoesBuilder.of(List.of(med1, med2)).build();

        assertTrue(med1.isPermiteIniciarAteste());
        assertFalse(med2.isPermiteIniciarAteste());
    }

    @Test
    void testIniciarAtesteComComplementacaoAnaliseAnteriorAtestada() {

        MedicaoDTO med1 = buildMedicaoComComplementacaoAnalise(1, ATD);
        MedicaoDTO med2 = buildMedicaoAcumulada(2, EC);
        MedicaoDTO med3 = buildMedicao(2, EC);

        IndicadoresAcaoListaMedicoesBuilder.of(List.of(med1, med2, med3)).build();

        assertFalse(med2.isPermiteIniciarAteste());
        assertTrue(med3.isPermiteIniciarAteste());
    }

    @Test
    void testExcluirMedicao() {

        MedicaoDTO med1 = buildMedicao(1, AC);
        MedicaoDTO med2 = buildMedicao(2, AT);

        IndicadoresAcaoListaMedicoesBuilder.of(List.of(med1, med2)).build();

        assertFalse(med1.isPermiteExcluir());
        assertTrue(med2.isPermiteExcluir());
    }

    private MedicaoDTO buildMedicao(int sequencial, SituacaoMedicaoEnum situacao) {
        MedicaoDTO medicao = new MedicaoDTO();
        medicao.setId(RandomUtils.nextLong());
        medicao.setSequencial((short) sequencial);
        medicao.setSituacao(situacao);
        return medicao;
    }

    private MedicaoDTO buildMedicaoComComplementacaoAnalise(int sequencial, SituacaoMedicaoEnum situacao) {
        MedicaoDTO medicao = buildMedicao(sequencial, situacao);
        medicao.setPermiteComplementacaoValor(false);
        return medicao;
    }

    private MedicaoDTO buildMedicaoAcumulada(int sequencial, SituacaoMedicaoEnum situacao) {
        MedicaoDTO medicao = buildMedicao(sequencial, situacao);
        medicao.setIdMedicaoAgrupadora(RandomUtils.nextLong());
        return medicao;
    }

    private MedicaoDTO buildMedicaoComComplementacaoAnaliseAcumulada(int sequencial, SituacaoMedicaoEnum situacao) {
        MedicaoDTO medicao = buildMedicao(sequencial, situacao);
        medicao.setPermiteComplementacaoValor(false);
        medicao.setIdMedicaoAgrupadora(RandomUtils.nextLong());
        return medicao;
    }
}
