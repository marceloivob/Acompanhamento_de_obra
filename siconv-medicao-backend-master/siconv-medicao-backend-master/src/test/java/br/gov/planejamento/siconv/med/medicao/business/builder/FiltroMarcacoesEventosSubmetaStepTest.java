package br.gov.planejamento.siconv.med.medicao.business.builder;

import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.CONCEDENTE;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.EMPRESA;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.MANDATARIA;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.PROPONENTE_CONVENENTE;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.CE;
import static br.gov.planejamento.siconv.med.test.builder.ContextBuilder.buildContextComMedicao;
import static br.gov.planejamento.siconv.med.test.builder.SubmetaMedicaoDTOBuilder.newSubmetaMedicaoBuilder_ComEvento;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;

import br.gov.planejamento.siconv.med.infra.security.domain.Permission;
import br.gov.planejamento.siconv.med.infra.security.domain.Profile;
import br.gov.planejamento.siconv.med.infra.security.domain.Role;
import br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum;
import br.gov.planejamento.siconv.med.medicao.entity.dto.EventoVrplDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.MedicaoDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.SubmetaMedicaoDTO;
import br.gov.planejamento.siconv.med.test.extension.BusinessControllerBaseTest;
import br.gov.planejamento.siconv.med.test.extension.MockUsuario;

class FiltroMarcacoesEventosSubmetaStepTest extends BusinessControllerBaseTest {

    @InjectMocks
    private FiltroMarcacoesEventosSubmetaStep step;

    @ParameterizedTest
    @MethodSource("br.gov.planejamento.siconv.med.test.util.TestArguments#listaParametrosTodasSituacoes")
    @MockUsuario(profile = EMPRESA, permissions = Permission.VISUALIZAR_MEDICAO)
    void testIdMedicaoEmpresa_visivel_profileEmpresa(SituacaoMedicaoEnum situacao) {

        SubmetaMedicaoDTO submetaMedicao = buildSubmetaComEventoMedidoEmpresa(1L);
        EventoVrplDTO evento = getEvento(submetaMedicao);

        step.process(submetaMedicao, buildContextComMedicao(1L, situacao));

        assertEquals(1L, evento.getIdMedicaoEmpresa());
    }

    @ParameterizedTest
    @MethodSource("br.gov.planejamento.siconv.med.test.util.TestArguments#listaParametrosOutroProfileComSituacaoNaoPermiteManutencaoEmpresa")
    void testIdMedicaoEmpresa_visivel_outroProfile(Profile profile, SituacaoMedicaoEnum situacao) {

        mockUsuario(profile);

        SubmetaMedicaoDTO submetaMedicao = buildSubmetaComEventoMedidoEmpresa(1L);
        EventoVrplDTO evento = getEvento(submetaMedicao);

        step.process(submetaMedicao, buildContextComMedicao(1L, situacao));

        assertEquals(1L, evento.getIdMedicaoEmpresa());
    }

    @ParameterizedTest
    @MethodSource("br.gov.planejamento.siconv.med.test.util.TestArguments#listaParametrosTodosProfilesExcetoEmpresa")
    void testIdMedicaoEmpresa_visivel_outroProfile_complementacao_sem_alteracao_valor(Profile profile) {

        mockUsuario(profile);

        MedicaoDTO medicao = new MedicaoDTO();
        medicao.setId(1L);
        medicao.setSituacao(CE);
        medicao.setPermiteComplementacaoValor(false);

        SubmetaMedicaoDTO submetaMedicao = buildSubmetaComEventoMedidoEmpresa(1L);
        EventoVrplDTO evento = getEvento(submetaMedicao);

        step.process(submetaMedicao, buildContextComMedicao(medicao));

        assertEquals(1L, evento.getIdMedicaoEmpresa());
    }

    @ParameterizedTest
    @MethodSource("br.gov.planejamento.siconv.med.test.util.TestArguments#listaParametrosOutroProfileComSituacaoPermiteManutencaoEmpresa")
    void testIdMedicaoEmpresa_oculto_outroProfile(Profile profile, SituacaoMedicaoEnum situacao) {

        mockUsuario(profile);

        SubmetaMedicaoDTO submetaMedicao = buildSubmetaComEventoMedidoEmpresa(1L);
        EventoVrplDTO evento = getEvento(submetaMedicao);

        step.process(submetaMedicao, buildContextComMedicao(1L, situacao));

        assertNull(evento.getIdMedicaoEmpresa());
    }

    @ParameterizedTest
    @MethodSource("br.gov.planejamento.siconv.med.test.util.TestArguments#listaParametrosTodosProfilesExcetoEmpresa")
    void testIdMedicaoEmpresa_oculto_outroProfile_complementacao_com_alteracao_valor(Profile profile) {

        mockUsuario(profile);

        MedicaoDTO medicao = new MedicaoDTO();
        medicao.setId(1L);
        medicao.setSituacao(CE);
        medicao.setPermiteComplementacaoValor(true);

        SubmetaMedicaoDTO submetaMedicao = buildSubmetaComEventoMedidoEmpresa(1L);
        EventoVrplDTO evento = getEvento(submetaMedicao);

        step.process(submetaMedicao, buildContextComMedicao(medicao));

        assertNull(evento.getIdMedicaoEmpresa());
    }

    @Test
    @MockUsuario(profile = EMPRESA, permissions = Permission.VISUALIZAR_MEDICAO)
    void testIdMedicaoEmpresa_oculto_medicao_posterior() {

        MedicaoDTO medicao = new MedicaoDTO();
        medicao.setId(1L);

        SubmetaMedicaoDTO submetaMedicao = buildSubmetaComEventoMedidoEmpresa(2L);
        EventoVrplDTO evento = getEvento(submetaMedicao);

        step.process(submetaMedicao, buildContextComMedicao(medicao));

        assertNull(evento.getIdMedicaoEmpresa());
    }

    @ParameterizedTest
    @MethodSource("br.gov.planejamento.siconv.med.test.util.TestArguments#listaParametrosTodasSituacoes")
    @MockUsuario(profile = PROPONENTE_CONVENENTE, roles = Role.FISCAL_CONVENENTE)
    void testIdMedicaoConvenente_visivel_profileConvenente(SituacaoMedicaoEnum situacao) {

        SubmetaMedicaoDTO submetaMedicao = buildSubmetaComEventoMedidoConvenente(1L);
        EventoVrplDTO evento = getEvento(submetaMedicao);

        step.process(submetaMedicao, buildContextComMedicao(1L, situacao));

        assertEquals(1L, evento.getIdMedicaoConvenente());
    }

    @ParameterizedTest
    @MethodSource("br.gov.planejamento.siconv.med.test.util.TestArguments#listaParametrosOutroProfileComSituacaoNaoPermiteManutencaoConvenente")
    void testIdMedicaoConvenente_visivel_outroProfile(Profile profile, SituacaoMedicaoEnum situacao) {

        mockUsuario(profile);

        SubmetaMedicaoDTO submetaMedicao = buildSubmetaComEventoMedidoConvenente(1L);
        EventoVrplDTO evento = getEvento(submetaMedicao);

        step.process(submetaMedicao, buildContextComMedicao(1L, situacao));

        assertEquals(1L, evento.getIdMedicaoConvenente());
    }

    @ParameterizedTest
    @MethodSource("br.gov.planejamento.siconv.med.test.util.TestArguments#listaParametrosOutroProfileComSituacaoPermiteManutencaoConvenente")
    void testIdMedicaoConvenente_visivel_outroProfile_complementacao_sem_alteracao_valor(Profile profile,
            SituacaoMedicaoEnum situacao) {

        mockUsuario(profile);

        MedicaoDTO medicao = new MedicaoDTO();
        medicao.setId(1L);
        medicao.setSituacao(situacao);
        medicao.setPermiteComplementacaoValor(false);

        SubmetaMedicaoDTO submetaMedicao = buildSubmetaComEventoMedidoConvenente(1L);
        EventoVrplDTO evento = getEvento(submetaMedicao);

        step.process(submetaMedicao, buildContextComMedicao(medicao));

        assertEquals(1L, evento.getIdMedicaoConvenente());
    }

    @ParameterizedTest
    @MethodSource("br.gov.planejamento.siconv.med.test.util.TestArguments#listaParametrosOutroProfileComSituacaoPermiteManutencaoConvenente")
    void testIdMedicaoConvenente_oculto_outroProfile(Profile profile, SituacaoMedicaoEnum situacao) {

        mockUsuario(profile);

        SubmetaMedicaoDTO submetaMedicao = buildSubmetaComEventoMedidoConvenente(1L);
        EventoVrplDTO evento = getEvento(submetaMedicao);

        step.process(submetaMedicao, buildContextComMedicao(1L, situacao));

        assertNull(evento.getIdMedicaoConvenente());
    }

    @ParameterizedTest
    @MethodSource("br.gov.planejamento.siconv.med.test.util.TestArguments#listaParametrosOutroProfileComSituacaoPermiteManutencaoConvenente")
    void testIdMedicaoConvenente_oculto_outroProfile_complementacao_com_alteracao_valor(Profile profile,
            SituacaoMedicaoEnum situacao) {

        mockUsuario(profile);

        MedicaoDTO medicao = new MedicaoDTO();
        medicao.setId(1L);
        medicao.setSituacao(situacao);
        medicao.setPermiteComplementacaoValor(true);

        SubmetaMedicaoDTO submetaMedicao = buildSubmetaComEventoMedidoConvenente(1L);
        EventoVrplDTO evento = getEvento(submetaMedicao);

        step.process(submetaMedicao, buildContextComMedicao(medicao));

        assertNull(evento.getIdMedicaoConvenente());
    }

    @Test
    @MockUsuario(profile = PROPONENTE_CONVENENTE, roles = Role.FISCAL_CONVENENTE)
    void testIdMedicaoConvenente_oculto_medicao_posterior() {

        MedicaoDTO medicao = new MedicaoDTO();
        medicao.setId(1L);

        SubmetaMedicaoDTO submetaMedicao = buildSubmetaComEventoMedidoConvenente(2L);
        EventoVrplDTO evento = getEvento(submetaMedicao);

        step.process(submetaMedicao, buildContextComMedicao(medicao));

        assertNull(evento.getIdMedicaoConvenente());
    }

    @ParameterizedTest
    @MethodSource("br.gov.planejamento.siconv.med.test.util.TestArguments#listaParametrosTodasSituacoes")
    @MockUsuario(profile = CONCEDENTE, roles = Role.FISCAL_CONCEDENTE)
    void testIdMedicaoConcedente_visivel_profileConcedente(SituacaoMedicaoEnum situacao) {

        SubmetaMedicaoDTO submetaMedicao = buildSubmetaComEventoMedidoConcedente(1L);
        EventoVrplDTO evento = getEvento(submetaMedicao);

        step.process(submetaMedicao, buildContextComMedicao(1L, situacao));

        assertEquals(1L, evento.getIdMedicaoConcedente());
    }

    @ParameterizedTest
    @MethodSource("br.gov.planejamento.siconv.med.test.util.TestArguments#listaParametrosTodasSituacoes")
    @MockUsuario(profile = MANDATARIA, roles = Role.AGENTE_ACOMPANHAMENTO_INSTITUICAO_MANDATARIA)
    void testIdMedicaoConcedente_visivel_profileMandataria(SituacaoMedicaoEnum situacao) {

        SubmetaMedicaoDTO submetaMedicao = buildSubmetaComEventoMedidoConcedente(1L);
        EventoVrplDTO evento = getEvento(submetaMedicao);

        step.process(submetaMedicao, buildContextComMedicao(1L, situacao));

        assertEquals(1L, evento.getIdMedicaoConcedente());
    }

    @ParameterizedTest
    @MethodSource("br.gov.planejamento.siconv.med.test.util.TestArguments#listaParametrosOutroProfileComSituacaoPermitePublicacaoConcedente")
    void testIdMedicaoConcedente_visivel_outroProfile(Profile profile, SituacaoMedicaoEnum situacao) {

        mockUsuario(profile);

        SubmetaMedicaoDTO submetaMedicao = buildSubmetaComEventoMedidoConcedente(1L);
        EventoVrplDTO evento = getEvento(submetaMedicao);

        step.process(submetaMedicao, buildContextComMedicao(1L, situacao));

        assertEquals(1L, evento.getIdMedicaoConcedente());
    }

    @ParameterizedTest
    @MethodSource("br.gov.planejamento.siconv.med.test.util.TestArguments#listaParametrosOutroProfileComSituacaoNaoPermitePublicacaoConcedente")
    void testIdMedicaoConcedente_oculto_outroProfile(Profile profile, SituacaoMedicaoEnum situacao) {

        mockUsuario(profile);

        SubmetaMedicaoDTO submetaMedicao = buildSubmetaComEventoMedidoConcedente(1L);
        EventoVrplDTO evento = getEvento(submetaMedicao);

        step.process(submetaMedicao, buildContextComMedicao(1L, situacao));

        assertNull(evento.getIdMedicaoConcedente());
    }

    @Test
    @MockUsuario(profile = CONCEDENTE, roles = Role.FISCAL_CONCEDENTE)
    void testIdMedicaoConcedente_oculto_medicao_posterior() {

        MedicaoDTO medicao = new MedicaoDTO();
        medicao.setId(1L);

        SubmetaMedicaoDTO submetaMedicao = buildSubmetaComEventoMedidoConcedente(2L);
        EventoVrplDTO evento = getEvento(submetaMedicao);

        step.process(submetaMedicao, buildContextComMedicao(medicao));

        assertNull(evento.getIdMedicaoConcedente());
    }

    // =========================== Métodos utilitários ===========================

    private SubmetaMedicaoDTO buildSubmetaComEventoMedidoEmpresa(Long idMedicaoMarcacaoEvento) {
        return newSubmetaMedicaoBuilder_ComEvento(idMedicaoMarcacaoEvento)
                .setEventoMedidoEmpresa(idMedicaoMarcacaoEvento).create();
    }

    private SubmetaMedicaoDTO buildSubmetaComEventoMedidoConvenente(Long idMedicaoMarcacaoEvento) {
        return newSubmetaMedicaoBuilder_ComEvento(idMedicaoMarcacaoEvento)
                .setEventoMedidoConvenente(idMedicaoMarcacaoEvento).create();
    }

    private SubmetaMedicaoDTO buildSubmetaComEventoMedidoConcedente(Long idMedicaoMarcacaoEvento) {
        return newSubmetaMedicaoBuilder_ComEvento(idMedicaoMarcacaoEvento)
                .setEventoMedidoConcedente(idMedicaoMarcacaoEvento).create();
    }

    private EventoVrplDTO getEvento(SubmetaMedicaoDTO submetaMedicao) {
        return submetaMedicao.getFrentesObra().get(0).getEventos().get(0);
    }
}
