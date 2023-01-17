package br.gov.planejamento.siconv.med.medicao.business.medicao;

import static br.gov.planejamento.siconv.med.contrato.entity.ModalidadeEnum.CONTRATO_REPASSE;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.MANDATARIA;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.PROPONENTE_CONVENENTE;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.AC;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.AT;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.ATD;
import static br.gov.planejamento.siconv.med.test.builder.ContratoMedicaoBuilder.newContratoMedicaoBuilder;
import static br.gov.planejamento.siconv.med.test.builder.ContratoSiconvBuilder.newContratoDTOBuilder;
import static br.gov.planejamento.siconv.med.test.builder.MedicaoBuilder.newMedicaoBuilder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
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

import br.gov.planejamento.siconv.med.contrato.business.ContratosBC;
import br.gov.planejamento.siconv.med.contrato.dao.ContratoDAO;
import br.gov.planejamento.siconv.med.contrato.entity.ContratoBD;
import br.gov.planejamento.siconv.med.contrato.entity.ModalidadeEnum;
import br.gov.planejamento.siconv.med.contrato.entity.dto.ContratoSiconvDTO;
import br.gov.planejamento.siconv.med.infra.message.MessageKey;
import br.gov.planejamento.siconv.med.infra.security.SecurityContext;
import br.gov.planejamento.siconv.med.infra.security.UsuarioLogado;
import br.gov.planejamento.siconv.med.medicao.business.HistoricoMedicaoBC;
import br.gov.planejamento.siconv.med.medicao.business.MedicaoBC;
import br.gov.planejamento.siconv.med.medicao.business.ObservacaoBC;
import br.gov.planejamento.siconv.med.medicao.business.SubmetaBC;
import br.gov.planejamento.siconv.med.medicao.dao.MedicaoDAO;
import br.gov.planejamento.siconv.med.medicao.dao.SubmetaDAO;
import br.gov.planejamento.siconv.med.medicao.entity.database.HistoricoMedicaoBD;
import br.gov.planejamento.siconv.med.medicao.entity.database.MedicaoBD;
import br.gov.planejamento.siconv.med.test.extension.BusinessControllerBaseTest;

class MedicaoBC_IniciarAnaliseTest extends BusinessControllerBaseTest {

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

	/* *************************************************************
	 *               INICIAR ANÁLISE 
	 * ************************************************************* */
	
	@Test
	void testIniciarAnaliseParametroIdMedicaoNulo() {

		Exception exception = assertThrows(NullPointerException.class,
				() -> medicaoBC.iniciarAnalise(null));

		assertEquals("Parâmetro idMedicao não pode ser nulo", exception.getMessage());
	}

	@Test
	void testIniciarAnaliseMedicaoNaoEncontrada() {

		Long idMedicao = 999L;

		assertThrowsMedicaoRestException(MessageKey.ERRO_MEDICAO_NAO_ENCONTRADA,
				() -> medicaoBC.iniciarAnalise(idMedicao));
	}
	
	@Test
	void testIniciarAnaliseSituacaoInvalida() {

		MedicaoBD medicao = newMedicaoBuilder().setId(1L)
				.comSituacao(AT).create();

		when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);

		assertThrowsMedicaoRestException(MessageKey.ERRO_SITUACAO_INVALIDA_PARA_INICIAR_ANALISE,
				() -> medicaoBC.iniciarAnalise(medicao.id));
	}
	

	@Test
	void testIniciarAnaliseMedicaoAgrupada() {

		MedicaoBD medicao = newMedicaoBuilder().setId(1L)
				.comSituacao(ATD)
				.setAgrupadora(2L).create();

		when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);

		assertThrowsMedicaoRestException(MessageKey.ERRO_INICIAR_ANALISE_MEDICAO_AGRUPADA,
				() -> medicaoBC.iniciarAnalise(medicao.id));
	}
	
	@Test
	void testIniciarAnaliseMedicao() {

		ContratoBD contratoMedicao = newContratoMedicaoBuilder().setId(1L).porEventos(true)
				.setContratoSiconv(7L).create();
		
		ContratoSiconvDTO contratoSiconvDTO = newContratoDTOBuilder()
				.setModalidade(CONTRATO_REPASSE)
				.setId(1L).create();
		
		Short nrSequencial = 1;
		
		MedicaoBD medicao = newMedicaoBuilder().setId(1L)
				.setMedContrato(contratoMedicao.id)
				.setNrSequencial(nrSequencial)
				.comSituacao(ATD).create();
		
		when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);
		when(contratoDao.consultarContrato(medicao.getIdContratoMedicao())).thenReturn(contratoMedicao);
		when(contratoBC.consultarContratoAssociadoMedicao(medicao.id)).thenReturn(contratoMedicao);
		when(contratoBC.consultarContratoPorId(contratoMedicao.contratoFk)).thenReturn(contratoSiconvDTO);
		when(securityContext.isUserInProfile(MANDATARIA)).thenReturn(true);

		medicaoBC.iniciarAnalise(medicao.id);

		verify(observacaoBC, times(1)).bloquearObservacao(handle, medicao.id, PROPONENTE_CONVENENTE);
		verify(medicaoDao, times(1)).alterar(medicaoCaptor.capture());
		verify(historicoMedicaoBC, times(1)).inserir(historicoCaptor.capture());
		
		assertEquals(AC, medicaoCaptor.getValue().getSituacao());
		assertEquals(medicao.nrSequencial, historicoCaptor.getValue().getNrSequencial());
		assertEquals(medicao.idContratoMedicao, historicoCaptor.getValue().getIdContratoMedicao());
		assertEquals(AC, historicoCaptor.getValue().getSituacao());

	}
	
	@Test
	void testIniciarAnaliseMedicaoAgrupadaAtestadas() {
		
		ContratoBD contratoMedicao = newContratoMedicaoBuilder().setId(1L).porEventos(true)
				.setContratoSiconv(7L).create();
		
		ContratoSiconvDTO contratoSiconvDTO = newContratoDTOBuilder()
				.setModalidade(ModalidadeEnum.fromCodigo(2))
				.setId(1L).create();
		
		Short nrSequencial = 2;
		
		MedicaoBD medicao = newMedicaoBuilder().setId(2L)
				.setMedContrato(contratoMedicao.id)
				.setNrSequencial(nrSequencial)
				.comSituacao(ATD).create();
		
		Short nrSequencialFilha = 1;
		
		MedicaoBD medicaoFilha = newMedicaoBuilder().setId(1L)
				.setMedContrato(contratoMedicao.id)
				.setNrSequencial(nrSequencialFilha)
				.setDtVistoriaExtra(LocalDate.now())
				.comSituacao(ATD).create();
		
		List<MedicaoBD> listaMedicoesAnteriores = new ArrayList<MedicaoBD>();
		listaMedicoesAnteriores.add(medicaoFilha);
		
		when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);
		when(contratoDao.consultarContrato(medicao.getIdContratoMedicao())).thenReturn(contratoMedicao);
		when(contratoBC.consultarContratoAssociadoMedicao(medicao.id)).thenReturn(contratoMedicao);
		when(contratoBC.consultarContratoPorId(contratoMedicao.contratoFk)).thenReturn(contratoSiconvDTO);
		when(securityContext.isUserInProfile(MANDATARIA)).thenReturn(true);
		when(medicaoDao.listarMedicoesAnterioresPorSituacao(
				medicao.getIdContratoMedicao(), medicao.getNrSequencial(), List.of(AC,
						ATD))).thenReturn(listaMedicoesAnteriores);

		medicaoBC.iniciarAnalise(medicao.id);

		// medicao agrupadora
		verify(observacaoBC, times(1)).bloquearObservacao(handle, medicao.id, PROPONENTE_CONVENENTE);
		
		// medicao filha
		verify(observacaoBC, times(1)).bloquearObservacaoMedicaoFilha(handle, medicaoFilha, PROPONENTE_CONVENENTE);
		
		verify(medicaoDao, times(2)).alterar(medicaoCaptor.capture());
		List<MedicaoBD> listaMedicaoBD = medicaoCaptor.getAllValues();

		verify(historicoMedicaoBC, times(2)).inserir(historicoCaptor.capture());
		List<HistoricoMedicaoBD> listaHistoricoMedicaoBD = historicoCaptor.getAllValues();
		
		// medicao filha
		assertEquals(AC, listaMedicaoBD.get(0).getSituacao());
		assertEquals(medicao.id, listaMedicaoBD.get(0).getIdMedicaoAgrupadora());
		assertNull(listaMedicaoBD.get(0).getDataVistoriaExtra());
		
		// medicao agrupadora
		assertEquals(AC, listaMedicaoBD.get(1).getSituacao());
		
		// historico filha
		assertEquals(medicaoFilha.nrSequencial, listaHistoricoMedicaoBD.get(0).getNrSequencial());
		assertEquals(medicaoFilha.idContratoMedicao, listaHistoricoMedicaoBD.get(0).getIdContratoMedicao());
		assertEquals(AC, listaHistoricoMedicaoBD.get(0).getSituacao());
		
		// historico
		assertEquals(medicao.nrSequencial, listaHistoricoMedicaoBD.get(1).getNrSequencial());
		assertEquals(medicao.idContratoMedicao, listaHistoricoMedicaoBD.get(1).getIdContratoMedicao());
		assertEquals(AC, listaHistoricoMedicaoBD.get(1).getSituacao());

	}
	
	@Test
	void testIniciarAnaliseMedicaoAgrupadaEmAnalise() {

		ContratoBD contratoMedicao = newContratoMedicaoBuilder().setId(1L).porEventos(true)
				.setContratoSiconv(7L).create();
		
		ContratoSiconvDTO contratoSiconvDTO = newContratoDTOBuilder()
				.setModalidade(ModalidadeEnum.fromCodigo(2))
				.setId(1L).create();

		Short nrSequencial = 2;
		
		MedicaoBD medicao = newMedicaoBuilder().setId(2L)
				.setMedContrato(contratoMedicao.id)
				.setNrSequencial(nrSequencial)
				.comSituacao(ATD).create();
		
		MedicaoBD medicaoFilha = newMedicaoBuilder().setId(1L)
				.setDtVistoriaExtra(LocalDate.now())
				.setMedContrato(contratoMedicao.id)
				.setAgrupadora(medicao.getId())
				.comSituacao(AC).create();
		
		List<MedicaoBD> listaMedicoesAnteriores = new ArrayList<MedicaoBD>();
		listaMedicoesAnteriores.add(medicaoFilha);
				
		when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);
		when(contratoDao.consultarContrato(medicao.getIdContratoMedicao())).thenReturn(contratoMedicao);
		when(contratoBC.consultarContratoAssociadoMedicao(medicao.id)).thenReturn(contratoMedicao);
		when(contratoBC.consultarContratoPorId(contratoMedicao.contratoFk)).thenReturn(contratoSiconvDTO);
		when(securityContext.isUserInProfile(MANDATARIA)).thenReturn(true);
		
		when(medicaoDao.listarMedicoesAnterioresPorSituacao(
				medicao.getIdContratoMedicao(), medicao.getNrSequencial(), List.of(AC,
						ATD))).thenReturn(listaMedicoesAnteriores);
		
		medicaoBC.iniciarAnalise(medicao.id);

		// bloquear observações
		verify(observacaoBC, times(1)).bloquearObservacao(handle, medicao.id, PROPONENTE_CONVENENTE);
		
		// move observações da medicao filha para pai
		verify(observacaoBC, times(1)).moverObservacaoMedicaoAgrupadaParaMedicaoAtualConcedenteMandataria(handle, medicaoFilha);
		
		// apaga marcacões das submetas
		verify(submetaBC, times(1)).apagarMarcacoesConcedenteSubmetasMedicao(handle, medicaoFilha, contratoMedicao);
		
		// altera medições
		verify(medicaoDao, times(2)).alterar(medicaoCaptor.capture());
		List<MedicaoBD> listaMedicaoBD = medicaoCaptor.getAllValues();
		
		// inclui histórico
		verify(historicoMedicaoBC, times(1)).inserir(historicoCaptor.capture());
		
		// medicao filha
		assertEquals(medicao.id, listaMedicaoBD.get(0).getIdMedicaoAgrupadora());
		assertNull(listaMedicaoBD.get(0).getDataVistoriaExtra());
		
		// medicao agrupadora
		assertEquals(AC, listaMedicaoBD.get(1).getSituacao());
		
		// historico agrupadora
		assertEquals(medicao.nrSequencial, historicoCaptor.getValue().getNrSequencial());
		assertEquals(medicao.idContratoMedicao, historicoCaptor.getValue().getIdContratoMedicao());
		assertEquals(AC, historicoCaptor.getValue().getSituacao());

	}

	@Test
	void testIniciarAnaliseMedicaoAgrupada_indicadorPermiteComplementacao() {

		ContratoBD contratoMedicao = newContratoMedicaoBuilder().setId(1L).create();

		MedicaoBD medicao = newMedicaoBuilder()
								.setId(3L)
								.setNrSequencial((short) 3)
								.setMedContrato(contratoMedicao.getId())
								.comSituacao(ATD)
								.setPermiteComplementacaoValor(true)
								.create();

		MedicaoBD medicaoFilha2 = newMedicaoBuilder()
				.setId(2L)
				.setNrSequencial((short) 2)
				.setMedContrato(contratoMedicao.getId())
				.setAgrupadora(medicao.getId())
				.comSituacao(ATD)
				.setPermiteComplementacaoValor(true)
				.create();

		MedicaoBD medicaoFilha1 = newMedicaoBuilder()
				.setId(1L)
				.setNrSequencial((short) 1)
				.setMedContrato(contratoMedicao.getId())
				.setAgrupadora(medicao.getId())
				.comSituacao(ATD)
				.setPermiteComplementacaoValor(true)
				.create();

		when(medicaoDao.consultarMedicao(medicao.getId())).thenReturn(medicao);
		when(medicaoDao.listarMedicoesAnterioresPorSituacao(medicao.getIdContratoMedicao(), medicao.getNrSequencial(),
				List.of(AC, ATD))).thenReturn(List.of(medicaoFilha1, medicaoFilha2));
		when(contratoDao.consultarContrato(medicao.getIdContratoMedicao())).thenReturn(contratoMedicao);

		medicaoBC.iniciarAnalise(medicao.getId());

		verify(medicaoDao, times(3)).alterar(medicaoCaptor.capture());

		medicaoCaptor.getAllValues().forEach(med -> {
			assertEquals(AC, med.getSituacao());
			assertNull(med.getPermiteComplementacaoValor());
		});
	}
}
