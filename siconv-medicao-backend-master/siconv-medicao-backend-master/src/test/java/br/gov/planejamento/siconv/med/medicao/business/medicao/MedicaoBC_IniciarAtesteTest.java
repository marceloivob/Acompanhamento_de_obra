package br.gov.planejamento.siconv.med.medicao.business.medicao;

import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.EMPRESA;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.PROPONENTE_CONVENENTE;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.AC;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.AT;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.CC;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.EC;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.ECC;
import static br.gov.planejamento.siconv.med.test.builder.ContratoMedicaoBuilder.newContratoMedicaoBuilder;
import static br.gov.planejamento.siconv.med.test.builder.MedicaoBuilder.newMedicaoBuilder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import br.gov.planejamento.siconv.med.contrato.dao.ContratoDAO;
import br.gov.planejamento.siconv.med.contrato.entity.ContratoBD;
import br.gov.planejamento.siconv.med.infra.message.MessageKey;
import br.gov.planejamento.siconv.med.medicao.business.HistoricoMedicaoBC;
import br.gov.planejamento.siconv.med.medicao.business.MedicaoBC;
import br.gov.planejamento.siconv.med.medicao.business.ObservacaoBC;
import br.gov.planejamento.siconv.med.medicao.business.SubmetaBC;
import br.gov.planejamento.siconv.med.medicao.dao.MedicaoDAO;
import br.gov.planejamento.siconv.med.medicao.dao.SubmetaDAO;
import br.gov.planejamento.siconv.med.medicao.entity.database.HistoricoMedicaoBD;
import br.gov.planejamento.siconv.med.medicao.entity.database.MedicaoBD;
import br.gov.planejamento.siconv.med.test.extension.BusinessControllerBaseTest;
import br.gov.planejamento.siconv.med.test.extension.MockUsuario;

class MedicaoBC_IniciarAtesteTest extends BusinessControllerBaseTest {

	@Mock
	private MedicaoDAO medicaoDao;

	@Mock
	private ContratoDAO contratoDao;

	@Mock
	private SubmetaDAO submetaDao;

	@Mock
	private HistoricoMedicaoBC historicoMedicaoBC;

	@Mock
	private ObservacaoBC observacaoBC;

	@Mock
	private SubmetaBC submetaBC;

	@InjectMocks
	private MedicaoBC medicaoBC;

	@Captor
	private ArgumentCaptor<MedicaoBD> medicaoCaptor;

	@Captor
	private ArgumentCaptor<HistoricoMedicaoBD> historicoCaptor;

	@BeforeEach
	void setup() throws Exception {
		setupDaoMock(MedicaoDAO.class, medicaoDao);
		setupDaoMock(SubmetaDAO.class, submetaDao);
		setupDaoMock(ContratoDAO.class, contratoDao);
	}

	@Test
	void testIniciarAteste_parametroIdMedicaoNulo() {

		Exception exception = assertThrows(NullPointerException.class,
				() -> medicaoBC.iniciarAteste(null));

		assertEquals("Parâmetro idMedicao não pode ser nulo", exception.getMessage());
	}

	@Test
	@MockUsuario(profile = PROPONENTE_CONVENENTE)
	void testIniciarAteste_situacaoInvalidaParaIniciarAteste() {

		MedicaoBD medicao = newMedicaoBuilder().setId(1L).setMedContrato(1L).comSituacao(AC).create();

		when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);

		assertThrowsMedicaoRestException(MessageKey.ERRO_SITUACAO_INVALIDA_PARA_INICIAR_ATESTE,
				() -> medicaoBC.iniciarAteste(medicao.id));
	}
	

	@Test
	void testIniciarAtesteMedicaoAgrupada() {

		MedicaoBD medicao = newMedicaoBuilder().setId(1L)
				.comSituacao(EC)
				.setAgrupadora(2L).create();

		when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);

		assertThrowsMedicaoRestException(MessageKey.ERRO_INICIAR_ATESTE_MEDICAO_AGRUPADA,
				() -> medicaoBC.iniciarAteste(medicao.id));
	}
	
	@Test
	@MockUsuario(profile = PROPONENTE_CONVENENTE)
	void testIniciarAteste_possuiMedicaoEmComplementacao() {

		ContratoBD contratoMedicao = newContratoMedicaoBuilder().setId(1L).porEventos(true)
				.setContratoSiconv(7L).create();
				
		Short nrSequencial = 2;
		
		MedicaoBD medicao = newMedicaoBuilder().setId(2L)
				.setMedContrato(contratoMedicao.id)
				.setNrSequencial(nrSequencial)
				.comSituacao(EC).create();
		
		Short nrSequencialAnterior = 1;
		
		MedicaoBD medicaoAnterior = newMedicaoBuilder().setId(1L)
				.setMedContrato(contratoMedicao.id)
				.setNrSequencial(nrSequencialAnterior)
				.setAgrupadora(null)
				.setPermiteComplementacaoValor(true)
				.comSituacao(ECC).create();
		
		List<MedicaoBD> listaMedicoesAnteriores = new ArrayList<MedicaoBD>();
		listaMedicoesAnteriores.add(medicaoAnterior);
		
		when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);
		when(medicaoDao.listarMedicoesAnterioresPorSituacao(
				medicao.getIdContratoMedicao(), medicao.getNrSequencial(), List.of(EC, AT, ECC, CC)))
				.thenReturn(listaMedicoesAnteriores);

		assertThrowsMedicaoRestException(MessageKey.ERRO_CONTRATO_POSSUI_MEDICAO_COMPL_CONV_OU_ENVIADA_COMPL_CONV,
				() -> medicaoBC.iniciarAteste(medicao.id));
	}
	
	@Test
	@MockUsuario(profile = PROPONENTE_CONVENENTE)
	void testIniciarAteste_iniciarAteste() {

		ContratoBD contratoMedicao = newContratoMedicaoBuilder().setId(1L).porEventos(true)
				.setContratoSiconv(7L).create();
		
		Short nrSequencial = 2;
		
		MedicaoBD medicao = newMedicaoBuilder().setId(2L)
				.setMedContrato(contratoMedicao.id)
				.setNrSequencial(nrSequencial)
				.comSituacao(EC).create();
		
		Short nrSequencialAnterior = 1;
		
		MedicaoBD medicaoAnterior = newMedicaoBuilder().setId(1L)
				.setMedContrato(contratoMedicao.id)
				.setNrSequencial(nrSequencialAnterior)
				.setDtVistoriaExtra(LocalDate.now())
				.comSituacao(AC).create();
		
		List<MedicaoBD> listaMedicoesAnteriores = new ArrayList<MedicaoBD>();
		listaMedicoesAnteriores.add(medicaoAnterior);
		
		when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);
		when(contratoDao.consultarContrato(medicao.getIdContratoMedicao())).thenReturn(contratoMedicao);
		
		medicaoBC.iniciarAteste(medicao.id);
		
		verify(observacaoBC, times(1)).bloquearObservacao(handle, medicao.id, EMPRESA);
		verify(medicaoDao, times(1)).alterar(medicaoCaptor.capture());
		verify(historicoMedicaoBC, times(1)).inserir(historicoCaptor.capture());
		
		assertEquals(AT, medicaoCaptor.getValue().getSituacao());
		assertEquals(medicao.nrSequencial, historicoCaptor.getValue().getNrSequencial());
		assertEquals(medicao.idContratoMedicao, historicoCaptor.getValue().getIdContratoMedicao());
		assertEquals(AT, historicoCaptor.getValue().getSituacao());

	}
	
	@Test
	@MockUsuario(profile = PROPONENTE_CONVENENTE)
	void testIniciarAteste_iniciarAteste_Agrupado() {

		ContratoBD contratoMedicao = newContratoMedicaoBuilder().setId(1L).porEventos(true)
				.setContratoSiconv(7L).create();
		
		Short nrSequencial = 3;
		
		MedicaoBD medicao = newMedicaoBuilder().setId(3L)
				.setMedContrato(contratoMedicao.id)
				.setNrSequencial(nrSequencial)
				.comSituacao(EC).create();
		
		Short nrSequencialAnterior2 = 2;
		
		MedicaoBD medicaoFilha2 = newMedicaoBuilder().setId(2L)
				.setMedContrato(contratoMedicao.id)
				.setNrSequencial(nrSequencialAnterior2)
				.comSituacao(EC).create();
		
		Short nrSequencialAnterior1 = 1;
		
		MedicaoBD medicaoFilha1 = newMedicaoBuilder().setId(1L)
				.setMedContrato(contratoMedicao.id)
				.setNrSequencial(nrSequencialAnterior1)
				.comSituacao(AT).create();
		
		List<MedicaoBD> listaMedicoes = new ArrayList<MedicaoBD>();
		listaMedicoes.add(medicaoFilha2);
		listaMedicoes.add(medicaoFilha1);
		
		when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);
		when(medicaoDao.listarMedicoesAnterioresPorSituacao(
				medicao.getIdContratoMedicao(), medicao.getNrSequencial(), List.of(EC,AT,ECC,CC))).thenReturn(listaMedicoes);
		when(contratoDao.consultarContrato(medicao.getIdContratoMedicao())).thenReturn(contratoMedicao);
		
		medicaoBC.iniciarAteste(medicao.id);
		
		verify(observacaoBC, times(1)).bloquearObservacao(handle, medicao.id, EMPRESA);
		verify(medicaoDao, times(3)).alterar(medicaoCaptor.capture());
		verify(historicoMedicaoBC, times(2)).inserir(historicoCaptor.capture());
		
		assertEquals(AT, medicaoCaptor.getValue().getSituacao());
		assertEquals(medicao.nrSequencial, historicoCaptor.getValue().getNrSequencial());
		assertEquals(medicao.idContratoMedicao, historicoCaptor.getValue().getIdContratoMedicao());
		assertEquals(AT, historicoCaptor.getValue().getSituacao());

	}
	
}
