package br.gov.planejamento.siconv.med.medicao.business;

import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import br.gov.planejamento.siconv.med.infra.security.domain.Profile;
import br.gov.planejamento.siconv.med.integration.UsuarioConsumer;
import br.gov.planejamento.siconv.med.medicao.business.HistoricoMedicaoBC;
import br.gov.planejamento.siconv.med.medicao.business.PerfilHelper;
import br.gov.planejamento.siconv.med.medicao.dao.HistoricoMedicaoDAO;
import br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum;
import br.gov.planejamento.siconv.med.medicao.entity.database.HistoricoMedicaoBD;
import br.gov.planejamento.siconv.med.medicao.entity.dto.HistoricoMedicaoDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.PerfilEnum;
import br.gov.planejamento.siconv.med.test.extension.BusinessControllerBaseTest;
import br.gov.planejamento.siconv.med.test.extension.MockUsuario;

class HistoricoMedicaoBCTest extends BusinessControllerBaseTest {

	@Mock
	private HistoricoMedicaoDAO historicoDAO;

	@Mock
	private UsuarioConsumer usuarioConsumer;

	@Mock
	private PerfilHelper perfilHelper;

	@InjectMocks
	private HistoricoMedicaoBC historicoBC;

	private static final Long ID_CONTRATO = 1L;
	private static final Short NR_SEQUENCIAL_MEDICAO = 1;

	@BeforeEach
	void setup() throws Exception {
		setupDaoMock(HistoricoMedicaoDAO.class, historicoDAO);
	}

	@MockUsuario(profile = Profile.EMPRESA)
	@Test
	void testInserirHistorico() {

		HistoricoMedicaoBD historico = new HistoricoMedicaoBD();

		when(perfilHelper.getPerfilUsuarioLogado()).thenReturn(PerfilEnum.EMP);

		historicoBC.inserir(historico);

		verify(historicoDAO, times(1)).inserir(historico);
		assertEquals(PerfilEnum.EMP, historico.getInPerfilResponsavel());
		assertEquals("11111111111", historico.getNrCpfResponsavel());
	}

	@Test
	void testListarHistorico() {

		String nrCpfUsuarioEmpresa = "11111111111";
		String nomeUsuarioEmpresa = "Nome usuário Empresa";

		String nrCpfUsuarioConvenente = "22222222222";
		String nomeUsuarioConvenente = "Nome usuário Convenente";

		String nrCpfAdministrador = "33333333333";

		HistoricoMedicaoDTO hist1 = buildDto(nrCpfUsuarioEmpresa, PerfilEnum.EMP);
		HistoricoMedicaoDTO hist2 = buildDto(nrCpfUsuarioConvenente, PerfilEnum.CVE);
		HistoricoMedicaoDTO hist3 = buildDto(nrCpfAdministrador, PerfilEnum.ADM);

		when(historicoDAO.recuperarHistoricoMedicaoPorContrato(ID_CONTRATO)).thenReturn(List.of(hist1, hist2, hist3));
		when(usuarioConsumer.getNomeUsuario(nrCpfUsuarioEmpresa, PerfilEnum.EMP, true)).thenReturn(nomeUsuarioEmpresa);
		when(usuarioConsumer.getNomeUsuario(nrCpfUsuarioConvenente, PerfilEnum.CVE, true))
				.thenReturn(nomeUsuarioConvenente);

		List<HistoricoMedicaoDTO> listaHistorico = historicoBC.buscarHistoricosMedicao(ID_CONTRATO);

		assertThat(listaHistorico, hasSize(3));

		assertEquals(PerfilEnum.EMP, listaHistorico.get(0).getInPerfilResponsavel());
		assertEquals(nrCpfUsuarioEmpresa, listaHistorico.get(0).getNrCpfResponsavel());
		assertEquals(nomeUsuarioEmpresa, listaHistorico.get(0).getNomeResponsavel());

		assertEquals(PerfilEnum.CVE, listaHistorico.get(1).getInPerfilResponsavel());
		assertEquals(nrCpfUsuarioConvenente, listaHistorico.get(1).getNrCpfResponsavel());
		assertEquals(nomeUsuarioConvenente, listaHistorico.get(1).getNomeResponsavel());

		assertEquals(PerfilEnum.ADM, listaHistorico.get(2).getInPerfilResponsavel());
		assertNull(listaHistorico.get(2).getNrCpfResponsavel());
		assertEquals("ADMINISTRADOR DO SISTEMA", listaHistorico.get(2).getNomeResponsavel());
	}

	@Test
	void testRecuperarPenultimoHistorico_nulo() {

		when(historicoDAO.recuperarHistoricoMedicao(ID_CONTRATO, NR_SEQUENCIAL_MEDICAO)).thenReturn(null);

		Optional<HistoricoMedicaoBD> penultimo = historicoBC.recuperarPenultimoHistoricoPorMedicaoContrato(ID_CONTRATO,
				NR_SEQUENCIAL_MEDICAO);

		assertTrue(penultimo.isEmpty());
	}

	@Test
	void testRecuperarPenultimoHistorico_inexistente() {

		when(historicoDAO.recuperarHistoricoMedicao(ID_CONTRATO, NR_SEQUENCIAL_MEDICAO)).thenReturn(emptyList());

		Optional<HistoricoMedicaoBD> penultimo = historicoBC.recuperarPenultimoHistoricoPorMedicaoContrato(ID_CONTRATO,
				NR_SEQUENCIAL_MEDICAO);

		assertTrue(penultimo.isEmpty());
	}

	@Test
	void testRecuperarPenultimoHistorico_existente() {

		HistoricoMedicaoBD hist1 = buildBd(SituacaoMedicaoEnum.AT);
		HistoricoMedicaoBD hist2 = buildBd(SituacaoMedicaoEnum.EM);

		when(historicoDAO.recuperarHistoricoMedicao(ID_CONTRATO, NR_SEQUENCIAL_MEDICAO))
				.thenReturn(List.of(hist1, hist2));

		Optional<HistoricoMedicaoBD> penultimo = historicoBC.recuperarPenultimoHistoricoPorMedicaoContrato(ID_CONTRATO,
				NR_SEQUENCIAL_MEDICAO);

		assertFalse(penultimo.isEmpty());
		assertEquals(hist1, penultimo.get());
	}

	@Test
	void testRecuperarUltimoHistoricoPorMedicaoContrato() {

		HistoricoMedicaoBD historico = buildBd(SituacaoMedicaoEnum.EC);

		when(historicoDAO.recuperarUltimoHistoricoPorMedicaoContrato(ID_CONTRATO, NR_SEQUENCIAL_MEDICAO))
				.thenReturn(Optional.of(historico));

		Optional<HistoricoMedicaoBD> ultimo = historicoBC.recuperarUltimoHistoricoPorMedicaoContrato(ID_CONTRATO,
				NR_SEQUENCIAL_MEDICAO);

		assertFalse(ultimo.isEmpty());
		assertEquals(historico, ultimo.get());
	}

	private static HistoricoMedicaoDTO buildDto(String nrCpfResponsavel, PerfilEnum inPerfilResponsavel) {

		HistoricoMedicaoDTO historico = new HistoricoMedicaoDTO();

		historico.setNrCpfResponsavel(nrCpfResponsavel);
		historico.setInPerfilResponsavel(inPerfilResponsavel);
		historico.setNrSequencial(NR_SEQUENCIAL_MEDICAO);

		return historico;
	}

	private static HistoricoMedicaoBD buildBd(SituacaoMedicaoEnum situacao) {
		return new HistoricoMedicaoBD(ID_CONTRATO, NR_SEQUENCIAL_MEDICAO, situacao);
	}
}
