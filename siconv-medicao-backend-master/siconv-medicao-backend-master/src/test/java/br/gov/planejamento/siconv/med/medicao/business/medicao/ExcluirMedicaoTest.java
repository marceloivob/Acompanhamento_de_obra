package br.gov.planejamento.siconv.med.medicao.business.medicao;

import static br.gov.planejamento.siconv.med.infra.security.domain.Permission.EXCLUIR_MEDICAO;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.CONCEDENTE;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.EMPRESA;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.PROPONENTE_CONVENENTE;
import static br.gov.planejamento.siconv.med.infra.security.domain.Role.ADMINISTRADOR_SISTEMA;
import static br.gov.planejamento.siconv.med.infra.security.domain.Role.ADMINISTRADOR_SISTEMA_ORGAO_EXTERNO;
import static br.gov.planejamento.siconv.med.infra.security.domain.Role.FISCAL_ACOMPANHAMENTO;
import static br.gov.planejamento.siconv.med.infra.security.domain.Role.FISCAL_CONCEDENTE;
import static br.gov.planejamento.siconv.med.infra.security.domain.Role.FISCAL_CONVENENTE;
import static br.gov.planejamento.siconv.med.infra.security.domain.Role.GESTOR_CONVENIO_CONCEDENTE;
import static br.gov.planejamento.siconv.med.infra.security.domain.Role.GESTOR_CONVENIO_CONVENENTE;
import static br.gov.planejamento.siconv.med.infra.security.domain.Role.GESTOR_FINANCEIRO_CONCEDENTE;
import static br.gov.planejamento.siconv.med.infra.security.domain.Role.GESTOR_FINANCEIRO_CONVENENTE;
import static br.gov.planejamento.siconv.med.infra.security.domain.Role.OPERACIONAL_CONCEDENTE;
import static br.gov.planejamento.siconv.med.infra.security.domain.Role.OPERADOR_FINANCEIRO_CONVENENTE;
import static br.gov.planejamento.siconv.med.infra.security.domain.Role.TECNICO_TERCEIRO;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.ACT;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.AT;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.EC;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.EM;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.EXC;
import static br.gov.planejamento.siconv.med.test.builder.ContratoMedicaoBuilder.newContratoMedicaoBuilder;
import static br.gov.planejamento.siconv.med.test.builder.MedicaoBuilder.newMedicaoBuilder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.verification.VerificationMode;

import br.gov.planejamento.siconv.med.configuracao.documentocomplementar.business.DocumentoComplementarBC;
import br.gov.planejamento.siconv.med.contrato.dao.ContratoDAO;
import br.gov.planejamento.siconv.med.contrato.entity.ContratoBD;
import br.gov.planejamento.siconv.med.infra.message.MessageKey;
import br.gov.planejamento.siconv.med.infra.security.domain.Profile;
import br.gov.planejamento.siconv.med.medicao.business.HistoricoMedicaoBC;
import br.gov.planejamento.siconv.med.medicao.business.MedicaoBC;
import br.gov.planejamento.siconv.med.medicao.dao.AnexoDAO;
import br.gov.planejamento.siconv.med.medicao.dao.ItemMedicaoDAO;
import br.gov.planejamento.siconv.med.medicao.dao.MedicaoDAO;
import br.gov.planejamento.siconv.med.medicao.dao.ObservacaoDAO;
import br.gov.planejamento.siconv.med.medicao.dao.SubmetaDAO;
import br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum;
import br.gov.planejamento.siconv.med.medicao.entity.database.HistoricoMedicaoBD;
import br.gov.planejamento.siconv.med.medicao.entity.database.MedicaoBD;
import br.gov.planejamento.siconv.med.test.extension.BusinessControllerBaseTest;
import br.gov.planejamento.siconv.med.test.extension.MockUsuario;

class ExcluirMedicaoTest extends BusinessControllerBaseTest {

    @Mock
    private ContratoDAO contratoDAO;

    @Mock
    private MedicaoDAO medicaoDAO;

    @Mock
    private AnexoDAO anexoDAO;

    @Mock
    private ObservacaoDAO observacaoDAO;

    @Mock
    private SubmetaDAO submetaDAO;

    @Mock
    private ItemMedicaoDAO itemMedicaoDAO;

    @Mock
    private HistoricoMedicaoBC historicoBC;

    @Mock
    private DocumentoComplementarBC documentoComplementarBC;

    @InjectMocks
    private MedicaoBC medicaoBC;

    @Captor
    private ArgumentCaptor<HistoricoMedicaoBD> historicoMedicaoCaptor;

    private final Long idContrato = 771L;
    private final Long idMedicao = 891L;
    private final Short nrSequencial = 1;
    private final Long contratoFk = 1L;

    @BeforeEach
    void setup() throws Exception {

        setupDaoMock(ContratoDAO.class, contratoDAO);
        setupDaoMock(MedicaoDAO.class, medicaoDAO);
        setupDaoMock(AnexoDAO.class, anexoDAO);
        setupDaoMock(ObservacaoDAO.class, observacaoDAO);
        setupDaoMock(SubmetaDAO.class, submetaDAO);
        setupDaoMock(ItemMedicaoDAO.class, itemMedicaoDAO);
    }

    @Test
    void testExcluirMedicao_idNulo() {

        assertThrows(NullPointerException.class, () -> medicaoBC.excluirMedicao(null),
                "Parâmetro idMedicao não pode ser nulo");
    }

    @Test
    void testExcluirMedicao_inexistente() {

        when(medicaoDAO.consultarMedicao(idMedicao)).thenReturn(null);

        assertThrowsMedicaoRestException(MessageKey.ERRO_MEDICAO_NAO_ENCONTRADA,
                () -> medicaoBC.excluirMedicao(idMedicao));
    }

    @Test
    void testExcluirMedicao_diferenteUltimaMedicao() {

        MedicaoBD medicao = newMedicaoBuilder().setId(idMedicao)
                                               .setMedContrato(idContrato)
                                               .comSituacao(EC)
                                               .create();

        ContratoBD contrato = newContratoMedicaoBuilder().setId(idContrato)
                                                         .setContratoFk(contratoFk)
                                                         .create();

        MedicaoBD ultimaMedicao = newMedicaoBuilder().create();

        when(medicaoDAO.consultarMedicao(idMedicao)).thenReturn(medicao);
        when(contratoDAO.consultarContrato(idContrato)).thenReturn(contrato);
        when(medicaoDAO.consultarUltimaMedicao(contratoFk)).thenReturn(ultimaMedicao);

        assertThrowsMedicaoRestException(MessageKey.ERRO_EXCLUIR_MEDICAO, () -> medicaoBC.excluirMedicao(idMedicao));
    }

    @ParameterizedTest
    @ValueSource(strings = { "EC", "AT", "ATD", "ECE", "AC", "ACT", "ECC", "CC" })
    @MockUsuario(profile = EMPRESA, permissions = EXCLUIR_MEDICAO)
    void testExcluirMedicao_semPermissao_empresa(String codigoSituacao) {
        testExcluirMedicao_semPermissao(SituacaoMedicaoEnum.fromCodigo(codigoSituacao));
    }

    @ParameterizedTest
    @ValueSource(strings = { "EM", "EC", "ATD", "ECE", "CE", "AC", "ACT", "ECC" })
    @MockUsuario(profile = PROPONENTE_CONVENENTE, roles = { FISCAL_CONVENENTE, GESTOR_CONVENIO_CONVENENTE,
            GESTOR_FINANCEIRO_CONVENENTE, OPERADOR_FINANCEIRO_CONVENENTE })
    void testExcluirMedicao_semPermissao_convenente(String codigoSituacao) {
        testExcluirMedicao_semPermissao(SituacaoMedicaoEnum.fromCodigo(codigoSituacao));
    }

    @Test
    @MockUsuario(profile = CONCEDENTE, roles = { FISCAL_CONCEDENTE, OPERACIONAL_CONCEDENTE, GESTOR_CONVENIO_CONCEDENTE,
            GESTOR_FINANCEIRO_CONCEDENTE, FISCAL_ACOMPANHAMENTO, TECNICO_TERCEIRO })
    void testExcluirMedicao_semPermissao_concedente() {
        testExcluirMedicao_semPermissao(null); // independente da situacao
    }

    @ParameterizedTest
    @ValueSource(strings = { "mandataria", "guest", "usuario_siconv" })
    void testExcluirMedicao_semPermissao_outrosProfiles(String keyProfile) {
        mockUsuario(Profile.fromKey(keyProfile));
        testExcluirMedicao_semPermissao(null); // independente da situacao
    }

    void testExcluirMedicao_semPermissao(SituacaoMedicaoEnum situacao) {

        MedicaoBD medicao = newMedicaoBuilder().setId(idMedicao)
                                               .setMedContrato(idContrato)
                                               .comSituacao(situacao)
                                               .create();

        ContratoBD contrato = newContratoMedicaoBuilder().setId(idContrato)
                                                         .setContratoFk(contratoFk)
                                                         .create();

        when(medicaoDAO.consultarMedicao(idMedicao)).thenReturn(medicao);
        when(contratoDAO.consultarContrato(idContrato)).thenReturn(contrato);
        when(medicaoDAO.consultarUltimaMedicao(contratoFk)).thenReturn(medicao);

        assertThrowsMedicaoRestException(MessageKey.ERRO_EXCLUIR_MEDICAO, () -> medicaoBC.excluirMedicao(idMedicao));
    }

    @Test
    @MockUsuario(profile = EMPRESA, permissions = EXCLUIR_MEDICAO)
    void testExcluirMedicao_emElaboracao_semHistorico() {

        MedicaoBD medicao = newMedicaoBuilder().setId(idMedicao)
                                               .setMedContrato(idContrato)
                                               .setNrSequencial(nrSequencial)
                                               .comSituacao(EM)
                                               .create();

        ContratoBD contrato = newContratoMedicaoBuilder().setId(idContrato)
                                                         .setContratoFk(contratoFk)
                                                         .create();

        HistoricoMedicaoBD ultimoHistoricoMedicao = new HistoricoMedicaoBD(idContrato, nrSequencial, EXC);

        int qtdMedicoesAposExclusao = 1; // Nao desbloqueia documentos

        when(medicaoDAO.consultarMedicao(idMedicao)).thenReturn(medicao);
        when(medicaoDAO.consultarUltimaMedicao(contratoFk)).thenReturn(medicao);
        when(medicaoDAO.consultarQtdeMedicoesPorContrato(contratoFk)).thenReturn(qtdMedicoesAposExclusao);
        when(contratoDAO.consultarContrato(idContrato)).thenReturn(contrato);
        when(historicoBC.recuperarUltimoHistoricoPorMedicaoContrato(idContrato, nrSequencial))
                .thenReturn(Optional.of(ultimoHistoricoMedicao));

        medicaoBC.excluirMedicao(idMedicao);

        verifyDao(times(1));
        verify(historicoBC, never()).inserir(Mockito.any());
        verify(documentoComplementarBC, never()).desbloquearDocumentosComplementares(handle, idContrato);
    }

    @Test
    @MockUsuario(profile = EMPRESA, permissions = EXCLUIR_MEDICAO)
    void testExcluirMedicao_emElaboracao_comHistorico() {

        MedicaoBD medicao = newMedicaoBuilder().setId(idMedicao)
                                               .setMedContrato(idContrato)
                                               .setNrSequencial(nrSequencial)
                                               .comSituacao(EM)
                                               .setBloqueada(true) // mesmo bloqueada pode excluir
                                               .create();

        ContratoBD contrato = newContratoMedicaoBuilder().setId(idContrato)
                                                         .setContratoFk(contratoFk)
                                                         .create();

        // Historico de medicao que foi enviada convenente e depois cancelado
        HistoricoMedicaoBD ultimoHistoricoMedicao = new HistoricoMedicaoBD(idContrato, nrSequencial, EM);

        int qtdMedicoesAposExclusao = 0; // Faz desbloqueio documentos

        when(medicaoDAO.consultarMedicao(idMedicao)).thenReturn(medicao);
        when(medicaoDAO.consultarUltimaMedicao(contratoFk)).thenReturn(medicao);
        when(medicaoDAO.consultarQtdeMedicoesPorContrato(contratoFk)).thenReturn(qtdMedicoesAposExclusao);
        when(contratoDAO.consultarContrato(idContrato)).thenReturn(contrato);
        when(historicoBC.recuperarUltimoHistoricoPorMedicaoContrato(idContrato, nrSequencial))
                .thenReturn(Optional.of(ultimoHistoricoMedicao));

        medicaoBC.excluirMedicao(idMedicao);

        verifyDao(times(1));

        verify(historicoBC, times(1)).inserir(historicoMedicaoCaptor.capture());

        assertEquals(idContrato, historicoMedicaoCaptor.getValue().getIdContratoMedicao());
        assertEquals(nrSequencial, historicoMedicaoCaptor.getValue().getNrSequencial());
        assertEquals(EXC, historicoMedicaoCaptor.getValue().getSituacao());

        verify(documentoComplementarBC, times(1)).desbloquearDocumentosComplementares(handle, idContrato);
    }

    @Test
    @MockUsuario(profile = PROPONENTE_CONVENENTE, roles = { FISCAL_CONVENENTE, GESTOR_CONVENIO_CONVENENTE,
            GESTOR_FINANCEIRO_CONVENENTE, OPERADOR_FINANCEIRO_CONVENENTE })
    void testExcluirMedicao_emAteste() {

        MedicaoBD medicao = newMedicaoBuilder().setId(idMedicao)
                                               .setMedContrato(idContrato)
                                               .setNrSequencial(nrSequencial)
                                               .comSituacao(AT)
                                               .setBloqueada(true) // mesmo bloqueada pode excluir
                                               .create();

        ContratoBD contrato = newContratoMedicaoBuilder().setId(idContrato)
                                                         .setContratoFk(contratoFk)
                                                         .create();

        int qtdMedicoesAposExclusao = 1; // Nao desbloqueia documentos

        when(medicaoDAO.consultarMedicao(idMedicao)).thenReturn(medicao);
        when(medicaoDAO.consultarUltimaMedicao(contratoFk)).thenReturn(medicao);
        when(medicaoDAO.consultarQtdeMedicoesPorContrato(contratoFk)).thenReturn(qtdMedicoesAposExclusao);
        when(contratoDAO.consultarContrato(idContrato)).thenReturn(contrato);

        medicaoBC.excluirMedicao(idMedicao);

        verifyDao(times(1));
        verify(historicoBC, times(1)).inserir(any());
        verify(documentoComplementarBC, never()).desbloquearDocumentosComplementares(handle, idContrato);
    }

    @Test
    @MockUsuario(profile = CONCEDENTE, roles = { ADMINISTRADOR_SISTEMA, ADMINISTRADOR_SISTEMA_ORGAO_EXTERNO })
    void testExcluirMedicao_aceita_acumulada() {

        MedicaoBD medAcumulada1 = newMedicaoBuilder().setId(1L)
                                                     .setMedContrato(idContrato)
                                                     .comSituacao(ACT)
                                                     .setAgrupadora(idMedicao)
                                                     .create();

        MedicaoBD medAcumulada2 = newMedicaoBuilder().setId(2L)
                                                     .setMedContrato(idContrato)
                                                     .comSituacao(ACT)
                                                     .setAgrupadora(idMedicao)
                                                     .create();

        MedicaoBD medicao = newMedicaoBuilder().setId(idMedicao)
                                               .setMedContrato(idContrato)
                                               .comSituacao(ACT)
                                               .create();

        ContratoBD contrato = newContratoMedicaoBuilder().setId(idContrato)
                                                         .setContratoFk(contratoFk)
                                                         .create();

        int qtdMedicoesAposExclusao = 0; // Faz desbloqueio documentos

        when(medicaoDAO.consultarMedicao(idMedicao)).thenReturn(medicao);
        when(medicaoDAO.consultarUltimaMedicao(contratoFk)).thenReturn(medicao);
        when(medicaoDAO.listarMedicoesAcumuladas(idMedicao)).thenReturn(List.of(medAcumulada1, medAcumulada2));
        when(medicaoDAO.consultarQtdeMedicoesPorContrato(contratoFk)).thenReturn(qtdMedicoesAposExclusao);
        when(contratoDAO.consultarContrato(idContrato)).thenReturn(contrato);

        medicaoBC.excluirMedicao(idMedicao);

        verifyDao(times(3)); // para cada medicao
        verify(historicoBC, times(3)).inserir(any()); // para cada medicao
        verify(documentoComplementarBC, times(1)).desbloquearDocumentosComplementares(handle, idContrato);
    }

    private void verifyDao(VerificationMode mode) {
        verify(anexoDAO, mode).excluirAnexoPorIdMedicao(any());
        verify(observacaoDAO, mode).excluirObservacaoPorIdMedicao(any());
        verify(submetaDAO, mode).excluirSubmetaPorIdMedicao(any());
        verify(itemMedicaoDAO, mode).limparItemMedicaoPorIdMedicao(any());
        verify(itemMedicaoDAO, mode).excluirItemMedicaoValorBM(any());
        verify(medicaoDAO, mode).excluirMedicaoPorId(any());
    }
}
