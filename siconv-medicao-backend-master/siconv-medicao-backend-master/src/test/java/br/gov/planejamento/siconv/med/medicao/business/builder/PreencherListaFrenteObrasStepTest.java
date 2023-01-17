package br.gov.planejamento.siconv.med.medicao.business.builder;

import static br.gov.planejamento.siconv.med.test.builder.ContextBuilder.newContextBuilder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import br.gov.planejamento.siconv.med.infra.message.MessageKey;
import br.gov.planejamento.siconv.med.integration.vrpl.VrplGRPCConsumer;
import br.gov.planejamento.siconv.med.medicao.business.builder.PreencherListaFrenteObrasStep;
import br.gov.planejamento.siconv.med.medicao.business.builder.SubmetaMedicaoBuilder.Context;
import br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum;
import br.gov.planejamento.siconv.med.medicao.entity.dto.MedicaoDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.SubmetaMedicaoDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.SubmetaVrplDTO;
import br.gov.planejamento.siconv.med.test.builder.SubmetaMedicaoDTOBuilder;
import br.gov.planejamento.siconv.med.test.builder.SubmetaVrplDTOBuilder;
import br.gov.planejamento.siconv.med.test.extension.BusinessControllerBaseTest;

class PreencherListaFrenteObrasStepTest extends BusinessControllerBaseTest {

	@InjectMocks
	private PreencherListaFrenteObrasStep preencherStep;
	
	@Mock
	private VrplGRPCConsumer vrplConsumer;
	
	@Test
	void testPreencherLista_SubmetaInexistente() {
		
		MedicaoDTO medicao = new MedicaoDTO();
		medicao.setSituacao(SituacaoMedicaoEnum.EM);
		
		Context contexto = newContextBuilder()
				.setContext(null, medicao, null, null)
				.create();
		
		BigDecimal qtdPlanejadaServico = BigDecimal.valueOf(0.00);
		
		SubmetaMedicaoDTOBuilder submetaMedicaoBuilder = 
				SubmetaMedicaoDTOBuilder.newSubmetaMedicaoBuilder_ComServico(qtdPlanejadaServico);
		
		SubmetaMedicaoDTO submetaMedicao = submetaMedicaoBuilder.create();
		
		when(vrplConsumer.getSubmetaPorId(1L)).thenReturn(null);
		
		assertThrowsMedicaoRestException(MessageKey.ERRO_SUBMETA_INEXISTENTE,
				() -> preencherStep.process(submetaMedicao, contexto));
		
	}
	
	@Test
	void testPreencherLista_CompletarDados_PLE() {
		
		MedicaoDTO medicao = new MedicaoDTO();
		
		Context contexto = newContextBuilder()
				.setContext(null, medicao, null, null)
				.create();
		
		SubmetaMedicaoDTOBuilder submetaMedicaoBuilder = 
				SubmetaMedicaoDTOBuilder.newSubmetaMedicaoBuilder_ComEvento(1L);
		
		SubmetaMedicaoDTO submetaMedicao = submetaMedicaoBuilder.create();
		
		SubmetaVrplDTOBuilder submetaVrplBuilder = 
				SubmetaVrplDTOBuilder.newSubmetaMedicaoBuilder_ComEvento(1L, true);
		submetaVrplBuilder.addDadosFrenteObra();
		
		SubmetaVrplDTO submetaVrpl = submetaVrplBuilder.create();
		submetaMedicao.setId(1L);
		
		Optional<SubmetaVrplDTO> submetaVrplOpt = Optional.ofNullable(submetaVrpl);
		
		when(vrplConsumer.getSubmetaPorId(1L)).thenReturn(submetaVrplOpt);
		
		preencherStep.process(submetaMedicao, contexto);
		
		assertEquals("Frente de Obra",submetaMedicao.getFrentesObra().get(0).getDescricao());
		assertEquals("Evento",submetaMedicao.getFrentesObra().get(0).getEventos().get(0).getDescricao());
		assertEquals("Serviço",submetaMedicao.getFrentesObra().get(0).getEventos().get(0)
				.getServicos().get(0).getDescricao());
		
	}
	
	@Test
	void testPreencherLista_CompletarDados_BM() {
		
		MedicaoDTO medicao = new MedicaoDTO();
		
		Context contexto = newContextBuilder()
				.setContext(null, medicao, null, null)
				.create();
		
		BigDecimal qtdePlanejada = new BigDecimal(50);
		
		SubmetaMedicaoDTOBuilder submetaMedicaoBuilder = 
				SubmetaMedicaoDTOBuilder.newSubmetaMedicaoBuilder_ComServico(qtdePlanejada);
		
		SubmetaMedicaoDTO submetaMedicao = submetaMedicaoBuilder.create();
		
		SubmetaVrplDTOBuilder submetaVrplBuilder = 
				SubmetaVrplDTOBuilder.newSubmetaMedicaoBuilder_ComServico(qtdePlanejada);
		submetaVrplBuilder.addDadosFrenteObra();
		
		SubmetaVrplDTO submetaVrpl = submetaVrplBuilder.create();
		submetaMedicao.setId(1L);
		
		Optional<SubmetaVrplDTO> submetaVrplOpt = Optional.ofNullable(submetaVrpl);
		
		when(vrplConsumer.getSubmetaPorId(1L)).thenReturn(submetaVrplOpt);
		
		preencherStep.process(submetaMedicao, contexto);
		
		assertEquals("Frente de Obra",submetaMedicao.getFrentesObra().get(0).getDescricao());
		assertEquals("Serviço",submetaMedicao.getFrentesObra().get(0).getMacroServicosView().get(0)
				.getServicos().get(0).getDescricao());
		
	}

}
