package br.gov.planejamento.siconv.med.medicao.business.builder;

import static br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.dto.TipoResponsavelTecnicoEnum.ANS;
import static br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.dto.TipoResponsavelTecnicoEnum.EXE;
import static br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.dto.TipoResponsavelTecnicoEnum.FIS;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.CONCEDENTE;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.EMPRESA;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.MANDATARIA;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.PROPONENTE_CONVENENTE;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.CE;
import static br.gov.planejamento.siconv.med.medicao.entity.dto.PerfilEnum.CVE;
import static br.gov.planejamento.siconv.med.medicao.entity.dto.PerfilEnum.EMP;
import static br.gov.planejamento.siconv.med.test.builder.ContextBuilder.newContextBuilder;
import static br.gov.planejamento.siconv.med.test.builder.ContratoMedicaoBuilder.newContratoMedicaoBuilder;
import static br.gov.planejamento.siconv.med.test.builder.SubmetaMedicaoDTOBuilder.newSubmetaMedicaoBuilder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.dto.TipoResponsavelTecnicoEnum;
import br.gov.planejamento.siconv.med.contrato.entity.ContratoBD;
import br.gov.planejamento.siconv.med.infra.security.domain.Profile;
import br.gov.planejamento.siconv.med.integration.UsuarioConsumer;
import br.gov.planejamento.siconv.med.medicao.dao.SubmetaDAO;
import br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum;
import br.gov.planejamento.siconv.med.medicao.entity.dto.MedicaoDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.PerfilEnum;
import br.gov.planejamento.siconv.med.medicao.entity.dto.ResponsavelTecnicoFiscalizacaoDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.SubmetaMedicaoDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.SubmetaMedicaoDTO.Assinatura;
import br.gov.planejamento.siconv.med.test.extension.BusinessControllerBaseTest;

class AssinaturaSubmetaStepTest extends BusinessControllerBaseTest {

    @Mock
    private SubmetaDAO submetaDAO;

    @Mock
    private UsuarioConsumer usuarioConsumer;

    @InjectMocks
    private AssinaturaSubmetaStep step;

    // Constantes
    private static final String CPF = "11111111111";
    private static final String NOME = "Nome mock";
    private static final String CREA_CAU = "0123ABCD";

    @BeforeEach
    void setup() throws Exception {
        setupDaoMock(SubmetaDAO.class, submetaDAO);
    }

    // =========================== Empresa ===========================

    @DisplayName("Assinatura empresa sempre visível para a empresa")
    @ParameterizedTest
    @MethodSource("br.gov.planejamento.siconv.med.test.util.TestArguments#listaParametrosTodasSituacoes")
    void testAssinaturaEmpresa_visivel_profileEmpresa(SituacaoMedicaoEnum situacao) {
        mockUsuario(EMPRESA);
        testAssinaturaEmpresa_visivel(buildMedicao(situacao));
    }

    @DisplayName("Assinatura empresa visível para outros profiles quando situação da medição não permite manutenção da empresa")
    @ParameterizedTest
    @MethodSource("br.gov.planejamento.siconv.med.test.util.TestArguments#listaParametrosOutroProfileComSituacaoNaoPermiteManutencaoEmpresa")
    void testAssinaturaEmpresa_visivel_outroProfile(Profile profile, SituacaoMedicaoEnum situacao) {
        mockUsuario(profile);
        testAssinaturaEmpresa_visivel(buildMedicao(situacao));
    }

    @DisplayName("Assinatura empresa visível para outros profiles quando medição em complementação pela empresa sem alteração de valores")
    @ParameterizedTest
    @MethodSource("br.gov.planejamento.siconv.med.test.util.TestArguments#listaParametrosTodosProfilesExcetoEmpresa")
    void testAssinaturaEmpresa_visivel_outroProfile_complementacao_sem_alteracao_valor(Profile profile) {
        mockUsuario(profile);
        testAssinaturaEmpresa_visivel(buildMedicaoSemComplementacaoValor(CE));
    }

    @DisplayName("Assinatura empresa oculta para outros profiles quando situação da medição permite manutenção da empresa")
    @ParameterizedTest
    @MethodSource("br.gov.planejamento.siconv.med.test.util.TestArguments#listaParametrosOutroProfileComSituacaoPermiteManutencaoEmpresa")
    void testAssinaturaEmpresa_oculta_outroProfile(Profile profile, SituacaoMedicaoEnum situacao) {
        mockUsuario(profile);
        testAssinaturaEmpresa_oculta(buildMedicao(situacao));
    }

    @DisplayName("Assinatura empresa oculta para outros profiles quando medição em complementação pela empresa com alteração de valores")
    @ParameterizedTest
    @MethodSource("br.gov.planejamento.siconv.med.test.util.TestArguments#listaParametrosTodosProfilesExcetoEmpresa")
    void testAssinaturaEmpresa_oculta_outroProfile_complementacao_com_alteracao_valor(Profile profile) {
        mockUsuario(profile);
        testAssinaturaEmpresa_oculta(buildMedicaoComComplementacaoValor(CE));
    }

    private void testAssinaturaEmpresa_visivel(MedicaoDTO medicao) {
        SubmetaMedicaoDTO submetaMedicao = testAssinaturaEmpresa(medicao);
        assertAssinaturaVisivel(submetaMedicao.getAssinaturas(), PerfilEnum.EMP);
    }

    private void testAssinaturaEmpresa_oculta(MedicaoDTO medicao) {
        SubmetaMedicaoDTO submetaMedicao = testAssinaturaEmpresa(medicao);
        assertEquals(0, submetaMedicao.getAssinaturas().size());
    }

    private SubmetaMedicaoDTO testAssinaturaEmpresa(MedicaoDTO medicao) {

        ContratoBD contrato = newContratoMedicaoBuilder().create();

        SubmetaMedicaoDTO submetaMedicao = newSubmetaMedicaoBuilder().assinarEmpresa(CPF).create();

        TipoResponsavelTecnicoEnum tipoRT = EXE;
        ResponsavelTecnicoFiscalizacaoDTO rt = buildResponsavelTecnico(tipoRT);

        when(submetaDAO.consultarDadosResponsavelTecnicoArqEng(eq(CPF), any())).thenReturn(rt);
        when(usuarioConsumer.getNomeUsuarioPorTipoRT(CPF, tipoRT, true)).thenReturn(NOME);

        step.process(submetaMedicao, newContextBuilder().withContrato(contrato).withMedicao(medicao).create());

        return submetaMedicao;
    }

    // =========================== Convenente ===========================

    @DisplayName("Assinatura convenente sempre visível para o convenente")
    @ParameterizedTest
    @MethodSource("br.gov.planejamento.siconv.med.test.util.TestArguments#listaParametrosTodasSituacoes")
    void testAssinaturaConvenente_visivel_profileConvenente(SituacaoMedicaoEnum situacao) {
        mockUsuario(PROPONENTE_CONVENENTE);
        testAssinaturaConvenente_visivel(buildMedicao(situacao));
    }

    @DisplayName("Assinatura convenente visível para outros profiles quando situação da medição não permite manutenção do convenente")
    @ParameterizedTest
    @MethodSource("br.gov.planejamento.siconv.med.test.util.TestArguments#listaParametrosOutroProfileComSituacaoNaoPermiteManutencaoConvenente")
    void testAssinaturaConvenente_visivel_outroProfile(Profile profile, SituacaoMedicaoEnum situacao) {
        mockUsuario(profile);
        testAssinaturaConvenente_visivel(buildMedicao(situacao));
    }

    @DisplayName("Assinatura convenente visível para outros profiles quando medição proveniente complementação sem alteração de valores")
    @ParameterizedTest
    @MethodSource("br.gov.planejamento.siconv.med.test.util.TestArguments#listaParametrosOutroProfileComSituacaoPermiteManutencaoConvenente")
    void testAssinaturaConvenente_visivel_outroProfile_complementacao_sem_alteracao_valor(Profile profile,
            SituacaoMedicaoEnum situacaoMedicao) {
        mockUsuario(profile);
        testAssinaturaConvenente_visivel(buildMedicaoSemComplementacaoValor(situacaoMedicao));
    }

    @DisplayName("Assinatura convenente oculta para outros profiles quando situação da medição permite manutenção do convenente")
    @ParameterizedTest
    @MethodSource("br.gov.planejamento.siconv.med.test.util.TestArguments#listaParametrosOutroProfileComSituacaoPermiteManutencaoConvenente")
    void testAssinaturaConvenente_oculta_outroProfile(Profile profile, SituacaoMedicaoEnum situacao) {
        mockUsuario(profile);
        testAssinaturaConvenente_oculta(buildMedicao(situacao));
    }

    @DisplayName("Assinatura convenente oculta para outros profiles quando medição proveniente complementação com alteração de valores")
    @ParameterizedTest
    @MethodSource("br.gov.planejamento.siconv.med.test.util.TestArguments#listaParametrosOutroProfileComSituacaoPermiteManutencaoConvenente")
    void testAssinaturaConvenente_oculta_outroProfile_complementacao_com_alteracao_valor(Profile profile,
            SituacaoMedicaoEnum situacaoMedicao) {
        mockUsuario(profile);
        testAssinaturaConvenente_oculta(buildMedicaoComComplementacaoValor(situacaoMedicao));
    }

    private void testAssinaturaConvenente_visivel(MedicaoDTO medicao) {
        SubmetaMedicaoDTO submetaMedicao = testAssinaturaConvenente(medicao);
        assertAssinaturaVisivel(submetaMedicao.getAssinaturas(), PerfilEnum.CVE);
    }

    private void testAssinaturaConvenente_oculta(MedicaoDTO medicao) {
        SubmetaMedicaoDTO submetaMedicao = testAssinaturaConvenente(medicao);
        assertEquals(0, submetaMedicao.getAssinaturas().size());
    }

    private SubmetaMedicaoDTO testAssinaturaConvenente(MedicaoDTO medicao) {

        ContratoBD contratoSocial = newContratoMedicaoBuilder().isSocial().create();

        SubmetaMedicaoDTO submetaMedicao = newSubmetaMedicaoBuilder().assinarConvenente(CPF).create();

        TipoResponsavelTecnicoEnum tipoRT = FIS;
        ResponsavelTecnicoFiscalizacaoDTO rt = buildResponsavelTecnico(tipoRT);

        when(submetaDAO.consultarDadosResponsavelTecnicoSocial(eq(CPF), any())).thenReturn(rt);
        when(usuarioConsumer.getNomeUsuarioPorTipoRT(CPF, tipoRT, true)).thenReturn(NOME);

        step.process(submetaMedicao, newContextBuilder().withContrato(contratoSocial).withMedicao(medicao).create());

        return submetaMedicao;
    }

    // =========================== Concedente/Mandatária ===========================

    @DisplayName("Assinatura concedente/mandatária sempre visível para o concedente")
    @ParameterizedTest
    @MethodSource("br.gov.planejamento.siconv.med.test.util.TestArguments#listaParametrosTodasSituacoes")
    void testAssinaturaConcedenteMandataria_visivel_profileConcedente(SituacaoMedicaoEnum situacao) {
        mockUsuario(CONCEDENTE);
        testAssinaturaConcedenteMandataria_visivel(buildMedicao(situacao));
    }

    @DisplayName("Assinatura concedente/mandatária sempre visível para a mandatária")
    @ParameterizedTest
    @MethodSource("br.gov.planejamento.siconv.med.test.util.TestArguments#listaParametrosTodasSituacoes")
    void testAssinaturaConcedenteMandataria_visivel_profileMandataria(SituacaoMedicaoEnum situacao) {
        mockUsuario(MANDATARIA);
        testAssinaturaConcedenteMandataria_visivel(buildMedicao(situacao));
    }

    @DisplayName("Assinatura concedente/mandatária visível para outros profiles quando situação da medição permite publicação dos dados concedente")
    @ParameterizedTest
    @MethodSource("br.gov.planejamento.siconv.med.test.util.TestArguments#listaParametrosOutroProfileComSituacaoPermitePublicacaoConcedente")
    void testAssinaturaConcedenteMandataria_visivel_outroProfile(Profile profile, SituacaoMedicaoEnum situacao) {
        mockUsuario(profile);
        testAssinaturaConcedenteMandataria_visivel(buildMedicao(situacao));
    }

    @DisplayName("Assinatura concedente/mandatária oculta para outros profiles quando situação da medição não permite publicação dos dados concedente")
    @ParameterizedTest
    @MethodSource("br.gov.planejamento.siconv.med.test.util.TestArguments#listaParametrosOutroProfileComSituacaoNaoPermitePublicacaoConcedente")
    void testAssinaturaConcedenteMandataria_oculta_outroProfile(Profile profile, SituacaoMedicaoEnum situacao) {
        mockUsuario(profile);
        testAssinaturaConcedenteMandataria_oculta(buildMedicao(situacao));
    }

    private void testAssinaturaConcedenteMandataria_visivel(MedicaoDTO medicao) {
        SubmetaMedicaoDTO submetaMedicao = testAssinaturaConcedenteMandataria(medicao);
        assertAssinaturaVisivel(submetaMedicao.getAssinaturas(), PerfilEnum.CCE);
    }

    private void testAssinaturaConcedenteMandataria_oculta(MedicaoDTO medicao) {
        SubmetaMedicaoDTO submetaMedicao = testAssinaturaConcedenteMandataria(medicao);
        assertEquals(0, submetaMedicao.getAssinaturas().size());
    }

    private SubmetaMedicaoDTO testAssinaturaConcedenteMandataria(MedicaoDTO medicao) {

        ContratoBD contrato = newContratoMedicaoBuilder().create();

        SubmetaMedicaoDTO submetaMedicao = newSubmetaMedicaoBuilder().assinarConcedente(CPF).create();

        when(usuarioConsumer.getNomeUsuarioPorTipoRT(CPF, ANS, true)).thenReturn(NOME);

        step.process(submetaMedicao, newContextBuilder().withContrato(contrato).withMedicao(medicao).create());

        return submetaMedicao;
    }

    // =========================== Métodos utilitários ===========================

    private void assertAssinaturaVisivel(List<Assinatura> assinaturas, PerfilEnum perfil) {
        assertEquals(1, assinaturas.size());
        assertEquals(perfil.getDescricao(), assinaturas.get(0).getResponsavel().getPerfil());
        assertEquals(NOME, assinaturas.get(0).getResponsavel().getNome());
        assertEquals(CPF, assinaturas.get(0).getResponsavel().getNrCpf());

        if (perfil == CVE || perfil == EMP) {
            assertEquals(CREA_CAU, assinaturas.get(0).getResponsavel().getNrCrea());
        }
    }

    private ResponsavelTecnicoFiscalizacaoDTO buildResponsavelTecnico(TipoResponsavelTecnicoEnum tipo) {
        ResponsavelTecnicoFiscalizacaoDTO rt = new ResponsavelTecnicoFiscalizacaoDTO();
        rt.setTipo(tipo);
        rt.setNrCpfResponsavelTecnico(CPF);
        rt.setCdResponsavelTecnico(CREA_CAU);
        return rt;
    }

    private MedicaoDTO buildMedicaoComComplementacaoValor(SituacaoMedicaoEnum situacao) {
        MedicaoDTO medicao = buildMedicao(situacao);
        medicao.setPermiteComplementacaoValor(true);
        return medicao;
    }

    private MedicaoDTO buildMedicaoSemComplementacaoValor(SituacaoMedicaoEnum situacao) {
        MedicaoDTO medicao = buildMedicao(situacao);
        medicao.setPermiteComplementacaoValor(false);
        return medicao;
    }

    private MedicaoDTO buildMedicao(SituacaoMedicaoEnum situacao) {
        MedicaoDTO medicao = new MedicaoDTO();
        medicao.setId(1L);
        medicao.setSituacao(situacao);
        return medicao;
    }
}
