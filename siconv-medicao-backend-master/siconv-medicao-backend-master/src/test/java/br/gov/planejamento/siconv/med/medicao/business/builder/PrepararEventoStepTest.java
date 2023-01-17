package br.gov.planejamento.siconv.med.medicao.business.builder;

import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.CONCEDENTE;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.EMPRESA;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.GUEST;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.MANDATARIA;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.PROPONENTE_CONVENENTE;
import static br.gov.planejamento.siconv.med.test.builder.ContextBuilder.newContextBuilder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import br.gov.planejamento.siconv.med.medicao.business.builder.SubmetaMedicaoBuilder.Context;
import br.gov.planejamento.siconv.med.medicao.dao.MedicaoDAO;
import br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum;
import br.gov.planejamento.siconv.med.medicao.entity.SituacaoSubmetaEnum;
import br.gov.planejamento.siconv.med.medicao.entity.dto.MedicaoDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.SubmetaMedicaoDTO;
import br.gov.planejamento.siconv.med.test.builder.SubmetaMedicaoDTOBuilder;
import br.gov.planejamento.siconv.med.test.extension.BusinessControllerBaseTest;
import br.gov.planejamento.siconv.med.test.extension.MockUsuario;


@TestMethodOrder(OrderAnnotation.class)
class PrepararEventoStepTest extends BusinessControllerBaseTest {
	
	@InjectMocks
	private PrepararEventoStep prepararEventoStep;
	
	@Mock
	private MedicaoDAO medicaoDAO;
	
	@BeforeEach
	void setup() throws Exception {
		setupDaoMock(MedicaoDAO.class, medicaoDAO);
	}
	
	@MockUsuario(profile = EMPRESA)
	@Test
	void testEventoNaoConcluidoPelaEmpresa() {
		
		MedicaoDTO medicao = new MedicaoDTO();
		medicao.setSituacao(SituacaoMedicaoEnum.EM);
		
		Context contexto = newContextBuilder()
				.setContext(null, medicao, null, null)
				.create();
				
		SubmetaMedicaoDTOBuilder submetaMedicaoBuilder = 
				SubmetaMedicaoDTOBuilder.newSubmetaMedicaoBuilder_ComEvento(null);
		submetaMedicaoBuilder.permiteMarcacaoEmpresa(true);
		submetaMedicaoBuilder.setSituacaoSubmetaEmpresa(SituacaoSubmetaEnum.RAS);
		
		SubmetaMedicaoDTO submetaMedicao = submetaMedicaoBuilder.create();
		
		when(medicaoDAO.obterMedicao(1L)).thenReturn(medicao);
		
		prepararEventoStep.process(submetaMedicao, contexto);
		
		assertTrue(submetaMedicao.getFrentesObra().get(0).getEventos().get(0).getPermiteMarcacao());
		assertFalse(submetaMedicao.getFrentesObra().get(0).getEventos().get(0).getIndRealizado());
	}
	
	@MockUsuario(profile = EMPRESA)
	@Test
	void testEventoConcluidoPelaEmpresa() {
		
		short sequencial = 1;
		
		MedicaoDTO medicao = new MedicaoDTO();
		medicao.setId(1L);
		medicao.setSituacao(SituacaoMedicaoEnum.EC);
		medicao.setSequencial(sequencial);
		
		Context contexto = newContextBuilder()
				.setContext(null, medicao, null, null)
				.create();
				
		SubmetaMedicaoDTOBuilder submetaMedicaoBuilder = 
				SubmetaMedicaoDTOBuilder.newSubmetaMedicaoBuilder_ComEvento(1L);
		submetaMedicaoBuilder.setEventoMedidoEmpresa(medicao.getId());
		
		SubmetaMedicaoDTO submetaMedicao = submetaMedicaoBuilder.create();
		
		when(medicaoDAO.obterMedicao(1L)).thenReturn(medicao);
		
		prepararEventoStep.process(submetaMedicao, contexto);
		
		assertFalse(submetaMedicao.getFrentesObra().get(0).getEventos().get(0).getPermiteMarcacao());
		assertEquals(medicao.getId(), submetaMedicao.getFrentesObra().get(0).getEventos().get(0).getIdMedicaoEmpresa());
	}

	@MockUsuario(profile = EMPRESA)
	@Test
	void testPrepararEvento_Empresa_MedicaoAceita() {
		
		short sequencial = 1;
		
		MedicaoDTO medicao = new MedicaoDTO();
		medicao.setId(1L);
		medicao.setSituacao(SituacaoMedicaoEnum.ACT);
		medicao.setSequencial(sequencial);
		
		Context contexto = newContextBuilder()
				.setContext(null, medicao, null, null)
				.create();
				
		SubmetaMedicaoDTOBuilder submetaMedicaoBuilder = 
				SubmetaMedicaoDTOBuilder.newSubmetaMedicaoBuilder_ComEvento(1L);
		submetaMedicaoBuilder.setEventoMedidoEmpresa(medicao.getId());
		submetaMedicaoBuilder.setEventoMedidoConvenente(medicao.getId());
		submetaMedicaoBuilder.setEventoMedidoConcedente(medicao.getId());
		
		SubmetaMedicaoDTO submetaMedicao = submetaMedicaoBuilder.create();
		
		when(medicaoDAO.obterMedicao(1L)).thenReturn(medicao);
		
		prepararEventoStep.process(submetaMedicao, contexto);
		
		assertFalse(submetaMedicao.getFrentesObra().get(0).getEventos().get(0).getPermiteMarcacao());
		assertEquals(medicao.getId(), submetaMedicao.getFrentesObra().get(0).getEventos().get(0).getNrSeqMedicaoConvenente());
		assertEquals(medicao.getId(), submetaMedicao.getFrentesObra().get(0).getEventos().get(0).getNrSeqMedicaoConcedente());
	}
	
	@MockUsuario(profile = PROPONENTE_CONVENENTE)
	@Test
	void testEventoNaoConcluidoPelaConvenente() {
		
		Short sequencial = 1;
		MedicaoDTO medicao = new MedicaoDTO();
		medicao.setId(1L);
		medicao.setSequencial(sequencial);
		medicao.setSituacao(SituacaoMedicaoEnum.EC);
		
		Context contexto = newContextBuilder()
				.setContext(null, medicao, null, null)
				.create();
				
		SubmetaMedicaoDTOBuilder submetaMedicaoBuilder = 
				SubmetaMedicaoDTOBuilder.newSubmetaMedicaoBuilder_ComEvento(1L);
		submetaMedicaoBuilder.setEventoMedidoEmpresa(medicao.getId());
		submetaMedicaoBuilder.permiteMarcacaoConvenente(true);
		
		SubmetaMedicaoDTO submetaMedicao = submetaMedicaoBuilder.create();
		
		when(medicaoDAO.obterMedicao(1L)).thenReturn(medicao);
		
		prepararEventoStep.process(submetaMedicao, contexto);
		
		assertTrue(submetaMedicao.getFrentesObra().get(0).getEventos().get(0).getPermiteMarcacao());
		assertNull(submetaMedicao.getFrentesObra().get(0).getEventos().get(0).getIdMedicaoConvenente());
	}
	
	@MockUsuario(profile = PROPONENTE_CONVENENTE)
	@Test
	void testEventoNaoConcluidoPelaConvenente_eventoGlosado() {
		
		Short sequencial = 1;
		MedicaoDTO medicao = new MedicaoDTO();
		medicao.setId(1L);
		medicao.setSequencial(sequencial);
		medicao.setSituacao(SituacaoMedicaoEnum.ATD);
		
		Context contexto = newContextBuilder()
				.setContext(null, medicao, null, null)
				.create();
				
		SubmetaMedicaoDTOBuilder submetaMedicaoBuilder = 
				SubmetaMedicaoDTOBuilder.newSubmetaMedicaoBuilder_ComEvento(1L);
		submetaMedicaoBuilder.setEventoMedidoEmpresa(medicao.getId());
		submetaMedicaoBuilder.permiteMarcacaoConvenente(true);
		submetaMedicaoBuilder.setSituacaoSubmetaConvenente(SituacaoSubmetaEnum.ASS);
		
		SubmetaMedicaoDTO submetaMedicao = submetaMedicaoBuilder.create();
		
		when(medicaoDAO.obterMedicao(1L)).thenReturn(medicao);
		
		prepararEventoStep.process(submetaMedicao, contexto);
		
		assertTrue(submetaMedicao.getFrentesObra().get(0).getEventos().get(0).getPermiteMarcacao());
		assertFalse(submetaMedicao.getFrentesObra().get(0).getEventos().get(0).getIndRealizado());
	}
	
	@MockUsuario(profile = PROPONENTE_CONVENENTE)
	@Test
	void testEventoConcluidoPelaConvenente() {
		
		Short sequencial = 1;
		MedicaoDTO medicao = new MedicaoDTO();
		medicao.setId(1L);
		medicao.setSequencial(sequencial);
		medicao.setSituacao(SituacaoMedicaoEnum.ATD);
		
		Context contexto = newContextBuilder()
				.setContext(null, medicao, null, null)
				.create();
				
		SubmetaMedicaoDTOBuilder submetaMedicaoBuilder = 
				SubmetaMedicaoDTOBuilder.newSubmetaMedicaoBuilder_ComEvento(1L);
		submetaMedicaoBuilder.permiteMarcacaoConvenente(true);
		submetaMedicaoBuilder.setEventoMedidoEmpresa(medicao.getId());
		submetaMedicaoBuilder.setEventoMedidoConvenente(medicao.getId());
		
		SubmetaMedicaoDTO submetaMedicao = submetaMedicaoBuilder.create();
		
		when(medicaoDAO.obterMedicao(1L)).thenReturn(medicao);
		
		prepararEventoStep.process(submetaMedicao, contexto);
		
		assertTrue(submetaMedicao.getFrentesObra().get(0).getEventos().get(0).getPermiteMarcacao());
		assertEquals(medicao.getId(), submetaMedicao.getFrentesObra().get(0).getEventos().get(0).getIdMedicaoConvenente());
	}
	
	@MockUsuario(profile = PROPONENTE_CONVENENTE)
	@Test
	void testPrepararEvento_Convenente_MedicaoAceita() {
		
		short sequencial = 1;
		
		MedicaoDTO medicao = new MedicaoDTO();
		medicao.setId(1L);
		medicao.setSituacao(SituacaoMedicaoEnum.ACT);
		medicao.setSequencial(sequencial);
		
		Context contexto = newContextBuilder()
				.setContext(null, medicao, null, null)
				.create();
				
		SubmetaMedicaoDTOBuilder submetaMedicaoBuilder = 
				SubmetaMedicaoDTOBuilder.newSubmetaMedicaoBuilder_ComEvento(1L);
		submetaMedicaoBuilder.setEventoMedidoEmpresa(medicao.getId());
		submetaMedicaoBuilder.setEventoMedidoConvenente(medicao.getId());
		submetaMedicaoBuilder.setEventoMedidoConcedente(medicao.getId());
		
		SubmetaMedicaoDTO submetaMedicao = submetaMedicaoBuilder.create();
		
		when(medicaoDAO.obterMedicao(1L)).thenReturn(medicao);
		
		prepararEventoStep.process(submetaMedicao, contexto);
		
		assertFalse(submetaMedicao.getFrentesObra().get(0).getEventos().get(0).getPermiteMarcacao());
		assertEquals(medicao.getId(), submetaMedicao.getFrentesObra().get(0).getEventos().get(0).getNrSeqMedicaoConcedente());
	}
	
	@MockUsuario(profile = MANDATARIA)
	@Test
	void testEventoNaoConcluidoPelaMandataria() {
		
		Short sequencial = 1;
		MedicaoDTO medicao = new MedicaoDTO();
		medicao.setId(1L);
		medicao.setSequencial(sequencial);
		medicao.setSituacao(SituacaoMedicaoEnum.ATD);
		
		Context contexto = newContextBuilder()
				.setContext(null, medicao, null, null)
				.create();
				
		SubmetaMedicaoDTOBuilder submetaMedicaoBuilder = 
				SubmetaMedicaoDTOBuilder.newSubmetaMedicaoBuilder_ComEvento(1L);
		submetaMedicaoBuilder.setEventoMedidoConvenente(medicao.getId());
		submetaMedicaoBuilder.permiteMarcacaoConcedente(true);
		
		SubmetaMedicaoDTO submetaMedicao = submetaMedicaoBuilder.create();
		
		when(medicaoDAO.obterMedicao(1L)).thenReturn(medicao);
		
		prepararEventoStep.process(submetaMedicao, contexto);
		
		assertTrue(submetaMedicao.getFrentesObra().get(0).getEventos().get(0).getPermiteMarcacao());
		assertNull(submetaMedicao.getFrentesObra().get(0).getEventos().get(0).getIdMedicaoConcedente());
	}
	
	@MockUsuario(profile = CONCEDENTE)
	@Test
	void testEventoNaoConcluidoPeloConcedente() {
		
		Short sequencial = 1;
		MedicaoDTO medicao = new MedicaoDTO();
		medicao.setId(1L);
		medicao.setSequencial(sequencial);
		medicao.setSituacao(SituacaoMedicaoEnum.ATD);
		
		Context contexto = newContextBuilder()
				.setContext(null, medicao, null, null)
				.create();
				
		SubmetaMedicaoDTOBuilder submetaMedicaoBuilder = 
				SubmetaMedicaoDTOBuilder.newSubmetaMedicaoBuilder_ComEvento(1L);
		submetaMedicaoBuilder.setEventoMedidoConvenente(medicao.getId());
		submetaMedicaoBuilder.permiteMarcacaoConcedente(true);
		
		SubmetaMedicaoDTO submetaMedicao = submetaMedicaoBuilder.create();
		
		when(medicaoDAO.obterMedicao(1L)).thenReturn(medicao);
		
		prepararEventoStep.process(submetaMedicao, contexto);
		
		assertTrue(submetaMedicao.getFrentesObra().get(0).getEventos().get(0).getPermiteMarcacao());
		assertNull(submetaMedicao.getFrentesObra().get(0).getEventos().get(0).getIdMedicaoConcedente());
	}
	
	@MockUsuario(profile = CONCEDENTE)
	@Test
	void testEventoConcluidoPeloConcedente() {
		
		Short sequencial = 1;
		MedicaoDTO medicao = new MedicaoDTO();
		medicao.setId(1L);
		medicao.setSequencial(sequencial);
		medicao.setSituacao(SituacaoMedicaoEnum.ATD);
		
		Context contexto = newContextBuilder()
				.setContext(null, medicao, null, null)
				.create();
				
		SubmetaMedicaoDTOBuilder submetaMedicaoBuilder = 
				SubmetaMedicaoDTOBuilder.newSubmetaMedicaoBuilder_ComEvento(1L);
		submetaMedicaoBuilder.permiteMarcacaoConcedente(true);
		submetaMedicaoBuilder.setEventoMedidoConvenente(medicao.getId());
		submetaMedicaoBuilder.setEventoMedidoConcedente(medicao.getId());
		
		SubmetaMedicaoDTO submetaMedicao = submetaMedicaoBuilder.create();
		
		when(medicaoDAO.obterMedicao(1L)).thenReturn(medicao);
		
		prepararEventoStep.process(submetaMedicao, contexto);
		
		assertTrue(submetaMedicao.getFrentesObra().get(0).getEventos().get(0).getPermiteMarcacao());
		assertEquals(medicao.getId(), submetaMedicao.getFrentesObra().get(0).getEventos().get(0).getIdMedicaoConcedente());
	}
	
	@MockUsuario(profile = GUEST)
	@Test
	void testPrepararEvento_guest() {
		
		Short sequencial = 1;
		MedicaoDTO medicao = new MedicaoDTO();
		medicao.setId(1L);
		medicao.setSequencial(sequencial);
		medicao.setSituacao(SituacaoMedicaoEnum.ACT);
		
		Context contexto = newContextBuilder()
				.setContext(null, medicao, null, null)
				.create();
				
		SubmetaMedicaoDTOBuilder submetaMedicaoBuilder = 
				SubmetaMedicaoDTOBuilder.newSubmetaMedicaoBuilder_ComEvento(1L);
		submetaMedicaoBuilder.setEventoMedidoConvenente(medicao.getId());
		submetaMedicaoBuilder.setEventoMedidoConcedente(medicao.getId());
		
		SubmetaMedicaoDTO submetaMedicao = submetaMedicaoBuilder.create();
		
		when(medicaoDAO.obterMedicao(1L)).thenReturn(medicao);
		
		prepararEventoStep.process(submetaMedicao, contexto);
		
		assertFalse(submetaMedicao.getFrentesObra().get(0).getEventos().get(0).getPermiteMarcacao());
	}
}
