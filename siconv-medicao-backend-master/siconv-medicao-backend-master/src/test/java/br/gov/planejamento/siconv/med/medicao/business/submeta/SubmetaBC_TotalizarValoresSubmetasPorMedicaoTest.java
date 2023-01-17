package br.gov.planejamento.siconv.med.medicao.business.submeta;

import static br.gov.planejamento.siconv.med.test.builder.ContratoMedicaoBuilder.newContratoMedicaoBuilder;
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

import br.gov.planejamento.siconv.med.contrato.dao.ContratoDAO;
import br.gov.planejamento.siconv.med.contrato.entity.ContratoBD;
import br.gov.planejamento.siconv.med.infra.database.DAOFactory;
import br.gov.planejamento.siconv.med.infra.security.SecurityContext;
import br.gov.planejamento.siconv.med.integration.contratos.ContratosGrpcConsumer;
import br.gov.planejamento.siconv.med.integration.siconv.SiconvGRPCConsumer;
import br.gov.planejamento.siconv.med.integration.vrpl.VrplGRPCConsumer;
import br.gov.planejamento.siconv.med.medicao.business.SubmetaBC;
import br.gov.planejamento.siconv.med.medicao.business.builder.AbstractSubmetaMedicaoStep;
import br.gov.planejamento.siconv.med.medicao.business.builder.CalculoValoresSubmetaPorEventoStep;
import br.gov.planejamento.siconv.med.medicao.business.builder.FiltroMarcacoesEventosSubmetaStep;
import br.gov.planejamento.siconv.med.medicao.business.builder.SubmetaMedicaoBuilder;
import br.gov.planejamento.siconv.med.medicao.business.builder.SubmetaMedicaoBuilder.Context;
import br.gov.planejamento.siconv.med.medicao.business.builder.SubmetaMedicaoBuilder.Pipeline;
import br.gov.planejamento.siconv.med.medicao.dao.ItemMedicaoDAO;
import br.gov.planejamento.siconv.med.medicao.dao.MedicaoDAO;
import br.gov.planejamento.siconv.med.medicao.dao.SubmetaDAO;
import br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum;
import br.gov.planejamento.siconv.med.medicao.entity.database.SubmetaMedicaoBD;
import br.gov.planejamento.siconv.med.medicao.entity.dto.EventoVrplDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.FrenteObraVrplDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.MedicaoDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.SubmetaMedicaoDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.SubmetaVrplDTO;
import br.gov.planejamento.siconv.med.test.builder.ContextBuilder;

class SubmetaBC_TotalizarValoresSubmetasPorMedicaoTest {

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
	private ArgumentCaptor<List <SubmetaMedicaoDTO>> subMedicaoCaptor;
	
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
	void totalizarValoresSubmetasPorMedicao() {
				
		ContratoBD contrato = newContratoMedicaoBuilder().setId(1L)
				.setContratoSiconv(7L).create();
		
		Map<Long, SituacaoMedicaoEnum> cacheSituacaoMedicao = new HashMap<Long, SituacaoMedicaoEnum>();
		
		List<SubmetaMedicaoBD> cacheSubmetaMedicao = new ArrayList<SubmetaMedicaoBD>();
		
		List<MedicaoDTO> medicoesDTO = new ArrayList<MedicaoDTO>();

		//
		MedicaoDTO medicaoDTO1 = new MedicaoDTO();
		medicaoDTO1.setId(1L);
		medicaoDTO1.setIdContrato(contrato.id);
		medicaoDTO1.setSituacao(SituacaoMedicaoEnum.EM);
		medicaoDTO1.setIdMedicaoAgrupadora(null);
		medicaoDTO1.setBloqueada(false);
		medicoesDTO.add(medicaoDTO1);
		
		FrenteObraVrplDTO frenteObra = new FrenteObraVrplDTO();
		EventoVrplDTO evento = new EventoVrplDTO();
		List<EventoVrplDTO> eventos = new ArrayList<EventoVrplDTO>();
		List<FrenteObraVrplDTO> frentesObras = new ArrayList<FrenteObraVrplDTO>();
		evento.setPermiteMarcacao(true);
		evento.setId(1L);
		eventos.add(evento);
		frenteObra.setEventos(eventos);
		frenteObra.setId(1L);
		frentesObras.add(frenteObra);
		
		List<SubmetaMedicaoDTO> submetasMedicao = new ArrayList<SubmetaMedicaoDTO>();
		SubmetaMedicaoDTO submetaMedicao1 = new SubmetaMedicaoDTO();		
		SubmetaMedicaoDTO submetaMedicao2 = new SubmetaMedicaoDTO();
		SubmetaMedicaoDTO submetaMedicao3 = new SubmetaMedicaoDTO();		
		submetaMedicao1.setId(1L);
		submetaMedicao1.setPermiteMarcacaoEmpresa(true);
		submetaMedicao1.setFrentesObra(frentesObras);
		submetaMedicao1.setValor(new BigDecimal("10.00"));
		submetaMedicao1.setValorRealizadoEmpresa(new BigDecimal("10.00"));
		submetaMedicao1.setValorRealizadoAcumuladoEmpresa(new BigDecimal("10.00"));
		submetaMedicao1.setValorRealizadoConvenente(new BigDecimal("0"));
		submetaMedicao1.setValorRealizadoAcumuladoConvenente(new BigDecimal("0"));
		submetaMedicao1.setValorRealizadoConcedente(new BigDecimal("0"));
		submetaMedicao1.setValorRealizadoAcumuladoConcedente(new BigDecimal("0"));
		
		submetaMedicao2.setId(2L);
		submetaMedicao2.setPermiteMarcacaoEmpresa(true);
		submetaMedicao2.setFrentesObra(frentesObras);
		submetaMedicao2.setValor(new BigDecimal("10.00"));
		submetaMedicao2.setValorRealizadoEmpresa(new BigDecimal("10.00"));
		submetaMedicao2.setValorRealizadoAcumuladoEmpresa(new BigDecimal("10.00"));
		submetaMedicao2.setValorRealizadoConvenente(new BigDecimal("10.00"));
		submetaMedicao2.setValorRealizadoAcumuladoConvenente(new BigDecimal("10.00"));
		submetaMedicao2.setValorRealizadoConcedente(new BigDecimal("0"));
		submetaMedicao2.setValorRealizadoAcumuladoConcedente(new BigDecimal("0"));	
		
		submetaMedicao3.setId(2L);
		submetaMedicao3.setPermiteMarcacaoEmpresa(true);
		submetaMedicao3.setFrentesObra(frentesObras);
		submetaMedicao3.setValor(new BigDecimal("10.00"));
		submetaMedicao3.setValorRealizadoEmpresa(new BigDecimal("10.00"));
		submetaMedicao3.setValorRealizadoAcumuladoEmpresa(new BigDecimal("10.00"));
		submetaMedicao3.setValorRealizadoConvenente(new BigDecimal("10.00"));
		submetaMedicao3.setValorRealizadoAcumuladoConvenente(new BigDecimal("10.00"));
		submetaMedicao3.setValorRealizadoConcedente(new BigDecimal("10.00"));
		submetaMedicao3.setValorRealizadoAcumuladoConcedente(new BigDecimal("10.00"));		
				
		submetasMedicao.add(submetaMedicao1);
		submetasMedicao.add(submetaMedicao2);
		submetasMedicao.add(submetaMedicao3);
		
		List<SubmetaVrplDTO> submetasVrpl = new ArrayList<SubmetaVrplDTO>();
		SubmetaVrplDTO submeta1 = new SubmetaVrplDTO();
		submeta1.setId(1L);
		submeta1.setNrSubmetaAnalise("1.1");
		submeta1.setDescricao("Submeta 1");
		submeta1.setValor(new BigDecimal("10.00"));
		SubmetaVrplDTO submeta2 = new SubmetaVrplDTO();
		submeta2.setId(2L);
		submeta2.setNrSubmetaAnalise("2.1");
		submeta2.setDescricao("Submeta 2");
		submeta2.setValor(new BigDecimal("10.00"));
		SubmetaVrplDTO submeta3 = new SubmetaVrplDTO();
		submeta3.setId(3L);
		submeta3.setNrSubmetaAnalise("3.1");
		submeta3.setDescricao("Submeta 3");
		submeta3.setValor(new BigDecimal("10.00"));
		
		submetasVrpl.add(submeta1);
		submetasVrpl.add(submeta2);
		submetasVrpl.add(submeta3);

		Context context = ContextBuilder.newContextBuilder()
				.setContext(contrato, medicaoDTO1, cacheSituacaoMedicao, cacheSubmetaMedicao).create();

		doAnswer(invocation -> {
			new Pipeline(context)
				.add(new FiltroMarcacoesEventosSubmetaStep(securityContext))
				.add(new CalculoValoresSubmetaPorEventoStep(securityContext))
				.build(submetasMedicao);
			return null;
		}).when(pipeline).build(submetasMedicao);

		when(contratoDAO.consultarContrato(medicaoDTO1.getIdContrato())).thenReturn(contrato);
		when(contratosConsumer.listarSubmetasPorContratoId(contrato.getContratoFk())).thenReturn(submetasVrpl);
		when(medicaoDAO.listarSituacoesMedicoes(medicaoDTO1.getIdContrato())).thenReturn(cacheSituacaoMedicao);
		when(submetaDAO.consultarSubmetasMedicaoPorContrato(medicaoDTO1.getIdContrato())).thenReturn(cacheSubmetaMedicao);
		when(submetaDAO.listarSubmetasMedicao(medicaoDTO1.getIdContrato(), medicaoDTO1.getId())).thenReturn(submetasMedicao);
		
		submetaBC.totalizarValoresSubmetasPorMedicao(medicoesDTO);
		
		assertEquals(submetaMedicao1.getValorRealizadoEmpresa(), medicaoDTO1.getValorRealizadoEmpresa());
		assertEquals(submetaMedicao1.getValorRealizadoAcumuladoEmpresa(), medicaoDTO1.getValorRealizadoAcumuladoEmpresa());
		assertEquals(submetaMedicao1.getValorRealizadoEmpresa(), medicaoDTO1.getValorRealizadoConvenente());
		assertEquals(submetaMedicao1.getValorRealizadoAcumuladoEmpresa(), medicaoDTO1.getValorRealizadoAcumuladoConvenente());
		assertEquals(submetaMedicao1.getValorRealizadoEmpresa(), medicaoDTO1.getValorRealizadoConcedente());
		assertEquals(submetaMedicao1.getValorRealizadoAcumuladoEmpresa(), medicaoDTO1.getValorRealizadoAcumuladoConcedente());
		
		assertEquals(submetaMedicao1.getPercentualRealizadoEmpresa(), medicaoDTO1.getPercentualRealizadoEmpresa());
		assertEquals(submetaMedicao1.getPercentualRealizadoAcumuladoEmpresa(), medicaoDTO1.getPercentualRealizadoAcumuladoEmpresa());
		assertEquals(submetaMedicao1.getPercentualRealizadoConvenente(), medicaoDTO1.getPercentualRealizadoConvenente());
		assertEquals(submetaMedicao1.getPercentualRealizadoAcumuladoConvenente(), medicaoDTO1.getPercentualRealizadoAcumuladoConvenente());
		assertEquals(submetaMedicao1.getPercentualRealizadoConcedente(), medicaoDTO1.getPercentualRealizadoConcedente());
		assertEquals(submetaMedicao1.getPercentualRealizadoAcumuladoConcedente(), medicaoDTO1.getPercentualRealizadoAcumuladoConcedente());
			
	}
}
