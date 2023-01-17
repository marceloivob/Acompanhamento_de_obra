package br.gov.planejamento.siconv.med.medicao.business.builder;

import static br.gov.planejamento.siconv.med.test.builder.ContextBuilder.newContextBuilder;
import static java.math.BigDecimal.valueOf;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;

import br.gov.planejamento.siconv.med.medicao.business.builder.IndicadoresSubmetaServicoStep;
import br.gov.planejamento.siconv.med.medicao.business.builder.SubmetaMedicaoBuilder.Context;
import br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum;
import br.gov.planejamento.siconv.med.medicao.entity.SituacaoSubmetaEnum;
import br.gov.planejamento.siconv.med.medicao.entity.database.SubmetaMedicaoBD;
import br.gov.planejamento.siconv.med.medicao.entity.dto.MedicaoDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.SubmetaMedicaoDTO;
import br.gov.planejamento.siconv.med.test.builder.SubmetaMedicaoDTOBuilder;
import br.gov.planejamento.siconv.med.test.extension.BusinessControllerBaseTest;

class IndicadoresSubmetaEventoStepTest extends BusinessControllerBaseTest {

	@InjectMocks
	private IndicadoresSubmetaEventoStep step;

	private static final String CPF = "11111111111";

	// ========================= Métodos utilitários =========================

	private SubmetaMedicaoDTOBuilder getSubmetaBuilder() {
		return SubmetaMedicaoDTOBuilder.newSubmetaMedicaoBuilder_ComEvento(1L);
	}

	private Context buildContextComMedicao(MedicaoDTO medicao) {
		return newContextBuilder().setContext(null, medicao, new HashMap<>(), new ArrayList<SubmetaMedicaoBD>())
				.create();
	}

	// =================== Testes do indicador permiteMarcacaoEmpresa ===================

	@ParameterizedTest
	@ValueSource(strings = { "EC", "EXC", "AT", "ATD", "ECE", "AC", "ACT", "ECC", "CC" })
	void testPermiteMarcacaoEmpresa_situacaoMedicaoInvalida(String codigoSituacao) {

		MedicaoDTO medicao = new MedicaoDTO();
		medicao.setId(1L);
		medicao.setSituacao(SituacaoMedicaoEnum.fromCodigo(codigoSituacao));

		SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilder().create();

		step.process(submetaMedicao, buildContextComMedicao(medicao));

		assertFalse(submetaMedicao.isPermiteMarcacaoEmpresa());
	}

	@Test
	void testPermiteMarcacaoEmpresa_medicaoBloqueada() {

		MedicaoDTO medicao = new MedicaoDTO();
		medicao.setSituacao(SituacaoMedicaoEnum.EM);
		medicao.setBloqueada(true);

		SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilder().create();

		step.process(submetaMedicao, buildContextComMedicao(medicao));

		assertFalse(submetaMedicao.isPermiteMarcacaoEmpresa());
	}

	@Test
	void testPermiteMarcacaoEmpresa_eventoPendentePreenchimento() {

		MedicaoDTO medicao = new MedicaoDTO();
		medicao.setId(1L);
		medicao.setSituacao(SituacaoMedicaoEnum.EM);

		SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilder().create();

		step.process(submetaMedicao, buildContextComMedicao(medicao));

		assertTrue(submetaMedicao.isPermiteMarcacaoEmpresa());
	}

	@Test
	void testPermiteMarcacaoEmpresa_eventoConcluidoMedicaoAtual() {

		MedicaoDTO medicao = new MedicaoDTO();
		medicao.setId(2L);
		medicao.setSituacao(SituacaoMedicaoEnum.EM);

		SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilder().create();
		submetaMedicao.getFrentesObra().get(0).getEventos().get(0).setIdMedicaoEmpresa(2L);

		step.process(submetaMedicao, buildContextComMedicao(medicao));

		assertTrue(submetaMedicao.isPermiteMarcacaoEmpresa());
	}

	@Test
	void testPermiteMarcacaoEmpresa_eventoConcluidoMedicaoAnterior() {

		MedicaoDTO medicao = new MedicaoDTO();
		medicao.setId(2L);
		medicao.setSituacao(SituacaoMedicaoEnum.EM);

		SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilder().create();
		submetaMedicao.getFrentesObra().get(0).getEventos().get(0).setIdMedicaoEmpresa(1L);

		step.process(submetaMedicao, buildContextComMedicao(medicao));

		assertFalse(submetaMedicao.isPermiteMarcacaoEmpresa());
	}

	@Test
	void testPermiteMarcacaoEmpresa_medicaoAcumuladaSemPreenchimentoOriginal() {

		MedicaoDTO medicao = new MedicaoDTO();
		medicao.setId(1L);
		medicao.setSituacao(SituacaoMedicaoEnum.CE);
		medicao.setIdMedicaoAgrupadora(2L);

		SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilder().create();

		step.process(submetaMedicao, buildContextComMedicao(medicao));

		assertFalse(submetaMedicao.isPermiteMarcacaoEmpresa());
	}

	@Test
	void testPermiteMarcacaoEmpresa_medicaoAcumuladaComPreenchimentoOriginal() {

		MedicaoDTO medicao = new MedicaoDTO();
		medicao.setId(1L);
		medicao.setSituacao(SituacaoMedicaoEnum.CE);
		medicao.setIdMedicaoAgrupadora(2L);

		SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilder().setEventoMedidoEmpresa(medicao.getId()).create();

		step.process(submetaMedicao, buildContextComMedicao(medicao));

		assertFalse(submetaMedicao.isPermiteMarcacaoEmpresa());
	}

	@Test
	void testPermiteMarcacaoEmpresa_permiteComplementacaoValorFalse() {

		MedicaoDTO medicao = new MedicaoDTO();
		medicao.setSituacao(SituacaoMedicaoEnum.CE);
		medicao.setBloqueada(false);
		medicao.setPermiteComplementacaoValor(false);

		SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilder().create();

		step.process(submetaMedicao, buildContextComMedicao(medicao));

		assertFalse(submetaMedicao.isPermiteMarcacaoEmpresa());
	}

	@Test
	void testPermiteMarcacaoEmpresa_permiteComplementacaoValorTrue() {

		MedicaoDTO medicao = new MedicaoDTO();
		medicao.setSituacao(SituacaoMedicaoEnum.CE);
		medicao.setBloqueada(false);
		medicao.setPermiteComplementacaoValor(true);

		SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilder().create();

		step.process(submetaMedicao, buildContextComMedicao(medicao));

		assertTrue(submetaMedicao.isPermiteMarcacaoEmpresa());
	}

	// =================== Testes do indicador permiteMarcacaoConvenente =================

	@ParameterizedTest
	@ValueSource(strings = { "EM", "EC", "EXC", "ATD", "ECE", "CE", "AC", "ACT", "ECC" })
	void testPermiteMarcacaoConvenente_situacaoMedicaoInvalida(String codigoSituacao) {

		MedicaoDTO medicao = new MedicaoDTO();
		medicao.setId(1L);
		medicao.setSituacao(SituacaoMedicaoEnum.fromCodigo(codigoSituacao));

		SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilder().assinarEmpresa(CPF)
				.setEventoMedidoEmpresa(medicao.getId()).create();

		step.process(submetaMedicao, buildContextComMedicao(medicao));

		assertFalse(submetaMedicao.isPermiteMarcacaoConvenente());
	}

	@Test
	void testPermiteMarcacaoConvenente_medicaoBloqueada() {

		MedicaoDTO medicao = new MedicaoDTO();
		medicao.setId(1L);
		medicao.setSituacao(SituacaoMedicaoEnum.AT);
		medicao.setBloqueada(true);

		SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilder().assinarEmpresa(CPF)
				.setEventoMedidoEmpresa(medicao.getId()).create();

		step.process(submetaMedicao, buildContextComMedicao(medicao));

		assertFalse(submetaMedicao.isPermiteMarcacaoConvenente());
	}

	@Test
	void testPermiteMarcacaoConvenente_medicaoAcumulada() {

		MedicaoDTO medicao = new MedicaoDTO();
		medicao.setId(1L);
		medicao.setSituacao(SituacaoMedicaoEnum.AT);
		medicao.setIdMedicaoAgrupadora(2L);

		SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilder().setEventoMedidoEmpresa(medicao.getId())
				.assinarEmpresa(CPF).create();

		step.process(submetaMedicao, buildContextComMedicao(medicao));

		assertFalse(submetaMedicao.isPermiteMarcacaoConvenente());
	}

	@Test
	void testPermiteMarcacaoConvenente_submetaSemValorAssinadaEmpresa() {

		MedicaoDTO medicao = new MedicaoDTO();
		medicao.setId(1L);
		medicao.setSituacao(SituacaoMedicaoEnum.AT);

		SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilder().assinarEmpresa(CPF).create();

		step.process(submetaMedicao, buildContextComMedicao(medicao));

		assertTrue(submetaMedicao.isPermiteMarcacaoConvenente());
	}

	@Test
	void testPermiteMarcacaoConvenente_eventoPendentePreenchimento() {

		MedicaoDTO medicao = new MedicaoDTO();
		medicao.setId(2L);
		medicao.setSituacao(SituacaoMedicaoEnum.AT);

		SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilder().setEventoMedidoEmpresa(medicao.getId())
				.assinarEmpresa(CPF).create();

		step.process(submetaMedicao, buildContextComMedicao(medicao));

		assertTrue(submetaMedicao.isPermiteMarcacaoConvenente());
	}

	@Test
	void testPermiteMarcacaoConvenente_eventoConcluidoMedicaoAtual() {

		MedicaoDTO medicao = new MedicaoDTO();
		medicao.setId(2L);
		medicao.setSituacao(SituacaoMedicaoEnum.AT);

		SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilder().setEventoMedidoEmpresa(medicao.getId())
				.assinarEmpresa(CPF).setEventoMedidoConvenente(medicao.getId()).create();

		step.process(submetaMedicao, buildContextComMedicao(medicao));

		assertTrue(submetaMedicao.isPermiteMarcacaoConvenente());
	}

	@Test
	void testPermiteMarcacaoConvenente_eventoConcluidoMedicaoAnterior() {

		MedicaoDTO medicao = new MedicaoDTO();
		medicao.setId(2L);
		medicao.setSituacao(SituacaoMedicaoEnum.AT);

		SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilder().setEventoMedidoEmpresa(1L).assinarEmpresa(CPF)
				.setEventoMedidoConvenente(1L).assinarConvenente(CPF).setEventoMedidoConcedente(1L)
				.assinarConcedente(CPF).create();

		step.process(submetaMedicao, buildContextComMedicao(medicao));

		assertTrue(submetaMedicao.isPermiteMarcacaoConvenente());
	}

	@Test
	void testPermiteMarcacaoConvenente_AT_permiteComplementacaoValorTrue() {

		MedicaoDTO medicao = new MedicaoDTO();
		medicao.setId(1L);
		medicao.setSituacao(SituacaoMedicaoEnum.AT);
		medicao.setBloqueada(false);
		medicao.setPermiteComplementacaoValor(true);

		SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilder().setEventoMedidoEmpresa(medicao.getId())
				.assinarEmpresa(CPF).create();

		step.process(submetaMedicao, buildContextComMedicao(medicao));

		assertTrue(submetaMedicao.isPermiteMarcacaoConvenente());
	}

	@Test
	void testPermiteMarcacaoConvenente__CC_permiteComplementacaoValorTrue() {

		MedicaoDTO medicao = new MedicaoDTO();
		medicao.setId(1L);
		medicao.setSituacao(SituacaoMedicaoEnum.CC);
		medicao.setBloqueada(false);
		medicao.setPermiteComplementacaoValor(true);

		SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilder().assinarEmpresa(CPF).create();

		step.process(submetaMedicao, buildContextComMedicao(medicao));

		assertTrue(submetaMedicao.isPermiteMarcacaoConvenente());
	}

	@Test
	void testPermiteMarcacaoConvenente_AT_permiteComplementacaoValorFalse() {

		MedicaoDTO medicao = new MedicaoDTO();
		medicao.setId(1L);
		medicao.setSituacao(SituacaoMedicaoEnum.AT);
		medicao.setBloqueada(false);
		medicao.setPermiteComplementacaoValor(false);

		SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilder().assinarEmpresa(CPF).create();

		step.process(submetaMedicao, buildContextComMedicao(medicao));

		assertFalse(submetaMedicao.isPermiteMarcacaoConvenente());
	}

	@Test
	void testPermiteMarcacaoConvenente__CC_permiteComplementacaoValorFalse() {

		MedicaoDTO medicao = new MedicaoDTO();
		medicao.setSituacao(SituacaoMedicaoEnum.CC);
		medicao.setBloqueada(false);
		medicao.setPermiteComplementacaoValor(false);

		SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilder().assinarEmpresa(CPF).create();

		step.process(submetaMedicao, buildContextComMedicao(medicao));

		assertFalse(submetaMedicao.isPermiteMarcacaoConvenente());
	}

	// ================= Testes do indicador permiteMarcacaoConcedente =================

	@ParameterizedTest
	@ValueSource(strings = { "EM", "EC", "EXC", "AT", "ATD", "ECE", "ACT", "ECC", "CC" })
	void testPermiteMarcacaoConcedente_situacaoMedicaoInvalida(String codigoSituacao) {

		MedicaoDTO medicao = new MedicaoDTO();
		medicao.setSituacao(SituacaoMedicaoEnum.fromCodigo(codigoSituacao));

		SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilder().assinarEmpresa(CPF).assinarConvenente(CPF).create();

		step.process(submetaMedicao, buildContextComMedicao(medicao));

		assertFalse(submetaMedicao.isPermiteMarcacaoConcedente());
	}

	@Test
	void testPermiteMarcacaoConcedente_medicaoBloqueada() {

		MedicaoDTO medicao = new MedicaoDTO();
		medicao.setSituacao(SituacaoMedicaoEnum.AC);
		medicao.setBloqueada(true);

		SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilder().assinarEmpresa(CPF).assinarConvenente(CPF).create();

		step.process(submetaMedicao, buildContextComMedicao(medicao));

		assertFalse(submetaMedicao.isPermiteMarcacaoConcedente());
	}

	@Test
	void testPermiteMarcacaoConcedente_medicaoAcumulada() {

		MedicaoDTO medicao = new MedicaoDTO();
		medicao.setId(1L);
		medicao.setSituacao(SituacaoMedicaoEnum.AC);
		medicao.setIdMedicaoAgrupadora(2L);

		SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilder().assinarEmpresa(CPF).assinarConcedente(CPF).create();

		step.process(submetaMedicao, buildContextComMedicao(medicao));

		assertFalse(submetaMedicao.isPermiteMarcacaoConcedente());
	}

	@Test
	void testPermiteMarcacaoConcedente_eventoSemValorAssinadaConvenente() {

		MedicaoDTO medicao = new MedicaoDTO();
		medicao.setSituacao(SituacaoMedicaoEnum.AC);

		SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilder().assinarEmpresa(CPF).assinarConvenente(CPF).create();

		step.process(submetaMedicao, buildContextComMedicao(medicao));

		assertTrue(submetaMedicao.isPermiteMarcacaoConcedente());
	}

	@Test
	void testPermiteMarcacaoConcedente_eventoPendentePreenchimento() {

		MedicaoDTO medicao = new MedicaoDTO();
		medicao.setId(2L);
		medicao.setSituacao(SituacaoMedicaoEnum.AC);

		SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilder().assinarEmpresa(CPF).assinarConvenente(CPF).create();

		step.process(submetaMedicao, buildContextComMedicao(medicao));

		assertTrue(submetaMedicao.isPermiteMarcacaoConcedente());
	}

	@Test
	void testPermiteMarcacaoConcedente_eventoMedidoMedicaoAtual() {

		MedicaoDTO medicao = new MedicaoDTO();
		medicao.setId(2L);
		medicao.setSituacao(SituacaoMedicaoEnum.AC);

		SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilder().setEventoMedidoEmpresa(medicao.getId())
				.assinarEmpresa(CPF).setEventoMedidoConvenente(medicao.getId()).assinarConvenente(CPF)
				.setEventoMedidoConcedente(medicao.getId()).create();

		step.process(submetaMedicao, buildContextComMedicao(medicao));

		assertTrue(submetaMedicao.isPermiteMarcacaoConcedente());
	}

	@Test
	void testPermiteMarcacaoConcedente_eventoMedidoMedicaoAnterior() {

		MedicaoDTO medicao = new MedicaoDTO();
		medicao.setId(2L);
		medicao.setSituacao(SituacaoMedicaoEnum.AC);

		SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilder().setEventoMedidoEmpresa(medicao.getId())
				.assinarEmpresa(CPF).setEventoMedidoConvenente(medicao.getId()).assinarConvenente(CPF)
				.setEventoMedidoConcedente(medicao.getId()).assinarConcedente(CPF).create();

		step.process(submetaMedicao, buildContextComMedicao(medicao));

		assertTrue(submetaMedicao.isPermiteMarcacaoConcedente());
	}
}
