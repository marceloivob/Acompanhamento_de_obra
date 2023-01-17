package br.gov.planejamento.siconv.med.medicao.business.submeta;

import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.EMPRESA;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.HandleCallback;
import org.jdbi.v3.core.HandleConsumer;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.dto.TipoResponsavelTecnicoEnum;
import br.gov.planejamento.siconv.med.contrato.dao.ContratoDAO;
import br.gov.planejamento.siconv.med.contrato.entity.ContratoBD;
import br.gov.planejamento.siconv.med.infra.database.DAOFactory;
import br.gov.planejamento.siconv.med.integration.contratos.ContratosGrpcConsumer;
import br.gov.planejamento.siconv.med.integration.siconv.SiconvGRPCConsumer;
import br.gov.planejamento.siconv.med.integration.vrpl.VrplGRPCConsumer;
import br.gov.planejamento.siconv.med.medicao.business.SubmetaBC;
import br.gov.planejamento.siconv.med.medicao.business.builder.AbstractSubmetaMedicaoStep;
import br.gov.planejamento.siconv.med.medicao.business.builder.FiltroSituacoesSubmetaStep;
import br.gov.planejamento.siconv.med.medicao.business.builder.SubmetaMedicaoBuilder;
import br.gov.planejamento.siconv.med.medicao.business.builder.SubmetaMedicaoBuilder.Context;
import br.gov.planejamento.siconv.med.medicao.business.builder.SubmetaMedicaoBuilder.Pipeline;
import br.gov.planejamento.siconv.med.medicao.dao.ItemMedicaoDAO;
import br.gov.planejamento.siconv.med.medicao.dao.MedicaoDAO;
import br.gov.planejamento.siconv.med.medicao.dao.SubmetaDAO;
import br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum;
import br.gov.planejamento.siconv.med.medicao.entity.SituacaoSubmetaEnum;
import br.gov.planejamento.siconv.med.medicao.entity.database.SubmetaMedicaoBD;
import br.gov.planejamento.siconv.med.medicao.entity.dto.MedicaoDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.SubmetaMedicaoDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.SubmetaVrplDTO;
import br.gov.planejamento.siconv.med.test.builder.ContextBuilder;
import br.gov.planejamento.siconv.med.infra.security.SecurityContext;
import br.gov.planejamento.siconv.med.infra.security.UsuarioLogado;

class SubmetaBC_RecuperarListaSubmetasPorMedicaoTest {

	@Mock
	private Jdbi jdbi;
	
	@Mock
	private DAOFactory dao;

	@Mock
	private Handle handle;

	@Mock
	private SecurityContext securityContext;
	
	@Mock
	private VrplGRPCConsumer vrplConsumer;
	
	@Mock
	private ContratosGrpcConsumer contratosConsumer;
	
	@Mock
	private SiconvGRPCConsumer siconvGRPCConsumer;
	
	@Mock
	private ContratoDAO contratoDAO;
	
	@Mock
	private MedicaoDAO medicaoDAO;
	
	@Mock
	private SubmetaDAO submetaDAO;
	
	@Mock
	private ItemMedicaoDAO itemMedicaoDAO;

	@Mock
	private SubmetaMedicaoBuilder submetaMedicaoBuilder;

	@Mock
	private Pipeline pipeline;

	@InjectMocks
	private SubmetaBC submetaBC;
	
	@Captor
	private ArgumentCaptor<SubmetaMedicaoBD> subMedicaoCaptor;
	
	@BeforeEach
	void setup() throws Exception {

		MockitoAnnotations.initMocks(this);

		dao = mock(DAOFactory.class);
		submetaBC.setDao(dao);
		when(dao.get(ContratoDAO.class)).thenReturn(contratoDAO);
		when(dao.get(MedicaoDAO.class)).thenReturn(medicaoDAO);
		when(dao.get(ItemMedicaoDAO.class)).thenReturn(itemMedicaoDAO);
		when(dao.get(SubmetaDAO.class)).thenReturn(submetaDAO);
		when(dao.getJdbi()).thenReturn(jdbi);
		
		when(handle.attach(ItemMedicaoDAO.class)).thenReturn(itemMedicaoDAO);
		when(handle.attach(SubmetaDAO.class)).thenReturn(submetaDAO);
		when(jdbi.onDemand(SubmetaDAO.class)).thenReturn(submetaDAO);
		
		when(jdbi.inTransaction(Mockito.any())).then(invocation -> {
			HandleCallback<?, ?> callback = invocation.getArgument(0);
			return callback.withHandle(handle);
		});

		doAnswer(invocation -> {
			HandleConsumer<?> consumer = invocation.getArgument(0);
			consumer.useHandle(handle);
			return null;
		}).when(jdbi).useTransaction(Mockito.any());

		when(pipeline.add(Mockito.any(Class.class))).thenReturn(pipeline);
		when(pipeline.add(Mockito.any(AbstractSubmetaMedicaoStep.class))).thenReturn(pipeline);
		when(pipeline.when(Mockito.anyBoolean())).thenReturn(pipeline);
		when(pipeline.orElse()).thenReturn(pipeline);
		when(pipeline.anyway()).thenReturn(pipeline);

		when(submetaMedicaoBuilder.of(Mockito.any(), Mockito.any())).thenReturn(pipeline);
	}
	
	@Test
	void testRecuperarListaSubmetasPorMedicao_MedicaoEmElaboracao_UsuarioDiferenteEmpresa() {
		
		Long idMedicao = 1L;
		
		MedicaoDTO medicao = new MedicaoDTO();		
		medicao.setId(1L);
		medicao.setSituacao(SituacaoMedicaoEnum.EM);
		
        //----------------------------------------------------------
		SubmetaMedicaoDTO submetaMedicao1 = new SubmetaMedicaoDTO();
		submetaMedicao1.setId(1L);
		submetaMedicao1.setSituacaoEmpresa(SituacaoSubmetaEnum.ASS);
        //----------------------------------------------------------		
		SubmetaMedicaoDTO submetaMedicao2 = new SubmetaMedicaoDTO();
		submetaMedicao2.setId(1L);
		submetaMedicao2.setSituacaoConvenente(SituacaoSubmetaEnum.ASS);
        //----------------------------------------------------------		
		SubmetaMedicaoDTO submetaMedicao3 = new SubmetaMedicaoDTO();
		submetaMedicao3.setId(1L);
		submetaMedicao3.setSituacaoConcedente(SituacaoSubmetaEnum.ASS);
        //----------------------------------------------------------		
		
		List<SubmetaMedicaoDTO> submetasMedicao = new ArrayList<SubmetaMedicaoDTO>();
		submetasMedicao.add(submetaMedicao1);
		
		ContratoBD contratoBD = new ContratoBD();
		contratoBD.setContratoFk(1L);
		contratoBD.setInAcompanhamentoEventos(true);
		
		SubmetaVrplDTO submetaVrplDTO = new SubmetaVrplDTO();
		submetaVrplDTO.setId(1L);
		submetaVrplDTO.setNrSubmetaAnalise("112312");
		submetaVrplDTO.setDescricao("Submeta do Contrato 1L");
		submetaVrplDTO.setValor(BigDecimal.valueOf(1000));
		List<SubmetaVrplDTO> submetasContrato = new ArrayList<SubmetaVrplDTO>();		
		submetasContrato.add(submetaVrplDTO);
		
		Map<Long, SituacaoMedicaoEnum> cacheSituacaoMedicao = new HashMap<Long, SituacaoMedicaoEnum>();
		cacheSituacaoMedicao.put(1L, SituacaoMedicaoEnum.EM);
		
		List<SubmetaMedicaoBD> cacheSubmetaMedicao  = new ArrayList<SubmetaMedicaoBD>();
		List<Long> listaIdMedicoesAcumuladas = new ArrayList<Long>();

		Context context = ContextBuilder.newContextBuilder()
				.setContext(contratoBD, medicao, cacheSituacaoMedicao, cacheSubmetaMedicao).create();

		doAnswer(invocation -> {
			new Pipeline(context)
				.add(new FiltroSituacoesSubmetaStep(securityContext))
				.build(submetasMedicao);
			return null;
		}).when(pipeline).build(submetasMedicao);

        when(medicaoDAO.obterMedicao(idMedicao)).thenReturn(medicao);
        when(contratoDAO.consultarContrato(medicao.getIdContrato())).thenReturn(contratoBD);
        when(contratosConsumer.listarSubmetasPorContratoId(contratoBD.getContratoFk())).thenReturn(submetasContrato);
        when(submetaDAO.listarSubmetasMedicao(medicao.getIdContrato(), idMedicao)).thenReturn(submetasMedicao);
        when(submetaDAO.listarSubmetasMedicao(medicao.getIdContrato(), idMedicao)).thenReturn(submetasMedicao);
        when(medicaoDAO.listarSituacoesMedicoes(medicao.getIdContrato())).thenReturn(cacheSituacaoMedicao);
        when(submetaDAO.consultarSubmetasMedicaoPorContrato(medicao.getIdContrato())).thenReturn(cacheSubmetaMedicao);
        when(medicaoDAO.listarIdMedicoesAcumuladas(medicao.getId())).thenReturn(listaIdMedicoesAcumuladas);
        when(securityContext.hasAnyPermissionInProfile(EMPRESA)).thenReturn(false);
        
        List<SubmetaMedicaoDTO> listaSubmetaMedicao = submetaBC.recuperarListaSubmetasPorMedicao(medicao.getId());	

		assertEquals(null, listaSubmetaMedicao.get(0).getSituacaoEmpresa());
		assertEquals(null, listaSubmetaMedicao.get(0).getSituacaoConvenente());
		assertEquals(null, listaSubmetaMedicao.get(0).getSituacaoConcedente());
	}

}
