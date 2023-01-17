package br.gov.planejamento.siconv.med.medicao.business.builder;

import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.CONCEDENTE;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.EMPRESA;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.MANDATARIA;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.PROPONENTE_CONVENENTE;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.ACT;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.AT;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.ATD;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.CE;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.EC;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.EM;
import static br.gov.planejamento.siconv.med.test.builder.ContextBuilder.buildContextComMedicao;
import static br.gov.planejamento.siconv.med.test.builder.ContextBuilder.newContextBuilder;
import static java.math.BigDecimal.valueOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;

import br.gov.planejamento.siconv.med.infra.security.domain.Permission;
import br.gov.planejamento.siconv.med.infra.security.domain.Profile;
import br.gov.planejamento.siconv.med.infra.security.domain.Role;
import br.gov.planejamento.siconv.med.medicao.business.builder.SubmetaMedicaoBuilder.Context;
import br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum;
import br.gov.planejamento.siconv.med.medicao.entity.dto.MedicaoDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.ServicoVrplDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.SubmetaMedicaoDTO;
import br.gov.planejamento.siconv.med.test.builder.ServicoVrplDTOBuilder;
import br.gov.planejamento.siconv.med.test.builder.SubmetaMedicaoDTOBuilder;
import br.gov.planejamento.siconv.med.test.extension.BusinessControllerBaseTest;
import br.gov.planejamento.siconv.med.test.extension.MockUsuario;

class FiltroValoresServicosSubmetaStepTest extends BusinessControllerBaseTest {

    @InjectMocks
    private FiltroValoresServicosSubmetaStep step;

    @ParameterizedTest
    @MethodSource("br.gov.planejamento.siconv.med.test.util.TestArguments#listaParametrosTodasSituacoes")
    @MockUsuario(profile = EMPRESA, permissions = Permission.VISUALIZAR_MEDICAO)
    void testQtdEmpresa_visivel_profileEmpresa(SituacaoMedicaoEnum situacao) {

        ServicoVrplDTO servico = buildServicoComPreenchimentoEmpresa(1L, valueOf(10));

        step.process(buildSubmetaComServico(servico), buildContextComMedicao(1L, situacao));

        assertThat(servico.obterValorMedicao(1L).getQtdEmpresa(), comparesEqualTo(valueOf(10)));
    }

    @ParameterizedTest
    @MethodSource("br.gov.planejamento.siconv.med.test.util.TestArguments#listaParametrosOutroProfileComSituacaoNaoPermiteManutencaoEmpresa")
    void testQtdEmpresa_visivel_outroProfile(Profile profile, SituacaoMedicaoEnum situacao) {

        mockUsuario(profile);

        ServicoVrplDTO servico = buildServicoComPreenchimentoEmpresa(1L, valueOf(10));

        step.process(buildSubmetaComServico(servico), buildContextComMedicao(1L, situacao));

        assertThat(servico.obterValorMedicao(1L).getQtdEmpresa(), comparesEqualTo(valueOf(10)));
    }

    @ParameterizedTest
    @ValueSource(strings = { "concedente", "proponente", "mandataria", "guest", "usuario_siconv" })
    void testQtdEmpresa_visivel_outroProfile_complementacao_sem_alteracao_valor(String profile) {

        mockUsuario(Profile.fromKey(profile));

        MedicaoDTO medicao = new MedicaoDTO();
        medicao.setId(1L);
        medicao.setSituacao(CE);
        medicao.setPermiteComplementacaoValor(false);

        ServicoVrplDTO servico = buildServicoComPreenchimentoEmpresa(1L, valueOf(10));

        step.process(buildSubmetaComServico(servico), buildContextComMedicao(medicao));

        assertThat(servico.obterValorMedicao(1L).getQtdEmpresa(), comparesEqualTo(valueOf(10)));
    }

    @ParameterizedTest
    @MethodSource("br.gov.planejamento.siconv.med.test.util.TestArguments#listaParametrosOutroProfileComSituacaoPermiteManutencaoEmpresa")
    void testQtdEmpresa_oculto_outroProfile(Profile profile, SituacaoMedicaoEnum situacao) {

        mockUsuario(profile);

        ServicoVrplDTO servico = buildServicoComPreenchimentoEmpresa(1L, valueOf(10));

        step.process(buildSubmetaComServico(servico), buildContextComMedicao(1L, situacao));

        assertNull(servico.obterValorMedicao(1L).getQtdEmpresa());
    }

    @ParameterizedTest
    @ValueSource(strings = { "concedente", "proponente", "mandataria", "guest", "usuario_siconv" })
    void testQtdEmpresa_oculto_outroProfile_complementacao_com_alteracao_valor(String profile) {

        mockUsuario(Profile.fromKey(profile));

        MedicaoDTO medicao = new MedicaoDTO();
        medicao.setId(1L);
        medicao.setSituacao(CE);
        medicao.setPermiteComplementacaoValor(true);

        ServicoVrplDTO servico = buildServicoComPreenchimentoEmpresa(1L, valueOf(10));

        step.process(buildSubmetaComServico(servico), buildContextComMedicao(medicao));

        assertNull(servico.obterValorMedicao(1L).getQtdEmpresa());
    }

    @ParameterizedTest
    @MethodSource("br.gov.planejamento.siconv.med.test.util.TestArguments#listaParametrosTodasSituacoes")
    @MockUsuario(profile = PROPONENTE_CONVENENTE, roles = Role.FISCAL_CONVENENTE)
    void testQtdConvenente_visivel_profileConvenente(SituacaoMedicaoEnum situacao) {

        ServicoVrplDTO servico = buildServicoComPreenchimentoConvenente(1L, valueOf(10));

        step.process(buildSubmetaComServico(servico), buildContextComMedicao(1L, situacao));

        assertThat(servico.obterValorMedicao(1L).getQtdConvenente(), comparesEqualTo(valueOf(10)));
    }

    @ParameterizedTest
    @MethodSource("br.gov.planejamento.siconv.med.test.util.TestArguments#listaParametrosOutroProfileComSituacaoNaoPermiteManutencaoConvenente")
    void testQtdConvenente_visivel_outroProfile(Profile profile, SituacaoMedicaoEnum situacao) {

        mockUsuario(profile);

        ServicoVrplDTO servico = buildServicoComPreenchimentoConvenente(1L, valueOf(10));

        step.process(buildSubmetaComServico(servico), buildContextComMedicao(1L, situacao));

        assertThat(servico.obterValorMedicao(1L).getQtdConvenente(), comparesEqualTo(valueOf(10)));
    }

    @ParameterizedTest
    @MethodSource("br.gov.planejamento.siconv.med.test.util.TestArguments#listaParametrosOutroProfileComSituacaoPermiteManutencaoConvenente")
    void testQtdConvenente_visivel_outroProfile_complementacao_sem_alteracao_valor(Profile profile,
            SituacaoMedicaoEnum situacao) {

        mockUsuario(profile);

        MedicaoDTO medicao = new MedicaoDTO();
        medicao.setId(1L);
        medicao.setSituacao(situacao);
        medicao.setPermiteComplementacaoValor(false);

        ServicoVrplDTO servico = buildServicoComPreenchimentoConvenente(1L, valueOf(10));

        step.process(buildSubmetaComServico(servico), buildContextComMedicao(medicao));

        assertThat(servico.obterValorMedicao(1L).getQtdConvenente(), comparesEqualTo(valueOf(10)));
    }

    @ParameterizedTest
    @MethodSource("br.gov.planejamento.siconv.med.test.util.TestArguments#listaParametrosOutroProfileComSituacaoPermiteManutencaoConvenente")
    void testQtdConvenente_oculto_outroProfile(Profile profile, SituacaoMedicaoEnum situacao) {

        mockUsuario(profile);

        ServicoVrplDTO servico = buildServicoComPreenchimentoConvenente(1L, valueOf(10));

        step.process(buildSubmetaComServico(servico), buildContextComMedicao(1L, situacao));

        assertNull(servico.obterValorMedicao(1L).getQtdConvenente());
    }

    @ParameterizedTest
    @MethodSource("br.gov.planejamento.siconv.med.test.util.TestArguments#listaParametrosOutroProfileComSituacaoPermiteManutencaoConvenente")
    void testQtdConvenente_oculto_outroProfile_complementacao_com_alteracao_valor(Profile profile,
            SituacaoMedicaoEnum situacao) {

        mockUsuario(profile);

        MedicaoDTO medicao = new MedicaoDTO();
        medicao.setId(1L);
        medicao.setSituacao(situacao);
        medicao.setPermiteComplementacaoValor(true);

        ServicoVrplDTO servico = buildServicoComPreenchimentoConvenente(1L, valueOf(10));

        step.process(buildSubmetaComServico(servico), buildContextComMedicao(medicao));

        assertNull(servico.obterValorMedicao(1L).getQtdConvenente());
    }

    @ParameterizedTest
    @MethodSource("br.gov.planejamento.siconv.med.test.util.TestArguments#listaParametrosTodasSituacoes")
    @MockUsuario(profile = CONCEDENTE, roles = Role.FISCAL_CONCEDENTE)
    void testQtdConcedente_visivel_profileConcedente(SituacaoMedicaoEnum situacao) {

        ServicoVrplDTO servico = buildServicoComPreenchimentoConcedente(1L, valueOf(10));

        step.process(buildSubmetaComServico(servico), buildContextComMedicao(1L, situacao));

        assertThat(servico.obterValorMedicao(1L).getQtdConcedente(), comparesEqualTo(valueOf(10)));
    }

    @ParameterizedTest
    @MethodSource("br.gov.planejamento.siconv.med.test.util.TestArguments#listaParametrosTodasSituacoes")
    @MockUsuario(profile = MANDATARIA, roles = Role.AGENTE_ACOMPANHAMENTO_INSTITUICAO_MANDATARIA)
    void testQtdConcedente_visivel_profileMandataria(SituacaoMedicaoEnum situacao) {

        ServicoVrplDTO servico = buildServicoComPreenchimentoConcedente(1L, valueOf(10));

        step.process(buildSubmetaComServico(servico), buildContextComMedicao(1L, situacao));

        assertThat(servico.obterValorMedicao(1L).getQtdConcedente(), comparesEqualTo(valueOf(10)));
    }

    @ParameterizedTest
    @MethodSource("br.gov.planejamento.siconv.med.test.util.TestArguments#listaParametrosOutroProfileComSituacaoPermitePublicacaoConcedente")
    void testQtdConcedente_visivel_outroProfile(Profile profile, SituacaoMedicaoEnum situacao) {

        mockUsuario(profile);

        ServicoVrplDTO servico = buildServicoComPreenchimentoConcedente(1L, valueOf(10));

        step.process(buildSubmetaComServico(servico), buildContextComMedicao(1L, situacao));

        assertThat(servico.obterValorMedicao(1L).getQtdConcedente(), comparesEqualTo(valueOf(10)));
    }

    @ParameterizedTest
    @MethodSource("br.gov.planejamento.siconv.med.test.util.TestArguments#listaParametrosOutroProfileComSituacaoNaoPermitePublicacaoConcedente")
    void testQtdConcedente_oculto_outroProfile(Profile profile, SituacaoMedicaoEnum situacao) {

        mockUsuario(profile);

        ServicoVrplDTO servico = buildServicoComPreenchimentoConcedente(1L, valueOf(10));

        step.process(buildSubmetaComServico(servico), buildContextComMedicao(1L, situacao));

        assertNull(servico.obterValorMedicao(1L).getQtdConcedente());
    }

    @Test
    @MockUsuario(profile = EMPRESA, permissions = Permission.VISUALIZAR_MEDICAO)
    void testServicoComMedicaoPosterior() {

        Long idMedicaoAtual = 1L;
        Long idMedicaoPosterior = 2L;

        MedicaoDTO medicao = new MedicaoDTO();
        medicao.setId(idMedicaoAtual);

        Map<Long, SituacaoMedicaoEnum> cacheSituacaoMedicao = new HashMap<Long, SituacaoMedicaoEnum>();
        cacheSituacaoMedicao.put(idMedicaoAtual, ATD);
        cacheSituacaoMedicao.put(idMedicaoPosterior, EC);

        Context context = newContextBuilder().setContext(null, medicao, cacheSituacaoMedicao, null).create();

        ServicoVrplDTO servico = getServicoBuilder()
                .comPreenchimentoMedicao(idMedicaoAtual, valueOf(10), null, null)
                .comPreenchimentoMedicao(idMedicaoPosterior, valueOf(15), null, null)
                .build();

        step.process(buildSubmetaComServico(servico), context);

        assertTrue(servico.getValoresPorIdMedicao().containsKey(idMedicaoAtual));
        assertTrue(servico.getValoresPorIdMedicao().containsKey(idMedicaoPosterior));
    }

    @Test
    @MockUsuario(profile = EMPRESA, permissions = Permission.VISUALIZAR_MEDICAO)
    void testServicoComMedicaoPosteriorEmElaboracao() {

        Long idMedicaoAnterior = 1L;
        Long idMedicaoAtual = 2L;
        Long idMedicaoPosterior = 3L;

        MedicaoDTO medicao = new MedicaoDTO();
        medicao.setId(idMedicaoAtual);

        Map<Long, SituacaoMedicaoEnum> cacheSituacaoMedicao = new HashMap<Long, SituacaoMedicaoEnum>();
        cacheSituacaoMedicao.put(idMedicaoAnterior, ACT);
        cacheSituacaoMedicao.put(idMedicaoAtual, CE);
        cacheSituacaoMedicao.put(idMedicaoPosterior, EM);

        Context context = newContextBuilder().setContext(null, medicao, cacheSituacaoMedicao, null).create();

        ServicoVrplDTO servico = getServicoBuilder()
                .comPreenchimentoMedicao(idMedicaoAnterior, valueOf(10), null, null)
                .comPreenchimentoMedicao(idMedicaoAtual, valueOf(10), null, null)
                .comPreenchimentoMedicao(idMedicaoPosterior, valueOf(15), null, null)
                .build();

        step.process(buildSubmetaComServico(servico), context);

        assertTrue(servico.getValoresPorIdMedicao().containsKey(idMedicaoAnterior));
        assertTrue(servico.getValoresPorIdMedicao().containsKey(idMedicaoAtual));
        assertFalse(servico.getValoresPorIdMedicao().containsKey(idMedicaoPosterior));
    }
    
    @Test
    @MockUsuario(profile = EMPRESA, permissions = Permission.VISUALIZAR_MEDICAO)
    void testServicoComMedicaoPosteriorEmAteste() {

        Long idMedicaoAnterior = 1L;
        Long idMedicaoAtual = 2L;
        Long idMedicaoPosterior = 3L;

        MedicaoDTO medicao = new MedicaoDTO();
        medicao.setId(idMedicaoAtual);

        Map<Long, SituacaoMedicaoEnum> cacheSituacaoMedicao = new HashMap<Long, SituacaoMedicaoEnum>();
        cacheSituacaoMedicao.put(idMedicaoAnterior, ACT);
        cacheSituacaoMedicao.put(idMedicaoAtual, CE);
        cacheSituacaoMedicao.put(idMedicaoPosterior, AT);

        Context context = newContextBuilder().setContext(null, medicao, cacheSituacaoMedicao, null).create();

        ServicoVrplDTO servico = getServicoBuilder()
                .comPreenchimentoMedicao(idMedicaoAnterior, valueOf(10), null, null)
                .comPreenchimentoMedicao(idMedicaoAtual, valueOf(10), null, null)
                .comPreenchimentoMedicao(idMedicaoPosterior, valueOf(15), null, null)
                .build();

        step.process(buildSubmetaComServico(servico), context);

        assertTrue(servico.getValoresPorIdMedicao().containsKey(idMedicaoAnterior));
        assertTrue(servico.getValoresPorIdMedicao().containsKey(idMedicaoAtual));
        assertFalse(servico.getValoresPorIdMedicao().containsKey(idMedicaoPosterior));
    }

    // =========================== Métodos utilitários ===========================

    private ServicoVrplDTO buildServicoComPreenchimentoEmpresa(Long idMedicao, BigDecimal qtdEmpresa) {
        return getServicoBuilder().comPreenchimentoMedicao(idMedicao, qtdEmpresa, null, null).build();
    }

    private ServicoVrplDTO buildServicoComPreenchimentoConvenente(Long idMedicao, BigDecimal qtdConvenente) {
        return getServicoBuilder().comPreenchimentoMedicao(idMedicao, null, qtdConvenente, null).build();
    }

    private ServicoVrplDTO buildServicoComPreenchimentoConcedente(Long idMedicao, BigDecimal qtdConcedente) {
        return getServicoBuilder().comPreenchimentoMedicao(idMedicao, null, null, qtdConcedente).build();
    }

    private SubmetaMedicaoDTO buildSubmetaComServico(ServicoVrplDTO servico) {
        return new SubmetaMedicaoDTOBuilder(1L, 1L, 1L).servicos(List.of(servico)).create();
    }

    private ServicoVrplDTOBuilder getServicoBuilder() {
        return new ServicoVrplDTOBuilder();
    }
}
