package br.gov.planejamento.siconv.med.medicao.business.medicao;

import static br.gov.planejamento.siconv.med.configuracao.documentocomplementar.entity.dto.TipoManifestoEnum.DIS;
import static br.gov.planejamento.siconv.med.configuracao.documentocomplementar.entity.dto.TipoManifestoEnum.LIN;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.CONCEDENTE;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.EMPRESA;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.MANDATARIA;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.PROPONENTE_CONVENENTE;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.AC;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.ACT;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.ATD;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.CC;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.CE;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.EC;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.ECC;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.ECE;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.EM;
import static br.gov.planejamento.siconv.med.test.builder.ContratoMedicaoBuilder.newContratoMedicaoBuilder;
import static br.gov.planejamento.siconv.med.test.builder.DocumentoComplementarDTOBuilder.newDocumentoComplementar;
import static br.gov.planejamento.siconv.med.test.builder.MedicaoBuilder.newMedicaoBuilder;
import static br.gov.planejamento.siconv.med.test.builder.SubmetaBDBuilder.newSubmetaBuilder;
import static br.gov.planejamento.siconv.med.test.builder.SubmetaMedicaoDTOBuilder.newSubmetaMedicaoBuilder;
import static br.gov.planejamento.siconv.med.test.builder.VistoriaExtraDTOBuilder.newVistoriaBuilder;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.HandleCallback;
import org.jdbi.v3.core.HandleConsumer;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import br.gov.planejamento.siconv.med.configuracao.documentocomplementar.business.DocumentoComplementarBC;
import br.gov.planejamento.siconv.med.configuracao.documentocomplementar.dao.DocumentoComplementarDAO;
import br.gov.planejamento.siconv.med.configuracao.documentocomplementar.entity.dto.DocumentoComplementarDTO;
import br.gov.planejamento.siconv.med.configuracao.documentocomplementar.entity.dto.TipoDocumentoEnum;
import br.gov.planejamento.siconv.med.configuracao.documentocomplementar.entity.dto.TipoManifestoEnum;
import br.gov.planejamento.siconv.med.contrato.business.ContratosBC;
import br.gov.planejamento.siconv.med.contrato.dao.ContratoDAO;
import br.gov.planejamento.siconv.med.contrato.entity.ContratoBD;
import br.gov.planejamento.siconv.med.empresa.business.EmpresaBC;
import br.gov.planejamento.siconv.med.infra.exception.MedicaoRestException;
import br.gov.planejamento.siconv.med.infra.message.Message;
import br.gov.planejamento.siconv.med.infra.message.MessageKey;
import br.gov.planejamento.siconv.med.infra.security.SecurityContext;
import br.gov.planejamento.siconv.med.infra.security.UsuarioLogado;
import br.gov.planejamento.siconv.med.integration.contratos.ContratosGrpcConsumer;
import br.gov.planejamento.siconv.med.integration.maisbrasil.MaisBrasilGRPCConsumer;
import br.gov.planejamento.siconv.med.integration.projetobasico.ProjetoBasicoGRPCConsumer;
import br.gov.planejamento.siconv.med.integration.vrpl.VrplGRPCConsumer;
import br.gov.planejamento.siconv.med.medicao.business.HistoricoMedicaoBC;
import br.gov.planejamento.siconv.med.medicao.business.MedicaoBC;
import br.gov.planejamento.siconv.med.medicao.business.ObservacaoBC;
import br.gov.planejamento.siconv.med.medicao.business.SubmetaBC;
import br.gov.planejamento.siconv.med.medicao.dao.HistoricoMedicaoDAO;
import br.gov.planejamento.siconv.med.medicao.dao.ItemMedicaoDAO;
import br.gov.planejamento.siconv.med.medicao.dao.MedicaoDAO;
import br.gov.planejamento.siconv.med.medicao.dao.SubmetaDAO;
import br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum;
import br.gov.planejamento.siconv.med.medicao.entity.SituacaoSubmetaEnum;
import br.gov.planejamento.siconv.med.medicao.entity.SolicitanteVistoriaExtraEnum;
import br.gov.planejamento.siconv.med.medicao.entity.database.HistoricoMedicaoBD;
import br.gov.planejamento.siconv.med.medicao.entity.database.MedicaoBD;
import br.gov.planejamento.siconv.med.medicao.entity.database.SubmetaMedicaoBD;
import br.gov.planejamento.siconv.med.medicao.entity.dto.MedicaoAgrupadaDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.MedicaoDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.SubmetaMedicaoDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.SubmetaVrplDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.VistoriaExtraDTO;
import br.gov.planejamento.siconv.med.test.builder.SubmetaMedicaoDTOBuilder;

class MedicaoBCTest {

	final String CPF_RESPONSAVEL = "11111111111";
	final UsuarioLogado usuarioLogado = mock(UsuarioLogado.class);

	@Mock
	private Jdbi jdbi;

	@Mock
	private Handle handle;

	@Mock
	private MedicaoDAO medicaoDao;
	
	@Mock
	private ContratoDAO contratoDao;
	
	@Mock
	private SubmetaDAO submetaDao;
	
	@Mock
	private ItemMedicaoDAO itemMedicaoDao;
	
	@Mock
	private HistoricoMedicaoDAO historicoDao;
	
	@Mock
	private DocumentoComplementarDAO docComplementarDao;
	
	@Mock
	private VrplGRPCConsumer vrplGrpcConsumer;
	
	@Mock
	private ProjetoBasicoGRPCConsumer projetoBasicoConsumer;
	
	@Mock
	private ContratosGrpcConsumer contratosConsumer;
	
	@Mock
	private MaisBrasilGRPCConsumer maisBrasilCadastroConsumer;
	
	@Mock
	private ContratosBC contratoBC;
	
	@InjectMocks
	private EmpresaBC empresaBC;
	
	@Mock
	private HistoricoMedicaoBC historicoMedicaoBC;

    @Mock
	private ObservacaoBC observacaoBC;
    
    @Mock
	private DocumentoComplementarBC docComplementarBC;
	
	@Mock
	private SubmetaBC submetaBC;

	@InjectMocks
	private MedicaoBC medicaoBC;
	
	@Mock
	private SecurityContext securityContext;
	
	@Captor
	private ArgumentCaptor<MedicaoBD> medicaoCaptor;
	
	@Captor
	private ArgumentCaptor<HistoricoMedicaoBD> historicoCaptor;
	
	@Captor
	private ArgumentCaptor<ContratoBD> contratoCaptor;
	
	@BeforeEach
	void setup() throws Exception {

		MockitoAnnotations.initMocks(this);

		when(handle.attach(MedicaoDAO.class)).thenReturn(medicaoDao);
		when(jdbi.onDemand(MedicaoDAO.class)).thenReturn(medicaoDao);
		
		when(handle.attach(SubmetaDAO.class)).thenReturn(submetaDao);
		when(jdbi.onDemand(SubmetaDAO.class)).thenReturn(submetaDao);	
		
		when(handle.attach(ContratoDAO.class)).thenReturn(contratoDao);
		when(jdbi.onDemand(ContratoDAO.class)).thenReturn(contratoDao);

		when(handle.attach(DocumentoComplementarDAO.class)).thenReturn(docComplementarDao);
		when(jdbi.onDemand(DocumentoComplementarDAO.class)).thenReturn(docComplementarDao);
		
		when(handle.attach(ItemMedicaoDAO.class)).thenReturn(itemMedicaoDao);
		when(jdbi.onDemand(ItemMedicaoDAO.class)).thenReturn(itemMedicaoDao);
		
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
	
	/* *************************************************************
	 *               INICIAR COMPLEMENTAÇÃO 
	 * ************************************************************* */
	
	@Test
	void testIniciarComplementacaoEmpresaParametroIdMedicaoNulo() {

		Exception exception = assertThrows(NullPointerException.class,
				() -> medicaoBC.iniciarComplementacao(null));

		assertEquals("Parâmetro idMedicao não pode ser nulo", exception.getMessage());
	}

	@Test
	void testIniciarComplementacaoEmpresaMedicaoNaoEncontrada() {

		Long idMedicao = 999L;

		assertThrowsMedicaoRestException(MessageKey.ERRO_MEDICAO_NAO_ENCONTRADA,
				() -> medicaoBC.iniciarComplementacao(idMedicao));
	}
	

	@Test
	void testIniciarComplementacaoEmpresaMedicaoSituacaoInvalida() {

		MedicaoBD medicao = newMedicaoBuilder().setId(1L).comSituacao(EC).create();

		when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);
		when(securityContext.isUserInProfile(EMPRESA)).thenReturn(true);

		assertThrowsMedicaoRestException(MessageKey.ERRO_SITUACAO_INVALIDA_PARA_INICIAR_COMPLEMENTACAO_EMPRESA,
				() -> medicaoBC.iniciarComplementacao(medicao.id));
	}

	@Test
	void testIniciarComplementacaoEmpresaMedicaoAcumulada() {

		MedicaoBD medicao = newMedicaoBuilder().setId(2L)
				.setAgrupadora(1L).comSituacao(ECE).create();

		when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);
		when(securityContext.isUserInProfile(EMPRESA)).thenReturn(true);

		assertThrowsMedicaoRestException(MessageKey.ERRO_COMPLEMENTACAO_MEDICAO_ACUMULADA,
				() -> medicaoBC.iniciarComplementacao(medicao.id));
	}

	@Test
	void testIniciarComplementacaoEmpresaMedicaoNaoAgrupada() {
		
		Short nrSequencial = 1;
		
		MedicaoBD medicao = newMedicaoBuilder().setId(1L)
				.setNrSequencial(nrSequencial)
				.comSituacao(ECE).create();

		when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);
		when(securityContext.isUserInProfile(EMPRESA)).thenReturn(true);

		medicaoBC.iniciarComplementacao(medicao.id);

		verify(medicaoDao, times(1)).alterar(medicaoCaptor.capture());
		assertEquals(SituacaoMedicaoEnum.CE, medicaoCaptor.getValue().getSituacao());

		verify(historicoMedicaoBC, times(1)).inserir(historicoCaptor.capture());
		assertEquals(medicao.nrSequencial, historicoCaptor.getValue().getNrSequencial());
		assertEquals(medicao.idContratoMedicao, historicoCaptor.getValue().getIdContratoMedicao());
		assertEquals(CE, historicoCaptor.getValue().getSituacao());

		verify(observacaoBC, times(1)).bloquearObservacao(handle, medicao.id, PROPONENTE_CONVENENTE);
	}

	@Test
	void testIniciarComplementacaoConvenenteMedicaoSituacaoInvalida() {

		MedicaoBD medicao = newMedicaoBuilder().setId(1L).comSituacao(ACT).create();

		when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);
		when(securityContext.isUserInProfile(PROPONENTE_CONVENENTE)).thenReturn(true);

		assertThrowsMedicaoRestException(MessageKey.ERRO_SITUACAO_INVALIDA_PARA_INICIAR_COMPLEMENTACAO_CONVENENTE,
				() -> medicaoBC.iniciarComplementacao(medicao.id));
	}

	@Test
	void testIniciarComplementacaoConvenenteMedicaoAcumulada() {
		
		MedicaoBD medicao = newMedicaoBuilder().setId(2L)
				.setAgrupadora(1L).comSituacao(ECC).create();

		when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);
		when(securityContext.isUserInProfile(PROPONENTE_CONVENENTE)).thenReturn(true);

		assertThrowsMedicaoRestException(MessageKey.ERRO_COMPLEMENTACAO_MEDICAO_ACUMULADA,
				() -> medicaoBC.iniciarComplementacao(medicao.id));
	}

	@Test
	void testIniciarComplementacaoConvenenteMedicaoNaoAgrupada() {

		Short nrSequencial = 1;
		
		MedicaoBD medicao = newMedicaoBuilder().setId(1L)
				.setNrSequencial(nrSequencial).comSituacao(ECC).create();
		
		when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);
		when(securityContext.isUserInProfile(PROPONENTE_CONVENENTE)).thenReturn(true);

		medicaoBC.iniciarComplementacao(medicao.id);

		verify(medicaoDao, times(1)).alterar(medicaoCaptor.capture());
		assertEquals(CC, medicaoCaptor.getValue().getSituacao());

		verify(historicoMedicaoBC, times(1)).inserir(historicoCaptor.capture());
		assertEquals(medicao.nrSequencial, historicoCaptor.getValue().getNrSequencial());
		assertEquals(medicao.idContratoMedicao, historicoCaptor.getValue().getIdContratoMedicao());
		assertEquals(CC, historicoCaptor.getValue().getSituacao());

		verify(observacaoBC, times(1)).bloquearObservacao(handle, medicao.id, CONCEDENTE);
		verify(observacaoBC, times(1)).bloquearObservacao(handle, medicao.id, MANDATARIA);
	}
	
	/* *************************************************************
	 *               ALTERAR CONCEDENTE MANDATARIA 
	 * ************************************************************* */
    //Casos1: Medicao nao pode ser alterada
    //Caso2: Meidcao Bloqueada
    //Caso3: Data Vistoria Extra Invalida
    //Caso4: Vistoria Extra sem Solicitante
    //Caso5: Validar Retorno Medicao

    //Casos1: Medicao nao pode ser alterada
	@Test
	void testAlterarConcedenteMandatariaMedicaoAgrupada() {
				
		VistoriaExtraDTO vistoriaExtraDTO = newVistoriaBuilder().create();
		
		MedicaoBD medicao = newMedicaoBuilder().setId(2L)
				.setAgrupadora(1L)
				.comSituacao(SituacaoMedicaoEnum.ATD).create();
		
		when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);		
		
		assertThrowsMedicaoRestException(MessageKey.ERRO_MEDICAO_NAO_PODE_SER_ALTERADA,
				() -> medicaoBC.alterarConcedenteMandataria(vistoriaExtraDTO, medicao.getId()));
	}
	
    //Casos1: Medicao nao pode ser alterada
	@Test
	void testAlterarConcedenteMandatariaSituacaoMedicaoDiferenteEmAnalisePeloConcedente() {
		
		VistoriaExtraDTO vistoriaExtraDTO = newVistoriaBuilder().create();
		
		MedicaoBD medicao = newMedicaoBuilder().setId(1L)
				.comSituacao(SituacaoMedicaoEnum.ATD).create();
		
		when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);		
		
		assertThrowsMedicaoRestException(MessageKey.ERRO_MEDICAO_NAO_PODE_SER_ALTERADA,
				() -> medicaoBC.alterarConcedenteMandataria(vistoriaExtraDTO, medicao.getId()));	
        			
	}	
	
    //Caso2: Medicao Bloqueada
	@Test
	void testAlterarConcedenteMandatariaMedicaoBloqueada() {
		
		VistoriaExtraDTO vistoriaExtraDTO = newVistoriaBuilder().create();
		
		MedicaoBD medicao = newMedicaoBuilder().setId(1L)
				.setBloqueada(true)
				.comSituacao(SituacaoMedicaoEnum.AC).create();
		
		when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);		
		
		assertThrowsMedicaoRestException(MessageKey.ERRO_MEDICAO_BLOQUEADA,
				() -> medicaoBC.alterarConcedenteMandataria(vistoriaExtraDTO, medicao.getId()));        			
	}	

	
    //Caso3: Data Vistoria Extra Menor que Data Inicio de Objeto
	@Test
	void testAlterarConcedenteMandatariaDataVistoriaExtraAnteriorDataInicioObra() {
		
		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setDataInicioObra(LocalDate.of(2019, 1, 5));

		VistoriaExtraDTO vistoriaExtraDTO = newVistoriaBuilder()
				.setSolicitante(SolicitanteVistoriaExtraEnum.CCD)
				.isVistoriaExtra(true)
				.setDataVistoria(medicaoDTO.getDataInicioObra().minusDays(5))
				.setVersao(1L).create();
		
		MedicaoBD medicao = newMedicaoBuilder().setId(1L)
				.comSituacao(SituacaoMedicaoEnum.AC).create();
		
		when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);
		when(medicaoDao.obterMedicao(medicao.id)).thenReturn(medicaoDTO);
		
		when(securityContext.isUserInProfile(CONCEDENTE)).thenReturn(true);		
		when(securityContext.isUserInProfile(MANDATARIA)).thenReturn(true);
		
		assertThrowsMedicaoRestException(MessageKey.ERRO_DATA_VISTORIA_EXTRA_INVALIDA,
				() -> medicaoBC.alterarConcedenteMandataria(vistoriaExtraDTO, medicao.getId()));
	}		
	
	//Caso4: Vistoria Extra sem Solicitante
	@Test
	void testAlterarConcedenteMandatariaVistoriaExtraSemSolicitante() {
		
		VistoriaExtraDTO vistoriaExtraDTO = newVistoriaBuilder()
				.isVistoriaExtra(true)
				.setVersao(1L).create();
		
		MedicaoBD medicao = newMedicaoBuilder().setId(1L)
				.comSituacao(SituacaoMedicaoEnum.AC).create();
		
		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setDataInicioObra(LocalDate.of(2019, 1, 5));
		
		when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);
		when(medicaoDao.obterMedicao(medicao.id)).thenReturn(medicaoDTO);
		
		when(securityContext.isUserInProfile(CONCEDENTE)).thenReturn(true);		
		when(securityContext.isUserInProfile(MANDATARIA)).thenReturn(true);
		
		assertThrowsMedicaoRestException(MessageKey.ERRO_CAMPO_SOLICITANTE_VISTORIA_EXTRA_OBRIGATORIO,
				() -> medicaoBC.alterarConcedenteMandataria(vistoriaExtraDTO, medicao.getId()));
	}	
	
	//Caso: Vistoria Extra versão nula
		@Test
		void testAlterarConcedenteMandatariaVistoriaExtraSemVersao() {
			
			VistoriaExtraDTO vistoriaExtraDTO = newVistoriaBuilder()
					.isVistoriaExtra(true).create();
			
			MedicaoBD medicao = newMedicaoBuilder().setId(1L)
					.comSituacao(SituacaoMedicaoEnum.AC).create();
			
			MedicaoDTO medicaoDTO = new MedicaoDTO();
			medicaoDTO.setDataInicioObra(LocalDate.of(2019, 1, 5));
			
			when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);
			when(medicaoDao.obterMedicao(medicao.id)).thenReturn(medicaoDTO);
			
			when(securityContext.isUserInProfile(CONCEDENTE)).thenReturn(true);		
			when(securityContext.isUserInProfile(MANDATARIA)).thenReturn(true);
			
			assertThrowsMedicaoRestException(MessageKey.ERRO_PARAMETRO_OBRIGATORIO_NAO_INFORMADO,
					() -> medicaoBC.alterarConcedenteMandataria(vistoriaExtraDTO, medicao.getId()));
		}	

    //Caso5: Validar Retorno Medicao	
	@Test
	void testAlterarConcedenteMandatariaValidarRetorno() {
	
		VistoriaExtraDTO vistoriaExtraDTO = newVistoriaBuilder()
				.setSolicitante(SolicitanteVistoriaExtraEnum.CCD)
				.isVistoriaExtra(true)
				.setVersao(1L).create();
		
		MedicaoBD medicao = newMedicaoBuilder().setId(1L)
				.comSituacao(SituacaoMedicaoEnum.AC).create();
		
		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setDataInicioObra(LocalDate.of(2019, 1, 5));
		
		when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);
		when(medicaoDao.obterMedicao(medicao.id)).thenReturn(medicaoDTO);		
		when(securityContext.isUserInProfile(CONCEDENTE)).thenReturn(true);		
		when(securityContext.isUserInProfile(MANDATARIA)).thenReturn(true);
		
		medicaoBC.alterarConcedenteMandataria(vistoriaExtraDTO, medicao.getId());

		verify(medicaoDao, times(1)).alterar(medicaoCaptor.capture());
		assertEquals(SituacaoMedicaoEnum.AC, medicaoCaptor.getValue().getSituacao());
		assertEquals(SolicitanteVistoriaExtraEnum.CCD, medicaoCaptor.getValue().getSolicitanteVistoriaExtra());
		assertEquals(vistoriaExtraDTO.getDataVistoriaExtra(), medicaoCaptor.getValue().getDataVistoriaExtra());						
	}	
	
	/* *************************************************************
	 *                    ACEITAR MEDIÇÃO 
	 * ************************************************************* */
	
	//Caso1: Erro Aceite Medicao não Permitida
	//Caso2: Submetas Assinadas pelo Convenente e não assinadas pelo Concedente/Mandatária
	//Caso3: Submetas em Rascunho
	//Caso4: Submetas Agrupadas Assinadas pelo Convenente e nao pelo Concedente/Mandadataria
    //Caso5: Data Vistoria Extra Nula
    //Caso6: Data Vistoria Extra Menor que Data Inicio de Objeto
	//Caso7: Vistoria Extra sem Solicitante
	//Caso8: Validar Alterar medicao
	//Caso9: Validar Alterar Histórico
	//Caso10: Validar Alterar Situacoes Medicoes Acumuladas
	//Caso11: Validar Retorno Aceitar Medicao
	
	//Caso1: Erro Medicao Agrupada	
	@Test
	void testAceitarConcedenteMandatariaMedicaoAgrupada() {

		VistoriaExtraDTO vistoriaExtraDTO = newVistoriaBuilder()
				.setSolicitante(SolicitanteVistoriaExtraEnum.CCD)
				.isVistoriaExtra(true).create();
		
		MedicaoBD medicao = newMedicaoBuilder().setId(2L)
				.setAgrupadora(1L)
				.comSituacao(SituacaoMedicaoEnum.ATD).create();
		
		when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);		
		
		assertThrowsMedicaoRestException(MessageKey.ERRO_ACEITE_MEDICAO_NAO_PERMITIDA,
				() -> medicaoBC.aceitar(vistoriaExtraDTO, medicao.getId()));
	}
	
	//Caso1: Erro Situacao Diferente Em Analise Concedente/Mandataria	
	@Test
	void testAceitarConcedenteMandatariaSituacaoMedicaoDiferenteEmAnaliseCocedenteMandataria() {
		
		VistoriaExtraDTO vistoriaExtraDTO = newVistoriaBuilder()
				.setSolicitante(SolicitanteVistoriaExtraEnum.CCD)
				.isVistoriaExtra(true).create();
		
		MedicaoBD medicao = newMedicaoBuilder().setId(1L)
				.comSituacao(SituacaoMedicaoEnum.ATD).create();
		
		when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);		
		
		assertThrowsMedicaoRestException(MessageKey.ERRO_ACEITE_MEDICAO_NAO_PERMITIDA,
				() -> medicaoBC.aceitar(vistoriaExtraDTO, medicao.getId()));	
		
	}
	
	//Caso2: Submetas Assinadas pelo Convenente e não assinadas pelo Concedente/Mandatária
	@Test
	void testAceitarConcedenteMandatariaSubmetasAssinadasConvenenteNAssinadasConcedente() {

		Long idSubmetaMedicao = 1L;
		
		VistoriaExtraDTO vistoriaExtraDTO = newVistoriaBuilder().create();		
		
		MedicaoBD medicao = newMedicaoBuilder().setId(1L)
				.comSituacao(SituacaoMedicaoEnum.AC).create();

		//Lista de submetas para validar assinaturas e rascunho.
		List<SubmetaMedicaoBD> listaSubmetasMedicao = new ArrayList<SubmetaMedicaoBD>();
		SubmetaMedicaoBD submetaMedicaoBD = newSubmetaBuilder()
			.setIdMedicao(medicao.id)
			.setIdSubmetaMedicao(idSubmetaMedicao)
			.setSituacaoConvenente(SituacaoSubmetaEnum.ASS)
			.create();
		listaSubmetasMedicao.add(submetaMedicaoBD);		

		when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);
		when(submetaDao.buscarListaSubmetasporMedicao(medicao.getId())).thenReturn(listaSubmetasMedicao);
		
		assertThrowsMedicaoRestException(MessageKey.ERRO_SUBMETAS_ASSINADAS_CONVENENTE_NAO_ASSINADAS_CONCEDENTE_MANDATARIA,
				() -> medicaoBC.aceitar(vistoriaExtraDTO, medicao.id));	
		
	}

	//Caso3: Submetas em Rascunho
	@Test
	void testAceitarConcedenteMandatariaSubmetasRascunho() {

		Long idSubmetaMedicao = 1L;
		
		VistoriaExtraDTO vistoriaExtraDTO = newVistoriaBuilder().create();
		
		MedicaoBD medicao = newMedicaoBuilder().setId(1L)
				.comSituacao(SituacaoMedicaoEnum.AC).create();

		//Lista de submetas para validar assinaturas e rascunho.
		List<SubmetaMedicaoBD> listaSubmetasMedicao = new ArrayList<SubmetaMedicaoBD>();
		SubmetaMedicaoBD submetaMedicaoBD = newSubmetaBuilder()
				.setIdMedicao(medicao.id)
				.setIdSubmetaMedicao(idSubmetaMedicao)
				.setSituacaoConcedente(SituacaoSubmetaEnum.RAS)
				.create();
		listaSubmetasMedicao.add(submetaMedicaoBD);		
		
		when(submetaDao.buscarListaSubmetasporMedicao(medicao.id)).thenReturn(listaSubmetasMedicao);	
		when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);
		
		assertThrowsMedicaoRestException(MessageKey.ERRO_SUBMETAS_RASCUNHO_CONCEDENTE_MADATARIA,
				() -> medicaoBC.aceitar(vistoriaExtraDTO, medicao.getId()));	
		
	}
	
	//Caso4: Submetas Agrupadas Assinadas pelo Convenente e nao pelo Concedente/Mandadataria
	@Test
	void testAceitarConcedenteMandatariaSubmetasAgrupadasAssinadasConvenenteNConcedente() {
		
		//Medicao Agrupadora
		VistoriaExtraDTO vistoriaExtraDTO = newVistoriaBuilder().create();		
		
		MedicaoBD medicao = newMedicaoBuilder().setId(1L)
				.comSituacao(SituacaoMedicaoEnum.AC).create();
		
		//Lista de submetas da medição agrupadora
		List<SubmetaMedicaoBD> listaSubmetasMedicao = new ArrayList<SubmetaMedicaoBD>();
		SubmetaMedicaoBD submetaMedicaoBD = newSubmetaBuilder()
				.setIdMedicao(medicao.id)
				.setIdSubmetaMedicao(1L)
				.setIdSubmetaVrpl(1L)
				.setSituacaoConvenente(SituacaoSubmetaEnum.ASS)
				.setSituacaoConcedente(SituacaoSubmetaEnum.ASS)
				.create();
		listaSubmetasMedicao.add(submetaMedicaoBD);		
		
		//Lista de submetas da medição agrupada
		List<SubmetaMedicaoBD> listaSubmetasMedicaoAgrupada = new ArrayList<SubmetaMedicaoBD>();
		SubmetaMedicaoBD submetaMedicaoAgrupadaBD = newSubmetaBuilder()
			.setIdMedicao(medicao.id)
			.setIdSubmetaMedicao(1L)
			.setSituacaoConvenente(SituacaoSubmetaEnum.ASS)
			.create();
		listaSubmetasMedicaoAgrupada.add(submetaMedicaoAgrupadaBD);		
		
		
		when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);
		when(submetaDao.buscarListaSubmetasporMedicao(medicao.id)).thenReturn(listaSubmetasMedicao);		
		when(submetaDao.listarSubmetasMedicoesAcumuladas(medicao.id)).thenReturn(listaSubmetasMedicaoAgrupada);
		
		assertThrowsMedicaoRestException(MessageKey.ERRO_SUBMETAS_ACUMULADAS_ASSINADAS_CONVENENTE_NAO_ASSINADAS_CONCEDENTE_MANDATARIA,
				() -> medicaoBC.aceitar(vistoriaExtraDTO, medicao.getId()));	
		
	}

    //Caso5: Data Vistoria Extra Nula
	@Test
	void testAceitarConcedenteMandatariaDataVistoriaExtraNula() {
		
		VistoriaExtraDTO vistoriaExtraDTO = newVistoriaBuilder()
				.setSolicitante(SolicitanteVistoriaExtraEnum.CCD)
				.isVistoriaExtra(true).create();
		
		MedicaoBD medicao = newMedicaoBuilder().setId(1L)
				.comSituacao(SituacaoMedicaoEnum.AC).create();
		
		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setDataInicioObra(LocalDate.of(2019, 1, 5));
		
		when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);
		when(medicaoDao.obterMedicao(medicao.id)).thenReturn(medicaoDTO);
		
		when(securityContext.isUserInProfile(CONCEDENTE)).thenReturn(true);		
		when(securityContext.isUserInProfile(MANDATARIA)).thenReturn(true);
		
		assertThrowsMedicaoRestException(MessageKey.ERRO_PARAMETRO_OBRIGATORIO_NAO_INFORMADO,
				() -> medicaoBC.aceitar(vistoriaExtraDTO, medicao.getId()));	
        			
	}
	
    //Caso6: Data Vistoria Extra Menor que Data Inicio de Objeto
	@Test
	void testAceitarConcedenteMandatariaDataVistoriaExtraAnteriorDataInicioObra() {
		
		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setDataInicioObra(LocalDate.of(2019, 1, 5));
		
		VistoriaExtraDTO vistoriaExtraDTO = newVistoriaBuilder()
				.setSolicitante(SolicitanteVistoriaExtraEnum.CCD)
				.setDataVistoria(medicaoDTO.getDataInicioObra().minusDays(5))
				.isVistoriaExtra(true)
				.setVersao(1L).create();
		
		MedicaoBD medicao = newMedicaoBuilder().setId(1L)
				.comSituacao(SituacaoMedicaoEnum.AC).create();
		
		when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);
		when(medicaoDao.obterMedicao(medicao.id)).thenReturn(medicaoDTO);
		
		when(securityContext.isUserInProfile(CONCEDENTE)).thenReturn(true);		
		when(securityContext.isUserInProfile(MANDATARIA)).thenReturn(true);
		
		assertThrowsMedicaoRestException(MessageKey.ERRO_DATA_VISTORIA_EXTRA_INVALIDA,
				() -> medicaoBC.aceitar(vistoriaExtraDTO, medicao.getId()));
	}		
	
	//Caso7: Vistoria Extra sem Solicitante
	@Test
	void testAceitarConcedenteMandatariaVistoriaExtraSemSolicitante() {
		
		VistoriaExtraDTO vistoriaExtraDTO = newVistoriaBuilder()
				.setDataVistoria(LocalDate.now())
				.isVistoriaExtra(true)
				.setVersao(1L).create();
		
		MedicaoBD medicao = newMedicaoBuilder().setId(1L)
				.comSituacao(SituacaoMedicaoEnum.AC).create();
		
		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setDataInicioObra(LocalDate.of(2019, 1, 5));
		
		when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);
		when(medicaoDao.obterMedicao(medicao.id)).thenReturn(medicaoDTO);
		
		when(securityContext.isUserInProfile(CONCEDENTE)).thenReturn(true);		
		when(securityContext.isUserInProfile(MANDATARIA)).thenReturn(true);
		
		assertThrowsMedicaoRestException(MessageKey.ERRO_CAMPO_SOLICITANTE_VISTORIA_EXTRA_OBRIGATORIO,
				() -> medicaoBC.aceitar(vistoriaExtraDTO, medicao.getId()));
	}	
	
    //Caso8: Validar Retorno Aceitar Medicao	
	@Test
	void testAceitarConcedenteMandatariaValidarRetorno() {

		VistoriaExtraDTO vistoriaExtraDTO = newVistoriaBuilder()
				.setDataVistoria(LocalDate.now())
				.setSolicitante(SolicitanteVistoriaExtraEnum.CCD)
				.isVistoriaExtra(true)
				.setVersao(1L).create();
		
		//Medicao Agrupadora		
		Short nrSequencial = 2;
		
		MedicaoBD medicao = newMedicaoBuilder().setId(2L)
				.setNrSequencial(nrSequencial)
				.comSituacao(SituacaoMedicaoEnum.AC).create();
		
		//Medicao Agrupada	
		Short nrSequencialFilha = 1;
		
		MedicaoBD medicaoFilha = newMedicaoBuilder().setId(1L)
				.setNrSequencial(nrSequencialFilha)
				.comSituacao(SituacaoMedicaoEnum.AC).create();
		
		List<MedicaoBD> listaMedicoesAgrupadas = new ArrayList<MedicaoBD>();
		listaMedicoesAgrupadas.add(medicaoFilha);
		
		//Lista de submetas da medição agrupada
		List<SubmetaMedicaoBD> listaSubmetasMedicaoAgrupada = new ArrayList<SubmetaMedicaoBD>();
		SubmetaMedicaoBD submetaMedicaoAgrupadaBD = newSubmetaBuilder()
				.setIdMedicao(medicaoFilha.id)
				.setIdSubmetaMedicao(1L)
				.setSituacaoConvenente(SituacaoSubmetaEnum.ASS)
				.setSituacaoConcedente(SituacaoSubmetaEnum.ASS)
				.create();
		listaSubmetasMedicaoAgrupada.add(submetaMedicaoAgrupadaBD);	
		
		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setDataInicioObra(LocalDate.of(2019, 1, 5));

		when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);
		when(medicaoDao.obterMedicao(medicao.id)).thenReturn(medicaoDTO);	
		when(medicaoDao.listarMedicoesAcumuladas(medicao.id)).thenReturn(listaMedicoesAgrupadas);
		when(securityContext.isUserInProfile(CONCEDENTE)).thenReturn(true);		
		when(securityContext.isUserInProfile(MANDATARIA)).thenReturn(true);
		
		medicaoBC.aceitar(vistoriaExtraDTO, medicao.getId());

		verify(medicaoDao, times(2)).alterar(medicaoCaptor.capture());
		
		assertEquals(SituacaoMedicaoEnum.ACT, medicaoCaptor.getAllValues().get(0).getSituacao());
		assertEquals(SolicitanteVistoriaExtraEnum.CCD, medicaoCaptor.getAllValues().get(0).getSolicitanteVistoriaExtra());
		assertEquals(vistoriaExtraDTO.getDataVistoriaExtra(), medicaoCaptor.getAllValues().get(0).getDataVistoriaExtra());
		
		assertEquals(SituacaoMedicaoEnum.ACT, medicaoCaptor.getAllValues().get(1).getSituacao());
        assertEquals(null, medicaoCaptor.getAllValues().get(1).getSolicitanteVistoriaExtra());
        assertEquals(null, medicaoCaptor.getAllValues().get(1).getDataVistoriaExtra());
		
		verify(historicoMedicaoBC, times(2)).inserir(historicoCaptor.capture());
		
		assertEquals(medicao.getNrSequencial(), historicoCaptor.getAllValues().get(0).getNrSequencial());
		assertEquals(medicao.getIdContratoMedicao(), historicoCaptor.getAllValues().get(0).getIdContratoMedicao());
		assertEquals(SituacaoMedicaoEnum.ACT, historicoCaptor.getAllValues().get(0).getSituacao());
		
		assertEquals(medicaoFilha.getNrSequencial(), historicoCaptor.getAllValues().get(1).getNrSequencial());
		assertEquals(medicaoFilha.getIdContratoMedicao(), historicoCaptor.getAllValues().get(1).getIdContratoMedicao());
		assertEquals(SituacaoMedicaoEnum.ACT, historicoCaptor.getAllValues().get(1).getSituacao());	
	}
	
	/* *************************************************************
	 *                    ATESTAR MEDIÇÃO 
	 * ************************************************************* */
	
	
	@Test
	void testAtestarMedicaoNaoEncontrada() {
		Long idMedicao = 999L;

		assertThrowsMedicaoRestException(MessageKey.ERRO_MEDICAO_NAO_ENCONTRADA,
				() -> medicaoBC.atestar(idMedicao));
	}
	
	@Test
	void testAtestarMedicaoSituacaoNaoPermitida() {
		
		MedicaoBD medicao = newMedicaoBuilder().setId(1L)
				.comSituacao(SituacaoMedicaoEnum.EM).create();

		when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);

		assertThrowsMedicaoRestException(MessageKey.ERRO_ATESTE_MEDICAO_NAO_PERMITIDA,
				() -> medicaoBC.atestar(medicao.id));
	}
	
	@Test
	void testAtestarMedicaoNaoPermitidaAgrupada() {
		
		MedicaoBD medicao = newMedicaoBuilder().setId(2L)
				.setAgrupadora(1L)
				.comSituacao(SituacaoMedicaoEnum.AT).create();

		when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);

		assertThrowsMedicaoRestException(MessageKey.ERRO_ATESTE_MEDICAO_NAO_PERMITIDA,
				() -> medicaoBC.atestar(medicao.id));
	}
	
	@Test
	void testAtestarMedicaoSubmetasNaoAssinadasConvenente() {
		
		MedicaoBD medicao = newMedicaoBuilder().setId(1L)
				.comSituacao(SituacaoMedicaoEnum.AT).create();

		SubmetaMedicaoBD submetaMedicaoBD = newSubmetaBuilder()
				.setIdMedicao(medicao.id)
				.setIdSubmetaMedicao(1L)
				.setSituacaoEmpresa(SituacaoSubmetaEnum.ASS)
				.setSituacaoConvenente(SituacaoSubmetaEnum.RAS)
				.create();
		
		List<SubmetaMedicaoBD> submetasMedicao = new ArrayList<SubmetaMedicaoBD>();
		submetasMedicao.add(submetaMedicaoBD);
		
		when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);
		when(submetaDao.buscarListaSubmetasporMedicao(medicao.id)).thenReturn(submetasMedicao);

		assertThrowsMedicaoRestException(MessageKey.ERRO_SUBMETAS_ASSINADAS_EMPRESA_NAO_ASSINADAS_CONVENENTE,
				() -> medicaoBC.atestar(medicao.id));
	}
	
	@Test
	void testAtestarMedicaoSubmetasRascunhoConvenente() {
		
		MedicaoBD medicao = newMedicaoBuilder().setId(1L)
				.comSituacao(SituacaoMedicaoEnum.AT).create();

		SubmetaMedicaoBD submetaMedicaoBD = newSubmetaBuilder()
				.setIdMedicao(medicao.id)
				.setIdSubmetaMedicao(1L)
				.setSituacaoConvenente(SituacaoSubmetaEnum.RAS)
				.create();
		
		List<SubmetaMedicaoBD> submetasMedicao = new ArrayList<SubmetaMedicaoBD>();
		submetasMedicao.add(submetaMedicaoBD);
		
		when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);
		when(submetaDao.buscarListaSubmetasporMedicao(medicao.id)).thenReturn(submetasMedicao);

		assertThrowsMedicaoRestException(MessageKey.ERRO_SUBMETAS_RASCUNHO_CONVENENTE,
				() -> medicaoBC.atestar(medicao.id));
	}
	
	@Test
	void testAtestarMedicaoPLESubmetasMedicoesAcumuladasNaoAssinadasConvenente() {

		ContratoBD contratoMedicao = newContratoMedicaoBuilder().setId(1L)
				.setAcompanhadoPorEventos(TRUE)
				.setContratoSiconv(7L).create();
		
		MedicaoBD medicao = newMedicaoBuilder().setId(1L)
				.setMedContrato(contratoMedicao.id)
				.comSituacao(SituacaoMedicaoEnum.AT).create();
		
		SubmetaMedicaoBD submetaMedicaoBD = newSubmetaBuilder()
				.setIdMedicao(medicao.id)
				.setIdSubmetaMedicao(1L)
				.setIdSubmetaVrpl(1L)
				.setSituacaoEmpresa(SituacaoSubmetaEnum.ASS)
				.setSituacaoConvenente(SituacaoSubmetaEnum.ASS)
				.create();
		
		SubmetaMedicaoBD submetaMedicaoBD2 = newSubmetaBuilder()
				.setIdMedicao(medicao.id)
				.setIdSubmetaMedicao(2L)
				.setIdSubmetaVrpl(2L)
				.setSituacaoEmpresa(SituacaoSubmetaEnum.ASS)
				.create();
		
		List<SubmetaMedicaoBD> submetasMedicao = new ArrayList<SubmetaMedicaoBD>();
		submetasMedicao.add(submetaMedicaoBD);
			
		List<SubmetaMedicaoBD> submetasMedicao2 = new ArrayList<SubmetaMedicaoBD>();
		submetasMedicao2.add(submetaMedicaoBD2);

		when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);
		when(submetaDao.buscarListaSubmetasporMedicao(medicao.id)).thenReturn(submetasMedicao);
		when(submetaDao.listarSubmetasMedicoesAcumuladas(medicao.id)).thenReturn(submetasMedicao2);
		when(contratoDao.consultarContrato(medicao.id)).thenReturn(contratoMedicao);

		assertThrowsMedicaoRestException(MessageKey.ERRO_SUBMETAS_ACUMULADAS_ASSINADAS_EMPRESA_NAO_ASSINADAS_CONVENENTE,
				() -> medicaoBC.atestar(medicao.id));
	}
	
	@Test
	void testAtestarMedicaoBMSubmetasMedicoesAcumuladasNaoAssinadasConvenente() {

		ContratoBD contratoMedicao = newContratoMedicaoBuilder().setId(1L).setAcompanhadoPorEventos(FALSE)
				.setContratoSiconv(7L).create();

		MedicaoBD medicao = newMedicaoBuilder()
				.setId(1L).setMedContrato(contratoMedicao.id)
				.comSituacao(SituacaoMedicaoEnum.AT).create();

		SubmetaMedicaoBD submetaMedicaoBD = newSubmetaBuilder()
				.setIdMedicao(medicao.id).setIdSubmetaMedicao(1L)
				.setIdSubmetaVrpl(1L).setSituacaoEmpresa(SituacaoSubmetaEnum.ASS)
				.setSituacaoConvenente(SituacaoSubmetaEnum.ASS).create();

		SubmetaMedicaoBD submetaMedicaoBD2 = newSubmetaBuilder()
				.setIdMedicao(medicao.id).setIdSubmetaMedicao(2L)
				.setIdSubmetaVrpl(2L)
				.setSituacaoEmpresa(SituacaoSubmetaEnum.ASS).create();

		List<SubmetaMedicaoBD> submetasMedicao = new ArrayList<SubmetaMedicaoBD>();
		submetasMedicao.add(submetaMedicaoBD);

		List<SubmetaMedicaoBD> submetasMedicao2 = new ArrayList<SubmetaMedicaoBD>();
		submetasMedicao2.add(submetaMedicaoBD2);

		when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);
		when(submetaDao.buscarListaSubmetasporMedicao(medicao.id)).thenReturn(submetasMedicao);
		when(submetaDao.listarSubmetasMedicoesAcumuladas(medicao.id)).thenReturn(submetasMedicao2);
		when(contratoDao.consultarContrato(medicao.id)).thenReturn(contratoMedicao);

		assertThrowsMedicaoRestException(MessageKey.ERRO_SUBMETAS_ACUMULADAS_ASSINADAS_EMPRESA_NAO_ASSINADAS_CONVENENTE,
				() -> medicaoBC.atestar(medicao.id));
	}

	@Test
	void testAtestarMedicaoBMSubmetasMedicoesAcumuladasRascunhoConvenente() {

		ContratoBD contratoMedicao = newContratoMedicaoBuilder()
				.setId(1L)
				.setAcompanhadoPorEventos(FALSE)
				.setContratoSiconv(7L).create();

		MedicaoBD medicao = newMedicaoBuilder()
				.setId(1L)
				.setMedContrato(contratoMedicao.id)
				.comSituacao(SituacaoMedicaoEnum.AT).create();

		SubmetaMedicaoBD submetaMedicaoBD = newSubmetaBuilder()
				.setIdMedicao(medicao.id).setIdSubmetaMedicao(1L)
				.setIdSubmetaVrpl(1L).setSituacaoEmpresa(SituacaoSubmetaEnum.ASS)
				.setSituacaoConvenente(SituacaoSubmetaEnum.ASS).create();

		SubmetaMedicaoBD submetaMedicaoBD2 = newSubmetaBuilder()
				.setIdMedicao(medicao.id)
				.setIdSubmetaMedicao(2L)
				.setIdSubmetaVrpl(2L)
				.setSituacaoEmpresa(SituacaoSubmetaEnum.ASS)
				.setSituacaoConvenente(SituacaoSubmetaEnum.RAS).create();

		List<SubmetaMedicaoBD> submetasMedicao = new ArrayList<SubmetaMedicaoBD>();
		submetasMedicao.add(submetaMedicaoBD);

		List<SubmetaMedicaoBD> submetasMedicao2 = new ArrayList<SubmetaMedicaoBD>();
		submetasMedicao2.add(submetaMedicaoBD2);

		when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);
		when(submetaDao.buscarListaSubmetasporMedicao(medicao.id)).thenReturn(submetasMedicao);
		when(submetaDao.listarSubmetasMedicoesAcumuladas(medicao.id)).thenReturn(submetasMedicao2);
		when(contratoDao.consultarContrato(medicao.id)).thenReturn(contratoMedicao);

		assertThrowsMedicaoRestException(MessageKey.ERRO_SUBMETAS_RASCUNHO_CONVENENTE,
				() -> medicaoBC.atestar(medicao.id));
	}

	@Test
	void testAtestarComplementacaoBMADMSubmetasMedicoesAcumuladasRascunhoConvenente() {

		ContratoBD contratoMedicao = newContratoMedicaoBuilder()
				.setId(1L)
				.setAcompanhadoPorEventos(FALSE)
				.setContratoSiconv(7L).create();

		MedicaoBD medicao = newMedicaoBuilder()
				.setId(1L)
				.setMedContrato(contratoMedicao.id)
				.comSituacao(SituacaoMedicaoEnum.CC)
				.setPermiteComplementacaoValor(TRUE).create();

		SubmetaMedicaoBD submetaMedicaoBD = newSubmetaBuilder()
				.setIdMedicao(medicao.id)
				.setIdSubmetaMedicao(1L)
				.setIdSubmetaVrpl(1L)
				.setSituacaoEmpresa(SituacaoSubmetaEnum.ASS)
				.setSituacaoConvenente(SituacaoSubmetaEnum.ASS).create();

		SubmetaMedicaoBD submetaMedicaoBD2 = newSubmetaBuilder()
				.setIdMedicao(medicao.id)
				.setIdSubmetaMedicao(2L)
				.setIdSubmetaVrpl(2L)
				.setSituacaoEmpresa(SituacaoSubmetaEnum.ASS)
				.setSituacaoConvenente(SituacaoSubmetaEnum.RAS).create();

		List<SubmetaMedicaoBD> submetasMedicao = new ArrayList<SubmetaMedicaoBD>();
		submetasMedicao.add(submetaMedicaoBD);

		List<SubmetaMedicaoBD> submetasMedicao2 = new ArrayList<SubmetaMedicaoBD>();
		submetasMedicao2.add(submetaMedicaoBD2);

		when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);
		when(submetaDao.buscarListaSubmetasporMedicao(medicao.id)).thenReturn(submetasMedicao);
		when(submetaDao.listarSubmetasMedicoesAcumuladas(medicao.id)).thenReturn(submetasMedicao2);
		when(contratoDao.consultarContrato(medicao.id)).thenReturn(contratoMedicao);

		assertThrowsMedicaoRestException(MessageKey.ERRO_SUBMETAS_ACUMULADAS_RASCUNHO_CONVENENTE,
				() -> medicaoBC.atestar(medicao.id));
	}

	@Test
	void testAtestarComplementacaoBMSubmetasMedicoesAcumuladasRascunhoConvenente() {

		ContratoBD contratoMedicao = newContratoMedicaoBuilder()
				.setId(1L)
				.setAcompanhadoPorEventos(FALSE)
				.setContratoSiconv(7L).create();

		MedicaoBD medicao = newMedicaoBuilder()
				.setId(1L)
				.setMedContrato(contratoMedicao.id)
				.comSituacao(SituacaoMedicaoEnum.CC)
				.setPermiteComplementacaoValor(FALSE).create();

		SubmetaMedicaoBD submetaMedicaoBD = newSubmetaBuilder()
				.setIdMedicao(medicao.id)
				.setIdSubmetaMedicao(1L)
				.setIdSubmetaVrpl(1L)
				.setSituacaoEmpresa(SituacaoSubmetaEnum.ASS)
				.setSituacaoConvenente(SituacaoSubmetaEnum.ASS).create();

		SubmetaMedicaoBD submetaMedicaoBD2 = newSubmetaBuilder()
				.setIdMedicao(medicao.id)
				.setIdSubmetaMedicao(2L)
				.setIdSubmetaVrpl(2L)
				.setSituacaoEmpresa(SituacaoSubmetaEnum.ASS)
				.setSituacaoConvenente(SituacaoSubmetaEnum.RAS).create();

		List<SubmetaMedicaoBD> submetasMedicao = new ArrayList<SubmetaMedicaoBD>();
		submetasMedicao.add(submetaMedicaoBD);

		List<SubmetaMedicaoBD> submetasMedicao2 = new ArrayList<SubmetaMedicaoBD>();
		submetasMedicao2.add(submetaMedicaoBD2);

		when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);
		when(submetaDao.buscarListaSubmetasporMedicao(medicao.id)).thenReturn(submetasMedicao);
		when(submetaDao.listarSubmetasMedicoesAcumuladas(medicao.id)).thenReturn(submetasMedicao2);
		when(contratoDao.consultarContrato(medicao.id)).thenReturn(contratoMedicao);

		assertThrowsMedicaoRestException(MessageKey.ERRO_SUBMETAS_RASCUNHO_CONVENENTE,
				() -> medicaoBC.atestar(medicao.id));
	}
	
	@Test
	void testAtestarMedicaoDocumentoComplementarDasSubmetasInvalidoOuInexistente() {
		
		ContratoBD contratoMedicao = newContratoMedicaoBuilder().setId(1L)
				.setContratoSiconv(7L).create();
		
		Short nrSequencial = 1;
		
		MedicaoBD medicao = newMedicaoBuilder().setId(1L)
				.setMedContrato(contratoMedicao.id)
				.setNrSequencial(nrSequencial)
				.comSituacao(SituacaoMedicaoEnum.AT).create();
		
		SubmetaMedicaoBD submetaMedicaoBD = newSubmetaBuilder()
				.setIdMedicao(medicao.id)
				.setIdSubmetaMedicao(1L)
				.setIdSubmetaVrpl(1L)
				.setSituacaoEmpresa(SituacaoSubmetaEnum.ASS)
				.setSituacaoConvenente(SituacaoSubmetaEnum.ASS)
				.create();
				
		List<SubmetaMedicaoBD> submetasMedicao = new ArrayList<SubmetaMedicaoBD>();
		submetasMedicao.add(submetaMedicaoBD);

		List<SubmetaVrplDTO> submetasVRPL = new ArrayList<SubmetaVrplDTO>();
		SubmetaVrplDTO subVRPL = new SubmetaVrplDTO();
		subVRPL.setId(submetaMedicaoBD.idSubmetaVrpl);
		submetasVRPL.add(subVRPL);
		
		DocumentoComplementarDTO docComplementar = newDocumentoComplementar()
				.setId(1L)
				.setSubmetas(submetasVRPL)
				.setTipoDocumento(TipoDocumentoEnum.MAM)
				.setTipoManifestoAmbiental(TipoManifestoEnum.PRO)
				.create();
		
		DocumentoComplementarDTO docComplementar2 = newDocumentoComplementar()
				.setId(2L)
				.setIdContratoSiconv(contratoMedicao.contratoFk)
				.setSubmetas(submetasVRPL)
				.setDtEmissao(LocalDate.now().minusYears(1L))
				.setDtValidade(LocalDate.now().minusMonths(5L))
				.setTipoDocumento(TipoDocumentoEnum.MAM)
				.setTipoManifestoAmbiental(TipoManifestoEnum.LIN)
				.create();
		
		DocumentoComplementarDTO docComplementar3 = newDocumentoComplementar()
				.setId(3L)
				.setIdContratoSiconv(contratoMedicao.contratoFk)
				.setSubmetas(submetasVRPL)
				.setDtEmissao(LocalDate.now().minusYears(1L))
				.setDtValidade(LocalDate.now().minusMonths(5L))
				.setTipoDocumento(TipoDocumentoEnum.MAM)
				.setTipoManifestoAmbiental(TipoManifestoEnum.OUT)
				.setEquivaleALicencaInstalacao(true)
				.create();

		List<DocumentoComplementarDTO> listaDocComplementar = new ArrayList<>();
		listaDocComplementar.add(docComplementar);
		listaDocComplementar.add(docComplementar2);
		listaDocComplementar.add(docComplementar3);
		
		List<Long> listaItemMedicao = new ArrayList<Long>();
		listaItemMedicao.add(1L);		
			
		when(contratoDao.consultarContrato(contratoMedicao.id)).thenReturn(contratoMedicao);
		when(docComplementarBC.listarDocumentosComplementares(contratoMedicao.contratoFk)).thenReturn(listaDocComplementar);
		when(contratoDao.consultarContratoPorContratoFK(contratoMedicao.contratoFk)).thenReturn(contratoMedicao);
		when(contratoDao.consultarContrato(medicao.id)).thenReturn(contratoMedicao);
		when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);
		when(submetaDao.buscarListaSubmetasporMedicao(medicao.id)).thenReturn(submetasMedicao);
		when(itemMedicaoDao.consultarSubmetasContrato(contratoMedicao.id)).thenReturn(listaItemMedicao);
		when(projetoBasicoConsumer.consultarDocumentosComplementaresProjetoBasico(listaItemMedicao, Arrays.asList(DIS, LIN))).thenReturn(listaDocComplementar);

		assertThrowsMedicaoRestException(MessageKey.ERRO_DOC_COMPLEMENTAR_INVALIDO_INEXISTENTE,
				() -> medicaoBC.atestar(medicao.id));
	}
	
	@Test
	void testAtestarMedicaoSubmetasAssinadasEmpresaNaoAssinadaConvenente() {

		List<SubmetaMedicaoBD> submetasMedicao = new ArrayList<SubmetaMedicaoBD>();
		
		MedicaoBD medicao = newMedicaoBuilder().setId(1L)
				.comSituacao(SituacaoMedicaoEnum.AT).create();
		
		ContratoBD contratoMedicao = newContratoMedicaoBuilder().setId(1L)
				.setContratoSiconv(7L).create();
			
		when(contratoDao.consultarContrato(medicao.idContratoMedicao)).thenReturn(contratoMedicao);
		when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);
		when(submetaDao.buscarListaSubmetasporMedicao(medicao.id)).thenReturn(submetasMedicao);

		assertThrowsMedicaoRestException(MessageKey.ERRO_SUBMETAS_ASSINADAS_EMPRESA_NAO_ASSINADAS_CONVENENTE,
				() -> medicaoBC.atestar(medicao.id));
	}
	
	@Test
	void testAtestarMedicao() {
		
		ContratoBD contratoMedicao = newContratoMedicaoBuilder().setId(1L)
				.setContratoSiconv(7L).create();
		
		Short nrSequencial = 1;
		
		MedicaoBD medicao = newMedicaoBuilder().setId(1L)
				.setMedContrato(contratoMedicao.id)
				.setNrSequencial(nrSequencial)
				.comSituacao(SituacaoMedicaoEnum.AT).create();
		
		SubmetaMedicaoBD submetaMedicaoBD = newSubmetaBuilder()
				.setIdMedicao(medicao.id)
				.setIdSubmetaMedicao(1L)
				.setIdSubmetaVrpl(1L)
				.setSituacaoEmpresa(SituacaoSubmetaEnum.ASS)
				.setSituacaoConvenente(SituacaoSubmetaEnum.ASS)
				.create();
				
		List<SubmetaMedicaoBD> submetasMedicao = new ArrayList<SubmetaMedicaoBD>();
		submetasMedicao.add(submetaMedicaoBD);
		
		List<SubmetaVrplDTO> submetasVRPL = new ArrayList<SubmetaVrplDTO>();
		SubmetaVrplDTO subVRPL = new SubmetaVrplDTO();
		subVRPL.setId(submetaMedicaoBD.idSubmetaVrpl);
		submetasVRPL.add(subVRPL);
		
		DocumentoComplementarDTO docComplementar = newDocumentoComplementar()
				.setDtEmissao(LocalDate.now())
				.setDtValidade(LocalDate.now().plusYears(1L))
				.setSubmetas(submetasVRPL)
				.setTipoDocumento(TipoDocumentoEnum.MAM)
				.setTipoManifestoAmbiental(TipoManifestoEnum.DIS)
				.create();
				
		List<DocumentoComplementarDTO> listaDocComplementar = new ArrayList<>();
		listaDocComplementar.add(docComplementar);
		
		List<Long> listaItemMedicao = new ArrayList<Long>();
		listaItemMedicao.add(1L);		
			
		when(contratoDao.consultarContrato(medicao.idContratoMedicao)).thenReturn(contratoMedicao);
		when(docComplementarBC.listarDocumentosComplementares(contratoMedicao.contratoFk)).thenReturn(listaDocComplementar);
		when(contratoDao.consultarContratoPorContratoFK(contratoMedicao.contratoFk)).thenReturn(contratoMedicao);
		when(contratoDao.consultarContrato(medicao.idContratoMedicao)).thenReturn(contratoMedicao);
		when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);
		when(submetaDao.buscarListaSubmetasporMedicao(medicao.id)).thenReturn(submetasMedicao);
		when(itemMedicaoDao.consultarSubmetasContrato(medicao.idContratoMedicao)).thenReturn(listaItemMedicao);
		when(projetoBasicoConsumer.consultarDocumentosComplementaresProjetoBasico(listaItemMedicao, Arrays.asList(DIS, LIN))).thenReturn(listaDocComplementar);
		when(securityContext.isUserInProfile(PROPONENTE_CONVENENTE)).thenReturn(true);

		medicaoBC.atestar(medicao.id);
		
		verify(docComplementarBC, times(1)).bloquearDocumentosComplementares(handle, medicao.idContratoMedicao);
		verify(medicaoDao, times(1)).alterar(medicaoCaptor.capture());			
		verify(historicoMedicaoBC, times(1)).inserir(historicoCaptor.capture());
		
		assertEquals(SituacaoMedicaoEnum.ATD, medicaoCaptor.getAllValues().get(0).getSituacao());
		assertEquals(medicao.nrSequencial, historicoCaptor.getAllValues().get(0).getNrSequencial());
		assertEquals(medicao.idContratoMedicao, historicoCaptor.getAllValues().get(0).getIdContratoMedicao());
		assertEquals(SituacaoMedicaoEnum.ATD, historicoCaptor.getAllValues().get(0).getSituacao());		
		
	}
	
	@Test
	void testAtestarMedicaoEmComplementacao() {
	
		ContratoBD contratoMedicao = newContratoMedicaoBuilder().setId(1L)
				.setContratoSiconv(7L).create();
		
		Short nrSequencial = 1;
		
		MedicaoBD medicao = newMedicaoBuilder().setId(1L)
				.setMedContrato(contratoMedicao.id)
				.setNrSequencial(nrSequencial)
				.comSituacao(SituacaoMedicaoEnum.CC).create();
		
		SubmetaMedicaoBD submetaMedicaoBD = newSubmetaBuilder()
				.setIdMedicao(medicao.id)
				.setIdSubmetaMedicao(1L)
				.setIdSubmetaVrpl(1L)
				.setSituacaoEmpresa(SituacaoSubmetaEnum.ASS)
				.setSituacaoConvenente(SituacaoSubmetaEnum.ASS)
				.create();
				
		List<SubmetaMedicaoBD> submetasMedicao = new ArrayList<SubmetaMedicaoBD>();
		submetasMedicao.add(submetaMedicaoBD);
		
		List<SubmetaVrplDTO> submetasVRPL = new ArrayList<SubmetaVrplDTO>();
		SubmetaVrplDTO subVRPL = new SubmetaVrplDTO();
		subVRPL.setId(submetaMedicaoBD.idSubmetaVrpl);
		submetasVRPL.add(subVRPL);
		
		DocumentoComplementarDTO docComplementar = newDocumentoComplementar()
				.setDtEmissao(LocalDate.now())
				.setDtValidade(LocalDate.now().plusYears(1L))
				.setSubmetas(submetasVRPL)
				.setTipoDocumento(TipoDocumentoEnum.MAM)
				.setTipoManifestoAmbiental(TipoManifestoEnum.DIS)
				.create();

		List<DocumentoComplementarDTO> listaDocComplementar = new ArrayList<>();		
		listaDocComplementar.add(docComplementar);
		
		List<Long> listaItemMedicao = new ArrayList<Long>();
		listaItemMedicao.add(1L);		
			
		when(contratoDao.consultarContrato(contratoMedicao.id)).thenReturn(contratoMedicao);
		when(docComplementarBC.listarDocumentosComplementares(contratoMedicao.contratoFk)).thenReturn(listaDocComplementar);
		when(contratoDao.consultarContratoPorContratoFK(contratoMedicao.contratoFk)).thenReturn(contratoMedicao);
		when(contratoDao.consultarContrato(medicao.idContratoMedicao)).thenReturn(contratoMedicao);
		when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);
		when(submetaDao.buscarListaSubmetasporMedicao(medicao.id)).thenReturn(submetasMedicao);
		when(itemMedicaoDao.consultarSubmetasContrato(contratoMedicao.id)).thenReturn(listaItemMedicao);
		when(projetoBasicoConsumer.consultarDocumentosComplementaresProjetoBasico(listaItemMedicao, Arrays.asList(DIS, LIN))).thenReturn(listaDocComplementar);
		when(securityContext.isUserInProfile(PROPONENTE_CONVENENTE)).thenReturn(true);

		medicaoBC.atestar(medicao.id);
		
		verify(docComplementarBC, times(1)).bloquearDocumentosComplementares(handle, contratoMedicao.id);
		verify(medicaoDao, times(1)).alterar(medicaoCaptor.capture());			
		verify(historicoMedicaoBC, times(1)).inserir(historicoCaptor.capture());
		
		assertEquals(SituacaoMedicaoEnum.ATD, medicaoCaptor.getAllValues().get(0).getSituacao());
		assertEquals(medicao.nrSequencial, historicoCaptor.getAllValues().get(0).getNrSequencial());
		assertEquals(contratoMedicao.id, historicoCaptor.getAllValues().get(0).getIdContratoMedicao());
		assertEquals(SituacaoMedicaoEnum.ATD, historicoCaptor.getAllValues().get(0).getSituacao());		
			
	}
	
	@Test
	void testAtestarMedicaoEmComplementacao_PosteriorEmAteste() {
	
		ContratoBD contratoMedicao = newContratoMedicaoBuilder().setId(1L)
				.setContratoSiconv(7L).create();
		
		Short nrSequencial = 1;
		Short nrSequencial2 = 2;
		
		MedicaoBD medicao = newMedicaoBuilder().setId(1L)
				.setMedContrato(contratoMedicao.id)
				.setNrSequencial(nrSequencial)
				.comSituacao(SituacaoMedicaoEnum.CC).create();
		
		MedicaoBD medicaoEmAtesteBloqueada = newMedicaoBuilder().setId(1L)
				.setMedContrato(contratoMedicao.id)
				.setNrSequencial(nrSequencial2)
				.setBloqueada(TRUE)
				.comSituacao(SituacaoMedicaoEnum.AT).create();
		
		SubmetaMedicaoBD submetaMedicaoBD = newSubmetaBuilder()
				.setIdMedicao(medicao.id)
				.setIdSubmetaMedicao(1L)
				.setIdSubmetaVrpl(1L)
				.setSituacaoEmpresa(SituacaoSubmetaEnum.ASS)
				.setSituacaoConvenente(SituacaoSubmetaEnum.ASS)
				.create();
				
		List<SubmetaMedicaoBD> submetasMedicao = new ArrayList<SubmetaMedicaoBD>();
		submetasMedicao.add(submetaMedicaoBD);
		
		List<SubmetaVrplDTO> submetasVRPL = new ArrayList<SubmetaVrplDTO>();
		SubmetaVrplDTO subVRPL = new SubmetaVrplDTO();
		subVRPL.setId(submetaMedicaoBD.idSubmetaVrpl);
		submetasVRPL.add(subVRPL);
		
		DocumentoComplementarDTO docComplementar = newDocumentoComplementar()
				.setDtEmissao(LocalDate.now())
				.setDtValidade(LocalDate.now().plusYears(1L))
				.setSubmetas(submetasVRPL)
				.setTipoDocumento(TipoDocumentoEnum.MAM)
				.setTipoManifestoAmbiental(TipoManifestoEnum.DIS)
				.create();

		List<DocumentoComplementarDTO> listaDocComplementar = new ArrayList<>();		
		listaDocComplementar.add(docComplementar);
		
		List<Long> listaItemMedicao = new ArrayList<Long>();
		listaItemMedicao.add(1L);		
			
		when(contratoDao.consultarContrato(contratoMedicao.id)).thenReturn(contratoMedicao);
		when(docComplementarBC.listarDocumentosComplementares(contratoMedicao.contratoFk)).thenReturn(listaDocComplementar);
		when(contratoDao.consultarContratoPorContratoFK(contratoMedicao.contratoFk)).thenReturn(contratoMedicao);
		when(contratoDao.consultarContrato(medicao.idContratoMedicao)).thenReturn(contratoMedicao);
		when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);
		when(submetaDao.buscarListaSubmetasporMedicao(medicao.id)).thenReturn(submetasMedicao);
		when(itemMedicaoDao.consultarSubmetasContrato(contratoMedicao.id)).thenReturn(listaItemMedicao);
		when(projetoBasicoConsumer.consultarDocumentosComplementaresProjetoBasico(listaItemMedicao, Arrays.asList(DIS, LIN))).thenReturn(listaDocComplementar);
		when(securityContext.isUserInProfile(PROPONENTE_CONVENENTE)).thenReturn(true);
		when(medicaoDao.consultarMedicaoporSituacao(medicao.idContratoMedicao, SituacaoMedicaoEnum.AT)).thenReturn(List.of(medicaoEmAtesteBloqueada));

		medicaoBC.atestar(medicao.id);
		
		verify(medicaoDao, times(1)).desbloquearMedicao(medicaoCaptor.capture());
		verify(medicaoDao, times(1)).alterar(medicaoCaptor.capture());
		verify(docComplementarBC, times(1)).bloquearDocumentosComplementares(handle, contratoMedicao.id);			
		verify(historicoMedicaoBC, times(1)).inserir(historicoCaptor.capture());
		
		assertEquals(SituacaoMedicaoEnum.AT, medicaoCaptor.getAllValues().get(0).getSituacao());
		assertEquals(SituacaoMedicaoEnum.ATD, medicaoCaptor.getAllValues().get(1).getSituacao());
		assertEquals(medicao.nrSequencial, historicoCaptor.getAllValues().get(0).getNrSequencial());
		assertEquals(contratoMedicao.id, historicoCaptor.getAllValues().get(0).getIdContratoMedicao());
		assertEquals(SituacaoMedicaoEnum.ATD, historicoCaptor.getAllValues().get(0).getSituacao());		
			
	}
	
	@Test
	void testAtestarMedicaoBloqueada() {

		MedicaoBD medicao = newMedicaoBuilder().setId(1L).setBloqueada(true).comSituacao(SituacaoMedicaoEnum.AT)
				.create();

		when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);

		assertThrowsMedicaoRestException(MessageKey.ERRO_MEDICAO_BLOQUEADA, () -> medicaoBC.atestar(medicao.id));
	}
	
	/* *************************************************************
	 *                CANCELAR ENVIO PARA CONCEDENTE
	 * ************************************************************* */
	
	@Test
	void cancelarEnvioConcedente_MedicaoNaoEncontrada() {
		Long idMedicao = 999L;

		assertThrowsMedicaoRestException(MessageKey.ERRO_MEDICAO_NAO_ENCONTRADA,
				() -> medicaoBC.cancelarEnvioConcedente(idMedicao));
	}
	
	@Test
	void cancelarEnvioConcedente_SituacaoNaoPrevista() {

		MedicaoBD medicao = newMedicaoBuilder().setId(1L)
				.comSituacao(SituacaoMedicaoEnum.ATD).create();

		when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);
		when(medicaoDao.permiteCancelarEnvioConcedente(medicao.id)).thenReturn(false);

		assertThrowsMedicaoRestException(MessageKey.ERRO_CANCELAR_ENVIO_CONCEDENTE_SITUACAO_NAO_PREVISTA,
				() -> medicaoBC.cancelarEnvioConcedente(medicao.id));
	}
	
	@Test
	void cancelarEnvioConcedente() {
		
		MedicaoBD medicao = newMedicaoBuilder().setId(1L)
				.comSituacao(SituacaoMedicaoEnum.ATD).create();
		
		HistoricoMedicaoBD penultimoHistorico = new HistoricoMedicaoBD();
		penultimoHistorico.setSituacao(SituacaoMedicaoEnum.AT);

		when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);
		when(medicaoDao.permiteCancelarEnvioConcedente(medicao.id)).thenReturn(true);
		when(historicoMedicaoBC.recuperarPenultimoHistoricoPorMedicaoContrato(
				medicao.idContratoMedicao,medicao.nrSequencial)).thenReturn(Optional.of(penultimoHistorico));
		
		medicaoBC.cancelarEnvioConcedente(medicao.id);
		
		verify(medicaoDao, times(1)).alterar(medicaoCaptor.capture());
		assertEquals(SituacaoMedicaoEnum.AT, medicaoCaptor.getValue().getSituacao());
	}
	
	/* *************************************************************
	 *               CANCELAR ENVIO PARA COMPLEMENTAÇÃO 
	 * ************************************************************* */
	
	@Test
	void testCancelarEnvioParaComplementacao_MedicaoNaoEncontrada() {
		Long idMedicao = 999L;

		assertThrowsMedicaoRestException(MessageKey.ERRO_MEDICAO_NAO_ENCONTRADA,
				() -> medicaoBC.cancelarEnvioParaComplementacao(idMedicao));
	}
	
	@Test
	void testCancelarEnvioParaComplementacao_MedicaoSituacaoNaoPermitida() {
		
		MedicaoBD medicao = newMedicaoBuilder().setId(1L)
				.comSituacao(SituacaoMedicaoEnum.EM).create();

		when(securityContext.isUserInProfile(CONCEDENTE)).thenReturn(true);
		when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);

		assertThrowsMedicaoRestException(MessageKey.ERRO_CANCELAR_ENVIO_PARA_COMPLEMENTACAO_CONVENENTE_SITUACAO_NAO_PREVISTA,
				() -> medicaoBC.cancelarEnvioParaComplementacao(medicao.id));
	}
	
	@Test
	void testCancelarEnvioParaComplementacao_MedicaoAgrupada() {
		
		MedicaoBD medicao = newMedicaoBuilder().setId(2L)
				.setAgrupadora(1L)
				.comSituacao(SituacaoMedicaoEnum.ECC).create();

		List<SituacaoMedicaoEnum> situacoesEmEdicao = new ArrayList<SituacaoMedicaoEnum>();
		situacoesEmEdicao.add(SituacaoMedicaoEnum.AC);
		
		when(securityContext.isUserInProfile(CONCEDENTE)).thenReturn(true);
		when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);

		assertThrowsMedicaoRestException(MessageKey.ERRO_CANCELAR_ENVIO_PARA_COMPLEMENTACAO_CONVENENTE_SITUACAO_NAO_PREVISTA,
				() -> medicaoBC.cancelarEnvioParaComplementacao(medicao.id));
	}
	
	@Test
	void testCancelarEnvioParaComplementacao_MedicaoPosteriorEmAteste() {
	
		MedicaoBD medicao = newMedicaoBuilder().setId(1L)
				.comSituacao(SituacaoMedicaoEnum.ECC).create();
		
		List<SituacaoMedicaoEnum> situacoesEmEdicao = new ArrayList<SituacaoMedicaoEnum>();
		situacoesEmEdicao.add(SituacaoMedicaoEnum.AC);
		
		when(securityContext.isUserInProfile(CONCEDENTE)).thenReturn(true);
		when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);
		when(medicaoDao.existeMedicao(medicao.idContratoMedicao, situacoesEmEdicao)).thenReturn(true);

		assertThrowsMedicaoRestException(MessageKey.ERRO_CANCELAR_ENVIO_PARA_COMPLEMENTACAO_CONVENENTE_SITUACAO_NAO_PREVISTA,
				() -> medicaoBC.cancelarEnvioParaComplementacao(medicao.id));
	}
	
	@Test
	void testCancelarEnvioParaComplementacao_PenultimoHistoricoNaoEncontrado() {

		Short nrSequencial = 1;
		
		MedicaoBD medicao = newMedicaoBuilder().setId(1L)
				.comSituacao(SituacaoMedicaoEnum.ECC).create();
		
		List<SituacaoMedicaoEnum> situacoesEmEdicao = new ArrayList<SituacaoMedicaoEnum>();
		situacoesEmEdicao.add(SituacaoMedicaoEnum.AC);
		
		when(securityContext.isUserInProfile(CONCEDENTE)).thenReturn(true);
		when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);
		when(medicaoDao.existeMedicao(medicao.idContratoMedicao, situacoesEmEdicao)).thenReturn(false);
		when(historicoMedicaoBC.recuperarPenultimoHistoricoPorMedicaoContrato(
				medicao.idContratoMedicao,nrSequencial)).thenThrow(MedicaoRestException.class);
		
		assertThrowsMedicaoRestException(MessageKey.ERRO_PENULTIMO_HISTORICO_MEDICAO_NAO_ENCONTRADO,
				() -> medicaoBC.cancelarEnvioParaComplementacao(medicao.id));
	}
	
	@Test
	void testCancelarEnvioParaComplementacaoConvente() {
		Short nrSequencial = 1;
		
		MedicaoBD medicao = newMedicaoBuilder().setId(1L)
				.setMedContrato(1L)
				.setNrSequencial(nrSequencial)
				.comSituacao(SituacaoMedicaoEnum.ECC)
				.setPermiteComplementacaoValor(FALSE).create();
		
		List<SituacaoMedicaoEnum> situacoesEmEdicao = new ArrayList<SituacaoMedicaoEnum>();
		situacoesEmEdicao.add(SituacaoMedicaoEnum.AC);
		
		HistoricoMedicaoBD penultimoHistorico = new HistoricoMedicaoBD();
		penultimoHistorico.setSituacao(SituacaoMedicaoEnum.AC);
		
		when(securityContext.isUserInProfile(CONCEDENTE)).thenReturn(true);
		when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);
		when(medicaoDao.existeMedicao(medicao.idContratoMedicao, situacoesEmEdicao)).thenReturn(false);
		when(historicoMedicaoBC.recuperarPenultimoHistoricoPorMedicaoContrato(
				medicao.idContratoMedicao, medicao.nrSequencial)).thenReturn(Optional.of(penultimoHistorico));
		
		medicaoBC.cancelarEnvioParaComplementacao(medicao.id);
		
		verify(medicaoDao, times(1)).alterar(medicaoCaptor.capture());
		assertEquals(SituacaoMedicaoEnum.AC, medicaoCaptor.getValue().getSituacao());
		assertEquals(null, medicaoCaptor.getValue().getPermiteComplementacaoValor());
		
	}

	@Test
	void testCancelarEnvioParaComplementacaoConventeComAgrupamento() {

		Short nrSequencial = 1;

		MedicaoBD medicaoAgrupadora = newMedicaoBuilder().setId(3L).setNrSequencial(nrSequencial).setMedContrato(1L)
				.comSituacao(ECC).setPermiteComplementacaoValor(false).create();

		MedicaoBD medicaoAcumulada1 = newMedicaoBuilder().setId(1L).setMedContrato(1L).comSituacao(ECC)
				.setPermiteComplementacaoValor(false).setAgrupadora(3L).create();

		MedicaoBD medicaoAcumulada2 = newMedicaoBuilder().setId(2L).setMedContrato(1L).comSituacao(ECC)
				.setPermiteComplementacaoValor(false).setAgrupadora(3L).create();

		HistoricoMedicaoBD penultimoHistorico = new HistoricoMedicaoBD();
		penultimoHistorico.setSituacao(SituacaoMedicaoEnum.AC);

		when(securityContext.isUserInProfile(MANDATARIA)).thenReturn(true);
		when(medicaoDao.consultarMedicao(medicaoAgrupadora.getId())).thenReturn(medicaoAgrupadora);
		when(historicoMedicaoBC.recuperarPenultimoHistoricoPorMedicaoContrato(medicaoAgrupadora.getIdContratoMedicao(),
				nrSequencial)).thenReturn(Optional.of(penultimoHistorico));
		when(medicaoDao.listarMedicoesAcumuladas(medicaoAgrupadora.getId()))
				.thenReturn(List.of(medicaoAcumulada1, medicaoAcumulada2));

		medicaoBC.cancelarEnvioParaComplementacao(medicaoAgrupadora.getId());

		verify(medicaoDao, times(3)).alterar(medicaoCaptor.capture());

		medicaoCaptor.getAllValues().forEach(medicao -> {
			assertEquals(SituacaoMedicaoEnum.AC, medicao.getSituacao());
			assertNull(medicao.getPermiteComplementacaoValor());
		});
	}

	@Test
	void testCancelarEnvioParaComplementacaoConventePermiteValor() {
		Short nrSequencial = 1;
		
		MedicaoBD medicao = newMedicaoBuilder().setId(1L)
				.setMedContrato(1L)
				.setNrSequencial(nrSequencial)
				.comSituacao(SituacaoMedicaoEnum.ECC)
				.setPermiteComplementacaoValor(TRUE).create();
		
		List<SituacaoMedicaoEnum> situacoesEmEdicao = new ArrayList<SituacaoMedicaoEnum>();
		situacoesEmEdicao.add(SituacaoMedicaoEnum.AC);
		
		HistoricoMedicaoBD penultimoHistorico = new HistoricoMedicaoBD();
		penultimoHistorico.setSituacao(SituacaoMedicaoEnum.AC);
		
		when(securityContext.isUserInProfile(CONCEDENTE)).thenReturn(true);
		when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);
		when(medicaoDao.existeMedicao(medicao.idContratoMedicao, situacoesEmEdicao)).thenReturn(false);
		when(historicoMedicaoBC.recuperarPenultimoHistoricoPorMedicaoContrato(
				medicao.idContratoMedicao, medicao.nrSequencial)).thenReturn(Optional.of(penultimoHistorico));
		
		medicaoBC.cancelarEnvioParaComplementacao(medicao.id);
		
		verify(medicaoDao, times(1)).alterar(medicaoCaptor.capture());
		assertEquals(SituacaoMedicaoEnum.AC, medicaoCaptor.getValue().getSituacao());
		assertEquals(null, medicaoCaptor.getValue().getPermiteComplementacaoValor());
		
	}
	
	@Test
	void testCancelarEnvioParaComplementacaoEmpresa() {
		
		Short nrSequencial = 1;
		
		MedicaoBD medicao = newMedicaoBuilder().setId(1L)
				.setMedContrato(1L)
				.setNrSequencial(nrSequencial)
				.comSituacao(SituacaoMedicaoEnum.ECE).create();
		
		List<SituacaoMedicaoEnum> situacoesEmEdicao = new ArrayList<SituacaoMedicaoEnum>();
		situacoesEmEdicao.add(SituacaoMedicaoEnum.AT);
		situacoesEmEdicao.add(SituacaoMedicaoEnum.CC);
		
		HistoricoMedicaoBD penultimoHistorico = new HistoricoMedicaoBD();
		penultimoHistorico.setSituacao(SituacaoMedicaoEnum.AT);
		
		when(securityContext.isUserInProfile(PROPONENTE_CONVENENTE)).thenReturn(true);
		when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);
		when(medicaoDao.existeMedicao(medicao.idContratoMedicao, situacoesEmEdicao)).thenReturn(false);
		when(historicoMedicaoBC.recuperarPenultimoHistoricoPorMedicaoContrato(
				medicao.idContratoMedicao,medicao.nrSequencial)).thenReturn(Optional.of(penultimoHistorico));
		
		medicaoBC.cancelarEnvioParaComplementacao(medicao.id);
		
		verify(medicaoDao, times(1)).alterar(medicaoCaptor.capture());
		assertEquals(SituacaoMedicaoEnum.AT, medicaoCaptor.getValue().getSituacao());	
	}
	
	@Test
	void testCancelarEnvioParaComplementacao_MedicaoAgrupadora() {
		
		Short nrSequencial = 2;
		
		MedicaoBD medicao = newMedicaoBuilder().setId(2L)
				.setMedContrato(1L)
				.setNrSequencial(nrSequencial)
				.comSituacao(SituacaoMedicaoEnum.ECC).create();
		
		Short nrSequencialFilha = 1;
		
		MedicaoBD medicaoFilha = newMedicaoBuilder().setId(1L)
				.setMedContrato(1L)
				.setNrSequencial(nrSequencialFilha)
				.comSituacao(SituacaoMedicaoEnum.ECC).create();
		
		List<MedicaoBD> listaMedicoesAcumuladas = new ArrayList<MedicaoBD>();
		listaMedicoesAcumuladas.add(medicaoFilha);
		
		List<SituacaoMedicaoEnum> situacoesEmEdicao = new ArrayList<SituacaoMedicaoEnum>();
		situacoesEmEdicao.add(SituacaoMedicaoEnum.AC);
		
		HistoricoMedicaoBD penultimoHistorico = new HistoricoMedicaoBD();
		penultimoHistorico.setSituacao(SituacaoMedicaoEnum.AC);
		
		when(securityContext.isUserInProfile(CONCEDENTE)).thenReturn(true);
		when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);
		when(medicaoDao.existeMedicao(medicao.idContratoMedicao, situacoesEmEdicao)).thenReturn(false);
		when(historicoMedicaoBC.recuperarPenultimoHistoricoPorMedicaoContrato(medicao.idContratoMedicao,medicao.nrSequencial)).thenReturn(Optional.of(penultimoHistorico));
		when(medicaoDao.listarMedicoesAcumuladas(medicao.id)).thenReturn(listaMedicoesAcumuladas);
		
		medicaoBC.cancelarEnvioParaComplementacao(medicao.id);
		
		verify(medicaoDao, times(2)).alterar(medicaoCaptor.capture());
		
		assertEquals(SituacaoMedicaoEnum.AC, medicaoCaptor.getAllValues().get(0).getSituacao());
		assertEquals(SituacaoMedicaoEnum.AC, medicaoCaptor.getAllValues().get(1).getSituacao());
	}
	
	@Test
	void testCancelarEnvioParaComplementacao_DesbloquearMedicaoEmAteste() {
		
		Short nrSequencial = 2;
		
		MedicaoBD medicao = newMedicaoBuilder().setId(2L)
				.setMedContrato(1L)
				.setNrSequencial(nrSequencial)
				.comSituacao(SituacaoMedicaoEnum.ECC).create();
		
		Short nrSequencialEmAteste = 1;
		
		MedicaoBD medicaoEmAteste = newMedicaoBuilder().setId(1L)
				.setMedContrato(1L)
				.setNrSequencial(nrSequencialEmAteste)
				.setBloqueada(true)
				.comSituacao(SituacaoMedicaoEnum.AT).create();
		
		List<SituacaoMedicaoEnum> situacoesEmEdicao = new ArrayList<SituacaoMedicaoEnum>();
		situacoesEmEdicao.add(SituacaoMedicaoEnum.AC);
		
		HistoricoMedicaoBD penultimoHistorico = new HistoricoMedicaoBD();
		penultimoHistorico.setSituacao(SituacaoMedicaoEnum.AC);
		
		when(securityContext.isUserInProfile(CONCEDENTE)).thenReturn(true);
		when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);
		when(medicaoDao.existeMedicao(medicao.idContratoMedicao, situacoesEmEdicao)).thenReturn(false);
		when(historicoMedicaoBC.recuperarPenultimoHistoricoPorMedicaoContrato(medicao.idContratoMedicao,medicao.nrSequencial)).thenReturn(Optional.of(penultimoHistorico));
		when(medicaoDao.consultarMedicaoporSituacao(medicao.idContratoMedicao, SituacaoMedicaoEnum.AT)).thenReturn(List.of(medicaoEmAteste));
		
		medicaoBC.cancelarEnvioParaComplementacao(medicao.id);
		
		verify(medicaoDao, times(1)).alterar(medicaoCaptor.capture());
		assertEquals(SituacaoMedicaoEnum.AC, medicaoCaptor.getValue().getSituacao());

		verify(medicaoDao, times(1)).desbloquearMedicao(medicaoCaptor.capture());
		assertEquals(medicaoEmAteste.getId(), medicaoCaptor.getValue().getId());
		//assertEquals(false, medicaoCaptor.getValue().isBloqueada());	
	}
	
	/* *************************************************************
	 *          CANCELAR REENCAMINHAR PARA COMPLEMENTAÇÃO 
	 * ************************************************************* */
	
	@Test
	void testCancelarReencaminharParaComplementacaoEmpresa() {
		
		Short nrSequencial = 1;
		
		MedicaoBD medicao = newMedicaoBuilder().setId(1L)
				.setMedContrato(1L)
				.setNrSequencial(nrSequencial)
				.comSituacao(SituacaoMedicaoEnum.ECE)
				.setPermiteComplementacaoValor(FALSE).create();
		
		
		List<SituacaoMedicaoEnum> situacoesEmEdicao = new ArrayList<SituacaoMedicaoEnum>();
		situacoesEmEdicao.add(SituacaoMedicaoEnum.AT);
		situacoesEmEdicao.add(SituacaoMedicaoEnum.CC);
		
		HistoricoMedicaoBD penultimoHistorico = new HistoricoMedicaoBD();
		penultimoHistorico.setSituacao(SituacaoMedicaoEnum.CC);
		
		when(securityContext.isUserInProfile(PROPONENTE_CONVENENTE)).thenReturn(true);
		when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);
		when(medicaoDao.existeMedicao(medicao.idContratoMedicao, situacoesEmEdicao)).thenReturn(false);
		when(historicoMedicaoBC.recuperarPenultimoHistoricoPorMedicaoContrato(
				medicao.idContratoMedicao,medicao.nrSequencial)).thenReturn(Optional.of(penultimoHistorico));
		
		medicaoBC.cancelarEnvioParaComplementacao(medicao.id);
		
		verify(medicaoDao, times(1)).alterar(medicaoCaptor.capture());
		assertEquals(SituacaoMedicaoEnum.CC, medicaoCaptor.getValue().getSituacao());
		assertEquals(FALSE, medicaoCaptor.getValue().getPermiteComplementacaoValor());
	}
	
	@Test
	void testCancelarReencaminharComplementacaoEmpresa_MedicaoAgrupadora() {
		
		Short nrSequencial = 2;
		
		MedicaoBD medicao = newMedicaoBuilder().setId(2L)
				.setMedContrato(1L)
				.setNrSequencial(nrSequencial)
				.comSituacao(SituacaoMedicaoEnum.ECE)
				.setPermiteComplementacaoValor(FALSE).create();
		
		Short nrSequencialFilha = 1;
		
		MedicaoBD medicaoFilha = newMedicaoBuilder().setId(1L)
				.setMedContrato(1L)
				.setNrSequencial(nrSequencialFilha)
				.comSituacao(SituacaoMedicaoEnum.ECE)
				.setPermiteComplementacaoValor(FALSE).create();
		
		List<MedicaoBD> listaMedicoesAcumuladas = new ArrayList<MedicaoBD>();
		listaMedicoesAcumuladas.add(medicaoFilha);
		
		List<SituacaoMedicaoEnum> situacoesEmEdicao = new ArrayList<SituacaoMedicaoEnum>();
		situacoesEmEdicao.add(SituacaoMedicaoEnum.CC);
		
		HistoricoMedicaoBD penultimoHistorico = new HistoricoMedicaoBD();
		penultimoHistorico.setSituacao(SituacaoMedicaoEnum.CC);
		
		when(securityContext.isUserInProfile(PROPONENTE_CONVENENTE)).thenReturn(true);
		when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);
		when(medicaoDao.existeMedicao(medicao.idContratoMedicao, situacoesEmEdicao)).thenReturn(false);
		when(historicoMedicaoBC.recuperarPenultimoHistoricoPorMedicaoContrato(medicao.idContratoMedicao,medicao.nrSequencial)).thenReturn(Optional.of(penultimoHistorico));
		when(medicaoDao.listarMedicoesAcumuladas(medicao.id)).thenReturn(listaMedicoesAcumuladas);
		
		medicaoBC.cancelarEnvioParaComplementacao(medicao.id);
		
		verify(medicaoDao, times(2)).alterar(medicaoCaptor.capture());
		
		assertEquals(SituacaoMedicaoEnum.CC, medicaoCaptor.getAllValues().get(0).getSituacao());
		assertEquals(SituacaoMedicaoEnum.CC, medicaoCaptor.getAllValues().get(1).getSituacao());
		assertEquals(FALSE, medicaoCaptor.getAllValues().get(0).getPermiteComplementacaoValor());
		assertEquals(FALSE, medicaoCaptor.getAllValues().get(1).getPermiteComplementacaoValor());
	}
	
	/* *************************************************************
	 *               VERIFICAR MEDIÇÃO PERMITE COMPLEMENTAÇÃO
	 * ************************************************************* */
	
	@Test
	void testVerificarMedicaoPermiteComplementacaoParametroIdMedicaoNulo() {
		
		when(securityContext.isUserInProfile(PROPONENTE_CONVENENTE)).thenReturn(true);

		
		Exception exception = assertThrows(NullPointerException.class,
				() -> medicaoBC.verificarMedicaoPermiteComplementacao(null));
		
		assertEquals("Parâmetro idMedicao não pode ser nulo", exception.getMessage());
	}
	
	@Test
	void testVerificarMedicaoPermiteComplementacaoMedicaoNaoEncontrada() {

		Long idMedicao = 999L;
		
		when(securityContext.isUserInProfile(PROPONENTE_CONVENENTE)).thenReturn(true);


		assertThrowsMedicaoRestException(MessageKey.ERRO_MEDICAO_NAO_ENCONTRADA,
				() -> medicaoBC.verificarMedicaoPermiteComplementacao(idMedicao));
	}
	
	@Test
	void testVerificarMedicaoPermiteComplementacaoMedicaoComAgrupadoraEmpresa() {
		Short nrSequencial = 2;
		Short nrSequencialFilha = 1;
		
		MedicaoBD medicaoPai = newMedicaoBuilder().setId(2L)
				.setNrSequencial(nrSequencial)
				.setMedContrato(1L)
				.comSituacao(SituacaoMedicaoEnum.AT)
				.create();
		
		MedicaoBD medicaoFilha = newMedicaoBuilder().setId(1L)
				.setMedContrato(1L)
				.setNrSequencial(nrSequencialFilha)
				.comSituacao(SituacaoMedicaoEnum.AT).create();
		medicaoFilha.setIdMedicaoAgrupadora(medicaoPai.id);
		

		when(securityContext.isUserInProfile(PROPONENTE_CONVENENTE)).thenReturn(true);
		when(medicaoDao.consultarMedicao(medicaoFilha.id)).thenReturn(medicaoFilha);
		
		boolean resultado = medicaoBC.verificarMedicaoPermiteComplementacao(medicaoFilha.id);
		
		
		assertEquals(Boolean.FALSE, resultado);
	}
	
	@Test
	void testVerificarMedicaoPermiteComplementacaoMedicaoComAgrupadoraConvenente() {
		Short nrSequencial = 2;
		Short nrSequencialFilha = 1;
		
		MedicaoBD medicaoPai = newMedicaoBuilder().setId(2L)
				.setNrSequencial(nrSequencial)
				.setMedContrato(1L)
				.comSituacao(SituacaoMedicaoEnum.AC)
				.create();
		
		MedicaoBD medicaoFilha = newMedicaoBuilder().setId(1L)
				.setMedContrato(1L)
				.setNrSequencial(nrSequencialFilha)
				.comSituacao(SituacaoMedicaoEnum.AC).create();
		medicaoFilha.setIdMedicaoAgrupadora(medicaoPai.id);
		

		when(securityContext.isUserInProfile(MANDATARIA)).thenReturn(true);
		when(medicaoDao.consultarMedicao(medicaoFilha.id)).thenReturn(medicaoFilha);
		
		boolean resultado = medicaoBC.verificarMedicaoPermiteComplementacao(medicaoFilha.id);
		
		
		assertEquals(Boolean.FALSE, resultado);
	}
	
	@Test
	void testVerificarMedicaoPermiteComplementacaoMedicaoConvenentePermitir() {
		MedicaoBD medicao = newMedicaoBuilder().setId(3L).setMedContrato(1L).comSituacao(AC).create();

		Map<Long, SituacaoMedicaoEnum> situacoesMedicoesContrato = new HashMap<Long, SituacaoMedicaoEnum>();
		situacoesMedicoesContrato.put(1L, ACT);
		situacoesMedicoesContrato.put(2L, ACT);


		when(securityContext.isUserInProfile(MANDATARIA)).thenReturn(true);
		when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);
		when(medicaoDao.listarSituacoesMedicoes(medicao.idContratoMedicao)).thenReturn(situacoesMedicoesContrato);

		boolean resultado = medicaoBC.verificarMedicaoPermiteComplementacao(medicao.id);

		assertEquals(Boolean.TRUE, resultado);
	}
	
	@Test
	void testVerificarMedicaoPermiteComplementacaoMedicaoConvenenteNegar() {
		MedicaoBD medicao = newMedicaoBuilder().setId(1L).setMedContrato(1L).comSituacao(AC).create();

		Map<Long, SituacaoMedicaoEnum> situacoesMedicoesContrato = new HashMap<Long, SituacaoMedicaoEnum>();
		situacoesMedicoesContrato.put(2L, ATD);
		situacoesMedicoesContrato.put(3L, CC);


		when(securityContext.isUserInProfile(MANDATARIA)).thenReturn(true);
		when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);
		when(medicaoDao.listarSituacoesMedicoes(medicao.idContratoMedicao)).thenReturn(situacoesMedicoesContrato);

		boolean resultado = medicaoBC.verificarMedicaoPermiteComplementacao(medicao.id);

		assertEquals(Boolean.FALSE, resultado);
	}
	
	@Test
	void testVerificarMedicaoPermiteComplementacaoMedicaoEmpresaPermitir() {
		MedicaoBD medicao = newMedicaoBuilder().setId(1L).setMedContrato(1L).comSituacao(CC).create();

		Map<Long, SituacaoMedicaoEnum> situacoesMedicoesContrato = new HashMap<Long, SituacaoMedicaoEnum>();
		situacoesMedicoesContrato.put(2L, ATD);
		situacoesMedicoesContrato.put(3L, EM);


		when(securityContext.isUserInProfile(PROPONENTE_CONVENENTE)).thenReturn(true);
		when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);
		when(medicaoDao.listarSituacoesMedicoes(medicao.idContratoMedicao)).thenReturn(situacoesMedicoesContrato);

		boolean resultado = medicaoBC.verificarMedicaoPermiteComplementacao(medicao.id);

		assertEquals(Boolean.TRUE, resultado);
	}
	
	@Test
	void testListarMedicoesAgrupadas_medicaoAgrupada() {
		MedicaoBD medicao = newMedicaoBuilder().setId(1L).setAgrupadora(1L).create();

		when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);

		assertThrows(MedicaoRestException.class, () -> medicaoBC.listarMedicoesAgrupadas(1L,Boolean.TRUE));
	}

	@Test
	void testListarMedicoesAgrupadas_sem_submetasPreenchidas() {
		MedicaoBD medicao = newMedicaoBuilder().setId(3L).create();

		MedicaoBD medicaoF1 = newMedicaoBuilder().setId(1L).setAgrupadora(3L).create();
		MedicaoBD medicaoF2 = newMedicaoBuilder().setId(2L).setAgrupadora(3L).create();
		

//		when(securityContext.isUserInProfile(PROPONENTE_CONVENENTE)).thenReturn(true);
		when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);
		when(medicaoDao.listarMedicoesAcumuladas(medicao.id)).thenReturn(List.of(medicaoF1,medicaoF2));

		List<MedicaoAgrupadaDTO> listaMedicoes = medicaoBC.listarMedicoesAgrupadas(3L,Boolean.FALSE);
		
		listaMedicoes.forEach(med -> {
			assertNull(med.getListaSubmetasPreenchidas());
			Mockito.verify(submetaBC, Mockito.times(0)).recuperarListaSubmetasPorMedicao(Mockito.any());
		});
		
	}

	@Test
	void testListarMedicoesAgrupadas_com_submetasPreenchidasEmpresa() {
		MedicaoBD medicao = newMedicaoBuilder().setId(3L).create();

		MedicaoBD medicaoF1 = newMedicaoBuilder().setId(1L).setAgrupadora(3L).create();
		
		MedicaoBD medicaoF2 = newMedicaoBuilder().setId(2L).setAgrupadora(3L).create();

		SubmetaMedicaoDTO sub1 = SubmetaMedicaoDTOBuilder.
				newSubmetaMedicaoBuilder().
				permiteMarcacaoEmpresa(true).create();
		
		SubmetaMedicaoDTO sub2 = SubmetaMedicaoDTOBuilder.
				newSubmetaMedicaoBuilder().
				permiteMarcacaoEmpresa(false).create();
		
		SubmetaMedicaoDTO sub3 = SubmetaMedicaoDTOBuilder.
				newSubmetaMedicaoBuilder().
				permiteMarcacaoEmpresa(false).create();		
		

		when(securityContext.hasAnyPermissionInProfile(EMPRESA)).thenReturn(true);

		when(medicaoDao.consultarMedicao(medicao.id)).thenReturn(medicao);
		when(medicaoDao.listarMedicoesAcumuladas(medicao.id)).thenReturn(List.of(medicaoF1,medicaoF2));
		
		when(submetaBC.recuperarListaSubmetasPorMedicao (medicaoF1.getId())).thenReturn(List.of(sub1,sub2));
		when(submetaBC.recuperarListaSubmetasPorMedicao (medicaoF2.getId())).thenReturn(List.of(sub2,sub3));
		

		List<MedicaoAgrupadaDTO> listaMedicoes = medicaoBC.listarMedicoesAgrupadas(3L,Boolean.TRUE);
		
		listaMedicoes.forEach(med -> {
			if (med.getId().equals(1L)) {
				assertEquals (1L,med.getListaSubmetasPreenchidas().size());
			}
			if (med.getId().equals(2L)) {
				assertThat(med.getListaSubmetasPreenchidas(), is(empty()));
			}
			verify(submetaBC, Mockito.times(1)).recuperarListaSubmetasPorMedicao(1L);
		});
		
	}

	@Test
	void testListarMedicoesAgrupadas_com_submetasPreenchidasConvenente() {

		MedicaoBD medicao = newMedicaoBuilder().setId(3L).create();

		MedicaoBD medicaoF1 = newMedicaoBuilder().setId(1L).setAgrupadora(3L).create();

		MedicaoBD medicaoF2 = newMedicaoBuilder().setId(2L).setAgrupadora(3L).create();

		SubmetaMedicaoDTO sub1 = newSubmetaMedicaoBuilder()
				.permiteMarcacaoConvenente(true)
				.create();

		SubmetaMedicaoDTO sub2 = newSubmetaMedicaoBuilder()
				.permiteMarcacaoConvenente(false)
				.create();

		SubmetaMedicaoDTO sub3 = newSubmetaMedicaoBuilder()
				.permiteMarcacaoEmpresa(true)
				.permiteMarcacaoConvenente(false)
				.create();

		when(securityContext.isUserInProfile(PROPONENTE_CONVENENTE)).thenReturn(true);

		when(medicaoDao.consultarMedicao(medicao.getId())).thenReturn(medicao);
		when(medicaoDao.listarMedicoesAcumuladas(medicao.getId())).thenReturn(List.of(medicaoF1, medicaoF2));

		when(submetaBC.recuperarListaSubmetasPorMedicao(medicaoF1.getId())).thenReturn(List.of(sub1, sub2));
		when(submetaBC.recuperarListaSubmetasPorMedicao(medicaoF2.getId())).thenReturn(List.of(sub2, sub3));

		List<MedicaoAgrupadaDTO> listaMedicoes = medicaoBC.listarMedicoesAgrupadas(medicao.getId(), true);

		assertThat(listaMedicoes, hasSize(2));

		MedicaoAgrupadaDTO medAgrupada1 = listaMedicoes.get(0);
		MedicaoAgrupadaDTO medAgrupada2 = listaMedicoes.get(1);

		assertEquals(medicaoF1.getId(), medAgrupada1.getId());
		assertThat(medAgrupada1.getListaSubmetasPreenchidas(), hasSize(1));
	
		assertEquals(medicaoF2.getId(), medAgrupada2.getId());
		assertThat(medAgrupada2.getListaSubmetasPreenchidas(), is(empty()));
	}

	private void assertThrowsMedicaoRestException(MessageKey expectedMessageKey, Executable executable) {

		MedicaoRestException exception = assertThrows(MedicaoRestException.class, executable);

		exception.getMessages().stream().map(Message::getKey).findFirst().ifPresentOrElse(
				actualMessageKey -> assertEquals(expectedMessageKey, actualMessageKey),
				() -> fail(format("A messageKey esperada era %s, mas nenhuma foi obtida", expectedMessageKey)));
	}
}
