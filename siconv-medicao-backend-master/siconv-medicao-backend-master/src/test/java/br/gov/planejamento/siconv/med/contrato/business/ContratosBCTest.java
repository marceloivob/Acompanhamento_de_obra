package br.gov.planejamento.siconv.med.contrato.business;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import br.gov.planejamento.siconv.med.contrato.dao.ContratoDAO;
import br.gov.planejamento.siconv.med.contrato.entity.ContratoBD;
import br.gov.planejamento.siconv.med.contrato.entity.dto.ContratoSiconvDTO;
import br.gov.planejamento.siconv.med.infra.exception.MedicaoRestException;
import br.gov.planejamento.siconv.med.infra.message.Message;
import br.gov.planejamento.siconv.med.infra.message.MessageKey;
import br.gov.planejamento.siconv.med.medicao.dao.HistoricoMedicaoDAO;
import br.gov.planejamento.siconv.med.medicao.dao.ItemMedicaoDAO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.EventoFrenteObraTotalizadoDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.ServicoFrenteObraTotalizadoDTO;
import br.gov.planejamento.siconv.med.test.builder.ContratoMedicaoBuilder;
import br.gov.planejamento.siconv.med.test.extension.BusinessControllerBaseTest;

class ContratosBCTest  extends BusinessControllerBaseTest {

	@InjectMocks
	private ContratosBC contratosBC;
	
	@Mock
	private ItemMedicaoBC itemMedicaoBC;

	@Mock
	private ContratoDAO contratoDAO;
	
	@Mock
	private ItemMedicaoDAO itemMedicaoDAO;
	
	@Mock
	private HistoricoMedicaoDAO historicoMedicaoDAO;

	@BeforeEach
	void setup() throws Exception {
		setupDaoMock(ContratoDAO.class, contratoDAO);
		setupDaoMock(ItemMedicaoDAO.class, itemMedicaoDAO);
		setupDaoMock(HistoricoMedicaoDAO.class, historicoMedicaoDAO);
	}
	
	@Test
	void testIncluirContrato_ItemMedicao_ItemMedicaoInexistente() throws Exception {

		ContratoSiconvDTO contrato = new ContratoSiconvDTO();
		contrato.setId(1L);
		contrato.setInAcompEvento(Boolean.TRUE);
		
		List<EventoFrenteObraTotalizadoDTO> listaEventoFrenteObraVrplDTO = new ArrayList<>();
		
		when(itemMedicaoBC.getEventoFO(contrato.getId())).thenReturn(listaEventoFrenteObraVrplDTO);
		when(handle.inTransaction(Mockito.any())).thenCallRealMethod();

		assertThrowsMedicaoRestException(MessageKey.ITENS_MEDICOES_INEXISTENTES,
				() -> contratosBC.incluir(contrato, handle));
	}
	
	@Test
	void testIncluirContrato_ItemMedicao() throws Exception {

		ContratoBD contratoBD = new ContratoBD(1L, false, "111111111111111", 1L, true);
		
		ContratoSiconvDTO contrato = new ContratoSiconvDTO();
		contrato.setId(1L);
		contrato.setInAcompEvento(Boolean.TRUE);
		contrato.setInSocial(Boolean.FALSE);
		contrato.setCnpj("111111111111111");
		contrato.setPropostaFk(1L);
		
		List<EventoFrenteObraTotalizadoDTO> listaEventoFrenteObraVrplDTO = new ArrayList<>();
		EventoFrenteObraTotalizadoDTO evento = new EventoFrenteObraTotalizadoDTO();
		listaEventoFrenteObraVrplDTO.add(evento);
		
		when(itemMedicaoBC.getEventoFO(contrato.getId())).thenReturn(listaEventoFrenteObraVrplDTO);
		when(handle.inTransaction(Mockito.any())).thenCallRealMethod();
		when(handle.attach(ContratoDAO.class).inserir(Mockito.any())).thenReturn(contratoBD);
		
		contratosBC.incluir(contrato, handle);
		
		verify(contratoDAO, times(1)).inserir(contratoBD);
		verify(itemMedicaoDAO, times(1)).inserir(Mockito.any());
	}
	
	@Test
	void testIncluirContrato_ItemMedicao_BM_ItemMedicaoInexistente() throws Exception {

		ContratoSiconvDTO contrato = new ContratoSiconvDTO();
		contrato.setId(1L);
		contrato.setInAcompEvento(Boolean.FALSE);
		
		List<ServicoFrenteObraTotalizadoDTO> listaMacroServicoFrenteObraVrplDTO = new ArrayList<>();
		
		when(itemMedicaoBC.getMacroServicoFO(contrato.getId())).thenReturn(listaMacroServicoFrenteObraVrplDTO);
		when(handle.inTransaction(Mockito.any())).thenCallRealMethod();

		assertThrowsMedicaoRestException(MessageKey.ITENS_MEDICOES_BM_INEXISTENTES,
				() -> contratosBC.incluir(contrato, handle));
	}
	
	@Test
	void testIncluirContrato_ItemMedicao_BM() throws Exception {

		ContratoBD contratoBD = new ContratoBD(1L, false, "111111111111111", 1L, false);
		
		ContratoSiconvDTO contrato = new ContratoSiconvDTO();
		contrato.setId(1L);
		contrato.setInAcompEvento(Boolean.FALSE);
		contrato.setInSocial(Boolean.FALSE);
		contrato.setCnpj("111111111111111");
		contrato.setPropostaFk(1L);
		
		List<ServicoFrenteObraTotalizadoDTO> listaMacroServicoFrenteObraVrplDTO = new ArrayList<>();
		ServicoFrenteObraTotalizadoDTO servico = new ServicoFrenteObraTotalizadoDTO();
		listaMacroServicoFrenteObraVrplDTO.add(servico);
		
		when(itemMedicaoBC.getMacroServicoFO(contrato.getId())).thenReturn(listaMacroServicoFrenteObraVrplDTO);
		when(handle.inTransaction(Mockito.any())).thenCallRealMethod();
		when(handle.attach(ContratoDAO.class).inserir(Mockito.any())).thenReturn(contratoBD);
		
		contratosBC.incluir(contrato, handle);
		
		verify(contratoDAO, times(1)).inserir(contratoBD);
		verify(itemMedicaoDAO, times(1)).inserirItemBM(Mockito.any());
	}
	
	@Test
	void testExcluirContrato_contratoInexistente() throws Exception {
		
		assertThrowsMedicaoRestException(MessageKey.CONTRATO_INEXISTENTE, () -> contratosBC.excluirEstruturaContrato(null, handle));
	}
	
	
	@Test
	void testExcluirContrato_BM() throws Exception {

		Long idContrato = Long.valueOf(1);
		
		ContratoMedicaoBuilder contratoBuilder = ContratoMedicaoBuilder.newContratoMedicaoBuilder();
		contratoBuilder.
		setId(idContrato).
		setAcompanhadoPorEventos (false);
		ContratoBD contratoBD = contratoBuilder.create();

		doReturn(contratoBD).when (contratoDAO).consultarContrato(idContrato);
		
		contratosBC.excluirEstruturaContrato(contratoBD.getId(), handle);
		verify(itemMedicaoDAO, times(1)).excluirItemMedicaoBMPorContrato(contratoBD.getId());
		verify(historicoMedicaoDAO, times(1)).excluir(contratoBD.getId());
		verify(contratoDAO, times(1)).excluir(contratoBD.getId());
	}
	
	@Test
	void testExcluirContrato_PLE() throws Exception {

		Long idContrato = Long.valueOf(1);
		
		ContratoMedicaoBuilder contratoBuilder = ContratoMedicaoBuilder.newContratoMedicaoBuilder();
		contratoBuilder.
		setId(idContrato).
		setAcompanhadoPorEventos (true);
		
		ContratoBD contratoBD = contratoBuilder.create();

		doReturn(contratoBD).when (contratoDAO).consultarContrato(idContrato);
		
		contratosBC.excluirEstruturaContrato(contratoBD.getId(), handle);
		verify(itemMedicaoDAO, times(1)).excluirItemMedicaoPLEPorContrato(contratoBD.getId());
		verify(historicoMedicaoDAO, times(1)).excluir(contratoBD.getId());
		verify(contratoDAO, times(1)).excluir(contratoBD.getId());
	}
	
	protected void assertThrowsMedicaoRestException(MessageKey expectedMessageKey, Executable executable) {

		MedicaoRestException exception = assertThrows(MedicaoRestException.class, executable);

		exception.getMessages().stream().map(Message::getKey).findFirst().ifPresentOrElse(
				actualMessageKey -> assertEquals(expectedMessageKey, actualMessageKey),
				() -> fail(format("A messageKey esperada era %s, mas nenhuma foi obtida", expectedMessageKey)));
	}

}
