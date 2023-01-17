package br.gov.planejamento.siconv.med.medicao.business.medicao;

import static br.gov.planejamento.siconv.med.test.builder.ContratoMedicaoBuilder.newContratoMedicaoBuilder;
import static br.gov.planejamento.siconv.med.test.builder.MedicaoBuilder.newMedicaoBuilder;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.HandleCallback;
import org.jdbi.v3.core.HandleConsumer;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import br.gov.planejamento.siconv.med.contrato.business.ContratosBC;
import br.gov.planejamento.siconv.med.contrato.dao.ContratoDAO;
import br.gov.planejamento.siconv.med.contrato.entity.ContratoBD;
import br.gov.planejamento.siconv.med.contrato.entity.dto.ContratoSiconvDTO;
import br.gov.planejamento.siconv.med.infra.exception.MedicaoRestException;
import br.gov.planejamento.siconv.med.infra.message.Message;
import br.gov.planejamento.siconv.med.infra.message.MessageKey;
import br.gov.planejamento.siconv.med.medicao.business.MedicaoBC;
import br.gov.planejamento.siconv.med.medicao.dao.MedicaoDAO;
import br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum;
import br.gov.planejamento.siconv.med.medicao.entity.database.MedicaoBD;
import br.gov.planejamento.siconv.med.medicao.entity.dto.MedicaoDTO;

class MedicaoBC_IncluirAlterarMedicaoTest {

	@Mock
	private Jdbi jdbi;

	@Mock
	private Handle handle;

	@Mock
	private MedicaoDAO medicaoDao;
	
	@Mock
	private ContratoDAO contratoDao;
		
	@Mock
	private ContratosBC contratoBC;
	
	@InjectMocks
	private MedicaoBC medicaoBC;
		
	@BeforeEach
	void setup() throws Exception {

		MockitoAnnotations.initMocks(this);

		when(handle.attach(MedicaoDAO.class)).thenReturn(medicaoDao);
		when(jdbi.onDemand(MedicaoDAO.class)).thenReturn(medicaoDao);
		
		when(handle.attach(ContratoDAO.class)).thenReturn(contratoDao);
		when(jdbi.onDemand(ContratoDAO.class)).thenReturn(contratoDao);
		
		when(jdbi.inTransaction(Mockito.any())).then(invocation -> {
			HandleCallback<?, ?> callback = invocation.getArgument(0);
			return callback.withHandle(handle);
		});

		doAnswer(invocation -> {
			HandleConsumer<?> consumer = invocation.getArgument(0);
			consumer.useHandle(handle);
			return null;
		}).when(jdbi).useTransaction(Mockito.any());
	}

	/* ****************************************************************
	 * INCLUIR MEDIÇÃO
	 * ****************************************************************
	 */

	@Test
	void testIncluirMedicao_contratoNaoConfigurado() {

		ContratoSiconvDTO contrato = new ContratoSiconvDTO();
		contrato.setId(1L);
		contrato.setIsConfiguradoParaMedicao(FALSE);
		
		ContratoBD contratoMedicao = newContratoMedicaoBuilder().setId(1L)
				.setContratoSiconv(contrato.getId()).create();
		
		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setDataInicioObra(LocalDate.of(2019, 1, 5));
		
		when(contratoBC.consultarContratoPorId(contrato.getId())).thenReturn(contrato);
		when(contratoBC.consultarContratoMedicaoPorContratoFK(1L)).thenReturn(contratoMedicao);
		
		assertThrowsMedicaoRestException(MessageKey.ERRO_CONTRATO_NAO_CONFIGURADO,
				() -> medicaoBC.incluir(medicaoDTO, 1L));
	}
	
	@Test
	void testIncluirMedicao_todasSubmetasFinalizadas() {

		ContratoSiconvDTO contrato = new ContratoSiconvDTO();
		contrato.setId(1L);
		contrato.setIsConfiguradoParaMedicao(TRUE);
		
		ContratoBD contratoMedicao = newContratoMedicaoBuilder().setId(1L)
				.setContratoSiconv(contrato.getId()).create();
		
		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setDataInicioObra(LocalDate.of(2019, 1, 5));
		
		when(contratoBC.consultarContratoPorId(contrato.getId())).thenReturn(contrato);
		when(contratoBC.consultarContratoMedicaoPorContratoFK(1L)).thenReturn(contratoMedicao);
		when(contratoBC.temSubmetasAExecutar(contrato.getId())).thenReturn(FALSE);
		
		assertThrowsMedicaoRestException(MessageKey.ERRO_CONTRATO_TODAS_SUBMETAS_FINALIZADAS_EMPRESA,
				() -> medicaoBC.incluir(medicaoDTO, 1L));
	}
	
	@Test
	void testIncluirMedicao_jaExisteMedicaoEmElaboracao() {

		ContratoSiconvDTO contrato = new ContratoSiconvDTO();
		contrato.setId(1L);
		contrato.setIsConfiguradoParaMedicao(TRUE);
		
		ContratoBD contratoMedicao = newContratoMedicaoBuilder().setId(1L)
				.setContratoSiconv(contrato.getId()).create();
		
		Map<Long, SituacaoMedicaoEnum> situacoesMedicoes = new HashMap<Long, SituacaoMedicaoEnum>();
		situacoesMedicoes.put(1L, SituacaoMedicaoEnum.EM);
		
		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setDataInicioObra(LocalDate.of(2019, 1, 5));
		
		when(contratoBC.consultarContratoPorId(contrato.getId())).thenReturn(contrato);
		when(contratoBC.consultarContratoMedicaoPorContratoFK(1L)).thenReturn(contratoMedicao);
		when(contratoBC.temSubmetasAExecutar(contrato.getId())).thenReturn(TRUE);
		when(medicaoDao.listarSituacoesMedicoes(1L)).thenReturn(situacoesMedicoes);
		
		assertThrowsMedicaoRestException(MessageKey.ERRO_CRIACAO_MEDICAO_QUANDO_JA_EXISTE_EM_ELABORACAO,
				() -> medicaoBC.incluir(medicaoDTO, 1L));
	}
	
	@Test
	void testIncluirMedicao_existeMedicaoEmComplementacao() {

		ContratoSiconvDTO contrato = new ContratoSiconvDTO();
		contrato.setId(1L);
		contrato.setIsConfiguradoParaMedicao(TRUE);
		
		ContratoBD contratoMedicao = newContratoMedicaoBuilder().setId(1L)
				.setContratoSiconv(contrato.getId()).create();
		
		Map<Long, SituacaoMedicaoEnum> situacoesMedicoes = new HashMap<Long, SituacaoMedicaoEnum>();
		situacoesMedicoes.put(1L, SituacaoMedicaoEnum.CE);
		
		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setDataInicioObra(LocalDate.of(2019, 1, 5));
		
		when(contratoBC.consultarContratoPorId(contrato.getId())).thenReturn(contrato);
		when(contratoBC.consultarContratoMedicaoPorContratoFK(1L)).thenReturn(contratoMedicao);
		when(contratoBC.temSubmetasAExecutar(contrato.getId())).thenReturn(TRUE);
		when(medicaoDao.listarSituacoesMedicoes(1L)).thenReturn(situacoesMedicoes);
		
		assertThrowsMedicaoRestException(MessageKey.ERRO_CONTRATO_POSSUI_MEDICAO_COMPL_EMP_OU_ENVIADA_COMPL_EMP,
				() -> medicaoBC.incluir(medicaoDTO, 1L));
	}
	
	@Test
	void testIncluirMedicao_existeMedicaoEnviadaParaComplementacao() {

		ContratoSiconvDTO contrato = new ContratoSiconvDTO();
		contrato.setId(1L);
		contrato.setIsConfiguradoParaMedicao(TRUE);
		
		ContratoBD contratoMedicao = newContratoMedicaoBuilder().setId(1L)
				.setContratoSiconv(contrato.getId()).create();
		
		Map<Long, SituacaoMedicaoEnum> situacoesMedicoes = new HashMap<Long, SituacaoMedicaoEnum>();
		situacoesMedicoes.put(1L, SituacaoMedicaoEnum.ECE);
		
		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setDataInicioObra(LocalDate.of(2019, 1, 5));
		
		when(contratoBC.consultarContratoPorId(contrato.getId())).thenReturn(contrato);
		when(contratoBC.consultarContratoMedicaoPorContratoFK(1L)).thenReturn(contratoMedicao);
		when(contratoBC.temSubmetasAExecutar(contrato.getId())).thenReturn(TRUE);
		when(medicaoDao.listarSituacoesMedicoes(1L)).thenReturn(situacoesMedicoes);
		
		assertThrowsMedicaoRestException(MessageKey.ERRO_CONTRATO_POSSUI_MEDICAO_COMPL_EMP_OU_ENVIADA_COMPL_EMP,
				() -> medicaoBC.incluir(medicaoDTO, 1L));
	}
	
	@Test
	void testIncluirMedicao_dtInicioObraObrigatoria() {

		ContratoSiconvDTO contrato = new ContratoSiconvDTO();
		contrato.setId(1L);
		contrato.setIsConfiguradoParaMedicao(TRUE);
		contrato.setQtdeMedicoes(1);
		
		ContratoBD contratoMedicao = newContratoMedicaoBuilder().setId(1L)
				.setContratoSiconv(contrato.getId()).create();
		
		Map<Long, SituacaoMedicaoEnum> situacoesMedicoes = new HashMap<Long, SituacaoMedicaoEnum>();
		situacoesMedicoes.put(1L, SituacaoMedicaoEnum.EC);
		
		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setDataInicioObra(LocalDate.of(2019, 1, 5));
		
		when(contratoBC.consultarContratoPorId(contrato.getId())).thenReturn(contrato);
		when(contratoBC.consultarContratoMedicaoPorContratoFK(1L)).thenReturn(contratoMedicao);
		when(contratoBC.temSubmetasAExecutar(contrato.getId())).thenReturn(TRUE);
		when(medicaoDao.listarSituacoesMedicoes(1L)).thenReturn(situacoesMedicoes);
		
		assertThrowsMedicaoRestException(MessageKey.ERRO_DATA_INICIO_OBRIGATORIA,
				() -> medicaoBC.incluir(medicaoDTO, 1L));
	}
	
	@Test
	void testIncluirMedicao_dtInicioObrigatoria() {

		ContratoSiconvDTO contrato = new ContratoSiconvDTO();
		contrato.setId(1L);
		contrato.setIsConfiguradoParaMedicao(TRUE);
		contrato.setQtdeMedicoes(1);
		
		ContratoBD contratoMedicao = newContratoMedicaoBuilder().setId(1L)
				.setContratoSiconv(contrato.getId()).create();
		
		Map<Long, SituacaoMedicaoEnum> situacoesMedicoes = new HashMap<Long, SituacaoMedicaoEnum>();
		situacoesMedicoes.put(1L, SituacaoMedicaoEnum.EC);
		
		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setDataInicioObra(LocalDate.of(2019, 1, 5));
		
		when(contratoBC.consultarContratoPorId(contrato.getId())).thenReturn(contrato);
		when(contratoBC.consultarContratoMedicaoPorContratoFK(1L)).thenReturn(contratoMedicao);
		when(contratoBC.temSubmetasAExecutar(contrato.getId())).thenReturn(TRUE);
		when(medicaoDao.listarSituacoesMedicoes(1L)).thenReturn(situacoesMedicoes);
		
		assertThrowsMedicaoRestException(MessageKey.ERRO_DATA_INICIO_OBRIGATORIA,
				() -> medicaoBC.incluir(medicaoDTO, 1L));
	}
	
	@Test
	void testIncluirMedicao_versaoMedicaoNulo() {

		ContratoSiconvDTO contrato = new ContratoSiconvDTO();
		contrato.setId(1L);
		contrato.setIsConfiguradoParaMedicao(TRUE);
		contrato.setQtdeMedicoes(1);
		
		ContratoBD contratoMedicao = newContratoMedicaoBuilder().setId(1L)
				.setContratoSiconv(contrato.getId()).create();
		
		Map<Long, SituacaoMedicaoEnum> situacoesMedicoes = new HashMap<Long, SituacaoMedicaoEnum>();
		situacoesMedicoes.put(1L, SituacaoMedicaoEnum.EC);
		
		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setDataInicioObra(LocalDate.of(2019, 1, 5));
		medicaoDTO.setDataInicio(LocalDate.of(2019, 1, 5));
		medicaoDTO.setId(2L);
		medicaoDTO.setVersao(null);
		
		when(contratoBC.consultarContratoPorId(contrato.getId())).thenReturn(contrato);
		when(contratoBC.consultarContratoMedicaoPorContratoFK(1L)).thenReturn(contratoMedicao);
		when(contratoBC.temSubmetasAExecutar(contrato.getId())).thenReturn(TRUE);
		when(medicaoDao.listarSituacoesMedicoes(1L)).thenReturn(situacoesMedicoes);
		
		assertThrowsMedicaoRestException(MessageKey.ERRO_PARAMETRO_OBRIGATORIO_NAO_INFORMADO,
				() -> medicaoBC.incluir(medicaoDTO, 1L));
	}
	
	@Test
	void testIncluirMedicao_dtInicioObjetoMaiorAtual() {

		ContratoSiconvDTO contrato = new ContratoSiconvDTO();
		contrato.setId(1L);
		contrato.setIsConfiguradoParaMedicao(TRUE);
		contrato.setQtdeMedicoes(1);
		
		ContratoBD contratoMedicao = newContratoMedicaoBuilder().setId(1L)
				.setContratoSiconv(contrato.getId()).create();
		
		Map<Long, SituacaoMedicaoEnum> situacoesMedicoes = new HashMap<Long, SituacaoMedicaoEnum>();
		situacoesMedicoes.put(1L, SituacaoMedicaoEnum.EC);
		
		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setDataInicioObra(LocalDate.now().plusDays(2L));
		medicaoDTO.setDataInicio(LocalDate.of(2019, 1, 5));
		medicaoDTO.setId(2L);
		medicaoDTO.setVersao(1L);
		
		when(contratoBC.consultarContratoPorId(contrato.getId())).thenReturn(contrato);
		when(contratoBC.consultarContratoMedicaoPorContratoFK(1L)).thenReturn(contratoMedicao);
		when(contratoBC.temSubmetasAExecutar(contrato.getId())).thenReturn(TRUE);
		when(medicaoDao.listarSituacoesMedicoes(1L)).thenReturn(situacoesMedicoes);
		
		assertThrowsMedicaoRestException(MessageKey.ERRO_DATA_INICIO_OBJETO_MAIOR_QUE_ATUAL,
				() -> medicaoBC.incluir(medicaoDTO, 1L));
	}
	
	@Test
	void testIncluirMedicao_dtInicioMaiorQueDtFim() {

		ContratoSiconvDTO contrato = new ContratoSiconvDTO();
		contrato.setId(1L);
		contrato.setIsConfiguradoParaMedicao(TRUE);
		contrato.setQtdeMedicoes(1);
		
		ContratoBD contratoMedicao = newContratoMedicaoBuilder().setId(1L)
				.setContratoSiconv(contrato.getId()).create();
		
		Map<Long, SituacaoMedicaoEnum> situacoesMedicoes = new HashMap<Long, SituacaoMedicaoEnum>();
		situacoesMedicoes.put(1L, SituacaoMedicaoEnum.EC);
		
		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setDataInicioObra(LocalDate.of(2019, 1, 5));
		medicaoDTO.setDataInicio(LocalDate.of(2019, 1, 5));
		medicaoDTO.setDataFim(LocalDate.of(2018, 1, 5));
		medicaoDTO.setId(2L);
		medicaoDTO.setVersao(1L);
		
		when(contratoBC.consultarContratoPorId(contrato.getId())).thenReturn(contrato);
		when(contratoBC.consultarContratoMedicaoPorContratoFK(1L)).thenReturn(contratoMedicao);
		when(contratoBC.temSubmetasAExecutar(contrato.getId())).thenReturn(TRUE);
		when(medicaoDao.listarSituacoesMedicoes(1L)).thenReturn(situacoesMedicoes);
		
		assertThrowsMedicaoRestException(MessageKey.ERRO_DATA_INICIO_ANTERIOR_DATA_FIM,
				() -> medicaoBC.incluir(medicaoDTO, 1L));
	}
	
//	@Test
//	void testIncluirMedicao_dtAssinaturaContrato() {
//
//		ContratoSiconvDTO contrato = new ContratoSiconvDTO();
//		contrato.setId(1L);
//		contrato.setIsConfiguradoParaMedicao(TRUE);
//		contrato.setQtdeMedicoes(1);
//		contrato.setDtAssinatura(LocalDate.now());
//		
//		ContratoBD contratoMedicao = newContratoMedicaoBuilder().setId(1L)
//				.setContratoSiconv(contrato.getId()).create();
//		
//		Map<Long, SituacaoMedicaoEnum> situacoesMedicoes = new HashMap<Long, SituacaoMedicaoEnum>();
//		situacoesMedicoes.put(1L, SituacaoMedicaoEnum.EC);
//		
//		MedicaoDTO medicaoDTO = new MedicaoDTO();
//		medicaoDTO.setDataInicioObra(LocalDate.of(2019, 1, 5));
//		medicaoDTO.setDataInicio(LocalDate.of(2019, 1, 5));
//		medicaoDTO.setId(2L);
//		medicaoDTO.setVersao(1L);
//		
//		when(contratoBC.consultarContratoPorId(contrato.getId())).thenReturn(contrato);
//		when(contratoBC.consultarContratoMedicaoPorContratoFK(1L)).thenReturn(contratoMedicao);
//		when(contratoBC.temSubmetasAExecutar(contrato.getId())).thenReturn(TRUE);
//		when(medicaoDao.listarSituacoesMedicoes(1L)).thenReturn(situacoesMedicoes);
//		
//		assertThrowsMedicaoRestException(MessageKey.ERRO_DATA_INICIO_ANTERIOR_DATA_ASSINATURA_CONTRATO,
//				() -> medicaoBC.incluir(medicaoDTO, 1L));
//	}
	
//	@Test
//	void testIncluirMedicao_dtFimMedicaoIgualAnteriorDtFimCTEF() {
//
//		ContratoSiconvDTO contrato = new ContratoSiconvDTO();
//		contrato.setId(1L);
//		contrato.setIsConfiguradoParaMedicao(TRUE);
//		contrato.setQtdeMedicoes(1);
//		contrato.setDtAssinatura(LocalDate.of(2018, 1, 5));
//		contrato.setDtFimVigencia(LocalDate.of(2018, 1, 5));
//		
//		ContratoBD contratoMedicao = newContratoMedicaoBuilder().setId(1L)
//				.setContratoSiconv(contrato.getId()).create();
//		
//		Map<Long, SituacaoMedicaoEnum> situacoesMedicoes = new HashMap<Long, SituacaoMedicaoEnum>();
//		situacoesMedicoes.put(1L, SituacaoMedicaoEnum.EC);
//		
//		MedicaoDTO medicaoDTO = new MedicaoDTO();
//		medicaoDTO.setDataInicioObra(LocalDate.of(2019, 1, 5));
//		medicaoDTO.setDataInicio(LocalDate.of(2019, 1, 5));
//		medicaoDTO.setDataFim(LocalDate.of(2019, 1, 5));
//		medicaoDTO.setId(2L);
//		medicaoDTO.setVersao(1L);
//		
//		when(contratoBC.consultarContratoPorId(contrato.getId())).thenReturn(contrato);
//		when(contratoBC.consultarContratoMedicaoPorContratoFK(1L)).thenReturn(contratoMedicao);
//		when(contratoBC.temSubmetasAExecutar(contrato.getId())).thenReturn(TRUE);
//		when(medicaoDao.listarSituacoesMedicoes(1L)).thenReturn(situacoesMedicoes);
//		
//		assertThrowsMedicaoRestException(MessageKey.ERRO_DATA_FIM_MEDICAO_IGUAL_OU_ANTERIOR_DATA_FIM_VIGENCIA_CTEF,
//				() -> medicaoBC.incluir(medicaoDTO, 1L));
//	}
	
	@Test
	void testIncluirMedicao_dtInicioObraDiferenteDtInicioMedicao() {
		
		ContratoSiconvDTO contrato = new ContratoSiconvDTO();
		contrato.setId(1L);
		contrato.setIsConfiguradoParaMedicao(TRUE);
		contrato.setQtdeMedicoes(0);
		contrato.setDtAssinatura(LocalDate.of(2018, 1, 5));
		contrato.setDtFimVigencia(LocalDate.of(2020, 1, 5));
		
		ContratoBD contratoMedicao = newContratoMedicaoBuilder().setId(1L)
				.setContratoSiconv(contrato.getId()).create();
		
		Map<Long, SituacaoMedicaoEnum> situacoesMedicoes = new HashMap<Long, SituacaoMedicaoEnum>();
		
		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setDataInicioObra(LocalDate.of(2019, 1, 10));
		medicaoDTO.setDataInicio(LocalDate.of(2019, 1, 5));
		medicaoDTO.setDataFim(LocalDate.of(2019, 1, 5));
		medicaoDTO.setId(1L);
		medicaoDTO.setVersao(1L);
		
		when(contratoBC.consultarContratoPorId(contrato.getId())).thenReturn(contrato);
		when(contratoBC.consultarContratoMedicaoPorContratoFK(1L)).thenReturn(contratoMedicao);
		when(contratoBC.temSubmetasAExecutar(contrato.getId())).thenReturn(TRUE);
		when(medicaoDao.listarSituacoesMedicoes(1L)).thenReturn(situacoesMedicoes);
		
		assertThrowsMedicaoRestException(MessageKey.ERRO_DATA_INICIO_OBRA_DIFERENTE_DATA_INICIO_MEDICAO,
				() -> medicaoBC.incluir(medicaoDTO, 1L));
	}
	
	@Test
	void testIncluirMedicao_dtInicioMedicaoDiferenteMedicaoAnteriorMais1() {
		
		Short sequencialAnterior = 1;
		
		ContratoSiconvDTO contrato = new ContratoSiconvDTO();
		contrato.setId(1L);
		contrato.setIsConfiguradoParaMedicao(TRUE);
		contrato.setQtdeMedicoes(1);
		contrato.setDtAssinatura(LocalDate.of(2018, 1, 5));
		contrato.setDtFimVigencia(LocalDate.of(2020, 1, 5));
		
		ContratoBD contratoMedicao = newContratoMedicaoBuilder().setId(1L)
				.setContratoSiconv(contrato.getId()).create();
		
		MedicaoBD medicaoAnterior = newMedicaoBuilder()
				.setNrSequencial(sequencialAnterior)
				.setDtFimMedicao(LocalDate.of(2018, 1, 5)).create();
		
		Map<Long, SituacaoMedicaoEnum> situacoesMedicoes = new HashMap<Long, SituacaoMedicaoEnum>();
		
		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setDataInicioObra(LocalDate.of(2019, 1, 5));
		medicaoDTO.setDataInicio(LocalDate.of(2019, 1, 5));
		medicaoDTO.setDataFim(LocalDate.of(2019, 1, 5));
		medicaoDTO.setId(2L);
		medicaoDTO.setVersao(1L);
		
		when(contratoBC.consultarContratoPorId(contrato.getId())).thenReturn(contrato);
		when(contratoBC.consultarContratoMedicaoPorContratoFK(1L)).thenReturn(contratoMedicao);
		when(contratoBC.temSubmetasAExecutar(contrato.getId())).thenReturn(TRUE);
		when(medicaoDao.listarSituacoesMedicoes(1L)).thenReturn(situacoesMedicoes);
		when(medicaoDao.consultarMedicaoPorSequencial(1L, sequencialAnterior)).thenReturn(medicaoAnterior);
		
		assertThrowsMedicaoRestException(MessageKey.ERRO_DATA_INICIO_MEDICAO_DIFERENTE_DIA_SEGUINTE_MEDICAO_ANTERIOR,
				() -> medicaoBC.incluir(medicaoDTO, 1L));
	}
	
	@Test
	void testIncluirMedicao_contratoParalisado() {

		ContratoSiconvDTO contrato = new ContratoSiconvDTO();
		contrato.setId(1L);
		contrato.setIsConfiguradoParaMedicao(TRUE);
		
		ContratoBD contratoMedicao = newContratoMedicaoBuilder().setId(1L)
				.setContratoSiconv(contrato.getId()).create();
		
		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setDataInicioObra(LocalDate.of(2019, 1, 5));
		
		when(contratoBC.consultarContratoPorId(contrato.getId())).thenReturn(contrato);
		when(contratoBC.consultarContratoMedicaoPorContratoFK(1L)).thenReturn(contratoMedicao);
		when(contratoBC.temSubmetasAExecutar(contrato.getId())).thenReturn(TRUE);
		when(contratoBC.isContratoParalisado(contrato.getId())).thenReturn(TRUE);
		
		assertThrowsMedicaoRestException(MessageKey.ERRO_CRIACAO_MEDICAO_QUANDO_CONTRATO_PARALISADO,
				() -> medicaoBC.incluir(medicaoDTO, 1L));
	}
	
	@Test
	void testIncluirMedicao() {
		
		Short sequencial = 1;

		ContratoSiconvDTO contrato = new ContratoSiconvDTO();
		contrato.setId(1L);
		contrato.setIsConfiguradoParaMedicao(TRUE);
		contrato.setQtdeMedicoes(0);
		contrato.setDtAssinatura(LocalDate.of(2018, 1, 5));
		contrato.setDtFimVigencia(LocalDate.of(2020, 1, 5));
		
		ContratoBD contratoMedicao = newContratoMedicaoBuilder().setId(1L)
				.setContratoSiconv(contrato.getId()).create();
				
		Map<Long, SituacaoMedicaoEnum> situacoesMedicoes = new HashMap<Long, SituacaoMedicaoEnum>();
		
		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setDataInicioObra(LocalDate.of(2019, 1, 5));
		medicaoDTO.setDataInicio(LocalDate.of(2019, 1, 5));
		medicaoDTO.setDataFim(LocalDate.of(2019, 1, 5));
		medicaoDTO.setId(2L);
		medicaoDTO.setVersao(1L);
		
		MedicaoBD medicao = newMedicaoBuilder()
				.setMedContrato(1L).setNrSequencial(sequencial)
				.setDtInicioMedicao(medicaoDTO.getDataInicio())
				.setDtFimMedicao(medicaoDTO.getDataFim())
				.comSituacao(SituacaoMedicaoEnum.EM).create();
				
		when(contratoBC.consultarContratoPorId(contrato.getId())).thenReturn(contrato);
		when(contratoBC.consultarContratoMedicaoPorContratoFK(1L)).thenReturn(contratoMedicao);
		when(contratoBC.temSubmetasAExecutar(contrato.getId())).thenReturn(TRUE);
		when(medicaoDao.listarSituacoesMedicoes(1L)).thenReturn(situacoesMedicoes);
		when(medicaoDao.inserir(medicao)).thenReturn(medicaoDTO.getId());
		
		medicaoBC.incluir(medicaoDTO, 1L);
		
		verify(contratoDao, times(1)).alterar(contratoMedicao);
		verify(medicaoDao, times(1)).inserir(medicao);
		verify(medicaoDao, times(1)).obterMedicao(2L);
	}
	
	/* ****************************************************************
	 * ALTERAR MEDIÇÃO
	 * ****************************************************************
	 */

	@Test
	void testAlterarMedicao_medicaoNaoEncontrada() {

		ContratoSiconvDTO contrato = new ContratoSiconvDTO();
		contrato.setId(1L);
		
		ContratoBD contratoMedicao = newContratoMedicaoBuilder().setId(1L)
				.setContratoSiconv(contrato.getId()).create();
				
		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setId(1L);
		
		when(contratoBC.consultarContratoPorId(contrato.getId())).thenReturn(contrato);
		when(contratoBC.consultarContratoAssociadoMedicao(1L)).thenReturn(contratoMedicao);
		when(medicaoDao.consultarMedicao(1L)).thenReturn(null);
		
		assertThrowsMedicaoRestException(MessageKey.ERRO_MEDICAO_NAO_ENCONTRADA,
				() -> medicaoBC.alterar(medicaoDTO));
	}
	
	@Test
	void testAlterarMedicao_medicaoNaoPodeSerAlterada_motivoSituacao() {

		ContratoSiconvDTO contrato = new ContratoSiconvDTO();
		contrato.setId(1L);
		
		ContratoBD contratoMedicao = newContratoMedicaoBuilder().setId(1L)
				.setContratoSiconv(contrato.getId()).create();
		
		MedicaoBD medicao = newMedicaoBuilder()
				.setMedContrato(1L).setId(1L)
				.comSituacao(SituacaoMedicaoEnum.EC).create();
		
		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setId(1L);
		
		when(contratoBC.consultarContratoPorId(contrato.getId())).thenReturn(contrato);
		when(contratoBC.consultarContratoAssociadoMedicao(1L)).thenReturn(contratoMedicao);
		when(medicaoDao.consultarMedicao(1L)).thenReturn(medicao);
		
		assertThrowsMedicaoRestException(MessageKey.ERRO_MEDICAO_NAO_PODE_SER_ALTERADA,
				() -> medicaoBC.alterar(medicaoDTO));
	}
	
	@Test
	void testAlterarMedicao_medicaoNaoPodeSerAlterada_motivoEhAgrupadora() {

		ContratoSiconvDTO contrato = new ContratoSiconvDTO();
		contrato.setId(1L);
		
		ContratoBD contratoMedicao = newContratoMedicaoBuilder().setId(1L)
				.setContratoSiconv(contrato.getId()).create();
		
		MedicaoBD medicao = newMedicaoBuilder()
				.setMedContrato(1L).setId(1L)
				.setAgrupadora(2L)
				.comSituacao(SituacaoMedicaoEnum.EM).create();
		
		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setId(1L);
		
		when(contratoBC.consultarContratoPorId(contrato.getId())).thenReturn(contrato);
		when(contratoBC.consultarContratoAssociadoMedicao(1L)).thenReturn(contratoMedicao);
		when(medicaoDao.consultarMedicao(1L)).thenReturn(medicao);
		
		assertThrowsMedicaoRestException(MessageKey.ERRO_MEDICAO_NAO_PODE_SER_ALTERADA,
				() -> medicaoBC.alterar(medicaoDTO));
	}

	@Test
	void testAlterarMedicao_medicaoNaoPodeSerAlterada_motivoComplementacaoSemAlteracao() {

		ContratoSiconvDTO contrato = new ContratoSiconvDTO();
		contrato.setId(1L);
		
		ContratoBD contratoMedicao = newContratoMedicaoBuilder().setId(1L)
				.setContratoSiconv(contrato.getId()).create();
		
		MedicaoBD medicao = newMedicaoBuilder()
				.setMedContrato(1L).setId(1L)
				.setPermiteComplementacaoValor(false)
				.comSituacao(SituacaoMedicaoEnum.CE).create();
		
		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setId(1L);
		
		when(contratoBC.consultarContratoPorId(contrato.getId())).thenReturn(contrato);
		when(contratoBC.consultarContratoAssociadoMedicao(1L)).thenReturn(contratoMedicao);
		when(medicaoDao.consultarMedicao(1L)).thenReturn(medicao);
		
		assertThrowsMedicaoRestException(MessageKey.ERRO_MEDICAO_NAO_PODE_SER_ALTERADA,
				() -> medicaoBC.alterar(medicaoDTO));
	}

	@Test
	void testAlterarMedicao() {

		Short sequencial = 1;
		
		ContratoSiconvDTO contrato = new ContratoSiconvDTO();
		contrato.setId(1L);
		contrato.setIsConfiguradoParaMedicao(TRUE);
		contrato.setQtdeMedicoes(0);
		contrato.setDtAssinatura(LocalDate.of(2018, 1, 5));
		contrato.setDtFimVigencia(LocalDate.of(2020, 1, 5));
		
		ContratoBD contratoMedicao = newContratoMedicaoBuilder().setId(1L)
				.setContratoSiconv(contrato.getId()).create();
				
		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setId(1L);
		medicaoDTO.setSequencial(sequencial);
		medicaoDTO.setDataInicioObra(LocalDate.of(2019, 1, 5));
		medicaoDTO.setDataInicio(LocalDate.of(2019, 1, 5));
		medicaoDTO.setDataFim(LocalDate.of(2019, 1, 5));
		medicaoDTO.setVersao(1L);
		
		MedicaoBD medicao = newMedicaoBuilder()
				.setMedContrato(1L).setId(1L).setNrSequencial(sequencial)
				.setDtInicioMedicao(medicaoDTO.getDataInicio())
				.setDtFimMedicao(medicaoDTO.getDataFim())
				.comSituacao(SituacaoMedicaoEnum.EM).create();

		Map<Long, SituacaoMedicaoEnum> situacoesMedicoes = new HashMap<Long, SituacaoMedicaoEnum>();
		
		when(contratoBC.consultarContratoPorId(contrato.getId())).thenReturn(contrato);
		when(contratoBC.consultarContratoAssociadoMedicao(1L)).thenReturn(contratoMedicao);
		when(medicaoDao.consultarMedicao(1L)).thenReturn(medicao);
		when(contratoBC.temSubmetasAExecutar(contrato.getId())).thenReturn(TRUE);
		when(medicaoDao.listarSituacoesMedicoes(1L)).thenReturn(situacoesMedicoes);
		
		medicaoBC.alterar(medicaoDTO);
		
		verify(contratoDao, times(1)).alterar(contratoMedicao);
		verify(medicaoDao, times(1)).alterar(medicao);
		verify(medicaoDao, times(1)).obterMedicao(1L);
	}
	
	private void assertThrowsMedicaoRestException(MessageKey expectedMessageKey, Executable executable) {

		MedicaoRestException exception = assertThrows(MedicaoRestException.class, executable);

		exception.getMessages().stream().map(Message::getKey).findFirst().ifPresentOrElse(
				actualMessageKey -> assertEquals(expectedMessageKey, actualMessageKey),
				() -> fail(format("A messageKey esperada era %s, mas nenhuma foi obtida", expectedMessageKey)));
	}
}
