package br.gov.planejamento.siconv.med.medicao.business.builder;

import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.CONCEDENTE;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.EMPRESA;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.PROPONENTE_CONVENENTE;
import static br.gov.planejamento.siconv.med.infra.util.MathUtil.is;
import static br.gov.planejamento.siconv.med.infra.util.MathUtil.nullSafeAdd;
import static br.gov.planejamento.siconv.med.infra.util.MathUtil.zeroIfNull;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.CC;
import static java.math.BigDecimal.ZERO;
import static java.util.stream.Collectors.toList;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.OptionalLong;
import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import br.gov.planejamento.siconv.med.infra.security.SecurityContext;
import br.gov.planejamento.siconv.med.infra.security.domain.Profile;
import br.gov.planejamento.siconv.med.medicao.business.builder.SubmetaMedicaoBuilder.Context;
import br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum;
import br.gov.planejamento.siconv.med.medicao.entity.dto.MedicaoDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.ServicoVrplDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.ServicoVrplDTO.ValorServicoBM;
import br.gov.planejamento.siconv.med.medicao.entity.dto.SubmetaMedicaoDTO;
import io.quarkus.arc.Unremovable;

@Unremovable
@ApplicationScoped
public class PrepararServicoStep extends AbstractSubmetaMedicaoStep {

	@Inject
	public PrepararServicoStep(SecurityContext securityContext) {
		super(securityContext);
	}

	@Override
	public void process(SubmetaMedicaoDTO submetaMedicao, Context builderContext) {

		MedicaoDTO medicaoAtual = builderContext.getMedicao();

		super.getServicosfromMacroServico(submetaMedicao).forEach(servicoMedicao -> {

			if (securityContext.isUserInProfile(EMPRESA)) {
				preencherServicoPerfilEmpresa(servicoMedicao, submetaMedicao, medicaoAtual);

			} else if (securityContext.isUserInProfile(PROPONENTE_CONVENENTE)) {
				preencherServicoPerfilConvenente(servicoMedicao, submetaMedicao, medicaoAtual);

			} else if (securityContext.isUserInProfile(CONCEDENTE)
					|| securityContext.isUserInProfile(Profile.MANDATARIA)) {
				preencherServicoPerfilConcedente(servicoMedicao, submetaMedicao);

			} else {
				servicoMedicao.setPermiteMedicao(Boolean.FALSE);
				servicoMedicao.setQtdMaxPermitido(ZERO);
			}

			servicoMedicao.setPossuiGlosasAnterioresConvenente(
					preencheIndicadorGlosaConvenente(servicoMedicao, medicaoAtual));

			servicoMedicao.setPossuiGlosasAnterioresConcedente(
					preencherIndicadorGlosaConcedente(servicoMedicao, medicaoAtual));
		});
	}

	private void preencherServicoPerfilEmpresa(ServicoVrplDTO servicoMedicao, SubmetaMedicaoDTO submetaMedicao,
			MedicaoDTO medicaoAtual) {

		if (submetaMedicao.isPermiteMarcacaoEmpresa()
				&& (!medicaoAtual.isAcumulada() || servicoMedicao.getQtdRealizadoEmpresa() != null)) {

			servicoMedicao.setQtdMaxPermitido(calcularQtdMaxPermitidoEmpresa(medicaoAtual, servicoMedicao));
			servicoMedicao.setPermiteMedicao(is(servicoMedicao.getQtdMaxPermitido()).greaterThan(ZERO));

		} else {
			servicoMedicao.setQtdMaxPermitido(ZERO);
			servicoMedicao.setPermiteMedicao(false);
		}
	}

	/**
	 * Calcula o valor máximo que a empresa poderá preencher como quantidade
	 * realizada para um serviço específico da medição informada.
	 * <p>
	 * A regra geral de cálculo segue a seguinte fórmula:
	 * 
	 * <pre>
	 * valorMaximo = qtdPlanejadoServico - (qtdAcumuladoEmpresaAteMedicaoAtual - qtdRealizadoEmpresaMedicaoAtual)
	 * </pre>
	 * 
	 * Como exceção à regra, para medições acumuladas (filhas) com situação 'CE - Em
	 * complementação pela Empresa', é necessário levar em consideração as medições
	 * posteriores para definir o valor máximo que a empresa poderá informar na
	 * medição atual. Sendo assim, a fórmula de cálculo passa a ser a seguinte:
	 * 
	 * <pre>
	 * valorMaximo = qtdPlanejadoServico - (qtdAcumuladoEmpresaTodasMedicoes - qtdRealizadoEmpresaMedicaoAtual)
	 * </pre>
	 * 
	 * @param medicaoAtual   Medição considerada para lançamento do valor do serviço
	 * @param servicoMedicao Serviço específico já considerando Submeta/Frente de
	 *                       Obra/Macrosserviço
	 * @return Quantidade máxima realizada pela empresa permitida
	 */
	private BigDecimal calcularQtdMaxPermitidoEmpresa(MedicaoDTO medicaoAtual, ServicoVrplDTO servicoMedicao) {

		BigDecimal qtdPlanejado = servicoMedicao.getQtd();
		BigDecimal qtdRealizadoEmpresaMedicaoAtual = zeroIfNull(servicoMedicao.getQtdRealizadoEmpresa());

		if (medicaoAtual.isAcumulada() && medicaoAtual.getSituacao() == SituacaoMedicaoEnum.CE) {

			BigDecimal qtdAcumuladoEmpresaTodasMedicoes = zeroIfNull(servicoMedicao.getQtdAcumuladoEmpresaTodasMedicoes());

			return qtdPlanejado.subtract(qtdAcumuladoEmpresaTodasMedicoes.subtract(qtdRealizadoEmpresaMedicaoAtual));

		} else {

			BigDecimal qtdAcumuladoEmpresaAteMedicaoAtual = zeroIfNull(servicoMedicao.getQtdAcumuladoEmpresa());

			return qtdPlanejado.subtract(qtdAcumuladoEmpresaAteMedicaoAtual.subtract(qtdRealizadoEmpresaMedicaoAtual));
		}
	}

	/**
	 * Preenche os seguintes atributos do Serviço para o Convenente
	 * (permiteMedicaoq, qtdMaxPermitido, possuiGlosaConvenente)
	 * 
	 * @param servicoMedicao
	 * @param medicaoAtual
	 * @param builderContext
	 */
	private void preencherServicoPerfilConvenente(ServicoVrplDTO servicoMedicao, SubmetaMedicaoDTO submetaMedicao,
			MedicaoDTO medicaoAtual) {

		BigDecimal qtdAcumuladoEmpresa = zeroIfNull(servicoMedicao.getQtdAcumuladoEmpresa());
		
		// Se permite marcação do convenente E 
		// O serviço foi medido pela empresa, há o que o convenente medir
		if (submetaMedicao.isPermiteMarcacaoConvenente() && is(qtdAcumuladoEmpresa).greaterThan(ZERO) &&
				(!medicaoAtual.isAcumulada() || servicoMedicao.getQtdRealizadoConvenente() != null)) {
								
			BigDecimal qtdMaximaPermitidaConvenente = calcularQtdMaximaPermitidaConvenente(servicoMedicao, medicaoAtual);

			servicoMedicao.setPermiteMedicao(is(qtdMaximaPermitidaConvenente).greaterThan(ZERO));
			servicoMedicao.setQtdMaxPermitido(qtdMaximaPermitidaConvenente);			
		} else {
			servicoMedicao.setPermiteMedicao(Boolean.FALSE);
			servicoMedicao.setQtdMaxPermitido(ZERO);
		}
	}

	private Boolean preencheIndicadorGlosaConvenente(ServicoVrplDTO servicoMedicao, MedicaoDTO medicaoAtual) {

		OptionalLong idMedAnteriorConvenente = servicoMedicao.getValoresPorIdMedicao().entrySet().stream()
				.filter(e -> e.getKey() < medicaoAtual.getId() && e.getValue().getQtdConvenente() != null)
				.mapToLong(Entry::getKey).max();

		if (idMedAnteriorConvenente.isPresent()) {

			BigDecimal acumuladoEmpresa = obterValorAcumulado(servicoMedicao, idMedAnteriorConvenente.getAsLong(),
					ServicoVrplDTO.ValorServicoBM::getQtdEmpresa);

			BigDecimal acumuladoConvenente = obterValorAcumulado(servicoMedicao, idMedAnteriorConvenente.getAsLong(),
					ServicoVrplDTO.ValorServicoBM::getQtdConvenente);

			return !is(acumuladoConvenente).equalTo(acumuladoEmpresa);
		}

		return false;
	}

	private BigDecimal obterValorAcumulado(ServicoVrplDTO servicoMedicao, Long idMedicaoBase,
			Function<ServicoVrplDTO.ValorServicoBM, BigDecimal> funcaoRetornoQuantidade) {
		return servicoMedicao.getValoresPorIdMedicao().entrySet().stream()
				.filter(entry -> entry.getKey() <= idMedicaoBase)
				.map(Map.Entry::getValue)
				.map(funcaoRetornoQuantidade)
				.filter(Objects::nonNull)
				.reduce(BigDecimal::add)
				.orElse(null);
	}

	private BigDecimal calcularQtdMaximaPermitidaConvenente(ServicoVrplDTO servicoMedicao, MedicaoDTO medicaoAtual) {

		BigDecimal qtdAcumuladoEmpresa = zeroIfNull(servicoMedicao.getQtdAcumuladoEmpresa());
		BigDecimal qtdAcumuladoConvenente = zeroIfNull(servicoMedicao.getQtdAcumuladoConvenente());
		BigDecimal qtdRealizadoConvenente = zeroIfNull(servicoMedicao.getQtdRealizadoConvenente());

		if (medicaoAtual.isAcumulada() && medicaoAtual.getSituacao() == CC) {

			List<ValorServicoBM> valoresMedicoesPosteriores = servicoMedicao.getValoresPorIdMedicao().entrySet()
					.stream()
					.filter(e -> e.getKey() > medicaoAtual.getId())
					.sorted(Entry.comparingByKey())
					.map(Entry::getValue)
					.collect(toList());

			BigDecimal menorDiferencaAcumulada = qtdAcumuladoEmpresa.subtract(qtdAcumuladoConvenente);

			for (ValorServicoBM valor : valoresMedicoesPosteriores) {
				qtdAcumuladoEmpresa = nullSafeAdd(qtdAcumuladoEmpresa, valor.getQtdEmpresa());
				qtdAcumuladoConvenente = nullSafeAdd(qtdAcumuladoConvenente, valor.getQtdConvenente());
				BigDecimal diferencaAcumulada = qtdAcumuladoEmpresa.subtract(qtdAcumuladoConvenente);

				if (is(menorDiferencaAcumulada).greaterThan(diferencaAcumulada)) {
					menorDiferencaAcumulada = diferencaAcumulada;
				}
			}

			return qtdRealizadoConvenente.add(menorDiferencaAcumulada);

		} else {
			return qtdAcumuladoEmpresa.subtract(qtdAcumuladoConvenente.subtract(qtdRealizadoConvenente));
		}
	}

	private void preencherServicoPerfilConcedente(ServicoVrplDTO servicoMedicao, SubmetaMedicaoDTO submetaMedicao) {

		if (submetaMedicao.isPermiteMarcacaoConcedente()) {
			servicoMedicao.setQtdMaxPermitido(calcularQtdMaxPermitidoConcedente(servicoMedicao));
			servicoMedicao.setPermiteMedicao(is(servicoMedicao.getQtdMaxPermitido()).greaterThan(ZERO));

		} else {
			servicoMedicao.setQtdMaxPermitido(ZERO);
			servicoMedicao.setPermiteMedicao(false);
		}
	}

	/**
	 * Calcula o valor máximo que o concedente poderá preencher como quantidade
	 * realizada para um serviço específico da medição informada.
	 * <p>
	 * A regra geral de cálculo segue a seguinte fórmula:
	 * 
	 * <pre>
	 * valorMaximo = qtdAcumuladoConvenente - (qtdAcumuladoConcedente - qtdRealizadoConcedenteMedicaoAtual)
	 * </pre>
	 * 
	 * @param servicoMedicao Serviço específico já considerando Submeta/Frente de
	 *                       Obra/Macrosserviço
	 * @return Quantidade máxima realizada pelo concedente permitida
	 */
	private BigDecimal calcularQtdMaxPermitidoConcedente(ServicoVrplDTO servicoMedicao) {

		BigDecimal qtdAcumuladoConvenente = zeroIfNull(servicoMedicao.getQtdAcumuladoConvenente());
		BigDecimal qtdAcumuladoConcedente = zeroIfNull(servicoMedicao.getQtdAcumuladoConcedente());
		BigDecimal qtdRealizadoConcedenteMedicaoAtual = zeroIfNull(servicoMedicao.getQtdRealizadoConcedente());

		return qtdAcumuladoConvenente.subtract(qtdAcumuladoConcedente.subtract(qtdRealizadoConcedenteMedicaoAtual));
	}

	private Boolean preencherIndicadorGlosaConcedente(ServicoVrplDTO servicoMedicao, MedicaoDTO medicaoAtual) {

		OptionalLong idMedAnteriorConcedente = servicoMedicao.getValoresPorIdMedicao().entrySet().stream()
				.filter(e -> e.getKey() < medicaoAtual.getId() && e.getValue().getQtdConcedente() != null)
				.mapToLong(Entry::getKey).max();

		if (idMedAnteriorConcedente.isPresent()) {
			BigDecimal acumuladoConvenente = obterValorAcumulado(servicoMedicao, idMedAnteriorConcedente.getAsLong(),
					ServicoVrplDTO.ValorServicoBM::getQtdConvenente);

			BigDecimal acumuladoConcedente = obterValorAcumulado(servicoMedicao, idMedAnteriorConcedente.getAsLong(),
					ServicoVrplDTO.ValorServicoBM::getQtdConcedente);

			return !is(acumuladoConcedente).equalTo(acumuladoConvenente);
		}

		return false;
	}

}
