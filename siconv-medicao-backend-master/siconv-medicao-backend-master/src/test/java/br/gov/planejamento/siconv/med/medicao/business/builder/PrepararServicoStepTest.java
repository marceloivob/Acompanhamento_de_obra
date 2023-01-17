
package br.gov.planejamento.siconv.med.medicao.business.builder;

import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.CONCEDENTE;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.EMPRESA;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.PROPONENTE_CONVENENTE;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.CC;
import static br.gov.planejamento.siconv.med.test.builder.ContextBuilder.newContextBuilder;
import static java.math.BigDecimal.ZERO;
import static java.math.BigDecimal.valueOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.InjectMocks;

import br.gov.planejamento.siconv.med.infra.security.domain.Profile;
import br.gov.planejamento.siconv.med.medicao.business.builder.SubmetaMedicaoBuilder.Context;
import br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum;
import br.gov.planejamento.siconv.med.medicao.entity.dto.MedicaoDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.ServicoVrplDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.ServicoVrplDTO.ValorServicoBM;
import br.gov.planejamento.siconv.med.medicao.entity.dto.SubmetaMedicaoDTO;
import br.gov.planejamento.siconv.med.test.builder.SubmetaMedicaoDTOBuilder;
import br.gov.planejamento.siconv.med.test.extension.BusinessControllerBaseTest;
import br.gov.planejamento.siconv.med.test.extension.MockUsuario;


@TestMethodOrder(OrderAnnotation.class)
class PrepararServicoStepTest extends BusinessControllerBaseTest{
	
	
	@InjectMocks
	private PrepararServicoStep prepararServicoStep;
	
	@MockUsuario(profile = EMPRESA)
	@Test
	void testServicoNaoMedidoPelaEmpresa() {
		
		MedicaoDTO medicao = new MedicaoDTO();
		medicao.setSituacao(SituacaoMedicaoEnum.EM);
		medicao.setIsMedicaoAgrupadora(false);
		medicao.setId(2L);

		
		Context contexto = newContextBuilder()
				.setContext(null, medicao, null, null)
				.create();
		
		BigDecimal qtdPlanejadaServico = BigDecimal.valueOf(15.32);
		
		SubmetaMedicaoDTOBuilder submetaMedicaoBuilder = SubmetaMedicaoDTOBuilder.newSubmetaMedicaoBuilder_ComServico(qtdPlanejadaServico).permiteMarcacaoEmpresa(true);
		
		SubmetaMedicaoDTO submetaMedicao = submetaMedicaoBuilder.create();
		
		prepararServicoStep.process(submetaMedicao, contexto);
		
		assertTrue(submetaMedicao.getFrentesObra().get(0).getMacroServicosView().get(0).getServicos().get(0).isPermiteMedicao());
		assertEquals(qtdPlanejadaServico,submetaMedicao.getFrentesObra().get(0).getMacroServicosView().get(0).getServicos().get(0).getQtdMaxPermitido());
	}

	private void assertEquals(BigDecimal expected, BigDecimal actual) { 
		assertThat(actual, comparesEqualTo(expected));
	}

	@MockUsuario(profile = EMPRESA)
	@Test
	void testServicoNaoMedidoPelaEmpresa_ComMedicaoEmSituacaoNaoPermitidaEmpresa() {
		
		MedicaoDTO medicao = new MedicaoDTO();
		medicao.setSituacao(SituacaoMedicaoEnum.AT);
		medicao.setIsMedicaoAgrupadora(false);
		medicao.setId(2L);

		
		Context contexto = newContextBuilder()
				.setContext(null, medicao, null, null)
				.create();
		
		BigDecimal qtdPlanejadaServico = BigDecimal.valueOf(15.32);
		
		SubmetaMedicaoDTOBuilder submetaMedicaoBuilder = SubmetaMedicaoDTOBuilder.newSubmetaMedicaoBuilder_ComServico(qtdPlanejadaServico).permiteMarcacaoEmpresa(false);
		
		SubmetaMedicaoDTO submetaMedicao = submetaMedicaoBuilder.create();
		
		prepararServicoStep.process(submetaMedicao, contexto);
		
		assertFalse(submetaMedicao.getFrentesObra().get(0).getMacroServicosView().get(0).getServicos().get(0).isPermiteMedicao());
		assertEquals(BigDecimal.valueOf(0.00),submetaMedicao.getFrentesObra().get(0).getMacroServicosView().get(0).getServicos().get(0).getQtdMaxPermitido());
	}
	
	@MockUsuario(profile = EMPRESA)
	@Test
	void testServicoComQtdServicoPlanejado() {
		
		MedicaoDTO medicao = new MedicaoDTO();
		medicao.setSituacao(SituacaoMedicaoEnum.EM);
		medicao.setIsMedicaoAgrupadora(false);
		medicao.setId(2L);

		
		Context contexto = newContextBuilder()
				.setContext(null, medicao, null, null)
				.create();
		
		BigDecimal qtdPlanejadaServico = BigDecimal.valueOf(1.00);
		
		SubmetaMedicaoDTOBuilder submetaMedicaoBuilder = SubmetaMedicaoDTOBuilder.newSubmetaMedicaoBuilder_ComServico(qtdPlanejadaServico).permiteMarcacaoEmpresa(true);
		
		SubmetaMedicaoDTO submetaMedicao = submetaMedicaoBuilder.create();
		
		prepararServicoStep.process(submetaMedicao, contexto);
		
		assertTrue(submetaMedicao.getFrentesObra().get(0).getMacroServicosView().get(0).getServicos().get(0).isPermiteMedicao());
		assertEquals(qtdPlanejadaServico,submetaMedicao.getFrentesObra().get(0).getMacroServicosView().get(0).getServicos().get(0).getQtdMaxPermitido());
	}

	@MockUsuario(profile = EMPRESA)
	@Test
	void testServicoComQtdServicoPlanejado_ComMedicaoEmSituacaoNaoPermitidaEmpresa() {
		
		MedicaoDTO medicao = new MedicaoDTO();
		medicao.setSituacao(SituacaoMedicaoEnum.CC);
		medicao.setIsMedicaoAgrupadora(false);
		medicao.setId(2L);

		
		Context contexto = newContextBuilder()
				.setContext(null, medicao, null, null)
				.create();
		
		BigDecimal qtdPlanejadaServico = BigDecimal.valueOf(0.00);
		
		SubmetaMedicaoDTOBuilder submetaMedicaoBuilder = SubmetaMedicaoDTOBuilder.newSubmetaMedicaoBuilder_ComServico(qtdPlanejadaServico).permiteMarcacaoEmpresa(false);
		
		SubmetaMedicaoDTO submetaMedicao = submetaMedicaoBuilder.create();
		
		prepararServicoStep.process(submetaMedicao, contexto);
		
		assertFalse(submetaMedicao.getFrentesObra().get(0).getMacroServicosView().get(0).getServicos().get(0).isPermiteMedicao());
		assertEquals(qtdPlanejadaServico,submetaMedicao.getFrentesObra().get(0).getMacroServicosView().get(0).getServicos().get(0).getQtdMaxPermitido());
	}
	
	@MockUsuario(profile = EMPRESA)
	@Test
	void testServicoComMedicaoIniciadaPelaEmpresa() {
		
		MedicaoDTO medicao = new MedicaoDTO();
		medicao.setSituacao(SituacaoMedicaoEnum.EM);
		medicao.setIsMedicaoAgrupadora(false);
		medicao.setId(2L);
		
		Context contexto = newContextBuilder()
				.setContext(null, medicao, null, null)
				.create();
		
		BigDecimal qtdPlanejadaServico = BigDecimal.valueOf(15.32);
		BigDecimal saldoQtdMedir = BigDecimal.valueOf(7.32);
		
		
		SubmetaMedicaoDTOBuilder submetaMedicaoBuilder = SubmetaMedicaoDTOBuilder.newSubmetaMedicaoBuilder_ComServico(qtdPlanejadaServico).permiteMarcacaoEmpresa(true);
		
		submetaMedicaoBuilder.addQuantidadeInsuficienteParaServicoEmpresa ();
		
		SubmetaMedicaoDTO submetaMedicao = submetaMedicaoBuilder.create();
		
		prepararServicoStep.process(submetaMedicao, contexto);
		
		ServicoVrplDTO servico = submetaMedicao.getFrentesObra().get(0).getMacroServicosView().get(0).getServicos().get(0);
		
		assertTrue(submetaMedicao.getFrentesObra().get(0).getMacroServicosView().get(0).getServicos().get(0).isPermiteMedicao());
		assertEquals(qtdPlanejadaServico.subtract(servico.getQtdAcumuladoEmpresa().subtract(servico.getQtdRealizadoEmpresa())), saldoQtdMedir );
	}
	
	@MockUsuario(profile = EMPRESA)
	@Test
	void testServicoComMedicaoIniciadaPelaEmpresa_ComMedicaoEmSituacaoNaoPermitidaEmpresa() {
		
		MedicaoDTO medicao = new MedicaoDTO();
		medicao.setSituacao(SituacaoMedicaoEnum.ATD);
		medicao.setIsMedicaoAgrupadora(false);
		medicao.setId(2L);

		
		Context contexto = newContextBuilder()
				.setContext(null, medicao, null, null)
				.create();
		
		BigDecimal qtdPlanejadaServico = BigDecimal.valueOf(15.32);
		
		SubmetaMedicaoDTOBuilder submetaMedicaoBuilder = SubmetaMedicaoDTOBuilder.newSubmetaMedicaoBuilder_ComServico(qtdPlanejadaServico).permiteMarcacaoEmpresa(false);
		
		submetaMedicaoBuilder.addQuantidadeInsuficienteParaServicoEmpresa ();
		
		SubmetaMedicaoDTO submetaMedicao = submetaMedicaoBuilder.create();
		
		prepararServicoStep.process(submetaMedicao, contexto);
		
		ServicoVrplDTO servico = submetaMedicao.getFrentesObra().get(0).getMacroServicosView().get(0).getServicos().get(0);
		
		assertFalse(submetaMedicao.getFrentesObra().get(0).getMacroServicosView().get(0).getServicos().get(0).isPermiteMedicao());
		assertEquals(BigDecimal.valueOf(0.00), servico.getQtdMaxPermitido() );
	}

	@MockUsuario(profile = EMPRESA)
	@Test
	void testServicoTotalmenteMedidoPelaEmpresa() {
		
		MedicaoDTO medicao = new MedicaoDTO();
		medicao.setSituacao(SituacaoMedicaoEnum.EM);
		medicao.setIsMedicaoAgrupadora(false);
		medicao.setId(2L);

		
		Context contexto = newContextBuilder()
				.setContext(null, medicao, null, null)
				.create();
		
		BigDecimal qtdPlanejadaServico = BigDecimal.valueOf(15.32);
		
		SubmetaMedicaoDTOBuilder submetaMedicaoBuilder = SubmetaMedicaoDTOBuilder.newSubmetaMedicaoBuilder_ComServico(qtdPlanejadaServico).permiteMarcacaoEmpresa(true);
		
		submetaMedicaoBuilder.addQuantidadeTotalParaServicoEmpresa ();
		
		SubmetaMedicaoDTO submetaMedicao = submetaMedicaoBuilder.create();
		
		prepararServicoStep.process(submetaMedicao, contexto);
		
		ServicoVrplDTO servico = submetaMedicao.getFrentesObra().get(0).getMacroServicosView().get(0).getServicos().get(0);
		
		assertTrue(submetaMedicao.getFrentesObra().get(0).getMacroServicosView().get(0).getServicos().get(0).isPermiteMedicao());
		assertEquals(qtdPlanejadaServico.subtract(servico.getQtdAcumuladoEmpresa().subtract(servico.getQtdRealizadoEmpresa())), servico.getQtdMaxPermitido() );
	}
	
	@MockUsuario(profile = EMPRESA)
	@Test
	void testServicoTotalmenteMedidoPelaEmpresa_ComMedicaoEmSituacaoNaoPermitidaEmpresa() {
		
		MedicaoDTO medicao = new MedicaoDTO();
		medicao.setSituacao(SituacaoMedicaoEnum.AC);
		medicao.setIsMedicaoAgrupadora(false);
		medicao.setId(2L);

		
		Context contexto = newContextBuilder()
				.setContext(null, medicao, null, null)
				.create();
		
		BigDecimal qtdPlanejadaServico = BigDecimal.valueOf(15.32);
		
		SubmetaMedicaoDTOBuilder submetaMedicaoBuilder = SubmetaMedicaoDTOBuilder.newSubmetaMedicaoBuilder_ComServico(qtdPlanejadaServico).permiteMarcacaoEmpresa(false);
		
		submetaMedicaoBuilder.addQuantidadeTotalParaServicoEmpresa ();
		
		SubmetaMedicaoDTO submetaMedicao = submetaMedicaoBuilder.create();
		
		prepararServicoStep.process(submetaMedicao, contexto);
		
		ServicoVrplDTO servico = submetaMedicao.getFrentesObra().get(0).getMacroServicosView().get(0).getServicos().get(0);
		
		assertFalse(submetaMedicao.getFrentesObra().get(0).getMacroServicosView().get(0).getServicos().get(0).isPermiteMedicao());
		assertEquals(BigDecimal.valueOf(0.00), servico.getQtdMaxPermitido() );
	}
	
	@MockUsuario(profile = EMPRESA)
	@Test
	void testServicoTotalmenteMedidoPelaEmpresaMasNaoPelaMedicaoAtual() {
		
		MedicaoDTO medicao = new MedicaoDTO();
		medicao.setSituacao(SituacaoMedicaoEnum.EM);
		medicao.setIsMedicaoAgrupadora(false);
		medicao.setId(2L);

		
		Context contexto = newContextBuilder()
				.setContext(null, medicao, null, null)
				.create();
		
		BigDecimal qtdPlanejadaServico = BigDecimal.valueOf(15.32);
		
		SubmetaMedicaoDTOBuilder submetaMedicaoBuilder = SubmetaMedicaoDTOBuilder.newSubmetaMedicaoBuilder_ComServico(qtdPlanejadaServico).permiteMarcacaoEmpresa(true);
		
		submetaMedicaoBuilder.addQuantidadeTotalMasSemMedicaoParaServicoCorrenteEmpresa();
		
		SubmetaMedicaoDTO submetaMedicao = submetaMedicaoBuilder.create();
		
		prepararServicoStep.process(submetaMedicao, contexto);
		
		ServicoVrplDTO servico = submetaMedicao.getFrentesObra().get(0).getMacroServicosView().get(0).getServicos().get(0);
		
		assertFalse(submetaMedicao.getFrentesObra().get(0).getMacroServicosView().get(0).getServicos().get(0).isPermiteMedicao());
		assertEquals(BigDecimal.valueOf(0.00), servico.getQtdMaxPermitido() );
	}

    @MockUsuario(profile = EMPRESA)
    @Test
    void testMedicaoAcumuladaEmComplementacaoEmpresa_servicoPreenchidoOriginalmente() {

        MedicaoDTO medicao = new MedicaoDTO();
        medicao.setId(1L);
        medicao.setSituacao(SituacaoMedicaoEnum.CE);
        medicao.setIsMedicaoAgrupadora(false);
        medicao.setIdMedicaoAgrupadora(2L);

        Context contexto = newContextBuilder().setContext(null, medicao, null, null).create();

        final BigDecimal qtdPlanejada = valueOf(10);
        final BigDecimal qtdRealizadoEmpresaMedicaoAtual = valueOf(2.5);
        final BigDecimal qtdAcumuladoEmpresaAteMedicaoAtual = valueOf(2.5);
        final BigDecimal qtdRealizadaEmpresaMedicaoPosterior = valueOf(7);

        // qtdMaxPermitido = qtdPlanejadoServico - (qtdAcumuladoEmpresaTodasMedicoes - qtdRealizadoEmpresaMedicaoAtual)
        //                 = 10 - (9.5 - 2.5) = 3
        final BigDecimal qtdMaxPermitido = valueOf(3); 

        SubmetaMedicaoDTO submetaMedicao = SubmetaMedicaoDTOBuilder
                .newSubmetaMedicaoBuilder_ComServico(qtdPlanejada)
                .permiteMarcacaoEmpresa(true)
                .servicoQtdRealizadoEmpresa(qtdRealizadoEmpresaMedicaoAtual)
                .servicoQtdAcumuladoEmpresa(qtdAcumuladoEmpresaAteMedicaoAtual)
                .create();

        ServicoVrplDTO servico = submetaMedicao.getFrentesObra().get(0).getServicos().get(0);
        servico.getValoresPorIdMedicao().put(1L, new ValorServicoBM(qtdRealizadoEmpresaMedicaoAtual, null, null));
        servico.getValoresPorIdMedicao().put(2L, new ValorServicoBM(qtdRealizadaEmpresaMedicaoPosterior, null, null));

        prepararServicoStep.process(submetaMedicao, contexto);

        assertTrue(servico.isPermiteMedicao());
        assertThat(servico.getQtdMaxPermitido(), comparesEqualTo(qtdMaxPermitido));
    }

    @MockUsuario(profile = EMPRESA)
    @Test
    void testMedicaoAcumuladaEmComplementacaoEmpresa_servicoNaoPreenchidoOriginalmente() {

        MedicaoDTO medicao = new MedicaoDTO();
        medicao.setId(1L);
        medicao.setSituacao(SituacaoMedicaoEnum.CE);
        medicao.setIsMedicaoAgrupadora(false);
        medicao.setIdMedicaoAgrupadora(2L);

        Context contexto = newContextBuilder().setContext(null, medicao, null, null).create();

        final BigDecimal qtdPlanejada = valueOf(10);
        final BigDecimal qtdRealizadaEmpresaMedicaoPosterior = valueOf(7);

        final BigDecimal qtdMaxPermitido = BigDecimal.ZERO; 

        SubmetaMedicaoDTO submetaMedicao = SubmetaMedicaoDTOBuilder
                .newSubmetaMedicaoBuilder_ComServico(qtdPlanejada)
                .permiteMarcacaoEmpresa(true)
                .servicoQtdRealizadoEmpresa(null)
                .servicoQtdAcumuladoEmpresa(null)
                .create();

        ServicoVrplDTO servico = submetaMedicao.getFrentesObra().get(0).getServicos().get(0);
        servico.getValoresPorIdMedicao().put(1L, new ValorServicoBM(null, null, null));
        servico.getValoresPorIdMedicao().put(2L, new ValorServicoBM(qtdRealizadaEmpresaMedicaoPosterior, null, null));

        prepararServicoStep.process(submetaMedicao, contexto);

        assertFalse(servico.isPermiteMedicao());
        assertThat(servico.getQtdMaxPermitido(), comparesEqualTo(qtdMaxPermitido));
    }

    @MockUsuario(profile = EMPRESA)
    @Test
    void testServicoTotalmenteMedidoPelaEmpresaMasNaoPelaMedicaoAtual_ComMedicaoEmSituacaoNaoPermitidaEmpresa() {
        
        MedicaoDTO medicao = new MedicaoDTO();
        medicao.setSituacao(SituacaoMedicaoEnum.ECC);
        medicao.setIsMedicaoAgrupadora(false);
        medicao.setId(2L);

        
        Context contexto = newContextBuilder()
                .setContext(null, medicao, null, null)
                .create();
        
        BigDecimal qtdPlanejadaServico = BigDecimal.valueOf(15.32);
        
        SubmetaMedicaoDTOBuilder submetaMedicaoBuilder = SubmetaMedicaoDTOBuilder.newSubmetaMedicaoBuilder_ComServico(qtdPlanejadaServico).permiteMarcacaoEmpresa(false);
        
        submetaMedicaoBuilder.addQuantidadeTotalMasSemMedicaoParaServicoCorrenteEmpresa();
        
        SubmetaMedicaoDTO submetaMedicao = submetaMedicaoBuilder.create();
        
        prepararServicoStep.process(submetaMedicao, contexto);
        
        ServicoVrplDTO servico = submetaMedicao.getFrentesObra().get(0).getMacroServicosView().get(0).getServicos().get(0);
        
        assertFalse(submetaMedicao.getFrentesObra().get(0).getMacroServicosView().get(0).getServicos().get(0).isPermiteMedicao());
        assertEquals(BigDecimal.valueOf(0.00), servico.getQtdMaxPermitido() );
    }

	@MockUsuario(profile = Profile.GUEST)
	@Test
	void testServicoComPerfilConsulta() {
		
		MedicaoDTO medicao = new MedicaoDTO();
		medicao.setIsMedicaoAgrupadora(false);
		
		Context contexto = newContextBuilder()
				.setContext(null, medicao, null, null)
				.create();
		
		BigDecimal qtdPlanejadaServico = BigDecimal.valueOf(15.32);
		
		SubmetaMedicaoDTOBuilder submetaMedicaoBuilder = SubmetaMedicaoDTOBuilder.newSubmetaMedicaoBuilder_ComServico(qtdPlanejadaServico).permiteMarcacaoEmpresa(true);
		
		submetaMedicaoBuilder.addQuantidadeTotalMasSemMedicaoParaServicoCorrenteEmpresa();
		
		SubmetaMedicaoDTO submetaMedicao = submetaMedicaoBuilder.create();
		
		prepararServicoStep.process(submetaMedicao, contexto);
		
		ServicoVrplDTO servico = submetaMedicao.getFrentesObra().get(0).getMacroServicosView().get(0).getServicos().get(0);
		
		assertFalse(submetaMedicao.getFrentesObra().get(0).getMacroServicosView().get(0).getServicos().get(0).isPermiteMedicao());
		assertEquals(BigDecimal.valueOf(0.00), servico.getQtdMaxPermitido() );
	}
	
	@MockUsuario(profile = PROPONENTE_CONVENENTE)
	@Test
	void testServicoNaoMedidoPeloConvenente() {
		
		MedicaoDTO medicao = new MedicaoDTO();
		medicao.setSituacao(SituacaoMedicaoEnum.AT);
		medicao.setIsMedicaoAgrupadora(Boolean.FALSE);

		Context contexto = newContextBuilder()
				.setContext(null, medicao, null, null)
				.create();
		
		BigDecimal qtdPlanejadaServico = BigDecimal.valueOf(15.32);
		
		SubmetaMedicaoDTOBuilder submetaMedicaoBuilder = SubmetaMedicaoDTOBuilder.newSubmetaMedicaoBuilder_ComServico(qtdPlanejadaServico).permiteMarcacaoConvenente(true);
		submetaMedicaoBuilder.addQuantidadeInsuficienteParaServicoEmpresa();
		
		SubmetaMedicaoDTO submetaMedicao = submetaMedicaoBuilder.create();
		
		prepararServicoStep.process(submetaMedicao, contexto);
		
		ServicoVrplDTO servico = submetaMedicao.getFrentesObra().get(0).getMacroServicosView().get(0).getServicos().get(0);
		
		assertTrue(servico.isPermiteMedicao());
		assertEquals(servico.getQtdAcumuladoEmpresa(), servico.getQtdMaxPermitido());
	}
	
	@MockUsuario(profile = PROPONENTE_CONVENENTE)
    @Test
    void testMedicaoAcumuladaEmComplementacaoConvenente_servicoNaoPreenchidoOriginalmente() {

        MedicaoDTO medicao = new MedicaoDTO();
        medicao.setId(1L);
        medicao.setSituacao(CC);
        medicao.setIsMedicaoAgrupadora(false);
        medicao.setIdMedicaoAgrupadora(2L);

        Context contexto = newContextBuilder().setContext(null, medicao, null, null).create();

        final BigDecimal qtdPlanejada = valueOf(10);
        final BigDecimal qtdRealizadaEmpresa = valueOf(2);
        final BigDecimal qtdAcumuladaEmpresa = valueOf(2);
        final BigDecimal qtdRealizadaEmpresaMedicao2 = valueOf(7);
        final BigDecimal qtdRealizadaConvenenteMedicao2 = valueOf(9);

        final BigDecimal qtdMaxPermitido = ZERO; 

        SubmetaMedicaoDTO submetaMedicao = SubmetaMedicaoDTOBuilder
                .newSubmetaMedicaoBuilder_ComServico(qtdPlanejada)
                .permiteMarcacaoConvenente(true)
                .servicoQtdRealizadoEmpresa(qtdRealizadaEmpresa)
                .servicoQtdAcumuladoEmpresa(qtdAcumuladaEmpresa)
                .create();

        ServicoVrplDTO servico = submetaMedicao.getFrentesObra().get(0).getServicos().get(0);
        servico.getValoresPorIdMedicao().put(1L, new ValorServicoBM(qtdRealizadaEmpresa, null, null));
        servico.getValoresPorIdMedicao().put(2L, new ValorServicoBM(qtdRealizadaEmpresaMedicao2, qtdRealizadaConvenenteMedicao2, null));

        prepararServicoStep.process(submetaMedicao, contexto);

        assertFalse(servico.isPermiteMedicao());
        assertThat(servico.getQtdMaxPermitido(), comparesEqualTo(qtdMaxPermitido));
    }
	
	@MockUsuario(profile = PROPONENTE_CONVENENTE)
	@Test
	void testServicoNaoMedidoPeloConvenente_ComMedicaoEmSituacaoNaoPermitidaConvenente() {
		
		MedicaoDTO medicao = new MedicaoDTO();
		medicao.setSituacao(SituacaoMedicaoEnum.AC);
		medicao.setIsMedicaoAgrupadora(Boolean.FALSE);
		
		Context contexto = newContextBuilder()
				.setContext(null, medicao, null, null)
				.create();
		
		BigDecimal qtdPlanejadaServico = BigDecimal.valueOf(15.32);
		
		SubmetaMedicaoDTOBuilder submetaMedicaoBuilder = SubmetaMedicaoDTOBuilder.newSubmetaMedicaoBuilder_ComServico(qtdPlanejadaServico).permiteMarcacaoConvenente(false);
		submetaMedicaoBuilder.addQuantidadeInsuficienteParaServicoEmpresa();
		
		SubmetaMedicaoDTO submetaMedicao = submetaMedicaoBuilder.create();
		
		prepararServicoStep.process(submetaMedicao, contexto);
		
		ServicoVrplDTO servico = submetaMedicao.getFrentesObra().get(0).getMacroServicosView().get(0).getServicos().get(0);
		
		assertFalse(servico.isPermiteMedicao());
		assertEquals(BigDecimal.valueOf(0.00),servico.getQtdMaxPermitido());
	}
	
	@MockUsuario(profile = PROPONENTE_CONVENENTE)
	@Test
	void testServicoComMedicaoIniciadaPeloConvenente() {
		
		MedicaoDTO medicao = new MedicaoDTO();
		medicao.setSituacao(SituacaoMedicaoEnum.AT);
		medicao.setIsMedicaoAgrupadora(Boolean.FALSE);
		
		Context contexto = newContextBuilder()
				.setContext(null, medicao, null, null)
				.create();
		
		BigDecimal qtdPlanejadaServico = BigDecimal.valueOf(15.32);
		BigDecimal saldoQtdMedir = BigDecimal.valueOf(5.00);
		
		SubmetaMedicaoDTOBuilder submetaMedicaoBuilder = SubmetaMedicaoDTOBuilder.newSubmetaMedicaoBuilder_ComServico(qtdPlanejadaServico).permiteMarcacaoConvenente(true);
		submetaMedicaoBuilder.addQuantidadeInsuficienteParaServicoEmpresa();
		submetaMedicaoBuilder.addQuantidadeInsuficienteParaServicoConvenente();
		
		SubmetaMedicaoDTO submetaMedicao = submetaMedicaoBuilder.create();
		
		prepararServicoStep.process(submetaMedicao, contexto);
		
		ServicoVrplDTO servico = submetaMedicao.getFrentesObra().get(0).getMacroServicosView().get(0).getServicos().get(0);
		
		assertTrue(servico.isPermiteMedicao());
		assertEquals(servico.getQtdAcumuladoEmpresa().subtract(servico.getQtdAcumuladoConvenente().subtract(servico.getQtdRealizadoConvenente())), saldoQtdMedir);
	}
	
    @MockUsuario(profile = PROPONENTE_CONVENENTE)
    @Test
    void testMedicaoAcumuladaEmComplementacaoConvenente_servicoPreenchidoOriginalmente() {

        MedicaoDTO medicao = new MedicaoDTO();
        medicao.setId(1L);
        medicao.setSituacao(SituacaoMedicaoEnum.CC);
        medicao.setIsMedicaoAgrupadora(false);
        medicao.setIdMedicaoAgrupadora(2L);

        Context contexto = newContextBuilder().setContext(null, medicao, null, null).create();

        final BigDecimal qtdPlanejada = valueOf(10);
        
        final BigDecimal qtdRealizadoEmpresaMedicao1 = valueOf(2.5);
        final BigDecimal qtdAcumuladoEmpresaMedicao1 = valueOf(2.5);
        final BigDecimal qtdRealizadoEmpresaMedicao2 = valueOf(2.5);
        
        final BigDecimal qtdRealizadoConvenenteMedicao1 = valueOf(2.5);
        final BigDecimal qtdRealizadoConvenenteMedicao2 = valueOf(0);
        final BigDecimal qtdAcumuladoConvenenteAteMedicao2 = valueOf(2.5);

        // qtdMaxPermitido = qtdAcumuladaEmpresa - (qtdAcumuladoConvenenteTodasMedicoes - qtdRealizadaConvenente)
        //                 = 2.5 - (2.5 - 2.5) = 2.5
        final BigDecimal qtdMaxPermitido = valueOf(2.5); 

        SubmetaMedicaoDTO submetaMedicao = SubmetaMedicaoDTOBuilder
                .newSubmetaMedicaoBuilder_ComServico(qtdPlanejada)
                .permiteMarcacaoConvenente(true)
                .servicoQtdAcumuladoEmpresa(qtdAcumuladoEmpresaMedicao1)
                .servicoQtdRealizadoConvenente(qtdRealizadoConvenenteMedicao1)
                .servicoQtdAcumuladoConvenente(qtdAcumuladoConvenenteAteMedicao2)
                .create();

        ServicoVrplDTO servico = submetaMedicao.getFrentesObra().get(0).getServicos().get(0);
        servico.getValoresPorIdMedicao().put(1L, new ValorServicoBM(qtdRealizadoEmpresaMedicao1, qtdRealizadoConvenenteMedicao1, null));
        servico.getValoresPorIdMedicao().put(2L, new ValorServicoBM(qtdRealizadoEmpresaMedicao2, qtdRealizadoConvenenteMedicao2, null));

        prepararServicoStep.process(submetaMedicao, contexto);

        assertTrue(servico.isPermiteMedicao());
        assertThat(servico.getQtdMaxPermitido(), comparesEqualTo(qtdMaxPermitido));
    }
    
    @MockUsuario(profile = PROPONENTE_CONVENENTE)
    @Test
    void testMedicaoAcumuladaEmComplementacaoConvenente_servicoPreenchidoOriginalmenteEComGlosaEmMedicaoAnterior() {

        MedicaoDTO medicao = new MedicaoDTO();
        medicao.setId(2L);
        medicao.setSituacao(SituacaoMedicaoEnum.CC);
        medicao.setIsMedicaoAgrupadora(false);
        medicao.setIdMedicaoAgrupadora(3L);

        Context contexto = newContextBuilder().setContext(null, medicao, null, null).create();

        final BigDecimal qtdPlanejada = valueOf(10);
        
        final BigDecimal qtdRealizadoEmpresaMedicao1 = valueOf(2.5);
        final BigDecimal qtdRealizadoEmpresaMedicao2 = valueOf(2.5);
        final BigDecimal qtdAcumuladoEmpresaMedicao2 = valueOf(5.0);
        final BigDecimal qtdRealizadoEmpresaMedicao3 = valueOf(2.5);
        
        final BigDecimal qtdRealizadoConvenenteMedicao1 = valueOf(1.5);
        final BigDecimal qtdRealizadoConvenenteMedicao2 = valueOf(2.5);
        final BigDecimal qtdRealizadoConvenenteMedicao3 = valueOf(0);
        final BigDecimal qtdAcumuladoConvenenteAteMedicao3 = valueOf(4.0);

        final BigDecimal qtdMaxPermitido = valueOf(3.5); 
        
        SubmetaMedicaoDTO submetaMedicao = SubmetaMedicaoDTOBuilder
                .newSubmetaMedicaoBuilder_ComServico(qtdPlanejada)
                .permiteMarcacaoConvenente(true)
                .servicoQtdAcumuladoEmpresa(qtdAcumuladoEmpresaMedicao2)
                .servicoQtdRealizadoConvenente(qtdRealizadoConvenenteMedicao2)
                .servicoQtdAcumuladoConvenente(qtdAcumuladoConvenenteAteMedicao3)
                .create();

        ServicoVrplDTO servico = submetaMedicao.getFrentesObra().get(0).getServicos().get(0);
        servico.getValoresPorIdMedicao().put(1L, new ValorServicoBM(qtdRealizadoEmpresaMedicao1, qtdRealizadoConvenenteMedicao1, null));
        servico.getValoresPorIdMedicao().put(2L, new ValorServicoBM(qtdRealizadoEmpresaMedicao2, qtdRealizadoConvenenteMedicao2, null));
        servico.getValoresPorIdMedicao().put(3L, new ValorServicoBM(qtdRealizadoEmpresaMedicao3, qtdRealizadoConvenenteMedicao3, null));

        prepararServicoStep.process(submetaMedicao, contexto);

        assertTrue(servico.isPermiteMedicao());
        assertTrue(servico.isPossuiGlosasAnterioresConvenente());
        assertThat(servico.getQtdMaxPermitido(), comparesEqualTo(qtdMaxPermitido));
    }
    
    @MockUsuario(profile = PROPONENTE_CONVENENTE)
    @Test
    void testMedicaoAcumuladaEmComplementacaoReencaminhadoConvenente_servicoPreenchidoOriginalmente() {

		MedicaoDTO medicao = new MedicaoDTO();
        medicao.setId(1L);
        medicao.setSituacao(SituacaoMedicaoEnum.AT);
        medicao.setPermiteComplementacaoValor(true);
        medicao.setIsMedicaoAgrupadora(false);
        medicao.setIdMedicaoAgrupadora(2L);

        Context contexto = newContextBuilder().setContext(null, medicao, null, null).create();

        final BigDecimal qtdPlanejada = valueOf(10);
        
        final BigDecimal qtdRealizadoEmpresaMedicao1 = valueOf(2.5);
        final BigDecimal qtdAcumuladoEmpresaMedicao1 = valueOf(2.5);
        final BigDecimal qtdRealizadoEmpresaMedicao2 = valueOf(2.5);
        
        final BigDecimal qtdRealizadoConvenenteMedicao1 = valueOf(2.5);
        final BigDecimal qtdRealizadoConvenenteMedicao2 = valueOf(0);
        final BigDecimal qtdAcumuladoConvenenteAteMedicao2 = valueOf(2.5);

        // qtdMaxPermitido = qtdAcumuladaEmpresa - (qtdAcumuladoConvenenteTodasMedicoes - qtdRealizadaConvenente)
        //                 = 2.5 - (2.5 - 2.5) = 2.5
        final BigDecimal qtdMaxPermitido = valueOf(2.5); 

        SubmetaMedicaoDTO submetaMedicao = SubmetaMedicaoDTOBuilder
                .newSubmetaMedicaoBuilder_ComServico(qtdPlanejada)
                .permiteMarcacaoConvenente(true)
                .servicoQtdAcumuladoEmpresa(qtdAcumuladoEmpresaMedicao1)
                .servicoQtdRealizadoConvenente(qtdRealizadoConvenenteMedicao1)
                .servicoQtdAcumuladoConvenente(qtdAcumuladoConvenenteAteMedicao2)
                .create();

        ServicoVrplDTO servico = submetaMedicao.getFrentesObra().get(0).getServicos().get(0);
        servico.getValoresPorIdMedicao().put(1L, new ValorServicoBM(qtdRealizadoEmpresaMedicao1, qtdRealizadoConvenenteMedicao1, null));
        servico.getValoresPorIdMedicao().put(2L, new ValorServicoBM(qtdRealizadoEmpresaMedicao2, qtdRealizadoConvenenteMedicao2, null));

        prepararServicoStep.process(submetaMedicao, contexto);

        assertTrue(servico.isPermiteMedicao());
        assertThat(servico.getQtdMaxPermitido(), comparesEqualTo(qtdMaxPermitido));
    }
	
	@MockUsuario(profile = PROPONENTE_CONVENENTE)
	@Test
	void testServicoComMedicaoIniciadaPeloConvenente_ComMedicaoEmSituacaoNaoPermitidaConvenente() {
		
		MedicaoDTO medicao = new MedicaoDTO();
		medicao.setSituacao(SituacaoMedicaoEnum.ATD);
		medicao.setIsMedicaoAgrupadora(Boolean.FALSE);
		
		Context contexto = newContextBuilder()
				.setContext(null, medicao, null, null)
				.create();
		
		BigDecimal qtdPlanejadaServico = BigDecimal.valueOf(15.32);
		
		SubmetaMedicaoDTOBuilder submetaMedicaoBuilder = SubmetaMedicaoDTOBuilder.newSubmetaMedicaoBuilder_ComServico(qtdPlanejadaServico).permiteMarcacaoConvenente(false);
		submetaMedicaoBuilder.addQuantidadeInsuficienteParaServicoEmpresa();
		submetaMedicaoBuilder.addQuantidadeInsuficienteParaServicoConvenente();
		
		SubmetaMedicaoDTO submetaMedicao = submetaMedicaoBuilder.create();
		
		prepararServicoStep.process(submetaMedicao, contexto);
		
		ServicoVrplDTO servico = submetaMedicao.getFrentesObra().get(0).getMacroServicosView().get(0).getServicos().get(0);
		
		assertFalse(servico.isPermiteMedicao());
		assertEquals(BigDecimal.valueOf(0.00), servico.getQtdMaxPermitido());
	}

	@MockUsuario(profile = PROPONENTE_CONVENENTE)
	@Test
	void testServicoTotalmenteMedidoPeloConvenente() {
		MedicaoDTO medicao = new MedicaoDTO();
		medicao.setSituacao(SituacaoMedicaoEnum.AT);
		medicao.setIsMedicaoAgrupadora(Boolean.FALSE);
		
		Context contexto = newContextBuilder()
				.setContext(null, medicao, null, null)
				.create();
		
		BigDecimal qtdPlanejadaServico = BigDecimal.valueOf(15.32);
		
		SubmetaMedicaoDTOBuilder submetaMedicaoBuilder = SubmetaMedicaoDTOBuilder.newSubmetaMedicaoBuilder_ComServico(qtdPlanejadaServico).permiteMarcacaoConvenente(true);
		submetaMedicaoBuilder.addQuantidadeTotalParaServicoEmpresa();
		submetaMedicaoBuilder.addQuantidadeTotalParaServicoConvenente();
		
		SubmetaMedicaoDTO submetaMedicao = submetaMedicaoBuilder.create();
		
		prepararServicoStep.process(submetaMedicao, contexto);
		
		ServicoVrplDTO servico = submetaMedicao.getFrentesObra().get(0).getMacroServicosView().get(0).getServicos().get(0);
		
		assertTrue(servico.isPermiteMedicao());
		assertEquals(servico.getQtdAcumuladoEmpresa().subtract(servico.getQtdAcumuladoConvenente().subtract(servico.getQtdRealizadoConvenente())), servico.getQtdMaxPermitido());
	}
	
	@MockUsuario(profile = PROPONENTE_CONVENENTE)
	@Test
	void testServicoTotalmenteMedidoPeloConvenente_ComMedicaoEmSituacaoNaoPermitidaConvenente() {
		MedicaoDTO medicao = new MedicaoDTO();
		medicao.setSituacao(SituacaoMedicaoEnum.AC);
		medicao.setIsMedicaoAgrupadora(Boolean.FALSE);
		
		Context contexto = newContextBuilder()
				.setContext(null, medicao, null, null)
				.create();
		
		BigDecimal qtdPlanejadaServico = BigDecimal.valueOf(15.32);
		
		SubmetaMedicaoDTOBuilder submetaMedicaoBuilder = SubmetaMedicaoDTOBuilder.newSubmetaMedicaoBuilder_ComServico(qtdPlanejadaServico).permiteMarcacaoConvenente(false);
		submetaMedicaoBuilder.addQuantidadeTotalParaServicoEmpresa();
		submetaMedicaoBuilder.addQuantidadeTotalParaServicoConvenente();
		
		SubmetaMedicaoDTO submetaMedicao = submetaMedicaoBuilder.create();
		
		prepararServicoStep.process(submetaMedicao, contexto);
		
		ServicoVrplDTO servico = submetaMedicao.getFrentesObra().get(0).getMacroServicosView().get(0).getServicos().get(0);
		
		assertFalse(servico.isPermiteMedicao());
		assertEquals(BigDecimal.valueOf(0.00), servico.getQtdMaxPermitido());
	}
	
	@MockUsuario(profile = PROPONENTE_CONVENENTE)
	@Test
	void testServicoTotalmenteMedidoPeloConvenenteMasNaoPelaMedicaoAtual() {
		MedicaoDTO medicao = new MedicaoDTO();
		medicao.setSituacao(SituacaoMedicaoEnum.AT);
		medicao.setIsMedicaoAgrupadora(Boolean.FALSE);
		
		Context contexto = newContextBuilder()
				.setContext(null, medicao, null, null)
				.create();
		
		BigDecimal qtdPlanejadaServico = BigDecimal.valueOf(15.32);
		
		SubmetaMedicaoDTOBuilder submetaMedicaoBuilder = SubmetaMedicaoDTOBuilder.newSubmetaMedicaoBuilder_ComServico(qtdPlanejadaServico).permiteMarcacaoConvenente(true);
		submetaMedicaoBuilder.addQuantidadeTotalMasSemMedicaoParaServicoCorrenteEmpresa();
		submetaMedicaoBuilder.addQuantidadeTotalMasSemMedicaoParaServicoCorrenteConvenente();
		
		SubmetaMedicaoDTO submetaMedicao = submetaMedicaoBuilder.create();
		
		prepararServicoStep.process(submetaMedicao, contexto);
		
		ServicoVrplDTO servico = submetaMedicao.getFrentesObra().get(0).getMacroServicosView().get(0).getServicos().get(0);
		
		assertFalse(servico.isPermiteMedicao());
		assertEquals(BigDecimal.valueOf(0.00), servico.getQtdMaxPermitido());
	}
	
	@MockUsuario(profile = PROPONENTE_CONVENENTE)
	@Test
	void testServicoTotalmenteMedidoPeloConvenenteMasNaoPelaMedicaoAtual_ComMedicaoEmSituacaoNaoPermitidaConvenente() {
		MedicaoDTO medicao = new MedicaoDTO();
		medicao.setSituacao(SituacaoMedicaoEnum.AC);
		medicao.setIsMedicaoAgrupadora(Boolean.FALSE);
		
		Context contexto = newContextBuilder()
				.setContext(null, medicao, null, null)
				.create();
		
		BigDecimal qtdPlanejadaServico = BigDecimal.valueOf(15.32);
		
		SubmetaMedicaoDTOBuilder submetaMedicaoBuilder = SubmetaMedicaoDTOBuilder.newSubmetaMedicaoBuilder_ComServico(qtdPlanejadaServico).permiteMarcacaoConvenente(false);
		submetaMedicaoBuilder.addQuantidadeTotalMasSemMedicaoParaServicoCorrenteEmpresa();
		submetaMedicaoBuilder.addQuantidadeTotalMasSemMedicaoParaServicoCorrenteConvenente();
		
		SubmetaMedicaoDTO submetaMedicao = submetaMedicaoBuilder.create();
		
		prepararServicoStep.process(submetaMedicao, contexto);
		
		ServicoVrplDTO servico = submetaMedicao.getFrentesObra().get(0).getMacroServicosView().get(0).getServicos().get(0);
		
		assertFalse(servico.isPermiteMedicao());
		assertEquals(BigDecimal.valueOf(0.00), servico.getQtdMaxPermitido());
	}
	
	@MockUsuario(profile = CONCEDENTE)
	@Test
	void testServicoNaoMedidoPeloConcedente() {
		
		MedicaoDTO medicao = new MedicaoDTO();
		medicao.setSituacao(SituacaoMedicaoEnum.AC);
		medicao.setIsMedicaoAgrupadora(Boolean.FALSE);

		Context contexto = newContextBuilder()
				.setContext(null, medicao, null, null)
				.create();
		
		BigDecimal qtdPlanejadaServico = BigDecimal.valueOf(15.32);
		
		SubmetaMedicaoDTOBuilder submetaMedicaoBuilder = SubmetaMedicaoDTOBuilder.newSubmetaMedicaoBuilder_ComServico(qtdPlanejadaServico);
		submetaMedicaoBuilder.addQuantidadeInsuficienteParaServicoEmpresa();
		submetaMedicaoBuilder.addQuantidadeInsuficienteParaServicoConvenente();
		submetaMedicaoBuilder.permiteMarcacaoConcedente(Boolean.TRUE);
		
		SubmetaMedicaoDTO submetaMedicao = submetaMedicaoBuilder.create();
		
		prepararServicoStep.process(submetaMedicao, contexto);
		
		ServicoVrplDTO servico = submetaMedicao.getFrentesObra().get(0).getMacroServicosView().get(0).getServicos().get(0);
		
		assertTrue(servico.isPermiteMedicao());
		assertEquals(servico.getQtdAcumuladoConvenente(), servico.getQtdMaxPermitido());
	}
	
	@MockUsuario(profile = CONCEDENTE)
	@Test
	void testServicoNaoMedidoPeloConcedente_ComMedicaoEmSituacaoNaoPermitidaConcedente() {
		
		MedicaoDTO medicao = new MedicaoDTO();
		medicao.setSituacao(SituacaoMedicaoEnum.ACT);
		medicao.setIsMedicaoAgrupadora(Boolean.FALSE);
		
		Context contexto = newContextBuilder()
				.setContext(null, medicao, null, null)
				.create();
		
		BigDecimal qtdPlanejadaServico = BigDecimal.valueOf(15.32);
		
		SubmetaMedicaoDTOBuilder submetaMedicaoBuilder = SubmetaMedicaoDTOBuilder.newSubmetaMedicaoBuilder_ComServico(qtdPlanejadaServico);
		submetaMedicaoBuilder.addQuantidadeInsuficienteParaServicoEmpresa();
		submetaMedicaoBuilder.addQuantidadeInsuficienteParaServicoConvenente();
		
		SubmetaMedicaoDTO submetaMedicao = submetaMedicaoBuilder.create();
		
		prepararServicoStep.process(submetaMedicao, contexto);
		
		ServicoVrplDTO servico = submetaMedicao.getFrentesObra().get(0).getMacroServicosView().get(0).getServicos().get(0);
		
		assertFalse(servico.isPermiteMedicao());
		assertEquals(BigDecimal.valueOf(0.00),servico.getQtdMaxPermitido());
	}
	
	@MockUsuario(profile = CONCEDENTE)
	@Test
	void testServicoComMedicaoIniciadaPeloConcedente() {
		
		MedicaoDTO medicao = new MedicaoDTO();
		medicao.setSituacao(SituacaoMedicaoEnum.AC);
		medicao.setIsMedicaoAgrupadora(Boolean.FALSE);
		
		Context contexto = newContextBuilder()
				.setContext(null, medicao, null, null)
				.create();
		
		BigDecimal qtdPlanejadaServico = BigDecimal.valueOf(15.32);
		BigDecimal saldoQtdMedir = BigDecimal.valueOf(3.00);
		
		SubmetaMedicaoDTOBuilder submetaMedicaoBuilder = SubmetaMedicaoDTOBuilder.newSubmetaMedicaoBuilder_ComServico(qtdPlanejadaServico);
		submetaMedicaoBuilder.addQuantidadeInsuficienteParaServicoEmpresa();
		submetaMedicaoBuilder.addQuantidadeInsuficienteParaServicoConvenente();
		submetaMedicaoBuilder.addQuantidadeInsuficienteParaServicoConcedente();
		submetaMedicaoBuilder.permiteMarcacaoConcedente(Boolean.TRUE);
		
		SubmetaMedicaoDTO submetaMedicao = submetaMedicaoBuilder.create();
		
		prepararServicoStep.process(submetaMedicao, contexto);
		
		ServicoVrplDTO servico = submetaMedicao.getFrentesObra().get(0).getMacroServicosView().get(0).getServicos().get(0);
		
		assertTrue(servico.isPermiteMedicao());
		assertEquals(servico.getQtdAcumuladoConvenente().subtract(servico.getQtdAcumuladoConcedente().subtract(servico.getQtdRealizadoConcedente())), saldoQtdMedir);
	}
	
	@MockUsuario(profile = CONCEDENTE)
	@Test
	void testServicoComMedicaoIniciadaPeloConcedente_ComMedicaoEmSituacaoNaoPermitidaConcedente() {
		
		MedicaoDTO medicao = new MedicaoDTO();
		medicao.setSituacao(SituacaoMedicaoEnum.ACT);
		medicao.setIsMedicaoAgrupadora(Boolean.FALSE);
		
		Context contexto = newContextBuilder()
				.setContext(null, medicao, null, null)
				.create();
		
		BigDecimal qtdPlanejadaServico = BigDecimal.valueOf(15.32);
		
		SubmetaMedicaoDTOBuilder submetaMedicaoBuilder = SubmetaMedicaoDTOBuilder.newSubmetaMedicaoBuilder_ComServico(qtdPlanejadaServico);
		submetaMedicaoBuilder.addQuantidadeInsuficienteParaServicoEmpresa();
		submetaMedicaoBuilder.addQuantidadeInsuficienteParaServicoConvenente();
		submetaMedicaoBuilder.addQuantidadeInsuficienteParaServicoConcedente();
		
		SubmetaMedicaoDTO submetaMedicao = submetaMedicaoBuilder.create();
		
		prepararServicoStep.process(submetaMedicao, contexto);
		
		ServicoVrplDTO servico = submetaMedicao.getFrentesObra().get(0).getMacroServicosView().get(0).getServicos().get(0);
		
		assertFalse(servico.isPermiteMedicao());
		assertEquals(BigDecimal.valueOf(0.00), servico.getQtdMaxPermitido());
	}
	
	@MockUsuario(profile = CONCEDENTE)
	@Test
	void testServicoTotalmenteMedidoPeloConcedente() {
		MedicaoDTO medicao = new MedicaoDTO();
		medicao.setSituacao(SituacaoMedicaoEnum.AC);
		medicao.setIsMedicaoAgrupadora(Boolean.FALSE);
		
		Context contexto = newContextBuilder()
				.setContext(null, medicao, null, null)
				.create();
		
		BigDecimal qtdPlanejadaServico = BigDecimal.valueOf(15.32);
		
		SubmetaMedicaoDTOBuilder submetaMedicaoBuilder = SubmetaMedicaoDTOBuilder.newSubmetaMedicaoBuilder_ComServico(qtdPlanejadaServico);
		submetaMedicaoBuilder.addQuantidadeTotalParaServicoEmpresa();
		submetaMedicaoBuilder.addQuantidadeTotalParaServicoConvenente();
		submetaMedicaoBuilder.addQuantidadeTotalParaServicoConcedente();
		submetaMedicaoBuilder.permiteMarcacaoConcedente(Boolean.TRUE);
		
		SubmetaMedicaoDTO submetaMedicao = submetaMedicaoBuilder.create();
		
		prepararServicoStep.process(submetaMedicao, contexto);
		
		ServicoVrplDTO servico = submetaMedicao.getFrentesObra().get(0).getMacroServicosView().get(0).getServicos().get(0);
		
		assertTrue(servico.isPermiteMedicao());
		assertEquals(servico.getQtdAcumuladoEmpresa().subtract(servico.getQtdAcumuladoConcedente().subtract(servico.getQtdRealizadoConcedente())), servico.getQtdMaxPermitido());
	}
	
	@MockUsuario(profile = CONCEDENTE)
	@Test
	void testServicoTotalmenteMedidoPeloConcedente_ComMedicaoEmSituacaoNaoPermitidaConcedente() {
		MedicaoDTO medicao = new MedicaoDTO();
		medicao.setSituacao(SituacaoMedicaoEnum.ACT);
		medicao.setIsMedicaoAgrupadora(Boolean.FALSE);
		
		Context contexto = newContextBuilder()
				.setContext(null, medicao, null, null)
				.create();
		
		BigDecimal qtdPlanejadaServico = BigDecimal.valueOf(15.32);
		
		SubmetaMedicaoDTOBuilder submetaMedicaoBuilder = SubmetaMedicaoDTOBuilder.newSubmetaMedicaoBuilder_ComServico(qtdPlanejadaServico);
		submetaMedicaoBuilder.addQuantidadeTotalParaServicoEmpresa();
		submetaMedicaoBuilder.addQuantidadeTotalParaServicoConvenente();
		submetaMedicaoBuilder.addQuantidadeTotalParaServicoConcedente();
		
		SubmetaMedicaoDTO submetaMedicao = submetaMedicaoBuilder.create();
		
		prepararServicoStep.process(submetaMedicao, contexto);
		
		ServicoVrplDTO servico = submetaMedicao.getFrentesObra().get(0).getMacroServicosView().get(0).getServicos().get(0);
		
		assertFalse(servico.isPermiteMedicao());
		assertEquals(BigDecimal.valueOf(0.00), servico.getQtdMaxPermitido());
	}

	@MockUsuario(profile = CONCEDENTE)
	@Test
	void testServicoTotalmenteMedidoPeloConcedenteMasNaoPelaMedicaoAtual() {
		MedicaoDTO medicao = new MedicaoDTO();
		medicao.setSituacao(SituacaoMedicaoEnum.AT);
		medicao.setIsMedicaoAgrupadora(Boolean.FALSE);
		
		Context contexto = newContextBuilder()
				.setContext(null, medicao, null, null)
				.create();
		
		BigDecimal qtdPlanejadaServico = BigDecimal.valueOf(15.32);
		
		SubmetaMedicaoDTOBuilder submetaMedicaoBuilder = SubmetaMedicaoDTOBuilder.newSubmetaMedicaoBuilder_ComServico(qtdPlanejadaServico);
		submetaMedicaoBuilder.addQuantidadeTotalMasSemMedicaoParaServicoCorrenteEmpresa();
		submetaMedicaoBuilder.addQuantidadeTotalMasSemMedicaoParaServicoCorrenteConvenente();
		submetaMedicaoBuilder.addQuantidadeTotalMasSemMedicaoParaServicoCorrenteConcedente();
		
		SubmetaMedicaoDTO submetaMedicao = submetaMedicaoBuilder.create();
		
		prepararServicoStep.process(submetaMedicao, contexto);
		
		ServicoVrplDTO servico = submetaMedicao.getFrentesObra().get(0).getMacroServicosView().get(0).getServicos().get(0);
		
		assertFalse(servico.isPermiteMedicao());
		assertEquals(BigDecimal.valueOf(0.00), servico.getQtdMaxPermitido());
	}
	
	@MockUsuario(profile = Profile.CONCEDENTE)
	@Test
	void testServicoTotalmenteMedidoPeloConcedenteMasNaoPelaMedicaoAtual_ComMedicaoEmSituacaoNaoPermitidaConcedente() {
		MedicaoDTO medicao = new MedicaoDTO();
		medicao.setSituacao(SituacaoMedicaoEnum.ACT);
		medicao.setIsMedicaoAgrupadora(Boolean.FALSE);
		
		Context contexto = newContextBuilder()
				.setContext(null, medicao, null, null)
				.create();
		
		BigDecimal qtdPlanejadaServico = BigDecimal.valueOf(15.32);
		
		SubmetaMedicaoDTOBuilder submetaMedicaoBuilder = SubmetaMedicaoDTOBuilder.newSubmetaMedicaoBuilder_ComServico(qtdPlanejadaServico);
		submetaMedicaoBuilder.addQuantidadeTotalMasSemMedicaoParaServicoCorrenteEmpresa();
		submetaMedicaoBuilder.addQuantidadeTotalMasSemMedicaoParaServicoCorrenteConvenente();
		submetaMedicaoBuilder.addQuantidadeTotalMasSemMedicaoParaServicoCorrenteConcedente();
		
		SubmetaMedicaoDTO submetaMedicao = submetaMedicaoBuilder.create();
		
		prepararServicoStep.process(submetaMedicao, contexto);
		
		ServicoVrplDTO servico = submetaMedicao.getFrentesObra().get(0).getMacroServicosView().get(0).getServicos().get(0);
		
		assertFalse(servico.isPermiteMedicao());
		assertEquals(BigDecimal.valueOf(0.00), servico.getQtdMaxPermitido());
	}
}
