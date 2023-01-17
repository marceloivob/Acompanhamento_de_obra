package br.gov.planejamento.siconv.med.medicao.business.medicao;

import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.CE;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.ECE;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.EM;
import static br.gov.planejamento.siconv.med.test.builder.ContratoMedicaoBuilder.newContratoMedicaoBuilder;
import static br.gov.planejamento.siconv.med.test.builder.MedicaoBuilder.newMedicaoBuilder;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
import br.gov.planejamento.siconv.med.contrato.business.ContratosBC;
import br.gov.planejamento.siconv.med.contrato.dao.ContratoDAO;
import br.gov.planejamento.siconv.med.contrato.entity.ContratoBD;
import br.gov.planejamento.siconv.med.contrato.entity.dto.ContratoSiconvDTO;
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
import br.gov.planejamento.siconv.med.medicao.entity.database.HistoricoMedicaoBD;
import br.gov.planejamento.siconv.med.medicao.entity.database.MedicaoBD;
import br.gov.planejamento.siconv.med.medicao.entity.database.SubmetaMedicaoBD;
import br.gov.planejamento.siconv.med.medicao.entity.dto.MedicaoDTO;

class MedicaoBC_EnviarConvenenteTest {

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
	private ContratosBC contratoBC2;
	
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

	
	
	// Caso 1: Entrada com id medição nulo.	
	@Test
	void testEnviarConvenente_ReencaminharComplementacao_IdMedicaoNulo() {
		
		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setId(null);
		
		Exception exception = assertThrows(NullPointerException.class,
				() -> medicaoBC.enviarConvenente(medicaoDTO));

		assertEquals("Parâmetro idMedicao não pode ser nulo", exception.getMessage());
		
	}
	
	//Caso 2: Entrada com medição não localizada.
	@Test
	void testEnviarConvenente_ReencaminharComplementacao_MedicaoNaoEncontrada() {
		
		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setId(123123123L);
		
		when(medicaoDao.consultarMedicao(medicaoDTO.getId())).thenReturn(null);

		assertThrowsMedicaoRestException(MessageKey.ERRO_MEDICAO_NAO_ENCONTRADA,
				() -> medicaoBC.enviarConvenente(medicaoDTO));
		
		
	}
	
	
	//Caso 3: Entrada com Contrato não localizado.
	@Test
	void testEnviarConvenente_ReencaminharComplementacao_ContratoNaoEncontrado() {
		
		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setId(123123123L);
		
		MedicaoBD medicao = newMedicaoBuilder().setId(1L).comSituacao(CE).create();
		
		ContratoBD contratoBD = newContratoMedicaoBuilder().setId(1L).create();
		contratoBD.setContratoFk(1L);
		
		when(medicaoDao.consultarMedicao(medicaoDTO.getId())).thenReturn(medicao);
		when(contratoBC.consultarContratoAssociadoMedicao(medicaoDTO.getId())).thenReturn(contratoBD);
		when(contratoBC.consultarContratoPorId(contratoBD.getContratoFk())).thenThrow(new MedicaoRestException(MessageKey.CONTRATO_INEXISTENTE));
		
		assertThrowsMedicaoRestException(MessageKey.CONTRATO_INEXISTENTE,
				() -> medicaoBC.enviarConvenente(medicaoDTO));
	}
	
	
	//Caso 4: Entrada com CNPJ Inválido.
	@Test
	void testEnviarConvenente_ReencaminharComplementacao_CNPJInvalido() {
		
		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setId(123123123L);
		
		MedicaoBD medicao = newMedicaoBuilder().setId(1L).comSituacao(CE).create();
		
		ContratoBD contratoBD = newContratoMedicaoBuilder().setId(1L).create();
		contratoBD.setContratoFk(1L);
		
		when(medicaoDao.consultarMedicao(medicaoDTO.getId())).thenReturn(medicao);
		when(contratoBC.consultarContratoAssociadoMedicao(medicaoDTO.getId())).thenReturn(contratoBD);	
		when(contratoBC.consultarContratoPorId(contratoBD.getContratoFk())).thenThrow(new MedicaoRestException(MessageKey.ERRO_EMPRESA_INEXISTENTE));
		
		assertThrowsMedicaoRestException(MessageKey.ERRO_EMPRESA_INEXISTENTE,
				() -> medicaoBC.enviarConvenente(medicaoDTO));
	}
	
	
	//Caso 5: Enviar Convenente de uma medição Agrupada.
	@Test
	void testEnviarConvenente_ReencaminharComplementacao_ValidarPermiteEnvioMedicao_MedicaoAgrupada() {
		
		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setId(123123123L);
		
		MedicaoBD medicao = newMedicaoBuilder().setId(1L).comSituacao(CE).setAgrupadora(1L).create();
		
		ContratoBD contratoBD = newContratoMedicaoBuilder().setId(1L).create();
		contratoBD.setContratoFk(1L);
		contratoBD.setCnpjFornecedor("23123321000112");
		
		ContratoSiconvDTO contratoSiconv = new ContratoSiconvDTO();
		contratoSiconv.setId(1L);
		contratoSiconv.setCnpj("23123321000112");
		
		when(medicaoDao.consultarMedicao(medicaoDTO.getId())).thenReturn(medicao);
		when(contratoBC.consultarContratoAssociadoMedicao(medicaoDTO.getId())).thenReturn(contratoBD);
		when(contratoBC.consultarContratoPorId(contratoBD.getContratoFk())).thenReturn(contratoSiconv);	
	
		assertThrowsMedicaoRestException(MessageKey.ERRO_ENVIO_CONVENENTE_MEDICAO_NAO_PERMITIDA,
				() -> medicaoBC.enviarConvenente(medicaoDTO));
	}
	
	
	//Caso 6: Enviar Convenente com medição em situação diferente de "Em Elaboração" e "Em Complementação"
	@Test
	void testEnviarConvenente_ReencaminharComplementacao_ValidarPermiteEnvioMedicao_SitMedDifElaboracaoeComplementacao() {
		
		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setId(123123123L);
		
		MedicaoBD medicao = newMedicaoBuilder().setId(1L).comSituacao(ECE).create();
		
		ContratoBD contratoBD = newContratoMedicaoBuilder().setId(1L).create();
		contratoBD.setContratoFk(1L);
		contratoBD.setCnpjFornecedor("23123321000112");
		
		ContratoSiconvDTO contratoSiconv = new ContratoSiconvDTO();
		contratoSiconv.setId(1L);
		contratoSiconv.setCnpj("23123321000112");
		
		when(medicaoDao.consultarMedicao(medicaoDTO.getId())).thenReturn(medicao);
		when(contratoBC.consultarContratoAssociadoMedicao(medicaoDTO.getId())).thenReturn(contratoBD);
		when(contratoBC.consultarContratoPorId(contratoBD.getContratoFk())).thenReturn(contratoSiconv);	
	
		assertThrowsMedicaoRestException(MessageKey.ERRO_ENVIO_CONVENENTE_MEDICAO_NAO_PERMITIDA,
				() -> medicaoBC.enviarConvenente(medicaoDTO));
	}

	//Caso 7: Medição Bloqueada
	@Test
	void testEnviarConvenente_ReencaminharComplementacao_ValidarPermiteEnvioMedicao_MedicaoBloqueada() {
		
		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setId(123123123L);
		
		MedicaoBD medicao = newMedicaoBuilder().setId(1L).setAgrupadora(null).setBloqueada(true).comSituacao(CE).create();
		
		ContratoBD contratoBD = newContratoMedicaoBuilder().setId(1L).create();
		contratoBD.setContratoFk(1L);
		contratoBD.setCnpjFornecedor("23123321000112");
		
		ContratoSiconvDTO contratoSiconv = new ContratoSiconvDTO();
		contratoSiconv.setId(1L);
		contratoSiconv.setCnpj("23123321000112");
		
		when(medicaoDao.consultarMedicao(medicaoDTO.getId())).thenReturn(medicao);
		when(contratoBC.consultarContratoAssociadoMedicao(medicaoDTO.getId())).thenReturn(contratoBD);
		when(contratoBC.consultarContratoPorId(contratoBD.getContratoFk())).thenReturn(contratoSiconv);	
	
		assertThrowsMedicaoRestException(MessageKey.ERRO_MEDICAO_BLOQUEADA,
				() -> medicaoBC.enviarConvenente(medicaoDTO));
	}	
	
	//Caso 8: Medição sem Data Início de Obra
	@Test
	void testEnviarConvenente_ReencaminharComplementacao_ValidarPermiteEnvioMedicao_MedicaoSemDataInicioObra() {
		
		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setId(123123123L);
		medicaoDTO.setDataInicio(LocalDate.of(2019, 5, 22));		
		
		MedicaoBD medicao = newMedicaoBuilder().setId(1L).setAgrupadora(null).setBloqueada(false).comSituacao(CE).create();
		
		ContratoBD contratoBD = newContratoMedicaoBuilder().setId(1L).create();
		contratoBD.setContratoFk(1L);
		contratoBD.setCnpjFornecedor("23123321000112");
		
		ContratoSiconvDTO contratoSiconv = new ContratoSiconvDTO();
		contratoSiconv.setId(1L);
		contratoSiconv.setCnpj("23123321000112");
		
		when(medicaoDao.consultarMedicao(medicaoDTO.getId())).thenReturn(medicao);
		when(contratoBC.consultarContratoAssociadoMedicao(medicaoDTO.getId())).thenReturn(contratoBD);
		when(contratoBC.consultarContratoPorId(contratoBD.getContratoFk())).thenReturn(contratoSiconv);	
	
		assertThrowsMedicaoRestException(MessageKey.ERRO_DATA_INICIO_OBRIGATORIA,
				() -> medicaoBC.enviarConvenente(medicaoDTO));
	}	
	
	//Caso 9: Medição sem Data Início da Medição
	@Test
	void testEnviarConvenente_ReencaminharComplementacao_ValidarPermiteEnvioMedicao_MedicaoSemDataInicio() {
		
		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setId(123123123L);
		medicaoDTO.setDataInicioObra(LocalDate.of(2019, 5, 22));	
		
		MedicaoBD medicao = newMedicaoBuilder().setId(1L).setAgrupadora(null).setBloqueada(false).comSituacao(CE).create();
		
		ContratoBD contratoBD = newContratoMedicaoBuilder().setId(1L).create();
		contratoBD.setContratoFk(1L);
		contratoBD.setCnpjFornecedor("23123321000112");
		
		ContratoSiconvDTO contratoSiconv = new ContratoSiconvDTO();
		contratoSiconv.setId(1L);
		contratoSiconv.setCnpj("23123321000112");
		
		when(medicaoDao.consultarMedicao(medicaoDTO.getId())).thenReturn(medicao);
		when(contratoBC.consultarContratoAssociadoMedicao(medicaoDTO.getId())).thenReturn(contratoBD);
		when(contratoBC.consultarContratoPorId(contratoBD.getContratoFk())).thenReturn(contratoSiconv);	
	
		assertThrowsMedicaoRestException(MessageKey.ERRO_DATA_INICIO_OBRIGATORIA,
				() -> medicaoBC.enviarConvenente(medicaoDTO));
	}		
	
	//Caso 10: Parâmetro Versão não informado.
	@Test
	void testEnviarConvenente_ReencaminharComplementacao_ValidarPermiteEnvioMedicao() {
		
		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setId(123123123L);
		medicaoDTO.setDataInicioObra(LocalDate.of(2019, 5, 22));	
		medicaoDTO.setDataInicio(LocalDate.of(2019, 5, 23));
		medicaoDTO.setVersao(null);
		
		MedicaoBD medicao = newMedicaoBuilder().setId(1L).setAgrupadora(null).setBloqueada(false).comSituacao(CE).create();
		
		ContratoBD contratoBD = newContratoMedicaoBuilder().setId(1L).create();
		contratoBD.setContratoFk(1L);
		contratoBD.setCnpjFornecedor("23123321000112");
		
		ContratoSiconvDTO contratoSiconv = new ContratoSiconvDTO();
		contratoSiconv.setId(1L);
		contratoSiconv.setCnpj("23123321000112");
		
		when(medicaoDao.consultarMedicao(medicaoDTO.getId())).thenReturn(medicao);
		when(contratoBC.consultarContratoAssociadoMedicao(medicaoDTO.getId())).thenReturn(contratoBD);
		when(contratoBC.consultarContratoPorId(contratoBD.getContratoFk())).thenReturn(contratoSiconv);	
	
		assertThrowsMedicaoRestException(MessageKey.ERRO_PARAMETRO_OBRIGATORIO_NAO_INFORMADO,
				() -> medicaoBC.enviarConvenente(medicaoDTO));
	}		
	
	//Caso 11: Validar data início da obra	
	@Test
	void testEnviarConvenente_ReencaminharComplementacao_ValidarDataInicioObra() {
		
		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setId(123123123L);
		medicaoDTO.setDataInicioObra(LocalDate.now().plusDays(2));	
		medicaoDTO.setDataInicio(LocalDate.of(2019, 5, 23));
		medicaoDTO.setVersao(1L);
		
		MedicaoBD medicao = newMedicaoBuilder().setId(1L).setAgrupadora(null).setBloqueada(false).comSituacao(CE).create();
		
		ContratoBD contratoBD = newContratoMedicaoBuilder().setId(1L).create();
		contratoBD.setContratoFk(1L);
		contratoBD.setCnpjFornecedor("23123321000112");
		
		ContratoSiconvDTO contratoSiconv = new ContratoSiconvDTO();
		contratoSiconv.setId(1L);
		contratoSiconv.setCnpj("23123321000112");
		
		when(medicaoDao.consultarMedicao(medicaoDTO.getId())).thenReturn(medicao);
		when(contratoBC.consultarContratoAssociadoMedicao(medicaoDTO.getId())).thenReturn(contratoBD);
		when(contratoBC.consultarContratoPorId(contratoBD.getContratoFk())).thenReturn(contratoSiconv);	
	
		assertThrowsMedicaoRestException(MessageKey.ERRO_DATA_INICIO_OBJETO_MAIOR_QUE_ATUAL,
				() -> medicaoBC.enviarConvenente(medicaoDTO));
	}		
	
	
	//Caso 12: Data Início anterior à Data Fim	
	@Test
	void testEnviarConvenente_ReencaminharComplementacao_DataInicioAnteriorDataFim() {
		
		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setId(123123123L);
		medicaoDTO.setDataInicioObra(LocalDate.of(2019, 5, 15));	
		medicaoDTO.setDataInicio(LocalDate.of(2019, 5, 23));
		medicaoDTO.setDataFim(LocalDate.of(2019, 5, 20));		
		medicaoDTO.setVersao(1L);
		
		MedicaoBD medicao = newMedicaoBuilder().setId(1L).setAgrupadora(null).setBloqueada(false).comSituacao(CE).create();
		
		ContratoBD contratoBD = newContratoMedicaoBuilder().setId(1L).create();
		contratoBD.setContratoFk(1L);
		contratoBD.setCnpjFornecedor("23123321000112");
		
		ContratoSiconvDTO contratoSiconv = new ContratoSiconvDTO();
		contratoSiconv.setId(1L);
		contratoSiconv.setCnpj("23123321000112");
		
		when(medicaoDao.consultarMedicao(medicaoDTO.getId())).thenReturn(medicao);
		when(contratoBC.consultarContratoAssociadoMedicao(medicaoDTO.getId())).thenReturn(contratoBD);
		when(contratoBC.consultarContratoPorId(contratoBD.getContratoFk())).thenReturn(contratoSiconv);	
	
		assertThrowsMedicaoRestException(MessageKey.ERRO_DATA_INICIO_ANTERIOR_DATA_FIM,
				() -> medicaoBC.enviarConvenente(medicaoDTO));
	}		
	
//	//Caso 13: Data Início Anterior à Data Assinatura do Contrato	
//	@Test
//	void testEnviarConvenente_ReencaminharComplementacao_DataInicioAnteriorDataAssinaturaContrato() {
//		
//		MedicaoDTO medicaoDTO = new MedicaoDTO();
//		medicaoDTO.setId(123123123L);
//		medicaoDTO.setDataInicioObra(LocalDate.of(2019, 5, 10));	
//		medicaoDTO.setDataInicio(LocalDate.of(2019, 5, 23));
//		medicaoDTO.setDataFim(LocalDate.of(2019, 5, 25));		
//		medicaoDTO.setVersao(1L);
//		
//		MedicaoBD medicao = newMedicaoBuilder().setId(1L).setAgrupadora(null).setBloqueada(false).comSituacao(CE).create();
//		
//		ContratoBD contratoBD = newContratoMedicaoBuilder().setId(1L).create();
//		contratoBD.setContratoFk(1L);
//		contratoBD.setCnpjFornecedor("23123321000112");
//		
//		ContratoSiconvDTO contratoSiconv = new ContratoSiconvDTO();
//		contratoSiconv.setId(1L);
//		contratoSiconv.setCnpj("23123321000112");
//		contratoSiconv.setDtAssinatura(LocalDate.of(2019, 5, 15));
//		
//		when(medicaoDao.consultarMedicao(medicaoDTO.getId())).thenReturn(medicao);
//		when(contratoBC.consultarContratoAssociadoMedicao(medicaoDTO.getId())).thenReturn(contratoBD);
//		when(contratoBC.consultarContratoPorId(contratoBD.getContratoFk())).thenReturn(contratoSiconv);	
//	
//		assertThrowsMedicaoRestException(MessageKey.ERRO_DATA_INICIO_ANTERIOR_DATA_ASSINATURA_CONTRATO,
//				() -> medicaoBC.enviarConvenente(medicaoDTO));
//	}		
	
//	//Caso 14: Data fim da medição é posterior à data fim de vigência do contrato.
//	@Test
//	void testEnviarConvenente_ReencaminharComplementacao_DataFimMedicaoMaiorDataFimVigencia() {
//		
//		MedicaoDTO medicaoDTO = new MedicaoDTO();
//		medicaoDTO.setId(1L);
//		medicaoDTO.setDataInicioObra(LocalDate.of(2019, 5, 15));	
//		medicaoDTO.setDataInicio(LocalDate.of(2019, 5, 23));
//		medicaoDTO.setDataFim(LocalDate.of(2019, 5, 26));		
//		medicaoDTO.setVersao(1L);
//		
//		MedicaoBD medicao = newMedicaoBuilder().setId(1L).setAgrupadora(null).setBloqueada(false).comSituacao(CE).create();
//		
//		ContratoBD contratoBD = newContratoMedicaoBuilder().setId(1L).create();
//		contratoBD.setContratoFk(1L);
//		contratoBD.setCnpjFornecedor("23123321000112");
//		
//		ContratoSiconvDTO contratoSiconv = new ContratoSiconvDTO();
//		contratoSiconv.setId(1L);
//		contratoSiconv.setCnpj("23123321000112");
//		contratoSiconv.setDtAssinatura(LocalDate.of(2019, 5, 10));
//		contratoSiconv.setDtFimVigencia(LocalDate.of(2019, 5, 25));
//		
//		when(medicaoDao.consultarMedicao(medicaoDTO.getId())).thenReturn(medicao);
//		when(contratoBC.consultarContratoAssociadoMedicao(medicaoDTO.getId())).thenReturn(contratoBD);
//		when(contratoBC.consultarContratoPorId(contratoBD.getContratoFk())).thenReturn(contratoSiconv);	
//	
//		assertThrowsMedicaoRestException(MessageKey.ERRO_DATA_FIM_MEDICAO_IGUAL_OU_ANTERIOR_DATA_FIM_VIGENCIA_CTEF,
//				() -> medicaoBC.enviarConvenente(medicaoDTO));
//	}	
	
	//Caso 15: Valida para a primeira medição se a Data Início Obra é igual à Data Incio da Medição.	
	@Test
	void testEnviarConvenente_ReencaminharComplementacao_DataInicioObraIgualDataInicioMedicao() {
		
		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setId(1L);		
		medicaoDTO.setDataInicioObra(LocalDate.of(2019, 5, 15));	
		medicaoDTO.setDataInicio(LocalDate.of(2019, 5, 23));
		medicaoDTO.setDataFim(LocalDate.of(2019, 5, 25));		
		medicaoDTO.setVersao(1L);
		medicaoDTO.setSequencial((short)1);
		
		MedicaoBD medicao = newMedicaoBuilder().setId(1L).setAgrupadora(null).setBloqueada(false).comSituacao(CE).setNrSequencial((short) 1).create();
		
		ContratoBD contratoBD = newContratoMedicaoBuilder().setId(1L).create();
		contratoBD.setContratoFk(1L);
		contratoBD.setCnpjFornecedor("23123321000112");
		
		ContratoSiconvDTO contratoSiconv = new ContratoSiconvDTO();
		contratoSiconv.setId(1L);
		contratoSiconv.setCnpj("23123321000112");
		contratoSiconv.setDtAssinatura(LocalDate.of(2019, 5, 10));
		contratoSiconv.setDtFimVigencia(LocalDate.of(2019, 5, 26));
		
		when(medicaoDao.consultarMedicao(medicaoDTO.getId())).thenReturn(medicao);
		when(contratoBC.consultarContratoAssociadoMedicao(medicaoDTO.getId())).thenReturn(contratoBD);
		when(contratoBC.consultarContratoPorId(contratoBD.getContratoFk())).thenReturn(contratoSiconv);	
		
		assertThrowsMedicaoRestException(MessageKey.ERRO_DATA_INICIO_OBRA_DIFERENTE_DATA_INICIO_MEDICAO,
				() -> medicaoBC.enviarConvenente(medicaoDTO));
	}	

	//Caso 16: Validar se data de Início da Medição que está sendo cadastrada é o dia seguinte da data fim da Medição anterior.	
	@Test
	void testEnviarConvenente_ReencaminharComplementacao_DataInicioMedicaoPosteriorDataFimMedicaoAnterior() {
		
		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setId(2L);		
		medicaoDTO.setDataInicioObra(LocalDate.of(2019, 5, 15));	
		medicaoDTO.setDataInicio(LocalDate.of(2019, 5, 15));
		medicaoDTO.setDataFim(LocalDate.of(2019, 5, 25));		
		medicaoDTO.setVersao(1L);
		medicaoDTO.setSequencial((short)2);
		
		MedicaoBD medicao = newMedicaoBuilder().setId(2L).setAgrupadora(null).setBloqueada(false).comSituacao(CE).setNrSequencial((short) 2).create();
		MedicaoBD medicaoAnterior = newMedicaoBuilder().setId(1L).setAgrupadora(null).setBloqueada(false).comSituacao(CE).setNrSequencial((short) 1).create();
		medicaoAnterior.setDtFim(LocalDate.of(2019, 5, 16));
		
		ContratoBD contratoBD = newContratoMedicaoBuilder().setId(1L).create();
		contratoBD.setContratoFk(1L);
		contratoBD.setCnpjFornecedor("23123321000112");
		
		ContratoSiconvDTO contratoSiconv = new ContratoSiconvDTO();
		contratoSiconv.setId(1L);
		contratoSiconv.setCnpj("23123321000112");
		contratoSiconv.setDtAssinatura(LocalDate.of(2019, 5, 10));
		contratoSiconv.setDtFimVigencia(LocalDate.of(2019, 5, 26));
		
		when(medicaoDao.consultarMedicao(medicaoDTO.getId())).thenReturn(medicao);
		when(contratoBC.consultarContratoAssociadoMedicao(medicaoDTO.getId())).thenReturn(contratoBD);
		when(contratoBC.consultarContratoPorId(contratoBD.getContratoFk())).thenReturn(contratoSiconv);
		when(medicaoDao.consultarMedicaoPorSequencial(contratoSiconv.getId(), medicaoAnterior.getNrSequencial())).thenReturn(medicaoAnterior);
		
		assertThrowsMedicaoRestException(MessageKey.ERRO_DATA_INICIO_MEDICAO_DIFERENTE_DIA_SEGUINTE_MEDICAO_ANTERIOR,
				() -> medicaoBC.enviarConvenente(medicaoDTO));
	}	

	//Caso 17: Validar Obrigatoriedade da Data Fim
	@Test
	void testEnviarConvenente_ReencaminharComplementacao_ValidarObrigatoriedadeDataFim() {
		
		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setId(2L);		
		medicaoDTO.setDataInicioObra(LocalDate.of(2019, 5, 15));	
		medicaoDTO.setDataInicio(LocalDate.of(2019, 5, 15));
		medicaoDTO.setDataFim(null);		
		medicaoDTO.setVersao(1L);
		medicaoDTO.setSequencial((short)1);
		
		MedicaoBD medicao = newMedicaoBuilder().setId(2L).setAgrupadora(null).setBloqueada(false).comSituacao(CE).setNrSequencial((short) 1).create();
				
		ContratoBD contratoBD = newContratoMedicaoBuilder().setId(1L).create();
		contratoBD.setContratoFk(1L);
		contratoBD.setCnpjFornecedor("23123321000112");
		
		ContratoSiconvDTO contratoSiconv = new ContratoSiconvDTO();
		contratoSiconv.setId(1L);
		contratoSiconv.setCnpj("23123321000112");
		contratoSiconv.setDtAssinatura(LocalDate.of(2019, 5, 10));
		contratoSiconv.setDtFimVigencia(LocalDate.of(2019, 5, 26));
		
		when(medicaoDao.consultarMedicao(medicaoDTO.getId())).thenReturn(medicao);
		when(contratoBC.consultarContratoAssociadoMedicao(medicaoDTO.getId())).thenReturn(contratoBD);
		when(contratoBC.consultarContratoPorId(contratoBD.getContratoFk())).thenReturn(contratoSiconv);
		
		assertThrowsMedicaoRestException(MessageKey.ERRO_PARAMETRO_OBRIGATORIO_NAO_INFORMADO,
				() -> medicaoBC.enviarConvenente(medicaoDTO));
	}	

	//Caso 18: Validar medição sem submetas
	@Test
	void testEnviarConvenente_ReencaminharComplementacao_MedicaoSemSubmetas() {
		
		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setId(2L);		
		medicaoDTO.setDataInicioObra(LocalDate.of(2019, 5, 15));	
		medicaoDTO.setDataInicio(LocalDate.of(2019, 5, 15));
		medicaoDTO.setDataFim(LocalDate.of(2019, 5, 25));		
		medicaoDTO.setVersao(1L);
		medicaoDTO.setSequencial((short)1);
		
		MedicaoBD medicao = newMedicaoBuilder().setId(2L).setAgrupadora(null).setBloqueada(false).comSituacao(CE).setNrSequencial((short) 1).create();
				
		ContratoBD contratoBD = newContratoMedicaoBuilder().setId(1L).create();
		contratoBD.setContratoFk(1L);
		contratoBD.setCnpjFornecedor("23123321000112");
		
		ContratoSiconvDTO contratoSiconv = new ContratoSiconvDTO();
		contratoSiconv.setId(1L);
		contratoSiconv.setCnpj("23123321000112");
		contratoSiconv.setDtAssinatura(LocalDate.of(2019, 5, 10));
		contratoSiconv.setDtFimVigencia(LocalDate.of(2019, 5, 26));
		
		List<SubmetaMedicaoBD> listaSubmetasMedicao = new ArrayList<SubmetaMedicaoBD>();
		
		when(medicaoDao.consultarMedicao(medicaoDTO.getId())).thenReturn(medicao);
		when(contratoBC.consultarContratoAssociadoMedicao(medicaoDTO.getId())).thenReturn(contratoBD);
		when(contratoBC.consultarContratoPorId(contratoBD.getContratoFk())).thenReturn(contratoSiconv);
		when(submetaDao.buscarListaSubmetasporMedicao(medicao.getId())).thenReturn(listaSubmetasMedicao);
		
		assertThrowsMedicaoRestException(MessageKey.ERRO_MEDICAO_SEM_SUBMETAS,
				() -> medicaoBC.enviarConvenente(medicaoDTO));
	}	
	
	
	//Caso 19: Validar se existe pelo menos uma submeta assinada
	@Test
	void testEnviarConvenente_ReencaminharComplementacao_ValidarExisteAoMenosUmaSubmetaAssinada() {
		
		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setId(2L);		
		medicaoDTO.setDataInicioObra(LocalDate.of(2019, 5, 15));	
		medicaoDTO.setDataInicio(LocalDate.of(2019, 5, 15));
		medicaoDTO.setDataFim(LocalDate.of(2019, 5, 25));		
		medicaoDTO.setVersao(1L);
		medicaoDTO.setSequencial((short)1);
		
		MedicaoBD medicao = newMedicaoBuilder().setId(2L).setAgrupadora(null).setBloqueada(false).comSituacao(CE).setNrSequencial((short) 1).setMedContrato(1L).create();
				
		ContratoBD contratoBD = newContratoMedicaoBuilder().setId(1L).create();
		contratoBD.setContratoFk(1L);
		contratoBD.setCnpjFornecedor("23123321000112");
		
		ContratoSiconvDTO contratoSiconv = new ContratoSiconvDTO();
		contratoSiconv.setId(1L);
		contratoSiconv.setCnpj("23123321000112");
		contratoSiconv.setDtAssinatura(LocalDate.of(2019, 5, 10));
		contratoSiconv.setDtFimVigencia(LocalDate.of(2019, 5, 26));
		
		
		List<SubmetaMedicaoBD> listaSubmetasMedicao = new ArrayList<SubmetaMedicaoBD>();
		
		SubmetaMedicaoBD submetaMedicaoBD = new SubmetaMedicaoBD();
		submetaMedicaoBD.setSituacaoEmpresa(SituacaoSubmetaEnum.RAS);		
		listaSubmetasMedicao.add(submetaMedicaoBD);
			
		when(medicaoDao.consultarMedicao(medicaoDTO.getId())).thenReturn(medicao);
		when(contratoBC.consultarContratoAssociadoMedicao(medicaoDTO.getId())).thenReturn(contratoBD);
		when(contratoBC.consultarContratoPorId(contratoBD.getContratoFk())).thenReturn(contratoSiconv);
		when(submetaDao.buscarListaSubmetasporMedicao(medicao.getId())).thenReturn(listaSubmetasMedicao);
		
		assertThrowsMedicaoRestException(MessageKey.ERRO_SUBMETAS_NAO_ASSINADAS_ENVIO_COMPLEMENTACAO,
				() -> medicaoBC.enviarConvenente(medicaoDTO));
	}	
	
	
	//Caso 20: Validar gravação dos dados para Enviar Convenente Sem EM Elaboracao
	@Test
	void testEnviarConvenente_ReencaminharComplementacao_ValidarGravacaoEnviarConvenenteSemEmElaboracao() {
		
		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setId(4L);		
		medicaoDTO.setDataInicioObra(LocalDate.of(2019, 5, 15));	
		medicaoDTO.setDataInicio(LocalDate.of(2019, 5, 16));
		medicaoDTO.setDataFim(LocalDate.of(2019, 5, 25));		
		medicaoDTO.setVersao(1L);
		medicaoDTO.setSequencial((short)4);
		
		MedicaoBD medicao = newMedicaoBuilder().setId(4L).setAgrupadora(null).setBloqueada(false).comSituacao(CE).setNrSequencial((short) 4).setMedContrato(1L).create();	
		MedicaoBD medicaoFilha = newMedicaoBuilder().setId(3L).setAgrupadora(4L).setBloqueada(false).comSituacao(CE).setNrSequencial((short) 3).setMedContrato(1L).create();						
		medicaoFilha.setDtFim(LocalDate.of(2019, 5, 15));
		
		ContratoBD contratoBD = newContratoMedicaoBuilder().setId(1L).create();
		contratoBD.setContratoFk(1L);
		contratoBD.setCnpjFornecedor("23123321000112");
		
		ContratoSiconvDTO contratoSiconv = new ContratoSiconvDTO();
		contratoSiconv.setId(1L);
		contratoSiconv.setCnpj("23123321000112");
		contratoSiconv.setDtAssinatura(LocalDate.of(2019, 5, 10));
		contratoSiconv.setDtFimVigencia(LocalDate.of(2019, 5, 26));
		
		
		List<SubmetaMedicaoBD> listaSubmetasMedicao = new ArrayList<SubmetaMedicaoBD>();
		
		SubmetaMedicaoBD submetaMedicaoBD = new SubmetaMedicaoBD();
		submetaMedicaoBD.setSituacaoEmpresa(SituacaoSubmetaEnum.ASS);
		submetaMedicaoBD.setSituacaoConvenente(SituacaoSubmetaEnum.ASS);
		listaSubmetasMedicao.add(submetaMedicaoBD);
		
		SubmetaMedicaoBD submetaMedicaoApenasConvenente = new SubmetaMedicaoBD();
		submetaMedicaoApenasConvenente.setSituacaoConvenente(SituacaoSubmetaEnum.ASS);
		listaSubmetasMedicao.add(submetaMedicaoApenasConvenente);
		
		List<MedicaoBD> medicaoEmElaboracao = new ArrayList<MedicaoBD>();
		
		List<MedicaoBD> listaMedicoesAcumuladas = new ArrayList<MedicaoBD>(); 
		listaMedicoesAcumuladas.add(medicaoFilha);
				
		when(medicaoDao.consultarMedicao(medicaoDTO.getId())).thenReturn(medicao);
		when(contratoBC.consultarContratoAssociadoMedicao(medicaoDTO.getId())).thenReturn(contratoBD);
		when(contratoBC.consultarContratoPorId(contratoBD.getContratoFk())).thenReturn(contratoSiconv);
		when(submetaDao.buscarListaSubmetasporMedicao(medicao.getId())).thenReturn(listaSubmetasMedicao);
		when(medicaoDao.consultarMedicaoporSituacao(medicao.getIdContratoMedicao(), SituacaoMedicaoEnum.EM)).thenReturn(medicaoEmElaboracao);
		when(medicaoDao.listarMedicoesAcumuladas(medicao.getId())).thenReturn(listaMedicoesAcumuladas);
		when(medicaoDao.consultarMedicaoPorSequencial(contratoSiconv.getId(), medicaoFilha.getNrSequencial())).thenReturn(medicaoFilha);		
		
		medicaoBC.enviarConvenente(medicaoDTO);

		verify(medicaoDao, times(2)).alterar(medicaoCaptor.capture());
		List<MedicaoBD> listaMedicaoBD = medicaoCaptor.getAllValues();

		verify(historicoMedicaoBC, times(2)).inserir(historicoCaptor.capture());
		List<HistoricoMedicaoBD> listaHistoricoMedicaoBD = historicoCaptor.getAllValues();
		
		// medicao filha
		assertEquals(SituacaoMedicaoEnum.EC, listaMedicaoBD.get(1).getSituacao());
		assertEquals(medicao.id, listaMedicaoBD.get(1).getIdMedicaoAgrupadora());
		
		// medicao agrupadora
		assertEquals(SituacaoMedicaoEnum.EC, listaMedicaoBD.get(0).getSituacao());
		assertEquals(null, listaMedicaoBD.get(0).getIdMedicaoAgrupadora());
		assertEquals(medicao.getDtInicio(), listaMedicaoBD.get(0).getDtInicio());
		assertEquals(medicao.getDtFim(), listaMedicaoBD.get(0).getDtFim());
		assertEquals(medicao.getSituacao(), listaMedicaoBD.get(0).getSituacao());
		assertEquals(medicao.getVersao(), listaMedicaoBD.get(0).getVersao());
		
		// historico filha
		assertEquals(medicaoFilha.getNrSequencial(), listaHistoricoMedicaoBD.get(1).getNrSequencial());
		assertEquals(medicaoFilha.idContratoMedicao, listaHistoricoMedicaoBD.get(1).getIdContratoMedicao());
		assertEquals(SituacaoMedicaoEnum.EC, listaHistoricoMedicaoBD.get(1).getSituacao());
		
		// historico
		assertEquals(medicao.nrSequencial, listaHistoricoMedicaoBD.get(0).getNrSequencial());
		assertEquals(medicao.idContratoMedicao, listaHistoricoMedicaoBD.get(0).getIdContratoMedicao());
		assertEquals(SituacaoMedicaoEnum.EC, listaHistoricoMedicaoBD.get(0).getSituacao());		
	}
	
	//Caso 21: Validar gravação dos dados para Enviar Convenente Com medição posterior EM Elaboracao
	@Test
	void testEnviarConvenente_ReencaminharComplementacao_ValidarGravacaoEnviarConvenentecomEmElaboracao() {
		
		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setId(4L);		
		medicaoDTO.setDataInicioObra(LocalDate.of(2019, 5, 15));	
		medicaoDTO.setDataInicio(LocalDate.of(2019, 5, 16));
		medicaoDTO.setDataFim(LocalDate.of(2019, 5, 25));		
		medicaoDTO.setVersao(1L);
		medicaoDTO.setSequencial((short)4);
		
		List<MedicaoBD> medicaoEmElaboracao = new ArrayList<MedicaoBD>();
		medicaoEmElaboracao.add(newMedicaoBuilder().setId(5L).setAgrupadora(null).setBloqueada(true).comSituacao(EM).setNrSequencial((short) 5).setMedContrato(1L).create());		 
		MedicaoBD medicao = newMedicaoBuilder().setId(4L).setAgrupadora(null).setBloqueada(false).comSituacao(CE).setNrSequencial((short) 4).setMedContrato(1L).create();	
		MedicaoBD medicaoFilha = newMedicaoBuilder().setId(3L).setAgrupadora(4L).setBloqueada(false).comSituacao(CE).setNrSequencial((short) 3).setMedContrato(1L).create();						
		medicaoFilha.setDtFim(LocalDate.of(2019, 5, 15));
		
		ContratoBD contratoBD = newContratoMedicaoBuilder().setId(1L).create();
		contratoBD.setContratoFk(1L);
		contratoBD.setCnpjFornecedor("23123321000112");
		
		ContratoSiconvDTO contratoSiconv = new ContratoSiconvDTO();
		contratoSiconv.setId(1L);
		contratoSiconv.setCnpj("23123321000112");
		contratoSiconv.setDtAssinatura(LocalDate.of(2019, 5, 10));
		contratoSiconv.setDtFimVigencia(LocalDate.of(2019, 5, 26));
		
		
		List<SubmetaMedicaoBD> listaSubmetasMedicao = new ArrayList<SubmetaMedicaoBD>();
		
		SubmetaMedicaoBD submetaMedicaoBD = new SubmetaMedicaoBD();
		submetaMedicaoBD.setSituacaoEmpresa(SituacaoSubmetaEnum.ASS);		
		listaSubmetasMedicao.add(submetaMedicaoBD);
		
		List<MedicaoBD> listaMedicoesAcumuladas = new ArrayList<MedicaoBD>(); 
		listaMedicoesAcumuladas.add(medicaoFilha);
				
		when(medicaoDao.consultarMedicao(medicaoDTO.getId())).thenReturn(medicao);
		when(contratoBC.consultarContratoAssociadoMedicao(medicaoDTO.getId())).thenReturn(contratoBD);
		when(contratoBC.consultarContratoPorId(contratoBD.getContratoFk())).thenReturn(contratoSiconv);
		when(submetaDao.buscarListaSubmetasporMedicao(medicao.getId())).thenReturn(listaSubmetasMedicao);
		when(medicaoDao.consultarMedicaoporSituacao(medicao.getIdContratoMedicao(), SituacaoMedicaoEnum.EM)).thenReturn(medicaoEmElaboracao);
		when(medicaoDao.listarMedicoesAcumuladas(medicao.getId())).thenReturn(listaMedicoesAcumuladas);
		when(medicaoDao.consultarMedicaoPorSequencial(contratoSiconv.getId(), medicaoFilha.getNrSequencial())).thenReturn(medicaoFilha);		
		
		medicaoBC.enviarConvenente(medicaoDTO);
		
		verify(medicaoDao, times(3)).alterar(medicaoCaptor.capture());
		List<MedicaoBD> listaMedicaoBD = medicaoCaptor.getAllValues();

		verify(historicoMedicaoBC, times(2)).inserir(historicoCaptor.capture());
		List<HistoricoMedicaoBD> listaHistoricoMedicaoBD = historicoCaptor.getAllValues();
		
		//Medicao Em Elaboracao
		assertEquals(SituacaoMedicaoEnum.EM, listaMedicaoBD.get(0).getSituacao());
		assertEquals(medicaoEmElaboracao.get(0).getId(), listaMedicaoBD.get(0).getId());
		assertEquals(medicaoEmElaboracao.get(0).isBloqueada(), listaMedicaoBD.get(0).isBloqueada());
		
		// medicao filha
		assertEquals(SituacaoMedicaoEnum.EC, listaMedicaoBD.get(2).getSituacao());
		assertEquals(medicao.id, listaMedicaoBD.get(2).getIdMedicaoAgrupadora());
		
		// medicao agrupadora
		assertEquals(SituacaoMedicaoEnum.EC, listaMedicaoBD.get(1).getSituacao());
		assertEquals(null, listaMedicaoBD.get(1).getIdMedicaoAgrupadora());
		assertEquals(medicao.getDtInicio(), listaMedicaoBD.get(1).getDtInicio());
		assertEquals(medicao.getDtFim(), listaMedicaoBD.get(1).getDtFim());
		assertEquals(medicao.getSituacao(), listaMedicaoBD.get(1).getSituacao());
		assertEquals(medicao.getVersao(), listaMedicaoBD.get(1).getVersao());
		
		// historico filha
		assertEquals(medicaoFilha.getNrSequencial(), listaHistoricoMedicaoBD.get(1).getNrSequencial());
		assertEquals(medicaoFilha.idContratoMedicao, listaHistoricoMedicaoBD.get(1).getIdContratoMedicao());
		assertEquals(SituacaoMedicaoEnum.EC, listaHistoricoMedicaoBD.get(1).getSituacao());
		
		// historico
		assertEquals(medicao.nrSequencial, listaHistoricoMedicaoBD.get(0).getNrSequencial());
		assertEquals(medicao.idContratoMedicao, listaHistoricoMedicaoBD.get(0).getIdContratoMedicao());
		assertEquals(SituacaoMedicaoEnum.EC, listaHistoricoMedicaoBD.get(0).getSituacao());		
	}		

	// Caso 22: Validar gravação dos dados para Enviar Convenente com medição em
	// complementação que não permite alteração de valor e datas
	@Test
	void testEnviarConvenente_medicaoNaoPermiteComplementacaoComAlteracao() {

		LocalDate dataPersistida = LocalDate.of(2021, 5, 1);
		LocalDate novaDataInformada = LocalDate.of(2022, 2, 2);

		MedicaoBD medicaoBD = newMedicaoBuilder()
				.setId(1L)
				.setNrSequencial((short) 1)
				.comSituacao(CE)
				.setMedContrato(1L)
				.setDtInicioMedicao(dataPersistida)
				.setDtFimMedicao(dataPersistida)
				.setPermiteComplementacaoValor(false)
				.create();

		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setId(1L);
		medicaoDTO.setDataInicioObra(novaDataInformada);
		medicaoDTO.setDataInicio(novaDataInformada);
		medicaoDTO.setDataFim(novaDataInformada);
		medicaoDTO.setVersao(10L);

		ContratoBD contratoBD = newContratoMedicaoBuilder()
				.setId(1L)
				.setDataInicioObra(dataPersistida)
				.setContratoFk(1L)
				.create();

		SubmetaMedicaoBD submetaMedicaoBD = new SubmetaMedicaoBD();
		submetaMedicaoBD.setSituacaoEmpresa(SituacaoSubmetaEnum.ASS);

		when(medicaoDao.consultarMedicao(medicaoDTO.getId())).thenReturn(medicaoBD);
		when(contratoBC.consultarContratoAssociadoMedicao(medicaoDTO.getId())).thenReturn(contratoBD);
		when(submetaDao.buscarListaSubmetasporMedicao(medicaoBD.getId())).thenReturn(List.of(submetaMedicaoBD));

		medicaoBC.enviarConvenente(medicaoDTO);

		verify(medicaoDao, times(1)).alterar(medicaoCaptor.capture());

		assertEquals(dataPersistida, contratoBD.getDataInicioObra());
		assertEquals(dataPersistida, medicaoCaptor.getValue().getDtInicio());
		assertEquals(dataPersistida, medicaoCaptor.getValue().getDtFim());
	}

	private void assertThrowsMedicaoRestException(MessageKey expectedMessageKey, Executable executable) {

		MedicaoRestException exception = assertThrows(MedicaoRestException.class, executable);

		exception.getMessages().stream().map(Message::getKey).findFirst().ifPresentOrElse(
				actualMessageKey -> assertEquals(expectedMessageKey, actualMessageKey),
				() -> fail(format("A messageKey esperada era %s, mas nenhuma foi obtida", expectedMessageKey)));
	}

}
