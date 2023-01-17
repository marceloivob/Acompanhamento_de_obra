package br.gov.planejamento.siconv.med.medicao.business.builder;

import static br.gov.planejamento.siconv.med.test.builder.ContextBuilder.newContextBuilder;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import br.gov.planejamento.siconv.med.contrato.entity.ContratoBD;
import br.gov.planejamento.siconv.med.infra.security.SecurityContext;
import br.gov.planejamento.siconv.med.medicao.business.builder.CalculoValoresSubmetaPorServicoStep;
import br.gov.planejamento.siconv.med.medicao.business.builder.SubmetaMedicaoBuilder.Context;
import br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum;
import br.gov.planejamento.siconv.med.medicao.entity.SituacaoSubmetaEnum;
import br.gov.planejamento.siconv.med.medicao.entity.database.SubmetaMedicaoBD;
import br.gov.planejamento.siconv.med.medicao.entity.dto.FrenteObraVrplDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.MedicaoDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.ServicoVrplDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.SubmetaMedicaoDTO;
import br.gov.planejamento.siconv.med.test.extension.BusinessControllerBaseTest;

class CalculoValoresSubmetaPorServicoStepTest extends BusinessControllerBaseTest {
	
	@InjectMocks
	private CalculoValoresSubmetaPorServicoStep calculoValoresStep;
	
	@Mock
	private SecurityContext securityContext;
	
	@Test
	void testProcessCalculoValoresSubmetaEmpresa_semValor() {
		
		MedicaoDTO medicao = new MedicaoDTO();
		ContratoBD contrato = new ContratoBD();
		
		Map<Long, SituacaoMedicaoEnum> cacheSituacaoMedicao = new HashMap<>();
		cacheSituacaoMedicao.put(1L, SituacaoMedicaoEnum.EM);
		
		List<SubmetaMedicaoBD> cacheSubmetaMedicao = new ArrayList<SubmetaMedicaoBD>();
		
		Context contexto = newContextBuilder()
				.setContext(contrato, medicao, cacheSituacaoMedicao, cacheSubmetaMedicao)
				.create();
		
		SubmetaMedicaoDTO submetaMedicao = new SubmetaMedicaoDTO();
		
		calculoValoresStep.process(submetaMedicao, contexto);
		
		assertEquals(null, submetaMedicao.getValorRealizadoEmpresa());
	}
	
	@Test
	void testProcessCalculoValoresSubmetaEmpresa_comValor() {
		
		MedicaoDTO medicao = new MedicaoDTO();
		medicao.setId(2L);
		
		ContratoBD contrato = new ContratoBD();
		
		Map<Long, SituacaoMedicaoEnum> cacheSituacaoMedicao = new HashMap<>();
		cacheSituacaoMedicao.put(2L, SituacaoMedicaoEnum.EM);
		
		List<SubmetaMedicaoBD> cacheSubmetaMedicao = new ArrayList<SubmetaMedicaoBD>();
		
		Context contexto = newContextBuilder()
				.setContext(contrato, medicao, cacheSituacaoMedicao, cacheSubmetaMedicao)
				.create();
		
		SubmetaMedicaoDTO submetaMedicao = new SubmetaMedicaoDTO();
		submetaMedicao.setSituacaoEmpresa(SituacaoSubmetaEnum.RAS);
		submetaMedicao.setValor(new BigDecimal(200));
		
		List<FrenteObraVrplDTO> frentesObra = new ArrayList<>();
		FrenteObraVrplDTO fo = new FrenteObraVrplDTO();
		List<ServicoVrplDTO> servicos = new ArrayList<>();
		
		ServicoVrplDTO serv = new ServicoVrplDTO();
		serv.setValorRealizadoEmpresa(new BigDecimal(66.88));
		serv.setValorAcumuladoEmpresa(new BigDecimal(86.88));
		
		servicos.add(serv);
		fo.setServicos(servicos);
		frentesObra.add(fo);
		submetaMedicao.setFrentesObra(frentesObra);
		
		calculoValoresStep.process(submetaMedicao, contexto);
		
		assertEquals(new BigDecimal(33.44).setScale(2, RoundingMode.HALF_UP), 
				submetaMedicao.getPercentualRealizadoEmpresa().setScale(2, RoundingMode.HALF_UP));
		assertEquals(new BigDecimal(43.44).setScale(2, RoundingMode.HALF_UP), 
				submetaMedicao.getPercentualRealizadoAcumuladoEmpresa().setScale(2, RoundingMode.HALF_UP));
	}
	
	@Test
	void testProcessCalculoValoresSubmetaConvenente_semValor() {
		
		MedicaoDTO medicao = new MedicaoDTO();
		ContratoBD contrato = new ContratoBD();
		
		Map<Long, SituacaoMedicaoEnum> cacheSituacaoMedicao = new HashMap<>();
		cacheSituacaoMedicao.put(1L, SituacaoMedicaoEnum.ATD);
		
		List<SubmetaMedicaoBD> cacheSubmetaMedicao = new ArrayList<SubmetaMedicaoBD>();
		
		Context contexto = newContextBuilder()
				.setContext(contrato, medicao, cacheSituacaoMedicao, cacheSubmetaMedicao)
				.create();
		
		SubmetaMedicaoDTO submetaMedicao = new SubmetaMedicaoDTO();
		
		calculoValoresStep.process(submetaMedicao, contexto);
		
		assertEquals(null, submetaMedicao.getValorRealizadoConvenente());
	}
	
	@Test
	void testProcessCalculoValoresSubmetaConvenente_comValorZero() {		
		
		MedicaoDTO medicao = new MedicaoDTO();
		medicao.setId(2L);
		
		ContratoBD contrato = new ContratoBD();
		
		Map<Long, SituacaoMedicaoEnum> cacheSituacaoMedicao = new HashMap<>();
		cacheSituacaoMedicao.put(2L, SituacaoMedicaoEnum.ATD);
		
		List<SubmetaMedicaoBD> cacheSubmetaMedicao = new ArrayList<SubmetaMedicaoBD>();
		
		Context contexto = newContextBuilder()
				.setContext(contrato, medicao, cacheSituacaoMedicao, cacheSubmetaMedicao)
				.create();
		
		SubmetaMedicaoDTO submetaMedicao = new SubmetaMedicaoDTO();
		submetaMedicao.setSituacaoConvenente(SituacaoSubmetaEnum.ASS);
		submetaMedicao.setValor(new BigDecimal(200));
		
		List<FrenteObraVrplDTO> frentesObra = new ArrayList<>();
		FrenteObraVrplDTO fo = new FrenteObraVrplDTO();
		List<ServicoVrplDTO> servicos = new ArrayList<>();
		
		ServicoVrplDTO serv = new ServicoVrplDTO();
		serv.setValorRealizadoConvenente(new BigDecimal(0));
		serv.setValorAcumuladoConvenente(new BigDecimal(52.30));
		
		servicos.add(serv);
		fo.setServicos(servicos);
		frentesObra.add(fo);
		submetaMedicao.setFrentesObra(frentesObra);
		
		calculoValoresStep.process(submetaMedicao, contexto);
		
		assertEquals(new BigDecimal(0).setScale(2, RoundingMode.HALF_UP), 
				submetaMedicao.getPercentualRealizadoConvenente().setScale(2, RoundingMode.HALF_UP));
		assertEquals(new BigDecimal(26.15).setScale(2, RoundingMode.HALF_UP), 
				submetaMedicao.getPercentualRealizadoAcumuladoConvenente().setScale(2, RoundingMode.HALF_UP));
		
	}	

	@Test
	void testProcessCalculoValoresSubmetaConvenente_comValorRealizadoNulo() {		
		
		MedicaoDTO medicao = new MedicaoDTO();
		medicao.setId(2L);
		
		ContratoBD contrato = new ContratoBD();
		
		Map<Long, SituacaoMedicaoEnum> cacheSituacaoMedicao = new HashMap<>();
		cacheSituacaoMedicao.put(2L, SituacaoMedicaoEnum.ATD);
		
		List<SubmetaMedicaoBD> cacheSubmetaMedicao = new ArrayList<SubmetaMedicaoBD>();
		
		Context contexto = newContextBuilder()
				.setContext(contrato, medicao, cacheSituacaoMedicao, cacheSubmetaMedicao)
				.create();
		
		SubmetaMedicaoDTO submetaMedicao = new SubmetaMedicaoDTO();
		submetaMedicao.setSituacaoConvenente(SituacaoSubmetaEnum.ASS);
		submetaMedicao.setValor(new BigDecimal(200));
		
		List<FrenteObraVrplDTO> frentesObra = new ArrayList<>();
		FrenteObraVrplDTO fo = new FrenteObraVrplDTO();
		List<ServicoVrplDTO> servicos = new ArrayList<>();
		
		ServicoVrplDTO serv = new ServicoVrplDTO();
		serv.setValorRealizadoConvenente(null);
		serv.setValorAcumuladoConvenente(new BigDecimal(52.30));
		
		servicos.add(serv);
		fo.setServicos(servicos);
		frentesObra.add(fo);
		submetaMedicao.setFrentesObra(frentesObra);
		
		calculoValoresStep.process(submetaMedicao, contexto);
		
		assertEquals(new BigDecimal(0).setScale(2, RoundingMode.HALF_UP), 
				submetaMedicao.getValorRealizadoConvenente().setScale(2, RoundingMode.HALF_UP));		
		assertEquals(new BigDecimal(0).setScale(2, RoundingMode.HALF_UP), 
				submetaMedicao.getPercentualRealizadoConvenente().setScale(2, RoundingMode.HALF_UP));
		assertEquals(new BigDecimal(26.15).setScale(2, RoundingMode.HALF_UP), 
				submetaMedicao.getPercentualRealizadoAcumuladoConvenente().setScale(2, RoundingMode.HALF_UP));
		
	}	
	
	
	
	@Test
	void testProcessCalculoValoresSubmetaConvenente_comValor() {
		
		MedicaoDTO medicao = new MedicaoDTO();
		medicao.setId(2L);
		
		ContratoBD contrato = new ContratoBD();
		
		Map<Long, SituacaoMedicaoEnum> cacheSituacaoMedicao = new HashMap<>();
		cacheSituacaoMedicao.put(2L, SituacaoMedicaoEnum.EM);
		
		List<SubmetaMedicaoBD> cacheSubmetaMedicao = new ArrayList<SubmetaMedicaoBD>();
		
		Context contexto = newContextBuilder()
				.setContext(contrato, medicao, cacheSituacaoMedicao, cacheSubmetaMedicao)
				.create();
		
		SubmetaMedicaoDTO submetaMedicao = new SubmetaMedicaoDTO();
		submetaMedicao.setSituacaoConvenente(SituacaoSubmetaEnum.RAS);
		submetaMedicao.setValor(new BigDecimal(200));
		
		List<FrenteObraVrplDTO> frentesObra = new ArrayList<>();
		FrenteObraVrplDTO fo = new FrenteObraVrplDTO();
		List<ServicoVrplDTO> servicos = new ArrayList<>();
		
		ServicoVrplDTO serv = new ServicoVrplDTO();
		serv.setValorRealizadoConvenente(new BigDecimal(10.88));
		serv.setValorAcumuladoConvenente(new BigDecimal(22.75));
		
		servicos.add(serv);
		fo.setServicos(servicos);
		frentesObra.add(fo);
		submetaMedicao.setFrentesObra(frentesObra);
		
		calculoValoresStep.process(submetaMedicao, contexto);
		
		assertEquals(new BigDecimal(5.44).setScale(2, RoundingMode.HALF_UP), 
				submetaMedicao.getPercentualRealizadoConvenente().setScale(2, RoundingMode.HALF_UP));
		assertEquals(new BigDecimal(11.38).setScale(2, RoundingMode.HALF_UP), 
				submetaMedicao.getPercentualRealizadoAcumuladoConvenente().setScale(2, RoundingMode.HALF_UP));
	}
	
	

}
