package br.gov.planejamento.siconv.med.medicao.business.builder;

import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.CONCEDENTE;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.EMPRESA;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.MANDATARIA;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.PROPONENTE_CONVENENTE;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.CE;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoSubmetaEnum.ASS;
import static br.gov.planejamento.siconv.med.test.builder.ContextBuilder.buildContextComMedicao;
import static br.gov.planejamento.siconv.med.test.builder.SubmetaMedicaoDTOBuilder.newSubmetaMedicaoBuilder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;

import br.gov.planejamento.siconv.med.infra.security.domain.Permission;
import br.gov.planejamento.siconv.med.infra.security.domain.Profile;
import br.gov.planejamento.siconv.med.infra.security.domain.Role;
import br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum;
import br.gov.planejamento.siconv.med.medicao.entity.dto.MedicaoDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.SubmetaMedicaoDTO;
import br.gov.planejamento.siconv.med.test.extension.BusinessControllerBaseTest;
import br.gov.planejamento.siconv.med.test.extension.MockUsuario;

class FiltroSituacoesSubmetaStepTest extends BusinessControllerBaseTest {

    @InjectMocks
    private FiltroSituacoesSubmetaStep step;

    @ParameterizedTest
    @MethodSource("br.gov.planejamento.siconv.med.test.util.TestArguments#listaParametrosTodasSituacoes")
    @MockUsuario(profile = EMPRESA, permissions = Permission.VISUALIZAR_MEDICAO)
    void testSituacaoSubmetaEmpresa_visivel_profileEmpresa(SituacaoMedicaoEnum situacaoMedicao) {

        SubmetaMedicaoDTO submetaMedicao = buildSubmetaMedicaoAssinadaEmpresa();

        step.process(submetaMedicao, buildContextComMedicao(1L, situacaoMedicao));

        assertEquals(ASS, submetaMedicao.getSituacaoEmpresa());
    }

    @ParameterizedTest
    @MethodSource("br.gov.planejamento.siconv.med.test.util.TestArguments#listaParametrosOutroProfileComSituacaoNaoPermiteManutencaoEmpresa")
    void testSituacaoSubmetaEmpresa_visivel_outroProfile(Profile profile, SituacaoMedicaoEnum situacaoMedicao) {

        mockUsuario(profile);

        SubmetaMedicaoDTO submetaMedicao = buildSubmetaMedicaoAssinadaEmpresa();

        step.process(submetaMedicao, buildContextComMedicao(1L, situacaoMedicao));

        assertEquals(ASS, submetaMedicao.getSituacaoEmpresa());
    }

    @ParameterizedTest
    @MethodSource("br.gov.planejamento.siconv.med.test.util.TestArguments#listaParametrosTodosProfilesExcetoEmpresa")
    void testSituacaoSubmetaEmpresa_visivel_outroProfile_complementacao_sem_alteracao_valor(Profile profile) {

        mockUsuario(profile);

        MedicaoDTO medicao = new MedicaoDTO();
        medicao.setId(1L);
        medicao.setSituacao(CE);
        medicao.setPermiteComplementacaoValor(false);

        SubmetaMedicaoDTO submetaMedicao = buildSubmetaMedicaoAssinadaEmpresa();

        step.process(submetaMedicao, buildContextComMedicao(medicao));

        assertEquals(ASS, submetaMedicao.getSituacaoEmpresa());
    }

    @ParameterizedTest
    @MethodSource("br.gov.planejamento.siconv.med.test.util.TestArguments#listaParametrosOutroProfileComSituacaoPermiteManutencaoEmpresa")
    void testSituacaoSubmetaEmpresa_oculto_outroProfile(Profile profile, SituacaoMedicaoEnum situacaoMedicao) {

        mockUsuario(profile);

        SubmetaMedicaoDTO submetaMedicao = buildSubmetaMedicaoAssinadaEmpresa();

        step.process(submetaMedicao, buildContextComMedicao(1L, situacaoMedicao));

        assertNull(submetaMedicao.getSituacaoEmpresa());
    }

    @ParameterizedTest
    @MethodSource("br.gov.planejamento.siconv.med.test.util.TestArguments#listaParametrosTodosProfilesExcetoEmpresa")
    void testSituacaoSubmetaEmpresa_oculto_outroProfile_complementacao_com_alteracao_valor(Profile profile) {

        mockUsuario(profile);

        MedicaoDTO medicao = new MedicaoDTO();
        medicao.setId(1L);
        medicao.setSituacao(CE);
        medicao.setPermiteComplementacaoValor(true);

        SubmetaMedicaoDTO submetaMedicao = buildSubmetaMedicaoAssinadaEmpresa();

        step.process(submetaMedicao, buildContextComMedicao(medicao));

        assertNull(submetaMedicao.getSituacaoEmpresa());
    }

    @ParameterizedTest
    @MethodSource("br.gov.planejamento.siconv.med.test.util.TestArguments#listaParametrosTodasSituacoes")
    @MockUsuario(profile = PROPONENTE_CONVENENTE, roles = Role.FISCAL_CONVENENTE)
    void testSituacaoSubmetaConvenente_visivel_profileConvenente(SituacaoMedicaoEnum situacaoMedicao) {

        SubmetaMedicaoDTO submetaMedicao = buildSubmetaMedicaoAssinadaConvenente();

        step.process(submetaMedicao, buildContextComMedicao(1L, situacaoMedicao));

        assertEquals(ASS, submetaMedicao.getSituacaoConvenente());
    }

    @ParameterizedTest
    @MethodSource("br.gov.planejamento.siconv.med.test.util.TestArguments#listaParametrosOutroProfileComSituacaoNaoPermiteManutencaoConvenente")
    void testSituacaoSubmetaConvenente_visivel_outroProfile(Profile profile, SituacaoMedicaoEnum situacaoMedicao) {

        mockUsuario(profile);

        SubmetaMedicaoDTO submetaMedicao = buildSubmetaMedicaoAssinadaConvenente();

        step.process(submetaMedicao, buildContextComMedicao(1L, situacaoMedicao));

        assertEquals(ASS, submetaMedicao.getSituacaoConvenente());
    }

    @ParameterizedTest
    @MethodSource("br.gov.planejamento.siconv.med.test.util.TestArguments#listaParametrosOutroProfileComSituacaoPermiteManutencaoConvenente")
    void testSituacaoSubmetaConvenente_visivel_outroProfile_complementacao_sem_alteracao_valor(Profile profile,
            SituacaoMedicaoEnum situacaoMedicao) {

        mockUsuario(profile);

        MedicaoDTO medicao = new MedicaoDTO();
        medicao.setId(1L);
        medicao.setSituacao(situacaoMedicao);
        medicao.setPermiteComplementacaoValor(false);

        SubmetaMedicaoDTO submetaMedicao = buildSubmetaMedicaoAssinadaConvenente();

        step.process(submetaMedicao, buildContextComMedicao(medicao));

        assertEquals(ASS, submetaMedicao.getSituacaoConvenente());
    }

    @ParameterizedTest
    @MethodSource("br.gov.planejamento.siconv.med.test.util.TestArguments#listaParametrosOutroProfileComSituacaoPermiteManutencaoConvenente")
    void testSituacaoSubmetaConvenente_oculto_outroProfile(Profile profile, SituacaoMedicaoEnum situacaoMedicao) {

        mockUsuario(profile);

        SubmetaMedicaoDTO submetaMedicao = buildSubmetaMedicaoAssinadaConvenente();

        step.process(submetaMedicao, buildContextComMedicao(1L, situacaoMedicao));

        assertNull(submetaMedicao.getSituacaoConvenente());
    }

    @ParameterizedTest
    @MethodSource("br.gov.planejamento.siconv.med.test.util.TestArguments#listaParametrosOutroProfileComSituacaoPermiteManutencaoConvenente")
    void testSituacaoSubmetaConvenente_oculto_outroProfile_complementacao_com_alteracao_valor(Profile profile,
            SituacaoMedicaoEnum situacaoMedicao) {

        mockUsuario(profile);

        MedicaoDTO medicao = new MedicaoDTO();
        medicao.setId(1L);
        medicao.setSituacao(situacaoMedicao);
        medicao.setPermiteComplementacaoValor(true);

        SubmetaMedicaoDTO submetaMedicao = buildSubmetaMedicaoAssinadaConvenente();

        step.process(submetaMedicao, buildContextComMedicao(medicao));

        assertNull(submetaMedicao.getSituacaoConvenente());
    }

    @ParameterizedTest
    @MethodSource("br.gov.planejamento.siconv.med.test.util.TestArguments#listaParametrosTodasSituacoes")
    @MockUsuario(profile = CONCEDENTE, roles = Role.FISCAL_CONCEDENTE)
    void testSituacaoSubmetaConcedente_visivel_profileConcedente(SituacaoMedicaoEnum situacaoMedicao) {

        SubmetaMedicaoDTO submetaMedicao = buildSubmetaMedicaoAssinadaConcedente();

        step.process(submetaMedicao, buildContextComMedicao(1L, situacaoMedicao));

        assertEquals(ASS, submetaMedicao.getSituacaoConcedente());
    }

    @ParameterizedTest
    @MethodSource("br.gov.planejamento.siconv.med.test.util.TestArguments#listaParametrosTodasSituacoes")
    @MockUsuario(profile = MANDATARIA, roles = Role.AGENTE_ACOMPANHAMENTO_INSTITUICAO_MANDATARIA)
    void testSituacaoSubmetaConcedente_visivel_profileMandataria(SituacaoMedicaoEnum situacaoMedicao) {

        SubmetaMedicaoDTO submetaMedicao = buildSubmetaMedicaoAssinadaConcedente();

        step.process(submetaMedicao, buildContextComMedicao(1L, situacaoMedicao));

        assertEquals(ASS, submetaMedicao.getSituacaoConcedente());
    }

    @ParameterizedTest
    @MethodSource("br.gov.planejamento.siconv.med.test.util.TestArguments#listaParametrosOutroProfileComSituacaoPermitePublicacaoConcedente")
    void testSituacaoSubmetaConcedente_visivel_outroProfile(Profile profile, SituacaoMedicaoEnum situacaoMedicao) {

        mockUsuario(profile);

        SubmetaMedicaoDTO submetaMedicao = buildSubmetaMedicaoAssinadaConcedente();

        step.process(submetaMedicao, buildContextComMedicao(1L, situacaoMedicao));

        assertEquals(ASS, submetaMedicao.getSituacaoConcedente());
    }

    @ParameterizedTest
    @MethodSource("br.gov.planejamento.siconv.med.test.util.TestArguments#listaParametrosOutroProfileComSituacaoNaoPermitePublicacaoConcedente")
    void testSituacaoSubmetaConcedente_oculto_outroProfile(Profile profile, SituacaoMedicaoEnum situacaoMedicao) {

        mockUsuario(profile);

        SubmetaMedicaoDTO submetaMedicao = buildSubmetaMedicaoAssinadaConcedente();

        step.process(submetaMedicao, buildContextComMedicao(1L, situacaoMedicao));

        assertNull(submetaMedicao.getSituacaoConcedente());
    }

    // =========================== Métodos utilitários ===========================

    private SubmetaMedicaoDTO buildSubmetaMedicaoAssinadaEmpresa() {
        return newSubmetaMedicaoBuilder().setSituacaoSubmetaEmpresa(ASS).create();
    }

    private SubmetaMedicaoDTO buildSubmetaMedicaoAssinadaConvenente() {
        return newSubmetaMedicaoBuilder().setSituacaoSubmetaConvenente(ASS).create();
    }

    private SubmetaMedicaoDTO buildSubmetaMedicaoAssinadaConcedente() {
        return newSubmetaMedicaoBuilder().setSituacaoSubmetaConcedente(ASS).create();
    }
}
