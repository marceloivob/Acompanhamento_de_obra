package br.gov.planejamento.siconv.med.medicao.business.builder;

import static br.gov.planejamento.siconv.med.test.builder.ContextBuilder.newContextBuilder;
import static java.math.BigDecimal.valueOf;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;

import br.gov.planejamento.siconv.med.medicao.business.builder.SubmetaMedicaoBuilder.Context;
import br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum;
import br.gov.planejamento.siconv.med.medicao.entity.SituacaoSubmetaEnum;
import br.gov.planejamento.siconv.med.medicao.entity.database.SubmetaMedicaoBD;
import br.gov.planejamento.siconv.med.medicao.entity.dto.MedicaoDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.SubmetaMedicaoDTO;
import br.gov.planejamento.siconv.med.test.builder.SubmetaMedicaoDTOBuilder;
import br.gov.planejamento.siconv.med.test.extension.BusinessControllerBaseTest;

class IndicadoresSubmetaServicoStepTest extends BusinessControllerBaseTest {

    @InjectMocks
    private IndicadoresSubmetaServicoStep step;

    // ========================= Métodos utilitários =========================

    private SubmetaMedicaoDTOBuilder getSubmetaBuilder() {
        return new SubmetaMedicaoDTOBuilder(321L, 99881L, 66612L);
    }

    private Context buildContextComMedicao(MedicaoDTO medicao) {
        return newContextBuilder().setContext(null, medicao, new HashMap<>(), new ArrayList<SubmetaMedicaoBD>())
                .create();
    }

    // =================== Testes do indicador permiteMarcacaoEmpresa ===================

    @ParameterizedTest
    @ValueSource(strings = { "EC", "EXC", "AT", "ATD", "ECE", "AC", "ACT", "ECC", "CC" })
    void testPermiteMarcacaoEmpresa_situacaoMedicaoInvalida(String codigoSituacao) {

        MedicaoDTO medicao = new MedicaoDTO();
        medicao.setSituacao(SituacaoMedicaoEnum.fromCodigo(codigoSituacao));

        SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilder()
                                               .servicoQtdPlanejado(valueOf(20))
                                               .create();

        step.process(submetaMedicao, buildContextComMedicao(medicao));

        assertFalse(submetaMedicao.isPermiteMarcacaoEmpresa());
    }

    @Test
    void testPermiteMarcacaoEmpresa_medicaoBloqueada() {

        MedicaoDTO medicao = new MedicaoDTO();
        medicao.setSituacao(SituacaoMedicaoEnum.EM);
        medicao.setBloqueada(true);

        SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilder()
                                               .servicoQtdPlanejado(valueOf(20))
                                               .create();

        step.process(submetaMedicao, buildContextComMedicao(medicao));

        assertFalse(submetaMedicao.isPermiteMarcacaoEmpresa());
    }

    @Test
    void testPermiteMarcacaoEmpresa_servicoPendentePreenchimento() {

        MedicaoDTO medicao = new MedicaoDTO();
        medicao.setId(2L);
        medicao.setSituacao(SituacaoMedicaoEnum.EM);

        SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilder()
                                                .servicoQtdPlanejado(valueOf(20))
                                                .create();

        step.process(submetaMedicao, buildContextComMedicao(medicao));

        assertTrue(submetaMedicao.isPermiteMarcacaoEmpresa());
    }

    @Test
    void testPermiteMarcacaoEmpresa_servicoConcluidoMedicaoAtual() {

        MedicaoDTO medicao = new MedicaoDTO();
        medicao.setId(2L);
        medicao.setSituacao(SituacaoMedicaoEnum.EM);

        SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilder()
                                               .servicoQtdPlanejado(valueOf(20))
                                               .servicoQtdRealizadoEmpresa(valueOf(10))
                                               .servicoQtdAcumuladoEmpresa(valueOf(20))
                                               .create();

        step.process(submetaMedicao, buildContextComMedicao(medicao));

        assertTrue(submetaMedicao.isPermiteMarcacaoEmpresa());
    }

    @Test
    void testPermiteMarcacaoEmpresa_servicoConcluidoMedicaoAnterior() {

        MedicaoDTO medicao = new MedicaoDTO();
        medicao.setId(2L);
        medicao.setSituacao(SituacaoMedicaoEnum.EM);

        SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilder()
                                               .servicoQtdPlanejado(valueOf(20))
                                               .servicoQtdAcumuladoEmpresa(valueOf(20))
                                               .create();

        step.process(submetaMedicao, buildContextComMedicao(medicao));

        assertFalse(submetaMedicao.isPermiteMarcacaoEmpresa());
    }

    @Test
    void testPermiteMarcacaoEmpresa_medicaoAcumuladaSemPreenchimentoOriginal() {

        MedicaoDTO medicao = new MedicaoDTO();
        medicao.setId(1L);
        medicao.setSituacao(SituacaoMedicaoEnum.CE);
        medicao.setIdMedicaoAgrupadora(2L);

        SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilder()
                                               .servicoQtdPlanejado(valueOf(20))
                                               .create();

        step.process(submetaMedicao, buildContextComMedicao(medicao));

        assertFalse(submetaMedicao.isPermiteMarcacaoEmpresa());
    }

    @Test
    void testPermiteMarcacaoEmpresa_medicaoAcumuladaComPreenchimentoOriginal() {

        MedicaoDTO medicao = new MedicaoDTO();
        medicao.setId(1L);
        medicao.setSituacao(SituacaoMedicaoEnum.CE);
        medicao.setIdMedicaoAgrupadora(2L);

        SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilder()
                                               .servicoQtdPlanejado(valueOf(20))
                                               .servicoQtdRealizadoEmpresa(valueOf(10))
                                               .servicoQtdAcumuladoEmpresa(valueOf(10))
                                               .create();

        step.process(submetaMedicao, buildContextComMedicao(medicao));

        assertTrue(submetaMedicao.isPermiteMarcacaoEmpresa());
    }
    
    @Test
    void testPermiteMarcacaoEmpresa_permiteComplementacaoValorFalse() {

        MedicaoDTO medicao = new MedicaoDTO();
        medicao.setSituacao(SituacaoMedicaoEnum.CE);
        medicao.setBloqueada(false);
        medicao.setPermiteComplementacaoValor(false);

        SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilder()
                                               .servicoQtdPlanejado(valueOf(20))
                                               .create();

        step.process(submetaMedicao, buildContextComMedicao(medicao));

        assertFalse(submetaMedicao.isPermiteMarcacaoEmpresa());
    }
    
    @Test
    void testPermiteMarcacaoEmpresa_permiteComplementacaoValorTrue() {

        MedicaoDTO medicao = new MedicaoDTO();
        medicao.setSituacao(SituacaoMedicaoEnum.CE);
        medicao.setBloqueada(false);
        medicao.setPermiteComplementacaoValor(true);

        SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilder()
                                               .servicoQtdPlanejado(valueOf(20))
                                               .create();

        step.process(submetaMedicao, buildContextComMedicao(medicao));

        assertTrue(submetaMedicao.isPermiteMarcacaoEmpresa());
    }

    // =================== Testes do indicador permiteMarcacaoConvenente =================

    @ParameterizedTest
    @ValueSource(strings = { "EM", "EC", "EXC", "ATD", "ECE", "CE", "AC", "ACT", "ECC" })
    void testPermiteMarcacaoConvenente_situacaoMedicaoInvalida(String codigoSituacao) {

        MedicaoDTO medicao = new MedicaoDTO();
        medicao.setSituacao(SituacaoMedicaoEnum.fromCodigo(codigoSituacao));

        SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilder()
                                               .servicoQtdAcumuladoEmpresa(valueOf(10))
                                               .create();

        step.process(submetaMedicao, buildContextComMedicao(medicao));

        assertFalse(submetaMedicao.isPermiteMarcacaoConvenente());
    }

    @Test
    void testPermiteMarcacaoConvenente_medicaoBloqueada() {

        MedicaoDTO medicao = new MedicaoDTO();
        medicao.setSituacao(SituacaoMedicaoEnum.AT);
        medicao.setBloqueada(true);

        SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilder()
                                               .servicoQtdAcumuladoEmpresa(valueOf(10))
                                               .create();

        step.process(submetaMedicao, buildContextComMedicao(medicao));

        assertFalse(submetaMedicao.isPermiteMarcacaoConvenente());
    }

    @Test
    void testPermiteMarcacaoConvenente_medicaoAcumulada() {

        MedicaoDTO medicao = new MedicaoDTO();
        medicao.setId(1L);
        medicao.setSituacao(SituacaoMedicaoEnum.AT);
        medicao.setIdMedicaoAgrupadora(2L);

        SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilder()
                                               .servicoQtdAcumuladoEmpresa(valueOf(10))
                                               .create();

        step.process(submetaMedicao, buildContextComMedicao(medicao));

        assertFalse(submetaMedicao.isPermiteMarcacaoConvenente());
    }

    @Test
    void testPermiteMarcacaoConvenente_submetaSemValorAssinadaEmpresa() {

        MedicaoDTO medicao = new MedicaoDTO();
        medicao.setId(1L);
        medicao.setSituacao(SituacaoMedicaoEnum.AT);

        SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilder()
                                               .setSituacaoSubmetaEmpresa(SituacaoSubmetaEnum.ASS)
                                               .create();

        step.process(submetaMedicao, buildContextComMedicao(medicao));

        assertTrue(submetaMedicao.isPermiteMarcacaoConvenente());
    }

    @Test
    void testPermiteMarcacaoConvenente_servicoPendentePreenchimento() {

        MedicaoDTO medicao = new MedicaoDTO();
        medicao.setId(2L);
        medicao.setSituacao(SituacaoMedicaoEnum.AT);

        SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilder()
                                               .servicoQtdAcumuladoEmpresa(valueOf(10))
                                               .create();

        step.process(submetaMedicao, buildContextComMedicao(medicao));

        assertTrue(submetaMedicao.isPermiteMarcacaoConvenente());
    }

    @Test
    void testPermiteMarcacaoConvenente_servicoConcluidoMedicaoAtual() {

        MedicaoDTO medicao = new MedicaoDTO();
        medicao.setId(2L);
        medicao.setSituacao(SituacaoMedicaoEnum.AT);

        SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilder()
                                               .servicoQtdAcumuladoEmpresa(valueOf(20))
                                               .servicoQtdRealizadoConvenente(valueOf(20))
                                               .servicoQtdAcumuladoConvenente(valueOf(20))
                                               .create();

        step.process(submetaMedicao, buildContextComMedicao(medicao));

        assertTrue(submetaMedicao.isPermiteMarcacaoConvenente());
    }

    @Test
    void testPermiteMarcacaoConvenente_servicoConcluidoMedicaoAnterior() {

        MedicaoDTO medicao = new MedicaoDTO();
        medicao.setId(2L);
        medicao.setSituacao(SituacaoMedicaoEnum.AT);

        SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilder()
                                               .servicoQtdAcumuladoEmpresa(valueOf(20))
                                               .servicoQtdAcumuladoConvenente(valueOf(20))
                                               .create();

        step.process(submetaMedicao, buildContextComMedicao(medicao));

        assertFalse(submetaMedicao.isPermiteMarcacaoConvenente());
    }

    @Test
    void testPermiteMarcacaoConvenente_AT_permiteComplementacaoValorTrue() {

        MedicaoDTO medicao = new MedicaoDTO();
        medicao.setSituacao(SituacaoMedicaoEnum.AT);
        medicao.setBloqueada(false);
        medicao.setPermiteComplementacaoValor(true);

        SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilder()
                                                .setSituacaoSubmetaEmpresa(SituacaoSubmetaEnum.ASS)
                                                .create();

        step.process(submetaMedicao, buildContextComMedicao(medicao));

        assertTrue(submetaMedicao.isPermiteMarcacaoConvenente());
    }

    @Test
    void testPermiteMarcacaoConvenente__CC_permiteComplementacaoValorTrue() {

        MedicaoDTO medicao = new MedicaoDTO();
        medicao.setSituacao(SituacaoMedicaoEnum.CC);
        medicao.setBloqueada(false);
        medicao.setPermiteComplementacaoValor(true);

        SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilder()
                                                .setSituacaoSubmetaEmpresa(SituacaoSubmetaEnum.ASS)
                                                .create();

        step.process(submetaMedicao, buildContextComMedicao(medicao));

        assertTrue(submetaMedicao.isPermiteMarcacaoConvenente());
    }

    @Test
    void testPermiteMarcacaoConvenente_AT_permiteComplementacaoValorFalse() {

        MedicaoDTO medicao = new MedicaoDTO();
        medicao.setSituacao(SituacaoMedicaoEnum.AT);
        medicao.setBloqueada(false);
        medicao.setPermiteComplementacaoValor(false);

        SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilder()
                                                .setSituacaoSubmetaEmpresa(SituacaoSubmetaEnum.ASS)
                                                .create();

        step.process(submetaMedicao, buildContextComMedicao(medicao));

        assertFalse(submetaMedicao.isPermiteMarcacaoConvenente());
    }

    @Test
    void testPermiteMarcacaoConvenente__CC_permiteComplementacaoValorFalse() {

        MedicaoDTO medicao = new MedicaoDTO();
        medicao.setSituacao(SituacaoMedicaoEnum.CC);
        medicao.setBloqueada(false);
        medicao.setPermiteComplementacaoValor(false);

        SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilder()
                                                .setSituacaoSubmetaEmpresa(SituacaoSubmetaEnum.ASS)
                                                .create();

        step.process(submetaMedicao, buildContextComMedicao(medicao));

        assertFalse(submetaMedicao.isPermiteMarcacaoConvenente());
    }

    @Test
    void testPermiteMarcacaoConvenente_CC_medicaoAcumulada_servicoComValorRealizadoConv() {

        MedicaoDTO medicao = new MedicaoDTO();
        medicao.setId(1L);
        medicao.setSituacao(SituacaoMedicaoEnum.CC);
        medicao.setIdMedicaoAgrupadora(2L);

        SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilder()
                                               .servicoQtdAcumuladoEmpresa(valueOf(10))
                                               .servicoQtdRealizadoConvenente(valueOf(10))
                                               .servicoQtdAcumuladoConvenente(valueOf(10))
                                               .create();

        step.process(submetaMedicao, buildContextComMedicao(medicao));

        assertTrue(submetaMedicao.isPermiteMarcacaoConvenente());
    }

    @Test
    void testPermiteMarcacaoConvenente_CC_medicaoAcumulada_servicoSemValorRealizadoConv() {

        MedicaoDTO medicao = new MedicaoDTO();
        medicao.setId(2L);
        medicao.setSituacao(SituacaoMedicaoEnum.CC);
        medicao.setIdMedicaoAgrupadora(3L);

        SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilder()
                                               .servicoQtdAcumuladoEmpresa(valueOf(10))
                                               .servicoQtdAcumuladoConvenente(valueOf(10))
                                               .create();

        step.process(submetaMedicao, buildContextComMedicao(medicao));

        assertFalse(submetaMedicao.isPermiteMarcacaoConvenente());
    }

    @Test
    void testPermiteMarcacaoConvenente_submetaRequerAssinaturaConvenente() {

        MedicaoDTO medicaoAgrupadora = new MedicaoDTO();
        medicaoAgrupadora.setId(3L);
        medicaoAgrupadora.setSituacao(SituacaoMedicaoEnum.AT);

        SubmetaMedicaoDTO submetaMedAgrupadora = getSubmetaBuilder().create();

        SubmetaMedicaoBD submetaMedAcumulada = new SubmetaMedicaoBD();
        submetaMedAcumulada.setIdSubmetaVrpl(submetaMedAgrupadora.getId());
        submetaMedAcumulada.setSituacaoEmpresa(SituacaoSubmetaEnum.ASS);
        submetaMedAcumulada.setIdMedicao(2L);

        SubmetaMedicaoBD outraSubmetaMedAcumulada = new SubmetaMedicaoBD();
        outraSubmetaMedAcumulada.setIdSubmetaVrpl(999L);
        outraSubmetaMedAcumulada.setSituacaoEmpresa(SituacaoSubmetaEnum.ASS);
        outraSubmetaMedAcumulada.setIdMedicao(2L);

        SubmetaMedicaoBD submetaMedAnterior = new SubmetaMedicaoBD();
        submetaMedAnterior.setIdSubmetaVrpl(submetaMedAgrupadora.getId());
        submetaMedAnterior.setSituacaoEmpresa(SituacaoSubmetaEnum.ASS);
        submetaMedAnterior.setIdMedicao(1L);

        SubmetaMedicaoBD submetaMedPosterior = new SubmetaMedicaoBD();
        submetaMedPosterior.setIdSubmetaVrpl(submetaMedAgrupadora.getId());
        submetaMedPosterior.setSituacaoEmpresa(SituacaoSubmetaEnum.RAS);
        submetaMedPosterior.setIdMedicao(4L);

        Context context = newContextBuilder()
                                .withMedicao(medicaoAgrupadora)
                                .withCacheSituacaoMedicao(Map.of(1L, SituacaoMedicaoEnum.ATD,
                                                                 2L, SituacaoMedicaoEnum.AT,
                                                                 3L, SituacaoMedicaoEnum.AT,
                                                                 4L, SituacaoMedicaoEnum.EM))
                                .withCacheIdMedicoesAcumuladas(List.of(2L))
                                .withCacheSubmetaMedicaoBD(List.of(submetaMedAnterior,
                                                                   submetaMedPosterior,
                                                                   outraSubmetaMedAcumulada,
                                                                   submetaMedAcumulada))
                                .create();

        step.process(submetaMedAgrupadora, context);

        assertTrue(submetaMedAgrupadora.isPermiteMarcacaoConvenente());
    }

    // ================= Testes do indicador permiteMarcacaoConcedente =================

    @ParameterizedTest
    @ValueSource(strings = { "EM", "EC", "EXC", "AT", "ATD", "ECE", "ACT", "ECC", "CC" })
    void testPermiteMarcacaoConcedente_situacaoMedicaoInvalida(String codigoSituacao) {

        MedicaoDTO medicao = new MedicaoDTO();
        medicao.setSituacao(SituacaoMedicaoEnum.fromCodigo(codigoSituacao));

        SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilder()
                                               .servicoQtdAcumuladoConvenente(valueOf(10))
                                               .create();

        step.process(submetaMedicao, buildContextComMedicao(medicao));

        assertFalse(submetaMedicao.isPermiteMarcacaoConcedente());
    }

    @Test
    void testPermiteMarcacaoConcedente_medicaoBloqueada() {

        MedicaoDTO medicao = new MedicaoDTO();
        medicao.setSituacao(SituacaoMedicaoEnum.AC);
        medicao.setBloqueada(true);

        SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilder()
                                               .servicoQtdAcumuladoConvenente(valueOf(10))
                                               .create();

        step.process(submetaMedicao, buildContextComMedicao(medicao));

        assertFalse(submetaMedicao.isPermiteMarcacaoConcedente());
    }

    @Test
    void testPermiteMarcacaoConcedente_medicaoAcumulada() {

        MedicaoDTO medicao = new MedicaoDTO();
        medicao.setId(1L);
        medicao.setSituacao(SituacaoMedicaoEnum.AC);
        medicao.setIdMedicaoAgrupadora(2L);

        SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilder()
                                               .servicoQtdAcumuladoConvenente(valueOf(10))
                                               .create();

        step.process(submetaMedicao, buildContextComMedicao(medicao));

        assertFalse(submetaMedicao.isPermiteMarcacaoConcedente());
    }

    @Test
    void testPermiteMarcacaoConcedente_submetaSemValorAssinadaConvenente() {

        MedicaoDTO medicao = new MedicaoDTO();
        medicao.setSituacao(SituacaoMedicaoEnum.AC);

        SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilder()
                                               .setSituacaoSubmetaConvenente(SituacaoSubmetaEnum.ASS)
                                               .create();

        step.process(submetaMedicao, buildContextComMedicao(medicao));

        assertTrue(submetaMedicao.isPermiteMarcacaoConcedente());
    }

    @Test
    void testPermiteMarcacaoConcedente_servicoPendentePreenchimento() {

        MedicaoDTO medicao = new MedicaoDTO();
        medicao.setId(2L);
        medicao.setSituacao(SituacaoMedicaoEnum.AC);

        SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilder()
                                               .servicoQtdAcumuladoConvenente(valueOf(10))
                                               .create();

        step.process(submetaMedicao, buildContextComMedicao(medicao));

        assertTrue(submetaMedicao.isPermiteMarcacaoConcedente());
    }

    @Test
    void testPermiteMarcacaoConcedente_servicoConcluidoMedicaoAtual() {

        MedicaoDTO medicao = new MedicaoDTO();
        medicao.setId(2L);
        medicao.setSituacao(SituacaoMedicaoEnum.AC);

        SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilder()
                                               .servicoQtdAcumuladoConvenente(valueOf(10))
                                               .servicoQtdRealizadoConcedente(valueOf(5))
                                               .servicoQtdAcumuladoConcedente(valueOf(10))
                                               .create();

        step.process(submetaMedicao, buildContextComMedicao(medicao));

        assertTrue(submetaMedicao.isPermiteMarcacaoConcedente());
    }

    @Test
    void testPermiteMarcacaoConcedente_servicoConcluidoMedicaoAnterior() {

        MedicaoDTO medicao = new MedicaoDTO();
        medicao.setId(2L);
        medicao.setSituacao(SituacaoMedicaoEnum.AC);

        SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilder()
                                              .servicoQtdAcumuladoConvenente(valueOf(10))
                                              .servicoQtdAcumuladoConcedente(valueOf(10))
                                              .create();

        step.process(submetaMedicao, buildContextComMedicao(medicao));

        assertFalse(submetaMedicao.isPermiteMarcacaoConcedente());
    }

    @Test
    void testPermiteMarcacaoConcedente_submetaRequerAssinaturaConcedente() {

        MedicaoDTO medicaoAgrupadora = new MedicaoDTO();
        medicaoAgrupadora.setId(3L);
        medicaoAgrupadora.setSituacao(SituacaoMedicaoEnum.AC);

        SubmetaMedicaoDTO submetaMedAgrupadora = getSubmetaBuilder().create();

        SubmetaMedicaoBD submetaMedAcumulada = new SubmetaMedicaoBD();
        submetaMedAcumulada.setIdSubmetaVrpl(submetaMedAgrupadora.getId());
        submetaMedAcumulada.setSituacaoConvenente(SituacaoSubmetaEnum.ASS);
        submetaMedAcumulada.setIdMedicao(2L);

        SubmetaMedicaoBD outraSubmetaMedAcumulada = new SubmetaMedicaoBD();
        outraSubmetaMedAcumulada.setIdSubmetaVrpl(999L);
        outraSubmetaMedAcumulada.setSituacaoConvenente(SituacaoSubmetaEnum.ASS);
        outraSubmetaMedAcumulada.setIdMedicao(2L);

        SubmetaMedicaoBD submetaMedAnterior = new SubmetaMedicaoBD();
        submetaMedAnterior.setIdSubmetaVrpl(submetaMedAgrupadora.getId());
        submetaMedAnterior.setSituacaoConvenente(SituacaoSubmetaEnum.ASS);
        submetaMedAnterior.setIdMedicao(1L);

        SubmetaMedicaoBD submetaMedPosterior = new SubmetaMedicaoBD();
        submetaMedPosterior.setIdSubmetaVrpl(submetaMedAgrupadora.getId());
        submetaMedPosterior.setSituacaoConvenente(SituacaoSubmetaEnum.RAS);
        submetaMedPosterior.setIdMedicao(4L);

        Context context = newContextBuilder()
                                .withMedicao(medicaoAgrupadora)
                                .withCacheSituacaoMedicao(Map.of(1L, SituacaoMedicaoEnum.ACT,
                                                                 2L, SituacaoMedicaoEnum.AC,
                                                                 3L, SituacaoMedicaoEnum.AC,
                                                                 4L, SituacaoMedicaoEnum.ATD))
                                .withCacheIdMedicoesAcumuladas(List.of(2L))
                                .withCacheSubmetaMedicaoBD(List.of(submetaMedAnterior,
                                                                   submetaMedPosterior,
                                                                   outraSubmetaMedAcumulada,
                                                                   submetaMedAcumulada))
                                .create();

        step.process(submetaMedAgrupadora, context);

        assertTrue(submetaMedAgrupadora.isPermiteMarcacaoConcedente());
    }
}
