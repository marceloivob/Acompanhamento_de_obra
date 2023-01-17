package br.gov.planejamento.siconv.med.medicao.business.medicao;

import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.CONCEDENTE;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.MANDATARIA;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.PROPONENTE_CONVENENTE;
import static br.gov.planejamento.siconv.med.infra.security.domain.Role.ADMINISTRADOR_SISTEMA;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.AC;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.ACT;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.AT;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.ECC;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.CC;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.ECE;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.EM;
import static br.gov.planejamento.siconv.med.test.builder.MedicaoBuilder.newMedicaoBuilder;
import static br.gov.planejamento.siconv.med.test.builder.VistoriaExtraDTOBuilder.newVistoriaBuilder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import br.gov.planejamento.siconv.med.contrato.dao.ContratoDAO;
import br.gov.planejamento.siconv.med.infra.message.MessageKey;
import br.gov.planejamento.siconv.med.medicao.business.HistoricoMedicaoBC;
import br.gov.planejamento.siconv.med.medicao.business.MedicaoBC;
import br.gov.planejamento.siconv.med.medicao.business.ObservacaoBC;
import br.gov.planejamento.siconv.med.medicao.business.SubmetaBC;
import br.gov.planejamento.siconv.med.medicao.dao.MedicaoDAO;
import br.gov.planejamento.siconv.med.medicao.dao.SubmetaDAO;
import br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum;
import br.gov.planejamento.siconv.med.medicao.entity.SolicitanteVistoriaExtraEnum;
import br.gov.planejamento.siconv.med.medicao.entity.database.HistoricoMedicaoBD;
import br.gov.planejamento.siconv.med.medicao.entity.database.MedicaoBD;
import br.gov.planejamento.siconv.med.medicao.entity.dto.VistoriaExtraDTO;
import br.gov.planejamento.siconv.med.test.extension.BusinessControllerBaseTest;
import br.gov.planejamento.siconv.med.test.extension.MockUsuario;

class MedicaoBC_SolicitarComplementacaoTest extends BusinessControllerBaseTest {

	private static final String CPF_RESPONSAVEL = "11111111111";

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
	void testSolicitarComplementacaoEmpresa_parametroIdMedicaoNulo() {

		Exception exception = assertThrows(NullPointerException.class,
				() -> medicaoBC.solicitarComplementacaoEmpresa(null));

		assertEquals("Parâmetro idMedicao não pode ser nulo", exception.getMessage());
	}

	@Test
	@MockUsuario(profile = PROPONENTE_CONVENENTE)
	void testSolicitarComplementacaoEmpresa_medicaoNaoPermiteComplementacao() {

		MedicaoBD medicao = newMedicaoBuilder().setId(1L).setMedContrato(1L).comSituacao(AC).create();

		when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);

		assertThrowsMedicaoRestException(MessageKey.ERRO_COMPLEMENTACAO_EMPRESA_NAO_PERMITIDA,
				() -> medicaoBC.solicitarComplementacaoEmpresa(medicao.id));
	}
	
	@Test
	@MockUsuario(profile = PROPONENTE_CONVENENTE)
	void testSolicitarComplementacaoEmpresa_medicaoNaoPermiteComplementacao_bloqueada() {

		MedicaoBD medicao = newMedicaoBuilder().setId(1L).setMedContrato(1L).comSituacao(AT).setBloqueada(true).create();

		when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);

		assertThrowsMedicaoRestException(MessageKey.ERRO_COMPLEMENTACAO_EMPRESA_NAO_PERMITIDA,
				() -> medicaoBC.solicitarComplementacaoEmpresa(medicao.id));
	}

	@Test
	@MockUsuario(profile = PROPONENTE_CONVENENTE)
	void testSolicitarComplementacaoEmpresa_necessarioPeloMenosUmaObservacao() {

		MedicaoBD medicao = newMedicaoBuilder().setId(1L).setMedContrato(1L).comSituacao(AT).create();

		when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);
		when(medicaoDao.listarSituacoesMedicoes(medicao.idContratoMedicao)).thenReturn(Map.of(1L, AT, 2L, EM));
		when(observacaoBC.existeObservacaoCadastradaPorUsuarioDesbloqueadaNaMedicao(medicao.id, CPF_RESPONSAVEL))
				.thenReturn(false);

		assertThrowsMedicaoRestException(MessageKey.ERRO_NECESSARIO_CADASTRAR_PELO_MENOS_UMA_OBSERVACAO,
				() -> medicaoBC.solicitarComplementacaoEmpresa(medicao.id));
	}

	@Test
	@MockUsuario(profile = PROPONENTE_CONVENENTE)
	void testSolicitarComplementacaoEmpresa_medicaoSemAgrupamento() {

		MedicaoBD medicao = newMedicaoBuilder().setId(1L).setMedContrato(1L).comSituacao(AT).create();

		when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);
		when(medicaoDao.listarSituacoesMedicoes(medicao.idContratoMedicao)).thenReturn(Map.of(1L, AT, 2L, EM));
		when(observacaoBC.existeObservacaoCadastradaPorUsuarioDesbloqueadaNaMedicao(medicao.id, CPF_RESPONSAVEL))
				.thenReturn(true);

		medicaoBC.solicitarComplementacaoEmpresa(medicao.id);

		verify(medicaoDao, times(1)).alterar(medicaoCaptor.capture());
		verify(submetaBC, times(1)).apagarMarcacoesConvenenteSubmetasMedicao(any(), any(), any());
		verify(historicoMedicaoBC, times(1)).inserir(historicoCaptor.capture());

		assertEquals(ECE, medicaoCaptor.getValue().getSituacao());
		assertEquals(ECE, historicoCaptor.getValue().getSituacao());
	}
	
	@Test
	@MockUsuario(profile = PROPONENTE_CONVENENTE)
	void testSolicitarComplementacaoEmpresa_medicaoSemAgrupamento_reencaminhada_naoApagaMarcacoes() {

		MedicaoBD medicao = newMedicaoBuilder().setId(1L).setMedContrato(1L).comSituacao(CC).setPermiteComplementacaoValor(false).create();

		when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);
		when(medicaoDao.listarSituacoesMedicoes(medicao.idContratoMedicao)).thenReturn(Map.of(1L, CC, 2L, EM));
		when(observacaoBC.existeObservacaoCadastradaPorUsuarioDesbloqueadaNaMedicao(medicao.id, CPF_RESPONSAVEL))
				.thenReturn(true);

		medicaoBC.solicitarComplementacaoEmpresa(medicao.id);

		verify(medicaoDao, times(1)).alterar(medicaoCaptor.capture());
		verify(submetaBC, times(0)).apagarMarcacoesConvenenteSubmetasMedicao(any(), any(), any());
		verify(historicoMedicaoBC, times(1)).inserir(historicoCaptor.capture());

		assertEquals(ECE, medicaoCaptor.getValue().getSituacao());
		assertEquals(ECE, historicoCaptor.getValue().getSituacao());
	}
	
	@Test
	@MockUsuario(profile = PROPONENTE_CONVENENTE)
	void testSolicitarComplementacaoEmpresa_medicaoSemAgrupamento_reencaminhada_apagaMarcacoes() {

		MedicaoBD medicao = newMedicaoBuilder().setId(1L).setMedContrato(1L).comSituacao(CC).setPermiteComplementacaoValor(true).create();

		when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);
		when(medicaoDao.listarSituacoesMedicoes(medicao.idContratoMedicao)).thenReturn(Map.of(1L, CC, 2L, EM));
		when(observacaoBC.existeObservacaoCadastradaPorUsuarioDesbloqueadaNaMedicao(medicao.id, CPF_RESPONSAVEL))
				.thenReturn(true);

		medicaoBC.solicitarComplementacaoEmpresa(medicao.id);

		verify(medicaoDao, times(1)).alterar(medicaoCaptor.capture());
		verify(submetaBC, times(1)).apagarMarcacoesConvenenteSubmetasMedicao(any(), any(), any());
		verify(historicoMedicaoBC, times(1)).inserir(historicoCaptor.capture());

		assertEquals(ECE, medicaoCaptor.getValue().getSituacao());
		assertEquals(ECE, historicoCaptor.getValue().getSituacao());
	}

	@Test
	@MockUsuario(profile = PROPONENTE_CONVENENTE)
	void testSolicitarComplementacaoEmpresa_medicaoComAgrupamento() {

		MedicaoBD medicaoAgrupadora = newMedicaoBuilder().setId(3L).setMedContrato(1L).comSituacao(AT).create();

		MedicaoBD medicaoAcumulada1 = newMedicaoBuilder().setId(1L).setMedContrato(1L).comSituacao(AT).setAgrupadora(3L)
				.create();

		MedicaoBD medicaoAcumulada2 = newMedicaoBuilder().setId(2L).setMedContrato(1L).comSituacao(AT).setAgrupadora(3L)
				.create();

		when(medicaoDao.consultarMedicao(medicaoAgrupadora.id)).thenReturn(medicaoAgrupadora);
		when(medicaoDao.listarSituacoesMedicoes(medicaoAgrupadora.idContratoMedicao))
				.thenReturn(Map.of(1L, AT, 2L, AT, 3L, AT));
		when(medicaoDao.listarMedicoesAcumuladas(medicaoAgrupadora.getId()))
				.thenReturn(List.of(medicaoAcumulada1, medicaoAcumulada2));
		when(observacaoBC.existeObservacaoCadastradaPorUsuarioDesbloqueadaNaMedicao(medicaoAgrupadora.id,
				CPF_RESPONSAVEL)).thenReturn(true);

		medicaoBC.solicitarComplementacaoEmpresa(medicaoAgrupadora.id);

		verify(medicaoDao, times(3)).alterar(medicaoCaptor.capture());
		verify(submetaBC, times(3)).apagarMarcacoesConvenenteSubmetasMedicao(any(), any(), any());
		verify(historicoMedicaoBC, times(3)).inserir(historicoCaptor.capture());

		medicaoCaptor.getAllValues().forEach(medicao -> assertEquals(ECE, medicao.getSituacao()));
		historicoCaptor.getAllValues().forEach(historico -> assertEquals(ECE, historico.getSituacao()));

		assertEquals(ECE, historicoCaptor.getValue().getSituacao());
	}
	
	@Test
	@MockUsuario(profile = PROPONENTE_CONVENENTE)
	void testSolicitarComplementacaoEmpresa_medicaoComAgrupamento_naoApagaMarcacoes() {

		MedicaoBD medicaoAgrupadora = newMedicaoBuilder().setId(3L).setMedContrato(1L).comSituacao(AT).setPermiteComplementacaoValor(false).create();

		MedicaoBD medicaoAcumulada1 = newMedicaoBuilder().setId(1L).setMedContrato(1L).comSituacao(AT).setAgrupadora(3L).setPermiteComplementacaoValor(false)
				.create();

		MedicaoBD medicaoAcumulada2 = newMedicaoBuilder().setId(2L).setMedContrato(1L).comSituacao(AT).setAgrupadora(3L).setPermiteComplementacaoValor(false)
				.create();

		when(medicaoDao.consultarMedicao(medicaoAgrupadora.id)).thenReturn(medicaoAgrupadora);
		when(medicaoDao.listarSituacoesMedicoes(medicaoAgrupadora.idContratoMedicao))
				.thenReturn(Map.of(1L, AT, 2L, AT, 3L, AT));
		when(medicaoDao.listarMedicoesAcumuladas(medicaoAgrupadora.getId()))
				.thenReturn(List.of(medicaoAcumulada1, medicaoAcumulada2));
		when(observacaoBC.existeObservacaoCadastradaPorUsuarioDesbloqueadaNaMedicao(medicaoAgrupadora.id,
				CPF_RESPONSAVEL)).thenReturn(true);

		medicaoBC.solicitarComplementacaoEmpresa(medicaoAgrupadora.id);

		verify(medicaoDao, times(3)).alterar(medicaoCaptor.capture());
		verify(submetaBC, times(0)).apagarMarcacoesConvenenteSubmetasMedicao(any(), any(), any());
		verify(historicoMedicaoBC, times(3)).inserir(historicoCaptor.capture());

		medicaoCaptor.getAllValues().forEach(medicao -> assertEquals(ECE, medicao.getSituacao()));
		historicoCaptor.getAllValues().forEach(historico -> assertEquals(ECE, historico.getSituacao()));

		assertEquals(ECE, historicoCaptor.getValue().getSituacao());
	}
	
	@Test
	@MockUsuario(profile = PROPONENTE_CONVENENTE)
	void testSolicitarComplementacaoEmpresa_medicaoComAgrupamento_reencaminharNaoApagaMarcacoes() {

		MedicaoBD medicaoAgrupadora = newMedicaoBuilder().setId(3L).setMedContrato(1L).comSituacao(CC).setPermiteComplementacaoValor(false).create();

		MedicaoBD medicaoAcumulada1 = newMedicaoBuilder().setId(1L).setMedContrato(1L).comSituacao(CC).setAgrupadora(3L).setPermiteComplementacaoValor(false)
				.create();

		MedicaoBD medicaoAcumulada2 = newMedicaoBuilder().setId(2L).setMedContrato(1L).comSituacao(CC).setAgrupadora(3L).setPermiteComplementacaoValor(false)
				.create();

		when(medicaoDao.consultarMedicao(medicaoAgrupadora.id)).thenReturn(medicaoAgrupadora);
		when(medicaoDao.listarSituacoesMedicoes(medicaoAgrupadora.idContratoMedicao))
				.thenReturn(Map.of(1L, AT, 2L, AT, 3L, AT));
		when(medicaoDao.listarMedicoesAcumuladas(medicaoAgrupadora.getId()))
				.thenReturn(List.of(medicaoAcumulada1, medicaoAcumulada2));
		when(observacaoBC.existeObservacaoCadastradaPorUsuarioDesbloqueadaNaMedicao(medicaoAgrupadora.id,
				CPF_RESPONSAVEL)).thenReturn(true);

		medicaoBC.solicitarComplementacaoEmpresa(medicaoAgrupadora.id);

		verify(medicaoDao, times(3)).alterar(medicaoCaptor.capture());
		verify(submetaBC, times(0)).apagarMarcacoesConvenenteSubmetasMedicao(any(), any(), any());
		verify(historicoMedicaoBC, times(3)).inserir(historicoCaptor.capture());

		medicaoCaptor.getAllValues().forEach(medicao -> assertEquals(ECE, medicao.getSituacao()));
		historicoCaptor.getAllValues().forEach(historico -> assertEquals(ECE, historico.getSituacao()));

		assertEquals(ECE, historicoCaptor.getValue().getSituacao());
	}

	@Test
	@MockUsuario(profile = PROPONENTE_CONVENENTE)
	void testSolicitarComplementacaoEmpresa_bloqueiaMedicaoEmElaboracao() {

		MedicaoBD medicao = newMedicaoBuilder().setId(1L).setMedContrato(1L).comSituacao(AT).create();

		MedicaoBD medicaoEM = newMedicaoBuilder().setId(2L).setMedContrato(1L).comSituacao(EM).setBloqueada(false)
				.create();

		when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);
		when(medicaoDao.listarSituacoesMedicoes(medicao.idContratoMedicao)).thenReturn(Map.of(1L, AT, 2L, EM));
		when(observacaoBC.existeObservacaoCadastradaPorUsuarioDesbloqueadaNaMedicao(medicao.id, CPF_RESPONSAVEL))
				.thenReturn(true);
		when(medicaoDao.consultarMedicaoporSituacao(medicao.idContratoMedicao, SituacaoMedicaoEnum.EM))
				.thenReturn(List.of(medicaoEM));

		medicaoBC.solicitarComplementacaoEmpresa(medicao.id);

		verify(medicaoDao, times(1)).bloquearMedicao(medicaoCaptor.capture());
		assertEquals(medicaoEM.id, medicaoCaptor.getValue().getId());
	}

	@Test
	@MockUsuario(profile = CONCEDENTE)
	void testSolicitarComplementacaoConvenente_medicaoNaoPermiteComplementacao() {

		MedicaoBD medicao = newMedicaoBuilder().setId(1L).setMedContrato(1L).comSituacao(AT).create();

		VistoriaExtraDTO vistoriaExtraDTO = newVistoriaBuilder().setSolicitante(SolicitanteVistoriaExtraEnum.CCD)
				.isVistoriaExtra(true).create();

		Map<Long, SituacaoMedicaoEnum> situacoesMedicoesContrato = new HashMap<Long, SituacaoMedicaoEnum>();

		when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);
		when(medicaoDao.listarSituacoesMedicoes(medicao.idContratoMedicao)).thenReturn(situacoesMedicoesContrato);

		assertThrowsMedicaoRestException(MessageKey.ERRO_COMPLEMENTACAO_MEDICAO_NAO_PERMITIDA,
				() -> medicaoBC.solicitarComplementacaoConvenente(vistoriaExtraDTO, medicao.id));
	}

	@Test
	@MockUsuario(profile = MANDATARIA)
	void testSolicitarComplementacaoConvenente_necessarioPeloMenosUmaObservacao() {

		MedicaoBD medicao = newMedicaoBuilder().setId(1L).setMedContrato(1L).comSituacao(AC).create();

		VistoriaExtraDTO vistoriaExtraDTO = newVistoriaBuilder().setSolicitante(SolicitanteVistoriaExtraEnum.CCD)
				.isVistoriaExtra(true).create();

		when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);
		when(medicaoDao.listarSituacoesMedicoes(medicao.idContratoMedicao)).thenReturn(Map.of(1L, AC, 2L, AT));
		when(observacaoBC.existeObservacaoCadastradaPorUsuarioDesbloqueadaNaMedicao(medicao.id, CPF_RESPONSAVEL))
				.thenReturn(false);

		assertThrowsMedicaoRestException(MessageKey.ERRO_NECESSARIO_CADASTRAR_PELO_MENOS_UMA_OBSERVACAO,
				() -> medicaoBC.solicitarComplementacaoConvenente(vistoriaExtraDTO, medicao.id));
	}

	@Test
	@MockUsuario(profile = MANDATARIA)
	void testSolicitarComplementacaoConvenente_medicaoBloqueada() {

		MedicaoBD medicao = newMedicaoBuilder().setId(2L).setMedContrato(1L).comSituacao(SituacaoMedicaoEnum.AC)
				.setBloqueada(true).create();

		VistoriaExtraDTO vistoriaExtraDTO = newVistoriaBuilder().setSolicitante(SolicitanteVistoriaExtraEnum.CCD)
				.isVistoriaExtra(true).create();

		when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);
		when(medicaoDao.listarSituacoesMedicoes(medicao.idContratoMedicao)).thenReturn(Map.of(1L, ACT, 2L, AC));
		when(observacaoBC.existeObservacaoCadastradaPorUsuarioDesbloqueadaNaMedicao(medicao.id, CPF_RESPONSAVEL))
				.thenReturn(true);

		assertThrowsMedicaoRestException(MessageKey.ERRO_MEDICAO_BLOQUEADA,
				() -> medicaoBC.solicitarComplementacaoConvenente(vistoriaExtraDTO, medicao.id));
	}

	@Test
	@MockUsuario(profile = CONCEDENTE, roles = ADMINISTRADOR_SISTEMA)
	void testSolicitarComplementacaoConvenente_medicaoSemAgrupamento() {

		MedicaoBD medicao = newMedicaoBuilder().setId(1L).setMedContrato(1L).comSituacao(AC).create();

		VistoriaExtraDTO vistoriaExtraDTO = newVistoriaBuilder().setSolicitante(SolicitanteVistoriaExtraEnum.CCD)
				.isVistoriaExtra(false).setVersao(1L).create();

		when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);
		when(medicaoDao.listarSituacoesMedicoes(medicao.idContratoMedicao)).thenReturn(Map.of(1L, AC, 2L, AT));
		when(observacaoBC.existeObservacaoCadastradaPorUsuarioDesbloqueadaNaMedicao(medicao.id, CPF_RESPONSAVEL))
				.thenReturn(false);

		medicaoBC.solicitarComplementacaoConvenente(vistoriaExtraDTO, medicao.id);

		verify(medicaoDao, times(1)).alterar(medicaoCaptor.capture());
		verify(submetaBC, times(1)).apagarMarcacoesConcedenteSubmetasMedicao(any(), any(), any());
		verify(historicoMedicaoBC, times(1)).inserir(historicoCaptor.capture());

		assertEquals(ECC, medicaoCaptor.getValue().getSituacao());
		assertEquals(ECC, historicoCaptor.getValue().getSituacao());
		assertEquals(true, medicao.getPermiteComplementacaoValor());
	}

	@Test
	@MockUsuario(profile = CONCEDENTE, roles = ADMINISTRADOR_SISTEMA)
	void testSolicitarComplementacaoConvenente_medicaoComAgrupamento() {

		MedicaoBD medicaoAgrupadora = newMedicaoBuilder().setId(3L).setMedContrato(1L).comSituacao(AC).create();

		MedicaoBD medicaoAcumulada1 = newMedicaoBuilder().setId(1L).setMedContrato(1L).comSituacao(AC).setAgrupadora(3L)
				.create();

		MedicaoBD medicaoAcumulada2 = newMedicaoBuilder().setId(2L).setMedContrato(1L).comSituacao(AC).setAgrupadora(3L)
				.create();

		VistoriaExtraDTO vistoriaExtraDTO = newVistoriaBuilder().setSolicitante(SolicitanteVistoriaExtraEnum.CCD)
				.isVistoriaExtra(false).setVersao(1L).create();

		when(medicaoDao.consultarMedicao(medicaoAgrupadora.getId())).thenReturn(medicaoAgrupadora);
		when(medicaoDao.listarSituacoesMedicoes(medicaoAgrupadora.idContratoMedicao))
				.thenReturn(Map.of(1L, AC, 2L, AC, 3L, AC));
		when(medicaoDao.listarMedicoesAcumuladas(medicaoAgrupadora.getId()))
				.thenReturn(List.of(medicaoAcumulada1, medicaoAcumulada2));
		when(observacaoBC.existeObservacaoCadastradaPorUsuarioDesbloqueadaNaMedicao(medicaoAgrupadora.getId(),
				CPF_RESPONSAVEL)).thenReturn(false);

		medicaoBC.solicitarComplementacaoConvenente(vistoriaExtraDTO, medicaoAgrupadora.getId());

		verify(medicaoDao, times(3)).alterar(medicaoCaptor.capture());
		verify(submetaBC, times(1)).apagarMarcacoesConcedenteSubmetasMedicao(any(), any(), any());
		verify(historicoMedicaoBC, times(3)).inserir(historicoCaptor.capture());

		medicaoCaptor.getAllValues().forEach(medicao -> {
			assertEquals(ECC, medicao.getSituacao());
			assertTrue(medicao.getPermiteComplementacaoValor());
		});

		historicoCaptor.getAllValues().forEach(historico -> assertEquals(ECC, historico.getSituacao()));
	}

	@Test
	@MockUsuario(profile = MANDATARIA)
	void testSolicitarComplementacaoConvenente_medicaoSemAgrupamentoMandataria_naoApagaDados() {

		MedicaoBD medicao = newMedicaoBuilder().setId(1L).setMedContrato(1L).comSituacao(AC).create();

		VistoriaExtraDTO vistoriaExtraDTO = newVistoriaBuilder().setSolicitante(SolicitanteVistoriaExtraEnum.CCD)
				.isVistoriaExtra(false).setVersao(1L).create();

		when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);
		when(medicaoDao.listarSituacoesMedicoes(medicao.idContratoMedicao)).thenReturn(Map.of(1L, AC, 2L, AT));
		when(observacaoBC.existeObservacaoCadastradaPorUsuarioDesbloqueadaNaMedicao(medicao.id, CPF_RESPONSAVEL))
				.thenReturn(true);

		medicaoBC.solicitarComplementacaoConvenente(vistoriaExtraDTO, medicao.id);

		verify(medicaoDao, times(1)).alterar(medicaoCaptor.capture());
		verify(submetaBC, times(0)).apagarMarcacoesConcedenteSubmetasMedicao(any(), any(), any());
		verify(historicoMedicaoBC, times(1)).inserir(historicoCaptor.capture());

		assertEquals(ECC, medicaoCaptor.getValue().getSituacao());
		assertEquals(ECC, historicoCaptor.getValue().getSituacao());
		assertEquals(false, medicao.getPermiteComplementacaoValor());
	}

	@Test
	@MockUsuario(profile = MANDATARIA)
	void testSolicitarComplementacaoConvenente_medicaoComAgrupamentoMandataria_naoApagaDados() {

		MedicaoBD medicaoAgrupadora = newMedicaoBuilder().setId(3L).setMedContrato(1L).comSituacao(AC).create();

		MedicaoBD medicaoAcumulada1 = newMedicaoBuilder().setId(1L).setMedContrato(1L).comSituacao(AC).setAgrupadora(3L)
				.create();

		MedicaoBD medicaoAcumulada2 = newMedicaoBuilder().setId(2L).setMedContrato(1L).comSituacao(AC).setAgrupadora(3L)
				.create();

		VistoriaExtraDTO vistoriaExtraDTO = newVistoriaBuilder().setSolicitante(SolicitanteVistoriaExtraEnum.CCD)
				.isVistoriaExtra(false).setVersao(1L).create();

		when(medicaoDao.consultarMedicao(medicaoAgrupadora.getId())).thenReturn(medicaoAgrupadora);
		when(medicaoDao.listarSituacoesMedicoes(medicaoAgrupadora.idContratoMedicao))
				.thenReturn(Map.of(1L, AC, 2L, AC, 3L, AC));
		when(medicaoDao.listarMedicoesAcumuladas(medicaoAgrupadora.getId()))
				.thenReturn(List.of(medicaoAcumulada1, medicaoAcumulada2));
		when(observacaoBC.existeObservacaoCadastradaPorUsuarioDesbloqueadaNaMedicao(medicaoAgrupadora.id,
				CPF_RESPONSAVEL)).thenReturn(true);

		medicaoBC.solicitarComplementacaoConvenente(vistoriaExtraDTO, medicaoAgrupadora.id);

		verify(medicaoDao, times(3)).alterar(medicaoCaptor.capture());
		verify(submetaBC, times(0)).apagarMarcacoesConcedenteSubmetasMedicao(any(), any(), any());
		verify(historicoMedicaoBC, times(3)).inserir(historicoCaptor.capture());

		medicaoCaptor.getAllValues().forEach(medicao -> {
			assertEquals(ECC, medicao.getSituacao());
			assertFalse(medicao.getPermiteComplementacaoValor());
		});

		historicoCaptor.getAllValues().forEach(historico -> assertEquals(ECC, historico.getSituacao()));
	}
}
