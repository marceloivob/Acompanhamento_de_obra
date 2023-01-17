package br.gov.planejamento.siconv.med.medicao.business.medicao;

import static br.gov.planejamento.siconv.med.infra.message.MessageKey.ERRO_CANCELAR_ACEITE_MEDICAO_AGRUPADA;
import static br.gov.planejamento.siconv.med.infra.message.MessageKey.ERRO_CANCELAR_ACEITE_SITUACAO_NAO_PREVISTA;
import static br.gov.planejamento.siconv.med.infra.message.MessageKey.ERRO_MEDICAO_NAO_ENCONTRADA;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.CONCEDENTE;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.AC;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.ACT;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.EM;
import static br.gov.planejamento.siconv.med.test.builder.MedicaoBuilder.newMedicaoBuilder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import br.gov.planejamento.siconv.med.contrato.business.ContratosBC;
import br.gov.planejamento.siconv.med.contrato.dao.ContratoDAO;
import br.gov.planejamento.siconv.med.infra.security.SecurityContext;
import br.gov.planejamento.siconv.med.infra.security.UsuarioLogado;
import br.gov.planejamento.siconv.med.medicao.business.HistoricoMedicaoBC;
import br.gov.planejamento.siconv.med.medicao.business.MedicaoBC;
import br.gov.planejamento.siconv.med.medicao.business.ObservacaoBC;
import br.gov.planejamento.siconv.med.medicao.business.SubmetaBC;
import br.gov.planejamento.siconv.med.medicao.dao.MedicaoDAO;
import br.gov.planejamento.siconv.med.medicao.dao.SubmetaDAO;
import br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum;
import br.gov.planejamento.siconv.med.medicao.entity.database.HistoricoMedicaoBD;
import br.gov.planejamento.siconv.med.medicao.entity.database.MedicaoBD;
import br.gov.planejamento.siconv.med.test.extension.BusinessControllerBaseTest;

class MedicaoBC_CancelarAceiteTest extends BusinessControllerBaseTest {

	final String CPF_RESPONSAVEL = "11111111111";
	final UsuarioLogado usuarioLogado = mock(UsuarioLogado.class);
	
	@Mock
	private SecurityContext securityContext;
	
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
	private ContratosBC contratoBC;

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
	void testCancelarAceite_MedicaoNaoEncontrada() {
		Long idMedicao = 999L;

		assertThrowsMedicaoRestException(ERRO_MEDICAO_NAO_ENCONTRADA,
				() -> medicaoBC.cancelarAceite(idMedicao));
	}
	
	@Test
	void testCancelarAceite_MedicaoSituacaoNaoPermitida() {
		
		MedicaoBD medicao = newMedicaoBuilder().setId(1L)
				.comSituacao(EM).create();

		when(securityContext.isUserInProfile(CONCEDENTE)).thenReturn(true);
		when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);

		assertThrowsMedicaoRestException(ERRO_CANCELAR_ACEITE_SITUACAO_NAO_PREVISTA,
				() -> medicaoBC.cancelarAceite(medicao.id));
	}
	
	@Test
	void testCancelarAceite_MedicaoAgrupada() {
		
		MedicaoBD medicao = newMedicaoBuilder().setId(1L)
				.comSituacao(ACT)
				.setAgrupadora(2L).create();

		when(securityContext.isUserInProfile(CONCEDENTE)).thenReturn(true);
		when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);

		assertThrowsMedicaoRestException(ERRO_CANCELAR_ACEITE_MEDICAO_AGRUPADA,
				() -> medicaoBC.cancelarAceite(medicao.id));
	}
	
	@Test
	void testCancelarAceite_MedicaoPosteriorEmAnalise() {
		
		MedicaoBD medicao = newMedicaoBuilder().setId(1L)
				.comSituacao(ACT)
				.create();

		when(securityContext.isUserInProfile(CONCEDENTE)).thenReturn(true);
		when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);
		when(medicaoDao.permiteCancelarAceite(medicao.id)).thenReturn(false);

		assertThrowsMedicaoRestException(ERRO_CANCELAR_ACEITE_SITUACAO_NAO_PREVISTA,
				() -> medicaoBC.cancelarAceite(medicao.id));
	}
	
	@Test
	void testCancelarAceite_MedicaoAgrupadora() {
		Short nrSequencial = 2;
		Short nrSequencialFilha = 1;
		
		MedicaoBD medicaoPai = newMedicaoBuilder().setId(2L)
				.setNrSequencial(nrSequencial)
				.setMedContrato(1L)
				.comSituacao(ACT).create();
		
		MedicaoBD medicaoFilha = newMedicaoBuilder().setId(1L)
				.setMedContrato(1L)
				.setNrSequencial(nrSequencialFilha)
				.comSituacao(ACT).create();
		
		List<MedicaoBD> listaMedicoesAcumuladas = new ArrayList<MedicaoBD>();
		listaMedicoesAcumuladas.add(medicaoFilha);
		
		HistoricoMedicaoBD penultimoHistorico = new HistoricoMedicaoBD();
		penultimoHistorico.setSituacao(AC);

		when(securityContext.isUserInProfile(CONCEDENTE)).thenReturn(true);
		when(medicaoDao.consultarMedicao(medicaoPai.id)).thenReturn(medicaoPai);
		when(medicaoDao.permiteCancelarAceite(medicaoPai.id)).thenReturn(true);
		when(historicoMedicaoBC.recuperarPenultimoHistoricoPorMedicaoContrato(medicaoPai.idContratoMedicao,medicaoPai.nrSequencial)).thenReturn(Optional.of(penultimoHistorico));
		when(medicaoDao.listarMedicoesAcumuladas(medicaoPai.id)).thenReturn(listaMedicoesAcumuladas);
		
		medicaoBC.cancelarAceite(medicaoPai.id);
		
		verify(medicaoDao, times(2)).alterar(medicaoCaptor.capture());
		verify(historicoMedicaoBC, times(2)).inserir(historicoCaptor.capture());
		
		assertEquals(AC, medicaoCaptor.getAllValues().get(0).getSituacao());
		assertEquals(AC, medicaoCaptor.getAllValues().get(1).getSituacao());
		assertEquals(AC, historicoCaptor.getAllValues().get(0).getSituacao());
		assertEquals(AC, historicoCaptor.getAllValues().get(1).getSituacao());
	}
	
	@Test
	void testCancelarAceite() {
		Short nrSequencial = 1;
		
		MedicaoBD medicaoPai = newMedicaoBuilder().setId(2L)
				.setNrSequencial(nrSequencial)
				.setMedContrato(1L)
				.comSituacao(SituacaoMedicaoEnum.ACT)
				.create();
		
		HistoricoMedicaoBD penultimoHistorico = new HistoricoMedicaoBD();
		penultimoHistorico.setSituacao(AC);

		when(securityContext.isUserInProfile(CONCEDENTE)).thenReturn(true);
		when(medicaoDao.consultarMedicao(medicaoPai.id)).thenReturn(medicaoPai);
		when(medicaoDao.permiteCancelarAceite(medicaoPai.id)).thenReturn(true);
		when(historicoMedicaoBC.recuperarPenultimoHistoricoPorMedicaoContrato(medicaoPai.idContratoMedicao,medicaoPai.nrSequencial)).thenReturn(Optional.of(penultimoHistorico));
		when(medicaoDao.listarMedicoesAcumuladas(medicaoPai.id)).thenReturn(new ArrayList<>());
		
		medicaoBC.cancelarAceite(medicaoPai.id);
		
		verify(medicaoDao, times(1)).alterar(medicaoCaptor.capture());
		verify(historicoMedicaoBC, times(1)).inserir(historicoCaptor.capture());
		
		assertEquals(AC, medicaoCaptor.getValue().getSituacao());
		assertEquals(AC, historicoCaptor.getValue().getSituacao());
	}
	
}
